/* An actor that outputs a scaled version of the input.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Scale
/**
Produce an output token on each firing with a value that is
equal to a scaled version of the input.  The actor is polymorphic
in that it can support any token type that supports multiplication
by the <i>gain</i> parameter.  The output type is constained to be at least
as general as both the input and the <i>gain</i> parameter.
For data types where multiplication is not commutative (such
as matrices), the parameter is multiplied on the left, and the input
on the right.

@author Edward A. Lee
@version $Id$
*/

public class Scale extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scale(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        gain = new Parameter(this, "gain", new IntToken(1));

	// set the type constraints.
	output.setTypeAtLeast(input);
	output.setTypeAtLeast(gain);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The gain.  The default value of this parameter is the integer 1.
     */
    public Parameter gain;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Scale newobj = (Scale)super.clone(ws);
        newobj.gain = (Parameter)newobj.getAttribute("gain");
	newobj.output.setTypeAtLeast(newobj.input);
	newobj.output.setTypeAtLeast(newobj.gain);
        return newobj;
    }

    /** Compute the product of the input and the <i>gain</i>.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token in = input.get(0);
            Token gainToken = gain.getToken();
            Token result = gainToken.multiply(in);
            output.broadcast(result);
        }
    }
}

