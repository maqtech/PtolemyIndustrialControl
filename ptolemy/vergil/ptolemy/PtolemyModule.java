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

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
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

        // Create the Devel menu
        JMenu menuDevel = new JMenu("Ptolemy II");
        menuDevel.setMnemonic('P');
        _application.addMenu(menuDevel);

        action = new AbstractAction ("Print document info") {
            public void actionPerformed(ActionEvent e) {
                Document d = _application.getCurrentDocument();
                if (d == null) {
                    System.out.println("Document is null");
                } else {
                    System.out.println(d.toString());
                }
            }
        };
	GUIUtilities.addMenuItem(menuDevel, action, 'I',
				 "Print current document info");

	JToolBar tb = new JToolBar();
	Container pane = _application.getDesktopContext().getToolBarPane();
	pane.add(tb);
	
	action = new executeSystemAction();
	_application.addAction(action);
	GUIUtilities.addMenuItem(menuDevel, action, 'E', "Execute System");

	action = new layoutAction();
        _application.addAction(action);
	GUIUtilities.addMenuItem(menuDevel, action, 'L', 
				 "Automatically layout the model");
	
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
           getApplication().showError("Failed to parse icon library", e);
        }
   
	// Get the url for the entity library.
	URL entityLibURL = 
	    getModuleResources().getResource("rootEntityLibrary");
	// Create the library browser.
	JTree pTree = LibraryTreeModel.createTree(application, entityLibURL);
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
	    super("Execute System");
	}

	public void actionPerformed(ActionEvent e) {
	    PtolemyDocument d =
		(PtolemyDocument) _application.getCurrentDocument();
	    if (d == null) {
		return;
	    }
	    try {
		CompositeActor toplevel =
		    (CompositeActor) d.getModel();
		Manager manager = toplevel.getManager();
		
		// FIXME there is alot of code in here that is similar
		// to code in MoMLApplet and MoMLApplication.  I think
		// this should all be in ModelPane.
				

		// Create a manager.
		// Attaching these listeners is a nasty business...
		// All Managers are not created equal, since some have
		// listeners attached.
		if(manager == null) {
		    manager =
			new Manager(toplevel.workspace(), "Manager");
		    toplevel.setManager(manager);
		    manager.addExecutionListener(_statusListener);
		    manager.addExecutionListener(_streamListener);
		}
		
		// Get a frame to execute in.
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

		// Create a panel to place placeable objects.
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BoxLayout(displayPanel,
						     BoxLayout.Y_AXIS));
		modelPane.setDisplayPane(displayPanel);
		
		// Put placeable objects in a reasonable place
		for(Iterator i = toplevel.deepEntityList().iterator();
		    i.hasNext();) {
		    Object o = i.next();
		    if(o instanceof Placeable) {
			((Placeable) o).place(displayPanel);
		    }
		}
		
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
		getApplication().showError("Execution Failed", ex);
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
	    DocumentationViewer viewer = 
		new DocumentationViewer(target);	
	    JFrame frame = new JFrame();
	    frame.getContentPane().add(viewer);
	    frame.setTitle(target.getClass().getName());
	    frame.show();
	    frame.toFront();
	    frame.pack();
	}
    };
    
    private class layoutAction extends AbstractAction {
	public layoutAction() {
	    super("Automatic Layout");
	}
	public void actionPerformed(ActionEvent e) {
	    PtolemyDocument d = (PtolemyDocument)
		_application.getCurrentDocument();
	    JGraph jg = d.getView();
	    _redoLayout(jg);
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
	    Application app = getApplication();
	    PtolemyDocument doc = new PtolemyDocument(app);
	    doc.setModel(entity);
	    app.addDocument(doc);
	    app.displayDocument(doc);
	    app.setCurrentDocument(doc);
	}
    }
   
    // An action to create a new port.
    public class newPortAction extends FigureAction {
	public newPortAction() {
	    super("New External Port");
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    Document doc = getApplication().getCurrentDocument();
	    // Only create ports for ptolemy documents.
	    if(!(doc instanceof PtolemyDocument)) return;
	    PtolemyDocument ptolemyDocument = (PtolemyDocument)doc;
	    JGraph jgraph = ptolemyDocument.getView();
	    GraphPane pane = jgraph.getGraphPane();
	    // Get the location
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
	    
	    final double finalX = x;
	    final double finalY = y;
	    final EditorGraphController controller = 
		(EditorGraphController)pane.getGraphController();
	    GraphModel model = controller.getGraphModel();
	    final CompositeEntity toplevel =
		(CompositeEntity)model.getRoot();
	    _doChangeRequest(toplevel, new ChangeRequest(toplevel, 
		"Creating new Port in " + toplevel.getFullName()) {
		public void execute() throws ChangeFailedException {
		    try {
			Port port = 
			    toplevel.newPort(toplevel.uniqueName("port"));
			controller.addNode(port, finalX, finalY);
		    } catch (Exception ex) {
			throw new ChangeFailedException(this, ex.getMessage());
		    }
		}
	    });
	}
    }
    
    // An action to create a new relation.
    public class newRelationAction extends FigureAction {
	public newRelationAction() {
	    super("New Relation");
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    Document doc = getApplication().getCurrentDocument();
	    // Only create ports for ptolemy documents.
	    if(!(doc instanceof PtolemyDocument)) return;
	    PtolemyDocument ptolemyDocument = (PtolemyDocument)doc;
	    JGraph jgraph = ptolemyDocument.getView();
	    GraphPane pane = jgraph.getGraphPane();
	    // Get the location
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
	    
	    final double finalX = x;
	    final double finalY = y;
	    final EditorGraphController controller = 
		(EditorGraphController)pane.getGraphController();
	    final GraphModel model = controller.getGraphModel();
	    final CompositeEntity toplevel =
		(CompositeEntity)model.getRoot();
	    _doChangeRequest(toplevel, new ChangeRequest(toplevel, 
		"Creating new Relation in " + toplevel.getFullName()) {
		public void execute() throws ChangeFailedException {
		    try {
			Relation relation = 
			    toplevel.newRelation(toplevel.uniqueName("relation"));
			Vertex vertex = new Vertex(relation,
						   relation.uniqueName("vertex"));
			controller.addNode(vertex, finalX, finalY);
		    } catch (Exception ex) {
			throw new ChangeFailedException(this, ex.getMessage());
		    }
		}
	    });
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
	    _application.showError(manager.getName(), exception);
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
    
    // A class for properly doing the layout of the graphs we have
    private class PtolemyLayout extends LevelLayout {
	
	/**
	 * Construct a new levelizing layout with a vertical orientation.
	 */
	public PtolemyLayout(LayoutTarget target) {
	    super(target);
	}

	/**
	 * Copy the given graph and make the nodes/edges in the copied
	 * graph point to the nodes/edges in the original.
	 */ 
	protected Object copyComposite(Object origComposite) {
	    LayoutTarget target = getLayoutTarget();
	    GraphModel model = target.getGraphModel();
	    diva.graph.basic.BasicGraphModel local = getLocalGraphModel();
	    Object copyComposite = local.createComposite(null);
	    HashMap map = new HashMap();
	    
	    // Copy all the nodes for the graph.
	    for(Iterator i = model.nodes(origComposite); i.hasNext(); ) {
		Object origNode = i.next();
		if(target.isNodeVisible(origNode)) {
		    Rectangle2D r = target.getBounds(origNode);
		    LevelInfo inf = new LevelInfo();
		    inf.origNode = origNode;
		    inf.x = r.getX();
		    inf.y = r.getY();
		    inf.width = r.getWidth();
		    inf.height = r.getHeight();
		    Object copyNode = local.createNode(inf);
		    local.addNode(this, copyNode, copyComposite);
		    map.put(origNode, copyNode);
		}
	    }
	    
	    // Add all the edges.
	    Iterator i = GraphUtilities.localEdges(origComposite, model); 
	    while(i.hasNext()) {
		Object origEdge = i.next();
		Object origTail = model.getTail(origEdge);
		Object origHead = model.getHead(origEdge);
		if(origHead != null && origTail != null) {
		    Figure tailFigure = 
			(Figure)target.getVisualObject(origTail);
		    Figure headFigure = 
			(Figure)target.getVisualObject(origHead);
		    // Swap the head and the tail if it will improve the 
		    // layout, since LevelLayout only uses directed edges.
		    if(tailFigure instanceof Terminal) {
			Terminal terminal = (Terminal)tailFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.WEST) {
				Object temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    } else if(headFigure instanceof Terminal) {
			Terminal terminal = (Terminal)headFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.EAST) {
				Object temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    }

		    origTail =
			_getParentInGraph(model, origComposite, origTail);
		    origHead = 
			_getParentInGraph(model, origComposite, origHead);
		    Object copyTail = map.get(origTail);
		    Object copyHead = map.get(origHead);

		    if(copyHead != null && copyTail != null) {
                        Object copyEdge = local.createEdge(origEdge);
                        local.setEdgeTail(this, copyEdge, copyTail);
                        local.setEdgeHead(this, copyEdge, copyHead);
 		    }
		}
	    }
	    
	    return copyComposite;
	}

	// Unfortunately, the head and/or tail of the edge may not 
	// be directly contained in the graph.  In this case, we need to
	// figure out which of their parents IS in the graph 
	// and calculate the cost of that instead.
	private Object _getParentInGraph(GraphModel model, 
					 Object graph, Object node) {
	    while(node != null && !model.containsNode(graph, node)) {
		Object parent = model.getParent(node);
		if(model.isNode(parent)) {
		    node = parent;
		} else {
		    node = null;
		}
	    }
	    return node;
	}
    }

    // A layout target that translates locatable nodes.
    private class PtolemyLayoutTarget extends BasicLayoutTarget {
	/**
	 * Construce a new layout target that operates
	 * in the given pane.
	 */
	public PtolemyLayoutTarget(GraphController controller) {
	    super(controller);
	}
    
	/**
	 * Translate the figure associated with the given node in the
	 * target's view by the given delta.
	 */
	public void translate(Object node, double dx, double dy) {
	    super.translate(node, dx, dy);
	    if(node instanceof Locatable) {
		double location[] = ((Locatable)node).getLocation();
		if(location == null) {
		    location = new double[2];
		    Figure figure = getController().getFigure(node);
		    location[0] = figure.getBounds().getCenterX();
		    location[1] = figure.getBounds().getCenterY();
		} else {
		    location[0] += dx;
		    location[1] += dy;
		}
		((Locatable)node).setLocation(location);
 	    }
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Queue the change request with the given entity.  show any exceptions
    // that occur in the user interface.
    private void _doChangeRequest(CompositeEntity entity, 
				 ChangeRequest request) {
	try {
	    entity.requestChange(request);
	} 
	catch (ChangeFailedException ex) {
	    getApplication().showError("Create relation failed:", ex);
	}
    }
    
    // Redo the layout of the given JGraph.
    private void _redoLayout(JGraph jgraph) {
	GraphController controller = 
	    jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        GraphModel model = controller.getGraphModel();
        PtolemyLayout layout = new PtolemyLayout(target);
	layout.setOrientation(LevelLayout.HORIZONTAL);
	layout.setRandomizedPlacement(false);
        // Perform the layout and repaint
        try {
            layout.layout(model.getRoot());
        } catch (Exception e) {
            getApplication().showError("Layout failed", e);
        }
        jgraph.repaint();
    }
    
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
