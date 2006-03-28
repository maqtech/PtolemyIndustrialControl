/* Base class for C code generator helper.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.codegen.c.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// CCodeGeneratorHelper

/**
 Base class for C code generator helper. It overrides the
 generateFireCode(), generateInitializeCode(), generatePreinitializeCode(),
 and generateWrapupCode() methods by appending a corresponding code block.
 Subclasses may override these methods if they have to do fancier things.

 @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (mankit) Need to look for c code in actor super classes.
 @Pt.AcceptedRating Red (mankit)
 */
public class CCodeGeneratorHelper extends CodeGeneratorHelper {
    /**
     * Create a new instance of the C code generator helper.
     * @param component The actor object for this helper.
     */
    public CCodeGeneratorHelper(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = new CParseTreeCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate the fire code. In this base class, do nothing. Subclasses
     * may extend this method to generate the fire code of the associated
     * component and append the code to the given string buffer.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        _codeStream.clear();
        _codeStream.append(super.generateFireCode());
        // parent class will generate the preinitialization code
        // Can this method go away?
        //_codeStream.appendCodeBlock("fireBlock", true);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate the initialize code. In this base class, return an empty
     * string. Subclasses may extend this method to generate the initialize
     * code of the associated component and append the code to the given
     * string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeCode() throws IllegalActionException {
        _codeStream.clear();
        _codeStream.append(super.generateInitializeCode());
        _codeStream.appendCodeBlock("initBlock", true);
        return processCode(_codeStream.toString());
    }

    /** Generate the main entry point.
     *  @return In this base class, return a comment.  Subclasses
     *  should return the definition of the main entry point for a program.
     *  In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */ 
    public String generateMainEntryCode() throws IllegalActionException {
        // FIXME: should this be moved to class called CCodeGenerator?
        return "\n\nmain(int argc, char *argv[]) {\n";
    }

    /** Generate the main entry point.
     *  @return Return a string that declares the start of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */ 
    public String generateMainExitCode() throws IllegalActionException {
        // FIXME: should this be moved to class called CCodeGenerator?
        return _INDENT1 + "exit(0);\n}\n";
    }


    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the helper.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        _codeStream.clear();
        _codeStream.append(super.generatePreinitializeCode());
        // parent class will generate the preinitialization code
        // Can this method go away?
        //_codeStream.appendCodeBlock("preinitBlock", true);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate the shared code. This is the FIRST generate method invoked out
     * of all, so any initializations of variables of this helper should be
     * done in this method. In this base class, return an empty set. Subclasses
     * may generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        sharedCode.addAll(super.getSharedCode());
        _codeStream.clear();
        _codeStream.appendCodeBlocks(".*shared.*");
        sharedCode.add(processCode(_codeStream.toString()));
        return sharedCode;
    }

    /**
     * Generate the wrapup code. This is the LAST generate method invoked out
     * of all, so any resets of variables of this helper should be done
     * in this method. In this base class, do nothing. Subclasses may extend
     * this method to generate the wrapup code of the associated component
     * and append the code to the given string buffer.
     * @return code The given string buffer.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupCode() throws IllegalActionException {
        _codeStream.clear();
        _codeStream.append(super.generateWrapupCode());
        _codeStream.appendCodeBlock("wrapupBlock", true);
        return processCode(_codeStream.toString());
    }

    /** Return the parse tree to use with expressions.
     *  @return the parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        _parseTreeCodeGenerator = new CParseTreeCodeGenerator();
        return _parseTreeCodeGenerator;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName);
        return processCode(_codeStream.toString());
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @param args The arguments for the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName, ArrayList args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, args);
        return processCode(_codeStream.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The code stream associated with this helper.
     */
    protected CodeStream _codeStream = new CodeStream(this);

    /** Indent string for indent level 1.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */ 
    protected static String _INDENT1 = StringUtilities.getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */ 
    protected static String _INDENT2 = StringUtilities.getIndentPrefix(2);
}
