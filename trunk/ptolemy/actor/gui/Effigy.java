/* A named object that represents a ptolemy model.

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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.util.*;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Effigy
/**
An effigy represents model data, and is contained in the model directory
or another effigy.
An effigy contains all open instances of Tableau associated with the model.
It also contains a string attribute named "identifier" with a value that
uniquely identifies the model. A typical choice (which depend on
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
     *  the title of all contained Tableaux to the value of the parameter.
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
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. This calls the
     *  base class and then sets the <code>identifier</code>
     *  public members to the parameters of the new object.
     *  @param ws The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Effigy newobj = (Effigy)super.clone(ws);
        newobj.identifier = (StringAttribute)newobj.getAttribute("identifier");
        newobj.url = (URLAttribute)newobj.getAttribute("url");
        return newobj;
    }

    /** Get a tableau factory that offers multiple views of this effigy, or
     *  null if none has been specified.
     *  This can be used by a contained tableau to set up a View menu.
     *  @returns A tableau factory offering multiple views.
     */
    public TableauFactory getTableauFactory() {
        return _factory;
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

    /** Make all tableaux associated with this model visible by raising
     *  or deiconifying them.  If there is no tableau associated with
     *  this effigy, then create one by calling createPrimaryTableau()
     *  in the configuration.
     */
    public void showTableaux() {
        Iterator tableaux = entityList(Tableau.class).iterator();
        boolean foundOne = false;
        while(tableaux.hasNext()) {
            Tableau tableau = (Tableau)tableaux.next();
            tableau.show();
            foundOne = true;
        }
        if (!foundOne) {
            // Create a new tableau.
            Configuration configuration = (Configuration)toplevel();
            try {
                configuration.createPrimaryTableau(this);
            } catch (Exception ex) {
                MessageHandler.error("Unable to create a tableau.", ex);
            }
        }
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

    // A tableau factory affering multiple views.
    private TableauFactory _factory = null;

    // Indicator that the data represented in the window has been modified.
    private boolean _modified = false;
}

