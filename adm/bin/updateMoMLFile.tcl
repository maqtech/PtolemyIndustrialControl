# Tcl script that runs the backward compatibility filter on a MoML file

# Author:  Christopher Hylands
# Version: $Id$
#
# Copyright (c) 1999-2002 The Regents of the University of California.
# 	All Rights Reserved.
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

proc updateMoMLFile { {file ../../ptolemy/domains/ct/demo/CarTracking/CarTracking.xml} } {
    puts "parsing $file"
    set parser [java::new ptolemy.moml.MoMLParser]
    #$parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterBackwardCompatibility]
    set toplevel [$parser parseFile $file]
    set outputFile "updateMoMLFiles.xml"
    set fileOutputStream [java::new java.io.FileOutputStream $outputFile]
    set outputStreamWriter [java::new java.io.OutputStreamWriter \
	    $fileOutputStream]
    puts "exporting $outputFile"
    $toplevel exportMoML $outputStreamWriter
    $outputStreamWriter close
}

puts "$argv0 $argc $argv"
if {$argc > 0 } {
    updateMoMLFile $argv
}