/* The graph controller for the ptolemy schematic editor ports

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.editor;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.schematic.util.*;
import ptolemy.schematic.xml.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*; 
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import diva.util.java2d.Polygon2D;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// EntityPortController
/**
 * A controller for ports of entities.  These ports cannot be dragged
 *
 * @author Steve Neuendorffer 
 * @version $Id$
 */

public class EntityPortController extends NodeController {
    public EntityPortController(GraphController controller) {
	super(controller);
	setNodeRenderer(new EntityPortRenderer());
	SelectionModel sm = controller.getSelectionModel();
	NodeInteractor interactor = new NodeInteractor(sm);
        interactor.setDragInteractor(null);
	setNodeInteractor(interactor);
	_menuCreator = new MenuCreator(new PortContextMenuFactory());
	interactor.addInteractor(_menuCreator);
    }
    

    public class EntityPortRenderer implements NodeRenderer {
	public Figure render(Node n) {
            Port port = (Port) n.getSemanticObject();
          
	    /*
	    StraightTerminal figure = new StraightTerminal();
            ConnectorEnd end = null;
            if(port instanceof IOPort) {
                IOPort ioport = (IOPort) port;
                if(ioport.isInput() || ioport.isOutput()) {
                    end = new Arrowhead();
                    if(ioport.isOutput())
                        ((Arrowhead)end).setFlipped(true);                
                } else {
                    // FIXME something else
                    end = new Blob();
                }
            } else {                
                end = new Blob();
            }

	    figure.setEnd(end);
	    */
	   
	    //	    Figure figure = new BasicRectangle(-2, -2, 4, 4, Color.black);
	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(-4, 4);
	    polygon.lineTo(4, 0);
	    polygon.lineTo(-4, -4);
	    polygon.closePath();
	    Figure figure = new BasicFigure(polygon, Color.black);
	    return figure;
	}
    }    

    /** Given a node, add it to the given parent.  
     */
    public void drawNode(Node node, CompositeNode parentNode, int direction, 
			double fraction) {
        // Create a figure for it
	//System.out.println("adding port");
	Figure nf = getNodeRenderer().render(node);
        nf.setInteractor(getNodeInteractor());
        nf.setUserObject(node);
        node.setVisualObject(nf);
        CompositeFigure parentFigure = 
	    (CompositeFigure)parentNode.getVisualObject();
	BoundsSite site = 
	    new BoundsSite(parentFigure.getBackgroundFigure(), 0, direction, fraction);
        //nf.setAttachSite(site);
	nf.translate(site.getX() - parentFigure.getBackgroundFigure().getBounds().getX(),
                site.getY() - parentFigure.getBackgroundFigure().getBounds().getY());
	parentFigure.add(nf);
        
        // Add to the graph
	//getController().getGraphImpl().addNode(node, parentNode);
    }
    
    /**
     * The factory for creating context menus on entities.
     */
    public class PortContextMenuFactory extends MenuFactory {
	public JPopupMenu create(Figure source) {
	    Node sourcenode = (Node) source.getUserObject();
	    NamedObj object = (NamedObj) sourcenode.getSemanticObject();
	    return new Menu(object);
	}    
	
	public class Menu extends BasicContextMenu {
	    public Menu(NamedObj target) {	    
		super(target);
		if(target instanceof IOPort) {
		    IOPort port = (IOPort)target;
		    JCheckBox checkBox;
		    checkBox = new JCheckBox("Input", port.isInput());
		    add(checkBox);
		    checkBox = new JCheckBox("Output", port.isOutput());
		    add(checkBox);
		    checkBox = new JCheckBox("Multiport", port.isMultiport());
		    add(checkBox);		
		}
	    }
	}
    }

    MenuCreator _menuCreator;
}
