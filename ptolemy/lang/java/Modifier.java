/* Handle modifiers such as public

 Copyright (c) 2000-2001 The Regents of the University of California.
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

package ptolemy.lang.java;

//////////////////////////////////////////////////////////////////////////
//// Modifier
/** This class contains helper methods for the constants
 *  defined in JavaStaticSemanticConstants.  Unfortunately,
 *  most of this class is a duplicate of java.lang.reflect.Modifiers
@author Jeff Tsay
@version $Id$
 */
public class Modifier implements JavaStaticSemanticConstants {

    public static final String toString(final int modifier) {
        StringBuffer modString = new StringBuffer();

        if (modifier == NO_MOD) return "";

        if ((modifier & PUBLIC_MOD) != 0)
            modString.append("public ");

        if ((modifier & PROTECTED_MOD) != 0)
            modString.append("protected ");

        if ((modifier & PRIVATE_MOD) != 0)
            modString.append("private ");

        if ((modifier & ABSTRACT_MOD) != 0)
            modString.append("abstract ");

        if ((modifier & FINAL_MOD) != 0)
            modString.append("final ");

        if ((modifier & NATIVE_MOD) != 0)
            modString.append("native ");

        if ((modifier & SYNCHRONIZED_MOD) != 0)
            modString.append("synchronized ");

        if ((modifier & TRANSIENT_MOD) != 0)
            modString.append("transient ");

        if ((modifier & VOLATILE_MOD) != 0)
            modString.append("volatile ");

        if ((modifier & STATIC_MOD) != 0)
            modString.append("static ");

        if ((modifier & STRICTFP_MOD) != 0)
            modString.append("strictfp ");

        return modString.toString();
    }

    public static final void checkClassModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD |
                        ABSTRACT_MOD | STATIC_MOD | STRICTFP_MOD)) != 0) {
            throw new RuntimeException("Illegal class modifier: " +
                    toString(modifiers));
        }
    }

    public static final void checkInterfaceModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD |
                        ABSTRACT_MOD | STATIC_MOD | STRICTFP_MOD)) != 0) {
            throw new RuntimeException("Illegal interface modifier: " +
                    toString(modifiers));
        }
    }


    public static final void checkConstructorModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD)) != 0) {
            throw new RuntimeException("Illegal constructor modifier: " +
                    toString(modifiers));

        }
    }

    public static final void checkConstantFieldModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | STATIC_MOD | FINAL_MOD)) != 0) {
            throw new RuntimeException("Illegal constant field modifier: " +
                    toString(modifiers));
        }
    }

    public static final void checkFieldModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD |
                        FINAL_MOD | TRANSIENT_MOD | VOLATILE_MOD)) != 0) {
            throw new RuntimeException("Illegal field modifier : " +
                    toString(modifiers));
        }
    }

    public static final void checkLocalVariableModifiers(final int modifiers) {
        if ((modifiers & ~(FINAL_MOD)) != 0) {
            throw new RuntimeException("Illegal local variable modifier : " +
                    toString(modifiers));
        }
    }


    public static final void checkMethodModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD | STATIC_MOD | FINAL_MOD |
                        ABSTRACT_MOD | NATIVE_MOD | SYNCHRONIZED_MOD | STRICTFP_MOD)) != 0) {
            throw new RuntimeException("Illegal method modifier: " +
                    toString(modifiers));
        }
    }

    public static final void checkMethodSignatureModifiers(final int modifiers) {
        if ((modifiers &
                ~(PUBLIC_MOD | ABSTRACT_MOD)) != 0) {
            throw new RuntimeException("Illegal method signature modifier: " +
                    toString(modifiers));
        }
    }

    public static final void checkParameterModifiers(final int modifiers) {
        if ((modifiers & ~(FINAL_MOD)) != 0) {
            throw new RuntimeException("Illegal parameter modifier : " +
                    toString(modifiers));
        }
    }

    /** Convert java.lang.Modifiers into ptolemy.lang.java.Modifiers
     */
    public static final int convertModifiers(final int modifier) {
	int retval = 0;
	//System.out.println("convertModifiers(" + modifier + ") " +
	//		   java.lang.reflect.Modifier.toString(modifier));
	if (java.lang.reflect.Modifier.isPublic(modifier)) {
	    retval = retval | PUBLIC_MOD;
	}
	if (java.lang.reflect.Modifier.isProtected(modifier)) {
	    retval = retval | PROTECTED_MOD;
	}
	if (java.lang.reflect.Modifier.isPrivate(modifier)) {
	    retval = retval | PRIVATE_MOD;
	}
	if (java.lang.reflect.Modifier.isAbstract(modifier)) {
	    retval = retval | ABSTRACT_MOD;
	}
	if (java.lang.reflect.Modifier.isFinal(modifier)) {
	    retval = retval | FINAL_MOD;
	}
	if (java.lang.reflect.Modifier.isNative(modifier)) {
	    retval = retval | NATIVE_MOD;
	}
	if (java.lang.reflect.Modifier.isSynchronized(modifier)) {
	    retval = retval | SYNCHRONIZED_MOD;
	}
	if (java.lang.reflect.Modifier.isTransient(modifier)) {
	    retval = retval | TRANSIENT_MOD;
	}
	if (java.lang.reflect.Modifier.isVolatile(modifier)) {
	    retval = retval | VOLATILE_MOD;
	}
	if (java.lang.reflect.Modifier.isStatic(modifier)) {
	    retval = retval | STATIC_MOD;
	}
	if (java.lang.reflect.Modifier.isStrict(modifier)) {
	    retval = retval | STRICTFP_MOD;
	}
	return retval;
    }
}
