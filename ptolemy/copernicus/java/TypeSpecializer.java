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

package ptolemy.copernicus.java;

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
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.MustAliasAnalysis;


/**
A Transformer that is responsible for inlining the values of parameters.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class TypeSpecializer extends SceneTransformer {
    /** Construct a new transformer
     */
    private TypeSpecializer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static TypeSpecializer v(CompositeActor model) { 
        return new TypeSpecializer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep debug"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("TypeSpecializer.internalTransform("
                + phaseName + ", " + options + ")");
        if(!Options.getBoolean(options, "deep")) {
            return;
        }

        boolean debug = Options.getBoolean(options, "debug");

        Scene.v().setActiveHierarchy(new Hierarchy());
    
        Hierarchy h = Scene.v().getActiveHierarchy();
        for(Iterator classes = Scene.v().getApplicationClasses().iterator();
            classes.hasNext();) {
            SootClass theClass = (SootClass)classes.next();
            
            specializeTypes(debug, theClass, new HashSet());
        }
    }

    /** Specialize all token types that appear in the given class.
     *  Return a map from locals and fields in the class to their new specific
     *  Ptolemy type.  Exclude locals in the given set from the typing algorithm.
     */
    public static Map specializeTypes(boolean debug, SootClass theClass, Set unsafeLocals) {
        InequalitySolver solver = new InequalitySolver(TypeLattice.lattice());
        HashMap objectToInequalityTerm = new HashMap();
        
        _collectConstraints(debug, theClass, solver, objectToInequalityTerm, unsafeLocals);
        
        boolean succeeded = solver.solveLeast();
        
        if(debug) {
            System.out.println("Type Assignment:");
            Iterator variables = solver.variables();
            while (variables.hasNext()) {
                System.out.println("Inequality: " 
                        + variables.next().toString());
            }
        }
            
        if(succeeded) {
            if(debug) System.out.println("solution FOUND!");
            Map map = _updateTypes(debug, theClass, objectToInequalityTerm);
            return map;
        } else {
            System.out.println("Unsatisfied Inequalities:");
            Iterator inequalities = solver.unsatisfiedInequalities();
            while (inequalities.hasNext()) {
                System.out.println("Inequality: " 
                        + inequalities.next().toString());
            }
            throw new RuntimeException("NO Type solution found!");
        }
    }

    private static void _collectConstraints(boolean debug, 
            SootClass theClass, InequalitySolver solver, Map objectToInequalityTerm,
            Set unsafeLocals) {
        if(debug) System.out.println("collecting constraints for " + theClass);
        // Loop through all the fields.
        for(Iterator fields = theClass.getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            // Ignore things that aren't reference types.
            Type type = field.getType();
            _createInequalityTerm(debug, field, type, objectToInequalityTerm);

            // If the field has been tagged with a more specific type, then
            // constrain the type more.
            TypeTag tag = (TypeTag)field.getTag("_CGType");
            if(tag != null) {
                _addInequality(debug, solver, 
                        new ConstantTerm(tag.getType(), field),
                        (InequalityTerm)objectToInequalityTerm.get(field)); 
            }            
        }
        
        // FIXME: we also need the fields that we represent from
        //
        for(Iterator fields = Scene.v().getMainClass().getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            // Ignore things that aren't reference types.
            Type type = field.getType();
            _createInequalityTerm(debug, field, type, objectToInequalityTerm);

            // If the field has been tagged with a more specific type, then
            // constrain the type more.
            TypeTag tag = (TypeTag)field.getTag("_CGType");
            if(tag != null) {
                _addInequality(debug, solver, 
                        new ConstantTerm(tag.getType(), field),
                        (InequalityTerm)objectToInequalityTerm.get(field)); 
            }            
        }

        // Loop through all the methods.
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            Body body = method.retrieveActiveBody();
            if(debug) System.out.println("collecting constraints for " + method);
              
            CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
            // this will help us figure out where locals are defined.
            SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
            SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
            MustAliasAnalysis aliasAnalysis = new MustAliasAnalysis(unitGraph);

            for(Iterator locals = body.getLocals().iterator();
                locals.hasNext();) {
                Local local = (Local)locals.next();
                if(unsafeLocals.contains(local)) {
                    continue;
                }
                // Ignore things that aren't reference types.
                Type type = local.getType();
                _createInequalityTerm(debug, local, type, objectToInequalityTerm);
            }
            for(Iterator units = body.getUnits().iterator();
                units.hasNext();) {
                Stmt stmt = (Stmt)units.next();
                if(debug) System.out.println("stmt = " + stmt);
                if(stmt instanceof AssignStmt) {
                    Value leftOp = ((AssignStmt)stmt).getLeftOp();
                    Value rightOp = ((AssignStmt)stmt).getRightOp();
                    
                    // Note that the only real possibilities on the left side are
                    // a local or a fieldRef.
                    InequalityTerm leftOpTerm =  
                        _getInequalityTerm(method, debug, leftOp,
                                solver, objectToInequalityTerm, stmt, 
                                localDefs, localUses, aliasAnalysis);

                    InequalityTerm rightOpTerm = 
                        _getInequalityTerm(method, debug, rightOp,
                                solver, objectToInequalityTerm, stmt,
                                localDefs, localUses, aliasAnalysis);

                    // The type of all aliases of the left hand side
                    // must always be greater than
                    // the type of all aliases of the right hand side.  
                    _addInequality(debug, solver, rightOpTerm,
                            leftOpTerm); 
                  
                    // If an alias is created by this instruction, then the 
                    // left and right hand sides must actually be equal.
                    // FIXME: Alternatively, we could create individual constraints for
                    // all of the different aliases.  This might be better
                    // given that we actually have alias information.
                    if(SootUtilities.isAliasableValue(leftOp) && 
                            (SootUtilities.isAliasableValue(rightOp) ||
                                    rightOp instanceof NewArrayExpr)) {
                        _addInequality(debug, solver, leftOpTerm, rightOpTerm);
                    }
                } else if(stmt instanceof InvokeStmt) {
                    // Still call getInequalityTerm because there may be side effects
                    // that cause type constraints.
                    _getInequalityTerm(method, debug, 
                            ((InvokeStmt)stmt).getInvokeExpr(),
                            solver, objectToInequalityTerm, stmt, 
                            localDefs, localUses, aliasAnalysis);
                }
            }
        }
        
    }
 
    private static Map _updateTypes(boolean debug,
            SootClass theClass, Map objectToInequalityTerm) {
        if(debug) System.out.println("updateing types for " + theClass);
        Map map = new HashMap();

        // Loop through all the methods and update types of locals.
        // Note that unlike the types of fields, the types of locals
        // are not stored in the bytecode, hence we don't have to insert
        // casts to please the bytecode verifier.   On the other hand, 
        // this information will be lost once we actually write to bytecode.
        // We are updating it because further passes of code generation
        // use this information (for example, when converting 
        // token types (like IntToken) to native types (like int).
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            if(debug) System.out.println("updating types for " + method);
            Body body = method.retrieveActiveBody();
            for(Iterator locals = body.getLocals().iterator();
                locals.hasNext();) {
                Local local = (Local)locals.next();
                Type type = local.getType();
                // Things that aren't token types are ignored.
                Type newType = _getUpdateType(debug, local, type, objectToInequalityTerm);
                if(newType != null) {
                    if(debug) System.out.println("local = " + local);
                    local.setType(newType);
                    map.put(local, _getTokenType(objectToInequalityTerm, local));
                }
            }
            for(Iterator boxes = body.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                if(box.getValue() instanceof NewArrayExpr) {
                    NewArrayExpr newArrayExpr = (NewArrayExpr)box.getValue();
                    if(debug) System.out.println("newArrayExpr = " + newArrayExpr);
 
                    Type type = newArrayExpr.getBaseType();
                    // Things that aren't token types are ignored.
                    Type newType = _getUpdateType(debug, newArrayExpr, type, objectToInequalityTerm);
                    if(newType != null) {
                        if(debug) System.out.println("updating newArrayExpr = " + newArrayExpr);
 
                        newArrayExpr.setBaseType(newType);
                        map.put(newArrayExpr, _getTokenType(objectToInequalityTerm, newArrayExpr));
                    }
                }
            }
        }
        
        // Loop through all the methods and insert casts whenever we
        // have a field store that has changed type to please the bytecode
        // verifier.
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod method = (SootMethod)methods.next();
            Body body = method.retrieveActiveBody();
            for(Iterator units = body.getUnits().snapshotIterator();
                units.hasNext();) {
                Unit unit = (Unit) units.next();
                // Ignore anything that isn't an assignment.
                if(!(unit instanceof AssignStmt)) {
                    continue;
                }
                AssignStmt assignStmt = (AssignStmt)unit;
                // Ignore anything that isn't an assignment to a field.
                if(!(assignStmt.getLeftOp() instanceof FieldRef)) {
                    continue;
                }
                FieldRef ref = (FieldRef)((AssignStmt)assignStmt).getLeftOp(); 
                SootField field = ref.getField();
                Type type = field.getType();
                // Things that aren't token types are ignored.
                // Things that are already the same type are ignored.
                Type newType = _getUpdateType(debug, field, type, objectToInequalityTerm);
                if(newType != null && !newType.equals(type)) {
                    Local tempLocal = Jimple.v().newLocal("fieldUpdateLocal", newType);
                    body.getLocals().add(tempLocal);
                    body.getUnits().insertBefore(
                            Jimple.v().newAssignStmt(tempLocal,
                                    Jimple.v().newCastExpr(
                                            assignStmt.getRightOp(),
                                            newType)),
                            unit);
                    assignStmt.setRightOp(tempLocal);
                }
            }
        }

        // Loop through all the fields and update the types.
        for(Iterator fields = theClass.getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            if(debug) System.out.println("updating types for " + field);
            Type type = field.getType();
            // Things that aren't token types are ignored.
            Type newType = _getUpdateType(debug, field, type, objectToInequalityTerm);
            if(newType != null) {
                field.setType(newType);
                map.put(field, _getTokenType(objectToInequalityTerm, field));
            }
        }

        // FIXME: Loop through all the fields in the main class and update the types.
        for(Iterator fields = Scene.v().getMainClass().getFields().iterator();
            fields.hasNext();) {
            SootField field = (SootField)fields.next();
            if(debug) System.out.println("updating types for " + field);
            Type type = field.getType();
            // Things that aren't token types are ignored.
            Type newType = _getUpdateType(debug, field, type, objectToInequalityTerm);
            if(newType != null) {
                field.setType(newType);
                map.put(field, _getTokenType(objectToInequalityTerm, field));
            }
        }

        return map;
    }
           
    // Given an object (which must be either a local, or a field) of the given type, 
    // look into the given map and retrieve the inequality term for the object.
    // retreive the resolved type, and return it.
    private static Type _getUpdateType(boolean debug,
            Object object, Type type, Map objectToInequalityTerm) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if(tokenType != null) {
            if(debug) System.out.println("type of value " + object + " = " + type);
            InequalityTerm term = (InequalityTerm)objectToInequalityTerm.get(object);
            if(term == null) {
                return null;
            }
            ptolemy.data.type.Type newTokenType = (ptolemy.data.type.Type)term.getValue();
            RefType newType = PtolemyUtilities.getSootTypeForTokenType(newTokenType);
            if(debug) System.out.println("newType = " + newType);
            if(!SootUtilities.derivesFrom(newType.getSootClass(), tokenType.getSootClass())) {
                // If the new Type is less specific, in Java terms, than what we 
                // had before, then the resulting code is likely not correct.
                // FIXME: hack to get around the bogus type lattice.  This should be an exception.
                System.out.println("Warning! Resolved type of " + object + 
                        " to " + newType + " which is more general than the old type " + type);
                newType = tokenType;
            }
            
            // create a new type isomorphic with the old type.
            return SootUtilities.createIsomorphicType(type, newType);
        }
        // If this is not a token class, then we don't change it.
        return null;
    }  

    public static ptolemy.data.type.Type _getTokenType(Map objectToInequalityTerm, Object object) {
        InequalityTerm term = (InequalityTerm)objectToInequalityTerm.get(object);
        if(term == null) {
            throw new RuntimeException("Attempt to get type for object " + object + " with no inequality term!");
        }
        return (ptolemy.data.type.Type)term.getValue();
    }

    public static InequalityTerm _getInequalityTerm(
            SootMethod method, boolean debug,
            Value value, InequalitySolver solver, 
            Map objectToInequalityTerm,
            Unit unit, LocalDefs localDefs, LocalUses localUses,
            MustAliasAnalysis aliasAnalysis) {
        if(value instanceof StaticInvokeExpr) {
            StaticInvokeExpr r = (StaticInvokeExpr)value;
            if(r.getMethod().equals(PtolemyUtilities.arraycopyMethod)) {
                // If we are copying one array to another, then the
                // types must be equal.
                InequalityTerm firstArgTerm = (InequalityTerm)
                    objectToInequalityTerm.get(r.getArg(0));
                InequalityTerm thirdArgTerm = (InequalityTerm)
                    objectToInequalityTerm.get(r.getArg(2));
                _addInequality(debug, solver, firstArgTerm,
                        thirdArgTerm);
                _addInequality(debug, solver, thirdArgTerm,
                        firstArgTerm);
                return null;
            }
        } else if(value instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr r = (InstanceInvokeExpr)value;
            String methodName = r.getMethod().getName();
            //        System.out.println("invokeExpr = " + r);
            SootClass baseClass = ((RefType)r.getBase().getType()).getSootClass();
            InequalityTerm baseTerm =
                (InequalityTerm)objectToInequalityTerm.get(r.getBase());
            // FIXME: match better.
            // If we are invoking a method on a token, then...
            if(SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.tokenClass)) {
                if(methodName.equals("one") ||
                        methodName.equals("zero")) {
                    // The returned type must be equal to the type  
                    // we are calling the method on.
                    return baseTerm;
                } else if(methodName.equals("add") ||
                        methodName.equals("addReverse") ||
                        methodName.equals("subtract") ||
                        methodName.equals("subtractReverse") ||
                        methodName.equals("multiply") ||
                        methodName.equals("multiplyReverse") ||
                        methodName.equals("divide") ||
                        methodName.equals("divideReverse") ||
                        methodName.equals("modulo") ||
                        methodName.equals("moduloReverse")) {
                    // The return value is greater than the base and 
                    // the argument.
                    InequalityTerm returnValueTerm = 
                        new VariableTerm(
                                PtolemyUtilities.getTokenTypeForSootType(
                                        (RefType)r.getMethod().getReturnType()),
                                r.getMethod());
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));
                    _addInequality(debug, solver, firstArgTerm,
                            returnValueTerm);
                    _addInequality(debug, solver, baseTerm,
                            returnValueTerm);
                    return returnValueTerm;
                } else if(methodName.equals("convert")) {
                    // The return value type is equal to the base type.
                    // The first argument type is less than or equal to the base type.
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));
                    _addInequality(debug, solver, firstArgTerm,
                            baseTerm); 
                    return baseTerm;
                } else if(methodName.equals("getElement") ||
                          methodName.equals("arrayValue")) {
                    // If we call getElement or arrayValue on an array token, then 
                    // the returned type is the element type of the array.
                    ptolemy.data.type.ArrayType arrayType =
                        new ptolemy.data.type.ArrayType(
                                ptolemy.data.type.BaseType.UNKNOWN);
                    _addInequality(debug, solver, baseTerm, 
                            new VariableTerm(arrayType, r) 
                            );
                    InequalityTerm returnTypeTerm = (InequalityTerm)
                        arrayType.getElementTypeTerm();
                    return returnTypeTerm;
                }
            } else if(SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.portClass)) {
                // If we are invoking a method on a port.
                TypedIOPort port = (TypedIOPort)
                    InlinePortTransformer.getPortValue(
                            method,
                            (Local)r.getBase(),
                            unit, 
                            localDefs, 
                            localUses);
                // Don't create constant terms for 
                // ports where we don't already know the type.
                if(!port.getType().isInstantiable()) {
                    return null;
                }
                InequalityTerm portTypeTerm =
                    new ConstantTerm(port.getType(),
                            port);
                if(methodName.equals("broadcast")) {
                    // The type of the argument must be less than the 
                    // type of the port.
                    InequalityTerm firstArgTerm = (InequalityTerm)
                        objectToInequalityTerm.get(
                                r.getArg(0));
                   
                    _addInequality(debug, solver,firstArgTerm,
                            portTypeTerm);
                    // Return type is void.
                    return null;
                } else if(methodName.equals("get")) {
                    if(r.getArgCount() == 2) {
                        // FIXME: array of portTypeTerm?
                        return portTypeTerm;
                    } else if(r.getArgCount() == 1) {
                        return portTypeTerm;
                    }
                } else if(methodName.equals("send")) {
                    if(r.getArgCount() == 3) {
                        // The type of the argument must be less than the 
                        // type of the port.
                        InequalityTerm secondArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);
                        // Return type is void.
                        return null;
                    } else if(r.getArgCount() == 2) {
                        // The type of the argument must be less than the 
                        // type of the port.
                        InequalityTerm secondArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(1));
                        _addInequality(debug, solver, secondArgTerm,
                                portTypeTerm);
                        // Return type is void.
                        return null;
                    }
                }                        
            } else if(SootUtilities.derivesFrom(baseClass,
                    PtolemyUtilities.attributeClass)) {
                // If we are invoking a method on a port.
                Attribute attribute = (Attribute)
                    InlineParameterTransformer.getAttributeValue(
                            method, 
                            (Local)r.getBase(),
                            unit, 
                            localDefs, 
                            localUses);
                if(attribute == null) {
                    // A method invocation with a null base is bogus,
                    // so don't create a type constraint.
                    return null;
                }
                if(attribute instanceof Variable) {
                    Variable parameter = (Variable)attribute;
                    InequalityTerm parameterTypeTerm =
                        new ConstantTerm(parameter.getType(),
                                parameter);
                    if(methodName.equals("setToken")) {
                    // The type of the argument must be less than the 
                        // type of the parameter.
                        InequalityTerm firstArgTerm = (InequalityTerm)
                            objectToInequalityTerm.get(
                                    r.getArg(0));
                        
                        _addInequality(debug, solver, firstArgTerm,
                                parameterTypeTerm);
                        // Return type is void.
                        return null;
                    } else if(methodName.equals("getToken")) {
                        // Return the type of the parameter.
                        return parameterTypeTerm;
                    }
                }
            }
        } else if(value instanceof ArrayRef) {
            // The type must be the same as the type of the
            // base of the array.
            return (InequalityTerm)objectToInequalityTerm.get(
                    ((ArrayRef)value).getBase());
        } else if(value instanceof CastExpr) {
            CastExpr castExpr = (CastExpr)value;
            Type type = castExpr.getType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
            if(tokenType != null) {
                // The type of the argument must be greater than the type of the
                // cast.
                // The return type will be the type of the cast.
                InequalityTerm baseTerm = new VariableTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        tokenType);
                    //(InequalityTerm)objectToInequalityTerm.get(
                    //     castExpr.getOp());
                InequalityTerm typeTerm = new ConstantTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        tokenType);
                //System.out.println("baseTerm = " + baseTerm);
                //System.out.println("typeTerm = " + typeTerm);
                 _addInequality(debug, solver, typeTerm, baseTerm);
                return baseTerm;
            } else {
                // Otherwise there is nothing to be done.
                return null;
            }
        } else if(value instanceof NewExpr) {
            NewExpr newExpr = (NewExpr)value;
            RefType type = newExpr.getBaseType();
            SootClass castClass = type.getSootClass();
            // If we are creating a Token type...
            if(SootUtilities.derivesFrom(castClass,
                    PtolemyUtilities.tokenClass)) {
                InequalityTerm typeTerm = new ConstantTerm(
                        PtolemyUtilities.getTokenTypeForSootType(type),
                        newExpr);
                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            } else {
                // Otherwise there is nothing to be done.
                return null;
            }
        } else if(value instanceof NewArrayExpr) {
            // Since arrays are aliasable, we must update their types.
            NewArrayExpr newExpr = (NewArrayExpr)value;
            Type type = newExpr.getBaseType();
            RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
            if(tokenType != null) {
                InequalityTerm typeTerm = new VariableTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        newExpr);
                // This is something we update, so put an entry
                // in the map used for updating
                objectToInequalityTerm.put(newExpr, typeTerm);
                // Then the value of the expression is the type of the
                // constructor.
                return typeTerm;
            }
            // Otherwise there is nothing to be done.
            return null;
        } else if(value instanceof FieldRef) {
            FieldRef r = (FieldRef)value;
            // Field references have the type of the field.
            SootField field = r.getField();

            // FIXME: UGH: This is the same as elementType...  
            if(field.getSignature().equals("<ptolemy.data.ArrayToken: ptolemy.data.Token[] _value>")) {
                InequalityTerm baseTerm =
                    (InequalityTerm)objectToInequalityTerm.get(((InstanceFieldRef)r).getBase());
                ptolemy.data.type.ArrayType arrayType =
                    new ptolemy.data.type.ArrayType(
                            ptolemy.data.type.BaseType.UNKNOWN);
                _addInequality(debug, solver, baseTerm, 
                        new VariableTerm(arrayType, r));
                InequalityTerm returnTypeTerm = (InequalityTerm)
                    arrayType.getElementTypeTerm();
                return returnTypeTerm;
            }
            return (InequalityTerm)objectToInequalityTerm.get(field);
        } else if(value instanceof Local) {
            // Local references have the type of the local.
            return (InequalityTerm)objectToInequalityTerm.get(value);
        } 
        // do nothing.
        return null;
    }
    
    private static void _addInequality(boolean debug, 
            InequalitySolver solver, InequalityTerm lesser, InequalityTerm greater) {
        if(lesser != null && greater != null) {
            Inequality inequality = new Inequality(lesser, greater);
            if(debug) System.out.println("adding inequality = " + inequality);
            solver.addInequality(inequality);
        }
    }

    private static void _createInequalityTerm(boolean debug, Object object, Type type, 
            Map objectToInequalityTerm) {
        RefType tokenType = PtolemyUtilities.getBaseTokenType(type);
        if(tokenType != null) {
            if(debug) System.out.println("creating inequality term for " + object);
            if(debug) System.out.println("type " + type);
       
            InequalityTerm term = 
                new VariableTerm(
                        PtolemyUtilities.getTokenTypeForSootType(tokenType),
                        object);
            objectToInequalityTerm.put(object, term);
        }
    }
        
    private static class ConstantTerm implements InequalityTerm {
        public ConstantTerm(ptolemy.data.type.Type type, Object object) {
            _type = type;
            _object = object;
        }

        public void fixValue() { }

        public Object getValue() { return _type; }

        public Object getAssociatedObject() { return _object; }

        public InequalityTerm[] getVariables() {
            return new InequalityTerm[0];
        }

        public void initialize(Object e) {
            setValue(e);
        }

        // Constant terms are not settable
        public boolean isSettable() { return false; }

        public boolean isValueAcceptable() {
            return true;
        }

        public void setValue(Object e) {
            _type = (ptolemy.data.type.Type)e;
        }

        public String toString() {
            return "{ConstantTerm: value = " + _type + ", associated object = " +
		_object + "}";
        }

        public void unfixValue() { }

        private ptolemy.data.type.Type _type;
        private Object _object;
    }    

    private static class VariableTerm implements InequalityTerm {
        public VariableTerm(ptolemy.data.type.Type type, Object object) {
            _declaredType = type;
            _currentType = type;
            _object = object;
        }

        public void fixValue() { _fixed = true; }

        public Object getValue() { return _currentType; }

        public Object getAssociatedObject() { return _object; }

        public InequalityTerm[] getVariables() {
             if (isSettable()) {
	    	InequalityTerm[] result = new InequalityTerm[1];
	    	result[0] = this;
	    	return result;
	    }
	    return (new InequalityTerm[0]);
        }

        public void initialize(Object e) throws IllegalActionException {
            if (_declaredType == ptolemy.data.type.BaseType.UNKNOWN) {
		_currentType = (ptolemy.data.type.Type)e;
	    } else {
		// _declaredType is a StructuredType
		((ptolemy.data.type.StructuredType)_currentType).initialize((ptolemy.data.type.Type) e);
	    }
        }

        // Variable terms are settable
        public boolean isSettable() { return ( !_declaredType.isConstant()); }

        public boolean isValueAcceptable() {
            return  _currentType.isInstantiable();
        }

        public void setValue(Object e) throws IllegalActionException {
	    if (!_declaredType.isSubstitutionInstance((ptolemy.data.type.Type)e)) {
	    	throw new RuntimeException("VariableTerm.setValue: "
		        + "Cannot update the type of " + this + " to the "
			+ "new type."
                        + ", Variable type: " + _declaredType.toString()
			+ ", New type: " + e.toString());
	    }

	    if (_declaredType == ptolemy.data.type.BaseType.UNKNOWN) {
		_currentType = (ptolemy.data.type.Type)e;
	    } else {
		// _declaredType is a StructuredType
		((ptolemy.data.type.StructuredType)_currentType).updateType((ptolemy.data.type.StructuredType)e);
	    }
        }

        public String toString() {
            return "{VariableTerm: value = " + _currentType + ", associated object = " +
		_object + "}";
        }

        public void unfixValue() { _fixed = false; }

        private ptolemy.data.type.Type _declaredType;
        private ptolemy.data.type.Type _currentType;
        private Object _object;
        private boolean _fixed = false;
    }    

    private CompositeActor _model;
}














