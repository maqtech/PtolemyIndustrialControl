/** An actor that outputs the fixpoint value of the concatenation of
 the input bits.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.apps.vhdl;

import java.math.BigDecimal;
import java.math.BigInteger;

import ptolemy.data.FixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

//////////////////////////////////////////////////////////////////////////
//// Concat

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the concatenation of the input bits from each input channel. 
 The ordering of channels determines the order of the concatenation; inputs
 from later channels are appended to the end. The input can have any scalar
 type. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Concat extends SynchronousFixPointTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Concat(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the fixpoint value of the concatenation of the input bits. 
     *  If there is no inputs, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int binaryPointValue = ((IntToken) binaryPoint.getToken()).intValue();
        String bits = "";

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                FixToken in = (FixToken) input.get(i);
                bits += in.fixValue().toBitString();
            }
        }

        //FIXME: what do we do if input is negative?
        //bits = bits.replace('-', '1');

        FixPoint result = new FixPoint(new BigDecimal(new BigInteger(bits, 2)),
                new FixPointQuantization(new Precision(0, bits.length(),
                        binaryPointValue), Overflow.GROW, Rounding.HALF_EVEN));

        output.send(0, new FixToken(result));
    }
}
