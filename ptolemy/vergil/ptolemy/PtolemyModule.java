/* A modular Vergil package for Ptolemy models.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy;

// FIXME: Trim this.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.Location;
import ptolemy.moml.Vertex;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.Locatable;
import ptolemy.vergil.*;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.vergil.icon.*;
import ptolemy.vergil.tree.LibraryTreeModel;
import ptolemy.vergil.tree.PTree;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.graph.*;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.RelativeBundle;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.*;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * A module that can be plugged into Vergil that adds support for
 * Ptolemy II.  This package adds a Ptolemy II menu to the menu bar, which
 * allows access to the model of the currently selected document, if that
 * document is a Ptolemy document.  It also adds a new tool bar that contains
 * a pulldown menu for selecting directors and a pulldown menu for executing a
 * graph layout algorithm.
 * <p>
 * This package contains a list of Directors which are placed in a
 * toolbar menu.
 * <p>
 * This package contains a list of Visual notations.  Each notation is
 * capable of creating a view on a Ptolemy Document.  In some cases,
 * certain notations may be preferable for some domains.  For instance,
 * Finite State Machine models are usually represented using a bubble
 * and arc diagram.  However, bubble and arc diagrams are not usually used
 * for dataflow diagrams.
 * <p>
 * Currently the only access to the model that is provided is execution of the
 * model.  When the model is executed, this package listens to the state of
 * the model's manager and updates the Vergil status bar.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class PtolemyModule implements Module {
    /**
     * Create a new module that will extend the functionality of the given
     * Vergil application.  Create a new "Ptolemy II" menu and add it to
     * the application's menu bar.  Create and add an action for viewing a 
     * MoML description of the current document.  Create and add an action for
     * executing the model of the current document.  Both of these actions
     * are only enabled if the current document is an instance of
     * PtolemyDocument.  Also create a new toolbar and add it to the 
     * application.  Create and add a button to the toolbar which will 
     * trigger layout of the current document.
     */
    public PtolemyModule(VergilApplication application) {
	_application = application;

	_statusListener = new VergilExecutionListener(application);
	_streamListener = new StreamExecutionListener();

	Action action;
	// First add the the generic actions.

	action = new lookInsideAction();
	_application.addAction(action);

        // Create the View menu
        JMenu menuView = new JMenu("View");
        menuView.setMnemonic('V');
        _application.addMenu(menuView);

        action = new AbstractAction ("Description") {
            public void actionPerformed(ActionEvent e) {
		View v = _application.getCurrentView();
                Document doc = v.getDocument();
                if (doc == null) {
                    try {
                        MessageHandler.warning("No current document");
                    } catch (CancelException ex) {
                        // Ignore, since there is nothing happening anyway.
                    }
                } else {
                    TextEditor show = new TextEditor();
                    show.text.setEditable(false);
                    show.text.append(doc.toString());
                    show.setVisible(true);
                }
            }
        };
	GUIUtilities.addMenuItem(menuView, action, 'D',
                "Show a description of the current model");

	action = new LayoutAction();
        _application.addAction(action);
	GUIUtilities.addMenuItem(menuView, action, 'L', 
                "Automatically layout the model");

        // Create the Execute menu
        JMenu menuExecute = new JMenu("Execute");
        menuExecute.setMnemonic('X');
        _application.addMenu(menuExecute);

        // Populate the Execute menu
	action = new executeSystemAction();
	_application.addAction(action);
	GUIUtilities.addMenuItem(menuExecute, action, 'X', "Execute the model");
	
        // Create the toolbar.
	JToolBar tb = new JToolBar();
	Container pane = _application.getDesktopContext().getToolBarPane();
	pane.add(tb);

	String dflt = "";
	// Creating the renderers this way is rather nasty..
	// Standard toolbar icons are 25x25 pixels.
	NodeRenderer renderer = new PortController.PortRenderer();
	Figure figure = renderer.render(null);
	
	Icon icon = new FigureIcon(figure, 25, 25, 1, true);
	action = new newPortAction();
	_application.addAction(action);
	GUIUtilities.addToolBarButton(tb, action,
				      "New External Port", icon);

	action = new getDocumentationAction();
	_application.addAction(action);

	action = new editIconAction();
	_application.addAction(action);

	renderer = new RelationController.RelationRenderer();
	figure = renderer.render(null);
	icon = new FigureIcon(figure, 25, 25, 1, true);
	action = new newRelationAction();
	_application.addAction(action);
	GUIUtilities.addToolBarButton(tb, new newRelationAction(), 
				      "New Relation", icon);
   
	//figure = EditorIcon._createDefaultBackgroundFigure();
	//icon = new FigureIcon(figure, 20, 20);
	//GUIUtilities.addToolBarButton(tb, new newCompositeAction(), 
	//			      "New Composite", icon);

	// Create something that will manage Directors for us.  
	new DirectorService(getApplication(), tb);

        action = new executeSystemAction();
        _application.addAction(action);
        GUIUtilities.addToolBarButton(tb, action,
				      "Execute System", "Go");
        
        application.addDocumentFactory(new PtolemyDocument.Factory());
	application.addDocumentFactory(new PtolemyDocument.FSMFactory());

	// parse the icon library
	URL iconlibURL = null;
        try {
            iconlibURL = 
		getModuleResources().getResource("rootIconLibrary");

	    MoMLParser parser = new MoMLParser();
	    _iconLibrary = (CompositeEntity) parser.parse(
                    iconlibURL, iconlibURL.openStream());
            LibraryIcon.setIconLibrary(_iconLibrary);
        } catch (Exception e) {
           MessageHandler.error("Failed to parse icon library", e);
        }
   
	// Get the url for the entity library.
	URL entityLibURL = 
	    getModuleResources().getResource("rootEntityLibrary");
	// Create the library browser.
	JTree pTree = LibraryTreeModel.createTree(entityLibURL);
        pTree.setBackground(BACKGROUND_COLOR);
        JScrollPane scrollPane = new JScrollPane(pTree);
        scrollPane.setMinimumSize(new Dimension(200, 200));
        scrollPane.setPreferredSize(new Dimension(200, 200));
        DesktopContext frame = application.getDesktopContext();
        JPanel palettePane = (JPanel)frame.getPalettePane();
        // add at zero because the panner should stay at the bottom.
        palettePane.add(scrollPane, 0);
        JSplitPane splitPane = frame.getSplitPane();
        splitPane.resetToPreferredSizes();
        splitPane.validate();
        splitPane.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a visual notation to the list of visual notations.
     */
    public void addNotation(PtolemyNotation notation) {
	_notationModel.addElement(notation);
    }

    /** 
     * Return the application that contains this module.
     */
    public VergilApplication getApplication() {
        return _application;
    }

    /** 
     * Return the icon library associated with this Vergil.
     */
    public CompositeEntity getIconLibrary() {
	return _iconLibrary;
    }

    /** 
     * Return the resources for this module.
     */
    public RelativeBundle getModuleResources() {
        return _moduleResources;
    }

    /**
     * Return a list of the notations in the notation list.
     */
    public List notationList() {
	List list = new LinkedList();
	for(int i = 0; i < _notationModel.getSize(); i++) {
	    list.add(_notationModel.getElementAt(i));
	}
	return list;
    }

    /**
     * Remove a notation from the list of notations.
     */
    public void removeNotation(PtolemyNotation notation) {
	_notationModel.removeElement(notation);
    }

    /** 
     * The color that is used for the background of a Ptolemy model.  
     * Icons should be designed to show up well against this color.
     */
    public static Color BACKGROUND_COLOR = 
	new Color((float).85, (float).85, (float).85);
    
    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    public class editIconAction extends FigureAction {
	public editIconAction() {
	    super("Edit Icon");
	}

	public void actionPerformed(ActionEvent e) {
	    // Figure out what entity.
	    super.actionPerformed(e);		
	    NamedObj object = getTarget();
	    if(!(object instanceof Entity)) return;
	    Entity entity = (Entity) object;
	    XMLIcon icon = null;
	    List iconList = entity.attributeList(XMLIcon.class);
	    if(iconList.size() == 0) {
		try {
		    icon = new XMLIcon(entity, entity.uniqueName("icon"));
		} catch (Exception ex) {
		    throw new InternalErrorException("duplicated name, but " + 
						     "there were no other icons.");
		}
	    } else if(iconList.size() == 1) {
		icon = (XMLIcon)iconList.get(0);
	    } else {
		throw new InternalErrorException("entity " + entity + 
				 "contains more than one icon");
	    }
	    ApplicationContext appContext = new ApplicationContext();
	    appContext.setTitle("Icon editor");
	    new IconEditor(appContext, icon);
	}
    }

    private class executeSystemAction extends AbstractAction {
	public executeSystemAction() {
	    super("Execute Model");
	}

	public void actionPerformed(ActionEvent e) {
	    View v = _application.getCurrentView();
	    PtolemyDocument doc = (PtolemyDocument)v.getDocument();
            if (doc == null) {
                try {
                    MessageHandler.warning("No current document");
                } catch (CancelException ex) {
                    // Ignore, since there is nothing happening anyway.
                }
	    }
	    try {
		CompositeActor toplevel =
		    (CompositeActor) doc.getModel();
		Manager manager = toplevel.getManager();
				
		// FIXME there is alot of code in here that is similar
		// to code in MoMLApplet and MoMLApplication.  I think
		// this should all be in ModelPane.

                // FIXME: Create the manager sooner?
				
		// Create a manager.
		// Attaching these listeners is a nasty business...
		// All Managers are not created equal, since some have
		// listeners attached.
		if(manager == null) {
		    manager = new Manager(toplevel.workspace(), "Manager");
		    toplevel.setManager(manager);
		    manager.addExecutionListener(_statusListener);
		    manager.addExecutionListener(_streamListener);
		}
		
		// Get a frame to execute in.
                // FIXME: Use ModelFrame here.  How?
                JFrame frame;
		// First see if we've created a frame previously.
		List list = toplevel.attributeList(FrameAttribute.class);
		if(list.size() == 0) {
		    // If we don't have a frame, create a new one.
		    frame = new JFrame();
		    FrameAttribute attrib = 
			new FrameAttribute(toplevel, toplevel.getName());
		    attrib.setFrame(frame);	

		    // hook into the window closing action to stop the model.
		    final Manager finalManager = manager;
		    frame.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
			    finalManager.finish();
			}
		    });    
		} else if(list.size() == 1) {
		    // if we DO have a frame, then use it.
		    FrameAttribute attrib = (FrameAttribute)list.get(0);
		    frame = attrib.getFrame();
		    frame.getContentPane().removeAll();
		} else {		
		    // this should never happen since FrameAttribute
		    // disallows it.
		    throw new InvalidStateException("Composite Actor can " + 
						    "only contain one " + 
						    "execution pane.");
		}

		// Now that we have a frame, create a modelpane to put in it.
		ModelPane modelPane = new ModelPane(toplevel);
		frame.getContentPane().add(modelPane, BorderLayout.NORTH);

		if(frame != null) {
		    // Show the frame, even if it is currently iconified.
		    frame.setVisible(true);
		    frame.setState(frame.NORMAL);
		    frame.show();
		    frame.toFront();
		}
		
		// Pack the frame, which has to happen AFTER it appears..
		// This is a horrible horrible hack.
		final JFrame packframe = frame;
		Action packer = new AbstractAction() {
		    public void actionPerformed(ActionEvent event) {
			packframe.getContentPane().doLayout();
			packframe.repaint();
			packframe.pack();
		    }
		};
		javax.swing.Timer timer =
		    new javax.swing.Timer(200, packer);
		timer.setRepeats(false);
		timer.start();
				
	    } catch (Exception ex) {
		MessageHandler.error("Execution Failed", ex);
	    }	    
	}
    }
    
    public class getDocumentationAction extends FigureAction {
	public getDocumentationAction() {
	    super("Get Documentation");
	}
	public void actionPerformed(ActionEvent e) {	    
	    // Create a dialog for configuring the object.
	    // FIXME this should probably be one frame for each class.
	    super.actionPerformed(e);		
	    NamedObj target = getTarget();
	    final DocumentationViewer viewer = 
		new DocumentationViewer(target);	
	    viewer.setTitle(target.getClass().getName());
	    viewer.show();
	    viewer.toFront();
	    viewer.pack();

	}
    };
    
    private class LayoutAction extends AbstractAction {
	public LayoutAction() {
	    super("Automatic Layout");
	}
	public void actionPerformed(ActionEvent e) {
	    View view = _application.getCurrentView();
	    if(view instanceof PtolemyGraphView) {
		try {
		    ((PtolemyGraphView)view).layout();
		} catch (Exception ex) {
		    MessageHandler.error("Layout failed", ex);
		}      
	    }
	}
    }
    
    // An action to look inside a composite.
    private class lookInsideAction extends FigureAction {
	public lookInsideAction() {
	    super("Look Inside");
	}
	public void actionPerformed(ActionEvent e) {
	    // Figure out what entity.
	    super.actionPerformed(e);		
	    NamedObj object = getTarget();
	    if(!(object instanceof CompositeEntity)) return;
	    CompositeEntity entity = (CompositeEntity)object;
	    NamedObj deferredTo = entity.getDeferMoMLDefinitionTo();
	    if(deferredTo != null) {
		entity = (CompositeEntity)deferredTo;
	    }

	    Application app = getApplication();
	    PtolemyDocument doc = new PtolemyDocument(app);
	    doc.setModel(entity);
	    app.addDocument(doc);
	    View v = app.createView(doc);
	    app.addView(v);
	    app.setCurrentView(v);
	}
    }
   
    // An action to create a new port.
    public class newPortAction extends FigureAction {
	public newPortAction() {
	    super("New External Port");
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    View view = _application.getCurrentView();
	    // Only create ports for ptolemy graph views?
	    if(view instanceof PtolemyGraphView) {
		PtolemyGraphView graphView = (PtolemyGraphView)view;
		// Get the location
		JGraph jgraph = graphView.getJGraph();
		GraphPane pane = jgraph.getGraphPane();
		double x;
		double y;
		if(getSourceType() == TOOLBAR_TYPE ||
		   getSourceType() == MENUBAR_TYPE) {	
		    // no location in the action, so make something up.
		    Point2D point = pane.getSize();    
		    x = point.getX()/2;
		    y = point.getY()/2;
		} else {		    
		    x = getX();
		    y = getY();
		}
		
		PtolemyDocument doc = graphView.getPtolemyDocument();
		final double finalX = x;
		final double finalY = y;
		final CompositeEntity toplevel =
		    (CompositeEntity)doc.getModel();
		toplevel.requestChange(new ChangeRequest(this,
		    "Creating new Port in " + toplevel.getFullName()) {
		    protected void _execute() throws Exception {
			Port port = 
			    toplevel.newPort(toplevel.uniqueName("port"));
			Location location =
			    new Location(port, 
					 port.uniqueName("_location"));
			
			double coords[] = new double[2];
			coords[0] = ((int)finalX);
			coords[1] = ((int)finalY);
			location.setLocation(coords);
		    }
		});
	    }
	}
    }
    
    // An action to create a new relation.
    public class newRelationAction extends FigureAction {
	public newRelationAction() {
	    super("New Relation");
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    View view = _application.getCurrentView();
	    // Only create relations for ptolemy graph views?
	    if(view instanceof PtolemyGraphView) {
		PtolemyGraphView graphView = (PtolemyGraphView)view;
		// Get the location
		JGraph jgraph = graphView.getJGraph();
		GraphPane pane = jgraph.getGraphPane();
		double x;
		double y;
		if(getSourceType() == TOOLBAR_TYPE ||
		   getSourceType() == MENUBAR_TYPE) {	
		    // no location in the action, so make something up.
		    // FIXME this is a lousy way to do this.
		    Point2D point = pane.getSize();    
		    x = point.getX()/2;
		    y = point.getY()/2;
		} else {
		    x = getX();
		    y = getY();
		}
		
		PtolemyDocument doc = graphView.getPtolemyDocument();
		final double finalX = x;
		final double finalY = y;
		final CompositeEntity toplevel =
		    (CompositeEntity)doc.getModel();
		
		// FIXME use MoML.  If no class is specifed in MoML, it should
		// use the newRelation method.
		toplevel.requestChange(new ChangeRequest(this,
		    "Creating new Relation in " + toplevel.getFullName()) {
		    protected void _execute() throws Exception {
			Relation relation = 
			    toplevel.newRelation(toplevel.uniqueName("relation"));
			Vertex vertex =
			    new Vertex(relation, relation.uniqueName("vertex"));
			
			double coords[] = new double[2];
			coords[0] = ((int)finalX);
			coords[1] = ((int)finalY);
			vertex.setLocation(coords);
		    }
		});
	    }
	}
    }

    // An execution listener that displays the status of the current
    // document.
    public static class VergilExecutionListener implements ExecutionListener {
	public VergilExecutionListener(Application a) {
            _application = a;
        }

        // Defer to the application to display the error to the user.
	public void executionError(Manager manager, Exception exception) {
	    MessageHandler.error(manager.getName(), exception);
	}
	
	// Do nothing when execution finishes
	public void executionFinished(Manager manager) {
	}

	// Display the new manager state in the application's status bar.
	public void managerStateChanged(Manager manager) {
	    AppContext context = _application.getAppContext();
	    context.showStatus(manager.getState().getDescription());
	}
        private Application _application;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The application that this package is associated with.
    private VergilApplication _application;

    // The layout button
    private JButton _layoutButton;

    // The list of notations.
    private DefaultComboBoxModel _notationModel;

    // The layout engine
    private GlobalLayout _globalLayout;

    // The Icon Library.
    private CompositeEntity _iconLibrary;

    // The resources for this module.
    private RelativeBundle _moduleResources =
	new RelativeBundle("ptolemy.vergil.ptolemy.Ptolemy", getClass(), null);
    
    // A listener for setting the status line.
    private ExecutionListener _statusListener = null;
	
    // A listener for getting debug information.
    private ExecutionListener _streamListener = null;
}
