# Test Test
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Run the codegenerator on file
proc testCG {file} {
    catch {set application [createAndExecute $file]} errMsg
    set args [java::new {String[]} 1 \
	  [list $file]]

    set returnValue [java::call ptolemy.codegen.kernel.CodeGenerator \
   	generateCode $args]
    list $returnValue
}

######################################################################
####
#
test Test-1.1 {} {
    catch {java::new ptolemy.actor.gui.MoMLSimpleApplication TestFailure.xml} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Test fails in iteration 1.
Value was: 1.0. Should have been: 1.1
  in .TestFailure.Test}}

######################################################################
####
#
test Test-2.1 {TestFailure should fail because the expected values are not correct} {
    testCG TestFailure.xml
} {255}

######################################################################
####
#
test Test-2.2 {TestFailure2Channel should fail because the expected values are not correct} {
    testCG TestFailure2Channel.xml
} {255}

######################################################################
####
#
test Test-3.1 {TestNotEnoughTokens.xml should fail because we don't have enoughtokens} {
    testCG TestNotEnoughTokens.xml
} {254}
