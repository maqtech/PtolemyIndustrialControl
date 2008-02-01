/* An utility function for traversing the system and generate files for model checking.

 Copyright (c) 1998-2008 The Regents of the University of California.
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

package ptolemy.vergil.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * This is an utility function for ptolemy models. It performs a systematic
 * traversal of the system and generate files for model checking.
 *
 * @author Chihhong Patrick Cheng, Contributor: Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red ()
 */
public class VerificationUtility {

    /**
     * Return an StringBuffer that contains the converted .smv format of the system.
     *
     * @param model
     * @param pattern The temporal formula used to be attached in the .smv file.
     * @param choice The type of the formula. It may be either a CTL or LTL formula.
     * @param span A constant used to expand the size of the rough domain.
     * @return The converted .smv format of the system.
     */
    public static StringBuffer generateSMVDescription(CompositeActor model,
            String pattern, String choice, String span)
            throws IllegalActionException {

        StringBuffer returnFmvFormat = new StringBuffer("");
        returnFmvFormat.append("MODULE main \n");
        returnFmvFormat.append("\tVAR \n");

        returnFmvFormat.append("\t\tproc: " + model.getName() + "();\n\n");

        if (choice.equalsIgnoreCase("CTL")) {
            returnFmvFormat.append("\tSPEC \n");
            returnFmvFormat.append("\t\t" + pattern + "\n");
        } else if (choice.equalsIgnoreCase("LTL")) {
            returnFmvFormat.append("\tLTLSPEC \n");
            returnFmvFormat.append("\t\t" + pattern + "\n");
        }

        returnFmvFormat.append("MODULE " + model.getName() + "() \n");
        returnFmvFormat.append("\tVAR \n");
        HashMap<String, FSMActor> FSMActors = new HashMap<String, FSMActor>();

        // List out all FSMs with their states.
        // int index = 0;
        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {

                // String name = innerModel.getName() == null ? new String(
                // Integer.toString(index) + "-") : new String(Integer
                // .toString(index)
                // + "-" + innerModel.getName());
                String name = innerModel.getName();
                FSMActors.put(name, (FSMActor) innerModel);

                HashSet<State> frontier = new HashSet<State>();
                try {
                    // Enumerate all states
                    frontier = _enumerateStateSet((FSMActor) innerModel);
                } catch (Exception ex) {
                    throw new IllegalActionException(
                            "VerificationUtility.generateSMVFormat() clashes: "
                                    + ex.getMessage());
                }

                returnFmvFormat.append("\t\t" + name + "-state : {");
                Iterator<State> it = frontier.iterator();
                while (it.hasNext()) {
                    State val = (State) it.next();
                    returnFmvFormat.append(val.getDisplayName());
                    if (it.hasNext()) {
                        returnFmvFormat.append(",");
                    }
                }
                returnFmvFormat.append("};\n");

            }
        }

        // now initiate _variableInfo
        _variableInfo = new HashMap<String, VariableInfo>();
        HashSet<String> globalVariableSet = new HashSet<String>();

        // Decide variables encoded in the Kripke Structure. Here we need to
        // consider all variables in FSMActor.
        // FIXME: For outer coordination, it can be observed via the initial
        // setting of variables. For example, in the example of traffic
        // light, we know that for variable Sec, Sec_isPresent is shown in
        // the guard, but we are not initializing it. This indicates that
        // Sec is an outer variable used for coordination. In this way, we
        // do not need to list it as variable in the .smv file. But we still
        // need to use it as coordination between variables.
        // FIXME: Nop, the above statement is wrong. No cooredination is
        // needed.

        HashSet<String> variableSet = new HashSet<String>();
        try {
            // Enumerate all variables used in the Kripke structure
            // note that every valName =
            // String(FSMActor.getName()+"-" + valName).
            // So don't add FSMActor.getName()+"-" later on!
            int numSpan = Integer.parseInt(span);
            variableSet = _decideVariableSet((CompositeActor) model, numSpan);
            // _variableInfo would store the domain (min, max) of a
            // certain variable.
        } catch (Exception ex) {
            throw new IllegalActionException(
                    "VerificationUtility.generateSMVFormat() clashes: "
                            + ex.getMessage());
        }

        Iterator<String> itVariableSet = variableSet.iterator();
        while (itVariableSet.hasNext()) {

            String valName = (String) itVariableSet.next();
            globalVariableSet.add(new String(valName));
            returnFmvFormat.append("\t\t" + valName + " : {");
            // Retrieve the lower bound and upper bound of the
            // variable used in the system
            if (_variableInfo.get(valName) == null) {
                throw new IllegalActionException("Internal error, getting \""
                        + valName + "\" from \"_variableInfo\" returned null?");
            }
            VariableInfo individual = (VariableInfo) _variableInfo.get(valName);
            if (individual == null) {
                throw new IllegalActionException("Internal error, getting \""
                        + valName + "\" returned null?");
            }
            int lowerBound = Integer.parseInt(individual._minValue);
            int upperBound = Integer.parseInt(individual._maxValue);
            try {
                int numSpan = Integer.parseInt(span);
                returnFmvFormat.append(" ls,");
                for (int number = lowerBound; number <= upperBound; number++) {
                    returnFmvFormat.append(number);
                    returnFmvFormat.append(",");
                }
                returnFmvFormat.append("gt };\n");

            } catch (Exception ex) {
                throw new IllegalActionException(
                        "VerificationUtility.generateSMVFormat() clashes: "
                                + ex.getMessage());
            }
        }

        System.out.println("In main, _variableInfo.size is "
                + Integer.valueOf(_variableInfo.size()));

        _generateAllVariableTransitions(globalVariableSet,
                (CompositeActor) model);

        returnFmvFormat.append("\tASSIGN \n");

        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                // setup initial state
                try {
                    String name = ((FSMActor) innerModel).getInitialState()
                            .getName();
                    returnFmvFormat.append("\t\tinit("
                            + ((FSMActor) innerModel).getName() + "-"
                            + "state) := " + name + ";\n");
                } catch (Exception ex) {
                    throw new IllegalActionException(
                            "VerificationUtility.generateSMVFormat() clashes: "
                                    + ex.getMessage());
                }
                returnFmvFormat.append("\t\tnext("
                        + ((FSMActor) innerModel).getName() + "-"
                        + "state) :=\n");
                returnFmvFormat.append("\t\t\tcase\n");
                LinkedList<VariableTransitionInfo> infoList = _variableTransitionInfo
                        .get(((FSMActor) innerModel).getName() + "-" + "state");
                for (int i = 0; i < infoList.size(); i++) {
                    VariableTransitionInfo info = infoList.get(i);
                    returnFmvFormat.append("\t\t\t\t" + info._preCondition
                            + " :{ " + info._varibleNewValue + " };\n");
                }
                returnFmvFormat.append("\t\t\t\t1             : "
                        + ((FSMActor) innerModel).getName() + "-" + "state;\n");
                returnFmvFormat.append("\t\t\tesac;\n\n");
            }

        }

        // Find out initial values for those variables.
        HashMap<String, String> variableInitialValue = new HashMap<String, String>();
        variableInitialValue = _retrieveVariableInitialValue(globalVariableSet,
                ((CompositeActor) model));

        Iterator<String> newItVariableSet = globalVariableSet.iterator();
        while (newItVariableSet.hasNext()) {

            String valName = (String) newItVariableSet.next();
            returnFmvFormat.append("\t\tinit(" + valName + ") := "
                    + variableInitialValue.get(valName) + ";\n");
            returnFmvFormat.append("\t\tnext(" + valName + ") :=\n");
            returnFmvFormat.append("\t\t\tcase\n");

            // Generate all transitions; start from "state"
            List<VariableTransitionInfo> innerInfoList = _variableTransitionInfo
                    .get(valName);
            for (int i = 0; i < innerInfoList.size(); i++) {
                VariableTransitionInfo info = innerInfoList.get(i);
                returnFmvFormat.append("\t\t\t\t" + info._preCondition + " :{ "
                        + info._varibleNewValue + " };\n");
            }
            returnFmvFormat
                    .append("\t\t\t\t1             : " + valName + ";\n");
            returnFmvFormat.append("\t\t\tesac;\n\n");
        }

        //System.out.println("\n\n****** RESULT OF CONVERSION ******");
        //System.out.println(returnFmvFormat.toString());
        return returnFmvFormat;
    }

    /**
     *  This function decides if the director of the current actor is SR.
     *  If not, return false. This is because our current analysis is only
     *  valid when the director is SR.
     *
     *  @param model Model used for testing.
     *  @return a boolean value indicating if the director is SR.
     */
    public static boolean isValidModelForVerification(CompositeActor model) {
        Director director = ((CompositeActor) model).getDirector();
        if (!(director instanceof SRDirector)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This private function first decides variables that would be used in the
     * Kripke structure. Once when it is decided, it performs step 1 and 2 of
     * the variable domain generation process.
     *
     * @param model
     * @param numSpan
     * @return returnVariableSet
     */
    private static HashSet<String> _decideVariableSet(CompositeActor model,
            int numSpan) throws IllegalActionException {
        // BY PATRICK
        _variableInfo = new HashMap<String, VariableInfo>();
        HashSet<String> returnVariableSet = new HashSet<String>();

        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                HashSet<State> stateSet = new HashSet<State>();
                try {
                    // initialize
                    HashMap<String, State> frontier = new HashMap<String, State>();

                    // create initial state
                    State stateInThis = ((FSMActor) innerModel)
                            .getInitialState();
                    String name = stateInThis.getName();
                    frontier.put(name, stateInThis);

                    // iterate
                    while (!frontier.isEmpty()) {
                        // pick a state from frontier. It seems that there isn't
                        // an easy way to pick an arbitrary entry from a HashMap,
                        // except through Iterator
                        Iterator<String> iterator = frontier.keySet()
                                .iterator();
                        name = (String) iterator.next();
                        stateInThis = (State) frontier.remove(name);
                        if (stateInThis == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + name
                                            + "\" returned null?");
                        }
                        ComponentPort outPort = stateInThis.outgoingPort;
                        Iterator transitions = outPort.linkedRelationList()
                                .iterator();

                        while (transitions.hasNext()) {
                            Transition transition = (Transition) transitions
                                    .next();

                            State destinationInThis = transition
                                    .destinationState();

                            if (!stateSet.contains(destinationInThis)) {
                                frontier.put(destinationInThis.getName(),
                                        destinationInThis);
                                stateSet.add(destinationInThis);
                            }

                            // Retrieve the transition

                            boolean hasAnnotation = false;
                            String text;
                            try {
                                text = transition.annotation.stringValue();
                            } catch (IllegalActionException e) {
                                text = "Exception evaluating annotation: "
                                        + e.getMessage();
                            }
                            if (!text.trim().equals("")) {
                                hasAnnotation = true;

                            }

                            String guard = transition.getGuardExpression();
                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // do nothing
                                } else {

                                    // Rule II. For all variables that are used
                                    // as guards, they would be expanded as AP

                                    // Separate each guard expression into
                                    // substring
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");
                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {

                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();
                                            // Retrieve the left value of the
                                            // inequality.
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");
                                            // Here we may still have two cases:
                                            // (1) XXX_isPresent (2) the normal
                                            // case.
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {
                                                // First case, synchronize
                                                // usage.
                                                // Currently not implementing
                                            } else {
                                                // Second case, place this
                                                // variable into usage set.
                                                // Retrieve the rvalue
                                                //
                                                // Check if the right value
                                                // exists.
                                                // We need to ward off cases
                                                // like "true".

                                                try {
                                                    String rValue = characterOfSubGuard[1]
                                                            .trim();
                                                    int numberRetrival = 0;
                                                    boolean rvalueSingleNumber = true;
                                                    try {
                                                        numberRetrival = Integer
                                                                .parseInt(rValue);
                                                    } catch (Exception ex) {
                                                        rvalueSingleNumber = false;
                                                    }
                                                    if (rvalueSingleNumber == true) {
                                                        // add it into the
                                                        // _variableInfo
                                                        returnVariableSet
                                                                .add(innerModel
                                                                        .getName()
                                                                        + "-"
                                                                        + characterOfSubGuard[0]
                                                                                .trim());

                                                        VariableInfo variable = (VariableInfo) _variableInfo
                                                                .get(innerModel
                                                                        .getName()
                                                                        + "-"
                                                                        + characterOfSubGuard[0]
                                                                                .trim());
                                                        if (variable == null) {
                                                            // Create a new one
                                                            // and
                                                            // insert all info.
                                                            VariableInfo newVariable = new VariableInfo(
                                                                    innerModel
                                                                            .getName()
                                                                            + "-"
                                                                            + characterOfSubGuard[0]
                                                                                    .trim(),
                                                                    Integer
                                                                            .toString(numberRetrival),
                                                                    Integer
                                                                            .toString(numberRetrival));
                                                            _variableInfo
                                                                    .put(
                                                                            innerModel
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + characterOfSubGuard[0]
                                                                                            .trim(),
                                                                            newVariable);

                                                        } else {
                                                            // modify the
                                                            // existing
                                                            // one
                                                            if (Integer
                                                                    .parseInt(variable._maxValue) < numberRetrival) {
                                                                variable._maxValue = Integer
                                                                        .toString(numberRetrival);
                                                            }
                                                            if (Integer
                                                                    .parseInt(variable._minValue) > numberRetrival) {
                                                                variable._minValue = Integer
                                                                        .toString(numberRetrival);
                                                            }
                                                            _variableInfo
                                                                    .remove(innerModel
                                                                            .getName()
                                                                            + "-"
                                                                            + characterOfSubGuard[0]
                                                                                    .trim());
                                                            _variableInfo
                                                                    .put(
                                                                            innerModel
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + characterOfSubGuard[0]
                                                                                            .trim(),
                                                                            variable);

                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    // We know that we can not
                                                    // parse the
                                                    // rvalue.

                                                }

                                            }

                                        }

                                    }
                                }

                            }

                            String expression = transition.setActions
                                    .getExpression();
                            if ((expression != null)
                                    && !expression.trim().equals("")) {
                                // Retrieve possible value of the variable
                                String[] splitExpression = expression
                                        .split(";");
                                for (int i = 0; i < splitExpression.length; i++) {
                                    String[] characters = splitExpression[i]
                                            .split("=");
                                    String lValue = characters[0].trim();
                                    String rValue;
                                    int numberRetrival = 0;
                                    boolean rvalueSingleNumber = true;
                                    try {
                                        rValue = characters[1].trim();
                                        numberRetrival = Integer
                                                .parseInt(rValue);
                                    } catch (Exception ex) {
                                        rvalueSingleNumber = false;
                                    }
                                    if (rvalueSingleNumber == true) {
                                        // add it into the _variableInfo
                                        VariableInfo variable = (VariableInfo) _variableInfo
                                                .get(innerModel.getName() + "-"
                                                        + lValue);
                                        if (variable == null) {
                                            // Create a new one and insert all
                                            // info.
                                            VariableInfo newVariable = new VariableInfo(
                                                    innerModel.getName() + "-"
                                                            + lValue,
                                                    Integer
                                                            .toString(numberRetrival),
                                                    Integer
                                                            .toString(numberRetrival));
                                            _variableInfo
                                                    .put(innerModel.getName()
                                                            + "-" + lValue,
                                                            newVariable);

                                        } else {
                                            // modify the existing one
                                            if (Integer
                                                    .parseInt(variable._maxValue) < numberRetrival) {
                                                variable._maxValue = Integer
                                                        .toString(numberRetrival);
                                            }
                                            if (Integer
                                                    .parseInt(variable._minValue) > numberRetrival) {
                                                variable._minValue = Integer
                                                        .toString(numberRetrival);
                                            }
                                            _variableInfo.remove(innerModel
                                                    .getName()
                                                    + "-" + lValue);
                                            _variableInfo.put(innerModel
                                                    .getName()
                                                    + "-" + lValue, variable);

                                        }
                                    }
                                }
                            }

                        }

                    }

                } catch (Exception exception) {
                    // FIXME: this can actually happen, although extremely
                    // unlikely.
                    // Eg. this automaton has states "X" and "Y_Z", the argument
                    // has "X_Y" and "Z". Do we need to worry about this?
                    throw new InternalErrorException(
                            "FmvAutomaton._DecideVariableSet() clashes: "
                                    + exception.getMessage());
                }
            }
        }

        // Expend based on the domain
        Iterator<String> itVariableSet = returnVariableSet.iterator();
        while (itVariableSet.hasNext()) {

            String valName = (String) itVariableSet.next();

            // Retrieve the lower bound and upper bound of the variable used in
            // the system based on inequalities or assignments
            VariableInfo individual = (VariableInfo) _variableInfo
                    .remove(valName);
            try {
                int lbOriginal = Integer.parseInt(individual._minValue);
                int ubOriginal = Integer.parseInt(individual._maxValue);
                int lbNew = lbOriginal - (ubOriginal - lbOriginal + 1)
                        * numSpan;
                int ubNew = ubOriginal + (ubOriginal - lbOriginal + 1)
                        * numSpan;
                individual._minValue = new String(Integer.toString(lbNew));
                individual._maxValue = new String(Integer.toString(ubNew));
                _variableInfo.put(valName, individual);

            } catch (Exception ex) {

            }
        }

        return returnVariableSet;
    }

    /**
     * Perform an enumeration of the state in this FmvAutomaton and return a
     * HashSet of states.
     *
     * @param innerModel A FSM which is fed into the function.
     * @return A HashSet of states of a particular FSMActor
     */
    private static HashSet<State> _enumerateStateSet(FSMActor innerModel)
            throws IllegalActionException {

        HashSet<State> returnStateSet = new HashSet<State>();
        try {
            // initialize
            HashMap<String, State> frontier = new HashMap<String, State>();

            // create initial state
            State stateInThis = innerModel.getInitialState();
            String name = stateInThis.getName();
            frontier.put(name, stateInThis);
            returnStateSet.add(stateInThis);
            // iterate
            while (!frontier.isEmpty()) {
                // pick a state from frontier. It seems that there isn't an
                // easy way to pick an arbitrary entry from a HashMap,
                // except through Iterator
                Iterator<String> iterator = frontier.keySet().iterator();
                name = (String) iterator.next();
                stateInThis = (State) frontier.remove(name);
                if (stateInThis == null) {
                    throw new IllegalActionException(
                            "Internal error, removing \"" + name
                                    + "\" returned null?");
                }
                ComponentPort outPort = stateInThis.outgoingPort;
                Iterator transitions = outPort.linkedRelationList().iterator();

                while (transitions.hasNext()) {
                    Transition transition = (Transition) transitions.next();

                    State destinationInThis = transition.destinationState();
                    if (!returnStateSet.contains(destinationInThis)) {
                        frontier.put(destinationInThis.getName(),
                                destinationInThis);
                        returnStateSet.add(destinationInThis);
                    }
                }
            }
        } catch (Exception exception) {
            throw new InternalErrorException(
                    "FmvAutomaton._EnumerateStateSet() clashes: "
                            + exception.getMessage());
        }
        return returnStateSet;
    }

    /**
     * Generate all premise-action pairs regarding this
     * FmvAutomaton. For example, this method may generate
     * (Carlight-state=red)&&(Carlight-count=1):{grn}.  This can only be applied when
     * the domain of variable is decided.
     *
     * @param globalVariableSet Set of variables used in the system.
     * @param model System under analysis.
     */
    private static void _generateAllVariableTransitions(
            HashSet<String> globalVariableSet, CompositeActor model)
            throws IllegalActionException {

        _variableTransitionInfo = new HashMap<String, LinkedList<VariableTransitionInfo>>();

        try {

            Iterator<String> vit = globalVariableSet.iterator();
            while (vit.hasNext()) {
                String v = vit.next();
                LinkedList<VariableTransitionInfo> l = new LinkedList<VariableTransitionInfo>();
                _variableTransitionInfo.put(v, l);
            }
        } catch (Exception ex) {

        }

        // initialize
        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                LinkedList<VariableTransitionInfo> l = new LinkedList<VariableTransitionInfo>();
                _variableTransitionInfo.put(((FSMActor) innerModel).getName()
                        + "-state", l);
                // For each variable in different FSMActor actor, we use
                // actor.getName()+"-"+variableName as its variable name in
                // global. We use actor.getName()+"-state" to represent variable
                // in a particular machine. Note that for those variables that
                // do not used in the Kripke structure rather than
                // synchronization process, we SHOULD NOT add actor.getName()
                // because it would impede the checking process among two FSMs.
            }

        }

        // First we need to evaluate "outer coordination" in the system. An
        // outer coordination happens in a transition guard like XX_isPresent,
        // but we do not see any initialization of XX in the system. Once when
        // we recognize those variables, we say that for all transitions with
        // "XX_isPresent" should be synchronized. This is the NEW Rule IV.
        //
        // FIXME: I didn't implement this because the above statement seems to
        // be wrong. We don't need to synchronize them because they would not
        // influence each other. It is only the inner synchronization that
        // matters. Hence Rule IV is now still used to decide outer coordination
        // variables, but as a reference only. This would be further
        // investigated when I write the report.
        //
        // Also we need to apply Rule III: For all variable XX that is used at
        // post-firing rules (transition.outputActions) and also used with
        // XX_isPresent, we need to perform synchronizing techniques.
        //

        // Rule IV: outputSetUpSet stores variables used in all
        // transition.outputActions. Hence these variables SHOULD NOT have
        // actor.getName() attached in front of their names.
        HashSet<String> outputSetUpSet = new HashSet<String>();
        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                HashSet<State> stateSet = new HashSet<State>();

                try {
                    // Initialize
                    HashMap<String, State> frontier = new HashMap<String, State>();

                    // Create initial state
                    State stateInThis = ((FSMActor) innerModel)
                            .getInitialState();
                    String name = stateInThis.getName();
                    frontier.put(name, stateInThis);

                    // Iterate
                    while (!frontier.isEmpty()) {
                        // pick a state from frontier.
                        Iterator<String> iterator = frontier.keySet()
                                .iterator();
                        name = (String) iterator.next();
                        stateInThis = (State) frontier.remove(name);
                        if (stateInThis == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + name
                                            + "\" returned null?");
                        }
                        ComponentPort outPort = stateInThis.outgoingPort;
                        Iterator transitions = outPort.linkedRelationList()
                                .iterator();

                        while (transitions.hasNext()) {
                            // Retrieve the transition
                            Transition transition = (Transition) transitions
                                    .next();

                            State destinationInThis = transition
                                    .destinationState();

                            if (!stateSet.contains(destinationInThis)) {
                                frontier.put(destinationInThis.getName(),
                                        destinationInThis);
                                stateSet.add(destinationInThis);
                            }

                            String setOutputExpression = transition.outputActions
                                    .getExpression();
                            if ((setOutputExpression != null)
                                    && !setOutputExpression.trim().equals("")) {
                                // Retrieve possible value of the variable
                                String[] splitExpression = setOutputExpression
                                        .split(";");
                                for (int i = 0; i < splitExpression.length; i++) {
                                    String[] characters = splitExpression[i]
                                            .split("=");
                                    String lValue = characters[0].trim();
                                    String rValue = "";
                                    int numberRetrival = 0;
                                    boolean rvalueSingleNumber = true;
                                    try {
                                        rValue = characters[1].trim();
                                        numberRetrival = Integer
                                                .parseInt(rValue);
                                    } catch (Exception ex) {
                                        rvalueSingleNumber = false;
                                    }
                                    if (rvalueSingleNumber == true) {
                                        // see if the lValue is in variableSet
                                        outputSetUpSet.add(lValue);
                                    }
                                }
                            }

                        }
                    }

                } catch (Exception ex) {

                }
            }
        }

        // Now perform a search on all transitions to see if there is an
        // sub guard expression XX_isPresent that is not used in outputActions,
        // meaning that it is not in the outputSetUpSet. Record those in
        // the outer-coordination variable set. Hence these variables SHOULD NOT
        // have actor.getName() attached in front of their names.
        HashSet<String> outerCoordinationVariableSet = new HashSet<String>();
        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                HashSet<State> stateSet = new HashSet<State>();
                try {
                    // Initialize
                    HashMap<String, State> frontier = new HashMap<String, State>();

                    // Create initial state
                    State stateInThis = ((FSMActor) innerModel)
                            .getInitialState();
                    String name = stateInThis.getName();
                    frontier.put(name, stateInThis);

                    // Iterate
                    while (!frontier.isEmpty()) {
                        // pick a state from frontier.
                        Iterator<String> iterator = frontier.keySet()
                                .iterator();
                        name = (String) iterator.next();
                        stateInThis = (State) frontier.remove(name);
                        if (stateInThis == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + name
                                            + "\" returned null?");
                        }
                        ComponentPort outPort = stateInThis.outgoingPort;
                        Iterator transitions = outPort.linkedRelationList()
                                .iterator();

                        while (transitions.hasNext()) {
                            // Retrieve the transition
                            Transition transition = (Transition) transitions
                                    .next();

                            State destinationInThis = transition
                                    .destinationState();

                            if (!stateSet.contains(destinationInThis)) {
                                frontier.put(destinationInThis.getName(),
                                        destinationInThis);
                                stateSet.add(destinationInThis);
                            }

                            boolean hasAnnotation = false;
                            String text;
                            try {
                                text = transition.annotation.stringValue();
                            } catch (IllegalActionException e) {
                                text = "Exception evaluating annotation: "
                                        + e.getMessage();
                            }
                            if (!text.trim().equals("")) {
                                hasAnnotation = true;
                                // buffer.append(text);
                            }

                            String guard = transition.getGuardExpression();

                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // FIXME: (2007/12/14 Patrick.Cheng)
                                    // Currently I don't know the meaning of
                                    // annotation. Do nothing currently.
                                } else {

                                    // Rule II. For all variables that are
                                    // used as guards, they would be
                                    // expanded as Atomic Propositions (AP).

                                    // Separate each guard expression into
                                    // "sub guard expressions".
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");

                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {
                                            // Trim tab/space
                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();

                                            // Retrieve the left value of
                                            // the inequality. Here we may still
                                            // have two cases for the lValue:
                                            // (1) XX_isPresent (2) the
                                            // normal case (including "true").
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");

                                            String lValue = characterOfSubGuard[0]
                                                    .trim();
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {
                                                // XX_isPresent
                                                String[] variableName = (characterOfSubGuard[0]
                                                        .trim())
                                                        .split("_isPresent");
                                                // variableName[0] is the XX.
                                                // Check if it is in
                                                if (outputSetUpSet
                                                        .contains(variableName[0]) == false) {
                                                    // add into
                                                    // outerCoordinationVariableSet
                                                    outerCoordinationVariableSet
                                                            .add(variableName[0]);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {

                }
            }
        }

        // Main Process to Perform Transition Generation.

        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {

                HashSet<State> stateSet = new HashSet<State>();
                try {
                    // Initialize
                    HashMap<String, State> frontier = new HashMap<String, State>();

                    // Create initial state
                    State stateInThis = ((FSMActor) innerModel)
                            .getInitialState();
                    String name = stateInThis.getName();
                    frontier.put(name, stateInThis);

                    // Iterate
                    while (!frontier.isEmpty()) {
                        // pick a state from frontier.
                        Iterator<String> iterator = frontier.keySet()
                                .iterator();
                        name = (String) iterator.next();
                        stateInThis = (State) frontier.remove(name);
                        if (stateInThis == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + name
                                            + "\" returned null?");
                        }
                        ComponentPort outPort = stateInThis.outgoingPort;
                        Iterator transitions = outPort.linkedRelationList()
                                .iterator();

                        while (transitions.hasNext()) {
                            // Retrieve the transition
                            Transition transition = (Transition) transitions
                                    .next();

                            State destinationInThis = transition
                                    .destinationState();

                            if (!stateSet.contains(destinationInThis)) {
                                frontier.put(destinationInThis.getName(),
                                        destinationInThis);
                                stateSet.add(destinationInThis);
                            }

                            boolean hasAnnotation = false;
                            String text;
                            try {
                                text = transition.annotation.stringValue();
                            } catch (IllegalActionException e) {
                                text = "Exception evaluating annotation: "
                                        + e.getMessage();
                            }
                            if (!text.trim().equals("")) {
                                hasAnnotation = true;
                                // buffer.append(text);
                            }

                            StringBuffer premiseRelated = new StringBuffer("");
                            String guard = transition.getGuardExpression();
                            String setAction = transition.setActions
                                    .getExpression();

                            // variableUsedInTransitionSet: Store variable
                            // names used in this transition as
                            // preconditions. If in the guard expression, we
                            // have X<3 && Y>5, then X and Y are used
                            // as variables in precondition and should be
                            // stored in the set "variableUsedInTransitionSet".

                            // FIXME: (2008/01/22) Also, variables used in setAction should
                            // be stored in the set "variableUsedInTransitionSet".
                            HashSet<String> variableUsedInTransitionSet = new HashSet<String>();

                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // FIXME: (2007/12/14 Patrick.Cheng)
                                    // Currently I don't know the meaning of
                                    // annotation. Do nothing currently.
                                } else {

                                    // Rule II. For all variables that are
                                    // used as guards, they would be
                                    // expanded as Atomic Propositions (AP).

                                    // Separate each guard expression into
                                    // "sub guard expressions".
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");

                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {
                                            // Trim tab/space
                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();

                                            // Retrieve the left value of
                                            // the inequality. Here we may still
                                            // have two cases for the lValue:
                                            // (1) XXX_isPresent (2) the
                                            // normal case (including "true").
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");

                                            String lValue = characterOfSubGuard[0]
                                                    .trim();
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {
                                                String[] variableName = characterOfSubGuard[0]
                                                        .trim().split(
                                                                "_isPresent");
                                                // The variable SHOULD NOT
                                                // have actor.getName() attached
                                                // in front of its name. This is
                                                // because this is XX_isPresent,
                                                // and we need to use XX to
                                                // match synchronizations.
                                                variableUsedInTransitionSet
                                                        .add(variableName[0]);
                                            } else {
                                                // Store in the set. Use
                                                // try-catch to capture cases
                                                // when single "true" exists.

                                                try {
                                                    String rValue = characterOfSubGuard[1]
                                                            .trim();
                                                    // The variable SHOULD
                                                    // have actor.getName()
                                                    // attached in front of its
                                                    // name because this is a
                                                    // variable used in the
                                                    // Kripke structure.

                                                    variableUsedInTransitionSet
                                                            .add(((FSMActor) innerModel)
                                                                    .getName()
                                                                    + "-"
                                                                    + lValue);
                                                } catch (Exception ex) {

                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            if ((setAction != null)
                                    && !setAction.trim().equals("")) {

                                String[] setActionSplitExpression = setAction
                                        .split("(;)");

                                if (setActionSplitExpression.length != 0) {
                                    for (int i = 0; i < setActionSplitExpression.length; i++) {
                                        // Trim tab/space
                                        String subSetActionCondition = setActionSplitExpression[i]
                                                .trim();

                                        String[] characterOfSubSetAction = subSetActionCondition
                                                .split("(=)");

                                        String lValue = characterOfSubSetAction[0]
                                                .trim();

                                        try {
                                            variableUsedInTransitionSet
                                                    .add(((FSMActor) innerModel)
                                                            .getName()
                                                            + "-" + lValue);
                                        } catch (Exception ex) {

                                        }

                                    }
                                }

                            }

                            // Once all variables used in the transition is
                            // listed, generate a list to estimate its domain.
                            // For example, if variable X has upper bound 5 and
                            // lower bound 1, then the result of the next step
                            // would show that variable X has a list with domain
                            // {1,2,3,4,5}.
                            // FIXME: We should apply {min, 1, 2, 3, 4, 5, max}
                            // instead ... (new discoveries by Patrick)
                            // But I haven't do this yet...
                            // FIXME: In this new version we implement this...

                            HashMap<String, ArrayList<Integer>> valueDomain = new HashMap<String, ArrayList<Integer>>();
                            Iterator<String> it = variableUsedInTransitionSet
                                    .iterator();
                            // Here in the variableUsedInTransitionSet, it has
                            // some variables attached with actor.getName() and
                            // some others without those name attachments.
                            while (it.hasNext()) {
                                String val = (String) it.next();
                                // Retrieve the value in the _variableInfo.
                                // contents in _variableInfo are previously
                                // generated by
                                VariableInfo variableInfo = _variableInfo
                                        .get(val);
                                // variableInfo may be null. This is because
                                // those values are like XX in XX_isPresent.
                                // They are used as synchronization only.
                                if (variableInfo != null) {
                                    int lowerBound = Integer
                                            .parseInt(variableInfo._minValue);
                                    int upperBound = Integer
                                            .parseInt(variableInfo._maxValue);
                                    // Now perform the add up of new value: DOMAIN_GT and
                                    // DOMAIN_LS into each of the
                                    // variableDomainForTransition set. We make it a sorted
                                    // list to facilitate further processing.
                                    ArrayList<Integer> variableDomainForTransition = new ArrayList<Integer>();
                                    variableDomainForTransition.add(DOMAIN_LS);
                                    for (int number = lowerBound; number <= upperBound; number++) {
                                        // Place each possible value within
                                        // boundary into the list.
                                        variableDomainForTransition.add(Integer
                                                .valueOf(number));
                                    }
                                    variableDomainForTransition.add(DOMAIN_GT);
                                    valueDomain.put(val,
                                            variableDomainForTransition);
                                }

                            }

                            // After previous steps, for each variable now
                            // there exists a list with all possible values
                            // between lower bound and upper bound. Now perform
                            // the restriction process based on the guard
                            // expression. For example, if variable X has upper
                            // bound 5 and lower bound 1, and the guard
                            // expression says that X<3, then the domain would
                            // be restricted to only {1,2}.

                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // do nothing
                                } else {
                                    // Separate each guard expression into
                                    // substring
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");
                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {

                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();

                                            // Retrieve the left value of
                                            // the inequality.
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");
                                            // Here we may still have two
                                            // cases:
                                            // (1) XXX_isPresent
                                            // (2) the normal case.
                                            String lValue = characterOfSubGuard[0]
                                                    .trim();
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {
                                                // FIXME: (2007/12/14
                                                // Patrick.Cheng)
                                                // First check if XX is in the
                                                // outer coordination set. If
                                                // so, we skip it. Then check
                                                // for every transition that has
                                                // outputAction XX = const. Add
                                                // up the constraint for that
                                                // transition into that.

                                                String[] variableName = (characterOfSubGuard[0]
                                                        .trim())
                                                        .split("_isPresent");
                                                if (outerCoordinationVariableSet
                                                        .contains(variableName[0]) == false) {
                                                    // Perform premise addition
                                                    // process.
                                                    String a = _generateRelatedPremise(
                                                            model,
                                                            variableName[0],
                                                            (FSMActor) innerModel,
                                                            outerCoordinationVariableSet);
                                                    if (a == null
                                                            || a
                                                                    .trim()
                                                                    .equalsIgnoreCase(
                                                                            "")) {

                                                    } else {
                                                        premiseRelated
                                                                .append(" & ("
                                                                        + a
                                                                        + ")");
                                                    }

                                                }
                                            } else {
                                                // Check if the right value
                                                // exists. We need to ward off
                                                // cases like "true".
                                                // This is achieved using
                                                // try-catch and
                                                // retrieve the rValue from
                                                // characterOfSubGuard[1].

                                                try {

                                                    String rValue = characterOfSubGuard[1]
                                                            .trim();
                                                    int numberRetrival = 0;
                                                    boolean rvalueSingleNumber = true;
                                                    try {
                                                        numberRetrival = Integer
                                                                .parseInt(rValue);
                                                    } catch (Exception ex) {
                                                        rvalueSingleNumber = false;
                                                    }
                                                    if (rvalueSingleNumber == true) {

                                                        // We need to understand
                                                        // what is the operator
                                                        // of the value in
                                                        // order to reason
                                                        // the bound of
                                                        // the variable for
                                                        // suitable
                                                        // transition.

                                                        if (Pattern
                                                                .matches(
                                                                        ".*==.*",
                                                                        subGuardCondition)) {
                                                            // equal than,
                                                            // restrict the
                                                            // set of all
                                                            // possible
                                                            // values in the
                                                            // domain into
                                                            // one single
                                                            // value.

                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);

                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() != numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*!=.*",
                                                                        subGuardCondition)) {
                                                            // not equal
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() == numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*<=.*",
                                                                        subGuardCondition)) {
                                                            // less or equal than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() > numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*>=.*",
                                                                        subGuardCondition)) {
                                                            // greater or
                                                            // equal than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(lValue);

                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() < numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*>.*",
                                                                        subGuardCondition)) {
                                                            // greater than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() <= numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*<.*",
                                                                        subGuardCondition)) {
                                                            // less than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() >= numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);
                                                        }

                                                    }
                                                } catch (Exception ex) {
                                                    // We know that we can
                                                    // not parse the rvalue. Do
                                                    // nothing.
                                                }

                                            }

                                        }
                                    }
                                }

                            }

                            // setActions stores information about the
                            // update of the variable; outputActions stores
                            // information about the update of the variable that
                            // is going to be transmitted through the output
                            // port.

                            String setActionExpression = transition.setActions
                                    .getExpression();

                            if ((setActionExpression != null)
                                    && !setActionExpression.trim().equals("")) {
                                // Retrieve possible value of the variable
                                String[] splitExpression = setActionExpression
                                        .split(";");
                                for (int i = 0; i < splitExpression.length; i++) {
                                    String[] characters = splitExpression[i]
                                            .split("=");
                                    String lValue = characters[0].trim();
                                    String rValue = "";
                                    int numberRetrival = 0;
                                    boolean rvalueSingleNumber = true;
                                    try {
                                        rValue = characters[1].trim();
                                        numberRetrival = Integer
                                                .parseInt(rValue);
                                    } catch (Exception ex) {
                                        rvalueSingleNumber = false;
                                    }
                                    if (rvalueSingleNumber == true) {

                                        // Generate all possible conditions
                                        // that leads to this change.
                                        try {
                                            // set up all possible transitions
                                            // regarding to this assignment.

                                            String statePrecondition = new String(
                                                    ((FSMActor) innerModel)
                                                            .getName()
                                                            + "-"
                                                            + "state="
                                                            + stateInThis
                                                                    .getDisplayName());

                                            _generatePremiseAndResultEachTransition(
                                                    statePrecondition
                                                            + premiseRelated
                                                                    .toString(),
                                                    valueDomain,
                                                    ((FSMActor) innerModel)
                                                            .getName()
                                                            + "-" + lValue,
                                                    rValue, "N");

                                            _generatePremiseAndResultEachTransition(
                                                    statePrecondition
                                                            + premiseRelated
                                                                    .toString(),
                                                    valueDomain,
                                                    ((FSMActor) innerModel)
                                                            .getName()
                                                            + "-" + "state",
                                                    destinationInThis
                                                            .getDisplayName(),
                                                    "S");
                                        } catch (Exception ex) {

                                        }

                                    } else {
                                        // FIXME: The right hand side is
                                        // actually complicated expression which
                                        // needs to be carefully designed for
                                        // accepting various expression.
                                        // If we expect to do this, it is
                                        // necessary to construct a parse tree
                                        // and evaluate the value.
                                        // Currently let us assume that we
                                        // are manipulating simple format
                                        // a = a op constInt; or a = constInt;

                                        String[] rValueOperends = rValue
                                                .split("[+]|[-]|[*]|[/]");
                                        if (Pattern.matches(".*+.*", rValue)) {
                                            try {
                                                int value = Integer
                                                        .parseInt(rValueOperends[1]
                                                                .trim());
                                                // set up all possible
                                                // transitions regarding to this
                                                // assignment.

                                                String statePrecondition = new String(
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"
                                                                + "state="
                                                                + stateInThis
                                                                        .getDisplayName());

                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"

                                                                + lValue,
                                                        rValueOperends[1]
                                                                .trim(), "+");
                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-" + "state",
                                                        destinationInThis
                                                                .getDisplayName(),
                                                        "S");
                                            } catch (Exception ex) {

                                            }

                                        } else if (Pattern.matches(".*-.*",
                                                rValue)) {
                                            try {
                                                int value = Integer
                                                        .parseInt(rValueOperends[1]
                                                                .trim());
                                                // set up all possible
                                                // transitions
                                                // regarding to this
                                                // assignment.

                                                String statePrecondition = new String(
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"
                                                                + "state="
                                                                + stateInThis
                                                                        .getDisplayName());

                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"

                                                                + lValue,
                                                        rValueOperends[1]
                                                                .trim(), "-");
                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-" + "state",
                                                        destinationInThis
                                                                .getDisplayName(),
                                                        "S");
                                            } catch (Exception ex) {

                                            }
                                        } else if (Pattern.matches(".**.*",
                                                rValue)) {
                                            try {
                                                int value = Integer
                                                        .parseInt(rValueOperends[1]
                                                                .trim());
                                                // set up all possible
                                                // transitions
                                                // regarding to this
                                                // assignment.

                                                String statePrecondition = new String(
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"
                                                                + "state="
                                                                + stateInThis
                                                                        .getDisplayName());

                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition
                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"

                                                                + lValue,
                                                        rValueOperends[1]
                                                                .trim(), "*");
                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition
                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-" + "state",
                                                        destinationInThis
                                                                .getDisplayName(),
                                                        "S");
                                            } catch (Exception ex) {

                                            }
                                        } else if (Pattern.matches(".*/.*",
                                                rValue)) {
                                            try {
                                                int value = Integer
                                                        .parseInt(rValueOperends[1]
                                                                .trim());
                                                // set up all possible
                                                // transitions
                                                // regarding to this
                                                // assignment.

                                                String statePrecondition = new String(
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"
                                                                + "state="
                                                                + stateInThis
                                                                        .getDisplayName());

                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-"

                                                                + lValue,
                                                        rValueOperends[1]
                                                                .trim(), "/");
                                                _generatePremiseAndResultEachTransition(
                                                        statePrecondition

                                                                + premiseRelated
                                                                        .toString(),
                                                        valueDomain,
                                                        ((FSMActor) innerModel)
                                                                .getName()
                                                                + "-" + "state",
                                                        destinationInThis
                                                                .getDisplayName(),
                                                        "S");
                                            } catch (Exception ex) {

                                            }
                                        }

                                    }
                                }
                            } else {
                                // Note that there may be no setActions in
                                // the transition.
                                String statePrecondition = new String(
                                        ((FSMActor) innerModel).getName() + "-"
                                                + "state="
                                                + stateInThis.getDisplayName());
                                _generatePremiseAndResultEachTransition(
                                        statePrecondition
                                                + premiseRelated.toString(),
                                        valueDomain, ((FSMActor) innerModel)
                                                .getName()
                                                + "-" + "state",
                                        destinationInThis.getDisplayName(), "S");
                            }

                        }
                    }

                } catch (Exception ex) {

                }
            }
        }

        // Last, we need to perform the conjunction for cases like XX=1 and
        // XX_isPresent. In _variableTransitionInfo, the output value would not
        // change. It only perform restriction on premises.
        // FIXME: Now I remove into the premise generation step.

    }

    /**
     * This function is used to generate detailed pre-conditions and
     * post-conditions in .smv format. It is used by the function
     * _generateAllVariableTransitions()
     */
    private static void _generatePremiseAndResultEachTransition(
            String statePrecondition,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String offset, String operatingSign) throws IllegalActionException {

        // 1. If operatingSign=="N", then offset means the value that needs
        // to be assigned.
        // 2. if operatingSign=="S", then offset means the destination
        // vertex label.
        // 3. For rest cases (operatingSign=="+","-","*","/"), variable
        // has "X = X operatingSign offset".

        String[] keySetArray = (String[]) valueDomain.keySet().toArray(
                new String[0]);

        _recursiveStepGeneratePremiseAndResultEachTransition(statePrecondition,
                0, keySetArray.length, keySetArray, valueDomain, lValue,
                offset, operatingSign);

    }

    private static String _generatePremiseTransition(String statePrecondition,
            HashMap<String, ArrayList<Integer>> valueDomain)
            throws IllegalActionException {

        String[] keySetArray = (String[]) valueDomain.keySet().toArray(
                new String[0]);
        premiseText = new StringBuffer("");
        premiseText.append(_recursiveStepGeneratePremiseTransition(
                statePrecondition, 0, keySetArray.length, keySetArray,
                valueDomain));
        String returnText = premiseText.toString().trim();
        if (returnText.substring(0, 1).equalsIgnoreCase("|")) {
            return returnText.substring(1);
        }
        return returnText;
    }

    private static String _generateRelatedPremise(CompositeActor model,
            String variableNameUsed, FSMActor currentModel,
            HashSet<String> outerCoordinationVariableSet)
            throws IllegalActionException {

        StringBuffer returnPremise = new StringBuffer(" ");

        // Main Process to Perform Transition Generation.

        for (Iterator actors = (((CompositeActor) model).entityList())
                .iterator(); actors.hasNext();) {
            Entity innerModel = (Entity) actors.next();
            if (innerModel instanceof FSMActor) {
                if (innerModel.getName().equalsIgnoreCase(
                        currentModel.getName())) {
                    continue;
                }

                HashSet<State> stateSet = new HashSet<State>();
                try {
                    // Initialize
                    HashMap<String, State> frontier = new HashMap<String, State>();

                    // Create initial state
                    State stateInThis = ((FSMActor) innerModel)
                            .getInitialState();
                    String name = stateInThis.getName();
                    frontier.put(name, stateInThis);

                    // Iterate
                    while (!frontier.isEmpty()) {
                        // pick a state from frontier.
                        Iterator<String> iterator = frontier.keySet()
                                .iterator();
                        name = (String) iterator.next();
                        stateInThis = (State) frontier.remove(name);
                        if (stateInThis == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + name
                                            + "\" returned null?");
                        }
                        ComponentPort outPort = stateInThis.outgoingPort;
                        Iterator transitions = outPort.linkedRelationList()
                                .iterator();

                        while (transitions.hasNext()) {
                            // Retrieve the transition
                            Transition transition = (Transition) transitions
                                    .next();

                            State destinationInThis = transition
                                    .destinationState();

                            if (!stateSet.contains(destinationInThis)) {
                                frontier.put(name, destinationInThis);
                                stateSet.add(destinationInThis);
                            }

                            boolean hasAnnotation = false;
                            String text;
                            try {
                                text = transition.annotation.stringValue();
                            } catch (IllegalActionException e) {
                                text = "Exception evaluating annotation: "
                                        + e.getMessage();
                            }
                            if (!text.trim().equals("")) {
                                hasAnnotation = true;
                                // buffer.append(text);
                            }

                            // The first step is to analyze the output

                            String outputActionExpression = transition.outputActions
                                    .getExpression();

                            if ((outputActionExpression == null)
                                    || outputActionExpression.trim().equals("") == true) {
                                continue;
                            }

                            String[] splitOutputActionExpression = outputActionExpression
                                    .split(";");

                            boolean continueThisExpression = false;
                            if (splitOutputActionExpression.length != 0) {

                                for (int i = 0; i < splitOutputActionExpression.length; i++) {
                                    // Trim tab/space
                                    String subOutputAction = splitOutputActionExpression[i]
                                            .trim();
                                    // Split again to retrieve the lValue.
                                    String[] characterOfSubOutputAction = subOutputAction
                                            .split("=");
                                    String lValue = characterOfSubOutputAction[0]
                                            .trim();
                                    if (lValue
                                            .equalsIgnoreCase(variableNameUsed)) {
                                        continueThisExpression = true;
                                        break;
                                    }

                                }
                            }
                            if (continueThisExpression == false) {
                                continue;
                            }

                            String guard = transition.getGuardExpression();

                            // variableUsedInTransitionSet: Store variable
                            // names used in this transition as
                            // preconditions. If in the guard expression, we
                            // have X<3 && Y>5, then X and Y are used
                            // as variables in precondition and should be
                            // stored in the set "variableUsedInTransitionSet".

                            HashSet<String> variableUsedInTransitionSet = new HashSet<String>();

                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // FIXME: (2007/12/14 Patrick.Cheng)
                                    // Currently I don't know the meaning of
                                    // annotation. Do nothing currently.
                                } else {

                                    // Rule II. For all variables that are
                                    // used as guards, they would be
                                    // expanded as Atomic Propositions (AP).

                                    // Separate each guard expression into
                                    // "sub guard expressions".
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");

                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {
                                            // Trim tab/space
                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();

                                            // Retrieve the left value of
                                            // the inequality. Here we may still
                                            // have two cases for the lValue:
                                            // (1) XXX_isPresent (2) the
                                            // normal case (including "true").
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");

                                            String lValue = characterOfSubGuard[0]
                                                    .trim();
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {
                                                // String[] variableName =
                                                // characterOfSubGuard[0]
                                                // .trim().split(
                                                // "_isPresent");
                                                // The variable SHOULD NOT
                                                // have actor.getName() attached
                                                // in front of its name. This is
                                                // because this is XX_isPresent,
                                                // and we need to use XX to
                                                // match synchronizations.
                                                // variableUsedInTransitionSet
                                                // .add(variableName[0]);
                                            } else {
                                                // Store in the set. Use
                                                // try-catch to capture cases
                                                // when single "true" exists.

                                                try {
                                                    String rValue = characterOfSubGuard[1]
                                                            .trim();
                                                    // The variable SHOULD
                                                    // have actor.getName()
                                                    // attached in front of its
                                                    // name because this is a
                                                    // variable used in the
                                                    // Kripke structure.

                                                    variableUsedInTransitionSet
                                                            .add(((FSMActor) innerModel)
                                                                    .getName()
                                                                    + "-"
                                                                    + lValue);
                                                } catch (Exception ex) {

                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            // Once all variables used in the transition is
                            // listed, generate a list to estimate its domain.
                            // For example, if variable X has upper bound 5 and
                            // lower bound 1, then the result of the next step
                            // would show that variable X has a list with domain
                            // {1,2,3,4,5}.
                            // FIXME: We should apply {min, 1, 2, 3, 4, 5, max}
                            // instead ... (new discoveries by Patrick)
                            // But I haven't do this yet...

                            HashMap<String, ArrayList<Integer>> valueDomain = new HashMap<String, ArrayList<Integer>>();
                            Iterator<String> it = variableUsedInTransitionSet
                                    .iterator();
                            // Here in the variableUsedInTransitionSet, it has
                            // some variables attached with actor.getName() and
                            // some others without those name attachments.
                            while (it.hasNext()) {
                                String val = (String) it.next();
                                // Retrieve the value in the _variableInfo.
                                // contents in _variableInfo are previously
                                // generated by
                                VariableInfo variableInfo = _variableInfo
                                        .get(val);
                                // variableInfo may be null. This is because
                                // those values are like XX in XX_isPresent.
                                // They are used as synchronization only.
                                if (variableInfo != null) {
                                    int lowerBound = Integer
                                            .parseInt(variableInfo._minValue);
                                    int upperBound = Integer
                                            .parseInt(variableInfo._maxValue);
                                    // Set up the lower and upper bound of the
                                    // variable
                                    ArrayList<Integer> variableDomainForTransition = new ArrayList<Integer>();
                                    for (int number = lowerBound; number <= upperBound; number++) {
                                        // Place each possible value within
                                        // boundary into the list.
                                        variableDomainForTransition.add(Integer
                                                .valueOf(number));
                                    }
                                    valueDomain.put(val,
                                            variableDomainForTransition);
                                }

                            }

                            // After previous steps, for each variable now
                            // there exists a list with all possible values
                            // between lower bound and upper bound. Now perform
                            // the restriction process based on the guard
                            // expression. For example, if variable X has upper
                            // bound 5 and lower bound 1, and the guard
                            // expression says that X<3, then the domain would
                            // be restricted to only {1,2}.

                            if ((guard != null) && !guard.trim().equals("")) {
                                if (hasAnnotation) {
                                    // do nothing
                                } else {
                                    // Separate each guard expression into
                                    // substring
                                    String[] guardSplitExpression = guard
                                            .split("(&&)");
                                    if (guardSplitExpression.length != 0) {
                                        for (int i = 0; i < guardSplitExpression.length; i++) {

                                            String subGuardCondition = guardSplitExpression[i]
                                                    .trim();

                                            // Retrieve the left value of
                                            // the inequality.
                                            String[] characterOfSubGuard = subGuardCondition
                                                    .split("(>=)|(<=)|(==)|(!=)|[><]");
                                            // Here we may still have two
                                            // cases:
                                            // (1) XXX_isPresent
                                            // (2) the normal case.
                                            String lValue = characterOfSubGuard[0]
                                                    .trim();
                                            boolean b = Pattern.matches(
                                                    ".*_isPresent",
                                                    characterOfSubGuard[0]
                                                            .trim());
                                            if (b == true) {

                                            } else {
                                                // Check if the right value
                                                // exists. We need to ward off
                                                // cases like "true".
                                                // This is achieved using
                                                // try-catch and
                                                // retrieve the rValue from
                                                // characterOfSubGuard[1].

                                                try {

                                                    String rValue = characterOfSubGuard[1]
                                                            .trim();
                                                    int numberRetrival = 0;
                                                    boolean rvalueSingleNumber = true;
                                                    try {
                                                        numberRetrival = Integer
                                                                .parseInt(rValue);
                                                    } catch (Exception ex) {
                                                        rvalueSingleNumber = false;
                                                    }
                                                    if (rvalueSingleNumber == true) {

                                                        // We need to understand
                                                        // what is the operator
                                                        // of the value in
                                                        // order to reason
                                                        // the bound of
                                                        // the variable for
                                                        // suitable
                                                        // transition.

                                                        if (Pattern
                                                                .matches(
                                                                        ".*==.*",
                                                                        subGuardCondition)) {
                                                            // equal than,
                                                            // restrict the
                                                            // set of all
                                                            // possible
                                                            // values in the
                                                            // domain into
                                                            // one single
                                                            // value.

                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);

                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() != numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*!=.*",
                                                                        subGuardCondition)) {
                                                            // not equal
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() == numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*<=.*",
                                                                        subGuardCondition)) {
                                                            // less or equal
                                                            // than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() > numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*>=.*",
                                                                        subGuardCondition)) {
                                                            // greater or
                                                            // equal than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(lValue);

                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() < numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*>.*",
                                                                        subGuardCondition)) {
                                                            // greater than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() <= numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);

                                                        } else if (Pattern
                                                                .matches(
                                                                        ".*<.*",
                                                                        subGuardCondition)) {
                                                            // less than
                                                            ArrayList<Integer> domain = valueDomain
                                                                    .remove(((FSMActor) innerModel)
                                                                            .getName()
                                                                            + "-"
                                                                            + lValue);
                                                            for (int j = domain
                                                                    .size() - 1; j >= 0; j--) {
                                                                if (domain
                                                                        .get(j)
                                                                        .intValue() >= numberRetrival) {
                                                                    domain
                                                                            .remove(j);
                                                                }
                                                            }
                                                            valueDomain
                                                                    .put(
                                                                            ((FSMActor) innerModel)
                                                                                    .getName()
                                                                                    + "-"
                                                                                    + lValue,
                                                                            domain);
                                                        }

                                                    }
                                                } catch (Exception ex) {
                                                    // We know that we can
                                                    // not parse the rvalue. Do
                                                    // nothing.
                                                }

                                            }

                                        }
                                    }
                                }

                            }

                            String statePrecondition = new String(
                                    ((FSMActor) innerModel).getName() + "-"
                                            + "state="
                                            + stateInThis.getDisplayName());

                            returnPremise.append(_generatePremiseTransition(
                                    statePrecondition, valueDomain));

                        }
                    }

                } catch (Exception ex) {

                }
            }
        }
        String returnString = returnPremise.toString().trim();
        // eliminate the last "|" sign.
        if (returnString.length() == 0) {
            return "";
        }
        String s = returnString;

        return s;
    }

    private static StringBuffer _recursiveStepGeneratePremiseTransition(
            String currentPremise, int index, int maxIndex,
            String[] keySetArray,
            HashMap<String, ArrayList<Integer>> valueDomain)
            throws IllegalActionException {

        if (index >= maxIndex) {

            return new StringBuffer(" | " + currentPremise);

        } else {
            // if (index == 0) {
            // premiseText.append(currentPremise);
            // }

            ArrayList<Integer> vList = valueDomain.get(keySetArray[index]);
            if (vList.size() != 0) {
                for (int i = 0; i < vList.size(); i++) {

                    // retrieve the string and concatenate
                    String newPremise = new String(currentPremise + " & "
                            + keySetArray[index] + "="
                            + vList.get(i).toString());
                    return _recursiveStepGeneratePremiseTransition(newPremise,
                            index + 1, maxIndex, keySetArray, valueDomain);
                }
            } else {
                return _recursiveStepGeneratePremiseTransition(currentPremise,
                        index + 1, maxIndex, keySetArray, valueDomain);
            }

        }
        return new StringBuffer(currentPremise);
    }

    private static void _recursiveStepGeneratePremiseAndResultEachTransition(
            String currentPremise, int index, int maxIndex,
            String[] keySetArray,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String newVariableValue, String operatingSign)
            throws IllegalActionException {

        if (index >= maxIndex) {
            // Store in the array

            VariableTransitionInfo newTransitionInfo = new VariableTransitionInfo();
            newTransitionInfo._preCondition = currentPremise;
            newTransitionInfo._variableName = lValue;
            newTransitionInfo._varibleNewValue = newVariableValue;
            LinkedList<VariableTransitionInfo> temp = _variableTransitionInfo
                    .remove(lValue);
            temp.add(newTransitionInfo);
            _variableTransitionInfo.put(lValue, temp);

        } else {
            // retrieve all possible variable value in this stage, skip when
            // no possible value is needed.

            // See if this key corresponds to the lValue; if so we need to
            // record the new value of the outcome.
            if (keySetArray[index].equalsIgnoreCase(lValue)) {
                // update the newVariableValue based on +, -, *, /, and N.
                if (operatingSign.equalsIgnoreCase("+")) {
                    // vList stores all possible values for the variable
                    // that is possible to perform transition
                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) >= 0) {
                                // Offset positive/zero case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    // newpremise=currentPremise & (var = C)
                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    // When the original value is GT, then
                                    // GT + positive_const = GT
                                    // Hence the updated value remains the same.
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // For DOMAIN_LS, we place conservative
                                    // analysis and assert that it might lead to
                                    // all its possible values. For example, if
                                    // min=1, and offset=3, then possible value
                                    // may include LS, 1, 2.

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    // First, LS + positive_const = LS
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                    int minimumInBoundary = Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue);
                                    for (int j = 0; j < (Integer
                                            .parseInt(newVariableValue)); j++) {

                                        // We need to make sure that it would
                                        // never exceeds upper bound. If it
                                        // is below lower bound, we must stop it
                                        // and use GT to replace the value.

                                        if ((minimumInBoundary + j) > Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue)) {
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue, "gt",
                                                    operatingSign);
                                            break;
                                        }

                                        String updatedVariableValue = String
                                                .valueOf(minimumInBoundary + j);
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }

                                } else {
                                    // For ordinary cases, we only need to check
                                    // if the new value would exceeds the upper
                                    // bound. If so, then use DOMAIN_GT to
                                    // replace the value.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    + (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            + (Integer
                                                    .parseInt(newVariableValue)) > Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue)) {
                                        // Use DOMAIN_GT to replace the value.
                                        updatedVariableValue = new String("gt");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            } else {
                                // Offset negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_LS) {

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    // When the original value is LS, then
                                    // LS + negative_const = LS
                                    // Hence the updated value remains the same.
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    // When the original value is GT, we place
                                    // conservative analysis and assert that it
                                    // might lead to all its possible values.

                                    // First case: GT + negative_const = GT
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                    int maximumInBoundary = Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue);
                                    for (int j = 0; j > (Integer
                                            .parseInt(newVariableValue)); j--) {
                                        // here j-- because newVariableValue is
                                        // negative

                                        // We need to make sure that it would
                                        // never exceeds upper bound. If it
                                        // is below lower bound, we must stop it
                                        // and use LS to replace the value.

                                        if ((maximumInBoundary + j) < Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue)) {
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue, "ls",
                                                    operatingSign);
                                            break;
                                        }

                                        String updatedVariableValue = String
                                                .valueOf(maximumInBoundary + j);
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }

                                } else {
                                    // For ordinary cases, we only need to check
                                    // if the new value would exceeds the lower
                                    // bound. If so, then use DOMAIN_LS to
                                    // replace the value.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    + (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            + (Integer
                                                    .parseInt(newVariableValue)) < Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue)) {
                                        // Use DOMAIN_LS to replace the value.
                                        updatedVariableValue = new String("ls");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            }

                        }
                    } else {

                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("-")) {
                    // Cases when operating sign is minus.

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);

                    if (vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) >= 0) {
                                // Offset positive/zero case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // When the original value is LS, then
                                    // LS - positive_const = LS

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    // Hence the updated value remains the same.
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    // If original variable value is GT, we
                                    // place conservative analysis and assert
                                    // that it might lead to all its possible
                                    // values.

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    // First, it may keep to be GT
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                    int maximumInBoundary = Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue);
                                    for (int j = 0; j < (Integer
                                            .parseInt(newVariableValue)); j++) {

                                        // We need to make sure that it would
                                        // never exceeds upper bound. If it
                                        // is below lower bound, we must stop it
                                        // and use LS to replace the value.

                                        if ((maximumInBoundary - j) < Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue)) {
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue, "ls",
                                                    operatingSign);
                                            break;
                                        }

                                        String updatedVariableValue = String
                                                .valueOf(maximumInBoundary - j);
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }

                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceed the lower
                                    // bound. If so, then use DOMAIN_LS to
                                    // replace the value.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    - (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            - (Integer
                                                    .parseInt(newVariableValue)) < Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue)) {
                                        // Use DOMAIN_LS to replace the value.
                                        updatedVariableValue = new String("ls");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            } else {
                                // Offset negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {
                                    // GT - negative_const = GT

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    // Hence the updated value remains the same.
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // For DOMAIN_LS, we place conservative
                                    // analysis and assert that it might lead to
                                    // all its possible values

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    // First, LS - negative_const = LS
                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                    int minimumInBoundary = Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue);

                                    for (int j = 0; j > (Integer
                                            .parseInt(newVariableValue)); j--) {

                                        // We need to make sure that it would
                                        // never exceeds upper bound. If it
                                        // exceeds upper bound, we must stop it
                                        // and use GT to replace the value.

                                        if ((minimumInBoundary - j) < Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue)) {
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue, "gt",
                                                    operatingSign);
                                            break;
                                        }

                                        String updatedVariableValue = String
                                                .valueOf(minimumInBoundary - j);

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);

                                    }

                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceeds the upper
                                    // bound. If so, then use DOMAIN_GT to
                                    // replace the value.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    - (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            - (Integer
                                                    .parseInt(newVariableValue)) > Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue)) {
                                        // Use DOMAIN_LS to replace the value.
                                        updatedVariableValue = new String("gt");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            }
                        }

                    } else {
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("*")) {

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) > 0) {
                                // Positive case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    // newpremise = currentPremise & (var =
                                    // const)
                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue) >= 0) {
                                        // when max>=0, GT * positive_const = GT
                                        // Hence the updated value remains the
                                        // same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);
                                    } else {
                                        // Integer.parseInt(((VariableInfo)
                                        // _variableInfo.get(lValue))._maxValue)
                                        // < 0
                                        //
                                        // Starting from the upper bound + 1,
                                        // +2, +3, +4 ... calculate all possible
                                        // values until the new set-value is
                                        // greater than GT.
                                        //
                                        // For example, if upper bound is -5,
                                        // and if the offset is 2, then for
                                        // values in GT that is greater or equal
                                        // to -2, the new variable would be in
                                        // GT. But if the lower bound is -7,
                                        // then we need to replace cases that is
                                        // lower to -7. For example, -4*2=-8. We
                                        // should use LS to represent this
                                        // value.
                                        //
                                        // Also we expect to record one LS as
                                        // the new value only. So there are
                                        // tricks that needs to be applied.

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue) + 1;

                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue)) {
                                            if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) < Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._minValue))
                                                    && ((starter + 1)
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {
                                                // This IF statement represents
                                                // tricks mentioned above.
                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                            } else if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && (starter
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {
                                                String updatedVariableValue = String
                                                        .valueOf(starter
                                                                * Integer
                                                                        .parseInt(newVariableValue));
                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }

                                            starter++;

                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                    }

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue) <= 0) {
                                        // when min<=0, LS * positive_const = LS
                                        // Hence the updated value remains the
                                        // same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);
                                    } else {
                                        // Starting from the lower bound -1,
                                        // -2, -3, -4 ...
                                        // calculate all possible values until
                                        // the value is greater than LS.

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue) - 1;
                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue)) {
                                            if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) > Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && ((starter - 1)
                                                            * Integer
                                                                    .parseInt(newVariableValue) <= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._maxValue))) {

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                            } else if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && (starter
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {
                                                String updatedVariableValue = String
                                                        .valueOf(starter
                                                                * Integer
                                                                        .parseInt(newVariableValue));
                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }

                                            starter++;

                                        }
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                    }

                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceed the lower
                                    // or upper bound.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    * (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            * (Integer
                                                    .parseInt(newVariableValue)) < Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue)) {
                                        // Use DOMAIN_LS to replace the value.
                                        updatedVariableValue = new String("ls");

                                    } else if (vList.get(i).intValue()
                                            * (Integer
                                                    .parseInt(newVariableValue)) > Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue)) {
                                        updatedVariableValue = new String("gt");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            } else if (Integer.parseInt(newVariableValue) < 0) {
                                // Negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt");

                                    if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue) >= 0) {
                                        // Starting from the upper bound + 1,
                                        // +2, +3, +4 ...
                                        // calculate all possible values until
                                        // the value is less than LS.
                                        //
                                        // For example, if upper bound = 1,
                                        // lower bound = -7, and offset = -2,
                                        // then we might have possible new
                                        // set-values -4, -6, LS

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue) + 1;

                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue)) {

                                            String updatedVariableValue = String
                                                    .valueOf(starter
                                                            * Integer
                                                                    .parseInt(newVariableValue));
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);

                                            starter++;
                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                    } else if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue) < 0) {
                                        // One important thing is that we may
                                        // have cases where 0 * const = 0.
                                        // Because 0 is in GT, so we would have
                                        // new value GT as a choice.

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue) + 1;
                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue)) {
                                            if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) > Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && ((starter + 1)
                                                            * Integer
                                                                    .parseInt(newVariableValue) <= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._maxValue))) {

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                            } else if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && (starter
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {
                                                String updatedVariableValue = String
                                                        .valueOf(starter
                                                                * Integer
                                                                        .parseInt(newVariableValue));
                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }

                                            starter++;

                                        }
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                        // Special case where 0 * const = 0
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                    }

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // (Integer.parseInt(newVariableValue) < 0)
                                    // && original variable value == DOMAIN_LS

                                    String newPremise = new String(
                                            currentPremise + " & "
                                                    + keySetArray[index] + "="
                                                    + "ls");

                                    if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue) <= 0) {
                                        // Starting from the lower bound -1,
                                        // -2, -3, -4 ...
                                        // calculate all possible values until
                                        // the value is less than GT.
                                        //
                                        // For example, if upper bound = 7,
                                        // lower bound = -1, and offset = -2,
                                        // then we might have possible new
                                        // set-values 4, 6, GT

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue) - 1;

                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue)) {

                                            String updatedVariableValue = String
                                                    .valueOf(starter
                                                            * Integer
                                                                    .parseInt(newVariableValue));
                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);

                                            starter++;
                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                    } else if (Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._minValue) > 0) {
                                        // One important thing is that we may
                                        // have cases where 0 * const = 0.
                                        // Because 0 is in LS, so we would have
                                        // new value LS as a choice.

                                        int starter = Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._minValue) - 1;
                                        while (starter
                                                * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                .parseInt(((VariableInfo) _variableInfo
                                                        .get(lValue))._maxValue)) {
                                            if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) < Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._minValue))
                                                    && ((starter + 1)
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                            } else if ((starter
                                                    * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                    .parseInt(((VariableInfo) _variableInfo
                                                            .get(lValue))._maxValue))
                                                    && (starter
                                                            * Integer
                                                                    .parseInt(newVariableValue) >= Integer
                                                            .parseInt(((VariableInfo) _variableInfo
                                                                    .get(lValue))._minValue))) {
                                                String updatedVariableValue = String
                                                        .valueOf(starter
                                                                * Integer
                                                                        .parseInt(newVariableValue));
                                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }

                                            starter++;

                                        }
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                        // Special case where 0 * const = 0
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                    }

                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceeds the upper
                                    // bound. If so, then use DOMAIN_GT to
                                    // replace the value.

                                    String newPremise = new String(
                                            currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(
                                                            i).intValue()));

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    - (Integer
                                                            .parseInt(newVariableValue)));

                                    if (vList.get(i).intValue()
                                            - (Integer
                                                    .parseInt(newVariableValue)) > Integer
                                            .parseInt(((VariableInfo) _variableInfo
                                                    .get(lValue))._maxValue)) {
                                        // Use DOMAIN_LS to replace the value.
                                        updatedVariableValue = new String("gt");
                                    }

                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            } else {
                                // Integer.parseInt(newVariableValue)==0
                                // When offset is zero, the result should be
                                // zero. So we only need to check if zero
                                // exceeds the upper bound or is below the lower
                                // bound.

                                String newPremise = new String(currentPremise
                                        + " & "
                                        + keySetArray[index]
                                        + "="
                                        + String.valueOf(vList.get(i)
                                                .intValue()));

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    newPremise = new String(currentPremise
                                            + " & " + keySetArray[index] + "="
                                            + "ls");
                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    newPremise = new String(currentPremise
                                            + " & " + keySetArray[index] + "="
                                            + "gt");
                                }

                                String updatedVariableValue = new String("0");

                                if (0 > Integer
                                        .parseInt(((VariableInfo) _variableInfo
                                                .get(lValue))._maxValue)) {
                                    // Use DOMAIN_LS to replace the value.
                                    updatedVariableValue = new String("gt");
                                } else if (0 < Integer
                                        .parseInt(((VariableInfo) _variableInfo
                                                .get(lValue))._minValue)) {
                                    updatedVariableValue = new String("ls");
                                }

                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                        newPremise, index + 1, maxIndex,
                                        keySetArray, valueDomain, lValue,
                                        updatedVariableValue, operatingSign);
                            }
                        }
                    } else {
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("/")) {

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {
                            String updatedVariableValue = String.valueOf(vList
                                    .get(i).intValue()
                                    / (Integer.parseInt(newVariableValue)));
                            // retrieve the string and concatenate
                            String newPremise = new String(currentPremise
                                    + " & " + keySetArray[index] + "="
                                    + vList.get(i).toString());
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    newPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    updatedVariableValue, operatingSign);
                        }
                    } else {
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("N")) {

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {
                            String updatedVariableValue = newVariableValue;
                            // retrieve the string and concatenate
                            String newPremise = new String(currentPremise
                                    + " & " + keySetArray[index] + "="
                                    + String.valueOf(vList.get(i).intValue()));

                            if (vList.get(i).intValue() == DOMAIN_LS) {
                                newPremise = new String(currentPremise + " & "
                                        + keySetArray[index] + "=" + "ls");
                            } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                newPremise = new String(currentPremise + " & "
                                        + keySetArray[index] + "=" + "gt");
                            }
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    newPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    updatedVariableValue, operatingSign);
                        }
                    } else {
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                }

            } else {
                // meaning: if
                // (keySetArray[index].equalsIgnoreCase(lValue)==false)
                ArrayList<Integer> vList = valueDomain.get(keySetArray[index]);
                if (vList.size() != 0) {
                    for (int i = 0; i < vList.size(); i++) {

                        // retrieve the string and concatenate
                        String newPremise = new String(currentPremise + " & "
                                + keySetArray[index] + "="
                                + String.valueOf(vList.get(i).intValue()));

                        if (vList.get(i).intValue() == DOMAIN_LS) {
                            newPremise = new String(currentPremise + " & "
                                    + keySetArray[index] + "=" + "ls");
                        } else if (vList.get(i).intValue() == DOMAIN_GT) {
                            newPremise = new String(currentPremise + " & "
                                    + keySetArray[index] + "=" + "gt");
                        }
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                newPremise, index + 1, maxIndex, keySetArray,
                                valueDomain, lValue, newVariableValue,
                                operatingSign);
                    }
                } else {
                    _recursiveStepGeneratePremiseAndResultEachTransition(
                            currentPremise, index + 1, maxIndex, keySetArray,
                            valueDomain, lValue, newVariableValue,
                            operatingSign);
                }
            }

        }

    }

    private static HashMap<String, String> _retrieveVariableInitialValue(
            HashSet<String> variableSet, CompositeActor model) {

        HashMap<String, String> returnMap = new HashMap<String, String>();
        try {
            for (Iterator actors = (((CompositeActor) model).entityList())
                    .iterator(); actors.hasNext();) {
                Entity innerModel = (Entity) actors.next();
                if (innerModel instanceof FSMActor) {
                    ComponentPort outPort = ((FSMActor) innerModel)
                            .getInitialState().outgoingPort;
                    Iterator transitions = outPort.linkedRelationList()
                            .iterator();
                    while (transitions.hasNext()) {
                        Transition transition = (Transition) transitions.next();
                        String setActionExpression = transition.setActions
                                .getExpression();
                        if ((setActionExpression != null)
                                && !setActionExpression.trim().equals("")) {
                            // Retrieve possible value of the variable
                            String[] splitExpression = setActionExpression
                                    .split(";");
                            for (int i = 0; i < splitExpression.length; i++) {
                                String[] characters = splitExpression[i]
                                        .split("=");
                                String lValue = characters[0].trim();
                                String rValue = "";
                                int numberRetrival = 0;
                                boolean rvalueSingleNumber = true;
                                try {
                                    rValue = characters[1].trim();
                                    numberRetrival = Integer.parseInt(rValue);
                                } catch (Exception ex) {
                                    rvalueSingleNumber = false;
                                }
                                if (rvalueSingleNumber == true) {
                                    // see if the lValue is in variableSet
                                    if (variableSet
                                            .contains(((FSMActor) innerModel)
                                                    .getName()
                                                    + "-" + lValue)) {
                                        returnMap.put(((FSMActor) innerModel)
                                                .getName()
                                                + "-" + lValue, rValue);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } catch (Exception ex) {

        }

        return returnMap;
    }

    // BY PATRICK
    private static HashMap<String, VariableInfo> _variableInfo;
    private static HashMap<String, LinkedList<VariableTransitionInfo>> _variableTransitionInfo;

    private static StringBuffer premiseText;

    private static int DOMAIN_GT = Integer.MAX_VALUE;
    private static int DOMAIN_LS = Integer.MIN_VALUE;

    // /////////////////////////////////////////////////////////////////
    // // inner class ////
    private static class VariableInfo {
        private VariableInfo(String paraVariableName, String paraMax,
                String paraMin) {
            _variableName = paraVariableName;
            _maxValue = paraMax;
            _minValue = paraMin;
        }

        private String _variableName = null;
        private String _maxValue;
        private String _minValue;

    }

    // /////////////////////////////////////////////////////////////////
    // // inner class ////
    private static class VariableTransitionInfo {
        private VariableTransitionInfo() {

        }

        private String _preCondition;
        // Set of conditions that leads to the change of variable _variableName.
        private String _varibleNewValue = null;
        private String _variableName = null;

    }

}
