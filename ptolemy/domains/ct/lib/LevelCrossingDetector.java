/* A CT actor that detects level crossings of its trigger input signal.

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

package ptolemy.domains.ct.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LevelCrossingDetector
/**
   An event detector that converts continuous signals to discrete events when
   the continuous signal crosses a level threshold.
   <p>
   When the <i>trigger</i> equals to the level threshold (within the specified
   <i>errorTolerance</i>), this actor outputs a discrete event with the value as
   <i>defaultEventValue</i> if <i>useEventValue</i> is selected. Otherwise, the
   actor outputs a discrete event with the value as the level threshold.
   <p>
   This actor controls the step size such that level crossings never
   occur during an integration. So, this actor is only used in Continuous-Time
   domain.

   @author Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class LevelCrossingDetector extends Transformer
    implements CTStepSizeControlActor, CTEventGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public LevelCrossingDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Parameter(input, "signalType", new StringToken("CONTINUOUS"));

        output.setTypeAtLeast(input);
        new Parameter(output, "signalType", new StringToken("DISCRETE"));

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        new Parameter(trigger, "signalType", new StringToken("CONTINUOUS"));

        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        defaultEventValue = new Parameter(this, "defaultEventValue",
                new DoubleToken(0.0));
        output.setTypeAtLeast(defaultEventValue);

        usingDefaultEventValue = new Parameter(this, "usingDefaultEventValue");
        usingDefaultEventValue.setTypeEquals(BaseType.BOOLEAN);
        usingDefaultEventValue.setToken(BooleanToken.FALSE);

        _errorTolerance = (double)1e-4;
        errorTolerance = new Parameter(this, "errorTolerance",
                new DoubleToken(_errorTolerance));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A parameter that specifies the value of output events
     *  if the <i>useEventValue</i> parameter is checked. By default,
     *  it contains a DoubleToken of 0.0.
     */
    public Parameter defaultEventValue;

    /** The parameter of error tolerance of type double. By default,
     *  it contains a DoubleToken of 1e-4.
     */
    public Parameter errorTolerance;

    /** The parameter that specifies the level threshold. By default, it
     *  contains a DoubleToken of value 0.0. Note, a change of this
     *  parameter at run time will not be applied until the next
     *  iteration.
     */
    public Parameter level;

    /** The trigger port. Single port with type double.
     */
    public TypedIOPort trigger;

    /** The parameter that indicates whether to use the default event
     *  value.
     */
    public Parameter usingDefaultEventValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>errorTolerance</i> or <i>level</i>, then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException{
        if (attribute == errorTolerance) {
            double tolerance = ((DoubleToken)errorTolerance.getToken()
                        ).doubleValue();
            if (tolerance <= 0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }
            _errorTolerance = tolerance;
        } else if (attribute == level) {
            _level = ((DoubleToken)level.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        LevelCrossingDetector newObject = (LevelCrossingDetector)
            super.clone(workspace);
        // Set the type constraints.
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.defaultEventValue);
        return newObject;
    }

    /** Produce a discrete event if level crossing happens. If the current
     *  execution is in a continuous phase, the current trigger is recorded but
     *  no event can be produced. If the current execution is in a discrete
     *  phase, the current and previous trigger tokens are compared to find
     *  whether a level crossing happens. If there is a crossing, a discrete
     *  event is generated.
     *  <p>
     *  The value of this event may be the specified level, or the default
     *  event value if the usingDefaultEventValue is configured true (checked).
     *  @exception IllegalActionException If can not get token from the trigger
     *  port or can not send token through the output port.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        //record the input.
        _thisTrigger = ((DoubleToken) trigger.get(0)).doubleValue();

        if (_levelCrossingDetectionDisabled) {
            _lastTrigger = _thisTrigger;
        }

        if (_debugging) {
            _debug("Consuming a trigger token: " + _thisTrigger);
        }

        CTDirector director = (CTDirector)getDirector();
        if (director.getExecutionPhase() ==
            CTExecutionPhase.GENERATING_EVENTS_PHASE) {
            if (_debugging && _verbose) {
                _debug("This is a discrete phase execution.");
            }
            // There are two conditions when an event is generated.
            // 1. There is a discontinuity at the current time; OR
            // 2. By linear interpolation, an event is located at the current
            // time.
            if (((_lastTrigger - _level) * (_thisTrigger - _level) < 0.0)
                || hasCurrentEvent()) {
                // Emit an event.
                if (((BooleanToken)usingDefaultEventValue.getToken()).booleanValue()) {
                    output.send(0, defaultEventValue.getToken());
                    if (_debugging) {
                        _debug("Emitting an event with a default value: "
                            + defaultEventValue.getToken());
                    }
                } else {
                    output.send(0, new DoubleToken(_level));
                    if (_debugging) {
                        _debug("Emitting an event with the level value: "
                            + _level);
                    }
                }
                _eventNow = false;
                _eventMissed = false;
            }
        }
    }

    /** Return true if there is an event at the current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        return _eventNow;
    }

    /** Initialize the execution.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _eventMissed = false;
        _eventNow = false;
        _level = ((DoubleToken)level.getToken()).doubleValue();;
        _levelCrossingDetectionDisabled = true;
        // Note that _lastTrigger and _thisTrigger are not initialized.
        // Instead, they will be assigned some values at the first firing.
    }

    /** Return true if there is no event detected during the current step size.
     *  @return true if there is no event detected in the current iteration.
     */
    public boolean isOutputAccurate() {
        if (_levelCrossingDetectionDisabled) {
            if (_debugging && _verbose) {
                _debug("First firing of this actor, " +
                        "the output is always accurate. " +
                        "Note that if the initial step size is too big, " +
                        "an event may be missing.");
            }
            _levelCrossingDetectionDisabled = false;
            _eventNow = false;
            _eventMissed = false;
            return !_eventMissed;
        }

        if (_debugging && _verbose) {
            _debug("The last trigger is " + _lastTrigger);
            _debug("The current trigger is " + _thisTrigger);
        }
        // If the level is crossed and the current trigger is very close
        // to the level, the current step size is accurate.
        // If the current trigger is equal to the level threshold, the current
        // step size is accurate.
        // Otherwise, the current step size is too big.
        if ((_lastTrigger - _level) * (_thisTrigger - _level) < 0.0) {
            // Preinitialize method ensures the cast to be safe.
            CTDirector director = (CTDirector)getDirector();
            if (Math.abs(_thisTrigger - _level) < _errorTolerance) {
                // The current time is when the event happens.
                if (_debugging)
                    _debug("Event is detected at "
                            + getDirector().getModelTime());
                _eventNow = true;
                _eventMissed = false;
            } else {
                _eventNow = false;
                _eventMissed = true;
            }
        } else if (_thisTrigger == _level) {
            _eventNow = true;
            _eventMissed = false;
        } else {
            _eventNow = false;
            _eventMissed = false;
        }
        return !_eventMissed;
    }

    /** Always return true because this actor is not involved
     *  in resolving states.
     *  @return true.
     */
    public boolean isStateAccurate() {
         return true;
    }

    /** Return true if this step does not cross the threshold.
     *  The current trigger
     *  token will be compared to the previous trigger token. If they
     *  cross the level threshold, this step is not accurate.
     *  A special case is taken care so that if the previous trigger
     *  and the current trigger both equal to the level value,
     *  then no new event is
     *  detected. If this step crosses the level threshold,
     *  then the refined integration
     *  step size is computed by linear interpolation.
     *  If this is the first iteration after initialize() is called,
     *  then always return true, since there is no history to compare with.
     *  @return True if the trigger input in this integration step
     *          does not cross the level threshold.
     */
    public boolean isThisStepAccurate() {
        return isOutputAccurate() && isStateAccurate();
    }

    /** Prepare for the next iteration, by making the current trigger
     *  token to be the history trigger token.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException {
        _lastTrigger = _thisTrigger;
        return super.postfire();
    }

    /** Return the maximum Double, since this actor does not predict
     *  step size.
     *  @return java.Double.MAX_VALUE.
     */
    public double predictedStepSize() {
        return java.lang.Double.MAX_VALUE;
    }

    /** Make sure the actor runs inside a CT domain.
     *  @exception IllegalActionException If the director is not
     *  a CTDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof CTDirector)) {
            throw new IllegalActionException("LevelCrossingDetector can only" +
                    " be used inside CT domain.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if there is a missed event,
     *  otherwise return the current step size.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        CTDirector dir = (CTDirector)getDirector();
        double refinedStep = dir.getCurrentStepSize();
        if (_eventMissed) {
            // The refined step size is a linear interpolation.
            refinedStep = (Math.abs(_lastTrigger - _level)
                * dir.getCurrentStepSize())
                / Math.abs(_thisTrigger-_lastTrigger);
            if (_debugging) {
                _debug(getFullName() +
                    " Event Missed: refined step at" +  refinedStep);
            }
        }
        return refinedStep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The level threshold this actor detects.
     */
    // The variable is proetected because ZeroCrossingDetector needs access
    // to this variable.
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Parameter, the error tolerance, local copy
    private double _errorTolerance;

    // flag for indicating a missed event
    private boolean _eventMissed = false;

    // flag indicating if there is an event at the current time.
    private boolean _eventNow = false;

    // flag indicating that level-crossing detection is disabled
    // due to the lack of history information
    private boolean _levelCrossingDetectionDisabled = true;

    // last trigger input.
    private double _lastTrigger;

    // this trigger input.
    private double _thisTrigger;
 }
