/* Plot sequences that are potentially infinitely long.

@Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.plot.*;

/**
A sequence plotter for sequences that are potentially infinitely long.
This plotter contains an instance of the Plot
class from the Ptolemy plot package as a public member. Data at
the input, which can consist of any number of channels, are plotted
on this instance.  Each channel is plotted as a separate data set.
The horizontal axis represents the count of the iterations, modulo
the <i>width</i> parameter, scaled by the <i>xUnit</i> parameter.
The <i>width</i> parameter must be an integer that gives the width
of the plot in number of samples. It defaults to 10.
If the <i>persistence</i> parameter is positive, then it specifies
the number of points that are remembered. It also defaults to 10.
Any points older than these are erased and forgotten.
The horizontal increment between samples is given by the
<i>xUnit</i> parameter. Its default value is 1.0. The horizontal value
of the first sample is given by the <i>xInit</i> parameter.
Its default value is 0.0. The input is of type DoubleToken.

@author  Edward A. Lee
@version $Id$
 */
public class SequenceScope extends SequencePlotter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceScope(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        width = new Parameter(this, "width", new IntToken(10));
        width.setTypeEquals(BaseType.INT);
        persistence = new Parameter(this, "persistence", new IntToken(10));
        persistence.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the X axis, in number of samples (an integer). */
    public Parameter width;

    /** The number of samples to be displayed at any one time (an integer). */
    public Parameter persistence;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify this that an attribute has changed.  If either parameter
     *  is changed, then this actor updates the configuration of the 
     *  visible plot.
     *  @exception IllegalActionException If the expression of the
     *  attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == width && plot != null) {
            double xunit = ((DoubleToken)xInit.getToken()).doubleValue();
            int widthValue = ((IntToken)width.getToken()).intValue();
            plot.setXRange(0.0, xunit*widthValue);
        } else if (attribute == persistence && plot != null) {
            int persValue = ((IntToken)persistence.getToken()).intValue();
            plot.setPointsPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Initialize this actor.  This method configures the visible plot
     *  using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double xunit = ((DoubleToken)xUnit.getToken()).doubleValue();
        int widthValue = ((IntToken)width.getToken()).intValue();
        plot.setXRange(0.0, xunit*widthValue);
        plot.setWrap(true);
        int persValue = ((IntToken)persistence.getToken()).intValue();
        plot.setPointsPersistence(persValue);
        // Override the default so that there are not gaps in the lines.
        if (plot.getMarksStyle().equals("none")) {
            plot.setMarksStyle("pixels");
        }
        plot.repaint();
    }

    /** Call the base class postfire() method, then yield this
     *  thread so that the event thread gets a chance.  This is necessary,
     *  because otherwise the swing thread may be starved and accumulate a 
     *  large number of points waiting to be plotted.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        Thread.yield();
        return result;
    }
}
