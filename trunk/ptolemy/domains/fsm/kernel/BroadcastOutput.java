/* An action sending a token to all connected receivers of a port.

 Copyright (c) 2000 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;

//////////////////////////////////////////////////////////////////////////
//// BroadcastOutput
/**
A BroadcastOutput action takes the token from evaluating the expression
specified by the <i>expression</i> string attribute and sends it to all
connected receivers of the port specified by the <i>portName</i> string
attribute. This action is a choice action contained by a transition in an
FSMActor, which will be called the associated FSMActor of this action.
The port with name specified by the <i>portName</i> attribute must be an
output port of the associated FSMActor, otherwise an exception will be
thrown when this action is executed. The scope of the specified expression
includes all the variables and parameters contained by the associated
FSMActor.

@author Xiaojun Liu
@version $Id$
@see Transition
@see FSMActor
*/
public class BroadcastOutput extends Action implements ChoiceAction {

    /** Construct a BroadcastOutput action with the given name contained
     *  by the specified transition. The transition argument must not be
     *  null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the transition for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. A variable for expression evaluation is
     *  created in the transition. The name of the variable is obtained
     *  by prepending an underscore to the name of this action.
     *  Increment the version of the workspace.
     *  @param transition The transition.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public BroadcastOutput(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
        expression = new StringAttribute(this, "expression");
        portName = new StringAttribute(this, "portName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying an expression. The token sent when this
     *  action is executed is obtained by evaluating the expression.
     *  The scope of the expression includes all the variables and
     *  parameters of the associated FSMActor of this action.
     */
    public StringAttribute expression = null;

    /** Attribute specifying the name of the output port. The port
     *  must be a port of the associated FSMActor, otherwise an
     *  exception will be thrown when this action is executed.
     */
    public StringAttribute portName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>portName</i> attribute, record the change but do not
     *  check whether the associated FSMActor has an output port with
     *  the specified name. If the changed attribute is the
     *  <i>expression</i> attribute, set the specified expression to the
     *  variable for expression evaluation.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == portName) {
            _portVersion = -1;
        }
        if (attribute == expression) {
            String expr = expression.getExpression();
            _evaluationVariable().setExpression(expr);
        }
    }

    /** Clone the action into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new action.
     *  @param workspace The workspace for the new action.
     *  @return A new action.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        BroadcastOutput newObject = (BroadcastOutput)super.clone(workspace);
        newObject.expression =
	        (StringAttribute)newObject.getAttribute("expression");
        newObject.portName =
	        (StringAttribute)newObject.getAttribute("portName");
        newObject._portVersion = -1;
        return newObject;
    }

    /** Take the token from evaluating the expression specified by the
     *  <i>expression</i> attribute and send it to all connected
     *  receivers of the port specified by the <i>portName</i> attribute.
     *  @exception IllegalActionException If expression evaluation fails,
     *   or the specified port is not found, or sending to one of the
     *   channels of the port throws a NoRoomException.
     */
    public void execute() throws IllegalActionException {
        IOPort port = _getPort();
        try {
            port.broadcast(_evaluationVariable().getToken());
        } catch (NoRoomException ex) {
            throw new IllegalActionException(this, "Cannot complete "
                    + "action: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the port specified by the <i>portName</i> attribute.
     *  This method is read-synchronized on the workspace.
     *  @return The specified port.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have an output port with the specified name.
     */
    protected IOPort _getPort() throws IllegalActionException {
        if (_portVersion == workspace().getVersion()) {
            return _port;
        }
        try {
            workspace().getReadAccess();
            FSMActor fsm = (FSMActor)getContainer().getContainer();
            String name = portName.getExpression();
            IOPort port = (IOPort)fsm.getPort(name);
            if (port == null) {
                throw new IllegalActionException(fsm, this, "Cannot find "
                        + "port with name: " + name);
            }
            if (!port.isOutput()) {
                throw new IllegalActionException(fsm, this, "The specified "
                        + "port is not an output port.");
            }
            _port = port;
            _portVersion = workspace().getVersion();
            return _port;
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached reference to the specified port.
    private IOPort _port;

    // Version of reference to the specified port.
    private long _portVersion = -1;

}
