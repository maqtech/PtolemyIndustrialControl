# Tests for the SDFDirector class
#
# @Author: Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test SDFDirector-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d1 [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 W.D2 W.E0.D3}

######################################################################
####
#
test SDFDirector-3.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d4 [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.Manager}

######################################################################
####
#
test SDFDirector-4.1 {Test _makeDirectorOf} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e0 D3]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e1 setName E1
    $e1 setManager $manager
    $e1 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {W.E1.D3 W.D4 {W.E0 W.E1}}

######################################################################
####
#
test SDFDirector-5.1 {Test action methods} {
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer]
    $e1 connect [java::field $a1 output] [java::field $a2 input] R1
    set iter [$d3 getAttribute iterations]

    # _testSetToken is defined in $PTII/util/testsuite/testParams.tcl
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]

    $manager run
    list [$a2 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

test SDFDirector-5.2 {Test action methods} {
    # NOTE: Uses the setup above
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e1 D3]
    $e1 setName E0
    $e1 setManager $manager
    $e1 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $e1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer]
    $e1 connect [java::field $a1 output] [java::field $a2 input] R1
    $e1 connect [java::field $a2 output] [java::field $a3 input] R2
    set iter [$d3 getAttribute iterations]
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

test SDFDirector-5.3 {Test action methods} {
    # NOTE: Uses the setup above
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e1 D3]
    $e1 setName E0
    $e1 setManager $manager
    $e1 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFSplit $e1 Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer1]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer2]
    $e1 connect [java::field $a1 output] [java::field $a2 input] R1
    $e1 connect [java::field $a2 output1] [java::field $a3 input] R2
    $e1 connect [java::field $a2 output2] [java::field $a4 input] R3
    set iter [$d3 getAttribute iterations]
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory] [$a4 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(6)
ptolemy.data.IntToken(8)
ptolemy.data.IntToken(10)
} {ptolemy.data.IntToken(1)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(5)
ptolemy.data.IntToken(7)
ptolemy.data.IntToken(9)
ptolemy.data.IntToken(11)
}}

test SDFDirector-5.4 {Test action methods} {
    # NOTE: Uses the setup above
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e1 D3]
    $e1 setName E0
    $e1 setManager $manager
    $e1 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFSplit $e1 Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFJoin $e1 Comm]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer1]
    $e1 connect [java::field $a1 output] [java::field $a2 input] R1
    $e1 connect [java::field $a2 output1] [java::field $a3 input1] R2a
    $e1 connect [java::field $a2 output2] [java::field $a3 input2] R2d
    $e1 connect [java::field $a3 output] [java::field $a4 input] R3
    set iter [$d3 getAttribute iterations]
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a4 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
ptolemy.data.IntToken(6)
ptolemy.data.IntToken(7)
ptolemy.data.IntToken(8)
ptolemy.data.IntToken(9)
ptolemy.data.IntToken(10)
ptolemy.data.IntToken(11)
}}

######################################################################
####
#
test SDFDirector-6.1 {Test wormhole activation} {
    # NOTE: Uses the setup above
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e1 D3]
    $e1 setName E0
    $e1 setManager $manager
    $e1 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $e1 Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer]
    $e1 connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $e1 connect $p2 [java::field $a3 input] R4

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#$d3 addDebugListener $debugger
#set s3 [$d3 getScheduler]
#$s3 addDebugListener $debugger
#$d5 addDebugListener $debugger
#set s5 [$d5 getScheduler]
#$s5 addDebugListener $debugger

    set iter [$d3 getAttribute iterations]
    _testSetToken $iter  [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory] 
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

test SDFDirector-6.2 {Test transparent activation} {
    # NOTE: Uses the setup above
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e1 D3]
    $e1 setName E0
    $e1 setManager $manager
    $e1 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $e1 Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $e1 Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $e1 Consumer]
    $e1 connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $e1 connect $p2 [java::field $a3 input] R4

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#$d3 addDebugListener $debugger
#set s3 [$d3 getScheduler]
#$s3 addDebugListener $debugger

    set iter [$d3 getAttribute iterations]
    _testSetToken $iter  [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory] 
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}


