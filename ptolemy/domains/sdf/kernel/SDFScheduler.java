/* A Scheduler for the SDF domain

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;
import ptolemy.math.Fraction;

import java.util.*;

///////////////////////////////////////////////////////////
//// SDFScheduler
/**

A scheduler that implements basic scheduling of SDF graphs.  This
class calculates the SDF schedule in two phases.  First, the balance
equations for the rates between actors are solved to determine the
<i>firing vector</i> (also known as the repetitions vector).  The
firing vector is the least integer solution such that the number of
tokens created on each channel of each relation is equal to the number
of tokens consumed.  In some cases, no solution exists.  Such graphs
are not executable under SDF.
<p>

Then the actors are ordered such that each actor only fires when the
scheduler has determined that enough tokens will be present on its
input ports to allow it to fire.  In cases where the Dataflow graph is
cyclic, a valid firing vector exists, but no actor can fire, since
they all depend on the output of another actor.  This situation is
known as "Deadlock".  Deadlock must be prevented in SDF by manually
inserting delay actors, which represent initial tokens on each
relation.  Such delay actors are responsible for creating tokens
during initialization that will prevent deadlock.  Delay actors must
set their "tokenInitProduction" parameters to represent the number of
tokens they will create during initialization.  The SDFScheduler uses
the tokenInitProduction parameter to break the dependency in a cyclic
graph.

<p>

Note that this scheduler only ensures that the number of firings is
minimal.  Most notably, it does not attempt to minimize the size of
the buffers that are associated with each relation.  The resulting
schedule is a linear schedule (as opposed to a looped schedule) and is
not suitable for multiprocessing environments.  <p> Any actors may be
scheduled by this scheduler, which will, by default, assume
homogeneous behavior for each actor.  (i.e. each output port produces
one token for each firing, and each input port consumes one token on
each firing, and no tokens are created during initialization.)  If
this is not the case then the parameters "tokenConsumptionRate",
"tokenProductionRate", and "tokenInitProduction" must be set.  The
SDFIOPort class provides easier access to these parameters.  <p>
Note that reconstructing the schedule is expensive, so the schedule is
locally cached for as long as possible, and mutations under SDF should
be avoided.

<p>

Note that this scheduler supports actors with 0-rate ports as long as
the graph is not equivalent to a disconnected graph. This scheduler
is somewhat conservative in this respect. 

@see ptolemy.actor.sched.Scheduler
@see ptolemy.domains.sdf.kernel.SDFIOPort

@author Stephen Neuendorffer
@version $Id$
*/
public class SDFScheduler extends Scheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public SDFScheduler() {
        super();
	_localMemberInitialize();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public SDFScheduler(Workspace workspace) {
        super(workspace);
	_localMemberInitialize();
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SDFScheduler(Director container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the schedule.  Return the number of times that the given
     *  entity will fire in a single iteration of the system.
     */
    public int getFiringCount(Entity entity)
            throws IllegalActionException {
        schedule();
        return _getFiringCount(entity);
    }

    /** Get the number of tokens that are consumed
     *  on the given port.  If the actor is not an
     *  input port, then return 0.  Otherwise, return the value of
     *  the port's "tokenConsumptionRate" Parameter.   If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the tokenConsumptionRate
     *   parameter has an invalid expression.
     */
    public static int getTokenConsumptionRate(IOPort p)
            throws IllegalActionException {
        if(p.isInput()) {
            Parameter param =
                (Parameter)p.getAttribute("tokenConsumptionRate");
            if(param == null) {
                return 1;
            } else {
                Token token = param.getToken();
                if(token instanceof IntToken) {
                    return ((IntToken)token).intValue();
                } else {
                    throw new IllegalActionException("Parameter "
                            + param.getFullName() + " was expected "
                            + "to contain an IntToken, but instead "
                            + "contained a " + token.getType() + ".");
                }
            }
        } else
            return 0;
    }

    /** Get the number of tokens that are produced on the given port
     *  during initialization.  If the actor is not an
     *  output port, then return 0.  Otherwise, return the value of
     *  the port's "tokenInitProduction" Parameter.   If the parameter
     *  does not exist, then assume the actor is zero-delay and return
     * a value of zero.
     * @exception IllegalActionException If the tokenInitProduction
     *  parameter has an invalid expression.
     */
    public static int getTokenInitProduction(IOPort p)
            throws IllegalActionException {
        if(p.isOutput()) {
            Parameter param =
                (Parameter)p.getAttribute("tokenInitProduction");
            if(param == null) {
                return 0;
            } else {
                Token token = param.getToken();
                if(token instanceof IntToken) {
                    return ((IntToken)token).intValue();
                } else {
                    throw new IllegalActionException("Parameter "
                            + param.getFullName() + " was expected "
                            + "to contain an IntToken, but instead "
                            + "contained a " + token.getType() + ".");
                }
            }
        } else
            return 0;
    }

    /** Get the number of tokens that are produced
     *  on the given port.  If the actor is not an
     *  output port, then return 0.  Otherwise, return the value of
     *  the port's "tokenProductionRate" Parameter.  If the parameter
     *  does not exist, then assume the actor is homogeneous and return a
     *  rate of 1.
     *  @exception IllegalActionException If the tokenProductionRate
     *  parameter has an invalid expression.
     */
    public static int getTokenProductionRate(IOPort p)
            throws IllegalActionException {
        if(p.isOutput()) {
            Parameter param =
                (Parameter)p.getAttribute("tokenProductionRate");
            if(param == null) {
                return 1;
            } else {
                Token token = param.getToken();
                if(token instanceof IntToken) {
                    return ((IntToken)token).intValue();
                } else {
                    throw new IllegalActionException("Parameter "
                            + param.getFullName() + " was expected "
                            + "to contain an IntToken, but instead "
                            + "contained a " + token.getType() + ".");
                }
            }
        } else
            return 0;
    }

    /** Set the tokenConsumptionRate parameter of the given port
     *  to the given rate.  The port is assumedly an input port.
     *  @exception IllegalActionException If the rate is negative.
     */
    public static void setTokenConsumptionRate(IOPort port, int rate)
            throws IllegalActionException {
        if(rate < 0) throw new IllegalActionException(
                "tokenConsumptionRate cannot be set to " + rate
                + " which is negative");
        Parameter param = (Parameter)
            port.getAttribute("tokenConsumptionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port, "tokenConsumptionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    /** Set the tokenInitProduction parameter of the given port to
     *  the given rate.  The port is assumedly an output port.
     *  @exception IllegalActionException If the production is negative.
     */
    public static void setTokenInitProduction(IOPort port, int rate)
            throws IllegalActionException {
        if(rate < 0) throw new IllegalActionException(
                "tokenInitProduction cannot be set to " + rate
                + " which is negative");
        Parameter param = (Parameter)
            port.getAttribute("tokenInitProduction");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port, "tokenInitProduction",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    /** Set the tokenProductionRate parameter of the given port
     *  to the given rate.  The port is assumedly an output port.
     *  @exception IllegalActionException If the rate is negative.
     */
    public static void setTokenProductionRate(IOPort port, int rate)
            throws IllegalActionException {
        if(rate < 0) throw new IllegalActionException(
                "tokenInitProduction cannot be set to " + rate
                + " which is negative.");
        Parameter param = (Parameter)
            port.getAttribute("tokenProductionRate");
        try {
            if(param != null) {
                param.setToken(new IntToken(rate));
            } else {
                param = new Parameter(port, "tokenProductionRate",
                        new IntToken(rate));
            }
        } catch (Exception exception) {
            // This should never happen.
            // e might be NameDuplicationException, but we already
            // know it doesn't exist.
            // e might be IllegalActionException, but we've already
            // checked the error conditions
            throw new InternalErrorException(exception.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  </ul>
     *
     * @return A Schedule of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  schedulable.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector dir =
            (StaticSchedulingDirector)getContainer();
        CompositeActor container = (CompositeActor)(dir.getContainer());

        // A linked list containing all the actors
        LinkedList AllActors = new LinkedList();
        Iterator entities = container.deepEntityList().iterator();

        while(entities.hasNext()) {
            ComponentEntity a = (ComponentEntity)entities.next();

            // Fill AllActors with the list of things that we can schedule
            // FIXME: What if other things can be scheduled than actors?
            if(a instanceof Actor) AllActors.addLast(a);
        }

        // First solve the balance equations
        Map firings = null;

        // externalRates maps from external 
        // ports to the number of tokens that that port 
        // will produce or consume each firing.  
        // It gets populated with the fractional production ratios
        // and is used in the end to set final rates on external ports.
        // This map is initialized to zero.
        Map externalRates = new TreeMap(new NamedObjComparator());
        
        // Initialize externalRates to zero.
        Iterator enumPorts = container.portList().iterator();
        while(enumPorts.hasNext()) {
            IOPort port = (IOPort) enumPorts.next();
            externalRates.put(port, Fraction.ZERO);
        }

        try {
            firings =
                _solveBalanceEquations(container, AllActors, externalRates);

	    List deadActors = new LinkedList();
	    // now remove all actors that get fired 0 times.
	    Iterator allActor = AllActors.iterator();
	    while (allActor.hasNext()) {
		ComponentEntity anActor = (ComponentEntity)allActor.next();
		// Remove this actor from the firing sequence if it will
		// not be fired.
		Fraction theFiring = (Fraction)firings.get(anActor);
		if(_debugging) {
                    _debug("actor name: " + anActor.getName());
                    _debug("has firings value: " + 
                            theFiring.getNumerator());
                }
		if (theFiring.getNumerator() == 0) {
                    if(_debugging) {
                        _debug("and will be removed because" +
                                " it is not being fired.");
                    }
		    deadActors.add(anActor);
		}
	    }

	    // Now remove the dead actors.
	    Iterator removeIt = deadActors.iterator();
	    while (removeIt.hasNext()) {
		ComponentEntity actorToRemove = 
		    (ComponentEntity)removeIt.next();
                if(_debugging) {
                    _debug("Removing actor : " + 
                            actorToRemove.getName() + 
                            " from the schedule");
                }
		firings.remove(actorToRemove);
		// remove the actor from the list of all actors, as
		// well.
		AllActors.remove(actorToRemove);
	    }
            if(_debugging) {
                _debug("theFiring string: " +  firings.toString());
            }

        } catch (IllegalActionException ex) {
            throw new NotSchedulableException(this, "Check expression of "
                    + "rate and initial production parameters:\n"
                    + ex.getMessage() + ".");
        }
        _normalizeFirings(firings, externalRates);

        _setFiringVector(firings);

        if (_debugging) {
            _debug("Firing Vector:");
            _debug(firings.toString());
        }

        // Schedule all the actors using the calculated firings.
        Schedule result = _scheduleConnectedActors(AllActors);

        _setFiringVector(firings);

        if (_debugging) {
            _debug("Firing Vector:");
            _debug(firings.toString());
        }

        try {
            _setContainerRates(externalRates);
        } catch (IllegalActionException ex) {
            throw new NotSchedulableException(this, "Check expression of "
                    + "rate and initial production parameters.");
        }

        setValid(true);
        return result;
    }

    /** Initialize the local data members of this object.  */
    protected void _localMemberInitialize() {
        _firingvector = new TreeMap(new NamedObjComparator());
        _firingvectorvalid = true;
    }

    /** Return the scheduling sequence.  An exception will be thrown if the
     *  graph is not schedulable.  This occurs in the following circumstances:
     *  <ul>
     *  <li>The graph is not a connected graph.
     *  <li>No integer solution exists for the balance equations.
     *  <li>The graph contains cycles without delays (deadlock).
     *  <li>Multiple output ports are connected to the same broadcast
     *  relation. (equivalent to a non-deterministic merge)
     *  </ul>
     *
     * @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  schedulable.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        final Iterator schedule = _getSchedule().actorIterator();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return schedule.hasNext();
            } 
            public Object nextElement() {
                return schedule.next();
            }
        };
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Count the number of inputports in the Actor that must be
     *  fulfilled before the actor can fire.  Ports that are connected
     *  to actors that we are not scheduling right now are assumed to
     *  be fulfilled.  Ports that have more tokens waiting on each of
     *  their channels than their input consumption rate are also
     *  already fulfilled.  All other ports are considered to be
     *  unfulfilled.
     *  @param a The actor
     *  @param unscheduledactors The set of actors that we are scheduling.
     *  @param waitingTokens The Map of tokens currently waiting on all the
     *  input ports.
     *  @return The number of unfulfilled inputs of a.
     *  @exception IllegalActionException If any called method throws it.
     */
    private int _countUnfulfilledInputs(Actor a,
            LinkedList actorList, Map waitingTokens)
            throws IllegalActionException {
	if (_debugging)
            _debug("counting unfulfilled inputs for " +
                    ((Entity) a).getFullName());
        Iterator ainputPorts = a.inputPortList().iterator();

	int inputCount = 0;
	while(ainputPorts.hasNext()) {
	    IOPort ainputPort = (IOPort) ainputPorts.next();
	    if (_debugging) _debug("checking input " +
                    ainputPort.getFullName());

            /*  Iterator cports = ainputPort.deepConnectedOutPortList().iterator();

	    boolean isOnlyExternalPort = true;
	    while(cports.hasNext()) {
		IOPort cport = (IOPort) cports.next();
		if(actorList.contains(cport.getContainer()))
		    isOnlyExternalPort = false;
	    }
            */

	    int threshold =
		getTokenConsumptionRate(ainputPort);
	    if (_debugging) _debug("Threshold = " + threshold);
	    int[] tokens =
		(int []) waitingTokens.get(ainputPort);

	    boolean isFulfilled = true;
	    int channel;
	    for(channel = 0;
		channel < ainputPort.getWidth() && isFulfilled;
		channel++) {
		if (_debugging) {
                    _debug("Channel = " + channel);
                    _debug("Waiting Tokens = " + tokens[channel]);
                }

		if(tokens[channel] < threshold)
		    isFulfilled = false;
	    }
	    if(!isFulfilled)
		inputCount++;
	}
	return inputCount;
    }

    /** Return the number of firings associated with the Actor.   The
     *  number of firings is stored in the _firingvector Map, indexed
     *  by the entity.
     */
    private int _getFiringCount(Entity entity) {
        return ((Integer) _firingvector.get(entity)).intValue();
    }

    /** Return the number of tokens that will be produced or consumed on the
     *  given port.   If the port is an input, then return its consumption
     *  rate, or if the port is an output, then return its production rate.
     *  @exception NotSchedulableException If the port is both an input and
     *  an output, or is neither an input nor an output.
     */
    private int _getRate(IOPort port) 
            throws NotSchedulableException, IllegalActionException {
        if(port.isInput() && port.isOutput()) {
            throw new NotSchedulableException(port,
                    "Port is both an input and an output, which is not"
                    + " allowed in SDF.");
        } else if(port.isInput()) {
            return getTokenConsumptionRate(port);
        } else if(port.isOutput()) {
            return getTokenProductionRate(port);
        } else {
            // FIXME IS THIS RIGHT?
            throw new NotSchedulableException(port,
                    "Port is neither an input and an output, which is not"
                    + " allowed in SDF.");
        }
    }

    /** Normalize fractional firing ratios into a firing vector that
     *  corresponds to a single SDF iteration.   Multiply all of the
     *  fractions by the GCD of their denominators.
     *
     *  @param firings Map of firing ratios to be normalized.
     *  @param externalRates Map of token production rates that will
     *  be scaled along with the firings map.
     *  @exception InternalErrorException If the calculated GCD does not
     *  normalize all of the fractions.
     */
    private void _normalizeFirings(Map firings, Map externalRates) {
        Iterator unnormalizedFirings = firings.values().iterator();
        int lcm = 1;

        if (_debugging) _debug("Normalizing Firings");
        // First find the lcm of all the denominators
        while(unnormalizedFirings.hasNext()) {
            Fraction f = (Fraction) unnormalizedFirings.next();
            int den = f.getDenominator();
            lcm = Fraction.lcm(lcm, den);
        }

        if (_debugging) _debug("lcm = " + (new Integer(lcm)).toString());
        Fraction lcmFraction = new Fraction(lcm);
    
        Iterator actors = firings.keySet().iterator();
        // now go back through and multiply by the lcm we just found, which
        // should normalize all the fractions to integers.
        while(actors.hasNext()) {
            Object actor = actors.next();
            if (_debugging) _debug("normalizing Actor " +
                    ((ComponentEntity) actor).getName());
            Fraction reps = (Fraction) firings.get(actor);
            reps = reps.multiply(lcmFraction);
            if(reps.getDenominator() != 1)
                throw new InternalErrorException(
                        "Failed to properly perform " +
                        "fraction normalization");
            firings.put(actor, new Integer(reps.getNumerator()));
        }

        Iterator ports = externalRates.keySet().iterator();
        while(ports.hasNext()) {
            Object port = ports.next();
            if (_debugging) _debug("normalizing Rate for " +
                    ((ComponentPort) port).getName());
            Fraction reps = (Fraction) externalRates.get(port);
            reps = reps.multiply(lcmFraction);
            if(reps.getDenominator() != 1)
                throw new InternalErrorException(
                        "Failed to properly perform " +
                        "fraction normalization");
            externalRates.put(port, new Integer(reps.getNumerator()));
        }            
    }

    /** Propagate the number of fractional firings decided for this actor
     *  through the specified port.   Set and verify the fractional
     *  firing for each Actor that is connected to this port.
     *  Any actors that we calculate their firing vector for the first time
     *  are moved from RemainingActors to pendingActors.
     *  Note that ports directly container by the given container are
     *  handled slightly differently than other ports.  Most importantly,
     *  Their rates are propagated to ports they are connected to on the
     *  inside, as opposed to ports they are connected to on the outside.
     *
     *  @param container The actor that is being scheduled.
     *  @param currentPort The port that we are propagating from.
     *  @param firings The current Map of fractional firings for each
     *  Actor.
     *  @param remainingActors The set of actors that have not had their
     *  fractional firing set.
     *  @param pendingActors The set of actors that have had their rate
     *  set, but have not been propagated onwards.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable.
     *  @exception IllegalActionException If any called method throws it.
     */
    private void _propagatePort(CompositeActor container, 
            IOPort currentPort,
            Map firings,
            Map externalRates,
            LinkedList remainingActors,
            LinkedList pendingActors)
            throws NotSchedulableException, IllegalActionException {

        ComponentEntity currentActor =
            (ComponentEntity) currentPort.getContainer();

        // First check to make sure that this Port is not connected to
        // Any other output ports.  This results in a non-deterministic
        // merge and is illegal.
        // This only makes sense if we are not talking about a
        // port in the container.
        if(currentPort.isOutput() && currentPort.getContainer() != container) {
            Iterator connectedOutPorts =
                currentPort.deepConnectedOutPortList().iterator();
            
            while(connectedOutPorts.hasNext()) {
                IOPort connectedPort =
                    (IOPort) connectedOutPorts.next();
                // connectPort might be connected on the inside to the
                // currentPort, which is legal.
                if(!connectedPort.getContainer().equals(
                        currentPort.getContainer().getContainer())) {
                    throw new NotSchedulableException(
                            currentPort, connectedPort,
                            "Two output ports are connected " +
                            "on the same relation. " +
                            "This is not legal in SDF.");
                }
            }
        }

        // Get the rate of this port.
        int currentRate = _getRate(currentPort);

        if(currentRate >= 0) {
            // Compute the rate for the Actor currentPort is connected to
            Iterator connectedPorts;
            if(currentPort.getContainer() == container) {
                // NOTE: deepInsidePortList() will only return 
                // currentPort, since it is opaque.
                // Don't we wish that ComponentPort had a nice method
                // to do this???
                List deepInsidePorts = new LinkedList();
                Iterator insidePorts = 
                    currentPort.insidePortList().iterator();
                while(insidePorts.hasNext()) {
                    IOPort p = (IOPort)insidePorts.next();
                    deepInsidePorts.addAll(p.deepInsidePortList());
                }
                connectedPorts = deepInsidePorts.iterator();
            } else {
                connectedPorts = 
                    currentPort.deepConnectedPortList().iterator();
            }
            while(connectedPorts.hasNext()) {
                IOPort connectedPort =
                    (IOPort) connectedPorts.next();

                ComponentEntity connectedActor =
                    (ComponentEntity) connectedPort.getContainer();

                if (_debugging) 
                    _debug("Propagating " + currentPort
                            + " to " + connectedActor.getName());

                int connectedRate = _getRate(connectedPort);

                // currentFiring is the firing that we've already
                // calculated for currentactor
                Fraction currentFiring =
                    (Fraction) firings.get(currentActor);

                // the firing that we think the connected actor should be,
                // based on currentActor
		Fraction desiredFiring;

		if ((currentRate == 0) && (connectedRate > 0)) {
		    // the current port of the current actor has a rate
		    // of 0, and the current connected port of the
		    // connected actor has a positive integer rate.
		    // therefore, we must set the firing count of
		    // the connected actor to 0 so that it will
		    // not appear in the final static schedule.
		    desiredFiring = Fraction.ZERO;
		} else if ((currentRate > 0) && (connectedRate == 0)) {
		    // the current port of the current actor has a 
		    // positive integer rate, and the current
		    // connected port of the connected actor has
		    // rate of 0. therefore, we set the firing
		    // count of the current actor to 0 so that
		    // it will not appear in the final static schedule.
		    currentFiring = Fraction.ZERO;
		    // update the entry in the firing table.
		    firings.put(currentActor, currentFiring);
		    
		    // Set the firing count of the connected actor to
		    // be 1.
		    desiredFiring = new Fraction(1);
		} else if ((currentRate == 0) && (connectedRate == 0)) {
		    // Give the connected actor the same rate as the
		    // current actor.
		    desiredFiring = currentFiring;
		} else {
		    // Only do this if both of these rates 
		    // are non-zero.
		    desiredFiring =
			currentFiring.multiply(
			  new Fraction(currentRate, connectedRate));
		}

                // What the firing for connectedActor already is set to.
                // This should be either 0, or equal to desiredFiring.
                try {
		    Fraction minusOne = new Fraction(-1);

                    Fraction presentFiring =
                        (Fraction) firings.get(connectedActor);
                    if(_debugging) 
                        _debug("presentFiring of connectedActor " 
                                + connectedActor + " = " + presentFiring);
                    if(presentFiring == null) {
                        // This means we've propagated to the containing
                        // actor.
                        // Temporarily create the entry in the firing table
                        // This is possibly rather fragile.
                        firings.put(connectedActor, desiredFiring);
                        // Compute the external rate for this port.
                        Fraction rate = currentFiring.multiply(
                                new Fraction(currentRate, 1));
                        Fraction previousRate = 
                            (Fraction) externalRates.get(connectedPort);
                        if(previousRate.equals(Fraction.ZERO)) {
                            externalRates.put(connectedPort, rate);
                        } else if(!rate.equals(previousRate)) {
                            throw new NotSchedulableException("No solution " +
                                    "exists for the balance equations.\n" +
                                    "Graph is not " +
                                    "consistent under the SDF domain " +
                                    "detected on external port " + 
                                    connectedPort.getFullName());
                        }
                        _propagatePort(container, connectedPort, firings,
                                externalRates, remainingActors, pendingActors);
                        firings.remove(connectedActor);
                    } else if(presentFiring.equals(minusOne)) {
                        // create the entry in the firing table
                        firings.put(connectedActor, desiredFiring);
                        // Remove them from remainingActors
                        remainingActors.remove(connectedActor);
                        // and add them to the pendingActors.
                        pendingActors.addLast(connectedActor);
                        
                    } else if(!presentFiring.equals(desiredFiring))
                        throw new NotSchedulableException("No solution " +
                                "exists for the balance equations.\n" +
                                "Graph is not " +
                                "consistent under the SDF domain");
                }
                catch (NoSuchElementException e) {
                    throw new InternalErrorException("SDFScheduler: " +
                            "connectedActor " +
                            ((ComponentEntity) connectedActor).getName() +
                            "does not appear in the firings Map");
                }

                /*
                  else if(!presentFiring.equals(desiredFiring))
                  throw new NotSchedulableException("No solution " +
                  "exists for the balance equations.\n" +
                  "Graph is not" +
                  "consistent under the SDF domain");
                  }
                  catch (NoSuchElementException e) {
                  throw new InternalErrorException("SDFScheduler: " +
                  "connectedActor " +
                  ((ComponentEntity) connectedActor).getName() +
                  "does not appear in the firings Map");
                  }
                */

                if (_debugging) {
                    _debug("New Firing: ");
                    _debug(firings.toString());
                }
            }
        }
    }

    /** Create a schedule for a set of UnscheduledActors.  Given a valid
     *  firing vector, simulate the scheduling of the actors until the
     *  end of one synchronous dataflow iteration.
     *  Each actor will appear in the schedule exactly the number of times that
     *  minimally solves the balance equations and in an order where each
     *  actor has sufficient tokens on its inputs to fire.   Note that no
     *  claim is made that this is an optimal solution in any other sense.
     *
     *  @param actorList The Actors that need to be scheduled.
     *  @return A LinkedList of the Actors in the order they should fire.
     *  @exception NotSchedulableException If the algorithm encounters an SDF
     *  graph that is not consistent with the firing vector, or detects an
     *  inconsistent internal state, or detects a graph that cannot be
     *  scheduled.
     */
    private Schedule _scheduleConnectedActors(
            LinkedList actorList)
            throws NotSchedulableException {

        // A linked list containing all the actors that have no inputs
        LinkedList readyToScheduleActorList = new LinkedList();
        // A linked list that will contain our new schedule.
        Schedule newSchedule = new Schedule();

        // An association between All the input ports in a simulation and an
	// array of the number of tokens waiting on each relation of that port
	Map waitingTokens = new TreeMap(new NamedObjComparator());
        Map firingsRemainingVector = new TreeMap(new NamedObjComparator());
        firingsRemainingVector.putAll(_firingvector);

	LinkedList unscheduledActorList = new LinkedList();
	unscheduledActorList.addAll(actorList);

        try {
	    // Initialize the waitingTokens at all the
	    // input ports to zero
	    Iterator schedulableEntities = actorList.iterator();
	    while(schedulableEntities.hasNext()) {
		Actor a = (Actor)schedulableEntities.next();

		Iterator ainputports = a.inputPortList().iterator();
		while(ainputports.hasNext()) {
		    IOPort ainputport = (IOPort) ainputports.next();
		    int[] tokencount = new int[ainputport.getWidth()];
		    for(int channel = 0; channel < tokencount.length;
			channel++)
			tokencount[channel] = 0;
		    waitingTokens.put(ainputport, tokencount);
		}
	    }

	    // simulate the creation of initialization tokens (delays).
	    schedulableEntities = actorList.iterator();
	    while(schedulableEntities.hasNext()) {
		Actor a = (Actor)schedulableEntities.next();

		Iterator aoutputports = a.outputPortList().iterator();
		while(aoutputports.hasNext()) {
		    IOPort aOutputPort = (IOPort) aoutputports.next();
		    int count = getTokenInitProduction(aOutputPort);
                    if (_debugging) _debug("Simulating " + count
                            + " tokens created on " + aOutputPort);
		    if(count > 0) {
			_simulateTokensCreated(aOutputPort,
                                count,
                                actorList,
                                readyToScheduleActorList,
                                waitingTokens);
		    }
		}
            }

            // Simulate a large number of tokens created on each
            // external input port.
            StaticSchedulingDirector dir =
                (StaticSchedulingDirector)getContainer();
            CompositeActor ca = (CompositeActor)(dir.getContainer());
            Iterator inputPorts = ca.inputPortList().iterator();
            while(inputPorts.hasNext()) {
                IOPort port = (IOPort)inputPorts.next();
                _simulateExternalInputs(port, actorList, waitingTokens);
            }

	    // Fill readyToScheduleActorList with all the actors that have
	    // no unfulfilled input ports, and are thus ready to fire.
	    schedulableEntities = actorList.iterator();
	    while(schedulableEntities.hasNext()) {
		Actor a = (Actor)schedulableEntities.next();

		int inputCount = _countUnfulfilledInputs(a, actorList,
                        waitingTokens);
		if(inputCount == 0)
		    readyToScheduleActorList.addFirst((ComponentEntity) a);

		if (_debugging) _debug("Actor "
                        + ((ComponentEntity) a).getName() +
                        " has " + inputCount + " unfulfilledInputs.");
	    }

	    while(readyToScheduleActorList.size() > 0) {
		if (_debugging) _debug("\nwaitingTokens: ");
		Iterator ports = waitingTokens.keySet().iterator();
		while(ports.hasNext()) {
		    IOPort port = (IOPort)ports.next();
		    int tokencount[] = (int[])waitingTokens.get(port);
		    if (_debugging) {
                        _debug("Port " + port.getFullName());
                        _debug("Number of channels = " + tokencount.length);
                    }
		    for(int channel = 0;
			channel < tokencount.length;
			channel++)
			if (_debugging) _debug("Channel " + channel + " has " +
                                tokencount[channel] + " tokens.");
		}

		if (_debugging) {
                    _debug("Actors that can be scheduled:");
                    Iterator actorsLeft = readyToScheduleActorList.iterator();
                    while(actorsLeft.hasNext()) {
                        Entity e = (Entity)actorsLeft.next();
                        _debug(e.getFullName());
                    }
                    _debug("Actors with firings left:");
                    actorsLeft = unscheduledActorList.iterator();
                    while(actorsLeft.hasNext()) {
                        Entity e = (Entity)actorsLeft.next();
                        _debug(e.getFullName());
                    }
		}

		// pick an actor that is ready to fire.
		ComponentEntity currentActor
		    = (ComponentEntity) readyToScheduleActorList.getFirst();
		// remove it from the list of actors we are waiting to fire
                while(readyToScheduleActorList.remove(currentActor));

		if (_debugging) {
                    _debug("Scheduling Actor " + currentActor.getName());
                }
		_simulateInputConsumption(currentActor, waitingTokens);

		// add it to the schedule
                boolean incrementCount = false;
                if(newSchedule.size() > 0) {
                    Firing lastFiring = (Firing)
                        newSchedule.get(newSchedule.size() - 1);
                    if(lastFiring.getActor().equals(currentActor)) {
                        incrementCount = true;
                        lastFiring.setIterationCount(
                                lastFiring.getIterationCount() + 1);
                    }
                }
                
                // If we weren't able to increment the count in the last
                // firing, then create a new firing.
                if(!incrementCount) {
                    Firing firing = new Firing();
                    firing.setActor((Actor)currentActor);
                    newSchedule.add(firing);
                }
                // newSchedule.addLast(currentActor);

		// Get all it's outputPorts.
		Iterator aOutputPorts =
		    ((Actor) currentActor).outputPortList().iterator();

		// And simulate the proper production of tokens.
		while(aOutputPorts.hasNext()) {
		    IOPort aOutputPort = (IOPort) aOutputPorts.next();

		    int count =
			getTokenProductionRate(aOutputPort);

		    _simulateTokensCreated(aOutputPort,
                            count,
                            unscheduledActorList,
                            readyToScheduleActorList,
                            waitingTokens);
		}

		// Update the firingRemainingVector for this actor.
		int firingsRemaining =
                    ((Integer) firingsRemainingVector.get(currentActor)).
                    intValue();
		firingsRemaining -= 1;
                firingsRemainingVector.put(currentActor,
                        new Integer(firingsRemaining));

		if (_debugging) {
                    _debug(currentActor.getName() + " should fire " +
                            firingsRemaining + " more times.");
                }

		// Figure out what to do with the actor, now that it has been
		// scheduled.
		if(firingsRemaining < 0)
		    // If we screwed up somewhere, and fired this more
		    // times than we thought we should have
		    // then throw an exception.
		    // This should never happen.
		    throw new IllegalStateException("Balance Equation " +
                            "solution does not agree with " +
                            "scheduling algorithm");

		// If we've fired this actor all the times that it should, then
		// we get rid of it entirely.
		else {
		    if(firingsRemaining == 0) {
                        if(_debugging) {
                            _debug("Current Actor = " + currentActor);
                        }
			// remove the actor from the readyToScheduleActorList
			// so that it does not get scheduled
			while(readyToScheduleActorList.remove(currentActor));
			// remove the actor from the unscheduledActorList
			// so that it does not get added back to the
			// readyToScheduleActorList
			while(unscheduledActorList.remove(currentActor));
			if(_debugging) {
                            _debug("Remaining actors");
                        }
			Iterator actorsLeft =
                            readyToScheduleActorList.iterator();
			while(actorsLeft.hasNext()) {
			    Entity e = (Entity)actorsLeft.next();
			    if (_debugging) _debug(e.getFullName());
			}
		    }
		    // Otherwise the actor still has firings left
		    else {
			// Count the number of unfulfilled inputs.
			int inputCount =
			    _countUnfulfilledInputs((Actor)currentActor,
                                    unscheduledActorList,
                                    waitingTokens);
			// We've already removed currentActor from
			// ReadytoSchedule actors
			// so if it can be fired again right away,
			// put it back on the list.
			// if the actor can still be scheduled
			// i.e. all its inputs are satisfied, and it
			// appears in the unscheduled actors list
			// then put it
			// at the END of readyToScheduleActorList.
			if(inputCount < 1 &&
                                unscheduledActorList.contains(currentActor))
			    readyToScheduleActorList.addFirst(currentActor);
		    }
		}
	    }
	}
        catch (IllegalActionException iae) {
            // This could happen if we call getTokenConsumptionRate on a
            // port that isn't a part of the actor.   This probably means
            // the graph is screwed up, or somebody else is mucking
            // with it.
            throw new InternalErrorException("SDF Scheduler Failed " +
                    "internal consistency check: " + iae.getMessage());
        } finally {
            if (_debugging) _debug("finishing loop");
	}

	if(unscheduledActorList.size() > 0) {
	    String s = new String("Actors remain that cannot be scheduled:\n");
	    Iterator actors = unscheduledActorList.iterator();
	    while(actors.hasNext()) {
		Entity actor = (Entity)actors.next();
		s += actor.getFullName() + "\n";
	    }
	    throw new NotSchedulableException(s);
	}

        Iterator eschedule = newSchedule.iterator();
        if (_debugging) {
            _debug("Schedule is:");
            while(eschedule.hasNext()) {
                _debug(((ComponentEntity) eschedule.next()).toString());
            }
        }
        return newSchedule;
    }

    /** Push the rates calculated for this system up to the contained Actor.
     *  This allows the container to be properly scheduled if it is
     *  in a hierarchical system
     *  @exception IllegalActionException If any called method throws it.
     */
    private void _setContainerRates(Map externalRates)
            throws NotSchedulableException, IllegalActionException {
        Director director = (Director) getContainer();
        CompositeActor container = (CompositeActor) director.getContainer();
        Iterator ports = container.portList().iterator();
        while(ports.hasNext()) {
            int consumptionRate = 0;
            int productionRate = 0;
            int initProduction = 0;
            IOPort port = (IOPort) ports.next();
            Integer rate = (Integer)externalRates.get(port);
            if(port.isInput() && port.isOutput()) {
                throw new NotSchedulableException(port,
                        "External Port is both an input and an output, "
                        + "which is not allowed in SDF.");
            } else if(port.isInput()) {
                setTokenConsumptionRate(port, rate.intValue());
            } else if(port.isOutput()) {
                setTokenProductionRate(port, rate.intValue());
                // Infer init production.
                Iterator connectedPorts = port.insidePortList().iterator();
                //   IOPort foundOutputPort = null;
                while(connectedPorts.hasNext()) {
                    IOPort connectedPort = (IOPort) connectedPorts.next();
                    if(connectedPort.isOutput()) {
                        /*if(foundOutputPort != null) {
                            throw new NotSchedulableException(
                                    "External output port " + port
                                    + " is connected on the inside to more "
                                    + " than one output port:" 
                                    + foundOutputPort + " and "
                                    + connectedPort);
                                    }*/
                        setTokenInitProduction(port, 
                                getTokenInitProduction(connectedPort));
                        //       foundOutputPort = connectedPort;
                    }
                } 
            } else {
                // FIXME IS THIS RIGHT?
                throw new NotSchedulableException(port,
                        "External Port is neither an input and an output, "
                        + "which is not allowed in SDF.");
            }

            if (_debugging) {
                _debug("Port " + port.getName());
                _debug("consumptionRate = " + consumptionRate);
                _debug("productionRate = " + productionRate);
                _debug("initProduction = " + initProduction);
            }
        }
    }

    /** Set the number of firings associated with the Actor.   This is
     *  equivalent to changing the entry in the FiringVector associated with
     *  with the entity to have a value count.
     */
    private void _setFiringCount(Entity entity, int count) {
        _firingvector.put(entity, new Integer(count));
    }

    /** Set the firing vector, which is a Map associating an Actor
     *  with the number of times that it will fire during an SDF iteration.
     *  Every object that this Scheduler is responsible for should have an
     *  entry, even if it is zero indicating that the Actor has not yet had
     *  its firings determined.
     *
     *  @param newfiringvector A Map from ComponentEntity to Integer.
     */
    private void _setFiringVector(Map newfiringvector) {
        _firingvector = newfiringvector;
        _firingvectorvalid = true;
    }

    /**
     * Simulate the consumption of tokens from the given external input
     * ports.  This sets the values in the waiting tokens map to be a 
     * very large number, since we assume that each external input port
     * is automatically fulfilled.
     */
    private void _simulateExternalInputs(IOPort port,
            LinkedList actorList,
            Map waitingTokens)
            throws IllegalActionException {

	Receiver[][] creceivers = port.deepGetReceivers();

	if (_debugging) {
            _debug("Simulating external input tokens from "
                    + port.getFullName());
            _debug("inside channels = " + creceivers.length);
        }
	int sourcechannel;
	for(sourcechannel = 0;
            sourcechannel < creceivers.length;
            sourcechannel++) {
	    if (_debugging) {
                _debug("destination receivers for channel "
                        + sourcechannel + ": " + creceivers[sourcechannel].length);
            }
	    int destinationreceiver;
	    for(destinationreceiver = 0;
		destinationreceiver < creceivers[sourcechannel].length;
		destinationreceiver++) {
		IOPort connectedPort =
		    (IOPort) creceivers[sourcechannel][destinationreceiver].
		    getContainer();
		ComponentEntity connectedActor =
		    (ComponentEntity) connectedPort.getContainer();
		// Only proceed if the connected actor is something we are
		// scheduling.  The most notable time when this will not be
		// true is when a connections is made to the
		// inside of an opaque port.
		if(actorList.contains(connectedActor)) {
		    int destinationchannel =
			_getChannel(connectedPort,
                                creceivers[sourcechannel]
                                [destinationreceiver]
				    );
		    int[] tokens = (int[]) waitingTokens.get(connectedPort);
		    tokens[destinationchannel] = Integer.MAX_VALUE;
		    if (_debugging) {
                        _debug("Channel " + destinationchannel + " of " +
                                connectedPort.getName());
                    }
		}
	    }
	}
    }

    /** Simulate the consumption of tokens by the actor during an execution.
     *  The entries in Map will be modified to reflect the number of
     *  tokens still waiting after the actor has consumed tokens for a firing.
     *  Also determine if enough tokens still remain at the inputs of the actor
     *  for it to fire again immediately.
     *
     *  @param currentActor The actor that is being simulated.
     *  @param waitingTokens A Map between each input IOPort and the number of
     *  tokens in the queue for that port.
     *  @return boolean Whether or not the actor can fire again right away
     *  after it has consumed tokens.
     *  @exception IllegalActionException If any called method throws it.
     */
    private boolean _simulateInputConsumption(ComponentEntity currentActor,
            Map waitingTokens)
            throws IllegalActionException {

        boolean stillReadyToSchedule = true;
        // update tokensWaiting on the actor's input ports.

        Iterator inputPorts =
	    ((Actor) currentActor).inputPortList().iterator();
        while(inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int[] tokens =
		(int []) waitingTokens.get(inputPort);
	    int tokenrate =
		getTokenConsumptionRate(inputPort);
	    for(int channel = 0; channel < inputPort.getWidth(); channel++) {
		tokens[channel] -= tokenrate;

		// keep track of whether or not this actor can fire again
		// immediately
		if(tokens[channel] < tokenrate) stillReadyToSchedule = false;
	    }
	}
        return stillReadyToSchedule;
    }

    /**
     * Simulate the creation of tokens by the given output port when
     * its actor fires.  If any actors that receive tokens are then ready to
     * fire, given that only actors in the actor list are being scheduled, then
     * add those actors to the list of actors that are ready to schedule.
     * update the waiting tokens map with the tokens available on each
     * channel of each port.
     */
    private void _simulateTokensCreated(IOPort outputPort,
	    int createdTokens,
            LinkedList actorList,
            LinkedList readyToScheduleActorList,
            Map waitingTokens)
            throws IllegalActionException {

	Receiver[][] creceivers = outputPort.getRemoteReceivers();

	if (_debugging) {
            _debug("Creating " + createdTokens + " tokens on "
                    + outputPort.getFullName());
            _debug("source channels = " + creceivers.length);
        }
	int sourcechannel;
	for(sourcechannel = 0;
            sourcechannel < creceivers.length;
            sourcechannel++) {
	    if (_debugging) {
                _debug("destination receivers for channel "
                        + sourcechannel + ": " + creceivers[sourcechannel].length);
            }
	    int destinationreceiver;
	    for(destinationreceiver = 0;
		destinationreceiver < creceivers[sourcechannel].length;
		destinationreceiver++) {
		IOPort connectedPort =
		    (IOPort) creceivers[sourcechannel][destinationreceiver].
		    getContainer();
		ComponentEntity connectedActor =
		    (ComponentEntity) connectedPort.getContainer();
		// Only proceed if the connected actor is something we are
		// scheduling.  The most notable time when this will not be
		// true is when a connections is made to the
		// inside of an opaque port.
		if(actorList.contains(connectedActor)) {
		    int destinationchannel =
			_getChannel(connectedPort,
                                creceivers[sourcechannel]
                                [destinationreceiver]
				    );
		    int[] tokens = (int[]) waitingTokens.get(connectedPort);
		    tokens[destinationchannel] += createdTokens;
		    if (_debugging) {
                        _debug("Channel " + destinationchannel + " of " +
                                connectedPort.getName());
                    }
		    // Check and see if the connectedActor can be scheduled
		    int ival =
			_countUnfulfilledInputs((Actor)connectedActor,
                                actorList,
                                waitingTokens);
		    int firingsRemaining = _getFiringCount(connectedActor);
		    // If so, then add it to the proper list.  Note that the
		    // actor may appear more than once.  This is OK, since we
		    // remove all of the appearances from the list when the
		    // actor is actually scheduled.
		    if((ival <= 0) && (firingsRemaining > 0)) {
			readyToScheduleActorList.addLast(connectedActor);
		    }
		}
	    }
	}
    }

    /** Solve the Balance Equations for the list of connected Actors.
     *  For each actor, determine the ratio that determines the rate at
     *  which it should fire relative to the other actors for the graph to
     *  be live and operate within bounded memory.   This ratio is known as the
     *  fractional firing of the actor.
     *
     *  @param container The container that is being scheduled.
     *  @param actors The actors that we are interested in.
     *  @param externalRates A map from external ports of container to 
     *  the fractional rates of that port.
     *  @return A Map that associates each actor with its fractional
     *  firing.
     *  @exception NotSchedulableException If the graph is not consistent
     *  under the synchronous dataflow model.
     *  @exception NotSchedulableException If the graph is not connected.
     *  @exception IllegalActionException If any called method throws it.
     */
    private Map _solveBalanceEquations(CompositeActor container,
            List actorList, Map externalRates)
            throws NotSchedulableException, IllegalActionException {

        // firings contains the Map that we will return.
        // It gets populated with the fraction firing ratios for
        // each actor
        Map firings = new TreeMap(new NamedObjComparator());

        // remainingActors contains the pool of Actors that have not been
        // touched yet. (i.e. all their firings are still set to Fraction
	// equal to -1/1)
        LinkedList remainingActors = new LinkedList();

        // pendingActors have their firings set, but have not had their
        // ports explored yet.
        LinkedList pendingActors = new LinkedList();

        // Are we done?  (Is pendingActors Empty?)
        boolean done = false;

        // Initialize remainingActors to contain all the actors we were given 
        remainingActors.addAll(actorList);
        
        // Initialize firings for everybody to -1
        Iterator enumActors = remainingActors.iterator();
        while(enumActors.hasNext()) {
            ComponentEntity e = (ComponentEntity) enumActors.next();
            firings.put(e, new Fraction(-1));
        }

        // FIXME: this doesn't work if there are no actors (i.e. a tunneling
        // actor in a hierarchical model)
        try {
            // Pick an actor as a reference
	    // Should pick the reference actor to be one
	    // that contains 0-valued ports, if there exists one.
	    ComponentEntity a = null;
	    // Try to find an actor that contains at least one 0-rate
	    // port. Note that this preprocessing step only needs to
	    // be done if we want to support SDF graphs with actors that
	    // contain one or more 0-rate ports.
	    a = _pickZeroRatePortActor(remainingActors);
	    if (a == null) {
		// We did not find an actor with any 0-rate ports,
		// so just pick a reference actor arbitrarily.
		a = (ComponentEntity) remainingActors.removeFirst();
	    } else {
		// We found an actor "a" with at least one 0-rate port.
		remainingActors.remove(a);
	    }

            // And set it's rate to one per iteration
            firings.put(a, new Fraction(1));
            // And start the list to recurse over.
            pendingActors.addLast(a);
        }
        catch (NoSuchElementException e) {
            // if remainingActors.removeFirst() fails, then we've been given
            // no actors to do anything with, so return an empty Map
            return firings;
        }

        while(!done) try {
            // NOTE: Do not move this debug clause after the removeFirst()
            // call... that causes an infinite loop!
            if (_debugging) {
                _debug("pendingActors: ");
                _debug(pendingActors.toString());
            }
            // Get the next actor to recurse over
            Actor currentActor = (Actor) pendingActors.removeFirst();
            if (_debugging) {
                _debug("Balancing from " +
                        ((ComponentEntity) currentActor).getName());
            }

            // traverse all the input and output ports, setting the firings
            // for the actor(s)???? that each port is connected to relative
            // to currentActor.
            Iterator AllPorts =
                ((ComponentEntity) currentActor).portList().iterator();
            while(AllPorts.hasNext()) {
                IOPort currentPort = (IOPort) AllPorts.next();
                _propagatePort(container, currentPort, firings, externalRates,
                        remainingActors, pendingActors);
            }
        }
        catch (NoSuchElementException e) {
            // Once we've exhausted pendingActors, this exception will be
            // thrown, causing us to terminate the loop.
	    // FIXME this is a bad way to do this.
            done = true;

            Iterator actors = remainingActors.iterator();

            // If there are any Actors left that we didn't get to, then
            // this is not a connected graph, and we throw an exception.
            if(actors.hasNext()) {
                String msg = "SDF scheduler found disconnected actors: ";
                while (actors.hasNext()) {
                    NamedObj actor = (NamedObj)(actors.next());
                    msg += actor.getFullName() + " ";
                }
                throw new NotSchedulableException(msg);
            }
        }
        return firings;
    }

    /** Find the channel number of the given port that corresponds to the
     *  given receiver.  If the receiver is not contained within the port,
     *  throw an InternalErrorException.
     */
    // FIXME: Move this functionality to the kernel.
    private int _getChannel(IOPort port, Receiver receiver)
            throws IllegalActionException {
	int width = port.getWidth();
	Receiver[][] receivers = port.getReceivers();
	int channel;
	if (_debugging) {
            _debug("-- getting channels on port " + port.getFullName());
            _debug("port width = " + width);
            _debug("number of channels = " + receivers.length);
        }
	for(channel = 0; channel < receivers.length; channel++) {
	    int receivernumber;
	    if (_debugging) {
                _debug("number of receivers in channel " + channel
                        + " = " + receivers[channel].length);
            }
	    for(receivernumber = 0;
		receivernumber < receivers[channel].length;
		receivernumber++)
		if(receivers[channel][receivernumber] == receiver) {
                    if (_debugging) {
                        _debug("-- returning channel number:" + channel);
                    }
                    return channel;
                }
	}
	// Hmm...  didn't find it yet.  Port might be connected on the inside,
	// so try the inside relations.
	receivers = port.getInsideReceivers();
	for(channel = 0; channel < receivers.length; channel++) {
	    int receivernumber;
	    if (_debugging) {
                _debug("number of insidereceivers = "
                        + receivers[channel].length);
            }
	    for(receivernumber = 0;
		receivernumber < receivers[channel].length;
		receivernumber++) {
		if(receivers[channel][receivernumber] == receiver) {
                    return channel;
                }
            }
	}

	throw new InternalErrorException("Receiver not found in the port " +
                port.getFullName());
    }

    /** Try to find an actor that contains at least one 0-rate
     *  port. Note that this preprossessing step only needs to
     *  be done if we want to support SDF graphs with actors that
     *  contain one or more 0-rate ports.
     *
     * @param remainingActors The list of all of the actors in the
     *  SDF graph, not in any particular order.
     * @return An actor that contains at least one 0-rate port.
     *  Otherwise return null.
     */
    private ComponentEntity _pickZeroRatePortActor(List remainingActors)
    throws IllegalActionException {
	ComponentEntity a = null;
	    Iterator remainingActorsIter = remainingActors.iterator();
	    boolean foundZeroPortActor = false;
	    while (remainingActorsIter.hasNext()) {
		ComponentEntity tempActor = 
		    (ComponentEntity)remainingActorsIter.next();
		// Check if this actor has any ports with rate = 0.
		Iterator thePorts =
                ((ComponentEntity) tempActor).portList().iterator();
		while (thePorts.hasNext()) {
		    // Get the next port.
		    IOPort currentPort = (IOPort)thePorts.next();
		    
		    int currentRate = -1; 
		    if(currentPort.isInput()) {
			currentRate = 
			    getTokenConsumptionRate(currentPort);
		    } else if (currentPort.isOutput()) {
			currentRate = 
			    getTokenProductionRate(currentPort);
		    }
		    if (currentRate == 0) {
			foundZeroPortActor = true;
			a = tempActor;
		    }
		}
	    }
	    return a;
    }

    /** A comparator for Named Objects.  This is currently SLOW because
     *  getFullName is not cached.
     */
    private class NamedObjComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if((o1 instanceof NamedObj)&&(o2 instanceof NamedObj)) {
                // Compare names.
                NamedObj n1 = (NamedObj) o1;
                NamedObj n2 = (NamedObj) o2;
                int compare = n1.getFullName().compareTo(n2.getFullName());
                if(compare != 0) return compare;
                // Compare class names.
                Class c1 = n1.getClass();
                Class c2 = n2.getClass();
                compare = c1.getName().compareTo(c2.getName());
                if(compare != 0) return compare;
                if(o1.equals(o2))
                    return 0;
                else {
                    // FIXME This should never happen, hopefully.  Otherwise
                    // the comparator needs to be made more specific.
                    throw new InternalErrorException("Comparator not " +
                            "capable of comparing not equal objects.");
                }
            } else
                throw new ClassCastException("Object must be NamedObjs");
        }
    }

    private Map _firingvector;
    private boolean _firingvectorvalid;
}
