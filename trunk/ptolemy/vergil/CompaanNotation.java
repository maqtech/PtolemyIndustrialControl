/* A generic visual notation for all Ptolemy models.

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

package ptolemy.vergil;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.gui.*;
import diva.graph.*;
import diva.graph.model.*;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import java.awt.Color;
import java.awt.geom.*;

/**
 * A visual notation creates views for a ptolemy document in Vergil.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class CompaanNotation extends Attribute implements VisualNotation {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public CompaanNotation() {
	super();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public CompaanNotation(Workspace workspace) {
	super(workspace);
        setMoMLElementName("notation");
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.

     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CompaanNotation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a graph document that is owned by the given
     *  application
     */
    public GraphPane createView(Document d) {
	if(!(d instanceof PtolemyDocument)) {
	    throw new InternalErrorException("Ptolemy Notation is only " +
		"compatible with Ptolemy documents.");
	}

	// These two things control the view of a ptolemy model.
	EditorGraphController controller = new EditorGraphController();
	controller.getEntityController().setNodeRenderer(new CompaanEntityRenderer());
	GraphImpl impl = new VergilGraphImpl();

	GraphPane pane = new GraphPane(controller, impl);
	CompositeEntity entity =
	    (CompositeEntity) ((PtolemyDocument)d).getModel();
	Graph graph = impl.createGraph(entity);
	controller.setGraph(graph);
	return pane;
    }

    public class CompaanEntityRenderer implements NodeRenderer {
	public Figure render(Node n) {
	    CompositeFigure figure;
	    EditorIcon icon = (EditorIcon)n.getSemanticObject();
	    figure = (CompositeFigure)icon.createFigure();
	    Rectangle2D bounds = figure.getBounds();
	    Entity entity = (Entity)icon.getContainer();
	    // Insert code to find color here.

	    figure.setBackgroundFigure(new BasicRectangle(0, 0, 
	        bounds.getWidth(), bounds.getHeight(), Color.blue));	
	    return figure;
	}
    }
}
