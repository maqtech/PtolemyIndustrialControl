/* Top-level window for Ptolemy models with a menubar and status bar.

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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PtolemyFrame
/**
This is a top-level window for Ptolemy models with a menubar and status bar.
Derived classes should add components to the content pane using a
line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>
This extends the base class by associating with it a Ptolemy II model,
and specifying a model error handler for that model that handles model
errors by throwing an exception.
<p>
If the model contains an instance of FileAttribute named "_help", then
the file or URL specified by that attribute will be opened when "Help"
in the Help menu is invoked.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public abstract class PtolemyFrame extends TableauFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model) {
        this(model, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame, or null if none.
     */
    public PtolemyFrame(CompositeEntity model, Tableau tableau) {
        super(tableau);

        // Create a file filter that accepts .xml and .moml files.
        LinkedList extensions = new LinkedList();
        extensions.add("xml");
        extensions.add("moml");
        _fileFilter = new ExtensionFileFilter(extensions);

        setModel(model);

        // Set the window properties if there is an attribute in the
        // model specifying them.  Errors are ignored.
        try {
            WindowPropertiesAttribute properties
                    = (WindowPropertiesAttribute)model.getAttribute(
                    "_windowProperties", WindowPropertiesAttribute.class);
            if (properties != null) {
                properties.setProperties(this);
            }
        } catch (IllegalActionException ex) {
            // Ignore.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the associated model.
     *  @return The associated model.
     */
    public CompositeEntity getModel() {
        return _model;
    }

    /** Set the associated model.  This also sets an error handler for
     *  the model that results in model errors throwing an exception.
     *  @param model The associated model.
     */
    public void setModel(CompositeEntity model) {
        _model = model;
        _model.setModelErrorHandler(new BasicModelErrorHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            setModel(new CompositeEntity());
            return true;
        } else {
            return false;
        }
    }

    /** Display more detailed information than given by _about().
     *  If the model contains an instance of FileAttribute named "_help",
     *  that the file or URL given by that attribute is opened.  Otherwise,
     *  a built-in generic help file is opened.
     */
    protected void _help() {
        try {
            FileAttribute helpAttribute = (FileAttribute)getModel()
                    .getAttribute("_help", FileAttribute.class);
            URL doc = helpAttribute.asURL();
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            super._help();
        }
    }

    /** Print the contents.  If this frame implements either the
     *  Printable or Pageable then those interfaces are used to print
     *  it.  This overrides the base class to queue a change request to do
     *  the printing, because otherwise, printing will cause a deadlock.
     */
    protected void _print() {
        if (_model != null) {
            ChangeRequest request = new ChangeRequest(this, "Print") {
                protected void _execute() throws Exception {
                    PtolemyFrame.super._print();
                }
            };
            _model.requestChange(request);
        } else {
            super._print();
        }
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  If setModel() has been called, then the initial filename
     *  is set to the name of the model.  If setModel() has not yet
     *  been called, then the initial filename to
     *  <code>model.xml</code>.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
	if (_model == null || _model.getName().length() == 0) {
	    _initialSaveAsFileName = "model.xml";
	} else {
	    // We are not sanitizing the name here . . .
	    _initialSaveAsFileName = _model.getName() + ".xml";
	}
	return super._saveAs();
    }

    /** Write the model to the specified file.  This method delegates
     *  to the top effigy containing the associated Tableau, if there
     *  is one, and otherwise throws an exception. This ensures that the
     *  data written is the description of the entire model, not just
     *  the portion within some composite actor.   It also adjusts the
     *  URIAttribute in the model to match the specified file, if
     *  necessary, and creates one otherwise.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        Tableau tableau = getTableau();
        if (tableau != null) {
            Effigy effigy = (Effigy)tableau.getContainer();
            if (effigy != null) {
                // Ensure that if we do ever try to call this method,
                // that it is the top effigy that is written.
                Effigy topEffigy = effigy.topEffigy();
                topEffigy.writeFile(file);
                if (topEffigy instanceof PtolemyEffigy) {
                    NamedObj model = ((PtolemyEffigy)topEffigy).getModel();
                    // NOTE: Fairly brute force here... There might
                    // already be a URIAttribute, but we simply overwrite it.
                    // Perhaps should check to see whether the one that is
                    // there matches.  EAL
                    try {
                        URIAttribute uri = new URIAttribute(model, "_uri");
                        uri.setURI(file.toURI());
                    } catch (KernelException ex) {
                        throw new InternalErrorException(
                        "Failed to create URIAttribute for new location!");
                    }
                }                            
                return;
            }
        }
        throw new IOException("Cannot find an effigy to delegate writing.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The model that this window controls, if any.
    private CompositeEntity _model;
}
