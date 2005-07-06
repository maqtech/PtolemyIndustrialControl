/*

A C code generator for generating "code files" (.c files) that implement
Java classes.

Copyright (c) 2001-2005 The University of Maryland.
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

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES
, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
package ptolemy.copernicus.c;

import java.util.Iterator;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;


/** A C code generator for generating "code files" (.c files) that implement
    Java classes.

    @author Shuvra S. Bhattacharyya, Ankush Varma
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (ssb)
    @Pt.AcceptedRating Red (ssb)
*/
public class CodeFileGenerator extends CodeGenerator {
    /** Construct a code file generator.
     */
    public CodeFileGenerator() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate C code for a class, including code (function declarations)
     *  for all of its methods, and for its initialization function.
     *  @param source The class.
     *  @return The code.
     */
    public String generate(SootClass source) {
        StringBuffer bodyCode = new StringBuffer();
        StringBuffer headerCode = new StringBuffer();
        _context.clear();
        headerCode.append("/* Automatically generated by the Ptolemy "
                + "C Code Generator. */\n\n");

        source.setApplicationClass();
        _updateRequiredTypes(source.getType());

        // Add runtime include files.
        _context.addIncludeFile("<setjmp.h>");
        _context.addIncludeFile("<stdlib.h>");
        _context.addIncludeFile("<stdio.h>");
        _context.addIncludeFile("<math.h>");

        // The location of time.h is different for the C6000
        if (Options.v().get("target").equals("C6000")) {
            _context.addIncludeFile("<time.h>");
        } else {
            // This used to include sys/time.h, but
            // under Linux, CLOCKS_PER_SEC is defined in
            // /usr/include/bits/time.h, which is included by
            // /usr/include/time.h
            _context.addIncludeFile("<time.h>");
        }

        // This file cannot be auto-detected because its called from a
        // runtime method.
        if (source.getName().equals("java.lang.System")) {
            _context.addIncludeFile("\"java/io/PrintStream.h\"");
        }

        if (!Context.getSingleClassMode()) {
            _context.addIncludeFile("\"strings.h\"");
            _context.addIncludeFile("\"pccg_runtime.h\"");
        } else {
            _context.addIncludeFile("\"pccg_runtime_single.h\"");
        }

        // Include file for garbage collection.
        if (!Options.v().get("gcDir").equals("")) {
            _context.addIncludeFile("\"include/gc.h\"");
        }

        // Generate function prototypes for all private methods.
        int count = 0;
        Iterator methods = source.getMethods().iterator();

        while (methods.hasNext()) {
            SootMethod method = (SootMethod) (methods.next());

            if (method.isPrivate() && RequiredFileGenerator.isRequired(method)) {
                if (count++ == 0) {
                    bodyCode.append(_comment("Prototypes for functions that "
                                            + "implement private methods"));
                }

                bodyCode.append(_generateMethodHeader(method) + ";\n");
            }
        }

        bodyCode.append("\n");

        // Generate the code for all of the methods.
        MethodCodeGenerator methodCodeGenerator = new MethodCodeGenerator(_context,
                _requiredTypeMap);

        methods = source.getMethods().iterator();

        while (methods.hasNext()) {
            SootMethod thisMethod = (SootMethod) methods.next();
            String methodCode = new String();

            if (RequiredFileGenerator.isRequired(thisMethod)) {
                methodCode = methodCodeGenerator.generate(thisMethod);
                bodyCode.append(methodCode);
            }

            if (methodCode.length() != 0) {
                bodyCode.append("\n");
            }
        }

        // Generate the code for the method that looks up interfaces.
        InterfaceLookupGenerator interfaceHandler = new InterfaceLookupGenerator();
        bodyCode.append(interfaceHandler.generate(source));

        // Function for handing "instanceof".
        bodyCode.append(new InstanceOfFunctionGenerator().generate(source));

        // Declare the run-time structure that is to contain class information.
        bodyCode.append(_comment("Structure that contains class information"));
        bodyCode.append("struct " + CNames.classNameOf(source) + " "
                + CNames.classStructureNameOf(source) + ";\n\n");

        // Generate code for the function that initializes the class.
        bodyCode.append(_generateClassInitialization(source));

        headerCode.append(_generateIncludeDirectives() + "\n");

        // Define custom memory allocation routine.
        headerCode.append("#define malloc(x) PCCG_malloc(x)\n");

        headerCode.append(_declareConstants() + "\n");

        // Generate the typedefs for the array instances.
        headerCode.append(_generateArrayInstanceDeclarations());

        return (headerCode.append(bodyCode)).toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate declarations for constants in the generated code.
     *  @return The generated declarations for constants.
     */
    protected StringBuffer _declareConstants() {
        StringBuffer code = new StringBuffer();
        String typeName = CNames.instanceNameOf(Scene.v().getSootClass("java.lang.String"));
        Iterator stringConstants = _context.getStringConstants();

        if (stringConstants.hasNext()) {
            code.append(_comment("Pointers to string constants"));

            while (stringConstants.hasNext()) {
                code.append("static " + typeName + " "
                        + _context.getIdentifier((String) (stringConstants.next()))
                        + ";\n");
            }
        }

        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate initialization code for structures that implement classes.
     *  Code for a C function is returned. This function implements
     *  initialization required for the class-specific structure
     *  that is pointed to by its argument.
     *  @param source The class.
     *  @return Initialization code for the class.
     */
    private String _generateClassInitialization(SootClass source) {
        StringBuffer code = new StringBuffer();
        final String argumentName = "class";
        final String argumentReference = argumentName + "->";
        code.append(_comment("Function that initializes structure for Class "
                            + source.getName()));
        code.append("void " + CNames.initializerNameOf(source) + "("
                + CNames.classNameOf(source) + " " + argumentName + ") {\n");

        // Inherited Methods.
        if (!Context.getSingleClassMode()) {
            code.append(_indent(1) + "/* Inherited Methods */\n");
            code.append(_generateMethodPointerInitialization(
                                MethodListGenerator.getInheritedMethods(source),
                                argumentReference));
        }

        // New methods.
        code.append(_indent(1) + "/* New Methods */\n");
        code.append(_generateMethodPointerInitialization(
                            MethodListGenerator.getNewMethods(source), argumentReference));

        // Constructors.
        code.append(_indent(1) + "/* Constructors */\n");
        code.append(_generateMethodPointerInitialization(
                            MethodListGenerator.getConstructors(source), argumentReference));

        // Private methods.
        code.append(_indent(1) + "/* Private Methods */\n");
        code.append(_generateMethodPointerInitialization(
                            MethodListGenerator.getPrivateMethods(source), argumentReference));

        Iterator stringConstants = _context.getStringConstants();

        if (stringConstants.hasNext()) {
            code.append(_indent(1) + "/* String Constant Initialization */\n");

            SootClass stringClass = Scene.v().getSootClass("java.lang.String");
            String stringType = CNames.instanceNameOf(stringClass);
            String stringStructure = CNames.classStructureNameOf(stringClass);
            String stringInitializer = CNames.methodNameOf(stringClass
                    .getMethod("void <init>(char[])"));
            code.append("\n" + _indent(1)
                    + _comment("Initialization of string constants"));

            while (stringConstants.hasNext()) {
                StringBuffer value = new StringBuffer(stringConstants.next()
                        .toString());

                // Replace all incidences of " with \" to prevent bad characters
                // between quotes.
                for (int i = 0; i < value.length(); i++) {
                    if (value.charAt(i) == '"') {
                        value.insert(i, "\\");
                        i++;
                    }
                }

                String identifier = _context.getIdentifier(value.toString());
                code.append(_indent(1) + identifier + " = (" + stringType
                        + ")(malloc(sizeof (struct " + stringType + ")));\n");

                code.append(_indent(1) + stringStructure + ".methods."
                        + stringInitializer + "(" + identifier + ", \"" + value
                        + "\");\n");
            }
        }

        // Initialize the name of the class.
        code.append("\n");
        code.append(_indent(1) + _comment("The name of the class."));
        code.append(_indent(1) + "class->name = \"" + source.getName()
                + "\";\n\n");

        // Initialize the field representing memory needed by instances of
        // this class.
        code.append(_indent(1)
                + _comment("The memory needed by instances of this class"));
        code.append(_indent(1) + "class->instance_size = sizeof" + "(struct "
                + CNames.instanceNameOf(source) + ")" + ";\n\n");

        // Set up the superclass pointer.
        code.append("\n" + _indent(1) + "/* Superclass pointer */\n");
        code.append(_indent(1) + argumentReference
                + CNames.superclassPointerName() + " = ");

        if (!Context.getSingleClassMode() && source.hasSuperclass()) {
            // If the superclass is not required, comment it out and
            // replace it with a null.
            if (!RequiredFileGenerator.isRequired(source.getSuperclass())) {
                code.append("/* " + "&"
                        + CNames.classStructureNameOf(source.getSuperclass())
                        + " */ NULL");
            } else {
                code.append("&"
                        + CNames.classStructureNameOf(source.getSuperclass()));
                _updateRequiredTypes(source.getSuperclass().getType());
            }
        } else {
            code.append("NULL");
            _context.addIncludeFile("<stdio.h>");
        }

        code.append(";\n");

        code.append("\n");

        // Interface lookup function
        if (InterfaceLookupGenerator.needsLookupFunction(source)) {
            code.append(_indent(1) + _comment("Interface lookup function."));
            code.append(_indent(1) + "class->lookup = &"
                    + CNames.interfaceLookupNameOf(source) + ";\n");
        }

        // Handler for "instanceof".
        code.append(_indent(1) + _comment("Handler for \"instanceof\"."));
        code.append(_indent(1) + "class->instanceOf = &instanceOf;\n");

        code.append("}\n");
        return code.toString();
    }

    /** Generate code to initialize method pointers (in the method table)
        in a structure that implements a class.

        @param methodList The list of methods for which pointers are to be
        initialized.

        @param argumentReference A C reference pointing to the structure
        which has the "methods" substructure containing the method
        pointers. Typically this is a class structure in the C code.
    */
    private String _generateMethodPointerInitialization(List methodList,
            String argumentReference) {
        StringBuffer code = new StringBuffer();
        Iterator methods = methodList.iterator();

        while (methods.hasNext()) {
            SootMethod method = (SootMethod) (methods.next());

            // Method Pointer Initialization is not done for methods that
            // are not required, static methods, and abstract methods.
            if ((!method.isStatic())
                    && RequiredFileGenerator.isRequired(method)
                    && (!method.isAbstract())) {
                code.append(_indent(1) + argumentReference + "methods."
                        + CNames.methodNameOf(method) + " = "
                        + CNames.functionNameOf(method) + ";\n");
                _updateRequiredTypes(method.getDeclaringClass().getType());
            }
        }

        return code.toString();
    }
}
