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
package ptolemy.codegen.c.actor.lib.gui;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.gui.XYPlotter.
 *
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class XYPlotter extends CCodeGeneratorHelper {
    /**
     * Constructor method for the XYPlotter helper.
     * @param actor the associated actor.
     */
    public XYPlotter(ptolemy.actor.lib.gui.XYPlotter actor) {
        super(actor);
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>closeFile</code> and <code>graphPlot</code>
     * from XYPlotter.c, replaces macros with their values and appends to the
     * given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        super.generateWrapupCode();
        _codeStream.appendCodeBlock("closeFile");

        ptolemy.actor.lib.gui.XYPlotter actor = (ptolemy.actor.lib.gui.XYPlotter) getComponent();
        if (actor.fillOnWrapup.getExpression().equals("true")) {
            _codeStream.appendCodeBlock("graphPlot");
        }
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * XYPlotter actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the XYPlotter actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<stdio.h>");
        return files;
    }
}
