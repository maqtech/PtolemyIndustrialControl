/* A subscriber actor that read entries from Java Spaces.

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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces;

import ptolemy.actor.lib.Source;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;
import net.jini.space.JavaSpace;
import net.jini.core.lease.Lease;
import net.jini.core.event.*;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Subscriber
/**
A subscriber that read TokenEnties from a Java Space.
The Java Space is identified by the <i>jspaceName</i> parameter.
Upon preinitialization, this actor register a template of TokenEntries
with the name given by the <i>entryName<i> parameter. The Java Space
will notify the actor if there are new TokenEntries with that name.
When get notified, this actor starts another thread which reads the
TokenEntry. In the execution thread, when the actor's fire method
is called, the actor outputs the last token read from the Java Space.
That is if there are more than one notified entries, the old entry
will be overridden by the new one. If there are no notifications
between to successive fires, the behavior of the actor depends on
the <i>blocking</i> parameter. If this parameter is true, then
the fire() call will block the execution thread, until there is
a new entry coming. If this parameter is false, then the fire()
method outputs the <i>defaultToken</i>. The type of the output
port is also determined by the type of this parameter.

@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class Subscriber extends Source implements RemoteEventListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Subscriber(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	jspaceName = new Parameter(this, "jspaceName",
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);
        entryName = new Parameter(this, "entryName",
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        blocking = new Parameter(this, "blocking",
                new BooleanToken(false));
        blocking.setTypeEquals(BaseType.BOOLEAN);
        defaultToken = new Parameter(this, "defaultToken",
                new DoubleToken(0.0));
        defaultToken.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the subscribed entry. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** Indicate whether the actor blocks when it can not read
     *  an entry from the space. The default value is false of
     *  type BooleanToken.
     */
    public Parameter blocking;

    /** The default token. If the actor is nonblocking
     *  and there is no matching entry in the space, then this
     *  token will be output when the fire() method is called.
     *  The default value is 0.0 of type
     *  DoubleToken. Notice that the type of the output port
     *  is determined by the first token read from the Java Space.
     *  The user should make sure that the type of this token
     *  is compatible with the type of the output port.
     */
    public Parameter defaultToken;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>blocking</i> update the local
     *  cache of the parameter value, otherwise pass the call to
     *  the super class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blocking) {
            _blocking = ((BooleanToken)blocking.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Find the JavaSpaces and register for notification. Set the type of
     *  the output to be the type of the defaultToken.
     *  @exception IllegalActionException If the space cannot be found.
     */
    public void preinitialize() throws IllegalActionException {
        _entryName = ((StringToken)entryName.getToken()).stringValue();

        _lookupThread = Thread.currentThread();

        _space = SpaceFinder.getSpace(
                ((StringToken)jspaceName.getToken()).stringValue());

        _lookupThread = null;

        // export this object so that the space can call back
        try {
            UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e) {
            System.err.println("Warning: " + e.getMessage());
        }
        //FIXME: Consider set type by the token read.
        output.setTypeEquals(defaultToken.getToken().getType());

        TokenEntry template = new TokenEntry(_entryName, null, null);

        // Register for notification
        try {
            _eventReg = _space.notify(
                    template, null, this, Lease.FOREVER, null);
            _notificationSeq = _eventReg.getSequenceNumber();

        } catch (Exception e) {
            throw new IllegalActionException( this,
                    "Error register for notification from the JavaSpace." +
                    e.getMessage());
        }
    }

    /** Star a new thread to read the TokenEntry from the Java Space.
     *  @param event The notification event sent from the Java Space.
     */
    public void notify(RemoteEvent event) {
        if (_debugging) {
            _debug(getName(), "Get notified from JavaSpaces.");
        }
        NotifyHandler nh = new NotifyHandler(this, event);
        new Thread(nh).start();
    }

    /** Output the latest received token from last time the fire()
     *  method is called.
     *  If there's no token available, then the behavior depends
     *  on the "blocking" parameter. If blocking is true, the
     *  execution blocks until there's a token coming in.
     *  Otherwise, the defaultToken is produced.
     */
    public void fire() throws IllegalActionException {
        synchronized(_lock) {
            while(true) {
                if (_lastReadToken == null) {
                    if(_blocking) {
                        try {
                            if (_debugging) {
                                _debug(getName(), " is waiting.");
                            }
                            _lock.wait();
                            if (_debugging) {
                                _debug(getName(), " wakes up.");
                            }
                        } catch (InterruptedException e) {
                            throw new IllegalActionException(this,
                                    "blocking interrupted." +
                                    e.getMessage());
                        }
                    } else {
                        output.send(0, defaultToken.getToken());
                        break;
                    }
                } else {
                    if (_debugging) {
                        _debug(getName(), " outputs a token.");
                    }
                    output.send(0, _lastReadToken);
                    _lastReadToken = null;
                    return;
                }
            }
        }
    }

    /** Kill the lookup thread if it is not returned.
     */
    public void stopFire() {
        if (_lookupThread != null) {
            _lookupThread.interrupt();
        }
        super.stopFire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The current serial number
    private String _entryName;

    // The space to read from.
    private JavaSpace _space;

    // cached value of whether blocks when no data
    private boolean _blocking;

    // The list of tokens that received.
    private Object _lock = new Object();

    // The indicator the last read serial number
    private Token _lastReadToken;

    // Used to identify the event registration
    private EventRegistration _eventReg;

    // Notification sequence number
    private long _notificationSeq;

    // The thread that finds jini.
    private Thread _lookupThread;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** The inner class that retrieve token entries from the Java Spaces.
     *  This class is runnable, and when there is a notification
     *  from the Java Space, a thread will be created associated
     *  with this class. The run() method will then read the TokenEntry
     *  from the Java Space.
     */
    public class NotifyHandler implements Runnable {

        /** construct the notify handler.
         *  @param container The subscriber that contains this handler.
         *  @param event The notification event.
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
                    // make sure the actor is not producing outputs.
                    TokenEntry entryTemplate = new TokenEntry(_entryName,
                            null, null);
                    TokenEntry entry;
                    try{
                        entry = (TokenEntry)_space.readIfExists(
                                entryTemplate, null, Long.MAX_VALUE);
                    } catch (Exception e) {
                        throw new InvalidStateException(_container,
                                "error reading space." +
                                e.getMessage());
                    }
                    if(entry == null) {
                        System.out.println("Warning: " + getName() +
                                " read null from space");
                    } else {
                        //System.out.println(getName() +
                        //        " reads successfully.");

                        _lastReadToken = entry.token;
                    }
                    _lock.notifyAll();
                }
            }
            _notificationSeq = _event.getSequenceNumber();
        }

        //////////////////////////////////////////////////////////////
        ////                     private variables                ////

        // The container.
        private TypedAtomicActor _container;

        // The notification event.
        private RemoteEvent _event;
    }
}
