# Tests for the Audio
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1999 The Regents of the University of California.
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

####
#
test Audio-2.1 {Audio(byte []): Generate a .au file from a byte array} {
    set audioByteArray [java::new {byte[]} 5 {-127 -63 0 63 127 }]

    set audio [java::new {ptolemy.media.Audio {byte[]}} $audioByteArray]
    set fos [java::new java.io.FileOutputStream "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    # It would be nice if we could diff two files somehome
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 37
size = 5
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio} 42}

####
#
test Audio-2.2 {Audio(double []): Generate a .au file from a double array} {
    set audioDoubleArray [java::new {double[]} 5 {-1.0 0.5 0 0.5 1.0}]

    set audio [java::new {ptolemy.media.Audio {double[]}} $audioDoubleArray]
    set fos [java::new java.io.FileOutputStream "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    # It would be nice if we could diff two files somehome
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 37
size = 5
format code = 1
sampleRate = 8000
number of channels = 1
info field = Ptolemy audio} 42}


######################################################################
####
#
test Audio-2.3 {Audio(DataInputStream) && write(DataOutputStream): 
                          Read in an audio file, write it back out} {
    set fis [java::new java.io.FileInputStream "bark.au"]
    set dis [java::new java.io.DataInputStream $fis]
    set audio [java::new ptolemy.media.Audio $dis]

    set fos [java::new java.io.FileOutputStream "tmp.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio write $dos
    $dos close
    # It would be nice if we could diff these two files somehome
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark    } 2407}

####
#
test Audio-3.2 {writeRaw(DataOutputStream): 
                     Read in an audio file, write it back out as raw data} {
    # Use the $audio from Audio-2.1
    set fos [java::new java.io.FileOutputStream "tmp.raw"]
    set dos [java::new java.io.DataOutputStream $fos]
    $audio writeRaw $dos
    $dos close
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark    } 2367}



####
#
test Audio-4.1 {writeAudio(): Generate a .au file from an array} {
    set audioArray [java::new {double[]} 5 {-1.0 0.5 0 0.5 1.0}]

    set fos [java::new java.io.FileOutputStream "array.au"]
    set dos [java::new java.io.DataOutputStream $fos]
    java::call ptolemy.media.Audio writeAudio $audioArray $dos
    list [$audio toString] [$dos size]
} {{file ID tag = .snd
offset = 40
size = 2367
format code = 1
sampleRate = 8000
number of channels = 1
info field = terrier bark    } 42}




