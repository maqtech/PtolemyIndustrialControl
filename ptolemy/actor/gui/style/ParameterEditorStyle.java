/* An attribute for specifying how a parameter is edited.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

// Ptolemy imports.
import ptolemy.gui.Query;
import ptolemy.kernel.util.*;
import ptolemy.actor.gui.PtolemyQuery;

// Java imports.

//////////////////////////////////////////////////////////////////////////
//// ParameterEditorStyle
/**
This attribute annotates user settable attributes to suggest an interactive
mechanism for editing.  The EditorPaneFactory class observes the
presence of this attribute to guide construction of an interactive
parameter editor.

@see EditorPaneFactory
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public abstract class ParameterEditorStyle extends Attribute {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public ParameterEditorStyle() {
	super();
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of UserSettable.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ParameterEditorStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param The attribute that this annotates.
     */
    public abstract boolean accept(UserSettable param);

    /** Create a new entry in the given query with the given name
     *  with this style and attach the attribute that
     *  contains this style to the created entry so that we are notified
     *  of changes in value.
     *  
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing attribute
     *   has a value that cannot be edited using this style.
     */
    public abstract void addEntry(PtolemyQuery query)
	throws IllegalActionException;

    /** Override the base class to first check that the container is
     *  an instance of UserSettable.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment, or
     *   the proposed container is not an instance of UserSettable.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && !(container instanceof UserSettable)) {
            throw new IllegalActionException(this, container,
                "ParameterEditorStyle can only be contained by UserSettable.");
        }
        super.setContainer(container);
    }
}
