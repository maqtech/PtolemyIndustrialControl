# Tests for the PNDirector class that requires actors from pn/lib
#
# @Author: Mudit Goel
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

#############################################

test PNDirector-6.1 {Test an application} {
    set b1 [java::new ptolemy.actor.CompositeActor]
    $b1 setName b1
    set m2 [java::new ptolemy.actor.Manager m2]
    $b1 setManager $m2
    set d5 [java::new ptolemy.domains.pn.kernel.PNDirector "D5"]
    $b1 setDirector $d5
    set r1 [java::new ptolemy.domains.pn.lib.PNRamp $b1 r1]
    $r1 setParam "Initial Value"  2
    set s1 [java::new ptolemy.domains.pn.kernel.test.TestSink $b1 s1]
    $s1 clear
    set p1 [$r1 getPort output]
    set p2 [$s1 getPort input]
    $b1 connect $p1 $p2
    $m2 run
    $s1 getData
} {23456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100}
    
############################################
test PNDirector-7.1 {Test a mutation} {
    set b1 [java::new ptolemy.actor.CompositeActor]
    $b1 setName b1
    set m2 [java::new ptolemy.actor.Manager m2]
    $b1 setManager $m2
    set d5 [java::new ptolemy.domains.pn.kernel.PNDirector "local"]
    $b1 setDirector $d5
    set r1 [java::new ptolemy.domains.pn.lib.PNRamp $b1 r1]
    $r1 setParam "Initial Value"  2

    set sieve [java::new ptolemy.domains.pn.lib.PNSieve $b1 "2_sieve"]
    set param [$sieve getAttribute "prime"]
    $param setToken [java::new {ptolemy.data.IntToken int} 2]
    set portin [$sieve getPort "input"]
    set portout [$r1 getPort "output"]
    $b1 connect $portin $portout 
    
    set s1 [java::new ptolemy.domains.pn.kernel.test.TestSink $b1 s1]
    $s1 clear
    set p1 [$sieve getPort output]
    set p2 [$s1 getPort input]
    $b1 connect $p1 $p2
    $m2 run
    
    enumToFullNames [$b1 deepGetEntities]
} {.b1.r1 .b1.2_sieve .b1.s1 .b1.3_sieve .b1.5_sieve .b1.7_sieve .b1.11_sieve .b1.13_sieve .b1.17_sieve .b1.19_sieve .b1.23_sieve .b1.29_sieve .b1.31_sieve .b1.37_sieve .b1.41_sieve .b1.43_sieve .b1.47_sieve .b1.53_sieve .b1.59_sieve .b1.61_sieve .b1.67_sieve .b1.71_sieve .b1.73_sieve .b1.79_sieve .b1.83_sieve .b1.89_sieve .b1.97_sieve} 





