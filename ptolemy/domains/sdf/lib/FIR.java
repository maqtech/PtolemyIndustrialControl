/* An FIR filter.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// FIR
/**
This actor implements a finite-impulse response
filter with multirate capability.
<p>
When the <i>decimation</i> (<i>interpolation</i>)
parameters are different from unity, the filter behaves exactly
as it were followed (preceded) by a DownSample (UpSample) actor.
However, the implementation is much more efficient than
it would be using UpSample and DownSample stars;
a polyphase structure is used internally, avoiding unnecessary use
of memory and unnecessary multiplication by zero.
Arbitrary sample-rate conversions by rational factors can
be accomplished this way.
<p>
To design a filter for a multirate system, simply assume the
sample rate is the product of the interpolation parameter and
the input sample rate, or equivalently, the product of the decimation
parameter and the output sample rate.
In particular, considerable care must be taken to avoid aliasing.
Specifically, if the input sample rate is <i>f</i>,
then the filter stopband should begin before <i>f</i>/2.
If the interpolation ratio is <i>i < /i>, then <i>f</i>/2 is a fraction
1/2<i>i < /i> of the sample rate at which you must design your filter.
<p>
The <i>decimationPhase</i> parameter is somewhat subtle.
It is exactly equivalent the phase parameter of the DownSample star.
Its interpretation is as follows; when decimating,
samples are conceptually discarded (although a polyphase structure
does not actually compute the discarded samples).
If you are decimating by a factor of three, then you will select
one of every three outputs, with three possible phases.
When decimationPhase is zero (the default),
the latest (most recent) samples are the ones selected.
The decimationPhase must be strictly less than
the decimation ratio.
<p>
For more information about polyphase filters, see F. J. Harris,
"Multirate FIR Filters for Interpolating and Desampling", in
<i>Handbook of Digital Signal Processing</i>, Academic Press, 1987.

@author Edward A. Lee
@version $Id$
*/

public class FIR extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FIR(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(DoubleToken.class);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(DoubleToken.class);

        taps = new Parameter(this, "taps", new DoubleMatrixToken());
        interpolation = new Parameter(this, "interpolation", new IntToken(1));

        // FIXME: Added decimation and decimationPhase parameters
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public TypedIOPort input;

    /** The output port. */
    public TypedIOPort output;

    /** The interpolation ratio of the filter. This must contain an
     *  IntToken, and by default it has value one.
     */
    public Parameter interpolation;

    /** The taps of the filter.  This is a row vector embedded in
     *  in a token of type DoubleMatrixToken.  By default, it is empty,
     *  meaning that the output of the filter is zero.
     */
    public Parameter taps;

    // FIXME: Check that the above comment is correct.

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            FIR newobj = (FIR)(super.clone(ws));
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            newobj.interpolation =
                (Parameter)newobj.getAttribute("interpolation");
            newobj.taps = (Parameter)newobj.getAttribute("taps");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Set up the consumption and production constants.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        IntToken interptoken = (IntToken)(interpolation.getToken());
        _interp = interptoken.intValue();

        // FIXME: Support multirate.  Get values from parameters.
        _dec = 1;
        _decPhase = 0;

        // FIXME: Does the SDF infrastructure support accessing past samples?
        // FIXME: Handle mutations.
        setTokenConsumptionRate(input, _dec);
        setTokenProductionRate(output, _interp);
        if (_decPhase >= _dec) {
            throw new IllegalActionException(this,"decimationPhase too large");
        }
        // FIXME: Need error checking of parameter.
        DoubleMatrixToken tapstoken = (DoubleMatrixToken)(taps.getToken());
        _taps = new double[tapstoken.getColumnCount()];
        for (int i = 0; i < _taps.length; i++) {
            _taps[i] = tapstoken.getElementAt(0, i);
        }
        _phaseLength = (int)(_taps.length / _interp);
        if ((_taps.length % _interp) != 0) _phaseLength++;

        // Create new data array and initialize index into it.
        int datalength = _taps.length/_interp;
        if (_taps.length%_interp != 0) datalength++;
        _data = new double[datalength];
        _mostRecent = datalength;
    }

    /** Consume the inputs and produce the outputs of the FIR filter.
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {

        // phase keeps track of which phase of the filter coefficients
        // are used. Starting phase depends on the _decPhase value.
        int phase = _dec - _decPhase - 1;

        // FIXME: consume just one input for now.
        if (--_mostRecent < 0) _mostRecent = _data.length - 1;
        _data[_mostRecent] = ((DoubleToken)(input.get(0))).doubleValue();

        // Interpolate once for each input consumed
        for (int inC = 1; inC <= _dec; inC++) {
            // Produce however many outputs are required
            // for each input consumed
            while (phase < _interp) {
                double out = 0.0;
                // Compute the inner product.
                for (int i = 0; i < _phaseLength; i++) {
                    int tapsIndex = i * _interp + phase;
                    double tap = 0.0;
                    if (tapsIndex < _taps.length) tap = _taps[tapsIndex];
                    int dataIndex =
                        (_mostRecent + _dec - inC + i)%(_data.length);
                    out += tap * _data[dataIndex];
                }
                output.broadcast(new DoubleToken(out));
                phase += _dec;
            }
            phase -= _interp;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The phaseLength is ceiling(length/interpolation), where
     *  length is the number of taps.
     */
    int _phaseLength;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Local cache of these parameter values.
    private int _dec, _interp, _decPhase;
    private double[] _taps;
    private double[] _data;
    private int _mostRecent;
}
