/* Base class for time-based sources.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;

//////////////////////////////////////////////////////////////////////////
//// TimedSource
/**
Base class for time-based sources.  A time-based source is
a source where the output value is a function of current time.
For some sequence-based domains, such as SDF, actors of this type
probably do not make sense because current time is not incremented.
This actor has a parameter, <i>stopTime</i>, that optionally controls
the duration for which the actor is fired.  If this number is
<i>t</i> > 0.0, then when current time reaches <i>t</i>,
postfire() returns false. This indicates
to the director that this actor should not be invoked again.
The default value of <i>stopTime</i> is 0.0, which results in postfire
always returning true.  In other words, this makes the lifetime
infinite. Derived classes must call super.postfire() for this mechanism to
work.

@author Edward A. Lee
@version $Id$
*/

public class TimedSource extends Source implements TimedActor {

    /** Construct an actor with the given container and name.
     *  The <i>stopTime</i> parameter is also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        stopTime = new Parameter(this, "stopTime", new DoubleToken(0.0));
	stopTime.setTypeEquals(BaseType.DOUBLE);

	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" "
                + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<circle cx=\"20\" cy=\"20\" r=\"17\""
                + "style=\"fill:white\"/>\n"
                + "<line x1=\"20\" y1=\"5\" x2=\"20\" y2=\"7\"/>\n"
                + "<line x1=\"20\" y1=\"34\" x2=\"20\" y2=\"36\"/>\n"
                + "<line x1=\"5\" y1=\"20\" x2=\"7\" y2=\"20\"/>\n"
                + "<line x1=\"34\" y1=\"20\" x2=\"36\" y2=\"20\"/>\n"
                + "<line x1=\"20\" y1=\"12\" x2=\"20\" y2=\"20\"/>\n"
                + "<line x1=\"20\" y1=\"20\" x2=\"31.26\" y2=\"13.5\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If greater than zero, then this parameter gives the time at which
     *  postfire() should return false.
     */
    public Parameter stopTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the <i>stopTime</i> parameter is changed, then
     *  if the new value is greater
     *  than zero and greater than the current time, then ask the director
     *  to fire this actor at that time.  If the new value is less than
     *  the current time, then request refiring at the current time.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == stopTime) {
            double time = ((DoubleToken)stopTime.getToken()).doubleValue();
            if (time > 0.0) {
                Director director = getDirector();
                if (director != null) {
                    double currentTime = director.getCurrentTime();
                    if (time > currentTime) {
                        director.fireAt(this, time);
                    } else {
                        director.fireAt(this, currentTime);
                    }
                } // else ignore.
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Initialize the actor. Schedule a refiring of this actor at the
     *  stop time given by the <i>stopTime</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double time = ((DoubleToken)stopTime.getToken()).doubleValue();
        if (time > 0.0) {
            Director director = getDirector();
            if (director == null) {
                throw new IllegalActionException(this, "No director!");
            }
            double currentTime = director.getCurrentTime();
            director.fireAt(this, time);
        }
    }

    /** Return false if the current time is greater than or equal to
     *  the <i>stopTime</i> parameter value.
     *  Otherwise, return true.  Derived classes should call this
     *  at the end of their postfire() method and return its returned
     *  value.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        double time = ((DoubleToken)stopTime.getToken()).doubleValue();
        if (time > 0.0 && getDirector().getCurrentTime() >= time) {
            return false;
        }
        return true;
    }
}
