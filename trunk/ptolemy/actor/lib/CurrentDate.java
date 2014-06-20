/* A timed actor that outputs the current time .

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CurrentTime

/**
 Produce an output token on each firing with a value that is
 the current date.

 @author Patricia Derler
 @version $Id: CurrentTime.java 65763 2013-03-07 01:54:37Z cxh $
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class CurrentDate extends TimedSource {
    
    /** Construct an actor with the given container and name.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CurrentDate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set the type constraints.
        output.setTypeEquals(BaseType.DATE);

        // Override the clock to make it look a bit
        // different from the DiscreteClock and PoissonClock.
        _attachText(
                "_iconDescription",
                "<svg>\n"
                        + "<rect x=\"-20\" y=\"-20\" "
                        + "width=\"40\" height=\"40\" "
                        + "style=\"fill:lightGrey\"/>\n"
                        + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                        + "style=\"fill:black\"/>\n"
                        + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\" style=\"stroke:white\"/>\n"
                        + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the current time to the output.  If there are trigger inputs,
     *  then the current time is the minimum of the times of each of the
     *  input tokens (currently, these can be different only in the DT
     *  domain).  Otherwise, current time is that reported by the director.
     *  @exception IllegalActionException If send() throws it.
     */
    public void fire() throws IllegalActionException {

        if (trigger.isOutsideConnected()) {
            // Trigger port is connected.
            // If there is a token in a channel of the trigger port,
            // output the current time (that is associated with the token).
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.hasToken(i)) {
                    // Do not consume the token... It will be consumed
                    // in the superclass fire().
                    // trigger.get(i);
                    output.send(0, new DateToken());
                }
            }
        } else {
            output.send(0, new DateToken());
        }

        super.fire();
    }
}
