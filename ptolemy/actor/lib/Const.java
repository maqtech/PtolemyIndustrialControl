/* A constant source.

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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Const
/**
An actor that produces a constant output. The type and value of the
output is determined by a parameter set by the user.

@author Yuhong Xiong
@version $Id$
*/

public class Const extends TypedAtomicActor {

    /** Construct a constant source. The value of the constant output
     *  is determined by the specified expression. If the expression is
     *  null, it is set to the default value "0".
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param expr The String expression of the constant output.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Const(TypedCompositeActor container, String name, String expr)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

    	_value = new Parameter(this, "Value");
	if (expr == null) {
	    _value.setExpression("0");
	} else {
	    _value.setExpression(expr);
	}
	_value.evaluate();
    	_output = new TypedIOPort(this, "Output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the Parameter containing the value of this constant
     *  source and set the declared type.
     *   @exception IllegalActionException Not thrown in this class.
     */
    // FIXME: this might not work if user change the parameter during
    // simulation.
    public void initialize()
	    throws IllegalActionException {
        _output.setDeclaredType(_value.getType());
    }

    /** Output a token of the constant value.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
	    throws IllegalActionException {
        _output.broadcast(_value.getToken());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Parameter _value = null;
    private TypedIOPort _output = null;
}

