/* A variable to encapsulate the NamedObj that contains itself.

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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



 */

package ptolemy.actor.gt;

import java.util.List;

import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 A variable to encapsulate the NamedObj that contains itself. This variable is
 automatically generated by the expression evaluator when a name resolves to a
 NamedObj which is not a variable. In that case, one such variable is generated
 in the resolved NamedObj, whose value is an {@link ObjectToken} containing the
 NamedObj as its value.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class NamedObjVariable extends Variable {

    /** Construct a variable with a generated name as an attribute of the
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @exception IllegalActionException If the container does not accept
     *   a variable as its attribute.
     *  @exception NameDuplicationException If the name coincides with a
     *   variable already in the container.
     */
    public NamedObjVariable(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        this(container, container.uniqueName(NAME_PREFIX));
    }

    /** Construct a variable with the given name as an attribute of the
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @param name The name of the variable.
     *  @exception IllegalActionException If the container does not accept
     *   a variable as its attribute.
     *  @exception NameDuplicationException If the name coincides with a
     *   variable already in the container.
     */
    public NamedObjVariable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setToken(new ObjectToken(container));
        _setTokenWithContainer = true;
        setPersistent(false);
    }

    /** Get the NamedObjVariable contained in the container, and create a new
     *  one if none is found in the container and autoCreate is true.
     *
     *  @param container The container.
     *  @param autoCreate Whether a NamedObjVariable should be created if none
     *   is found.
     *  @return The NamedObjVariable, or null if none is found and autoCreate is
     *   false.
     *  @exception IllegalActionException If variable of the container cannot be
     *   retrieved, or a new one cannot be created.
     */
    public static NamedObjVariable getNamedObjVariable(NamedObj container,
            boolean autoCreate) throws IllegalActionException {
        try {
            container.workspace().getReadAccess();
            List<?> attributes = container
                    .attributeList(NamedObjVariable.class);
            if (attributes.isEmpty()) {
                if (autoCreate) {
                    try {
                        return new NamedObjVariable(container);
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException(e);
                    }
                } else {
                    return null;
                }
            } else {
                return (NamedObjVariable) attributes.get(0);
            }
        } finally {
            container.workspace().doneReading();
        }
    }

    /** Specify the container, and add this variable to the list
     *  of attributes in the container. If this variable already has a
     *  container, remove this variable from the attribute list of the
     *  current container first. Otherwise, remove it from the directory
     *  of the workspace, if it is there. If the specified container is
     *  null, remove this variable from the list of attributes of the
     *  current container. If the specified container already contains
     *  an attribute with the same name, then throw an exception and do
     *  not make any changes. Similarly, if the container is not in the
     *  same workspace as this variable, throw an exception. If this
     *  variable is already contained by the specified container, do
     *  nothing.
     *  <p>
     *  If this method results in a change of container (which it usually
     *  does), then remove this variable from the scope of any
     *  scope dependent of this variable.
     *  <p>
     *  This method is write-synchronized on the workspace and increments
     *  its version number.
     *  @param container The proposed container of this variable.
     *  @exception IllegalActionException If the container will not accept
     *   a variable as its attribute, or this variable and the container
     *   are not in the same workspace, or the proposed container would
     *   result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this variable.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        NamedObj oldContainer = getContainer();
        super.setContainer(container);
        if (_setTokenWithContainer && container != oldContainer) {
            setToken(new ObjectToken(container));
        }
    }

    /** Put a new token in this variable and notify the container and
     *  and value listeners. The token must be an ObjectToken containing the
     *  container of this variable.
     *  If an expression had been
     *  previously given using setExpression(), then that expression
     *  is forgotten. If the type of this variable has been set with
     *  setTypeEquals(), then convert the specified token into that
     *  type, if possible, or throw an exception, if not.  If
     *  setTypeAtMost() has been called, then verify that its type
     *  constraint is satisfied, and if not, throw an exception.
     *  <br>Note that you can call this with a null argument regardless
     *  of type constraints, unless there are other variables that
     *  depend on its value.
     *  <br>Note that {@link #setPersistent(boolean) setPersistent(true}}
     *  may need to be called so that the change to the token is
     *  marked as persistent and is exported.
     *  to the token is expor
     *  @param token The new token to be stored in this variable.
     *  @exception IllegalActionException If the token type is not
     *   compatible with specified constraints, or if you are attempting
     *   to set to null a variable that has value dependents, or if the
     *   container rejects the change.
     *  @see #getToken()
     */
    @Override
    public void setToken(Token token) throws IllegalActionException {
        if (token instanceof ObjectToken) {
            if (!getContainer().equals(((ObjectToken) token).getValue())) {
                throw new IllegalActionException("The NamedObj in the token "
                        + "is not equal to the container of this variable.");
            }
            super.setToken(token);
        } else {
            throw new IllegalActionException("Only instances of NamedObjToken "
                    + "are allowed as argument of setToken().");
        }
    }

    /** Prefix of the names of any automatically generated NamedObjVariable.
     */
    public static final String NAME_PREFIX = "namedObjVariable";

    /** Whether the token should be set as the container. This should be false
     *  only before the local constructor is invoked. After that, this variable
     *  should always be true.
     */
    private boolean _setTokenWithContainer = false;
}
