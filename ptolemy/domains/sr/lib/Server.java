/* A server that serves internally stored data to a client.

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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Server
/**
Output to the <i>output</i> port the value in the array specified by the 
<i>data</i> parameter at the index specified by the <i>input</i> parameter.  
All ports are single ports.  The index must be an integer.  If the index is 
out of range, no token is output.

@author Paul Whitaker
@version $Id$
*/

public class Server extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Server(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Set parameters.
        data = new Parameter(this, "index");
        data.setExpression("{1, 2, 4, 8, 16}");

	// set type constraints.
        input.setTypeEquals(BaseType.INT);
	data.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
	ArrayType dataType = (ArrayType)data.getType();
	InequalityTerm elemTerm = dataType.getElementTypeTerm();
	output.setTypeAtLeast(elemTerm);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The data array.
     */
    public Parameter data;

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
        Server newObject = (Server)(super.clone(workspace));

        // set the type constraints
        ArrayType dataType = (ArrayType)data.getType();
        InequalityTerm elemTerm = dataType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elemTerm);

        return newObject;
    }

    /** Consume at most one token from the input port and produce
     *  the element at the index specified by this token from the 
     *  data array on the output port.  If there is no token
     *  on the input or of the token is out of range, then no output 
     *  is produced.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if(input.hasToken(0)) {
            ArrayToken token = (ArrayToken)data.getToken();
            int indexValue = ((IntToken)input.get(0)).intValue();
            if (indexValue >= 0 && indexValue < token.length()) {
                output.broadcast(token.getElement(indexValue));
            }
        }
    }
}

