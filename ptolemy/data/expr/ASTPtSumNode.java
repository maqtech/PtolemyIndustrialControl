/* ASTPtSumNode represent sum(+, -) nodes in the parse tree

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

//////////////////////////////////////////////////////////////////////////
//// ASTPtSumNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents sum(+, -) nodes in
the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtSumNode extends ASTPtRootNode {

    protected ptolemy.data.Token _resolveNode() throws IllegalArgumentException {
        int num =  jjtGetNumChildren();
        if (num == 1) {
            return childTokens[0];
        }
        if (jjtGetNumChildren() != ( _tokenList.size() +1) ) {
            String str = "Invalid state in sum node, number of children is";
            str = str + "not equal to number of oprators plus one";
            throw new IllegalArgumentException(str);
        }
        ptolemy.data.Token result = childTokens[0];
        String op = "";
        int i = 1;
        try {
            for (i = 1; i<num; i++) {
                // When start using 1.2 will change this
                // take from the front, put back at the end
                Token x = (Token)_tokenList.take();
                _tokenList.insertLast(x); // here so that tree can be reparsed
                op = x.image;
                if (op.compareTo("+") == 0) {
                    result = result.add(childTokens[i]);
                } else if (op.compareTo("-") == 0) {
                    result = result.subtract(childTokens[i]);
                } else {
                    String str = "Invlid concatenator in sum() production, ";
                    throw new IllegalArgumentException(str + "check parser");
                }
            }
        } catch (Exception ex) {
            String str = "Invalid operation " + op + " between ";
            str = str + result.getClass().getName() + " and ";
            str = str + childTokens[i].getClass().getName();
            throw new IllegalArgumentException(str);
        }
        return result;
    }


    public ASTPtSumNode(int id) {
        super(id);
    }

    public ASTPtSumNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtSumNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtSumNode(p, id);
    }
}
