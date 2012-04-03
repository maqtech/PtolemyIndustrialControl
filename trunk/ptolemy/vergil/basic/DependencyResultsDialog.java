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
public class DependencyResultsDialog extends SearchResultsDialog {

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
        _resultsTable.setDefaultRenderer(NamedObj.class, new DependencyResultsNamedObjRenderer());
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the query dialog.
     *  Derived classes may change the layout of the query dialog.
     */
    protected void _initializeQuery() {
        _query.setColumns(2);
        _query.addCheckBox("prerequisites", "Prerequisites", true);
        _query.addCheckBox("dependents", "Dependents", true);
    }

    
    /** Perform a search and update the results table.
     */
    protected void _search() {
        boolean prerequisites = _query.getBooleanValue("prerequisites");
        boolean dependents = _query.getBooleanValue("dependents");
        try {
            Set<NamedObj>results = _findDependencies((Actor)_target, prerequisites, dependents);
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

    /** Return a list of objects in the model that match the
     *  specified search.
     *  @param actor The actor to be searched.
     *  @param prerequisites True to search for prerequisites.
     *  @param dependents True to search for dependents.
     *  @return The Set of objects in the model that match the specified search.
     *  @exception KernelException If thrown while preinitializing() or wrapping up.
     */
    protected Set<NamedObj> _findDependencies(
            Actor actor, boolean prerequisites, boolean dependents) throws KernelException {

        // FIXME: we could add a field to the search box for specifying the filter
        // class.  However, how would we specify that searching Publishers should
        // have Subscribers as the field?
        Class clazz = AtomicActor.class;
        SortedSet<NamedObj> result = new TreeSet<NamedObj>(new NamedObjComparator());
        if (prerequisites) {
            _report("Generating prerequisite information.");
            System.out.println("_findDependencies: " + actor);
            result.addAll(ActorDependencies.prerequisites(actor, clazz));
            _report("");
        }
        if (dependents) {
            _report("Generating dependency information.");
            result.addAll(ActorDependencies.dependents(actor, clazz));
            _report("");
        }
        return result;
    }
    
    /** Return a URL that points to the help page.
     *  @return A URL that points to the help page
     */
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                "ptolemy/vergil/basic/doc/DependencyResultsDialog.htm");
        return helpURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Default renderer for results table. */
    class DependencyResultsNamedObjRenderer extends DefaultTableCellRenderer {
        public void setValue(Object value) {
         String fullName = ((NamedObj)value).getFullName();
            String strippedName = fullName.substring(fullName.indexOf(".", 1));
            setText(strippedName);
        }
    }
}
