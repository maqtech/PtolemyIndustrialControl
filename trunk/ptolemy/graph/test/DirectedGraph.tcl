# Tests for the DirectedGraph class
#
# @Author: Yuhong Xiong, Shuvra S. Bhattacharyya, Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 1997-2002 The Regents of the University of California.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#


######################################################################
####
# 
test DirectedGraph-2.1 {Create an empty instance} {
    set p [java::new ptolemy.graph.DirectedGraph]
    list [$p contains null] [$p isAcyclic]
} {0 1}

######################################################################
####
# 
#    set z [$p {reachableNodes Object} null] 
test DirectedGraph-2.2 {test reachableNodes on empty graph above} {
     catch {$p {reachableNodes Object} null} msg
     list $msg
} {{java.lang.IllegalArgumentException: The specified weight is not a node weight in this graph.
Dumps of the offending weight and graph follow.
The offending weight:
null
The offending graph:
{ptolemy.graph.DirectedGraph
}
}}

######################################################################
####
# 
test DirectedGraph-3.1 {Create a cyclic graph with 2 nodes} {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    $p add $n1
    $p add $n2
    $p addEdge $n1 $n2
    $p addEdge $n2 $n1
    set reach [$p {reachableNodes Object} $n1]
    list [$p isAcyclic] [$reach get 0] [$reach get 1]
} {0 node1 node2}

######################################################################
####
# 
test DirectedGraph-3.2 {an acyclic graph with 4 nodes forming a diamond} {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    $p add $n1
    $p add $n2
    $p add $n3
    $p add $n4
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    $p addEdge $n2 $n4
    $p addEdge $n3 $n4
    set reach [$p {reachableNodes Object} $n2]
    list [$p isAcyclic] \
	 [$reach get 0]
} {1 node4}

######################################################################
####
# 
test DirectedGraph-4.1 { backwardReachableNodes } {
    # Note: Use the previous set up.
    set reach [$p {backwardReachableNodes Object} $n4]
    list [$reach get 0] [$reach get 1] [$reach get 2]
} {node1 node2 node3}

######################################################################
####
# 
test DirectedGraph-4.2 { backwardReachableNodes for a set of nodes } {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node5]
    set n6 [java::new {java.lang.String String} node6]
    $p add $n1
    $p add $n2
    $p add $n3
    $p add $n4
    $p add $n5
    $p add $n6
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    $p addEdge $n2 $n4
    $p addEdge $n3 $n4
    $p addEdge $n5 $n6

    set nodeArray [java::new {java.lang.String[]} 2 [list $n4 $n6]]
    set reach [$p {backwardReachableNodes Object[]} $nodeArray]
    list [$reach get 0] [$reach get 1] [$reach get 2] [$reach get 3]
} {node1 node2 node3 node5}

######################################################################
####
# 
test DirectedGraph-4.3 { reachableNodes for a set of nodes } {
    set nodeArray [java::new {java.lang.String[]} 4 [list $n1 $n2 $n3 $n5]]
    set reach [$p {reachableNodes Object[]}  $nodeArray]
    $reach getrange 0
} {node2 node3 node4 node6}

######################################################################
####
# 
test DirectedGraph-5.1 { cycleNodes } {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    $p add $n1
    $p add $n2
    $p add $n3
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3

    set cycle [$p cycleNodes]
    list [$p isAcyclic] [$cycle getrange 0]
} {1 {}}

######################################################################
####
# 
test DirectedGraph-5.2 { cycleNodes } {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    $p add $n1
    $p add $n2
    $p add $n3
    $p addEdge $n1 $n2
    $p addEdge $n2 $n1
    $p addEdge $n2 $n3

    set cycle [$p cycleNodes]
    list [$p isAcyclic] [$cycle getrange 0]
} {0 {node1 node2}}

######################################################################
####
# 
test DirectedGraph-5.3 { cycleNodes } {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node5]
    set n6 [java::new {java.lang.String String} node6]
    $p add $n1
    $p add $n2
    $p add $n3
    $p add $n4
    $p add $n5
    $p add $n6
    $p addEdge $n1 $n2
    $p addEdge $n2 $n3
    $p addEdge $n3 $n1
    $p addEdge $n3 $n5
    $p addEdge $n5 $n4
    $p addEdge $n4 $n3
    $p addEdge $n5 $n6

    set cycle [$p cycleNodes]
    list [$p isAcyclic] [$cycle getrange 0]
} {0 {node1 node2 node3 node4 node5}}

######################################################################
####
# 
test DirectedGraph-5.4 { successors } {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set z [$p add $n1]
    $p add $n2
    $p add $n3
    $p add $n4
    $p addEdge $n1 $n2
    $p addEdge $n1 $n2
    $p addEdge $n1 $n4
    $p addEdge $n4 $n1
    $p addEdge $n4 $n1
    $p addEdge $n4 $n1
    $p addEdge $n3 $n1
    set s [$p successors $z]
    set result [java::call ptolemy.graph.test.Utilities toSortedString $s 1]
    list $result
} {{[node2, node4]}}

######################################################################
####
# 
test DirectedGraph-5.5 { predecessors } {
    set s [$p predecessors $z]
    set result [java::call ptolemy.graph.test.Utilities toSortedString $s 1]
    list $result
} {{[node3, node4]}}

######################################################################
####
# 
test DirectedGraph-6.1 {Test successor edges} {
    set p [java::new ptolemy.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set node1 [$p add $n1]
    set node2 [$p add $n2]
    set node3 [$p add $n3]
    set node4 [$p add $n4]
    set e1 [java::new {java.lang.String String} edge1]
    set e2 [java::new {java.lang.String String} edge2]
    set e3 [java::new {java.lang.String String} edge3]
    set e4 [java::new {java.lang.String String} edge4]
    set e5 [java::new {java.lang.String String} edge5]
    set e6 [java::new {java.lang.String String} edge6]
    set e7 [java::new {java.lang.String String} edge7]
    set e8 [java::new {java.lang.String String} edge8]
    set edge1 [$p addEdge $n1 $n2 $e1]
    set edge2 [$p addEdge $n1 $n2 $e2]
    set edge3 [$p addEdge $n1 $n3 $e3]
    set edge4 [$p addEdge $n2 $n3 $e4]
    set edge5 [$p addEdge $n3 $n2 $e5]
    set edge6 [$p addEdge $n3 $n2 $e6]
    set edge7 [$p addEdge $n2 $n4 $e7]
    set edge8 [$p addEdge $n4 $n1 $e8]
    set nodes [$p nodes]
    set collection [$p successorEdges $node2 $node3]
    set obj [java::cast java.lang.Object $collection]
    set result1 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p successorEdges $node1 $node3]
    set obj [java::cast java.lang.Object $collection]
    set result2 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p successorEdges $node1 $node2]
    set obj [java::cast java.lang.Object $collection]
    set result3 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p successorEdges $node2 $node4]
    set obj [java::cast java.lang.Object $collection]
    set result4 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p successorEdges $node2 $node1]
    set obj [java::cast java.lang.Object $collection]
    set result5 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    list $result1 $result2 $result3 $result4 $result5
} {{[(node2, node3, edge4)]} {[(node1, node3, edge3)]} {[(node1, node2, edge1), (node1, node2, edge2)]} {[(node2, node4, edge7)]} {[]}}

######################################################################
####
# 
test Graph-6.2 {Test predecessor edges} {
    set collection [$p predecessorEdges $node2 $node3]
    set obj [java::cast java.lang.Object $collection]
    set result1 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p predecessorEdges $node1 $node3]
    set obj [java::cast java.lang.Object $collection]
    set result2 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p predecessorEdges $node4 $node2]
    set obj [java::cast java.lang.Object $collection]
    set result3 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p predecessorEdges $node2 $node1]
    set obj [java::cast java.lang.Object $collection]
    set result4 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    list $result1 $result2 $result3 $result4
} {{[(node3, node2, edge5), (node3, node2, edge6)]} {[]} {[(node2, node4, edge7)]} {[(node1, node2, edge1), (node1, node2, edge2)]}}
