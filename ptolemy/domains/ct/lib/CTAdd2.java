/* A multi-input one output Add2er (IO type: doule)

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// CTAdd2
/**
An multi-input one output double precision adder (IO type: double)
In the fire() phase, it adds all the input token and emit the sum as
the output.
FIXME: Remove this class...
@author Jie Liu
@version $Id$
*/
public class CTAdd2 extends TypedAtomicActor {
    /** Construct the adder. The adder is not dynamic.
     * @param container The CTSubSystem this adder belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
    */
    public CTAdd2(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input1 = new TypedIOPort(this, "input1");
        input1.setInput(true);
        input1.setOutput(false);
        input1.setTypeEquals(DoubleToken.class);

        input2 = new TypedIOPort(this, "input2");
        input2.setInput(true);
        input2.setOutput(false);
        input2.setTypeEquals(DoubleToken.class);

        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the input tokens, and output the sum.
     *  @exception IllegalActionException If there's no enough input
     *       tokens.
     */
    public void fire() throws IllegalActionException {
        double sum = 0.0;
        
        sum += ((DoubleToken)input1.get(0)).doubleValue();
        sum += ((DoubleToken)input2.get(0)).doubleValue();

        output.broadcast(new DoubleToken(sum));
    }

    /** The multi-input port.
     */
    public TypedIOPort input1;
    public TypedIOPort input2;

    /** The singal output port.
     */
    public TypedIOPort output;
}
