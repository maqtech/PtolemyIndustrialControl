/* An action that is associated with a figure.

 Copyright (c) 2000 The Regents of the University of California.
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

import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import javax.swing.event.*;
import diva.canvas.*;
import diva.graph.*;
import diva.graph.model.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;

//////////////////////////////////////////////////////////////////////////
//// FigureAction
/** 
An action that is attached to a figure on a named object.  
Such an action is usually fired
in two ways.  The first way is through an ActionInteractor that is attached
to the figure.  The second way is through a context menu that is created
on the figure.  Unfortunately, the source of the event is different in 
these two cases.  This class makes it easy to write an action that is
accessed by either mechanism.

@author Steve Neuendorffer
@version $Id$
*/
public class FigureAction extends AbstractAction {
    
    public FigureAction(String name) {
	super(name);
    }

    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	if(source instanceof Figure) {
	    // Action activated using an ActionInteractor.
	    Figure figure = (Figure) source;
	    Node node = (Node) figure.getUserObject();
	    Icon icon = (Icon) node.getSemanticObject();
	    _target = (NamedObj) icon.getContainer();
	} else if(source instanceof JComponent) {
	    // Action activated using a context menu.
	    JMenuItem item = (JMenuItem) source;
	    BasicContextMenu menu = (BasicContextMenu)item.getParent();
	    _target = (NamedObj) menu.getTarget();
	} else {
	    _target = null;
	}
    }

    public NamedObj getTarget() {
	return _target;
    }

    private NamedObj _target = null;
}
