# Tests for the MoMLUndoChangeRequest class
#
# @Author: Christopher Hylands, based on MoMLChangeRequest.tcl by Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
#set VERBOSE 1


######################################################################
####
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
</entity>
}

######################################################################
####
#
test MoMLUndoChangeRequest-1.1 {Test adding an entity} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
    $toplevel setManager $manager

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="const" class="ptolemy.actor.lib.Const"/>
        </entity>
    }]
    $change setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change

    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </property>
    <entity name="const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLUndoChangeRequest-1.2 {Undo} {
    set originalMoML [$toplevel exportMoML]
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.moml.MoMLUndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneMoML [$toplevel exportMoML]
    diffText $originalMoML $undoneMoML
} {15,25d14
<     <entity name="const" class="ptolemy.actor.lib.Const">
<         <property name="value" class="ptolemy.data.expr.Parameter" value="1">
<         </property>
<         <port name="output" class="ptolemy.actor.TypedIOPort">
<             <property name="output"/>
<         </port>
<         <port name="trigger" class="ptolemy.actor.TypedIOPort">
<             <property name="input"/>
<             <property name="multiport"/>
<         </port>
<     </entity>}


test MoMLUndoChangeRequest-1.2a {Undo again, with nothing to undo} {
    # Uses $undoneMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.moml.MoMLUndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneAgainMoML [$toplevel exportMoML]
    diffText $undoneMoML $undoneAgainMoML
} {}

######################################################################
####
#
test MoMLUndoChangeRequest-1.3 {Redo} {
    # Uses $originalMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set redoChange [java::new ptolemy.moml.MoMLUndoChangeRequest \
	$originator $toplevel] 
    $redoChange setRedoable
    $toplevel requestChange $redoChange 
    set redoneMoML [$toplevel exportMoML]
    diffText $originalMoML $redoneMoML
} {}


######################################################################
####
#
test MoMLUndoChangeRequest-1.4 {Redo again, with nothing to redo } {
    # Uses $originalMoML from 1.2 above
    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set redoChange [java::new ptolemy.moml.MoMLUndoChangeRequest \
	$originator $toplevel] 
    $redoChange setRedoable
    $toplevel requestChange $redoChange 
    set redoneMoML [$toplevel exportMoML]
    diffText $originalMoML $redoneMoML
} {}

######################################################################
####
#
test MoMLUndoChangeRequest-2.1 {Make three changes, merge the first and the last, but the middle one is not undoable } {

    # Make a change that is undoable	
    set change1 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard1" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]
    $change1 setUndoable true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change1

    # Make a change that is not undoable	
    set change2 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard2" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]

    #$change2 setUndoable false

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change2

    # Make a change that is undoable	
    set change3 [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <entity name=".top">
            <entity name="discard3" class="ptolemy.actor.lib.Discard"/>
        </entity>
    }]

    $change3 setUndoable true
    $change3 setMergeWithPreviousUndo true

    # NOTE: Request is filled immediately because the model is not running.
    $manager requestChange $change3
    set threeChangeMoML [$toplevel exportMoML]

    set originator [java::new ptolemy.kernel.util.NamedObj "originator"]
    set undoChange [java::new ptolemy.moml.MoMLUndoChangeRequest \
	$originator $toplevel] 
    $toplevel requestChange $undoChange 
    set undoneThreeChangeMoML [$toplevel exportMoML]
    diffText $threeChangeMoML $undoneThreeChangeMoML

} {26,31d25
<     <entity name="discard1" class="ptolemy.actor.lib.Discard">
<         <port name="input" class="ptolemy.actor.TypedIOPort">
<             <property name="input"/>
<             <property name="multiport"/>
<         </port>
<     </entity>
33,38d26
<         <port name="input" class="ptolemy.actor.TypedIOPort">
<             <property name="input"/>
<             <property name="multiport"/>
<         </port>
<     </entity>
<     <entity name="discard3" class="ptolemy.actor.lib.Discard">}
