/* A token that contains a double precision number.

 Copyright (c) 1997 The Regents of the University of California.
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

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import java.text.NumberFormat;

//////////////////////////////////////////////////////////////////////////
//// DoubleToken
/**
A token that contains a double precision number.
FIXME: what do do about long in the operator overloading methods?*
<p>
@author Neil Smyth, Yuhong Xiong
@see ptolemy.data.Token
@see java.text.NumberFormat
@version $Id$ %G

*/
public class DoubleToken extends ScalarToken {

    /** Construct a token with double 0.0.
     */
    public DoubleToken() {
	_value = 0.0;
    }

    /** Construct a token with the specified value.
     */
    public DoubleToken(double value) {
	_value = value;
    }

    /** Construct an IntToken from the specified string.
     *  @exception IllegalArgumentException If the Token could not
     *   be created with the given String.

    public DoubleToken(String init) throws IllegalArgumentException {
	try {
	    _value = (Double.valueOf(init)).doubleValue();
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e.getMessage());
	}
    }
    */
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve a lossless conversion.
     *  @param tok The token to add to this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token add(ptolemy.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.addR(this);
            } else if (tok instanceof DoubleToken) {
                double result = _value + ((DoubleToken)tok).getValue();
                return new DoubleToken(result);
            } else  if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(tok);
                double result = _value + tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "add method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + tok.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param tok The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token addR(ptolemy.data.Token tok) throws IllegalActionException {
        DoubleToken tmp = (DoubleToken)this.convert(tok);
        double result = tmp.getValue() + _value;
        return new DoubleToken(result);
    }

    /** Return the value of this token as a Complex. The real part
     *  of the Complex is the value of this token, the imaginary part
     *  is set to 0.
     *  @return A Complex
     */
    public Complex complexValue() {
        return new Complex(_value);
    }

    /** Convert the specified token into an instance of DoubleToken.
     *  This method does lossly conversion.
     *  If the argument is already an instance of DoubleToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below DoubleToken in the type hierarchy, it is converted to
     *  an instance of DoubleToken or one of the subclasses of
     *  DoubleToken and returned. If non of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a DoubleToken.
     *  @return A DoubleToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeCPO.compare(new DoubleToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("DoubleToken.convert: " +
	    	"type of argument: " + token.getClass().getName() +
	    	"is higher or incomparable with DoubleToken in the type " +
		"hierarchy.");
	}

	if (token instanceof DoubleToken) {
	    return token;
	}

	compare = TypeCPO.compare(new IntToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    IntToken inttoken = (IntToken)IntToken.convert(token);
	    return new DoubleToken(inttoken.doubleValue());
	}

	// FIXME: token must be user defined. what to do?
	throw new IllegalActionException("cannot convert from token " +
		"type: " + token.getClass().getName() + " to a DoubleToken");
    }

    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param divisor The token to divide this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divide(Token divisor) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, divisor);
        try {
            if (typeInfo == CPO.LOWER) {
                return divisor.divideR(this);
            } else if (divisor instanceof DoubleToken) {
                double result = _value / ((DoubleToken)divisor).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(divisor);
                double result = _value / tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "divide method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + divisor.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

     /** Return a new Token whose value is the value of the argument token
     *  divided by the value of this token. Type resolution
     *  also occurs here, with the returned Token type chosen to achieve
     *  a lossless conversion.
     *  @param dividend The token to be divided by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be divided by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token divideR(Token dividend) throws IllegalActionException {
        DoubleToken tmp = (DoubleToken)this.convert(dividend);
        double result = tmp.getValue() / _value;
        return new DoubleToken(result);
    }

    /** Return the value in the token as a double.
     *  @return The value contained in this token as a double.
     */
    public double doubleValue() {
	return _value;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token to test equality of this token with.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be compared with this Tokens value.
     *  @return BooleanToken indicating whether the values are equal.
     */
    public BooleanToken equals(Token token) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.equals(this);
            } else if (token instanceof DoubleToken) {
                if ( _value == ((DoubleToken)token).getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(token);
                if ( _value == tmp.getValue()) {
                    return new BooleanToken(true);
                }
                return new BooleanToken(false);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "equality method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }


    /** Get the double value contained by this token.
     *  @return The double value contained by this token.
     */
    public double getValue() {
        return _value;
    }

    /** Return a new Token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param token The token to modulo this Token by
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be  used with modulo in a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token modulo(Token token) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, token);
        try {
            if (typeInfo == CPO.LOWER) {
                return token.moduloR(this);
            } else if (token instanceof DoubleToken) {
                double result = _value % ((DoubleToken)token).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(token);
                double result = _value % tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "modulo method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + token.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument token
     *  modulo the value of this token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param token The token to apply modulo to by the value of this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can apply modulo by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token moduloR(Token token) throws IllegalActionException {
        DoubleToken tmp = (DoubleToken)this.convert(token);
        double result = tmp.getValue() %  _value;
        return new DoubleToken(result);
    }


    /** Return a new Token whose value is the value of this Token
     *  multiplied with the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightFactor The token to multiply this Token by.
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be multiplied by this Tokens value in
     *  a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, rightFactor);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightFactor.multiplyR(this);
            } else if (rightFactor instanceof DoubleToken) {
                double result = _value * ((DoubleToken)rightFactor).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(rightFactor);
                double result = _value * tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "multiply method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightFactor.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of the argument Token
     *  multiplied with the value of this Token.
     *  Type resolution also occurs here, with the returned Token
     *  type chosen to achieve a lossless conversion.
     *  @param leftFactor The token to be multiplied by the value of
     *   this Token.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be multiplied by this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token multiplyR(Token leftFactor) throws IllegalActionException {
        DoubleToken tmp = (DoubleToken)this.convert(leftFactor);
        double result = tmp.getValue() * _value;
        return new DoubleToken(result);
    }

    /** Returns a new Token representing the multiplicative identity.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new DoubleToken(1.0);
    }

    /** Get the value contained in this Token as a String.
     *  It uses java.text.NumberFormat to format the number.
     *  @return The value contained in this token as a String.
     */
    public String stringValue() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        return nf.format(_value);
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArg The token to subtract to this Token.
     *  @exception IllegalActionException If the passed token is
     *   not of a type that can be subtracted from this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token rightArg) throws IllegalActionException {
        int typeInfo = TypeCPO.compare(this, rightArg);
        try {
            if (typeInfo == CPO.LOWER) {
                return rightArg.addR(this);
            } else if (rightArg instanceof DoubleToken) {
                double result = _value -  ((DoubleToken)rightArg).getValue();
                return new DoubleToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                DoubleToken tmp = (DoubleToken)this.convert(rightArg);
                double result = _value - tmp.getValue();
                return new DoubleToken(result);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            String str = "subtract method not supported between";
            str = str + this.getClass().getName() + " and ";
            str = str + rightArg.getClass().getName();
            throw new IllegalActionException(str + ": " + ex.getMessage());
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  Type resolution also occurs here, with the returned Token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArg The token to add this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     *  @return A new Token containing the result.
     */
    public Token subtractR(Token leftArg) throws IllegalActionException {
        DoubleToken tmp = (DoubleToken)this.convert(leftArg);
        double result = _value - tmp.getValue();
        return new DoubleToken(result);
    }

    /** Return a description of the token as a string.
     *  In this base class, we return the fully qualified class name.
     *  @return A String representation of this token.
     */
    public String toString() {
        String str =  getClass().getName() + "(" + stringValue() + ")";
        return str;
    }

    /** Returns a new token representing the additive identity.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
        return new DoubleToken(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _value;
}

