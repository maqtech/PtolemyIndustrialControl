/* A top-level dialog window for displaying dependency results.

   Copyright (c) 2012 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.PtolemyDialog;
import ptolemy.actor.lib.Publisher;
import ptolemy.actor.lib.Subscriber;
import ptolemy.actor.util.ActorDependencies;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.gui.Top;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// DependencyResultsDialog

/**
   A non-modal dialog that displays the actor dependency analysis results as a
   list.

   @author Christopher Brooks, Based on SearchResultsDialog by Edward A. Lee
   @version $Id: DependencyResultsDialog.java 63152 2012-03-08 21:28:43Z derler $
   @since Ptolemy II 8.1
   @Pt.ProposedRating Yellow (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class DependencyResultsDialog extends PtolemyDialog 
    implements ListSelectionListener, QueryListener {

    /** Construct a dialog for search results.
     *  @param tableau The DialogTableau.
     *  @param owner The frame that, per the user, is generating the dialog.
     *  @param target The object on which the search is to be done.
     *  @param configuration The configuration to use to open the help screen
     *   (or null if help is not supported).
     */
    public DependencyResultsDialog(DialogTableau tableau, Frame owner,
            Entity target, Configuration configuration) {
        super("Dependency analysis for " + target.getName(), tableau, owner,
                target, configuration);
        
        _owner = owner;
        _target = target;
        
        _query = new Query();

        // FIXME: we could use the text field to specify the filter class.
        //_query.addLine("text", "Find", _previousSearchTerm);

        _query.setColumns(2);
        _query.addCheckBox("prerequisites", "Prerequisites", true);
        _query.addCheckBox("dependents", "Dependents", true);
        //_query.addCheckBox("recursive", "Recursive search", true);
        //_query.addCheckBox("case", "Case sensitive", false);
        getContentPane().add(_query, BorderLayout.NORTH);
        _query.addQueryListener(this);

        _resultsTableModel = new ResultsTableModel();
        _resultsTable = new JTable(_resultsTableModel);
        _resultsTable.setDefaultRenderer(NamedObj.class, new NamedObjRenderer());
        // If you change the height, then check that a few rows can be added.
        // Also, check the setRowHeight call below.
        _resultsTable.setPreferredScrollableViewportSize(new Dimension(300, 300));
        
        ListSelectionModel selectionModel = _resultsTable.getSelectionModel();
        selectionModel.addListSelectionListener(this);

        addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent ke) {
                    if (ke.getKeyChar() == '\n') {
                        _search();
                    }
                }
            });

        // Make the contents of the table scrollable and visible.
        JScrollPane scrollPane = new JScrollPane(_resultsTable);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        _resultsTable.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent event) {
                    int code = event.getKeyCode();
                    if (code == KeyEvent.VK_ENTER) {
                        _search();
                    } else if (code == KeyEvent.VK_ESCAPE) {
                        _cancel();
                    }
                }
            });

        _resultsTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO Auto-generated method stub
                    // super.mouseClicked(e);
                    int button = e.getButton();
                    int count = e.getClickCount();
                    if (button == MouseEvent.BUTTON1 && count == 2) {
                        int[] selected = _resultsTable.getSelectedRows();
                        for (int i = 0; i < selected.length; i++) {
                            NamedObj selectedObject = (NamedObj) _resultsTableModel.getValueAt(selected[i], 0);
                            _openResult(selectedObject);
                        }
                    }
                }
            });
        
        
        pack();
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the search. This is called to 
     *  notify this dialog that one of the search options has changed.
     *  @param name The name of the query field that changed.
     */
    public void changed(String name) {
        _search();
    }

    /** Override to clear highlights. */
    public void dispose() {
        _clearHighlights();
        super.dispose();
    }
    
    /** React to notice that the selection has changed.
     *  @param event The selection event.
     */
    public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            // Selection change is not finished. Ignore.
            return;
        }
        _clearHighlights();

        // Highlight new selection.
        int[] selected = _resultsTable.getSelectedRows();
        for (int i = 0; i < selected.length; i++) {
            NamedObj selectedObject = (NamedObj) _resultsTableModel.getValueAt(selected[i], 0);
            _highlightResult(selectedObject);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear all highlights.
     */
    protected void _clearHighlights() {
        // Clear previous highlights.
        ChangeRequest request = new ChangeRequest(this, "Error Dehighlighter") {
                protected void _execute() throws Exception {
                    for (Attribute highlight : _highlights) {
                        highlight.setContainer(null);
                    }
                }
            };
        request.setPersistent(false);
        _target.requestChange(request);
    }

    /** Highlight the specified object and all its containers to
     *  indicate that it matches the search criteria.
     *  @param target The target.
     */
    protected void _highlightResult(final NamedObj target) {
        if (target instanceof NamedObj) {
            ChangeRequest request = new ChangeRequest(this, "Error Highlighter") {
                    protected void _execute() throws Exception {
                        _addHighlightIfNeeded(target);
                        NamedObj container = target.getContainer();
                        while (container != null) {
                            _addHighlightIfNeeded(container);
                            container = container.getContainer();
                        }
                    }
                };
            request.setPersistent(false);
            ((NamedObj) target).requestChange(request);
        }
    }

    /** Opens the nearest composite actor above the target in the hierarchy.
     *  @param target The target.
     */
    protected void _openResult(final NamedObj target) {
        NamedObj container = target.getContainer();
        while (container != null && !(container instanceof CompositeActor)) {
            container = container.getContainer();
        }
        if (container != null) {
            try {
                _configuration.openInstance(container);
            } catch (Throwable throwable) {
                MessageHandler.error("Failed to open container", throwable);
            }
        }
    }
    
    /** Perform a search and update the results table.
     */
    protected void _search() {
//         String findText = _query.getStringValue("text");
//         if (findText.trim().equals("")) {
//             MessageHandler.message("Please enter a search term");
//             return;
//         }
//         _previousSearchTerm = findText;
        boolean prerequisites = _query.getBooleanValue("prerequisites");
        boolean dependents = _query.getBooleanValue("dependents");
        //boolean recursiveSearch = _query.getBooleanValue("recursive");
        //boolean caseSensitive = _query.getBooleanValue("case");
        try {
            Set<NamedObj>results = _find((Actor)_target, prerequisites, dependents);
            _resultsTableModel.setContents(results);
            if (results.size() == 0) {
                MessageHandler.message("No prerequisites and/or dependents.");
            }
        } catch (KernelException ex) {
            MessageHandler.error("Failed to get prequisites or dependents for "
                    + _target.getFullName() + ".",
                    ex);
        }
    }

    /** Create buttons.
     *  @param panel The panel into which to put the buttons.
     */
    protected void _createExtendedButtons(JPanel panel) {
        _searchButton = new JButton("Search");
        panel.add(_searchButton);
    }

    /** Return a list of objects in the model that match the
     *  specified search.
     *  @param actor The actor to be searched.
     *  @param prerequisites True to search for prerequisites.
     *  @param dependents True to search for dependents.
     *  @return The Set of objects in the model that match the specified search.
     *  @exception KernelException If thrown while preinitializing() or wrapping up.
     */
    protected Set<NamedObj> _find(
            Actor actor, boolean prerequisites, boolean dependents) throws KernelException {

        // FIXME: we could add a field to the search box for specifying the filter
        // class.  However, how would we specify that searching Publishers should
        // have Subscribers as the field?
        Class clazz = AtomicActor.class;
        SortedSet<NamedObj> result = new TreeSet<NamedObj>(new NamedObjComparator());
        if (prerequisites) {
            _report("Generating prerequisite information.");
            result.addAll(ActorDependencies.prerequisites(actor, clazz));
            _report("Done generating prequisite information.");
        }
        if (dependents) {
            _report("Generating dependency information.");
            result.addAll(ActorDependencies.dependents(actor, clazz));
            _report("Done generating dependency information.");
        }

        //         Iterator<NamedObj> objects = container.containedObjectsIterator();
        //         while (objects.hasNext()) {
        //             NamedObj object = objects.next();
        //             if (includeNames) {
        //                 String name = object.getName();
        //                 if (!caseSensitive) {
        //                     name = name.toLowerCase();
        //                 }
        //                 if (name.contains(text)) {
        //                     result.add(object);
        //                 }
        //             }
        //             if (includeValues && object instanceof Settable) {
        //                 Settable.Visibility visible = ((Settable)object).getVisibility();
        //                 if (!visible.equals(Settable.NONE) && !visible.equals(Settable.EXPERT)) { 
        //                     String value = ((Settable)object).getExpression();
        //                     if (!caseSensitive) {
        //                         value = value.toLowerCase();
        //                     }
        //                     if (value.contains(text)) {
        //                         result.add(object);
        //                     }
        //                 }
        //             }
        //             if (recursive) {
        //                 result.addAll(_find(object, text, includeValues, includeNames, recursive, caseSensitive));
        //             }
        //         }
        return result;
    }
    
    /** Return a URL that points to the help page.
     *  @return A URL that points to the help page
     */
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/DependencyResultsDialog.htm");
        return helpURL;
    }

    /** Process a button press.
     *  @param button The button.
     */
    protected void _processButtonPress(String button) {
        // If the user has typed in a port name, but not
        // moved the focus, we want to tell the model the
        // data has changed.
        if (_resultsTable.isEditing()) {
            _resultsTable.editingStopped(new ChangeEvent(button));
        }

        // The button semantics are
        // Add - Add a new port.
        if (button.equals("Search")) {
            _search();
        } else {
            super._processButtonPress(button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private method                    ////

    /** Add a highlight color to the specified target if it is
     *  not already present.
     *  @param target The target to highlight.
     *  @exception IllegalActionException If the highlight cannot be added.
     *  @exception NameDuplicationException Should not be thrown.
     */
    private void _addHighlightIfNeeded(NamedObj target)
            throws IllegalActionException, NameDuplicationException {
        Attribute highlightColor = target.getAttribute("_highlightColor");
        if (highlightColor instanceof ColorAttribute) {
            // There is already a highlight. Set its color.
            ((ColorAttribute) highlightColor)
                .setExpression(_HIGHLIGHT_COLOR);
            _highlights.add(highlightColor);
        } else if (highlightColor == null) {
            highlightColor = new ColorAttribute(target, "_highlightColor");
            ((ColorAttribute) highlightColor)
                .setExpression(_HIGHLIGHT_COLOR);
            highlightColor.setPersistent(false);
            ((ColorAttribute) highlightColor).setVisibility(Settable.EXPERT);
            _highlights.add(highlightColor);
        }
    }

    /** Report a message to either the status bar or message handler.
     *  @param message The message.   
     */   
    private void _report(String message) {
        if (_owner instanceof Top) {
            ((Top)_owner).report(message);
        } else {
            MessageHandler.message(message);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The color to use for the highlight. */
    private static String _HIGHLIGHT_COLOR = "{0.6, 0.6, 1.0, 1.0}";
    
    /** Highlights that have been created. */
    private Set<Attribute> _highlights = new HashSet<Attribute>();
    
    /** The The frame that, per the user, is generating the dialog.
     *  Typically a BasicGraphFrame.
     */
    private Frame _owner;

    /** Previous search term, if any. */
    //private String _previousSearchTerm = "";
    
    /** Table for search results. */
    private JTable _resultsTable;

    /** Model for the table. */
    private ResultsTableModel _resultsTableModel = null;

    /** The results of the latest search. */
    private NamedObj[] _results = null;
    
    /** The query portion of the dialog. */
    private Query _query;

    /** The Search button. */
    private JButton _searchButton;
    
    /** The entity on which search is performed. */
    private Entity _target;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Comparator for sorting named objects alphabetically by name. */
    class NamedObjComparator implements Comparator<NamedObj> {
        public int compare(NamedObj arg0, NamedObj arg1) {
            return (arg0.getFullName().compareTo(arg1.getFullName()));
        }
    }

    /** Default renderer for results table. */
    class NamedObjRenderer extends DefaultTableCellRenderer {
        public void setValue(Object value) {
            String fullName = ((NamedObj)value).getFullName();
            // Strip the name of the model name.
            String strippedName = fullName.substring(fullName.indexOf(".", 1));
            setText(strippedName);
        }
    }

    /** The table model for the search results table. */
    class ResultsTableModel extends AbstractTableModel {
        
        /** Populate the _results list.
         */
        public ResultsTableModel() {
            _results = new NamedObj[0];
        }

        /** Return the number of columns, which is one.
	 *  @return the number of columns, which is 1.   
         */
        public int getColumnCount() {
            return 1;
        }

        /** Get the number of rows.
         *  @return the number of rows.
         */
        public int getRowCount() {
            return _results.length;
        }

        /** Get the column header name.
	 *  @return The string "Found in (select to highlight, double-click to open)".   
         *  @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int col) {
            return "Found in (select to highlight, double-click to open):";
        }

        /** Get the value at a particular row and column.
         *  @param row The row.
         *  @param col The column.
	 *  @return the value.
         */
        public Object getValueAt(int row, int col) {
            return _results[row];
        }

        /** Return NameObj.class.
         *  @param column The column number.
         *  @return Return NamedObj.class.
         */
        public Class getColumnClass(int column) {
            return NamedObj.class;
        }

        /** Return false. Search result cells are not editable.
         *  @param row The row number.
         *  @param column The column number.
         *  @return false.
         */
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public void setContents(Set<NamedObj> results) {
            _results = new NamedObj[results.size()];
            int i = 0;
            for (NamedObj result : results) {
                _results[i++] = result;
            }
            fireTableDataChanged();
        }
    }

}
