/* An Interface Automaton.

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
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomaton
/**
This class models an Interface Automaton. Interface automata is an automata
model defined by Luca de Alfaro in the paper "Interface Automata". 
An InterfaceAutomaton contains a set of states and
InterfaceAutomatonTransitions. There are three kinds transitions:
input transition, output transition, and internal transitions.
The input and output transitions correspond to input and output ports,
respectively. The internal transition correspond to a parameter in this
InterfaceAutomaton. The parameter is added automatically when the internal
transition is added.
<p>
When an InterfaceAutomaton is fired, the outgoing transitions of the current
state are examined. An IllegalActionException is thrown if there is more than
one enabled transition. If there is exactly one enabled transition then it is
taken.
<p>
An InterfaceAutomaton enters its initial state during initialization. The
name of the initial state is specified by the <i>initialStateName</i> string
attribute.
<p>

@author Yuhong Xiong, Xiaojun Liu and Edward A. Lee
@version $Id$
@see State
@see InterfaceAutomatonTransition
*/

// FIXME: Are interface automata that are fired required to be deterministic?
// or just randomly choose a transition.

public class InterfaceAutomaton extends FSMActor {

    /** Construct an InterfaceAutomaton in the default workspace with an
     *  empty string as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public InterfaceAutomaton() {
        super();
    }

    /** Construct an InterfaceAutomaton in the specified workspace with an
     *  empty string as its name. The name can be changed later with
     *  setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public InterfaceAutomaton(Workspace workspace) {
        super(workspace);
    }

    /** Create an InterfaceAutomaton in the specified container with the
     *  specified name. The name must be unique within the container or an
     *  exception is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this automaton within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public InterfaceAutomaton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add instances of TypedIOPort that correspond to input and output
     *  transitions, if these port do not exist. If the ports
     *  corresponding to some transitions already exist, do nothing with
     *  respect to those transitions.
     */
    public void addPorts() {
        try {
            Iterator iterator = relationList().iterator();
            while (iterator.hasNext()) {
                InterfaceAutomatonTransition transition =
                    (InterfaceAutomatonTransition)iterator.next();
                String label = transition.getLabel();
                String name = label.substring(0, label.length()-1);
                if (label.endsWith("?")) {
                    TypedIOPort port = (TypedIOPort)getPort(name);
                    if (port == null) {
                        port = new TypedIOPort(this, name);
                        port.setInput(true);
                    }
                } else if (label.endsWith("!")) {
                    TypedIOPort port = (TypedIOPort)getPort(name);
                    if (port == null) {
                        port = new TypedIOPort(this, name);
                        port.setOutput(true);
                    }
                }
            }
        } catch (IllegalActionException exception) {
            // should not happen since TypedIOPort can be added to
            // InterfaceAutomaton
            throw new InternalErrorException("InterfaceAutomaton.addPorts: "
                + "Cannot add port. " + exception.getMessage());
        } catch (NameDuplicationException exception) {
            // should not happen since new port will not be created if there
            // is already a port with the same name.
            throw new InternalErrorException("InterfaceAutomaton.addPorts: "
                + "Cannot add port. " + exception.getMessage());
            }
    }

    /** Combine each chain of internal transitions into one transition.
     *  This method iterates through all the states. If a state has just
     *  one incoming and one outgoing internal transitions, and it is
     *  not the initial state, it is removed and the two transitions are
     *  combined into one. The label on the new transition is formed by 
     *  <incomingLabel><NAME_CONNECTOR><outgoingLabel>.
     */
    public void combineInternalTransitions() {
        State initialState = (State)getEntity(initialStateName.getExpression());
        try {
            Iterator states = entityList().iterator();
            while (states.hasNext()) {
                State state = (State)states.next();

                ComponentPort inPort = state.incomingPort;
                List transitionList = inPort.linkedRelationList();
                InterfaceAutomatonTransition incomingTransition = null;
                if (transitionList.size() != 1) {
		    continue;
		}

                // just one incoming transition, check if it's internal
                incomingTransition = 
                    (InterfaceAutomatonTransition)transitionList.get(0);
                if (incomingTransition.getType() != 
                    InterfaceAutomatonTransition.INTERNAL_TRANSITION) {
                    continue;
		}

                ComponentPort outPort = state.outgoingPort;
                transitionList = outPort.linkedRelationList();
                InterfaceAutomatonTransition outgoingTransition = null;
                if (transitionList.size() != 1) {
		    continue;
		}

                // just one outgoing transition, check if it's internal
                outgoingTransition = 
                    (InterfaceAutomatonTransition)transitionList.get(0);
                if (outgoingTransition.getType() != 
                    InterfaceAutomatonTransition.INTERNAL_TRANSITION) {
                    continue;
		}

                // only has one incoming and one outgoing internal transition,
                // check if this state is initial.
                if (state == initialState) {
                    continue;
                }

                // combine transitions
                State sourceState = incomingTransition.sourceState();
                State destinationState = outgoingTransition.destinationState();
                String incomingLabel = incomingTransition.getLabel();
                String incomingName = incomingLabel.substring(0,
                                              incomingLabel.length() -1);
                String outgoingLabel = outgoingTransition.getLabel();
                String newLabel = incomingName + NAME_CONNECTOR + outgoingLabel;

                String relationNamePrefix = incomingTransition.getName() + 
                        NAME_CONNECTOR + outgoingTransition.getName();

                // remove this state and add new transition
                _removeStateAndTransitions(state);
                _addTransition(this, relationNamePrefix,
                               sourceState, destinationState, newLabel);
            }
        } catch (IllegalActionException exception) {
            // should not happen
            throw new InternalErrorException(
                "InterfaceAutomaton.combineInternalTransitions: "
                + exception.getMessage());
        } catch (NameDuplicationException exception) {
            // should not happen since _addTransition() uses unique name.
            // Maybe that method should not throw this exception.
            throw new InternalErrorException(
                "InterfaceAutomaton.combineInternalTransitions: "
                + exception.getMessage());
        }
    }

    /** Return a new InterfaceAutomaton that is the composition of the
     *  specified InterfaceAutomaton and this one.
     *  @param automaton An InterfaceAutomaton to compose with this one.
     *  @return An InterfaceAutomaton that is the composition.
     *  @exception IllegalActionException If this automaton is not composable
     *   with the argument.
     */
    public InterfaceAutomaton compose(InterfaceAutomaton automaton)
                throws IllegalActionException {

        this._check();
        automaton._check();

        // check composability
        _checkComposability(automaton);

        // compute the input, output, and internal transitions of the
        // composition
        _computeTransitionNamesInComposition(automaton);

        // compute the product automaton
        InterfaceAutomaton composition = _computeProduct(automaton);

        // prune illegal states
        _pruneIllegalStates();

        // remove states unreacheable from the initial state.
        composition._removeUnreacheableStates();

        // Create ports for the composition.  Internal transition parameters 
        // were created automatically when the transition labels were set.
        _createPorts(composition);

        return composition;
    }

    /** Choose the enabled transition among the outgoing transitions of
     *  the current state. Throw an exception if there is more than one
     *  transition enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Return a high-level description of this automaton. The returned String
     *  has the format:
     *  <pre>
     *  (full name of automaton):
     *    (number of states) states
     *    (number of transitions) transitions
     *    (number of input names) input names
     *    (number of output names) output names
     *    (number of internal transition names) internal transition names
     *    Input Names:
     *      (list of input names)
     *    Output Names:
     *      (list of output names)
     *    Internal Transition Names:
     *      (list of internal transition names)
     *  </pre>
     */
    public String getInfo() {
        String info = getFullName() + "\n";

        Set inputNames = inputNameSet();
        Set outputNames = outputNameSet();
        Set internalNames = internalTransitionNameSet();

        info += "  " + entityList().size() + " states\n";
        info += "  " + relationList().size() + " transitions\n";
        info += "  " + inputNames.size() + " input names\n";
        info += "  " + outputNames.size() + " output names\n";
        info += "  " + internalNames.size() + " internal transition names\n";

        info += "  Input Names:\n";
        Iterator iterator = inputNames.iterator();
        while (iterator.hasNext()) {
            info += "    " + iterator.next().toString() + "\n";
        }

        info += "  Output Names:\n";
        iterator = outputNames.iterator();
        while (iterator.hasNext()) {
            info += "    " + iterator.next().toString() + "\n";
        }

        info += "  Internal Transition Names:\n";
        iterator = internalNames.iterator();
        while (iterator.hasNext()) {
            info += "    " + iterator.next().toString() + "\n";
        }

        return info;
    }

    /** Return the names of the input ports as a Set.
     *  @return A Set containing all the input port names.
     */
    public Set inputNameSet() {
        Set set = new HashSet();
        Iterator iterator = inputPortList().iterator();
        while (iterator.hasNext()) {
            IOPort port = (IOPort)iterator.next();
            set.add(port.getName());
        }
        return set;
    }

    /** Return the names of the internal transitions as a Set.
     *  @return A Set containing all the internal transition names.
     */
    // This method differs from inputNameSet() and outputNameSet() in that
    // those methods return the names of the input or output ports, but this
    // one does not get the names from the parameters corresponding to the
    // internal transitions. As a result, all the returned names have one or
    // more corresponding internal transition instances. The is because
    // (1) Unlike the relation between input/output transitions and ports,
    // where some ports may not have corresponding instances of transition,
    // it does not make sense to have any "internal transition parameter"
    // that does not have transition instances; (2) there is no way to tell
    // which parameter is for internal transition, and which is for other
    // purpose.
    public Set internalTransitionNameSet() {
        Set set = new HashSet();
        Iterator iterator = relationList().iterator();
        while (iterator.hasNext()) {
            InterfaceAutomatonTransition transition =
                (InterfaceAutomatonTransition)iterator.next();
            String label = transition.getLabel();
            if (label.endsWith(";")) {
                String name = label.substring(0, label.length()-1);
                set.add(name);
            }
        }
        return set;
    }

    /** Create a new instance of InterfaceAutomatonTransition with the
     *  specified name in this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return An InterfaceAutomatonTransition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            InterfaceAutomatonTransition transition =
                    new InterfaceAutomatonTransition(this, name);
            return transition;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return true if this automaton is empty; false otherwise.
     *  @return true if this automaton is empty; false otherwise.
     */
    public boolean isEmpty() {
        List states = entityList();
        return states.size() == 0;
    }

    /** Return the names of the output ports as a Set.
     *  @return A Set containing all the output port names.
     */
    public Set outputNameSet() {
        Set set = new HashSet();
        Iterator iterator = outputPortList().iterator();
        while (iterator.hasNext()) {
            IOPort port = (IOPort)iterator.next();
            set.add(port.getName());
        }
        return set;
    }

    /** Rename the labels on some transitions. The argument is a Map
     *  specifying which transition labels should be renamed. The keys of the
     *  Map are the old label names, and the values are the new label names.
     *  Neither the keys nor the values should include the ending character
     *  "?", "!", or ";" that indicate the type of the transtion. And this
     *  method does not change the type. 
     *  <p>
     *  For input and output transitions, this method also renames the ports
     *  associated with the renamed transitions, if these ports are created
     *  already. This is done regardless of whether there are instances of
     *  transitions that correspond to the ports. For internal transitions,
     *  this method renames the parameter associated with the renamed
     *  transition.
     *  @param nameMap A map between the old and the new label names.
     *  @exception IllegalActionException If the new name is not legal.
     *  @exception NameDuplicationException If the requested name change will
     *   cause name collision.
     */
    public void renameTransitionLabels(Map nameMap)
            throws IllegalActionException, NameDuplicationException {
        Iterator iterator = relationList().iterator();
        while (iterator.hasNext()) {
            InterfaceAutomatonTransition transition =
                (InterfaceAutomatonTransition)iterator.next();
            String oldLabel = transition.getLabel();
            int length = oldLabel.length();
            String oldLabelName = oldLabel.substring(0, length-1);
            String newLabelName = (String)nameMap.get(oldLabelName);
            if (newLabelName != null) {
                String ending = oldLabel.substring(length-1, length);
                String newLabel = newLabelName + ending;
                transition.label.setExpression(newLabel);

                // change port or parameter name
                if (ending.equals("?") || ending.equals("!")) {
                    TypedIOPort port = (TypedIOPort)getPort(oldLabelName);
                    if (port != null) {
                        port.setName(newLabelName);
                    }
                } else if (ending.equals(";")) {
                    Parameter param = (Parameter)getAttribute(oldLabelName);
                    param.setName(newLabelName);
                } else {
                    throw new InternalErrorException("Transition label "
                        + "does not end with ?, !, or ;");
                }
            }
        }

        // for ports that do not have corresponding transitions, rename the
        // ports
        iterator = portList().iterator();
        while (iterator.hasNext()) {
            TypedIOPort port = (TypedIOPort)iterator.next();
            String oldName = port.getName();
            String newName = (String)nameMap.get(oldName);
            if (newName != null) {
                port.setName(newName);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an InterfaceAutomatonTransition to this InterfaceAutomaton.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the transition instead. This method does not set the
     *  container of the transition to refer to this container. This method
     *  is <i>not</i> synchronized on the workspace, so the caller should be.
     *
     *  @param relation The InterfaceAutomatonTransition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof InterfaceAutomatonTransition)) {
            throw new IllegalActionException(this, relation,
                    "InterfaceAutomaton can only contain instances of "
                    + "InterfaceAutomatonTransition.");
        }
        super._addRelation(relation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add a state to the product automaton and the frontier, if the
    // state is not added already. Return the state added or the one
    // already existed. This method is called from _computeProduct.
    private State _addState(InterfaceAutomaton product, State stateInThis,
                            State stateInArgument, HashMap frontier)
            throws IllegalActionException, NameDuplicationException {
        String name = stateInThis.getName() + NAME_CONNECTOR
                      + stateInArgument.getName();
        State state = (State)product.getEntity(name);
        if (state == null) {
            // not in product
            state = new State(product, name);
            Triple triple = new Triple(state, stateInThis, stateInArgument);
            frontier.put(name, triple);
        }
        return state;
    }

    // Add a transition to between two states in an automaton
    private void _addTransition(InterfaceAutomaton automaton,
                                String relationNamePrefix,
                                State sourceState, State destinationState,
                                String label)
                throws IllegalActionException, NameDuplicationException {
        String name = automaton.uniqueName(relationNamePrefix);
        InterfaceAutomatonTransition transition =
                new InterfaceAutomatonTransition(automaton, name);
        sourceState.outgoingPort.link(transition);
        destinationState.incomingPort.link(transition);
        transition.label.setExpression(label);
    }

    // Perform sanity check on this automaton. In particular, this method
    // checks: (1) all input transitions have a corresponding input port,
    // all output transitions have a corresponding output port, and all
    // internal transitions have a corresponding parameter; (2) The names
    // for input, output, and internal transitions do not overlap.
    // If one of the above check fails, an exception is thrown;
    // otherwise, this method just returns.
    //
    private void _check() throws IllegalActionException {
        // check all transitions have corresponding ports or parameter.
        Iterator iterator = relationList().iterator();
        while (iterator.hasNext()) {
            InterfaceAutomatonTransition transition =
                (InterfaceAutomatonTransition)iterator.next();
            String label = transition.getLabel();
            String name = label.substring(0, label.length()-1);
            if (label.endsWith("?")) {
                IOPort port = (IOPort)getPort(name);
                if (port == null || port.isInput() == false) {
                    throw new IllegalActionException(
                        "InterfaceAutomaton._check: " + getFullName()
                        + ": The transition " + label
                        + " does not have a corresponding input port.");
                }
            } else if (label.endsWith("!")) {
                IOPort port = (IOPort)getPort(name);
                if (port == null || port.isOutput() == false) {
                    throw new IllegalActionException(
                        "InterfaceAutomaton._check: " + getFullName()
                        + ": The transition " + label
                        + " does not have a corresponding output port.");
                }
            } else if (label.endsWith(";")) {
                Attribute attribute = getAttribute(name);
                if (attribute == null || !(attribute instanceof Parameter)) {
                    throw new IllegalActionException(
                        "InterfaceAutomaton._check: " + getFullName()
                        + ": The transition " + label
                        + " does not have a corresponding Parameter.");
                }
            } else {
                throw new InternalErrorException(
                    "InterfaceAutomaton._check: The label " + label
                        + " does not end with ?, !, or ;.");
            }
        }

        // check transition names do not overlap
        Set inputNames = inputNameSet();
        Set outputNames = outputNameSet();
        inputNames.retainAll(outputNames);
        if ( !inputNames.isEmpty()) {
            throw new IllegalActionException("InterfaceAutomaton._check: "
                + getFullName() + ": The names for input and output "
                + "transitions overlap.");
        }

        inputNames = inputNameSet();
        Set internalNames = internalTransitionNameSet();
        inputNames.retainAll(internalNames);
        if ( !inputNames.isEmpty()) {
            throw new IllegalActionException("InterfaceAutomaton._check: "
                + getFullName() + ": The names for input and internal "
                + "transitions overlap.");
        }

        outputNames.retainAll(internalNames);
        if ( !outputNames.isEmpty()) {
            throw new IllegalActionException("InterfaceAutomaton._check: "
                + getFullName() + ": The names for output and internal "
                + "transitions overlap.");
        }
    }

    // Throw an exception if this automaton and the specified one is not
    // composable.
    private void _checkComposability(InterfaceAutomaton automaton)
            throws IllegalActionException {
        String message = "InterfaceAutomaton._checkComposability: "
                + this.getFullName() + " is not composable with "
                + automaton.getFullName() + " because ";

        // check the internal transitions of one do not overlap with the
        // transitions of the other
        Set thisInternals = this.internalTransitionNameSet();

        Set thatInputs = automaton.inputNameSet();
        Set thatOutputs = automaton.outputNameSet();
        Set thatInternals = automaton.internalTransitionNameSet();

        thatInputs.retainAll(thisInternals);
        thatOutputs.retainAll(thisInternals);
        thatInternals.retainAll(thisInternals);

        if ( !thatInputs.isEmpty() || 
             !thatOutputs.isEmpty() ||
             !thatInternals.isEmpty()) {
            throw new IllegalActionException(message + "the internal "
                + "transitions of the former overlaps with the transitions "
                + "of the latter.");
        }

        thatInternals = automaton.internalTransitionNameSet();

        Set thisInputs = this.inputNameSet();
        Set thisOutputs = this.outputNameSet();
        thisInternals = this.internalTransitionNameSet();

        thisInputs.retainAll(thatInternals);
        thisOutputs.retainAll(thatInternals);
        thisInternals.retainAll(thatInternals);
        if ( !thisInputs.isEmpty() ||
             !thisOutputs.isEmpty() ||
             !thisInternals.isEmpty()) {
            throw new IllegalActionException(message + "the internal "
                + "transitions of the latter overlaps with the transitions "
                + "of the former.");
        }

        // check the input transitions do not overlap
        thisInputs = this.inputNameSet();
        thatInputs = automaton.inputNameSet();
        thisInputs.retainAll(thatInputs);
        if ( !thisInputs.isEmpty()) {
            throw new IllegalActionException(message + "the input "
                + "transitions of the two overlap.");
        }

        // check the output transitions do not overlap
        thisOutputs = this.outputNameSet();
        thatOutputs = automaton.outputNameSet();
        thisOutputs.retainAll(thatOutputs);
        if ( !thisOutputs.isEmpty()) {
            throw new IllegalActionException(message + "the output "
                + "transitions of the two overlap.");
        }
    }

    // Compute the product of this autmaton and the argument. Also store
    // the illegal states found in the Set _illegalStates.
    //
    // Use frontier exploration. The frontier is represented by a HashMap
    // frontier. The key is the name of the state in the product, the value
    // is a Triple: stateInProduct, stateInThis, stateInArgument. The keys
    // are used to easily check if a product state is in the frontier.
    //
    // init: product = (this.initialState x automaton.initialSate)
    //       frontier = (this.initialState x automaton.initialSate, 
    //                   this.initialState, automaton.initialState)
    // iterate: pick (remove) a state p x q from frontier;
    //          pick a step pTr from p
    //            (case 1) T is input for p:
    //              (1A) T is input of product: add T to product
    //              (1B) T is shared:
    //                (1Ba) q has T ouput: add T to product as internal
    //                      transition
    //                (1Bb) q does not have T output: transition cannot happen
    //                      in product. ignore
    //            (case 2) T is output for p:
    //              (2A) T is output of product: add T to product
    //              (2B) T is shared:
    //                (2Ba) q has T input: add T to product as internal
    //                      transition
    //                (2Bb) q does not have T input: mark p x q as illegal.
    //                      stop exploring from p x q.
    //            (case 3) T is internal for p: add T to product
    //
    //          The cases for a transition from q is almost symmetric,
    //          but be careful not to add shared transition twice. In the code
    //          below, shared transitions are added in the code that explores
    //          the state with the input transition.
    //
    //          (after exploring all transitions from p and q), remove p x q
    //          from frontier.
    //
    //          end when frontier is empty
    //
    private InterfaceAutomaton _computeProduct(InterfaceAutomaton automaton)
            throws IllegalActionException {
        try {
            // init
            _illegalStates = new HashSet();
            InterfaceAutomaton product = new InterfaceAutomaton();
            product.setName(this.getName() + NAME_CONNECTOR
                            + automaton.getName());
            HashMap frontier = new HashMap();

            // create initial state
            State stateInThis = this.getInitialState();
            State stateInArgument = automaton.getInitialState();
            String name = stateInThis.getName() + NAME_CONNECTOR
                              + stateInArgument.getName();
            State stateInProduct = new State(product, name);
            product.initialStateName.setExpression(name);

            Triple triple = new Triple(stateInProduct, stateInThis,
                                       stateInArgument);
            frontier.put(name, triple);

            // iterate
            while ( !frontier.isEmpty()) {
                // pick a state from frontier. It seems that there isn't an
                // easy way to pick an arbitrary entry from a HashMap, except
                // through Iterator
                Iterator iterator = frontier.keySet().iterator();
                name = (String)iterator.next();
                triple = (Triple)frontier.remove(name);
                stateInProduct = triple._stateInProduct;
                stateInThis = triple._stateInThis;
                stateInArgument = triple._stateInArgument;

                boolean isStateInProductIllegal = false;

                // extend frontier from state in this automaton
                ComponentPort outPort = stateInThis.outgoingPort;
                Iterator transitions = outPort.linkedRelationList().iterator();
                while (transitions.hasNext() && !isStateInProductIllegal) {
                    InterfaceAutomatonTransition transition =
                            (InterfaceAutomatonTransition)transitions.next();

                    State destinationInThis = transition.destinationState();

                    // get transitionLabel, transitionName and relation name
                    // for later use
                    String transitionLabel = transition.getLabel();
                    // remove ending "?"
                    String transitionName = transitionLabel.substring(0,
                                             transitionLabel.length()-1);

                    // switch depending on type of transition
                    int transitionType = transition.getType();
                    if (transitionType ==
                            InterfaceAutomatonTransition.INPUT_TRANSITION) {
                        // case 1
                        if (_inputNames.contains(transitionName)) {
                            // case 1A. Add transition to product as input
                            // transition
                            State destinationInProduct = _addState(product,
                                destinationInThis, stateInArgument, frontier);
                            _addTransition(product,
                              this.getName(),
                              stateInProduct, destinationInProduct,
                              transitionLabel);
                        } else {
                            // case 1B. transition is shared in product
                            String outName = transitionName + "!";
                            State destinationInArgument = _getDestinationState(
                                stateInArgument, outName);
                            if (destinationInArgument != null) {
                                // case 1Ba. q has T output. Add T to product
                                // as internal transition
                                State destinationInProduct = _addState(product,
                                    destinationInThis, destinationInArgument,
                                    frontier);
                                _addTransition(product,
                                  this.getName() + NAME_CONNECTOR
                                      + automaton.getName(),
                                  stateInProduct, destinationInProduct,
                                  transitionName + ";");
                            } else {
                                // case 1Bb. q does not have T output.
                                // Transition cannot happen, ignore.
                            }
                        }
                    }
                    else if (transitionType ==
                            InterfaceAutomatonTransition.OUTPUT_TRANSITION) {
                        // case 2. T is output for p.
                        if (_outputNames.contains(transitionName)) {
                            // case 2A. T is output of product. Add T to
                            // product as output transition
                            State destinationInProduct = _addState(product,
                                destinationInThis, stateInArgument, frontier);
                            _addTransition(product,
                              this.getName(),
                              stateInProduct, destinationInProduct,
                              transitionLabel);
                        } else {
                            // case 2B. transition is shared in product
                            String inName = transitionName + "?";
                            State destinationInArgument = _getDestinationState(
                                stateInArgument, inName);
                            if (destinationInArgument != null) {
                                // case 2Ba. q has T input. Need to add T
                                // to product as internal transition. However,
                                // to avoid adding this transition twice,
                                // leave the code that explores state q to
                                // add the transition
                            } else {
                                // case 2Bb. q does not have T input.
                                // stateInProduct is illegal
                                _illegalStates.add(stateInProduct);
                                isStateInProductIllegal = true;
                            }
                        }
                    } else if (transitionType ==
                            InterfaceAutomatonTransition.INTERNAL_TRANSITION) {
                        // case 3. T is internal for p. Add T to product
                        State destinationInProduct = _addState(product,
                                destinationInThis, stateInArgument, frontier);
                        _addTransition(product, this.getName(),
                          stateInProduct, destinationInProduct,
                          transitionLabel);
                    } else {
                        throw new InternalErrorException(
                            "InterfaceAutomaton._computeProduct: unrecognized "
                            + "transition type.");
                    }
                } // end explore from state p

                // extend frontier from state in the argument automaton
                outPort = stateInArgument.outgoingPort;
                transitions = outPort.linkedRelationList().iterator();
                while (transitions.hasNext() && !isStateInProductIllegal) {
                    InterfaceAutomatonTransition transition =
                            (InterfaceAutomatonTransition)transitions.next();

                    State destinationInArgument = transition.destinationState();

                    // get transitionLabel, transitionName and relation name
                    // for later use
                    String transitionLabel = transition.getLabel();
                    // remove ending "?"
                    String transitionName = transitionLabel.substring(0,
                                             transitionLabel.length()-1);

                    // switch depending on type of transition
                    int transitionType = transition.getType();
                    if (transitionType ==
                            InterfaceAutomatonTransition.INPUT_TRANSITION) {
                        // case 1
                        if (_inputNames.contains(transitionName)) {
                            // case 1A. Add transition to product as input
                            // transition
                            State destinationInProduct = _addState(product,
                                stateInThis, destinationInArgument, frontier);
                            _addTransition(product,
                              automaton.getName(),
                              stateInProduct, destinationInProduct,
                              transitionLabel);
                        } else {
                            // case 1B. transition is shared in product
                            String outName = transitionName + "!";
                            State destinationInThis = _getDestinationState(
                                stateInThis, outName);
                            if (destinationInThis != null) {
                                // case 1Ba. p has T output. Add T to product
                                // as internal transition
                                State destinationInProduct = _addState(product,
                                    destinationInThis, destinationInArgument,
                                    frontier);
                                _addTransition(product,
                                  this.getName() + NAME_CONNECTOR
                                      + automaton.getName(),
                                  stateInProduct, destinationInProduct,
                                  transitionName + ";");
                            } else {
                                // case 1Bb. p does not have T output.
                                // Transition cannot happen, ignore.
                            }
                        }
                    }
                    else if (transitionType ==
                            InterfaceAutomatonTransition.OUTPUT_TRANSITION) {
                        // case 2. T is output for q.
                        if (_outputNames.contains(transitionName)) {
                            // case 2A. T is output of product. Add T to
                            // product as output transition
                            State destinationInProduct = _addState(product,
                                stateInThis, destinationInArgument, frontier);
                            _addTransition(product,
                              automaton.getName(),
                              stateInProduct, destinationInProduct,
                              transitionLabel);
                        } else {
                            // case 2B. transition is shared in product
                            String inName = transitionName + "?";
                            State destinationInThis = _getDestinationState(
                                stateInThis, inName);
                            if (destinationInThis != null) {
                                // case 2Ba. p has T input. Need to add T
                                // to product as internal transition. However,
                                // to avoid adding this transition twice,
                                // leave the code that explores state p to
                                // add the transition
                            } else {
                                // case 2Bb. p does not have T input.
                                // stateInProduct is illegal
                                _illegalStates.add(stateInProduct);
                                isStateInProductIllegal = true;
                            }
                        }
                    } else if (transitionType ==
                            InterfaceAutomatonTransition.INTERNAL_TRANSITION) {
                        // case 3. T is internal for q. Add T to product
                        State destinationInProduct = _addState(product,
                            stateInThis, destinationInArgument, frontier);
                        _addTransition(product,
                          automaton.getName(),
                          stateInProduct, destinationInProduct,
                          transitionLabel);
                    } else {
                        throw new InternalErrorException(
                            "InterfaceAutomaton._computeProduct: unrecognized "
                            + "transition type.");
                    }
                } // end explore from state q
            }

            return product;
        } catch (NameDuplicationException exception) {
            // FIXME: this can actually happen, although extremly unlikely.
            // Eg. this automaton has states "X" and "Y_Z", the argument
            // has "X_Y" and "Z". Do we need to worry about this?
            throw new InternalErrorException(
                "InterfaceAutomaton._computeProduct: name in product "
                + "automaton clashes: " + exception.getMessage());
        }
    } 

    // Compute the names of the input, output, and internal transitions of
    // the composition.  Set the results to _inputNames, _outputNames, and
    // _internalNames;
    private void _computeTransitionNamesInComposition(
                                            InterfaceAutomaton automaton) {
        // compute shared transitions
        Set shared = this.inputNameSet();
        Set thatOutputs = automaton.outputNameSet();
        shared.retainAll(thatOutputs);

        Set shared1 = this.outputNameSet();
        Set thatInputs = automaton.inputNameSet();
        shared1.retainAll(thatInputs);

        shared.addAll(shared1);

        // compute input, output, and internal transitions
        _inputNames = this.inputNameSet();
        _inputNames.addAll(automaton.inputNameSet());
        _inputNames.removeAll(shared);

        _outputNames = this.outputNameSet();
        _outputNames.addAll(automaton.outputNameSet());
        _outputNames.removeAll(shared);

        _internalNames = this.internalTransitionNameSet();
        _internalNames.addAll(automaton.internalTransitionNameSet());
        _internalNames.addAll(shared);
    }

    // Create ports on the composition automaton, based on _inputNames and
    // _outputNames.
    private void _createPorts(InterfaceAutomaton composition)
            throws IllegalActionException {
        try {
            Iterator iterator = _inputNames.iterator();
            while (iterator.hasNext()) {
                String name = (String)iterator.next();
                TypedIOPort port = new TypedIOPort(composition, name);
                port.setInput(true);
            }

            iterator = _outputNames.iterator();
            while (iterator.hasNext()) {
                String name = (String)iterator.next();
                TypedIOPort port = new TypedIOPort(composition, name);
                port.setOutput(true);
            }
        } catch (NameDuplicationException exception) {
            // this should not happen. Composability check should ensure that
            // names do not clash.
            throw new InternalErrorException("InterfaceAutomaton._createPort: "
                + "Cannot create ports due to name duplication: "
                + exception.getMessage());
        }
    }

    // Return the destination state of the transition from the specified state
    // with the specified label. Return null if such a transition does not
    // exist.
    private State _getDestinationState(State state, String label) {
        ComponentPort outPort = state.outgoingPort;
        Iterator iterator = outPort.linkedRelationList().iterator();
        while (iterator.hasNext()) {
            InterfaceAutomatonTransition transition =
                    (InterfaceAutomatonTransition)iterator.next();
            String transitionLabel = transition.getLabel();
            if (transitionLabel.equals(label)) {
                return transition.destinationState();
            }
        }
        return null;
    }

    // prune illegal states from the argument. Use fontier exploration.
    // The Set frontier contains the references of illegal states in the
    // frontier; the Set _illegalStates contains references of all the
    // illegal states found so far. The Set frontier is always a subset
    // of _illegalStates. When this method is called, _illegalStats contains 
    // an initial set of illegal states computed in _computeProduct().
    //
    // init: frontier = _illegalStates
    // 
    // iterate: pick (remove) a state p from frontier
    //          for all states s that has an output or internal transition to p,
    //              if s is not in _illegalStates
    //                add s to both _illegalStates and frontier
    // 
    //          end when frontier is empty
    //
    // remove all states in _illegalstates from automaton 
    //
    // Note: this method does not operate the "this" automaton, it operates
    // on the composition automaton. This is implicite since _illegalStates
    // contains the states in the composition.
    private void _pruneIllegalStates() {
        // init
        Set frontier = new HashSet();
        Iterator iterator = _illegalStates.iterator();
        while (iterator.hasNext()) {
            frontier.add(iterator.next());
        }

        // iterate
        while ( !frontier.isEmpty()) {
            // there does not seem to be an easy way to remove an arbitray
            // element, except through Iterator
            iterator = frontier.iterator();
            State current = (State)iterator.next();
            frontier.remove(current);

            // make all states that can reach current by output or internal
            // transitions illegal
            ComponentPort inPort = current.incomingPort;
            Iterator transitions = inPort.linkedRelationList().iterator();
            while (transitions.hasNext()) {
                InterfaceAutomatonTransition transition =
                    (InterfaceAutomatonTransition)transitions.next();
                int transitionType = transition.getType();
                if (transitionType == 
                        InterfaceAutomatonTransition.OUTPUT_TRANSITION ||
                    transitionType ==
                        InterfaceAutomatonTransition.INTERNAL_TRANSITION) {
                    State sourceState = transition.sourceState();
                    if ( !_illegalStates.contains(sourceState)) {
                        _illegalStates.add(sourceState);
                        frontier.add(sourceState);
                    }
                }
            }
        }

        // remove all illegalStates from automaton
        iterator = _illegalStates.iterator();
        while (iterator.hasNext()) {
            State state = (State)iterator.next();
            _removeStateAndTransitions(state);
        }
    }

    // remove the specified state and transitions linked to it.
    private void _removeStateAndTransitions(State state) {
        try {
            // remove incoming transitions
            ComponentPort inPort = state.incomingPort;
            Iterator transitions = inPort.linkedRelationList().iterator();
            while (transitions.hasNext()) {
                ComponentRelation transition =
                    (ComponentRelation)transitions.next();
                transition.setContainer(null);
            }

            // remove outgoing transitions
            ComponentPort outPort = state.outgoingPort;
            transitions = outPort.linkedRelationList().iterator();
            while (transitions.hasNext()) {
                ComponentRelation transition =
                    (ComponentRelation)transitions.next();
                transition.setContainer(null);
            }

            // remove the state
            state.setContainer(null);
        } catch (IllegalActionException exception) {
            // Should not happen since the argument for setContainer() is null.
            throw new InternalErrorException(
                "InterfaceAutomaton._removeStateAndTransitions: "
                + "IllegalActionException thrown when calling setContainer() "
                + "with null argument: " + exception.getMessage());
        } catch (NameDuplicationException exception) {
            // Should not happen since the argument for setContainer() is null.
            throw new InternalErrorException(
                "InterfaceAutomaton._removeStateAndTransitions: "
                + "NameDuplicationException thrown when calling "
                + "setContainer() with null argument: "
                + exception.getMessage());
        }
    }

    // Remove states unreacheable from the initial state. Also remove the
    // transition from and to these states. Note that these states may not
    // be disconnected from the initial state. For example, these states
    // may have transitions to the initial state, but the initial state does
    // not have transitions to these states.
    // Use frontier exploration. The Set frontier stores the frontier states,
    // and the Set reacheableStates stores all reacheable states. frontier
    // is always a subset of reacheableStates.
    //
    // init: if initial state does not exist (it was illegal and removed)
    //           remove all states
    //       else
    //           reacheableStates = frontier = initial state
    //
    // iterate: Pick (remove) a state p from the frontier
    //          for all states s reacheable from p
    //              if s is not in reacheableStates
    //                  add s to both reacheableStates and frontier
    //
    //          end when frontier is empty
    //
    // remove all states not in reacheableStates from this automaton
    private void _removeUnreacheableStates() {
        // init
        State initialState;
        try {
            initialState = getInitialState();
        } catch (IllegalActionException exception) {
            // initial state was removed since it was illegal. remove all
            // states from this automaton to make it empty.
            this.removeAllRelations();
            this.removeAllEntities();
            return;
        }

        Set reacheableStates = new HashSet();
        Set frontier = new HashSet();
        reacheableStates.add(initialState);
        frontier.add(initialState);

        // iterate
        while ( !frontier.isEmpty()) {
            // there does not seem to be an easy way to remove an arbitray
            // element, except through Iterator
            Iterator iterator = frontier.iterator();
            State current = (State)iterator.next();
            frontier.remove(current);

            // make all states that are reacheable from current reacheable
            ComponentPort outPort = current.outgoingPort;
            Iterator transitions = outPort.linkedRelationList().iterator();
            while (transitions.hasNext()) {
                InterfaceAutomatonTransition transition =
                    (InterfaceAutomatonTransition)transitions.next();
                State destinationState = transition.destinationState();
                if ( !reacheableStates.contains(destinationState)) {
                    reacheableStates.add(destinationState);
                    frontier.add(destinationState);
                }
            }
        }

        // remove all states not reacheable from initial state
        List states = entityList();

        // the method removeAll() is not supported by this List (it throws
        // UnsupportedOperationException), so manually construct the set
        // of unreacheable states.
        // states.removeAll(reacheableStates);
        Set unreacheableStates = new HashSet();
        Iterator iterator = states.iterator();
        while (iterator.hasNext()) {
            Object state = iterator.next();
            if ( !reacheableStates.contains(state)) {
                unreacheableStates.add(state);
            }
        }

        iterator = unreacheableStates.iterator();
        while (iterator.hasNext()) {
            State state = (State)iterator.next();
            _removeStateAndTransitions(state);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The state name in the composition automaton is formed by
    // <nameInThisAutomaton><NAME_CONNECTOR><nameInArgumentAutomaton>
    // The transition name prefix in the composition is formed by
    // (1) <nameOfAutomaton> for non-shared transitions;
    // (2) <nameOfAutomaton1><NAME_CONNECTOR><nameofAutomaton2> for shared
    //     transitions.
    // The name of the product automaton is
    // <thisName><NAME_CONNECTOR><argumentName>
    private final String NAME_CONNECTOR = "_";

    // The following variables are used to store intermediate results
    // during the computation of compose().

    // Names of the transitions in the composition. Constructed by
    // _computeTransitionNamesInComposition().
    private Set _inputNames;
    private Set _outputNames;
    private Set _internalNames;

    // Set of illegal states in the product automaton. The elements of
    // the Set are references to states. Constructed by _computeProduct().
    private Set _illegalStates;
    
    ///////////////////////////////////////////////////////////////////
    ////                            inner class                    ////
    private class Triple {
        private Triple(State stateInProduct, State stateInThis,
                                                State stateInArgument) {
            _stateInProduct = stateInProduct;
            _stateInThis = stateInThis;
            _stateInArgument = stateInArgument;
        }

        private State _stateInProduct = null;
        private State _stateInThis = null;
        private State _stateInArgument = null;
    }
}
