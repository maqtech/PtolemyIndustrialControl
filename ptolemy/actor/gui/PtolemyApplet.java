/* A base class for Ptolemy applets.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.*;
import ptolemy.gui.BasicJApplet;
import ptolemy.kernel.util.*;

import java.awt.event.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.System;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JApplet;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplet
/**
This class provides a convenient way to make applets out of Ptolemy II
models.  It assumes that the model is defined as a Java class that
extends CompositeActor, with the classname given by the
<i>modelClass</i> applet parameter. If that model does not contain
a manager, then this class will create one for it.
<p>
This class offers a number of alternatives that control the visual
appearance of the applet. By default, the applet places on the screen
a set of control buttons that can be used to start, stop, pause, and
resume the model.  Below those buttons, it places the visual elements
of any actors in the model that implement the Placeable interface,
such as plotters or textual output.
<p>
The applet parameters are:
<ul>
<li>
<i>background</i>: The background color, typically given as a hex
number of the form "#<i>rrggbb</i>" where <i>rr</i> gives the red
component, <i>gg</i> gives the green component, and <i>bb</i> gives
the blue component.
<li>
<i>controls</i>:
This gives a comma-separated list
of any subset of the words "buttons", "topParameters", and
"directorParameters" (case insensitive), or the word "none".
If this parameter is not given, then it is equivalent to
giving "buttons", and only the control buttons mentioned above
will be displayed.  If the parameter is given, and its value is "none",
then no controls are placed on the screen.  If the word "topParameters"
is included in the comma-separated list, then controls for the
top-level parameters of the model are placed on the screen, below
the buttons.  If the word "directorParameters" is included,
then controls for the director parameters are also included.
<i>modelClass</i>: The fully qualified class name of a Java class
that extends CompositeActor.  This class defines the model.
<li>
<i>orientation</i>: This can have value "horizontal"
or "vertical" (case insensitive).  If it is "vertical", then the
controls are placed above the visual elements of the Placeable actors.
This is the default.  If it is "horizontal", then the controls
are placed to the left of the visual elements.
</ul>
<p>
To create a model in a different way, say without a <i>modelClass</i>
applet parameter, you may extend this class and override the
protected method _createModel().  If you wish to alter the way
that the model is represented on the screen, you can extend this
class an override the _createView() method.  The rendition in this class
is an instance of ModelPane.
<p>
This class provides a number of methods that might be useful even
if its init() or _createModel() methods are not appropriate for a
given applet.  Specifically, it provides a mechanism for reporting
errors and exceptions; and it provide an applet parameter for
controlling the background color.

@see ModelPane
@see Placeable
@author Edward A. Lee
@version $Id$
*/
public class PtolemyApplet extends BasicJApplet
        implements ExecutionListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cleanup after execution of the model.  This method is called
     *  by the browser or appletviewer to inform this applet that
     *  it should clean up.
     */
    public void destroy() {
        // Note: we used to call manager.terminate() here to get rid
        // of a lingering browser problem
        stop();
    }

    /** Report that an execution error occurred.  This is
     *  called by the manager.
     *  @param manager The manager in charge of the execution.
     *  @param ex The exception that triggered the error.
     */
    public void executionError(Manager manager, Exception ex) {
        report(ex);
    }

    /** Report that execution of the model has finished.  This is
     *  called by the manager.
     *  @param manager The manager in charge of the execution.
     */
    public void executionFinished(Manager manager) {
        report("execution finished.");
    }

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    public String getAppletInfo() {
        return "Ptolemy applet for Ptolemy II "
	    + PtolemyApplication.RELEASE_VERSION
            + "\nPtolemy II comes from UC Berkeley, Department of EECS.\n"
            + "See http://ptolemy.eecs.berkeley.edu/ptolemyII"
	    + "\n(Build: $Id$)";
    }

    /** Initialize the applet. This method is called by the browser
     *  or applet viewer to inform this applet that it has been
     *  loaded into the system. It is always called before
     *  the first time that the start() method is called.
     *  In this base class, this method creates a new workspace,
     *  and instantiates in it the model whose class name is given
     *  by the <i>modelClass</i> applet parameter.  If that model
     *  does not contain a manager, then this method creates one for it.
     */
    public void init() {
        super.init();
        _setupOK = true;
        _workspace = new Workspace(getClass().getName());
        try {
            _toplevel = _createModel();
        } catch (Exception ex) {
            _setupOK = false;
            report("Creation of model failed:\n", ex);
        }
        _createView();
    }

    /** Report that the manager state has changed.  This is
     *  called by the manager.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newState = manager.getState();
        if (newState != _previousState) {
            report(manager.getState().getDescription());
            _previousState = newState;
        }
    }

    /** Start execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  start its execution. It is called after the init method
     *  and each time the applet is revisited in a Web page.
     *  In this base class, this method calls the protected method
     *  _go(), which executes the model.  If a derived class does not
     *  wish to execute the model each time start() is called, it should
     *  override this method with a blank method.
     */
    public void start() {
        // If an exception occurred during init, do not execute.
        if (!_setupOK) return;
        try {
            _go();
        } catch (Exception ex) {
            report(ex);
        }
    }

    /** Stop execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  stop its execution. It is called when the Web page
     *  that contains this applet has been replaced by another page,
     *  and also just before the applet is to be destroyed.
     *  In this base class, this method calls the finish() method
     *  of the manager. If there is no manager, do nothing.
     */
    public void stop() {
        if(_manager != null && _setupOK) {
            _manager.finish();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a model.  In this base class, we check to see whether
     *  the applet has a parameter <i>modelClass</i>, and if so, then we
     *  instantiate the class specified in that parameter.  If not,
     *  then we create an empty instance of CompositeActor.
     *  It is required that the class specified in the modelClass
     *  parameter have a constructor that takes one argument, an instance
     *  of Workspace.
     *  In either case, if the resulting model does not have a manager,
     *  then we give it a manager.
     *  @throws Exception If something goes wrong.
     */
    protected CompositeActor _createModel() throws Exception {
        CompositeActor result = null;
        // Look for modelClass applet parameter.
        String modelSpecification = getParameter("modelClass");
        if (modelSpecification != null) {
            Object[] arguments = new Object[1];
            arguments[0] = _workspace;
            Class modelClass = Class.forName(modelSpecification);
            Constructor[] constructors = modelClass.getConstructors();
            boolean foundConstructor = false;
            for (int i = 0; i < constructors.length; i++) {
                Constructor constructor = constructors[i];
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != arguments.length) continue;
                boolean match = true;
                for (int j = 0; j < parameterTypes.length; j++) {
                    if (!(parameterTypes[j].isInstance(arguments[j]))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    result = (CompositeActor)constructor
                             .newInstance(arguments);
                    foundConstructor = true;
                }
            }
            if(!foundConstructor) {
                throw new IllegalActionException(
                        "Cannot find a suitable constructor for "
                        + modelSpecification);
            }            
        }
        // If result is still null, then there was no modelClass given.
        if (result == null) {
            throw new Exception("Applet does not specify a modelClass.");
        }
        if (result.getManager() == null) {
            _manager = new Manager(_workspace, "manager");
            _manager.addExecutionListener(this);
            result.setManager(_manager);
        }
        return result;
    }

    /** Create run controls in a panel and return that panel.
     *  The argument controls how many buttons are
     *  created.  If its value is greater than zero, then a "Go" button
     *  created.  If its value is greater than one, then a "Stop" button
     *  is also created.  Derived classes may override this method to add
     *  additional controls, or to create a panel with a different layout.
     *  @param numberOfButtons How many buttons to create.
     *  @deprecated Use the <i>control</i> applet parameter.
     */
    protected JPanel _createRunControls(int numberOfButtons) {
        JPanel panel = new JPanel();
        if (numberOfButtons > 0) {
            _goButton = new JButton("Go");
            panel.add(_goButton);
            _goButton.addActionListener(new GoButtonListener());
        }
        if (numberOfButtons > 1) {
            _stopButton = new JButton("Stop");
            panel.add(_stopButton);
            _stopButton.addActionListener(new StopButtonListener());
        }
        return panel;
    }

    /** Create a ModelPane to control execution of the model and display
     *  its results.  Derived classes may override this to do something
     *  different.
     */
    protected void _createView() {

        // Parse applet parameters that determine visual appearance.

        // Start with orientation.
        String orientationSpec = getParameter("orientation");
        // Default is vertical
        int orientation = ModelPane.VERTICAL;
        if (orientationSpec != null) {
            if (orientationSpec.trim().toLowerCase().equals("horizontal")) {
                orientation = ModelPane.HORIZONTAL;
            }
        }

        // Next do controls.
        String controlsSpec = getParameter("controls");
        // Default has only the buttons.
        int controls = ModelPane.BUTTONS;
        if (controlsSpec != null) {
            // If controls are given, then buttons need to be explicit.
            controls = 0;
            StringTokenizer tokenizer = new StringTokenizer(controlsSpec, ",");
            while (tokenizer.hasMoreTokens()) {
                String controlSpec = tokenizer.nextToken().trim().toLowerCase();
                if (controlSpec.equals("buttons")) {
                    controls = controls | ModelPane.BUTTONS;
                } else if (controlSpec.equals("topparameters")) {
                    controls = controls | ModelPane.TOP_PARAMETERS;
                } else if (controlSpec.equals("directorparameters")) {
                    controls = controls | ModelPane.DIRECTOR_PARAMETERS;
                } else if (controlSpec.equals("none")) {
                    controls = 0;
                } else {
                    report("Warning: unrecognized controls: " + controlSpec);
                }
            }
        }

        ModelPane pane = new ModelPane(_toplevel, orientation, controls);
        pane.setBackground(null);
        getContentPane().add(pane);
    }

    /** Execute the model, if the manager is not currently executing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
        // If an exception occurred during init, do not execute.
        if (!_setupOK) return;
        // Only try to start if there is no execution currently running.
        if(_manager.getState() == _manager.IDLE)
            _manager.startRun();

    }

    /** Stop the execution.
     */
    protected void _stop() {
        // If an exception occurred during init, do not finish.
        if (!_setupOK) return;
	_manager.finish();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The manager, created in the init() method. */
    protected Manager _manager;

    /** Set this to false if the setup of the model during the init()
     *  method fails.  This prevents the model from executing.
     */
    protected boolean _setupOK = true;

    /** The top-level composite actor, created in the init() method. */
    protected CompositeActor _toplevel;

    /** The workspace that the applet is built in. Each applet has
     *  it own workspace.
     */
    protected Workspace _workspace;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private JButton _goButton;
    private JButton _stopButton;
    private Manager.State _previousState;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _go();
            } catch (Exception ex) {
                report(ex);
            }
        }
    }

    private class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _stop();
        }
    }
}
