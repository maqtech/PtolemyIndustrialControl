/* Refinement for modal models.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.modal;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.fsm.kernel.ContainmentExtender;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.RefinementActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

// NOTE: This is a combination of ModalController and CTStepSizeControlActor,
// but because of the inheritance hierarchy, there appears to be no convenient
// way to share the code.
//////////////////////////////////////////////////////////////////////////
//// Refinement

/**
 This typed composite actor supports mirroring of its ports in its container
 (which is required to be a ModalModel), which in turn assures
 mirroring of ports in each of the refinements and the controller.
 Refinements fulfills the CTStepSizeControlActor interface so that
 it can be used to construct hybrid systems using the CT domain.
 Refinements also fulfills the CTEventGenerator interfact so that
 it can report events generated inside.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Refinement extends CTCompositeActor implements RefinementActor {
    /** Construct a modal controller with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Refinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.domains.fsm.modal.Refinement");

        new ContainmentExtender(this, "_containmentExtender");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a refinement for the given state.
     *
     *  @param state The state that will contain the new refinement.
     *  @param name The name of the composite entity that stores the refinement.
     *  @param template The template used to create the refinement, or null if
     *   template is not used.
     *  @param className The class name for the refinement, which is used when
     *   template is null.
     *  @param configuration The configuration that is used to open the
     *   refinement (as a look-inside action) after it is created, or null if it
     *   is not needed to open the refinement.
     *  @throws IllegalActionException If error occurs while creating the
     *   refinement.
     */
    public static void addRefinement(State state, final String name,
            Entity template, String className,
            final Configuration configuration) throws IllegalActionException {
        Attribute allowRefinement = state.getAttribute("_allowRefinement");
        if (allowRefinement instanceof Parameter &&
                !((BooleanToken) ((Parameter) allowRefinement).getToken())
                        .booleanValue()) {
            throw new IllegalActionException(state, "State does not support " +
                    "refinement.");
        }

        // Check that all these containers exist.
        Nameable immediateContainer = state.getContainer();

        if (immediateContainer == null) {
            throw new IllegalActionException(state, "State has no container!");
        }

        final CompositeEntity container = (CompositeEntity) immediateContainer
                .getContainer();

        if (container == null) {
            throw new IllegalActionException(state, "State container has no " +
                    "container!");
        }

        if (container.getEntity(name) != null) {
            throw new IllegalActionException(state, "There is already a " +
                    "refinement with name " + name + ".");
        }

        String currentRefinements = state.refinementName.getExpression();

        if ((currentRefinements == null) || currentRefinements.equals("")) {
            currentRefinements = name;
        } else {
            currentRefinements = currentRefinements.trim() + ", " + name;
        }

        String moml;

        // The MoML we create depends on whether the configuration
        // specified a set of prototype refinements.
        if (template != null) {
            String templateDescription = template.exportMoML(name);
            moml = "<group>" + templateDescription + "<entity name=\""
                    + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements + "\"/></entity></group>";
        } else {
            moml = "<group><entity name=\"" + name + "\" class=\""
                    + className + "\"/>" + "<entity name=\""
                    + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements + "\"/></entity></group>";
        }

        MoMLChangeRequest change = new MoMLChangeRequest(state, container,
                moml) {
            protected void _execute() throws Exception {
                super._execute();

                // Mirror the ports of the container in the refinement.
                // Note that this is done here rather than as part of
                // the MoML because we have set protected variables
                // in the refinement to prevent it from trying to again
                // mirror the changes in the container.
                Entity entity = container.getEntity(name);

                // Get the initial port configuration from the container.
                Iterator ports = container.portList().iterator();

                while (ports.hasNext()) {
                    Port port = (Port) ports.next();

                    try {
                        // NOTE: This is awkward.
                        if (entity instanceof Refinement) {
                            ((Refinement) entity).setMirrorDisable(true);
                        } else if (entity instanceof ModalController) {
                            ((ModalController) entity).setMirrorDisable(true);
                        }

                        String name = port.getName();
                        Port newPort = entity.getPort(name);
                        if (newPort == null) {
                            newPort = entity.newPort(port.getName());
                        }

                        if (newPort instanceof RefinementPort
                                && port instanceof IOPort) {
                            try {
                                ((RefinementPort) newPort).setMirrorDisable(
                                        true);

                                if (((IOPort) port).isInput()) {
                                    ((RefinementPort) newPort).setInput(true);
                                }

                                if (((IOPort) port).isOutput()) {
                                    ((RefinementPort) newPort).setOutput(true);
                                }

                                if (((IOPort) port).isMultiport()) {
                                    ((RefinementPort) newPort).setMultiport(
                                            true);
                                }

                                /* No longer needed since Yuhong modified
                                 * the type system to allow UNKNOWN. EAL
                                 if (port instanceof TypedIOPort
                                 && newPort instanceof TypedIOPort) {
                                 ((TypedIOPort)newPort).setTypeSameAs(
                                 (TypedIOPort)port);
                                 }
                                 */

                                // Copy the location to the new port if any.
                                // (tfeng 08/29/08)
                                if (container instanceof ModalModel) {
                                    FSMActor controller =((ModalModel)
                                            container).getController();
                                    if (controller != null &&
                                            controller != container) {
                                        Port controllerPort = controller
                                                .getPort(port.getName());
                                        if (controllerPort != null) {
                                            Location location = (Location)
                                                    controllerPort.getAttribute(
                                                            "_location",
                                                            Location.class);
                                            if (location != null) {
                                                location = (Location)
                                                        location.clone(
                                                        newPort.workspace());
                                                location.setContainer(newPort);
                                            }
                                        }
                                    }
                                }
                            } finally {
                                ((RefinementPort) newPort).setMirrorDisable(
                                        false);
                            }
                        }
                    } finally {
                        // NOTE: This is awkward.
                        if (entity instanceof Refinement) {
                            ((Refinement) entity).setMirrorDisable(false);
                        } else if (entity instanceof ModalController) {
                            ((ModalController) entity).setMirrorDisable(false);
                        }
                    }
                }

                if (configuration != null) {
                    // Look inside.
                    configuration.openInstance(entity);
                }
            }
        };

        container.requestChange(change);
    }

    /** Get the state in any ModalController within this ModalModel that has
     *  this refinement as its refinement, if any. Return null if no such state
     *  is found.
     *
     *  @return The state with this refinement as its refinement, or null.
     *  @exception IllegalActionException If the specified refinement cannot be
     *   found in a state, or if a comma-separated list is malformed.
     */
    public State getRefinedState() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof ModalModel) {
            List<?> controllers = ((ModalModel) container).entityList(
                    ModalController.class);
            for (Object controllerObject : controllers) {
                ModalController controller = (ModalController) controllerObject;
                List<?> states = controller.entityList(State.class);
                for (Object stateObject : states) {
                    State state = (State) stateObject;
                    TypedActor[] refinements = state.getRefinement();
                    if (refinements != null) {
                        for (TypedActor refinement : refinements) {
                            if (refinement == this) {
                                return state;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Create a new port with the specified name in the container of
     *  this refinement, which in turn creates a port in this refinement
     *  all other refinements, and the controller.
     *  This method is write-synchronized on the workspace.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || (getContainer() == null)) {
                // Have already called newPort() in the container.
                // This time, process the request.
                RefinementPort port = new RefinementPort(this, name);

                // NOTE: Changed RefinementPort so mirroring
                // is enabled by default. This means mirroring
                // will occur during MoML parsing, but this
                // is harmless. EAL 12/04.
                // port._mirrorDisable = false;
                // Create the appropriate links.
                ModalModel container = (ModalModel) getContainer();

                if (container != null) {
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);

                    if (relation == null) {
                        relation = container.newRelation(relationName);

                        Port containerPort = container.getPort(name);
                        containerPort.link(relation);
                    }

                    port.link(relation);
                }

                return port;
            } else {
                _mirrorDisable = true;
                ((ModalModel) getContainer()).newPort(name);
                return getPort(name);
            }
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "Refinement.newPort: Internal error: " + ex.getMessage());
        } finally {
            _mirrorDisable = false;
            _workspace.doneWriting();
        }
    }

    /** Control whether adding a port should be mirrored in the modal
     *  model and the mode controller.
     *  This is added to allow control by the UI.
     *  @param disable True if mirroring should not occur.
     */
    public void setMirrorDisable(boolean disable) {
        _mirrorDisable = disable;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a ModalModel or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof ModalModel) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "Refinement can only be contained by "
                            + "ModalModel objects.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    // These are protected to be accessible to ModalModel.

    /** Indicator that we are processing a newPort request. */
    protected boolean _mirrorDisable = false;
}
