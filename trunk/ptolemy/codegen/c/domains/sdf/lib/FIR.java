/* A code generation helper class for domains.sdf.lib.FIR
 @Copyright (c) 2006-2007 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.domains.sdf.lib;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.sdf.lib.FIR. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit) 
 @Pt.AcceptedRating Red (mankit)
 */
public class FIR extends CCodeGeneratorHelper {

    /**
     * Construct a FIR helper.
     * @param actor The associated actor.
     */
    public FIR(ptolemy.domains.sdf.lib.FIR actor) {
        super(actor);
    }
    
    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> and <code>fireBlock0</code> from
     * FIR.c and replace macros with their values and return the
     * processed code block.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        code.append(_generateBlockCode("fireBlock0"));

        code.append(super.generateFireCode());

        return processCode(code.toString());
    }
    
    /**
     * Generate initialize code.
     * Read the <code>initBlock</code> and <code>initBlock0</code> from
     * FIR.c and replace macros with their values and return the
     * processed code block.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        code.append(_generateBlockCode("initBlock0"));

        code.append(super.generateInitializeCode());

        return processCode(code.toString());
    }
}
