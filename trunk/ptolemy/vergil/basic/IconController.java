/* The node controller for objects with icons.

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

package ptolemy.vergil.basic;

import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.moml.Location;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// IconController
/**
This class provides interaction with nodes that represent Ptolemy II
objects that are represented on screen as icons, such as attributes
and entities.   It provides a double click binding to edit the parameters
of the node, and a context menu containing a command to edit parameters
("Configure"). This adds to the base class the ability to render an
icon for the object being controlled, where the icon is specified
by a contained attribute of class EditorIcon (typically, but not
necessily named "_icon").

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class IconController extends ParameterizedNodeController {

    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public IconController(GraphController controller) {
	super(controller);
	setNodeRenderer(new IconRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** An icon renderer. */
    public static class IconRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();

	    // NOTE: this code is similar to that in PtolemyTreeCellRenderer
            Figure result = null;
            try {
                List iconList = object.attributeList(EditorIcon.class);
                if (iconList.size() == 0) {
		    EditorIcon icon = new XMLIcon(object, "_icon");
                    result = icon.createFigure();
                } else if (iconList.size() == 1) {
                    EditorIcon icon = (EditorIcon)iconList.iterator().next();
                    result = icon.createFigure();
                } else {
                    // There are multiple figures.
                    Iterator icons = iconList.iterator();
                    result = new CompositeFigure();
                    while (icons.hasNext()) {
                        EditorIcon icon = (EditorIcon)icons.next();
                        ((CompositeFigure)result).add(icon.createFigure());
                    }
                }
	    } catch (KernelException ex) {
		throw new InternalErrorException("could not create icon " +
                        "in " + object + " even " +
                        "though one did not previously exist");
	    }
            result.setToolTipText(object.getClass().getName());
	    return result;
	}
    }
}
