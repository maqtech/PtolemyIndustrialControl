/* An actor that delays the input by the specified amount.

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
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEORGate
/**
This actor performs a binary AND operation and produce the output after
a specified delay of time.

@author Lukito Muliadi
@version $Id$
*/
public class DEORGate extends DEActor {

    /** Construct a DEORGate actor with the default delay equal to 0.1.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container, or if the delay is less than zero.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEORGate(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name,0.1);

    }

    /** Construct a DEORGate actor with the specified delay.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *  @param delay The amount of propagation delay for the gate.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container, or if the delay is less than zero.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEORGate(TypedCompositeActor container,
            String name, double delay)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        if (delay < 0.0) {
            throw new IllegalActionException(this,
                    "Invalid delay.  Cannot be less than zero.");
        }
        _delay = new Parameter(this, "delay", new DoubleToken(delay));

        // create the ports
        output = new DEIOPort(this, "output", false, true);
        input1 = new DEIOPort(this, "input1", true, false);
        input2 = new DEIOPort(this, "input2", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Transfer the input tokens to the outputs with the specified delay.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {

        if (input1.hasToken(0)) {
            _in1 = ((DoubleToken)input1.get(0)).doubleValue();
        }
        if (input2.hasToken(0)) {
            _in2 = ((DoubleToken)input2.get(0)).doubleValue();
        }
        boolean in1 = (_in1 == 1.0);
        boolean in2 = (_in2 == 1.0);

        DoubleToken outToken = null;
        if (in1 || in2) {
            outToken = new DoubleToken(1.0);
        } else {
            outToken = new DoubleToken(0.0);
        }

        output.broadcast(outToken, ((DoubleToken)_delay.getToken()).doubleValue());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public DEIOPort output;
    public DEIOPort input1;
    public DEIOPort input2;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //private double _delay = 0.0;
    private Parameter _delay;
    private double _in1;
    private double _in2;
}
