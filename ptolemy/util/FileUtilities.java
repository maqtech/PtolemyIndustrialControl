/* Utilities used to manipulate files

Copyright (c) 2004 The Regents of the University of California.
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

package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// FileUtilities
/**
   A collection of utilities for manipulating files
   These utilities do not depend on any other ptolemy.* packages.

   @author Christopher Hylands Brooks
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class FileUtilities {

    /** Instances of this class cannot be created.
     */
    private FileUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Copy sourceURL to destinationFile without doing any byte conversion.
     *  @param sourceURL The source URL
     *  @param destinationFile The destination File.
     *  @return true if the file was copied, false if the file was not
     *  copied because the sourceURL and the destinationFile refer to the
     *  same file.
     *  @exception IOException If the source file is not the same as the
     *  destination file and the destination file does not exist.
     */
    public static boolean binaryCopyURLToFile(URL sourceURL,
            File destinationFile)
            throws IOException {

        URL destinationURL = destinationFile.getCanonicalFile().toURL();
        if (sourceURL.sameFile(destinationURL)) {
            return false;
        }

        // If sourceURL is of the form file:./foo, then we need to try again.
        File sourceFile = new File(sourceURL.getFile());
        if (sourceFile.getCanonicalFile().toURL().sameFile(destinationURL)) {
            return false;
        }

        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new BufferedInputStream(sourceURL.openStream());

            output = new BufferedOutputStream(
                    new FileOutputStream(destinationFile));

            int c;
            while (( c = input.read()) != -1) {
                output.write(c);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + sourceURL);
                    throwable.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + destinationFile);
                    throwable.printStackTrace();
                }
            }
        }
        return true;
    }

    /** Given a file name or URL, return the file. This method first attempts
     *  to directly use the file name to construct the File. If the
     *  resulting File is not absolute, then it attempts to resolve it
     *  relative to the specified base directory, if there is one.
     *  If there is no such base URI, then it simply returns the
     *  relative File object.
     *  <p>
     *  The file need not exist for this method to succeed.  Thus,
     *  this method can be used to determine whether a file with a given
     *  name exists, prior to calling openForWriting(), for example.
     *  @param name The file name or URL.
     *  @param base The base for relative URLs.
     *  @return A File, or null if the filename argument is null or
     *   an empty string.
     *  @exception IllegalActionException If a parse error occurs
     *   reading the file name.
     */
    public static File nameToFile(String name, URI base) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = new File(name);
        if (!file.isAbsolute()) {
            // Try to resolve the base directory.
            if (base != null) {
                URI newURI = base.resolve(name);
                file = new File(newURI);
            }
        }
        return file;
    }
    
    /** Given a file or URL name, return as a URL.  If the file name
     *  is relative, then it is interpreted as being relative to the
     *  specified base directory. If the name begins with
     *  "xxxxxxCLASSPATHxxxxxx",
     *  then search for the file relative to the classpath.
     *  Note that this is the value of the globally defined constant
     *  $CLASSPATH available in the Ptolemy II expression language.
     *  If no file is found, then throw an exception.
     *  @param name The name of a file or URL.
     *  @param baseDirectory The base directory for relative file names,
     *   or null to specify none.
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IOException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in), or
     *   the name specification cannot be parsed.
     *  @exception MalformedURLException If the
     */
    public static URL nameToURL(
            String name, URI baseDirectory, ClassLoader classLoader)
            throws IOException {

        if (name == null || name.trim().equals("")) {
            return null;
        }
        // If the name begins with "$CLASSPATH", then attempt to
        // open the file relative to the classpath.
        // NOTE: Use the dummy variable constant set up in the constructor.
        if (name.startsWith(_CLASSPATH_VALUE)) {
            // Try relative to classpath.
            // The +1 is to skip over the delimiter after $CLASSPATH.
            String trimmedName = name.substring(_CLASSPATH_VALUE.length() + 1);
            if (classLoader == null) {
                try {
                    // WebStart: We might be in the Swing Event thread, so
                    // Thread.currentThread().getContextClassLoader()
                    // .getResource(entry) probably will not work so we
                    // use a marker class.
                    Class refClass =
                        Class.forName("ptolemy.kernel.util.NamedObj");
                    classLoader = refClass.getClassLoader();
                } catch (Exception ex) {
                    // IOException constructor does not take a cause
                    IOException ioException =
                        new IOException("Cannot find file '" + trimmedName
                                + "' in classpath");
                    ioException.initCause(ex);
                    throw ioException;
                }
            }
            // Use Thread.currentThread()... for Web Start.
            URL result = classLoader.getResource(trimmedName);
            if (result == null) {
                new IOException("Cannot find file '" + trimmedName
                        + "' in classpath");
            }
            return result;
        }

        File file = new File(name);
        if (file.isAbsolute()) {
            if (!file.canRead()) {
                // FIXME: This is a hack.
                // Expanding the configuration with Ptolemy II installed
                // in a directory with spaces in the name fails on
                // JAIImageReader because PtolemyII.jpg is passed in
                // to this method as C:\Program%20Files\Ptolemy\...
                file = new File(StringUtilities.substitute(name, "%20", " "));
                if (!file.canRead()) {
                    throw new IOException(
                            "Cannot read file '" + name + "' or '"
                            + StringUtilities.substitute(name, "%20", " ")
                            + "'");
                }
            }
            return file.toURL();
        } else {
            // Try relative to the base directory.
            if (baseDirectory != null) {
                // Try to resolve the URI.
                URI newURI;
                try {
                    newURI = baseDirectory.resolve(name);
                } catch (IllegalArgumentException ex) {
                    // FIXME: Another hack
                    // This time, if we try to open some of the JAI
                    // demos that have actors that have defaults FileParameters
                    // like "$PTII/doc/img/PtolemyII.jpg", then resolve()
                    // bombs.
                    String name2 =
                        StringUtilities.substitute(name, "%20", " ");
                    try {
                        newURI = baseDirectory.resolve(name2);
                        name = name2;
                    } catch (IllegalArgumentException ex2) {
                        IOException io = new IOException(
                                "Problem with URI format in '" + name + "'. "
                                + "and '" + name2 + "'"
                                + "This can happen if the file name "
                                + " is not absolute"
                                + " and is not present relative to the directory"
                                + " in which the specified model was read"
                                + " (which was '" + baseDirectory + "')");
                        io.initCause(ex2);
                        throw io;
                    }
                }
                try {
                    return newURI.toURL();
                } catch (IllegalArgumentException ex3) {
                    IOException io = new IOException(
                            "Problem with URI format in '" + name + "'. "
                            + "This can happen if the '" + name
                            + "' is not absolute"
                            + " and is not present relative to the directory"
                            + " in which the specified model was read"
                            + " (which was '" + baseDirectory + "')");
                    io.initCause(ex3);
                    throw io;
                }
            }

            // As a last resort, try an absolute URL.
            return new URL(name);
        }
    }

    /** Open the specified file for reading. If the
     *  specified name is "System.in", then a reader from standard
     *  in is returned. If the name begins with
     *  "$CLASSPATH", then search for the file relative to the classpath.
     *  If the file name is not absolute, the it is assumed
     *  to be relative to the specified base directory.
     *  @see #nameToFile(String, URI)
     *  @param name File name.
     *  @param base The base URI for relative references.
     *  @param classLoader The class loader to use for opening files
     *   relative to the classpath.
     *  @return A buffered reader.
     *  @exception IOException If the file cannot be opened.
     */
    public static BufferedReader openForReading(
            String name, URI base, ClassLoader classLoader)
            throws IOException {
        if (name.trim().equals("System.in")) {
            if (STD_IN == null) {
                STD_IN = new BufferedReader(new InputStreamReader(System.in));
            }
            return STD_IN;
        }
        // Not standard input. Try URL mechanism.
        URL url = nameToURL(name, base, classLoader);
        if (url == null) {
            throw new IOException("No file name has been specified.");
        }
        return new BufferedReader(
               new InputStreamReader(url.openStream()));
    }

    /** Open the specified file for writing or appending. If the
     *  specified name is "System.out", then a writer to standard
     *  out is returned. If the file does not exist, then
     *  create it.  If the file name is not absolute, the it is assumed
     *  to be relative to the specified base directory.
     *  If permitted, this method will return a Writer that will simply
     *  overwrite the contents of the file. It is up to the user of this
     *  method to check whether this is OK (by first calling 
     *  {@link #nameToFile(String, URI)} 
     *  and calling exists() on the returned value).
     *  @param name File name.
     *  @param base The base URI for relative references.
     *  @param append If true, then append to the file rather than
     *   overwriting.
     *  @return A writer, or null if no file name is specified.
     *  @exception IOException If the file cannot be opened
     *   or created.
     */
    public static Writer openForWriting(String name, URI base, boolean append)
            throws IOException {
        if (name.trim().equals("System.out")) {
            if (STD_OUT == null) {
                STD_OUT = new PrintWriter(System.out);
            }
            return STD_OUT;
        }
        if (name == null || name.trim().equals("")) {
            return null;
        }
        File file = nameToFile(name, base);
        return new FileWriter(file, append);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                   ////

    /** Standard in as a reader, which will be non-null
     *  only after a call to openForReading("System.in").
     */
    public static BufferedReader STD_IN = null;

    /** Standard out as a writer, which will be non-null
     *  only after a call to openForWriting("System.out").
     */
    public static PrintWriter STD_OUT = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Tag value used by this class and registered as a parser
     *  constant for the identifier "CLASSPATH" to indicate searching
     *  in the classpath.  This is a hack, but it deals with the fact
     *  that Java is not symmetric in how it deals with getting files
     *  from the classpath (using getResource) and getting files from
     *  the file system.
     */
    private static String _CLASSPATH_VALUE = "xxxxxxCLASSPATHxxxxxx";
}


