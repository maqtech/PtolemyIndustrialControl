/* Base class for Ptolemy applications.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// Application
/**
This is a base class for an application that uses Ptolemy II classes.
It must contain, at a minimum, an instance of ModelDirectory, called
"directory", an instance of ModelReader, called "reader", and
an instance of ViewFactory, called "factory".  This class uses
those instances to manage a collection of models, open new models,
and create views of those models.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class Application extends CompositeEntity {

    /** Construct an instance in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the instance to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public Application(Workspace workspace) {
	super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a model with the specified name is present in the directory,
     *  then find all the views of that model and make them 
     *  visible; otherwise, read a model from the specified URL
     *  and create a default view for the model and add the view 
     *  to this directory.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param identifier The identifier that uniquely identifies the model.
     *  @exception Exception If the URL cannot be read.
     */
    public void openModel(URL base, URL in, String identifier)
            throws Exception {
        ModelDirectory directory = (ModelDirectory)getEntity("directory");
        if (directory == null) {
            throw new InternalErrorException("No model directory!");
        }
        ModelProxy model = directory.getModel(identifier);
        if (model == null) {
            ModelReader reader = (ModelReader)getEntity("reader");
            if (reader == null) {
                throw new InternalErrorException("No model reader!");
            }
            model = reader.read(base, in, identifier);
	    Parameter parameter = new Parameter(model, "identifier");
	    parameter.setToken(new StringToken(identifier));
            model.setName(directory.uniqueName("model"));
	    model.setContainer(directory);

            // Create a view.
            ViewFactory factory = (ViewFactory)getEntity("factory");
            View view = factory.createView(model);
	    view.setName(model.uniqueName("view"));
            view.setContainer(model);
	    // The first view is a master.
	    view.setMaster(true);
        } else {
            // Model already exists.
            model.showViews();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity; if that entity is the model directory,
     *  then exit the application.  This method should not be called
     *  directly.  Call the setContainer() method of the entity instead with
     *  a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
	if (entity.getName().equals("directory")) {
            System.exit(0);
        }
    }
}
