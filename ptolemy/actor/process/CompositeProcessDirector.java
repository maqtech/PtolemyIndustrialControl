/* A base class for directors in process oriented domains that
incorporates hierarchical, heterogeneity.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow (davisj@eecs.berkeley.edu)
*/

package ptolemy.actor.process;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;
import ptolemy.kernel.CompositeEntity;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CompositeProcessDirector
/**
A base class for directors in process oriented domains that incorporates
hierarchical, heterogeneity. As with the ProcessDirector class,
CompositeProcessDirectors need to keep a count of the number of active
processes and the number of processes that are blocked for any reason
(e.g., trying to read from an empty channel in PN).
CompositeProcessDirector extends ProcessDirector by facilitating models
that consist of non-atomic actors.
<P>
A composite process director can be contained by an opaque composite
actor that is contained by a composite actor. Ports contained by opaque
composite actors are called opaque ports and such ports facilitate data
transfer across the composite actor boundaries. A composite process
director allocates two branch controllers to monitor data transfer in
channels associated with opaque ports. The <I>input</I> branch controller
monitors data transfer for channels associated with input opaque ports.
The <I>output</I> branch controller monitors data transfer for channels
associated with output opaque ports.
<P>
Associated with each opaque port's channels is a pair of process
receivers. The <I>producer receiver</I> serves as the channel source
and the <I>consumer receiver</I> serves as the channel destination.
Each branch controller allocates a branch for each process receiver
pair and when executing, a branch repeatedly attempts to transfer a
single token from its producer receiver to its consumer receiver.
<P>
When a branch blocks while attempting to transfer data, it informs its
branch controller by passing the branch controller the blocked receiver.
If all of a branch controller's branches have blocked, then we say that
the branch controller is blocked and the branch controller informs the
composite process director. In addition to monitoring the status of its
branch controllers, a composite process director keeps track of the
state of the actors that it contains. Actors can be internally or
externally blocked. We say that an actor is externally blocked if it
is blocked waiting to transfer tokens to or from a boundary port of
its container actor. Actors that are blocked but not externally are
said to be internally blocked.
<P>
Composite process directors monitor the state of the branch controllers
and contained actors and when necessary invoke the _resolveDeadlock()
method to deal with deadlocks. In the remainder of this paragraph we
consider the case of a process-oriented opaque composite actor that is
contained by another process-oriented opaque composite actor. If the
actors contained by the inner composite actor are not blocked, then
execution of the inner composite actor is allowed to continue
independent of the state of the branch controllers. If the actors
contained by the inner composite actor are internally blocked, then
after the branch controllers have been deactivated, execution of the
composite actor ends and postfire returns false indicating that
successive iterations are not allowed. If the actors contained by the
inner composite actor are externally blocked, then the composite
process director waits until the branch controllers block (an
inevitable condition) and registers the block with the containing
(outer) composite actor's director.
<P>
In this paragraph we consider the case of a process-oriented opaque
composite actor that is contained by a schedule-oriented (non process)
opaque composite actor. If the actors contained by the inner composite
actor are not blocked, then execution of the inner composite actor is
allowed to continue independent of the state of the branch controllers.
If the actors contained by the inner composite actor are internally
blocked, then after the branch controllers have been deactivated,
execution of the composite actor ends and postfire returns false
indicating that successive iterations are not allowed. If the actors
contained by the inner composite actor are externally blocked, then
the composite process director waits until the branch controllers
block (an inevitable condition) and ends the iteration with postfire()
returning true indicating that successive iterations are allowed.
<P>
<P>
@author John S. Davis II
@version $Id$
@see Director
*/
public class CompositeProcessDirector extends ProcessDirector {

    /** Construct a director in the default workspace with an empty
     *  string as its name. The director is added to the list of
     *  objects in the workspace. Increment the version number of
     *  the workspace.
     */
    public CompositeProcessDirector() {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public CompositeProcessDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown. If the name argument is null,
     *  then the name is set to the empty string. Increment the version
     *  number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name contains a period,
     *   or if the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CompositeProcessDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there).
     *  The result is a new director with no container, no pending mutations,
     *  and no topology listeners. The count of active processes is zero.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new ProcessDirector.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CompositeProcessDirector newObj =
	        (CompositeProcessDirector)super.clone(workspace);
	newObj._onFirstIteration = true;
	newObj._inputBranchController = null;
	newObj._outputBranchController = null;
	newObj._blockedReceivers = new LinkedList();
	newObj._branchControllerLock = new Object();
        return newObj;
    }

    /** Create a input and/or output branch controllers according to
     *  whether the ports passed in as arguments are input or output
     *  ports. If any of the ports are input (output) ports, then they
     *  will be added to the input (output) branch controller.
     *
     *  @param ports The ports for which branches will be assigned.
     *  @exception IllegalActionException If any of the ports are
     *   not opaque.
     */
    public void createBranchController(Iterator ports)
    	    throws IllegalActionException {

        IOPort port = null;
        while ( ports.hasNext() ) {
            port = (IOPort)ports.next();
            if (!port.isOpaque()) {
                    throw new IllegalActionException(this, port,
                    "port argument is not an opaque port.");
            }
	    if ( port.isInput() ) {
		_inputBranchController.addBranches(port);
	    }
	    if ( port.isOutput() ) {
		_outputBranchController.addBranches(port);
	    }
        }
    }

    /** Return the input branch controller of this director. If
     *  this method is called prior to the invocation of
     *  initialize(), then this method will return null.
     *
     *  @return The input branch controller of this director.
     */

    public BranchController getInputController() {
        return _inputBranchController;
    }

    /** Return the output branch controller of this director. If
     *  this method is called prior to the invocation of
     *  initialize(), then this method will return null.
     *
     *  @return The output branch controller of this director.
     */
    public BranchController getOutputController() {
        return _outputBranchController;
    }

    /** Invoke the initialize() methods of all the deeply contained
     *  actors in the container (a composite actor) of this director.
     *  These are expected to call initialize(Actor), which will
     *  result in the creation of a new thread for each actor.
     *  Also, set current time to 0.0, or to the current time of
     *  the executive director of the container, if there is one.
     *
     *  @exception IllegalActionException If the initialize() method
     *   of one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        CompositeActor container = ((CompositeActor)getContainer());
        if (container != null) {
            CompositeActor containersContainer =
                (CompositeActor)container.getContainer();
            if ( containersContainer == null ) {
                setCurrentTime(0.0);
            } else {
                double time =
                    containersContainer.getDirector().getCurrentTime();
                setCurrentTime(time);
            }
        }

        _blockedReceivers.clear();

        _inputBranchController = new BranchController(container);
        _outputBranchController = new BranchController(container);

	// Instantiate Input/Output Branch Controllers
	if ( container != null ) {
            Iterator inPorts = container.inputPortList().iterator();
            createBranchController(inPorts);
	    Iterator outports = container.outputPortList().iterator();
            createBranchController(outports);
	}

        _inputControllerIsBlocked = _inputBranchController.isBlocked();
        _outputControllerIsBlocked = _outputBranchController.isBlocked();

    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of
     *  MailboxBoundaryReceiver.
     *
     *  @return A new MailboxBoundaryReceiver.
     */
    public Receiver newReceiver() {
        return new MailboxBoundaryReceiver();
    }

    /** Return false if the model has reached deadlock and can
     *  be terminated if desired. Return true otherwise.
     *  This flag is set on detection of a deadlock in the fire() method.
     *
     *  @return false if the director has detected a deadlock and can be
     *   terminated if desired.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if ( _debugging ) {
	    _debug(_name+": returning _notDone = " + _notDone);
	}
	return _notDone;
    }

    /** Start threads for all actors that have not had threads started
     *  already (this might include actors initialized since the last
     *  invocation of prefire). This starts the threads, corresponding
     *  to all of the actors that were created in the initialize() method.
     *
     *  @return True.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        super.prefire();

        Thread thread = null;
        if ( _inputBranchController.hasBranches() && _onFirstIteration ) {
            thread = new Thread(_inputBranchController);
            thread.start();
        }
        if ( _outputBranchController.hasBranches() && _onFirstIteration ) {
            thread = new Thread(_outputBranchController);
            thread.start();
        }
        _onFirstIteration = false;
        return true;
    }

    /** Stop the input branch controller of this director. This
     *  method will block until the input branch controller
     *  has stopped due to all of the branches it controls
     *  stopping.
     */
    public void stopInputBranchController() {
        Workspace workspace = workspace();
        if ( _inputBranchController == null ) {
            // This happens under DDE Zeno under IE 5 with Java Plug-in 1.3
            return;
        }
        if ( !_inputBranchController.hasBranches() ) {
            return;
        }
        _inputBranchController.deactivateBranches();
        while ( !_inputBranchController.isBlocked() ) {
            workspace.wait(this);
        }
    }

    /** Stop the output branch controller of this director. This
     *  method will block until the output branch controller
     *  has stopped due to all of the branches it controls
     *  stopping.
     */
    public void stopOutputBranchController() {
        Workspace workspace = workspace();
        if ( _outputBranchController == null) {
            return;
        }
        if ( !_outputBranchController.hasBranches() ) {
            return;
        }
        _outputBranchController.deactivateBranches();
        while ( !_outputBranchController.isBlocked() ) {
            workspace.wait(this);
        }
    }

    /** End the execution of the model under the control of this
     *  director. A flag is set in all of the receivers that causes
     *  each process to terminate at the earliest communication point.
     *  <P>
     *  Prior to setting receiver flags, this method wakes up the
     *  threads if they all are stopped.
     *  <P>
     *  This method is not synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If an error occurs while
     *   accessing the receivers of all actors under the control of
     *   this director.
     */
    public void wrapup() throws IllegalActionException {
        if ( _debugging ) _debug(_name+": calling wrapup()");

        // Kill all branch controllers
        stopInputBranchController();
        stopOutputBranchController();

        if ( _debugging ) _debug(_name+": finished deactivating branches");

        super.wrapup();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Register the receiver that instigated the newly blocked actor.
     *  This method may be overridden in derived classes to add
     *  domain specific functionality. Implementations of this method
     *  must be synchronized.
     *
     *  @param receiver The receiver whose data transfer is blocked.
     */
    protected synchronized void _actorBlocked(ProcessReceiver receiver) {
        _blockedReceivers.add(receiver);
	super._actorBlocked(receiver);
    }

    /** Register the receivers that instigated the newly blocked actor.
     *  This method may be overridden in derived classes to add domain
     *  specific functionality. Implementations of this method must be
     *  synchronized.
     *
     *  @param receivers The receivers whose data transfer is blocked.
     */
    protected synchronized void _actorBlocked(LinkedList receivers) {
        if ( receivers == _blockedReceivers ) {
            return;
        }
        _blockedReceivers.addAll(receivers);
	super._actorBlocked(receivers);
    }

    /** Unregister the specified receiver that was previously blocked.
     *  This method may be overridden in derived classes to add
     *  domain specific functionality. Implementations of this method
     *  must be synchronized.
     *
     *  @param receiver The receiver whose data transfer was
     *   previously blocked.
     */
    protected synchronized void _actorUnBlocked(ProcessReceiver receiver) {
        _blockedReceivers.remove(receiver);
	super._actorUnBlocked(receiver);
    }

    /** Unregister the receivers that were previously blocked. This
     *  method may be overridden in derived classes to add domain
     *  specific functionality. Implementations of this method must
     *  be synchronized.
     *
     *  @param receivers The receivers whose data transfer was
     *   previously blocked.
     */
    protected synchronized void _actorUnBlocked(LinkedList receivers) {
        Iterator receiverIterator = receivers.iterator();
        while ( receiverIterator.hasNext() ) {
            ProcessReceiver receiver = (ProcessReceiver)receiverIterator.next();
            _blockedReceivers.remove(receiver);
        }
	super._actorUnBlocked(receivers);
    }

    /** Return false if the number of blocked processes is less than
     *  the number of active actors; return true otherwise. Note that
     *  if the number of active actors is 0 then this method will
     *  return true. Derived classes may override this method to add
     *  domain specific functionality. Implementations of this method
     *  must be synchronized.
     *
     *  @return false If the number of blocked processes is less than
     *   the number of active actors; return true otherwise.
     */
    protected synchronized boolean _areActorsDeadlocked() {
        if ( _getBlockedActorsCount() >= _getActiveActorsCount() ) {
            return true;
        }
	return false;
    }

    /** Return true if one or more contained actors are externally
     *  blocked; return false otherwise. We say an actor is
     *  externally blocked if it is blocked attempting data transfer
     *  through a boundary port of its containing actor. Note that
     *  a true return value for this method does not imply that the
     *  contained actors are deadlocked.
     *
     *  @return true If one or more contained actors are externally
     *   blocked; return false otherwise.
     */
    protected boolean _areActorsExternallyBlocked() {
    	Iterator blockedReceivers = _blockedReceivers.iterator();
        while ( blockedReceivers.hasNext() ) {
            ProcessReceiver receiver =
                    (ProcessReceiver)blockedReceivers.next();
            if ( receiver.isConnectedToBoundaryInside() ) {
                return true;
            }
        }
        return false;
    }

    /** Register that the specified controller is blocked. Pass the
     *  specified controller in as an argument. Note that if the
     *  controller passed in as an argument is not contained by this
     *  director or if the state of the controller is not blocked
     *  then no registration operation will be performed by this
     *  method.
     *
     *  @param controller The controller for which registration of a
     *   blocked state will occur.
     */
    protected synchronized void _controllerBlocked(
            BranchController controller) {
        if ( controller == _inputBranchController ) {
            _inputControllerIsBlocked = controller.isBlocked();
        }
        if ( controller == _outputBranchController ) {
            _outputControllerIsBlocked = controller.isBlocked();
        }
        notifyAll();
    }

    /** Unregister the specified controller as being no longer
     *  blocked. Pass the specified controller in as an argument.
     *  Note that if the controller passed in as an argument is
     *  not contained by this director or if the state of the
     *  controller is blocked then no registration operation will
     *  be performed by this method.
     *
     *  @param controller The controller for which registration of an
     *   unblocked state will occur.
     */
    protected void _controllerUnBlocked(BranchController controller) {
        synchronized(_branchControllerLock) {
            if ( controller == _inputBranchController ) {
                _inputControllerIsBlocked = controller.isBlocked();
            }
            if ( controller == _outputBranchController ) {
                _outputControllerIsBlocked = controller.isBlocked();
            }
        }
    }

    /** Return true if the input controller of this director is
     *  blocked; return false otherwise.
     *
     *  @return true If the input controller of this director is
     *   blocked; return false otherwise.
     */
    protected synchronized boolean _isInputControllerBlocked() {
        return _inputControllerIsBlocked;
    }

    /** Return true if the output controller of this director is
     *  blocked; return false otherwise.
     *
     *  @return true If the output controller of this director is
     *   blocked; return false otherwise.
     */
    protected synchronized boolean _isOutputControllerBlocked() {
        return _outputControllerIsBlocked;
    }

    /** Return true after registering all blocked receivers of this
     *  director's branch controllers with the executive director
     *  and then waiting on the executive director to handle the
     *  blocked receivers.
     *  @return True after registering blocked branch controller
     *   receivers with the executive director and waiting on the
     *   executive director to respond.
     */
    protected boolean _registerBlockedReceiversWithExecutive() {
	Workspace workspace = workspace();
	LinkedList blockedReceivers = new LinkedList();
	blockedReceivers.addAll(
                _outputBranchController.getBlockedReceivers() );
	blockedReceivers.addAll(
                _inputBranchController.getBlockedReceivers() );

	int originalCount = blockedReceivers.size();

    	Director execDir = ((Actor)getContainer()).getExecutiveDirector();
	CompositeActor execContainer =
                ((CompositeActor)getContainer().getContainer());

    	((CompositeProcessDirector)execDir)._actorBlocked(blockedReceivers);

	while ( blockedReceivers.size() >= originalCount ) {
	    workspace.wait(this);
	}

	return true;
    }

    /** Attempt to resolve a deadlock and return true if the deadlock
     *  no longer exists and successive iterations are allowed; if
     *  the deadlock still exists then return false indicating that
     *  future iterations are not allowed. If the deadlock is internal
     *  then apply a domain specific algorithm to attempt deadlock
     *  resolution via the _resolveInternalDeadlock() method. If the
     *  algorithm is successful and deadlock no longer exists then
     *  return true. If the algorithm is unsuccessful and deadlock
     *  persists then end the iteration and return false.
     *  <P>
     *  If the deadlock is an external deadlock and the containing model
     *  of computation is process-oriented, then register the externally
     *  blocked receivers with the composite actor that contains this
     *  director's composite actor. If the deadlock is an external
     *  deadlock and the containing model of computation is
     *  schedule-oriented, then end this iteration and return true.
     *  <P>
     *  While in special cases it my be useful to override this method
     *  for domain specific functionality it is more likely that this
     *  method will remain the same and the _resolveInternalDeadlock()
     *  method will be overridden for particular models of computation.
     *
     *  @return false If deadlock could not be resolved and successive
     *   iterations are not allowed; return true otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _resolveDeadlock() throws IllegalActionException {
	Director execDir = ((Actor)getContainer()).getExecutiveDirector();
	Workspace workspace = workspace();
	if ( _areActorsExternallyBlocked() && _areActorsDeadlocked() ) {
	    if ( _inputBranchController.isBlocked() ) {
                while ( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
		if ( execDir == null ) {
		    // This is the top level director - problem!!!
		    throw new IllegalActionException( this,
			    "No executive director exists yet this " +
			    "director's composite actor is externally " +
			    "deadlocked.");
		} else if ( execDir instanceof CompositeProcessDirector ) {
		    // This is contained by a process-oriented MoC
		    return _registerBlockedReceiversWithExecutive();
		} else {
		    // This is contained by a schedule-oriented MoC
		    return true;
		}
	    } else if ( _outputBranchController.isBlocked() ) {
		stopInputBranchController();
		stopOutputBranchController();
		if ( execDir == null ) {
		    // This is the top level director - problem!!!
		    throw new IllegalActionException( this,
			    "No executive director exists yet this " +
			    "director's composite actor is externally " +
			    "deadlocked.");
		} else if ( execDir instanceof CompositeProcessDirector ) {
		    // This is contained by a process-oriented MoC
		    return _registerBlockedReceiversWithExecutive();
		} else {
		    // This is contained by a schedule-oriented MoC
		    return true;
		}
	    }
	}

	if ( !_areActorsExternallyBlocked() && _areActorsDeadlocked() ) {
	    if ( _inputBranchController.isBlocked() ) {
                while ( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    } else if ( _outputBranchController.isBlocked() ) {
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    } else {
                while ( !_outputBranchController.isBlocked() ) {
                    workspace.wait(this);
                }
		stopInputBranchController();
		stopOutputBranchController();
		return _resolveInternalDeadlock();
	    }
	}

	return false;
    }

    /** Return false indicating that resolution of an internal
     *  deadlock was unsuccessful and execution should discontinue.
     *  Subclasses may override this method for domain specific
     *  functionality. Domain specific functionality should
     *  include algorithms to resolve internal deadlock.
     *  Successful application of the algorithm should result in
     *  a return value of true; unsuccessful application should
     *  result in a return value of false.
     *
     *  @return False indicating that internal deadlock was not
     *   resolved.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected boolean _resolveInternalDeadlock() throws
            IllegalActionException {
	return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _onFirstIteration = true;

    private BranchController _inputBranchController;
    private BranchController _outputBranchController;

    private boolean _inputControllerIsBlocked = true;
    private boolean _outputControllerIsBlocked = true;

    private LinkedList _blockedReceivers = new LinkedList();

    private Object _branchControllerLock = new Object();

    private String _name = null;
}
