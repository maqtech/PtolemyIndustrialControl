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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

// FIXME: when the interface to the type system simplifies, these
// will no longer be needed.
import ptolemy.graph.Inequality;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Const
/**
Produce a constant output. The type and value of the
output is that of the token contained by the <i>value</i> parameter,
which by default is an IntToken with value 1.

@author Yuhong Xiong, Edward A. Lee
@version $Id$
*/

public class Const extends Source {

    /** Construct a constant source with the given container and name.
     *  Create the <i>value</i> parameter, set its type to Token,
     *  and set its default value to an IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Const(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // Have to initialize this with a Token so that it has the most
        // general possible type.
    	value = new Parameter(this, "value", _initToken);
        // Reset the token to the default value.
        value.setToken(new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The value produced by this constant source. The type of this
     *  parameter is Token, meaning that you can insert any token into
     *  it.  By default, it contains an IntToken with value 1.  If the
     *  type of this token is changed during the execution of a model,
     *  then the manager will be asked to redo type resolution.
     */
    public Parameter value = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Const newobj = (Const)super.clone(ws);
        newobj.value = (Parameter)newobj.getAttribute("value");
        return newobj;
    }

    /** Send the constant value to the output.
     *  @exception IllegalActionException If it is thrown by the method
     *   sending out the token.
     */
    public void fire() throws IllegalActionException {
        output.broadcast(value.getToken());
    }

    /** Return the type constraint that the output type must be
     *  greater than or equal to the type of the token in the value parameter.
     *  @return An enumeration of inequality type constraints.
     */
    public Enumeration typeConstraints() {
        // FIXME: When there is better infrastructure in the type system,
        // replace this with a simpler form.
	LinkedList result = new LinkedList();
	Class paramType = value.getToken().getClass();
        Inequality ineq = new Inequality(new TypeConstant(paramType),
                output.getTypeTerm());
	result.insertLast(ineq);
	return result.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Token _initToken = new Token();
}

