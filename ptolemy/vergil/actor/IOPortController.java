/* The node controller for ports contained in entities.

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

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.Polygon2D.Double;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.*;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// IOPortController
/**
This class provides interaction with nodes that represent Ptolemy II
ports on an actor.  It provides a double click binding and context
menu entry to edit the parameters of the port ("Configure") and a
command to get documentation.
It can have one of two access levels, FULL or PARTIAL.
If the access level is FULL, the the context menu also
contains a command to rename the node.
Note that whether the port is an input or output or multiport cannot
be controlled via this interface.  The "Configure Ports" command of
the container should be invoked instead.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class IOPortController extends AttributeController {

    /** Create a port controller associated with the specified graph
     *  controller.  The controller is given full access.
     *  @param controller The associated graph controller.
     */
    public IOPortController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a port controller associated with the
     *  specified graph controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public IOPortController(GraphController controller, Access access) {
	super(controller, access);
	setNodeRenderer(new EntityPortRenderer());

        // "Listen to Actor"
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new ListenToPortAction()));

	// Ports of entities do not use a selection interactor with
	// the same selection model as the rest of the first level figures.
	// If this were allowed, then the port would be able to be deleted.
	CompositeInteractor interactor = new CompositeInteractor();
 	setNodeInteractor(interactor);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the ports of components as triangles.  Multiports are
     *  rendered hollow, while single ports are rendered filled.
     */
    public class EntityPortRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    final Port port = (Port)n;

            // If the port has an attribute called "_hide", then
            // do not render it.
            if (port.getAttribute("_hide") != null) return null;

	    Polygon2D.Double polygon = new Polygon2D.Double();
	    polygon.moveTo(-4, 4);
	    polygon.lineTo(4, 0);
	    polygon.lineTo(-4, -4);
	    polygon.closePath();
            Color fill;
            float lineWidth = (float)1.5;
            if (port instanceof ParameterPort) {
                fill = Color.lightGray;
                lineWidth = (float)0.0;
            } else if (port instanceof IOPort && ((IOPort)port).isMultiport()) {
                fill = Color.white;
            } else {
                fill = Color.black;
            }

            ActorGraphModel model =
                (ActorGraphModel)getController().getGraphModel();

	    // Wrap the figure in a TerminalFigure to set the direction that
	    // connectors exit the port.  Note that this direction is the
	    // same direction that is used to layout the port in the
	    // Entity Controller.
            StringAttribute cardinal = (StringAttribute)port.getAttribute("_cardinal");

	    int direction;

// 	    if (!(port instanceof IOPort)) {
// 		direction = SwingUtilities.SOUTH;
// 	    } else if (((IOPort)port).isInput() && ((IOPort)port).isOutput()) {
// 		direction = SwingUtilities.SOUTH;
// 	    } else if (((IOPort)port).isInput()) {
// 		direction = SwingUtilities.WEST;
// 	    } else if (((IOPort)port).isOutput()) {
// 		direction = SwingUtilities.EAST;
// 	    } else {
// 		// should never happen
// 		direction = SwingUtilities.SOUTH;
// 	    }

          if ( cardinal == null && port instanceof IOPort ) 
          {
             if(((IOPort)port).isInput() && ((IOPort)port).isOutput()) {
		   direction = SwingUtilities.SOUTH;
               AffineTransform transform = new AffineTransform();
               transform.setToRotation( Math.toRadians( -90 ));
               polygon.transform( transform );
	       } else if(((IOPort)port).isInput()) {
		   direction = SwingUtilities.WEST;
	       } else if(((IOPort)port).isOutput()) {
		   direction = SwingUtilities.EAST;
	       } else {
		   // should never happen
		   direction = SwingUtilities.SOUTH;
               AffineTransform transform = new AffineTransform();
               transform.setToRotation( Math.toRadians( -90 ) );
               polygon.transform( transform );
	       }
          }
          else if ( port instanceof IOPort )
          {
             if ( cardinal.getExpression().equalsIgnoreCase("NORTH") ) {
                direction = SwingUtilities.NORTH;
                if ( ((IOPort)port).isInput() && !((IOPort)port).isOutput() ) {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( 90 ) );
                   polygon.transform( transform );
                }
                else {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( -90 ) );
                   polygon.transform( transform );
                }
             }
             else if ( cardinal.getExpression().equalsIgnoreCase("SOUTH") ) {
                direction = SwingUtilities.SOUTH;
                if ( ((IOPort)port).isInput() && !((IOPort)port).isOutput() ) {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( -90 ) );
                   polygon.transform( transform );
                }
                else {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( 90 ) );
                   polygon.transform( transform );
                }
             }
             else if ( cardinal.getExpression().equalsIgnoreCase("EAST") ) {
                direction = SwingUtilities.EAST;
                if ( ((IOPort)port).isInput() && !((IOPort)port).isOutput() ) {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( 180 ) );
                   polygon.transform( transform );
                }
             }
             else if ( cardinal.getExpression().equalsIgnoreCase("WEST") ) {
                direction = SwingUtilities.WEST;
                if ( ((IOPort)port).isOutput() && !((IOPort)port).isInput() ) {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( 180 ) );
                   polygon.transform( transform );
                }
             }
             else {// this shouldn't happen either
                direction = SwingUtilities.SOUTH;
                if ( ((IOPort)port).isInput() && !((IOPort)port).isOutput() ) {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( -90 ) );
                   polygon.transform( transform );
                }
                else {
                   AffineTransform transform = new AffineTransform();
                   transform.setToRotation( Math.toRadians( 90 ) );
                   polygon.transform( transform );
                }
             }
          } 
          else 
          {
	   	   direction = SwingUtilities.SOUTH;
               AffineTransform transform = new AffineTransform();
               transform.setToRotation( Math.toRadians( 90 ) );
               polygon.transform( transform );
	    }
            
	    Figure figure = new BasicFigure(polygon, fill, (float)1.5) {
                // Override this because we want to show the type.
                // It doesn't work to set it once because the type
                // has not been resolved, and anyway, it may change.
                public String getToolTipText() {
                    String tipText = port.getName();
                    if (port instanceof Typeable) {
                        try {
                            tipText = tipText + ", type:"
                                     + ((Typeable)port).getType();
                        } catch (IllegalActionException ex) {}
                    }
                    return tipText;
                }
            };
            // Have to do this also, or the awt doesn't display any
            // tooltip at all.

            figure.setToolTipText(port.getName());
	    double normal = CanvasUtilities.getNormal(direction);
	    Site tsite = new PerimeterSite(figure, 0);
	    tsite.setNormal(normal);
	    tsite = new FixedNormalSite(tsite);
	    figure = new TerminalFigure(figure, tsite);
	    return figure;
	}
    }

    // An action to listen to debug messages of the port.
    private class ListenToPortAction extends FigureAction {
        public ListenToPortAction() {
            super("Listen to Port");
        }
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot listen to port without a configuration.");
                return;
            }

            // Determine which entity was selected for the listen to
            // port action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            try {
                BasicGraphController controller =
                        (BasicGraphController)getController();
                BasicGraphFrame frame = controller.getFrame();
                Tableau tableau = frame.getTableau();

                // effigy is of the whole model.
                Effigy effigy = (Effigy)tableau.getContainer();
                
                // We want to open a new window that behaves as a
                // child of the model window.  So, we create a new text
                // effigy inside this one.  Specify model's effigy as
                // a container for this new effigy.
                Effigy textEffigy = new TextEffigy(effigy,
                        effigy.uniqueName("debugListener" + object.getName()));
                
                DebugListenerTableau debugTableau =
                    new DebugListenerTableau(textEffigy,
                            textEffigy.uniqueName("debugListener"
                                    + object.getName()));
                debugTableau.setDebuggable(object);
            }
            catch (KernelException ex) {
                MessageHandler.error(
                        "Failed to create debug listener.", ex);
            }
        }
    }
}
