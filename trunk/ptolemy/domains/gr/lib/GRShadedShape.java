/* An abstract base class for shaded DD3D Actors

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

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.gr.kernel.*;

import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;


//////////////////////////////////////////////////////////////////////////
//// GRShadedShape
/** An abstract base class for DD3D Actors that have material and color
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
    public GRShadedShape(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.GENERAL);
        redComponent   = new Parameter(this,"redComponent", new DoubleToken(0.7));
        greenComponent = new Parameter(this,"greenComponent", new DoubleToken(0.7));
        blueComponent  = new Parameter(this,"blueComponent", new DoubleToken(0.7));
        shininess = new Parameter(this,"shininess",new DoubleToken(0.0));
        
        _color = new Color3f(1.0f,1.0f,1.0f);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The output port for connecting to other DD3D Actors in
     *  the scene graph
     */
    public TypedIOPort output;
    
    /** The red color component of the 3D shape
     */
    public Parameter redComponent;

    /** The green color component of the 3D shape
     */
    public Parameter greenComponent;
    
    /** The blue color component of the 3D shape
     */
    public Parameter blueComponent;
    
    /** The shininess of the 3D shape
     */
    public Parameter shininess;
    
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
        GRShadedShape newobj = (GRShadedShape)super.clone(workspace);
        
        newobj.output = (TypedIOPort)newobj.getPort("output");
        newobj.redComponent = (Parameter)newobj.getAttribute("redComponent");
        newobj.greenComponent = (Parameter)newobj.getAttribute("greenComponent");
        newobj.blueComponent = (Parameter)newobj.getAttribute("blueComponent");
        newobj.shininess = (Parameter) newobj.getAttribute("shininess");
        return newobj;
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
        _color.x = (float) ((DoubleToken) redComponent.getToken()).doubleValue();
        _color.y = (float) ((DoubleToken) greenComponent.getToken()).doubleValue();
        _color.z = (float) ((DoubleToken) blueComponent.getToken()).doubleValue();
        _shine = (float) ((DoubleToken) shininess.getToken()).doubleValue();
        
        _createAppearance();
    }
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////
    
    protected Color3f _color;
    protected Appearance _appearance;
    protected Material _material;
    protected float _shine;

    protected static final Color3f whiteColor = new Color3f(1.0f,1.0f,1.0f);
    protected static final Color3f blueColor = new Color3f(0.0f,0.0f,1.0f);
}
