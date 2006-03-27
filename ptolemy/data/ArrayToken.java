/* A token that contains an array of tokens.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.data;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ArrayToken

/**
 A token that contains an array of tokens.  The operations between
 arrays are defined pointwise, and require that the lengths of the
 arrays are the same.  The elements of the ArrayToken will be converted
 to the least upper bound of their input types and zero length array
 tokens cannot be created.

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh) nil token code
 */
public class ArrayToken extends AbstractNotConvertibleToken {
    /** Construct an ArrayToken with the specified token array. All
     *  the tokens in the array must have the same type, otherwise an
     *  exception will be thrown.  Generally, the type of the array
     *  created is determined by the type of the first element in the
     *  given array.  This class makes a copy of the given array, so
     *  the passed array may be reused.
     *  @param value An array of tokens.
     *  @exception IllegalActionException If the tokens in the array
     *   do not have the same type, or the length of the given array is
     *   zero.
     */
    public ArrayToken(Token[] value) throws IllegalActionException {
        // NOTE: This code is purposefully duplicated from
        // _initialize() for the benefit of the code generator.
        // Otherwise type inference has to propagate through the
        // _initialize method correctly, which is hard.
        if (value.length == 0) {
            throw new IllegalActionException("The "
                    + "length of the specified array is zero.");
        }

        
        Type elementType = null;
        int length = value.length;
        int nonNilIndex = 0;
        // Look for the first non-nil token to get the type.
        while (nonNilIndex < length) {
            if (!value[nonNilIndex].isNil()) {
                _elementPrototype = value[nonNilIndex];
                elementType = value[nonNilIndex].getType();
                break;
            }
            nonNilIndex++;
        }
        if (elementType == null) {
            // Did not find a non-nil element, use the 0th.
            nonNilIndex = 0;
            elementType = value[0].getType();
            _elementPrototype = value[0];

        }


        // It would be nice to have this, but the Code generator cannot
        // deal with the least upper bound.
        //    for (int i = 0; i < length; i++) {
        //             Type valueType = value[i].getType();
        //             if (!elementType.equals(valueType)) {
        //                 _elementType = TypeLattice.leastUpperBound(
        //                         elementType, valueType);
        //             }
        //         }
        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            if (elementType.equals(value[i].getType())) {
                _value[i] = value[i]; // _elementType.convert(value[i]);
            } else {
                if (value[i].isNil()) {
                    // Construct a nil token, same type as elementType.
                    _value[i] = Token.NIL;
                } else {
                    throw new IllegalActionException(
                            "Elements of the array do not have the same type:"
                            + "value[0]=" + value[0]
                            + " (type: " + elementType + ")"
                            + " value[" + i + "]=" + value[i]
                            + " (type: " + value[i].getType() + ")");

                }
            }
        }
    }

    /** Construct an ArrayToken from the specified string.
     *  The format of the string is a list of comma separated
     *  token values that begins with "{" and ends with "}".
     *  For example
     *  <code>
     *  "{1, 2, 3}"
     *  </code>
     *  @param init A string expression of an array.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable array.
     */
    public ArrayToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        Token token = evaluator.evaluateParseTree(tree);

        if (token instanceof ArrayToken) {
            Token[] value = ((ArrayToken) token)._value;
            _initialize(value);
        } else {
            throw new IllegalActionException("An array token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an empty ArrayToken with the element type of the
     *  specified token. If the token parameter is a nil token, then a
     *  new nil ArrayToken is returned, see 
     *  {@link ptolemy.data.Token#NIL}.

     *  @param elementPrototype A token specifying the element type.
     */
    public ArrayToken(Token elementPrototype) {
        _value = new Token[0];
        _elementPrototype = elementPrototype;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert an ArrayToken to an array of unsigned bytes.
     *
     * @param dataArrayToken to be converted to a unsigned byte array.
     * @return dataBytes the resulting unsigned byte array.
     */
    public static byte[] arrayTokenToUnsignedByteArray(ArrayToken dataArrayToken) {
        byte[] dataBytes = new byte[dataArrayToken.length()];

        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken = (UnsignedByteToken) dataArrayToken
                    .getElement(j);
            dataBytes[j] = dataToken.byteValue();
        }

        return dataBytes;
    }

    /** Return an array of tokens populated with the contents of this
     *  array token.  The returned array is a copy so the caller is
     *  free to modify it.
     *  @return An array of tokens.
     */
    public Token[] arrayValue() {
        Token[] result = new Token[_value.length];

        // This code will create a token array of a more specific type
        // than token.  Eventually, we would like to use this code
        // since it will simplify writing some actors, but for the
        // moment the code generator cannot deal with it.
        // (Token[])
        //             java.lang.reflect.Array.newInstance(
        //                     getElementType().getTokenClass(),
        //                     _value.length);
        if (_value.length > 0) {
            System.arraycopy(_value, 0, result, 0, _value.length);
        }

        return result;
    }

    /** Add the given token to each element of this array.
     *  @param token The token to be added to this token.   
     *  @return A new array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be added to an element of this token.
     */
    public ArrayToken elementAdd(Token token) throws IllegalActionException {
        Token[] result = new Token[_value.length];

        try {
            for (int i = 0; i < _value.length; i++) {
                result[i] = _value[i].add(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementAdd", this, token));
        }

        return new ArrayToken(result);
    }

    /** Divide each element of this array by the given token.
     *  @param token The token which which to divide this token
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be divided into an element of this token.
     */
    public ArrayToken elementDivide(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];

        try {
            for (int i = 0; i < _value.length; i++) {
                result[i] = _value[i].divide(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementDivide", this, token));
        }

        return new ArrayToken(result);
    }

    /** Modulo each element of this array by the given token.
     *  @param token The token with which to modulo this token. 
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be used with modulo.
     */
    public ArrayToken elementModulo(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];

        try {
            for (int i = 0; i < _value.length; i++) {
                result[i] = _value[i].modulo(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementModulo", this, token));
        }

        return new ArrayToken(result);
    }

    /** Multiply each element of this array by the given token.
     *  @param token The token with which to multiply this token. 
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be multiplied to an element of this token.
     */
    public ArrayToken elementMultiply(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];

        try {
            for (int i = 0; i < _value.length; i++) {
                result[i] = _value[i].multiply(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementMultiply", this, token));
        }

        return new ArrayToken(result);
    }

    /** Return the (exact) return type of the elementMultiply function
     *  above.  This returns a new array type whose element type is
     *  the least upper bound of the element type of this token and
     *  the given type.
     *  @param type1 The type of the base of the corresponding function.
     *  @param type2 The type of the argument of the corresponding function.
     *  @return The type of the value returned from the corresponding
     *  function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public static Type elementMultiplyReturnType(Type type1, Type type2)
            throws IllegalActionException {
        if (type1 instanceof ArrayType) {
            return new ArrayType(TypeLattice.leastUpperBound(
                    ((ArrayType) type1).getElementType(), type2));
        } else {
            return new ArrayType(BaseType.UNKNOWN);
        }
    }

    /** Subtract the given token from each element of this array.
     *  @param token The token to subtract from this token.
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be subtracted from an element of this token.
     */
    public ArrayToken elementSubtract(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];

        try {
            for (int i = 0; i < _value.length; i++) {
                result[i] = _value[i].subtract(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementSubtract", this, token));
        }

        return new ArrayToken(result);
    }

    /** Return true if the class of the argument is ArrayToken and of
     *  the same length and the elements are equal to that of this
     *  token.  Equality of the contained elements is tested by their
     *  equals() method.
     *  @param object the object to compare with.
     *  @return True if the argument is an array token of the same length
     *   and the elements are equal to that of this token.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        ArrayToken arrayArgument = (ArrayToken) object;
        int length = arrayArgument.length();

        if (_value.length != length) {
            return false;
        }

        Token[] array = arrayArgument._value;

        for (int i = 0; i < length; i++) {
            if (!_value[i].equals(array[i]) || _value[i].isNil()) {
                return false;
            }
        }

        return true;
    }

    /** Extract a non-contiguous subarray either by giving a boolean array
     *  of the same length of this array describing which elements to
     *  include and which to include, or by giving an an array of an
     *  arbitrary length giving the indices of elements from this array
     *  to include in the subarray.  An example of the first form is
     *  {"red","green","blue"}.extract({true,false,true}), which evaluates
     *  to {"red", "blue"}.  An example of the second form is
     *  {"red","green","blue"}.extract({2,0,1,1}), which evalues to
     *  {"blue", "red", "green", "green"}.
     *  @param selection An ArrayToken describing the selection of elements
     *   with which to form the subarray: either an array of integer
     *   indices, or an array of boolean inclusion/exclusion choices.
     *  @return An ArrayToken containing the extracted subarray.
     *  @exception IllegalActionException If the argument type is invalid
     *   or the result cannot be constructed.
     *  @exception ArrayIndexOutOfBoundsException  If the argument is an array
     *   of integers, and one or more of those integers is not a valid
     *   index into this array.
     *  @since Ptolemy II 4.1
     */
    public ArrayToken extract(ArrayToken selection)
            throws IllegalActionException {
        List result = new LinkedList();

        if (selection.getElementType() == BaseType.BOOLEAN) {
            if (selection.length() != length()) {
                throw new IllegalActionException(
                        "When the argument is an array of booleans, it must have "
                                + "the same length as this array.");
            }

            for (int i = 0; i < selection.length(); i++) {
                if (selection.getElement(i).equals(BooleanToken.TRUE)) {
                    result.add(getElement(i));
                }
            }
        } else if (selection.getElementType() == BaseType.INT) {
            for (int i = 0; i < selection.length(); i++) {
                // We could check for out-of-bounds indicies and ignore them,
                // if we wanted to.
                int index = ((IntToken) (selection.getElement(i))).intValue();
                result.add(getElement(index));
            }
        } else {
            throw new IllegalActionException(
                    "The argument must be {boolean} or {int}.");
        }

        if (result.size() > 0) {
            Token[] resultArray = new Token[result.size()];
            resultArray = (Token[]) (result.toArray(resultArray));
            return new ArrayToken(resultArray);
        } else {
            return new ArrayToken(getElementPrototype());
        }
    }

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundsException If the specified index is
     *   outside the range of the token array.
     */
    public Token getElement(int index) {
        return _value[index];
    }

    /** Return a token whose type matches the element type of this
     *   array.
     *  @return A token whose type matches the element type.
     */
    public Token getElementPrototype() {
        return _elementPrototype;
    }

    /** Return the type contained in this ArrayToken.
     *  @return A Type.
     */
    public Type getElementType() {
        return _elementPrototype.getType();
    }

    /** Return the type of this ArrayToken.
     *  @return An ArrayType.
     */
    public Type getType() {
        return new ArrayType(getElementType());
    }

    /** Return a hash code value for this token. This method returns
     *  the hash code of the first element, or of the prototype
     *  if the array is empty.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        return _elementPrototype.hashCode();
    }

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    public int length() {
        return _value.length;
    }

    /** Return a new ArrayToken representing the multiplicative
     *  identity.  The returned token contains an array of the same
     *  size as the array contained by this token, and each element of
     *  the array in the returned token is the multiplicative identity
     *  of the corresponding element of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by the element token.
     */
    public Token one() throws IllegalActionException {
        Token[] oneValueArray = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            oneValueArray[i] = _value[i].one();
        }

        return new ArrayToken(oneValueArray);
    }

    /** Return the contiguous subarray starting at the specified index and
     *  of the specified length. If the specified index is out of range,
     *  or if the specified length extends beyond the end of the array,
     *  then return an empty array with the same type as this array.
     *  @param index The index of the beginning of the subarray.
     *  @param count The length of the subarray.
     *  @return The extracted subarray.
     *  @exception IllegalActionException If the index argument is
     *   less than zero.
     *  @since Ptolemy II 4.1
     */
    public ArrayToken subarray(int index, int count)
            throws IllegalActionException {
        if (index < 0) {
            throw new IllegalActionException(
                    "index argument of subarray() must be non-negative.");
        }

        if ((count > 0) && (index < _value.length) && (index >= 0)) {
            if ((count + index) > _value.length) {
                count = _value.length - index;
            }

            Token[] result = new Token[count];
            System.arraycopy(_value, index, result, 0, count);
            return new ArrayToken(result);
        } else {
            return new ArrayToken(getElementPrototype());
        }
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A string beginning with "{" that contains expressions
     *  for every element in the array separated by commas, ending with "}".
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("{");

        for (int i = 0; i < _value.length; i++) {
            buffer.append(_value[i].toString());

            if (i < (_value.length - 1)) {
                buffer.append(", ");
            }
        }

        buffer.append("}");
        return buffer.toString();
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes data to be converted to an ArrayToken.
     * @return dataArrayToken the resulting ArrayToken.
     * @exception IllegalActionException If ArrayToken can not be created.
     */
    public static ArrayToken unsignedByteArrayToArrayToken(byte[] dataBytes)
            throws IllegalActionException {
        int bytesAvailable = dataBytes.length;
        Token[] dataArrayToken = new Token[bytesAvailable];

        for (int j = 0; j < bytesAvailable; j++) {
            dataArrayToken[j] = new UnsignedByteToken(dataBytes[j]);
        }

        return new ArrayToken(dataArrayToken);
    }

    /** Returns a new ArrayToken representing the additive identity.
     *  The returned token contains an array of the same size as the
     *  array contained by this token, and each element of the array
     *  in the returned token is the additive identity of the
     *  corresponding element of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If additive identity is not
     *  supported by an element token.
     */
    public Token zero() throws IllegalActionException {
        Token[] zeroValueArray = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            zeroValueArray[i] = _value[i].zero();
        }

        return new ArrayToken(zeroValueArray);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  token added to the value of this token.  It is assumed that
     *  this class is the class of the argument.
     *  @param rightArgument The token whose value we add to the value
     *  of this token.
     *  @return A new array token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the add method of
     *  an element token throws it.
     */
    protected Token _add(Token rightArgument) throws IllegalActionException {
        _checkArgumentLength(rightArgument);

        ArrayToken rightArray = (ArrayToken) rightArgument;
        Token[] result = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].add(rightArray.getElement(i));
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  It is assumed that
     *  this class is the class of the argument.
     *  @param rightArgument The token to divide this token by
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the divide method
     *  of the element token throws it.
     */
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        _checkArgumentLength(rightArgument);

        ArrayToken rightArray = (ArrayToken) rightArgument;
        Token[] result = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].divide(rightArray.getElement(i));
        }

        return new ArrayToken(result);
    }

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the argument is
     *         an ArrayToken, and the isCloseTo() method of the array elements
     *         is used.
     *  @param token The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @exception IllegalActionException If the elements do not support
     *   this comparison.
     *  @return A true-valued token if the first argument is close
     *  to this token.
     */
    protected BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        ArrayToken rightArray = (ArrayToken) token;

        if (length() != rightArray.length()) {
            return BooleanToken.FALSE;
        }

        for (int i = 0; i < _value.length; i++) {
            // Here is where isCloseTo() differs from isEqualTo().
            // Note that we return false the first time we hit an
            // element token that is not close to our current element token.
            BooleanToken result = _value[i].isCloseTo(rightArray.getElement(i),
                    epsilon);

            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a true-valued token if the argument is equal to this one.
     *  The isEqualTo() method of the element tokens is used to make the
     *         comparison.  It is assumed that the argument is an ArrayToken.
     *  @param token The token to compare to this token.
     *  @exception IllegalActionException If the element types do not
     *   support this comparison.
     *  @return A true-valued token if the argument is equal.
     */
    protected BooleanToken _isEqualTo(Token token)
            throws IllegalActionException {
        _checkArgumentLength(token);

        ArrayToken rightArray = (ArrayToken) token;

        for (int i = 0; i < _value.length; i++) {
            BooleanToken result = _value[i].isEqualTo(rightArray.getElement(i));

            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  this class is the class of the argument.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the modulo method
     *  of the element token throws it.
     */
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        _checkArgumentLength(rightArgument);

        ArrayToken rightArray = (ArrayToken) rightArgument;
        Token[] result = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].modulo(rightArray.getElement(i));
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that this class is the class of the argument.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the multiply method
     *  of the element token throws it.
     */
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        _checkArgumentLength(rightArgument);

        ArrayToken rightArray = (ArrayToken) rightArgument;
        Token[] result = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].multiply(rightArray.getElement(i));
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is assumed
     *  that this class is the class of the argument.
     *  @param rightArgument The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the subtract method
     *  of the element token throws it.
     */
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        _checkArgumentLength(rightArgument);

        ArrayToken rightArray = (ArrayToken) rightArgument;
        Token[] result = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].subtract(rightArray.getElement(i));
        }

        return new ArrayToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Throw an exception if the argument is not an ArrayToken of the
    // same length.
    private void _checkArgumentLength(Token token)
            throws IllegalActionException {
        int length = ((ArrayToken) token).length();

        if (length() != length) {
            throw new IllegalActionException("The length of the argument ("
                    + length
                    + ") is not the same as the length of this token ("
                    + length() + ").");
        }
    }

    // initialize this token using the specified array.
    private void _initialize(Token[] value) throws IllegalActionException {
        if (value.length == 0) {
            throw new IllegalActionException("The "
                    + "length of the specified array is zero.");
        }

        Type elementType = null;
        int length = value.length;
        int nonNilIndex = 0;
        // Look for the first non-nil token to get the type.
        while (nonNilIndex < length) {
            if (!value[nonNilIndex].isNil()) {
                _elementPrototype = value[nonNilIndex];
                elementType = value[nonNilIndex].getType();
                break;
            }
            nonNilIndex++;
        }
        if (elementType == null) {
            // Did not find a non-nil element, use the 0th.
            nonNilIndex = 0;
            elementType = value[0].getType();
            _elementPrototype = value[0];

        }


        // It would be nice to have this, but the Code generator cannot
        // deal with the least upper bound.
        //    for (int i = 0; i < length; i++) {
        //             Type valueType = value[i].getType();
        //             if (!elementType.equals(valueType)) {
        //                 _elementType = TypeLattice.leastUpperBound(
        //                         elementType, valueType);
        //             }
        //         }
        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            if (elementType.equals(value[i].getType())) {
                _value[i] = value[i]; // _elementType.convert(value[i]);
            } else {
                if (value[i].isNil()) {
                    // Construct a nil token, same type as elementType.
                    // FIXME: what?
                    _value[i] = Token.NIL;
                } else {
                    throw new IllegalActionException(
                            "Elements of the array do not have the same type:"
                            + "value[0]=" + value[0]
                            + " (type: " + elementType + ")"
                            + " value[" + i + "]=" + value[i]
                            + " (type: " + value[i].getType() + ")");

                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Token[] _value;

    private Token _elementPrototype;
}
