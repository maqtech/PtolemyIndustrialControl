/* A transformer that writes Jimple text.

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;
import java.util.*;
import java.io.*;

/**
A transformer that writes Jimple text.
@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class JimpleWriter extends SceneTransformer {
    private static JimpleWriter instance = new JimpleWriter();
    private JimpleWriter() {}

    public static JimpleWriter v() {
        return instance;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug outDir";
    }

    /** Write out the Jimple file.
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
    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("JimpleWriter.internalTransform("
                + phaseName + ", " + options + ")");

        String outDir = Options.getString(options, "outDir");

        for(Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();

            String fileName;

            if(!outDir.equals("")) {
                File outDirFile = new File(outDir);
                if (!outDirFile.isDirectory()) {
                    outDirFile.mkdirs();
                }
                fileName = outDir + System.getProperty("file.separator");
            } else {
                fileName = "";
            }

            fileName += theClass.getName() + ".jimple";

            FileOutputStream streamOut = null;
            PrintWriter writerOut = null;
            try {
                streamOut = new FileOutputStream(fileName);
                writerOut = new PrintWriter(new EscapedWriter(
                        new OutputStreamWriter(streamOut)));
                theClass.printJimpleStyleTo(writerOut, 0);
            }
            catch (IOException e) {
                System.out.println("Failed to output jimple for file '"
                        + fileName + "':" + e);
            }
            finally {
                if (writerOut != null) {
                    writerOut.close();
                }
                try {
                    if (streamOut != null) {
                        streamOut.close();
                    }
                } catch (IOException io) {
                }
            }
        }
    }
}

