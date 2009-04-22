/* Code generator helper class associated with the PtidesDirector class.

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
package ptolemy.codegen.c.domains.ptides.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////PtidesEmbeddedDirector

/**
 Code generator helper associated with the PtidesEmbeddedDirector class. This class
 is also associated with a code generator.
 Also unlike the Ptolemy implementation, the execution does not depend on the WCET
 of actor.
 @author Jia Zou
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating red (jiazou)
 @Pt.AcceptedRating
 */
public class PtidesEmbeddedDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  PtidesEmbeddedDirector.
     *  @param ptidesEmbeddedDirector The associated director
     *  ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector
     */
    public PtidesEmbeddedDirector(ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector ptidesEmbeddedDirector) {
        super(ptidesEmbeddedDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         static variables                       ////
    /** Each output port has a fixed buffer size, which is set using this variable.
     *  FIXME: this variable can possibly be inferred from the PTIDES model and
     *  an event model.
     *  FIXME: If the PTIDES scheduler were to adopt Park's algorithm, then this
     *  size could be used as a "first guess".
     */
    static final int _outputPortBufferSize = 10;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the helpers for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    /*
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_codeGenerator.comment("The firing of the director."));

        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {

            Actor actor = (Actor) actors.next();
            code.append(_eol + "void* " +
                    CodeGeneratorHelper.generateName((NamedObj) actor) + "(void* arg) {" + _eol);
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helper.generateFireCode());
            code.append(helper.generateTypeConvertFireCode());

            //code.append("}" + _eol);
        }
        return code.toString();
    }*/

    /** Generate the initialize code for the associated PtidesEmbedded director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        //ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
        //.getContainer();
        //CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);

        // FIXME: I don't really know what this does, and I don't know what I would use this for...
        // Generate code for creating external initial production.
        /*
        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int rate = DFUtilities.getTokenInitProduction(outputPort);

            if (rate > 0) {
                for (int i = 0; i < outputPort.getWidthInside(); i++) {
                    if (i < outputPort.getWidth()) {
                        String name = outputPort.getName();

                        if (outputPort.isMultiport()) {
                            name = name + '#' + i;
                        }

                        for (int k = 0; k < rate; k++) {
                            code.append(CodeStream.indent(containerHelper
                                    .getReference(name + "," + k)));
                            code.append(" = ");
                            code.append(containerHelper.getReference("@" + name
                                    + "," + k));
                            code.append(";" + _eol);
                        }
                    }
                }

                // The offset of the ports connected to the output port is
                // updated by outside director.
                _updatePortOffset(outputPort, code, rate);
            }
        }
        */

        code.append(_codeStream.getCodeBlock("initPIBlock"));
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     *   FIXME: fire code for each function, as well as the scheduler, should all go here
     *   Take care of platform independent code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code =
            new StringBuffer(super.generatePreinitializeCode());

        // Deal with buffers by first updating the buffer size on each of the output ports.
        // Then, for each output port, we also need to create two variables, which keep
        // track of where the head and tail of the data values are in the buffer.

        code.append(_generatePtrToEventHeadCodeInputs());
        
        code.append(_generateActorFireCode());
        
        // FIXME: after each firing, the output needs to be sent to the 

        List args = new LinkedList();
        args.add(_generateDirectorHeader());

        args.add(((CompositeActor)
                _director.getContainer()).deepEntityList().size());

        // FIXME: this fetching of preinitBlock only fetches the platform dependent part.
        code.append("void initPIBlock() {" + _eol);
        code.append(_codeStream.getCodeBlock("initPIBlock"));
        code.append("}" + _eol);

        code.append(_codeStream.getCodeBlock("preinitPIBlock", args));

        return code.toString();
    }
    
    /**
     * Generate the type conversion fire code. Here we don't actually want to generate
     * any type conversion statement, the type conversion is done within the event creation.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode() throws IllegalActionException {
        return "";
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            //CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            //code.append(helperObject.generateVariableDeclaration());

            String name = CodeGeneratorHelper.generateName(getComponent());

            // Generate variable declarations for input ports.
            String inputVariableDeclaration = _generateInputVariableDeclaration(actor);
            if (inputVariableDeclaration.length() > 1) {
                code.append(_eol
                        + _codeGenerator.comment(name + "'s input variable declarations."));
                code.append(inputVariableDeclaration);
            }

            // Generate variable declarations for output ports.
            String outputVariableDeclaration = _generateOutputVariableDeclaration(actor);
            if (outputVariableDeclaration.length() > 1) {
                code.append(_eol
                        + _codeGenerator.comment(name + "'s output variable declarations."));
                code.append(outputVariableDeclaration);
            }

            // Generate type convert variable declarations.
            /*
            String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration();
            if (typeConvertVariableDeclaration.length() > 1) {
                code.append(_eol
                        + _codeGenerator.comment(name + "'s type convert variable declarations."));
                code.append(typeConvertVariableDeclaration);
            }*/

            // code.append(helperObject.generateVariableDeclaration());
        }

        return processCode(code.toString());
    }
    
//    public String getReference(TypedIOPort port, String[] channelAndOffset,
//            boolean forComposite, boolean isWrite, CodeGeneratorHelper helper)
//            throws IllegalActionException {
//        if (port.isInput()){
//            return "Event_Head_" + port.getContainer().getName() + "_" + port.getName()
//                + "[" + channelAndOffset[0] + "]->" + port.getType().toString() + "_Value";
//        } else {
//            return helper.getReference(port, channelAndOffset, forComposite, isWrite);
//        }
//    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Generate code for director header.
     *
     *  @return Code that declaresheader for director
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */

    protected String _generateDirectorHeader() {
        return CodeGeneratorHelper.generateName(_director) + "_controlBlock";
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
     * helpers for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(Channel source,
            Channel sink, int offset) throws IllegalActionException {

        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) sink.port).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        // The references are associated with their own helper, so we need
        // to find the associated helper.
        String sourcePortChannel = source.port.getName() + "#"
        + source.channelNumber + ", " + offset;
        String sourceRef = ((CodeGeneratorHelper) _getHelper(source.port
                .getContainer())).getReference(sourcePortChannel);

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
        + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((CodeGeneratorHelper) _getHelper(sink.port
                .getContainer())).getReference(sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        String sourceCodeGenType = codeGenType(sourceType);
        String sinkCodeGenType = codeGenType(sinkType);

        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
            result = "$convert_" + sourceCodeGenType + "_"
            + sinkCodeGenType + "(" + result + ")";
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /** Update buffer sizes for each output port to the value specified by _outputPortBufferSize
     *  Do not update the buffer sizes of the input ports, assuming they are 1.
     *  @exception IllegalActionException If thrown while setting
     *   buffer size.
     *  @see #_outputPortBufferSize
     */
    protected void _updatePortBufferSize() throws IllegalActionException {

        for (Actor actor : (List<Actor>)((CompositeActor) _director.getContainer()).deepEntityList()) {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            for (IOPort outputPort : (List<IOPort>)actor.outputPortList()) {
                for (int channel = 0; channel < outputPort.getWidth(); channel++) {
                    actorHelper.setBufferSize(outputPort, channel, _outputPortBufferSize);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    /** fire methods for each actor.
     * @return fire methods for each actor
     * @exception IllegalActionException
     * @exception IllegalActionException If thrown when getting the port's helper.
     */
    private String _generateActorFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
        .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = (CodeGeneratorHelper)_getHelper((NamedObj)actor);
            code.append("void " + CodeGeneratorHelper.generateName((NamedObj) actor) + "() " + "{" + _eol);
            code.append(helper.generateFireCode());
            
            // After each actor firing, the Event Head ptr needs to point to null
            code.append(_generateClearEventHeadCode(actor));
            code.append("}" + _eol);
        }
        
        return code.toString();
    }
        
    /** code that keeps track of the head and tail of each buffer, as well as the size of
     *  each buffer. The size is used to make sure this buffer does not overflow.
     *  @return code
     *  @throws IllegalActionException
     */
    private String _generateBufferHeadTailCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        for (Actor actor: (List<Actor>)(((CompositeActor) _director.getContainer()).deepEntityList())) {
            for (IOPort outputPort: (List<IOPort>)actor.outputPortList()){
                for (int channel = 0; channel < outputPort.getWidth(); channel++) {
                    code.append("static int " + actor.getName() + "_" + outputPort.getName() + "_" + channel 
                            + "_Head = 0;" + _eol);
                    code.append("static int " + actor.getName() + "_" + outputPort.getName() + "_" + channel
                            + "_Tail = 0;" + _eol);
                    code.append("static int " + actor.getName() + "_" + outputPort.getName() + "_" + channel 
                            + "_Size = 0;" + _eol);
                }
            }
        }
        return code.toString();
    }
    
    /** This code reset the Event_Head pointer for each channel to null.
     * @param actor The actor which the input channels reside, whose pointers are pointed to null
     * @return
     * @throws IllegalActionException 
     */
    private String _generateClearEventHeadCode(Actor actor) throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* generate code for clearing Event Head buffer. */" + _eol);
        for (IOPort inputPort: (List<IOPort>)actor.inputPortList()) {
            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                code.append("Event_Head_" + actor.getName() + "_" + inputPort.getName() 
                        + "[" + channel + "] = NULL;" + _eol);
            }
        }
        return code.toString();
    }
    
    private String _generatePtrToEventHeadCodeInputs() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        for (Actor actor: (List<Actor>)(((CompositeActor) _director.getContainer()).deepEntityList())) {
            for (IOPort inputPort: (List<IOPort>)actor.inputPortList()){
                if (inputPort.getWidth() > 0) {
                    code.append("Event* Event_Head_" + actor.getName() + "_" + inputPort.getName() 
                            + "[" + inputPort.getWidth() + "] = {NULL");
                    for (int channel = 1; channel < inputPort.getWidth(); channel++) {
                        code.append(", NULL");
                    }
                    code.append("};" + _eol);
                }
            }
        }
        return code.toString();
    }

    /** Generate input variable declarations.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateInputVariableDeclaration(Actor actor)
            throws IllegalActionException {
        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();

        StringBuffer code = new StringBuffer();

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (!inputPort.isOutsideConnected()) {
                continue;
            }

            code.append("static " + targetType(inputPort.getType()) + " "
                    + generateName(inputPort));

            int bufferSize = getBufferSize(inputPort);
            if (inputPort.isMultiport()) {
                code.append("[" + inputPort.getWidth() + "]");
                if (bufferSize > 1 || dynamicReferencesAllowed) {
                    code.append("[" + bufferSize + "]");
                }
            } else {
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
            }

            code.append(";" + _eol);
        }

        return code.toString();
    }

    /** Generate output variable declarations.
     *  The output is an buffer array.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateOutputVariableDeclaration(Actor actor)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // Each output port should have a buffer.
            code.append("static " + targetType(outputPort.getType()) + " "
                    + generateName(outputPort));

            if (outputPort.isMultiport()) {
                code.append("[" + outputPort.getWidthInside() + "]");
            }

            int bufferSize = _getBufferSize(outputPort);

            if (bufferSize > 1) {
                code.append("[" + bufferSize + "]");
            }
            code.append(";" + _eol);
        }

        return code.toString();
    }

    /**
     * Return the buffer size of a given port, which is the maximum of
     * the bufferSizes of all channels of the given port.
     * @param port The given port.
     * @return The buffer size of the given port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(IOPort, int)} method throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    private int _getBufferSize(IOPort port) throws IllegalActionException {
        int bufferSize = 1;

        int length = 0;

        length = port.getWidth();

        for (int i = 0; i < length; i++) {
            int channelBufferSize = _getBufferSize(port, i);

            if (channelBufferSize > bufferSize) {
                bufferSize = channelBufferSize;
            }
        }
        return bufferSize;
    }

    /** Get the buffer size of the given port of this actor.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @return The buffer size of the given port and channel.
     *  @exception IllegalActionException If the getBufferSize()
     *   method of the actor helper class throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    private int _getBufferSize(IOPort port, int channelNumber)
    throws IllegalActionException {
        return ((PortCodeGenerator) _getHelper(port))
        .getBufferSize(channelNumber);
    }

}
