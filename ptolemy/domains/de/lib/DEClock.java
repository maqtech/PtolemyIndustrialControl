/* A DE actor that generate events at regular intervals, starting at time zero.

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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEClock
/**
Generate events at regular intervals, starting at time zero.

@author Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class DEClock extends DEActor {

    /** Construct a clock that generates events at default interval 1.0.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEClock(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException  {
        // this(container, name, new Token(), 1.0);
        this(container, name, 1.0, 1.0);
    }

    /** Construct a clock that generates events with the specified values
     *  at the specified interval.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     *  @deprecated This constructor is going away... 
     */
    public DEClock(TypedCompositeActor container, String name,
            double value, double interval)
            throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(Token.class);
        _interval = new Parameter(this, "interval", new DoubleToken(interval));
        _value = new Parameter(this, "value", new DoubleToken(value));
    }


    /** Construct a clock that generates events with the specified values
     *  at the specified interval.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @param value The value of the output.
     *  @param interval The interval between clock ticks.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    // ambiguous signature in TclBlend with the deprecated constructor.
    // wait till deprecated -> gone, then uncomment this constructor
    /*
      public DEClock(TypedCompositeActor container, String name,
      Token value, double interval)
      throws IllegalActionException, NameDuplicationException  {
      super(container, name);
      output = new TypedIOPort(this, "output", false, true);
      output.setTypeEquals(Token.class);
      _interval = new Parameter(this, "interval", new DoubleToken(interval));
      _value = new Parameter(this, "value", value);
      }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce the initializer event that will cause the generation of
     *  the first output at time zero.
     *
     *  FIXME: What to do if the initial current event is less than zero ?
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        // FIXME: This should be just DEDirector
        // FIXME: This class should be derived from DEActor, which should
        // ensure that this cast is valid.
        super.initialize();
        double curTime = getCurrentTime();
        // The delay parameter maybe negative, but it's permissible in the
        // director because the start time is not initialized yet.
	fireAfterDelay(0.0-curTime);
    }

    /** Produce an output event at the current time, and then schedule
     *  a firing in the future.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire()
            throws IllegalActionException {
        output.broadcast(_value.getToken());
	fireAfterDelay(((DoubleToken)_interval.getToken()).doubleValue());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    // The output port.
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // the interval between events
    // private double _interval;
    private Parameter _interval;
    // the output value.
    // private double _value;
    private Parameter _value;
}






