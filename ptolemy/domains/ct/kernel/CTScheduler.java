/* A static scheduler for the continuous time domain.

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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTScheduler
/**
Static scheduler for the CT domain.
A CT (sub)system can be mathematically represented as:<Br>
<pre>
<pre>    dx/dt = f(x, u, t)<Br>
<pre>    y = g(x, u, t)<BR>
</pre></pre></pre>
where x is the state of the system, u is the input, y is the output,
f() is the state transition map and g() is the output map.
<P>
The system is built by actors. That is, all the functions, f() and g(),
are built up by chains of actors.  For high order systems,
x is a vector, which is built up by more than one integrators.
In general, actors that have the functionality of integration
from their inputs to their outputs are called <I>dynamic actors</I>.
Other actors are called <I>arithmetic actors</I>.
<P>
In order to interact with discrete domains, some actors in the
CT domain are able to convert continuous waveforms to discrete events,
and vice versa. An actor that has continuous input and discrete
output is call an <I>event generator</I>; an actor that has
discrete input and continuous output is called a
<I>waveform generator</I>.
<P>
The interaction with some discrete domains requires that the
CT simulation be able to remember its state and roll-back
to the remembered state when needed. This in turn requires
that all actors which have internal states should be able
to remember and restore their states. These actors are called
<I>stateful actors</I>.
<P>
In the continuous time simulation, time progresses in a discrete way.
The distance between consecutive simulation time points are called
<I>integration step size</I> or step size, in short. Some actors
require specific step sizes of the simulation. These actors are
called <I>step size control actors</I>. Examples of step size
control actors include integrators, which control the
accuracy and speed of numerical ODE solutions, and some event
generators, which detect events.
<P>
To help the scheduling, a system topology is partitioned into
several clusters:
the <I>arithmetic actors</I>, the <I>dynamic actors</I>,
the <I>step size control actors</I>, the <I>sink actors</I>,
the <I>stateful actors</I>, the <I> event generators</I>,
and the <I> waveform generators</I>.
This scheduler uses the clustered information and the system topology,
to provide the firing sequences for evaluating f() and g().
It also provides a firing order for all the dynamic actors.
The firing sequence for evaluating f() is
called the <I> state transition schedule</I>; the firing
sequence for evaluating g() is called the <I> output schedule</I>;
and the firing sequence for dynamic actors is called the
<I>dynamic actor schedule</I>.
<P>
The state transition schedule is the actors in f() function sorted
in the topological order, such that, after the integrators emit their
state x, a chain of firings according to the schedule evaluates the
f() function and returns tokens corresponding to dx/dt to the
integrators.
<P>
The output schedule is the actors in g() function sorted in the topological
order.
<P>
The dynamic actor schedule is a list of dynamic actors in their reverse
topological order.
<P>
If there are loops of arithmetic actors or loops of integrators,
then the (sub)system is not schedulable, and a NotSchedulableException
will be thrown if schedules are requested.

@author Jie Liu
@version $Id$
@see ptolemy.actor.sched.Scheduler
*/

public class CTScheduler extends Scheduler {

    /** Construct a CT scheduler in the default workspace
     *  with the default name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     */
    public CTScheduler() {
        this(null);
    }

    /** Construct a CT scheduler in the given workspace
     *  with the name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     *
     *  @param workspace The workspace.
     */
    public CTScheduler(Workspace workspace) {
        super(workspace);
        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setting name to a CTScheduler");
        }
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
    public CTScheduler(Director container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////

    // Signal type: continuous
    public static Integer CONTINUOUS = new Integer(1);

    // Signal type: discrete
    public static Integer DISCRETE = new Integer(-1);

    // Signal type: unknown
    public static Integer UNKNOWN = new Integer(0);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return the predecessors of the given actor in the topology within
     *  this opaque composite actor.
     *  If the argument is null, returns null.
     *  If the actor is a source, returns an empty list.
     *  @param The specified actor.
     *  @return The list of predecessors, unordered.
     */
    public List predecessorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList predecessors = new LinkedList();
        Iterator inPorts = actor.inputPortList().iterator();
        while(inPorts.hasNext()) {
            IOPort port = (IOPort) inPorts.next();
            Iterator outPorts = port.deepConnectedOutPortList().iterator();
            while(outPorts.hasNext()) {
                IOPort outPort = (IOPort)outPorts.next();
                Actor pre = (Actor)outPort.getContainer();
                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector()
                        == pre.getExecutiveDirector()) &&
                        !predecessors.contains(pre)) {
                    predecessors.addLast(pre);
                }
            }
        }
        return predecessors;
    }

    /** Return the successive actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a sink, returns an empty list.
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors.
     */
    public List successorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList successors = new LinkedList();
        Iterator outports = actor.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort outPort = (IOPort) outports.next();
            Iterator inPorts = outPort.deepConnectedInPortList().iterator();
            while(inPorts.hasNext()) {
                IOPort inPort = (IOPort)inPorts.next();
                Actor post = (Actor)inPort.getContainer();
                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector()
                        == post.getExecutiveDirector()) &&
                        !successors.contains(post)) {
                    successors.addLast(post);
                }
            }
        }
        return successors;
    }

    /** Return all the scheduling information in a Sting.
     *  @return All the schedules.
     */
    public String toString() {
        return getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the CTSchedule. Caching of the schedule is done
     *  in the director class, so this method does not test
     *  for the validation of the schedule.
     */
    protected Schedule _getSchedule() throws NotSchedulableException,
            IllegalActionException {
        // This implementation creates new Lists every time,
        // If this hurts performance a lot, consider reuse old lists.
        // That requires Schedule class to implement clear().
        CTSchedule ctSchedule = new CTSchedule();

        SignalTypes signalTypes = new SignalTypes();

        LinkedList sinkActors = new LinkedList();
        LinkedList dynamicActors = new LinkedList();
        LinkedList discreteActors = new LinkedList();
        LinkedList arithmeticActors = new LinkedList();
        LinkedList eventGenerators = new LinkedList();
        LinkedList waveformGenerators = new LinkedList();
        LinkedList continuousActors = new LinkedList();

        Schedule discreteActorSchedule = new Schedule();
        Schedule continuousActorSchedule = new Schedule();
        Schedule dynamicActorSchedule = new Schedule();
        Schedule eventGeneratorSchedule = new Schedule();
        Schedule outputSchedule = new Schedule();
        Schedule outputSSCActors = new Schedule();
        Schedule stateTransitionSchedule = new Schedule();
        Schedule statefulActors = new Schedule();
        Schedule stateSSCActors = new Schedule();
        Schedule waveformGeneratorSchedule = new Schedule();

        // classify actors and fill in unordered schedules.
        CompositeActor container =
            (CompositeActor)getContainer().getContainer();

        // We clone the list of all actors and will remove discrete actors
        // from them.
        continuousActors = (LinkedList)
            ((LinkedList)container.deepEntityList()).clone();

        Iterator allActors = container.deepEntityList().iterator();
        while(allActors.hasNext()) {

            Actor a = (Actor) allActors.next();
            //System.out.println("examine " + ((Nameable)a).getFullName());

            // Now classify actors by their interfaces.
            // Event generators are treated as sinks, and
            // waveform genterators are treated as sources.
            // Note that this breaks some causality loops.

            if(a instanceof CTStatefulActor) {
                statefulActors.add(new Firing(a));
            }
            if (a instanceof CTWaveformGenerator) {
                waveformGenerators.add(a);
            }
            if (a instanceof CTEventGenerator) {
                eventGenerators.add(a);
            }

            if (a instanceof CTDynamicActor) {
                dynamicActors.addLast(a);
            }else if (!(a instanceof CTWaveformGenerator)) {
                arithmeticActors.add(a);
            }

            // Now resolve signal types to find the continuous and
            // discrete cluster.
            if (a instanceof SequenceActor) {
                if (predecessorList(a).isEmpty()) {
                    throw new NotSchedulableException(((Nameable)a).getName()
                            + " is a SequenceActor, which cannot be a"
                            + " source actor in the CT domain.");
                }
                Iterator ports = ((Entity)a).portList().iterator();
                while(ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    signalTypes.setType(port, DISCRETE);
                    if (port.isOutput()) {
                        signalTypes.propagateType(port);
                    }
                }
            } else if ((a instanceof TypedCompositeActor) &&
                    !(a instanceof CTStepSizeControlActor)) {
                // Opaque composite actors that are not CTComposite actors
                // are treated as DISCRETE actors.
                Iterator ports = ((Entity)a).portList().iterator();
                while(ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    signalTypes.setType(port, DISCRETE);
                    if (port.isOutput()) {
                        signalTypes.propagateType(port);
                    }
                }
            } else {
                // Other signal types are obtained from parameters on ports.
                Iterator ports = ((Entity)a).portList().iterator();
                while(ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    Parameter signalType =
                        (Parameter)port.getAttribute("signalType");
                    if (signalType != null) {
                        String type = ((StringToken)signalType.getToken()).
                            stringValue();
                        type = type.trim().toUpperCase();
                        if (type.equals("CONTINUOUS")) {
                            signalTypes.setType(port, CONTINUOUS);
                            if (port.isOutput()) {
                                signalTypes.propagateType(port);
                            }
                        } else if (type.equals("DISCRETE")) {
                            signalTypes.setType(port, DISCRETE);
                            if (port.isOutput()) {
                                signalTypes.propagateType(port);
                            }
                        } else {
                            throw new InvalidStateException(port,
                                    " signalType not understandable.");
                        }
                    } else if (a instanceof CTCompositeActor) {
                        // Assume all it ports to be continuous unless
                        // otherwise specified.
                        signalTypes.setType(port, CONTINUOUS);
                        if (port.isOutput()) {
                            signalTypes.propagateType(port);
                        }
                    }
                }
                // If it is a domain polymorphic source, then treat
                // it outputs as CONTINUOUS, unless otherwise specified.
                if (predecessorList(a).isEmpty()) {
                    ports = ((Entity)a).portList().iterator();
                    while(ports.hasNext()) {
                         IOPort port = (IOPort)ports.next();
                         if (signalTypes.getType(port).equals(UNKNOWN)) {
                             signalTypes.setType(port, CONTINUOUS);
                             if (port.isOutput()) {
                                 signalTypes.propagateType(port);
                             }
                         }
                    }
                }
            }
        }

        // Done with all actor classification and known port signal
        // type assignment.

        // Now we propagate the signal types by topological sort.
        // First make sure that there is no causality loop of arithmetic
        // actor. This makes other graph reachability algorithms terminate.
        DirectedAcyclicGraph arithmeticGraph = _toGraph(arithmeticActors);
        if(!arithmeticGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Arithmetic loops are not allowed in the CT domain.");
        }
        DirectedAcyclicGraph dynamicGraph = _toGraph(dynamicActors);
        if(!dynamicGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Loops of dynamic actors (e.g. integrators) " +
                    "are not allowed in the CT domain. You may insert a " +
                    "Scale actor with factor 1.");
        }

        // Find the continuous and discrete clusters by propagating signal
        // type constrains.
        // Notice that signal types of dynamic actors and source actors
        // and waveform generators has already propagated by one step.
        // So, we can start with the arithmetic actors only.

        Object[] sortedArithmeticActors = arithmeticGraph.topologicalSort();
        // Examine and propagete signal types.
        for (int i = 0; i < sortedArithmeticActors.length; i++ ) {
            Actor actor = (Actor)sortedArithmeticActors[i];
            // Note that the signal type of the input ports should be set
            // already. If all input ports are CONTINUOUS, and the
            // output ports are UNKNOWN, then all output ports should be
            // CONTINUOUS. If all input ports are DISCRETE, and the
            // output ports are UNKNOWN, then all output ports should be
            // DISCRETE. If some input ports are continuous and some
            // input ports are discrete, then the output port type must
            // be set manually, which mean they have been resolved by now.
            Iterator inputPorts = actor.inputPortList().iterator();
            Integer knownType = UNKNOWN;
            boolean needManuallySetType = true;
            while (inputPorts.hasNext()) {
                IOPort port = (IOPort)inputPorts.next();
                if (port.getWidth() != 0) {
                    Integer type = signalTypes.getType(port);
                    if (type.equals(UNKNOWN)) {
                        throw new NotSchedulableException("Cannot resolve "
                                + "signal type for port "
                                + port.getFullName());
                    } else if (knownType == UNKNOWN) {
                        knownType = type;
                        needManuallySetType = false;
                    } else if (!knownType.equals(type)) {
                        needManuallySetType = true;
                        break;
                    }
                }
            }
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                Integer type = signalTypes.getType(port);
                if (type.equals(UNKNOWN)) {
                    if (needManuallySetType) {
                        throw new NotSchedulableException("Cannot resolve "
                                + "signal type for port " + port.getFullName());
                    } else {
                        signalTypes.setType(port, knownType);
                    }
                }
                // If there's any inconsistency in the signal type,
                // then this method will throw exception.
                signalTypes.propagateType(port);
            }
        }

        // Now all ports are in the SignalTypes table. We classify
        // continuous and discrete actors.
        // Note that an actor is continuous if it has continuous ports;
        // an actor is discrete if it has discrete ports. So under this
        // rule, continuous actor set and discrete actor set may overlap.
        discreteActors = signalTypes.getDiscreteActors();
        continuousActors = signalTypes.getContinuousActors();

        // Notice that by now, we have all the discrete actors, but
        // they are not  in the topological order.
        // Now create the discrete schedule.
        DirectedAcyclicGraph discreteGraph = _toGraph(discreteActors);
        Object[] discreteSorted = discreteGraph.topologicalSort();
        for (int i = 0; i < discreteSorted.length; i++) {
            discreteActorSchedule.add(new
                    Firing((Actor)discreteSorted[i]));
        }

        // Actors remain in the continuousActors list are real continuous
        // actor. The normal (CT) scheduling should only apply to them.

        // Now we check whether there are any sequence actors or discrete
        // composite actors in the continuous part of the system.
        // Add all continuous actors in the continuous actors schedule.
        Iterator continuousIterator = continuousActors.iterator();
        while (continuousIterator.hasNext()) {
            Actor actor = (Actor)continuousIterator.next();
            if((actor instanceof SequenceActor) ||
                    ((actor instanceof CompositeActor) &&
                            (!(actor instanceof CTStepSizeControlActor)))) {
                throw new NotSchedulableException((Nameable)actor,
                        " is in the continuous cluster, but it is a "
                        + "sequence or discrete actor.");
            }
            continuousActorSchedule.add(new Firing(actor));
            // We test for sinks in the continuous cluster. These actors
            // are used to generate the output schedule. Event generators
            // in the continuous cluster are treated as sink actors.
            if (actor instanceof CTEventGenerator) {
                sinkActors.add(actor);
            } else {
                List successorList = successorList(actor);
                if(successorList.isEmpty()) {
                    sinkActors.add(actor);
                } else {
                    Iterator successors = successorList.iterator();
                    boolean isSink = true;
                    while (successors.hasNext()) {
                        Actor successor = (Actor)successors.next();
                        if (continuousActors.contains(successor)) {
                            isSink = false;
                            break;
                        }
                    }
                    if (isSink) {
                        sinkActors.add(actor);
                    }
                }
            }

        }

        // Create waveformGeneratorSchedule.
        Iterator generators = waveformGenerators.iterator();
        while(generators.hasNext()) {
            Actor generator = (Actor)generators.next();
            waveformGeneratorSchedule.add(new Firing(generator));
        }

        // Schedule event generators so that they are executed topologically.
        // And treat them as sinks.
        if (!eventGenerators.isEmpty()) {
            DirectedAcyclicGraph eventGraph = _toGraph(eventGenerators);
            Object[] eventSorted = eventGraph.topologicalSort();
            for (int i = 0; i < eventSorted.length; i++) {
                eventGeneratorSchedule.add(new Firing((Actor)eventSorted[i]));
                //if (continuousActors.contains(eventSorted[i])) {
                    // Also add them to the sink actor list in
                    // topological order.
                //  sinkActors.addLast(eventSorted[i]);
                //}
            }
        }

        // Now schedule dynamic actors and state transtion actors.
        // Manipulate on the arithmeticGraph and the dynamicGraph within
        // the continuous actors.

        arithmeticGraph = _toArithmeticGraph(continuousActors);
        if(!dynamicActors.isEmpty()) {
            Object[] dynamicArray = dynamicActors.toArray();
            // Dynamic actors are reverse ordered in the schedule.
            Object[] xSorted = dynamicGraph.topologicalSort(dynamicArray);
            for(int i = 0; i < xSorted.length; i++) {
                Actor a = (Actor)xSorted[i];
                // Looping on add(0, a) will reverse the order.
                dynamicActorSchedule.add(0, new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but addFirst() is
                    // considered more efficient.
                    stateSSCActors.add(new Firing(a));
                }
            }

            // State transition schedule
            Object[] fx = arithmeticGraph.backwardReachableNodes(dynamicArray);
            Object[] fxSorted = arithmeticGraph.topologicalSort(fx);
            for(int i = 0; i < fxSorted.length; i++) {
                Actor a = (Actor)fxSorted[i];
                stateTransitionSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but we try to keep
                    // a topological order anyway.
                    stateSSCActors.add(new Firing(a));
                }
            }
        }

        // Construct an array of sink actors.
        if(!sinkActors.isEmpty()) {
            Object[] sinkArray = sinkActors.toArray();
            // Output map.
            Object[] gx = arithmeticGraph.backwardReachableNodes(sinkArray);
            Object[] gxSorted = arithmeticGraph.topologicalSort(gx);
            for(int i = 0; i < gxSorted.length; i++) {
                Actor a = (Actor)gxSorted[i];
                outputSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    outputSSCActors.add(new Firing(a));
                }
            }
            // Add sinks to the output schedule. Note the ordering among
            // sink actors since we allow chains of event generators.
            Iterator sinks = sinkActors.iterator();
            while(sinks.hasNext()) {
                Actor a = (Actor)sinks.next();
                outputSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    outputSSCActors.add(new Firing(a));
                }
            }

        }

        // Create the CTSchedule. Note it must be done in this order.
        ctSchedule.add(discreteActorSchedule);
        ctSchedule.add(continuousActorSchedule);
        ctSchedule.add(dynamicActorSchedule);
        ctSchedule.add(eventGeneratorSchedule);
        ctSchedule.add(outputSchedule);
        ctSchedule.add(outputSSCActors);
        ctSchedule.add(stateTransitionSchedule);
        ctSchedule.add(statefulActors);
        ctSchedule.add(stateSSCActors);
        ctSchedule.add(waveformGeneratorSchedule);

        setValid(true);
        return ctSchedule;
    }

    /** Convert the given list of actors to a directed acyclic graph.
     *  CTDynamicActors are treated as sinks to break closed loops.
     *  Each actor in the argument is a node in the graph,
     *  each link between a pair of actors, except the output links
     *  from dynamic actors, is a edge between the
     *  corresponding nodes.
     *  The existence of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @param list The list of actors to be scheduled.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toArithmeticGraph(List list) {
        DirectedAcyclicGraph graph = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            graph.add(actor);
        }

        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if(!(actor instanceof CTDynamicActor) &&
               !(actor instanceof CTEventGenerator)) {
                // Find the successors of the actor
                Iterator successors = successorList(actor).iterator();
                while (successors.hasNext()) {
                    Actor successor = (Actor)successors.next();
                    if(list.contains(successor)) {
                        graph.addEdge(actor, successor);
                    }
                }
            }
        }
	return graph;
    }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are NOT treated as sinks. This method
     *  is used to construct the dynamic actor schedule.
     *  Each actor in the argument is a node in the graph,
     *  and each link between a pair of actors is a edge between the
     *  corresponding nodes.
     *  @param list The list of actors to be converted to a graph.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toGraph(List list) {

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            g.add(a);
        }
        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor) actors.next();
            // Find the successors of a
            Iterator successors = successorList(a).iterator();
            while (successors.hasNext()) {
                Actor s = (Actor) successors.next();
                if(list.contains(s)) {
                    g.addEdge(a, s);
                }
            }
        }
	return g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler.
    private static final String _STATIC_NAME = "CTScheduler";


    ////////////////////////////////////////////////////////////////////
    ////                        inner class                         ////

    // Inner class for signal type table. This wraps a HashMap, but
    // the put() method will check for conflicts. That is, if there
    // exist a map port --> CONTINUOUS, but a nother map port --> DISCRETE
    // is trying to be inserted, then a NotSchedulableException will be
    // thrown.

    private class SignalTypes {

        public SignalTypes() {
            _map = new HashMap();
            _continuousActors = new LinkedList();
            _discreteActors = new LinkedList();
        }

        /////////////////////////////////////////////////////////////////
        ////                     public methods                      ////

        // Return the list of actors with continuos ports.
        public LinkedList getContinuousActors() {
            return _continuousActors;
        }

        // Return the list of actors with discrete ports.
        public LinkedList getDiscreteActors() {
            return _discreteActors;
        }


        // Return the sigal type of the specified port.
        // @return CONTINUOUS, DISCRETE, or UNKNOWN

        public Integer getType(IOPort port)
                throws NotSchedulableException {
            if (!_map.containsKey(port)) {
                return UNKNOWN;
            } else {
                return (Integer)_map.get(port);
            }
        }

        // Check for consistency and set a port to the specific type.
        // the map.
        public void setType(IOPort port, Integer type)
                throws NotSchedulableException {
            if (!_map.containsKey(port)) {
                _map.put(port, type);
                Entity actor = (Entity)port.getContainer();
                if (type.equals(CONTINUOUS) &&
                        actor != CTScheduler.this.getContainer().getContainer()
                        && !_continuousActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is CONTINUOUS.");
                    _continuousActors.add(actor);
                }
                if (type.equals(DISCRETE) &&
                        actor != CTScheduler.this.getContainer().getContainer()
                        && !_discreteActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is DISCRETE.");
                    _discreteActors.add(actor);
                }
            } else {
               Integer previousType = (Integer)_map.get(port);
               if (!previousType.equals(type)) {
                   throw new NotSchedulableException(port.getFullName()
                           + " has a signal type conflict.");
               }
            }
        }

        // Set the type of all the connected input ports equal to the
        // type of the specified port. The caller must make sure that
        // the type of argument has already been set. Otherwise an
        // InternalErrorException will be thrown.
        // If any connected port already has a type and
        // it is not the same as the type to be set, then throw
        // a NonSchedulableException.
        public void propagateType(IOPort port)
                throws NotSchedulableException {
            if (!_map.containsKey(port)) {
                throw new InternalErrorException(port.getFullName()
                        + " type unknown.");
            }
            // Iterate over all ports that can receive data from this one.
            // This includes input ports lower in the hierarchy or output
            // ports higher in the hierarchy.
            int referenceDepth = port.depthInHierarchy();
            Iterator connectedPorts =
                   port.deepConnectedPortList().iterator();
            while(connectedPorts.hasNext()) {
                IOPort nextPort = (IOPort)connectedPorts.next();
                if ((nextPort.isInput() && nextPort.depthInHierarchy()
                        >= referenceDepth)
                        || (nextPort.isOutput() && nextPort.depthInHierarchy()
                        < referenceDepth)) {
                    if (!_map.containsKey(nextPort)) {
                        setType(nextPort, getType(port));
                    } else if (!getType(port).equals(getType(nextPort))) {
                        throw new NotSchedulableException(
                                "Signal type conflict: "
                                + port.getFullName() + " and "
                                + nextPort.getFullName());
                    }
                }
            }
        }

        /////////////////////////////////////////////////////////////////
        ////                     private variables                   ////
        // The HashMap.
        private HashMap _map;

        private LinkedList _continuousActors;

        private LinkedList _discreteActors;
    }
}
