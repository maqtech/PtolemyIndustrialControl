/* An Icon whose pattern is in an icon library.

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

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.Configurable;
import ptolemy.moml.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.net.URL;
import java.io.*;
import collections.*;
import ptolemy.schematic.xml.XMLElement;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;

//////////////////////////////////////////////////////////////////////////
//// LibraryIcon
/**

An icon is the graphical representation of a schematic entity.
Icons are stored hierarchically in icon libraries.   Every icon has a 
name, along with a graphical representation.

This icon is for those based on XML.  If the icon is never configured, then
it will have a default figure.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class LibraryIcon extends PatternIcon implements Configurable {

    /**
     * Create a new icon with the name "Icon" in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public LibraryIcon (NamedObj container) 
            throws NameDuplicationException, IllegalActionException {
        this(container, "_icon");
    }

    /**
     * Create a new icon with the name "EditorIcon" in the given container. 
     */
    public LibraryIcon (NamedObj container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Configure the object with data from the specified input stream.
     *  This method is defined to throw a very general exception to allow
     *  classes that implement the interface to use whatever exceptions
     *  are appropriate.
     *  @param base The base relative to which references within the input
     *   stream are found, or null if this is not known.
     *  @param in InputStream
     *  @exception Exception If the stream cannot be read or its syntax
     *   is incorrect.
     */
    public void configure(URL base, InputStream in) throws Exception {
	int bytesread = 0;
	// There must be a better way to do this.
	byte[] b = new byte[100];
	bytesread = in.read(b, 0, 100);
	if(bytesread > 90) {
	    throw new RuntimeException("buffer overrun");
	}
	String name = new String(b, 0, bytesread);
	System.out.println(name);
	// FIXME get the name from the input stream.
        IconLibrary library = LibraryIcon.getIconLibrary();
	setPattern((EditorIcon)library.findIcon(name));
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
	String str = super.toString() + "(";
        return str + ")";
    }

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(detail, indent, 0);
        else 
            result += super._description(detail, indent, 1);
	//	result += " graphics {\n";	
        //result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    public static IconLibrary getIconLibrary() {
	return _iconLibrary;
    }

    public static void setIconLibrary(IconLibrary library) {
	_iconLibrary = library;
    }

    private static IconLibrary _iconLibrary;

}

