/* Decode convolutional code with non-antipodal constellation.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.actor.TypeAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// ViterbiDecoder
/**
The TrellisDecoder is a generalization of the ViterbiDecoder. It
can handle trellis coding, which has non-antipodal constellation. 
For a <i>k</i>/<i>n</i> convolutional code, the constellation
should map each codeword into a complex number. Hence the length
of the constellation should be a complex array of length
2<i><sup>n</sup></i>. For example, a 1/2 rate convolutional code
should use 4PSK. a <i>k</i>/3 convolutional code should use 8PSK.
<p>
The input port of the TrellisDecoder is complex. On each firing, 
the TrellisDecoder reads one input. The Euclidean distance is defined
as the distance between the noisy input and the point in the 
constellation mapped from the codeword. Like in ViterbiDecoder, 
this actor produces <i>k</i> outputs on each firing.
<p>
See ConvolutionalCoder and ViterbiDecoder for details about
the meaning of these parameters.
<p>
For more information on convolutional codes, Viterbi decoder, and
trellis coding, see the ConvolutionalCoder actor, ViterbiDecoder
actor and Proakis, <i>Digital Communications</i>, Fourth Edition,
McGraw-Hill, 2001, pp. 471-477 and pp. 482-485,
or Barry, Lee and Messerschmitt, <i>Digital Communication</i>, Third Edition,
Kluwer, 2004.
<p>
@author Rachel Zhou, contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 3.0
*/
public class TrellisDecoder extends ViterbiDecoder {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TrellisDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        //uncodedRate = new Parameter(this, "uncodedRate");
        //uncodedRate.setTypeEquals(BaseType.INT);
        //uncodedRate.setExpression("1");
        
        //polynomialArray = new Parameter(this, "polynomialArray");
        //polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        //polynomialArray.setExpression("{05, 07}");
        
        //delay = new Parameter(this, "delay");
        //delay.setTypeEquals(BaseType.INT);
        //delay.setExpression("10");
        
        //softDecoding = new Parameter(this, "softDecoding");
        //softDecoding.setTypeEquals(BaseType.BOOLEAN);
        softDecoding.setVisibility(Settable.NONE);
        softDecoding.setExpression("false");
        
        trellisDecoding.setExpression("true");

        //constellation = new Parameter(this, "constellation");
        //constellation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        //constellation.setTypeAtLeast
        //    (new TypeConstant(new ArrayType(BaseType.DOUBLE)));

        constellation.setTypeEquals(new ArrayType(BaseType.COMPLEX));        
        constellation.setExpression("{1.0, i, -1.0, -i}");
        // Declare data types, consumption rate and production rate.
        //_type = new ptolemy.actor.TypeAttribute(input, "inputType");
        //_type.setExpression("complex");
        //_inputRate = new Parameter(input, "tokenConsumptionRate",
        //        new IntToken(1));
        //input.setTypeEquals(BaseType.COMPLEX);
        //output.setTypeEquals(BaseType.BOOLEAN);
        //_outputRate = new Parameter(output, "tokenProductionRate",
        //      new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An array of integers defining polynomials with
     *  binary coefficients. The coefficients indicate the presence (1)
     *  or absence (0) of a tap in the shift register. Each element
     *  of this array parameter should be a positive integer.
     *  The default value is {05, 07}.
     */
    //public Parameter polynomialArray;

    /** Integer defining the number of bits produced at the output
     *  in each firing. It should be a positive integer. Its
     *  default value is 1.
     */
    //public Parameter uncodedRate;

    /** Integer defining the trace back depth of the viterbi decoder.
     *  It should be a positive integer. Its default value is the
     *  integer 10.
     */
    //public Parameter delay;

    /** Boolean defining the decoding mode. If it is true, the decoder
     *  will do soft decoding, and the input data type will be double;
     *  otherwise it will do hard decoding, and the input data type will
     *  be boolean. The default value is true.
     */
    //public Parameter softDecoding;

    /** The constellation for soft decoding.  Inputs are expected to be
     *  symbols from this constellation with added noise.
     *  This parameter should be a double array of length 2. The first
     *  element defines the amplitude of "true" input. The second element
     *  defines the amplitude of "false" input.
     */
    //public Parameter constellation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>mode</i>, set input port
     *  type to be double if <i>mode</i> is true and set it to type boolean
     *  if it is false.
     *  If the attribute being changed is <i>uncodedRate</i> or
     *  <i>delay</i> then verify it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements
     *  is a positive integer.
     *  @exception IllegalActionException If <i>uncodedRate</i>,
     *  or <i>delay</i> is non-positive, or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    /*
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        //_inputRate.setToken(new IntToken(1));
        //ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
        //_maskNumber = maskToken.length();       
        /*        
        if (attribute == softDecoding) {
            _mode = ((BooleanToken)softDecoding.getToken()).booleanValue();
            // Set different input port types for soft and hard decoding.
            if (_mode) {
                _type.setExpression("double");
            } else {
                _type.setExpression("boolean");
            }
        } else if (attribute == uncodedRate) {
            _inputNumber = ((IntToken)uncodedRate.getToken()).intValue();
            if (_inputNumber < 1 ) {
                throw new IllegalActionException(this,
                        "inputLength must be non-negative.");
            }
            // Set a flag indicating the private variable
            // _inputNumber is invalid, but do not compute
            // the value until all parameters have been set.
            _inputNumberInvalid = true;
            // Set the input comsumption rate.
            _outputRate.setToken(new IntToken(_inputNumber));
        } else if (attribute == delay) {
            _depth = ((IntToken)delay.getToken()).intValue();
            if (_depth < 1) {
                throw new IllegalActionException(this,
                        "Delay must be a positive integer.");
            }
            _depthInvalid = true;
        } else if (attribute == polynomialArray) {
            ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
            _maskNumber = maskToken.length();
            _mask = new int[_maskNumber];
            _maxPolyValue = 0;
            for (int i = 0; i < _maskNumber; i++) {
                _mask[i] = ((IntToken)maskToken.getElement(i)).intValue();
                if (_mask[i] <= 0) {
                    throw new IllegalActionException(this,
                            "Polynomial is required to be strictly positive.");
                }
                // Find maximum value in integer of all polynomials.
                if (_mask[i] > _maxPolyValue) {
                    _maxPolyValue = _mask[i];
                }
            }
            _inputNumberInvalid = true;
            // Set the output production rate.
            _inputRate.setToken(new IntToken(_maskNumber));
        } else {
            super.attributeChanged(attribute);
        }
    }
*/
/*
   public double computeDistance(boolean mode,
           Token[] inputToken, int truthValue) {
       Complex[] y = new Complex[1];
       Complex truthComplex = _constellation[truthValue];
       Complex z = truthComplex.subtract(y[0]);
       return z.magnitude();
   }

    public void constellationValid(boolean mode)
            throws IllegalActionException {
        ArrayToken ampToken = ((ArrayToken)constellation.getToken());
        int length = ampToken.length();
        if (length != 1 << _maskNumber) {
            throw new IllegalActionException(this,
                "Invalid constellation for trellis decoding!");
        }
        _constellation = new Complex[length];
        for (int i = 0; i < ampToken.length(); i++) {
            _constellation[i] = 
                ((ComplexToken)ampToken.getElement(0)).complexValue();
        }
    }
*/
    /** Read <i>n</i> inputs and produce <i>k</i> outputs, where <i>n</i>
     *  is the number of integers in <i>polynomialArray</i> and <i>k</i>
     *  is the value of the <i>uncodedRate</i> parameter.  The outputs
     *  are a decoded bit sequence, with a prefix of <i>false</i>-valued
     *  tokens produced on the first <i>delay</i> firings.  The number
     *  of leading <i>false</i> outputs, therefore, is
     *  <i>delay</i>*<i>uncodedRate</i>.
     *  To decode, the actor searches iteratively of all possible
     *  input sequence and find the one that has the minimum distance
     *  to the observed inputs.
     */
    /*
    public void fire() throws IllegalActionException {

        constellationValid(_mode);
        super.fire();
       
        // If the private variable _inputNumberInvalid is true, verify
        // the validity of the parameters. If they are valid, compute
        // the state-transition table of this convolutional code, which
        // is stored in a 3-D array _truthTable[][][]. 
        /*
        if (_inputNumberInvalid) {
            if (_inputNumber >= _maskNumber) {
                throw new IllegalActionException(this,
                        "Output rate should be larger than input rate.");
            }

            //Comput the length of shift register.
            _shiftRegLength = 0;
            int regLength = 1;
            while (regLength <= _maxPolyValue) {
                //regLength = regLength << _inputNumber;
                //_shiftRegLength = _shiftRegLength + _inputNumber;
                regLength = regLength << 1;
                _shiftRegLength ++;
            }

            if (_inputNumber >= _shiftRegLength) {
                throw new IllegalActionException(this,
                        "The highest order of all polynomials is "
                        + "still too low.");
            }
            _inputNumberInvalid = false;

            // Compute the necessary dimensions for the truth table and
            // the length of buffers used to store possible input sequence.
            _rowNum = 1 << (_shiftRegLength - _inputNumber);
            _colNum = 1 << _inputNumber;
            _truthTable = new int[_rowNum][_colNum][3];
            _distance = new double[_rowNum];
            _tempDistance = new double[_rowNum];
            // Initialize the truth table and the buffer.
            for (int i = 0; i < _rowNum; i ++) {
                _distance[i] = 0;
                _tempDistance[i] = 0;
            }

            int inputMask = (1 << _inputNumber) - 1;
            // Compute the truth table.
            // _truthTable[m][n][1:3] has the following meanings:
            // "m" is the possible current state of the shift register.
            // It has 2<i>k</i> possible previous states, where "k"
            // is the <i>uncodedRate</i>.
            // Hence _truthTable[m][n][1:3] stores the truth values for
            // the n-th possible previous state for state "m".
            // _truthTable[m][n][1] is the "value" of the previous
            // shift register's states.
            // _truthTable[m][n][2] is the corresponding input block.
            // _truthTable[m][n][0] is the corresponding codewords
            // produced from the encoder.
            for (int state = 0; state < _rowNum; state ++) {
                for (int head = 0; head < _colNum; head ++) {
                    int reg = head << (_shiftRegLength - _inputNumber);
                    reg = reg + state;
                    int[] parity =  _calculateParity(_mask, _maskNumber, reg);
                    int outValue = 0;
                    // store the output values as an integer
                    // in the order of yn...y1y0
                    for (int i = _maskNumber - 1; i >= 0; i --) {
                        outValue = outValue << 1;
                        outValue = outValue + parity[i];
                    }
                    _truthTable[state][head][0] = outValue;
                    int oldState = reg >> _inputNumber;
                    _truthTable[state][head][1] = oldState;
                    int input = reg & inputMask;
                    _truthTable[state][head][2] = input;
                }
            }
        }

        if (_depthInvalid) {
            _path = new int[_rowNum][_depth + 1];
            _tempPath = new int[_rowNum][_depth + 1];
            for (int i = 0; i < _rowNum; i ++) {
                for (int j = 0; j < _depth; j ++) {
                    _path[i][j] = 0;
                    _tempPath[i][j] = 0;
                }
            }
            _depthInvalid = false;
        }

        // Read from the input port.
        Token[] inputToken = (Token[])input.get(0, _maskNumber);

        // Search the optimal path (minimum distance) for each state.
        for (int state = 0; state < _rowNum; state ++) {
            double minDistance = 0;
            int minInput = 0;
            int minState = 0;
            for (int colIndex = 0; colIndex < _colNum; colIndex ++) {
                // Compute the distance for each possible path to "state".
                double d = 0.0;
                d = computeDistance(_mode, inputToken, _truthTable[state][colIndex][0]);
                // The previous state for that possibility.
                int oldState = _truthTable[state][colIndex][1];
                d = _tempDistance[oldState] + d;
                // Find the minimum distance and corresponding previous
                // state for each possible current state of the shift register.
                if (colIndex == 0 || d < minDistance) {
                    minDistance = d;
                    minState = oldState;
                    minInput = _truthTable[state][colIndex][2];
                }
            }

            // update the buffers for minimum distance and its
            // corresponding possible input sequence.
            _distance[state] = minDistance;
            for (int i = 0; i < _flag; i ++) {
                _path[state][i] = _tempPath[minState][i];
            }
            _path[state][_flag] = minInput;

        }

        // Send all-false tokens for the first "D" firings.
        // If the waiting time has reached "D", the decoder starts to send
        // the decoded bits to the output port.
        if (_flag < _depth) {
            BooleanToken[] initialOutput = new BooleanToken[_inputNumber];
            for (int i = 0; i < _inputNumber; i ++) {
                initialOutput[i] = BooleanToken.FALSE;
            }
            output.broadcast(initialOutput, _inputNumber);
        } else {
            // make a "final" decision among minimum distances of all states.
            double minD = 0;
            int minIndex = 0;
            for (int state = 0; state < _rowNum; state ++) {
                if (state == 0 || _distance[state] < minD) {
                    minD = _distance[state];
                    minIndex = state;
                }
            }

            // Cast the decoding result into booleans and
            // send them in sequence to the output.
            BooleanToken[] decoded = new BooleanToken[_inputNumber];
            decoded = _convertToBit(_path[minIndex][0], _inputNumber);
            output.broadcast(decoded, _inputNumber);

            // Discard those datum in the buffers which have already
            // been made a "final" decision on. Move the rest datum
            // to the front of the buffers.
            for (int state = 0; state < _rowNum; state ++ ) {
                for (int i = 0; i < _flag; i ++) {
                    _path[state][i] = _path[state][i+1];
                }
            }
            _flag = _flag - 1;
        }
        _flag = _flag + 1;
        
    }
*/
    /** Initialize the actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    /*
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inputNumberInvalid = true;
        _flag = 0;
    }
*/
    /** Record the datum in buffers into their temporary versions.
     *  @exception IllegalActionException If the base class throws it
     */
    /*
    public boolean postfire() throws IllegalActionException {
        // Copy datum in buffers to their temp versions.
        for (int i = 0; i < _rowNum; i ++) {
            _tempDistance[i] = _distance[i];
            for (int j = 0; j < _flag; j ++) {
                _tempPath[i][j] = _path[i][j];
            }
        }
        return super.postfire();
    }
*/

    //////////////////////////////////////////////////////////
    ////            private methods                        ////

    /** Calculate the parity given by the polynomial and the
     *  state of shift register.
     *  @param mask Polynomial.
     *  @param maskNumber Number of polynomials.
     *  @param reg State of shift register.
     *  @return Parity.
     */
/*    
    private int[] _calculateParity(int[] mask, int maskNumber, int reg) {
        int[] parity = new int[maskNumber];
        for (int i = 0; i < maskNumber; i++) {
            int masked = mask[i] & reg;
            // Find the parity of the "masked".
            parity[i] = 0;
            // Calculate the parity of the masked word.
            while (masked > 0){
                parity[i] = parity[i] ^ (masked & 1);
                masked = masked >> 1;
            }
        }
        return parity;
    }
*/

    /** Compute the Hamming distance given by the datum received from
     *  the input port and the value in the truthTable.
     *  @param y Array of the booleans received from the input port.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @param maskNum The length of "y" and "truthValue".
     *  @return The distance.
     */
  /*
    private int _computeHardDistance(
            boolean[] y, int truthValue, int maskNum) {
        int hammingDistance = 0;
        for (int i = 0; i < maskNum; i ++) {
            int truthBit = truthValue & 1;
            truthValue = truthValue >> 1;

            // Compute Hamming distance for hard decoding.
            hammingDistance = hammingDistance + ((y[i] ? 1:0) ^ truthBit);
        }
        return hammingDistance;
    }
*/
    /** Compute the Euclidean distance given by the datum received from
     *  the input port and the value in the truthTable.
     *  @param y Array of the double-type numbers received from
     *  the input port.
     *  @param trueAmp Amplitude of "true" input.
     *  @param falseAmp Amplitude of "false" input.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @param maskNum The length of "y" and "truthValue".
     *  @return The distance.
     */
  /*  
    private double _computeSoftDistance(double[] y, double trueAmp,
            double falseAmp, int truthValue, int maskNum) {
        double distance = 0.0;

        for (int i = 0; i < maskNum; i ++) {
            int truthBit = truthValue & 1;
            truthValue = truthValue >> 1;
            double truthAmp;
            if (truthBit == 1) {
                truthAmp = trueAmp;
            } else {
                truthAmp = falseAmp;
            }
            // Euclidean distance for soft decoding. Here we
            // actually compute the square of the Euclidean distance.
            distance = distance
                + java.lang.Math.pow(y[i] - truthAmp, 2);
        }
        return distance;
    }
*/
    /** Convert an integer to its binary form. The bits
     *  are stored in an array.
     *  @param integer Interger that should be converted.
     *  @param length The length of "integer" in binary form.
     *  @return The bits of "integer" stored in an array.
     */
  /*  
    private BooleanToken[] _convertToBit(int integer, int length) {
        BooleanToken[] bit = new BooleanToken[length];
        for (int i = length -1; i >= 0; i --) {
            if ((integer & 1) == 1) {
                bit[i] = BooleanToken.TRUE;
            } else {
                bit[i] = BooleanToken.FALSE;
            }
            integer = integer >> 1;
        }
        return bit;
    }
*/
    //////////////////////////////////////////////////////////////
    ////           private parameters and variables           ////
/*
    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Input port type.
    private TypeAttribute _type;
    
    // Decoding mode.
    private boolean _mode;

    // Amplitudes for soft decoding.
    private double _trueAmp;
    private double _falseAmp;
    private Complex[] _constellation; 
    // Number bits the actor consumes per firing.
    private int _inputNumber;

    // Number of polynomials.
    private int _maskNumber;

    // Polynomial array.
    private int[] _mask;

    // The maximum value in integer among all polynomials.
    // It is used to compute the necessary shift register's length.
    private int _maxPolyValue;

    // Length of the shift register.
    private int _shiftRegLength = 0;

    // A flag indicating that the private variable
    // _inputNumber is invalid.
    private transient boolean _inputNumberInvalid = true;

    // A flag indicating that the the private variable
    // _depth is invalid.
    private transient boolean _depthInvalid = true;

    // Truth table for the corresponding convolutinal code.
    private int[][][] _truthTable;
    // Size of first dimension of the truth table.
    private int _rowNum;
    // Size of second dimension of the truth table.
    private int _colNum;

    // The delay specified by the user.
    private int _depth;

    // Buffers for minimum distance, possible input sequence
    // for each state. And their temporary versions used
    // when updating.
    private double[] _distance;
    private double[] _tempDistance;
    private int[][] _path;
    private int[][] _tempPath;
    // A flag used to indicate the positions that new values
    // should be inserted in the buffers.
    private int _flag;
    
    private static final int _HARD = 0;
    private static final int _SOFT = 1;
    private static final int _TRELLIS = 2;
*/
}
