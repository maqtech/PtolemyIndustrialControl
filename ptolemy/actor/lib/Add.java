/* A polymorphic scalar adder.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Adder
/**
A polymorphic scalar adder.

This adder has an input port which is a multiport and an output port.
The types on the ports are undeclared and will be resolved by
the type resolution mechanism.<p>

This adder is not strict. That is, it does not require all the input
channels to have a token upon firing.

@author Yuhong Xiong
@version $Id$
*/

public class Add extends TypedAtomicActor {

    /** Construct an adder in the specified container with the specified
     *  name.
     *
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Add(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	input = new TypedIOPort(this, "Input", true, false);
	input.setMultiport(true);
	output = new TypedIOPort(this, "Output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Add newobj = (Add)super.clone(ws);
            newobj.input = new TypedIOPort(this, "input", true, false);
            newobj.input.setMultiport(true);
            newobj.output = new TypedIOPort(this, "output", false, true);
            return newobj;
        } catch (KernelException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Internal error: " + ex.getMessage());
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** If there is at least one token in the input port, add the
     *  tokens on all the channels of the input port and send
     *  the result to the output port. If none of the input port
     *  channels has a token, do nothing.
     *
     *  @exception IllegalActionException If there is no director,
     *   and hence no receivers have been created.
     */
    public void fire()
	    throws IllegalActionException {
	Token sum = null;
	for (int i = 0; i < input.getWidth(); i++) {
	    if (input.hasToken(i)) {
		if (sum == null) {
		    sum = input.get(i);
		} else {
		    sum = sum.add(input.get(i));
		}
	    }
	}

	if (sum != null) {
	    output.broadcast(sum);
	}
    }

    /** Return the type constraints: Input <= Output; Output <= Scalar.
     */
    public Enumeration typeConstraints() {
	LinkedList result = new LinkedList();
	result.appendElements(super.typeConstraints());

	TypeTerm scalar = new TypeTerm(ScalarToken.class);
	Inequality ineq = new Inequality(output.getTypeTerm(), scalar);
	result.insertLast(ineq);

	return result.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////

    public TypedIOPort input = null;
    public TypedIOPort output = null;
}

