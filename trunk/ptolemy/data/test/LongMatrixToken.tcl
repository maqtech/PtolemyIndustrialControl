# Tests for the LongMatrixToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test LongMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.LongMatrixToken]
    $p toString
} {[0]}

######################################################################
####
# 
test LongMatrixToken-1.1 {Create a non-empty instance from an int} {
    set a [java::new {long[][]} {2 2} {{5 4} {3 2}}]
    set p [java::new {ptolemy.data.LongMatrixToken long[][]} $a]
    $p toString
} {[5, 4; 3, 2]}

######################################################################
####
# 
test LongMatrixToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.LongMatrixToken String} "\[\[5 4\]\[3 2\]\]"]
    $p toString
} {ptolemy.data.LongMatrixToken([5, 4; 3, 2])} {Expression language doesn't support this yet}

######################################################################
####
# 
test LongMatrixToken-2.0 {Create a non-empty instance and query its value as an int} {
    catch {$p intMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.LongMatrixToken cannot be converted to an integer matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.1 {Create a non-empty instance and query its value as a double} {
    catch {$p doubleMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.LongMatrixToken cannot be converted to a double matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    set res1 [$p longMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5 4} {3 2}}

######################################################################
####
# 
test LongMatrixToken-2.3 {Create a non-empty instance and query its value as a string} {
    set res1 [$p toString]
    list $res1
} {{[5, 4; 3, 2]}}

######################################################################
####
#
test LongMatrixToken-2.4 {Create a non-empty instance and query its value as a complex} {
    catch {$p complexMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.LongMatrixToken cannot be converted to a complex matrix.}}

######################################################################
####
# 
test LongMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[0, 0; 0, 0]}}

######################################################################
####
# 
test LongMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1, 0; 0, 1]}}

######################################################################
####
# Test addition of longs to Token types below it in the lossless 
# type hierarchy, and with other longs.
test LongMatrixToken-3.0 {Test adding longs.} {
    set b [java::new {long[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$p add $q]

    list [$res1 toString] 
} {{[7, 5; 6, 3]}}

######################################################################
####
# Test division of longs with Token types below it in the lossless 
# type hierarchy, and with other longs. Note that dividing longs could 
# give a double.
test LongMatrixToken-4.0 {Test dividing longs.} {
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Division not supported for ptolemy.data.LongMatrixToken divided by ptolemy.data.LongMatrixToken.}}

######################################################################
####
# Test equals operator applied to other longs and Tokens types 
# below it in the lossless type hierarchy.
test LongMatrixToken-5.0 {Test equality between longs.} {
    set q2 [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$q isEqualTo $q2]
    set res2 [$q isEqualTo $p]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# Test modulo operator between longs and longs.
test LongMatrixToken-6.0 {Test modulo between longs.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported: ptolemy.data.LongMatrixToken modulo ptolemy.data.LongMatrixToken.}}

######################################################################
####
# Test multiply operator between longs and longs.
test LongMatrixToken-7.0 {Test multiply operator between longs.} {
    set b3 [java::new {long[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q3 [java::new {ptolemy.data.LongMatrixToken long[][]} $b3]
    set res1 [$p multiply $q]
    set res2 [$p multiply $q3]
    catch {$q3 multiply $p} res3

    list [$res1 toString] [$res2 toString] $res3
} {{[22, 9; 12, 5]} {[22, 9, 39; 12, 5, 21]} {ptolemy.kernel.util.IllegalActionException: Cannot multiply matrix with 3 columns by a matrix with 2 rows.}} {not yet implemented}

######################################################################
####
# Test subtract operator between longs and longs.
test LongMatrixToken-8.0 {Test subtract operator between longs.} {
    set b [java::new {long[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.LongMatrixToken long[][]} $b]
    set res1 [$p subtract $q]

    list [$res1 toString] 
} {{[3, 3; 0, 1]}} {not yet implemented}

