/* Generate discrete events by periodically sampling a CT signal.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.Transformer;


//////////////////////////////////////////////////////////////////////////
//// CTPeriodicSampler
/**
This actor periodically sample the input signal and generate events
which has the value of the input signal. The sampling rate is given by
parameter "samplePeriod", which has default value 0.1.
The actor has a multi-input port and a multi-output port. Signals in
each input channel are sampled and produced to corresponding output
channel.
@author Jie Liu
@version $Id$
*/
public class CTPeriodicSampler extends Transformer
    implements CTEventGenerator, TimedActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param CompositeActor The subsystem that this actor is lived in
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
        Parameter inputType = new Parameter(input, "signalType",
                new StringToken("CONTINUOUS"));
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        Parameter outputType = new Parameter(output, "signalType",
                new StringToken("DISCRETE"));
        _samplePeriod = (double)0.1;
        samplePeriod = new Parameter(this,
                "samplePeriod", new DoubleToken(_samplePeriod));
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
            if(p <= 0) {
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
        if (director.isDiscretePhase() && _hasCurrentEvent) {
            for (int i = 0;
                 i < Math.min(input.getWidth(), output.getWidth());
                 i++) {
                if(input.hasToken(i)) {
                    output.send(i, input.get(i));
                }
            }
            _hasCurrentEvent = false;
            // register for the next event.
            _nextSamplingTime += _samplePeriod;
            getDirector().fireAt(this, _nextSamplingTime);
            if (_debugging) _debug(getFullName(), " produces event at: "
                    + director.getCurrentTime());
        }
    }

    /** Return true if there is a current event.
     *  @return If there is a discrete event to emit.
     */
    public boolean hasCurrentEvent() {
        CTDirector director = (CTDirector)getDirector();
        if(Math.abs(director.getCurrentTime() - _nextSamplingTime)
                < director.getTimeResolution() ) {
            _hasCurrentEvent = true;
        } else {
            _hasCurrentEvent = false;
        }
        if(_debugging) _debug(getFullName(), " has event at: "
                + director.getCurrentTime() + " is " + _hasCurrentEvent );
        return _hasCurrentEvent;
    }

    /** Request the first sampling time by calling director.fireAt().
     *  @exception IllegalActionException If thrown by the supper class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // clear receivers
        for (int i = 0; i < Math.min(input.getWidth(), output.getWidth());
             i++) {
            if(input.hasToken(i)) {
                input.get(i);
            }
        }
        _hasCurrentEvent = false;
        CTDirector dir = (CTDirector) getDirector();
        _nextSamplingTime = dir.getCurrentTime();
        dir.fireAt(this, dir.getCurrentTime());
        if(_debugging) _debug(getFullName() + ": next sampling time = "
                + _nextSamplingTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the local copy of the sample period.
    private double _samplePeriod;

    // flag indicating if there is a current event.
    private boolean _hasCurrentEvent = false;

    // the next sampling time.
    private double _nextSamplingTime;
}
