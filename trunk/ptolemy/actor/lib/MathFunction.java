/* An actor that computes a specified math function of the input.

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

@ProposedRating Yellow (chf@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/math.xml.

//////////////////////////////////////////////////////////////////////////
//// MathFunction
/**
Produce an output token on each firing with a value that is
equal to the specified math function of the input.
The input and output types are DoubleToken.  The functions
are a subset of those in the java.lang.Math class.  They are:
<ul>
<li> <b>exp</b>: The exponential function.
This is the default function for this actor
If the argument is NaN, then the result is NaN.
<li> <b>log</b>: The natural logarithm function.
If the argument is NaN, then the result is NaN.
<li> <b>remainder</b>: The remainder after division.
If the second operand is zero, then the result is NaN.
<li> <b>sign</b>: The signum function
If the argument is NaN, then the result is NaN.
<li> <b>square</b>: The square function
If the argument is NaN, then the result is NaN.
<li> <b>sqrt</b>: The square root function.
If the argument is NaN, then the result is NaN.
</ul>
<p>
NOTE: Some functions like exp, log, square, and sqrt act on a single
operand only.  Other functions like remainder act on two operands.
The actor acquires a second input when the function is changed to
remainder, and loses the input when the function is changed back.

@author C. Fong
@version $Id$
@see AbsoluteValue
@see Scale
@see TrigFunction
*/
public class MathFunction extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MathFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Parameters
        function = new StringAttribute(this, "function");
        function.setExpression("exp");
        _function = _EXP;

        // Ports
        // secondOperand port is not allocated in the constructor
        // instead it will allocated dynamically during run-time

        firstOperand = new TypedIOPort(this, "firstOperand");
        firstOperand.setInput(true);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        firstOperand.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued attribute
     *  that defaults to "exp".
     */
    public StringAttribute function;

    /** The port for the first operand.
     *  The port has type BaseType.DOUBLE
     */
    public TypedIOPort firstOperand = null;

    /** The port for the second operand, if it is needed.
     *  The port has type BaseType.DOUBLE
     */
    public TypedIOPort secondOperand = null;

    /** Output port
     *  The port has type BaseType.DOUBLE
     */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws  IllegalActionException {
        try {
            if (attribute == function) {
                String functionName = function.getExpression();
                if (functionName.equals("exp")) {
                    _function = _EXP;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("log")) {
                    _function = _LOG;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("remainder")) {
                    _function = _REMAINDER;
                    _createSecondPort();
                } else if (functionName.equals("sign")) {
                    _function = _SIGN;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("square")) {
                    _function = _SQUARE;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else if (functionName.equals("sqrt")) {
                    _function = _SQRT;
                    if (secondOperand != null) {
                        secondOperand.setContainer(null);
                    }
                } else {
                    throw new IllegalActionException(this,
                            "Unrecognized math function: " + functionName);
                }
            } else {
                super.attributeChanged(attribute);
            }
        } catch (NameDuplicationException e) {
            throw new InternalErrorException("Unexpected name duplication.");
        }
    }

    /** Consume at most one input token from each input channel, and
     *  compute the specified math function of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (firstOperand.hasToken(0)) {
            double input1 = ((DoubleToken) firstOperand.get(0)).doubleValue();
            double input2 = 1.0;
            if (_function == _REMAINDER) {
                if (secondOperand.hasToken(0)) {
                    input2 =
                        ((DoubleToken) secondOperand.get(0)).doubleValue();
                }
            }
            output.send(0, new DoubleToken(_doFunction(input1, input2)));
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration computes the math function specified by the
     *  <i>function</i> parameter on a single token. An invocation
     *  of this method therefore applies the function to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public int iterate(int count) throws IllegalActionException {
	// Check whether we need to reallocate the output token array.

        Token[] inArray1;
        Token[] inArray2;

        if (count > _resultArray.length) {
            _resultArray = new DoubleToken[count];
        }

        if (firstOperand.hasToken(0, count)) {
            if (_function == _REMAINDER) {
                if (secondOperand.hasToken(0, count)) {
                    inArray1 = firstOperand.get(0, count);
                    inArray2 = secondOperand.get(0, count);
                    for (int i = 0; i < count; i++) {
                        double input1 =
                            ((DoubleToken)(inArray1[i])).doubleValue();
                        double input2 =
                            ((DoubleToken)(inArray2[i])).doubleValue();
                        _resultArray[i] =
                            new DoubleToken(_doFunction(input1, input2));
                    }
                    output.send(0, _resultArray, count);
                    return COMPLETED;
                } else {
                    return NOT_READY;
                }
            } else {
                inArray1 = firstOperand.get(0, count);
                for(int i = 0; i < count ; i++) {
                    double input1 = ((DoubleToken)(inArray1[i])).doubleValue();
                    _resultArray[i] = new DoubleToken(_doFunction(input1, 0));
                }
                output.send(0, _resultArray, count);
                return COMPLETED;
            }
        } else {
            return NOT_READY;
        }
        // Note: constants COMPLETED and NOT_READY are defined in
        // ptolemy.actor.Executable
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create the second port needed by remainder function
     */
    private void _createSecondPort()
            throws NameDuplicationException, IllegalActionException {

        if (secondOperand == null) {
            secondOperand = new TypedIOPort(this, "secondOperand", true, false);
        } else if (secondOperand.getContainer() == null) {
            secondOperand.setContainer(this);
        }
        secondOperand.setTypeEquals(BaseType.DOUBLE);
    }


    /** Calculate the function on the given argument.
     *  @param input1 The first input value.
     *  @param input2 The second input value.
     *  @return The result of applying the function.
     */
    private double _doFunction(double input1, double input2) {
        double result;
        switch(_function) {
        case _EXP:
            result = Math.exp(input1);
            break;
        case _LOG:
            result = Math.log(input1);
            break;
        case _REMAINDER:
            result = input1 % input2;
            break;
        case _SIGN:
            if (input1 > 0) {
                result = 1.0;
            } else if (input1 < 0) {
                result = -1.0;
            } else {
                result = 0.0;
            }
            break;
        case _SQUARE:
            result = input1 * input1;
            break;
        case _SQRT:
            result = Math.sqrt(input1);
            break;
        default:
            throw new InternalErrorException(
                    "Invalid value for _function private variable. "
                    + "MathFunction actor (" + getFullName()
                    + ")"
                    + " on function type " + _function);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DoubleToken[] _resultArray = new DoubleToken[0];

    // An indicator for the function to compute.
    private int _function;

    // Constants used for more efficient execution.
    private static final int _EXP = 0;
    private static final int _LOG = 1;
    private static final int _REMAINDER = 2;
    private static final int _SIGN = 3;
    private static final int _SQUARE = 4;
    private static final int _SQRT = 5;
}

