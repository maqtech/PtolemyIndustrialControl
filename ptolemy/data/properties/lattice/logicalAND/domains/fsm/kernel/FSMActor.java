/* A helper class for ptolemy.actor.AtomicActor.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND.domains.fsm.kernel;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintFSMHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 A helper class for ptolemy.actor.FSMActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class FSMActor extends PropertyConstraintFSMHelper {

    /**
     * Construct a helper for the given AtomicActor. This is the
     * helper class for any ActomicActor that does not have a
     * specific defined helper class. Default actor constraints
     * are set for this helper. 
     * @param actor The given ActomicActor.
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public FSMActor(PropertyConstraintSolver solver, 
            ptolemy.domains.fsm.kernel.FSMActor actor)
            throws IllegalActionException {
        
        super(solver, actor);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        
        return super.constraintList();
    }
}
