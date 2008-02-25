package ptolemy.domains.ptides.kernel;

import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.vergil.basic.GetDocumentationAction;

public class DEReceiver4Ptides extends PrioritizedTimedQueue {
    
	
	public DEReceiver4Ptides() {
        super();
    }

    /** Construct an empty DEReceiver with the specified container.
     *  @param container The container.
     *  @exception IllegalActionException If the container does
     *  not accept this receiver.
     */
    public DEReceiver4Ptides(IOPort container) throws IllegalActionException {
        super(container);
    }
    

    /** Return true if there is at least one token available to the
     *  get() method.
     *  @return True if there are more tokens.
     */
    public boolean hasToken() {
    	if (super.hasToken())
    		return hasToken(getModelTime());
    	else
    		return false;
    }


    /** Put a token into this receiver and post a trigger event to the director.
     *  The director will be responsible to dequeue the trigger event at
     *  the correct timestamp and microstep and invoke the corresponding actor
     *  whose input port contains this receiver. This receiver may contain
     *  more than one events.
     *  @param token The token to be put.
     */
    public void put(Token token) {
        put(token, getModelTime());
    }
    
    public void put(Token token, Time time) {
    	try {
    		EmbeddedDEDirector4Ptides dir = _getDirector();
            dir._enqueueTriggerEvent(getContainer(), time);
            
            if (token == null)
            	System.out.println(token);	
            super.put(token, time);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(null, ex, null);
        }
    }
    
    public void putToAll(Token token, Receiver[] receivers, Time time)
	    	throws NoRoomException, IllegalActionException {
		for (int j = 0; j < receivers.length; j++) {
		    IOPort container = receivers[j].getContainer();
		
		    // If there is no container, then perform no conversion.
		    if (container == null) {
		        ((DEReceiver4Ptides)receivers[j]).put(token, time);
		    } else {
		    	((DEReceiver4Ptides)receivers[j]).put(container.convert(token), time);
		    }
		}
	}

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the director that created this receiver.
     *  If this receiver is an inside receiver of
     *  an output port of an opaque composite actor,
     *  then the director will be the local director
     *  of the container of its port. Otherwise, it's the executive
     *  director of the container of its port.Note that
     *  the director returned is guaranteed to be non-null.
     *  This method is read synchronized on the workspace.
     *  @return An instance of DEDirector.
     *  @exception IllegalActionException If there is no container port, or
     *   if the port has no container actor, or if the actor has no director,
     *   or if the director is not an instance of DEDirector.
     */
    private EmbeddedDEDirector4Ptides _getDirector() throws IllegalActionException {
        IOPort port = getContainer();

        if (port != null) {
            if (_directorVersion == port.workspace().getVersion()) {
                return _director;
            }

            // Cache is invalid.  Reconstruct it.
            try {
                port.workspace().getReadAccess();
                Actor actor = (Actor) port.getContainer();
                if (actor != null) {
                    Director dir;

                    if (!port.isInput() && (actor instanceof CompositeActor)
                            && ((CompositeActor) actor).isOpaque()) {
                        dir = actor.getDirector();
                    } else {
                        dir = actor.getExecutiveDirector();
                    }

                    if (dir != null) {
                        if (dir instanceof EmbeddedDEDirector4Ptides) {
                            _director = (EmbeddedDEDirector4Ptides) dir;
                            _directorVersion = port.workspace().getVersion();
                            return _director;
                        } else {
                            throw new IllegalActionException(getContainer(),
                                    "Does not have a EmbeddedDEDirector4Ptides.");
                        }
                    }
                }
            } finally {
                port.workspace().doneReading();
            }
        }

        throw new IllegalActionException(getContainer(),
                "Does not have a IOPort as the container of the receiver.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The director where this DEReceiver should register for De events.
    private EmbeddedDEDirector4Ptides _director;

    private long _directorVersion = -1;

}
