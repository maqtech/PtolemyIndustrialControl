/* A helper class for ptolemy.actor.AtomicActor.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.exampleSetLattice.actor.lib;

import java.util.List;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.PropertySet;
import ptolemy.data.properties.lattice.exampleSetLattice.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AtomicActor

/**
 A helper class for ptolemy.actor.AtomicActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Const extends PropertyConstraintHelper {

    /**
     * Construct a helper for the given AtomicActor. This is the
     * helper class for any ActomicActor that does not have a
     * specific defined helper class. Default actor constraints
     * are set for this helper.
     * @param solver The given solver.
     * @param actor The given ActomicActor.
     * @exception IllegalActionException
     */
    public Const(PropertyConstraintSolver solver,
            ptolemy.actor.lib.Const actor)
            throws IllegalActionException {

        super(solver, actor);
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        Lattice lattice = (Lattice) getSolver().getLattice();
        ptolemy.actor.lib.Const actor = (ptolemy.actor.lib.Const) getComponent();

        if (actor.value.getExpression().equalsIgnoreCase("\"a\"")) {
            setAtLeast(actor.value, new PropertySet(lattice,
                    new Property[] {lattice.A}));
        } else if (actor.value.getExpression().equalsIgnoreCase("\"b\"")) {
            setAtLeast(actor.value, new PropertySet(lattice,
                    new Property[] {lattice.B}));
        } else if (actor.value.getExpression().equalsIgnoreCase("\"c\"")) {
            setAtLeast(actor.value, new PropertySet(lattice,
                    new Property[] {lattice.C}));
        } else {
            setAtLeast(actor.value, new PropertySet(lattice,
                    new Property[] {lattice.UNKNOWN}));
        }

        setAtLeast(actor.output, actor.value);
        return super.constraintList();
    }

}
