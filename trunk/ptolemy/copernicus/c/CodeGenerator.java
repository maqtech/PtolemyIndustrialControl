/*

A base class for C code generators in Ptolemy II.

Copyright (c) 2001-2003 The University of Maryland.
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

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.ArrayType;
import soot.RefType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/** A base class for C code generators in Ptolemy II.

   @author Shuvra S. Bhattacharyya
   @version $Id$
   @since Ptolemy II 2.0

*/

public abstract class CodeGenerator {

    /** Construct a new code generator */
    public CodeGenerator() {
        _context = new Context();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  public methods                           ////

    /** Given a class, return the code generated by this code generator for
     *  the class.
     *  @param source The class.
     *  @return The generated code.
     */
    public abstract String generate(SootClass source);

    /** Turn on (enable) single class mode translation
     *  (see {@link Context#getSingleClassMode()}).
     */
    public void setSingleClassMode() {
        _context.setSingleClassMode();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  protected methods                        ////


    /** Enclose a given string of text within appropriate delimiters to
     *  form a comment in the generated code.
     *  Also, append a new line after the comment.
     *  @param text The text to place in the generated comment.
     *  @return The generated comment.
     */
    protected final String _comment(String text) {
        return Utilities.comment(text);
    }

    /** Generate code for typedef declaring array instances
     * @param void
     * @return A newline character (\n) separated string of typdefs for
     * the arrays needed.
     */
    protected String _generateArrayInstanceDeclarations() {
        Iterator i = _context.getArrayInstances().iterator();
        String code = new String();

        while (i.hasNext()) {
            String name = i.next().toString();

            code = code + "#ifndef A_DEF_"+ name + "\n"
                +"#define A_DEF_" + name + "\n"
                +"typedef PCCG_ARRAY_INSTANCE_PTR " + name +";\n"
                +"#endif\n";
        }

        return code;

    }

    /** Generate include directives for all types that are required for the
     *  class that we are generating code for.
     *  @param source The class that we are generating code for.
     *  @return The generated include directives.
     */
    protected String _generateIncludeDirectives() {
        StringBuffer headerCode = new StringBuffer();

        Iterator includeFiles = _context.getIncludeFiles();
        if (includeFiles.hasNext()) {
            headerCode.append(_comment("System, runtime and "
                +"CSwitch-generated include files"));
        }
        while (includeFiles.hasNext()) {
            headerCode.append("#include ");
            headerCode.append((String)(includeFiles.next()));
            headerCode.append("\n");
        }

        Iterator requiredTypes = _getRequiredIncludeFiles();
        if (requiredTypes.hasNext()) {
            headerCode.append("\n" + _comment("Converted classes"));
        }
        while (requiredTypes.hasNext()) {
            headerCode.append("#include \"");
            headerCode.append((String)(requiredTypes.next()));
            headerCode.append("\"\n");
        }

        return headerCode.toString();
    }

    /** Generate header code for a method. Parameter names are not included
     *  in the generated code.
     *  @param method The method.
     *  @return The header code.
     */
    protected String _generateMethodHeader(SootMethod method) {
        StringBuffer header = new StringBuffer();
        Type returnType = method.getReturnType();
        header.append(CNames.typeNameOf(returnType));
        _updateRequiredTypes(returnType);
        header.append(" ");
        header.append(CNames.functionNameOf(method));
        header.append("(");
        header.append(_generateParameterTypeList(method));
        header.append(")");

        //If return type is an array, record this
        //(If parameter type is an array, it is recorded by
        // _generateParameterTypeList

        if (returnType instanceof ArrayType) {
            _context.addArrayInstance(CNames.typeNameOf(returnType));
        }

        return header.toString();
    }


    /** Generate code for the parameter type list of a method,
     *  excluding parentheses.
     *  @param method The method.
     *  @return Code for the parameter type list.
     */
    protected String _generateParameterTypeList(SootMethod method) {
        StringBuffer code = new StringBuffer();
        Iterator parameters = method.getParameterTypes().iterator();
        int numberOfParameters = 0;
        if (!method.isStatic()) {
            SootClass source = method.getDeclaringClass();
            code.append(CNames.instanceNameOf(method.getDeclaringClass()));
            numberOfParameters++;
        }
        while (parameters.hasNext()) {
            if ((++numberOfParameters) > 1) code.append(", ");
            Type parameterType = (Type)(parameters.next());
            code.append(CNames.typeNameOf(parameterType));

            _updateRequiredTypes(parameterType);

            if (parameterType instanceof ArrayType) {
                _context.addArrayInstance(CNames.typeNameOf(parameterType));
            }
        }
        return code.toString();
    }

    /** Return an iterator over the include files required by
     *  the generated code. Each element
     *  in the iterator is a String that gives the name of an include file.
     *  @return The names of the required include files.
     */
    protected Iterator _getRequiredIncludeFiles() {
        return _requiredTypeMap.values().iterator();
    }

    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given
     *  indentation level.
     */
    protected String _indent(int level) {
        return Utilities.indent(level);
     }

    /** Remove a class from the list of required types (types whose associated
     *  include files must be imported) if the class exists in the list.
     *  @param source The class.
     */
    protected void _removeRequiredType(SootClass source) {
        if (_requiredTypeMap.containsKey(source)) {
            _requiredTypeMap.remove(source);
        }
    }

    /** Register a type as a type that must be imported into the generated code
     *  through an #include directive. The request is processed only if the
     *  argument
     *  is a RefType, or if it is an ArrayType with a RefType as the base type.
     *  All other requests are ignored. Duplicate requests are also ignored.
     *  @param type The type.
     */
    protected void _updateRequiredTypes(Type type) {
        if (!_context.getDisableImports()) {
            SootClass source = null;
            if (type instanceof RefType) {
                source = ((RefType)type).getSootClass();
            } else if ((type instanceof ArrayType) &&
                    (((ArrayType)type).baseType instanceof RefType)) {
                source = ((RefType)(((ArrayType)type).baseType)).getSootClass();
            }
            if (source != null) {

                if (!_requiredTypeMap.containsKey(source)) {
                    _requiredTypeMap.put(source,
                            CNames.includeFileNameOf(source));
                }
            }
        }
    }

    /** Issue a warning message to standard error.
     *  @param message The warning message.
     */
    protected void _warn(String message) {
        System.err.println("C code generation warning: " + message + "\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  protected variables                      ////

    // Code generation context information.
    protected Context _context;

    // Mapping from classes that the current class depends on to their
    // include file names.
    protected HashMap _requiredTypeMap = new HashMap();



    ///////////////////////////////////////////////////////////////////
    ////                  private variables                        ////

}
