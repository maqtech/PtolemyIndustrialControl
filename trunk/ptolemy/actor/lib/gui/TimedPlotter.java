/* Plot functions of time.

 @Copyright (c) 1998-2005 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.gui;

import java.util.ArrayList;

import ptolemy.actor.TimedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// TimedPlotter

/**
 A signal plotter.  This plotter contains an instance of the Plot class
 from the Ptolemy plot package as a public member.  Data at the input, which
 can consist of any number of channels, are plotted on this instance.
 Each channel is plotted as a separate data set.
 The horizontal axis represents time.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class TimedPlotter extends Plotter implements TimedActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedPlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        disconnectGraphOnAbscentValue = new Parameter(this, "disconnectGraphOnAbscentValue",
                new BooleanToken(false));
        disconnectGraphOnAbscentValue.setTypeEquals(BaseType.BOOLEAN);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** When disconnectGraphOnAbscentValue is True there will be a gap
     *  in the graph each time a the actor is fired, but the value
     *  is absent for a certain channel. Especially in the continuous
     *  domain this options is useful. By default this parameter is
     *  False.
     */
    public Parameter disconnectGraphOnAbscentValue;

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        int width = input.getWidth();
        _connected.clear();
        for (int i = 0; i < width; i++) {
            _connected.add(true);
        }
        if (((BooleanToken) disconnectGraphOnAbscentValue.getToken()).booleanValue()) {
         // NOTE: We assume the superclass ensures this cast is safe.
            if (((Plot) plot).getMarksStyle().equals("none")) {
                // If we wouldn't do this you wouldn't see anything for discrete signals.
                ((Plot) plot).setMarksStyle("dots");
            }
        }
    }
    
    /** Read at most one input from each channel and plot it as a
     *  function of time.
     *  This is done in postfire to ensure that data has settled.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        double currentTimeValue;
        int width = input.getWidth();
        
        boolean disconnectOnAbscent = ((BooleanToken) disconnectGraphOnAbscentValue.getToken()).booleanValue();
        int offset = ((IntToken) startingDataset.getToken()).intValue();

        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                currentTimeValue = input.getModelTime(i).getDoubleValue();

                DoubleToken currentToken = (DoubleToken) input.get(i);
                double currentValue = currentToken.doubleValue();

                // NOTE: We assume the superclass ensures this cast is safe.
                ((Plot) plot).addPoint(i + offset, currentTimeValue,
                        currentValue, _connected.get(i));
                if (disconnectOnAbscent) {
                    _connected.set(i, true);
                }
            } else if (disconnectOnAbscent && _connected.get(i)) {
                _connected.set(i, false);                                       
            }
        }

        return super.postfire();
    }
    
    private ArrayList<Boolean> _connected = new ArrayList<Boolean>();
}
