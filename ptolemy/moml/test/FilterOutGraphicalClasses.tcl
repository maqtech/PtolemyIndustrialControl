# Tests for the FilterOutGraphicalClasses class
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test FilterOutGraphicalClasses-1.1 {filterAttributeValue} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterHideAnnotationNames]
    set toplevel [$parser parseFile "./FilterOutGraphicalClasses.xml"]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="MoMLFilter" class="ptolemy.actor.TypedCompositeActor">
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[600, 400]">
    </property>
    <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[104, 127]">
    </property>
    <property name="annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
FilterOutGraphicalClasses.</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_controllerFactory" class="ptolemy.kernel.util.Attribute">
        </property>
        <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="190.0, 5.0">
        </property>
        <property name="_hideName" class="ptolemy.data.expr.Parameter">
        </property>
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="100.0, 45.0">
        </property>
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="PI/2">
        </property>
        <doc>Create a constant sequence</doc>
        <property name="_location" class="ptolemy.moml.Location" value="100.0, 165.0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.kernel.util.StringAttribute" value="sin">
            <property name="style" class="ptolemy.actor.gui.style.ChoiceStyle">
                <property name="acos" class="ptolemy.kernel.util.StringAttribute" value="acos">
                </property>
                <property name="asin" class="ptolemy.kernel.util.StringAttribute" value="asin">
                </property>
                <property name="atan" class="ptolemy.kernel.util.StringAttribute" value="atan">
                </property>
                <property name="cos" class="ptolemy.kernel.util.StringAttribute" value="cos">
                </property>
                <property name="sin" class="ptolemy.kernel.util.StringAttribute" value="sin">
                </property>
                <property name="tan" class="ptolemy.kernel.util.StringAttribute" value="tan">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="235.0, 165.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="Test" class="ptolemy.actor.lib.Test">
        <property name="correctValues" class="ptolemy.data.expr.Parameter" value="{1.0,1.0,1.0,1.0,1.0}">
        </property>
        <property name="tolerance" class="ptolemy.data.expr.Parameter" value="1.0E-9">
        </property>
        <property name="_location" class="ptolemy.moml.Location" value="355.0, 165.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="Const.output" relation="relation"/>
    <link port="TrigFunction.input" relation="relation"/>
    <link port="TrigFunction.output" relation="relation2"/>
    <link port="Test.input" relation="relation2"/>
</entity>
}}

test FilterOutGraphicalClasses-2.2 {Try running old models, first check that the makefile created the compat/ directory} { 
    if {! [file exists compat]} {
	error "compat directory does not exist.  This could happen\
		If you do not have access to old Ptolemy II tests"
    } else {
	list 1
    }
} {1}

if {[info procs jdkStackTrace] == 1} then {
    source [file join $PTII util testsuite jdkTools.tcl]
}
# createAndExecute a file with a MoMLFilter
proc createAndExecute {file} {
    global KNOWN_FAILED
    if { "$file" == "compat/testAudioReaderAudioPlayer.xml" \
	    || "$file" == "compat/testAudioReader.xml" \
	    || "$file" == "compat/testAudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture_AudioPlayer.xml" \
	    || "$file" == "compat/testAudioCapture.xml" \
	    || "$file" == "compat/MaximumEntropySpectrum.xml" \ 
	    || "$file" == "compat/ArrayAppend.xml" } {
	puts "$file: Skipping Known Failure"
	incr KNOWN_FAILED
	return
    }

    
    #java::new ptolemy.actor.gui.MoMLSimpleApplication $file
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterOutGraphicalClasses]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterBackwardCompatibility]
    set namedObj [$parser parseFile $file]
    set toplevel [java::cast ptolemy.actor.CompositeActor $namedObj]

    # DT is a mess, don't bother testing it
    set compositeActor [java::cast ptolemy.actor.CompositeActor $toplevel]
    set director [$compositeActor getDirector]
    if [java::instanceof \
	    $director ptolemy.domains.dt.kernel.DTDirector] {
	puts "$file: Skipping DT tests, marking as Known Failure"
	incr KNOWN_FAILED
	return
    }

    # Look for comp
    set deepEntityList [$compositeActor deepEntityList]
    for {set i 0} {$i < [$deepEntityList size]} {incr i} {
	set containedActor [$deepEntityList get $i]
	if [java::instanceof $containedActor \
		ptolemy.actor.TypedCompositeActor] {
	    set compositeActor [java::cast ptolemy.actor.CompositeActor \
		    $containedActor]
	    set director [$compositeActor getDirector]
	    if [java::instanceof \
		    $director ptolemy.domains.dt.kernel.DTDirector] {
		puts "$file: Skipping tests with DT inside, marking as Known Failure"
		incr KNOWN_FAILED
		return
	    }
	}
    }


    #set newMoML [$toplevel exportMoML]
    #puts $newMoML

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute

}


# Find all the files in the compat directory


#foreach file [list compat/ComplexToCartesianAndBack.xml compat/testAudioReaderAudioPlayer.xml compat/test1.xml compat/FIR1.xml] {
foreach file [lsort [glob compat/*.xml]] {
    puts "------------------ testing $file"
    test "Auto" "Automatic test in file $file" {
        set application [createAndExecute $file]
        list {}
    } {{}}
    #test "Auto-rerun" "Automatic test rerun in file $file" {
    #	$application rerun
    #	list {}
    #} {{}}
}
#doneTests

