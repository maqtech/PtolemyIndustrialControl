# Tests for the Node class
#
# @Author: Shuvra S. Bhattacharyya 
#
# $Id$
#
# @Copyright (c) 2001-2002 The Regents of the University of Maryland.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
}

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
test Node-2.1 {Create an unweighted node} {
    set n [java::new ptolemy.graph.Node]
    set hasWeight [$n hasWeight]
    set representation [$n toString]
    list $hasWeight $representation
} {0 {<unweighted node>}}

######################################################################
####
# 
test Node-2.2 {Attempt to access the weight of an unweighted node} {
    catch {$n weight} msg
    list $msg
} {{java.lang.IllegalStateException: Attempt to access the weight of an unweighted node.
}}

######################################################################
####
# 
test Node-3.1 {Create a weighted node} {
    set weight [java::new {java.lang.String String} aSimpleWeight]
    set n2 [java::new ptolemy.graph.Node $weight]
    set hasWeight [$n2 hasWeight]
    set representation [$n2 toString]
    list $hasWeight $representation
} {1 aSimpleWeight}

######################################################################
####
# 
test Node-3.2 {Attempt to create a weighted node with a null weight} {

    catch {set newNode [java::new {ptolemy.graph.Node Object} [java::null]]} \
            msg
    list $msg
} {{java.lang.IllegalArgumentException: Attempt to assign a null weight to a node.}}
