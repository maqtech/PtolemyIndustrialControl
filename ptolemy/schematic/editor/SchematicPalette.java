
package ptolemy.schematic.editor;

import ptolemy.schematic.util.*;
import java.io.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.*;
import javax.swing.*;
import diva.graph.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.interactor.*;
import diva.canvas.event.*;
import diva.gui.*;
import diva.gui.toolbox.*;


/**
 * A "palette" of figures that can be dragged onto another
 * JComponent.  
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class SchematicPalette extends JGraph {
    /**
     * Construct a new JCanvasPalette with a default empty graph
     */
    public SchematicPalette() {
        super();
	_controller = new PaletteController(this);
	GraphPane pane = new GraphPane(_controller, new SchematicGraphImpl());
	setGraphPane(pane);
	makeDraggable(this);

    }

    public void addNode(Node n, double x, double y) {
	_controller.addNode(n, x, y);
    }

    public Node getDraggedNode() {
	return _draggedNode;
    }
    
    /**
     * Make the given component draggable; the given data string will
     * be the "dragged" object that is associated with the component.
     * This data is made available via the NodeTransferable class.
     */
    public void makeDraggable(JComponent c) {
        final EditorDragGestureListener dgl = new EditorDragGestureListener();
	dgl.setPalette(this);

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                c, DnDConstants.ACTION_COPY_OR_MOVE,
		(DragGestureListener) dgl);
    }
    
    public void setDraggedNode(Node node) {
	_draggedNode = node;
    }

    /////////////////////////////////////////////////////////////
    //                    Inner Classes                        //

    public class PaletteController extends GraphController {
	public PaletteController (SchematicPalette palette) {
	    // The interactors attached to terminals and edges
	    SelectionModel sm = getSelectionModel();	    
	    NodeInteractor ni = new NodeInteractor(this, sm);
	    ni.setDragInteractor(new NodeDnDInteractor(this, palette));
	    setNodeInteractor(ni);
	    
	    NodeRenderer nr = new EditorNodeRenderer();
	    setNodeRenderer(nr);
	}

	/** Add a node to this graph editor and render it
	 * at the given location.
	 */
	public void addNode(Node n, double x, double y) {
	    // Create a figure for it
	    drawNode(n,x,y);
	    
	    // Add to the graph
	    getGraphImpl().addNode(n, getGraph());
	}
	
	/** Draw a node at the given location.
	 */
	public void drawNode(Node n, double x, double y) {
	    // Create a figure for it
	    Figure nf = getNodeRenderer().render(n);
	    nf.setInteractor(getNodeInteractor());
	    getGraphPane().getForegroundLayer().add(nf);
	    CanvasUtilities.translateTo(nf, x, y);
	    
	    // Add to the view and model
	    nf.setUserObject(n);
	    n.setVisualObject(nf);
	}
	
	/**
	 * Initialize all interaction on the graph pane. This method
	 * is called by the setGraphPane() method of the superclass.
	 * This initialization cannot be done in the constructor because
	 * the controller does not yet have a reference to its pane
	 * at that time.
	 */
	protected void initializeInteraction () {
	    GraphPane pane = getGraphPane();    
      	}
	

	public class NodeDnDInteractor extends DragInteractor {
	    /** The controller that this interactor is a part of
	     */
	    private PaletteController _controller;
	    
	    /** The Palette that this interactor is a part of.
	     */
	    private SchematicPalette _palette;
	    
	    /** Create a new NodeDragInteractor and give it a pointer
	     * to its controller to it can find other useful objects
	     */
	    public NodeDnDInteractor (GraphController c, 
				      SchematicPalette palette) {
		_controller = (PaletteController) c;
		_palette = palette;
	    }
	    
	    /** Drag all selected nodes and move any attached edges
	     */
	    public void translate (LayerEvent e, double x, double y) {
		Iterator i = targets();
		
		while (i.hasNext()) {
		    Figure t = (Figure) i.next();		    
		    _palette.setDraggedNode((Node)t.getUserObject());
		}
	    }
	}
    }

    public class NodeTransferable implements Transferable {
	public NodeTransferable(Node node) {
	    _node = node;
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
	    return _flavors;
	}
	
	public boolean isDataFlavorSupported( DataFlavor flavor ) {
	    int i;
	    for(i = 0; i < _flavors.length; i++) 
		if(_flavors[i].equals(flavor)) return true;
	    return false;
	}

	public Object getTransferData(DataFlavor flavor) 
	    throws UnsupportedFlavorException, IOException {
	    if (flavor.equals(DataFlavor.plainTextFlavor)) {
		return new ByteArrayInputStream(_node.toString().
						getBytes("Unicode"));
	    } else if(flavor.equals(SchematicPalette.nodeFlavor)) {
		return _node;
	    } else if(flavor.equals(DataFlavor.stringFlavor)) {
		return _node.toString();
	    }
	    throw new UnsupportedFlavorException(flavor);
	}
	
	public final DataFlavor[] _flavors = {
	    DataFlavor.plainTextFlavor,
	    DataFlavor.stringFlavor,
	    SchematicPalette.nodeFlavor,
	};
	private Node _node;
    }

    public class EditorDragGestureListener implements DragGestureListener
    {
	public void dragGestureRecognized(DragGestureEvent e) {
	    final DragSourceListener dsl = new DragSourceListener() {
		public void dragDropEnd(DragSourceDropEvent dsde) {}
		public void dragEnter(DragSourceDragEvent dsde) {
		    DragSourceContext context = dsde.getDragSourceContext();
		    //intersection of the users selected action, and the
		    //source and target actions
		    int myaction = dsde.getDropAction();
		    if( (myaction & DnDConstants.ACTION_COPY) != 0) { 
			context.setCursor(DragSource.DefaultCopyDrop); 
		    } else {
			context.setCursor(DragSource.DefaultCopyNoDrop); 
		    }
		}
		public void dragExit(DragSourceEvent dse) {}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}
	    };

	    try {
		if(_palette.getDraggedNode() == null) return;
		// check to see if action is OK ...
		NodeTransferable transferable = 
		    new NodeTransferable(_palette.getDraggedNode()); 

		//initial cursor, transferable, dsource listener 
		e.startDrag(DragSource.DefaultCopyNoDrop, 
				transferable, dsl);
	    } catch (InvalidDnDOperationException idoe) {
		System.err.println( idoe );
	    }
	}
	public void setPalette(SchematicPalette palette) {
	    _palette = palette;
	}
	public SchematicPalette getPalette() {
	    return _palette;
	}
	private SchematicPalette _palette;
    };
    

    ///////////////////////////////////////////////////////////////
    //                      Data Members                         //
    public static DataFlavor nodeFlavor = 
	new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + 
		       "ptolemy.diva.graph.Node", "divanode");

    private Node _draggedNode;
    private PaletteController _controller;

}


