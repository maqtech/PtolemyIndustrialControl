/* An application for editing ptolemy models visually.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
This application opens run control panels for models specified on the
command line.  The exact facilities that are available are determined
by the configuration file ptolemy/configs/runPanelConfiguration.xml,
which is loaded before any command-line arguments are processed.
If there are no command-line arguments at all, then the file
ptolemy/configs/runBlankConfiguration.xml is read instead.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@see ModelFrame
@see RunTableau
*/
public class VergilApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String args[]) throws Exception {
	super(args);
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
     *  arguments, which in this case is given by the MoML file
     *  ptolemy/configs/vergilBlankConfiguration.xml.
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
    protected void _parseArgs(String args[]) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";
        super._parseArgs(args);
    }
}
