/* The JavaCodeGeneratorAdapterStrategy.

 Copyright (c) 2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program.procedural.java;

import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGeneratorAdapterStrategy;

//////////////////////////////////////////////////////////////////////////
//// JavaCodeGeneratorAdapterStrategy
/**

The strategy that determines how code should be generated for a certain ProceduralCodeGeneratorAdapter.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/
public class JavaCodeGeneratorAdapterStrategy extends
        ProceduralCodeGeneratorAdapterStrategy {
    /**
     * Create a new instance of the JavaCodeGeneratorAdapterStrategy.
     */
    public JavaCodeGeneratorAdapterStrategy() {        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     */
    public JavaCodeGenerator getCodeGenerator() {
        return (JavaCodeGenerator) _codeGenerator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods               ////

    /** Create the template parser.
     */
    protected void _createParser() { 
        _templateParser = new JavaTemplateParser(getComponent(), _adapter);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    
}
