/* An actor that outputs the sum of the inputs so far.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// Accumulator
/**
Output the initial value plus the sum of all the inputs since
the last time a true token was received at the reset port.
One output is produced each time the actor is fired. The
inputs and outputs can be any token type that supports addition.
The output type is constrained to be the greater than or
equal to the input type and the initial value type.

@author Edward A. Lee
@version $Id$
*/

public class Accumulator extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Accumulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input.setMultiport(true);

        reset = new TypedIOPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.BOOLEAN);

        init = new Parameter(this, "init", new IntToken(0));

	// set the type constraints.
	output.setTypeAtLeast(init);
	output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by the ramp on its first iteration.
     *  The default value of this parameter is the integer 0.
     */
    public Parameter init;

    /** The reset port of type BooleanToken. If this port
     *  receives a True token, then the accumulator state will be
     *  reset to the initial value.
     */
    public TypedIOPort reset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        Accumulator newObject = (Accumulator)super.clone(workspace);
	// set the type constraints.
	newObject.output.setTypeAtLeast(newObject.init);
	newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Consume at most one token from the <i>input</i> port,
     *  add it to the running sum, and produce the result at the
     *  <i>output</i> port.  If there is no input token available,
     *  the current value of the running sum is the output value.
     *  If there is a true-valued token on the <i>reset</i> input,
     *  then the running sum is reset to the initial value before
     *  adding the input.
     *  @exception IllegalActionException If addition is not
     *   supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException {
        _latestSum = _sum;
        // Check whether to reset.
        for (int i = 0; i < reset.getWidth(); i++) {
            if (reset.hasToken(i)) {
                BooleanToken r = (BooleanToken)reset.get(i);
                if(r.booleanValue()) {
                    // Being reset at this firing.
                    _latestSum = init.getToken();
                }
            }
        }
        for (int i=0; i<input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token in = input.get(i);
                _latestSum = _latestSum.add(in);
            }
        }
        output.broadcast(_latestSum);
    }

    /** Reset the running sum to equal the value of <i>init</i>.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestSum = _sum = init.getToken();
    }

    /** Record the most recent input as part of the running average.
     *  Do nothing if there is no input.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _sum = _latestSum;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Token _sum;
    private Token _latestSum;
}
