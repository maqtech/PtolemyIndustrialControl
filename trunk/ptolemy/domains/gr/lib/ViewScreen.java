/* A GR scene viewer

 Copyright (c) 1998-2001 The Regents of the University of California.
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
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;

import ptolemy.domains.gr.kernel.*;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.util.Enumeration;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// ViewScreen

/** A sink actor that renders the GR geometry into a display screen
@author C. Fong
@version $Id$
*/
public class ViewScreen extends GRActor implements Placeable {

    /** Construct a ViewScreen in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ViewScreen(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setTypeEquals(BaseType.OBJECT);
        sceneGraphIn.setMultiport(true);

        horizontalResolution = new Parameter(this,
                               "horizontalResolution",new IntToken(400));
        verticalResolution = new Parameter(this,
                             "verticalResolution",new IntToken(400));
        rotatable = new Parameter(this,
                    "rotatable",new BooleanToken(true));
        scalable = new Parameter(this,
                   "scalable",new BooleanToken(false));
        translatable = new Parameter(this,
                       "translatable",new BooleanToken(false));
        showAxes = new Parameter(this,"showAxes",new BooleanToken(false));
        iterationSynchronized = new Parameter(this,
                       "iterationSynchronized",new BooleanToken(false));


        _lastTransform = new Transform3D();
        _root = this;
    }

    public TypedIOPort sceneGraphIn;

    /** The horizontal resolution of the display screen
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is the IntToken 400
     */
    public Parameter horizontalResolution;

    /** Boolean variable that determines whether the user is allowed to
     *   rotate the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken true
     */
    public Parameter rotatable;

    /** Boolean variable that determines whether the user is allowed to
     *   scale the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter scalable;

    public Parameter showAxes;

    /*  Boolean variable that determines whether screen update is done
     *   once per iteration
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter iterationSynchronized;

    /** Boolean variable that determines whether the user is allowed to
     *   translate the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter translatable;

    /** The vertical resolution of the display screen
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is IntToken 400
     */
    public Parameter verticalResolution;


    public void place(Container container) {
        GraphicsConfiguration config =
            SimpleUniverse.getPreferredConfiguration();

        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalResolution();
            verticalDimension = _getVerticalResolution();
        } catch (Exception e) {
            // FIXME handle this
        }

        if (_canvas ==null) _canvas = new Canvas3D(config);
        if (container==null) {
            _frame = new JFrame("ViewScreen");
            _frame.getContentPane().add(_canvas,BorderLayout.CENTER);
            _canvas.setSize(new Dimension(horizontalDimension,verticalDimension));
            _frame.setSize(horizontalDimension+50,verticalDimension);
            if (simpleUniverse == null) simpleUniverse = new SimpleUniverse(_canvas);
            simpleUniverse.getViewingPlatform().setNominalViewingTransform();
            _frame.setVisible(true);
        } else {
            container.add("Center",_canvas);
            _canvas.setSize(new Dimension(horizontalDimension,verticalDimension));
            if (simpleUniverse == null) simpleUniverse = new SimpleUniverse(_canvas);
            simpleUniverse.getViewingPlatform().setNominalViewingTransform();

            /* FIXME: experimental code for changing views
            TransformGroup VPTG = new TransformGroup();
            VPTG = simpleUniverse.getViewingPlatform().getMultiTransformGroup().getTransformGroup(0);
            Transform3D VPT3D = new Transform3D();
            //VPT3D.lookAt(new Point3d(0.0,0.0,10.0), new Point3d(0.0,0.0,0.0), new Vector3d(0.0,1.0,0.0));
            //VPT3D.setTranslation(new Vector3f(0.0f,0.0f,10.0f));
            //VPT3D.rotX(Math.PI/2.0);
            VPT3D.rotX(Math.PI/2);
            VPT3D.setTranslation(new Vector3f(0.0f,-10.0f,0.0f));

            VPTG.setTransform(VPT3D);
            */
        }
    }


    /*
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_iterationSynchronized)  {
            _canvas.swap();
            _canvas.startRenderer();
            _canvas.stopRenderer();
        }
    }

    public Canvas3D getCanvas() {
        return _canvas;
    }

    public BranchGroup getBranchGroup() {
        return branchRoot;
    }


    public void initialize() throws IllegalActionException {

        super.initialize();
        if (_canvas == null) {
            place(_container);
        }
        if (_frame != null) {
            _frame.setVisible(true);
        }
        if (simpleUniverse == null) simpleUniverse = new SimpleUniverse(_canvas);
        Enumeration branches = simpleUniverse.getLocale().getAllBranchGraphs();

        while (branches.hasMoreElements()) {
            BranchGroup branchGroup = (BranchGroup) branches.nextElement();
            if (branchGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
                simpleUniverse.getLocale().removeBranchGraph(branchGroup);
            }
        }

        branchRoot = new BranchGroup();
        branchRoot.setCapability(BranchGroup.ALLOW_DETACH);


        _userTransformation = new TransformGroup(_lastTransform);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        branchRoot.addChild(_userTransformation);

        bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        if (_isRotatable()) {
            mouseRotate = new MouseRotateView(this);
            mouseRotate.setTransformGroup(_userTransformation);
            mouseRotate.setSchedulingBounds(bounds);
            branchRoot.addChild(mouseRotate);
        }

        if (_isScalable()) {
            MouseZoom mouseZoom = new MouseZoom();
            mouseZoom.setTransformGroup(_userTransformation);
            mouseZoom.setSchedulingBounds(bounds);
            branchRoot.addChild(mouseZoom);
        }

        if (_isTranslatable()) {
    	    MouseTranslate mouseTranslate = new MouseTranslate();
            mouseTranslate.setTransformGroup(_userTransformation);
    	    _userTransformation.addChild(mouseTranslate);
            mouseTranslate.setSchedulingBounds(bounds);
        }

        // FIXME: should implement this so that user can dynamically
        // modify this value during design-time and run-time
        // right now this is only user-changable during initialization
        if (_isIterationSynchronized()) {
            _iterationSynchronized = true;
        } else {
            _iterationSynchronized = false;
        }

        if (_shouldShowAxes()) {
            Sphere origin = new Sphere((float) 0.05);
            _userTransformation.addChild(origin);
            Cylinder yAxis = new Cylinder((float)0.01,(float) 6.0);
            _userTransformation.addChild(yAxis);

            Cylinder xAxis = new Cylinder((float)0.01,(float) 6.0);
            Transform3D rotation = new Transform3D();
            Quat4d quat = new Quat4d();
            quat.set(new AxisAngle4d(0.0,0.0,1.0,Math.PI/2.0));
            rotation.set(quat);
            TransformGroup xAxisGroup = new TransformGroup(rotation);
            xAxisGroup.addChild(xAxis);
            _userTransformation.addChild(xAxisGroup);

            Cylinder zAxis = new Cylinder((float)0.01,(float) 6.0);
            Transform3D rotation2 = new Transform3D();
            Quat4d quat2 = new Quat4d();
            quat2.set(new AxisAngle4d(1.0,0.0,0.0,Math.PI/2.0));
            rotation2.set(quat2);
            TransformGroup zAxisGroup = new TransformGroup(rotation2);
            zAxisGroup.addChild(zAxis);
            _userTransformation.addChild(zAxisGroup);
        }


        // setup the lights
        BranchGroup lightRoot = new BranchGroup();

        AmbientLight lightA = new AmbientLight(new Color3f(0.8f,0.8f,0.8f));
        lightA.setInfluencingBounds(bounds);
        lightRoot.addChild(lightA);

        DirectionalLight lightD1 = new DirectionalLight();
        lightD1.setInfluencingBounds(bounds);
        Vector3f direction = new Vector3f(0.0f, -1.0f, -1.0f);
        direction.normalize();
        lightD1.setDirection(direction);
        lightD1.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        lightRoot.addChild(lightD1);


        simpleUniverse.getViewer().getView().setLocalEyeLightingEnable(true);
        simpleUniverse.addBranchGraph(lightRoot);

        if (_iterationSynchronized) {
            if (_canvas != null) _canvas.stopRenderer();
        }
    }


    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _userTransformation.getTransform(_lastTransform);
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
            _canvas.swap();
            if (mouseRotate != null) mouseRotate.stopped();
            _canvas.startRenderer();
        }
        _isSceneGraphInitialized = false;
    }


    public void addChild(Node node) {
        _userTransformation.addChild(node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /**
     */
    protected void _addChild(Node node) {
        _userTransformation.addChild(node);
    }


    /** The ViewScreen does not have an associated Java3D node
     *
     *  @return null
     */
    protected Node _getNodeObject() {
        return null;
    }

    /**
     */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();
        for(int i=0;i<width;i++) {
            ObjectToken objectToken = (ObjectToken) sceneGraphIn.get(i);
            Node node = (Node) objectToken.getValue();
            _addChild(node);
        }
        branchRoot.compile();
        simpleUniverse.addBranchGraph(branchRoot);
    }


    /** Start the internal Java3D renderer
     */
    protected void startRenderer() {
        if (_iterationSynchronized) {
            _canvas.startRenderer();
        }
    }


    /** Stop the internal Java3D renderer
     */
    protected void stopRenderer() {
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private int _getHorizontalResolution() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    private int _getVerticalResolution() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
    }

    private boolean _isRotatable() throws IllegalActionException  {
        return ((BooleanToken) rotatable.getToken()).booleanValue();
    }

    private boolean _isScalable() throws IllegalActionException  {
        return ((BooleanToken) scalable.getToken()).booleanValue();
    }

    private boolean _isTranslatable() throws IllegalActionException  {
        return ((BooleanToken) translatable.getToken()).booleanValue();
    }

    private boolean _isIterationSynchronized() throws IllegalActionException {
        return ((BooleanToken)
                 iterationSynchronized.getToken()).booleanValue();
    }



    private boolean _shouldShowAxes() throws IllegalActionException {
        return ((BooleanToken) showAxes.getToken()).booleanValue();
    }

    private class MouseRotateView extends MouseRotate {
        public MouseRotateView(ViewScreen viewContainer) {
            super();
            _viewContainer = viewContainer;
        }

        public void processStimulus(java.util.Enumeration criteria) {
            if (stopped !=true) {
                _viewContainer.startRenderer();

            }
            /* FIXME: experimental code for changing xforms
            double[] db = new double[16];

            db[0]=1.0;db[1]=db[2]=db[3]=0.0;
            db[4]=0.0;db[5]=1.0;db[6]=db[7]=0.0;
            db[8]=db[9]=0.0;db[10]=1.0;db[11]=0.0;
            db[12]=db[13]=db[14]=0.0;db[15]=1.0;
            currXform.set(db);
            */

            super.processStimulus(criteria);


            if (stopped !=true ) {
                _viewContainer.stopRenderer();
            }
        }

        public void stopped() {
            stopped = true;
        }

        /* FIXME experimental code for changing xforms
        public void transformChanged(Transform3D transform) {
            double[] db = new double[16];

            transform.get(db);
            for(int i=0;i<16;i++) {
                if (i%4 ==0) {
                    System.out.println(" ");
                }
                System.out.print(" "+db[i]);
            }
            System.out.println(" ");
            System.out.println("---------------------------------------------------------------");
            db[0]=1.0;db[1]=db[2]=db[3]=0.0;
            db[4]=0.0;db[5]=1.0;db[6]=db[7]=0.0;
            db[8]=db[9]=0.0;db[10]=1.0;db[11]=0.0;
            db[12]=db[13]=db[14]=0.0;db[15]=1.0;
            Transform3D td = new Transform3D();
            td.set(db);
            //currXform.set(db);
        }*/

        boolean stopped = false;
        ViewScreen _viewContainer;
    }

    private MouseRotateView mouseRotate;
    private Canvas3D _canvas;
    private SimpleUniverse simpleUniverse;
    private boolean _iterationSynchronized = false;
    // The main connection branch that connects to the universe
    private BranchGroup branchRoot;
    private Transform3D _lastTransform = new Transform3D();
    private TransformGroup _userTransformation = new TransformGroup();
    private Container _container;
    private JFrame _frame;
    private BoundingSphere bounds;
    GRDebug debug = new GRDebug(false);
}
