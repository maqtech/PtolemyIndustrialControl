/*
 Transform Actors using Soot and generate C code.

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

package ptolemy.copernicus.c;

// FIXME: clean up import list.
import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.java.ActorTransformer;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.ImprovedDeadAssignmentEliminator;
import ptolemy.copernicus.kernel.InvocationBinder;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.copernicus.kernel.ClassWriter;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.UnusedFieldRemover;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.copernicus.java.CommandLineTransformer;
//FIXME
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.java.InlineDirectorTransformer;
import ptolemy.copernicus.java.ModelTransformer;



import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.jimple.toolkits.scalar.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/** Read in a MoML model, generate .c files with very few
    dependencies on Ptolemy II.

    @author Shuvra S. Bhattacharyya, Michael Wirthlin, Stephen Neuendorffer,
    Edward A. Lee, Christopher Hylands
    @version $Id$
*/

public class Main extends ptolemy.copernicus.java.Main {
    /** Read in a MoML model and generate Java classes for that model.
     *  @param args An array of Strings that control the transformation
     */
    public Main(String [] args) throws IllegalActionException {
        // args[0] contains the MoML class name.
        super(args);
    }


    /** Add transforms to the Scene.
     */
    public void addTransforms() {
	    super.addTransforms();
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.finalSnapshot", CWriter.v()));

        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.watchDogCancel",
                        WatchDogTimer.v(), "cancel:true"));
    }


    /** Read in a MoML model, generate .class files for use with C.
     *  Arguments are passed on to soot, except for special non-Soot
     *  arguments that are used to configure the overall code generation
     *  process. Presently, two non-Soot argument are recognized:
     *  "-clooped," which indicates that schedule loops should be translated
     *  into loops in the generated code (FIXME: this is not supported yet);
     *  and "-cdebug" which turns on debugging output for c code generation.
     *  The first argument specifies the MoML model. This is followed
     *  by zero or more non-Soot arguments, after which the Soot arguments
     *  are listed.
     *  @param args Code generation arguments.
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args) {

        String modelName = args[0];
        try {
            long startTime = System.currentTimeMillis();

            Main main = new Main(args);

            // Parse the model.
	        CompositeActor toplevel = main.readInModel(modelName);

            // Create instance classes for the actors.
            main.initialize(toplevel);

            // Add Transforms to the Scene.
            main.addTransforms();

            main.generateCode(args);

            // Print out memory usage info
            System.out.println(modelName + " "
                    + ptolemy.actor.Manager.timeAndMemory(startTime));
            // We need to call exit here if we are running codegen on
            // a model that uses Swing.  Useful models that use the
            // plotter fall in this category.
            System.exit(0);
        } catch (Exception ex) {
	    System.err.println("Code generation of '" + modelName
                    + "' failed:");
            ex.printStackTrace(System.err);
            System.err.flush();
	    System.exit(2);
        }

    }
    // Local debugging flag.
    private static boolean _debug = false;

    // The CWriter instance used to generate code.
    private static CWriter _writer = null;
}
