/* A representative of a text file.

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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.io.*;
import java.net.URL;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

//////////////////////////////////////////////////////////////////////////
//// TextEffigy
/**
An effigy for a text file.

@author Edward A. Lee
@version $Id$
*/
public class TextEffigy extends Effigy {

    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public TextEffigy(Workspace workspace) {
	super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     */
    public TextEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the document that this is an effigy of.
     *  @return The document, or null if none has been set.
     */
    public Document getDocument() {
	return _doc;
    }

    /** Create a new effigy in the given container containing the specified
     *  text.  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param text The text to insert in the effigy.
     *  @return A new instance of TextEffigy.
     *  @exception Exception If the text effigy cannot be
     *   contained by the specified container, or if the specified
     *   text cannot be inserted into the document.
     */
    public static TextEffigy newTextEffigy(
            CompositeEntity container, String text)
            throws Exception {
        // Create a new effigy.
        TextEffigy effigy = new TextEffigy(container,
                container.uniqueName("effigy"));
        Document doc = new DefaultStyledDocument();
        effigy.setDocument(doc);
        if (text != null) {
            doc.insertString(0, text, null);
        }
        return effigy;
    }

    /** Create a new effigy in the given container by reading the specified
     *  URL. If the specified URL is null, then create a blank effigy.
     *  The extension of the URL is not checked, so this will open any file.
     *  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.  This is ignored in this
     *   class.
     *  @param in The input URL.
     *  @return A new instance of TextEffigy.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public static TextEffigy newTextEffigy(
            CompositeEntity container, URL base, URL in)
            throws Exception {
        // Create a new effigy.
        TextEffigy effigy = new TextEffigy(container,
                container.uniqueName("effigy"));
        Document doc = new DefaultStyledDocument();
        effigy.setDocument(doc);

        if (in != null) {
            // A URL has been given.  Read it.
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in.openStream()));
            String line = reader.readLine();
            while(line != null) {
                // Translate newlines to Java form.
                doc.insertString(doc.getLength(), line + "\n", null);
                line = reader.readLine();
            }
            reader.close();
            // Check the URL to see whether it is a file,
            // and if so, whether it is writable.
            if (in.getProtocol().equals("file")) {
                String filename = in.getFile();
                File file = new File(filename);
                if (!file.canWrite()) {
                    effigy.setModifiable(false);
                }
            } else {
                effigy.setModifiable(false);
            }
        } else {
            // No document associated.  Allow modifications.
            effigy.setModifiable(true);
        }
        effigy.url.setURL(in);
        return effigy;
    }

    /** Set the document that this is an effigy of.
     *  @param document The document
     */
    public void setDocument(Document document) {
        _doc = document;
    }

    /** Write the text of the document to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    public void writeFile(File file) throws IOException {
        if (_doc != null) {
            java.io.FileWriter fout = new java.io.FileWriter(file);
            try {
                fout.write(_doc.getText(0, _doc.getLength()));
            } catch (BadLocationException ex) {
                throw new IOException("Failed to get text from the docuemnt: "
                        + ex);
            }
            fout.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The document associated with this effigy.
    private Document _doc;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {

	/** Create a factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this entity.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an entity already in the container.
	 */
	public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return true, indicating that this effigy factory is
         *  capable of creating an effigy without a URL being specified.
         *  @return True.
         */
        public boolean canCreateBlankEffigy() {
            return true;
        }

        /** Create a new effigy in the given container by reading the specified
         *  URL. If the specified URL is null, then create a blank effigy.
         *  The extension of the URL is not
         *  checked, so this will open any file.  Thus, this factory
         *  should be last on the list of effigy factories in the
         *  configuration.
         *  The new effigy will have a new instance of
         *  DefaultStyledDocument associated with it.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.  This is ignored in this
         *   class.
         *  @param in The input URL.
         *  @return A new instance of TextEffigy.
         *  @exception Exception If the URL cannot be read, or if the data
         *   is malformed in some way.
         */
        public Effigy createEffigy(
                CompositeEntity container, URL base, URL in)
                throws Exception {
            // Create a new effigy.
            return newTextEffigy(container, base, in);
	}
    }
}

