/* A base class for attributes to be attached to instances of NamedObj.

 Copyright (c) 1998 The Regents of the University of California.
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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;


//////////////////////////////////////////////////////////////////////////
//// Attribute
/**
Attribute is a base class for attributes to be attached to instances
of NamedObj.  This base class is itself a NamedObj, with the only
extension being that it can have a container.  The setContainer()
method puts this object on the list of attributes of the container.

@author Edward A. Lee, Neil Smyth
@version $Id$
*/
public class Attribute extends NamedObj {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public Attribute() {
	super();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Attribute(Workspace workspace) {
	super(workspace, "");
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.

     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Attribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setContainer(container);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     *  @return The new Attribute.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Attribute newobj = (Attribute)super.clone(ws);
        newobj._container = null;
        return newobj;
    }

    /** Get the NamedObj that this Attribute is attached to.
     *  @return The container, an instance of NamedObj.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Specify the container NamedObj, adding this attribute to the
     *  list of attributes in the container.  If the container already
     *  contains an attribute with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this object being garbage collected.
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && workspace() != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        try {
            workspace().getWriteAccess();
            if (deepContains(container)) {
                throw new IllegalActionException(this, container,
                        "Attempt to construct recursive containment " +
                        "of attributes.");
            }

            NamedObj prevcontainer = (NamedObj)getContainer();
            if (prevcontainer == container) return;
            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addAttribute(this);
                if (prevcontainer == null) {
                    workspace().remove(this);
                }
            }
            _container = container;
            if (prevcontainer != null) {
                prevcontainer._removeAttribute(this);
            }
        } finally {
            workspace().doneWriting();
        }
    }

    /** Update the attribute value, resolving any dependencies on other
     *  attributes.  In this base class, no such dependencies can occur,
     *  but in derived classes, they do occur.  This method is called by
     *  the container of the attribute after its entire attribute list has
     *  been cloned using the clone() method.  Thus, all other attributes
     *  of the container have been created, so interdependencies can be
     *  resolved.  In this base class, the method does nothing.
     */
    public void update() {
    }

    ///////////////////////////////////////////////////////////////////////
    ////                      private variables                        ////

    private NamedObj _container;
}
