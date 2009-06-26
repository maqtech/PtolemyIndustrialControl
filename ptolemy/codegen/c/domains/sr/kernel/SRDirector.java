/* Code generator helper class associated with the SRDirector class.

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
package ptolemy.codegen.c.domains.sr.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.jni.CompiledCompositeActor;
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.c.actor.sched.StaticSchedulingDirector;
import ptolemy.codegen.c.kernel.CCodegenUtilities;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////
////SRDirector

/**
 Code generator helper associated with the SRDirector class. This class
 is also associated with a code generator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class SRDirector extends StaticSchedulingDirector {

    /** Construct the code generator helper associated with the given
     *  SRDirector.
     *  @param SRDirector The associated
     *  ptolemy.domains.sr.kernel.SRDirector
     */
    public SRDirector(ptolemy.domains.sr.kernel.SRDirector SRDirector) {
        super(SRDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        _portNumber = 0;
        _intFlag = false;
        _doubleFlag = false;
        _booleanFlag = false;

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
        code.append(CodeStream.indent(_codeGenerator.comment("SRDirector: "
                + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);
        boolean targetCpp = ((BooleanToken) _codeGenerator.generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) _codeGenerator.generateEmbeddedCode
                        .getToken()).booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }
            Type type = ((TypedIOPort) inputPort).getType();
            String portName = generateSimpleName(inputPort);

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String tokensFromOneChannel = "tokensFromOneChannelOf"
                            + portName + i;
                    String pointerToTokensFromOneChannel = "pointerTo"
                            + tokensFromOneChannel;
                    code.append("jobject "
                            + tokensFromOneChannel
                            + " = "
                            + CCodegenUtilities.jniGetObjectArrayElement(
                                    portName, String.valueOf(i), targetCpp)
                            + ";" + _eol);

                    if (type == BaseType.INT) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Double", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Boolean", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                    }
                    String portNameWithChannelNumber = portName;
                    if (inputPort.isMultiport()) {
                        portNameWithChannelNumber = portName + '#' + i;
                    }
                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorHelper.getReference("@"
                                + portNameWithChannelNumber + "," + k));
                        if (type == PointerToken.POINTER) {
                            code.append(" = (void *) "
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "];" + _eol);
                        } else {
                            code.append(" = " + pointerToTokensFromOneChannel
                                    + "[" + k + "];" + _eol);
                        }
                    }

                    if (type == BaseType.INT) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Double", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Boolean", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                    }
                }
            }

        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = generateSimpleName(inputPort);

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorHelper.getReference("@"
                                + name + "," + k));
                        code.append(" = " + _eol);
                        code.append(compositeActorHelper.getReference(name
                                + "," + k));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorHelper.generateTypeConvertFireCode(true));

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
        code.append(CodeStream.indent(_codeGenerator.comment("SRDirector: "
                + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);
        boolean targetCpp = ((BooleanToken) _codeGenerator.generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) _codeGenerator.generateEmbeddedCode
                        .getToken()).booleanValue()) {

            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                code.append("jobjectArray tokensToAllOutputPorts;" + _eol);
                code.append("jclass "
                        + _objClass
                        + " = "
                        + CCodegenUtilities.jniFindClass("Ljava/lang/Object;",
                                targetCpp) + ";" + _eol);
                code.append("tokensToAllOutputPorts = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfOutputPorts), "objClass",
                                targetCpp) + ";" + _eol);
            }

            String portName = generateSimpleName(outputPort);
            String tokensToThisPort = "tokensTo" + portName;

            // FindBugs wants this instanceof check.
            if (!(outputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(outputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            Type type = ((TypedIOPort) outputPort).getType();

            int numberOfChannels = outputPort.getWidthInside();
            code.append("jobjectArray " + tokensToThisPort + ";" + _eol);

            // Find jni classes and methods and initialize the jni array
            // for the given type.
            if (type == BaseType.INT) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.DOUBLE) {
                if (!_doubleFlag) {
                    code.append("jclass " + _objClassD + " = "
                            + CCodegenUtilities.jniFindClass("[D", targetCpp)
                            + ";" + _eol);
                    _doubleFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassD,
                                targetCpp) + ";" + _eol);
            } else if (type == PointerToken.POINTER) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.BOOLEAN) {
                if (!_booleanFlag) {
                    code.append("jclass objClassZ = "
                            + CCodegenUtilities.jniFindClass("[Z", targetCpp)
                            + ";" + _eol);
                    _booleanFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(String
                                .valueOf(numberOfChannels), "objClassZ",
                                targetCpp) + ";" + _eol);
            } else {
                // FIXME: need to deal with other types
            }

            // Create an array to contain jni objects
            for (int i = 0; i < outputPort.getWidthInside(); i++) {

                String tokensToOneChannel = "tokensToOneChannelOf" + portName;
                if (i == 0) {
                    if (type == BaseType.INT) {
                        code.append("jint " + tokensToOneChannel + "[" + rate
                                + "];" + _eol);

                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble " + tokensToOneChannel + "["
                                + rate + "];" + _eol);

                    } else if (type == PointerToken.POINTER) {
                        code.append("jint " + tokensToOneChannel + "[" + rate
                                + "];" + _eol);

                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean " + tokensToOneChannel + "["
                                + rate + "];" + _eol);

                    } else {
                        // FIXME: need to deal with other types
                    }
                }

                String portNameWithChannelNumber = portName;
                if (outputPort.isMultiport()) {
                    portNameWithChannelNumber = portName + '#' + i;
                }

                // Assign each token to the array of jni objects
                for (int k = 0; k < rate; k++) {
                    String portReference = compositeActorHelper
                            .getReference("@" + portNameWithChannelNumber + ","
                                    + k);
                    if (type == PointerToken.POINTER) {
                        code.append(tokensToOneChannel + "[" + k + "] = "
                                + "(int) " + portReference + ";" + _eol);
                    } else {
                        code.append(tokensToOneChannel + "[" + k + "] = "
                                + portReference + ";" + _eol);
                    }
                }

                String tokensToOneChannelArray = "arr" + portName + i;
                // Create and fill an array of Java objects.
                if (type == BaseType.INT) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == BaseType.DOUBLE) {
                    code.append("jdoubleArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Double", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Double",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == PointerToken.POINTER) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == BaseType.BOOLEAN) {
                    code.append("jbooleanArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Boolean", String
                                    .valueOf(rate), targetCpp) + ";" + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Boolean",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);
                } else {
                    // FIXME: need to deal with other types
                }

                code.append(CCodegenUtilities.jniSetObjectArrayElement(
                        tokensToThisPort, String.valueOf(i),
                        tokensToOneChannelArray, targetCpp)
                        + ";" + _eol);
                code.append(CCodegenUtilities.jniDeleteLocalRef(
                        tokensToOneChannelArray, targetCpp)
                        + ";" + _eol);
            }

            code.append(CCodegenUtilities.jniSetObjectArrayElement(
                    "tokensToAllOutputPorts", String.valueOf(_portNumber),
                    tokensToThisPort, targetCpp)
                    + ";" + _eol);
            code.append(CCodegenUtilities.jniDeleteLocalRef(tokensToThisPort,
                    targetCpp)
                    + ";" + _eol);
            _portNumber++;

        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = generateSimpleName(outputPort);

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorHelper
                                .getReference(name + "," + k)));
                        code.append(" =" + _eol);
                        code.append(CodeStream.indent(compositeActorHelper
                                .getReference("@" + name + "," + k)));
                        code.append(";" + _eol);
                    }
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
        return 1;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    private int _portNumber = 0;

    private boolean _doubleFlag;

    private boolean _booleanFlag;

    private boolean _intFlag;

    /** Name of variable containing jni class for Objects. */
    private String _objClass = "objClass";

    /** Name of variable containing jni double class. */
    private String _objClassD = "objClassD";

    /** Name of variable containing jni int class. */
    private String _objClassI = "objClassI";

}
