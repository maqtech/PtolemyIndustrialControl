/* Interface for receivers in process domains.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.util.*;
import ptolemy.actor.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ProcessReceiver
/**
Interface for receivers in the process oriented domains.
It adds methods to the Receiver interface for setting flags that
indicate whether a pause, resume or termination of the simulation has
been requested.

In process oriented domains, simulations are normally ended on the
detection of a deadlock. During a deadlock, processes or the
corresponding threads are normally waiting on a call to some
methods (for reading or writing) on a receiver.
To terminate or end the simulation, these methods should
either return or throw an exception to inform the processes that they
should terminate themselves. For this a method setFinish() is defined.
This method would set a local flag in the receivers and wake up all the
processes waiting on some call to the receiver. On waking up these
processes would see that the termination flag set and behave accordingly.
A sample implementation is <BR>
<Code>
public synchronized void setFinish() {
    _terminate = true;
    notifyAll();
}
</code>
<p>

Similarly, in process oriented domains, a simulation can be paused,
safely, only when the processes try to communicate with some other
process by calling methods on the receiver. For this, a setPause()
method is defined. This method will set a local flag in the receiver
which indicates that a pause has been requested. When a process next
calls any of the methods in the receiver to read or write a token,
it will be paused. To resume the simulation, the method will be called
with false as an argument. This method will then reset the local flag
and resume the paused processes.
A sample implementation is: <BR>
<code>
public synchronized void setPause(boolean pause) {
    if (pause) {
        _pause = true;
    } else {
        _pause = false;
	notifyAll();
    }
}
</code>

@author Neil Smyth, Mudit Goel
@version $Id$

*/
public interface ProcessReceiver extends Receiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Initialize the receiver by resetting local flags. This method
     *  is useful in clone() methods as well as when restarting
     *  execution.
     */
    public void initialize();

    /** Set a local flag that requests that the simulation be paused
     *  or resumed.
     *  @param value The flag indicating a requested pause or resume.
     */
    public void setPause(boolean value);

    /** Set a local flag requesting that the simulation be finished.
      */
    public void setFinish();
}



