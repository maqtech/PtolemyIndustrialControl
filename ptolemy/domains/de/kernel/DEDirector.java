/* A DE domain director.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** This director implements the discrete-event model of computation (MoC).
 *  It should be used as the local director of a CompositeActor that is
 *  to be executed according to this MoC. This director maintain a notion
 *  of current time, and processes events chronologically in this time.
 *  An <i>event</i> is a token with a time stamp.  Much of the sophistication
 *  in this director is aimed at handling simultaneous events intelligently,
 *  so that deterministic behavior can be achieved.
 *  <p>
 *  The bottleneck in a typical DE simulator is in the maintenance of the
 *  global event queue. By default, a DE director uses the calendar queue
 *  as the global event queue. This is an efficient algorithm
 *  with O(1) time complexity in both enqueue and dequeue operations.
 *  <p>
 *  Sorting in the CalendarQueue class is done with respect to sort-keys,
 *  which are implemented by the DEEventTag class. DEEventTag consists of a
 *  time stamp (double) and a depth (long). The time stamp
 *  indicates the time when the event occurs, and the depth
 *  indicates the relative priority of events with the same time stamp
 *  (simultaneous events).  The depth is determined by topologically
 *  sorting the ports according to data dependencies over which there
 *  is no time delay.
 *  <p>
 *  Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
 *  should be used whenever an actor introduces time delays between the
 *  inputs and the outputs. When an ordinary IOPort is used, the scheduler
 *  assumes, for the purpose of calculating priorities, that the delay
 *  across the actor is zero. On the other hand, when DEIOPort is used,
 *  the delay across the actor can be declared to be non-zero by calling
 *  the delayTo() method.
 *  <p>
 *  Input ports in a DE model contain instances of DEReceiver.
 *  When a token is put into a DEReceiver, that receiver enqueues the
 *  event by calling the _enqueueEvent() method of this director.
 *  This director sorts all such events in a global event queue
 *  (a priority queue) implemented as an instance of the CalendarQueue class.
 *  <p>
 *  Directed loops with no delay actors are not permitted; they would make it
 *  impossible to assign priorities.  Such a loop can be broken by inserting
 *  an instance of the Delay actor.  If zero delay around the loop is
 *  truly required, then simply set the <i>delay</i> parameter of that
 *  actor to zero.
 *  <p>
 *  At the beginning of the fire() method, this director dequeues
 *  a subset of the oldest events (the ones with smallest time
 *  stamp) from the global event queue, and puts those events into
 *  their corresponding receivers. The subset is chosen so that
 *  the events dequeued are all destined for the same actor, which
 *  becomes the one to be fired.
 *  If there are oldest events destined for multiple actors, then
 *  the choice of the actor to fire is determined by the topological depth
 *  of the input ports of the actors.  Specifically, the actor containing
 *  a port with the smallest topological depth will be fired first.
 *  <p>
 *  The actor that is fired must consume tokens from
 *  its input port(s), and will usually produce new events on its output
 *  port(s). These new events will be enqueued in the global event queue
 *  until their time stamps equal the current time.  It is important that
 *  the actor actually consume tokens from its inputs, because it will
 *  be fired repeatedly until there are no more tokens in its input
 *  ports with the current time stamp.  Alternatively, if the actor
 *  returns false in prefire(), then it will not be invoked again
 *  in the same iteration even if there are events in its receivers.
 *  <p>
 *  Execution of a DE model ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the prefire() method of this director. By default, execution also ends
 *  when the global event queue becomes empty. Sometimes, the desired
 *  behaviour is for the director to wait on an empty queue until another
 *  thread makes new events avalable.  For example, a DE actor may produce
 *  events when a user hits a button on the screen. To prevent ending the
 *  execution when there are no more events, call
 *  stopWhenQueueIsEmpty() with argument <code>false</code>.
 *  <p>
 *  This director tolerates changes to the model during execution.
 *  The change should be queued with the director or manager using
 *  requestChange().  While invoking those changes, the method
 *  invalidateSchedule() is expected to be called, notifying the director
 *  that the topology it used to calculate the priorities of the actors
 *  is no longer valid.  This will result in the priorities being
 *  recalculated the next time prefire() is invoked.
 *  <p>
 *  However, there is one subtlety.  If an actor produces events in the
 *  future via DEIOPort, then the desintation actor will be fired even
 *  if it has been removed from the topology by the time the execution
 *  reaches that future time.  This may not always be the expected behavior.
 *  The Delay actor in the DE library behaves this way.
 *
 *  @author Lukito Muliadi, Edward A. Lee
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 */
public class DEDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DEDirector() {
	this(null);
    }

    /**  Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public DEDirector(Workspace workspace) {
        super(workspace);
        setStopTime(Double.MAX_VALUE);
        // Create event queue.
        _eventQueue = new DECQEventQueue();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     */
    public DEDirector(CompositeActor container , String name)
            throws IllegalActionException {
        this(container, name, null);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name Name of this director.
     *  @param eventQueue The event queue to use with this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     */
    public DEDirector(CompositeActor container, String name,
            DEEventQueue eventQueue) throws IllegalActionException {
	super(container, name);
        setStopTime(Double.MAX_VALUE);
        // Assign the appropriate event queue.
        if (eventQueue == null) {
            _eventQueue = new DECQEventQueue();
        } else {
            _eventQueue = eventQueue;
            _eventQueue.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener) {
        _eventQueue.addDebugListener(listener);
        super.addDebugListener(listener);
    }

    /** Advance current time to the next event in the event queue,
     *  and fire one or more actors that have events at that time.
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens at the current time, or
     *  its prefire() method returns false. If there are no events in the
     *  event queue, then the behavior depends on whether
     *  stopWhenQueueIsEmpty() has been called.  If it has, and was given
     *  the argument false, then this thread will stall until inputs
     *  become available on the input queue.  Otherwise, time will advance
     *  to the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {

        boolean _timeHasNotAdvanced = true;
        while (true) {
            Actor actorToFire = _getActorToFire();
            if (actorToFire == null) {
                // There is nothing more to do.
                _debug("No more events on the event queue.");
                _noMoreActorsToFire = true;
                return;
            }
            // It is possible that the next event to be processed is on
            // an inside receiver of an output port of an opaque composite
            // actor containing this director.  In this case, we simply
            // return, giving the outside domain a chance to react to
            // event.
            if (actorToFire == getContainer()) {
                return;
            }
            // Repeatedly fire the actor until there are no more input
            // tokens available, or until prefire() return false.
            boolean refire = false;
            do {
                _debug("Iterating actor", ((Entity)actorToFire).getName(),
                        "at time " + getCurrentTime());
                if (!actorToFire.prefire()) {
                    _debug("Prefire returned false.");
                    break;
                }
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    _debug("Postfire returned false:",
                            ((Entity)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    if (_deadActors == null) {
                        _deadActors = new HashSet();
                    }
                    _deadActors.add(actorToFire);
                }
                // Check the input ports of the actor see whether there
                // is additional input data available.
                refire = false;
                Enumeration inputPorts = actorToFire.inputPorts();
                while (inputPorts.hasMoreElements()) {
                    IOPort port = (IOPort)inputPorts.nextElement();
                    for (int i = 0; i < port.getWidth(); i++) {
                        if (port.hasToken(i)) {
                            refire = true;
                            break;
                        }
                    }
                    if (refire == true) break;
                }
            } while (refire);

            // Check whether the next time stamp is equal to current time.
            DEEventTag nextKey = null;
            try {
                nextKey = _eventQueue.getNextTag();
            } catch (IllegalActionException e) {
                // The queue is empty. Proceed to postfire().
                break;
            }
            if (nextKey.timeStamp() > getCurrentTime()) {
                // if the next event is in the future then proceed
                // to postfire().
                break;
            } else if (nextKey.timeStamp() < getCurrentTime()) {
                throw new InternalErrorException(
                        "fire(): the next event has smaller time stamp than" +
                        " the current time!");
            }
        }
    }

    /** Schedule a firing of the given actor at the current time, but with
     *  lowest possible priority. This means that all other pending events
     *  at the current time (except those generated by other calls to this
     *  method) will be processed first.  This is used by actors that
     *  wish to schedule a refiring at the current time, but only after
     *  all other actors have been given a chance to provide events to
     *  their inputs.  In effect, this gives a way to bypass the normal
     *  mechanism.  
     *  <p>
     *  This method should be used with caution, as its semantics are a bit
     *  subtle. Note for example that when the actor actually fires, it may
     *  produce events with priorities higher than the event that triggered
     *  the firing.  It is implemented by queueing a pure event with a depth
     *  of Long.MAX_VALUE.
     *  @param actor The actor scheduled to be fired.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public void fireAfterIteration(Actor actor)
            throws IllegalActionException {
        _enqueueEvent(actor, getCurrentTime(), Long.MAX_VALUE);
    }

    /** Schedule an actor to be fired at the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time is in the past.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // NOTE: This does not check whether the actor is in the
        // composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This error would be fairly hard to make,
        // so we don't check for it here.

        // Set the depth equal to the depth of the actor.
        // NOTE: This used to set it to one more than the depth of the
        // actor, but with the new DEDirector, this results in double
        // firings where only one is expected.  Setting equal to the
        // depth of the actor ensures that events that are simultaneous
        // with the refiring will be seen.  The depth of the actor
        // can only be gotten from a receiver, so this is a bit baroque.
        // Since that depth is computed by this class, would it be worth
        // saving it?  It could go into a hashtable indexed by actor.
        Enumeration iports = actor.inputPorts();
        while (iports.hasMoreElements()) {
            IOPort p = (IOPort) iports.nextElement();
            Receiver[][] r = p.getReceivers();
            if (r != null && r.length != 0
                    && r[0] != null && r[0].length != 0) {
                DEReceiver rr = (DEReceiver) r[0][0];
                _enqueueEvent(actor, time, rr._getDepth());
                return;
            }
        }
        // No receivers found, so the depth is zero (highest priority).
        _enqueueEvent(actor, time, 0);
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.  If there is nothing on
     *  the event queue, then return the stop time.
     *  @return The next larger time on the event queue.
     */
    public double getNextIterationTime() {
        try {
            DEEventTag sortkey = _eventQueue.getNextTag();
            return sortkey.timeStamp();
        } catch (IllegalActionException e) {
            return getStopTime();
        }
    }

    /** Return the time of the earliest event seen in the model.
     *  Before execution begins, this is java.lang.Double.MAX_VALUE.
     *  @return The start time of the execution.
     */
    public double getStartTime() {
        return _startTime;
    }

    /** Return the stop time of the execution as set by setStopTime().
     *  @return The stop time of the execution.
     */
    public double getStopTime() {
        return _stopTime;
    }

    /** Invoke the initialize() method of each deeply contained actor,
     *  and then check the event queue for any events. If there are any,
     *  and the director is embedded in an opaque composite actor, then
     *  request a firing of the outside director.
     *  This method should be invoked once per execution, after the
     *  initialization phase, but before any iteration.  Since type
     *  resolution has been completed, the initialize() method of a contained
     *  actor may produce output or schedule events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Request a firing to the outer director if the queue is not empty.
        if (_isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }
    }

    /** Indicate that the topological depth of the ports in the model may
     *  no longer be valid. This method should be called when topology
     *  changes are made.  It sets a flag which will cause the topological
     *  sort to be redone next time prefire() is called.
     */
    public void invalidateSchedule() {
        _sortValid = false;
    }

    /** Return a new receiver of a type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
	return new DEReceiver();
    }

    /** Return false when the base class method return false, else request
     *  firing from outer domain (if embedded) then return true.
     *  @exception IllegalActionException If super.postfire() throws it.
     */
    public boolean postfire() throws IllegalActionException {

        if (_noMoreActorsToFire) {
            return false;
        } else if (_isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }
        return true;
    }

    /** If the topological sort is not valid, then compute it.
     *  @return True.
     *  @exception IllegalActionException If the graph has a zero
     *   delay loop.
     */
    public boolean prefire() throws IllegalActionException {
        if (!_sortValid) {
            _computeDepth();
            _sortValid = true;
        }
        return super.prefire();
    }

    /** Set current time to zero, invoke the preinitialize() methods of
     *  all actors deeply contained by the container, and calculate
     *  priorities for simultaneous events.
     *  To be able to calculate the priorities,
     *  it is essential that the graph not have a delay-free loop.  If it
     *  does, then this can be corrected by inserting a DEDelay actor
     *  with a zero-valued delay.  This has the effect of breaking the
     *  loop for the purposes of calculating priorities, without introducing
     *  a time delay.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors may produce output data in their preinitialize()
     *  methods, or more commonly, they may schedule pure events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the preinitialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {

	_eventQueue.clear();
        _deadActors = null;
        _currentTime = 0.0;
        _noMoreActorsToFire = false;
        _pendingEvents = new LinkedList();

        // Haven't seen any events yet, so...
        _startTime = Double.MAX_VALUE;

        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
        _computeDepth();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        _eventQueue.removeDebugListener(listener);
        super.removeDebugListener(listener);
    }

    /** Set the stop time for the execution.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double stopTime) {
        _stopTime = stopTime;
    }

    /** Specify whether the simulation should be stopped when there are no
     *  more events in the event queue. By default, an execution will stop
     *  in that case. Calling this method with a <i>false</i> argument
     *  causes the director to wait on the queue in the fire() method when
     *  it discovers that the queue is empty.  Another thread must insert
     *  an event into the queue to get the director going again.  This would
     *  be typically used when events are generated at a user interface.
     *  @param flag False to prevent the director from halting when there are
     *   no more events.
     */
    public void stopWhenQueueIsEmpty(boolean flag) {
        _stopWhenQueueIsEmpty = flag;
    }

    /** Advance current time to the current time of the executive director,
     *  and then call the superclass method.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @param port The input port from which tokens are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if the current time of the executive director
     *   is in the past.
     */
    public void transferInputs(IOPort port) throws IllegalActionException {
        Actor container = (Actor)getContainer();
        double outsideCurrTime =
            container.getExecutiveDirector().getCurrentTime();
        if (outsideCurrTime < getCurrentTime()) {
            throw new IllegalActionException(this,
                    "Received an event in the past at "
                    + "an opaque composite actor boundary.");
        }
        setCurrentTime(outsideCurrTime);
        super.transferInputs(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Put a "pure event" into the event queue with the specified time stamp
     *  and depth. The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transferred.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @param depth The depth.
     *  @exception IllegalActionException If the time is in the past.
     */
    protected void _enqueueEvent(Actor actor, double time, long depth)
            throws IllegalActionException {

        // Check for events in the past.
        if (_startTime != Double.MAX_VALUE && time < getCurrentTime()) {
            throw new IllegalActionException((Entity)actor,
                    "Attempt to schedule a firing in the past.");
        }
        DEEventTag key = new DEEventTag(time, depth);
        DEEvent event = new DEEvent(actor, key);
        _eventQueue.put(event);
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, transferred token, time stamp and depth. The depth
     *  is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time, long depth) throws IllegalActionException {

        Nameable destination = receiver.getContainer();
        if (time < getCurrentTime()) {
            throw new IllegalActionException(destination,
                    "Attempt to send a token with a time stamp in the past.");
        }
        DEEventTag key = new DEEventTag(time, depth);
        DEEvent event = new DEEvent(receiver, token, key);
        _eventQueue.put(event);
    }

    /** Override the default Director implementation, because in DE
     *  domain, we don't need write access inside an iteration.
     *  @return false.
     */
    protected boolean _writeAccessRequired() {
        // Return false to let the workspace be write-protected.
        // Return true to debug the PtolemyThread.
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Dequeue the next event from the event queue, advance time to its
    // time stamp, and mark the destination actor for firing.
    // If there are multiple events on the queue with the same time
    // stamp that are destined for the same actor, dequeue all of them,
    // making them available in the input ports of the destination actor.
    // The firing actor will be repeated until all of these tokens are
    // consumed.
    // If the time stamp is greater than the stop time then return null.
    // If there are no events on the event queue, and _stopWhenQueueIsEmpty
    // flag is true (which is set to true by default) then return null,
    // which will have the effect of stopping the simulation.
    // If _stopWhenQueueIsEmpty is false and the queue is empty, then
    // stall the current thread by calling wait() on the _eventQueue
    // until there are events available.
    //
    private Actor _getActorToFire() {
        Actor actorToFire = null;
        DEEvent currentEvent = null;
        double currentTime = getCurrentTime();
        long currentDepth = 0;

        // Get any events that have previously been dequeued and cached
        // (their tag was the same as the previous firing).
        while (_pendingEvents.size() > 0) {
            currentEvent = (DEEvent)_pendingEvents.remove(0);
            actorToFire = currentEvent.getDestinationActor();
            if (_deadActors != null && _deadActors.contains(actorToFire)) {
                _debug("Skipping actor: ", ((Entity)actorToFire).getFullName());
                actorToFire = null;
                continue;
            }
            currentTime = currentEvent.getEventTag().timeStamp();
            currentDepth = currentEvent.getEventTag().receiverDepth();
            // NOTE: No need to set current time, because it is the
            // same as during the previous firing.
            break;
        }

        // Keep taking events out until there are no more simultaneous
        // events or until the queue is empty. One event may get put back
        // into the queue (the first one with a future tag).  Others
        // may be put on the pending list (if their tag is identical to
        // that of the currentEvent).
        while (true) {
            // Get the next event off the event queue.
            if (_stopWhenQueueIsEmpty) {
                try {
                    currentEvent = (DEEvent)_eventQueue.take();
                } catch (IllegalActionException ex) {
                    // Nothing more to read from queue.
                    break;
                }
            } else {
                // In this case, effectively, we want to do a blocking
                // take(). So, keep invoking take() until an exception
                // is not thrown.
                while (_eventQueue.isEmpty()) {
                    // Queue is empty.
                    _debug("Queue is empty. Waiting for input events.");
                    synchronized(_eventQueue) {
                        try {
                            _eventQueue.wait();
                        } catch (InterruptedException e) {
                            // ignore... Keep waiting
                        }
                    }
                }
                try {
                    currentEvent = (DEEvent)_eventQueue.take();
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.toString());
                }
            }

            if (actorToFire == null) {
                // No previously seen event at this time stamp, so
                // always accept the event.
                actorToFire = currentEvent.getDestinationActor();

                if (_deadActors != null && _deadActors.contains(actorToFire)) {
                    // This actor has requested that it not be fired again.
                    _debug("Skipping actor: ",
                           ((Entity)actorToFire).getFullName());
                    actorToFire = null;
                    continue;
                }

                currentTime = currentEvent.getEventTag().timeStamp();
                currentDepth = currentEvent.getEventTag().receiverDepth();
                // Advance current time.
                try {
                    setCurrentTime(currentTime);
                } catch (IllegalActionException ex) {
                    // Thrown if time moves backwards.
                    throw new InternalErrorException(ex.toString());
                }
                _debug("******* Setting current time to: " + currentTime);

                // Note: The following comparison is true
                // only during the first iteration, before the start time
                // is initialized to the smallest time stamp in the
                // event queue.
                if (currentTime < _startTime) {
                    _startTime = currentTime;
                }

                if (currentTime > getStopTime()) {
                    _debug("Current time has passed the stop time.");
                    return null;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver rec = currentEvent.getDestinationReceiver();
                // If rec is null, then it's a 'pure event', and there's
                // no need to put event into receiver.
                if (rec != null) {
                    // Transfer the event to the receiver.
                    rec._triggerEvent(currentEvent.getTransferredToken());
                }
            } else {
                // Already seen an event at the current time;
                // check whether the new event has tag equal to the previous
                // one. Then check whether it's for the same actor.
                double eventTime = currentEvent.getEventTag().timeStamp();
                long eventDepth = currentEvent.getEventTag().receiverDepth();
                if (eventTime < currentTime) {
                    throw new InternalErrorException("Event that was "+
                            "dequeued later has smaller time stamp!");
                }
                // Check whether the event occurred at current time.
                if (eventTime > currentTime || eventDepth > currentDepth) {
                    // The event has a later time stamp or depth,
                    // so we put it back
                    _eventQueue.put(currentEvent);
                    break;
                } else {
                    // The event has the same time stamp and depth as the first
                    // event seen.

                    // Transfer the event into the receiver.
                    DEReceiver rec = currentEvent.getDestinationReceiver();
                    // If rec is null, then it's a 'pure event' and
                    // there's no need to put event into receiver.
                    if (rec != null) {
                        // Transfer the event to the receiver.
                        rec._triggerEvent(
                                currentEvent.getTransferredToken());
                    }
                    // If the event is not for the same actor, add it to
                    // the pending list.
                    if (currentEvent.getDestinationActor() != actorToFire) {
                        _pendingEvents.add(currentEvent);
                    }
                }
            }
        }
        return actorToFire;
    }

    // Construct a directed graph with the nodes representing actors and
    // directed edges representing dependencies.  The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        CompositeActor container = ((CompositeActor)getContainer());

        // If there is no container, there are no actors.
        if (container == null) return dag;

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        Enumeration actors = container.deepGetEntities();
        while (actors.hasMoreElements()) {
            dag.add(actors.nextElement());
        }

        // Next, create the directed edges by iterating again.
        actors = container.deepGetEntities();
        while (actors.hasMoreElements()) {
            Actor actor = (Actor)actors.nextElement();
            // get all the input ports in that actor
            Enumeration ports = actor.inputPorts();
            while (ports.hasMoreElements()) {
                IOPort inputPort = (IOPort)ports.nextElement();
                    
                Set delayPorts = null;
                if (inputPort instanceof DEIOPort) {
                    DEIOPort dePort = (DEIOPort) inputPort;
                    delayPorts = dePort.getDelayToPorts();
                }

                // Find the successor of the port.
                Enumeration triggers
                        = ((Actor)inputPort.getContainer()).outputPorts();
                while (triggers.hasMoreElements()) {
                    IOPort outPort = (IOPort) triggers.nextElement();

                    if (delayPorts != null && delayPorts.contains(outPort)) {
                        // Skip this port since there is a declared delay.
                        continue;
                    }
                    // find the input ports connected to outPort
                    Enumeration inPortEnum = outPort.deepConnectedInPorts();
                    while (inPortEnum.hasMoreElements()) {
                        IOPort pp = (IOPort)inPortEnum.nextElement();
                        Actor destination = (Actor)(pp.getContainer());
                        if(destination.equals(actor)) {
                            throw new IllegalActionException(this,
                            "Zero delay self-loop on actor: "
                            + ((Nameable)actor).getFullName());
                        }
                        // create an arc from this actor to the successor.
                        if (dag.contains(destination)) {
                            dag.addEdge(actor, destination);
                        } else {
                            // This happens if there is a
                            // level-crossing transition.
                            throw new IllegalActionException(this,
                            "Level-crossing transition from "
                            + ((Nameable)actor).getFullName() + " to "
                            + ((Nameable)destination).getFullName());
                        }
                    }
                }
            }
        }
        // NOTE: The following may be a very costly test, which is why
        // it it done at the end.  However, this means that we cannot
        // report an actor in the directed cycle.  Probably DirectedGraph
        // should be modified to enable such reporting.
        if (!dag.isAcyclic()) {
            throw new IllegalActionException(this,
            "Zero delay loop found.  Cannot prioritize actors.");
        }
        return dag;
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth field of the DEReceiver objects.
    private void _computeDepth() throws IllegalActionException {
        DirectedAcyclicGraph dag = _constructDirectedGraph();
        Object[] sort = (Object[]) dag.topologicalSort();
        _debug("####### Result of topological sort (highest to lowest):");
	for(int i = sort.length-1; i >= 0; i--) {
            Actor actor = (Actor)sort[i];
            _debug(((Nameable)actor).getFullName());
            // Set the fine levels of all DEReceiver instances in input
            // ports of the actor to i.
            Enumeration ports = actor.inputPorts();
            while (ports.hasMoreElements()) {
                IOPort inputPort = (IOPort)ports.nextElement();
                Receiver[][] r;
                r = inputPort.getReceivers();
                if (r == null) {
                    // dangling input port..
                    continue;
                }
                for (int j = r.length-1; j >= 0; j--) {
                    for (int k = r[j].length-1; k >= 0; k--) {
                        DEReceiver der = (DEReceiver)r[j][k];
                        der._setDepth(i);
                    }
                }
            }
	}
        _debug("####### End of topological sort.");
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy 0.x terminology).
    private void _requestFiring() throws IllegalActionException {
        DEEventTag sortkey = null;
        try {
            sortkey = _eventQueue.getNextTag();
        } catch (IllegalActionException e) {
            throw new IllegalActionException(
                    "Request to refire composite actor, "
                    + "but the event queue is empty.");
        }
        double nextRefire = sortkey.timeStamp();

        // Enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt(
                (Actor)getContainer(), nextRefire);

        _debug("DEDirector requests refiring at " + nextRefire);
    }

    // Return true if this director is embedded inside an opaque composite
    // actor contained by another composite actor.
    private boolean _isEmbedded() {
        return (getContainer().getContainer() != null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The queue used for sorting events.
    private DEEventQueue _eventQueue;

    // List of dequeued events waiting to be processed.
    private LinkedList _pendingEvents;

    // Set to true when it's time to end the execution.
    private boolean _noMoreActorsToFire = false;

    // The time of the earliest event seen in the current simulation.
    private double _startTime = Double.MAX_VALUE;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    private boolean _stopWhenQueueIsEmpty = true;

    // The stop time.
    private double _stopTime;

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private Set _deadActors;

    // Indicator of whether the topological sort giving ports their
    // priorities is valid.
    private boolean _sortValid = false;
}
