/* Source of DeScrambler Code.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DeScrambler
/**
Descramble the input bit sequence using a feedback shift register.
The taps of the feedback shift register are given by the <i>polynomial</i>
parameter. The initial state of the shift register is given by the
<i>initial</i> parameter. This is a self-synchronizing descrambler that
will exactly reverse the operation of the Scrambler if the polynomials
are the same. The low-order bit of the polynomial should always be set.
For more information, see the documentation for the Scrambler actor
and Lee and Messerschmitt, Digital Communication, Second Edition,
Kluwer Academic Publishers, 1994, pp. 595-603.
<p>
@author Edward A. Lee and Rachel Zhou
@version $Id$
*/

public class DeScrambler extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DeScrambler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        polynomial = new Parameter(this, "polynomial");
        polynomial.setTypeEquals(BaseType.INT);
        polynomial.setExpression("0440001");

        initial = new Parameter(this, "initial");
        initial.setTypeEquals(BaseType.INT);
        initial.setExpression("1");

        // Declare input data type.
        input.setTypeEquals(BaseType.BOOLEAN);

        // Declare output data type.
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

     /** Integer defining a polynomial with binary coefficients.
      *  The coefficients indicate the presence (1) or absence (0)
      *  of a tap in a feedback shift register. This parameter should
      *  contain a positive integer with the lower-order bit being 1.
      *  Its default value is the integer 0440001.
      */
    public Parameter polynomial;

    /** Integer defining the intial state of the shift register.
     *  The n-th bit of the integer indicates the value of the
     *  n-th register. This parameter should be a non-negative
     *  integer. Its default value is the integer 1.
     */
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>initial</i>, then verify
        that is a non-negative interger; if it is <i>polynomial</i>, then
        verify that is a positive interger and the lower-order bit is 1.
     *  @exception IllegalActionException If <i>initial</i> is non-positive
     *  or polynomial is non-positive or the lower-order bit is not 1.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
          if (attribute == initial) {
          int seed = ((IntToken)initial.getToken()).intValue();
          if (seed<0 ) {
                throw new IllegalActionException(this,
                "shift register's value must be non-negative.");
            }
           } else if (attribute == polynomial) {
               int mask = ((IntToken)polynomial.getToken()).intValue();
               if (mask <= 0) {
                   throw new IllegalActionException(this,
                   "Polynomial is required to be strictly positive.");
               }
               if ((mask & 1) == 0) {
                   throw new IllegalActionException(this,
                   "The low-order bit of the the polynomial is not set.");
               }
           } else {
               super.attributeChanged(attribute);
           }
    }

    /** Read bit from the input port and fill it into the shift register
     *  to descramble. Compute the parity and send "true" to the output
     *  port if it is 1; otherwise send "false" to the output port.
     */
    public void fire() throws IllegalActionException {
        _latestShiftReg = _shiftReg;
        int mask = ((IntToken)polynomial.getToken()).intValue();
        BooleanToken inputToken = ((BooleanToken)input.get(0));
        int reg = _latestShiftReg << 1;
        // Put the input in the low-order bit: true = 1, false = 0.
        if (inputToken.booleanValue()){
            reg = reg | 1;
        }
        // Find the parity of "masked".
        int masked = mask & reg;
        int parity = 0;
        // Calculate the parity of the masked word.
        while (masked >0){
            parity = parity ^ (masked & 1);
            masked = masked >> 1;
            }

        _latestShiftReg = reg;
        if (parity == 1){
            output.broadcast(_tokenTrue);
        }else {
            output.broadcast(_tokenFalse);
        }
    }

    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initial</i>.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestShiftReg = _shiftReg = ((IntToken)initial.getToken()).intValue();
    }

    /** Record the most recent shift register state as the new
     *  initial state for the next iteration.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _shiftReg = _latestShiftReg;
        return super.postfire();
    }

    //////////////////////////////////////////////////////////////
    ////                     private variables                ////

    // Record the state of the shift register.
    private int _shiftReg;

    // Updated state of the shift register.
    private int _latestShiftReg;

    // Since this actor always sends one of the two tokens, we statically
    // create those tokens to avoid unnecessary object construction.
    private static BooleanToken _tokenTrue = new BooleanToken(true);
    private static BooleanToken _tokenFalse = new BooleanToken(false);
}
