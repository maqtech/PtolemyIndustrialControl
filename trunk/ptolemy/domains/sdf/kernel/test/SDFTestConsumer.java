/*
@Copyright (c) 1998-2001 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red
@AcceptedRating Red
*/
package ptolemy.domains.sdf.kernel.test;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

/**
 * This actor will consume all tokens on its input port and write their
 * values to a string.  The value of the string can then be obtained
 * for use in test scripts, etc.
 *
 * This actor is aware of the rate that is set on its input port and will
 * consume an appropriate number of tokens with each firing.
 * This actor is type Polymorphic.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class SDFTestConsumer extends TypedAtomicActor {
    public SDFTestConsumer(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new SDFIOPort(this, "input", true, false);
        input.setTokenConsumptionRate(1);
        _history = new StringBuffer("");
    }

    public SDFIOPort input;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SDFTestConsumer newObject = (SDFTestConsumer)(super.clone(workspace));
        newObject._history = new StringBuffer(_history.toString());
        return newObject;
    }

    /**
     * Fire the Actor
     * Consume an input token, and append its value to the history.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        int tokens = input.getTokenConsumptionRate();
        int i;
        for(i = 0; i < tokens; i++) {
            Token t = input.get(0);
            _history.append(t.toString() + "\n");
        }
    }

    /**
     * Return a string representing the values of the tokens that have been
     * consumed so far by this actor, since its creation.
     */
    public String getHistory() {
        return _history.toString();
    }

    private StringBuffer _history;
}
