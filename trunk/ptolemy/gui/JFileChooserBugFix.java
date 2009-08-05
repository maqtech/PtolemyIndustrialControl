/* Utilities for managing the background color.

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
package ptolemy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// JFileChooserBugFix

/**
 A workaround for a JFileChooser bug.
 <p>This class is necessary to work around a bug under Windows where
 the "common places" portion of the JFileChooser dialog is affected
 by the background color of a component.  Sun has acknowledged the
 bug as <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6817933
">#6817933</a>.  See also "<a href="http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3801">open dialog, common places pane has white box instead of text</a>."

 <p> Every time JFileChooser is instantiated, saveBackground() should
 be called so that the background is properly set.  Then, in a finally
 clause, restoreBackground() should be called.  For example:
 <pre>
   
  // Swap backgrounds and avoid white boxes in "common places" dialog
  JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
  Color background = null;
  try {
      background = jFileChooserBugFix.saveBackground();
      JFileChooser fileDialog = new JFileChooser();
      // Do the usual JFileChooser song and dance . . .
  } finally {
      jFileChooserBugFix.restoreBackground(background);
  }

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JFileChooserBugFix {

    /** Instantiate a JFileChooserBugFix object. */
    public JFileChooserBugFix() {
        _HTMLEditorKit = new HTMLEditorKit();
    }

    /** Restore the background.
     *  @param background The background to be restored.
     *  @see #saveBackground();
     */
    public void restoreBackground(Color background) {
        try {
            if (background != null) {
                // Restore the background color.
                String rgb = Integer.toHexString(background.getRGB());
                String rule = "body {background: #"
                    + rgb.substring(2, rgb.length()) + ";}";
                StyleSheet styleSheet = _HTMLEditorKit.getStyleSheet();
                styleSheet.addRule(rule);
                _HTMLEditorKit.setStyleSheet(styleSheet);
            }
        } catch (Exception ex) {
            System.out.println("Problem restoring background color.");
            ex.printStackTrace();
        }
    }

    /** Set the background to the value of the ToolBar.shadow property
     *  and return the previous background.   
     *  <p>Avoid a problem under Windows where the common places pane
     *  on the left of the file browser dialog has white boxes
     *  because the background is set to white.
     *  http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3801
     *  <p>Call this method before instantiating a JFileChooser.
     *  @return the value of the previous background.
     */
    public Color saveBackground() {
        if (_HTMLEditorKit == null) {
            _HTMLEditorKit = new HTMLEditorKit();
        }
        StyleSheet styleSheet = _HTMLEditorKit.getStyleSheet();
        Color background = null;

        try {
            // Get the background color of the HTML widget.
            AttributeSet bodyAttribute = (AttributeSet) styleSheet.getStyle(
                    "body").getAttribute(
                    javax.swing.text.StyleConstants.ResolveAttribute);
            background = styleSheet.getBackground(bodyAttribute);
        } catch (Exception ex) {
            System.err.println("Problem getting background color");
            ex.printStackTrace();
        }

        try {
            // Get the color of the ToolBar shadow and use it.
            Color shadow = UIManager.getColor("ToolBar.shadow");
            String rgb = Integer.toHexString(shadow.getRGB());
            String rule = "body {background: #"
                + rgb.substring(2, rgb.length()) + ";}";
            styleSheet.addRule(rule);
            _HTMLEditorKit.setStyleSheet(styleSheet);
        } catch (Exception ex) {
            System.err.println("Problem setting background color");
            ex.printStackTrace();
        }
        return background;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The HTMLEditorKit*/
    private HTMLEditorKit _HTMLEditorKit;
}