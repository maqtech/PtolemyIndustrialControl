/* Code generator helper class associated with the SDFDirector class.

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
package ptolemy.codegen.c.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Code generator helper associated with the SDFDirector class. This class
 is also associated with a code generator.
 FIXME: Should associated with a static scheduling code generator.

 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (cxh) Cleanup, then review.
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate code for declaring read and write offset variables if needed. 
     * 
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating
     *  offset variables.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_createOffsetVariablesIfNeeded());
        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }

    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated helper.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        Schedule schedule = ((StaticSchedulingDirector) getComponent())
                .getScheduler().getSchedule();

        boolean isIDefined = false;
        Iterator actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            // FIXME: Before looking for a helper class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the helper.
            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            if (inline) {
                for (int i = 0; i < firing.getIterationCount(); i++) {

                    // generate fire code for the actor
                    code.append(helper.generateFireCode());
                    code.append(helper.generateTypeConvertFireCode());

                    // update buffer offset after firing each actor once
                    Iterator inputPorts = actor.inputPortList().iterator();
                    while (inputPorts.hasNext()) {
                        IOPort port = (IOPort) inputPorts.next();
                        int rate = DFUtilities.getRate(port);
                        _updatePortOffset(port, code, rate);
                    }

                    Iterator outputPorts = actor.outputPortList().iterator();
                    while (outputPorts.hasNext()) {
                        IOPort port = (IOPort) outputPorts.next();
                        int rate = DFUtilities.getRate(port);
                        _updateConnectedPortsOffset(port, code, rate);
                    }
                }
            } else {

                int count = firing.getIterationCount();
                if (count > 1) {
                    if (!isIDefined) {
                        code.append("int i;\n");
                        isIDefined = true;
                    }
                    code.append("for (i = 0; i < " + count + " ; i++) {\n");
                }

                code.append(CodeGeneratorHelper.generateName((NamedObj) actor)
                        + "();\n");

                // update buffer offset after firing each actor once
                Iterator inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort port = (IOPort) inputPorts.next();
                    int rate = DFUtilities.getRate(port);
                    _updatePortOffset(port, code, rate);
                }

                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort port = (IOPort) outputPorts.next();
                    int rate = DFUtilities.getRate(port);
                    _updateConnectedPortsOffset(port, code, rate);
                }

                if (count > 1) {
                    code.append("}\n");
                }
            }
        }
        return code.toString();
    }

    /** Generate the initialize code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
                .getContainer();
        CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);

        // Generate code for creating external initial production.
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
                            code.append(containerHelper.getReference(name + ","
                                    + k));
                            code.append(" = ");
                            code.append(containerHelper.getReference("@" + name
                                    + "," + k));
                            code.append(";\n");
                        }
                    }
                }

                // The offset of the ports connected to the output port is 
                // updated by outside director.
                _updatePortOffset(outputPort, code, rate);
            }
        }
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
        code.append(_codeGenerator.comment(2, "SDFDirector: "
                + "Transfer tokens to the inside."));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(getComponent()
                .getContainer());

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = inputPort.getName();

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    code.append(_INDENT2
                            + compositeActorHelper.getReference("@" + name
                                    + "," + k));
                    code.append(" =\n");
                    code
                            .append(_INDENT4
                                    + compositeActorHelper.getReference(name
                                            + "," + k));
                    code.append(";\n");
                }
            }
        }

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
        code.append(_codeGenerator.comment(2, "SDFDirector: "
                + "Transfer tokens to the outside."));

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(getComponent()
                .getContainer());

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    code
                            .append(_INDENT2
                                    + compositeActorHelper.getReference(name
                                            + "," + k));
                    code.append(" =\n");
                    code.append(_INDENT4
                            + compositeActorHelper.getReference("@" + name
                                    + "," + k));
                    code.append(";\n");
                }
            }
        }

        // The offset of the ports connected to the output port is 
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number). The default value is 1. If the
     *  port is an output port, then the buffer size is obtained
     *  from the inside receiver. If it is an input port, then it
     *  is obtained from the specified port.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel.
     *  @exception IllegalActionException If the channel number is
     *   out of range or if the port is neither an input nor an
     *   output.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        Receiver[][] receivers = null;

        if (port.isInput()) {
            receivers = port.getReceivers();
        } else if (port.isOutput()) {
            receivers = port.getInsideReceivers();
        }
        // else {
        //    throw new IllegalActionException(port,
        //           "Port is neither an input nor an output.");
        //}

        //try {
        int size = 0;

        for (int copy = 0; copy < receivers[channelNumber].length; copy++) {
            int copySize = ((SDFReceiver) receivers[channelNumber][copy])
                    .getCapacity();

            if (copySize > size) {
                size = copySize;
            }

            // When an output port of a composite actor is directly
            // connected to an input port of the same composite actor,
            // calling getCapacity() will return 0. Therefore we use
            // the rate to determine the buffer size.
            if (port.isOutput()) {
                copySize = DFUtilities.getRate(port);
                if (copySize > size) {
                    size = copySize;
                }
            }
        }

        return size;
        //} 
        //catch (ArrayIndexOutOfBoundsException ex) {
        //    throw new IllegalActionException(port, "Channel out of bounds: "
        //            + channelNumber);
        //}
    }

    /** Check to see if variables are needed to represent read and
     *  write offsets for the given port.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.   
     */
    protected String _createOffsetVariablesIfNeeded()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();

        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        Iterator outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                int readTokens = 0;
                int writeTokens = 0;
                // If each actor firing is inlined in the code, then read
                // and write positions in the buffer must return to the 
                // previous values after one iteration of the container actor
                // in order to avoid using read and write offset variables.
                if (inline) {
                    readTokens = DFUtilities.getRate(outputPort);
                    writeTokens = readTokens;
                    // If each actor firing is wrapped in a function, then read
                    // and write positions in the buffer must return to the 
                    // previous values after one firing of this actor or one
                    // firing of the actor that produces tokens consumed by the 
                    // inside receiver of this actor in order to avoid using 
                    // read and write offset variables.
                } else {
                    readTokens = DFUtilities.getRate(outputPort);
                    Iterator sourcePorts = outputPort.insideSourcePortList()
                            .iterator();
                    label1: while (sourcePorts.hasNext()) {
                        IOPort sourcePort = (IOPort) sourcePorts.next();
                        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(sourcePort
                                .getContainer());
                        int width;
                        if (sourcePort.isInput()) {
                            width = sourcePort.getWidthInside();
                        } else {
                            width = sourcePort.getWidth();
                        }
                        for (int j = 0; j < width; j++) {
                            Iterator channels = helper.getSinkChannels(
                                    sourcePort, j).iterator();
                            while (channels.hasNext()) {
                                Channel channel = (Channel) channels.next();
                                if (channel.port == outputPort
                                        && channel.channelNumber == i) {
                                    writeTokens = DFUtilities
                                            .getRate(sourcePort);
                                    break label1;
                                }
                            }
                        }
                    }
                }
                code.append(_createOffsetVariablesIfNeeded(outputPort, i,
                        readTokens, writeTokens));
            }
        }

        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int i = 0; i < inputPort.getWidth(); i++) {
                    int readTokens = 0;
                    int writeTokens = 0;
                    // If each actor firing is inlined in the code, then read
                    // and write positions in the buffer must return to the 
                    // previous values after one iteration of the container actor
                    // in order to avoid using read and write offset variables.
                    if (inline) {
                        Variable firings = (Variable) ((NamedObj) actor)
                                .getAttribute("firingsPerIteration");
                        int firingsPerIteration = ((IntToken) firings
                                .getToken()).intValue();
                        readTokens = DFUtilities.getRate(inputPort)
                                * firingsPerIteration;
                        writeTokens = readTokens;
                        // If each actor firing is wrapped in a function, then read
                        // and write positions in the buffer must return to the 
                        // previous values after one firing of this actor or one
                        // firing of the actor that produces tokens consumed by 
                        // this actor in order to avoid using read and write offset 
                        // variables.
                    } else {
                        readTokens = DFUtilities.getRate(inputPort);
                        Iterator sourcePorts = inputPort.sourcePortList()
                                .iterator();
                        label2: while (sourcePorts.hasNext()) {
                            IOPort sourcePort = (IOPort) sourcePorts.next();
                            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(sourcePort
                                    .getContainer());
                            int width;
                            if (sourcePort.isInput()) {
                                width = sourcePort.getWidthInside();
                            } else {
                                width = sourcePort.getWidth();
                            }
                            for (int j = 0; j < width; j++) {
                                Iterator channels = helper.getSinkChannels(
                                        sourcePort, j).iterator();
                                while (channels.hasNext()) {
                                    Channel channel = (Channel) channels.next();
                                    if (channel.port == inputPort
                                            && channel.channelNumber == i) {
                                        writeTokens = DFUtilities
                                                .getRate(sourcePort);
                                        break label2;
                                    }
                                }
                            }
                        }
                    }
                    code.append(_createOffsetVariablesIfNeeded(inputPort, i,
                            readTokens, writeTokens));
                }
            }
        }
        return code.toString();
    }

    /** Check for the given channel of the given port to see if
     *  variables are needed for recording read offset and write
     *  offset. If the buffer size of a channel divides the readTokens
     *  and writeTokens given in the argument, then there is no need
     *  for the variables. Otherwise the integer offsets are replaced
     *  with variables and the code to initialize these variables are
     *  generated.
     *
     *  @param port The port to be checked.
     *  @param channelNumber The channel number.
     *  @param readTokens The number of tokens read.
     *  @param writeTokens The number of tokens written.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.   
     */
    protected String _createOffsetVariablesIfNeeded(IOPort port,
            int channelNumber, int readTokens, int writeTokens)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int bufferSize = helper.getBufferSize(port, channelNumber);
        if (readTokens % bufferSize != 0 || writeTokens % bufferSize != 0) {

            // Increase the buffer size of that channel to the power of two.
            int newBufferSize = _ceilToPowerOfTwo(bufferSize);
            helper.setBufferSize(port, channelNumber, newBufferSize);

            int width;
            if (port.isInput()) {
                width = port.getWidth();
            } else {
                width = port.getWidthInside();
            }

            // We check again if the new bufferSize divides readTokens or 
            // writeTokens. If yes, we could avoid using variable to represent 
            // offset.
            if (readTokens % newBufferSize != 0) {

                // Declare the read offset variable.
                StringBuffer channelReadOffset = new StringBuffer();
                channelReadOffset
                        .append(CodeGeneratorHelper.generateName(port));
                if (width > 1) {
                    channelReadOffset.append("_" + channelNumber);
                }
                channelReadOffset.append("_readoffset");
                String channelReadOffsetVariable = channelReadOffset.toString();
                code.append("static int " + channelReadOffsetVariable + " = "
                        + helper.getReadOffset(port, channelNumber) + ";\n");
                // Now replace the concrete offset with the variable.
                helper.setReadOffset(port, channelNumber,
                        channelReadOffsetVariable);
            }

            if (writeTokens % newBufferSize != 0) {

                // Declare the write offset variable.
                StringBuffer channelWriteOffset = new StringBuffer();
                channelWriteOffset.append(CodeGeneratorHelper
                        .generateName(port));
                if (width > 1) {
                    channelWriteOffset.append("_" + channelNumber);
                }
                channelWriteOffset.append("_writeoffset");
                String channelWriteOffsetVariable = channelWriteOffset
                        .toString();
                code.append("static int " + channelWriteOffsetVariable + " = "
                        + helper.getWriteOffset(port, channelNumber) + ";\n");
                // Now replace the concrete offset with the variable.
                helper.setWriteOffset(port, channelNumber,
                        channelWriteOffsetVariable);
            }
        }
        return code.toString();
    }
}
