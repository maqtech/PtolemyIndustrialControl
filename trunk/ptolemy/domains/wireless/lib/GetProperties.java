/* An actor that retrieves the received properties of the specified port.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// GetProperties

/**
This actor retrieves the properties most recently received by
the specified input port, which must be contained by the container
of this actor.  A typical usage pattern is
inside an instance of WirelessComposite, to connect the
trigger input to the port from which you want to read
the properties.
<p>
NOTE: The type of the properties port is inferred from the
<i>defaultProperties</i> field of the channel used by the specified
port at preinitialize()
time. If the channel is changed during execution, or this
field is changed, then the type of the port will not be updated,
and a run-time type error could occur.
Thus, this actor assumes that these types do not change.
If the channel has no default properties (as in the base class
WirelessChannel), then the type of the properties port will
be undefined. If the output is left disconnected, then this is fine,
but if it is connected, then its type will need to be declared
explicitly.

@author Edward A. Lee
@version $Id$
*/
public class GetProperties extends TypedAtomicActor {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GetProperties(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        output = new TypedIOPort(this, "output", false, true);
        // FIXME: lame: disable default type inference.
        output.setTypeEquals(BaseType.UNKNOWN);

        // Create and configure the parameters.
        portName = new StringParameter(this, "portName");
        portName.setExpression("input");

        // Create and configure the ports.       
        trigger = new TypedIOPort(this, "trigger", true, false);

        // FIXME: Show the name of the port.        
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
 
    /** Port that triggers execution.
     */
    public TypedIOPort trigger;

    /** Name of the input port from which to read properties.
     *  This is a string that defaults to "input".
     */
    public StringParameter portName;

    /** Port that transmits the properties received on the <i>input</i>
     *  port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
 
    /** Read the properties from the specified input port and produce
     *  them at the output. If there are no properties, then produce
     *  no output.
     *  @exception IllegalActionException If the specified port is not
     *   an instance of WirelessIOPort, or if there is no such port.
     */
    public void fire() throws IllegalActionException {

        super.fire();
      
        if (trigger.hasToken(0)) {
            Token inputValue = trigger.get(0);
        }
        String portNameValue = portName.stringValue();
        Entity container = (Entity)getContainer();
        if (container != null) {
            Port port = container.getPort(portNameValue);
            if (port == null || !(port instanceof WirelessIOPort)) {
                throw new IllegalActionException(this,
                "portName does not specify a WirelessIOPort: "
                + portNameValue);
            }
            Token propertiesValue = ((WirelessIOPort)port).getProperties(0);
            
            // Do not send properties if the port has no destinations.
            // This prevents run-time type errors from occurring.
            if (propertiesValue == null || output.numberOfSinks() == 0) {
                return;
            }
            if (propertiesValue != null) {
                output.send(0, propertiesValue);
            }
        }
    }

    /** Create receivers and set up the type constraints on the
     *  <i>output</i> port.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Set the type of the output to match the channel properties.
        String portNameValue = portName.stringValue();
        Entity container = (Entity)getContainer();
        if (container != null) {
            Port port = container.getPort(portNameValue);
            if (port == null || !(port instanceof WirelessIOPort)) {
                throw new IllegalActionException(this,
                "portName does not specify a WirelessIOPort: "
                + portNameValue);
            }
            String channelName
                    = ((WirelessIOPort)port).outsideChannel.stringValue();
            CompositeEntity container2 = (CompositeEntity)container.getContainer();
            if (container2 == null) {
                throw new IllegalActionException(this,
                "The container does not have a container.");         
            }
            Entity channel = container2.getEntity(channelName);
            if (channel instanceof WirelessChannel) {
                Parameter channelProperties = ((WirelessChannel)channel)
                        .defaultProperties;
                // Only set up the type constraint if the type of the
                // of the properties field is known.
                if (channelProperties.getType() != BaseType.UNKNOWN) {
                    output.setTypeSameAs(channelProperties);
                }
            } else {
                throw new IllegalActionException(this,
                "The referenced port does not refer to a valid channel.");
            }
        }
    }
}
