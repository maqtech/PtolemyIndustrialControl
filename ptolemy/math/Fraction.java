/* A Fraction.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/



// do we want to make this a token class?
// upside: consistent interface, might be useful for domains.
// downside: work

package ptolemy.math;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Fraction
/**
A Fraction class.  Yes, it works just the way you'd expect.

@author Stephen Neuendorffer
@version $Id$
*/
public class Fraction {
    /** Create a new fraction.   Default Value = 0
     */
    public Fraction() {
        _num = 0;
        _den = 1;
	_simplify();
    }

    /** Create a new fraction.   Default Value = i;
     */
    public Fraction(int i) {
        _num = i;
        _den = 1;
	_simplify();
    }

    /** Create a new Fraction.   Default Value = Numerator/Denominator;
     */
    public Fraction(int Numerator, int Denominator) {
                if(Denominator == 0)
                    throw new ArithmeticException("Illegal Fraction: " +
                            "cannot Divide by zero");

        _num = Numerator;
        _den = Denominator;
	_simplify();
    }

    /** Create a new Fraction.   Default value = Fraction;
     */
    public Fraction(Fraction f) {
        _num = f._num;
        _den = f._den;
	_simplify();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the Numerator of an existing Fraction
     */
    /*   public void setNumerator(int Numerator) {
        _num = Numerator;
    }
    */
    /** Set the Denominator of an existing Fraction
     */
    /*public void setDenominator(int Denominator) {
                if(Denominator == 0)
                    throw new ArithmeticException("Illegal Fraction: " +
                            "cannot Divide by zero");
                _den = Denominator;
		}*/

    /** Multiply this fraction by the given fraction.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction multiply(Fraction b) {
        Fraction f = new Fraction(_num * b._num, _den * b._den);
        return f;
    }

    /** Divide this fraction by the given fraction.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction divide(Fraction b) {
        Fraction f = new Fraction(_num * b._den, _den * b._num);
	return f;
    }

    /** Add this fraction to the given fractions.
     *  @return The answer as another fraction in lowest terms.
     */
    public Fraction add(Fraction b) {
        Fraction f = new Fraction(
                _num * b._den + _den * b._num, _den * b._den);
        return f;
    }

    /** Subtract The given fraction from this fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction subtract(Fraction b) {
       Fraction f = new Fraction(
               _num * b._den - _den * b._num, _den * b._den);
        return f;
    }

    /** Negate a Fraction.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction negate() {
        Fraction f = new Fraction(-_num, _den);
        return f;
    }

    /** Invert a Fractions.
     *  @return The answer as another fraction in lowest terms
     */
    public Fraction inverse() {
        Fraction f = new Fraction(_den, _num);
        return f;
    }

    /** Does this fraction have the same value as the given fraction?
     *  The Fractions are compared in lowest terms.
     */
    public boolean equals(Fraction b) {
	_simplify();
        b._simplify();
        return ((_num == b._num) && (_den == b._den));
    }

    /** Convert the fraction to a readable string
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
	_simplify();
        s.append(_num);
        s.append('/');
        s.append(_den);
        return s.toString();
    }

    /** Return the Numerator
     */
    public int getNumerator() {
        return _num;
    }

    /** Return the Denominator
     */
    public int getDenominator() {
        return _den;
    }

    public static Fraction ZERO = new Fraction(0, 1);

    /** Implement Euclid's method for finding the Greatest Common Divisor of
     *  two numbers
     */
    // FIXME: These should be moved to the Math Package

    public static int gcd(int u, int v) {
        int t;
        while(u > 0) {
            if(u < v) { t = u; u = v; v = t; }
            u = u - v;
        }
        return v;
    }

    /** Finds the least common multiple of two integers
     */
    public static int lcm(int u, int v) {
        int gcd = gcd(u, v);
        int result = u * v / gcd;
        return result;
    }

   /** Reduce the fraction to lowest terms by dividing the Numerator and
     *  Denominator by their Greatest Common Divisor.  In addition the 
     *  fraction is put in standard form (denominator greater than zero).
     */
    private void _simplify() {
        int factor = gcd(_num, _den);
        _num = _num / factor;
        _den = _den / factor;
        // Standardize the sign
        if(_den < 0) {
            _den = -_den;
            _num = -_num;
        }
    }

    private int _num;
    private int _den;


}
