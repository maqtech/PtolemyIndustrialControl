/* An actor that outputs monotonically increasing values.

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
import ptolemy.graph.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DERamp
/**
An actor that produces an output event with a monotonically increasing value
when stimulated by an input event. The value of the output event starts at
<code>value</code> and increases by <code>step</code> each time the actor
fires.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DERamp extends TypedAtomicActor {

    /** Construct a DERamp with default parameters.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DERamp(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, 0, 1);
    }

    /** Construct a DERamp with the default value and step size.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DERamp(TypedCompositeActor container, String name,
            double value, double step)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        _value = new Parameter(this, "value", new DoubleToken(value));
        _step = new Parameter(this, "step", new DoubleToken(step));
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(Token.class);
    }

    /** Construct a DERamp with the specified container, name, initial
     *  value and step size. The initial value and step size are
     *  represented by String expressions which will be evaluated
     *  by the corresponding Parameters.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The expression for the initial output event value.
     *  @param step The expression for the step size by which to
     *   increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DERamp(TypedCompositeActor container, String name,
            String value, String step)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // set the parameters.
        _value = new Parameter(this, "value");
	_value.setExpression(value);
        _step = new Parameter(this, "step");
	_step.setExpression(step);
        // create an output port
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        // create an input port
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(Token.class);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public TypedIOPort output;
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME: No comment here yet.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        Class valueClass = _value.getToken().getClass();
        Class stepClass = _step.getToken().getClass();

        int compare = TypeLattice.compare(valueClass, stepClass);
        // FIXME: this might not work if user change the parameter during
        // simulation.
        if (compare == CPO.INCOMPARABLE) {
            throw new InvalidStateException(
                    "Bad parameter type in DERamp.initialize()");
        }
        if (compare == CPO.LOWER) {
            output.setTypeEquals(stepClass);
        } else {
            output.setTypeEquals(valueClass);
        }
        _stateToken = _value.getToken();
    }



    /** Produce the next ramp output with the same time stamp as the current
     *  input.
     *  FIXME: better exception tags needed.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        // output the state token.
        while (input.hasToken(0)) {
            input.get(0);
            output.broadcast(_stateToken);

            // increment the state.
            _stateToken = _stateToken.add(_step.getToken());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the intial value and increment
    private Token _stateToken;
    private Parameter _value;
    private Parameter _step;
}
