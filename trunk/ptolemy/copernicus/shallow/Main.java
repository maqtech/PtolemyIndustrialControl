/* Main for shallow code generation.

 Copyright (c) 2001 The Regents of the University of California.
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

package ptolemy.copernicus.shallow;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.CastAndInstanceofEliminator;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.ImprovedDeadAssignmentEliminator;
import ptolemy.copernicus.kernel.InstanceEqualityEliminator;
import ptolemy.copernicus.kernel.JimpleWriter;
import ptolemy.copernicus.kernel.SideEffectFreeInvocationRemover;
import ptolemy.copernicus.kernel.TransformerAdapter;
import ptolemy.copernicus.kernel.WatchDogTimer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.StaticMethodBinder;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.*;
import soot.dava.*;
import soot.util.*;

import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/**
Read in a MoML model and generate a Java class that creates the
same model.  (i.e. shallow code generation)
No attempt is made to analyze actor code.  This is primarily
useful for using the Java compiler to find bugs, and removing
MoML from shipped code.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class Main extends KernelMain {

    /** Read in a MoML model.
     *  @params args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  command line options, see the superclass documentation for details.
     *  @exception IllegalActionException If the model cannot be parsed.
     */
    public Main(String [] args) throws IllegalActionException {
	// args[0] contains the MoML class name.
	super(args[0]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add transforms to the Scene.
     */
    public void addTransforms() {
	super.addTransforms();

        // Set up a watch dog timer to exit after a certain amount of time.
        // For example, to time out after 5 minutes, or 300000 ms:
	// -p wjtp.watchDog time:30000
        Scene.v().getPack("wjtp").add(new Transform("wjtp.watchDog",
                WatchDogTimer.v()));

        // Create a class for the composite actor of the model
        Scene.v().getPack("wjtp").add(new Transform("wjtp.mt",
                ShallowModelTransformer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.writeJimple1",
                JimpleWriter.v()));

        // Run the standard soot optimizations.  We explicitly specify
        // this instead of using soot's -O flag so that we can
        // have access to the result.
        _addStandardOptimizations(Scene.v().getPack("wjtp"));


        // Disable the watch dog timer
        Scene.v().getPack("wjtp").add(new Transform("wjtp.watchDogCancel",
                WatchDogTimer.v(), "cancel:true"));

    }

    /** Read in a MoML model, generate java files
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
            throws IllegalActionException, NameDuplicationException {

        long startTime = System.currentTimeMillis();

	Main main = new Main(args);

	// Parse the model.
	CompositeActor toplevel = main.readInModel(args[0]);

	// Create instance classes for the actors.
	main.initialize(toplevel);

	// Add Transforms to the Scene.
	main.addTransforms();

	main.generateCode(args);

        // Print out memory usage info
	System.out.println(ptolemy.actor.Manager.timeAndMemory(startTime));

        WatchDogTimer.v().cancel();

        // For some reason, we need to call exit here, perhaps because
        // the WatchDog timer thread is still running in the background?
        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add transforms corresponding to the standard soot optimizations
     *  to the given pack.
     */
    private void _addStandardOptimizations(Pack pack) {
        pack.add(new Transform("jop.cse",
                new TransformerAdapter(CommonSubexpressionEliminator.v())));
        pack.add(new Transform("jop.cp",
                new TransformerAdapter(CopyPropagator.v())));
        pack.add(new Transform("jop.cpf",
                new TransformerAdapter(ConstantPropagatorAndFolder.v())));
        pack.add(new Transform("jop.cbf",
                new TransformerAdapter(ConditionalBranchFolder.v())));
        pack.add(new Transform("jop.dae",
                new TransformerAdapter(DeadAssignmentEliminator.v())));
        pack.add(new Transform("jop.uce1",
                new TransformerAdapter(UnreachableCodeEliminator.v())));
        pack.add(new Transform("jop.ubf1",
                new TransformerAdapter(UnconditionalBranchFolder.v())));
        pack.add(new Transform("jop.uce2",
                new TransformerAdapter(UnreachableCodeEliminator.v())));
        pack.add(new Transform("jop.ubf2",
                new TransformerAdapter(UnconditionalBranchFolder.v())));
        pack.add(new Transform("jop.ule",
                new TransformerAdapter(UnusedLocalEliminator.v())));
    }
}













