/* An actor that produces an array that lists the contents of a directory.

@Copyright (c) 2003-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

PT_COPYRIGHT_VERSION 2
COPYRIGHTENDKEY
*/

package ptolemy.actor.lib.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DirectoryListing
/**
   Given a URL or directory name, this actor produces an array of file names
   in that directory that match an (optional) pattern.  The file names that
   are returned are absolute. The pattern is
   a regular expression. For a reference on regular expression syntax see:
   <a href="http://java.sun.com/docs/books/tutorial/extra/regex/index.html">
   http://java.sun.com/docs/books/tutorial/extra/regex/index.html</a>.
   <p>
   If <i>directoryOrURL</i> is a directory (not a URL), then you can
   optionally list only contained files or directories.
   If <i>listOnlyDirectories</i> is true, then only directories will be
   listed on the output.  If <i>listOnlyFiles</i> is true, then only
   files will be listed on the output. If both are true, then an exception
   is thrown.

   @author  Christopher Hylands, Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (liuj)
*/
public class DirectoryListing extends SequenceSource implements FilenameFilter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DirectoryListing(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Tell the file browser to allow only selection of directories.
        directoryOrURL = new FileParameter(this, "directoryOrURL");
        new Parameter(directoryOrURL, "allowFiles", BooleanToken.FALSE);
        new Parameter(directoryOrURL, "allowDirectories", BooleanToken.TRUE);
 
        directoryOrURLPort = new TypedIOPort(this, "directoryOrURL", true, false);
        directoryOrURLPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(new ArrayType(BaseType.STRING));

        pattern = new StringParameter(this, "pattern");
        pattern.setExpression("");
        
        listOnlyDirectories = new Parameter(this, "listOnlyDirectories");
        listOnlyDirectories.setTypeEquals(BaseType.BOOLEAN);
        listOnlyDirectories.setExpression("false");

        listOnlyFiles = new Parameter(this, "listOnlyFiles");
        listOnlyFiles.setTypeEquals(BaseType.BOOLEAN);
        listOnlyFiles.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The directory name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter directoryOrURL;

    /** An input port for optionally providing a directory name. This has
     *  type string.
     */
    public TypedIOPort directoryOrURLPort;
    
    /** If true, then only directories will be listed on the output.
     *  This is a boolean that defaults to false.
     */
    public Parameter listOnlyDirectories;

    /** If true, then only file will be listed on the output.
     *  This is a boolean that defaults to false.
     */
    public Parameter listOnlyFiles;

    /** If non-empty, then only output file and directory names that
     *  match the specified (regular expression) pattern.
     *  The default value of this parameter is the empty String "",
     *  which indicates that everything matches.
     */
    public StringParameter pattern;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the specified name matches the specified pattern,
     *  or if no pattern has been specified.
     *  @param directory The directory in which the file was found
     *   (ignored, but required by the FilenameFilter interface).
     *  @param name The name of the file or directory.
     *  @return True if the specified name matches.
     */
    public boolean accept(File directory, String name) {
        if (_pattern != null) {
            Matcher match = _pattern.matcher(name);
            return match.find();
        }
        return true;
    }

    /** Override the base class to locally cache parameter values.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pattern) {
            try {
                _pattern = Pattern.compile(pattern.stringValue());
            } catch (PatternSyntaxException ex) {
                String patternValue
                    = ((StringToken)pattern.getToken()).stringValue();
                throw new IllegalActionException(this, ex,
                        "Failed to compile regular expression \""
                        + patternValue + "\"");
            }
        }
        super.attributeChanged(attribute);
    }

    /** Output an array containing file and/or directory names.
     *  @exception IllegalActionException If there's no director or
     *   if the directory or URL is invalid.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (directoryOrURLPort.getWidth() > 0
                && directoryOrURLPort.hasToken(0)) {
            String newValue
                    = ((StringToken)directoryOrURLPort.get(0)).stringValue();
            directoryOrURL.setExpression(newValue);
        }

        URL sourceURL = directoryOrURL.asURL();

        if (sourceURL == null) {
            // Nothing to read
            throw new IllegalActionException(this,
                    "directoryOrURL is empty.");
        }

        boolean directoriesOnly = ((BooleanToken)
                listOnlyDirectories.getToken()).booleanValue();
        boolean filesOnly = ((BooleanToken)
                listOnlyDirectories.getToken()).booleanValue();
        if (sourceURL.getProtocol().equals("file")) {
            File sourceFile = directoryOrURL.asFile();
            if (sourceFile.isDirectory()) {
                File[] files = sourceFile.listFiles(this);
                ArrayList result = new ArrayList();
                for (int i = 0; i < files.length; i++) {
                    if (filesOnly && !files[i].isFile()) {
                    	continue;
                    }
                    if (directoriesOnly && !files[i].isDirectory()) {
                        continue;
                    }
                    if (accept(null, files[i].getName())) {
                    	result.add(new StringToken(
                                files[i].getAbsolutePath()));
                    }
                }
                if (result.size() == 0) {
                    throw new IllegalActionException(this,
                            "No files or directories that match the pattern.");                    
                }
                StringToken[] resultArray = new StringToken[result.size()];
                for (int i = 0; i < resultArray.length; i++) {
                	resultArray[i] = (StringToken)result.get(i);
                }
                output.broadcast(new ArrayToken(resultArray));
                return;
            } else if (sourceFile.isFile()) {
                StringToken[] result = new StringToken[1];
                result[0] = new StringToken(sourceFile.toString());
                output.broadcast(new ArrayToken(result));
                return;
            } else {
                throw new IllegalActionException("'"
                        + directoryOrURL
                        + "' is neither a file "
                        + "nor a directory.");
            }
        } else {
            try {
                _readURL(sourceURL);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Error reading the URL \'" + directoryOrURL + "\'.");
            }
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Read the URL and produce output.
    private void _readURL(URL sourceURL)
            throws IOException, IllegalActionException {
        // Handle urls here.
        URLConnection urlConnection = sourceURL.openConnection();
        String contentType = urlConnection.getContentType();
        if (!contentType.startsWith("text/html")
                && !contentType.startsWith("text/plain") ) {
            throw new IllegalActionException(this,
                    "Could not parse '"
                    + directoryOrURL
                    + "'; it is not \"text/html\", "
                    + "or \"text/plain\", it is: "
                    + urlConnection.getContentType());
        }
        BufferedReader in =
            new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
        if (!contentType.startsWith("text/plain")
                && !urlConnection.getURL().toString().endsWith("/")) {
            // text/plain urls need not end with /, but
            // text/html urls _must_ end with / since the web server
            // will rewrite them for us.
            throw new IllegalActionException(this,
                    "Could not parse '"
                    + directoryOrURL
                    + "'; it needs to end with '/'");

        }

        // Parse the contents in a haphazard fashion.
        // The idea is that we look for the <BODY> line and
        // then look for lines that contain HREF
        // If we find a line like HREF="foo">foo, then we report
        // foo as being a file.
        // A more robust way would be to use a spider, see
        // http://www.acme.com/java/software/WebList.html
        List resultsList = new LinkedList();
        String line;
        String target = null;
        boolean sawBody = false, sawHREF = false;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("<BODY")
                    || line.startsWith("<body")) {
                sawBody = true;
            } else {
                if (sawBody) {
                    StringTokenizer tokenizer =
                        new StringTokenizer(line, "<\" >=");
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        if (token.compareToIgnoreCase("HREF") == 0) {
                            sawHREF = true;
                            target = null;
                        } else {
                            if (sawHREF) {
                                if (target == null) {
                                    // Here, we should check that target
                                    // is a relative pathname.
                                    target = token;
                                } else {
                                    // Check to see if the token is
                                    // the same as the last token.
                                    if (token.compareTo(target) != 0) {
                                        sawHREF = false;
                                    } else {
                                        if (accept(null, target)) {
                                            resultsList.add(new StringToken(
                                                                    directoryOrURL + target));
                                        }
                                        sawHREF = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        in.close();
        StringToken [] results = new StringToken[resultsList.size()];
        output.broadcast(new ArrayToken((StringToken [])(resultsList.toArray(results))));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // An array containing the files and subdirectories in the directory
    // named by sourceURL.
    // FIXME: Should we clone this?
    private String[] _data;

    // The pattern for the regular expression.
    private Pattern _pattern;
}
