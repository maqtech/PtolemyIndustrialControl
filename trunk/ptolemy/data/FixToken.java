/** A token that contains a FixPoint number.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCL5AIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.FixPoint;
import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// FixToken
/**
A token that contains an instance of FixPoint.

@author Bart Kienhuis, Edward A. Lee, Steve Neuendorffer
@see ptolemy.data.Token
@see ptolemy.math.FixPoint
@see ptolemy.math.Precision
@see ptolemy.math.Quantizer
@version $Id$
@since Ptolemy II 0.4
*/

public class FixToken extends ScalarToken {

    /** Construct a FixToken with the supplied FixPoint value.
     *  @param value A FixPoint value.
     */
    public FixToken(FixPoint value) {
        _value = value;
    }

    // FIXME: The constructors should throw IllegalActionException instead of
    // IllegalArgumentException. But since the FixPointFunctions class in the
    // expression package does not catch IllegalActionException, leave
    // IllegalArgumentException for now.

    /** Construct a FixToken representing the specified value with the
     *  specified precision.  The specified value is quantized to the
     *  closest value representable with the specified precision.
     *
     *  @param value The value to represent.
     *  @param precision The precision to use.
     *  @exception IllegalArgumentException If the supplied precision
     *   is invalid.
     */
    public FixToken(double value, Precision precision)
            throws IllegalArgumentException {
        try {
            _value = Quantizer.round(value, precision);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Construct a FixToken representing the specified value with the
     *  specified precision.  The specified value is quantized to the
     *  closest value representable with the specified precision.
     *
     *  @param value The value to represent.
     *  @param numberOfBits The total number of bits.
     *  @param integerBits The number of integer bits.
     *  @exception IllegalArgumentException If the supplied precision
     *   is invalid.
     */
    public FixToken(double value, int numberOfBits, int integerBits)
            throws IllegalArgumentException {
        try {
            Precision precision =
                new Precision( numberOfBits, integerBits);
            _value = Quantizer.round(value, precision);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /** Construct a FixToken from the specified string.
     *  @param init A string expression of a fixed point number in Ptolemy II
     *   expression language syntax.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable fixed point number.
     */
    public FixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	FixToken token = (FixToken)tree.evaluateParseTree();
        _value = token.fixValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of FixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of FixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below FixToken in the type hierarchy, it is converted to
     *  an instance of FixToken or one of the subclasses of
     *  FixToken and returned. If none of the above conditions are
     *  met, an exception is thrown.
     *  @param token The token to be converted to a FixToken.
     *  @return A FixToken.
     *  @exception IllegalActionException If the conversion
     *  cannot be carried out.
     */
    public static FixToken convert(Token token)
	    throws IllegalActionException {
	if (token instanceof FixToken) {
	    return (FixToken)token;
	}

        int compare = TypeLattice.compare(BaseType.FIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "fix"));
        }

	throw new IllegalActionException(
                notSupportedConversionMessage(token, "fix"));
    }

    /** Return the fixed point value of this token as a double. The
     *  conversion from a fixed point to a double is not lossless, so
     *  the doubleValue() cannot be used. Therefore an explicit lossy
     *  conversion method is provided.
     *  @return A double representation of the value of this token.
     */
    public double convertToDouble() {
        return _value.doubleValue();
    }

    /** Return true if the argument is an instance of FixToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of FixToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != FixToken.class) {
	    return false;
	}

	if (((FixToken)object).fixValue().equals(_value)) {
	    return true;
	}

	return false;
    }

    /** Return the value of this token as a FixPoint.
     *  @return A FixPoint.
     */
    public FixPoint fixValue() {
        return _value;
    }

    /** Return the type of this token.
     *  @return BaseType.FIX.
     */
    public Type getType() {
	return BaseType.FIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the contained fixed point number.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	double code = _value.doubleValue();
	return (int)code;
    }

    /** Returns a new Token representing the multiplicative identity
     *  with the same precision as this FixToken.
     *  @return A new FixToken with value 1.0.
     */
    public Token one() {
        return new FixToken( 1.0, _value.getPrecision() );
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same
     *  value. The "fix" keyword indicates it is a FixToken. The first
     *  argument is the decimal value, the second is the total number
     *  of bits and the third is the number of bits for the integer
     *  portion. For more information about these arguments, see the
     *  three argument constructor.
     *
     *  @return A String representing of this Token.
     */
    public String toString() {
        Precision precision = _value.getPrecision();
	return "fix(" + _value.toString() +
            "," + precision.getNumberOfBits() +
            "," + precision.getIntegerBitLength() + ")";
    }

    /** Return a new token representing the additive identity with
     *  the same precision as this FixToken.
     *  @return A new FixToken with value 0.0.
     */
    public Token zero() {
        return new FixToken( 0.0, _value.getPrecision() );
    }

    /** Print the content of this FixToken: This is used for debugging
     *  only.
     */
    public void print() {
        _value.printFix();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.
     *  @return A FixToken.
     */
    protected ScalarToken _absolute() {
        FixToken result = new FixToken(_value.abs());
        return result;
    }

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is an FixToken.
     *  @param rightArgument The token to add to this token.
     *  @return A new FixToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        FixPoint result = _value.add(((FixToken)rightArgument).fixValue());
        return new FixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an FixToken
     *  @param rightArgument The token to divide this token by.
     *  @return A new FixToken containing the result.
     */
    protected ScalarToken _divide(ScalarToken rightArgument) {
        FixPoint result = _value.divide(((FixToken)rightArgument).fixValue());
        return new FixToken(result);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  FixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isCloseTo(
            ScalarToken rightArgument, double epsilon)
            throws IllegalActionException {
        return _isEqualTo(rightArgument);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  FixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException {
        FixToken convertedArgument = (FixToken)rightArgument;
        FixPoint fixValue = convertedArgument.fixValue();
        return BooleanToken.getInstance(_value.equals(fixValue));
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is FixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        FixToken convertedArgument = (FixToken)rightArgument;
        return BooleanToken.getInstance(
                 _value.doubleValue() < convertedArgument.fixValue().doubleValue());

    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  @param rightArgument The token to modulo this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed that
     *  the type of the argument is an FixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new FixToken containing the result.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        FixPoint result =
            _value.multiply(((FixToken)rightArgument).fixValue());
        return new FixToken(result);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is an FixToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new FixToken containing the result.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        FixPoint result =
            _value.subtract(((FixToken)rightArgument).fixValue());
        return new FixToken(result);
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The FixPoint value contained in this FixToken. */
    private FixPoint _value;
}
