# Test MaximumEntropySpectrum.
#
# @Author: Christopher Brooks, based on Clock.tcl by Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 2004-2008 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Constructors and Clone
#
test MaximumEntropySpectrum-1.0 {test constructor and initial value} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set mes [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile ../MaximumEntropySpectrum.xml]]
    set order [getParameter $mes order]
    set r1 [$mes getFullName] 	
    set workspace [$mes workspace]
    set e0 [java::new ptolemy.kernel.CompositeEntity $workspace]
    $e0 setName "MyCompositeEntity"
    $mes setContainer $e0
    list $r1 [$mes getFullName]	    	
} {.MaximumEntropySpectrum .MyCompositeEntity.MaximumEntropySpectrum}

test MaximumEntrySpectrum-1.2 {test clone} {
    set mes2 [java::cast ptolemy.actor.TypedCompositeActor [$mes clone [$e0 workspace]]]
    $order setExpression {16}
    set order [getParameter $mes2 order]
    [$order getToken] toString
} {8}

test MaximumEntrySpectrum-2.1 {test description} {
    set mes2_1 [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile ../MaximumEntropySpectrum.xml]]
    $mes2_1 description
} {}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
