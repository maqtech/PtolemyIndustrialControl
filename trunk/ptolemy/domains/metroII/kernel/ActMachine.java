/* Abstract wrapper for actors.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

///////////////////////////////////////////////////////////////////
//// ActMachine

/**
 * 
 * Abstract wrapper for actors. ActMachine is a FSM. The states of the FSM represent
 * the state of the wrapped actor. Each state may associate with a MetroII event and 
 * these events are supposed to be used as the interface interacting with outside. 
 * The StartOrResumable interface has to be implemented by subclass as the triggering 
 * function of the FSM, in which the transitions are going to be defined.
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public abstract class ActMachine implements StartOrResumable {

    /** Actor state
     */
    public enum State {
        PREFIRE_BEGIN, PREFIRE_END_FIRE_BEGIN, FIRING, FIRE_END_POSTFIRE_BEGIN, POSTFIRE_END
    }

    /**
     * Construct an ActMachine wrapper and initialize the MetroII events
     * 
     * @param actor the actor to be wrapped.
     */
    public ActMachine(Actor actor) {
        _actor = actor;

        String actorName = _actor.getFullName();
        String actorNameWithoutModelName = _trimModelName(actorName);

        _PrefireBeginEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "PREFIRE_BEGIN", actorName);
        _FireBeginEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "FIRE_BEGIN", actorName);
        _FiringEvent = MetroEventBuilder.newProposedEvent(actorNameWithoutModelName
                + "." + "FIRING", actorName);
        _PostfireBeginEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "POSTFIRE_BEGIN", actorName);
        _PostfireEndEvent = MetroEventBuilder.newProposedEvent(
                actorNameWithoutModelName + "." + "POSTFIRE_END", actorName);

        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the MetroII event associated with the current state. 
     * 
     * @return the MetroII event associated with the current state.
     */
    public Builder getStateEvent() {
        switch (getState()) {
        case PREFIRE_BEGIN:
            return _PrefireBeginEvent;
        case PREFIRE_END_FIRE_BEGIN:
            return _FireBeginEvent;
        case FIRING:
            return _FiringEvent;
        case FIRE_END_POSTFIRE_BEGIN:
            return _PostfireBeginEvent;
        case POSTFIRE_END:
            return _PostfireEndEvent;
        default:
            assert false;
            return null;
        }
    }

    /**
     * Get the state of the wrapped actor
     * 
     * @return The state.
     */
    public State getState() {
        return _state;
    }

    /**
     * Reset the state to be PREFIRE_BEGIN
     */
    @Override
    public void reset() {
        setState(State.PREFIRE_BEGIN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /**
     * Return the wrapped actor.
     * 
     * @return the wrapped actor.
     */
    protected Actor actor() {
        return _actor;
    }

    /**
     * Return the MetroII event associated with the current state and 
     * set the state of the event to be PROPOSED.
     * 
     * @return the MetroII event associated with the current state
     */
    protected Builder proposeStateEvent() {
        Builder event = getStateEvent();
        event.setStatus(Status.PROPOSED);
        event.clearTime();
        return event;
    }

    /**
     * Set the state of the wrapped actor
     * 
     * @param state
     */
    protected void setState(State state) {
        _state = state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _trimModelName(String name) {
        assert name.length() > 1;
        int pos = name.indexOf(".", 1);
        return name.substring(pos);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * Prefire begin event.
     */
    final private Builder _PrefireBeginEvent;

    /**
     * Fire begin event
     */
    final private Builder _FireBeginEvent;

    /**
     * Firing event
     */
    final private Builder _FiringEvent;

    /**
     * Postfire begin event
     */
    final private Builder _PostfireBeginEvent;

    /**
     * Postfire end event
     */
    final private Builder _PostfireEndEvent;

    /** 
     * Actor state
     */
    private State _state;

    /** Actor which is being fired
     *
     */
    private Actor _actor;
}
