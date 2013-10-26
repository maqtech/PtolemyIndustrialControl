/* Code generator adapter for refinement port.

 Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.hoc;

import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort;

/**
Code generator adapter.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class RefinementPort extends IOPort {

    /** Construct the code generator adapter associated
     *  with the given RefinementPort.
     *  @param component The associated component.
     */
    public RefinementPort(ptolemy.actor.lib.hoc.RefinementPort component) {
        super(component);
    }
}
