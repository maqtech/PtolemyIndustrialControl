/* Utilities for manipulating strings.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// StringUtilities
/**
A collection of utilities for manipulating strings.

@author Steve Neuendorffer
@version $Id$
*/
public class StringUtilities {

    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a string, replace all the instances of XML special characters
     *  with their corresponding XML entities.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.  This method
     *  replaces instances of double quotes with "&quot;", instances
     *  of less than with "&lt;" instances of ampersand with "&amp;",
     *  and instances of greater than with "&gt;".
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     */
    public static String escapeForXML(String string) {
        string = substitute(string, "&", "&amp;");
        string = substitute(string, "\"", "&quot;");
        string = substitute(string, "<", "&lt;");
        string = substitute(string, ">", "&gt;");
        return string;
    }

    /** Replace all occurrences of <i>old</i> in the specified
     *  string with <i>replacement</i>.
     *  @param string The string to edit.
     *  @param old The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string,
            String old, String replacement) {
        int start = string.indexOf(old);
        while(start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + old.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(old, start + replacement.length());
        }
        return string;
    }
}
