/* ASTPtRelationalNodes represent relational operator(>, >=, <, <=, ==, !=)
   nodes in the parse tree.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtRelationalNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents relational
operator(>, >=, <, <=, ==, !=) nodes in the parse tree.
<p>
Each node of this type has exactly two children. The resolved type
is a BooleanToken.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtRelationalNode extends ASTPtRootNode {

    protected ptolemy.data.Token  _resolveNode()
            throws IllegalArgumentException {
        int num =  jjtGetNumChildren();
        if ( (num != 2) ||  (_lexicalTokens.size() != 1) ) {
            throw new IllegalArgumentException(
                    "A relational node needs two children and " +
                    "one operator.");
        }
        ptolemy.data.Token result = childTokens[0];
        Token x = (Token)_lexicalTokens.take();
        // need to insert at end if want to reparse tree
        _lexicalTokens.insertLast(x);
        try {
            if (x.image.compareTo("==") == 0) {
                result = result.isEqualTo(childTokens[1]);
                return result;
            } else  if (x.image.compareTo("!=") == 0) {
                result = result.isEqualTo(childTokens[1]);
                return ((ptolemy.data.BooleanToken)result).negate();
            } else  {
                // relational operators only make sense on types below double
                double a = ((ScalarToken)childTokens[0]).doubleValue();
                double b = ((ScalarToken)childTokens[1]).doubleValue();
                boolean res = false;
                if (x.image.compareTo(">=") == 0) {
                    if (a >= b) res = true;
                } else if  (x.image.compareTo(">") == 0) {
                    if (a>b) res = true;
                } else if (x.image.compareTo("<=") == 0) {
                    if (a <= b) res = true;
                } else if (x.image.compareTo("<") == 0) {
                    if (a < b) res = true;
                } else {
                    throw new IllegalArgumentException("invalid operator " +
                            x.image + " in relational node");
                }
                return new ptolemy.data.BooleanToken(res);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Invalid operation " + x.image + " between " +
                    childTokens[0].getClass().getName() + " and " +
                    childTokens[1].getClass().getName() +
                    ": " + ex.getMessage());
        }
    }

    public ASTPtRelationalNode(int id) {
        super(id);
    }

    public ASTPtRelationalNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtRelationalNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtRelationalNode(p, id);
    }
}
