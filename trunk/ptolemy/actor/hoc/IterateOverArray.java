/* An actor that iterates a contained actor over input arrays.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
 */

package ptolemy.actor.hoc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DropListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// IterateOverArray
/**
This actor iterates a contained actors over input arrays.
Each input port expects an array. When this actor fires,
an array is read on each input port that has one, and its
contents are provided sequentially to the contained actors
with connected ports.  This actor then iterates the
contained actors in the order in which they appear
in the actor list (which is the order in which they were
created) repeatedly until either there are no more
input data for the actor or the prefire() method of the actor
returns false. If postfire() of any actor returns false,
then postfire() of this director will return false, requesting
a halt to execution of the model.  The outputs from the
contained actors are collected into arrays that are
produced on the outputs of this actor.
<p>
Normally, this actor is expected to contain only a single actor,
which can, of course, be a composite actor. To make it easier to
use, it reacts to a "drop" of an actor on it by replicating the
ports and parameters of that actor.
<p>
A special variable named "iterationCount" can be used in
any expression setting the value of a parameter of this actor
or its contents. This variable has an integer value that
starts at 1 during the first iteration of the contained
actor(s) and is incremented by 1 on each firing. Typically,
it's final value will be the size of the input array(s),
assuming that the inside actors consume one token on each
firing.
<p>
This actor is properly viewed as a "higher-order component" in
that its contained actor is parameter that specifies how to
operate on input arrays.  It is inspired by the higher-order
functions of functional languages, but unlike those, the
contained actor need not be functional. That is, it can have
state.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
*/
public class IterateOverArray extends TypedCompositeActor
        implements DropListener {
    
    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IterateOverArray(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().className = "ptolemy.actor.hoc.IterateOverArray";
        new IterateDirector(this, uniqueName("IterateDirector"));
        
        _iterationCount = new Variable(this, "iterationCount", new IntToken(0));
        _iterationCount.setTypeEquals(BaseType.INT);
        
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"-6\" y=\"10\""
                + "style=\"font-size:24\">?</text>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    /** React to the dropping of an object onto this object by
     *  adjusting the ports and parameters to match the contained
     *  entity, if necessary. This is called when a user interface
     *  drops an object into an object implementing this interface.
     *  The call actually occurs when the change request is queued,
     *  so the listener (which implements this method) should react
     *  from with a change request itself, so as to ensure that
     *  the reaction occurs after the drop has been completed.
     */
    public void dropped() {
                
        // FIXME: When an actor is dropped into this actor, it
        // apparently gets rendered double, leaving the old figure
        // as debris in the graph.
        
        // FIXME: Undo doesn't work to reverse the effects of the drop.

        // NOTE: It would be nice if we could react to the added entity
        // in _addEntity(), but it has not been fully constructed, and
        // in particular, it has no ports at that time.  Thus, we
        // use the HACK to queue a request here instead. I tried creating a
        // ChangeRequest in _addEntity(), but this didn't
        // work either because the ChangeRequest would be executed
        // immediately if possible.
        // I also tried listening as a ChangeListener, but this didn't
        // work because this would result in notification when an icon
        // was created in this object, which happened when first reading
        // a MoML file containing this actor.  This would result in
        // collisions on the ports created here.
        
        // This needs to be a MoMLChangeRequest so that undo works.
        // Use the container as a context so that redraw occurs after
        // the change request is executed.
        NamedObj tmpContext = MoMLChangeRequest
                .getDeferredToParent((NamedObj)getContainer());
        if (tmpContext == null) {
            tmpContext = ((NamedObj)getContainer());
        }
        final NamedObj context = tmpContext;
        MoMLChangeRequest request = new MoMLChangeRequest(
                this,
                context,
                "Adjust ports and parameters") {
            protected void _execute() throws Exception {
                // NOTE: We defer the construction of the MoML change request
                // to here because only at this point can we be sure that the
                // drop change request has completed.
                
                synchronized(this) {
                    
                    StringBuffer command = new StringBuffer("<group>\n");
                    command.append("<entity name=\"");
                    command.append(getName(context));
                    command.append("\">\n");
                    
                    // Entity most recently added.
                    ComponentEntity entity = null;

                    // Delete any previously contained entity.
                    Iterator priors = entityList().iterator();
                    LinkedList deletedEntities = new LinkedList();
                    while (priors.hasNext()) {
                        ComponentEntity prior = (ComponentEntity)priors.next();
                        // If there is at least one more contained object,
                        // then delete this one.
                        if (priors.hasNext()) {
                            command.append("<deleteEntity name=\"");
                            command.append(prior.getName());
                            command.append("\"/>\n");
                            deletedEntities.add(prior);
                        } else {
                            entity = prior;
                        }
                    }

                    if (entity == null) {
                        // Nothing to do.
                        return;
                    }
                    
                    // Add commands to delete parameters that are associated
                    // with a deleted entity.
                    Iterator attributes = attributeList(Parameter.class).iterator();
                    while (attributes.hasNext()) {
                        Attribute attribute = (Attribute)attributes.next();
                        if (attribute == _iterationCount) continue;
                        // Only delete attributes whose names don't match
                        // an attribute of the new entity, but do match an
                        // attribute of a deleted entity.
                        // This preserves values of attributes with the same
                        // name.
                        if (entity.getAttribute(attribute.getName()) == null) {
                            Iterator deleted = deletedEntities.iterator();
                            while(deleted.hasNext()) {
                                ComponentEntity deletedEntity
                                        = (ComponentEntity)deleted.next();
                                if (deletedEntity.getAttribute(
                                        attribute.getName()) != null) {
                                    command.append("<deleteProperty name=\"");
                                    command.append(attribute.getName());
                                    command.append("\"/>\n");
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Add commands to add parameters as needed.
                    // We do only parameters, not all Settables because only
                    // parameters can propogate the values down the hierarchy.
                    attributes = entity.attributeList(Parameter.class).iterator();
                    while (attributes.hasNext()) {
                        Parameter attribute = (Parameter)attributes.next();
                        // Add the attribute if there isn't one already.
                        if (attribute.getVisibility() == Settable.FULL
                                && getAttribute(attribute.getName()) == null) {
                            // The class we want to generate, unfortunately,
                            // depends on the mode, not just the exported
                            // MoML for this parameter.
                            command.append("<property name=\"");
                            command.append(attribute.getName());
                            command.append("\" class=\"");
                            if (attribute instanceof PortParameter) {
                                command.append("ptolemy.actor.parameters.PortParameter");
                            } else {
                                command.append("ptolemy.data.expr.Parameter");
                            }
                            command.append("\" value=\"");
                            command.append(attribute.getExpression());
                            command.append("\">");
                            if (attribute.isStringMode()) {
                                command.append("<property name=\"_stringMode\" "
                                + "class=\"ptolemy.kernel.util.Attribute\"/>");
                            }
                            command.append("</property>\n");
                            // FIXME: For the above, if the original Parameter has
                            // choices, then we should create a choice attribute here.

                            // Set the attribute on the inside to reflect
                            // the one on the outside.  This is a little
                            // tricky because the syntax is different if
                            // it's a StringParameter.
                            command.append("<entity name=\"");
                            command.append(entity.getName());
                            command.append("\"><property name=\"");
                            command.append(attribute.getName());
                            if (attribute.isStringMode()) {
                                command.append("\" value=\"$");
                            } else {
                                command.append("\" value=\"");
                            }
                            command.append(attribute.getName());
                            command.append("\"/></entity>\n");
                        }
                    }

                    // Add commands to delete ports.
                    // FIXME: How to deal with ports that are added later?
                    // Perhaps ports should be added on the outside.
                    Iterator ports = portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        // Only delete ports whose names don't match.
                        // This preserves connections to ports with the same
                        // name.
                        if (entity.getPort(port.getName()) == null) {
                            // FIXME: Should explicitly delete relations linked
                            // to these ports, or undo won't work properly.
                            // This is a bit of a pain, since we have to pop
                            // out of this context to do it.
                            command.append("<deletePort name=\"");
                            command.append(port.getName());
                            command.append("\"/>\n");
                        }
                    }
                    
                    // Remove all inside relations. This will have the
                    // side effect of removing connections on the inside.
                    Iterator relations = relationList().iterator();
                    while(relations.hasNext()) {
                        command.append("<deleteRelation name=\"");
                        command.append(((NamedObj)relations.next()).getName());
                        command.append("\"/>\n");
                    }

                    // Set up the inside connections.
                    int count = 1;
                    Iterator entityPorts = entity.portList().iterator();
                    while (entityPorts.hasNext()) {
                        Port insidePort = (Port)entityPorts.next();
                        
                        // Skip ports associated with PortParameters.
                        if (insidePort instanceof ParameterPort) continue;
                        String name = insidePort.getName();
                        
                        // If there isn't already a port with this name,
                        // the MoML parser will create one.
                        // Do not specify a class so that
                        // the MoMLParser uses newPort() to create it.
                        command.append("<port name=\"");
                        command.append(name);
                        command.append("\">");
                        if (insidePort instanceof IOPort) {
                            IOPort castPort = (IOPort)insidePort;
                            command.append("<property name=\"multiport\" value=\"");
                            command.append(castPort.isMultiport());
                            command.append("\"/>");
                            
                            command.append("<property name=\"input\" value=\"");
                            command.append(castPort.isInput());
                            command.append("\"/>");

                            command.append("<property name=\"output\" value=\"");
                            command.append(castPort.isOutput());
                            command.append("\"/>");
                        }
                        command.append("</port>\n");
                                                  
                        // Set up inside connections. Note that if the outside
                        // port was preserved from before, then it will have
                        // lost its inside links when the inside relation was
                        // deleted. Thus, we need to recreate them.
                        // Presumably, there are no inside relations now, so
                        // we can use any suitable names.
                        String relationName = "insideRelation" + count++;
                        command.append("<relation name=\"");
                        command.append(relationName);
                        command.append("\"/>\n");
                        
                        command.append("<link port=\"");
                        command.append(name);
                        command.append("\" relation=\"");
                        command.append(relationName);
                        command.append("\"/>\n");

                        command.append("<link port=\"");
                        command.append(insidePort.getName(IterateOverArray.this));
                        command.append("\" relation=\"");
                        command.append(relationName);
                        command.append("\"/>\n");
                    }
                    
                    command.append("</entity>\n</group>\n");
                    // The MoML command is the description of the change request.
                    setDescription(command.toString());
                    
                    // Uncomment the following to see the (rather complicated)
                    // MoML command that is issued.
                    // System.out.println(command.toString());
                                        
                    super._execute();
                }
            }
        };
        // Do this so that a single undo reverses the entire operation.
        request.setMergeWithPreviousUndo(true);
        
        requestChange(request);        
    }

    /** Override the base class to return a specialized port.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            IteratePort port = new IteratePort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            workspace().doneWriting();
        }
    }
    
    /** Set type constraints, create Receivers and invoke the
     *  preinitialize() method of its local director.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it.
     */
    public void preinitialize() throws IllegalActionException {
        
        // NOTE: It would have been nice to do this in _addPort, but
        // this doesn't work because _addPort() is called in the constructor
        // of the port, and the port is not fully constructed!
        
        // FIXME: This doesn't support mutations because this
        // is only done in preinitialize().
        Iterator ports = portList().iterator();
        while(ports.hasNext()) {
            TypedIOPort port = (TypedIOPort)ports.next();
            ArrayType arrayType = new ArrayType(BaseType.UNKNOWN);
            ((TypedIOPort)port).setTypeEquals(arrayType);
        }
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check types from a source port to a group of destination ports,
     *  assuming the source port is connected to all the ports in the
     *  group of destination ports.  Return a list of instances of
     *  Inequality that have type conflicts.
     *  @param sourcePort The source port.
     *  @param destinationPortList A list of destination ports.
     *  @return A list of instances of Inequality indicating the
     *   type constraints that are not satistfied.
     */
    protected List _checkTypesFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();
        
        boolean isUndeclared = sourcePort.getTypeTerm().isSettable();
        if (!isUndeclared) {
            // sourcePort has a declared type.
            Type srcDeclared = sourcePort.getType();
            Iterator destinationPorts = destinationPortList.iterator();
            while (destinationPorts.hasNext()) {
                TypedIOPort destinationPort =
                    (TypedIOPort)destinationPorts.next();
                isUndeclared = destinationPort.getTypeTerm().isSettable();

                if (!isUndeclared) {
                    // both source/destination ports are declared,
                    // check type
                    Type destDeclared = destinationPort.getType();
                    
                    int compare;
                    // If the source port belongs to me, then we want to
                    // compare its array element type to the type of the
                    // destination.
                    if(sourcePort.getContainer() == this
                            && destinationPort.getContainer() != this) {
                        // The source port belongs to me, but not the destination.
                        Type srcElementType = ((ArrayType)srcDeclared).getElementType();
                        compare = TypeLattice.compare(srcElementType, destDeclared);
                    } else if(sourcePort.getContainer() != this
                            && destinationPort.getContainer() == this) {
                        // The destination port belongs to me, but not the source.
                        Type destElementType = ((ArrayType)destDeclared).getElementType();
                        compare = TypeLattice.compare(srcDeclared, destElementType);
                    } else {
                        compare = TypeLattice.compare(srcDeclared, destDeclared);
                    }
                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        Inequality inequality = new Inequality(
                                sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(inequality);
                    }
                }
            }
        }
        return result;
    }

    /** Return the type constraints on all connections starting from the
     *  specified source port to all the ports in a group of destination
     *  ports. This overrides the base class to ensure that if the source
     *  port or the destination port is a port of this composite, then
     *  the port is forced to be an array type and the proper constraint
     *  on the element type of the array is made. If the source port
     *  has no possible sources of data, then no type constraints are
     *  added for it.
     *  @param sourcePort The source port.
     *  @param destinationPortList The destination port list.
     *  @return A list of instances of Inequality.
     */
    protected List _typeConstraintsFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();
        
        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();
        Iterator destinationPorts = destinationPortList.iterator();
        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort)destinationPorts.next();
            boolean destUndeclared =
                    destinationPort.getTypeTerm().isSettable();

            if (srcUndeclared || destUndeclared) {
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                if(sourcePort.getContainer() == this &&
                        destinationPort.getContainer() == this) {
                    // Both ports belong to this, so their type must be equal.
                    // Represent this with two inequalities.
                    Inequality ineq1 = new Inequality(sourcePort.getTypeTerm(),
                            destinationPort.getTypeTerm());
                    result.add(ineq1);
                    Inequality ineq2 = new Inequality(destinationPort.getTypeTerm(),
                            sourcePort.getTypeTerm());
                    result.add(ineq2);
                } else if (sourcePort.getContainer().equals(this)) {
                    if (sourcePort.sourcePortList().size() == 0) {
                        // Skip this port. It is not connected on the outside.
                        continue;
                    }
                    Type sourcePortType = sourcePort.getType();
                    if (!(sourcePortType instanceof ArrayType)) {
                        throw new InternalErrorException(
                        "Source port was expected to be an array type: "
                        + sourcePort.getFullName()
                        + ", but it had type: "
                        + sourcePortType);
                    }
                    InequalityTerm elementTerm = ((ArrayType)sourcePortType).getElementTypeTerm();
                    Inequality ineq = new Inequality(elementTerm, destinationPort.getTypeTerm());
                    result.add(ineq);
                } else if (destinationPort.getContainer().equals(this)) {
                    Type destinationPortType = destinationPort.getType();
                    if (!(destinationPortType instanceof ArrayType)) {
                        throw new InternalErrorException(
                        "Destination port was expected to be an array type: "
                        + destinationPort.getFullName()
                        + ", but it had type: "
                        + destinationPortType);
                    }
                    InequalityTerm elementTerm = ((ArrayType)destinationPortType).getElementTypeTerm();
                    Inequality ineq = new Inequality(sourcePort.getTypeTerm(), elementTerm);
                    result.add(ineq);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Variable that reflects the current iteration count on the
    // inside.
    private Variable _iterationCount;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// IterateDirector
    
    /** This is a specialized director that fires contained actors
     *  in the order in which they appear in the actor list repeatedly
     *  until either there is no more input data for the actor or
     *  the prefire() method of the actor returns false. If postfire()
     *  of any actor returns false, then postfire() of this director
     *  will return false, requesting a halt to execution of the model.
     */
    private class IterateDirector extends Director {

        /** Create a new instance of the director for IterateOverArray.
         *  @param container The container for the director.
         *  @param name The name of the director.
         *  @throws IllegalActionException Should not be thrown.
         *  @throws NameDuplicationException Should not be thrown.
         */
        public IterateDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            setPersistent(false);
        }

        /** Invoke iterations on each of the deeply contained actors of the
         *  container of this director repeatedly until either it runs out
         *  of input data or prefire() returns false. If postfire() of any
         *  actor returns false, then set a flag indicating to postfire() of
         *  this director to return false.  The contained
         *  actors are iterated in the order reported by the entityList()
         *  method of the container.
         *  @exception IllegalActionException If any called method of one
         *  of the associated actors throws it.
         */
        public void fire() throws IllegalActionException {
            Nameable container = getContainer();
            Iterator actors = ((CompositeActor)container)
                    .deepEntityList().iterator();
            _postfireReturns = true;
            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor)actors.next();
                int result = Actor.COMPLETED;
                int iterationCount = 0;
                while (result != Actor.NOT_READY) {
                    iterationCount++;
                    _iterationCount.setToken(new IntToken(iterationCount));
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_ITERATE,
                                iterationCount));
                    }
                    result = actor.iterate(1);
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_ITERATE,
                                iterationCount));
                    }
                    // Should return if there is no more input data, irrespective
                    // of return value of perfire() of the actor, which
                    // is not reliable.
                    boolean outOfData = true;
                    Iterator inPorts = actor.inputPortList().iterator();
                    while (inPorts.hasNext()) {
                        IOPort port = (IOPort)inPorts.next();
                        for(int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i)) {
                                outOfData = false;
                                break;
                            }
                        }
                    }
                    if (outOfData) {
                        if (_debugging) {
                            _debug("No more input data for: "
                                    + ((Nameable)actor).getFullName());
                        }
                        break;
                    }
                    if (result == Actor.STOP_ITERATING) {
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable)actor).getFullName());
                        }
                        _postfireReturns = false;
                        break;
                    }
                }
            }
        }

        /** Delegate by calling fireAt() on the director of the container's
         *  container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAt(Actor actor, double time)
                throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();
            if (director != null) {
                director.fireAt(actor, time);
            }
        }

        /** Delegate by calling fireAtCurrentTime() on the director
         *  of the container's container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAtCurrentTime(Actor actor)
                throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();
            if (director != null) {
                director.fireAtCurrentTime(actor);
            }
        }
        
        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Override the base class to return the logical AND of
         *  what the base class postfire() method return and the
         *  flag set in fire().  As a result, this will return
         *  false if any contained actor returned false in its
         *  postfire() method.
         */
        public boolean postfire() throws IllegalActionException {
            boolean superReturns = super.postfire();
            return (superReturns && _postfireReturns);
        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method extracts tokens from the input array and
         *  provides them sequentially to the corresponding ports
         *  of the contained actor.
         *  @exception IllegalActionException Should not be thrown.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         */
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {

            boolean result = false;
            for (int i = 0; i < port.getWidth(); i++) {
                // NOTE: This is not compatible with certain cases
                // in PN, where we don't want to block on a port
                // if nothing is connected to the port on the
                // inside.
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            if (_debugging) {
                                _debug(getName(),
                                        "transferring input from "
                                        + port.getName());
                            }
                            ArrayToken arrayToken = (ArrayToken)t;
                            for(int j = 0; j < arrayToken.length(); j++) {
                                port.sendInside(i, arrayToken.getElement(j));
                            }
                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
            return result;
        }

        /** Transfer data from the inside receivers of an output port of the
         *  container to the ports it is connected to on the outside.
         *  This method packages the available tokens into a single array.
         *  @exception IllegalActionException Should not be thrown.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @see IOPort#transferOutputs
         */
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    ArrayList list = new ArrayList();
                    while(port.isKnownInside(i) && port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        list.add(t);
                    }
                    if(list.size() != 0) {
                        Token[] tokens = (Token[])list.toArray(new Token[list.size()]);
                        if (_debugging) {
                            _debug(getName(),
                                    "transferring output to "
                                    + port.getName());
                        }
                        port.send(i, new ArrayToken(tokens));
                    }
                    result = true;
                } catch (NoTokenException ex) {
                    throw new InternalErrorException(this, ex, null);
                }
            }
            return result;
        }
        
        //////////////////////////////////////////////////////////////
        ////                   private variables                  ////
        
        // Indicator that at least one actor returned false in postfire.
        private boolean _postfireReturns = true;
    }
    
    ///////////////////////////////////////////////////////////////////
    //// IteratePort
    
    /** This is a specialized port for handling type conversions between
     *  the array types of the ports of the enclosing IterateOverArray
     *  actor and the scalar types (or arrays with one less dimension)
     *  of the actor that are contained.
     */
    public static class IteratePort extends TypedIOPort {
        
        // NOTE: This class has to be static because otherwise the
        // constructor has an extra argument (the first argument,
        // actually) that is an instance of the enclosing class.
        // The MoML parser cannot know what the instance of the
        // enclosing class is, so it would not be able to instantiate
        // these ports.

        /** Create a new instance of a port for IterateOverArray.
         *  @param container The container for the port.
         *  @param name The name of the port.
         *  @throws IllegalActionException Should not be thrown.
         *  @throws NameDuplicationException Should not be thrown.
         */
        public IteratePort(IterateOverArray container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            // NOTE: Ideally, Port are created when an entity is added.
            // However, there appears to be no clean way to do this.
            // Instead, ports are added when an entity is added via a
            // change request registered with this IterateOverArray actor.
            // Consequently, these ports have to be persistent, and this
            // constructor and class have to be public.
            // setPersistent(false);
        }
        
        /** Override the base class to convert the token to the element
         *  type rather than to the type of the port.
         *  @param token The token to convert.
         *  @exception IllegalActionException If the convertion is
         *   invalid.
         */
        public Token convert(Token token) throws IllegalActionException {
            // If this port is an output port, then we assume the data
            // is being sent from the inside, and hence needs to be converted.
            if (isOutput()) {
                Type type = ((ArrayType)getType()).getElementType();
                if (type.equals(token.getType())) {
                    return token;
                } else {
                    Token newToken = type.convert(token);
                    return newToken;
                }
            } else {
                return super.convert(token);
            }
        }

        /** Override the base class to convert the token to the element
         *  type rather than to the type of the port.
         *  @param channelIndex The index of the channel, from 0 to width-1
         *  @param token The token to send
         *  @exception NoRoomException If there is no room in the receiver.
         *  @exception IllegalActionException Not thrown in this class.
         */
        public void sendInside(int channelIndex, Token token)
                throws IllegalActionException, NoRoomException {
            Receiver[][] farReceivers;
            if (_debugging) {
                _debug("send inside to channel " + channelIndex + ": " + token);
            }
            try {
                try {
                    _workspace.getReadAccess();
                    ArrayType type = (ArrayType)getType();
                    int compare = TypeLattice.compare(token.getType(),
                            type.getElementType());
                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        throw new IllegalActionException(
                                "Run-time type checking failed. Token type: "
                                + token.getType().toString() + ", port: "
                                + getFullName() + ", port type: "
                                + getType().toString());
                    }

                    // Note that the getRemoteReceivers() method doesn't throw
                    // any non-runtime exception.
                    farReceivers = deepGetReceivers();
                    if (farReceivers == null ||
                            farReceivers[channelIndex] == null) return;
                } finally {
                    _workspace.doneReading();
                }
                for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                    TypedIOPort port =
                        (TypedIOPort)farReceivers[channelIndex][j].getContainer();
                    Token newToken = port.convert(token);
                    farReceivers[channelIndex][j].put(newToken);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // NOTE: This may occur if the channel index is out of range.
                // This is allowed, just do nothing.
            }
        }
    }
}
