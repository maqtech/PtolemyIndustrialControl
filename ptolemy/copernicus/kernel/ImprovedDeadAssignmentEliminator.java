/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-2002 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-2002.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

/* Reference Version: $SootVersion: 1.2.2.dev.6 $ */


// FIXME: This improvement will be incorporated back into soot in the next
// version.  remove this class when that happens.



package ptolemy.copernicus.kernel;

import soot.Body;
import soot.BodyTransformer;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Main;
import soot.PhaseOptions;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.DivExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NopStmt;
import soot.jimple.RemExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.LocalDefs;
import soot.toolkits.scalar.LocalUses;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.Chain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ImprovedDeadAssignmentEliminator extends BodyTransformer
{
    private static ImprovedDeadAssignmentEliminator instance = new ImprovedDeadAssignmentEliminator();
    private ImprovedDeadAssignmentEliminator() {}

    public static ImprovedDeadAssignmentEliminator v() { return instance; }

    public String getDeclaredOptions() { return super.getDeclaredOptions() + " only-stack-locals"; }

    /** Eliminates dead code in a linear fashion.  Complexity is linear
        with respect to the statements.

        Does not work on grimp code because of the check on the right hand
        side for side effects.
    */

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        boolean eliminateOnlyStackLocals = Options.getBoolean(options, "only-stack-locals");

        JimpleBody body = (JimpleBody)b;
        if (Main.isVerbose)
            System.out.println("[" + body.getMethod().getName() +
                    "] Eliminating dead code...");

        if (Main.isProfilingOptimization)
            Main.deadCodeTimer.start();

        Set essentialStmts = new HashSet();
        LinkedList toVisit = new LinkedList();
        Chain units = body.getUnits();

        // Make a first pass through the statements, noting
        // the statements we must absolutely keep.
        {
            Iterator stmtIt = units.iterator();

            while (stmtIt.hasNext())
                {
                    Stmt s = (Stmt) stmtIt.next();
                    boolean isEssential = true;

                    if (s instanceof NopStmt)
                        isEssential = false;

                    if (s instanceof AssignStmt)
                        {
                            AssignStmt as = (AssignStmt) s;

                            if (as.getLeftOp() instanceof Local &&
                                    (!eliminateOnlyStackLocals ||
                                            ((Local) as.getLeftOp()).getName().startsWith("$")))
                                {
                                    Value rhs = as.getRightOp();

                                    isEssential = false;

                                    // proposed change: instance field refs to this can
                                    // be removed.
                                    if (rhs instanceof InvokeExpr ||
                                            (rhs instanceof InstanceFieldRef &&
                                                    !(!b.getMethod().isStatic() &&
                                                            ((InstanceFieldRef)rhs).getBase() ==
                                                            body.getThisLocal())) ||
                                            rhs instanceof ArrayRef)
                                        {
                                            // Note that InstanceFieldRef, ArrayRef, InvokeExpr all can
                                            // have side effects (like throwing a null pointer exception)

                                            isEssential = true;
                                        }
                                    else if (rhs instanceof DivExpr ||
                                            rhs instanceof RemExpr)
                                        {
                                            BinopExpr expr = (BinopExpr) rhs;

                                            if (expr.getOp1().getType().equals(IntType.v()) ||
                                                    expr.getOp2().getType().equals(IntType.v()) ||
                                                    expr.getOp1().getType().equals(LongType.v()) ||
                                                    expr.getOp2().getType().equals(LongType.v()))
                                                {
                                // Can trigger a division by zero
                                                    isEssential = true;
                                                }
                                        }
                                }
                        }

                    if (isEssential)
                        {
                            essentialStmts.add(s);
                            toVisit.addLast(s);
                        }
                }
        }

        CompleteUnitGraph graph = new CompleteUnitGraph(body);
        LocalDefs defs = new SimpleLocalDefs(graph);
        LocalUses uses = new SimpleLocalUses(graph, defs);

        // Add all the statements which are used to compute values
        // for the essential statements, recursively
        {

            while (!toVisit.isEmpty())
                {
                    Stmt s = (Stmt) toVisit.removeFirst();
                    Iterator boxIt = s.getUseBoxes().iterator();

                    while (boxIt.hasNext())
                        {
                            ValueBox box = (ValueBox) boxIt.next();

                            if (box.getValue() instanceof Local)
                                {
                                    Iterator defIt = defs.getDefsOfAt(
                                            (Local) box.getValue(), s).iterator();

                                    while (defIt.hasNext())
                                        {
                                            // Add all the definitions as essential stmts

                                            Stmt def = (Stmt) defIt.next();

                                            if (!essentialStmts.contains(def))
                                                {
                                                    essentialStmts.add(def);
                                                    toVisit.addLast(def);
                                                }
                                        }
                                }
                        }
                }
        }

        // Remove the dead statements
        {
            Iterator stmtIt = units.iterator();

            while (stmtIt.hasNext())
                {
                    Stmt s = (Stmt) stmtIt.next();

                    if (!essentialStmts.contains(s))
                        stmtIt.remove();
                    else if (s instanceof AssignStmt &&
                            ((AssignStmt) s).getLeftOp() == ((AssignStmt) s).getRightOp() &&
                            ((AssignStmt) s).getLeftOp() instanceof Local)
                        {
                            // Stmt is of the form a = a which is useless

                            stmtIt.remove();
                        }
                }
        }

        // Eliminate dead assignments from invokes such as x = f(), where
        //    x is no longer used
        {
            Iterator stmtIt = units.snapshotIterator();

            while (stmtIt.hasNext())
                {
                    Stmt s = (Stmt) stmtIt.next();

                    if (s instanceof AssignStmt &&
                            s.containsInvokeExpr())
                        {
                            Local l = (Local) ((AssignStmt) s).getLeftOp();
                            InvokeExpr e = (InvokeExpr) s.getInvokeExpr();

                            // Just find one use of l which is essential
                            {
                                Iterator useIt = uses.getUsesOf(s).iterator();
                                boolean isEssential = false;

                                while (useIt.hasNext())
                                    {
                                        UnitValueBoxPair pair = (UnitValueBoxPair)
                                            useIt.next();

                                        if (essentialStmts.contains(pair.unit))
                                            {
                                                isEssential = true;
                                                break;
                                            }
                                    }

                                if (!isEssential)
                                    {
                                        // Transform it into a simple invoke.

                                        Stmt newInvoke = Jimple.v().newInvokeStmt(e);

                                        units.swapWith(s, newInvoke);
                                    }
                            }
                        }
                }
        }

        if (Main.isProfilingOptimization)
            Main.deadCodeTimer.end();

    }
}







