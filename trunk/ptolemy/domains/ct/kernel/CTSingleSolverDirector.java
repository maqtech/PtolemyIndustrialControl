/* A CTDirector that uses only one ODE solver.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTSingleSolverDirector
/**
A CTDirector that uses only one ODE solver. The solver is a parameter
of the director called "ODESolver". The default solver is the 
ForwardEulerSoler.
The solver of this director must be able to self start, so any solver
that uses history points can not be the solver for this director. 
<P>
This director can handle explicit breakpoints, which are breakpoints
that are registered in the breakpoint table. It does not handle 
unexpected breakpoints like event detections.  This director can
only be a top-level director. Since impulse backward Euler method 
does not advance time, it should not be used as the solver for this
director. As a result, if the system contains impulse sources,
this director is not applicable. Please use CTMultiSolverDirector with
ImpulseBESolver as the breakpoint solver for better result.
<P>
Each iteration of the director simulates the system for one step.
The size of the step is determined by the ODE solver as well as 
the breakpoints. After each iteration, the execution control will be
returned to the manager, where possible mutations are taken care of.
At the end of the simulation, the postfire() method will return false,
telling the manager that the simulation finished.
 

@author Jie Liu
@version $Id$
*/
public class CTSingleSolverDirector extends CTDirector {


    /** Construct a CTDirector with no name and no Container.
     *  All the parameters takes their default values.
     */
    public CTSingleSolverDirector () {
        super();
        this._initParameters();
    }

    /** Construct a CTDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param name The name of this director.
     */
    public CTSingleSolverDirector (String name) {
        super(name);
        this._initParameters();
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTSingleSolverDirector (Workspace workspace, String name) {
        super(workspace, name);
        this._initParameters();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**  Fire the system for one iteration. One iteration is defined as
     *   simulating the system at one time point, which includes
     *   resolving states and producing outputs. For the first iteration
     *   it only produces the output, since the initial states are
     *   the "real" states of the system, and no more resolving is needed.
     *   The step size of one iteration is determined by the suggested
     *   next step size and the breakpoints. If the first breakpoint in 
     *   the breakpoint table is in the middle of the "intended" step.
     *   Then the current step size is reduced to breakpoint - current
     *   time. The result of such a step is the left limit of the states
     *   at the breakpoint. 
     *   <P>
     *   All the actors are prefired before an iteration is begun. If 
     *   any one of them returns false, then the iteration is not 
     *   proceeded, and the function returns.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        if (_first) {
            _first = false;
            produceOutput();
            return;
        }
        updateStates(); // call postfire on all actors
        //Refine step size
        setCurrentStepSize(getSuggestedNextStepSize());
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        // If now is a break point, remove the break point from table;
        if((breakPoints != null) && !breakPoints.isEmpty()) {
            bp = ((Double)breakPoints.first()).doubleValue();
            if(bp <= getCurrentTime()) {
                // break point now!
                breakPoints.removeFirst();
            }
            //adjust step size;
            if(!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                double iterEndTime = getCurrentTime()+getCurrentStepSize();
                if (iterEndTime > bp) {
                    setCurrentStepSize(bp-getCurrentTime());
                }
            }
        }
        //choose ODE solver
        setCurrentODESolver(_defaultSolver);
        // prefire all the actors.
        boolean ready = true;
        CompositeActor ca = (CompositeActor) getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            ready = ready && a.prefire();
        }
        if(ready) {
            ODESolver solver = getCurrentODESolver();
            solver.proceedOneStep();
            produceOutput();
        }
    }

    /** Initialization for the entire system. This
     *  is called exactly once at the start of the entire execution.
     *  It set the current time to the start time and the suggested 
     *  next step size to the initial step size.
     *  It invoke the initialize() method for all the Actors in the
     *  system. Parameters are updated, so that the parameters 
     *  set after the creation of the actors are evaluated and ready
     *  for use. The stop time is registered as a breakpoint.
     *  This method checks if there is a composite actor for this 
     *  director to direct, and if there is a proper scheduler for this
     *  director. If not, an exception is throw. 
     *  The ODE solver is instantiated.
     *  
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println("Director initialize.");
        }
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            if(DEBUG) {
                System.out.println("Director has no container.");
            }
            throw new IllegalActionException(this, "Has no container.");
        }
        if (ca.getContainer() != null) {
            if(DEBUG) {
                System.out.println("Director can only be the top director.");
            }
            throw new IllegalActionException(this,
            " can only serve as the top level director.");
        }
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            if(DEBUG) {
                System.out.println("Director does not have a scheduler.");
            }
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        if(STAT) {
            NSTEP=0;
            NFUNC=0;
            NFAIL=0;
        }
        if(VERBOSE) {
            System.out.println("updating parameters");
        }
        updateParameters();
        // Instantiate ODE solver
        if(VERBOSE) {
            System.out.println("instantiating ODE solver"+_solverclass);
        }
        if(_defaultSolver == null) {
            _defaultSolver = _instantiateODESolver(_solverclass);
        }
        // set time
        setCurrentTime(getStartTime());
        setSuggestedNextStepSize(getInitialStepSize());
        fireAt(null, getStopTime());
        sch.setValid(false);
        _first = true;
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        super.initialize();
    }

    /** Return false if simulation stop time is reached.
     *  Test if the current time is 
     *  the stop time. If so, return false ( for stop further simulation).
     *  Otherwise, returns true.
     *  @return false If the simulation is finished.
     *  @exception IllegalActionException Never thrown
     */
    public boolean postfire() throws IllegalActionException {
        if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
            fireAt(null, getStopTime());
        }
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeResolution()) {
            updateStates(); // call postfire on all actors
            return false;
        }
        return true;
    }

    /** Return true always, indicating that the system is always ready
     *  for one iteration. The schedule
     *  is recomputed if there is any mutation. The parameters are
     *  updated, since this is the safe place to change parameters.
     *
     *  @return True Always
     *  @exception IllegalActionException Never thrown in this director.
     */
    public boolean prefire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("Director prefire.");
        }
        if(DEBUG) {
            NSTEP++;
        }
        if(!scheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        updateParameters();
        return true;
    }

    /** produce outputs. Fire all the actors in the output schedule.
     *  @exception IllegalActionException If the actor on the output
     *      schedule throws it.
     */
    public void produceOutput() throws IllegalActionException {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        // Integrators emit output.
        // FIXME: Do we need this? If the last fire of the integrators
        //        has already emitted token, then the output actors
        //        can use them. That is at least true for implicit methods.
        Enumeration integrators = scheduler.dynamicActorSchedule();
        while(integrators.hasMoreElements()) {
            CTDynamicActor dyn =(CTDynamicActor)integrators.nextElement();
            if(VERBOSE) {
                System.out.println("Excite State..."+
                    ((Nameable)dyn).getName());
            }
            dyn.emitPotentialStates();
        }
        // outputSchdule.fire()
        Enumeration outputactors = scheduler.outputSchedule();
        while(outputactors.hasMoreElements()) {
            Actor nextoutputactor = (Actor)outputactors.nextElement();
            if(VERBOSE) {
                System.out.println("Fire output..."+
                    ((Nameable)nextoutputactor).getName());
            }
            nextoutputactor.fire();
        }
    } 

    /** Call postfire() on all actors. For a correct CT simulation,
     *  the state of an actor can only change at this stage of an
     *  iteration.
     *  @exception IllegalActionException If any of the actors 
     *      throws it.
     */
    public void updateStates() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        Enumeration allactors = container.deepGetEntities();
        while(allactors.hasMoreElements()) {
            Actor nextactor = (Actor)allactors.nextElement();
            nextactor.postfire();
        }
    }

    /** Update given parameter. If the parameter does not exist, 
     *  throws an exception.
     *  @param param The parameter.
     *  @exception IllegalActionException If the parameter does not exist.
     */
    public void updateParameter(Parameter param)
            throws IllegalActionException {
        if(param == _paramODESolver) {
            if(VERBOSE) {
                System.out.println("solver updating.");
            }
            _solverclass =
            ((StringToken)param.getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_solverclass);
        } else {
            super.updateParameter(param);
        }
    }

    /** Show the statistics of the simulation if needed. The statistics
     *  includes the number of step simulated, the number of function
     *  evaluations (firing all actors in the state transition schedule),
     *  and the number of failed steps (due to error control).
     *  
     *  @exception IllegalActionException Never thrown.
     */
    public void wrapup() throws IllegalActionException{
        if(STAT) {
            System.out.println("**************STATISTICS***************");
            System.out.println("Total # of STEPS "+NSTEP);
            System.out.println("Total # of Function Evaluation "+NFUNC);
            System.out.println("Total # of Failed Steps "+NFAIL);
        }
        super.wrapup();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                      ////
    private void _initParameters() {
        try {
            _solverclass=
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver";
            _paramODESolver = new CTParameter(
                this, "ODESolver", new StringToken(_solverclass));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // parameter of ODE solver
    private CTParameter _paramODESolver;
    // The classname of the ODE solver
    private String _solverclass;

    // The default solver.
    private ODESolver _defaultSolver = null;

    //indicate the first round of execution.

    private boolean _first;
}
