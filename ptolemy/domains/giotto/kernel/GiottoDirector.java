/* Director for the Giotto model of computation.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.NoTokenException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.InternalErrorException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirector
/**

FIXME: document this.

@see ptolemy.domains.kernel.GiottoReceiver
@see ptolemy.domains.kernel.GiottoScheduler

@author  Christoph Meyer, Ben Horowitz, and Edward A. Lee
@version $Id$
*/
public class GiottoDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GiottoDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public GiottoDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the base class has an
     *   "iterations" parameter (which it should not), or if the container is
     *   not a CompositeActor and the name collides with an entity in the
     *   container.
     */
    public GiottoDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false.  If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the interations member.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        GiottoDirector newobj = (GiottoDirector)(super.clone(ws));
        newobj.iterations = (Parameter)newobj.getAttribute("iterations");
        return newobj;
    }

    /** Return the real start time.
     *  @return The real start time in terms of milliseconds counting 
     *  from 1/1/1970
     */
    public long getRealStartTime() {
        return _realStartTime;
    }

    /** Calculate the current schedule, if necessary, and iterate
     *  the contained actors in the order given by the schedule.
     *
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
        _postfirereturns = true;

        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        if (container != null) {
            Enumeration giottoSchedule = getScheduler().schedule();

	    if (_debugging)
		_debug("Giotto director firing!");
	    
	    _realStartTime = System.currentTimeMillis();

	    _postfirereturns = _fire(giottoSchedule);
        } else
	    throw new IllegalActionException(this, "Has no container!");
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new GiottoReceiver();

        _receivers.add(receiver);

        return receiver;
    }

    /** Initialize the actors associated with this director and
     *  initialize the iteration count to zero.  The order in which
     *  the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _iteration = 0;
    }

    /** Update all the receivers that have been created by the
     *  newReceiver() method of this director.
     *  Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the model
     *  return false in postfire.
     *  Increment the number of iterations.
     *  @return True if the execution is not finished.
     *  @throws IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
        // NOTE: Some of these receivers may no longer be in use, so this
        // will be inefficient for models that are continually mutating.
        // However, it is functionally harmless.

        /* Legacy: Iterator receivers = _receivers.iterator();

	   while(receivers.hasNext()) {
	   GiottoReceiver receiver = (GiottoReceiver) receivers.next();

	   receiver.update();
	   } */

        int numiterations = ((IntToken) (iterations.getToken())).intValue();

        _iteration++;

        if((numiterations > 0) && (_iteration >= numiterations)) {
            _iteration = 0;

            return false;
        }

        return _postfirereturns;
    }

    /** Return true if it transfers data from an input port of the
     *  container to the ports it is connected to on the inside.
     *  The port argument must  be an opaque input port.  If any
     *  channel of the input port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each input channel that has at least one token available.
     *  Update all receivers to which token is transferred.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i = 0; i < port.getWidth(); i++) {
	    if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debugging) _debug(getName(),
                                "transferring input from " + port.getName());
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
			    ((GiottoReceiver)insiderecs[i][j]).update();
                        }
                        trans = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
        return trans;
    }

    /** Return the next time of interest in the Giotto model.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
	// FIXME: actor must become method argument.
	Actor actor = null;

	double currentTime = getCurrentTime();

	int actorFrequency = GiottoActorComparator.getFrequency(actor);

        return currentTime + (_period / actorFrequency);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The static default Giotto period is 100ms.
    protected static double _DEFAULT_GIOTTO_PERIOD = 0.1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the director by creating a scheduler and iterations
     *  parameter.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        GiottoScheduler scheduler = new GiottoScheduler(workspace());

        setScheduler(scheduler);

	_setPeriod(_DEFAULT_GIOTTO_PERIOD);

	setCurrentTime(0.0);

        iterations = new Parameter(this, "iterations", new IntToken(0));

        // FIXME: Remove this after debugging, or when GUI supports it.
        addDebugListener(new StreamListener());
    }

    private boolean _fire(Enumeration schedule)
	throws IllegalActionException {

	boolean postfire = true;

	if (schedule != null)
	    while (schedule.hasMoreElements()) {
		List sameFrequencyList = (List) schedule.nextElement();

		Enumeration sameFrequency = Collections.enumeration(sameFrequencyList);

		while (sameFrequency.hasMoreElements()) {
		    Actor actor = (Actor) sameFrequency.nextElement();

		    if (_debugging)
			_debug("Prefiring " + ((NamedObj)actor).getFullName());

		    if (actor.prefire()) {
			if (_debugging)
			    _debug("Firing " + ((NamedObj)actor).getFullName());

			actor.fire();
		    }

		    if (_debugging)
			_debug("Postfiring " + ((NamedObj)actor).getFullName());

		    if (!actor.postfire())
			postfire = false;
		}

		// Assumption: schedule has even number of elements.

		List higherFrequencyList = (List) schedule.nextElement();
		
		if (higherFrequencyList != null) {
		    Enumeration higherFrequency = Collections.enumeration(higherFrequencyList);

		    // Recursive call.
		    postfire = _fire(higherFrequency) && postfire;
		} else {
		    // Update time for every invocation of the most frequent tasks
		    // which are stored at the bottom of the tree.
		    double currentTime;

		    currentTime = getCurrentTime();

		    // What is the highest frequency?
		    // We look it up in the first actor.
		    // Assumption: sameFrequencyList is non-empty.
		    Actor actor = (Actor) sameFrequencyList.get(0);

		    int maxFrequency = GiottoActorComparator.getFrequency(actor);

		    setCurrentTime(currentTime + (_period / maxFrequency));

		    if (_synchronizeToRealTime) {
			long elapsedTime = System.currentTimeMillis() - _realStartTime;

			double elapsedTimeInSeconds = ((double) elapsedTime) / 1000.0;

			if (currentTime > elapsedTimeInSeconds) {
			    long timeToWait = (long) ((currentTime - elapsedTimeInSeconds) * 1000.0);

			    if (timeToWait > 0) {
				if (_debugging) {
				    _debug("Waiting for real time to pass: " + timeToWait);
				}

				// FIXME: Do I need to synchronize on anything?
				Scheduler scheduler = getScheduler();

				synchronized(scheduler) {
				    try {
					scheduler.wait(timeToWait);
				    } catch (InterruptedException ex) {
					// Continue executing.
				    }
				}
			    }
			}
		    }
		}

		sameFrequency = Collections.enumeration(sameFrequencyList);

		while (sameFrequency.hasMoreElements()) {
		    Actor actor = (Actor) sameFrequency.nextElement();

		    if (_debugging)
			_debug("Updating " + ((NamedObj)actor).getFullName());

		    List outputPortList = actor.outputPortList();

		    Enumeration outputPorts = Collections.enumeration(outputPortList);

		    while (outputPorts.hasMoreElements()) {
			IOPort port = (IOPort) outputPorts.nextElement();

			Receiver[][] channelArray = port.getRemoteReceivers();

			for (int i = 0; i < channelArray.length; i++) {
			    Receiver[] receiverArray = channelArray[i];

			    for (int j = 0; j < receiverArray.length; j++) {
				GiottoReceiver receiver = (GiottoReceiver) receiverArray[j];

				receiver.update();
			    }
			}
		    }
		}
	    }

	return postfire;
    }

    private void _setPeriod(double period) {
	_period = period;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The current period in milliseconds.
    private double _period = _DEFAULT_GIOTTO_PERIOD;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime = false;

    // The real time at which the last unit has been invoked.
    private long _realStartTime = 0;

    // The count of iterations executed.
    private int _iteration = 0;

    // The anded result of the values returned by actors' postfire().
    private boolean _postfirereturns = true;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();
}
