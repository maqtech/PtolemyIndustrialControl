/* A transformer that removes dead token and type creations.

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
import ptolemy.data.type.Typeable;
import ptolemy.data.expr.Variable;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;


/**

*/
public class DeadObjectEliminator extends BodyTransformer {
    /** Construct a new transformer
     */
    private DeadObjectEliminator() {}

    /* Return the instance of this transformer.
     */
    public static DeadObjectEliminator v() { 
        return instance; 
    }
    
    public String getDefaultOptions() {
        return ""; 
    }
    
    public String getDeclaredOptions() { 
        return super.getDeclaredOptions(); 
    }

    protected void internalTransform(Body body, String phaseName, Map options) {
        _removeDeadObjectCreation(body, PtolemyUtilities.tokenClass);
        _removeDeadObjectCreation(body, PtolemyUtilities.typeClass);
        _removeDeadObjectCreation(body, PtolemyUtilities.attributeClass);
    }
        // Lastly go back and look for any constructors of attributes.
        // Remove them and all uses of those objects.  If there is anything left,
        // Then we should deal with it above.
        /*
          for(Iterator units = body.getUnits().snapshotIterator();
          units.hasNext();) {
          Unit unit = (Unit)units.next();
          Iterator boxes = unit.getUseBoxes().iterator();
          while(boxes.hasNext()) {
          ValueBox box = (ValueBox)boxes.next();
          Value value = box.getValue();
          if(value instanceof NewExpr) {
          SootClass newClass = ((RefType)((NewExpr)value).getType()).getSootClass();
          if(SootUtilities.derivesFrom(newClass, 
          PtolemyUtilities.attributeClass)) {
          if(unit instanceof DefinitionStmt) {
          // If we are keeping a definition, then 
          // set the definition to be null.
          box.setValue(NullConstant.v());
          } else {
          // I can't imagine when this would
          // be true?
          body.getUnits().remove(unit);
          }
          }
          } else if(value instanceof SpecialInvokeExpr) {
          SootClass newClass = ((RefType)((SpecialInvokeExpr)value).getBase().getType()).getSootClass();
          if(SootUtilities.derivesFrom(newClass, 
          PtolemyUtilities.attributeClass)) {
          // and remove the constructor.
          body.getUnits().remove(unit);
          }
          }
          }
          }*/
    /** Remove any creations of objects of the
     *  given class, or subclasses that are
     *  not directly used in the given body.  Note 
     *  that this is not, technically a 
     *  safe thing to do, since object creation may
     *   have side effects that will not
     *  be seen.  We use this when we have knowledge of the given class that 
     *  side effects are not possible, or that the object is immutable.
     */
    public static void _removeDeadObjectCreation(
            Body body, SootClass theClass) {
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLiveLocals liveLocals = new SimpleLiveLocals(unitGraph);
        for(Iterator units = body.getUnits().snapshotIterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            Iterator boxes = unit.getUseBoxes().iterator();
            while(boxes.hasNext()) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
                if(value instanceof SpecialInvokeExpr) {
                    SpecialInvokeExpr r = (SpecialInvokeExpr)value;
                    if(SootUtilities.derivesFrom(
                            r.getMethod().getDeclaringClass(), theClass) &&
                            !liveLocals.getLiveLocalsAfter(unit).contains(
                                    r.getBase())) {
                        // Remove the initialization and the constructor.
                        // Note: This assumes a fairly tight coupling between
                        // the new and the object constructor.  This may
                        // not be true.
                        body.getUnits().remove(unit);
                        for(Iterator defs = localDefs.getDefsOfAt(
                                (Local)r.getBase(), unit).iterator();
                            defs.hasNext();) {
                            Unit defUnit = (Unit)defs.next();
                            if(defUnit instanceof DefinitionStmt) {
                                // If we are keeping a definition, then 
                                // set the definition to be null.
                                ((DefinitionStmt)defUnit).getRightOpBox().setValue(NullConstant.v());
                            } else {
                                // I can't imagine when this would
                                // be true?
                                body.getUnits().remove(defUnit);
                            }
                        }
                    }
                }
            }
        }
    }

    private static DeadObjectEliminator instance = new DeadObjectEliminator();
}














