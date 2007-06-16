/* A graph editor frame for ptolemy graph transformation models.
 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.vergil.gt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorEditorGraphController.NewRelationAction;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.WithIconGraphController.NewPortAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// GTRuleGraphFrame

/**
 A graph editor frame for ptolemy graph transformation models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.vergil.actor.ActorGraphFrame
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTRuleGraphFrame extends ActorGraphFrame
implements ChangeListener {

    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public GTRuleGraphFrame(SingleRuleTransformer entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public GTRuleGraphFrame(SingleRuleTransformer entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     *  @see #getCenter()
     */
    public void setCenter(Point2D center) {
        if (_graphs != null) {
            Rectangle2D visibleRect = getVisibleCanvasRectangle();
            AffineTransform newTransform = getJGraph().getCanvasPane()
                    .getTransformContext().getTransform();

            newTransform.translate(visibleRect.getCenterX() - center.getX(),
                    visibleRect.getCenterY() - center.getY());

            Iterator<JGraph> graphsIterator = _graphs.iterator();
            while (graphsIterator.hasNext()) {
                graphsIterator.next().getCanvasPane().setTransform(
                        newTransform);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source instanceof JTabbedPane) {
            Component selected = ((JTabbedPane) source).getSelectedComponent();
            if (selected instanceof JGraph) {
                setJGraph((JGraph) selected);
            }
            if (_graphPanner != null) {
                _graphPanner.setCanvas((JGraph) selected);
                _graphPanner.repaint();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// NewRelationAction
    /** An action to create a new relation. */
    public class GTNewRelationAction extends FigureAction {

        /** Create an action that creates a new relation.
         */
        public GTNewRelationAction() {
            super("New Relation");

            String[][] iconRoles = new String[][] {
                    { "/ptolemy/vergil/actor/img/relation.gif",
                        GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/relation_o.gif",
                        GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/relation_ov.gif",
                        GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/relation_on.gif",
                        GUIUtilities.SELECTED_ICON } };
            GUIUtilities.addIcons(this, iconRoles);

            putValue("tooltip", "Control-click to create a new relation");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer.valueOf(
                    KeyEvent.VK_R));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            JGraph graph = (JGraph) _tabbedPane.getSelectedComponent();
            Dimension size = graph.getSize();
            x = size.getWidth() / 2;
            y = size.getHeight() / 2;

            ActorGraphModel graphModel =
                (ActorGraphModel) _controller.getGraphModel();
            double[] point = SnapConstraint.constrainPoint(x, y);
            CompositeActorMatcher matcher =
                (CompositeActorMatcher) graphModel.getPtolemyModel();
            SingleRuleTransformer transformer =
                (SingleRuleTransformer) matcher.getContainer();
            List<?> entityList =
                transformer.entityList(CompositeActorMatcher.class);
            Iterator<?> iterator = entityList.iterator();
            for (int i = 0; i < _tabbedPane.getSelectedIndex(); i++) {
                iterator.next();
            }

            NamedObj namedObj = (NamedObj) iterator.next();

            final String relationName = namedObj.uniqueName("relation");
            final String vertexName = "vertex1";

            // Create the relation.
            StringBuffer moml = new StringBuffer();
            moml.append("<relation name=\"" + relationName + "\">\n");
            moml.append("<vertex name=\"" + vertexName + "\" value=\"{");
            moml.append(point[0] + ", " + point[1]);
            moml.append("}\"/>\n");
            moml.append("</relation>");

            MoMLChangeRequest request = new MoMLChangeRequest(this, namedObj,
                    moml.toString());
            request.setUndoable(true);
            namedObj.requestChange(request);
        }

        private static final long serialVersionUID = 2208151447002268749L;
    }

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();
        _ruleMenu = new JMenu("Rule");
        _ruleMenu.setMnemonic(KeyEvent.VK_R);
        _menubar.add(_ruleMenu);

        // Remove create new port actions in the tool bar.
        Component[] components = _toolbar.getComponents();
        for (int i = 0, del = 0; i < components.length; i++) {
            if (components[i] instanceof JButton) {
                Action action = ((JButton) components[i]).getAction();
                if (action != null && (action instanceof NewPortAction
                        || action instanceof NewRelationAction)) {
                    _toolbar.remove(i - del);
                    del++;
                }
            }
        }

        // Remove create new port actions in the menu.
        components = _graphMenu.getMenuComponents();
        for (int i = 0, del = 0; i < components.length; i++) {
            if (components[i] instanceof JMenuItem) {
                Action action = ((JMenuItem) components[i]).getAction();
                if (action != null && (action instanceof NewPortAction
                        || action instanceof NewRelationAction)) {
                    _graphMenu.remove(i - del);
                    del++;
                }
            }
        }

        // Remove duplicated menu separators.
        for (int i = _graphMenu.getMenuComponentCount() - 1; i >= 0; i--) {
            if (_graphMenu.getMenuComponent(i)
                    instanceof JPopupMenu.Separator) {
                if (i == 0 || _graphMenu.getMenuComponent(i - 1)
                    instanceof JPopupMenu.Separator) {
                    _graphMenu.remove(i);
                }
            }
        }

        GTNewRelationAction newRelationAction = new GTNewRelationAction();
        diva.gui.GUIUtilities.addMenuItem(_graphMenu, newRelationAction);
        diva.gui.GUIUtilities.addToolBarButton(_toolbar, newRelationAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Create the component that goes to the right of the library.
     *  NOTE: This is called in the base class constructor, before
     *  things have been initialized. Hence, it cannot reference
     *  local variables.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     */
    protected JComponent _createRightComponent(NamedObj entity) {
        if (!(entity instanceof SingleRuleTransformer)) {
            return super._createRightComponent(entity);
        }

        _graphs = new LinkedList<JGraph>();

        _tabbedPane = new JTabbedPane() {
            public void setMinimumSize(Dimension minimumSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setMinimumSize(minimumSize);
                }
            }

            public void setPreferredSize(Dimension preferredSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setPreferredSize(preferredSize);
                }
            }

            public void setSize(int width, int height) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setSize(width, height);
                }
            }

            /** Serial ID */
            private static final long serialVersionUID = -4998226270980176175L;
        };
        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((SingleRuleTransformer) entity).entityList(
                CompositeActorMatcher.class).iterator();
        boolean first = true;
        while (cases.hasNext()) {
            CompositeActorMatcher matcher = (CompositeActorMatcher) cases.next();
            JGraph jgraph = _addTabbedPane(matcher, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                setJGraph(jgraph);
            }
            _graphs.add(jgraph);
        }
        return _tabbedPane;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The case menu. */
    protected JMenu _ruleMenu;

    /** Add a tabbed pane for the specified case.
     *  @param refinement The case.
     *  @param newPane True to add the pane prior to the last pane.
     *  @return The pane.
     */
    private JGraph _addTabbedPane(CompositeActorMatcher matcher,
            boolean newPane) {
        GraphPane pane = _createGraphPane(matcher);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });
        JGraph jgraph = new JGraph(pane);
        String name = matcher.getName();
        jgraph.setName(name);
        int index = _tabbedPane.getComponentCount();
        // Put before the default pane, unless this is the default.
        if (newPane) {
            index--;
        }
        _tabbedPane.add(jgraph, index);
        jgraph.setBackground(BACKGROUND_COLOR);
        // Create a drop target for the jgraph.
        // FIXME: Should override _setDropIntoEnabled to modify all the drop
        //        targets created.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List<JGraph> _graphs;

    /** The tabbed pane for cases. */
    private JTabbedPane _tabbedPane;

    /** Serial ID */
    private static final long serialVersionUID = 5919681658644668772L;
}
