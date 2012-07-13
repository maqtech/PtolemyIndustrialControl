/* Attribute containing information and methods for web elements, 
 * for example, HTML tags and content, as in 
 * <div> This is some HTML content </div> 

 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.vergil.basic.export.web;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// WebElement
/**
 * Attribute containing information and methods for web elements, 
 * for example, HTML tags and content, as in 
 * <div> This is some HTML content </div> 
 * 
 * The full name including the _elementName is used as WebElement's web id.  
 * In an HTML page, elements that have ids are required to have globally 
 * unique ids.  The _webName field is used for the WebElement's web name.
 * Elements can have non-unique web names - this is done for example for radio
 * buttons on web forms.
 * http://solidlystated.com/scripting/html-difference-between-id-and-name/
 * http://stackoverflow.com/questions/1363693/do-input-field-names-have-to-be-unique-across-forms  
 *
 * @author Elizabeth Latronico
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 */

// FIXME:  How to make this non-persistent?  Don't want to save these with the
// model.
public class WebElement extends StringAttribute {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public WebElement(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        setParent("");
        setWebName("");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Factory method for creating WebElements.  Checks if there is an 
     * existing WebElement with the same id (Ptolemy name).  If so, return it; 
     * if not create a new one.  Sets persistent to false.  Static so that any
     * WebExportable may call it.
     * 
     * @param container  The container object for the WebElement
     * @param id The Ptolemy name for the WebElement (WebElement uses this as a 
     * unique id)
     * @param webName The web name of this WebElement
     * @return The WebElement that was created (or that previously existed) 
     * with persistent set to false
     * @throws An IllegalActionException if the WebAttribute cannot be created
     * (perhaps another Attribute exists with the requested name)
     */
    public static WebElement createWebElement(NamedObj container, String id,
            String webName) throws IllegalActionException{
       WebElement webElement;
        
       try {
            if (id != null && container.getAttribute(id) == null)
            {
                webElement = new WebElement(container, id);
                webElement.setPersistent(false);
            }
       } catch(NameDuplicationException e){ 
              throw new IllegalActionException(container, 
               "Cannot create web content.  Duplicate id (Ptolemy name) for" +
               "WebElement: " + id);
       }
        
        webElement = (WebElement) container.getAttribute(id, WebElement.class);
        webElement.setWebName(webName);
        webElement.setPersistent(false);
        webElement.setVisibility(NONE);
        return webElement;
    }
    
    /** Return the name of the desired parent element, or the empty string if
     * none. 
     * 
     * @return The name of the desired parent element, or the empty string if
     * none.
     */
    public String getParent() {
        return _parent;
    }
    

    /** Return the web name of this element; for example, "myElement" in 
     * <div name="myElement"> in HTML.
     * 
     * @return The web name of this element; for example, "myElement" in 
     * <div name="myElement"> in HTML.
     */
    public String getWebName() {
        return _webName;
    }
    
    /** Set the name of the desired parent element.  Can also be a special 
     * constant for tags that do not typically have names, like <head> and 
     * <body>.
     * 
     * @param parent  The name or special constant of the parent element.
     */
    public void setParent(String parent) {
        _parent = parent;
    }
    
    /** Set the web name of this element; for example, "myElement" in 
     * <div name="myElement"> in HTML.
     * 
     * @param webName The web name of this element; for example, "myElement" in 
     * <div name="myElement"> in HTML.
     */
    public void setWebName(String webName) {
        _webName = webName;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** A special constant indicating that the <body> element should
     *  be the parent.
     *  The <head> and <body> tags of an HTML document do not typically
     *  have names.  For example, almost no one writes <head name="head">, 
     *  it's only <head>.  Therefore, the WebExporter will not be able to use
     *  the name to find the parent element.  
     */
    public static final String BODY = "body";
    
    /** A special constant indicating that the <head> element should
     *  be the parent.
     *  The <head> and <body> tags of an HTML document do not typically
     *  have names.  For example, almost no one writes <head name="head">, 
     *  it's only <head>.  Therefore, the WebExporter will not be able to use
     *  the name to find the parent element.  
     */
    public static final String HEAD = "head";
    
    /** Special constants for backwards compatibility to Export to Web. 

     * Parameter specifying the position into which to export HTML text.
     * The parameter offers the following possibilities:
     *  <ul>
     *  <li><b>end</b>: Put the text at the end of the HTML file.
     *  <li><b>head</b>: Put the text in the head section.
     *  <li><b>start</b>: Put the text at the start of the body section.
     *  <li><i>anything_else</i>: Put the text in a div of this name.
     *  </ul>
     *  The default is "start".
     */
    public static final String START = "start";
    public static final String END = "end";
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
   
    /** The name of the desired parent element of this element, if any.
     * For example, if we have the element name "result"
     * <div name="result"> </div> that we want as the parent:
     * <div name="result"> <div name="thisElement"> </div> </div>
     * Please see {@link ptolemy.vergil.basic.export.web.HTMLTextPosition} for some
     * more examples.  If there is no parent element, _position is set to 
     * the empty string.
     */
    private String _parent;
    
    /** The desired name of this element in the web file, for example, 
     * "myElement" in <div name="myElement"></div>.
     */
    private String _webName;
    
}