/* Code generator adapter class associated with the SDFDirector class.

 Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.java.AutoAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.lib.CompiledCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// SDFDirector

/**
 Code generator adapter associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Ye Zhou, Gang Zhou, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        // Note that if the variablesAsArrays parameter is true, then
        // the port declarations are generated by
        // JavaCodeGenerator.generateTypeConvertCode().

        // If variablesAsArrays is true, then initialize arrays before
        // calling super.generateInitializeCode() so that the arrays are
        // initialized.  To illustrate the problem, run:
        // $PTII/bin/ptcg -language java -variablesAsArrays true $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/Commutator.xml

        StringBuffer code = new StringBuffer();
        boolean variablesAsArrays = ((BooleanToken) getCodeGenerator().variablesAsArrays
                .getToken()).booleanValue();
        if (variablesAsArrays) {
            CompositeActor container = (CompositeActor) _director
                    .getContainer();

            // $PTII/bin/ptcg -verbosity 10 -language java -variablesAsArrays true $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib/test/auto/SampleDelay.xml

            StringBuffer code2 = new StringBuffer();
            // Input ports that are multirate or have a width > 1
            Iterator<?> inputPorts = container.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) inputPorts.next();
                int rate = DFUtilities.getTokenInitConsumption(port);
                if (rate > 0) {

                    int bufferSize = ports.getBufferSize(port);
                    code2.append(generatePortName(port)
                            + " = new "
                            + targetType(port.getType())
                            + "["
                            + java.lang.Math.max(port.getWidth(),
                                    port.getWidthInside()) + "]" + "["
                            + bufferSize + "];" + _eol);
                } else if (port.getWidth() > 1 || port.getWidthInside() > 1) {
                    code2.append(generatePortName(port)
                            + " = new "
                            + targetType(port.getType())
                            + "["
                            + java.lang.Math.max(port.getWidth(),
                                    port.getWidthInside()) + "];" + _eol);
                }
            }

            // Output ports that are multirate or have a width > 1
            Iterator<?> outputPorts = container.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) outputPorts.next();
                int rate = DFUtilities.getTokenInitProduction(port);
                if (rate > 0) {
                    int bufferSize = ports.getBufferSize(port);
                    code2.append(generatePortName(port)
                            + " = new "
                            + targetType(port.getType())
                            + "["
                            + java.lang.Math.max(port.getWidth(),
                                    port.getWidthInside()) + "]" + "["
                            + bufferSize + "];" + _eol);
                } else if (port.getWidth() > 1 || port.getWidthInside() > 1) {
                    code2.append(generatePortName(port)
                            + " = new "
                            + targetType(port.getType())
                            + "["
                            + java.lang.Math.max(port.getWidth(),
                                    port.getWidthInside()) + "];" + _eol);
                }
            }
            if (code2.length() > 0) {
                code.append(getCodeGenerator()
                        .comment(
                                "Java SDFDirector: variablesAsArrays is true, "
                                        + "added decls for multiports and multirate multiports")
                        + code2.toString());
            }

            code.append(getCodeGenerator().comment(
                    "Java SDFDirector: variablesAsArrays is true, initializing "
                            + "ports."));
            Iterator<?> actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                if (actor instanceof CompositeActor
                        && ((CompositeActor) actor).isOpaque()
                //&& actor.getContainer() == null
                ) {
                    code.append(getCodeGenerator().comment(
                            "Java SDFDirector: actor is opaque: "
                                    + actor.getFullName()));
                    //code.append(adapterObject.generateVariableDeclaration());
                } else {
                    //code.append(getCodeGenerator().comment("Java SDFDirector: actor is not opaque: " + actor.getFullName()));
                    code.append(_generatePortVariableDeclaration(adapterObject));

                    if (variablesAsArrays) {
                        code.append(getCodeGenerator()
                                .comment(
                                        "Java SDFDirector: variablesAsArrays is true initialized elements in ports3"));
                        // Initialize elements in ports3_
                        outputPorts = actor.outputPortList().iterator();
                        while (outputPorts.hasNext()) {
                            TypedIOPort port = (TypedIOPort) outputPorts.next();
                            int rate = DFUtilities.getTokenInitProduction(port);
                            int bufferSize = ports.getBufferSize(port);
                            if (port.isMultiport() && rate > 0) {
                                code.append(generatePortName(port)
                                        + " = new "
                                        + targetType(port.getType())
                                        + "["
                                        + java.lang.Math.max(port.getWidth(),
                                                port.getWidthInside()) + "]"
                                        + "[" + bufferSize + "];" + _eol);
                            }
                        }
                    }
                }
            }
        }
        code.append(super.generateInitializeCode());
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _updatePortBufferSize();

        code.append(super.generatePreinitializeCode());

        _updatePortBufferSize();
        _portNumber = 0;

        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }
            Type type = ((TypedIOPort) inputPort).getType();
            String portName = TemplateParser
                    .escapePortName(inputPort.getName());

            String exceptionMessage = "Failed to generate code "
                    + "to transfer tokens to for input. "
                    + "The type of the \"" + portName + "\" output port was "
                    + type + ", which is not supported. "
                    + "Try setting the type of the \"" + portName
                    + "\" port by right clicking on the actor "
                    + "and selecting Customize -> Ports.";

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String tokensFromOneChannel = "tokensFromOneChannelOf"
                            + portName + i;

                    if (type == BaseType.INT) {
                        code.append("int[] " + tokensFromOneChannel
                                + " = (int[])(" + portName + "["
                                + String.valueOf(i) + "]);" + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append("double[] " + tokensFromOneChannel
                                + " = (double[])" + portName + "["
                                + String.valueOf(i) + "];" + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append("boolean[] " + tokensFromOneChannel
                                + " = (boolean[])" + portName + "["
                                + String.valueOf(i) + "];" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                        throw new IllegalActionException(inputPort,
                                exceptionMessage);
                    }

                    String portNameWithChannelNumber = portName;
                    if (inputPort.isMultiport()) {
                        portNameWithChannelNumber = portName + '#' + i;
                    }
                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + portNameWithChannelNumber + "," + k, false));
                        /*if (type == PointerToken.POINTER) {
                            code.append(" = (void *) "
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "];" + _eol);
                        } else {*/
                        code.append(" = " + tokensFromOneChannel + "[" + k
                                + "];" + _eol);
                        //}
                    }
                }
            }

        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = TemplateParser.escapePortName(inputPort
                            .getName());

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + name + "," + k, false)
                                + " = "
                                + compositeActorAdapter.getReference(name + ","
                                        + k, false) + ";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                code.append("Object[] tokensToAllOutputPorts = "
                        + " new Object[" + String.valueOf(numberOfOutputPorts)
                        + "];" + _eol);
            }

            String portName = TemplateParser.escapePortName(outputPort
                    .getName());
            String tokensToThisPort = "tokensTo" + portName;

            // FindBugs wants this instanceof check.
            if (!(outputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(outputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            Type type = ((TypedIOPort) outputPort).getType();

            String exceptionMessage = "Failed to generate code "
                    + "to transfer tokens to fulfill the output rate."
                    + "The type of the \"" + outputPort.getName()
                    + "\" output port was " + type
                    + ", which is not supported. "
                    + "Try setting the type of the \"" + outputPort.getName()
                    + "\" port by right clicking on the actor "
                    + "and selecting Customize -> Ports.";

            int numberOfChannels = outputPort.getWidthInside();

            // Find construct correct array type.
            if (type == BaseType.INT) {
                code.append("int[][] " + tokensToThisPort + " =" + " new int[ "
                        + String.valueOf(numberOfChannels) + "][" + rate + "];"
                        + _eol);

            } else if (type == BaseType.DOUBLE) {
                code.append("double[][] " + tokensToThisPort + " ="
                        + " new double[ " + String.valueOf(numberOfChannels)
                        + "][" + rate + "];" + _eol);
            } else if (type == BaseType.BOOLEAN) {
                code.append("boolean[][] " + tokensToThisPort + " ="
                        + " new boolean[ " + String.valueOf(numberOfChannels)
                        + "][" + rate + "];" + _eol);

            } else {
                // FIXME: need to deal with other types
                throw new IllegalActionException(outputPort, exceptionMessage);
            }

            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                String portNameWithChannelNumber = portName;
                if (outputPort.isMultiport()) {
                    portNameWithChannelNumber = portName + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    String portReference = compositeActorAdapter.getReference(
                            "@" + portNameWithChannelNumber + "," + k, false);
                    /*if (type == PointerToken.POINTER) {
                        code.append(tokensToOneChannel + "[" + k
                                + "] = " + "(int) " + portReference + ";"
                                + _eol);
                    } else {*/
                    code.append(tokensToThisPort + "[" + i + "][" + k + "] = "
                            + portReference + ";" + _eol);
                    //}
                }
            }
            code.append("tokensToAllOutputPorts ["
                    + String.valueOf(_portNumber) + "] = " + tokensToThisPort
                    + ";" + _eol);

            _portNumber++;

        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = TemplateParser.escapePortName(outputPort
                            .getName());

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    // code.append(CodeStream.indent(getCodeGenerator().comment(
                    //   "SDFDirector: token outside transfer " + name + " "
                    //   + rate + " " + compositeActorAdapter + " " + compositeActorAdapter.getClass())));

                    for (int k = 0; k < rate; k++) {
                        String result = compositeActorAdapter.getReference(name
                                + "," + k, false)
                                + " = "
                                + CodeStream.indent(compositeActorAdapter
                                        .getReference("@" + name + "," + k,
                                                false)) + ";" + _eol;
                        code.append(result);
                    }
                }
            }
        }
        // Generate the type conversion code before fire code.
        // Needed by adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/hoc/test/auto/CaseOpaque.xml
        code.append(compositeActorAdapter.generateTypeConvertFireCode(false));

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

    /** Generate constant for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer(
                super.generateVariableDeclaration());

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();

        Attribute period = _director.getAttribute("period");
        if (period != null) {
            Double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
            // Print period only if it is the containing actor is the top level.
            // FIXME: should this test also be applied to the other code?
            if (director.getContainer().getContainer() == null) {
                variableDeclarations.append(_eol
                        + getCodeGenerator().comment(
                                "Provide the period attribute as constant."));
                variableDeclarations
                        .append("public final static double PERIOD = "
                                + periodValue + ";" + _eol);
            }

        }

        if (director.getContainer().getContainer() == null) {
            variableDeclarations.append(_eol
                    + getCodeGenerator()
                            .comment("Provide the iteration count."));
            variableDeclarations.append("public static int _iteration = 0;"
                    + _eol);
        }
        return variableDeclarations.toString();
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    public JavaCodeGenerator getCodeGenerator() {
        return (JavaCodeGenerator) super.getCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate the input and output port declarations.
     *
     *  @param target The NamedProgramCodeGeneratorAdapter for which
     *  code needs to be generated.
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while generating
     *  the input or output variables.
     */
    protected String _generatePortVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        // Note that if the variablesAsArrays parameter is true, then
        // the port declarations are generated by
        // JavaCodeGenerator.generateTypeConvertCode() and
        // generateInitializeCode().  If variablesAsArrays is false,
        // the the port declarations are generated by
        // _generatePortVariablesDeclarations().

        StringBuffer code = new StringBuffer();
        ProgramCodeGenerator codeGenerator = getCodeGenerator();
        String name = CodeGeneratorAdapter.generateName(target.getComponent());

        // Generate variable declarations for input ports.
        String inputVariableDeclaration = _generateInputVariableDeclaration(target);
        if (inputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s input variable declarations."));
            code.append(inputVariableDeclaration);
        }

        // Generate variable declarations for output ports.
        String outputVariableDeclaration = _generateOutputVariableDeclaration(target);
        if (outputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s output variable declarations."));
            code.append(outputVariableDeclaration);
        }
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    protected String _generateVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        String name = CodeGeneratorAdapter.generateName(target.getComponent());
        // Generate variable declarations for referenced parameters.
        if (((BooleanToken) codeGenerator.variablesAsArrays.getToken())
                .booleanValue()) {
            code.append(codeGenerator
                    .comment("Java SDFDirector: variablesAsArrays is true, skipping "
                            + "generating "
                            + name
                            + "'s referenced parameter declarations."));

        } else {
            String referencedParameterDeclaration = _generateReferencedParameterDeclaration(target);
            if (referencedParameterDeclaration.length() > 1) {
                code.append(_eol
                        + codeGenerator.comment(name
                                + "'s referenced parameter declarations."));
                code.append(referencedParameterDeclaration);
            }

            code.append(_generatePortVariableDeclaration(target));
        }

        // Generate type convert variable declarations.
        String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration(target);
        if (typeConvertVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s type convert variable declarations."));
            code.append(typeConvertVariableDeclaration);
        }

        return processCode(code.toString());
    }

    /**
     * Return an unique label for the given attribute referenced
     * by the given adapter.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     * @param attribute The given attribute.
     * @param channelAndOffset The given channel and offset.
     * @return an unique label for the given attribute.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    @Override
    protected String _getParameter(NamedProgramCodeGeneratorAdapter target,
            Attribute attribute, String[] channelAndOffset)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        //FIXME: potential bug: if the attribute is not a parameter,
        //it will be referenced but not declared.
        if (attribute instanceof Parameter) {
            if (!_referencedParameters.containsKey(target)) {
                _referencedParameters.put(target, new HashSet<Parameter>());
            }
            _referencedParameters.get(target).add((Parameter) attribute);
        }

        result.append(getCodeGenerator().generateVariableName(attribute));

        if (!channelAndOffset[0].equals("")) {
            throw new IllegalActionException(target.getComponent(),
                    "a parameter cannot have channel number.");
        }

        if (!channelAndOffset[1].equals("")) {
            if (!(attribute instanceof Parameter)) {
                throw new InternalErrorException(attribute, null, "Attribute "
                        + attribute.getFullName() + " is not a Parameter.");
            } else {
                Type elementType = ((ArrayType) ((Parameter) attribute)
                        .getType()).getElementType();

                //result.append("[" + channelAndOffset[1] + "]");
                result.insert(
                        0,
                        "("
                                + getCodeGenerator().codeGenType(elementType)
                                        .replace("Array", "Token")
                                        .replace("Matrix", "Token")
                                + ")(/*JCGH44*/Array_get(");
                if (getCodeGenerator().isPrimitive(elementType)) {
                    result.insert(0, "(");
                }

                result.append(" ," + channelAndOffset[1] + ")");

                if (getCodeGenerator().isPrimitive(elementType)) {
                    String cgType = getCodeGenerator().codeGenType(elementType)
                            .toLowerCase(Locale.getDefault());
                    if (cgType.equals("integer")) {
                        cgType = "int";
                    }
                    String operator = "Value()";
                    if (cgType.equals("string")) {
                        cgType = "";
                        operator = "toString()";
                    }
                    result.append(".payload/*jcgh2*/))." + cgType + operator);
                } else {
                    result.append(")");
                }
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                        ////

    /** The port number. */
    protected int _portNumber = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _portVariableDeclaration(StringBuffer codeResult,
            TypedIOPort port) throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        String portType = targetType(port.getType());
        if (portType.equals("Object")
                && !(getCodeGenerator().getAdapter(port.getContainer()) instanceof AutoAdapter)
                && !AutoAdapter.isAutoAdapteredRemotePort(getCodeGenerator(),
                        port)) {
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ObjectToken1.xml
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ObjectTokenTestSampleDelay.xml
            System.out.println("CG SDFDirector: changing to Object to Token");
            portType = "Token";
        }
        boolean variablesAsArrays = ((BooleanToken) getCodeGenerator().variablesAsArrays
                .getToken()).booleanValue();
        if (!variablesAsArrays) {
            code.append("public static " + portType + " ");
        }
        code.append(generatePortName(port));

        int bufferSize = ports.getBufferSize(port);

        if (port.isMultiport()) {
            if (!variablesAsArrays) {
                code.append("[]");
                if (bufferSize > 1) {
                    code.append("[]");
                }
            }
            code.append(" = new " + portType);
        } else {
            if (bufferSize > 1) {
                if (!variablesAsArrays) {
                    code.append("[]");
                }
                code.append(" = new " + targetType(port.getType()));
            } else {
                //code.append(" = ");
            }
        }

        if (port.isMultiport()) {
            code.append("["
                    + java.lang.Math.max(port.getWidth(), port.getWidthInside())
                    + "]");
        }

        if (bufferSize > 1) {
            code.append("[" + bufferSize + "]");
        } else {
            //code.append("0");
        }
        code.append(";" + _eol);
        if (variablesAsArrays) {
            if (code.toString().indexOf("=") != -1) {
                codeResult.append(code);
            }
        } else {
            codeResult.append(code);
        }
    }

    /** Generate input variable declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateInputVariableDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> inputPorts = ((Actor) target.getComponent())
                .inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (!inputPort.isOutsideConnected()) {
                continue;
            }

            _portVariableDeclaration(code, inputPort);
        }

        return code.toString();
    }

    /** Generate output variable declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateOutputVariableDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> outputPorts = ((Actor) target.getComponent())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected()
                    || outputPort.isInsideConnected()) {
                _portVariableDeclaration(code, outputPort);
            }
        }

        return code.toString();
    }

    /** Generate referenced parameter declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares referenced parameters.
     *  @exception IllegalActionException If thrown while
     *  getting modified variable information.
     */
    private String _generateReferencedParameterDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (_referencedParameters.containsKey(target)) {

            for (Parameter parameter : _referencedParameters.get(target)) {
                // avoid duplicate declaration.
                if (!getCodeGenerator().getModifiedVariables().contains(
                        parameter)) {
                    code.append("public static "
                            + targetType(parameter.getType())
                            + " "
                            + getCodeGenerator()
                                    .generateVariableName(parameter) + ";"
                            + _eol);
                }
            }
        }

        return code.toString();
    }

    /** Generate type convert variable declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares type convert variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateTypeConvertVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> channels = target.getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
                    .next();
            Type portType = ((TypedIOPort) channel.port).getType();

            if (getCodeGenerator().isPrimitive(portType)) {

                code.append("public static ");
                code.append(targetType(portType));
                //getStrategy();
                code.append(" " + getTypeConvertReference(channel));

                //int bufferSize = getBufferSize(channel.port);
                int bufferSize = Math.max(
                        DFUtilities.getTokenProductionRate(channel.port),
                        DFUtilities.getTokenConsumptionRate(channel.port));

                if (bufferSize > 1) {
                    //code.append("[" + bufferSize + "]");
                    code.append("[] = new " + targetType(portType) + "["
                            + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }
        return code.toString();
    }
}
