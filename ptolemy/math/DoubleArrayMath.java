/*
A library for mathematical operations on arrays of doubles.

This file was automatically generated with a preprocessor, so that
similar array operations are supported on ints, longs, floats, and doubles.

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

import java.lang.Double;              /* Needed by javadoc */

//////////////////////////////////////////////////////////////////////////
//// DoubleArrayMath
/**
 * This class provides a library for mathematical operations on double arrays.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null. If a null array is passed to a method, a NullPointerException
 * will be thrown in the method or called methods.
 * <p>
 * This file was automatically generated with a preprocessor, so that
 * similar matrix operations are supported on ints, longs, floats, and doubles.
 * <p>
 *
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 */

public class DoubleArrayMath {

    // Protected constructor prevents construction of this class.
    protected DoubleArrayMath() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new array that is the formed by adding z to each element
     *  of the input array.
     */
    public static final double[] add(final double[] array, final double z) {
        int length = array.length;
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array[i] + z;
        }
        return retval;
    }

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] add(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.add");
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] + array2[i];
        }
        return retval;
    }

    /** Return a new array that is the result of appending array2 to the end
     *  of array1. This method simply calls
     *  append(array1, 0, array1.length, array2, 0, array2.length)
     */
    public static final double[] append(final double[] array1,
            final double[] array2) {
        return append(array1, 0, array1.length, array2, 0, array2.length);
    }

    /** Return a new array that is the result of appending length2 elements
     *  of array2, starting from the array1[idx2] to length1 elements of array1,
     *  starting from array1[idx1].
     *  Appending empty arrays is supported. In that case, the corresponding
     *  idx may be any number. Allow System.arraycopy() to throw array access
     *  exceptions if idx .. idx + length - 1 are not all valid array indices,
     *  for both of the arrays.
     *  @param array1 The first array of doubles.
     *  @param idx1 The starting index for array1.
     *  @param length1 The number of elements of array1 to use.
     *  @param array2 The second array of doubles, which is appended.
     *  @param idx2 The starting index for array2.
     *  @param length2 The number of elements of array2 to append.
     *  @return A new array of doubles.
     */
    public static final double[] append(final double[] array1,
            final int idx1, final int length1, final double[] array2, final int idx2,
            final int length2) {
        double[] retval = new double[length1 + length2];

        if (length1 > 0) {
            System.arraycopy(array1, idx1, retval, 0, length1);
        }

        if (length2 > 0) {
            System.arraycopy(array2, idx2, retval, length1, length2);
        }

        return retval;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input array
     *  and z, using the array elements as the left operands and z
     *  as the right operand in all cases. (op.operate(array[i], z)).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op, final double[] array, final double z) {
        int length = array.length;
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array[i], z);
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to each element in the input array,
     *  using z as the left operand in all cases and the array elements
     *  as the right operands (op.operate(z, array[i])).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op, final double z, final double[] array) {
        int length = array.length;
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array[i], z);
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleBinaryOperation to the two arrays, element by element,
     *  using the elements of the first array as the left operands and the
     *  elements of the second array as the right operands.
     *  (op.operate(array[i], array2[i])).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] applyBinaryOperation(
            DoubleBinaryOperation op, final double[] array1, final double[] array2) {
        int length = _commonLength(array1, array2,
                "DoubleArrayMath.applyBinaryOperation");
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array1[i], array2[i]);
        }
        return retval;
    }

    /** Return a new array that is formed by applying an instance of a
     *  DoubleUnaryOperation to each element in the input array
     *  (op.operate(array[i])).
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] applyUnaryOperation(
            final DoubleUnaryOperation op, final double[] array) {
        int length = array.length;
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = op.operate(array[i]);
        }
        return retval;
    }



    // no need for an element-by-element division, use divide(array, 1.0 / z) instead


    /** Return a new array that is the element-by-element division of
     *  the first array by the second array (array1[i] / array2[i]).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 The first array of doubles.
     *  @param array2 The second array of doubles.
     *  @return A new array of doubles.
     */
    public static final double[] divide(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.divide");
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] / array2[i];
        }
        return retval;
    }

    /** Return the dot product of the two arrays.
     *  If the lengths of the array are both 0, return 0.0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double dotProduct(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2,
                "DoubleArrayMath.dotProduct");

        double sum = 0.0;

        for (int i = 0; i < length; i++) {
            sum += array1[i] * array2[i];
        }
        return sum;
    }

    /** Return the L2-norm of the array, that is, the square root of the sum of the
     *  squares of the elements.
     */
    public static final double l2norm(final double[] array) {
        return (double) Math.sqrt(sumOfSquares(array));
    }


    /** Return a new array that is a copy of the argument except that the
     *  elements are limited to lie within the specified range.
     *  If any value is infinite or NaN (not a number),
     *  then it is replaced by either the top or the bottom, depending on
     *  its sign.  To leave either the bottom or the top unconstrained,
     *  specify Double.NEGATIVE_INFINITY or Double.POSITIVE_INFINITY.


     *  If the length of the array is 0, return a new array of length 0.
     *  @param array An array of doubles.
     *  @param bottom The bottom limit.
     *  @param top The top limit.
     *  @return A new array with values in the range [bottom, top].
     */
    public static final double[] limit(final double[] array,
            final double bottom, final double top) {
        double[] retval = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            if ((array[i] > top) ||
                    (array[i] == Double.NaN) ||
                    (array[i] == Double.POSITIVE_INFINITY)) {


                retval[i] = top;
            } else if ((array[i] < bottom) ||
                    (array[i] == -Double.NaN) ||
                    (array[i] == Double.NEGATIVE_INFINITY)) {


                retval[i] = bottom;

            } else {
                retval[i] = array[i];
            }
        }
        return retval;
    }



    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the lengths of both arrays are 0, return a new array of length 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final double[] multiply(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.multiply");
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] * array2[i];
        }
        return retval;
    }

    /** Return a new array that is the formed by the additive inverse of each
     *  element of the input array (-array[i]).
     */
    public static final double[] negative(final double[] array) {
        int length = array.length;
        double[] retval = new double[length];
        for (int i = 0; i < length; i++) {
            retval[i] = -array[i];
        }
        return retval;
    }

    /** Return a new array that is formed by scaling the array so that
     *  it has a L2-norm of 1.
     */
    public static final double[] normalize(final double[] array) {
        return scale(array, 1.0 / l2norm(array));
    }



    /** Return a new array of doubles that is formed by padding the
     *  middle of the array with 0's. If either the length of the
     *  input array is odd, the sample with index ceil(L/2) will be
     *  repeated in the output array, where L is the length of the input array.
     *  If the length of the input and output arrays are equal, return
     *  a copy of the input array.
     *  This method is useful for preparing data for an IFFT.
     *  @param array An array of doubles.
     *  @param newLength The desired length of the returned array.
     *  @return A new array of doubles.
     */
    public static final double[] padMiddle(final double[] array,
            final int newLength) {
        int length = array.length;

        int entriesNeeded = newLength - length;

        if (entriesNeeded < 0) {
            throw new IllegalArgumentException("ptolemy.math." +
                    "DoubleArrayMath.padMiddle() : newLength must be >= length of " +
                    "array.");
        } else if (entriesNeeded == 0) {
            return resize(array, newLength); // allocates a new array
        }

        double halfLength   = length * 0.5;
        int halfLengthFloor = (int) Math.floor(halfLength);
        int halfLengthCeil  = (int) Math.ceil(halfLength);
        double[] retval = new double[newLength];

        System.arraycopy(array, 0, retval, 0, halfLengthCeil);

        System.arraycopy(array,  halfLengthFloor, retval,
                newLength - halfLengthCeil, halfLengthCeil);


        return retval;
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of doubles.
     *  @param newLength The desired length of the output array.
     *  @return A new array of doubles of length newLength.
     */
    public static final double[] resize(final double[] array,
            final int newLength) {
        return resize(array,  newLength, 0);
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  Elements from the input array are copied to the output array,
     *  starting from array[startIdx] until one of the following conditions
     *  is met :
     *  1) The input array has no more elements to copy.
     *  2) The output array has been completely filled.
     *  startIdx must index a valid entry in array unless the input array
     *  is of zero length or the output array is of zero length.
     *  If case 1) is met, the remainder of the output array is filled with
     *  zero's, implicitly by Java (padding).
     *  @param array An array of doubles.
     *  @param newLength The desired length of the output array.
     *  @param startIdx The starting index for the input array.
     *  @return A new array of doubles of length newLength.
     */
    public static final double[] resize(double[] array,
            final int newLength, final int startIdx) {

        double[] retval = new double[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);
        if ((startIdx >= array.length) && (copySize > 0)) {
            throw new IllegalArgumentException(
                    "ptolemy.math.DoubleArrayMath.resize() : " +
                    "input array size is less than the start index");
        }

        if (copySize > 0) {
            System.arraycopy(array, startIdx, retval, 0, copySize);
        }

        return retval;
    }

    /** Return a new array of doubles produced by scaling the input
     *  array elements by scalefactor.
     *  If the length of the array is 0, return a new array of length 0.
     */
    public static final double[] scale(double[] array, double scalefactor) {
        double[] retval = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            retval[i] = scalefactor * array[i];
        }
        return retval;
    }


    /** Return a new array that is the element-by-element difference of the
     *  two input arrays, i.e. the first array minus the second array
     *  (array1[i] - array2[i]).
     *  If the lengths of both arrays are 0, return a new array of length 0.
     */
    public static final double[] subtract(final double[] array1,
            final double[] array2) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.subtract");
        double[] retval = new double[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array1[i] - array2[i];
        }
        return retval;
    }

    /** Return the sum of the elements in the array.
     *  Return 0.0 if the length of the array is 0.
     */
    public static final double sum(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /** Return the sum of the squares of all of the elements in the array.
     *  This is equivalent to the square of the L2-norm of the array.
     *  Return 0.0 if the length of the array is 0.
     */
    public static final double sumOfSquares(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += (array[i] * array[i]);
        }
        return sum;
    }

    /** Return a new array that is formed by converting the doubles in
     *  the argument array to complex numbers. Each complex number has real
     *  part equal to the value in the argument matrix and a zero imaginary part.
     *  @param array An array of double.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] toComplexArray(final double[] array) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = new Complex(array[i], 0.0);
        }
        return retval;

    }



    /** Return a new array that is formed by converting the doubles in
     *  the argument array to doubles.
     *  If the length of the argument array is 0,
     *  return a new array of length 0.
     *  @param array An array of double.
     *  @return A new array of doubles.
     */
    public static final float[] toFloatArray(final double[] array) {
        int length = array.length;
        float[] retval = new float[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (float) array[i];
        }
        return retval;
    }


    /** Return a new array that is formed by converting the doubles in
     *  the argument array to integers.
     *  If the length of the argument array is 0,
     *  return a new array of length 0.
     *  @param array An array of double.
     *  @return A new array of integers.
     */
    public static final int[] toIntegerArray(final double[] array) {
        int length = array.length;
        int[] retval = new int[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (int) array[i];
        }
        return retval;
    }


    /** Return a new array that is formed by converting the doubles in
     *  the argument array to longs.
     *  If the length of the argument array is 0, return a new array of length 0.
     *  @param array An array of double.
     *  @return A new array of longs.
     */
    public static final long[] toLongArray(final double[] array) {
        int length = array.length;
        long[] retval = new long[length];

        for (int i = 0; i < length; i++) {
            retval[i] = (long) array[i];
        }
        return retval;
    }


    /** Return a new String representing the array, formatted as
     *  in Java array initializers.
     */
    public static final String toString(final double[] array) {
        return toString(array, ArrayStringFormat.javaASFormat);
    }

    /** Return a new String representing the array, formatted as
     *  specified by the ArrayStringFormat argument.
     *  To get a String in the Ptolemy expression language format,
     *  call this method with ArrayStringFormat.exprASFormat as the
     *  format argument.
     */
    public static final String toString(final double[] array,
            ArrayStringFormat format) {
        int length = array.length;
        StringBuffer sb = new StringBuffer();

        sb.append(format.vectorBeginString());

        for (int i = 0; i < length; i++) {

            sb.append(format.doubleString(array[i]));

            if (i < (length - 1)) {
                sb.append(format.elementDelimiterString());
            }
        }

        sb.append(format.vectorEndString());

        return new String(sb);
    }

    /** Return true if all the absolute differences between corresponding
     *  elements of array1 and array2 are all less than or equal to maxError.
     *  Otherwise return false.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final boolean within(final double[] array1,
            final double[] array2, double maxError) {
        int length = _commonLength(array1, array2, "DoubleArrayMath.within");

        for (int i = 0; i < length; i++) {
            if (Math.abs(array1[i] - array2[i]) > maxError) {
                return false;
            }
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the array is null or length 0.
     *  Otherwise return the length of the array.
     *  @param array An array of doubles.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The length of the array.
     */
    public static final int _nonZeroLength(final double[] array,
            String methodName) {
        if (array == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input array is null.");
        }

        if (array.length <= 0) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input array has length 0.");
        }

        return array.length;
    }

    /** Throw an exception if the two arrays are not of the same length,
     *  or if either array is null. An exception is NOT thrown if both
     *  arrays are of length 0. If no exception is thrown, return the common
     *  length of the arrays.
     *  @param array The first array of doubles.
     *  @param array The second array of doubles.
     *  @param methodName A String representing the method name of the caller,
     *  without parentheses.
     *  @return The common length of both arrays.
     */
    public static final int _commonLength(final double[] array1,
            final double[] array2,
            String methodName) {
        if (array1 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : first input array is null.");
        }

        if (array2 == null) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : second input array is null.");
        }

        if (array1.length != array2.length) {
            throw new IllegalArgumentException("ptolemy.math." + methodName +
                    "() : input arrays must have the same length, " +
                    "but the first array has length " + array1.length +
                    " and the second array has length " +
                    array2.length + ".");
        }

        return array1.length;
    }
}
