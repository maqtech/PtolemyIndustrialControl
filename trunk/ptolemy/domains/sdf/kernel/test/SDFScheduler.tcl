# Tests for the SDFScheduler class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2001 The Regents of the University of California.
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
proc _getSchedule {toplevel scheduler} {
    [$toplevel getManager] initialize
    [$toplevel getManager] wrapup
    _testEnums schedule $scheduler
}
######################################################################
####
#
test SDFScheduler-2.1 {Constructor tests} {
    set s1 [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set s2 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    set s3 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s3 setName S3
    list [$s1 getFullName] [$s2 getFullName] [$s3 getFullName]
} {.Scheduler .Scheduler .S3}

######################################################################
####
#
test SDFScheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.domains.sdf.kernel.SDFScheduler \
            [$s2 clone $w]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {.Scheduler .S3}

######################################################################
####
#
test SDFScheduler-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d0 [java::new ptolemy.domains.sdf.kernel.SDFDirector $e0 D1]
    set s4 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s4 setName "TestScheduler"
    $d0 setScheduler $s4
    set d1 [$s4 getContainer]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]

    list [$d0 getFullName] [$d1 getFullName] [$s4 getFullName]
} {.E0.D1 .E0.D1 .E0.D1.TestScheduler}

test SDFScheduler-4.2 {Test setValid and isValid} {
    # NOTE: Uses the setup above
    $s1 setValid true
    set result0 [$s1 isValid]
    $s1 setValid false
    set result1 [$s1 isValid]
    list $result0 $result1
} {1 0}

test SDFScheduler-4.3 {Test TokenConsumptionRate methods} {
    # NOTE: Uses the setup above
    $p1 setInput 1
    set result1 [$s2 getTokenConsumptionRate $p1]
    $s2 setTokenConsumptionRate $p1 2
    set result2 [$s2 getTokenConsumptionRate $p1]
    $p1 setInput 0
    set result3 [$s2 getTokenConsumptionRate $p1]
    list $result1 $result2 $result3
} {1 2 0}

test SDFScheduler-4.4 {Test TokenProductionRate methods} {
    # NOTE: Uses the setup above
    $p1 setOutput 1
    set result1 [$s2 getTokenProductionRate $p1]
    $s2 setTokenProductionRate $p1 2
    set result2 [$s2 getTokenProductionRate $p1]
    $p1 setOutput 0
    set result3 [$s2 getTokenProductionRate $p1]
    list $result1 $result2 $result3
} {1 2 0}

test SDFScheduler-4.5 {Test TokenInitProduction methods} {
    # NOTE: Uses the setup above
    $p1 setOutput 1
    set result1 [$s2 getTokenInitProduction $p1]
    $s2 setTokenInitProduction $p1 2
    set result2 [$s2 getTokenInitProduction $p1]
    $p1 setOutput 0
    set result3 [$s2 getTokenInitProduction $p1]
    list $result1 $result2 $result3
} {0 2 0}


######################################################################
####
#
# Tests 5.* test some simple scheduling tasks without hierarchy
test SDFScheduler-5.1 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $scheduler setValid false

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#$director addDebugListener $debugger
#$scheduler addDebugListener $debugger

    _getSchedule $toplevel $scheduler
} {{Ramp Consumer}}

######################################################################
####
#
test SDFScheduler-5.2 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2
    $scheduler setValid false

    _getSchedule $toplevel $scheduler

} {{Ramp Delay Consumer}}

######################################################################
####
#
test SDFScheduler-5.3 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output1] [java::field $a3 input] R2
    $toplevel connect [java::field $a2 output2] [java::field $a4 input] R3
    $scheduler setValid false

    _getSchedule $toplevel $scheduler

} {{Ramp Ramp Dist Consumer1 Consumer2}}

######################################################################
####
#
test SDFScheduler-5.4 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestJoin $toplevel Comm]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output1] [java::field $a3 input1] R2a
    $toplevel connect [java::field $a2 output2] [java::field $a3 input2] R2d
    $toplevel connect [java::field $a3 output] [java::field $a4 input] R3
    $scheduler setValid false

    _getSchedule $toplevel $scheduler

} {{Ramp Ramp Dist Comm Consumer1 Consumer1}}

######################################################################
####
#
# Tests 6.* test multirate scheduling without hierarchy.
test SDFScheduler-6.1 {Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R4

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Delay Consumer}}}

test SDFScheduler-6.2 {Multirate Scheduling tests} {

    [java::field $a1 output] setTokenProductionRate 2
    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Delay Delay Consumer Consumer}}}

test SDFScheduler-6.3 {Multirate Scheduling tests} {

    [java::field $a2 output] setTokenProductionRate 2
    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Delay Consumer Consumer}}}

test SDFScheduler-6.4 {Multirate Scheduling tests} {

    [java::field $a2 input] setTokenConsumptionRate 2
    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Delay Consumer}}}

test SDFScheduler-6.5 {Multirate Scheduling tests} {

    [java::field $a3 input] setTokenConsumptionRate 2
    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Delay Delay Consumer}}}

######################################################################
####
#
# Tests 7.* test multirate scheduling with hierarchy
test SDFScheduler-7.1 {Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $toplevel connect $p2 [java::field $a3 input] R4

    $scheduler setValid false
    $s5 setValid false

 #   set debuglistener [java::new ptolemy.kernel.util.StreamListener]
 #   $scheduler addDebugListener $debuglistener
 #   $director addDebugListener $debuglistener
 #   $manager addDebugListener $debuglistener

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.2 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Ramp Cont Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.3 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.

    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]

    [java::field $a2 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Ramp Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.4 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.5 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Cont Consumer}} Delay}

######################################################################
####
#
# Tests 8.* test multiport scheduling without hierarchy.
test SDFScheduler-8.1 {input Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $toplevel connect [java::field $a1 output] [java::field $a3 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.2 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-8.3 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer Consumer}}}

test SDFScheduler-8.4 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-8.5 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.6 {input Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {Consumer}

test SDFScheduler-8.7 {input Multiport with no connections - disconnected graph} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set port [java::field $a3 input]
    $port setMultiport true

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set port [java::field $a3 input]
    $port setMultiport true

    $scheduler setValid false

    set sched1 {}
    catch {set sched1 [_getSchedule $toplevel $scheduler]} s1
    list $sched1 $s1
} {{} {ptolemy.actor.sched.NotSchedulableException: SDF scheduler found disconnected actors!
Reached Actors:
.Toplevel.Consumer1
Unreached Actors:
.Toplevel.Consumer2 }}

test SDFScheduler-8.11 {output Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set port [java::field $a1 output]
    $port setMultiport true

    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a1 output] [java::field $a3 input] R2

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-8.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-8.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-8.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-8.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-8.16 {output Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set port [java::field $a3 output]
    $port setMultiport true

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {Ramp}


######################################################################
####
#
# Tests 9.* test multiport, multirate scheduling with hierarchy
test SDFScheduler-9.1 {Input Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $toplevel connect [java::field $a1 output] $p1 R1
    $toplevel connect [java::field $a2 output] $p1 R2
    $c1 connect $p1 [java::field $a3 input] R3
    set r3 [$c1 getRelation R3]
    [java::cast ptolemy.actor.IORelation $r3] setWidth 2

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.2 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Cont}} Consumer}

test SDFScheduler-9.3 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Ramp1 Cont Cont}} Consumer}

test SDFScheduler-9.4 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Ramp1 Cont}} Consumer}

test SDFScheduler-9.5 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.11 {Output Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setOutput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $c1 Ramp]
    set port [java::field $a1 output]
    $port setMultiport true

    $c1 connect [java::field $a1 output] $p1 R1
    set r1 [$c1 getRelation R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 2
    $toplevel connect $p1 [java::field $a2 input] R2
    $toplevel connect $p1 [java::field $a3 input] R3

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2}} Ramp}

test SDFScheduler-9.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer1 Consumer2 Consumer2}} Ramp}

test SDFScheduler-9.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Cont Cont Consumer1 Consumer2 Consumer2}} Ramp}

test SDFScheduler-9.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2 Consumer2}} Ramp}

test SDFScheduler-9.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2}} Ramp}

######################################################################
####
#
# Tests 10.* test multiport scheduling without hierarchy.
test SDFScheduler-10.1 {input Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a3 input] R1]
    [java::field $a2 output] link $r1

    $scheduler setValid false
    catch {[$scheduler schedule]} e1
    list $e1
} {{ptolemy.actor.sched.NotSchedulableException: .Toplevel.Ramp1.output and .Toplevel.Ramp2.output:
Output ports drive the same relation. This is not legal in SDF.}}

test SDFScheduler-10.11 {output Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]
    [java::field $a3 input] link $r1

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-10.12 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-10.13 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-10.14 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-10.15 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}


######################################################################
####
#
# Tests 11.* test multirate scheduling with transparent hierarchy
test SDFScheduler-11.1 {Multirate and transparent hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $toplevel connect $p2 [java::field $a3 input] R4

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Delay Consumer}}}

######################################################################
####
#
test SDFScheduler-11.2 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Delay Delay Consumer Consumer}}}

######################################################################
####
#
test SDFScheduler-11.3 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.

    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Delay Consumer Consumer}}}

######################################################################
####
#
test SDFScheduler-11.4 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Delay Consumer}}}

######################################################################
####
#
test SDFScheduler-11.5 {Multirate and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Delay Delay Consumer}}}

######################################################################
####
#
# Tests 12.* test multiport, multirate scheduling with transparent hierarchy
test SDFScheduler-12.1 {Input Multirate and transparent hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $toplevel connect [java::field $a1 output] $p1 R1
    $toplevel connect [java::field $a2 output] $p1 R2
    $c1 connect $p1 [java::field $a3 input] R3

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-12.2 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp2 Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-12.3 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer Consumer}}}

test SDFScheduler-12.4 {Input Multiport, Multirate, and transparent hierarchy Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp1 Consumer}}}

test SDFScheduler-12.5 {Input Multiport, Multirate, and transparent hierarch Scheduling tests} {
    # uses previous setup.
    [java::field $a3 input] setTokenConsumptionRate 2
    [java::field $a2 output] setTokenProductionRate 2
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a3 input] setTokenConsumptionRate 1
    [java::field $a2 output] setTokenProductionRate 1
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-12.11 {Output Multirate and hierarch Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setOutput 1
    $p1 setMultiport true
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $c1 Ramp]
    set port [java::field $a1 output]
    $port setMultiport true

    $c1 connect [java::field $a1 output] $p1 R1
    set r1 [$c1 getRelation R1]
    [java::cast ptolemy.actor.IORelation $r1] setWidth 2
    $toplevel connect $p1 [java::field $a2 input] R2
    $toplevel connect $p1 [java::field $a3 input] R3

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-12.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a1 output] setTokenProductionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-12.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-12.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-12.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    [java::field $a1 output] setTokenProductionRate 2
    [java::field $a2 input] setTokenConsumptionRate 2
    [java::field $a3 input] setTokenConsumptionRate 2

    $scheduler setValid false
    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a1 output] setTokenProductionRate 1
    [java::field $a2 input] setTokenConsumptionRate 1
    [java::field $a3 input] setTokenConsumptionRate 1
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

######################################################################
####
#
# Tests 13.* test error cases.
test SDFScheduler-13.1 {connected graph, disconnected relation} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    set r1 [java::new ptolemy.actor.TypedIORelation $toplevel R1]
    [java::field $a3 input] link $r1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2

    $scheduler setValid false

    #set debuglistener [java::new ptolemy.kernel.util.StreamListener]
    #$scheduler addDebugListener $debuglistener

    set err1 ""
    set sched1 ""
    catch {set sched1 [_getSchedule $toplevel $scheduler]} err1
    list $sched1 $err1
} {{} {ptolemy.actor.sched.NotSchedulableException: Actors remain that cannot be scheduled!
Scheduled actors:
.Toplevel.Ramp2
Unscheduled actors:
.Toplevel.Consumer
}}

test SDFScheduler-13.2 {Output External port connected } {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $c1 Consumer]
    set port [java::field $a3 input]
    $port setMultiport true

    $toplevel connect [java::field $a1 output] $p1 R1
    $toplevel connect [java::field $a2 output] $p1 R2
    $c1 connect $p1 [java::field $a3 input] R3
    set r3 [$c1 getRelation R3]
    [java::cast ptolemy.actor.IORelation $r3] setWidth 2

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-13.3 {_debugging code coverage} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $toplevel connect $p2 [java::field $a3 input] R4

    $scheduler setValid false
    $s5 setValid false

#    set debuglistener [java::new ptolemy.kernel.util.StreamListener]
#    $scheduler addDebugListener $debuglistener
#    $director addDebugListener $debuglistener

    set sched1 [_getSchedule $toplevel $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp Cont Consumer}} Delay}

test SDFScheduler-13.5 {buffersize annotation} {
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Toplevel" class="ptolemy.actor.TypedCompositeActor">
    <property name="Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="Ramp" class="ptolemy.domains.sdf.kernel.test.SDFTestRamp">
        <port name="output" class="ptolemy.domains.sdf.kernel.SDFIOPort">
            <property name="output"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
        </port>
    </entity>
    <entity name="Cont" class="ptolemy.actor.TypedCompositeActor">
        <property name="d5" class="ptolemy.domains.sdf.kernel.SDFDirector">
            <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
            </property>
            <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
            </property>
        </property>
        <port name="p1" class="ptolemy.domains.sdf.kernel.SDFIOPort">
            <property name="input"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
        <port name="p2" class="ptolemy.domains.sdf.kernel.SDFIOPort">
            <property name="output"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
        </port>
        <entity name="Delay" class="ptolemy.domains.sdf.kernel.test.SDFTestDelay">
            <port name="input" class="ptolemy.domains.sdf.kernel.SDFIOPort">
                <property name="input"/>
                <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
                </property>
                <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
                </property>
            </port>
            <port name="output" class="ptolemy.domains.sdf.kernel.SDFIOPort">
                <property name="output"/>
                <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="1">
                </property>
            </port>
        </entity>
        <relation name="R2" class="ptolemy.actor.TypedIORelation">
            <property name="bufferSize" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </relation>
        <relation name="R3" class="ptolemy.actor.TypedIORelation">
            <property name="bufferSize" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </relation>
        <link port="p1" relation="R2"/>
        <link port="p2" relation="R3"/>
        <link port="Delay.input" relation="R2"/>
        <link port="Delay.output" relation="R3"/>
    </entity>
    <entity name="Consumer" class="ptolemy.domains.sdf.kernel.test.SDFTestConsumer">
        <port name="input" class="ptolemy.domains.sdf.kernel.SDFIOPort">
            <property name="input"/>
            <property name="tokenConsumptionRate" class="ptolemy.data.expr.Parameter" value="1">
            </property>
            <property name="tokenInitProduction" class="ptolemy.data.expr.Parameter" value="0">
            </property>
            <property name="tokenProductionRate" class="ptolemy.data.expr.Parameter" value="0">
            </property>
        </port>
    </entity>
    <relation name="R1" class="ptolemy.actor.TypedIORelation">
        <property name="bufferSize" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="R4" class="ptolemy.actor.TypedIORelation">
        <property name="bufferSize" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Ramp.output" relation="R1"/>
    <link port="Cont.p1" relation="R1"/>
    <link port="Cont.p2" relation="R4"/>
    <link port="Consumer.input" relation="R4"/>
</entity>
}




######################################################################
####
#
# Tests 14.* test 0-rate ports
# Test the case where a zero-rate input port is connected to
# a chain of three actors connected in sequence. None of
# these three actors should fire.
test SDFScheduler-14.1 {Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay2]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay3]
    set a5 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay4]
    set a6 [java::new ptolemy.domains.sdf.kernel.test.SDFTestDelay $toplevel Delay5]
    set a7 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2
    set r3 [$toplevel connect [java::field $a3 output] [java::field $a4 input] R3]
    $toplevel connect [java::field $a4 output] [java::field $a5 input] R4
    $toplevel connect [java::field $a5 output] [java::field $a6 input] R5
    $toplevel connect [java::field $a6 output] [java::field $a7 input] R6
    
    [java::field $a4 output] setTokenProductionRate 0


    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Ramp Delay1 Delay2 Delay3}}}

# Test zero-rate ports.
# Test the case where a zero-rate output port is connected to
# a chain of three sequentially connected actors. None of
# these three actors should fire.
test SDFScheduler-14.2 {Multirate Scheduling tests} {

    [java::field $a4 input] setTokenConsumptionRate 0
    [java::field $a4 output] setTokenProductionRate 1

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {{{Delay3 Delay4 Delay5 Consumer}}}

# Test zero-rate ports.
# Test the case where a zero-rate input port is connected to
# a chain of three actors connected in sequence
# and where a zero-rate output port is connected to
# a chain of three sequentially connected actors.
# Only the actor with the zero-rate ports should fire.
test SDFScheduler-14.3 {Multirate Scheduling tests} {

    [java::field $a4 input] setTokenConsumptionRate 0
    [java::field $a4 output] setTokenProductionRate 0

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    list $sched1
} {Delay3}

# Test zero-rate ports.
# Test the case where an zero-rate input port is connected to more
# than one actor. None of the connected actors should fire.
test SDFScheduler-14.4 {Multirate Scheduling tests} {

    [java::field $a4 input] setTokenConsumptionRate 0
    [java::field $a4 output] setTokenProductionRate 1
    [java::field $a4 input] setMultiport true
    set b1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $toplevel Ramp2]
    $toplevel connect [java::field $b1 output] [java::field $a4 input] R7

    $scheduler setValid false

    set sched1 [_getSchedule $toplevel $scheduler]
    [java::field $a4 input] setMultiport false
    list $sched1
} {{{Delay3 Delay4 Delay5 Consumer}}}

