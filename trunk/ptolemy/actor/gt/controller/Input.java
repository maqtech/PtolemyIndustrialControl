/*

 Copyright (c) 2008 The Regents of the University of California.
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

*/
package ptolemy.actor.gt.controller;

import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ParserScope;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Input

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Input extends GTEvent {

    public Input(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fireOnInput.setToken(BooleanToken.TRUE);
    }

    public void fire(ArrayToken arguments) throws IllegalActionException {
        super.fire(arguments);

        ParserScope scope = _getParserScope();
        BooleanToken inputPortPresent = (BooleanToken) scope.get(
                _INPUT_PORT_NAME + "_isPresent");
        if (inputPortPresent != null && inputPortPresent.booleanValue()) {
            ActorToken modelToken = (ActorToken) scope.get(_INPUT_PORT_NAME);
            Entity entity = modelToken.getEntity();
            if (!(entity instanceof CompositeEntity)) {
                throw new IllegalActionException("Only instances of " +
                        "CompositeEntity are accepted in the input " +
                        "ActorTokens to the transformation controller.");
            }
            getModelParameter().setModel((CompositeEntity) entity);
        }
    }

    public void scheduleEvents() throws IllegalActionException {
        ParserScope scope = _getParserScope();
        BooleanToken inputPortPresent = (BooleanToken) scope.get(
                _INPUT_PORT_NAME + "_isPresent");
        if (inputPortPresent != null && inputPortPresent.booleanValue()) {
            super.scheduleEvents();
        }
    }

    private static final String _INPUT_PORT_NAME = "modelInput";
}
