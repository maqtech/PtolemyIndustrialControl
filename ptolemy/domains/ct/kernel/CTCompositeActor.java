/* Composite Actor in the CT domain.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;


//////////////////////////////////////////////////////////////////////////
//// CTCompositeActor
/**
Composite actor in the CT domain. This class is derived from 
TypedCompositeActor
and implements the CTStepSizeControlActor interface. If the director of
this composite actor is an instance of CTTransparentDirector, then the
CTStepSizeControlActor calls will be delegated to its local director.
Otherwise, it returns default values.
<P>
This composite actor should be used when a CT subsystem need to transfer
its step size control information to the outer domain. Typical usage
includes CT inside CT or CT inside FSM inside CT.

@author  Jie Liu
@version $Id$
@see CTStepSizeControlActor
@see CTTransparentDirector
*/
public class CTCompositeActor extends TypedCompositeActor
        implements CTStepSizeControlActor {

    /** Construct a CTCompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     */
    public CTCompositeActor() {
        super();
    }

    /** Construct a CTCompositeActor in the specified workspace with no
     *  container
     *  and an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CTCompositeActor(Workspace workspace) {
	super(workspace);
    }

    /** Create an CTCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  The director should be set before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CTCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return true, indicating that this composite actor does not
     *  perform step size control.
     *  @return True if this step is accurate.
     */
    public boolean isThisStepAccurate() {
        Director dir = getDirector();
        if((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).isThisStepAccurate();
        }
        return true;
    }

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return java.lang.Double.MAX_VALUE.
     *  @return The predicted step size.
     */
    public double predictedStepSize() {
        Director dir = getDirector();
        if((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).predictedStepSize();
        }
        return java.lang.Double.MAX_VALUE;
    }

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return the current step size of the executive director.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        Director dir = getDirector();
        if((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).refinedStepSize();
        }
        return ((CTDirector)getExecutiveDirector()).getCurrentStepSize();
    }
}
