/* Check the input streams against a parameter value, ignoring absent values.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// NonStrictTest
/**

This actor compares the inputs against the value specified by the
<i>correctValues</i> parameter.  That parameter is an ArrayToken,
where each element of the array is of the same type as the input.  If,
on a given iteration, no token is present on a channel, the absence is
ignored and no exception is thrown.  Subsequent iterations always
succeed, so the actor can be used as a "power-up" test for a model,
checking the first few iterations against some known results.

<p> Unlike the Test actor, NonStrictTest does not support a multiport
input, only single port inputs are supported.

<p> The input values are checked in the postfire() method.  If an
input value is absent, the absence is ignored and on the next firing
the input is compared against the same value in the
<i>correctValues</i> Parameter.  If an input value is present and
differs from what it should be, then postfire() throws an exception.
Thus, the test passes if no exception is thrown.

<p> If the input is a DoubleToken or ComplexToken, then the comparison
passes if the value is close to what it should be, within the
specified <i>tolerance</i> (which defaults to 10<sup>-9</sup>.  The
input data type is undeclared, so it can resolve to anything.


@See Test
@author Paul Whitaker, Christopher Hylands
@version $Id$
*/

public class NonStrictTest extends Test {

    // The Test actor could be extended so that Strictness was a parameter,
    // but that would require some slightly tricky code to handle
    // multiports in a non-strict fashion.  The problem is that if
    // we have more than one input channel, and we want to handle
    // non-strict inputs, then we need to keep track of number of
    // tokens we have seen on each channel.

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonStrictTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iteration = 0;
    }

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>.  If the token count
     *  is larger than the length of <i>correctValues</i>, then return
     *  immediately, indicating that the inputs correctly matched
     *  the values in <i>correctValues</i> and that the test succeeded.
     *
     *  @exception IllegalActionException If an input does not match
     *   the required value or if the width of the input is not 1.
     */
    public boolean postfire() throws IllegalActionException {
	if (input.getWidth() != 1) {
	    throw new IllegalActionException(this,
			 "Width of input is " + input.getWidth()
			 + "but NonStrictTest only supports a width of 1.");
	}
	if (_numberOfInputTokensSeen
	    >= ((ArrayToken)(correctValues.getToken())).length()) {
	    // Consume and discard input values.  We are beyond the end
	    // of the correctValues array.
	    if (input.hasToken(0)) {
		input.get(0);
	    }
	    return true;
	}

	Token referenceToken
	    = ((ArrayToken)(correctValues.getToken()))
	    .getElement(_numberOfInputTokensSeen);
	if (referenceToken instanceof ArrayToken) {
	    throw new IllegalActionException(this,
			 "Reference is an ArrayToken, "
			 + "but NonStrictTest only supports a width of 1.");
	}

	if (input.hasToken(0)) {
	    Token token = input.get(0);
	    _numberOfInputTokensSeen++;
	    if (token.isCloseTo(referenceToken, _tolerance).booleanValue()
		== false)
		throw new IllegalActionException(this,
                        "Test fails in iteration " + _iteration
                        + ".\n"
                        + "Value was: " + token
		        + ". Should have been: " + referenceToken);

	}
	_iteration++;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of iterations.
    private int _iteration;
}
