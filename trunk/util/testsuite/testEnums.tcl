# Tcl procs for accessing Enums in pt.kernel classes
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

# This file contains Tcl procs for accessing Enums in pt.kernel classes.
# These procs are not included in testDefs.tcl so that we can use them
# in other locations without including the test definitions.
#
# Each Java method that returns an Enumeration should have a proc
# in this file will return a Tcl list when given an object.
#
# The name of the proc should start with _test, then have the Class
# name, then the name of the method that returns the Enumeration.
# For example, Port has a enumRelations method, so we the proc
# would be named _testPortEnumRelation
#
# Eventually, these procs should use namespaces or be in a Itcl class.
# 
# Each file should include code to source testEnums.tcl if the procs
# the test file needs are not loaded:
# if {[info procs _testPortEnumRelation] == "" } then { 
#     source testEnums.tcl
# }
#
# Please keep the procs in this file alphabetical.

######################################################################
#### _testEnums
# This internal procedure provides common functionality for
# the other procs below.
# This proc returns a list consisting of the names of all of the
# elements in the enum returned by calling the method named by enummethod.
#
# enummethod is the name of the method to be called on the object to get
# the enum.
#
# args is one or more objects.
#
proc _testEnums {enummethod args} {
    set results {}
    foreach objecttoenum $args {
	if {$objecttoenum == [java::null]} {
	    lappend results [java::null]
	} else {
	    set lresults {}
	    for {set enum [$objecttoenum $enummethod]} \
		    {$enum != [java::null] && \
		    [$enum hasMoreElements] == 1} \
		    {} {
		set enumelement [$enum nextElement]
		if [ java::instanceof $enumelement pt.kernel.NamedObj] {
		    lappend lresults [$enumelement getName]
		} else {
		    lappend lresults $enumElement
		}
	    }
	    lappend results $lresults
	}
    }
    return $results
}

######################################################################
#### _testCrossRefListElements
# Given one or more CrossRefLists, return a Tcl List containing 
# a list of lists of the elements in each CrossRefList
#
proc _testCrossRefListElements {args} {
    eval _testEnums elements $args
}

######################################################################
#### _testParamListEnumParams
# Given a ParamList, return a Tcl List containing its Params.
#
proc _testParamListEnumParams {paramlist} {
    # We don't use _testEnums here because we want to call getValue too.
    set results {}
    if {$paramlist == [java::null]} {
	return $results
    } 
    for {set collectionenum [$paramlist enumParams]} \
	    {$collectionenum != [java::null] && \
	    [$collectionenum hasMoreElements] == 1} \
	    {} {
	set paramtest4 [$collectionenum nextElement]
	lappend results [list [$paramtest4 getName] [$paramtest4 getValue]]
    }
    return $results
}


######################################################################
#### _testPortEnumRelations
# Given a Port, return a Tcl List containing its Relations.
#
proc _testPortEnumRelations {args} {
    eval _testEnums enumRelations $args
}
