/* Commonly used static functions for manipulating Strings.

Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang;

import java.io.File;

/** Commonly used static functions for manipulating Strings.
@author Jeff Tsay
@version $Id$
 */
public class StringManip {

    /** Return the portion of the substring after the final period '.', if any.
     *  Note that the input string is usually either a classname or a
     *  file name.
     */
    public static final String unqualifiedPart(String qualifiedName) {
        return partAfterLast(qualifiedName, '.');
    }

    /** Return the substring that follows after the last file
     *  file separator.
     */
    public static final String baseFilename(String filename) {
        return partAfterLast(filename, File.separatorChar);
    }

    /** Return the substring that follows the last occurrence of the
     *  argument character in the argument string. If the
     *  character does not occur, return the whole string.
     */
    public static final String partAfterLast(String string, char c) {
        return string.substring(string.lastIndexOf(c) + 1);
    }

    /** Return the substring that precedes the last occurrence of the
     *  argument character in the argument string. If the
     *  character does not occur, return the whole string.
     */
    public static final String partBeforeLast(String string, char c) {
        return string.substring(0, string.lastIndexOf(c));
    }
}
