/* A live signal plotter.

@Author: Edward A. Lee
@Version: $Id$

@Copyright (c) 1997 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import java.awt.*;
import java.util.*;
import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// PlotLive
/** 
 * Plot signals dynamically, where points can be added at any time and the
 * the display will be updated.  This should be normally used with some
 * finite persistence so that old points are erased as new points are added.
 * Unfortunately, the most efficient way to erase old points is to draw graphics
 * using the "exclusive or" mode, which introduces quite a number of artifacts.
 * When lines are drawn between points, where they overlap the points the line
 * becomes white. Moreover, if two lines or points overlap completely, they disappear.
 * <p>
 * This class is abstract, so it must be used by creating a derived class.
 * To use it, create a derived class with <code>init()</code> and
 * <code>addPoints()</code> methods. The <code>init()</code> method can call methods in the
 * <code>Plot</code> or <code>PlotBox</code> classes
 * (both of which are base classes) to set the static properties of
 * the graph, such as the title, axis ranges, and axis labels. 
 * The <code>addPoints()</code> method should call <code>addPoint()</code>
 * of the <code>Plot</code> base class to dynamically add points to the plot.
 * This method is called within a thread separate from the applet thread.
 * <p>
 * The <code>init()</code> method <i>must</i> call
 * <code>super.init()</code> somewhere in its body;  along with general initialization,
 * this reads a file given by a URL if the dataurl applet parameter is specified.
 * Thus, the initial configuration can be specified in a separate file rather
 * than in Java code.
 */
public abstract class PlotLive extends Plot implements Runnable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
   
    /**
     * Handle button presses to enable or disable plotting.
     */
    public boolean action (Event evt, Object arg) {
        if (evt.target == _startButton) {
            enable();
            return true;
        } else if (evt.target == _stopButton) {
            disable();
            return true;
        } else {
            return super.action (evt, arg);
        }
    }

	/**
     * Redefine in derived clases to add points to the plot.
     * Adding many points at once will make the plot somewhat faster because the thread
     * yields between calls to this method.  However, the plot will also be somewhat
     * less responsive to user inputs such as zooming, filling, or stopping.
     */
    public abstract void addPoints();

    /**
     * Stop the thread when the applet goes away.
     */
    public synchronized void destroy() {
        if (_plotThread != null && _plotThread.isAlive()) {
            // destroying the thread while it is in wait leaves a waiting thread on
            // the queue, apparently.  Notify the thread to die.
            _die = true;
            notifyAll();
            // wait for a response.
            try {
                wait();
            } catch (InterruptedException e) {}
            _plotThread.stop();
            _plotThread = null;
        }
    }

    /**
     * Disable plotting.  While plotting is disabled, no calls are made to
     * <code>addPoints()</code>.  The applet just sits idly waiting for plotting
     * to be enabled.
     */
    public void disable() {
        _running = false;
    }

    /**
     * Enable plotting. While plotting is enabled, calls are made to
     * <code>addPoints()</code>.
     */
    public synchronized void enable() {
        _running = true;
        notifyAll();
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotLive 1.0: A live data plotter. By: Edward A. Lee, eal@eecs.berkeley.edu";
    }

    /**
     * Create a start and stop buttons, by which the user can invoke
     * <code>enable()</code> and <code>disable</code>.  Alternatively, a derived class
     * might invoke these directly and dispense with the buttons.  This should be
     * called within the <code>init()</code> method in derived classes.
     */
    public void makeButtons () {
        // So that the buttons appear at the upper right...
        // Note that this infringes on the title space... maybe not good.
        _startButton = new Button("start");
        add(_startButton);
        _stopButton = new Button("stop");
        add(_stopButton);
    }
    
	/**
     * This is the body of a thread that monitors which of the start or stop buttons
     * have been pushed most recently, or which of the <code>enable()</code> or
     * <code>disable()</code> methods has been called most recently, to determine
     * whether to patiently wait or to call the <code>addPoints()</code> method.
     * Between calls to <code>addPoints()</code>, it calls <code>Thread.yield()</code>
     * so that the thread does not hog all the resources.  This somewhat slows
     * down execution, so derived classes may wish to plot quite a few points in their
     * <code>addPoints()</code> method, if possible.  However, plotting more points
     * at once may also decrease the responsiveness of the user interface.
     */
    public synchronized void run() {
        while (true) {
            if (_die) {
                notifyAll();
                return;
            }
            if (_running) {
                addPoints();
                Thread.yield();
            } else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Start the applet.  This is called by the applet viewer.
     * It creates a thread to plot live data, if this has not been already done.
     */
    public void start() {
        if (_plotThread == null) {
            _plotThread = new Thread(this, "Plot Thread");
            _plotThread.start();
        }
    }

    /**
     * Stop the applet.  This destroys the thread created by <code>start()</code>.
     */
    public void stop() {
        destroy();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                       private variables                          ////
    
    private Thread _plotThread;
   
    private Button _startButton, _stopButton;
    
    private boolean _running = false;
    private boolean _die = false;
}
