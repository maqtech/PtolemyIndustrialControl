/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.plugin.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.util.Environment;

//////////////////////////////////////////////////////////////////////////
//// BacktrackingPreferencePage
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackingPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    public BacktrackingPreferencePage() {
        super(GRID);
        setPreferenceStore(EclipsePlugin.getDefault().getPreferenceStore());
        setDescription("Ptolemy II backtracking settings.");
    }
    
    public Control createContents(Composite parent) {
        _toolkit = new FormToolkit(getShell().getDisplay());
        
        _form = _toolkit.createScrolledForm (parent);
        _form.setLayoutData(new GridData(GridData.FILL_BOTH));
        _form.setBackground(null);
        
        _form.getBody().setLayout(new GridLayout(1, true));
        
        _createSection1();
        _createSection2();
        _createSection3();
        _createSection4();
        
        initialize();
        checkState();
        
        return parent;
    }
    
    public void init(IWorkbench workbench) {
    }
    
    public void setVisible(boolean visible) {
        if (visible) {
            _updateSources();
            _checkEnabled();
        }
        super.setVisible(visible);
    }

    protected void addField(FieldEditor editor) {
        _fields.add(editor);
        super.addField(editor);
    }
    
    protected void createFieldEditors() {
    }
    
    private void _checkEnabled() {
        String PTII = Environment.getPtolemyHome();
        Iterator fieldsIter = _fields.iterator();
        while (fieldsIter.hasNext()) {
            FieldEditor editor = (FieldEditor)fieldsIter.next();
            editor.setEnabled(PTII != null, _getParent(editor));
        }
    }
    
    private void _createSection1() {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION |
                Section.TWISTIE |
                Section.CLIENT_INDENT);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setBackground(null);
        section.setText("Refactoring Sources");
        //_toolkit.createCompositeSeparator(section);
        section.setDescription(
                "Set the sources of refactoring. A source list file stores " +
                "the complete list of Java source files to be refactored. " +
                "A single Java source file name is written on each line of " +
                "the source list. The source file names may use paths " +
                "relative to the path where the source list file is in.");
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                _form.reflow(false);
            }
        });
        
        Composite composite = _newComposite(section);
        section.setClient(composite);
        
        Composite currentComposite = _newComposite(composite);
        _sourceList = new FileFieldEditor(
                PreferenceConstants.BACKTRACK_SOURCE_LIST,
                "Source &list file:", currentComposite) {
            protected boolean checkState() {
                boolean superResult = super.checkState();
                
                if (!getTextControl(_getParent(this)).isEnabled())
                    return superResult;
                
                if (getStringValue().equals(_lastString)) {
                    if (superResult)
                        return true;
                } else {
                    _lastString = getStringValue();
                    _sourcesModified = false;
                    if (superResult)
                        return _updateSources();
                }
                
                List list = _sources.getListControl(_getParent(_sources));
                list.removeAll();
                return false;
            }
            
            private String _lastString = null;
        };
        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _sourceList.getTextControl(currentComposite).setLayoutData(gridData);
        _setParent(_sourceList, currentComposite);
        addField(_sourceList);

        currentComposite = _newComposite(composite);
        _sources = new ListEditor(
                PreferenceConstants.BACKTRACK_SOURCES,
                "&Sources", currentComposite) {
            protected String createList(String[] items) {
                StringBuffer path = new StringBuffer("");
                for (int i = 0; i < items.length; i++) {
                    path.append(items[i]);
                    path.append(File.pathSeparator);
                }
                return path.toString();
            }
            
            protected String getNewInputObject() {
                FileDialog dialog = new FileDialog(getShell());
                dialog.setText("Please choose a file to be added to the source list");
                String file = dialog.open();
                if (file != null) {
                    file = file.trim();
                    if (file.length() == 0)
                        return null;
                }
                return file;
            }
            
            protected String[] parseString(String stringList) {
                StringTokenizer st =
                    new StringTokenizer(stringList, File.pathSeparator
                            + "\n\r");
                ArrayList v = new ArrayList();
                while (st.hasMoreElements()) {
                    v.add(st.nextElement());
                }
                return (String[]) v.toArray(new String[v.size()]);
            }
            
            protected void doLoad() {
            }

            protected void doLoadDefault() {
            }

            protected void doStore() {
                if (_sourcesModified) {
                    List list = getListControl(_getParent(this));
                    String[] items = list.getItems();
                    String fileName = _sourceList.getStringValue();
                    try {
                        PrintWriter writer =
                            new PrintWriter(new FileOutputStream(fileName));
                        for (int i = 0; i < items.length; i++)
                            writer.write(items[i] + "\n");
                        writer.close();
                    } catch (Exception e) {
                        MessageDialog.openError(getShell(), "Error writing file", e.getMessage());
                    }
                }
            }
        };
        gridData = new GridData();
        gridData.widthHint = 0;
        gridData.heightHint = LIST_HEIGHT;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _sources.getListControl(currentComposite).setLayoutData(gridData);
        _setParent(_sources, currentComposite);
        addField(_sources);
    }
    
    private void _createSection2() {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION |
                Section.TWISTIE |
                Section.CLIENT_INDENT);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setBackground(null);
        section.setText("Location");
        section.setDescription(
                "Set the location to store the refactored Java code. The " +
                "location of the output files is defined by the root of the " +
                "classes, and packages where the classes are in. A prefix " +
                "may be added to existing package declarations at the time " +
                "of refactoring.");
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                _form.reflow(false);
            }
        });
        
        Composite composite = _newComposite(section);
        section.setClient(composite);
        
        Composite currentComposite = _newComposite(composite);
        _root = new DirectoryFieldEditor(
                PreferenceConstants.BACKTRACK_ROOT,
                "&Root of refactoring:", currentComposite);
        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _root.getTextControl(currentComposite).setLayoutData(gridData);
        _setParent(_root, currentComposite);
        addField(_root);
        
        currentComposite = _newComposite(composite);
        _prefix = new StringFieldEditor(
                PreferenceConstants.BACKTRACK_PREFIX,
                "&Extra prefix:", currentComposite);
        _setParent(_prefix, currentComposite);
        addField(_prefix);
    }
    
    private void _createSection3() {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION |
                Section.TWISTIE |
                Section.CLIENT_INDENT);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setBackground(null);
        section.setText("Extra Class Paths");
        section.setDescription(
                "Add class paths to locate classes in name resolving. The " +
                "Ptolemy II home directory is by default in the class paths.");
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                _form.reflow(false);
            }
        });
        
        Composite composite = _newComposite(section);
        section.setClient(composite);
        
        Composite currentComposite = _newComposite(composite);
        _extraClassPaths = new PathEditor(
                PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS,
                "Extra &classpaths:",
                "Add a path to the classpaths",
                currentComposite);
        GridData gridData = new GridData();
        gridData.widthHint = 0;
        gridData.heightHint = LIST_HEIGHT;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        _extraClassPaths.getListControl(currentComposite).setLayoutData(gridData);
        _setParent(_extraClassPaths, currentComposite);
        addField(_extraClassPaths);
    }
    
    private void _createSection4() {
        Section section = _toolkit.createSection(_form.getBody(),
                Section.DESCRIPTION |
                Section.TWISTIE |
                Section.CLIENT_INDENT);
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setBackground(null);
        section.setText("Miscalleneous");
        section.setDescription(
                "Set other options.");
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                _form.reflow(false);
            }
        });
        
        Composite composite = _newComposite(section);
        section.setClient(composite);
        
        Composite currentComposite = _newComposite(composite);
        _overwrite = new BooleanFieldEditor(
                PreferenceConstants.BACKTRACK_OVERWRITE,
                "&Overwrite existing Java files",
                currentComposite);
        _setParent(_overwrite, currentComposite);
        addField(_overwrite);
    }
    
    private Composite _newComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setBackground(null);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return composite;
    }
    
    private boolean _updateSources() {
        String fileName = _sourceList.getStringValue();
        File sourceListFile = new File(fileName);
        File sourceListPath = sourceListFile.getParentFile();
        List list = _sources.getListControl(_getParent(_sources));
        list.removeAll();
        try {
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(
                        new FileInputStream(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                File file = new File(sourceListPath, line);
                if (!file.exists()) {
                    MessageDialog.openError(getShell(), "Error",
                            "Cannot open source file \"" + line + "\".");
                    throw new FileNotFoundException();
                }
                list.add(line);
            }
            reader.close();
            return true;
        } catch (Exception e) {
            list.removeAll();
            return false;
        }
    }
    
    private Composite _getParent(FieldEditor editor) {
        return (Composite)_composites.get(editor);
    }
    
    private void _setParent(FieldEditor editor, Composite parent) {
        _composites.put(editor, parent);
    }

    private static final int LIST_HEIGHT = 100;
    
    private boolean _sourcesModified = false;
    
    private java.util.List _fields = new LinkedList();
    
    private Hashtable _composites = new Hashtable();
    
    /* Controls */
    private FormToolkit _toolkit;
    
    private ScrolledForm _form;
    
    private FileFieldEditor _sourceList;
    
    private ListEditor _sources;
    
    private DirectoryFieldEditor _root;
    
    private StringFieldEditor _prefix;
    
    private PathEditor _extraClassPaths;
    
    private BooleanFieldEditor _overwrite;
}
