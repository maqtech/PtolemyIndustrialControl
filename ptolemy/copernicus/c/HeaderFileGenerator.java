/*
A C code generator for generating "header files" (.h files) that implement
Java classes.

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

import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/** A C code generator for generating "header files" (.h files) that implement
 * Java classes.
 *
 * @author Shuvra S. Bhattacharyya
 * @version $Id$
 *
 */

// FIXME: Handle (ignore?) phantom methods and fields.

public class HeaderFileGenerator extends CodeGenerator {
    
    /** Construct a header file generator.
     */
    public HeaderFileGenerator() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for a C header file that implements declarations 
     *  associated with a class.
     *  Code for two struct-based type definitions is generated here.
     *  One type corresponds to the class itself (class variables, 
     *  function pointers to methods, etc.), and the other type
     *  is for instances of the class.
     *  @param source the class.
     *  @return header code for the class.
     */
    public String generate(SootClass source) {
        StringBuffer bodyCode = new StringBuffer();
        StringBuffer headerCode = new StringBuffer();
        StringBuffer footerCode = new StringBuffer();

        // An iterator over all different member declarations
        // (declarations for fields, methods, constructors, etc.) of the
        // given Java class declaration.
        Iterator membersIter;

        // Extract the unique class name and instance-specific type name to 
        // use for the class
        String className = source.getName();
        String typeName = CNames.instanceNameOf(source);

        // Avoid multiple inclusions of the generated header file
        headerCode.append("\n#ifndef _" + typeName + "_h\n");
        headerCode.append("#define _" + typeName + "_h\n\n");
        footerCode.append("\n#endif\n\n");

        // FIXME: generate header code for inner classes (probably here)

        // Generate typedef for instance-specific structure. The actual
        // definition of the structure will be placed after the definition
        // of the class-specific structure;
        bodyCode.append("struct " + typeName + ";\n");
        bodyCode.append("typedef struct " + typeName + " *" + typeName + ";\n\n");

        // Generate the type declaration header for the class
        // structure. This structure represents the class as a whole.
        // A second structure will be defined to represent each instance of the
        // class.
        bodyCode.append("/* Structure that implements Class ");
        bodyCode.append(className);
        bodyCode.append(" */\n");
        bodyCode.append("typedef struct " + CNames.classNameOf(source) + " {\n\n");

        // Pointer to superclass structure.
        bodyCode.append(_indent(1));
        if (source.hasSuperclass()) {
            bodyCode.append(_comment("Pointer to superclass structure"));
            bodyCode.append(_indent(1));
            bodyCode.append(CNames.classNameOf(source.getSuperclass()));
        } else {
            bodyCode.append(_comment("Placeholder for pointer to superclass"
                    + " structure"));
            bodyCode.append(_indent(1));
            bodyCode.append("void *");
        }
        bodyCode.append(CNames.superclassPointerName() + ";\n\n");
 
        // Generate the method table. Constructors are included since they
        // operate on class instances. 
        if (_context.getSingleClassMode()) {
            _context.setDisableImports();
        }
        String inheritedMethods = _generateMethodPointers(
                MethodListGenerator.getInheritedMethods(source),
                "Inherited/overridden methods");
        _context.clearDisableImports();
        String introducedMethods = 
                _generateMethodPointers(
                MethodListGenerator.getNewMethods(source),
                "New public and protected methods")
                + _generateMethodPointers(
                MethodListGenerator.getConstructors(source),
                "Constructors")
                + _generateMethodPointers(
                MethodListGenerator.getPrivateMethods(source),
                "Private methods");
        if (((_context.getSingleClassMode()) || inheritedMethods.equals("")) &&
                introducedMethods.equals("")) {
            bodyCode.append(_comment("Empty method table"));
        } else {
            bodyCode.append(_indent(1) + "struct {\n");
            bodyCode.append(inheritedMethods + introducedMethods);
            bodyCode.append("\n" + _indent(1) + "} methods;\n");
        }

        // Generate class variables.
        // FIXME: only including static fields for now.
        String staticFields = _generateStaticFields(source);
        if (!staticFields.equals("")) {
            bodyCode.append("\n" + _indent(1) + "struct {\n\n");
            bodyCode.append(staticFields);
            bodyCode.append("\n" + _indent(1) + "} classvars;\n");
        }

        // Terminator for declared type for the class as a whole.
        bodyCode.append("\n} *" + CNames.classNameOf(source) + ";\n\n");    

        // Generate the type declaration header for the class instance
        // structure.
        bodyCode.append(_comment("Structure that implements instances of Class "
                + className));
        bodyCode.append("struct " + typeName + " {\n");

        // Pointer to common, class-specific information.
        bodyCode.append("\n" + _indent(1) + CNames.classNameOf(source) + 
                " class;\n");

        // Extract the non-static fields, and insert them into the struct
        // that is declared to implement the class. 
        Iterator superClasses = _getSuperClasses(source).iterator();
        while (superClasses.hasNext()) {
            SootClass superClass = (SootClass)superClasses.next();
            bodyCode.append(_generateInheritedFields(superClass));
        }
        bodyCode.append(_generateFields(source));


        // Terminator for declared type for class instances.
        bodyCode.append("\n};\n\n");    

        // Export function prototypes for all public and protected methods.
        Iterator methods = source.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod method = (SootMethod)(methods.next());
            if (method.isProtected() || method.isPublic()) {
                bodyCode.append("\n" + _comment(method.getSubSignature()));
                bodyCode.append("extern " + _generateMethodHeader(method) + ";\n");
            }
        }

        // Export the name of the variable that contains class-specific information
        // for the generated class.
        bodyCode.append("\n" + _comment("Class information"));
        bodyCode.append("extern struct " + CNames.classNameOf(source) + " " 
                + CNames.classStructureNameOf(source) + ";\n");

        // Export the name of the function that initializes the class
        bodyCode.append("\n" + _comment("Class initialization function"));
        bodyCode.append("void " + CNames.initializerNameOf(source) + "("
                + CNames.classNameOf(source) + ");\n");

        // Generate "#include" directives for each required type.
        // We are generating the include file for 'source', so there is
        // no need to import it.
        _removeRequiredType(source);  
        headerCode.append(_generateIncludeDirectives());
        headerCode.append("\n");

        // Return an appropriate concatenation of the code strings.
        return (headerCode.append(bodyCode.append(footerCode))).toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Generate a C declaration that corresponds to a class field.
    private String _generateField(SootField field) {
        StringBuffer fieldCode = new StringBuffer(_indent(1));
        // FIXME: generate any modifier-related code
        fieldCode.append(CNames.typeNameOf(field.getType()) + " ");
        _updateRequiredTypes(field.getType());
        fieldCode.append(CNames.fieldNameOf(field));
        fieldCode.append(";\n");
        return fieldCode.toString();
    }

    // Generate C declarations corresponding to all non-static fields of the class
    // that we are presently generating code for. The public and protected
    // fields are declared first, followed by the private fields.
    private String _generateFields(SootClass source) {
        StringBuffer fieldCode = new StringBuffer();
        Iterator fields = source.getFields().iterator();
        int insertedFields = 0;
        String header = "\n" + _indent(1) + "/* Public and protected fields defined in " 
                + source.getName() + " */\n";

        // Generate public and protected fields 
        while (fields.hasNext()) {
            SootField field = (SootField)(fields.next());
            if ((field.isPublic() || field.isProtected()) && 
                    !(Modifier.isStatic(field.getModifiers()))) {
                if (insertedFields == 0) fieldCode.append(header);
                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }

        // Generate private fields 
        fields = source.getFields().iterator();
        while (fields.hasNext()) {
            SootField field = (SootField)(fields.next());
            if (field.isPrivate() && !(Modifier.isStatic(field.getModifiers()))) {
                if (insertedFields == 0) fieldCode.append(header);
                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }

        return fieldCode.toString();
    }

    // Generate C declarations corresponding to all non-static fields inherited from
    // a given super class.
    private String _generateInheritedFields(SootClass superClass) {
        StringBuffer fieldCode = new StringBuffer();
        Iterator fields = superClass.getFields().iterator();
        int insertedFields = 0;
        String header = "\n" + _indent(1) + 
                _comment("Fields inherited from " + superClass.getName());

        while (fields.hasNext()) {
            SootField field = (SootField)(fields.next());
            if ((field.isPublic() || field.isProtected()) && 
                    !(Modifier.isStatic(field.getModifiers()))) {
                if (insertedFields == 0) fieldCode.append(header);
                fieldCode.append(_generateField(field));
                insertedFields++;
            }
        }
        return fieldCode.toString();
    }

    /** Given a list of Java methods, generate code that declares function 
     *  pointers corresponding to the non-static methods in the list.
     *  The format of a method pointer declaration is as follows:
     *  Do not generate pointers for static methods.
     *
     *  functionReturnType (*functionName)(paramOneType, paramTwoType, ...);
     *
     *  is inserted at the beginning, before the methods are declared. The comment
     *  is omitted if no code is produced by this method (i.e., there are no
     *  non-static methods to generate pointers for).
     *  @param methodList The list of methods.
     *  @param comment A comment to insert in the generated code. This comment
     *  @return Function pointer code for specified Java methods.
     */
    private String _generateMethodPointers(List methodList, String comment) {
        StringBuffer methodCode = new StringBuffer();
        final String indent = _indent(2);
        Iterator methods = methodList.iterator();
        int insertedMethods = 0;
        while (methods.hasNext()) {
            SootMethod method = (SootMethod)(methods.next());
            if (!method.isStatic()) {
                if (insertedMethods == 0) {
                    methodCode.append("\n" + indent + _comment(comment));
                    // If importing of referenced include files in disabled,
                    // then place the method table in comments.
                    if (_context.getDisableImports()) {
                        methodCode.append(_openComment);
                    }
                }
                methodCode.append(indent);
                methodCode.append(CNames.typeNameOf(method.getReturnType()));
                methodCode.append(" (*");
                methodCode.append(CNames.methodNameOf(method));
                methodCode.append(")(");
                methodCode.append(_generateParameterTypeList(method));
                methodCode.append(");\n");
                _updateRequiredTypes(method.getReturnType());
                insertedMethods++;
            }
        }
        if ((insertedMethods > 0) && _context.getDisableImports()) 
            methodCode.append(_closeComment);
        return methodCode.toString();
    }

    // Generate static field declarations.
    private String _generateStaticFields(SootClass source) {
        StringBuffer fieldCode = new StringBuffer();
        String header = _indent(2) + _comment("Class variables");
        Iterator fields = source.getFields().iterator();
        int insertedFields = 0;
        while (fields.hasNext()) {
            SootField field = (SootField)(fields.next());
            if (Modifier.isStatic(field.getModifiers())) {
                if (insertedFields == 0) fieldCode.append(header);
                fieldCode.append(_indent(1) + _generateField(field));
                insertedFields++;
            }
        }
        return fieldCode.toString();
    }
    
    // Return the superclasses of a class as a linked list.
    // The list entries are ordered in decreasing (parents before children)
    // hierarchy order. 
    private LinkedList _getSuperClasses(SootClass source) {
        LinkedList classes = (LinkedList)(_superClasses.get(source));
        if (classes  == null) {
            if (source.hasSuperclass()) {
                classes = (LinkedList)
                        (_getSuperClasses(source.getSuperclass()).clone());
                classes.add(source.getSuperclass());
                _superClasses.put(source, classes);
            } else {
                _superClasses.put(source, classes = new LinkedList());
            }
        }
        return classes; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The end of a comment for generated code that is to be 
    // commented-out.
    private static final String _closeComment = 
            "***********************************/\n";

    // The beginning of a comment for generated code that is to be 
    // commented-out.
    private static final String _openComment = 
            "/***********************************\n";

    // Mapping from classes into lists of superclasses as computed by
    // {@link #_getSuperClasses(SootClass)}. 
    private static HashMap _superClasses = new HashMap();
}
