/* A menu item factory that opens a dialog for adding ports.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PortConfigurerDialog;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.MenuItemFactory;

import diva.gui.toolbox.JContextMenu;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

//////////////////////////////////////////////////////////////////////////
//// PortDialogFactory
/**
A factory that creates a dialog to configure, add, or remove ports
from objects.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
*/
public class PortDialogFactory implements MenuItemFactory {

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Add an item to the given context menu that will open a dialog
     *  to add or remove ports from an object.
     *  @param menu The context menu.
     *  @param object The object whose ports are being manipulated.
     */
    public JMenuItem create(final JContextMenu menu, NamedObj object) {
        String name = "Configure Ports";

        // Removed this method since it was never used. EAL
        // final NamedObj target = _getItemTargetFromMenuTarget(object);
        final NamedObj target = object;

        // ensure that we actually have a target, and that it's an Entity.
        if(!(target instanceof Entity)) return null;
        Action action = new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {
                // Create a dialog for configuring the object.
                // First, identify the top parent frame.
                // Normally, this is a Frame, but just in case, we check.
                // If it isn't a Frame, then the edit parameters dialog
                // will not have the appropriate parent, and will disappear
                // when put in the background.
                Component parent = menu.getInvoker();
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }
                if (parent instanceof Frame) {
                    new PortConfigurerDialog((Frame)parent,
                            (Entity)target,
                            _configuration);
                } else {
                    new PortConfigurerDialog(null,
                            (Entity)target,
                            _configuration);
                }
            }
        };
	return menu.add(action, name);
    }

    /** Set the configuration for use by the help screen.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** The configuration. */
    private Configuration _configuration;
}
