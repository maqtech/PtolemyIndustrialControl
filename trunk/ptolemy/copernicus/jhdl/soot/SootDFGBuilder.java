/* Dataflow representation of a Soot Block

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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.copernicus.jhdl.util.*;

import soot.toolkits.graph.Block;
import soot.*;
import soot.jimple.*;

import ptolemy.graph.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class SootDFGBuilder extends SootASTVisitor {

    public SootDFGBuilder(Block block, SootBlockDirectedGraph g) 
	throws SootASTException {
	_graph = g;
	_valueMap = _graph.getValueMap();
	processBlock(block);
    }

    public Stmt processDefinitionStmt(DefinitionStmt stmt, 
					Value rightOp, Value leftOp) {
	
	Node rightNode = _valueMap.getValueNode(rightOp);
	Node leftNode = _valueMap.getValueNode(leftOp);

	// Add edge
	_graph.addEdge(rightNode,leftNode);

	return stmt;
    }

    public Value processValue(Value val, boolean left) 
	throws SootASTException {

	if (!left)
	    _valueMap.getOrAddValueNode(val); // make sure it is added
	else
	    _valueMap.addValueNode(val);	

	Value v = super.processValue(val,left);
	return v;
    }

    public Value processUnopExpr(UnopExpr expr, Value op) {
	Node opNode = _valueMap.getValueNode(op);
	Node exprNode = _valueMap.getValueNode(expr);	
	_graph.addEdge(opNode,exprNode);
	return expr;
    }

    public Value processBinopExpr(BinopExpr expr, Value op1, Value op2) {
	Node op1Node = _valueMap.getValueNode(op1);
	Node op2Node = _valueMap.getValueNode(op2);
	Node exprNode = _valueMap.getValueNode(expr);
	_graph.addEdge(op1Node,exprNode,"op1");
	_graph.addEdge(op2Node,exprNode,"op2");
	return expr;
    }

    public Stmt processReturnVoidStmt(ReturnVoidStmt stmt) { return stmt; }
    public Stmt processReturnStmt(ReturnStmt stmt, Value returnVal) { 
	Node returnedValue = _valueMap.getValueNode(returnVal);
	Node returnNode = _graph.addNodeWeight(stmt);
	_graph.addEdge(returnedValue,returnNode);
	return stmt; 
    }
    public Stmt processInvokeStmt(InvokeStmt stmt, InvokeExpr ie) { 
	return stmt; 
    }
    public Stmt processIfStmt(IfStmt stmt, ConditionExpr condition) {
	return stmt;
    }
    public Stmt processGotoStmt(GotoStmt stmt) { return stmt; }
    public Stmt processTableSwitchStmt(TableSwitchStmt stmt) { return stmt; }
    public Value processThisRef(ThisRef ifr) {return ifr;}
    public Value processParameterRef(ParameterRef ifr) {return ifr; }
    public Value processConstant(Constant c) { return c; }

    public Value processLocal(Local l, boolean left) {
	return l;
    }

    public Value processInstanceFieldRef(InstanceFieldRef ifr, Value base,
					 boolean left) {
	
	// Node that represents field-ref Base
	Node baseNode = _valueMap.getValueNode(base);
	// Node that represents ifr.
	Node ifrNode = _valueMap.getValueNode(ifr);

	// Determine whether a base edge has been created
	Edge baseEdge=null;
	for (Iterator i=_graph.inputEdges(ifrNode).iterator();i.hasNext();) {
	    Edge e = (Edge) i.next();
	    if (e.hasWeight() && e.getWeight().equals(_graph.BASE_WEIGHT))
		baseEdge = e;
	}
	if (baseEdge == null)
	    _graph.addEdge(baseNode,ifrNode,_graph.BASE_WEIGHT);
	
	/*
	if (dupIfr == null) {
	    //System.out.println("No dup");
	    // No matching IFR. Add edge for base.
	    _graph.addEdge(baseNode,ifrNode,_graph.BASE_WEIGHT);
 	} else {
	    //System.out.println("dup:"+ifr+" "+dupIfr);
	    // A matching IFR has been found. Delete the Node that
	    // has been created for ifr and create a reference
	    // between ifr with the Node of the matching ifr.
	    _graph.removeNode(ifrNode);
	    _valueMap.replaceValueNode(ifr,dupIfr);
	    if (left) {
		// An assignment is being made. Create a new node
		// with the existing IFR.
		Node newNode = _valueMap.addValueNode(dupIfr);
		_graph.addEdge(baseNode,newNode,_graph.BASE_WEIGHT);
	    }
	}
	*/
 	return ifr;
    }

    public Value processVirtualInvokeExpr(VirtualInvokeExpr ie, 
					  Value args[], Value base) {
	Node invokeNode = _valueMap.getValueNode(ie);
	Node baseNode = _valueMap.getValueNode(base);
	for(int i = 0; i < args.length; i++) {
	    Node argNode = _valueMap.getValueNode(args[i]);
	    //System.out.println("arg="+argNode+" invokeNode="+invokeNode);
	    _graph.addEdge(argNode,invokeNode,"arg"+i);
	}
	_graph.addEdge(baseNode,invokeNode);
	return ie;
    }

    public static void main(String args[]) {
	SootASTVisitor.DEBUG = true;
	Block blocks[] = getBlocks(args);
	SootBlockDirectedGraph graphs[] = 
	    new SootBlockDirectedGraph[blocks.length];
	for (int i = 0 ; i < blocks.length; i++) {
	    graphs[i] = new SootBlockDirectedGraph(blocks[i]);
	    try {
		SootDFGBuilder s = new SootDFGBuilder(blocks[i],
						      graphs[i]);
		PtDirectedGraphToDotty.writeDotFile("bgraph"+i,graphs[i]);
	    } catch (SootASTException e) {
		//System.err.println(e);
		e.printStackTrace();
		System.exit(1);
	    }
	} 
    }

    protected SootBlockDirectedGraph _graph;

    protected ValueMap _valueMap;
}
