/* Utilities that manipulate strings using XSLT

Copyright (c) 2002-2004 The Regents of the University of California.
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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//////////////////////////////////////////////////////////////////////////
//// XSLTUtilities
/**
   A collection of utilities for manipulating strings using XSLT
   These utilities do not depend on any other ptolemy.* packages.

   <p>This file uses Saxon, the XSLT and XQuery Processor
   <a href="http://saxon.sourceforge.net/#in_browser" target="_top">http://saxon.sourceforge.net</a>.

   <p>Between Java 1.4.x and Java 1.5, Xalan was removed from the jar
   files that are shipped.  Since Caltrop uses Saxon anyway, we now
   use Saxon here as well.


   @author Christopher Hylands, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class XSLTUtilities {

    /** Instances of this class cannot be created.
     */
    private XSLTUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply XSL transforms to an input file and generate an output file.
     *  <p>Example use:
     * <pre>
     *  java -classpath $PTII ptolemy.util.XSLTUtilities
     *       $PTII/ptolemy/hsif/demo/SwimmingPool/SwimmingPool.xml \
     *       $PTII/ptolemy/hsif/xsl/GlobalVariablePreprocessor.xsl \
     *       /tmp/SwimmingPool_1.xml
     * </pre>

     * @param args At least three arguments:
     * <ul>
     * <li> The first argument is the input file name.
     * <li> The second through n-1 arguments are the names xsl files.
     * <li> The final argument is the output file name.
     * @exception Exception If there are problems with the transform.
     */
    public static void main(String []args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.util.XSLTUtilities inputFile "
                    + "xslFile1 [xslFile2 . . .] outputFile");
            System.exit(2);
        }
        // Make sure we can write the output first
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(args[args.length - 1]);
            Document inputDocument = parse(args[0]);

            List transforms = new LinkedList();
            for (int i = 1; i < args.length - 1; i++) {
                transforms.add(args[i]);
            }
            Document outputDocument =
                XSLTUtilities.transform(inputDocument, transforms);
            fileWriter.write(XSLTUtilities.toString(outputDocument));
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    /** Parse an XML document using Saxon.
     *  @param filename The file name of the xml file to be read in
     *  The filename is passed to org.xml.sax.InputSource(String),
     *  so it may be a file name or a URL.
     *  @return the parsed document.
     *  @exception ParserConfigurationException Thrown if there is a problem
     *  creating the DocumentBuilder.
     *  @exception IOException Thrown if the filename could not be parsed.
     */
    public static Document parse(String filename)
            throws ParserConfigurationException, IOException {
        // FIXME: Throw something other than Exception
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "net.sf.saxon.om.DocumentBuilderFactoryImpl");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        // We use InputSource here so that we can specify the filename
        // argument as a jar url so that HSIFToMoML works under Web Start.
        try {
            return builder.parse(new InputSource(filename));
        } catch (SAXException ex) {
            // Rethrow this with the filename included.
            IOException exception =
                new IOException("Failed to parse '" + filename + "'");
            exception.initCause(ex);
            throw exception;
        }
    }

    /** Given a Document, generate a String.
     *  @param document The document to be converted to a string.
     *  @return A string representation of the Document.
     *  @exception TransformerException Thrown if there is a
     *  a problem creating a new Transformer or parser.
     *  @exception IOException Thrown if there is a problem closing the output
     *  stream.
     */
    public static String toString(Document document)
            throws TransformerException, IOException {
        // FIXME: Joern's sample code had this in it, but if we
        // include it, then we get Provider   not found errors
        //String defaultDBFI =
        //    System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        //System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
        //                   defaultDBFI == null ? "" : defaultDBFI);

        OutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(outputStream);
            TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.transform(new DOMSource(document), result);
            return outputStream.toString();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /** Transform a document.
     *  @param inputDocument The Document to be transformed
     *  @param xslFileName The file name of the xsl file to be used.
     *  If the file cannot be found, then we look up the file in the classpath.
     *  @return a transformed document
     *  @exception TransformerException Thrown if there is a
     *  a problem with the transform.
     *  @exception IOException Thrown if there is a problem
     *  finding the transform file.
     */
    public static Document transform(Document inputDocument,
            String xslFileName) throws TransformerException, IOException {
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory
                .newTransformer(new StreamSource(xslFileName));
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            try {
                // We might be in the Swing Event thread, so
                // Thread.currentThread().getContextClassLoader()
                // .getResource(entry) probably will not work.
                Class refClass = Class.forName("ptolemy.util.XSLTUtilities");
                URL entryURL =
                    refClass.getClassLoader().getResource(xslFileName);
                if (entryURL != null) {
                    transformer = transformerFactory
                        .newTransformer(new StreamSource(entryURL.toString()));
                } else {
                    IOException exception =
                        new IOException("Failed to open '" + xslFileName
                                + "'");
                    exception.initCause(ex);
                    throw exception;
                }

            } catch (Exception ex2) {
                IOException exception =
                    new IOException("Failed to open \"" + xslFileName + "\".\n"
                            + "Searching the classpath threw:\n" + ex2);
                exception.initCause(ex);
                throw exception;
            }
        }
        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(inputDocument), result);
        return (Document)result.getNode();
    }

    /** Transform a document by applying a list of transforms.
     *  @param inputDocument The Document to be transformed
     *  @param xslFileNames A list of Strings naming the
     *  xsl files to be applied sequentially.
     *  @return a transformed document
     *  @exception TransformerException Thrown if there is a
     *  a problem creating a new Transformer or parser.
     *  @exception IOException Thrown if there is a problem closing the output
     *  stream.
     */
    public static Document transform(Document inputDocument,
            List xslFileNames) throws TransformerException, IOException {
        Iterator fileNames = xslFileNames.iterator();
        while (fileNames.hasNext()) {
            String fileName = (String) fileNames.next();
            inputDocument = transform(inputDocument, fileName);
        }
        return inputDocument;
    }

    /** Transform a document.
     *  @param xsltFileName The name of the xsl file to be used.
     *  @param sourceFileName The name of the file to be read in and
     *  transformed.
     *  @param resultFileName The name of the file to be generated.
     *  @exception IOException Thrown if the resultFileName file
     *  cannot be found or if flushing the output stream throws it.
     *  @exception TransformerException Thrown if there is a
     *  a problem with the transform.
     * the transformation.
     */
    public static void transform (String xsltFileName,
            String sourceFileName,
            String resultFileName)
            throws IOException, TransformerException {
        OutputStream resultStream = null;
        try {
            resultStream = new FileOutputStream(resultFileName);
            StreamSource source = new StreamSource(sourceFileName);
            StreamResult result = new StreamResult(resultStream);
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Transformer transformer = transformerFactory
                .newTransformer(new StreamSource(xsltFileName));
            transformer.setOutputProperty("indent", "yes");
            transformer.transform(source, result);
            resultStream.flush();
        } finally {
            if (resultStream != null) {
                try {
                    resultStream.close();
                } catch (Throwable throwable) {
                    System.out.println("Ignoring failure to close stream "
                            + "on " + resultFileName);
                    throwable.printStackTrace();
                }
            }
        }
    }

    /** Transform a file by applying a list of XSL transforms
     *  @param input The XML to be transformed
     *  @param fileWriter A FileWriter that will write to the MoML
     *  file.
     *  @param xslFileNames A list of Strings naming the
     *  xsl files to be applied sequentially.
     *  @exception ParserConfigurationException Thrown if there is a problem
     *  creating the DocumentBuilder.
     *  @exception TransformerException Thrown if there is a
     *  a problem with the transform.
     *  @exception exception Thrown if there is a problem
     *  finding a transform file or applying a transform.
     */

    public static void transform(String input, FileWriter fileWriter,
            List xslFileNames)
            throws ParserConfigurationException, TransformerException, IOException {
        // This method takes a FileWriter so that the user can
        // ensure that the FileWriter exists and is writable before going
        // through the trouble of doing the conversion.

        Document inputDocument = null;
        try {
            inputDocument = XSLTUtilities.parse(input);
        } catch (IOException ex) {
            // net.sf.saxon.om.DocumentBuilderImpl.parse()
            // can throw a javax.xml.transform.TransformerException
            // which extends Exception, but has  IOException as a cause,
            // so we must catch Exception here, not IOException. 

            // Try it as a jar url
            try {
                URL jarURL =
                    ClassUtilities.jarURLEntryResource(input);
                if (jarURL == null) {
                    throw new IOException("'" + input + "' was not a jar "
                            + "URL, or was not found");
                }
                inputDocument = XSLTUtilities.parse(jarURL.toString());
            } catch (IOException ex2) {
                // FIXME: IOException does not take a cause argument
                throw ex;
            }
        }
        Document outputDocument =
            XSLTUtilities.transform(inputDocument, xslFileNames);

        fileWriter.write(XSLTUtilities.toString(outputDocument));

        // Let the caller close the fileWriter.
        //fileWriter.close();
    }
}
