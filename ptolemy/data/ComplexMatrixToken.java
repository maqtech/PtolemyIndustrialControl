/* A token that contains a 2-D Complex matrix.

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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;
import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// ComplexMatrixToken
/**
A token that contains a 2-D Complex matrix.

@author Yuhong Xiong
@version $Id$
@see ptolemy.math.Complex
*/
public class ComplexMatrixToken extends MatrixToken {

    /** Construct an ComplexMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0.0
     */
    public ComplexMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _value = new Complex[1][1];
        _value[0][0] = Complex.ZERO;
    }

    /** Construct a ComplexMatrixToken with the specified 2-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified matrix
     *   is null.
     */
    public ComplexMatrixToken(final Complex[][] value) {
        _initialize(value, DO_COPY);
    }

    /** Construct a ComplexMatrixToken with the specified 2-D matrix.
     *  If copy is DO_COPY, make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the matrix (do not copy
     *  its contents). This saves some time and memory.
     *  The argument matrix should NOT be modified after this constructor
     *  is called to preserve immutability.
     *  @exception NullPointerException If the specified matrix
     *   is null.
     */
    protected ComplexMatrixToken(final Complex[][] value, int copy) {
        _initialize(value, copy);
    }

    /** Construct an ComplexMatrixToken from the specified string.
     *  @param init A string expression of a 2-D complex matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D complex matrix.
     */
    public ComplexMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	ComplexMatrixToken token =
	                        (ComplexMatrixToken)tree.evaluateParseTree();
        Complex[][] value = token.complexMatrix();
        _initialize(value, DO_COPY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its dimension must be the
     *  same as this token.
     *  @param token The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public Token add(Token token) throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "add method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return token.addReverse(this);
        } else {
            // type of the specified token <= ComplexMatrixToken
            Complex[][] result = null;

            if (token instanceof ScalarToken) {
                Complex scalar = ((ScalarToken)token).complexValue();
                result = ComplexMatrixMath.add(_value, scalar);
            } else {
                // the specified token is not a scalar.
                ComplexMatrixToken tem = (ComplexMatrixToken)convert(token);

                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimension.");
                }

                result = ComplexMatrixMath.add(tem._getInternalComplexMatrix(),
                        _value);
            }
            return new ComplexMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than ComplexMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexMatrixToken.
     */
    public Token addReverse(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }
        // add is commutative on Complex matrix.
        return add(token);
    }

    /** Return the content of this token as a new 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    public Complex[][] complexMatrix() {
        return ComplexMatrixMath.allocCopy(_value);
    }

    /** Convert the specified token into an instance of ComplexMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of ComplexMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ComplexMatrixToken in the type hierarchy, it is converted to
     *  an instance of ComplexMatrixToken or one of the subclasses of
     *  ComplexMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a ComplexMatrixToken.
     *  @return A ComplexMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static Token convert(Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(BaseType.COMPLEX_MATRIX,
                token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("ComplexMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with ComplexMatrixToken " +
                    "in the type hierarchy.");
        }

        if (token instanceof ComplexMatrixToken) {
            return token;
        }

        // try Complex
        compare = TypeLattice.compare(BaseType.COMPLEX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            Complex[][] result = new Complex[1][1];
            ComplexToken tem = (ComplexToken)ComplexToken.convert(token);
            result[0][0] = tem.complexValue();
            return new ComplexMatrixToken(result);
        }

        // try DoubleMatrix
        compare = TypeLattice.compare(BaseType.COMPLEX_MATRIX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            DoubleMatrixToken tem =
                (DoubleMatrixToken)DoubleMatrixToken.convert(token);
            return new ComplexMatrixToken(tem.complexMatrix());
        }

        // The argument is below ComplexMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
                "ComplexMatrixToken.");
    }

    /** Return the type of this token.
     *  @return BaseType.COMPLEX_MATRIX
     */
    public Type getType() {
        return BaseType.COMPLEX_MATRIX;
    }

    /** Test if the content of this token is equal to that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the matrices are equal, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa.
     *  @param token The token with which to test equality.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not a matrix token; or lossless conversion is not possible.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if ( !(token instanceof MatrixToken) ||
                compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    token.getClass().getName());
        }

        if ( ((MatrixToken)token).getRowCount() != _rowCount ||
                ((MatrixToken)token).getColumnCount() != _columnCount) {
            return new BooleanToken(false);
        }

        if (compare == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
            // type of specified token <= ComplexMatrixToken
            ComplexMatrixToken tem = (ComplexMatrixToken)convert(token);
            return new BooleanToken(
                    ComplexMatrixMath.arePartsWithin(_value,
                            tem._getInternalComplexMatrix(), 0.0));
        }
    }

    /** Return the element of the matrix at the specified
     *  row and column in a ComplexToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A ComplexToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Token getElementAsToken(final int row, final int column)
            throws ArrayIndexOutOfBoundsException {
        return new ComplexToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The Complex at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Complex getElementAt(final int row, final int column) {
        return _value[row][column];
    }

    /** Return the number of columns in the matrix.
     *  @return The number of columns in the matrix.
     */
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public int getRowCount() {
        return _rowCount;
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its number of rows should
     *  be the same as this token's number of columns.
     *  @param token The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public final Token multiply(final Token token)
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
            // type of the specified token <= ComplexMatrixToken
            Complex[][] result = null;

            if (token.getType() == BaseType.COMPLEX) {
                // multiply by a complex number
                Complex c = ((ComplexToken)token).complexValue();
                result = ComplexMatrixMath.multiply(_value, c);
            } else if (token instanceof ScalarToken) {
                // multiply by a double
                double scalar = ((ScalarToken)token).doubleValue();
                result = ComplexMatrixMath.multiply(_value, scalar);
            } else {
                // the specified token is not a scalar.
                ComplexMatrixToken tem = (ComplexMatrixToken)convert(token);
                if (tem.getRowCount() != _columnCount) {
                    throw new IllegalActionException("Cannot multiply " +
                            "matrix with " + _columnCount +
                            " columns by a matrix with " +
                            tem.getRowCount() + " rows.");
                }

                result = ComplexMatrixMath.multiply(
                        _value, tem._getInternalComplexMatrix());
            }
            return new ComplexMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than ComplexMatrixToken.
     *  @param token The token to multiply this Token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexMatrixToken.
     */
    public final Token multiplyReverse(final Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }

        // Check if t is matrix. In that case we must convert t into a
        // ComplexMatrixToken because matrix multiplication is not
        // commutative.
        if (token instanceof ScalarToken) {
            // multiply is commutative on complex matrices, for scalar types.
            return multiply(token);
        } else {
            // the specified token is not a scalar
            ComplexMatrixToken tem = (ComplexMatrixToken)convert(token);
            if (tem.getColumnCount() != _rowCount) {
                throw new IllegalActionException("Cannot multiply " +
                        "matrix with " + tem.getColumnCount() +
                        " columns by a matrix with " +
                        _rowCount + " rows.");
            }
            return new ComplexMatrixToken(ComplexMatrixMath.multiply(
                    tem._getInternalComplexMatrix(), _value), DO_NOT_COPY);
        }
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its dimension must be the
     *  same as this token.
     *  @param token The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be subtracted from this token.
     */
    public final Token subtract(final Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "subtract method not supported between " +
                this.getClass().getName() + " and " +
                token.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            Token me = token.convert(this);
            return me.subtract(token);
        } else {
            // type of the specified token <= ComplexMatrixToken
            Complex[][] result = null;

            if (token instanceof ScalarToken) {
                Complex scalar = ((ScalarToken)token).complexValue();
                result = ComplexMatrixMath.add(_value, scalar.negate());
            } else {
                // the specified token is not a scalar.
                ComplexMatrixToken tem = (ComplexMatrixToken)convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot subtract two " +
                            "matrices with different dimensions.");
                }

                result = ComplexMatrixMath.subtract(_value,
                        tem._getInternalComplexMatrix());
            }
            return new ComplexMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  The type of the specified token must be lower than ComplexMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than ComplexMatrixToken.
     */
    public final Token subtractReverse(final Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }
        // add the argument Token to the negative of this Token
        ComplexMatrixToken negativeToken =
            new ComplexMatrixToken(
	                     ComplexMatrixMath.negative(_value), DO_NOT_COPY);
        return negativeToken.add(token);
    }


    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the left multiplicative
     *   identity.
     */
    public Token one() {
        return new ComplexMatrixToken(
                ComplexMatrixMath.identity(_rowCount), DO_NOT_COPY);
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the right
     *   multiplicative identity.
     */
    public Token oneRight() {
        return new ComplexMatrixToken(
                ComplexMatrixMath.identity(_columnCount), DO_NOT_COPY);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the additive identity.
     */
    public Token zero() {
        return new ComplexMatrixToken(
                ComplexMatrixMath.zero(_rowCount, _columnCount), DO_NOT_COPY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Return a reference to the internal 2-D matrix of complex numbers that
     *  represents this Token. Because no copying is done, the contents must
     *  NOT be modified to preserve the immutability of Token.
     *  @return A 2-D complex Java matrix.
     */
    protected Complex[][] _getInternalComplexMatrix() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(Complex[][] value, int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;

        if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = ComplexMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Complex[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
