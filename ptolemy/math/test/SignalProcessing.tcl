# Tests for the SignalProcessing Class
#
# @Author: Edward A. Lee, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

set PI [java::field java.lang.Math PI]

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test SignalProcessing-1.1 {close} {
    set epsilon [java::field ptolemy.math.SignalProcessing epsilon]
    set testpairslist [list \
	    [list 1 1] \
	    [list -1 [expr {-1 + $epsilon/2}]] \
	    [list -1 [expr {-1 - $epsilon/2}]] \
	    [list [expr {-1 + $epsilon/2}] -1] \
	    [list [expr {-1 - $epsilon/2}] -1] \
	    [list 1 2] \
	    [list -1 2] \
	    [list 1 -2] \
	    [list -1 -2] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NaN] 1]\
	    [list [java::field java.lang.Double MIN_VALUE] 1] \
	    [list [java::field java.lang.Double MAX_VALUE] 1] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] \
	          [java::field java.lang.Double POSITIVE_INFINITY]] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] \
	          [java::field java.lang.Double NEGATIVE_INFINITY]] \
	    [list [java::field java.lang.Double NaN] \
	          [java::field java.lang.Double NaN]] \
	    [list [java::field java.lang.Double MIN_VALUE] \
	          [java::field java.lang.Double MIN_VALUE]] \
	    [list [java::field java.lang.Double MAX_VALUE] \
	          [java::field java.lang.Double MAX_VALUE]] \
	    ]

    set results {}

    foreach testpair $testpairslist {
	set a [lindex $testpair 0]
	set b [lindex $testpair 1]
	if [catch {set callresults \
		[java::call ptolemy.math.SignalProcessing close  $a $b]} \
		errmsg] {
	    lappend results $errmsg
	} else {
	    lappend results $callresults
	}
    }
    list $results
} {{1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1 1}}

####################################################################
test SignalProcessing-2.1 {decibel} {
    epsilonDiff \
	    [list \
	    [java::call ptolemy.math.SignalProcessing {decibel double} -10.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 0.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 0.1] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 1.0] \
	    [java::call ptolemy.math.SignalProcessing {decibel double} 10.0] \
	    ] {-Infinity -Infinity -106.03796221 0.0 106.03796221}
} {}

####################################################################
test SignalProcessing-3.1 {decibel array: empty array} {
    set emptyarray [java::new {double[]} 0]
    set dbresults [java::call ptolemy.math.SignalProcessing \
	    {decibel double[]} $emptyarray]
    $dbresults getrange 0
} {}

####################################################################
test SignalProcessing-3.2 {decibel array} {
    set dbarray [java::new {double[]} 5 {-10.0 0.0 0.1 1.0 10.0}]
    set dbresults [java::call ptolemy.math.SignalProcessing \
	    {decibel double[]} $dbarray]
    epsilonDiff [$dbresults getrange 0] \
	    {-Infinity -Infinity -106.03796221 0.0 106.03796221}
} {}

####################################################################
test SignalProcessing-4.1 {fft Complex: null argument} {
    # Real array
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-4.2 {fft Complex: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} $ca0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}


####################################################################
test SignalProcessing-4.2 {fft Complex} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 5 [list $c1 $c0 $c0 $c0 $c0]]

    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[]} $ca1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.1 {fft Complex, order: null argument} {
    # Real array
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int} [java::null] 1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-5.2 {fft Complex, order: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int} $ca0 1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-5.3 {fft Complex, order 0} {
    catch {java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 0} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: order argument must be positive.}}

####################################################################
test SignalProcessing-5.4 {fft Complex, order 1} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.5 {fft Complex, order 2} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array
    set ca1 [java::new {ptolemy.math.Complex[]} 4 [list $c1 $c0 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}


####################################################################
test SignalProcessing-5.6 {fft Complex, order 1, w/ larger array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 3, or a order 1 fft, the size should be 2
    set ca1 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-5.7 {fft Complex, order 2, smaller array} {
    set c0 [java::new ptolemy.math.Complex 0.0 0.0]
    set c1 [java::new ptolemy.math.Complex 1.0 0.0]
    # Complex array of size 2, hopefully fft will pad
    set ca1 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft ptolemy.math.Complex[] int } $ca1 2]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-6.1 {fft Complex: empty array} {
    set da0 [java::new {double[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} $da0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-6.2 {fft Complex: null array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-6.3 {fft double} {
    # Real array
    set impulse [java::new {double[]} 5 [list 1.0 0.0 0.0 0.0 0.0]]
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} $impulse]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-7.1 {fft Complex, order: empty array} {
    set da0 [java::new {double[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} $da0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-7.2 {fft Complex, order: null array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[]} [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: empty array argument.}}

####################################################################
test SignalProcessing-7.3 {fft double, order 0} {
    # NOTE: uses setup from 6.3 above
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[] int} $impulse 0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fft: order argument must be positive.}}

####################################################################
test SignalProcessing-7.4 {fft double, order 1} {
    # NOTE: uses setup from 6.3 above
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[] int} $impulse 1 ]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-7.5 {fft double, order 3} {
    # NOTE: uses setup from 6.3 above
    # The input array is length 5.
    set result [java::call ptolemy.math.SignalProcessing \
	    {fft double[] int} $impulse 3 ]
    javaPrintArray $result
} {{1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i} {1.0 + 0.0i}}

####################################################################
test SignalProcessing-8.1 {fftInPlace: empty real array} {
    set da0 [java::new {double[]} 0]
    set ra2 [java::new {double[]} 2 {1.0 0.0}]
    set ia2 [java::new {double[]} 2 {0.0 1.0}]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $da0 $ia2]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: empty array argument.}}

####################################################################
test SignalProcessing-8.2 {fftInPlace: empty imaginary array} {
    # NOTE: uses setup from 7.1
    catch {java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 $da0} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: empty array argument.}}

####################################################################
test SignalProcessing-8.3 {fftInPlace: null real array} {
    # NOTE: uses setup from 7.1
    catch {java::call ptolemy.math.SignalProcessing \
	    fftInPlace  [java::null] $ia2} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: empty array argument.}}

####################################################################
test SignalProcessing-8.4 {fftInPlace: null imag array} {
    # NOTE: uses setup from 7.1
    catch {java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 [java::null]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: empty array argument.}}

####################################################################
test SignalProcessing-8.5 {fftInPlace: order 1} {
    # NOTE: uses setup from 7.1
    java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 $ia2
    list [$ra2 getrange 0] [$ia2 getrange 0]
} {{1.0 1.0} {1.0 -1.0}}

####################################################################
test SignalProcessing-8.6 {fftInPlace: two different size arrays} {
    set ra2 [java::new {double[]} 2 {1.0 0.0}]
    set ia1 [java::new {double[]} 1 {0.0}]
    java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 $ia2
    list [$ra2 getrange 0] [$ia2 getrange 0]
} {{1.0 1.0} {0.0 2.0}}

####################################################################
test SignalProcessing-8.7 {fftInPlace: two different size arrays} {
    set ra2 [java::new {double[]} 2 {1.0 0.0}]
    set ia1 [java::new {double[]} 1 {0.0}]
    catch {java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 $ia1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: arrays have different length.}}

####################################################################
test SignalProcessing-8.8 {fftInPlace: arrays not a power of two} {
    set ra2 [java::new {double[]} 3 {1.0 0.0 1.0}]
    set ia1 [java::new {double[]} 3 {1.0 1.0 -1.0}]
    catch {java::call ptolemy.math.SignalProcessing \
	    fftInPlace  $ra2 $ia1} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInPlace: In-place fft requires an array argument with length that is a power of two. Got length: 3}}

####################################################################
test SignalProcessing-9.1 {fftInverse Complex: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInverse: empty array argument.}}

####################################################################
test SignalProcessing-9.2 {fftInverse Complex: null array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse [java::null]]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInverse: empty array argument.}}

####################################################################
test SignalProcessing-9.3 {fftInverse Complex: order 1} {
    # The inverse of test 5.4 above
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c1 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca2]
    javaPrintArray $result
} {{1.0 + 0.0i} {0.0 + 0.0i}}

####################################################################
test SignalProcessing-9.4 {fftInverse Complex: array that is not a power of two in length} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca3]
    javaPrintArray $result
} {{0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i}}

####################################################################
test SignalProcessing-10.1 {fftInverse Complex int: empty array} {
    set ca0 [java::new {ptolemy.math.Complex[]} 0]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca0 1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInverse: empty array argument.}}

####################################################################
test SignalProcessing-10.2 {fftInverse Complex int: null array} {
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse [java::null] 1]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInverse: empty array argument.}}

####################################################################
test SignalProcessing-10.3 {fftInverse Complex: order 0} {
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c1 ]]
    catch {set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca2 0]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: SignalProcessing.fftInverse: order must be positive.}}

####################################################################
test SignalProcessing-10.4 {fftInverse Complex: order 1} {
    # The inverse of test 5.4 above
    set ca2 [java::new {ptolemy.math.Complex[]} 2 [list $c1 $c1 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca2 1]
    javaPrintArray $result
} {{1.0 + 0.0i} {0.0 + 0.0i}}

####################################################################
test SignalProcessing-10.5 {fftInverse Complex: array that is not a power of two in length} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca3 2]
    javaPrintArray $result
} {{0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i} {0.25 + 0.0i}}

####################################################################
test SignalProcessing-10.6 {fftInverse Complex: array is longer than order} {
    set ca3 [java::new {ptolemy.math.Complex[]} 3 [list $c1 $c0 $c0 ]]
    set result [java::call ptolemy.math.SignalProcessing \
	    fftInverse $ca3 1]
    javaPrintArray $result
} {{0.5 + 0.0i} {0.5 + 0.0i}}

####################################################################
test SignalProcessing-11.1 {poleZeroToFreq:} {
    list "We need tests for poleZeroToFreq with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-12.1  {powerOfTwo: check range} {
    set negative [catch {[java::call \
	    ptolemy.math.SignalProcessing powerOfTwo -0.01]} errMsg]
    set zero [catch {[java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 0.01]} errMsg]
    set positive [java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 2.1]
    set anotherpositive [java::call \
	    ptolemy.math.SignalProcessing powerOfTwo 10.0]
    list $negative $zero $positive $anotherpositive
} {1 1 4 16}

####################################################################
test SignalProcessing-13.1 {raisedCosine} {
    list "We need tests for raisedCosine with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-14.1 {raisedCosinePulse} {
    list "We need tests for raisedCosinePulse with realistic input data"
} {1} {KNOW_ERROR}


# Used to test sawtooth, square and triangle
proc _testSignalProcessingFunction { function period phase \
	starttime endtime steptime} {
    set plot 0
    if {$plot} {
	global plotfilenumber
	if ![info exists plotfilenumber] {
	    set plotfilenumber 1
	} else {
	    incr plotfilenumber
	}
	set plotfile /tmp/sp$plotfilenumber.plt
	set fd [open $plotfile "w"]
	puts $fd "TitleText: $function period=$period phase=$phase $starttime <= t <= $endtime by $steptime"

    } 
    set results {}
    for {set time $starttime} \
	    {$time < $endtime} \
	    {set time [expr {$time + $steptime}]} {
	set value  [java::call \
		ptolemy.math.SignalProcessing $function $period $phase $time]
	lappend results $value
	if {$plot} {
	    puts $fd "$time $value"
	}
    }
    if {$plot} {
	close $fd
	exec pxgraph $plotfile &
    }
    return $results
}

####################################################################
test SignalProcessing-15.1 {sawtooth} {
    _testSignalProcessingFunction sawtooth 1.0 0.0 -1.0 2.0 0.2
} {-2.0 -1.6 -1.2 -0.8 -0.4 0.0 0.4 0.8 -0.8 -0.4 0.0 0.4 0.8 -0.8 -0.4}

####################################################################
test SignalProcessing-15.2 {sawtooth: negative period} {
    # FIXME, some of the results are less than -1.0?
    _testSignalProcessingFunction sawtooth -1.0 0.5 -1.0 2.0 0.2
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-15.3 {sawtooth: negative phase} {
    # FIXME, some of the results are less than -1.0?
    _testSignalProcessingFunction sawtooth 1.0 -0.5 -1.0 2.0 0.2
    #{-1.0 0.6 0.2 -0.2 -0.6 -1.0 0.6 0.2 -0.2 -0.6 -1.0 -1.4 -1.8 -2.2 -2.6}
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.1 {square} {
    # FIXME, should these vary more at the beginning
    _testSignalProcessingFunction square 1.0 0.5 -1.0 2.0 0.2
    #1.0 1.0 1.0 1.0 1.0 -1.0 -1.0 -1.0 1.0 1.0 -1.0 -1.0 -1.0 1.0 1.0
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.2 {square: negative period} {
    # FIXME, the value goes to -1 and stays there?
    _testSignalProcessingFunction square -1.0 0.5 -1.0 2.0 0.2
    #-1.0 -1.0 -1.0 1.0 1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-16.3 {square: negative phase} {
    # FIXME, the value is always -1?
    _testSignalProcessingFunction square -1.0 -0.5 -1.0 2.0 0.2
    #-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0
} {} {KNOWN_ERROR}


####################################################################
test SignalProcessing-17.1 {sqrtRaisedCosine} {
    list "We need tests for sqrtRaisedCosine with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-18.1 {sqrtRaisedCosinePulse} {
    list "We need tests for sqrtRaisedCosinePulse with realistic input data"
} {1} {KNOW_ERROR}

####################################################################
test SignalProcessing-19.1 {triangle} {
    # FIXME: Does not look very triangular to me
    _testSignalProcessingFunction triangle 1.0 0.5 -1.0 2.0 0.2
    #-2.0 -1.2 -0.4 0.4 0.8 0.0 -0.8 -0.4 0.4 0.8 0.0 -0.8 -0.4 0.4 0.8
} {} {KNOWN_ERROR}

####################################################################
test SignalProcessing-19.2 {triangle: negative period} {
    # FIXME: values are less than -1.0
    _testSignalProcessingFunction triangle -1.0 0.5 -1.0 2.0 0.2
    #0.0 0.8 0.4 -0.4 -0.8 0.0 0.8 0.4 -0.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2
} {} {KNOW_ERROR}

####################################################################
test SignalProcessing-19.3 {triangle: negative phase} {
    # FIXME: values are less than -1.0
    _testSignalProcessingFunction triangle -1.0 -0.5 -1.0 2.0 0.2
    #0.0 0.8 0.4 -0.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2 -2.0 -2.8 -3.6 -4.4 -1.2
} {} {KNOW_ERROR}

####################################################################
test SignalProcessing-20.1 {unwrap} {
    list "We need tests for unwrap with realistic input data"
} {1} {KNOW_ERROR}
