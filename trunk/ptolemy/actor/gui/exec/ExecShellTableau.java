/* A tableau for evaluating Exec expression interactively.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.exec;

import tcl.lang.*;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ExecShellTableau
/**
A tableau that provides a Exec Shell for interacting with Ptjacl,
a 100% Java implementation of Exec

@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class ExecShellTableau extends Tableau
        implements ShellInterpreter {

    /** Create a new tableau.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ExecShellTableau(ExecShellEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
	ExecShellFrame frame = new ExecShellFrame(this);
	setFrame(frame);

	// Here is a good place to do initialization
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception {
	try {
	    _interpreter.eval(command);
	    return _interpreter.getResult().toString();
	} catch (TclException ex) {
	    return _interpreter.getVar("errorInfo", null,0).toString();
	}
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True if the command is complete.
     */
    public boolean isCommandComplete(String command) {
	return _interpreter.commandComplete(command);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Exec interpreter
    // FIXME: Perhaps the interpreter should be in its own thread?
    private Interp _interpreter = new Interp();


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of ExecShellTableau.
     */
    public class ExecShellFrame extends TableauFrame {

	/** Construct a frame to display the ExecShell window.
	 *  After constructing this, it is necessary
	 *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
	 *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
	 */
	public ExecShellFrame(Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
	    super(tableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

	    _shellTextArea = new ShellTextArea();
            _shellTextArea.setInterpreter(ExecShellTableau.this);
	    _shellTextArea.mainPrompt = "% "; 
	    component.add(_shellTextArea);
            getContentPane().add(component, BorderLayout.CENTER);
	}

	///////////////////////////////////////////////////////////////////
	////                         protected methods                 ////

	protected void _help() {
	    try {
		URL doc = getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/ptjacl/help.htm");
		getConfiguration().openModel(null, doc, doc.toExternalForm());
	    } catch (Exception ex) {
		System.out.println("ExecShellTableau._help(): " + ex);
		_about();
	    }
	}
	public ShellTextArea _shellTextArea;
    }

    /** A factory that creates a control panel to display a Exec Shell
     */
    public static class Factory extends TableauFactory {

	/** Create a factory with the given name and container.
	 *  @param container The container.
	 *  @param name The name.
	 *  @exception IllegalActionException If the container is incompatible
	 *   with this attribute.
	 *  @exception NameDuplicationException If the name coincides with
	 *   an attribute already in the container.
	 */
	public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	}

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

	/** Create a new instance of ExecShellTableau in the specified
         *  effigy. It is the responsibility of callers of
         *  this method to check the return value and call show().
	 *  @param effigy The model effigy.
	 *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
            // NOTE: Can create any number of tableaux within the same
            // effigy.  Is this what we want?
            if (effigy instanceof ExecShellEffigy) {
                return new ExecShellTableau(
                        (ExecShellEffigy)effigy,
                        "ExecShellTableau");
	    } else {
		return null;
	    }
	}
    }
}
