/* A controller that manages multiway branches for performing
 multiway rendezvous in the CSP domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.csp.kernel;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// MultiwayBranchController

/**
 This controller manages branches for performing
 multiway rendezvous within the CSP (Communication Sequential Processes)
 domain. Any CSP actors (either atomic or composite) that
 multiway rendezvous should contain an instance of this class.
 In addition, they also needs to implement the
 ConditionalBranchActor interface.
 <p>
 The multiway branches are created within the parent
 actor that contains this controller.
 The executeBranches() method takes those branches (an array) as an
 argument, and returns when every branch is successful. A successful
 branch is a branch that succeeds with its communication.
 Only branches whose guards are true are <I>enabled</I>.
 If no branch is enabled, i.e. if all
 the guards are false, then executeBranches() returns immediately.
 If exactly one branch is enabled, then
 the corresponding communication is an ordinary rendezvous.
 If more than one branch is enabled, a separate thread is
 created and started for each enabled branch. The executeBranches()
 method then waits for all of the branches to succeed. When the last branch
 thread has finished, the method returns, allowing the parent actor
 thread to continue.

 @author Edward A. Lee
 @version $Id$
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 @see ConditionalBranch
 @see ConditionalBranchActor
 @see ConditionalReceive
 @see ConditionalSend
 */
public class MultiwayBranchController extends AbstractBranchController implements Debuggable {
    
    /** Construct a controller in the specified container, which should
     *  be an actor that implements ConditionalBranchActor.
     *  @param container The parent actor that contains this object.
     */
    public MultiwayBranchController(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute a multiway rendezvous using the specified branches.
     *  If the guard for any branch is
     *  false, then that branch is not enabled and it is ignored.
     *  If no branch is enabled, then this method returns immediately.
     *  Otherwise, it does not return until either
     *  all the branches are terminated or all have successfully
     *  completed a rendezvous.
     *  <p>
     *  If exactly one branch is enabled, then the communication is
     *  performed directly as an ordinary rendezvous.
     *  If more than one branch is enabled, a thread  is created and
     *  started for each enabled branch. These threads try to
     *  rendezvous until all succeed.
     *  <p>
     *  @param branches The set of branches involved.
     *  @return True if the branches all succeed, false if any
     *   them was terminated before completing the rendezvous.
     *  @exception IllegalActionException If the rendezvous fails
     *   (e.g. because of incompatible types).
     */
    public boolean executeBranches(ConditionalBranch[] branches) throws IllegalActionException {
        try {
            synchronized (_internalLock) {
                _failed = false;

                if (_debugging) {
                    _debug("** Executing branches.");
                }
                _branches = branches;
                
                // reset the state that controls the conditional branches
                _resetConditionalState();

                // Create the threads for the branches.
                _threadList = new LinkedList();

                ConditionalBranch onlyBranch = null;

                for (int i = 0; i < branches.length; i++) {
                    // If the guard is false, then the branch is not enabled.
                    if (branches[i].getGuard()) {
                        // Create a thread for this enabled branch
                        Nameable actor = (Nameable) branches[i].getController()
                                .getParent();
                        String name = actor.getName() + " channel " + branches[i].getID();
                        if (_debugging) {
                            _debug("** Creating branch: " + name);
                        }
                        Thread thread = new Thread((Runnable) branches[i], name);
                        _threadList.add(0, thread);
                        onlyBranch = branches[i];
                    }
                }

                // Three cases: 1) No guards were true so return immediately.
                // 2) Only one guard was true so perform the rendezvous
                // directly, 3) More than one guard was true, so start
                // the thread for each branch and wait for one of them
                // to rendezvous.
                int threadListSize = _threadList.size();

                if (threadListSize == 0) {
                    // The guards preceding all the conditional
                    // communications were false, so no branches to create.
                    if (_debugging) {
                        _debug("** No branches enabled.");
                    }
                    return true;
                } else if (threadListSize == 1) {
                    // Only one guard was true, so perform simple rendezvous.
                    if (onlyBranch instanceof ConditionalSend) {
                        Token token = onlyBranch.getToken();
                        Receiver[] receivers = onlyBranch.getReceivers();
                        if (receivers != null && receivers.length >= 1) {
                            receivers[0].putToAll(token, receivers);
                        }
                        int result = onlyBranch.getID();
                        if (_debugging) {
                            _debug("** The only enabled branch (a ConditionalSend) has succeeded: " + result);
                        }
                        return true;
                    } else {
                        // branch is a ConditionalReceive
                        Receiver[] receivers = onlyBranch.getReceivers();
                        Token token = receivers[0].get();
                        onlyBranch._setToken(token);
                        int result = onlyBranch.getID();
                        if (_debugging) {
                            _debug("** The only enabled branch (a ConditionalReceive) has succeeded: " + result);
                        }
                        return true;
                    }
                } else {
                    if (_debugging) {
                        _debug("** Starting threads.");
                    }
                    // Have a proper multiway rendezvous.
                    // Start the threads for each branch.
                    Iterator threads = _threadList.iterator();

                    while (threads.hasNext()) {
                        Thread thread = (Thread) threads.next();
                        _getDirector().addThread(thread);
                        _branchesActive++;
                        thread.start();
                    }
                }
            } // synchronized (_internalLock)
                
            // Wait for each of the threads to die.
            // It is essential that this occur outside
            // the synchronized block or we get deadlock.
            if (_debugging) {
                _debug("** Waiting for branches to succeed.");
            }
            _getDirector().threadBlocked(Thread.currentThread(), null);

            Iterator threadsIterator = _threadList.iterator();
            while (threadsIterator.hasNext()) {
                Thread thread = (Thread) threadsIterator.next();
                try {
                    thread.join();
                    if (_debugging) {
                        _debug("** Thread completed: " + thread);
                    }
                } catch (InterruptedException ex) {
                    // Ignore and continue to the next thread.
                }
            }
            _getDirector().threadUnblocked(Thread.currentThread(), null);

            // If we get to here, all the branches have succeeded
            // or been terminated.
            if (_failed) {
                if (_debugging) {
                    _debug("** At least one thread was terminated.");
                }
                return false;
            } else {
                if (_debugging) {
                    _debug("** All threads completed their rendezvous.");
                }
                return true;
            }
        } finally {
            _branches = null;
            _threadList = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Register the calling branch as failed. This reduces the count
     *  of active branches, and if all the active branches have
     *  finished, it notifies the internal lock so any threads
     *  that are blocked on it can continue.
     *  This is called by a conditional branch just before it dies.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     */
    protected void _branchFailed(int branchNumber) {
        _failed = true;
        super._branchFailed(branchNumber);
    }

    /** Indicate that the branch is not ready to rendezvous.
     *  This method does nothing.
     *  @param branchNumber The ID assigned to the branch upon creation.
     */
    protected void _branchNotReady(int branchNumber) {
    }

    /** Return true if all branches under the control of this controller
     *  are ready.
     *  @param branchNumber The ID assigned to the calling branch
     *   upon creation.
     *  @return True if the calling branch is ready
     *   to rendezvous, otherwise false.
     */
    protected boolean _isBranchReady(int branchNumber) {
        // FIXME: Can't do this... leads to deadlock. Is synchronization necessary?
        // What to synchronize on?  Need a mechanism like the _group in CSPReceiver.
        // synchronized (_internalLock) {
        for (int i = 0; i < _branches.length; i++) {
            if (!_branches[i]._isReady()) {
                if (_debugging) {
                    _debug("** Branch is not ready: " + i);
                }
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Resets the internal state controlling the execution of a conditional
     * branching construct (CIF or CDO). It is only called by chooseBranch()
     * so that it starts with a consistent state each time.
     */
    private void _resetConditionalState() {
        synchronized (_internalLock) {
            _branchesActive = 0;
            _branchesBlocked = 0;
            _threadList = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The number of conditional branches that are blocked. */
    private int _branchesBlocked = 0;
    
    /** Indicator of whether branches were terminated. */
    private boolean _failed = false;
}
