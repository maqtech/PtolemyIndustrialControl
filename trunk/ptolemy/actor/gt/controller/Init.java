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

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Init

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Init extends GTEvent {

    public Init(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isInitialEvent.setToken(BooleanToken.TRUE);
        isInitialEvent.setVisibility(Settable.NONE);
        modelName = new StringParameter(this, "modelName");
    }

    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        if (attribute == modelName) {
            _emptyModel = new TypedCompositeActor(new Workspace());
            try {
                _emptyModel.setName(modelName.stringValue());
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Unexpected error.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    public void fire(ArrayToken arguments) throws IllegalActionException {
        ParserScope scope = _getParserScope(arguments);
        actions.execute(scope);

        ModelAttribute modelAttribute = getModelAttribute();
        if (modelAttribute.getModel() == null) {
            modelAttribute.setModel(_getInitialModel());
            getMatchedParameter().setToken(BooleanToken.getInstance(true));
        }
    }

    public StringParameter modelName;

    protected CompositeEntity _getInitialModel() throws IllegalActionException {
        try {
            return (CompositeEntity) _emptyModel.clone(new Workspace());
        } catch (CloneNotSupportedException e) {
            throw new IllegalActionException("Unable to clone an empty model.");
        }
    }

    private CompositeEntity _emptyModel;
}
