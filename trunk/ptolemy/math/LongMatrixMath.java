/* A library for mathematical operations on matrices of longs.

Some algorithms are from

[1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
    Signal Processing". Prentice Hall. Englewood Cliffs, NJ, 1991.

This file was automatically generated with a preprocessor, so that 
similar matrix operations are supported on ints, longs, floats, and doubles. 

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Yellow (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// LongMatrixMath

/**
 * This class provides a library for mathematical operations on
 * matrices of longs.
 * <p>
 * Rows and column numbers of matrices are specified with zero-based indices.
 *
 * All calls expect matrix arguments to be non-null. In addition, all
 * rows of the matrix are expected to have the same number of columns.
 *
 * @author Jeff Tsay
 */

public class LongMatrixMath {

    // Private constructor prevents construction of this class.
    private LongMatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix A matrix of longs.
     *  @param z The long number to add.
     *  @return A new matrix of longs.
     */
    public static final long[][] add(long[][] matrix, long z) {
        long[][] retval = new long[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = matrix[i][j] + z;
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     *  @param matrix1 The first matrix of longs.
     *  @param matrix2 The second matrix of longs.
     *  @return A new matrix of longs.
     */
    public static final long[][] add(final long[][] matrix1, 
            final long[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        long[][] retval = new long[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                retval[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of longs.
     *  @return A new matrix of longs.
     */
    public static final long[][] allocCopy(final long[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix)) ;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  LongBinaryOperation to each element in the input matrix,     
     *  using z as the left operand in all cases and the matrix elements
     *  as the right operands (op.operate(z, matrix[i][j])).
     */
    public static final long[][] applyBinaryOperation(
            LongBinaryOperation op, final long z, final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(z, matrix[i][j]);
            }
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  LongBinaryOperation to each element in the input matrix,     
     *  using the matrix elements as the left operands and z as the right 
     *  operand in all cases (op.operate(matrix[i][j], z)).
     */
    public static final long[][] applyBinaryOperation(
            LongBinaryOperation op, final long[][] matrix, final long z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix[i][j], z);
            }
        }
        return retval;
    }
            
    /** Return a new array that is formed by applying an instance of a 
     *  LongBinaryOperation to the two matrices, element by element,
     *  using the elements of the first matrix as the left operands and the 
     *  elements of the second matrix as the right operands.
     *  (op.operate(matrix1[i][j], matrix2[i][j])).
     *  If the matrices are not the same size, throw an IllegalArgumentException.
     */
    public static final long[][] applyBinaryOperation(
            LongBinaryOperation op, final long[][] matrix1, final long[][] matrix2) {     
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);
        
        _checkSameDimension("applyBinaryOperation", matrix1, matrix2);      
            
        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix1[i][j], matrix2[i][j]);
            }
        }     
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a 
     *  LongUnaryOperation to each element in the input matrix 
     *  (op.operate(matrix[i][j])).
     */
    public static final long[][] applyUnaryOperation(
            final LongUnaryOperation op, final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = op.operate(matrix[i][j]);
            }
        }        
        return retval;
    }

    /** Return a new matrix that is the formed by bitwise ANDing z with each element
     *  of the input matrix (matrix[i][j] & z).
     */
    public static final long[][] bitwiseAnd(final long[][] matrix, final long z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
    
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix[i][j] & z;
            }
        }
        
        return retval;
    }
      
    /** Return a new array that is the element-by-element bitwise AND of the two
     *  input matrices (matrix1[i][j] & matrix2[i][j]).
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] bitwiseAnd(final long[][] matrix1,
     final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);
        
        _checkSameDimension("bitwiseAnd", matrix1, matrix2);
        
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix1[i][j] & matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new array that formed by the bitwise complement of each element in
     *  the input matrix (~matrix[i][j].
     */
    public static final long[][] bitwiseComplement(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
               
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = ~matrix[i][j];
            }
        }
        return retval;
    }

    /** Return a new matrix that is the formed by bitwise ORing z with each element
     *  of the input matrix (matrix[i][j] | z).
     */
    public static final long[][] bitwiseOr(final long[][] matrix, final long z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
    
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix[i][j] | z;
            }
        }
        
        return retval;
    }
      
    /** Return a new array that is the element-by-element bitwise OR of the two
     *  input matrices (matrix1[i][j] | matrix2[i][j]).
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] bitwiseOr(final long[][] matrix1,
     final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);
        
        _checkSameDimension("bitwiseOr", matrix1, matrix2);
        
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix1[i][j] | matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new matrix that is the formed by bitwise XORing z with each element
     *  of the input matrix (matrix[i][j] ^ z).
     */
    public static final long[][] bitwiseXor(final long[][] matrix, final long z) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
    
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix[i][j] ^ z;
            }
        }
        
        return retval;
    }
      
    /** Return a new array that is the element-by-element bitwise XOR of the two
     *  input matrices (matrix1[i][j] & matrix2[i][j]).
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] bitwiseXor(final long[][] matrix1,
            final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);
        
        _checkSameDimension("bitwiseXor", matrix1, matrix2);
        
        long[][] retval = new long[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; i < columns; j++) {
                retval[i][j] = matrix1[i][j] ^ matrix2[i][j];
            }
        }
        return retval;
    }


    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of longs.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final long[][] crop(final long[][] matrix,
            final int rowStart, final int colStart,
            final int rowSpan, final int colSpan) {
        long[][] retval = new long[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                    retval[i], 0, colSpan);
        }
        return retval;
    }


    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] divideElements(final long[][] matrix1,
            final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The longs are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m, n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param A matrix of longs.
     *  @return A new array of longs.
     */
    public static final long[] fromMatrixToArray(final long[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The longs are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of longs.
     *  @return A new array of longs.
     */
    public static final long[] fromMatrixToArray(final long[][] matrix,
            int maxRow, int maxCol) {
        long[] retval = new long[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, retval, i * maxCol, maxCol);
        }
        return retval;
    }


    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    public static final long[][] identity(final int dim) {
        long[][] retval = new long[dim][dim];
        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            retval[i][i] = 1L;
        }
        return retval;
    }


    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of longs, used as the destination.
     *  @param srcMatrix A matrix of longs, used as the source.
     */
    public static final void matrixCopy(final long[][] srcMatrix,
            final long[][] destMatrix) {
        matrixCopy(srcMatrix, 0, 0, destMatrix, 0, 0, _rows(srcMatrix),
                _columns(srcMatrix));
    }

    /** Replace the first matrix argument's values, in the specified row
     *  and column range, with the second matrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param srcMatrix A matrix of longs, used as the destination.
     *  @param srcRowStart An int specifying the starting row of the source.
     *  @param srcColStart An int specifying the starting column of the
     *  source.
     *  @param destMatrix A matrix of longs, used as the destination.
     *  @param destRowStart An int specifying the starting row of the dest.
     *  @param destColStart An int specifying the starting column of the
     *         dest.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final void matrixCopy(final long[][] srcMatrix,
            final int srcRowStart, final int srcColStart,
            final long[][] destMatrix,
            final int destRowStart, final int destColStart,
            final int rowSpan, final int colSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                    destMatrix[destRowStart + i], destColStart,
                    colSpan);
        }
    }

    /** Return a new matrix that is constructed by computing the remainders between
     *  each element in the matrix and z.
     */
    public static final long[][] moduloElements(final long[][] matrix, 
            final long z) {
        long[][] retval = new long[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                retval[i][j] = matrix[i][j] % z;
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed by computing the remainders between
     *  each element in the first matrix argument and the corresponding element in the 
     *  second matrix argument. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] modulo(final long[][] matrix1, 
            final long[][] matrix2) {
        _checkSameDimension("moduloElements", matrix1, matrix2);
       
        long[][] retval = new long[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                retval[i][j] = matrix1[i][j] % matrix2[i][j];
            }
        }
        return retval;
    }    

    
    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scalefactor.
     */
    public static final long[][] multiply(final long[][] matrix,
            final long scalefactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix[i][j] * scalefactor;
            }
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     */
    public static final long[] multiply(final long[][] matrix,
            final long[] array) {

        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
            throw new IllegalArgumentException(
                    "preMultiply : array does not have the same number of " +
                    "elements (" + array.length + ") as the number of rows " +
                    "of the matrix (" + rows + ")");
        }

        long[] retval = new long[columns];
        for (int i = 0; i < columns; i++) {
            long sum = 0L;
            for (int j = 0; j < rows; j++) {
                sum += matrix[j][i] * array[j];
            }
            retval[i] = sum;
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     */
    public static final long[] multiply(final long[] array,
            final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
            throw new IllegalArgumentException(
                    "postMultiply() : array does not have the same number " +
                    "of elements (" + array.length + ") as the number of " +
                    "columns of the matrix (" + columns + ")");
        }

        long[] retval = new long[rows];
        for (int i = 0; i < rows; i++) {
            long sum = 0L;
            for (int j = 0; j < columns; j++) {
                sum += matrix[i][j] * array[j];
            }
            retval[i] = sum;
        }
        return retval;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  @param matrix1 The first matrix of longs.
     *  @param matrix2 The second matrix of longs.
     *  @return A new matrix of longs.
     */
    public static final long[][] multiply(long[][] matrix1,
            long[][] matrix2) {
        long[][] retval = new long[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                long sum = 0L;
                for (int k = 0; k < matrix2.length; k++) {
                    sum += matrix1[i][k] * matrix2[k][j];
                }
                retval[i][j] = sum;
            }
        }
        return retval;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments. 
     *  If the two matrices are not the same size, throw an IllegalArgumentException.     
     */
    public static final long[][] multiplyElements(final long[][] matrix1,
            final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     */
    public static final long[][] negative(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
    
        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = -matrix[i][j];
            }
        }
        return retval;
    }





    /** Return a new matrix that is constructed from the argument by
     *  arithmetically shifting the elements in the matrix by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (arithmetically, with the >>> operator) by the absolute 
     *  value of the second argument. If the second argument is 0, no operation is 
     *  performed (the matrix is just copied).
     *  @param matrix A first matrix of longs.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new matrix of longs.
     */
    public static final long[][] shiftArithmetic(final long[][] matrix, 
            final int shiftAmount) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
            
        long[][] retval = new long[rows][columns];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < rows; i++) {
               for (int j = 0; j < columns; j++) {
                   retval[i][j] = matrix[i][j] << shiftAmount;
               }
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < rows; i++) {
               for (int j = 0; j < columns; j++) {
                   retval[i][j] = matrix[i][j] >>> -shiftAmount;
               }
           }
        }
        
        return retval;
    }
       
    /** Return a new matrix that is constructed from the argument by
     *  logically shifting the elements in the matrix by the second argument.
     *  If the second argument is positive, the elements are shifted left by
     *  the second argument. If the second argument is negative, the elements
     *  are shifted right (logically, with the >> operator) by the absolute value 
     *  of the second  argument. If the second argument is 0, no operation is performed
     *  (the matrix is just copied).
     *  @param matrix A first matrix of longs.
     *  @param shiftAmount The amount to shift by, positive for left shift, 
     *  negative for right shift.
     *  @return A new matrix of longs.
     */
    public static final long[][] shiftLogical(final long[][] matrix, 
     final int shiftAmount) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        long[][] retval = new long[rows][columns];
        
        if (shiftAmount >= 0) {        
           for (int i = 0; i < rows; i++) {
               for (int j = 0; j < columns; j++) {
                   retval[i][j] = matrix[i][j] << shiftAmount;
               }
           }
        } else if (shiftAmount < 0) {
           for (int i = 0; i < rows; i++) {
               for (int j = 0; j < columns; j++) {
                   retval[i][j] = matrix[i][j] >> -shiftAmount;
               }
           }
        }
        
        return retval;
    }   

    
    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  
     *  If the two matrices are not the same size, throw an IllegalArgumentException.          
     */
    public static final long[][] subtract(final long[][] matrix1,
            final long[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        long[][] retval = new long[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return retval;
    }

    /** Return a new matrix that is formed by converting the longs in
     *  the argument matrix to doubles.
     *  @param array An matrix of long.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toDoubleMatrix(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        double[][] retval = new double[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = (double) matrix[i][j];
            }
        }
        return retval;
    }    

    
    /** Return a new matrix that is formed by converting the longs in
     *  the argument matrix to floats.
     *  @param array An matrix of long.
     *  @return A new matrix of floats.
     */
    public static final float[][] toFloatMatrix(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        float[][] retval = new float[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = (float) matrix[i][j];
            }
        }
        return retval;
    }            

    
    /** Return a new matrix that is formed by converting the longs in
     *  the argument matrix to integers.
     *  @param array An matrix of long.
     *  @return A new matrix of integers.
     */
    public static final int[][] toIntegerMatrix(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);
 
        int[][] retval = new int[rows][columns];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[i][j] = (int) matrix[i][j];
            }
        }
        return retval;
    }            

    

    
    /** Return a new matrix of longs that is initialized from a 1-D array.
     *  The format of the array must be (0, 0), (0, 1), ..., (0, n-1), (1, 0),
     *  (1, 1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of longs.
     *  @param rows An integer representing the number of rows of the new matrix.
     *  @param cols An integer representing the number of columns of the new matrix.
     *  @return A new matrix of longs.
     */
    public static final long[][] toMatrixFromArray(long[] array, int rows,
            int cols) {
        long[][] retval = new long[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, retval[i], 0, cols);
        }
        return retval;
    }

    /** Return a new String representing the matrix, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final long[][] matrix) {
        return toString(matrix, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the matrix, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final long[][] matrix,
            final ArrayStringFormat asf) {
        StringBuffer sb = new StringBuffer();
        sb.append(asf.matrixBeginString());

        for (int i = 0; i < _rows(matrix); i++) {

            sb.append(asf.vectorBeginString());
            for (int j = 0; j < _columns(matrix); j++) {
                sb.append(asf.longString(matrix[i][j]));

                if (j < (_columns(matrix) - 1)) {
                    sb.append(asf.elementDelimiterString());
                }
            }

            sb.append(asf.vectorEndString());

            if (i < (_rows(matrix) - 1)) {
                sb.append(asf.vectorDelimiterString());
            }
        }

        sb.append(asf.matrixEndString());

        return new String(sb);
    }


    /** Return the trace of a square matrix, which is the sum of the
     *  diagonal entries A<sub>11</sub> + A<sub>22</sub> + ... + A<sub>nn</sub>
     *  Throw an IllegalArgumentException if the matrix is not square.
     *  Note that the trace of a matrix is equal to the sum of its eigenvalues.
     */
    public static final long trace(final long[][] matrix) {
        int dim = _checkSquare("trace", matrix);
        long sum = 0L;

        for (int i = 0; i < dim; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix. If the input matrix is m x n, the output matrix will be 
     *  n x m.
     */
    public static final long[][] transpose(final long[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        long[][] retval = new long[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                retval[j][i] = matrix[i][j];
            }
        }
        return retval;
    }

    /** Returns true iff the differences of all corresponding elements of
     *  2 matrices, that are of the same size, are all within a constant range,
     *  [-R, R], where R is the allowed error. The specified absolute
     *  difference must be non-negative.
     *  More concisely, abs(M1[i, j] - M2[i, j]) must be within [R, R]
     *  for 0 <= i < m and 0 <= j <n where M1 and M2 are both m x n matrices.
     *  @param matrix1 A matrix of longs.
     *  @param matrix2 A matrix of longs.
     *  @param absoluteError A long indicating the absolute value of the
     *  allowed error.
     *  @return A boolean condition.
     */
    public static final boolean within(final long[][] matrix1, 
            final long[][] matrix2, long absoluteError) {
        if (absoluteError < 0L) {
            throw new IllegalArgumentException(
                    "within(): absoluteError (" + absoluteError +
                    " must be non-negative.");
        }

        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(matrix1[i][j] - matrix2[i][j]) > absoluteError) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns true iff the differences of all corresponding elements of
     *  2 matrices, that are of the same size, are all within the range
     *  specificed by the corresponding values of the error matrix. The
     *  error matrix may contain negative entries; the absolute value
     *  is used.
     *  More concisely, abs(M1[i, j] - M2[i, j]) must be
     *  within [-E[i,j], E[i,j]], for 0 <= i < m and 0 <= j < n
     *  where M1, M2, and E are all m x n matrices.
     *  @param matrix1 A matrix of longs.
     *  @param matrix2 A matrix of longs.
     *  @param errorMatrix A matrix of longs.
     *  @return A boolean condition.
     */
    public static final boolean within(final long[][] matrix1, 
            final long[][] matrix2, final long[][] errorMatrix) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("within", matrix1, matrix2);
        _checkSameDimension("within", matrix1, errorMatrix);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Math.abs(matrix1[i][j] - matrix2[i][j]) >
                        Math.abs(errorMatrix[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return the number of columns of a matrix. */
    private static final int _columns(final long[][] matrix) {
        return matrix[0].length;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of longs.
     *  @param matrix2 A matrix of longs.
     */
    private static final void _checkSameDimension(final String caller,
             final long[][] matrix1, final long[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
            throw new IllegalArgumentException(
                    "ptolemy.math.longMatrixMath." + caller + "() : one matrix " +
                    _dimensionString(matrix1) +
                    " is not the same size as another matrix " +
                    _dimensionString(matrix2) + ".");
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of longs.
     *  @return The dimension of the square matrix.
     */
    private static final int _checkSquare(final String caller, 
             final long[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.LongMatrixMath." + caller +
                    "() : matrix argument " + _dimensionString(matrix) +
                    " is not a square matrix.");
        }
        return _rows(matrix);
    }

    private static final String _dimensionString(final long[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }


    
    /** Return the number of rows of a matrix. */
    private static final int _rows(final long[][] matrix) {
        return matrix.length;
    }
}
