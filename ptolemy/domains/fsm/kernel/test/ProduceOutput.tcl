# Tests for the ProduceOutput class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ProduceOutput-1.1 {test creating a ProduceOutput action} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set act0 [java::new ptolemy.domains.fsm.kernel.ProduceOutput $t0 act0]
    set v0 [java::field $act0 channel]
    set v1 [java::cast ptolemy.data.expr.Variable [$t0 getAttribute _act0]]
    list [$act0 getFullName] [$v0 getFullName] [$v1 getFullName]
} {.e0.fsm.t0.act0 .e0.fsm.t0.act0.channel .e0.fsm.t0._act0}

test ProduceOutput-1.2 {container must be a transition or null} {
    $act0 setContainer [java::null]
    set re0 [$act0 getFullName]
    set re1 [$v1 getFullName]
    set r1 [java::new ptolemy.actor.TypedIORelation]
    $r1 setName r1
    catch {$act0 setContainer $r1} msg
    list $re0 $re1 $msg
} {.act0 ._act0 {ptolemy.kernel.util.IllegalActionException: .r1 and .act0:
Action can only be contained by instances of Transition.}}

test ProduceOutput-1.3 {test clone} {
    $act0 setContainer $t0
    set w0 [java::new ptolemy.kernel.util.Workspace]
    set t1 [java::cast ptolemy.domains.fsm.kernel.Transition \
            [$t0 clone $w0]]
    set act0clone [java::cast ptolemy.domains.fsm.kernel.BroadcastOutput \
            [$t1 getAttribute act0]]
    set v0 [java::field $act0clone expression]
    $v0 setExpression "1 + 1"
    set v1 [java::cast ptolemy.data.expr.Variable [$t1 getAttribute _act0]]
    list [$v1 getExpression]
} {{1 + 1}}

######################################################################
####
#
test ProduceOutput-2.1 {test scope of evaluation variable} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName e0
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set v0 [java::new ptolemy.data.expr.Variable $fsm v0]
    set v1 [java::new ptolemy.data.expr.Parameter $fsm v1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set act0 [java::new ptolemy.domains.fsm.kernel.ProduceOutput $t0 act0]
    set v2 [java::cast ptolemy.data.expr.Variable [$t0 getAttribute _act0]]
    listToNames [[$v2 getScope] elementList]
} {preemptive _guard _trigger _actions v0 v1}

######################################################################
####
#
test ProduceOutput-3.1 {test execution} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set fsm [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $fsm s0]
    [java::field $fsm initialStateName] setExpression s0
    set p0 [java::new ptolemy.actor.TypedIOPort $fsm p0]
    $p0 setOutput true
    $p0 setMultiport true
    $p0 setTypeEquals [java::field ptolemy.data.type.BaseType INT]
    set v0 [java::new ptolemy.data.expr.Variable $fsm v0]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $fsm t0]
    set act0 [java::new ptolemy.domains.fsm.kernel.ProduceOutput $t0 act0]
    set channel [java::field $act0 channel]
    set act0 [java::cast ptolemy.domains.fsm.kernel.BroadcastOutput $act0]
    set expression [java::field $act0 expression]
    set pname [java::field $act0 portName]
    $pname setExpression p0
    $expression setExpression "v0 + 1"
    set tok2 [java::new {ptolemy.data.IntToken int} 0]
    $channel setToken $tok2
    $v0 setToken $tok2
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 e1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 p1]
    $p1 setInput true
    $p1 setMultiport true
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
    $r0 setWidth 2
    $p0 link $r0
    $p1 link $r0
    $dir preinitialize
    $act0 execute
    set re0 [[$p1 get 0] toString]
    catch {$p1 get 1} msg0
    set tok2 [java::new {ptolemy.data.IntToken int} 1]
    $channel setToken $tok2
    $v0 setToken $tok2
    $act0 execute
    catch {$p1 get 0} msg1
    set re1 [[$p1 get 1] toString]
    $pname setExpression p2
    catch {$act0 execute} msg2
    list $re0 $re1 $msg0 $msg1 $msg2
} {1 2 {ptolemy.actor.NoTokenException: ..e1.p1: Attempt to get data from an empty mailbox.} {ptolemy.actor.NoTokenException: ..e1.p1: Attempt to get data from an empty mailbox.} {ptolemy.kernel.util.IllegalActionException: ..fsm and ..fsm.t0.act0:
Cannot find port with name: p2}}
