/* An actor that outputs a random sequence with a Rician distribution.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import java.util.Random;
import java.lang.Math;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// Rician
/**
Produce a random sequence with a Rician distribution.
A Rician random variable is defined as follows:
Let Z = sqrt(X<sup>2</sup> + Y<sup>2</sup>), where X and Y are statically
independent Gaussian random variables with means given by parameters
<i>xMean</i> and <i>yMean</i> respectively, and common variance given by
parameter <i>standardDeviation</i>.
<p>
When <i>xMean</i> and <i>yMean</i> are both set to be zero, the Rician
distribution is also called a Rayleigh distribution. So this actor can
also be used as a Rayleigh random generator.
<p>
On each iteration, a new random number is produced. The output port
is of type DoubleToken. The values that are generated are independent
and identically distributed with the means and the standard deviation
given by parameters. In addition, the seed can be specified as a
parameter to control the sequence that is generated.

@author Rachel Zhou
@version $Id$
@since Ptolemy II 0.2
*/

public class Rician extends RandomSource {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Rician(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        xMean = new Parameter(this, "xMean", new DoubleToken(0.0));
        xMean.setTypeEquals(BaseType.DOUBLE);

        yMean = new Parameter(this, "yMean", new DoubleToken(0.0));
        yMean.setTypeEquals(BaseType.DOUBLE);

        standardDeviation = new Parameter(this, "standardDeviation",
              new DoubleToken(1.0));
        standardDeviation.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The mean of the random number along X-axis.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter xMean;

     /** The mean of the random number along X-axis.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter yMean;

    /** The standard deviation of the random number.
     *  This parameter contains a DoubleToken, initially with value 1.
     */
    public Parameter standardDeviation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a Rician distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    /** Calculate the next random number.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
        double xMeanValue = ((DoubleToken)(xMean.getToken())).doubleValue();
        double yMeanValue = ((DoubleToken)(yMean.getToken())).doubleValue();
        double standardDeviationValue =
            ((DoubleToken)(standardDeviation.getToken())).doubleValue();
        double xRawNum = _random.nextGaussian();
        double yRawNum = _random.nextGaussian();
        //_current = ptolemy.math.complex.sqrt(
        //  ptolemy.math.complex.pow(
        //  rawNum*standardDeviationValue + meanValue, 2)
        //  + ptolemy.math.complex.pow(
        //  raw2Num*standardDeviationValue + mean2Value, 2);
        //_current = ptolemy.math.Complex.sqrt(2);
        //_current = ptolemy.math.Complex.pow(2, 2);
        _current = java.lang.Math.sqrt(
             java.lang.Math.pow(
             xRawNum*standardDeviationValue + xMeanValue, 2)
             + java.lang.Math.pow(
             yRawNum*standardDeviationValue + yMeanValue, 2));
        return super.prefire();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The random number for the current iteration.
    private double _current;
}
