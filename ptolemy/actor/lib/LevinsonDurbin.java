/* Calculate the coefficients of a linear predictor.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// LevinsonDurbin
/**
This actor uses the Levinson-Durbin algorithm to compute the linear
predictor coefficients of a random process, given its autocorrelation
function as an input. These coefficients are produced both in
tapped delay line form (on the <i>linearPredictor</i> output) and in
lattice filter form (on the <i>reflectionCoefficients</i> output).
The <i>order</i> of the predictor (the number of <i>linearPredictor</i>
and coefficients <i>reflectionCoefficients</i> produced) is the
number of lags of the supplied autocorrelation.
The <i>errorPower</i> output is the power of the prediction error
as a function of the predictor order.
The inputs and outputs are all arrays of doubles.
<p>
The autocorrelation estimates provided as inputs can be generated
by the Autocorrelation actor. It the Autocorrelation actor is set
so that its <i>biased</i> parameter is true, then the combined
effect of that actor and this one is called the autocorrelation
method. The <i>order</i> of the predictor is the value of the
<i>numberOfLags</i> parameter of the Autocorrelation actor.
If the length of the autocorrelation input is odd, then it is assumed
to be a symmetric autocorrelation function, and the <i>order</i> of the
predictor calculated by this actor is (length + 1)/2.  Otherwise,
the <i>order</i> is 1 + (length/2), which assumes that discarding the last
sample of the autocorrelation would make it symmetric.
<p>
Three output signals are generated by this actor. On the
<i>errorPower</i> output port, an array of length <i>order</i> + 1
gives the prediction error power for each predictor order from zero
to <i>order</i>. The first value in this array, which corresponds
to the zeroth-order predictor, is simply the zero-th lag of the
input autocorrelation, which is the power of the random process
with that autocorrelation. Note that for signals without noise
whose autocorrelations are estimated by the Autocorrelation actor,
the <i>errorPower</i> output can get small. If it gets close
to zero, or goes negative, this actor fixes it at zero.
"Close to" is determined by the close() method of the
ptolemy.math.SignalProcessing class.
<p>
The <i>linearPredictor</i> output gives the coefficients of an
FIR filter that performs linear prediction for the random process.
This set of coefficients is suitable for directly feeding a
VariableFIR actor, which accepts outside coefficients.
The number of coefficients produced is equal to the <i>order</i>.
The predictor coefficients produced by this actor can be
used to create a maximum-entropy spectral estimate of the input
to the Autocorrelation actor.  They can also be used for
linear-predictive coding, and any number of other applications.
<p>
The <i>reflectionCoefficients</i> output is the reflection
coefficients, suitable for feeding directly to a VariableLattice
actor, which will then generate the forward and backward prediction error.
The number of coefficients produced is equal to the <i>order</i>.
<p>
Note that the definition of reflection coefficients is not quite
universal in the literature. The reflection coefficients in
reference [2] is the negative of the ones generated by this actor,
which correspond to the definition in most other texts,
and to the definition of partial-correlation (PARCOR)
coefficients in the statistics literature.
<p>
<b>References</b>
<p>[1]
J. Makhoul, "Linear Prediction: A Tutorial Review",
<i>Proc. IEEE</i>, vol. 63, pp. 561-580, Apr. 1975.
<p>[2]
S. M. Kay, <i>Modern Spectral Estimation: Theory & Application</i>,
Prentice-Hall, Englewood Cliffs, NJ, 1988.

@see ptolemy.domains.sdf.lib.Autocorrelation
@see ptolemy.domains.sdf.lib.VariableFIR
@see ptolemy.domains.sdf.lib.VariableLattice
@see ptolemy.math.SignalProcessing

@author Edward A. Lee
@version $Id$
*/

public class LevinsonDurbin extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public LevinsonDurbin(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        autocorrelation =
            new TypedIOPort(this, "autocorrelation", true, false);
        errorPower = new TypedIOPort(this, "errorPower", false, true);
        linearPredictor =
            new TypedIOPort(this, "linearPredictor", false, true);
        reflectionCoefficients = new TypedIOPort(
                this, "reflectionCoefficients", false, true);

        // FIXME: Can the inputs be complex?
        autocorrelation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        errorPower.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        linearPredictor.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        reflectionCoefficients.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The autocorrelation input, which is an array.
     */
    public TypedIOPort autocorrelation;

    /** The output for the error power, as a function of the predictor
     *  order.  This produces an array.
     */
    public TypedIOPort errorPower;

    /** The output for linear predictor coefficients.
     *  This produces an array.
     */
    public TypedIOPort linearPredictor;

    /** The output for lattice filter coefficients for a prediction
     *  error filter.  This produces an array.
     */
    public TypedIOPort reflectionCoefficients;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the autocorrelation input, and calculate the predictor
     *  coefficients, reflection coefficients, and prediction error power.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        ArrayToken autocorrelationValue = (ArrayToken)autocorrelation.get(0);
        int autocorrelationValueLength = autocorrelationValue.length();

        // If the length of the input is odd, then the order is
        // (length + 1)/2. Otherwise, it is 1 + length/2.
        // Both numbers are the result of integer division
        // (length + 2)/2.
        int order = autocorrelationValueLength/2;

        Token[] power = new Token[order + 1];
        Token[] refl = new Token[order];
        Token[] lp = new Token[order];
        double[] a = new double[order + 1];
        double[] aP = new double[order + 1];
        double[] r = new double[order+1];

        a[0] = 1.0;
        aP[0] = 1.0;

        // For convenience, read the autocorrelation lags into a vector.
        for (int i = 0; i <= order; i++) {
            r[i] = ((DoubleToken)autocorrelationValue
                    .getElement(autocorrelationValueLength - order + i - 1))
                .doubleValue();
        }

        // Output the zeroth order prediction error power, which is
        // simply the power of the input process.
        double P = r[0];
        power[0] = new DoubleToken(P);

        double gamma;

        // The order recurrence
        for (int M = 0; M < order; M++ ) {

            // Compute the new reflection coefficient.
            double deltaM = 0.0;
            for (int m = 0; m < M+1; m++) {
                deltaM += a[m]*r[M+1-m];
            }
            // Compute and output the reflection coefficient
            // (which is also equal to the last AR parameter).
            if (SignalProcessing.close(P, 0.0)) {
                aP[M+1] = gamma = 0.0;
            } else {
                aP[M+1] = gamma = -deltaM/P;
            }

            refl[M] = new DoubleToken(-gamma);

            for (int m = 1; m < M+1; m++) {
                aP[m] = a[m] + gamma*a[M+1-m];
            }

            // Update the prediction error power.
            P = P*(1.0 - gamma*gamma);
            if (P < 0.0 || SignalProcessing.close(P, 0.0)) {
                P = 0.0;
            }
            power[M + 1] = new DoubleToken(P);

            // Swap a and aP for next order recurrence.
            double[] temp = a;
            a = aP;
            aP = temp;
        }
        // Generate the lp outputs.
        for (int m = 1; m <= order; m++ ) {
            lp[m-1] = new DoubleToken(-a[m]);
        }

        linearPredictor.broadcast(new ArrayToken(lp));
        reflectionCoefficients.broadcast(new ArrayToken(refl));
        errorPower.broadcast(new ArrayToken(power));
    }

    /** If there is no token on the <i>autocorrelation</i> input, return
     *  false. Otherwise, return whatever the base class returns.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
        if (!autocorrelation.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }
}
