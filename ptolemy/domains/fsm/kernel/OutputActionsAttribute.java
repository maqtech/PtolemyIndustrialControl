/* An action that sends outputs.

 Copyright (c) 2000-2002 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.UnknownResultException;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// OutputActionsAttribute
/**
An action that sends outputs to one or more ports.
This action is contained by a transition,
and is evaluated whenever that transition becomes enabled.  The evaluation
is done in the fire() method of the FSMActor that contains the
transition (hereafter called "the FSM actor").
Note that the fire() method may be invoked more than once in an
iteration, particularly in domains where there is iteration to a fixed point,
such as CT.  To specify an action that is executed only when the
transition is taken (in the postfire() method), use the class
CommitActionsAttribute.
<p>
The value of this attribute is a semicolon separated list of commands,
where each command gives a destination port to send data to and a value
to send. The actions are given by calling setExpression() with
a string of the form:
<pre>
     <i>command</i>; <i>command</i>; ...
</pre>
where each <i>command</i> has the form:
<pre>
     <i>destination</i> = <i>expression</i>
</pre>
where <i>destination</i> is either
<pre>
     <i>portName</i>
</pre>
or
<pre>
     <i>portName</i>(<i>channelNumber</i>)
</pre>
where <i>portName</i> is the name of a port of the FSM actor.
If no <i>channelNumber</i> is given, then the value
is broadcast to all channels of the port.
<p>
The <i>expression</i> is a string giving an expression in the usual
Ptolemy II expression language.  The expression may include references
to variables and parameters contained by the FSM actor.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see CommitActionsAttribute
@see Transition
@see FSMActor
*/
public class OutputActionsAttribute
        extends AbstractActionsAttribute implements ChoiceAction {

    /** Construct an action with the given name contained
     *  by the specified transition. The <i>transition</i> argument must not
     *  be null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the transition for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. A variable for expression evaluation is
     *  created in the transition. The name of the variable is obtained
     *  by prepending an underscore to the name of this action.
     *  This increments the version of the workspace.
     *  @param transition The transition that contains this action.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the transition already
     *   has an attribute with the name or that obtained by prepending
     *   an underscore to the name.
     */
    public OutputActionsAttribute(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send tokens to the designated outputs.  Each token is determined
     *  by evaluating the expression portion of the action.
     *  @exception IllegalActionException If expression evaluation fails,
     *   or the specified port is not found, or sending to one of the
     *   channels of the port throws a NoRoomException.
     */
    public void execute() throws IllegalActionException {
        super.execute();
        if (_destinations != null) {
            Iterator destinations = _destinations.iterator();
            Iterator channels = _numbers.iterator();
            Iterator variables = _variables.iterator();
            while (destinations.hasNext()) {
                NamedObj nextDestination = (NamedObj)destinations.next();
                if (!(nextDestination instanceof IOPort)) {
                    throw new IllegalActionException(this,
                    "Destination is not an IOPort: "
                    + nextDestination.getFullName());
                }
                IOPort destination = (IOPort)nextDestination;
                Integer channel = (Integer)channels.next();
                Variable variable = (Variable)variables.next();
                try {
                    Token token = variable.getToken();
                    if (token != null) {
                        if (channel != null) {
                            destination.send(channel.intValue(),token);
                        if (_debugging)
                            _debug(getFullName()+" port: "+
                                   destination.getName()+" channel: "+
                                   channel.intValue()+", token: "+token);
                        } else {
                            destination.broadcast(token);
                            if (_debugging)
                                _debug(getFullName()+" port: "+
                                       destination.getName()+" token: "+token);
                        }
                    }
                } catch (NoRoomException ex) {
                    throw new IllegalActionException(this,
                    "Cannot complete action: " + ex.getMessage());
                } catch (UnknownResultException ex) {
                    // Produce no output.
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a destination name, return a NamedObj that matches that
     *  destination.
     *  @param name The name of the destination, or null if none is found.
     *  @return An object (like a port or a variable) with the specified name.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    protected NamedObj _getDestination(String name)
            throws IllegalActionException {
        Transition transition = (Transition)getContainer();
        if (transition == null) {
            throw new IllegalActionException(this,
            "Action has no container transition.");
        }
        Entity fsm = (Entity)transition.getContainer();
        if (fsm == null) {
            throw new IllegalActionException(this, transition,
            "Transition has no container.");
        }
        IOPort port = (IOPort)fsm.getPort(name);
        if (port == null) {
            throw new IllegalActionException(fsm, this,
            "Cannot find port with name: " + name);
        }
        if (!port.isOutput()) {
            throw new IllegalActionException(fsm, this,
            "The port is not an output port: "
            + name);
        }
        return port;
    }
}
