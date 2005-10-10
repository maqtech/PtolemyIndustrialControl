/* A code generation helper class for actor.lib.gui.Display

 @Copyright (c) 2005 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.gui.Display.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Display extends CCodeGeneratorHelper {

    /**
     * Constructor method for the Display helper.
     * @param actor The associated actor.
     */
    public Display(ptolemy.actor.lib.gui.Display actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>printInt</code>, <code>printArray</code>,
     * <code>printString</code>, or <code>printDouble</code> from Display.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void  generateFireCode(StringBuffer code)
        throws IllegalActionException {
        ptolemy.actor.lib.gui.Display actor =
            (ptolemy.actor.lib.gui.Display) getComponent();
        _codeStream.clear();
        ArrayList args = new ArrayList();
        List connectedPorts = actor.input.connectedPortList();

        args.add(new Integer(0));           
        for (int i = 0; i < actor.input.getWidth(); i++) {
            TypedIOPort port = (TypedIOPort) connectedPorts.get(i);
            
            args.set(0, Integer.toString(i));
            if (port.getType() == BaseType.INT) {
                _codeStream.appendCodeBlock("printInt", args);
            } else if (port.getType() == BaseType.DOUBLE) {
                _codeStream.appendCodeBlock("printDouble", args);
            } else if (port.getType() == BaseType.STRING) {
                _codeStream.appendCodeBlock("printString", args);
            } else {
                _codeStream.appendCodeBlock("printToken", args);
            }
        }
        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate shared code.
     * This method reads the <code>sharedBlock</code> from Display.c,
     * replaces macros with their values and returns the processed code string.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set generateSharedCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        Set codeBlocks = new HashSet();
        codeBlocks.add(_generateBlockCode("sharedBlock", false));
        return codeBlocks;
    }

    /**
     * Get the files needed by the code generated for the
     * Display actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Display actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
}
