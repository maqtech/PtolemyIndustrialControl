/* An actor that converse 32 boolean tokens to an IntToken

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

@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////
/// BitsToInt
/** This actor takes in a integer token and output 32 boolean tokens 
    which represents that integer.

@author Michael Leung
@version $Id$
*/

public class BitsToInt extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BitsToInt(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        
        super(container, name);

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTokenConsumptionRate(32);
        input.setTypeEquals(BooleanToken.class);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTokenProductionRate(1);
        output.setTypeEquals(IntToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type BooleanToken. */
    public SDFIOPort input;

    /** The output port. This has type IntToken. */
    public SDFIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            BitsToInt newobj = (BitsToInt)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
                return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }


    /** Consume 32 BooleanTokens on the input. Produce a single
     *  IntToken on the output port which the 32 bitsBooleanTokens
     *  represent.
     *
     *  @exception IllegalActionException will be thrown if attempt to
     *  fire this actor when there is no director.
     */

    public final void fire() throws IllegalActionException  {
        int i;
        int integer = 0;
        BooleanToken[] bits = new BooleanToken[32];
        
        input.getArray(0, bits);
        
        for (i = 0; i < 32; i++) {
            integer = integer << 1;
            if (bits[31 - i].booleanValue())
                integer += 1;
        }
    
        IntToken value = new IntToken(integer);
        output.send(0, value);
    }
    
    private BooleanToken[] bits;
    private int integer;
    private int remainder;
    private IntToken value;
}



