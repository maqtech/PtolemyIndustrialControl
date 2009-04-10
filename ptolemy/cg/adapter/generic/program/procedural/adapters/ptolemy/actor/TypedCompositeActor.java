/* Code generator adapter for typed composite actor.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.CodeStream;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.cg.lib.CompiledCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator adapter for typed composite actor.

 @author Gang Zhou, Contributors: Teale Fristoe
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends ptolemy.cg.adapter.generic.adapters.ptolemy.actor.TypedCompositeActor {
    
    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /**
     * For each actor in this typed composite actor, determine which ports
     * need type conversion.
     * @exception IllegalActionException If any of the adapters of the
     * inside actors is unavailable.
     * @see ptolemy.codegen.kernel.CodeGeneratorAdapter#analyzeTypeConvert
     */
    @Override
    public void analyzeTypeConvert() throws IllegalActionException {
        super.analyzeTypeConvert();
        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            try {
                CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
                adapterObject.analyzeTypeConvert();
            } catch (Throwable throwable) {
                throw new IllegalActionException(actor, throwable,
                        "Failed to determine which ports need type conversion.");
            }
        }
    }

    /** Generate the fire code of the associated composite actor. This method
     *  first generates code for transferring any data from the input
     *  ports of this composite to the ports connected on the inside
     *  by calling the generateTransferInputsCode() method of the
     *  local director adapter. It then invokes the generateFireCode()
     *  method of its local director adapter.  After the
     *  generateFireCode() method of the director adapter returns,
     *  generate code for transferring any output data created by
     *  calling the local director adapter's
     *  generateTransferOutputsCode() method.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the adapter associated
     *  with an actor throws it while generating fire code for the
     *  actor, or the director adapter throws it while generating code
     *  for transferring data.
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment(2,
                        "Fire Composite "
                        + getComponent().getName()));

        code.append(super._generateFireCode());

        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        Iterator<?> inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();

        // Update port parameters.
        StringBuffer tempCode = new StringBuffer();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort && inputPort.isOutsideConnected()) {

                PortParameter portParameter = ((ParameterPort) inputPort)
                        .getParameter();
                tempCode.append(CodeStream.indent(getCodeGenerator()
                        .generateVariableName(portParameter)));
                // FIXME: The = sign is language specific.
                tempCode.append(" = ");
                tempCode.append(getReference(inputPort.getName()));
                tempCode.append(";" + _eol);
            }
        }
        if (tempCode.length() > 0) {
            code.append(CodeStream.indent(getCodeGenerator().comment("Update "
                    + getComponent().getName() + "'s port parameters")));
            code.append(tempCode);
        }

        // Transfer the data to the inside.
        inputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (!(inputPort instanceof ParameterPort)) {
                directorAdapter.generateTransferInputsCode(inputPort, code);
            }
        }

        // Generate the fire code by the director adapter.
        code.append(directorAdapter.generateFireCode());

        // Transfer the data to the outside.
        Iterator<?> outputPorts = ((ptolemy.actor.CompositeActor) getComponent())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            directorAdapter.generateTransferOutputsCode(outputPort, code);
            //generateTransferOutputsCode(outputPort, code);
        }
        return processCode(code.toString());
    }
    
    /** Generate The fire function code. This method is called when
     *  the firing code of each actor is not inlined. Each actor's
     *  firing code is in a function with the same name as that of the
     *  actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor compositeActor = (CompositeActor) getComponent();
        ptolemy.actor.Director director = compositeActor.getDirector();
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(director);
        code.append(directorAdapter.generateFireFunctionCode());
        if (!(compositeActor instanceof CompiledCompositeActor && ((BooleanToken) ((ProceduralCodeGenerator) getCodeGenerator()).generateEmbeddedCode
                .getToken()).booleanValue())) {
            code.append(super.generateFireFunctionCode());
        }
        return processCode(code.toString());
    }

    /** Generate the initialize code of the associated composite actor. It
     *  first resets the read and write offset of all input ports of all
     *  contained actors and all output ports. It then gets the result of
     *  generateInitializeCode() method of the local director adapter.
     *
     *  @return The initialize code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor
     *   or while resetting read and write offset.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer initializeCode = new StringBuffer();
        //initializeCode.append(getCodeGenerator().comment(1,
        //        "Initialize composite "
        //        + getComponent().getName()));

        //initializeCode.append(super.generateInitializeCode());


        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());

        // Generate the initialize code by the director adapter.
        initializeCode.append(directorAdapter.generateInitializeCode());

        return processCode(initializeCode.toString());
    }

    /** Generate mode transition code. It delegates to the director adapter
     *  of the local director. The mode transition code generated in this
     *  method is executed after each global iteration, e.g., in HDF model.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the director adapter throws it
     *   while generating mode transition code.
     */
    @Override
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        directorAdapter.generateModeTransitionCode(code);
    }

    /** Generate the postfire code of the associated composite
     *  actor. It returns the result of generatePostfireCode() method
     *  of the local director adapter.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating postfire code for the actor.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(super.generatePostfireCode());
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorAdapter.generatePostfireCode());
        return processCode(code.toString());
    }


    /** Generate variable declarations for input ports, output ports and
     *  parameters if necessary, as well as for the director and the
     *  contained actors.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating variable declarations for
     *   the actor.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(super.generateVariableDeclaration());

        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorAdapter.generateVariableDeclaration());

        return processCode(code.toString());
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating variable declarations for
     *   the actor.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // code.append(_eol + getCodeGenerator().comment(1, "Composite actor "
        //                + getComponent().getName()
        //                + "'s variable initialization."));

        code.append(super.generateVariableInitialization());

        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorAdapter.generateVariableInitialization());

        return processCode(code.toString());
    }

    /** Generate the wrapup code of the associated composite actor. It returns
     *  the result of generateWrapupCode() method of the local director adapter.
     *
     *  @return The wrapup code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating wrapup code for the actor.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(super.generateWrapupCode());
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        code.append(directorAdapter.generateWrapupCode());
        return processCode(code.toString());
    }

    /** Return an int array of firings per global iteration. For each
     *  internal configuration of this composite actor, the array
     *  contains a corresponding element representing the number of
     *  firings of this composite actor per global iteration.
     *
     *  @return An int array of firings per global iteration.
     *  @see #setFiringsPerGlobalIteration(int[])
     */
    public int[] getFiringsPerGlobalIteration() {
        return _firingsPerGlobalIteration;
    }

    /** Get the header files needed by the code generated from this adapter
     *  class. It returns the result of calling getHeaderFiles() method of
     *  the adapters of all contained actors.
     *
     *  @return A set of strings that are header files.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating header files for the actor.
     */
    @Override
    public Set<String> getHeaderFiles() throws IllegalActionException {
        Set<String> files = new LinkedHashSet<String>();
        files.addAll(super.getHeaderFiles());

        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
            files.addAll(adapterObject.getHeaderFiles());
        }

        // Get headers needed by the director adapter.
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        files.addAll(directorAdapter.getHeaderFiles());

        return files;
    }

    /** Return the include directories specified in the "includeDirectories"
     *  blocks in the templates of the actors included in this CompositeActor.
     *  @return A Set of the include directories.
     *  @exception IllegalActionException If thrown when gathering
     *  include directories.
     */
    @Override
    public Set<String> getIncludeDirectories() throws IllegalActionException {
        Set<String> includeDirectories = new LinkedHashSet<String>();
        includeDirectories.addAll(super.getIncludeDirectories());

        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
            includeDirectories.addAll(adapterObject.getIncludeDirectories());
        }

        // Get include directories needed by the director adapter.
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        includeDirectories.addAll(directorAdapter.getIncludeDirectories());

        return includeDirectories;
    }

    /** Return the libraries specified in the "libraries" blocks in the
     *  templates of the actors included in this CompositeActor.
     *  @return A Set of libraries.
     *  @exception IllegalActionException If thrown when gathering libraries.
     */
    @Override
    public Set<String> getLibraries() throws IllegalActionException {
        Set<String> libraries = new LinkedHashSet<String>();
        libraries.addAll(super.getLibraries());

        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
            libraries.addAll(adapterObject.getLibraries());
        }

        // Get libraries needed by the director adapter.
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        libraries.addAll(directorAdapter.getLibraries());

        return libraries;
    }

    /** Return a set of parameters that will be modified during the
     *  execution of the model. These parameters are those returned by
     *  getModifiedVariables() method of directors or actors that
     *  implement ExplicitChangeContext interface.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the adapter associated with an actor
     *   or director throws it while getting modified variables.
     */
    @Override
    public Set<Parameter> getModifiedVariables() throws IllegalActionException {
        Set<Parameter> set = new HashSet<Parameter>();
        set.addAll(super.getModifiedVariables());

        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        set.addAll(directorAdapter.getModifiedVariables());
        return set;
    }

    /** Return a two-dimensional int array of rates of this actor. For
     *  each internal configuration of this composite actor, the array
     *  contains a corresponding one-dimensional int array
     *  representing the rates of all ports of this composite
     *  actor. It returns null when there is only one internal
     *  configuration, e.g., when the internal model is an SDF model.
     *
     *  @return A two-dimensional int array of rates of this actor or null.
     *  @see #setRates(int[][])
     */
    public int[][] getRates() {
        return _rates;
    }

    /** Generate a set of shared code fragments of the associated
     *  composite actor.  It returns the result of calling
     *  getSharedCode() method of the adapters of all contained actors.
     *
     *  @return a set of shared code fragments.
     *  @exception IllegalActionException If the adapter associated with
     *  an actor throws it while generating shared code for the actor.
     */
    @Override
    public Set<String> getSharedCode() throws IllegalActionException {

        // Use LinkedHashSet to give order to the shared code.
        Set<String> sharedCode = new LinkedHashSet<String>();
        sharedCode.addAll(super.getSharedCode());

        Iterator<?> actors = ((ptolemy.actor.CompositeActor) getComponent())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
            sharedCode.addAll(adapterObject.getSharedCode());
        }

        // Get shared code used by the director adapter.
        Director directorAdapter = (Director) getCodeGenerator().getAdapter(((ptolemy.actor.CompositeActor) getComponent())
                .getDirector());
        sharedCode.addAll(directorAdapter.getSharedCode());

        return sharedCode;
    }

    /** Set the int array of firings per global iteration. For each
     *  internal configuration of this composite actor, the array
     *  contains a corresponding element representing the number of
     *  firings of this composite actor per global iteration.
     *
     *  @param firingsPerGlobalIteration An int array of firings per
     *   global iteration
     *  @see #getFiringsPerGlobalIteration()
     */
    public void setFiringsPerGlobalIteration(int[] firingsPerGlobalIteration) {
        _firingsPerGlobalIteration = firingsPerGlobalIteration;
    }

    /** Set the two-dimensional int array of rates of this actor. For
     *  each internal configuration of this composite actor, the array
     *  contains a corresponding one-dimensional int array
     *  representing the rates of all ports of this composite actor.
     *
     *  @param rates A two-dimensional int array of rates of this actor.
     *  @see #getRates()
     */
    public void setRates(int[][] rates) {
        _rates = rates;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An int array of firings per global iteration.
     */
    private int[] _firingsPerGlobalIteration;

    /** A two-dimensional int array of rates of this actor.
     */
    private int[][] _rates;
}
