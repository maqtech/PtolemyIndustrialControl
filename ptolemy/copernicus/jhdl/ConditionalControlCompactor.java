/* 

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

package ptolemy.copernicus.jhdl;


import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.ListIterator;
import java.util.LinkedList;

import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;
import ptolemy.copernicus.jhdl.util.CompoundBooleanExpression;
import ptolemy.copernicus.jhdl.util.CompoundOrExpression;
import ptolemy.copernicus.jhdl.util.CompoundAndExpression;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import soot.jimple.IfStmt;
import soot.jimple.BinopExpr;

import soot.SootMethod;
import soot.Unit;
import soot.Body;
import soot.Value;
import soot.PatchingChain;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.Block;


//////////////////////////////////////////////////////////////////////////
//// ConditionalControlCompactor
/**
 * 
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class ConditionalControlCompactor {
    
    public static void compact(SootMethod method) 
	throws IllegalActionException {

	Body mbody = method.retrieveActiveBody();
	PatchingChain chain = mbody.getUnits();

	// Iterate over entire graph until compaction results in no new changes
	boolean graphModified;
	Vector removedUnits = new Vector(chain.size());
	int version=1;
	do {
	    graphModified = false;
	    // iterate over graph (update current at end)
	    Iterator i = chain.snapshotIterator();
	    for (Unit current = (Unit) i.next();i.hasNext();) {
		
//    		System.out.println("Attemping to merge Unit "+
//    				   current);
		if (removedUnits.contains(current)) {
//  		    System.out.println("\tRemoved - ignore");
		    if (i.hasNext())
			current = (Unit) i.next();
		    else
			current = null;
		    continue;
		}

		Unit mergedUnit = mergeUnit(chain,current);
		if (mergedUnit != null) {
		    removedUnits.add(mergedUnit);		    
		    // graphModified = true;
		    // Print
		    // Keep same node! (continue merging here)
		    // need to update graph

		    BriefBlockGraph bbgraph = new BriefBlockGraph(mbody); 
		    BlockGraphToDotty.writeDotFile("mod_"+version++,bbgraph);
//  		    if (version > 15)
//  			return;
		    graphModified = true;
		} else {
		    if (i.hasNext())
			current = (Unit) i.next();
		    else
			current = null;
		}

	    } while (i.hasNext());

	} while(graphModified);

    }

    protected static Unit mergeUnit(PatchingChain chain, Unit root) 
	throws IllegalActionException {
	
	// 1. Is root an IfStmt?
	if (!(root instanceof IfStmt))
	    return null;
	IfStmt rootIfStmt = (IfStmt) root;
	Unit rootTarget = rootIfStmt.getTarget();
	
	// 2. Is successor an IfStmt?
	Unit successor = (Unit) chain.getSuccOf(root);
	if (!(successor instanceof IfStmt))
	    return null;
	IfStmt successorIfStmt = (IfStmt) successor;
	Unit successorSuccessor = (Unit) chain.getSuccOf(successor);
	Unit successorTarget = successorIfStmt.getTarget();

	// 3. See if target of rootIfStmt goes to same unit
	//    as target OR succesesor of successorIfStmt
	if (!((rootTarget == successorSuccessor) ^
	      (rootTarget == successorTarget)))
	    return null;
	    
	// 4. Create new merged expression for rootIfStmt
	Value rootCondition = rootIfStmt.getCondition();
	Value successorCondition = successorIfStmt.getCondition();

	CompoundBooleanExpression newExpression;
	if (rootTarget == successorSuccessor) {
	    // Expression = 'rootCondition & successorCondition
	    newExpression = new CompoundAndExpression(
		CompoundBooleanExpression.invertValue(rootCondition),
		successorCondition);
						      
	} else {
	    // Expression = rootCondition | successorCondition
	    newExpression = 
		new CompoundOrExpression(rootCondition,
					 successorCondition);
	}
	rootIfStmt.setCondition(newExpression);
	
	// 5. Remove successor & patch target
	chain.remove(successor);
	if (rootTarget == successorSuccessor)
	    rootIfStmt.setTarget(successorTarget);

	return successor;
    }
 

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.err.println("<classname> <methodname>");
	}
	String classname = args[0];
	String methodname = args[1];
	
	soot.SootClass testClass = 
	    ptolemy.copernicus.jhdl.test.Test.getApplicationClass(classname);
	if (testClass == null) {
	    System.err.println("Class "+classname+" not found");
	    System.exit(1);
	}
	System.out.println("Loading class "+classname+" method "+methodname);
	if (!testClass.declaresMethodByName(methodname)) {
	    System.err.println("Method "+methodname+" not found");
	    System.exit(1);
	}
	soot.SootMethod testMethod = testClass.getMethodByName(methodname);
	soot.Body body = testMethod.retrieveActiveBody();
	soot.toolkits.graph.CompleteUnitGraph unitGraph = 
	    new soot.toolkits.graph.CompleteUnitGraph(body);	
	BriefBlockGraph bbgraph = new BriefBlockGraph(body);
	BlockGraphToDotty.writeDotFile("beforegraph",bbgraph);
	try {
	    //	    compactConditionalControl(testMethod);
	    compact(testMethod);
	} catch (IllegalActionException e) {
	    System.err.println(e);
	}
	bbgraph = new BriefBlockGraph(body);
	BlockGraphToDotty.writeDotFile("aftergraph",bbgraph);
    }

}
