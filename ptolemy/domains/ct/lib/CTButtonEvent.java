/* A event generator that responses to button clicks.

 Copyright (c) 1999 The Regents of the University of California.
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

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTButtonEvent
/**
This is not an event generator. It outputs a continuous signal with value
true or false. At the beginning of the execution it outputs false. If the 
parameter "Button" is changed, it will output true in the next iteration.
I.e. there is, at most, one step size delay of responding the button event.
The true value will be kept for one iteration, after that the output goes
back to false.   
@author  Jie Liu
@version $Id$

*/
public class CTButtonEvent extends TypedAtomicActor {
    /** Construct the actor. One output of type boolean
     *  The default output value is false.
     * @param container The TypedCompositeActor this star belongs to
     * @param name The name.
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public CTButtonEvent(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setInput(false);
        output.setOutput(true);
        output.setTypeEquals(BooleanToken.class);
        _buttonClicked = false;
        _paramButtonClicked = new Parameter(this, "ButtonClicked",
                new BooleanToken(_buttonClicked));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output button clicked value
     */
    public void fire() throws IllegalActionException {
        output.broadcast(new BooleanToken(_buttonClicked));
    }

    /** Always return true, and reset button clicked to false.
     *  @return True
     */
    public boolean postfire(){
        _buttonClicked = false;
        return true;
    }

    /** Update the parameter if it has been changed.
     *  The new parameter will be used only after this method is called.
     */
    public void updateParameters() throws IllegalActionException{
        boolean b = 
                ((BooleanToken)_paramButtonClicked.getToken()).booleanValue();
        if (!_buttonClicked && b) {
            _buttonClicked = true;
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
 
    public TypedIOPort output;
 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private Parameter _paramButtonClicked;
    private boolean _buttonClicked;
}
