/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.ptalon.gt;

import ptolemy.actor.gt.CreationAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.PreservationAttribute;
import ptolemy.actor.ptalon.PtalonActor;
import ptolemy.actor.ptalon.PtalonEvaluator;
import ptolemy.actor.ptalon.PtalonRuntimeException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TransformationEvaluator

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationEvaluator extends PtalonEvaluator {

    /**
     *  @param actor
     */
    public TransformationEvaluator(PtalonActor actor) {
        super(actor);
    }

    protected void _processAttributes(NamedObj object)
            throws PtalonRuntimeException {
        if (_isInTransformation()) {
            try {
            	GTTools.deepRemoveAttributes(object,
            			PreservationAttribute.class);
            	GTTools.deepAddAttributes(object, CreationAttribute.class);
            } catch (Exception e) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        e);
            }
        } else if (_isPreservingTransformation()) {
            try {
            	GTTools.deepRemoveAttributes(object,
            			CreationAttribute.class);
            	GTTools.deepAddAttributes(object, PreservationAttribute.class);
            } catch (Exception e) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        e);
            }
        }
    }
}
