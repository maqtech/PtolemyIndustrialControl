/* A CT Director that handles the interaction with event based domains.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.ct.kernel;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
   This is a CTDirector that supports the interaction of the continuous-time
   simulation with event-based domains. This director can both serve as
   a top-level director and an inside director that is contained by
   a composite actor in an event-based domain. If it is a top-level
   director, it acts exactly like a CTMultiSolverDirector. If it is
   embedded in an event-based domain, it will run ahead of the global
   time and prepare to roll back if necessary.
   <P>
   This director has an extra parameter compared to the CTMultiSolverDirector,
   the maximum run ahead of time length (<code>runAheadLength</code>).
   Its default value is 1.0.
   <P>
   The running ahead of time is achieved by the following mechanism.<br>
   <UL>
   <LI> At the initialize stage of an execution, the director requests
   a firing at the global current time.
   <LI> At each prefire stage of the execution, the fire end time is computed
   based on the current time of the executive director, t1, the next iteration
   time of the executive director, t2, and the value of the parameter
   <code>runAheadLength</code>, t3. The fire end time is t1 + min(t2, t3)
   <LI> At the prefire stage, the local current time is compared with the
   current time of the executive director. If the local time is later than
   the executive director time, then the directed system will rollback to a
   "known good" state.
   <LI> The "known good" state is the state of the system at the time when
   local time is equal to the current time of the executive director.
   <LI> At the fire stage, the director will stop at the first of the
   following two times, the fire end time and the first detected event time.
   </UL>

   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class CTMixedSignalDirector extends CTMultiSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTMixedSignalDirector() {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMixedSignalDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTMixedSignalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                      ////

    /** Parameter of the run ahead length. The default value is 1.0.
     */
    public Parameter runAheadLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the runAhendLength does not have
     *  a valid token, or the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == runAheadLength) {
            if (_debugging) _debug("run ahead length updating.");
            double value =
                ((DoubleToken)runAheadLength.getToken()).doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        " runAheadLength cannot be negative.");
            }
            _runAheadLength = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return true indicating that this director can be an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Execute the directed (sub)system to the iteration end time.
     *  If the current phase is an event phase, (in the sense that
     *  discrete events will be produced or consumed),
     *  this director will consume all the input
     *  tokens, produce all output tokens, and then request a zero
     *  delay refire from it executive director.
     *  If this is a top-level director, the iteration end time is the
     *  current time at the beginning of the fire() method plus the
     *  the step size of one accurate step.
     *  Otherwise, it executes until one of the following conditions
     *  is satisfied. 1) The iteration end time computed in the prefire()
     *  method is reached. 2) An event is generated.
     *  It saves the state of the system at the current time of the executive
     *  director as the "known good" state, and runs ahead of that time.
     *  The "known good" state is used for roll back.
     *  @exception IllegalActionException If thrown by the ODE solver,
     *       or the prefire() or the fire() methods of an actor.
     */
    public void fire() throws IllegalActionException {
        if (_isTopLevel()) {
            super.fire();
            return;
        }
        
        _discretePhaseExecution();
        // Mark states and prepare for roll back.
        _markStates();
        // Guarantee to stop at the iteration end time.
        _setIterationBeginTime(getModelTime());
        // FIXME: the following statement may not be necessary.
        fireAt((CompositeActor)getContainer(), getIterationEndTime());

        _continuousPhaseExecution();
    }

    /** Initialize the execution. If this director is not at the top level, 
     *  ask the executive director to fire the container of this director 
     *  at the current model time.
     *  @see CTMultiSolverDirector#initialize()
     *  @exception IllegalActionException If thrown by the initialize method
     *  of super class, or the quest for refiring can not be accepted.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _mutationVersion = -1;
        if (!_isTopLevel()) {
            TypedCompositeActor container = (TypedCompositeActor)getContainer();
            Director exe = container.getExecutiveDirector();
            exe.fireAt(container, getModelTime());
        }
    }

    /** If this is a top-level director, behave exactly as a
     *  CTMultiSolverDirector, otherwise always return true.
     *  @return True if this is not a top-level director or the simulation
     *  is not finished.
     *  @exception IllegalActionException If thrown in the postfire method
     *  in the super class.
     */
    public boolean postfire() throws IllegalActionException {
        // FIXME: how to inform the upper level when the next scheduled 
        // firing will be? 
        if (!_isTopLevel()) {
            _secondPrefire = false;
        }
        return super.postfire();
    }

    /** Always returns true, indicating that the (sub)system is always ready
     *  for one iteration.
     *  <P>
     *  If this is not a top-level director, some additional work is done
     *  to synchronize time with the executive director. In particular,
     *  it will compare its local time, say t, with the current time
     *  of the executive director, say t0.
     *  If t == t0, do nothing. <BR>
     *  If t > t0, then rollback to the "known good" time (which should be
     *  less than the outside time) and catch up with the outside time. <BR>
     *  If t < t0, then throw an exception because the CT subsystem
     *  should always run ahead of time. <BR>
     *  <P>
     *  If this director is not a top-level director, the iteration end time is
     *  resolved from the current time of the outside domains, say t1,
     *  the next iteration time of the outside domain, say t2, and
     *  the runAheadLength parameter of this director, say t3.
     *  The iteration end time is set to be <code>t5 = t1 + min(t2, t3)</code>. 
     *  The iteration end time may be further refined in the fire() method 
     *  due to possible event generated during the iteration. 
     *  In particular, when the first event is detected, say at t5 and t5 < t4,
     *  then the iteration ends at t5.
     *  @return true Always.
     *  @exception IllegalActionException If the local time is
     *       less than the current time of the executive director,
     *       or thrown by a directed actor.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug(getName(), " prefire: ");
        }
        if (!_isTopLevel()) {
            // synchronize the local time with the outside time.
            CompositeActor container = (CompositeActor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            _outsideTime = executiveDirector.getModelTime();
            Time localTime = getModelTime();
            double timeResolution = getTimeResolution();
            Time outsideNextIterationTime = 
                executiveDirector.getModelNextIterationTime();

            if (_debugging) {
                _debug("The current time of outside model is " + _outsideTime,
                    "\n its next iteration time is " + outsideNextIterationTime,
                    "\nThe current time of this director is " + localTime);
            }

            // Now, check the next iteration time.
            if (outsideNextIterationTime.compareTo(_outsideTime) < 0) {
                // FIXME: This seems redundant. The outside director should 
                // guarantee that this never happen.
                throw new IllegalActionException(this, "Outside domain"
                        + " time is going backward."
                        + " Current outside time = " + _outsideTime
                        + ", but the next iteration time = "
                        + outsideNextIterationTime);
            }

            // If outside next iteration time is equal to the outside
            // time, then request for a zero delay refire.
            // FIXME: we need to support integration with zero step size.
            // FIXME: the following "if" block does not make sense to me...
            if (outsideNextIterationTime.equals(_outsideTime) 
                && (_secondPrefire == false)) {
                executiveDirector.fireAt(container, outsideNextIterationTime);
                _secondPrefire = true;
                return false;
            }

            // Ideally, the outside time should equal the local time. 
            // If the outside time is less than the local time, then rollback 
            // is needed. If the outside time is greater than the local time, 
            // an exception will be thrown.
            if (_outsideTime.equals(localTime)) {
                // We are woke up as we requested.
                if (_debugging) {
                    _debug("The outside time is equal to the local time. " +
                            "Check whether there are outputs.");
                }
                // FIXME: The following code is not necessary. A correct
                // implementation of CT director should have already transfered
                // events to outside when the container of the director fires. 
                
                // Process local discrete events and emit outputs
                // if there are any. If there are any outputs emitted,
                // request for a zero delay refire and return false.
                if (_hasDiscreteEvents) {
                    _discretePhaseExecution();
                    boolean hasOutput = false;
                    Iterator outports = container.outputPortList().iterator();
                    while (outports.hasNext()) {
                        IOPort p = (IOPort)outports.next();
                        if (executiveDirector.transferOutputs(p)) {
                            hasOutput = true;
                        }
                    }
                    _hasDiscreteEvents = false;
                    if (hasOutput) {
                        if (_debugging) {
                            _debug(getName(),
                                " produces output to the outside domain.",
                                " Requesting zero delay refiring",
                                " Prefire() returns false.");
                        }
                        executiveDirector.fireAt(container, _outsideTime);
                        return false;
                    }
                }
            } else if (_outsideTime.compareTo(localTime) > 0) {
                throw new IllegalActionException(this, executiveDirector,
                        "Outside time is later than the local time. " +
                        "This should never happen.");
            } else if (_outsideTime.compareTo(localTime) < 0) {
                // Outside time less than the local time. Rollback!
                if (_debugging) {
                    _debug(getName() + " rollback from: " +
                        localTime + " to: " +_knownGoodTime +
                        "due to outside time " +_outsideTime );
                }
                // The local time is set backwards to a known good time.
                _rollback();  
                // Set a catch-up destination time by registering the 
                // outside time as a breakpoint.
                fireAt(container, _outsideTime);
                // The local time is set to the outside time.
                _catchUp(); 
                
                if (_debugging) {
                    _debug("After catch up, the current time is " + localTime);
                }
            }

            // Now, we have outside time equals the curren time.
            double aheadLength 
                = outsideNextIterationTime.subtract(_outsideTime).
                    getDoubleValue();
            if (_debugging) {
                _debug(getName(),
                    " local time = " + localTime,
                    " Outside Time = " + _outsideTime,
                    " NextIterationTime = " + outsideNextIterationTime +
                    " Inferred run length = " + aheadLength);
            }

            if (aheadLength < timeResolution ) {
                // This is a zero step size iteration.
                _setIterationEndTime(_outsideTime);
                if (_debugging) {
                    _debug( "This is an iteration with the step size as 0.");
                }
            } else if (aheadLength < _runAheadLength) {
                _setIterationEndTime(outsideNextIterationTime);
            } else {
                // aheadLength > _runAheadLength parameter.
                _setIterationEndTime(_outsideTime.add(_runAheadLength));
            }
            // Now it is safe to execute the continuous part.
            if (_debugging) {
                _debug(getName(), "Iteration end time = " +
                    getIterationEndTime(), " <-- end of prefire.");
            }
            return true;
        } else {
            return super.prefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Catch up the simulation from a known good state to the outside
     *  current time. There should be no breakpoints of any kind
     *  in this process. If the current time is greater than or equal
     *  to the outside time, then do nothing.
     *  @exception IllegalActionException If thrown from the execution
     *  methods from any actor.
     */
    protected void _catchUp() throws IllegalActionException {
        Time outsideTime = _getOutsideTime();
        Time localTime = getModelTime();
        if (localTime.compareTo(outsideTime) >= 0) {
            return;
        }
        _setIterationBeginTime(localTime);
        while (!localTime.equals(outsideTime)) {
            setCurrentStepSize(getSuggestedNextStepSize());
            _processBreakpoints();
            if (_debugging) { 
                _debug("Catch up: ending..." +
                    (localTime.add(getCurrentStepSize())));
            }
            _resolveInitialStates();
            if (_debugging) {
                _debug("Catch up one step: current time is" + localTime);
            }
        }
        if (_debugging) {
            _debug(getFullName() + " Catch up time" + localTime);
        }
    }

    /** Call the super class method. After that, check whether any events
     *  are produced at the end of this iteration. If so, ask the upper 
     *  level to schedule a firing to react the events generated as this 
     *  level. 
     */
    protected void _resolveInitialStates() throws IllegalActionException {
        super._resolveInitialStates();
        if (_isTopLevel()) {
            return;
        }
        // FIXME: why do we need the followings?
        // Check whether the iteration is interrupted by event.
        // If so, ask the upper level to schedule a firing to react to the
        // events generated at this level.
        Time localTime = getModelTime();
        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (_isStoppedByEvent()) {
            if (_debugging) {
                _debug("Fire stopped by event."
                        + " at " + localTime
                        + "; request refiring at "
                        + localTime
                        + "; set Event phase to TRUE");
            }
            _hasDiscreteEvents = true;
            //hold Outputs;
        } else if (localTime.equals(getIterationEndTime())) {
            if (_debugging) {
                _debug("Fire stopped normally."
                        + " at " + localTime
                        + "; request refiring at "
                        + getIterationEndTime()
                        + "; set Event phase to FALSE");
            }
            _hasDiscreteEvents = false;
        }
        // FIXME: why do we need to refire the container? 
        // should this go to the postfire method? 
        // This is related to the FIXME in the psotfire method.
        executiveDirector.fireAt(container, localTime);
        return;
    }
    
    /** Initialize parameters in addition to the parameters inherited
     *  from CTMultiSolverDirector. In this class the additional
     *  parameter is the maximum run ahead time length
     *  (<code>runAheadLength</code>). The default value is 1.0.
     */
    protected void _initParameters() {
        super._initParameters();
        try {
            _runAheadLength = 0.1;
            runAheadLength = new Parameter(this,
                    "runAheadLength", new DoubleToken(_runAheadLength));
            runAheadLength.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    // FIXME: this method is very confusing. Need a better way to 
    // handle events. The bottom line is that whenever an event is 
    // generated, requrest a refiring and let the upper level react
    // to the event.
    /** Return true if the current iteration is stopped due to
     *  the occurrence of events (predictable or unpredictable).
     *  @return True if the current fire phase is stopped by an event.
     *  @exception IllegalActionException If thrown by the scheduler.
     */
    protected boolean _isStoppedByEvent() throws IllegalActionException {
        // predictable breakpoints
        Time breakpoint;
        TotallyOrderedSet table = getBreakPoints();
        Time localTime = getModelTime();
        if (table != null) {
            while (!table.isEmpty()) {
                breakpoint = (Time)table.first();
                if (breakpoint.compareTo(localTime) < 0) {
                    // The breakpoints in the past.
                    // This should not happen. 
                    throw new InternalErrorException("The breakpoint " +
                            breakpoint + " is in the past.");
                } else if (breakpoint.equals(localTime) &&
                        breakpoint.compareTo(getIterationEndTime()) < 0) {
                    // FIXME: why the breakpoint needs to be smaller than
                    // the iteration ending time?
                    // break point now! stopped by event
                    return true;
                } else {
                    break;
                }
            }
        }
        // unpredictable breakpoints. Detect current events.
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        Iterator generators = schedule.get(
                CTSchedule.EVENT_GENERATORS).actorIterator();
        while (generators.hasNext()) {
            CTEventGenerator generator = (CTEventGenerator)generators.next();
            if (generator.hasCurrentEvent()) {
                return true;
            }
        }
        return false;
    }

    /**Return true if this is a top-level director. 
     * @return True if this director is at the top level.
     */
    protected final boolean _isTopLevel() {
        // This is a syntactic sugar.
        long version = workspace().getVersion();
        if (version == _mutationVersion) {
            return _isTop;
        }
        try {
            workspace().getReadAccess();
            CompositeActor container = (CompositeActor)getContainer();
            if (container.getExecutiveDirector() == null) {
                _isTop = true;
            } else {
                _isTop = false;
            }
            _mutationVersion = version;
        } finally {
            workspace().doneReading();
        }
        return _isTop;
    }

    /** Mark the current state as the known good state. Call the
     *  markStates() method on all CTStatefulActors. Save the current time
     *  as the "known good" time.
     *  @exception IllegalActionException If thrown by the scheduler.
     */
    protected void _markStates() throws IllegalActionException {
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATEFUL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if (_debugging) _debug("Save State..."+
                    ((Nameable)actor).getName());
            actor.markState();
        }
        _knownGoodTime = getModelTime();
    }

    /** Rollback the system to a "known good" state. All the actors with
     *  states are called to restore their saved states. The
     *  current time of the director is set to the time of the "known
     *  good" state.
     *  @exception IllegalActionException If thrown by the goToMarkedState()
     *  method of an actor, or the schedule does not exist.
     */
    protected void _rollback() throws IllegalActionException{
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATEFUL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if (_debugging) {
                _debug("Restore State..." + ((Nameable)actor).getName());
            }
            actor.goToMarkedState();
        }
        setModelTime(_knownGoodTime);
    }

    /** Set the end time for this iteration. If the argument is
     *  earlier than the current time, then an InvalidStateException
     *  will be thrown.
     *  @param time The fire end time.
     */
    protected void _setIterationEndTime(Time time) {
        if (time.compareTo(getModelTime()) < 0) {
            throw new InvalidStateException(this,
                    " Iteration end time" + time + " is earlier than" +
                    " the current time." + getModelTime());
        }
        super._setIterationEndTime(time);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The number of rollbacks. Used for statistics.
     */
    // FIXME: not used anywhere.
    protected int _Nroll = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the time of the outside domain. If this is the top-level
     *  director return the current time.
     *  @return The outside current time.
     */
    private Time _getOutsideTime() {
        if (_isTopLevel()) {
            return getModelTime();
        }
        return _outsideTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicate whether there are pending discrete events.
    private boolean _hasDiscreteEvents;

    // Indicate if this is the top level director.
    private boolean _isTop;

    // The time for the "known good" state.
    private Time _knownGoodTime;

    // The version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // The current outside time.
    private Time _outsideTime;

    // The local variable of the run ahead length parameter.
    private double _runAheadLength;

    // Indicate whether this is the second time that prefire has been
    // called in a row.
    private boolean _secondPrefire = false;
}
