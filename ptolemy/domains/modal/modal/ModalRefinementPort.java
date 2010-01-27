/* An IOPort for controllers and refinements in modal models.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.Iterator;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ModalRefinementPort

/**
 A port for refinements in modal models that are themselves modal models.  This port
 mirrors certain changes to it in the ports of the container of the container and
 in the ports of the controller and refinements of the modal model that contains it.
 It is designed to work closely with RefinementPort and ModalPort, since changes to the
 ports can be initiated in either class.

 @see ModalPort
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (liuxj)
 */
public class ModalRefinementPort extends RefinementPort {
    /** Construct a port with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ModalRefinementPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Move this object down by one in the list of attributes of
     *  its container. If this object is already last, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveDown() throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            int result = super.moveDown();

            // If there was actually a move, then mirror the move
            // downward in the hierarchy.
            if (result != -1) {
                CompositeEntity container = (CompositeEntity) getContainer();
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean priorStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.moveDown();
                        } finally {
                            castPort._mirrorDisable = priorStatus;
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. If  this object is already first,
     *  do nothing. Increment the version of the workspace.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToFirst() throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            int result = super.moveToFirst();

            if (result != -1) {
                // Mirror the change in mirror ports.
                CompositeEntity container = (CompositeEntity) getContainer();
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.moveToFirst();
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the specified position in the list
     *  of attributes of the container. If this object is already at the
     *  specified position, do nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @param index The position to move this object to.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container or if the index is out of bounds.
     */
    public int moveToIndex(int index) throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            int result = super.moveToIndex(index);

            if (result != -1) {
                // Mirror the change in mirror ports.
                CompositeEntity container = (CompositeEntity) getContainer();
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.moveToIndex(index);
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object to the last position in the list
     *  of attributes of the container.  If this object is already last,
     *  do nothing. This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveToLast() throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            int result = super.moveToLast();

            if (result != -1) {
                // Mirror the change in mirror ports.
                CompositeEntity container = (CompositeEntity) getContainer();
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.moveToLast();
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Move this object up by one in the list of
     *  attributes of the container. If  this object is already first, do
     *  nothing.
     *  This method overrides the base class to mirror the change
     *  in any mirror ports.
     *  Increment the version of the workspace.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    public int moveUp() throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            int result = super.moveUp();

            if (result != -1) {
                // Mirror the change in mirror ports.
                CompositeEntity container = (CompositeEntity) getContainer();
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.moveUp();
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Override the base class so that if the port is being removed
     *  from the current container, then it is also removed from the
     *  controller and from each of the refinements.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj oldContainer = getContainer();
        
        if (_mirrorDisable || oldContainer == null) {
            // Mirroring changes from above, or there
            // is no pre-existing above.
            
            // Use the superclass to delegate up the hierarchy and mirror the change
            // to ports in the enclosing modal model and its refinements.
            super.setContainer(container);

            if ((oldContainer instanceof CompositeEntity) && (container != oldContainer)) {
                // The port is being removed from the current container.
                // Remove it from the mirrored ports.
                Iterator entities = ((CompositeEntity)oldContainer).entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(getName());

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.setContainer(null);
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }

                // Remove the relation as well.
                ComponentRelation relation = ((CompositeEntity)oldContainer).getRelation(getName()
                        + "Relation");

                if (relation != null) {
                    relation.setContainer(null);
                }
            }
        } else {
            // Not mirroring changes from above.
            // Use the superclass to delegate up the hierarchy and mirror the change
            // to ports in the enclosing modal model and its refinements.
            // But don't mirror below in the hierarchy because the code
            // above will be called as a result of delegating upwards.
            super.setContainer(container);
        }
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isInput True to make the port an input.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setInput(boolean isInput) throws IllegalActionException {
        try {
            _workspace.getWriteAccess();
            
            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            super.setInput(isInput);

            // Mirror the change in mirror ports.
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator entities = container.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Port mirrorPort = entity.getPort(getName());

                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort) mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;

                    try {
                        castPort._mirrorDisable = true;
                        castPort.setInput(isInput);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }
    
    /** If the argument is true, make the port a multiport.
     *  If the argument is false, make the port not a multiport.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isMultiport True to make the port a multiport.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setMultiport(boolean isMultiport) throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            super.setMultiport(isMultiport);
            
            // Mirror the change in mirror ports.
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator entities = container.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Port mirrorPort = entity.getPort(getName());

                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort) mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;

                    try {
                        castPort._mirrorDisable = true;
                        castPort.setMultiport(isMultiport);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Set the name of the port, and mirror the change in all the
     *  mirror ports.
     *  This method is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @exception IllegalActionException If the name has a period.
     *  @exception NameDuplicationException If there is already a port
     *   with the same name in the container.
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            String oldName = getName();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            super.setName(name);

            // Mirror the change in mirror ports.
            CompositeEntity container = (CompositeEntity) getContainer();

            // NOTE: This is called before there is even a container
            // to originally set the name.
            if (container != null) {
                Iterator entities = container.entityList().iterator();

                while (entities.hasNext()) {
                    Entity entity = (Entity) entities.next();
                    Port mirrorPort = entity.getPort(oldName);

                    if (mirrorPort instanceof RefinementPort) {
                        RefinementPort castPort = (RefinementPort) mirrorPort;
                        boolean disableStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.setName(name);
                        } finally {
                            castPort._mirrorDisable = disableStatus;
                        }
                    }
                }

                // Rename the corresponding relation.
                Relation relation = container.getRelation(oldName + "Relation");

                if (relation != null) {
                    relation.setName(name + "Relation");
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  In addition, if the container is an instance of Refinement,
     *  and the argument is true, find the corresponding port of the
     *  controller and make it an input and not an output.  This makes
     *  it possible for the controller to see the outputs of the refinements.
     *  This method overrides the base class to make the same
     *  change on the mirror ports in the controller and state refinments.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isOutput True to make the port an output.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted.
     */
    public void setOutput(boolean isOutput) throws IllegalActionException {
        try {
            _workspace.getWriteAccess();

            // Use the superclass, which will mirror the change
            // upward in the hierarchy to the ports of the container
            // unless the change originated from above.
            super.setOutput(isOutput);
            
            // Mirror the change in mirror ports.
            CompositeEntity container = (CompositeEntity) getContainer();
            Iterator entities = container.entityList().iterator();

            while (entities.hasNext()) {
                Entity entity = (Entity) entities.next();
                Port mirrorPort = entity.getPort(getName());

                if (mirrorPort instanceof RefinementPort) {
                    RefinementPort castPort = (RefinementPort) mirrorPort;
                    boolean disableStatus = castPort._mirrorDisable;

                    try {
                        castPort._mirrorDisable = true;
                        castPort.setOutput(isOutput);
                    } finally {
                        castPort._mirrorDisable = disableStatus;
                    }

                    // If the entity is a controller, then set the
                    // port to also be an input.
                    if (entity.getName().equals("_Controller")) {
                        boolean controlPortStatus = castPort._mirrorDisable;

                        try {
                            castPort._mirrorDisable = true;
                            castPort.setInput(true);

                            // Mark that the input property is
                            // automatically set, so that if it
                            // is changed, that change is not
                            // mirrored.
                            if (!isInput()) {
                                castPort._automaticallyInput = true;
                            }
                        } finally {
                            castPort._mirrorDisable = controlPortStatus;
                        }
                    }
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a CompositeEntity or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof CompositeEntity) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "ModalPort can only be contained by CompositeEntity objects. "
                            + "The container was: " + container);
        }
    }
}
