/* An action that sends outputs or sets variable values.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// CommitActionsAttribute
/**
An action that sends outputs to one or more ports, or sets the values
of variables either in the containing FSMActor or in a state refinement.
This action is contained by a transition,
and is evaluated whenever that transition is taken.  The evaluation
is done in the postfire() method of the FSMActor that contains the
transition (hereafter called "the FSM actor").
To specify an action that is executed earlier, in the fire()
method, when the transition is enabled rather than taken,
use the class OutputActionsAttribute.
<p>
The value of this attribute is a semicolon separated list of commands,
where each command gives a destination and a value.
The actions are given by calling setExpression() with
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
or
<pre>
     <i>variableName</i>
</pre>
Here, <i>portName</i> is the name of a port of the FSM actor,
If no <i>channelNumber</i> is given, then the value
is broadcast to all channels of the port.
Also, <i>variableName</i> is either a variable or parameter of
the FSM actor, or a variable or parameter of a refinement state.
To give a variable of a refinement state, use a dotted name,
as follows:
<pre>
     <i>refinementStateName</i>.<i>variableName</i>
</pre>
If destination name is given where there is both a port and a variable
with that name, then the port will be used.
<p>
The <i>expression</i> is a string giving an expression in the usual
Ptolemy II expression language.  The expression may include references
to variables and parameters contained by the FSM actor.

@author Xiaojun Liu and Edward A. Lee
@version $Id$
@see CommitActionsAttribute
@see Transition
@see FSMActor
*/
public class CommitActionsAttribute
        extends AbstractActionsAttribute implements CommitAction {

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
    public CommitActionsAttribute(Transition transition, String name)
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
                Variable variable = (Variable)variables.next();
                NamedObj nextDestination = (NamedObj)destinations.next();
                // Need to get the next channel even if it's not used.
                Integer channel = (Integer)channels.next();
                if (nextDestination instanceof IOPort) {
                    IOPort destination = (IOPort)nextDestination;
                    try {
                        if (channel != null) {
                            destination.send(channel.intValue(),
                                    variable.getToken());
                        } else {
                            destination.broadcast(variable.getToken());
                        }
                    } catch (NoRoomException ex) {
                        throw new IllegalActionException(this,
                        "Cannot complete action: " + ex.getMessage());
                    }
                } else if (nextDestination instanceof Variable) {
                    ((Variable)nextDestination).setToken(variable.getToken());
                } else {
                    throw new IllegalActionException(this,
                    "Destination is neither an IOPort nor a Variable: "
                    + nextDestination.getFullName());
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
            // No port found.  Try for a variable.
            Attribute var = fsm.getAttribute(name);
            if (var == null) {
                // Try for a refinement variable.
                int period = name.indexOf(".");
                if (period > 0) {
                    String refinementName = name.substring(0, period);
                    String entryName = name.substring(period + 1);
                    // FIXME: Look in the container of the fsm???
                    // Below we look for an attribute only in the fsm
                    // itself.
                    Nameable fsmContainer = fsm.getContainer();
                    if (fsmContainer instanceof CompositeEntity) {
                        Entity refinement = ((CompositeEntity)fsmContainer)
                                .getEntity(refinementName);
                        if (refinement != null) {
                            Attribute entry
                                    = refinement.getAttribute(entryName);
                            if (entry instanceof Variable) {
                                return entry;
                            }
                        }
                    }
                }
                throw new IllegalActionException(fsm, this,
                "Cannot find port or variable with the name: " + name);
            } else {
                if (!(var instanceof Variable)) {
                    throw new IllegalActionException(fsm, this,
                    "The attribute with name \""
                    + name
                    + "\" is not an "
                    + "instance of Variable.");
                }
                return var;
            }
        } else {
            if (!port.isOutput()) {
                throw new IllegalActionException(fsm, this,
                "The port is not an output port: "
                + name);
            }
            return port;
        }
    }
}
