/* Transform Actors using Soot and generate C code.

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
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.copernicus.java.ModelTransformer;
import ptolemy.copernicus.java.InlineDirectorTransformer;
import ptolemy.copernicus.java.CommandLineTransformer;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
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

public class Main extends KernelMain {

    /** Read in a MoML model and generate Java classes for that model.
     *  @param args An array of Strings that control the transformation
     */
    public Main(String [] args) throws IllegalActionException {
	    // args[0] contains the MoML class name.
	    super(args[0]);
    }

    /** Add transforms to the Scene.
     */
    public void addTransforms() {

	    super.addTransforms();

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
	    // -p wjtp.watchDog time:30000
        Scene.v().getPack("wjtp").add(new Transform("wjtp.watchDog",
                WatchDogTimer.v()));

        // Sanitize names of objects in the model.
        // We change the names to all be valid java identifiers
        // so that we can
        //      Scene.v().getPack("wjtp").add(new Transform("wjtp.ns",
        //         NameSanitizer.v(_toplevel)));

        // Create instance classes for actors.
	    // This transformer takes no input as far as soot is concerned
	    // (i.e. no application classes) and creates application
	    // classes from the model.
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.at", ActorTransformer.v(_toplevel)));

        // Create a class for the composite actor of the model
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.mt", ModelTransformer.v(_toplevel)));

        // Inline the director into the composite actor.
        InlineDirectorTransformer directorTransformer = null;
        if (_generateLoopedSchedule) {
            // directorTransformer = LoopedScheduleTransformer.v(_toplevel);
            directorTransformer = InlineDirectorTransformer.v(_toplevel);
        } else {
            directorTransformer = InlineDirectorTransformer.v(_toplevel);
        }
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.idt", directorTransformer));

        // Add a command line interface (i.e. Main)
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.clt",
                        CommandLineTransformer.v(_toplevel)));

        // Generate C code
        _writer = CWriter.v();
        Scene.v().getPack("wjtp").add(
                new Transform("wjtp.snapshot1", _writer));

        // FIXME: add optimizations
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call soot.Main.main(), which does command line argument
     *  processing and then starts the transformation.  This method
     *  should be called after calling initialize() and addTransforms().
     *
     *  @param args Soot command line arguments to be passed
     *  to soot.Main.main().
     */
    public void generateCode(String [] args) {
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";

        // Rather than calling soot.Main.main() here directly, which
        // spawns a separate thread, we run this in the same thread
        //soot.Main.main(args);
        System.out.println("debug args = " + Arrays.asList(args));
        soot.Main.setReservedNames();
        soot.Main.setCmdLineArgs(args);
        soot.Main main = new soot.Main();
        soot.ConsoleCompilationListener consoleCompilationListener =
                new soot.ConsoleCompilationListener();
        soot.Main.addCompilationListener(consoleCompilationListener);
        // main.run(false);
        main.run();
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
    public static void main(String[] args)
            throws IllegalActionException, NameDuplicationException {

        _writer = null;
	    Main main = new Main(args);

	    // Parse the model.
	    CompositeActor toplevel = main.readInModel(args[0]);

        // Set of argument indices to skip when constructing the argument
        // list for Soot.
        int skipCount = 0;
        boolean[] skipArgument = new boolean[args.length];
        for (int i = 0; i < args.length; i++) {
            skipArgument[i] = false;
        }

        // Extract non-Soot arguments.
        int index = 1;    // Index into original argument array.
        boolean done = false; // Finished looking for non-Soot arguments.
        while (!done) {
            if (args.length <= index) {
                done = true;
            } else if (args[index].equals("-clooped")) {
                 _generateLoopedSchedule = true;
                 skipArgument[index++] = true;
                 skipCount++;
            } else if (args[index].equals("-cdebug")) {
                 _debug = true;
                 skipArgument[index++] = true;
                 skipCount++;
            } else {
                done = true;
            }
        }

        // Filter out arguments not meant for Soot.
        if (skipCount >= args.length) {
            throw new RuntimeException("Too many non-Soot arguments.");
        }
        String [] sootArguments = new String[args.length - skipCount];
        int sootIndex = 0;
        for (int i = 0; i < args.length; i++) {
            if (!skipArgument[i]) {
                sootArguments[sootIndex++] = args[i];
            }
        }

	    // Create instance classes for the actors.
	    main.initialize(toplevel);

	    // Add Transforms to the Scene.
	    main.addTransforms();

        // Ignore exceptions that pertain to writing unnecessary class files
        // after code generation is complete.
	    try {
            main.generateCode(sootArguments);
        } catch (RuntimeException exception) {
            // Under debug mode, soot throws a RuntimeException if it
            // can't generate a class file. We wan't to generate (and
            // ignore) this exception here so that class files are not
            // generated. More specifically, we ignore any exceptions that
            // occur after code generation is completed.
            if ((_writer == null) || (!_writer.completedTransform())) {
                throw new RuntimeException(exception.getMessage()
                        + exception.getClass().getName());
            } else if (_debug) {
                System.err.println("Warning: exception after code generation"
                        + " completed.\n" + exception + "\n");
                exception.printStackTrace();
            }
        }
    }

    // Local debugging flag.
    private static boolean _debug = false;

    // Flags to control what gets generated.
    private boolean _generateJimple = false;
    private static boolean _generateLoopedSchedule = false;

    // The CWriter instance used to generate code.
    private static CWriter _writer = null;
}
