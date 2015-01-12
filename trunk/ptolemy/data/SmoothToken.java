/* A token for QSS integration that contains a double and a derivative.

   Copyright (c) 2014 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SmoothToken

/**
   A double-valued token that contains zero or more derivatives.
   In mathematical analysis, smoothness has to do with how many derivatives
   a function possesses. A smooth function is one that has derivatives of
   all orders everywhere in its domain. An instance of this class represents
   a sample of a function at a point together with some finite number of
   derivatives of the function at that same point.
   <p>
   This token will be treated exactly
   like a {@link DoubleToken} by any actor or operation that does not
   specifically support it, and it will be represented in the type systems
   as a "double." But it can (potentially) carry additional information giving
   one or more derivatives of the function from which it is a sample.
   This token, therefore, gives a way for actors that either generate or
   use this derivative information to make that information available to other
   actors that can use it. Such actors should declare their input ports to
   be of type double, but when they receive an input token, they should
   check (using instanceof) whether the token in a SmoothToken, and if so,
   access these derivatives using the {@link #derivativeValues()} method.
   <p>
   Note that if two SmoothTokens are added or subtracted, then the derivatives also
   add or subtract.
   If a SmoothToken is added to a DoubleToken, the derivatives of the DoubleToken
   are assumed to be zero, and similarly for subtraction.
   <p>
   If a SmoothToken is multiplied by a SmoothToken, then the product rule of
   calculus is used to determine the derivatives of the product.
   The product rule stipulates that
   <pre>
      (xy)' = x'y + xy'
   </pre>
   If a SmoothToken is multiplied by a DoubleToken, then its derivatives are
   assumed to be zero.
   <p>
   Division works similarly:
   <pre>
      (x/y)' = x'/y + x(1/y)' = x'/y - xy'/y^2
   </pre>
   where the last equality follows from the reciprocal rule of calculus.
   The second derivative of a multiplication or division is obtained by
   applying the above rules to x' and y' rather than to x and y.
   Higher-order derivatives are similarly obtained.

   @author Thierry S. Nouidui, Michael Wetter, Edward A. Lee
   @version $Id$
   @since Ptolemy II 10
   @Pt.ProposedRating Red (mw)
   @Pt.AcceptedRating Red (mw)
*/
public class SmoothToken extends DoubleToken {

    /** Construct a SmoothToken with the specified value and no derivatives.
     *  @param value The specified value.
     */
    public SmoothToken(double value) {
    	super(value);
    }
	
    /** Construct a SmoothToken with the specified value and derivatives.
     *  This constructor does not copy the derivatives argument, so it is up
     *  to the caller to ensure that the array passed in does not later get
     *  modified (tokens are required to be immutable).
     *  @param value The specified value.
     *  @param derivatives The specified derivatives.
     */
    public SmoothToken(double value, 
            double[] derivatives) {
    	super(value);
    	_derivatives = derivatives;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the derivatives in the token as a double[], or null if there are
     *  no derivatives. Since tokens are immutable, the caller of this method must
     *  copy the returned array if it intends to modify the array.
     *  @return The value of the derivatives contained in this token.
     */
    public double[] derivativeValues() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return null;
	}
	return _derivatives;
    }
    
    /** Return a new token that is the negative of this one.
     *  @return The negative, where all the derivatives are also negated.
     */
    public SmoothToken negate() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return new SmoothToken(-_value);
	}
	double[] derivatives = new double[_derivatives.length];
	for (int i = 0; i < _derivatives.length; i++) {
	    derivatives[i] = - _derivatives[i];
	}
	return new SmoothToken(-_value, derivatives);
    }

    /** Return true if the argument's class is SmoothToken and it has the
     *  same values as this token.
     *  @param object An object to compare for equality.
     *  @return True if the argument is a SmoothToken with the same
     *   value and derivatives. If either this object or the argument is a nil Token, return
     *  false.
     */
    @Override
    public boolean equals(Object object) {
	// The superclass checks class equality, doubleValue equality, and handles nil.
    	if (super.equals(object)) {
    	    // Now we just have to check the derivatives.
            double[] derivatives = ((SmoothToken) object).derivativeValues();
            if (derivatives == _derivatives) {
        	// Derivatives are identical (should be true only if null).
        	return true;
            }
            if (derivatives == null && _derivatives != null
        	    || derivatives != null && _derivatives == null) {
        	return false;
            }
            // Both tokens have derivatives.
            if (derivatives.length != _derivatives.length) {
                return false;
            }
            // Both tokens have the same number of derivatives.
            for(int i = 0; i < _derivatives.length; i++){
                if (derivatives[i] != _derivatives[i]) {
                    return false;
                }
            }
            return true;
    	} else {
            return false;
    	}
    }
    
    /** Return a SmoothToken with the specified value and no derivatives.
     *  This function gets registered when the {@link QSSDirector}
     *  class is loaded, after which it becomes available in the
     *  expression language.
     *  @param value The value.
     */
    public static SmoothToken smoothToken(double value) {
	return new SmoothToken(value, null);
    }

    /** Return a SmoothToken with the specified value and derivatives.
     *  This function gets registered when the {@link QSSDirector}
     *  class is loaded, after which it becomes available in the
     *  expression language.
     *  @param value The value.
     *  @param derivatives An array containing the first derivative,
     *   the second derivative, etc.
     */
    public static SmoothToken smoothToken(double value, double[] derivatives) {
	return new SmoothToken(value, derivatives);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  If there are no derivatives, then this just returns what the superclass
     *  returns to represent a double. Otherwise, the returned
     *  string has the form "smoothToken(value, derivatives)", where
     *  the value is the value returned by {@link #doubleValue()}, and
     *  derivatives is an array of doubles.
     */
    @Override
    public String toString() {
	if (_derivatives == null || _derivatives.length == 0) {
	    return super.toString();
	}
	StringBuffer derivatives = new StringBuffer("{");
	boolean first = true;
	for (int i = 0; i < _derivatives.length; i++) {
	    if (first) {
		first = false;
	    } else {
		derivatives.append(",");
	    }
	    derivatives.append(Double.toString(_derivatives[i]));
	}
	derivatives.append("}");
    	return "smoothToken(" 
    		+ super.toString()
    		+ ", "
    		+ derivatives.toString()
    		+ ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.
     *  The argument is guaranteed to be either a DoubleToken or
     *  a SmoothToken by the caller. If the argument is a DoubleToken,
     *  then its value is simply added to the value of this token, and
     *  a new SmoothToken is returned with the sum value and the derivatives
     *  of this token. If the argument is a SmoothToken, then returned SmoothToken
     *  will have the maximum of the number of derivatives of this token and
     *  the derivatives of the argument, and for derivatives given by both
     *  tokens, the derivative will be the sum of the two derivatives.
     *  @param rightArgument The token to add to this token.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _add(ScalarToken rightArgument) {
        final double sum = super.doubleValue() + ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, _derivatives);
            } else if (_derivatives == null) {
                // Just use the derivatives of that token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(sum, derivatives);
            }
            // Create a sum of derivatives.
            int max = derivatives.length;
            if (max < _derivatives.length) {
        	max = _derivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < _derivatives.length && i < derivatives.length) {
        	    result[i] = _derivatives[i] + derivatives[i];
        	} else if (i < _derivatives.length) {
        	    result[i] = _derivatives[i];
        	} else {
        	    result[i] = derivatives[i];
        	}
            }
            return new SmoothToken(sum, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(sum, _derivatives);
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token. It is assumed that
     *  the type of the argument is an SmoothToken
     *  @param divisor The token to divide this token by.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _divide(ScalarToken divisor) {
	// FIXME: Need to implement the rule in the class comment.
    	final double div = ((DoubleToken) divisor).doubleValue();
        final double quotient = super.doubleValue() / div;
        double[] der = new double[_derivatives.length];
        for(int i = 0; i < _derivatives.length; i++) {
            der[i] = _derivatives[i]/div;
        }
        return new SmoothToken(quotient, der);
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is SmoothToken.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected BooleanToken _isLessThan(ScalarToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException("Method not implemented.");
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  The derivatives
     *  of the result are calculated using the product rule.
     *  The argument is assumed to be a DoubleToken.
     *  It may also be a SmoothToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _multiply(ScalarToken rightArgument) {
        double x = doubleValue();
        double y = ((DoubleToken)rightArgument).doubleValue();
        double product = x*y;
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken)rightArgument).derivativeValues();
            // Check whether one or both tokens lack derivatives.
            if (_derivatives == null || _derivatives.length == 0) {
                if (derivatives == null || derivatives.length == 0) {
                    // Both lack derivatives.
                    return new SmoothToken(product);
                }
                // Only x lacks derivatives. Hence, x should scale y's derivatives.
                double[] result = new double[derivatives.length];
                for (int i = 0; i < derivatives.length; i++) {
            	    result[i] = derivatives[i]*x;
                }
                return new SmoothToken(product, result);
            }
            // Both have derivatives.
            int max = _derivatives.length;
            if (max < derivatives.length) {
        	max = derivatives.length;
            }
            double[] result = new double[max];
            double xdot = _derivatives[0];
            double ydot = derivatives[0];
    	    result[0] = _derivativeOfTheProduct(x, y, xdot, ydot);
            for (int i = 1; i < max; i++) {
        	// For the next higher-order derivatives, replace x with xdot,
        	// y with ydot, and get the next higher-order derivatives.
        	// FIXME: This is not correct! Fails for derivatives two or higher.
        	x = xdot;
        	y = ydot;
        	if (i+1 < _derivatives.length) {
        	    xdot = _derivatives[i+1];
        	} else {
        	    xdot = 0.0;
        	}
        	if (i+1 < derivatives.length) {
        	    ydot = derivatives[i+1];
        	} else {
        	    ydot = 0.0;
        	}
        	result[i] = _derivativeOfTheProduct(x, y, xdot, ydot);
            }
            return new SmoothToken(product, result);
        } else {
            // Assume the y derivatives are zero, so the returned result just
            // has the derivatives of this token scaled by y.
            if (_derivatives == null || _derivatives.length == 0) {
        	return new SmoothToken(product);
            }
            double[] result = new double[_derivatives.length];
            for (int i = 0; i < _derivatives.length; i++) {
        	result[i] = _derivatives[i]*y;
            }
            return new SmoothToken(product, result);
        }
    }
    
    /** Return the derivative of the product xy, given x, y, x', and y', implementing
     *  the product rule of calculus:
     *  <pre>
     *    (xy)' = x'y + xy'
     *  </pre>
     * @param x Multiplicand.
     * @param y Multiplicand.
     * @param xdot Derivative of the multiplicand.
     * @param ydot Derivative of the multiplicand.
     * @return Derivative of the product.
     */
    protected double _derivativeOfTheProduct(double x, double y, double xdot, double ydot) {
	return xdot*y + x*ydot;
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that
     *  the type of the argument is a DoubleToken.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new SmoothToken containing the result.
     */
    @Override
    protected ScalarToken _subtract(ScalarToken rightArgument) {
        final double difference = super.doubleValue() - ((DoubleToken) rightArgument).doubleValue();
        if (rightArgument instanceof SmoothToken) {
            double[] derivatives = ((SmoothToken) rightArgument).derivativeValues();
            if (derivatives == null) {
                // Just use the derivatives of this token.
                // This should be safe because, by policy, their value is immutable.
                return new SmoothToken(difference, _derivatives);
            } else if (_derivatives == null) {
                // The derivatives should be negated.
        	double[] result = new double[derivatives.length];
        	for (int i = 0; i < result.length; i++) {
        	    result[i] = - derivatives[i];
        	}
                return new SmoothToken(difference, result);
            }
            // Create a difference of derivatives.
            int max = derivatives.length;
            if (max < _derivatives.length) {
        	max = _derivatives.length;
            }
            double[] result = new double[max];
            for (int i=0; i < max; i++) {
        	if (i < _derivatives.length && i < derivatives.length) {
        	    result[i] = _derivatives[i] - derivatives[i];
        	} else if (i < _derivatives.length) {
        	    result[i] = _derivatives[i];
        	} else {
        	    result[i] = -derivatives[i];
        	}
            }
            return new SmoothToken(difference, result);
        } else {
            // Just use the derivatives of this token.
            // This should be safe because, by policy, their value is immutable.
            return new SmoothToken(difference, _derivatives);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The derivatives. */
    private double[] _derivatives;
}