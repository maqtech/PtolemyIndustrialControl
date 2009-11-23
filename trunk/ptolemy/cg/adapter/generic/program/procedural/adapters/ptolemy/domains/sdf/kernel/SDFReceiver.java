/* Code generator adapter for SDFReceiver.

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

package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.lib.ModularCodeGenTypedCompositeActor;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////////
////SDFReceiver

/** The adapter for SDF recevier.
*  @author Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 7.1
*  @Pt.ProposedRating Red (jiazou)
*  @Pt.AcceptedRating Red (jiazou)
*/
public class SDFReceiver extends Receiver {

    /** Construct an adapther for an SDF receiver.
     *  @param The SDFReceiver for which an adapther is constructed.
     *  @exception If thrown by the superclass.
     */
    public SDFReceiver(ptolemy.domains.sdf.kernel.SDFReceiver receiver)
            throws IllegalActionException {
        super(receiver);
        // FIXME: not sure if this is totally correct.
        if (receiver.getContainer().getContainer() instanceof CompositeActor) {
            _forComposite = true;
        }
    }

    /** Generates code for getting tokens from the receiver.
     *  @param offset The offset of the port.
     *  @return The generated get code.
     *  @exception IllegalActionException If thrown while getting the component,
     *  getting the adapter, getting the director or getting the port reference.
     */
    public String generateGetCode(String offset) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort)getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        NamedProgramCodeGeneratorAdapter containingActorAdapter = (NamedProgramCodeGeneratorAdapter)
            getAdapter(getComponent().getContainer().getContainer());
        
        return _getDirectorForReceiver().getReference(port, new String[]{
                Integer.toString(channel), offset}, 
                _forComposite, false, containingActorAdapter);
    }

    /** Generates code to check the receiver has token.
     *  @return The generated hasToken code, in this class, the string "true"
     *  is returned
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateHasTokenCode(String offset) throws IllegalActionException {
        return "true"; // Assume "true" is a defined constant.
    }

    /** Generate code for putting tokens from the receiver.
     *  @param sourcePort The source port.
     *  @param offset The offset of the port.
     *  @param token The token.    
     *  @return The generated put code.
     *  @exception IllegalActionException If thrown while getting the component,
     *  getting the adapter, getting the director or getting the port reference.
     */
    public String generatePutCode(IOPort sourcePort, String offset, String token) throws IllegalActionException {
        TypedIOPort port = (TypedIOPort)getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());
        NamedProgramCodeGeneratorAdapter containingActorAdapter = (NamedProgramCodeGeneratorAdapter)
            getAdapter(getComponent().getContainer().getContainer());

        // The source's channel as well as the offsetis irrelevant here because 
        // we use the token as the sourceRef instead.
        // The sink is actually also irrelevant, since we will get rid of it later.
        ProgramCodeGeneratorAdapter.Channel source = new Channel(sourcePort, 0);
        ProgramCodeGeneratorAdapter.Channel sink = new Channel(port, channel);
        token = ((NamedProgramCodeGeneratorAdapter)
            getAdapter(getComponent().getContainer().getContainer())).getTemplateParser()
            .generateTypeConvertStatement(source, sink, 0, token);
        
        token = _removeSink(token);

        boolean forComposite = _forComposite;
        if (getComponent().getContainer().getContainer() instanceof ModularCodeGenTypedCompositeActor
                && port.isInput()) {
            // If the container is a ModularCodeGenTypedCompositeActor
            // and the port is an input, then generate a reference
            // assuming that the we are not operating on a composite.
            // This code is needed for
            // $PTII/ptolemy/cg/lib/test/auto/ModularCodeGen2.xml,
            // which has nested ModularCodegen.
            forComposite = false;
        }
        return _getDirectorForReceiver().getReference(port, new String[]{Integer.toString(channel), offset}, 
                forComposite, true, containingActorAdapter) + "=" + token + ";" + _eol;
//        adapter.processCode("$ref(" + port.getName() + "#" + channel
//                + ")")
//                + " = " + token + ";" + _eol;
    }

    
    protected String _generateTypeConvertStatement(Channel source)
            throws IllegalActionException {
        
        Type sourceType = ((TypedIOPort) source.port).getType();
//        Type sinkType = ((TypedIOPort) getComponent().getContainer()).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }
        // FIXME: what do we do with offset?
//
//        // The references are associated with their own adapter, so we need
//        // to find the associated adapter.
//        String sourcePortChannel = source.port.getName() + "#"
//                + source.channelNumber + ", " + offset;
//        String sourceRef = (_getAdapter(source.port.getContainer()))
//                .getReference(sourcePortChannel);
//
//        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
//                + ", " + offset;
//
//        // For composite actor, generate a variable corresponding to
//        // the inside receiver of an output port.
//        // FIXME: I think checking sink.port.isOutput() is enough here.
//        if (sink.port.getContainer() instanceof CompositeActor
//                && sink.port.isOutput()) {
//            sinkPortChannel = "@" + sinkPortChannel;
//        }
//        String sinkRef = (_getAdapter(sink.port.getContainer())).getReference(
//                sinkPortChannel, true);
//
//        // When the sink port is contained by a modal controller, it is
//        // possible that the port is both input and output port. we need
//        // to pay special attention. Directly calling getReference() will
//        // treat it as output port and this is not correct.
//        // FIXME: what about offset?
//        if (sink.port.getContainer() instanceof ModalController) {
//            sinkRef = ProgramCodeGeneratorAdapter.generateName(sink.port);
//            if (sink.port.isMultiport()) {
//                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
//            }
//        }
//
//        String result = sourceRef;
//
//        String sourceCodeGenType = _codeGenerator.codeGenType(sourceType);
//        String sinkCodeGenType = _codeGenerator.codeGenType(sinkType);
//
//        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
//            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
//                    + "(" + result + ")";
//        }
//        return sinkRef + " = " + result + ";" + _eol;
//    }

        return null;
    }
    
    /** Each receiver is associated with a director, return that director.
     *  @return The director associated with this receiver.
     *  @exception IllegalActionException 
     *  
     *  FIXME: this is not exactly correct.
     */
    protected StaticSchedulingDirector _getDirectorForReceiver() throws IllegalActionException {
        return (StaticSchedulingDirector) super._getDirectorForReceiver();
    }

    //$send(port#channel) ==> port_channel[writeOffset]
    //$get(port#channel) ==> port_channel[readOffset]

    
    private boolean _forComposite;
    
}
