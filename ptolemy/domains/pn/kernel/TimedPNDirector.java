/* Governs the execution of a CompositeActor with timed Kahn process
network semantics.

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
@AcceptedRating Green (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.TimedEvent;
import ptolemy.actor.util.TimedEvent.TimeComparator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TimedPNDirector
/**
A TimedPNDirector governs the execution of a CompositeActor with
Kahn-MacQueen process networks (PN) semantics extended by introduction of a
notion of global time.
<p>
The thread that calls the various execution methods (initialize, prefire, fire
and postfire) on the director is referred to as the <i>directing thread</i>.
This directing thread might be the main thread responsible for the execution
of the entire simulation or might be the thread created by the executive
director of the containing composite actor.
<p>
In the PN domain, the director creates a thread (an instance of
ProcessThread), representing a Kahn process, for each actor in the model.
The threads are created in initialize() and started in the prefire() method
of the ProcessDirector. A process is considered <i>active</i> from its
creation until its termination. An active process can block when trying to
read from a channel (read-blocked), when trying to write to a channel
(write-blocked), or when waiting for time to progress (time-blocked). Time
can progress for an active process in this model of computation only when the
process is  blocked.
<p>
A <i>deadlock</i> is when all the active processes are blocked.
The director is responsible for handling deadlocks during execution.
This director handles three different sorts of deadlocks, real deadlock, timed
deadlock and artificial deadlock.
<p>
A real deadlock is when all the processes are blocked on a read meaning that
no process can proceed until it receives new data. The execution can be
terminated, if desired, in such a situation. If the container of this director
does not have any input ports (as is in the case of a top-level composite
actor), then the executive director or manager terminates the execution.
If the container has input ports, then it is up to the
executive director of the container to decide on the termination of the
execution. To terminate the execution after detection of a real deadlock, the
manager or the executive director calls wrapup() on the director.
<p>
An artificial deadlock is when all processes are blocked and at least one
process is blocked on a write. In this case the director increases the
capacity of the receiver with the smallest capacity amongst all the
receivers on which a process is blocked on a write.
This breaks the deadlock and the execution can proceed.
<p>
A timed deadlock is when all the processes under the control of this
director are blocked, at least one process is blocked on a delay (time-blocked)
and no process is blocked on a write. This director supports a notion of global
time. All active processes that are not blocked and are executing concurrently
are executing at the same global time. A process that wants time to advance,
suspends itself by calling the fireAt() method of the director and specifies
the time it wants to be awakened at. Time can advance only when a timed
deadlock occurs. In such a case, the director advances time to the time when
the first timed-blocked process can be awakened.
<p>

@author Mudit Goel
@version $Id$
@since Ptolemy II 0.2
*/
public class TimedPNDirector extends BasePNDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the FIFO queues in all
     *  the receivers created in the PN domain.
     */
    public TimedPNDirector() {
        super();
    }

    /**Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param workspace The workspace of this object.
     */
    public TimedPNDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Create a director parameter "Initial_queue_capacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public TimedPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (It must be added
     *  by the user if he wants it to be there). The result is a new director
     *  with no container and no topology listeners. The count of active
     *  processes is zero. The parameter "Initial_queue_capacity" has the
     *  same value as the director being cloned.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new TimedPNDirector.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TimedPNDirector newObject = (TimedPNDirector)super.clone(workspace);
	newObject._eventQueue =
            new CalendarQueue(new TimedEvent.TimeComparator());
	newObject._delayBlockCount = 0;
        return newObject;
    }
    /** Suspend the calling thread until a deadlock
     *  is detected. On resuming, handle the various deadlocks appropriately.
     *  Break the deadlock if possible. If the deadlock is an artificial
     *  deadlock, then select the receiver with the smallest queue capacity on
     *  which any process is blocked on a write and increment the capacity of
     *  the contained queue.
     *  If the capacity is non-negative, then increment the capacity by 1.
     *  Otherwise set the capacity to 1. Unblock the process blocked on
     *  this receiver. Notify the thread corresponding to the blocked
     *  process.
     *  If the deadlock is a timed deadlock, process any pending topology
     *  change requests. If there are no pending requests, then advance the
     *  time to the earliest value when a time-blocked process can be
     *  awakened. If the deadlock detected is a real deadlock, then do
     *  nothing.
     *
     *  If processing the queued topology change requests, then inform the
     *  registered topology listeners of each change in a series of calls
     *  after successful completion of each request. If any queued
     *  request fails, the request is undone, and no further requests
     *  are processed. Note that change requests processed successfully
     *  prior to the failed request are <i>not</i> undone.
     *  Create receivers for any new actors created, initialize the new actors,
     *  and create new threads for these actors. After all threads
     *  are created, resume the execution and start the threads for the
     *  newly created actors. This method returns only on occurrence of a real
     *  deadlock.
     *
     *  <b>This method is synchronized on the director. This method is normally
     *  called by the directing thread. </b>
     *  @exception IllegalActionException If any of the called methods throw
     *  it.
     public void fire() throws IllegalActionException {
     boolean timedMutation;
     Workspace workspace = workspace();
     synchronized (this) {
     if ( _getActiveActorsCount() == 0 ) {
     _notDone = false;
     return;
     }
     // Loop until a real deadlock is detected.
     while ( _readBlockCount != _getActiveActorsCount() ) {
     while ( !_areActorsDeadlocked() ) {
     workspace.wait(this);
     }
     _notDone = _resolveDeadlock();
     }
     }
     return;
     }
    */

    /** Suspend the calling process until the time has advanced to at least the
     *  time specified by the method argument.
     *  Add the actor corresponding to the calling process to the priority
     *  queue and sort it by the time specified by the method argument.
     *  Increment the count of the actors blocked on a delay. Suspend the
     *  calling process until the time has advanced to at least the time
     *  specified by the method argument. Resume the execution of the calling
     *  process and return.
     *  @exception IllegalActionException If the operation is not
     *  permissible (e.g. the given time is in the past).
     */
    public synchronized void fireAt(Actor actor, double newFiringTime)
            throws IllegalActionException {
	if (newFiringTime < getCurrentTime()) {
	    throw new IllegalActionException(this, "The process wants to "
		    + " get fired in the past!");
	}
        _eventQueue.put(new TimedEvent(newFiringTime, actor));
        _informOfDelayBlock();
        try {
            while (getCurrentTime() < newFiringTime) {
                wait();
            }
        } catch (InterruptedException e) {
	    System.err.println(e.toString());
	}
    }


    /** Set the current time of the model under this director.
     *  @exception IllegalActionException If an attempt is made to change the
     *  time to less than the current time.
     *  @param newTime The new time of the model.
     */
    public void setCurrentTime(double newTime)
            throws IllegalActionException {
	if (newTime < getCurrentTime()) {
	    throw new IllegalActionException(this, "Attempt to set the "+
		    "time to past.");
	} else {
	    super.setCurrentTime(newTime);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if a deadlock is detected. Return false otherwise.
     *  Return true if all the active processes in the container are either
     *  read-blocked, write-blocked or delay-blocked.
     *  @return true if a deadlock is detected.
     */
    protected synchronized boolean _areActorsDeadlocked() {
	if (_readBlockCount + _writeBlockCount + _delayBlockCount
	        >= _getActiveActorsCount()) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Increment by 1 the count of actors waiting for the time to advance.
     *  Check for a resultant deadlock or pausing of the
     *  execution. If either of them is detected, then notify the directing
     *  thread of the same.
     */
    protected synchronized void _informOfDelayBlock() {
	_delayBlockCount++;
	notifyAll();
    }

    /** Decrease by 1 the count of processes blocked on a delay.
     */
    protected synchronized void _informOfDelayUnblock() {
	_delayBlockCount--;
	return;
    }

    /** Return false on detection of a real deadlock. Otherwise break the
     *  deadlock and return true.
     *  On detection of a timed deadlock, advance time to the earliest
     *  time that a delayed process is waiting for, wake up all the actors
     *  waiting for time to advance to the new time, and remove them from
     *  the priority queue. This method is synchronized on the director.
     *  @return true if a real deadlock is detected, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     *  This might be thrown by derived classes.
     */
    protected boolean _resolveDeadlock()
	    throws IllegalActionException {
	if (_writeBlockCount != 0) {
	    // Artificial deadlock based on write blocks.
            _incrementLowestWriteCapacityPort();
	    return true;
        } else if (_delayBlockCount == 0) {
	    // Real deadlock with no delayed processes.
	    return false;
	} else {
	    // Artificial deadlock due to delayed processes.
	    // Advance time to next possible time.
	    synchronized(this) {
		if (!_eventQueue.isEmpty()) {
		    //Take the first time-blocked process from the queue.
		    TimedEvent event = (TimedEvent)_eventQueue.take();
		    //Advance time to the resumption time of this process.
		    setCurrentTime(event.timeStamp);
		    _informOfDelayUnblock();
		} else {
		    throw new InternalErrorException("Inconsistency"+
			    " in number of actors blocked on delays count"+
			    " and the entries in the CalendarQueue");
		}

		//Remove any other process waiting to be resumed at the new
		//advanced time (the new currentTime).
		boolean sameTime = true;
		while (sameTime) {
		    //If queue is not empty, then determine the resumption
		    //time of the next process.
		    if (!_eventQueue.isEmpty()) {
                        //Remove the first process from the queue.
                        TimedEvent event = (TimedEvent)_eventQueue.take();
                        Actor actor = (Actor)event.contents;
                        //Get the resumption time of the newly removed
                        //process.
                        double newTime = event.timeStamp;
                        //If the resumption time of the newly removed
                        //process is the same as the newly advanced time
                        //then unblock it. Else put the newly removed
                        //process back on the event queue.
                        if (newTime == getCurrentTime()) {
                            _informOfDelayUnblock();
                        } else {
                            _eventQueue.put(new TimedEvent(newTime, actor));
                            sameTime = false;
                        }
		    } else {
			sameTime = false;
		    }
		}
		//Wake up all delayed actors
		notifyAll();
	    }
	}
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The priority queue that stores the list of processes waiting for time
     *  to advance. These processes are sorted by the time they want to resume
     *  at.
     */
    protected CalendarQueue _eventQueue =
	    new CalendarQueue(new TimedEvent.TimeComparator());

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The number of time-blocked processes. */
    private int _delayBlockCount = 0;
}
