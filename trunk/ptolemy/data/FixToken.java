/** A token that contains a FixPoint number.

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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.FixPoint;
import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// FixToken
/**
A token that contains a FixPoint.
<p>
@author Bart Kienhuis
@see ptolemy.data.Token
@see ptolemy.math.FixPoint
@see ptolemy.math.Precision
@see ptolemy.math.Quantizer
@version $Id$

*/
public class FixToken extends ScalarToken {

    /** Construct a FixToken with for the supplied FixPoint value
     *  @param value a FixPoint value
     */
    public FixToken(FixPoint value) {
	_value = value;
    }

    /** Construct a FixToken with a value given as a String and a
	precision given as a String. Since FixToken has a finite
	number uses a finite number of bits to represent a value the
	supplied value is rounded to the nearest value possible given the
	precision, thereby introducing quantization errors.
	@param pre the precision of the FixToken.
	@param value the value of the FixToken.
	@exception IllegalArgumentException If the format of the precision 
	string is incorrect
    */
    public FixToken(double value, String pre) 
            throws IllegalArgumentException {
                try {
                    Precision precision = new Precision( pre );
                    _value = Quantizer.round(value, precision);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
    }

    /** Construct a FixToken with a value given as a String and a
	precision given as a String. Since FixToken has a finite
	number uses a finite number of bits to represent a value, the
	supplied value is rounded to the nearest value possible given
	the precision, thereby introducing quantization errors.  
	@param precision String giving the precision of the FixToken 
	@param value Double value of the FixToken
	@exception IllegalArgumentException If the format of the
	precision string is incorrect */
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a FixToken containing the absolute value of the
     *  value of this token.
     *  @return An FixToken. 
     */
    public ScalarToken absolute() {
	return new FixToken(_value.absolute());
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  @param token A Token.
     *  @return A new Token.  
     *  @exception IllegalActionException If the specified token
     *   is not of a type that can be added to this Tokens value in
     *   a lossless fashion.
     */
    public Token add(Token token)
	    throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            String msg = "add method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return token.addReverse(this);
        } else {
	    // type of the specified token <= FixToken
	    FixToken tem = (FixToken)convert(token);
	    FixPoint result = _value.add(tem.fixValue());
	    return new FixToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixToken.
     *  @param token The token to add this Token to.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken.
     */
    public Token addReverse(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
                    + " is not lower than "
                    + getClass().getName());
        }

	// add is commutative on FixPoint.
        return add(token);
    }


    /** Return a new Token whose value is the value of this token
     *  divided by the value of the argument token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param divisor A FixToken.
     *  @return A new FixToken. 
     *  @exception IllegalActionException If the passed token is
     *  not of a type that can be divide this Tokens value by in a
     *  lossless fashion. 
     */
    public Token divide(Token divisor) 
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, divisor);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.divide: " +
                    "type of argument: " + divisor.getClass().getName() +
                    "is incomparable with FixToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return divisor.divideReverse(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken comptoken = (FixToken)convert(divisor);
            FixPoint result = _value.divide(comptoken.fixValue());
            return new FixToken(result);
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  divided by the value of this token. The type of the specified
     *  token must be lower than FixToken.
     *  @param dividend The token to be divided by the value of this Token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken;
     */
    public Token divideReverse(Token dividend)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, dividend);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the dividend "
                    + dividend.getClass().getName() + " is not lower than "
                    + getClass().getName());
        }

        FixToken tem = (FixToken)this.convert(dividend);
        FixPoint result = tem.fixValue().divide(_value);
        return new FixToken(result);
    }

    /** Return the value of this token as a Fixpoint.
     *  @return A Fixpoint
     */
    public FixPoint fixValue() {
        // FixToken is immutable, so we can just return the value.
        return _value;
    }

    /** Return the fix point value of this token as a double. The
     *  conversion from a fix point to a double is not lossless, and
     *  the doubleValue() cannot be used. Therefore an explicit lossy
     *  conversion method is provided, 
     *  @return A double.
     */
    public double convertToDouble() {
        return _value.doubleValue();
    }

    /** Return the type of this token.
     *  @return BaseType.FIX
     */
    public Type getType() {
	return BaseType.FIX;
    }

    /** Test the values of this Token and the argument Token for equality.
     *  The type of the specified token must be such that either it can be
     *  converted to the type of this token, or the type of this token can
     *  be converted to the type of the specified token, without loss of
     *  information.
     *  @param token The token to test equality of this token with.
     *  @return BooleanToken indicating whether the values are equal.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be compared with this Token.
     */
    public BooleanToken isEqualTo(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.isEqualTo: " +
                    "type of argument: " + token.getClass().getName() +
                    " is incomparable with FixToken in the type " +
                    " hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken comptoken = (FixToken)convert(token);
            FixPoint tem = comptoken.fixValue();
	    if (_value.equals(tem)) {
                return new BooleanToken(true);
	    }
	    return new BooleanToken(false);
        }
    }

    /** Check if the value of this token is strictly less than that of the
     *  argument token.
     *  @param arg A ScalarToken.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public BooleanToken isLessThan(ScalarToken arg)
	    throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, arg);
        if (typeInfo == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.isLessThan: The type" +
		" of the argument token is incomparable with the type of " +
		"this token. argType: " + arg.getType());
	}

	if (typeInfo == CPO.LOWER) {
	    return arg.isLessThan(this);
	}

	// Argument type is lower or equal to this token.
	ScalarToken fixArg = arg;
	if (typeInfo == CPO.HIGHER) {
	    fixArg = (ScalarToken)convert(arg);
	}

        // Use double value of the fix point.
	if (_value.doubleValue() < fixArg.doubleValue()) {
            return new BooleanToken(true);
        }
	return new BooleanToken(false);
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param arg A FixToken.  
     *  @return A new FixToken.  
     *  @exception IllegalActionException If the specified token
     *  is not of a type that can be multiplied to this Token in
     *  a lossless fashion.
     */
    public Token multiply(Token token) 
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
            String msg = "multiply method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return token.multiplyReverse(this);
        } else {
	    // type of the specified token <= FixToken
	    FixToken tem = (FixToken)convert(token);
	    FixPoint result = _value.multiply(tem.fixValue());
	    return new FixToken(result);    
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixToken.
     *
     *  @param token The token to multiply this Token to.
     *  @return A new Token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken.
     */
    public Token multiplyReverse(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("FixToken.multiplyReverse: "
                    + "The type of the specified token "
                    + token.getClass().getName()
                    + " is not lower than " + getClass().getName());
        }

	// multiply is commutative on FixToken.
        return multiply(token);
    }

    /** Returns a new Token representing the multiplicative identity
     *  with the same precision as the current FixToken.  
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() {
        return new FixToken( 1.0, _value.getPrecision().toString() );
    }

    /** Return a new Token whose value is the value of the argument token
     *  subtracted by the value of this token. The type of the
     *  specified token must be such that either it can be converted
     *  to the type of this token, or the type of this token can be
     *  converted to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *
     *  @param token A FixToken.
     *  @return A new FixToken. 
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be subtracted from this Token in a
     *   lossless fashion. 
     */
    public Token subtract(Token rightArg) 
	    throws IllegalActionException {
        int compare = TypeLattice.compare(this, rightArg);
	if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("FixToken.subtract: " +
                    "type of argument: " + rightArg.getClass().getName() +
                    "is incomparable with FixToken in the type " +
                    "hierarchy.");
        }

        if (compare == CPO.LOWER) {
            return rightArg.subtractReverse(this);
        } else {
	    // argument type is lower or the same as FixPoint.
	    FixToken comptoken = (FixToken)convert(rightArg);
            FixPoint result = _value.subtract(comptoken.fixValue());
            return new FixToken(result);
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token. The type of
     *  the specified token must be lower than FixToken.
     *  @param leftArg The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixToken;
     */
    public Token subtractReverse(Token leftArg)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, leftArg);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + leftArg.getClass().getName() + " is not lower "
                    + "than " + getClass().getName());
        }

        FixToken tem = (FixToken)this.convert(leftArg);
        FixPoint result = tem.fixValue().subtract(_value);
        return new FixToken(result);
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     *  @deprecated Use toString() instead.
     */
    public String stringValue() {
	return toString();
    }

    /** Return the value contained in this Token as a String.
     *  @return A String.
     */
    public String toString() {        
        Precision precision = _value.getPrecision();
	return "fix(" + _value.toString() + 
            "," + precision.getNumberOfBits() + 
            "," + precision.getIntegerBitLength() + ")";
    }

    /** Returns a new token representing the additive identity with the
     *  same precision as the current FixToken.  
     *  @return A new Token containing the additive identity.  
     */
    public Token zero()
    {
        return new FixToken( 0.0, _value.getPrecision().toString() );
    }

    /** Set the Rounding mode of the FixPoint number. */
    // FIXME: Currently it is a string, should be come a
    // type safe enumerated type
    public void setRoundingMode(String x) {
	// _value.setRounding( (String) x );
    }

    /** Print the content of this FixToken: Debug Function */
    public void print() {
        _value.printFix();
    }

    /** 

     Create a Token with a fix point value of a given precision. This
     function is used by the expression parser to create a FixToken. A
     FixToken is created as 

     <p><pre>fix( 5.34, 10, 2)</pre><p> 

     This generates a FixToken with the value 5.34 using 10 bits of
     which 2 are used to represent the integer part and 8 for the
     fractional part.

     @param value The value of the FixToken
     @param numberOfBits The total number of bits.  
     @param integerBits The number of bits used for the integer part.
     */
    public static Token fix( double value, 
            int numberOfBits, int integerBits) {
        return new FixToken( value, numberOfBits, integerBits );
    }

    /** Create a matrix with fix point values of a given
     precision. This method is used by the expression parser to create
     a matrix of fix point tokens. It is created as
     
     <p><pre>fix( [ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ], 10,  2)</pre><p> 

     This generates a 1x8 FixMatrixToken. The precision of each fix
     point value is 10 bits of which 2 are used for the integer part
     and 8 for the fraction part.
     @param values The matrix with double values.
     @param numberOfBits The total number of bits.
     @param integerBits The number of bits used for the integer part.

     */
   public static Token fix(  DoubleMatrixToken values, 
           int numberOfBits, int integerBits) {
       FixPoint [][] fxa = new FixPoint[1][values.getColumnCount()];       
       Precision precision = new Precision( numberOfBits, integerBits );
       for( int i=0; i<values.getColumnCount(); i++) {
           fxa[0][i] = Quantizer.round( values.getElementAt(0,i), precision);
       }
       return new FixMatrixToken( fxa );        
   }

    /** Create a matrix of double values that are quantized to a given
     precision. This function is used by the expression parser to
     create a matrix of quantized doubles. The matrix of quantized
     doubles is created as
     
     <p><pre>quantize( [ -.040609, -.001628, .17853, .37665, .37665, 
     .17853, -.001628, -.040609 ], 10,  2)</pre><p> 

     This generates a 1x8 matrix of double values, of which each value
     is quantized using a given precision. In this case 10 bits are
     used of which 2 for the integer part and 8 for the fraction part.
     @param values The matrix with quantized double values.
     @param numberOfBits The total number of bits.
     @param integerBits The number of bits used for the integer part.
     */
   public static Token quantize( DoubleMatrixToken values, int numberOfBits, 
           int integerBits) {
       double [][] fxa = new double[1][values.getColumnCount()];
       for( int i=0; i<values.getColumnCount(); i++) {
           Precision precision = new Precision( numberOfBits, integerBits );
           fxa[0][i] = 
               (Quantizer.round(values.getElementAt(0,i), precision)).doubleValue();;
       }
       return new DoubleMatrixToken( fxa );
   }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FixPoint _value;

}

