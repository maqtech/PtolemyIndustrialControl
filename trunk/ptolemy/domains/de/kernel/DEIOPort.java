/* A port for use by actors specialized to the DE domain.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

//////////////////////////////////////////////////////////////////////////
//// DEIOPort
/**
This port can be used by actors that are specialized to the discrete-event
(DE) domain. It supports annotations that inform the scheduler about delays
and about priorities for handling simultaneous inputs. It also provides
two additional methods, overloaded versions of broadcast() and send().
The overloaded versions have a second argument for the time delay,
allowing actors to send output data with a time delay (relative to current
time).
<p>
Actors in the DE domain are not required to use this port. If they use
the base class, TypedIOPort, then the data they send is sent with zero delay.

@authors Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class DEIOPort extends TypedIOPort {

    /** Construct a DEIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public DEIOPort() {
        super();
    }

    /** Construct a DEIOPort with the specified container and name
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
    public DEIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct a DEIOPort with the specified container and name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isinput True if this is to be an input port.
     *  @param isoutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public DEIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isinput, isoutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast a token to all receivers connected to this output
     *  port with the specified time delay.  The time stamp of
     *  of the token is equal to current time plus the specified delay.
     *  If the specified delay is zero, then the event is queued to be
     *  processed in the next microstep.
     *
     *  @param token The token to send.
     *  @param delay The time stamp of the token being broadcast.
     *  @exception IllegalActionException If the port is not an output.
     */
    public void broadcast(Token token, double delay)
            throws IllegalActionException {
        _delay = delay;
        _useDelay = true;
        broadcast(token);
    }

    /** Add the specified port to the set of output ports that
     *  have delayed events triggered by this input port.
     *  @param output The output port with delayed events.
     *  @exception IllegalActionException If this port is not an input,
     *   or if the argument is not an output port.
     */
    public void delayTo(IOPort output) throws IllegalActionException {
        if (!isInput() || !output.isOutput()) {
            throw new IllegalActionException(this,
                    "Invalid delayTo relationship.  " +
                    "Must be input.delayTo(output).");
        }
        _delayToSet.add(output);
    }

    /** Return the set of ports that have delayed events triggered
     *  by this one (as opposed to instantaneous events).
     */
    public Set getDelayToPorts() {
        return _delayToSet;
    }

    /** Override the base class to use the delay that may have been set
     *  by calling the overloaded version of send() that takes a delay
     *  argument, or the similarly overloaded version of broadcast().
     *  After setting the delay in all the receivers, the base class
     *  send() method is invoked.  If the channel index is out of range,
     *  then the token is not sent anywhere.
     *
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception IllegalActionException If the port is not an output,
     *   or if the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     *   This should not occur in the DE domain.
     */
    public void send(int channelindex, Token token)
            throws IllegalActionException, NoRoomException {
        if (_useDelay) {
            _useDelay = false;
            try {
                workspace().getReadAccess();
                Receiver[][] fr = getRemoteReceivers();
                if (fr == null) return;
                if (fr[channelindex] == null) return;
                for (int j = 0; j < fr[channelindex].length; j++) {
                    try {
                        ((DEReceiver)fr[channelindex][j]).setDelay(_delay);
                    } catch (ClassCastException e) {
                        throw new InvalidStateException("DEIOPort.send() " +
                        "expects to connect to receivers of type DEReceiver.");
                    }
                }
                super.send(channelindex, token);
            } catch (ArrayIndexOutOfBoundsException ex) {
                // Ignore... send token nowhere.
            } finally {
                workspace().doneReading();
            }
        } else {
            super.send(channelindex, token);
        }
    }

    /** Send a token with the specified time delay to the receivers connected
     *  on the specified channel.  The time stamp of
     *  of the token is equal to current time plus the specified delay.
     *  If the specified delay is zero, then the event is queued to be
     *  processed in the next microstep.
     *
     *  @param channelindex The index of the channel, from 0 to width-1.
     *  @param token The token to send.
     *  @param delay The time delay of the token being sent.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range.
     */
    public void send(int channelindex, Token token, double delay)
            throws IllegalActionException {
        _delay = delay;
        _useDelay = true;
        send(channelindex, token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // List of ports triggered immediately by this input port.
    private Set _delayToSet = new HashSet();

    // The delay to use in transfering tokens.
    // Be careful to set this back to zero after using it.
    private double _delay = 0.0;

    // A flag indicating that there is delay in the next output.
    private boolean _useDelay = false;
}
