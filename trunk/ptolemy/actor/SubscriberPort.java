/* An output port that publishes its data on a named channel.

 Copyright (c) 1997-2011 The Regents of the University of California.
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
 */
package ptolemy.actor;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SubscriberPort

/**
 This is a specialized input port that subscribes to data sent
 to it on the specified named channel.
 The tokens are "tunneled" from an instance of
 {@link PublisherPort} that names the same channel.
 If {@link #global} is false (the default), then this subscriber
 will only see instances of PublisherPort that are under the
 control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director). If {@link #global} is true,
 then the publisher may be anywhere in the model, as long as its
 <i>global</i> parameter is also true.
 <p>
 Any number of instances of SubscriberPort can subscribe to the same
 channel.
 <p>
 This actor actually has a hidden input port that is connected
 to the publisher via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs.  Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SubscriberPort extends PubSubPort {

    /** Construct a subscriber port with a containing actor and a name.
     *  This is always an input port.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SubscriberPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        setOutput(false);
        setInput(true);
        
        // In order for this to show up in the vergil library, it has to have
        // an icon description.
        _attachText("_smallIconDescription", "<svg>\n"
                + "<polygon points=\"0,4 0,9 12,0 0,-9 0,-4 -8,-4 -8,4\" "
                + "style=\"fill:cyan\"/>\n" + "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a publish and subscribe channel is set, then set up the connections.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown if the new color attribute cannot
     *      be created.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor
                            && !(_channel == null || _channel.trim().equals(""))) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                _channel, this, _global);
                    }
                }
                _channel = newValue;
            }
        } else if (attribute == global) {
            boolean newValue = ((BooleanToken) global.getToken())
                    .booleanValue();
            if (newValue == false && _global == true) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor
                            && !(_channel == null || _channel.trim().equals(""))) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                _channel, this, _global);
                    }
                }
            }
            _global = newValue;
            // Do not call SubscriptionAggregator.attributeChanged()
            // because it will remove the published port name by _channel.
            // If _channel is set to a real name (not a regex pattern),
            // Then chaos ensues.  See test 3.0 in SubscriptionAggregator.tcl
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Notify this object that the containment hierarchy above it will be
     *  changed, which results in 
     *  @exception IllegalActionException If unlinking to a published port fails.
     */
    @Override
    public void hierarchyWillChange() throws IllegalActionException {
        if (channel != null) {
            String channelValue = null;
            try {
                // The channel may refer to parameters via $
                // but the parameters are not yet in scope.
                channelValue = channel.stringValue();
            } catch (Throwable throwable) {
                channelValue = channel.getExpression();
            }
            if (channelValue != null) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                channelValue, this);
                    }
                }
            }
        }
        super.hierarchyWillChange();
    }
    
    /** Override the base class to ensure that there is a publisher.
     *  @exception IllegalActionException If there is no matching
     *   publisher, if the channel is not specified or if the port
     *   is in the top level.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_channel == null) {
            throw new IllegalActionException(this, "No channel specified.");
        }
        NamedObj actor = getContainer();
        if (actor != null && actor.getContainer() == null) {
            throw new IllegalActionException(this,
                    "SubscriberPorts cannot be used at the top level, use a Subscriber actor instead.");
        }
        if (((InstantiableNamedObj)getContainer()).isWithinClassDefinition()) {
            // Don't preinitialize Class Definitions.
            // See $PTII/ptolemy/actor/lib/test/auto/PublisherToplevelSubscriberPortAOC.xml 
            return;
        }
        _updateLinks();
    }

    /** Override the base class to only accept setting to be an input.
     *  @param isInput True to make the port an input.
     *  @exception IllegalActionException If the argument is false.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        if (!isInput) {
            throw new IllegalActionException(this,
                    "SubscriberPort is required to be an input port.");
        }
        super.setInput(true);
    }
    
    /** Override the base class to refuse to make the port an output.
     *  @param isOutput Required to be false.
     *  @exception IllegalActionException If the argument is true.
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        if (isOutput) {
            throw new IllegalActionException(this,
                    "SubscriberPort cannot be an output port.");
        }
        super.setOutput(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the connection to the publisher, if there is one.
     *  Note that this method is computationally intensive for large
     *  models as it traverses the model by searching
     *  up the hierarchy for the nearest opaque container
     *  or the top level and then traverses the contained entities.
     *  Thus, avoid calling this method except when the model
     *  is running.
     *  @exception IllegalActionException If creating the link
     *   triggers an exception.
     */
    protected void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }
        
        NamedObj immediateContainer = getContainer();
        if (immediateContainer != null) {
            NamedObj container = immediateContainer.getContainer();
            if (container instanceof CompositeActor) {
                try {
                    try {
                        ((CompositeActor) container).linkToPublishedPort(
                                _channel, this, _global);
                    } catch (IllegalActionException ex) {
                        // If we have a LazyTypedCompositeActor that
                        // contains the Publisher, then populate() the
                        // model, expanding the LazyTypedCompositeActors
                        // and retry the link.  This is computationally
                        // expensive.
                        // See $PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml
                        _updatePublisherPorts((CompositeEntity)toplevel());
                        // Now try again.
                        try {
                            ((CompositeActor) container).linkToPublishedPort(
                                    _channel, this, _global);
                        } catch (IllegalActionException ex2) {
                            // Rethrow with the "this" so that Go To Actor works.
                            throw new IllegalActionException(this, ex2, "Failed to update link.");
                        }
                    }
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e,
                            "Can't link SubscriptionAggregatorPort with a PublisherPort.");
                }
            }
        }
    }
    
    /** Traverse the model, starting at the specified object
     *  and examining objects below it in the hierarchy, to find
     *  all instances of PublisherPort and make sure that they have
     *  registered their port. This method defeats lazy composites
     *  and is expensive to execute.
     *  @param root The root of the tree to search.
     *  @throws IllegalActionException If the port rejects its channel.
     */
    protected void _updatePublisherPorts(Entity root) throws IllegalActionException {
        List<Port> ports = root.portList();
        for (Port port : ports) {
            if (port instanceof PublisherPort) {
                // FIXME: Not sure if this is necessary
                StringParameter channel = ((PublisherPort)port).channel;
                channel.validate();
                port.attributeChanged(channel);
            }
        }
        if (root instanceof CompositeEntity) {
            List<Entity> entities = ((CompositeEntity)root).entityList();
            for (Entity entity : entities) {
                _updatePublisherPorts(entity);
            }
        }
    }
}
