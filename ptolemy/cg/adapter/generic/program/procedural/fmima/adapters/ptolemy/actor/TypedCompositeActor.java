/* Code generator adapter for typed composite actor.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor

/**
 Code generator adapter for typed composite actor.

 @author Man-Kit Leung, Bert Rodiers
 @version $Id: TypedCompositeActor.java 67784 2013-10-26 16:53:27Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (zgang)
 */
public class TypedCompositeActor extends FMIMACodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public TypedCompositeActor(ptolemy.actor.TypedCompositeActor component) {
        super(component);
    }

    /** Generate FMIMA code.
     *  @return The generated FMIMA.
     *  @exception IllegalActionException If there is a problem getting the adapter, getting
     *  the director or generating FMIMA for the director.
     */
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (getContainer() == null) {
            return "fmima: TypedCompositeActor: top level";
        }
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        code.append(adapter.getComponent().getName() + " contains: ");
        code.append("<ul>" + _eol);

        Object director = getCodeGenerator().getAdapter(
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        Director directorAdapter = null;
        try {
            directorAdapter = (Director) director;
        } catch (ClassCastException ex) {
            throw new IllegalActionException(adapter.getComponent(), ex,
                    "Failed to cast " + director + " of class "
                            + director.getClass().getName() + " to "
                            + Director.class.getName() + ".");
        }
        code.append(directorAdapter.generateFMIMA());

        code.append("</ul>" + _eol);
        return /*processCode(code.toString())*/code.toString();
    }
}
