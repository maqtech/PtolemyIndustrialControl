/* A base class for attributes to be attached to instances of NamedObj.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kvm.kernel.util;

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

    /** Construct an attribute with an empty string
     *  as its name.
     */
    public Attribute() {
	super();
        setMoMLElementName("property");
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.

     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Attribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(name);
        setContainer(container);
        setMoMLElementName("property");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException Thrown only in derived classes.
     *  @return The new Attribute.
     */
    
    /* KVM_FIXME: Clone is not supported in kvm
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Attribute newobj = (Attribute)super.clone(ws);
        newobj._container = null;
        return newobj;
    }
    */

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
            //if (prevcontainer == null) {
            //    _workspace.remove(this);
            //}
        }
        _container = container;
        if (prevcontainer != null) {
            prevcontainer._removeAttribute(this);
        }
    }

    /** Set the name of the attribute. If there is already an attribute
     *  of the container with the same name, then throw a
     *  NameDuplicationException.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If there is already an
     *       attribute with the same name in the container.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        if (name == null) {
            name = new String("");
        }
        NamedObj container = (NamedObj) getContainer();
        if((container != null)) {
            Attribute another = container.getAttribute(name);
            if((another != null) && (another != this)) {
                throw new NameDuplicationException (container,
                        "already contains an attribute with the name " +
                        name + ".");
            }
        }
        super.setName(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial Container of this attribute. */
    private NamedObj _container;
}
