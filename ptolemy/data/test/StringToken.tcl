# Tests for the StringToken class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test StringToken-1.1 {Get information about the class} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.data.StringToken]
    list [getJavaInfo $n]
} {{
  class:         pt.data.StringToken
  fields:        
  methods:       {add pt.data.Token} {addR pt.data.Token} clone {divide 
    pt.data.Token} {divideR pt.data.Token} {equality pt.dat
    a.Token} {equals java.lang.Object} {fromString java.lan
    g.String} getClass getObject getPublisher getValue hash
    Code isArray {modulo pt.data.Token} {moduloR pt.data.To
    ken} {multiply pt.data.Token} {multiplyR pt.data.Token}
     notify notifyAll notifySubscribers one {setPublisher p
    t.data.TokenPublisher} {setValue java.lang.Object} {set
    Value java.lang.String} stringValue {subtract pt.data.T
    oken} {subtractR pt.data.Token} toString wait {wait lon
    g} {wait long int} zero
    
  constructors:  pt.data.StringToken {pt.data.StringToken java.lang.Stri
    ng}
    
  properties:    array class object publisher value
    
  superclass:    pt.data.ObjectToken
    
}}

######################################################################
####
# 
test StringToken-2.1 {Create an empty instance} {
    set p [java::new pt.data.StringToken]
    $p toString
} {}

######################################################################
####
# 
test StringToken-2.2 {Create an empty instance and query its value} {
    set p [java::new pt.data.StringToken]
    $p getValue
} {}

######################################################################
####
# 
test StringToken-3.1 {Create an empty instance and attempt to init from string} {
    set p [java::new pt.data.StringToken]
    $p fromString foo
    $p toString
} {foo}

######################################################################
####
# 
test StringToken-4.1 {Create an empty instance and clone} {
    set p [java::new pt.data.StringToken]
    set q [$p clone]
    $q getValue
} {}

######################################################################
####
# 
test StringToken-4.2 {Create a non empty instance and clone} {
    set n [java::new {java.lang.String String} foo]
    set p [java::new pt.data.StringToken $n]
    set q [$p clone]
    list [$p toString] [$q toString]
} {foo foo}

