/* A transformer that writes C source code.

 Copyright (c) 2002 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.copernicus.c;

// FIXME: cleanup imports
import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;
import java.util.*;
import java.io.*;

/** A transformer that writes C source code.
    @author Shuvra S. Bhattacharyya
    @version $Id$
    @since Ptolemy II 2.0
*/
public class CWriter extends SceneTransformer {
    /** Return a new CWriter.
     *  @return The new CWriter.
     */
    public static CWriter v() {
        return instance;
    }

    /** Get the options associated with the C Writer.
     *  @return The options.
     */
    public String getDeclaredOptions() {
        // FIXME: conditionally allow the debug option.
        // return super.getDeclaredOptions() + " debug outDir";
        // The debug option suppresses exceptions, which in turn forces
        // repeated attempts to generate class files.
        System.out.println("Options: " + super.getDeclaredOptions());
        return super.getDeclaredOptions() + " outDir";
    }

    /** Test if the internal transform associated with this writer has
     *  completed.
     *  @return True if the transform has completed.
     */
    public boolean completedTransform() {
        return _completedTransform;
    }


    /** Write out the C (.i, .h, interface Header) files.
     *  Sample option arguments:
     *	<code>-p wjtp.writeJimple1 outDir:jimple1</code>
     *
     *  @see ClassWriter
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.writeJimple2</code>.
     *  @param options The options Map.  This method uses the
     *  <code>outdir</code> option to specify where the .jimple
     *  file should be written
     */
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CWriter.internalTransform("
                + phaseName + ", " + options + ")");

        String outDir = Options.getString(options, "outDir");

        // FIXME: Remove the next line if things don't break without it
        // _completedTransform = true;

        // We need to cache the classes up front to avoid a concurrent
        // modification exception.
        ArrayList classList = new ArrayList();
        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
             classes.hasNext(); classList.add(classes.next()));

        for (Iterator sootClasses = classList.iterator();
                sootClasses.hasNext(); ) {
            SootClass sootClass = (SootClass)sootClasses.next();

            // Determine the base of the source code file names.
            String fileName;
            if (!outDir.equals("")) {
                File outDirFile = new File(outDir);
                if (!outDirFile.isDirectory()) {
                    outDirFile.mkdirs();
                }
                fileName = outDir + System.getProperty("file.separator");
            } else {
                fileName = "";
            }
            fileName += sootClass.getName();
            //fileName = sootClass.getName();

            // FIXME: move these out of the loop?
            HeaderFileGenerator hGenerator = new HeaderFileGenerator();
            CodeFileGenerator cGenerator = new CodeFileGenerator();
            InterfaceFileGenerator iGenerator = new InterfaceFileGenerator();
            CNames.setup();

            //generate the .i.h, .h, and .c files
            System.out.println("Generating C code files for " + fileName);
            String code = null;
            code = iGenerator.generate(sootClass);
            FileHandler.write(fileName
                    + InterfaceFileGenerator.interfaceFileNameSuffix(),
                      code);
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName + ".h", code);
            code = cGenerator.generate(sootClass);
            FileHandler.write(fileName + ".c", code);
            System.out.println("Done generating C code files for " + fileName);
        }

        _completedTransform = true;
    }

    // Flag that indicates whether transform has been completed.
    private boolean _completedTransform = false;

    private static CWriter instance = new CWriter();
    private CWriter() {}

}

