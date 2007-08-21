/* An actor that executes compiled embedded C code.

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.actor.lib.jni;

import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.toolbox.TextEditorConfigureFactory;

//////////////////////////////////////////////////////////////////////////
////EmbeddedCActor

/**
 An actor of this class executes compiled embedded C Code.
 To use it, double click on the actor and insert C code into
 the code templates, as indicated by the sample template.
 Normally you will also need to add ports to the actor.
 You may need to set the types of these ports as well.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class EmbeddedCActor extends CompiledCompositeActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>embeddedCCode</i> parameter, and initialize
     *  it to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public EmbeddedCActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        embeddedCCode = new StringAttribute(this, "embeddedCCode");

        // Set the visibility to expert, as casual users should
        // not see the C code.  This is particularly true if one
        // installs an actor that is an instance of this with 
        // particular C code in the library.
        embeddedCCode.setVisibility(Settable.EXPERT);

        // initialize the code to provide a template for identity function:
        //
        // /***fileDependencies***/
        // /**/
        //
        // /***preinitBlock***/
        // /**/
        //
        // /***initBlock***/
        // /**/
        //
        // /***fireBlock***/
        // // Assuming you have added an input port named "input"
        // // and an output port named "output", then the following
        // // line results in the input being copied to the output.
        // $ref(output) = $ref(input);   
        // /**/
        //
        // /***wrapupBlock***/
        // /**/
        //
        embeddedCCode.setExpression("/***fileDependencies***/\n"
                + "/**/\n\n"
                + "/***preinitBlock***/\n" 
                + "/**/\n\n"
                + "/***initBlock***/\n"
                + "/**/\n\n"
                + "/***fireBlock***/\n"
                + "// Assuming you have added an input port named \"input\"\n"
                + "// and an output port named \"output\", then the following\n"
                + "// line results in the input being copied to the output.\n"
                + "//$ref(output) = $ref(input);\n"
                + "/**/\n\n" 
                + "/***wrapupBlock***/\n"
                + "/**/\n\n");
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"62\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-29\" y=\"4\""
                + "style=\"font-size:10; fill:white; font-family:SansSerif\">"
                + "EmbeddedC</text>\n" + "</svg>\n");
        
        // For embeddedCActor, there is only C code specifying its
        // functionality.  Therefore JNI has to be invoked when it is
        // executed.
        invokeJNI.setExpression("true");
        
        new SDFDirector(this, "SDFDirector");
        
        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new TextEditorConfigureFactory(this, "_textEditorConfigureFactory")
                .attributeName.setExpression("embeddedCCode");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The C code that specifies the function of this actor.
     *  The default value provides a template for an identity function.
     */
    public StringAttribute embeddedCCode;

    public void preinitialize() throws IllegalActionException {
        try {
            _embeddedActor = new EmbeddedActor(this, "EmbeddedActor");
            
            int i = 0;
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                TypedIOPort port = (TypedIOPort) ports.next();
                TypedIOPort newPort = (TypedIOPort) port.clone(workspace());
                newPort.setContainer(_embeddedActor);
                TypedIORelation relation 
                        = new TypedIORelation(this, "relation" + i++); 
                port.link(relation);
                newPort.link(relation);
            }    
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, ex, "Name duplication.");
        } catch (CloneNotSupportedException ex) {
            throw new IllegalActionException(this, ex, "Clone not supported.");
        } 
        super.preinitialize();
    }
    
    public void wrapup() throws IllegalActionException {
        try {
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                TypedIOPort port = (TypedIOPort) ports.next();
                TypedIORelation relation 
                        = (TypedIORelation) port.insideRelationList().get(0);
                relation.setContainer(null);
            }
            _embeddedActor.setContainer(null);
        } catch (NameDuplicationException ex) {
            // should not happen.
            throw new IllegalActionException(this, "name duplication.");
        }   
        super.wrapup();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //FIXME: Note that the next block is not a javadoc, I changed /** to /* 
    /* If <i>embeddedCCode</i> is changed, compile the changed C Code. 
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If there is any error in evaluating
     *   the embedded C code.
     */
    /*
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == embeddedCCode) {
            _generateandCompileCCode();
        } else {
            super.attributeChanged(attribute);
        }
    }
    */
       
    private EmbeddedActor _embeddedActor = null;
    
    /** An actor inside the EmbeddedCActor that is used as a dummy
     *  placeholder.  The EmbeddedActor is created in preinitialize() where
     *  ports from the outer EmbeddedCActor are connected to the EmbeddedActor.
     *  The EmbeddedActor is destroyed in wrapup().   
     */
    public static class EmbeddedActor extends TypedAtomicActor {
        /** Create a new instance of EmbeddedActor.
         *  @param container The container.
         *  @param name The name of this actor within the container.
         *  @exception IllegalActionException If this actor cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public EmbeddedActor(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }
    }
}
