

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
import ptolemy.copernicus.kernel.SootUtilities;


/**
A Transformer that is responsible for inlining the values of parameters.
The values of the parameters are taken from the model specified for this 
transformer.
*/
public class InlineParameterTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private InlineParameterTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on the given model.
     *  The model is assumed to already have been properly initialized so that
     *  resolved types and other static properties of the model can be inspected.
     */
    public static InlineParameterTransformer v(CompositeActor model) { 
        return new InlineParameterTransformer(model);
    }

    public String getDefaultOptions() {
        return ""; 
    }

    public String getDeclaredOptions() { 
        return super.getDeclaredOptions() + " deep"; 
    }

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("InlineParameterTransformer.internalTransform("
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
       
        SootClass attributeClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Attribute");
        Type attributeType = RefType.v(attributeClass);
        SootClass settableClass = 
            Scene.v().loadClassAndSupport("ptolemy.kernel.util.Settable");
        Type settableType = RefType.v(settableClass);
               
        SootClass parameterClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.expr.Variable");
        SootMethod getTokenMethod = 
            parameterClass.getMethod("ptolemy.data.Token getToken()");
        SootMethod setTokenMethod = 
            parameterClass.getMethod("void setToken(ptolemy.data.Token)");

        Map attributeToValueFieldMap = new HashMap();

        // For every variable and settable attribute in the model, create a
        // field that has the value of that attributes.
        _createTokenAndExpressionFields(Scene.v().getMainClass(), _model, _model,
                attributeToValueFieldMap);

        // Loop over all the actor instance classes.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className = Options.getString(options, "targetPackage")
                + "." + entity.getName();
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            
            _createTokenAndExpressionFields(entityClass, entity, entity,
                    attributeToValueFieldMap);
        }

        for(Iterator i = Scene.v().getApplicationClasses().iterator();
            i.hasNext();) {
            SootClass theClass = (SootClass)i.next();
            
            // inline calls to parameter.getToken and getExpression
            for(Iterator methods = theClass.getMethods().iterator();
                methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();
                
                // What about static methods?
                if(method.isStatic()) {
                    continue;
                }
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                // Add a this local...  note that we might not have one.
                Local thisLocal;
                try {
                    thisLocal = body.getThisLocal();
                } catch (Exception ex) {
                    //FIXME: what if no thisLocal?
                    throw new RuntimeException("method " + method + " does not have a thisLocal!");
                }
                /*Jimple.v().newLocal("this", 
                        RefType.v(theClass));
                body.getLocals().add(thisLocal);
                body.getUnits().addFirst(Jimple.v().newIdentityStmt(thisLocal, 
                        Jimple.v().newThisRef((RefType)thisLocal.getType())));
                */

                // System.out.println("method = " + method);

                boolean moreToDo = true;
                while(moreToDo) {
                    CompleteUnitGraph unitGraph = 
                        new CompleteUnitGraph(body);
                    // this will help us figure out where locals are defined.
                    SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
                    SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);
                    
                    moreToDo = _inlineMethodCalls(theClass, method, body,
                        unitGraph, localDefs, localUses, attributeToValueFieldMap);
                    LocalNameStandardizer.v().transform(body, phaseName + ".lns");
                }
            }
        }                
    }
    
    private static boolean _inlineMethodCalls(SootClass theClass, SootMethod method, 
            JimpleBody body, UnitGraph unitGraph,
            LocalDefs localDefs, LocalUses localUses, Map attributeToValueFieldMap) {
        boolean doneSomething = false;
        for(Iterator units = body.getUnits().snapshotIterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            Iterator boxes = unit.getUseBoxes().iterator();
            while(boxes.hasNext()) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
                if(value instanceof InstanceInvokeExpr) {
                    InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                    //      System.out.println("invoking = " + r.getMethod());
                    if(r.getBase().getType() instanceof RefType) {
                        RefType type = (RefType)r.getBase().getType();
                        
                        // inline calls to attribute changed.
                        if(r.getMethod().equals(PtolemyUtilities.attributeChangedMethod)) {
                            // If we are calling attribute changed on one of the classes
                            // we are generating code for.
                            if(type.getSootClass().isApplicationClass()) {
                             // Now inline the resulting call.
                               List methodList = 
                                    Scene.v().getActiveHierarchy().resolveAbstractDispatch(
                                            type.getSootClass(), PtolemyUtilities.attributeChangedMethod);
                                if(methodList.size() == 1) {
                                    // inline the method.
                                    SootMethod inlinee = (SootMethod)methodList.get(0);
                                    if(!inlinee.getDeclaringClass().isApplicationClass()) {
                                        inlinee.getDeclaringClass().setLibraryClass();
                                    }
                                    inlinee.retrieveActiveBody();
                                    SiteInliner.inlineSite(inlinee,
                                            (Stmt)unit, method);
                                    doneSomething = true;
                                } else {
                                    System.out.println("Can't inline " + unit);
                                    for(int i = 0; i < methodList.size(); i++) {
                                        System.out.println("target = " + methodList.get(i));
                                    }
                                }
                            } else {
                                // FIXME: this is a bit of a hack, but
                                // for right now it seems to work.
                                // How many things that aren't
                                // the actors we are generating
                                // code for do we really care about here?
                                // Can we do this without having to create
                                // a class for the attribute too????
                                body.getUnits().remove(unit);
                                doneSomething = true;
                            }
                        }

                        // Statically evaluate constant arguments.
                        Value argValues[] = new Value[r.getArgCount()];
                        int argCount = 0;
                        for(Iterator args = r.getArgs().iterator();
                            args.hasNext();) {
                            Value arg = (Value)args.next();
                            //    System.out.println("arg = " + arg);
                            if(Evaluator.isValueConstantValued(arg)) {
                                argValues[argCount++] = Evaluator.getConstantValueOf(arg);
                                //        System.out.println("argument = " + argValues[argCount-1]);
                            } else {
                                break;
                            }
                        }

                        if(SootUtilities.derivesFrom(type.getSootClass(), 
                                PtolemyUtilities.settableClass)) {
                            // if we are invoking a method on a variable class, then
                            // attempt to get the constant value of the variable.
                            Attribute attribute =
                                getAttributeValue((Local)r.getBase(), unit, localDefs, localUses);
                                  
                            // If the base is not constant, then obviously there is nothing we can do
                            if(attribute == null) {
                                continue;
                            }
                                    
                            // Inline getType, setTypeEquals, etc...
                            if(attribute instanceof Typeable) {
                                PtolemyUtilities.inlineTypeableMethods(body, 
                                        unit, box, r, (Typeable)attribute);
                                       
                            }

                            // For Variables, we handle get/setToken, get/setExpression
                            // different from other settables
                            if(attribute instanceof Variable) {
                                // Deal with tricky methods separately.
                                if(r.getMethod().getName().equals("getToken")) {
                                    // replace the method call with a field ref.
                                    box.setValue(Jimple.v().newStaticFieldRef(
                                            (SootField)attributeToValueFieldMap.get(attribute)));
                                    doneSomething = true;
                                } else if(r.getMethod().getName().equals("setToken")) {
                                    // Call attribute changed AFTER we set the token.
                                    PtolemyUtilities.callAttributeChanged(
                                            (Local)r.getBase(), theClass, method, body, 
                                            body.getUnits().getSuccOf(unit));
                                            
                                    // replace the entire statement
                                    // (which must be an invokeStmt anyway)
                                    // with an assignment to the field of the first argument.
                                    body.getUnits().swapWith(unit, 
                                            Jimple.v().newAssignStmt(
                                                    Jimple.v().newStaticFieldRef((SootField)
                                                            attributeToValueFieldMap.get(attribute)),
                                                    r.getArg(0)));
                                    doneSomething = true;
                                } else if(r.getMethod().getName().equals("getExpression")) {
                                    // First get the token out of the field, and then insert a call
                                    // to its toString method to get the expression.
                                    SootField tokenField = 
                                        (SootField)attributeToValueFieldMap.get(attribute);
                                    String localName = "_CGTokenLocal";
                                    Local tokenLocal = Jimple.v().newLocal(localName,
                                            tokenField.getType());
                                    body.getLocals().add(tokenLocal);
                                            
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(tokenLocal,
                                                    Jimple.v().newStaticFieldRef(tokenField)),
                                            unit);
                                    box.setValue(Jimple.v().newVirtualInvokeExpr(tokenLocal, 
                                            PtolemyUtilities.toStringMethod));
                                    doneSomething = true;
                                    // FIXME null result => ""
                                } else if(r.getMethod().getName().equals("setExpression")) {
                                    // Call attribute changed AFTER we set the token.
                                    PtolemyUtilities.callAttributeChanged(
                                            (Local)r.getBase(), theClass, method, body, 
                                            body.getUnits().getSuccOf(unit));
                                            
                                    Token token;
                                    // First create a token with the given
                                    // expression and then set the
                                    // token to that value.
                                    try {
                                        // FIXME: This is rather tricky..
                                        // is there a better way to do it?
                                        Variable temp = new Variable();
                                        temp.setTypeEquals(((Variable)attribute).getType());
                                        temp.setExpression(((StringConstant)argValues[0]).value);
                                        token = temp.getToken();
                                    } catch (Exception ex) {
                                        throw new RuntimeException("Illegal parameter value = " 
                                                + argValues[0]);
                                    }
                                    // Create code to instantiate the token
                                    SootField tokenField = 
                                        (SootField)attributeToValueFieldMap.get(attribute);
                                    String localName = "_CGTokenLocal";
                                    Local tokenLocal = 
                                        PtolemyUtilities.buildConstantTokenLocal(
                                                body, unit, token, localName);
                                                                                         
                                    body.getUnits().swapWith(unit, 
                                            Jimple.v().newAssignStmt(
                                                    Jimple.v().newStaticFieldRef(
                                                            tokenField), tokenLocal));
                                    doneSomething = true;
                                } 
                            } else {
                                // It's just settable, so handle get/setExpression
                                if(r.getMethod().equals(PtolemyUtilities.getExpressionMethod)) {
                                    // Call attribute changed AFTER we set the expression
                                    PtolemyUtilities.callAttributeChanged(
                                            (Local)r.getBase(), theClass, method, body, 
                                            body.getUnits().getSuccOf(unit));
                                            
                                    box.setValue(Jimple.v().newStaticFieldRef(
                                            (SootField)attributeToValueFieldMap.get(attribute)));
                                    doneSomething = true;
                                } else if(r.getMethod().equals(PtolemyUtilities.setExpressionMethod)) {
                                    // Call attribute changed AFTER we set the token.
                                    PtolemyUtilities.callAttributeChanged(
                                            (Local)r.getBase(), theClass, method, body, 
                                            body.getUnits().getSuccOf(unit));
                                    // replace the entire statement (which must be an invokeStmt anyway)
                                    // with an assignment to the field of the first argument.
                                    body.getUnits().swapWith(unit, 
                                            Jimple.v().newAssignStmt(
                                                    Jimple.v().newStaticFieldRef((SootField)
                                                            attributeToValueFieldMap.get(attribute)),
                                                    r.getArg(0)));
                                    doneSomething = true;
                                }
                            }
                                   

                            /*
                              // FIXME what about all the other methods???
                              // If we have a attribute and all the args are constant valued, then
                              if(argCount == r.getArgCount()) {
                              // reflect and invoke the same method on our token
                              Constant constant = SootUtilities.reflectAndInvokeMethod(attribute,
                              r.getMethod(), argValues);
                              System.out.println("method result  = " + constant);
                                        
                              // replace the method invocation.
                              box.setValue(constant);
                              }
                            */
                        } 
                    }                              
                }
            }
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
        return doneSomething;
    }            

    /** Attempt to determine the constant value of the given local, which is assumed to have a variable
     *  type.  Walk backwards through all the possible places that the local may have been defined and
     *  try to symbolically evaluate the value of the variable. If the value can be determined, 
     *  then return it, otherwise return null.
     */ 
    public static Attribute getAttributeValue(Local local, 
            Unit location, LocalDefs localDefs, LocalUses localUses) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if(definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if(value instanceof CastExpr) {
                return getAttributeValue( 
                        (Local)((CastExpr)value).getOp(), stmt, localDefs, localUses);
            } else if(value instanceof FieldRef) {
                SootField field = ((FieldRef)value).getField();
                ValueTag tag = (ValueTag)field.getTag("_CGValue");
                if(tag == null) {
                    return null;
                } else {
                    return (Attribute)tag.getObject();
                }
            } else if(value instanceof NewExpr) {
                // If we get to an object creation, then try
                // to figure out where the variable is stored into a field.
                Iterator pairs = localUses.getUsesOf(stmt).iterator();
                while(pairs.hasNext()) {
                    UnitValueBoxPair pair = (UnitValueBoxPair)pairs.next();
                    if(pair.getUnit() instanceof DefinitionStmt) {
                        DefinitionStmt useStmt = (DefinitionStmt)pair.getUnit();
                        if(useStmt.getLeftOp() instanceof FieldRef) {
                             SootField field = ((FieldRef)useStmt.getLeftOp()).getField();
                             ValueTag tag = (ValueTag)field.getTag("_CGValue");
                             if(tag == null) {
                                 return null;
                             } else {
                                 return (Attribute)tag.getObject();
                             }
                        }
                    }
                }
            } else {
                System.out.println("unknown value = " + value);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for(Iterator i = definitionList.iterator();
                i.hasNext();) {
                System.out.println(i.next().toString());
            }
        }
        return null;
    }
    
    // Create a static field in the given class for each attribute in the given container 
    // that is a variable or settable.  If the attribute is a variable, then the field will have
    // type Token, and if only a settable, then the field will have type String.
    // In addition, add a tag to the field that contains the value of the token or expression
    // that that field contains.
    private static void _createTokenAndExpressionFields(SootClass theClass,
            NamedObj context, NamedObj container, Map attributeToValueFieldMap) {
        /*   SootClass tokenClass = 
            Scene.v().loadClassAndSupport("ptolemy.data.Token");
            Type tokenType = RefType.v(tokenClass);*/
        SootClass stringClass =
            Scene.v().loadClassAndSupport("java.lang.String");
        Type stringType = RefType.v(stringClass);
        for(Iterator attributes =
                container.attributeList().iterator();
            attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
            if(attribute instanceof Settable) {
                Settable settable = (Settable)attribute;

                String fieldName = SootUtilities.sanitizeName(attribute.getName(context));
                SootField field;
                // Create a field to contain the value of the attribute.
                if(settable instanceof Variable) {
                    Variable variable = (Variable)settable;
                    ptolemy.data.type.Type type = variable.getType();
                    Type tokenType = PtolemyUtilities.getSootTypeForTokenType(type);
                    field = new SootField(
                            fieldName + "_CGToken",
                            tokenType, 
                            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
                    theClass.addField(field);
                    try {
                        field.addTag(new ValueTag(variable.getToken()));
                    } catch (Exception ex) {
                    }
                } else {
                    field = new SootField(
                            fieldName + "_CGExpression",
                            stringType,
                            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
                    theClass.addField(field);
                    String expression = settable.getExpression();
                    field.addTag(new ValueTag(expression));
                }
                attributeToValueFieldMap.put(attribute, field);
            }
            _createTokenAndExpressionFields(theClass, context, attribute, attributeToValueFieldMap);
        }
    }

    public SootField _findAttributeField(SootClass entityClass, String name) {
        return entityClass.getFieldByName(name);
    }

    private CompositeActor _model;
}














