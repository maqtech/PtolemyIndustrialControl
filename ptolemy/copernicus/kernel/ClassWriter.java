/* A transformer that writes class files.

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
Write all of the application classes out to class files.  Jasmin files
for the classes will be created in a temporary directory and then compiled
into bytecode using the Jasmin assembler.  The output directory is specified
using the outDir parameter.  The class files will be placed in
the appropriate subdirectory of that directory according to their package
name.
@author Stephen Neuendorffer
@version $Id$
*/
public class ClassWriter extends SceneTransformer {
    private static ClassWriter instance = new ClassWriter();
    private ClassWriter() {}

    public static ClassWriter v() {
        return instance;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug outDir";
    }


    /** Write out the class file.
     *  This transform can be used to take snapshots, and is
     *  usually called in conjunction with JimpleWriter inside addTransforms():
     *  <pre>
     *   Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
     *           ClassWriter.v()));
     *   Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
     *           JimpleWriter.v()));
     *  </pre>
     *  Sample option arguments:
     *	<code>-p wjtp.snapshot1 outDir:jimple1</code>
     *
     *  @see JimpleWriter
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.snapshot1</code>.
     *  @param options The options Map.  This method uses the
     *  <code>outDir</code> option to specify where the .class
     *  file should be written
     */
    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("ClassWriter.internalTransform("
                + phaseName + ", " + options + ")");

        String outDir = Options.getString(options, "outDir");

        if (!outDir.equals("")) {
            File outDirFile = new File(outDir);
            if (!outDirFile.isDirectory()) {
                outDirFile.mkdirs();
            }
        }

        for (Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();

            theClass.write(outDir);
        }
    }
}

