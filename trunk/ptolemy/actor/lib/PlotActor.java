/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.actor.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.plot.*;
import java.awt.Panel;

/** A signal plotter.  This plotter contains an instance of the Plot class
 *  from the Ptolemy plot package as a public member.  Data at the input, which
 *  can consist of any number of channels, is plotted on this instance.
 *  The horizontal axis can represent either time or a count of the
 *  firings.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class PlotActor extends TypedAtomicActor implements Placeable {

    public PlotActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(DoubleToken.class);

        // create parameters.
        timed = new Parameter(this, "timed", new BooleanToken(true));
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Input port. */
    public TypedIOPort input;

    /** The plot object. */
    public Plot plot;

    /** If true, fill the plot when wrapup is called. */
    public Parameter fillOnWrapup;

    /** Specify whether to use token count (vs. current time)
     *  for the X axis value.  Using current time is the default.
     *  This parameter contains a BooleanToken.
     */
    public Parameter timed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            PlotActor newobj = (PlotActor)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.input.setMultiport(true);
            newobj.input.setTypeEquals(DoubleToken.class);
            newobj.timed
                = (Parameter)newobj.getAttribute("timed");
            newobj.fillOnWrapup
                = (Parameter)newobj.getAttribute("fillOnWrapup");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read all available inputs and plot them as a function of time.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_useCurrentTime) {
            _xValue = ((Director)getDirector()).getCurrentTime();
        } else {
            _xValue += 1.0;
        }
        int width = input.getWidth();
        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                DoubleToken curToken = (DoubleToken)input.get(i);
                double curValue = curToken.doubleValue();
                plot.addPoint(i, _xValue, curValue, true);
            }
        }
    }

    /** If the plot has not already been created, create it.
     *  If a panel has been specified, and that panel is an instance of
     *  of Plot, then plot data to that instance.  If a panel has been
     *  specified but it is not an instance of Plot, then create a new
     *  instance of Plot and place the plot in that panel
     *  using its add() method.
     */
    public void initialize() {
        if (plot == null) {
            setPanel(_panel);
        } else {
            plot.clear(false);
        }
        // Process parameters.
        _useCurrentTime = ((BooleanToken)timed.getToken()).booleanValue();

        plot.repaint();
        _xValue = -1.0;
    }

    /** Specify the panel into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument.
     *
     *  @param panel The panel into which to place the plot.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            // place the plot in its own frame.
            plot = new Plot();
            PlotFrame frame = new PlotFrame(getFullName(), plot);
        } else {
            if (_panel instanceof Plot) {
                plot = (Plot)_panel;
            } else {
                plot = new Plot();
                _panel.add(plot);
                plot.setButtons(true);
            }
        }
    }

    /** Rescale the plot so that all the data is visible if the fillOnWrapup
     *  parameter is true.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
            plot.fillPlot();
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel;
    private boolean _useCurrentTime = true;
    private double _xValue;
}
