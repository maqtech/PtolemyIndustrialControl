/* An actor that outputs a random sequence with a Gaussian distribution.

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
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Gaussian
/**
Produce a random sequence with a Gaussian distribution.  On each iteration,
a new random number is produced.  The output port is of type DoubleToken.
The values that are generated are independent and identically distributed
with the mean and the standard deviation given by parameters.  In addition, the
seed can be specified as a parameter to control the sequence that is
generated.

@author Edward A. Lee
@version $Id$
*/

public class Gaussian extends RandomSource {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Gaussian(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        mean = new Parameter(this, "mean", new DoubleToken(0.0));
        standardDeviation = new Parameter(this,
                "standardDeviation", new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The mean of the random number.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter mean;

    /** The standard deviation of the random number.
     *  This parameter contains a DoubleToken, initially with value 1.
     */
    public Parameter standardDeviation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Gaussian newobj = (Gaussian)super.clone(ws);
        newobj.output.setTypeEquals(BaseType.DOUBLE);
        newobj.mean = (Parameter)newobj.getAttribute("mean");
        newobj.standardDeviation = (Parameter)newobj.getAttribute(
                "standardDeviation");
        return newobj;
    }

    /** Send a random number with a Gaussian distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     */
    public void fire() {
        try {
            super.fire();
            output.send(0, new DoubleToken(_current));
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    /** Calculate the next random number.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
	double meanValue = ((DoubleToken)(mean.getToken())).doubleValue();
	double standardDeviationValue =
            ((DoubleToken)(standardDeviation.getToken())).doubleValue();
        double rawNum = _random.nextGaussian();
        _current = (rawNum*standardDeviationValue) + meanValue;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The random number for the current iteration.
    private double _current;
}
