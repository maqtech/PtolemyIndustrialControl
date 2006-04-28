/* Manager of the semantic highlightings.

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
package ptolemy.backtrack.eclipse.plugin.editor;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting;
import org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.jdt.internal.ui.text.JavaPresentationReconciler;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;

import org.eclipse.jface.preference.IPreferenceStore;

//////////////////////////////////////////////////////////////////////////
//// SemanticHighlightingManager

/**
   Manager of the semantic highlightings.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class SemanticHighlightingManager {
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Enable Ptolemy semantic highlighting.
     */
    public void enable() {
        _initializeHighlightings();
    }

    /** Initialize Ptolemy semantic highlighting and enable it.
     * 
     *  @param editor The editor to which semantic highlighting is to be
     *   installed.
     *  @param sourceViewer The source viewer.
     *  @param colorManager The color manager.
     *  @param preferenceStore The Eclipse preference store.
     */
    public void install(JavaEditor editor, JavaSourceViewer sourceViewer,
            IColorManager colorManager, IPreferenceStore preferenceStore) {
        final String JAVA_PARTITIONING = "___java_partitioning";

        _editor = editor;
        _sourceViewer = sourceViewer;
        _colorManager = colorManager;
        _preferenceStore = preferenceStore;

        if (editor != null) {
            _configuration = new JavaSourceViewerConfiguration(colorManager,
                    preferenceStore, editor, JAVA_PARTITIONING);
            _presentationReconciler = (JavaPresentationReconciler) _configuration
                    .getPresentationReconciler(sourceViewer);
        } else {
            _configuration = null;
            _presentationReconciler = null;
        }

        //_preferenceStore.addPropertyChangeListener(this);
        if (isEnabled()) {
            enable();
        }
    }

    /** Return whether semantic highlighting is enabled.
     * 
     *  @return true if semantic highlighting is enabled.
     */
    public boolean isEnabled() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Initialize semantic highlightings.
     */
    protected void _initializeHighlightings() {
        _semanticHighlightings = SemanticHighlightings
                .getSemanticHighlightings();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The color manager.
     */
    private IColorManager _colorManager;

    /** The source viewer configuration.
     */
    private JavaSourceViewerConfiguration _configuration;

    /** The editor.
     */
    private JavaEditor _editor;

    /** The Eclipse preference store.
     */
    private IPreferenceStore _preferenceStore;

    /** The source code presentation reconciler.
     */
    private JavaPresentationReconciler _presentationReconciler;

    /** The semantic highlightings.
     */
    private SemanticHighlighting[] _semanticHighlightings;

    /** The source viewer.
     */
    private JavaSourceViewer _sourceViewer;
}
