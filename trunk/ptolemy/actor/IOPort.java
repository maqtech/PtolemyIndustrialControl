/* A port supporting message passing.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
Review vectorized methods.
Review broadcast/get/send/hasRoom/hasToken.
Review setInput/setOutput/setMultiport.
Review isKnown/sendAbsent.
createReceivers creates inside receivers based solely on insideWidth, and 
   outsideReceivers based solely on outside width.  
*/

package ptolemy.actor;

import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// IOPort
/**
This class supports exchanging data between entities via message passing.
It can serve as an input port, an output port, or both. If it is an
input port, then it contains some number of receivers, which are
responsible for receiving data from remote entities. If it is an
output port, then it can send data to remote receivers.
<p>
Its receivers are created by a director.  It must therefore be
contained by an actor that has a director.  If it is not, then
any attempt to read data or list the receivers will trigger
an exception.
<p>
If this port is at the boundary of an opaque composite actor, then
then it can have both inside and outside links, with corresponding
inside and outside receivers. The inside links are to relations
inside the opaque composite actor, whereas the outside links are
to relations outside. If it is not specified, then a link is an
outside link.
<p>
The port has a <i>width</i>, which by default can be no greater
than one.  This width is the sum of the widths of the linked relations.
A port with a width greater than one behaves as a bus interface,
so if the width is <i>w</i>, then the port can simultaneously
handle <i>w</i> distinct input or output channels of data.
<p>
In general, an input port might have more than one receiver for
each channel.  This occurs particularly for transparent input ports,
which treat the receivers of the ports linked on the inside as its own.
But might also occur for opaque ports in some derived classes.
Each receiver in the group is sent the same data. Thus, an input port in
general will have <i>w</i> distinct groups of receivers, and can receive
<i>w</i> distinct channels.
<p>
By default, the maximum width of the port is one, so only one
channel is handled. A port that allows a width greater than one
is called a <i>multiport</i>. Calling setMultiport() with a
<i>true</i> argument converts the port to a multiport.
<p>
The width of the port is not set directly. It is the sum of the
widths of the relations that the port is linked to on the outside.
The sum of the widths of the relations linked on the inside can be
more or less than the width.  If it is more, then the excess inside relations
will be treated as if they are unconnected.  If it is less, then the
excess outside relations will be treated as if they are unconnected.
<p>
An IOPort can only link to instances of IORelation. Derived classes
may further constrain links to a subclass of IORelation.  To do this,
they should override the protected methods _checkLink() and _checkLiberalLink()
to throw an exception if their arguments are not of the appropriate
type.  Similarly, an IOPort can only be contained by a class
derived from ComponentEntity and implementing the Actor interface.
Subclasses may further constrain the containers by overriding
the protected method _checkContainer().

@authors Edward A. Lee, Jie Liu, Neil Smyth, Lukito Muliadi
@version $Id$
*/
public class IOPort extends ComponentPort {

    /** Construct an IOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public IOPort() {
        super();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public IOPort(Workspace workspace) {
	super(workspace);
    }

    /** Construct an IOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public IOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct an IOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public IOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
        setInput(isInput);
        setOutput(isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a token to all connected receivers.
     *  Tokens are in general immutable, so each receiver is given a
     *  reference to the same token and no clones are made.
     *  The transfer is accomplished by calling getRemoteReceivers()
     *  to determine the number of channels with valid receivers and
     *  then calling send on the appropriate channels.
     *  It would probably be faster to call put() directly on the receivers.
     *  If there are no destination receivers, then nothing is sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param token The token to send
     *  @exception IllegalActionException Not thrown in this base class.
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    public void broadcast(Token token)
	    throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            _workspace.getReadAccess();
            farReceivers = getRemoteReceivers();
            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }
        // NOTE: This does not call send() here, because send()
        // repeats the above on each call.
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] == null) continue;

            for (int j = 0; j < farReceivers[i].length; j++) {
                farReceivers[i][j].put(token);
            }
        }
    }

    /** Send the specified portion of a token array to all receivers connected
     *  to this port. The first <i>vectorLength</i> tokens
     *  of the token array are sent.
     *  <p>
     *  Tokens are in general immutable, so each receiver
     *  is given a reference to the same token and no clones are made.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The transfer is accomplished
     *  by calling the vectorized put() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            _workspace.getReadAccess();
            farReceivers = getRemoteReceivers();
            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }
        // NOTE: This does not call send() here, because send()
        // repeats the above on each call.
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] == null) continue;
                
            for (int j = 0; j < farReceivers[i].length; j++) {
                farReceivers[i][j].putArray(tokenArray, vectorLength);
            }
        }
    }

    /** Set all connected receivers to have no token.
     *  The transfer is accomplished by calling getRemoteReceivers()
     *  to determine the number of channels with valid receivers and
     *  then calling setAbsent on the appropriate receivers.
     *  If there are no destination receivers, then nothing is sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    public void broadcastAbsent()
	    throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            _workspace.getReadAccess();
            farReceivers = getRemoteReceivers();
            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }
        // NOTE: This does not call send() here, because send()
        // repeats the above on each call.
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] == null) continue;

            for (int j = 0; j < farReceivers[i].length; j++) {
                farReceivers[i][j].setAbsent();
            }
        }
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new IOPort.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        IOPort newObject = (IOPort)super.clone(workspace);
        newObject._insideInputVersion = -1;
        newObject._insideOutputVersion = -1;
        newObject._width = 0;
        newObject._widthVersion = -1;
        newObject._farReceivers = null;
        newObject._farReceiversVersion = -1;
        newObject._localReceivers = null;
        newObject._localReceiversVersion = -1;
        newObject._localInsideReceivers = null;
        newObject._localInsideReceiversVersion = -1;
        newObject._localReceiversTable = null;
        newObject._insideReceivers = null;
        newObject._insideReceiversVersion = -1;
        return newObject;
    }

    /** Create new receivers for this port, replacing any that may
     *  previously exist, and validate any instances of Settable that this
     *  port may contain. This method should only be
     *  called on opaque ports. It should also normally only be called
     *  during the preinitialize and prefire methods of the director.
     *  <p>
     *  If the port is an input port, receivers are created as necessary
     *  for each relation connecting to the port from the outside.
     *  If the port is an output port, receivers are created as necessary
     *  for each relation connected to the port from the inside. Note that
     *  only composite entities will have relations connecting to ports
     *  from the inside.
     *  <p>
     *  Note that it is perfectly allowable for a zero width output port to
     *  have insideReceivers.  This can be used to allow a model to be
     *  embedded in a container that does not connect the port to anything.
     *  <p>
     *  This method is <i>not</i> write-synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException If this port is not
     *   an opaque input port or if there is no director.
     */
    public void createReceivers() throws IllegalActionException {
        if (!isOpaque()) {
            throw new IllegalActionException(this,
                    "createReceivers: Can only create " +
                    "receivers on opaque ports.");
        }

        // Create the hashtable of lists of receivers in this port, keyed by
        // relation.  This replaces any previous table.
        _localReceiversTable = new HashMap();

        boolean input = isInput();
        boolean output = isOutput();

        if (input) {
            Iterator outsideRelations = linkedRelationList().iterator();
            while (outsideRelations.hasNext()) {
                IORelation relation = (IORelation) outsideRelations.next();
                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    int width = relation.getWidth();

                    Receiver[][] result = new Receiver[width][1];

                    for (int i = 0; i< width; i++) {
                        // This throws an exception if there is no director.
                        result[i][0] = _newReceiver();
                    }
                    // Save it.  If we have previously seen this relation,
                    // then we simply add the new array to the list
                    // of occurrences for this relation.  Otherwise,
                    // we create a new list with one element.
                    // EAL 7/30/99.
                    if (_localReceiversTable.containsKey(relation)) {
                        List occurrences =
                            (List)(_localReceiversTable.get(relation));
                        occurrences.add(result);
                    } else {
                        List occurrences = new LinkedList();
                        occurrences.add(result);
                        _localReceiversTable.put(relation, occurrences);
                    }
                }
            }
        }
        if (output) {
            Iterator insideRelations = insideRelationList().iterator();
            while (insideRelations.hasNext()) {
                IORelation relation = (IORelation)insideRelations.next();
                int width = relation.getWidth();

                Receiver[][] result = new Receiver[width][1];

                // Inside links need to have receivers compatible
                // with the local director.  We need to create those
                // receivers here.
                for (int i = 0; i< width; i++) {
                    // This throws an exception if there is no director.
                    result[i][0] = _newInsideReceiver();
                }
                // Save it.  If we have previously seen this relation,
                // then we simply add the new array to the list
                // of occurrences for this relation.  Otherwise,
                // we create a new list with one element.
                // EAL 7/30/99.
                if (_localReceiversTable.containsKey(relation)) {
                    List occurrences =
                        (List)(_localReceiversTable.get(relation));
                    occurrences.add(result);
                } else {
                    List occurrences = new LinkedList();
                    occurrences.add(result);
                    _localReceiversTable.put(relation, occurrences);
                }
            }
        }
    }

    /** Deeply enumerate the input ports connected to this port on the
     *  outside.  This method is deprecated and calls
     *  deepConnectedInPortList(). It is read-synchronized on the
     *  workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of input IOPort objects.
     */
    public Enumeration deepConnectedInPorts() {
        return Collections.enumeration( deepConnectedInPortList() );
    }

    /** Deeply enumerate the input ports connected to this port on the
     *  outside and return a list of these ports.  This method calls
     *  deepConnectedPortList() of the super class to get all the deeply
     *  connected ports and returns only the input ports among them.
     *  It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPortList
     *  @return A list of input IOPort objects.
     */
    public List deepConnectedInPortList() {
	try {
	    _workspace.getReadAccess();
	    LinkedList result = new LinkedList();

	    Iterator ports = deepConnectedPortList().iterator();
            while(ports.hasNext()) {
		IOPort port = (IOPort)ports.next();
		if (port.isInput()) {
		    result.addLast(port);
		}
	    }
	    return result;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** Deeply enumerate the output ports connected to this port on the
     *  outside.  This method is deprecated and calls
     *  deepConnectedOutPortList().
     *  It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of output IOPort objects.
     */
    public Enumeration deepConnectedOutPorts() {
        return Collections.enumeration( deepConnectedOutPortList() );
    }

    /** Deeply enumerate the output ports connected to this port on the
     *  outside and a return a list of these ports.  This method calls
     *  deepConnectedPortList() of the super *  class to get all the
     *  deeply connected ports and returns only the output ports among
     *  them. It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @return An enumeration of output IOPort objects.
     */
    public List deepConnectedOutPortList() {
	try {
	    _workspace.getReadAccess();
	    LinkedList result = new LinkedList();

	    Iterator ports = deepConnectedPortList().iterator();
	    while (ports.hasNext()) {
		IOPort port = (IOPort)ports.next();
		if (port.isOutput()) {
		    result.addLast(port);
		}
	    }
	    return result;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** If the port is an input, return the receivers deeply linked on the
     *  inside.  This method is used to obtain
     *  the receivers that are to receive data at this input port.
     *  The returned value is an array of
     *  arrays in the same format as that returned by getReceivers(). The
     *  difference between this method and getReceivers() is that this method
     *  treats the port as a transparent port regardless of whether it is
     *  one.  If there are no relations linked on the inside, it returns null.
     *  This method is used for opaque, non-atomic entities.  It "sees through"
     *  the boundary of opaque ports and actors.
     *  This method is <i>not</i> read-synchronized on the workspace, so the
     *  caller should be.
     *
     *  @return The inside receivers.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    public Receiver[][] deepGetReceivers() throws IllegalActionException {
        if (!isInput()) return _EMPTY_RECEIVER_ARRAY;
        int width = getWidth();
        if (width <= 0) return _EMPTY_RECEIVER_ARRAY;
        if (_insideReceiversVersion != _workspace.getVersion()) {
            // Cache is invalid.  Update it.
            _insideReceivers = new Receiver[width][0];
            int index = 0;
            Iterator insideRelations = insideRelationList().iterator();
            while (insideRelations.hasNext()) {
                IORelation relation = (IORelation) insideRelations.next();
                Receiver[][] deepReceiver = relation.deepReceivers(this);
                if (deepReceiver != null) {
                    int size = java.lang.Math.min(deepReceiver.length,
                            width-index);
                    for (int i = 0; i < size; i++) {
                        if (deepReceiver[i] != null) {
                            _insideReceivers[index++] = deepReceiver[i];
                        }
                    }
                }
            }
            _insideReceiversVersion = _workspace.getVersion();
        }
        return _insideReceivers;
    }

    /** Get a token from the specified channel.
     *  If the channel has a group with more than one receiver (something
     *  that is possible if this is a transparent port), then this method
     *  calls get() on all receivers, but returns only the first non-null
     *  token returned by these calls.
     *  Normally this method is not used on transparent ports.
     *  If there is no token to return, then throw an exception.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a get,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling get.
     *
     *  @param channelIndex The channel index.
     *  @return A token from the specified channel.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token get(int channelIndex)
            throws NoTokenException, IllegalActionException {
        Receiver[][] localReceivers;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getReceivers() method might throw an
                // IllegalActionException if there's no director.
                localReceivers = getReceivers();
                if (localReceivers[channelIndex] == null) {
                    throw new NoTokenException(this,
                            "get: no receiver at index: "
                            + channelIndex + ".");
                }
            } finally {
                _workspace.doneReading();
            }
            Token token = null;
            for (int j = 0; j < localReceivers[channelIndex].length; j++) {
                Token localToken = localReceivers[channelIndex][j].get();
                if (token == null) {
                    token = localToken;
                }
            }
            if (token == null) {
                throw new NoTokenException(this, "get: No token to return.");
            }
            return token;
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "get: channel index is out of range.");
        }
    }

    /** Get an array of tokens from the specified channel. The
     *  parameter <i>channelIndex</i> specifies the channel and
     *  the parameter <i>vectorLength</i> specifies the number of
     *  valid tokens to get in the returned array. The length of
     *  the returned array can be greater than the specified vector
     *  length, in which case, only the first <i>vectorLength</i>
     *  elements are guaranteed to be valid.
     *  <p>
     *  If the channel has a group with more than one receiver (something
     *  that is possible if this is a transparent port), then this method
     *  calls get() on all receivers, but returns only the first non-null
     *  token returned by these calls.
     *  Normally this method is not used on transparent ports.
     *  If there are not enough tokens to fill the array, then throw
     *  an exception.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a get,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling get.
     *
     *  @param channelIndex The channel index.
     *  @param vectorLength The number of valid tokens to get in the
     *   returned array.
     *  @return A token array from the specified channel containing
     *   <i>vectorLength</i> valid tokens.
     *  @exception NoTokenException If there is no array of tokens.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token[] get(int channelIndex, int vectorLength)
            throws NoTokenException, IllegalActionException {
        Receiver[][] localReceivers;
        try {
            _workspace.getReadAccess();
            // Note that the getReceivers() method might throw an
            // IllegalActionException if there's no director.
            localReceivers = getReceivers();
            
        } finally {
            _workspace.doneReading();
        }
        
        if(channelIndex >= localReceivers.length) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "get: channel index is out of range.");
        } 
        if (localReceivers[channelIndex] == null) {
            throw new NoTokenException(this,
                    "get: no receiver at index: "
                    + channelIndex + ".");
        }
        Token[] retArray =
            localReceivers[channelIndex][0].getArray(vectorLength);
        if (retArray == null) {
            throw new NoTokenException(this, "get: No token array " +
                    "to return.");
        }
        return retArray;
    }
    
    /** Return the current time associated with a certain channel.
     *  In most domains, this is just the current time of the director.
     *  However, in some domains, the current time is a per-channel
     *  concept.  If the channel has a token to be read (i.e. hasToken()
     *  returns true), then the current time is the time associated with
     *  that token.  If there is no token to be read, then the current
     *  time is the time of most recently read token. If no token has been
     *  previously read, then the current time is 0.0.  Notice that this
     *  means that an actor accessing time should do things in the
     *  following order:
     *  <pre>
     *     if (hasToken(n)) {
     *        double time = port.getCurrentTime(n);
     *        Token token = port.get(n);
     *     }
     *  </pre>
     *  I.e., getCurrentTime() is called before get().
     *  Currently, only the DT domain uses this per-channel time feature.
     */
    public double getCurrentTime(int channelIndex) 
                                     throws IllegalActionException {
        Receiver[][] localReceivers;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getReceivers() method might throw an
                // IllegalActionException if there's no director.
                localReceivers = getReceivers();
                if (localReceivers[channelIndex] == null) {
                    throw new IllegalActionException(this,
                            "no receiver at index: "
                            + channelIndex + ".");
                }
            } finally {
                _workspace.doneReading();
            }
            AbstractReceiver receiver = (AbstractReceiver) 
                                       localReceivers[channelIndex][0];
            return receiver.getCurrentTime();
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "getCurrentTime: channel index is out of range.");
        }
    }


    /** If the port is an opaque output port, return the receivers that
     *  receive data from all inside linked relations.
     *  This method is used for opaque, non-atomic entities, which have
     *  opaque ports with inside links.  Normally, those inside links
     *  are not visible.
     *  This method permits a director to transfer data across an opaque
     *  boundary by transferring it from the inside receivers to whatever
     *  receivers this might be connected to on the outside.
     *  The returned value is an an array of arrays in the same format as
     *  that returned by getReceivers().
     *  This method is read-synchronized on the workspace.
     *
     *  @return The local inside receivers.
     *  @exception IllegalActionException If there is no local director,
     *   and hence no receivers have been created.
     */
    public Receiver[][] getInsideReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOutput() || !isOpaque()) return _EMPTY_RECEIVER_ARRAY;

            // Check to see whether cache is valid.
            if (_localInsideReceiversVersion == _workspace.getVersion()) {
                return _localInsideReceivers;
            }

            // Have to compute the _inside_ width.
            int width = 0;
            Iterator relations = insideRelationList().iterator();
            while(relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();
                width += relation.getWidth();
            }

            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // Cache not valid.  Reconstruct it.
            _localInsideReceivers = new Receiver[width][0];
            int index = 0;
            relations = insideRelationList().iterator();

            // NOTE: Have to be careful here to keep track of the
            // occurrence number of the receiver.
            // EAL 7/30/00.
            HashMap seen = new HashMap();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                int occurrence = 0;
                if (seen.containsKey(relation)) {
                    // Have seen this relation before.  Increment
                    // the occurrence number.
                    occurrence = ((Integer)(seen.get(relation))).intValue();
                    occurrence++;
                }
                seen.put(relation, new Integer(occurrence));

                Receiver[][] receivers = getReceivers(relation, occurrence);
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        _localInsideReceivers[index++] = receivers[i];
                    }
                }
            }
            _localInsideReceiversVersion = _workspace.getVersion();
            return _localInsideReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an input, return the receivers that receive data
     *  from all linked relations. For an input
     *  port, the returned value is an array of arrays.  The first index
     *  (the group) specifies the group of receivers that receive from
     *  the same channel.  The second index (the column) specifies the
     *  receiver number within the group of receivers that get copies from
     *  the same channel.
     *  <p>
     *  For a transparent port (a port of a non-opaque entity), this method
     *  returns receivers in ports connected to this port on the inside.
     *  For an opaque port, the receivers returned are contained directly by
     *  this port.
     *  <p>
     *  The number of channels (groups) is the width of the port.
     *  <p>
     *  For each channel, there may be any number of receivers in the group.
     *  The individual receivers are selected using the second index of the
     *  returned array of arrays.  If there are no receivers in the group,
     *  then the channel is represented by null.  I.e., if the returned
     *  array of arrays is <i>x</i> and the channel number is <i>c</i>,
     *  then <i>x</i>[<i>c</i>] is null.  Otherwise, it is an array, where
     *  the size of the array is the number of receivers in the group.
     *  If the port is opaque, then the group size is one, so only
     *  <i>x</i>[<i>c</i>][0] is defined.  If the port is transparent,
     *  the group size is arbitrary.
     *  <p>
     *  For an opaque port, this method creates receivers by calling
     *  _newReceiver() if there are no receivers or the number of receivers
     *  does not match the width of the port.  In the latter case,
     *  previous receivers are lost, together with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.  If its cached
     *  list of local receivers is not valid, however, then it acquires
     *  write synchronization on the workspace to reconstruct it.
     *
     *  @return The local receivers.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    public Receiver[][] getReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isInput()) return _EMPTY_RECEIVER_ARRAY;

            if(isOpaque()) {
                // Check to see whether cache is valid.
                if (_localReceiversVersion == _workspace.getVersion()) {
                    return _localReceivers;
                }

                // Cache not valid.  Reconstruct it.
                int width = getWidth();
                if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

                _localReceivers = new Receiver[width][0];
                int index = 0;
                Iterator relations = linkedRelationList().iterator();
                // NOTE: Have to be careful here to keep track of the
                // occurrence number of the receiver.
                // EAL 7/30/00.
                HashMap seen = new HashMap();
                while (relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();
                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (relation != null) {
                        int occurrence = 0;
                        if (seen.containsKey(relation)) {
                            // Have seen this relation before.  Increment
                            // the occurrence number.
                            occurrence =
                                ((Integer)(seen.get(relation))).intValue();
                            occurrence++;
                        }
                        seen.put(relation, new Integer(occurrence));
                        Receiver[][] receiverRelation =
                            getReceivers(relation, occurrence);
                        if (receiverRelation != null) {
                            for (int i = 0; i < receiverRelation.length; i++) {
                                _localReceivers[index++] = receiverRelation[i];
                            }
                        }
		    }
                }
                _localReceiversVersion = _workspace.getVersion();
                return _localReceivers;
            } else {
                // Transparent port.
                return deepGetReceivers();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, this method only returns
     *  the relations correspond to the first occurrence.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation Relations that are linked on the outside or inside.
     *  @return The local receivers.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside, or if there is no director.
     */
    public Receiver[][] getReceivers(IORelation relation)
            throws IllegalActionException {
        return getReceivers(relation, 0);
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, the <i>occurrences</i>
     *  argument specifies which of the links we wish to examine.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation Relations that are linked on the outside or inside.
     *  @param occurrence The occurrence number that we are interested in,
     *   starting at 0.
     *  @return The local receivers.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside, or if there is no director.
     */
    public Receiver[][] getReceivers(IORelation relation, int occurrence)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            // Allow inside relations also to support opaque,
            // non-atomic entities.
            boolean insideLink = isInsideLinked(relation);
            if (!isLinked(relation) && !insideLink) {
                throw new IllegalActionException(this, relation,
                        "getReceivers: Relation argument is not " +
                        "linked to me.");
            }
            boolean opaque = isOpaque();
            if (!isInput() && !(opaque && insideLink && isOutput())) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = relation.getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            Receiver[][] result = null;
            // If the port is opaque, return the local Receivers for the
            // relation.
            if(opaque) {
                // If _localReceiversTable is null, then createReceivers()
                // hasn't been called, so there is nothing to return.
                if (_localReceiversTable == null) return _EMPTY_RECEIVER_ARRAY;

                if( _localReceiversTable.containsKey(relation) ) {
                    // Get the list of receivers for this relation.
                    List list = (List)_localReceiversTable.get(relation);
                    try {
                        result = (Receiver[][])(list.get(occurrence));
                    } catch (IndexOutOfBoundsException ex) {
                        return _EMPTY_RECEIVER_ARRAY;
                    }
                    if (result.length != width)  {
                        throw new InvalidStateException(this,
                                "getReceivers(IORelation, int): "
                                + "Invalid receivers. "
                                + "Need to call createReceivers().");
                    }
                }
                return result;
            } else {
                // If a transparent input port, ask its all inside receivers,
                // and trim the returned Receivers array to get the
                // part corresponding to this occurrence of the IORelation
                Receiver[][] insideReceivers = getReceivers();
                if(insideReceivers == null) {
                    return _EMPTY_RECEIVER_ARRAY;
                }
                int insideWidth = insideReceivers.length;
                int index = 0;
                result = new Receiver[width][];
                Iterator outsideRelations = linkedRelationList().iterator();
                int seen = 0;
                while(outsideRelations.hasNext()) {
                    IORelation outsideRelation =
                        (IORelation) outsideRelations.next();
                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (outsideRelation != null) {
                        if(outsideRelation == relation) {
                            if (seen == occurrence) {
                                // Have to be careful here to get the right
                                // occurrence of the relation.  EAL 7/30/00.
                                result = new Receiver[width][];
                                int receiverSize =
                                    java.lang.Math.min(width,
                                            insideWidth-index);
                                for (int i = 0; i< receiverSize; i++) {
                                    result[i] = insideReceivers[index++];
                                }
                                break;
                            } else {
                                seen++;
                                index += outsideRelation.getWidth();
                                if(index > insideWidth) break;
                            }
                        } else {
                            index += outsideRelation.getWidth();
                            if(index > insideWidth) break;
                        }
                    }
                }
                return result;
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an output, return the remote receivers that can
     *  receive from the port.  For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  The length
     *  of the array is the width of the port (the number of channels).
     *  It is an array of arrays, each of which represents a group of
     *  receivers that receive data from the same channel.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @return The receivers for output data.
     */
    public Receiver[][] getRemoteReceivers() {
        try {
            _workspace.getReadAccess();
            if (!isOutput()) return _EMPTY_RECEIVER_ARRAY;

            int width = getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // For opaque port, try the cached _farReceivers
            // Check validity of cached version
            if(isOpaque() &&
                    _farReceiversVersion == _workspace.getVersion()) {
                return _farReceivers;
            }
            // If not an opaque port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][0];
            Iterator relations = linkedRelationList().iterator();
            int index = 0;
            boolean foundRemoteInput = false;
            while(relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();
                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    Receiver[][] deepReceivers = relation.deepReceivers(this);
                    if (deepReceivers != null) {
                        for(int i = 0; i < deepReceivers.length; i++) {
                            farReceivers[index] = deepReceivers[i];
                            index++;
                            foundRemoteInput = true;
                        }
                    } else {
                        // create a number of null entries in farReceivers
                        // corresponding to the width of relation r
                        index += relation.getWidth();
                    }
                }
            }
            // No longer needed, davisj (3/29/99)
            /*
              if (!foundRemoteInput) {
              // No remote receivers
              farReceivers = null;
              }
            */
            // For an opaque port, cache the result.
            if(isOpaque()) {
                _farReceiversVersion = _workspace.getVersion();
                _farReceivers = farReceivers;
            }
            return farReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this port is an output, return the remote receivers that can
     *  receive data from this port through the specified relation.
     *  The relation should linked to the port
     *  from the inside, otherwise an exception is thrown. For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @return The receivers for output data.
     *  @exception IllegalActionException If the IORelation is not linked
     *       to the port from the inside.
     */
    public Receiver[][] getRemoteReceivers(IORelation relation)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isInsideLinked(relation)) {
                throw new IllegalActionException(this, relation,
                        "not linked from the inside.");
            }

            if (!isOutput()) return _EMPTY_RECEIVER_ARRAY;

            int width = relation.getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // no cache used.
            Receiver[][] outsideReceivers = getRemoteReceivers();
            if(outsideReceivers == null) {
                return _EMPTY_RECEIVER_ARRAY;
            }
            Receiver[][] result = new Receiver[width][];
            Iterator insideRelations = insideRelationList().iterator();
            int index = 0;
            while(insideRelations.hasNext()) {
                IORelation insideRelation =
                    (IORelation) insideRelations.next();
                if(insideRelation == relation) {
                    int size = java.lang.Math.min
                        (width, outsideReceivers.length-index);
                    //NOTE: if size = 0, the for loop is skipped.
                    for(int i = 0; i < size; i++) {
                        result[i] = outsideReceivers[i+index];
                    }
                    break;
                }
                index += insideRelation.getWidth();
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the width of the port.  The width is the sum of the
     *  widths of the relations that the port is linked to (on the outside).
     *  This method is read-synchronized on the workspace.
     *
     *  @return The width of the port.
     */
    public int getWidth() {
        try {
            _workspace.getReadAccess();
            long version = _workspace.getVersion();
            if(_widthVersion != version) {
                _widthVersion = version;
                int sum = 0;
                Iterator relations = linkedRelationList().iterator();
                while(relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();
                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (relation != null) {
                        sum += relation.getWidth();
                    }
                }
                _width = sum;
            }
            return _width;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the specified channel can accept a token via the
     *  put() method.  If this port is not an output, or the channel index
     *  is out of range, then throws IllegalActionException.  If there
     *  are multiple receivers in the group associated with the channel,
     *  then return true only if all the receivers can accept a token.
     *
     *  @param channelIndex The channel index.
     *  @return True if there is room for a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if this is not an output port, or if the channel index
     *   is out of range.
     */
    public boolean hasRoom(int channelIndex) throws IllegalActionException {
        try {
            Receiver[][] farReceivers = getRemoteReceivers();
            if (farReceivers == null || farReceivers[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                if (!farReceivers[channelIndex][j].hasRoom()) return false;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an output port.
            throw new IllegalActionException(this,
                    "hasRoom: channel index is out of range.");
        }
        return true;
    }

    /** Return true if the specified channel has a token to deliver
     *  via the get() method.  If this port is not an input, or if the
     *  channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean hasToken(int channelIndex) throws IllegalActionException {
        try {
            // The getReceivers() method throws an IllegalActionException if
            // there's no director.
            Receiver[][] receivers = getReceivers();
            if (receivers == null || receivers[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < receivers[channelIndex].length; j++) {
                if (receivers[channelIndex][j].hasToken()) return true;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "hasToken: channel index is out of range.");
        }
        return false;
    }

    /** Return true if the specified channel has the specified number
     *  of tokens to deliver via the get() method.
     *  If this port is not an input, or if the
     *  channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @param tokens The number of tokens to query the channel for.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean hasToken(int channelIndex, int tokens)
            throws IllegalActionException {
        try {
            // The getReceivers() method throws an IllegalActionException if
            // there's no director.
            Receiver[][] receivers = getReceivers();
            if (receivers == null || receivers[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < receivers[channelIndex].length; j++) {
                if (receivers[channelIndex][j].hasToken(tokens)) return true;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an output port.
            throw new IllegalActionException(this,
                    "hasToken: channel index is out of range.");
        }
        return false;
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param index The index at which to insert the link.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation.
     */
    public void insertLink(int index, Relation relation)
            throws IllegalActionException {
        super.insertLink(index, relation);
        _invalidate();
    }

    /** Return true if the port is an input.  The port is an input
     *  if either setInput() has been called with a <i>true</i> argument, or
     *  it is connected on the inside to an input port, or if it is
     *  connected on the inside to the inside of an output port.
     *  In other words, it is an input if data can be put directly into
     *  it or sent through it to an input.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the port is an input.
     */
    public boolean isInput() {
        if (_isInputOutputStatusSet) {
            return _isInput;
        }
        // Status has not been set.  Try to infer it.
        long version = _workspace.getVersion();
        if (_insideInputVersion != version) {
            try {
                _workspace.getReadAccess();
                // Check to see whether any port linked on the inside
                // is an input.
                _isInput = false;  // By default we are not an output port.
                Iterator ports = deepInsidePortList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    // Rule out case where this port itself is listed...
                    if (p != this && p.isInput()) _isInput = true;
                }
                _insideInputVersion = version;
            } finally {
                _workspace.doneReading();
            }
        }
        return _isInput;
    }

    /** Return true if all channels of this port have known state, that is, 
     *  the tokens on each channel are known or each channel is known not to 
     *  have any tokens.
     *  <p>
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @return True if it is known whether there is a token in each channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean isKnown() throws IllegalActionException {
        for (int j = 0; j < getWidth(); j++) {
            if (!isKnown(j)) return false;
        }
        return true;
    }

    /** Return true if the specified channel has known state, that is, the
     *  tokens on this channel are known or this channel is known not to 
     *  have any tokens.  If the channel index is out of range, then throw 
     *  an exception.
     *  <p>
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @return True if it is known whether there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean isKnown(int channelIndex) throws IllegalActionException {
        try {
            if (isInput()) {
                Receiver[][] receivers = getReceivers();
                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (receivers[channelIndex][j].isKnown()) return true;
                    }
                }
            }
            if (isOutput()) {
                Receiver[][] receivers = getRemoteReceivers();
                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (receivers[channelIndex][j].isKnown()) return true;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(this,
                    "isKnown: channel index is out of range.");
        }
        return false;
    }

    /** Return true if the port is a multiport.  The port is a multiport
     *  if setMultiport() has been called with a true argument.
     *
     *  @return True if the port is a multiport.
     */
    public boolean isMultiport() {
        // No need to synchronize this because the action is atomic
        // and synchronization would just ensure that no write action
        // is in progress.
        return _isMultiport;
    }

    /** Return true if the port is an output. The port is an output
     *  if either setOutput() has been called with a true argument, or
     *  it is connected on the inside to an output port, or it is
     *  connected on the inside to the inside of an input port.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the port is an output.
     */
    public boolean isOutput() {
        if (_isInputOutputStatusSet) {
            return _isOutput;
        }
        // Status has not been set.  Try to infer it.
        long version = _workspace.getVersion();
        if (_insideOutputVersion != version) {
            try {
                _workspace.getReadAccess();
                // Check to see whether any port linked on the
                // inside is an output.
                _isOutput = false;  // By default we are not an output port.
                Iterator ports = deepInsidePortList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    // Rule out case where this port itself is listed...
                    if (p != this && p.isOutput()) _isOutput = true;
                }
                _insideOutputVersion = version;
            } finally {
                _workspace.doneReading();
            }
        }
        return _isOutput;
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the relation does not share
     *   the same workspace, or the port has no container.
     */
    public void liberalLink(ComponentRelation relation)
            throws IllegalActionException {
        super.liberalLink(relation);
        _invalidate();
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the link crosses levels of
     *   the hierarchy, or the port has no container, or the relation
     *   is not a ComponentRelation.
     */
    public void link(ComponentRelation relation)
            throws IllegalActionException {
        super.link(relation);
        _invalidate();
    }

    /** Send the specified token to all receivers connected to the
     *  specified channel.  Tokens are in general immutable, so each receiver
     *  is given a reference to the same token and no clones are made.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The transfer is
     *  accomplished by calling the put() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void  send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();
                if (farReceivers == null ||
                        farReceivers[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                farReceivers[channelIndex][j].put(token);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Send the specified portion of a token array to all receivers connected
     *  to the specified channel. The first <i>vectorLength</i> tokens
     *  of the token array are sent.
     *  <p>
     *  Tokens are in general immutable, so each receiver
     *  is given a reference to the same token and no clones are made.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The transfer is accomplished
     *  by calling the vectorized put() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();
                if (farReceivers == null ||
                        farReceivers[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                farReceivers[channelIndex][j].
                    putArray(tokenArray, vectorLength);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Set all receivers connected to the specified channel to have no 
     *  tokens.  Receivers that do not support this action will do nothing.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The action is accomplished
     *  by calling the setAbsent() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing setAbsent,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void sendAbsent(int channelIndex)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();
                if (farReceivers == null ||
                        farReceivers[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                farReceivers[channelIndex][j].setAbsent();
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Override the base class to ensure that the proposed container
     *  implements the Actor interface (the base class ensures that the
     *  container is an instance of ComponentEntity) or null. A null
     *  argument will remove the port from the container.  This method
     *  invalidates the schedule and type resolution of the director
     *  of the container, if there is one.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        // Invalidate schedule and type resolution of the old container.
        Actor oldContainer = (Actor)getContainer();
        if (oldContainer != null) {
            Director director = oldContainer.getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }
        // Invalidate schedule and type resolution of the new container.
        if (container instanceof Actor) {
            Director director = ((Actor)container).getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }
        super.setContainer(container);
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  If this is never called, and setOutput() is never called,
     *  and the port is a transparent port of a composite actor,
     *  then the input/output status will be inferred from the connection.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *
     *  @param isInput True to make the port an input.
     */
    public void setInput(boolean isInput) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isInput = isInput;
        // Flag that the input status has been set,
        // and therefore should not be inferred.
        _isInputOutputStatusSet = true;
        _invalidate();
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port a multiport.
     *  That is, make it capable of linking with multiple IORelations,
     *  or with IORelations that have width greater than one.
     *  If the argument is false, allow only links with a single
     *  IORelation of width one.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace.
     *
     *  @param isMultiport True to make the port a multiport.
     */
    public void setMultiport(boolean isMultiport) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isMultiport = isMultiport;
        _invalidate();
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  If this is never called, and setInput() is never called,
     *  and the port is a transparent port of a composite actor,
     *  then the input/output status will be inferred from the connection.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *
     *  @param isOutput True to make the port an output.
     */
    public void setOutput(boolean isOutput) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isOutput = isOutput;
        // Flag that the output status has been set,
        // and therefore should not be inferred.
        _isInputOutputStatusSet = true;
        _invalidate();
        _workspace.doneWriting();
    }

    /** Transfer data from this port to the ports it is connected to
     *  on the inside.
     *  This port must be an opaque input port.  If any
     *  channel of the this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each input channel that has at least one token available.
     *
     *  @exception IllegalActionException If this port is not an opaque
     *   input port.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferInputs() throws IllegalActionException {
        if (!this.isInput() || !this.isOpaque()) {
            throw new IllegalActionException(this,
                    "transferInputs: this port is not an opaque" +
                    "input port.");
        }
        boolean wasTransferred = false;
        Receiver[][] insideReceivers = this.deepGetReceivers();
        for (int i = 0; i < this.getWidth(); i++) {
	    // NOTE: tokens on a channel are consumed only if the
	    // corresponding inside reciever is not null. This behavior
	    // should be OK for all of the current domains.
            if (insideReceivers != null && insideReceivers[i] != null) {
                try {
                    if (this.isKnown(i)) {
                        if (this.hasToken(i)) {
                            Token t = this.get(i);
                            if(_debugging) _debug(getName(),
                                    "transferring input from " 
                                    + this.getName());
                            for (int j = 0; j < insideReceivers[i].length; 
                                 j++) {
                                insideReceivers[i][j].put(t);
                            }
                            wasTransferred = true;
                        } else {
                            for (int j = 0; j < insideReceivers[i].length; 
                                 j++) {
                                insideReceivers[i][j].setAbsent();
                            }
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
        return wasTransferred;
    }

    /** Transfer data from this port to the ports it is connected to on
     *  the outside.
     *  This port must be an opaque output port.  If any
     *  channel of this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each output channel that has at least one token available.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @return True if at least one data token is transferred.
     */
    public boolean transferOutputs() throws IllegalActionException {
        if (!this.isOutput() || !this.isOpaque()) {
            throw new IllegalActionException(this,
                    "transferOutputs: this port is not " +
                    "an opaque output port.");
        }
        boolean wasTransferred = false;
        Receiver[][] insideReceivers = this.getInsideReceivers();
        if (insideReceivers != null) {
            for (int i = 0; i < insideReceivers.length; i++) {
                if (insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        try {
                            if (insideReceivers[i][j].isKnown()) {
                                if (insideReceivers[i][j].hasToken()) {
                                    Token t = insideReceivers[i][j].get();
                                    this.send(i, t);
                                    wasTransferred = true;
                                } else {
                                    this.sendAbsent(i);
                                }
                            }
                        } catch (NoTokenException ex) {
                            throw new InternalErrorException(this, ex, null);
                        }
                    }
                }
            }
        }
        return wasTransferred;
    }

    /** Unlink whatever relation is currently linked at the specified index
     *  number. If there is no such relation, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param index The index number of the link to remove.
     */
    public void unlink(int index) {
        // Override the base class to update _localReceiversTable.
        try {
            _workspace.getWriteAccess();
            Relation toDelete = (Relation)_relationsList.get(index);
            if (toDelete != null && _localReceiversTable != null) {
                _localReceiversTable.remove(toDelete);
            }
            super.unlink(index);
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation. The receivers associated with
     *  this relation, and any data they contain, are lost. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  This method is write-synchronized on the workspace.
     *
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            super.unlink(relation);
            if (_localReceiversTable != null) {
                _localReceiversTable.remove(relation);
            }
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations that are linked on the outside.
     *  This method is write-synchronized on the
     *  workspace.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            // NOTE: Can't just clear the _localReceiversTable because
            // that would unlink inside relations as well.
            if (_localReceiversTable != null) {
                // Have to clone the local receivers table to avoid
                // a ConcurrentModificationException.
                HashMap clonedMap = (HashMap)(_localReceiversTable.clone());
                Iterator relations = clonedMap.keySet().iterator();
                while (relations.hasNext()) {
                    Relation relation = (Relation)relations.next();
                    if (!isInsideLinked(relation)) {
                        _localReceiversTable.remove(relation);
                    }
                }
            }
            super.unlinkAll();
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations that are linked on the inside.
     *  This method is write-synchronized on the
     *  workspace.
     */
    public void unlinkAllInside() {
        try {
            _workspace.getWriteAccess();
            // NOTE: Can't just clear the _localReceiversTable because
            // that would unlink inside relations as well.
            if (_localReceiversTable != null) {
                // Have to clone the local receivers table to avoid
                // a ConcurrentModificationException.
                HashMap clonedMap = (HashMap)(_localReceiversTable.clone());
                Iterator relations = clonedMap.keySet().iterator();
                while (relations.hasNext()) {
                    Relation relation = (Relation)relations.next();
                    if (isInsideLinked(relation)) {
                        _localReceiversTable.remove(relation);
                    }
                }
            }
            super.unlinkAllInside();
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink whatever relation is currently linked on the inside
     *  with the specified index number. If the relation
     *  is not linked to this port on the inside, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param index The index number of the link to remove.
     */
    public void unlinkInside(int index) {
        // Override the base class to update _localReceiversTable.
        try {
            _workspace.getWriteAccess();
            Relation toDelete = (Relation)_insideLinks.get(index);
            if (toDelete != null) {
                if (_localReceiversTable != null) {
                    _localReceiversTable.remove(toDelete);
                }
            }
            super.unlinkInside(index);
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation on the inside. The receivers associated
     *  with this relation, and any data they contain, are lost. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  This method is write-synchronized on the workspace.
     *
     *  @param relation The relation to unlink.
     */
    public void unlinkInside(Relation relation) {
        try {
            _workspace.getWriteAccess();
            super.unlinkInside(relation);
            if (_localReceiversTable != null) {
                _localReceiversTable.remove(relation);
            }
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include information
     *  about whether the port is an input, output, or multiport, whether it
     *  is opaque, and what is its width.
     */
    public static final int CONFIGURATION = 512;

    /** Indicate that the description(int) method should include receivers
     *  contained by this port (if any).
     */
    public static final int RECEIVERS = 1024;

    /** Indicate that the description(int) method should include receivers
     *  remotely connected to this port (if any).
     */
    public static final int REMOTERECEIVERS = 2048;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Check that the specified container implements the Actor interface
     *  (or is null).
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof Actor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "IOPort can only be contained by objects implementing " +
                    "the Actor interface.");
        }
    }

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one.  This method
     *  allows level-crossing links.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to on the inside.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an IORelation, or the port already linked to a
     *   relation and is not a multiport, or the relation has width
     *   not exactly one and the port is not a multiport, or the
     *   relation is incompatible with this port, or the port is not
     *   in the same workspace as the relation.
     */
    protected void _checkLiberalLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " IOPort requires IORelation.");
        }
        _checkMultiportLink((IORelation) relation);
        super._checkLiberalLink(relation);
    }

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an IORelation, or the port already linked to a
     *   relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    protected void _checkLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " IOPort requires IORelation.");
        }
        _checkMultiportLink((IORelation) relation);
        super._checkLink(relation);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  CONFIGURATION, then append to the description a field containing
     *  any subset of the words "input", "output", "multiport", and "opaque",
     *  separated by spaces, plus a subfield of the form "{width
     *  <i>integer</i>}", where the integer is the width of the port.
     *  The field keyword is "configuration".
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  RECEIVERS, then append to the description a field containing
     *  the receivers contained by this port.  The keyword is "receivers"
     *  and the format is like the Receivers array, an array of groups, with
     *  each group receiving from a channel.
     *  Each group is a list of receiver descriptions (it may also be empty).
     *  If the detail argument sets the bit defined by the constant
     *  REMOTERECEIVERS, then also append to the description a field containing
     *  the remote receivers connected to this port.
     *
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & CONFIGURATION) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "configuration {";
                boolean space = false;
                if (isInput()) {
                    space = true;
                    result += "input";
                }
                if (isOutput()) {
                    if (space) result += " ";
                    space = true;
                    result += "output";
                }
                if (isMultiport()) {
                    if (space) result += " ";
                    space = true;
                    result += "multiport";
                }
                if (isOpaque()) {
                    if (space) result += " ";
                    space = true;
                    result += "opaque";
                }
                if (space) result += " ";
                result += "{width " + getWidth() + "}}";
            }
            if ((detail & RECEIVERS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "receivers {\n";
                try {
                    Receiver[][] receivers = getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        // One list item per group
                        result += _getIndentPrefix(indent+1) + "{\n";
                        for (int j = 0; j < receivers[i].length; j++) {
                            result += _getIndentPrefix(indent+2);
                            result += "{";
                            if (receivers[i][j] != null) {
                                result +=
                                    receivers[i][j].getClass().getName();
                            }
                            result += "}\n";
                        }
                        result += _getIndentPrefix(indent+1) + "}\n";
                    }
                } catch (IllegalActionException ex) {
                    result += _getIndentPrefix(indent+1) +
                        ex.getMessage() + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if ((detail & REMOTERECEIVERS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "remotereceivers {\n";
                Receiver[][] receivers = null;
                receivers = getRemoteReceivers();;
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        // One list item per group
                        result += _getIndentPrefix(indent+1) + "{\n";
                        if (receivers[i] != null) {
                            for (int j = 0; j< receivers[i].length; j++) {
                                result += _getIndentPrefix(indent+2);
                                result += "{";
                                if (receivers[i][j] != null) {
                                    result +=
                                        receivers[i][j].getClass().getName();
                                    result += " in ";
                                    result += receivers[i][j].
                                        getContainer().getFullName();
                                }
                                result += "}\n";
                            }
                        }
                        result += _getIndentPrefix(indent+1) + "}\n";
                    }
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class is the attributes plus possibly a special attribute
     *  to indicate whether the port is a multiport.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        if (_isInput) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"input\"/>\n");
        }
        if (_isOutput) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"output\"/>\n");
        }
        if (_isMultiport) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"multiport\"/>\n");
        }
        super._exportMoMLContents(output, depth);
    }

    /** Return the sums of the widths of the relations linked on the inside,
     *  except the specified port.  If any of these relations has not had
     *  its width specified, throw an exception.  This is used by IORelation
     *  to infer the width of a bus with unspecified width and to determine
     *  whether more than one relation with unspecified width is linked on the
     *  inside, and by the liberalLink() method to check validity of the link.
     *  If the argument is null, all relations linked on the inside are checked.
     *  This method is not read-synchronized on the workspace, so the caller
     *  should be.
     *
     *  @param except The relation to exclude.
     */
    protected int _getInsideWidth(IORelation except) {
        int result = 0;
        Iterator relations = insideRelationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation)relations.next();
            if (relation != except) {
                if (!relation.isWidthFixed()) {
                    throw new InvalidStateException(this,
                            "Width of inside relations cannot "
                            + "be determined.");
                }
                result += relation.getWidth();
            }
        }
        return result;
    }

    /** Create a new receiver compatible with the local director.
     *  This is done by asking the local director of the container for
     *  a new receiver, and then setting its
     *  container to this port.  This allows actors to work across
     *  several domains, since often the only domain-specific part of
     *  of an actor is its receivers.  Derived classes may choose to
     *  handle this directly, creating whatever specific type of receiver
     *  they want. This method is not read-synchronized
     *  on the workspace, so the caller should be.
     *
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no local director).
     */
    protected Receiver _newInsideReceiver() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            CompositeActor castContainer = (CompositeActor)container;
            if (castContainer.isOpaque() && !castContainer.isAtomic()) {
                Receiver receiver = castContainer.newInsideReceiver();
                receiver.setContainer(this);
                return receiver;
            }
        }
        throw new IllegalActionException(this,
                "Can only create inside receivers for a port of a non-atomic, "
                + "opaque entity.");
    }

    /** Create a new receiver compatible with the executive director.
     *  This is done by asking the
     *  containing actor for a new receiver, and then setting its
     *  container to this port.  This allows actors to work across
     *  several domains, since often the only domain-specific part of
     *  of an actor is its receivers.  Derived classes may choose to
     *  handle this directly, creating whatever specific type of receiver
     *  they want.  This method is not write-synchronized
     *  on the workspace, so the caller should be.
     *
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no executive director).
     */
    protected Receiver _newReceiver() throws IllegalActionException {
        Actor container = (Actor)getContainer();
        if (container == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a container.");
        }
        Receiver receiver = container.newReceiver();
        receiver.setContainer(this);
        return receiver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check that a port that is not a multiport will not have too many
     *  links if a link is established with the specified relation.
     *  @exception IllegalActionException If the port will have too many
     *  links.
     */
    private void _checkMultiportLink(IORelation relation)
            throws IllegalActionException {
        if (_isInsideLinkable(relation.getContainer())) {
            // An inside link
            if(!isInsideLinked(relation)) {
                // Check for existing inside links
                if(!isMultiport() && numInsideLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation " +
                            "to a single port.");
                }
                if ((relation.getWidth() != 1) || !relation.isWidthFixed()) {
                    // Relation is a bus.
                    if(!isMultiport()) {
                        throw new IllegalActionException(this,  relation,
                                "Attempt to link a bus relation " +
                                "to a single port.");
                    }
                    if (!relation.isWidthFixed()) {
                        // Make sure there are no other busses already
                        // connected with unspecified widths.
                        try {
                            _getInsideWidth(null);
                        } catch (InvalidStateException ex) {
                            throw new IllegalActionException(this, relation,
                                    "Attempt to link a second bus relation " +
                                    "with unspecified width to the inside " +
                                    "of a port.");
                        }
                    }
                }
            }
        } else {
            // An outside link
            if(!isLinked(relation)) {
                // Check for existing outside links
                if(!isMultiport() && numLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation " +
                            "to a single port.");
                }
                if (relation.getWidth() != 1 || !relation.isWidthFixed()) {
                    // Relation is a bus.
                    if(!isMultiport()) {
                        throw new IllegalActionException(this,  relation,
                                "Attempt to link a bus relation " +
                                "to a single port.");
                    }
                    Iterator relations = linkedRelationList().iterator();
                    while (relations.hasNext()) {
                        IORelation theRelation = (IORelation)relations.next();
                        // A null link (supported since indexed links) might
                        // yield a null relation here. EAL 7/19/00.
                        if (theRelation != null &&
                                !theRelation.isWidthFixed()) {
                            throw new IllegalActionException(this, relation,
                                    "Attempt to link a second bus relation " +
                                    "with unspecified width to the outside " +
                                    "of a port.");
                        }
                    }
                }
            }
        }
    }

    // Invalidate schedule and type resolution of the director of the
    // container, if there is one.
    private void _invalidate() {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Director director = ((Actor)container).getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // To avoid creating this repeatedly, we use a single version.
    private static final Receiver[][]
    _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

    // Indicate whether the port is an input, an output, or both.
    // The value may be overridden in transparent ports, in that if
    // a transparent port is inside linked to an input or output port,
    // then it will be considered an inside or output port respectively.
    // This determination is cached, so we need variables to track the
    // validity of the cache.
    // 'transient' means that the variable will not be serialized.
    private boolean _isInput, _isOutput;
    private transient long _insideInputVersion = -1;
    private transient long _insideOutputVersion = -1;

    // Flag that the input/output status has been set.
    private boolean _isInputOutputStatusSet = false;

    // Indicate whether the port is a multiport. Default false.
    private boolean _isMultiport = false;

    // The cached width of the port, which is the sum of the widths of the
    // linked relations.  The default 0 because initially there are no
    // linked relations.  It is set or updated when getWidth() is called.
    // 'transient' means that the variable will not be serialized.
    private transient int _width = 0;
    // The workspace version number on the last update of the _width.
    // 'transient' means that the variable will not be serialized.
    private transient long _widthVersion = -1;

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;
    private transient long _farReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localReceivers;
    private transient long _localReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localInsideReceivers;
    private transient long _localInsideReceiversVersion = -1;

    // A cache of the inside Receivers, and the version.
    private transient Receiver[][] _insideReceivers;
    private transient long _insideReceiversVersion = -1;

    // Lists of local receivers, indexed by relation.
    private HashMap _localReceiversTable;
}
