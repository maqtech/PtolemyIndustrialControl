/* An abstract base class for shaded GR Actors

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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.GRUtilities2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Paint;

//////////////////////////////////////////////////////////////////////////
//// GRShape2D
/** An abstract base class for two-dimensional GR Actors representing
figures. The parameters <i>redComponent</i>, <i>greenComponent</i>,
<i>blueComponent</i> determine the color of the object.  

@author Steve Neuendorffer, Ismael M. Sarmiento
@version $Id$
@since Ptolemy II 1.0
*/
abstract public class GRShape2D extends GRActor2D {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRShape2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(Scene2DToken.TYPE);

        rgbFillColor = new Parameter(this, "rgbFillColor",
                new DoubleMatrixToken(new double[][] {{ 1.0, 1.0, 1.0}} ));
        rgbFillColor.setTypeEquals(BaseType.DOUBLE_MATRIX);
        
        rgbOutlineColor = new Parameter(this, "rgbOutlineColor",
                new DoubleMatrixToken(new double[][] {{ 0.0, 0.0, 0.0}} ));
        rgbOutlineColor.setTypeEquals(BaseType.DOUBLE_MATRIX);

        outlineWidth = new Parameter(this, "outlineWidth",
                new DoubleToken(1.0));
        outlineWidth.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The output port for connecting to other GR Actors in
     *  the scene graph
     */
    public TypedIOPort sceneGraphOut;

    /** The red, green, and blue color components of the interior of
     *  the figure.  This parameter must contain a DoubleMatrixToken.
     *  The default value is [1.0,1.0,1.0], corresponding to white.
     */
    public Parameter rgbFillColor;

    /** The red, green, and blue color components of the outside of
     *  the figure.  This parameter must contain a DoubleMatrixToken.
     *  The default value is [0.0,0.0,0.0], corresponding to black.
     */
    public Parameter rgbOutlineColor;

    /** The width of the figure's outline.  This parameter must contain a 
     *  DoubleToken.  The default value is 1.0.
     */
    public Parameter outlineWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the figure for this actor.
     *
     *  @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _figure = _createFigure();
        _setAppearance(_figure);
    }


    /** Return false if the scene graph is already initialized.
     *
     *  @return false if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        if (_isSceneGraphInitialized) {
            return false;
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the figure for this actor.  Derived classes should implement
     *  this method to create the correct figure.
     *
     *  @exception IllegalActionException If a parameter is not valid.
     */
    abstract protected BasicFigure _createFigure() 
            throws IllegalActionException;
 
    /** Setup the scene graph connections of this actor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new Scene2DToken(_figure));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Set the appearance of the given figure consistent with the
    // parameters of this class.
    private void _setAppearance(Figure figure) throws IllegalActionException {
        Paint fillPaint = GRUtilities2D.makeColor(   
                (DoubleMatrixToken) rgbFillColor.getToken());
        Paint strokePaint = GRUtilities2D.makeColor(
                (DoubleMatrixToken) rgbOutlineColor.getToken());
        float lineWidth = (float)
            ((DoubleToken) outlineWidth.getToken()).doubleValue();
        
        _figure.setFillPaint(fillPaint);
        _figure.setStrokePaint(strokePaint);
        _figure.setLineWidth(lineWidth);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////

    protected BasicFigure _figure;
}
