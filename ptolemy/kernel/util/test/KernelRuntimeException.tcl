# Tests for the KernelRuntimeException class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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

if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
} {}

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
    source [file join $PTII util testsuite jdktools.tcl]
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
test KernelRuntimeException-2.1 {Create a KernelRuntimeException} {
    set pe [java::new ptolemy.kernel.util.KernelRuntimeException]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{} {}}

set nameables [java::new java.util.LinkedList]
$nameables add [java::new ptolemy.kernel.util.NamedObj "n1"]
$nameables add [java::new ptolemy.kernel.util.NamedObj "n2"]
$nameables add [java::new ptolemy.kernel.util.NamedObj "n3"]
 
######################################################################
####
#
test KernelRuntimeException-7.1 {Create a KernelRuntimeException with a Collection, a Cause and a detail message} {
    set cause [java::new Exception "Cause Exception"]
    set pe [java::new ptolemy.kernel.util.KernelRuntimeException \
	    $nameables $cause  "Detail Message"]

    # Try out printStackTrace(PrintStream)
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    $pe printStackTrace $printStream
    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output

    list [$pe getMessage] [[$pe getCause] toString] "\n\n" \
	    [string range $output 0 108]
} {{Detail Message
  in .n1, .n2, .n3
Because:
Cause Exception} {java.lang.Exception: Cause Exception} {

} {ptolemy.kernel.util.KernelRuntimeException: Detail Message
  in .n1, .n2, .n3
Because:
Cause Exception
	at ja}}

test KernelRuntimeException-8.0 {printStackTrace()} {
    set cause [java::new Exception "Cause Exception"]
    set pe [java::new ptolemy.kernel.util.KernelRuntimeException \
	    $nameables $cause "Detail Message2"]
    jdkCaptureErr {$pe printStackTrace} errMsg
    list [string range $errMsg 0 108]
} {{ptolemy.kernel.util.KernelRuntimeException: Detail Message2
  in .n1, .n2, .n3
Because:
Cause Exception
	at j}}
