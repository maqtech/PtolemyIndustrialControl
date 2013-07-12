/* MetroSequentialSDFDirector extends SDFDirector to support Metro semantics.
 
Copyright (c) 2012-2013 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.metroII.kernel;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
///////////////////////////////////////////////////////////////////
//// MetroSequentialSDFDirector

/** 
* MetroSequentialSDFDirector extends SDFDirector to support Metro semantics.
* The order of actor firing should be static and identical to fire(). Each 
* actor is fired only if the associated Metro event is notified.
* 
* @author Liangpeng Guo
* @version $Id$
* @since Ptolemy II 9.1
* @Pt.ProposedRating Red (glp)
* @Pt.AcceptedRating Red (glp)
*
*/
public class MetroSequentialSDFDirector extends SDFDirector implements
        MetroEventHandler {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroSequentialSDFDirector() throws IllegalActionException,
            NameDuplicationException {
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroSequentialSDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public MetroSequentialSDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Clone the object into the specified workspace. The new object
     *  is <i>not</i> added to the directory of that workspace (you
     *  must do this yourself if you want it there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroSequentialSDFDirector newObject = (MetroSequentialSDFDirector) super
                .clone(workspace);
        newObject._actorDictionary = (Hashtable<String, FireMachine>) _actorDictionary
                .clone();
        newObject._pendingIteration = (Hashtable<String, Integer>) _pendingIteration
                .clone();

        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Initialize the list of actors. The actors implementing MetroEventHandler are wrapped by ResumableFire,
     * otherwise are wrapped by BlockingFire. 
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Nameable container = getContainer();

        // In the actor library, the container might be an moml.EntityLibrary.
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            _actorDictionary.clear();
            _pendingIteration.clear();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof MetroEventHandler) {
                    _actorDictionary.put(actor.getFullName(),
                            new ResumableFire(actor));
                } else {
                    _actorDictionary.put(actor.getFullName(), new BlockingFire(
                            actor));
                }
                _pendingIteration.put(actor.getFullName(), 0);
            }
        }
    }

    /**
     * YieldAdapter interface
     */
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    /**
     * The order of actor firing should be static and identical to fire(). Each 
     * actor is fired only if the associated Metro event is notified.
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {

        _prefire = false;
        try {
            // Don't call "super.fire();" here because if you do then
            // everything happens twice.
            Iterator firings = null;

            Scheduler scheduler = getScheduler();

            if (scheduler == null) {
                throw new IllegalActionException("Attempted to fire "
                        + "system with no scheduler");
            }

            // This will throw IllegalActionException if this director
            // does not have a container.
            Schedule schedule = scheduler.getSchedule();
            firings = schedule.firingIterator();

            Firing firing = null;
            while (firings.hasNext() && !_stopRequested) {
                firing = (Firing) firings.next();
                Actor actor = firing.getActor();

                int iterationCount = firing.getIterationCount();

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE, iterationCount));
                }
                _pendingIteration.put(actor.getFullName(), iterationCount);
                
                int returnValue = Executable.NOT_READY;
                FireMachine firingProcess = _actorDictionary.get(actor
                        .getFullName());
                while (_pendingIteration.get(actor.getFullName()) > 0) {
                    _pendingIteration.put(actor.getFullName(),
                            _pendingIteration.get(actor.getFullName()) - 1);
                    // Check if the actor has reached the end of postfire()
                    while (firingProcess.getState() != FireMachine.State.FINAL) {
                        LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                        firingProcess.startOrResume(metroIIEventList);
                        resultHandler.handleResult(metroIIEventList);
                    }
                    boolean pfire = firingProcess.actor().postfire();
                    if (!pfire) {
                        returnValue = Executable.STOP_ITERATING;
                    } else {
                        returnValue = Executable.COMPLETED;
                    }
                    firingProcess.reset();

                }

                if (returnValue == STOP_ITERATING) {
                    _postfireReturns = false;
                } else if (returnValue == NOT_READY) {
                    // See de/test/auto/knownFailedTests/DESDFClockTest.xml
                    throw new IllegalActionException(
                            this,
                            actor,
                            "Actor "
                                    + "is not ready to fire.  Perhaps "
                                    + actor.getName()
                                    + ".prefire() returned false? "
                                    + "Try debugging the actor by selecting "
                                    + "\"Listen to Actor\".  Also, for SDF check moml for "
                                    + "tokenConsumptionRate on input.");
                }

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_ITERATE, iterationCount));
                }
            }
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////
    
    /**
     * List of actors governed by MetroIIDEDirector
     */
    private Hashtable<String, FireMachine> _actorDictionary = new Hashtable<String, FireMachine>();

    /**
     * List of actors pending to fire.
     */
    private Hashtable<String, Integer> _pendingIteration = new Hashtable<String, Integer>();

}
