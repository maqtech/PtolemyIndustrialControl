/* A library for mathematical operations on arrays of doubles.

Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;
import java.lang.Double;              /* Needed by javadoc */

//////////////////////////////////////////////////////////////////////////
//// DoubleArrayMath
/**
 * This class provides library for mathematical operations on double arrays.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null.
 * <p>
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version $Id$
 */

public class DoubleArrayMath {

  // Protected constructor prevents construction of this class.
  protected DoubleArrayMath() {}

  /////////////////////////////////////////////////////////////////////////
  ////                         Public classes                          ////

  /** Implements ArrayStringFormat to produce strings in the format used
   *  to initialize arrays in Java. More specifically, the format
   *  "{x[0], x[1], x[2], ... , x[n-1]}",
   *  where x[i] is the ith element of the array.
   */
  public static class JavaArrayStringFormat implements
   ArrayStringFormat {

    public JavaArrayStringFormat() {}
    
    public String beginString() {
      return "{";
    }

    public String complexString(Complex c) {
      return c.toString();
    }

    public String deliminatorString() {
      return ", ";
    }

    public String doubleString(double d) {
      return Double.toString(d);
    }

    public String endString() {
      return "}";
    }
  }

  /** Implements ArrayStringFormat to produce strings in the format used
   *  in the Ptolemy II expression language and Matlab. More specifically,
   *  the format
   *  "[x[0] x[1] x[2] ...  x[n-1]]",
   */
  public static class ExprArrayStringFormat implements
   ArrayStringFormat {
    public ExprArrayStringFormat() {}

    public String beginString() {
      return "[";
    }

    public String complexString(Complex c) {
      return c.toString();
    }

    public String deliminatorString() {
      return " ";
    }

    public String doubleString(double d) {
      return Double.toString(d);
    }

    public String endString() {
      return "]";
    }
  };

  /////////////////////////////////////////////////////////////////////////
  ////                         Public methods                          ////

  /** Return a new array that is the element-by-element sum of the two
   *  input arrays.
   *  If the sizes of both arrays are 0, return a new array of size 0.
   *  If the two arrays do not have the same length, throw an
   *  IllegalArgumentException.
   *  @param array1 The first array of doubles.
   *  @param array2 The second array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] add(double[] array1, double[] array2) {
    int length = _commonLength(array1, array2, "DoubleArrayMath.add");
    double[] retval = new double[length];
    for (int i = 0; i < length; i++) {
        retval[i] = array1[i] + array2[i];
    }
    return retval;
  }

  /** Return a new array that is the absolute value of the input array.
   *  If the size of the array is 0, return a new array of size 0.
   *  @param array An array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] abs(double[] array) {
    double[] retval = new double[array.length];
    for (int i = 0; i < array.length; i++) {
        retval[i] = Math.abs(array[i]);
    }
    return retval;
  }

  /** Return a new array that is the result of appending array2 to the end
   *  of array1.
   *  @param array1 The first array of doubles.
   *  @param array2 The second array of doubles, which is appended.
   *  @return A new array of doubles.
   */
  public static final double[] append(double[] array1, double[] array2) {
    return append(array1, 0, array1.length, array2, 0, array2.length);
  }

  /** Return a new array that is the result of appending width2 elements
   *  of array2, starting from the array1[idx2] to width1 elements of array1,
   *  starting from array1[idx1].
   *  @param array1 The first array of doubles.
   *  @param idx1 The starting index for array1.
   *  @param length1 The number of elements of array1 to use.
   *  @param array2 The second array of doubles, which is appended.
   *  @param idx2 The starting index for array2.
   *  @param length2 The number of elements of array2 to append.
   *  @return A new array of doubles.
   */
  public final static double[] append(double[] array1, int idx1,
    int length1, double[] array2, int idx2, int length2) {
    double[] retval = new double[length1 + length2];

    System.arraycopy(array1, idx1, retval, 0, length1);
    System.arraycopy(array2, idx2, retval, length1, length2);

    return retval;
  }

  /** Return a new array that is the element-by-element division of
   *  the first array by the second array.
   *  If the sizes of both arrays are 0, return a new array of size 0.
   *  If the two arrays do not have the same length, throw an
   *  IllegalArgumentException.
   *  @param array1 The first array of doubles.
   *  @param array2 The second array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] divide(double[] array1, double[] array2) {
      int length = _commonLength(array1, array2, "DoubleArrayMath.divide");
      double[] retval = new double[length];
      for (int i = 0; i < length; i++) {
          retval[i] = array1[i] / array2[i];
      }
      return retval;
  }

  /** Return the dot product of the two arrays.
   *  If the sizes of the array are both 0, return 0.0.
   *  If the two arrays do not have the same length, throw an
   *  IllegalArgumentException.
   *  @param array1 The first array of doubles.
   *  @param array2 The first array of doubles.
   *  @return A double.
   */
  public final static double dotProduct(double[] array1, double[] array2) {
    int length = _commonLength(array1, array2, "DoubleArrayMath.dotProduct");

    double sum = 0.0;

    for (int i = 0; i < length; i++) {
        sum += array1[i] * array2[i];
    }
    return sum;
  }

  /** Return a new array that is a copy of the argument except that the
   *  elements are limited to lie within the specified range.
   *  If any value is infinite or NaN (not a number),
   *  then it is replaced by either the top or the bottom, depending on
   *  its sign.  To leave either the bottom or the top unconstrained,
   *  specify Double.NEGATIVE_INFINITY or Double.POSITIVE_INFINITY.
   *  If the size of the array is 0, return a new array of size 0.
   *  @param array An array of doubles.
   *  @param bottom The bottom limit.
   *  @param top The top limit.
   *  @return A new array with values in the range [bottom, top].
   */
  public final static double[] limit(double[] array, double bottom,
    double top) {
    double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
        if ((array[i] > top) ||
            (array[i] == Double.NaN) ||
            (array[i] == Double.POSITIVE_INFINITY)) {
           result[i] = top;
        } else if ((array[i] < bottom) ||
                   (array[i] == -Double.NaN) ||
                   (array[i] == Double.NEGATIVE_INFINITY)) {
          result[i] = bottom;
        } else {
          result[i] = array[i];
        }
    }
    return result;
  }

  /** Return a new array that is the element-by-element multiplication of
   *  the two input arrays.
   *  If the sizes of both arrays are 0, return a new array of size 0.
   *  If the two arrays do not have the same length, throw an
   *  IllegalArgumentException.
   *  @param array1 The first array of doubles.
   *  @param array2 The second array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] multiply(double[] array1, double[] array2) {
    int length = _commonLength(array1, array2, "DoubleArrayMath.multiply");
    double[] retval = new double[length];
    for (int i = 0; i < length; i++) {
        retval[i] = array1[i] * array2[i];
     }
     return retval;
  }

  /** Return a new array of doubles that is formed by raising each
   *  element to the specified exponent.
   *  If the size of the array is 0, return a new array of size 0.
   *  @param array An array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] pow(double[] array, double exponent) {
    int length = array.length;
    double[] retval = new double[length];

    for (int i = 0; i < length; i++) {
        retval[i] = Math.pow(array[i], exponent);
    }
    return retval;
  }

  /** Return a new array of length newLength that is formed by
   *  either truncating or padding the input array.
   *  This method simply calls :
   *  resize(array, newLength, 0)
   *  @param array An array of doubles.
   *  @param newLength The desired size of the output array.
   */
  public final static double[] resize(double[] array, int newLength) {
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
   *  @param newLength The desired size of the output array.
   *  @param startIdx The starting index for the input array.
   */
  public final static double[] resize(double[] array, int newLength,
   int startIdx) {

    double[] retval = new double[newLength];
    int copySize = Math.min(newLength, array.length - startIdx);
    if ((startIdx >= array.length) && (copySize >= 0)) {
       throw new IllegalArgumentException(
        "ptolemy.math.DoubleArrayMath.resize() : input array size is " +
        "less than the start index");
    }

    if (copySize > 0) {
       System.arraycopy(array, startIdx, retval, 0, copySize);
    }

    return retval;
  }

  /** Return a new array of doubles produced by scaling the input
   *  array elements by a constant.
   *  If the size of the array is 0, return a new array of size 0.
   *  @param array An array of doubles.
   *  @param scalefactor A double.
   *  @return A new array of doubles.
   */
  public final static double[] scale(double[] array, double scalefactor) {
    double[] retval = new double[array.length];
    for (int i = 0; i < array.length; i++) {
        retval[i] = scalefactor * array[i];
    }
    return retval;
  }

  /** Return a new array that is the element-by-element difference of the
   *  two input arrays, i.e. the first array minus the second array.
   *  If the sizes of both arrays are 0, return a new array of size 0.
   *  @param array1 The first array of doubles.
   *  @param array2 The second array of doubles.
   *  @return A new array of doubles.
   */
  public final static double[] subtract(double[] array1, double[] array2) {
    int length = _commonLength(array1, array2, "DoubleArrayMath.subtract");
    double[] retval = new double[length];

    for (int i = 0; i < length; i++) {
        retval[i] = array1[i] - array2[i];
    }
    return retval;
  }

  /** Return a new array that is formed by converting the shorts in
   *  the argument to doubles.
   *  If the size of the argument array is 0, return a new array of size 0.
   *  @param array An array of shorts.
   *  @return A new array of doubles.
   */
  public static final double[] toDoubleArray(short[] array) {
    int length = array.length;
    double[] retval = new double[length];

    for (int i = 0; i < length; i++) {
        retval[i] = (double) array[i];
    }
    return retval;
  }

  /** Return a new array that is formed by converting the integers in
   *  the argument to doubles.
   *  If the size of the argument array is 0, return a new array of size 0.
   *  @param array An array of integers.
   *  @return A new array of doubles.
   */
  public static final double[] toDoubleArray(int[] array) {
    int length = array.length;
    double[] retval = new double[length];

    for (int i = 0; i < length; i++) {
        retval[i] = (double) array[i];
    }
    return retval;
  }

  /** Return a new array that is formed by converting the floats in
   *  the argument to doubles.
   *  If the size of the argument array is 0, return a new array of size 0.
   *  @param array An array of shorts.
   *  @return A new array of doubles.
   */
  public static final double[] toDoubleArray(float[] array) {
    int length = array.length;
    double[] retval = new double[length];

    for (int i = 0; i < length; i++) {
        retval[i] = (double) array[i];
    }
    return retval;
  }

  /** Return a new String in the format "{x[0], x[1], x[2], ... , x[n-1]}",
   *  where x[i] is the ith element of the array.
   *  @param array An array of doubles.
   *  @return A new String representing the contents of the array.
   */
  public final static String toString(double[] array) {
    return toString(array, javaASFormat);
  }

  /** Return a new String representing the array, formatted as
   *  specified by the ArrayStringFormat argument.
   *  To get a String in the Ptolemy expression language format,
   *  call this method with DoubleArrayMath.exprASFormat as the
   *  format argument.
   *  @param array An array of doubles.
   *  @return A new String representing the contents of the array.
   */
  public final static String toString(double[] array,
   ArrayStringFormat format) {
    int length = array.length;
    StringBuffer sb = new StringBuffer();

    sb.append(format.beginString());

    for (int i = 0; i < length; i++) {

        sb.append(format.doubleString(array[i]));

        if (i < (length - 1)) {
           sb.append(format.deliminatorString());
        }
    }

    sb.append(format.endString());

    return new String(sb);
  }


  /** Return true if all the absolute differences between corresponding
   *  elements of array1 and array2 are all less than or equal to maxError.
   *  Otherwise return false.
   *  If the two arrays do not have the same length, throw an
   *  IllegalArgumentException.
   *  @param array1 An array of doubles.
   *  @param array2 An array of doubles.
   *  @param maxError A double.
   *  @return A boolean.
   */
  public final static boolean within(double[] array1, double[] array2,
   double maxError) {
    int length = _commonLength(array1, array2, "DoubleArrayMath.within");

    for (int i = 0; i < length; i++) {
        if (Math.abs(array1[i] - array2[i]) > maxError) {
           return false;
        }
    }
    return true;
  }

  /////////////////////////////////////////////////////////////////////////
  ////                         Public fields                           ////

  /** A static instance of JavaArrayStringFormat.
   *  @see JavaArrayStringFormat
   */
  public static final ArrayStringFormat javaASFormat =
   new JavaArrayStringFormat();

  /** A static instance of ExprArrayStringFormat.
   *  @see ExprArrayStringFormat
   */
  public static final ArrayStringFormat exprASFormat =
   new ExprArrayStringFormat();

  /////////////////////////////////////////////////////////////////////////
  //    protected methods

  /** Throw an exception if the array is null or length 0.
   *  Otherwise return the length of the array.
   */
  protected static final int _nonZeroLength(double[] array,
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
   *  arrays are of size 0. If no exception is thrown, return the common
   *  length of the arrays.
   */
  protected static final int _commonLength(double[] array1, double[] array2,
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
        "() : input arrays must have the same length, but the first " +
        "array has length " + array1.length + " and the second array " +
        "has length " + array2.length + ".");
    }

    return array1.length;
  }
}


