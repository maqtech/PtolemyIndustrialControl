# Tests for the Event class
#
# @Author: John S. Davis II
#
# @Version: @(#)TimedQueueReceiver.tcl	1.5	11/17/98
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
test TimedQueueReceiver-2.1 {Check for correct IOPort container in new receiver} {
    set iop [java::new ptolemy.actor.IOPort]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    list [expr { $iop == [$tqr getContainer] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-3.1 {Check that hasToken() works for empty queue} {
    set iop [java::new ptolemy.actor.IOPort]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    list [expr { 0 == [$tqr hasToken] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-3.2 {Check that hasToken() works for non-empty queue} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 
    $tqr put $t2 
    list [expr { 1 == [$tqr hasToken] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.1 {Check that hasRoom() works for finite capacity, empty queue} {
    set iop [java::new ptolemy.actor.IOPort]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    $tqr setCapacity 10
    list [expr { 1 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.2 {Check that hasRoom() works for full queue} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    $tqr setCapacity 2
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1
    $tqr put $t2
    list [expr { 0 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-4.3 {Check that hasRoom() works for infinite capacity queue} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]
    $tqr put $t1
    $tqr put $t2
    $tqr put $t3
    list [expr { 1 == [$tqr hasRoom] } ]
} {1}

######################################################################
####
#
test TimedQueueReceiver-5.1 {get(), put(), check _rcvrTime and _lastTime} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.odf.kernel.ODFDirector $wspc "director"]
    $topLevel setDirector $dir
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.odf.kernel.test.ODFPutToken $topLevel "actorA" 3] 
    set actorB [java::new ptolemy.domains.odf.kernel.test.ODFGetToken $topLevel "actorB" 3] 

    set token0 [java::new ptolemy.data.Token]
    set token1 [java::new ptolemy.data.Token]
    set token2 [java::new ptolemy.data.Token]
    $actorA setToken $token0 5.5 0
    $actorA setToken $token1 15.2 1
    $actorA setToken $token2 17.2 2
    set portA [$actorA getPort "output"]
    set portB [$actorB getPort "input"]
    set rel [$topLevel connect $portB $portA "rel"]

    $manager run

    set outToken0 [$actorB getToken 0] 
    set threadTime0 [$actorB getThreadTime 0] 
    set rcvrTime0 [$actorB getRcvrTime 0] 
    set outToken1 [$actorB getToken 1] 
    set threadTime1 [$actorB getThreadTime 1] 
    set rcvrTime1 [$actorB getRcvrTime 1] 
    set outToken2 [$actorB getToken 2] 
    set threadTime2 [$actorB getThreadTime 2] 
    set rcvrTime2 [$actorB getRcvrTime 2] 
    set lastTime [$actorB getLastTime]

    if { $outToken0 == $token0 && $outToken1 == $token1 && $outToken2 == $token2 } {
	set tok0 1
	set tok1 1
	set tok2 1
    }   

    if { $rcvrTime0 == 5.5 && $rcvrTime1 == 15.2 && $rcvrTime2 == 17.2 } {
	set rcvrTime0 1
	set rcvrTime1 1
	set rcvrTime2 1
    }

    if { $threadTime0 == 5.5 && $threadTime1 == 15.2 && $threadTime2 == 17.2 } {
	set threadTime0 1
	set threadTime1 1
	set threadTime2 1
    }

    list $tok0 $tok1 $tok2 $rcvrTime0 $rcvrTime1 $rcvrTime2 $threadTime0 $threadTime1 $threadTime2 $lastTime
} {1 1 1 1 1 1 1 1 1 17.2}


######################################################################
####
#
test TimedQueueReceiver-5.1 {Put delayed event into empty queue; check rcvrTime and lastTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t [java::new ptolemy.data.Token]
    $tqr put $t 5.0
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {5.0 5.0}

######################################################################
####
#
test TimedQueueReceiver-5.2 {Put delayed event into non-empty queue; check rcvrTime and lastTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 
    $tqr put $t2 5.0
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {0.0 5.0}

######################################################################
####
#
test TimedQueueReceiver-5.3 {Put current time event into empty queue; check rcvrTime and lastTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {0.0 0.0}

######################################################################
####
#
test TimedQueueReceiver-5.4 {Put current time event into non-empty queue; check rcvrTime and lastTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 12.5
    $tqr put $t2 
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {12.5 12.5}

######################################################################
####
#
test TimedQueueReceiver-5.5 {Single put; check rcvrTime and lastTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    $tqr put $t1 12.5
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {12.5 12.5}

######################################################################
####
#
test TimedQueueReceiver-6.1 {A single get followed by a single put} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    $tqr put $t1 0.5
    set t2 [$tqr get]
    list [expr { $t1 == $t2 } ] 
} {1}

######################################################################
####
#
test TimedQueueReceiver-6.2 {Three gets followed by three puts} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]
    $tqr put $t1 0.5
    $tqr put $t2 5.0
    $tqr put $t3 12.5
    set t4 [$tqr get]
    set t5 [$tqr get]
    set t6 [$tqr get]
    list [expr { $t1 == $t4 } ] [expr { $t2 == $t5 } ] [expr { $t3 == $t6 } ] 
} {1 1 1}

######################################################################
####
#
test TimedQueueReceiver-6.3 {put, put, get; check rcvrTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    $tqr put $t1 0.5
    $tqr put $t2 5.0
    set t3 [$tqr get]
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {5.0 5.0}

######################################################################
####
#
test TimedQueueReceiver-6.4 {put, put, put, get; check rcvrTime} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]
    $tqr put $t1 0.5
    $tqr put $t2 5.0
    $tqr put $t3 6.7
    set t4 [$tqr get]
    list [$tqr getRcvrTime] [$tqr getLastTime]
} {5.0 6.7}

######################################################################
####
#
#test TimedQueueReceiver-7.1 {Check getSize} {
#    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
#    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
#    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
#    set t1 [java::new ptolemy.data.Token]
#    set t2 [java::new ptolemy.data.Token]
#    set t3 [java::new ptolemy.data.Token]
#    $tqr put $t1
#    set size1 [$tqr getSize]
#    $tqr put $t2 5.0
#    set size2 [$tqr getSize]
#    $tqr put $t3 
#    set size3 [$tqr getSize]
#    list $size1 $size2 $size3
#} {1 2 3}

######################################################################
####
#
test TimedQueueReceiver-8.1 {Check for exception with put() given full queue} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t [java::new ptolemy.data.Token]
    $tqr setCapacity 0
    # set msg [$tqr put $t] 
    catch {$tqr put $t} msg 
    list $msg
} {{ptolemy.actor.NoRoomException: ..port: Queue is at capacity. Cannot insert token.}}

######################################################################
####
#
test TimedQueueReceiver-8.2 {Check for exception with get() given empty queue} {
    set actor [java::new ptolemy.domains.odf.kernel.ODFActor]
    set iop [java::new ptolemy.domains.odf.kernel.ODFIOPort $actor "port"]
    set tqr [java::new ptolemy.domains.odf.kernel.TimedQueueReceiver $iop]
    set t [java::new ptolemy.data.Token]
    catch {$tqr get} msg 
    list $msg
} {{java.util.NoSuchElementException: The FIFOQueue is empty!}}















