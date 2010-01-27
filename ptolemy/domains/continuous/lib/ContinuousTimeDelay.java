/* An actor that delays the input by the specified amount.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.domains.continuous.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.CausalityMarker;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.AbsentToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ContinuousTimeDelay

/**
 Delay the input by a specified amount of time.

 <p>This actor is designed
 to be used in timed domains such as DE and Continuous. It can also be used
 in other domains, such as SR and SDF, but this will only be useful if the
 delay value is a multiple of the period of those directors. The amount
 of the delay is required to be non-negative and has a default value 1.0.
 The input and output types are unconstrained, except that the output type
 must be the same as that of the input.
 <p>
 This actor keeps a local FIFO queue of all input events that may be requested
 by the director; an event expires and is removed from this queue if its
 timestamp is older than the current time less the delay. The behavior of this
 actor on each firing is to read a token from the input port (if present) and
 generates an output that is either equal to or an approximation of the delayed
 input signal. Output is absent if and only if no initial value is given and
 the actor is fired before input is received, or before the transient delay
 period has passed (i.e. model time is less than delay time).
 <p>
 Output is generated by the fire() method, and inputs are processed in postFire().
 <p> 
 Occasionally, this actor is useful with the
 delay parameter set to 0.0.  The time stamp of the output will
 equal that of the input, but there is a "microstep" delay.
 The continuous domain in Ptolemy II has a "super dense" model
 of time, meaning that a signal from one actor to another can
 contain multiple events with the same time stamp. These events
 are "simultaneous," but nonetheless
 have a well-defined sequential ordering determined by the order
 in which they are produced.
 If \textit{delay} is 0.0, then the actor does not generate output
 in the current time microstep, but rather on a refiring at the
 the same physical time but incremented timestep.
 <p>
 A consequence of this strategy is that this actor is
 able to produce an output (or assert that there is no output) before the
 input with the same time is known.   Hence, it can be used to break
 causality loops in feedback systems. The Continuous director will leverage this when
 determining the fixed point behavior. It is sometimes useful to think
 of this zero-valued delay as an infinitesimal delay.

 @author Edward A. Lee, Jeff C. Jensen
 @version $Id: ContinuousTimeDelay.java 55615 2009-08-27 22:32:34Z cxh $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ContinuousTimeDelay extends Transformer {
    // FIXME: delay cannot change during a run (more
    // precisely... ignored until next initialize()).
  
    // FIXME: implement solver step size control to capture periods of
    // fine granularity; without this, though input events were stored
    // with the same resolution as the solver deemed necessary, these
    // delayed input events may be sampled sparsely if no other actor
    // requires increased resolution at output time. If this actor
    // uses fireAt() to force the solver to sample with the same
    // resolution at which the input was generated, then subsequent
    // inputs to the delay actor will arrive with this frequency
    // regardless of whether or not this resolution is necessary.
  

    /** Construct an actor with the specified container and name.
     *  Constrain that the output type to be the same as the input type.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ContinuousTimeDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1.0");
        _delay = 1.0;

        output.setTypeSameAs(input);
        
        // empty set of dependent ports.
        Set<Port> dependentPorts = new HashSet<Port>();
        _causalityMarker = new CausalityMarker(this, "causalityMarker");
        _causalityMarker.addDependentPortSet(dependentPorts);

        initialOutput = new Parameter(this, "initialOutput");
        initialOutput.setExpression(null);

        //FIXME - this causes an exception if initialDelay = NULL
        //output.setTypeAtLeast(initialOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay. The default for this parameter is 1.0. This
     * parameter must contain a DoubleToken with a non-negative value, or an
     * exception will be thrown when it is set.
     */
    public Parameter delay;

    /** Initial output of the delay actor. The default for this parameter
     *  is null, indicating no output will be generated until after input
     *  has been received.
     */
    public Parameter initialOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then ensure that the value
     *  is non-negative.
     *  <p>NOTE: the newDelay may be 0.0, which may change the causality
     *  property of the model. We leave the model designers to decide
     *  whether the zero delay is really what they want.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay) {
            double newDelay = ((DoubleToken) (delay.getToken())).doubleValue();

            if (newDelay < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative delay: " + newDelay);
            } else {
                _delay = newDelay;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ContinuousTimeDelay newObject = (ContinuousTimeDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        newObject._causalityMarker = (CausalityMarker)newObject.getAttribute("causalityMarker");
        return newObject;
    }

    /** Initialize the states of this actor. Place initial output
     *  token on the input buffer.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        Token initialToken = initialOutput.getToken();
        super.initialize();
        _currentOutput = null;
        _inputBuffer = new CalendarQueue(new TimedEvent.TimeComparator());
        _discarded = null;
        _nextFireAt = new Time(getDirector(), 0);

        //Place the initial token in the input buffer
        if (initialToken != null){
            Time modelStartTime = getDirector().getModelStartTime();
            _inputBuffer.put(new TimedEvent(modelStartTime, initialToken));
        }
    }

    /*
     * Consume input (if available) and produce output for this actor, either
     * by using the initial value (during transient behavior), or finding or
     * interpolating the delayed signal from input buffers.
     * 
     * The goal is to determine the input signal at the current time less delay.
     * We refer to this point as the center point. If the center point occurs
     * during the transient period of this actor and an initial value is
     * present, then output the initial value. Otherwise, if the input buffer
     * contains the center point, we simply output the recorded token and
     * discard it from the input buffer.
     * 
     * If the center point was not recorded in the input buffer, then find the
     * most recent event that occurred before the center point, and label it the
     * left point. Similarly, we search for the nearest point of our input
     * buffer that occurred after the center point, and label it the right
     * point.
     * 
     * By discarding expired tokens (tokens which will no longer be requested by
     * the director), the center point falls between the most recently discarded
     * event and the first element of the input buffer. Hence the left point is
     * the most recently discarded event, and the right point is the first
     * element of the input buffer.
     * 
     * If the time of the center point is equal to the current time, then the
     * input is delayed by a microstep. The right point retains the input value
     * and is output on a refire at the same physical time. The left point is
     * ignored.
     * 
     * @exception IllegalActionException
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        Time currentTime = getDirector().getModelTime();
        Time centerTime = currentTime.subtract(_delay);
        TimedEvent leftEvent = null;
        TimedEvent rightEvent = null;
        _currentOutput = null;
        
        // Consume input; if input is absent, do not add an event to the output queue,
        // as it will force the scheduler to fire this actor to produce an absent output.
        // Otherwise, the solver step size is prevented from increasing as delayed
        // absent events are present everywhere in the signal. This would result in
        // monotonically decreasing solver step size that quickly converges to the minimum
        // allowed step size, effectively bypassing the solver logic and slowing simulation.
        if (input.isKnown(0) && input.hasToken(0)){
            Token inputToken = input.get(0);
            if (!inputToken.equals(AbsentToken.ABSENT)){
                _inputBuffer.put(new TimedEvent(currentTime, inputToken));
            }
            else {
                inputToken = null;
            }
        }

        // Discard expired input events that will never be considered
        // by the solver. These are events that have timestamps before
        // the current time less delay.
        while (_inputBuffer.size() > 0){
            Time earliestEventTime = ((TimedEvent)_inputBuffer.get()).timeStamp;   

            //Expired event
            if (earliestEventTime.compareTo(centerTime) < 0){
                _discarded = (TimedEvent) _inputBuffer.take();
            }
            //Earliest event is valid, so stop searching
            else {
                break;
            }
        }
        
        //Record the left event; this is the most recently discarded input token.
        // Note that if we have not seen input, but we have an initial value, the
        // value was put to the input queue in the initialize() method, so the left
        // point will be the initial value at time 0.
        leftEvent = _discarded;
        
        //Record the right event; because expired events are discarded from the
        // input queue, the right event is always the first element of the queue
        if (_inputBuffer.size() > 0){
            rightEvent = (TimedEvent) _inputBuffer.get();
        }
        
        // If the input signal was recorded at the center point, output it here,
        // and remove the event from the input queue to prevent refiring
        if (rightEvent != null && rightEvent.timeStamp.equals(centerTime)){
            _currentOutput = (Token)rightEvent.contents;
            _discarded = (TimedEvent) _inputBuffer.take();
        }
        // If the current time is less than the delay time, output the initial value
        else if (currentTime.compareTo(new Time(getDirector(), _delay)) < 0){
            _currentOutput = initialOutput.getToken();
        }
        // If the current time is equal to the center time (delay=0), but the event was
        // not on the input queue, then we have not read the input. Output nothing now,
        // and postFire() will read the input and request a refiring at the current time;
        // this will force the director to increase its microstep.
        else if (currentTime.equals(centerTime)){
            //Do nothing
        }
        // If we have a left point, we construct the center point here
        else if (leftEvent != null){
            //If we have a right point, then we interpolate the center point
            if (rightEvent != null){
                _currentOutput = linearInterpolate(leftEvent, rightEvent);
            }
            // If we have a left point but no right point, we assume the value has not changed.
            else {
                //FIXME: Is this the best solution?
                _currentOutput = (Token) leftEvent.contents;
            }
        }
        // Otherwise, we did not record the event at the center time, have not seen input,
        // and do not have a left point (e.g. no initial value). We cannot generate output.
        
        // Produce output
        if (_currentOutput != null && !output.isKnown(0)){
            output.send(0, _currentOutput);
            // In the case where delay is zero (currentTime = centerTime) we have refired in
            // order to output a token with an increased microstep. After this token is sent,
            // it needs to be removed from the input buffer.
            if (currentTime.equals(centerTime)){
                _discarded = (TimedEvent)_inputBuffer.take();
            }
        }
        
        //FIXME: What happens if two events in input buffer
        // have the same physical timestamp? We need to interpolate
        // earlier points by using the matching timestamp,
        // and then output both events at some point.
        // How does this affect the left point in interpolation?
        //
        // To maintain continuity principles, the current implementation
        // may be correct; the events in the input buffer are ordered
        // by microstep index, so that in interpolation, the event with
        // the earliest time index is always used. This will preserve
        // left continuity of an input signal.
    }

    /** Schedule the next output event.
     *  @exception IllegalActionException 
     */
    public boolean postfire() throws IllegalActionException {
        // Schedule the next output event. This event may fire at the same
        // physical time in the case of zero delay or simultaneous events.
        if (_inputBuffer.size() > 0){
            TimedEvent nextEvent = (TimedEvent)_inputBuffer.get();
            Time nextOutputTime = nextEvent.timeStamp.add(_delay);
            Time currentTime = getDirector().getModelTime();

            // If the next output time is now, then there are additional tokens to output at the
            // current physical time, so a refire is requested. If the next output time is in the
            // future, first ensure we have not already scheduled this actor to fire. 
            if (nextOutputTime.equals(currentTime) || !nextOutputTime.equals(_nextFireAt)){
                getDirector().fireAt(this, nextOutputTime);
                _nextFireAt = nextOutputTime;
            }
        }

        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        declareDelayDependency(input, output, _delay);
    }
    
    /** Override the base class to declare that the actor is nonstrict
     *  if it has an initial value token.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean isStrict(){
//        //FIXME: Does strictness depend on presence of an initial value?
  //      return false;
        try {
            Token t = initialOutput.getToken(); 
            return t == null;
        } catch (IllegalActionException e) {
            return true;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** 
     * Linear interpolate between previous and current input.
     * 
     * <p>To interpolate, we determine the slope between the left and right
     * interpolation points, and multiply this by time gap between the left
     * point and the current time. This estimates the amount by which the input
     * signal has changed between the left and center points. We add this change
     * to the value of the left point to estimate the value of the center point.
     * 
     * @param leftEvent The left event.
     * @param rightEvent the right event.
     * @return The linear interpolation.
     * @exception IllegalActionException If thrown by arithmetic operations
     * on the events
     */
    protected Token linearInterpolate(TimedEvent leftEvent, TimedEvent rightEvent) 
            throws IllegalActionException {
        Time centerTime = getDirector().getModelTime().subtract(_delay);
        
        //time gap (run) between left and right events
        Token slope = new DoubleToken(rightEvent.timeStamp.subtract(leftEvent.timeStamp).getDoubleValue());
        
        //slope = rise / run
        slope = ((Token)rightEvent.contents).subtract((Token)leftEvent.contents).divide(slope);
        
        //leftEvent + estimated rise from leftEvent to centerEvent
        return ((Token)leftEvent.contents).add(slope.multiply(new DoubleToken(centerTime.subtract(leftEvent.timeStamp).getDoubleValue())));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Current output. */
    protected Token _currentOutput;
    
    /** The amount of delay. */
    protected double _delay;

    /** A local event queue to store input tokens, sorted by input time. */
    protected CalendarQueue _inputBuffer;
    
    /** Holds the most recently discarded event from the input buffer. */
    protected TimedEvent _discarded;
    
    /** Records the next scheduled fireAt() call, so that we do not request more than
     *  one fireAt() call for a given input event. 
     */
    protected Time _nextFireAt;

    /** A causality marker to store information about how pure events are causally
     *  related to trigger events.
     */
    protected CausalityMarker _causalityMarker;
}
