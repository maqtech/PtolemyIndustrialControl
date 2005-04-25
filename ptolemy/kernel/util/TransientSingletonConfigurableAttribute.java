/* A nonpersistent configurable singleton attribute.

Copyright (c) 1998-2005 The Regents of the University of California.
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

*/
package ptolemy.kernel.util;


//////////////////////////////////////////////////////////////////////////
//// TransientSingletonConfigurableAttribute

/**
   This class is a nonpersistent configurable singleton attribute.
   By "nonpersistent" we mean that it does not export MoML.
   A major application of this class is to define a default icon
   description.  An icon description is XML code in the SVG
   (scalable vector graphics) schema, set in a configure element
   in MoML.

   @deprecated Use SingletonConfigurableAttribute instead with setPersistent(false).
   @author Steve Neuendorffer and Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Green (bilung)
*/
public class TransientSingletonConfigurableAttribute
    extends ConfigurableAttribute implements Singleton {
    // NOTE: This class does not extend SingletonConfigurableAttribute
    // even though the setContainer() method is identical.  The reason
    // is subtle.  The base classes in the Ptolemy kernel all create
    // instances of this class with the name "_iconDescription" to
    // give a default icon.  However, when one wishes to replace
    // this with another icon, the natural way to do it is to create
    // an instance of SingletonConfigurableAttribute in the MoML
    // data that goes in the library.  However, when the MoML parser
    // reads this MoML, it finds a pre-existing attribute that is
    // an instance of TransientSingletonConfigurableAttribute.
    // If that is also an instance of SingletonConfigurableAttribute,
    // then the MoML parser will not replace the attribute.
    // Thus, it is important that a TransientSingletonConfigurableAttribute
    // not be an instance of SingletonConfigurableAttribute.

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public TransientSingletonConfigurableAttribute() {
        super();
        setPersistent(false);
    }

    /** Construct a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public TransientSingletonConfigurableAttribute(Workspace workspace) {
        super(workspace);
        setPersistent(false);
    }

    /** Construct an attribute with the given container and name.
     *  If an attribute already exists with the same name as the one
     *  specified here, and of class SingletonConfigurableAttribute, then that
     *  attribute is removed before this one is inserted in the container.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   SingletonConfigurableAttribute.
     */
    public TransientSingletonConfigurableAttribute(NamedObj container,
        String name) throws NameDuplicationException, IllegalActionException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove any previous attribute in the container that has the same
     *  name as this attribute, and then call the base class method to set
     *  the container. If the container is not in the same workspace as
     *  this attribute, throw an exception.
     *  If this attribute is already contained by the NamedObj, do nothing.
     *  If the attribute already has a container, remove
     *  this attribute from its attribute list first.  Otherwise, remove
     *  it from the directory of the workspace, if it is there.
     *  If the argument is null, then remove this attribute from its
     *  container. It is not added to the workspace directory, so this
     *  could result in this object being garbage collected.
     *  <p>
     *  Note that since an Attribute is a NamedObj, it can itself have
     *  attributes.  However, recursive containment is not allowed, where
     *  an attribute is an attribute of itself, or indirectly of any attribute
     *  it contains.
     *  <p>
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute that is of class
     *   SingletonConfigurableAttribute.
     */
    public void setContainer(NamedObj container)
        throws IllegalActionException, NameDuplicationException {
        Attribute previous = null;

        if (container != null) {
            previous = container.getAttribute(getName());

            if (previous != null) {
                previous.setContainer(null);
            }
        }

        try {
            super.setContainer(container);
        } catch (IllegalActionException ex) {
            // Restore previous.
            if (previous != null) {
                previous.setContainer(container);
            }

            throw ex;
        } catch (NameDuplicationException ex) {
            // Restore previous.
            if (previous != null) {
                previous.setContainer(container);
            }

            throw ex;
        }
    }
}
