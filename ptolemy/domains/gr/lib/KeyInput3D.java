/* An actor that listens for keys pressed on the viewscreen

 Copyright (c) 2000-2003 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.gr.kernel.GRActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

/**
An actor that listens for keys pressed on the viewscreen.
@author C. Fong
@version $Id$
@since Ptolemy II 1.0
*/
public class KeyInput3D extends GRActor {

    public KeyInput3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        keycode = new TypedIOPort(this, "keycode");
        keycode.setOutput(true);
        keycode.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    public TypedIOPort keycode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException  {
        super.fire();
        if (_hasData) {
            keycode.send(0, new IntToken((int)_keycode));
            // FIXME: uncomment for debugging
            //System.out.print(" "+(int)_keycode+",");
            _hasData = false;
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        userInputNode = new BranchGroup();
        _react = new React();
        _react.setSchedulingBounds(new BoundingSphere());
        userInputNode.addChild(_react);
        _hasData = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    protected Node _getNodeObject() {
        return (Node) userInputNode;
    }

    protected void _makeSceneGraphConnection() throws IllegalActionException {
        if (_root == null) {
            throw new IllegalActionException(
                    "GR error: no ViewScreen actor");
        } else {
            ViewScreen viewScreen = (ViewScreen) _root;
            viewScreen.addChild(_getNodeObject());
        }
    }




    private class React extends Behavior {

        public void initialize() {
            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
        }

        public void processStimulus(Enumeration criteria) {
            WakeupCriterion wakeup;
            int eventId;
            AWTEvent[] event;

            while (criteria.hasMoreElements()) {
                wakeup = (WakeupCriterion) criteria.nextElement();
                event = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
                      for (int i = 0; i < event.length; i++) {
                    eventId = event[i].getID();
                    if (eventId == KeyEvent.KEY_PRESSED) {
                        _keycode = ((KeyEvent) event[i]).getKeyChar();
                        _hasData = true;
                    }
                }
            }
            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
        }
    }

    protected BranchGroup userInputNode;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private React _react;
    private boolean _hasData;
    private char _keycode;
}
