/* A base class for threaded DE domain actors.

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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// DEThreadActor
/**
A base class for threaded DE domain actor.
<P>
FIXME: EXPERIMENTAL.
<P>
This actor, upon its initialization, will start another thread.
The thread communicate with the DEDirector thread by placing
events into the DEEventQueue ashynchronously.
<P>
Subclass of this class should implement the run() method.
The subclass is executed in an event driven way. More precisely,
the implementation of the run() method should call
waitForNewInputs() after processing all current events. The
calls are blocked until the next time fire() is called.
Recall that the Director (after puting events into the
receiver of the input ports) will call fire() on the actor.
NOTE: The synchronization mechanism is implemented in DECQEventQueue
to ensure the correct multi-threading behaviour.
<P>
This implementation does not change the semantics of DEReceiver,
but still supports an asynchronous message passing type of
concurrency.

@author Lukito Muliadi
@version $Id$
@see DEActor
*/
public abstract class DEThreadActor extends DEActor implements Runnable {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEThreadActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Create a thread for the actor and start the thread.
     */
    public void initialize() {
        // start a thread.
        _thread = new PtolemyThread(this);
        _isWaiting = true;
        _thread.start();
    }

    /** Awake the thread running this actor.
     */
    public void fire() {
        // Set the flag to false, to make sure only this actor wakes up.
        _isWaiting = false;
        synchronized(_monitor) {
            _monitor.notifyAll();
        }
        // then wait until this actor go to wait.
        while (!_isWaiting) {
            synchronized(_monitor) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Implement this method to define the job of the threaded actor.
     */
    public abstract void run();

    /** Clear input ports then wait until
     *  input events arrive.
     */
    public void waitForNewInputs() {

        _emptyPorts();

        // Set the flag to true, so the director can wake up.
        _isWaiting = true;
        synchronized(_monitor) {
            _monitor.notifyAll();
        }

        while (_isWaiting) {
            synchronized(_monitor) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Wait for new inputs on the specified array of ports.
     *  @param ports The array of ports whose inputs we're interested in.
     *  @exception IllegalActionException The specified array of ports is not
     *  all input ports.
     */
    public void waitForNewInputs(IOPort[] ports)
            throws IllegalActionException {

        _emptyPorts();

        while (true) {

            waitForNewInputs();
            // check for availability of tokens in the list of ports.
            // If any of the listed ports has at least a token, then return
            // Otherwise, wait for more new inputs.

            for (int i = 0; i < ports.length; i++) {
                IOPort port = ports[i];
                for (int j = 0; j < port.getWidth(); j++) {
                    if ( port.hasToken(j) ) {
                        return;
                    }
                }
            }
        } // while (true)

    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    private PtolemyThread _thread;
    private boolean _isWaiting = true;

    protected static Object _monitor = new Object();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Empty all receivers of all input ports.
    // FIXME: Shouldn't this be guaranteed by the run() of the actor?
    private void _emptyPorts() {
        Iterator ports = inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();
            for (int ch = 0; ch < port.getWidth(); ch++) {
                try {
                    while (port.hasToken(ch)) {
                        port.get(ch);
                    }
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                    throw new InternalErrorException(e.getMessage());
                }
            }
        }
    }
}
