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
//// ExceptionEliminator
/**
Replace instances of Ptolemy exceptions with instances of plain old
RuntimeException.  This transformation is primarily useful from a memory 
standpoint, as it prevents the ptolemy kernel from being required in the
generated code.

@author Stephen Neuendorffer
@version $Id$
*/
public class ExceptionEliminator extends SceneTransformer {
    /** Construct a new transformer
     */
    private ExceptionEliminator(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static ExceptionEliminator v(CompositeActor model) { 
        return new ExceptionEliminator(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("ExceptionEliminator.internalTransform("
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
                        
                        _replaceExceptions(box);
                    }
                }
            }
        }
    }

    private boolean _isPtolemyException(SootClass exceptionClass) {
       if(SootUtilities.derivesFrom(
                  exceptionClass,
                  PtolemyUtilities.kernelExceptionClass)) {
           return true;
       }
       if(SootUtilities.derivesFrom(
                  exceptionClass,
                  PtolemyUtilities.kernelRuntimeExceptionClass)) {
           return true;
       } 
       return false;
    }
    // Replace any Ptolemy exception constructor
    // or initializer with a plain old RuntimeException.
    private void _replaceExceptions(ValueBox box) {
        // FIXME: This is currently way too simple.
        Value value = box.getValue();
        Type type = value.getType();
        // Fix kernel exceptions to be runtime exceptions.
        if(value instanceof NewExpr) {
            NewExpr expr = (NewExpr)value;
            SootClass exceptionClass = 
                expr.getBaseType().getSootClass();
            if(_isPtolemyException(exceptionClass)) {
                expr.setBaseType(
                        RefType.v(PtolemyUtilities.runtimeExceptionClass));
                
            }
        }
        // Fix the exception constructors.
        if(value instanceof SpecialInvokeExpr) {
            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
            SootClass exceptionClass = 
                ((RefType)expr.getBase().getType())
                .getSootClass();
            if(_isPtolemyException(exceptionClass)) {
                box.setValue(Jimple.v().newSpecialInvokeExpr(
                                     (Local)expr.getBase(),
                                     PtolemyUtilities.runtimeExceptionClass.getMethod("void <init>()"),
                                     new LinkedList()));
            }
        }
    }

    private CompositeActor _model;
}














