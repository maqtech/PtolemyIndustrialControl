/* An abstract base class for shaded GR Actors

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.*;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.gr.kernel.*;

import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// GRShadedShape
/** An abstract base class for GR Actors that have material and color
properties. The parameters <i>redComponent</i>, <i>greenComponent</i>,
<i>blueComponent</i> determine the color of the object.  The parameter
<i>shininess</i> determines the Phong exponent used in calculating
the shininess of the object. 

@author C. Fong
*/
public class GRShadedShape extends GRActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRShadedShape(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.GENERAL);
        rgbColor = new Parameter(this,"RGB color",
                    new DoubleMatrixToken(new double[][] {{ 0.7, 0.7, 0.7}} ));
        
        shininess = new Parameter(this,"shininess",new DoubleToken(0.0));
        
        pose = new Parameter(this,"pose",
               new DoubleMatrixToken(_pose));
        
        _color = new Color3f(1.0f,1.0f,1.0f);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The output port for connecting to other GR Actors in
     *  the scene graph
     */
    public TypedIOPort output;
    
    /** The red, green, and blue color components of the 3D shape
     */
    public Parameter rgbColor;
   
    /** The shininess of the 3D shape
     */
    public Parameter shininess;
    
    /** The initial pose of the 3D shape
     */
    public Parameter pose;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GRShadedShape newObject = (GRShadedShape)super.clone(workspace);
        
        newObject.output = (TypedIOPort)newobj.getPort("output");
        newObject.rgbColor = (Parameter)newobj.getAttribute("rgbColor");
        newObject.shininess = (Parameter) newobj.getAttribute("shininess");
        return newObject;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Create the material appearance of the shaded 3D actor
     */
    protected void _createAppearance() {

        _material = new Material();
        _appearance = new Appearance();
        
        _material.setDiffuseColor(_color);
        if (_shine > 1.0) {
            _material.setSpecularColor(whiteColor);
            _material.setShininess(_shine);
        } else {
            _material.setSpecularColor(_color);
        }
        _appearance.setMaterial(_material);
    }
    
    /** Create the color of the shaded 3D actor
     */
    protected void _createModel() throws IllegalActionException {
        
        super._createModel();
        
        DoubleMatrixToken color = (DoubleMatrixToken) rgbColor.getToken();
       
        _color.x = (float) color.getElementAt(0,0);
        _color.y = (float) color.getElementAt(0,1);
        _color.z = (float) color.getElementAt(0,2);
        _shine = (float) ((DoubleToken) shininess.getToken()).doubleValue();
        
        _createAppearance();
    }
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////
    
    protected Color3f _color;
    protected Appearance _appearance;
    protected Material _material;
    protected float _shine;
    protected double[][] _pose = new double[][] {{1.0, 0.0, 0.0, 0.0},
                                                 {0.0, 1.0, 0.0, 0.0},
                                                 {0.0, 0.0, 1.0, 0.0},
                                                 {0.0, 0.0, 0.0, 1.0}};

    protected static final Color3f whiteColor = new Color3f(1.0f,1.0f,1.0f);
    protected static final Color3f blueColor = new Color3f(0.0f,0.0f,1.0f);
}
