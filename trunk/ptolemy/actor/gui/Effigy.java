/* A named object that represents a ptolemy model.

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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.util.*;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.URLAttribute;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;

//////////////////////////////////////////////////////////////////////////
//// Effigy
/**
An effigy represents model data, and is contained in the model directory
or another effigy.
An effigy contains all open instances of Tableau associated with the model.
It also contains a string attribute named "identifier" with a value that
uniquely identifies the model. A typical choice (which depends on
the configuration) is the canonical URL for a MoML file that
describes the model.  In the case of an effigy contained by another,
a typical choice is the URL of the parent effigy, a pound sign "#",
and a name.
<p>
An effigy may contain other effigies.  The top effigy
in such a containment hierarchy is associated with a URL or file.
Contained effigies are associated with the same file, and represent
structured data within the top-level representation in the file.
The topEffigy() method returns that top effigy.
<p>
NOTE: It might seem more natural for the identifier
to be the name of the effigy rather than the value of a string attribute.
But the name cannot have periods in it, and a URL typically does
have periods in it, and periods are not allowed in the names of
Ptolemy II objects.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see ModelDirectory
@see Tableau
*/
public class Effigy extends CompositeEntity {

    /** Create a new proxy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this proxy.
     */
    public Effigy(Workspace workspace) {
	super(workspace);
        try {
            identifier = new StringAttribute(this, "identifier");
            identifier.setExpression("Unnamed");
            url = new URLAttribute(this, "url");
        } catch (Exception ex) {
            throw new InternalErrorException("Can't create identifier!");
        }
    }

    /** Construct an effigy with the given name and container.
     *  @param container The container.
     *  @param name The name of the effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Effigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        identifier = new StringAttribute(this, "identifier");
        identifier.setExpression("Unnamed");
        url = new URLAttribute(this, "url");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The identifier for the effigy.  The default value is "Unnamed". */
    public StringAttribute identifier;

    /** The URL for the effigy.  The default value is null. */
    public URLAttribute url;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>identifier</i> parameter, then set
     *  the title of all contained Tableaux to the value of the parameter;
     *  if the argument is the <i>url</i> parameter, then check to see
     *  whether it is writable, and call setModifiable() appropriately.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == identifier) {
            Iterator tableaux = entityList(Tableau.class).iterator();
            while (tableaux.hasNext()) {
                Tableau tableau = (Tableau)tableaux.next();
                tableau.setTitle(identifier.getExpression());
            }
        } else if (attribute == url) {
            URL u = url.getURL();
            if (u == null) {
                // A new model, with no URL, is by default modifiable.
                _modifiableURL = true;
            } else {
                String protocol = u.getProtocol();
                if (!(protocol.equals("file"))) {
                    _modifiableURL = false;
                } else {
                    String filename = u.getFile();
                    File file = new File(filename);
                    _modifiableURL = file.canWrite();
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Close all tableaux contained by this effigy, and by any effigies
     *  it contains.
     *  @return False if the user cancels on a save query.
     */
    public boolean closeTableaux() {
        Iterator effigies = entityList(Effigy.class).iterator();
        while(effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            if (!effigy.closeTableaux()) return false;
        }
        Iterator tableaux = entityList(Tableau.class).iterator();
        while(tableaux.hasNext()) {
            Tableau tableau = (Tableau)tableaux.next();
            if (!tableau.close()) return false;
        }
        return true;
    }

    /** Get a tableau factory that offers multiple views of this effigy, or
     *  null if none has been specified.
     *  This can be used by a contained tableau to set up a View menu.
     *  @returns A tableau factory offering multiple views.
     */
    public TableauFactory getTableauFactory() {
        return _factory;
    }

    /** Return whether the URL associated with this effigy can be written
     *  to.  This will be false if either there is no URL associated
     *  with this effigy, or the URL is not a file, or the file is not
     *  writable or does not exist, or setModifiable() has been called
     *  with a false argument.
     *  @return False to indicated that the URL is not writable.
     */
    public boolean isModifiable() {
        if (!_modifiable) return false;
        else return _modifiableURL;
    }

    /** Return the value set by setModified(), or false if setModified()
     *  has not been called on this effigy or any effigy contained by
     *  the same top effigy (returned by topEffigy()).
     *  This method is intended to be used to
     *  keep track of whether the data in the file or URL associated
     *  with this data has been modified.  The method is called by
     *  an instance of TableauFrame to determine whether it is safe
     *  to close.
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        return topEffigy()._modified;
    }

    /** Override the base class so that tableaux contained by this object
     *  are removed before this effigy is removed from the ModelDirectory.
     *  This causes the frames associated with those tableaux to be
     *  closed.
     *  @param container The directory in which to list this effigy.
     *  @exception IllegalActionException If the proposed container is not
     *   an instance of ModelDirectory, or if the superclass throws it.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the specified name.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
	if (container == null) {
	    // Remove all tableaux.
	    Iterator tableaux = entityList(Tableau.class).iterator();
	    while (tableaux.hasNext()) {
		ComponentEntity tableau = (ComponentEntity)tableaux.next();
		tableau.setContainer(null);
	    }
	    // Remove all contained effigies as well.
	    Iterator effigies = entityList(Effigy.class).iterator();
	    while (effigies.hasNext()) {
		ComponentEntity effigy = (ComponentEntity)effigies.next();
		effigy.setContainer(null);
	    }
        }
        super.setContainer(container);
    }

    /** Specify that the URL associated with this effigy must not be written
     *  to, irrespective of whether it is writable.
     *  Notice that this does not automatically result in any tableaux
     *  that are contained switching to being uneditable.  But it will
     *  prevent them from writing to the URL.
     *  @param flag False to prevent writing to the URL.
     */
    public void setModifiable(boolean flag) {
        _modifiable = flag;
    }

    /** Record whether the data associated with this effigy has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent calls to isModified()
     *  will return true.  This is used by instances of TableauFrame.
     *  This is recorded in the entity returned by topEntity(), which
     *  is the one associated with a file.
     *  @param modified Indicator of whether the data has been modified.
     */
    public void setModified(boolean modified) {
        topEffigy()._modified = modified;
    }

    /** Specify a tableau factory that offers multiple views of this effigy.
     *  This can be used by a contained tableau to set up a View menu.
     *  @param factory A tableau factory offering multiple views.
     */
    public void setTableauFactory(TableauFactory factory) {
        _factory = factory;
    }

    /** Make all tableaux associated with this effigy and any effigies it
     *  contains visible by raising or deiconifying them.
     *  If there is no tableau contained directly by
     *  this effigy, then create one by calling createPrimaryTableau()
     *  in the configuration.
     *  @return The first tableau encountered, or a new one if there are none.
     */
    public Tableau showTableaux() {
        Iterator effigies = entityList(Effigy.class).iterator();
        while(effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            effigy.showTableaux();
        }
        Iterator tableaux = entityList(Tableau.class).iterator();
        Tableau result = null;
        while(tableaux.hasNext()) {
            Tableau tableau = (Tableau)tableaux.next();
            tableau.show();
            if (result == null) result = tableau;
        }
        if (result == null) {
            // Create a new tableau.
            Configuration configuration = (Configuration)toplevel();
            result = configuration.createPrimaryTableau(this);
        }
        return result;
    }

    /** If this effigy is contained by another effigy, then return
     *  the result of calling this method on that other effigy;
     *  otherwise, return this effigy.
     *  @return The top-level effigy that (deeply) contains this one.
     */
    public Effigy topEffigy() {
        Nameable container = getContainer();
        if (container instanceof Effigy) {
            return ((Effigy)container).topEffigy();
        } else {
            return this;
        }
    }

    /** Write the model associated with this effigy
     *  to the specified file.  This base class throws
     *  an exception, since it does not know how to write model data.
     *  Derived classes should override this method to write model
     *  data.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    public void writeFile(File file) throws IOException {
        throw new IOException("I do not know how to write this model data.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container is of a suitable class for
     *  this entity, i.e., ModelDirectory or Effigy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.
     */
    protected void _checkContainer(CompositeEntity container)
            throws IllegalActionException {
	if(container != null
                && !(container instanceof ModelDirectory)
                && !(container instanceof Effigy)) {
	    throw new IllegalActionException(this, container,
		    "The container can only be set to an " +
                    "instance of ModelDirectory or Effigy.");
	}
    }

    /** Remove the specified entity, and if there are no more tableaux
     *  contained, then remove this object from its container.
     *  @param entity The tableau to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
       	if(entityList(Tableau.class).size() == 0) {
	    try {
		setContainer(null);
	    } catch (Exception ex) {
                ex.printStackTrace();
		throw new InternalErrorException("Cannot remove effigy!");
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // A tableau factory offering multiple views.
    private TableauFactory _factory = null;

    // Indicator that the URL must not be written to (if false).
    private boolean _modifiable = true;

    // Indicator that the URL can be written to.
    private boolean _modifiableURL = true;

    // Indicator that the data represented in the window has been modified.
    private boolean _modified = false;
}

