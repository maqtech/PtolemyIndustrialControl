/*

A class that handles generation and management of Java methods that are
over-ridden by pre-defined C code.

Copyright (c) 2002-2003 The University of Maryland.
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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.HashSet;
import java.util.Iterator;

import soot.SootMethod;
import soot.SootClass;
import soot.Type;
import soot.VoidType;
import soot.BaseType;
import soot.ArrayType;
import soot.RefType;


/**
    A class that handles generation and management of Java methods that are
    over-ridden by pre-defined C code. The class allows conventional java
    methods to be replaced with pre-defined C code. This may be done for
    platform-specificness, correctness or performance considerations. Note
    that "overridden" here means that the code for a method is replaced
    with either dummy or user-defined C code. It does not refer to
    overriding methods by inheritance in java.

    @author Ankush Varma
    @version $Id$
*/

public class OverriddenMethodGenerator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

     /** Return the name of the file where the C code for an overridden method
      *  should be. This name also includes the appropriate directory path.
      *  @param method The method.
      *  @return The name of the file.
      */
     public static String fileContainingCodeFor(SootMethod method) {
        String fileName = overriddenBodyLib + CNames.functionNameOf(method)
                + ".c";

        return fileName;
     }

    /** Returns the code for a given overridden method.
     *
     *  @param method The method for which C code is needed.
     *  @return The code for the method.
     */
    public static String getCode(SootMethod method) {
        StringBuffer code = new StringBuffer("/* Function that implements "
                + "method " + method.getSignature() + "*/\n");

        code.append(_getHeaderCode(method) + "{\n");
        code.append(_getBodyCode(method) + "}\n");

        return code.toString();

    }


    /**
     * Perform initialization functions and set up the list of
     * force-overridden methods.
     */
    public static void init() {
        _forceOverriddenMethods = new HashSet();

        ///////////// Methods replaced with dummy code ////////////

        _forceOverriddenMethods.add(
                "<java.util.ResourceBundle: java.lang.Object "
                + "loadBundle(java.lang.ClassLoader,java.lang.String)>");
        // Overridden it throws a warning for an unused variable.

        _forceOverriddenMethods.add("<java.util.Locale: void <clinit>()>");
        // Overridden because called and used the result from an
        // unimplemented native.

        _forceOverriddenMethods.add("<java.lang.Character: void <clinit>()>");
        // Overridden because it threw an array error that causes a
        // segfault.


        _forceOverriddenMethods.add(
                "<java.security.MessageDigest: "
                + "java.security.MessageDigest getInstance(java.lang.String,"
                + "java.lang.String)>");
        // Overridden because inner classes have problems accessing private
        // fields of outer classes. Specifically outerClass cannot call
        // InnerClass.privateFieldOfOuterClass.


        _forceOverriddenMethods.add(
            "<java.security.Signature: java.security.Signature "
            + "getInstance(java.lang.String,java.lang.String)>");
        // Overridden because inner classes have problems accessing private
        // fields of outer classes. Specifically outerClass cannot call
        // InnerClass.privateFieldOfOuterClass.

        _forceOverriddenMethods.add(
            "<java.io.File: void <clinit>()>");

        _forceOverriddenMethods.add(
            "<java.lang.SecurityManager: "
            + "java.lang.ThreadGroup getRootGroup()>");


        _forceOverriddenMethods.add(
            "<sun.net.InetAddressCachePolicy: void <clinit>()>");
        // Overridden because Object.hashCode() is not up yet.

        _forceOverriddenMethods.add(
            "<java.io.ObjectOutputStream: long getUTFLength(char[],int)>");
        // Overridden because it threw a warning.

        _forceOverriddenMethods.add(
            "<java.lang.ClassLoader: void <clinit>()>");
        // Overridden because of the implicit "super()" it ends up calling
        // in Stack.

        _forceOverriddenMethods.add(
            "<java.lang.System: void <clinit>()>");
        // Overridden because nullPrintStream etc. are not up yet.

        _forceOverriddenMethods.add(
            "<sun.net.www.MimeTable: boolean "
            + "saveAsProperties(java.io.File)>");
        // Overridden it throws a warning for an unused variable.

        _forceOverriddenMethods.add(
            "<java.lang.reflect.Proxy: java.lang.Class "
            + "getProxyClass(java.lang.ClassLoader,java.lang.Class[])>");
        // Overridden it throws a warning for an unused variable.

        _forceOverriddenMethods.add(
            "<java.lang.reflect.AccessibleObject: void <clinit>()>");

        _forceOverriddenMethods.add(
            "<java.io.DataOutputStream: "
            + "int writeUTF(java.lang.String,java.io.DataOutput)>");
        // Overridden because it threw a warning.

        _forceOverriddenMethods.add(
            "<java.lang.InheritableThreadLocal: "
            + "void bequeath(java.lang.Thread,java.lang.Thread)>");
        // Overridden because call to an interface led to a statement with
        // no effect which caused a warning.

        _forceOverriddenMethods.add(
            "<java.util.ResourceBundle: "
            + "java.lang.Object "
            + "loadBundle(java.lang.ClassLoader,"
            + "java.lang.String,java.util.Locale)>");
        // Overridden because it threw a warning for an unused variable.

        _forceOverriddenMethods.add("<java.security.MessageDigest: "
            + "java.security.MessageDigest getInstance(java.lang.String)>");
        // Overridden because inner classes have problems accessing private
        // fields of outer classes. Specifically outerClass cannot call
        // InnerClass.privateFieldOfOuterClass.

        _forceOverriddenMethods.add("<java.io.ObjectOutputStream: "
            + "void writeUTFBody(char[],int)>");

        _forceOverriddenMethods.add("<java.security.Signature: "
            + "java.security.Signature getInstance(java.lang.String)>");
        // Overridden because inner classes have problems accessing private
        // fields of outer classes. Specifically outerClass cannot call
        // InnerClass.privateFieldOfOuterClass.

        _forceOverriddenMethods.add("<java.lang.ClassNotFoundException: "
            + "void printStackTrace()>");
        // Overridden because it had a statement with no effect. This was
        // because CSwitch expects method class to be superclass of base.

        _forceOverriddenMethods.add("<java.text.AttributedString: "
            + "void <init>(java.text.AttributedCharacterIterator[])>");
        _forceOverriddenMethods.add("<java.text.AttributedString: "
            + "void <init>(java.lang.String,java.util.Map)>");
        _forceOverriddenMethods.add("<java.text.AttributedString: "
            + "void <init>(java.text.AttributedCharacterIterator)>");
        _forceOverriddenMethods.add("<java.text.AttributedString: "
            + "void <init>(java.text.AttributedCharacterIterator,int,"
            + "int,java.text.AttributedCharacterIterator$Attribute[])>");
        // Overridden because they assume inheritance from interfaces, and
        // we don't have interfaces up yet.

        _forceOverriddenMethods.add("<org.apache.crimson.tree.ElementNode2: "
            + "void trimToSize()>");
        _forceOverriddenMethods.add("<org.apache.crimson.tree.ElementNode2: "
            + "void setReadonly(boolean)>");
        _forceOverriddenMethods.add("<org.apache.crimson.tree.ElementNode2: "
            + "org.w3c.dom.Attr getAttributeNodeNS(java.lang.String"
            + ",java.lang.String)>");
        // Overridden because they did an incorrect cast on an inherited
        // method. Needs to be fixed.

        _forceOverriddenMethods.add("<java.security.MessageDigest: "
            + "java.security.MessageDigest getInstance(java.lang.String"
            + ",java.security.Provider)>");
        // Overridden because an inner class tried to access the private
        // method of an outer class, and this is not supported right now.


        _forceOverriddenMethods.add("<sun.nio.ch.FileChannelImpl: "
            + "java.nio.channels.FileLock lock(long,long,boolean)>");
        // Overridden because it threw a warning for an unused variable.

       _forceOverriddenMethods.add("<sun.net.www.URLConnection: "
            + "void setFileNameMap(java.net.FileNameMap)>");
       // Overridden because it calls a nonexistant method.

       _forceOverriddenMethods.add("<java.security.Signature: "
            + "java.security.Signature getInstance"
            + "(java.lang.String,java.security.Provider)>");
       // Overridden because it asks for a nonexistant field.

        ///////// Methods replaced with actual code ////////

        // Methods that provide basic I/O functionality in
        // java.io.PrintStream.
        _forceOverriddenMethods.add(
            "<java.io.PrintStream: void println(float)>");
        _forceOverriddenMethods.add(
            "<java.io.PrintStream: void println(int)>");
        _forceOverriddenMethods.add(
            "<java.io.PrintStream: void println(java.lang.String)>");


        _forceOverriddenMethods.add("<java.lang.String: void <init>()>");
        // Overridden because java.lang.Object.clone() is not implemented yet.

        _forceOverriddenMethods.add(
        "<java.lang.System: void initializeSystemClass()>");
        // Contains initializations needed to get System.out.println() and
        // other needed functionality to be supported.


    }

    /** Checks if the given method is overridden.
     * @param method The method to be checked.
     * @return True if the method is overridden.
     */
    public static boolean isOverridden(SootMethod method) {
        if (_forceOverriddenMethods.contains(method.getSignature())
                || isOverridden(method.getDeclaringClass())
                ) {
            return true;
        }
        else {
            return false;
        }
    }

    /** Checks if the given class is overridden.
     *  @param class The class to check.
     *  @return True if the entire class is overridden.
     */
    public static boolean isOverridden(SootClass sootClass) {
        String className = sootClass.getName();

        if ((className.indexOf("sun.") == 0)
                ||(className.indexOf("org.") == 0)
                ||(className.indexOf("com.") == 0)
                ||(className.indexOf("javax.") == 0)
                ||(className.indexOf("java.nio.") == 0)
                ||(className.indexOf("java.lang.reflect.") == 0)
                ||(className.indexOf("java.lang.ref.") == 0)
                ||(className.indexOf("java.util.prefs.") == 0)
                ||(className.indexOf("java.util.logging.") == 0)
                ||(className.indexOf("java.security.") == 0)
            ) {
            return true;
        }
        else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** The directory containing the location of the bodies of overridden
     * methods.
     */
    public static String overriddenBodyLib = "../runtime/over_bodies/";


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /**
     * Returns the code for the body of a given overridden method. If a
     * file exists containing the body of this overridden method, that file
     * is used, otherwise a dummy method stub is generated that just allows
     * the method to be compiled correctly. Note that auto-generated dummy
     * method stubs cannot provide correct functionality. This may lead to
     * problems(for example, segmentation faults) due to uninitialized
     * fields/function pointers. These problems can be solved by using a
     * file instead of a dummy stub for the method body. A large majority
     * of overridden methods have no problems with dummy stubs.
     *
     *  @return The code for the body of the method.
     */
    protected static String _getBodyCode(SootMethod method) {
        // We're putting 4 leading spaces for indentation.
        String indent = "    ";
        StringBuffer code = new StringBuffer();
        String cReturnType = CNames.typeNameOf(method.getReturnType());

        code.append(indent + "/* OVERRIDDEN METHOD */\n");

        if (FileHandler.exists(fileContainingCodeFor(method))) {
                code.append(indent + "#include \""
                    + overriddenBodyLib
                    + CNames.functionNameOf(method) + ".c\"");
        }
        else {
            code.append(_indent(1) + "/* DUMMY METHOD STUB */\n");

            if (!cReturnType.equals("void")) {
                code.append(_indent(1) + cReturnType + " dummy;\n");
                Type returnType = method.getReturnType();

                // Initializing the variable prevents warnings from gcc -
                // O2.
                if (returnType instanceof BaseType) {
                    // Allocate memory for objects. Set other data types to
                    // 0.
                    if (returnType instanceof RefType) {
                        code.append(_indent(1)
                                + "dummy = malloc(sizeof(struct "
                                + cReturnType + "));\n");
                    }
                    else {
                        code.append(_indent(1) + "dummy = 0;\n");
                    }
                }
                else if (returnType instanceof ArrayType) {
                    code.append(_indent(1) + "dummy = NULL;\n");
                }

                code.append(_indent(1) + "return dummy;\n");
            }
        }

        code.append("\n");

        return code.toString();
    }

    /** Returns the code for the header of the overridden method. This is
     * almost identical to
     * NativeMethodGenerator._getStubHeader(SootMethod).
     *
     *  @return The code for the header of the method.
     */
    protected static String _getHeaderCode(SootMethod method) {

        StringBuffer code = new StringBuffer();
        Type returnType = method.getReturnType();
        int numParameters = method.getParameterCount();


        code.append(CNames.typeNameOf(returnType) + " ");
        code.append(CNames.functionNameOf(method));

        code.append("( ");

        // The first parameter is an instance of the class the
        // method belongs to, if the method is non-static.
        if (!method.isStatic()) {
            code.append(CNames.instanceNameOf(method.getDeclaringClass())
                    + " instance");

            // Put a comma if there are more parameters.
            if (numParameters > 0) {
                code.append(", ");
            }
        }

        Iterator i = method.getParameterTypes().iterator();
        int parameterIndex = 0;
        Type parameterType;
        while (i.hasNext()) {
            parameterType = (Type)i.next();
            code.append(CNames.typeNameOf(parameterType));
            code.append(" p" + parameterIndex);
            // The dummy names of the parameters are p0, p1 ... etc.
            if (parameterIndex < numParameters -1) {
                code.append(", ");// Separators.
            }
            parameterIndex++;
        }
        code.append(")");

        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given
     *  indentation level.
     */
    private static String _indent(int level) {
        return Utilities.indent(level);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private static HashSet _forceOverriddenMethods;

}
