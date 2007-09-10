/* Code generator for the C language.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorUtilities;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CodeGenerator

/** Base class for C code generator.
 *  
 *  @author Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating red (zgang)
 *  @Pt.AcceptedRating red (zgang)
 */

public class CCodeGenerator extends CodeGenerator {

    /** Create a new instance of the C code generator.
     *  @param container The container.
     *  @param name The name of the C code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackage.setExpression("ptolemy.codegen.c");
    }


    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the function table.  In this base class return
     *  the empty string.
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public Object generateFunctionTable(Object[] types, Object[] functions) {
        StringBuffer code = new StringBuffer();

        if (functions.length > 0 && types.length > 0) {

            code.append("#define NUM_TYPE " + types.length + _eol);
            code.append("#define NUM_FUNC " + functions.length + _eol);
            code.append("Token (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token, ...)= {" + _eol);

            for (int i = 0; i < types.length; i++) {
                code.append("\t{");
                for (int j = 0; j < functions.length; j++) {
                    if (functions[j].equals("isCloseTo")
                            && (types[i].equals("Boolean") 
                                    || types[i].equals("String"))) {
                        // Boolean_isCloseTo and String_isCloseTo
                        // are the same as their corresponding *_equals
                        code.append(types[i] + "_equals");
                    } else {
                        // Check to see if the type/function combo is supported.
                        String typeFunctionName = types[i]
                            + "_" + functions[j];
                        if (_unsupportedTypeFunctions.contains(typeFunctionName)) {
                            code.append("unsupportedTypeFunction");
                        } else {
                            if (_scalarDeleteTypes.contains(types[i])
                                    && functions[j].equals("delete")) {
                                code.append("scalarDelete"); 
                            } else {
                                code.append(typeFunctionName);
                            }
                        }
                    }
                    if (j != (functions.length - 1)) {
                        code.append(", ");
                    }
                }
                if (i != (types.length - 1)) {
                    code.append("},");
                } else {
                    code.append("}");
                }
                code.append(_eol);
            }

            code.append("};" + _eol);
        }
        return code.toString();
    }

    /** Generate include files.
     *  @return The include files.
     *  @exception IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        includingFiles.add("<stdlib.h>"); // Sun requires stdlib.h for malloc
        
        if (isTopLevel() && ((BooleanToken) measureTime.getToken()).booleanValue()) {
            includingFiles.add("<sys/time.h>");
        }

        if (!isTopLevel()) {
            includingFiles.add("\"" + _sanitizedModelName + ".h\"");
            
            // FIXME: This only works under windows.
            String javaHome = StringUtilities.getProperty("java.home");
            javaHome = javaHome.replace('\\', '/');
            int index = javaHome.lastIndexOf("jre");
            javaHome = javaHome.substring(0, index);
            addInclude("-I\"" + javaHome + "include\"");

            String osName = StringUtilities.getProperty("os.name");
            if (osName != null) {
                if (osName.startsWith("Windows")) {
                    addInclude("-I\"" + javaHome + "include/win32\"");
                } else if (osName.startsWith("SunOS")) {
                    addInclude("-I\"" + javaHome + "include/solaris\"");
                } else if (osName.startsWith("Linux")) {
                    addInclude("-I\"" + javaHome + "include/linux\"");
                } else if (osName.startsWith("Darwin")) {
                    addInclude("-I\"" + javaHome + "include/darwin\"");
                }
            }
        }

        includingFiles.add("<stdarg.h>");
        includingFiles.add("<stdio.h>");
        includingFiles.add("<string.h>");
        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
            
            // Not all embedded platforms have all .h files.
            // For example, the AVR does not have time.h
            code.append("#ifndef PT_NO_" + file.substring(1, file.length() - 3).replace('/', '_').toUpperCase() + "_H" + _eol
                    + "#include " + file + _eol
                    + "#endif" + _eol);
        }

        return code.toString();
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeEntryCode() throws IllegalActionException {

        // If the container is in the top level, we are generating code 
        // for the whole model.
        if (isTopLevel()) {
            // We use (void) so as to avoid the avr-gcc 3.4.6 warning: 
            // "function declaration isn't a prototype
            return _eol + _eol + "void initialize(void) {" + _eol;

            // If the container is not in the top level, we are generating code 
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_initialize("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
    }

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeExitCode() throws IllegalActionException {

        return "}" + _eol;
    }

    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeProcedureName()
            throws IllegalActionException {
        return _INDENT1 + "initialize();" + _eol;
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *   In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code 
        // for the whole model.
        if (isTopLevel()) {
            mainEntryCode.append(_eol + _eol + "int main(int argc, char *argv[]) {"
                    + _eol);

            // If the container is not in the top level, we are generating code 
            // for the Java and C co-simulation.
        } else {
            mainEntryCode.append(_eol + _eol + "JNIEXPORT jobjectArray JNICALL"
                    + _eol + "Java_" + _sanitizedModelName + "_fire ("
                    + _eol + "JNIEnv *env, jobject obj");

            Iterator inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                mainEntryCode.append(", jobjectArray " + inputPort.getName());
            }

            mainEntryCode.append("){" + _eol);
        }
        return mainEntryCode.toString();
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        if (isTopLevel()) {
            return _INDENT1 + "exit(0);" + _eol + "}" + _eol;
        } else {
            return _INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                    + _eol;
        }
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireEntryCode() throws IllegalActionException {

        // If the container is in the top level, we are generating code 
        // for the whole model.
        if (isTopLevel()) {
            return _eol + _eol + "boolean postfire(void) {" + _eol;

            // If the container is not in the top level, we are generating code 
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_postfire("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
    }

    /** Generate the postfire procedure exit point.
     *  @return a string for the postfire procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireExitCode() throws IllegalActionException {
        return _INDENT1 + "return true;" + _eol
            + "}" + _eol;
    }

    /** Generate the postfire procedure name.
     *  @return a string for the postfire procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireProcedureName() throws IllegalActionException {

        return _INDENT1 + "postfire(void);" + _eol;
    }

    /**
     * Generate type conversion code.
     * Determine the proper code put into the source to support dynamic type
     * resolution. First, find out the different types used in the model.
     * Second, find out the different polymorphic functions used. (note: types
     * and functions are independent of each other). Third, append code blocks
     * according to the functions used, and read from files according to the
     * types referenced. Fourth, generate type resolution code, which consists
     * of constants (MAX_NUM_TYPE, MAX_NUM_FUNC), the type map, the function
     * map, function definitions read from the files, and function table.
     * @return The type resolution code.
     * @exception IllegalActionException If an error occurrs when generating
     *  the type resolution code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the type resolution code.
     */
    public String generateTypeConvertCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        code.append(_eol
                + comment("Generate type resolution code for "
                        + getContainer().getFullName()));

        // Include the constantsBlock at the top so that sharedBlocks from
        // actors can use true and false etc.  StringMatches needs this.
        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/codegen/c/kernel/SharedCode.c", this);
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();
        functions.add("delete");
        //functions.add("toString");    // for debugging.
        functions.add("convert");
        functions.add("isCloseTo");
        functions.addAll(_typeFuncUsed);
        functions.addAll(_tokenFuncUsed);

        // Determine the total number of referenced types.
        HashSet types = new HashSet();
        if (functions.contains("equals") || functions.contains("isCloseTo")) {
            types.add("Boolean");
        }

        if (functions.contains("toString")) {
            types.add("String");
        }

        if (functions.contains("isCloseTo")
                && _newTypesUsed.contains("Int")
                && !_newTypesUsed.contains("Double")) {
            // FIXME: we should not need Double for Int_isCloseTo()
            types.add("Double");
        }


        types.addAll(_newTypesUsed);

        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        StringBuffer typeMembers = new StringBuffer();
        code.append("#define TYPE_Token -1 " + _eol);
        for (int i = 0; i < typesArray.length; i++) {
            // Open the .c file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/c/kernel/type/" + typesArray[i]
                            + ".c", this);

            code.append("#define TYPE_" + typesArray[i] + " " + i + _eol);

            // Dynamically generate all the types within the union.
            if (i > 0) {
                typeMembers.append(_INDENT2);
            }
            typeMembers.append(typesArray[i] + "Token " + typesArray[i] + ";");
            if (i < typesArray.length - 1) {
                typeMembers.append(_eol);
            }
        }

        Object[] functionsArray = functions.toArray();

        
        // True if we have a delete function that needs to return a Token
        boolean defineEmptyToken = false;

        // Generate function map.
        for (int i = 0; i < functionsArray.length; i++) {

            code.append("#define FUNC_" + functionsArray[i] + " " + i + _eol);
            if (functionsArray[i].equals("delete")) {
                defineEmptyToken = true;
            }
        }

        code.append("typedef struct token Token;" + _eol);

        // Generate type and function definitions.
        for (int i = 0; i < typesArray.length; i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            code.append(typeStreams[i].toString());
        }

        ArrayList args = new ArrayList();
        // Token declareBlock.
        if (typeMembers.length() != 0) {
            args.add(typeMembers.toString());
            sharedStream.clear();
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);

            if (defineEmptyToken) {
                code.append("Token emptyToken; "
                        + comment("Used by *_delete() and others.")
                        + _eol);
            }
        }

        // Set to true if we need the unsupportedFunction() method.
        boolean defineUnsupportedTypeFunctionMethod = false;

        // Set to true if we need to scalarDelete() method.
        boolean defineScalarDeleteMethod = false;

        // Append type-polymorphic functions included in the function table. 
        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
            for (int j = 0; j < functionsArray.length; j++) {
                String typeFunctionName = typesArray[i] + "_" + functionsArray[j];
                if (_unsupportedTypeFunctions.contains(typeFunctionName)) {
                    defineUnsupportedTypeFunctionMethod = true;
                }
                if (_scalarDeleteTypes.contains(typesArray[i])
                        && functionsArray[j].equals("delete")) {
                    defineScalarDeleteMethod = true;
                }
                if (functionsArray[j].equals("isCloseTo")
                        && (typesArray[i].equals("Boolean") 
                                || typesArray[i].equals("String"))) {
                    boolean foundEquals = false;
                    for (int k = 0; k < functionsArray.length; k++) {
                        if (functionsArray[k].equals("equals")) {
                            foundEquals = true;
                        }
                    }
                    if (!foundEquals) {
                        // Boolean_isCloseTo and String_isCloseTo
                        // use Boolean_equals and String_equals.
                        args.clear();
                        args.add(typesArray[i] + "_equals");
                        sharedStream.appendCodeBlock("funcHeaderBlock", args);
                    }
                }
                if (!_scalarDeleteTypes.contains(typesArray[i])
                        || !functionsArray[j].equals("delete")) {
                    // Skip Boolean_delete etc.
                    args.clear();
                    args.add(typeFunctionName);
                    sharedStream.appendCodeBlock("funcHeaderBlock", args);
                }
            }
        }

        if (defineUnsupportedTypeFunctionMethod) {
            // Some type/function combos are not supported, so we
            // save space by defining only one method.
            sharedStream.appendCodeBlock("unsupportedTypeFunction");
        }

        if (defineScalarDeleteMethod) {
            // Types that share the scalarDelete() method, which does nothing. 
            // We use one method so as to reduce code size.
            sharedStream.appendCodeBlock("scalarDeleteFunction");
        }

        code.append(sharedStream.toString());

        // Append functions that are specified used by this type (without
        // going through the function table).
        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("funcDeclareBlock");
            code.append(typeStreams[i].toString());
        }

        // FIXME: in the future we need to load the convertPrimitivesBlock
        // dynamically, and maybe break it into multiple blocks to minimize
        // code size.
        sharedStream.clear();
        sharedStream.appendCodeBlock("convertPrimitivesBlock");
        code.append(sharedStream.toString());

        // Generate function type and token table.
        code.append(generateFunctionTable(typesArray, functionsArray));

        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("newBlock");

            for (int j = 0; j < functionsArray.length; j++) {
                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/ 
                //     .....
                // /**/
                try {
                    // Boolean_isCloseTo and String_isCloseTo map to
                    // Boolean_equals and String_equals.
                    if (functionsArray[j].equals("isCloseTo")
                        && (typesArray[i].equals("Boolean") 
                                || typesArray[i].equals("String"))) {
                        boolean foundEquals = false;
                        for (int k = 0; k < functionsArray.length; k++) {
                            if (functionsArray[k].equals("equals")) {
                                foundEquals = true;
                                break;
                            }
                        }
                        if (!foundEquals) {
                            typeStreams[i].appendCodeBlock("equalsBlock");
                        }
                    } else {
                        if (!_unsupportedTypeFunctions.contains(
                                        typesArray[i] + "_"
                                        + functionsArray[j])) {
                            typeStreams[i].appendCodeBlock(functionsArray[j] + "Block");
                        }
                    }
                } catch (IllegalActionException ex) {
                    // We have to catch the exception if some code blocks are
                    // not found. We have to define the function label in the
                    // generated code because the function table makes
                    // reference to this label.

                    typeStreams[i].append("#define " + typesArray[i] + "_"
                            + functionsArray[j] + " MISSING " + _eol);

                    // It is ok because this polymorphic function may not be
                    // supported by all types. 
                }
            }
            code.append(typeStreams[i].toString());
        }
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableDeclaration());

        // Generate variable declarations for modified variables.
        if (_modifiedVariables != null && !(_modifiedVariables.isEmpty())) {
            code.append(comment("Generate variable declarations for "
                    + "modified parameters"));
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                code.append("static "
                        + CodeGeneratorHelper.targetType(parameter.getType()) + " "
                        + generateVariableName(parameter) + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableInitialization());

        // Generate variable initialization for modified variables.
        if (_modifiedVariables != null && !(_modifiedVariables.isEmpty())) {
            code.append(comment(1, "Generate variable initialization for "
                    + "modified parameters"));
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                NamedObj container = parameter.getContainer();
                CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);
                code.append(_INDENT1
                        + generateVariableName(parameter)
                        + " = "
                        + containerHelper.getParameterValue(
                                parameter.getName(), parameter.getContainer())
                        + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate the wrapup procedure entry point.
     *  @return a string for the wrapup procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupEntryCode() throws IllegalActionException {

        // If the container is in the top level, we are generating code 
        // for the whole model.
        if (isTopLevel()) {
            return _eol + _eol + "void wrapup(void) {" + _eol;

            // If the container is not in the top level, we are generating code 
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_wrapup("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
    }

    /** Generate the wrapup procedure exit point.
     *  @return a string for the wrapup procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupExitCode() throws IllegalActionException {

        return "}" + _eol;
    }

    /** Generate the wrapup procedure name.
     *  @return a string for the wrapup procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupProcedureName() throws IllegalActionException {

        return _INDENT1 + "wrapup();" + _eol;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there are problems reading
     *  parameters or executing the commands.
     */
    protected int _executeCommands() throws IllegalActionException {

        List commands = new LinkedList();
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            commands.add("make -f " + _sanitizedModelName + ".mk "
                    + compileTarget.stringValue());
        }

        if (isTopLevel()) {
            if (((BooleanToken) run.getToken()).booleanValue()) {
                String command = codeDirectory.stringValue()
                        + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                                .stringValue().endsWith("\\")) ? "/" : "")
                        + _sanitizedModelName;

                commands.add("\"" + command.replace('\\', '/') + "\"");
            }
        }

        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
            // FIXME: need to put this output in to the UI, if any. 
            _executeCommands.start();
        } catch (Exception ex) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + _eol);
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:" + _eol + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }
    
    
    /** Make a final pass over the generated code. Subclass may extend
     * this method to do extra processing to format the output code. If
     * sourceLineBinding is set to true, it will check and insert the
     * appropriate #line macro for each line in the given code. Blank lines
     * are discarded if #line macros are inserted.  
     * @param code The given code to be processed. 
     * @return The processed code.
     * @throws IllegalActionException If #getOutputFilename() throws it.
     */
    protected StringBuffer _finalPassOverCode(StringBuffer code) 
            throws IllegalActionException {

        code = super._finalPassOverCode(code);
        
        if (((BooleanToken) sourceLineBinding.getToken()).booleanValue()) {
            
            String filename = getOutputFilename();
            //filename = new java.io.File(filename).getAbsolutePath().replace('\\', '/');

            StringTokenizer tokenizer = 
                new StringTokenizer(code.toString(), _eol);
            
            code = new StringBuffer();
            
            String lastLine = null;
            if (tokenizer.hasMoreTokens()) {
                lastLine = tokenizer.nextToken();
            }
    
            for (int i = 2; tokenizer.hasMoreTokens(); ) {
                String line = tokenizer.nextToken();
                if (lastLine.trim().length() == 0) {
                    lastLine = line;
                } else if (line.trim().length() == 0) {
                    // Get another line.
                } else {
                    if (lastLine.trim().startsWith("#line")) {
                        if (!line.trim().startsWith("#line")) {
                            code.append(lastLine + _eol);
                            i++;
                        }
                    } else {                    
                        code.append(lastLine + _eol);
                        i++;
                        
                        if (!line.trim().startsWith("#line")) {
                            code.append("#line " + i++ + " \"" + filename + "\"" + _eol);                        
                        }                    
                    }
                    lastLine = line;
                }
            }
            
            if (lastLine != null && lastLine.trim().length() != 0) {
                code.append(lastLine + _eol);            
            }
        }
        
        return code;
    }
    
    /** Generate the code for measuring the execution end time and total
     *  execution time.
     *  @return Return the code for measuring the execution end time and total
     *  execution time.
     */
    protected String _measureEndTime() {
        StringBuffer endCode = new StringBuffer();
        endCode.append(super._measureEndTime());
        endCode.append("clock_gettime(CLOCK_REALTIME, &end);\n" +
                       "dT = end.tv_sec - start.tv_sec + (end.tv_nsec - start.tv_nsec) * 1.0e-9;\n" +
                       "printf(\"execution time: %g seconds\\n\", dT);\n\n");
        
        return endCode.toString();
    }  
    
    /** Generate the code for measuring the execution start time.
     *  @return Return the code for measuring the execution start time.
     */
    protected String _measureStartTime() {
        
        StringBuffer startCode = new StringBuffer();
        startCode.append(super._measureStartTime());
        startCode.append("struct timespec start, end;\n" +
                         "double dT = 0.0;\n" +
                         "clock_gettime(CLOCK_REALTIME, &start);\n\n");
        
        return startCode.toString();
    }
    
    /** Read in a template makefile, substitute variables and write
     *  the resulting makefile.
     *
     *  <p>If a <code>.mk.in</code> file with the name of the sanitized model
     *  name, then that file is used as a template.  For example, if the
     *  model name is <code>Foo</code> and the file <code>Foo.mk.in</code>
     *  exists, then the file <code>Foo.mk.in</code> is used as a makefile
     *  template.
     *
     *  <p>If no <code>.mk.in</code> file is found, then the makefile
     *  template can be found by looking up a resource name
     *  makefile.in in the package named by the
     *  <i>generatorPackage</i> parameter.  Thus, if the
     *  <i>generatorPackage</i> has the value "ptolemy.codegen.c",
     *  then we look for the resouce "ptolemy.codegen.c.makefile.in", which
     *  is usually found as <code>$PTII/ptolemy/codegen/c/makefile.in</code>.
     *
     *  <p>The makefile is written to a directory named by the
     *  <i>codeDirectory</i> parameter, with a file name that is a
     *  sanitized version of the model name, and a ".mk" extension.
     *  Thus, for a model named "Foo", we might generate a makefile in
     *  "$HOME/codegen/Foo.mk".

     *  <p>Under Java under Windows, your <code>$HOME</code> variable
     *  is set to the value of the <code>user.home</code>System property,
     *  which is usually something like
     *  <code>C:\Documents and Settings\<i>yourlogin</i></code>, thus
     *  for user <code>mrptolemy</code> the makefile would be
     *  <code>C:\Documents and Settings\mrptolemy\codegen\Foo.mk</code>.
     *
     *  <p>The following variables are substituted
     *  <dl>
     *  <dt><code>@modelName@</code>
     *  <dd>The sanitized model name, created by invoking
     *  {@link ptolemy.util.StringUtilities#sanitizeName(String)} 
     *  on the model name.
     *  <dt><code>@PTCGIncludes@</code>
     *  <dd>The elements of the set of include command arguments that
     *  were added by calling {@link #addInclude(String)}, where each
     *  element is separated by a space.
     *  <dt><code>@PTCGLibraries@</code>
     *  <dd>The elements of the set of library command arguments that
     *  were added by calling {@link #addLibrary(String)}, where each
     *  element is separated by a space.
     *  </dl>

     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected void _writeMakefile() throws IllegalActionException {

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        //try {
        // Check if needs to overwrite.
        if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                && codeDirectory.asFile().exists()) {
            // FIXME: It is totally bogus to ask a yes/no question
            // like this, since it makes it impossible to call
            // this method from a script.  If the question is
            // asked, the build will hang.
            if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                    + " exists. OK to overwrite?")) {
                throw new IllegalActionException(this,
                        "Please select another file name.");
            }
        }

        File codeDirectoryFile = codeDirectory.asFile();
        if (codeDirectoryFile.isFile()) {
            throw new IllegalActionException(this, "Error: "
                    + codeDirectory.stringValue() + " is a file, "
                    + " it should be a directory.");
        }

        if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
            throw new IllegalActionException(this, "Failed to make the \""
                    + codeDirectory.stringValue() + "\" directory.");
        }

        Map substituteMap;
        try {
            // Add substitutions for all the parameter.
            // For example, @generatorPackage@ will be replaced with
            // the value of the generatorPackage.
            substituteMap = CodeGeneratorUtilities.newMap(this);
            substituteMap.put("@modelName@", _sanitizedModelName);
            substituteMap
                    .put("@PTCGIncludes@", _concatenateElements(_includes));
            substituteMap.put("@PTCGLibraries@",
                    _concatenateElements(_libraries));

            // Define substitutions to be used in the makefile
            substituteMap.put("@PTJNI_NO_CYGWIN@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@",
                    "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                    "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@",
                    "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@",
                    "");

            String osName = StringUtilities.getProperty("os.name");
            if (osName != null) {
                // FIXME: What about Darwin/MacOS?
                if (osName.startsWith("Windows")) {
                    substituteMap.put("@PTJNI_NO_CYGWIN@", "-mno-cygwin");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                            "-Wl,--add-stdcall-alias");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@",
                            "dll");
                } else if (osName.startsWith("SunOS")) {
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@",
                            "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                            "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@",
                            "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@",
                            "so");
                } else if (osName.startsWith("Linux")) { 
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@",
                            "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@",
                            "so");
                }
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Problem generating substitution map from " + _model);
        }

        BufferedReader makefileTemplateReader = null;

        // Look for a .mk.in file with the same name as the model.
        String makefileTemplateName;
        URIAttribute uriAttribute = (URIAttribute) _model.getAttribute("_uri",
                URIAttribute.class);
        if (uriAttribute != null) {
            String uriString = uriAttribute.getURI().toString();
            makefileTemplateName = uriString.substring(0, uriString
                    .lastIndexOf("/") + 1)
                    + _sanitizedModelName + ".mk.in";
        } else {
            // The model does not have a _uri attribute, so 
            // Look for the generic C makefile.in
            // Note this code is repeated in the catch below.
            makefileTemplateName = generatorPackage.stringValue().replace('.',
                    '/')
                    + (isTopLevel() ? "/makefile.in" : "/jnimakefile.in");
        }
        // If necessary, add a trailing / after codeDirectory.
        String makefileOutputName = codeDirectory.stringValue()
                + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                        .stringValue().endsWith("\\")) ? "/" : "")
                + _sanitizedModelName + ".mk";

        try {
            try {
                makefileTemplateReader = CodeGeneratorUtilities
                        .openAsFileOrURL(makefileTemplateName);
            } catch (IOException ex) {
                String makefileTemplateName2 = "<unknown>";
                try {
                    // Look for the generic C makefile.in
                    // Note this line is a repeat from the _uri check above.
                    makefileTemplateName2 = generatorPackage.stringValue()
                            .replace('.', '/')
                            + (isTopLevel() ? "/makefile.in"
                                    : "/jnimakefile.in");
                    makefileTemplateReader = CodeGeneratorUtilities
                            .openAsFileOrURL(makefileTemplateName2);
                } catch (IOException ex2) {
                    throw new IllegalActionException(this, ex2,
                            "Failed to open \"" + makefileTemplateName
                                    + "\" and \"" + makefileTemplateName2
                                    + "\" for reading.");
                }
                makefileTemplateName = makefileTemplateName2;
            }

            _executeCommands.stdout("Reading \"" + makefileTemplateName + "\","
                    + _eol + "    writing \"" + makefileOutputName + "\"");
            CodeGeneratorUtilities.substitute(makefileTemplateReader,
                    substituteMap, makefileOutputName);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to read \""
                    + makefileTemplateName + "\" or write \""
                    + makefileOutputName + "\"");
        } finally {
            if (makefileTemplateReader != null) {
                try {
                    makefileTemplateReader.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to close \"" + makefileTemplateName + "\"");
                }
            }
        }
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a Set of Strings, return a string where each element of the
     *  Set is separated by a space.
     *  @param set The Set of Strings.
     *  @return A String that contains each element of the Set separated by
     *  a space.
     */
    private static String _concatenateElements(Set set) {
        StringBuffer buffer = new StringBuffer();
        Iterator sets = set.iterator();
        while (sets.hasNext()) {
            if (buffer.length() > 0) {
                buffer.append(" ");
            }
            buffer.append((String) sets.next());
        }
        return buffer.toString();
    }

    /** Set of type/function combinations that are not supported.
     *  We use one method so as to reduce code size.
     */
    private static Set _unsupportedTypeFunctions;

    /** Types that share the scalarDelete() method, which does nothing. 
     *  We use one method so as to reduce code size.
     */
    private static Set _scalarDeleteTypes;


    static {
        _unsupportedTypeFunctions = new HashSet();
        _unsupportedTypeFunctions.add("String_divide");
        _unsupportedTypeFunctions.add("String_multiply");
        _unsupportedTypeFunctions.add("String_negate");
        _unsupportedTypeFunctions.add("String_one");
        _unsupportedTypeFunctions.add("String_subtract");

        _unsupportedTypeFunctions.add("Boolean_divide");
        _unsupportedTypeFunctions.add("Boolean_multiply");
        _unsupportedTypeFunctions.add("Boolean_subtract");

        _scalarDeleteTypes = new HashSet();
        _scalarDeleteTypes.add("Boolean");
        _scalarDeleteTypes.add("Double");
        _scalarDeleteTypes.add("Int");
        _scalarDeleteTypes.add("Long");
    }
}
