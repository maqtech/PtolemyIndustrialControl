# Tests for the FixToken class
#
# @Author: Bart Kienhuis
#
# @Version $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
test FixToken-1.0 {Create an empty instance} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set c0 [java::call ptolemy.math.Quantizer round 0.0 $p0 ]
    set p [java::new ptolemy.data.FixToken $c0 ]
    $p toString
} {fix(0.0,16,4)}

test FixToken-1.1 {Create a non-empty instance from two strings} {
    set c1 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set p [java::new ptolemy.data.FixToken $c1 ]
    $p toString
} {fix(5.573486328125,16,4)}

test FixToken-1.2 {Create a non-empty instance from two strings} {
    set p1 [java::new ptolemy.math.Precision "(4^32)" ]
    set c2 [java::call ptolemy.math.Quantizer round 5.5734 $p1 ]
    set p [java::new ptolemy.data.FixToken $c2 ]
    $p toString
} {fix(5.573399998247623,32,4)}

test FixToken-1.3 {Create a non-empty instance from an String} {
    set p1 [java::new ptolemy.math.Precision "(4.12)" ]
    set c3 [java::call ptolemy.math.Quantizer round  7.7734 $p1 ]
    set p [java::new ptolemy.data.FixToken $c3 ]
    $p toString
} {fix(7.7734375,16,4)}

######################################################################

test FixToken-2.1 {Test additive identity} {	    
    set c21 [java::call ptolemy.math.Quantizer round 5.5734 $p0 ]
    set p [java::new ptolemy.data.FixToken $c21 ]
    set token [$p zero]
    list [$token toString]
} {fix(0.0,16,4)}

test FixToken-2.2 {Test multiplicative identity} { 
    set c22 [java::call ptolemy.math.Quantizer round 12.2 $p0 ]
    set p [java::new ptolemy.data.FixToken $c22 ]
    set token [$p one]
    list [$token toString]
} {fix(1.0,16,4)}

######################################################################

test FixToken-3.1 {Test Addition} {
    set c31 [java::call ptolemy.math.Quantizer round 3.2334454232 $p0 ]	
    set c32 [java::call ptolemy.math.Quantizer round -1.5454325   $p0 ]
    set pa [java::new ptolemy.data.FixToken $c31 ]
    set pb [java::new ptolemy.data.FixToken $c32 ]
    set res [$pa add $pb]    
    list [$pa toString] [$pb toString] [$res toString]

} {fix(3.2333984375,16,4) fix(-1.54541015625,16,4) fix(1.68798828125,16,4)}

test FixToken-3.2 {Test Subtraction} {
    set res [$pa subtract $pb]   
    list [$res toString]
} {fix(4.77880859375,16,4)}

test FixToken-3.3 {Test Multiply} {
    set res [$pa multiply $pb]   
    list [$res toString]
} {fix(-4.996926784515381,27,3)}

test FixToken-3.4 {Test Divide} {    
   set res [$pa divide $pb]    	
   list [$res toString]
} {fix(-2.09228515625,16,4)}
