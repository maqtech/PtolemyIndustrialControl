/* A top-level dialog window for editing parameters of a NamedObj.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.actor.gui.style.StyleConfigurer;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.moml.MoMLChangeRequest;

import java.awt.Frame;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// EditParametersDialog
/**
This class is a modal dialog box for editing the parameters of a target
object, which is an instance of NamedObj. All attributes that implement
the Settable interface and have visibility FULL are included in the
dialog. An instance of this class contains an instance of
Configurer, which examines the target for attributes of type
EditorPaneFactory.  Those attributes, if they are present, define
the panels that are used to edit the parameters of the target.
If they are not present, then a default panel is created.
<p>
If the panels returned by EditorPaneFactory implement the
CloseListener interface, then they are notified when this dialog
is closed, and are informed of which button (if any) was used to
close the dialog.
<p>
The dialog is modal, so that (in lieu of a proper undo mechanism)
the Cancel button can properly undo any
modifications that are made.  This means that the statement that creates
the dialog will not return until the user dismisses the dialog.
The method buttonPressed()
can then be called to find out whether the user clicked the Commit button
or the Cancel button (or any other button specified in the constructor).
Then you can access the component to determine what values were set
by the user.

@author Edward A. Lee
@version $Id$
*/
public class EditParametersDialog extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  A "Commit" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     */
    public EditParametersDialog(Frame owner, NamedObj target) {
        super(owner,
                "Edit parameters for " + target.getName(),
                new Configurer(target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _owner = owner;
        _target = target;
        if (buttonPressed().equals("Add")) {
            _openAddDialog(null, "", "", "ptolemy.data.expr.Parameter");
            _target.removeChangeListener(this);
        } else if (buttonPressed().equals("Remove")) {
            // Create a new dialog to remove a parameter, then open a new
            // EditParametersDialog.
            // First, create a string array with the names of all the
            // parameters.
            List attList = _target.attributeList(Settable.class);

            // Count visible attributes
            Iterator parameters = attList.iterator();
            int count = 0;
            while (parameters.hasNext()) {
                Settable param = (Settable)parameters.next();
                if (param.getVisibility() == Settable.FULL) count++;
            }

            String[] attNames = new String[count];
            Iterator params = attList.iterator();
            int index = 0;
            while (params.hasNext()) {
                Settable param = (Settable)params.next();
                if (param.getVisibility() == Settable.FULL) {
                    attNames[index++] = ((Attribute)param).getName();
                }
            }
            Query query = new Query();
            query.addChoice("delete", "Parameter to delete",
                    attNames, null, false);

            ComponentDialog dialog = new ComponentDialog(
                    _owner,
                    "Delete a parameter for " + _target.getFullName(),
                    query,
                    null);
            // If the OK button was pressed, then queue a mutation
            // to delete the parameter.
            String delName = query.stringValue("delete");

            if (dialog.buttonPressed().equals("OK") && !delName.equals("")) {
                String moml = "<deleteProperty name=\""
                    + delName
                    + "\"/>";
                    _target.addChangeListener(this);
                    _target.requestChange(
                            new MoMLChangeRequest(this, _target, moml));
            }
        } else if (buttonPressed().equals("Edit Styles")) {
            // Create a dialog for setting parameter styles.
            try {
                StyleConfigurer panel = new StyleConfigurer(target);
                ComponentDialog dialog = new ComponentDialog(
                        _owner,
                        "Edit parameter styles for " + target.getName(),
                        panel);
                if (!(dialog.buttonPressed().equals("OK"))) {
                    // Restore original parameter values.
                    panel.restore();
                }
                // Open a new dialog.
                new EditParametersDialog(_owner, _target);

            } catch (IllegalActionException ex) {
                MessageHandler.error("Edit Parameter Styles failed", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that a change has been successfully executed.
     *  This method opens a new parameter editor to replace the one that
     *  was closed.
     *  @param change The change that was executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) return;

        // FIXME: this is ugly..  Why is this necessary?
        // Open a new dialog.
        new EditParametersDialog(_owner, _target);

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) return;

        _target.removeChangeListener(this);

        String newName = _query.stringValue("name");
        ComponentDialog dialog = _openAddDialog(exception.getMessage()
                + "\n\nPlease enter a new default value:",
                newName,
                _query.stringValue("default"),
                _query.stringValue("class"));
        _target.removeChangeListener(this);
        if (!dialog.buttonPressed().equals("OK")) {
            // Remove the parameter, since it seems to be erroneous
            // and the user hit cancel or close.
            String moml = "<deleteProperty name=\"" + newName + "\"/>";
            _target.requestChange(
                    new MoMLChangeRequest(this, _target, moml));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the contents of this dialog implements the CloseListener
     *  interface, then notify it that the window has closed.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals("Commit")
                && !buttonPressed().equals("Add")
                && !buttonPressed().equals("Edit Styles")
                && !buttonPressed().equals("Remove")) {
            // Restore original parameter values.
            ((Configurer)contents).restore();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open a dialog to add a new parameter.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param className The default class name.
     *  @return The dialog that is created.
     */
    private ComponentDialog _openAddDialog(
            String message, String name, String defValue, String className) {
        // Create a new dialog to add a parameter, then open a new
        // EditParametersDialog.
        _query = new Query();
        if (message != null) _query.setMessage(message);
        _query.addLine("name", "Name", name);
        _query.addLine("default", "Default value", defValue);
        _query.addLine("class", "Class", className);
        ComponentDialog dialog = new ComponentDialog(
                _owner,
                "Add a new parameter to " + _target.getFullName(),
                _query,
                null);
        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.stringValue("name");

        // Need to escape quotes in default value.
        String newDefValue = StringUtilities.escapeForXML(
                _query.stringValue("default"));

        if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
            String moml = "<property name=\""
                + newName
                + "\" value=\""
                    + newDefValue.toString()
                        + "\" class=\""
                        + _query.stringValue("class")
                            + "\"/>";
                        _target.addChangeListener(this);
                        _target.requestChange(
                                new MoMLChangeRequest(this, _target, moml));
        }
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Button labels.
    private static String[] _moreButtons
            = {"Commit", "Add", "Remove", "Edit Styles", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The query window for adding parameters.
    private Query _query;

    // The target object whose parameters are being edited.
    private NamedObj _target;
}
