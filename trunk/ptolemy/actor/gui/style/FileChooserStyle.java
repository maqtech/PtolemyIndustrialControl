/* A style for parameters that can be specified using a FileChooser.

 Copyright (c) 2001-2004 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.io.File;
import java.net.URI;

//////////////////////////////////////////////////////////////////////////
//// FileChooserStyle
/**
This attribute annotates user settable attributes to specify
that the value of the parameter can be optionally given using a
FileChooser.

@see ptolemy.actor.gui.EditorPaneFactory
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/

public class FileChooserStyle extends ParameterEditorStyle {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public FileChooserStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public FileChooserStyle(Workspace workspace) {
        // This constructor is needed for Shallow codegen to work.
        super(workspace);
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Settable.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public FileChooserStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param param The attribute that this annotates.
     *  @return True if the argument is a StringAttribute, false otherwise.
     */
    public boolean acceptable(Settable param) {
        if (!(param instanceof StringAttribute)) return false;
        else return true;
    }

    /** Create a new entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *  @param query The query into which to add the entry.
     */
    public void addEntry(PtolemyQuery query) {
        Settable container = (Settable)getContainer();
        String name = container.getName();
        String defaultValue = container.getExpression();
        defaultValue = container.getExpression();
        URI modelURI = URIAttribute.getModelURI(this);
        File directory = null;
        if (modelURI != null) {
            if (modelURI.getScheme().equals("file")) {
                File modelFile = new File(modelURI);
                directory = modelFile.getParentFile();
            }
        }
        query.addFileChooser(
                name, 
                name, 
                defaultValue, 
                modelURI, 
                directory, 
                PtolemyQuery.preferredBackgroundColor(container));
        query.attachParameter(container, name);
    }
}
