/* A transformer that removes unnecessary fields from classes.

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

package ptolemy.copernicus.jhdl;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.invoke.VTATypeGraph;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.TypeLattice;

import ptolemy.data.expr.Variable;
import ptolemy.graph.*;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.MustAliasAnalysis;
import ptolemy.copernicus.java.ActorTransformer;


/**

*/
public class CircuitTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private CircuitTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static CircuitTransformer v(CompositeActor model) { 
        return new CircuitTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CircuitTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        // Loop over all the actor instance classes.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass = 
                        Scene.v().loadClassAndSupport(className);

            CircuitAnalysis analysis = new CircuitAnalysis(entityClass);
            HashMutableDirectedGraph operatorGraph =
                analysis.getOperatorGraph();
            // Write as a circuit.

        }
    }

    private CompositeActor _model;

}












