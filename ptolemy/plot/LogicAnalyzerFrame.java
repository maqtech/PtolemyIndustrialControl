/* A simple application that uses the ptolemy.plot package.

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
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

// TO DO:
//   - Add a mechanism for combining two plots into one
//   - Convert to use swing, especially for the menu.
//   - Add a swing-based dialog for setting the plot format
//   - Add a swing-based dialog for adding points.
//   - Improve the help mechanism and separate from the usage message.
//   - Add an "export" mechanism.  Should create:
//        + and HTML file and a .plt plot file.
//        + a gif
//        + an MIF file
//        + what else?

//////////////////////////////////////////////////////////////////////////
//// LogicAnalyzerFrame
/**

LogicAnalyzerFrame is a versatile two-dimensional data plotter that runs as
part of an application, but in its own window. It can read files
compatible with the Ptolemy plot file format (currently only ASCII),
or the application can interact directly with the contained Plot
object, which is visible as a public member. For a description of
the file format, see the Plot and PlotBox classes.
<p>
An application that uses this class should set up the handling of
window-closing events.  Presumably, the application will exit when
all windows have been closed. This is done with code something like:
<pre>
    plotFrameInstance.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            // Handle the event
        }
    });
</pre>

@see Plot
@see PlotBox
@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class LogicAnalyzerFrame extends Frame {

    /** Construct a plot frame with a default title.
     */
    public LogicAnalyzerFrame() {
        this("Ptolemy Plot Frame");
    }

    /** Construct a plot frame with the specified title.
     */
    public LogicAnalyzerFrame(String title) {
        super(title);
        // File menu
        MenuItem[] fileMenuItems = {
            // FIXME: These shortcuts are not right.
            new MenuItem("Open", new MenuShortcut(KeyEvent.VK_O)),
            new MenuItem("Save", new MenuShortcut(KeyEvent.VK_S)),
            new MenuItem("SaveAs", new MenuShortcut(KeyEvent.VK_A)),
            new MenuItem("Print", new MenuShortcut(KeyEvent.VK_P)),
            new MenuItem("Close", new MenuShortcut(KeyEvent.VK_C)),
        };
        FileMenuListener fml = new FileMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < fileMenuItems.length; i++) {
            fileMenuItems[i].setActionCommand(fileMenuItems[i].getLabel());
            fileMenuItems[i].addActionListener(fml);
            _fileMenu.add(fileMenuItems[i]);
        }
        _menubar.add(_fileMenu);

        // Special menu
        MenuItem[] specialMenuItems = {
            new MenuItem("About", null),
            new MenuItem("Help", new MenuShortcut(KeyEvent.VK_H)),
            new MenuItem("Clear", new MenuShortcut(KeyEvent.VK_C)),
            new MenuItem("Fill", new MenuShortcut(KeyEvent.VK_F)),
        };
        SpecialMenuListener sml = new SpecialMenuListener();
        // Set the action command and listener for each menu item.
        for(int i = 0; i < specialMenuItems.length; i++) {
            specialMenuItems[i].setActionCommand(
                    specialMenuItems[i].getLabel());
            specialMenuItems[i].addActionListener(sml);
            _specialMenu.add(specialMenuItems[i]);
        }
        _menubar.add(_specialMenu);

        setMenuBar(_menubar);

        add("Center",logicAnalyzer);
        // FIXME: This should not be hardwired in here.
        setSize(500, 300);
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial The plot object held by this frame. */
    public LogicAnalyzer logicAnalyzer = new LogicAnalyzer();

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial Menubar for this frame. */
    protected MenuBar _menubar = new MenuBar();

    /** @serial File menu for this frame. */
    protected Menu _fileMenu = new Menu("File");

    /** @serial Special menu for this frame. */
    protected Menu _specialMenu = new Menu("Special");

    /** @serial directory that contains the input file. */
    protected String _directory = null;

    /** @serial name of the input file. */
    protected String _filename = null;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        // FIXME: Is the web address correct?
        Message message = new Message(
                "Ptolemy plot frame\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version 3.1, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n");
        message.setTitle("About Ptolemy Plot");
    }

    /** Close the window.
     */
    protected void _close() {
        dispose();
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        // FIXME:  This is a pretty lame excuse for help...
        Message message = new Message("Help information should go here!");
        message.setTitle("Plot frame");
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        FileDialog filedialog = new FileDialog(this, "Select a plot file");
        filedialog.setFilenameFilter(new PlotFilenameFilter());
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setVisible(true);
        String filename = filedialog.getFile();
        if (filename == null) return;
        _directory = filedialog.getDirectory();
        File file = new File(_directory, filename);
        _filename = null;
        try {
            logicAnalyzer.clear(true);
            logicAnalyzer.read(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            Message msg = new Message("File not found: " + ex);
        } catch (IOException ex) {
            Message msg = new Message("Error reading input: " + ex);
        }
        _filename = filename;
    }

    /** Print the plot.
     */
    protected void _print() {
        // The awt uses properties to set the defaults:
        // awt.print.destination   - can be "printer" or "file"
        // awt.print.printer       - print command
        // awt.print.fileName      - name of the file to print
        // awt.print.numCopies     - obvious
        // awt.print.options       - options to pass to the print command
        // awt.print.orientation   - can be "portrait" or "landscape"
        // awt.print.paperSize     - can be "letter", "legal", "executive"
        //                           or "a4"

        // Accept the defaults... But if you want to change them,
        // do something like this...
        // Properties newprops = new Properties();
        // newprops.put("awt.print.destination", "file");
        // newprops.put("awt.print.fileName", _outputFile);
        // PrintJob printjob = getToolkit().getPrintJob(this,
        //      getTitle(), newprops);
        PrintJob printjob = getToolkit().getPrintJob(this,
                getTitle(), null);
        if (printjob != null) {
            try {
                Graphics printgraphics = printjob.getGraphics();
                if (printgraphics != null) {
                    // Print only the plot frame.
                    try {
                        logicAnalyzer.printAll(printgraphics);
                    } finally {
                        printgraphics.dispose();
                    }
                }
            } finally {
                printjob.end();
            }
        }
    }

    /** Save the plot to the current file, determined by the _directory
     *  and _filename protected variables.
     */
    protected void _save() {
        if (_filename != null) {
            File file = new File(_directory, _filename);
            try {
                FileOutputStream fout = new FileOutputStream(file);
                logicAnalyzer.write(fout);
            } catch (IOException ex) {
                Message msg = new Message("Error writing file: " + ex);
            }
        } else {
            _saveAs();
        }
    }

    /** Query the user for a filename and save the plot to that file.
     */
    protected void _saveAs() {
        FileDialog filedialog = new FileDialog(this, "Save plot as...");
        filedialog.setFilenameFilter(new PlotFilenameFilter());
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setFile("plot.plt");
        filedialog.setVisible(true);
        _filename = filedialog.getFile();
        if (_filename == null) return;
        _directory = filedialog.getDirectory();
        _save();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    class FileMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MenuItem target = (MenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Open")) _open();
            else if (actionCommand.equals("Save")) _save();
            else if (actionCommand.equals("SaveAs")) _saveAs();
            else if (actionCommand.equals("Print")) _print();
            else if (actionCommand.equals("Close")) _close();
        }
    }

    class SpecialMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MenuItem target = (MenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("About")) {
                _about();
            } else if (actionCommand.equals("Help")) {
                _help();
            } else if (actionCommand.equals("Fill")) {
                logicAnalyzer.fillPlot();
            } else if (actionCommand.equals("Clear")) {
                logicAnalyzer.clear(false);
                logicAnalyzer.repaint();
            }
        }
    }

    // FIXME: This filter doesn't work.  Why?
    class PlotFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            if (name.endsWith(".plt")) return true;
            return false;
        }
    }
}
