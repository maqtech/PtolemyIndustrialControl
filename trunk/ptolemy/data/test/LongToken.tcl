# Tests for the LongToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
test LongToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.LongToken]
    $p toString
} {ptolemy.data.LongToken(0)}

######################################################################
####
# 
test LongToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.LongToken long} 7]
    $token toString
} {ptolemy.data.LongToken(7)}

######################################################################
####
# 
test LongToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.LongToken String} "5"]
    $token toString
} {ptolemy.data.LongToken(5)}

######################################################################
####
# 
test LongToken-2.0 {Create a non-empty instance and query its value as an long} {
    set p [java::new {ptolemy.data.LongToken long} 3]
    set res1 [$p longValue]
    set res2 [$p getValue]
    list $res1 $res2
} {3 3}

######################################################################
####
# 
test LongToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p doubleValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: ScalarToken.doubleValue: This base class does not contain a value.}}

######################################################################
####
# 
test LongToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: ScalarToken.intValue: This base class does not contain a value.}}

######################################################################
####
# 
test LongToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    $p stringValue
} {12}

######################################################################
####
# 
#test LongToken-2.4 {Create a non-empty instance and query its value as a complex#} {
#    set p [java::new {ptolemy.data.LongToken long} 12]
#    $p complexValue
#} {12}


######################################################################
####
# 
test LongToken-3.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.LongToken long} 23]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{ptolemy.data.StringToken(value is 2323.....)}}
