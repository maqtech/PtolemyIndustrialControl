/* A Branch transfers tokens through a channel that crosses a composite 
actor boundary. 

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.actor.process;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Branch
/**
A Branch transfers tokens through a channel that crosses a composite 
actor boundary. Branch implements the Runnable class and the execution 
of a branch object is controlled by an instantiation of the 
BranchController class. Each branch object is assigned two receiver 
objects and each of these receiver objects must implement the 
ProcessReceiver class. One of the assigned receivers is referred to as 
the producer receiver (the channel source) and the other is referred to 
as the consumer receiver (the channel destination). 
<P>
During its execution a branch attempts to get data from the producer 
receiver and put data in the consumer receiver. The communication 
semantics of the producer receiver get() method are dependent upon the 
model of computation associated with the producer receiver. In cases 
where a blocking read occurs, the producer receiver registers the block 
with the calling branch leading to a blocked branch. So that the producer 
receiver knows which branch to register the block with, a branch always 
invokes the receiver's get() method by passing itself as an argument. 
A blocked branch registers the block with the branch controller that it 
is assigned to. 
<P>
Putting data in a consumer receiver is symmetrically analogous to getting 
data from a producer receiver. In cases where a blocking write occurs, 
the consumer receiver registers the block with the calling branch leading 
to a blocked branch. So that the consumer receiver knows which branch to 
register the block with, a branch always invokes the receiver's put() 
method by passing itself as an argument. 
 
@author John S. Davis II
@version $Id$
*/

public class Branch implements Runnable {

    /** Construct a branch object with a branch controller.
     * 
     *  @param cntlr The branch controller assigned to this branch.
     *  @deprecated Use this constructor for testing purposes only.
     */
    public Branch(BranchController cntlr) throws 
    	    IllegalActionException {
        _controller = cntlr;
    }
 
    /** Construct a branch object with a producer receiver, a consumer 
     *  receiver and a branch controller.
     * 
     *  @param prodRcvr The producer receiver assigned to this branch.
     *  @param consRcvr The consumer receiver assigned to this branch.
     *  @param cntlr The branch controller assigned to this branch.
     *  @exception IllegalActionException If the receivers assigned to
     *   this branch are null or improperly configured.
     */
    public Branch(ProcessReceiver prodRcvr, ProcessReceiver consRcvr, 
	    BranchController cntlr) throws IllegalActionException {
        _controller = cntlr;
        
        if( prodRcvr == null || consRcvr == null ) {
            throw new IllegalActionException("The boundary "
            	    + "receivers of this branch are null.");
        }
        if( !prodRcvr.isProducerReceiver() ) {
	    String name = ((Nameable)consRcvr.getContainer()).getName();
            throw new IllegalActionException("Receiver: " + name + 
		    " Not producer receiver");
        }
	_prodRcvr = prodRcvr;
        
        if( !consRcvr.isConsumerReceiver() ) {
	    String name = ((Nameable)consRcvr.getContainer()).getName();
            throw new IllegalActionException("Receiver: " + name + 
		    " Not consumer receiver");
        }
	_consRcvr = consRcvr;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the consumer receiver that this branch puts data into.
     * 
     *  @return The consumer receiver that this branch puts data into.
     *  @see ptolemy.actor.process.BoundaryDetector
     */
    public ProcessReceiver getConsReceiver() {
        return _consRcvr;
    }

    /** Return the producer receiver that this branch gets data from.
     * 
     * @return The producer receiver that this branch gets data from.
     * @see ptolemy.actor.process.BoundaryDetector
     */
    public ProcessReceiver getProdReceiver() {
        return _prodRcvr;
    }

    /** Return true if this branch is active. 
     * 
     *  @return True if this branch is still alive.
     */
    public boolean isActive() {
        return _active;
    }

    /** Register that the receiver controlled by this branch
     *  is blocked. The blocked receiver, either the producer
     *  or consumer receiver (but not both) are passed as an
     *  argument according to which one is blocked.
     * 
     *  @param rcvr The receiver assigned to this branch that
     *   is blocked.
     */
    public void registerRcvrBlocked(ProcessReceiver rcvr) {
    	if( !_rcvrBlocked ) {
    	    _rcvrBlocked = true;
            _controller._branchBlocked(rcvr);
        }
    }

    /** Register that the receiver controlled by this branch
     *  is no longer blocked. 
     *
     *  @param rcvr The receiver assigned to this branch for 
     *   which a block is being removed. 
     */
    public void registerRcvrUnBlocked(ProcessReceiver rcvr) {
    	if( _rcvrBlocked ) {
    	    _rcvrBlocked = false;
            _controller._branchUnBlocked(rcvr);
        }
    }

    /** Repeatedly transfer a single token between the producer 
     *  receiver and the consumer receiver as long as the branch
     *  is active or until a TerminateProcessException is thrown. 
     *  NOTE: This method could probably be more efficient.
     */
    public void run() {
        try {
            setActive(true);
            while( isActive() ) {
                transferToken();
            }
        } catch( TerminateProcessException e ) {
            return;
        }
    }
    
    /** Set a flag indicating this branch is no longer active.
     * 
     *  @param value A boolean indicating whether this branch is 
     *   still active.
     */
    public void setActive(boolean value) {
        _active = value;
    }

    /** Transfer a single token from the producer receiver to the
     *  consumer receiver. If either the producer receiver or
     *  consumer receiver is null then return without attempting
     *  token transfer.
     */
    public void transferToken() {
        if( _prodRcvr == null ) {
            return;
        } else if ( _consRcvr == null ) {
            return;
        }
        Token token = _prodRcvr.get(this); 
        _consRcvr.put(token, this);
    }
    
    //////////////////////////////////////////////////////////////////
    ////                       protected methods                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _active = false;
    private BranchController _controller;
    private ProcessReceiver _prodRcvr;
    private ProcessReceiver _consRcvr;
    private boolean _rcvrBlocked = false;

}
