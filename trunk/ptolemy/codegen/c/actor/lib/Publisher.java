/*
 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;


/**
 * Generate C code for an actor that publishes tokens on a named channel.
 *
 * @see ptolemy.actor.lib.Publisher
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (cxh)
 *
 */
public class Publisher extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Publisher helper.
     * @param actor the associated actor
     */
    public Publisher(ptolemy.actor.lib.Publisher actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Publisher.c and
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Publisher actor = (ptolemy.actor.lib.Publisher) getComponent();

        ArrayList args = new ArrayList();
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.add(Integer.valueOf(i));
            _codeStream.appendCodeBlock("fireBlock", args);
        }

        return processCode(_codeStream.toString());
    }

}
