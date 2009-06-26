/* RTMaude Code generator helper class for the SetVariable class.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.codegen.rtmaude.actor.lib;

//////////////////////////////////////////////////////////////////////////
//// SetVariable

/**
 * Generate RTMaude code for a SetVariable actor in DE domain.
 *
 * @see ptolemy.actor.lib.SetVariable
 * @author Kyungmin Bae
 * @version $Id$
 * @Pt.ProposedRating Red (kquine)
 *
 */
import java.util.List;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class SetVariable extends Entity {

    public SetVariable(ptolemy.actor.lib.SetVariable component) {
        super(component);
    }

    @Override
    protected String _generateInfoCode(String name, List<String> parameters)
            throws IllegalActionException {
        if (name.equals("variableName"))
            return ((ptolemy.actor.lib.SetVariable)getComponent()).variableName.getExpression();
        return super._generateInfoCode(name, parameters);
    }
}
