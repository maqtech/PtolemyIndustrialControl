/* Top-level window containing a simple text editor or viewer.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

// FIXME: To do:
//  - Fix printing.
//  - Handle file changes (warn when discarding modified files.

package ptolemy.actor.gui;

// Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Point;
import java.net.URL;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.text.Document;

//////////////////////////////////////////////////////////////////////////
//// TextEditor
/**

TextEditor is a top-level window containing a simple text editor or viewer.

@author Edward A. Lee
@version $Id$
*/
public class TextEditor extends TableauFrame {

    /** Construct an empty text editor with no name.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public TextEditor() {
        this("Unnamed");
    }

    /** Construct an empty text editor with the specified title.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     */
    public TextEditor(String title) {
        this(title, null);
    }

    /** Construct an empty text editor with the specified title and
     *  document.  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     */
    public TextEditor(String title, Document document) {
        super();
        setTitle(title);

        text = new JTextArea(document);
        JScrollPane scrollPane = new JScrollPane(text);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // FIXME: Need to do something with the progress bar in the status bar.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The text area. */
    public JTextArea text;

    ///////////////////////////////////////////////////////////////////
    ////                            public methods                 ////

    /** Scroll as necessary so that the last line is visible.
     */
    public void scrollToEnd() {
	// Song and dance to scroll to the new line.
	text.scrollRectToVisible(new Rectangle(
                new Point(0, text.getHeight())));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            text.setText("");
            return true;
        } else {
            return false;
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        // FIXME: Give instructions for the editor here.
        _about();
    }

    /** Print the contents.
     */
    protected void _print() {
        // FIXME: What should we print?
        super._print();
    }

    // FIXME: Listen for edit changes.
    // FIXME: Listen for window closing.
}
