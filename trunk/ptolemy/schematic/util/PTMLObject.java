/* An Icon is the graphical representation of a schematic entity.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.*;
import ptolemy.schematic.xml.XMLElement;

//////////////////////////////////////////////////////////////////////////
//// PTMLObject
/**

A PTMLObject is the base class for any object that can be pulled out of a PTML
file.  This just provides a description.

@author Steve Neuendorffer
@version $Id$
*/
public class PTMLObject extends NamedObj {

    /**
     * Create a new PTMLObject with the name "PTMLObject" and an empty 
     *  description 
     */
    public PTMLObject () {
        super("PTMLObject");
        _description = new String("");
    }

    /**
     * Create a new PTMLObject with the given name and an empty 
     *  description.
     */
    public PTMLObject (String name) {
        super(name);
        _description = new String("");
    }

    /**
     * Return a long description string of this PTMLObject
     */
    public String getDescription() {
        return _description;
    }

   /**
     * Set the string that contains the long description of this PTMLObject.
     */
    public void setDescription(String s) {
        _description = s;
    }

    private String _description;
}

