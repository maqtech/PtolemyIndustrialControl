/* An executable entity whose ports have types.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.type.Typeable;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TypedAtomicActor
/**
A TypedAtomicActor is an AtomicActor whose ports and parameters have types.
The container is required to be an instance of TypedCompositeActor.
Derived classes may further constrain the container by overriding
setContainer(). The Ports of TypedAtomicActors are constrained to be
TypedIOPorts.  Derived classes may further constrain the ports by
overriding the public method newPort() to create a port of the
appropriate subclass, and the protected method _addPort() to throw an
exception if its argument is a port that is not of the appropriate
subclass.
<p>
The typeConstraints() method returns the type constraints among
the contained ports.  This base class provides a default implementation
of this method, which should be suitable for most of the derived classes.


@author Yuhong Xiong
@version $Id$
@see ptolemy.actor.AtomicActor
@see ptolemy.actor.TypedCompositeActor
@see ptolemy.actor.TypedIOPort
*/
public class TypedAtomicActor extends AtomicActor implements TypedActor {

    // all the constructors are wrappers of the super class constructors.

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public TypedAtomicActor() {
	super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public TypedAtomicActor(Workspace workspace) {
	super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TypedAtomicActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the type of an attribute.  This method is
     *  called by a contained attribute when its type changes.
     *  In this base class, the method throws an exception.
     *  Thus, by default, attribute types are not allowed to change.
     *  If an actor can allow attribute types to change, then it should
     *  override this method.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this actor (always thrown in this base class).
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        throw new IllegalActionException(this,
                "Attribute type changes are not allowed. Attempt to change: "
                + attribute.getName());
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of TypedCompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this actor and container are not in the same workspace, or
     *   if the argument is not a TypedCompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this actor.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedCompositeActor) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedAtomicActor can only be contained by instances of " +
                    "TypedCompositeActor.");
        }
        super.setContainer(container);
    }

    /** Return the type constraints of this actor.
     *  The constraints have the form of an enumeration of inequalities.
     *  In this base class, the implementation of type constraints
     *  is that the type of any input port that does not have its type
     *  declared must be less than or equal to the type of any output port
     *  that does not have its type declared.
     *  In addition, this method also collects type constraints from the
     *  contained Typeables (ports, variables, and parameters).
     *  This method is read-synchronized on the workspace.
     *  @return an Enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

	    LinkedList result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    // inport has undeclared type
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(),
                                    outport.getTypeTerm());
			    result.insertLast(ineq);
			}
		    }
		}
	    }

	    // collect constraints from contained Typeables
	    Enumeration ports = getPorts();
	    while (ports.hasMoreElements()) {
		Typeable port = (Typeable)ports.nextElement();
		result.appendElements(port.typeConstraints());
	    }

	    Enumeration attrib = getAttributes();
	    while (attrib.hasMoreElements()) {
		Attribute att = (Attribute)attrib.nextElement();
		if (att instanceof Typeable) {
		    result.appendElements(((Typeable)att).typeConstraints());
		}
	    }

	    return result.elements();

	}finally {
	    workspace().doneReading();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of TypedIOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this actor, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of TypedIOPort. This method is <i>not</i> synchronized on
     *  the workspace, so the caller should be.
     *
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with a
     *   name already in the actor.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this actor.");
        }
        super._addPort(port);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
