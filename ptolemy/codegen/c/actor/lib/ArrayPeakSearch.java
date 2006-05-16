/* A code generation helper class for actor.lib.ArrayPeakSearch
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.ArrayPeakSearch. 

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (mankit) 
 @Pt.AcceptedRating Red (mankit)
 */
public class ArrayPeakSearch extends CCodeGeneratorHelper {

    /**
     * Constructor method for the ArrayPeakSearch helper.
     * @param actor The associated actor.
     */
    public ArrayPeakSearch(ptolemy.actor.lib.ArrayPeakSearch actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from ArrayPeakSearch.c,
     * replace macros with their values and append the processed code              
     * block to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();
        ptolemy.actor.lib.ArrayPeakSearch actor = 
            (ptolemy.actor.lib.ArrayPeakSearch) getComponent();
        
        String scaleValue = actor.scale.stringValue();

        if (!scaleValue.equals("absolute")) {
            if (scaleValue.equals("relative amplitude decibels")) {
            } else if (scaleValue.equals("relative power decibels")) {
            } else if (scaleValue.equals("relative linear")) {
            }
        }
        
        if (scaleValue.equals("relative amplitude decibels")) {
            _codeStream.appendCodeBlock("amplitude_" + aboveValue);
        } else if (scaleValue.equals("relative power decibels")) {
            _codeStream.appendCodeBlock("power_" + aboveValue);
        } else if (scaleValue.equals("relative linear")) {
            _codeStream.appendCodeBlock("linear_" + aboveValue);
        }
        _codeStream.appendCodeBlock("findCrossing_" + aboveValue);

        return processCode(_codeStream.toString());
    }

    /**
     * Generate initialize code.
     * Read the  from ArrayPeakSearch.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        return code.toString();
    }

    /**
     * Generate preinitialize code.
     * Reads the <code>preinitBlock</code> from ArrayPeakSearch.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append(_generateBlockCode("preinitBlock"));
        return code.toString();
    }

    /**
     * Generate shared code.
     * Read the  from ArrayPeakSearch.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        sharedCode.addAll(super.getHeaderFiles());

        return sharedCode;
    }

    /**
     * Generate wrap up code.
     * Read the  from ArrayPeakSearch.c, 
     * replace macros with their values and append the processed code block
     * to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateWrapupCode());

        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * ArrayPeakSearch actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the ArrayPeakSearch actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.addAll(super.getHeaderFiles());
        files.add("<stdio.h>");

        return files;
    }
}
