/* A receiver that stores time stamped tokens according to DDE semantics.

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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DDEReceiver
/**
A receiver that stores time stamped tokens according to DDE
semantics. A <I>time stamped token</I> is a token that has a
time stamp associated with it. A DDEReceiver stores time
stamped tokens by enforcing a blocking read and blocking
write style. Time stamped tokens are appended to the queue
with either of the put() methods, both of which block on a
write if the queue is full. Time stamped tokens are removed
from the queue via the get() method that blocks on a read if
the queue is empty. If a process blocks on a read or a write,
the director is informed. Blocks are removed (and the director
is informed) if the conditions of the queue contents that led
to blocking no longer exist.
<P>
The key difference between DDEReceiver and TimedQueueReceiver
is that get() and put() block as described above. In fact,
the blocking mechanism of DDEReceiver is such that hasToken()
blocks as well. If hasToken() is called while the receiver is
empty, then hasToken() will block until a token is available.
<P>
This class assumes that valid time stamps have non-negative
values. Several reserved negative values exist for special
purposes: INACTIVE, IGNORE and RECEIVER. These values are
public attributes of TimedQueueReceiver.


@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.TimedQueueReceiver
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class DDEReceiver extends TimedQueueReceiver
    implements ProcessReceiver {

    /** Construct an empty receiver with no container.
     */
    public DDEReceiver() {
        super();
    }

    /** Construct an empty receiver with the specified container.
     * @param container The IOPort that contains this receiver.
     */
    public DDEReceiver(IOPort container) {
        super(container);
    }

    /** Construct an empty queue with the specified IOPort container
     *  and priority.
     * @param container The IOPort that contains this receiver.
     * @param priority The priority of this receiver.
     */
    public DDEReceiver(IOPort container, int priority) {
        super(container, priority);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove ignored tokens from this receiver. If the receiver
     *  time of this receiver is equal to TimedQueueReceiver.IGNORE,
     *  then take the oldest token off of the queue. If after doing
     *  so no other tokens are on the queue, then set the receiver
     *  time of this receiver to be equal to the current time of the
     *  time keeper controlling this receiver.
     * @see ptolemy.domains.dde.kernel.TimedQueueReceiver
     */
    public synchronized void clearIgnoredTokens() {
        if( getRcvrTime() != TimedQueueReceiver.IGNORE ) {
            return;
        }

	// Remove Ignored Token
	super.get();

	// Set the receiver time if queue is empty
	if( (getRcvrTime() == TimedQueueReceiver.IGNORE)
		&& !super.hasToken() ) {
	    Thread thread = Thread.currentThread();
	    DDEThread ddeThread;
	    if( thread instanceof DDEThread ) {
		ddeThread = (DDEThread)thread;
		TimeKeeper keeper = ddeThread.getTimeKeeper();
		setRcvrTime( keeper.getCurrentTime() );
	    }
	}
    }

    /** Do a blocking read on the queue. If no token is available,
     *  then inform the director that this receiver is blocking on
     *  a read and wait until a token becomes available. When a
     *  token becomes available, determine if this queue has the
     *  unique oldest receiver time with respect to all of the
     *  receivers contained by the actor that contains this receiver;
     *  if so, return the token. If the receiver time is a non-unique
     *  minimum then determine if this receiver has the highest
     *  priority of all receivers that share the non-unique minimum
     *  receiver time and if so, return the token. Otherwise throw a
     *  NoTokenException. If at any point during this method this
     *  receiver is scheduled for termination, then throw a
     *  TerminateProcessException to cease execution of the actor
     *  that contains this receiver.
     *  <P>
     *  IMPORTANT: This method is designed to be called after
     *  hasToken() has been called. Verify that this method is
     *  safe to call by calling hasToken() first.
     * @return Token The oldest token on this queue if this queue has
     *  the minimum receiver time of all receivers contained by the
     *  actor that contains this receiver.
     * @exception NoTokenException If this method is called while
     *  hasToken() returns false.
     */
    public Token get() throws NoTokenException {
        DDEDirector director = (DDEDirector)
            ((Actor)getContainer().getContainer()).getDirector();
	synchronized( this ) {
	    if( _terminate ) {
		throw new TerminateProcessException("");
	    }
	    Token token = super.get();
	    if( _writePending ) {
                director.removeWriteBlock( this );
		_writePending = false;
		notifyAll();
	    }
	    Thread thread = Thread.currentThread();
	    if( thread instanceof DDEThread ) {
		TimeKeeper timeKeeper =
                    ((DDEThread)thread).getTimeKeeper();
		timeKeeper.sendOutNullTokens(this);
	    }
	    return token;
	}
    }

    /** Return true if the get() method of this receiver will return a
     *  token without throwing a NoTokenException. If this receiver has
     *  a receiver time that is not less than or equal to the receiver
     *  time of all receivers contained by the actor that contains this
     *  receiver then return false. If this receiver has a receiver time
     *  that is equal to the minimum receiver time of all receivers
     *  contained by the actor that contains this receiver and at least
     *  one receiver has a receiver time equal to that of this receiver,
     *  then return false if this receiver has a lower priority when
     *  compared to all receivers sharing its receiver time. Otherwise,
     *  block until this receiver contains a token. If at any point during
     *  this method this receiver is scheduled for termination, then throw
     *  a TerminateProcessException to cease execution of the actor that
     *  contains this receiver.
     * @return Return true if the get() method of this receiver will
     *  return a token without throwing a NoTokenException.
     */
    public boolean hasToken() {
	Workspace workspace = getContainer().workspace();
        DDEDirector director = (DDEDirector)
            ((Actor)getContainer().getContainer()).getDirector();
	Thread thread = Thread.currentThread();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper =
                ((DDEThread)thread).getTimeKeeper();
	    return _hasToken( workspace, director,
            	    timeKeeper, _hideNullTokens );
	}
	return false;
    }

    /** Return true if this receiver is contained on the inside of a 
     *  boundary port. A boundary port is an opaque port that is 
     *  contained by a composite actor. If this receiver is contained 
     *  on the inside of a boundary port then return true. Otherwise 
     *  return false. This method is not synchronized so the caller 
     *  should be.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     */
     public boolean isInsideBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 
         if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
             // This receiver is contained by the port 
             // of a composite actor.
             if( innerPort.isOutput() && !innerPort.isInput() ) {
                 return true;
             } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                 return false;
             } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else {
                 // FIXME: The following only works if the port is not 
                 // both an input and output.
                 throw new IllegalArgumentException("A port that is "
                         + "both an input and output can not be " 
                         + "properly dealt with by "
                         + "DDEReceiver.isInsideBoundary");
             }
         } 
         return false;
     }

    /** Return true if this receiver is contained on the inside of a 
     *  boundary port. A boundary port is an opaque port that is 
     *  contained by a composite actor. If this receiver is contained 
     *  on the inside of a boundary port then return true. Otherwise 
     *  return false. This method is not synchronized so the caller 
     *  should be.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     */
     public boolean isOutsideBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 
         if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
             // This receiver is contained by the port 
             // of a composite actor.
             if( innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                 return true;
             } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else {
                 // FIXME: The following only works if the port is not 
                 // both an input and output.
                 throw new IllegalArgumentException("A port that is "
                         + "both an input and output can not be " 
                         + "properly dealt with by "
                         + "DDEReceiver.isInsideBoundary");
             }
         } 
         return false;
     }

    /** Return true if this receiver is contained in a boundary port or
     *  is connected to the inside of a boundary port. A boundary port 
     *  is an opaque port that is contained by a composite actor. If this 
     *  receiver is contained by a boundary port or is connected to the
     *  inside of a boundary port, then return true. Otherwise return false. 
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver contained by or is connected to 
     *   the inside of a boundary port; return false otherwise.
     */
     public boolean isBoundaryReceiver() {
         IOPort innerPort = (IOPort)getContainer();
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 
         if( !innerEntity.isAtomic() && innerPort.isOpaque() ) {
             // This receiver is contained by the port 
             // of a composite actor.
             if( innerPort.isOutput() && !innerPort.isInput() ) {
                 return true;
             } else if( !innerPort.isOutput() && innerPort.isInput() ) {
                 return false;
             } else if( !innerPort.isOutput() && !innerPort.isInput() ) {
                 return false;
             } else {
                 // FIXME: The following only works if the port is not 
                 // both an input and output.
                 throw new IllegalArgumentException("A port that is "
                         + "both an input and output can not be " 
                         + "properly dealt with by "
                         + "DDEReceiver.isBoundaryReceiver");
             }
         } 
         
         Port outerPort = null; 
         Enumeration enum = innerPort.connectedPorts(); 
         ComponentEntity outerEntity = null; 
         while( enum.hasMoreElements() ) {
             outerPort = (Port)enum.nextElement();
             outerEntity = (ComponentEntity)outerPort.getContainer();
             /*
             if( !outerEntity.isAtomic() && outerPort.isOpaque() ) {
             */
             if( outerEntity == innerEntity.getContainer() ) {
		 // We are connected to a boundary port. Now
		 // determine if the boundary port is connected
		 // to this relation.
                 try {
		 Receiver[][] rcvrs = 
                         ((IOPort)outerPort).deepGetReceivers();
		 for( int i = 0; i < rcvrs.length; i++ ) {
		     for( int j = 0; j < rcvrs[i].length; j++ ) {
		         if( this == rcvrs[i][j] ) {
			     return true;
			 }
		     }
		 }
                 } catch( IllegalActionException e) {
                     // FIXME: Do Something!
                 }
             }
         }
         return false;
     }

    /** Return true if this receiver is connected to the inside of a 
     *  boundary port. A boundary port is an opaque port that is contained 
     *  by a composite actor. If this receiver is connected to the inside 
     *  of a boundary port, then return true. Otherwise return false. 
     *  Note that this method will return false if this receiver is 
     *  contained in a boundary port.
     *  This method is not synchronized so the caller should be.
     *  @return True if this receiver is connected to the inside of a 
     *   boundary port; return false otherwise.
     */
     public boolean isConnectedToBoundary() {
         IOPort innerPort = (IOPort)getContainer();
         ComponentEntity innerEntity = 
                 (ComponentEntity)innerPort.getContainer(); 

         Port outerPort = null; 
         Enumeration enum = innerPort.connectedPorts(); 
         ComponentEntity outerEntity = null; 
         while( enum.hasMoreElements() ) {
             outerPort = (Port)enum.nextElement();
             outerEntity = (ComponentEntity)outerPort.getContainer();
             /*
             if( !outerEntity.isAtomic() && outerPort.isOpaque() ) {
             */
             if( outerEntity == innerEntity.getContainer() ) {
		 // We are connected to a boundary port. Now
		 // determine if the boundary port is connected
		 // to this relation.
                 try {
		 Receiver[][] rcvrs = 
                         ((IOPort)outerPort).deepGetReceivers();
		 for( int i = 0; i < rcvrs.length; i++ ) {
		     for( int j = 0; j < rcvrs[i].length; j++ ) {
		         if( this == rcvrs[i][j] ) {
			     return true;
			 }
		     }
		 }
                 } catch( IllegalActionException e) {
                     // FIXME: Do Something!
                 }
             }
         }
         return false;
     }
     
    /** Do a blocking write on the queue. Set the time stamp to be
     *  the current time of the sending actor. If the time stamp of
     *  the token is greater than the completionTime of this receiver,
     *  then set the time stamp to INACTIVE and the token to null. If
     *  the queue is full, then inform the director that this receiver
     *  is blocking on a write and wait until room becomes available.
     *  When room becomes available, put the token and time stamp in
     *  the queue and inform the director that the block no longer
     *  exists. If at any point during this method this receiver is
     *  scheduled for termination, then throw a TerminateProcessException
     *  which will cease activity for the actor that contains this
     *  receiver.
     * @param token The token to put on the queue.
     */
    public void put(Token token) {
	Thread thread = Thread.currentThread();
	double time = getLastTime();
	if( thread instanceof DDEThread ) {
	    TimeKeeper timeKeeper = ((DDEThread)thread).getTimeKeeper();
	    time = timeKeeper.getOutputTime();
	}
	put( token, time );
    }

    /** Do a blocking write on the queue. If at any point during
     *  this method this receiver is scheduled for termination,
     *  then throw a TerminateProcessException which will cease
     *  activity for the actor that contains this receiver. If
     *  the specified time stamp of the token is greater than the
     *  completionTime of this receiver, then set the time stamp
     *  to INACTIVE. If the queue is full, then inform the director
     *  that this receiver is blocking on a write and wait until
     *  room becomes available. When room becomes available, put
     *  the token and time stamp in the queue and inform the director
     *  that the block no longer exists.
     * @param token The token to put on the queue.
     * @param time The time stamp associated with the token.
     */
    public void put(Token token, double time) {
        Workspace workspace = getContainer().workspace();
        DDEDirector director = (DDEDirector)
            ((Actor)getContainer().getContainer()).getDirector();
	_put(token, time, workspace, director);
    }

    /** Schedule this receiver to terminate. After this method is
     *  called, a TerminateProcessException will be thrown during
     *  the next call to get() or put() of this class.
     */
    public synchronized void requestFinish() {
        _terminate = true;
	notifyAll();
    }

    /** Set the pause flag of this receiver. If the flag is set to true,
     *  then pause any process that tries to read from or write to this
     *  receiver. If the flag is false, then resume any process that
     *  tries to read from or write to this receiver.
     *  NOTE: This method is not implemented but is included in
     *  accordance with the constraints of the ProcessReceiver
     *  interface.
     * @param flag The boolean pause flag of this receiver.
     */
    public synchronized void requestPause(boolean pause) {
	;
    }

    /** Reset local flags. The local flag of this receiver indicates
     *  whether this receiver is scheduled for termination. Resetting
     *  the termination flag will make sure that this receiver is not
     *  scheduled for termination.
     */
    public void reset() {
	super.reset();
	_terminate = false;
    	_readPending = false;
    	_writePending = false;
    	_ignoreNotSeen = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     package friendly methods   	   ////

    /** Indicate whether hasToken() should return true if the only
     *  available tokens it finds are NullTokens. Specify that
     *  NullTokens should not be taken into consideration by
     *  hasToken() if the parameter is true; otherwise do consider
     *  NullTokens.
     * @parameter hide The parameter indicating whether NullTokens
     *  should be taken into consideration by hasToken().
     */
    void hideNullTokens(boolean hide) {
	_hideNullTokens = hide;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods 		   ////

    /** This method provides the recursive
     *  functionality of hasToken()
     */
    private synchronized boolean _hasToken(Workspace workspace,
	    DDEDirector director, TimeKeeper timeKeeper,
	    boolean _hideNullTokens ) {

	timeKeeper.resortRcvrList();
        if( timeKeeper.getNextTime() == INACTIVE ) {
            requestFinish();
	}
        if( getRcvrTime() == INACTIVE && !_terminate ) {
	    return false;
	}
        if( getRcvrTime() == IGNORE && !_terminate ) {
	    if( _ignoreNotSeen ) {
		_ignoreNotSeen = false;
		return false;
	    } else {
		_ignoreNotSeen = true;
		clearIgnoredTokens();
		// Call the next line since
                // TimeKeeper.updateIgnoredReceivers()
		// has not been called.
		timeKeeper.setIgnoredTokens(false);
		return false;
	    }
        }
	if( getRcvrTime() > timeKeeper.getNextTime() &&
        	!_terminate ) {
	    return false;
	}
        if( super.hasToken() && !_terminate ) {
	    if( !timeKeeper.hasMinRcvrTime() ) {
		if( hasNullToken() ) {
		    if( timeKeeper.getHighestPriorityReal() != null ) {
			return false;
		    } else if( this !=
                    	    timeKeeper.getHighestPriorityNull() ) {
			return false;
		    } else if( !_hideNullTokens ) {
			return true;
		    } else {
			super.get();
			timeKeeper.sendOutNullTokens(this);
			return _hasToken(workspace, director,
				timeKeeper, _hideNullTokens);
		    }
		} else {
		    return true;
		}
	    } else {
		if( hasNullToken() ) {
		    if( !_hideNullTokens ) {
			return true;
		    }
		    super.get();
		    timeKeeper.sendOutNullTokens(this);
		    return _hasToken(workspace, director,
                            timeKeeper, _hideNullTokens);
		}
		return true;
	    }
	}
	if( !super.hasToken() && !_terminate ) {
	    _readPending = true;
            // BEGIN JFIXME
            if( isConnectedToBoundary() ) {
            // if( isBoundaryReceiver() ) {
                // JFIXME
         	String name = ((Nameable)getContainer().getContainer()).getName();
                System.out.println(name+": blocked on external read.");
         	// END JFIXME
                director.addExternalReadBlock();
            } else {
                // JFIXME
         	String name = ((Nameable)getContainer().getContainer()).getName();
                System.out.println(name+": blocked on internal read.");
         	// END JFIXME
         
                director.addInternalReadBlock();
            }
	    // END JFIXME: director.addInternalReadBlock();
	    while( _readPending && !_terminate ) {
		workspace.wait( this );
	    }
	}
	if( _terminate ) {
	    if( _readPending ) {
		_readPending = false;
                // BEGIN JFIXME
            	if( isBoundaryReceiver() ) {
                    // JFIXME
         	    String name = ((Nameable)getContainer().getContainer()).getName();
                    System.out.println(name+": removed an external read block.");
         	    // END JFIXME
		    director.removeExternalReadBlock();
                } else {
                    // JFIXME
         	    String name = ((Nameable)getContainer().getContainer()).getName();
                    System.out.println(name+": removed an internal read block.");
         	    // END JFIXME
		    director.removeInternalReadBlock();
                }
		// END JFIXME: director.removeReadBlock();
	    }
            throw new TerminateProcessException("");
	} else {
            return _hasToken(workspace, director,
            	    timeKeeper, _hideNullTokens);
	}
    }

    /** This method provides the recursive functionality
     *  of put(Token, double).
     */
    private void _put(Token token, double time, Workspace workspace,
	    DDEDirector director) {
        synchronized(this) {
            if( time > getCompletionTime() &&
                    getCompletionTime() != ETERNITY && !_terminate ) {
	        time = INACTIVE;
	    }

            if( super.hasRoom() && !_terminate ) {
                super.put(token, time);
		if( _readPending ) {
                    // BEGIN JFIXME 
                    if( isBoundaryReceiver() ) {
                    	// JFIXME
         	    	String name = ((Nameable)getContainer().getContainer()).getName();
                    	System.out.println(name+": removed an external read block.");
         	    	// END JFIXME
		        director.removeExternalReadBlock();
                    } else {
                    	// JFIXME
         	    	String name = ((Nameable)getContainer().getContainer()).getName();
                    	System.out.println(name+": removed an internal read block.");
         	    	// END JFIXME
		        director.removeInternalReadBlock();
                    }
		    // END JFIXME: director.removeReadBlock();
		    _readPending = false;
		    notifyAll();
		}
                return;
            }

            if ( !super.hasRoom() && !_terminate ) {
		_writePending = true;
                director.addWriteBlock(this);
		while( _writePending && !_terminate ) {
		    workspace.wait( this );
		}
            }
            if( _terminate ) {
		if( _writePending ) {
		    _writePending = false;
                    director.removeWriteBlock( this );
		}
                throw new TerminateProcessException( getContainer(),
                        "This receiver has been terminated "
                        + "during _put()");
            } else {
                _put(token, time, workspace, director);
            }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _terminate = false;
    private boolean _readPending = false;
    private boolean _writePending = false;
    private boolean _ignoreNotSeen = true;
    private boolean _hideNullTokens = true;
}
