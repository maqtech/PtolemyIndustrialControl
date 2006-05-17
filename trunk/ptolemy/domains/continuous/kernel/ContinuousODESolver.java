/* The abstract base class of the ODE solvers.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel;

import ptolemy.domains.ct.kernel.CTMultiSolverDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ODESolver

/**
 Abstract base class for ODE solvers. CT directors call these methods to
 resolve the initial states in a future time in the continuous phase
 of execution of a complete iteration. See {@link
 CTMultiSolverDirector} for explanation of initial states and phases
 of executions. The process of resolving the initial states in a
 future time is also known as an integration. A complete integration
 is composed of one or more rounds of executions. One round of
 execution consists of calling fireDynamicActors() once followed by
 calling fireStateTransitionActors() once. How the states are
 resolved are solver dependent. Derived classes need to implement
 these methods according to their ODE solving algorithms.
 <P>
 The behavior of integrators also changes when changing the ODE solver,
 so this class provides some methods for the integrators too, including the
 fire() method and the step size control related methods. Here we use the
 strategy and delegation design patterns. CTBaseIntegrator delegates its
 corresponding methods to this class. And subclasses of this class provide
 concrete implementations of these methods.
 <P>
 How many rounds are needed in one integration is solver dependent. For some
 solving algorithms, (i.e. the so called explicit methods) the number of
 rounds is fixed. For some others (i.e. implicit methods), the number of
 rounds can not be decided beforehand.
 <P>
 A round counter is a counter for the number of rounds in one integration.
 It helps the solvers to decide how to behave under different rounds.
 The round counter can be retrieved by the _getRoundCount() method.
 The _incrementRoundCount() method will increase the counter by one,
 and _resetRoundCount() will always reset the counter to 0. These methods are
 protected because they are only used by solvers and CT directors.
 <p>
 In this class, two methods {@link #_isConverged} and {@link
 #_voteForConverged} are defined to let CT directors know the status
 of resolved states. If multiple integrators exist, only when all of
 them vote true for converged, will the _isConverged() return
 true. Another related method is {@link #resolvedStates()}, which
 always returns true in this base class. However, in the solvers
 that implement the implicit solving methods, this method may return
 false if the maximum number of iterations is reached but states
 have not been resolved.
 <P>
 Conceptually, ODE solvers do not maintain simulation parameters,
 like step sizes and error tolerance.
 They get these parameters from the director. So the same set of parameters
 are shared by all the solvers in a simulation.

 @author Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public abstract class ContinuousODESolver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the amount of history information needed by this solver.
     *  Some solvers need history information from each integrator.
     *  The derived class should implement this method to return the
     *  number of history information needed so that the integrator can
     *  prepare for that in advance. In particular, if a solver needs no
     *  history information, this method returns 0.
     *  @return The amount of history information needed.
     */
    public abstract int getAmountOfHistoryInformation();

    /** Return the director that contains this solver.
     *  @return the director that contains this solver.
     */
    public final NamedObj getContainer() {
        return _director;
    }

    /** Return the number of auxiliary variables that an integrator should
     *  provide when solving the ODE. Auxiliary variables are variables
     *  in integrators to store integrator-dependent intermediate results
     *  when solving an ODE.
     *  <br>
     *  For example, the fixed-step solvers need 0 auxiliary variable, but
     *  the RK23 solver needs 4 auxiliary variables to store the temporary
     *  derivatives at different time points during an integration.
     *  @return The number of auxiliary variables.
     */
    public abstract int getIntegratorAuxVariableCount();

    /** Perform one integration step. The fire() method of integrators
     *  delegates to this method. Derived classes need to implement
     *  the details.
     *  @param integrator The integrator that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void integratorFire(ContinuousIntegrator integrator)
            throws IllegalActionException;

    /** Return true if the current integration step is accurate from the
     *  argument integrator's point of view. The integratorIsAccurate() method
     *  of integrators delegates to this method.
     *  Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return True if the integrator finds the step accurate.
     */
    public abstract boolean integratorIsAccurate(ContinuousIntegrator integrator);

    /** The predictedStepSize() method of the integrator delegates to this
     *  method. Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return The suggested next step size by the given integrator.
     */
    public abstract double integratorPredictedStepSize(ContinuousIntegrator integrator);

    /** Return true if the states of the system have been resolved
     *  successfully.
     *  In this base class, always return true. Derived classes may change
     *  the returned value.
     *  @return True If states of the system have been resolved successfully.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean resolvedStates() throws IllegalActionException {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Report a debug message via the director.
     *  @param message The message.
     */
    protected void _debug(String message) {
        _director._reportDebugMessage(message);
    }

    /** Increment the round and return the time increment associated
     *  with the round.
     *  @return The time increment associated with the next round.
     */
    protected abstract double _incrementRound() throws IllegalActionException; 
    
    /** Return true if debugging is turned on in the director.
     *  @return True if debugging is turned on.
     */
    protected final boolean _isDebugging() {
        return _director._isDebugging();
    }

    /** Return true if the current integration step is finished. For example,
     *  solvers with a fixed number of rounds in an integration step will
     *  return true when that number of rounds are complete. Solvers that
     *  iterate to a solution will return true when the solution is found.
     *  @return Return true if the solver has finished an integration step.
     */
    protected abstract boolean _isStepFinished();

    /** Make this solver the solver of the given Director. This method
     *  should only be called by CT directors, when they instantiate solvers
     *  according to the ODESolver parameters.
     *  @param director The CT director that contains this solver.
     */
    protected void _makeSolverOf(ContinuousDirector director) {
        _director = director;
    }

    /** Reset the solver, indicating to it that we are starting an
     *  integration step. This method sets a flag indicating that
     *  we have not converged to a solution.
     */
    protected abstract void _reset();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Cont director that contains this solver. */
    private ContinuousDirector _director = null;
}
