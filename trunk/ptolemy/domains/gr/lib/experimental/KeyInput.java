/* An actor that listens for keys pressed on the viewscreen

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
package ptolemy.domains.gr.lib.experimental;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.gr.kernel.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class KeyInput extends GRActor {

    public KeyInput(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.GENERAL);
        keycode = new TypedIOPort(this, "keycode");
        keycode.setOutput(true);
        keycode.setTypeEquals(BaseType.INT);
    }
    
    public TypedIOPort output;
    public TypedIOPort keycode;
    
   
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        KeyInput newObject = (KeyInput) super.clone(workspace);
        
        newObject.output = (TypedIOPort) newobj.getPort("output");
        newObject.keycode = (TypedIOPort) newobj.getPort("keycode");
        return newObject;
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
  	    userInputNode = new BranchGroup();
  	    _react = new _React();
  	    _react.setSchedulingBounds(new BoundingSphere());
  	    userInputNode.addChild(_react);
  	    _hasData = false;
    }
    
    public Node getNodeObject() {
        return (Node) userInputNode;
    }
    
    public void fire() throws IllegalActionException  {
        if (_hasData) {
            keycode.send(0, new IntToken((int)_keycode));
            System.out.print(" "+(int)_keycode+",");
            _hasData = false;
        }
    }
    
    private class _React extends Behavior {
        
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
              	for (int i=0; i<event.length; i++) { 
	                eventId = event[i].getID();
	                if (eventId == KeyEvent.KEY_PRESSED) {
	                    _keycode = ((KeyEvent) event[i]).getKeyChar();
	                    _hasData = true;
	                }
	                /*
	                if (eventId == MouseEvent.MOUSE_CLICKED) {
            	        _xClicked = ((MouseEvent)event[i]).getX();
                	    _yClicked = ((MouseEvent)event[i]).getY();
                	    _hasData = true;
                	    System.out.println(" "+_xClicked+" "+_yClicked);
                	}*/
                }
            }
            this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
        }
    }

    protected _React _react;
    protected BranchGroup userInputNode;
    boolean _hasData;
    char _keycode;
}
