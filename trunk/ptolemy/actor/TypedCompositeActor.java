/* An aggregation of typed actors.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor
/**
A TypedCompositeActor is an aggregation of typed actors.
The ports of a TypedCompositeActor are constrained to be TypedIOPorts,
the relations to be TypedIORelations, and the actors to be instances of
ComponentEntity that implement the TypedActor interface.  Derived classes
may impose further constraints by overriding newPort(), _addPort(),
newRelation(), _addRelation(), and _addEntity().
<p>
The container is constrained to be an instance of TypedCompositeActor.
Derived classes may impose further constraints by overriding setContainer().

@author Yuhong Xiong
@version $Id$
@see ptolemy.actors.TypedIOPort
@see ptolemy.actors.TypedIORelation
@see ptolemy.actors.TypedActor
@see ptolemy.kernel.ComponentEntity
@see ptolemy.actors.CompositeActor
*/
public class TypedCompositeActor extends CompositeActor implements TypedActor {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a TypedCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public TypedCompositeActor() {
        super();
    }

    /** Construct a TypedCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public TypedCompositeActor(Workspace workspace) {
	super(workspace);
    }

    /** Construct a TypedCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TypedCompositeActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return A new TypedIOPort.
     *  @exception NameDuplicationException If this actor already has a
     *   port with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedCompositeActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }

    /** Create a new TypedIORelation with the specified name, add it
     *  to the relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of TypedIORelation.
     *  This method is write-synchronized on the workspace.
     *
     *  @return A new TypedIORelation.
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already on the container's contents list.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIORelation rel = new TypedIORelation(this, name);
            return rel;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of TypedCompositeActor. If it is, call the base
     *  class setContainer() method.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this actor and the container are not in the same workspace, or
     *   if the argument is not a TypedCompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this actor.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedCompositeActor) &&
	    (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedCompositeActor can only be contained by instances "
		    + "of TypedCompositeActor.");
        }
        super.setContainer(container);
    }

    /** Return the type constraints of this typed composite actor.
     *  The constraints have the form of an enumeration of inequalities.
     *  The constraints come from two sources, the topology and the
     *  contained actors. To generate the constraints based on the
     *  topology, this method scans all the connections from an output
     *  port to an input. If one or both of the input and output
     *  ports are undeclared, this method forms a type constraint that
     *  requires the output type to be less than or equal to the input.
     *  To collect the type constraints from the contained actors,
     *  This method recursively calls the typeConstaints() method of the
     *  deeply contained actors and combine all the constraints together.
     *  <p>
     *  In addition to collecting type constraints, This method also
     *  does static type checking.  This is done while scanning all the
     *  connection from an output port to an input. If both the input
     *  and output ports have declared types, this method checks if the
     *  output type is less than or equal to the input. If not, it
     *  throws a TypeConflictException.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *  @return an Enumerations of Inequality.
     *  @exception TypeConflictException If type conflict is detected
     *   during static type checking.
     *  @see ptolemy.graph.Inequality
     */
    public Enumeration typeConstraints()
	    throws TypeConflictException {
	try {
	    workspace().getReadAccess();
	    LinkedList result = new LinkedList();

	    Enumeration e = deepGetEntities();
	    while (e.hasMoreElements()) {
	        // collect type constraints from contained actors
	        TypedActor actor = (TypedActor)e.nextElement();
	        result.appendElements(actor.typeConstraints());

	        // type check on all output connections
	        // NOTE: this can also be done on all input connections.
		Enumeration outports = actor.outputPorts();
	        while (outports.hasMoreElements()) {
		    TypedIOPort outport = (TypedIOPort)outports.nextElement();

		    Enumeration inports = outport.deepConnectedInPorts();
		    while (inports.hasMoreElements()) {
		        TypedIOPort inport = (TypedIOPort)inports.nextElement();

		        Class outDeclared = outport.getDeclaredType();
		        Class inDeclared = inport.getDeclaredType();
		        if (outDeclared != null && inDeclared != null) {
			    // both in/out ports are declared, type check
			    int compare = TypeCPO.compare(outDeclared,
                                    inDeclared);
			    if (compare == CPO.HIGHER ||
                                compare == CPO.INCOMPARABLE) {
			        throw new TypeConflictException(outport,
				    inport, "Output port type is not less " +
				    "than or equal to the connected inpurt.");
			    }
		        } else {
			    // at least one of the in/out ports does not have
			    // declared type, form type constraint.
			    Inequality ineq = new Inequality(outport, inport);
			    result.insertLast(ineq);
		        }
		    }
	        }
	    }
	    return result.elements();
	} finally {
	    workspace().doneReading();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the TypedActor interface. This
     *  method does not alter the actor in any way.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity TypedActor to contain.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument does not implement the TypedActor interface.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the actor contents list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof TypedActor)) {
            throw new IllegalActionException(this, entity,
                    "TypedCompositeActor can only contain entities that " +
		    "implement the TypedActor interface.");
        }
        super._addEntity(entity);
    }

    /** Add a port to this actor. This overrides the base class to
     *  throw an exception if the proposed port is not an instance of
     *  TypedIOPort.  This method should not be used directly.  Call the
     *  setContainer() method of the port instead. This method does not set
     *  the container of the port to point to this actor.
     *  It assumes that the port is in the same workspace as this
     *  actor, but does not check.  The caller should check.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "TypedCompositeActor can only contain instances of " +
		    "TypedIOPort.");
        }
        super._addPort(port);
    }

    /** Add a relation to this container. This overrides the base class to
     *  throw an exception if the proposed relation is not an instance of
     *  TypedIORelation. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set the container of the relation to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The TypedIORelation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of TypedIORelation.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "TypedCompositeActor can only contain instances of " +
		    "TypedIORelation.");
        }
        super._addRelation(relation);
    }


    // NOTE: There is nothing new to report in the _description() method,
    // so we do not override it.

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}

