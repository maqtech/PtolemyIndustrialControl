/* Transform Actors using Soot and generate C

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

package ptolemy.copernicus.deep;


import ptolemy.copernicus.kernel.ActorTransformer;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.SootUtilities;

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

import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/**
Read in a MoML model, generate .class files with very few
dependencies on Ptolemy II.

FIXME: This is only a stub file, it does not implement
C code generation yet.

@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
*/

public class Main extends KernelMain {
    

    /** Read in a MoML mode and generate Java classes for that model. 
     *  @param args An array of Strings that control the transformation
     */
    public Main(String [] args) throws Exception {
	super(args);

        // Process the global options.
        // FIXME!!
        String options = "deep targetPackage:ptolemy.apps.soot.demo.SimpleAdd.cg";
 
        // Add a transformer to convert each actor class to Deep Class
        // "wjtp" means "whole java tranformation package"
        // This transformer is required to be a scene transformer,
        // and it is applied before body transformers.
        // "wjtp.deep" is the name of the phase.
        //Scene.v().getPack("wjtp").add(new Transform("wjtp.deep", 
        //        DeepTransformer.v(_toplevel), options));
        
        // Add transformers to do other passes.
        // "jtp" mean "java tranformation package.
        // These transformers are required to be a body transformer,
        // and are applied after scene transformers.

        // First pass: "cpaf" = "constant propagator and folder"
        Scene.v().getPack("jtp").add(new Transform("jtp.cpaf",
                ConstantPropagatorAndFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.cbf",
                ConditionalBranchFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.uce",
                UnreachableCodeEliminator.v()));
	Scene.v().getPack("jtp").add(new Transform("jtp.cp",
                  CopyPropagator.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.dae",
                DeadAssignmentEliminator.v()));
           
        _callSootMain(args);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in a MoML model, generate .class files with very few
     *  dependencies on Ptolemy II.
     */
    public static void main(String[] args) throws Exception {
	// We do most of the work in the constructor so that we
	// can more easily test this class
	Main main = new Main(args);
    }

}
