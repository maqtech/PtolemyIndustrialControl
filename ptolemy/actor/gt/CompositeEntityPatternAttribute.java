/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLChangeRequest;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class CompositeEntityPatternAttribute
extends SingletonAttribute {

    public CompositeEntityPatternAttribute(NamedObj container, String name,
            String parameterName, Type type, String defaultExpression)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        parameter = new Parameter(this, parameterName);
        parameter.setTypeEquals(type);
        parameter.setExpression(defaultExpression);

        _attachText("_iconDescription", _LIBRARY_ICON);
    }

    public Parameter parameter;

    protected void _checkContainerClass(
            Class<? extends CompositeEntity> containerClass, boolean deep)
    throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof EntityLibrary) {
            return;
        }

        while (deep && container != null
                && !containerClass.isInstance(container)) {
            container = container.getContainer();
        }

        if (container == null || !containerClass.isInstance(container)) {
            _deleteThis();
            throw new IllegalActionException(getClass().getSimpleName()
                    + " can only be added to " + containerClass.getSimpleName()
                    + ".");
        }
    }

    protected void _checkUniqueness() throws IllegalActionException {
        NamedObj container = getContainer();
        if (container instanceof EntityLibrary) {
            return;
        }

        List<?> attributeList = getContainer().attributeList(getClass());
        for (Object attributeObject : attributeList) {
            if (attributeObject != this) {
                _deleteThis();
                throw new IllegalActionException("Only 1 "
                        + getClass().getSimpleName() + " can be used.");
            }
        }
    }

    protected void _setIconDescription(String iconDescription) {
        String moml = "<property name=\"_iconDescription\" class="
            + "\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
            + "  <configure>" + iconDescription + "</configure>"
            + "</property>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
        request.execute();
    }

    protected static final String _LIBRARY_ICON =
        "<svg>"
        + "<rect x=\"0\" y=\"0\" width=\"30\" height=\"20\""
        + "  style=\"fill:#00FFFF\"/>"
        + "</svg>";

    private void _deleteThis() {
        String moml = "<deleteProperty name=\"" + getName() + "\"/>";
        requestChange(new MoMLChangeRequest(this, getContainer(), moml));
    }
}
