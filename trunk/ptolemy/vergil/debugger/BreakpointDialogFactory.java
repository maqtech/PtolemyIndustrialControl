/* A menu item factory that opens a dialog for setting breakpoints.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Red (celaine@eecs.berkeley.edu)
@AcceptedRating Red (celaine@eecs.berkeley.edu)
*/

package ptolemy.vergil.debugger;

import diva.gui.toolbox.JContextMenu;

import ptolemy.actor.Actor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.toolbox.MenuItemFactory;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

//////////////////////////////////////////////////////////////////////////
//// BreakpointDialogFactory
/**
A factory that creates a dialog box to configure breakpoints for the
actor selected.

@see ptolemy.vergil.kernel.PortDialogFactory

@author Elaine Cheong
@version $Id$
*/
public class BreakpointDialogFactory implements MenuItemFactory {

    /** Create factory.
     *  @param graphController The associated graph controller for the
     *  actor selected.
     */
    public BreakpointDialogFactory(BasicGraphController graphController) {
        super();
        _graphController = graphController;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an item to the given context menu that will open a dialog
     *  to configure breakpoints for an object.
     *  @param menu The context menu.
     *  @param object The object whose breakpoints are being modified.
     */
    public JMenuItem create(final JContextMenu menu, NamedObj object) {
        String name = "Set Breakpoints";
        final NamedObj target = object;

        // Ensure that if we have a ComponentEntity, it is opaque.
        // Also ensure that it is an actor and that it has a director.
        if (!(target instanceof ComponentEntity)
                || !(((ComponentEntity)target).isOpaque())
                || !(target instanceof Actor)
                || (((Actor)target).getExecutiveDirector() == null)) {
            return null;
        }

        Action action = new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {
                // Create a dialog for configuring the object.  First,
                // identify the top parent frame.  Normally, this is a
                // Frame, but just in case, we check.  If it isn't a
                // Frame, then the set breakpoints dialog will not
                // have the appropriate parent, and will disappear
                // when put in the background.
                Component parent = menu.getInvoker();
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
                if (parent instanceof Frame) {
                    new BreakpointConfigurerDialog((Frame)parent,
                            (Entity)target,
                            _graphController);
                } else {
                    new BreakpointConfigurerDialog(null,
                            (Entity)target,
                            _graphController);
                }
            }
        };

        return menu.add(action, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The graph controller associated with the actor selected. */
    private BasicGraphController _graphController;
}
