/* Three Dimensional (GR) domain receiver.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.*;
import ptolemy.domains.gr.lib.*;

import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Iterator;

import javax.media.j3d.*;


//////////////////////////////////////////////////////////////////////////
//// 3DReceiver
/**
@author C. Fong
@version $Id$
*/
public class GRReceiver extends AbstractReceiver {

    /** Construct an empty receiver with no container.
     */
    public GRReceiver() {
        super();
        _queue = new QueueBuffer();
        _init();
    }

    /** Construct an empty receiver with no container and given size.
     *  @param size The size of the buffer for the receiver.
     */
    public GRReceiver(int size) {
        super();
        _queue = new QueueBuffer();
        _init();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     */
    public GRReceiver(IOPort container) {
        super(container);
        _queue = new QueueBuffer();
        _init();
    }

    /** Construct an empty receiver with the specified container and size.
     *  @param container The container of the receiver.
     *  @param size  The size of the buffer for the receiver.
     */
    public GRReceiver(IOPort container, int size) {
        super(container);
      	_queue = new QueueBuffer(size);
        _init();
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enumerate the tokens in the receiver, beginning with the oldest.
     *  @return An enumeration of tokens.
     */
    public Enumeration elements() {
        return _queue.elements();
    }
    
    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.
     *  @return The oldest token in the receiver.
     *  @exception NoTokenException If there is no token in the receiver.
     */
    public Token get() {
        Token t = null;
        try {
            t = (Token)_queue.take();
        } catch (NoSuchElementException ex) {
            // The queue is empty.
            throw new NoTokenException(getContainer(),
                    "Attempt to get token from an empty QueueReceiver.");
        }
        return t;
    }
    
    /** Return a token in the receiver or its history. If the offset
     *  argument is zero, return the oldest token in the receiver.
     *  If the offset is 1, return the second oldest token, etc. The
     *  token is not removed from the receiver. If there is no such
     *  token in the receiver (the offset is greater than or equal
     *  to the number of tokens currently in the receiver), throw an
     *  exception. If the offset is -1, return the most recent token
     *  removed from the receiver. If it is -2, return the second
     *  most recent token removed from the receiver, etc. If there is
     *  no such token in the receiver's history (the history capacity
     *  is zero or the absolute value of offset is greater than the
     *  number of tokens currently in the receiver's history), an
     *  exception is thrown.
     *  @param offset The offset from the oldest token in the receiver.
     *  @return The token at the desired offset in the receiver or its
     history.
     *  @exception NoTokenException If the offset is out of range.
     */
    public Token get(int offset) {
        try {
            return (Token)_queue.get(offset);
        } catch (NoSuchElementException ex) {
            throw new NoTokenException(getContainer(),
                    "Offset " + offset + " out of range with " + _queue.size()
                    + " tokens in the receiver and " + _queue.historySize()
                    + " in history.");
        }
    }
    
    /** Return the capacity, or INFINITE_CAPACITY if it is unbounded.
     *  @return The capacity of the receiver.
     */
    public int getCapacity() {
        return _queue.getCapacity();
    }
    
   
    /** Return true if put() will succeed in accepting a token.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     */
    public boolean hasRoom() {
        return !_queue.isFull();
    }
    
    /** Return true if put() will succeed in accepting the specified
     *  number of tokens.
     *  @param tokens The number of tokens.
     *  @return A boolean indicating whether a token can be put in this
     *   receiver.
     *  @exception IllegalActionException If the number of tokens is less
     *  than one.
     */
    public boolean hasRoom(int tokens) throws IllegalActionException {
	if (_queue.getCapacity() == INFINITE_CAPACITY) {
	    // queue has infinite capacity, so it can accept any
	    // finite number of tokens.
	    return true;
	}
	if(tokens < 1) 
	    throw new IllegalActionException("The number of " + 
					     "tokens must be greater than 0");
	return (_queue.size() + tokens) < _queue.getCapacity();
    }
    
    

    /** Determine the source and destination ports that use this
     *  receiver in their communications.  The source and destination
     *  ports are distinct for each receiver. 
     *  @param director The director that directs this receiver
     */
    public void determineEnds(Director director) {
        _toPort = this.getContainer();
    	_to = (Actor) _toPort.getContainer();
    	_fromPort = null;
        IOPort connectedPort = null;
        List listOfConnectedPorts = null;
        boolean isCompositeContainer = !((ComponentEntity) _to).isAtomic();
    	
    	_localDirector = director;
    	
    	if (isCompositeContainer && (_toPort.isOutput()) ) {
    	    listOfConnectedPorts = _toPort.insidePortList();
    	} else {
    	    listOfConnectedPorts = _toPort.connectedPortList();
    	}
    	    
    	Iterator portListIterator = listOfConnectedPorts.iterator();
    	
    	foundReceiver:
    	while (portListIterator.hasNext()) {
    	    connectedPort = (IOPort) portListIterator.next();
    	
    	    if (connectedPort.isOutput() == true) {
    		    Receiver[][] remoteReceivers = connectedPort.getRemoteReceivers();
    		    
    		    for(int i=0;i<connectedPort.getWidth();i++) {
    			    for(int j=0;j<remoteReceivers[i].length;j++) {
    			        if (remoteReceivers[i][j] == this) {
                            _from = (Actor) connectedPort.getContainer();
                            _fromPort = connectedPort;
                            if (_fromPort == null) {
                                throw new InternalErrorException(
                                    "internal GR error: Receiver with null source");
                            }
                            break foundReceiver;
    			        } 
    			    }
    		    }
    	    } else if (connectedPort.getContainer() instanceof TypedCompositeActor) {
    	        // FIXME: should use at isAtomic() insteadof instanceof?
    	        _from = (Actor) connectedPort.getContainer();
    	        _fromPort = connectedPort;
    	        if (_fromPort == null) {
    	            throw new InternalErrorException(
                        "internal GR error: Receiver with null source");
    	        }
    	        break foundReceiver;
    	    } else if (connectedPort.isInput() == true) {
    	       // This case occurs when the destination port and 
    	       // the queried connected port are both inputs.
    	       // This case should be ignored.
    	    } 
    	}
    	
    	if (_fromPort == null) {
    	    throw new InternalErrorException(
    	        "internal GR error: Receiver with null source");
    	}
    }
    

   
    
    /** Return the port that feeds this Receiver
     *  @return The port that feeds this receiver.
     */
    public TypedIOPort getSourcePort() {
        return (TypedIOPort) _fromPort;
    }
    

    /** Put a token to the receiver. If the port feeding this
     *  receiver is null, report an internal error.
     *  @param token The token to be put to the receiver.
     *  @exception InternalErrorException If the source port is null.
     */
    public void put(Token token) {
        if (_fromPort == null) {
            throw new InternalErrorException(
                      "internal GR error: Receiver with null source");
        } 
        if (!_queue.put(token)) {
            throw new NoRoomException(getContainer(),
                    "Queue is at capacity. Cannot put a token.");
        }
    }
    
    /** Return true if get() will succeed in returning a token.
     *  @return A boolean indicating whether there is a token in this
     *  receiver.
     */
    public boolean hasToken() {
        return !_queue.isEmpty();
    }
    
    /** Return true if get() will succeed in returning a token the given
     *  number of times.
     *  @return A boolean indicating whether there are the given number of
     *  tokens in this receiver.
     *  @exception IllegalActionException If the number of tokens is less
     *  than one.
     */
    public boolean hasToken(int tokens) throws IllegalActionException {
	if(tokens < 1) 
	    throw new IllegalActionException("The number of " + 
					     "tokens must be greater than 0");
        return _queue.size() >= tokens;
    }
    
    /** Enumerate the tokens stored in the history queue, which are
     *  the N most recent tokens taken from the receiver, beginning with
     *  the oldest, where N is less than or equal to the history capacity.
     *  If the history capacity is INFINITE_CAPACITY, then the enumeration
     *  includes all tokens previously taken from the receiver. If the
     *  history capacity is zero, then return an empty enumeration.
     *  @return An enumeration of tokens.
     */
    public Enumeration historyElements() {
        return _queue.historyElements();
    }

    /** Return the number of tokens in history.
     *  @return The number of tokens in history.
     */
    public int historySize() {
        return _queue.historySize();
    }
    
    /** Set receiver capacity. Use INFINITE_CAPACITY to indicate unbounded
     *  capacity (which is the default). If the number of tokens currently
     *  in the receiver exceeds the desired capacity, throw an exception.
     *  @param capacity The desired receiver capacity.
     *  @exception IllegalActionException If the receiver has more tokens
     *   than the proposed capacity or the proposed capacity is illegal.
     */
    public void setCapacity(int capacity) throws IllegalActionException {
        try {
            _queue.setCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex.getMessage());
        }
    }

    /** Set the capacity of the history queue. Use 0 to disable the
     *  history mechanism and INFINITE_CAPACITY to make the history
     *  capacity unbounded. If the size of the history queue exceeds
     *  the desired capacity, then remove the oldest tokens from the
     *  history queue until its size equals the proposed capacity.
     *  Note that this can be used to clear the history queue by
     *  supplying 0 as the argument.
     *  @param capacity The desired history capacity.
     *  @exception IllegalActionException If the desired capacity is illegal.
     */
    public void setHistoryCapacity(int capacity)
            throws IllegalActionException {
        try {
            _queue.setHistoryCapacity(capacity);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getContainer(), ex.getMessage());
        }
    }

    /** Return the number of tokens in the receiver.
     *  @return The number of tokens in the receiver.
     */
    public int size() {
        return _queue.size();
    }
    
 
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final int INFINITE_CAPACITY =
    QueueBuffer.INFINITE_CAPACITY;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private QueueBuffer _queue;
    private IOPort _container;
    private Token[] _tokenArray;
    ///////////////////////////////////////////////////////////////////
    ////                  package-access methods                   ////
   
    /**  For debugging purposes. Display pertinent information about
     *   this receiver.
     */
    void displayReceiverInfo() {
        String fromString;
        String toString;
        
        if (_from == null) {
              fromString="0";
        } else {
              fromString=((Nameable) _from).getName();
        }
        
        fromString += " (" + ((TypedIOPort)_fromPort).getType() + ")";
        
        if (_to == null) {
              toString="0";
        } else {
              toString=((Nameable) _to).getName();
        }
        
        toString += " (" + ((TypedIOPort)_toPort).getType() + ")";
        
        debug.println(fromString+" "+toString+" ");
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the DTReceiver.  Set the cached information regarding
     *  source and destination actors to null.  Set the local time to
     *  zero. 
     */
    private void _init() {
        _from = null;
        _to   = null;
        overrideHasToken = false;
        debug = new GRDebug(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                  package-access variables                 ////
    
    // override the value of hasToken() given by SDFReceiver
    // This variable is used in mixed-hierarchical DT
    boolean overrideHasToken;    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The director that directs this receiver; should be a DTDirector
    private Director _localDirector;
    
    // The actor feeding this receiver
    private Actor _from;
    
    // The actor containing this receiver
    private Actor _to;
    
    // The port feeding this receiver
    private IOPort _fromPort;
    
    // The port containing this receiver
    private IOPort _toPort;
    
    BranchGroup group;
    
    // display for debugging purposes
    private GRDebug debug;
    

}
