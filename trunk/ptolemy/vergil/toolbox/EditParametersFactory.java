/* A menu item factory that creates actions for editing parameters.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import diva.gui.toolbox.JContextMenu;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

//////////////////////////////////////////////////////////////////////////
//// EditParametersFactory
/**
A factory that adds an action to the given context menu that will configure
parameters on the given object.

@author Steve Neuendorffer
@version $Id$
*/
public class EditParametersFactory extends MenuItemFactory {

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.
     *  @param menu The context menu.
     *  @param object The object whose parameters are being configured.
     */
    public JMenuItem create(JContextMenu menu, NamedObj object) {
	String name = _getName();
	final NamedObj target = _getItemTargetFromMenuTarget(object);
	// ensure that we actually have a target.
	if(target == null) return null;
	Action action = new AbstractAction(name) {
	    public void actionPerformed(ActionEvent e) {
		// Create a dialog for configuring the object.
                // FIXME: First argument below should be a parent window
                // (a JFrame).
                new EditParametersDialog(null, target);
	    }
	};
	return menu.add(action, name);
    }

    /**
     * Get the name of the items that will be created.  This is provided so
     * that factory can be overriden slightly with the name changed.
     */
    protected String _getName() {
	return "Edit Parameters";
    }
}
