/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2002 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.matlab.Engine;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.StringTokenizer;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ParseTreeEvaluator
/**
This class visits parse trees and evaluates them into a token value.

@author Steve Neuendorffer
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ParseTreeEvaluator implements ParseTreeVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node) 
            throws IllegalActionException {
        return evaluateParseTree(node, null);
    }
    
    public ptolemy.data.Token evaluateParseTree(
            ASTPtRootNode node, ParserScope scope) 
            throws IllegalActionException {
        _scope = scope;
        // Evaluate the value of the root node.
        node.visit(this);
        // and return it.
        _scope = null;
        return node.getToken();
    }
    
    public static ptolemy.data.Token functionCall(String methodName, 
            int argCount, Type[] argTypes, Object[] argValues) 
            throws IllegalActionException {
        CachedMethod method = CachedMethod.findMethod(methodName,
                argTypes, CachedMethod.FUNCTION);
        if(!method.isMissing()) {
            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        } else {
            // If we reach this point it means the function was not found on
            // the search path.
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < argCount; i++) {
                if (i == 0) {
                    buffer.append(argValues[i].toString());
                } else {
                    buffer.append(", " + argValues[i].toString());
                }
            }
            throw new IllegalActionException("No matching function " + 
                    methodName + "( " + buffer + " ).");
        }
    }

   public static ptolemy.data.Token methodCall(String methodName, 
           int argCount, Type[] argTypes, Object[] argValues) 
           throws IllegalActionException {
        CachedMethod method = CachedMethod.findMethod(methodName,
                argTypes, CachedMethod.METHOD);
        if(!method.isMissing()) {
            ptolemy.data.Token result = method.invoke(argValues);
            return result;
        } else {
            // If we reach this point it means the function was not found on
            // the search path.
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < argCount; i++) {
                if (i == 0) {
                    buffer.append(argValues[i].toString());
                } else {
                    buffer.append(", " + argValues[i].toString());
                }
            }
            throw new IllegalActionException("No matching function " + 
                    methodName + "( " + buffer + " ).");
        }
    }

    public void visitArrayConstructNode(ASTPtArrayConstructNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        
        int numChildren = node.jjtGetNumChildren();

        ptolemy.data.Token[] tokens = _getChildTokens(node);
        node.setToken(new ArrayToken(tokens));
    }

    public void visitBitwiseNode(ASTPtBitwiseNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();

        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        if(!(result instanceof BitwiseOperationToken)) {
            throw new IllegalActionException(
                    "Operation " + node.getOperator().image +
                    " not defined on " + result +
                    " which does not support bitwise operations.");
        }

        BitwiseOperationToken bitwiseResult = 
            (BitwiseOperationToken)result;
 
        // Make sure that exactly one of AND, OR, XOR is set.
        _assert(node.isBitwiseAnd() ^ node.isBitwiseOr() ^ node.isBitwiseXor(),
                node, "Invalid operation");

        for (int i = 1; i < numChildren; i++ ) {
            ptolemy.data.Token nextToken = node.jjtGetChild(i).getToken();
            if(!(nextToken instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        "Operation " + node.getOperator().image +
                        " not defined on " + result +
                        " which does not support bitwise operations.");
            }
            if(node.isBitwiseAnd()) {
                bitwiseResult = bitwiseResult.bitwiseAnd(nextToken);
            } else if(node.isBitwiseOr()) {
                bitwiseResult = bitwiseResult.bitwiseOr(nextToken);
            } else {
                bitwiseResult = bitwiseResult.bitwiseXor(nextToken);
            } 
        }
        node.setToken((ptolemy.data.Token)bitwiseResult);
    }

    public void visitFunctionNode(ASTPtFunctionNode node) 
            throws IllegalActionException {
        // A flag for debugging.
        boolean debug = false;

        // First check to see if the name references a valid variable.
        ptolemy.data.Token value = null;
		String functionName = node.getFunctionName();
        if(_scope != null && functionName != null) {
            value = _scope.get(node.getFunctionName());
        }
        if(value != null || functionName == null) {
            // The value of the first child should be either a FunctionToken,
            // an ArrayToken, or a MatrixToken.
            ptolemy.data.Token result;
            _evaluateAllChildren(node);
			
            value = node.jjtGetChild(0).getToken();
            
            int numChildren = node.jjtGetNumChildren();
            if(value instanceof ArrayToken) {
                if(numChildren == 2) {
                    result = _evaluateArrayIndex(node, value,
                            node.jjtGetChild(1).getToken());
                } else {
                    //FIXME need better error message when the first child
                    // is, say, an array expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if(value instanceof MatrixToken) {
                if (numChildren == 3) {
                    result = _evaluateMatrixIndex(node, value,
                            node.jjtGetChild(1).getToken(),
                            node.jjtGetChild(2).getToken());
                } else {
                    //FIXME need better error message when the first child
                    // is, say, a matrix expression
                    throw new IllegalActionException("Wrong number of indices "
                            + "when referencing " + node.getFunctionName());
                }
            } else if(value instanceof FunctionToken) {
                FunctionToken function = (FunctionToken)value;
                // check number of children against number of arguments of
                // function
                if (function.getNumberOfArguments() != numChildren - 1) {
                    throw new IllegalActionException("Wrong number of "
                            + "arguments when applying function "
                            + value.toString());
                }
                // apply the function token to the arguments
                ArrayList argList = new ArrayList(numChildren - 1);
                for(int i = 0; i < numChildren - 1; ++i) {
                    argList.add(i, node.jjtGetChild(i + 1).getToken());
                }
                result = function.apply(argList);
            } else {
                // the value cannot be indexed or applied
                // throw exception
                throw new IllegalActionException(
                        "Cannot index or apply arguments to "
                        + value.toString());
            }
            node.setToken(result);
            return;
        }

        if (node.getFunctionName().compareTo("eval") == 0) {
            if (node.jjtGetNumChildren() == 2) {
                _evaluateChild(node, 1);
                ptolemy.data.Token token = 
                    node.jjtGetChild(1).getToken();
                
                if(token instanceof StringToken) {
                    // Note that we do not want to store a reference to
                    // the parser, because parsers take up alot of memory.
                    PtParser parser = new PtParser();
                    ASTPtRootNode tree = parser.generateParseTree(
                            ((StringToken)token).stringValue());

                    // Note that we evaluate the recursed parse tree
                    // in the same scope as this parse tree.
                    tree.visit(this);
                    node.setToken(tree.getToken());
                    // FIXME cache?
                    return;
                }
            }
            throw new IllegalActionException("The function \"eval\" is" +
                    " reserved for reinvoking the parser, and takes" +
                    " exactly one String argument.");
        }

        if (node.getFunctionName().compareTo("matlab") == 0) {
            _evaluateChild(node, 1);
            ptolemy.data.Token token = 
                node.jjtGetChild(1).getToken();
	    if (token instanceof StringToken) {
                // Invoke the matlab engine to evaluate this function
                String expression = ((StringToken)token).stringValue();
                // NamedList scope = node.getParser().getScope();
                Engine matlabEngine = new Engine();
                ptolemy.data.Token result = null;
                long[] engine = matlabEngine.open();
                try {
                    synchronized (Engine.semaphore) {
                        String addPathCommand = null;         // Assume none
                        ptolemy.data.Token previousPath = null;
                        ptolemy.data.Token packageDirectories = null;
                        if(_scope != null) {
                            packageDirectories =
                                _scope.get("packageDirectories");
                        }
                        if (packageDirectories != null && 
                                packageDirectories instanceof StringToken) {
                            StringTokenizer dirs = new
                                StringTokenizer
                                ((String)((StringToken)packageDirectories
                                          ).stringValue(), ",");
                            StringBuffer cellFormat = new StringBuffer(512);
                            cellFormat.append("{");
                            if (dirs.hasMoreTokens()) {
                                cellFormat.append
                                    ("'" + UtilityFunctions
                                     .findFile(dirs.nextToken()) + "'");
                            }
                            while (dirs.hasMoreTokens()) {
                                cellFormat.append
                                    (",'" + UtilityFunctions
                                     .findFile(dirs.nextToken()) + "'");
                            }
                            cellFormat.append("}");

                            if (cellFormat.length() > 2) {
                                addPathCommand = "addedPath_=" +
                                    cellFormat.toString()
                                    + ";addpath(addedPath_{:});";
                                matlabEngine.evalString
                                    (engine, "previousPath_=path");
                                previousPath = matlabEngine.get
                                    (engine, "previousPath_");
                            }
                        }
                        matlabEngine.evalString
                            (engine, "clear variables;clear globals");

                        if (addPathCommand != null)
                            matlabEngine.evalString(engine, addPathCommand);

                        // Set scope variables
                        // This would be more efficient if the matlab engine
                        // understood the scope.
                        Iterator variables =
                            _scope.variableList().elementList().iterator();
                        while (variables.hasNext()) {
                            Variable var = (Variable)variables.next();
                            // This was here...  don't understand why???
                            // if (var != packageDirectories)
                            matlabEngine.put
                                (engine, var.getName(), var.getToken());
                        }
                        matlabEngine.evalString(engine, 
                                "result__=" + expression);
                        result = matlabEngine.get(engine, "result__");
                    }
                }
                finally {
                    matlabEngine.close(engine);
                }
                node.setToken(result);
                return;
            } else {
		throw new IllegalActionException("The function \"matlab\" is" +
                        " reserved for invoking the matlab engine, and takes" +
                        " a string matlab expression argument followed by" +
                        " names of input variables used in the expression.");
	    }
        }

        // the first child contains the function name as an id
        int argCount = node.jjtGetNumChildren() - 1;

        // If not a special function, then reflect the name of the function.
	Type[] argTypes = new Type[argCount];
	Object[] argValues = new Object[argCount];

        // First try to find a signature using argument token values.
	for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
			_evaluateChild(node, i + 1);
            ptolemy.data.Token token = node.jjtGetChild(i + 1).getToken();
            argValues[i] = token;
            argTypes[i] = token.getType();
        }

        ptolemy.data.Token result = functionCall(
                node.getFunctionName(), argCount, argTypes, argValues);
        node.setToken(result);
    }
	
    public void visitFunctionDefinitionNode(ASTPtFunctionDefinitionNode node)
            throws IllegalActionException {
        // collect all free variables in the function definition
        ParseTreeFreeVariableCollector collector =
            new ParseTreeFreeVariableCollector();
        Set freeVariableNames = collector.collectFreeVariables(node, _scope);
        // construct a NamedConstantsScope that maps the free variables to
        // their current value in the scope
        Map map = new HashMap();
        Iterator variableNameIterator = freeVariableNames.iterator();
        while (variableNameIterator.hasNext()) {
            String name = (String)variableNameIterator.next();
            if(_scope != null) {
                ptolemy.data.Token value = _scope.get(name);
                if(value != null) {
                    map.put(name, value);
                    continue;
                }
            }
            throw new IllegalActionException(
                    "The ID " + name + " is undefined.");
        }
        NamedConstantsScope constantsScope = new NamedConstantsScope(map);
        ExpressionFunction definedFunction =
            new ExpressionFunction(node.getArgumentNameList(),
                    (ASTPtRootNode)node.jjtGetChild(0), constantsScope);
        FunctionToken result = new FunctionToken(definedFunction);
        node.setToken(result);
        return;
    }
    
    public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }

        int numChildren = node.jjtGetNumChildren();
	if (numChildren != 3) {
	    // A functional-if node MUST have three children in the parse
	    // tree.
	    throw new InternalErrorException(
		    "PtParser error: a functional-if node does not have "
		    + "three children in the parse tree.");
	}

	// evaluate the first sub-expression
        _evaluateChild(node, 0);
        ptolemy.data.Token test = node.jjtGetChild(0).getToken();
        if (!(test instanceof BooleanToken)) {
            throw new IllegalActionException(
                    "Functional-if must branch on a boolean, but instead was "
		    + test.toString() + " an instance of "
                    + test.getClass().getName());
        }

        boolean value = ((BooleanToken)test).booleanValue();

     	// Choose the correct sub-expression to evaluate,
        // and type check the other.
        if(_typeInference == null) {
            _typeInference = new ParseTreeTypeInference();
        }
        
        ASTPtRootNode tokenChild, typeChild;
        if (value) {
            tokenChild = (ASTPtRootNode)node.jjtGetChild(1);
            typeChild = (ASTPtRootNode)node.jjtGetChild(2);
        } else {
            tokenChild = (ASTPtRootNode)node.jjtGetChild(2);
            typeChild = (ASTPtRootNode)node.jjtGetChild(1);
        }

        tokenChild.visit(this);
        ptolemy.data.Token token = tokenChild.getToken();
        Type type = _typeInference.inferTypes(typeChild, _scope);
              
        Type conversionType = (Type)TypeLattice.lattice().leastUpperBound(
                type, token.getType());
        
        token = conversionType.convert(token);
        node.setToken(token);
    }

    public void visitLeafNode(ASTPtLeafNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }

        // The node refers to a variable, or something else that is in
        // scope.
        if(_scope != null) {
            String name = node.getName();
            ptolemy.data.Token value = _scope.get(name);
            if(value != null) {
                node.setToken(value);
                return;
            }
        }
        throw new IllegalActionException(
                "The ID " + node.getName() + " is undefined.");
    }

    public void visitLogicalNode(ASTPtLogicalNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }

        // Note that we do not always evaluate all of the children...
        // We perform short-circuit evaluation instead and evaluate the 
        // children in order until the final value is determined, after
        // which point no more children are evaluated.

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");

        _evaluateChild(node, 0);

        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        if(!(result instanceof BooleanToken)) {
            throw new IllegalActionException("Cannot perform logical "
                    + "operation on " + result + " which is a " 
                    + result.getClass().getName());
        }
        
        // Make sure that exactly one of AND or OR is set.
        _assert(node.isLogicalAnd() ^ node.isLogicalOr(),
                node, "Invalid operation");
       
        // Perform both the short-circuit AND and short-circuit OR in
        // one piece of code.
        // FIXME: I dislike that this is not done in the token classes...
        boolean flag = node.isLogicalAnd();
        for(int i = 0; i < numChildren; i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            // Evaluate the child
            child.visit(this);
            // Get its value.
            ptolemy.data.Token nextToken = child.getToken();
            if(!(nextToken instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + nextToken + " which is a "
                        + result.getClass().getName());
            }
            if(flag != ((BooleanToken)nextToken).booleanValue()) {
                node.setToken(BooleanToken.getInstance(!flag));
                // Note short-circuit eval.
                return;
            }
        }
        node.setToken(BooleanToken.getInstance(flag));
    }
        
    public void visitMatrixConstructNode(ASTPtMatrixConstructNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);

        ptolemy.data.Token result = null;
        if (node.getForm() == 1) {
            int numChildren = node.jjtGetNumChildren();
            ptolemy.data.Token[] tokens = _getChildTokens(node);
            result = MatrixToken.create(tokens, node.getRowCount(), 
                             node.getColumnCount());
        } else if (node.getForm() == 2) {
            try {
                int columnCount = MatrixToken.determineSequenceLength(
                        (ScalarToken)node.jjtGetChild(0).getToken(),
                        (ScalarToken)node.jjtGetChild(1).getToken(), 
                        (ScalarToken)node.jjtGetChild(2).getToken());
                // Make sure that all following rows have the same number
                // of columns.
                for (int i = 1; i < node.getRowCount(); ++i) {
                    if(columnCount != MatrixToken.determineSequenceLength(
                            (ScalarToken)node.jjtGetChild(3*i).getToken(),
                            (ScalarToken)node.jjtGetChild(3*i+1).getToken(), 
                            (ScalarToken)node.jjtGetChild(3*i+2).getToken())) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }
                
                ptolemy.data.Token[] tokens =
                    new ptolemy.data.Token[node.getRowCount() * columnCount];
                for(int i = 0; i < node.getRowCount(); i++) {
                    ptolemy.data.Token[] newTokens = 
                        MatrixToken.createTokenSequence(
                                node.jjtGetChild(3*i).getToken(),
                                node.jjtGetChild(3*i+1).getToken(),
                                columnCount);
                    System.arraycopy(newTokens, 0, 
                            tokens, columnCount * i, columnCount);
                }
                result = MatrixToken.create(tokens,
                                 node.getRowCount(), columnCount);
            } catch (IllegalActionException ex) {
                // FIXME: better detail message that includes the thing
                // we were parsing.
                throw new IllegalActionException(null, null, ex,
                        "Matrix Token construction failed.");
            }
        }
        node.setToken(result);
    }

    public void visitMethodCallNode(ASTPtMethodCallNode node) 
        throws IllegalActionException {
        // Method calls are generally not cached...  They are repeated
        // every time the tree is evaluated.

        int argCount = node.jjtGetNumChildren();
        _evaluateAllChildren(node);

        // Handle indexing into a record.
        if(argCount == 1 &&
                node.jjtGetChild(0).getToken() instanceof RecordToken) {
            RecordToken record = (RecordToken)node.jjtGetChild(0).getToken();
            if(record.labelSet().contains(node.getMethodName())) {
                node.setToken(record.get(node.getMethodName()));
                return;
            }
        }

        // The first child is the object to invoke the method on.
	Type[] argTypes = new Type[argCount];
	Object[] argValues = new Object[argCount];

        // First try to find a signature using argument token values.
	for (int i = 0; i < argCount; i++) {
            // Save the resulting value.
            ptolemy.data.Token token = node.jjtGetChild(i).getToken();
            argValues[i] = token;
            argTypes[i] = token.getType();
        }

        ptolemy.data.Token result = methodCall(
                node.getMethodName(), argCount, argTypes, argValues);
        
        node.setToken(result);
    }

    public void visitPowerNode(ASTPtPowerNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        
        // Operator is always exponentiation
        
        // Note that since we use an iterative integer method, instead of 
        // a logarithmic method, the fastest thing is to apply the 
        // exponentiation inside out, i.e. left to right.
        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        for(int i = 1; i < numChildren; i++) {
            int times = 1;
            try {
                times = ((ptolemy.data.ScalarToken)
                    node.jjtGetChild(i).getToken()).intValue();
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(
                        "Only integral power numbers (e.g. 10^3) " +
                        "are allowed. Please check expression and use " +
                        "pow(10,3.5) instead to express non-integer " +
                        "powers.");
            }

            result = result.pow(times);
        }
        node.setToken(result);
    }

    public void visitProductNode(ASTPtProductNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is " +
                "not equal to number of operators plus one");
        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        for(int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
            if(operator.kind == PtParserConstants.MULTIPLY) {
                result = result.multiply(node.jjtGetChild(i).getToken());
            } else if(operator.kind == PtParserConstants.DIVIDE) {
                result = result.divide(node.jjtGetChild(i).getToken());
            } else if(operator.kind == PtParserConstants.MODULO) {
                result = result.modulo(node.jjtGetChild(i).getToken());
            } else {
                _assert(false, node, "Invalid operation");
            }
        }
        node.setToken(result);
    }
        
    public void visitRecordConstructNode(ASTPtRecordConstructNode node) 
        throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        
        int numChildren = node.jjtGetNumChildren();
        
        _assert(node.getFieldNames().size() == numChildren,
                node, "The number of labels and values does not " +
                "match in parsing a record expression.");
        String[] labels = (String[]) node.getFieldNames().toArray(
                new String[numChildren]);
        
        ptolemy.data.Token[] tokens = _getChildTokens(node);
        node.setToken(new RecordToken(labels, tokens));
    }

    public void visitRelationalNode(ASTPtRelationalNode node)
        throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        ptolemy.data.Token leftToken = node.jjtGetChild(0).getToken();
        ptolemy.data.Token rightToken = node.jjtGetChild(1).getToken();
        ptolemy.data.Token result;
        if(operator.kind == PtParserConstants.EQUALS) {
            result = leftToken.isEqualTo(rightToken);
        } else if(operator.kind == PtParserConstants.NOTEQUALS) {
            result = leftToken.isEqualTo(rightToken).not();
        } else {
            if(!((leftToken instanceof ScalarToken) &&
                    (rightToken instanceof ScalarToken))) {
                _assert(true, node,
                        "The " + operator.image +
                        " operator cannot be applied between " +
                        leftToken.getClass().getName() + " and " +
                        rightToken.getClass().getName());
            }
            ScalarToken leftScalar = (ScalarToken)leftToken;
            ScalarToken rightScalar = (ScalarToken)rightToken;
            if(operator.kind == PtParserConstants.GTE) {
                result = leftScalar.isLessThan(rightScalar).not();
            } else if(operator.kind == PtParserConstants.GT) {
                result = rightScalar.isLessThan(leftScalar);
            } else if(operator.kind == PtParserConstants.LTE) {
                result = rightScalar.isLessThan(leftScalar).not();
            } else if(operator.kind == PtParserConstants.LT) {
                result = leftScalar.isLessThan(rightScalar);
            } else {
                throw new IllegalActionException(
                        "Invalid operation " + operator.image + " between " +
                        leftToken.getClass().getName() + " and " +
                        rightToken.getClass().getName());
            }
        }
        node.setToken(result);
    }

//     public void visitRootNode(ASTPtRootNode node) 
//             throws IllegalActionException {
//         _assert(numChildren == 1, node, 
//                 "The root node must have exactly one child");
//         setToken(((ASTPtRootNode)node.jjtGetChild(0)).getToken());
//    }

   public void visitShiftNode(ASTPtShiftNode node)
        throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);

        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren == 2, node,
                "The number of child nodes must be two");

        Token operator = (Token)node.getOperator();
        ptolemy.data.Token token = node.jjtGetChild(0).getToken();
        ptolemy.data.Token bitsToken = node.jjtGetChild(1).getToken();
        ptolemy.data.Token result = null;

        _assert(token instanceof ScalarToken, node,
                "The " + operator + " operator cannot be applied " +
                "to a token that is not a ScalarToken.");

        _assert(bitsToken instanceof ScalarToken, node,
                "The " + operator + " operator cannot be applied " +
                "with a number of bits that is not a ScalarToken.");

        if(operator.kind == PtParserConstants.SHL) {
            result = ((ScalarToken)token).leftShift(
                    ((ScalarToken)bitsToken).intValue());
        } else if(operator.kind == PtParserConstants.SHR) {
            result = ((ScalarToken)token).rightShift(
                    ((ScalarToken)bitsToken).intValue());
        } else if(operator.kind == PtParserConstants.LSHR) {
            result = ((ScalarToken)token).logicalRightShift(
                    ((ScalarToken)bitsToken).intValue());
        } else {
            _assert(false, node, "Invalid operation");
        }
        node.setToken(result);
    }

    public void visitSumNode(ASTPtSumNode node) 
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        List lexicalTokenList = node.getLexicalTokenList();
        int numChildren = node.jjtGetNumChildren();
        _assert(numChildren > 0, node,
                "The number of child nodes must be greater than zero");
        _assert(numChildren == lexicalTokenList.size() + 1, node,
                "The number of child nodes is " +
                "not equal to number of operators plus one");
        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        for(int i = 1; i < numChildren; i++) {
            Token operator = (Token)lexicalTokenList.get(i - 1);
            if(operator.kind == PtParserConstants.PLUS) {
                result = result.add(node.jjtGetChild(i).getToken());
            } else if(operator.kind == PtParserConstants.MINUS) {
                result = result.subtract(node.jjtGetChild(i).getToken());
            } else {
                _assert(false, node, "Invalid operation");
            }
        }
        node.setToken(result);
    }

    public void visitUnaryNode(ASTPtUnaryNode node)
            throws IllegalActionException {
        if(node.isConstant() && node.isEvaluated()) {
            return;
        }
        _evaluateAllChildren(node);
        _assert(node.jjtGetNumChildren() == 1, node,
                "Unary node must have exactly one child!");
        ptolemy.data.Token result = node.jjtGetChild(0).getToken();
        if(node.isMinus()) {
            result = result.zero().subtract(result);
        } else if(node.isNot()) {
            if(result instanceof BooleanToken) {
                result = ((BooleanToken)result).not();
            } else {
                throw new IllegalActionException(
                        "Not operator not support for non-boolean token: " +
                        result.toString());
            }
        } else if(node.isBitwiseNot()) {
            if(!(result instanceof BitwiseOperationToken)) {
                throw new IllegalActionException(
                        "Bitwise negation" +
                        " not defined on " + result +
                        " which does not support bitwise operations.");
            }
            result = (ptolemy.data.Token)
                ((BitwiseOperationToken)result).bitwiseNot();
        } else {
            _assert(false, node, "Unrecognized unary node");
        }
        node.setToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /**
     * Assert that the given boolean value, which describes the given
     * parse tree node is true.  If it is false, then throw a new
     * InternalErrorException that describes the node that includes
     * the given message.
     */
    protected void _assert(boolean flag, ASTPtRootNode node, String message) {
        if(!flag) {
            throw new InternalErrorException(message + ": " + node.toString());
        }
    }    
    
    /** Loop through all of the children of this node, 
     *  visiting each one of them, which will cause their token
     *  value to be determined.
     */
    protected void _evaluateAllChildren(ASTPtRootNode node) 
            throws IllegalActionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            _evaluateChild(node, i);
        }
    }

    /** Return a new array of Tokens that correspond to the token values of
     *  the children of the given node.  This method does not visit the nodes,
     *  so the calling method should do that.
     */
    protected ptolemy.data.Token[] _getChildTokens(ASTPtRootNode node) {
        ptolemy.data.Token[] tokens = 
            new ptolemy.data.Token[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
            tokens[i] = child.getToken();
        }    
        return tokens;
    }

    /** Visit the child with the given index of the given node.
     *  This is usually called while visiting the given node.
     */
    protected void _evaluateChild(ASTPtRootNode node, int i)
            throws IllegalActionException {
        ASTPtRootNode child = (ASTPtRootNode)node.jjtGetChild(i);
        child.visit(this);
    }
    
    /** Evaluate the array index operation represented by the given node.
     *  @param node The syntax tree node that caused this method to be
     *  called.
     *  @param value The token that is being indexed into, which must
     *  be an ArrayToken.
     *  @param index The index, which must be an integer token.
     *  @return The element of the given token at the given index.
     */
    protected ptolemy.data.Token _evaluateArrayIndex(ASTPtRootNode node,
            ptolemy.data.Token value,
            ptolemy.data.Token index) throws IllegalActionException {
        if(!(value instanceof ArrayToken)) {
            _assert(true, node, "Cannot use array "
                    + "indexing on '" + value.toString()
                    + "' because its value is not an ArrayToken.");
        }
        if(!(index instanceof IntToken)) {
            _assert(true, node, "The array index '"
                    + index + "' is not an integer.");
        }
        int integerIndex = ((IntToken)index).intValue();
        try {
            return ((ArrayToken)value).getElement(integerIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index '"
                    + index + "' is out of bounds to the array '"
                    + value + "'.");
        }
    }

    /** Evaluate the Matrix index operation represented by the given node.
     *  @param node The syntax tree node that caused this method to be
     *  called.
     *  @param value The token that is being indexed into, which must
     *  be a MatrixToken.
     *  @param rowIndex The row index, which must be an integer token.
     *  @param columnIndex The column index, which must be an integer token.
     *  @return The element of the given token at the given index.
     */
    protected ptolemy.data.Token _evaluateMatrixIndex(ASTPtRootNode node,
            ptolemy.data.Token value,
            ptolemy.data.Token rowIndex,
            ptolemy.data.Token columnIndex) throws IllegalActionException {
        if(!(value instanceof MatrixToken)) {
            _assert(true, node, "Cannot use matrix "
                + "indexing on '" + value.toString()
                + "' because its value is not a MatrixToken.");
        }
        if(!(rowIndex instanceof IntToken)) {
            _assert(true, node, "The row index '"
                    + rowIndex + "' is not an integer.");
        }
        if(!(columnIndex instanceof IntToken)) {
            _assert(true, node, "The column index '"
                + rowIndex + "' is not an integer.");
        }
        int integerRowIndex = ((IntToken)rowIndex).intValue();
        int integerColumnIndex = ((IntToken)columnIndex).intValue();
        try {
            return ((MatrixToken)value).getElementAsToken(
                    integerRowIndex, integerColumnIndex);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException("The index ("
                    + rowIndex + "," + columnIndex 
                    + ") is out of bounds to the matrix '"
                    + value + "'.");
        }
    }

    // Evaluate the given parse tree in the same scope that is
    // currently being evaluated.
    protected ptolemy.data.Token _evaluateParseTree(
            ASTPtRootNode node) 
            throws IllegalActionException {
        // Evaluate the value of the root node.
        node.visit(this);
        return node.getToken();
    }

    private ParserScope _scope;
    private ParseTreeTypeInference _typeInference;
	
    ///////////////////////////////////////////////////////////////////
    ////                       inner classes                       ////

	private class ExpressionFunction implements FunctionToken.Function {
		
		public ExpressionFunction(List argumentNames, ASTPtRootNode exprRoot,
				ParserScope freeVariablesScope) {
			_argumentNames = new ArrayList(argumentNames);
			_exprRoot = exprRoot;
			_freeVariablesScope = freeVariablesScope;
		}
		
		public ptolemy.data.Token apply(List args)
				throws IllegalActionException {
			if (_parseTreeEvaluator == null) {
				_parseTreeEvaluator = new ParseTreeEvaluator();
			}
			// construct a NamedConstantsScope that contains mappings from
			// argument names to the given argument values
			Map map = new HashMap();
			for (int i = 0; i < args.size(); ++i) {
				String name = (String)_argumentNames.get(i);
				ptolemy.data.Token arg = (ptolemy.data.Token)args.get(i);
				map.put(name, arg);
			}
			NamedConstantsScope argumentsScope = new NamedConstantsScope(map);
			ArrayList listOfScopes = new ArrayList(2);
			listOfScopes.add(0, argumentsScope);
			listOfScopes.add(1, _freeVariablesScope);
			ParserScope evaluationScope = new NestedScope(listOfScopes);
			return _parseTreeEvaluator.evaluateParseTree(_exprRoot, evaluationScope);
		}
		
		public int getNumberOfArguments() {
			return _argumentNames.size();
		}

		private ASTPtRootNode _exprRoot;
		private List _argumentNames;
		private ParserScope _freeVariablesScope;
		private ParseTreeEvaluator _parseTreeEvaluator;
	}

}
