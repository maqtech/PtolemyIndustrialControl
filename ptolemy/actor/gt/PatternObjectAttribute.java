 /*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PatternObjectAttribute extends StringAttribute
implements ValueListener {

    /**
     *
     */
    public PatternObjectAttribute() {
        _init();
    }

    /**
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public PatternObjectAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     */
    public PatternObjectAttribute(Workspace workspace) {
        super(workspace);
        _init();
    }

    public void valueChanged(Settable settable) {
        if (settable == this) {
            NamedObj container = getContainer();
            if (GTTools.isInReplacement(container)) {
                // Update the ports with the criteria attribute of the
                // corresponding actor in the pattern of the transformation
                // rule.
                NamedObj correspondingEntity =
                    GTTools.getCorrespondingPatternObject(container);
                if (correspondingEntity != null) {
                    GTIngredientsAttribute criteria;
                    try {
                        criteria = (GTIngredientsAttribute)
                                container.getAttribute("criteria",
                                        GTIngredientsAttribute.class);
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                    if (criteria != null) {
                        if (container instanceof GTEntity) {
                            criteria.setPersistent(false);
                            try {
                                criteria.setExpression("");
                            } catch (IllegalActionException e) {
                                // Ignore because criteria is not used for
                                // patternObject.
                            }
                        } else {
                            try {
                                criteria.setContainer(null);
                            } catch (KernelException e) {
                                throw new InternalErrorException(e);
                            }
                        }
                    }
                    if (container instanceof GTEntity
                            && correspondingEntity instanceof GTEntity) {
                        ((GTEntity) container).updateAppearance(
                                ((GTEntity) correspondingEntity)
                                .getCriteriaAttribute());
                    }
                }
            }
        }
    }

    private void _init() {
        setClassName("ptolemy.actor.gt.PatternObjectAttribute");
        setVisibility(EXPERT);
        addValueListener(this);
    }

}
