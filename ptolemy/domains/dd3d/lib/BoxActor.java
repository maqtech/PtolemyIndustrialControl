/* A DD3D Shape consisting of a polyhedral box

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
package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;

import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// BoxActor
/** This actor contains the geometry and appearance specifications for a DD3D
box.  The output port is used to connect this actor to the Java3D scene
graph. This actor will only have meaning in the DD3D domain. 

    The parameters <i>xLength</i>, <i>yHeight</i>, and <i>zWidth</i> determine
the dimensions of box.  

@author C. Fong
*/
public class BoxActor extends Shaded3DActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BoxActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        xLength = new Parameter(this, "xLength", new DoubleToken(0.5));
        yHeight = new Parameter(this, "yHeight", new DoubleToken(0.5));
        zWidth = new Parameter(this, "zWidth", new DoubleToken(0.5));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the box in the x-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter xLength;
    
    /** The height of the box in the y-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter yHeight;
    
    /** The width of the box in the z-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 0.5
     */
    public Parameter zWidth;
  
  
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
        BoxActor newobj = (BoxActor)super.clone(workspace);
        newobj.xLength = (Parameter) newobj.getAttribute("xLength");
        newobj.yHeight = (Parameter) newobj.getAttribute("yHeight");
        newobj.zWidth = (Parameter) newobj.getAttribute("zWidth");
        return newobj;
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D box.
     *  @return the Java3D box.
     */    
    public Node getNodeObject() {
        return (Node) containedNode;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        containedNode = new Box((float)_getLength(),(float) _getWidth(),
                   (float) _getHeight(), Box.GENERATE_NORMALS,_appearance);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the value of the length parameter
     *  @return the length of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getLength() throws IllegalActionException {
        double value = ((DoubleToken) xLength.getToken()).doubleValue();
        return value / 2.0;
    }
    
    
    /** Return the value of the width parameter
     *  @return the width of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getWidth() throws IllegalActionException {
        double value = ((DoubleToken) yHeight.getToken()).doubleValue();
        return value / 2.0;
    }

    /** Return the value of the height parameter
     *  @return the height of the box
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getHeight() throws IllegalActionException  {
        double value = ((DoubleToken) zWidth.getToken()).doubleValue();
        return value / 2.0;
    }    
 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
   
    private Box containedNode;
}
