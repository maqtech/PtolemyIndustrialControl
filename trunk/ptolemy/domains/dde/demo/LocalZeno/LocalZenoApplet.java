/* A DDE application illustrating localized Zeno conditions.

 Copyright (c) 1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.demo.LocalZeno;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.model.Node;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.canvas.connector.*;
import diva.util.gui.BasicWindow;

import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.dde.lib.*;
import ptolemy.domains.dde.gui.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.dde.kernel.test.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// LocalZenoApplet

/** 
 *  A DDE application illustrating localized Zeno conditions.
 *
 *  @author John S. Davis II (davisj@eecs.berkeley.edu)
 *  @author Michael Shilman  (michaels@eecs.berkeley.edu)
 *  @version $Id$
 */
public class LocalZenoApplet extends DDEApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
	super.init();

	setLayout( new BorderLayout(5, 5) );

	// Panel for controls and plotter
	Panel topPanel = new Panel();
	topPanel.setSize( new Dimension(600, 300) );

	// The '2' argument specifies a 'go' and 'stop' button.
	topPanel.add( _createRunControls(2), BorderLayout.NORTH );

	_plotPanel = new Panel();
	_plotPanel.setSize( new Dimension(600, 200) );
	topPanel.add( _plotPanel, BorderLayout.CENTER );

	add( topPanel, BorderLayout.NORTH );

	constructPtolemyModel();

	_divaPanel = new JPanel( new BorderLayout() );
	_divaPanel.setSize( new Dimension(600, 300) );
	_divaPanel.setBackground( getBackground() );
	add( _divaPanel, BorderLayout.CENTER );

        _model = constructDivaGraph();
	final GraphModel finalModel = _model;

        try {
	    SwingUtilities.invokeAndWait(new Runnable (){
		public void run () {
		    displayGraph(_jgraph, finalModel);
		}
	    });
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

        StateListener listener = 
	        new StateListener((GraphPane)_jgraph.getCanvasPane());
	_join1.addListeners(listener);
	_join2.addListeners(listener);
	_fork1.addListeners(listener);
	_fork2.addListeners(listener);
	_fBack1.addListeners(listener);
	_fBack2.addListeners(listener);
	_rcvr1.addListeners(listener);
	_rcvr2.addListeners(listener);
	_clock.addListeners(listener);
    }

    /** Construct the graph representing the topology.
     *  This is sort of bogus because it's totally hird-wired,
     *  but it will do for now...
     */
    public GraphModel constructDivaGraph () {
        GraphModel model = new GraphModel();

        // Nodes, with user object set to the actor
        Node n1 = model.createNode(_clock);

        Node n2 = model.createNode(_join1);
        Node n3 = model.createNode(_fork1);
        Node n4 = model.createNode(_fBack1);
        Node n5 = model.createNode(_rcvr1);

        Node n6 = model.createNode(_join2);
        Node n7 = model.createNode(_fork2);
        Node n8 = model.createNode(_fBack2);
        Node n9 = model.createNode(_rcvr2);

        model.addNode(n1);
        model.addNode(n2);
        model.addNode(n3);
        model.addNode(n4);
        model.addNode(n5);
        model.addNode(n6);
        model.addNode(n7);
        model.addNode(n8);
        model.addNode(n9);

        _nodeMap.put(_clock, n1);

        _nodeMap.put(_join1, n2);
        _nodeMap.put(_fork1, n3);
        _nodeMap.put(_fBack1, n4);
        _nodeMap.put(_rcvr1, n5);

        _nodeMap.put(_join2, n6);
        _nodeMap.put(_fork2, n7);
        _nodeMap.put(_fBack2, n8);
        _nodeMap.put(_rcvr2, n9);

        // Edges
        model.createEdge(n1,n2);
        model.createEdge(n2,n3);
        model.createEdge(n3,n4);
        model.createEdge(n3,n5);
        model.createEdge(n4,n2);

        model.createEdge(n1,n6);
        model.createEdge(n6,n7);
        model.createEdge(n7,n8);
        model.createEdge(n7,n9);
        model.createEdge(n8,n6);
 
        return model;
    }

    /** Construct the Ptolemy model; instantiate all
     *  actors and make connections.
     */
    public void constructPtolemyModel () {
        try {
            // Instantiate the Actors 
	    _clock = new ListenClock( _toplevel, "Clock" );
	    _clock.values.setExpression( "[1, 1, 1]" );
	    _clock.period.setToken( new DoubleToken(20.0) );
	    _clock.offsets.setExpression( "[5.0, 10.0, 15.0]" );
	    _clock.stopTime.setToken( new DoubleToken(90.0) );

	    _join1 = new ListenWire( _toplevel, "UpperJoin" );
	    _fork1 = new ListenFork( _toplevel, "UpperFork" );
	    _fBack1 = new ListenFBDelay( _toplevel, "UpperFeedBack" );
	    _join2 = new ListenWire( _toplevel, "LowerJoin" );
	    _fork2 = new ListenFork( _toplevel, "LowerFork" );
	    _fBack2 = new ZenoDelay( _toplevel, "LowerFeedBack" );
            
	    _rcvr1 = new ListenSink( _toplevel, "UpperRcvr" );
	    _rcvr2 = new ListenSink( _toplevel, "LowerRcvr" );

	    _upperTime = new TimeAdvance( _toplevel, "upperTime" );
	    _upperPlotter = new TimedPlotter( _toplevel, "upperPlotter" );
	    _upperPlotter.setPanel( _plotPanel );
	    _upperPlotter.plot.setTitle("Upper Branch");
	    _upperPlotter.plot.setXRange(0.0, 90.0);
	    _upperPlotter.plot.setYRange(-1.0, 1.0);
	    _upperPlotter.plot.setSize(200, 150);
	    _upperPlotter.plot.addLegend(0, "Time");
            
	    _lowerTime = new TimeAdvance( _toplevel, "lowerTime" );
	    _lowerPlotter = new TimedPlotter( _toplevel, "lowerPlotter" );
	    _lowerPlotter.setPanel( _plotPanel );
	    _lowerPlotter.plot.setTitle("Lower Branch");
	    _lowerPlotter.plot.setXRange(0.0, 90.0);
	    _lowerPlotter.plot.setYRange(-1.0, 1.0);
	    _lowerPlotter.plot.setSize(200, 150);
	    _lowerPlotter.plot.addLegend(0, "Time");
            
	    _fBack1.setDelay(4.5);
	    _fBack2.setDelay(4.5);

	    // Set up ports, relation 
	    TypedIOPort clockOut = (TypedIOPort)_clock.getPort("output"); 
	    clockOut.setMultiport(true);

	    // Set up connections 
	    _toplevel.connect( _clock.output, _join1.input );
	    _toplevel.connect( _clock.output, _join2.input );

	    _toplevel.connect( _join1.output, _fork1.input );
	    _toplevel.connect( _fork1.output1, _rcvr1.input );
	    _toplevel.connect( _fork1.output2, _fBack1.input );
	    _toplevel.connect( _fBack1.output, _join1.input );

	    _toplevel.connect( _join2.output, _fork2.input );
	    _toplevel.connect( _fork2.output1, _rcvr2.input );
	    _toplevel.connect( _fork2.output2, _fBack2.input );
	    _toplevel.connect( _fBack2.output, _join2.input );

	    _toplevel.connect( _fork1.output1, _upperTime.input );
	    _toplevel.connect( _upperTime.output, _upperPlotter.input );

	    _toplevel.connect( _fork2.output1, _lowerTime.input );
	    _toplevel.connect( _lowerTime.output, _lowerPlotter.input );

            System.out.println("Connections are complete.");
	    
        } catch (Exception e) {
	    report("Setup failed:", e);
        }
    }

    /** Construct the graph widget with the default constructor 
     *  (giving it an empty graph), and then set the model once 
     *  the _window is showing. Add control buttons to the _window.
     */
    public void displayGraph(JGraph g, GraphModel model) {
	_divaPanel.add( g, BorderLayout.NORTH );
	g.setPreferredSize( new Dimension(600,300) );

        // Make sure we have the right renderers and then 
	// display the graph
        final GraphPane gp = (GraphPane) g.getCanvasPane();
        final GraphView gv = gp.getGraphView();
        gv.setNodeRenderer(new ThreadRenderer());
        gv.setEdgeRenderer(new LocalEdgeRenderer());
	gv.setLayoutPercentage(0.6);
        g.setGraphModel(model);

        // Do the layout
        gp.repaint();
	final GraphModel m = model;
	try {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    LevelLayout staticLayout = new LevelLayout();
		    staticLayout.setOrientation(LevelLayout.HORIZONTAL);
		    staticLayout.layout(gv, m.getGraph());
		    gp.repaint();
		}
	    });
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    /** Override the baseclass start method so that the model
     *  does not immediately begin executing as soon as the
     *  the applet page is displayed. Execution begins once
     *  the "Go" button is depressed.
     */
    public void start() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The Actors
    private ListenClock _clock;
    private ListenWire _join1;
    private ListenFork _fork1;
    private ListenFBDelay _fBack1;
    private ListenSink _rcvr1;
    private ListenWire _join2;
    private ListenFork _fork2;
    private ZenoDelay _fBack2;
    private ListenSink _rcvr2;
    private TimeAdvance _upperTime;
    private TimeAdvance _lowerTime;
    private TimedPlotter _upperPlotter;
    private TimedPlotter _lowerPlotter;

    // Plot Panel
    private Panel _plotPanel;

    // The mapping from Ptolemy actors to Diva graph nodes
    private HashMap _nodeMap = new HashMap();

    // The Diva JGraph where we display stuff
    private JGraph _jgraph = new JGraph();

    // The Diva panel where we display stuff
    private JPanel _divaPanel;

    // The Diva graph model
    private GraphModel _model;

    // Flag to prevent spurious exceptions being thrown during _go().
    private boolean _initCompleted = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    //////////////////////////////////
    //// StateListener
    /**
     * StateListener is an inner class that listens to state
     * events on the Ptolemy kernel and changes the color of
     * the nodes appropriately.
     */
    public class StateListener implements ExecEventListener {

        // The Pane
        GraphPane _graphPane;

        /** Create a listener on the given graph pane
         */
        public StateListener (GraphPane pane) {
            _graphPane = pane;
        }

        /** Respond to a state changed event.
         */
        public void stateChanged(ExecEvent event) {
            final int state = event.getCurrentState();
            Actor actor = event.getActor();
            
            String name = ((Nameable)actor).getName();
            System.out.println(name+":  Event generated");

            // Get the corresponding graph node and its figure
            Node node = (Node) _nodeMap.get(actor);
            LabelWrapper wrapper = (LabelWrapper)
                _graphPane.getGraphView().getNodeFigure(node);
            final BasicFigure figure = (BasicFigure)
                wrapper.getChild();

            // Color the graph
            try {
                SwingUtilities.invokeAndWait(new Runnable () {
                    public void run () {
                        switch (state) {
                        case 1:
			    figure.setFillPaint(Color.yellow);
                            break;
                        
                        case 2:
                            figure.setFillPaint(Color.green);
                            break;
                        
                        case 3:
                            figure.setFillPaint(Color.red);
                            break;

                        default:
                            System.out.println("Unknown state: " + state);
                        }
                    }
                });
            } 
            catch (Exception e) {}
        }
    }


    //////////////////////////////////
    //// ThreadRenderer

    /**
     * ThreadRenderer draws the nodes to represent running threads.
     */
    public class ThreadRenderer implements NodeRenderer {

        /** The rectangle size
         */
        private double _size = 40;

        /**
         * Return the rendered visual representation of this node.
         */
        public Figure render (Node n) {
            ComponentEntity actor = (ComponentEntity) n.getSemanticObject();

            boolean isEllipse = 
                   actor instanceof ListenWire
                || actor instanceof ListenFork
		|| actor instanceof ListenClock
		|| actor instanceof ListenSink
                || actor instanceof ListenFBDelay;

            
            BasicFigure f;
            if (isEllipse) {
                f = new BasicEllipse(0, 0, _size, _size);
		f.setFillPaint(Color.blue);
            } else {
                f = new BasicRectangle(0, 0, _size, _size); 
		f.setFillPaint(Color.pink);
            }
            String label = actor.getName();
            System.out.println("Actor " + actor + " has label " + label);
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }

    //////////////////////////////////
    //// LocalEdgeRenderer

    /**
     * LocalEdgeRenderer draws arrowheads on both ends of the connector
     */
    public class LocalEdgeRenderer implements EdgeRenderer {
      /**
       * Render the edge
       */
      public Connector render (Edge edge, Site tailSite, Site headSite) {
	StraightConnector c = new StraightConnector(tailSite, headSite);

	// Create an arrow at the head
	Arrowhead headArrow = new Arrowhead(
					    headSite.getX(), headSite.getY(),
					    headSite.getNormal());
	c.setHeadEnd(headArrow);
	c.setUserObject(edge);
	return c;
      }
    }
}
