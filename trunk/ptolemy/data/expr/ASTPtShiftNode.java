/* ASTPtShiftNodes represent arthimetic and logical shift
   operators(<<, >>, >>>) nodes in the parse tree.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtShiftNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents shift
operators(>>, <<, <<<) nodes in the parse tree.
<p>
Each node of this type has exactly two children. The resolved type
is the type of the first child.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtShiftNode extends ASTPtRootNode {

    public ASTPtShiftNode(int id) {
        super(id);
    }

    public ASTPtShiftNode(PtParser p, int id) {
        super(p, id);
    }

    /** Return the lexical token representing the operation of this node.
     */
    public Token getOperator() {
        return _lexicalToken;
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitShiftNode(this);
    }

    protected Token _lexicalToken = null;
}
