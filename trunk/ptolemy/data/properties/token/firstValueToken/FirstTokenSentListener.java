/*  When a token is sent on a port, add a PropertyToken to the adapter
   associated with the solver.

Copyright (c) 2007-2010 The Regents of the University of California.
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
*/
package ptolemy.data.properties.token.firstValueToken;

import ptolemy.actor.IOPort;
import ptolemy.actor.IOPortEvent;
import ptolemy.actor.IOPortEventListener;
import ptolemy.data.Token;
import ptolemy.data.properties.token.PropertyToken;
import ptolemy.data.properties.token.PropertyTokenHelper;
import ptolemy.data.properties.token.PropertyTokenSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// FirstTokenSentListener

/**
   When a token is sent on a port, add a PropertyToken to the adapter
   associated with the solver.
   @author Man-Kit Leung
   @version $Id$
   @since Ptolemy II 8.0
   @Pt.ProposedRating Red (mankit)
   @Pt.AcceptedRating Red (mankit)
*/
public class FirstTokenSentListener implements IOPortEventListener {

    /** Construct a FirstTokenGotListener.
     *  @param solver The solver.
     */
    public FirstTokenSentListener(PropertyTokenSolver solver) {
        _solver = solver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When a token is sent on a port, add a PropertyToken 
     *  to the adapter associated with the solver.
     *  @param event The event to report.
     */
    public void portEvent(IOPortEvent event) {
        if (event.getEventType() != IOPortEvent.SEND_BEGIN) {
            return;
        }

        IOPort port = event.getPort();
        Token token = event.getToken();
        if (token == null) {
            Token [] tokens = event.getTokenArray();
            if (tokens == null) {
                 throw new InternalErrorException(event.getSource(), null,
                                                  "event " + event +
                                                  " returned a null token array.");
            }
            token = event.getTokenArray()[0];
        }

        try {
            // prevent of logging an event multiple times (necessary for SampleDelay in combination
            // with value inference for extendedFirstValueToken solver)
            if (_solver.getToken(port) == null) {
                ((PropertyTokenHelper) _solver.getHelper(port.getContainer()))
                        .setEquals(port, new PropertyToken(token));
            }
        } catch (IllegalActionException e) {
            // FIXME: instead of asserting false, this method should
            // not catch the exception and just throw IllegalActionException.
            // However, this method in IOPortEventListener does not throw
            // an IllegalActionException.
            // TODO Auto-generated catch block
            e.printStackTrace();
            assert false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private PropertyTokenSolver _solver;
}
