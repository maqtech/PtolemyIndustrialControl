/* An analysis for propagating token types

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

package ptolemy.copernicus.java;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeLattice;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.FastForwardFlowAnalysis;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// TokenTypeAnalysis
/**
An analysis that maps each local variable that represents a token onto
the particular type of the token.  This propagates the type
information from ports and parameter using standard dataflow
techniques through all of the java code for a particular method.
The result is used by transformers, such as TokenInstanceofEliminator.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class TokenTypeAnalysis extends FastForwardFlowAnalysis {
    public TokenTypeAnalysis(SootMethod method, CompleteUnitGraph g) {
        super(g);
        _method = method;
        _localDefs = new SimpleLocalDefs(g);
        _localUses = new SimpleLocalUses(g, _localDefs);
        doAnalysis();
        // Ensure that the analysis can get collected.
        _localDefs = null;
        _localUses = null;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.  If no information is available about the local,
     *  then return ptolemy.data.type.BaseType.GENERAL
     */
    public ptolemy.data.type.Type getTypeOfAfter(Local local, Unit unit) {
        Map map = (Map)getFlowAfter(unit);
        Object object = map.get(local);
        if (object == null) {
            return ptolemy.data.type.BaseType.GENERAL;
        }
        return (ptolemy.data.type.Type)object;
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.   If no information is available about the local,
     *  then return ptolemy.data.type.BaseType.GENERAL
     */
    public ptolemy.data.type.Type getTypeOfBefore(Local local, Unit unit) {
        Map map = (Map)getFlowBefore(unit);
        Object object = map.get(local);
        if (object == null) {
            return ptolemy.data.type.BaseType.GENERAL;
        }
        return (ptolemy.data.type.Type)object;
    }

    /** Inline the given invocation point in the given box, unit, and method.
     *  Use the given type analysis and local definition information to
     *  perform the inlining.
     */
    public void inlineTypeLatticeMethods(SootMethod method,
            Unit unit, ValueBox box, StaticInvokeExpr expr,
            LocalDefs localDefs) {
        SootMethod tokenTokenCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.Token,ptolemy.data.Token)");
        SootMethod tokenTypeCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.Token,ptolemy.data.type.Type)");
        SootMethod typeTokenCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.type.Type,ptolemy.data.Token)");
        SootMethod typeTypeCompareMethod =
            PtolemyUtilities.typeLatticeClass.getMethod(
                    "int compare(ptolemy.data.type.Type,ptolemy.data.type.Type)");

        ptolemy.data.type.Type type1;
        ptolemy.data.type.Type type2;
        if (expr.getMethod().equals(tokenTokenCompareMethod)) {
            Local tokenLocal1 = (Local)expr.getArg(0);
            Local tokenLocal2 = (Local)expr.getArg(1);
            type1 = getTypeOfBefore(tokenLocal1, unit);
            type2 = getTypeOfBefore(tokenLocal2, unit);
        } else if (expr.getMethod().equals(typeTokenCompareMethod)) {
            Local typeLocal = (Local)expr.getArg(0);
            Local tokenLocal = (Local)expr.getArg(1);
            type1 = PtolemyUtilities.getTypeValue(
                    method, typeLocal, unit, localDefs);
            type2 = getTypeOfBefore(tokenLocal, unit);
        } else if (expr.getMethod().equals(tokenTypeCompareMethod)) {
            Local tokenLocal = (Local)expr.getArg(0);
            Local typeLocal = (Local)expr.getArg(1);
            type1 = getTypeOfBefore(tokenLocal, unit);
            type2 = PtolemyUtilities.getTypeValue(
                    method, typeLocal, unit, localDefs);
        } else if (expr.getMethod().equals(typeTypeCompareMethod)) {
            Local typeLocal1 = (Local)expr.getArg(0);
            Local typeLocal2 = (Local)expr.getArg(1);
            type1 = PtolemyUtilities.getTypeValue(
                    method, typeLocal1, unit, localDefs);
            type2 = PtolemyUtilities.getTypeValue(
                    method, typeLocal2, unit, localDefs);
        } else {
            throw new RuntimeException(
                    "attempt to inline unhandled typeLattice method: " + unit);
        }
        box.setValue(IntConstant.v(TypeLattice.compare(type1, type2)));
    }

    // Formulation:
    protected Object newInitialFlow() {
        return new HashMap();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue) {
        Map in = (Map) inValue, out = (Map) outValue;
        Stmt stmt = (Stmt)d;

        // System.out.println("flowing " + d + " " + in);

        // By default, the out is equal to the in.
        copy(inValue, outValue);

        if (stmt instanceof AssignStmt) {
            Value leftOp = ((AssignStmt)stmt).getLeftOp();
            if (!_isTokenType(leftOp.getType())) return;
            //  System.out.println("from " + in);

            Value rightOp = ((AssignStmt)stmt).getRightOp();

            if (rightOp instanceof StaticInvokeExpr) {
                StaticInvokeExpr r = (StaticInvokeExpr)rightOp;
                if (r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                    out.put(r.getArg(0), in.get(r.getArg(2)));
                }
            } else if (rightOp instanceof InstanceInvokeExpr) {
                InstanceInvokeExpr r = (InstanceInvokeExpr)rightOp;
                String methodName = r.getMethod().getName();
                //   System.out.println("invokeExpr = " + r);
                SootClass baseClass = ((RefType)r.getBase().getType()).getSootClass();
                // FIXME: match better.
                // If we are invoking a method on a token, then...
                if (SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.tokenClass)) {
                    if (methodName.equals("one") ||
                            methodName.equals("zero")) {
                        // The returned type must be equal to the type
                        // we are calling the method on.
                        out.put(leftOp, in.get(r.getBase()));
                    } else if (methodName.equals("add") ||
                            methodName.equals("addReverse") ||
                            methodName.equals("subtract") ||
                            methodName.equals("subtractReverse") ||
                            methodName.equals("multiply") ||
                            methodName.equals("multiplyReverse") ||
                            methodName.equals("divide") ||
                            methodName.equals("divideReverse") ||
                            methodName.equals("modulo") ||
                            methodName.equals("moduloReverse")) {
//                         System.out.println("methodName = " + methodName);
//                         System.out.println("r.getBase() = " + r.getBase());
//                         System.out.println("r.getArg(0) = " + r.getArg(0));
//                         System.out.println("type(r.getBase()) = " + in.get(r.getBase()));
//                         System.out.println("type(r.getArg(0)) = " + in.get(r.getArg(0)));
                        ptolemy.data.type.Type baseType = (ptolemy.data.type.Type) in.get(r.getBase());
                        ptolemy.data.type.Type argType = (ptolemy.data.type.Type) in.get(r.getArg(0));
                        if(baseType == null || argType == null) {
                            out.put(leftOp, null);
                        } else {
                            out.put(leftOp, TypeLattice.lattice().leastUpperBound(baseType, argType));
                        }
                    } else if (methodName.equals("convert")) {
                        // The return rightOp type is equal to the base type.
                        // The first argument type is less than or equal to the base type.
                        out.put(leftOp, in.get(r.getBase()));
                    } else if (methodName.equals("getElement") ||
                            methodName.equals("arrayValue")) {
                        ptolemy.data.type.Type arrayType =
                            (ptolemy.data.type.Type)in.get(r.getBase());
                        if (arrayType != null &&
                                arrayType instanceof ArrayType) {
                            out.put(leftOp,
                                    ((ArrayType)arrayType).getElementType());
                        }
                    }
                } else if (SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.portClass)) {
                    // If we are invoking a method on a port.
                    TypedIOPort port = (TypedIOPort)
                        InlinePortTransformer.getPortValue(
                                _method,
                                (Local)r.getBase(),
                                stmt,
                                _localDefs,
                                _localUses);
                    //System.out.println("port for " + r.getBase() + " = " + port);
                    if (methodName.equals("broadcast")) {
                        // The type of the argument must be less than the
                        // type of the port.

                    } else if (methodName.equals("get")) {
                        // The port here may be null if the model does not
                        // actually contain the port...  This happens, for
                        // instance, in MathFunction
                        if (port != null) {
                            out.put(leftOp, port.getType());
                        }
                    } else if (methodName.equals("send")) {
                        if (r.getArgCount() == 3) {
                            // The type of the argument must be less than the
                            // type of the port.

                            //r.getArg(1));
                        } else if (r.getArgCount() == 2) {
                            // The type of the argument must be less than the
                            // type of the port.
                            //            r.getArg(1));
                        }
                    }
                } else if (SootUtilities.derivesFrom(baseClass,
                        PtolemyUtilities.attributeClass)) {
                    // If we are invoking a method on a parameter.
                    Attribute attribute = (Attribute)
                        InlineParameterTransformer.getAttributeValue(
                                _method,
                                (Local)r.getBase(),
                                stmt,
                                _localDefs,
                                _localUses);
                    if (attribute == null) {
                        // A method invocation with a null base is bogus,
                        // so don't create a type constraint.
                    }
                    if (attribute instanceof Variable) {
                        Variable parameter = (Variable)attribute;
                        if (methodName.equals("setToken")) {
                            // The type of the argument must be less than the
                            // type of the parameter.
                            // r.getArg(0));

                        } else if (methodName.equals("getToken")) {
                            // Return the type of the parameter.
                            out.put(leftOp, parameter.getType());
                        }
                    }
                }
            } else if (rightOp instanceof ArrayRef) {
               //  System.out.println("arrayRef stmt = " + stmt);
//                 System.out.println("right type = " + in.get(((ArrayRef)rightOp).getBase()));
                out.put(leftOp, in.get(((ArrayRef)rightOp).getBase()));
            } else if (rightOp instanceof CastExpr) {
                CastExpr castExpr = (CastExpr)rightOp;
                Type type = castExpr.getType();
                // RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
                //if (tokenType != null) {
                out.put(leftOp, in.get(castExpr.getOp()));
                // } else {
                // Otherwise there is nothing to be done.
                //}
            } else if (rightOp instanceof NewExpr) {
                NewExpr newExpr = (NewExpr)rightOp;
                RefType type = newExpr.getBaseType();
                SootClass castClass = type.getSootClass();
                // If we are creating a Token type...
                if (SootUtilities.derivesFrom(castClass,
                        PtolemyUtilities.tokenClass)) {
                    // Then the rightOp of the expression is the type of the
                    // constructor.
                    out.put(leftOp,
                            PtolemyUtilities.getTokenTypeForSootType(type));
                } else {
                    // Otherwise there is nothing to be done.
                }
            } else if (rightOp instanceof NewArrayExpr) {
                // Since arrays are aliasable, we must update their types.
                NewArrayExpr newExpr = (NewArrayExpr)rightOp;
                Type type = newExpr.getBaseType();
                RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
                if (tokenType != null) {
                    out.put(leftOp,
                            PtolemyUtilities.getTokenTypeForSootType(tokenType));
                }
                // Otherwise there is nothing to be done.
            } else if(rightOp instanceof FieldRef) {
                // System.out.println("fieldRef stmt = " + stmt);
                FieldRef fieldRef = (FieldRef)rightOp;
                SootField field = fieldRef.getField();
                TypeTag tag = (TypeTag)field.getTag("_CGType");
                Object newType;
                if(tag == null) {
              //       System.out.println("No Tag... Existing type = " + in.get(rightOp));
//                     System.out.println("No Tag... field type = " + field.getType());
                    
                    if(in.get(rightOp) == null) {
                        RefType fieldType = PtolemyUtilities.getBaseTokenType(field.getType());
                        newType = PtolemyUtilities.getTokenTypeForSootType(
                                fieldType);
                    } else {
                        // Then flow the type.
                        newType = in.get(rightOp);
                    }
                } else {
                    // The type is fixed.
                    newType = tag.getType();
                }
                //     System.out.println("newType = " + newType);
                out.put(leftOp, newType);
            }
            //  System.out.println("type of " + leftOp + " set to " + out.get(leftOp));
        }
        //   System.out.println("equals " + in + " == " + out + " = " + in.equals(out));
    }

    private boolean _isTokenType(Type type) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        return tokenType != null;
    }

    protected void copy(Object inValue, Object outValue) {
        Map in = (Map) inValue, out = (Map) outValue;
        out.clear();
        for (Iterator i = in.keySet().iterator(); i.hasNext();) {
            Object object = i.next();
            out.put(object, in.get(object));
        }
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        Map in1 = (Map) in1Value, in2 = (Map) in2Value, out = (Map) outValue;

        // System.out.println("merging " + in1 + " and " + in2 + " into " + out);

        Set allKeys = new HashSet();
        allKeys.addAll(in1.keySet());
        allKeys.addAll(in2.keySet());
        for (Iterator i = allKeys.iterator(); i.hasNext();) {
            Object object = i.next();
            ptolemy.data.type.Type in1Type =
                (ptolemy.data.type.Type)in1.get(object);
            ptolemy.data.type.Type in2Type =
                (ptolemy.data.type.Type)in2.get(object);
            if (in1Type == null) {
                in1Type = BaseType.UNKNOWN;
            }
            if (in2Type == null) {
                in2Type = BaseType.UNKNOWN;
            }
            if (in1Type.equals(in2Type)) {
                out.put(object, in1Type);
            } else {
                out.put(object, TypeLattice.lattice().leastUpperBound(in1Type,
                        in2Type));
            }
        }
        //      System.out.println("result = " + out);

    }

    private SootMethod _method;
    private SimpleLocalDefs _localDefs;
    private SimpleLocalUses _localUses;
}
