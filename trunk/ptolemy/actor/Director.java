/* A Director governs the execution of a CompositeActor.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Director
/**
A Director governs the execution within a CompositeActor.  A composite actor
that contains a director is said to be <i>opaque</i>, and the execution model
within the composite actor is determined by the contained director.   This
director is called the <i>local director</i> of a composite actor.
A composite
actor is also aware of the director of its container, which is referred to
as its <i>executive director</i>.
<p>
A top-level composite actor is generally associated with a <i>manager</i>
as well as
a local director.  The Manager has overall responsibility for
executing the application, and is often associated with a GUI.   Top-level
composite actors have no executive director and getExecutiveDirector() will
return null.
<p>
A local director is responsible for invoking the actors contained by the
composite.  If there is no local director, then the executive director
is given the responsibility.  The getDirector() method of CompositeActor,
therefore, returns the local director, if there is one, and otherwise
returns the executive director.  Thus, it returns whichever director
is responsible for executing the contained actors, or null if there is none.
Whatever it returns is called simply the <i>director</i> (vs. local
director or executive director).
<p>
A director implements the action methods (initialize(), prefire(), fire(),
postfire(), and wrapup()).  In this base class, default implementations
are provided that may or may not be useful in specific domains.   In general,
these methods will perform domain-dependent actions, and then call the
respective methods in all contained actors.
<p>
The director also provides methods to optimize the iteration portion of an
execution. This is done by setting the workspace to be read-only during
an iteration. In this base class, the default implementation results in
a read/write workspace. Derived classes (e.g. domain specific
directors) should override the _writeAccessRequired() method to report
that write access is not required. If none of the directors in a simulation
require write access, then it is safe to set the workspace to be read-only,
which will result in faster execution.

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Director extends NamedObj implements Executable {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Director() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public Director(Workspace workspace) {
        super(workspace, "");
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public Director(CompositeActor container, String name)
            throws IllegalActionException {
        super(container.workspace(), name);
        container.setDirector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Director newobj = (Director)super.clone(ws);
        newobj._container = null;
        return newobj;
    }

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this director.  In general, this may be called more
     *  than once in the same iteration of the director's container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  <p>
     *  In this base class, an attempt is made to fire each actor exactly
     *  once, in the order they were created.  Prefire is called once, and
     *  if prefire returns true, then fire is called once, followed by
     *  postfire.  The return value from postfire is ignored.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    public void fire() throws IllegalActionException {
        // Somewhere in here, constrained mutations should
        // probably be allowed to occur.
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                if(actor.prefire()) {
                    actor.fire();
                    actor.postfire();
                }
            }
        }
    }

    /** Schedule a firing of the given actor at the given time. It does
     *  nothing in this base class. Derived classes
     *  should override this method.
     *  <p>
     *  Note that this method is not made abstract to facilitate the use
     *  of the test suite.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // do nothing in this base class.
        // Note that, alternatively, this method could have been abstract.
        // But we didn't do that, because otherwise we wouldn't be able
        // to run Tcl Blend testscript on this class.

    }

    /** Return the container, which is the composite actor for which this
     *  is the local director.
     *  @return The CompositeActor that this director is responsible for.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the current time of the model being executed by this director.
     *  This time can be set with the setCurrentTime method. In this base
     *  class, time never passes, and there are no restrictions on valid
     *  times.
     *
     *  @return The current time.
     */
    public double getCurrentTime() {
        return _currentTime;
    }

    /** Return the next time of interest in the model being executed by
     *  this director. This method is useful for domains that perform
     *  speculative execution (such as CT).  Such a domain in a hierarchical
     *  model (i.e. CT inside DE) uses this method to determine how far
     *  into the future to execute.
     *  <p>
     *  In this base class, we return the current time.
     *  Derived classes should override this method to provide an appropriate
     *  value, if possible.
     *  <p>
     *  Note that this method is not made abstract to facilitate the use
     *  of the test suite.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        return _currentTime;
    }

    /** Create receivers and then invoke the initialize()
     *  methods of all its deeply contained actors.
     *  Set the current time to be 0.0.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        setCurrentTime(0.0);
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                _debug(getName() + " initializes actor "+
                        ((NamedObj)actor).getName());
                actor.initialize();
            }
        }
        _debug(getName() + " finished initializing.");
    }

    /** Perform domain-specific initialization on the specified actor, if any.
     *  In this base class, do nothing.
     *  This is called by the initialize() method of the actor, and may be
     *  called after the initialization phase of an execution.  In particular,
     *  in the event of mutations during an execution that introduce new
     *  actors, this method will be called as part of initializing the
     *  actor.  Typical actions a director might perform include starting
     *  threads to execute the actor or checking to see whether the actor
     *  can be managed by this director.  For example, a time-based domain
     *  (such as CT) might reject sequence based actors.
     *  @exception IllegalActionException If the actor is not acceptable
     *   to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
    }

    /** Indicate that resolved types in the model may no longer be valid.
     *  This will force type resolution to be redone on the next iteration.
     *  This method simply defers to the manager, notifying it.  If there
     *  is no container, or if it has no manager, do nothing.
     */
    public void invalidateResolvedTypes() {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Manager manager = container.getManager();
            if (manager != null) {
                manager.invalidateResolvedTypes();
            }
        }
    }

    /** Indicate that a schedule for the model may no longer be valid, if
     *  there is a schedule.  This method should be called when topology
     *  changes are made, or for that matter when any change that may
     *  invalidate the schedule is made.  In this base class, the method
     *  does nothing. In derived classes, it will cause any static
     *  schedule information to be recalculated in the prefire method
     *  of the director.
     */
    public void invalidateSchedule() {
    }

    /** Return true if this director, or any of its contained directors
     *  requires write access on the workspace during execution.
     *  If this director requires write access during execution
     *  (i.e. _writeAccessRequired() returns true), then
     *  this method returns true.   Otherwise, needWriteAccess() is called
     *  recursively on all the local directors of all deeply
     *  contained entities that are opaque composite actors.
     *  If any of those lower level directors requires write access, then
     *  this method will return true.  Otherwise, this method returns false.
     *  <p>
     *  This method is called on the top level director by the manager
     *  at the start of an execution.
     *  If it returns false (indicating that none of the directors in
     *  the model need write access on the workspace), then the manager
     *  will set the workspace to be read only during each toplevel iteration
     *  of the model.  Note that mutations can still occur, but they can
     *  only be performed by the manager.
     *
     *  @return true If this director, or any of its contained directors,
     *  needs write access to the workspace.
     *  @exception InvalidStateException If the director does not have
     *  a container.
     */
    public final boolean needWriteAccess() {
        if (_writeAccessRequired()) {
            return true;
        }
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                // find out which of those actors has a local director.
                if (actor instanceof CompositeActor &&
                        ((CompositeActor)actor).isOpaque()) {
                    CompositeActor ca = (CompositeActor) actor;
                    // ca.getDirector() is guaranteed to return a local
                    // director, not the executive director.
                    if (ca.getDirector().needWriteAccess()) {
                        // If any of the directors need a write access, then
                        // everyone has to respect it.
                        return true;
                    }
                }
            }
            // Up to this point, all lower level directors have been queried
            // and none of them returned true (or else we would have returned)
            // Therefore, return false.
            return false;
        } else {
            throw new InvalidStateException("Director is not " +
                    "associated with a composite actor!");
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration.  This method is called by the container of
     *  this director to see if the director wishes to execute anymore, and
     *  should <i>not</i>, in general, just take the logical AND of calling
     *  postfire on all the contained actors.
     *  <p>
     *  In this base class, assume that the director only wants to get
     *  fired once, so return false. Domain directors will probably want
     *  to override this method.
     *
     *  @return false
     *  @exception IllegalActionException If the postfire()
     *  method of one of the associated actors throws it.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /** Return true if the director is ready to fire. This method is
     *  called but the container of this director to determine if the
     *  director is ready to execute, and
     *  should <i>not</i>, in general, just take the logical AND of calling
     *  prefire on all the contained actors.
     *  <p>
     *  In this base class, assume that the director is always ready to
     *  be fired, so return true. Domain directors should probably
     *  override this method.
     *
     *  @return true
     *  @exception IllegalActionException If the postfire()
     *  method of one of the associated actors throws it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Queue a change request with the manager.
     *  The indicated change will be executed at the next opportunity,
     *  typically between top-level iterations of the model.
     *  If there is no container, or if it has no manager, do nothing.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Manager manager = container.getManager();
            if (manager != null) {
                manager.requestChange(change);
            }
        }
    }

    /** Request that execution of the current iteration stop.
     *  In this base class, the request is simply passed on to all actors
     *  that are deeply contained by the container of this director.
     *  For most domains, an iteration is a finite computation, so nothing
     *  further needs to be done here.  However, for some process-oriented
     *  domains, the fire() method of the director is an unbounded computation.
     *  Those domains should override this method so that when it is called,
     *  it does whatever it needs to do to get the fire() method to return.
     *  Typically, it will set flags that will cause all executing threads
     *  to suspend.  These domains should suspend execution in such a way
     *  that if the fire() method is called again, execution will
     *  resume at the point where it was suspended.  However, they should
     *  not assume the fire() method will be called again.  It is possible
     *  that the wrapup() method will be called next.
     */
    public void stopFire() {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.stopFire();
            }
        }
    }

    /** Set the current time of the model under this director.
     *  Derived classes will likely override this method to ensure that
     *  the time is valid.
     *
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        if (newTime < getCurrentTime()) {
            throw new IllegalActionException(this,
            "Attempt to move current time backwards.");
        }
        _currentTime = newTime;
    }

    /** Terminate any currently executing model with extreme prejudice.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
     *  method instead. This method should be called only
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  There is no assurance that the topology will be in a consistent
     *  state after this method returns.  The
     *  topology should probably be recreated before attempting any
     *  further operations.
     *  <p>
     *  This base class recursively calls terminate() on all actors deeply
     *  contained by the container of this director. Derived classes should
     *  override this method to release all resources in use and kill
     *  any sub-threads.  Derived classes should not synchronize this
     *  method because it should execute as soon as possible.
     *  <p>
     */
    public void terminate() {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.terminate();
            }
        }
    }

    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     */
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The port argument must
     *  be an opaque output port.  If any channel of the output port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     */
    public void transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j = 0; j < insiderecs[i].length; j++) {
                        if (insiderecs[i][j].hasToken()) {
                            try {
                                Token t = insiderecs[i][j].get();
                                port.send(i, t);
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container.   In this base class wrapup() is called on the
     *  associated actors in the order of their creation.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            // FIXME: Add director-specific information here, like
            // what is the state of the director.
            // if ((detail & FIXME) != 0 ) {
            //  if (result.trim().length() > 0) {
            //      result += " ";
            //  }
            //  result += "FIXME {\n";
            //  result += _getIndentPrefix(indent) + "}";
            // }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Make this director the local director of the specified composite
     *  actor.  If the CompositeActor is not null, then remove the Actor
     *  from the workspace directory. If the CompositeActor is null, then
     *  the director is not added back into the directory of the Workspace,
     *  which could result in it being garbage collected.
     *  This method should not be called directly.  Instead, call
     *  setDirector of the CompositeActor class (or a derived class).
     */
    protected void _makeDirectorOf(CompositeActor cast) {

        _container = cast;
        if (cast != null) {
            workspace().remove(this);
        }
    }

    /** Return true if this director requires write access
     *  on the workspace during execution. Most director functions
     *  during execution do not need write access on the workpace.
     *  A director will generally only need write access on the workspace if
     *  it performs mutations locally, instead of queueing them with the
     *  manager.
     *  <p>
     *  In this base class, we assume
     *  that write access is required and always return true.  This method
     *  should probably be overridden by derived classes.
     *
     *  @return true
     */
    protected boolean _writeAccessRequired() {
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    // The current time of the model.
    protected double _currentTime = 0.0;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The composite of which this is the local director.
    private CompositeActor _container = null;
}
