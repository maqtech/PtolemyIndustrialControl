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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

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
}


