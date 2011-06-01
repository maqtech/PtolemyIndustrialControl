/*
 Thread on which a Ptolemy simulation will be executed
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver;

import java.util.UUID;
import java.util.logging.Level;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// SimulationThread

/** 
 * Launch the simulation on the current thread under the provided
 * ticket reference and wait for the user to issue control commands.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public class SimulationThread extends Thread {

    /**
     * Create an instance of the simulation thread running on the Ptolemy
     * server application.
     * 
     * @param ticket Reference to the simulation request
     * @exception Exception If the simulation encounters a problem setting
     * the director or getting workspace access, throw an exception so that
     * the caller is notified. 
     */
    public SimulationThread(Ticket ticket) throws Exception {
        _ticket = ticket;
        _remoteModel = new RemoteModel(UUID.randomUUID().toString(), _ticket
                .getTicketID().toString() + "_CLIENT", _ticket.getTicketID()
                .toString() + "_SERVER", RemoteModelType.SERVER);

        CompositeActor topLevelActor = _remoteModel.getTopLevelActor();
        if (topLevelActor != null) {
            Manager manager = new Manager(topLevelActor.workspace(), _ticket
                    .getTicketID().toString());
            topLevelActor.setManager(manager);

            PNDirector director = new PNDirector(topLevelActor.workspace());
            topLevelActor.setDirector(director);
        }
    }

    /**
     * Start the execution of the simulation by kicking off the thread.
     */
    public void run() {

        Manager manager = getManager();
        if (manager != null) {
            try {
                manager.execute();
            } catch (IllegalActionException e) {
                PtolemyServer.LOGGER.log(Level.WARNING, String.format("%s: %s",
                        _ticket.getTicketID().toString(),
                        "The simulation is already running."));
                throw new IllegalStateException(e);
            } catch (KernelException e) {
                PtolemyServer.LOGGER.log(Level.SEVERE, String.format(
                        "%s: %s - %s", _ticket.getTicketID().toString(),
                        "A KernelException has been thrown", e.getMessage()));
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Get the manager responsible for coordinating the model of computation.
     * 
     * @return The Manager used to control the simulation
     */
    public Manager getManager() {
        CompositeActor topLevelActor = _remoteModel.getTopLevelActor();
        if (topLevelActor == null) {
            return null;
        } else {
            return topLevelActor.getManager();
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables

    // References the simulation by the Ptolemy server
    private final Ticket _ticket;
    // Replace actors and accesses the manager of the simulation
    private final RemoteModel _remoteModel;
}
