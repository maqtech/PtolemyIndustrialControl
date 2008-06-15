/* Display an array of records that arrives on the input port.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.actor.lib.database;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.BoxedValueIcon;
import ptolemy.vergil.toolbox.ArrayOfRecordsConfigureFactory;

//////////////////////////////////////////////////////////////////////////
//// ArrayOfRecordsRecorder

/**
 An actor that displays a record that arrives on its input port.
 The record (or a specified subset of it) is displayed in the icon
 of the actor. In addition, double clicking on the actor displays
 the entire record.

 @author Edward A. Lee
 @version $Id: Occupants.java 49820 2008-06-14 18:56:43Z eal $
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayOfRecordsRecorder extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayOfRecordsRecorder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(false);

        // Force the type to contain at least a record.
        String[] labels = new String[0];
        Type[] types = new Type[0];
        RecordType type = new RecordType(labels, types);
        input.setTypeAtMost(new ArrayType(type));
        
        records = new Parameter(this, "records");
        // Declare that it must be an array of records.
        records.setTypeAtMost(new ArrayType(type));
        records.setToken(new ArrayToken(type));

        contents = new StringParameter(this, "contents");
        contents.setVisibility(Settable.EXPERT);
        
        // FIXME: This should be in the library, not in the
        // Java code, since it depends on vergil.
        BoxedValueIcon icon = new BoxedValueIcon(this, "_icon");
        icon.displayHeight.setExpression("100");
        icon.displayWidth.setExpression("20");
        icon.attributeName.setExpression("contents");
        
        // Customize the interaction by inserting this property.
        // This is done in the library,
        // not in the Java code, since it depends on vergil.
        /*
        ArrayOfRecordsConfigureFactory factory = new ArrayOfRecordsConfigureFactory(this, "factory");
        factory.parameterName.setExpression("records");
        */
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Parameter to store the display string. */
    public StringParameter contents;

    /** Parameter to store the array of records read at the input.
     *  This is an array of records that is by default empty.
     */
    public Parameter records;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Read the input and update the display.
     *  @throws IllegalActionException If we fail to update the
     *   contents parameter.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // FIXME: We want to parameterize what is shown.
        if (input.hasToken(0)) {
            ArrayToken array = (ArrayToken)input.get(0);
            records.setToken(array);
            records.setPersistent(true);
            StringBuffer display = new StringBuffer();
            for (int i = 0; i < array.length(); i++) {
                RecordToken record = (RecordToken)array.getElement(i);
                if (i > 0) {
                    display.append("\n");
                }
                String desk = sanitize(
                        record.get("deskno").toString(),
                        "?");
                display.append(desk);
                display.append(": ");
                String name = sanitize(
                        record.get("lname").toString(),
                        "VACANT");
                display.append(name);
            }
            // This needs to be recorded via MoML or it isn't persistent.
            // Why?
            String moml = "<property name=\"contents\" value=\""
                + StringUtilities.escapeForXML(display.toString())
                + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, this, moml);
            requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a string, remove quotation marks if it has them.
     *  If the string is then empty, return the specified default.
     *  Otherwise, return the string without quotation marks.
     *  This method also trims white space, unless the white
     *  space is inside quotation marks.
     *  @param string String to sanitize.
     *  @param ifEmpty Default to use if result is empty.
     *  @return A string with no quotation marks that is not empty.
     */
    private String sanitize(String string, String ifEmpty) {
        string = string.trim();
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }
        if (string.trim().equals("")) {
            string = ifEmpty;
        }
        return string;
    }
}
