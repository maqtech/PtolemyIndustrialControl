# Tests for the Nightly Build
#
# @Author: Christopher Brooks
#
# $Id: Release.tcl 63463 2012-05-02 02:47:37Z hudson $
#
# @Copyright (c) 2012 The Regents of the University of California.
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

#set VERBOSE 1
# Get rid of any previous lists of .java files etc.
exec make clean

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# This is a bit of a hack.
#
# We use hudson to invoke "ant installers", which invokes 
# adm/test/junit/JUnitTclTest.java, which runs the tcl tests
# in this directory including this file.
#
# This file runs make in adm/gen-X.0, which creates the installer
# and then invokes the installer and runs the tests using make.
# We use ptolemy.util.StreamExec to invoke make so that we
# can get the output as it occurs and so that we can search
# stdout and stderr for error messages.
#
# Try to keep up.
#
# In the future, it would be good to migrate away from some of
# this.  Maven has potential, but our jnlp setup of having
# many jnlp files does not fit the Maven model.


# These variables match variables in the $PTII/adm/gen-$version/makefile
set major_version 11.0
set minor_version devel
set version $major_version.$minor_version
set windows_version 11_0_devel
set gendir $PTII/adm/gen-$major_version
set ptII_full $gendir/ptII$version.tar
set ptII_src_jar $gendir/ptII$version.src.jar
set ptsetup ptII${windows_version}_setup_windows

proc nightlyMake {target {pattern {.*\*\*\*.*}}} {
    global PTII gendir
    set ptIIhome $PTII
    set ptIIadm $PTII/adm
    set user hudson

    # Use StreamExec so that we echo the results to stdout as the
    # results are produced.
    set streamExec [java::new ptolemy.util.StreamExec]
    set commands [java::new java.util.LinkedList]
    cd $PTII
    $commands add "make -C $gendir USER=$user PTII=${PTII} PTIIHOME=${ptIIhome} PTIIADM=${ptIIadm} JAR=/usr/bin/jar TAR=/usr/local/bin/tar $target"
    $streamExec setCommands $commands
    $streamExec setPattern $pattern
    $streamExec start
    set returnCode [$streamExec getLastSubprocessReturnCode]
    if { $returnCode != 0 } {
	return [list "Last subprocess returned non-zero: $returnCode" \
		    [$streamExec getPatternLog]]
    }
    return [$streamExec getPatternLog]
}

set startingDirectory [pwd]
cd $gendir

test coverity-1.0 {coverity} {
    set matches [nightlyMake coverity]
} {}

set VERBOSE 0
cd $startingDirectory