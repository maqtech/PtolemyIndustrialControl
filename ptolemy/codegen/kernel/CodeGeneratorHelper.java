/* Base class for code generator helper.

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
package ptolemy.codegen.kernel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CodeGeneratorHelper

/**
 * Base class for code generator helper.
 *
 * <p>Subclasses should override generateFireCode(),
 * generateInitializeCode(), generatePreinitializeCode(), and
 * generateWrapupCode() methods by appending a corresponding code
 * block.
 *
 * @author Ye Zhou, Gang Zhou, Edward A. Lee, Contributors: Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGeneratorHelper implements ActorCodeGenerator {
    /** Construct the code generator helper associated
     *  with the given component.
     *  @param component The associated component.
     */
    public CodeGeneratorHelper(NamedObj component) {
        _component = component;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for declaring read and write offset variables if needed.
     *  Return empty string in this base class. 
     * 
     *  @return The empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. Subclasses may extend this
     * method to generate the fire code of the associated component.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        return "\n/* fire " + getComponent().getName() + " */\n";
    }
    
    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a 
     *  function with the same name as that of the actor.
     * 
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("\nvoid " + getComponent().getFullName().replace('.', '_')
                + "() {\n");
        code.append(generateFireCode());
        code.append(generateTypeConvertFireCode());
        code.append("}\n");
        return code.toString();
    }

    /**
     * Generate the initialize code. In this base class, return empty
     * string. Subclasses may extend this method to generate initialize 
     * code of the associated component and append the code to the 
     * given string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return "\n/* initialize " + getComponent().getName() + " */\n";      
    }

    /** Generate mode transition code. The mode transition code generated in 
     *  this method is executed after each global iteration, e.g., in HDF model. 
     *  Do nothing in this base class.
     * 
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the helper.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        _createBufferSizeAndOffsetMap();
        return "\n/* preinitialize " + getComponent().getName() + " */\n";
    }

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this helper should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set generateSharedCode() throws IllegalActionException {
        return new HashSet();
    }

    /**
     * Generate the type conversion fire code. This method is called by the 
     * Director to append necessary fire code to handle type conversion.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Type conversion code for inter-actor port conversion. 
        Object[] ports = _portConversions.keySet().toArray();
        for (int i = 0; i < ports.length; i++) {
            String portName = (String) ports[i];
            String convert = (String) _portConversions.get(portName);
            String type = (String) _portDeclareTypes.get(portName);

            if (type != null && type.equals("char*")
                    && convert.indexOf("new") == -1) {
                code.append("\t" + _getReference(portName) + " = " + convert
                        + "(" + getReference(portName) + ");\n");
            }
        }
        return code.toString();
    }

    /**
     * Generate the type conversion initialize code. This method is called 
     * by the Director to append necessary initialize code to handle type
     * conversion.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertInitializeCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Type conversion code for inter-actor port conversion.         
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            // See if source port(s) need to be converted.
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            List sourcePorts = inputPort.sourcePortList();

            for (int i = 0; i < sourcePorts.size(); i++) {
                TypedIOPort sourcePort = (TypedIOPort) sourcePorts.get(i);
                if (inputPort.getType() == sourcePort.getType() ||
                        inputPort.getType() != BaseType.GENERAL) {
                    continue;
                }
                // FIXME: 1. inputPort.sourcePortList() returns a list 
                // of source ports. The API does not say the 1st source
                // port in the list connects to the 1st channel of input 
                // port, the 2nd to the 2nd, etc. _getChannelIndex(inputPort, j,
                // sourcePort) uses this assumption which is not guaranteed.
                // 2. It does not consider the case that the same channel
                // of the input port may be connected to more than one source
                // port, e.g., in modal model.
                String sourcePortName = sourcePort.getName() + "#"
                        + _getChannelIndex(inputPort, i, sourcePort);
                CodeGeneratorHelper sourceHelper = (CodeGeneratorHelper) _getHelper(sourcePort
                        .getContainer());

                String type = (String) sourceHelper._portDeclareTypes
                        .get(sourcePortName);
                String convert = (String) sourceHelper._portConversions
                        .get(sourcePortName);

                // if no given type declaration, then use the type of the port.
                if (type == null) {

                } else if (type.equals("Token")) {
                    // Case: upgrade from primitive to Token.
                    // We need to create the Token object.                  
                    // The type should be a primitive type (less than String).
                    if (_isPrimitiveType(convert.substring(0, convert
                            .indexOf("_new")))) {
                        for (int j = 0; j < getBufferSize(inputPort, i); j++) {
                            code.append("\t"
                                    + _getReference(inputPort.getName() + "#"
                                            + i + ", " + j) + " = " + convert
                                    + "(0);\n");
                        }
                    }
                }
            }
        }
        return processCode(code.toString());
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("\n/* " + _component.getName()
                + "'s variable declarations. */\n");

        //  Generate variable declarations for referenced parameters.    
        if (_referencedParameters != null) {
            Iterator parameters = _referencedParameters.iterator();

            while (parameters.hasNext()) {
                Parameter parameter = (Parameter) parameters.next();

                // avoid duplicate declaration.
                if (!_codeGenerator._modifiedVariables.contains(parameter)) {
                    code.append("static " + _generateType(parameter.getType())
                            + " " + generateVariableName(parameter) 
                            + ";\n");
                }
            }
        }

        // Generate variable declarations for input ports.
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (inputPort.getWidth() == 0) {
                break;
            }

            String cType = _generateType(inputPort.getType());

            code.append("static " + cType + " ");
            code.append(inputPort.getFullName().replace('.', '_'));

            if (inputPort.isMultiport()) {
                code.append("[" + inputPort.getWidth() + "]");
            }

            int bufferSize = getBufferSize(inputPort);

            if (bufferSize > 1) {
                code.append("[" + bufferSize + "]");
            }

            code.append(";\n");
            _generateTypeConvertVariableDeclaration(inputPort, code);
        }

        // Generate variable declarations for output ports.
        Iterator outputPorts = ((Actor) _component).outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if ((outputPort.getWidth() == 0)
                    || (outputPort.getWidthInside() != 0)) {
                code.append("static " + _generateType(outputPort.getType()) + " ");
                code.append(outputPort.getFullName().replace('.', '_'));

                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }

                int bufferSize = getBufferSize(outputPort);

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";\n");
                _generateTypeConvertVariableDeclaration(outputPort, code);
            }
        }
        return processCode(code.toString());
    }
    
    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("\n/* " + _component.getName()
                + "'s variable initialization. */\n");

        //  Generate variable initialization for referenced parameters.    
        if (_referencedParameters != null) {
            Iterator parameters = _referencedParameters.iterator();

            while (parameters.hasNext()) {
                Parameter parameter = (Parameter) parameters.next();

                // avoid duplication.
                if (!_codeGenerator._modifiedVariables.contains(parameter)) {
                    code.append(generateVariableName(parameter)
                            + " = " 
                            + getParameterValue(parameter.getName(), _component)
                            + ";\n");
                }
            }
        }
        return code.toString();
    }    
    
    public static String generateVariableName(NamedObj namedObj) {
        return namedObj.getFullName().replace('.', '_') + "_"; 
    }

    /**
     * Generate the wrapup code. In this base class, do nothing. Subclasses
     * may extend this method to generate the wrapup code of the associated
     * component and append the code to the given string buffer.
     * @return The generated wrapup code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return "";
    }

    /**
     * Return the buffer size of a given port, which is the maximum of
     * the bufferSizes of all channels of the given port.
     * @param port The given port.
     * @return The buffer size of the given port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(IOPort, int)} method throws it.
     */
    public int getBufferSize(IOPort port) throws IllegalActionException {
        int bufferSize = 1;

        if (port.getContainer() == _component) {
            int length = 0;

            if (port.isInput()) {
                length = port.getWidth();
            } else {
                length = port.getWidthInside();
            }

            for (int i = 0; i < length; i++) {
                int channelBufferSize = getBufferSize(port, i);

                if (channelBufferSize > bufferSize) {
                    bufferSize = channelBufferSize;
                }
            }
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
                    .getContainer());
            bufferSize = actorHelper.getBufferSize(port);
        }

        return bufferSize;
    }

    /** Get the buffer size of the given port of this actor.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @return The buffer size of the given port and channel.
     *  @exception IllegalActionException If the getBufferSize()
     *   method of the actor helper class throws it.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        if (port.getContainer() == _component) {
            return ((int[]) _bufferSizes.get(port))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
                    .getContainer());
            return actorHelper.getBufferSize(port, channelNumber);
        }
    }

    /** Get the component associated with this helper.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        return files;
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The actor gets those variables if it implements 
     *  ExplicitChangeContext interface or it contains PortParameters. 
     * 
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If an actor throws it while getting 
     *   modified variables. 
     */
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();
        if (_component instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _component)
                    .getModifiedVariables());
        }
        
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator(); 
        while(inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort && inputPort.getWidth() > 0) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }    
        return set;
    }

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this helper. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
     */
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        constructorString = processCode(constructorString);

        // i.e. "$new(Array(8, 8, arg1, arg2, ...))"
        // this transforms to ==> 
        // "Array_new(8, arg1, arg2, ...)"
        int openFuncParenIndex = constructorString.indexOf('(');
        int closeFuncParenIndex = constructorString.lastIndexOf(')');

        // Syntax checking.
        if ((openFuncParenIndex == -1)
                || (closeFuncParenIndex != (constructorString.length() - 1))) {
            throw new IllegalActionException(
                    "Bad Syntax with the $new() macro. "
                            + "[i.e. -- $new(Array(8, 8, arg1, arg2, ...))]");
        }

        String typeName = constructorString.substring(0, openFuncParenIndex)
                .trim();

        // Record the referenced type function in the infoTable.
        _newTypesUsed.add(typeName);

        return typeName + "_new"
                + constructorString.substring(openFuncParenIndex);
    }

    /** Return the value or an expression in the target language for the specified
     *  parameter of the associated actor.
     *  If the parameter is specified by an expression, then the expression will
     *  be parsed. If any parameter referenced in that expression is specified
     *  by another expression, the parsing continues recursively until either a 
     *  parameter is directly specified by a constant or a parameter can be 
     *  directly modified during execution in which case a reference to the 
     *  parameter is generated.
     *   
     *  @param name The name of the parameter.
     *  @param container The container to search upwards from.
     *  @return The value or expression as a string.
     *  @exception IllegalActionException If the parameter does not exist or
     *   does not have a value.
     */
    public String getParameterValue(String name, NamedObj container)
            throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, ",");

        String attributeName = tokenizer.nextToken().trim();
        String offset = null;

        if (tokenizer.hasMoreTokens()) {
            offset = tokenizer.nextToken().trim();

            if (tokenizer.hasMoreTokens()) {
                throw new IllegalActionException(_component, name
                        + " does not have the correct format for"
                        + " accessing the parameter value.");
            }
        }

        Attribute attribute = ModelScope.getScopedVariable(null, container,
                attributeName);

        if (attribute == null) {
            throw new IllegalActionException(container, "No attribute named: "
                    + name);
        }

        if (offset == null) {
            if (attribute instanceof Variable) {
                // FIXME: need to ensure that the returned string
                // is correct syntax for the target language.
                Variable variable = (Variable) attribute;

                /*
                if (_codeGenerator._modifiedVariables.contains(variable)) {
                    return generateVariableName(variable);
                } else if (variable.isStringMode()) {
                    return "\"" + variable.getExpression() + "\"";
                }
                */

                if (variable.isStringMode()) {
                    return "\"" + variable.getExpression() + "\"";
                }
                
                PtParser parser = new PtParser();
                ASTPtRootNode parseTree = parser.generateParseTree(variable
                        .getExpression());
                ParseTreeCodeGenerator parseTreeCodeGenerator = new ParseTreeCodeGenerator();
                parseTreeCodeGenerator.evaluateParseTree(parseTree,
                        new HelperScope(variable));
                return processCode(parseTreeCodeGenerator.generateFireCode());
            } else if (attribute instanceof Settable) {
                return ((Settable) attribute).getExpression();
            }

            // FIXME: Are there any other values that a
            // parameter might have?
            throw new IllegalActionException(_component,
                    "Attribute does not have a value: " + name);
        } else {
            // FIXME: if offset != null, for now we assume the value of 
            // the parameter is fixed during execution.
            if (attribute instanceof Parameter) {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof ArrayToken) {
                    return (((ArrayToken) token).getElement(new Integer(offset)
                            .intValue())).toString();
                }

                throw new IllegalActionException(_component, attributeName
                        + " does not contain an ArrayToken.");
            }

            throw new IllegalActionException(_component, attributeName
                    + " is not a parameter.");
        }
    }

    /** Return the associated actor's rates for all configurations of this actor. 
     *  In this base class, return null.
     *  @return null
     */
    public int[][] getRates() {
        return null;
    }

    /** Get the read offset in the buffer of a given channel from which a token
     *  should be read. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel from which a token
     *   should be read.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     */
    public Object getReadOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            return ((Object[]) _readOffsets.get(inputPort))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            return actorHelper.getReadOffset(inputPort, channelNumber);
        }
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
    public String getReference(String name) throws IllegalActionException {
        boolean isInputPort = false;
        name = processCode(name);

        String[] nameChannelOffset = parseName(name);
        String portName = nameChannelOffset[0];
        String channel = nameChannelOffset[1];

        IOPort port = (IOPort)((Entity)_component).getPort(portName);

        CodeGeneratorHelper sourceHelper = this;
        if (port != null && port.isInput()) {
            isInputPort = true;

            // Find the source helper
            /*
            TypedIOPort sinkPort = port;
            int sourceChannel = new Integer(channel).intValue();
            port = (TypedIOPort) port.connectedPortList().get(sourceChannel);
            portName = port.getName();
            channel = "" + _getChannelIndex(sinkPort, sourceChannel, port);
            sourceHelper = (CodeGeneratorHelper) _getHelper(port.getContainer());
            */
            // else if (port.isOutput()), that means THIS is the source helper.
            
            int channelNumber = new Integer(channel).intValue();
            Receiver receiver = port.getReceivers()[channelNumber][0];
            Iterator sourcePorts = port.sourcePortList().iterator();
            breakOutLabel:
            while (sourcePorts.hasNext()) {
                IOPort sourcePort = (IOPort) sourcePorts.next();
                Receiver[][] remoteReceivers = sourcePort.getRemoteReceivers();
                for (int i = 0; i < remoteReceivers.length; i++) {
                    for (int j = 0; j < remoteReceivers[i].length; j++) {
                        if (remoteReceivers[i][j] == receiver) {
                            portName = sourcePort.getName();
                            channel = "" + i;
                            sourceHelper = (CodeGeneratorHelper) 
                                    _getHelper(sourcePort.getContainer());
                            break breakOutLabel;
                        }
                    }
                }
            }
            
        }

        String refName = _getReference(name);

        String convertMethod = (String) sourceHelper._portConversions
                .get(portName + "#" + channel);
        if (convertMethod != null) {
            String type = (String) sourceHelper._portDeclareTypes.get(portName
                    + "#" + channel);
            if (type.equals("Token")) {
                String typeName = convertMethod.substring(0, convertMethod
                        .indexOf("_new"));
                if (_isPrimitiveType(typeName)) {
                    refName += ".payload." + typeName;
                    sourceHelper._newTypesUsed.add(typeName);
                }
            } else if (type.equals("char*")) {
                if (!isInputPort) {
                    // Give the temp variable holder as reference.     
                    refName = refName.replace('[', '_').replace(']', '_');
                }
            } else {
                // FIXME: we can add code here to handle conversion 
                // between different primitive types.   
            }
        }
        return refName;
    }

    /** Return a list of channel objects that are the sink input ports given
     *  a port and channel. Note the returned channels are newly
     *  created objects and therefore not associated with the helper class.
     *  @param port The given output port.
     *  @param channelNumber The given channel number.
     *  @return The list of channel objects that are the sink channels
     *   of the given output channel.
     */
    public List getSinkChannels(IOPort port, int channelNumber) {
        List sinkChannels = new LinkedList();
        Receiver[][] remoteReceivers;

        // due to reason stated in getReference(String), 
        // we cannot do: if (port.isInput())...
        if (port.isOutput()) {
            remoteReceivers = port.getRemoteReceivers();
        } else {
            remoteReceivers = port.deepGetReceivers();
        }

        if (remoteReceivers.length == 0) {
            // This is an escape method. This class will not call this
            // method if the output port does not have a remote receiver.
            return sinkChannels;
        }

        for (int i = 0; i < remoteReceivers[channelNumber].length; i++) {
            IOPort sinkPort = remoteReceivers[channelNumber][i].getContainer();
            Receiver[][] portReceivers;

            if (sinkPort.isInput()) {
                portReceivers = sinkPort.getReceivers();
            } else {
                portReceivers = sinkPort.getInsideReceivers();
            }

            for (int j = 0; j < portReceivers.length; j++) {
                for (int k = 0; k < portReceivers[j].length; k++) {
                    if (remoteReceivers[channelNumber][i] == portReceivers[j][k]) {
                        Channel sinkChannel = new Channel(sinkPort, j);
                        sinkChannels.add(sinkChannel);
                        break;
                    }
                }
            }
        }

        return sinkChannels;
    }

    /** Get the size of a parameter. The size of a parameter
     *  is the length of its array if the parameter's type is array,
     *  and 1 otherwise.
     *  @param name The name of the parameter.
     *  @return The size of a parameter.
     *  @exception IllegalActionException If no port or parameter of
     *   the given name is found.
     */
    public int getSize(String name) throws IllegalActionException {

        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(name);

        if (attribute != null) {
            // FIXME:  Could it be something other than variable?
            if (attribute instanceof Variable) {
                Token token = ((Variable) attribute).getToken();

                if (token instanceof ArrayToken) {
                    return ((ArrayToken) token).length();
                }

                return 1;
            }
        }

        throw new IllegalActionException(_component, "Attribute not found: "
                + name);
    }

    /** Return the translated type function invocation string.
     *  @param functionString The string within the $typeFunc() macro.
     *  @return The translated type function invocation string.
     *  @exception IllegalActionException The given function string is
     *   not well-formed.
     */
    public String getTypeFuncInvocation(String functionString)
            throws IllegalActionException {
        functionString = processCode(functionString);

        // i.e. "$typeFunc(token, add(arg1, arg2, ...))"
        // this transforms to ==> 
        // "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
        // FIXME: we need to do some more smart parsing to find the following
        // indexes.
        int commaIndex = functionString.indexOf(',');
        int openFuncParenIndex = functionString.indexOf('(', commaIndex);
        int closeFuncParenIndex = functionString.lastIndexOf(')');

        // Syntax checking.
        if ((commaIndex == -1) || (openFuncParenIndex == -1)
                || (closeFuncParenIndex != (functionString.length() - 1))) {
            throw new IllegalActionException(
                    "Bad Syntax with the $typeFunc() macro. "
                            + "[i.e. -- $typeFunc(token.func(arg1, arg2, ...))]");
        }

        String typedToken = functionString.substring(0, commaIndex).trim();
        String functionName = functionString.substring(commaIndex + 1,
                openFuncParenIndex).trim();

        // Record the referenced type function in the infoTable.
        _typeFuncUsed.add(functionName);

        String argumentList = functionString.substring(openFuncParenIndex + 1)
                .trim();
        // if it is more than just a closing paren
        if (argumentList.length() > 1) {
            argumentList = ", " + argumentList;
        }

        return "functionTable[" + typedToken + ".type][FUNC_" + functionName
                + "](" + typedToken + argumentList;
    }

    /** Get the write offset in the buffer of a given channel to which a token
     *  should be put. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel to which a token
     *   should be put.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     */
    public Object getWriteOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            return ((Object[]) _writeOffsets.get(inputPort))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            return actorHelper.getWriteOffset(inputPort, channelNumber);
        }
    }

    /**
     * Parse and type check the given name, and get the reference name,
     * channel index and offset number.
     * @param name The given name string.
     * @return An array of three Strings, the first is the port name,
     * the second is the channel and the third is the offset.
     * @throws IllegalActionException Thrown if the name string is not 
     *  in the proper format.
     */
    public String[] parseName(String name) throws IllegalActionException {
        String[] result = new String[3];
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
                && (tokenizer.countTokens() != 5)) {
            throw new IllegalActionException(_component,
                    "Bad formatted name string reference: " + name);
        }

        String portName = tokenizer.nextToken().trim();
        String channel = null;
        String offset = null;
        if (tokenizer.hasMoreTokens()) {
            String nextToken = tokenizer.nextToken();
            if (nextToken.equals("#")) {
                channel = tokenizer.nextToken().trim();

                if (tokenizer.hasMoreTokens()) {
                    if (!tokenizer.nextToken().equals(",")) {
                        throw new IllegalActionException(_component,
                                "Bad formatted name string reference: " + name);
                    } else {
                        offset = tokenizer.nextToken().trim();
                    }
                }
            } else if (nextToken.equals(",")) {
                offset = tokenizer.nextToken().trim();
                if (tokenizer.hasMoreTokens()) {
                    throw new IllegalActionException(_component,
                            "Bad formatted name string reference: " + name);
                }
            }
        }
        if (channel == null) {
            channel = "0";
        }
        if (offset == null) {
            offset = "0";
        }

        try {
            Integer.parseInt(channel); // type check for number format.
            Integer.parseInt(offset); // type check for number format.
        } catch (NumberFormatException ex) {
            result[0] = portName;
            // A variable index is passed as arguments
        }
        result[0] = portName;
        result[1] = channel;
        result[2] = offset;
        return result;
    }

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        int currentPos = code.indexOf("$");

        if (currentPos < 0) {
            // No "$" in the string
            return code;
        }

        result.append(code.substring(0, currentPos));

        while (currentPos < code.length()) {
            int openParenIndex = code.indexOf("(", currentPos + 1);
            int closeParenIndex = _findCloseParen(code, openParenIndex);

            if (closeParenIndex < 0) {
                // No matching close parenthesis is found.
                result.append(code.substring(currentPos));
                return result.toString();
            }

            int nextPos = code.indexOf("$", closeParenIndex + 1);

            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }

            String subcode = code.substring(currentPos, nextPos);

            if ((currentPos > 0) && (code.charAt(currentPos - 1) == '\\')) {
                // found "\$", do not make replacement.
                result.append(subcode);
                currentPos = nextPos;
                continue;
            }

            boolean foundIt = false;
            String macro = code.substring(currentPos + 1, openParenIndex);
            macro = macro.trim();

            List macroList = Arrays.asList(new String[] { "ref", "val", "type",
                    "typeFunc", "token", "actorSymbol", "actorClass", "new",
                    "size" });

            if (macroList.contains(macro)) {
                String name = code.substring(openParenIndex + 1,
                        closeParenIndex);
                name = name.trim();

                if (macro.equals("ref")) {
                    result.append(getReference(name));
                } else if (macro.equals("token")) {
                    result.append(_getReference(name));
                } else if (macro.equals("type")) {
                    // FIXME: we should be able to resolve the type in compile time.
                    result.append(_getReference(name) + ".payload.type");
                } else if (macro.equals("val")) {
                    result.append(getParameterValue(name, _component));
                } else if (macro.equals("size")) {
                    result.append(getSize(name));
                } else if (macro.equals("actorSymbol")) {
                    result.append(_component.getFullName().replace('.', '_'));
                    result.append("_" + name);
                } else if (macro.equals("actorClass")) {
                    result.append(_component.getClassName().replace('.', '_'));
                    result.append("_" + name);
                } else if (macro.equals("new")) {
                    result.append(getNewInvocation(name));
                } else if (macro.equals("typeFunc")) {
                    result.append(getTypeFuncInvocation(name));
                } else {
                    // This macro is not handled.
                    throw new IllegalActionException("Macro is not handled.");
                }

                foundIt = true;
                result.append(code.substring(closeParenIndex + 1, nextPos));
            }

            if (!foundIt) {
                result.append(subcode);
            }

            currentPos = nextPos;
        }

        return result.toString();
    }

    /** Reset the offsets of all channels of all input ports of the
     *  associated actor to the default value of 0.
     * 
     *  @return The reset code of the associated actor.
     *  @exception IllegalActionException If thrown while getting or
     *   setting the offset.
     */
    public String resetInputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            for (int i = 0; i < port.getWidth(); i++) {
                Object readOffset = getReadOffset(port, i);
                if (readOffset instanceof Integer) {
                    setReadOffset(port, i, new Integer(0));
                } else {
                    code.append(((String) readOffset) + " = 0;\n");
                }
                Object writeOffset = getWriteOffset(port, i);
                if (writeOffset instanceof Integer) {
                    setWriteOffset(port, i, new Integer(0));
                } else {
                    code.append(((String) writeOffset) + " = 0;\n");
                }
            }
        }
        return code.toString();
    }

    /** Set the buffer size of a given port.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param bufferSize The buffer size to be set to that port and channel.
     */
    public void setBufferSize(IOPort port, int channelNumber, int bufferSize) {
        int[] bufferSizes = (int[]) _bufferSizes.get(port);
        bufferSizes[channelNumber] = bufferSize;

        // perhaps this step is redundant?
        _bufferSizes.put(port, bufferSizes);
    }

    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /** Set the read offset in a buffer of a given channel from which a token
     *  should be read.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel.
     *  @param readOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     */
    public void setReadOffset(IOPort inputPort, int channelNumber,
            Object readOffset) throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            Object[] readOffsets = (Object[]) _readOffsets.get(inputPort);
            readOffsets[channelNumber] = readOffset;
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            actorHelper.setReadOffset(inputPort, channelNumber, readOffset);
        }
    }

    /** Set the write offset in the downstream buffers connected to the given channel.
     *  @param outputPort The given port.
     *  @param channelNumber The given channel.
     *  @param writeOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException If 
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     */
    public void setSinkActorsWriteOffset(IOPort outputPort, int channelNumber,
            Object writeOffset) throws IllegalActionException {
        List sinkChannels = getSinkChannels(outputPort, channelNumber);

        for (int i = 0; i < sinkChannels.size(); i++) {
            Channel channel = (Channel) sinkChannels.get(i);
            IOPort sinkPort = channel.port;
            int sinkChannelNumber = channel.channelNumber;
            setWriteOffset(sinkPort, sinkChannelNumber, writeOffset);
        }
    }

    /** Set the write offset in a buffer of a given channel to which a token
     *  should be put.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel.
     *  @param writeOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException If 
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     */
    public void setWriteOffset(IOPort inputPort, int channelNumber,
            Object writeOffset) throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            Object[] writeOffsets = (Object[]) _writeOffsets.get(inputPort);
            writeOffsets[channelNumber] = writeOffset;
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            actorHelper.setWriteOffset(inputPort, channelNumber, writeOffset);
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public class Channel {
        /** Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel(IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }

        /** The port that contains this channel.
         */
        public IOPort port;

        /** The channel number of this channel.
         */
        public int channelNumber;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Create the buffer size and offset maps for each input port, which is
     *  associated with this helper object. A key of the map is an IOPort
     *  of the actor. The corresponding value is an array of channel objects.
     *  The i-th channel object corresponds to the i-th channel of that IOPort.
     *  This method is used to maintain a internal HashMap of channels of the
     *  actor. The channel objects in the map are used to keep track of the
     *  buffer sizes or offsets in their buffer.
     *  @exception IllegalActionException If the director helper or executive
     *   director is not found, or if 
     *   {@link #setReadOffset(IOPort, int, Object)} method throws it, or if
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     *  
     */
    protected void _createBufferSizeAndOffsetMap()
            throws IllegalActionException {
        //We only care about input ports where data are actually stored
        //except when an output port is not connected to any input port.
        //In that case the variable corresponding to the unconnected output
        //port always has size 1 and the assignment to this variable is 
        //performed just for the side effect.
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            int length = port.getWidth();

            //if (length == 0) {
            //    length = 1;
            //}
            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);

            Director directorHelper = (Director) _getHelper(((Actor) _component)
                    .getExecutiveDirector());

            for (int i = 0; i < port.getWidth(); i++) {
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            Object[] readOffsets = new Object[length];
            _readOffsets.put(port, readOffsets);

            Object[] writeOffsets = new Object[length];
            _writeOffsets.put(port, writeOffsets);

            for (int i = 0; i < length; i++) {
                setReadOffset(port, i, new Integer(0));
                setWriteOffset(port, i, new Integer(0));
            }
        }
    }

    /** Given a port or parameter, append a string in the form
     *  "static <i>type</i> <i>objectName</i>" to the given string buffer.
     *  This method is only called in the generateVariableDeclarations() 
     *  method.
     *  @param ptType The port or parameter.
     *  @return The generated code for the type.
     */
    protected static String _generateType(Type ptType) {
        return _getCTypeFromPtolemyType(ptType);
    }

    /**
     * Generate the type conversion variable declarations for a given port.
     * These are extra variable declarations that are needed for port type
     * conversion between actors.
     * @param port The given port.
     * @param code The given code buffer to append to.
     * @exception IllegalActionException Thrown if the associated helper is not found, or if the
     *  source port channel index is not found, or if the buffer size of 
     *  the given port cannot be determined, or if the given port name
     *  reference is not found.
     */
    protected void _generateTypeConvertVariableDeclaration(TypedIOPort port,
            StringBuffer code) throws IllegalActionException {

        // See if source port(s) need to be converted.
        List sourcePorts = port.sourcePortList();
        for (int i = 0; i < sourcePorts.size(); i++) {
            TypedIOPort sourcePort = (TypedIOPort) sourcePorts.get(i);
            if (port.getType() == sourcePort.getType() ||
                    port.getType() != BaseType.STRING) {
                continue;
            }
            // FIXME: 1. inputPort.sourcePortList() returns a list 
            // of source ports. The API does not say the 1st source
            // port in the list connects to the 1st channel of input 
            // port, the 2nd to the 2nd, etc. _getChannelIndex(inputPort, j,
            // sourcePort) uses this assumption which is not guaranteed.
            // 2. It does not consider the case that the same channel
            // of the input port may be connected to more than one source
            // port, e.g., in modal model.
            String sourcePortName = sourcePort.getName() + "#"
                    + _getChannelIndex(port, i, sourcePort);
            CodeGeneratorHelper sourceHelper = (CodeGeneratorHelper) _getHelper(sourcePort
                    .getContainer());

            String type = (String) sourceHelper._portDeclareTypes
                    .get(sourcePortName);
            String convert = (String) sourceHelper._portConversions
                    .get(sourcePortName);

            // if no given type declaration, then use the type of the port.
            if (type == null) {

            } else if (type.equals("char*") && convert.indexOf("_new") == -1) {
                // Case: convert primitive type to String.
                // We need to declare temp variable holder.
                for (int j = 0; j < getBufferSize(port, i); j++) {
                    String tempVariableName = _getReference(
                            port.getName() + "#" + i + ", " + j).replace('[',
                            '_').replace(']', '_');

                    // The type should be a primitive type (less than String).
                    String tempVariableType = sourcePort.getType().toString();
                    code.append(tempVariableType + " " + tempVariableName
                            + ";\n");
                }
            }
        }
    }

    /**
     * Find the index of the source port relative to the sink port, 
     * given the source port and the relative channel index. 
     * @param sinkPort The sink port.
     * @param sourceIndex The source channel index in the sink port.
     * @param sourcePort The source port.
     * @return The index of the sink port in the source port.
     * @exception IllegalActionException Thrown if the channel index
     *  of the source port is not found.
     */
    protected int _getChannelIndex(TypedIOPort sinkPort, int sourceIndex,
            TypedIOPort sourcePort) throws IllegalActionException {

        // Get receiver from the sink port.
        Receiver receiver = sinkPort.getReceivers()[sourceIndex][0];

        // Iterate receivers in source port to find the receiver.
        Receiver[][] receivers = null;
        if (sourcePort.isOutput()) {
            receivers = sourcePort.getRemoteReceivers();
        } else {
            receivers = sourcePort.deepGetReceivers();
        }
        for (int i = 0; i < receivers.length; i++) {
            for (int j = 0; j < receivers[i].length; j++) {
                if (receiver.equals(receivers[i][j])) {
                    return i;
                }
            }
        }

        throw new IllegalActionException("Channel index not found "
                + "for sink port (" + sinkPort.getFullName()
                + ") and source port(" + sourcePort.getFullName() + "\n");
    }

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
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
    protected String _getReference(String name) throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        Actor actor = (Actor) _component;
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
                && (tokenizer.countTokens() != 5)) {
            throw new IllegalActionException(_component,
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        boolean forComposite = false;

        // Usually given the name of an input port, getReference(String name) 
        // returns variable name representing the input port. Given the name 
        // of an output port, getReference(String name) returns variable names
        // representing the input ports connected to the output port. 
        // However, if the name of an input port starts with '@', 
        // getReference(String name) returns variable names representing the 
        // input ports connected to the given input port on the inside. 
        // If the name of an output port starts with '@', 
        // getReference(String name) returns variable name representing the 
        // the given output port which has inside receivers.
        // The special use of '@' is for composite actor when
        // tokens are transferred into or out of the composite actor.
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        IOPort port = null;

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (inputPort.getName().equals(refName)) {
                port = inputPort;
                break;
            }
        }

        if (port == null) {
            Iterator outputPorts = actor.outputPortList().iterator();

            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();

                // The channel is specified as $ref(port#channelNumber).
                if (outputPort.getName().equals(refName)) {
                    port = outputPort;
                    break;
                }
            }
        }

        if (port != null) {
            // To support modal model, we need to check the following condition 
            // first because an output port of a modal controller should be 
            // mainly treated as an output port. However, during choice action, 
            // an output port of a modal controller will receive the tokens sent 
            // from the same port.  During commit action, an output port of a modal 
            // controller will NOT receive the tokens sent from the same port.  
            if ((port.isOutput() && !forComposite)
                    || (port.isInput() && forComposite)) {
                Receiver[][] remoteReceivers;

                // For the same reason as above, we cannot do: if (port.isInput())...
                if (port.isOutput()) {
                    remoteReceivers = port.getRemoteReceivers();
                } else {
                    remoteReceivers = port.deepGetReceivers();
                }

                if (remoteReceivers.length == 0) {
                    // This channel of this output port doesn't have any sink.
                    result.append(_component.getFullName().replace('.', '_'));
                    result.append("_");
                    result.append(port.getName());
                    return result.toString();
                }

                String[] channelAndOffset = _getChannelAndOffset(name);

                List sinkChannels = new LinkedList();
                int channelNumber = 0;

                if (!channelAndOffset[0].equals("")) {
                    channelNumber = (new Integer(channelAndOffset[0]))
                            .intValue();
                }

                sinkChannels = getSinkChannels(port, channelNumber);

                for (int i = 0; i < sinkChannels.size(); i++) {
                    Channel channel = (Channel) sinkChannels.get(i);
                    IOPort sinkPort = channel.port;
                    int sinkChannelNumber = channel.channelNumber;

                    if (i != 0) {
                        result.append(" = ");
                    }

                    result.append(sinkPort.getFullName().replace('.', '_'));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }

                    //int sinkPortBufferSize = getBufferSize(sinkPort);

                    if (!channelAndOffset[1].equals("")
                            && (getBufferSize(sinkPort) > 1)) {
                        // Specified offset.

                        String temp = "";

                        Object offsetObject = getWriteOffset(sinkPort,
                                sinkChannelNumber);

                        if (offsetObject instanceof Integer) {

                            int offset = ((Integer) offsetObject).intValue()
                                    + (new Integer(channelAndOffset[1]))
                                            .intValue();
                            offset %= getBufferSize(sinkPort, sinkChannelNumber);
                            temp = new Integer(offset).toString();
                            /*
                             int divisor = getBufferSize(sinkPort,
                             sinkChannelNumber);
                             temp = "("
                             + getWriteOffset(sinkPort,
                             sinkChannelNumber) + " + "
                             + channelAndOffset[1] + ")%" + divisor;
                             */

                        } else {
                            int modulo = getBufferSize(sinkPort,
                                    sinkChannelNumber) - 1;
                            temp = "("
                                    + (String) getWriteOffset(sinkPort,
                                            sinkChannelNumber) + " + "
                                    + channelAndOffset[1] + ")&" + modulo;
                        }

                        result.append("[" + temp + "]");

                    } else if (getBufferSize(sinkPort) > 1) {
                        // Did not specify offset, so the receiver buffer
                        // size is 1. This is multiple firing.
                        String temp = "";

                        Object offsetObject = getWriteOffset(sinkPort,
                                sinkChannelNumber);

                        if (offsetObject instanceof Integer) {
                            int offset = ((Integer) offsetObject).intValue();
                            offset %= getBufferSize(sinkPort, sinkChannelNumber);
                            temp = new Integer(offset).toString();
                        } else {
                            int modulo = getBufferSize(sinkPort,
                                    sinkChannelNumber) - 1;
                            temp = (String) getWriteOffset(sinkPort,
                                    sinkChannelNumber)
                                    + "&" + modulo;
                        }
                        result.append("[" + temp + "]");
                    }
                }

                return result.toString();
            }

            if ((port.isInput() && !forComposite)
                    || (port.isOutput() && forComposite)) {
                result.append(port.getFullName().replace('.', '_'));

                String[] channelAndOffset = _getChannelAndOffset(name);
                int channelNumber = 0;

                if (!channelAndOffset[0].equals("")) {
                    // Channel number specified. This must be a multiport.
                    result.append("[" + channelAndOffset[0] + "]");
                    channelNumber = new Integer(channelAndOffset[0]).intValue();
                }

                if (!channelAndOffset[1].equals("")
                        && (getBufferSize(port) > 1)) {
                    String temp = "";

                    if (getReadOffset(port, channelNumber) instanceof Integer) {
                        int offset = ((Integer) getReadOffset(port,
                                channelNumber)).intValue();
                        offset = offset
                                + (new Integer(channelAndOffset[1])).intValue();
                        offset = offset % getBufferSize(port, channelNumber);
                        temp = new Integer(offset).toString();
                    } else {
                        // Note: This assumes the director helper will increase
                        // the buffer size of the channel to the power of two.
                        // Otherwise, use "%" instead.
                        // FIXME: We haven't check if modulo is 0. But this
                        // should never happen. For offsets that need to be
                        // represented by string expression,
                        // getBufferSize(port, channelNumber) will always
                        // return a value at least 2.
                        int modulo = getBufferSize(port, channelNumber) - 1;
                        temp = (String) getReadOffset(port, channelNumber);
                        temp = "(" + temp + " + " + channelAndOffset[1] + ")&"
                                + modulo;
                    }

                    result.append("[" + temp + "]");
                } else if (getBufferSize(port) > 1) {
                    // Did not specify offset, so the receiver buffer
                    // size is 1. This is multiple firing.
                    String temp = "";

                    if (getReadOffset(port, channelNumber) instanceof Integer) {
                        int offset = ((Integer) getReadOffset(port,
                                channelNumber)).intValue();
                        offset = offset % getBufferSize(port, channelNumber);
                        temp = new Integer(offset).toString();
                    } else {
                        int modulo = getBufferSize(port, channelNumber) - 1;
                        temp = (String) getReadOffset(port, channelNumber);
                        temp = temp + "&" + modulo;
                    }

                    result.append("[" + temp + "]");
                }

                return result.toString();
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(refName);

        if (attribute != null) {
            //FIXME: potential bug: if the attribute is not a parameter,
            //it will be referenced but not declared.
            if (attribute instanceof Parameter) {
                _referencedParameters.add(attribute);
            }

            result.append(generateVariableName(attribute));

            String[] channelAndOffset = _getChannelAndOffset(name);

            if (!channelAndOffset[0].equals("")) {
                throw new IllegalActionException(_component,
                        "a parameter cannot have channel number.");
            }

            if (!channelAndOffset[1].equals("")) {
                //result.append("[" + channelAndOffset[1] + "]");
                result.insert(0, "Array_get(");
                result.append(" ," + channelAndOffset[1] + ")" + 
                        ".payload.");
                Type elementType = ((ArrayType) ((Parameter) 
                        attribute).getType()).getElementType();
                result.append(_getCodeGenTypeFromPtolemyType(elementType));
            }

            return result.toString();
        }

        throw new IllegalActionException(_component, "Reference not found: "
                + name);

    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type. 
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     * @exception IllegalActionException Thrown if the given ptolemy cannot
     *  be resolved.
     */
    protected static String _getCodeGenTypeFromPtolemyType(Type ptType)
            throws IllegalActionException {
        // FIXME: we may need to add more types.
        String result = ptType == BaseType.INT ? "Int"
                : ptType == BaseType.STRING ? "String"
                        : ptType == BaseType.DOUBLE ? "Double"
                                : ptType == BaseType.BOOLEAN ? "Boolean"
                                        : ptType instanceof ArrayType ? "Array"
                                                : ptType == BaseType.MATRIX ? "Matrix"
                                                        : ptType == BaseType.GENERAL ? "Token"
                                                                : "";
        if (result.length() == 0) {
            throw new IllegalActionException(
                    "Cannot resolved codegen type from Ptolemy type: " + ptType);
        }
        return result;
    }

    /**
     * Get the corresponding type in C from the given Ptolemy type. 
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     */
    protected static String _getCTypeFromPtolemyType(Type ptType) {
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "char*"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : "Token";
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     * @exception IllegalActionException Thrown if there is no
     *  corresponding codegen type.
     */
    protected static boolean _isPrimitiveType(Type ptType)
            throws IllegalActionException {
        return CodeGenerator._primitiveTypes
                .contains(_getCodeGenTypeFromPtolemyType(ptType));
    }

    /**
     * Determine if the given type is primitive.
     * @param cgType The given codegen type.
     * @return true if the given type is primitive, otherwise false.
     */
    protected static boolean _isPrimitiveType(String cgType) {
        return CodeGenerator._primitiveTypes.contains(cgType);
    }

    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    protected class HelperScope extends ModelScope {
        /** Construct a scope consisting of the variables of the containing
         *  actor and its containers and their scope-extending attributes.
         */
        public HelperScope() {
            _variable = null;
        }

        /** Construct a scope consisting of the variables of the container
         *  of the given instance of Variable and its containers and their
         *  scope-extending attributes.
         *  @param variable The variable whose expression is under code 
         *   generation using this scope.
         */
        public HelperScope(Variable variable) {
            _variable = variable;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Look up and return the macro or expression in the target language
         *  corresponding to the specified name in the scope.
         *  @param name The given name string.
         *  @return The macro or expression with the specified name in the scope.
         *  @exception IllegalActionException If thrown while getting buffer 
         *   sizes or creating ObjectToken.
         */
        public Token get(String name) throws IllegalActionException {
            Iterator inputPorts = ((Actor) _component).inputPortList()
                    .iterator();

            // try input port
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();

                StringBuffer code = new StringBuffer();
                boolean found = false;
                int channelNumber = 0;
                // try input port name only
                if (name.equals(inputPort.getName())) {
                    found = true;
                    code.append(inputPort.getFullName().replace('.', '_'));
                    if (inputPort.isMultiport()) {
                        code.append("[0]");
                    }
                } else {
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        // try the format: inputPortName_channelNumber 
                        if (name.equals(inputPort.getName() + "_" + i)) {
                            found = true;
                            channelNumber = i;
                            code.append(inputPort.getFullName().replace('.',
                                    '_'));
                            code.append("[" + i + "]");
                            break;
                        }
                    }
                }
                if (found) {
                    int bufferSize = getBufferSize(inputPort);
                    if (bufferSize > 1) {
                        int bufferSizeOfChannel = getBufferSize(inputPort,
                                channelNumber);
                        String writeOffset = (String) getWriteOffset(inputPort,
                                channelNumber);
                        // Note here inputPortNameArray in the original expression 
                        // is converted to 
                        // inputPortVariable[(writeoffset - 1 
                        // + bufferSizeOfChannel)&(bufferSizeOfChannel-1)] 
                        // in the generated C code.
                        code.append("[(" + writeOffset + " + "
                                + (bufferSizeOfChannel - 1) + ")&"
                                + (bufferSizeOfChannel - 1) + "]");
                    }
                    return new ObjectToken(code.toString());
                }

                // try the format: inputPortNameArray
                found = false;
                channelNumber = 0;
                if (name.equals(inputPort.getName() + "Array")) {
                    found = true;
                    code.append(inputPort.getFullName().replace('.', '_'));
                    if (inputPort.isMultiport()) {
                        code.append("[0]");
                    }
                } else {
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        // try the format: inputPortName_channelNumberArray
                        if (name
                                .equals(inputPort.getName() + "_" + i + "Array")) {
                            found = true;
                            channelNumber = i;
                            code.append(inputPort.getFullName().replace('.',
                                    '_'));
                            code.append("[" + i + "]");
                            break;
                        }
                    }
                }
                if (found) {
                    int bufferSize = getBufferSize(inputPort);
                    if (bufferSize > 1) {
                        int bufferSizeOfChannel = getBufferSize(inputPort,
                                channelNumber);
                        String writeOffset = (String) getWriteOffset(inputPort,
                                channelNumber);
                        // '@' represents the array index in the parsed expression.
                        // It will be replaced by actual array index in 
                        // the method visitFunctionApplicationNode() in
                        // ParseTreeCodeGenerator.
                        // Note here inputPortNameArray(i) in the original expression 
                        // is converted to 
                        // inputPortVariable[(writeoffset - i - 1 
                        // + bufferSizeOfChannel)&(bufferSizeOfChannel-1)] 
                        // in the generated C code.
                        code.append("[(" + writeOffset + " - (@)" + " + "
                                + (bufferSizeOfChannel - 1) + ")&"
                                + (bufferSizeOfChannel - 1) + "]");
                    }
                    return new ObjectToken(code.toString());
                }

            }

            // try variable
            NamedObj container = _component;
            if (_variable != null) {
                container = _variable.getContainer();
            }

            Variable result = getScopedVariable(_variable, container, name);

            if (result != null) {
                // If the variable found is a modified variable, which means
                // its vaule can be directly changed during execution 
                // (e.g., in commit action of a modal controller), then this
                // variable is declared in the target language and should be
                // referenced by the name anywhere it is used.
                if (_codeGenerator._modifiedVariables.contains(result)) {
                    return new ObjectToken(generateVariableName(result));
                } else {
                    // This will lead to recursive call until a variable found 
                    // is either directly specified by a constant or it is a  
                    // modified variable.  
                    return new ObjectToken("("
                            + getParameterValue(name, result.getContainer())
                            + ")");
                }
            } else {
                return null;
            }
        }

        /** This method should not be called.
         *  @exception IllegalActionException If it is called.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            throw new IllegalActionException(
                    "This method should not be called.");
        }

        /** This method should not be called.
         *  @exception IllegalActionException If it is called.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            throw new IllegalActionException(
                    "This method should not be called.");
        }

        /** This method should not be called.
         *  @throws IllegalActionException If it is called.
         */
        public Set identifierSet() throws IllegalActionException {
            throw new IllegalActionException(
                    "This method should not be called.");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** If _variable is not null, then the helper scope created is
         *  for parsing the expression specified for this variable and
         *  generating the corresponding code in target language.
         */
        private Variable _variable = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the paired close parenthesis given a string and an index
     *  which is the position of an open parenthesis. Return -1 if no
     *  paired close parenthesis is found.
     *  @param string The given string.
     *  @param pos The given index.
     *  @return The index which indicates the position of the paired
     *   close parenthesis of the string.
     *  @exception IllegalActionException If the character at the
     *   given position of the string is not an open parenthesis.
     */
    private int _findCloseParen(String string, int pos)
            throws IllegalActionException {
        if (string.charAt(pos) != '(') {
            throw new IllegalActionException(_component,
                    "The character at index " + pos + " of string: " + string
                            + " is not a open parenthesis.");
        }

        int nextOpenParen = string.indexOf("(", pos + 1);

        if (nextOpenParen < 0) {
            nextOpenParen = string.length();
        }

        int nextCloseParen = string.indexOf(")", pos);

        if (nextCloseParen < 0) {
            return -1;
        }

        int count = 1;
        int beginIndex = pos + 1;

        while (beginIndex > 0) {
            if (nextCloseParen < nextOpenParen) {
                count--;

                if (count == 0) {
                    return nextCloseParen;
                }

                beginIndex = nextCloseParen + 1;
                nextCloseParen = string.indexOf(")", beginIndex);

                if (nextCloseParen < 0) {
                    return -1;
                }
            }

            if (nextOpenParen < nextCloseParen) {
                count++;
                beginIndex = nextOpenParen + 1;
                nextOpenParen = string.indexOf("(", beginIndex);

                if (nextOpenParen < 0) {
                    nextOpenParen = string.length();
                }
            }
        }

        return -1;
    }

    /** Return the channel number and offset given in a string.
     *  The result is an integer array of length 2. The first element
     *  indicates the channel number, the second the offset. If either
     *  element is -1, it means that channel/offset is not specified.
     * @param name The given string.
     * @return An integer array of length 2, indicating the channel
     *  number and offset.
     * @exception IllegalActionException If the channel number or offset
     *  specified in the given string is illegal.
     */
    private String[] _getChannelAndOffset(String name)
            throws IllegalActionException {
        String[] result = { "", "" };
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);
        tokenizer.nextToken();

        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (token.equals("#")) {
                result[0] = tokenizer.nextToken().trim();

                if (tokenizer.hasMoreTokens()) {
                    if (tokenizer.nextToken().equals(",")) {
                        result[1] = tokenizer.nextToken().trim();
                    }
                }
            } else if (token.equals(",")) {
                result[1] = tokenizer.nextToken().trim();
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A hashmap that keeps track of the bufferSizes of each channel
     *  of the actor.
     */
    protected HashMap _bufferSizes = new HashMap();

    /** The code generator that contains this helper class.
     */
    protected CodeGenerator _codeGenerator;

    /** A HashSet that contains all codegen types referenced in the model.
     * When the codegen kernel processes a $new() macro, it would add the
     * codegen type to this set. Codegen types are supported by the code
     * generator package. (e.g. Int, Double, Array, and etc.)
     */
    protected HashSet _newTypesUsed = new HashSet();

    /** A HashSet that contains all type functions referenced in the model.
     *  When the codegen kernel processes a $typeFunc() macro, it would add
     *  the type function to this set. 
     */
    protected HashSet _typeFuncUsed = new HashSet();

    /** A HashMap that contains mapping for ports and their conversion method.
     *  Ports that does not need to be converted do NOT have record in this
     *  map. The codegen kernel record this mapping during the first pass over
     *  the model. This map is used later in the code generation phase.
     */
    protected HashMap _portConversions = new HashMap();

    /** A HashMap that contains mapping between ports and their corresponding
     *  c declaration types. The codegen kernel record this mapping during the
     *  first pass over the model. This map is used later in the code
     *  generation phase.
     */
    protected HashMap _portDeclareTypes = new HashMap();

    /** A hashmap that keeps track of the read offsets of each input channel of
     *  the actor.
     */
    protected HashMap _readOffsets = new HashMap();

    /** A hashmap that keeps track of the write offsets of each input channel of
     *  the actor.
     */
    protected HashMap _writeOffsets = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated component. */
    private NamedObj _component;

    /** A hashset that keeps track of parameters that are referenced for
     *  the associated actor.
     */
    private HashSet _referencedParameters = new HashSet();

}
