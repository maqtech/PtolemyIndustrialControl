/* An actor that implements a resettable timer.

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

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DETimer
/**
The input sets the time interval before the next expire.

@author Xiaojun Liu
@version $Id$
*/
public class DETimer extends DEActor {

    /** Constructor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DETimer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        expired = new DEIOPort(this, "expired", false, true);
        expired.setTypeEquals(Token.class);
        set = new DEIOPort(this, "set", true, false);
        set.setTypeEquals(DoubleToken.class);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the timer if there is a token in port set. Otherwise send
     *  a token to port expire if the current time agrees with the time
     *  the timer is set to expire.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        if (set.hasToken(0)) {
            // reset timer
            double delay = ((DoubleToken)set.get(0)).doubleValue();
            if (delay > 0.0) {
                _expireTime = getCurrentTime() + delay;
                fireAt(getCurrentTime() + delay);
            } else {
                // disable timer
                _expireTime = -1.0;
            }

            //System.out.println("Reset DETimer " + this.getFullName() +
            //        " to expire at " + _expireTime);

        } else if (Math.abs(getCurrentTime() - _expireTime) < 1e-14) {
            // timer expires
            expired.broadcast(_outToken);

            //System.out.println("DETimer " + this.getFullName() + " expires at "
            //        + getCurrentTime());

        }

    }

    /** Initialize the timer.
     *  @exception IllegalActionException If the initialize() of the parent
     *   class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _expireTime = -1.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial Set port. */
    public DEIOPort set;
    /** @serial Expired port. */
    public DEIOPort expired;



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial So we don't need to create the token every time the
     *  timer expires.
     */
    private static final Token _outToken = new Token();

    /** @serial The time to expire.*/
    private double _expireTime = -1.0;

}
