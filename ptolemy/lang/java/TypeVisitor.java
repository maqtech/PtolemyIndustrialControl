package ptolemy.lang.java;

import ptolemy.lang.*;
import java.util.LinkedList;

public class TypeVisitor extends JavaVisitor {
    public TypeVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return _setType(node, LongTypeNode.instance);
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return _setType(node, FloatTypeNode.instance);
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return _setType(node, DoubleTypeNode.instance);
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return _setType(node, CharTypeNode.instance);
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        // fixme
        node.setProperty("type",
         new TypeNameNode(new NameNode(AbsentTreeNode.instance, "String")));
        return null;
    }
     
    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return _setType(node, ArrayInitTypeNode.instance);    
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return _setType(node, NullTypeNode.instance);
    }
    
    public Object visitThisNode(ThisNode node, LinkedList args) {
        ClassDecl theClass = (ClassDecl) node.getDefinedProperty("theClass");
                
        return _setType(node, theClass.getDefType());
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        ArrayTypeNode arrType = (ArrayTypeNode) type(node.getArray()); 
        return _setType(node, arrType.getBaseType());
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        TypedDecl decl = (TypedDecl) JavaDecl.getDecl(node.getName());
        return _setType(node, decl.getType());        
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        MethodDecl decl = (MethodDecl) JavaDecl.getDecl(node.getMethod());
        
        return _setType(node, decl.getType());
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _setType(node, node.getSuperType());
    }

    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitUnaryArithNode(node);
    }

    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitUnaryArithNode(node);
    }

    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitIncrDecrNode(node);
    }

    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr())));
    }

    public Object visitNotNode(NotNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitPlusNode(PlusNode node, LinkedList args) {
        // support string concatenations
           
    
        return _visitBinaryArithNode(node);
    }

    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryArithNode(node);
    }

    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));    
    }

    public Object visitRightShiftArithNode(RightShiftArithNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));    
    }

    public Object visitLTNode(LTNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitGTNode(GTNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitLENode(LENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitGENode(GENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBitwiseNode(node);    
    }

    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBitwiseNode(node);
    }

    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBitwiseNode(node);
    }

    public Object visitCandNode(CandNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitCorNode(CorNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitRightShiftArithAssignNode(RightShiftArithAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    public TypeNode _visitFieldAccessNode(FieldAccessNode node) {
        FieldDecl fieldDecl = (FieldDecl) JavaDecl.getDecl((NamedNode) node);
        
        return _setType(node, fieldDecl.getType());
    }
    
    public TypeNode _visitUnaryArithNode(UnaryArithNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr())));
    }

    public TypeNode _visitIncrDecrNode(IncrDecrNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr())));
    }

    public TypeNode _visitBinaryArithNode(BinaryArithNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    public TypeNode _visitBitwiseNode(BitwiseNode node) {
        return _setType(node, TypeUtility.arithPromoteType(
         type(node.getExpr1()), type(node.getExpr2())));
    }

    protected TypeNode _setType(ExprNode expr, TypeNode type) {
        expr.setProperty("type", type);
        return type;            
    }

    /** Return the type of an expression node, checking for a memoized type before 
     *  starting the visitation.
     *  The visitor must not call this method with the same node it handles,
     *  or else an infinite recursion will occur.
     */
    public TypeNode type(ExprNode node) {
        if (node.hasProperty("type")) {
           return (TypeNode) node.getDefinedProperty("type"); 
        }
        return (TypeNode) node.accept(this, null); 
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        ApplicationUtility.error("node " + node.toString() +
        " is not an expression, so it does not have a type");
        return null;
    }          
}
