/* An GraphicElement is an atomic piece of a graphical representation.

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
import diva.canvas.toolbox.*;
import diva.util.java2d.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// GraphicElement
/**
An GraphicElement is an atomic piece of a graphical representation.
i.e. a line, box, textbox, etc.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class GraphicElement extends Object {

    /**
     * Create a new GraphicElement with the given type.
     * By default, the GraphicElement contains no graphic
     * representations.
     * @param attributes a CircularList from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public GraphicElement (String type) {
        _attributes = (LLMap) new LLMap();
        _type = type;
        _label = "";
    }

    /**
     * Return an enumeration of all the attributeNames of this graphic element
     */
    public Enumeration attributeNames() {
        return _attributes.keys();
    }
        
    /** 
     * Return the type of this graphic element.  
     * The type is immutably set when the element is created.
     */
    public String getType() {
        return _type;
    }

    /**
     * Return the value of the attribute with the given name.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public String getAttribute (String name) {
        return (String) _attributes.at(name);
    }

    /**
     * Return the label of this graphic element. This is 
     * primarily useful for textual elements, but may be used for other
     * objects that have a label.
     */
    public String getLabel() {
        return _label;
    }

    /**
     * Return a painted object that looks like this graphic element
     */
    public PaintedObject getPaintedObject() {
	String type = getType();
	String label = getLabel();
	HashMap map = new HashMap();
	for (Enumeration j = attributeNames(); j.hasMoreElements(); ) {
	    String key = (String) j.nextElement();
	    String val = (String) getAttribute(key);
	    map.put(key,val);
	}
	PaintedObject paintedObject = 
	    GraphicsParser.createPaintedObject(type, map, label);

	if(paintedObject == null) 
	    return GraphicElement._errorObject;

        return paintedObject;
    }

    /**
     * Test if this schematic has the attribute wuth the given name.
     */
    public boolean hasAttribute (String name) {
        return _attributes.includesKey(name);
    }

    /**
     * Remove an attribute from this element
     */
    public void removeAttribute(String name) {
        _attributes.removeAt(name);
    }

   /**
     * Set the attribute with the given name to the given value.
     * Throw an exception if there is no attribute with the
     * given name in this schematic.
     */
    public void setAttribute (String name, String value) {
        _attributes.putAt(name, value);
    }

    /** Set the label for this graphic element.  This is usually parsed from
     * the content portion of an XML entity.
     */
    public void setLabel (String name) {
        _label = name;
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        String result = "{";
        result += getClass().getName() + " {" + _type + "}";
	result += " attributes {";
	Enumeration attributeNames = attributeNames();
        while (attributeNames.hasMoreElements()) {
            String p = (String) attributeNames.nextElement();
            result += " {" + p + "=" + getAttribute(p) + "}";
        }
	
        result += "} label {" + getLabel() + "}}";

        return result;       
    }

    private static final PaintedString _errorObject = 
	new PaintedString("ERROR!");
    
    private LLMap _attributes;
    private String _type;
    private String _label;
}

    
