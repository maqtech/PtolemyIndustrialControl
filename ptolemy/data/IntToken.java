/* A Particle that contains an integer

 Copyright (c) 1997 The Regents of the University of California.
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
*/

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// IntToken
/** 
A token that contains an integer, or more specifically a reference to an 
instance of an integer. The reference is never null, the default being 0.

@author Mudit Goel
@version $Id$
*/
public class IntToken extends ScalarToken {

    /** Construct a token with integer 0
     */	
    public IntToken() {
	_value = 0;
    }

    /** Construct a token with the specified integer
     */
    public IntToken(int value) {
        _value = value;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////


    /** Converts value to byte and returns it
     */
    public byte byteValue() {
        return (byte)_value;
    }

    /** Converts value to double and returns it
     */
    public double doubleValue() {
        return (double)_value;
    }

    /** Converts value to int and returns it
     */
    public int intValue() {
        return (int)_value;
    }

    /** Converts value to long and returns it
     */
    public long longValue() {
        return (long)_value;
    }
    

    /////////////////////////////////////////////////////////////////////////
    ////                        private variables                        ////
 
    private int _value = 0;

}




