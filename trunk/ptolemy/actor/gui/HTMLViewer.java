/* A viewer for HTML files.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports.
import ptolemy.gui.MessageHandler;

// Java imports.
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

//////////////////////////////////////////////////////////////////////////
//// HTMLViewer
/**
This class is a toplevel frame that can view HTML documents.
It supports printing and will save the text to a .html file.
The url that is viewed can be changed by calling the <i>setPage</i> method.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class HTMLViewer extends TableauFrame
    implements Printable, HyperlinkListener {

    /** Construct a blank viewer.
     */
    public HTMLViewer() {
	getContentPane().setLayout(new BorderLayout(0, 0));
        pane.setEditable(false);
        pane.addHyperlinkListener(this);
        _scroller = new JScrollPane(pane);
        // Default, which can be overridden by calling setSize().
        _scroller.setPreferredSize(new Dimension(800, 600));
        getContentPane().add(_scroller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the page displayed by this viewer.
     *  @return The page displayed by this viewer.
     */
    public URL getPage() {
        return pane.getPage();
    }

    /** React to a hyperlink being clicked on in the rendered HTML.
     *  This method opens the hyperlink URL in a new window, using
     *  the configuration.  This means that hyperlinks can reference
     *  any file that the configuration can open, including MoML files.
     *  @param event The hyperlink event.
     */
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            report(event.getURL().toString());
        } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
            report("");
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL newUrl = event.getURL();

            if (event instanceof HTMLFrameHyperlinkEvent) {
                // For some bizarre reason, when a link is within a frame,
                // it needs to be handled differently than if its not in
                // a frame.
                HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)event;
                String target = evt.getTarget();
                // If the target is "_blank" or "_top", then we want to open
                // in a new window, so we defer to the below.
                if (!target.equals("_blank") && !target.equals("_top")) {
                    HTMLDocument doc = (HTMLDocument)pane.getDocument();
                    try {
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } catch (Exception ex) {
                        MessageHandler.error(
                                "Hyperlink reference failed", ex);
                    }
                    return;
                }
            }
            // Attempt to open in a new window.
            Configuration configuration = getConfiguration();
            // FIXME: Should detect target="_blank" and open
            // in a new window, rather than always opening in a new
            // window.  However, regrettably, there appears to be
            // no way to access the target unless the event is an
            // instanceof HTMLFrameHyperlinkEvent, which it is only
            // if the HTML happens to be in a frame.  Moreover, it would
            // be tricky to do this because we would have to check that
            // the content type is "text/html" or "text/rtf", and we
            // would have to associate our tableau with a new effigy.
            // Nonetheless, it's perfectly doable if we can get the
            // target...
            try {
                if (configuration != null) {
                    configuration.openModel(
                            newUrl, newUrl, newUrl.toExternalForm());
                } else {
                    // If there is no configuration, open in the same window.
                    pane.setPage(newUrl);
                }
            } catch (Exception ex) {
                MessageHandler.error("Hyperlink reference failed", ex);
            }
        }
    }

    // FIXME: This should be handled in Top...

    /** Print the documentation to a printer.  The documentation will be
     *  scaled to fit the width of the paper, growing to as many pages as
     *  is necessary.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {

	Dimension dimension = pane.getSize();

	// How much do we have to scale the width?
	double scale = format.getImageableWidth() / dimension.getWidth();
	double scaledHeight = dimension.getHeight() * scale;
	int lastPage = (int) (scaledHeight / format.getImageableHeight());

	// If we're off the end, then we're done.
	if(index > lastPage) {
            return Printable.NO_SUCH_PAGE;
        }
        AffineTransform at = new AffineTransform();
	at.translate((int)format.getImageableX(),
                (int)format.getImageableY());
	at.translate(0, -(format.getImageableHeight() * index));
	at.scale(scale, scale);

        ((Graphics2D) graphics).transform(at);

        pane.paint(graphics);
        return Printable.PAGE_EXISTS;
    }

    /** Set the page displayed by this viewer to be that given by the
     *  specified URL.
     *  @param page The location of the documentation.
     *  @exception IOException If the page cannot be read.
     */
    public void setPage(URL page) throws IOException {
        pane.setPage(page);
    }

    /** Override the base class to set the size of the scroll pane.
     *  Regrettably, this is necessary because swing packers ignore
     *  the specified size of a container.
     *  @param width The width of the scroll pane.
     *  @param height The height of the scroll pane.
     */
    public void setSize(int width, int height) {
        // FIXME: As usual with Swing, the following has no effect :-(
        _scroller.setPreferredSize(new Dimension(width, height));
        _scroller.setSize(new Dimension(width, height));
        super.setSize(width, height);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                 ////

    /** The text pane. */
    public JEditorPane pane = new JEditorPane();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the model to the specified file.  Note that this does not
     *  defer to the effigy.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        java.io.FileWriter fout = new java.io.FileWriter(file);
        fout.write(pane.getText());
        fout.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The scroll pane.
    private JScrollPane _scroller;
}
