/* Code generator helper class associated with the Director class.

 Copyright (c) 2005 The Regents of the University of California.
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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.codegen.kernel.CodeGeneratorHelper.Channel;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////
////Director

/**
 Code generator helper associated with the ptolemy.actor.Director class.
 This class is also associated with a code generator.

 FIXME: need documentation on how subclasses should extend this class.

 @see CodeGenerator
 @author Ye Zhou
 @version $Id$ Director.java,v 1.24 2005/07/13 14:07:26 cxh Exp $
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (zhouye)
 @Pt.AcceptedRating Yellow (zhouye)

 */
public class Director implements ActorCodeGenerator {
    /** Construct the code generator helper associated with the given director.
     *  Note before calling the generate*() methos, you must also call
     *  setCodeGenerator(CodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        _director = director;
    }

    /////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////
    
    public String createOffsetVariablesIfNeeded() 
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();    
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            code.append(helperObject.createOffsetVariablesIfNeeded());
        }      
        return code.toString();
}

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the helpers for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        code.append("/* The firing of the director. */\n");

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            helperObject.generateFireCode(code);
        }
    }

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* The initialization of the director. */\n");

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        //Iterator actors = actorsList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            // Initialize code for the actor.
            code.append(helperObject.generateInitializeCode());
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                int rate = DFUtilities.getTokenInitProduction(port);
                _updateConnectedPortsOffset(port, code, rate);
            }    

            // Initialize code for inter-actor port type conversion. 
            Hashtable refTable = (Hashtable) helperObject.
                getInfo(CodeGeneratorHelper.FIELD_REFCONVERT);
            if (refTable != null) {
                Enumeration ports = refTable.keys();  
                while (ports.hasMoreElements()) {
                    String portRef = (String) ports.nextElement();
                    String convertMethod = (String) refTable.get(portRef);
                    
                    // Initialize only if it is converting to a Token type.
                    if (convertMethod.indexOf("new") > -1) {
                        code.append(helperObject.processCode("\t" +
                                   helperObject._getReference(portRef) + 
                                   " = " + convertMethod + "(0);\n"));
                    }
                }
            }
        }
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor. Set the buffer sizes of each
     *  port of the actors under the associated director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* The preinitialization of the director. */\n");

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        //Iterator actors = actorsList.iterator();
        while (actors.hasNext()) {
            // Set the buffer sizes of each channel of the actor before
            // generating initialize code.
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            //helperObject.createBufferAndOffsetMap();
            code.append(helperObject.generatePreinitializeCode());
        }

        return code.toString();
    }
    
    public void generateSwitchModeCode(StringBuffer code) 
            throws IllegalActionException {
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = 
                    (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            helperObject.generateSwitchModeCode(code);
        }      
    }


    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append("\n/* Transfer tokens to the inside */\n\n");

        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(_director
                .getContainer());

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = inputPort.getName();

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(_compositeActorHelper.getReference("@" + name));
                code.append(" = ");
                code.append(_compositeActorHelper.getReference(name));
                code.append(";\n");
            }
        }
        
        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, 1);
    }

    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append("\n/* Transfer tokens to the outside */\n\n");

        ptolemy.codegen.c.actor.TypedCompositeActor _compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(_director
                .getContainer());

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(_compositeActorHelper.getReference(name));
                code.append(" = ");
                code.append(_compositeActorHelper.getReference("@" + name));
                code.append(";\n");
            }
        }
        
        // The offset of the ports connected to the output port is 
        // updated by outside director.
        _updatePortOffset(outputPort, code, 1);
    }

    /** Generate the wrapup code of the director associated with this helper
     *  class. For this base class, this method just generate the wrapup code
     *  for each actor.
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If the helper class for each actor
     *  cannot be found, or if an error occurs while the helper generate the
     *  wrapup code.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("/* The wrapup of the director. */\n");

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ComponentCodeGenerator helperObject = _getHelper((NamedObj) actor);
            code.append(helperObject.generateWrapupCode());
        }

        return code.toString();
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number). In this base class, this method
     *  always returns 1.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel. This base class
     *   always returns 1.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        return 1;
    }

    /** Return the director associated with this class.
     *  @return The director associated with this class.
     */
    public NamedObj getComponent() {
        return _director;
    }

    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();

        if (_director instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _director)
                    .getModifiedVariables());
        }

        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            set.addAll(helperObject.getModifiedVariables());
        }

        return set;
    }

    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /////////////////////////////////////////////////////////////////////
    ////                   protected methods                         ////
    
    /** Return the minimum number of power of two that is greater than or
     *  equal to the given integer.
     *  @param value The given integer.
     *  @return the minimum number of power of two that is greater than or
     *   equal to the given integer.
     *  @exception IllegalActionException If the given integer is not positive.
     */
    protected int _ceilToPowerOfTwo(int value) throws IllegalActionException {
        if (value < 1) {
            throw new IllegalActionException(getComponent(),
                    "The given integer must be a positive integer.");
        }

        int powerOfTwo = 1;

        while (value > powerOfTwo) {
            powerOfTwo <<= 1;
        }

        return powerOfTwo;
    }

    /** Get the helper class associated with the given component.
     *  @param component The given component.
     *  @return the helper class associated with the given component.
     *  @exception IllegalActionException If the code generator throws
     *   it when getting the helper associated with the given component.
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
    
    /** Update the offsets of the buffer associated with the given port.
     * 
     *  @param port
     *  @param code
     *  @exception IllegalActionException
     */
    protected void _updatePortOffset(IOPort port, StringBuffer code, int rate) 
            throws IllegalActionException {
     
        CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper(port.getContainer());
        
        int length = 0;
        if (port.isInput()) {
            length = port.getWidth();
        } else {
            length = port.getWidthInside();
        }
     
        for (int j = 0; j < length; j++) {
            // Update the offset for each channel.
            if (helper.getReadOffset(port, j) instanceof Integer) {
                int offset = ((Integer) helper.getReadOffset(port, j)).intValue();
                offset = (offset + rate)% helper.getBufferSize(port, j);
                helper.setReadOffset(port, j, new Integer(offset));
            } else {
                int modulo = helper.getBufferSize(port, j) - 1;
                String offsetVariable = (String) helper.getReadOffset(port, j);
                code.append((String) offsetVariable + " = (" + offsetVariable 
                        + " + " + rate + ")&" + modulo + ";\n");
            }
        }
    }
    
    /** Update the offsets of the buffers associated with the ports connected 
     *  with the given port in its downstream.
     * 
     *  @param port
     *  @param code
     *  @throws IllegalActionException
     */
    protected void _updateConnectedPortsOffset(IOPort port, 
            StringBuffer code, int rate) throws IllegalActionException {
    
        CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper(port.getContainer());
        
        int length = 0;
        if (port.isInput()) {
            length = port.getWidthInside();
        } else {
            length = port.getWidth();
        }

        for (int j = 0; j < length; j++) {
            List sinkChannels = helper.getSinkChannels(port, j);

            for (int k = 0; k < sinkChannels.size(); k++) {
                Channel channel = (Channel) sinkChannels.get(k);
                IOPort sinkPort = (IOPort) channel.port;
                int sinkChannelNumber = channel.channelNumber;
         
                Object offsetObject = helper
                        .getWriteOffset(sinkPort, sinkChannelNumber);
                if (offsetObject instanceof Integer) {
                    int offset = ((Integer) offsetObject).intValue();
                    offset = (offset + rate) % helper.getBufferSize
                            (sinkPort, sinkChannelNumber);
                    helper.setWriteOffset(sinkPort, 
                            sinkChannelNumber, new Integer(offset));
                } else {
                    int modulo = helper.getBufferSize
                            (sinkPort, sinkChannelNumber) - 1;
                    String offsetVariable = (String) helper.getWriteOffset
                            (sinkPort, sinkChannelNumber);
                    code.append((String) offsetVariable + " = (" + offsetVariable 
                            + " + " + rate + ")&" + modulo + ";\n");
                }
            }
        }    
    }

    ////////////////////////////////////////////////////////////////////
    ////                     protected variables                    ////

    /** The code generator containing this director helper.
     */
    protected CodeGenerator _codeGenerator;

    ////////////////////////////////////////////////////////////////////
    ////                     private variables                      ////

    /** The associate director.
     */
    private ptolemy.actor.Director _director;
}
