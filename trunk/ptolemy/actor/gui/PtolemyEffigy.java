/* A representative of a ptolemy model

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
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.*;
import ptolemy.gui.MessageHandler;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.URLAttribute;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// PtolemyEffigy
/**
An effigy for a Ptolemy II model.
An effigy represents model metadata, and is contained by the
model directory or by another effigy.  This class adds to the base
class an association with a Ptolemy II model. The model, strictly
speaking, is any Ptolemy II object (an instance of NamedObj).
The Effigy class extends CompositeEntity, so an instance of Effigy
can contain entities.  By convention, an effigy contains all
open instances of Tableau associated with the model.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class PtolemyEffigy extends Effigy implements ChangeListener {

    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public PtolemyEffigy(Workspace workspace) {
	super(workspace);
    }

    /** Create a new effigy in the given container with the given name.
     *  @param container The container that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PtolemyEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that a change has been successfully executed.
     *  This method does nothing.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {}

    /** React to the fact that a change has triggered an error by
     *  reporting the error in a top-level dialog.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Do not report if it has already been reported.
        // NOTE: This method assumes that the context of the error handler
        // has been set so that there is an owner for the error window.
        if (change == null) {
            MessageHandler.error("Change failed: ", exception);
        } else if (!change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Change failed: " + change.getDescription(),
                    exception);
        }
    }

    /** Clone the object into the specified workspace. This calls the
     *  base class and then clones the associated model into a new
     *  workspace, if there is one.
     *  @param workspace The workspace for the new effigy.
     *  @return A new effigy.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PtolemyEffigy newObject = (PtolemyEffigy)super.clone(workspace);
        if (_model != null) {
            newObject._model = (NamedObj)_model.clone(new Workspace());
        }
        return newObject;
    }

    /** Return the ptolemy model that this is an effigy of.
     *  @return The model, or null if none has been set.
     */
    public NamedObj getModel() {
	return _model;
    }

    /** Set the ptolemy model that this is an effigy of.
     *  Register with that model as a change listener.
     *  @param model The model.
     */
    public void setModel(NamedObj model) {
        if (_model != null) {
            _model.removeChangeListener(this);
        }
        _model = model;
        if (model != null) {
            _model.addChangeListener(this);
        }
    }

    /** Write the model associated with the top effigy (returned by
     *  topEffigy()) to the specified file in MoML format.
     *  Change the name to match the file name, up to its first period.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    public void writeFile(File file) throws IOException {
        java.io.FileWriter fileWriter = new java.io.FileWriter(file);
        String name = getModel().getName();

        String filename = file.getName();
        int period = filename.indexOf(".");
        if (period > 0) {
            name = filename.substring(0, period);
        } else {
            name = filename;
        }

        // NOTE: The following cast is safe because of the check
        // in _checkContainer().
        ((PtolemyEffigy)topEffigy()).getModel().exportMoML(fileWriter,
                0, name);
        fileWriter.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this entity, i.e., ModelDirectory or PtolemyEffigy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.
     */
    protected void _checkContainer(CompositeEntity container)
            throws IllegalActionException {
	if (container != null
                && !(container instanceof ModelDirectory)
                && !(container instanceof PtolemyEffigy)) {
	    throw new IllegalActionException(this, container,
		    "The container can only be set to an " +
                    "instance of ModelDirectory or PtolemyEffigy.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The model associated with this effigy.
    private NamedObj _model;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new Ptolemy effigies.
     */
    public static class Factory extends EffigyFactory {

	/** Create a factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return true, indicating that this effigy factory is
         *  capable of creating an effigy without a URL being specified.
         *  @return True.
         */
        public boolean canCreateBlankEffigy() {
            return true;
        }

        /** Create a new effigy in the given container by reading the specified
         *  (input) URL. If the specified URL (<i>input</i>
         *  is null, then create a blank effigy.
         *  The blank effigy will have a new model associated with it.
         *  If this effigy factory contains an entity named "blank", then
         *  the new model will be a clone of that entity.  Otherwise,
         *  it will be an instance of TypedCompositeActor.
         *  If the URL does not end with extension ".xml" or ".moml", then
         *  return null.  If the URL points to an XML file that is not
         *  a MoML file, then also return null.
         *  The specified base is used to expand any relative file references
         *  within the URL.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.
         *  @param input The input URL.
         *  @return A new instance of PtolemyEffigy, or null if the URL
         *   does not specify a Ptolemy II model.
         *  @exception Exception If the URL cannot be read, or if the data
         *   is malformed in some way.
         */
        public Effigy createEffigy(
                CompositeEntity container, URL base, URL input)
                throws Exception {
	    if (input == null) {
                // Create a blank effigy.
                PtolemyEffigy effigy = new PtolemyEffigy(
                        container, container.uniqueName("effigy"));

                // If this factory contains an entity called "blank", then
                // clone that.
                NamedObj entity = getEntity("blank");
                NamedObj newModel;
                if (entity != null) {
                    newModel = (NamedObj)entity.clone(new Workspace());
                } else {
                    newModel = new TypedCompositeActor(new Workspace());
                }
                // The name might be "blank" which is confusing.
                // Set it to an empty string.  On Save As, this will
                // be changed to match the file name.
                newModel.setName("");
                effigy.setModel(newModel);
                return effigy;
            } else {
                String extension = getExtension(input);
                if (!extension.equals("xml") && !extension.equals("moml")) {
                    return null;
                }
                // Create a blank effigy.
                PtolemyEffigy effigy = new PtolemyEffigy(
                        container, container.uniqueName("effigy"));

                MoMLParser parser = new MoMLParser();
                NamedObj toplevel = null;
		try {
		    try {
			// If the following fails, we should remove the effigy.
			try {
			    toplevel = parser.parse(base, input.openStream());
			} catch (IOException io) {
			    // If we are running under Web Start, we
			    // might have a URL that refers to another
			    // jar file.
			    URL anotherURL =
				JNLPUtilities
				.jarURLEntryResource(input.toString());
			    if (anotherURL != null) {
				toplevel = parser.parse(base,
                                        anotherURL.openStream());
			    } else {
				throw io;
			    }
			}
			if (toplevel != null) {
			    effigy.setModel(toplevel);

			    // A MoMLFilter may have modified the model
			    // as it was being parsed.
			    effigy.setModified(MoMLParser.isModified());
                            // The effigy will handle saving the modified
                            // moml for us, so MoMLParser need
                            // not care anymore.
                            MoMLParser.setModified(false);

			    // Identify the URL from which the model was read
			    // by inserting an attribute into both the model
			    // and the effigy.
			    URLAttribute url =
                                new URLAttribute(toplevel, "_url");
			    url.setURL(input);

			    // This is used by TableauFrame in its
			    //_save() method.
			    effigy.url.setURL(input);

			    return effigy;
			} else {
			    effigy.setContainer(null);
			}
		    } catch (Exception e) {
                        // The finally clause below can result in the
                        // application exiting if there are no other
                        // effigies open.  We check for that condition,
                        // and report the error here.  Otherwise, we
                        // pass the error to the caller.
                        ModelDirectory dir = (ModelDirectory)
                            effigy.topEffigy().getContainer();
                        List effigies = dir.entityList(Effigy.class);
                        // Note that one of the effigies is the configuration
                        // itself, which does not prevent exiting the app.
                        // Hence, we handle the error if there are 2 or fewer.
                        if (effigies.size() <= 2) {
                            MessageHandler.error("Failed to read " + input, e);
                        } else {
                            // Let the caller handle the error.
                            throw e;
                        }
		    }
		} finally {
		    // If we failed to populate the effigy with a model,
		    // then we remove the effigy from its container.
                    if (toplevel == null) {
			effigy.setContainer(null);
		    }
		}
                return null;
            }
	}
    }
}
