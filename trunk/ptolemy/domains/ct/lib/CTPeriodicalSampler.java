/* One line description of file.

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
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CTPeriodicalSampler
/**
This actor periodically sample the input signal and generate an event
which has the value of the input signal.
@author Jie Liu
@version $Id$
*/
public class CTPeriodicalSampler extends TypedAtomicActor
        implements CTEventGenerator {

    public static final boolean DEBUG = false;

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
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */
    public CTPeriodicalSampler(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setMultiport(false);
        input.setInput(true);
        input.setOutput(false);
        input.setDeclaredType(DoubleToken.class);
        output = new TypedIOPort(this, "output");
        output.setMultiport(false);
        output.setInput(false);
        output.setOutput(true);
        output.setDeclaredType(DoubleToken.class);

        _samplePeriod = (double)1.0;
        _paramSamplePeriod = new Parameter(this,
            "SamplePeriod", new DoubleToken(_samplePeriod));
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    /** prefire. If the current time is greater than the next sample
     *  time, increase the next sample time until it is 
     *  greater than the current time.
     */
    public boolean prefire() throws IllegalActionException {
        /*
        if(!(getDirector() instanceof CTMixedSignalDirector)) {
            throw new IllegalActionException(this,
                " Must be executed after a CTMixedSignalDirector.");
        }
        */
        updateParameters();
        CTDirector dir = (CTDirector) getDirector();
        boolean hasjump = false;
        while (_nextSamplingTime < 
                (dir.getCurrentTime()-dir.getTimeResolution())) {
            hasjump = true;
            _nextSamplingTime += _samplePeriod;
        }
        if(hasjump) {
            dir.fireAt(this, _nextSamplingTime);
        }
        if(DEBUG) {
            System.out.println("Sampler: next sampling time= "
                + _nextSamplingTime);
        }
        return true;
    }
    /** Fire: if the current time is the event time, request the end
     *  of this fire.
     */
    public void fire() throws IllegalActionException {
        CTDirector dir = (CTDirector)getDirector();
        double tnow = dir.getCurrentTime();
         _hasCurrentEvent = false;
        if(Math.abs(tnow - _nextSamplingTime)<dir.getTimeResolution()) {
            _hasCurrentEvent = true;
        }
    }

    /** Postfire: if this is the sampling point, output a token with the
     *  input signal as the value. Otherwise output no token.
     *  register the next sampling time as the next break point.
     */
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    /** Initilaize, chech if the director is a CTMixedSignalDirector.
     *  If the director is not a CTMixedSignalDirector throw an exception.
     */
    public void initialize() throws IllegalActionException {
        //if(!(getDirector() instanceof CTMixedSignalDirector)) {
        //    throw new IllegalActionException(this,
        //        " Must be executed after a CTMixedSignalDirector.");
        //}
        updateParameters();
        CTDirector dir = (CTDirector) getDirector();
        _nextSamplingTime = dir.getCurrentTime() + _samplePeriod;
        dir.fireAt(this, _nextSamplingTime);
        if(DEBUG) {
             System.out.println("Sampler: next sampling time= "
                + _nextSamplingTime);
        }
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     */
    public void updateParameters() throws IllegalActionException{
        double p = ((DoubleToken)_paramSamplePeriod.getToken()).doubleValue();
        if(p <= 0) {
            throw new IllegalActionException(this,
                    " Sample period must be greater than 0.");
        }
        _samplePeriod = p;
    }

    /** Return true if there is defintly an event missed in the
     *  last step.
     */
    public boolean hasCurrentEvent() {
        return _hasCurrentEvent;
    }

    /** If there is a missed event return the expected sample time
     *  - current time; else return the current step size.
     */
    public void emitCurrentEvents() {
        CTDirector dir = (CTDirector)getDirector();
        if(_hasCurrentEvent) {
            try {
                if(input.hasToken(0)) {
                    output.broadcast(input.get(0));
                    _hasCurrentEvent = false;
                }
            }catch (IllegalActionException e) {
                throw new InvalidStateException("No input Token.");
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
    public TypedIOPort input;
    public TypedIOPort output;

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////



    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Parameter, the sample period.
    private Parameter _paramSamplePeriod;
    private double _samplePeriod;
    private double _eventTime;
    private boolean _hasCurrentEvent = false;
    private double _refineStep;
    private double _nextSamplingTime;
}
