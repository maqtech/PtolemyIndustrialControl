/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.TypedActor;

import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// FSMActor
/**
An FSMActor contains a set of states and transitions. A transition has
a guard expression and a trigger expression. A transition is enabled and
can be taken when its guard is true. A transition is triggered and must be
taken when its trigger is true. A transition can contain a set of actions.
<p>
When an FSMActor is fired, the outgoing transitions of the current state
are examined. An IllegalActionException is thrown if there is more than one
enabled transition. If there is exactly one enabled transition then it is
chosen and the choice actions contained by the transition are executed.
An FSMActor does not change state during successive firings in one iteration
in order to support domains that iterate to a fixed point. When the FSMActor
is postfired, the chosen transition of the latest firing of the actor is
committed. The commit actions contained by the transition are executed and
the current state of the actor is set to the destination state of the
transition.
<p>
An FSMActor enters its initial state during initialization. The name of the
initial state is specified by the <i>initialStateName</i> string attribute.
<p>
An FSMActor contains a set of variables for the input ports that can be
referenced in the guard and trigger expressions of transitions. If an input
port is a single port, two variables are created: one is an input status
variable with name "<i>portName</i>_isPresent";
the other is input value variable
with name "<i>portName</i>". The input status variable always contains a
BooleanToken. When this actor is fired, the status variable is set to true
if the port has a token, false otherwise. The input value variable always
contains the latest token received from the port.
If the given port is a multiport, a status variable and a value variable are
created for each channel. The status variable is named
"<i>portName</i>_<i>channelIndex</i>_isPresent". The value variable is named
"<i>portName</i>_<i>channelIndex</i>".
<p>
An FSMActor can be used in a modal model to represent the mode control logic.
A state can have a TypedActor refinement. A transition in an FSMActor can be
preemptive or non-preemptive. When a preemptive transition is chosen, the
refinement of its source state is not fired. A non-preemptive transition can
only be chosen after the refinement of its source state is fired.

@author Xiaojun Liu
@version $Id$
@see State
@see Transition
@see Action
@see FSMDirector
*/
public class FSMActor extends CompositeEntity implements TypedActor {

    /** Construct an FSMActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public FSMActor() {
        super();
        _init();
    }

    /** Construct an FSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public FSMActor(Workspace workspace) {
	super(workspace);
        _init();
    }

    /** Create an FSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying the name of the initial state of this
     *  actor.
     */
    public StringAttribute initialStateName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>initialStateName</i> attribute, record the change but do
     *  not check whether this actor contains a state with the specified
     *  name.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == initialStateName) {
            _initialStateVersion = -1;
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        FSMActor newObject = (FSMActor)super.clone(workspace);
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        newObject._connectionMapsVersion = -1;
	newObject._connectionMaps = null;
        newObject._initialStateVersion = -1;
	newObject._inputVariableMap = new HashMap();
        return newObject;
    }


    /** Return the current state of this actor.
     *  @return The current state of this actor.
     */
    public State currentState() {
        return _currentState;
    }

    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  Otherwise, execute the choice actions contained by the chosen
     *  transition.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        _setInputVariables();
        List trList = _currentState.outgoingPort.linkedRelationList();
        _chooseTransition(trList);
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeEntity container = (CompositeEntity)getContainer();
        if (container instanceof CompositeActor) {
            return ((CompositeActor)container).getDirector();
        }
        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the initial state of this actor. The name of the initial
     *  state is specified by the <i>initialStateName</i> attribute. An
     *  exception is thrown if this actor does not contain a state with
     *  the specified name.
     *  This method is read-synchronized on the workspace.
     *  @return The initial state of this actor.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    public State getInitialState() throws IllegalActionException {
        if (_initialStateVersion == workspace().getVersion()) {
            return _initialState;
        }
        try {
            workspace().getReadAccess();
            String name = initialStateName.getExpression();
            if (name == null || name.trim().equals("")) {
                throw new IllegalActionException(this,
                "No initial state has been specified.");
            }
            State st = (State)getEntity(name);
            if (st == null) {
                throw new IllegalActionException(this, "Cannot find "
                        + "initial state with name \"" + name
                        + "\".");
            }
            _initialState = st;
            _initialStateVersion = workspace().getVersion();
            return _initialState;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
	try {
	    _workspace.getReadAccess();
	    CompositeEntity container = (CompositeEntity)getContainer();
	    if (container instanceof CompositeActor) {
		return ((CompositeActor)container).getManager();
	    }
	    return null;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** Perform domain-specific initialization by calling the
     *  initialize(Actor) method of the director.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        getDirector().initialize(this);
    }

    /** Return a list of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input TypedIOPort objects.
     */
    public List inputPortList() {
        if(_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                // Update the cache.
                LinkedList inports = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if (p.isInput()) {
                        inports.add(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedInputPorts;
    }

    /** Return true.
     *  @return True.
     */
    public boolean isOpaque() {
        return true;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalant to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
	int n = 0;
	while (n++ < count) {
	    if (prefire()) {
		fire();
		if(!postfire()) return STOP_ITERATING;
	    } else {
                return NOT_READY;
	    }
	}
	return COMPLETED;
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

    /** Return a new receiver of a type compatible with the director.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            Transition tr = new Transition(this, name);
            return tr;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output TypedIOPort objects.
     */
    public List outputPortList() {
        if(_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }
                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedOutputPorts;
    }

    /** Execute actions on the last chosen transition. Change state
     *  to the destination state of the last chosen transition.
     *
     *  @return True.
     *  @exception IllegalActionException If any action throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _commitLastChosenTransition();
        return true;
    }

    /** Return true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        _lastChosenTransition = null;
        return true;
    }

    /** Create receivers and input variables for the input ports of this
     *  actor, and validate attributes. Set current state to the initial
     *  state. Throw an exception if this actor does not contain a state with
     *  name specified by the <i>initialStateName</i> attribute.
     *  @exception IllegalActionException If this actor does not contain a
     *   state with name specified by the <i>initialStateName</i> attribute.
     */
    public void preinitialize() throws IllegalActionException {

        _createReceivers();
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inport = (TypedIOPort)inputPorts.next();

            _removeInputVariables(inport);
        }
        Iterator attributes = attributeList(Settable.class).iterator();
        while(attributes.hasNext()) {
            Settable attribute = (Settable)attributes.next();
            attribute.validate();
        }
        _createInputVariables();
        _gotoInitialState();
    }

    /** Reset current state to the initial state. Throw an
     *  IllegalActionException if this actor does not contain a state with
     *  name specified by the <i>initialStateName</i> attribute.
     *  @exception IllegalActionException If this actor does not contain a
     *   state with name specified by the <i>initialStateName</i> attribute.
     */
    public void reset() throws IllegalActionException {
        _gotoInitialState();
    }

    /** Do nothing.
     */
    public void stopFire() {
    }

    /** Try wrap up.
     */
    public void terminate() {
        try {
            wrapup();
        }
        catch (IllegalActionException e) {
            // Just ignore everything and terminate.
        }
    }

    /** Return the type constraints of this actor. The constraints
     *  have the form of a list of inequalities. This method first
     *  creates constraints such that the type of any input port that
     *  does not have its type declared must be less than or equal to
     *  the type of any output port that does not have its type
     *  declared. Type constraints from the contained Typeables
     *  (ports, variables, and parameters) are collected. In addition,
     *  type constraints from all the transitions are added. These
     *  constraints are determined by the guard and trigger expressions
     *  of transitions, and actions contained by the transitions.
     *  This method is read-synchronized on the workspace.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
        try {
            _workspace.getReadAccess();

            List result = new LinkedList();
            Iterator inPorts = inputPortList().iterator();
            while (inPorts.hasNext()) {
                TypedIOPort inport = (TypedIOPort)inPorts.next();
                boolean isUndeclared = inport.getTypeTerm().isSettable();
                if (isUndeclared) {
                    // inport has undeclared type
                    Iterator outPorts = outputPortList().iterator();
                    while (outPorts.hasNext()) {
                        TypedIOPort outport =
                            (TypedIOPort)outPorts.next();

                        isUndeclared = outport.getTypeTerm().isSettable();
                        if (isUndeclared && inport != outport) {
                            // outport also has undeclared type
                            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(),
                                    outport.getTypeTerm());
                            result.add(ineq);
                        }
                    }
                }
            }

            // Collect constraints from contained Typeables.
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                Typeable port = (Typeable)ports.next();
                result.addAll(port.typeConstraintList());
            }

            Iterator attrib = attributeList(Typeable.class).iterator();
            while (attrib.hasNext()) {
                Typeable att = (Typeable)attrib.next();
                result.addAll(att.typeConstraintList());
            }

            // Collect constraints from all transitions.
            Iterator trs = relationList().iterator();
            while (trs.hasNext()) {
                Relation tr = (Relation)trs.next();
                attrib = tr.attributeList(Typeable.class).iterator();
                while (attrib.hasNext()) {
                    Typeable att = (Typeable)attrib.next();
                    result.addAll(att.typeConstraintList());
                }
            }

            return result;

        } finally {
            _workspace.doneReading();
        }
    }

    /** Do nothing.  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    //FIXME: Destroy input variables.
    public void wrapup() throws IllegalActionException {
        //Iterator inputPorts = inputPortList().iterator();
        //while (inputPorts.hasNext()) {
        //    TypedIOPort inport = (TypedIOPort)inputPorts.next();
        //    _removeInputVariables(inport);
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a state to this FSMActor. This overrides the base-class
     *  method to make sure the argument is an instance of State.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to contain.
     *  @exception IllegalActionException If the state has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of State.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the state list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof State)) {
            throw new IllegalActionException(this, entity,
                    "FSMActor can only contain entities that " +
                    "are instances of State.");
        }
        super._addEntity(entity);
    }

    /** Add a transition to this FSMActor. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Transition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof Transition)) {
            throw new IllegalActionException(this, relation,
                    "FSMActor can only contain instances of Transition.");
        }
        super._addRelation(relation);
    }

    /** Return the enabled transition among the given list of transitions
     *  and execute the choice actions contained by the transition.
     *  Throw an exception if there is more than one transition enabled.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or if thrown by any choice action contained
     *   by the enabled transition.
     */
    protected Transition _chooseTransition(List transitionList)
            throws IllegalActionException {
        Transition result = null;
        Iterator trs = transitionList.iterator();
        while (trs.hasNext()) {
            Transition tr = (Transition)trs.next();
            if (!tr.isEnabled()) {
                continue;
            }
            if (result != null) {
                throw new IllegalActionException(this, currentState(),
                        "Multiple enabled transitions.");
            } else {
                result = tr;
            }
        }
        if (result != null) {
            Iterator actions = result.choiceActionList().iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                action.execute();
            }
        }
        _lastChosenTransition = result;
        return result;
    }

    /** Execute all commit actions contained by the transition chosen
     *  during the last call to _chooseTransition(). Change current state
     *  to the destination state of the transition. Reset the refinement
     *  of the destination state if the <i>reset</i> parameter of the
     *  transition is true.
     *  @exception IllegalActionException If any commit action throws it.
     */
    protected void _commitLastChosenTransition()
            throws IllegalActionException {
        if (_lastChosenTransition == null) {
            return;
        }

        Iterator actions = _lastChosenTransition.commitActionList().iterator();
        while (actions.hasNext()) {
            Action action = (Action)actions.next();
            action.execute();
        }
        _currentState = _lastChosenTransition.destinationState();
	BooleanToken resetToken =
	        (BooleanToken)_lastChosenTransition.reset.getToken();
	if (resetToken.booleanValue()) {
	    Actor[] actors = _currentState.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    actors[i].initialize();
                }
            }
	}
        _setCurrentConnectionMap();
    }

    /** Create input variables for the port. The variables are contained
     *  by this actor and can be referenced in the guard and trigger
     *  expressions of transitions.
     *  If the given port is a single port, two variables are created:
     *  one is input status variable with name "<i>portName</i>_isPresent";
     *  the other is input value variable with name "<i>portName</i>". The
     *  input status variable always contains a BooleanToken. When this
     *  actor is fired, the status variable is set to true if the port has
     *  a token, false otherwise. The input value variable always contains
     *  the latest token received from the port.
     *  If the given port is a multiport, a status variable and a value
     *  variable are created for each channel. The status variable is
     *  named "<i>portName</i>_<i>channelIndex</i>_isPresent".
     *  The value variable is named "<i>portName</i>_<i>channelIndex</i>".
     *  If a variable to be created has the same name as an attribute
     *  already contained by this actor, the attribute will be removed
     *  from this actor by setting its container to null.
     *  @param port A port.
     *  @exception IllegalActionException If the port is not contained
     *   by this FSMActor or is not an input port.
     */
    protected void _createInputVariables(TypedIOPort port)
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "that is not input.");
        }
        // If there are input variables already created for the port,
        // remove them.
        if (_inputVariableMap.get(port) != null) {
            _removeInputVariables(port);
        }
        int width = port.getWidth();
        if (width == 0) {
            return;
        }
        Variable[][] pVars = new Variable[width][2];
        boolean addChIndex = (width > 1);
        for (int chIndex = 0; chIndex < width; ++chIndex) {
            String vName = null;
            if (addChIndex) {
                vName = port.getName() + "_" + chIndex + "_isPresent";
            } else {
                vName = port.getName() + "_isPresent";
            }
            Attribute a = getAttribute(vName);
            try {
                if(a != null) {
                    a.setContainer(null);
                }
                pVars[chIndex][0] = new Variable(this, vName);
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error creating input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            }
            if (addChIndex) {
                vName = port.getName() + "_" + chIndex;
            } else {
                vName = port.getName();
            }
            a = getAttribute(vName);
            try {
                if(a != null) {
                    a.setContainer(null);
                }
                pVars[chIndex][1] = new Variable(this, vName);
            } catch (NameDuplicationException ex) {
                throw new InvalidStateException(this,
                        "Error creating input variables for port.\n"
                        + ex.getMessage());
            }
        }
        _inputVariableMap.put(port, pVars);
    }

    /** Return true if the channel of the port is connected to an output
     *  port of the refinement of current state. If the current state
     *  does not have refinement, return false.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @return True if the channel of the port is connected to an output
     *   port of the refinement of current state.
     *  @exception IllegalActionException If the refinement specified for
     *   one of the states is not valid.
     */
    protected boolean _isRefinementOutput(TypedIOPort port, int channel)
            throws IllegalActionException {
        TypedActor[] refinements = _currentState.getRefinement();
        if (refinements == null || refinements.length == 0) {
            return false;
        }
        if (_connectionMapsVersion != workspace().getVersion()) {
            _setCurrentConnectionMap();
        }
        boolean[] flags = (boolean[])_currentConnectionMap.get(port);
        return flags[channel];
    }

    /** Remove the input variables created for the port.
     *  @see #_createInputVariables(TypedIOPort port)
     *  @param port A port.
     */
    protected void _removeInputVariables(TypedIOPort port) {
        Variable[][] pVars = (Variable[][])_inputVariableMap.get(port);
        if (pVars == null) {
            return;
        }
        for (int index = 0; index < pVars.length; ++index) {
            try {
                Variable v = pVars[index][0];
                if (v != null) {
                    v.setContainer(null);
                }
                v = pVars[index][1];
                if (v != null) {
                    v.setContainer(null);
                }
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error removing input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error removing input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            }
        }
    }

    /*  Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        if (_connectionMapsVersion != workspace().getVersion()) {
            _buildConnectionMaps();
        }
        _currentConnectionMap = (Map)_connectionMaps.get(_currentState);
    }

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    protected void _setInputsFromRefinement()
            throws IllegalActionException {
        Iterator inports = inputPortList().iterator();
        while (inports.hasNext()) {
            TypedIOPort p = (TypedIOPort)inports.next();
            int width = p.getWidth();
            for (int channel = 0; channel < width; ++channel) {
                if (_isRefinementOutput(p, channel)) {
                    _setInputVariables(p, channel);
                }
            }
        }
    }

    /** Set the input variables for all ports of this actor.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    protected void _setInputVariables() throws IllegalActionException {
        Iterator inports = inputPortList().iterator();
        while (inports.hasNext()) {
            TypedIOPort p = (TypedIOPort)inports.next();
            int width = p.getWidth();
            for (int channel = 0; channel < width; ++channel) {
                _setInputVariables(p, channel);
            }
        }
    }

    /** Set the input variables for the channel of the port.
     *  @see #_createInputVariables(TypedIOPort port)
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor, or if the port is not an input port, or if the value
     *   variable cannot take the token read from the channel.
     */
    protected void _setInputVariables(TypedIOPort port, int channel)
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot set input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            throw new IllegalActionException(this, port,
                    "Cannot set input variables for port "
                    + "that is not input.");
        }
        if (_debugging) {
            _debug(this.getFullName(), "set input variables for port",
                    port.getName());
        }
        int width = port.getWidth();
        Variable[][] pVars = (Variable[][])_inputVariableMap.get(port);
        if (pVars == null) {
            throw new InternalErrorException(getName() + ": "
                    + "Cannot find input variables for port "
                    + port.getName() + ".\n");
        }
        if (port.isKnown(channel)) {
            boolean t = port.hasToken(channel);
            if (_debugging) {
                _debug(port.getName(), "has token: " + t);
            }
            Token tok = t ? BooleanToken.TRUE : BooleanToken.FALSE;
            pVars[channel][0].setToken(tok);
            // Update the value variable if there is a token in the channel.
            if (t == true) {
                tok = port.get(channel);
                if (_debugging) {
                    _debug(port.getName(), "token value:", tok.toString());
                }
                pVars[channel][1].setToken(tok);
            }
        } else {
            pVars[channel][0].setUnknown(true);
            pVars[channel][1].setUnknown(true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Current state.
    protected State _currentState = null;

    // A map from ports to corresponding input variables.
    protected Map _inputVariableMap = new HashMap();

    // The last chosen transition.
    protected Transition _lastChosenTransition = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Build for each state a map from input ports to boolean flags
     *  indicating whether a channel is connected to an output port
     *  of the refinement of the state.
     *  This method is read-synchronized on the workspace.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    private void _buildConnectionMaps() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (_connectionMaps == null) {
                _connectionMaps = new HashMap();
            } else {
                // Remove any existing maps.
                _connectionMaps.clear();
            }
            // Create a map for each state.
            Iterator states = entityList().iterator();
            State st = null;
            while (states.hasNext()) {
                st = (State)states.next();
                Map stMap = new HashMap();
		TypedActor[] actors = st.getRefinement();
                // Determine the boolean flags for each input port.
                Iterator inports = inputPortList().iterator();
                while (inports.hasNext()) {
                    IOPort inport = (IOPort)inports.next();
                    boolean[] flags = new boolean[inport.getWidth()];
                    if (actors == null || actors.length == 0) {
                        java.util.Arrays.fill(flags, false);
                        stMap.put(inport, flags);
                        continue;
                    }
                    Iterator rels = inport.linkedRelationList().iterator();
                    int chindex = 0;
                    while (rels.hasNext()) {
                        IORelation rel = (IORelation)rels.next();
                        boolean linked = false;
			for (int i = 0; i < actors.length; ++i) {
			    Iterator outports =
                                    actors[i].outputPortList().iterator();
			    while (outports.hasNext()) {
				IOPort outport = (IOPort)outports.next();
				linked = linked | outport.isLinked(rel);
			    }
			}
                        for (int j = 0; j < rel.getWidth(); ++j) {
                            flags[chindex+j] = linked;
                        }
                        chindex += rel.getWidth();
                    }
                    stMap.put(inport, flags);
                }
                _connectionMaps.put(st, stMap);
            }
            _connectionMapsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /*  Create input variables for each input port.
     */
    private void _createInputVariables() {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inport = (TypedIOPort)inputPorts.next();
            try {
                _createInputVariables(inport);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error creating input variables for port "
                        + inport.getName() + ".\n"
                        + ex.getMessage());
            }
        }
    }

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inport = (IOPort)inputPorts.next();
            inport.createReceivers();
        }
    }

    /*  Set the current state to the initial state. The name of the initial
     *  state is specified by the <i>initialStateName</i> attribute. An
     *  exception is thrown if this actor does not contain a state with
     *  the specified name.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    private void _gotoInitialState() throws IllegalActionException {
        _currentState = getInitialState();
        _setCurrentConnectionMap();
    }

    /*  Initialize the actor.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _init() {
        try {
            initialStateName = new StringAttribute(this, "initialStateName");
            new Attribute(this, "_nonStrictMarker");
        } catch (KernelException ex) {
            // This should never happen.
            throw new InternalErrorException("Constructor error "
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;

    // Stores for each state a map from input ports to boolean flags
    // indicating whether a channel is connected to an output port
    // of the refinement of the state.
    private Map _connectionMaps = null;

    // Version of the connection maps.
    private long _connectionMapsVersion = -1;

    // The map from input ports to boolean flags indicating whether a
    // channel is connected to an output port of the refinement of the
    // current state.
    private Map _currentConnectionMap = null;

    // Cached reference to the initial state.
    private State _initialState = null;

    // Version of the reference to the initial state.
    private long _initialStateVersion = -1;

}
