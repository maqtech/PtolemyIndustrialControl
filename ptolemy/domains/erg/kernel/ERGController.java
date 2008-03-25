/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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

package ptolemy.domains.erg.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.domains.erg.lib.SynchronizeToRealtime;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.StateEvent;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGController extends ModalController {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ERGController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }
    
    public boolean synchronizeToRealtime() {
        List<?> synchronizeAttributes =
            attributeList(SynchronizeToRealtime.class);
        boolean synchronize = false;
        if (synchronizeAttributes.size() > 0) {
            SynchronizeToRealtime attribute =
                (SynchronizeToRealtime) synchronizeAttributes.get(0);
            try {
                synchronize = ((BooleanToken) attribute.getToken()).booleanValue();
            } catch (IllegalActionException e) {
                return false;
            }
        }
        return synchronize;
    }
    
    public boolean hasInput() throws IllegalActionException {
        Iterator<?> inPorts = ((ERGModalModel) getContainer()).inputPortList()
                .iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) inPorts.next();
            Token token =
                (Token) _inputTokenMap.get(p.getName() + "_isPresent");
            if (token != null && BooleanToken.TRUE.equals(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param workspace
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ERGController(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    public void fire() throws IllegalActionException {
        director.fire();
    }

    public State getInitialState() {
        return null;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        director.initialize();
    }

    public boolean isFireFunctional() {
        return director.isFireFunctional();
    }

    public boolean isStrict() {
        return director.isStrict();
    }

    public int iterate(int count) throws IllegalActionException {
        return director.iterate(count);
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            SchedulingRelation relation = new SchedulingRelation(this, name);
            return relation;
        } finally {
            workspace().doneWriting();
        }
    }

    public boolean postfire() throws IllegalActionException {
        return director.postfire();
    }

    public boolean prefire() throws IllegalActionException {
        return director.prefire();
    }

    public void stop() {
        director.stop();
    }

    public void stopFire() {
        director.stopFire();
    }

    public void terminate() {
        director.terminate();
    }

    public List<?> typeConstraintList() {
        List<Object> constraintList = new LinkedList<Object>(
                (List<?>) super.typeConstraintList());
        List<?> events = entityList(Event.class);
        for (Object eventObject : events) {
            Event event = (Event) eventObject;
            List<?> attributes = event.attributeList(HasTypeConstraints.class);
            for (Object attributeObject : attributes) {
                HasTypeConstraints attribute =
                    (HasTypeConstraints) attributeObject;
                constraintList.addAll((List<?>) attribute.typeConstraintList());
            }
        }
        return constraintList;
    }

    public ERGDirector director;

    protected void _debug(Event event) {
        _debug(new StateEvent(this, event));
    }

    protected void _setCurrentEvent(Event event) {
        _currentState = event;
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        director = new ERGDirector(this, "_Director");
        new SingletonAttribute(director, "_hide");
    }
}
