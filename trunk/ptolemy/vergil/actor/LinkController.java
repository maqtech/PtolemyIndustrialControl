/* The edge controller for links.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.vergil.actor;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.ManhattanConnector;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Terminal;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.Manipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.BasicEdgeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.gui.toolbox.MenuCreator;
import diva.gui.toolbox.MenuFactory;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.moml.Location;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;

//////////////////////////////////////////////////////////////////////////
//// LinkController
/**
This class provides interaction techniques for edges that are to be connected
between ports and relations.  Standard interaction techniques for an
undirected edge are allowed.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class LinkController extends BasicEdgeController {

    /** Create a link controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public LinkController(final GraphController controller) {
	super(controller);
	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getEdgeInteractor();
	interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

	ConnectorTarget ct = new LinkTarget();
	setConnectorTarget(ct);
	setEdgeRenderer(new LinkRenderer());

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.
        _menuFactory = new PtolemyMenuFactory(controller);
        _menuFactory.addMenuItemFactory(new EditParametersFactory("Configure"));
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        Action action = new AbstractAction("Configure") {
	    public void actionPerformed(ActionEvent e) {
                LayerEvent event = (LayerEvent)e.getSource();
                Figure figure = event.getFigureSource();
                Object object = figure.getUserObject();
                GraphModel graphModel = controller.getGraphModel();
                NamedObj target =
                         (NamedObj)graphModel.getSemanticObject(object);
                // Create a dialog for configuring the object.
                Component pane = controller.getGraphPane().getCanvas();
                while (pane.getParent() != null) {
                    pane = pane.getParent();
                }
                if (pane instanceof Frame) {
                    // The first argument below is the parent window
                    // (a Frame), which ensures that if this is iconified
                    // or sent to the background, it can be found again.
                    new EditParametersDialog((Frame)pane, target);
                } else {
                    new EditParametersDialog(null, target);
                }
	    }
	};
        ActionInteractor doubleClickInteractor = new ActionInteractor(action);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The configuration. */
    protected Configuration _configuration;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public class LinkTarget extends PerimeterTarget {
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();
	    if (object instanceof Port) return super.acceptHead(c, f);
	    if (object instanceof Vertex) return super.acceptHead(c, f);
	    if (object instanceof Location &&
                    ((Location)object).getContainer() instanceof Port)
		return super.acceptHead(c, f);
	    return false;
        }

        public boolean acceptTail(Connector c, Figure f) {
            Object object = f.getUserObject();
	    if (object instanceof Port) return super.acceptTail(c, f);
	    if (object instanceof Vertex) return super.acceptTail(c, f);
	    if (object instanceof Location &&
                    ((Location)object).getContainer() instanceof Port)
		return super.acceptHead(c, f);
	    return false;
        }

        public Site getHeadSite(Figure f, double x, double y) {
            if (f instanceof Terminal) {
		Site site = ((Terminal)f).getConnectSite();
		return site;
            } else {
                return super.getHeadSite(f, x, y);
            }
        }
	// Tail sites are the same as head sites.
    }

    public static class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            ManhattanConnector c = new ManhattanConnector(tailSite, headSite);
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);
            // The default bend radius of 50 is too large...
            // parallel curves look bad.
            c.setBendRadius(20);

	    Link link = (Link)edge;
	    Relation relation = link.getRelation();
	    if (relation != null) {
		c.setToolTipText(relation.getName());
	    }
            return c;
        }
    }

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class LinkDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            ActorGraphModel model =
		(ActorGraphModel) getController().getGraphModel();
	    switch (evt.getEnd()) {
	    case ConnectorEvent.HEAD_END:
		model.getLinkModel().setHead(edge, node);
		break;
	    case ConnectorEvent.TAIL_END:
		model.getLinkModel().setTail(edge, node);
		break;
	    default:
		throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
	    }
        }
    }
}
