/* A library for mathematical operations on matrices.

Some algorithms are from :

[1] Embree, Paul M. and Bruce Kimble. "C Language Algorithms for Digital
    Signal Processing". Prentice Hall. Englewood Cliffs, NJ, 1991.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// MatrixMath
/**
 * This class provides a library for mathematical operations on
 * matrices of doubles.
 * <p>
 * Rows and column numbers of matrices are specified with zero-based indices.
 *
 * All calls expect matrix arguments to be non-null. In addition, all
 * rows of the matrix are expected to have the same number of columns.
 *
 * @author Jeff Tsay
 * @version $Id$
 */

public class MatrixMath {

    // Private constructor prevents construction of this class.
    private MatrixMath() {}

    /** Return a new matrix that is constructed from the argument by
     *  adding the second argument to every element.
     *  @param matrix An array of doubles.
     *  @param z The double number to add.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(double[][] matrix, double z) {
        double[][] result = new double[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                result[i][j] = matrix[i][j] + z;
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  adding the second matrix to the first one. The matrices must be
     *  of the same size.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] add(double[][] matrix1, double[][] matrix2) {
        _checkSameDimension("add", matrix1, matrix2);

        double[][] result = new double[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                result[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is a copy of the matrix argument.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] allocCopy(double[][] matrix) {
        return crop(matrix, 0, 0, _rows(matrix), _columns(matrix)) ;
    }

    /** Return a new matrix that is a sub-matrix of the input
     *  matrix argument. The row and column from which to start
     *  and the number of rows and columns to span are specified.
     *  @param matrix A matrix of doubles.
     *  @param rowStart An int specifying which row to start on.
     *  @param colStart An int specifying which column to start on.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final double[][] crop(double[][] matrix,
                                        int rowStart, int colStart,
                                        int rowSpan, int colSpan) {
        double[][] retval = new double[rowSpan][colSpan];
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(matrix[rowStart + i], colStart,
                             retval[i], 0, colSpan);
        }
        return retval;
    }

    /** Return the determinate of a square matrix.
     *  This algorithm uses LU decomposition, and is taken from [1]
     *  @param matrix A matrix of doubles.
     *  @return The determinate of the matrix.
     */
    public static final double determinate(double[][] matrix) {
         _checkSquare("determinate", matrix);

        double[][] a;
        double det = 1.0;
        int n = _rows(matrix);

        a = allocCopy(matrix);

        for (int pivot = 0; pivot < n-1; pivot++) {
            // find the biggest absolute pivot
            double big = Math.abs(a[pivot][pivot]);
            int swapRow = 0; // initialize for no swap
            for (int row = pivot + 1; row < n; row++) {
                double absElement = Math.abs(a[row][pivot]);
                if (absElement > big) {
                   swapRow = row;
                   big = absElement;
                }
            }

            // unless swapRow is still zero we must swap two rows
            if (swapRow != 0) {
               double[] aPtr = a[pivot];
               a[pivot] = a[swapRow];
               a[swapRow] = aPtr;

               // change sign of determinate because of swap
               det *= -a[pivot][pivot];
            } else {
               // calculate the determinate by the product of the pivots
               det *= a[pivot][pivot];
            }

            // if almost singular matrix, give up now

            // FIXME use epsilon instead of this ugly constant
            if (Math.abs(det) < 1.0e-50) {
               return det;
            }

            double pivotInverse = 1.0 / a[pivot][pivot];
            for (int col = pivot + 1; col < n; col++) {
                a[pivot][col] *= pivotInverse;
            }

            for (int row = pivot + 1; row < n; row++) {
                double temp = a[row][pivot];
                for (int col = pivot + 1; col < n; col++) {
                    a[row][col] -= a[pivot][col] * temp;
                }
            }
        }

        // last pivot, no reduction required
        det *= a[n-1][n-1];

        return det;
    }

    /** Return a new matrix that is constructed by element by element
     *  division of the two matrix arguments. Each element of the
     *  first matrix is divided by the corresponding element of the
     *  second matrix.  The matrices must be of the same size.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] divideElements(double[][] matrix1,
                                                  double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("divideElements", matrix1, matrix2);

        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix1[i][j] / matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a (m,n) matrix :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] fromMatrixToArray(double[][] matrix) {
        return fromMatrixToArray(matrix, _rows(matrix), _columns(matrix));
    }

    /** Return a new array that is filled with the contents of the matrix.
     *  The maximum numbers of rows and columns to copy are specified so
     *  that entries lying outside of this range can be ignored. The
     *  maximum rows to copy cannot exceed the number of rows in the matrix,
     *  and the maximum columns to copy cannot exceed the number of columns
     *  in the matrix.
     *  The doubles are stored row by row, i.e. using the notation
     *  (row, column), the entries of the array are in the following order
     *  for a matrix, limited to m rows and n columns :
     *  (0, 0), (0, 1), (0, 2), ... , (0, n-1), (1, 0), (1, 1), ..., (m-1)(n-1)
     *  @param matrix A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] fromMatrixToArray(double[][] matrix, int maxRow,
                                                   int maxCol) {
        double[] retval = new double[maxRow * maxCol];
        for (int i = 0; i < maxRow; i++) {
            System.arraycopy(matrix[i], 0, retval, i * maxCol, maxCol);
        }
        return retval;
    }

    /** Return a new matrix, which is defined by Aij = 1/(i+j+1),
     *  the Hilbert matrix. The matrix is square with one
     *  dimension specifier required.
     *  @param dim An int
     *  @return A new Hilbert matrix of doubles
     */
    public static final double[][] hilbert(int dim) {
        double[][] retval = new double[dim][dim];
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                retval[i][j] = 1.0 / (double) (i + j + 1);
            }
        }
        return retval;
    }

    /** Return an identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  @param dim An int
     *  @return A new identity matrix of doubles
     */
    public static final double[][] identity(int dim) {
        double[][] retval = new double[dim][dim];
        // we rely on the fact Java fills the allocated matrix with 0's
        for (int i = 0; i < dim; i++) {
            retval[i][i] = 1.0;
        }
        return retval;
    }

    /** Return a new matrix that is constructed by inverting the input
     *  matrix. If the input matrix is singular, null is returned.
     *  This method is from [1]
     *  @param matrix A matrix of doubles
     *  @return A new matrix of doubles, or null if no inverse exists
     */
    public static final double[][] inverse(double[][] A) {
        _checkSquare("inverse", A);

        int n = _rows(A);

        double[][] Ai = allocCopy(A);

        System.out.println(toString(Ai));

        // We depend on each of the elements being initialized to 0
        int[] pivotFlag = new int[n];
        int[] swapCol = new int[n];
        int[] swapRow = new int[n];

        int irow = 0, icol = 0;

        for (int i = 0; i < n; i++) { // n iterations of pivoting
            // find the biggest pivot element
            double big = 0.0;
            for (int row = 0; row < n; row++) {
                if (pivotFlag[row] == 0) {
                    for (int col = 0; col < n; col++) {
                        if (pivotFlag[col] == 0) {
                            double absElement = Math.abs(Ai[row][col]);
                            if (absElement >= big) {
                                big = absElement;
                                irow = row;
                                icol = col;
                            }
                        }
                    }
                }
            }
            pivotFlag[icol]++;

            // swap rows to make this diagonal the biggest absolute pivot
            if (irow != icol) {
                for (int col = 0; col < n; col++) {
                    double temp = Ai[irow][col];
                    Ai[irow][col] = Ai[icol][col];
                    Ai[icol][col] = temp;
                }
            }

            // store what we swapped
            swapRow[i] = irow;
            swapCol[i] = icol;

            // if the pivot is zero, the matrix is singular
            if (Ai[icol][icol] == 0.0) {
               return null;
            }

            // divide the row by the pivot
            double pivotInverse = 1.0 / Ai[icol][icol];
            Ai[icol][icol] = 1.0; // pivot = 1 to avoid round off
            for (int col = 0; col < n; col++) {
                Ai[icol][col] *= pivotInverse;
            }

            // fix the other rows by subtracting
            for (int row = 0; row < n; row++) {
                if (row != icol) {
                   double temp = Ai[row][icol];
                   Ai[row][icol] = 0.0;
                   for (int col = 0; col < n; col++) {
                       Ai[row][col] -= Ai[icol][col] * temp;
                   }
                }
            }
        }

        // fix the effect of all the swaps for final answer
        for (int swap = n - 1; swap >= 0; swap--) {
            if (swapRow[swap] != swapCol[swap]) {
                for (int row = 0; row < n; row++) {
                    double temp = Ai[row][swapRow[swap]];
                    Ai[row][swapRow[swap]] = Ai[row][swapCol[swap]];
                    Ai[row][swapCol[swap]] = temp;
                }
            }
        }

        return Ai;
    }

    /** Replace the first matrix argument elements with the values of
     *  the second matrix argument. The second matrix argument must be
     *  large enough to hold all the values of second matrix argument.
     *  @param destMatrix A matrix of doubles, used as the destination.
     *  @param srcMatrix A matrix of doubles, used as the source.
     */
    public static final void matrixCopy(double[][] srcMatrix,
                                        double[][] destMatrix) {
        matrixCopy(srcMatrix, 0, 0, destMatrix, 0, 0, _rows(srcMatrix),
                   _columns(srcMatrix));
    }

    /** Replace the first matrix argument's values, in the specified row
     *  and column range, with the second matrix argument's values, starting
     *  from specified row and column of the second matrix.
     *  @param srcMatrix A matrix of doubles, used as the destination.
     *  @param srcRowStart An int specifying the starting row of the source.
     *  @param srcColStart An int specifying the starting column of the
     *  source.
     *  @param destMatrix A matrix of doubles, used as the destination.
     *  @param destRowStart An int specifying the starting row of the dest.
     *  @param destColStart An int specifying the starting column of the
     *         dest.
     *  @param rowSpan An int specifying how many rows to copy.
     *  @param colSpan An int specifying how many columns to copy.
     */
    public static final void matrixCopy(double[][] srcMatrix,
                                        int srcRowStart, int srcColStart,
                                        double[][] destMatrix,
                                        int destRowStart, int destColStart,
                                        int rowSpan, int colSpan) {
        // We should verify the parameters here
        for (int i = 0; i < rowSpan; i++) {
            System.arraycopy(srcMatrix[srcRowStart + i], srcColStart,
                             destMatrix[destRowStart + i], destColStart,
                             colSpan);
        }
    }

    /** Return a new matrix that is constructed by multiplying the matrix
     *  by a scalefactor.
     *  @param matrix A matrix of doubles.
     *  @scalefactor The constant by which to multiply the matrix.
     */
    public static final double[][] multiply(double[][] matrix,
                                            double scalefactor) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix[i][j] * scalefactor;
            }
        }
        return result;
    }

    /** Return a new array that is constructed from the argument by
     *  pre-multiplying the array (treated as a row vector) by a matrix.
     *  The number of rows of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of columns of the matrix.
     *  @param matrix A matrix of doubles.
     *  @param array An array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] multiply(double[][] matrix,
                                          double[] array) {

        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (rows != array.length) {
           throw new IllegalArgumentException(
            "preMultiply : array does not have the same number of elements (" +
            array.length + ") as the number of rows of the matrix (" + rows +
            ")");
        }

        double[] result = new double[columns];
        for (int i = 0; i < columns; i++) {
            double sum = 0.0;
            for (int j = 0; j < rows; j++) {
                sum += matrix[j][i] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Return a new array that is constructed from the argument by
     *  post-multiplying the matrix by an array (treated as a row vector).
     *  The number of columns of the matrix must equal the number of elements
     *  in the array. The returned array will have a length equal to the number
     *  of rows of the matrix.
     *  @param array An array of doubles.
     *  @param matrix A matrix of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] multiply(double[] array,
                                          double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        if (columns != array.length) {
           throw new IllegalArgumentException(
            "postMultiply() : array does not have the same number of elements (" +
            array.length + ") as the number of columns of the matrix (" +
            columns + ")");
        }

        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < columns; j++) {
                sum += matrix[i][j] * array[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  multiplying the first matrix by the second one.
     *  Note this operation is not commutative,
     *  so care must be taken in the ordering of the arguments.
     *  The number of columns of matrix1
     *  must equal the number of rows of matrix2. If matrix1 is of
     *  size m x n, and matrix2 is of size n x p, the returned matrix
     *  will have size m x p.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] multiply(double[][] matrix1,
                                            double[][] matrix2) {
        double[][] result = new double[_rows(matrix1)][matrix2[0].length];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                double sum = 0.0;
                for (int k = 0; k < matrix2.length; k++) {
                    sum += matrix1[i][k] * matrix2[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed by element by element
     *  multiplication of the two matrix arguments. The matrices must be
     *  of the same size.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] multiplyElements(double[][] matrix1,
                                                    double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix1);

        _checkSameDimension("multiplyElements", matrix1, matrix2);

        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrix1[i][j] * matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is the additive inverse of the
     *  argument matrix.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] negative(double[][] matrix) {
        double[][] result = new double[_rows(matrix)][_columns(matrix)];
        for (int i = 0; i < _rows(matrix); i++) {
            for (int j = 0; j < _columns(matrix); j++) {
                result[i][j] = -matrix[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix that is constructed from the argument by
     *  subtracting the second matrix from the first one.  The matrices must be
     *  of the same size.
     *  @param matrix1 The first matrix of doubles.
     *  @param matrix2 The second matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] subtract(double[][] matrix1,
                                            double[][] matrix2) {
        _checkSameDimension("subtract", matrix1, matrix2);

        double[][] result = new double[_rows(matrix1)][_columns(matrix1)];
        for (int i = 0; i < _rows(matrix1); i++) {
            for (int j = 0; j < _columns(matrix1); j++) {
                result[i][j] = matrix1[i][j] - matrix2[i][j];
            }
        }
        return result;
    }

    /** Return a new matrix of doubles that is initialized from a 1-D array.
     *  The format of the array must be (0,0), (0,1), ..., (0, n-1), (1,0),
     *  (1,1), ..., (m-1, n-1) where the output matrix is to be m x n and
     *  entries are denoted by (row, column).
     *  @param array An array of doubles.
     *  @param rows An int.
     *  @param cols An int.
     *  @return A new matrix of doubles.
     */
    public static final double[][] toMatrixFromArray(double[] array, int rows,
                                                     int cols) {
        double[][] retval = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, retval[i], 0, cols);
        }
        return retval;
    }

  /** Return a new String representing the matrix, formatted as
   *  in Java array initializers.
   */
  public static final String toString(double[][] matrix) {
    return toString(matrix, ArrayStringFormat.javaASFormat);
  }

  /** Return a new String representing the matrix, formatted as
   *  specified by the ArrayStringFormat argument.
   *  To get a String in the Ptolemy expression language format,
   *  call this method with ArrayStringFormat.exprASFormat as the
   *  format argument.
   */
  public static final String toString(double[][] matrix,
   ArrayStringFormat asf) {
    StringBuffer sb = new StringBuffer();
    sb.append(asf.matrixBeginString());

        for (int i = 0; i < _rows(matrix); i++) {

            // Replace with ArrayMath.toString(matrix[i]) when it gets in line

            sb.append(asf.vectorBeginString());
            for (int j = 0; j < _columns(matrix); j++) {
               sb.append(asf.doubleString(matrix[i][j]));

               if (j < (_columns(matrix) - 1)) {
                  sb.append(asf.elementDeliminatorString());
               }
            }

            sb.append(asf.vectorEndString());

            if (i < (_rows(matrix) - 1)) {
               sb.append(asf.vectorDeliminatorString());
            }
        }

        sb.append(asf.matrixEndString());

        return new String(sb);
    }


  /** Return the trace of a square matrix, which is the sum of the
   *  diagonal entries a<sub>11</sub> + <sub>a22</sub> + ... + a<sub>nn</sub>
   *  Throw an IllegalArgumentException if the matrix is not square.
   *  @param matrix A matrix of doubles.
   *  @return The trace of the matrix.
   */
  public static final double trace(double[][] matrix) {
    int dim = _checkSquare("trace", matrix);
    double sum = 0.0;

    for (int i = 0; i < dim; i++) {
        sum += matrix[i][i];
    }
    return sum;
  }

    /** Return a new matrix that is constructed by transposing the input
     *  matrix.
     *  @param matrix A matrix of doubles.
     *  @return A new matrix of doubles.
     */
    public static final double[][] transpose(double[][] matrix) {
        int rows = _rows(matrix);
        int columns = _columns(matrix);

        double[][] result = new double[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    /** Returns true iff the differences of all corresponding elements of
     *  2 matrices, that are of the same size, are all within a constant range,
     *  [-R, R], where R is the allowed error. The specified absolute
     *  difference must be non-negative.
     *  More concisely, abs(M1[i,j] - M2[i,j]) must be within [R, R]
     *  for 0<=i<m and 0<=j<n where M1 and M2 are both m x n matrices.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @param absoluteError A double indicating the absolute value of the
     *  allowed error.
     *  @return A boolean condition.
     */
    public static final boolean within(double[][] matrix1, double[][] matrix2,
                                       double absoluteError) {
        if (absoluteError < 0.0) {
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
     *  More concisely, abs(M1[i,j] - M2[i,j]) must be within [-E[i,j], E[i,j]],
     *  for 0<=i<m and 0<=j<n where M1, M2, and E are all m x n matrices.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     *  @param errorMatrix A matrix of doubles.
     *  @return A boolean condition.
     */
    public static final boolean within(double[][] matrix1, double[][] matrix2,
                                       double[][] errorMatrix) {
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

    /** Return the number of columns of a matrix.
     *  @param matrix A matrix of doubles.
     *  @return An int.
     */
    private static final int _columns(double[][] matrix) {
        return matrix[0].length;
    }

    /** Check that the two matrix arguments are of the same dimension.
     *  If they are not, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix1 A matrix of doubles.
     *  @param matrix2 A matrix of doubles.
     */
    private static final void _checkSameDimension(String caller,
                                                  double[][] matrix1,
                                                  double[][] matrix2) {
        int rows = _rows(matrix1);
        int columns = _columns(matrix2);

        if ((rows != _rows(matrix2)) || (columns != _columns(matrix2))) {
           throw new IllegalArgumentException(
            "ptolemy.math.MatrixMath." + caller + "() : one matrix " +
            _dimensionString(matrix1) +
            " is not the same size as another matrix " +
            _dimensionString(matrix2));
        }
    }

    /** Check that the argument matrix is a square matrix. If the matrix is not
     *  square, an IllegalArgumentException is thrown.
     *  @param caller A string representing the caller method name.
     *  @param matrix A matrix of doubles.
     *  @return The dimension of the square matrix.
     */
    private static final int _checkSquare(String caller, double[][] matrix) {
        if (_rows(matrix) != _columns(matrix)) {
           throw new IllegalArgumentException(
           "ptolemy.math.MatrixMath." + caller + "() : matrix argument "
           + _dimensionString(matrix) + " is not a square matrix.");
        }
        return _rows(matrix);
    }

    private static final String _dimensionString(double[][] matrix) {
        return ("[" + _rows(matrix) + " x " + _columns(matrix) + "]");
    }

    /** Return the number of rows of a matrix.
     *  @param matrix A matrix of doubles.
     *  @return An int.
     */
    private static final int _rows(double[][] matrix) {
        return matrix.length;
    }
}

