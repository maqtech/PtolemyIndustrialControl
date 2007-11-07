/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.gt;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.gt.GTIngredientsEditor;

//////////////////////////////////////////////////////////////////////////
//// CompositeActorMatcher

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class CompositeActorMatcher extends TypedCompositeActor
implements GTEntity {

    public CompositeActorMatcher(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        setClassName("ptolemy.actor.gt.CompositeActorMatcher");

        criteria = new GTIngredientsAttribute(this, "criteria");
        criteria.setExpression("");

        operations = new GTIngredientsAttribute(this, "operations");
        operations.setExpression("");

        patternObject = new PatternObjectAttribute(this, "patternObject");
        patternObject.setExpression("");

        editorFactory = new GTIngredientsEditor.Factory(this, "editorFactory");
    }

    public GTIngredientsAttribute getCriteriaAttribute() {
        return criteria;
    }

    public GTIngredientsAttribute getOperationsAttribute() {
        return operations;
    }

    public PatternObjectAttribute getPatternObjectAttribute() {
        return patternObject;
    }

    public void updateAppearance(GTIngredientsAttribute attribute) {

    }

    public GTIngredientsAttribute criteria;

    public GTIngredientsEditor.Factory editorFactory;

    public GTIngredientsAttribute operations;

    public PatternObjectAttribute patternObject;

    private static final long serialVersionUID = -3093694369352820033L;

}
