/* Generate discrete events by periodically sampling a CT signal.

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

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CTPeriodicSampler
/**
   Generate discrete events by periodically sampling a CT signal.

   <p>This actor periodically sample the input signal and generate events
   which has the value of the input signal. The sampling rate is given by
   parameter "samplePeriod", which has default value 0.1.
   The actor has a multi-input port and a multi-output port. Signals in
   each input channel are sampled and produced to corresponding output
   channel.
   @author Jie Liu
   @version $Id$
   @since Ptolemy II 0.3
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (cxh)
*/
public class CTPeriodicSampler extends Transformer
    implements CTEventGenerator {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param container The container of this actor.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public CTPeriodicSampler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        new Parameter(input, "signalType",
                new StringToken("CONTINUOUS"));
        output.setMultiport(true);
        new Parameter(output, "signalType",
                new StringToken("DISCRETE"));
        _samplePeriod = (double)0.1;
        samplePeriod = new Parameter(this,
                "samplePeriod", new DoubleToken(_samplePeriod));

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,0 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n"
                + "</svg>\n");
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The parameter for the sampling period; the type is double; the
     *  default value is 1.0.
     */
    public Parameter samplePeriod;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the local cache of the sampling period if it has been changed.
     *  @exception IllegalActionException If the sampling period is
     *  less than or equal to 0.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException{
        if (attribute == samplePeriod) {
            double p = ((DoubleToken)samplePeriod.getToken()).doubleValue();
            if (p <= 0) {
                throw new IllegalActionException(this,
                        " Sample period must be greater than 0.");
            } else {
                _samplePeriod = p;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Emit the current event if there is one. The value of the event
     *  is the sample of the input signal.
     *  @exception IllegalActionException If the transfer of tokens failed.
     */
    public void fire() throws IllegalActionException {
        CTDirector director = (CTDirector)getDirector();
        if (((director.getExecutionPhase() == 
            CTExecutionPhase.FIRINGEVENTGENERATORS_PHASE)  
            || (director.getExecutionPhase() == 
            CTExecutionPhase.GENERATINGEVENTS_PHASE))
            && hasCurrentEvent()) {
//            for (int i = 0;
//                 i < Math.min(input.getWidth(), output.getWidth());
//                 i++) {
//                if (input.hasToken(i)) {
//                    Token token = input.get(i);
//                    output.send(i, token);
//                    if (_debugging) 
//                        _debug(getFullName(), " sends event: " + token
//                            + " to channel " + i  
//                            + ", at: " + getDirector().getModelTime());
//                }
//            }
        }
    }

    /** Return true if there is a current event.
     *  @return If there is a discrete event to emit.
     */
    public boolean hasCurrentEvent() {
        CTDirector director = (CTDirector)getDirector();
        // This actor may be fired several times at a discrete phase
        // execution. By checking whether the 
        if (director.getModelTime().compareTo(_nextSamplingTime) == 0) {
            _hasCurrentEvent = true;
        } else {
            _hasCurrentEvent = false;
        }
        if (_debugging) _debug(getFullName(), " has event at: "
                + director.getModelTime() + " is " + _hasCurrentEvent );
        return _hasCurrentEvent;
    }

    /** Set the next sampling time as the start time (i.e. the current time).
     *  We do not register the start time as a breakpoint, since the
     *  director will fire at the start time any way.
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        CTDirector dir = (CTDirector) getDirector();
        _nextSamplingTime = dir.getModelTime();
        if (_debugging) _debug(getFullName() + ": next sampling time = "
                + _nextSamplingTime);
    }

    /** Set the next sampling time and return true.
     *  It computes the next sampling time,
     *  and registers it as a breakpoint.
     *  @return True.
     */
    public boolean postfire() throws IllegalActionException {
        CTDirector director = (CTDirector)getDirector();
        if (((director.getExecutionPhase() == 
            CTExecutionPhase.POSTFIRINGEVENTGENERATORS_PHASE)  
            || (director.getExecutionPhase() == 
            CTExecutionPhase.GENERATINGEVENTS_PHASE))
            && hasCurrentEvent()) {
            // Generate discrete events. 
            // NOTE: this is unusual since most actors produce
            // outputs at their fire method. The reason for 
            // producing output in the postfire method is that
            // producing an event is a state change. It must
            // happen when the event should happen.
            // NOTE: It is arguable to defer the production of
            // event till the discrete phase execution. However,
            // if we do so, we lose the desired behavior of this
            // sampler, which is to only sample values at t_minus
            // but not any values at t or t_plus.
            for (int i = 0; i < Math.min(input.getWidth(), output.getWidth()); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    output.send(i, token);
                    if (_debugging)
                        _debug(getFullName(), " sends event: " + token
                                + " to channel " + i + ", at: "
                                + getDirector().getModelTime());
                }
            }
            // discrete events are generated; schedule another
            // discrete phase execution at the current time
            // such that other discrete actors react to the events.
            director.fireAt(null, director.getModelTime());
            // register for the next event.
            _nextSamplingTime = _nextSamplingTime.add(_samplePeriod);
            if (_debugging)
                _debug(getFullName(), "request fire at "
                        + _nextSamplingTime);
            getDirector().fireAt(this, _nextSamplingTime);
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the local copy of the sample period.
    private double _samplePeriod;

    // flag indicating if there is a current event. 
    // NOTE: this variable should be only used inside the hasCurrentEvent
    // method. Other methods can only access the status of this variable
    // via the hasCurrentEvent method.
    private boolean _hasCurrentEvent = false;

    // the next sampling time.
    private Time _nextSamplingTime;
}
