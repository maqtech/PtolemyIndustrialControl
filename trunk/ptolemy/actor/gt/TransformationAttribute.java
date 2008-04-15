/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.io.IOException;
import java.io.Writer;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.gt.TransformationAttributeIcon;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttribute extends GTAttribute {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public TransformationAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _init();
    }

    /**
     * @param workspace
     */
    public TransformationAttribute(Workspace workspace) {
        super(workspace);
    }

    /** The editor factory for the transformer in this attribute.
     */
    public TransformationAttributeEditorFactory editorFactory;

    public TransformerAttribute transformer;

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        transformer = new TransformerAttribute(this, "transformer");
        transformer.setExpression("");
        transformer.setPersistent(true);
        
        new TransformationAttributeIcon(this, "_icon"); 
        
        editorFactory = new TransformationAttributeEditorFactory(this,
                "editorFactory");
    }
    
    public static class TransformerAttribute extends StringAttribute {
        
        public TransformerAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }
        
        public void exportMoML(Writer output, int depth, String name)
        throws IOException {
            if (_transformer != null) {
                try {
                    setExpression(_transformer.exportMoML());
                } catch (IllegalActionException e) {
                    throw new IOException("Unable to obtain MoML string from " +
                            "transformer.", e);
                }
            }
            super.exportMoML(output, depth, name);
        }
        
        public synchronized void setTransformer(ToplevelTransformer transformer) {
            _transformer = transformer;
        }
        
        public synchronized ToplevelTransformer getTransformer() {
            return _transformer;
        }
        
        private ToplevelTransformer _transformer;
    }
}
