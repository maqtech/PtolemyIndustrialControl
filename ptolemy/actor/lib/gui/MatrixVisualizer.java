/* FIXME

@Copyright (c) 1998-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.actor.lib.gui;

import java.awt.Container;
//import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.PlotFrame;
import ptolemy.plot.Render;

/** FIXME
A MatrixVisualizer.  This plotter contains an instance of the Render
class from the Ptolemy plot package as a public member. 

@author  Neil Turner
@version $Id$
 */
public class MatrixVisualizer extends TypedAtomicActor implements Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixVisualizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        // set the parameters
        xMax = new Parameter(this, "xMax", new DoubleToken(0.0));
        xMax.setTypeEquals(BaseType.DOUBLE);
        xMin = new Parameter(this, "xMin", new DoubleToken(0.0));
        xMin.setTypeEquals(BaseType.DOUBLE);
        yMax = new Parameter(this, "yMax", new DoubleToken(0.0));
        yMax.setTypeEquals(BaseType.DOUBLE);
        yMin = new Parameter(this, "yMin", new DoubleToken(0.0));
        yMin.setTypeEquals(BaseType.DOUBLE);

        // initialize the parameters
        attributeChanged(xMax);
        attributeChanged(xMin);
        attributeChanged(yMax);
        attributeChanged(yMin);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    /** The render object. */
    public transient Render render;

    /** The maximum of the x-axis. */
    public Parameter xMax;

    /** The minimum of the x-axis. */
    public Parameter xMin;

    /** The maximum of the y-axis. */
    public Parameter yMax;

    /** The minimum of the y-axis. */
    public Parameter yMin;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @exception IllegalActionException If the expression of the
     *  attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xMax) {
            _xMax = ((DoubleToken)xMax.getToken()).doubleValue();       
        } else if (attribute == xMin) {
            _xMin = ((DoubleToken)xMin.getToken()).doubleValue();
        } else if (attribute == yMax) {
            _yMax = ((DoubleToken)yMax.getToken()).doubleValue();
        } else if (attribute == yMin) {
            _yMin = ((DoubleToken)yMin.getToken()).doubleValue();
        } else { // This part I'm not sure about.  I carried it over from
                 // SequencePlotter.
            super.attributeChanged(attribute);
        }
    }


    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        MatrixVisualizer newobj = (MatrixVisualizer)super.clone(ws);
        newobj.input = (TypedIOPort)newobj.getPort("input");
        newobj.xMax = (Parameter)newobj.getAttribute("xMax");
        newobj.xMin = (Parameter)newobj.getAttribute("xMin");
        newobj.yMax = (Parameter)newobj.getAttribute("yMax");
        newobj.yMin = (Parameter)newobj.getAttribute("yMin");
        return newobj;
    }

//     /** Configure the object with data from the specified input source
//      *  (a URL) and/or textual data, assumed to be in PlotML format.
//      *  If this is called before the plotter has been created
//      *  (by calling place() or initialize()), then the configuration
//      *  is deferred until the plotter is created.
//      *  @param base The base relative to which references within the input
//      *   are found, or null if this is not known, or there is none.
//      *  @param source The input source, which specifies a URL.
//      *  @param text Configuration information given as text.
//      *  @exception Exception If the configuration source cannot be read
//      *   or if the configuration information is incorrect.
//      */
// //taken from Plotter.java
//     public void configure(URL base, String source, String text)
//             throws Exception {
//         if (plot != null) {
//             PlotMLParser parser = new PlotMLParser(plot);
//             if (source != null && !source.equals("")) {
//                 URL xmlFile = new URL(base, source);
//                 InputStream stream = xmlFile.openStream();
//                 parser.parse(base, stream);
//                 stream.close();
//             }
//             if (text != null && !text.equals("")) {
//                 // NOTE: Regrettably, the XML parser we are using cannot
//                 // deal with having a single processing instruction at the
//                 // outer level.  Thus, we have to strip it.
//                 String trimmed = text.trim();
//                 if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
//                     trimmed = trimmed.substring(2, trimmed.length() - 2).trim();
//                     if (trimmed.startsWith("plotml")) {
//                         trimmed = trimmed.substring(6).trim();
//                         parser.parse(base, trimmed);
//                     }
//                     // If it's not a plotml processing instruction, ignore.
//                 } else {
//                     // Data is not enclosed in a processing instruction.
//                     // Must have been given in a CDATA section.
//                     parser.parse(base, text);
//                 }
//             }
//         } else {
//             // Defer until plot has been placed.
//             if (_configureBases == null) {
//                 _configureBases = new LinkedList();
//                 _configureSources = new LinkedList();
//                 _configureTexts = new LinkedList();
//             }
//             _configureBases.add(base);
//             _configureSources.add(source);
//             _configureTexts.add(text);
//         }
//     }

    /** Reset the x axis counter, and call the base class.
     *  Also, clear the datasets that this actor will use.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (render == null) {
            place(_container);
        }
        if (_frame != null) {
	    _frame.setVisible(true);
        }
        render.clearData();
        render.samplePlot();
        render.repaint();
    }

    /** Specify the container into which this render should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the render will be placed in its own frame.
     *  The render is also placed in its own frame if this method
     *  is called with a null argument.  The size of the render,
     *  unfortunately, cannot be effectively determined from the size
     *  of the container because the container may not yet be laid out
     *  (its size will be zero).  Thus, you will have to explicitly
     *  set the size of the render by calling render.setSize().
     *  The background of the plot is set equal to that of the container
     *  (unless it is null).
     *  <p>
     *  If configure() has been called (prior to the plot getting created),
     *  then the configurations that it specified have been deferred. Those
     *  configurations are performed at this time.
     *
     *  @param container The container into which to place the plot.
     */
     public void place(Container container) {
         _container = container;
         if (_container == null) {
             // place the render in its own frame.
             render = new Render();
             _frame = new PlotFrame(getFullName(), render);
             _frame.setVisible(true);
         } else if (_container instanceof Render) {
             render = (Render)_container;
         } else {
             if (render == null) {
                 render = new Render();
                 render.setButtons(true);
             }
             _container.add(render);
             render.setBackground(_container.getBackground());
         }
     
 // If configurations have been deferred, implement them now.
//         if (_configureSources != null) {
//             Iterator sources = _configureSources.iterator();
//             Iterator texts = _configureTexts.iterator();
//             Iterator bases = _configureBases.iterator();
//             while(sources.hasNext()) {
//                 URL base = (URL)bases.next();
//                 String source = (String)sources.next();
//                 String text = (String)texts.next();
//                 try {
//                     configure(base, source, text);
//                 } catch (Exception ex) {
//                     getManager().notifyListenersOfException(ex);
//                 }
//             }
//             _configureSources = null;
//             _configureTexts = null;
//             _configureBases = null;
//        }
    }


    /** FIXME
     *
     *  Read at most one token from each input channel and plot it as
     *  a function of the iteration number, scaled by <i>xUnit</i>.
     *  The first point is plotted at the horizontal position given by
     *  <i>xInit</i>. The increments on the position are given by
     *  <i>xUnit</i>. The input data are plotted in postfire() to
     *  ensure that the data have settled.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire(int matrix[][]) throws IllegalActionException {
        int stripeLength = matrix[0].length;
        int stripe[] = new int[stripeLength];

        // Clear the render object's image data.
        render.clearData();
        // Add the matrix stripe by stripe to the render object.
        for (int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < stripeLength; j++) {
                stripe[j] = matrix[i][j];
            }
            render.addStripe(stripe);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////


    /** Container into which this plot should be placed */
    protected Container _container;

    /** X axis counter. */
    protected double _xValue;

    /** The maximum of the x-axis. */
    protected double _xMax;

    /** The minimum of the x-axis. */
    protected double _xMin;

    /** The maximum of the y-axis. */
    protected double _yMax;

    /** The minimum of the y-axis. */
    protected double _yMin;


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The bases and input streams given to the configure() method.
    private List _configureBases = null;
    private List _configureSources = null;
    private List _configureTexts = null;

    /** Frame into which plot is placed, if any. */
    private transient PlotFrame _frame;




}
