# Tests for configurations
#
# @Author: Steve Neuendorffer, Contributor: Christopher Hylands
#
# $Id$
#
# @Copyright (c) 2000-2002 The Regents of the University of California.
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

######################################################################
####
#


cd ..
set configs [glob */*configuration*.xml]
cd test

foreach i $configs {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    # Add backward compatibility filters
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # Filter out graphical classes while inside MoMLParser
    # See ptII/util/testsuite/removeGraphicalClasses.tcl
    removeGraphicalClasses $parser

    set loader [[$parser getClass] getClassLoader]
    

    if { "$i" == "vergilConfiguration.xml" } {
	puts "Stripping matlab actor out of vergilConfiguration.xml"
	set URL [$loader getResource "ptolemy/configs/vergilConfiguration.xml"]
	#puts "URL of vergilConfiguration.xml: [$URL toString]"
	if { "$tcl_platform(host_platform)" == "windows"} {
	    set inFile [string range [$URL getPath] 1 end]
	} else {
	    set inFile [$URL getPath]
	}

	#puts "file name vergilConfiguration.xml: $inFile"

	# See if jxta is present
	set jxta 1
	if [catch {java::info superclass net.jxta.resolver.QueryHandler} \
		err] { 
	    puts "net.jxta.resolver.QueryHandler not found, skipping jxta"
	    set jxta 0
	}

	set infd [open $inFile]
	set outfd [open vergilConfigurationNoMatlabNoSerialNoApps.xml "w"]
	while {![eof $infd]} {
	    set linein [gets $infd]
	    regsub -all {.*matlab.*} $linein {} lineout
	    # Filter out the serial actor because it does not work under Unix,
	    # which is where the nightly build is run
	    regsub -all {.*comm/comm.xml.*} $lineout {} lineout2

	    #regsub -all {.*experimentalDirectors.xml.*} $lineout2 {} lineout2

	    # Filter out apps
	    regsub -all {.*apps/apps.xml.*} $lineout2 {} lineout3
	    # Filter out jmf
	    regsub -all {.*jmf/jmf.xml.*} $lineout3 {} lineout4
	    # Filter out interactive icons
	    regsub -all {.*lib/interactive.xml.*} $lineout4 {} lineout5
	    # Filter out jxta
	    if {$jxta == 1} {
		set lineout6 $lineout5
	    } else {
		# Filter out jxta
		regsub -all {.*jxta/jxta.xml.*} $lineout5 {} lineout6
	    }
	    puts $outfd $lineout6
	}
	close $infd
	close $outfd
	set i test/vergilConfigurationNoMatlabNoSerialNoApps.xml
    } 

    set URL [$loader getResource ptolemy/configs/$i]
    set object [$parser {parse java.net.URL java.net.URL} $URL $URL]
    puts " Force everything to get expanded ptolemy/configs/$i"
    set configuration [java::cast ptolemy.kernel.CompositeEntity $object]
    

    test "$i-1.1" "Test to see if $i contains any bad XML" {
	# force everything to get expanded
	expr [string length [$configuration description]] > 0
    } {1}

    test "$i-2.1" "Test to see if $i has fields with names that are wrong" {
	# In general, if we call getName on a public field in an actor,
	# then the name that is returned should be the same as the name
	# of the field.
	set cloneConfiguration [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]

	set entityList [$configuration allAtomicEntityList]
	set results {}
	set logfile [open logfile2-1 "w"]
	for {set iterator [$entityList iterator]} \
		{[$iterator hasNext] == 1} {} {
	    set entity [$iterator next]
	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		set momlInfo [$actor getMoMLInfo]
		set className [java::field $momlInfo className]
		if [java::instanceof $entity $className] {
		    set realActor [java::cast $className $entity]
		    set fields [java::info fields $realActor]
		    # This puts seems to be necessary, or else we get
		    # field being set to 'tcl.lang.FieldSig@2b6fc7'
		    # instead of 'factor'
  		    puts $logfile "actor: $className fields: $fields"
		    foreach field $fields {
			# If the field is actually defined in the parent class
			# then java::field will not find the field
			set fieldObj [java::null]
			catch {
			    # We use -noconvert here in case there is a public
			    # int or double. hde.ArrayMem has a public int.
			    set fieldObj [java::field -noconvert \
				    $realActor $field]
			}
			if {![java::isnull $fieldObj]} {
			    if [catch {set dottedName [$fieldObj getName $entity]} errMsg] {
				set msg "\n\nIn '$className'\n\
					On the field '$field'\n\
					The getName() method failed:\n\
					$errMsg\n\
					Perhaps the field is a basic type?\n"
				lappend results $msg
			    } else {
				set sanitizedName [java::call ptolemy.util.StringUtilities sanitizeName $dottedName]
				if {"$sanitizedName" != "$field"} {
				    set msg "\n\nIn '$className'\n\
					    The getName() method returns\n \
					    '[$fieldObj getName]' but the \
					    field is named\n  '$field'"
				    lappend results $msg
				}
			    }
			}
		    }
		}
	    }
	}
	close $logfile
	file delete logfile2-1
	list $results
    } {{}}

    test "$i-3.1" "Test to see if $i contains any actors whose type constraints don't clone" {
	set cloneConfiguration [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]

	set entityList [$configuration allAtomicEntityList]
	set results {}
	for {set iterator [$entityList iterator]} {[$iterator hasNext] == 1} {} {
	    set entity [$iterator next]
	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {
		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		set fullName [$actor getName $configuration]
	
		set clone [java::cast ptolemy.actor.TypedAtomicActor [$cloneConfiguration getEntity $fullName]]
		if [java::isnull $clone] {
		    lappend results "\n\tActor $fullName was not cloned!"
		} {
		    set constraints [$actor typeConstraintList]
		    
		    set cloneConstraints [$clone typeConstraintList]
		    # Don't join the constraints, because some types have
		    # braces in them.
		    set c [jdkPrintArray \
				     [$constraints toArray] "\n" ]
		    set cc [jdkPrintArray \
				      [$cloneConstraints toArray] "\n" ]
		    if {$c != $cc} {
			set size [$constraints size]
			set cloneSize [$cloneConstraints size]
		   	set msg "\n\n[$actor getFullName]\n\
				\thas $size constraints, \
				that differ from the $cloneSize \
                                constraints its clone has."
			#set diff [diffText $c $cc]
			#puts $diff

			#set c [join [jdkPrintArray 
			#	[$constraints toArray] "\n" ] "\n"]
			#set cc [join [jdkPrintArray 
			#	[$cloneConstraints toArray] "\n" ] "\n"]
			lappend results "$msg\n\tActor Constraints:\n$c\
					     \tClone constraints:\n$cc"
		    }
		}
	    } 
	}
	
	# Don't call return as the last line of a test proc, since return
	# throws an exception.
	list $results
    } {{}}


    test "$i-4.1" "Test to see if $i contains any actors that might not drag and drop properly by creating ChangeRequests " {

	# This test caught a problem with AudioReader, where the initial
	# default source URL parameter had a bogus value.

	# Create a base model.
	set baseModel {<?xml version="1.0" standalone="no"?>
	    <!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
	    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
	    <entity name="top" class="ptolemy.actor.TypedCompositeActor">
	    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
	    <property name="iterations" value="2"/>
	    </property>
	    </entity>
	}

	set parser [java::new ptolemy.moml.MoMLParser]
	$parser reset
	set toplevel [java::cast ptolemy.actor.CompositeActor \
			  [$parser parse $baseModel]]
	set manager [java::new ptolemy.actor.Manager [$toplevel workspace] "w"]
	$toplevel setManager $manager
	

	# Set up a StreamChangeListener to listen for errors
	set stream [java::new java.io.ByteArrayOutputStream]
	set printStream [java::new \
		     {java.io.PrintStream java.io.OutputStream} $stream]
	set listener [java::new ptolemy.kernel.util.StreamChangeListener \
			  $printStream]
	$toplevel addChangeListener $listener

	set cloneConfiguration \
	    [java::cast ptolemy.kernel.CompositeEntity [$configuration clone]]

	set entityList [$configuration allAtomicEntityList]
	set results {}
	for {set iterator [$entityList iterator]} \
	    {[$iterator hasNext] == 1} {} {
	    set entity [$iterator next]
	    
	    #puts [$entity toString]
	    #if [java::instanceof $entity ptolemy.kernel.util.NamedObj] {
	    #	puts [[java::cast ptolemy.kernel.util.NamedObj $entity] \
	    #		  getName]
	    #}

	    if [java::instanceof $entity ptolemy.actor.TypedAtomicActor] {

		set actor [java::cast ptolemy.actor.TypedAtomicActor $entity]
		set fullName [$actor getName $configuration]
	
		set clone [java::cast ptolemy.actor.TypedAtomicActor \
			       [$cloneConfiguration getEntity $fullName]]
		set moml [java::new StringBuffer]
		# Simulate vergil.basic.EditorDropTarget.drop()
		$moml append "<group>"
		$moml append [$clone exportMoML "dropped_[$actor getName]"]
		$moml append "</group>"

		# The context of the ChangeRequest is the container
		# so that we properly evaluate atomic actors in
		# composite actors like MaximumEntropySpectrum
		set changeRequest [java::new ptolemy.moml.MoMLChangeRequest \
				       $toplevel [$clone getContainer] \
				       [$moml toString]]
		if [catch {$toplevel requestChange $changeRequest} errMsg] {
		    # Note that the changeRequest will likely never
		    # throw an error that will get us to here, we use
		    # a StreamChangeListener instead
		    set msg "\n\nIn '$fullName'\n\
                            the ChangeRequest:\n\
                            [$moml toString]\n\
                            failed:\n\
                            $errMsg\n\
                            Perhaps there is a typo in the initial\n\
                            value of a parameter?\n"
		    puts $msg
		    lappend results $msg
		}
		
		# Flush the listener
		$printStream flush
		regsub -all [java::call System getProperty "line.separator"] \
		    [$stream toString] "\n" output
		if {[string first "StreamChangeRequest.changeFailed():" \
			 $output] != -1 } {
		    # If the listener starts with changedFailed, then we
		    # have an error
		    lappend results $output
		    puts $output
		}
		$stream reset
	    }
	}
	list $results
    } {{}}

}

