/* A helper class for ptolemy.actor.lib.gui.SequencePlotter
 
 Copyright (c) 2006 The Regents of the University of California.
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
 A helper class for ptolemy.actor.lib.gui.SequencePlotter.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class SequencePlotter extends CCodeGeneratorHelper {
   
    /** Constructor method for the SequencePlotter helper.
     *  @param actor the associated actor.
     */
    public SequencePlotter(ptolemy.actor.lib.gui.SequencePlotter actor) {
        super(actor);
    }

    /** Generate fire code.
     *  The method reads in <code>writeFile</code> from SequencePlotter.c,
     *  replaces macros with their values and appends to the given code buffer.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());
        code.append(_generateBlockCode("writeFile"));
        return code.toString();
    }

    /** Generate initialize code.
     *  This method reads the <code>initBlock</code> from SequencePlotter.c,
     *  replaces macros with their values and returns the processed code string.
     *  @return The processed code block.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     *  
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        code.append(_generateBlockCode("initBlock"));
        return processCode(code.toString());
    }

    /** Generate preinitialize code.
     *  This method reads the <code>preinitBlock</code> from SequencePlotter.c,
     *  replaces macros with their values and returns the processed code string.
     *  @return The processed code block.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append(_generateBlockCode("preinitBlock"));
        return processCode(code.toString());
    }

    /** Generate wrap up code.
     *  This method reads the <code>closeFile</code> and <code>graphPlot</code>
     *  from SequencePlotter.c, replaces macros with their values and appends to the
     *  given code buffer.
     *  @return The processed code string.
     *  @exception IllegalActionException If the code stream encounters an
     *   error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateWrapupCode());
        code.append(_generateBlockCode("closeFile"));
        code.append(_generateBlockCode("graphPlot"));
        return code.toString();
    }

    /** Get the header files needed by the code generated for the
     *  SequencePlotter actor.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for the SequencePlotter actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
