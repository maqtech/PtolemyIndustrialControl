/* An application for editing ptolemy models visually.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

package ptolemy.vergil;

// Ptolemy imports
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

// Java imports
import java.net.URL;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
This application opens run control panels for models specified on the
command line.  The exact facilities that are available are determined
by the configuration file ptolemy/configs/vergilConfiguration.xml,
which is loaded before any command-line arguments are processed.
If there are no command-line arguments at all, then the configuration
file is augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@see ptolemy.actor.gui.ModelFrame
@see ptolemy.actor.gui.RunTableau
*/
public class VergilApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String args[]) throws Exception {
	super(args);
        // FIXME: In March, 2001, Johan Ecker writes
        // Ptolemy gave tons of exception when started on my laptop
        // which has Swedish settings as default. The Swedish standard
        // for floating points are "2,3", i.e. using a comma as
        // delimiter. However, I think most Swedes are adaptable and
        // do not mind using a dot instead since this more or less has
        // become the world standard, at least in engineering. The
        // problem is that I needed to change my global settings to
        // start Ptolemy and this is quite annoying. I guess that the
        // expression parser should just ignore the delimiter settings
        // on the local computer and always use dot, otherwise Ptolemy
        // will crash using its own init files.

        // Even if the user is set up for foreign locale, use the US locale.
        // This is because certain parts of Ptolemy (like the expression 
        // language) are not localized.
	try {
	    // FIXME: This is a workaround for the locale problem, not a fix.
	    java.util.Locale.setDefault(java.util.Locale.US);
	} catch (java.security.AccessControlException accessControl) {
	    // FIXME: If the application is run under Web Start, then this
	    // exception will be thrown.
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
	try {
	    new VergilApplication(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Return a default Configuration, which in this case is given by
     *  the MoML file ptolemy/configs/vergilConfiguration.xml.
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        return _readConfiguration("ptolemy/configs/vergilConfiguration.xml");
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments, which in this case is given by the default configuration
     *  augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.
     *  @return A configuration for when there no command-line arguments.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();

        // FIXME: This code is Dog slow for some reason.
        URL inurl = specToURL("ptolemy/configs/vergilWelcomeWindow.xml");
        _parser.reset();
        _parser.setContext(configuration);
        _parser.parse(inurl, inurl.openStream());
        Effigy doc = (Effigy)configuration.getEntity("directory.doc");
        URL idurl = specToURL("ptolemy/configs/intro.htm");
        doc.identifier.setExpression(idurl.toExternalForm());
        return configuration;
    }

    /** Parse the command-line arguments. This overrides the base class
     *  only to set the usage information.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(final String args[]) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";
        // NOTE: Java superstition dictates that if you want something
        // to work, you should invoke it in event thread.  Otherwise,
        // weird things happens at the user interface level.  This
        // seems to prevent occasional errors rending HTML.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    VergilApplication.super._parseArgs(args);
                } catch (Exception ex) {
                    MessageHandler.error("Command failed", ex);
                    System.exit(0);
                }
            }
        });
    }
}
