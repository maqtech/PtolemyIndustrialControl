/* An aggregation of listeners.

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

@ProposedRating Red (johnr@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.pn.kernel.event;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PNProcessMulticaster
/**
A PNProcessMulticaster forwards process state change events it
receives to a list of other listeners. This is typically used
by classes that provide addProcessListener() and removeProcessListener()
methods.

@author John Reekie, Mudit Goel
@version $Id$
@see ptolemy.kernel.event.TopologyChangeRequest
*/
public class PNProcessMulticaster implements PNProcessListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new process listener. Any time any of the
     * event notification methods is called, the call will be forwarded
     * to the added listener.
     *
     * @param listener An object that listens to process state events
     */
    public void addProcessListener(PNProcessListener listener) {
        _listeners.insertLast(listener);
    }

    /** Return true if there are any listeners registered with the multicaster.
     *  @return true if there are any registered listeners.
     */
    public boolean anyListeners() {
        return  !(_listeners.isEmpty());
    }

    /** Notify that a process has finished execution. 
     *
     * @param event The PNProcessEvent
     */
    public void processFinished(PNProcessEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((PNProcessListener) e.nextElement()).processFinished(event);
        }
    }

    /** Notify that a process has changed state.
     *
     * @param event. The PNProcessEvent
     */
    public void processStateChanged(PNProcessEvent event) {
        for (Enumeration e = _listeners.elements(); e.hasMoreElements(); ) {
            ((PNProcessListener) e.nextElement()).processStateChanged(event);
        }
    }

    /** Remove a process listener. The listener will
     * no longer be notified of process events. If the
     * given listener has not been previously registered
     * with addProcessListener() (or is null), then do
     * nothing.
     *
     * @param listener An object that listens to topology events
     */
    public void removeProcessListener(PNProcessListener listener) {
        _listeners.removeOneOf(listener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // FIXME: use ArrayList when we port to JDK1.2
    private LinkedList _listeners = new LinkedList();
}
