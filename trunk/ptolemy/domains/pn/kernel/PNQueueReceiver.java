/* A receiver with a FIFO queue and performing blocking reads
   and blocking writes.

   Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.domains.pn.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.process.BoundaryDetector;
import ptolemy.actor.process.Branch;
import ptolemy.actor.process.ProcessReceiver;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// PNQueueReceiver
/**

A receiver with a FIFO queue that blocks the calling process on a read if the
FIFO queue is empty and on a write if the queue is full. Blocking read provides
the basic functionality of a FIFO channel in the process networks model of
computation. Blocking write supports the implementation suggested by Parks for
bounded memory execution of process networks.
<p>
Tokens are appended to the queue with the put() method, which blocks on a write
if the queue is full. Tokens are removed from the queue with the get() method,
which blocks on a read if the queue is empty.
In case a process blocks on a read or a write, the receiver informs the
director about the same.
The receiver also unblocks processes blocked on a read or a write. In case
a process is blocked on a read (read-blocked), it is unblocked on availability
of a token.  If a process is blocked on a write (write-blocked), it
is unblocked on the availability of room in the queue and informs the director
of the same.
<p>
This class is also responsible for pausing or terminating a process that tries
to read from or write to the receiver. In case of termination, the receiver
throws a TerminateProcessException when a process tries to read from or write
to the receiver. This terminates the process.
In case of pausing, the receiver suspends the process when it tries to read
from or write to the receiver and resumes it only after a request to resume the
process has been received.
<p>

@author Mudit Goel, John S. Davis II
@version $Id$
@since Ptolemy II 0.2
@Pt.ProposedRating Green (mudit)
@Pt.AcceptedRating Green (davisj)
@see QueueReceiver
@see ptolemy.actor.QueueReceiver
*/
public class PNQueueReceiver extends QueueReceiver implements ProcessReceiver {

    /** Construct an empty receiver with no container
     */
    public PNQueueReceiver() {
        super();
        _boundaryDetector = new BoundaryDetector(this);
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of this receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public PNQueueReceiver(IOPort container) throws IllegalActionException {
        super(container);
        _boundaryDetector = new BoundaryDetector(this);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Prepare to register a block. If the branch object specified as
     *  a parameter is non-null then register the block with the branch.
     *  If the branch object specified as a parameter is null then
     *  register the block with the local director.
     *
     *  @param branch The Branch managing execution of this method.
     */
    public synchronized void prepareToBlock(Branch branch) {
        if ( branch != null ) {
            branch.registerReceiverBlocked(this);
            _otherBranch = branch;
        } else {
            PNDirector director = ((PNDirector)((Actor)
                                           (getContainer().getContainer())).getExecutiveDirector());
            director._actorBlocked(this);
            _otherBranch = branch;
        }
    }

    /** Unblock this receiver and register this new state with
     *  either the monitoring branch or the local director. If
     *  there is no blocked branch waiting, then register the
     *  new state with the local director; otherwise, register
     *  the new state with the blocked branch.
     */
    public synchronized void wakeUpBlockedPartner() {
        if ( _otherBranch != null ) {
            _otherBranch.registerReceiverUnBlocked(this);
        } else {
            PNDirector director = ((PNDirector)((Actor)
                                           (getContainer().getContainer())).getExecutiveDirector());
            director._actorUnBlocked(this);

        }
        notifyAll();
    }

    /** Get a token from this receiver. If the receiver is empty then
     *  block until a token becomes available. Use the local director
     *  to manage blocking reads that occur. If this receiver is
     *  terminated during the execution of this method, then throw a
     *  TerminateProcessException.
     *
     *  @return The token contained by this receiver.
     */
    public Token get() {
        return get(null);
    }

    /** Remove and return the oldest token from the FIFO queue contained
     *  in the receiver. Terminate the calling process by throwing a
     *  TerminateProcessException if requested.
     *  Otherwise, if the FIFO queue is empty, then suspend the calling
     *  process and inform the director of the same.
     *  If a new token becomes available to the FIFO queue, then resume the
     *  suspended process.
     *  If the queue was not empty, or on availability of a new token (calling
     *  process was suspended), take the oldest token from the FIFO queue.
     *  Check if any process is blocked on a write to this
     *  receiver. If a process is indeed blocked, then unblock the
     *  process, and inform the director of the same.
     *  Otherwise return.
     *  @return The oldest Token read from the queue
     */
    public Token get(Branch branch) {
        Workspace workspace = getContainer().workspace();
        PNDirector director = ((PNDirector)
                ((Actor)(getContainer().getContainer()))
                .getExecutiveDirector());
        Token result = null;
        synchronized (this) {
            while (!_terminate && !super.hasToken()) {
                _readBlocked = true;
                prepareToBlock(branch);
                // director._actorBlocked(this);
                while (_readBlocked && !_terminate) {
                    // checkIfBranchIterationIsOver(branch);
                    try {
                        workspace.wait(this);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                result = super.get();
                //Check if pending write to the Queue;
                if (_writeBlocked) {
                    // director._actorUnBlocked(this);
                    wakeUpBlockedPartner();
                    _writeBlocked = false;
                    notifyAll(); // Wake up threads waiting on a write;
                }
            }
            return result;
        }
    }

    /** Return true, since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @return True.
     */
    public boolean hasRoom() {
        return true;
    }

    /** Return true, since a channel in the Kahn process networks
     *  model of computation is of infinite capacity and always has room.
     *  @return True.
     */
    public boolean hasRoom(int tokens) {
        return true;
    }

    /** Return true, since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @return True.
     */
    public boolean hasToken() {
        return true;
    }

    /** Return true, since a call to the get() method of the receiver will
     *  always return a token if the call to get() ever returns.
     *  @return True.
     */
    public boolean hasToken(int tokens) {
        return true;
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundary() {
        return _boundaryDetector.isConnectedToBoundary();
    }

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is connected to the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryInside() {
        return _boundaryDetector.isConnectedToBoundaryInside();
    }

    /** Return true if this receiver is connected to the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  This method is not synchronized so the caller
     *  @return True if this receiver is connected to the outside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isConnectedToBoundaryOutside() {
        return _boundaryDetector.isConnectedToBoundaryOutside();
    }

    /** This class serves as an example of a ConsumerReceiver and
     *  hence this method returns true;
     */
    public boolean isConsumerReceiver() {
        if ( isConnectedToBoundary() ) {
            return true;
        }
        return false;
    }

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false.
     *  <P>
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public boolean isInsideBoundary() {
        return _boundaryDetector.isInsideBoundary();
    }

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false.
     *  <P>
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     *  @see BoundaryDetector
     */
    public boolean isOutsideBoundary() {
        return _boundaryDetector.isOutsideBoundary();
    }

    /** This class serves as an example of a ProducerReceiver and
     *  hence this method returns true;
     */
    public boolean isProducerReceiver() {
        if ( isOutsideBoundary() || isInsideBoundary() ) {
            return true;
        }
        return false;
    }

    /** Return a true or false to indicate whether there is a read block
     *  on this receiver or not, respectively.
     *  @return a boolean indicating whether a read is blocked on this
     *  receiver or not.
     */
    public synchronized boolean isReadBlocked() {
        return _readBlocked;
    }

    /** Return a true or false to indicate whether there is a write block
     *  on this receiver or not.
     *  @return A boolean indicating whether a write is blocked  on this
     *  receiver or not.
     */
    public synchronized boolean isWriteBlocked() {
        return _writeBlocked;
    }

    /** Put a token on the queue contained in this receiver.
     *  If the queue is full, then suspend the calling process (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting a room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token) {
        put(token, null);
    }

    /** Put a token on the queue contained in this receiver.
     *  If the queue is full, then suspend the calling process (blocking
     *  write) and inform the director of the same. Resume the process on
     *  detecting room in the queue.
     *  If a termination is requested, then initiate the termination of the
     *  calling process by throwing a TerminateProcessException.
     *  On detecting room in the queue, put a token in the queue.
     *  Check whether any process is blocked
     *  on a read from this receiver. If a process is indeed blocked, then
     *  unblock the process, and inform the director of the same.
     *  @param token The token to be put in the receiver.
     */
    public void put(Token token, Branch branch) {
        Workspace workspace = getContainer().workspace();
        PNDirector director = (PNDirector)
            ((Actor)(getContainer().getContainer())).getExecutiveDirector();
        synchronized(this) {
            // if (!super.hasRoom()) {
            while (!_terminate && !super.hasRoom()) {
                _writeBlocked = true;
                prepareToBlock(branch);
                // director._actorBlocked(this);
                while (_writeBlocked && !_terminate ) {
                    // while (!_terminate && !super.hasRoom()) {
                    // while (_writeBlocked) {
                    // checkIfBranchIterationIsOver(branch);
                    try {
                        workspace.wait(this);
                    } catch (InterruptedException e) {
                        _terminate = true;
                        break;
                    }
                    // }
                }
            }
            if (_terminate) {
                throw new TerminateProcessException("");
            } else {
                //token can be put in the queue;
                super.put(token);
                //Check if pending write to the Queue;
                if (_readBlocked) {
                    wakeUpBlockedPartner();
                    // director._actorUnBlocked(this);
                    _readBlocked = false;
                    notifyAll();
                    //Wake up all threads waiting on a write to this receiver;
                }
            }
        }
    }

    /** Reset the state variables in the receiver.
     */
    public void reset() {
        _readBlocked = false;
        _writeBlocked = false;
        _terminate = false;
        _boundaryDetector.reset();
    }

    /** Set a state flag indicating that there is a process blocked while
     *  trying to read from this receiver.
     *  @param readPending true if the calling process is blocking on a
     *  read, false otherwise.
     */
    public synchronized void setReadPending(boolean readPending) {
        _readBlocked = readPending;
    }

    /** Set a state flag indicating that there is a process blocked
     *  (write-blocked) while trying to write to the receiver.
     *  @param writePending true if the calling process is blocking on
     *  a write, false otherwise.
     */
    public synchronized void setWritePending(boolean writePending) {
        _writeBlocked = writePending;
    }

    /** Set a flag in the receiver to indicate the onset of termination.
     *  This will result in termination of any process that is either blocked
     *  on the receiver or is trying to read from or write to it.
     */
    public synchronized void requestFinish() {
        _terminate = true;
        notifyAll();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _readBlocked = false;
    private boolean _writeBlocked = false;
    private boolean _terminate = false;

    private Branch _otherBranch = null;
    private BoundaryDetector _boundaryDetector;
}
