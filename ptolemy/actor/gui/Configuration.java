/* Base class for Ptolemy configurations.

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

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;

//////////////////////////////////////////////////////////////////////////
//// Configuration
/**
This is a base class for a composite entity that defines the
configuration of an application that uses Ptolemy II classes.
It must contain, at a minimum, an instance of ModelDirectory, called
"directory", and an instance of EffigyFactory, called "effigyFactory".
It may also contain an instance of TableauFactory, called "tableuFactory".
A tableau is a visual representation of the model in a top-level window.
This class uses those instances to manage a collection of models,
open new models, and create tableaux of those models.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see EffigyFactory
@see ModelDirectory
@see Tableau
@see TableauFactory
*/
public class Configuration extends CompositeEntity {

    /** Construct an instance in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the instance to the workspace directory.
     *  Increment the version number of the workspace.
     *  Note that there is no constructor that takes a container
     *  as an argument, thus ensuring that a Configuration is always
     *  a top-level entity.
     *  @param workspace The workspace that will list the entity.
     */
    public Configuration(Workspace workspace) {
	super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the first tableau for the given effigy, using the 
     *  tableau factory.  This is called after an effigy is first opened,
     *  or when a new effigy is created.
     *  @exception Exception if an error occurs while creating the tableau.
     */
    public void createPrimaryTableau(Effigy effigy) throws Exception {
        // Create a tableau if there is a tableau factory.
        TableauFactory factory = (TableauFactory)getEntity("tableauFactory");
        if (factory != null) {
            Tableau tableau = factory.createTableau(effigy);
            if (tableau == null) {
                throw new Exception("Unable to create a Tableau.");
            }
            // The first tableau is a master.
            tableau.setMaster(true);
            tableau.show();
        }
    }

    /** Create a new model.  This defers to the effigy factory contained
     *  by this configuration.
     */
    public void newModel() {
	final ModelDirectory directory = (ModelDirectory)getEntity("directory");
	if(directory == null) return;

        EffigyFactory mainFactory = (EffigyFactory)getEntity("effigyFactory");
        if (mainFactory == null) return;

	List factoryList = mainFactory.entityList(EffigyFactory.class);
	Box panel = new Box(BoxLayout.Y_AXIS);
	final JFrame frame = new JFrame();
	frame.getContentPane().add(panel);
	Iterator factories = factoryList.iterator();
	while(factories.hasNext()) {
	    final EffigyFactory factory = (EffigyFactory)factories.next();
            if (!factory.canCreateBlankEffigy()) continue;
	    String buttonName = factory.getName();
	    JButton button = new JButton(buttonName);
	    panel.add(button);
	    button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    frame.hide();
		    Effigy effigy = null;
		    try {
			effigy = factory.createEffigy(directory);
		    } catch (Exception ex) {
			MessageHandler.error("Could not create new effigy", ex);
		    } 
		    try {
			createPrimaryTableau(effigy);
		    } catch (Exception ex) {
			MessageHandler.error("Could not create tableau " +
					     "for new effigy", ex);
		    } 
		}
	    });
	}
        panel.add(panel.createVerticalStrut(15));
	panel.add(panel.createHorizontalGlue());
	JButton button = new JButton("Cancel");	
	panel.add(button);
	button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		frame.hide();
	    }
	});
	frame.show();
	frame.pack();
    }

    /** If a model with the specified name is present in the directory,
     *  then find all the tableaux of that model and make them 
     *  visible; otherwise, read a model from the specified URL
     *  and create a default tableau for the model and add the tableau 
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
        // Check to see whether the model is already open.
        Effigy effigy = directory.getEffigy(identifier);
        if (effigy == null) {
            // No previous effigy exists that is identified by this URL.
            // Find an effigy factory to read it.
            EffigyFactory factory = (EffigyFactory)getEntity("effigyFactory");
            if (factory == null) {
                throw new InternalErrorException(
                        "No effigy factories in the configuration!");
            }
            effigy = factory.createEffigy(directory, base, in);
            if (effigy == null) {
                MessageHandler.error("Unsupported file type: "
                        + in.toExternalForm());
                return;
            }
            effigy.identifier.setExpression(identifier);
            // If this fails, we do not want the effigy in the directory.
            try {
                createPrimaryTableau(effigy);
            } catch (Exception ex) {
                effigy.setContainer(null);
                // Hmm... the following doesn't seem to properly give the
                // stack trace.
                throw (Exception)(ex.fillInStackTrace());
            }
        } else {
            // Model already exists.
            effigy.showTableaux();
        }
    }

    /** If the argument is not null, then throw an exception.
     *  This ensures that the object is always at the top level of
     *  a hierarchy.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the argument is not null.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException {
        if (container != null) {
            throw new IllegalActionException(this,
            "Configuration can only be at the top level of a hierarchy.");
        }
    }

    /** Find all instance of Tableau deeply contained in the directory
     *  and call show() on them.  If there is no directory, then do nothing.
     */
    public void showAll() {
	final ModelDirectory directory = 
	    (ModelDirectory)getEntity("directory");
	if(directory == null) return;
        _showTableaux(directory);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Call show() on all instances of Tableaux contained by the specified
    // container.
    private void _showTableaux(CompositeEntity container) {
        Iterator entities = container.entityList().iterator();
        while (entities.hasNext()) {
            Object entity = entities.next();
            if (entity instanceof Tableau) {
                ((Tableau)entity).show();
            } else if (entity instanceof CompositeEntity) {
                _showTableaux((CompositeEntity)entity);
            }
        }
    }
}
