# Test GenericCodeGenerator
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

if {[info procs sdfModel] == "" } then {
    source [file join $PTII util testsuite models.tcl]
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

#####
test FMIMACodeGenerator-1.1 {Instantiate a FMIMACodeGenerator, call a few methods} {
    set model [sdfModel]
    set codeGenerator \
	    [java::new ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGenerator \
	    $model "myCodeGenerator"]

    list \
	[$codeGenerator toString] \
	[$codeGenerator comment {This is a comment}] \
} {{ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGenerator {.top.myCodeGenerator}} {/* This is a comment */
}}

#####
test FMIMACodeGenerator-5.1 {generateCode(StringBuffer)} {
    set stringBuffer [java::new StringBuffer]
    set result [$codeGenerator {generateCode StringBuffer} $stringBuffer]
    list $result [$stringBuffer toString]
} {0 {/* Generated from ptolemy/cg/kernel/generic/program/procedural/fmima/FMIMACodeGenerator.java _generateCode */
/* Generated by Ptolemy II (http://ptolemy.eecs.berkeley.edu)

Copyright (c) 2005-2013 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
 */
/* end includeecode */
/* end typeResolution code */
/* end shared code */
/* end variable declaration code */
/* end preinitialize code */
/* end preinitialize method code */
/* Before appending splitPreinitializeMethodBodyCode[0]. */
/* After appending splitPreinitializeMethodBodyCode[0]. */
/* preinitialization entry code */
/* preinitialization exit code */
/* Before appending splitVariableInitCode[0]. */

/* 
After appending splitVariableInitCode[0].
 */
/* Before appending splitInitializeCode[0]. */
/* After appending splitInitializeCode[0]. */
/* Before appending initializeEntryCode */
/* initialization entry code */
/* After appending initializeEntryCode */
/* Before appending splitVariableInitCode[1]. */
/* After appending splitVariableInitCode[1]. */
/* Before appending splitInitializeCode[1]. */
/* After appending splitInitializeCode[1]. */
/* Before appending initializeExitCode. */
/* initialization exit code */
/* wrapup entry code */
/* wrapup exit code */
/* ptolemy/cg/kernel/generic/program/procedural/fmima/FMIMACodeGenerator.java */
/* Probably the thing to do is to create .c files and copy them over to the cg/ directory. */
/* Then we can create a few functions that do the real work. */


int main(int argc, char *argv[]) {
/* ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/TypedCompositeActor.java start
   top contains:  */
/* ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java start
    */
/* ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java end */
/* ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/TypedCompositeActor.java end */

return 0;
}
/* Fire top_ */
/* closing exit code */
/* main exit code */
}}


