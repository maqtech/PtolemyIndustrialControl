/* The right hand side terms of the helicopter dynamics.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.demo.heli;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;

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
helicopter; m is the mass; Tm is the torque generated by the spiner;
a is the angle of the spiner; Mm, hm, Iy, and g are physical parameters;
dd/dtt is the second order derivative operator.
<P>
The input of the actor is Tm and a, which are the control signals from
the controller. The outputs are ddPx/dtt, ddPz/dtt, and ddth/dtt, they
will be fed into integrators to get Px, Pz, and th.

@author  Jie Liu, John Koo
@version %Id%
*/
public class HelicopterActor extends CTActor{
    /** Construct the actor, all parameters take the default value.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name
     * @exception NameDuplicationException another star already had this name
     * @exception IllegalActionException illustrates internal problems
     */	
    public HelicopterActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        inputTm = new TypedIOPort(this, "inputTm");
        inputTm.setInput(true);
        inputTm.setOutput(false);
        inputTm.setMultiport(false);
        inputTm.setDeclaredType(DoubleToken.class);

        inputA = new TypedIOPort(this, "inputA");
        inputA.setInput(true);
        inputA.setOutput(false);
        inputA.setMultiport(false);
        inputA.setDeclaredType(DoubleToken.class);

        inputTh = new TypedIOPort(this, "inputTh");
        inputTh.setInput(true);
        inputTh.setOutput(false);
        inputTh.setMultiport(false);
        inputTh.setDeclaredType(DoubleToken.class);

        outputPx = new TypedIOPort(this, "outputPx");
        outputPx.setInput(false);
        outputPx.setOutput(true);
        outputPx.setMultiport(false);
        outputPx.setDeclaredType(DoubleToken.class);

        outputPz = new TypedIOPort(this, "outputPz");
        outputPz.setInput(false);
        outputPz.setOutput(true);
        outputPz.setMultiport(false);
        outputPz.setDeclaredType(DoubleToken.class);

        outputTh = new TypedIOPort(this, "outputTh");
        outputTh.setInput(false);
        outputTh.setOutput(true);
        outputTh.setMultiport(false);
        outputTh.setDeclaredType(DoubleToken.class);

        _Iy = (double)0.271256;
        _paramIy = new CTParameter(this, "Iy", new DoubleToken(_Iy));

        _hm = (double)0.2943;
        _paramHm = new CTParameter(this, "hm", new DoubleToken(_hm));

        _Mm = (double)25.23;
        _paramMm = new CTParameter(this, "Iy", new DoubleToken(_Mm));

        _mass = (double)4.9;
        _paramMass = new CTParameter(this, "Mass", new DoubleToken(_mass));
          
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the output.
     *
     *  @exception IllegalActionException If there's no input token
     *        when needed.
     */
    public void fire() throws IllegalActionException{
        double Tm = ((DoubleToken)inputTm.get(0)).doubleValue();
        double A = ((DoubleToken)inputA.get(0)).doubleValue();
        double Th = ((DoubleToken)inputTh.get(0)).doubleValue();
        
        double ddPx = (-Tm*Math.cos(Th)*Math.sin(A) + 
                Tm*Math.sin(Th)*Math.cos(A))/_mass;
        double ddPz = (-Tm*Math.sin(Th)*Math.sin(A) - 
                Tm*Math.cos(Th)*Math.cos(A))/_mass + g;
        double ddTh = (_Mm*A + _hm*Tm*Math.sin(A))/_Iy;
        outputPx.broadcast(new DoubleToken(ddPx));
        outputPz.broadcast(new DoubleToken(ddPz));
        outputTh.broadcast(new DoubleToken(ddTh));
    }
    
    /** Update the parameter if they have been changed.
     *  The new parameter will be used only after this method is called.
     *  @exception IllegalActionException Never thrown.*
     */
    public void updateParameters() throws IllegalActionException {
        _Iy = ((DoubleToken)_paramIy.getToken()).doubleValue();
        _hm = ((DoubleToken)_paramHm.getToken()).doubleValue();
        _Mm = ((DoubleToken)_paramMm.getToken()).doubleValue();
        _mass = ((DoubleToken)_paramMass.getToken()).doubleValue();
    }


    /////////////////////////////////////////////////////////////////////
    ////                         public variables                    ////
    
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
    public TypedIOPort outputPx;

    /** Output port Pz
     */
    public TypedIOPort outputPz;

    /** Output port Th
     */
    public TypedIOPort outputTh;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Variables
    private CTParameter _paramIy;
    private double _Iy;

    private CTParameter _paramHm;
    private double _hm;

    private CTParameter _paramMm;
    private double _Mm;

    private CTParameter _paramMass;
    private double _mass;

    
}
