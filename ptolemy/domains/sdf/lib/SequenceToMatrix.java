/* Bundle a sequence of N by M input tokens into a matrix.

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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.UnsizedMatrixType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// SequenceToMatrix
/**
This actor bundles a specified number of input tokens into a matrix.
On each firing, it reads <i>rows</i> times <i>columns</i> input tokens
and writes one output matrix token with the specified number of rows
and columns.
This actor is polymorphic. It can accept inputs of any scalar type
that has a corresponding matrix type.
<p>

@author Edward Lee
@version $Id$
@since Ptolemy II 0.4
*/

public class SequenceToMatrix extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// default tokenConsumptionRate is 1.
	input.setTokenConsumptionRate(1);

	// tokenProductionRate is 1.
	output.setTokenProductionRate(1);

        output.setTypeAtLeast(new FunctionTerm(input));

        // Set parameters.
        rows = new Parameter(this, "rows");
        rows.setExpression("1");
        columns = new Parameter(this, "columns");
        columns.setExpression("1");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of rows in the output.  This is an integer that defaults
     *  to 1.
     */
    public Parameter rows;

    /** The number of columns in the output.  This is an integer that defaults
     *  to 1.
     */
    public Parameter columns;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>rows</i> or <i>columns</i>, then
     *  set the consumption rate of the input port, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rows || attribute == columns) {
            int rowsValue = ((IntToken)rows.getToken()).intValue();
            int columnsValue = ((IntToken)columns.getToken()).intValue();
            if (rowsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of rows: " + rowsValue);
            }
            if (columnsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of columns: " + columnsValue);
            }
            int rate = rowsValue * columnsValue;
            input.setTokenConsumptionRate(rate);
            Director director = getDirector();
            if (director != null) {
                director.invalidateSchedule();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SequenceToMatrix newObject = (SequenceToMatrix)super.clone(workspace);
        newObject.output.setTypeAtLeast(new FunctionTerm(newObject.input));
        return newObject;
    }

    /** Consume the inputs and produce the output matrix.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int rowsValue = ((IntToken)rows.getToken()).intValue();
        int columnsValue = ((IntToken)columns.getToken()).intValue();
        int length = rowsValue * columnsValue;
	Token[] valueArray = input.get(0, length);

        // Use reflection, to avoid having to have a case statement
        // on the type.
        Class outputTokenClass
                = ((UnsizedMatrixType)output.getType()).getTokenClass();
        Class[] argumentTypes = new Class[3];
        argumentTypes[0] = Token[].class;
        argumentTypes[1] = int.class;
        argumentTypes[2] = int.class;
        try {
            Constructor constructor
                    = outputTokenClass.getConstructor(argumentTypes);
            Object[] arguments = new Object[3];
            arguments[0] = valueArray;
            arguments[1] = new Integer(rowsValue);
            arguments[2] = new Integer(columnsValue);
            Token outputToken = (Token)constructor.newInstance(arguments);
            output.send(0, outputToken);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
            "Cannot find a suitable output matrix type.");
        }
    }

    /** Return true if the input port has enough tokens for this actor to
     *  fire. The number of tokens required is the product of the
     *  values of the <i>rows</i> and <i>columns</i> parameters.
     *  @return boolean True if there are enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    public boolean prefire() throws IllegalActionException {
        int rowsValue = ((IntToken)rows.getToken()).intValue();
        int columnsValue = ((IntToken)columns.getToken()).intValue();
        int length = rowsValue * columnsValue;
        if (!input.hasToken(0, length)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the input port
    // type. The result of the function is a matrix type with elements
    // that are the same as the input type. If there is no such matrix
    // type, then the result is unknown.
    // NOTE: This is largely copied from AbsoluteValue.  Should
    // there be a common base class?
    private class FunctionTerm implements InequalityTerm {

        // The constructor takes a port argument so that the clone()
        // method can construct an instance of this class for the
        // input port on the clone.
        private FunctionTerm(TypedIOPort port) {
            _port = port;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return null.
         *  @return null.
         */
        public Object getAssociatedObject() {
            return null;
        }

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type inputType = _port.getType();
            try {
                return UnsizedMatrixType.getMatrixTypeForElementType(inputType);
            } catch (IllegalActionException ex) {
                return BaseType.UNKNOWN;
            }
        }

        /** Return the variables in this term. If the type of the input port
         *  is a variable, return a one element array containing the
         *  InequalityTerm of that port; otherwise, return an array of zero
         *  length.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (_port.getTypeTerm().isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = _port.getTypeTerm();
                return variable;
            } else {
                return new InequalityTerm[0];
            }
        }

        /** Throw an Exception.
         *  @exception IllegalActionException If we call initialize on
         *  a function term.  Always thrown in this class.
         */
        public void initialize(Object e)
                throws IllegalActionException {
            throw new IllegalActionException("SequenceToMatrix$FunctionTerm." +
                    "initialize: Cannot initialize a function term.");
        }

        /** Return false.
         *  @return false.
         */
        public boolean isSettable() {
            return false;
        }

        /** Return true.
         *  @return True.
         */
        public boolean isValueAcceptable() {
            return true;
        }

        /** Throw an Exception.
         *  @exception IllegalActionException If the type is not settable.
         *  Always thrown in this class.
         */
        public void setValue(Object e)
                throws IllegalActionException {
            throw new IllegalActionException(
                    "SequenceToMatrix$FunctionTerm.setValue: The type is not " +
                    "settable.");
        }

        /** Override the base class to give a description of this term.
         *  @return A description of this term.
         */
        public String toString() {
            return "(SequenceToMatrix$FunctionTerm, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private TypedIOPort _port;
    }
}
