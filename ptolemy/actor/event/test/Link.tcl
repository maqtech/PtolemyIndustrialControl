# Test Link
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Link
#
test Link-1.0 {test adding a new link} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]

    set relation [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]
    set m [$e0 getManager]
    $m initialize
    $m iterate
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]
    set recinput [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]
    set c1 [java::new ptolemy.actor.event.Link $e0 $recinput $relation]
    set c2 [java::new ptolemy.actor.event.InitializeActor $e0 $rec2]
    $m requestChange $c1
    $m requestChange $c2
    $m iterate
    $m iterate
    $m wrapup
    list [enumToTokenValues [$rec getRecord 0]] \
            [enumToTokenValues [$rec2 getRecord 0]]
} {{1 1 1} {1 1}}
