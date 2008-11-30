/* A helper class for ptolemy.actor.lib.Accumulator

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.java.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.Accumulator.
 *
 * @author Man-Kit Leung, Gang Zhou
 * @version $Id: Accumulator.java 47513 2007-12-07 06:32:21Z cxh $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Accumulator extends JavaCodeGeneratorHelper {
    /**
     * Construct an Accumulator helper.
     * @param actor the associated actor.
     */
    public Accumulator(ptolemy.actor.lib.Accumulator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Accumulator.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));
        String type = codeGenType(actor.output.getType());

        if (actor.reset.isOutsideConnected()) {
            _codeStream.appendCodeBlock("initReset");
            for (int i = 1; i < actor.reset.getWidth(); i++) {
                args.set(0, Integer.valueOf(i));
                _codeStream.appendCodeBlock("readReset", args);
            }
            _codeStream.appendCodeBlock("ifReset");

            _codeStream
                    .appendCodeBlock((type.equals("String")) ? "StringInitSum"
                            : "InitSum");
        }

        if (!isPrimitive(type)) {
            type = "Token";
        }
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            _codeStream.appendCodeBlock(type + "FireBlock", args);
        }
        _codeStream.appendCodeBlock("sendBlock");

        return processCode(_codeStream.toString());
    }

    /** Generate the initialize code.
     *  The method reads in <code>initBlock</code> from Accumulator.c,
     *  replaces macros with their values and returns the processed code
     *  block.
     *  @return The initialize code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        _codeStream
                .appendCodeBlock((actor.output.getType() == BaseType.STRING) ? "StringInitSum"
                        : "InitSum");

        return processCode(_codeStream.toString());
    }

    /** Generate the preinitialize code.
     *  The method reads in <code>preinitBlock</code> from Accumulator.c,
     *  replaces macros with their values and returns the processed code
     *  block.
     *  @return The preinitialize code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();

        if (actor.reset.isOutsideConnected()) {
            _codeStream.appendCodeBlock("preinitReset");
        }

        return processCode(_codeStream.toString());
    }
}
