/* Top-level window containing a Ptolemy II model.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

// FIXME: To do:
//  - Fix printing.
//  - Handle file changes (warn when discarding modified models).

package ptolemy.actor.gui;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.Director;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Documentation;

// Java imports
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

//////////////////////////////////////////////////////////////////////////
//// ModelFrame
/**
ModelFrame is a top-level window containing a Ptolemy II model control panel.
It contains a ModelPane, and has a menu bar and a status bar for
message reporting.

@see ModelPane
@author Edward A. Lee
@version $Id$
*/
public class ModelFrame extends PtolemyFrame implements ExecutionListener {

    /** Construct a frame to control the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This creates an instance of ModelPane and puts it in a top-level window.
     *  @see ModelPane
     *  @param model The model to put in this frame, or null if none.
     */
    public ModelFrame(CompositeActor model) {
        this(model, null);
    }

    /** Construct a frame to control the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This creates an instance of ModelPane and puts it in a top-level window.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see ModelPane
     *  @see Tableau#show()
     *  @param model The model to put in this frame, or null if none.
     *  @param tableau The tableau responsible for this frame, or null if none.
     */
    public ModelFrame(CompositeActor model, Tableau tableau) {
        super(model, tableau);

        _pane = new ModelPane(model);
        getContentPane().add(_pane, BorderLayout.CENTER);

        Manager manager = model.getManager();
        if (manager != null) {
            manager.addExecutionListener(this);
        }

        // Make the go button the default.
        _pane.setDefaultButton();

        // FIXME: Need to do something with the progress bar in the status bar.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Report that an execution error has occurred.  This method
     *  is called by the specified manager.
     *  @param manager The manager calling this method.
     *  @param ex The exception being reported.
     */
    public void executionError(Manager manager, Exception ex) {
        report(ex);
    }

    /** Report that execution of the model has finished.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        report("execution finished.");
    }

    /** Report that a manager state has changed.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newstate = manager.getState();
        if (newstate != _previousState) {
            report(manager.getState().getDescription());
            _previousState = newstate;
        }
    }

    /** Return the container into which to place placeable objects.
     *  @return A container for graphical displays.
     */
    public ModelPane modelPane() {
        return _pane;
    }

    /** Set background color.  This overrides the base class to set the
     *  background of the contained ModelPane.
     *  @param background The background color.
     */
    public void setBackground(Color background) {
        super.setBackground(background);
        getContentPane().setBackground(background);
        // This seems to be called in a base class constructor, before
        // this variable has been set. Hence the test against null.
        if (_pane != null) _pane.setBackground(background);
    }

    /** Set the associated model.
     *  @param model The associated model.
     */
    public void setModel(CompositeActor model) {
        super.setModel(model);
        if (model != null) {
            if (_pane != null) _pane.setModel(model);
            Manager manager = model.getManager();
            if (manager != null) {
                manager.addExecutionListener(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            setModel(new CompositeActor());
            return true;
        } else {
            return false;
        }
    }

    /** Close the window.  Override the base class to remove the
     *  execution listener from the manager, and to notify the contained
     *  ModelPane.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        boolean result = super._close();
        CompositeEntity model = getModel();
        if (model instanceof CompositeActor) {
            Manager manager = ((CompositeActor)model).getManager();
            if (manager != null) {
                manager.removeExecutionListener(this);
            }
        }
        if (_pane != null) {
            _pane.stopRun();
        }
        // The second argument is supposed to be a button name, but there
        // is no button that would have triggered this.
        _pane.windowClosed(this, "");
        return result;
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        String message = "Ptolemy II model.";
        CompositeEntity model = getModel();
        if (model != null) {
            String tip = Documentation.consolidate(model);
            if (tip != null) {
                message = "Ptolemy II model:\n" + tip;
            }
        }
        JOptionPane.showMessageDialog(this, message,
                "About " + getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The pane in which the model data is displayed.
    private ModelPane _pane;

    // The previous state of the manager, to avoid reporting it if it hasn't
    // changed.
    private Manager.State _previousState;
}
