/* Base class for program code generator adapter.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program;

import java.util.Set;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapterStrategy.Channel;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;


///////////////////////////////////////////////////////////////////////
////ProgramCodeGeneratorAdapter

/**
* FIXME: Overhaul comments.
* Base class for code generator adapter.
*
* <p>Subclasses should override generateFireCode(),
* generateInitializeCode() generatePostfireCode(),
* generatePreinitializeCode(), and generateWrapupCode() methods by
* appending a corresponding code block.
*
* <p>Subclasses should be sure to properly indent the code by
* either using the code block functionality in methods like
* _generateBlockCode(String) or by calling
* {@link ptolemy.codegen.kernel.CodeStream#indent(String)},
* for example:
* <pre>
*     StringBuffer code = new StringBuffer();
*     code.append(super.generateWrapupCode());
*     code.append("// Local wrapup code");
*     return processCode(CodeStream.indent(code.toString()));
* </pre>
*
* @author Ye Zhou, Gang Zhou, Edward A. Lee, Bert Rodiers Contributors: Christopher Brooks, Teale Fristoe
* @version $Id$
* @since Ptolemy II 7.1
* @Pt.ProposedRating Yellow (eal)
* @Pt.AcceptedRating Yellow (eal)
*/
public class ProgramCodeGeneratorAdapter extends CodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given component.
     *  @param component The associated component.
     */
    public ProgramCodeGeneratorAdapter(NamedObj component) {
        _component = component;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Find out each output port that needs to be converted for the
     * actor associated with this adapter. Then, mark these ports along
     * with the sink ports (connection).
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        _strategy.analyzeTypeConvert();
    }
    
    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. It checks the inline parameter
     * of the code generator. If the value is true, it generates the actor
     * fire code and the necessary type conversion code. Otherwise, it
     * generate an invocation to the actor function that is generated by
     * generateFireFunctionCode. 
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        return _strategy.generateFireCode();
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return _strategy.generateFireFunctionCode();
    }

    /**
     * Generate the initialize code. In this base class, return empty
     * string. Subclasses may extend this method to generate initialize
     * code of the associated component and append the code to the
     * given string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return _strategy.generateInitializeCode();
    }

    /** Generate mode transition code. The mode transition code
     *  generated in this method is executed after each global
     *  iteration, e.g., in HDF model.  Do nothing in this base class.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code) throws IllegalActionException {
        _strategy.generateModeTransitionCode(code);
    }
    
    /**
     * Generate the postfire code. In this base class, do nothing. Subclasses
     * may extend this method to generate the postfire code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generatePostfireCode() throws IllegalActionException {
        return _strategy.generatePostfireCode();
    }

    /** Generate the prefire code of the associated composite actor.
    *  @return The prefire code of the associated composite actor.
    *  @exception IllegalActionException If illegal macro names are found.
    */    
    public String generatePrefireCode() throws IllegalActionException {
        // FIXME: This is to be used in future re-structuring.
        return _strategy.generatePrefireCode();
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the adapter.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        return _strategy.generatePreinitializeCode();
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @param forComposite True if we are generating code for a composite.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode(boolean forComposite) throws IllegalActionException {
        return _strategy.generateTypeConvertFireCode(forComposite);
    }


    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization() throws IllegalActionException {
        return "";
    }
    
    /**
     * Generate the wrapup code. In this base class, do nothing. Subclasses
     * may extend this method to generate the wrapup code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated wrapup code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return _strategy.generateWrapupCode();
    }    

    /** Get the code generator adapter associated with the given component.
     *  @param component The given component.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    final public ProgramCodeGeneratorAdapter getAdapter(Object component) throws IllegalActionException {
        return (ProgramCodeGeneratorAdapter) getCodeGenerator().getAdapter(component);
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    public ProgramCodeGenerator getCodeGenerator() {
        return _strategy.getCodeGenerator();
    }


    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        return _strategy.getHeaderFiles();
    }


    /** Return a set of directories to include for the generated code.
     *  @return A Set containing the contents of the actor's
     *   "includeDirectories" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getIncludeDirectories() throws IllegalActionException {
        return _strategy.getIncludeDirectories();
    }


    /** Return a set of libraries to link in the generated code.
     *  @return A Set containing the libraries in the actor's
     *   "libraries" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getLibraries() throws IllegalActionException {
        return _strategy.getLibraries();
    }

    /** Return a set of directories to find libraries in.
     *  @return A Set containing the directories in the actor's
     *   "libraryDirectories" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getLibraryDirectories() throws IllegalActionException {
        return _strategy.getLibraryDirectories();
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The actor gets those variables if it implements
     *  ExplicitChangeContext interface or it contains PortParameters.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If an actor throws it while getting
     *   modified variables.
     */
    public Set<Parameter> getModifiedVariables() throws IllegalActionException {
        return _strategy.getModifiedVariables();

    }
    /** Return the value or an expression in the target language for
     *  the specified parameter of the associated actor.  If the
     *  parameter is specified by an expression, then the expression
     *  will be parsed. If any parameter referenced in that expression
     *  is specified by another expression, the parsing continues
     *  recursively until either a parameter is directly specified by
     *  a constant or a parameter can be directly modified during
     *  execution in which case a reference to the parameter is
     *  generated.
     *
     *  @param name The name of the parameter.
     *  @param container The container to search upwards from.
     *  @return The value or expression as a string.
     *  @exception IllegalActionException If the parameter does not exist or
     *   does not have a value.
     */
    final public String getParameterValue(String name, NamedObj container)
            throws IllegalActionException {
        return _strategy.getParameterValue(name, container);
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     *  FIXME: need documentation on the input string format.
     *
     *  @param name The name of the parameter or port
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    final public String getReference(String name) throws IllegalActionException {
        return _strategy.getReference(name);
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     *  FIXME: need documentation on the input string format.
     *
     *  @param name The name of the parameter or port
     *  @param isWrite Whether to generate the write or read offset.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite) 
            throws IllegalActionException {
        return _strategy.getReference(name, isWrite);
    }

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this adapter should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set<String> getSharedCode() throws IllegalActionException {
        return _strategy.getSharedCode();
    }
    
    /** Get the strategy associated with this adapter.
     *  @return The associated strategy.
     *   @see #setStrategy
     */
    // TODO rodiers: do we want to have this public?
    public ProgramCodeGeneratorAdapterStrategy getStrategy() {
        return _strategy;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _strategy.getComponent();
    }
    
    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    final public String processCode(String code) throws IllegalActionException {
        return _strategy.processCode(code);
    }
    
    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _strategy.setCodeGenerator((ProgramCodeGenerator) codeGenerator);
    }

    /** Set the strategy for generating code for this adapter.
     * @param strategy The strategy.
     * @see #getStrategy
     */ 
    final public void setStrategy(Object strategy) {
     _strategy = (ProgramCodeGeneratorAdapterStrategy) strategy;
     _strategy.setComponent(_component);
     _strategy.setAdapter(this);
    }
    
    /**
     * Get the corresponding type in C from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     * FIXME rodiers: This is C specific
     */
    final public String targetType(Type ptType) {
        return _strategy.targetType(ptType);
    }
    
    public String toString() {
        return getComponent().toString() + "'s Adapter";
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                    ////
    
    
    /**
     * Generate the fire code. This method is intended to be
     * overwritten by sub-classes to generate actor-specific code. 
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generateFireCode() throws IllegalActionException {
        return _strategy._generateFireCode();
    }
    

    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(Channel source,
            Channel sink, int offset) throws IllegalActionException {    
        return _strategy._generateTypeConvertStatement(source, sink, offset);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;
        // FIXME rodiers: This is defined multiple times, in different root classes
        //              Not really what we want.

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }      
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The strategy for generating code for this adapter.*/
    private ProgramCodeGeneratorAdapterStrategy _strategy;
    
    /** The associated component. */
    private NamedObj _component;
}
