/* Abstract base class for tokens that contain a scalar.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
//// ScalarToken
/**
Abstract base class for tokens that contain a scalar.
This class defines methods for type conversion among different scalar
tokens. The implementation in this base class just throws an exception.
Derived class should override the methods that the corresponding
conversion can be achieved without loss of information.

@author Yuhong Xiong, Mudit Goel
@version $Id$
*/
public abstract class ScalarToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of this token as a Complex.
     *  In this base class, we just throw an exception.
     *  @return A Complex
     *  @exception IllegalActionException always thrown
     */
    public Complex complexValue()
	    throws IllegalActionException {
	throw new IllegalActionException("Cannot convert the value in " +
    		getClass().getName() + " to a Complex losslessly.");
    }

    /** Return the value of this token as a double.
     *  In this base class, we just throw an exception.
     *  @return A double
     *  @exception IllegalActionException always thrown
     */
    public double doubleValue()
	    throws IllegalActionException {
	throw new IllegalActionException("Cannot convert the value in " +
    		getClass().getName() + " to a double losslessly.");
    }

    // Return the value of this token as a Fix.
    // In this base class, we just throw an exception.
    // @return A Fix
    // @exception IllegalActionException always thrown.
    //
    // FIXME: restore this method after the Fix class is available.
    //    public Fix fixValue()
    //        throws IllegalActionException {
    //	throw new IllegalActionException("Cannot convert the value in " +
    //		getClass().getName() + " to a Fix losslessly.");
    // }

    /** Return the value of this token as an int.
     *  In this base class, we just throw an exception.
     *  @return An integer
     *  @exception IllegalActionException always thrown
     */
    public int intValue()
	    throws IllegalActionException {
	throw new IllegalActionException("Cannot convert the value in " +
    		getClass().getName() + " to an int losslessly.");
    }

    /** Return the value of this token as a long integer.
     *  In this base class, we just throw an exception.
     *  @return A long
     *  @exception IllegalActionException always thrown.
     */
    public long longValue()
	    throws IllegalActionException {
	throw new IllegalActionException("Cannot convert the value in " +
    		getClass().getName() + " to a long losslessly.");
    }
}

