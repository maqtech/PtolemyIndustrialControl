/* Java implementation of the X11 pxgraph plotting program

 Copyright (c) 1997 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
*/

package plot;

import java.awt.Button;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.Thread;
import java.lang.InterruptedException; 

import java.net.URL;
import java.net.MalformedURLException;

//////////////////////////////////////////////////////////////////////////
//// Pxgraph
/** 
This class is a Java application that uses the Plot Java applet to
simulate the <code>pxgraph</code> X Windows system program.  
<p>
The <code>pxgraph</code> script is a Bourne shell script that
attempts to call Java with the proper environment.  The 
<code>pxgraph</code> script has the following usage:
<br>
<code>pxgraph <i>[ options ]  [ =WxH+X+Y ] [file . . .]</i></code>
<p>
Below we describe the <code>pxgraph</code> arguments.  The
text is based on the <code>xgraph</code> Unix man page written
by David Harrison (University of California).
To see the command line options, you can type
<code>pxgraph -help</code>.
<p>
The <code>pxgraph</code> program draws a graph on a display given data
read from either data files or from standard input if no
files are specified. It can display up to 64 independent
data sets using different colors and/or line styles for each
set. It annotates the graph with a title, axis labels,
grid lines or tick marks, grid labels, and a legend. There
are options to control the appearance of most components of
the graph.
<P>
The input format is similar to <code>graph(<i>1G</i>)</code> but differs
slightly. The data consists of a number of <I>data</I> <I>sets</I>. Data
sets are separated by a blank line. A new data set is also
assumed at the start of each input file. A data set consists
of an ordered list of points of the form &quot;{directive}
X Y". The directive is either &quot;draw&quot; or &quot;move&quot; and can be
omitted. If the directive is &quot;draw", a line will be drawn
between the previous point and the current point (if a line
graph is chosen). Specifying a &quot;move&quot; directive tells
xgraph not to draw a line between the points. If the directive
is omitted, &quot;draw&quot; is assumed for all points in a data
set except the first point where &quot;move&quot; is assumed. The
&quot;move&quot; directive is used most often to allow discontinuous
data in a data set. 

After <code>pxgraph</code> has read the data, it will create a new window
to graphically display the data.

Once the window has been opened, all of the data sets will
be displayed graphically (subject to the options explained
below) with a legend in the upper right corner of the
screen. To zoom in on a portion of the graph, depress a
mouse button in the window and sweep out a region. <code>pxgraph</code>
will then open a new window looking at just that portion of
the graph. <code>pxgraph</code> also presents three control buttons in
the upper left corner of each window: <I>Close</I>, 
<I>About</I> and <I>Fill</I>.
<p>
<I>pxgraph</I> accepts a large number of commmand line options.
A list of these options is given below.
<code>=WxH+X+Y</code> <BR>

Specifies the initial size and location of the xgraph
window. <code>-<i>&lt;digit&gt; &lt;name&gt;</i></code>
 These options specify the data
set name for the corresponding data set. The digit
should be in the range `0' to `63'. This name will be
used in the legend.
<P>
<DL>
<DT><code>-bar</code> <DD>
Specifies that vertical bars should be drawn from the
data points to a base point which can be specified with
<code>-brb</code>.
Usually, the <code>-nl</code> flag is used with this option.
The point itself is located at the center of the bar.
<P>
</DD>
</DL>
<DL>
<DT><code>-bb</code> <DD>
Draw a bounding box around the data region. This is
very useful if you prefer to see tick marks rather than
grid lines (see <code>-tk</code>).
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-bd</code> <code><i>&lt;color&gt;</i></code> <DD>
This specifies the border color of the <code>pxgraph</code> window.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-bg</code> <code><i>&lt;color&gt;</i></code> <DD>
Background color of the <code>pxgraph</code> window.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-binary</code><DD>
Data files are in binary format.  The <code>-binary</code>
argument is the primary difference between <code>xgraph</code>
and <code>pxgraph</code>.  The 
<A HREF="http://ptolemy.eecs.berkeley.edu">Ptolemy Project</A> software
makes extensive use of <code>-binary</code>.
<br>The plot commands are encoded as single characters, and the numeric data 
is a 4 byte float.  
 <br>The commands are encoded as follows:
<DL>
<DT> <CODE>d <I>&lt;4byte float&gt; &lt;4byte float&gt;</I></CODE>
<DD> Draw a X,Y point
<DT> <CODE>e</CODE>
<DD> End of dataset
<DT> <CODE>n <I>&lt;dataset name&gt;</I>&#92n</CODE>
<DD> New dataset name, ends in <CODE>&#92n</CODE>
<DT> <CODE>m <I>&lt;4byte float&gt; &lt;4byte float&gt;</I></CODE>
<DD> Move to a X,Y point.
</DL>
 <br>To view a binary plot file under unix, we can use the 
<CODE>od</CODE> command.  Note that the first character is a <CODE>d</CODE>
followed by eight bytes of data consisting of two floats of four bytes.
<PRE>
cxh@carson 324% od -c data/integrator1.plt
0000000   d  \0  \0  \0  \0  \0  \0  \0  \0   d   ? 200  \0  \0   ? 200
0000020  \0  \0   d   @  \0  \0  \0   @   , 314 315   d   @   @  \0  \0

</PRE>

<P>
</DD>
</DL>

<DL>
<DT><code>-brb</code> <code><i>&lt;base&gt;</i></code> <DD>
This specifies the base for a bar graph. By default,
the base is zero.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-brw</code> <code><i>&lt;width&gt;</i></code> <DD>
This specifies the width of bars in a bar graph. The
amount is specified in the user's units. By default,
a bar one pixel wide is drawn.
<P>
</DD>
</DL>
<DL>
<DT><code>-bw</code> <code><i>&lt;size&gt;</i></code> <DD>
Border width (in pixels) of the <code>pxgraph</code> window.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-db</code> <DD>
Causes xgraph to run in synchronous mode and prints out
the values of all known defaults.
<P>
</DD>
</DL>
<DL>
<DT><code>-fg</code> <code><i>&lt;color&gt;</i></code> <DD>
Foreground color. This color is used to draw all text
and the normal grid lines in the window.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-gw</code> <DD>
Width, in pixels, of normal grid lines.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-gs</code> <DD>
Line style pattern of normal grid lines.
<P>
</DD>
</DL>
<DL>
<DT><code>-lf</code> <code><i>&lt;fontname&gt;</i></code> <DD>
Label font. All axis labels and grid labels are drawn
using this font. A font name may be specified exactly
(e.g. &quot;9x15&quot; or &quot;-*-courier-bold-r-normal-*-140-*") or
in an abbreviated form: &lt;family&gt;-&lt;size&gt;. The family is
the family name (like helvetica) and the size is the
font size in points (like 12). The default for this
parameter is &quot;helvetica-12".
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-lnx</code> <DD>
Specifies a logarithmic X axis. Grid labels represent
powers of ten.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-lny</code> <DD>
Specifies a logarithmic Y axis. Grid labels represent
powers of ten.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-lw</code> <code><i>width</i></code> <DD>
Specifies the width of the data lines in pixels. The
default is zero.
<b>Unsupported in the Java version.</b>
<P>
</DD>
</DL>
<DL>
<DT><code>-lx</code> <code><i>&lt;xl,xh&gt;</i></code> <DD>
This option limits the range of the X axis to the
specified interval. This (along with -ly) can be used
to &quot;zoom in&quot; on a particularly interesting portion of a
larger graph.
<P>
</DD>
</DL>
<DL>
<DT><code>-ly</code> <code><i>&lt;yl,yh&gt;</i></code> <DD>
This option limits the range of the Y axis to the
specified interval.
<P>
</DD>
</DL>
<DL>
<DT><code>-m</code> <DD>
Mark each data point with a distinctive marker. There
are eight distinctive markers used by xgraph. These
markers are assigned uniquely to each different line
style on black and white machines and varies with each
color on color machines.
<P>
</DD>
</DL>
<DL>
<DT><code>-M</code> <DD>
Similar to -m but markers are assigned uniquely to each
eight consecutive data sets (this corresponds to each
different line style on color machines).
<P>
</DD>
</DL>
<DL>
<DT><code>-nl</code> <DD>
Turn off drawing lines. When used with -m, -M, -p, or
-P this can be used to produce scatter plots. When
used with -bar, it can be used to produce standard bar
graphs.
<P>
</DD>
</DL>
<DL>
<DT><code>-p</code> <DD>
Marks each data point with a small marker (pixel
sized). This is usually used with the -nl option for
scatter plots.
<P>
</DD>
</DL>
<DL>
<DT><code>-P</code> <DD>
Similar to -p but marks each pixel with a large dot.
<P>
</DD>
</DL>
<DL>
<DT><code>-rv</code> <DD>
Reverse video. On black and white displays, this will
invert the foreground and background colors. The
behaviour on color displays is undefined.
<P>
</DD>
</DL>
<DL>
<DT><code>-t</code> <code><i>&lt;string&gt;</i></code> <DD>
Title of the plot. This string is centered at the top
of the graph.
<P>
</DD>
</DL>
<DL>
<DT><code>-tf</code> <code><i>&lt;fontname&gt;</i></code> <DD>
Title font. This is the name of the font to use for
the graph title. A font name may be specified exactly
(e.g. &quot;9x15&quot; or &quot;-*-courier-bold-r-normal-*-140-*") or
in an abbreviated form: &lt;family&gt;-&lt;size&gt;. The family is
the family name (like helvetica) and the size is the
font size in points (like 12). The default for this
parameter is &quot;helvetica-18".
<P>
</DD>
</DL>
<DL>
<DT><code>-tk</code> <DD>
This option causes <code>pxgraph</code> to draw tick marks rather
than full grid lines. The -bb option is also useful
when viewing graphs with tick marks only.
<P>
</DD>
</DL>
<DL>
<DT><code>-x</code>  <code><i>&lt;unitname&gt;</i></code> <DD>
This is the unit name for the X axis. Its default is
&quot;X".
<P>
</DD>
</DL>
<DL>
<DT><code>-y</code> <code><i>&lt;unitname&gt;</i></code> <DD>
This is the unit name for the Y axis. Its default is
&quot;Y".
<P>
</DD>
</DL>
<DL>
<DT><code>-zg</code> <code><i>&lt;color&gt;</i></code> <DD>
This is the color used to draw the zero grid line.
<P>
</DD>
</DL>
<DL>
<DT><code>-zw</code> <code><i>&lt;width&gt;</i></code> <DD>
This is the width of the zero grid line in pixels.
<P>
</DD>
</DL>

<H2> Compatibility Issues </H2>
<li>The original <code>xgraph</code> program allowed many formatting
directives inside the file.  This version only supports
<code>draw</code> and <code>move</code>.
<li>This version does not support X resources.
<li>Hardcopy is not yet supported.
<li>Typing <code>Control-D</code> does not close a window.
 * @author Christopher Hylands
 * @version $Id$
 * @see Plot
 */
public class Pxgraph extends Frame { 

    /** Constructor
     */	
    public Pxgraph() {
	//        setLayout(new FlowLayout(FlowLayout.RIGHT));
	//	_exitButton = new Button();
	//	_exitButton.setLabel("Exit");
	//	add(_exitButton);
    }

    //    public boolean action(Event e, Object arg) {
    //	Object target = e.target;
    //	if (target == _exitButton) {
    //	    System.exit(1);
    //	    return true;
    //	} else {
    //            return super.action (e, arg);
    //	}
    //    }

    /** handle an event.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public boolean handleEvent(Event e) {
        switch (e.id) {
          case Event.WINDOW_ICONIFY:
	      //stopAnimation();
            break;
          case Event.WINDOW_DEICONIFY:
	      //startAnimation();
            break;
          case Event.WINDOW_DESTROY:
            System.exit(0);
            break;
        }  

        return super.handleEvent(e); // FIXME: handleEvent is
 	// deprecated in 1.1, we should use processEvent(),
	// However, we need to compile under 1.0.2 for compatibility with
	// netscape3.x so we stick with handleEvent().
    }

    /** Parse the command line arguments, do any preprocessing, then plot.
      * If you have the <code>pxgraph</code> shell script, then 
      * type <code>pxgraph -help</code> for the complete set of arguments.
      */
    public static void main(String args[]) {
        int argsread = 0, i;
	Plot plotApplet = new Plot();
	Pxgraph pxgraph = new Pxgraph();

	pxgraph.pack();
	pxgraph.add(plotApplet);

	try {
	    // First we parse the args for things like -help or -version
	    // then we have the Plot applet parse them
	    pxgraph._parseArgs(args);
	    argsread = plotApplet.parseArgs(args);
	} catch (CmdLineArgException e) {
	    System.err.println("Failed to parse command line arguments: "
			       + e);
	    System.exit(1);
	}

        pxgraph.show();
	plotApplet.init();
	plotApplet.start();

	if (_test) {
	    if (_debug > 4) System.out.println("Sleeping for 2 seconds");
	    try {
		Thread.currentThread().sleep(2000);
	    }
	    catch (InterruptedException e) {
	    }
	    System.exit(0);
	}
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    /* help - print out help
     */	
    private void _help () {
	// FIXME: we should bring up a dialog box or something.
	// We use a table here to keep things neat.
	// If we have:
	//  {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
	// -bd       - The argument
	// <color>   - The description of the value of the argument
	// Border    - The Xgraph file directive (not supported at this time).
	// White     - The default (not supported at this time)
	// "(Unsupported)" - The string that is printed to indicate if
	//                   a option is unsupported.
	String commandOptions[][] = {
	    {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
	    {"-bg",  "<color>", "BackGround",  "White", "(Unsupported)"},
	    {"-brb", "<base>", "BarBase",  "0", "(Unsupported)"},
	    {"-brw", "<width>", "BarWidth",  "1", ""},
	    {"-bw",  "<size>", "BorderSize",  "1", "(Unsupported)"},
	    {"-fg",  "<color>", "Foreground",  "Black", "(Unsupported)"},
	    {"-gw",  "<pixels>", "GridStyle",  "1", "(Unsupported)"},
	    {"-lf",  "<fontname>", "LabelFont",  "helvetica-12", "(Unsupported)"},
	    {"-lw",  "<width>", "LineWidth",  "0", "(Unsupported)"},
	    {"-lx",  "<xl,xh>", "XLowLimit, XHighLimit",  "0", ""},
	    {"-ly",  "<yl,yh>", "YLowLimit, YHighLimit",  "0", ""},
	    {"-t",   "<title>", "TitleText",  "An X Graph", ""},
	    {"-tf",  "<fontname>", "TitleFont",  "helvetica-18", "(Unsupported)"},
	    {"-x",   "<unitName>", "XUnitText",  "X", ""},
	    {"-y",   "<unitName>", "YUnitText",  "Y", ""},
	    {"-zg",  "<color>", "ZeroColor",  "Black", "(Unsupported)"},
	    {"-zw",  "<width>", "ZeroWidth",  "0", "(Unsupported)"},
	};

	String commandFlags[][] = {
	    {"-bar", "BarGraph",  ""},
	    {"-bb", "BoundBox",  "(Unsupported)"},
	    {"-binary", "Binary",  ""},
	    {"-db", "Debug",  ""},
	    // -help is not in the original X11 pxgraph.
	    {"-help", "Help",  ""},
	    {"-lnx", "LogX",  "(Unsupported)"},
	    {"-lny", "LogY",  "(Unsupported)"},
	    {"-m", "Markers",  ""},
	    {"-M", "StyleMarkers",  ""},
	    {"-nl", "NoLines",  ""},
	    {"-p", "PixelMarkers",  ""},
	    {"-P", "LargePixel",  ""},
	    {"-rv", "ReverseVideo",  ""},
	    // -test is not in the original X11 pxgraph.  We use it for testing
	    {"-test", "Test",  ""},
	    {"-tk", "Ticks",  ""},
	    // -v is not in the original X11 pxgraph.
	    {"-v", "Version",  ""},
	    {"-version", "Version",  ""},
	};
	int i;
	System.out.println("Usage: pxgraph [ options ] [=WxH+X+Y] [file ...]");
	System.out.println(" options that take values as second args:");
	for(i=0; i < commandOptions.length; i++) {
	    System.out.println(" " + commandOptions[i][0] +
			       " " + commandOptions[i][1] +
			       " " + commandOptions[i][4] );
	}
	System.out.println(" Boolean flags:");
	for(i=0; i < commandFlags.length; i++) {
	    System.out.println(" " + commandFlags[i][0] +
			       " " + commandFlags[i][2]);
	}
	System.out.println("The following pxgraph features are not supported:");
	System.out.println(" * Directives in pxgraph input files");
	System.out.println(" * Xresources");
	System.out.println(" For complete documentation, see the "+
			   "Pxgraph Java class documentation.");
	System.exit(1);
    }

    /* Parse the arguments and make calls to the plotApplet accordingly.
     */	
    private int _parseArgs(String args[])
	{
        int i = 0, j, argsread;
        String arg;

	// Default URL to be opened
	String dataurl = "";

	String title = "A plot";
	int width = 400;      // Default width of the graph
	int height = 400;     // Default height of the graph


        while (i < args.length && (args[i].startsWith("-") || 
            args[i].startsWith("=")) ) {
            arg = args[i++];
	    if (_debug > 2) System.out.print("Pxgraph: arg = " + arg + "\n");

	    if (arg.startsWith("-")) {
		if (arg.equals("-db")) {
		    _debug = 10;
		} else if (arg.equals("-debug")) {
		    _debug = (int)Integer.valueOf(args[i++]).intValue();
		    continue;
		} else if (arg.equals("-help")) {
		    // -help is not in the original X11 pxgraph.
		    _help();
		    continue;
		} else if (arg.equals("-test")) {
		    // -test is not in the original X11 pxgraph.
		    _test = true;
		    continue;
		} else if (arg.equals("-t")) {
		    // -t <title> TitleText "An X Graph"
		    title =  args[i++];
		    continue;
		} else if (arg.equals("-v") || arg.equals("-version")) {
		    // -version is not in the original X11 pxgraph.
		    _version();
		    continue;
		}
	    } else if (arg.startsWith("=")) {
		// Process =WxH+X+Y
                int endofheight;
                width = (int)Integer.valueOf(arg.substring(1,
					   arg.indexOf('x'))).intValue();
                if (arg.indexOf('+') != -1) {
                   height = 
		       (int)Integer.valueOf(arg.substring(arg.indexOf('x')+1,
					  arg.indexOf('+'))).intValue();
                } else {
                    if (arg.length() > arg.indexOf('x')) {
                        height =
			    Integer.valueOf(arg.substring(arg.indexOf('x')+1,
					  arg.length())).intValue();
                    }
                }
		// FIXME: need to handle X and Y in =WxH+X+Y
		continue;

            }
	}

	// Set up the frame
	resize(width,height); 	// FIXME: resize is deprecated in 1.1,
	                        // we should use setsize(width,height)
				// but setsize is not in JDK1.0.2
	setTitle(title);

        if (i < args.length) {
            dataurl=args[i];
	}
        argsread = i++;

        if (_debug > 2) {
	    System.err.println("Pxgraph: dataurl = " + dataurl);
	    System.err.println("Pxgraph: title = " + title);
	    System.err.println("Pxgraph: width = " + width + 
			       " height = " + height +
			       " _debug = " + _debug);
	}
        return argsread;
    }

    /* version - print out version info
     */	
    private void _version () {
	// FIXME: we should bring up a dialog box or something.
	System.out.println("Pxgraph - (Java implementation) by\n" +
			   "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
			   "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
			   "($Id$)");
	System.exit(0);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private Button _exitButton;

    // For debugging, call with -db or -debug.
    private static int _debug = 0;

    // If true, then auto exit after a few seconds.
    private static boolean _test = false;
}
