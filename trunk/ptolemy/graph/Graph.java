/* A graph with optionally-weighted nodes and edges.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Graph
/**
A graph with optionally-weighted edges and nodes.

<p>Each node or edge may have a weight associated with it
(see {@link Edge} and {@link Node}).
The nodes (edges) in a graph are always distinct, but their weights
need not be.

<p>Each node (edge) has a unique, integer label associated with it.
These labels can be used, for example, to index arrays and matrixes
whose rows/columns correspond to nodes (edges). See {@link #nodeLabel(Node)}
({@link #edgeLabel(Edge)}) for details.

<p>Both directed and undirected graphs can be implemented using this
class. In directed graphs, the order of nodes specified to the
<code>addEdge</code> method is relevant, whereas in undirected graphs, the
order is unimportant. Support for both undirected and directed graphs
follows from the combined support for these in the underlying {@link
Node} and {@link Edge} classes. For more thorough support for directed
graphs, see {@link DirectedGraph}.

<p>The same node can exist in multiple graphs, but any given graph can contain
only one instance of the node. Node labels, however, are local to individual
graphs. Thus, the same node may have different labels in different graphs.
Furthermore, the label assigned in a given graph to a node may change over time
(if the set of nodes in the graph changes). If a node is contained in
multiple graphs, it has the same weight in all of the graphs.
All of this holds for edges
all well. The same weight may be shared among multiple nodes and edges.

<p> Multiple edges in a graph can connect the same pair of nodes.
Thus, multigraphs are supported.

<p>Once assigned, node and edge weights should not be changed in ways that
affect comparison under the <code>equals</code> method.
Otherwise, unpredictable behavior may result.

@author Shuvra S. Bhattacharyya, Yuhong Xiong, Jie Liu, Ming-Yung Ko,
Shahrooz Shahparnia
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.graph.Edge
@see ptolemy.graph.Node
*/
public class Graph implements Cloneable {

    /** Construct an empty graph.
     */
    public Graph() {
        _nodes = new LabeledList();
        _edges = new LabeledList();
        _initializeListeners();
        _nodeWeightMap = new HashMap();
        _edgeWeightMap = new HashMap();
        _incidentEdgeMap = new HashMap();
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of nodes.  Memory management is more
     *  efficient with this constructor if the number of nodes is
     *  known.
     *  @param nodeCount The number of nodes.
     */
    public Graph(int nodeCount) {
        _nodes = new LabeledList(nodeCount);
        _edges = new LabeledList();
        _initializeListeners();
        _nodeWeightMap = new HashMap(nodeCount);
        _edgeWeightMap = new HashMap();
        _incidentEdgeMap = new HashMap(nodeCount);
    }

    /** Construct an empty graph with enough storage allocated for the
     *  specified number of edges, and number of nodes.  Memory
     *  management is more efficient with this constructor if the
     *  number of nodes and edges is known.
     *  @param nodeCount The number of nodes.
     *  @param edgeCount The number of edges.
     */
    public Graph(int nodeCount, int edgeCount) {
        _nodes = new LabeledList(nodeCount);
        _edges = new LabeledList(edgeCount);
        _initializeListeners();
        _nodeWeightMap = new HashMap(nodeCount);
        _edgeWeightMap = new HashMap(edgeCount);
        _incidentEdgeMap = new HashMap(nodeCount);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a weighted edge between two nodes.  If the edge is subsequently
     *  operated on as a directed edge, its orientation will be taken
     *  to be directed <i>from</i> the first (<code>node1</code>) node
     *  <i>to</i> the second (<code>node2</code>) node. Multiple edges
     *  between the same nodes are allowed, and are considered
     *  different edges.  Self-loops are also allowed.
     *
     *  @param node1 The first node.
     *  @param node2 The second node.
     *  @param weight The weight.
     *  @return The edge.
     *  @exception IllegalArgumentException If the first node or second
     *  node is not already in the graph, or if the weight is
     *  <code>null</code>.
     */
    public Edge addEdge(Node node1, Node node2, Object weight) {
        return _addEdge(node1, node2, true, weight);
    }

    /** Add an unweighted edge between two nodes. Operation is the same as in
     *  {@link #addEdge(Node, Node, Object)}, except that no
     *  weight is assigned to the edge.
     *
     *  @param node1 The first node.
     *  @param node2 The second node.
     *  @return The edge.
     *  @exception IllegalArgumentException If the first node or second
     *  node is not already in the graph.
     */
    public Edge addEdge(Node node1, Node node2) {
        return _addEdge(node1, node2, false, null);
    }

    /** Given two node weights <i>w1</i> and <i>w2</i>, add weighted
     *  edges of the form (<i>x1</i>, <i>x2</i>), where
     *  <code>(x1.weight() == w1) && (x2.weight() == w2)</code>.
     *
     *  @param weight1 The first node weight.
     *  @param weight2 The second node weight.
     *  @param newEdgeWeight The weight to assign to each new edge.
     *  @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     *  @exception IllegalArgumentException If no edge is
     *  added (i.e., if no nodes x1, x2 satisfy the above condition).
     */
    public Collection addEdge(Object weight1, Object weight2,
            Object newEdgeWeight) {
        return _addEdges(weight1, weight2, true, newEdgeWeight);
    }

    /** Given two node weights <i>w1</i> and <i>w2</i>, add all unweighted
     *  edges of the form (<i>x1</i>, <i>x2</i>), where
     *  <code>(x1.weight() == w1) && (x2.weight() == w2)</code>.
     *
     *  @param weight1 The first node weight.
     *  @param weight2 The second node weight.
     *  @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     *  @exception IllegalArgumentException If no edge is
     *  added (i.e., if no nodes x1, x2 satisfy the above condition).
     */
    public Collection addEdge(Object weight1, Object weight2) {
        return _addEdges(weight1, weight2, false, null);
    }

    /** Add a pre-constructed edge (unweighted or weighted).
     *
     *  @param edge The edge.
     *  @exception IllegalArgumentException If the source or sink node
     *  of the edge is not already in the graph, or if the edge is
     *  already in the graph.
     */
    public Edge addEdge(Edge edge) {
        if (!containsNode(edge.source())) {
            throw new IllegalArgumentException("The source node "
                    + "is not in the graph." + _edgeDump(edge));
        }
        else if (!containsNode(edge.sink())) {
            throw new IllegalArgumentException("The sink node "
                    + "is not in the graph." + _edgeDump(edge));
        } else if (containsEdge(edge)) {
            throw new IllegalArgumentException("Attempt to add an edge that "
                    + "is already in the graph." + _edgeDump(edge));
        } else {
            _registerEdge(edge);
            return edge;
        }
    }

    /** Add a listener to the set of listeners that this graph broadcasts to.
     *  @param listener The listener.
     *  @exception IllegalArgumentException If the graph associated with the
     *  listener is not equal to this graph, or if the graph already contains
     *  the listener in its list of listeners.
     */
    public void addListener(GraphListener listener) {
        if (listener.graph() != this) {
            throw new IllegalArgumentException("Invalid associated graph.\n" +
                    "The listener:\n" + listener + "\n");
        } else {
            Iterator listeners = _listenerList.iterator();
            while (listeners.hasNext()) {
                if (listeners.next() == listener) {
                    throw new IllegalArgumentException("Attempt to add " +
                            "duplicate listener.\nThe listener:\n" + listener);
                }
            }
        }
        _listenerList.add(listener);
    }

    /** Add an unweighted node to this graph.
     *  @return The node.
     */
    public Node addNode() {
        Node node = new Node();
        _registerNode(node);
        return node;
    }

    /** Add a pre-constructed node (unweighted or weighted).
     *
     *  @param node The node.
     *  @exception IllegalArgumentException If the node is already in the graph.
     */
    public Node addNode(Node node) {
        if (containsNode(node)) {
            throw new IllegalArgumentException("Attempt to add a node "
                    + "that is already contained in the graph."
                    + _nodeDump(node));
        }
        else {
            _registerNode(node);
            return node;
        }
    }

    /** Add a weighted node to this graph given the node weight.
     *
     *  @param weight The node weight.
     *  @return The node.
     *  @exception IllegalArgumentException If the specified weight is null.
     */
    public Node addNodeWeight(Object weight) {
        Node node = new Node(weight);
        _registerNode(node);
        return node;
    }

    /** Add a collection of nodes to the graph.
     *  Each element of the collection is interpreted
     *  as a weight of a new node to add in the graph.
     *  @param weightCollection The collection of node weights; each element
     *  is an instance of {@link Object}.
     *  @return The set of nodes that that were added; each element
     *  is an instance of {@link Node}.
     */
    public Collection addNodeWeights(Collection weightCollection) {
        Iterator weights = weightCollection.iterator();
        ArrayList nodes = new ArrayList();
        while (weights.hasNext()) {
            nodes.add(addNodeWeight(weights.next()));
        }
        return nodes;
    }

    /** Return the present value of a counter that keeps track
     *  of changes to the graph.
     *  This counter is monitored by GraphListeners to determine
     *  if associated computations are obsolete. Upon overflow, the counter
     *  resets to zero, broadcasts a change to all graph listeners, and
     *  begins counting again.
     *  @return The present value of the counter.
     */
    public long changeCount() {
        return _changeCount;
    }

    /** Return a clone of this graph. The clone has the same set of
     *  nodes and edges. Changes to the node or edge weights
     *  affect the clone simultaneously. However,
     *  modifications to the graph topology make the clone different from
     *  this graph (e.g., they are no longer equal (see
     *  {@link #equals(Object)})).
     *
     *  @return The clone graph.
     */
    public Object clone() {
        Graph cloneGraph = new Graph();
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            cloneGraph.addNode((Node)nodes.next());
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            cloneGraph.addEdge((Edge)edges.next());
        }
        return cloneGraph;
    }

    /** Return the connected components of the graph. The connected
     *  components are returned as a Collection, where each element
     *  of the Collection is a Collection of Nodes.
     *  @return The connected components.
     */
    public Collection connectedComponents() {
        // We divide the set of nodes into disjoint subsets called 'components'.
        // These components are repeatedly modified until they coincide with
        // the connected components. The following HashMap is a map from
        // nodes into the components that contain them. Each element in the map
        // is a Node whose weight is an ArrayList of Nodes. We encapsulate
        // each ArrayList as the weight of a Node (called the 'container' of
        // the ArrayList) so that we can modify the ArrayList without
        // interfering with the hashing semantics of the HashMap.
        HashMap componentMap = new HashMap(nodeCount());
        HashSet components = new HashSet(nodeCount());
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            ArrayList component = new ArrayList();
            component.add(node);
            Node container = new Node(component);
            componentMap.put(node, container);
            components.add(container);
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            Node sourceContainer = (Node)(componentMap.get(edge.source()));
            Node sinkContainer = (Node)(componentMap.get(edge.sink()));
            ArrayList sourceSet = (ArrayList)(sourceContainer.weight());
            ArrayList sinkSet = (ArrayList)(sinkContainer.weight());
            if (sourceSet != sinkSet) {
                // Construct the union of the two components in the source set.
                components.remove(sinkContainer);
                Iterator moveNodes = sinkSet.iterator();
                while (moveNodes.hasNext()) {
                    Node moveNode = (Node)moveNodes.next();
                    componentMap.put(moveNode, sourceContainer);
                    sourceSet.add(moveNode);
                }
            }
        }

        // Before returning the result, do away with the container that
        // encapsulates each connected component.
        ArrayList result = new ArrayList(components.size());
        Iterator connectedComponents = components.iterator();
        while (connectedComponents.hasNext()) {
            result.add(((Node)connectedComponents.next()).weight());
        }
        return result;
    }

    /** Return true if the specified edge exists in the
     *  graph; otherwise, return false.
     *  @param edge The specified edge.
     *  @return True if the specified edge exists in the graph.
     */
    public boolean containsEdge(Edge edge) {
        return _edges.contains(edge);
    }

    /** Test if the specified object is an edge weight in this
     *  graph. Equality is
     *  determined by the <code>equals</code> method. If the specified
     *  edge weight is null, return false.
     *
     *  @param weight The edge weight to be tested.
     *  @return True if the specified object is an edge weight in this graph.
     */
    public boolean containsEdgeWeight(Object weight) {
        try {
            _sameWeightEdges(weight);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    /** Return True if the specified node exists in the
     *  graph; otherwise, return false.
     *  @param node The specified node.
     *  @return True if the specified node exists in the
     *  graph.
     */
    public boolean containsNode(Node node) {
        return _nodes.contains(node);
    }

    /** Test if the specified object is a node weight in this
     *  graph. Equality is
     *  determined by the <code>equals</code> method. If the specified
     *  weight is null, return false.
     *
     *  @param weight The node weight to be tested.
     *  @return True if the specified object is a node weight in this graph.
     */
    public boolean containsNodeWeight(Object weight) {
        try {
            _sameWeightNodes(weight);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    /** Return a description of this graph.
     *  The form of the description is:<p>
     *  <pre>
     *  {class_name
     *    {node0 sinks(node0)}
     *    {node1 sinks(node1)}
     *    ...
     *    {nodeN sinks(nodeN)}
     *  }
     *  </pre>
     *  where N is the number of nodes in the graph, nodeI denotes the node
     *  whose label is I, each node is described by its <code>toString()</code>
     *  method,
     *  and sinks(nodeI) denotes the set of nodes nodeK such that (nodeI, nodeK)
     *  is an edge in the graph (with each node again represented by its
     *  <code>toString()</code> method).
     *  @return A description of this graph.
     *  @deprecated Use toString().
     */
    public String description() {
        StringBuffer result = new StringBuffer("{"
                + this.getClass().getName() + "\n");
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            Node node = (Node)(nodes.next());
            result.append("  {" + node.toString());
            Iterator incidentEdges = incidentEdges(node).iterator();
            while (incidentEdges.hasNext()) {
                Edge edge = (Edge)(incidentEdges.next());
                if (edge.source() == node) {
                    result.append(" " + edge.sink().toString());
                }
            }
            result.append("}\n");
        }
        result.append("}");
        return result.toString();
    }

    /** Return an edge in this graph that has a specified weight. If multiple
     *  edges have the specified weight, then return one of them
     *  arbitrarily.
     *  @param weight The specified edge weight.
     *  @return An edge that has this weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not an edge weight in this graph.
     */
    public Edge edge(Object weight) {
        return (Edge)(((ArrayList)_sameWeightEdges(weight)).get(0));
    }

    /** Return an edge in this graph given the edge label.
     *  @param edge The edge label.
     *  @return The edge.
     *  @exception IllegalArgumentException If the label is not associated
     *  with an edge in this graph.
     *  @see #edgeLabel(Edge).
     */
    public Edge edge(int label) {
        return (Edge)(_edges.get(label));
    }

    /** Return the total number of edges in this graph.  Multiple
     *  connections between two nodes are counted multiple times.
     *  @return The total number of edges in this graph.
     */
    public int edgeCount() {
        return _edges.size();
    }

    /** Return the edge label of the specified edge.
     *  The edge label is a unique integer from 0 through
     *  <i>E</i>-1, where <i>E</i> is the number of edges
     *  currently in the graph. Edge labels maintain their
     *  consistency (remain constant) during periods when
     *  no edges are removed from the graph. When edges are removed,
     *  the labels assigned to the remaining edges may change.
     *
     *  @param edge A graph edge.
     *  @return The edge label.
     *  @exception IllegalArgumentException If the specified edge is not
     *  not an edge in this graph.
     */
    public int edgeLabel(Edge edge) throws IllegalArgumentException {
        return _edges.label(edge);
    }

    /** Return the edge label of the specified edge given the edge weight.
     *  If multiple edges have the specified weight, then return one of their
     *  labels arbitrarily.
     *
     *  @param weight The edge weight.
     *  @return The edge label.
     *  @exception IllegalArgumentException If the specified weight is not
     *  an edge weight in this graph.
     *  @see #edgeLabel(Edge).
     */
    public int edgeLabel(Object weight) throws IllegalArgumentException {
        return _edges.label(edge(weight));
    }

    /** Return the weight of a given edge in the graph given the edge label.
     *
     *  @param edge The edge label.
     *  @return The weight of the edge.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception IllegalArgumentException If the edge corresponding
     *  to the label is unweighted.
     *  @see #edgeLabel(Edge).
     */
    public Object edgeWeight(int label) {
        return ((Edge)(_edges.get(label))).weight();
    }

    /** Return all the edges in this graph in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Edge}.
     *  @return All the edges in this graph.
     */
    public Collection edges() {
        return Collections.unmodifiableList(_edges);
    }

    /** Return all the edges in this graph that have a specified weight.
     *  The edges are returned in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Edge}.
     *  @param weight The specified weight.
     *  @return The edges in this graph that have the specified weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Collection edges(Object weight) {
        return Collections.unmodifiableList(_sameWeightEdges(weight));
    }

    /** Return all the edges in this graph whose weights are contained
     *  in a specified collection.
     *  The edges are returned in the form of a collection.
     *  Duplicate weights in the specified collection result
     *  in duplicate edges in the returned collection.
     *  Each element in the returned collection is an instance of
     *  {@link Edge}.
     *  @param collection The specified collection of weights.
     *  @return The edges in this graph whose weights are contained
     *  in the specified collection.
     */
    public Collection edges(Collection collection) {
        ArrayList edges = new ArrayList();
        Iterator weights = collection.iterator();
        while (weights.hasNext()) {
            edges.addAll(edges(weights.next()));
        }
        return edges;
    }

    /** Test if a graph is equal to this one. It is equal 
     *  if it is of the same class, and has the same sets of nodes 
     *  and edges.
     *
     *  @param graph The graph with which to compare this graph.
     *  @return True if the graph is equal to this one.
     */
    public boolean equals(Object graph) {
        boolean result = true;
        if (graph == null) {
            result = false;
        }
        else if (graph.getClass() != getClass()) {
            return false;
        } else {
            Graph argumentGraph = (Graph)graph; 
            Iterator argumentNodes = argumentGraph.nodes().iterator();
            while (argumentNodes.hasNext()) {
                if (!containsNode((Node)argumentNodes.next()))
                    return false;
            }
            Iterator nodes = nodes().iterator();
            while (nodes.hasNext()) {
                if (!argumentGraph.containsNode((Node)nodes.next()) )
                    return false;
            }
            Iterator argumentEdges = argumentGraph.edges().iterator();
            while (argumentEdges.hasNext()) {
                if (!containsEdge((Edge)argumentEdges.next()))
                    return false;
            }
            Iterator edges = edges().iterator();
            while (edges.hasNext()) {
                if (!argumentGraph.containsEdge((Edge)edges.next()) )
                    return false;
            }
        }
        return result;
    }

    /** Returns the hash code for this graph. The hash code value is
     *  the sum of the node and edge hash code values.
     *
     *  @return The hash code for this graph.
     */
    public int hashCode() {
        int code = 0;
        Iterator nodes = nodes().iterator();
        while (nodes.hasNext()) {
            code += nodes.next().hashCode();
        }
        Iterator edges = edges().iterator();
        while (edges.hasNext()) {
            code += edges.next().hashCode();
        }
        return code;
    }

    /** Return the number of edges that are incident to a specified node.
     *  @param node The node.
     *  @return The number of incident edges.
     */
    public int incidentEdgeCount(Node node) {
        return _incidentEdgeList(node).size();
    }

    /** Return the set of incident edges for a specified node. Each element in
     *  the returned set is an {@link Edge}.
     *
     *  @param node The specified node.
     *  @return The set of incident edges.
     */
    public Collection incidentEdges(Node node) {
        return Collections.unmodifiableList(_incidentEdgeList(node));
    }

    /** Return the collection of edges that make a node n2 a neighbor of a
     *  node n1. In other words, return the set of edges that are incident to
     *  both n1 and n2. Each element of the returned collection is an instance
     *  of {@link Edge}.
     *  @param n1 The node n1.
     *  @param n2 The node n2.
     *  @return The collection of edges that make n2 a neighbor of n1.
     *  @see DirectedGraph#predecessorEdges(Node, Node)
     *  @see DirectedGraph#successorEdges(Node, Node)
     */
    public Collection neighborEdges(Node n1, Node n2) {
        Collection edgeCollection = this.incidentEdges(n1);
        Iterator edges = edgeCollection.iterator();
        ArrayList commonEdges = new ArrayList();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            if (edge.source() == n2) {
                commonEdges.add(edge);
            } else if (edge.sink() == n2) {
                commonEdges.add(edge);
            }
        }
        return commonEdges;
    }

    /** Return all of the neighbors of a given node in the form of a
     *  a collection. Each element of the collection is a Node.
     *  A neighbor of a node X is a node that is the sink
     *  of an edge whose source is X, or the source of a node whose sink
     *  is node X. In other words, a neighbor of X is a node that is adjacent
     *  to X. All elements in the returned collection are unique nodes.
     *  @param node The node whose neighbors are to be returned.
     *  @return The neighbors of the node.
     */
    public Collection neighbors(Node node) {
        Collection incidentEdgeCollection = incidentEdges(node);
        Iterator incidentEdges = incidentEdgeCollection.iterator();
        ArrayList result = new ArrayList(incidentEdgeCollection.size());
        while (incidentEdges.hasNext()) {
            Edge edge = (Edge)(incidentEdges.next());
            Node sink = edge.sink();
            Node source = edge.source();
            if (source == node) {
                if (!result.contains(sink)) {
                    result.add(sink);
                }
            } else if (sink == node) {
                if (!result.contains(source)) {
                    result.add(source);
                }
            }
        }
        return result;
    }

    /** Return a node in this graph that has a specified weight. If multiple
     *  nodes have the specified weight, then return one of them
     *  arbitrarily.
     *  @param weight The specified node weight.
     *  @return A node that has this weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Node node(Object weight) {
        return (Node)(((ArrayList)_sameWeightNodes(weight)).get(0));
    }

    /** Return a node in this graph given the node label.
     *  @param node The node label.
     *  @return The node.
     *  @exception IllegalArgumentException If the label is not associated with
     *  a node in this graph.
     *  @see #nodeLabel(Node).
     */
    public Node node(int label) {
        return (Node)(_nodes.get(label));
    }

    /** Return the total number of nodes in this graph.
     *  @return The total number of nodes in this graph.
     */
    public int nodeCount() {
        return _nodes.size();
    }

    /** Return the node label of the specified node.
     *  The node label is a unique integer from 0 through
     *  <i>N</i>-1, where <i>N</i> is the number of nodes
     *  currently in the graph. Node labels maintain their
     *  consistency (remain constant) during periods when
     *  no nodes are removed from the graph. When nodes are removed,
     *  the labels assigned to the remaining nodes may change.
     *
     *  @param node A graph node.
     *  @return The node label.
     *  @exception IllegalArgumentException If the specified node is not
     *  a node in this graph.
     */
    public int nodeLabel(Node node) throws IllegalArgumentException {
        return _nodes.label(node);
    }

    /** Return the node label of the specified node given the node weight.
     *  If multiple nodes have the specified weight, then return one of their
     *  labels arbitrarily.
     *
     *  @param weight The node weight.
     *  @return The node label.
     *  @exception IllegalArgumentException If the specified weight is not
     *  a node weight in this graph.
     *  @see #nodeLabel(Node).
     */
    public int nodeLabel(Object weight) throws IllegalArgumentException {
        return _nodes.label(node(weight));
    }

    /** Return the weight of a given node in the graph given the node label.
     *
     *  @param node The node label.
     *  @return The weight of the node.
     *  @exception IndexOutOfBoundsException If the label is
     *  not valid.
     *  @exception IllegalArgumentException If the node corresponding
     *  to the label is unweighted.
     *  @see #nodeLabel(Node).
     */
    public Object nodeWeight(int label) {
        return ((Node)(_nodes.get(label))).weight();
    }

    /** Return all the nodes in this graph in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @return All the nodes in this graph.
     */
    public Collection nodes() {
        return Collections.unmodifiableList(_nodes);
    }

    /** Return all the nodes in this graph that have a specified weight.
     *  The nodes are returned in the form of a collection.
     *  Each element in the returned collection is an instance of {@link Node}.
     *  @param weight The specified weight.
     *  @return The nodes in this graph that have the specified weight.
     *  @exception NullPointerException If the specified weight
     *  is null.
     *  @exception IllegalArgumentException If the specified weight
     *  is not a node weight in this graph.
     */
    public Collection nodes(Object weight) {
        return Collections.unmodifiableList(_sameWeightNodes(weight));
    }

    /** Return the collection of nodes in this graph whose weights are contained
     *  in a specified collection.
     *  Each element in the returned collection is an instance of
     *  {@link Node}. Duplicate weights in the specified collection result
     *  in duplicate nodes in the returned collection.
     *  @param collection The specified collection of weights.
     *  @return The nodes in this graph whose weights are contained
     *  in a specified collection.
     *  @exception NullPointerException If any specified weight
     *  is null.
     *  @exception IllegalArgumentException If any specified weight
     *  is not a node weight in this graph.
     */
    public Collection nodes(Collection collection) {
        ArrayList nodes = new ArrayList();
        Iterator weights = collection.iterator();
        while (weights.hasNext()) {
            nodes.addAll(nodes(weights.next()));
        }
        return nodes;
    }

    /** Remove an edge from this graph.
     * An edge that is removed from a graph can be re-inserted
     * into the graph at a later time (using {@link #addEdge(Edge)}),
     * provided that the incident nodes are still in the graph.
     * @param edge The edge to be removed.
     * @exception IllegalArgumentException If the edge is not contained
     * in the graph.
     */
    public void removeEdge(Edge edge) {
        try {
            _edges.remove(edge);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Attempt to remove an edge "
                    + "that is not in the graph." + _edgeDump(edge));
        }
        _disconnect(edge, edge.source());
        _disconnect(edge, edge.sink());
        if (edge.hasWeight()) {
            ArrayList sameWeightList = _sameWeightEdges(edge.weight());
            sameWeightList.remove(edge);
            if (sameWeightList.size() == 0) {
                _edgeWeightMap.remove(edge.weight());
            }
        }
        _registerChange();
    }

    /** Remove a node from this graph.
     * All edges incident to the node are also removed.
     * @param node The node to be removed.
     * @exception IllegalArgumentException If the node is not contained
     * in the graph.
     */
    public void removeNode(Node node) {
        try {
            _nodes.remove(node);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Attempt to remove a node "
                    + "that is not in the graph."  + _nodeDump(node));
        }
        // Avoid concurrent modification of the incident edges list.
        Object[] incidentEdgeArray = incidentEdges(node).toArray();
        for (int i = 0; i < incidentEdgeArray.length; i++) {
            removeEdge((Edge)(incidentEdgeArray[i]));
        }
        _incidentEdgeMap.remove(node);
        if (node.hasWeight()) {
            ArrayList sameWeightList = _sameWeightNodes(node.weight());
            sameWeightList.remove(node);
            if (sameWeightList.size() == 0) {
                _nodeWeightMap.remove(node.weight());
            }
        }
        _registerChange();
    }

    /** Return the number of self loop edges in this graph.
     *  @param node The node.
     *  @return The number of self loop edges.
     */
    public int selfLoopEdgeCount() {
        return selfLoopEdges().size();
    }

    /** Return the number of self loop edges of a specified node.
     *  @param node The node.
     *  @return The number of self loop edges.
     */
    public int selfLoopEdgeCount(Node node) {
        return selfLoopEdges(node).size();
    }

    /** Return the collection of all self-loop edges in this graph.
     *  Each element in the returned collection is an {@link Edge}.
     *  This operation takes <i>O(E)</i> time.
     *  @return The self-loop edges in this graph.
     */
    public Collection selfLoopEdges() {
        if (_selfLoopListener.obsolete()) {
            _selfLoopEdges = new ArrayList();
            Iterator edges = edges().iterator();
            while (edges.hasNext()) {
                Edge edge = (Edge)edges.next();
                if (edge.isSelfLoop()) {
                    _selfLoopEdges.add(edge);
                }
            }
            _selfLoopListener.registerComputation();
        }
        return Collections.unmodifiableList(_selfLoopEdges);
    }

    /** Return the collection of all self-loop edges that are incident to
     *  a specified node. Each element in the collection is an {@link Edge}.
     *
     *  @param node The node.
     *  @return The self-loop edges that are incident to the node.
     */
    public Collection selfLoopEdges(Node node) {
        ArrayList result = new ArrayList();
        Iterator edges = incidentEdges(node).iterator();
        while (edges.hasNext()) {
            Edge edge = (Edge)edges.next();
            if (edge.isSelfLoop()) {
                result.add(edge);
            }
        }
        return result;
    }

    /** Return the subgraph induced by a collection of nodes.
     *  In other words, return the subgraph formed by the given collection N of
     *  nodes together with the set of edges of the form (x, y), where
     *  x and y are both in N.
     *  Node and edge weights are preserved. In derived classes, this
     *  method returns the same type of graph as is returned by
     *  {@link ptolemy.graph.Graph#_emptyGraph()}.
     *  @param nodes The collection of nodes; each element is a {@link Node}.
     *  @return The induced subgraph.
     */
    public Graph subgraph(Collection collection) {
        Graph subgraph = _emptyGraph();
        Iterator nodes = collection.iterator();
        while (nodes.hasNext()) {
            subgraph.addNode((Node)nodes.next());
        }
        nodes = collection.iterator();
        while (nodes.hasNext()) {
            Node node = (Node)nodes.next();
            Iterator incidentEdges = incidentEdges(node).iterator();
            while (incidentEdges.hasNext()) {
                Edge edge = (Edge)(incidentEdges.next());
                if (subgraph.containsNode(edge.source()) &&
                        subgraph.containsNode(edge.sink()) &&
                        !subgraph.containsEdge(edge)) {
                    subgraph.addEdge(edge);
                }
            }
        }
        return subgraph;
    }

    /** Return the subgraph formed by a subset of nodes and a subset of
     *  edges. Node and edge weights are preserved.
     *  In derived classes, this
     *  method returns the same type of graph as is returned by
     *  {@link ptolemy.graph.Graph#_emptyGraph()}.
     *  @param nodes The subset of nodes; each element is an instance
     *  of {@link Node}.
     *  @param edges The subset of edges. Each element is an instance
     *  of {@link Edge}.
     *  @return The subgraph.
     */
    public Graph subgraph(Collection nodeCollection,
            Collection edgeCollection) {
        Graph subgraph = _emptyGraph();
        Iterator nodes = nodeCollection.iterator();
        while (nodes.hasNext()) {
            subgraph.addNode((Node)(nodes.next()));
        }
        Iterator edges = edgeCollection.iterator();
        while (edges.hasNext()) {
            subgraph.addEdge((Edge)(edges.next()));
        }
        return subgraph;
    }

    /** Return a string representation of this graph. The string
     *  representation lists the nodes, including their labels
     *  and their weights, followed by the edges, including their
     *  labels, source nodes, sink nodes, and weights.
     *  @return A string representation of this graph.
     */
    public String toString() {
        StringBuffer result = new StringBuffer("{"
                + this.getClass().getName() + "\n");
        result.append("Node Set:\n" + _nodes.toString("\n", true) + "\n");
        result.append("Edge Set:\n" + _edges.toString("\n", true) + "\n}\n");
        return result.toString();
    }

    /** Given a collection of graph elements (nodes and edges), return an array
     * of weights associated with these elements.
     * If a weight is common across multiple elements in
     * the collection, it will appear multiple times in the array.
     * If the element collection is null or empty, an empty (zero-element)
     * array is returned.
     * @param elementCollection The collection of graph elements;
     * each element is a {@link Node} or an {@link Edge}.
     * @return The weights of the graph elements, in the order that that
     * elements are returned by collection's iterator; each element in the
     * returned array is an {@link Object}.
     * @exception NullPointerException If the specified collection contains
     * a null value.
     * @exception IllegalArgumentException If the specified collection
     * contains a non-null value that is neither a node nor an edge.
     */
    public static Object[] weightArray(Collection elementCollection) {
        if (elementCollection == null) {
            return new Object[0];
        } else {
            Object[] result = new Object[elementCollection.size()];
            Iterator elements = elementCollection.iterator();
            for (int i = 0; i < elementCollection.size(); i++) {
                Object element = elements.next();
                if (element == null) {
                    throw new NullPointerException("Null graph element "
                            + "specified.\n");
                } else if (element instanceof Node) {
                    result[i] = ((Node)element).weight();
                } else if (element instanceof Edge) {
                    result[i] = ((Edge)element).weight();
                } else {
                    throw new IllegalArgumentException("Illegal graph element "
                            + "(neither a Node nor an Edge) specified.\n"
                            + "The element's type is: "
                            + element.getClass().getName() + ".\n");
                }
            }
            return result;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create and add an edge with a specified source node, sink node,
     *  and optional weight.
     *  The third parameter specifies whether the edge is to be
     *  weighted, and the fourth parameter is the weight that is
     *  to be applied if the edge is weighted.
     *  Returns the edge that is added.
     *  @param node1 The source node of the edge.
     *  @param node2 The sink node of the edge.
     *  @param weighted True if the edge is to be weighted.
     *  @param weight The weight that is to be applied if the edge is to
     *  be weighted.
     *  @return The edge.
     *  @exception IllegalArgumentException If either of the specified nodes
     *  is not in the graph.
     *  @exception NullPointerException If the edge is to be weighted, but
     *  the specified weight is null.
     */
    protected Edge _addEdge(Node node1, Node node2, boolean weighted,
            Object weight) {
        if (!containsNode(node1)) {
            throw new IllegalArgumentException("The specified first node "
                    + "is not in the graph.\nThe node: " + _nodeDump(node1));
        }
        else if (!containsNode(node2)) {
            throw new IllegalArgumentException("The specified second node "
                    + "is not in the graph.\nThe node: " + _nodeDump(node2));

        }
        else if (weighted && (weight == null)) {
            throw new IllegalArgumentException("Attempt to assign a null "
                    + "weight to an edge. The first node:\n" + node1
                    + "\nThe second node:\n" + node2 + "The graph: \n" + this);
        } else {
            Edge edge = null;
            if (weighted) {
                edge = new Edge(node1, node2, weight);
            } else {
                edge = new Edge(node1, node2);
            }
            _registerEdge(edge);
            return edge;
        }
    }

    /** Connect an edge to a node by appropriately modifying
     * the adjacency information associated with the node.
     * @param edge The edge.
     * @param node The node.
     * @exception IllegalArgumentException If the edge has already
     * been connected to the node.
     */
    protected void _connect(Edge edge, Node node) {
        if (_incidentEdgeList(node).contains(edge)) {
            throw new IllegalArgumentException("Attempt to connect the "
                    + "same edge multiple times." + _edgeDump(edge));
        } else {
            _incidentEdgeList(node).add(edge);
        }
    }

    /** Disconnect an edge from a node that it is incident to.
     *  Do nothing if the edge is not incident to the node.
     *  @param edge The edge.
     *  @param node The node.
     */
    protected void _disconnect(Edge edge, Node node) {
        _removeIfPresent(_incidentEdgeList(node), edge);
    }

    /** Return an empty graph that has the same run-time type as this graph. 
     *  @return An empty graph.
     */
    protected Graph _emptyGraph() {
        Graph graph = null;
        try {
            graph = (Graph)(getClass().newInstance());
        } catch (Exception exception) {
            throw new RuntimeException("Could not create an empty graph from "
                    + "this one.\n" + exception + "\n" + _graphDump());
        }
        return graph;
    }

    /** Create and register all of the change listeners for this graph, and
     *  initialize the change counter of the graph.
     */
    protected void _initializeListeners() {
        _listenerList = new ArrayList();
        _selfLoopListener = new GraphListener(this);
        _changeCount = 0;
    }

    /** Register a change to the graph by updating the change counter.
     *  This method must be called after any change to the graph
     *  that may affect (invalidate) any of the computations associated with
     *  the change listeners in the graph.
     */
    protected void _registerChange() {
        if (_changeCount == Long.MAX_VALUE) {
            // Invalidate all of the change listeners.
            Iterator listeners = _listenerList.iterator();
            while (listeners.hasNext()) {
                ((GraphListener)(listeners.next())).reset();
            }
            _changeCount = 0;
        } else {
            _changeCount++;
        }
    }

    /** Register a new edge in the graph. The edge is assumed to
     *  be non-null, unique, and consistent with the node set.
     *  Derived classes can override this method to first check that
     *  the edge weight is meaningful in the context of the graph.
     *  @param edge The new edge.
     */
    protected void _registerEdge(Edge edge) {
        _edges.add(edge);
        _connect(edge, edge.source());
        if (!edge.isSelfLoop()) {
            _connect(edge, edge.sink());
        }
        if (edge.hasWeight()) {
            ArrayList sameWeightList;
            try {
                sameWeightList = _sameWeightEdges(edge.weight());
            } catch (Exception exception) {
                sameWeightList = new ArrayList();
                _edgeWeightMap.put(edge.weight(), sameWeightList);
            }
            sameWeightList.add(edge);
        }
        _registerChange();
    }

    /** Register a new node in the graph. The node is assumed to
     *  be non-null and unique.
     *  Derived classes can override this method to first check that
     *  the node weight is meaningful in the context of the graph.
     *  @param node The new node.
     */
    protected void _registerNode(Node node) {
        _nodes.add(node);
        _incidentEdgeMap.put(node, new ArrayList());
        if (node.hasWeight()) {
            ArrayList sameWeightList;
            try {
                sameWeightList = _sameWeightNodes(node.weight());
            } catch (Exception exception) {
                sameWeightList = new ArrayList();
                _nodeWeightMap.put(node.weight(), sameWeightList);
            }
            sameWeightList.add(node);
        }
        _registerChange();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Given two node weights w1 and w2, add all edges of the form
    // edges of the form (x1, x2), where
    //     (x1.weight() == w1) && (x2.weight() == w2).
    // The third parameter specifies whether the edges are to be
    // weighted, and the fourth parameter is the weight that is
    // to be applied if the edges are weighted.
    // The method returns one of the edges that is added.
    // The method returns an iterator over the edges that were added;
    // each element of this iterator is an instance of Edge.
    // The method throws an IllegalArgumentException if no edge is
    // added (i.e., if no nodes x1, x2 satisfy the above condition.
    // The method throws a NullPointerException if w1 or w2 is null.
    private Collection _addEdges(Object weight1, Object weight2,
            boolean weighted, Object weight) {
        if (weight1 == null) {
            throw new NullPointerException("Null source node weight");
        }
        else if (weight2 == null) {
            throw new NullPointerException("Null sink node weight");
        }
        Iterator nodes1 = nodes(weight1).iterator();
        Edge newEdge = null;
        ArrayList newEdges = new ArrayList();
        while (nodes1.hasNext()) {
            Node node1 = (Node)(nodes1.next());
            Iterator nodes2 = nodes(weight2).iterator();
            while (nodes2.hasNext()) {
                newEdge = _addEdge(node1, (Node)(nodes2.next()), weighted,
                        weight);
                newEdges.add(newEdge);
            }
        }
        if (newEdges.isEmpty()) {
            throw new IllegalArgumentException("No edge can be added based "
                    + "on the specified source and sink node weights.\n"
                    + "Weight1:\n" + weight1 + "\nWeight2:\n" + weight2 + "\n"
                    + _graphDump());
        } else {
            return newEdges;
        }
    }

    // Return a dump of an edge and this graph suitable to be appended
    // to an error message.
    private String _edgeDump(Edge edge) {
        String edgeString = (edge == null) ? "<null>" : edge.toString();
        return "\nDumps of the offending edge and graph follow.\n"
            + "The offending edge:\n" + edgeString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    // Return a dump of this graph suitable to be appended to an error message.
    private String _graphDump() {
        return "\nA Dump of the offending graph follows.\n" + toString()
            + "\n";
    }

    // Return the list of incident edges for a specified node.
    private ArrayList _incidentEdgeList(Node node) {
        return (ArrayList)_incidentEdgeMap.get(node);
    }

    // Return a dump of a node and this graph suitable to be appended
    // to an error message.
    private String _nodeDump(Node node) {
        String nodeString = (node == null) ? "<null>" : node.toString();
        return "\nDumps of the offending node and graph follow.\n"
            + "The offending node:\n" + nodeString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    // Remove an object from an ArrayList if it exists in the list.
    private void _removeIfPresent(ArrayList list, Object element) {
        int index;
        if ((index = list.indexOf(element)) != -1) {
            list.remove(index);
        }
    }

    // Return the list of edges that have a given edge weight. Return
    // null if no edges have the given weight.
    // @exception NullPointerException If the specified weight is null.
    // @exception IllegalArgumentException If the specified weight
    // is not an edge weight in this graph.
    private ArrayList _sameWeightEdges(Object weight) {
        if (weight == null) {
            throw new NullPointerException("Null edge weight specified.");
        } else {
            ArrayList edgeList = (ArrayList)_edgeWeightMap.get(weight);
            if (edgeList == null) {
                throw new IllegalArgumentException("The specified weight "
                        + "is not an edge weight in this graph."
                        + _weightDump(weight));
            } else {
                return edgeList;
            }
        }
    }

    // Return the list of nodes that have a given node weight. Return
    // null if no nodes have the given weight.
    // @exception NullPointerException If the specified weight is null.
    // @exception IllegalArgumentException If the specified weight
    // is not a node weight in this graph.
    private ArrayList _sameWeightNodes(Object weight) {
        if (weight == null) {
            throw new NullPointerException("Null node weight specified.");
        } else {
            ArrayList nodeList = (ArrayList)_nodeWeightMap.get(weight);
            if (nodeList == null) {
                throw new IllegalArgumentException("The specified weight "
                        + "is not a node weight in this graph."
                        + _weightDump(weight));
            } else {
                return nodeList;
            }
        }
    }

    // Return a dump of a node or edge weight and this graph suitable to be
    // appended to an error message.
    private String _weightDump(Object weight) {
        String weightString = (weight == null) ? "<null>" : weight.toString();
        return "\nDumps of the offending weight and graph follow.\n"
            + "The offending weight:\n" + weightString
            + "\nThe offending graph:\n" + this.description() + "\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A counter that keeps track of changes to the graph.
    private long _changeCount;

    // A mapping from edge weights to associated edges. Unweighted edges are not
    // represented in this map. Keys in this this map are instances of
    // of Object, and values are instances of ArrayList whose elements
    // are instances of Edge.
    private HashMap _edgeWeightMap;

    // The list of edges in this graph.
    // Each element of this list is an Edge.
    private LabeledList _edges;

    // A mapping from nodes into their lists of incident edges.
    // This redundant information is maintained for improved
    // run-time efficiency when handing undirected graphs, or when operating
    // on directed graphs in ways for which edge orientation is not relevant.
    // Each key in this map is an instance of Node. Each value
    // is an instance of ArrayList whose elements are instances of Edge.
    private HashMap _incidentEdgeMap;

    // A list of objects that track changes to the graph. Each element
    // is a GraphListener.
    private ArrayList _listenerList;

    // A mapping from node weights to associated nodes. Unweighted nodes are not
    // represented in this map. Keys in this this map are instances of
    // of Object, and values instances of ArrayList whose elements are
    // instances of Node.
    private HashMap _nodeWeightMap;

    // The list of nodes in this graph.
    // Each element of this list is a Node.
    private LabeledList _nodes;

    // The graph listener for computation of self loop edges.
    private GraphListener _selfLoopListener;

    // The set of self-loop edges in this graph. Recomputation requirements
    // of this data structure are tracked by _selfLoopListener.
    private ArrayList _selfLoopEdges;
}
