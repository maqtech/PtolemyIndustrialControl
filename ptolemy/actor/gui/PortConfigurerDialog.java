/* A top-level dialog window for configuring the ports of an entity.

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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;

import java.awt.Frame;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurerDialog
/**
This class is a modal dialog box for configuring the ports of an entity.
An instance of this class contains an instance of PortConfigurer.
The dialog is modal, so the statement that creates the dialog will
not return until the user dismisses the dialog.

@see PortConfigurer
@author Edward A. Lee
@version $Id$
*/
public class PortConfigurerDialog extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  Several buttons are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose ports are being configured.
     */
    public PortConfigurerDialog(Frame owner, Entity target) {
        super(owner,
                "Configure ports for " + target.getName(),
                new PortConfigurer(target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _owner = owner;
        _target = target;
        if (buttonPressed().equals("Add")) {
            _openAddDialog(null, "", "", "ptolemy.actor.TypedIOPort");
            _target.removeChangeListener(this);
        } else if (buttonPressed().equals("Remove")) {
            // Create a new dialog to remove a port then open a new
            // PortConfigurerDialog.
            // First, create a string array with the names of all the
            // ports.
            List portList = _target.portList();
            String[] portNames = new String[portList.size()];
            Iterator ports = portList.iterator();
            int index = 0;
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                portNames[index++] = port.getName();
            }
            Query query = new Query();
            query.addChoice("delete", "Port to delete",
                    portNames, null, false);

            ComponentDialog dialog = new ComponentDialog(
                    _owner,
                    "Delete a port for " + _target.getFullName(),
                    query,
                    null);
            // If the OK button was pressed, then queue a mutation
            // to delete the port.
            if (dialog.buttonPressed().equals("OK")) {

                String portName = query.stringValue("delete");
                if (portName != null) {
                    Port port = _target.getPort(portName);

                    if (port != null) {
                        // The context for the MoML should be the first
                        // container above this port in the hierarchy
                        // that defers its MoML definition, or the
                        // immediate parent if there is none.
                        NamedObj container
                            = MoMLChangeRequest.getDeferredToParent(port);
                        if (container == null) {
                            container = (NamedObj)port.getContainer();
                        }

                        String moml = "<deletePort name=\""
                            + port.getName(container) + "\"/>\n";

                            ChangeRequest request =
                                new MoMLChangeRequest(this, container, moml);
                            container.addChangeListener(this);
                            container.requestChange(request);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the listener that a change has been successfully executed.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change.getSource() != this) return;

        // Open a new dialog.
        PortConfigurerDialog dialog = new PortConfigurerDialog(_owner, _target);

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change.getSource() != this) return;

        _target.removeChangeListener(this);

        if (!change.isErrorReported()) {
            MessageHandler.error("Change failed: ", exception);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals("Cancel")) {
            ((PortConfigurer)contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open a dialog to add a new port.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param className The default class name.
     *  @return The dialog that is created.
     */
    private ComponentDialog _openAddDialog(
            String message, String name, String defValue, String className) {
        // Create a new dialog to add a port, then open a new
        // PortConfigurerDialog.
        _query = new Query();
        if (message != null) _query.setMessage(message);
        _query.addLine("name", "Name", name);
        _query.addLine("class", "Class", className);
        ComponentDialog dialog = new ComponentDialog(
                _owner,
                "Add a new port to " + _target.getFullName(),
                _query,
                null);
        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.stringValue("name");

        if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
            String moml = "<port name=\""
                + newName
                + "\" class=\""
                    + _query.stringValue("class")
                        + "\"/>";
                    _target.addChangeListener(this);
                    _target.requestChange(new MoMLChangeRequest(this, _target, moml));
        }
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Button labels.
    private static String[] _moreButtons
            = {"Commit", "Add", "Remove", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The query window for adding parameters.
    private Query _query;

    // The target object whose ports are being configured.
    private Entity _target;
}
