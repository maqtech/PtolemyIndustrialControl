/* A tableau representing an HTML window.

Copyright (c) 2000-2004 The Regents of the University of California.
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

package ptolemy.actor.gui;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// HTMLViewerTableau
/**
   A tableau representing a rendered HTML view in a toplevel window.
   The URL that is viewed is given by the <i>url</i> parameter, and
   can be either an absolute URL, a system fileName, or a resource that
   can be loaded relative to the classpath.  For more information about how
   the URL is specified, see MoMLApplication.specToURL().
   <p>
   The constructor of this
   class creates the window. The text window itself is an instance
   of HTMLViewer, and can be accessed using the getFrame() method.
   As with other tableaux, this is an entity that is contained by
   an effigy of a model.
   There can be any number of instances of this class in an effigy.

   @author  Steve Neuendorffer and Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
   @see Effigy
   @see HTMLViewer
   @see MoMLApplication#specToURL(String)
*/
public class HTMLViewerTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This creates an instance of HTMLViewer.  It does not make the frame
     *  visible.  To do that, call show().
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public HTMLViewerTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        url = new StringAttribute(this, "url");

        HTMLViewer frame = new HTMLViewer();
        setFrame(frame);
        frame.setTableau(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The URL to display. */
    public StringAttribute url;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>url</i> parameter, then open the
     *  specified URL and display its contents.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL cannot be opened,
     *   or if the base class throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == url) {
            String urlSpec = ((Settable)attribute).getExpression();
            try {
                // NOTE: This cannot handle a URL that is relative to the
                // MoML file within which this attribute might be being
                // defined.  Is there any way to do that?
                URL toRead = MoMLApplication.specToURL(urlSpec);
                ((HTMLViewer)getFrame()).setPage(toRead);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot open URL: " + urlSpec);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates HTML viewer tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "htmlTableau", then return that tableau; otherwise, create
         *  a new instance of HTMLViewerTableau in the specified
         *  effigy, and name it "htmlTableau".  If the specified
         *  effigy is not an instance of HTMLEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A HTML viewer tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof HTMLEffigy) {

                // Indicate to the effigy that this factory contains effigies
                // offering multiple views of the effigy data.
                effigy.setTableauFactory(this);

                // First see whether the effigy already contains an
                // HTMLViewerTableau.
                HTMLViewerTableau tableau =
                    (HTMLViewerTableau)effigy.getEntity("htmlTableau");
                if (tableau == null) {
                    tableau = new HTMLViewerTableau(
                            (HTMLEffigy)effigy, "htmlTableau");
                }
                // Unfortunately, if we have a jar url, (for example
                // jar:file:/C:/foo.jar!/intro.htm
                // then the java.net.URI toURL() method will return
                // a URL like jar:, which is missing the file: part
                // This breaks Ptolemy II under WebStart.
                URL pageURL = new URL(effigy.uri.getURI().toString());
                try {
                    ((HTMLViewer)tableau.getFrame())
                        .setPage(pageURL);
                } catch (IOException io) {
                    // setPage() throws an IOException if the page can't
                    // be found.  If we are under Web Start, it could be
                    // that we are looking in the wrong Jar file, so
                    // we try again.
                    String urlString = effigy.uri.getURI().toString();
                    URL anotherURL =
                        JNLPUtilities.jarURLEntryResource(urlString);
                    if (anotherURL == null) {
                        throw io;
                    }
                    ((HTMLViewer)tableau.getFrame()).setPage(anotherURL);
                }
                // Don't call show() here.  If show() is called here,
                // then you can't set the size of the window after
                // createTableau() returns.  This will affect how
                // centering works.
                return tableau;
            } else {
                return null;
            }
        }
    }
}
