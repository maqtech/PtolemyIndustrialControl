/* Replace method calls on parameter objects.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
import soot.jimple.toolkits.scalar.LocalNameStandardizer;

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
//// NamedObjEqualityEliminator
/**


@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class NamedObjEqualityEliminator extends SceneTransformer {
    /** Construct a new transformer
     */
    private NamedObjEqualityEliminator(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static NamedObjEqualityEliminator v(CompositeActor model) {
        return new NamedObjEqualityEliminator(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " debug";
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("NamedObjEqualityEliminator.internalTransform("
                + phaseName + ", " + options + ")");

        _options = options;
        _debug = Options.getBoolean(options, "debug");

        _eliminateAllComparisons(_model);

    }

    private void _eliminateAllComparisons(CompositeActor model) {
        // Loop over all the actor instance classes.
        for (Iterator entities = model.deepEntityList().iterator();
             entities.hasNext();) {
            Entity entity = (Entity)entities.next();
            String className =
                ModelTransformer.getInstanceClassName(entity, _options);
            SootClass entityClass =
                Scene.v().loadClassAndSupport(className);

            for (Iterator methods = entityClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                _eliminateComparisons(entityClass, method, entity);
            }

            // Recurse
            if (entity instanceof CompositeActor) {
                _eliminateAllComparisons((CompositeActor)entity);
            }
        }
    }

    private boolean _eliminateComparisons(SootClass theClass,
            SootMethod method, Entity entity) {
        boolean doneSomething = false;
        if (_debug) System.out.println("Removing object comparisons in " +
                method);

        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

        for (Iterator units = body.getUnits().snapshotIterator();
             units.hasNext();) {
            Stmt stmt = (Stmt)units.next();
            for (Iterator boxes = stmt.getUseBoxes().iterator();
                 boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();

                if (value instanceof BinopExpr) {
                    BinopExpr binop = (BinopExpr)value;
                    Value left = binop.getOp1();
                    Value right = binop.getOp2();
                    // handle nulls
                    if (left.getType() instanceof NullType &&
                            right.getType() instanceof NullType) {
                        binop.getOp1Box().setValue(
                                IntConstant.v(0));
                        binop.getOp2Box().setValue(
                                IntConstant.v(0));
                    } else if (left.getType() instanceof RefType &&
                            right.getType() instanceof RefType) {
                        RefType leftType = (RefType)left.getType();
                        RefType rightType = (RefType)right.getType();
                        SootClass leftClass = leftType.getSootClass();
                        SootClass rightClass = rightType.getSootClass();
                        if (SootUtilities.derivesFrom(leftClass,
                                PtolemyUtilities.namedObjClass) &&
                                SootUtilities.derivesFrom(rightClass,
                                        PtolemyUtilities.namedObjClass)) {
                            try {
                                NamedObj leftObject =
                                    getNamedObjValue(method, (Local)left,
                                            stmt, localDefs, localUses);
                                NamedObj rightObject =
                                    getNamedObjValue(method, (Local)right,
                                            stmt, localDefs, localUses);
                                System.out.println("leftObject = "
                                        + leftObject);
                                System.out.println("rightObject = "
                                        + rightObject);

                                if (leftObject == rightObject) {
                                    binop.getOp1Box().setValue(
                                            IntConstant.v(0));
                                    binop.getOp2Box().setValue(
                                            IntConstant.v(0));
                                } else {
                                    binop.getOp1Box().setValue(
                                            IntConstant.v(0));
                                    binop.getOp2Box().setValue(
                                            IntConstant.v(1));
                                }
                            } catch (Exception ex) {
                                // Ignore... We cannot determine the
                                // value of the object.
                            }
                        }
                    }
                }
            }
        }

        return doneSomething;
    }

    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a named object type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise throw an exception
     */
    public static NamedObj getNamedObjValue(SootMethod method, Local local,
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof Local) {
                return getNamedObjValue(method,
                        (Local)value,
                        stmt, localDefs, localUses);
            } else if (value instanceof CastExpr) {
                return getNamedObjValue(method,
                        (Local)((CastExpr)value).getOp(),
                        stmt, localDefs, localUses);
            } else if (value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if (tag == null) {
                    // return null;
                    throw new RuntimeException(
                            "Could not determine the static value of "
                            + local + " in " + method);
                } else {
                    return (NamedObj)tag.getObject();
                }
            } else if (value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while (pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if (pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt =
                            (DefinitionStmt)pair.getUnit();
                        if (useStmt.getLeftOp() instanceof FieldRef) {
                            SootField field =
                                ((FieldRef)useStmt.getLeftOp()).getField();
                            ValueTag tag = (ValueTag)field.getTag("_CGValue");
                            if (tag == null) {
                                System.out.println("Failed usage: " +
                                        useStmt);
                            } else {
                                return (NamedObj)tag.getObject();
                            }
                        }
                    }
                }
                throw new RuntimeException("Could not determine the " +
                        " static value of" + local + " in " + method);
            } else if (value instanceof NullConstant) {
                // If we get to an assignment from null, then the
                // attribute statically evaluates to null.
                return null;
            } else {
                throw new RuntimeException("Unknown type of value: "
                        + value + " in " + method);
            }
        } else {
            String string = "More than one definition of = " + local + "\n";
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                string += "Definition = " + i.next().toString();
            }
            throw new RuntimeException(string);
        }
    }

    private Map _options;
    private boolean _debug;
    private CompositeActor _model;
}














