# Tests for the StaticSchedulingDirector class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998- The Regents of the University of California.
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
test StaticSchedulingDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.actor.StaticSchedulingDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.actor.StaticSchedulingDirector D2]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.actor.StaticSchedulingDirector $w D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 W.D3}

######################################################################
####
#
test StaticSchedulingDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [$d2 clone $w]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.D3}

######################################################################
####
#
test StaticSchedulingDirector-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set s0 [java::new ptolemy.actor.Scheduler]
    $d1 setScheduler $s0
    set s1 [$d1 getScheduler]
    list [$s0 getFullName] [$s1 getFullName]
} {{.D1.Basic Scheduler} {.D1.Basic Scheduler}}

######################################################################
####
#

