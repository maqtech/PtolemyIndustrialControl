# Test UnionDisassembler
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}


######################################################################
####
#
test UnionDisassembler-1.1 {Test getVerboseString} {
    set workspace [java::new ptolemy.kernel.util.Workspace "uDWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "auto/UnionDisassemblerTest.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set unionDisassembler [java::cast {ptolemy.actor.lib.UnionDisassembler} \
	[$model getEntity UnionDisassembler]]
    set typeConstraints0 [$unionDisassembler typeConstraintList]
    regsub -all {\} \{} [lsort [listToStrings $typeConstraints0]] '\}\n\{' results0
    set manager [java::new ptolemy.actor.Manager $workspace "pubManager"]
    $model setManager $manager 
    $manager execute

    set typeConstraints1 [$unionDisassembler typeConstraintList]
    regsub -all {\} \{} [lsort [listToStrings $typeConstraints1]] '\}\n\{' results1
    list [list $results0] \
	"\n" \
	[list $results1]
} {{{{(TypeConstant, boolean) <= (ptolemy.actor.parameters.SharedParameter {.UnionDisassemblerTest.UnionDisassembler.bidirectionalTypeInference} true, boolean)'}
{'(ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.input}, unknown) <= (TypeConstant, {|car = general, cloth = general, money = general, record = general|})'}
{'(ptolemy.actor.util.ConstructCompositeType, {|car = unknown, cloth = unknown, money = unknown, record = unknown|}) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.input}, unknown)'}
{'(ptolemy.actor.util.ExtractFieldType, unknown) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.car}, unknown)'}
{'(ptolemy.actor.util.ExtractFieldType, unknown) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.cloth}, unknown)'}
{'(ptolemy.actor.util.ExtractFieldType, unknown) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.money}, unknown)'}
{'(ptolemy.actor.util.ExtractFieldType, unknown) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.record}, unknown)}}} {
} {{{(TypeConstant, boolean) <= (ptolemy.actor.parameters.SharedParameter {.UnionDisassemblerTest.UnionDisassembler.bidirectionalTypeInference} true, boolean)'}
{'(ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.input}, {|car = int, cloth = int, money = string, record = {car = int, cloth = int, money = string}|}) <= (TypeConstant, {|car = general, cloth = general, money = general, record = general|})'}
{'(ptolemy.actor.util.ConstructCompositeType, {|car = int, cloth = int, money = string, record = {car = int, cloth = int, money = string}|}) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.input}, {|car = int, cloth = int, money = string, record = {car = int, cloth = int, money = string}|})'}
{'(ptolemy.actor.util.ExtractFieldType, int) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.car}, int)'}
{'(ptolemy.actor.util.ExtractFieldType, int) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.cloth}, int)'}
{'(ptolemy.actor.util.ExtractFieldType, string) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.money}, string)'}
{'(ptolemy.actor.util.ExtractFieldType, {car = int, cloth = int, money = string}) <= (ptolemy.actor.TypedIOPort {.UnionDisassemblerTest.UnionDisassembler.record}, {car = int, cloth = int, money = string})}}}}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
