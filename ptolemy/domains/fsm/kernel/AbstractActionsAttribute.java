/* A base class for actions with semicolon delimited lists of commands.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.NoRoomException;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// AbstractActionsAttribute
/**
A base class for actions with semicolon delimited lists of commands.
<p>
The value of this attribute is a semicolon separated list of commands,
where each command gives a destination to send data to and a value
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
     <i>name</i>
</pre>
or
<pre>
     <i>name</i>(<i>number</i>)
</pre>
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
public abstract class AbstractActionsAttribute extends Action {

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
    public AbstractActionsAttribute(Transition transition, String name)
            throws IllegalActionException, NameDuplicationException {
        super(transition, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** For each destination identified in an action, find the corresponding
     *  Ptolemy II object (a variable or a port).
     *  @exception IllegalActionException If a destination is not found.
     */
    public void execute() throws IllegalActionException {
        if (_destinationsListVersion != workspace().getVersion()) {
            _updateDestinations();
        }
        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }
        if (_scope == null) {
            _scope = new ActionScope();
        }
    }

    /** Return the channel number associated with the given name, assuming
     *  that the destination is a port object.
     *  @exception IllegalActionException If the name does not refer to a
     *  port, or a channel has not been specified for the name.
     */
    public int getChannel(String name)
            throws IllegalActionException {
        Integer integer = (Integer)_numbers.get(
                _destinationNames.indexOf(name));
       if (integer == null) {
           throw new IllegalActionException(
                   "No channel was specified for " + name);
       }
       return integer.intValue();
    }

    /** Return the destination object referred to by the given name.
     *  Depending on the subclass of this class, this might be a variable,
     *  or an output port.
     *  @exception IllegalActionException If the given name is not a valid
     *  destination for this action.
     */
    public NamedObj getDestination(String name)
            throws IllegalActionException {
        return _getDestination(name);
    }

    /** Return the list of destination names given in expression set
     *  for this attribute.  If no destinations are specified, then return
     *  an empty list.
     */
    public List getDestinationNameList() {
        if (_destinationNames == null) {
            return new LinkedList();
        } else {
            return Collections.unmodifiableList(_destinationNames);
        }
    }

    /** Return the expression referred to by the given name.  When the
     *  action is executed, this expression will be evaluated and
     *  assigned to the object associated with the name.
     */
    public String getExpression(String name) {
        ParseTreeWriter writer = new ParseTreeWriter();
        return writer.printParseTree((ASTPtRootNode)
                _parseTrees.get(_destinationNames.indexOf(name)));
    }

    /** Test if a channel number is associated with the given name.
     *  @return true If a channel was specified.
     */
    public boolean isChannelSpecified(String name) {
        Integer integer = (Integer)_numbers.get(
                _destinationNames.indexOf(name));
        return integer != null;
    }

    /** Set the action and notify the container
     *  that the action has changed by calling attributeChanged(),
     *  and notify any listeners that have
     *  been registered using addValueListener().
     *  @param expression The action.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container, or if the action is syntactically incorrect.
     */
    public void setExpression(String expression)
            throws IllegalActionException {
        super.setExpression(expression);

        // This is important for InterfaceAutomata which extend from
        // this class.
        if (expression == null ||
            expression.trim().equals("")) return;

        // Initialize the lists that store the commands to be executed.
        _destinationNames = new LinkedList();
        _numbers = new LinkedList();
        _parseTrees = new LinkedList();

        // Indicate that the _destinations list is invalid.  We defer
        // determination of the destinations because the destinations
        // may not have been created yet.
        _destinationsListVersion = -1;

        PtParser parser = new PtParser();
        Map map = parser.generateAssignmentMap(expression);
        for (Iterator names = map.keySet().iterator();
            names.hasNext();) {
            String name = (String)names.next();
            ASTPtAssignmentNode node = (ASTPtAssignmentNode)map.get(name);

            // Parse the destination specification first.
            String completeDestinationSpec = node.getIdentifier();
            int openParen = completeDestinationSpec.indexOf("(");
            if (openParen > 0) {
                // A channel is being specified.
                int closeParen = completeDestinationSpec.indexOf(")");
                if (closeParen < openParen) {
                    throw new IllegalActionException(this,
                            "Malformed action: expected destination = "
                            + "expression. Got: " + completeDestinationSpec);
                }
                _destinationNames.add(
                        completeDestinationSpec.substring(
                                0, openParen).trim());
                String channelSpec = completeDestinationSpec.substring(
                        openParen + 1, closeParen);
                try {
                    _numbers.add(new Integer(channelSpec));
                } catch (NumberFormatException ex) {
                    throw new IllegalActionException(this,
                            "Malformed action: expected destination = "
                            + "expression. Got: " + completeDestinationSpec);
                }
            } else {
                // No channel is specified.
                _destinationNames.add(completeDestinationSpec);
                _numbers.add(null);
            }

            // Parse the expression
            _parseTrees.add(node.getExpressionTree());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a destination name, return a NamedObj that matches that
     *  destination.  An implementation of this method should never return
     *  null (throw an exception instead).
     *  @param name The name of the destination, or null if none is found.
     *  @return An object (like a port or a variable) with the specified name.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    protected abstract NamedObj _getDestination(String name)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // List of channels.
    protected List _numbers;

    // List of destinations.
    protected List _destinations;

    // List of destination names.
    protected List _destinationNames;

    // The workspace version number when the _destinations list is last
    // updated.
    protected long _destinationsListVersion = -1;

    // The list of parse trees.
    protected List _parseTrees;

    // The parse tree evaluator.
    protected ParseTreeEvaluator _parseTreeEvaluator;

    // The scope.
    protected ActionScope _scope;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  For each destination in the _destinationNames list,
     *  create a corresponding
     *  entry in the _destinations list that refers to the destination.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    private void _updateDestinations() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            FSMActor fsm = (FSMActor)getContainer().getContainer();
            if (_destinationNames != null) {
                _destinations = new LinkedList();
                Iterator destinationNames = _destinationNames.iterator();
                while (destinationNames.hasNext()) {
                    String destinationName = (String)destinationNames.next();
                    NamedObj destination = _getDestination(destinationName);
                    _destinations.add(destination);
                }
            }

            _destinationsListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }


    private class ActionScope extends ModelScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            Variable result = getScopedVariable(
                    null,
                    (NamedObj)AbstractActionsAttribute.this, name);
            if (result != null) {
                return result.getToken();
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            Variable result = getScopedVariable(
                    null,
                    (NamedObj)AbstractActionsAttribute.this, name);
            if (result != null) {
                return result.getType();
            } else {
                return null;
            }
        }

        /** Return the list of variables within the scope.
         *  Note that this method is an extremely inefficient to refer
         *  to the scope of a variable because it constructs a list containing
         *  every variable in the scope.  It is best to avoid calling it
         *  and instead just use the get() method.
         *  @return The list of variables within the scope.
         */
        public NamedList variableList() {
            return null;
        }
    }
}
