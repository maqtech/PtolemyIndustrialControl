/* An application that uses Ptolemy II SDF domain to demonstrate the AudioSource actor.

 Copyright (c) 1999 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.javasound.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.applet.Applet;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;


//////////////////////////////////////////////////////////////////////////
//// AudioSourceDemo
/**
A simple application demonstrating the use of the AudioSource actor.
Note that AudioSource will not work unless a Java Sound API implementation
is installed.

@author Brian K. Vogel
*/
public class AudioSourceDemo extends SDFApplet {   


    /** After invoking super.init(), create and connect the actors.
     */
    public void init() {
        super.init();
        // The 1 argument requests a go and a stop button.
        add(_createRunControls(2));	

	try {
	    AudioSource soundData = new AudioSource(_toplevel, "soundData");
	    // Specify where to get the sound file.
	    soundData.pathName.setToken(new StringToken("http://ptolemy.eecs.berkeley.edu/~vogel/tempjava/flute+hrn+mrmba.au"));
	    soundData.isPeriodic.setToken(new BooleanToken(false));

            // Create and configure plotter
            SequencePlotter myplot = new SequencePlotter(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setTitle("Audio Data");
            myplot.plot.setXRange(0.0, 200.0);
            myplot.plot.setWrap(true);
            myplot.plot.setYRange(-1.3, 1.3);
            myplot.plot.setMarksStyle("none");
            myplot.plot.setPointsPersistence(512);
            myplot.plot.setSize(500, 300);

            _toplevel.connect(soundData.output, myplot.input);
 
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }

    }

    // Now override some methods of PtolemyApplet. This needs to be done
    // so that a NullPointer exception will not be thrown when an application
    // is run. The changes involve catching NullPointer exceptions thrown by
    // getParameter() and replacing Applet methods by non-Applet methods.

    
    /** Report that execution of the model has finished.  This is
     *  called by the manager.
     *  @param manager The manager in charge of the execution.
     */
    public void executionFinished(Manager manager) {
	// showStatus() is an Applet method. If showStatus() is called
	// from within an application, it will throw a nullpointer exception.
	// Therefore, I comment it out and print to standard out instead so
	// that applications will run. Maybe I should put a try {} catch {} here
	// to handle the nullpointerexceptions.
      // FIXME: implement showStatus() in the applet stub.
        //showStatus("Execution finished.");
	System.out.println("Execution finished.");
    }


    /** Report that the manager state has changed.  This is
     *  called by the manager.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newState = manager.getState();
        if (newState != _previousState) {
	    // showStatus() is an Applet method. If showStatus() is called
	    // from within an application, it will throw a nullpointer exception.
	    // Therefore, I comment it out and print to standard out instead so
	    // that applications will run. Maybe I should put a try {} catch {} here
	    // to handle the nullpointerexceptions.
	    //showStatus(manager.getState().getDescription());
	  // FIXME: implement showStatus() in the applet stub.
	    System.out.println(manager.getState().getDescription());
            
            _previousState = newState;
        }
    }
    
    static public void main (String argv[]) {
	final Applet applet = new AudioSourceDemo();
	System.runFinalizersOnExit(true);
	Frame frame = new Frame ("AudioSourceDemo");
	frame.addWindowListener (new WindowAdapter()
	    {
		public void windowClosing (WindowEvent event)
		{
		    applet.stop();
		    applet.destroy();
		    System.exit(0);
		}
	    });
	frame.add ("Center", applet);
	applet.setStub (new MyAppletStub (argv, applet));
	frame.show();
	applet.init();
	applet.start();
	frame.pack();
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Used by overriden method from PtolemyApplet
    private Manager.State _previousState;
            
}

