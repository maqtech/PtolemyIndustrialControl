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
import diva.gui.*;
import diva.graph.*;
import diva.graph.model.*;
import ptolemy.vergil.graph.*;


/**
 * A visual notation creates views for a ptolemy document in Vergil.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class PtolemyNotation extends Attribute implements VisualNotation {
    /** Construct a graph document that is owned by the given
     *  application
     */
    public GraphPane createView(Document d) {
	if(!(d instanceof PtolemyDocument)) {
	    throw new InternalErrorException("Ptolemy Notation is only " +
		"compatible with Ptolemy documents.");
	}

	// These two things control the view of a ptolemy model.
	GraphController controller = new EditorGraphController();
	GraphImpl impl = new VergilGraphImpl();

	GraphPane pane = new GraphPane(controller, impl);
	CompositeEntity entity =
	    (CompositeEntity) ((PtolemyDocument)d).getModel();
	Graph graph = impl.createGraph(entity);
	controller.setGraph(graph);
	return pane;
    }
}
