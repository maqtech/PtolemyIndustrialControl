/* Helper thread for notifing each object in a LinkedList.


 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.actor;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// NotifyThread
/** 
Helper thread for notifing each object in a LinkedList.
It is used to create a new thread without any locks. It is mainly
used to notify a set of objects to wake up objects waiting on a lock.

<p>
To use this to wake up any threads waiting on a lock, create a new instance 
of this class with a LinkedList of lock objects to call notifyAll on, then 
wait for this object.


@author Neil Smyth
@version $Id$
*/

public class NotifyThread implements Runnable {

    /** Construct a thread to be used call notifyAll on a set of locks.
     *  @param locks The set of locks to call notifyAll on.
     */
    public NotifyThread(LinkedList locks) {
	_locks = locks;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The run returns the actor being executed by this thread
     *  @return The actor being executed by this thread.
     */
    public void run() {
        synchronized(this) {
            Enumeration objs = _locks.elements();
            while (objs.hasMoreElements()) {
                Object nextObj = objs.nextElement();
                if (nextObj instanceof ProcessReceiver) {
                    ProcessReceiver rec = (ProcessReceiver)nextObj;
                    System.out.println("Notifying all on: " + rec.getContainer().getName());
                }
                synchronized(nextObj) {
                    nextObj.notifyAll();
                }
            }
            this.notifyAll();
        }
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The locks to call notifyAll on.
    private LinkedList _locks;
}


















































































