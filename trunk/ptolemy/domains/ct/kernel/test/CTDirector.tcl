# Tests for the CTDirector and CTMultiSolverDirector class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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
test CTDirector-1.1 {Construct a Director and get name} {
    set d1 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector]
    list  [$d1 getName]
} {{}}

test CTDirector-1.2 {Construct a Director in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d1 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $w]
    list  [$d1 getFullName]
} {.}

test CTDirector-1.3 {Construct with a name and a container} {
    set ca [java::new ptolemy.actor.TypedCompositeActor $w]
    $ca setName CA
    set d2 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $ca DIR2]
    list [$d2 getFullName]
} {.CA.DIR2}


######################################################################
####  Test methods in (abstract) CTDirector
#
test CTMultiSolverDirector-2.1 {Get default values} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    $dir preinitialize
    $dir initialize
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [$dir getIterationBeginTime] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Runge_Kutta_2_3_Solver 0.1 0.0 0.0 0.1 0.0001 20 1.0 1e-05 0.1 1.79769313486e+308 0.1 1e-10 1e-06}


######################################################################
####  Test set parameters.
#    
test CTMultiSolverDirector-2.2 {set Parameters by expression} {
    #Note: Use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $param setToken $token
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute initStepSize]]
    $param setExpression 0.5
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute errorTolerance]]
    $param setExpression 0.4
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxIterations]]
    $param setExpression 10
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxStepSize]]
    $param setExpression 0.3
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute minStepSize]]
    $param setExpression 0.2
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute startTime]]
    $param setExpression 10.0
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    $param setExpression 100.0
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute timeResolution]]
    $param setExpression 1e-11
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute valueResolution]]
    $param setExpression 0.1
    $param getToken

    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [$dir getIterationBeginTime] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Backward_Euler_Solver 0.1 0.0 0.0 0.5 0.4 10 0.3 0.2 0.1 100.0 0.1 1e-11 0.1}

######################################################################
####  Test set parameters, same as above, but uses setToken
#   
#     
test CTMultiSolverDirector-2.2a {set Parameters} {
    #Note: Use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute initStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.5]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute errorTolerance]]
    set token [java::new ptolemy.data.DoubleToken 0.4]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxIterations]]
    set token [java::new ptolemy.data.IntToken 10]
    $param setToken $token 

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.3]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute minStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.2]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute startTime]]
    set token [java::new ptolemy.data.DoubleToken 10.0]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    set token [java::new ptolemy.data.DoubleToken 100.0]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute timeResolution]]
    set token [java::new ptolemy.data.DoubleToken 1e-11]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute valueResolution]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $param setToken $token

    $dir prefire
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [$dir getIterationBeginTime] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Backward_Euler_Solver 0.1 0.0 0.0 0.5 0.4 10 0.3 0.2 0.1 100.0 0.1 1e-11 0.1}


test CTMultiSolverDirector-2.3 {sets and gets} {
    #Note: Use above set up.
    $dir setCurrentTime 0.1
    $dir setCurrentStepSize 0.2
    list [$dir getCurrentTime] \
	    [$dir getCurrentStepSize]
} {0.1 0.2}

#############################################################################
#### Test set suggested next step size, it is larger than the maximum
#    step size, so nothing has changed.
test CTMultiSolverDirector-2.4 {suggested next step greater than max step} {
    #Note: Use above set up.
    $dir setSuggestedNextStepSize 0.5
    list [$dir getSuggestedNextStepSize]
} {0.3}

#############################################################################
#### Test set suggested next step size, it is less than the maximum
#    step size, so it is effective.
test CTMultiSolverDirector-2.5 {suggested next step less than max step} {
    #Note: Use above set up.
    # Max step size is 0.3
    $dir setSuggestedNextStepSize 0.1
    list [$dir getSuggestedNextStepSize]
} {0.1}

######################################################################
####  Test Breakpoints
#  
test CTMultiSolverDirector-3.1 {register a breakpoint} {     
    #Note: new set up.
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token
    $dir preinitialize
    $dir initialize
    # NOTE: Each of these causes two breakpoints to be registered.
    # One at the specified time, and one at the specified time minus
    # the time resolution.
    set timeResolution [$dir getTimeResolution]
    $dir fireAt $sys 0.1
    $dir fireAt $sys 0.4
    $dir fireAt $sys 0.2
    set bptable [$dir getBreakPoints]
    set starttime [$bptable first]
    $bptable removeFirst
    set first [$bptable first]
    $bptable removeFirst
    set second [$bptable first]
    set secondAgain [$bptable first]
    $bptable removeFirst
    set third [$bptable first]
    $bptable removeFirst
    set fourth [$bptable first]
    $bptable removeFirst
    set fifth [$bptable first]
    $bptable removeFirst
    set sixth [$bptable first]
    $bptable removeFirst
    set seventh [$bptable first]
    $bptable removeFirst
    set eighth [$bptable first]
    $bptable removeFirst
    set ninth [$bptable first]
    $bptable removeFirst
    set stoptime [$bptable first]
    list $starttime $first $second $secondAgain $third $fourth \
            $fifth $sixth $seventh $eighth $ninth $stoptime
} {0.0 0.09999 0.1 0.1 0.10001 0.19999 0.2 0.20001 0.39999 0.4 0.40001 1.0}

test CTMultiSolverDirector-3.2 {access empty breakpoint table} {     
    #Note: use above set up.
    $bptable removeFirst
    set nextone [$bptable first]
    list [expr {$nextone == [java::null]}]
} {1}

test CTMultiSolverDirector-3.3 {BreakpointODESolver} {
    #Note: use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.DerivativeResolver]
    catch {$param setToken $token} msg
    #catch {$param getToken} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.domains.ct.kernel.solver.DerivativeResolver can only be used as a breakpoint ODE solver.
  in .System.DIR}}

