/* A ExecEventListener is able to receive PNProcessEvents from the Processes.

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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.process;

//////////////////////////////////////////////////////////////////////////
//// ExecEventListener
/**
An ExecEventListener is able to receive PNProcessEvents that are issued
during the execution of a process by a ProcessThread or director in PN.   
In general, an object that implements this interface will probably be a front 
end such as a execution visualization tool for
the Ptolemy II system, or an object that is communicating with a front end.
The events are issued only when the event actually occurs, not when it is
requested.   For example: A process receives a call to pause(), but the 
process may pause some time after the pause was requested. The processPaused()
method will not be called until the process actually pauses and corresponding 
thread is suspended.   

@author Mudit Goel, John S. Davis II
@version $Id$
*/

public interface ExecEventListener {

    /** Called to report that a process has changed its state (i.e. started, 
     *  or blocked or unblocked, etc.). The PNProcessEvent
     *  will contain a reference to the actor corresponding to the process. 
     *  The event will also indicate the new state and blocking cause, etc.
     *
     *  @param event A PNProcessEvent that contains a reference to an actor.
     */
    public void stateChanged(ExecEvent event);

}




