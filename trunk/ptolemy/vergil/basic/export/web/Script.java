/* Attribute for inserting script content (e.g. Javascript) into a page
 * generated by a web exporter.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// Script
/**
 * A parameter for associating a script (such as Javascript) with an object in
 * a model.
 *
 * @author Edward A. Lee, Elizabeth Latronico
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public abstract class Script extends WebContent implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public Script(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _icon.setIconText("S");
        displayText.setExpression("Web page script to run on containers icon.");

        eventType = new AreaEventType(this, "eventType");

        script = new StringParameter(this, "script");
        TextStyle style = new TextStyle(script, "style");
        style.height.setExpression("5");

        evaluateScript = new Parameter(this, "evaluateScript");
        evaluateScript.setTypeEquals(BaseType.BOOLEAN);
        evaluateScript.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Parameter indicating whether the script text's expression should be
     *  evaluated or not.  Ptolemy interprets the dollar sign to indicate that
     *  the value of a parameter should be inserted; for example, $pi and
     *  a variable pi=3.14 would evaluate to 3.14.  However, many scripting
     *  languages such as jQuery use a dollar sign as part of the script itself
     *  (to refer to jQuery variables).  In this case, we want to insert the
     *  exact plain text into the web page, not the evaluated text.
     */
    public Parameter evaluateScript;

    /** Event type to respond to by executing the command given by
     *  the value of this Script parameter.
     *  The script will be run when the icon corresponding to the
     *  container of this parameter gets one of the following events:
     *  <ul>
     *  <li><b>onblur</b>: Command to be run when an element loses focus.
     *  <li><b>onclick</b>: Command to be run on a mouse click.
     *  <li><b>ondblclick</b>: Command to be run on a mouse double-click.
     *  <li><b>onfocus</b>: Command to be run when an element gets focus.
     *  <li><b>onmousedown</b>: Command to be run when mouse button is pressed.
     *  <li><b>onmousemove</b>: Command to be run when mouse pointer moves.
     *  <li><b>onmouseout</b>: Command to be run when mouse pointer moves out of an element.
     *  <li><b>onmouseover</b>: Command to be run when mouse pointer moves over an element.
     *  <li><b>onmouseup</b>: Command to be run when mouse button is released.
     *  <li><b>onkeydown</b>: Command to be run when a key is pressed.
     *  <li><b>onkeypress</b>: Command to be run when a key is pressed and released.
     *  <li><b>onkeyup</b>: Command to be run when a key is released.
     *  </ul>
     *  These are the events supported by the HTML area tag.
     *  The default is "onmouseover".
     */
    public AreaEventType eventType;

    /** Script to insert in the head section of the
     *  web page. This will normally define a JavaScript function that
     *  will be invoked when the UI event specified by <i>eventType</i>
     *  occurs. By default, this is blank. For example, if the value
     *  of this parameter is the string
    <pre>
    function writeText(text) {
    document.getElementById("xyz").innerHTML = text;
    };
    </pre>
     * and the value of this parameter is "writeText('hello world')",
     * then the HTML element with ID xyz will be populated with the
     * string 'hello world' when the UI action <i>eventType</i> occurs.
     */
    public StringParameter script;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Script is of type text/javascript for backwards compatibility.
     *  return string text/javascript
     */
    // FIXME:  Any other types of scripts?
    // FIXME:  Difference between these mime types?
    // Some info here:  http://stackoverflow.com/questions/6122905/whats-is-difference-between-text-javascript-and-application-javascript
    // .js     application/x-javascript
    // .js     application/javascript
    // .js     application/ecmascript
    // .js     text/javascript
    // .js     text/ecmascript
    @Override
    public String getMimeType() {
        return "text/javascript";
    }

    /** Return true, since new scripts and method calls should overwrite old.
     *
     * @return True, since new scripts and method calls should overwrite old
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }
}
