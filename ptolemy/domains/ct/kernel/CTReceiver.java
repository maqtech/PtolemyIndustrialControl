/* The receiver for the CT domain.

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
@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CTReceiver
/**
The receiver for the continuous time domain. This is a mailbox with
capacity one, and any token put in the receiver overwrites
any token previously present in the receiver. As a consequence,
hasRoom() method always returns true. The get() method will consume
the token if there exists one. After the consumption, the hasToken()
method will return false, until a token is put into this receiver.

@author  Jie Liu
@version $Id$
*/
public class CTReceiver extends Mailbox {

    /** Construct an empty CTReceiver with no container.
     */
    public CTReceiver() {
        super();
    }

    /** Construct an empty CTReceiver with the specified container.
     *  @param container The port that contains the receiver.
     *  @exception IllegalActionException If this receiver cannot be
     *   contained by the proposed container.
     */
    public CTReceiver(IOPort container) throws IllegalActionException {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true, since the new token will override the old one.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Put a token into this receiver. If the argument is null,
     *  then this receiver will not contain any token after this method
     *  returns. If the receiver already has a token, then the new token
     *  will override the old token, and the old
     *  token will be lost.
     *
     *  @param token The token to be put into this receiver.
     *  @exception NoRoomException Not thrown in this base class.
     */
    public void put(Token token) throws NoRoomException{
        if(hasToken()) {
            get();
        }
        super.put(token);
        // Uncomment the following lines when debugging the receiver.
        // System.out.println(getContainer().getFullName() +
        //        " received " + token);
    }
}
