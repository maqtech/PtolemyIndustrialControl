# Tests for the PlotBox class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test PlotBox-1.1 {} {
    set frame [java::new java.awt.Frame]
    set plot [java::new ptolemy.plot.PlotBox]
    $frame pack
    $frame {add java.lang.String java.awt.Component} "Center" $plot
    $frame setSize 500 300
    $frame show
    $frame repaint
    $plot setTitle "foo"
    $plot addXTick "X tick" -10
    $plot addYTick "Y tick" 10.1
    $plot setGrid false
    $plot repaint
    $plot clear true
    $plot repaint
    $plot fillPlot
} {}

test PlotBox-1.2 {addLegend, getLegend} {
    $plot addLegend 1 "A Legend"
    $plot addLegend 2 "Another Legend"
    $plot addLegend 3 "3rd Legend"
    $plot getLegend 2
} {Another Legend}

test PlotBox-2.1 {setDataurl, getDataurl, getDocumentBase} {
    $plot setDataurl ../demo/data.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    list $url [java::isnull $docbase]
} {../demo/data.plt 1}


test PlotBox-2.2 {setDataurl, getDataurl, getDocumentBase} {
    $plot setDataurl http://notasite/bar/foo.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    list $url [java::isnull $docbase]
} {http://notasite/bar/foo.plt 1}

# FIXME: This seems to be testing features of the AWT, not of PlotBox.
# test PlotBox-3.1 {getMinimumSize getPreferredSize} {
#     $frame setSize 425 600
#     $frame repaint
#     set minimumDimension [$plot getMinimumSize] 
#     set preferredDimension [$plot getPreferredSize]
#     list [java::field $minimumDimension width] \
# 	    [java::field $minimumDimension height] \
# 	    [java::field $preferredDimension width] \
# 	    [java::field $preferredDimension height]
# } {}

test PlotBox-4.1 {parseFile} {
    $plot parseFile ../demo/data.plt
} {}

test PlotBox-4.5 {read} {
    set file [java::new {java.io.File java.lang.String java.lang.String} \
	    "../demo" "bargraph.plt"]
    set fileInputStream \
	    [java::new {java.io.FileInputStream java.lang.String} "../demo/bargraph.plt"]
    $plot read $fileInputStream
} {}

test PlotBox-5.1 {getColorByName} {
    set color [$plot getColorByName "red"]
    $color toString
} {java.awt.Color[r=255,g=0,b=0]}


test PlotBox-6.1 {setButtons} {
    $plot setButtons false
    $plot repaint
    $plot setButtons true
    $plot repaint
} {}

test PlotBox-7.1 {setSize} {
    $plot setSize 420 420
    $plot repaint
    $plot setSize 400 440
    $plot repaint
} {}

test PlotBox-8.1 {setBackground} {
    set color [$plot getColorByName "red"]
    $plot setBackground $color
    set color [$plot getColorByName "green"]
    $plot setForeground $color
    $plot repaint
} {}

test PlotBox-9.1 {setGrid} {
    $plot setGrid false
    $plot repaint
    $plot setGrid true
    $plot repaint
} {}

test PlotBox-9.1 {setLabelFont} {
    $plot setLabelFont helvetica-ITALIC-20
    $plot repaint
} {}

test PlotBox-10.1 {setTitleFont} {
    $plot setTitleFont Courier-BOLD-16
    $plot repaint
} {}

test PlotBox-11.1 {setXLog} {
    $plot setXLog true
    $plot setYLog true
    $plot repaint
} {}

test PlotBox-12.1 {setXRange} {
    $plot setXRange 0.001 10
    $plot setYRange 1 1000
    $plot repaint
} {}

test PlotBox-13.1 {zoom} {
    $plot zoom 1 2 3 4
    $plot repaint
} {}

test PlotBox-14.1 {write} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    $plot write $printStream xxx
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                [$stream toString] "\n" output

    list $output
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "xxx">
<plot>
<!-- Ptolemy plot, version 3.1, PlotML format. -->
<title>Software Downloads</title>
<xLabel>Year</xLabel>
<yLabel>Downloads</yLabel>
<xRange min="0.0010" max="10.0"/>
<yRange min="1.0" max="1000.0"/>
<xTicks>
  <tick label="1993" position="0.0"/>
  <tick label="1994" position="1.0"/>
  <tick label="1995" position="2.0"/>
  <tick label="1996" position="3.0"/>
  <tick label="1997" position="4.0"/>
  <tick label="1998" position="5.0"/>
  <tick label="1999" position="6.0"/>
  <tick label="2000" position="7.0"/>
  <tick label="2001" position="8.0"/>
  <tick label="2002" position="9.0"/>
  <tick label="2003" position="10.0"/>
</xTicks>
<xLog/>
<yLog/>
<noColor/>
</plot>
}}
