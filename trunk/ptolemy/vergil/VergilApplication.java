/* An application for editing ptolemy models visually.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.moml.Documentation;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame; // VERGIL_USER_LIBRARY_NAME

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
This application opens run control panels for models specified on the
command line.
<p>
The exact facilities that are available are determined by an optional
command line argument that names a directory in ptolemy/configs that
contains a configuration.xml file.  For example, if we call vergil
-ptiny, then we will use ptolemy/configs/ptiny/configuration.xml and
ptolemy/configs/ptiny/intro.htm.  The default configuration is
ptolemy/configs/full/configuration.xml, which is loaded before any
other command-line arguments are processed.

<p>This application also takes an optional command line argument pair
<code>-conf <i>configurationFile.xml</i></code> that names a configuration
to be read.  For example,
<pre>
$PTII/bin/vergil -conf ptolemy/configs/ptiny/configuration.xml
<pre>
and
<pre>
$PTII/bin/vergil -ptiny
</pre>
are equivalent
<p>
If there are no command-line arguments at all, then the configuration
file is augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.

@author Edward A. Lee, Steve Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 1.0
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
        // Create register an error handler with the parser so that
        // MoML errors are tolerated more than the default.
        MoMLParser.setErrorHandler(new VergilErrorHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(final String args[]) {
        // FIXME: Java superstition dictates that if you want something
        // to work, you should invoke it in event thread.  Otherwise,
        // weird things happens at the user interface level.  This
        // seems to prevent occasional errors rending HTML under Web Start.
	try {
            // NOTE: This is unfortunate... It would be nice
            // if this could be run inside a PtolemyThread, since
            // getting read access the workspace is much more efficient
            // in PtolemyThread.
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            new VergilApplication(args);
                        } catch (Throwable throwable) {
                            // If we get an Error or and Exception while
                            // configuring, we will end up here.
                            _errorAndExit("Command failed", args, throwable);
                        }
                    }
                });
        } catch (Throwable throwable2) {
            // We are not likely to get here, but just to be safe
            // we try to print the error message and display it in a
            // graphical widget.
            _errorAndExit("Command failed", args, throwable2);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /**
     *  Open the MoML file at the given location as a new library in the
     *  actor library for this application.
     */
    public static void openLibrary(Configuration configuration,
            File file) throws Exception {
        final CompositeEntity libraryContainer = (CompositeEntity)
            configuration.getEntity("actor library");
        if (libraryContainer == null) {
            return;
        }

        final ModelDirectory directory = (ModelDirectory)
            configuration.getEntity(Configuration._DIRECTORY_NAME);
        if (directory == null) {
            return;
        }

        // If we have a jar URL, convert spaces to %20
        URL fileURL =  JNLPUtilities.canonicalizeJarURL(file.toURL());

        String identifier = fileURL.toExternalForm();

        // Check to see whether the library is already open.
        Effigy libraryEffigy = directory.getEffigy(identifier);
        if (libraryEffigy == null) {
            // No previous libraryEffigy exists that is identified by this URL.
            // Parse the user library into the workspace of the actor library.
            MoMLParser parser = new MoMLParser(libraryContainer.workspace());
            // Set the ErrorHandler so that if we have compatibility problems
            // between devel and production versions, we can skip that element.
            parser.setErrorHandler(new VergilErrorHandler());
            parser.parse(fileURL, fileURL);

            // Now create the effigy with no tableau.
            final PtolemyEffigy finalLibraryEffigy =
                new PtolemyEffigy(directory.workspace());
            finalLibraryEffigy.setSystemEffigy(true);

            final ComponentEntity userLibrary =
                (ComponentEntity)parser.getToplevel();

            finalLibraryEffigy.setName(
                    directory.uniqueName(userLibrary.getName()));

            ChangeRequest request =
                new ChangeRequest(configuration, file.toURL().toString()) {
                        protected void _execute() throws Exception {
                            userLibrary.setContainer(libraryContainer);
                            finalLibraryEffigy.setContainer(directory);
                        }
                    };

            libraryContainer.requestChange(request);
            request.waitForCompletion();

            finalLibraryEffigy.setModel(userLibrary);

            // Identify the URL from which the model was read
            // by inserting an attribute into both the model
            // and the effigy.
            URIAttribute uri =
                new URIAttribute(userLibrary, "_uri");
            uri.setURL(fileURL);

            // This is used by TableauFrame in its
            //_save() method.
            finalLibraryEffigy.uri.setURL(fileURL);

            finalLibraryEffigy.identifier.setExpression(identifier);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration.  The initial default configuration
     *  is the MoML file ptolemy/configs/full/configuration.xml, but
     *  using different command line arguments can change the value
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {

        if (_configurationURL == null) {
            _configurationURL =
                specToURL("ptolemy/configs/full/configuration.xml");
        }
        Configuration configuration = null;
        try {
            configuration = _readConfiguration(_configurationURL);
        } catch (Exception ex) {
            throw new Exception("Failed to read configuration '"
                    + _configurationURL + "'", ex);
        }

        // Read the user's vergilUserLibrary.xml file
        //
        // Use StringUtilities.getProperty() so we get the proper
        // canonical path

        // FIXME: If the name is something like
        // "vergilUserLibrary.xml" then when we save an actor in the
        // library and then save the window that comes up the name of
        // entity gets set to vergilUserLibrary instead of the value
        // of VERGIL_USER_LIBRARY_NAME.  This causes problems when we
        // try to save another file.  The name of the entity gets
        // changed by the saveAs code.

        String libraryName = null;
        try {
            libraryName = StringUtilities.preferencesDirectory()
                + BasicGraphFrame.VERGIL_USER_LIBRARY_NAME + ".xml";
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get the preferences "
                    + "directory (-sandbox always causes this): " + ex);
        }
        if (libraryName != null) {
            System.out.print("Opening user library " + libraryName + "...");
            File file = new File(libraryName);
            if (!file.isFile() || !file.exists()) {
                try {
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    writer.write("<entity name=\""
                            + BasicGraphFrame.VERGIL_USER_LIBRARY_NAME
                            + "\" class=\"ptolemy.moml.EntityLibrary\"/>");
                    writer.close();
                } catch (Exception ex) {
                    MessageHandler.error("Failed to create an empty user "
                            + "library: "
                            + libraryName, ex);
                }
            }

            // Load the user library.
            try {
                openLibrary(configuration, file);
                System.out.println(" Done");
            } catch (Exception ex) {
                MessageHandler.error("Failed to display user library.",
                        ex);
            }
        }

        return configuration;
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments, which in this case is given by the default configuration
     *  augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.
     *  @return A configuration for when there no command-line arguments.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();

        if (_configurationSubdirectory == null) {
            _configurationSubdirectory = "full";
        }
        // FIXME: This code is Dog slow for some reason.
        URL inURL = specToURL("ptolemy/configs/"
                + _configurationSubdirectory
                + "/welcomeWindow.xml");
        _parser.reset();
        _parser.setContext(configuration);
        _parser.parse(inURL, inURL.openStream());

        Effigy doc = (Effigy)configuration.getEntity("directory.doc");

        if (_configurationSubdirectory == null) {
            _configurationSubdirectory = "full";
        }
        URL idURL = specToURL("ptolemy/configs/"
                + _configurationSubdirectory + "/intro.htm");

        doc.identifier.setExpression(idURL.toExternalForm());
        return configuration;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(final String args[]) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";

        // VergilApplication.super._parseArgs(args)
        // calls _createDefaultConfiguration() asap,
        // so delay calling it until we process
        // the arguments and possibly get a configuration.
        List processedArgsList = new LinkedList();
        for (int i = 0; i < args.length; i++) {
            // Parse any configuration specific args first so that we can
            // set up the configuration for later use by the parent class.
            if (!_configurationParseArg(args[i])) {
                // If we did not parse the arg, the
                // add it to the list of args to be passed
                // to the super class.
                processedArgsList.add(args[i]);
            }
        }
        if (_expectingConfiguration) {
            throw new IllegalActionException(
                    "Missing configuration");
        }
        String [] processedArgs =
            (String [])processedArgsList
            .toArray(new String[processedArgsList.size()]);

        super._parseArgs(processedArgs);
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + _commandTemplate + "\n\n"
            + "Options that take values:\n";
        int i;
        for (i = 0; i < super._commandOptions.length; i++) {
            result += " " + super._commandOptions[i][0] +
                " " + super._commandOptions[i][1] + "\n";
        }

        for (i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }

        result += "\nBoolean flags:\n";
        for (i = 0; i < super._commandFlags.length; i++) {
            result += " " + super._commandFlags[i];
        }

        try {
            // Look for configuration directories in ptolemy/configs
            // This will likely fail if ptolemy/configs is in a jar file
            // We use a URI here so that we cause call File(URI).
            URI configurationURI =
                new URI(specToURL("ptolemy/configs").toExternalForm());
            File configurationDirectory = new File(configurationURI);
            ConfigurationFilenameFilter filter =
                new ConfigurationFilenameFilter();
            File [] configurationDirectories =
                configurationDirectory.listFiles(filter);
            System.out.println("ConfigurationDirectory = "
                    + configurationDirectory + " configurationDirectories = "
                    + configurationDirectories);
            if (configurationDirectories != null) {
                result += "\nThe following Boolean flags start vergil using "
                    + "different configurations:\n";
                for (i = 0; i < configurationDirectories.length; i++) {
                    result += " -" + configurationDirectories[i].getName();
                    String configurationFileName = configurationDirectories[i]
                        + File.separator
                        + "configuration.xml";
                    boolean printDefaultConfigurationMessage = true;
                    try {
                        // Read the configuration and get the top level docs

                        // FIXME: this will not work if the configs are
                        // in jar files.

                        // FIXME: Skip jxta, since it starts up the jxta config
                        // tools.
                        if (!configurationDirectories[i]
                                .getName().equals("jxta")) {
                            URL specificationURL =
                                specToURL(configurationFileName);
                            Configuration configuration =
                                _readConfiguration(specificationURL);
                            if (configuration != null
                                    && configuration.getAttribute("_doc")
                                    != null
                                    && configuration.getAttribute("_doc")
                                    instanceof Documentation
                                ) {
                                Documentation doc =
                                    (Documentation)configuration
                                    .getAttribute("_doc");
                                result += "\t" + doc.getValue() + "\n";
                                printDefaultConfigurationMessage = false;
                            }
                        }
                    } catch (Exception ex) {
                        //result += "\tCould not read configuration"
                        //    + "\n" + ex;
                        //ex.printStackTrace();
                    }
                    if (printDefaultConfigurationMessage) {
                        result += "\tuses "
                            + configurationFileName + "\n";
                    }
                }
            }
        } catch (Exception ex) {
            result += "Warning: Failed to find configuration(s) in "
                + "ptolemy/configs" + ex;
        }
        return result;
    }

    // _commandOptions is static because
    // _usage() may be called from the constructor of the parent class,
    //  in which case non-static variables are null?

    /** The command-line options that take arguments. */
    protected static String _commandOptions[][] = {
        {"-config",
         "<configuration URL, defaults to ptolemy/configs/full/configuration.xml>"},
    };

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Parse a command-line argument.  Usually, we would name this
     *  method _parseArg(), but we want to handle any arguments that
     *  handle configuration changes before calling the parent class
     *  _parseArg() because the parent class depends either having a
     *  configuration to work with, or the parent class sets up a
     *  configuration.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    private boolean _configurationParseArg(String arg) throws Exception {
        if (arg.startsWith("-conf")) {
            _expectingConfiguration = true;
        } else if (arg.startsWith("-")) {
            // If the argument names a directory in ptolemy/configs
            // that contains a file named configuration.xml that can
            // be found either as a URL or in the classpath, then
            // assume that it is a configuration.  For example, -ptiny
            // will look for ptolemy/configs/ptiny/configuration.xml
            // If the argument does not name a configuration, then
            // we return false so that the argument can be processed
            // by the parent class.
            try {
                _configurationSubdirectory = arg.substring(1);
                String potentialConfiguration =
                    "ptolemy/configs/" + _configurationSubdirectory
                    + "/configuration.xml";
                // This will throw an Exception if we can't find the config.
                _configurationURL = specToURL(potentialConfiguration);
            } catch (Exception ex) {
                // The argument did not name a configuration, let the parent
                // class have a shot.
                return false;
            }
        } else if (_expectingConfiguration) {
            _expectingConfiguration = false;
            _configurationURL = specToURL(arg);
        } else {
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    // Print out an error message and stack trace on stderr and then
    // display a dialog box.  This method is used as a fail safe
    // in case there are problems with the configuration
    // We use a Throwable here instead of an Exception because
    // we might get an Error or and Exception. For example, if we
    // are using JNI, then we might get a java.lang.UnsatistifiedLineError,
    // which is an Error, not and Exception.
    private static void _errorAndExit(String message,
            String [] args, Throwable throwable) {
        StringBuffer argsBuffer =
            new StringBuffer("Command failed");

        if (args.length > 0) {
            argsBuffer.append("\nArguments: " + args[0]);
            for (int i = 1; i < args.length; i++) {
                argsBuffer.append(" " + args[i]);
            }
            argsBuffer.append("\n");
        }

        // First, print out the stack trace so that
        // if the next step fails the user has
        // a chance of seeing the message.
        System.out.println(argsBuffer.toString());
        throwable.printStackTrace();

        // Display the error message in a stack trace
        // If there are problems with the configuration,
        // then there is a chance that we have not
        // registered the GraphicalMessageHandler yet
        // so we do so now so that we are sure
        // the user can see the message.
        // One way to test this is to run vergil -conf foo

        MessageHandler.setMessageHandler(new GraphicalMessageHandler());

        MessageHandler.error(argsBuffer.toString(), throwable);

        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The subdirectory (if any) of ptolemy/configs where the configuration
    // may be found.  For example if vergil was called with -ptiny,
    // then this variable will be set to "ptiny", and the configuration
    // should be at ptolemy/configs/ptiny/configuration.xml
    private String _configurationSubdirectory;

    // URL of the configuration to read.
    // The URL may absolute, or relative to the Ptolemy II tree root.
    // We use the URL instead of the string so that if the configuration
    // is set as a command line argument, we can use the processed value
    // from the command line instead of calling specToURL() again, which
    // might be expensive.
    private URL _configurationURL;

    // Flag indicating that the previous argument was -conf
    private boolean _expectingConfiguration = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Look for directories that contain files named configuration.xml
     *  and intro.htm.
     */
    class ConfigurationFilenameFilter implements FilenameFilter {

        /** Return true if the specified file names a directory
         *  that contains a file named configuration.xml
         *  and a file named intro.htm
         *  @param directory the directory in which the potential
         *  directory was found.
         *  @param name the name of the directory or file.
         *  @return true if the file is a directory that
         *  contains a file called configuration.xml
         */
        public boolean accept(File directory, String name ) {
            try {
                File configurationDirectory = new File(directory, name);
                if (!configurationDirectory.isDirectory()) {
                    return false;
                }
                File configurationFile = new File(configurationDirectory,
                        "configuration.xml");
                File introFile = new File(configurationDirectory,
                        "intro.htm");
                if (configurationFile.isFile()
                        && introFile.isFile()) {
                    return true;
                }
            } catch (Exception ex) {
                return false;
            }
            return false;
        }
    }
}
