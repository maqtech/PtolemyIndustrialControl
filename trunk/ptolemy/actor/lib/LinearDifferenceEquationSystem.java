/* Linear Difference Equation System.

@Copyright (c) 1998-2001 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/
package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LinearDifferenceEquationSystem
/**
The linear state-space model implements a system whose behavior is defined by:
<pre>
    x(k+1) = Ax(k) + Bu(k)
      y(k) = Cx(k) + Du(k)
      x(0) = x0
</pre>

where x is the state vector, u is the input vector, and y is the
output vector. (Note that in Ptolemy II, vectors are double matrices
with one column or one row.) The matrix coefficients must have the
following characteristics:

<pre>
A must be an n-by-n matrix, where n is the number of states.
B must be an n-by-m matrix, where m is the number of inputs.
C must be an r-by-n matrix, where r is the number of outputs.
D must be an r-by-m matrix.
</pre>

For each firing, the actor accepts one input DoubleMatrixToken of
dimension <i>m</i> x 1, and generates one output DoubleMatrixToken of
dimension <i>r</i> x 1.

<P>
In addition to produce the output <i>y</i> though port <i>output</i>, the
actor also produce the state values <i>x</i> though port <i>state</i>.

@author Jie Liu
@version $Id$
*/

public class LinearDifferenceEquationSystem extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LinearDifferenceEquationSystem(CompositeEntity container,
            String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(false);
        output.setMultiport(false);
        state = new TypedIOPort(this, "state", false, true);

        double[][] one = {{1.0}};
        double[][] zero = {{0.0}};

        A = new Parameter(this, "A", new DoubleMatrixToken(one));
        A.setTypeEquals(BaseType.DOUBLE_MATRIX);

        B = new Parameter(this, "B", new DoubleMatrixToken(one));
        B.setTypeEquals(BaseType.DOUBLE_MATRIX);

        C = new Parameter(this, "C", new DoubleMatrixToken(one));
        C.setTypeEquals(BaseType.DOUBLE_MATRIX);

        D = new Parameter(this, "D", new DoubleMatrixToken(zero));
        D.setTypeEquals(BaseType.DOUBLE_MATRIX);

        initialStates = new Parameter(this, "initialStates",
                new DoubleMatrixToken(zero));
        initialStates.setTypeEquals(BaseType.DOUBLE_MATRIX);

        _x = new DoubleMatrixToken(zero);
        _initialStateChanged = true;
        // icon
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" "
                + "width=\"150\" height=\"60\" "
                + "style=\"fill:white\"/>\n"
                + "<text x=\"5\" y=\"20\" "
                + "style=\"font-size:14\">\n"
                + "x(k+1) = Ax(k) + Bu(k) "
                + "</text>\n"
                + "<text x=\"5\" y=\"40\" "
                + "style=\"font-size:14\">\n"
                + "    y(k) = Cx(k) + Du(k)"
                + "</text>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output port that produces DoubleMatrixToken.
     */
    public TypedIOPort state;

    /** The A matrix in the state-space representation. It must be a
     *  square matrix.
     *  The default value is [[1.0]].
     */
    public Parameter A;

    /** The B matrix in the state-space representation. The number of
     *  rows must equal to the number of rows of the A matrix. The number
     *  of columns must equal to the number of roles in the input token.
     *  The default value is [[1.0]].
     */
    public Parameter B;

    /** The C matrix in the state-space representation. The number of
     *  columns must equal to the number of columns of the A matrix.
     *  The number of rows must equal to the number of columns in the
     *  output token. The default value is [[0.0]].
     */
    public Parameter C;

    /** The D matrix in the state-space representation. The number of
     *  columns must equal to the number of rows in the input token.
     *  And, the number of rows must equal to the number of columns in
     *  the output token.
     *  The default value is [[0.0]].
     */
    public Parameter D;

    /** The initial condition for the state variables. This must be
     *  a column vector (double matrix with only one column) whose
     *  length equals to the number of state variables.
     *  The default value is [0.0].
     *  NOTE: Changes of the initialStates will be applied at the
     *  next time when fire() is called.
     */
    public Parameter initialStates;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>A, B, C, D</i> or <i>initialState</i>
     *  parameters, check that they are indeed matrices and vectors,
     *  and request for initialization from the director if there is one.
     *  Other sanity checks like the dimensions of the matrices will
     *  be done in the preinitialize() method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the numerator and the
     *   denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if(attribute == A) {
            // Check that it is a square matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)A.getToken();
            if(token.getRowCount() == 0 || token.getColumnCount() == 0 ||
                    token.getRowCount() != token.getColumnCount()) {
                throw new IllegalActionException(this,
                        "The A matrix must be a nonempty square matrix.");
            }
        } else if(attribute == B) {
            // Check that B is a matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)B.getToken();
            if(token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The B matrix must be a nonempty matrix.");
            }
        } else if(attribute == C) {
            // Check that C is a matrix.
            DoubleMatrixToken token = (DoubleMatrixToken)C.getToken();
            if(token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The C matrix must be a nonempty matrix.");
            }
        } else if(attribute == D) {
            DoubleMatrixToken token = (DoubleMatrixToken)D.getToken();
            if(token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this,
                        "The D matrix must be a nonempty matrix.");
            }
        } else if(attribute == initialStates) {
            // The initialStates parameter should be a row vector.
            DoubleMatrixToken token =
                (DoubleMatrixToken)initialStates.getToken();
            if(token.getColumnCount() != 1 || token.getRowCount() < 1) {
                throw new IllegalActionException(this,
                        "The initialStates must be a column vector.");
            }
            _initialStateChanged = true;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the input token, compute the system response, and
     *  produces outputs. Notice that
     *  the state is updated in postfire. That is, if multiple fire()
     *  are called before a calling of postfire, this actor will use
     *  the same internal state to compute the outputs.
     *  @exception IllegalActionException If the get() or send() methods
     *   of the ports throw it.
     */
    public void fire() throws IllegalActionException {
        if(input.hasToken(0)) {
            Token u = input.get(0);
            Token y = C.getToken().multiply(_x).add(D.getToken().multiply(u));
            _xPrime = A.getToken().multiply(_x).add(B.getToken().multiply(u));
            if (_singleOutput) {
                output.send(0, ((DoubleMatrixToken)y).getElementAsToken(0, 0));
            } else {
                output.send(0, y);
            }
            if (_singleState) {
                state.send(0, ((DoubleMatrixToken)_x).getElementAsToken(0, 0));
            } else {
                state.send(0, _x);
            }
        }
    }

    /** Update the internal state.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException {
        if(super.postfire()) {
            _x = _xPrime;
            return true;
        } else {
            return false;
        }
    }

    /** If the parameter <i>initialStates</i> has been changed during the
     *  laster iteration, then update the internal state to be the
     *  new set value.
     *
     *  @exception IllegalActionException If <i>initialStates</i> parameter
     *   is invalid, or if the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        if(_initialStateChanged) {
            _x = initialStates.getToken();
            _initialStateChanged = false;
        }
        if(input.hasToken(0)) {
            return true;
        } else {
            return false;
        }
    }

    /** Check the dimension of all parameters. If the system needs
     *  multiple inputs, then set the input type to be DoubleMatrix.
     *  Otherwise set the input type to Double. Similar for output
     *  types.
     *  @exception IllegalActionException If the dimensions do not
     *  match.
     */
    public void preinitialize() throws IllegalActionException {
        super.initialize();
        DoubleMatrixToken a = (DoubleMatrixToken)A.getToken();
        int n = a.getRowCount();
        DoubleMatrixToken b = (DoubleMatrixToken)B.getToken();
        if (b.getRowCount() != n) {
            throw new IllegalActionException(this,
                    "The number of rows of the B matrix should equal to "
                    + "the number of rows of the A matrix.");
        }
        if (n == 1) {
            _singleState = true;
            state.setTypeEquals(BaseType.DOUBLE);
        } else {
            _singleState = false;
            state.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        int m = b.getColumnCount();
        if (m == 1) {
            input.setTypeEquals(BaseType.DOUBLE);
        } else {
            input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        DoubleMatrixToken c = (DoubleMatrixToken)C.getToken();
        if (c.getColumnCount() != n) {
            throw new IllegalActionException(this,
                    "The number of columns of the C matrix should equal to "
                    + "the number of rows of the A matrix.");
        }
        int r = c.getRowCount();
        if (r == 1) {
            _singleOutput = true;
            output.setTypeEquals(BaseType.DOUBLE);
        } else {
            _singleOutput = false;
            output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        DoubleMatrixToken d = (DoubleMatrixToken)D.getToken();
        if (c.getRowCount() != d.getRowCount()) {
            throw new IllegalActionException(this,
                    "The number of rows of the D matrix should equal to "
                    + "the number of rows of the C matrix.");
        }
        DoubleMatrixToken x0 =
            (DoubleMatrixToken)initialStates.getToken();
        if (x0.getRowCount() != n) {
            throw new IllegalActionException(this,
                    "The number of initial states should equal to "
                    + "the number of columns of the A matrix.");
        }
        // reset initial state.
         _initialStateChanged = true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The internal state.
    private Token _x;

    // The next state.
    private Token _xPrime;

    // Indicate whether the initial state has beed set.
    private boolean _initialStateChanged;

    // Indicate whether the output is a scalar.
    private boolean _singleOutput;

    // Indicate whether the state variable is a scalar;
    private boolean _singleState;

}


