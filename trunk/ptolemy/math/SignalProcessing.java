/* A library of signal processing operations.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.math;

import java.lang.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SignalProcessing
/**
 * This class provides signal processing functions.
 *
 * @author Albert Chen, William Wu, Edward A. Lee
 * @version $Id$
 */

public final class SignalProcessing {

    // The only constructor is private so that this class cannot
    // be instantiated.
    private SignalProcessing() {}

    /////////////////////////////////////////////////////////////////////////
    ////                         public methods                          ////

    /** Return true if the first argument is close to the second (within
     *  EPSILON).
     */
    public static boolean close(double first, double second) {
        double diff = first-second;
        return (diff < EPSILON && diff > -EPSILON);
    }

    /** Return the value of the argument <em>z</em>
     *  in decibels, which is defined to be 20*log<sub>10</sub>(<em>z</em>).
     *  Note that if the input represents power, which is proportional to a
     *  magnitude squared, then this should be divided
     *  by two to get 10*log<sub>10</sub>(<em>z</em>).
     */
    public static double db(double value) {
        return 20*Math.log(value)/_LOG10SCALE;
    }

    /** Return a new array the value of the argument array
     *  in decibels, using the previous db() method.
     *  You may wish to combine this with ArrayMath.limit()
     */
    public static double[] db(double[] values) {
        double[] result = new double[values.length];
        for (int i = values.length-1; i >= 0; i--) {
            result[i] = db(values[i]);
        }
        return result;
    }

    /** Return the discrete Fourier transform of the specified complex array.
     *  This is computed by the radix-two FFT algorithm with the size being
     *  the next power of two larger than or equal to the length of the
     *  array argument. The data are zero filled if necessary.
     *  If any element
     *  of the data array is null, it is assumed to have value zero.
     *
     *  @param x The data to transform
     *  @exception IllegalArgumentException If the array argument is empty
     *   or null. This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fft(Complex[] x) {
        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: empty array argument.");
        }
        // Calculate the order of the FFT.
        double m = Math.log(x.length)*_LOG2SCALE;
        double exp = Math.ceil(m);
        return fft(x,(int)exp);
    }

    /** Return the discrete Fourier transform of the specified complex array.
     *  This is computed by the radix-two FFT algorithm.
     *  The size of the FFT is
     *  <i>size</i> = 2<sup><i>order</i></sup>.
     *  If <i>size</i> is less than the length of <i>x</i>,
     *  then only the first <i>size</i> elements of <i>x</i> are used.
     *  If <i>size</i> is greater than the length of <i>x</i>,
     *  then the data is implicitly zero-filled (i.e. it is assumed that
     *  missing data have value zero).
     *  If any element
     *  of the data array is null, it is assumed to have value zero.
     *
     *  @param x The data to transform
     *  @param order The order of the FFT
     *  @exception IllegalArgumentException If the data array is empty
     *   (or null), or the order is not positive.
     *   This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fft(Complex[] x, int order) {
        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: empty array argument.");
        }
        if (order <= 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: order argument must be positive.");
        }
        // size = 2**order
        int size = 1 << order;
        double[] reals = new double[size];
        double[] imags = new double[size];
        // Copy the array.  Zero filling is implicit because Java
        // always initializes arrays.
        for (int i = 0; i < x.length; i++) {
            if (x[i] != null) {
                reals[i] = x[i].real;
                imags[i] = x[i].imag;
            }
        }
        _fft(reals, imags, order, false);
        Complex[] result = new Complex[size];
        for (int i = 0; i < size; i++) {
            result[i] = new Complex(reals[i], imags[i]);
        }
        return result;
    }

    /** Return the discrete Fourier transform of an array of doubles.
     *  This is computed by the radix-two FFT algorithm with the size being
     *  the next power of two larger than or equal to the length of the
     *  array argument.  The data are zero filled if necessary.
     *
     *  @param x The data to transform
     *  @return The DFT of the data
     *  @exception IllegalArgumentException If the argument is an empty
     *   array (or null).  This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fft(double[] x) {
        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: empty array argument.");
        }
        // Calculate the order of the FFT.
        double m = Math.log(x.length)*_LOG2SCALE;
        double exp = Math.ceil(m);
        return fft(x, (int)exp);
    }

    /** Return the discrete Fourier transform of an array of doubles.
     *  This is computed by the radix-two FFT algorithm.
     *  The size of the FFT is
     *  <i>size</i> = 2<sup><i>order</i></sup>.
     *  If <i>size</i> is less than the length of <i>x</i>,
     *  then only the first <i>size</i> elements of <i>x</i> are used.
     *  If <i>size</i> is greater than the length of <i>x</i>,
     *  then the data is implicitly zero-filled (i.e. it is assumed that
     *  missing data have value zero).
     *
     *  @param x The data to transform
     *  @param order The order of the FFT
     *  @return The DFT of the data
     *  @exception IllegalArgumentException If the data array is empty
     *   (or null), or the order is not positive.
     *   This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fft(double[] x, int order) {
        // NOTE: This could instead use the slightly more efficient
        // FFT that is possible when the data is real.
        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: empty array argument.");
        }
        if (order <= 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: order argument must be positive.");
        }
        // size = 2**order
        int size = 1 << order;
        double[] reals = new double[size];
        double[] imags = new double[size];
        // Copy the array.  Zero filling is implicit because Java
        // always initializes arrays.
        int length = x.length;
        if (reals.length < length) length = reals.length;
        System.arraycopy(x,0,reals,0,length);
        _fft(reals, imags, order, false);
        Complex[] result = new Complex[size];
        for (int i = 0; i < size; i++) {
            result[i] = new Complex(reals[i], imags[i]);
        }
        return result;
    }

    /** Return the discrete Fourier transform of the specified
     *  data array, given as an array containing the real parts and an array
     *  containing the imaginary parts.
     *  This is computed by the radix-two FFT algorithm.
     *  The size of the array arguments must be a power of two or an
     *  exception is thrown.
     *
     *  @param reals The real part of the data to transform
     *  @param imags The imaginary part of the data to transform
     *  @exception IllegalArgumentException If the array argument has length
     *   that is not a power of two. This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static void fftInPlace(double[] reals, double[] imags) {
        if (reals == null || reals.length == 0 ||
                imags == null || imags.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInPlace: empty array argument.");
        }
        if (reals.length != imags.length) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInPlace: arrays have different length.");
        }

        // Calculate the order of the FFT.
        double m = Math.log(reals.length)*_LOG2SCALE;
        double exp = Math.ceil(m);
        int order = (int) exp;
        // size = 2**order
        int size = 1 << order;
        if (size != reals.length) {
            throw new IllegalArgumentException("SignalProcessing.fftInPlace: "
                    + "In-place fft requires an array argument with length "
                    + "that is a power of two. Got length: " + reals.length);
        }

        _fft(reals, imags, order, false);
    }

    /** Return the inverse discrete Fourier transform of the specified
     *  data array, given as an array containing the real parts and an array
     *  containing the imaginary parts.
     *  This is computed by the radix-two FFT algorithm.
     *  The size of the array arguments must be a power of two or an
     *  exception is thrown.
     *
     *  @param reals The real part of the data to transform
     *  @param imags The imaginary part of the data to transform
     *  @exception IllegalArgumentException If the array argument has length
     *   that is not a power of two. This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static void fftInPlaceInverse(double[] reals, double[] imags) {
        if (reals == null || reals.length == 0 ||
                imags == null || imags.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInPlaceInverse: empty array argument.");
        }
        if (reals.length != imags.length) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInPlaceInverse: arrays have different "
                + "length.");
        }

        // Calculate the order of the FFT.
        double m = Math.log(reals.length)*_LOG2SCALE;
        double exp = Math.ceil(m);
        int order = (int) exp;
        // size = 2**order
        int size = 1 << order;
        if (size != reals.length) {
            throw new IllegalArgumentException(
                    "SignalProcessing.fftInPlaceInverse: "
                    + "In-place fft requires an array argument with length "
                    + "that is a power of two. Got length: " + reals.length);
        }

        _fft(reals, imags, order, true);
    }

    /** Return the inverse discrete Fourier transform
     *  of the argument <i>x</i>.  This is
     *  computed by the radix-two FFT algorithm with order being the next
     *  power of two larger than the length of the argument.
     *  The data are zero filled if necessary.
     *  Zero filling is done in the middle of the argument array
     *  (which corresponds to high frequencies).  If there is an odd number
     *  of elements in the argument array, then half the middle element is
     *  replicated on either side of the zero fill.  Also, if any element
     *  of the data array is null, it is assumed to have value zero.
     *
     *  @param x The data to transform
     *  @param order The log (base 2) of the size of the FFT
     *  @return The inverse DFT of the data
     *  @exception IllegalArgumentException If the argument is an empty
     *   array (or null), or if the order is not a positive
     *   integer.  This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fftInverse(Complex[] x) {

        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInverse: empty array argument.");
        }
        // Calculate the order of the FFT.
        double m = Math.log(x.length)*_LOG2SCALE;
        double exp = Math.ceil(m);
        return fftInverse(x, (int)exp);
    }

    /** Return the inverse discrete Fourier transform
     *  of the first argument <i>x</i>.  This is
     *  computed by the radix-two FFT algorithm with order given by the
     *  second argument.  The size of the FFT is
     *  <i>size</i> = 2<sup><i>order</i></sup>.
     *  The data are zero filled if necessary.
     *  Zero filling is done in the middle of the argument array
     *  (which corresponds to high frequencies).  If there is an odd number
     *  of elements in the argument array, then half the middle element is
     *  replicated on either side of the zero fill.  Also, if any element
     *  of the data array is null, it is assumed to have value zero.
     *
     *  @param x The data to transform
     *  @param order The log (base 2) of the size of the FFT
     *  @return The inverse DFT of the data
     *  @exception IllegalArgumentException If the argument is an empty
     *   array (or null), or if the order is not a positive
     *   integer.  This is a runtime exception, so it need
     *   not be declared by the caller explicitly.
     */
    public static Complex[] fftInverse(Complex[] x, int order) {

        if (x == null || x.length == 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInverse: empty array argument.");
        }
        if (order <= 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fftInverse: order must be positive.");
        }
        // size = 2**order
        int size = 1 << order;
        // Copy the array, zero filling if necessary.
        double[] reals = new double[size];
        double[] imags = new double[size];
        // Copy the array.  Zero filling is implicit because Java
        // always initializes arrays.
        // For the inverse FFT, need to zero-fill in the middle
        // (high frequencies).
        for (int i = 0; i < x.length/2; i++) {
            int k = x.length-i-1;
            if (k != i) {
                reals[i] = x[i].real;
                imags[i] = x[i].imag;
                reals[size-i-1] = x[k].real;
                imags[size-i-1] = x[k].imag;
            } else {
                reals[i] = x[i].real/2.0;
                imags[i] = x[i].imag/2.0;
                reals[size-i-1] = x[k].real/2.0;
                imags[size-i-1] = x[k].imag/2.0;
            }
        }
        _fft(reals, imags, order, true);
        Complex[] result = new Complex[size];
        for (int i = 0; i < size; i++) {
            result[i] = new Complex(reals[i], imags[i]);
        }
        return result;
    }

    /** Return a new array containing the angles of the specified
     *  complex array.
     *  @param array A complex array.
     *  @return An array of angles in the range of <em>-pi</em> to <em>pi</em>.
     */
    public static double[] phase(Complex[] array) {
        double[] angles = new double[array.length];
        for (int i = array.length-1; i >= 0; i--) {
            angles[i] = array[i].angle();
        }
        return angles;
    }

    /** Given an array of pole locations, an array of zero locations, and a
     *  gain term, return frequency response specified by these.
     *  This is calculated by walking around the unit circle and forming
     *  the product of the distances to the zeros, dividing by the product
     *  of the distances to the poles, and multiplying by the gain.
     *  The length of the returned array is (int)2*PI/step.
     *
     *  @param poles An array of pole locations.
     *  @param zeros An array of zero locations.
     *  @param gain A complex gain.
     *  @param step The resolution of the returned frequency response,
     *   in radians.
     */
    public static Complex[] poleZeroToFreq(Complex[] poles, Complex[] zeros,
            Complex gain, double step){
        Complex[] freq = new Complex[(int)(2*Math.PI/step)];

        double angle = -Math.PI;
        for (int index = 0; index < freq.length; index++){
            Complex polescontrib = new Complex(1);
            Complex zeroscontrib = new Complex(1);
            Complex ejw = new Complex(Math.cos(angle), Math.sin(angle));
            if (poles.length > 0) {
                Complex[] diffpoles = ArrayMath.subtract(poles, ejw);
                polescontrib = ArrayMath.product(diffpoles);
            }
            if (zeros.length > 0) {
                Complex[] diffzeros = ArrayMath.subtract(zeros, ejw);
                zeroscontrib = ArrayMath.product(diffzeros);
            }
            freq[index] = zeroscontrib.divide(polescontrib);
            freq[index].multiply(gain);
            angle += step;
        }
        return freq;
    }

    /** Return the next power of two larger than the argument.
     *  @param x A positive real number.
     */
    public static int powerOfTwo(double x) {
        if (x <= 0.0) {
            throw new IllegalArgumentException("SignalProcessing.powerOfTwo: "
                    + "argument is not a positive number: " + x);
        }
        double m = Math.log(x)*_LOG2SCALE;
        int exp = (int)Math.ceil(m);
        return 1 << exp;
    }

    /** Return a sample of a raised cosine pulse, or if the third
     *  argument is zero, a sin(x)/x function.  The first argument <em>t</em> is
     *  the time of the sample (the pulse is centered at zero). The second
     *  argument <em>T</em> is the time of the first zero crossing.
     *  This would be the symbol interval in a communications application
     *  of this pulse. The third argument <em>excess</em> is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.
     *  <p>
     *  The function that is computed is:
     *  <p>
     *  <pre>
     *         sin(pi t/T)   cos(excess pi t/T)
     *  h(n) = ----------- * -----------------
     *          pi t/T      1-(2 excess t/T)<sup>2</sup>
     *  </pre>
     *  <p>
     *  This is called a "raised cosine pulse" because in the frequency
     *  domain its shape is that of a raised cosine.
     *  <p>
     *  This implementation is ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.
     *
     *  @param t The sample time.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return A sample of a raised cosine pulse.
     */
    public static double raisedCosine(double t, double T, double excess) {
        if (t == 0.0) return 1.0;
        double x = t/T;
        double s = Math.sin(Math.PI * x) / (Math.PI * x);
        if (excess == 0.0) return s;
        x *= excess;
        double denominator = 1.0 - 4 * x * x;
        // If the denominator is close to zero, take it to be zero.
        if (close(denominator, 0.0)) {
            return s * Math.PI/4.0;
        }
        return s * Math.cos (Math.PI * x) / denominator;
    }

    /** Return a new array containing a raised cosine pulse, computed using
     *  the raisedCosine() method. The first argument is the time of the first
     *  sample.  The second is the number of desired samples.
     *  The third argument is the sample period.
     *  The fourth arguments is the time is the time of the first zero crossing
     *  (after time zero).
     *  This would be the symbol interval in a communications application
     *  of this pulse. The fifth argument is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.  Note that the pulse is centered
     *  at time zero.
     *  <p>
     *  For some applications, you may wish to apply a window function to
     *  this impulse response, since it is rather abruptly terminated
     *  at the two ends.
     *
     *  @param start The time of the first sample.
     *  @param length The number of desired samples.
     *  @param period The sample period.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return An array containing a raised cosine pulse.
     */
    public static double[] raisedCosinePulse(double start, int length,
            double period, double T, double excess) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = raisedCosine(start + i*period, T, excess);
        }
        return result;
    }

    /** Return a sample of a sawtooth wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at the falling edge with value -1.0.
     *  If it is 0.25, it begins at +0.5.
     *
     *  @param period The period of the sawtooth wave.
     *  @param phase The phase of the sawtooth wave.
     *  @param time The time of the sample.
     *  @return A number in the range -1.0 to +1.0.
     */
    public static double sawtooth(double period, double phase, double time) {
        double point = ((time/period)+phase+0.5)%1.0;
        return 2.0*point-1.0;
    }

    /** Return sin(x)/x, the so-called sinc function.
     *  If the argument is very close to zero, significant quantization
     *  errors may result (exactly 0.0 is OK, since this just returns 1.0).
     *
     *  @param x A number.
     *  @return The sinc function.
     */
    public static double sinc(double x) {
        if (x == 0.0) return 1.0;
        return Math.sin(x) / x;
    }

    /** Return a sample of a square wave with the specified period and
     *  phase at the specified time.  The returned value is 1 or -1.
     *  A sample that falls on the rising edge of the square wave is
     *  assigned value +1.  A sample that falls on the falling edge is
     *  assigned value -1.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the square wave begins at the start of the +1.0 phase.  If it is 0.5, it
     *  begins at the start of the -1.0 phase. If it is 0.25, it begins halfway
     *  through the +1.0 portion of the wave.
     *
     *  @param period The period of the square wave.
     *  @param phase The phase of the square wave.
     *  @param time The time of the sample.
     *  @return +1.0 or -1.0.
     */
    public static double square(double period, double phase, double time) {
        double point = (time+(phase*period))%period;
        return (point < period/2.0)?1.0:-1.0;
    }

    /** Return a sample of a square-root raised cosine pulse.
     *  The function computed is:
     *  <p>
     *  <pre>
     *         4 x(cos((1+x)pi t/T) + T sin((1-x)pi t/T)/(4n x/T))
     *  h(n) = ---------------------------------------------------
     *                       pi sqrt(T)(1-(4 x t/T)<sup>2</sup>)
     *  </pre>
     *  <p>
     *  where <i>x</i> is the the excess bandwidth.
     *  This pulse convolved with itself will, in principle, be equal
     *  to a raised cosine pulse.  However, because the pulse decays rather
     *  slowly for low excess bandwidth, this ideal is not
     *  closely approximated by short finite approximations of the pulse.
     *  <p>
     *  This implementation is ported from the Ptolemy 0.x implementation
     *  by Joe Buck, Brian Evans, and Edward A. Lee.
     *  Reference: E. A. Lee and D. G. Messerschmitt,
     *  <i>Digital Communication, Second Edition</i>,
     *  Kluwer Academic Publishers, Boston, 1994.
     *
     *  @param t The time of the sample.
     *  @param T The time of the first zero crossing of the corresponding
     *   raised cosine pulse.
     *  @param excess The excess bandwidth of the corresponding
     *   raised cosine pulse.
     */
    public static double sqrtRaisedCosine(double t, double T, double excess) {
        double sqrtT = Math.sqrt(T);
        if (t == 0) {
            return ((4*excess/Math.PI) + 1 - excess)/sqrtT;
        }

        double x = t/T;
        if (excess == 0.0) {
            return sqrtT*Math.sin(Math.PI * x)/(Math.PI * t);
        }

        double oneplus = (1.0 + excess)*Math.PI/T;
        double oneminus = (1.0 - excess)*Math.PI/T;
        // Check to see whether we will get divide by zero.
        double denominator = t*t*16*excess*excess - T*T;
        if (close(denominator, 0.0)) {
            return (T * sqrtT/(8 * excess * Math.PI * t)) *
                (oneplus * Math.sin(oneplus * t) -
                        (oneminus * T/(4 * excess * t)) * Math.cos(oneminus * t) +
                        (T/(4 * excess * t * t)) * Math.sin(oneminus * t) );
        }
        return (4 * excess / (Math.PI*sqrtT)) *
            (Math.cos(oneplus * t) + Math.sin(oneminus * t)/(x * 4 * excess)) /
            (1.0 - 16 * excess * excess * x * x);
    }

    /** Return a new array containing a square-root raised cosine pulse,
     *  computed using the sqrtRaisedCosine() method. The first
     *  argument is the time of the first sample. The second is
     *  the number of desired samples.  The third argument is the sample period.
     *  The fourth arguments is the time is the time of the first zero crossing
     *  (after time zero) of the corresponding raised-cosine pulse (the square).
     *  This would be the symbol interval in a communications application
     *  of this pulse. The fifth argument is the excess
     *  bandwidth, which is normally in the range of 0.0 to 1.0, corresponding
     *  to 0% to 100% excess bandwidth.  Note that the pulse is centered
     *  at time zero.
     *  <p>
     *  For some applications, you may wish to apply a window function to
     *  this impulse response, since it is rather abruptly terminated
     *  at the two ends.
     *
     *  @param start The time of the starting sample.
     *  @param length The number of desired samples.
     *  @param period The sample period.
     *  @param T The time of the first zero crossing.
     *  @param excess The excess bandwidth (in the range 0.0 to 1.0).
     *  @return An array containing a raised cosine pulse.
     */
    public static double[] sqrtRaisedCosinePulse(double start, int length,
            double period, double T, double excess) {
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = sqrtRaisedCosine(start + i*period, T, excess);
        }
        return result;
    }

    /** Return a sample of a triangle wave with the specified period and
     *  phase at the specified time.  The returned value ranges between
     *  -1.0 and 1.0.  The phase is given as a fraction of a cycle,
     *  typically ranging from 0.0 to 1.0.  If the phase is 0.0 or 1.0,
     *  the wave begins at zero with a rising slope.  If it is 0.5, it
     *  begins at zero with a falling slope. If it is 0.25, it begins at +1.0.
     *
     *  @param period The period of the triangle wave.
     *  @param phase The phase of the triangle wave.
     *  @param time The time of the sample.
     *  @return A number in the range -1.0 to +1.0.
     */
    public static double triangle(double period, double phase, double time) {
        double point = ((time/period)+phase+0.25)%1.0;
        return (point < 0.5)?(4.0*point-1.0):(((1.0-point)*4.0)-1.0);
    }

    /** Modify the specified array to unwrap the angles.
     *  That is, if the difference between successive values is greater than
     *  <em>pi</em> in magnitude, then the second value is modified by
     *  multiples of 2<em>pi</em> until the difference is less than <em>pi</em>.
     *  In addition, the first element is modified so that its difference from
     *  zero is less than <em>pi</em> in magnitude.  This method is used
     *  for generating more meaningful phase plots.
     */
    public void unwrap(double[] angles) {
        double previous = 0.0;
        for (int i = angles.length-1; i >= 0; i--) {
            while (angles[i] - previous < -Math.PI) {
                angles[i] += 2*Math.PI;
            }
            while (angles[i] - previous > -Math.PI) {
                angles[i] -= 2*Math.PI;
            }
            previous = angles[i];
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public variables                        ////

    /** A small number, used by algorithms to detect whether a double is close
     *  to zero.
     */
    public static final double EPSILON = 1.0e-9;

    /////////////////////////////////////////////////////////////////////////
    ////                         private methods                         ////

    // Replace the specified arrays with the discrete Fourier transform
    // or the inverse DFT of its value.  This is computed by the in-place
    // radix-two FFT algorithm with order given by the second argument.
    // The size of the FFT is <i>size</i> = 2<sup><i>order</i></sup>,
    // which must be equal to the length of the array argument (this is
    // not checked, so the caller should ensure it).
    // This is adapted from "C Language Algorithms for Digital
    // Signal Processing," Paul M. Embree and Bruce Kimble, Prentice-Hall,
    // 1991, P.258.
    // @param reals The real values of the data to transform
    // @param imags The imaginary values of the data to transform
    // @param order The log (base 2) of the size of the FFT
    // @param inverse If true, compute the inverse DFT
    // @exception IllegalArgumentException If the order is not a positive
    //  integer, or if the length of the array argument is not a power of two.
    //  This is a runtime exception, so it need
    //  not be declared by the caller explicitly.
    private static void _fft(double[] reals, double[] imags,
            int order, boolean inverse) {
        if (order <= 0) {
            throw new IllegalArgumentException(
                "SignalProcessing.fft: Invalid order argument: " + order);
        }
        // size = 2**order
        int size = 1 << order;

        // Begin by computing the twiddle factors if they have not been already.
        if (_twiddleReals == null) {
            _twiddleReals = new Vector();
            _twiddleImags = new Vector();
        }
        if (order > _twiddleReals.size()) {
            _twiddleReals.setSize(order);
            _twiddleImags.setSize(order);
        }
        double[] twReals = (double[])_twiddleReals.elementAt(order-1);
        double[] twImags;
        if (twReals == null) {
            // Need to compute the twiddle factors.
            // First, allocate the memory.
            int le = size/2;
            twReals = new double[le];
            twImags = new double[le];
            _twiddleReals.setElementAt(twReals, order-1);
            _twiddleImags.setElementAt(twImags, order-1);

            // Next, the angle increment.
            double arg = 2.0*Math.PI/size;
            // Then the corresponding vector
            double rotationReal = Math.cos(arg);
            double rotationImag = Math.sin(arg);
            // Then the starting twiddle factor
            double wrecurReal = rotationReal;
            double wrecurImag = rotationImag;
            int index;
            for (index = 0; index < le - 1; index++) {
                twReals[index] = wrecurReal;
                twImags[index] = wrecurImag;
                double temp = wrecurReal*rotationReal - wrecurImag*rotationImag;
                wrecurImag = wrecurReal*rotationImag + wrecurImag*rotationReal;
                wrecurReal = temp;
            }
            twReals[index] = wrecurReal;
            twImags[index] = wrecurImag;
        } else {
            twImags = (double[])_twiddleImags.elementAt(order-1);
        }

        int le = size;
        int windex = 1;
        for (int index = 0; index<order; index++){
            le = le >> 1;

            // first iteration has no multiplies
            for (int i = 0; i < size; i += 2*le) {
                int k = i+le;
                double tempR = reals[i];
                double tempI = imags[i];
                reals[i] = tempR+reals[k];
                imags[i] = tempI+imags[k];
                reals[k] = tempR-reals[k];
                imags[k] = tempI-imags[k];
            }

            // remaining iterations use twiddle factors
            int ii = windex-1;
            for (int j = 1; j < le; j++){
                double wReal = twReals[ii];
                double wImag = twImags[ii];
                if (inverse) {
                    wImag = -wImag;
                }
                for (int i = j; i < size; i = i+2*le) {
                    int k = i+le;
                    double tempR = reals[i];
                    double tempI = imags[i];
                    reals[i] = tempR + reals[k];
                    imags[i] = tempI + imags[k];
                    double diffR = tempR - reals[k];
                    double diffI = tempI - imags[k];
                    reals[k] = diffR*wReal - diffI*wImag;
                    imags[k] = diffR*wImag + diffI*wReal;
                }
                ii = ii + windex;
            }
            windex = windex << 1;
        }

        // rearrange data by bit reversing
        int j = 0;
        for (int i = 1;i<(size-1);i++){
            int k = size/2;
            while(k <= j){
                j = j - k;
                k = k/2;
            }
            j = j+k;
            if (i<j){
                double tempR = reals[j];
                double tempI = imags[j];
                reals[j] = reals[i];
                imags[j] = imags[i];
                reals[i] = tempR;
                imags[i] = tempI;
            }
        }

        // scale all result by 1/size if we are performing the inverse fft
        if (inverse) {
            double scale = 1.0/size;
            for (int i = 0; i < size; i++){
                reals[i] *= scale;
                imags[i] *= scale;
            }
        }
    }

    // Return a two-element array with the roots of the quadratic
    // <em>ax</em><sup>2</sup> + <em>bx</em> + <em>c</em>
    // (i.e., the values of x that make this zero).
    // This is private because it temporarily substitutes
    // for a more general root-finding algorithm.
    // FiXME: This is not used anywhere.  Delete.
    private static Complex[] _rootsQuadratic(double a, double b, double c) {
        Complex[] roots = new Complex[2];

        double discrim = b*b-4.0*a*c;

        if (discrim < 0) {
            roots[0] = new Complex(-b/(2*a), Math.pow(-discrim, 0.5)/(2*a));
            roots[1] = new Complex(-b/(2*a), -Math.pow(-discrim, 0.5)/(2*a));
        } else {
            // Adapted from "Numerical Recipes in C: The Art of Scientific
            // Computing" (ISBN 0-521-43108-5), pgs 183-84
            double q = -0.5*(b+ExtendedMath.sgn(b)*Math.sqrt(discrim));
            roots[0] = new Complex(q/a);
            roots[1] = new Complex(c/q);
        }

        return roots;
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         private members                         ////

    private static final double _LOG10SCALE = 1/Math.log(10);
    private static final double _LOG2SCALE = 1/Math.log(2);

    // A vector of twiddle factors for FFTs of various orders, so they
    // don't have to be recomputed each time.
    private static Vector _twiddleReals;
    private static Vector _twiddleImags;
}

