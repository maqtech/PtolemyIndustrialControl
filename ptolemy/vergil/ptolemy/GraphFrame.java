/* A simple graph view for Ptolemy models

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

package ptolemy.vergil.ptolemy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.ZList;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FocusMouseListener;
import diva.gui.toolbox.JPanner;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.RunTableau;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.VisibleTreeModel;

//////////////////////////////////////////////////////////////////////////
//// GraphFrame
/**
A simple graph view for ptolemy models.  This represents a level of the
hierarchy of a ptolemy model as a diva graph.  Cut, copy and paste operations
are supported using MoML.

@author  Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
*/
public abstract class GraphFrame extends PtolemyFrame
    implements Printable, ClipboardOwner, ChangeListener {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public GraphFrame(CompositeEntity entity, Tableau tableau) {
        super(entity, tableau);

        entity.addChangeListener(this);

	getContentPane().setLayout(new BorderLayout());

	GraphPane pane = _createGraphPane();

	_jgraph = new JGraph(pane);

	new EditorDropTarget(_jgraph);

	ActionListener deletionListener = new ActionListener() {
            /** Delete any nodes or edges from the graph that are currently
             *  selected.  In addition, delete any edges that are connected to
             *  any deleted nodes.
             */
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        };
    
	_jgraph.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
	_jgraph.setRequestFocusEnabled(true);
	_jgraph.addMouseListener(new FocusMouseListener());
	_jgraph.setAlignmentX(1);
	_jgraph.setAlignmentY(1);
	_jgraph.setBackground(BACKGROUND_COLOR);

	// Set the default size.
	_jgraph.setPreferredSize(new Dimension(1200, 900));
	_jgraph.setSize(1200, 900);

	// wrap the graph editor in a scroll pane.
	_graphScrollPane = new JScrollPane(_jgraph);
	_graphScrollPane.setVerticalScrollBarPolicy(
                _graphScrollPane.VERTICAL_SCROLLBAR_NEVER);
	_graphScrollPane.setHorizontalScrollBarPolicy(
                _graphScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	_graphScrollPane.setPreferredSize(new Dimension(600, 450));
	_graphScrollPane.setSize(600, 450);

        // Set the size and location.
        // Note that the location is of the frame, while the size
        // is of the scrollpane.
        _graphScrollPane.setMinimumSize(new Dimension(200, 200));
        try {
            SizeAttribute bounds = (
                     SizeAttribute)getModel().getAttribute(
                     "_vergilSize", SizeAttribute.class);
            if (bounds == null) {
                bounds = new SizeAttribute(getModel(), "_vergilSize");
            }
            bounds.setSize(_graphScrollPane);

            LocationAttribute location
                     = (LocationAttribute)getModel().getAttribute(
                     "_vergilLocation", LocationAttribute.class);
            if (location == null) {
                     location = new LocationAttribute(
                     getModel(), "_vergilLocation");
            }
            location.setLocation(this);
        } catch (Exception ex) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }
	// Create the panner.
	_graphPanner = new JPanner();
	_graphPanner.setPreferredSize(new Dimension(200, 150));
	_graphPanner.setMaximumSize(new Dimension(200, 150));
	_graphPanner.setSize(200, 150);
        // NOTE: Border causes all kinds of problems!
	// _graphPanner.setBorder(BorderFactory.createEtchedBorder());
        _graphPanner.setViewport(_graphScrollPane.getViewport());

	// Create the library of actors, or use the one in the entity,
        // if there is one.
        // FIXME: How do we make changes to the library persistent?
        boolean gotLibrary = false;
        try {
            LibraryAttribute libraryAttribute = (LibraryAttribute)
                    entity.getAttribute("_library", LibraryAttribute.class);
            if (libraryAttribute != null) {
                // The model contains a library.
                _topLibrary = libraryAttribute.getLibrary();
                gotLibrary = true;
            }
        } catch (Exception ex) {
            try {
                MessageHandler.warning("Invalid library in the model.", ex);
            } catch (CancelException e) {}
        }
        if (!gotLibrary) {
            // The model does not contain a library.
            // See if there is a default library in the configuration.
            Configuration configuration = getConfiguration();
            if (configuration != null) {
                _topLibrary = (CompositeEntity)
                        configuration.getEntity("actor library");
                if (_topLibrary == null) {
                    // Create an empty library by default.
                    Workspace workspace = entity.workspace();
                    _topLibrary = new CompositeEntity(workspace);
                    try {
                        _topLibrary.setName("topLibrary");
                        // Put a marker in so that this is
                        // recognized as a library.
                        new Attribute(_topLibrary, "_libraryMarker");
                    } catch (Exception ex) {
                        throw new InternalErrorException(
                                "Library configuration failed: " + ex);
                    }
                }
            }
        }

        _libraryModel = new VisibleTreeModel(_topLibrary);
        _library = new PTree(_libraryModel);
        _library.setRootVisible(false);
        _library.setBackground(BACKGROUND_COLOR);

        // If you want to expand the top-level libraries, uncomment this.
        /*
          Object[] path = new Object[2];
          path[0] = topLibrary;
          Iterator libraries = topLibrary.entityList().iterator();
          while(libraries.hasNext()) {
          path[1] = libraries.next();
          _library.expandPath(new TreePath(path));
          }
        */

        _libraryScrollPane = new JScrollPane(_library);
        _libraryScrollPane.setMinimumSize(new Dimension(200, 200));
        _libraryScrollPane.setPreferredSize(new Dimension(200, 200));

	// create the palette on the left.
	_palettePane = new JPanel();
	_palettePane.setBorder(null);
        _palettePane.setLayout(new BoxLayout(_palettePane, BoxLayout.Y_AXIS));

	_palettePane.add(_libraryScrollPane, BorderLayout.CENTER);
	_palettePane.add(_graphPanner, BorderLayout.SOUTH);

	_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
	_splitPane.setLeftComponent(_palettePane);
	_splitPane.setRightComponent(_graphScrollPane);
	getContentPane().add(_splitPane, BorderLayout.CENTER);

	// FIXME: hotkeys, shortcuts and move to a base class.
	_toolbar = new JToolBar();
	getContentPane().add(_toolbar, BorderLayout.NORTH);

       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _zoomInAction);
       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _zoomResetAction);
       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _zoomFitAction);
       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _zoomOutAction);

	_cutAction = new CutAction();
	_copyAction = new CopyAction();
	_pasteAction = new PasteAction();
	_layoutAction = new LayoutAction();
	_saveInLibraryAction = new SaveInLibraryAction();
	_importLibraryAction = new ImportLibraryAction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that a change has been successfully executed
     *  by marking the data associated with this window modified.  This
     *  will trigger a dialog when the window is closed, prompting the
     *  user to save the data.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        setModified(true);
        _graphPanner.repaint();
    }

    /** React to the fact that a change has triggered an error by
     *  doing nothing (the effigy is also listening and will report
     *  the error).
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Do not report if it has already been reported.
        if (change == null) {
            MessageHandler.error("Change failed", exception);
        } else if (!change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Change failed", exception);
        }
    }

    /** Get the currently selected objects from this document, if any,
     *  and place them on the clipboard in MoML format.
     */
    public void copy() {
	Clipboard clipboard =
	        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	        (GraphController)graphPane.getGraphController();
	SelectionModel model = controller.getSelectionModel();
	GraphModel graphModel = controller.getGraphModel();
	Object selection[] = model.getSelectionAsArray();
        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet namedObjSet = new HashSet();
	HashSet nodeSet = new HashSet();
        // First get all the nodes.
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject = ((Figure)selection[i]).getUserObject();
                if(graphModel.isNode(userObject)) {
                    nodeSet.add(userObject);
                    NamedObj actual =
                        (NamedObj)graphModel.getSemanticObject(userObject);
                    namedObjSet.add(actual);
                }
	    }
	}
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject = ((Figure)selection[i]).getUserObject();
                if(graphModel.isEdge(userObject)) {
                    // Check to see if the head and tail are both being
                    // copied.  Only if so, do we actually take the edge.
                    Object head = graphModel.getHead(userObject);
                    Object tail = graphModel.getTail(userObject);
                    boolean headOK = nodeSet.contains(head);
                    boolean tailOK = nodeSet.contains(tail);
                    Iterator objects = nodeSet.iterator();
                    while(!(headOK && tailOK) && objects.hasNext()) {
                        Object object = objects.next();
                        if(!headOK && GraphUtilities.isContainedNode(head,
                                object, graphModel)) {
                            headOK = true;
                        }
                        if(!tailOK && GraphUtilities.isContainedNode(tail,
                                object, graphModel)) {
                            tailOK = true;
                        }
                    }
                    if(headOK && tailOK) {
                        NamedObj actual =
                            (NamedObj)graphModel.getSemanticObject(userObject);
                         namedObjSet.add(actual);
                    }
                }
            }
        }
	StringWriter buffer = new StringWriter();
	try {
	    Iterator elements = namedObjSet.iterator();
	    while(elements.hasNext()) {
                NamedObj element = (NamedObj)elements.next();
                // first level to avoid obnoxiousness with
		// toplevel translations.
		element.exportMoML(buffer, 1);
	    }
	    CompositeEntity container = (CompositeEntity)graphModel.getRoot();
	    buffer.write(container.exportLinks(1, namedObjSet));

	    // The code below does not use a PtolemyTransferable,
	    // to work around
	    // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
	    // is that cut and paste through the system clipboard to native
	    // applications doesn't work unless you use string selection.
	    clipboard.setContents(new StringSelection(buffer.toString()),
                    this);
	}
	catch (Exception ex) {
	    MessageHandler.error("Copy failed", ex);
	}

    }

    /** Remove the currently selected objects from this document, if any,
     *  and place them on the clipboard.
     */
    public void cut() {
        copy();
        delete();
    }

    /** Delete the currently selected objects from this document.
     */
    public void delete() {
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
		(GraphController)graphPane.getGraphController();
	AbstractPtolemyGraphModel graphModel =
		(AbstractPtolemyGraphModel)controller.getGraphModel();
        // Note that we turn off event dispatching so that each individual
        // removal does not trigger graph redrawing.
        try {
            graphModel.setDispatchEnabled(false);
            SelectionModel model = controller.getSelectionModel();
            Object selection[] = model.getSelectionAsArray();
            Object userObjects[] = new Object[selection.length];
            // First remove the selection.
            for(int i = 0; i < selection.length; i++) {
                userObjects[i] = ((Figure)selection[i]).getUserObject();
                model.removeSelection(selection[i]);
            }

            // Remove all the edges first,
            // since if we remove the nodes first,
            // then removing the nodes might remove some of the edges.
            for(int i = 0; i < userObjects.length; i++) {
                Object userObject = userObjects[i];
                if(graphModel.isEdge(userObject)) {
                    graphModel.disconnectEdge(this, userObject);
                }
            }
            for(int i = 0; i < selection.length; i++) {
                Object userObject = userObjects[i];
                if(graphModel.isNode(userObject)) {
                    graphModel.removeNode(this, userObject);
                }
            }
        } finally {
            graphModel.setDispatchEnabled(true);
            graphModel.dispatchGraphEvent(new GraphEvent(
                    this,
                    GraphEvent.STRUCTURE_CHANGED,
                    graphModel.getRoot()));
        }
    }

    /** Override the dispose method to unattach any listeners that may keep
     *  this model from getting garbage collected.
     */
    public void dispose() {
        // Remove the association with the library. This is necessary to allow
        // this frame, and the rest of the model to be properly garbage
        // collected
        _libraryModel.setRoot(null);
        super.dispose();
    }

    /** Return the center location of the visible part of the pane.
     *  @return The center of the visible part.
     */
    public Point2D getCenter() {
        Rectangle2D rect = getVisibleRectangle();
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    /** Return the jgraph instance that this view uses to represent the
     *  ptolemy model.
     */
    public JGraph getJGraph() {
	return _jgraph;
    }

    /** Return the rectangle representing the visible part of the pane.
     *  This is the range of locations that are visible.
     *  @return The rectangle representing the visible part.
     */
    public Rectangle2D getVisibleRectangle() {
        Rectangle raw = _graphScrollPane.getViewport().getViewRect();
        Point2D upperLeft = new Point2D.Double(raw.getX(), raw.getY());
        Point2D lowerRight = new Point2D.Double(raw.getX() + raw.getWidth(),
               raw.getY() + raw.getHeight());

        // Reverse any current zoom or pan transformations.
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform transform = 
                canvas.getCanvasPane().getTransformContext().getTransform();
        try {
            Point2D newUpperLeft = transform.inverseTransform(
                    upperLeft, null);
            Point2D newLowerRight = transform.inverseTransform(
                    lowerRight, null);
            return new Rectangle2D.Double(
                    newUpperLeft.getX(), newUpperLeft.getY(),
                    newLowerRight.getX() - newUpperLeft.getX(),
                    newLowerRight.getY() - newUpperLeft.getY());
        } catch (NoninvertibleTransformException ex) {
            // This should not occur with just zoom and pan.
            return raw;
        }
    }

    /** Layout the graph view.
     */
    public void layoutGraph() {
	GraphController controller =
                 _jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        GraphModel model = controller.getGraphModel();
        PtolemyLayout layout = new PtolemyLayout(target);
	layout.setOrientation(LevelLayout.HORIZONTAL);
	layout.setRandomizedPlacement(false);
        // Perform the layout and repaint
	layout.layout(model.getRoot());
        _jgraph.repaint();
        _graphPanner.repaint();
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard,
            Transferable transferable) {
    }

    /** Assuming the contents of the clipboard is MoML code, paste it into
     *  the current model by issuing a change request.
     */
    public void paste() {
	Clipboard clipboard =
	        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transferable = clipboard.getContents(this);
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	        (GraphController)graphPane.getGraphController();
	GraphModel model = controller.getGraphModel();
	if(transferable == null) return;
	try {
	    CompositeEntity toplevel = (CompositeEntity)model.getRoot();
            StringBuffer moml = new StringBuffer();
            // The pasted version will have the name prepended with
            // a unique number.  This isn't really what we want, but
            // it will have to do for now.  FIXME.
	    moml.append("<group name=\"" + _copyNumber + "\">\n");
            _copyNumber++;
	    moml.append((String)
                    transferable.getTransferData(DataFlavor.stringFlavor));
	    moml.append("</group>\n");
	    toplevel.requestChange(
                    new MoMLChangeRequest(this, toplevel, moml.toString()));
	} catch (Exception ex) {
	    MessageHandler.error("Copy failed", ex);
	}
    }

    /** Print the document to a printer, represented by the specified graphics
     *  object.  This method assumes that a view exists of the this document
     *  in the application.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {
	if(_jgraph != null) {
            return _jgraph.print(graphics, format, index);
        } else return NO_SUCH_PAGE;
    }

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     */
    public void setCenter(Point2D center) {
        // The center is in view coordinates, and we have to convert
        // it to find the coordinates of the upper left corner in the
        // raw coordinates.  This is a bit complex...
        JViewport viewport = _graphScrollPane.getViewport();
        Rectangle raw = viewport.getViewRect();
        Point2D upperLeft = new Point2D.Double(raw.getX(), raw.getY());
        Point2D lowerRight = new Point2D.Double(raw.getX() + raw.getWidth(),
               raw.getY() + raw.getHeight());

        // Reverse any current zoom or pan transformations.
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform transform = 
                canvas.getCanvasPane().getTransformContext().getTransform();
        try {
            Point2D viewUpperLeft = transform.inverseTransform(
                    upperLeft, null);
            Point2D viewLowerRight = transform.inverseTransform(
                    lowerRight, null);
            double width = viewLowerRight.getX() - viewUpperLeft.getX();
            double height = viewLowerRight.getY() - viewUpperLeft.getY();
            Point2D newUpperLeft = new Point2D.Double(
                    center.getX() - width/2.0, center.getY() - height/2.0);
            Point2D rawNewUpperLeft = transform.transform(newUpperLeft, null);
            double x = rawNewUpperLeft.getX();
            // Do not let raw coordinates go negative.
            if (x < 0.0) x = 0.0;
            double y = rawNewUpperLeft.getY();
            if (y < 0.0) y = 0.0;
            Point spec = new Point();

            // Do not let the raw coordinates overflow the canvas.
            if (x + raw.getWidth() > canvas.getWidth()) {
                x = canvas.getWidth() - raw.getWidth();
            }
            if (y + raw.getHeight() > canvas.getHeight()) {
                y = canvas.getHeight() - raw.getHeight();
            }

            spec.setLocation(x, y);
            viewport.setViewPosition(spec);
        } catch (NoninvertibleTransformException ex) {
            // This should not occur with just zoom and pan.
            // Ignore.
        }
    }

    /** Zoom in or out to magnify by the specified factor, from the current
     *  magnification.
     *  @param factor The magnification factor (relative to 1.0).
     */
    public void zoom(double factor) {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current = 
                canvas.getCanvasPane().getTransformContext().getTransform();
        // Translate the view center to zero before zooming,
        // then translate back.
        Point2D center = getCenter();
        current.scale(factor, factor);
        canvas.getCanvasPane().setTransform(current);
        setCenter(center);
        _graphPanner.repaint();
    }

    /** Zoom to fit the current figures.
     */
    public void zoomFit() {
	GraphPane pane = _jgraph.getGraphPane();
        ZList list = pane.getForegroundLayer().getFigures();
        Rectangle2D bounds = list.getBounds();
        if (list.getFigureCount() == 0) {
            // Empty diagram.
            return;
        }
        Rectangle size = _graphScrollPane.getViewport().getViewRect();
        AffineTransform newTransform =
                CanvasUtilities.computeFitTransform(bounds, size);
        JCanvas canvas = pane.getCanvas();
        canvas.getCanvasPane().setTransform(newTransform);
        _graphPanner.repaint();
    }

    /** Set zoom to the nominal.
     */
    public void zoomReset() {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current = 
                canvas.getCanvasPane().getTransformContext().getTransform();
        current.setToIdentity();
        canvas.getCanvasPane().setTransform(current);
        _graphPanner.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
	super._addMenus();

       	_editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
	_menubar.add(_editMenu);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _cutAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _cutAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _copyAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _copyAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _pasteAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _pasteAction);

        // May be null if there are not multiple views in the configuration.
        if (_viewMenu == null) {
            _viewMenu = new JMenu("View");
            _viewMenu.setMnemonic(KeyEvent.VK_V);
            _menubar.add(_viewMenu);
        } else {
            _viewMenu.addSeparator();
        }
	diva.gui.GUIUtilities.addHotKey(_jgraph, _zoomInAction);
	diva.gui.GUIUtilities.addMenuItem(_viewMenu, _zoomInAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _zoomResetAction);
	diva.gui.GUIUtilities.addMenuItem(_viewMenu, _zoomResetAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _zoomFitAction);
	diva.gui.GUIUtilities.addMenuItem(_viewMenu, _zoomFitAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _zoomOutAction);
	diva.gui.GUIUtilities.addMenuItem(_viewMenu, _zoomOutAction);

       	_graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
	_menubar.add(_graphMenu);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _layoutAction);
	diva.gui.GUIUtilities.addMenuItem(_graphMenu, _layoutAction);
	diva.gui.GUIUtilities.addHotKey(_jgraph, _saveInLibraryAction);
	diva.gui.GUIUtilities.addMenuItem(_graphMenu, _saveInLibraryAction);
      	diva.gui.GUIUtilities.addHotKey(_jgraph, _importLibraryAction);
	diva.gui.GUIUtilities.addMenuItem(_graphMenu, _importLibraryAction);
    }

    /** Create a new graph pane.  Subclasses will override this to change
     *  the pane that is created.  Note that this method is called in
     *  constructor, so derived classes must be careful to not reference
     *  local variables that may not have yet been created.
     *  @return The pane that is created.
     */
    protected abstract GraphPane _createGraphPane();

    /** Get the directory that was last accessed by this window.
     *  @see #_setDirectory
     *  @return The directory last accessed.
     */
    protected File _getDirectory() {
        // NOTE: This method is necessary because we wish to have
        // this accessed by inner classes, and there is a bug in
        // jdk1.2.2 where inner classes cannot access protected
        // static members.
        return _directory;
    }

    /** Query the user for a filename and save the model to that file.
     *  This overrides the base class so that if we are in
     *  an inside composite actor, then only that composite actor is
     *  saved.  In addition, since the superclass clones the model,
     *  we need to clear and reconstruct the model.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        try {
            _saveAsFlag = true;
            return super._saveAs();
        } finally {
            _saveAsFlag = false;
        }
    }

    /** Set the directory that was last accessed by this window.
     *  @see #_getDirectory
     *  @param directory The directory last accessed.
     */
    protected void _setDirectory(File directory) {
        // NOTE: This method is necessary because we wish to have
        // this accessed by inner classes, and there is a bug in
        // jdk1.2.2 where inner classes cannot access protected
        // static members.
        _directory = directory;
    }

    /** Write the model to the specified file.  This overrides the base
     *  class to record the current size and position of the window
     *  in the model.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        // First, record size and position.
        try {
            SizeAttribute bounds = (SizeAttribute)getModel().getAttribute(
                     "_vergilSize", SizeAttribute.class);
            if (bounds == null) {
                bounds = new SizeAttribute(getModel(), "_vergilSize");
            }
            bounds.recordSize(_graphScrollPane);

            LocationAttribute location
                    = (LocationAttribute)getModel().getAttribute(
                    "_vergilLocation", LocationAttribute.class);
            if (location == null) {
                location = new LocationAttribute(getModel(), "_vergilLocation");
            }
            location.recordLocation(this);
        } catch (Exception ex) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }
        // NOTE: This used to override the base class so that saveAs
        // on a submodel would save only the submodel.  But this was
        // strange, to have behavior different from save, and also it
        // broke save for top-level modal models.  So now we just do
        // the same thing in saveAs as in save.
        /*
        if (_saveAsFlag && getModel().getContainer() != null) {
            java.io.FileWriter fout = new java.io.FileWriter(file);
            getModel().exportMoML(fout);
            fout.close();
        } else {
            super._writeFile(file);
        }
        */

        super._writeFile(file);
    }

    /** Remove the listeners we have created when the frame closes.
     *  Remove our panner-updating listener from the entity.
     *  Also remove the listeners our graph model has created.
     */
    protected boolean _close() {
        getModel().removeChangeListener(this);
        GraphModel gm = _jgraph.getGraphPane().getGraphModel();
        if (gm instanceof AbstractPtolemyGraphModel) {
            ((AbstractPtolemyGraphModel)gm).removeListeners();
        }
        return super._close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Action for zooming in. */
    private Action _zoomInAction = new ZoomInAction();

    /** Action for zoom reset. */
    private Action _zoomResetAction = new ZoomResetAction();

    /** Action for zoom fitting. */
    private Action _zoomFitAction = new ZoomFitAction();

    /** Action for zooming out. */
    private Action _zoomOutAction = new ZoomOutAction();

    // NOTE: should be somewhere else?
    // Default background color is a light grey.
    protected static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    protected JGraph _jgraph;
    protected JScrollPane _graphScrollPane;
    protected JPanner _graphPanner;
    protected JTree _library;
    protected EntityTreeModel _libraryModel;
    protected JScrollPane _libraryScrollPane;
    protected JPanel _palettePane;
    protected JSplitPane _splitPane;

    protected JToolBar _toolbar;
    protected JMenu _editMenu;
    protected Action _cutAction;
    protected Action _copyAction;
    protected Action _pasteAction;
    protected JMenu _graphMenu;
    protected Action _layoutAction;
    protected Action _saveInLibraryAction;
    protected Action _importLibraryAction;

    // Flag indicating that the current save action is "save as" rather than
    // "save".
    private boolean _saveAsFlag = false;

    // The library.
    protected CompositeEntity _topLibrary;

    // The number that will be used to create a unique name
    // for the next copy operation.
    private int _copyNumber = 1;

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /////////////////////////////////////////////////////////////////////
    //// CopyAction

    /** Action to copy the current selection. */
    private class CopyAction extends AbstractAction {

        /** Create a new action to copy the current selection. */
	public CopyAction() {
	    super("Copy");
	    putValue("tooltip",
                    "Copy the current selection onto the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_C,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_C));
	}

        /** Copy the current selection. */
	public void actionPerformed(ActionEvent e) {
	    copy();
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// CutAction

    /** Action to copy and delete the current selection. */
    private class CutAction extends AbstractAction {

        /** Create a new action to copy and delete the current selection. */
	public CutAction() {
	    super("Cut");
	    putValue("tooltip",
                    "Cut the current selection onto the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_X,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_T));
	}

        /** Copy and delete the current selection. */
	public void actionPerformed(ActionEvent e) {
	    cut();
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// EditIconAction

// 'Edit Icon' pop up menu not shipped with PtII1.0.
// See also ptolemy/vergil/ptolemy/kernel/KernelGraphFrame.java
//     public class EditIconAction extends FigureAction {
// 	public EditIconAction() {
// 	    super("Edit Icon");
// 	}

// 	public void actionPerformed(ActionEvent e) {
// 	    // Figure out what entity.
// 	    super.actionPerformed(e);
// 	    NamedObj object = getTarget();
// 	    if(!(object instanceof Entity)) return;
// 	    Entity entity = (Entity) object;
// 	    XMLIcon icon = null;
// 	    List iconList = entity.attributeList(XMLIcon.class);
// 	    if(iconList.size() == 0) {
// 		try {
// 		    icon = new XMLIcon(entity, entity.uniqueName("icon"));
// 		} catch (Exception ex) {
// 		    throw new InternalErrorException(
//                             "duplicated name, but there were no other icons.");
// 		}
// 	    } else if(iconList.size() == 1) {
// 		icon = (XMLIcon)iconList.get(0);
// 	    } else {
// 		throw new InternalErrorException("entity " + entity +
//                         " contains more than one icon");
// 	    }
// 	    // FIXME make a tableau.
// 	    ApplicationContext appContext = new ApplicationContext();
// 	    appContext.setTitle("Icon editor");
// 	    new IconEditor(appContext, icon);
// 	}
//     }

    /////////////////////////////////////////////////////////////////////
    //// ExecuteSystemAction

    /** An action to open a run control window. */
    private class ExecuteSystemAction extends AbstractAction {

        /** Construct an action to execute the model. */
	public ExecuteSystemAction() {
	    super("Go");
	    putValue("tooltip", "Execute The Model");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_G,
                    java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_G));
	}

        /** Open a run control window. */
	public void actionPerformed(ActionEvent e) {
	    try {
		PtolemyEffigy effigy =
		    (PtolemyEffigy)getTableau().getContainer();
		new RunTableau(effigy, effigy.uniqueName("tableau"));
	    } catch (Exception ex) {
		MessageHandler.error("Execution Failed", ex);
	    }
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// ImportLibraryAction

    /** An action to import a library of components. */
    private class ImportLibraryAction extends AbstractAction {

        /** Create a new action to import a library of components. */
	public ImportLibraryAction() {
	    super("Import Library");
	    putValue("tooltip", "Import a library into the Palette");
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
	}

        /** Import a library by first opening a file chooser dialog and
         *  then importing the specified library.
         */
	public void actionPerformed(ActionEvent e) {
	    // NOTE: this code is mostly copied from Top.
	    JFileChooser chooser = new JFileChooser();
	    chooser.setDialogTitle("Select a library");

	    if (_getDirectory() != null) {
		chooser.setCurrentDirectory(_getDirectory());
	    } else {
		// The default on Windows is to open at user.home, which is
		// typically an absurd directory inside the O/S installation.
		// So we use the current directory instead.
		// FIXME: This will throw a security exception in an applet?
		String cwd = System.getProperty("user.dir");
		if (cwd != null) {
		    chooser.setCurrentDirectory(new File(cwd));
		}
	    }
	    int result = chooser.showOpenDialog(GraphFrame.this);
	    if (result == JFileChooser.APPROVE_OPTION) {
		try {
		    File file = chooser.getSelectedFile();
		    // FIXME it would be nice if MoMLChangeRequest had the
		    // ability to read from a URL
		    StringBuffer buffer = new StringBuffer();
		    FileReader reader = new FileReader(file);
		    char[] chars = new char[50];
		    while(reader.ready()) {
			int count = reader.read(chars, 0, 50);
			buffer.append(chars, 0, count);
		    }
		    PtolemyEffigy effigy =
			(PtolemyEffigy)getTableau().getContainer();
		    Configuration configuration =
			(Configuration)effigy.toplevel();
		    NamedObj library =
			configuration.getEntity("actor library");
		    if(library == null) return;
		    ChangeRequest request =
			new MoMLChangeRequest(this, library,
                                buffer.toString(),
                                file.toURL());
		    library.requestChange(request);
                    _setDirectory(chooser.getCurrentDirectory());
		} catch (Exception ex) {
		    MessageHandler.error("Library import failed.", ex);
		}
	    }
	}
    };

    /////////////////////////////////////////////////////////////////////
    //// LayoutAction

    /** Action to automatically lay out the graph. */
    private class LayoutAction extends AbstractAction {

        /** Create a new action to automatically lay out the graph. */
	public LayoutAction() {
	    super("Automatic Layout");
	    putValue("tooltip", "Layout the Graph");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_L,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_L));
	}

        /** Lay out the graph. */
	public void actionPerformed(ActionEvent e) {
	    try {
		layoutGraph();
	    } catch (Exception ex) {
		MessageHandler.error("Layout failed", ex);
	    }
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// PasteAction

    /** Paste the current contents of the clipboard into the current model. */
    private class PasteAction extends AbstractAction {

        /** Create a new action to paste the current contents of the
         *  clipboard into the current model.
         */
	public PasteAction() {
	    super("Paste");
	    putValue("tooltip",
                    "Paste the contents of the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_V,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_P));
	}

        /** Paste the current contents of the clipboard into
         *  the current model.
         */
	public void actionPerformed(ActionEvent e) {
	    paste();
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// PtolemyLayout

    /** A layout algorithm for laying out ptolemy graphs.  Since our edges
     *  are undirected, this layout algorithm turns them into directed edges
     *  aimed consistently. i.e. An edge should always be "out" of an
     *  internal output port and always be "in" of an internal input port.
     *  Conversely, an edge is "out" of an external input port, and "in" of
     *  an external output port.  The copying operation also flattens
     *  the graph, because the level layout algorithm doesn't understand
     *  how to layout hierarchical nodes.
     */
    private class PtolemyLayout extends LevelLayout {

        // FIXME: input ports should be on left, and output ports on right.

	/** Construct a new levelizing layout with a vertical orientation. */
	public PtolemyLayout(LayoutTarget target) {
	    super(target);
	}

	/** Copy the given graph and make the nodes/edges in the copied
	 *  graph point to the nodes/edges in the original.
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
	    Iterator i =
		GraphUtilities.partiallyContainedEdges(origComposite, model);
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

    /////////////////////////////////////////////////////////////////////
    //// PtolemyLayoutTarget

    /** A layout target that translates locatable nodes. */
    private class PtolemyLayoutTarget extends BasicLayoutTarget {

	/** Construct a new layout target that operates
	 *  in the given pane.
	 */
	public PtolemyLayoutTarget(GraphController controller) {
	    super(controller);
	}

	/** Translate the figure associated with the given node in the
	 *  target's view by the given delta.
	 */
	public void translate(Object node, double dx, double dy) {
	    super.translate(node, dx, dy);
	    // NOTE: this is not the best way to handle locatables.
	    if(node instanceof Location) {
		double location[] = ((Location)node).getLocation();
		if(location == null) {
		    location = new double[2];
		    Figure figure = getController().getFigure(node);
		    location[0] = figure.getBounds().getCenterX();
		    location[1] = figure.getBounds().getCenterY();
		} else {
		    location[0] += dx;
		    location[1] += dy;
		}
                try {
                    ((Location)node).setLocation(location);
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.getMessage());
                }
 	    }
	}
    }

    /////////////////////////////////////////////////////////////////////
    //// SaveInLibraryAction

    // FIXME: The following needs quite a bit of work.
    // Changes to the library are not persistent.
    /** An action to save the current model in a library. */
    private class SaveInLibraryAction extends AbstractAction {

        /** Create a new action to save a model in a library. */
	public SaveInLibraryAction() {
	    super("Save In Library");
	    putValue("tooltip", "Save as a Component in Library");
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_S));
	}

        /** Create a new instance of the current model in the
         *  actor library of the configuration.
         */
	public void actionPerformed(ActionEvent e) {
	    try {
		PtolemyEffigy effigy =
		        (PtolemyEffigy)getTableau().getContainer();
		NamedObj object = effigy.getModel();
		if(object == null) return;
		StringWriter buffer = new StringWriter();
		object.exportMoML(buffer, 1);
		Configuration configuration = (Configuration)effigy.toplevel();
		NamedObj library = configuration.getEntity("actor library");
		if(library == null) return;
                ChangeRequest request =
		        new MoMLChangeRequest(this, library, buffer.toString());
		library.requestChange(request);
	    } catch (IOException ex) {
		// Ignore.
	    }
	}
    }

    ///////////////////////////////////////////////////////////////
    //// ZoomInAction

    // An action to zoom in.
    public class ZoomInAction extends AbstractAction {
	public ZoomInAction() {
	    super("Zoom In");
            // Load the image by using the absolute path to the gif.
	    // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
	    // For more information, see
	    // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/ptolemy/img/zoomin.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
	    putValue("tooltip", "Zoom in");
            // NOTE: The following assumes that the + key is the same
            // as the = key.  Unfortunately, the VK_PLUS key event doesn't
            // work, so we have to do it this way.
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                            java.awt.Event.CTRL_MASK
                            | java.awt.Event.SHIFT_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_Z));
	}

	public void actionPerformed(ActionEvent e) {
            zoom(1.25);
	}
    }

    ///////////////////////////////////////////////////////////////
    //// ZoomResetAction

    // An action to reset zoom.
    public class ZoomResetAction extends AbstractAction {
	public ZoomResetAction() {
	    super("Zoom Reset");
            // Load the image by using the absolute path to the gif.
	    // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
	    // For more information, see
	    // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/ptolemy/img/zoomreset.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
	    putValue("tooltip", "Zoom reset");
            // NOTE: The following assumes that the + key is the same
            // as the = key.  Unfortunately, the VK_PLUS key event doesn't
            // work, so we have to do it this way.
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
	}

	public void actionPerformed(ActionEvent e) {
            zoomReset();
	}
    }

    ///////////////////////////////////////////////////////////////
    //// ZoomFitAction

    // An action to zoom in.
    public class ZoomFitAction extends AbstractAction {
	public ZoomFitAction() {
	    super("Zoom Fit");
            // Load the image by using the absolute path to the gif.
	    // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
	    // For more information, see
	    // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/ptolemy/img/zoomfit.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
	    putValue("tooltip", "Zoom fit");
            // NOTE: The following assumes that the + key is the same
            // as the = key.  Unfortunately, the VK_PLUS key event doesn't
            // work, so we have to do it this way.
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_F));
	}

	public void actionPerformed(ActionEvent e) {
            zoomFit();
	}
    }

    ///////////////////////////////////////////////////////////////
    //// ZoomOutAction

    // An action to zoom out.
    public class ZoomOutAction extends AbstractAction {
	public ZoomOutAction() {
	    super("Zoom Out");
            // Load the image by using the absolute path to the gif.
	    // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
	    // For more information, see
	    // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/ptolemy/img/zoomout.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
	    putValue("tooltip", "Zoom out");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                            java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_U));
	}

	public void actionPerformed(ActionEvent e) {
            zoom(1.0/1.25);
	}
    }
}
