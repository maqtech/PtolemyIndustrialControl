/* A simple graph view for Ptolemy models

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import diva.graph.GraphPane;

import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.vergil.ptolemy.GraphFrame;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphFrame
/**
This is a graph editor frame for ptolemy FSM models.  Given a composite
entity and a tableau, it creates an editor and populates the menus
and toolbar.  This overrides the base class to associate with the
editor an instance of FSMGraphController.

@author  Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
*/
public class FSMGraphFrame extends GraphFrame {

    /** Construct a frame associated with the specified FSM model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public FSMGraphFrame(CompositeEntity entity, Tableau tableau) {
	super(entity, tableau);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
	super._addMenus();

        // Add any commands to graph menu and toolbar that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems = {
            new JMenuItem("Listen to State Machine", KeyEvent.VK_L),
            new JMenuItem("Animate", KeyEvent.VK_A),
            new JMenuItem("Stop Animating", KeyEvent.VK_S),
        };
        // NOTE: This has to be initialized here rather than
        // statically because this method is called by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);
        DebugMenuListener debugMenuListener = new DebugMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }
        _menubar.add(_debugMenu);
    }

    /** Close the window.  Override the base class to remove the debug
     *  listener, if there is one.
     *  @return False if the user cancels on a save query.
     */
    protected boolean _close() {
        getModel().removeDebugListener(_controller);
        return super._close();
    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {
	_controller = new FSMGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
	final FSMGraphModel graphModel = new FSMGraphModel(getModel());
	return new GraphPane(_controller, graphModel);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/doc/vergilFsmEditorHelp.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    // The controller is protected so that the subclass
    // (InterfaceAutomatonGraphFrame) can set it to a more specific
    // controller.
    protected FSMGraphController _controller;

    /** Debug menu for this frame. */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Listener for debug menu commands. */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command. */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to State Machine")) {
                    Effigy effigy = (Effigy)getTableau().getContainer();
                    // Create a new text effigy inside this one.
                    Effigy textEffigy = new TextEffigy(effigy,
                            effigy.uniqueName("debug listener"));
                    DebugListenerTableau tableau =
                            new DebugListenerTableau(textEffigy,
                            textEffigy.uniqueName("debugListener"));
                    tableau.setDebuggable(getModel());
                } else if (actionCommand.equals("Animate")) {
                    // To support animation.
                    getModel().addDebugListener(_controller);
                } else if (actionCommand.equals("Stop Animating")) {
                    getModel().removeDebugListener(_controller);
                    _controller.clearAnimation();
                }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning(
                            "Failed to create debug listener: " + ex);
                } catch (CancelException exception) {}
            }
        }
    }
}
