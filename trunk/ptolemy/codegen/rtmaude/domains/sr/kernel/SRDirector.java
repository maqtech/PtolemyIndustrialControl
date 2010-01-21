/* RTMaude Code generator helper class for the SRDirector class.

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
package ptolemy.codegen.rtmaude.domains.sr.kernel;

import ptolemy.codegen.rtmaude.actor.Director;

//////////////////////////////////////////////////////////////////////////
////Director

/**
 * Generate RTMaude code for a SRDirector. In the current RTMaude model,
 * a SR model is subsumed by DE model, and there is no difference.
 *
 * @see ptolemy.domains.sr.kernel.SRDirector
 * @author Kyungmin Bae
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating red (kquine)
 * @Pt.AcceptedRating red (kquine)
 */
public class SRDirector extends Director {
    /** Construct the code generator adaptor associated 
     * with the given SR director.
     *  @param director The associated SR director.
     */
    public SRDirector(ptolemy.domains.sr.kernel.SRDirector director) {
        super(director);
    }
}
