/* Chopstick in Sieve of Eratosthenes demo.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Chopstick
/**
Chopstick in Sieve of Eratosthenes demo. Each Chopstick can only be used by
one philosopher at a time. When it is not being used it can be
claimed by either of the two philosophers next to it. Once it has
been claimed, it is not available until it is released by the
philosopher holding it.
<p>
This actor has four ports, each of width one: two are used to communicate
with the philosopher on the left, and two are used to communicate with the
buffer on the right. Two ports are needed to communicate with each philosopher
as the philosopher holds the chopstick for some random time.
<p>
@author Neil Smyth
@version $Id$

*/

public class CSPChopstick extends CSPActor {
    /** Construct a CSPChopstick in the default workspace with an empty string
     *  as its name.
     *  The actor is created with two input ports and two output
     *  ports, all of width one. The input ports are called "leftIn"
     *  and "rightIn", and similarly, the output ports are called "leftOut"
     *  and "rightOut".
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the port or parameter cannot
     *   be contained by this actor.
     *  @exception NameDuplicationException If the port name coincides with
     *   a port already in this actor, or if the parameter name coincides with
     *   a parameter already in this actor
     */
    public CSPChopstick()
            throws IllegalActionException, NameDuplicationException{
        super();
        leftOut = new TypedIOPort(this, "leftOut", false, true);
        leftIn = new TypedIOPort(this, "leftIn", true, false);
        rightOut = new TypedIOPort(this, "rightOut", false, true);
        rightIn = new TypedIOPort(this, "rightIn", true, false);

	leftIn.setDeclaredType(Token.class);
	rightIn.setDeclaredType(Token.class);
	leftOut.setDeclaredType(IntToken.class);
	rightOut.setDeclaredType(IntToken.class);
    }

    /** Construct a CSPChopstick in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. The actor is created with
     *  two input ports and two output
     *  ports, all of width one. The input ports are called "leftIn"
     *  and "rightIn", and similarly, the output ports are called "leftOut"
     *  and "rightOut".
     *  <p>
     *  @param container The TypedCompositeActor that contains this actor.
     *  @param name The actor's name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name argument coincides with
     *   an entity already in the container.
     */
    public CSPChopstick(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);
         leftOut = new TypedIOPort(this, "leftOut", false, true);
         leftIn = new TypedIOPort(this, "leftIn", true, false);
         rightOut = new TypedIOPort(this, "rightOut", false, true);
         rightIn = new TypedIOPort(this, "rightIn", true, false);

	leftIn.setDeclaredType(Token.class);
	rightIn.setDeclaredType(Token.class);
	leftOut.setDeclaredType(IntToken.class);
	rightOut.setDeclaredType(IntToken.class);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Executes the code in this actor. This actor uses a CDO
     *  construct when it is waiting to be used by either of the
     *  philosophers next to it. Once one of the philosophers is using
     *  it, this actor waits to receive a message that the philosopher
     *  is finished eating (using it). It is a good example of using a CDO.
     *  This process continues executing until a TerminateProcessException
     *  is thrown.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     */
    public void fire() throws IllegalActionException {
        try {
            boolean guard = true;
            boolean continueCDO = true;
            ConditionalBranch[] branches = new ConditionalBranch[2];
            Token t = new IntToken(0);
            while (continueCDO) {
                // step 1
                branches[0] = new ConditionalSend(guard, leftOut, 0, 0, t);
                branches[1] = new ConditionalSend(guard, rightOut, 0, 1, t);

                // step 2
                int successfulBranch = chooseBranch(branches);

                // step 3
                if (successfulBranch == 0) {
                    // Wait for philosopher on left to finish eating.
                    leftIn.get(0);
                } else if (successfulBranch == 1) {
                    // Wait for philosopher on right to finish eating.
                    rightIn.get(0);
                } else if (successfulBranch == -1) {
                    // all guards false so exit CDO
                    continueCDO = false;
                } else {
                    throw new IllegalActionException(getName() + ": " +
                            "invalid branch id returned during execution " +
                            "of CDO.");
                }
            }
        } catch (NoTokenException ex) {
            throw new IllegalActionException(getName() + ": cannot " +
                    "get token.");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private TypedIOPort leftIn;
    private TypedIOPort leftOut;
    private TypedIOPort rightIn;
    private TypedIOPort rightOut;
}
