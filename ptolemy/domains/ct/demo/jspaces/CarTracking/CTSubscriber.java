/* A subscriber class that generates events in CT domain.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.jspaces.CarTracking;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.Source;
import ptolemy.actor.lib.jspaces.TokenEntry;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;
import ptolemy.domains.ct.kernel.CTEventGenerator;

import net.jini.space.JavaSpace;
import net.jini.core.lease.Lease;
import net.jini.core.event.*;
import net.jini.core.transaction.TransactionException;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Subscriber
/**
A subscriber to the JavaSpaces. The subscriber token is available at
the iteration of execution. If there are more than one notification
occurs in one iteration, only the latest notification is presented
in the next iteration.

@author Jie Liu
@version $Id$
*/

public class CTSubscriber extends Source
    implements RemoteEventListener, CTEventGenerator {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CTSubscriber(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	jspaceName = new Parameter(this, "jspaceName", 
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);

        entryName = new Parameter(this, "entryName", 
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of 
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the subcribed entry. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Find the JavaSpaces and retrieve the first token. The type of
     *  the output is infered from the type of the token
     *  @exception IllegalActionException If the space cannot be found.
     */
    public void preinitialize() throws IllegalActionException {
        _entryName = ((StringToken)entryName.getToken()).stringValue();
        _space = SpaceFinder.getSpace(
                ((StringToken)jspaceName.getToken()).stringValue());

        // export this object so that the space can call back
        try {
            UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e) {
            //throw new IllegalActionException( this,
            //        "unable to export object. Please check if RMI is OK. " +
            //        e.getMessage());
            System.err.println("Warning: " + e.getMessage());
        }
                
        // read the current data in the JavaSpaces, and use
        // it as the initial condition
        TokenEntry entryTemplate = new TokenEntry(_entryName, 
                null, null);
        // request for notification
        try {
            _eventReg = _space.notify(
                    entryTemplate, null, this, Lease.FOREVER, null);
            _notificationSeq = _eventReg.getSequenceNumber();
        } catch (RemoteException re) {
            throw new IllegalActionException(this,
                    "failed registering for notification. " + re.getMessage());
        } catch (TransactionException te) {
            throw new IllegalActionException(this,
                    "failed registering for notification. " + te.getMessage());
        }
    }

    /** Emit the notified event. If there
     *  is no such events, do nothing.
     */
    public void emitCurrentEvents() throws IllegalActionException {
        if(_hasNewToken) {
            synchronized(_lock) {
                output.send(0, _notifiedToken);
                _hasNewToken = false;
            }
        }
    }
        
    /** Return true if there is an event notified.
     *  @return True if there is a new event notification 
     *  in the last iteratoin.
     */
    public boolean hasCurrentEvent() {
        return _hasNewToken;
    }

    /** Fork a new thread to handle the notify event.
     */
    public void notify(RemoteEvent event) {
        NotifyHandler nh = new NotifyHandler(this, event);
        new Thread(nh).start();
    }    

    /** If the token is not emitted in the last iteration, just forget it.
     */
    public boolean prefire() throws IllegalActionException {
        _hasNewToken = false;
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The entry name.
    private String _entryName;
    
    // The space to read from.
    private JavaSpace _space;
    
    // Indicating whether there's new data came in.
    private boolean _hasNewToken = false;

    // The lock that the access of local variables are synchronized on.
    private Object _lock = new Object();

    // Current set of data.
    private Token _notifiedToken;

    // Used to identify the event registration
    private EventRegistration _eventReg;
    
    // Used to identify notification.
    private long _notificationSeq;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    
    public class NotifyHandler implements Runnable {
        
        /** construct the notify handler
         */
        public NotifyHandler(TypedAtomicActor container, RemoteEvent event) {
            _container = container;
            _event = event;
        }

        //////////////////////////////////////////////////////////////
        ////                     public methods                   ////
                
        /** Read the entry token from the javaspaces.
         */
        public void run() {
            // check if it is the right notification
            if (_event.getSource().equals(_eventReg.getSource()) &&
                    _event.getID() == _eventReg.getID() && 
                    _event.getSequenceNumber() > _notificationSeq) {
                // grab a lock and read all new entries.
                synchronized(_lock) {
                    TokenEntry entryTemplate = new TokenEntry(_entryName, 
                            null, null);
                    TokenEntry entry;
                    try {
                        entry = (TokenEntry)_space.readIfExists(
                                entryTemplate, null, 100);
                    } catch (Exception e) {
                        throw new InvalidStateException(_container,
                                "error reading from space." +
                                e.getMessage());
                    }
                    if(entry == null) {
                        System.out.println(getName() + 
                                " read null from space");
                    } else {
                        _notifiedToken = (Token)entry.token;
                        _hasNewToken = true;
                    }
                }
            }
        }
        
        //////////////////////////////////////////////////////////////
        ////                     private variables                ////
        
        // the container
        private TypedAtomicActor _container;
        
        // the event
        private RemoteEvent _event;
    }
}
