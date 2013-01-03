/* Director for SysML in the style of IBM Rational Rhapsody.

 Copyright (c) 1998-2012 The Regents of the University of California.
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
package ptolemy.domains.sysml.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.FiringsRecordable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SysMLADirector

/**
 <p>
 Version A of a SysML director. This version is inspired by a
 subset of the semantics of IBM Rational's Rhapsody SysML tool.
 In this MoC, each actor executes in its own thread (corresponding
 to an "active object" in SysML). Inputs provided to an input
 port (by the thread of another actor) are put into a single queue
 belonging to the destination actor. The thread for the
 destination actor retrieves the first input in the queue
 and uses it to set the value of exactly one input port.
 All other input ports are marked absent. The actor then
 fires, possibly producing one or more outputs which are
 directed to their destination actors.
 <p>
 When multiple actors send tokens to an actor,
 whether to the same port or to distinct ports,
 this MoC is nondeterministic. The order in which the
 tokens are processed will depend on the happenstances
 of scheduling, since the tokens are put into a single queue
 in the order in which they arrive.
 <p>
 In this MoC, we assume that an actor iterates within its
 thread only if either it has called fireAt() to request a
 future firing (or a re-firing at the current time), or
 it has at least one event in its input queue. Thus, the
 actor's thread will block until one of those conditions
 is satisfied.
 <p>
 When all threads are blocked, then if at least one has
 called fireAt() to request a future firing, then this director
 will advance model time to the smallest time of such a request,
 and then again begin executing actors until they all block.
 <p>
 When all actors are blocked, and none has called fireAt(),
 the model terminates.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SysMLADirector extends ProcessDirector {

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  Create a director parameter "initialQueueCapacity" with the default
     *  value 1. This sets the initial capacities of the queues in all
     *  the receivers created in the PN domain.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  Thrown in derived classes.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SysMLADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        activeObjects = new Parameter(this, "activeObjects");
        activeObjects.setTypeEquals(BaseType.BOOLEAN);
        activeObjects.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** If true, then every actor executes in its own thread.
     *  This is a boolean that defaults to false.
     */
    public Parameter activeObjects;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the director into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new PNDirector.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SysMLADirector newObject = (SysMLADirector) super.clone(workspace);
        newObject._actorData = new ConcurrentHashMap<Actor,ActorData>();
        return newObject;
    }
    
    /** Start a new iteration (at a new time, presumably) and either
     *  run the actors to completion in order of creation or
     *  wait until a deadlock is detected, depending on activeObjects.
     *  Then deal with the deadlock
     *  by calling the protected method _resolveDeadlock() and return.
     *  This method is synchronized on the director.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public synchronized void fire() throws IllegalActionException {
        if (_activeObjectsValue) {
            // Notify all of a new iteration.
            notifyAll();
            // The superclass does all the work.
            super.fire();
        } else {
            if (_debugging) {
                _debug("Director: Called fire().");
            }
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                List<Actor> actors = ((CompositeActor) container).deepEntityList();
                int iterationCount = 1;
                for (Actor actor : actors) {
                    if (_stopRequested) {
                        return;
                    }
                    ActorData actorData = _actorData.get(actor);
                    if (actorData == null) {
                        if (_debugging) {
                            _debug("******* Actor has terminated: " + actor.getName() + " at time " + getModelTime());
                        }
                        continue;
                    }
                    if (_stopRequested) {
                        _debug("******* A stop has been requested. Aborting " + actor.getName() + " at time " + getModelTime());
                        return;
                    }

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_ITERATE, iterationCount));
                    }

                    // Get returned time, and find minimum time, so time can advance. Do that in postfire?
                    Time actorNextTime = _runToCompletion(actor);
                    if (actorNextTime.compareTo(_nextTime) < 0) {
                        _nextTime = actorNextTime;
                    }

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_ITERATE, iterationCount));
                    }
                }
                // Have made one pass through the actors. If there are non-empty
                // queues, then we need to make another pass.
                // FIXME: This is not efficient, but making it efficient can wait.
                for (Actor actor : actors) {
                    ActorData actorData = _actorData.get(actor);
                    if (actorData != null && actorData.inputQueue.size() > 0) {
                        // At least one actor has input data. Make another pass.
                        fire();
                        return;
                    }
                }
            }
        }
    }

    /** Override the base class to make a local record of the requested
     *  firing.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The requested microstep.
     *  @return An instance of Time with the current time value, or
     *   if there is an executive director, the time at which the
     *   container of this director will next be fired
     *   in response to this request.
     *  @exception IllegalActionException If there is an executive director
     *   and it throws it. Derived classes may choose to throw this
     *   exception for other reasons.
     */
    public synchronized Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        ActorData actorData = _actorData.get(actor);
        if (actorData == null) {
            throw new IllegalActionException(this, actor, "Nothing known about actor.");
        }
        actorData.fireAtTimes.add(time);
        if (_debugging) {
            _debug(actor.getFullName()
                    + " requests firing at time "
                    + time);
        }
        // The following passes the request up the hierarchy.
        if (isEmbedded()) {
            return super.fireAt(actor, time, microstep);
        }
        return time;
    }

    /** Invoke the initialize() method of ProcessDirector. Also set all the
     *  state variables to the their initial values. The list of process
     *  listeners is not reset as the developer might want to reuse the
     *  list of listeners.
     *  @exception IllegalActionException If the initialize() method of one
     *  of the deeply contained actors throws it.
     */
    public synchronized void initialize() throws IllegalActionException {
        // Recreate actor data.
        _actorData.clear();
        _activeObjectsValue = ((BooleanToken)activeObjects.getToken()).booleanValue();
        _nextTime = Time.POSITIVE_INFINITY;
        // The following calls initialize(Actor), which may
        // create the threads and start them, if using active objects.
        // It also initializes the _queueDirectory structure.
        super.initialize();
    }

    /** Initialize the given actor.  This method is generally called
     *  by the initialize() method of the director, and by the manager
     *  whenever an actor is added to an executing model as a
     *  mutation.  This method will generally perform domain-specific
     *  initialization on the specified actor and call its
     *  initialize() method.  In this base class, only the actor's
     *  initialize() method of the actor is called and no
     *  domain-specific initialization is performed.  Typical actions
     *  a director might perform include starting threads to execute
     *  the actor or checking to see whether the actor can be managed
     *  by this director.  For example, a time-based domain (such as
     *  CT) might reject sequence based actors.
     *  @param actor The actor that is to be initialized.
     *  @exception IllegalActionException If the actor is not
     *  acceptable to the domain.  Not thrown in this base class.
     */
    public void initialize(Actor actor) throws IllegalActionException {
        if (_debugging) {
            _debug("Initializing actor: " + ((Nameable) actor).getFullName() + ".");
        }
        ActorData actorData = new ActorData();
        _actorData.put(actor, actorData);
        
        if (_activeObjectsValue) {
            // Use synchronized version.
            actorData.inputQueue = Collections.synchronizedList(new LinkedList<Input>());
            // NOTE: The following does NOT initialize the actors. They initialize
            // themselves in the threads that are created by the superclass.
            super.initialize(actor);
        } else {
            // Use unsynchronized version.
            actorData.inputQueue = new LinkedList<Input>();
            // Reset the receivers.
            Iterator ports = actor.inputPortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                Receiver[][] receivers = port.getReceivers();

                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        receivers[i][j].reset();
                    }
                }
            }
            actor.initialize();
            if (_getScheduler(actor) != null) {
                _resourceScheduling = true;
            }
        }
    }

    /** Return a new receiver SysMLAReceiver.
     *  @return A new SysMLAReceiver.
     */
    public Receiver newReceiver() {
        try {
            return new SysMLAReceiver();
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    
    /** Return false if a stop has been requested or if
     *  the model has reached deadlock. Otherwise, if there is a
     *  pending fireAt request, either advance time to that
     *  requested time (if at the top level) or request a
     *  firing at that time. If there is no pending fireAt
     *  request, then return false. Otherwise, return true.
     *  @return False if the director has detected a deadlock or
     *   a stop has been requested.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        if (_nextTime.compareTo(Time.POSITIVE_INFINITY) < 0) {
            if (isEmbedded()) {
                fireContainerAt(_nextTime);
            } else {
                setModelTime(_nextTime);
                if (_debugging) {
                    _debug("+++ Advancing time to " + _nextTime);
                }
            }
        }
        _nextTime = Time.POSITIVE_INFINITY;
        return _notDone;
    }
            
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to return true if all active threads are blocked.
     *  @return True if all active threads are blocked.
     */
    protected synchronized boolean _areThreadsDeadlocked() {
        return (_getBlockedThreadsCount() >= _getActiveThreadsCount());
    }

    /** Clear all the input receivers for the specified actor.
     *  @param The actor.
     *  @throws IllegalActionException If the receivers can't be cleared.
     */
    protected void _clearReceivers(Actor actor) throws IllegalActionException {
        List<IOPort> inputPorts = actor.inputPortList();
        for (IOPort inputPort : inputPorts) {
            if (_isFlowPort(inputPort)) {
                continue;
            }
            Receiver[][] receivers = inputPort.getReceivers();
            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    if (receivers[i][j] != null) {
                        receivers[i][j].clear();
                    }
                }
            }
        }
    }

    /** Iterate the specified actor once.
     *  @return True if either prefire() returns false
     *   or postfire() returns true.
     *  @throws IllegalActionException If the actor throws it.
     */
    protected boolean _iterateActorOnce(Actor actor)
            throws IllegalActionException {
        if (_debugging) {
            _debug("---" + actor.getFullName() + ": Iterating.");
        }

        FiringsRecordable firingsRecordable = null;
        if (actor instanceof FiringsRecordable) {
            firingsRecordable = (FiringsRecordable) actor;
        }

        if (firingsRecordable != null) {
            firingsRecordable
                    .recordFiring(FiringEvent.BEFORE_PREFIRE);
        }
        boolean result = true;
        if (actor.prefire()) {

            if (firingsRecordable != null) {
                firingsRecordable
                        .recordFiring(FiringEvent.AFTER_PREFIRE);
                firingsRecordable
                        .recordFiring(FiringEvent.BEFORE_FIRE);
            }

            actor.fire();

            if (firingsRecordable != null) {
                firingsRecordable
                        .recordFiring(FiringEvent.AFTER_FIRE);
                firingsRecordable
                        .recordFiring(FiringEvent.BEFORE_POSTFIRE);
            }

            result = actor.postfire();

            if (firingsRecordable != null) {
                firingsRecordable
                        .recordFiring(FiringEvent.AFTER_POSTFIRE);
            }
        } else if (firingsRecordable != null) {
            firingsRecordable
                    .recordFiring(FiringEvent.AFTER_PREFIRE);
        }
        if (!result) {
            // Postfire returned false. Remove the actor from
            // the active actors.
            if (_debugging) {
                _debug(actor.getFullName()
                        + " postfire() returns false. Terminating actor.");
            }
            _actorData.remove(actor);
        }
        return result;
    }

    /** Iterate the specified actor until its input queue
     *  is empty and any pending fireAt() time requests
     *  are in the future. NOTE: This method is used
     *  only if activeObjects = false.
     *  @return The earliest pending fireAt time in the
     *   future, or TIME.POSITIVE_INFINITY if there is none.
     *  @throws IllegalActionException If the actor throws it.
     */
    protected Time _runToCompletion(Actor actor)
            throws IllegalActionException {
        // First, clear all input receivers that are not marked as flow ports.
        // Record whether the actor actually has any input receivers.
        _clearReceivers(actor);
        
        ActorData actorData = _actorData.get(actor);
        
        if (_debugging) {
            _debug("******* Iterating actor " + actor.getName() + " at time " + getModelTime());
            _debug("input queue: " + actorData.inputQueue);
        }
        if (actorData.inputQueue.size() == 0) {
            // Input queue is empty.
            
            if (actorData.fireAtTimes.size() == 0) {
                // Input queue is empty and no future firing
                // has been requested. Nothing more to do.
                if (_debugging) {
                    _debug(actor.getFullName()
                            + " at time "
                            + getModelTime()
                            + " waiting for input.");
                }
                return Time.POSITIVE_INFINITY;
            }
            
            // If this actor has requested a future firing,
            // then continue as long as that time has been reached.
            while (actorData.fireAtTimes.size() > 0 && !_stopRequested) {
                // Actor has requested a firing. Get the time for the request.
                Time targetTime = actorData.fireAtTimes.peek();
                    
                // If time has not advanced sufficiently, then we are done.
                if (getModelTime().compareTo(targetTime) < 0) {
                    if (_debugging) {
                        _debug(actor.getFullName()
                                + " at time "
                                + getModelTime()
                                + " waiting for time to advance to "
                                + targetTime);
                    }
                    return targetTime;
                }
                // If we get here, queue is empty, but current time
                // matches the target time, so we iterate anyway.
                // Remove the time from the pending fireAt times.
                actorData.fireAtTimes.poll();
                if (!_iterateActorOnce(actor)) {
                    return Time.POSITIVE_INFINITY;
                }
            }
            // If we get here, then there are no pending fireAt requests
            // and the input queue is empty. Nothing more to do.
            return Time.POSITIVE_INFINITY;
        }
        while (actorData.inputQueue.size() != 0 && !_stopRequested) {
            // Input queue is not empty. Extract
            // an input from the queue and deposit in the receiver, unless
            // it is an event from a flow port, in which case the value
            // has already been updated.
            Input input = actorData.inputQueue.remove(0);
            if (!input.isChangeEvent) {
                input.receiver.reallyPut(input.token);
                if (_debugging) {
                    IOPort port = input.receiver.getContainer();
                    int channel = port.getChannelForReceiver(input.receiver);
                    _debug(actor.getFullName()
                            + ": Providing input to port "
                            + port.getName()
                            + " on channel "
                            + channel
                            + " with value: "
                            + input.token);
                }
            } else if (_debugging) {
                IOPort port = input.receiver.getContainer();
                int channel = port.getChannelForReceiver(input.receiver);
                SysMLADirector.this._debug(actor.getFullName()
                        + ": Providing change event to port "
                        + port.getName()
                        + " on channel "
                        + channel);
            }
            if (!_iterateActorOnce(actor)) {
                return Time.POSITIVE_INFINITY;
            }
        }
        // If there are now pending fireAt requests, then we
        // should return the earliest one.
        if (actorData.fireAtTimes.size() > 0) {
            return actorData.fireAtTimes.peek();
        } else {
            return Time.POSITIVE_INFINITY;
        }
    }

    /** Return true if the specified port is a flow port.
     *  @param port The port.
     *  @return True if the port contains a boolean-valued parameter named "flow"
     *   with value true.
     */
    protected boolean _isFlowPort(IOPort port) {
        boolean isFlowPort = false;
        Attribute flowPortMarker = port.getAttribute("flow");
        if (flowPortMarker instanceof Parameter) {
            try {
                Token flowPortMarkerValue = ((Parameter)flowPortMarker).getToken();
                if (flowPortMarkerValue instanceof BooleanToken
                        && (((BooleanToken)flowPortMarkerValue).booleanValue())) {
                    isFlowPort = true;
                }
            } catch (IllegalActionException e) {
                // If we get an exception, ignore and assume it's not
                // a flow port.
            }
        }
        return isFlowPort;
    }

    /** Create a new ProcessThread for controlling the actor that
     *  is passed as a parameter of this method.
     *  @param actor The actor that the created ProcessThread will
     *   control.
     *  @param director The director that manages the model that the
     *   created thread is associated with.
     *  @return Return a new ProcessThread that will control the
     *   actor passed as a parameter for this method.
     *  @exception IllegalActionException If creating a new ProcessThread
     *   throws it.
     */
    protected ProcessThread _newProcessThread(Actor actor,
            ProcessDirector director) throws IllegalActionException {
        return new SingleQueueProcessThread(actor, director);
    }

    /** Return false indicating that deadlock has not been resolved
     *  and that execution will be discontinued. In derived classes,
     *  override this method to obtain domain specific handling of
     *  deadlocks. Return false if a real deadlock has occurred and
     *  the simulation can be ended. Return true if the simulation
     *  can proceed given additional data and need not be terminated.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected synchronized boolean _resolveDeadlock() throws IllegalActionException {
        // Determine the earliest time at which an actor wants to be fired next.
        Time earliestFireAtTime = null;
        List<SingleQueueProcessThread> winningThreads = new LinkedList<SingleQueueProcessThread>();
        List<Actor> actors = ((CompositeEntity)getContainer()).deepEntityList();
        for (Actor actor : actors) {
            ActorData actorData = _actorData.get(actor);
            if (actorData == null) {
                // Actor is not active.
                continue;
            }
            if (actorData.fireAtTimes != null
                    && actorData.fireAtTimes.size() > 0) {
                Time otherTime = actorData.fireAtTimes.peek();
                if (earliestFireAtTime == null || earliestFireAtTime.compareTo(otherTime) > 0) {
                    earliestFireAtTime = otherTime;
                    winningThreads.add(actorData.thread);
                }
            }
        }
        if (earliestFireAtTime == null) {
            // Time does not advance.
            if (_debugging) {
                _debug("No pending firing request. Stopping execution.");
            }
            stop();
            return false;
        }
        if (earliestFireAtTime.compareTo(getModelStopTime()) > 0) {
            // The next available time is past the stop time.
            if (_debugging) {
                _debug("Next firing request is beyond the model stop time of " + getModelStopTime());
            }
            stop();
            return false;
        }
        if (_debugging) {
            _debug("Next earliest fire at request is at time " + earliestFireAtTime);
            _debug("Setting model time to " + earliestFireAtTime);
        }
        
        // Advance model time.
        // FIXME: This will only work if this director is at the top level.
        setModelTime(earliestFireAtTime);
                
        // Mark all threads unblocked because we will either move to a new time
        // or terminate the execution.
        // _blockedThreads.clear();
        for (SingleQueueProcessThread thread : winningThreads) {
            threadUnblocked(thread, null);
        }
        // Allow execution to continue.
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Cache of the value of the activeObjects parameter as of
     *  invocation of initialize().
     */
    private boolean _activeObjectsValue = false;
    
    /** Directory of data associated with each actor. */
    private Map<Actor,ActorData> _actorData = new ConcurrentHashMap<Actor,ActorData>();
    
    /** Earliest time of a fireAt request among all actors. */
    private Time _nextTime = Time.POSITIVE_INFINITY;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure for data associated with an actor. */
    private class ActorData {
        /** Fire at times by actor. */
        public PriorityQueue<Time> fireAtTimes = new PriorityQueue<Time>();

        /** Input queues indexed by actor. */
        public List<Input> inputQueue = null;
            
        /** Directory of queues by actor. Used only if activeObjects is true. */
        public SingleQueueProcessThread thread = null;
    }
    
    /** Data structure for storing inputs in an actor's queue. */
    private class Input {
        public SysMLAReceiver receiver;
        public Token token;
        public boolean isChangeEvent;
        public String toString() {
            IOPort port = receiver.getContainer();
            int channel;
            try {
                channel = port.getChannelForReceiver(receiver);
            } catch (IllegalActionException e) {
                // This should not happen.
                return "[invalid receiver]";
            }
            if (isChangeEvent) {
                return "[changeEvent for port " + port.getName() + " channel " + channel + "]";
            }
            return "[" + token + " for port " + port.getName() + " channel " + channel + "]";
        }
    }
    
    /** A process thread that clears all input receivers, extracts one
     *  input from the input queue (if there is one), deposits that one
     *  input into the corresponding receiver, and iterates the actor.
     */
    private class SingleQueueProcessThread extends ProcessThread {
        public SingleQueueProcessThread(Actor actor, ProcessDirector director) {
            super(actor, director);
            // This constructor is called by initializeActor(), which ensures
            // that the following does not yield null.
            _myActorData = _actorData.get(actor);
            _myActorData.thread = this;
        }
        
        /** Initialize the actor, iterate it through the execution cycle
         *  until it terminates. At the end of the termination, calls wrapup
         *  on the actor.
         */
        public void run() {
            super.run();
        }
        
        /** Iterate the actor associated with this thread. This method is used
         *  only if activeObjects is true.
         *  @return True if either prefire() returns false
         *   or postfire() returns true.
         *  @throws IllegalActionException If the actor throws it.
         */
        protected boolean _iterateActor()
                throws IllegalActionException {
            // First, clear all input receivers that are not marked as flow ports.
            // Record whether the actor actually has any input receivers.
            _clearReceivers(_actor);
            boolean deleteTimeAfterIterating = false;
            // Block until either the input queue is non-empty or
            // a firing has been requested.
            synchronized (SysMLADirector.this) {
                if (SysMLADirector.this._debugging) {
                    SysMLADirector.this._debug("******* Iterating actor " + _actor.getName() + " at time " + getModelTime());
                    SysMLADirector.this._debug("input queue: " + _myActorData.inputQueue);
                }
                while (_myActorData.inputQueue.size() == 0) {
                    // Input queue is empty.
                    if (_stopRequested) {
                        return false;
                    }
                    // If this actor has requested a future firing,
                    // then block until that time is reached.
                    if (_myActorData.fireAtTimes.size() > 0) {
                        // Actor has requested a firing. Get the time for the request.
                        Time targetTime = _myActorData.fireAtTimes.peek();
                        // Indicate to delete the time from the queue upon unblocking.
                        deleteTimeAfterIterating = true;
                        
                        // Wait for time to advance.
                        while (getModelTime().compareTo(targetTime) < 0) {
                            if (_stopRequested) {
                                return false;
                            }
                            if (SysMLADirector.this._debugging) {
                                SysMLADirector.this._debug(
                                        _actor.getFullName()
                                        + " blocked at time "
                                        + getModelTime()
                                        + " waiting for time to advance to "
                                        + targetTime);
                            }

                            // Notify the director that this thread is blocked.
                            threadBlocked(this, null);
                            try {
                                SysMLADirector.this.wait();
                            } catch (InterruptedException e) {
                                if (SysMLADirector.this._debugging) {
                                    SysMLADirector.this._debug(
                                            _actor.getFullName()
                                            + " thread interrupted. Requesting stop.");
                                }
                                SysMLADirector.this.stop();
                            }
                        }
                        break; // Break out of while loop blocked on empty queue.
                    } else {
                        // Input queue is empty and no future firing
                        // has been requested. Block until the input
                        // queue is non-empty.
                        if (SysMLADirector.this._debugging) {
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + " blocked at time "
                                    + getModelTime()
                                    + " waiting for input.");
                        }
                        // Second argument indicates that no particular receiver is involved.
                        threadBlocked(this, null);
                        try {
                            SysMLADirector.this.wait();
                        } catch (InterruptedException e) {
                            if (SysMLADirector.this._debugging) {
                                SysMLADirector.this._debug(
                                        _actor.getFullName()
                                        + " thread interrupted. Requesting stop.");
                            }
                            SysMLADirector.this.stop();
                        }
                    }
                } // while (inputQueue.size() == 0).
                // Either queue is non-empty, or time has passed.
                if (SysMLADirector.this._debugging) {
                    SysMLADirector.this._debug(
                            _actor.getFullName()
                            + " unblocked at time "
                            + getModelTime()
                            + ".");
                }
                threadUnblocked(this, null);
            } // synchronized


            // Either the input queue is non-empty, or time has passed
            // to match a requested firing. If the former, then extract
            // an input from the queue and deposit in the receiver, unless
            // it is an event from a flow port, in which case the value
            // has already been updated.
            if (_myActorData.inputQueue.size() > 0) {
                Input input = _myActorData.inputQueue.remove(0);
                if (!input.isChangeEvent) {
                    input.receiver.reallyPut(input.token);
                    if (SysMLADirector.this._debugging) {
                        synchronized (SysMLADirector.this) {
                            IOPort port = input.receiver.getContainer();
                            int channel = port.getChannelForReceiver(input.receiver);
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + ": Providing input to port "
                                    + port.getName()
                                    + " on channel "
                                    + channel
                                    + " with value: "
                                    + input.token);
                        }
                    }
                } else {
                    if (SysMLADirector.this._debugging) {
                        synchronized (SysMLADirector.this) {
                            IOPort port = input.receiver.getContainer();
                            int channel = port.getChannelForReceiver(input.receiver);
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + ": Providing change event to port "
                                    + port.getName()
                                    + " on channel "
                                    + channel);
                        }
                    }
                }
            }
            
            // Now, finally, actually iterate the actor.
            // Note that actor may have an empty input queue now,
            // and also the input ports may not have any data.
            // Catch any exceptions so that we can be sure to
            // remove the actor from the active list.
            try {
                if (SysMLADirector.this._debugging) {
                    synchronized (SysMLADirector.this) {
                        SysMLADirector.this._debug(
                                _actor.getFullName()
                                + ": Iterating.");
                    }
                }

                boolean result = super._iterateActor();
                
                if (deleteTimeAfterIterating) {
                    // After iterating the actor, if in fact the input queue
                    // was empty and this firing was caused by time advancing to
                    // match the requested time.
                    _myActorData.fireAtTimes.poll();
                }

                if (result == false) {
                    // Postfire returned false. Remove the actor from
                    // the active actors.
                    synchronized(SysMLADirector.this) {
                        if (SysMLADirector.this._debugging) {
                            SysMLADirector.this._debug(
                                    _actor.getFullName()
                                    + " postfire() returns false. Ending thread.");
                        }
                        removeThread(this);
                        _actorData.remove(_actor);
                        SysMLADirector.this.notifyAll();
                    }
                }
                return result;
            } catch (Throwable ex) {
                // Actor threw an exception.
                synchronized(SysMLADirector.this) {
                    removeThread(this);
                    _actorData.remove(_actor);
                    SysMLADirector.this.stop();
                    SysMLADirector.this.notifyAll();
                }
                if (ex instanceof IllegalActionException) {
                    throw (IllegalActionException)ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException)ex;
                }
                return false;
            }
        }
        /** The actor data for this thread's actor. */
        private ActorData _myActorData;
    }
    
    /** Variant of a Mailbox that overrides the put() method to
     *  divert the input to the queue associated with the actor
     *  and then provides a method to really put a token into
     *  a receiver. If the containing port has a boolean-valued
     *  attribute named "flow", then the behavior is quite different.
     *  First, a put updates the value of the receiver immediately
     *  in addition to putting a "change event" on the event queue.
     *  Second, the receiver value is persistent. A get() does not
     *  clear the receiver.
     */
    public class SysMLAReceiver extends Mailbox {
        public SysMLAReceiver() throws IllegalActionException {
            this(null);
        }
        public SysMLAReceiver(IOPort container) throws IllegalActionException {
            super(container);
        }
        
        /** Get the contained Token.  If there is none, throw an exception.
         *  The token is removed.
         *  @return The token contained by this mailbox.
         *  @exception NoTokenException If this mailbox is empty.
         */
        public Token get() throws NoTokenException {
            if (_token == null) {
                throw new NoTokenException(getContainer(),
                        "Attempt to get data from an empty mailbox.");
            }
            IOPort port = getContainer();
            boolean isFlowPort = _isFlowPort(port);
            if (isFlowPort) {
                // Value is persistent. Do not clear.
                return _token;
            } else {
                Token token = _token;
                _token = null;
                return token;
            }
        }

        /** Put a token into the queue for containing actor.
         *  @param token The token to be put into the queue.
         */
        public void put(Token token) {
            IOPort port = getContainer();
            boolean isFlowPort = _isFlowPort(port);
            Actor actor = (Actor)port.getContainer();
            ActorData actorData = _actorData.get(actor);
            if (actorData != null) {
                Input input = new Input();
                input.receiver = this;
                input.token = token;
                input.isChangeEvent = isFlowPort;
                // Notify the director that this queue is not empty.
                synchronized(SysMLADirector.this) {
                    actorData.inputQueue.add(input);
                    if (SysMLADirector.this._debugging) {
                        SysMLADirector.this._debug("Adding to queue for "
                                + actor.getName() + " at time " + getModelTime()
                                + ": " + input);
                        SysMLADirector.this._debug("input queue: " + actorData.inputQueue);
                    }
                    if (_activeObjectsValue) {
                        threadUnblocked(actorData.thread, null);
                        SysMLADirector.this.notifyAll();
                    }
                }
            }
            if (isFlowPort) {
                // Do not use super.put() here because it
                // will throw an exception if there already
                // is a token.
                _token = token;
            }
        }
        
        /** Put a token into the mailbox.
         *  @param token The token to be put into the mailbox.
         *  @exception NoRoomException If this mailbox is not empty.
         */
        public void reallyPut(Token token) throws NoRoomException {
            super.put(token);
        }
    }
}
