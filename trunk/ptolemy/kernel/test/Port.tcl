# Tests for the Port class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

# Load up Tcl procs to print out enums
if {[info procs _testPortEnumRelations] == "" } then { 
    source testEnums.tcl
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
test Port-1.1 {Get information about an instance of Port} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.Port]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.Port
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getContainer getFullName getName {setName java.lang.String} getLinkedRelations {link pt.kernel.Relation} numLinks {setContainer pt.kernel.Entity} {unlink pt.kernel.Relation} unlinkAll
  constructors:  pt.kernel.Port {pt.kernel.Port pt.kernel.Entity java.lang.String}
  properties:    fullName class name container linkedRelations
  superclass:    pt.kernel.NamedObj
}}

######################################################################
####
# 
test Port-2.1 {Construct Ports} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    set p2 [java::new pt.kernel.Port $e1 "My Port"]
    list [$p1 getName] [$p2 getName] \
	    [$p1 numLinks] [$p2 numLinks]
} {{} {My Port} 0 0}

######################################################################
####
# 
test Port-3.1 {Test link with one port, one relation} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation "My Relation"]
    $p1 link $r1
    list [_testPortGetLinkedRelations $p1]
} {{{{My Relation}}}}

######################################################################
####
# 
test Port-3.1.1 {Test link with one port, one relation twice} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation "My Relation"]
    $p1 link $r1
    $p1 link $r1
    list [_testPortGetLinkedRelations $p1]
} {{{{My Relation} {My Relation}}}}

######################################################################
####
# 
test Port-3.1.2 {Test link with one port to a null relation} {
    set p1 [java::new pt.kernel.Port]
    $p1 link [java::null]
    list [_testPortGetLinkedRelations $p1]
} {{{}}}

######################################################################
####
# 
test Port-3.2 {Test link with one port, two relations} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation "My Relation"]
    set r2 [java::new pt.kernel.Relation "My Other Relation"]
    $p1 link $r1
    $p1 link $r2
    list [_testPortGetLinkedRelations $p1]
} {{{{My Relation} {My Other Relation}}}}

######################################################################
####
# 
test Port-3.3 {Test link with two ports, one relation} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation "My Relation"]
    $p1 link $r1
    $p2 link $r1
    list [_testPortGetLinkedRelations $p1 $p2]
} {{{{My Relation}} {{My Relation}}}}

######################################################################
####
# 
test Port-3.4 {Test link with two ports, two relations} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation "My Relation"]
    set r2 [java::new pt.kernel.Relation "My Other Relation"]
    $p1 link $r1
    $p2 link $r1
    $p1 link $r2
    $p2 link $r2
    list [_testPortGetLinkedRelations $p1 $p2] \
	    [$p1 numLinks] \
	    [$p2 numLinks]
} {{{{My Relation} {My Other Relation}} {{My Relation} {My Other Relation}}} 2 2}

######################################################################
####
# 
test Port-4.1 {Test unlinkAll} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set p2 [java::new pt.kernel.Port $e1 P2]
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
    $p1 link $r1
    $p2 link $r1
    $p1 link $r2
    $p2 link $r2
    $p1 unlinkAll
    set result1 [_testPortGetLinkedRelations $p1 $p2]
    # We call this twice to make sure that if there are no relations,
    # we don't cause an error.
    $p1 unlinkAll
    set result2 [_testPortGetLinkedRelations $p1 $p2]
    $p2 unlinkAll 
    set result3 [_testPortGetLinkedRelations $p1 $p2]
   list "$result1\n$result2\n$result3"
} {{{} {relation1 relation2}
{} {relation1 relation2}
{} {}}}

######################################################################
####
# 
test Port-5.1 {Test unlink} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port $e1 P1]
    set p2 [java::new pt.kernel.Port]
    $p2 setContainer $e1
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
    $p1 link $r1
    $p2 link $r1
    $p1 link $r2
    $p2 link $r2
    $p1 unlink $r1
    set result1 [_testPortGetLinkedRelations $p1 $p2]
    $p2 unlink $r2
    set result2 [_testPortGetLinkedRelations $p1 $p2]
    $p2 unlink $r1
    set result3 [_testPortGetLinkedRelations $p1 $p2]

    # Call unlink on a relation that has already been disconnected.
    $p2 unlink $r1
    set result4 [expr {$result3 == [_testPortGetLinkedRelations $p1 $p2]}]

    $p1 unlink $r2
    set result5 [_testPortGetLinkedRelations $p1 $p2]

   list "$result1\n$result2\n$result3\n$result4\n$result5"
} {{relation2 {relation1 relation2}
relation2 relation1
relation2 {}
1
{} {}}}

######################################################################
####
# 
test Port-5.2 {Test unlink on a relation we are not connected to} {
    set e1 [java::new pt.kernel.Entity]
    set p1 [java::new pt.kernel.Port]
    $p1 setContainer $e1
    set r1 [java::new pt.kernel.Relation "relation1"]
    set r2 [java::new pt.kernel.Relation "relation2"]
    $p1 link $r1
    $p1 unlink $r2
    list [_testPortGetLinkedRelations $p1]
} {relation1}

######################################################################
####
# 
test Port-6.1 {Test getLinkedRElations} {
    set p1 [java::new pt.kernel.Port]
    set enum [$p1 getLinkedRelations]
    catch {$enum nextElement} errmsg
    list $errmsg [$enum hasMoreElements]
} {{java.util.NoSuchElementException: exhausted enumeration} 0}

######################################################################
####
# 
test Port-7.1 {Test getContainer on a Port that has no container } {
    set p1 [java::new pt.kernel.Port]
    list [expr { [java::null] == [$p1 getContainer] } ]
} {1}

######################################################################
####
# 
test Port-7.2 {Test getContainer on a Port that has a container } {
    set p1 [java::new pt.kernel.Port]
    set e1 [java::new pt.kernel.Entity "entity1"]
    $p1 setContainer $e1
    list [expr { $e1 == [$p1 getContainer] } ]
} {1}

######################################################################
####
# 
test Port-8.1 {Build a topology consiting of a Ramp and a Print Entity} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    # Note that we are not getting all the information we could
    list [_testPortGetLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{Arc Arc} {{{Ramp out}}} {{{Print in}}}}

######################################################################
####
# 
test Port-9.1 {Remove a port from its container} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    $out setContainer [java::null]

    # Note that we are not getting all the information we could
    list [_testPortGetLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{{} Arc} {{}} {{{Print in}}}}

######################################################################
####
# 
test Port-10.1 {Reassign a port to a new container} {
    # Create objects
    set ramp [java::new pt.kernel.Entity "Ramp"]
    set print [java::new pt.kernel.Entity "Print"]
    set out [java::new pt.kernel.Port $ramp "Ramp out"]
    set in [java::new pt.kernel.Port $print "Print in"]
    set arc [java::new pt.kernel.Relation "Arc"]

    # Connect
    $out link $arc
    $in link $arc

    $out setContainer $print

    # Note that we are not getting all the information we could
    list [_testPortGetLinkedRelations $out $in] \
            [_testEntityGetPorts $ramp] \
            [_testEntityGetPorts $print]
} {{Arc Arc} {{}} {{{Print in} {Ramp out}}}}

