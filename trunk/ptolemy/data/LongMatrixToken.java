/* A token that contains a 2-D long array.

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
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// LongMatrixToken
/**
A token that contains a 2-D long array.

FIXME: Except add() and addReverse(), other arithmetics operations are
not implemented yet. Those methods will be added after the corresponding
operations are added to the math package.

@author Yuhong Xiong
@version $Id$
*/
public class LongMatrixToken extends MatrixToken {

    /** Construct an LongMatrixToken with a one by one array. The
     *  only element in the array has value 0
     */
    public LongMatrixToken() {
	_rowCount = 1;
	_columnCount = 1;
	_value = new long[1][1];
	_value[0][0] = 0;
    }

    /** Construct a LongMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the copy,
     *  so changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public LongMatrixToken(long[][] value) {
        _initialize(value);
    }

    /** Construct a LongMatrixToken from the specified string.
     *  @param init A string expression of a 2-D long matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D long matrix.
     */
    public LongMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	LongMatrixToken token = (LongMatrixToken)tree.evaluateParseTree();
        long[][] value = token.longMatrix();
        _initialize(value);
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
	    // type of the specified token <= LongMatrixToken
	    long[][] result = null;

	    if (token instanceof ScalarToken) {
		long scalar = ((ScalarToken)token).longValue();
		result = new long[_rowCount][_columnCount];
		for (int i = 0; i < _rowCount; i++) {
		    for (int j = 0; j < _columnCount; j++) {
			result[i][j] = scalar + _value[i][j];
		    }
		}
	    } else {
		// the specified token is not a scalar.
		LongMatrixToken tem = (LongMatrixToken)this.convert(token);
	    	if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimension.");
	    	}

		result = tem.longMatrix();
		for (int i = 0; i < _rowCount; i++) {
		    for (int j = 0; j < _columnCount; j++) {
			result[i][j] += _value[i][j];
		    }
		}
	    }
	    return new LongMatrixToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than LongMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than LongMatrixToken.
     */
    public Token addReverse(Token token) throws IllegalActionException {
	int compare = TypeLattice.compare(this, token);
	if (! (compare == CPO.HIGHER)) {
	    throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
	}
	// add is commutative on long matrix.
	return add(token);
    }

    /** Convert the specified token into an instance of LongMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of LongMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below LongMatrixToken in the type hierarchy, it is converted to
     *  an instance of LongMatrixToken or one of the subclasses of
     *  LongMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a LongMatrixToken.
     *  @return A LongMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new LongMatrixToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("LongMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with LongMatrixToken in the " +
                    "type hierarchy.");
	}

	if (token instanceof LongMatrixToken) {
	    return token;
	}

	// try long
	compare = TypeLattice.compare(new LongToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    LongToken tem = (LongToken)LongToken.convert(token);
	    long[][] result = new long[1][1];
	    result[0][0] = tem.longValue();
	    return new LongMatrixToken(result);
	}

	// try IntMatrix
	compare = TypeLattice.compare(new IntMatrixToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    IntMatrixToken tem = (IntMatrixToken)IntMatrixToken.convert(token);
	    long[][] result = tem.longMatrix();
	    return new LongMatrixToken(result);
	}

	// The argument is below LongMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"LongMatrixToken.");
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    public Type getType() {
	return BaseType.LONG_MATRIX;
    }

    /** Test if the content of this token is equal to that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the arrays are equal, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa.
     *  @param token The token with which to test equality.
     *  @return A booleanToken containing the result.
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
	    // type of specified token <= LongMatrixToken
	    LongMatrixToken tem = (LongMatrixToken)convert(token);
	    long[][] array = tem.longMatrix();

	    for (int i = 0; i < _rowCount; i++) {
		for (int j = 0; j < _columnCount; j++) {
		    if (_value[i][j] != array[i][j]) {
			return new BooleanToken(false);
		    }
		}
	    }
	    return new BooleanToken(true);
	}
    }

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A LongToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
	return new LongToken(_value[row][column]);
    }

    /** Return the element of the contained array at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The long at the specified array entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public long getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the number of columns in the matrix.
     *  @return An integer.
     */
    public int getColumnCount() {
	return _columnCount;
    }

    /** Return the number of rows in the matrix.
     *  @return An integer.
     */
    public int getRowCount() {
	return _rowCount;
    }

    /** Return the content in the token as a 2-D long array.
     *  The returned array is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D long array.
     */
    public long[][] longMatrix() {
	long[][] array = new long[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
	 	array[i][j] = _value[i][j];
	    }
	}
	return array;
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new Token containing the left multiplicative identity.
     */
    public Token one() {
	long[][] result = new long[_rowCount][_rowCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _rowCount; j++) {
		result[i][j] = 0;
	    }
	    result[i][i] = 1;
	}
	return new LongMatrixToken(result);
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new Token containing the right multiplicative identity.
     */
    public Token oneRight() {
	long[][] result = new long[_columnCount][_columnCount];
	for (int i = 0; i < _columnCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = 0;
	    }
	    result[i][i] = 1;
	}
	return new LongMatrixToken(result);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
	long[][] result = new long[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = 0;
	    }
	}
	return new LongMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(long[][] value) {
	_rowCount = value.length;
	_columnCount = value[0].length;
	_value = new long[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		_value[i][j] = value[i][j];
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
