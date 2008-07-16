/* Interface representing a dependency between ports of a composite actor.

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;


//////////////////////////////////////////////////////////////////////////
//// CausalityInterfaceForComposites

/**
 This class elaborates its base class by providing an algorithm for inferring
 the causality interface of a composite actor from the causality interfaces
 of its component actors and their interconnection topology.

 @author Edward A. Lee
 @version $Id: CausalityInterfaceForComposites.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 7.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class CausalityInterfaceForComposites extends DefaultCausalityInterface {
    
    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *   This is required to be an instance of CompositeEntity.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public CausalityInterfaceForComposites(
            Actor actor, Dependency defaultDependency) 
            throws IllegalArgumentException {
        super(actor, defaultDependency);
        if (!(actor instanceof CompositeEntity)) {
            throw new IllegalArgumentException(
                    "Cannot create an instance of " +
                    "CausalityInterfaceForComposites for " +
                    actor.getFullName()
                    +", which is not a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a collection of the ports in the associated actor that depend on
     *  or are depended on by the specified port. A port X depends
     *  on a port Y if X is an output and Y is an input and
     *  getDependency(X,Y) returns something not equal to
     *  the oPlusIdentity() of the default dependency specified
     *  in the constructor.
     *  <p>
     *  This class presumes (but does not check) that the
     *  argument is a port contained by the associated actor.
     *  If the actor is an input, then it returns a collection of
     *  all the outputs. If the actor is output, then it returns
     *  a collection of all the inputs.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public Collection<IOPort> dependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        HashSet<IOPort> result = new HashSet<IOPort>();
        if (port.isOutput()) {
            List<IOPort> inputs = _actor.inputPortList();
            if (inputs.size() != 0) {
                // Make sure _dependency is computed.
                getDependency(inputs.get(0), port);
                Map<IOPort,Dependency> map = _reverseDependencies.get(port);
                if (map != null) {
                    result.addAll(map.keySet());
                }
            }
        } else {
            List<IOPort> outputs = _actor.outputPortList();
            if (outputs.size() != 0) {
                // Make sure _dependency is computed.
                getDependency(port, outputs.get(0));
                Map<IOPort,Dependency> map = _forwardDependencies.get(port);
                if (map != null) {
                    result.addAll(map.keySet());
                }
            }
        }
        return result;
    }

    /** Return a set of the input ports in this actor that are
     *  in an equivalence class with the specified input.
     *  The returned result includes the specified input port.
     *  <p>
     *  An equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency not equal to the
     *  default depenency's oPlusIdentity() on any common port,
     *  or any two ports in an equivalence class,
     *  or on the state of the associated actor, then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. Otherwise, they are not in the same
     *  equivalence class. If there are no
     *  output ports, then include all the input ports
     *  are in a single equivalence class.
     *  In this base class, we assume the actor has no
     *  state and return the equivalence classes determined
     *  only by the common dependence of output ports.
     *  @param input The port to find the equivalence class of.
     *  @throws IllegalActionException If the argument is not
     *   contained by the associated actor.
     */
    public Collection<IOPort> equivalentPorts(IOPort input)
            throws IllegalActionException {
        if (input.getContainer() != _actor || !input.isInput()) {
            throw new IllegalActionException(input, _actor,
                    "equivalentPort() called with argument "
                    + input.getFullName()
                    + " which is not an input port of "
                    + _actor.getFullName());
        }
        // Make sure the data structures are up to date.
        getDependency(input, null);
        // The following must include at least the specified input port.
        return _equivalenceClasses.get(input);
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This is done by traversing
     *  the network of actors from the input ports. For each output
     *  port reachable from an input port, its dependency on the
     *  input port is determined by composing the dependencies along
     *  all paths from the input port to the output port using
     *  oPlus() and oTimes() operators of the dependencies.
     *  For any output port that is not reachable from an input
     *  port, the dependency on that input port is set to
     *  the oPlusIdentity() of the default dependency given
     *  in the constructor.
     *  <p>
     *  When called for the first time since a change in the model
     *  structure, this method performs the complete analysis of
     *  the graph and caches the result. Subsequent calls just
     *  look up the result. Note that the complete analysis
     *  can be quite expensive. For each input port, it traverses
     *  the graph to find all ports reachable from that input port,
     *  and tracks the dependencies. In the worst case, the
     *  complexity can be N*M^2, where N is the number of
     *  input ports and M is the total number of ports in the
     *  composite (including the ports of all contained actors).
     *  The algorithm used, however, is optimized for typical
     *  Ptolemy II models, so in most cases the algorithm completes
     *  in time on the order of N*D, where D is the length of
     *  the longest chain of ports from an input port to an
     *  output port.
     *  @param input The input port.
     *  @param output The output port, or null to update the
     *   dependencies (and record equivalence classes) without
     *   requiring there to be an output port.
     *  @return The dependency between the specified input port
     *   and the specified output port, or null if a null output
     *   is port specified.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        // Cast is safe because this is checked in the constructor
        CompositeEntity actor = (CompositeEntity)_actor;
        
        // If the dependency is not up-to-date, then update it.
        long workspaceVersion = actor.workspace().getVersion();
        if (_dependencyVersion != workspaceVersion) {
            // Need to update dependencies. The cached version
            // is obsolete.
            try {
                actor.workspace().getReadAccess();
                _reverseDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
                _forwardDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
                _equivalenceClasses = new HashMap<IOPort,Set<IOPort>>();
                // The following map keeps track for each port in the model which input
                // ports of the associated actor it depends on. This is used to build
                // the equivalence classes.
                Map<IOPort,Set<IOPort>> dependsOnInputsMap = new HashMap<IOPort,Set<IOPort>>();
                Iterator inputPorts = _actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort)inputPorts.next();
                    // Make sure that equivalentPorts() always returns at least a set
                    // with one element.
                    Set<IOPort> justTheInput= new HashSet<IOPort>();
                    justTheInput.add(inputPort);
                    _equivalenceClasses.put(inputPort, justTheInput);
                    
                    // Construct a map of dependencies from this inputPort
                    // to all reachable ports.
                    Map<IOPort,Dependency> map = new HashMap<IOPort,Dependency>();
                    Collection<IOPort> portsToProcess = inputPort.insideSinkPortList();
                    // Set the initial dependency of all the portsToProcess.
                    Iterator ports = portsToProcess.iterator();
                    while (ports.hasNext()) {
                        IOPort port = (IOPort)ports.next();
                        map.put(port, _defaultDependency.oTimesIdentity());
                    }
                    if (!portsToProcess.isEmpty()) {
                        _setDependency(
                                inputPort,
                                map,
                                portsToProcess,
                                dependsOnInputsMap);
                    }
                }
            } finally {
                actor.workspace().doneReading();
            }
            _dependencyVersion = workspaceVersion;
        }
        if (output == null) {
            return null;
        }
        Map<IOPort,Dependency> inputMap = _forwardDependencies.get(input);
        if (inputMap != null) {
            Dependency result = inputMap.get(output);
            if (result != null) {
                return result;
            }
        }
        // If there is no recorded dependency, then reply
        // with the additive identity (which indicates no
        // dependency).
        return _defaultDependency.oPlusIdentity();
    }
    
    /** Remove the dependency that the specified output port has
     *  on the specified input port. Specifically, calling this
     *  method ensures that subsequent calls to
     *  getDependency(inputPort, outputPort)
     *  will return defaultDependency.oPlusIdentity().
     *  It also adjusts what is returned by
     *  {@link #equivalentPorts(IOPort)} and
     *  {@link #dependentPorts(IOPort)}.
     *  @see #getDependency(IOPort, IOPort)
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     */
    public void removeDependency(IOPort inputPort, IOPort outputPort) {
        // First ensure that all dependencies are calculated.
        try {
            getDependency(inputPort, outputPort);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        Map<IOPort,Dependency> outputPorts = _forwardDependencies.get(inputPort);
        if (outputPorts != null) {
            outputPorts.remove(outputPort);
        }
        Map<IOPort,Dependency> inputPorts = _reverseDependencies.get(outputPort);
        if (inputPorts != null) {
            inputPorts.remove(inputPort);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////
    
    /** The workspace version where the dependency was last updated. */
    protected long _dependencyVersion;
    
    /** Computed equivalence classes of input ports. */
    protected Map<IOPort,Set<IOPort>> _equivalenceClasses;
    
    /** Computed dependencies between input ports and output ports of the associated actor. */
    protected Map<IOPort,Map<IOPort,Dependency>> _forwardDependencies;
    
    /** Computed reverse dependencies (the key is now an output port). */
    protected Map<IOPort,Map<IOPort,Dependency>> _reverseDependencies;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Record a dependency of the specified port on the specified
     *  input port in the specified map. The map records all the
     *  dependencies for this particular input port.
     *  If there was a prior dependency already
     *  that was less than this one, then update the dependency
     *  using its oPlus() method. If the dependency is equal
     *  to the oPlusIdentity(), then do not record it and return false.
     *  Return true if the dependency was newly set or modified from
     *  a previously recorded dependency. Return false if no change
     *  was made to a previous dependency.
     *  @param inputPort The source port.
     *  @param port The destination port, which may be an output
     *   port of the associated actor, or any port in a contained
     *   actor.
     *  @param map The map in which to record the dependency.
     *  @param dependency The dependency map for ports reachable from the input port.
     *  @param dependsOnInputsMap The map from ports in the model to input ports
     *   that they depend on, used to construct the equivalence classes.
     *  @return True if the dependency was changed.
     */
    private boolean _recordDependency(
            IOPort inputPort,
            IOPort port,
            Map<IOPort,Dependency> map,
            Dependency dependency,
            Map<IOPort,Set<IOPort>> dependsOnInputsMap)
            throws IllegalActionException {
        if (dependency.equals(_defaultDependency.oPlusIdentity())) {
            return false;
        }
        // First, update the equivalence classes.
        // Construct a set that merges all known input port dependencies
        // for this port with any known equivalents of the input port.
        Set<IOPort> merged = _equivalenceClasses.get(inputPort);
        if (merged == null) {
            merged = new HashSet<IOPort>();
            merged.add(inputPort);
            _equivalenceClasses.put(inputPort, merged);
        }
        
        // If the port is not already entered in the dependsOnInputsMap,
        // then enter it. The entry will eventually be
        // all the actor input ports that this port depends on.
        Set<IOPort> dependsOn = dependsOnInputsMap.get(port);
        if (dependsOn != null) {
            // Make sure to include any previously found dependencies.
            merged.addAll(dependsOn);
        }
        // If this port has equivalents, and those have dependencies,
        // then those dependencies need to be added. It can only have
        // equivalents if it is an input port.
        if (port.isInput()) {
            Collection<IOPort> equivalents
                    = ((Actor)port.getContainer()).getCausalityInterface().equivalentPorts(port);
            for (IOPort equivalent : equivalents) {
                // This is guaranteed to include port.
                Set<IOPort> otherInputs = dependsOnInputsMap.get(equivalent);
                if (otherInputs != null) {
                    merged.addAll(otherInputs);
                    // For each of the other inputs, it may have
                    // equivalents. Add those.
                    for(IOPort dependentInputPort : otherInputs) {
                        // Get the equivalence class for another port depended on.
                        Set<IOPort> equivalenceClass
                                = _equivalenceClasses.get(dependentInputPort);
                        if (equivalenceClass != null) {
                            merged.addAll(equivalenceClass);
                        }
                    }
                }
            }
        }
        
        // For every input port in the merged set, record the equivalence
        // to the merged set.
        for (IOPort mergedInput : merged) {
            _equivalenceClasses.put(mergedInput, merged);
        }
        dependsOnInputsMap.put(port, merged);

        // Next update the forward and reverse dependencies.
        // If the port belongs to the associated actor,
        // make a permanent record.
        Map<IOPort,Dependency> forward = null;
        Map<IOPort,Dependency> reverse = null;
        if (port.getContainer() == _actor) {
            forward = _forwardDependencies.get(inputPort);
            if (forward == null) {
                forward = new HashMap<IOPort,Dependency>();
                _forwardDependencies.put(inputPort, forward);
            }            
            forward.put(port, dependency);

            reverse = _reverseDependencies.get(port);
            if (reverse == null) {
                reverse = new HashMap<IOPort,Dependency>();
                _reverseDependencies.put(port, reverse);
            }
            reverse.put(inputPort, dependency);
        }
        Dependency priorDependency = map.get(port);
        if (priorDependency == null) {
            map.put(port, dependency);
            return true;
        }
        // There is a prior dependency.
        Dependency newDependency = priorDependency.oPlus(dependency);
        if (!newDependency.equals(priorDependency)) {
            // Update the dependency.
            map.put(port, newDependency);
            if (port.getContainer() == _actor) {
                // Have to also change the forward and reverse dependencies.
                reverse.put(inputPort, newDependency);
                forward.put(port, newDependency);
            }
            return true;
        }
        // No change made to the dependency.
        return false;
    }
    
    /** Set the dependency from the specified inputPort to all
     *  ports that are reachable via the portsToProcess ports.
     *  The results are stored in the specified map.
     *  @param inputPort An input port of this actor.
     *  @param map A map of dependencies from this input port to all reachable ports,
     *   built by this method. The map is required to contain all ports in portsToProcess
     *   on entry.
     *  @param portsToProcess Ports connected to the input port directly or indirectly.
     *  @param dependsOnInputsMap The map from ports in the model to input ports
     *   that they depend on, used to construct the equivalence classes.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    private void _setDependency(
            IOPort inputPort, 
            Map<IOPort,Dependency> map, 
            Collection<IOPort> portsToProcess,
            Map<IOPort,Set<IOPort>> dependsOnInputsMap)
            throws IllegalActionException {
        Set<IOPort> portsToProcessNext = new HashSet<IOPort>();
        for (IOPort port : portsToProcess) {
            // The argument map is required to contain this dependency.
            Dependency dependency = map.get(port);
            // Next, check whether we have gotten to an output port of this actor.
            if (port.getContainer() == _actor) {
                // Port is owned by this actor. If it is
                // output port, then it is dependent on this
                // input port by the given dependency. It should
                // not normally be an input port, but we tolerate
                // that here in case some domain uses it someday.
                // In that latter case, there is no dependency.
                if (port.isOutput()) {
                    // We have a path from an input to an output.
                    // Record the dependency.
                    _recordDependency(inputPort, port, map, dependency, dependsOnInputsMap);
                }
            } else {
                // The port presumably belongs to an actor inside this actor.
                _recordDependency(inputPort, port, map, dependency, dependsOnInputsMap);
                // Next record the dependency that all output ports of
                // the actor containing the port have on the input port.
                Actor actor = (Actor)port.getContainer();
                CausalityInterface causality = actor.getCausalityInterface();
                Iterator outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort)outputPorts.next();
                    Dependency actorDependency = causality.getDependency(port, outputPort);
                    Dependency newDependency = dependency.oTimes(actorDependency);
                    if (_recordDependency(inputPort, outputPort, map, newDependency, dependsOnInputsMap)) {
                        // Dependency of this output port has been set or
                        // changed.  Add ports to the set of ports to be
                        // processed next.
                        Collection sinkPorts = outputPort.sinkPortList();
                        Iterator sinkPortsIterator = sinkPorts.iterator();
                        while (sinkPortsIterator.hasNext()) {
                            IOPort sinkPort = (IOPort)sinkPortsIterator.next();
                            _recordDependency(inputPort, sinkPort, map, newDependency, dependsOnInputsMap);
                            if (sinkPort.getContainer() != _actor) {
                                // Port is not owned by this actor.
                                // Further processing will be needed.
                                portsToProcessNext.add(sinkPort);
                            }
                        }
                    }
                }
            }
        }
        if (!portsToProcessNext.isEmpty()) {
            _setDependency(inputPort, map, portsToProcessNext, dependsOnInputsMap);
        }
    }
}
