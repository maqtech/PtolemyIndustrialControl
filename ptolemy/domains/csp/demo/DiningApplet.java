/* An applet containing CSP Dining Philosophers demo.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.csp.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.csp.kernel.CSPDirector;
import ptolemy.domains.csp.lib.*;
import ptolemy.actor.*;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import java.text.NumberFormat;

///////////////////////////////////////////////////////////////
//// DiningApplet
/**
   Applet containing Dining Philosophers demo. This demo uses the
   both the time and conditional communication constructs of the CSP
   domain in Ptolemy II. It represents the classic concurrency problem,
   first described by Dijkstra in 1965, which has 5 philosophers sitting
   around a table with one chopstick between each pair of philosophers. To
   eat a philosopher must have both chopsticks beside it. Each philosopher
   thinks for a while, then grabs one chopstick, then the other, eats for
   a while and puts the chopsticks down. This cycle continues.
   <p>
   @author Neil Smyth
   @version $Id$
*/

public class DiningApplet extends Applet
    implements Runnable, PhilosopherListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {

    // Process the background parameter.
    Color background = Color.white;
    try {
        String colorspec = getParameter("background");
        if (colorspec != null) {
            background = Color.decode(colorspec);
        }
    } catch (Exception ex) {}
    setBackground(background);


    try {
        univ = new CompositeActor();
        univ.setName("Top");

        // Set up the directors
        _localDirector = new CSPDirector("CSP Director");
        univ.setDirector(_localDirector);
        _manager = new Manager("Manager");
        univ.setManager(_manager);

        Parameter eatingRate = new Parameter(univ, "eatingRate");
        eatingRate.setExpression("1.0");
        eatingRate.evaluate();

        Parameter thinkingRate = new Parameter(univ, "thinkingRate");
        thinkingRate.setExpression("1.0");
        thinkingRate.evaluate();

        // Set up the actors and connections
        CSPPhilosopher p1 = new CSPPhilosopher(univ, "Aristotle");
        CSPPhilosopher p2 = new CSPPhilosopher(univ, "Plato");
        CSPPhilosopher p3 = new CSPPhilosopher(univ, "Sartre");
        CSPPhilosopher p4 = new CSPPhilosopher(univ, "DesCatres");
        CSPPhilosopher p5 = new CSPPhilosopher(univ, "Socrates");

        _philosophers[0] = p1;
        _philosophers[1] = p2;
        _philosophers[2] = p3;
        _philosophers[3] = p4;
        _philosophers[4] = p5;

        Parameter p = null;
        for (int i = 0; i< 5; i++) {
            _philosophers[i].addPhilosopherListener(this);
            p = (Parameter)_philosophers[i].getAttribute("eatingRate");
            if (p != null) {
                p.setExpression("eatingRate");
            }
            p = (Parameter)_philosophers[i].getAttribute("thinkingRate");
            if (p != null) {
                p.setExpression("thinkingRate");
            }
        }

        CSPChopstick f1 = new CSPChopstick(univ, "Chopstick1");
        CSPChopstick f2 = new CSPChopstick(univ, "Chopstick2");
        CSPChopstick f3 = new CSPChopstick(univ, "Chopstick3");
        CSPChopstick f4 = new CSPChopstick(univ, "Chopstick4");
        CSPChopstick f5 = new CSPChopstick(univ, "Chopstick5");

        // Now connect up the Actors
        IORelation r1 =
            (IORelation)univ.connect((IOPort)p1.getPort("leftIn"),
                    (IOPort)f5.getPort("rightOut"));
        IORelation r2 =
            (IORelation)univ.connect((IOPort)p1.getPort("leftOut"),
                    (IOPort)f5.getPort("rightIn"));
        IORelation r3 =
            (IORelation)univ.connect((IOPort)p1.getPort("rightIn"),
                    (IOPort)f1.getPort("leftOut"));
        IORelation r4 =
            (IORelation)univ.connect((IOPort)p1.getPort("rightOut"),
                    (IOPort)f1.getPort("leftIn"));

        IORelation r5 =
            (IORelation)univ.connect((IOPort)p2.getPort("leftIn"),
                    (IOPort)f1.getPort("rightOut"));
        IORelation r6 =
            (IORelation)univ.connect((IOPort)p2.getPort("leftOut"),
                    (IOPort)f1.getPort("rightIn"));
        IORelation r7 =
            (IORelation)univ.connect((IOPort)p2.getPort("rightIn"),
                    (IOPort)f2.getPort("leftOut"));
        IORelation r8 =
            (IORelation)univ.connect((IOPort)p2.getPort("rightOut"),
                    (IOPort)f2.getPort("leftIn"));

        IORelation r9  =
            (IORelation)univ.connect((IOPort)p3.getPort("leftIn"),
                    (IOPort)f2.getPort("rightOut"));
        IORelation r10 =
            (IORelation)univ.connect((IOPort)p3.getPort("leftOut"),
                    (IOPort)f2.getPort("rightIn"));
        IORelation r11 =
            (IORelation)univ.connect((IOPort)p3.getPort("rightIn"),
                    (IOPort)f3.getPort("leftOut"));
        IORelation r12 =
            (IORelation)univ.connect((IOPort)p3.getPort("rightOut"),
                    (IOPort)f3.getPort("leftIn"));

        IORelation r13 =
            (IORelation)univ.connect((IOPort)p4.getPort("leftIn"),
                    (IOPort)f3.getPort("rightOut"));
        IORelation r14 =
            (IORelation)univ.connect((IOPort)p4.getPort("leftOut"),
                    (IOPort)f3.getPort("rightIn"));
        IORelation r15 =
            (IORelation)univ.connect((IOPort)p4.getPort("rightIn"),
                    (IOPort)f4.getPort("leftOut"));
        IORelation r16 =
            (IORelation)univ.connect((IOPort)p4.getPort("rightOut"),
                    (IOPort)f4.getPort("leftIn"));

        IORelation r17 =
            (IORelation)univ.connect((IOPort)p5.getPort("leftIn"),
                    (IOPort)f4.getPort("rightOut"));
        IORelation r18 =
            (IORelation)univ.connect((IOPort)p5.getPort("leftOut"),
                    (IOPort)f4.getPort("rightIn"));
        IORelation r19 =
            (IORelation)univ.connect((IOPort)p5.getPort("rightIn"),
                    (IOPort)f5.getPort("leftOut"));
        IORelation r20 =
            (IORelation)univ.connect((IOPort)p5.getPort("rightOut"),
                    (IOPort)f5.getPort("leftIn"));

    } catch (Exception ex) {
        System.err.println("Setup failed: " + ex.getMessage());
        ex.printStackTrace();
    }

    // The applet has two panels, stacked vertically
    setLayout(new BorderLayout());

    _table = new TablePanel(_philosophers);
    add(_table, "Center");

    // Now create the panel that controls the applet
    Panel controlPanel = new Panel();
    add(controlPanel, "West");

    // Add a time display and go/stop buttons to control panel
    _currentTimeLabel = new Label("Current time = 0.0      ");
    _goButton = new Button("Go");
    _stopButton = new Button("Stop");
    controlPanel.add(_currentTimeLabel, "North");
    controlPanel.add(_goButton, "North");
    controlPanel.add(_stopButton, "North");

    // Add the listners for the go and stop buttons
    _goButton.addActionListener(new GoButtonListener());
    _stopButton.addActionListener(new StopButtonListener());

    // Add fields for editing the rate at which the
    // philosophers think and eat.
    _eatingRateBox = new TextField("1.0", 10);
    _thinkingRateBox = new TextField("1.0", 10);
    //controlPanel.add(_eatingRateBox, "South");
    //controlPanel.add(_thinkingRateBox, "South");

    // Add the listners for the go and stop buttons
    _eatingRateBox.addActionListener(new EatingRateListener());
    _thinkingRateBox.addActionListener(new ThinkingRateListener());
}

    public synchronized void philosopherChanged() {
        if (simulationThread.isAlive()) {
            // repaint the table for the current state
            _table.repaint();
            /*try {
              Thread.currentThread().sleep(100);
              } catch (InterruptedException e) {}*/
        }
    }

    public void run() {
        System.out.println("DiningApplet.run()");
        try {
            // Start the CurrentTimeThread.
            ctt = new CurrentTimeThread();
            ctt.start();

            // Start the simulation.
            _manager.run();
        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // The thread that runs the simulation.
    Thread simulationThread;

    // The thread that updates the time display
    Thread ctt;

    // The buttons and text fields on the control panel.
    public Label _currentTimeLabel;
    public Button _goButton;
    public Button _stopButton;
    public TextField _eatingRateBox;
    public TextField _thinkingRateBox;

    // the panel containing the animation for the applet.
    public TablePanel _table;

    public CSPPhilosopher[] _philosophers = new CSPPhilosopher[5];

    public CompositeActor univ;
    public CSPDirector _localDirector;
    public Manager _manager;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    // Show simulation progress.
    // FIXME: due to an applet bug, these inner classes are public.
    public class CurrentTimeThread extends Thread {
        NumberFormat nf = NumberFormat.getNumberInstance();
        public void run() {
            while ((simulationThread != null) || simulationThread.isAlive()) {
                // get the current time from director.
                double currenttime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = " +
                        nf.format(currenttime));
                try {
                    sleep(100);
                } catch (InterruptedException e) {}
            }
        }
    }

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(DiningApplet.this);
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(DiningApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _manager.terminate();
                simulationThread = null;
                init();
                repaint();
            } catch (Exception ex) {
                System.err.println("Run failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public class EatingRateListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Parameter p = (Parameter)univ.getAttribute("eatingRate");
            if ( p!= null) {
                String timespec = _eatingRateBox.getText();
                double spec = 1.0;
                try {
                    spec = (Double.valueOf(timespec)).doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid eating rate: " +
                            ex.getMessage() + ", defaulting to 1.0");
                }
                p.setToken(new DoubleToken(spec));
            }
        }
    }

    public class ThinkingRateListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Parameter p = (Parameter)univ.getAttribute("thinkingRate");
            if ( p!= null) {
                String timespec = _thinkingRateBox.getText();
                double spec = 1.0;
                try {
                    spec = (Double.valueOf(timespec)).doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid thinking rate: " +
                            ex.getMessage() + ", defaulting to 1.0");
                }
                p.setToken(new DoubleToken(spec));
            }
        }
    }


}
