/** A class representing the type of a multi-dimensional array.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.data.type;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.graph.*;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;

//////////////////////////////////////////////////////////////////////////
//// ArrayType
/**
A class representing the type of an array.

@author Steve Neuendorffer, Yuhong Xiong
$Id$
*/

public class ArrayType extends StructuredType {

    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.NAT.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
	if (elementType == null) {
	    throw new IllegalArgumentException("ArrayType: elementType is"
			+ " null");
	}

	_setElementType(elementType);
	_declaredElementType = elementType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an ArrayToken having the
     *  type represented by this object.
     *  @param t A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token t)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, t.getType());
	if (compare == CPO.INCOMPARABLE || compare == CPO.LOWER) {
	    throw new IllegalArgumentException("ArrayType.convert: " +
		"Cannot convert the argument token to this type.");
	}

	// argument must be an ArrayToken.
	Token[] argArray = ((ArrayToken)t).arrayValue();
	Token[] result = new Token[argArray.length];
	for (int i = 0; i < argArray.length; i++) {
	    result[i] = _elementType.convert(argArray[i]);
	}

	return new ArrayToken(result);
    }

    /** Return the type of the array elements. This methods always
     *  returns the argument passed into the constructor.
     *  @return a Type.
     */
    public Type getElementType() {
	return _elementType;
    }

    /** Return the InequalityTerm representing the element type.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public InequalityTerm getElementTypeTerm() {
	if (_elemTypeTerm == null) {
	    _elemTypeTerm = new ElementTypeTerm(this);
	}
	return _elemTypeTerm;
    }

    /** Return the user of this StructuredType. If the user is not set,
     *  return null.
     *  @return An Object.
     */
    public Object getUser() {
	return _user;
    }

    /** Test if this ArrayType is a constant. An ArrayType is a constant if
     *  it does not contain BaseType.NAT in any level.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
	return _declaredElementType.isConstant();
    }

    /** Determine if the argument represents the same ArrayType as this
     *  object.
     *  @param t A Type.
     *  @return True if the argument represents the same ArrayType as
     *   this object; false otherwise.
     */
    public boolean isEqualTo(Type t) {
	if ( !(t instanceof ArrayType)) {
	    return false;
	}
	return _elementType.isEqualTo(((ArrayType)t).getElementType());
    }

    /** Determine if this type corresponds to an instantiable token
     *  class. An ArrayType is instantiable if its element type is
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
	return _elementType.isInstantiable();
    }

    /** Set the user of this ArrayType. The user can only be set once,
     *  otherwise an exception will be thrown.
     *  @param Object The user.
     *  @exception IllegalActionException If the user is already set, or
     *   if the argument is null.
     */
    public void setUser(Object user)
	    throws IllegalActionException {
	if (_user != null) {
	    throw new IllegalActionException("ArrayType._setUser: " +
		"The user is already set.");
	}

	if (user == null) {
	    throw new IllegalActionException("ArrayType._setUser" +
		"The specified user is null.");
	}

	_user = user;
    }

    /** Return the string representation of this type. The format is
     *  (<type>) array, where <type> is is the elemenet type.
     *  @return A String.
     */
    public String toString() {
	return "(" + _elementType.toString() + ") array";
    }

    /** Return a deep copy of this ArrayType if it is a variable, or
     *  itself if it is a constant. The returned copy does
     *  not have the user set.
     *  @return An ArrayType.
     */
    public Object clone() {
	ArrayType newObj = new ArrayType(_declaredElementType);
	if ( !isConstant()) {
	    try {
	        newObj.updateType(this);
	    } catch (IllegalActionException ex) {
		throw new InternalErrorException("ArrayType.clone: Cannot " +
			"update new instance. " + ex.getMessage());
	    }
	}

	return newObj;
    }

    /** Update this Type to the specified ArrayType. This Type must not
     *  be a constant, otherwise an exception will be thrown. 
     *  The specified type must have the same structure as this type.
     *  This method will only update the component whose declared type is
     *  BaseType.NAT, and leave the constant part of this type intact.
     *  This method does not check for circular usage, the caller should.
     *  @param st A StructuredType.
     *  @exception IllegalActionException If this Type is a constant, or
     *   the specified type has a different structure.
     */
    public void updateType(StructuredType newType)
	    throws IllegalActionException {
	if (isConstant()) {
	    throw new IllegalActionException("ArrayType.updateType: " +
		"Cannot update a constant type.");
	}

	if ( !(newType instanceof ArrayType)) {
	    throw new IllegalActionException("ArrayType.updateType: " +
		"The specified type is not an ArrayType.");
	}

	Type newElemType = ((ArrayType)newType).getElementType();
	if (_declaredElementType == BaseType.NAT) {
	    _setElementType(newElemType);
	} else {
	    // _declaredElementType is a StructuredType. _elementType
	    // must also be.
	    ((StructuredType)_elementType).updateType(
						(StructuredType)newElemType);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compare this type with the specified type. The specified type
     *  must be an ArrayType, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param t an ArrayType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected int _compare(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.compare: " +
		"The argument is not an ArrayType.");
	}

	return TypeLattice.compare(_elementType,
				   ((ArrayType)t).getElementType());
    }

    /** Determine if the specified StructuredType is this object, or
     *  a user of this type, or a user of a higher level.
     *  @return True if the above condition is true.
     */
    protected boolean _deepIsUser(Object st) {
	if (st == this) {
	    return true;
	}

	if (_user != null && (_user instanceof StructuredType)) {
	    return ((StructuredType)_user)._deepIsUser(st);
	}

	return false;
    }

    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
     */
    protected StructuredType _getRepresentative() {
	return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _greatestLowerBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementGLB = (Type)TypeLattice.lattice().greatestLowerBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementGLB);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _leastUpperBound(StructuredType t) {
	if ( !(t instanceof ArrayType)) {
	    throw new IllegalArgumentException("ArrayType.leastUpperBound: "
		+ "The argument is not an ArrayType.");
	}

	Type elementLUB = (Type)TypeLattice.lattice().leastUpperBound(
			    _elementType, ((ArrayType)t).getElementType());
	return new ArrayType(elementLUB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    // Set the elementType. Clone and set the user of the specified
    // element type if necessary.
    private void _setElementType(Type elementType) {
	if (elementType instanceof BaseType) {
            _elementType = elementType;
	} else {
	    // elementType is a StructuredType
	    StructuredType elemTypeStruct = (StructuredType)elementType;

	    if (elemTypeStruct.isConstant()) {
                _elementType = elementType;
	    } else {
	        // elementType is a non-constant StructuredType
		try {
	            if (elemTypeStruct.getUser() == null) {
		        elemTypeStruct.setUser(this);
                        _elementType = elementType;
		    } else {
		        // user already set, clone elementType
		        StructuredType newElemType =
				(StructuredType)elemTypeStruct.clone();
		        newElemType.setUser(this);
		        _elementType = newElemType;
		    }
		} catch (IllegalActionException ex) {
		    // since the user was null, this should never happen.
		    throw new InternalErrorException(
			"ArrayToken._setElementType: " +
			" Cannot set user on the elementType. " +
			ex.getMessage());
		}
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // the type of array elements.
    private Type _declaredElementType;
    private Type _elementType;

    private Object _user = null;

    private ElementTypeTerm _elemTypeTerm = null;

    private static ArrayType _representative = new ArrayType(BaseType.NAT);

    ///////////////////////////////////////////////////////////////////
    ////                           inner class                     ////

    private class ElementTypeTerm implements InequalityTerm {

        // Pass the ArrayType reference in the constructor so it can be
	// returned by getAssociatedObject().
	private ElementTypeTerm(ArrayType t) {
	    _arrayType = t;
	}

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this ArrayType.
         *  @return an ArrayType.
         */
    	public Object getAssociatedObject() {
	    return _arrayType;
	}

        /** Return the element type.
         *  @return a Type.
         */
        public Object getValue() {
	    return _elementType;
	}

        /** Return this ElementTypeTerm in an array if this term
	 *  represents a type variable. Otherwise, return an array of
	 *  size zero.
	 *  @return An array of InequalityTerm.
	 */
    	public InequalityTerm[] getVariables() {
	    if ( !isConstant()) {
		InequalityTerm[] variable = new InequalityTerm[1];
		variable[0] = this;
		return variable;
	    }
	    return (new InequalityTerm[0]);
	}

        /** Test if the element type is a type variable.
	 *  @return True if the element type is a type variable.
     	 */
    	public boolean isSettable() {
	    return !isConstant();
	}

        /** Check whether the current element type is acceptable.
	 *  The element type is acceptable if it represents an
	 *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
	    return isInstantiable();
	}

    	/** Set the element type if it is settable.
         *  @param e a Type.
         *  @exception IllegalActionException If the element type is
	 *   not settable.
     	 */
    	public void setValue(Object e)
             throws IllegalActionException {
	    if (!isSettable()) {
	    	throw new IllegalActionException("ElementTypeTerm.setValue:" +
		" The element type cannot be changed.");
	    }

	    // check for circular type containment
	    if (e instanceof StructuredType) {
		if (_arrayType._deepIsUser(e)) {
		    throw new IllegalActionException(
			"ElementTypeTerm.setValue: Attempt to construct " +
			"circular type structure.");
		}
	    }


	    if (_declaredElementType == BaseType.NAT) {
		_elementType = (Type)e;
	    } else {
		if ( !(e instanceof StructuredType)) {
		    // The LUB of the _elementType and another type is General,
		    // this is a type conflict.

		    // FIXME Should throw TypeConflictException
		    // LinkedList conflict = new LinkedList();
		    // conflict.add(_arrayType);
		    // throw new TypeConflictException(conflict.elements(),
		    //    "Type conflict occurs when updating array element "
		    //    + "type. Old type: " + _elementType.toString() +
		    //    + "; New type: " + e.toString());

		    throw new IllegalActionException("Type conflict occurs " +
			" when updating array element type. Old type: " +
			_elementType.toString() + "; New type: " +
		  	e.toString());

		}
	        ((StructuredType)_elementType).updateType((StructuredType)e);
	    }
	}

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

	private ArrayType _arrayType = null;
    }
}

