# Tests for the TypeConflictException
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test TypeConflictException-1.0 {Constructor that takes a List} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    set tDouble [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $tDouble

    set conflicts [java::new java.util.LinkedList]
    $conflicts add $p1
    $conflicts add $p2

    set ex1 [java::new ptolemy.actor.TypeConflictException $conflicts]
    set ex2 [java::new ptolemy.actor.TypeConflictException $conflicts \
	    "Detail Message"]
    list [$ex1 getMessage] [$ex2 getMessage] \
	    [listToFullNames [$ex1 objectList]]
} {{Type conflicts occurred at the following places:
  ..E1.P1: double
  ..E1.P2: unknown
} {Detail Message
  ..E1.P1: double
  ..E1.P2: unknown
} {..E1.P1 ..E1.P2}}

######################################################################
####
#
test TypeConflictException-1.1 {Test with structured types} {
    set port [java::new ptolemy.actor.TypedIOPort $e1 port]
    set param [java::new ptolemy.data.expr.Parameter $e1 param]

    # an ArrayType
    set unknown [java::field ptolemy.data.type.BaseType UNKNOWN]
    set arrayT [java::new ptolemy.data.type.ArrayType $unknown]

    # a RecordType
    set l [java::new {String[]} {2} {{name} {value}}]

    set nt [java::field ptolemy.data.type.BaseType STRING]
    set vt [java::field ptolemy.data.type.BaseType UNKNOWN]
    set v [java::new {ptolemy.data.type.Type[]} 2 [list $nt $vt]]

    set recordT [java::new {ptolemy.data.type.RecordType} $l $v]

    # construct the exception
    set conflicts [java::new java.util.LinkedList]
    $conflicts add $port
    $conflicts add $param
    $conflicts add $arrayT
    $conflicts add $recordT

    set ex [java::new ptolemy.actor.TypeConflictException $conflicts]
    list [$ex getMessage] [listToFullNames [$ex objectList]]
} {{Type conflicts occurred at the following places:
  ..E1.port: unknown
  ..E1.param: unknown
  (unknown)array
  {name:string, value:unknown}
} {..E1.port ..E1.param NOT_NAMEABLE. NOT_NAMEABLE.}}
