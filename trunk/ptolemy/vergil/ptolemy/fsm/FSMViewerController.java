/* The graph controller for the vergil viewer

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.gui.*;
import ptolemy.moml.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.util.Filter;
import diva.util.java2d.Polygon2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.JPopupMenu;
import javax.swing.JLabel;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// FSMViewerController
/**
A graph controller for the Ptolemy II schematic viewer.
This controller allows nodes to be moved and context menus to be created,
but does not provide interaction for adding or removing nodes.
Right-clicking on the background will
create a context-sensitive menu for the graph.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMViewerController extends GraphController {
    /**
     * Create a new basic controller with default
     * terminal and edge interactors.
     */
    public FSMViewerController() {
	_stateController = new FSMStateController(this);
	_transitionController = new FSMTransitionController(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the controller for entities
     */
    public FSMStateController getStateController() {
	return _stateController;
    }

    /**
     * Return the controller for links
     */
    public FSMTransitionController getTransitionController() {
        return _transitionController;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
	SelectionDragger _selectionDragger = new SelectionDragger(pane);
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_stateController.getNodeInteractor());
	_selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_transitionController.getEdgeInteractor());

        MenuCreator _menuCreator = new MenuCreator(
	    new SchematicContextMenuFactory(this));
	pane.getBackgroundEventLayer().addInteractor(_menuCreator);

	pane.getBackgroundEventLayer().setConsuming(false);
    }

    /**
     * Return the node controller appropriate for the given node.
     */
    public NodeController getNodeController(Object node) {
	return _stateController;
    }

    /**
     * Return the edge controller appropriate for the given node.
     */
    public EdgeController getEdgeController(Object edge) {
        return _transitionController;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          inner classes                    ////
   
    public static class SchematicContextMenuFactory 
	extends PtolemyMenuFactory {
	public SchematicContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	}	
	
	public NamedObj _getObjectFromFigure(Figure source) {
	    return (NamedObj)getController().getGraphModel().getRoot();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;

    // The interactor for creating context sensitive menus on the
    // graph itself.
    private MenuCreator _menuCreator;

    // The controllers
    private FSMStateController _stateController;
    private FSMTransitionController _transitionController;
}
