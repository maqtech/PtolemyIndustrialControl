/*Not sure what should go here yet*/

/* Code generator helper for FSMActor.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.giotto.kernel;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;



//////////////////////////////////////////////////////////////////////////
//// GIOTTOActor

/**
 Code generator helper for GIOTTOActor.

 @author Shanna-Shaye Forbes
 @version $Id: GIOTTOActor.java 50322 2008-08-07 13:53:48Z cxh $
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class GiottoActor extends CCodeGeneratorHelper {
    /** Construct the code generator helper associated with the given FSMActor.
     *  @param component The associated component.
     */
    public GiottoActor(GiottoActor component) {
        super(component);
    }
}