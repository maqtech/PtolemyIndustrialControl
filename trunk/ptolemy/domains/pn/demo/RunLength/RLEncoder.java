/* Reads an audio file and divides the audio data into blocks.

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
*/

package ptolemy.domains.pn.demo.RunLength;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNImageSink
/**
Stores an image file (int the ASCII PBM format) and creates a matrix token

@author Mudit Goel
@version $Id$
*/

public class RLEncoder extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public RLEncoder(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
        _dimenin = new IOPort(this, "dimensionsIn", true, false);
        _dimenout = new IOPort(this, "dimensionsOut", false, true);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
        //Get and transmit the dimensions
        IntToken token = (IntToken)_dimenin.get(0);
        int rows = token.intValue();
        _dimenout.broadcast(token);
        token = (IntToken)_dimenin.get(0);
        int columns = token.intValue();
        _dimenout.broadcast(token);
        int inlen = rows*columns;
        _inputlength = rows*columns;

        //Read the stream of tokens to be encoded
        token = (IntToken)_input.get(0);
        int value = token.intValue();
	byte outval = 0;
	if (value == 1) outval = (byte)128;
	//byte outval = (byte)((byte)value<<7);
        int count = 1;
        for (int i = 1; i < inlen; i++) {
            int newval = ((IntToken)_input.get(0)).intValue();
            if (newval == value) {
                count++;
		if (count == 128) {
		    outval = (byte)(outval | (byte)127);
		    _output.broadcast(new IntToken(outval));
		    _outputlength++;
		    count = 0;
		    if (i < inlen) {
			value = ((IntToken)_input.get(0)).intValue();
			i++;
			if (value == 1) outval = (byte)128;
			else outval = 0;
			//outval = (byte)((byte)value<<7);
			count = 1;
		    }
		}
            } else {
		outval = (byte)(outval | (byte)(count-1));
                _output.broadcast(new IntToken(outval));
                //_output.broadcast(new IntToken(count));
                _outputlength ++; //= (count+127-((count+127)%128))/128;
                value = newval;
		if (value == 1) outval = (byte)128;
		else outval = 0;
                count = 1;
            }
        }
        //_output.broadcast(new IntToken(value));
        //_output.broadcast(new IntToken(count));
	outval = (byte)(outval | (byte)(count-1));
	_output.broadcast(new IntToken(outval));
	_outputlength++;
        System.out.println("Approximate compression ratio = "+
                ((double)_outputlength*100*8)/_inputlength);
    }

    //public boolean postfire() {
    //return false;
    //}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _dimenin;
    private IOPort _dimenout;
    private IOPort _output;
    private IOPort _input;
    private int _inputlength = 0;
    private int _outputlength = 0;
}
