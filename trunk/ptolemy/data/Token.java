/* Base class for data capsules.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;

import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// Token
/**
Token is the base class for data capsules.  Tokens are immutable,
meaning that their value cannot change after construction.  They have
a set of polymorphic methods providing a set of basic arithmetic and
logical operations.  This base class implements these polymorphic
methods so that lossless type conversion is performed to convert the
tokens to the same type.  After performing type conversion, the actual
operations are performed by a set of protected methods.  This ensures
that type conversions are performed consistently across all data
types.  Generally, derived classes should override the protected
method to implement type specific operations that make sense for a
given type.  For operations that are non-sensical for a given type,
such as division of matrices, the implementation of this base class
can be used, which simply throws an exception.

<p> Instances of this base class can be used to represent pure events,
i.e., to indicate that an event is present. To support this use, the
toString() method returns the String "present".

@author Neil Smyth, Yuhong Xiong, Edward A. Lee, Christopher Hylands,
Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2

*/
public class Token implements Serializable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("add", this, rightArgument));
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token addReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("addReverse", this, leftArgument));
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divide", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token divided by the value of this token.
     *  @param leftArgument The token to be divided by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divideReverse", this, leftArgument));
    }

    /** Override the base class method to check whether the value of this
     *  token is equal to that of the argument.
     *  Since this base token class does not have any state, this method
     *  returns true if the argument is an instance of Token, but not an
     *  instance of a subclass of Token or any other classes.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of Token, but not an
     *   instance of a subclass of Token or any other classes.
     */
    public boolean equals(Object object) {
        if (object.getClass() == Token.class) {
            return true;
        }
        return false;
    }

    /** Return the type of this token.
     *  @return BaseType.GENERAL
     */
    public Type getType() {
        return BaseType.GENERAL;
    }

    /** Return a hash code value for this token. Since the equals() method
     *  in this base Token class returns true for all instances of Token,
     *  all instances of Token must have the same hash code. To achieve this,
     *  this method simply returns the value 0.
     *  @return The integer 0.
     */
    public int hashCode() {
        return 0;
    }

    /** Test that the value of this Token is close to the argument
     *  Token.  In this base class, we call isEqualTo().  This method
     *  should be overridden in derived classes such as DoubleToken
     *  and ComplexToken to provide type specific actions for
     *  equality testing using an epsilon factor.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *  value and units of this token are close to those of the
     *  argument token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
     */
    public final BooleanToken isCloseTo(Token token)
            throws IllegalActionException{
        return isCloseTo(token, ptolemy.math.Complex.epsilon);
    }

    /** Test that the value of this Token is close to the argument
     *  Token.
     *
     *  @param rightArgument The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return A boolean token that contains the value true if the
     *  value of this token are close to those of the
     *  argument token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("closeness", this, rightArgument));
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.
     *
     *  @param rightArgument The token with which to test equality.
     *  @return A BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be compared with this token.
     */
    public BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("equality", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("moduloReverse", this, leftArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token multiply(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiply", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token multiplied by the value of this token.
     *  @param leftArgument The token to be multiplied by the value of
     *  this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiplyReverse", this, leftArgument));
    }

    /** Return a string with an error message that states that
     *  operation is not supported between two tokens.
     *  @param operation A string naming the unsupported token
     *  @param firstToken The first token in the message.
     *  @param secondToken The first token in the message.
     *  @return A string error message.
     */
    public static String notSupportedMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " operation not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString() + "'");
    }


    /** Return a string with an error message that states that
     *  the given token cannot be converted to the given token type.
     *  @param token The token being converted.
     *  @param typeString A string representing the type that is being
     *  converted to.
     *  @return A string error message.
     */
    public static String notSupportedConversionMessage(
            Token token, String typeString) {
        // We use this method to factor out a very common message
        return ("Conversion is not supported from "
                + token.getClass().getName()
                + " '" + token.toString()
                + "' to the type "
                + typeString + ".");
    }

    /** Return a string with an error message that states that
     *  operation is not supported between two tokens, because they
     *  have incomparable types and cannot be converted to the same type.
     *  @param operation A string naming the unsupported token
     *  @param firstToken The first token in the message.
     *  @param secondToken The first token in the message.
     *  @return A string error message.
     */
    public static String notSupportedIncomparableMessage(String operation,
            Token firstToken, Token secondToken) {
        // We use this method to factor out a very common message
        return (operation + " method not supported between "
                + firstToken.getClass().getName()
                + " '" + firstToken.toString()
                + "' and "
                + secondToken.getClass().getName()
                + " '" + secondToken.toString()
                + "' because the types are incomparable.");
    }

    /** Return a string with an error message that states that
     *  the given token cannot be converted a given token type.
     *  @param token The token being converted.
     *  @param typeString A string representing the type that is being
     *  converted to.
     *  @return A string error message.
     */
    public static String notSupportedIncomparableConversionMessage(
            Token token, String typeString) {
        // We use this method to factor out a very common message
        return ("Conversion is not supported from "
                + token.getClass().getName()
                + " '" + token.toString()
                + "' to the type " + typeString
                + " because the type of the token is higher "
                + "or incomparable with the given type.");
    }

    /** Returns a new Token representing the multiplicative identity.
     *  It should be overridden in subclasses.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the multiplicative identity.
     */
    public Token one() throws IllegalActionException {
        throw new IllegalActionException(
                "Multiplicative identity not supported on "
                + this.getClass().getName() + ".");
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtractReverse", this, leftArgument));
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  This method should be overridden by derived classes.
     *  In this base class, return the String "present" to indicate
     *  that an event is present.
     *  @return The String "present".
     */
    public String toString() {
        return "present";
    }

    /** Returns a new token representing the additive identity.
     *  It should be overridden in subclasses.
     *  @return A new Token containing the additive identity.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     */
    public Token zero() throws IllegalActionException {
        throw new IllegalActionException(
                "Token.zero: Additive identity not supported on "
                + this.getClass().getName() + ".");
    }
}
