# Tests for the SDFScheduler class
#
# @Author: Steve Neuendorffer
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
test SDFScheduler-2.1 {Constructor tests} {
    set s1 [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set s2 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    set s3 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s3 setName S3
    list [$s1 getFullName] [$s2 getFullName] [$s3 getFullName] 
} {.Scheduler W.Scheduler W.S3}

######################################################################
####
#
test SDFScheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.domains.sdf.kernel.SDFScheduler \
            [$s2 clone $w]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {W.Scheduler W.S3}

######################################################################
####
#
test SDFScheduler-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set d0 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D1]
    $d0 setScheduler $s2
    set d1 [$s2 getContainer]
    list [$d0 getFullName] [$d1 getFullName] [$s2 getFullName]
} {W.D1 W.D1 W.D1.Scheduler}

######################################################################
####
#

test SDFScheduler-4.2 {Test setValid and isValid} {
    # NOTE: Uses the setup above
    $s1 setValid true
    set e0 [$s1 isValid]
    $s1 setValid false
    set e1 [$s1 isValid]
    list $e0 $e1
} {1 0}

######################################################################
####
#
# Tests 5.* test some simple scheduling tasks without hierarchy
test SDFScheduler-5.1 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $scheduler setValid false

    _testEnums schedule $scheduler
    
} {{Ramp Consumer}}

######################################################################
####
#
test SDFScheduler-5.2 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2
    $scheduler setValid false

    _testEnums schedule $scheduler
    
} {{Ramp Delay Consumer}}

######################################################################
####
#
test SDFScheduler-5.3 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer1]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer2]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output1] [java::field $a3 input] R2
    $toplevel connect [java::field $a2 output2] [java::field $a4 input] R3
    $scheduler setValid false

    _testEnums schedule $scheduler
    
} {{Ramp Ramp Dist Consumer1 Consumer2}}

######################################################################
####
#
test SDFScheduler-5.4 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFSplit $toplevel Dist]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFJoin $toplevel Comm]
    set a4 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer1]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output1] [java::field $a3 input1] R2a
    $toplevel connect [java::field $a2 output2] [java::field $a3 input2] R2d
    $toplevel connect [java::field $a3 output] [java::field $a4 input] R3
    $scheduler setValid false

    _testEnums schedule $scheduler
    
} {{Ramp Ramp Dist Comm Consumer1 Consumer1}}

######################################################################
####
#
# Tests 6.* test multirate scheduling without hierarchy.
test SDFScheduler-6.1 {Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $toplevel Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R4

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {{{Ramp Delay Consumer}}}

test SDFScheduler-6.2 {Multirate Scheduling tests} {

    $a1 setTokenProductionRate [java::field $a1 output] 2
    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Delay Delay Consumer Consumer}}}

test SDFScheduler-6.3 {Multirate Scheduling tests} {

    $a2 setTokenProductionRate [java::field $a2 output] 2
    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp Delay Consumer Consumer}}}

test SDFScheduler-6.4 {Multirate Scheduling tests} {

    $a2 setTokenConsumptionRate [java::field $a2 input] 2
    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Delay Consumer}}}

test SDFScheduler-6.5 {Multirate Scheduling tests} {

    $a3 setTokenConsumptionRate [java::field $a3 input] 2
    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Ramp Delay Delay Consumer}}}

######################################################################
####
#
# Tests 7.* test multirate scheduling with hierarchy
test SDFScheduler-7.1 {Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    $toplevel connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $toplevel connect $p2 [java::field $a3 input] R4

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.2 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1 $sched2
} {{{Ramp Cont Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.3 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.

    $a2 setTokenProductionRate [java::field $a2 output] 2
 
    $scheduler setValid false
    $s5 setValid false

    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]

    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp Cont Consumer Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.4 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Consumer}} Delay}

######################################################################
####
#
test SDFScheduler-7.5 {Multirate and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1 $sched2
} {{{Ramp Ramp Cont Cont Consumer}} Delay}

######################################################################
####
#
# Tests 8.* test multiport scheduling without hierarchy.
test SDFScheduler-8.1 {input Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    set port [java::field $a3 input] 
    $port setMultiport true

    $toplevel connect [java::field $a1 output] [java::field $a3 input] R1
    $toplevel connect [java::field $a2 output] [java::field $a3 input] R2

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.2 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp2 Ramp1 Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.3 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a2 setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp1 Ramp2 Ramp1 Consumer Consumer}}} {Scheduler needs to keep track of 
fulfilled ports on a channel-by-channel basis}

test SDFScheduler-8.4 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
    $a2 setTokenProductionRate [java::field $a2 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1
} {{{Ramp1 Ramp2 Ramp1 Consumer}}} {Scheduler needs to keep track of 
fulfilled ports on a channel-by-channel basis}

test SDFScheduler-8.5 {input Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
    $a2 setTokenProductionRate [java::field $a2 output] 2
    $a1 setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    $a2 setTokenProductionRate [java::field $a2 output] 1
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp2 Ramp1 Consumer}}}

test SDFScheduler-8.6 {input Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]
    set port [java::field $a3 input] 
    $port setMultiport true

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {Consumer}

test SDFScheduler-8.11 {output Multiport, Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer2]
    set port [java::field $a1 output] 
    $port setMultiport true

    $toplevel connect [java::field $a1 output] [java::field $a2 input] R1
    $toplevel connect [java::field $a1 output] [java::field $a3 input] R2

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-8.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer1 Consumer2}}}

test SDFScheduler-8.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Consumer2 Consumer1 Consumer2}}}

test SDFScheduler-8.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-8.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-8.16 {output Multiport with no connections} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set port [java::field $a3 output] 
    $port setMultiport true

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {Ramp}


######################################################################
####
#
# Tests 9.* test multiport, multirate scheduling with hierarchy
test SDFScheduler-9.1 {Input Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $c1 Consumer]
    $toplevel connect [java::field $a1 output] $p1 R1
    $toplevel connect [java::field $a2 output] $p1 R2
    $c1 connect $p1 [java::field $a3 input] R3

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.2 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.3 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a2 setTokenProductionRate [java::field $a2 output] 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp1 Ramp2 Ramp1 Cont Cont}} Consumer} {Scheduler needs to keep track of 
fulfilled ports on a channel-by-channel basis}

test SDFScheduler-9.4 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
    $a2 setTokenProductionRate [java::field $a2 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    $a2 setTokenProductionRate [java::field $a2 output] 1
    list $sched1 $sched2
} {{{Ramp1 Ramp2 Ramp1 Cont}} Consumer} {Scheduler needs to keep track of 
fulfilled ports on a channel-by-channel basis}

test SDFScheduler-9.5 {Input Multiport, Multirate, and Hierarchy Scheduling tests} {
    # uses previous setup.
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
    $a2 setTokenProductionRate [java::field $a2 output] 2
    $a1 setTokenProductionRate [java::field $a1 output] 2

    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    $a2 setTokenProductionRate [java::field $a2 output] 1
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1 $sched2
} {{{Ramp2 Ramp1 Cont}} Consumer}

test SDFScheduler-9.11 {Output Multirate and Hierarchy Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer2]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $toplevel Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setOutput 1
    $p1 setMultiport true
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w d5]
    $c1 setDirector $d5
    set s5 [$d5 getScheduler]
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $c1 Ramp]
    $c1 connect [java::field $a1 output] $p1 R1
    $toplevel connect $p1 [java::field $a2 input] R2
    $toplevel connect $p1 [java::field $a3 input] R3

    $scheduler setValid false
    $s5 setValid false

    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2}} Ramp}

test SDFScheduler-9.12 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2 Consumer1 Consumer2}} Ramp}

test SDFScheduler-9.13 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1 $sched2
} {{{Cont Cont Consumer2 Consumer1 Consumer2}} Ramp}

test SDFScheduler-9.14 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2 Consumer2}} Ramp}

test SDFScheduler-9.15 {output Multiport, Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1 $sched2
} {{{Cont Consumer1 Consumer2}} Ramp}

######################################################################
####
#
# Tests 10.* test multiport scheduling without hierarchy.
test SDFScheduler-10.1 {input Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp1]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp2]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a3 input] R1]
    [java::field $a2 output] link $r1

    $scheduler setValid false
    catch {[$scheduler schedule]} e1
    list $e1
} {{ptolemy.actor.sched.NotSchedulableException: W.Toplevel.Ramp2.output and W.Toplevel.Ramp1.output: Two output ports are connected on the same relation. This is not legal in SDF.}}

test SDFScheduler-10.11 {output Broadcast Multirate Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector $w Director]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFRamp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer1]
    set a3 [java::new ptolemy.domains.sdf.kernel.test.SDFConsumer $toplevel Consumer2]

    set r1 [$toplevel connect [java::field $a1 output] [java::field $a2 input] R1]
    [java::field $a3 input] link $r1

    $scheduler setValid false

    set sched1 [_testEnums schedule $scheduler]
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

test SDFScheduler-10.12 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer1 Consumer2}}}

test SDFScheduler-10.13 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Ramp Consumer2 Consumer1 Consumer2}}}

test SDFScheduler-10.14 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2 Consumer2}}}

test SDFScheduler-10.15 {output Broadcast Multirate Scheduling tests} {
    # uses previous setup.
    $a1 setTokenProductionRate [java::field $a1 output] 2
    $a2 setTokenConsumptionRate [java::field $a2 input] 2
    $a3 setTokenConsumptionRate [java::field $a3 input] 2
 
    $scheduler setValid false
    $s5 setValid false
    set sched1 [_testEnums schedule $scheduler]
    set sched2 [_testEnums schedule $s5]
    $a1 setTokenProductionRate [java::field $a1 output] 1
    $a2 setTokenConsumptionRate [java::field $a2 input] 1
    $a3 setTokenConsumptionRate [java::field $a3 input] 1
    list $sched1
} {{{Ramp Consumer1 Consumer2}}}

