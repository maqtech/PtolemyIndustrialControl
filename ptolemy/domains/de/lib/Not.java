/* A logical negation.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Not
/** Negate the input token. If the input is non-zero then the output is zero,
 *  and vice-versa.
 *  <p>
 *  FIXME: This actor should operate on boolean tokens.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class Not extends TypedAtomicActor {

    /** Constructor.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     * @exception NameDuplicationException If the container already has an
     *  actor with this name.
     * @exception IllegalActionException FIXME: useless description:
     *  internal problem
     */
    public Not(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Produce an output token that is logical negation of the input.
     *  FIXME: Fix the following exception tags.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
	// get the input token from the input port.
        DoubleToken inputToken;
        inputToken = (DoubleToken)(input.get(0));

        // produce the output token.
	DoubleToken outputToken;
	if (inputToken.doubleValue() == 0.0)
	    outputToken = new DoubleToken(1.0);
	else
	    outputToken = new DoubleToken(0.0);

        // send the output token via output TypedIOPort.
        output.broadcast(outputToken);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public TypedIOPort output;
    public TypedIOPort input;
}
