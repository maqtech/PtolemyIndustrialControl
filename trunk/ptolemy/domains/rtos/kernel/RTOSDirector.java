/* A director that implements a priority-driven multitasking model of
computation.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DECQEventQueue;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashSet;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RTOSDirector
/**
A director that implements a priority-driven multitasking 
model of computation. This model of computation is usually seen in
real-time operating systems.
<P>
Each actor in this domain is called a task. A task is eligible to 
execute if there is an event that triggers it. Source actors may trigger
themselves by calling fireAt(time, actor) on this director. This call
is treated as an interrupt that happens at that particular time.
A task can have a priority and an execution time, specified by (adding)
<i>priority</i> and <i>executionTime</i> parameters. The <i>priority</i>
parameter takes an integer value, and the <i>executionTime</i>
parameter takes a double value. These parameters may also be specified
on a per input port basis, if the actor reacts differently
to input events at different ports. If these parameters are not
specified, then the default priority value is the java.Thread.NORM
on the JVM, and the default execution time is 0.
<P>
This domain assumes there is a single resource, say CPU, shared by  
the execution of all actors. At one particular time, only
one of the tasks can get the resource and execute. If the execution
is preemptable (by setting the <i>preemptive</i> parameter of
this director to true), then the execution of one task 
may be preempted by another eligible task with a higher priority.
Otherwise, the higher priority task has to wait until the current
task finishes its execution.
<P>
The priority-driven execution is achieved by using an event 
dispatcher, which sorts and dispatches events that trigger
the execution of tasks. The events being dispatched are called 
RTOS events (implemented by the RTOSEvent class). 
An RTOS event has a priority and a remaining processing time,
among other properties. The priority of the event
is inherited from its destination port, which may further inherit
its priority from the actor that contains the port. Whenever an 
event is produced by an actor, it is queued with the event dispatcher.
At any time, the event with the highest priority is dequeued, 
and delivered into its destination receiver. The director then starts
the execution of the destination actor (by calling its prefire()
method). After that, the director tracks how much time remained
for the task to finish processing the event. 
<P>
The events, called interrupt events, produced by calling fireAt()
on this director are treated differently. These events carry
a time stamp, and are queued with another queue which sorts these
events in their chronological order. When the modeling time reaches
an interrupt event time, (regardless whether there is a task 
executing), the interrupt event is processed. And the corresponding
(source) actor is fired, which may in turn produce some RTOS events.
If one of these RTOS events has a higher priority than the event
being processed by the current task, and the execution is preemptive,
then the current tasks is stalled, and the task triggered by the
highest priority event is started. Note that, a task is always
granted the resource that is specified by the <i>executionTime</i>,
no matter whether it has been preempted.
When that amount of time is elapsed, the fire() method of the actor
will be called, and the actor is expected to produce its output, if
there is any.
<P>
The RTOS domain can be nested with other (timed) domains. In that
case, the inputs from the outside domain are treated as interrupts
that happen at the (outside) current time.
<p>
This director supports executions that synchronize to real time.
To enable such an execution, set the <i>synchronizeToRealTime</i>
parameter to true.

@author  Jie Liu, Edward A. Lee
@version $Id$
@see ptolemy.domains.de.kernel.DEEvent
*/

public class RTOSDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public RTOSDirector() {
        super();
        _initParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public RTOSDirector(Workspace workspace) {
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
     *  @exception NameDuplicationException If
     *   the name collides with a property in the container.
     */
    public RTOSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicating whether the execution of the actors is preemptable.
     *  The default value is false, of type boolean.
     */
    public Parameter preemptive;

    /** The default execution time of a task (i.e. actor). If an actor
     *  (or its ports)
     *  does not specify its execution time, then this number will
     *  be used. The default value is 0.0, of type double, meaning that
     *  the task takes no time to execute (similar to the synchrony
     *  assumption in Synchronous/Reactive models).
     */
    public Parameter defaultTaskExecutionTime;

    /** The stop time of the model, only effective when this director
     *  is at the top level.  This parameter is of type double.
     *  The value defaults to Double.MAX_VALUE.
     */
    public Parameter stopTime;

    /** Indicating whether the execution synchronizes to the
     *  real time. This parameter has default value false, of type boolean.
     *  If this parameter is true, then the director does not process 
     *  events until the
     *  elapsed real time matches the time stamp of the events.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////
    
    /** Update the director parameters when the attributes are changed.
     *  If the change is <i>stopTime</i>, or <i>defaultTaskExecutionTime</i>,
     *  then check whether its value is less than zero. If so, throw an 
     *  exception.
     *  
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (_debugging) _debug("Updating RTOSDirector parameter",
                attribute.getName());
        if (attribute == stopTime) {
            if (((DoubleToken)stopTime.getToken()).doubleValue() < 0.0) {
                throw new IllegalActionException(this,
                        " stopTime cannot be less than 0.");
            }
        } else if (attribute == defaultTaskExecutionTime) {
            if (((DoubleToken)defaultTaskExecutionTime.getToken())
                    .doubleValue() < 0.0) {
                throw new IllegalActionException(this,
                        " task execution time cannot be less than 0.");
            }
        } else if (attribute == preemptive) {
            _preemptive = ((BooleanToken)preemptive.getToken()).booleanValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime =
                ((BooleanToken)synchronizeToRealTime.getToken()).
                booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Execute the model for one iteration. It first 
     *  compare the current time to the time stamp of the first
     *  event in the interrupt event queue. If they are equal, then
     *  dequeue the events at the current time from the interrupt event
     *  queue, and fire the destination actors.
     *  If the current time is less than the time stamp of the
     *  first event in the interrupt event queue, then look at the RTOS
     *  event queue. Pick
     *  the task with the highest priority and start/continue its execution.
     *  If the task has <i>executionTime</i> being zero, then finish
     *  that task. Otherwise, finish its execution util time advances.
     *  @exception IllegalActionException If an interrupt is in the past,
     *   or one of the execution methods of an actor throws it.
     */
    public void fire() throws IllegalActionException {
        _nextIterationTime = ((DoubleToken)stopTime.getToken()).
                doubleValue();
        
        // First look at interrupt events.
        while (!_interruptQueue.isEmpty()) {
            DEEvent interruptEvent = (DEEvent)_interruptQueue.get();
            double timeStamp = interruptEvent.timeStamp();
            if (timeStamp < (getCurrentTime() - 1e-10)) {
                // This should never happen.
                throw new IllegalActionException(this, 
                            "external input in the past.");
            } else if (Math.abs(timeStamp - getCurrentTime()) < 1e-10) {
                _interruptQueue.take();
                Actor actor = interruptEvent.actor();
                if (actor != null) {
                    // ATTN: Received an interrupt
                    if (actor.prefire()) {
                        // ATTN: Produce interrupt event.
                        actor.fire();
                        if(!actor.postfire()) {
                            _disableActor(actor);
                        }
                    }
                }
            } else {
                // All interrupts are in the future.
                // Get the time for the next interrupt.
                // It will be used for finding the next iteration time.
                _nextIterationTime = timeStamp;
                break;
            }
        }
        // Then we process RTOS events.
        RTOSEvent event = null;
        while (!_eventQueue.isEmpty()) {
            event = (RTOSEvent)_eventQueue.get();
            
            if(_debugging) _debug("The first event in the queue is ",
                    event.toString());
            // Notice that the first event in the queue
            // either has processing time > 0 or hasn't been started.
            if (!event.hasStarted()) {
                if(_debugging) _debug(getName(),
                        "put trigger event ",  event.toString(), " into " 
                        + ((NamedObj)event.actor()).getName() 
                        + " and processing");
                event.receiver()._triggerEvent(event.token());
                // FIXME: What shall we do if there are events to
                // the same actor with the same priority.
                // Check if there are any events with equal priority
                // for this actor. If so, make them available to the 
                // actor at the same time.
                
                Actor actor = event.actor();
                if(actor == getContainer() || !actor.prefire()) {
                    // If the actor is the container of this director,
                    // then the event is at the output boundary.
                    // Remove the event and look at the next event.
                    // ATTN: For the prefire(), the task is triggered
                    // but not ready.
                    _eventQueue.take();
                    event = null;
                    // Return to the while loop.
                } else {
                    // ATTN: The task is triggered and ready to run.
                    // ATTN: produce task start event.
                    // Determine the processing time.
                    double processingTime = ((DoubleToken)
                            defaultTaskExecutionTime.getToken()).doubleValue();
                    if (actor instanceof RTOSActor) {
                        processingTime = ((RTOSActor)actor).
                            getExecutionTime();
                    } else {
                        // Use the executionTime parameter from the port
                        // or the actor.
                        Parameter executionTime = (Parameter)
                            ((IOPort)event.receiver().getContainer()).
                            getAttribute("executionTime");
                        if (executionTime == null) {
                            executionTime = (Parameter)((NamedObj)actor).
                                getAttribute("executionTime");
                        }
                        if (executionTime != null) {
                            processingTime = ((DoubleToken)executionTime.
                                    getToken()).doubleValue();
                        }
                    }
                    if (processingTime == 0.0) {
                        if(_debugging) _debug(getName(), event.toString(),
                                " has processing time 0, so processed.");
                        // This event  can be processed immediately.
                        _eventQueue.take();
                        //ATTN: The task is finished immediately.
                        actor.fire();
                        if (!actor.postfire()) {
                            _disableActor(actor);
                        }
                    } else {
                        // Really start a task with non-zero processing
                        // time.
                        event.setProcessingTime(processingTime);
                        if(_debugging) _debug("Set processing time ",
                                event.toString());
                        // Start a new task.
                        // Now the behavior depend on whether the 
                        // execution is preemptive.
                        if (!_preemptive) {
                            event = (RTOSEvent)_eventQueue.take();
                            event.startProcessing();
                                // Set it priority to o so it won't 
                                // be preempted.
                            event.setPriority(0);
                            _eventQueue.put(event);
                        } else {
                            event.startProcessing();
                        }
                        if (_debugging) _debug("Start processing ", 
                                event.toString());
                        // Start one real time task a time.
                        break;
                    }
                }
            } else {
                break;
            }
        }
        // The event may be null, when the prefire() of the actor 
        // returns false for the last event in the queue.
        // The event may have processing time < 0, in which case
        // it is an event at the output boundary.
        if (event != null && event.processingTime() > 0) {
            // Check the finish processing time.
            double finishTime = getCurrentTime() + 
                event.processingTime();
            if ( finishTime < _nextIterationTime) {
                _nextIterationTime = finishTime;
            }
        }
        if(_isEmbedded() && _nextIterationTime < Double.MAX_VALUE) {
            _requestFiringAt(_nextIterationTime);
        }
    }
    
    /** Request an interrupt at the specified time. This inserts
     *  an event into the interrupt event queue.
     *  The corresponding actor will be executed when the current
     *  time of this director reaches the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If requested time is in
     *  the past.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {

        // ignore requests that are later than the stop time.
        if (_debugging) {
            _debug("+ requesting firing of "
                   + ((Nameable)actor).getFullName()
                   + " at time "
                   + time);
        }
        if (time < getCurrentTime()) {
            throw new IllegalActionException(this, ((NamedObj)actor).getName()
                    + " request an interrupt in the past.");
        }
        if (time <= ((DoubleToken)stopTime.getToken()).doubleValue()) {
            // create an interrupt event.
            DEEvent interruptEvent = new DEEvent(actor, time, 0, 0);
            //ATTN: request an interrupt in the future.
            _interruptQueue.put(interruptEvent);
        }
    }

    /** Return the system time at which the model began executing.
     *  That is, the system time (in milliseconds) when the initialize()
     *  method of the director is called.
     *  The time is in the form of milliseconds counting
     *  from 1/1/1970 (UTC).
     *  If this method is called before initialize() method
     *  is called, then the return value may be meaningless.
     *  @return The real start time of the model.
     */
    public long getRealStartTimeMillis() {
        return _realStartTime;
    }

    /** Set the starting time of execution and initialize all the 
     *  actors. If this is director is not at the top level and 
     *  the interrupt event queue is not empty, the request a refire from
     *  the executive director.
     *  
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        
        if(_isEmbedded()) {
            _outsideTime = ((CompositeActor)getContainer()).
                getExecutiveDirector().getCurrentTime();
        } else {
            _outsideTime = 0.0;
            _nextIterationTime = 0.0;
        }
        super.initialize();
        _realStartTime = System.currentTimeMillis();
        if (_isEmbedded() && !_interruptQueue.isEmpty()) {
            double nextPureEventTime = 
                ((DEEvent)_interruptQueue.get()).timeStamp();
            _requestFiringAt(nextPureEventTime);
        }
    }

    /** Return a new RTOSReceiver.
     *  @return a new RTOSReceiver.
     */
    public Receiver newReceiver() {
        if(_debugging) _debug("Creating new RTOS receiver.");
	return new RTOSReceiver();
    }

    
    /** Advance time to the next event time (or the outside time
     *  if this director is embedded in another domain); if there
     *  are any tasks that finish at the current time, then 
     *  finish the execution of the current task by calling the
     *  fire() method of that actors. 
     *  
     *  If <i>synchronizeToRealTime</i> is true, then wait until the
     *  real time has caught up the current time. 
     *  @return True
     *  @exception IllegalActionException If the execution method 
     *  of one of the actors throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_isEmbedded()) {
            _outsideTime = ((CompositeActor)getContainer())
                .getExecutiveDirector().getCurrentTime();
        } else {
            // set outside time to the next iteration time, which
            // is the smaller one of the next interrupt event time and
            // the finishing time of processing the next RTOS event.
            _outsideTime = _nextIterationTime;
        }
        if (_debugging) _debug("Prefire: outside time = " + _outsideTime,
                " current time = " + getCurrentTime());
        if (!_eventQueue.isEmpty()) {
            RTOSEvent event = (RTOSEvent)_eventQueue.get();
            if (event.hasStarted()) {
                if (_debugging) _debug("deduct "+ 
                        (_outsideTime - getCurrentTime()), 
                        " from processing time of event",
                        event.toString());
                //ATTN: Reduce the processing time of the actor by
                // the time progressed.
                event.timeProgress(_outsideTime - getCurrentTime());
                // Finish the tasks if it ends at this time.
                // We do it here to ensure that it is done before
                // the transfer input from composite actors.
                if (Math.abs(event.processingTime()) < 1e-10) {
                    if(_debugging) _debug(getName(),
                            "finish processing ", event.toString());
                    //ATTN: Finished a task.
                    _eventQueue.take();
                    Actor actor = event.actor();
                    actor.fire();
                    // Should handle dead actors.
                    if (!actor.postfire()) {
                        _disableActor(actor);
                    }
                }
            }
        }
        if (!_isEmbedded() && _synchronizeToRealTime) {
            // Wait for real time to cache up.
            long elapsedTime = System.currentTimeMillis()
                        - _realStartTime;
            double elapsedTimeInSeconds = ((double)elapsedTime)/1000.0;
            if (Math.abs(_outsideTime - elapsedTimeInSeconds) > 1e-3) {
                long timeToWait = (long)((_outsideTime -
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
        setCurrentTime(_outsideTime);
        return true;
    }

    /** In addition to the preinitialization implemented in the super
     *  class, create the interrupt event queue and the RTOS event queue.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void preinitialize() throws IllegalActionException {
        _eventQueue = new CalendarQueue(new RTOSEventComparator(), 16, 2);
        _interruptQueue =  new DECQEventQueue(2, 2, true);
        
        _disabledActors = null;
        //_noMoreActorsToFire = false;

        // Add debug listeners.
        if (_debugListeners != null) {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                DebugListener listener = (DebugListener)listeners.next();
                _interruptQueue.addDebugListener(listener);
            }
        }
        super.preinitialize();
    }

    /** If the current time is greater than the stop time, or
     *  both interrupt event queue and RTOS event queue are empty
     *  then return false. Otherwise, return true.
     *  @return Whether the execution should continue.
     */
    public boolean postfire() throws IllegalActionException {
        if(_debugging) _debug("Finish one iteration at time:" + 
                getCurrentTime(),
                " Next iteration time = " + _nextIterationTime);
        if (!_isEmbedded()) {
            if (getCurrentTime() >= ((DoubleToken)stopTime.getToken()).
                    doubleValue()) {
                return false;
            }
            if (_eventQueue.isEmpty() && _interruptQueue.isEmpty()) {
                return false;
            }
        }
        return true;

    }
    
    /** Set the current time of this director. This method override the
     *  default implementation, since time is allowed to go backward.
     *  @param newTime The current time to set.
     *
    public void setCurrentTime(double newTime) throws IllegalActionException {
        _currentTime = newTime;
    }
    */

    ////////////////////////////////////////////////////////////////////////
    ////                    protected methods                           ////
        
    /** Disable the specified actor.  All events destinated for this actor
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

    /** Put an event into the event queue with the specified destination
     *  receiver, token, and priority.
     *  @param event The event to be enqueued.
     */
    protected void _enqueueEvent(RTOSEvent event) {
        if (_eventQueue == null) return;
        if (event.actor() == getContainer()) {
            // This is an event at the output boundary, so give it
            // the highest priority.
            event.setPriority(0);
        }
        if(_debugging) _debug("enqueue event: to",
                event.toString());
        _eventQueue.put(event);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                    private methods                           ////

    // Remove useless parameters inherited from DEDirector, and set
    // different defaults.
    private void _initParameters() {
        try {

            stopTime = new Parameter(this, "stopTime",
                    new DoubleToken(Double.MAX_VALUE));
	    stopTime.setTypeEquals(BaseType.DOUBLE);
            preemptive = new Parameter(this, "preemptive", 
                    new BooleanToken(false));
            preemptive.setTypeEquals(BaseType.BOOLEAN);
            defaultTaskExecutionTime = new Parameter(this, 
                    "defaultTaskExecutionTime", new DoubleToken(0.0));
            defaultTaskExecutionTime.setTypeEquals(BaseType.DOUBLE);
            
            synchronizeToRealTime = new Parameter(this, 
                    "synchronizeToRealTime",
                    new BooleanToken(false));
            synchronizeToRealTime.setTypeEquals(BaseType.BOOLEAN);

        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() +
                    "fail to initialize parameters.");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(getName() +
                    "fail to initialize parameters.");
        }
    }

    // Return true if this director is embedded inside an opaque composite
    // actor contained by another composite actor.
    private boolean _isEmbedded() {
        return (getContainer() != null &&
                getContainer().getContainer() != null);
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor (i.e. a wormhole in Ptolemy Classic terminology).
    // If the queue is empty, then throw an InvalidStateException
    private void _requestFiringAt(double time) throws IllegalActionException {
        if (_debugging) _debug("Request refiring of composite actor.",
                getContainer().getName(), "at " + time);
        // Enqueue a refire for the container of this director.
        ((CompositeActor)getContainer()).getExecutiveDirector().fireAt(
                (Actor)getContainer(), time);
    }
    

    ////////////////////////////////////////////////////////////////////////
    ////                    private variables                           ////

    // The RTOS event queue.
    private CalendarQueue _eventQueue;

    // The interrupt event queue.
    // Time in this queue is absolute, and not affected by the execution
    // of other tasks.
    private DECQEventQueue _interruptQueue; 

    // local cache of the parameter
    private boolean _preemptive = false;

    // local cache of synchronize to real time
    private boolean _synchronizeToRealTime = false;

    // disabled actors list.
    private Set _disabledActors = null;

    // The outside time
    private double _outsideTime;

    // The next iteration time
    private double _nextIterationTime;

    // The real start time in milliseconds count.
    private long _realStartTime;
}
