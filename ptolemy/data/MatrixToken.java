/* Abstract base class for tokens that contain a 2-D array.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/**
Abstract base class for tokens that contain a 2-D array.
This class defines methods for type conversion among different matrix
tokens. The implementation in this base class just throws an exception.
Derived classes should override those methods where the corresponding
conversion can be achieved without loss of information.

@author Yuhong Xiong
@version $Id$
*/
public abstract class MatrixToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** In derived classes that support conversion to a 2-D complex array,
     *  return a copy of the content of this token as a 2-D Complex array.
     *  In this base class, just throw an exception.
     *  @return A 2-D Complex array.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public Complex[][] complexMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
        " cannot be converted to a complex matrix.");
    }

    /** Throw an exception, since convertion to this class is not supported
     *  (the class is abstract).  This method overrides that in Token,
     *  which simply returns the argument.
     *  @param token A Token to be converted.
     *  @return nothing
     *  @exception IllegalActionException Always thrown.
     */
    public static Token convert(Token token) throws IllegalActionException {
        throw new IllegalActionException(token.getClass().getName() +
        " cannot connvert to a MatrixToken since that class is abstract.");
    }

    /** In derived classes that can be represented as a 2-D double array,
     *  return a representation of their contents as a such an array.
     *  In this base class, just throw an exception.
     *  @return A 2-D double array.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public double[][] doubleMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
        " cannot be converted to a double matrix.");
    }

    // Return the content in the token as a 2-D Fix array.
    // FIXME: uncomment this method after the Fix class is implemented.
    // public Fix[][] fixMatrix();

    /** In derived classes that can be represented as a 2-D integer array,
     *  return a representation of their contents as a such an array.
     *  In this base class, just throw an exception.
     *  @return A 2-D integer array.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public int[][] intMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
        " cannot be converted to an integer matrix.");
    }

    /** In derived classes that can be represented as a 2-D long array,
     *  return a representation of their contents as a such an array.
     *  In this base class, just throw an exception.
     *  @return A 2-D long array.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public long[][] longMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
        " cannot be converted to a long matrix.");
    }

    /** Return the number of columns of the contained matrix.
     *  @return An integer representing the number of columns.
     */
    public abstract int getColumnCount();

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A token containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public abstract Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException;

    /** Return the number of rows of the contained matrix.
     *  @return An integer representing the number of rows.
     */
    public abstract int getRowCount();

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  The implementation in this base class just throws an
     *  exception. This method should be overridden in the subclass
     *  when the right multiplicative identity exists.
     *  @return A new Token containing the right multiplicative identity.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public Token oneRight()
	    throws IllegalActionException {
	throw new IllegalActionException("Right multiplicative identity " +
		"not supported on " + getClass().getName() + " objects.");
    }
}
