/* Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

public class SkeletonVisitor extends JavaVisitor implements JavaStaticSemanticConstants {
    public SkeletonVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        List retList = TNLManip.traverseList(this, null,
                node.getDefTypes());

        LinkedList newDefTypeList = new LinkedList();
        Iterator retItr = retList.iterator();

        while (retItr.hasNext()) {
            Object o = retItr.next();

            if (o != NullValue.instance) {
                newDefTypeList.addLast(o);
            }
        }

        node.setDefTypes(newDefTypeList);

        return node;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {

        int modifiers = node.getModifiers();

        List retList = TNLManip.traverseList(this, null, node.getMembers());

        Iterator retItr = retList.iterator();

        LinkedList newMemberList = new LinkedList();

        while (retItr.hasNext()) {
            Object o = retItr.next();

            if (o != NullValue.instance) {
                newMemberList.addLast(o);

                if (o instanceof ConstructorDeclNode) {
                    ConstructorDeclNode cDeclNode = (ConstructorDeclNode) o;

                    if (cDeclNode.getParams().size() == 0) {
                        // found default constructor, make it public
                        cDeclNode.setModifiers(PUBLIC_MOD);
                    }
                }
            }
        }

        node.setMembers(newMemberList);

        return node;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        int modifiers = node.getModifiers();

        if ((modifiers & PRIVATE_MOD) != 0) {
            return null;
        }

        node.setInitExpr(_initExpr(node.getDefType()));

        return node;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        int modifiers = node.getModifiers();

        if ((modifiers & PRIVATE_MOD) != 0) {
            return null;
        }

        if (node.getBody() != AbsentTreeNode.instance) {
            node.setModifiers(modifiers | NATIVE_MOD);
            node.setBody(AbsentTreeNode.instance);
        }

        return node;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        // constructor calls must not call private methods or access private variables
        // in the arguments to super() or this()

        node.setBody(new BlockNode(new LinkedList()));
        return node;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        List retList = TNLManip.traverseList(this, null, node.getMembers());

        Iterator retItr = retList.iterator();

        LinkedList newMemberList = new LinkedList();

        while (retItr.hasNext()) {
            Object o = retItr.next();

            if (o != NullValue.instance) {
                newMemberList.addLast(o);
            }
        }

        node.setMembers(newMemberList);

        return node;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    protected TreeNode _initExpr(TypeNode node) {
        if (node instanceof ReferenceTypeNode) {
            return new NullPntrNode();
        }

        if (node == IntTypeNode.instance) {
            return new IntLitNode("0");
        }

        if (node == LongTypeNode.instance) {
            return new LongLitNode("0L");
        }

        if (node == BoolTypeNode.instance) {
            return new BoolLitNode("false");
        }

        if (node == CharTypeNode.instance) {
            return new CharLitNode("\\0");
        }

        if (node == ShortTypeNode.instance) {
            return new CastNode(node, new IntLitNode("0"));
        }

        if (node == ByteTypeNode.instance) {
            return new CastNode(node, new IntLitNode("0"));
        }

        if (node == FloatTypeNode.instance) {
            return new FloatLitNode("0.0f");
        }

        if (node == DoubleTypeNode.instance) {
            return new DoubleLitNode("0.0");
        }

        return AbsentTreeNode.instance;
    }
}
