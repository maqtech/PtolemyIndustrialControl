# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

######################################################################
####  Test constructors.
#
test CTSingleSolverDirector-1.1 {Construct a Director and get name} {
    set d1 [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector]
    list  [$d1 getName]
} {{}}

test CTSingleSolverDirector-1.2 {Construct a Director with a name} {
    set d1 [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector DIR]
    list  [$d1 getName]
} {DIR}

test CTScheduler-1.3 {Construct a director with a name in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d2 [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector $w DIR2]
    list [$d2 getFullName]
} {W.DIR2}


######################################################################
####  Test methods in (abstract) CTDirector
#
test CTSingleSolverDirector-2.1 {Get default values} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector DIR]
    $sys setDirector $dir
    $dir initialize
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [$dir getFireBeginTime] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStartTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Forward_Euler_Solver 0.1 0.0 0.0 0.1 0.0001 20 1.0 1e-05 0.1 0.0 1.0 0.1 1e-10 1e-06}

test CTSingleSolverDirector-2.2 {set Parameters} {
    #Note: Use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    _testSetToken $param $token
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    $param setExpression 0.5
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute LocalTrancationErrorTolerance]]
    $param setExpression 0.4
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute MaximumIterationsPerStep]]
    $param setExpression 10
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute MaximumStepSize]]
    $param setExpression 0.3
    $param parameterChanged [java::null]    
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute MinimumStepSize]]
    $param setExpression 0.2
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    $param setExpression 10.0
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    $param setExpression 100.0
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute TimeResolution]]
    $param setExpression 1e-11
    $param parameterChanged [java::null]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ConvergeValueResolution]]
    $param setExpression 0.1
    $param parameterChanged [java::null]
    $dir prefire
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [$dir getFireBeginTime] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStartTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Backward_Euler_Solver 0.1 0.0 0.0 0.5 0.4 10 0.3 0.2 0.1 10.0 100.0 0.1 1e-11 0.1}

test CTSingleSolverDirector-2.3 {sets and gets} {
    #Note: Use above set up.
    $dir setCurrentTime 0.1
    $dir setCurrentStepSize 0.2
    $dir setStartTime 0.3
    $dir setStopTime 0.4
    list [$dir getCurrentTime] \
	    [$dir getCurrentStepSize] \
	    [$dir getStartTime] \
	    [$dir getStopTime] \
} {0.1 0.2 0.3 0.4}

test CTSingleSolverDirector-2.4 {suggested next step greater than max step} {
    #Note: Use above set up.
    # Max step size is 0.3
    $dir setSuggestedNextStepSize 0.5
    list [$dir getSuggestedNextStepSize]
} {0.3}


test CTSingleSolverDirector-2.5 {suggested next step less than max step} {
    #Note: Use above set up.
    # Max step size is 0.3
    $dir setSuggestedNextStepSize 0.1
    list [$dir getSuggestedNextStepSize]
} {0.1}

######################################################################
####  Test Breakpoints
#  
test CTSingleSolverDirector-3.1 {register a breakpoint} {     
    #Note: new set up.
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector DIR]
    $sys setDirector $dir
    $dir initialize
    $dir fireAt $sys 0.1
    $dir fireAt $sys 0.4
    $dir fireAt $sys 0.2
    set bptable [$dir getBreakPoints]
    set starttime [$bptable first]
    $bptable removeFirst
    set first [$bptable first]
    set firstAgain [$bptable first]
    $bptable removeFirst
    set second [$bptable first]
    set secondAgain [$bptable first]
    $bptable removeFirst
    set third [$bptable first]
    $bptable removeFirst
    set stoptime [$bptable first]
    list $starttime $first $firstAgain $second $secondAgain $third $stoptime
} {0.0 0.1 0.1 0.2 0.2 0.4 1.0}

test CTSingleSolverDirector-3.1 {access empty breakpoint table} {     
    #Note: use above set up.
    $bptable removeFirst
    set nextone [$bptable first]
    list [expr {$nextone == [java::null]}]
} {1}
