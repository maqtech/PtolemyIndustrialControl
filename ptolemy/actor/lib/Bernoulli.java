/* An actor that outputs a random sequence of booleans.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Bernoulli
/**
Produce a random sequence of booleans.  The output is of type BooleanToken.
The values that are generated are independent and identically distributed,
where the probability of <i>true</i> is given by the parameter
<i>trueProbability</i>.
The seed can be specified as a parameter to control the sequence that is
generated.
This actor uses the class java.util.Random to generate random numbers.

@author Edward A. Lee
@version $Id$
*/

public class Bernoulli extends RandomSource {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Bernoulli(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output.setTypeEquals(BooleanToken.class);

        trueProbability = new Parameter(this, "trueProbability",
                new DoubleToken(0.5));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability of <i>true</i>.
     *  This parameter contains a DoubleToken, initially with value 0.5.
     */
    public Parameter trueProbability;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the public variables.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Bernoulli newobj = (Bernoulli)super.clone(ws);
        newobj.trueProbability =
            (Parameter)newobj.getAttribute("trueProbability");
        return newobj;
    }

    /** Send a random boolean to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     */
    public void fire() {
        try {
            super.fire();
            output.broadcast(new BooleanToken(_current));
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Calculate the next random boolean for this iteration.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
	double tp = ((DoubleToken)(trueProbability.getToken())).doubleValue();
        double rawNum = _random.nextDouble();
        if (rawNum < tp) {
            _current = true;
        } else {
            _current = false;
        }
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The random boolean for the current iteration.
    private boolean _current;
}

