/* A token that contains a byte number in the range 0 through 255.

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

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (neuendor@robotics.eecs.berkeley.edu)
@AcceptedRating Yellow (winthrop@robotics.eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;


//////////////////////////////////////////////////////////////////////////
//// ByteToken
/**
A token that contains a byte number in the range 0 through 255.  This
ByteToken definition is in contrast to Java's definition of a byte as
a number in the range -128 through 127.

Overflow and underflow are handled by returning the result of all
operations modulo 256.  Thus, the result is always in the range 0
through 255.  Likewise, constructors of this class generate tokens
whose values are the argument modulo 256.  Note, for example, that
ByteToken((byte)(-100)) generates a ByteToken representing the value
156, which is -100 modulo 256.

Note, also, that the byteValue() method returns a Java byte in the
range -128 through 127.  This is in contrast to the intValue(),
longValue(), doubleValue(), and complexValue() methods which all
return values in the range 0 through 255.  The value returned by
byteValue() is the value represented by the ByteToken but with 256
subtracted if this value is greater than 127.  In other words, the
result and the argument are equal modulo 256.

@author Winthrop Williams, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ByteToken extends ScalarToken {

    /** Construct a token with byte 0.
     */
    public ByteToken() {
        _value = 0;
    }

    /** Construct a ByteToken with the specified byte value.  The
     *  ByteToken constructed represents a value in the range 0
     *  through 255.  However, the byte passed in as the argument to
     *  this method represents a value in Java in the range -128 to
     *  127.  Due to the difference between these definitions, this
     *  method effectively adds 256 if the argument is negative,
     *  resulting in a positive value for the ByteToken.
     */
    public ByteToken(byte value) {
        _value = value;
    }

    /** Construct a ByteToken with the specified integer value.  The
     *  ByteToken constructed represents a value in the range 0
     *  through 255.  However, the integer passed in as the argument
     *  to this method represents a value in Java in the range -2^31
     *  to (2^31)-1.  This method's cast to (byte) keeps only the low
     *  order 8 bits of the integer.  This effectively adds or
     *  subtracts a multiple of 256 to/from the argument.  The
     *  resulting ByteToken falls in the range 0 through 255.
     */
    public ByteToken(int value) {
        _value = (byte)value;
    }

    /** Construct a ByteToken from the specified string.  The string
     *  is parsed by the valueOf() method of the Java Byte object.
     *  @exception IllegalActionException If the token could not
     *   be created from the given string.
     */
    public ByteToken(String init) throws IllegalActionException {
        try {
            _value = (Byte.valueOf(init)).byteValue();
        } catch (NumberFormatException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex.
     */
    public Complex complexValue() {
        return new Complex((double)unsignedConvert(_value));
    }

    /** Convert the specified token into an instance of ByteToken.
     *  If the argument is already an instance of ByteToken, it is
     *  returned without any change. Otherwise, if the argument is
     *  above ByteToken in the type hierarchy or is incomparable with
     *  ByteToken, an exception is thrown with a message stating that
     *  either the conversion is not supported, or the types are
     *  incomparable.  If none of the above conditions is met, then the
     *  argument must be below ByteToken in the type hierarchy.
     *  However, not such types exist at this time, so an exception is
     *  thrown with a message stating simply that the conversion is
     *  not supported.
     *  @param token The token to be converted to a ByteToken.
     *  @return A ByteToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static ByteToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ByteToken) {
            return (ByteToken)token;
        }

        int compare = TypeLattice.compare(BaseType.BYTE, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "byte"));
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "byte"));
    }

    /** Return the value in the token as a double.  First, the
     *  unsignedConvert() method of this class is used to
     *  convert the byte to an integer in the range 0 through 255.
     *  Then this integer is cast to a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
        return (double)unsignedConvert(_value);
    }

    /** Return true if the argument is an instance of ByteToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of ByteToken with the
     *  same value.
     */
    public boolean equals(Object object) {
        // This test rules out subclasses.
        if (object.getClass() != ByteToken.class) {
            return false;
        }

        if (((ByteToken)object).byteValue() == _value) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.BYTE
     */
    public Type getType() {
        return BaseType.BYTE;
    }

    /** Return a hash code value for this token. This method just returns
     *  the value of this token.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return _value;
    }

    /** Return the value in this token as a byte, modulo 256.  The
     *  ByteToken being converted represents a value in the range 0
     *  through 255.  However, the Java byte it is being converted to
     *  represents a value in the range -128 through 127.  Thus, this
     *  method has the effect subtracting 256 from the value if the
     *  value is greater than 127.
     *  @return The byte value contained in this token, modulo 256.
     */
    public byte byteValue() {
        return _value;
    }

    /** Return the value in the token as an integer.  The ByteToken
     *  being converted to an integer represents a value in the range
     *  0 through 255.  The unsignedConvert() method of this class is
     *  used to convert the byte to an integer in the range 0 through
     *  255.
     *  @return The byte value contained in this token as a int.
     */
    public int intValue() {
        return unsignedConvert(_value);
    }

    /** Return the value in the token as a long.  The ByteToken being
     *  converted to a long represents a value in the range 0 through
     *  255.  The unsignedConvert() method of this class is used to
     *  convert the byte to an integer in the range 0 through 255.
     *  @return The byte value contained in this token as a long.
     */
    public long longValue() {
        return (long) unsignedConvert(_value);
    }

    /** Returns a new ByteToken with value 1.
     *  @return A new ByteToken with value 1.
     */
    public Token one() {
        return new ByteToken(1);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If this token has a unit, the return string also includes a unit
     *  string produced by the unitsString() method in the super class.
     *  @return A String representing the byte value and the units (if
     *   any) of this token.
     *  @see ptolemy.data.ScalarToken#unitsString
     */
    public String toString() {
        String unitString = "";
        if ( !_isUnitless()) {
            unitString = " * " + unitsString();
        }
        return Byte.toString(_value) + unitString;
    }

    /** Convert a byte to an integer, treating the byte as an unsigned
     *  value in the range 0 through 255.  Note that Java defines the
     *  byte as having a value ranging from -128 through 127, so 256
     *  is added if this value is negative.
     *  @param value The byte to convert as an unsigned byte.
     *  @return An integer in the range 0 through 255.
     */
    public static int unsignedConvert(byte value) {
        int intValue = value;
        if (intValue < 0) {
            intValue += 256;
        }
        return intValue;
    }

    /** Returns a new ByteToken with value 0.
     *  @return A new ByteToken with value 0.
     */
    public Token zero() {
        return new ByteToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is is
     *  return.  Note that it is explicitly allowable to return this
     *  token, since the units are the same.  Since we have defined
     *  the byte as ranging from 0 through 255, its absolute value is
     *  equal to itself.  Although this method does not actually do
     *  anything, it is included to make the ByteToken interface
     *  compatible with the interfaces of the other types.
     *  @return An ByteToken.
     */
    protected ScalarToken _absolute() {
        return this;
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is an ByteToken.  Overflow is handled
     *  by subtracting 256 so that the resulting sum falls in the
     *  range 0 through 255.
     *  @param rightArgument The token to add to this token.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _add(ScalarToken rightArgument) {
        byte sum = (byte)(_value + ((ByteToken)rightArgument).byteValue());
        return new ByteToken(sum);
    }

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @return The bitwise AND.
     */
    protected ScalarToken _bitwiseAnd(ScalarToken rightArgument) {
        byte sum = (byte)(_value & ((ByteToken)rightArgument).byteValue());
        return new ByteToken(sum);
    }

   /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     */
    protected ScalarToken _bitwiseNot() {
        ByteToken result = new ByteToken((byte)~_value);
        return result;
    }

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @return The bitwise OR.
     */
    protected ScalarToken _bitwiseOr(ScalarToken rightArgument) {
        byte sum = (byte)(_value | ((ByteToken)rightArgument).byteValue());
        return new ByteToken(sum);
    }

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @return The bitwise XOR.
     */
    protected ScalarToken _bitwiseXor(ScalarToken rightArgument) {
        byte sum = (byte)(_value ^ ((ByteToken)rightArgument).byteValue());
        return new ByteToken(sum);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an ByteToken.  The result will be
     *  a byte containing the quotient.  For example, 255 divided by
     *  10 returns 25.  This method does not test for or attempt to
     *  prevent division by 0.
     *  @param rightArgument The token to divide this token by.
     *  @return A new ByteToken containing the result.  */
    protected ScalarToken _divide(ScalarToken rightArgument) {
        byte quotient = (byte) (unsignedConvert(_value)
                / unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(quotient);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  ByteToken.
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
     *  ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(ScalarToken rightArgument)
            throws IllegalActionException {
        ByteToken convertedArgument = (ByteToken)rightArgument;
        return BooleanToken.getInstance(
                _value == convertedArgument.byteValue());
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is ByteToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        ByteToken convertedArgument = (ByteToken)rightArgument;
        return BooleanToken.getInstance(
                _value < convertedArgument.byteValue());
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  the type of the argument is ByteToken.  This method does not
     *  test for or attempt to prevent division by 0.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new ByteToken containing the result.
     */
    protected ScalarToken _modulo(ScalarToken rightArgument) {
        byte remainder = (byte) (unsignedConvert(_value)
                % unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(remainder);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is ByteToken.  Overflow is
     *  handled by subtracting the multiple of 256 which puts the
     *  result into the range 0 through 255.  In other words, return
     *  the product modulo 256.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new ByteToken containing the product modulo 256.
     */
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        byte product = (byte) (unsignedConvert(_value)
                * unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(product);
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is assumed
     *  that the type of the argument is ByteToken.  Underflow is
     *  handled by adding 256 to the result if it is less than 0.
     *  Thus the result is always in the range 0 through 255.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new ByteToken containing the difference modulo 256.
     */
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        byte difference = (byte) (unsignedConvert(_value)
                * unsignedConvert(((ByteToken)rightArgument).byteValue()));
        return new ByteToken(difference);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private byte _value;
}
