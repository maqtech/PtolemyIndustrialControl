/* Transform Actors using Soot

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


package ptolemy.copernicus.jhdl;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.dava.*;
import soot.util.*;

public class Main extends KernelMain {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        Scene.v().getPack("wjtp").add(new Transform("wjtp.at",
                ActorTransformer.v(_toplevel)));

        // Create a class for the composite actor of the model
        Scene.v().getPack("wjtp").add(new Transform("wjtp.mt",
                ModelTransformer.v(_toplevel)));

        // Add a command line interface (i.e. Main)
        Scene.v().getPack("wjtp").add(new Transform("wjtp.clt",
                CommandLineTransformer.v(_toplevel)));

        // Inline the director into the composite actor.
        Scene.v().getPack("wjtp").add(new Transform("wjtp.idt",
                InlineDirectorTransformer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
                ClassWriter.v()));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot1",
                JimpleWriter.v()));

        // In each actor and composite actor, ensure that there
        // is a field for every attribute, and replace calls
        // to getAttribute with references to those fields.
        Scene.v().getPack("wjtp").add(new Transform("wjtp.ffat",
               FieldsForAttributesTransformer.v(_toplevel)));
        // In each actor and composite actor, ensure that there
        // is a field for every port, and replace calls
        // to getPort with references to those fields.
        Scene.v().getPack("wjtp").add(new Transform("wjtp.ffpt",
                FieldsForPortsTransformer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot2",
                ClassWriter.v()));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot2",
                JimpleWriter.v()));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.ls",
                new TransformerAdapter(LocalSplitter.v())));

        // While we still have references to ports, use the
        // resolved types of the ports and run a typing
        // algorithm to specialize the types of domain
        // polymorphic actors.  After this step, no
        // uninstantiable types should remain.
        //  Scene.v().getPack("wjtp").add(new Transform("wjtp.ts",
        //        TypeSpecializer.v(_toplevel)));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.tie",
                new TransformerAdapter(TokenInstanceofEliminator.v())));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.ta",
                new TransformerAdapter(TypeAssigner.v())));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.cp",
                new TransformerAdapter(CopyPropagator.v())));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot3",
                ClassWriter.v()));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot3",
                JimpleWriter.v()));

        // Set about removing reference to attributes and parameters.
        // Anywhere where a method is called on an attribute or
        // parameter, replace the method call with the return value
        // of the method.  This is possible, since after
        // initialization attribute values are assumed not to
        // change.  (Note: There are certain cases where this is
        // not true, i.e. the expression actor.  Those will be
        // specially handled before this point, or we should detect
        // assignments to attributes and handle them differently.)
        Scene.v().getPack("wjtp").add(new Transform("wjtp.iat",
                InlineParameterTransformer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot4",
                JimpleWriter.v()));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.snapshot4",
                ClassWriter.v()));

        // Anywhere we have a method call on a token that can be
        // statically evaluated (usually, these will have been
        // created by inlining parameters), inline those calls.
        // We do this before port transformation, since it
        // often allows us to statically determine the channels
        // of port reads and writes.
        Scene.v().getPack("wjtp").add(new Transform("wjtp.itt",
                InlineTokenTransformer.v(_toplevel)));

        Scene.v().getPack("wjtp").add(new Transform("wjtp.circuit",
                ptolemy.copernicus.jhdl.CircuitTransformer.v(_toplevel)));

    }

    /** Read in a MoML model, generate .class files for use with JHDL.
     *  @exception IllegalActionException If the model cannot be parsed.
     *  @exception NameDuplicationException If the name of the
     *  model cannot be changed to a Java identifier String.
     */
    public static void main(String[] args)
	throws IllegalActionException, NameDuplicationException {
	Main main = new Main(args);

	// Parse the model.
	CompositeActor toplevel = main.readInModel(args[0]);

	// Create instance classes for the actors.
	main.initialize(toplevel);

	// Add Transforms to the Scene.
	main.addTransforms();

	main.generateCode(args);
    }
}
