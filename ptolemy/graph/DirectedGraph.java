/* A directed graph and some graph algorithms.

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

@ProposedRating Yellow (pwhitake@eecs.berkeley.edu)
@AcceptedRating Yellow (pwhitake@eecs.berkeley.edu)

review methods attemptTopologicalSort(), sccDecomposition(), subgraph(), and
               successorSet()
*/

package ptolemy.graph;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// DirectedGraph
/**
A directed graph and some graph algorithms.
<p>
NOTE: This class is a starting point for implementing graph algorithms,
more methods will be added.

@author Yuhong Xiong, Jie Liu, Paul Whitaker
@version $Id$
*/

public class DirectedGraph extends Graph {

    /** Construct an empty directed graph.
     */
    public DirectedGraph() {
        super();
    }

    /** Construct an empty directed graph with enough storage allocated
     *  for the specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount the integer specifying the number of nodes
     */
    public DirectedGraph(int nodeCount) {
        super(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a node to this graph.  The node is represented by the
     *  specified Object. The Object cannot be <code>null</code>.
     *  In addition, two Objects equal to each other, as determined
     *  by the <code>equals</code> method, cannot both be added.<p>
     *
     *  After nodes are added to a graph, The node Objects should not
     *  be changed in such a way that the Objects representing
     *  distinct nodes are equal, as determined by the
     *  <code>equals</code> method. Doing so may generate unexpected
     *  results.
     *
     *  @param o the Object representing a graph node
     *  @exception IllegalArgumentException If an Object equals to the
     *   specified one is already in this graph.
     *  @exception NullPointerException If the specified Object is
     *   <code>null</code>.
     */
    public void add(Object o) {
        super.add(o);

        _inDegree.add(_getNodeId(o), new Integer(0));
	_transitiveClosure = null;
    }

    /** Add a directed edge to connect two nodes. The first argument
     *  is the lower node and the second the higher.  Multiple connections
     *  between two nodes are allowed, and are considered different
     *  edges. Self loop is also allowed.
     *
     *  @param o1 the Object representing the lower node
     *  @param o2 the Object representing the higher node
     *  @exception IllegalArgumentException If at least one of the arguments
     *   is not a graph node, i.e., the argument is not equal to an Object
     *   specified in a successful <code>add</code> call. Equality
     *   is determined by the <code>equals</code> method.
     */
    public void addEdge(Object o1, Object o2) {
        super.addEdge(o1, o2);

        int id2 = _getNodeId(o2);
        int indeg = ((Integer)(_inDegree.get(id2))).intValue();
        _inDegree.set(id2, new Integer(indeg+1));
	_transitiveClosure = null;
    }

    /** Sort the given graph objects in their topological order as long as
     *  no two of the given objects are mutually reachable by each other.
     *  This method use the transitive closure matrix. Since generally
     *  the graph is checked for cyclicity before this method is
     *  called, the use of the transitive closure matrix should
     *  not add any overhead. A bubble sort is used for the internal
     *  implementation, so the complexity is n^2.
     *  @return The objects in their sorted order.
     *  @exception IllegalActionException If any two nodes are strongly
     *   connected.
     */
    public Object[] attemptTopologicalSort(Object[] objs)
            throws IllegalActionException {
        _computeTransitiveClosure();

        int N = objs.length;
        int[] ids = new int[N];
        for (int i = 0; i < N; i++) {
            ids[i] = _getNodeId(objs[i]);
        }
        for (int i = 0; i < N-1; i++) {
            for (int j = i+1; j < N; j++) {
                if (_transitiveClosure[ids[j]][ids[i]]) {
                    if (_transitiveClosure[ids[i]][ids[j]]) {
                        throw new IllegalActionException("Attempted to"
                                + " topologically sort cyclic nodes.");
                    } else {
                        //swap
                        int tmp = ids[i];
                        ids[i] = ids[j];
                        ids[j] = tmp;
                    }
                }

            }
        }
        Object[] result = new Object[N];
        for (int i = 0; i < N; i++) {
            result[i] = _getNodeObject(ids[i]);
        }
        return result;
    }
    
    /** Find all the nodes that can be reached backward from the
     *  specified node.
     *  The reachable nodes do not include the argument unless
     *  there is a loop from the specified node back to itself.
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @param o an Object representing a node in this graph.
     *  @return an array of Objects representing nodes reachable from
     *   the specified one.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node in this graph.
     */
    public Object[] backwardReachableNodes(Object o) {
	_computeTransitiveClosure();

	int id = _getNodeId(o);
	ArrayList nodes = new ArrayList(_transitiveClosure.length);
        // Look at the corresponding column.
	for (int i = 0; i < _transitiveClosure.length; i++) {
	    if (_transitiveClosure[i][id]) {
		nodes.add(_getNodeObject(i));
	    }
	}

        return nodes.toArray();
    }

    /** Find all the nodes that can be reached backward from the
     *  specified nodes.
     *  The reachable nodes do not include the specific ones unless
     *  there is a loop from the specified node back to itself.
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @param o an Object representing a node in this graph.
     *  @return an array of Objects representing nodes reachable from
     *   the specified one.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node in this graph.
     */
    public Object[] backwardReachableNodes(Object[] objs) {
	_computeTransitiveClosure();

        int N = objs.length;
        int ids[] = new int[N];
        for (int i = 0; i < N; i++) {
            ids[i] = _getNodeId(objs[i]);
        }
	ArrayList nodes = new ArrayList(_transitiveClosure.length);
        // Or the corresponding rows.
	for (int i = 0; i < _transitiveClosure.length; i++) {
            boolean reachable = false;
            for (int j = 0;  j < N; j++) {
                if (_transitiveClosure[i][ids[j]]) {
		    reachable = true;
		    break;
		}
            }
	    if (reachable) {
		nodes.add(_getNodeObject(i));
	    }
	}

        return nodes.toArray();
    }

    /** Return the nodes that are in cycles. If there are multiple cycles,
     *  the nodes in all the cycles will be returned.
     *  The implementation computes the transitive closure of the
     *  graph, if it has not already been computed since the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @return An array of Objects representing nodes in cycles.
     */
    public Object[] cycleNodes() {
	_computeTransitiveClosure();

	ArrayList nodes = new ArrayList(_transitiveClosure.length);
	for (int i = 0; i < _transitiveClosure.length; i++) {
	    if (_transitiveClosure[i][i]) {
		nodes.add(_getNodeObject(i));
	    }
	}

        return nodes.toArray();
    }

    /** Test if an edge exists from the first node to the second node.
     *  @return true if the graph includes an edge from the first node to
     *   the second node.
     */
    public boolean edgeExists(Object node1, Object node2) {

        Object[] successors = successorSet(node1);
        for (int i = 0; i < successors.length; i++) {
            if (successors[i] == node2)
                return true;
        }

        return false;
    }

    /** Test if this graph is acyclic (is a DAG).
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last change to
     *  this graph.  So the first call to this method after graph change
     *  may be slow, but all the subsequent calls returns in constant
     *  time.
     *  @return <code>true</code> if the the graph is acyclic, or
     *   empty; <code>false</code> otherwise.
     */
    public boolean isAcyclic() {
        _computeTransitiveClosure();

        return _isAcyclic;
    }

    /** Find all the nodes that can be reached from the specified node.
     *  The reachable nodes do not include the specific one unless
     *  there is a loop from the specified node back to itself.
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @param o an Object representing a node in this graph.
     *  @return an array of Objects representing nodes reachable from
     *   the specified one.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node in this graph.
     */
    public Object[] reachableNodes(Object o) {
	_computeTransitiveClosure();

	int id = _getNodeId(o);
	ArrayList nodes = new ArrayList(_transitiveClosure.length);
	for (int i = 0; i < _transitiveClosure.length; i++) {
	    if (_transitiveClosure[id][i]) {
		nodes.add(_getNodeObject(i));
	    }
	}

        return nodes.toArray();
    }

    /** Find all the nodes that can be reached from the specified nodes.
     *  The reachable nodes do not include the specific ones unless
     *  there is a loop from the specified node back to itself.
     *  The implementation computes the transitive closure of the
     *  graph, if it is not already computed after the last graph
     *  change.  So the first call to this method after graph
     *  change may be slow, but all the subsequent calls return
     *  in constant time.
     *  @param o an Object representing a node in this graph.
     *  @return an array of Objects representing nodes reachable from
     *   the specified one.
     *  @exception IllegalArgumentException If the specified Object is
     *   not a node in this graph.
     */
    public Object[] reachableNodes(Object[] objs) {
	_computeTransitiveClosure();

	int N = objs.length;
        int ids[] = new int[N];
        for (int i = 0; i < N; i++) {
            ids[i] = _getNodeId(objs[i]);
        }
	ArrayList nodes = new ArrayList(_transitiveClosure.length);
        // Or the corresponding rows.
	for (int i = 0; i < _transitiveClosure.length; i++) {
            boolean reachable = false;
            for (int j = 0; j < N; j++) {
		if (_transitiveClosure[ids[j]][i]) {
		    reachable = true;
		    break;
		}
            }
	    if (reachable) {
		nodes.add(_getNodeObject(i));
	    }
	}
        return nodes.toArray();
    }

    /** Compute the SCC decomposition of a graph.
     *  @return An array of instances of DirectedGraph which represent
     *   the SCCs of the graph in topological order.
     */
    public DirectedGraph[] sccDecomposition() {
	_computeTransitiveClosure();

        int N = getNodeCount();
        if (_transitiveClosure.length != N)
            throw new InternalErrorException("Graph inconsistency");

        // initially, no nodes have been added to an SCC
        boolean addedToAnSCC[] = new boolean[N];
        for (int i = 0; i < N; i++) {
            addedToAnSCC[i] = false;
        }

        ArrayList sccNodeLists = new ArrayList();
        ArrayList sccRepresentatives = new ArrayList();

        for (int i = 0; i < N; i++) {
            // given a node, if that node is not part of an SCC, assign
            // it to a new SCC
            if (!addedToAnSCC[i]) {
                ArrayList nodeList = new ArrayList();
                sccNodeLists.add(nodeList);
                Object node = _getNodeObject(i);
                nodeList.add(node);
                sccRepresentatives.add(node);
                addedToAnSCC[i] = true;
                for (int j = i + 1; j < N; j++) {
                    // given two nodes, the two are in the same SCC if they
                    // are mutually reachable
                    if (!addedToAnSCC[j]) {
                        if (_transitiveClosure[i][j] &&
                                _transitiveClosure[j][i]) {
                            nodeList.add(_getNodeObject(j));
                            addedToAnSCC[j] = true;
                        }
                    }
                }
            }
        }

        int numberOfSCCs = sccNodeLists.size();
        Object sortedSCCRepresentatives[];
        
        try {
            sortedSCCRepresentatives =
                attemptTopologicalSort(sccRepresentatives.toArray());
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Objects in different SCCs were"
                    + " found to be strongly connected.");
        }
 
        ArrayList sortedSCCNodeLists = new ArrayList();

        for (int i = 0; i < numberOfSCCs; i++) {
            Object sccRepresentative = sortedSCCRepresentatives[i];
            for (int j = 0; j < numberOfSCCs; j++) {
                ArrayList nodeList = (ArrayList) (sccNodeLists.get(j));
                if (nodeList.get(0) == sccRepresentative)
                    sortedSCCNodeLists.add(nodeList);
            }
        }

        DirectedGraph sccs[] = new DirectedGraph[numberOfSCCs];
        for (int i = 0; i < numberOfSCCs; i++) {
            ArrayList nodeList = (ArrayList) (sortedSCCNodeLists.get(i));
            sccs[i] = subgraph(nodeList.toArray());
        }

        return sccs;
    }

    /** Compute the subgraph of the graph containing only the specified nodes.
     *  @return An array of instances of DirectedGraph which represent
     *   the subgraph.
     */
    public DirectedGraph subgraph(Object[] objs) {

	int N = objs.length;
        int ids[] = new int[N];
        DirectedGraph subgraph = new DirectedGraph(N);
        for (int i = 0; i < N; i++) {
            ids[i] = _getNodeId(objs[i]);
            subgraph.add(objs[i]);
        }

        // now that all the nodes have been added, we check to see if any
        // object in the subgraph has an edge that points to another object
        // in the subgraph, and if so, we add it

        // for all objects in the subgraph
        for (int i = 0; i < N; i++) {
            ArrayList edge = (ArrayList)(_graph.get(ids[i]));
            // for all edges from the object
            for (int j = 0; j < edge.size(); j++) {
                int k = ((Integer)edge.get(j)).intValue();
                // check all objects in the subgraph
                for (int m = 0; m < N; m++) {
                    if (k == ids[m]) subgraph.addEdge(objs[i], objs[m]);
                }
            }
        }

        return subgraph;
    }

    /** Compute the successor set of a given node.
     *  @return An array of the nodes of the successor set.
     */
    public Object[] successorSet(Object obj) {

        Object objs[] = new Object[1];
        objs[0] = obj;
        return successorSet(objs);
    }

    /** Compute the successor set of a given set of nodes.
     *  @return An array of the nodes of the successor set.
     */
    public Object[] successorSet(Object[] objs) {

	int N = objs.length;
        HashSet successors = new HashSet();

        // for all objects in the set
        for (int i = 0; i < N; i++) {
            ArrayList edge = (ArrayList)(_graph.get(_getNodeId(objs[i])));
            // for all edges from the object
            for (int j = 0; j < edge.size(); j++) {
                int k = ((Integer)edge.get(j)).intValue();
                successors.add(_getNodeObject(k));
            }
        }

        return successors.toArray();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compute the transitive closure. Puts the result in the
     *  boolean array _transitiveClosure. If this graph is empty,
     *  set the dimension of _transitiveClosure to be 0 by 0.
     *  The implementation uses Warshall's algorithm, which can be
     *  found in chapter 6 of "Discrete Mathematics and Its
     *  Applications", 3rd Ed., by Kenneth H. Rosen.  The complexity
     *  of this algorithm is O(|N|^3), where N for nodes.
     *  This method also checks if the graph is cyclic and stores
     *  the result in an internal flag.
     */
    protected void _computeTransitiveClosure() {
        if (_transitiveClosure != null) {
            return;
        }

        int size = getNodeCount();

	// Initialize _transitiveClosure to the adjacency matrix
        _transitiveClosure = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                _transitiveClosure[i][j] = false;
            }

            ArrayList edge = (ArrayList)(_graph.get(i));
            for (int j = 0; j < edge.size(); j++) {
                int k = ((Integer)edge.get(j)).intValue();
                _transitiveClosure[i][k] = true;
            }
        }

        // Warshall's algorithm
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    _transitiveClosure[i][j] |= _transitiveClosure[i][k] &
                        _transitiveClosure[k][j];
                }
            }
        }

        // check for cycles.
        _isAcyclic = true;
        for (int i = 0; i < size; i++) {
            if (_transitiveClosure[i][i]) {
                _isAcyclic = false;
		break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The in-degree of each node.
     *  This ArrayList is indexed by node ID with each entry an
     *  <code>Integer</code> containing the in-degree of the
     *  corresponding node.
     */
    protected ArrayList _inDegree = new ArrayList();

    /** The adjacency matrix representation of the transitive closure.
     *  The entry (i, j) is <code>true</code> if and only if there
     *  exists a path from the node with ID i to the node with ID j.
     *  This array is computed by <code>_computeTransitiveClosure</code>.
     *  After each graph change, that method should be called before
     *  this array is used. Otherwise, this array is not valid.
     */
    protected boolean[][] _transitiveClosure = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isAcyclic;
}








