/* An attribute that creates a task plot.

@Copyright (c) 2013 The Regents of the University of California.
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

package ptolemy.apps.hardrealtime;

import java.awt.Frame;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DBFPlotEditorFactory

/**
 An attribute that creates a task plot {@link TaskPlot} that visualizes
 the execution of a hard real time system.

 @author Patricia Derler
 @version $Id: TaskPlotEditorFactory.java 69607 2014-07-30 17:07:26Z cxh $
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class DBFPlotEditorFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @throws IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @throws NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DBFPlotEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     * @param object The object to configure.
     * @param parent The parent window.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        try {
            // If there is no plot tableau or if it has been closed
            if (_plotTableau == null || _plotTableau.getContainer() == null) {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();
                if (_plot == null) {
                    _plot = new DBFPlot();
                }
                PlotEffigy schedulePlotEffigy = new PlotEffigy(configuration,
                        "dbfPlotterEffigy");
                schedulePlotEffigy.setPlot(_plot);
                schedulePlotEffigy.setModel(this.getContainer());
                schedulePlotEffigy.identifier.setExpression("Demand Bound Function");
                _plotTableau = configuration
                        .createPrimaryTableau(schedulePlotEffigy);
                _plot.setVisible(true);
                _plot.addLegend(0, "demand bound function for EDF");
                _plot.addLegend(1, "maximum");
                _plot.doLayout();
            }
        } catch (Throwable throwable) {
            throw new InternalErrorException(object, throwable,
                    "Cannot create Schedule Plotter");
        }
    }

    /** Return the task plot last generated by the factory.
     *  @return The last task plot.
     */
    public DBFPlot getTaskPlot() {
        return _plot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private DBFPlot _plot;
    private Tableau _plotTableau;

}
