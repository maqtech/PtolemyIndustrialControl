/* A selection window for editing breakpoint.

 Copyright (c) 1999-2000 SUPELEC and The Regents of the University of
 California.  All rights reserved.  Permission is hereby granted,
 without written agreement and without license or royalty fees, to
 use, copy, modify, and distribute this software and its documentation
 for any purpose, provided that the above copyright notice and the
 following two paragraphs appear in all copies of this software.

 IN NO EVENT SHALL SUPELEC OR THE UNIVERSITY OF CALIFORNIA BE LIABLE
 TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR
 CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 DOCUMENTATION, EVEN IF SUPELEC OR THE UNIVERSITY OF CALIFORNIA HAVE
 BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND SUPELEC SPECIFICALLY DISCLAIM ANY
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA AND SUPELEC HAVE NO OBLIGATION TO PROVIDE MAINTENANCE,
 SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (frederic.boulanger@supelec.fr)
@AcceptedRating Red
*/

package ptolemy.vergil.debugger;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.*;
import ptolemy.vergil.debugger.*;

//////////////////////////////////////////////////////////////////////////
//// DisplayList
/**
A selection window for editing breakpoints.

@author B. Desoutter, P. Domecq & G. Vibert and Steve Neuendorffer
@version $Id$
*/
public class DisplayList extends JFrame implements ActionListener {

    /**
     * Construct a new frame that lists the breakpoints for the given actor.
     * @param actor The actor with breakpoints.
     * @param frame The toplevel frame.
     * @param mode editing or deleting mode
     */
    public DisplayList(Actor actor, DebuggerFrame frame, int mode) {
	super("Select a breakpoint :");
	_actor = actor;
	_frame = frame;
	_list = (java.util.List)((NamedObj)actor).attributeList(ptolemy.vergil.debugger.Breakpoint.class);
	_mode = mode;
	JPanel panel = new JPanel();
	Box box = new Box(BoxLayout.Y_AXIS);

        group = new ButtonGroup();

	Iterator enum = _list.iterator();
	while (enum.hasNext()) {
	    Breakpoint element = (Breakpoint) enum.next();
	    JRadioButton radioButton = new JRadioButton(element.getName());
	    radioButton.setActionCommand(element.getName());
	    radioButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			String selection = e.getActionCommand();
			Iterator iter = _list.iterator();
			while (iter.hasNext()) {
			    Breakpoint brkpt = (Breakpoint)iter.next();
			    if (selection.equals(brkpt.getName())) {
				selectedItem = (Nameable)brkpt;
			    break;
			    }
			}
		    }
		});
	    group.add(radioButton);
	    box.add(radioButton);
	}

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BorderLayout());

	JButton button = new JButton("Ok");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			switch (_mode) {
			case EDIT_B :
			    BreakpointEditor editFrame =
				new BreakpointEditor((Breakpoint)selectedItem);
			    break;
			case EDIT_W :
			    break;
			case DEL :
			    try {
				Workspace w =
				    ((NamedObj) selectedItem).workspace();
				w.getWriteAccess();
				((Attribute) selectedItem).setContainer(null);
				w.doneWriting();
			    } catch (IllegalActionException ex) {
			    } catch (NameDuplicationException ex) {
			    }
			    break;
			default :
			    break;
			}
			DisplayList.this.dispose();
		}
	    });
	buttonPanel.add(button, BorderLayout.WEST);

	button = new JButton("Cancel");
	button.addActionListener(this);
	buttonPanel.add(button, BorderLayout.EAST);

	panel.setLayout(new BorderLayout());
	panel.add(box, BorderLayout.CENTER);
	panel.add(buttonPanel, BorderLayout.SOUTH);

	setContentPane(panel);
	pack();
	setLocation(500, 300);
	setVisible(true);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the window
     * @param e an action event
     */
    public void actionPerformed(ActionEvent e) {
		    DisplayList.this.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private java.util.List _list;
    private DebuggerFrame _frame;
    private Nameable selectedItem = null;
    private ButtonGroup group;
    private int _mode;
    private Actor _actor;
    static final int EDIT_B = 0;
    static final int EDIT_W = 1;
    static final int DEL = 2;
}
