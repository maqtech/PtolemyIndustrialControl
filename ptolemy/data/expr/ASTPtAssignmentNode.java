/* ASTPtFunctionNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998
*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.ComplexMatrixMath;
import ptolemy.matlab.Engine;
import java.lang.Math;		/* Needed for javadoc */
import java.util.Iterator;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// ASTPtAssignmentNode
/**

<p>
@author Steve Neuendorffer
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
@see ptolemy.data.expr.UtilityFunctions
@see java.lang.Math
*/
public class ASTPtAssignmentNode extends ASTPtRootNode {

    public ASTPtAssignmentNode(int id) {
        super(id);
    }

    public ASTPtAssignmentNode(PtParser p, int id) {
        super(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String getAssignment() {
        ParseTreeWriter writer = new ParseTreeWriter();
        return getIdentifier() + "=" + 
            writer.printParseTree(getExpressionTree());
    }

    public String getIdentifier() {
        Node n = jjtGetChild(0);
        if (!(n instanceof ASTPtLeafNode))
            return null;
        else
            return ((ASTPtLeafNode)n).getName();
    }

    public ASTPtRootNode getExpressionTree() {
        Node n = jjtGetChild(1);
        if (!(n instanceof ASTPtRootNode))
            return null;
        else
            return (ASTPtRootNode)n;
    }

    public void jjtClose() {
        super.jjtClose();
        // We cannot assume that the result of a function call is
        // constant, even when the arguments to the function are.
        _isConstant = false;
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitAssignmentNode(this);
    }
}

