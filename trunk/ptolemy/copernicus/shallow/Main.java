/* Transform Actors using Soot

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

import ptolemy.copernicus.kernel.ActorTransformer;
import ptolemy.copernicus.kernel.KernelMain;

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
Read in a MoML model and generate Java classes for that model.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class Main extends KernelMain {    

    /** Read in a MoML mode and generate Java classes for that model. 
     *  @param args An array of Strings that control the transformation
     */
    public Main(String [] args) throws Exception {
	super(args);

        // Process the options.
        // FIXME!!
        String options = "deep targetPackage:ptolemy.copernicus.shallow.cg";

        Scene.v().getPack("wjtp").add(new Transform("wjtp.at", 
                ActorTransformer.v(_toplevel), options));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.mt", 
                ModelTransformer.v(_toplevel), options));
        Scene.v().getPack("wjtp").add(new Transform("wjtp.clt", 
                CommandLineTransformer.v(_toplevel), options));
       
        //    Scene.v().getPack("wjtp").add(new Transform("wjtp.ibg",
        //        InvokeGraphBuilder.v()));
        // Scene.v().getPack("wjtp").add(new Transform("wjtp.si",
        //        StaticInliner.v()));
        
        // When we fold classes, we create extra locals.  These optimizations
        // will remove them.  Unfortunately, -O creates bogus code?
        /*   Scene.v().getPack("jtp").add(new Transform("jtp.cpaf",
                ConstantPropagatorAndFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.cbf",
                ConditionalBranchFolder.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.uce",
                UnreachableCodeEliminator.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.cp",
                CopyPropagator.v()));
        Scene.v().getPack("jtp").add(new Transform("jtp.dae",
        DeadAssignmentEliminator.v()));*/
       
	_callSootMain(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read in a MoML model, generate java files */
    public static void main(String[] args) throws Exception {
	// We do most of the work in the constructor so that we
	// can more easily test this class
	Main main = new Main(args);
    }
}

    











