/*
A library for mathematical operations on arrays of complex numbers.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.math;

import java.lang.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ComplexArrayMath
/**
 * This class a provides a library for mathematical operations on arrays of
 * complex numbers, in particular arrays of instances of class
 * ptolemy.math.Complex.
 * Unless explicity noted otherwise, all array arguments are assumed to be
 * non-null. If a null array is passed to a method, a NullPointerException
 * will be thrown in the method or called methods.
 * <p>
 * @author Albert Chen, William Wu, Edward A. Lee, Jeff Tsay
 * @version $Id$
 */
public class ComplexArrayMath {

    // Protected constructor prevents construction of this class.
    protected ComplexArrayMath() {}

    /** Return a new array that is constructed from the argument by
     *  adding the complex number z to every element.
     *  @return A new array of complex numbers.
     */
    public static final Complex[] add(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].add(z);
        }
        return result;
    }

    /** Return a new array that is the element-by-element sum of the two
     *  input arrays.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Complex[] add(Complex[] array1, Complex[] array2) {
        int length = _commonLength(array1, array2, "ComplexArrayMath.add");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].add(array2[i]);
        }
        return retval;
    }

  /** Return a new array that is the result of appending array2 to the end
   *  of array1. This method simply calls
   *  append(array1, 0, array1.length, array2, 0, array2.length)
   */
  public static final Complex[] append(Complex [] array1, Complex[] array2) {
    return append(array1, 0, array1.length, array2, 0, array2.length);
  }

  /** Return a new array that is the result of appending length2 elements
   *  of array2, starting from the array1[idx2] to length1 elements of array1,
   *  starting from array1[idx1].
   *  Appending empty arrays is supported. In that case, the corresponding
   *  idx may be any number. Allow System.arraycopy() to throw array access
   *  exceptions if idx .. idx + length - 1 are not all valid array indices,
   *  for both of the arrays.
   *  @param array1 The first array of Complex.
   *  @param idx1 The starting index for array1.
   *  @param length1 The number of elements of array1 to use.
   *  @param array2 The second array of Complex, which is appended.
   *  @param idx2 The starting index for array2.
   *  @param length2 The number of elements of array2 to append.
   *  @return A new array of Complex.
   */
  public static final Complex[] append(Complex[] array1, int idx1,
    int length1, Complex[] array2, int idx2, int length2) {
    Complex[] retval = new Complex[length1 + length2];

    if (length1 > 0) {
       System.arraycopy(array1, idx1, retval, 0, length1);
    }

    if (length2 > 0) {
       System.arraycopy(array2, idx2, retval, length1, length2);
    }

    return retval;
  }


    /** Return a new array that is the complex-conjugate of the argument.
     *  If the argument has length 0, return a new array of Complex's, with
     *  length 0.
     */
    public static final Complex[] conjugate(Complex[] array) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].conjugate();
        }
        return result;
    }

    /** Return a new array that is the element-by-element division of
     *  the first array by the second array.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 The first array of Complex's.
     *  @param array2 The second array of Complex's.
     *  @return A new array of Complex's.
     */
    public static final Complex[] divide(Complex[] array1, Complex[] array2) {
        int length = _commonLength(array1, array2, "ComplexArrayMath.divide");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].divide(array2[i]);
        }
        return retval;
    }

    /** Return a new array of Complex's with the values in imagArray for
     *  the imaginary parts, and 0 for the real parts.
     *  If imagArray has length 0, return a new Complex array of length 0.
     */
    public static final Complex[] imagArrayToComplexArray(double[] imagArray)
    {
       int size = imagArray.length;

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(0, imagArray[i]);
       }
       return retval;
    }

    /** Return a new array of doubles with the imaginary parts of the array of
     *  Complex's.
     */
    public static final double[] imagParts(Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].imag;
        }

        return retval;
    }

    /** Return a new array of Complex's using two arrays for the real and
     *  imaginary parts. If both arrays are of length 0, return a new
     *  array of Complex's with length 0.
     *  @param realPart An array of doubles, used for the real parts.
     *  @param imagPart An array of doubles, used for the imaginary parts.
     *  @return A new array of Complex's.
     */
    public static final Complex[] formComplexArray(double[] realPart,
     double[] imagPart) {
       int size = DoubleArrayMath._commonLength(realPart, imagPart,
        "ComplexArrayMath.formComplexArray");

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(realPart[i], imagPart[i]);
       }

       return retval;
    }

    /** Return a new array of doubles containing the magnitudes of the elements
     *  of the specified array of Complex's.
     */
    public static final double[] magnitude(Complex[] array) {
        double[] mags = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            mags[i] = array[i].magnitude();
        }
        return mags;
    }

    /** Return a new array that is the element-by-element multiplication of
     *  the two input arrays.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     */
    public static final Complex[] multiply(Complex[] array1,
     Complex[] array2) {
        int length = _commonLength(array1, array2,
         "ComplexArrayMath.multiply");
        Complex[] retval = new Complex[length];
        for (int i = 0; i < length; i++) {
            retval[i] = array1[i].multiply(array2[i]);
        }
        return retval;
    }

    /** Return a new array that is constructed from the argument by
     *  multiplying each element in the array by the second argument, which is
     *  a complex number.
     *  If the sizes of the array is 0, return a new array of size 0.
     *  @param array An array of Complex's.
     *  @param factor A Complex.
     *  @return A new array of Complex's.
     */
    public static final Complex[] multiply(Complex[] array, Complex factor) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array[i].multiply(factor);
        }

        return retval;
    }


    /** Return a new array containing the angles of the elements of the
     *  specified complex array.
     *  @param array A array of Complex's.
     *  @return An array of angles in the range of <em>-pi</em> to <em>pi</em>.
     */
    public static final double[] phase(Complex[] array) {
        double[] angles = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            angles[i] = array[i].angle();
        }
        return angles;
    }

    /** Given the roots of a polynomial, return a polynomial that has
     *  has such roots.  If the roots are
     *  [<em>r</em><sub>0</sub>, ..., <em>r</em><sub>N-1</sub>],
     *  then the polynomial is given by
     *  [<em>a</em><sub>0</sub>, ..., <em>a</em><sub>N</sub>], where
     *  <p>
     *  <em>a</em><sub>0</sub> +
     *  <em>a</em><sub>1</sub><em>z</em><sup>-1</sup> + ... +
     *  <em>a</em><sub>N</sub><em>z</em><sup>-N</sup> =
     *  (1 - <em>r</em><sub>0</sub><em>z</em><sup>-1</sup>)
     *  (1 - <em>r</em><sub>1</sub><em>z</em><sup>-1</sup>) ...
     *  (1 - <em>r</em><sub>N-1</sub><em>z</em><sup>-1</sup>).
     *  <p>
     *  The returned polynomial will always be monic, meaning that
     *  <em>a</em><sub>0</sub> = 1.
     *
     *  @param roots An array of roots of a polynomial.
     *  @return A new array representing a monic polynomial with the given
     *   roots.
     */
    public static final Complex[] polynomial(Complex[] roots) {
        if (roots.length <= 1) {
            Complex[] result = new Complex[1];
            result[0] = new Complex(1);
            return result;
        }
        Complex[] result = new Complex[2];
        result[0] = new Complex(1.0);

        if (roots.length >= 1) {
            result[1] = roots[0].negate();
            if (roots.length > 1) {
                for (int i = 1; i < roots.length; i++) {
                    Complex[] factor =
                    {new Complex(1), roots[i].negate()};
                    result = SignalProcessing.convolve(result, factor);
                }
            }
        }
        return result;
    }

    /** Return a new array of Complex's that is formed by raising each
     *  element to the specified exponent, a double.
     *  If the size of the array is 0, return a new array of size 0.
     */
    public static final Complex[] pow(Complex[] array, double exponent) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array[i].pow(exponent);
        }
        return retval;
    }

    /** Return a new array of Complex's that is formed by raising each
     *  element to the specified exponent, a complex number.
     *  If the size of the array is 0, return a new array of size 0.
     */
    public static final Complex[] pow(Complex[] array, Complex exponent) {
        int length = array.length;
        Complex[] retval = new Complex[length];

        for (int i = 0; i < length; i++) {
            retval[i] = array[i].pow(exponent);
        }
        return retval;
    }

    /** Return the product of the elements in the array.
     *  If there are no elements in the array, return a Complex number
     *  with value zero.
     *  @param array An array of Complex's.
     *  @return A new complex number.
     */
    public static final Complex product(Complex[] array) {
        if (array.length == 0) return new Complex();
        double real = 1.0;
        double imag = 0.0;
        for (int i = 0; i < array.length; i++) {
            double tmp = real*array[i].real - imag*array[i].imag;
            imag = real*array[i].imag + imag*array[i].real;
            real = tmp;
        }
        return new Complex(real, imag);
    }

    /** Return true iff all the elements in the array are purely imaginary.
     *  @param array An array of Complex's
     *  @return A boolean.
     */
    public static final boolean pureImag(Complex[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].real != 0.0) {
               return false;
            }
        }
        return true;
    }

    /** Return true iff all the elements in the array are purely real.
     *  @param array An array of Complex's
     *  @return A boolean.
     */
    public static final boolean pureReal(Complex[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].imag != 0.0) {
               return false;
            }
        }
        return true;
    }

    /** Return a new array of Complex's with the values in realArray for
     *  the real parts, and 0 for the imaginary parts. If the argument is
     *  of length 0, return a new array of length 0.
     */
    public static final Complex[] realArrayToComplexArray(double[] realArray) {
       int size = realArray.length;

       Complex[] retval = new Complex[size];

       for (int i = 0; i < size; i++) {
           retval[i] = new Complex(realArray[i], 0);
       }
       return retval;
    }

    /** Return a new array of doubles with the real parts of the array of
     *  Complex's.
     */
    public static final double[] realParts(Complex[] x) {
        int size = x.length;

        double[] retval = new double[size];

        for (int i = 0; i < size; i++) {
            retval[i] = x[i].real;
        }

        return retval;
    }

    /** Return a new array of length newLength that is formed by
     *  either truncating or padding the input array.
     *  This method simply calls :
     *  resize(array, newLength, 0)
     *  @param array An array of Complex's.
     *  @param newLength The desired size of the output array.
     */
    public static final Complex[] resize(Complex[] array, int newLength) {
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
     *  new Complex's with value 0.
     *  Copying here means shallow copying, i.e. pointers to Complex objects
     *  are copied instead of allocation of new copies. This works because
     *  Complex objects are immutable.
     *  @param array An array of Complex's.
     *  @param newLength The desired size of the output array.
     *  @param startIdx The starting index for the input array.
     */
    public static final Complex[] resize(Complex[] array, int newLength,
     int startIdx) {

        Complex[] retval = new Complex[newLength];
        int copySize = Math.min(newLength, array.length - startIdx);

        if ((startIdx >= array.length) && (copySize >= 0)) {
           throw new IllegalArgumentException(
            "resize() :  input array size is less than the start index");
        }

        if (copySize > 0) {
           System.arraycopy(array, startIdx, retval, 0, copySize);
        }

        for (int i = copySize; i < newLength; i++) {
            retval[i] = new Complex(0.0, 0.0);
        }

        return retval;
    }


    /** Return a new array that is constructed from the argument by
     *  scaling each element in the array by factor, which
     *  is a double. If the array argument is of length 0, return a new array
     *  of length 0.
     */
    public static final Complex[] scale(Complex[] array, double factor) {
        int len = array.length;
        Complex[] retval = new Complex[len];

        for (int i = 0; i < len; i++) {
            retval[i] = array[i].scale(factor);
        }

        return retval;
    }

    /** Return a new array that is constructed by subtracting the complex
     *  number z from every element in the first array. If the array argument
     *  is of length 0, return a new array of length 0.
     */
    public static final Complex[] subtract(Complex[] array, Complex z) {
        Complex[] result = new Complex[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            result[i] = array[i].subtract(z);
        }
        return result;
    }

    /** Return a new array that is the element-by-element
     *  subtraction of the second array from the first array.
     *  If the sizes of both arrays are 0, return a new array of size 0.
     *  If the two arrays do not have the same length, throw an
     *  IllegalArgumentException.
     *  @param array1 An array of Complex's from which to subtract.
     *  @param array2 An array of Complex's to subtract.
     *  @return A new array of Complex's.
     */
    public static final Complex[] subtract(Complex[] array1,
     Complex[] array2) {
        int length = _commonLength(array1, array2,
                      "ComplexArrayMath.subtract");
        Complex[] result = new Complex[length];
        for (int i = 0; i < length; i++) {
            result[i] = array1[i].subtract(array2[i]);
        }
        return result;
    }

  /** Return a new String representing the array, formatted as
   *  in Java array initializers.
   */
  public static final String toString(Complex[] array) {
    return toString(array, ArrayStringFormat.javaASFormat);
  }

  /** Return a new String representing the array, formatted as
   *  specified by the ArrayStringFormat argument.
   *  To get a String in the Ptolemy expression language format,
   *  call this method with ArrayStringFormat.exprASFormat as the
   *  format argument.
   */
  public static final String toString(Complex[] array,
   ArrayStringFormat format) {
    int length = array.length;
    StringBuffer sb = new StringBuffer();

    sb.append(format.vectorBeginString());

    for (int i = 0; i < length; i++) {

        sb.append(format.complexString(array[i]));

        if (i < (length - 1)) {
           sb.append(format.elementDeliminatorString());
        }
    }

    sb.append(format.vectorEndString());

    return new String(sb);
  }

    /** @deprecated */
    public static final boolean within(Complex[] array1, Complex[] array2,
     double maxError) {
      return arePartsWithin(array1, array2, maxError);
    }

  /** Return true if all the absolute differences between corresponding
   *  elements of array1 and array2, for both the real and imaginary parts,
   *  are all less than or equal to maxError. Otherwise, return false.
   *  Throw an IllegalArgument exception if the arrays are not of the same
   *  length. If both arrays are empty, return true.
   *  This is computationally less expensive than isSquaredErrorWithin().
   */
  public static final boolean arePartsWithin(Complex[] array1,
   Complex[] array2, double maxError) {
    int length = _commonLength(array1, array2,
     "ComplexArrayMath.arePartsWithin");

    for (int i = 0; i < length; i++) {
        if ((Math.abs(array1[i].real - array2[i].real) > maxError) ||
            (Math.abs(array1[i].imag - array2[i].imag) > maxError)) {
           return false;
        }

    }
    return true;
  }

  /** Return true if all the magnitudes of the differences between
   *  corresponding elements of array1 and array2, are all less than or
   *  equal to maxMagnitudeDifference. Otherwise, return false.
   *  Throw an exception if the arrays are not of the same length. If both
   *  arrays are empty, return true.
   *  This is computationally more expensive than arePartsWithin().
   */
  public static final boolean areMagnitudesWithin(Complex[] array1,
    Complex[] array2, double maxMagnitudeDifference) {
    int length = _commonLength(array1, array2,
     "ComplexArrayMath.areMagnitudesWithin");

    for (int i = 0; i < length; i++) {
         if (array1[i].subtract(array2[i]).magnitude() >
            maxMagnitudeDifference) {
            return false;
         }
    }
    return true;
  }

  /////////////////////////////////////////////////////////////////////////
  //    protected methods

  // Throw an exception if the array is null or length 0.
  // Otherwise return the length of the array.
  protected static final int _nonZeroLength(Complex[] array,
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

  // Throw an exception if the two arrays are not of the same length,
  // or if either array is null. An exception is NOT thrown if both
  // arrays are of size 0. If no exception is thrown, return the common
  // length of the arrays.
  protected static final int _commonLength(Complex[] array1, Complex[] array2,
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
