/* A DE domain director.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.DirectedAcyclicGraph;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.LinkedList;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// DEDirector

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
 *  Sorting in the CalendarQueue class is done according to the order
 *  defined by the DEEvent class, which implements the java.lang.Comparable
 *  interface. A DE event has a time stamp, a microstep, and a depth.
 *  The time stamp indicates the time when the event occurs.
 *  The microstep represents the phase of execution
 *  when processing simultaneous events in directed loops, or when an
 *  actor schedules itself for firing later at the current time
 *  (using fireAt()).
 *  The depth is the index of the destination actor in a topological
 *  sort.  A larger value of depth represents a lower priority when
 *  processing events.  The depth is determined by topologically
 *  sorting the actors according to data dependencies over which there
 *  is no time delay. Note that the zero-delay data dependencies are
 *  determined on a per port basis.
 *  <p>
 *  Ports in the DE domain may be instances of DEIOPort. The DEIOPort class
 *  should be used whenever an actor introduces time delays between the
 *  inputs and the outputs. When an ordinary IOPort is used, the director
 *  assumes, for the purpose of calculating priorities, that the delay
 *  across the actor is zero. On the other hand, when DEIOPort is used,
 *  the delay across the actor can be declared to be non-zero by calling
 *  the delayTo() method on output ports.
 *  <p>
 *  Directed loops with no delay actors are not permitted; they would make it
 *  impossible to assign priorities.  Such a loop can be broken by inserting
 *  an instance of the Delay actor.  If zero delay around the loop is
 *  truly required, then simply set the <i>delay</i> parameter of that
 *  actor to zero.
 *  <p>
 *  Input ports in a DE model contain instances of DEReceiver.
 *  When a token is put into a DEReceiver, that receiver enqueues the
 *  event to the director  by calling the _enqueueEvent() method of
 *  this director.
 *  This director sorts all such events in a global event queue
 *  (a priority queue).
 *  <p>
 *  An iteration, in the DE domain, is defined as processing all
 *  the events whose time stamp equals to the current time of the director.
 *  At the beginning of the fire() method, this director dequeues
 *  a subset of the oldest events (the ones with smallest time
 *  stamp, microstep, and depth) from the global event queue,
 *  and puts those events into
 *  their destination receivers. The actor(s) to which these
 *  events are destined are the ones to be fired.  The depth of
 *  an event is the depth of the actor to which it is destined.
 *  The depth of an actor is its position in a topological sort of the graph.
 *  The microstep is usually zero, but is incremented when a pure event
 *  is queued with time stamp equal to the current time.
 *  <p>
 *  The actor that is fired must consume tokens from
 *  its input port(s), and will usually produce new events on its output
 *  port(s). These new events will be enqueued in the global event queue
 *  until their time stamps equal the current time.  It is important that
 *  the actor actually consume tokens from its inputs, even if the tokens are
 *  solely used to trigger reactions. This is how polymorphic actors are
 *  used in the DE domain. The actor will
 *  be fired repeatedly until there are no more tokens in its input
 *  ports with the current time stamp.  Alternatively, if the actor
 *  returns false in prefire(), then it will not be invoked again
 *  in the same iteration even if there are events in its receivers.
 *  <p>
 *  A model starts from the time specified by <i>startTime</i>, which
 *  has default value 0.0
 *  <P>
 *  The stop time of the execution can be set using the
 *  <i>stopTime</i> parameter. The parameter has default value
 *  Double.MAX_VALUE, which means the execution stops
 *  only when the model time reaches that (rather large) number.
 *  <P>
 *  Execution of a DE model ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the prefire() method of this director. By default, execution also ends
 *  when the global event queue becomes empty. Sometimes, the desired
 *  behaviour is for the director to wait on an empty queue until another
 *  thread makes new events available.  For example, a DE actor may produce
 *  events when a user hits a button on the screen. To prevent ending the
 *  execution when there are no more events, set the
 *  <i>stopWhenQueueIsEmpty</i> parameter to <code>false</code>.
 *  <p>
 *  Parameters, <i>isCQAdaptive</i>, <i>minBinCount</i>, and
 *  <i>binCountFactor</i>, are
 *  used to configure the calendar queue. Changes to these parameters
 *  are ignored when the model is running.
 *  <p>
 *  If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
 *  then the director not process events until the real time elapsed
 *  since the model started matches the time stamp of the event.
 *  This ensures that the director does not get ahead of real time,
 *  but, of course, it does not ensure that the director keeps up with
 *  real time.
 *  <p>
 *  This director tolerates changes to the model during execution.
 *  The change should be queued with a component in the hierarchy using
 *  requestChange().  While invoking those changes, the method
 *  invalidateSchedule() is expected to be called, notifying the director
 *  that the topology it used to calculate the priorities of the actors
 *  is no longer valid.  This will result in the priorities being
 *  recalculated the next time prefire() is invoked.
 *  <p>
 *  However, there is one subtlety.  If an actor produces events in the
 *  future via DEIOPort, then the destination actor will be fired even
 *  if it has been removed from the topology by the time the execution
 *  reaches that future time.  This may not always be the expected behavior.
 *  The Delay actor in the DE library behaves this way.
 *
 *  @author Lukito Muliadi, Edward A. Lee, Jie Liu
 *  @version $Id$
 *  @see DEReceiver
 *  @see ptolemy.actor.util.CalendarQueue
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
        _initParameters();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The start time of model. This parameter must contain a
     *  DoubleToken.  The value defaults to 0.0.
     */
    public Parameter startTime;

    /** The stop time of the model.  This parameter must contain a
     *  DoubleToken.  The value defaults to Double.MAX_VALUE.
     */
    public Parameter stopTime;

    /** Specify whether the execution stops when the queue is empty.
     *  This parameter must contain a
     *  BooleanToken. If this parameter is true, the
     *  execution of the model will be stopped when the queue is empty.
     *  The value defaults to true.
     */
    public Parameter stopWhenQueueIsEmpty;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    /** Specify whether the calendar queue adjusts its bin number
     *  at run time. This parameter must contain a BooleanToken.
     *  If this parameter is true, the calendar queue will adapt
     *  its bin number with respect to the distribution of events.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to true.
     */
    public Parameter isCQAdaptive;

    /** The minimum (initial) number of bins in the calendar queue.
     *  This parameter must contain an IntToken.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to 2.
     */
    public Parameter minBinCount;

    /** The factor when adjusting the bin number.
     *  This parameter must contain an IntToken.
     *  Changes to this parameter are ignored when the model is running.
     *  The value defaults to 2.
     */
    public Parameter binCountFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the specified listener to the current set of debug listeners.
     *  If a calendar queue has been created,
     *  register the listener to the calendar queue, too.
     *  If the listener is already in the set,
     *  @param listener The listener to add to the list of listeners
     *   to which debug messages are sent.
     */
    public void addDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.addDebugListener(listener);
        }
        super.addDebugListener(listener);
    }

    /** Update the director parameters when the attributes are changed.
     *  Changes to <i>isCQAdaptive</i>, <i>minBinCount</i>, and
     *  <i>binCountFactor</i> parameters will only be effective on
     *  the next time the model is executed.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) _debug("Updating DEDirector parameter",
                attribute.getName());
        if (attribute == stopWhenQueueIsEmpty) {
            _stopWhenQueueIsEmpty =
                ((BooleanToken)stopWhenQueueIsEmpty.getToken()).booleanValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime =
                ((BooleanToken)synchronizeToRealTime.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Advance current time to the next event in the event queue,
     *  and fire one or more actors that have events at that time.
     *  If <i>synchronizeToRealTime</i> is true, then before firing,
     *  wait until real time matches or exceeds the time stamp of the event.
     *  Each actor is iterated repeatedly (prefire(), fire(), postfire()),
     *  until either it has no more input tokens at the current time, or
     *  its prefire() method returns false. If there are no events in the
     *  event queue, then the behavior depends on the
     *  <i>stopWhenQueueIsEmpty</i> parameter.  If it is false,
     *  then this thread will stall until events
     *  become available on the event queue.  Otherwise, time will advance
     *  to the stop time and the execution will halt.
     *
     *  @exception IllegalActionException If the firing actor throws it.
     */
    public void fire() throws IllegalActionException {
        while (true) {
            _stopRequested = false;

            Actor actorToFire = _dequeueEvents();
            if (actorToFire == null) {
                // There is nothing more to do.
                if (_debugging) _debug("No more events on the event queue.");
                _noMoreActorsToFire = true;
                return;
            }
            if (_debugging) {
                _debug("Found actor to fire: "
                        + ((NamedObj)actorToFire).getFullName());
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
            boolean refire;
            do {
                refire = false;
                if (_debugging) {
                    _debug("Iterating actor",
                            ((Nameable)actorToFire).getName(),
                            "at time ", Double.toString(getCurrentTime()));
                }
                if (((Nameable)actorToFire).getContainer() == null) {
                    if (_debugging) _debug(
                            "Actor has no container. Disabling actor.");
                    _disableActor(actorToFire);
                    break;
                }
                if (!actorToFire.prefire()) {
                    if (_debugging) _debug("Prefire returned false.");
                    break;
                }
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    if (_debugging) _debug("Postfire returned false:",
                            ((Nameable)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                    //break;
                }
                // Check the input ports of the actor see whether there
                // is additional input data available.

                Iterator inputPorts = actorToFire.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort port = (IOPort)inputPorts.next();
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
            if(!_eventQueue.isEmpty()) {
                DEEvent next = _eventQueue.get();
                // If the next event is in the future, proceed to postfire().
                if (next.timeStamp() > getCurrentTime()) {
                    break;
                } else if (next.timeStamp() < getCurrentTime()) {
                    throw new InternalErrorException(
                            "fire(): the next event has smaller time stamp" +
                            " than the current time!");
                }
            } else {
                // The queue is empty, proceed to postfire().
                break;
            }
        }
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
        _enqueueEvent(actor, time);
    }

    /** Return the event queue.
     *  @return The event queue.
     */
    public DEEventQueue getEventQueue() {
        return _eventQueue;
    }

    /** Return the time stamp of the next event in the queue with time stamp
     *  strictly greater than the current time.  If there is nothing on
     *  the event queue, then return the stop time. The next iteration time,
     *  for example, is used to estimate the run-ahead time, when a continuous
     *  time composite actor is embedded in the DE domain.
     *  @return The next larger time on the event queue.
     */
    public double getNextIterationTime() {
        // It seems like the current design of the DECQEventQueue, and
        // the use the calendar queue in general, cannot support this
        // method very efficiently. To get the next iteration time, the
        // director has to dequeue all events at the current time, find
        // the first event in the next iteration,
        // and put all event just been dequeued back to queue.
        // <P>
        // An alternative design is to have a hierarchical queue. The
        // top level of the queue sorts entries (a groups of DEEvents)
        // only by their time stamp.
        // Each entry at this level is another priority queue, which
        // sort (simultaneous) events further according to their microsteps
        // and depth. Then the next iteration time is simply the time stamp
        // of the next entry in the top queue.
        // However, since this method is called only when CT is side DE.
        // It is unclear how the change may effect normal DE execution.
        // So we will keep the current design unless there's prove that
        // the new design can improve (or at least does not ruin) the
        // performance under regular uses.
        if (_eventQueue.isEmpty()) {
            return getStopTime();
        } else {
            DEEvent next = _eventQueue.get();
            double nextTime = next.timeStamp();
            // The next event on the queue may have the current time.
            // May need to look deeper in the queue.
            // Save items to reinsert into queue.
            LinkedList eventsToPutBack = new LinkedList();
            while (nextTime <= getCurrentTime()) {
                if(_debugging) _debug("Temporarily remove event.");
                // take() is safe, since the queue is not empty.
                eventsToPutBack.add(_eventQueue.take());
                if (!_eventQueue.isEmpty()) {
                    next = _eventQueue.get();
                    nextTime = next.timeStamp();
                } else {
                    nextTime = getStopTime();
                    break;
                }
            }
            // Put back events that need to be put back.
            Iterator events = eventsToPutBack.iterator();
            while (events.hasNext()) {
                if(_debugging) _debug("Put dequeued current event back.");
                try {
                    _eventQueue.put((DEEvent)events.next());
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.getMessage());
                }
            }
            return nextTime;
        }
    }

    /** Return the system time at which the model begins executing.
     *  That is, the system time (in milliseconds) when the initialize()
     *  method of the director is called.
     *  The time is in the form of milliseconds counting
     *  from 1/1/1970 (UTC).
     *  @return The real start time of the model.
     */
    public long getRealStartTimeMillis() {
        return _realStartTime;
    }

    /** Return the time of the start time in the model as set by the
     *  <i>startTime</i> parameter.
     *  @return The start time of the execution.
     */
    public double getStartTime() {
        try {
            return ((DoubleToken)(startTime.getToken())).doubleValue();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "Cannot read startTime parameter:\n" +
                    e.getMessage());
        }
    }

    /** Return the stop time of the execution as set by the <i>stopTime</i>
     *  parameter.
     *  @return The stop time of the execution.
     */
    public double getStopTime() {
        try {
            return ((DoubleToken)(stopTime.getToken())).doubleValue();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "Cannot read stopTime parameter:\n" +
                    e.getMessage());
        }
    }

    /** Invoke the initialize() method of each deeply contained actor,
     *  and then check the event queue for any events. If there are any,
     *  and the director is embedded in an opaque composite actor, then
     *  request a firing of the outside director.
     *  This method should be invoked once per execution, after the
     *  initialization phase, but before any iteration.  Since type
     *  resolution has been completed, the initialize() method of a contained
     *  actor may produce output or schedule events.
     *  The real start time of the model is recorded when this method
     *  is called.
     *
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        if (!_isEmbedded() && getStartTime() > getStopTime()) {
            throw new IllegalActionException(this,
                    " startTime (" + getStartTime() 
                    + ") must be less than the stopTime ("
                    + getStopTime() + ").");
        }
        _exceedStopTime = false;
        // use the protected variable directly, since time can go backward.
        // This is the only place in DE where time can go backward.
        _currentTime = getStartTime();
        _realStartTime = System.currentTimeMillis();
        // We cannot call super.initialize() since it will set current time
        // back to 0.0
        Iterator actors = ((CompositeActor)getContainer())
            .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_debugging) _debug("Invoking initialize(): ",
                    ((NamedObj)actor).getFullName());
            actor.initialize();
        }
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
        if(_debugging) _debug("Creating new DE receiver.");
	return new DEReceiver();
    }

    /** Return false if there are no more actors to fire. Otherwise, if
     *  the director is an embedded director and the queue is not empty,
     *  then request that the executive director refire the container of
     *  this director at the time of the next event in the event queue
     *  of this director.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        boolean stop = ((BooleanToken)stopWhenQueueIsEmpty.getToken())
               .booleanValue();
        if (_noMoreActorsToFire && (stop || _exceedStopTime)) {
            return false;
        } else if (_isEmbedded() && !_eventQueue.isEmpty()) {
            _requestFiring();
        }
        return true;
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
     *  iteration. Actors cannot produce output data in their preinitialize()
     *  methods. If initial events are needed, e.g. pure events for source
     *  actor, the actors should do so in their initialize() methods.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the preinitialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _eventQueue = new DECQEventQueue(
                ((IntToken)minBinCount.getToken()).intValue(),
                ((IntToken)binCountFactor.getToken()).intValue(),
                ((BooleanToken)isCQAdaptive.getToken()).booleanValue());

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener)listeners.next();
                _eventQueue.addDebugListener(listener);
            }
        }
        _disabledActors = null;
        _noMoreActorsToFire = false;
        _microstep = 0;

        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        if (_eventQueue != null) {
            _eventQueue.removeDebugListener(listener);
        }
        super.removeDebugListener(listener);
    }

    /** Request that execution of the current iteration stop.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stopFire() {
        if (_eventQueue != null) {
            synchronized(_eventQueue) {
                _stopRequested = true;
                _eventQueue.notifyAll();
            }
        }
        super.stopFire();
    }

    /** Advance current time to the current time of the executive director,
     *  and then call the superclass method.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.  If the container
     *  does not implement the Actor interface, then this method does
     *  nothing.
     *
     *  @return True if data are transferred.
     *  @param port The input port from which tokens are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if the current time of the executive director
     *   is in the past.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            double outsideCurrentTime = ((Actor)container)
                .getExecutiveDirector().getCurrentTime();
            if (outsideCurrentTime < getCurrentTime()) {
                throw new IllegalActionException(this,
                        "Received an event in the past at "
                        + "an opaque composite actor boundary: "
                        + "Outside time is " + outsideCurrentTime
                        + ". Local current time is " + getCurrentTime() + ".");
            }
            setCurrentTime(outsideCurrentTime);
            return super.transferInputs(port);
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Dequeue the events from the event queue that have the smallest
     *  time stamp and depth. Advance the model time to their
     *  time stamp, and mark the destination actor for firing.
     *  If the time stamp is greater than the stop time then return null.
     *  If there are no events on the event queue, and _stopWhenQueueIsEmpty
     *  flag is true (which is set to true by default) then return null,
     *  which will have the effect of stopping the simulation.
     *  If _stopWhenQueueIsEmpty is false and the queue is empty, then
     *  stall the current thread by calling wait() on the _eventQueue
     *  until there are events available.  If _synchronizeToRealTime
     *  is true, then this method may suspend the calling thread using
     *  Object.wait(long) to let elapsed real time catch up with the
     *  current event.
     *  @return The next actor to fire.
     */
    protected Actor _dequeueEvents() {
        Actor actorToFire = null;
        DEEvent currentEvent = null, nextEvent = null;
        int currentDepth = 0;

        // If there is no event queue, then there is obviously no
        // actor to fire. (preinitialize() has not been called).
        if (_eventQueue == null) return null;

        // Keep taking events out until there are no more event with the same
        // tag or until the queue is empty.
        while (true) {
            // Get the next event off the event queue.
            if (_stopWhenQueueIsEmpty) {
                if (_eventQueue.isEmpty()) {
                    // Nothing more to read from queue.
                    break;
                } else {
                    nextEvent = (DEEvent)_eventQueue.get();
                }
            } else {
                // In this case we want to do a blocking read of the queue,
                // unless we have already found an actor to fire.
                if (actorToFire != null && _eventQueue.isEmpty()) break;
                while (_eventQueue.isEmpty() && !_stopRequested) {
                    if (_debugging) {
                        _debug("Queue is empty. Waiting for input events.");
                    }
                    Thread.currentThread().yield();
                    synchronized(_eventQueue) {
                        try {
                            // FIXME: If the manager gets a change request
                            // during this wait, the change request will
                            // not be executed until we emerge from this
                            // wait.  This can lead to deadlock if the UI
                            // waits for the change request to complete
                            // (which it typically does).
                            _eventQueue.wait();
                        } catch (InterruptedException e) {
                            // If the wait is interrupted, then stop waiting.
                            break;
                        }
                    }
                }
                if (_eventQueue.isEmpty()) {
                    // Nothing more to read from queue.
                    break;
                } else {
                    nextEvent = (DEEvent)_eventQueue.get();
                }
            }

            if (actorToFire == null) {
                // No previously seen event at this tag, so
                // always accept the event.  Consume it from the queue.
                _eventQueue.take();

                currentEvent = nextEvent;
                actorToFire = currentEvent.actor();

                if (_disabledActors != null &&
                        _disabledActors.contains(actorToFire)) {
                    // This actor has requested that it not be fired again.
                    if (_debugging) _debug("Skipping actor: ",
                            ((Nameable)actorToFire).getFullName());
                    actorToFire = null;
                    continue;
                }

                double currentTime = currentEvent.timeStamp();

                // If necessary, let elapsed real time catch up with
                // the event time.
                if (_synchronizeToRealTime) {
                    long elapsedTime = System.currentTimeMillis()
                        - _realStartTime;
                    // NOTE: We assume that the elapsed time can be
                    // safely cast to a double.  This means that
                    // the DE domain has an upper limit on running
                    // time of Double.MAX_VALUE milliseconds, which
                    // is probably longer than the sun is going to last
                    // (and maybe even longer than Sun Microsystems).
                    double elapsedTimeInSeconds = ((double)elapsedTime)/1000.0;
                    if (currentTime > elapsedTimeInSeconds) {
                        long timeToWait = (long)((currentTime -
                                elapsedTimeInSeconds)*1000.0);
                        if (timeToWait > 0) {
                            if (_debugging) {
                                _debug("Waiting for real time to pass: "
                                        + timeToWait);
                            }
                            synchronized(_eventQueue) {
                                try {
                                    _eventQueue.wait(timeToWait);
                                } catch (InterruptedException ex) {
                                    // Continue executing.
                                }
                            }
                        }
                    }
                }

                // Advance current time.
                if (_debugging) _debug("******* Setting current time to: ",
                        Double.toString(currentTime));
                try {
                    setCurrentTime(currentTime);
                } catch (IllegalActionException ex) {
                    // Thrown if time moves backwards.
                    throw new InternalErrorException(ex.toString());
                }

                currentDepth = currentEvent.depth();
                _microstep = currentEvent.microstep();

                if (currentTime > getStopTime()) {
                    if (_debugging) {
                        _debug("Current time has passed the stop time.");
                    }
                    _exceedStopTime = true;
                    return null;
                }

                // Transfer the event to the receiver and keep track
                // of which receiver is filled.
                DEReceiver receiver = currentEvent.receiver();

                // If receiver is null, then it's a 'pure event', and there's
                // no need to put event into receiver.
                if (receiver != null) {
                    // Transfer the event to the receiver.
                    if(_debugging) _debug(getName(),
                            "put trigger event to",
                            receiver.getContainer().getFullName());
                    receiver._triggerEvent(currentEvent.token());
                }
            } else {
                // Already seen an event.
                // Check whether the next event has equal tag.
                // If so, the destination actor should
                // be the same, but check anyway.
                if (nextEvent.isSimultaneousWith(currentEvent) &&
                        nextEvent.actor() == currentEvent.actor()) {
                    // Consume the event from the queue.

                    _eventQueue.take();

                    // Transfer the event into the receiver.
                    DEReceiver receiver = nextEvent.receiver();
                    // If receiver is null, then it's a 'pure event' and
                    // there's no need to put event into receiver.
                    if (receiver != null) {
                        // Transfer the event to the receiver.
                        receiver._triggerEvent(nextEvent.token());
                    }
                } else {
                    // Next event has a future tag or different destination.
                    break;
                }
            }
        }
        return actorToFire;
    }


    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    protected void _disableActor(Actor actor) {
        if (actor != null) {
            if(_debugging) _debug("Actor ", ((Nameable)actor).getName(),
                    " is disabled.");
            if (_disabledActors == null) {
                _disabledActors = new HashSet();
            }
            _disabledActors.add(actor);
        }
    }

    /** Put a pure event into the event queue with the specified time stamp.
     *  A "pure event" is one with no token, used to request
     *  a firing of the specified actor.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *  The depth for the queued event is equal to the depth of the actor.
     *  A smaller depth corresponds to a higher priority.
     *  The microstep for the queued event is equal to zero,
     *  unless the time is equal to the current time.
     *  If it is, then the event is queued with the current microstep
     *  plus one.  If there is no event queue, then this method does
     *  nothing.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @exception IllegalActionException If the time argument is in the past.
     */
    protected void _enqueueEvent(Actor actor, double time)
            throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        if (time == getCurrentTime()) {
            microstep = _microstep + 1;
        } else if ( time < getCurrentTime()) {
            throw new IllegalActionException((Nameable)actor,
                    "Attempt to queue an event in the past:"
                    + " Current time is " + getCurrentTime() 
                    + " while event time is " + time);
        }
        int depth = _getDepth(actor);
        if(_debugging) _debug("enqueue a pure event: ",
                ((NamedObj)actor).getName(),
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(actor, time, microstep, depth));
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, token, and time stamp. The depth of the event is the
     *  depth of the actor that has the receiver.
     *  A smaller depth corresponds
     *  to a higher priority.  The microstep is always equal to zero,
     *  unless the time argument is equal to the current time, in which
     *  case, the microstep is equal to the current microstep (determined
     *  by the last dequeue, or zero if there has been none). If there is
     *  no event queue, then this method does nothing.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        if (time == getCurrentTime()) {
            microstep = _microstep;
        } else if ( time < getCurrentTime()) {
            Nameable destination = receiver.getContainer();
            throw new IllegalActionException(destination,
                    "Attempt to queue an event in the past: "
                    + " Current time is " + getCurrentTime() 
                    + " while event time is " + time);
        }

        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        if(_debugging) _debug("enqueue event: to",
                receiver.getContainer().getFullName()
                + " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    /** Put an event into the event queue with the specified destination
     *  receiver, and token.
     *  The time stamp of the event is the
     *  current time, but the microstep is one larger than the current
     *  microstep. The depth is the depth of the actor.
     *  This method is used by actors that declare that they
     *  introduce delay, but where the value of the delay is zero.
     *  If there is no event queue, then this method does nothing.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected void _enqueueEvent(DEReceiver receiver, Token token)
            throws IllegalActionException {

        if (_eventQueue == null) return;

        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        _eventQueue.put(new DEEvent(receiver, token,
                getCurrentTime(), _microstep + 1, depth));
    }

    /** Return the depth of the actor.
     *  @exception IllegalActionException If the actor is not accessible.
     */
    protected int _getDepth(Actor actor) throws IllegalActionException {
        if (!_sortValid) {
            _computeDepth();
        }
        Integer depth = (Integer)_actorToDepth.get(actor);
        if (depth != null) {
            return depth.intValue();
        }
        throw new IllegalActionException("Attempt to get depth actor " +
                ((NamedObj)actor).getName() + " that was not sorted.");
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
    ////                         protected variables               ////

    /** The queue used for sorting events. */
    protected DEEventQueue _eventQueue;

    /** Set to true when it is time to end the execution. */
    protected boolean _noMoreActorsToFire = false;

    /** Flag that indicates that a stop has been requested. */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Construct a directed graph with the nodes representing actors and
    // directed edges representing dependencies.  The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph dag = new DirectedAcyclicGraph();

        Nameable container = getContainer();

        // If there is no container, there are no actors.
        if (!(container instanceof CompositeActor)) return dag;

        CompositeActor castContainer = (CompositeActor)container;

        // First, include all actors as nodes in the graph.
        // get all the contained actors.
        Iterator actors = castContainer.deepEntityList().iterator();
        while (actors.hasNext()) {
            dag.add(actors.next());
        }

        // Next, create the directed edges by iterating again.
        actors = castContainer.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            // get all the input ports in that actor
            Iterator ports = actor.inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort inputPort = (IOPort)ports.next();

                Set delayPorts = null;
                if (inputPort instanceof DEIOPort) {
                    DEIOPort dePort = (DEIOPort) inputPort;
                    delayPorts = dePort.getDelayToPorts();
                }

                // Find the successor of the port.
                Iterator triggers =
                    ((Actor)inputPort.getContainer()).
                    outputPortList().iterator();
                while (triggers.hasNext()) {
                    IOPort outPort = (IOPort) triggers.next();

                    if (delayPorts != null && delayPorts.contains(outPort)) {
                        // Skip this port since there is a declared delay.
                        continue;
                    }
                    // find the input ports connected to outPort
                    Iterator inPortIterator =
                        outPort.deepConnectedInPortList().iterator();
                    while (inPortIterator.hasNext()) {
                        IOPort port = (IOPort)inPortIterator.next();
                        Actor destination = (Actor)(port.getContainer());
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
            Object[] cycleNodes = dag.cycleNodes();
            StringBuffer names = new StringBuffer();
            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) names.append(", ");
                    names.append(((Nameable)cycleNodes[i]).getFullName());
                }
            }
            throw new IllegalActionException(this,
                    "Found zero delay loop including: " + names.toString());
        }
        return dag;
    }

    // Perform topological sort on the directed graph and use the result
    // to set the depth for each actor. A new Hashtable is created each
    // time this method is called.
    private void _computeDepth() throws IllegalActionException {
        DirectedAcyclicGraph dag = _constructDirectedGraph();
        Object[] sort = (Object[]) dag.topologicalSort();
        if (_debugging) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }
        // Allocate a new hash table with the equal to the
        // number of actors sorted + 1. The extra entry is
        // for the composite actor that contains this director.
        // This composite actor is set to the highest depth.
        _actorToDepth = new Hashtable(sort.length+1);
        if (_debugging) _debug(getContainer().getFullName(),
                "depth: " + sort.length);
        _actorToDepth.put(getContainer(), new Integer(sort.length));
	for(int i = sort.length-1; i >= 0; i--) {
            Actor actor = (Actor)sort[i];
            if (_debugging) _debug(((Nameable)actor).getFullName(),
                    "depth: " + i);
            // Insert the hashtable entry.
            _actorToDepth.put(actor, new Integer(i));
	}
        if (_debugging) _debug("## End of topological sort.");
        // the sort is now valid.
        _sortValid = true;
    }

    // initialize parameters. Set all parameters to their default
    // values.
    private void _initParameters() {
        try {
            startTime = new Parameter(this, "startTime",
                    new DoubleToken(0.0));
	    startTime.setTypeEquals(BaseType.DOUBLE);

            stopTime = new Parameter(this, "stopTime",
                    new DoubleToken(Double.MAX_VALUE));
	    stopTime.setTypeEquals(BaseType.DOUBLE);

	    stopWhenQueueIsEmpty = new Parameter(this, "stopWhenQueueIsEmpty",
                    new BooleanToken(true));
            stopWhenQueueIsEmpty.setTypeEquals(BaseType.BOOLEAN);

	    synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime",
                    new BooleanToken(false));
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

	    isCQAdaptive = new Parameter(this, "isCQAdaptive",
                    new BooleanToken(true));
            isCQAdaptive.setTypeEquals(BaseType.BOOLEAN);

	    minBinCount = new Parameter(this, "minBinCount",
                    new IntToken(2));
	    minBinCount.setTypeEquals(BaseType.INT);

	    binCountFactor = new Parameter(this, "binCountFactor",
                    new IntToken(2));
	    binCountFactor.setTypeEquals(BaseType.INT);
	} catch (KernelException e) {
            throw new InternalErrorException(
                    "Cannot set stopTime parameter:\n" +
                    e.getMessage());
        }
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy Classic terminology).
    // If the queue is empty, then throw an InvalidStateException
    private void _requestFiring() throws IllegalActionException {
        DEEvent nextEvent = null;
        nextEvent = _eventQueue.get();

        if (_debugging) _debug("Request refiring of opaque composite actor.");
        // Enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt(
                (Actor)getContainer(), nextEvent.timeStamp());
    }

    // Return true if this director is embedded inside an opaque composite
    // actor contained by another composite actor.
    private boolean _isEmbedded() {
        return (getContainer() != null &&
                getContainer().getContainer() != null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The current microstep.
    private int _microstep = 0;

    // Set to true when the time stamp of the token to be dequeue has
    // exceeded the stopTime.
    private boolean _exceedStopTime = false;

    // The real time at which the model begins executing.
    private long _realStartTime = 0;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    private boolean _stopWhenQueueIsEmpty = true;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime;

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private Set _disabledActors;

    // Indicator of whether the topological sort giving ports their
    // priorities is valid.
    private boolean _sortValid = false;

    // A Hashtable stores the mapping of each actor to its depth.
    private Hashtable _actorToDepth = null;
}
