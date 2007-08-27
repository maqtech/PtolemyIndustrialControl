# Test DepthFirstTransformer.
#
# @Author: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

######################################################################
####
#
test XMLTest-4.1.1 {Test rule4.xml with host4.1.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.1.xml]
    [$matchResult getMatchResult] toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output2:[.host4.relation3, .host4.Const.output2], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output2:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Display2.input:[.host4.relation3, .host4.Const.output2], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Display2.input:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue.output:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue.output:[.host4.relation, .host4.Display.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display.input:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display.input:[.host4.relation, .host4.Display.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.Display2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.gui.Display {.host4.Display2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}

test XMLTest-4.1.2 {Test rule4.xml with host4.1.xml (all matches)} {
    set helper [java::new ptolemy.actor.gt.test.AllMatchingTestHelper]
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.1.xml [java::field $helper callback]]
    $helper toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output2:[.host4.relation3, .host4.Const.output2], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output2:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Display2.input:[.host4.relation3, .host4.Const.output2], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Display2.input:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue.output:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue.output:[.host4.relation, .host4.Display.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display.input:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display.input:[.host4.relation, .host4.Display.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.Display2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.gui.Display {.host4.Display2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}

test XMLTest-4.2.1 {Test rule4.xml with host4.2.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.2.xml]
    [$matchResult getMatchResult] toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue2.output:[.host4.relation3, .host4.AbsoluteValue2.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue2.output:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display2.input:[.host4.relation3, .host4.AbsoluteValue2.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display2.input:[.host4.relation3, .host4.Display2.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display2.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display2}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}

test XMLTest-4.2.2 {Test rule4.xml with host4.2.xml (all matches)} {
    set helper [java::new ptolemy.actor.gt.test.AllMatchingTestHelper]
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.2.xml [java::field $helper callback]]
    $helper toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue2.output:[.host4.relation3, .host4.AbsoluteValue2.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue2.output:[.host4.relation3, .host4.Display2.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display2.input:[.host4.relation3, .host4.AbsoluteValue2.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display2.input:[.host4.relation3, .host4.Display2.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display2.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display2}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}
{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue.output:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue.output:[.host4.relation, .host4.Display.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display.input:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display.input:[.host4.relation, .host4.Display.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}

test XMLTest-4.3.1 {Test rule4.xml with host4.3.xml} {
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.3.xml]
    [$matchResult getMatchResult] toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue3.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue3.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue3.input:[.host4.relation4, .host4.AbsoluteValue3.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue3.output:[.host4.relation5, .host4.AbsoluteValue3.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue3.output:[.host4.relation5, .host4.Display3.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display3.input:[.host4.relation5, .host4.AbsoluteValue3.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display3.input:[.host4.relation5, .host4.Display3.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue3.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue3.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display3.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue3}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display3}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}

test XMLTest-4.3.2 {Test rule4.xml with host4.3.xml (all matches)} {
    set helper [java::new ptolemy.actor.gt.test.AllMatchingTestHelper]
    set matchResult [java::call ptolemy.actor.gt.RecursiveGraphMatcher match rule4.xml host4.3.xml [java::field $helper callback]]
    $helper toString
} {{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue3.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue3.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue3.input:[.host4.relation4, .host4.AbsoluteValue3.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue3.output:[.host4.relation5, .host4.AbsoluteValue3.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue3.output:[.host4.relation5, .host4.Display3.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display3.input:[.host4.relation5, .host4.AbsoluteValue3.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display3.input:[.host4.relation5, .host4.Display3.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue3.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue3.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display3.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue3}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display3}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}
{.rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.Const.output2:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.A.output1:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.Const.output2:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.Const.output:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.A.output2:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.Const.output:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.A.output1] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.Const.output2], .rule4.Left Hand Side.B.input:[.rule4.Left Hand Side.relation2, .rule4.Left Hand Side.B.input] = .host4.AbsoluteValue2.input:[.host4.relation4, .host4.AbsoluteValue2.input], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.A.output2] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.Const.output], .rule4.Left Hand Side.C.input:[.rule4.Left Hand Side.relation, .rule4.Left Hand Side.C.input] = .host4.AbsoluteValue.input:[.host4.relation2, .host4.AbsoluteValue.input], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.AbsoluteValue.output:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.C.output:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.AbsoluteValue.output:[.host4.relation, .host4.Display.input], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.C.output] = .host4.Display.input:[.host4.relation, .host4.AbsoluteValue.output], .rule4.Left Hand Side.D.input:[.rule4.Left Hand Side.relation3, .rule4.Left Hand Side.D.input] = .host4.Display.input:[.host4.relation, .host4.Display.input], ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output1} = ptolemy.actor.TypedIOPort {.host4.Const.output2}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.A.output2} = ptolemy.actor.TypedIOPort {.host4.Const.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.B.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue2.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.input} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.input}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.C.output} = ptolemy.actor.TypedIOPort {.host4.AbsoluteValue.output}, ptolemy.actor.TypedIOPort {.rule4.Left Hand Side.D.input} = ptolemy.actor.TypedIOPort {.host4.Display.input}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.A} = ptolemy.actor.lib.Const {.host4.Const}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.B} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue2}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.C} = ptolemy.actor.lib.AbsoluteValue {.host4.AbsoluteValue}, ptolemy.actor.gt.AtomicActorMatcher {.rule4.Left Hand Side.D} = ptolemy.actor.lib.gui.Display {.host4.Display}, ptolemy.actor.gt.CompositeActorMatcher {.rule4.Left Hand Side} = ptolemy.actor.TypedCompositeActor {.host4}}}
