/* The node controller for hierarchical states.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.fsm.modal;

import diva.graph.GraphController;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.fsm.StateController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;

import javax.swing.Action;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// HierarchicalStateController
/**
This class provides interaction with nodes that represent hierarchical
states in an FSM graph. Hierarchical states are those with refinements,
and what this adds to the base class is the ability to add a new
refinement or remove a refinement via a context menu command.
The base class provides a double click binding to edit the parameters
of the state, and context menu commands to edit parameters ("Configure"),
rename, look inside, and get documentation.

@author Edward A. Lee
@version $Id$
*/
public class HierarchicalStateController extends StateController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public HierarchicalStateController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public HierarchicalStateController(
            GraphController controller, Access access) {
	super(controller, access);

        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new AddRefinementAction()));
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new RemoveRefinementAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to add a new refinement.
     */
    private class AddRefinementAction extends FigureAction {
	public AddRefinementAction() {
	    super("Add Refinement");
	}
	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    NamedObj target = getTarget();
            if (!(target instanceof State)) {
                MessageHandler.error("Can only add refinements to states.");
                return;
            }
            State state = (State)target;

            // Check that all these containers exist.
            Nameable immediateContainer = state.getContainer();
            if (immediateContainer == null) {
                MessageHandler.error("State has no container!");
                return;
            }
            final CompositeEntity container
                    = (CompositeEntity)immediateContainer.getContainer();
            if (container == null) {
                MessageHandler.error("State container has no container!");
                return;
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            String defaultName = container.uniqueName(state.getName());
            query.addLine("Name", "Name", defaultName);
            String[] choices = {"ptolemy.vergil.fsm.modal.Refinement",
                    "ptolemy.vergil.fsm.modal.ModalController"};
            query.addChoice("Class", "Class", choices,
                    "ptolemy.vergil.fsm.modal.Refinement", true);
            // FIXME: Need a frame owner for first arg.
            // Perhaps calling getController(), which returns a GraphController
            // will be a good start.
            ComponentDialog dialog = new ComponentDialog(null,
                    "Specify Refinement", query);
            if (!dialog.buttonPressed().equals("OK")) {
                return;
            }

            final String newName = query.stringValue("Name");
            if (container.getEntity(newName) != null) {
                MessageHandler.error("There is already a refinement with name "
                         + newName + ".");
                return;
            }

            String newClass = query.stringValue("Class");

            String currentRefinements
                    = state.refinementName.getExpression();
            if (currentRefinements == null || currentRefinements.equals("")) {
                currentRefinements = newName;
            } else {
                currentRefinements = currentRefinements.trim() + ", " + newName;
            }
            String moml = "<group><entity name=\""
                    + newName
                    + "\" class=\""
                    + newClass
                    + "\"/>"
                    + "<entity name=\""
                    + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + currentRefinements
                    + "\"/></entity></group>";
            MoMLChangeRequest change = new MoMLChangeRequest(
                    this, container, moml)  {
                protected void _execute() throws Exception {
                    super._execute();

                    // Mirror the ports of the container in the refinement.
                    // Note that this is done here rather than as part of
                    // the MoML because we have set protected variables
                    // in the refinement to prevent it from trying to again
                    // mirror the changes in the container.
                    Entity entity = container.getEntity(newName);

                    // Get the initial port configuration from the container.
                    Iterator ports = container.portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        try {
                            // NOTE: This is awkward.
                            if (entity instanceof Refinement) {
                                ((Refinement)entity)._mirrorDisable = true;
                            } else if (entity instanceof ModalController) {
                                ((ModalController)entity)._mirrorDisable = true;
                            }
                            Port newPort = entity.newPort(port.getName());
                            if (newPort instanceof RefinementPort
                                     && port instanceof IOPort) {
                                 try {
                                     ((RefinementPort)newPort)
                                             ._mirrorDisable = true;
                                     if (((IOPort)port).isInput()) {
                                         ((RefinementPort)newPort)
                                                  .setInput(true);
                                     }
                                     if (((IOPort)port).isOutput()) {
                                         ((RefinementPort)newPort)
                                                  .setOutput(true);
                                     }
                                     if (((IOPort)port).isMultiport()) {
                                         ((RefinementPort)newPort)
                                                  .setMultiport(true);
                                     }
                                     /* No longer needed since Yuhong modified
                                      * the type system to allow UNKNOWN. EAL
                                     if (port instanceof TypedIOPort
                                            && newPort instanceof TypedIOPort) {
                                         ((TypedIOPort)newPort).setTypeSameAs(
                                                 (TypedIOPort)port);
                                     }
                                     */
                                 } finally {
                                     ((RefinementPort)newPort)
                                             ._mirrorDisable = false;
                                 }
                            }
                        } finally {
                            // NOTE: This is awkward.
                            if (entity instanceof Refinement) {
                                ((Refinement)entity)._mirrorDisable = false;
                            } else if (entity instanceof ModalController) {
                                ((ModalController)entity)
                                         ._mirrorDisable = false;
                            }
                        }
                    }
                    if (_configuration != null) {
                        // Look inside.
                        _configuration.openModel(entity);
                    }
                }
            };
            container.requestChange(change);
	}
    }

    /** Action to remove refinements. */
    private class RemoveRefinementAction extends FigureAction {
	public RemoveRefinementAction() {
	    super("Remove Refinement");
	}
	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    NamedObj target = getTarget();
            if (!(target instanceof State)) {
                MessageHandler.error(
                        "Can only remove refinements from states.");
                return;
            }
            State state = (State)target;

            // Check that all these containers exist.
            CompositeEntity immediateContainer = (CompositeEntity)
                    state.getContainer();
            if (immediateContainer == null) {
                MessageHandler.error("State has no container!");
                return;
            }
            final CompositeEntity container
                    = (CompositeEntity)immediateContainer.getContainer();
            if (container == null) {
                MessageHandler.error("State container has no container!");
                return;
            }

            TypedActor[] refinements;
            try {
                refinements = state.getRefinement();
            } catch (Exception ex) {
                MessageHandler.error("Invalid refinements.", ex);
                return;
            }
            if (refinements == null || refinements.length < 1) {
                MessageHandler.error("No refinements to remove.");
                return;
            }
            String[] choices = new String[refinements.length];
            for (int i = 0; i<refinements.length; i++) {
                choices[i] = ((Nameable)refinements[i]).getName();
            }

            // Open a dialog to get the refinement name and class.
            Query query = new Query();
            query.addChoice("Refinement", "Refinement", choices,
                    choices[0], false);
            // FIXME: Need a frame owner for first arg.
            // Perhaps calling getController(), which returns a GraphController
            // will be a good start.
            ComponentDialog dialog = new ComponentDialog(null,
                    "Specify Refinement", query);
            if (!dialog.buttonPressed().equals("OK")) {
                return;
            }

            String refinementName = query.stringValue("Refinement");
            StringBuffer newRefinements = new StringBuffer();
            String currentRefinements
                    = state.refinementName.getExpression();
            StringTokenizer tokenizer
                    = new StringTokenizer(currentRefinements, ",");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (!token.trim().equals(refinementName)) {
                    if (newRefinements.length() > 0) {
                        newRefinements.append(", ");
                    }
                    newRefinements.append(token.trim());
                }
            }
            // Check to see whether any other state has
            // this refinment, and if not, remove it from its container.
            Iterator states = immediateContainer.entityList().iterator();
            boolean foundOne = false;
            while (states.hasNext()) {
                NamedObj other = (NamedObj)states.next();
                if (other != state && other instanceof State) {
                    String refinementList = ((State)other)
                            .refinementName.getExpression();
                    if (refinementList == null) continue;
                    tokenizer = new StringTokenizer(refinementList, ",");
                    while(tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        if (token.equals(refinementName)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if (foundOne) break;
                }
            }
            String removal = "";
            if (!foundOne) {
                removal = "<deleteEntity name=\"" + refinementName + "\"/>";
            }

            String moml = "<group><entity name=\""
                    + state.getName(container)
                    + "\"><property name=\"refinementName\" value=\""
                    + newRefinements.toString()
                    + "\"/></entity>"
                    + removal
                    + "</group>";
            MoMLChangeRequest change = new MoMLChangeRequest(
                    this, container, moml);
            container.requestChange(change);
	}
    }
}
