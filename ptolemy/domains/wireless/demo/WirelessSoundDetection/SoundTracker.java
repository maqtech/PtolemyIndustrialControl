/* A sound tracker that uses triangulation to identify the origin of a sound.

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

@ProposedRating Yellow (eal@.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.domains.sensor.demo.WirelessSoundDetection;


import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SoundTracker
/**
FIXME

@author TODO: Philip Baldwin, Xioajun Liu, Edward A. Lee
@version $Id$
*/

public class SoundTracker extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SoundTracker(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort (this, "input", true, false);
        // FIXME: Set type?
        //TypeAttribute inputType = new TypeAttribute(input, "type");
        //inputType.setExpression("{location=[double], time=double}");

        outputX = new TypedIOPort (this, "outputX", false, true);
        outputX.setTypeEquals(BaseType.DOUBLE);

        outputY = new TypedIOPort (this, "outputY", false, true);
        outputY.setTypeEquals(BaseType.DOUBLE);

        // create signal propatation speed parameter, set it to 340.0
        signalPropagationSpeed = new Parameter(this, "signalPropagationSpeed");
        signalPropagationSpeed.setToken("340.0");
        signalPropagationSpeed.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public Parameter signalPropagationSpeed;

    /** TODO: Describe port and its type constraints.
     */
    public TypedIOPort input;

    /** The output producing the X coordinate of the sound source.
     *  This has type double.
     */
    public TypedIOPort outputX;

    /** The output producing the Y coordinate of the sound source.
     *  This has type double.
     */
    public TypedIOPort outputY;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** TODO: Describe what the fire method does.
     *  @exception IllegalActionException TODO: Describe when this is thrown.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        RecordToken recordToken;
        while (input.hasToken(0)) {
            recordToken = (RecordToken)input.get(0);
            _sensorReadings[_counter] = recordToken;
            ++_counter;

            // If enough inputs have been received, locate the source.
            if (_counter == 3) {
                _counter = 0;
                double[] locationsX = new double[3];
                double[] locationsY = new double[3];
                double[] times = new double[3];

                // Loop through the sensor reading array, extract x, y, and t, and
                // put them in the arrays above.
                for (int i = 0; i < 3; i++) {
                    RecordToken recordTokenTemp;
                    recordTokenTemp = _sensorReadings[i];
                    DoubleMatrixToken locationMatrix = (DoubleMatrixToken)recordTokenTemp.get("location");
                    times[i] = ((DoubleToken)recordTokenTemp.get("time")).doubleValue();

                    locationsX[i] = locationMatrix.getElementAt(0,0);
                    locationsY[i] = locationMatrix.getElementAt(0,1);
                }
                // Get signal speed, from the signalPropagationSpeed parameter.
                double speed = ((DoubleToken)(signalPropagationSpeed.getToken())).doubleValue();
                double [] result = _locate(locationsX[0], locationsY[0], times[0], locationsX[1], locationsY[1]
                    , times[1], locationsX[2], locationsY[2], times[2], speed);
                System.out.println("source at " + result[0] + ", " + result[1] + ", at time " + result[2]);
                if (Double.isInfinite(result[2]) || Double.isNaN(result[2])) return;
        
                outputX.send(0, new DoubleToken(result[0]));
                outputY.send(0, new DoubleToken(result[1]));
            }
        }
    }

    /** Override the base class to initialize the signal count.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _counter = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////

    /** FIXME
     * @param result
     * @param x1
     * @param y1
     * @param t1
     * @param x2
     * @param y2
     * @param t2
     * @param x3
     * @param y3
     * @param t3
     * @return
     */
    private static boolean _checkResult(double[] result,
            double x1, double y1, double t1, 
            double x2, double y2, double t2, 
            double x3, double y3, double t3,
            double v) {
        if (result[2] > t1 || result[2] > t2 || result[2] > t3) {
            return false;
        }
        double tdiff1 =
                Math.abs(_distance(x1, y1, result[0], result[1])/v - (t1 - result[2]));
        double tdiff2 =
                Math.abs(_distance(x2, y2, result[0], result[1])/v - (t2 - result[2]));
        double tdiff3 = 
                Math.abs(_distance(x3, y3, result[0], result[1])/v - (t3 - result[2]));
        //TODO: make the check threshold a parameter?
        if (tdiff1 > 1e-5 || tdiff2 > 1e-5 || tdiff3 > 1e-5) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Return the Cartesian distance between (x1, y1) and (x2, y2).
     *  @param x1 The first x coordinate.
     *  @param y1 The first y coordinate.
     *  @param x2 The second x coordinate.
     *  @param y2 The second y coordinate.
     *  @return The distance.
     */
    private static double _distance(
            double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private static double[] _locate(
            double x1, double y1, double t1,
            double x2, double y2, double t2,
            double x3, double y3, double t3,
            double v) {
        double[] result = new double[3];
        double v2 = v * v;
        double[][] m = {
            { 2 * (x2 - x1), 2 * (y2 - y1) },
            { 2 * (x3 - x1), 2 * (y3 - y1) }
        };
        double[] b = { 2 * v2 * (t2 - t1), 2 * v2 * (t3 - t1) };
        double[] c = {
            t1 * t1 * v2 - t2 * t2 * v2 + x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1,
            t1 * t1 * v2 - t3 * t3 * v2 + x3 * x3 - x1 * x1 + y3 * y3 - y1 * y1
        };
        // FIXME: what if det_m is 0? That is, the three sensors are located on
        // a straight line.
        double det_m = m[0][0] * m[1][1] - m[1][0] * m[0][1];
        double[][] m_inv = {
            { m[1][1] / det_m, -m[0][1] / det_m },
            { -m[1][0] / det_m, m[0][0] / det_m }
        };
        double[] m_inv_b = {
            m_inv[0][0] * b[0] + m_inv[0][1] * b[1],
            m_inv[1][0] * b[0] + m_inv[1][1] * b[1]
        };
        double[] m_inv_c = {
            m_inv[0][0] * c[0] + m_inv[0][1] * c[1],
            m_inv[1][0] * c[0] + m_inv[1][1] * c[1]
        };
        double ea = m_inv_b[0] * m_inv_b[0] + m_inv_b[1] * m_inv_b[1] - v2;
        double eb = 2 * m_inv_b[0] * (m_inv_c[0] - x1)
                + 2 * m_inv_b[1] * (m_inv_c[1] - y1) + 2 * v2 * t1;
        double ec = (m_inv_c[0] - x1) * (m_inv_c[0] - x1)
                + (m_inv_c[1] - y1) * (m_inv_c[1] - y1) - t1 * t1 * v2;
        double delta = eb * eb - 4 * ea * ec;
        //System.out.println("delta is " + delta);
        if (delta >= 0) {
            result[2] = (-eb + Math.sqrt(delta)) / ea / 2;
            result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
            result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
            if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                return result;
            } else {
                result[2] = (-eb - Math.sqrt(delta)) / ea / 2;
                result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
                result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
                if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                    return result;
                } else {
                    result[0] = Double.NEGATIVE_INFINITY;
                    result[1] = Double.NEGATIVE_INFINITY;
                    result[2] = Double.NEGATIVE_INFINITY;
                    return result;
                }
            }
        } else {
            result[2] = -eb / ea / 2;
            result[0] = m_inv_b[0] * result[2] + m_inv_c[0];
            result[1] = m_inv_b[1] * result[2] + m_inv_c[1];
            if (_checkResult(result, x1, y1, t1, x2, y2, t2, x3, y3, t3, v)) {
                return result;
            } else {
                result[0] = Double.NEGATIVE_INFINITY;
                result[1] = Double.NEGATIVE_INFINITY;
                result[2] = Double.NEGATIVE_INFINITY;
                return result;
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Counter that counts incoming signals. */
    private int _counter = 0;

    private RecordToken[] _sensorReadings = new RecordToken[3];
}
