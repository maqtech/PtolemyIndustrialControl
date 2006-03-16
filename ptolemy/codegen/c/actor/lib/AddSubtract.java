/* A helper class for ptolemy.actor.lib.AddSubtract

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A helper class for ptolemy.actor.lib.AddSubtract.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class AddSubtract extends CCodeGeneratorHelper {
    /**
     * Constructor method for the AddSubtract helper.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method generate code that loops through each
     * INPUT [multi-ports] and combine (add or subtract) them.
     * The result code is put into the given code buffer
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("\n    ");
        codeBuffer.append("$ref(output) = ");

        for (int i = 0; i < actor.plus.getWidth(); i++) {
            codeBuffer.append("$ref(plus#" + i + ")");

            if (i < (actor.plus.getWidth() - 1)) {
                codeBuffer.append(" + ");
            }
        }

        if (actor.minus.getWidth() > 0) {
            codeBuffer.append(" - ");
        }

        for (int i = 0; i < actor.minus.getWidth(); i++) {
            codeBuffer.append("$ref(minus#" + i + ")");

            if (i < (actor.minus.getWidth() - 1)) {
                codeBuffer.append(" - ");
            }
        }

        codeBuffer.append(";\n");
        code.append(processCode(codeBuffer.toString()));

        return code.toString();
    }
}
