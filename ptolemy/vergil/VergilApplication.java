/* A user interface application for component-based design.

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

package ptolemy.vergil;

import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.MoMLParser;

import diva.graph.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.DefaultBundle;
import diva.resource.RelativeBundle;
import java.awt.Event;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
Vergil is an extensible high-level graphical interface for component-based
design tools.  It is primarily aimed at Ptolemy II, although it could
be used with other tools as well.  The features of Vergil include:
<ul>
<li> Support for multiple types of documents.
<li> A module system to allow for easy extension.
<li> Expandable tool bars and menu bars.
</ul>
<p>
This class is associated with a desktop frame.  This frame contains a palette
into which packages can place design libraries.  The frame also inherits
improved support for multiple documents, a toolbar, status bar and
progress bar from the Diva desktop frame.
<p>
Only a singleton instance of this class ever exists, which is created by 
the static main method.

@author Steve Neuendorffer
@contributor John Reekie
@version $Id$
*/
public class VergilApplication extends MDIApplication {
    /**
     * Construct a new Vergil application.  Create a new desktop frame and
     * initialize it's menu bar and toolbar.  Load all the modular packages
     * for this application and open a starting document using the default
     * document factory.
     */
    protected VergilApplication() {
        super();

        // Create local objects
	//JTreePane treepane = new JTreePane(".");
       	
	DesktopFrame frame = new DesktopFrame(this, new JPanel());
        setApplicationFrame(frame);
    
        // Create and initialize the storage policy
        DefaultStoragePolicy storage = new DefaultStoragePolicy();
        setStoragePolicy(storage);
	FileFilter filter = new FileFilter() {
	    public boolean accept(File file) {
		if(file.isDirectory()) {
		    return true;
		}
		else {
		    return GUIUtilities.getFileExtension(file).
                        toLowerCase().equals("xml");
		}
	    }
	    public String getDescription() {
		return "XML files";
	    }
	};
        JFileChooser chooser;
        chooser = storage.getOpenFileChooser();
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        chooser = storage.getSaveFileChooser();
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
    
        // _incrementalLayout = new LevelLayout();

        // Initialize the menubar, toolbar, and palettes
        _initializeMenuBar(frame.getJMenuBar());
        _initializeToolBar(frame.getJToolBar());
	JPanel toolBarPane = frame.getToolBarPane();
	toolBarPane.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        Icon icon = getResources().getImageIcon("GraphIconImage");
        Image iconImage = getResources().getImage("GraphIconImage");

        frame.setFrameIcon(icon);
        frame.setIconImage(iconImage);

        setCurrentDocument(null);

	classLoadingService = new ClassLoadingService();
	addService(classLoadingService);
	// FIXME read this out of resources somehow.
	new ptolemy.vergil.ptolemy.PtolemyModule(this);
        new ptolemy.vergil.debugger.DebuggerModule(this);
	
	// Start with a new document.
	// This is kindof
	// bogus, but it is not easy to fire the action manually.
	Action action = getAction(DefaultActions.NEW);
	// FIXME this is really a horrible horrible hack.
	javax.swing.Timer timer = new javax.swing.Timer(200, action);
	timer.setRepeats(false);
	timer.start();
    }

    public ClassLoadingService classLoadingService;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add the factory that creates new documents.  Also create a new
     * action and add it to the
     * File->New menu that will create documents with the given factory.
     */
    public void addDocumentFactory(VergilDocumentFactory factory) {
	final VergilDocumentFactory f = factory;
	final VergilApplication app = this;
        _documentFactoryList.add(factory);
	Action action = new AbstractAction (f.getName()) {
            public void actionPerformed(ActionEvent e) {
                Document doc = f.createDocument(app);
                addDocument(doc);
                displayDocument(doc);
                setCurrentDocument(doc);
            }
        };
	_fileNewMenu.add(action);
    }

    /**
     * Add the menu to the menu bar of this application.
     */
    public void addMenu(JMenu menu) {
	JFrame frame = getApplicationFrame();
	if(frame == null) return;
	JMenuBar menuBar = frame.getJMenuBar();
	menuBar.add(menu);
    }

    /**
     * Add the service to the list of services that are published in
     * this application.
     */
    public void addService(Service service) {
	_serviceList.add(service);
    }

    /** 
     * Given a Diva document, create a new view that displays that
     * document. If the document is an instance of VergilDocument
     * then defer to the document to create the view.
     * @exception RuntimeException If the document is not an instance of 
     * VergilDocument.
     * @see #VergilDocument
     */
    public JComponent createView(Document document) {
	if(!(document instanceof VergilDocument)) {
	    throw new RuntimeException("Can only create views " +
				       "on VergilDocuments.");
	}
	JComponent view = ((VergilDocument)document).createView();
        return view;
    }
        
    /** 
     * Return the list of factories that create new documents.
     * @return An unmodifiable list of instances of DocumentFactory.
     */
    public List documentFactoryList () {
        return Collections.unmodifiableList(_documentFactoryList);
    }

    /** 
     * Return the default document factory.  If there is no default factory, 
     * then return null.  
     * @return The first factory added with the addDocumentFactory method.
     */
    public DocumentFactory getDocumentFactory() { 
        if(_documentFactoryList.size() < 1) 
            return null; 
        else 
	    return (DocumentFactory)_documentFactoryList.get(0);
    }

    /** 
     * Return the instance of this class that makes up the
     * application.
     */
    public static VergilApplication getInstance() {
	return _instance;
    }

    /** 
     * Get the title of this application.  This class returns
     * the string "Vergil", although subclasses may override this.
     */
    public String getTitle() {
        return "Vergil";
    }

    /** 
     * Create a new instance of VergilApplication and make it visible.  
     * The application object is responsible for creating the persistent user
     * interface which will remain after this method returns.
     */
    public static void main(String argv[]) {
        _instance = new VergilApplication();
        _instance.setVisible(true);
    }

   /** 
    * Redisplay a document after it appears on the screen. This method is 
    * called when a document is set to be the current document.  It is 
    * intended to allow an application to perform some action at this point,
    * such as executing a graph layout algorithm.  In this class, do nothing.
    */
    public void redisplay(Document document, JComponent view) {       
    }

    /** 
     * Remove the given factory that creates new documents from
     * this application.  Remove its entry in the File->New menu.
     */
    public void removeDocumentFactory(VergilDocumentFactory factory) {
	int index = _documentFactoryList.indexOf(factory);
        _documentFactoryList.remove(factory);
	_fileNewMenu.remove(_fileNewMenu.getItem(index));
    }

    /**
     * Remove the given menu from the menu bar of this application.
     */
    public void removeMenu(JMenu menu) {
	JFrame frame = getApplicationFrame();
	if(frame == null) return;
	JMenuBar menuBar = frame.getJMenuBar();
	menuBar.remove(menu);
    }

    /**
     * Remove the service to the list of services that are published in
     * this application.
     */
    public void removeService(Service service) {
	_serviceList.remove(service);
    }

    /** 
     * Return the list of services that are published in this application.
     * @return An unmodifiable list of instances of Service.
     */
    public List serviceList () {
        return Collections.unmodifiableList(_serviceList);
    }

    /** 
     * Return the list of services that are published in this application that
     * are instances of the specified class.
     * @return An unmodifiable list of instances of the given Service.
     */
    public List serviceList (Class filter) {
	List result = new LinkedList();
	Iterator services = _serviceList.iterator();
	while (services.hasNext()) {
	    Service service = (Service) services.next();
	    if (filter.isInstance(service)) {
		result.add(service);
	    }
	}
	return Collections.unmodifiableList(result);
    }

    /** 
     * Set the given document to be the current document, and raise
     * the internal window that corresponds to that document and give it
     * the keyboard focus.
     * If given document is not null, 
     * then ensure that the "Save" and "Save As"
     * actions are enabled.  If the given document is null, then disable
     * those actions.
     * @param document The document to set as the current document, or
     * null to set that there is no current document.
     */
    public void setCurrentDocument(Document document) {
        super.setCurrentDocument(document);

        if(document == null) {
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(false);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(false);
        } else {
	    JComponent view = getView(document);
	    if (!view.hasFocus()) {
		view.requestFocus();
	    }
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(true);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(true);
        }

    }

    /** 
     * Throw an Exception.  Vergil uses a factory list instead of a 
     * single factory.  
     * @deprecated Use addDocumentFactory to add a document factory.
     */
    public void setDocumentFactory(DocumentFactory factory) {
	throw new RuntimeException("setDocumentFactory is not allowed, use " + 
				   "addDocumentFactory instead.");
    }

   /** Show the error without the stack trace by default.
     */
    public void showError(String op, Exception e) {
	GUIUtilities.showException(getApplicationFrame(), e, op);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** 
     * Initialize the given menubar. Create a new File menu with 
     * "New", "Open", "Close", "Save", "Save As", and "Exit" items.
     * The "New" item is a submenu that contains one item for each
     * document factory contained in this application.  The other items
     * are actions that defer to the current document for their
     * functionality.
     */
    private void _initializeMenuBar(JMenuBar menuBar) {
        Action action;
        JMenuItem item;
	// FIXME pull the strings out of resources instead of hardcoding.

        // Create the File menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        menuBar.add(menuFile);

        _fileNewMenu = new JMenu("New");
        _fileNewMenu.updateUI();
        _fileNewMenu.setMnemonic('N');
	menuFile.add(_fileNewMenu);

        action = DefaultActions.openAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'O', "Open a graph document");

        action = DefaultActions.closeAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'C', "Close the current graph document");

        menuFile.addSeparator();

        action = DefaultActions.saveAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'S', "Save the current graph document");

        action = DefaultActions.saveAsAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'A',
                "Save the current graph document to a different file");

        action = DefaultActions.printAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'P', "Print current document");

        menuFile.addSeparator();

        action = DefaultActions.exitAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'X', "Exit from the graph editor");

        // Create the File menu
        menuFile = new JMenu("Edit");
        menuFile.setMnemonic('E');
        menuBar.add(menuFile);

	// FIXME implement cut.
	//action = DefaultActions.cutAction(this);
        //addAction(action);
        //addMenuItem(menuFile, action, 'u', "Cut");

        action = DefaultActions.copyAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'C', "Copy");
	getApplicationFrame().getRootPane().
	    registerKeyboardAction(action, "Copy",
	    KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK),
	    JComponent.WHEN_IN_FOCUSED_WINDOW);	

        action = DefaultActions.pasteAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'P', "Paste");
	getApplicationFrame().getRootPane().
	    registerKeyboardAction(action, "Paste",
	    KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK),
	    JComponent.WHEN_IN_FOCUSED_WINDOW);	

    }

    /** 
     * Initialize the given toolbar.  Create a new buttons that correspond
     * to the "New", "Open" and "Save" actions in the menu bar.
     */
    private void _initializeToolBar(JToolBar toolBar) {
        Action action;
        RelativeBundle resources = getResources();

	// Conventional new/open/save buttons
	action = DefaultActions.newAction(this);
        addAction(action);
	addToolBarButton(toolBar, action, null, 
			 resources.getImageIcon("NewImage"));
        action = getAction(DefaultActions.OPEN);
        addToolBarButton(toolBar, action, null, 
			 resources.getImageIcon("OpenImage"));
        action = getAction(DefaultActions.SAVE);
        addToolBarButton(toolBar, action, null, 
			 resources.getImageIcon("SaveImage"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director selection combobox.
    private JComboBox _directorComboBox;

    // The layout selection combobox.
    private JComboBox _layoutComboBox;

    // The list of factories that create graph documents.
    private List _documentFactoryList = new LinkedList();

    // The File->New menu.  Each document factory will appear in this menu.
    private JMenu _fileNewMenu = null;

    // The instance of this application.
    private static VergilApplication _instance = null;

    // The list of factories that create graph documents.
    private List _serviceList = new LinkedList();
}
















