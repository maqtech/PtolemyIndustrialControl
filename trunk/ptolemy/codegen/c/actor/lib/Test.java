/* A helper class for ptolemy.actor.lib.Test
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

package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Test

/**
   A helper class for ptolemy.actor.lib.Test

   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class Test extends CCodeGeneratorHelper {

    /** Construct a C code generator Test actor that contains
     *  a standard actor.lib.Test actor.
     *  @param actor The master Test actor.
     */
    public Test(ptolemy.actor.lib.Test actor) {
        super(actor);
        _testActor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate initialization code.
     *  This method reads the <code>initBlock</code> from Test.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>initBlock</code>.
     */
    public String generateInitializeCode()
            throws IllegalActionException {
        super.generateInitializeCode();
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("initBlock");
        return processCode(tmpStream.toString());
    }

    /** Generate initialization code.
     *  This method reads the <code>codeBlock1</code> from Test.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>initBlock</code>.
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        // FIXME: handle widths greater than 1.
        if (_testActor.input.getWidth() > 1) {
            throw new IllegalActionException(_testActor,
                    "The C version of the Test actor currently only handles "
                    + "inputs of width 1.  The width of input was: "
                    + _testActor.input.getWidth());
        }

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("codeBlock1");

        stream.append(processCode(tmpStream.toString()));
    }

    /** Get the libraries needed by the code generated for the
     *  Test actor.
     *  @return A set of strings that are names of the libraries
     *   needed by the code generated for the Test actor.
     */
    public Set getIncludingLibraries() {
        Set libraries = new HashSet();
        libraries.add("\"stdio.h\"");
        libraries.add("\"math.h\"");
        return libraries;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Original type polymorphic Test actor. */
    ptolemy.actor.lib.Test _testActor;
}
