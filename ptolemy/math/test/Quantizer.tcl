# Tests for the FixPoint Class
#
# @Author: Bart Kienhuis
#
# @Version: $Id$
#
# @Copyright (c) 1998-2002 The Regents of the University of California.
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

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################

test Quantizer-1.0 {The round function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
101.10010010110010100101011110101000 5.573400000110269 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred 
-101.11000101000111101011100001010010 -4.2299999999813735 No overflow occurred }}

test Quantizer-1.1 {The round function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {round java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
100.001110101110 4.22998046875 No overflow occurred 
-110.011011010011 -5.573486328125 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred }}

test Quantizer-1.2 {the round function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }}

test Quantizer-1.3 {the round function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {round double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010011 -5.573486328125 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }}

####################################################################

test Quantizer-2.0 {The truncate function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
101.10010010110010100101011110100111 5.573399999877438 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred 
-101.11000101000111101011100001010010 -4.2299999999813735 No overflow occurred }}

test Quantizer-2.1 {The truncate function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {truncate java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {truncate java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {truncate java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {truncate java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
100.001110101110 4.22998046875 No overflow occurred 
-110.011011010100 -5.5732421875 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred }}

test Quantizer-2.2 {the truncate function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }}

test Quantizer-2.3 {the truncate function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {truncate double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010100 -5.5732421875 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }}

####################################################################

test Quantizer-3.0 {The roundToZero function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
101.10010010110010100101011110100111 5.573399999877438 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred 
-101.11000101000111101011100001010010 -4.2299999999813735 No overflow occurred }}

test Quantizer-3.1 {The roundToZero function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {roundToZero java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundToZero java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundToZero java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundToZero java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
100.001110101110 4.22998046875 No overflow occurred 
-110.011011010100 -5.5732421875 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred }}

test Quantizer-3.2 {the roundToZero function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }}

test Quantizer-3.3 {the roundToZero function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundToZero double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010100 -5.5732421875 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }}

####################################################################

test Quantizer-4.0 {The roundUp function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
101.10010010110010100101011110101000 5.5734000001102686 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred 
-101.11000101000111101011100001010010 -4.2299999999813735 No overflow occurred }} {KNOWN_FAILED}

test Quantizer-4.1 {The roundUp function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {roundUp java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundUp java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundUp java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundUp java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
100.001110101111 4.230224609375 No overflow occurred 
-110.011011010100 -5.5732421875 No overflow occurred 
-101.110001010010 -4.22998046875 No overflow occurred }} {KNOWN_FAILED}

test Quantizer-4.2 {the roundUp function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101101 5.573486328125 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }} {KNOWN_FAILED}

test Quantizer-4.3 {the roundUp function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundUp double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010101 -5.573486328125 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }} {KNOWN_FAILED}

####################################################################

test Quantizer-5.0 {The roundDown function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
101.10010010110010100101011110100111 5.573399999877438 No overflow occurred 
-101.110001010001 -4.230224609375 No overflow occurred 
-101.11000101000111101011100001010001 -4.230000000214204 No overflow occurred }}

test Quantizer-5.1 {The roundDown function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {roundDown java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundDown java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundDown java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundDown java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
100.001110101110 4.22998046875 No overflow occurred 
-110.011011010011 -5.573486328125 No overflow occurred 
-101.110001010001 -4.230224609375 No overflow occurred }}

test Quantizer-5.2 {the roundDown function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
101.100100101100 5.5732421875 No overflow occurred 
11.1111111111111 3.9998779296875 Overflow occurred 
1.11111111111111 1.99993896484375 Overflow occurred 
0.111111111111111 0.999969482421875 Overflow occurred }}

test Quantizer-5.3 {the roundDown function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundDown double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {{
-110.011011010011 -5.573486328125 No overflow occurred 
-100.0000000000000 -4.0 Overflow occurred 
-10.00000000000000 -2.0 Overflow occurred 
-1.000000000000000 -1.0 Overflow occurred }}

####################################################################

test Quantizer-6.0 {The roundNearestEven function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -4.23  $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -4.23  $p1 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {} {KNOWN_FAILED}

test Quantizer-6.1 {The roundNearestEven function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(4.32)" ]
    set bd1 [java::new java.math.BigDecimal "5.5734" ]
    set bd2 [java::new java.math.BigDecimal "4.23" ]
    set bd3 [java::new java.math.BigDecimal "-5.5734" ]
    set bd4 [java::new java.math.BigDecimal "-4.23" ]

    set c0 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven java.math.BigDecimal ptolemy.math.Precision} $bd1 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven java.math.BigDecimal ptolemy.math.Precision} $bd2 $p0 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven java.math.BigDecimal ptolemy.math.Precision} $bd3 $p0 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven java.math.BigDecimal ptolemy.math.Precision} $bd4 $p0 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {} {KNOWN_FAILED}

test Quantizer-6.2 {the roundNearestEven function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} 5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {} {KNOWN_FAILED}

test Quantizer-6.3 {the roundNearestEven function} {
    set p0 [java::new ptolemy.math.Precision "(16/4)" ]
    set p1 [java::new ptolemy.math.Precision "(16/3)" ]
    set p2 [java::new ptolemy.math.Precision "(16/2)" ]
    set p3 [java::new ptolemy.math.Precision "(16/1)" ]
    set c0 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -5.5734 $p0 ]
    set c1 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -5.5734 $p1 ]
    set c2 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -5.5734 $p2 ]
    set c3 [java::call ptolemy.math.Quantizer \
	    {roundNearestEven double ptolemy.math.Precision} -5.5734 $p3 ]
    list "
[$c0 toBitString] [ $c0 toString ][[$c0 getError] getDescription] 
[$c1 toBitString] [ $c1 toString ][[$c1 getError] getDescription] 
[$c2 toBitString] [ $c2 toString ][[$c2 getError] getDescription] 
[$c3 toBitString] [ $c3 toString ][[$c3 getError] getDescription] "
} {} {KNOWN_FAILED}
