/* An actor that plots the input data.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.ct.lib;

import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.plot.*;
import java.awt.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTPlot
/**
A plotter for discrete-event signals.

@author Jie Liu, Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class  CTPlot extends CTActor {

    private static final boolean DEBUG = false;

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public CTPlot  (TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {

        this(container, name, null);

    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     *
     */
    public CTPlot (TypedCompositeActor container, String name, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        //input.setDeclaredType(DoubleToken.class);
        input.setMultiport(true);
        input.setDeclaredType(DoubleToken.class);

        _plot = plot;

        // FIXME: This is not the right way to handle this...
        String legends = new String("");
        _yMin = (double)-1.0;
        _yMax = (double)1.0;
        _xMin = (double)-1.0;
        _xMax = (double)1.0;
        _paramLegends = new Parameter(this, "Legends", 
                new StringToken(legends));
        _paramYMin = new Parameter(this, "Y_Min", new DoubleToken(_yMin));
        _paramYMax = new Parameter(this, "Y_Max", new DoubleToken(_yMax));
        _paramXMin = new Parameter(this, "X_Min", new DoubleToken(_xMin));
        _paramXMax = new Parameter(this, "X_Max", new DoubleToken(_xMax));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {

        if (_plot == null) {
            _plot = new Plot();
            new PlotFrame(getName(), _plot);
        }
        _plot.clear(true);
        _plot.setButtons(true);
	_plot.setPointsPersistence(0);
        //_plot.setMarksStyle("points");
        _plot.setImpulses(false);
        _plot.setConnected(true);
        _plot.setTitle(getName());

        // parameters
        _yMin = ((DoubleToken)_paramYMin.getToken()).doubleValue();
        _yMax = ((DoubleToken)_paramYMax.getToken()).doubleValue();
        _xMin = ((DoubleToken)_paramXMin.getToken()).doubleValue();
        _xMax = ((DoubleToken)_paramXMax.getToken()).doubleValue();
        
        String legs = ((StringToken)_paramLegends.getToken()).stringValue();
        //System.out.println(legs);
        if(!legs.equals("")) {
            StringTokenizer stokens = new StringTokenizer(legs);
            int index = 0;
            _legends = new String[stokens.countTokens()];
            while(stokens.hasMoreTokens()) {
                 _legends[index++]= stokens.nextToken();
                 //System.out.println(_legends[index-1]);
            }
        }
        int width = input.getWidth();
        _firstPoint = new boolean[width];
        for (int i = 0; i < width; i++) {
             if (_legends != null && i < _legends.length &&
                    _legends[i].length() != 0) {
                _plot.addLegend(i, _legends[i]);
            } else {
                _plot.addLegend(i, "Data " + i);

            }
            _firstPoint[i] = true;
	}

        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.
        _rangeInitialized = false;
        
    }

    /** Add new input data to the plot.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{

        if (!_rangeInitialized) {
            //_plot.setXRange(getStartTime(), getStopTime());
            _plot.setYRange(getYMin(), getYMax());
            _plot.setXRange(_xMin, _xMax);
            _plot.init();
            _plot.repaint();
            _rangeInitialized = true;
        }

        int numEmptyChannel = 0;

        int width = input.getWidth();
        for (int i = 0; i<width; i++) {
            // check channel i.
            if (input.hasToken(i)) {
                double curTime =((Director)getDirector()).getCurrentTime();
                // channel i is not empty, get all the tokens in it.
                while (input.hasToken(i)) {
                    DoubleToken curToken = null;
                    curToken = (DoubleToken)input.get(i);
                    double curValue = curToken.doubleValue();

                    // update the y range
                    boolean yRangeChanged = false;
                    if (curValue < _yMin) {
                        yRangeChanged = true;
                        _yMin = curValue;
                    }
                    if (curValue > _yMax) {
                        yRangeChanged = true;
                        _yMax = curValue;
                    }
                    if (yRangeChanged) {
                        _plot.setYRange(_yMin, _yMax);
                        _plot.repaint();
                    }

                    // add the point
                    if (DEBUG) {
                        System.out.print(this.getFullName() + ":");
                        System.out.println("Dataset = " + i +
                                ", CurrentTime = " + curTime +
                                ", CurrentValue = " + curValue + ".");
                    }
                    if(_firstPoint[i]) {
                        _plot.addPoint(i, curTime, curValue, false);
                        _firstPoint[i] = false;
                    } else {
                        _plot.addPoint(i, curTime, curValue, true);
                    }
                }
            } else {
                // Empty channel. Ignore
                // But keep track of the number of empty channel,
                // because this actor shouldn't be fired if all channels
                // are empty..
                numEmptyChannel++;
            }
        }
        // If all channels are empty, then the scheduler is wrong.
        if (numEmptyChannel == width) {
            throw new InternalErrorException(
                "Discrete event scheduling error. DEPlot fired, but there "
                + "is no input data.");
        }
    }

    /** Set the legends.
     */
    public void setLegend(String[] legends) {
        _legends = legends;
    }

    /** Rescale the plot so that all the data plotted is visible.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
	_plot.fillPlot();
        super.wrapup();
    }

    // FIXME: This is not the right way to handle this.
    public void setYRange(double ymin, double ymax) {
        _yMin = ymin;
        _yMax = ymax;
    }

    public double getYMin() {
        return _yMin;
    }

    public double getYMax() {
        return _yMax;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Plot _plot;

    private String[] _legends;
    private Parameter _paramLegends;

    private double _yMin;
    private Parameter _paramYMin;
    private double _yMax;
    private Parameter _paramYMax;

    private double _xMin;
    private Parameter _paramXMin;
    private double _xMax;
    private Parameter _paramXMax;

    private boolean _rangeInitialized = false;

    private boolean[] _firstPoint;
}
