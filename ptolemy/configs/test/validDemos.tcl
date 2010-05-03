# Tests for all demos listed in completeDemos.htm
#
# @Author: Christopher Brooks
#
# $Id$
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


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# Set the timeOut to two hours
set timeOutSeconds 12000


# See $PTII/ptolemy/moml/test/validateTests and validateDemos

proc validateModels {models} {
    global PTII
    set count 0
    foreach model $models {
	if {[string length $model] < 1} {
	    continue
	}
	incr count
	regsub {\$CLASSPATH} $model "$PTII" modelPath
	puts -nonewline .
	#puts $modelPath

	set fp [open $modelPath]
	set file_data [read $fp]
	close $fp

	set out [open /tmp/validDemos.xml w] 
	set data [split $file_data "\n"]
	set sawConfigure 0
	foreach line $data {
	    if [ regexp {<configure>} $line ] {
		incr sawConfigure
	    } else {
		if {$sawConfigure == 0} {
		    puts $out $line
		}
	    }
	    if [regexp {</configure>} $line ] {
		incr sawConfigure -1
	    }
        }
	close $out

	test validDemos-$count "Validating parse of $modelPath after removing configure" {
	    set errMsg {}
	    catch {java::call ptolemy.configs.test.ValidatingXMLParser parse /tmp/validDemos.xml} errMsg
	    list $errMsg
	} {{}}
    }
    puts "Checked $count demos from configs/doc/models.txt"
}

puts "Validating the xml in /auto/ xml files after removing the configure blocks"
set tests [exec find $PTII . -name adm -prune -o -name codeDoc -prune -o -name "*.xml"]
set autoTests {}
foreach test $tests {
    if [regexp {/auto/} $test] {
	lappend autoTests $test
    }
}
validateModels $autoTests

puts "Validating the xml in demo xml files after removing the configure blocks"
set file [open $PTII/ptolemy/configs/doc/models.txt]
set modelsFileContents [read $file]
close $file
set models [split $modelsFileContents "\n"]

validateModels $models
 
