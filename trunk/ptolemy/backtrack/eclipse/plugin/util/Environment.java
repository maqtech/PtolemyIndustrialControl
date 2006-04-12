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
package ptolemy.backtrack.eclipse.plugin.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.swt.widgets.Shell;

import ptolemy.backtrack.eclipse.ast.Transformer;
import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;
import ptolemy.backtrack.eclipse.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.util.PathFinder;
import ptolemy.backtrack.util.Strings;

import java.io.File;

//////////////////////////////////////////////////////////////////////////
//// Environment

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Environment {
    public static String getPtolemyHome() {
        return getPtolemyHome(null);
    }

    public static String getPtolemyHome(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String PTII = store.getString(PreferenceConstants.PTII);

        boolean valid = !PTII.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Ptolemy home is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            return PTII;
        }
    }

    public static String getRefactoringRoot() {
        return getRefactoringRoot(null);
    }

    public static String getRefactoringRoot(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String root = store.getString(PreferenceConstants.BACKTRACK_ROOT);

        boolean valid = !root.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Refactoring root is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            IPath rootPath = new Path(root);
            String[] segments = rootPath.segments();

            if (segments.length == 1) {
                root = ResourcesPlugin.getWorkspace().getRoot().getProject(
                        segments[0]).getLocation().toOSString();
            } else {
                root = ResourcesPlugin.getWorkspace().getRoot().getFolder(
                        rootPath).getLocation().toOSString();
            }

            return root;
        }
    }

    public static String getSourceList() {
        return getSourceList(null);
    }

    public static String getSourceList(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String sourceList = store
                .getString(PreferenceConstants.BACKTRACK_SOURCE_LIST);

        boolean valid = !sourceList.equals("");

        if (!valid) {
            if (shell != null) {
                MessageDialog.openError(shell, "Ptolemy II Environment Error",
                        "Refactored source list is invalid.\n"
                                + "Please set it in Ptolemy -> Options.");
            }

            return null;
        } else {
            return sourceList;
        }
    }

    public static String[] getClassPaths(Shell shell) {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String PTII = getPtolemyHome(shell);

        if (PTII != null) {
            return new String[] { PTII };
        } else {
            return null;
        }
    }

    public static IPath getRefactoredFile(String source, String packageName) {
        File oldFile = new File(source);
        String simpleName = oldFile.getName();

        if (simpleName.toLowerCase().endsWith(".java")) {
            simpleName = simpleName.substring(0, simpleName.length() - 5);
        }

        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        String newPackage;

        if ((packageName == null) || packageName.equals("")) {
            newPackage = prefix;
        } else if ((prefix == null) || prefix.equals("")) {
            newPackage = packageName;
        } else {
            newPackage = prefix + "." + packageName;
        }

        String fullClassName = (newPackage == null) ? simpleName : (newPackage
                + "." + simpleName);
        String fileName = fullClassName.replace(".", "" + File.separator)
                + ".java";

        String root = store.getString(PreferenceConstants.BACKTRACK_ROOT);
        IPath rootPath = new Path(root);
        return rootPath.append(fileName);
    }

    public static IContainer getAffectedFolder() {
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        IPath path = new Path(store
                .getString(PreferenceConstants.BACKTRACK_ROOT));
        IContainer container = getContainer(path);

        if ((prefix != null) && !prefix.equals("")) {
            container = container.getFolder(new Path(prefix.replace('.',
                    File.separatorChar)));
        }

        return container;
    }

    public static IContainer getContainer(IPath path) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String[] segments = path.segments();
        IContainer container = (segments.length == 1) ? (IContainer) root
                .getProject(segments[0])
                : ((segments.length > 1) ? (IContainer) root.getFolder(path)
                        : null);
        return container;
    }

    public static void createFolders(IContainer container) throws CoreException {
        if (!container.exists()) {
            if (container instanceof IFolder) {
                createFolders(container.getParent());
                ((IFolder) container).create(true, true, null);
            }
        }
    }

    public static boolean setupTransformerArguments(Shell shell,
            boolean config, boolean alwaysOverwrite) {
        // It is OK if PTII is not set.
        String PTII = Environment.getPtolemyHome(null);

        String root = getRefactoringRoot(shell);

        if (root == null) {
            return false;
        }

        //String sourceList = getSourceList(shell);
        //if (sourceList == null)
        //    return false;
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String prefix = store.getString(PreferenceConstants.BACKTRACK_PREFIX);
        boolean overwrite = alwaysOverwrite
                || store.getBoolean(PreferenceConstants.BACKTRACK_OVERWRITE);
        boolean generateConfiguration = store
                .getBoolean(PreferenceConstants.BACKTRACK_GENERATE_CONFIGURATION);
        String configuration = store
                .getString(PreferenceConstants.BACKTRACK_CONFIGURATION);

        String[] args = new String[] { "-prefix", prefix, "-output", root,
                overwrite ? "-overwrite" : "-nooverwrite" };

        if (config && generateConfiguration && (configuration != null)
                && !configuration.equals("")) {
            String[] extraArgs = new String[] { "-config", configuration };
            args = Strings.combineArrays(args, extraArgs);
        }

        if (PTII != null) {
            PathFinder.setPtolemyPath(PTII);
        } else {
            PathFinder.setPtolemyPath("");
        }

        int start = 0;

        while (start < args.length) {
            int newPosition = Transformer.parseArguments(args, start);

            if (newPosition != start) {
                start = newPosition;
            } else {
                break;
            }
        }

        return true;
    }
}
