/* Discrete Time (DT) domain director.

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

@ProposedRating Yellow (chf@eecs.berkeley.edu)
@AcceptedRating Yellow (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.actor.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.lib.SampleDelay;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DTDirector
/**

<h1>DT overview</h1>
The Discrete Time (DT) domain is a timed extension of the Synchronous Dataflow
(SDF) domain.  Like SDF, it has static scheduling of the dataflow graph
model. Similarly, DT requires that the data rates on the ports of all actors
be known beforehand and fixed. DT handles feedback systems in the same way
that SDF does, but with additional constraints on initial tokens.
<p>
<h1>Local and Global Time</h1>
Because of the inherent concurrency occuring within SDF models,
there are two notions of time in DT -- global time and local time.  Global time
increases steadily as execution progresses.  Moreover, global time increases by
fixed discrete amounts given by the <i>period</i> parameter. On the other hand,
each receiver is associated with an independent local time. All the
receivers have distinct local times as an iteration proceeds. The local time of
a receiver during an iteration depends on the global time, period, firing count,
port rates, and the schedule. These local times obey the following constraint:

<center>Global Time  <=  Local Time <= (Global Time + period)</center>

The exact way that local time increments during an iteration is described in
detail in the DTReceiver documentation.
<p>
<h1> Period Parameter </hi>
The DT director has a <i>period</i> parameter which specifies the amount
of time per iteration. For hierarchical DT, this period parameter only makes
sense on the top-level. The user cannot explicitly set the period parameter
for a DT subsystem inside another DT system. For heterogeneous hierarchies 
(e.g. DT inside DE or DT inside CT), the period parameter specifies the 
time interval between firings of the DT subsystem. The DT subsystem will
not fire on times that are not integer multiples of the period parameter.
<p>.
<h1>DT Features</h1>
The design of the DT domain is motivated by the following criteria:
<OL>
<LI>) Uniform Token Flow:  The time interval between tokens should be regular
    and unchanging.  This conforms to the idea of having sampled systems
    with fixed rates. Although the tokens flowing in DT do not keep internal
    time stamps, each actor can query the DT director for its own local time.
    This local time is uniformly increasing by a constant fraction of the
    director's <i>period</I>.  Local time is incremented every time the get()
    method is called to obtain a token.
<LI>) Causality: Tokens produced by an actor should only depend on tokens produced
    or consumed in the past. This makes sense because we don't expect an actor to
    produce a token before it can calculate the token's value.  For example,
    if an actor needs three tokens A, B, and C to compute token D, then the time
    when tokens A, B, and C are consumed should be earlier than or equal to
    the time when token D is produced.  Note that in DT, time does not get
    incremented due to computation.
<LI>) SDF-style semantics: Ideally, we want DT to be a timed-superset of SDF
    with compatible token flow and scheduling.  However, we can only
    approximate this behavior. It is not possible to have uniform token flow,
    causality, and SDF-style semantics at the same time.  Causality breaks
    for non-homogeneous actors in a feedback system when fully-compatible
    SDF-style semantics is adopted.  To remedy this situation, every actor
    in DT that has non-homogeneous input ports should produce initial tokens
    at each of its output ports.
</OL>
</p>
<p>
<h1> Design Notes</h1>
DT (Discrete Time) is a timed model of computation.  In order
to benefit from the internal time-keeping mechanism of DT, one should
use actors aware of time. For example, one should use TimedPlotter or
TimedScope instead of SequencePlotter or SequenceScope.
<p>
Top-level DT Directors have a <i>period</i> parameter that can be set by the
user.  Setting the period parameter of a non-top-level DT Director
under hierarchical DT has no meaning; and hence will be ignored.
<p>

@see ptolemy.domains.dt.kernel.DTReceiver
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.domains.sdf.kernel.SDFReceiver
@see ptolemy.domains.sdf.kernel.SDFScheduler


 @author C. Fong
 @version $Id$
*/
/* Fixme (known bugs)
 1.) Put more tests on this case: when events come in faster than the period of a DT
     composite actor (e.g clock feeding DT)
 2.) Put more tests on this case: when DT composite actor doesn't fire because there aren't
     enough tokens.
 */
public class DTDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DTDirector() {
    	super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public DTDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DTDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The period of the model.  This parameter must contain a
     *  DoubleToken.  Its default value is 1.0 .
     *  For homogeneous hierarchical DT (i.e. DT inside DT) , the period
     *  of the inside director cannot be set explicitly by the user.
     *  Instead, it will have a fixed value: "outsidePeriod / repetitions ",
     *  where 'outsidePeriod' is the period of the outside director; and
     *  'repetitions' is the firing count of the composite actor that contains
     *  the inside director. For heterogeneous hierarchical DT (i.e. DT inside
     *  DE or CT), the
     *  period parameter is used to determine how often the fireAt()
     *  method is called to request firing from the outside director.
     */
    public Parameter period;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. For this director the only
     *  relevant attribute is the <i>period</i> parameter.  
     *
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
    // -attributeChanged-
    // FIXME: handle period parameter mutations
        super.attributeChanged(attribute);
    }

    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().  If this director is not
     *  in the top-level, get the outside director's current time; and 
     *  check whether the returned time is an integer multiple of the 
     *  <i>period</i> parameter. If it is not, then don't fire. 
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        double presentTime;

        if (outsideDirector != null) {
            presentTime = outsideDirector.getCurrentTime();
        } else {
            presentTime = getCurrentTime();
        }
        // Some timed directors (such as CT) increment time after prefire()
        // and during fire(), so time may not be properly updated
        // before this stage of the execution.
        _checkValidTimeIntervals();
        _postfirereturns = true;

        if (!_isFiringAllowed) {
            return;
        }

        if (! _inputTokensAvailable) {
            return;
        }


        _debugViewSchedule();

        debug.println("DTDirector fire  " + presentTime);
        if (container == null) {
            throw new InvalidStateException("DTDirector " + getName() +
                    " fired, but it has no container!");
        } else {

            Scheduler scheduler = getScheduler();
            if (scheduler == null)
                throw new IllegalActionException("Attempted to fire " +
                        "DT system with no scheduler");
            Enumeration allactors = scheduler.schedule();
            while (allactors.hasMoreElements()) {

                Actor actor = (Actor)allactors.nextElement();
                _currentActiveActor = actor;

                boolean isFiringNonDTCompositeActor = false;

                if (actor instanceof CompositeActor) {
                    CompositeActor compositeActor = (CompositeActor) actor;
		            Director  insideDirector = compositeActor.getDirector();

		            if ( !(insideDirector instanceof DTDirector)) {
		                isFiringNonDTCompositeActor = true;
		                _insideDirector = insideDirector;
		            }
		        }

		        if (isFiringNonDTCompositeActor) {
		            _pseudoTimeEnabled = true;
		        }


                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging)
                    _debug("Firing " + ((Nameable)actor).getFullName());

                actor.fire();

                // note: short circuit evaulation here
                _postfirereturns = actor.postfire() && _postfirereturns;

                if (isFiringNonDTCompositeActor) {
		            _pseudoTimeEnabled = false;
		        }

		        _currentActiveActor = null;

            }
        }
        if ((outsideDirector != null) && _shouldDoInternalTransferOutputs) {
            _issueTransferOutputs();
        }
        // fire_
    }


    /** If an actor is firing, return the local time of the actor. 
     *  If the actor currently firing is an opaque composite actor
     *  that is not directed by DT, return that director's current time.
     *
     *  @return the current time
     */
    public double getCurrentTime() {
    // -getCurrentTime-
        double timeValue;

        if (_pseudoTimeEnabled == true) {
            timeValue = _insideDirector.getCurrentTime();
        } else {
            if (_currentActiveActor == null) {
                timeValue = _currentTime;
            } else {
                _DTActor dtActor = (_DTActor)
                                   _allActorsTable.get(_currentActiveActor);
                timeValue = dtActor._localTime;
                if (timeValue == 0.0 ) {
                    timeValue = _currentTime;
                }
            }
        }
        return timeValue;
    }


    /** Return the time value of the next iteration.
     *  
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        // FIXME: This is a currently a hack to get DT to work with CT
        return Double.MAX_VALUE;
    }


    /** Get the global time increment per iteration for this director.
     *  This is a convenience method for getting the period parameter.
     *  For hierarchical DT (DT inside DT), extra calculation is done
     *  to compute the period as a fraction of the outside period.
     *  @return The value of the period parameter.
     *  @exception IllegalActionException If the period parameter is
     *  is not of type DoubleToken or IntToken.
     */
    public double getPeriod() throws IllegalActionException {
    //  -getPeriod-
    //  FIXME: This method is very inefficient. Implementation should cache a
    //  private local _period variable instead. Also the implementation might
    //  need to update the inside DT director's period value
    //  FIXME: It is inefficient to calculate and set the inside
    //  DT director's period value at every call to this function
        Token periodToken;
        double periodValue = 0.0;
        Director outsideDirector;
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        boolean shouldUpdatePeriod = false;

        outsideDirector = _getOutsideDirector();
        if (outsideDirector instanceof DTDirector) {
            DTDirector outsideDTDirector = (DTDirector) outsideDirector;
            periodToken = outsideDTDirector.period.getToken();
            periodValue = 1.0 / outsideDTDirector._getRepetitions(container);
            // used as a multiplier to the actual value
            shouldUpdatePeriod = true;
        } else {
            periodToken = period.getToken();
            periodValue = 1.0;
            // used as a multiplier to the actual value
        }

        if (periodToken instanceof DoubleToken) {
            double storedValue = ((DoubleToken) periodToken).doubleValue();
            periodValue = periodValue * storedValue;
        } else if (periodToken instanceof IntToken) {
            double storedValue = ((IntToken) periodToken).intValue();
            periodValue = periodValue * storedValue;
        } else {
            throw new IllegalActionException(
                  "Illegal DT period parameter value");
        }
        if (shouldUpdatePeriod) {
            period.setToken(new DoubleToken(periodValue));
        }
        return periodValue;
    }


    /** Initialize all the actors associated with this director by calling
     *  super.initialize(). Determine which actors need to generate
     *  initial tokens for causality. All actors with nonhomogeneous input
     *  ports will need to generate initial tokens for all of their output
     *  ports. For example, if actor A has a nonhomogeneous input port and an
     *  output port with production rate 'm' then actor A needs to produce 'm'
     *  initial tokens on the output port.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
    //  -initialize-

        _requestRefireAt(0.0);
        _actorTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _receiverTable = new ArrayList();
        _buildReceiverTable();
        _buildActorTable();
        _buildOutputPortTable();
        super.initialize();

      // This portion figures out which actors should generate initial tokens
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            IOPort currentPort = currentReceiver.getContainer();
            int rate = 0;
            Actor actor = (Actor) currentPort.getContainer();
            String name = ((Nameable)actor).getFullName();

            _DTActor dtActor = (_DTActor) _allActorsTable.get(actor);
            debug.println(dtActor);
            if (dtActor == null) {
                throw new IllegalActionException(
                          "DT internal error: unknown actor");
            }

            Parameter param = (Parameter) currentPort.getAttribute("tokenConsumptionRate");
    	    if ((param != null)&&(currentPort.isInput())) {
               rate = ((IntToken)param.getToken()).intValue();
               if (rate > 1) dtActor._shouldGenerateInitialTokens = true;
            }
    	}
        _debugViewActorTable();

        // This portion generates the initial tokens for actors with nonhomogeneous outputs
        receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            TypedIOPort currentPort = (TypedIOPort) currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            TypedIOPort fromPort = currentReceiver.getSourcePort();
            Type fromType = fromPort.getType();
            Actor fromActor = (Actor) fromPort.getContainer();
            Parameter param = (Parameter) fromPort.getAttribute("tokenProductionRate");
            int outrate = 0;
            if ((param != null) && (fromPort.isOutput())) {
                outrate = ((IntToken)param.getToken()).intValue();
            }
            SDFScheduler currentScheduler = (SDFScheduler) getScheduler();

            String name = ((Nameable)fromActor).getFullName();

            _DTActor dtFromActor = (_DTActor) _allActorsTable.get(fromActor);

            if (dtFromActor != null) {
                if (dtFromActor._shouldGenerateInitialTokens) {
                    int numberInitialTokens = 
                                  currentScheduler.getTokenInitProduction(currentPort);
                    debug.prompt("initial port: "+fromType+
                                           " to "+currentPort.getType());
                    for(int j=0;j<outrate;j++) {
                        // FIXME:  should check what token basetype
                        // for the port and generate such.
                        // move this out of the loop
                        // FIXME: It might be a better idea to overwrite
                        // the contents of port parameter tokenInitProduction
                        // to hold the correct integer value of init. tokens
                        // FIXME: Put a new parameter on the port for the
                        // user to be able to put their own initial tokens;
                        // however some specific SDF actors may have their
                        // own buffers parameters that actually keep this
                        // initial tokens (similar to SampleDelay)
                        if (fromType.isEqualTo(BaseType.BOOLEAN)) {
                            currentReceiver.put(new BooleanToken(false));
                        } else if (fromType.isEqualTo(BaseType.DOUBLE)) {
                            currentReceiver.put(new DoubleToken(0.0));
                        } else if (fromType.isEqualTo(BaseType.INT)) {
                            currentReceiver.put(new IntToken(0));
                        }
                    }
                }
            }
        }
        _debugViewActorTable();
        _debugViewReceiverTable();
    }


    /** Process the mutation that occurred.  Notify the parent class about
     *  the invalidated schedule.  This method is called when an entity
     *  is instantiated under this director. This method is also
     *  called when a link is made between ports and/or relations.
     *  see also other mutation methods:
     *    
     *  @see ptolemy.kernel.util.NamedObj#attributeChanged
     *  @see ptolemy.kernel.util.NamedObj#attributeTypeChanged
     */
    public void invalidateSchedule() {
    //  -invalidateSchedule-
        super.invalidateSchedule();
    }

    /** Return a new receiver consistent with the DT domain.
     *
     *  @return A new DTReceiver.
     */
    public Receiver newReceiver() {
        return new DTReceiver();
    }

    /** Request the outside director to fire this director's container
     *  again for the next period.
     *
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
        _makeTokensAvailable();
        double timeIncrement = getPeriod();
        _currentTime = _formerValidTimeFired + timeIncrement;
        _requestRefireAt(_formerValidTimeFired + timeIncrement);
        if (! _isFiringAllowed) {
          return true;
        }

        boolean returnValue = super.postfire() && _postfirereturns;
        return returnValue;
        // When an actor's postfire_ returns false, whole model should stop.
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens. Always return
     *  true in order to allow firing or pseudo-firing. Pseudo-firing is
     *  needed when DT is interacting hierarchically with DE.
     *
     *  @exception IllegalActionException If the parent class throws
     *  it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
    //  -prefire-
        _inputTokensAvailable = super.prefire();
        return true;
    }


    /** Set the local time of an actor in the  model under
     *  this director. This method is called by the DTReceiver
     *  class and doesn't need to be called by any other classes.
     *
     *  @param newTime The new current simulation time.
     *  @param actor The actor to be assigned a new local time
     */
    public void setActorLocalTime(double newTime, Actor actor) {
        _DTActor dtActor = (_DTActor) _allActorsTable.get(actor);
        dtActor._localTime = newTime;
    }


    /** Set the current time of the model under this director.
     *  Setting the time back to the past is allowed in DT.
     *
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        // _currentTime is inherited from base Director
        _currentTime = newTime;
    }


    /** Override the base class method to make sure that enough tokens 
     *  are available to complete one iteration.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  input port. If any channel of the input port has no data, then
     *  that channel is ignored.  
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
    //  -transferInputs-
        if (_inputTokensAvailable) {
            return super.transferInputs(port);
        } else {
            return false;
        }
    }


    /** This is called by the outside director to get tokens
     *  from an opaque composite actor. Return true if data is
     *  transferred from an output port of the container to the
     *  ports it is connected to on the outside. This method differs
     *  from the base class method in that this method will transfer
     *  all available tokens in the receivers, while the base class
     *  method will transfer at most one token. This behavior is
     *  required to handle the case of non-homogeneous opaque
     *  composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */

    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
    //  -transferOutputs-
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        boolean returnValue = false;
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j = 0; j < insiderecs[i].length; j++) {
			            while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
                                port.send(i, t);
                                returnValue = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return returnValue;
    }



   
   /**  Reset this director to an uninitialized state.
    *
    *  @exception IllegalActionException If the parent class
    *  throws it
    */
    public void wrapup() throws IllegalActionException {
    //  -wrapup-
        super.wrapup();
        _reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Get the number of times an actor repeats in the schedule of an
     *  SDF graph.  If the actor does not exist, throw an exception.
     *
     *  @param actor The actor whose firing count is needed
     *  @exception IllegalActionException If actor does not exist.
     */
    protected int _getRepetitions(Actor actor) throws IllegalActionException {
        ListIterator actorIterator = _actorTable.listIterator();
        int repeats = 0;

        foundRepeatValue:
        while(actorIterator.hasNext()) {
            _DTActor currentActor = (_DTActor) actorIterator.next();
            if (actor.equals(currentActor._actor)) {
                repeats = currentActor._repeats;
                break foundRepeatValue;
            }
        }

        if (repeats == 0) {
            throw new IllegalActionException(
                      "internal DT error: actor with zero firing count");
        }
    	return repeats;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *
     *  @exception IllegalActionException If the scheduler is null
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null)
            throw new IllegalActionException("Attempted to fire " +
                    "DT system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();


        int actorsInSchedule = 0;
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            _DTActor dtActor = (_DTActor) _allActorsTable.get(actor);
            if (dtActor==null) {
              _allActorsTable.put(actor, new _DTActor(actor));
              dtActor = (_DTActor) _allActorsTable.get(actor);
              _actorTable.add(dtActor);
            }
            dtActor._repeats++;
            actorsInSchedule++;
        }

        // include the container as an actor.  This is needed for TypedCompositeActors
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new _DTActor((Actor)getContainer()));
        _DTActor dtActor = (_DTActor) _allActorsTable.get(actor);
        dtActor._repeats = 1;
        _actorTable.add(dtActor);

        _debugViewActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.determineEnds();
        }

        receiverIterator = _receiverTable.listIterator();

        while(receiverIterator.hasNext()) {
    	   DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
    	   currentReceiver.calculateDeltaTime();
        }

        _debugViewActorTable();
        _debugViewReceiverTable();
    }

    /** Build the internal cache of all the receivers directed by this
     *  director.
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildReceiverTable() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        if (container != null) {
            Iterator allActors = container.deepEntityList().iterator();
            while(allActors.hasNext()) {
                Actor actor = (Actor) allActors.next();
                // Get all input ports
                Iterator allInputs = actor.inputPortList().iterator();
                while(allInputs.hasNext()){
                    IOPort inputPort = (IOPort)allInputs.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if(receivers != null) {
                        for(int i = 0; i < receivers.length; i++) {
                            if (receivers[i] != null) {
                                for(int j = 0; j < receivers[i].length; j++) {
                                    if (receivers[i][j] != null) {
                                        _receiverTable.add(receivers[i][j]);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Also add the inside receivers in the ports of the
            // composite actor that contains this director.
            Iterator compositePorts = container.outputPortList().iterator();
            while(compositePorts.hasNext()) {
                IOPort outputPort = (IOPort)compositePorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if(receivers != null) {
                    for(int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for(int j = 0; j < receivers[i].length; j++) {
                                if (receivers[i][j] != null) {
                                    _receiverTable.add(receivers[i][j]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /** Build the internal cache of all the ports directed by this director
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        Iterator outports = container.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort port = (IOPort)outports.next();

            _outputPortTable.add(new _DTIOPort(port));
        }

    }

    /** Check if the current time is a valid time for execution. If the
     *  current time is not a integer multiple of the DT period, firing
     *  must not occur.
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private final void _checkValidTimeIntervals() throws IllegalActionException {
    //  -checkValidTimeIntervals-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        debug.println("shouldIgnoreFire subroutine called");

        // No need to check if this director is in the top level.
        if (outsideDirector == null) {
            _formerValidTimeFired = _currentTime;
            return;
        }

        // No need to check if the executive director is also a DTDirector
        if (outsideDirector instanceof DTDirector) {
            _formerValidTimeFired = _currentTime;
            return;
        }


        double currentTime = outsideDirector.getCurrentTime();
        double currentPeriod = getPeriod();
        double timeElapsed = currentTime - _formerValidTimeFired;

        debug.println("DT Director just started fire----------------"
                      +_formerValidTimeFired+" "+currentTime);


        if ((currentTime != 0) && (! _inputTokensAvailable) &&
            ((currentTime - _formerTimeFired) < _TOLERANCE )) {
           //  duplicate firings at the same time should be ignored
           //  unless there are input tokens
            _isFiringAllowed = false;
            _shouldDoInternalTransferOutputs = false;
            _makeTokensUnavailable();
            return;
        }  else {
            _formerTimeFired = currentTime;
        }

        // this occurs during startup
        if (currentTime == 0) {
            _formerValidTimeFired = currentTime;
            _issuePseudoFire(currentTime);
            _isFiringAllowed = true;
            return;
        }


        double iterationTimeElapsed = currentPeriod - timeElapsed;

        if (iterationTimeElapsed < -_TOLERANCE ) {
            // this case should not occur
            debug.prompt("InternalErrorException time: "+_formerValidTimeFired+
                                                     " "+currentTime);
            throw new InternalErrorException("unexpected time rollback");
        }

        if ((iterationTimeElapsed > _TOLERANCE) && (timeElapsed > _TOLERANCE)) {

            Iterator outputPorts = _outputPortTable.iterator();
            _isFiringAllowed = false;
            while(outputPorts.hasNext()) {
                Receiver[][] insideReceivers;
                _DTIOPort dtport = (_DTIOPort) outputPorts.next();

                insideReceivers = dtport._port.getInsideReceivers();
                double deltaTime = ((DTReceiver)insideReceivers[0][0]).getDeltaTime();
                double ratio = timeElapsed / deltaTime;

                if (Math.abs(Math.round(ratio) - ratio) < _TOLERANCE) {
                    // firing at a time when transferOutputs should be called
                    debug.println("*************** fractional fire ratio "
                                   +ratio+" should transferOutputs");
                    dtport._shouldTransferOutputs = true;
                    _isFiringAllowed = false;
                    _shouldDoInternalTransferOutputs = true;
                } else {
                // firing at a time when transferOutputs should not be called

                	for(int i=0;i<dtport._port.getWidth();i++) {
                	    for(int j=0;j<insideReceivers[i].length;j++) {
                	        DTReceiver receiver;

                	        receiver = (DTReceiver) insideReceivers[i][j];
                            receiver.overrideHasToken=true;
                	    }
            	    }
                    debug.println("******* nonfractional fire ratio "
                                  +ratio+" don't transferOutputs");
                    dtport._shouldTransferOutputs = false;
                }
            }
        } else if (_inputTokensAvailable)  {
            // this case occurs during period intervals
            // and enough input tokens are available

            _issuePseudoFire(currentTime);
            _formerValidTimeFired = currentTime;
            _isFiringAllowed = true;
            _shouldDoInternalTransferOutputs = false;
        } else {
            // this case occurs during period intervals
            // but not enough input tokens are available
            _formerValidTimeFired = currentTime;
            _isFiringAllowed = false;
            _shouldDoInternalTransferOutputs = false;
        }
    }


    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in
     *   obtaining the number of initial token for delay actors
     */
    private void _debugViewActorTable() throws IllegalActionException {

         debug.println("---------------------------------------");
         debug.println("\nACTOR TABLE with "+_actorTable.size()+" unique actors");
         ListIterator actorIterator = _actorTable.listIterator();
         while(actorIterator.hasNext()) {
            _DTActor currentActor = (_DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();

            debug.print(actorName+" repeats:"+currentActor._repeats);
            debug.print(" initial_tokens? "+currentActor._shouldGenerateInitialTokens);

            if (currentActor._actor instanceof SampleDelay) {
                SampleDelay delay = (SampleDelay) currentActor._actor;
                ArrayToken initialTokens = (ArrayToken) delay.initialOutputs.getToken();
                int delayCount = initialTokens.length();

                debug.print(" **DELAY** with "+delayCount+" initial tokens");
            }

            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
         }
    }


    /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     *
     *  @param obj The named object that has a list of attributes
     */
    private void _debugViewAttributesList(NamedObj obj)
    {
    	List list = obj.attributeList();
    	Iterator listIterator = list.iterator();

    	debug.println("attribute List:");
    	while(listIterator.hasNext()) {
    	    Attribute attribute = (Attribute) listIterator.next();
    	    debug.println(attribute);
    	}
    }

    /** For debugging purposes.  Display the list of output ports in the
     *  TypedCompositeActor that holds this director.
     */
    private void _debugViewContainerOutputPorts() throws IllegalActionException {

        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        debug.println("\ndirector container output port list:");
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            debug.println(" ->"+port);
            _debugViewPortInsideReceivers(port);
        }
        debug.println("\n");
    }


    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     *
     *  @param obj The composite entity with a list of contained entities.
     */
    private void _debugViewEntityList(CompositeEntity obj) {

        List list = obj.entityList();
    	Iterator listIterator = list.iterator();

    	debug.println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    debug.println(entity);
    	}
    	debug.println("\n");
    }

    /** For debugging purposes.  Display the list of inside receivers
     *  connected to a port.
     */
    private void _debugViewPortInsideReceivers(IOPort port)
                                        throws IllegalActionException {
        Receiver[][] portReceivers = port.getInsideReceivers();

    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<portReceivers[i].length;j++) {
    	        debug.println("  ->"+portReceivers[i][j]);
    	        ((DTReceiver)portReceivers[i][j])._debugViewReceiverInfo();
    	    }
    	}
    }


    /** For debugging purposes.  Display the list of remote receivers
     *  receivers connected to a port.
     */
    private void _debugViewPortRemoteReceivers(IOPort port) {
        Receiver[][] remoteReceivers = port.getRemoteReceivers();

    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<remoteReceivers[i].length;j++) {
    	        debug.println("  -->"+remoteReceivers[i][j]);
    	    }
    	}
    }

    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _debugViewReceiverTable() {
    //  -displayReceiverTable-
        debug.print("\nARC RECEIVER table with "+_receiverTable.size());
        debug.println(" unique receivers");

        ListIterator receiverIterator = _receiverTable.listIterator();

        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver._debugViewReceiverInfo();
        }
        debug.println("\n");
    }

    /** For debugging purposes. Display the schedule.
     */
    private void _debugViewSchedule() throws IllegalActionException {
        Scheduler scheduler = getScheduler();
        if (scheduler == null)
            throw new InternalErrorException("Attempted to use " +
                    "DT system with no scheduler");
        Enumeration allactors = scheduler.schedule();
        debug.println("--------SCHEDULE for "+getName()+"-----------------");
        while (allactors.hasMoreElements()) {

            Actor actor = (Actor)allactors.nextElement();
            debug.println(" --> "+((Nameable)actor).getName());
        }
    }



    /** Convenience method for getting the director of the container that
     *  holds this director.  If this director is inside a toplevel
     *  container, then the returned value is null.
     *  @returns The executive director
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        return outsideDirector;
    }

    /** Get the time of the outside director.
     *  If this is a top-level director, then return current time.
     *  @returns The time of the executive director
     */
    private double _getOutsideTime() throws IllegalActionException{
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        if (outsideDirector != null) {
            return outsideDirector.getCurrentTime();
        } else {
            return _currentTime;
        }
    }



    /** Convenience method for getting the token consumption rate of a
     *  specified port. If the port does not have the attribute
     *  "tokenConsumptionRate" then return a rate of 1.
     *  @param ioport The port to be queried
     *  @returns The token consumption rate of the port.
     *  @exception IllegalActionException If getting an attribute from
     *  this port fails.
     */
    private int _getTokenConsumptionRate(IOPort ioport) throws IllegalActionException {
        int rate;
        Parameter param = (Parameter) ioport.getAttribute("tokenConsumptionRate");
    	if (param != null) {
            rate = ((IntToken)param.getToken()).intValue();
        } else rate = 1;

        return rate;
    }

    /** Request the outside non-DT director to fire this TypedCompositeActor
     *  at time intervals equal to when the output tokens should be produced.
     *  No actual firing occurs of the inside actors will occur; hence the
     *  name 'pseudo-firing'
     */
    private void _issuePseudoFire(double currentTime)
                 throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        Receiver[][] insideReceivers;

        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            insideReceivers = port.getInsideReceivers();
            DTReceiver receiver = (DTReceiver) insideReceivers[0][0];
            double deltaTime = receiver.getDeltaTime();
            int periodDivider = receiver.getTokenFlowRate();
            debug.println("request pseudo-fire at "+deltaTime+" intervals. "+periodDivider);
            for(int n=1; n < periodDivider ;n++) {
                _requestRefireAt(currentTime + n * deltaTime);
                debug.println(" request pseudo-fire at "+(currentTime + n * deltaTime));
            }
        }
    }


    private void _issueTransferOutputs() throws IllegalActionException {
        Director outsideDirector = _getOutsideDirector();

        Iterator outputPorts = _outputPortTable.iterator();
        while(outputPorts.hasNext()) {
            _DTIOPort dtport = (_DTIOPort) outputPorts.next();

            if (dtport._shouldTransferOutputs) {
                outsideDirector.transferOutputs(dtport._port);
            }
        }
    }

    /** Enable the hasToken() method in the output ports of the
     *  TypedCompositeActor directed by this director.  This is
     *  used in composing DT with DE and CT.
     */
    private void _makeTokensAvailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();

    	    for(int i=0;i<port.getWidth();i++) {
    	        for(int j=0;j<portReceivers[i].length;j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken=false;
        	    }
    	    }
        }
    }


    /** Disble the hasToken() method in the output ports of the
     *  TypedCompositeActor directed by this director.  This is
     *  used in composing DT with DE and CT.
     */
    private void _makeTokensUnavailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();

    	    for(int i=0;i<port.getWidth();i++) {
    	        for(int j=0;j<portReceivers[i].length;j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken=true;
        	    }
    	    }
        }
    }


    /** Convenience method for asking the executive director to fire this
     *  director's container again at a specific time in the future.
     *  @param time The time when this director's container should be fired
     *  @exception IllegalActionException If getting the container or
     *  executive director fails
     */
    private void _requestRefireAt(double time) throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        if (outsideDirector != null) {
            outsideDirector.fireAt(container,time);
        }
    }


    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
    	try {
            period = new Parameter(this,"period",new DoubleToken(1.0));
            _reset();
            iterations.setToken(new IntToken(0));
            debug = new DTDebug(false);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
    	}
    }

    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _currentActiveActor = null;
        _allActorsTable = new Hashtable();
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _isFiringAllowed = true;
        _shouldDoInternalTransferOutputs = false;
    }





    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // ArrayList to keep track of all actors scheduled by DTDirector
    private ArrayList _actorTable;

    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList _receiverTable;

    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;

    // The current active actor during a firing
    private Actor _currentActiveActor;

    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;

    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;

    // The director of a non-DT internal model for which faking time
    // might be need. This variable is useful for mixed DT hierarchies
    private Director _insideDirector;

    // used to keep track of whether firing can be done at current time
    private boolean _isFiringAllowed;

    // ArrayList to keep track of all container output ports
    private ArrayList _outputPortTable;

    // Flag to specify whether time should be faked for non-DT internal
    // models (like DE, CT) in the hierarchy
    private boolean _pseudoTimeEnabled = false;

    // used to determine whether the director should call transferOutputs()
    private boolean _shouldDoInternalTransferOutputs;

    private boolean _inputTokensAvailable;

    // display for debugging purposes
    private DTDebug debug;


    // The tolerance value used when comparing time values.
    private static final double _TOLERANCE = 0.0000000001;




    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Inner class to cache important variables for contained actors
    private class _DTActor {
    	private Actor    _actor;
    	private double   _localTime;
    	private int      _repeats;
        private boolean  _shouldGenerateInitialTokens;

    	/* Construct the information on the contained Actor
    	 * @param a The actor
    	 */
    	public _DTActor(Actor actor) {
    		_actor = actor;
    		_repeats = 0;
    		_localTime = 0.0;
            _shouldGenerateInitialTokens = false;
    	}
    }

    // Inner class to cache important variables for container output ports
    private class _DTIOPort {
        private IOPort _port;
        private boolean _shouldTransferOutputs;

        /*  Construct the information on the output port
         *  @param p The port
         */
        public _DTIOPort(IOPort port) {
            _port = port;
            _shouldTransferOutputs = false;
        }
    }
}
