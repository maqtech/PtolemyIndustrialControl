/* The right hand side terms of the helicopter dynamics.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.demo.Helicopter;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// HelicopterActor

/**
   The system dynamic of a 2D-helicopter model.
   <pre><code>
   ddPx/dtt = [-Tm*Cos(th)*sin(a) + Tm*Sin(th)*Cos(a)]/m
   ddPz/dtt = [-Tm*Sin(th)*Sin(a) - Tm*Cos(th)*Cos(a)]/m + g
   ddth/dtt = [Mm*a+hm*Tm*Sin(a)]/Iy
   </code></pre>

   where Px is the position of the helicopter on the x-axis; Pz is the
   position of the helicopter on the z-axis; th is the pitch angle of the
   helicopter; m is the mass; Tm is the torque generated by the rotor;
   a is the angle of the rotor; Mm, hm, Iy, and g are physical parameters;
   dd/dtt is the second order derivative operator.
   <P>
   The input of the actor is Tm and a, which are the control signals from
   the controller. The outputs are ddPx/dtt, ddPz/dtt, and ddth/dtt, they
   will be fed into integrators to get Px, Pz, and th.

   @author  Jie Liu, John Koo
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (liuj)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
public class HelicopterActor extends TypedAtomicActor {
    /** Construct the actor, all parameters take the default value.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name
     * @exception NameDuplicationException If another star already had
     * this name
     * @exception IllegalActionException If there is an internal error.
     */
    public HelicopterActor(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        inputTm = new TypedIOPort(this, "inputTm");
        inputTm.setInput(true);
        inputTm.setOutput(false);
        inputTm.setMultiport(false);
        inputTm.setTypeEquals(BaseType.DOUBLE);

        inputA = new TypedIOPort(this, "inputA");
        inputA.setInput(true);
        inputA.setOutput(false);
        inputA.setMultiport(false);
        inputA.setTypeEquals(BaseType.DOUBLE);

        inputTh = new TypedIOPort(this, "inputTh");
        inputTh.setInput(true);
        inputTh.setOutput(false);
        inputTh.setMultiport(false);
        inputTh.setTypeEquals(BaseType.DOUBLE);

        outputDDPx = new TypedIOPort(this, "outputDDPx");
        outputDDPx.setInput(false);
        outputDDPx.setOutput(true);
        outputDDPx.setMultiport(false);
        outputDDPx.setTypeEquals(BaseType.DOUBLE);

        outputDDPz = new TypedIOPort(this, "outputDDPz");
        outputDDPz.setInput(false);
        outputDDPz.setOutput(true);
        outputDDPz.setMultiport(false);
        outputDDPz.setTypeEquals(BaseType.DOUBLE);

        outputDDTh = new TypedIOPort(this, "outputDDTh");
        outputDDTh.setInput(false);
        outputDDTh.setOutput(true);
        outputDDTh.setMultiport(false);
        outputDDTh.setTypeEquals(BaseType.DOUBLE);

        _Iy = (double) 0.271256;
        paramIy = new Parameter(this, "Iy", new DoubleToken(_Iy));

        _hm = (double) 0.2943;
        paramHm = new Parameter(this, "hm", new DoubleToken(_hm));

        _Mm = (double) 25.23;
        paramMm = new Parameter(this, "Mm", new DoubleToken(_Mm));

        _mass = (double) 4.9;
        paramMass = new Parameter(this, "Mass", new DoubleToken(_mass));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute att) throws IllegalActionException {
        if (att == paramIy) {
            _Iy = ((DoubleToken) paramIy.getToken()).doubleValue();
        } else if (att == paramHm) {
            _hm = ((DoubleToken) paramHm.getToken()).doubleValue();
        } else if (att == paramMm) {
            _Mm = ((DoubleToken) paramMm.getToken()).doubleValue();
        } else if (att == paramMass) {
            _mass = ((DoubleToken) paramMass.getToken()).doubleValue();
        }
    }

    /** Compute the output.
     *
     *  @exception IllegalActionException If there's no input token
     *        when needed.
     */
    public void fire() throws IllegalActionException {
        double Tm = ((DoubleToken) inputTm.get(0)).doubleValue();
        double A = ((DoubleToken) inputA.get(0)).doubleValue();
        double Th = ((DoubleToken) inputTh.get(0)).doubleValue();

        double ddPx = ((-Tm * Math.cos(Th) * Math.sin(A))
            + (Tm * Math.sin(Th) * Math.cos(A))) / _mass;
        double ddPz = (((-Tm * Math.sin(Th) * Math.sin(A))
            - (Tm * Math.cos(Th) * Math.cos(A))) / _mass) + g;
        double ddTh = ((_Mm * A) + (_hm * Tm * Math.sin(A))) / _Iy;
        outputDDPx.broadcast(new DoubleToken(ddPx));
        outputDDPz.broadcast(new DoubleToken(ddPz));
        outputDDTh.broadcast(new DoubleToken(ddTh));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     */
    public static final double g = 9.8;

    /** Input port Tm
     */
    public TypedIOPort inputTm;

    /** Input port a
     */
    public TypedIOPort inputA;

    /** Input port Th
     */
    public TypedIOPort inputTh;

    /** Output port Px
     */
    public TypedIOPort outputDDPx;

    /** Output port Pz
     */
    public TypedIOPort outputDDPz;

    /** Output port Th
     */
    public TypedIOPort outputDDTh;

    /** Parameter for Iy, double, default 0.271256.
     */
    public Parameter paramIy;

    /** Parameter for hm, double, default 0.2943.
     */
    public Parameter paramHm;

    /** Parameter for Mm, double, default 25.23.
     */
    public Parameter paramMm;

    /** Parameter for the mass, double, default 4.9.
     */
    public Parameter paramMass;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Variables
    private double _Iy;
    private double _hm;
    private double _Mm;
    private double _mass;
}
