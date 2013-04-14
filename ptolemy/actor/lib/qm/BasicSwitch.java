/* A switch with a service rule.

@Copyright (c) 2011-2013 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.qm.QuantityManagerListener.EventType;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** A {@link QuantityManager} actor that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. This quantity manager is used on
 *  input ports by setting a parameter with an ObjectToken that refers
 *  to this QuantityManager at the port. Note that the name of this
 *  parameter is irrelevant.
 *
 *  <p>This quantity manager implements a simple switch. It has a parameter
 *  specifying the number of ports. On each port, an actor is connected.
 *  Note that these ports are not represented as ptolemy actor ports.
 *  This actor can send tokens to the switch and receive tokens from the
 *  switch. The mapping of ports to actors is done via parameters of this
 *  quantity manager.
 *
 *  <p>Internally, this switch has a buffer for every input, a buffer
 *  for the switch fabric and a buffer for every output. The delays
 *  introduced by the buffers are configured via parameters. Tokens are
 *  processed simultaneously on the buffers.
 *
 *  <p> This switch implements a very basic switch fabric consisting
 *  of a FIFO queue.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class BasicSwitch extends MonitoredQuantityManager {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.f
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BasicSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _inputTokens = new HashMap<Integer, TreeSet<TimedEvent>>();
        _outputTokens = new HashMap<Integer, TreeSet<TimedEvent>>();
        _switchFabricQueue = new TreeSet<TimedEvent>();
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        _tokenCount = 0;

        inputBufferDelay = new Parameter(this, "inputBufferDelay");
        inputBufferDelay.setExpression("0.1");
        inputBufferDelay.setTypeEquals(BaseType.DOUBLE);
        _inputBufferDelay = 0.1;

        outputBufferDelay = new Parameter(this, "outputBufferDelay");
        outputBufferDelay.setExpression("0.1");
        outputBufferDelay.setTypeEquals(BaseType.DOUBLE);
        _outputBufferDelay = 0.1;

        switchFabricDelay = new Parameter(this, "switchFabricDelay");
        switchFabricDelay.setExpression("0.1");
        switchFabricDelay.setTypeEquals(BaseType.DOUBLE);
        _switchFabricDelay = 0.1;

        numberOfPorts = new Parameter(this, "numberOfPorts");
        numberOfPorts.setExpression("4");
        numberOfPorts.setTypeEquals(BaseType.INT);
        _numberOfPorts = 4;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  For now, we only support wrapping input ports.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException If the receiver is an
     *  ouptut port.
     */
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        if (receiver.getContainer().isOutput()) {
            throw new IllegalActionException(receiver.getContainer(),
                    "This quantity manager cannot be " + "used on port "
                            + receiver.getContainer()
                            + ", it only be specified on input port.");
        }
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

    /** Make sure that this quantity manager is only used in the DE domain.
     *  FIXME: this actor should be used in other domains later as well.
     *  @param container The container of this actor.
     *  @exception IllegalActionException If thrown by the super class or if the
     *  director of this actor is not a DEDirector.
     *  @exception NameDuplicationException If thrown by the super class.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (getDirector() != null && !(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "This quantity manager is currently only supported in the DE domain.");
        }
    }

    /** If the attribute for the input, switch fabric or output delay is
     *  changed, then ensure that the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the buffer delays are negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == inputBufferDelay) {
            double value = ((DoubleToken) inputBufferDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _inputBufferDelay = value;
        } else if (attribute == outputBufferDelay) {
            double value = ((DoubleToken) outputBufferDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _outputBufferDelay = value;
        } else if (attribute == switchFabricDelay) {
            double value = ((DoubleToken) switchFabricDelay.getToken())
                    .doubleValue();
            if (value <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero serviceTime: " + value);
            }
            _switchFabricDelay = value;
        } else if (attribute == numberOfPorts) {
            int ports = ((IntToken) numberOfPorts.getToken()).intValue();
            _numberOfPorts = ports;
            for (int i = 0; i < ports; i++) {
                _inputTokens.put(i, new TreeSet());
                _outputTokens.put(i, new TreeSet());
            }
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
     *  @return A new Bus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        BasicSwitch newObject = (BasicSwitch) super.clone(workspace); 
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        newObject._nextFireTime = null;
        newObject._inputTokens = new HashMap<Integer, TreeSet<TimedEvent>>();
        newObject._outputTokens = new HashMap<Integer, TreeSet<TimedEvent>>();
        newObject._switchFabricQueue = new TreeSet<TimedEvent>();
        return newObject;
    }

    /** Initialize the actor variables.
     *  @exception IllegalActionException If the superclass throws it or
     *  the switch table could not be parsed from the actor parameters.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextFireTime = null;
        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new TreeSet());
            _outputTokens.put(i, new TreeSet());
        }

        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new TreeSet());
            _outputTokens.put(i, new TreeSet());
        }
        _switchFabricQueue = new TreeSet();
    }

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric queue to the output queues and send tokens from the
     *  output queues to the target receivers. When moving tokens between
     *  queues the appropriate delays are considered.
     *  @exception IllegalActionException If the token cannot be sent to
     *  target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // move tokens from input queue to switch fabric

            TimedEvent event;
            for (int i = 0; i < _numberOfPorts; i++) {
                if (_inputTokens.get(i).size() > 0) {
                    event = _inputTokens.get(i).first();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Time lastTimeStamp = currentTime;
                        if (_switchFabricQueue.size() > 0) {
                            lastTimeStamp = _switchFabricQueue.last().timeStamp;
                        }
                        _switchFabricQueue.add(new TimedEvent(lastTimeStamp
                                .add(_switchFabricDelay), event.contents));
                        _inputTokens.get(i).remove(event);
                    }
                }
            }

            // move tokens from switch fabric to output queue

            if (_switchFabricQueue.size() > 0) {
                event = _switchFabricQueue.first();
                if (event.timeStamp.compareTo(currentTime) == 0) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];

                    Actor actor;
                    if (receiver instanceof IntermediateReceiver) {
                        actor = (Actor) ((IntermediateReceiver) receiver).quantityManager;
                    } else {
                        actor = (Actor) receiver.getContainer().getContainer();
                    }
                    int outputPortID = _getPortID(receiver, false);
                    Time lastTimeStamp = currentTime;
                    if (_outputTokens.get(outputPortID).size() > 0) {
                        lastTimeStamp = _outputTokens.get(outputPortID).last().timeStamp;
                    }
                    _outputTokens.get(outputPortID).add(
                            new TimedEvent(lastTimeStamp
                                    .add(_outputBufferDelay), event.contents));
                    _switchFabricQueue.remove(event);
                }
            }

            // send tokens to target receiver

            for (int i = 0; i < _numberOfPorts; i++) {
                if (_outputTokens.get(i).size() > 0) {
                    event = _outputTokens.get(i).first();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Object[] output = (Object[]) event.contents;
                        Receiver receiver = (Receiver) output[0];
                        Token token = (Token) output[1];
                        _sendToReceiver(receiver, token);
                        _tokenCount--;
                        sendQMTokenEvent(this, 0,
                                _tokenCount, EventType.RECEIVED);
                        _outputTokens.get(i).remove(event);
                    }
                }
            }

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send");
            }
        }
    }

    /** If there are still tokens in the queue and a token has been
     *  produced in the fire, schedule a refiring.
     *  @exception IllegalActionExeception If the refiring cannot be scheduled or
     *  by super class.
     */
    public boolean postfire() throws IllegalActionException {
        _scheduleRefire();
        return super.postfire();
    }
    
    protected int _getPortID(Receiver receiver, boolean input) {
        NamedObj containerPort = receiver.getContainer();
        while (!(receiver.getContainer() instanceof Port)) {
            containerPort = containerPort.getContainer();
        }
        Port port = (Port) containerPort;
        
        if (input) {
            return _ioPortToSwitchInPort.get(port);
        } else {
            return _ioPortToSwitchOutPort.get(port);
        }
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The sending receiver.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException { 

        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        // FIXME: Is this the right thing to do?
        // Presumably, this is an issue with the Continuous domain.
        if (getDirector() instanceof DEDirector && token == null) {
            return;
        }
        Time currentTime = getDirector().getModelTime();
        
        int inputPortID = _getPortID(receiver, true);
        
        Time lastTimeStamp = currentTime;
        if (_inputTokens.get(inputPortID).size() > 0) {
            lastTimeStamp = _inputTokens.get(inputPortID).last().timeStamp;
        }
        _inputTokens.get(inputPortID).add(
                new TimedEvent(lastTimeStamp.add(_inputBufferDelay),
                        new Object[] { receiver, token }));
        _tokenCount++;
        sendQMTokenEvent((Actor) source.getContainer().getContainer(), 0,
                _tokenCount, EventType.RECEIVED);
        
        _scheduleRefire();

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }
    
    /** Return the list of Attributes that can be specified per port with default
     *  values for the specified port.
     *  @param container The container parameter.
     *  @param port The port.
     *  @return List of attributes.
     *  @exception IllegalActionException Thrown if attributeList could not be created.
     */
    public List<Attribute> getPortAttributeList(Parameter container, Port port) throws IllegalActionException {
        List<Attribute> list = _parameters.get(port);
        if (list == null) {
            list = new ArrayList<Attribute>();
            try {
                Parameter portIn = new Parameter(container, "portIn", new IntToken(0));
                Parameter portOut = new Parameter(container, "portOut", new IntToken(1));
                _ioPortToSwitchInPort.put(port, 0);
                _ioPortToSwitchOutPort.put(port, 1);
                list.add(portIn);
                list.add(portOut);
            } catch (NameDuplicationException ex) {
                // This cannot happen.
            }
        } 
        return list;
    }
    
    /** Set an attribute for a given port.
     *  @param port The port. 
     *  @param attribute The new attribute or the attribute containing a new value.
     *  @exception IllegalActionException Thrown if attribute could not be updated.
     */
    public void setPortAttribute(Port port, Attribute attribute) throws IllegalActionException {
        super.setPortAttribute(port, attribute);
        if (attribute.getName().equals("portIn")) {
            _ioPortToSwitchInPort.put((IOPort)port, ((IntToken)((Parameter)attribute).getToken()).intValue());
        } else if (attribute.getName().equals("portOut")) {
            _ioPortToSwitchOutPort.put((IOPort)port, ((IntToken)((Parameter)attribute).getToken()).intValue());
        }
    } 
    
    protected HashMap<Port, Integer> _ioPortToSwitchInPort;
    protected HashMap<Port, Integer> _ioPortToSwitchOutPort;

    /** Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _inputTokens.clear();
        _outputTokens.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Number of ports on the switch. This parameter must contain an
     *  IntToken.  The value defaults to 4. */
    public Parameter numberOfPorts;

    /** Time it takes for a token to be put into the input queue.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.1. */
    public Parameter inputBufferDelay;

    /** Time it takes for a token to be put into the output queue.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.1. */
    public Parameter outputBufferDelay;

    /** Time it takes for a token to be processed by the switch fabric.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.1. */
    public Parameter switchFabricDelay;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get next fire time for a set of tokens which is either the minimum
     *  next fire time passed as an argument or the smallest timestamp of
     *  the tokens in the set.
     *  @param nextFireTime Minimum next fire time.
     *  @param tokens The set of tokens.
     *  @return The next time this actor should be fired based on the tokens
     *  in the queue.
     */
    protected Time _getNextFireTime(Time nextFireTime,
            TreeSet<TimedEvent> tokens) {
        if (tokens.size() > 0) {
            TimedEvent event = tokens.first();
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
        for (int i = 0; i < _numberOfPorts; i++) {
            _nextFireTime = _getNextFireTime(_nextFireTime, _inputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _outputTokens.get(i));
        }
        _nextFireTime = _getNextFireTime(_nextFireTime, _switchFabricQueue);
        _fireAt(_nextFireTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Time it takes for a token to be put into the input queue. */
    protected double _inputBufferDelay;

    /** Time it takes for a token to be put into the output queue. */
    protected double _outputBufferDelay;

    /** Time it takes for a token to be processed by the switch fabric. */
    protected double _switchFabricDelay;

    /** Next time a token is sent and the next token can be processed. */
    protected Time _nextFireTime;

    /** Tokens received by the switch. */
    protected HashMap<Integer, TreeSet<TimedEvent>> _inputTokens;

    /** Tokens to be sent to outputs. */
    protected HashMap<Integer, TreeSet<TimedEvent>> _outputTokens;

    /** Number of switch ports. */
    protected int _numberOfPorts;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Tokens processed by the switch fabric. */
    private TreeSet<TimedEvent> _switchFabricQueue;

}
