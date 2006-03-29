/* A code generation helper class for ptolemy.actor.lib.AddSubtract

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.sdf.lib;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Repeat

/**
 A code generation helper class for ptolemy.domains.sdf.lib.Repeat.

 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Repeat extends CCodeGeneratorHelper {
    /** Construct a helper with the given ptolemy.actor.lib.Scale actor.
     *  @param actor The given ptolemy.actor.lib.Scale actor.
     */
    public Repeat(ptolemy.domains.sdf.lib.Repeat actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the fire code of the Repeat actor.
     *  @return the generated fire code.
     *  @exception IllegalActionException If throw while calling the super
     *  method or while getting a token.
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.domains.sdf.lib.Repeat actor = (ptolemy.domains.sdf.lib.Repeat) getComponent();
        int numberOfTimes = ((IntToken) actor.numberOfTimes.getToken())
                .intValue();
        int blockSize = ((IntToken) actor.blockSize.getToken()).intValue();

        // FIXME: We eventually want to have a for loop in the c code,
        // instead of this.
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < numberOfTimes; j++) {
                _codeStream.append("$ref(output," + ((j * blockSize) + i)
                        + ") = ");
            }
            _codeStream.append("$ref(input," + i + ");\n");
        }

        return processCode(_codeStream.toString());
    }
}
