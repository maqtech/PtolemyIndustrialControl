/* An executable entity whose ports have types.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.data.Token;
import ptolemy.data.type.Typeable;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// TypedAtomicActor
/**
A TypedAtomicActor is an AtomicActor whose ports and parameters have types.
Derived classes may constrain the container by overriding
_checkContainer(). The Ports of TypedAtomicActors are constrained to be
TypedIOPorts.  Derived classes may further constrain the ports by
overriding the public method newPort() to create a port of the
appropriate subclass, and the protected method _addPort() to throw an
exception if its argument is a port that is not of the appropriate
subclass.
<p>
The typeConstraintList() method returns the type constraints among
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
    public TypedAtomicActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the type of an attribute.  This method is
     *  called by a contained attribute when its type changes.
     *  In this base class, the method informs the director to invalidate
     *  type resolution, if the director is not null.
     *  Thus, by default, attribute type changes cause type resolution to
     *  be redone at the next opportunity.
     *  If an actor does not allow attribute types to change, then it should
     *  override this method.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        Director director = getDirector();
        if (director != null) {
            director.invalidateResolvedTypes();
        }
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
            _workspace.getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the type constraints of this actor.
     *  The constraints have the form of a list of inequalities.
     *  In this base class, the implementation of type constraints
     *  is that the type of any input port that does not have its type
     *  declared must be less than or equal to the type of any output port
     *  that does not have its type declared.
     *  In addition, this method also collects type constraints from the
     *  contained Typeables (ports, variables, and parameters).
     *  This method is read-synchronized on the workspace.
     *  @return a list of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList()  {
	try {
	    _workspace.getReadAccess();

	    LinkedList result = new LinkedList();
	    Iterator inPorts = inputPortList().iterator();
	    while (inPorts.hasNext()) {
	        TypedIOPort inPort = (TypedIOPort)inPorts.next();
		boolean isUndeclared = inPort.getTypeTerm().isSettable();
		if (isUndeclared) {
		    // inPort has undeclared type
		    Iterator outPorts = outputPortList().iterator();
	    	    while (outPorts.hasNext()) {
		    	TypedIOPort outPort =
                            (TypedIOPort)outPorts.next();

			isUndeclared = outPort.getTypeTerm().isSettable();
		    	if (isUndeclared && inPort != outPort) {
			    // output also undeclared, not bidirectional port,
			    // check if there is any type constraints stored
			    // in ports.
			    List inPortConstraints =
			                     inPort.typeConstraintList();
			    List outPortConstraints =
			                     outPort.typeConstraintList();
                            if (inPortConstraints.isEmpty() &&
			        outPortConstraints.isEmpty()) {
				// ports not constrained, use default
				// constraint
		                Inequality inequality = new Inequality(
                                    inPort.getTypeTerm(),
                                    outPort.getTypeTerm());
			        result.add(inequality);
                            }

			}
		    }
		}
	    }

	    // collect constraints from contained Typeables
	    Iterator ports = portList().iterator();
	    while (ports.hasNext()) {
		Typeable port = (Typeable)ports.next();
		result.addAll(port.typeConstraintList());
	    }

	    Iterator attributes = attributeList().iterator();
	    while (attributes.hasNext()) {
		Attribute attribute = (Attribute)attributes.next();
		if (attribute instanceof Typeable) {
		    result.addAll(((Typeable)attribute).typeConstraintList());
		}
	    }

	    return result;

	} finally {
	    _workspace.doneReading();
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
}
