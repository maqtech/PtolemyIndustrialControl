/* Code generator adapter class associated with the SDFDirector class.

 Copyright (c)2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////
////SDFDirector

/**
Code generator adapter associated with the SDFDirector class. This class
is also associated with a code generator.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 7.2
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/

public class SDFDirector extends StaticSchedulingDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the initialize code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
                .getContainer();
        CodeGeneratorAdapter containerAdapter = getCodeGenerator().getAdapter(container);

        // Generate code for creating external initial production.
        Iterator<?> outputPorts = container.outputPortList().iterator();
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
                            code.append(containerAdapter
                                    .getReference(name + "," + k));
                            code.append(" = ");
                            code.append(containerAdapter.getReference("@" + name
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
        code.append(super.generatePreinitializeCode());

        code.append(_createOffsetVariablesIfNeeded());
        
        return code.toString();
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
        Receiver[][] receivers;

        if (port.isInput()) {
            receivers = port.getReceivers();
        } else if (port.isOutput()) {

            receivers = port.getInsideReceivers();
        } else {
            // Findbugs: receivers could be null, so we throw an exception.
            throw new IllegalActionException(port,
                    "Port is neither an input nor an output.");
        }

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

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Create offset variables for the channels of the given port.
     *  The offset variables are generated unconditionally, but this
     *  method is only called when dynamic multiport references are
     *  desired.  The buffers should be padded before this method is
     *  called.
     *
     *  @param port The port whose offset variables are generated.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createDynamicOffsetVariables(IOPort port)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(port
                .getContainer());

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = port.getWidthInside();
        }

        if (width != 0) {
            // Declare the read offset variable.
            String channelReadOffset = CodeGeneratorAdapterStrategy.generateName(port);
            channelReadOffset += "_readOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                adapter
                        .setReadOffset(port, i, channelReadOffset + "[" + i
                                + "]");
            }
            channelReadOffset += "[" + width + "]";
            code.append("static int " + channelReadOffset + ";\n");

            // Declare the write offset variable.
            String channelWriteOffset = CodeGeneratorAdapterStrategy.generateName(port);

            channelWriteOffset += "_writeOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                adapter.setWriteOffset(port, i, channelWriteOffset + "[" + i
                        + "]");
            }
            channelWriteOffset += "[" + width + "]";
            code.append("static int " + channelWriteOffset + ";\n");
        }
        return code.toString();
    }

    /** Check for the given channel of the given port to see if
     *  variables are needed for recording read offset and write
     *  offset. If the buffer size of a channel divides the readTokens
     *  and writeTokens given in the argument, then there is no need
     *  for the variables. Otherwise the integer offsets are replaced
     *  with variables and the code to initialize these variables are
     *  generated.  If padded buffers are desired (based on the padBuffers
     *  parameter of the CodeGenerator), pad the buffers.
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

        CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(port
                .getContainer());
        boolean padBuffers = ((BooleanToken) getCodeGenerator().padBuffers
                .getToken()).booleanValue();

        int bufferSize = adapter.getBufferSize(port, channelNumber);

        // Increase the buffer size of that channel to the power of two.
        if (bufferSize > 0 && padBuffers) {
            bufferSize = _padBuffer(port, channelNumber);
        }

        if (bufferSize != 0
                && (readTokens % bufferSize != 0 || writeTokens % bufferSize != 0)) {
            int width;
            if (port.isInput()) {
                width = port.getWidth();
            } else {
                width = port.getWidthInside();
            }

            // We check again if the new bufferSize divides readTokens or
            // writeTokens. If yes, we could avoid using variable to represent
            // offset.
            if (readTokens % bufferSize != 0) {

                // Declare the read offset variable.
                StringBuffer channelReadOffset = new StringBuffer();
                channelReadOffset
                        .append(CodeGeneratorAdapterStrategy.generateName(port));
                if (width > 1) {
                    channelReadOffset.append("_" + channelNumber);
                }
                channelReadOffset.append("_readOffset");
                String channelReadOffsetVariable = channelReadOffset.toString();
                //code.append("static int " + channelReadOffsetVariable + " = "
                //        + adapter.getReadOffset(port, channelNumber) + ";\n");
                code.append("static int " + channelReadOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                adapter.setReadOffset(port, channelNumber,
                        channelReadOffsetVariable);
            }

            if (writeTokens % bufferSize != 0) {

                // Declare the write offset variable.
                StringBuffer channelWriteOffset = new StringBuffer();
                channelWriteOffset.append(CodeGeneratorAdapterStrategy
                        .generateName(port));
                if (width > 1) {
                    channelWriteOffset.append("_" + channelNumber);
                }
                channelWriteOffset.append("_writeOffset");
                String channelWriteOffsetVariable = channelWriteOffset
                        .toString();
                code.append("static int " + channelWriteOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                adapter.setWriteOffset(port, channelNumber,
                        channelWriteOffsetVariable);
            }
        }
        return code.toString();
    }

    /** Check to see if the buffer size for the current schedule is greater
     *  than the previous size. If so, set the buffer size to the current
     *  buffer size needed.
     *  @exception IllegalActionException If thrown while getting adapter
     *   or buffer size.
     */
    protected void _updatePortBufferSize() throws IllegalActionException {

        ptolemy.domains.sdf.kernel.SDFDirector director = (ptolemy.domains.sdf.kernel.SDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        TypedCompositeActor containerAdapter = (TypedCompositeActor) getCodeGenerator().getAdapter(container);

        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorAdapter actorAdapter = getCodeGenerator().getAdapter((NamedObj) actor);
            Iterator<?> inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int k = 0; k < inputPort.getWidth(); k++) {
                    int newCapacity = getBufferSize(inputPort, k);
                    int oldCapacity = actorAdapter.getBufferSize(inputPort, k);
                    if (newCapacity > oldCapacity) {
                        actorAdapter.setBufferSize(inputPort, k, newCapacity);
                    }
                }
            }
        }

        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int k = 0; k < outputPort.getWidthInside(); k++) {
                int newCapacity = getBufferSize(outputPort, k);
                int oldCapacity = containerAdapter.getBufferSize(outputPort, k);
                if (newCapacity > oldCapacity) {
                    containerAdapter.setBufferSize(outputPort, k, newCapacity);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////


    /** Check to see if variables are needed to represent read and
     *  write offsets for the ports in the director's container.  If
     *  variables are needed, create them.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    private String _createOffsetVariablesIfNeeded()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();

        boolean inline = ((BooleanToken) getCodeGenerator().inline.getToken())
                .booleanValue();

        StringBuffer tempCode = new StringBuffer();
        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {

            IOPort outputPort = (IOPort) outputPorts.next();
            // If dynamic references are desired, conditionally pad buffers
            // and append the dynamic offset variables for output ports.
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
                    Iterator<?> sourcePorts = outputPort
                            .insideSourcePortList().iterator();
                    label1: while (sourcePorts.hasNext()) {
                        IOPort sourcePort = (IOPort) sourcePorts.next();
//                            CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(sourcePort
//                                    .getContainer());
                        int width;
                        if (sourcePort.isInput()) {
                            width = sourcePort.getWidthInside();
                        } else {
                            width = sourcePort.getWidth();
                        }
                        for (int j = 0; j < width; j++) {
                            Iterator<?> channels = CodeGeneratorAdapterStrategy.getSinkChannels(
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
                tempCode.append(_createOffsetVariablesIfNeeded(outputPort,
                        i, readTokens, writeTokens));
            }
        }
        if (tempCode.length() > 0) {
            code.append("\n"
                    + getCodeGenerator().comment(container.getName()
                            + "'s offset variables"));
            code.append(tempCode);
        }

        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            StringBuffer tempCode2 = new StringBuffer();
            Actor actor = (Actor) actors.next();
            Iterator<?> inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                // If dynamic references are desired, conditionally pad buffers
                // and append the dynamic offset variables for input ports.

                for (int i = 0; i < inputPort.getWidth(); i++) {
                    int readTokens = 0;
                    int writeTokens = 0;
                    // If each actor firing is inlined in the code,
                    // then read and write positions in the buffer
                    // must return to the previous values after one
                    // iteration of the container actor in order to
                    // avoid using read and write offset variables.
                    if (inline) {
                        Variable firings = (Variable) ((NamedObj) actor)
                                .getAttribute("firingsPerIteration");
                        int firingsPerIteration = ((IntToken) firings
                                .getToken()).intValue();
                        readTokens = DFUtilities.getRate(inputPort)
                                * firingsPerIteration;
                        writeTokens = readTokens;

                        // If each actor firing is wrapped in a
                        // function, then read and write positions in
                        // the buffer must return to the previous
                        // values after one firing of this actor or
                        // one firing of the actor that produces
                        // tokens consumed by this actor in order to
                        // avoid using read and write offset
                        // variables.
                    } else {
                        readTokens = DFUtilities.getRate(inputPort);
                        Iterator<?> sourcePorts = inputPort.sourcePortList()
                                .iterator();
                        label2: while (sourcePorts.hasNext()) {
                            IOPort sourcePort = (IOPort) sourcePorts.next();
//                                CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(sourcePort
//                                        .getContainer());
                            int width;
                            if (sourcePort.isInput()) {
                                width = sourcePort.getWidthInside();
                            } else {
                                width = sourcePort.getWidth();
                            }
                            for (int j = 0; j < width; j++) {
                                Iterator<?> channels = CodeGeneratorAdapterStrategy.getSinkChannels(
                                        sourcePort, j).iterator();
                                while (channels.hasNext()) {
                                    Channel channel = (Channel) channels
                                            .next();
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
                    tempCode2.append(_createOffsetVariablesIfNeeded(
                            inputPort, i, readTokens, writeTokens));
                }
            }
            if (tempCode2.length() > 0) {
                code.append("\n"
                        + getCodeGenerator().comment(actor.getName()
                                + "'s offset variables"));
                code.append(tempCode2);
            }
        }
        return code.toString();
    }

    /** Pad the buffer for the channel of the given port with the given
     *  channel number to a power of two.  Return the new buffer size.
     * @param port The port which has the buffer to pad.
     * @param channelNumber The number of the channel which has the buffer
     * to pad.
     * @return The size of the new buffer.
     * @exception IllegalActionException If thrown when getting the port's adapter.
     */
    private int _padBuffer(IOPort port, int channelNumber)
            throws IllegalActionException {
        CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(port
                .getContainer());

        int bufferSize = adapter.getBufferSize(port, channelNumber);
        int newBufferSize = _ceilToPowerOfTwo(bufferSize);
        adapter.setBufferSize(port, channelNumber, newBufferSize);

        return newBufferSize;
    }
}
