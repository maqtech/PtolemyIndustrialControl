# Makefile for the SSM library
#
# @Authors: Ilge Akkaya
#
# @Version: $Id$
#
# @Copyright (c) 2012-2015 The Regents of the University of California.
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

ME =		org/ptolemy/ssm

DIRS =		#demo test

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	ssm
PTCLASSJAR =	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
#PTCLASSALLJAR = $(PTPACKAGE).jar

# Java Sources, keep these alphabetical,
JSRCS = \
	CovarianceStatePredictor.java \
	GaussianMeasurementModel.java \
	InferenceActor.java \
	Map.java \
	MapConstrained.java \
	MeasurementModel.java\
	MeasurementModelAttributes.java \
	MirrorDecorator.java \
	MirrorDecoratorAttributes.java \
	MirrorDecoratorListener.java \
	ParticleFilter.java \
	Predictor.java \
	StateSpaceActor.java \
	StateSpaceModel.java \
	StateSpaceSimulator.java \
	StatePredictorWithAccControl.java \
	StatePredictorWithControl.java \
	UnscentedKalmanFilterSSM.java


OTHER_FILES_TO_BE_JARED = \
	GaussianMeasurementModelIcon.xml \
	MapIcon.xml \
	ssm.xml \
	PredictorIcon.xml \
	ParticleFilterIcon.xml \
	StateSpaceModelIcon.xml \
	StateSpaceSimulatorIcon.xml

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Non-source files such as README files and subdirectories
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	test \
	'MirrorDecoratorListener$$DecoratorEvent.class'

KRUFT =

JCLASS = $(JSRCS:%.java=%.class)

all: sources jclass

install: all jars 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
 


