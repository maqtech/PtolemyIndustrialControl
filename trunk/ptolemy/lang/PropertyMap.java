/* A class on which to base objects that have properties.

Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// PropertyMap
/**
A class on which to base objects that have properties.
A property is an arbitrary object that is associated with the object
by a key.
@author Jeff Tsay
@version $Id$
 */
public class PropertyMap implements Cloneable {

    /** Make a deep copy of the property map, so that the new instance
     *  can have different values for the same property than those of
     *  the old instance.
     *  @return The new PropertyMap.
     */
    public Object clone() {
        PropertyMap propertyMap = null;
        // There's no reason that clone() should fail, so just catch the
        // theoretical exception.
        try {
            propertyMap = (PropertyMap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone not supported on PropertyNode");
        }

        // make a shallow copy of keys and values
        propertyMap._propertyMap = (HashMap) _propertyMap.clone();
        return propertyMap;
    }


    /** Define a property. Return false if the property is already defined. */
    public boolean defineProperty(Integer property) {
        Object object = setProperty(property, NullValue.instance);
        return (object == null);
    }

    /** Get a property.
     *  Throw a RuntimeException if the property in not defined.
     */
    public Object getDefinedProperty(Integer property) {
        Object returnValue = _propertyMap.get(property);
        if (returnValue == null) {
            throw new RuntimeException("Property " + property +
                    " not defined");
        }
        return returnValue;
    }

    /** Get a property. If the property is not defined, returned null. */
    public Object getProperty(Integer property) {
        return _propertyMap.get(property);
    }

    /** Set a property.
     *  Throw a RuntimeException if the property in not defined.
     */
    public Object setDefinedProperty(Integer property, Object object) {
        if (object == null) {
            object = NullValue.instance;
        }

        Object returnValue = _propertyMap.put(property, object);

        if (returnValue == null) {
            throw new RuntimeException("Property " + property +
                    " not defined");
        }
        return returnValue;
    }

    /** Return the a Set of the defined properties. */
    public Set keySet() {
        return _propertyMap.keySet();
    }

    /** Set a property.
     *  The property may or may not have been defined already.
     */
    public Object setProperty(Integer property, Object object) {
        if (object == null) {
            object = NullValue.instance;
        }
        return _propertyMap.put(property, object);
    }

    /** Remove a property,
     *  returning the value of the property if the property is
     *  defined. If the property is not defined, return null.
     */
    public Object removeProperty(Integer property) {
        return _propertyMap.remove(property);
    }

    /** Return true iff this instance has the specified property. */
    public boolean hasProperty(Integer property) {
        return _propertyMap.containsKey(property);
    }

    /** Return a Collection containing all of the property values. */
    public Collection values() {
        return _propertyMap.values();
    }


    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////

    // reserved properties

    /** The key that retrieves the List of return values of the child
     *  nodes, after accept() is called on all of them by
     *  TNLManip.traverseList().
     */
    public static final Integer CHILD_RETURN_VALUES_KEY = new Integer(-2);

    /** The key that retrieves indicating a numbering. */
    public static final Integer NUMBER_KEY = new Integer(-1);

    // 0 is reserved for a dummy value for the interrogator

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** A map from properties (instances of Integer) to values
     *  (instances of Objects). The initial capacity is set to 2 to
     *  conserve memory.
     */
    protected HashMap _propertyMap = new HashMap(2);
}
