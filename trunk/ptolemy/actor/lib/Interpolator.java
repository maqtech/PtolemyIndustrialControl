/* An interpolator for a specified array of indexes and values.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.math.Interpolation;

//////////////////////////////////////////////////////////////////////////
//// Interpolator
/**
Produce an interpolation based on the parameters.
This class uses the Interpolation class in the math package to compute
the interpolation.
The <i>values</i> parameter specifies a sequence of values
to produce at the output.  The <i>indexes</i> parameter specifies
when those values should be produced.
The values and indexes parameters must both contain
arrays, and have equal lengths or an exception will be thrown.
The <i>indexes</i> array must be increasing and non-negative.
The values are periodic if the <i>period</i> parameter contains a
positive value. In this case, the period must be greater than the
largest index, and values within the index range 0 to (period-1) are
repeated indefinitely.  If the period is zero, the values are not
periodic, and the values outside the range of the indexes are
considered to be 0.0.  The <i>order</i> parameter
specifies which order of interpolation to apply  whenever the
iteration count does not match an index in <i>indexes</i>.
The Interpolation class currently supports zero, first, and third
order interpolations. The default parameter are those set in the
Interpolation class.
<p>
This actor counts iterations.  Whenever the iteration count matches an entry
in the <i>indexes</i> array, the corresponding entry (at the same position)
in the <i>values</i> array is produced at the output.  Whenever the iteration
count does not match a value in the <i>indexes</i> array, an interpolation
of the values is produced at the output.
<p>
Output type is DoubleToken.

@author Sarah Packman, Yuhong Xiong
@version $Id$
@since Ptolemy II 0.3
@see ptolemy.math.Interpolation
*/

public class Interpolator extends SequenceSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Interpolator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        _interpolation = new Interpolation();

        // Initialize the parameters with the default settings of the
        // Interpolation class. This is not required for this class to
        // function. But since these parameters are public, other objects
        // in the system may use them.

        indexes = new Parameter(this, "indexes");
        indexes.setExpression("{0, 1}");
        // Call this so that we don't have to copy its code here...
        attributeChanged(indexes);

        // set values parameter
        values = new Parameter(this, "values");
        values.setExpression("{1.0, 0.0}");
        values.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        int defOrder = _interpolation.getOrder();
        IntToken defOrderToken = new IntToken(defOrder);
        order = new Parameter(this, "order", defOrderToken);
        order.setTypeEquals(BaseType.INT);

        int defPeriod = _interpolation.getPeriod();
        IntToken defPeriodToken = new IntToken(defPeriod);
        period = new Parameter(this, "period", defPeriodToken);
        period.setTypeEquals(BaseType.INT);

        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The indexes at which the specified values will be produced.
     *  This parameter is an array of integers, with default value {0, 1}.
     */
    public Parameter indexes;

    /** The order of interpolation for non-index iterations.
     *  This parameter must contain an IntToken.
     */
    public Parameter order;

    /** The period of the reference values.
     *  This parameter must contain an IntToken.
     */
    public Parameter period;

    /** The values that will be produced at the specified indexes.
     *  This parameter is an array, with default value {1.0, 0.0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the validity of the parameter.
     *  @exception IllegalActionException If the argument is the
     *   <i>values</i> parameter and it does not contain an one dimensional
     *   array; or the argument is the <i>indexes</i> parameter and it does
     *   not contain an one dimensional array or is not increasing and
     *   non-negative; or the argument is the <i>period</i> parameter and is
     *   negative; or the argument is the <i>order</i> parameter and the order
     *   is not supported by the Interpolation class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {
            ArrayToken valuesValue = (ArrayToken)values.getToken();
            _values = new double[valuesValue.length()];
            double previous = 0.0;
            for (int i = 0; i < valuesValue.length(); i++) {
                _values[i] = ((DoubleToken)valuesValue.getElement(i))
                    .doubleValue();
                previous = _values[i];
            }
            _interpolation.setValues(_values);
        } else if (attribute == indexes) {
            ArrayToken indexesValue = (ArrayToken)indexes.getToken();
            _indexes = new int[indexesValue.length()];
            int previous = 0;
            for (int i = 0; i < indexesValue.length(); i++) {
                _indexes[i] = ((IntToken)indexesValue.getElement(i)).intValue();
                // Check nondecreasing property.
                if (_indexes[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of indexes is not nondecreasing " +
                            "and nonnegative.");
                }
                previous = _indexes[i];
            }
            _interpolation.setIndexes(_indexes);

        } else if (attribute == period) {
            int newPeriod = ((IntToken)period.getToken()).intValue();
            _interpolation.setPeriod(newPeriod);
        } else if (attribute == order) {
            int newOrder = ((IntToken)order.getToken()).intValue();
            _interpolation.setOrder(newOrder);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output the value at the current iteration count. The output is
     *  one of the reference values if the iteration count matches one
     *  of the indexes, or is interpolated otherwise.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>indexes</i> parameters do not contain arrays of the same length,
     *   or the period is not 0 and not greater than the largest index.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        double result = _interpolation.interpolate(_iterationCount);
        output.send(0, new DoubleToken(result));
    }

    /** Set the iteration count to zero.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
    }

    /** Update the iteration counter, then call the super class method.
     *  @return A boolean returned by the super class method.
     */
    public boolean postfire() throws IllegalActionException {
        ++_iterationCount;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cache of indexes array value.
    private transient int[] _indexes;

    private int _iterationCount = 0;
    private Interpolation _interpolation;

    // Cache of values array value.
    private transient double[] _values;
}
