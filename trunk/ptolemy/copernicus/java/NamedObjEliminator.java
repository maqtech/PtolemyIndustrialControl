/* Eliminate all references to named objects

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
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
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
import ptolemy.data.type.Typeable;

import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;


//////////////////////////////////////////////////////////////////////////
//// NamedObjEliminator
/**

@author Stephen Neuendorffer
@version $Id$
*/
public class NamedObjEliminator extends SceneTransformer {
    /** Construct a new transformer
     */
    private NamedObjEliminator(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static NamedObjEliminator v(CompositeActor model) { 
        return new NamedObjEliminator(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("NamedObjEliminator.internalTransform("
                + phaseName + ", " + options + ")");

        // Loop over all the classes
        for(Iterator i = Scene.v().getApplicationClasses().iterator(); 
            i.hasNext();) {
                     
            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for(Iterator methods = theClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for(Iterator units = body.getUnits().snapshotIterator();
                    units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    
                    // If any box is removable, then remove the statement.
                    for(Iterator boxes = unit.getUseAndDefBoxes().iterator();
                        boxes.hasNext();) {
                        ValueBox box = (ValueBox)boxes.next();
                        
                        Value value = box.getValue();
                        Type type = value.getType();
                        if(_isRemovableType(type)) {
                            //  System.out.println("removing " + unit);
                            body.getUnits().remove(unit);
                            break;
                        }
                        // Fix the constructor...
                        if(value instanceof SpecialInvokeExpr && 
                                !method.isStatic() &&
                                SootUtilities.derivesFrom(theClass,
                                        PtolemyUtilities.actorClass)) {
                            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                            if(expr.getBase().equals(body.getThisLocal())) {
                                System.out.println("replacing unit = " + unit
                                        + " in method " + method);
                                
                                // Replace with zero arg object constructor.
                                box.setValue(Jimple.v().newSpecialInvokeExpr(
                                                     (Local)expr.getBase(),
                                                     PtolemyUtilities.objectClass.getMethodByName("<init>"),
                                                     new LinkedList()));
                                break;
                            }
                        }
                        // Remove other remaining method invocations.
                        if(value instanceof InvokeExpr &&
                                SootUtilities.derivesFrom(theClass,
                                        PtolemyUtilities.actorClass)) {
                            InvokeExpr expr = (InvokeExpr)value;
                            SootClass methodClass =
                                expr.getMethod().getDeclaringClass();
                            if(SootUtilities.derivesFrom(methodClass, 
                                       PtolemyUtilities.namedObjClass)) {
                                System.out.println(
                                        "removing namedobj call = " + unit
                                        + " in method " + method);
                                body.getUnits().remove(unit);
                                break;
                            }
                        }
                    }
                    // If any locals are removable, then remove them.
                    for(Iterator locals = body.getLocals().snapshotIterator();
                        locals.hasNext();) {
                        Local local = (Local)locals.next();
                        Type type = local.getType();
                        if(_isRemovableType(type)) {
                            body.getLocals().remove(local);
                        }
                    }
                }
                // If any fields are removable, then remove them.
                for(Iterator fields = theClass.getFields().snapshotIterator();
                    fields.hasNext();) {
                    SootField field = (SootField)fields.next();
                    Type type = field.getType();
                    if(_isRemovableType(type)) {
                        theClass.getFields().remove(field);
                    }
                }
            }            
            if(SootUtilities.derivesFrom(theClass,
                       PtolemyUtilities.actorClass)) {
                System.out.println("changing superclass for " + theClass);
                theClass.setSuperclass(PtolemyUtilities.objectClass);
                
            }
        }
    }
    
    // Return true if the type is one that should not appear in generated
    // code.  This includes Attribute, Settable, Relation, Port, and their 
    // subclasses.
    private static boolean _isRemovableType(Type type) {
        if(type instanceof RefType) {
            RefType refType = (RefType)type;
            SootClass refClass = refType.getSootClass();
            if(SootUtilities.derivesFrom(refClass,
                    PtolemyUtilities.attributeClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.settableClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.relationClass) ||
                    SootUtilities.derivesFrom(refClass,
                            Scene.v().loadClassAndSupport(
                                    "ptolemy.kernel.Port"))) {
                return true;
            }
        }
        return false;
    }

    private CompositeActor _model;
}














