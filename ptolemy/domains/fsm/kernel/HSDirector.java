/* An HSDirector governs the execution of the discrete dynamics of a
   hybrid system model.

   Copyright (c) 1999-2004 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTGeneralDirector;
import ptolemy.domains.ct.kernel.CTReceiver;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.ExtendedMath;

//////////////////////////////////////////////////////////////////////////
//// HSDirector
/**
   An HSDirector governs the execution of the discrete dynamics of a hybrid
   system model.
   <p>
   <a href="http://ptolemy.eecs.berkeley.edu/publications/papers/99/hybridsimu/">
   Hierarchical Hybrid System Simulation</a> describes how hybrid system models
   are built and simulated in Ptolemy II.
   <p>
   Note: this class is still under development.

   @author Xiaojun Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (liuxj)
   @Pt.AcceptedRating Red (liuxj)
*/
public class HSDirector extends FSMDirector implements CTTransparentDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HSDirector() {
        super();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public HSDirector(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate the enbled refinenents to emit the tentative outputs.
     *  @exception IllegalActionException If the data transfer is not
     *       completed.
     */
    public void emitTentativeOutputs() throws IllegalActionException {
        Iterator actors = _enabledRefinements.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor)actor).emitTentativeOutputs();
            }
        }
    }

    /** Set the values of input variables in the mode controller. Examine
     *  the preemptive outgoing transitions of its current state. Throw an
     *  exception if there is more than one transition enabled. If there
     *  is exactly one preemptive transition enabled then it is chosen and
     *  the choice actions contained by the transition are executed. The
     *  refinement of the current state of the mode controller is not fired.
     *  If no preemptive transition is enabled and the refinement is ready
     *  to fire in the current iteration, fire the refinement. The
     *  non-preemptive transitions from the current state of the mode
     *  controller are examined. If there is more than one transition
     *  enabled, an exception is thrown. If there is exactly one
     *  non-preemptive transition enabled then it is chosen and the choice
     *  actions contained by the transition are executed.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or there is no controller, or thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        // FIXME: this basically copies the fire method of FSMDirector.
        // It will be cleaned by introducing an abstract modal model director.
        if (_debugging) {
            _debug(getName(), " fire.");
        }

        // If this is the first time to fire in an iteration,
        // reconstruct the list of enabled refinenents from the cuent state.
        // FIXME: prefire does this too.... why do this again here?
        if (_firstFire) {
            Actor[] actors = _st.getRefinement();
            _enabledRefinements = new LinkedList();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (actors[i].prefire()) {
                        _enabledRefinements.add(actors[i]);
                    }
                }
            }
            _firstFire = false;
        }

        _ctrl._readInputs();

        if (_debugging) _debug(getName(), " find FSMActor " + _ctrl.getName());
        Transition tr;

        // only check enabled transitions during a discrete-phase execution
        // and the end of a continuous phase execution where the accuracy of the
        // current step size is checked.
        if ((getExecutionPhase() ==
            CTExecutionPhase.FIRINGEVENTGENERATORS_PHASE) ||
            (getExecutionPhase() ==
            CTExecutionPhase.GENERATINGEVENTS_PHASE) ||
            (getExecutionPhase() ==
            CTExecutionPhase.FIRINGPURELYDISCRETE_PHASE)) {
            tr = _ctrl._chooseTransition(_st.preemptiveTransitionList());
        } else {
            tr = null;
        }

        // record the enabled preemptive transition
        _enabledTransition = tr;

        // FIXME: The refinements of a transition can not and must not
        // advance time. Will it be safe to iterate the refinements?
        if (tr != null) {
            Actor[] actors = tr.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) break;
                    if (actors[i].prefire()) {
                        actors[i].fire();
                        actors[i].postfire();
                    }
                }
            }
            _ctrl._readOutputsFromRefinement();
            // An enabled preemptive transition preempts the
            // firing of the enabled refienements.
            return;
        }

        Iterator actors = _enabledRefinements.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_debugging) _debug(getName(), " fire refinement",
                    ((ptolemy.kernel.util.NamedObj)actor).getName());
            actor.fire();
        }

        _ctrl._readOutputsFromRefinement();

        // only check enabled transition during event generating phase.
        if ((getExecutionPhase() ==
            CTExecutionPhase.FIRINGEVENTGENERATORS_PHASE) ||
            (getExecutionPhase() ==
            CTExecutionPhase.GENERATINGEVENTS_PHASE) ||
            (getExecutionPhase() ==
            CTExecutionPhase.FIRINGPURELYDISCRETE_PHASE)) {
            // Note that the output actions associated with the transition
            // are executed.
            tr = _ctrl._chooseTransition(_st.nonpreemptiveTransitionList());
        } else {
            tr = null;
        }

        // record the enabled nonpreemptive transition
        _enabledTransition = tr;

        // execute the refinements of the enabled transition
        if (tr != null) {
            Actor[] transitionActors = tr.getRefinement();
            if (transitionActors != null) {
                for (int i = 0; i < transitionActors.length; ++i) {
                    if (_stopRequested) break;
                    if (transitionActors[i].prefire()) {
                        if (_debugging) {
                            _debug(getFullName(),
                                    " fire transition refinement",
                                    ((ptolemy.kernel.util.NamedObj)transitionActors[i]).getName());
                        }
                        transitionActors[i].fire();
                        transitionActors[i].postfire();
                    }
                }
                _ctrl._readOutputsFromRefinement();
            }

            // execute the output actions, since these are normally
            // executed in chooseTransition, but the outputs may
            // have been changed by the transition refinemenets

            // FIXME: which happens first? the refinement execution
            // or the actions associated with the transitions?
            // FIXME: the semantics here is really ...!
            Iterator actions = tr.choiceActionList().iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                action.execute();
            }
            _ctrl._readOutputsFromRefinement();
        }
        return;
    }

    /** Ask for the current step size used by the solver from the
     *  enclosing CT director.
     *  @return The current step size.
     */
    public double getCurrentStepSize() {
        CTGeneralDirector executiveDirector =
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getCurrentStepSize();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with " +
                "an HSDirector must be used inside a CT model.");
        }
    }

    /** Return the current time obtained from the executive director, if
     *  there is one, and otherwise return the local view of current time.
     *  @return The current time.
     */
    public double getCurrentTime() {
        return getModelTime().getDoubleValue();
    }

    /** Return the enclosing CT director of this director, or null if
     *  this director is at the top level or the enclosing director is
     *  not a CT general director.
     *
     *  @return The enclosing CT general director of this director, if there
     *  is any.
     */
    public CTGeneralDirector getEnclosingCTGeneralDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector instanceof CTGeneralDirector) {
            return (CTGeneralDirector) executiveDirector;
        } else {
            return null;
        }
    }

    /** Get the current execution phase of this director.
     *  @return The current execution phase of this director.
     */
    public CTExecutionPhase getExecutionPhase() {
        CTGeneralDirector executiveDirector =
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getExecutionPhase();
        } else {
            // For any executive director that is not CTGeneralDirector,
            // the current execution phase is always FIRINGPURELYDISCRETE_PHASE.
            return CTExecutionPhase.FIRINGPURELYDISCRETE_PHASE;
        }
    }

    /** Return the begin time of the current iteration.
     *  @return The begin time of the current iteration.
     */
    public Time getIterationBeginTime() {
        CTGeneralDirector executiveDirector =
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getIterationBeginTime();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with " +
                "an HSDirector must be used inside a CT model.");
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public Time getModelNextIterationTime() {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        return execDir.getModelNextIterationTime();
    }

    /** Return the current time obtained from the executive director, if
     *  there is one, and otherwise return the local view of current time.
     *  @return The current time.
     */
    public Time getModelTime() {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        if (execDir != null) {
            return execDir.getModelTime();
        } else {
            return super.getModelTime();
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Return the ODE solver.
     *  @return The default ODE solver associated with this director.
     */
    public ODESolver getODESolver() {
        CTGeneralDirector executiveDirector =
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getODESolver();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with " +
                "an HSDirector must be used inside a CT model.");
        }
    }

    /** Restore the states of all the enabled refinements to the
     *  previously marked states.
     */
    public void goToMarkedState() throws IllegalActionException {
        Iterator actors = _enabledRefinements.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor)actor).goToMarkedState();
            }
        }
    }

    /** Return true if the enabled refinements may produce events.
     *  @return True if the emabled refinements may produce events.
     */
    public boolean hasCurrentEvent() {
        // FIXME: delegate the method to the enabled refinement.
        boolean eventPresent = false;
        Iterator actors = _enabledRefinements.iterator();
        while (!eventPresent && actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                eventPresent |=
                    ((CTCompositeActor)actor).hasCurrentEvent();
            }
        }
        return eventPresent;
    }

    /** Call the initialize method of the supper class. Get the controller
     *  and the current state. Get a set of the refinements associated
     *  with this state.
     *
     *  @exception IllegalActionException If the enabled refinements or
     *  the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _ctrl = getController();
        _st = _ctrl.currentState();
        _enabledRefinements = new LinkedList();
        Actor[] actors = _st.getRefinement();
         if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                actors[i].initialize();
                _enabledRefinements.add(actors[i]);
            }
        }
    }

    /** Return true if this is the discrete phase execution.
     *  @return True if this is the discrete phase execution.
     */
    public boolean isDiscretePhase() {
        CTGeneralDirector executiveDirector =
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().isDiscretePhase();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with " +
                "an HSDirector must be used inside a CT model.");
        }
    }

    /** Retun true if all the output-step-size-control actors of the enabled
     *  refinements are satisfied with the current step size and there is
     *  no enabled transition detected.
     *  @return True if all the refinements are satisfied with the
     *  current step size and there is no enabled transition detected.
     */
    public boolean isOutputAccurate() {
        boolean result = true;

        // Iterate all the enabled refinements to see whether they are
        // satisfied with the current step size.
        CTDirector dir= (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = result && ((CTStepSizeControlActor)
                            refinement).isOutputAccurate();
                }
            }
        }

        // Even if the result is false, this method does not return immediately.
        // Instead, we continue checking whether there is any transition
        // enabled with the current (temporary) outputs.
        // The reason is that when refining step size, we want to find the
        // largest step size that satisfies all the constraints to reduce the
        // computation cost.
        // NOTE: Both the non-preemptive and preemptive transitions are checked.
        try {
            // Check if there is any preemptive transition enabled.
            Transition preemptiveTr
                = _ctrl._checkTransition(_st.nonpreemptiveTransitionList());
            if (preemptiveTr != null) {
                if (_debugging) {
                    _debug("Find enabled transition:  " +
                            preemptiveTr.getGuardExpression());
                }
            }

            // Check if there is any non-preemptive transition enabled.
            Transition nonPreemptiveTr
                = _ctrl._checkTransition(_st.nonpreemptiveTransitionList());
            if (nonPreemptiveTr != null) {
                if (_debugging) {
                    _debug("Find enabled transition:  " +
                            nonPreemptiveTr.getGuardExpression());
                }
            }

            // Check if there is any event detected for preemptive transitions.
            Transition preemptiveTrWithEvent =
                _checkEvent(_st.nonpreemptiveTransitionList());
            if (preemptiveTrWithEvent != null) {
                if (_debugging) {
                    _debug("Detected event for transition:  " +
                            preemptiveTrWithEvent.getGuardExpression());
                }
            }

            // Check if there is any events detected for
            // nonpreemptive transitions.
            Transition nonPreemptiveTrWithEvent =
                _checkEvent(_st.nonpreemptiveTransitionList());
            if (nonPreemptiveTrWithEvent != null) {
                if (_debugging) {
                    _debug("Detected event for transition:  " +
                            nonPreemptiveTrWithEvent.getGuardExpression());
                }
            }

            // If no transition is enabled, set "tr" as the transition
            // with event detected.
            if (preemptiveTr == null) {
                preemptiveTr= preemptiveTrWithEvent;
            }
            if (nonPreemptiveTr == null) {
                nonPreemptiveTr= nonPreemptiveTrWithEvent;
            }

            // If there is no transition enabled, the last step size is accurate for
            // transitions. The status of the relations of the guard expressions are
            // committed into all the associated relation lists.
            // FIXME: this is wrong. It is too early to tell whether current
            // step size is accurate because the refinements may not be
            // satisfied with the current step size. What is more, the other
            // actors that at the same level of hierarchy as this modal model
            // may not be satisfied the current step size. The states should
            // be committed at the postfire method.
            if (nonPreemptiveTr == null && preemptiveTr == null) {
                return result;
            } else if (nonPreemptiveTr != null) {
                // There is one non-preemptive transition enabled.
                // We check the maximum difference of all relations that change
                // their status with the current step size for
                // step size refinement.
                RelationList relationList = nonPreemptiveTr.getRelationList();
                _distanceToBoundaryNonPreemptive = relationList.maximumDifference();

                _nonpreemptiveTransitionAccurate =
                    (_distanceToBoundaryNonPreemptive < dir.getErrorTolerance());

                if (_debugging) {
                    _debug(" ==> The guard " +
                            nonPreemptiveTr.getGuardExpression() +
                            " has difference " + _distanceToBoundaryNonPreemptive);
                }

                if (!_nonpreemptiveTransitionAccurate) {
                    // Retrive the previous distance of the relation which has the
                    // biggest difference with the current step size.
                    // The former distance will be used to refine the step size.
                    _lastDistanceToBoundaryNonPremptive = relationList.getFormerMaximumDistance();
                }
            } else {
                // There is one preemptive transition enabled.
                // We check the maximum difference of all relations that change
                // their status with the current step size for
                // step size refinement.
                RelationList relationList = nonPreemptiveTr.getRelationList();
                _distanceToBoundaryPreemptive = relationList.maximumDifference();

                _preemptiveTransitionAccurate =
                    (_distanceToBoundaryPreemptive < dir.getErrorTolerance());

                if (_debugging) {
                    _debug(" ==> The guard " +
                            nonPreemptiveTr.getGuardExpression() +
                            " has difference " + _distanceToBoundaryPreemptive);
                }

                if (!_preemptiveTransitionAccurate) {
                    // Retrive the previous distance of the relation which has the
                    // biggest difference with the current step size.
                    // The former distance will be used to refine the step size.
                    _lastDistanceToBoundaryPremptive = relationList.getFormerMaximumDistance();
                }
            }
            return result && _nonpreemptiveTransitionAccurate
                && _preemptiveTransitionAccurate;
        } catch (Exception e) {
            //FIXME: handle the exception
            System.out.println(
                    "FIXME:: HSDirector.isThisStepAccurate() throws exception ");
            e.printStackTrace();
            return result;
        }
    }


    /** Retun true if all the refinements can resolve their states with the
     *  current step size.
     *  @return True if all the refinements can resolve their states with the
     *  current step size.
     */
    public boolean isStateAccurate() {
        boolean result = true;
        CTDirector dir= (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = result && ((CTStepSizeControlActor)
                            refinement).isStateAccurate();
                }
            }
        }
        return result;
    }

    /** Return true if there are no refinements, or if the current
     *  integration step is accurate with the respect of all the enabled
     *  refinements, which are refinements that returned true in their
     *  prefire() methods in this iteration, or if a refinement is not a
     *  CTStepSizeControlActor; and if the current time is exactly the same
     *  time the transition is enabled.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        return isOutputAccurate() && isStateAccurate();
    }

    // FIXME: the following methods are to support CT domains only.
    // They are not fully developed and commented. They are related to
    // CTEmbeddedDirector. Actually, most methods are the same.

    /** Make the current states of all the enabled refinements.
     */
    public void markState() {
        Iterator actors = _enabledRefinements.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor)actor).markState();
            }
        }
    }

    /** Return a CTReceiver. By default, the signal type is discrete.
     *  @return a new CTReceiver with signal type as discrete.
     */
    public Receiver newReceiver() {
        CTReceiver receiver = new CTReceiver();
        receiver.setSignalType(CTReceiver.DISCRETE);
        return receiver;
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the enabled refinements of the
     *  current state
     *  of the mode controller and take out event outputs that the
     *  refinements generate. Execute the commit actions contained
     *  by the last chosen transition of the mode controller and set
     *  its current state to the destination state of the transition.
     *  Clear the relation list associated with the enabled transition
     *  and request to be fired again at the current time.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any action, or
     *   there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        Director dir= container.getExecutiveDirector();
        Iterator refinements = _enabledRefinements.iterator();
        while (refinements.hasNext()) {
            Actor refinement = (Actor)refinements.next();
            refinement.postfire();
            // take out event outputs generated in ref.postfire()
            Iterator outports = refinement.outputPortList().iterator();
            while (outports.hasNext()) {
                IOPort p = (IOPort)outports.next();
                transferOutputs(p);
            }
            _ctrl._readOutputsFromRefinement();
        }

        Transition tr = _enabledTransition;

        // If there is one transition enabled, the HSDirector requests
        // fire again at the same time to see whether the next state
        // has some outgoing transition enabled.
        if (tr != null) {
            if (_debugging) {
                _debug("Postfire deals with enabled transition " +
                        tr.getGuardExpression());
            }
            Iterator iterator = _st.nonpreemptiveTransitionList().listIterator();
            // It is important to clear the history information of the
            // relation list since after this breakpoint, no history
            // information is valid.
            while (iterator.hasNext()) {
                ((Transition) iterator.next()).getRelationList().clearRelationList();
            }
            // If the top level of the model is modal model, the director
            // is null. We do not request to fire again since no one in upper
            // hierarchy will do that.
            if (dir != null) {
                if (_debugging) {
                    _debug("HSDirector requests refiring at " + getModelTime());
                }
                dir.fireAt(container, getModelTime());
            }
        } else {
            // Otherwise, commit the current states of all the transitions.
            Iterator iterator = _st.nonpreemptiveTransitionList().listIterator();
            while (iterator.hasNext()) {
                ((Transition) iterator.next()).
                    getRelationList().commitRelationValues();
            }
            iterator = _st.preemptiveTransitionList().listIterator();
            while (iterator.hasNext()) {
                ((Transition) iterator.next()).
                    getRelationList().commitRelationValues();
            }
        }
        // clear the cached enabled transition
        _enabledTransition = null;
        return super.postfire();
    }

    /** Return the smallest next step size predicted by the all the
     *  enabled refinements, which are refinements that returned true
     *  in their prefire() methods in this iteration.
     *  If there are no refinements, then return Double.MAX_VALUE.
     *  If a refinement is not a CTStepSizeControlActor, then
     *  its prediction is Double.MAX_VALUE.
     *  @return The predicted next step size.
     */
    public double predictedStepSize() {
        double result = Double.MAX_VALUE;
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor)refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result, ((CTStepSizeControlActor)
                                              refinement).predictedStepSize());
                }
            }
        }
        return result;
    }

    /** Set the controller. Call super.prefire().
     */
    public boolean prefire() throws IllegalActionException {
        _ctrl = getController();
        _st = _ctrl.currentState();

        Actor[] actors = _st.getRefinement();
        _enabledRefinements = new LinkedList();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                if (actors[i].prefire()) {
                    _enabledRefinements.add(actors[i]);
                }
            }
        }
        _preemptiveTransitionAccurate = true;
        _nonpreemptiveTransitionAccurate = true;
        return super.prefire();
    }

    /** Return the step size refined by all the enabled refinements,
     *  which are refinements that returned true
     *  in their prefire() methods in this iteration, or the enabled
     *  transition which requires the current time be the same with
     *  the time it is enabled.
     *  If there are no refinements, or no refinement is a
     *  CTStepSizeControlActor, then the refined step size is the smaller
     *  value between current step size of the executive director and
     *  refined step size from enabled transition.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        CTDirector dir= (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
        double result = dir.getCurrentStepSize();
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor)refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result, ((CTStepSizeControlActor)
                                              refinement).refinedStepSize());
                }
            }
        }

        if (_preemptiveTransitionAccurate && _nonpreemptiveTransitionAccurate) {
            return result;
        } else {
            double refinedStepSizeNonPreemptive = result;
            double refinedStepSizePreemptive = result;
            double errorTolerance = dir.getErrorTolerance();
            double currentStepSize = dir.getCurrentStepSize();

            // Linear interpolation to refine the step size.
            // Note the step size is refined such that the distanceToBoundary
            // is half of errorTolerance.
            if (!_nonpreemptiveTransitionAccurate) {
            refinedStepSizeNonPreemptive = currentStepSize * (_lastDistanceToBoundaryNonPremptive + errorTolerance/2)
                / (_lastDistanceToBoundaryNonPremptive + _distanceToBoundaryNonPreemptive);
            }

            if (!_preemptiveTransitionAccurate) {
                refinedStepSizePreemptive = currentStepSize * (_lastDistanceToBoundaryPremptive + errorTolerance/2)
                / (_lastDistanceToBoundaryPremptive + _distanceToBoundaryPreemptive);
            }

            double refinedStepSize = Math.min(
                    refinedStepSizeNonPreemptive, refinedStepSizePreemptive);
            if (refinedStepSize > result) {
                return result;
            } else {
                return refinedStepSize;
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    // FIXME: where is the right position to put this method?
    // This method detects any events happened during one step size.
    private Transition _checkEvent(List transitionList) {
        Transition result = null;
        Iterator transitionRelations = transitionList.iterator();
        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (transition.getRelationList().hasEvent()) {
                return transition;
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached reference to mode controller.
    private FSMActor _ctrl = null;

    // Lcoal variable to indicate the distance to boundary.
    private double _distanceToBoundaryNonPreemptive = 0.0;

    private Transition _enabledTransition;

    // Lcoal variable to indicate the last distance to boundary.
    private double _lastDistanceToBoundaryNonPremptive = 0.0;

    // Lcoal variable to indicate the distance to boundary.
    private double _distanceToBoundaryPreemptive = 0.0;

    // Lcoal variable to indicate the last distance to boundary.
    private double _lastDistanceToBoundaryPremptive = 0.0;

    // Lcoal variable to indicate the last step size.
    private double _lastStepSize = 0.0;

    // Cached reference to current state.
    private State _st = null;

    // Lcoal variable to indicate whether
    // the enabled preemptive transition is accurate.
    private boolean _preemptiveTransitionAccurate = true;

    // Lcoal variable to indicate whether the
    // the enabled nonpreemptive transition is accurate.
    private boolean _nonpreemptiveTransitionAccurate = true;

