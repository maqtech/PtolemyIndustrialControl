/* A transformer that inlines references to tokens.

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

package ptolemy.copernicus.java;

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
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining the values of tokens.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class TokenToNativeTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private TokenToNativeTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static TokenToNativeTransformer v(CompositeActor model) { 
        return new TokenToNativeTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("TokenToNativeTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        SootClass stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");
        Type stringType = RefType.v(stringClass);
        SootClass objectClass = 
            Scene.v().loadClassAndSupport("java.lang.Object");
        SootMethod toStringMethod =
            objectClass.getMethod("java.lang.String toString()");
        SootClass namedObjClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.NamedObj");
        SootMethod getAttributeMethod = namedObjClass.getMethod(
                "ptolemy.kernel.util.Attribute getAttribute(java.lang.String)");
        SootMethod attributeChangedMethod = namedObjClass.getMethod(
                "void attributeChanged(ptolemy.kernel.util.Attribute)");

        SootClass attributeClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);
        SootClass settableClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
        SootMethod getExpressionMethod = 
            settableClass.getMethod("java.lang.String getExpression()");
        SootMethod setExpressionMethod = 
            settableClass.getMethod("void setExpression(java.lang.String)");
        
        SootClass tokenClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
        Type tokenType = RefType.v(tokenClass);
        SootClass parameterClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        SootMethod getTokenMethod = 
            parameterClass.getMethod("ptolemy.data.Token getToken()");
        SootMethod setTokenMethod = 
            parameterClass.getMethod("void setToken(ptolemy.data.Token)");

        // Loop over all the actor instance classes.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = Options.getString(options, "targetPackage")
                + "." + entity.getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
      
            System.out.println("class = " + entityClass);
            for(Iterator methods = entityClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                System.out.println("method = " + method);

                CompleteUnitGraph unitGraph = 
                    new CompleteUnitGraph(body);
                // this will help us figure out where locals are defined.
                SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
                SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
                Hierarchy hierarchy = Scene.v().getActiveHierarchy();
                
                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Unit unit = (Unit)units.next();
                    Iterator boxes = unit.getUseBoxes().iterator();
                    while(boxes.hasNext()) {
                        ValueBox box = (ValueBox)boxes.next();
                        Value value = box.getValue();
                        if(value instanceof InstanceInvokeExpr) {
                            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                            //       System.out.println("invoking = " + r.getMethod());
                         
                            if(r.getBase().getType() instanceof RefType) {
                                RefType type = (RefType)r.getBase().getType();

                                if(SootUtilities.derivesFrom(type.getSootClass(), tokenClass)) {
                                    List methodList = 
                                        hierarchy.resolveAbstractDispatch(
                                                type.getSootClass(), 
                                                r.getMethod());
                                    System.out.println("checking token method call = " + r);
                                    System.out.println("baseType = " + type.getSootClass());
                                    if(methodList.size() == 1) {
                                        System.out.println("inlining");
                                        type.getSootClass().setLibraryClass();
                                        // Then we know exactly what method will
                                        // be called, so inline it.
                                        SiteInliner.inlineSite(
                                                (SootMethod)methodList.get(0),
                                                (Stmt)unit, method);
                                    } else {
                                        
                                        for(Iterator targets = methodList.iterator();
                                            targets.hasNext();) {
                                            System.out.println("target = " + targets.next());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }            
        }
    }

    private CompositeActor _model;
}














