/* Director for the synchronous dataflow model of computation.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SDFDirector
/**
<h1>SDF overview</h1>
The Synchronous Dataflow(SDF) domain supports the efficient
execution of Dataflow graphs that
lack control structures.   Dataflow graphs that contain control structures
should be executed using the Process Networks(PN) domain instead.
SDF allows efficient execution, with very little overhead at runtime.  It
requires that the rates on the ports of all actors be known before hand.
SDF also requires that the rates on the ports not change during
execution.  In addition, in some cases (namely systems with feedback) delays,
which are represented by initial tokens on relations must be explicitly
noted.  SDF uses this rate and delay information to determine
the execution sequence of the actors before execution begins.
<h2>Schedule Properties</h2>
<ul>
<li>The number of tokens accumulated on every relation is bounded, given
an infinite number of executions of the schedule.
<li>Deadlock will never occur, given and infinite number of executions of
the schedule.
</ul>
<h1>Class comments</h1>
An SDFDirector is the class that controls execution of actors under the
SDF domain.  By default, actor scheduling is handled by the SDFScheduler
class.  Furthermore, the newReceiver method creates Receivers of type
SDFReceiver, which extends QueueReceiver to support optimized gets
and puts of arrays of tokens.
<p>
The SDF director has a single parameter, "iterations", corresponding to a
limit on the number of times the director will fire its hierarchy
before it returns false in postfire.  If this number is not greater
than zero, then no limit is set and postfire will always return true.
The default value of the iterations parameter is an IntToken with value zero.
@see ptolemy.domains.sdf.kernel.SDFScheduler
@see ptolemy.domains.sdf.kernel.SDFReceiver

@author Steve Neuendorffer
@version $Id$
*/
public class SDFDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     */
    public SDFDirector() {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param workspace The workspace for this object.
     */
    public SDFDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in postfire,
     *  and thus the execution can continue forever. Note that the amount
     *  of data processed by the SDF model is a function of both this
     *  parameter and the value of parameter <i>vectorizationFactor</i>, since
     *  <i>vectorizationFactor</i> can influence the choice of schedule.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** A Parameter representing the requested vectorization factor.
     *  The director will attempt to construct a single appearance
     *  schedule that minimizes actor activations, using the value
     *  of this parameter as the global blocking factor. This parameter
     *  serves only as a suggestion, and the director is free to ignore
     *  it or to use a different factor. Allowable values for this
     *  parameter consist of positive integers. An exception will occur
     *  if the value is not a positive integer. It is currently unsafe
     *  to set the value of this parameter to be greater than one if the SDF
     *  graph contains loops. The default value is an IntToken with the
     *  value one.
     */
    public Parameter vectorizationFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the interations member.  The new
     *  actor will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SDFDirector newObject = (SDFDirector)(super.clone(workspace));
        newObject.iterations =
            (Parameter)newObject.getAttribute("iterations");
	newObject.vectorizationFactor =
            (Parameter)newObject.getAttribute("vectorizationFactor");
        return newObject;
    }

    /** Calculate the current schedule, if necessary,
     *  and iterate the contained actors
     *  in the order given by the schedule.  No internal state of the
     *  director is updated during fire, so it may be used with domains that
     *  require this property, such as CT.
     *  <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's  prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());

        if (container == null) {
            throw new InvalidStateException("SDFDirector " + getName() +
                    " fired, but it has no container!");
        } else {
            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "SDF system with no scheduler");
	    Schedule sched = s.getSchedule();
	    Iterator firings = sched.firingIterator();
            while (firings.hasNext()) {
		Firing firing = (Firing)firings.next();
		Actor actor = (Actor)firing.getActor();
		int iterationCount = firing.getIterationCount();

		if(_debugging) {
                    _debug(new FiringEvent(this, actor, FiringEvent.ITERATE));
		}

		// FIXME: This is a hack. It does not even check if the
		// SDF graph contains loops, and may be far from optimal when
		// the SDF graph contains non-homogeneous actors. However,
		// the default value of vectorizationFactor = 1,
		// which should be completely safe for all models.
		// TODO: I need to modify the scheduler to generate an
		// optimum vectorized schedule. I.e., first try to
		// obtain a single appearance schedule. Then, try
		// to minimize the number of actor activations.
		int factor =
                    ((IntToken) (vectorizationFactor.getToken())).intValue();
		if (factor < 1) {
		    throw new IllegalActionException(this,
                            "The supplied vectorization factor is invalid " +
                            "Valid values consist of positive integers. " +
                            "The supplied value was: " + factor);
		}
		int returnVal =
                    actor.iterate(factor*iterationCount);
		if (returnVal == COMPLETED) {
		    _postfirereturns = _postfirereturns && true;
		} else if (returnVal == NOT_READY) {
		    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
		} else if (returnVal == STOP_ITERATING) {
		    _postfirereturns = false;
		}
            }
        }
    }

    /** Initialize the actors associated with this director and
     *  then compute the schedule.  The schedule is computed
     *  during initialization so that hierarchical opaque composite actors
     *  can be scheduled properly (since the act of computing the
     *  schedule sets the rate parameters of the external ports).
     *  The order in which the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        Scheduler s = getScheduler();
        if (s == null)
            throw new IllegalActionException("Attempted to initialize " +
                    "SDF system with no scheduler");
        // force the schedule to be computed.
        Schedule sched = s.getSchedule();
    }

    /** Return a new receiver consistent with the SDF domain.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  @exception IllegalActionException If port methods throw it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        _postfirereturns = true;

        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
	Iterator inputPorts = container.inputPortList().iterator();
	int inputCount = 0;
	while(inputPorts.hasNext()) {
	    IOPort inputPort = (IOPort) inputPorts.next();
	    int threshold = SDFScheduler.getTokenConsumptionRate(inputPort);
	    if (_debugging) {
                _debug("checking input " + inputPort.getFullName());
                _debug("Threshold = " + threshold);
            }
	    Receiver receivers[][] = inputPort.getReceivers();

	    int channel;
	    for(channel = 0; channel < inputPort.getWidth(); channel++) {
		if(!receivers[channel][0].hasToken(threshold)) {
		    if(_debugging) {
                        _debug("Channel " + channel +
                                " does not have enough tokens." +
                                " Prefire returns false on " +
                                container.getFullName());
                    }
                    return false;
		}
	    }
	}
	if(_debugging) _debug("Prefire returns true on " +
                container.getFullName());
	return true;
    }

    /** Preinitialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _iteration = 0;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.
     *  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        int numiterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if((numiterations > 0) && (_iteration >= numiterations)) {
            _iteration = 0;
            return false;
        }
        return _postfirereturns;
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.
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
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i = 0; i < port.getWidth(); i++) {
	    int rate = SDFScheduler.getTokenConsumptionRate(port);
	    for(int k = 0; k < rate; k++) {
                try {
                    ptolemy.data.Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debugging) _debug(getName(),
                                "transferring input from " + port.getName());
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                        trans = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "SDFDirector.transferInputs: Not enough tokens " +
                            ex.getMessage());
                }
            }
        }
        return trans;
    }

    /** Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
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
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j = 0; j < insiderecs[i].length; j++) {
			while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
                                port.send(i, t);
                                trans = true;
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
        return trans;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler.
     */
    private void _init() {
        try {
            SDFScheduler scheduler = 
                new SDFScheduler(this, uniqueName("Scheduler"));
        }
        catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }

        try {
            iterations
                = new Parameter(this,"iterations",new IntToken(0));
	    vectorizationFactor
                = new Parameter(this,"vectorizationFactor",new IntToken(1));
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iteration = 0;
    protected boolean _postfirereturns = true;
}
