/* Interface for MetroII Actor.
 
 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MetroII Actor

/**
 * A MetroIIActor is an executable entity. This interface defines the common
 * functionality in atomic actor and composite actor.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
*/
// FIXME: startOrResumable

public interface MetroIIActorInterface {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** <p> If it's called for the first time, the function initializes the state, 
     *  otherwise, it resumes the state it saves last time. 
     *  The state means local variables, stacks or anything it needs to 
     *  resume the execution of last time. </p> 
     * 
     *  <p> Continue iteratively executing the actor until any MetroII Event is 
     *  proposed. When any MetroII Event is proposed, the function returns with the 
     *  state of the function saved and the proposed events added into metroIIEventList. 
     *  </p>
     *  
     *  IMPORTANT: the references of MetroII Events could be part of the state of the 
     *  function. The events are returned in metroIIEventList and may be updated
     *  externally (the event status may change from PROPOSED to WAITING or NOTIFIED). 
     *  Therefore, when the function resumes the state, it should continue 
     *  the execution based on updated MetroII Events.  
     *  
     *  @param metroIIEventList List of MetroII Events
     *  @exception IllegalActionException.
     */
    public void startOrResume(LinkedList<Event.Builder> metroIIEventList)
            throws IllegalActionException;

    /** Reset the state of startOrResumable
     * 
     */
    public void reset();
}
