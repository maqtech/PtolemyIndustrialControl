/* An Object for changing the style of parameters.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// StyleConfigurer
/**
This class is an editor for the styles of the parameters of an object.
It allows a user to graphically change the ParameterEditorStyles contained
within the user settable attributes of a named object.
It is very similar in spirit and style to Configurer, which edits the actual
values of the attributes.
<p>
The restore() method restores the values of the parameters of the
object to their values when this object was created.  This can be used
in a modal dialog to implement a cancel button, which restores
the styles to those before the dialog was opened.

@see ptolemy.actor.gui.Configurer
@see ParameterEditorStyle
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class StyleConfigurer extends Query implements QueryListener {

    /** Construct a configurer for the specified object.
     *  @param object The object to configure.
     *  @exception IllegalActionException If the specified object has
     *   no editor factories, and refuses to acceptable as an attribute
     *   an instance of EditorPaneFactory.
     */
    public StyleConfigurer(NamedObj object) throws IllegalActionException {
        super();
	this.addQueryListener(this);
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _object = object;

	setTextWidth(25);

	try {
	    // FIXME this list should not be statically specified.
	    // Note that fixing this will probably move the accept method
	    // into some sort of factory object (instead of cloning
	    // existing styles).
	    parameterStyles = new ParameterEditorStyle[5];
	    parameterStyles[0] = new LineStyle();
	    parameterStyles[0].setName("Line");
	    parameterStyles[1] = new CheckBoxStyle();
	    parameterStyles[1].setName("Check Box");
	    parameterStyles[2] = new ChoiceStyle();
	    parameterStyles[2].setName("Choice");
	    parameterStyles[3] = new EditableChoiceStyle();
	    parameterStyles[3].setName("EditableChoice");
	    parameterStyles[4] = new FileChooserStyle();
	    parameterStyles[4].setName("FileChooser");
	} catch (NameDuplicationException ex) {
	    throw new InternalErrorException(ex.getMessage());
	}

	Iterator parameters
            = object.attributeList(Settable.class).iterator();
        while (parameters.hasNext()) {
            Settable param = (Settable)parameters.next();
            // Skip if the parameter is not visible.
            if (param.getVisibility() == Settable.NONE) continue;

 	    // Get the current style.
	    boolean foundOne = false;
	    Iterator styles = ((NamedObj)param)
                .attributeList(ParameterEditorStyle.class).iterator();
	    ParameterEditorStyle foundStyle = null;
	    while (styles.hasNext()) {
		foundOne = true;
		foundStyle = (ParameterEditorStyle)styles.next();
	    }

	    List styleList = new ArrayList();
	    // The index of the default;
	    int defaultIndex = 0;
	    int count = 0;
	    // Reduce the list of parameters
	    for(int i = 0; i < parameterStyles.length; i++) {
                if(foundOne &&
                        parameterStyles[i].getClass()
                        == foundStyle.getClass()) {
                    defaultIndex = count;
                    if(foundStyle.acceptable(param)) {
                        styleList.add(parameterStyles[i].getName());
                        count++;
                    }
                } else if(parameterStyles[i].acceptable(param)) {
		    styleList.add(parameterStyles[i].getName());
		    count++;
		}
	    }
	    String styleArray[] =
		(String[])styleList.toArray(new String[count]);

	    addChoice(param.getName(), param.getName(),
                    styleArray, styleArray[defaultIndex]);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to notify that one of the entries has changed.
     *  The name of the entry is passed as an argument.
     *  @param name The name of the entry.
     */
    public void changed(String name) {
	ParameterEditorStyle found = null;
	Attribute param = _object.getAttribute(name);
	for(int i = 0; i < parameterStyles.length && found == null; i++) {
	    if(stringValue(name).equals(parameterStyles[i].getName())) {
		found = parameterStyles[i];
	    }
	}
	Iterator styles
	    = param.attributeList(ParameterEditorStyle.class).iterator();
	ParameterEditorStyle style = null;
	try {
	    while (styles.hasNext()) {
		style = (ParameterEditorStyle)styles.next();
		style.setContainer(null);
	    }
	    style = (ParameterEditorStyle)found.clone(_object.workspace());
	    style.setName(style.uniqueName("style"));
	    style.setContainer(param);
	} catch (Exception ex) {
	    System.out.println(ex.getMessage());
	}
    }

    /** Request restoration of the parameter values to what they
     *  were when this object was created.  The actual restoration
     *  occurs later, in the UI thread, in order to allow all pending
     *  changes to the parameter values to be processed first.
     */
    public void restore() {
        // This is done in the UI thread in order to
        // ensure that all pending UI events have been
        // processed.  In particular, some of these events
        // may trigger notification of new parameter values,
        // which must not be allowed to occur after this
        // restore is done.  In particular, the default
        // parameter editor has lines where notification
        // of updates occurs when the line loses focus.
        // That notification occurs some time after the
        // window is destroyed.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Iterator entries = _originalValues.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry)entries.next();
                    Settable param = (Settable)
                        _object.getAttribute((String)entry.getKey());
                    try {
                        param.setExpression((String)entry.getValue());
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(
                                "Cannot restore style value!");
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The object that this configurer configures.
    private NamedObj _object;

    // The original values of the parameters.
    private Map _originalValues = new HashMap();

    // The list of the possible styles.
    private ParameterEditorStyle parameterStyles[];
}
