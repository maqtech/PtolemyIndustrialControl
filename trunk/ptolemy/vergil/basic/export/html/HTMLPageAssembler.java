/* An actor that assemble multiple inputs to a HTML page.

 Copyright (c) 2003-2013 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////HTMLPageAssembler

/**
<p>
HTMLPageAssembler reads contents from its ports and appends them into the
corresponding parts in the template file that must satisfy the following
requirement: for each port, there must be a corresponding DOM object whose ID is
the same as the port name in the template file, or in the content provided to
another port that is created before this port.
</p>
<p>
HTMLPageAssembler processes the ports in the order that they are added to it.
Each port can consists of a single or multiple channels. In the latter case,
contents from multiple channels are appended in the order that they are
connected to this port.
</p>
<p>
The content for a channel can be a string or an array of strings. HTML scripts,
such as JavaScript, can also be part of the content. For a long content, it is
better to first store the content in a separated file, then read this file using
the FileReader actor to provide the content to the port. A demo is available at
$ptII\ptolemy\vergil\basic\export\html\demo\PageAssembler. If the content is
provided through a StringConst actor, only the tags defined in the standard Java
library (javax.swing.text.html.HTML.Tag) can be supported. If the content is
read from a file, then all valid HTML tags can be supported. Unknown tags are
ignored without throwing any exceptions.
</p>
<p>
The content of the final HTML page is broadcasted to the output port, and saved
to a file if specified.
</p>
<p>
For more information, please refer to "Manual for Creating Web Pages" in Ptolemy.
</p>

@author Baobing (Brian) Wang
@version $Id$
@since Ptolemy II 9.0
@Pt.ProposedRating Yellow (bwang)
@Pt.AcceptedRating Yellow (bwang)
*/

public class HTMLPageAssembler extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public HTMLPageAssembler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        template = new FileParameter(this, "template");
        template.setDisplayName("Template HTML file");
        template.setExpression("template.html");

        htmlTitle = new StringParameter(this, "htmlTitle");
        htmlTitle.setDisplayName("HTML page title");
        htmlTitle.setExpression("Page Generated by HTMLPageAssembler");

        saveToFile = new Parameter(this, "saveToFile");
        saveToFile.setDisplayName("Save the new HTML page to a separate file");
        saveToFile.setTypeEquals(BaseType.BOOLEAN);
        saveToFile.setExpression("false");

        outputFile = new FileParameter(this, "outputFile");
        outputFile.setExpression("result.html");
        outputFile.setDisplayName("Output file");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The title of the generated HTML page.  The default value
     *  is the string "Page Generated by HTMLPageAssembler".
     */
    public StringParameter htmlTitle;

    /** The output port, which is of type String. */
    public TypedIOPort output;

    /** The file to save the content of the generated page.
     *  The default value is "result.html".
     */
    public FileParameter outputFile;

    /**
     * Specify whether the content of the generated page should be save to a
     * separated file.  The default value is a boolean with the value false.
     */
    public Parameter saveToFile;

    /** The template file. The default value is "template.html". */
    public FileParameter template;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException {
        super.fire();
        _htmlKit = new HTMLEditorKit();
        _htmlDoc = (HTMLDocument) _htmlKit.createDefaultDocument();
        _htmlDoc.setPreservesUnknownTags(true);
        try {
            //parser.parse(template.openForReading(), callback, true);
            _htmlKit.read(template.openForReading(), _htmlDoc, 0);

            // Set the HTML page title
            _htmlDoc.putProperty(Document.TitleProperty, htmlTitle
                    .stringValue().trim());

            /*
             * Insert the content from each port to its corresponding div.
             * If the name of a port doesn't match any DOM object in the
             * template file, or in the extra div list, throw an exception.
             */

            List<TypedIOPort> portList = inputPortList();
            for (TypedIOPort port : portList) {
                String divID = port.getName();

                Element divElement = _htmlDoc.getElement(divID);
                if (divElement == null) {
                    throw new IllegalActionException(this,
                            "Cannot find a \"div\" with id = \"" + divID
                                    + "\" in the template file.");
                }
                for (int i = (port.getWidth() - 1); i >= 0; i--) {
                    Token token = port.get(i);
                    StringBuffer htmlText = new StringBuffer();
                    if (token instanceof ArrayToken) {
                        ArrayToken array = (ArrayToken) token;
                        Token[] lines = array.arrayValue();
                        for (Token line : lines) {
                            htmlText.append(((StringToken) line).stringValue()
                                    + "\n");
                        }
                    } else {
                        htmlText.append(((StringToken) token).stringValue()
                                + "\n");

                    }
                    _htmlDoc.insertAfterStart(divElement, htmlText.toString());
                }
            }

            // check syntax errors
            ElementIterator iterator = new ElementIterator(_htmlDoc);
            Element element;
            while ((element = iterator.next()) != null) {
                AttributeSet attributes = element.getAttributes();

                Enumeration<?> attriList = attributes.getAttributeNames();
                while (attriList.hasMoreElements()) {
                    Object item = attriList.nextElement();
                    if (item instanceof HTML.UnknownTag) {
                        throw new IllegalActionException(this,
                                "Unknown HTML tag: " + item.toString());
                    }
                }
            }

            // send the result to the output, and save to the file if required

            StringWriter stringWriter = new StringWriter();
            // Manually add the DOCTYPE tag.  HTMLEditorKit removes this.
            stringWriter.write("<!DOCTYPE html>");
            // FIXME:  HTMLEditorKit appears to be replacing tags with color
            // styling, such as <b style="color:blue">, with <font> tags
            // The <font> tag is not supported in HTML5
            // Is HTMLEditorKit the best here?  Is there another editor that
            // is up to date with HTML5?
            _htmlKit.write(stringWriter, _htmlDoc, 0, _htmlDoc.getLength());

            /*
             *  HTML script is commented out originally. Thus, we need to remove
             *  the comment marks.
             */
            // FIXME:  Which script was commented out?  Why?
            // This code uncomments ALL comments, meaning that any comments in
            // the source HTML file will be uncommented and will be visible
            // as text on the response page
            String content = stringWriter.toString();
            content = content.replaceAll("<!--", "").replaceAll("-->", "");

            output.broadcast(new StringToken(content));

            if (((BooleanToken) saveToFile.getToken()).booleanValue()) {
                outputFile.openForWriting().write(content);
                outputFile.close();
            }
            template.close();

        } catch (BadLocationException e) {
            throw new IllegalActionException(this, e,
                    "Cannot insert into the template file: "
                            + template.getExpression());
        } catch (IOException e) {
            throw new IllegalActionException(this, e,
                    "Cannot read or write a file");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private HTMLDocument _htmlDoc;
    private HTMLEditorKit _htmlKit;
}
