# Makefile for tests of classes in the ptolemy.util package
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/util/test

# PTJUNIT_DIR is set to junit in ptII.mk by configure if JUnit is found.
DIRS =		$(PTJUNIT_DIR)

# Root of the Ptolemy II directory
ROOT =		../../..

# Jar used in FileUtilities.tcl
PDFSAMPLE = $(ROOT)$(ME)/PDFSample.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(SAXON_JAR)$(CLASSPATHSEPARATOR)$(PDFSAMPLE)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	test
#PTCLASSJAR = 	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		junit/junit.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	Diff.java \
	InnerClassTest.java \
	RecorderMessageHandler.java \
	ThreadStreamExec.java \
	WatchDog.java

JCLASS = $(JSRCS:%.java=%.class)

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	CancelException.tcl \
	ClassUtilities.tcl \
	FileUtilities.tcl \
	MessageHandler.tcl \
	StreamExec.tcl \
	StringUtilities.tcl \
	XSLTUtilities.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	PDFSample.jar \
	test.xml \
	copy.xsl \
	addMarkers.xsl \
        ptjacl.policy \
	#$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl  junit \
	extractJarFileTest.jar \
	junit \
	policy

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'InnerClassTest$$InnerClass.class' \
	'WatchDog$$1.class'

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl


# Rule to run a $(SCRIPT) with a security policy
# We have a rule here so that we can get AUXCLASSPATH for code coverage
test_policy:
	@"$(JAVA)" \
            -Djava.security.manager "-Djava.security.policy=ptjacl.policy" \
	    -classpath \
	    "$(CLASSPATH)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)$(PTJACL_JAR)"  \
	    "-Dptolemy.ptII.dir=$(PTII)" $(JTCLSHFLAGS) \
	    tcl.lang.Shell $(SCRIPT)



# Test running with ptolemy.ptII.dir containing cygrdrive.
# We have a rule here so that we can get AUXCLASSPATH for code coverage
test_cygdrive:
	@"$(JAVA)" \
	    -Dptolemy.ptII.dir=/cygdrive/c/ptII \
	    -classpath \
	    "$(CLASSPATH)$(AUXCLASSPATH)$(CLASSPATHSEPARATOR)$(PTJACL_JAR)"  \
	    $(JTCLSHFLAGS) \
	    tcl.lang.Shell ptolemy_ptII_dir.tcl


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
