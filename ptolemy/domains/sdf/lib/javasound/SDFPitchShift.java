/* An actor that performs pitch shifting on an audio signal.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.domains.sdf.lib.javasound;

import ptolemy.domains.sdf.test.pitchshift.*;
import ptolemy.math.*;
import ptolemy.math.SignalProcessing;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import ptolemy.data.Token;
import ptolemy.data.type.BaseType;

//import javax.media.sound.sampled.*;

import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFPitchShift
/**


@author Brian K. Vogel
@version
*/

public class SDFPitchShift extends SDFAtomicActor {

    /**
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFPitchShift(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	consumptionProductionRate = new Parameter(this,
                "consumptionProductionRate",
                new IntToken(512));
	consumptionProductionRate.setTypeEquals(BaseType.INT);
	consumptionRate =
	    ((IntToken)consumptionProductionRate.getToken()).intValue();
	productionRate = consumptionRate;

	output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
	output.setTokenProductionRate(productionRate);

	input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
	input.setTokenConsumptionRate(consumptionRate);

	scaleFactor = new SDFIOPort(this, "scaleFactor", true, false);
        scaleFactor.setTypeEquals(BaseType.DOUBLE);
	scaleFactor.setTokenConsumptionRate(1);

	pitchIn = new SDFIOPort(this, "pitchIn", true, false);
        pitchIn.setTypeEquals(BaseType.DOUBLE);
	pitchIn.setTokenConsumptionRate(consumptionRate);

	sampleRate = new Parameter(this, "sampleRate",
                new DoubleToken(22050));
        sampleRate.setTypeEquals(BaseType.DOUBLE);


    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port for audio data. */
    public SDFIOPort input;

    /** The pitch value input port. */
    public SDFIOPort pitchIn;

    /** The pitch scale factor input port. */
    public SDFIOPort scaleFactor;

    /** The output port. */
    public SDFIOPort output;

    /** The sampling rate to use, in Hz. The default value is
     * 22050.
     */
    public Parameter sampleRate;

    /** Set the token consumption rate and production rate to use (they
     * are equal). This only effects execution speed. Large values may result
     * in faster execution at the expense of larger channel buffers.
     * The default value is 512.
     */
    public Parameter consumptionProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the sample value of the sound file corresponding to the
     *  current index.
     */
    public void fire() throws IllegalActionException {


        input.getArray(0, audioTokenArray);
	pitchIn.getArray(0, pitchTokenArray);
	scaleFactor.getArray(0, scaleFactorTokenArray);

        int i;
        for (i = 0; i < consumptionRate; i++) {
            audioInDoubleArray[i] = audioTokenArray[i].doubleValue();

            pitchInDoubleArray[i] = pitchTokenArray[i].doubleValue();


        }
	// FIXME: May want to eventually make scaleFactorDoubleArray,
	// scaleFactorTokenArray be length consumptionRate to have
	// finer granularity pitch scale control.
        scaleFactorDoubleArray[0] = scaleFactorTokenArray[0].doubleValue();
	// FIXME: Should pass scaleFactorDoubleArray[] to
	// performPitchShift(), not just the first element.
	double pitchScaleIn = scaleFactorDoubleArray[0];


	audioOutDoubleArray = ps.performPitchShift(audioInDoubleArray,
                pitchInDoubleArray, pitchScaleIn);

	// Convert to DoubleToken[].
	for (i = 0; i < productionRate; i++) {
	    audioTokenArray[i] = new DoubleToken(audioOutDoubleArray[i]);
	}
	output.sendArray(0, audioTokenArray);
    }

    /** Read in the sound file specified by the <i>pathName</i> parameter
     *  and initialize the current sample index to 0.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

	sampRate = ((DoubleToken)sampleRate.getToken()).doubleValue();

	ps = new PitchShift((float)sampRate);

	audioTokenArray = new DoubleToken[consumptionRate];
	pitchTokenArray = new DoubleToken[consumptionRate];
	scaleFactorTokenArray = new DoubleToken[1];
	audioInDoubleArray = new double[consumptionRate];
	pitchInDoubleArray = new double[consumptionRate];
	audioOutDoubleArray = new double[consumptionRate];
	scaleFactorDoubleArray = new double[1];
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double sampRate;

    private PitchShift ps;

    private int productionRate;
    private int consumptionRate;

    private DoubleToken[] audioTokenArray;
    private DoubleToken[] pitchTokenArray;
    private DoubleToken[] scaleFactorTokenArray;

    private double[] audioInDoubleArray;
    private double[] pitchInDoubleArray;
    private double[] scaleFactorDoubleArray;

    private double[] audioOutDoubleArray;
}
