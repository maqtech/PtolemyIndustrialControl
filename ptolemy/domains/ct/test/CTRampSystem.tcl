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
####  The tests in this file construct and simulate a simple CT system
#     as the following:
#
#        +-------+           +------------+           +-----------+
#        | Const |---------->| Integrator |---------->| TestValue |
#        +-------+           +------------+           +-----------+
#
#     Test different ODE solvers and directors.


######################################################################
####  Test Director and solvers by execute a demo system
#  
# Note: Not depends on above set up. Can be moved to anywhere.
test CTRampSystem-4.1 {Ramp with ForwardEulerSolver} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set man [java::new ptolemy.actor.Manager]
    $sys setManager $man
    set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector DIR]
    $sys setDirector $dir
    #$dir setVERBOSE 1
    #$dir setDEBUG 1
    set const [java::new ptolemy.domains.ct.lib.CTConst $sys Const]
    set integral [java::new ptolemy.domains.ct.lib.CTIntegrator $sys Integ]
    set print [java::new ptolemy.domains.ct.test.CTTestValueSink\
    	    $sys Sink]
    #set sink [java::new ptolemy.domains.ct.lib.CTPrintln $sys pl]
    set constout [$const getPort output]
    set intglin [$integral getPort input]
    set intglout [$integral getPort output]
    set printin [$print getPort input]
    #set sinkin [$sink getPort input]

    set r1 [$sys connect $constout $intglin R1]
    set r2 [$sys connect $intglout $printin R2]
    #$sinkin link $r2
    
    set initstate [$integral getAttribute InitialState]
    $initstate setExpression 0.0
    #$initstate parameterChanged [java::null]

    set starttime [$dir getAttribute StartTime]
    $starttime setExpression 0.0
    #$starttime parameterChanged [java::null]
    
    set stoptime [$dir getAttribute StopTime]
    $stoptime setExpression 1.0
    #$stoptime parameterChanged [java::null]
    
    set initstep [$dir getAttribute InitialStepSize]
    $initstep setExpression 0.1
    #$initstep parameterChanged [java::null]
    
    set constval [$const getAttribute Value]
    $constval setExpression 1.0
    #$constval parameterChanged [java::null]
    
    $man run
    list [$print isSuccessful]
} {1}

test CTRampSystem-4.2 {Ramp with BackwardEulerSolver} {
    #Note: use above setup. reset parameters.
    set solver [$dir getAttribute ODESolver]
    set token [java::new ptolemy.data.StringToken\
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $solver setToken $token
    #$solver parameterChanged [java::null]

    set initstate [$integral getAttribute InitialState]
    $initstate setExpression 0.0
    #$initstate parameterChanged [java::null]

    set starttime [$dir getAttribute StartTime]
    $starttime setExpression 0.0
    #$starttime parameterChanged [java::null]
    
    set stoptime [$dir getAttribute StopTime]
    $stoptime setExpression 1.0
    #$stoptime parameterChanged [java::null]
    
    set initstep [$dir getAttribute InitialStepSize]
    $initstep setExpression 0.1
    #$initstep parameterChanged [java::null]
    
    set constval [$const getAttribute Value]
    $constval setExpression 1.0
    #$constval parameterChanged [java::null]
    
    $man run
    list [$print isSuccessful]
} {1}

test CTRampSystem-4.3 {Ramp with ExplicitRK23Solver} {
    #Note: use above setup. reset parameters.
    set solver [$dir getAttribute ODESolver]
    set token [java::new ptolemy.data.StringToken\
	    ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
    $solver setToken $token
    #$solver parameterChanged [java::null]

    set initstate [$integral getAttribute InitialState]
    $initstate setExpression 0.0
    #$initstate parameterChanged [java::null]

    set starttime [$dir getAttribute StartTime]
    $starttime setExpression 0.0
    #$starttime parameterChanged [java::null]
    
    set stoptime [$dir getAttribute StopTime]
    $stoptime setExpression 1.0
    #$stoptime parameterChanged [java::null]
    
    set initstep [$dir getAttribute InitialStepSize]
    $initstep setExpression 0.1
    #$initstep parameterChanged [java::null]
    
    set constval [$const getAttribute Value]
    $constval setExpression 1.0
    #$constval parameterChanged [java::null]
    
    $man run
    list [$print isSuccessful] 
} {1}

