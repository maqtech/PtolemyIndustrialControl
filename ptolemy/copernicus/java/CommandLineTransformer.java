/* A transformer that adds the command-line interface.

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

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Entity;

import soot.Body;
import soot.Hierarchy;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.NullType;
import soot.Options;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
A transformer that adds the command-line interface.
@author Michael Wirthlin, Stephen Neuendorffer, Edward A. Lee, Christopher Hylands
@version $Id$
*/
public class CommandLineTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private CommandLineTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static CommandLineTransformer v(CompositeActor model) {
        return new CommandLineTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " deep targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CommandLineTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        /* SootClass actorClass =  Scene.v().loadClassAndSupport(
                "ptolemy.actor.TypedAtomicActor");
        Type actorType = RefType.v(actorClass);
        SootClass compositeActorClass =
            Scene.v().loadClassAndSupport("ptolemy.actor.TypedCompositeActor");
        */
        // SootClass applicationClass = Scene.v().loadClassAndSupport(
        //        "ptolemy.actor.gui.CompositeActorApplication");
        SootClass applicationClass = Scene.v().loadClassAndSupport(
                "ptolemy.copernicus.java.CommandLineTemplate");
        applicationClass.setLibraryClass();

        SootClass modelClass = Scene.v().getMainClass();

        SootClass mainClass = SootUtilities.copyClass(applicationClass,
                Options.getString(options, "targetPackage") + ".Main");
        mainClass.setApplicationClass();
            
        Scene.v().setActiveHierarchy(new Hierarchy());

        // Optimizations.
        // We know that we will never parse classes, so throw away that code.
        SootUtilities.assertFinalField(mainClass, 
                mainClass.getFieldByName("_expectingClass"), 
                IntConstant.v(0));

        // We know that we will never be testing, so throw away that code.
        SootUtilities.assertFinalField(mainClass, 
                mainClass.getFieldByName("_test"), 
                IntConstant.v(0));

        // We know that we have exactly one model, so create it.
        // The final field for the model.
        SootField modelField =
            new SootField("_CGmodel", RefType.v(PtolemyUtilities.compositeActorClass),
                    Modifier.PRIVATE | Modifier.FINAL);
        mainClass.addField(modelField);


        // initialize the field by creating a model 
        // in all the <init> methods.
        for(Iterator methods = mainClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            // ignore things that aren't initializers.
            if(!method.getName().equals("<init>"))
                continue;

            System.out.println("method = " + method);
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();
            Chain units = body.getUnits();
            Stmt insertPoint = (Stmt)units.getLast();
            Local modelLocal = Jimple.v().newLocal("_CGTemp" +
                    modelField.getName(), modelField.getType());

            body.getLocals().add(modelLocal);
            units.insertBefore(Jimple.v().newAssignStmt(modelLocal,
                    Jimple.v().newNewExpr(RefType.v(modelClass))),
                    insertPoint);

            // the arguments
            List args = new LinkedList();
            SootMethod constructor =
                SootUtilities.getMatchingMethod(modelClass, "<init>", args);
            units.insertBefore(Jimple.v().newInvokeStmt(
                    Jimple.v().newSpecialInvokeExpr(modelLocal,
                            constructor, args)), insertPoint);

            FieldRef fieldRef =
                Jimple.v().newInstanceFieldRef(body.getThisLocal(),
                        modelField);
            units.insertBefore(Jimple.v().newAssignStmt(fieldRef, modelLocal),
                    insertPoint);
        }

        // Find calls to Manager.startRun() and replace it with 
        // iteration code.
        // Note: It would be nice if we could inline the manager 
        // code and optimize it, but in this case, the amount of code
        // we would want to throw away is fairly large.  This
        // just seems simpler here.
        SootClass managerClass =
            Scene.v().getSootClass("ptolemy.actor.Manager");
        SootMethod managerStartRunMethod =
            managerClass.getMethodByName("startRun");
        for(Iterator methods = mainClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            JimpleBody body = (JimpleBody)method.retrieveActiveBody();

            for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Unit unit = (Unit)units.next();
                for(Iterator boxes = unit.getUseBoxes().iterator();
                    boxes.hasNext();) {
                    ValueBox box = (ValueBox)boxes.next();
                    if(box.getValue() instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr expr = 
                            (InstanceInvokeExpr)box.getValue();
                        if(expr.getMethod().equals(managerStartRunMethod)) {
                            // Replace the start run method call
                            // with code to iterate the model.
                            // First create a local that refers to the model.
                            // FIXME This is redundant, since the local
                            // already exists somewhere...
                            Local modelLocal = Jimple.v().newLocal(
                                    "_CGTemp" +
                                    modelField.getName(), 
                                    modelField.getType());
                            
                            body.getLocals().add(modelLocal);
                            body.getUnits().insertBefore(
                                    Jimple.v().newAssignStmt(
                                            modelLocal,
                                            Jimple.v().newInstanceFieldRef(
                                                    body.getThisLocal(),
                                                    modelField)),
                                    unit);

                            _insertIterateCalls(body, 
                                    unit, 
                                    modelClass,
                                    modelLocal);
                            body.getUnits().remove(unit);
                        }
                    }
                }
            }
        }

        // unroll places where the list of models is used.
        LinkedList modelList = new LinkedList();
        modelList.add(modelField);
        SootField modelsField = mainClass.getFieldByName("_models");
        SootUtilities.unrollIteratorInstances(mainClass,
                modelsField, modelList);
        
        // inline calls to the startRun and stopRun method.
        SootMethod startRunMethod = mainClass.getMethodByName("startRun");
        SootUtilities.inlineCallsToMethod(startRunMethod, mainClass);
        mainClass.removeMethod(startRunMethod);
        SootUtilities.inlineCallsToMethod(
                mainClass.getMethodByName("stopRun"), mainClass);

        
        // unroll places where the model itself is looked at.
        // SootField modelsField = mainClass.getFieldByName("_models");
        // SootUtilities.unrollIteratorInstances(mainClass,
        //        modelsField, modelList);      
        
        // Take the instance of main, and convert it to be a static class.
        /*
          // FIXME this is currently broken.  
        {
            // First find the constructor statement.
            SootMethod mainMethod = mainClass.getMethodByName("main");
            JimpleBody body = (JimpleBody)mainMethod.retrieveActiveBody();
            Chain units = body.getUnits();
            for(Iterator stmts = units.iterator(); stmts.hasNext();) {
                Stmt stmt = (Stmt)stmts.next();
                // filter out anything that is not a definition.
                if(!(stmt instanceof DefinitionStmt)) {
                    continue;
                }
                DefinitionStmt newStmt = (DefinitionStmt)stmt;
                Value value = (newStmt).getRightOp();
                if(!(value instanceof NewExpr)) {
                    continue;
                }
                RefType type = ((NewExpr)value).getBaseType();
                if(type.getSootClass() != mainClass) {
                    continue;
                }
                InvokeStmt constructorStmt = null;
                // Now walk forward and find the constructor.
                while(stmts.hasNext()) {
                    stmt = (Stmt)stmts.next();
                    if(stmt instanceof InvokeStmt &&
                            ((InvokeStmt)stmt).getInvokeExpr()
                            instanceof SpecialInvokeExpr) {
                        constructorStmt = (InvokeStmt)stmt;
                    }
                    break;                            
                }

                // Now we actually have a creation of the main object,
                // so create a class just for that instance.
                SootClass staticMainClass = 
                    SootUtilities.createStaticClassForInstance(
                            mainClass, body, newStmt, constructorStmt, 
                            Options.getString(options, "targetPackage")
                            + ".StaticMain");
                
                // Remove the extra Main method that we created in
                // doing this.
                SootMethod staticMainMethod = 
                    staticMainClass.getMethodByName("main");
                staticMainClass.removeMethod(staticMainMethod);

                break;
            }
        }
        */
        for(Iterator methods = mainClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            System.out.println("method = " + method.toString());
            SootMethod method2 = Scene.v().getMethod(method.toString());
        }

    }

    private String _getFinalName(String dottedName) {
        // Take the entity and it's class name and munge them into a
        // unique name for the generated class
        StringTokenizer tokenizer = new StringTokenizer(dottedName, ".");
        String endName = "error";
        while(tokenizer.hasMoreElements()) {
            endName = tokenizer.nextToken();
        }
        return endName;
    }

    /** Insert into the given body before the given unit, calls to
     *  iterate the model that is referred to by the given local
     *  variable of the body that refers to an object of the given
     *  class.
     */
    private void _insertIterateCalls(Body body, Unit unit,
            SootClass modelClass, Local modelLocal) {
        Chain units = body.getUnits();
        
        //FIXME make parameter.
        int iterationLimit = 50;
        
        Local iterationLocal = null;
        if(iterationLimit > 1) {
            iterationLocal = Jimple.v().newLocal("iteration",
                    IntType.v());
            body.getLocals().add(iterationLocal);
            units.insertBefore(
                    Jimple.v().newAssignStmt(iterationLocal,
                            IntConstant.v(0)),
                    unit);
        }

        // call preinitialize
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "preinitialize"))),
                unit);
        
        // call initialize on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "initialize"))),
                unit);
        
        // A jump point for the start of the iteration.
        Stmt iterationStartStmt = Jimple.v().newNopStmt();
        // A jump point for the end of the iteration.
        // we don't actually insertBefore this until later in the sequence.
        Stmt iterationEndStmt = Jimple.v().newNopStmt();
        
        units.insertBefore(iterationStartStmt,
                unit);
        
        // call fire on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "fire"))),
                unit);
        
        // If we need to keep track of the number of iterations, then...
        if(iterationLimit > 1) {
            // Increment the number of iterations.
            units.insertBefore(Jimple.v().newAssignStmt(iterationLocal,
                    Jimple.v().newAddExpr(iterationLocal,
                            IntConstant.v(1))),
                    unit);
            // If the number of iterations is greater than the limit,
            // then we're done.
            units.insertBefore(Jimple.v().newIfStmt(
                    Jimple.v().newGtExpr(iterationLocal,
                            IntConstant.v(iterationLimit)),
                    iterationEndStmt),
                    unit);
        }
        if(iterationLimit != 1) {
            units.insertBefore(Jimple.v().newGotoStmt(iterationStartStmt),
                    unit);
        }
        
        // insertBefore the jump point for the end of the iteration
        units.insertBefore(iterationEndStmt,
                unit);
        
        // call wrapup on the model
        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newVirtualInvokeExpr(modelLocal,
                        SootUtilities.searchForMethodByName(modelClass,
                                "wrapup"))),
                unit);
    }

    private CompositeActor _model;
}














