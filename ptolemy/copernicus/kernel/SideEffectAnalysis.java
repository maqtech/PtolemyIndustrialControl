/* An analysis for detecting objects that must be aliased to eachother.

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

package ptolemy.copernicus.kernel;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

import java.util.*;

/**
An analysis that determines which methods in a given invoke graph
have no side effects.
*/
public class SideEffectAnalysis extends BackwardFlowAnalysis {
    public SideEffectAnalysis(MethodCallGraph g) {
        super(g);
        doAnalysis();
    }

    /** Return the set of fields that the given method assigns
     *  to, or null if the side effects are unknown.
     */
    public Set getSideEffects(SootMethod method) {
        EffectFlow flow = (EffectFlow)getFlowBefore(method);
        if(flow == null) {
            if(_debug) System.out.println(
                    "SideEffectAnalysis: Method not found: " + method);
            return null;
        }
        if(flow.hasEffects()) {
            return flow.effectSet();
        } else {
            return new HashSet();
        }
    }
            
    /** Return true if the given method has any side effects.
     *  i.e. it assigns to any fields.
     */
    public boolean hasSideEffects(SootMethod method) {
        EffectFlow flow = (EffectFlow)getFlowBefore(method);
        if(flow == null) {
            if(_debug) System.out.println(
                    "SideEffectAnalysis: Method not found: " + method);
            return true;
        }
        return flow.hasEffects();
    }

    /** Return true if the given method has any side effects 
     *  on the given field.  i.e. it assigns to the given field.
     */
    public boolean hasSideEffects(SootMethod method, SootField field) {
        EffectFlow flow = (EffectFlow)getFlowBefore(method);
        if(flow == null) {
            if(_debug) System.out.println(
                    "SideEffectAnalysis: Method not found: " + method);
            return true;
        }
        return flow.hasEffects(field);
    }

    // Formulation:  An instance of the EffectFlow class.  If there
    // are no side effects, then the flow has _hasEffects == false;
    // if _hasEffects == true, then the effectSet is the set of fields
    // that are side effected, or null if any field may be side effected.
    protected Object newInitialFlow() {
        return new EffectFlow();
    }

    protected void flowThrough(Object inValue, Object d, Object outValue) {
        EffectFlow in = (EffectFlow)inValue;
        EffectFlow out = (EffectFlow)outValue;
        SootMethod method = (SootMethod)d;
 
        if(_debug) System.out.println(
                "SideEffectAnalysis: method = " + method);

        // If the input has unknown side effects, then don't
        // bother going through all the methods.
        if(in.hasUnknownSideEffects()) {
            out.setUnknownSideEffects();
        }

        // A method that is a context class is assumed to have side effects,
        // since we can't get it's method body.  Note that we could do better
        // by handling each method specifically.  
        // (For Example, Thread.currentThread()
        // has no body, but also has no side effects).
        if(!method.isConcrete()) {
            if(_debug) System.out.println("SideEffectAnalysis: has no body.");
            out.setUnknownSideEffects();
            return;
        }
        
        // A method has side effects if it sets the values of any fields.
        Body body = method.retrieveActiveBody();
        for(Iterator units = body.getUnits().iterator();
            units.hasNext();) {
            Unit unit = (Unit)units.next();
            if(_debug) System.out.println("unit = " + unit);
            for(Iterator boxes = unit.getDefBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value value = box.getValue();
                if(value instanceof FieldRef) {
                    if(_debug) System.out.println(
                            "SideEffectAnalysis: assigns to field");
                    out.addSideEffect(((FieldRef)value).getField());
                }
            }

            // Method calls that are in the invokeGraph 
            // have already been checked.
            // However, it turns out that context classes 
            // are not included in the
            // invokeGraph!  This checks to see if there 
            // are any invocations of
            // methods that are not in the invoke graph.  Conservatively
            // assume that they have side effects.
            Hierarchy hierarchy = Scene.v().getActiveHierarchy();
            for(Iterator boxes = unit.getUseBoxes().iterator();
                boxes.hasNext();) {
                ValueBox box = (ValueBox)boxes.next();
                Value expr = box.getValue();
                if(expr instanceof InvokeExpr) {
                    SootMethod invokedMethod = ((InvokeExpr)expr).getMethod();
                    if(expr instanceof SpecialInvokeExpr) {
                        SootMethod target = 
                            hierarchy.resolveSpecialDispatch(
                                    (SpecialInvokeExpr)expr, invokedMethod);
                        
                        if(!((MethodCallGraph)graph).isReachable(
                                target.getSignature())) {
                            if(_debug) System.out.println(
                                    "SideEffectAnalysis: specialInvokes method that is not in the graph");
                            out.setUnknownSideEffects();
                        }
                    } else if(expr instanceof InstanceInvokeExpr) {
                        Type baseType =
                            ((InstanceInvokeExpr)expr).getBase().getType();
                        if(!(baseType instanceof RefType)) {
                            // We can invoke methods on arrays...
                            // Ignore them here.
                            continue;
                        }
                        List list = hierarchy.resolveAbstractDispatch(
                                ((RefType)baseType).getSootClass(), invokedMethod);
                        for(Iterator targets = list.iterator();
                            targets.hasNext();) {
                            SootMethod target = (SootMethod)targets.next();
                             if(!((MethodCallGraph)graph).isReachable(
                                     target.getSignature())) {
                                 if(_debug) System.out.println(
                                         "SideEffectAnalysis: virtualInvokes method that is not in the graph");
                                 out.setUnknownSideEffects();
                             }
                        } 
                    } else if(expr instanceof StaticInvokeExpr) {
                        if(!((MethodCallGraph)graph).isReachable(
                                invokedMethod.getSignature())) {
                            if(_debug) System.out.println(
                                    "SideEffectAnalysis: staticInvokes method that is not in the graph");
                            out.setUnknownSideEffects();
                        }
                    }                    
                }
            }
        }
    }

    protected void copy(Object inValue, Object outValue) {
        EffectFlow in = (EffectFlow)inValue;
        EffectFlow out = (EffectFlow)outValue;
        out.setEffectFlow(in);
    }

    protected void merge(Object in1Value, Object in2Value, Object outValue) {
        EffectFlow in1 = (EffectFlow)in1Value;
        EffectFlow in2 = (EffectFlow)in2Value;
        EffectFlow out = (EffectFlow)outValue;
        
        // A method has side effects if any method it uses has side effects.
        out.setEffectFlow(in1);
        out.mergeEffectFlow(in2);
    }
 
    private static class EffectFlow {
        public EffectFlow() {
            _hasEffects = false;
            _effectSet = null;
        }

        public boolean equals(Object o) {
            if(o instanceof EffectFlow) {
                EffectFlow other = (EffectFlow)o;
                if(_hasEffects != other.hasEffects()) {
                    return false;
                } else if(_effectSet == null && other.effectSet() != null) {
                    return false;
                } else if(_effectSet == null && other.effectSet() == null) {
                    return true;
                }
                return _effectSet.equals(((EffectFlow)o).effectSet());
            } else {
                return false;
            }
        }

        public void addSideEffect(SootField field) {
            if(_hasEffects) {
                if(_effectSet != null) {
                    _effectSet.add(field);
                }
            } else {
                _hasEffects = true;
                _effectSet = new HashSet();
                _effectSet.add(field);
            }
        }

        public void mergeEffectFlow(EffectFlow flow) {
            if(flow.hasUnknownSideEffects()) {
                // If the flow has unknown effects, then we will
                // have unknown effects.
                setUnknownSideEffects();
            } else if(!flow.hasEffects()) {
                // If the flow have no effects, then we have no
                // change.
                return;
            } else if(_hasEffects) {
                if(_effectSet != null) {
                    _effectSet.addAll(flow.effectSet());
                } // else we have unknown side effects already.
            } else {
                _hasEffects = true;
                _effectSet = new HashSet();
                _effectSet.addAll(flow.effectSet());
            }
        }
        
        public boolean hasEffects(SootField field) {
            if(_hasEffects) {
                if(_effectSet != null) {
                    return _effectSet.contains(field);
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        public Set effectSet() {
            return _effectSet;
        }

        public boolean hasEffects() {
            return _hasEffects;
        }

        public void setEffectFlow(EffectFlow flow) {
            _hasEffects = flow.hasEffects();
            _effectSet = flow.effectSet();
        }

        public void setUnknownSideEffects() {
            _hasEffects = true;
            _effectSet = null;
        }

        public boolean hasUnknownSideEffects() {
            return (_hasEffects == true && _effectSet == null);
        }

        private boolean _hasEffects;
        private Set _effectSet;
    }
    private boolean _debug = false;
}
