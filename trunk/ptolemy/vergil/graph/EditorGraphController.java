/* The graph controller for vergil

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

package ptolemy.vergil.graph;

// FIXME: Replace with per-class imports.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import ptolemy.vergil.VergilApplication;

import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.GraphPane;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.util.Filter;
import diva.util.java2d.Polygon2D;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EditorGraphController
/**
A Graph Controller for the Ptolemy II schematic editor.  In addition to the
interaction allowed in the viewer, this controller allows nodes to be
dragged and dropped onto its graph.  Relations can be created by
control-clicking on the background.  Links can be created by control-clicking 
and dragging on a port or a relation.  In addition links can be created by
clicking and dragging on the ports that are inside an entity.
Anything can be deleted by selecting it and pressing
the delete key on the keyboard.

@author Steve Neuendorffer
@version $Id$
 */
public class EditorGraphController extends ViewerGraphController {

    /**
     * Create a new basic controller with default
     * terminal and edge interactors.
     */
    public EditorGraphController() {
	super();
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

        // Create a listener that creates new relations
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_relationCreator);

        // Create a listener that creates new terminals
	//_portCreator = new PortCreator();
        //_portCreator.setMouseFilter(_controlFilter);
        //pane.getBackgroundEventLayer().addInteractor(_portCreator);

        // Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
	((CompositeInteractor)getPortController().getNodeInteractor()).addInteractor(_linkCreator);
        ((CompositeInteractor)getEntityPortController().getNodeInteractor()).addInteractor(_linkCreator);
	((CompositeInteractor)getRelationController().getNodeInteractor()).addInteractor(_linkCreator);

	LinkCreator linkCreator2 = new LinkCreator();
	linkCreator2.setMouseFilter(
           new MouseFilter(InputEvent.BUTTON1_MASK,0));
	((CompositeInteractor)getEntityPortController().getNodeInteractor()).addInteractor(linkCreator2);


        /*        // Create the interactor that drags new edges.
                  _connectedVertexCreator = new ConnectedVertexCreator();
                  _connectedVertexCreator.setMouseFilter(_shiftFilter);
                  getNodeInteractor().addInteractor(_connectedVertexCreator);
        */
    }

    ///////////////////////////////////////////////////////////////
    //// PortCreator

    protected class PortCreator extends ActionInteractor {
	public PortCreator() {
	    super(VergilApplication.getInstance().getAction("New External Port"));
	}
    }

    ///////////////////////////////////////////////////////////////
    //// RelationCreator

    protected class RelationCreator extends ActionInteractor {
	public RelationCreator() {
	    // FIXME don't ref VergilApplication.
	    super(VergilApplication.getInstance().getAction("New Relation"));
	}
    }
	
    ///////////////////////////////////////////////////////////////
    //// LinkCreator

    /** An interactor that interactively drags edges from one terminal
     * to another.
     */
    protected class LinkCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
	    NamedObj sourceObject = (NamedObj) source.getUserObject();
	
	    FigureLayer layer = (FigureLayer) e.getLayerSource();

	    // Create a new edge
	    CompositeEntity container = 
		(CompositeEntity)getGraphModel().getRoot();

	    Link link;
	    try {
                link = new Link(container, container.uniqueName("link"));
            }
            catch (Exception ex) {
		VergilApplication.getInstance().showError(
		    "Create relation failed:", ex);
		return;
	    }
	    // Add it to the editor
	    getLinkController().addEdge(link,
                    sourceObject,
                    ConnectorEvent.TAIL_END,
                    e.getLayerX(),
                    e.getLayerY());

	    // Add it to the selection so it gets a manipulator, and
	    // make events go to the grab-handle under the mouse
	    Figure ef = getFigure(link);
	    getSelectionModel().addSelection(ef);
	    ConnectorManipulator cm =
		(ConnectorManipulator) ef.getParent();
	    GrabHandle gh = cm.getHeadHandle();
	    layer.grabPointer(e, gh);
	}
    }



    /** An interactor that creates a new Vertex that is connected to a vertex
     *  in a relation
     
    protected class ConnectedVertexCreator extends AbstractInteractor {
	public void mousePressed(LayerEvent e) {
	    FigureLayer layer = (FigureLayer) e.getLayerSource();
	    Figure source = e.getFigureSource();
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj sourceObject = (NamedObj) sourcenode.getSemanticObject();

            if((sourceObject instanceof Vertex)) {
		Relation relation = (Relation)sourceObject.getContainer();
		Vertex vertex = null;
		try {
		    vertex = new Vertex(relation,
                            relation.uniqueName("vertex"));
                }
		catch (Exception ex) {
		    ex.printStackTrace();
		    throw new RuntimeException(ex.getMessage());
		}
		Node node = getGraphImpl().createNode(vertex);
		//addNode(node, e.getLayerX(), e.getLayerY());

		Edge edge = getGraphImpl().createEdge(null);
		//addEdge(edge,
		//	sourcenode,
		//ConnectorEvent.TAIL_END,
		//	e.getLayerX(),
                //e.getLayerY());

		// Add it to the selection so it gets a manipulator, and
		// make events go to the grab-handle under the mouse
		Figure nf = (Figure) node.getVisualObject();
		getSelectionModel().addSelection(nf);
		//		ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
		//GrabHandle gh = cm.getHeadHandle();
		layer.grabPointer(e, nf);
	    }
	}
    
    }
    */
    /** The interactor for creating new relations
     */
    private RelationCreator _relationCreator;

    /** The interactor for creating new vertecies connected
     *  to an existing relation
     */
    //   private ConnectedVertexCreator _connectedVertexCreator;

    /** The interactor for creating new terminals
     */
    private PortCreator _portCreator;

    /** The interactor for creating context sensitive menus.
     */
    private MenuCreator _menuCreator;

    /** The interactor that interactively creates edges
     */
    private LinkCreator _linkCreator;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations
     */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);
}
