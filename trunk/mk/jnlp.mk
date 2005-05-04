# Ptolemy II to build Web Start JNLP files
#
# @Author: Christopher Brooks
# @Version: $Id$
#
# Copyright (c) 2001-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the above
# copyright notice and the following two paragraphs appear in all copies
# of this software.
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
#						PT_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
##########################################################################

# Java Network Launch Protocol aka Web Start
#
# This makefile should be included from the bottom of $PTII/makefile
# It is a separate file so as to not clutter up $PTII/makefile

# Large jar file containing all the codedoc documentation.
# Comment this out for testing
DOC_CODEDOC_JAR = \
	doc/codeDoc.jar
#DOC_CODEDOC_JAR =

# We put the signed jar files in a separate subdirectory
# for two reasons
# 1) If a jar file is checked in to cvs, and we sign it, then
# cvs update will think that we need to update it.
# So, we copy the jar files to a different directory and then sign
# them.
#
# 2) If we run applets with jar files that have been signed, then the
# user gets a confusing message asking if they want to run the signed
# applets.  Since the Ptolemy II applets do not require signed jar
# files, this is unnecessary

SIGNED_DIR =		signed

lib/joystickWindows.jar: 
	if [ -d vendors/misc/joystick/lib/ ]; then \
		(cd vendors/misc/joystick/lib/; \
	 	"$(JAR)" -cvf $(PTII)/lib/joystickWindows.jar jjstick.dll); \
	else \
		echo "vendors/misc/joystick not found, creating dummy jar"; \
		echo "vendors/misc/joystick/lib not found" \
			> README_joystick.txt; \
		"$(JAR)" -cvf $(PTII)/lib/joystickWindows.jar \
			README_joystick.txt; \
		rm -f README_joystick.txt; \
	fi

# NATIVE_SIGNED_LIB_JARS is a separate vaiable so that we can
# include it in ALL_JNLP_JARS
NATIVE_SIGNED_LIB_JARS = \
	lib/joystickWindows.jar \
	lib/matlabWindows.jar \
	lib/matlabSunOS.jar

SIGNED_LIB_JARS =	$(NATIVE_SIGNED_LIB_JARS) \
			lib/diva.jar \
			lib/jasminclasses.jar \
			lib/jython.jar \
			lib/ptCal.jar \
			lib/sootclasses.jar

# Web Start can load jars either eagerly or lazily.
# This makefile variable gets passed to $PTII/bin/mkjnlp and determines
# the number of jars that are loaded eagerly.  Of course, for this to
# work, the jars you want to load eagerly need to be at the front of the
# list.  In general, large jars such as diva.jar and ptsupport.jar
# should be loaded eagerly.
NUMBER_OF_JARS_TO_LOAD_EAGERLY = 11

# Jar files that will appear in most Ptolemy II JNLP files.
# HyVisual has its own set of core jars

# Order matters here, include the most important jars first
CORE_JNLP_JARS = \
	doc/docConfig.jar \
	lib/diva.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/domains/domains.jar \
	lib/matlab.jar \
	ptolemy/actor/parameters/demo/demo.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/domains/sdf/doc/doc.jar \
	ptolemy/matlab/matlab.jar \
	ptolemy/matlab/demo/demo.jar \
	lib/matlab.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar

#######
# DSP - The smallest runtime
#
# Jar files that will appear in a DSP only JNLP Ptolemy II Runtime.
#
# doc/design/usingVergil/usingVergil.jar is used in dsp, ptiny and full,
# but not hyvisual.
DSP_ONLY_JNLP_JARS = \
	doc/design/usingVergil/usingVergil.jar 

DSP_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/DSPApplication.jar

DSP_JNLP_JARS =	\
	$(DSP_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR)



#######
# HyVisual - HybridSystenms
#
# Jar files that will appear in a HyVisual only JNLP Ptolemy II Runtime.
# This list is used to create the ptII/signed directory, so each
# jar file should be named once in one of the *ONLY_JNLP_JARS
#  - rather than including domains.jar, we include only ct.jar, fsm.jar
#  - hybrid/configure.xml includes actor/lib/math.xml which includes
#    sdf.lib.DotProduct
#  - hybrid/configure.xml includes
#    actor/lib/conversions/conversions.xml
#    which includes
#    sdf.lib.BitsToInt
#    sdf.lib.IntToBits
#
# The full version of Vergil should not include any of the jar files below
# because the hsif conversion does not work here
HYBRID_SYSTEMS_ONLY_JNLP_JARS = \
	doc/design/hyvisual.jar \
	doc/codeDocHyVisual.jar \
	lib/saxon7.jar \
	ptolemy/domains/ct/ct.jar \
	ptolemy/domains/de/de.jar \
	ptolemy/domains/fsm/fsm.jar \
	ptolemy/domains/sdf/lib/lib.jar \
	ptolemy/domains/sdf/kernel/kernel.jar \
	ptolemy/hsif/hsif.jar \
	ptolemy/hsif/demo/demo.jar

HYBRID_SYSTEMS_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/HyVisualApplication.jar

HYBRID_SYSTEMS_JNLP_JARS =	\
	$(HYBRID_SYSTEMS_MAIN_JAR) \
	$(HYBRID_SYSTEMS_ONLY_JNLP_JARS) \
	doc/docConfig.jar \
	lib/diva.jar \
	ptolemy/domains/ct/demo/demo.jar \
	ptolemy/domains/ct/doc/doc.jar \
	ptolemy/domains/fsm/doc/doc.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
        ptolemy/matlab/demo/demo.jar \
        lib/matlab.jar



#######
# Ptiny
#
# Jar files that will appear in a smaller (Ptiny) JNLP Ptolemy II Runtime.
PTINY_ONLY_JNLP_JARS = \
	lib/jython.jar \
	lib/ptcolt.jar \
	ptolemy/actor/lib/colt/colt.jar \
	ptolemy/actor/lib/colt/demo/demo.jar \
	ptolemy/actor/lib/comm/demo/demo.jar \
	ptolemy/actor/lib/javasound/demo/demo.jar \
        ptolemy/actor/lib/python/python.jar \
        ptolemy/actor/lib/python/demo/demo.jar \
        ptolemy/actor/lib/security/demo/demo.jar \
	ptolemy/data/type/demo/demo.jar \
	ptolemy/data/unit/demo/demo.jar \
	ptolemy/domains/ct/demo/demo.jar \
	ptolemy/domains/ct/doc/doc.jar \
	ptolemy/domains/de/demo/demo.jar \
	ptolemy/domains/de/doc/doc.jar \
	ptolemy/domains/fsm/demo/demo.jar \
	ptolemy/domains/fsm/doc/doc.jar \
	ptolemy/domains/pn/demo/demo.jar \
        ptolemy/domains/pn/doc/doc.jar \
	ptolemy/moml/demo/demo.jar \
	ptolemy/vergil/kernel/attributes/demo/demo.jar

PTINY_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinyApplication.jar

PTINY_JNLP_JARS = \
	$(PTINY_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

PTINY_SANDBOX_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinySandboxApplication.jar

PTINY_SANDBOX_JNLP_JARS = \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)


#######
# Full
#
COPERNICUS_JARS = \
	lib/jasminclasses.jar \
	lib/sootclasses.jar \
	ptolemy/copernicus/copernicus.jar


EXEC_JARS = 	ptolemy/actor/gui/exec/exec.jar

PTJACL_JARS =	ptolemy/actor/gui/ptjacl/ptjacl.jar \
		lib/ptjacl.jar
# Do not include PTJACL for size reasons
PTJACL_JARS =

WIRELESS_JARS = \
	ptolemy/domains/wireless/wireless.jar \
	ptolemy/domains/wireless/demo/demo.jar


# Jar files that will appear in a full JNLP Ptolemy II Runtime
# ptolemy/domains/sdf/lib/vq/data/data.jar contains images for HTVQ demo
FULL_ONLY_JNLP_JARS = \
	$(COPERNICUS_JARS) \
	doc/design/design.jar \
	doc/img/img.jar \
	$(PTJACL_JARS) \
	ptolemy/actor/lib/hoc/demo/demo.jar \
	ptolemy/actor/lib/io/comm/comm.jar \
	ptolemy/actor/lib/io/comm/demo/demo.jar \
	ptolemy/actor/lib/jai/jai.jar \
	ptolemy/actor/lib/jai/demo/demo.jar \
	ptolemy/actor/lib/jmf/jmf.jar \
	ptolemy/actor/lib/jmf/demo/demo.jar \
	ptolemy/actor/lib/joystick/joystick.jar \
	ptolemy/actor/lib/jxta/jxta.jar \
	ptolemy/actor/lib/x10/x10.jar \
	vendors/misc/joystick/Joystick.jar \
	ptolemy/actor/lib/x10/demo/demo.jar \
	vendors/misc/x10/tjx10p-12/lib/x10.jar \
	lib/ptCal.jar \
	lib/saxon7.jar \
	ptolemy/caltrop/caltrop.jar \
	ptolemy/caltrop/demo/demo.jar \
	ptolemy/demo/demo.jar \
	ptolemy/domains/experimentalDomains.jar \
	ptolemy/domains/ci/demo/demo.jar \
	ptolemy/domains/ddf/demo/demo.jar \
	ptolemy/domains/dt/demo/demo.jar \
	ptolemy/domains/dt/doc/doc.jar \
	ptolemy/domains/giotto/demo/demo.jar \
	ptolemy/domains/giotto/doc/doc.jar \
	ptolemy/domains/gr/demo/demo.jar \
	ptolemy/domains/gr/doc/doc.jar \
	ptolemy/domains/gr/lib/quicktime/quicktime.jar \
	ptolemy/domains/hdf/demo/demo.jar \
	ptolemy/domains/psdf/psdf.jar \
	ptolemy/domains/psdf/demo/demo.jar \
	lib/mapss.jar \
	ptolemy/domains/sdf/lib/vq/data/data.jar \
	ptolemy/domains/sr/demo/demo.jar \
	ptolemy/domains/sr/doc/doc.jar \
	ptolemy/domains/tm/demo/demo.jar \
	ptolemy/domains/tm/doc/doc.jar \
	$(WIRELESS_JARS)

FULL_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/FullApplication.jar

FULL_JNLP_JARS = \
	$(FULL_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS)

#######
# VisualSense
#
# Jar files that will appear in a VisualSense only JNLP Ptolemy II Runtime.
# ct, fsm, de, sdf

# FIXME: experimentalDomains.jar also includes wireless.jar
# Jar files that are only used in JNLP
VISUAL_SENSE_ONLY_JNLP_JARS = \
	doc/design/visualsense.jar \
	doc/codeDocVisualSense.jar

VISUAL_SENSE_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/VisualSenseApplication.jar

VISUAL_SENSE_JNLP_JARS =	\
	$(VISUAL_SENSE_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(WIRELESS_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(VISUAL_SENSE_ONLY_JNLP_JARS)


#########

# All the JNLP Jar files except the application jars,
# hopefully without duplicates so that  we don't sign jars twice.
ALL_NON_APPLICATION_JNLP_JARS = \
	$(NATIVE_SIGNED_LIB_JARS) \
	$(CORE_JNLP_JARS) \
	$(DOC_CODEDOC_JAR) \
	$(FULL_ONLY_JNLP_JARS) \
	$(HYBRID_SYSTEMS_ONLY_JNLP_JARS) \
	$(VISUAL_SENSE_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS)


# All the jar files, include the application jars
ALL_JNLP_JARS = \
	$(ALL_NON_APPLICATION_JNLP_JARS) \
	$(DSP_MAIN_JAR) \
	$(HYBRID_SYSTEMS_JNLP_JARS) \
	$(PTINY_MAIN_JAR) \
	$(PTINY_MAIN_JAR) \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(FULL_MAIN_JAR)

# Makefile variables used to set up keys for jar signing.
# To use Web Start, we have to sign the jars.
KEYDNAME = "CN=Claudius Ptolemaus, OU=Your Project, O=Your University, L=Your Town, S=Your State, C=US "
KEYSTORE = ptKeystore
KEYALIAS = claudius
# The password should not be stored in a makefile, for production
# purposes, run something like:
#
# make KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD= jnlp_all
#
STOREPASSWORD = -storepass this.is.the.storePassword,change.it
KEYPASSWORD = -keypass this.is.the.keyPassword,change.it

KEYTOOL = $(PTJAVA_DIR)/bin/keytool

# Script to update a *.jnlp file with the proper jar files
MKJNLP =		$(PTII)/bin/mkjnlp

# JNLP files that do the actual installation
JNLPS =	vergilDSP.jnlp \
	vergilHyVisual.jnlp \
	vergilPtiny.jnlp \
	vergilPtinySandbox.jnlp \
	vergilVisualSense.jnlp \
	vergil.jnlp 

jnlp_all: $(KEYSTORE) $(SIGNED_LIB_JARS) jnlp_sign $(JNLPS) 
jnlps: $(SIGNED_LIB_JARS) $(JNLPS)
jnlp_clean: 
	rm -rf $(JNLPS) $(SIGNED_DIR)
jnlp_distclean: jnlp_clean
	rm -f  $(ALL_JNLP_JARS) 

$(SIGNED_DIR):
	if [ ! -d $(SIGNED_DIR) ]; then \
		mkdir -p $(SIGNED_DIR); \
	fi

$(KEYSTORE): 
	"$(KEYTOOL)" -genkey \
		-dname $(KEYDNAME) \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	"$(KEYTOOL)" -selfcert \
		-keystore $(KEYSTORE) \
		-alias $(KEYALIAS) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD)
	"$(KEYTOOL)" -list \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD)


# Web Start: DSP version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilDSP.jnlp: vergilDSP.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(DSP_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(DSP_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(DSP_MAIN_JAR)`; \
		cp -p $(DSP_MAIN_JAR) `dirname $(SIGNED_DIR)/$(DSP_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(DSP_MAIN_JAR) \
		$(DSP_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(DSP_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(DSP_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(DSP_MAIN_JAR)`; \
	cp -p $(DSP_MAIN_JAR) `dirname $(SIGNED_DIR)/$(DSP_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(DSP_MAIN_JAR) $(KEYALIAS)


# Web Start: HyVisual version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilHyVisual.jnlp: vergilHyVisual.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(HYBRID_SYSTEMS_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(HYBRID_SYSTEMS_MAIN_JAR)`; \
		cp -p $(HYBRID_SYSTEMS_MAIN_JAR) \
			`dirname $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(HYBRID_SYSTEMS_MAIN_JAR) \
		$(HYBRID_SYSTEMS_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(HYBRID_SYSTEMS_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(HYBRID_SYSTEMS_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(HYBRID_SYSTEMS_MAIN_JAR)`; \
	cp -p $(HYBRID_SYSTEMS_MAIN_JAR) `dirname $(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(HYBRID_SYSTEMS_MAIN_JAR) $(KEYALIAS)

# Web Start: Ptiny version of Vergil - No sources or build env.
vergilPtiny.jnlp: vergilPtiny.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(PTINY_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(PTINY_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_MAIN_JAR)`; \
		cp -p $(PTINY_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(PTINY_MAIN_JAR) \
		$(PTINY_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(PTINY_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_MAIN_JAR)`; \
	cp -p $(PTINY_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(PTINY_MAIN_JAR) $(KEYALIAS)


# Web Start: Ptiny version of Vergil - No sources or build env., in a sandbox
vergilPtinySandbox.jnlp: vergilPtinySandbox.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(PTINY_SANDBOX_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_SANDBOX_MAIN_JAR)`; \
		cp -p $(PTINY_SANDBOX_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(PTINY_SANDBOX_MAIN_JAR) \
		$(PTINY_SANDBOX_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_SANDBOX_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(PTINY_SANDBOX_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(PTINY_SANDBOX_MAIN_JAR)`; \
	cp -p $(PTINY_SANDBOX_MAIN_JAR) `dirname $(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(PTINY_SANDBOX_MAIN_JAR) $(KEYALIAS)


# Web Start: VisualSense version of Vergil - No sources or build env.
# In the sed statement, we use # instead of % as a delimiter in case
# PTII_LOCALURL has spaces in it that get converted to %20
vergilVisualSense.jnlp: vergilVisualSense.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(VISUAL_SENSE_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(VISUAL_SENSE_MAIN_JAR)`; \
		cp -p $(VISUAL_SENSE_MAIN_JAR) \
			`dirname $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)`; \
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(VISUAL_SENSE_MAIN_JAR) \
		$(VISUAL_SENSE_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(VISUAL_SENSE_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(VISUAL_SENSE_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(VISUAL_SENSE_MAIN_JAR)`; \
	cp -p $(VISUAL_SENSE_MAIN_JAR) `dirname $(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(VISUAL_SENSE_MAIN_JAR) $(KEYALIAS)

# Web Start: Full Runtime version of Vergil - No sources or build env.
vergil.jnlp: vergil.jnlp.in $(SIGNED_DIR) $(KEYSTORE)
	sed 	-e 's#@PTII_LOCALURL@#$(PTII_LOCALURL)#' \
		-e 's#@PTVERSION@#$(PTVERSION)#' \
			$< > $@
	if [ ! -f $(SIGNED_DIR)/$(FULL_MAIN_JAR) ]; then \
		echo "$(SIGNED_DIR)$(FULL_MAIN_JAR) does not"; \
		echo "   exist yet, but we need the size"; \
		echo "   so we copy it now and sign it later"; \
		mkdir -p $(SIGNED_DIR)/`dirname $(FULL_MAIN_JAR)`; \
		cp -p $(FULL_MAIN_JAR) `dirname $(SIGNED_DIR)/$(FULL_MAIN_JAR)`;\
	fi
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(SIGNED_DIR) \
		$(FULL_MAIN_JAR) \
		$(FULL_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(FULL_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(FULL_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	mkdir -p $(SIGNED_DIR)/`dirname $(FULL_MAIN_JAR)`; \
	cp -p $(FULL_MAIN_JAR) `dirname $(SIGNED_DIR)/$(FULL_MAIN_JAR)`; \
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(SIGNED_DIR)/$(FULL_MAIN_JAR) $(KEYALIAS)


# We first copy the jars, then sign them so as to avoid
# problems with cvs and applets.
jnlp_sign: jnlp_sign1 $(JNLPS) $(KEYSTORE)
jnlp_sign1: $(SIGNED_DIR)
	set $(ALL_NON_APPLICATION_JNLP_JARS); \
	for x do \
		if [ ! -f $(SIGNED_DIR)/$$x ]; then \
			echo "#  Copying $$x to $(SIGNED_DIR)/"; \
			mkdir -p $(SIGNED_DIR)/`dirname $$x`; \
			cp -p $$x `dirname $(SIGNED_DIR)/$$x`; \
		fi; \
		echo "# Signing $(SIGNED_DIR)/$$x"; \
		"$(PTJAVA_DIR)/bin/jarsigner" \
			-keystore $(KEYSTORE) \
			$(STOREPASSWORD) \
			$(KEYPASSWORD) \
			$(SIGNED_DIR)/$$x $(KEYALIAS); \
	done;

sign_jar: 
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(KEYPASSWORD) \
		$(JARFILE) $(KEYALIAS)


JAR_DIST_DIR = jar_dist

$(JAR_DIST_DIR):
	if [ ! -d $(JAR_DIST_DIR) ]; then \
		mkdir -p $(JAR_DIST_DIR); \
	fi
	set $(ALL_NON_APPLICATION_JNLP_JARS); \
	for x do \
		if [ ! -f $(JAR_DIST_DIR)/$$x ]; then \
			echo "#  Copying $$x to $(JAR_DIST_DIR)/"; \
			mkdir -p $(JAR_DIST_DIR)/`dirname $$x`; \
			cp -p $$x `dirname $(JAR_DIST_DIR)/$$x`; \
		fi; \
	done;

# Jarfiles used by applet code generation
CODEGEN_DOMAIN_JARS = \
	ptolemy/domains/ci/ci.jar \
	ptolemy/domains/ct/ct.jar \
	ptolemy/domains/de/de.jar \
	ptolemy/domains/ddf/ddf.jar \
	ptolemy/domains/fsm/fsm.jar \
	ptolemy/domains/gr/gr.jar \
	ptolemy/domains/hdf/hdf.jar \
	ptolemy/domains/pn/pn.jar \
	ptolemy/domains/sdf/sdf.jar \
	ptolemy/domains/wireless/wireless.jar

UNJAR_JARS = \
	ptolemy/actor/gui/jnlp/jnlp.jar \
	$(CODEGEN_DOMAIN_JARS) \
	$(ALL_NON_APPLICATION_JNLP_JARS)


UNJAR_DIST_DIR = unjar_dist

$(UNJAR_DIST_DIR):
	if [ ! -d $(UNJAR_DIST_DIR) ]; then \
		mkdir -p $(UNJAR_DIST_DIR); \
		mkdir -p $(UNJAR_DIST_DIR)/lib; \
		mkdir -p $(UNJAR_DIST_DIR)/doc; \
	fi
	mkdir -p $(UNJAR_DIST_DIR)/ptolemy/vergil
	cp ptolemy/vergil/vergilApplet.jar $(UNJAR_DIST_DIR)/ptolemy/vergil
	set $(UNJAR_JARS); \
	for x do \
		echo $$x; \
		case "$$x" in \
			lib/*) \
			   echo "  Copying to lib"; \
			   cp $$x $(UNJAR_DIST_DIR)/lib;; \
			doc/codeDoc*) \
			   echo "  Copying to doc"; \
			   cp $$x $(UNJAR_DIST_DIR)/doc;; \
			ptolemy/actor/gui/jnlp/jnlp.jar) \
			   echo "  Copying jar to ptolemy/actor/gui/jnlp"; \
			   mkdir -p $(UNJAR_DIST_DIR)/ptolemy/actor/gui/jnlp; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy/actor/gui/jnlp; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			ptolemy/hsif/hsif.jar) \
			   echo "  Copying jar to ptolemy/hsif"; \
			   mkdir -p $(UNJAR_DIST_DIR)/ptolemy/hsif; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy/hsif; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			ptolemy/hsif/demo/demo.jar) \
			   echo "  Copying jar to ptolemy/hsif/demo"; \
			   mkdir -p $(UNJAR_DIST_DIR)/ptolemy/hsif/demo; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy/hsif/demo; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			ptolemy/ptsupport.jar) \
			   echo "  Copying to ptolemy"; \
			   cp $$x $(UNJAR_DIST_DIR)/ptolemy;; \
			ptolemy/domains/*/*.jar) \
			   echo "Copying to domains specific jars for cg "; \
			   mkdir -p $(UNJAR_DIST_DIR)/`dirname $$x`; \
			   cp $$x `dirname $(UNJAR_DIST_DIR)/$$x`; \
			  (cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
			*)(cd $(UNJAR_DIST_DIR); "$(JAR)" -xf ../$$x);; \
	        esac; \
	done;
	# Remove jars lie pn/demo/demo.jar, but leave pn/pn.jar
	rm $(UNJAR_DIST_DIR)/ptolemy/domains/*/*/*.jar
	# Fix for quicktime.jar
	rm $(UNJAR_DIST_DIR)/ptolemy/domains/*/*/*/*.jar

# Verify the jar files.  This is useful for debugging if you are
# getting errors about unsigned applications
 
jnlp_verify:
	set $(ALL_JNLP_JARS); \
	for x do \
		echo "$$x"; \
		"$(PTJAVA_DIR)/bin/jarsigner" -verify $$x; \
	done;

# Update a location with the files necessary to download
DIST_BASE = ptolemyII/ptII4.0/jnlp-4.0-beta
DIST_DIR = /vol/ptolemy/pt0/ptweb/$(DIST_BASE)
DIST_URL = http://ptolemy.eecs.berkeley.edu/$(DIST_BASE)
OTHER_FILES_TO_BE_DISTED = doc/img/PtolemyIISmall.gif \
	ptolemy/configs/hyvisual/hyvisualPlanet.gif
KEYSTORE2=/users/ptII/adm/certs/ptkeystore
KEYALIAS2=ptolemy
# make jnlp_dist STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx"
# make DIST_DIR=c:/cxh/hyv DIST_URL=file:///c:/cxh/hyv jnlp_dist KEYSTORE2=ptKeystore KEYALIAS2=claudius

jnlp_dist: jnlp_dist_1 jnlp_dist_update
jnlp_dist_1:
	rm -rf $(JNLPS) $(SIGNED_DIR)
	$(MAKE) KEYSTORE=$(KEYSTORE2) \
		KEYALIAS=$(KEYALIAS2) \
		PTII_LOCALURL=$(DIST_URL) jnlp_sign

jnlp_dist_update:
	tar -cf - $(SIGNED_DIR) $(JNLPS) \
		$(OTHER_FILES_TO_BE_DISTED) | \
		ssh messier "cd $(DIST_DIR); tar -xpf -"
	scp doc/webStartHelp.htm messier:$(DIST_DIR)

#make KEYALIAS=ptolemy STOREPASSWORD="-storepass xxx" KEYPASSWORD="-keypass xxx" KEYSTORE=ptkeystore PTII_LOCALURL=http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII4.0/jnlp-4.0 jnlp_sign

jnlp_dist_update_remote:
	scp doc/webStartHelp.htm messier:$(DIST_DIR)
	tar -cf - $(SIGNED_DIR) $(JNLPS) \
		$(OTHER_FILES_TO_BE_DISTED) | \
		ssh messier "cd $(DIST_DIR); tar -xpf -"


sign_jar_dist: 
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE2) \
		$(JARFILE) $(KEYALIAS2)

sign_jar_dist_update_remote: sign_jar_dist
	scp $(JARFILE) messier:$(DIST_DIR)/$(JARFILE)
