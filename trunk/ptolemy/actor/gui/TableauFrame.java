/* Top-level window for Ptolemy models with a menubar and status bar.

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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (celaine@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import java.net.URL;
import java.awt.Image;
import java.awt.Toolkit;

//////////////////////////////////////////////////////////////////////////
//// TableauFrame
/**
This is a top-level window associated with a tableau that has
a menubar and status bar. Derived classes should add components
to the content pane using a line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>
The base class provides generic features for menubars and toolbars,
and this class specializes the base class for Ptolemy II.

@author Edward A. Lee
@version $Id$
*/
public abstract class TableauFrame extends Top {

    /** Construct an empty top-level frame.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     */
    public TableauFrame() {
        super();
        setIconImage(_getDefaultIconImage());
    }

    /** Construct an empty top-level frame managed by the specified
     *  tableau. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     *  @param tableau The managing tableau.
     */
    public TableauFrame(Tableau tableau) {
        super();
        setTableau(tableau);
        setIconImage(_getDefaultIconImage());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the configuration at the top level of the hierarchy.
     *  @return The configuration controlling this frame, or null
     *   if there isn't one.
     */
    public Configuration getConfiguration() {
        NamedObj tableau = getTableau();
        if (tableau != null) {
            NamedObj toplevel = tableau.toplevel();
            if (toplevel instanceof Configuration) {
                return (Configuration)toplevel;
            }
        }
        return null;
    }

    /** Get the model directory in the top level configuration.
     *  @return The model directory, or null if there isn't one.
     */
    public ModelDirectory getDirectory() {
        Configuration configuration = getConfiguration();
        return configuration.getDirectory();
    }

    /** Get the effigy for the model associated with this window.
     *  @return The effigy for the model, or null if none exists.
     */
    public Effigy getEffigy() {
        if (_tableau != null) {
            return (Effigy)_tableau.getContainer();
        }
        return null;
    }

    /** Get the effigy for the specified Ptolemy model.
     *  This searches all instances of PtolemyEffigy deeply contained by
     *  the directory, and returns the first one it encounters
     *  that is an effigy for the specified model.
     *  @param model The model for which an effigy is desired.
     *  @return The effigy for the model, or null if none exists.
     */
    public PtolemyEffigy getEffigy(NamedObj model) {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            return configuration.getEffigy(model);
        } else {
            return null;
        }
    }

    /** Get the tableau associated with this frame.
     *  @return The tableau associated with this frame.
     */
    public Tableau getTableau() {
        return _tableau;
    }

    /** Return true if the data associated with this window has been
     *  modified since it was first read or last saved.  This returns
     *  the value set by calls to setModified(), or false if that method
     *  has not been called.
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        Effigy effigy = getEffigy();
        if (effigy != null) {
            return effigy.isModified();
        } else {
            return super.isModified();
        }
    }

    /** Record whether the data associated with this window has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent attempts to close
     *  the window will trigger a dialog box to confirm the closing.
     *  This overrides the base class to delegate to the effigy.
     *  @param modified True if the data has been modified.
     */
    public void setModified(boolean modified) {
        Effigy effigy = getEffigy();
        if (effigy != null) {
            effigy.setModified(modified);
        } else {
            super.setModified(modified);
        }
    }

    /** Set the tableau associated with this frame.
     *  @param tableau The tableau associated with this frame.
     */
    public void setTableau(Tableau tableau) {
	_tableau = tableau;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to open the intro.htm splash window,
     *  which is in the directory ptolemy/configs.
     */
    protected void _about() {
        // NOTE: We take some care here to ensure that this window is
        // only opened once.
        ModelDirectory directory = getDirectory();
        if (directory != null) {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/intro.htm");
            // Check to see whether the model is already open.
            Effigy effigy = directory.getEffigy(doc.toExternalForm());
            if (effigy == null) {
                try {
                    // No main welcome window.  Create one.
                    EffigyFactory effigyFactory = new HTMLEffigyFactory(
                            directory.workspace());
                    effigy = effigyFactory.createEffigy(
                            directory, (URL)null, doc);

                    effigy.identifier.setExpression(doc.toExternalForm());
                    effigy.url.setURL(doc);
                    // If this fails, we do not want the effigy
                    // in the directory.
                    try {
                        // Create a tableau if there is a tableau factory.
                        TableauFactory factory = (TableauFactory)
                            getConfiguration().getAttribute("tableauFactory");
                        if (factory != null) {
                            Tableau tableau = factory.createTableau(effigy);
                            if (tableau == null) {
                                throw new Exception("Can't create Tableau.");
                            }
                            // The first tableau is a master.
                            tableau.setMaster(true);
                            // NOTE: This size is the same as what's in
                            // the welcome window XML files in configs.
                            tableau.size.setExpression("650x320");
                            tableau.show();
                            return;
                        }
                    } catch (Exception ex) {
                        // Remove effigy.
                        effigy.setContainer(null);
                    }
                } catch (Exception ex) {}
            } else {
                // Model already exists.
                effigy.showTableaux();
                return;
            }
        }
        // Don't report any errors.  Just use the default.
        super._about();
    }

    /** Add a View menu and items to the File:New menu
     *  if a tableau was given in the constructor.
     */
    protected void _addMenus() {
        super._addMenus();
        if (_tableau != null) {
            // Start with the File:New menu.
	    // Check to see if we have an effigy factory, and whether it
            // is capable of creating blank effigies.
	    final Configuration configuration = getConfiguration();
	    EffigyFactory effigyFactory =
                (EffigyFactory)configuration.getEntity("effigyFactory");
            boolean canCreateBlank = false;
            final ModelDirectory directory = getDirectory();
	    if(effigyFactory != null && directory != null) {
                List factoryList =
                    effigyFactory.entityList(EffigyFactory.class);
	        Iterator factories = factoryList.iterator();
                while(factories.hasNext()) {
                    final EffigyFactory factory =
                        (EffigyFactory)factories.next();
                    if (!factory.canCreateBlankEffigy()) continue;
                    canCreateBlank = true;
                    String name = factory.getName();
                    ActionListener menuListener = new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                Effigy effigy = null;
                                try {
                                    effigy = factory.createEffigy(directory);
                                } catch (Exception ex) {
                                    MessageHandler.error(
                                            "Could not create new effigy", ex);
                                }
                                configuration.createPrimaryTableau(effigy);
                            }
                        };
                    JMenuItem item = new JMenuItem(name);
                    item.setActionCommand(name);
                    item.setMnemonic(name.charAt(0));
                    item.addActionListener(menuListener);
                    ((JMenu)_fileMenuItems[2]).add(item);
                }
            }
            if (canCreateBlank) {
		// Enable the "New" item in the File menu.
		_fileMenuItems[2].setEnabled(true);
	    }

            // Next do the View menu.
	    Effigy tableauContainer = (Effigy)_tableau.getContainer();
            if (tableauContainer != null) {
                _factoryContainer = tableauContainer.getTableauFactory();
                if (_factoryContainer != null) {
                    // If setTableau() has been called on the effigy,
                    // then there are multiple possible views of data
                    // represented in this top-level window.
                    // Thus, we create a View menu here.
                    _viewMenu = new JMenu("View");
                    _viewMenu.setMnemonic(KeyEvent.VK_V);
                    _menubar.add(_viewMenu);
                    ViewMenuListener viewMenuListener = new ViewMenuListener();
                    Iterator factories =
                        _factoryContainer.attributeList(TableauFactory.class)
                        .iterator();
                    while (factories.hasNext()) {
                        TableauFactory factory
                            = (TableauFactory)factories.next();
                        String name = factory.getName();
                        JMenuItem item = new JMenuItem(name);
                        // The "action command" is available to the listener.
                        item.setActionCommand(name);
                        item.setMnemonic(name.charAt(0));
                        item.addActionListener(viewMenuListener);
                        _viewMenu.add(item);
                    }
                }
            }
        }
    }

    /** Close the window.  Derived classes should override this to
     *  release any resources or remove any listeners.  In this class,
     *  if the data associated with this window has been modified,
     *  and there are no other tableaux in the parent effigy or
     *  any effigy that contains it,
     *  then ask the user whether to save the data before closing.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        // If we were given no tableau, then just close the window
        if(getEffigy() == null) {
            return super._close();
        }
        
        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        Effigy topEffigy = getEffigy().topEffigy();
        if (topEffigy != null && topEffigy.numberOfOpenTableaux() > 1) {
            // There are other tableau, so just close.
            dispose();
            return true;
        } else {
            return super._close();
        }
    }

    /** Close all open tableaux, querying the user as necessary to save data,
     *  and then exit the application.  If the user cancels on any save,
     *  then do not exit.
     *  @see Tableau#close()
     */
    protected void _exit() {
        ModelDirectory directory = getDirectory();
        Iterator effigies = directory.entityList(Effigy.class).iterator();
        while (effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            if (!effigy.closeTableaux()) return;
            try {
                effigy.setContainer(null);
            } catch (Exception ex) {
                throw new InternalErrorException(
                        "Unable to set effigy container to null! " + ex);
            }
        }
        // Some of the effigies closed may have triggered other
        // effigies being opened (if they were unnamed, and a saveAs()
        // was triggered).  So we need to close those now.
        // This is just a repeat of the above.
        effigies = directory.entityList(Effigy.class).iterator();
        while (effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            if (!effigy.closeTableaux()) return;
            try {
                effigy.setContainer(null);
            } catch (Exception ex) {
                throw new InternalErrorException(
                        "Unable to set effigy container to null! " + ex);
            }
        }
    }

    /** Return the default icon image, or null if there is none.
     *  Note that Frame.setIconImage(null) will set the image to the
     *  default platform dependent image for us.
     *  @return The default icon image, or null if there is none.
     */
    protected Image _getDefaultIconImage() {
	if(_defaultIconImage == null) {
	    // Note that PtolemyIISmallIcon.gif is also in doc/img.
	    // We place a duplicate copy here to make it easy to ship
	    // jar files that contain all the appropriate images.
	    URL url =
		getClass().getResource(
                        "/ptolemy/actor/gui/PtolemyIISmallIcon.gif");
	    if (url == null) {
		return null;
	    }
	    Toolkit tk = Toolkit.getDefaultToolkit();
            _defaultIconImage = tk.createImage(url);
	}
	return _defaultIconImage;
    }

    /** Get the name of this object, which in this class is the URL
     *  associated with the effigy, or the string "Unnamed" if none.
     *  This overrides the base class to provide a reasonable name
     *  for the title of the window.
     *  @return The name.
     */
    protected String _getName() {
        Effigy effigy = getEffigy();
        if (effigy != null) {
            URL url = effigy.url.getURL();
            if (url != null) {
                return url.toExternalForm();
            }
        }
        return "Unnamed";
    }

    /** Read the specified URL.  This delegates to the ModelDirectory
     *  to ensure that the preferred tableau of the model is opened, and
     *  that a model is not opened more than once.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected void _read(URL url) throws Exception {
        if (_tableau == null) {
            throw new Exception("No associated Tableau!"
                    + " Can't open a file.");
        }
        // NOTE: Used to use for the first argument the following, but
        // it seems to not work for relative file references:
        // new URL("file", null, _directory.getAbsolutePath()
        Configuration configuration = (Configuration)_tableau.toplevel();
        configuration.openModel(url, url, url.toExternalForm());
    }

    /** Save the model to the current file, determined by the
     *  <i>url</i> parameter of the associated effigy, or if
     *  that has not been set or is not a writable file, or if the
     *  effigy has been set non-modifiable, then invoke
     *  _saveAs(). This calls _writeFile() to perform the save.
     *  @return True if the save succeeds.
     */
    protected boolean _save() {
        File file = _writableFile();
        Effigy effigy = getEffigy();
        if ((effigy != null && !effigy.isModifiable()) || file == null) {
            return _saveAs();
        } else {
            try {
                _writeFile(file);
                setModified(false);
                return true;
            } catch (IOException ex) {
                report("Error writing file", ex);
                return false;
            }
        }
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to update the entry in the
     *  ModelDirectory and to rename the model to match the file name.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: This will probably fail with a security exception in
            // applets.
            String currentWorkingDirectory = System.getProperty("user.dir");
            if (currentWorkingDirectory != null) {
                fileDialog.setCurrentDirectory(
                        new File(currentWorkingDirectory));
            }
        }
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();

            try {
                URL newURL = file.toURL();
                String newKey = newURL.toExternalForm();
                Effigy previousOpen = getDirectory().getEffigy(newKey);
                if (previousOpen != null) {
                    // The destination file is already open.
                    if (previousOpen.isModified()) {
                        // Bring any visible tableaux to the foreground,
                        // then ask if it's OK to discard the changes?
                        previousOpen.showTableaux();
                        String confirm = "Unsaved changes in " + file.getName()
                            + ". OK to discard changes?";
                        // Show a MODAL dialog
                        int selected = JOptionPane.showOptionDialog(
                                this,
                                confirm,
                                "Discard changes?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                null,
                                null);
                        if (selected == 1) {
                            return false;
                        }
                        // Mark unmodified so that we don't get another
                        // query when it is close.
                        previousOpen.setModified(false);
                    }
                    previousOpen.closeTableaux();
                }

                if (file.exists()) {
                    // Ask for confirmation before overwriting a file.
                    String query = "Overwrite " + file.getName() + "?";
                    // Show a MODAL dialog
                    int selected = JOptionPane.showOptionDialog(
                            this,
                            query,
                            "Overwrite file?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            null);

                    if (selected == 1) {
                        return false;
                    }
                }

                _directory = fileDialog.getCurrentDirectory();
                _writeFile(file);
                // The original file will still be open, and has not
                // been saved, so we do not change its modified status.
                // setModified(false);

                // Open a new window on the model.
                getConfiguration().openModel(newURL, newURL, newKey);
                // If the tableau was unnamed before, then we need
                // to close this window after doing the save.
                Effigy effigy = getEffigy();
                if (effigy != null) {
                    String id = effigy.identifier.getExpression();
                    if (id.equals("Unnamed")) {
                        // This will have the effect of closing all the
                        // tableaux associated with the unnamed model.
                        effigy.setContainer(null);
                    }
                }
            } catch (Exception ex) {
                report("Error in save as.", ex);
                return false;
            }
        }
        return true;
    }

    /** Write the model to the specified file.  This method delegates
     *  to the top effigy containing the associated Tableau, if there
     *  is one, and otherwise throws an exception.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        Tableau tableau = getTableau();
        if (tableau != null) {
            Effigy effigy = (Effigy)tableau.getContainer();
            if (effigy != null) {
                effigy.writeFile(file);
                return;
            }
        }
        throw new IOException("Cannot find an effigy to delegate writing.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The view menu. Note that this is only created if there are multiple
     *  views, so if derived classes use it, they must test to see whether
     *  it is null.
     */
    protected JMenu _viewMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return a writable file for the URL given by the <i>url</i>
    // parameter of the associated effigy, if there is one, or return
    // null if there is not.  This will return null if the file does
    // not exist, or it exists and is not writable, or the <i>url</i>
    // parameter has not been set.
    private File _writableFile() {
        File result = null;
        Effigy effigy = getEffigy();
        if (effigy != null) {
            URL url = effigy.url.getURL();
            if (url != null) {
                String protocol = url.getProtocol();
                if (protocol.equals("file")) {
                    String filename = url.getFile();
                    File tentativeResult = new File(filename);
                    if (tentativeResult.canWrite()) {
                        result = tentativeResult;
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container of view factories, if one has been found.
    private TableauFactory _factoryContainer = null;

    // The tableau that created this frame.
    private Tableau _tableau = null;

    // The singleton icon image used for all ptolemy frames.
    private static Image _defaultIconImage = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for view menu commands. */
    class ViewMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (_factoryContainer != null) {
                JMenuItem target = (JMenuItem)e.getSource();
                String actionCommand = target.getActionCommand();
                TableauFactory factory = (TableauFactory)
                    _factoryContainer.getAttribute(actionCommand);
                if (factory != null) {
                    Effigy tableauContainer = (Effigy)_tableau.getContainer();
                    try {
                        Tableau tableau =
			    factory.createTableau(tableauContainer);
			tableau.show();
                    } catch (Exception ex) {
                        MessageHandler.error("Cannot create view", ex);
                    }
                }
            }
            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
