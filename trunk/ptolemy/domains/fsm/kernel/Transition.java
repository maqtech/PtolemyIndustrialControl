/* A transition in an FSMActor.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Yellow (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.ComponentRelation;

import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Transition
/**
A Transition has a source state and a destination state. A transition has
a guard expression and a trigger expression. Both expressions should evaluate
to a boolean value. The trigger of a transition must be true whenever the
guard is true. A transition is enabled and can be taken when its guard is true.
A transition is triggered and must be taken when its trigger is true.
<p>
A transition can contain actions.  The simplest way to specify actions
is to the set the value of the <i>actions</i> parameter.
The value of this parameter is a string of the form:
<pre>
     <i>command</i>; <i>command</i>; ...
</pre>
where each <i>command</i> has the form:
<pre>
     <i>destination</i> = <i>expression</i>
</pre>
where <i>destination</i> is either
<pre>
     <i>portName</i>
</pre>
or
<pre>
     <i>portName</i>(<i>channelNumber</i>)
</pre>
or
<pre>
     <i>variableName</i>
</pre>
Here, <i>portName</i> is the name of a port of the FSM actor,
If no <i>channelNumber</i> is given, then the value
is broadcast to all channels of the port.
Also, <i>variableName</i> is either a variable or parameter of
the FSM actor, or a variable or parameter of a refinement state.
To give a variable of a refinement state, use a dotted name,
as follows:
<pre>
     <i>refinementStateName</i>.<i>variableName</i>
</pre>
If destination name is given where there is both a port and a variable
with that name, then the port will be used.
<p>
The <i>expression</i> is a string giving an expression in the usual
Ptolemy II expression language.  The expression may include references
to variables and parameters contained by the FSM actor.
<p>
The <i>actions</i> parameter is not the only way to specify actions.
In fact, you can add action attributes that are instances of any
class that extends the abstract base class Action.
(Use the Add button in the Edit Parameters dialog).
The <i>actions</i> parameter is an instance of CommitActionsAttribute,
which implements a particular kind of action.
<p>
An action is either a ChoiceAction
or a CommitAction. The <i>actions</i> parameter is a CommitAction.
A commit action is executed when the transition is taken to change
the state of the FSM, in the postfire() method of FSMActor.
A choice action, by contrast, is executed in the fire() method
of the FSMActor when the transition is chosen, but not yet taken.
The difference is subtle, and for most domains, irrelevant.
A few domains, however, such as CT, which have fixed point semantics,
where the fire() method may be invoked several times before the
transisition is taken (committed). For such domains, it is useful
to have actions that implement the ChoiceAction interface.
Such actions participate in the search for a fixed point, but
do not change the state of the FSM.  The class OutputActionsAttribute
implements this interface.
<p>
A transition can be preemptive or non-preemptive. When a preemptive transition
is chosen, the refinement of its source state is not fired. A non-preemptive
transition is only chosen after the refinement of its source State is fired.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@see State
@see Action
@see ChoiceAction
@see CommitAction
@see CommitActionsAttribute
@see FSMActor
@see OutputActionsAttribute
*/
public class Transition extends ComponentRelation {

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        guardExpression = new StringAttribute(this, "guardExpression");
        preemptive = new Parameter(this, "preemptive");
        preemptive.setTypeEquals(BaseType.BOOLEAN);
        preemptive.setToken(BooleanToken.FALSE);
        triggerExpression = new StringAttribute(this, "triggerExpression");
        _guard = new Variable(this, "_guard");
        _guard.setTypeEquals(BaseType.BOOLEAN);
        _trigger = new Variable(this, "_trigger");
        _trigger.setTypeEquals(BaseType.BOOLEAN);

        actions = new CommitActionsAttribute(this, "actions");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The action commands to be taken when the transition is taken.
     */
    public CommitActionsAttribute actions;

    /** Attribute specifying the guard expression.
     */
    public StringAttribute guardExpression = null;

    /** Parameter specifying whether this transition is preemptive.
     */
    public Parameter preemptive = null;

    /** Attribute specifying the trigger expression.
     */
    public StringAttribute triggerExpression = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>preemptive</i> parameter, evaluate the parameter. If the
     *  parameter is given an expression that does not evaluate to a
     *  boolean value, throw an exception; otherwise increment the
     *  version number of the workspace.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method, or the changed attribute is the
     *   <i>preemptive</i> parameter and is given an expression that
     *   does not evaluate to a boolean value.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == preemptive) {
            // evaluate the parameter to make sure it is given a valid
            // expression
            preemptive.getToken();
            workspace().incrVersion();
        }
        if (attribute == guardExpression) {
            String expr = guardExpression.getExpression();
            _guard.setExpression(expr);
        }
        if (attribute == triggerExpression) {
            String expr = triggerExpression.getExpression();
            _trigger.setExpression(expr);
        }
    }

    /** Return the list of choice actions contained by this transition.
     *  @return The list of choice actions contained by this transition.
     */
    public List choiceActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _choiceActionList;
    }

    /** Clone the transition into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer to
     *  the attributes of the new transition.
     *  @param workspace The workspace for the new transition.
     *  @return A new transition.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Transition newObject = (Transition)super.clone(workspace);
        newObject.guardExpression =
            (StringAttribute)newObject.getAttribute("guardExpression");
        newObject.preemptive = (Parameter)newObject.getAttribute("preemptive");
        newObject.triggerExpression =
            (StringAttribute)newObject.getAttribute("triggerExpression");
        newObject._guard = (Variable)newObject.getAttribute("_guard");
        newObject._trigger = (Variable)newObject.getAttribute("_trigger");
        newObject._actionListsVersion = -1;
        newObject._choiceActionList = new LinkedList();
        newObject._commitActionList = new LinkedList();
        newObject._stateVersion = -1;
        return newObject;
    }

    /** Return the list of commit actions contained by this transition.
     *  @return The list of commit actions contained by this transition.
     */
    public List commitActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _commitActionList;
    }

    /** Return the destination state of this transition.
     *  @return The destination state of this transition.
     */
    public State destinationState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _destinationState;
    }

    /** Return the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @return The guard expression.
     */
    public String getGuardExpression() {
        return _guard.getExpression();
    }

    /** Return the trigger expression. The trigger expression should evaluate
     *  to a boolean value.
     *  @return The trigger expression.
     */
    public String getTriggerExpression() {
        return _trigger.getExpression();
    }

    /** Return true if the transition is enabled, that is the guard is true.
     *  @return True if the transition is enabled.
     *  @exception IllegalActionException If thrown when evaluating the guard.
     */
    public boolean isEnabled() throws IllegalActionException {
        Token tok = _guard.getToken();
        return ((BooleanToken)tok).booleanValue();
    }

    /** Return true if this transition is preemptive. Whether this transition
     *  is preemptive is specified by the <i>preemptive</i> parameter.
     *  @return True if this transition is preemptive.
     */
    public boolean isPreemptive() {
        try {
            return ((BooleanToken)preemptive.getToken()).booleanValue();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(preemptive.getFullName()
                    + ": The parameter does not have a valid value, \""
                    + preemptive.getExpression() + "\".");
        }
    }

    /** Return true if the transition is triggered.
     *  @return True if the transition is triggered.
     *  @exception IllegalActionException If thrown when evaluating the
     *   trigger, or the trigger is true but the guard is false.
     */
    public boolean isTriggered() throws IllegalActionException {
        Token tok = _trigger.getToken();
        boolean result = ((BooleanToken)tok).booleanValue();
        tok = _guard.getToken();
        boolean g = ((BooleanToken)tok).booleanValue();
        if (result == true && g == false) {
            throw new IllegalActionException(this, "The trigger: "
                    + getTriggerExpression() + " is true but the guard: "
                    + getGuardExpression() + " is false.");
        }
        return result;
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the transition from its container.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the transition would result
     *   in a recursive containment structure, or if
     *   this transition and container are not in the same workspace, or
     *   if the argument is not a FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an relation with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof FSMActor) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "Transition can only be contained by instances of " +
                    "FSMActor.");
        }
        super.setContainer(container);
    }

    /** Set the guard expression. The guard expression should evaluate
     *  to a boolean value.
     *  @param expression The guard expression.
     */
    public void setGuardExpression(String expression) {
        try {
            guardExpression.setExpression(expression);
            guardExpression.validate();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Error in setting the "
                    + "guard expression of a transition.");
        }
    }

    /** Set the trigger expression. The trigger expression should evaluate
     *  to a boolean value.
     *  @param expression The trigger expression.
     */
    public void setTriggerExpression(String expression) {
        try {
            triggerExpression.setExpression(expression);
            triggerExpression.validate();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Error in setting the "
                    + "trigger expression of a transition.");
        }
    }

    /** Return the source state of this transition.
     *  @return The source state of this transition.
     */
    public State sourceState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _sourceState;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an IllegalActionException if the port cannot be linked
     *  to this transition. A transition has a source state and a
     *  destination state. A transition is only linked to the outgoing
     *  port of its source state and the incoming port of its destination
     *  state.
     *  @exception IllegalActionException If the port cannot be linked
     *   to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);
        if (!(port.getContainer() instanceof State)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only connect to instances of State.");
        }
        State st = (State)port.getContainer();
        if (port != st.incomingPort && port != st.outgoingPort) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only be linked to incoming or outgoing "
                    + "port of State.");
        }
        if (numLinks() == 0) {
            return;
        }
        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "Transition can only connect two States.");
        }
        Iterator ports = linkedPortList().iterator();
        Port pt = (Port)ports.next();
        State s = (State)pt.getContainer();
        if ((pt == s.incomingPort && port == st.incomingPort) ||
                (pt == s.outgoingPort && port == st.outgoingPort)) {
            throw new IllegalActionException(this,
                    "Transition can only have one source and one destination.");
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check the states connected by this transition, cache the result.
    // This method is read-synchronized on the workspace.
    private void _checkConnectedStates() {
        try {
            workspace().getReadAccess();
            Iterator ports = linkedPortList().iterator();
            _sourceState = null;
            _destinationState = null;
            while (ports.hasNext()) {
                Port p = (Port)ports.next();
                State s = (State)p.getContainer();
                if (p == s.incomingPort) {
                    _destinationState = s;
                } else {
                    _sourceState = s;
                }
            }
            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    // Update the cached lists of actions.
    // This method is read-synchronized on the workspace.
    private void _updateActionLists() {
        try {
            workspace().getReadAccess();
            _choiceActionList.clear();
            _commitActionList.clear();
            Iterator actions = attributeList(Action.class).iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                if (action instanceof ChoiceAction) {
                    _choiceActionList.add(action);
                }
                if (action instanceof CommitAction) {
                    _commitActionList.add(action);
                }
            }
            _actionListsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Version of cached lists of actions.
    private long _actionListsVersion = -1;

    // Cached list of choice actions contained by this transition.
    private List _choiceActionList = new LinkedList();

    // Cached list of commit actions contained by this Transition.
    private List _commitActionList = new LinkedList();

    // Cached destination state of this transition.
    private State _destinationState = null;

    // Variable for evaluating guard.
    private Variable _guard = null;

    // Set to true when the transition is preemptive.
    private boolean _preemptive = false;

    // Cached source state of this transition.
    private State _sourceState = null;

    // Version of cached source/destination state.
    private long _stateVersion = -1;

    // Variable for evaluating trigger.
    private Variable _trigger = null;

}
