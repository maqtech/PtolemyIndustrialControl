/* An instance of FunctionDependencyOfCompositeActor describes the
   function dependency information of a composite actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.actor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependenceOfCompositeActor
/** An instance of FunctionDependencyOfCompositeActor describes the function
    dependency information of a composite actor. 
    <p>
    The construction of the dependency graph is in a bottom-up way by 
    composing the function dependencies of the contained actors, which may 
    be either atomic or composite. 
    <p>
    To check if the dependecy graph has cycles, use the getCycleNodes() method.
    The method returns an array of IOPorts in cycles. If there is no cycle, the
    returned array is empty.
    
    @see FunctionDependency
    @author Haiyang Zheng
    @version $Id $
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (hyzheng)
    @Pt.AcceptedRating Red (hyzheng)
*/
public class FunctionDependencyOfCompositeActor extends FunctionDependency {

    /** Construct a FunctionDependency in the given actor.
     *  @param actor The actor.
     */
    public FunctionDependencyOfCompositeActor(Actor actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a cycle loop in the dependency graph of this 
     *  FunctionDependency object, return the nodes in the 
     *  cycle loop. If there are multiple cycles, all the nodes will 
     *  be returned. If there is no cycle, an empty array is returned. 
     *  The type of the returned nodes is IOPort.
     *  @return An array contains the IOPorts in cycles.
     */
    public Object[] getCycleNodes() {
        _validate();
        return _detailedDependencyGraph.cycleNodes();
    }

    /** Return a detailed dependency graph representing the function 
     *  dependency information. The graph includes both the external
     *  and internal ports. This dependency graph is used
     *  by a director to construct a schedule.
     *  @return A detailed dependency graph reflecting the dependency
     *  information between the input and output ports. 
     */
    public DirectedGraph getDetailedDependencyGraph() {
        _validate();
        return _detailedDependencyGraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Construct a dependency graph from a detailed dependency graph by 
     *  excluding the internal ports.
     */
    protected void _constructDependencyGraph() {

        // First, construct the detailed dependency graph
        _constructDetailedDependencyGraph();

        // get associated actor
        Actor actor = getActor();
        
        // Initialize the dependency graph
        _dependencyGraph = 
            _initializeDisconnectedDependencyGraph();

        // add an edge from input to output 
        // if the output depends on the input
        Iterator inputs = actor.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Collection reachableOutputs =
                _detailedDependencyGraph.reachableNodes(
                    _detailedDependencyGraph.node(inputPort));
            Iterator outputs = actor.outputPortList().listIterator();
            while (outputs.hasNext()) {
                IOPort outputPort = (IOPort)outputs.next();
                if (reachableOutputs.
                    contains(_detailedDependencyGraph.node(outputPort))) {
                        _dependencyGraph.addEdge(inputPort, outputPort);
                }
            }
        }
    }


    /** Get a list of embedded entities for function dependency
     *  calculation.
     *  @return A list of embedded entities.
     */
    protected List _getEntities() {
        return ((CompositeActor)getActor()).deepEntityList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////
    
    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies. This
     *  graph includes both the externally and internally visible input 
     *  and output ports.
     */
    // The following code has recursive calls.
    // FIXME: Steve suggests the performance analysis.
    private void _constructDetailedDependencyGraph()  {
        // get the actor
        CompositeActor actor = (CompositeActor)getActor();

        // initialize the detailed dependency graph
        _detailedDependencyGraph = 
            _initializeDisconnectedDependencyGraph();
        
        // FIXME: for special domains, like Giotto, the inputs
        // and outputs are always independent if they are not 
        // directly connected. How to implement this? 
        // Domain-specific function dependency?

        // Here we add constraints on which actors to be used to
        // construct graph. For example, in a composite actor, all
        // the contained atomic and composite actors are included
        // to construct the function dependency. While in a modal 
        // model, only the refinement(s) of the current state is 
        // considered.
        
        // FIXME: the situation that a state has multiple refinements 
        // has to be considered.  
        List embeddedActors = _getEntities();

        // merge dependency graphs of the internal actors into 
        // the dependency graph of the actor 
        Iterator embeddedActorsIterator = embeddedActors.iterator();
        while (embeddedActorsIterator.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActorsIterator.next();
            FunctionDependency functionDependency =
                embeddedActor.getFunctionDependencies();
            if (functionDependency != null) {
                _detailedDependencyGraph.addGraph(
                    functionDependency.getDependencyGraph());
            } else {
                throw new InternalErrorException("FunctionDependency can "
                        + "not be null. Check all four types of function "
                        + "dependencies. There must be something wrong.");
            }
        }

        // Next, create the directed edges according to the connections at
        // the container level, communication dependencies between internal
        // actors
        List outputPorts = actor.outputPortList();
        embeddedActorsIterator = embeddedActors.iterator();
        // iterate all embedded actors (including opaque composite actors
        // and flattening transparent composite actors)
        while (embeddedActorsIterator.hasNext()) {
            Actor embeddedActor = (Actor)embeddedActorsIterator.next();
            // Find the successor of the output ports of current actor.
            Iterator successors =
                embeddedActor.outputPortList().iterator();
            while (successors.hasNext()) {
                IOPort outPort = (IOPort) successors.next();
                // Find the inside ports connected to outPort.
                // NOTE: sinkPortList() is an expensive operation,
                // and it may return ports that are not physically
                // connected (as in wireless ports).  Hence, we
                // use getRemoteReceivers() here. EAL
                Receiver[][] receivers = outPort.getRemoteReceivers();
                for (int i = 0; i < receivers.length; i++) {
                    // FIXME: For ParameterPort, it is possible that
                    // the downstream receivers are null. It is a
                    // unresolved issue about the semantics of Parameter
                    // Port considering the lazy evaluation of variables.
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            IOPort ioPort =
                                receivers[i][j].getContainer();
                            if (embeddedActors.contains(ioPort.getContainer())
                                    || outputPorts.contains(ioPort)) {
                                    _detailedDependencyGraph.addEdge(
                                        outPort, ioPort);
                            }
                        }
                    }
                }
            }
        }

        // Last, connect the container inputs to the inside
        // ports receiving tokens from these inputs.
        Iterator inputs = actor.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            // Find the inside ports connected to this input port.
            // NOTE: insideSinkPortList() is an expensive operation,
            // and it may return ports that are not physically
            // connected (as in wireless ports).  Hence, we
            // use deepGetReceivers() here. EAL
            Receiver[][] receivers = inputPort.deepGetReceivers();
            for (int i = 0; i < receivers.length; i++) {
                for (int j = 0; j < receivers[i].length; j++) {
                    IOPort ioPort =
                        receivers[i][j].getContainer();
                    Actor ioPortContainer = (Actor)ioPort.getContainer();
                    // The receivers may belong to either the inputs of
                    // contained actors, or the outputs of the container.
                    if (embeddedActors.contains(ioPortContainer) ||
                        actor.equals(ioPortContainer)) {
                            _detailedDependencyGraph.addEdge(inputPort,
                                ioPort);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    
    // The detailed dependency graph that includes both the externally
    // and internally visible input and output ports. 
    private DirectedGraph _detailedDependencyGraph;

}
