/* Plot functions of time in oscilloscope style.

@Copyright (c) 1998-2000 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.plot.*;
import java.awt.Panel;

import javax.swing.SwingUtilities;

/**
A signal plotter that plots in an oscilloscope style, meaning that the
horizontal axis is wrapped and that there is finite persistence.
This plotter contains an instance of the Plot class
from the Ptolemy plot package as a public member.  Data at the input, which
can consist of any number of channels, is plotted on this instance.
Each channel is plotted as a separate data set.
The horizontal axis represents time.
The <i>width</i> parameter is a double that gives the width
of the plot. The horizontal axis will be labeled from 0.0 to
<i>width</i>.  It defaults to 100.
If the <i>persistence</i> parameter is positive, then it specifies
the amount of time into the past that points are shown.
It also defaults to 100, so any point older than 100 time units is
erased and forgotten. The input is of type DoubleToken.

@author  Edward A. Lee
@version $Id$
 */
public class TimedScope extends TimedPlotter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedScope(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        width = new Parameter(this, "width", new DoubleToken(100.0));
        persistence = new Parameter(this, "persistence",
                new DoubleToken(100.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the X axis (a double). */
    public Parameter width;

    /** The amount of data displayed at any one time. */
    public Parameter persistence;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
        @exception IllegalActionException If the expression of the
        attribute cannot be parsed or cannot be evaluated.
    */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == width && plot != null) {
            double widthValue = ((DoubleToken)width.getToken()).doubleValue();
            plot.setXRange(0.0, widthValue);
        } else if (attribute == persistence && plot != null) {
            double persValue =
                    ((DoubleToken)persistence.getToken()).doubleValue();
            plot.setXPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TimedScope newobj = (TimedScope)super.clone(ws);
        newobj.width = (Parameter)newobj.getAttribute("width");
        newobj.persistence = (Parameter)newobj.getAttribute("persistence");
        return newobj;
    }

    /** Configure the plotter using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double widthValue = ((DoubleToken)width.getToken()).doubleValue();
        plot.setXRange(0.0, widthValue);
        plot.setWrap(true);
        double persValue = ((DoubleToken)persistence.getToken()).doubleValue();
        plot.setXPersistence(persValue);
        plot.repaint();
    }

    /** Read at most one input from each channel and plot it as a
     *  function of time.
     *  This is done in postfire to ensure that data has settled.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        Runnable doPostfire = new Runnable() {
            public void run() {
                try {
                    double currentTime =
                            ((Director)getDirector()).getCurrentTime();
                    int width = input.getWidth();
                    int offset =
                            ((IntToken)startingDataset.getToken()).intValue();
                    for (int i = width - 1; i >= 0; i--) {
                        if (input.hasToken(i)) {
                            DoubleToken currentToken =
                                    (DoubleToken)input.get(i);
                            double currentValue = currentToken.doubleValue();
                            plot.addPoint(i + offset, currentTime,
                                    currentValue, true);
                        }
                    }
                } catch (IllegalActionException ex) {
                    getManager().notifyListenersOfException(ex);
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(doPostfire);
        } catch (Exception ex) {
            // Ignore InterruptedException.
            // Other exceptions should not occur.
        }
        return true;
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.  This overrides the base
     *  class to do the fill in the event thread.
     */
    public void wrapup() {
        try {
            if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
                Runnable doFill = new Runnable() {
                    public void run() {
                        plot.fillPlot();
                    }
                };
                try {
                    SwingUtilities.invokeAndWait(doFill);
                } catch (Exception ex) {
                    // Ignore InterruptedException.
                    // Other exceptions should not occur.
                }
            }
        } catch (IllegalActionException ex) {
            // fillOnWrapup does not evaluate to a valid token,
            // skip fillPlot()
        }
    }
}
