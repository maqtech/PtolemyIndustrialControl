/* A password source.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import ptolemy.actor.lib.Source;
import ptolemy.actor.gui.style.PasswordStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Password
/**
Produce a password output. The value of the
output is that of the token contained by the <i>password</i> parameter,
which by default is an StringToken with value "foo". 

<p>The password itself is not persistent, so each time the model
is opened, the password is set to the default initial value.

@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class Password extends Source {

    /** Construct a password source with the given container and name.
     *  Create the <i>password</i> parameter, initialize its value to
     *  the default value of an IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Password(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        isPersistent = new Parameter(this, "isPersistent",
                new BooleanToken(false));
        isPersistent.setTypeEquals(BaseType.BOOLEAN);                

        password = new StringParameter(this, "password");
        password.setExpression("foo");
        password.setPersistent(false);
        new PasswordStyle(password, "style");

        output.setTypeEquals(BaseType.STRING);
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Set to true if the parameter is persistent and will be saved
     *  when the model is written out.  For security reasons, the default
     *  is false, which means that the password will not be save in
     *  clear text when the model is saved.
     */
    public Parameter isPersistent;

    /** The value produced by this password source.
     *  By default, it contains a String  with value "foo".
     */
    public StringParameter password;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reinitialize the state if
     *  the <i>isPersistent</i> parameter is changed.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == isPersistent) {
            password.setPersistent(((BooleanToken)isPersistent
                                        .getToken()).booleanValue());
        } else {
            super.attributeChanged(attribute);
        }
    }
    /** Send the token in the <i>password</i> parameter to the output.
     *  @exception IllegalActionException If it is thrown by the
     *   send() method sending out the token.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, password.getToken());
    }
}
