/* Some object or set of objects has a state that in theory is not permitted.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// InvalidStateException
/**
Some object or set of objects has a state that in theory is not
permitted. E.g., a NamedObj has a null name. Or a topology has
inconsistent or contradictory information in it, e.g. an entity
contains a port that has a different entity as it container. Our
design should make it impossible for this exception to ever occur,
so occurrence is a bug. This exception supports all the constructor
forms of KernelException, but is implemented as a RuntimeException
so that it does not have to be declared.

@author Edward A. Lee
@version $Id$
*/
public class InvalidStateException extends RuntimeException {

    // NOTE: This class has much duplicated code with KernelException,
    // but because it needs to be a RuntimeException, there seemed to
    // be no way to avoid this.  Should there be an interface defined
    // for the commonality?

    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public InvalidStateException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param obj The object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable obj, String detail) {
        this(obj, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public InvalidStateException(Nameable obj1, Nameable obj2,
            String detail) {
        String obj1string = _getFullName(obj1);
        String obj2string = _getFullName(obj2);
        String prefix;
        if (!obj1string.equals("")) {
            if (!obj2string.equals("")) {
                prefix = new String(obj1string + " and " + obj2string);
            } else {
                prefix = obj1string;
            }
        } else {
            prefix = obj2string;
        }
        _setMessage(prefix);
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(new String(prefix + ": " + detail));
                } else {
                    _setMessage(detail);
                }
            }
        }
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of an enumeration of nameable plus the an argument string.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public InvalidStateException( Enumeration objects, String detail) {
        String prefix = "";
        while(objects.hasMoreElements()) {
            Nameable obj = (Nameable)objects.nextElement();
            prefix +=  _getFullName(obj)+": ";
        }
        _setMessage(prefix);
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(new String(prefix + detail));
                } else {
                    _setMessage(detail);
                }
            }
        }
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the detail message. */
    public String getMessage() {
        return _message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     *  @param obj An object with a name.
     *  @return The name of the argument.
     */
    protected String _getName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            name = obj.getName();
            if (name.equals("")) {
                name = new String("<Unnamed Object>");
            }
        }
        return name;
    }

    /** Get the name of a Nameable object.  This method attempts to use
     *  getFullName(), if it is defined, and resorts to getName() if it is
     *  not.  If the argument is a null reference, return an empty string.
     *  @param obj An object with a full name.
     *  @return The full name of the argument.
     */
    protected String _getFullName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            try {
                name = obj.getFullName();
            } catch (InvalidStateException ex) {
                name = obj.getName();
            }
        }
        return name;
    }

    /** Sets the error message to the specified string.
     *  @param msg The message.
     */
    protected void _setMessage(String msg) {
        _message = msg;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The detail message.
    private String _message ;
}
