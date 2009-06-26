/* Base class for Java code generator adapter.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program.procedural.java;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapterStrategy;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// JavaCodeGeneratorAdapter

/**
 Base class for C code generator adapter.

 <p>Actor adapters extend this class and optionally define the
 generateFireCode(),
 generateInitializeCode(), generatePrefireCode(),
 generatePostfireCode(), generatePreinitializeCode(), and
 generateWrapupCode() methods.

 <p> In derived classes, these methods,
 if present, make actor specific changes to the corresponding code.
 If these methods are not present, then the parent class will automatically
 read the corresponding .c file and subsitute in the corresponding code
 block.  For example, generateInitializeCode() reads the
 <code>initBlock</code>, processes the macros and adds the resulting
 code block to the output.

 <p>For a complete list of methods to define, see
 {@link ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapterStrategy}.

 <p>For further details, see <code>$PTII/ptolemy/cg/README.html</code>

 @author Christopher Brooks, Edward Lee, Man-Kit Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 7.1
 o @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JavaCodeGeneratorAdapterStrategy extends
        ProgramCodeGeneratorAdapterStrategy {
    /**
     * Create a new instance of the C code generator adapter.
     */
    public JavaCodeGeneratorAdapterStrategy() {
        _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this adapter. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
     */
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        addFunctionUsed("new");
        return super.getNewInvocation(constructorString);
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        // FIXME: We need to create new ParseTreeCodeGenerator each time
        // here or else we get lots of test failures.  It would be better
        // if we could use the same JavaParseTreeCodeGenerator over and over.
        _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
        return _parseTreeCodeGenerator;
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     */
    public JavaCodeGenerator getCodeGenerator() {
        return (JavaCodeGenerator) _codeGenerator;
    }

    /** Return the translated token instance function invocation string.
     *  @param functionString The string within the $tokenFunc() macro.
     *  @param isStatic True if the method is static.
     *  @return The translated type function invocation string.
     *  @exception IllegalActionException The given function string is
     *   not well-formed.
     */
    public String getFunctionInvocation(String functionString, boolean isStatic)
            throws IllegalActionException {
        // Record the referenced type function in the infoTable.
        super.getFunctionInvocation(functionString, isStatic);

        // FIXME: lots of duplicated code from superclass here.
        functionString = processCode(functionString);

        // i.e. "$tokenFunc(token::add(arg1, arg2, ...))"
        // this transforms to ==>
        // "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
        // FIXME: we need to do some more smart parsing to find the following
        // indexes.
        int commaIndex = functionString.indexOf("::");
        int openFuncParenIndex = functionString.indexOf('(', commaIndex);
        int closeFuncParenIndex = functionString.lastIndexOf(')');

        // Syntax checking.
        if ((commaIndex == -1) || (openFuncParenIndex == -1)
                || (closeFuncParenIndex != (functionString.length() - 1))) {
            throw new IllegalActionException(
                    "Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "String was:\n:" + functionString);
        }

        String typeOrToken = functionString.substring(0, commaIndex).trim();
        String functionName = functionString.substring(commaIndex + 2,
                openFuncParenIndex).trim();

        String argumentList = functionString.substring(openFuncParenIndex + 1)
                .trim();

        if (isStatic) {

            if (argumentList.length() == 0) {
                throw new IllegalActionException(
                        "Static type function requires at least one argument(s).");
            }

            //return "functionTable[(int)" + typeOrToken + "][FUNC_"
            //+ functionName + "](" + argumentList;

            String methodType = typeOrToken
                    .substring(typeOrToken.indexOf('_') + 1);
            return methodType + "_" + functionName + "(" + argumentList;

        } else {

            // if it is more than just a closing paren
            if (argumentList.length() > 1) {
                argumentList = ", " + argumentList;
            }

            //return "functionTable[(int)" + typeOrToken + ".type][FUNC_"
            //+ functionName + "](" + typeOrToken + argumentList;
            //String methodType = typeOrToken.substring(typeOrToken.indexOf('_') + 1);
            getCodeGenerator().markFunctionCalled(
                    functionName + "_Token_Token", this);
            return functionName + "_Token_Token(" + typeOrToken + argumentList;
        }
    }

    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        Set<String> files = super.getHeaderFiles();
        files.addAll(_includeFiles);
        return files;
    }

    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        String result = super._replaceMacro(macro, parameter);

        if (result != null) {
            if (macro.equals("cgType")) {
                return result.replace("Int", "Integer").replace("Integereger",
                        "Integer");
            }
            return result;
        }

        if (macro.equals("include")) {
            _includeFiles.add(parameter);
            return "";
        } else if (macro.equals("refinePrimitiveType")) {
            TypedIOPort port = getPort(parameter);

            if (port == null) {
                throw new IllegalActionException(
                        parameter
                                + " is not a port. $refinePrimitiveType macro takes in a port.");
            }
            if (isPrimitive(port.getType())) {
                return ".payload/*jcgh*/." + codeGenType(port.getType());
            } else {
                return "";
            }
        } else if (macro.equals("lcCgType")) {
            String cgType = _replaceMacro("cgType", parameter);
            if (cgType.equals("Integer")) {
                return "int";
            }
            return cgType.toLowerCase();
        }

        // We will assume that it is a call to a polymorphic
        // functions.
        //String[] call = macro.split("_");
        getCodeGenerator().markFunctionCalled(macro, this);
        result = macro + "(" + parameter + ")";

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The set of header files that needed to be included. */
    private Set<String> _includeFiles = new HashSet<String>();
}
