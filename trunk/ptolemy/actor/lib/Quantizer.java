/* An actor that outputs a quantization version of the input.

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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Quantizer
/**
Produce an output token on each firing with a value that is
the quantization version of the input.  The actor is NOT type
polymorphic in that it only support DoubleToken input and
DoubleToken output.
Note: This may be changed in the future.
<P>
The <i>levels</i> parameter contains a DoubleMatrixToken,
which controls the quantization levels. The DoubleMatrix can
only has one row, and the elements in this row should be in
an increasing order, otherwise an exception will be thrown.
The default value of levels is [-1.0, 1.0].
<P>
Suppose u is the input, and levels = [a, b, c], a<b<c,
then the output of the actor will be:<BR>
y = a, for u <= (b+a)/2;<BR>
y = b, for (b+a)/2 < u <= (c+b)/2;<BR>
y = c, for u > (c+b)/2;<BR>
We do not require the quantization intervals to be equal,
i.e. we allow that (c-b) != (b-a).

@author Jie Liu
@version $Id$
*/

public class Quantizer extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Quantizer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        double defaultLevels[][] = {{-1.0, 1.0}};
        levels = new Parameter(this, "levels",
                new DoubleMatrixToken(defaultLevels));
        // Call this so that we don't have to copy its code here...
        attributeChanged(levels);

	// Set the type constraints.
	input.setTypeEquals(DoubleToken.class);
	output.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The quantization levels.
     *  The default value of this parameter is a row vector {-1.0, 1.0}.
     */
    public Parameter levels;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the levels parameter, check that the array
     *  is increasing and has the right dimension.  Recompute the
     *  quantization thresholds.
     *  @exception IllegalActionException If the levels array is not
     *   nondecreasing and nonnegative, or it is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == levels) {
            // Check nondecreasing property.
            double[][] lvls =
                ((DoubleMatrixToken)levels.getToken()).doubleMatrix();
            if (lvls.length != 1 || lvls[0].length == 0) {
                throw new IllegalActionException(this,
                        "Value of levels is not a row vector.");
            }
            double previous = -java.lang.Double.MAX_VALUE;
            int length = lvls[0].length;
            for (int j = 0; j < length; j++) {
                if (lvls[0][j] <= previous) {
                    throw new IllegalActionException(this,
                            "Value of levels is not increasing ");
                }
                previous = lvls[0][j];
            }
            // Compute the quantization thresholds.
            _thresholds = new double[length-1];
            for (int j = 0; j < length-1; j++) {
                _thresholds[j] = (lvls[0][j+1]+lvls[0][j])/2.0;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Quantizer newobj = (Quantizer)super.clone(ws);
        try {
            newobj.levels = (Parameter)newobj.getAttribute("levels");
            newobj.attributeChanged(newobj.levels);
            newobj.input.setTypeEquals(DoubleToken.class);
            newobj.output.setTypeEquals(DoubleToken.class);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return newobj;
    }
    
    /** Output the quantization of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            double in = ((DoubleToken)input.get(0)).doubleValue();
            int index = _getQuantizationIndex(in);
            output.broadcast(((DoubleMatrixToken)levels.getToken()).
                    getElementAsToken(0, index));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Compute the quantization index for the given input value.
     * @parameter in The input value
     * @return The quantization index.
     */
    private int _getQuantizationIndex(double in) {
        int index = _thresholds.length;
        for (int i = 0; i < _thresholds.length; i++) {
            if (in <= _thresholds[i]) {
                index = i;
                break;
            }
        }
        return index;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The thresholds for quantization.
    private double[] _thresholds;
}
