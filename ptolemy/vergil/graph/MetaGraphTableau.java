/* A simple graph view for meta graphs

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.graph;

import diva.graph.*;
import ptolemy.actor.gui.*;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import ptolemy.vergil.ptolemy.GraphFrame;

import java.awt.Color;
import java.net.*;


//////////////////////////////////////////////////////////////////////////
//// MetaGraphTableau
/**

@author  Steve Neuendorffer
@version $Id$
*/
public class MetaGraphTableau extends Tableau {

    public MetaGraphTableau(PtolemyEffigy container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        NamedObj model = ((PtolemyEffigy)getContainer()).getModel();
        if (!(model instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Cannot graphically edit a model that is not a CompositeEntity.");
        }
    }

    public void show() {
        if(getFrame() == null) {
            CompositeEntity entity = (CompositeEntity)
                ((PtolemyEffigy)getContainer()).getModel();
                   
            MetaGraphFrame frame = new MetaGraphFrame(entity, this);
                
            setFrame(frame);
            frame.setBackground(BACKGROUND_COLOR);
        } 
        super.show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The background color.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
	/** Create an factory with the given name and container.
	 *  The container argument must not be null, or a
	 *  NullPointerException will be thrown.  This entity will use the
	 *  workspace of the container for synchronization and version counts.
	 *  If the name argument is null,
	 *  then the name is set to the empty string.
	 *  Increment the version of the workspace.
	 *  @param container The container entity.
	 *  @param name The name of the entity.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
            new MetaGraphController(this, "graphController");
	}

	/** Create a tableau in the default workspace with no name for the
	 *  given Effigy.  The tableau will created with a new unique name
	 *  in the given model effigy.  If this factory cannot create a tableau
	 *  for the given effigy (perhaps because the effigy is not of the
	 *  appropriate subclass) then return null.
         *  It is the responsibility of callers of this method to check the
         *  return value and call show().
	 *
	 *  @param effigy The model effigy.
	 *  @return A new MetaGraphTableau, if the effigy is a
	 *  PtolemyEffigy, or null otherwise.
	 *  @exception Exception If an exception occurs when creating the
	 *  tableau.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
	    if(effigy instanceof PtolemyEffigy) {
                MetaGraphTableau tableau =
                    (MetaGraphTableau)effigy.getEntity("Meta Tableau");
                if (tableau == null) {
                    tableau = new MetaGraphTableau(
                            (PtolemyEffigy)effigy, "Meta Tableau");
                }
                MetaGraphController controller = (MetaGraphController)
                    getEntity("graphController");
                ((MetaGraphController)controller.clone()).setContainer(tableau);
                // Don't call show() here, it is called for us in
		// TableauFrame.ViewMenuListener.actionPerformed()
                return tableau;
	    } else {
		return null;
	    }
	}
    }

    public static class MetaGraphFrame extends GraphFrame {        
        public MetaGraphFrame(CompositeEntity entity, Tableau tableau) {
            super(entity, tableau);
        }

        /** Create a new graph pane.
         */
        protected GraphPane _createGraphPane() {
            // hack
            try {
                /*                MoMLParser parser = new MoMLParser();
                URL url = getClass().getResource("graph.xml");
                CompositeEntity metaModel = 
                    (CompositeEntity)parser.parse(url, url);
                */
                _controller = (GraphController)
                    getTableau().getEntity("graphController");
                
                diva.graph.basic.BasicGraphModel graphModel = 
                    new diva.graph.basic.BasicGraphModel();
                Object node = graphModel.createNode(new Object());
                graphModel.addNode(this, node, graphModel.getRoot());
                node = graphModel.createNode(new Object());
                graphModel.addNode(this, node, graphModel.getRoot());
                
                GraphPane pane = new GraphPane(_controller, graphModel);
                return pane;
            } catch (Exception ex) {
                MessageHandler.error("Could not parse graph type description",
                        ex);
                return null;
            }
        }
        private GraphController _controller;
    }
}
