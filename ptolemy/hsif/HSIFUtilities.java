/* Utilities that operate on HSIF files

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.hsif;

import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.util.XSLTUtilities;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;

//////////////////////////////////////////////////////////////////////////
//// HSIFUtilities
/** Utilities methods for operating on HSIF files.  These methods
are in a separate non-graphical class so that we can test them
as part of the nightly build, or provide non-graphical tools
that use these methods


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.2

*/
public class HSIFUtilities {
    /** Instances of this class cannot be created.
     */
    private HSIFUtilities() {
    }

    /**  Read in an HSIF file, transform it into MoML and write the output
     *	 to a FileWriter.
     *   @param input HSIF file to be read in.
     *   @param fileWriter A FileWriter that will write to the MoML
     *   file.
     *   @exception Exception if there is a problem with the transformation.
     */
    public static void HSIFToMoML(String input, FileWriter fileWriter)
	throws Exception {
	// This method takes a FileWriter so that the user can
	// ensure that the FileWriter exists and is writable before going
	// through the trouble of doing the conversion.

        Document inputDocument = null;
	try {
	    inputDocument = XSLTUtilities.parse(input);
	} catch (FileNotFoundException ex) {
	    // Try it as a jar url
	    try {
		URL jarURL =
		    JNLPUtilities.jarURLEntryResource(input);
		if (jarURL == null) {
		    throw new Exception("'" + input + "' was not a jar "
					+ "URL, or was not found");
		}
	    inputDocument = XSLTUtilities.parse(jarURL.toString());
	    } catch (Exception ex2) {
		// FIXME: IOException does not take a cause argument
		throw ex;
	    }
	}

        List transforms = new LinkedList();

	// The transform() method will look in the classpath.
        transforms.add("ptolemy/hsif/xsl/GlobalVariablePreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/SlimPreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/LocalVariablePreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/SlimPreprocessor.xsl");
        transforms.add("ptolemy/hsif/xsl/HSIF.xsl");

        Document outputDocument =
            XSLTUtilities.transform(inputDocument, transforms);

	fileWriter.write(XSLTUtilities.toString(outputDocument));

	// Let the caller close the fileWriter.
	//fileWriter.close();
    }

    /**  Read in an HSIF file, transform it into MoML and generate an output
     *	 file.  Note that if the output file exists, then it is overwritten.
     *   @param input HSIF file to be read in
     *   @param output The MoMLFile to be generated.
     *   @exception Exception if there is a problem with the transformation.
     */
    public static void HSIFToMoML(String input, String output)
	throws Exception {
	// This method makes it much easier to test the conversion,
	FileWriter fileWriter = new FileWriter(output);
	HSIFToMoML(input, fileWriter);
	fileWriter.close();
    }

    /** Convert the first argument from a HSIF file into a MoML file
     *  named by the second argument.
     *  For example
     *  <pre>
     *  java -classpath $PTII ptolemy.hsif.HSIFUtilities \
     *       $PTII/ptolemy/hsif/demo/SwimmingPool/SwimmingPool.xml \
     *       /tmp/SwimmingPool_moml.xml
     *  </pre>
     *  will read in SwimmingPool.xml and create SwimmingPool_moml.xml
     */
    public static void main(String [] args) throws Exception {
	if (args.length != 2) {
	    System.err.println("Usage: java -classpath $PTII "
			       + "ptolemy.hsif.HSIFUtilities HSIFInputFile "
			       + "MoMLOutputFile");
	    System.exit(2);
	} else {
	    HSIFToMoML(args[0], args[1]);
	}
    }
}
