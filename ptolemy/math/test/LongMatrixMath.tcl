# Tests for the LongMatrixMath Class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set epsilon [java::field ptolemy.math.SignalProcessing epsilon]


set m3 [java::new {long[][]} 3 [list [list 3 -6 0] \
                                       [list 4862 236 -36] \
                                       [list -56 -26 4]]]
set m23 [java::new {long[][]} 2 [list [list 3 -6 0] \
 	                                [list 4862 236 -36]]]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test LongMatrixMath-0.5.1 {applyBinaryOperation LongBinaryOperation long long[][]} {
    set dbo [java::new ptolemy.math.test.TestLongBinaryOperation]
    set mr [java::call ptolemy.math.LongMatrixMath \
	    {applyBinaryOperation ptolemy.math.LongBinaryOperation long long[][]} $dbo -2 $m3]

    set s [java::call ptolemy.math.LongMatrixMath toString $mr]
    regsub -all {,} $s {} stmp

    epsilonDiff $stmp {{{-5 4 -2} {-4864 -238 34} {54 24 -6}}}
} {}

####################################################################
test LongMatrixMath-0.5.2 {applyBinaryOperation LongBinaryOperation long[][] long} {
    set dbo [java::new ptolemy.math.test.TestLongBinaryOperation]
    set mr [java::call ptolemy.math.LongMatrixMath \
	    {applyBinaryOperation ptolemy.math.LongBinaryOperation long[][] long} $dbo $m3 -2]

    set s [java::call ptolemy.math.LongMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{5 -4 2} {4864 238 -34} {-54 -24 6}}}
} {}

####################################################################
test LongMatrixMath-0.5.3.1 {applyBinaryOperation LongBinaryOperation long[][] long[][]} {
    set dbo [java::new ptolemy.math.test.TestLongBinaryOperation]
    set mr [java::call ptolemy.math.LongMatrixMath \
	    {applyBinaryOperation ptolemy.math.LongBinaryOperation long[][] long[][]} $dbo $m3 $m3]

    set s [java::call ptolemy.math.LongMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{0 0 0} {0 0 0} {0 0 0}}}
} {}

####################################################################
test LongMatrixMath-0.5.3.2 {applyBinaryOperation LongBinaryOperation long[][] long[][] with matrices that are different sizes} {
    set dbo [java::new ptolemy.math.test.TestLongBinaryOperation]
    catch { set mr [java::call ptolemy.math.LongMatrixMath \
	    {applyBinaryOperation ptolemy.math.LongBinaryOperation long[][] long[][]} $dbo $m3 $m23]} errMsg
    set errMsg
} {java.lang.IllegalArgumentException: ptolemy.math.LongMatrixMath.applyBinaryOperation() : one matrix [3 x 3] is not the same size as another matrix [2 x 3].}

####################################################################
test LongMatrixMath-0.6.1 {applyUnaryOperation LongUnaryOperation long[][]} {
    set duo [java::new ptolemy.math.test.TestLongUnaryOperation]
    set mr [java::call ptolemy.math.LongMatrixMath \
	    {applyUnaryOperation ptolemy.math.LongUnaryOperation long[][] } $duo $m23]
    set s [java::call ptolemy.math.LongMatrixMath toString $mr]
    regsub -all {,} $s {} stmp
    epsilonDiff $stmp {{{-3 6 0} {-4862 -236 36}}}
} {}

