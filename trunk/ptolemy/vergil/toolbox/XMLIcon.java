/* An icon stored in XML.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.Configurable;
import ptolemy.moml.*;
import java.util.*;
import java.net.URL;
import java.io.*;
import diva.canvas.Figure;
import diva.canvas.toolbox.*;
import diva.util.xml.*;

//////////////////////////////////////////////////////////////////////////
//// XMLIcon
/**
An icon is the graphical representation of a schematic entity.
This icon contains a set of graphic elements.  These graphic elements can 
be added manually or created automatically by configuring the icon with
appropriate XML code.  Each graphic element represents a primitive graphical
object that will be used to create a figure or a Swing icon.   If this
icon contains no graphic elements, then the default figure or Swing icon will
be created.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class XMLIcon extends EditorIcon {

    /**
     * Create a new icon with the given name in the given container.
     * By default, the icon contains no graphic
     * representations.
     */
    public XMLIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _graphics = (LinkedList) new LinkedList();
    }

    /**
     * Add a new graphic element to the icon.
     */
    public void addGraphicElement(GraphicElement g)
            throws IllegalActionException {
        _graphics.add(g);
    }

    /**
     * Test if this icon contains a graphic in the
     * given format.
     */
    public boolean containsGraphicElement(GraphicElement g) {
        return _graphics.contains(g);
    }

    /**
     * Create a background figure based on this icon.  The background figure
     * will be painted with each graphic element that this icon contains.
     */
    public Figure createBackgroundFigure() {
	// FIXME what happens if the description changes?
	NamedObj container = (NamedObj)getContainer();
	ProcessedString description =
	    (ProcessedString)container.getAttribute("iconDescription");
	if(description == null) {
	    return _createDefaultBackgroundFigure();
	}
	
	// Check to see that we got the right instruction
	if(!description.getInstruction().equals("graphml"))
	    return _createDefaultBackgroundFigure();
	try {
	    _update(null, description.getString());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return _createDefaultBackgroundFigure();
	}
	
        Enumeration graphics = graphicElements();
        PaintedFigure figure = new PaintedFigure();
        while(graphics.hasMoreElements()) {
            GraphicElement element = (GraphicElement) graphics.nextElement();
            figure.add(element.getPaintedObject());
        }
        return figure;
    }

    /**
     * Return an unmodifiable list over the graphic elements
     * contained by this icon.
     */
    public List graphicElementList() {
        return Collections.unmodifiableList(_graphics);
    }

    /**
     * Return an enumeration over the graphic elements
     * contained by this icon.
     *
     * @return Enumeration of GraphicElements.
     * @deprecate
     */
    public Enumeration graphicElements() {
        return Collections.enumeration(_graphics);
    }

    /**
     * Remove a graphic element from the icon. Throw an exception if
     * the graphic element is not contained in this icon
     */
    public void removeGraphicElement(GraphicElement g)
            throws IllegalActionException {
        try {
            _graphics.remove(g);
        }
        catch (NoSuchElementException e) {
            throw new IllegalActionException("removeGraphicElement:" +
                    "GraphicElement not found in icon.");
        }
    }

    /**
     * Return a string this representing Icon.
     */
    public String toString() {
        Enumeration els = graphicElements();
        String str = super.toString() + "(";
        while(els.hasMoreElements()) {
            GraphicElement g = (GraphicElement) els.nextElement();
            str += "\n...." + g.toString();
        }
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
	result += " graphics {\n";
	Enumeration graphicElements = graphicElements();
        while (graphicElements.hasMoreElements()) {
            GraphicElement p = (GraphicElement) graphicElements.nextElement();
            result +=  _getIndentPrefix(indent + 1) + p.toString() + "\n";
        }

        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    /** Configure the icon with XML data.  The XML data is given by
     *  either a URL (the <i>source</i> argument), or by text
     *  (the <i>text</i> argument), or both.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL, or null
     *   if none.
     *  @param text Configuration information given as text, or null if
     *   none.
     *  @exception Exception If the stream cannot be read or its syntax
     *   is incorrect.
     */
    private void _update(URL base, String text) throws Exception {
        _graphics.clear();
	
	Reader in = new StringReader(text);
	XmlDocument document = new XmlDocument(base);
	XmlReader reader = new XmlReader();
	reader.parse(document, in);
	XmlElement root = document.getRoot();
	
	Iterator graphics = root.elements();
	while(graphics.hasNext()) {
	    XmlElement graphic = (XmlElement)graphics.next();
	    GraphicElement g = _createGraphicElement(graphic);
	    addGraphicElement(g);
	}
    }

    // Create a new graphic element from the given XML element.
    private GraphicElement _createGraphicElement(XmlElement e)
            throws IllegalActionException {

        String name = e.getType();
        GraphicElement element = new GraphicElement(name);
        Iterator children = e.elements();
        while(children.hasNext()) {
            XmlElement child = (XmlElement)children.next();
            System.out.println("Unrecognized element type = " +
                    child.getType() + " found in " +
                    element.getClass().getName());
        }

        Iterator attributes = e.attributeNames();
        while(attributes.hasNext()) {
            String n = (String) attributes.next();
            String v = e.getAttribute(n);
            element.setAttribute(n, v);
        }

        element.setLabel(e.getPCData());
        return element;
    }

    // The list of graphic elements contained in this icon.
    private List _graphics;
}
