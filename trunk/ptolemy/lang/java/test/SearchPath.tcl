# Tests for the ASTReflect class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
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

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

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
test SearchPath-1.1 {create a search path} {
    set searchPath [java::new ptolemy.lang.java.SearchPath "java.class.path" "."]
    set searchList [$searchPath toArray]
    # When run inside ptjacl, the searchPath should contain at
    # least two elements $PTII and ptjacl.jar
    list [expr {[$searchList toString] > 2}]
} {1}

######################################################################
####
#
test SearchPath-1.2 {create a search path with a bogus propertyName} {
    set searchPath [java::new ptolemy.lang.java.SearchPath "not-a-property" "."]
    
    set knownResults "\[.[java::call System getProperty "file.separator"]\]"
    set results [$searchPath toString]
    #puts "$knownResults == $results"
    expr {"$knownResults" == "$results"}
} {1}

######################################################################
####
#
test SearchPath-1.3 {create a search path with a null propertyName} {
    set searchPath [java::new ptolemy.lang.java.SearchPath "not-a-property" "."]
    
    set knownResults "\[.[java::call System getProperty "file.separator"]\]"
    set results [$searchPath toString]
    #puts "$knownResults == $results"
    expr {"$knownResults" == "$results"}
} {1}

######################################################################
####
#
test SearchPath-1.4 {create a search path with a null propertyName and \
	a null fallbackPaths} {
    catch {set searchPath [java::new ptolemy.lang.java.SearchPath \
	    [java::null] [java::null]]} ex
    list $ex
} {java.lang.NullPointerException}

######################################################################
####
#
test SearchPath-1.5 {create a search path with a null propertyName and \
	an empty fallbackPaths} {
    set searchPath [java::new ptolemy.lang.java.SearchPath \
	    [java::null] ""]
    list [$searchPath toString]
} {{[]}}

######################################################################
####
#
test SearchPath-2.0 {openSource} {
    set searchPath [java::field ptolemy.lang.java.SearchPath UNNAMED_PATH]
    set file [$searchPath openSource ReflectTest false]
    set knownFile [java::new java.io.File "ReflectTest.java"]
    set canonicalFile [$file getCanonicalFile] 
    set canonicalKnownFile [$knownFile getCanonicalFile]
    list [$canonicalFile compareTo $canonicalKnownFile]
} {0}

######################################################################
####
#
test SearchPath-2.1 {openSource on not-a-file} {
    set searchPath [java::field ptolemy.lang.java.SearchPath UNNAMED_PATH]
    set file [$searchPath openSource not-a-file false]
     list [java::isnull $file]
} {1}
