/* A GR Shape consisting of a sphere

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

//////////////////////////////////////////////////////////////////////////
//// TextString3D

/** Conceptually, this actor takes 3D geometry in its input and produces a scaled
version in its output. In reality, this actor encapsulates a Java3D TransformGroup
which is converted into a node in the resulting Java3D scene graph. This actor will
only have meaning in the GR domain. Scaling can be done uniformly or non-uniformly.
Uniform scaling scales the input geometry equally in all directions. Uniform scaling 
is done through modification of the <i>scaleFactor</i> parameter. Non-uniform scaling
involves preferential scaling of the input geometry in a specified Cartesian axis. 
Non-uniform scaling is done through modification of the <i>xScale<i>, <i>yScale<i/>,
and <i>zScale<i/> parameters. 

@author C. Fong
*/
package ptolemy.domains.gr.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.gr.kernel.*;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.Font;


public class TextString3D extends GRShadedShape {

    public TextString3D(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        text = new Parameter(this, "text", new StringToken("Ptolemy"));
    }
    
    public Parameter text;
    
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        Font3D font3D = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                                    new FontExtrusion());
        Text3D textGeom = new Text3D(font3D, new String(_getText()));
        textGeom.setAlignment(Text3D.ALIGN_CENTER);
        containedNode = new Shape3D();
        containedNode.setGeometry(textGeom);
        containedNode.setAppearance(_appearance);
    }

    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TextString3D newobj = (TextString3D) super.clone(workspace);
        newobj.text = (Parameter) newobj.getAttribute("text");
        return newobj;
    }
    
    
    public Node getNodeObject() {
        return (Node) containedNode;
    }
   

    private String _getText() throws IllegalActionException {
        return ((StringToken) text.getToken()).stringValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
   
    private Shape3D containedNode;
}
