# Makefile stub used to create fmus
#
# @Author: Christopher Brooks (makefile only)
#
# @Version: $Id$
#
# @Copyright (c) 2013-2015 The Regents of the University of California.
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

# PTJNI_SHAREDLIBRARY_SUFFIX2 is set in $PTII/mk/ptII.mk by configure to .dylib for Mac OS X.
include $(PTII)/mk/ptII.mk

# The fmus in the fmus/ directory should have a makefile that looks like
# FMU_NAME = dqME1
# include ../fmus.mk

PIC = -fPIC -O0 -falign-functions -march=native
INCLUDE = -I"$(OPENMODELICAHOME)/include/omc/c" -I"$(OPENMODELICAHOME)/include/omc/c/fmi2" -I. 
LDFLAGS = -llapack -lblas -L/opt/local/lib/gcc49 -L"$(OPENMODELICAHOME)/lib/omc"  -Wl,-rpath,'$(OPENMODELICAHOME)/lib/omc' -lSimulationRuntimeC  -L/usr/local/omniORB-4.2.0/lib -lSimulationRuntimeC -L/usr/lib64 -llapack -lblas -lm -lgc -lpthread -rdynamic -L. -llis

FMU_SRCS = $(CFILES) $(FMU_NAME).xml

# Don't want $(FMU_NAME)_info.c
#CFILES := \
#	$(wildcard $(FMU_NAME)_[0-9][0-9]*.c) \
#	$(wildcard $(FMU_NAME)_functions.c) \
#	$(wildcard $(FMU_NAME)_records.c) \
#	$(FMU_NAME)_FMU.c \
#	$(FMU_NAME).c 

#	$(wildcard $(FMU_NAME)_me_FMU_[0-9][0-9]*.c)

OFILES := $(CFILES:%.c=%.o)

all: $(FMU_NAME).fmu #modelDescription.xml

$(FMU_NAME).fmu: $(FMU_NAME).mos $(FMU_NAME).mo
	omc $(FMU_NAME).mos
	# Rebuild and avoid various crashes.
	rm -rf tmp
	mkdir tmp
	# Add -lrt for VanDerPol
	(cd tmp; unzip ../$(FMU_NAME).fmu; cd sources;env;LDFLAGS= ./configure; ../../../fixOmcMakefile; LDFLAGS= make)

# # CBITSFLAGS is set to -m32 to build linux32 fmus
# %.o: %.c
# 	$(CC) $(CBITSFLAGS) $(USER_CFLAGS) $(PIC) $(INCLUDE) $(CFLAGS) -c -o $@ $< 


# # Export the fmu. $(FMU_NAME).xml is a dervied file created by omc
# #export: modelDescription.xml
# modelDescription.xml: $(FMU_NAME).mos
# 	omc $(FMU_NAME).mos

# $(OFILES): $(CFILES)

# $(FMU_NAME)_FMU.c: modelDescription.xml

# Include definitions and rules common to sparse fmi and regular
# cosimulation.
include $(PTII)/ptolemy/actor/lib/fmi/fmus/omc/fmuBase.mk


# # Compile the files generated by 'make export'.  Typically, this is
# # done if there are changes made to the .c or .cpp files
# $(FMU_NAME).so.*: $(OFILES)
# 	$(CXX) -shared -I. -o $(FMU_NAME).so $(OFILES) $(PIC) $(INCLUDES) $(LDFLAGS)

# # Compile the files generated by 'make export'.  Typically, this is
# # done if there are changes made to the .c or .cpp files
# $(FMU_NAME).dylib: $(OFILES)
# 	$(CXX) -shared -I. -o $(FMU_NAME).dylib $(OFILES) $(PIC) $(INCLUDES) $(LDFLAGS)

# BITS=64
# ARCH=$(PTJNI_ARCHITECTURE)$(BITS)


# # PTJNI_SHAREDLIBRARY_SUFFIX2 is set in $PTII/mk/ptII.mk by configure to .dylib for Mac OS X.
# # $(FMU_NAME).fmu: modelDescription.xml $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX2)
# # 	mkdir -p $(FMU_NAME).fmutmp
# # 	mkdir -p $(FMU_NAME).fmutmp/binaries
# # 	mkdir -p $(FMU_NAME).fmutmp/binaries/$(ARCH)
# # 	mkdir -p $(FMU_NAME).fmutmp/sources
# # 	# Under Linux, $(PTJNI_SHAREDLIBRARY_SUFFIX2 )is .so.*, which causes problems.
# # 	if [ -f $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX) ]; then \
# # 		echo "cp $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX) $(FMU_NAME).fmutmp/binaries/$(ARCH)/"; \
# # 		cp $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX) $(FMU_NAME).fmutmp/binaries/$(ARCH)/; \
# # 	fi
# # 	if [ -f $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX2) ]; then \
# # 		echo "cp $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX2) $(FMU_NAME).fmutmp/binaries/$(ARCH)/"; \
# # 		cp $(FMU_NAME).$(PTJNI_SHAREDLIBRARY_SUFFIX2) $(FMU_NAME).fmutmp/binaries/$(ARCH)/; \
# # 	fi
# # 	cp $(FMU_NAME)_FMU.libs $(FMU_NAME).fmutmp/binaries/$(ARCH)/
# # 	cp $(FMU_NAME)_FMU.c $(FMU_NAME)_FMU.makefile $(FMU_NAME)_literals.h $(FMU_NAME)_model.h $(FMU_NAME)_includes.h $(FMU_NAME)_functions.h  $(FMU_NAME)_11mix.h $(FMU_NAME)_12jac.h $(FMU_NAME)_13opt.h $(FMU_NAME)_init.c $(FMU_NAME)_info.c $(FMU_NAME).c $(FMU_NAME)_functions.c $(FMU_NAME)_records.c $(FMU_NAME)_01exo.c $(FMU_NAME)_02nls.c $(FMU_NAME)_03lsy.c $(FMU_NAME)_04set.c $(FMU_NAME)_05evt.c $(FMU_NAME)_06inz.c $(FMU_NAME)_07dly.c $(FMU_NAME)_08bnd.c $(FMU_NAME)_09alg.c $(FMU_NAME)_10asr.c $(FMU_NAME)_11mix.c $(FMU_NAME)_12jac.c $(FMU_NAME)_13opt.c $(FMU_NAME)_14lnz.c $(FMU_NAME)_FMU.libs modelDescription.xml $(FMU_NAME).fmutmp/sources/
# # 	cp modelDescription.xml $(FMU_NAME).fmutmp/modelDescription.xml
# # 	(cd $(FMU_NAME).fmutmp; rm -f ../$(FMU_NAME).fmu; zip -r ../$(FMU_NAME).fmu *)
# # 	rm -rf $(FMU_NAME).fmutmp

# Get the rest of the rules
include $(PTII)/mk/ptcommon.mk

KRUFT = $(FMU_NAME)_* $(FMU_NAME).fmu $(FMU_NAME).c fmuTmp* modelDescription.xml binaries sources *.so *.dylib *.o src/binaries $(FMU_NAME).libs $(FMU_NAME).log $(FMU_NAME).fmutmp tmp
