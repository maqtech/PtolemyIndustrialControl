/* An FIR lattice filter.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Lattice
/**
An FIR filter with a lattice structure.  The coefficients of such a
filter are called "reflection coefficients."  Lattice filters are
typically used as linear predictors because it is easy to ensure that
they are minimum phase, and hence that their inverse is stable.
A lattice filter is (strictly) minimum phase if its reflection
coefficients are all less than unity in magnitude.  To get the
reflection coefficients for a linear predictor for a particular
random process, you can use the LevinsonDurbin actor.
The inputs and outputs are of type double.
<p>
The default reflection coefficients correspond to the optimal linear
predictor for an AR process generated by filtering white noise with
the following filter:
<pre>
                           1
H(z) =  --------------------------------------
        1 - 2z<sup>-1</sup> + 1.91z<sup>-2</sup> - 0.91z<sup>-3</sup> + 0.205z<sup>-4</sup>
</pre>
<p>
Since this filter is minimum phase, the transfer function of the lattice
filter is <i>H <sup>-</i>1<i></sup> </i>(<i>z</i>).
<p>
Note that the definition of reflection coefficients is not quite universal
in the literature. The reflection coefficients in reference [2]
are the negative of the ones used by this actor, which
correspond to the definition in most other texts,
and to the definition of partial-correlation (PARCOR)
coefficients in the statistics literature.
<p>
The signs of the coefficients used in this actor are appropriate for values
given by the LevinsonDurbin actor.
The structure of the filter is as follows:
<pre>
     y[0]              y[1]               y[n-1]           y[n]
X(n) -------o--&gt;-(+)--&gt;----o--&gt;-(+)--&gt;-- ... -&gt;---o--&gt;-(+)------&gt;  Y(n)
     |       \   /          \   /                  \   /        
     |      +K1 /          +K2 /                  +Kn /         
     |         X              X                      X          
     V      -K1 \          -K2 \                  -Kn \         
     |       /   \          /   \                  /   \        
     \-[z]--o--&gt;-(+)-[z]---o--&gt;-(+)-[z]- ... -&gt;---o--&gt;-(+)
          w[0]         w[1]               w[n-1]           w[n]

</pre>
<p>
<b>References</b>
<p>[1]
J. Makhoul, "Linear Prediction: A Tutorial Review",
<i>Proc. IEEE</i>, Vol. 63, pp. 561-580, Apr. 1975.
<p>[2]
S. M. Kay, <i>Modern Spectral Estimation: Theory & Application</i>,
Prentice-Hall, Englewood Cliffs, NJ, 1988.

@see ptolemy.domains.sdf.lib.FIR
@see LevinsonDurbin
@see RecursiveLattice
@see ptolemy.domains.sdf.lib.VariableLattice
@author Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 1.0
*/

public class Lattice extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Lattice(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        reflectionCoefficients = new Parameter(this, "reflectionCoefficients");
        // Note that setExpression() will call attributeChanged().
        reflectionCoefficients.setExpression(
                "{0.804534, -0.820577, 0.521934, -0.205}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The reflection coefficients.  This is an array of doubles with
     *  default value {0.804534, -0.820577, 0.521934, -0.205}. These
     *  are the reflection coefficients for the linear predictor of a
     *  particular random process.
     */
    public Parameter reflectionCoefficients;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>reflectionCoefficients</i> parameter,
     *  then reallocate the arrays to use.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == reflectionCoefficients) {
            ArrayToken value = (ArrayToken)reflectionCoefficients.getToken();
            _order = value.length();
            if (_backward == null || _order != _backward.length) {
                // Need to reallocate the arrays.
                _reallocate();
            }
            for (int i = 0; i < _order; i++) {
                _reflectionCoefficients[i] =
                    ((DoubleToken)value.getElement(i)).doubleValue();
            }        
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume one input token, if there is one, and produce one output
     *  token.  If there is no input, the produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            System.arraycopy(_backward, 0,
                    _backwardCache, 0,
                    _order + 1);
            System.arraycopy(_forward, 0,
                    _forwardCache, 0,
                    _order + 1);
            DoubleToken in = (DoubleToken)input.get(0);
       
            _forwardCache[0] = in.doubleValue();   // _forwardCache(0) = x(n)
    
            _doFilter();
          
            _backwardCache[0] = _forwardCache[0];   // _backwardCache[0] = x[n]

            // Send the forward residual.
            output.broadcast(new DoubleToken(_forwardCache[_order]));
        }
    }

    /** Initialize the state of the filter.
     */
    public void initialize() throws IllegalActionException {
        for(int i = 0; i < _order + 1; i ++) {
            _forward[i] = 0;
            _backward[i] = 0;
        }
    }

    /** Update the backward and forward prediction errors that
     *  were generated in fire() method.
     *  @return False if the number of iterations matches the number requested.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        System.arraycopy(_backwardCache, 0,
                _backward, 0,
                _order + 1);
        System.arraycopy(_forwardCache, 0,
                _forward, 0,
                _order + 1);
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    // Compute the filter, updating the caches, based on the current
    // values.
    protected void _doFilter() throws IllegalActionException {
        double k;
        // NOTE: The following code is ported from Ptolemy Classic.
        // Update forward errors.
        for (int i = 0; i < _order; i++) {
            k = _reflectionCoefficients[i];
            _forwardCache[i+1] = -k * _backwardCache[i] + _forwardCache[i];
        }
               
        // Backward: Compute the weights for the next round Note:
        // strictly speaking, _backwardCache[_order] is not necessary
        // for computing the output.  It is computed for the use of
        // subclasses which adapt the reflection coefficients.
        for (int i = _order; i > 0 ; i--) {
            k = _reflectionCoefficients[i-1];
            _backwardCache[i] = -k * _forwardCache[i-1]
                + _backwardCache[i-1];
        }
    }

    // Reallocate the internal arrays.
    protected void _reallocate() {
        _backward = new double[_order + 1];
        _backwardCache = new double[_order + 1];
        _forward = new double[_order + 1];
        _forwardCache = new double[_order + 1];
        _reflectionCoefficients = new double[_order];
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The order of the filter (i.e. the number of reflection coefficients)
    protected int _order = 0;

    // Backward prediction errors.  The length is _order.
    protected double[] _backward = null;

    // Cache of backward prediction errors.
    // The fire() method updates _forwardCache and postfire()
    // copies _forwardCache to _forward so this actor will work in domains
    // like SR.  The length is _order.
    protected double[] _backwardCache = null;

    // Forward prediction errors.  The length is _order + 1.
    protected double[] _forward = null;

    // Cache of forward prediction errors.
    // The fire() method updates _forwardCache and postfire()
    // copies _forwardCache to _forward so this actor will work in domains
    // like SR.  The length is _order + 1.
    protected double[] _forwardCache = null;

    // Cache of reflection coefficients.   The length is _order.
    protected double[] _reflectionCoefficients = null;
}
