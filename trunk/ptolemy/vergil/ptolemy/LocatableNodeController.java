/* The node controller for locatable nodes

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

package ptolemy.vergil.ptolemy;

// FIXME: Replace with per-class imports.
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;

import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.GraphController;
import diva.graph.NodeController;
import diva.graph.NodeInteractor;
import diva.graph.BasicNodeController;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.util.Filter;

import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeController
/**
This node controller provides interaction techniques for nodes that are
locations.   This is common when the node has some
concept of its graphical location, but does not know about the figure that it
is associated with.  This class provides the connection between the
figure's notion of location and the node's concept of location.
<p>
When nodes are drawn, they are automatically placed at the
coordinate given by the Location.  A LocatableNodeDragInteractor
is used to update the location of the node as the figure moves.

@author Steve Neuendorffer
@version $Id$
*/
public class LocatableNodeController extends BasicNodeController {

    public LocatableNodeController(GraphController controller) {
	super(controller);
        NodeInteractor nodeInteractor = (NodeInteractor) getNodeInteractor();
        nodeInteractor.setDragInteractor(new LocatableNodeDragInteractor(this));
    }

    /** Add a node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        throw new UnsupportedOperationException("Cannot add node.");
    }

    /** Draw the node at its location.
     */
    public Figure drawNode(Object node) {
        Figure nf = super.drawNode(node);
	locateFigure(node);
        return nf;
    }

    /** Return the desired location of this node.  Throw an exception if the
     *  node does not have a desired location.
     */
    public double[] getLocation(Object node) {
        if(hasLocation(node)) {
            return ((Location) node).getLocation();
        } else throw new RuntimeException("The node " + node +
                "does not have a desired location");
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Location.
     */
    public boolean hasLocation(Object node) {
        if(node instanceof Location) {
            Location object = (Location) node;
            double[] location = object.getLocation();
            if(location != null) return true;
        }
        return false;
    }

    /** Move the node's figure to the location specified in the node's
     *  semantic object, if that object is an instance of Location.
     *  If the semantic object is not a location, then do nothing.
     */
    public void locateFigure(Object node) {
	Figure nf = getController().getFigure(node);
	if(hasLocation(node)) {
	    double[] location = getLocation(node);
	    CanvasUtilities.translateTo(nf, location[0], location[1]);
        }
    }

    /** Set the desired location of this node.  Throw an exception if the
     *  node can not be given a desired location.
     */
    public void setLocation(Object node, double[] location)
            throws IllegalActionException {
	if(node instanceof Location) {
            ((Location)node).setLocation(location);
        } else throw new RuntimeException("The node " + node +
                "can not have a desired location");
    }
}
