/* Convert a FixToken into a DoubleToken.

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

@ProposedRating Yellow (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu) */

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.FixToken;   // for javadoc
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.math.Precision;
import ptolemy.math.Quantizer;
import ptolemy.math.FixPoint;

//////////////////////////////////////////////////////////////////////////
//// FixToDouble
/**
This actor converts a FixToken into a DoubleToken. This conversion is
explicitly provided because there is no lossless conversion
between these two types in the type lattice, and thus the type system
does not do this conversion automatically.  Although the conversion
is in fact usually lossless, a FixToken can, in principle, have
arbitrary precision, and thus can exceed the capabilities of a double.

@author Bart Kienhuis and Edward A. Lee
@version $Id$
@see ptolemy.data.FixToken
*/

public class FixToDouble extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixToDouble(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.FIX);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input the convert it to double
     *  precision. If there is no input available, then no output
     *  is produced.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	if (input.hasToken(0)) {
    	    FixToken in = (FixToken)input.get(0);
            FixPoint value = in.fixValue();
	    output.send(0, new DoubleToken(value.doubleValue()));
        }
    }
}
