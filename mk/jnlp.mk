# Ptolemy II to build Web Start JNLP files
#
# @Author: Christopher Hylands
# @Version: $Id$
#
# Copyright (c) 2001-2002 The Regents of the University of California.
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

# If a jar file is checked in to cvs, and we sign it, then
# cvs update will think that we need to update it.
# So, we copy the jar files to a different directory and then sign
# them.
SIGNED_DIR =		signed

# NATIVE_SIGNED_LIB_JARS is a separate vaiable so that we can
# include it in ALL_JNLP_JARS
NATIVE_SIGNED_LIB_JARS = $(SIGNED_DIR)/lib/matlabWindows.jar 

SIGNED_LIB_JARS =	$(NATIVE_SIGNED_LIB_JARS) \
			$(SIGNED_DIR)/lib/diva.jar \
			$(SIGNED_DIR)/lib/jasminclasses.jar \
			$(SIGNED_DIR)/lib/matlab.jar \
			$(SIGNED_DIR)/lib/sootclasses.jar

# Web Start can load jars either eagerly or lazily.
# This makefile variable gets passed to $PTII/bin/mkjnlp and determines
# the number of jars that are loaded eagerly.  Of course, for this to
# work, the jars you want to load eagerly need to be at the front of the
# list.  In general, large jars such as diva.jar and ptsupport.jar
# should be loaded eagerly.
NUMBER_OF_JARS_TO_LOAD_EAGERLY = 8

# Jar files that will appear in all Ptolemy II JNLP files.
CORE_JNLP_JARS = \
	doc/docConfig.jar \
	$(SIGNED_DIR)/lib/diva.jar \
	ptolemy/domains/domains.jar \
	ptolemy/domains/sdf/demo/demo.jar \
	ptolemy/ptsupport.jar \
	ptolemy/vergil/vergil.jar \
	ptolemy/actor/lib/javasound/javasound.jar \
	ptolemy/media/javasound/javasound.jar \
	$(DOC_CODEDOC_JAR)

#######
# DSP - The smallest runtime
#
# Jar files that will appear in a DSP only JNLP Ptolemy II Runtime.
DSP_ONLY_JNLP_JARS =

DSP_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/DSPApplication.jar

DSP_JNLP_JARS =	\
	$(DSP_MAIN_JAR) \
	$(CORE_JNLP_JARS)


#######
# Ptiny
#
# Jar files that will appear in a smaller (Ptiny) JNLP Ptolemy II Runtime.
PTINY_ONLY_JNLP_JARS = \
	ptolemy/actor/lib/javasound/demo/demo.jar \
	ptolemy/data/type/demo/demo.jar \
	ptolemy/domains/ct/demo/demo.jar \
	ptolemy/domains/de/demo/demo.jar \
	ptolemy/domains/fsm/demo/demo.jar \
	ptolemy/moml/demo/demo.jar

PTINY_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinyApplication.jar

PTINY_JNLP_JARS = \
	$(PTINY_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)

PTINY_SANDBOX_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/PtinySandboxApplication.jar

PTINY_SANDBOX_JNLP_JARS = \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS)


#######
# Full
#
COPERNICUS_JARS = \
	$(SIGNED_DIR)/lib/jasminclasses.jar \
	$(SIGNED_DIR)/lib/sootclasses.jar \
	ptolemy/copernicus/copernicus.jar

# Jar files that will appear in a full JNLP Ptolemy II Runtime
FULL_ONLY_JNLP_JARS = \
	$(COPERNICUS_JARS) \
	ptolemy/actor/lib/comm/comm.jar \
	ptolemy/domains/experimentalDomains.jar \
	ptolemy/domains/dt/demo/demo.jar \
	ptolemy/domains/giotto/demo/demo.jar \
	ptolemy/domains/gr/demo/demo.jar \
	ptolemy/domains/pn/demo/demo.jar \
	ptolemy/domains/sr/demo/demo.jar \
	ptolemy/domains/tm/demo/demo.jar \
	ptolemy/matlab/demo/demo.jar \
	$(SIGNED_DIR)/lib/matlab.jar

FULL_MAIN_JAR = \
	ptolemy/actor/gui/jnlp/FullApplication.jar

FULL_JNLP_JARS = \
	$(FULL_MAIN_JAR) \
	$(CORE_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS)

#########

# All the JNLP Jar files except the application jars,
# hopefully without duplicates so that  we don't sign jars twice.
ALL_NON_APPLICATION_JNLP_JARS = \
	$(NATIVE_SIGNED_LIB_JARS) \
	$(CORE_JNLP_JARS) \
	$(FULL_ONLY_JNLP_JARS) \
	$(PTINY_ONLY_JNLP_JARS) \
	$(DSP_ONLY_JNLP_JARS)


# All the jar files, include the application jars
ALL_JNLP_JARS = \
	$(ALL_NON_APPLICATION_JNLP_JARS) \
	$(DSP_MAIN_JAR) \
	$(PTINY_MAIN_JAR) \
	$(PTINY_SANDBOX_MAIN_JAR) \
	$(FULL_MAIN_JAR)

# Script to update a *.jnlp file with the proper jar files
MKJNLP =		$(PTII)/bin/mkjnlp


# JNLP files that do the actual installation
JNLPS =	vergilDSP.jnlp vergilPtiny.jnlp  vergilPtinySandbox.jnlp vergil.jnlp 

jnlp_all: $(SIGNED_LIB_JARS) $(JNLPS) jnlp_sign
jnlps: $(SIGNED_LIB_JARS) $(JNLPS)
jnlp_clean: 
	rm -f $(JNLPS)
jnlp_distclean: jnlp_clean
	rm -f  $(ALL_JNLP_JARS)

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
STOREPASSWORD = -storepass this.is.not.secure,it.is.for.testing.only
KEYPASSWORD = -keypass this.is.not.secure,it.is.for.testing.only
KEYTOOL = $(PTJAVA_DIR)/bin/keytool
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
		$(STOREPASSWORD)
	"$(KEYTOOL)" -list \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD)

# vergil*.jnlp is for Web Start.  For jar signing to work with Web Start,
# Web Start: DSP version of Vergil - No sources or build env.
vergilDSP.jnlp: vergilDSP.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(DSP_MAIN_JAR) \
		$(DSP_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(DSP_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(DSP_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(DSP_MAIN_JAR) $(KEYALIAS)

# Web Start: Ptiny version of Vergil - No sources or build env.
vergilPtiny.jnlp: vergilPtiny.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(PTINY_MAIN_JAR) \
		$(PTINY_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(PTINY_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(PTINY_MAIN_JAR) $(KEYALIAS)


# Web Start: Ptiny version of Vergil - No sources or build env., in a sandbox
vergilPtinySandbox.jnlp: vergilPtinySandbox.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(PTINY_SANDBOX_MAIN_JAR) \
		$(PTINY_SANDBOX_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(PTINY_SANDBOX_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(PTINY_SANDBOX_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(PTINY_SANDBOX_MAIN_JAR) $(KEYALIAS)


# Web Start: Full Runtime version of Vergil - No sources or build env.
vergil.jnlp: vergil.jnlp.in
	sed 	-e 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' \
		-e 's%@PTVERSION@%$(PTVERSION)%' \
			$< > $@
	@echo "# Adding jar files to $@"
	-chmod a+x "$(MKJNLP)"
	"$(MKJNLP)" $@ \
		$(NUMBER_OF_JARS_TO_LOAD_EAGERLY) \
		$(FULL_MAIN_JAR) \
		$(FULL_JNLP_JARS)
	@echo "# Updating JNLP-INF/APPLICATION.JNLP with $@"
	rm -rf JNLP-INF
	mkdir JNLP-INF
	cp $@ JNLP-INF/APPLICATION.JNLP
	@echo "# $(FULL_MAIN_JAR) contains the main class"
	"$(JAR)" -uf $(FULL_MAIN_JAR) JNLP-INF/APPLICATION.JNLP
	rm -rf JNLP-INF
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(FULL_MAIN_JAR) $(KEYALIAS)


jnlp_sign: $(JNLPS) $(KEYSTORE)
	set $(ALL_NON_APPLICATION_JNLP_JARS); \
	for x do \
		echo "# Signing '$$x'."; \
		"$(PTJAVA_DIR)/bin/jarsigner" \
			-keystore $(KEYSTORE) \
			$(STOREPASSWORD) \
			$$x $(KEYALIAS); \
	done;


# Jar files that we copy befor signing so as to avoid problems with cvs
# Each of the jar files below should be listed in $(SIGNED_LIB_JARS)
$(SIGNED_DIR)/lib/diva.jar: lib/diva.jar
		if [ ! -d $(SIGNED_DIR)/lib ]; then \
			mkdir -p $(SIGNED_DIR)/lib; \
		fi
		cp $< $@

$(SIGNED_DIR)/lib/jasminclasses.jar: lib/jasminclasses.jar
		if [ ! -d $(SIGNED_DIR)/lib ]; then \
			mkdir -p $(SIGNED_DIR)/lib; \
		fi
		cp $< $@

$(SIGNED_DIR)/lib/matlab.jar: lib/matlab.jar
		if [ ! -d $(SIGNED_DIR)/lib ]; then \
			mkdir -p $(SIGNED_DIR)/lib; \
		fi
		cp $< $@

$(SIGNED_DIR)/lib/matlabWindows.jar: lib/matlabWindows.jar
		if [ ! -d $(SIGNED_DIR)/lib ]; then \
			mkdir -p $(SIGNED_DIR)/lib; \
		fi
		cp $< $@

$(SIGNED_DIR)/lib/sootclasses.jar: lib/sootclasses.jar
		if [ ! -d $(SIGNED_DIR)/lib ]; then \
			mkdir -p $(SIGNED_DIR)/lib; \
		fi
		cp $< $@




sign_jar: 
	"$(PTJAVA_DIR)/bin/jarsigner" \
		-keystore $(KEYSTORE) \
		$(STOREPASSWORD) \
		$(JARFILE) $(KEYALIAS)


# Verify the jar files.  This is useful for debugging if you are
# getting errors about unsigned applications
 
jnlp_verify:
	set $(ALL_JNLP_JARS); \
	for x do \
		echo "$$x"; \
		"$(PTJAVA_DIR)/bin/jarsigner" -verify $$x; \
	done;

# Update a location with the files necessary to download
DIST_BASE = ptolemyII/ptII2.0/jnlp
DIST_DIR = /vol/ptolemy/pt0/ptweb/$(DIST_BASE)
DIST_URL = http://ptolemy.eecs.berkeley.edu:/$(DIST_BASE)
OTHER_FILES_TO_BE_DISTED = doc/img/PtolemyIISmall.gif
# make jnlp_dist STOREPASSWORD="-storepass xxx"
jnlp_dist:
	rm -f $(JNLPS)
	$(MAKE) KEYSTORE=/users/ptII/adm/certs/ptkeystore \
		KEYALIAS=ptolemy KEYPASSWORD= \
		PTII_LOCALURL=$(DIST_URL) $(JNLPS)
	tar -cf - $(ALL_JNLP_JARS) $(JNLPS) \
		$(OTHER_FILES_TO_BE_DISTED) | \
		(cd $(DIST_DIR); tar -xpf -)
	cp doc/webStartHelp.htm $(DIST_DIR)
