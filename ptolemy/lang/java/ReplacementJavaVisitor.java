/*
A base class for visitors that do replacement of child nodes.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.util.LinkedList;
import ptolemy.lang.*;

//////////////////////////////////////////////////////////////////////////
//// ReplacementJavaVisitor
/** ReplacementJavaVisitor attempts to collect the default behavior of visitors
 *  that replace children of a node with their return values after visitation.
 *  Therefore, the default behavior is to set the children to the list of 
 *  replacements, and return the same node. 
 *
 *  The _lazy flag can be retrieved from the compile unit node, and used to
 *  determine if only lazy resolution is required.
 *  _isSkippable() can then be called with a modified node to determine
 *  if it can be skipped, in the case of fields, methods, constructors,
 *  and inner classes of a class or interface. Also, by default static
 *  and instance initializers are skipped during lazy resolution.
 *
 *  @author ctsay@eecs.berkeley.edu
 */
public abstract class ReplacementJavaVisitor extends JavaVisitor {
    public ReplacementJavaVisitor() {
        super(TM_CUSTOM);
    }

    public ReplacementJavaVisitor(int traversalMethod) {
        super(traversalMethod);  
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        return node;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return node;
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return node;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return node;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return node;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return node;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return node;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return node;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return node;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return node;
    }


    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return node;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return node;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return node;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitVarDeclNode(VarDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return node;
    }
    
    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return node;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return node;
    }

    /** The default visit method. Replace all children with their return
     *  values, using the same arguments, and return the node.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList retChildList =
         TNLManip.traverseList(this, node, args, node.children());

        node.setChildren(retChildList);

        return node;
    }   
}