/* The edge controller for links between ports and relations.

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
import ptolemy.gui.*;
import ptolemy.moml.*;
import ptolemy.vergil.*;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import diva.util.java2d.Polygon2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;

import javax.swing.JPopupMenu;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// FSMTransitionController
/**
This class provides interaction techniques for edges that are to be connected
between ports and relations.  Standard interaction techniques for an
undirected edge are allowed.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMTransitionController extends EdgeController {
    public FSMTransitionController(GraphController controller) {
	super(controller);
	// Create and set up the target for connectors
	// This is wierd...  we want 2 targets, one for head and port,
	// one for tail and vertex.
	ConnectorTarget ct = new LinkTarget();
	setConnectorTarget(ct);
	setEdgeRenderer(new LinkRenderer());

	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getEdgeInteractor();
	interactor.setSelectionModel(sm);

	//    MouseFilter handleFilter = new MouseFilter(1, 0, 0);
	//manipulator.setHandleFilter(handleFilter);

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the menu factory that will create context menus for this
     *  controller.
     */
    public MenuFactory getMenuFactory() {
        return _menuCreator.getMenuFactory();
    }

    /** Set the menu factory that will create menus for this Entity.
     */
    public void setMenuFactory(MenuFactory factory) {
        _menuCreator.setMenuFactory(factory);
    }

    public class LinkTarget extends PerimeterTarget {
        public boolean accept(Figure f) {
            Object object = f.getUserObject();
	    if(object instanceof Icon) return true;
	    return false;
        }

        public Site getHeadSite(Connector c, Figure f, double x, double y) {
            Object object = f.getUserObject();
	    Site site = new PerimeterSite(f, 0);
	    return site;
	}  
        
        public Site getTailSite(Connector c, Figure f, double x, double y) {
            Object object = f.getUserObject();
	    Site site = new PerimeterSite(f, 0);
	    return site;
	}
    }

    public class LinkRenderer implements EdgeRenderer {
	/**
         * Render a visual representation of the given edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            AbstractConnector c = new ArcConnector(tailSite, headSite);
            c.setHeadEnd(new Arrowhead());
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);
            return c;
        }
    }
    private MenuCreator _menuCreator;
}
