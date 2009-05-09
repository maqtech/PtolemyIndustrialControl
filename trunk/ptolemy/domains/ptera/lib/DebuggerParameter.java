/* A parameter that represents a debugger for event debugging.

 Copyright (c) 2008-2009 The Regents of the University of California.
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

*/
package ptolemy.domains.ptera.lib;

import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.PteraController;
import ptolemy.domains.ptera.kernel.PteraDebugEvent;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DebuggerParameter

/**
 A parameter that represents a debugger for event debugging. It receives
 debugging messages from the events at the current level of the model hierarchy,
 and also the levels below if the {@link #hierarchical} parameter is set to
 true.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DebuggerParameter extends TableauParameter
        implements DebugListener {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public DebuggerParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setTypeEquals(BaseType.INT);
        rowsDisplayed.setExpression("10");

        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setTypeEquals(BaseType.INT);
        columnsDisplayed.setExpression("70");

        hierarchical = new Parameter(this, "hierarchical");
        hierarchical.setTypeEquals(BaseType.BOOLEAN);
        hierarchical.setExpression("true");
    }

    /** React to the given event.
     *  @param event The event.
     */
    public void event(DebugEvent event) {
        NamedObj container = getContainer();
        message(((PteraDebugEvent) event).toString(container));
    }

    /** Begin execution of the actor.  This is invoked exactly once
     *  after the preinitialization phase.  Since type resolution is done
     *  in the preinitialization phase, along with topology changes that
     *  may be requested by higher-order function actors, an actor
     *  can produce output data and schedule events in the initialize()
     *  method.
     *
     *  @exception IllegalActionException If execution is not permitted.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _registerDebugListener(true);
    }

    /** React to a debug message.
     *  @param message The debug message.
     */
    public void message(String message) {
        try {
            Tableau tableau = (Tableau) ((ObjectToken) getToken()).getValue();

            if (tableau == null) {
                tableau = _createTableau();
            }

            if (tableau != null) {
                if (!tableau.getFrame().isVisible()) {
                    tableau.getFrame().setVisible(true);
                }
                TextEditor frame = (TextEditor) tableau.getFrame();
                frame.text.append(message + "\n");
                try {
                    int lineOffset = frame.text.getLineStartOffset(frame.text
                            .getLineCount() - 1);
                    frame.text.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar doesn't move.
                }
            }
        } catch (Throwable e) {
            throw new InternalErrorException(this, e, "Unable to report " +
                    "message \"" + message + "\".");
        }
    }

    /** This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.  When this method is called,
     *  no further execution should occur.
     *
     *  @exception IllegalActionException If wrapup is not permitted.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _registerDebugListener(false);
    }

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** Whether debugging messages from lower levels of the model hierarchy
     *  should be displayed.
     */
    public Parameter hierarchical;

    /** The vertical size of the display, in rows. This contains an integer, and
     *  defaults to 10.
     */
    public Parameter rowsDisplayed;

    /** Create a tableau for displaying the debugging messages received from the
     *  events.
     *
     *  @return The tableau.
     *  @exception IllegalActionException If a text effigy cannot be created.
     */
    private Tableau _createTableau() throws IllegalActionException {
        Effigy effigy = EventUtils.findToplevelEffigy(this);
        TextEffigy textEffigy;
        try {
            textEffigy = TextEffigy.newTextEffigy(effigy, "");
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to create " +
                    "effigy.");
        }
        Tableau tableau;
        try {
            tableau = new Tableau(textEffigy, "tableau");
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unable to create " +
                    "tableau.");
        }
        TextEditor frame = new TextEditor(tableau.getTitle(),
                textEffigy.getDocument());
        frame.text.setColumns(((IntToken) columnsDisplayed.getToken())
                .intValue());
        frame.text.setRows(((IntToken) rowsDisplayed.getToken()).intValue());
        tableau.setFrame(frame);
        frame.setTableau(tableau);
        setToken(new ObjectToken(tableau, Tableau.class));
        frame.pack();
        frame.setVisible(true);
        return tableau;
    }

    /** Register or unregister this object as a debug listener for the events.
     *
     *  @param register Whether the operation is registering.
     *  @exception IllegalActionException If the refinements of an event cannot
     *   be obtained.
     */
    private void _registerDebugListener(boolean register)
            throws IllegalActionException {
        NamedObj container = getContainer();
        boolean hierarchical = ((BooleanToken) this.hierarchical.getToken())
                .booleanValue();
        if (container instanceof PteraController) {
            LinkedList<PteraController> controllers =
                new LinkedList<PteraController>();
            controllers.add((PteraController) container);
            while (!controllers.isEmpty()) {
                PteraController controller = controllers.removeFirst();
                for (Object entity : controller.deepEntityList()) {
                    if (entity instanceof Event) {
                        Event event = (Event) entity;
                        if (register) {
                            event.addDebugListener(this);
                        } else {
                            event.removeDebugListener(this);
                        }
                        if (!hierarchical) {
                            continue;
                        }
                        TypedActor[] refinements = event.getRefinement();
                        if (refinements != null) {
                            for (TypedActor refinement : refinements) {
                                if (refinement instanceof PteraController) {
                                    controllers.add(
                                            (PteraController) refinement);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
