/* Check the input streams against a parameter value, ignoring absent values.

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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Sink;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.math.Complex;

import java.util.Arrays;

//////////////////////////////////////////////////////////////////////////
//// SequenceTest
/**
This actor compares the inputs against the value specified by
the <i>correctValues</i> parameter.  That parameter is an ArrayToken,
which is an array of tokens.  If, on a given iteration, no token is
present on a channel, the absence is ignored.  Subsequent iterations
always succeed, so the actor can be used as a "power-up" test for
a model, checking the first few iterations against some known results.
<p>
The input is a multiport.  If there is more than one channel connected
to it, then each element of <i>correctValues</i> must itself be an
ArrayToken, with length matching the number of channels.
Suppose for example that the width of the input is one,
and the first three inputs should be 1, 2, and 3.  Then you can
set <i>correctValues</i> to
<pre>
    [1, 2, 3]
</pre>
or
<pre>
    [1; 2; 3]
</pre>
Either syntax is acceptable.
Suppose instead that the input has width two, and the correct values
in the first iteration are 1 on the first channel and 2 on the second.
Then on the second iteration, the correct values are 3 on the first
channel and 4 on the second.  Then you can set <i>correctValues</i> to
<pre>
    [1, 2; 3, 4]
</pre>
With this setting, no tests are performed after the first two iterations
of this actor.
<p>
The input values are checked in the postfire() method.  If an input value
is absent, the absence is ignored.  If an input value is
present and differs from what it should be, then postfire() throws an
exception.  Thus, the test passes if no exception is thrown.
<p>
If the input is a DoubleToken or ComplexToken,
then the comparison passes if the value is close to what it should
be, within the specified <i>tolerance</i> (which defaults to
10<sup>-9</sup>.  The input data type is undeclared, so it can
resolve to anything.

@author Edward A. Lee
@version $Id$
*/

public class SequenceTest extends Sink {

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        Token[] defaultEntries = new Token[1];
        defaultEntries[0] = new BooleanToken(true);
        ArrayToken defaultArray = new ArrayToken(defaultEntries);
        correctValues = new Parameter(this, "correctValues", defaultArray);
	correctValues.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        tolerance = new Parameter(this, "tolerance", new DoubleToken(1e-9));
        tolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A matrix specifying what the input should be.
     *  This defaults to a one-by-one array containing a boolean true.
     */
    public Parameter correctValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a double, with default
     *  value 10<sup>-9</sup>.
     */
    public Parameter tolerance;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iteration = 0;
        _count = 0;
    }

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>.  If the iteration count
     *  is larger than the length of <i>correctValues</i>, then return
     *  immediately, declaring success on the test.
     *  @exception IllegalActionException If an input is missing,
     *   or if its value does not match the required value.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        if (width != 1) {
            throw new IllegalActionException(this,
                    "Width of input is " + width
                    + "but SequenceTest only supports a width of 1.");
        }
        if (_count >= ((ArrayToken)(correctValues.getToken())).length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    input.get(i);
                }
            }
            return true;
        }

        Token referenceToken
            = ((ArrayToken)(correctValues.getToken())).getElement(_count);
        if (referenceToken instanceof ArrayToken) {
            throw new IllegalActionException(this,
                    "Reference is an ArrayToken, "
                    + "but SequenceTest only supports a width of 1.");
        }
        Token[] reference = new Token[1];
        reference[0] = referenceToken;

        // To extend this to work for multiports, there would be a for loop 
        // here.
        int i = 0;
        if (input.hasToken(i)) {
            Token token = input.get(i);
            _count++;
            if (token instanceof DoubleToken) {
                // Check using tolerance.
                Token correctValue = reference[i];
                try {
                    double correct = ((DoubleToken)correctValue).doubleValue();
                    double seen = ((DoubleToken)token).doubleValue();
                    double ok
                        = ((DoubleToken)(tolerance.getToken())).doubleValue();
                    if (Math.abs(correct - seen) > ok) {
                        throw new IllegalActionException(this,
                                "Test fails in iteration " + _iteration + ".\n"
                                + "Value was: " + seen
                                + ". Should have been: " + correct);
                    }
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _iteration + ".\n"
                            + "Input is a double but correct value is not: "
                            + correctValue.toString());
                }
            } else if (token instanceof ComplexToken) {
                // Check using tolerance.
                Token correctValue = reference[i];
                try {
                    Complex correct
                        = ((ComplexToken)correctValue).complexValue();
                    Complex seen
                        = ((ComplexToken)token).complexValue();
                    double ok
                        = ((DoubleToken)(tolerance.getToken())).doubleValue();
                    if (Math.abs(correct.real - seen.real) > ok ||
                            Math.abs(correct.imag - seen.imag) > ok) {
                        throw new IllegalActionException(this,
                                "Test fails in iteration " + _iteration + ".\n"
                                + "Value was: " + seen
                                + ". Should have been: " + correct);
                    }
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _iteration + ".\n"
                            + "Input is complex but correct value is not: "
                            + correctValue.toString());
                }
            } else {
                Token correctValue = reference[i];
                BooleanToken result = token.isEqualTo(correctValue);
                if (!result.booleanValue()) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _iteration + ".\n"
                            + "Value was: " + token
                            + ". Should have been: " + correctValue);
                }
            }
        }
        _iteration++;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Array of number of tokens received.
    private int _count;

    // Count of iterations.
    private int _iteration;
}




