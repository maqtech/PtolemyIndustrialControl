/* An attribute that contains documentation for the container.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.moml;

import ptolemy.kernel.util.*;

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Documentation
/** 
An attribute that contains documentation for the container.

@author  Edward A. Lee
@version $Id$
*/
public class Documentation extends Attribute {

    /** Construct an attribute with the specified container and name.
     *  The documentation contained by the attribute is initially empty,
     *  but can be set using the setValue() method.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */	
    public Documentation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return as a single string all the documentation associated with
     *  the specified object.  Each attribute of type of class Documentation
     *  that the object contains contributes to the documentation.
     *  The text contributed by each such attribute starts on a new line.
     *  If there are no such attributes, then null is returned.
     *  @param The object to document.
     *  @return The documentation for the object.
     */
    public static String consolidate(NamedObj object) {
        List docList = object.attributeList(Documentation.class);
        if (docList.size() > 0) {
            StringBuffer doc = new StringBuffer();
            Iterator segments = docList.iterator();
            while (segments.hasNext()) {
                Documentation segment = (Documentation)segments.next();
                doc.append(segment.getValue());
                if (segments.hasNext()) doc.append("\n");
            }
            return doc.toString();
        } else {
            return null;
        }
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth.  This class is directly supported by the MoML
     *  "doc" element, so we generate MoML of the form
     *  "&lt;doc&gt;<i>documentation</i>&lt;/doc&gt;", where
     *  <i>documentation</i> is replaced by the string value of this
     *  attribute.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @throws IOException If an I/O error occurs.
     *  @see NamedObj#_exportMoMLContents
     */
    public void exportMoML(Writer output, int depth) throws IOException {
        output.write(_getIndentPrefix(depth)
               + "<doc>"
               + _value
               + "</doc>\n");
    }

    /** Get the documentation as a string.
     *  @return The documentation.
     */	
    public String getValue() {
        return _value;
    }

    /** Set the documentation string.
     *  @param value The documentation.
     */	
    public void setValue(String value) {
        _value = value;
    }

    /** Get the documentation as a string, with the class name prepended.
     *  @return A string describing the object.
     */	
    public String toString() {
        return "(" + getClass().getName() + ", " + _value + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The string value of the documentation.
    private String _value;
}
