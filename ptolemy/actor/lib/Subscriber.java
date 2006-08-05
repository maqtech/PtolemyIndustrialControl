/* A subscriber that transparently receives tunneled messages from publishers.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Subscriber

/**
FIXME
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Subscriber extends TypedAtomicActor {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Subscriber(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        // Set this up as input port.
        super(container, name);

        channel = new StringParameter(this, "channel");
        channel.setExpression("channel");
        
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        
        new Parameter(input, "_hide", BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to "channel".
     */
    public StringParameter channel;
    
    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is the channel, increment the workspace version
     *  to force cached receiver lists to be updated, and invalidate
     *  the schedule and resolved types of the director, if there is one.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                _channel = newValue;
                _updateLinks();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Read all available input tokens and send them to the subscribers,
     *  if any.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // FIXME: Input should be a multiport.
        for (int i = 0; i < input.getWidth(); i++) {
            while (input.hasToken(i)) {
                Token token = input.get(i);
                output.send(i, token);
            }
        }
    }

    /** Override the base class to remove prior links, if any, and
     *  to create new links, if appropriate.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != getContainer()) {
            super.setContainer(container);
            _updateLinks();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The relation used to link to subscribers. */
    protected TypedIORelation _relation;
    
    /** Cached channel name. */
    protected String _channel;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the publisher, if there is one.
     *  @return A publisher, or null if none is found.
     */
    private Publisher _findPublisher() {
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity)getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity)container.getContainer();
        }
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Publisher) {
                    if (_channel.equals(((Publisher)actor)._channel)) {
                        return (Publisher)actor;
                    }
                }
            }
        }
        return null;
    }
    
    /** Update the connection to the publisher, if there is one.
     */
    private void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }
        Publisher publisher = _findPublisher();
        
        // Remove the link to a previous relation, if necessary.
        if (_relation != null) {
            input.unlink(_relation);
            _relation = null;
        }
        if (publisher != null) {
            _relation = publisher._relation;
            input.liberalLink(_relation);
        }
        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }
}
