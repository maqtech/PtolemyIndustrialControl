/* An applet demonstrating EditablePlot.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Sketch;

import java.awt.event.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.SequencePlotter;
import ptolemy.actor.lib.gui.SketchedSource;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// SketchApplet
/**
This applet demonstrates the use of the SketchSource actor,
and in particular, how to share the same plot display between
an instance of SketchedSource and an instance of SequencePlotter.

@see SketchedSource
@author Edward A. Lee
@version $Id$
*/
public class SketchApplet extends MoMLApplet implements EditListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the model.  This is called by the editable plot widget
     *  when the user edits the data in the plot.  The execution is
     *  carried out only if the model is idle (not currently executing).
     *  @param source The plot containing the modified data.
     *  @param dataset The data set that has been modified.
     */
    public void editDataModified(EditablePlot source, int dataset) {
        try {
            if (_manager.getState() == _manager.IDLE) {
                _go();
            }
        } catch (IllegalActionException ex) {
            report(ex);
        }
    }

    /** Create the shared plot and set it up based on the director parameters.
     */
    public void _createView() {
        super._createView();
        try {
            // Find out how many iterations the director expects to run for.
            SDFDirector director = (SDFDirector)_toplevel.getDirector();
            int iterations =
                ((IntToken)(director.iterations.getToken())).intValue();

            SketchedSource source = (SketchedSource)
                    _toplevel.getEntity("Sketched Source");
            SequencePlotter plotter = (SequencePlotter)
                    _toplevel.getEntity("Plotter");

            // Note: The order of the following is important.
            // First, specify how long the sketched plot should be.
            source.length.setToken(new IntToken(iterations));

            // Then, create the plot and place it in this applet,
            // and specify to both the source and destination actors
            // to use the same plot widget.
            EditablePlot plot = new EditablePlot();
            plot.setSize(700, 300);
            plot.setTitle("Editable envelope");
            plot.setXRange(0, iterations);
            plot.setButtons(true);
            getContentPane().add(plot);
            plotter.place(plot);
            source.place(plot);
            plot.setBackground(null);
            plot.addEditListener(this);
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }

    /** Do not execute the model on startup.
     */
    public void start() {
    }
}
