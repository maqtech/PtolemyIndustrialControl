# Tests for the CompositeEntity class
#
# @Author: Edward A. Lee
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

if {[info procs enumToObjects] == "" } then { 
     source enums.tcl
}

if {[info procs description2TclBlend] == "" } then { 
     source description.tcl
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
test CompositeEntity-1.1 {Get information about an instance \
	of CompositeEntity} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.CompositeEntity]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.CompositeEntity
  fields:        
  methods:       {addEntity pt.kernel.ComponentEntity} {addPort pt.kerne
    l.Port} {addRelation pt.kernel.ComponentRelation} {allo
    wLevelCrossingConnect boolean} {connect pt.kernel.Compo
    nentPort pt.kernel.ComponentPort} {connect pt.kernel.Co
    mponentPort pt.kernel.ComponentPort java.lang.String} c
    onnectedPorts {deepContains pt.kernel.ComponentEntity} 
    deepGetEntities {description int} {equals java.lang.Obj
    ect} getClass getContainer getEntities {getEntity java.
    lang.String} getFullName getName {getPort java.lang.Str
    ing} getPorts {getRelation java.lang.String} getRelatio
    ns hashCode isAtomic linkedRelations {newPort java.lang
    .String} {newRelation java.lang.String} notify notifyAl
    l numEntities numRelations removeAllEntities removeAllP
    orts removeAllRelations {removeEntity pt.kernel.Compone
    ntEntity} {removePort pt.kernel.Port} {removeRelation p
    t.kernel.ComponentRelation} {setContainer pt.kernel.Com
    positeEntity} {setName java.lang.String} toString wait 
    {wait long} {wait long int} workspace
    
  constructors:  pt.kernel.CompositeEntity {pt.kernel.CompositeEntity pt
    .kernel.CompositeEntity java.lang.String} {pt.kernel.Co
    mpositeEntity pt.kernel.Workspace}
    
  properties:    atomic class container entities fullName name ports rel
    ations
    
  superclass:    pt.kernel.ComponentEntity
    
}}

######################################################################
####
# 
test CompositeEntity-2.0 {Construct CompositeEntities, call a few methods} {
    set e1 [java::new pt.kernel.CompositeEntity]
    set e2 [java::new pt.kernel.CompositeEntity]
    $e2 setName A
    set e1contents [$e1 getEntities]
    list [$e1 getName] [$e2 getName] \
	    [$e1 getFullName] [$e2 getFullName] \
	    [$e1 isAtomic] [$e2 isAtomic] \
	    [ java::instanceof $e1contents java.util.Enumeration] \
	    [expr {[java::null] == [$e1 getContainer]}]
} {{} A . .A 0 0 1 1}

######################################################################
####
# 
test CompositeEntity-2.1 {Create a 3 level deep tree using constructors} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {{B C} {} D}

######################################################################
####
# 
test CompositeEntity-2.2 {Create a 3 level deep tree after construction} {
    # entity serving as a workspace
    set w [java::new pt.kernel.CompositeEntity]
    set a [java::new pt.kernel.CompositeEntity $w A]
    set b [java::new pt.kernel.CompositeEntity $w B]
    set c [java::new pt.kernel.CompositeEntity $w C]
    set d [java::new pt.kernel.ComponentEntity $w D]
    $c addEntity $d
    $a addEntity $b
    $b addEntity $c
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
# 
test CompositeEntity-3.1 {Test deepGetEntities} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [enumToNames [$a deepGetEntities]] \
            [enumToNames [$c deepGetEntities]] \
} {{B D} D}

######################################################################
####
# 
test CompositeEntity-3.2 {Test numEntities} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [$a numEntities] [$b numEntities] [$c numEntities]
} {2 0 1}

######################################################################
####
# 
test CompositeEntity-3.3 {Test getEntity by name} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    set e [$c getEntity D]
    $e getFullName
} {.A.B.C.D}

######################################################################
####
# 
test CompositeEntity-4.1 {Test deepContains} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    list [$a deepContains $d] [$a deepContains $a] [$c deepContains $a] \
            [$c deepContains $d] [$b deepContains $d]
} {1 0 0 1 0}

######################################################################
####
# 
test CompositeEntity-5.1 {Test reparenting} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.ComponentEntity $c D]
    $c setContainer $b
    list [enumToNames [$a getEntities]] \
            [enumToNames [$b getEntities]] \
            [enumToNames [$c getEntities]]
} {B C D}

######################################################################
####
# 
test CompositeEntity-5.2 {Test reparenting with an error} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$c setContainer $d} msg
    list $msg
} {{pt.kernel.IllegalActionException: .A.C and .A.C.D: Attempt to construct recursive containment.}}

######################################################################
####
# 
test CompositeEntity-6.1 {Test removing entities} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    $a {removeEntity pt.kernel.ComponentEntity} $b
    $c {removeEntity pt.kernel.ComponentEntity} $d
    $b {removeEntity pt.kernel.ComponentEntity} $c
    enumMethodToNames getEntities $a $b $c $d
} {{} {} {} {}}

######################################################################
####
# 
test CompositeEntity-6.2 {Test removing entities with an error} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$a {removeEntity pt.kernel.ComponentEntity} $d} msg
    list $msg
} {{pt.kernel.IllegalActionException: .A and .A.B.C.D: Attempt to remove an entity from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-6.3 {Test removing entities by name} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    $a removeEntity [$a getEntity B]
    enumMethodToNames getEntities $a $b $c $d
} {{} C D {}}

######################################################################
####
# 
test CompositeEntity-6.4 {Test removing entities by name with an error} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $b C]
    set d [java::new pt.kernel.CompositeEntity $c D]
    catch {$a removeEntity $d} msg
    list $msg
} {{pt.kernel.IllegalActionException: .A and .A.B.C.D: Attempt to remove an entity from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-7.1 {Add relations} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-7.2 {Add relations after creation} {
    # Workspace entity
    set w [java::new pt.kernel.CompositeEntity]
    set a [java::new pt.kernel.CompositeEntity $w A]
    set r1 [java::new pt.kernel.ComponentRelation $w R1]
    set r2 [java::new pt.kernel.ComponentRelation $w R2]
    $a addRelation $r1
    $a addRelation $r2
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-7.3 {Get relations by name} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    set r [$a getRelation R1]
    $r getFullName
} {.A.R1}

######################################################################
####
# 
test CompositeEntity-7.4 {Add relations using newRelation} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [$a newRelation R1]
    set r2 [$a newRelation R2]
    enumToNames [$a getRelations]
} {R1 R2}

######################################################################
####
# 
test CompositeEntity-8.1 {Remove relations} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a {removeRelation pt.kernel.ComponentRelation} $r1
    enumToNames [$a getRelations]
} {R2}

######################################################################
####
# 
test CompositeEntity-8.2 {Remove relations with an error} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation]
    $r1 setName R1
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    catch {$a {removeRelation pt.kernel.ComponentRelation} $r1} msg
    list $msg
} {{pt.kernel.IllegalActionException: .A and .R1: Attempt to remove a relation from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-8.3 {Remove relations by name} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a removeRelation $r2
    enumToNames [$a getRelations]
} {R1}

######################################################################
####
# 
test CompositeEntity-8.4 {Remove relations by name with an error} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation]
    $r1 setName R1
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    catch {$a removeRelation $r1} msg
    list $msg
} {{pt.kernel.IllegalActionException: .A and .R1: Attempt to remove a relation from a container that does not contain it.}}

######################################################################
####
# 
test CompositeEntity-8.5 {Test removing all entities} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set c [java::new pt.kernel.CompositeEntity $a C]
    set d [java::new pt.kernel.CompositeEntity $a D]
    $a removeAllEntities
    enumToNames [$a getEntities]
} {}

######################################################################
####
# 
test CompositeEntity-8.6 {Remove all relations} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set r1 [java::new pt.kernel.ComponentRelation $a R1]
    set r2 [java::new pt.kernel.ComponentRelation $a R2]
    $a removeAllRelations
    enumToNames [$a getRelations]
} {}

######################################################################
####
# 
test CompositeEntity-9.1 {Test transparent port} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.CompositeEntity $a B]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    $a connect $p2 $p1
    set result {}
    foreach ar [enumToObjects [$p1 insideRelations]] {
        lappend result [enumToFullNames [$ar linkedPortsExcept $p1]]
    }
    list $result
} {.A.B.P2}

######################################################################
####
# 
test CompositeEntity-10.1 {Test multiple relation naming} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1]
    set ar2 [$a connect $p3 $p4]
    list [$ar1 getFullName] [$ar2 getFullName]
} {.A._R0 .A._R1}

######################################################################
####
# 
test CompositeEntity-10.3 {Create and then remove a transparent port} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1]
    set ar2 [$a connect $p3 $p4]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $a {removeRelation pt.kernel.ComponentRelation} $ar2
    lappend result [$p4 numInsideLinks]
} {.A._R0 .A._R1 0}

######################################################################
####
# 
test CompositeEntity-10.4 {Create and then remove ports with given names} {
    set a [java::new pt.kernel.CompositeEntity]
    $a setName A
    set b [java::new pt.kernel.ComponentEntity $a B]
    set c [java::new pt.kernel.ComponentEntity $a C]
    set p1 [java::new pt.kernel.ComponentPort $a P1]
    set p2 [java::new pt.kernel.ComponentPort $b P2]
    set p3 [java::new pt.kernel.ComponentPort $c P3]
    set p4 [java::new pt.kernel.ComponentPort $a P4]
    set ar1 [$a connect $p2 $p1 AR1]
    set ar2 [$a connect $p3 $p4 AR2]
    set result {}
    lappend result [$ar1 getFullName] [$ar2 getFullName]
    $a removeRelation $ar2
    lappend result [$p4 numInsideLinks]
} {.A.AR1 .A.AR2 0}

######################################################################
####
# NOTE:  The setup constructed in this test is used in the subsequent
# tests.
test CompositeEntity-11.1 {Test deepLinkedEntities on component relations} {
    # This structure is the example in the kernel design document.

    # See ExampleSystem.tcl for a standalone file containing these commands

    # Create composite entities
    set e0 [java::new pt.kernel.CompositeEntity]
    $e0 setName E0
    set e3 [java::new pt.kernel.CompositeEntity $e0 E3]
    set e4 [java::new pt.kernel.CompositeEntity $e3 E4]
    set e7 [java::new pt.kernel.CompositeEntity $e0 E7]
    set e10 [java::new pt.kernel.CompositeEntity $e0 E10]

    # Create component entities.
    set e1 [java::new pt.kernel.ComponentEntity $e4 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e4 E2]
    set e5 [java::new pt.kernel.ComponentEntity $e3 E5]
    set e6 [java::new pt.kernel.ComponentEntity $e3 E6]
    set e8 [java::new pt.kernel.ComponentEntity $e7 E8]
    set e9 [java::new pt.kernel.ComponentEntity $e10 E9]

    # Create ports.
    set p0 [$e4 newPort P0]
    set p1 [$e1 newPort P1]
    set p2 [$e2 newPort P2]
    set p3 [$e2 newPort P3]
    set p4 [$e4 newPort P4]
    set p5 [$e5 newPort P5]
    set p6 [$e6 newPort P6]
    set p7 [$e3 newPort P7]
    set p8 [$e7 newPort P8]
    set p9 [$e8 newPort P9]
    set p10 [$e8 newPort P10]
    set p11 [$e7 newPort P11]
    set p12 [$e10 newPort P12]
    set p13 [$e10 newPort P13]
    set p14 [$e9 newPort P14]

    # Create links
    set r1 [$e4 connect $p1 $p0 R1]
    set r2 [$e4 connect $p1 $p4 R2]
    $p3 link $r2
    set r3 [$e4 connect $p1 $p2 R3]
    set r4 [$e3 connect $p4 $p7 R4]
    set r5 [$e3 connect $p4 $p5 R5]
    $e3 allowLevelCrossingConnect true
    set r6 [$e3 connect $p3 $p6 R6]
    set r7 [$e0 connect $p7 $p13 R7]
    set r8 [$e7 connect $p9 $p8 R8]
    set r9 [$e7 connect $p10 $p11 R9]
    set r10 [$e0 connect $p8 $p12 R10]
    set r11 [$e10 connect $p12 $p13 R11]
    set r12 [$e10 connect $p14 $p13 R12]
    $p11 link $r7

    enumMethodToNames deepLinkedPorts $r1 $r2 $r3 $r4 $r5 $r6 $r7 $r8 $r9 \
            $r10 $r11 $r12
} {P1 {P1 P9 P14 P10 P5 P3} {P1 P2} {P1 P3 P9 P14 P10} {P1 P3 P5} {P3 P6} {P1 P3 P9 P14 P10} {P9 P1 P3 P10} {P10 P1 P3 P9 P14} {P9 P1 P3 P10} {P9 P1 P3 P10} {P14 P1 P3 P10}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.2 {Test linkedPorts on relations} {
    enumMethodToNames linkedPorts $r1 $r2 $r3 $r4 $r5 $r6 $r7 $r8 $r9 \
            $r10 $r11 $r12
} {{P1 P0} {P1 P4 P3} {P1 P2} {P4 P7} {P4 P5} {P3 P6} {P7 P13 P11} {P9 P8} {P10 P11} {P8 P12} {P12 P13} {P14 P13}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.3 {Test deepConnectedPorts on ports} {
    enumMethodToNames deepConnectedPorts $p0 $p1 $p2 $p3 $p4 $p5 $p6 \
            $p7 $p8 $p9 $p10 $p11 $p12 $p13 $p14
} {{} {P9 P14 P10 P5 P3 P2} P1 {P1 P9 P14 P10 P5 P6} {P9 P14 P10 P5} {P1 P3} P3 {P9 P14 P10} {P1 P3 P10} {P1 P3 P10} {P1 P3 P9 P14} {P1 P3 P9 P14} P9 {P1 P3 P10} {P1 P3 P10}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.4 {Test connectedPorts on ports} {
    enumMethodToNames connectedPorts $p0 $p1 $p2 $p3 $p4 $p5 $p6 \
            $p7 $p8 $p9 $p10 $p11 $p12 $p13 $p14
} {{} {P0 P4 P3 P2} P1 {P1 P4 P6} {P7 P5} P4 P3 {P13 P11} P12 P8 P11 {P7 P13} P8 {P7 P11} P13}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.5 {Test QUIET description} {
    $e0 description [java::field pt.kernel.Nameable QUIET]
} {pt.kernel.CompositeEntity {.E0}}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.6 {Test PRETTYPRINT description} {
    $e0 description [java::field pt.kernel.Nameable PRETTYPRINT]
} {pt.kernel.CompositeEntity {.E0}
pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentRelation {.E0.R10}
pt.kernel.CompositeEntity {.E0.E3}
pt.kernel.ComponentPort {.E0.E3.P7}
pt.kernel.ComponentRelation {.E0.E3.R4}
pt.kernel.ComponentRelation {.E0.E3.R5}
pt.kernel.ComponentRelation {.E0.E3.R6}
pt.kernel.CompositeEntity {.E0.E3.E4}
pt.kernel.ComponentPort {.E0.E3.E4.P0}
pt.kernel.ComponentPort {.E0.E3.E4.P4}
pt.kernel.ComponentRelation {.E0.E3.E4.R1}
pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentRelation {.E0.E3.E4.R3}
pt.kernel.ComponentEntity {.E0.E3.E4.E1}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1}
pt.kernel.ComponentEntity {.E0.E3.E4.E2}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P2}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P3}
pt.kernel.ComponentEntity {.E0.E3.E5}
pt.kernel.ComponentPort {.E0.E3.E5.P5}
pt.kernel.ComponentEntity {.E0.E3.E6}
pt.kernel.ComponentPort {.E0.E3.E6.P6}
pt.kernel.CompositeEntity {.E0.E7}
pt.kernel.ComponentPort {.E0.E7.P8}
pt.kernel.ComponentPort {.E0.E7.P11}
pt.kernel.ComponentRelation {.E0.E7.R8}
pt.kernel.ComponentRelation {.E0.E7.R9}
pt.kernel.ComponentEntity {.E0.E7.E8}
pt.kernel.ComponentPort {.E0.E7.E8.P9}
pt.kernel.ComponentPort {.E0.E7.E8.P10}
pt.kernel.CompositeEntity {.E0.E10}
pt.kernel.ComponentPort {.E0.E10.P12}
pt.kernel.ComponentPort {.E0.E10.P13}
pt.kernel.ComponentRelation {.E0.E10.R11}
pt.kernel.ComponentRelation {.E0.E10.R12}
pt.kernel.ComponentEntity {.E0.E10.E9}
pt.kernel.ComponentPort {.E0.E10.E9.P14}
pt.kernel.ComponentPort {.E0.E3.P7} link pt.kernel.ComponentRelation {.E0.E3.R4}
pt.kernel.ComponentPort {.E0.E3.P7} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E3.E4.P0} link pt.kernel.ComponentRelation {.E0.E3.E4.R1}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.R4}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.R5}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R1}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R3}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P2} link pt.kernel.ComponentRelation {.E0.E3.E4.R3}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P3} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P3} link pt.kernel.ComponentRelation {.E0.E3.R6}
pt.kernel.ComponentPort {.E0.E3.E5.P5} link pt.kernel.ComponentRelation {.E0.E3.R5}
pt.kernel.ComponentPort {.E0.E3.E6.P6} link pt.kernel.ComponentRelation {.E0.E3.R6}
pt.kernel.ComponentPort {.E0.E7.P8} link pt.kernel.ComponentRelation {.E0.E7.R8}
pt.kernel.ComponentPort {.E0.E7.P8} link pt.kernel.ComponentRelation {.E0.R10}
pt.kernel.ComponentPort {.E0.E7.P11} link pt.kernel.ComponentRelation {.E0.E7.R9}
pt.kernel.ComponentPort {.E0.E7.P11} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E7.E8.P9} link pt.kernel.ComponentRelation {.E0.E7.R8}
pt.kernel.ComponentPort {.E0.E7.E8.P10} link pt.kernel.ComponentRelation {.E0.E7.R9}
pt.kernel.ComponentPort {.E0.E10.P12} link pt.kernel.ComponentRelation {.E0.E10.R11}
pt.kernel.ComponentPort {.E0.E10.P12} link pt.kernel.ComponentRelation {.E0.R10}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.E10.R11}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.E10.R12}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E10.E9.P14} link pt.kernel.ComponentRelation {.E0.E10.R12}
}

######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.7 {Generate a description, then regenerate it} {
    set desc0 [description2TclBlend [$e0 description \
	    [java::field pt.kernel.Nameable PRETTYPRINT]]]
    eval $desc0

    # Note that description2TclBlend uses the names of entities
    # as variables, so what was $e0 in 11.1 is $E0
    set desc1 [description2TclBlend [$E0 description \
	    [java::field pt.kernel.Nameable PRETTYPRINT]]]
    list [expr {"$desc0" != ""}] [expr {"$desc0" == "$desc1"}]
} {1 1}


######################################################################
####
# NOTE:  Uses the setup constructed in 11.1.
test CompositeEntity-11.8 {Test LIST_PRETTYPRINT description} {
    $e0 description [java::field pt.kernel.Nameable LIST_PRETTYPRINT]
} { { pt.kernel.CompositeEntity {.E0} }
    { pt.kernel.ComponentRelation {.E0.R7} }
    { pt.kernel.ComponentRelation {.E0.R10} }
 {  { pt.kernel.CompositeEntity {.E0.E3} }
   { pt.kernel.ComponentPort {.E0.E3.P7} }
    { pt.kernel.ComponentRelation {.E0.E3.R4} }
    { pt.kernel.ComponentRelation {.E0.E3.R5} }
    { pt.kernel.ComponentRelation {.E0.E3.R6} }
 {  { pt.kernel.CompositeEntity {.E0.E3.E4} }
   { pt.kernel.ComponentPort {.E0.E3.E4.P0} }
   { pt.kernel.ComponentPort {.E0.E3.E4.P4} }
    { pt.kernel.ComponentRelation {.E0.E3.E4.R1} }
    { pt.kernel.ComponentRelation {.E0.E3.E4.R2} }
    { pt.kernel.ComponentRelation {.E0.E3.E4.R3} }
 {  { pt.kernel.ComponentEntity {.E0.E3.E4.E1} }
   { pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} }
 }
 {  { pt.kernel.ComponentEntity {.E0.E3.E4.E2} }
   { pt.kernel.ComponentPort {.E0.E3.E4.E2.P2} }
   { pt.kernel.ComponentPort {.E0.E3.E4.E2.P3} }
 }
 }
 {  { pt.kernel.ComponentEntity {.E0.E3.E5} }
   { pt.kernel.ComponentPort {.E0.E3.E5.P5} }
 }
 {  { pt.kernel.ComponentEntity {.E0.E3.E6} }
   { pt.kernel.ComponentPort {.E0.E3.E6.P6} }
 }
 }
 {  { pt.kernel.CompositeEntity {.E0.E7} }
   { pt.kernel.ComponentPort {.E0.E7.P8} }
   { pt.kernel.ComponentPort {.E0.E7.P11} }
    { pt.kernel.ComponentRelation {.E0.E7.R8} }
    { pt.kernel.ComponentRelation {.E0.E7.R9} }
 {  { pt.kernel.ComponentEntity {.E0.E7.E8} }
   { pt.kernel.ComponentPort {.E0.E7.E8.P9} }
   { pt.kernel.ComponentPort {.E0.E7.E8.P10} }
 }
 }
 {  { pt.kernel.CompositeEntity {.E0.E10} }
   { pt.kernel.ComponentPort {.E0.E10.P12} }
   { pt.kernel.ComponentPort {.E0.E10.P13} }
    { pt.kernel.ComponentRelation {.E0.E10.R11} }
    { pt.kernel.ComponentRelation {.E0.E10.R12} }
 {  { pt.kernel.ComponentEntity {.E0.E10.E9} }
   { pt.kernel.ComponentPort {.E0.E10.E9.P14} }
 }
 }
pt.kernel.ComponentPort {.E0.E3.P7} link pt.kernel.ComponentRelation {.E0.E3.R4}
pt.kernel.ComponentPort {.E0.E3.P7} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E3.E4.P0} link pt.kernel.ComponentRelation {.E0.E3.E4.R1}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.R4}
pt.kernel.ComponentPort {.E0.E3.E4.P4} link pt.kernel.ComponentRelation {.E0.E3.R5}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R1}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.E1.P1} link pt.kernel.ComponentRelation {.E0.E3.E4.R3}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P2} link pt.kernel.ComponentRelation {.E0.E3.E4.R3}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P3} link pt.kernel.ComponentRelation {.E0.E3.E4.R2}
pt.kernel.ComponentPort {.E0.E3.E4.E2.P3} link pt.kernel.ComponentRelation {.E0.E3.R6}
pt.kernel.ComponentPort {.E0.E3.E5.P5} link pt.kernel.ComponentRelation {.E0.E3.R5}
pt.kernel.ComponentPort {.E0.E3.E6.P6} link pt.kernel.ComponentRelation {.E0.E3.R6}
pt.kernel.ComponentPort {.E0.E7.P8} link pt.kernel.ComponentRelation {.E0.E7.R8}
pt.kernel.ComponentPort {.E0.E7.P8} link pt.kernel.ComponentRelation {.E0.R10}
pt.kernel.ComponentPort {.E0.E7.P11} link pt.kernel.ComponentRelation {.E0.E7.R9}
pt.kernel.ComponentPort {.E0.E7.P11} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E7.E8.P9} link pt.kernel.ComponentRelation {.E0.E7.R8}
pt.kernel.ComponentPort {.E0.E7.E8.P10} link pt.kernel.ComponentRelation {.E0.E7.R9}
pt.kernel.ComponentPort {.E0.E10.P12} link pt.kernel.ComponentRelation {.E0.E10.R11}
pt.kernel.ComponentPort {.E0.E10.P12} link pt.kernel.ComponentRelation {.E0.R10}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.E10.R11}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.E10.R12}
pt.kernel.ComponentPort {.E0.E10.P13} link pt.kernel.ComponentRelation {.E0.R7}
pt.kernel.ComponentPort {.E0.E10.E9.P14} link pt.kernel.ComponentRelation {.E0.E10.R12}
}

######################################################################
####
# Test connections.
test CompositeEntity-12.1 {Test connect} {
    set e0 [java::new pt.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2]
    enumToNames [$r1 linkedPorts]
} {P1 P2}

######################################################################
####
# Test connections.
test CompositeEntity-12.2 {Test connect} {
    set e0 [java::new pt.kernel.CompositeEntity]
    $e0 setName E0
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p1 [java::new pt.kernel.ComponentPort $e1 P1]
    set p2 [java::new pt.kernel.ComponentPort $e2 P2]
    set r1 [$e0 connect $p1 $p2 R1]
    enumToNames [[$e0 getRelation R1] linkedPorts ]
} {P1 P2}

