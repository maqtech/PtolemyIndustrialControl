/* A request to remove an actor.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.actor.event;

import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// RemoveActor
/**
A request to remove an actor.  The execute() method of this request
invokes the wrapup() method of the actor, then disconnects it from
the topology and sets its container to null.  The execute() method
also called createReceivers() on all remote input ports that were
connected to this actor.

@author  Edward A. Lee
@version $Id$
@see ptolemy.actor.Actor
*/
public class RemoveActor extends ChangeRequest {

    /** Construct a request with the specified originator and
     *  actor to be removed. The actor must also implement the
     *  Nameable interface or a ClassCastException will occur.
     *  @param originator The source of the change request.
     *  @param actor The actor to remove.
     */
    public RemoveActor(Nameable originator, Actor actor) {
        super(originator, "Remove " + ((Nameable)actor).getFullName());
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change by calling the wrapup() method of the
     *  actor, then disconnecting all its ports and setting its container
     *  to null.  This method also notifies the director that the
     *  schedule and type resolution may be invalid.
     *  @exception ChangeFailedException If the wrapup() method throws an
     *   exception, or if the actor is not an instance of ComponentEntity.
     */
    public void execute() throws ChangeFailedException {
        try {
            _actor.wrapup();
            if (!(_actor instanceof ComponentEntity)) {
                throw new ChangeFailedException(this,
                        "Cannot remove an actor that is not an Entity.");
            }
            ComponentEntity entity = (ComponentEntity)_actor;
            Enumeration ports = entity.getPorts();
            LinkedList farPorts = new LinkedList();
            while (ports.hasMoreElements()) {
                Port port = (Port)ports.nextElement();
                if (port instanceof IOPort) {
                    farPorts.appendElements(
                            ((IOPort)port).deepConnectedInPorts());
                }
                port.unlinkAll();
            }
            ports = farPorts.elements();
            while (ports.hasMoreElements()) {
                IOPort port = (IOPort)ports.nextElement();
                port.createReceivers();
            }
            Director director = _actor.getDirector();
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
            entity.setContainer(null);
        } catch (KernelException ex) {
            throw new ChangeFailedException(this, ex);
        }
    }

    /** Get the actor.
     *  @return The actor to be removed.
     */
    public Actor getActor() {
        return _actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor to initialize.
    private Actor _actor;
}
