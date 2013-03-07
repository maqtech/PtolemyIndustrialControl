/* This actor implements a quantity manager which manages all end-systems
   for an AFDX network.

@Copyright (c) 2010-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.qm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is an {@link QuantityManager} that simulates an AFDX End-systems component
 *  When its {@link #sendToken(Receiver, Receiver, Token)} method is called,
 *  the delivery of the specified token to the specified receiver is delayed according
 *  to the AFDX end-system protocol and behavior. Only one actor of this kind is required
 *  to manage all end-systems of one AFDX network.
 *  <p>
 *  For more information please refer to:
 *      <i>AFDX network simulation in PtolemyII</i>.
 *      <i>AFDX standard document</i>.
 *
 *  @author Gilles Lasnier
 *  @version $Id$
 *  @since Ptolemy II 0.2
 */
public class AFDXESs extends MonitoredQuantityManager {

    /** Construct a AFDXESs with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AFDXESs(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        bitRate = new Parameter(this, "bitRate");
        bitRate.setDisplayName("bitRate (Mbit/s)");
        bitRate.setExpression("100");
        bitRate.setTypeEquals(BaseType.DOUBLE);
        _bitRate = 100;

        _tokenCount = 0;

        _virtualLinkTable = new HashMap<String, AFDXVlink>();
        _lastEmissionTable = new HashMap<String, Time>();
        _afdxVLinksQueue = new HashMap<String, LinkedList<TimedEvent>>();
        _afdxSchedMuxsQueue = new HashMap<String, LinkedList<TimedEvent>>();

        // icon description
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /**
     * The bit rate of the bus. This is a double with default value to 100
     * Mbits/s. It is required to be positive.
     */
    public Parameter bitRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>bitRate</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == bitRate) {
            double value = ((DoubleToken) bitRate.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative bitRate: " + value);
            }
            _bitRate = value;
        }
        super.attributeChanged(attribute);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new AFDXESs.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AFDXESs newObject = (AFDXESs) super.clone(workspace);

        newObject._afdxVLinksQueue = new HashMap();
        newObject._afdxSchedMuxsQueue = new HashMap();
        newObject._virtualLinkTable = new HashMap();
        newObject._lastEmissionTable = new HashMap();
        newObject._nextFireTime = _nextFireTime;
        newObject._bitRate = _bitRate;

        return newObject;
    }

    /** Fire the actor.
     *  Typically, the fire() method performs the computation associated
     *  with an actor.
     *  Here, it delivers (if required) the intended token to the intended receiver(s).
     *
     *  @exception IllegalActionException If firing is not permitted.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time computedTimeStamp = null;
        boolean multicast = false;

        // Send events from virtual links to the corresponding scheduler multiplexor
        for (Entry<String, LinkedList<TimedEvent>> entry : _afdxVLinksQueue.entrySet()) {

            if (entry.getValue().size() > 0) {
                TimedEvent e = entry.getValue().getFirst();

                if (e.timeStamp.compareTo(currentTime) == 0) {
                    Time lastTimeStamp = currentTime;
                    AFDXVlink vl = _virtualLinkTable.get(entry.getKey());

                    // lastTimeStamp and multicast (factorization)
                    if (_afdxSchedMuxsQueue.get(vl.getSchedulerMux()).size() > 0) {
                        Object[] last = (Object[]) _afdxSchedMuxsQueue
                                .get(vl.getSchedulerMux()).getLast().contents;
                        Object[] output = (Object[]) e.contents;

                        if ( ((AFDXVlink) output[2]).getSource()
                                .equals(((AFDXVlink) last[2]).getSource())
                                && ((Time) output[3]).compareTo((Time) last[3]) == 0) {
                            multicast = true;
                        }

                        lastTimeStamp = _afdxSchedMuxsQueue
                                .get(vl.getSchedulerMux()).getLast().timeStamp;
                    }

                    if (multicast) {
                        computedTimeStamp = lastTimeStamp;
                        multicast = false;
                    } else {
                        computedTimeStamp = lastTimeStamp
                                .add(vl.getTrameSize() / (_bitRate*1000000));
                    }
                    _afdxSchedMuxsQueue.get(vl.getSchedulerMux())
                    .add(new TimedEvent(computedTimeStamp, e.contents));

                    entry.getValue().remove(e);
                }
            }
        }

        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // Delivers (if required) the intended token to the intended receiver
            for (Entry<String, LinkedList<TimedEvent>> entry : _afdxSchedMuxsQueue.entrySet()) {

                if (entry.getValue().size() > 0) {
                    TimedEvent e = entry.getValue().getFirst();

                    if (e.timeStamp.compareTo(currentTime) == 0) {
                        Object[] output = (Object[]) e.contents;
                        Receiver receiver = (Receiver) output[0];

                        // FIXME (GL): check properly if the receiver is an AFDXSwitch
                        // case: the receiver is an AFDXSwitch qm
                        if (receiver instanceof IntermediateReceiver) {
                            String[] labels = new String[] { timestamp, vlink, payload };
                            Token[] values = new Token[] {
                                    new DoubleToken(e.timeStamp.getDoubleValue()),
                                    new ObjectToken (output[2]),
                                    (Token) output[1] };
                            RecordToken record = new RecordToken(labels, values);
                            _sendToReceiver((Receiver) output[0], record);
                        } else {
                            // case: the receiver is an actor
                            Token token = (Token) output[1];
                            _sendToReceiver(receiver, token);
                        }

                        entry.getValue().remove(e);

                        if (_debugging) {
                            _debug("Send from end-system:" + entry.getKey()
                                    +" at time: " + currentTime);
                        }
                    }
                }
            }
        }
        _scheduleRefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     */
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver) {
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return createIntermediateReceiver(receiver);
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _nextFireTime = null;

        _afdxVLinksQueue = new HashMap();
        _virtualLinkTable = new HashMap();
        _lastEmissionTable = new HashMap();
        _afdxSchedMuxsQueue = new HashMap();

        _afdxVLinksQueue.clear();
        _virtualLinkTable.clear();
        _afdxSchedMuxsQueue.clear();
        _lastEmissionTable.clear();
    }

    /** Print all elements of the scheduler multiplexor queue.
     */
    public void printSMQueue() {
        _debug("-- Start printing SchedulerMultiplexor Queue");
        for (Entry<String, LinkedList<TimedEvent>> entry : _afdxSchedMuxsQueue.entrySet()) {
            String sm = entry.getKey();
            _debug("scheduler_multiplexor : " + sm + "{");

            for (TimedEvent e : entry.getValue()) {
                Object[] output = (Object[]) e.contents;
                _debug("timestamp: " + e.timeStamp + " | value=" + output[1].toString());
            }
            _debug("}");
        }
        _debug("-- Stop printing SchedulerMultiplexor Queue");
    }

    /** Print all elements of the different VL queues.
     */
    public void printVlinksQueue() {
        _debug("-- Start printing the VLinksQueue");
        for (Entry<String, LinkedList<TimedEvent>> entry : _afdxVLinksQueue.entrySet()) {
            String vl = entry.getKey();
           _debug("vlink_name : " + vl + " {");

            for (TimedEvent e : entry.getValue()) {
                Object[] output = (Object[]) e.contents;
                _debug("timestamp: " + e.timeStamp + " | value=" + output[1].toString());
            }
            _debug("}");
        }
        _debug("-- Stop printing the VLinksQueue");
    }

    /**
     * Reset the quantity manager.
     */
    public void reset() {

    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  according to the requirements of the AFDX protocol.
     *
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time lastTimeStamp = null;
        boolean multicast = false;

        AFDXVlink vl = new AFDXVlink (source);

        if (!_lastEmissionTable.containsKey(vl.getName())) {
            _lastEmissionTable.put(vl.getName(),
                    new Time(getDirector(), currentTime.getDoubleValue()));
        }

        if (!_virtualLinkTable.containsKey(vl.getName())) {
            _virtualLinkTable.put(vl.getName(), vl);
        }

        if (!_afdxSchedMuxsQueue.containsKey(vl.getSchedulerMux())) {
            _afdxSchedMuxsQueue.put(vl.getSchedulerMux(),
                    new LinkedList<TimedEvent>());
        }

        if (!_afdxVLinksQueue.containsKey(vl.getName())) {
            _afdxVLinksQueue.put(vl.getName(), new LinkedList());
        }

        // last emission timestamp and multicast (factorization)
        if (_afdxVLinksQueue.get(vl.getName()).size() > 0) {
            lastTimeStamp = _afdxVLinksQueue.get(vl.getName()).getLast().timeStamp;

            Object[] output = (Object[]) _afdxVLinksQueue.get(vl.getName()).getLast().contents;
            if (((AFDXVlink) output[2]).getSource().equals(
                    ((IntermediateReceiver) source).source)
                    && ((Time) output[3]).compareTo(currentTime) == 0) {
                multicast = true;
            }
        } else {
            lastTimeStamp = _lastEmissionTable.get(vl.getName());
        }

        if (multicast) {
            _lastEmissionTable.put(vl.getName(), lastTimeStamp);
            _afdxVLinksQueue.get(vl.getName()).add(new TimedEvent(lastTimeStamp, new Object[] { receiver, token, vl , currentTime}));
            multicast = false;
        }
        else {
            // Compute the delay according to the AFDX bag emission policy
            if (lastTimeStamp.compareTo(currentTime) == 0
                    && _afdxVLinksQueue.get(vl.getName()).size() < 1) {
                _delay = 0.0;
            } else if (lastTimeStamp.add(vl.getBag()).compareTo(currentTime) < 0) {
                _delay = 0.0;
            } else if (lastTimeStamp.add(vl.getBag()).compareTo(currentTime) == 0) {
                _delay = 0.0;
            } else if (lastTimeStamp.add(vl.getBag()).compareTo(currentTime) > 0) {
                _delay = lastTimeStamp.add(vl.getBag()).subtract(currentTime).getDoubleValue();
            }

            _lastEmissionTable.put(vl.getName(), currentTime.add(_delay));

            _afdxVLinksQueue.get(vl.getName()).add(new TimedEvent(currentTime.add(_delay),
                    new Object[] { receiver, token, vl , currentTime}));
        }

        _tokenCount++;
        sendQMTokenEvent((Actor) source.getContainer().getContainer(), 0,
                _tokenCount, EventType.RECEIVED);

        _scheduleRefire();
    }

    /** Get next fire time for a set of tokens which is either the minimum
     *  next fire time passed as an argument or the smallest timestamp of
     *  the tokens in the set.
     *  @param nextFireTime Minimum next fire time.
     *  @param tokens The set of tokens.
     *  @return The next time this actor should be fired based on the tokens
     *  in the queue.
     */
    protected Time _getNextFireTime(Time nextFireTime,
            LinkedList<TimedEvent> tokens) {
        if (tokens.size() > 0) {
            TimedEvent event = tokens.getFirst();
            if (event.timeStamp.compareTo(nextFireTime) < 0) {
                nextFireTime = event.timeStamp;
            }
        }
        return nextFireTime;
    }

    /** Schedule a refiring of this actor based on the tokens in the queues.
     *  @exception IllegalActionException If actor cannot be refired
     *  at the computed time.
     */
    protected void _scheduleRefire() throws IllegalActionException {
        _nextFireTime = Time.POSITIVE_INFINITY;

        for (Entry<String, LinkedList<TimedEvent>> entry : _afdxVLinksQueue.entrySet()) {
            _nextFireTime = _getNextFireTime(_nextFireTime, entry.getValue());
        }
        for (Entry<String, LinkedList<TimedEvent>> entry : _afdxSchedMuxsQueue.entrySet()) {
            _nextFireTime = _getNextFireTime(_nextFireTime, entry.getValue());
        }

        _fireAt(_nextFireTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Tokens received as input to the AFDX traffic regulator
     * (aka Lissor or Shaper).
     */
    protected HashMap<String, LinkedList<TimedEvent>> _afdxVLinksQueue;

    /** Routing table for virtual link object.
     */
    protected HashMap<String, AFDXVlink> _virtualLinkTable;

    /** Last emission timestamp for a given virtual link.
     */
    protected HashMap<String, Time> _lastEmissionTable;

    /** Tokens received in the AFDX scheduler multiplexor.
     */
    protected HashMap<String, LinkedList<TimedEvent>> _afdxSchedMuxsQueue;

    /** Next time a token is sent and the next token can be processed.
     */
    protected Time _nextFireTime;

    /** Computed delay according to the bag emission (see AFDX spec.).
     */
    protected double _delay;

    /** Value of the bit rate of the bus.
     */
    protected double _bitRate;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Label of the timestamp that is transmitted within the RecordToken.
     */
    private static final String timestamp = "timestamp";

    /** Label of the vlink that is transmitted within the RecordToken.
     */
    private static final String vlink = "vlink";

    /** Label of the payload that is transmitted within the RecordToken.
     */
    private static final String payload = "payload";

}
