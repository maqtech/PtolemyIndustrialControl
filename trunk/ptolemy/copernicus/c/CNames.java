/* A class that determines names of various entities to use for C code generation.

Copyright (c) 2001 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.HashMap;

import soot.*;

/** A class that determines names of various entities to use for C code 
 *  generation. 
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */

public class CNames {

    // Private constructor to prevent instantiation of this class.
    private CNames() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine the C name for the class-specific structure type that
     *  implements a Soot class. The class-specific structure has
     *  type "struct {@link #classNameOf(SootClass)}". Additionally,
     *  the identifier {@link #classNameOf(SootClass)} (i.e., without
     *  the struct qualifier) is defined in the generated code to be a pointer type 
     *  that points to the class-specific structure. 
     *  @param source The class.
     *  @return The C name for the class-specific structure type. 
     */
    public static String classNameOf(SootClass source) {
        return ("C" + instanceNameOf(source));
    }

    /** Determine the C name for the class-specific structure variable that
     *  implements a Soot class. The type of this structure is 
     *  the type pointed to by the pointer type {@link #classNameOf(SootClass)}.
     *  @param source The class.
     *  @return The C name for the class-specific structure variable. 
     */
    public static String classStructureNameOf(SootClass source) {
        return ("V" + instanceNameOf(source));
    }

    /** Clear the set of local variable names.
     */
    public static void clearLocalNames() {
        _localMap = new HashMap();
    }

    /** Return the name associated with a field in a Soot class.
     *  @param field the field.
     *  @return the name.
     */
    public static String fieldNameOf(SootField field) {
        String name;
        if ((name = (String)(_nameMap.get(field))) == null) {
            // Hash the type signature to avoid naming conflicts associated
            // with names that are longer than the number of significant
            // characters in a C identifier.
            Integer prefixCode = new Integer(field.getSubSignature().hashCode());
            name = _sanitize("f" + prefixCode + "_" + field.getName());
            _nameMap.put(field, name);
        }
        return name;
    }

    /** Return the name of the C function that implements a given Soot method.
     *  @param method the method.
     *  @return the function name.
     */
    public static String functionNameOf(SootMethod method) {
        String name;
        if ((name = (String)(_functionMap.get(method))) == null) {
            if (method.isNative()) {
               name = ((method.getDeclaringClass().getName()) + "_" +
                       method.getName()).replace('.', '_');
            } else {
                // Hash the class name + type signature combination to avoid naming 
                // conflicts. 
                String prefixBase = method.getDeclaringClass().getName() 
                        + method.getSubSignature();
                Integer prefixCode = new Integer(prefixBase.hashCode());
                name = _sanitize("f" + prefixCode + "_" + method.getName());
            }
            _functionMap.put(method, name);
        }
        return name;
    }

    /** Return the include file name for a given class. 
     *  @param source the class.
     *  @return the include file name.
     */
    public static String includeFileNameOf(SootClass source) {
        return source.getName().replace('.', '/')  + ".h";
    }

    /** Given a class, return the name of the function that implements
     *  initialization of the class, including all functionality in the
     *  static initializer for the class (if it exists), and all
     *  class-level initialization required on the C data structures that
     *  implement the class.
     *  When called, this function must be passed the address of the 
     *  variable given by {@link #classStructureNameOf}.
     *  @param source the class.
     *  @return the function name.
     */
    public static String initializerNameOf(SootClass source) {
        String name;
        if ((name = (String)(_initializerMap.get(source))) == null) {
            final String suffix = "_init";
            String base = instanceNameOf(source) + suffix;
            Integer prefixCode = new Integer(base.hashCode());
            name = _sanitize("f" + prefixCode + suffix); 
            _initializerMap.put(source, name);
        }
        return name;
    }

    /** Determine the C name for the instance-specific structure type that
     *  implements a Soot class. The instance-specific structure has
     *  type "struct {@link #instanceNameOf(SootClass)}". Additionally,
     *  the identifier {@link #instanceNameOf(SootClass)} (i.e., without
     *  the struct qualifier) is defined in the generated code to be a pointer type 
     *  that points to the class-specific structure. 
     *  @param source The Soot class.
     *  @return The C name for the instance-specific structure type. 
     */
    public static String instanceNameOf(SootClass source) {
        if (_nameMap.containsKey(source)) return (String)(_nameMap.get(source));
        else return _instanceNameOf(source);
    }

    /** Return the name of a local.
     *  @param local the local.
     *  @return the name.
     */
    public static String localNameOf(Local local) {
        String name;
        if ((name = (String)(_localMap.get(local))) == null) {
            name = _sanitize("L" + local.getName());
            _localMap.put(local, name);
        }
        return name;
    }

    /** Return the name of the C structure member that represents 
     *  a given Soot method. The identifier returned by this method
     *  is a member of the structure that implements the associated class
     *  (see {@link #classNameOf(SootClass)}).
     *  @param method the Soot method.
     *  @return the name.
     */
    public static String methodNameOf(SootMethod method) {
        String name;
        if ((name = (String)(_nameMap.get(method))) == null) {
            // Hash the type signature to avoid naming conflicts for overloaded
            // methods.
            Integer prefixCode = new Integer(method.getSubSignature().hashCode());
            name = _sanitize("m" + prefixCode + "_" + method.getName());
            _nameMap.put(method, name);
        }
        return name;
    }

    /** Initialize C name generation. This method must be called once before any
     *  other method in this class is called.
     */ 
    public static void setup() {
        _functionMap = new HashMap();
        _initializerMap = new HashMap();
        _localMap = new HashMap();
        _nameMap = new HashMap();
    }

    /** Return the name of the class structure member that points to the superclass
     *  structure. Each structure that implements a class has as a member a pointer to
     *  the superclass. This method returns the name of this pointer member.
     *  @return the name of the pointer member.
     */
    public static String superclassPointerName() {
        return "superclass";
    }

    /** Determine the C name associated with a Soot type. For RefType types,
     *  the C name returned is the name of the instance-specific data structure.
     *  To obtain the name of the class-specific data strucuture associated with
     *  a RefType, see {@link #instanceNameOf(SootClass)}.
     *  @param type The type.
     *  @return The C name. 
     */
    public static String typeNameOf(Type type) {
        // FIXME: do this more efficiently.
        String name = null;
        if (type instanceof RefType) 
            name = instanceNameOf(((RefType)type).getSootClass());
        else if (type instanceof ArrayType) 
            name = typeNameOf(((ArrayType)type).baseType) + "[]";
        if (type instanceof BooleanType) name = "int"; 
        else if (type instanceof ByteType) name = "char"; 
        else if (type instanceof CharType) name = "char"; 
        else if (type instanceof DoubleType) name = "double"; 
        else if (type instanceof FloatType) name = "float"; 
        else if (type instanceof IntType) name = "int"; 
        else if (type instanceof LongType) name = "long"; 
        else if (type instanceof ShortType) name = "short"; 
        else if (type instanceof VoidType) name = "void"; 
        else new RuntimeException("Unsupported Soot type '"
                + type.getClass().getName() + "'");
        return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Derive a unique name for a class that is to be used as the
    //  name of the user-defined C type that implements instances of 
    //  the class.
    private static String _instanceNameOf(SootClass source) {
        String name = source.getName();

        // The choice of 'i' as the first letter stands for "instance."
        String className = (name.indexOf(".") < 0) ? name : 
                name.substring(name.lastIndexOf(".") + 1);
        Integer prefixCode = new Integer(name.hashCode());
        String CClassName = _sanitize("i" + prefixCode.toString() 
                + "_" + className);
        _nameMap.put(source, CClassName);
        return CClassName;
    }

    // Sanitize a name to be valid a C identifier.
    private static String _sanitize(String name) {
        return name.
                replace('-', '0').
                replace('<', '_').
                replace('>', '_').
                replace('$', '_');
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //  Map from a Java method to the name of the C function that implements
    //  the method. Keys are of type SootMethod. Values are of type String.
    private static HashMap _functionMap;

    //  Map from a class to the name of the function that implements
    //  instance-specific initialization for the class. Keys are of type SootClass.
    //  Values are of type String.
    private static HashMap _initializerMap;

    //  Map from a local to the identifier that represents the local in the
    //  generated C code. Keys are of type Local.  Values are of type String.
    private static HashMap _localMap;

    //  Map for names associated with the C structures that implement classes
    //  and class instances. Specifically, this map provides the names in the generated
    //  C code that correspond to class instances, instance fields, and
    //  class methods. Keys are of type SootClass, SootField, or SootMethod.
    //  Values are of type String.
    private static HashMap _nameMap;
}
