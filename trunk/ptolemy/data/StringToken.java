/* A particle that contains a string.

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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// StringToken
/**
A token that contains a string, or more specifically, a reference
to an instance of String.  The reference is never null, although it may
be an empty string ("").
Note that when this token is cloned, the clone will refer to exactly
the same String object.  However, a String object in Java is immutable,
so there is no risk when two tokens refer to the same string that
one of the strings will be changed.

@author Edward A. Lee, Neil Smyth
@version $Id$
@since Ptolemy II 0.2
*/
public class StringToken extends Token {

    /** Construct a token with an empty string.
     */
    public StringToken() {
        this("");
    }

    /** Construct a token with the specified string.
     */
    public StringToken(String value) {
        if (value != null) {
            _value = value;
        } else {
            _value = new String("");
        }
        _toString = "\"" + _value + "\"";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new StringToken whose value is the string value
     *  of the argument token appended to the String contained in
     *  this Token.
     *  @param token The token to add to this Token
     *  @exception IllegalActionException If the passed token is
     *   not of a type that can be added to this Tokens value.
     *  @return A new StringToken containing the result.
     */
    public Token add(Token token) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, token);
        try {
            if (token instanceof StringToken) {
                String result =
                    _value + ((StringToken)token).stringValue();
                return new StringToken(result);
            } else if (typeInfo == CPO.HIGHER) {
                StringToken tmp = (StringToken)this.convert(token);
                String result = _value + tmp.stringValue();
                return new StringToken(result);
            } else if (typeInfo == CPO.LOWER) {
                return token.addReverse(this);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new IllegalActionException("StringToken: add method not " +
                    "supported between " + getClass().getName() + " and " +
                    token.getClass().getName() + ": " + ex.getMessage());
        }
    }

    /** Return a new StringToken whose value is the string value
     *  of the argument token prepended to the String contained in
     *  this Token.
     *  @param token The token to concatenate this Token to.
     *  @exception IllegalActionException If the passed token
     *   is not of a type that can be added to this Tokens value.
     *  @return A new StringToken containing the result.
     */
    public Token addReverse(ptolemy.data.Token token)
	    throws IllegalActionException {
        StringToken tmp = (StringToken)this.convert(token);
        String result = tmp.stringValue() + _value;
        return new StringToken(result);
    }

    /** Convert the specified token into an instance of StringToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of StringToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below StringToken in the type hierarchy, it is converted to
     *  an instance of StringToken or one of the subclasses of
     *  StringToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a StringToken.
     *  @return A StringToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(BaseType.STRING, token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("StringToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with StringToken in the " +
                    "type hierarchy.");
	}

	if (token instanceof StringToken) {
	    return token;
	}

	if (token instanceof MatrixToken || token instanceof ScalarToken ||
                token instanceof BooleanToken) {
	    String str = token.toString();
	    return new StringToken(str);
	}
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"StringToken.");
    }

    /** Return true if the argument is an instance of StringToken with the
     *  same value.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of StringToken with the
     *  same value.
     */
    public boolean equals(Object object) {
	// This test rules out subclasses.
	if (object.getClass() != StringToken.class) {
	    return false;
	}

	if (((StringToken)object).stringValue().equals(_value)) {
	    return true;
	}
	return false;
    }

    /** Return the type of this token.
     *  @return BaseType.STRING
     */
    public Type getType() {
	return BaseType.STRING;
    }

    /** Return a hash code value for this token. This method returns the
     *  hash code of the contained string.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	return _value.hashCode();
    }

    /** Lexicographically test the values of this Token and the
     *  argument Token for equality. Return a new BooleanToken containing
     *  the result.
     *  @param token The token to lexicographically compare the value this
     *  Token with.
     *  @return BooleanToken indicating result of comparison.
     *  @exception IllegalActionException If the passed token
     *  is not of a type that can be compared this Tokens value.
     */
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
        if (token instanceof StringToken) {
            if ( toString().compareTo(token.toString()) == 0) {
                return new BooleanToken(true);
            } else {
                return new BooleanToken(false);
            }
        } else {
            throw new IllegalActionException(
                    _notSupportedMessage("equality", this, token));
        }
    }

    /** Return the string that this token contains.  Note that this is
     *  different than the toString method, which returns a string expression
     *  that has double quotes around it.
     *  @return The contained string.
     */
    public String stringValue() {
        return _value;
    }

    /** Return the value of this Token as a string.  The string will begin
     *  and end with double quotes, indicating a string constant in the
     *  expression language.
     *  @return A String.
     */
    public String toString() {
        return _toString;
    }

    /** Return a StringToken containing an empty string, which is considered
     *  as the additive identity of string.
     *  @return A new StringToken containing an empty string.
     */
    public Token zero() {
	return new StringToken("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The string contained in this token.
    private String _value;
    // The string contained in this token, with double quotes on either side.
    private String _toString;
}
