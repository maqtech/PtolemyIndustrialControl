/*
A code generator that generates C code from a Java abstract syntax tree.

Copyright (c) 2001 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.lang.c;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.lang.java.JavaVisitor;
import ptolemy.lang.java.JavaDecl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;


/** A code generator that generates C code from a Java abstract syntax tree.
 *  This code generator requires that static resolution has been
 *  performed beforehand, and it also requires that indentation
 *  levels (i.e., INDENTATION_KEY property settings) have been set.
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */
public class CCodeGenerator extends JavaVisitor implements CCodeGeneratorConstants {
    public CCodeGenerator() {
        super(TM_CHILDREN_FIRST);
    }

    /** Return the code fragment corresponding to a node. */
    public static String writeCodeFragment(TreeNode node) {
        CCodeGenerator ccg = new CCodeGenerator();
        Object retval = node.accept(ccg, null);

        if (retval instanceof String) {
            return (String) retval;
        }

        return _stringListToString((List) retval);
    }

    /** Write out the code files for each member of the argument list of
     *  CompileUnitNodes to the files with names corresponding to the members
     *  of the argument filename list.
     */
    public static void writeCompileUnitNodeList(List unitList, List filenameList) {
        Iterator unitItr = unitList.iterator();
        Iterator filenameItr = filenameList.iterator();

        while (unitItr.hasNext()) {
            CompileUnitNode unitNode = (CompileUnitNode) unitItr.next();

            String outCode = writeCodeFragment(unitNode);

            String outFileName = (String) filenameItr.next();

            try {
                FileOutputStream outFile = new FileOutputStream(outFileName);
                outFile.write(outCode.getBytes());
                outFile.close();
            } catch (IOException e) {
                System.err.println("error opening/writing/closing output file "
                        + outFileName);
                System.err.println(e.toString());
            }
        }
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        String ident = node.getIdent();
        TreeNode qualifier = node.getQualifier();
        if (qualifier == AbsentTreeNode.instance) {
            return TNLManip.addFirst(ident);
        }

        List qualPartList =
            (List) node.childReturnValueAt(node.CHILD_INDEX_QUALIFIER);

        return TNLManip.arrayToList(new Object[] {qualPartList, "." + ident});
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return TNLManip.addFirst("");
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return TNLManip.addFirst("'" + node.getLiteral() + '\'');
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return TNLManip.addFirst("\"" + node.getLiteral() + '\"');
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return TNLManip.addFirst("boolean");
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return TNLManip.addFirst("char");
    }

    // FIXME: Figure out exactly how byte types should be dealt with.
    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return TNLManip.addFirst("char");
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return TNLManip.addFirst("short");
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return TNLManip.addFirst("int");
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return TNLManip.addFirst("float");
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return TNLManip.addFirst("long");
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return TNLManip.addFirst("double");
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_BASETYPE), "[]"});
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return TNLManip.addFirst("void");
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_TYPE), ".this"});
    }

    public Object visitOuterSuperAccess(OuterSuperAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_TYPE), ".super"});
    }

    // FIXME : This returns a String instead of a string list, as it should
    // for consistency. However, returning a string list will break some
    // existing code, so this change will be deferred.
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        if (node.getPkg() != AbsentTreeNode.instance) {
            retList.addLast("package ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_PKG));
            retList.addLast(";\n");
        }

        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_IMPORTS));

        retList.addLast("\n");

        List typeList = (List) node.childReturnValueAt(node.CHILD_INDEX_DEFTYPES);

        Iterator typeItr = typeList.iterator();

        while (typeItr.hasNext()) {
            retList.addLast(typeItr.next());
            retList.addLast("\n");
        }

        // for new version:
        // return retList;

        return _stringListToString(retList);
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ";\n"});
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {

        return TNLManip.arrayToList(new Object[] {"import ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME), ".*;\n"});
    }


    /** Generate C code for a Java class declaration.
     *  The data structure associated with the class has
     *  already been declared in the header file generation pass,
     *  so just generate code for the methods.
     *  @param node The AST node of the Java class declaration that is
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given class declaration.
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // An iterator that sweeps through all different member declarations
        // (declarations for fields, methods, constructors, etc.) of the
        // given Java class declaration.
        Iterator membersIter; 

        // Include required standard header files.     
        retList.addLast("#include <stdlib.h>\n");
 
        // Include the declarations generated by the HeaderFileGenerator
        List className = (List) node.childReturnValueAt(node.CHILD_INDEX_NAME);
        retList.addLast("#include \"" + _stringListToString(className) 
                + ".h\"\n\n");
 
        // Just suppressing modifiers for now. 
        // FIXME: figure out exactly what, if any, processing  
        // needs to be done for each possible modifer.
        // JCG: retList.addLast(Modifier.toString(node.getModifiers()));

        // FIXME: Figure out what needs to be done for super classes,
        // and implemented interfaces.

        // Convert the declarations of member methods, and constructors.
        List members = node.getMembers();
   
        // Declare each method and constructor of the Java class as a 
        // separate C function.
        membersIter = members.iterator();
        while (membersIter.hasNext()) {
            Object member = membersIter.next();
            if (member instanceof MethodDeclNode) {
                retList.addLast(((MethodDeclNode)member).
                        returnValueAsElement());
            }
            else if (member instanceof ConstructorDeclNode) {
                retList.addLast(((ConstructorDeclNode)member).
                        returnValueAsElement());
            }
        }


        return retList;
    }


    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_RETURNTYPE));
        retList.addLast(" ");
        
        String methodName = (String)(node.getProperty(C_NAME_KEY));
        retList.addLast(methodName);
        retList.addLast("(");

        // Insert a reference to the containing class instance. This
        // reference is used to implement accesses through "this." 
        ClassDeclNode classDeclNode = (ClassDeclNode)
                JavaDecl.getDecl((TreeNode)node).getContainer().getSource();
        System.out.print("Got class decl source: ");
        System.out.println(classDeclNode.getClass().getName());
        retList.addLast("struct ");
        retList.addLast(_declaredClassNameOf(classDeclNode));
        retList.addLast(" *this");
        if (!(node.getParams().isEmpty())) retList.addLast(", ");
       
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS)));
        retList.addLast(")");

        // FIXME: Incorporate any necessary support for the throws list.

        if (node.getBody() == AbsentTreeNode.instance) {
            retList.addLast(";");
        } else {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_BODY));
        }

        retList.addLast("\n");

        return retList;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // Set the return type of the constructor to be a pointer to
        // a structure that implements the corresponding class.
        ClassDeclNode classDeclNode = (ClassDeclNode)
                JavaDecl.getDecl((TreeNode)node).getContainer().getSource();
        String equivalentType = "struct " + _declaredClassNameOf(classDeclNode);
        retList.addLast(equivalentType);
        retList.addLast(" *");

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast((String)(node.getProperty(C_NAME_KEY)));
        retList.addLast("(");
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_PARAMS)));
        retList.addLast(") ");

        // FIXME: Add any necessary support for the throws list.

        retList.addLast("{\n");

        retList.addLast(_indent(1) + equivalentType + " *this ");
        retList.addLast("= (" + equivalentType + "*)malloc(sizeof(" +
                        equivalentType + "));\n");

        // FIXME: convert nested constructor calls:
        // retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CONSTRUCTORCALL));

        LinkedList bodyStrList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_BODY);

        // Get rid of the first indentation string, '{' and '\n' 
        // of the block node string. Get rid of the trailing '}\n" too.
        bodyStrList.removeFirst();
        bodyStrList.removeFirst();
        bodyStrList.removeLast();
        retList.addLast(bodyStrList);

        retList.addLast(_indent(1) + "return this;\n}\n\n");

        return retList;
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);

        return TNLManip.arrayToList(new Object[] {"this(",
                                                      _commaList(argsList), ");\n"});
    }

    public Object visitStaticInitNode(StaticInitNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"static ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_BLOCK), "\n"});
    }

    public Object visitInstanceInitNode(InstanceInitNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_BLOCK);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("interface ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_NAME));
        retList.addLast(" ");

        List implList = (List) node.childReturnValueAt(node.CHILD_INDEX_INTERFACES);

        if (!implList.isEmpty()) {
            retList.addLast("extends ");
            retList.addLast(_commaList(implList));
            retList.addLast(" ");
        }

        retList.addLast("{\n");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_MEMBERS));
        retList.addLast("}\n");

        return retList;
    }

    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"{", _commaList(
                (List) node.childReturnValueAt(node.CHILD_INDEX_INITIALIZERS)),
                                                      "}"});
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            Modifier.toString(node.getModifiers()),
                node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE),
                " " + node.getName().getIdent()});
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"super(",
                                                      _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_ARGS)),
                                                      ");\n"});
    }

    /** Generate C code for a Java block. 
     *  @param node The AST node of the Java block that is 
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given block.
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
                _indent(node), "{\n",
                node.childReturnValueAt(node.CHILD_INDEX_STMTS), 
                _indent(node), "}\n"});
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return TNLManip.addFirst(";\n");
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_NAME),
                ": ",
                node.childReturnValueAt(node.CHILD_INDEX_STMT)});
    }

    /** Generate C code for a Java if statement (including the else
     *  part, if applicable.
     *  @param node The AST node of the Java if statement that is
     *  to be translated to C. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given if statement.
     */
    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(_indent(node));
        retList.addLast("if (");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CONDITION));
            retList.addLast(") ");
        _generateStmtOrBlock(node, node.CHILD_INDEX_THENPART, retList);

        TreeNode elsePart = node.getElsePart();
        if (elsePart != AbsentTreeNode.instance) {
            retList.addLast(_indent(node));
            retList.addLast("else ");
            _generateStmtOrBlock(node, node.CHILD_INDEX_ELSEPART, retList);
        }

        retList.addLast("\n");

        return retList;
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"switch (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ") {\n",
                                                      _separateList((List) node.childReturnValueAt(node.CHILD_INDEX_SWITCHBLOCKS), "\n"),
                                                      "}\n"});
    }

    public Object visitCaseNode(CaseNode node, LinkedList args) {
        if (node.getExpr() == AbsentTreeNode.instance) {
            return TNLManip.addFirst("default:\n");
        }

        return TNLManip.arrayToList(new Object[] {"case ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ":\n"});
    }

    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_CASES),
                node.childReturnValueAt(node.CHILD_INDEX_STMTS)});
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
            // while loop
            return TNLManip.arrayToList(new Object[] {"while (",
                                                          node.childReturnValueAt(node.CHILD_INDEX_TEST), ") \n",
                                                          node.childReturnValueAt(node.CHILD_INDEX_AFTSTMT)});
        } else {
            // do loop
            return TNLManip.arrayToList(new Object[] {"do ",
                                                          node.childReturnValueAt(node.CHILD_INDEX_FORESTMT),
                                                          " while (",
                                                          node.childReturnValueAt(node.CHILD_INDEX_TEST),
                                                          ");\n"});
        }
    }

    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] { _indent(node),
            node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(_indent(node) + "for (");
        retList.addLast(_forInitStringList(node.getInit()));
        retList.addLast("; ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_TEST));
        retList.addLast("; ");
        retList.addLast(_commaList((List) 
                node.childReturnValueAt(node.CHILD_INDEX_UPDATE)));
        retList.addLast(") ");
        _generateStmtOrBlock(node, node.CHILD_INDEX_STMT, retList);
        return retList;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("break");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        retList.addLast(";\n");

        return retList;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("continue");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_LABEL));
        }
        retList.addLast(";\n");

        return retList;
    }

    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {_indent(node), "return ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitThrowNode(ThrowNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"throw ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ";\n"});
    }

    public Object visitSynchronizedNode(SynchronizedNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"synchronized (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_EXPR), ") ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_STMT), "\n"});
    }

    public Object visitCatchNode(CatchNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"catch (",
                                                      node.childReturnValueAt(node.CHILD_INDEX_PARAM), ") ",
                                                      node.childReturnValueAt(node.CHILD_INDEX_BLOCK)});
    }

    public Object visitTryNode(TryNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("try ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_BLOCK));

        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_CATCHES));

        if (node.getFinly() != AbsentTreeNode.instance) {
            retList.addLast(" finally ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_FINLY));
        }

        return retList;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return TNLManip.addFirst("null");
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return TNLManip.addFirst("this");
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        LinkedList arrayStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_ARRAY);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getArray(), arrayStringList), "[",
                node.childReturnValueAt(node.CHILD_INDEX_INDEX), "]"});
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return node.childReturnValueAt(node.CHILD_INDEX_NAME);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        LinkedList objectStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_OBJECT);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getObject(), objectStringList), ".",
                node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"super.",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_FTYPE), ".",
                node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }


    /**
     *  Generate code for an access to a field of the enclosing object.
     *  Since 'this' is a Java keyword, using 'this' in the C output
     *  should be free of any danger of naming conflicts in the output code.
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {"this->",
                                                      node.childReturnValueAt(node.CHILD_INDEX_NAME)});
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return TNLManip.arrayToList(new Object[] {
            node.childReturnValueAt(node.CHILD_INDEX_FTYPE), ".class"});
    }


    /** Generate C code for a method call.
     *
     *  @param node The AST node of the method call to be translated. 
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given method call.
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);
        LinkedList retList = new LinkedList();

        ExprNode methodExpr = node.getMethod();
        JavaDecl declaration = JavaDecl.getDecl((TreeNode)methodExpr);
        TreeNode declarationNode = declaration.getSource();
        if ((declarationNode == null) ||
            !(declarationNode instanceof MethodDeclNode)) {
            throw new RuntimeException("Could not resolve method call."
                    + " A dump of the offending AST node follows.\n"
                    + node.toString());
            
        }
        String methodName = (String)(declarationNode.getProperty(C_NAME_KEY));

        // FIXME: currently, we are assuming that this is a 
        // call to a member method of the enclosing object.
        retList.addLast(_indent(node) + methodName + "(this");
        if (!argsList.isEmpty()) 
                retList.addLast(", " + _stringListToString(_commaList(argsList)));
        retList.addLast(")");

        return retList;
    }



    /** Generate C code for an invocation of the 'new' (class instance
     *  allocation) operator.  In other words, generate a call to
     *  the corresponding version of the class constructor function.
     *
     *  @param node The AST node of the 'new' operation to be translated.
     *  @param args Unused placeholder for visitor arguments.
     *  @return Equivalent C code for the given 'new' operation.
     */
    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();
       
        // Extract the C function name of the associated class constructor. 
        JavaDecl declaration = JavaDecl.getDecl(node);
        TreeNode declarationNode = declaration.getSource();
        if ((declarationNode == null) ||
            !(declarationNode instanceof ConstructorDeclNode)) {
            throw new RuntimeException("Could not resolve allocate node."
                    + " A dump of the offending AST node follows.\n"
                    + node.toString());
        }
        String functionName = (String)(declarationNode.getProperty(C_NAME_KEY));
        
        // FIXME figure out what support needs to be done for enclosing
        // instances.    
        int enclosingID = enclosingInstance.classID();
        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) 
        {

            LinkedList enclosingStringList = (LinkedList)
                node.childReturnValueAt(node.CHILD_INDEX_ENCLOSINGINSTANCE);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast(functionName);
        retList.addLast("(");
        List argsList = (List) node.childReturnValueAt(node.CHILD_INDEX_ARGS);
        retList.addLast(_commaList(argsList));
        retList.addLast(")");

        return retList;
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {

        LinkedList retList = new LinkedList();

        retList.addLast("new ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_DTYPE));

        List dimExprList = (List) node.childReturnValueAt(node.CHILD_INDEX_DIMEXPRS);

        Iterator dimExprItr = dimExprList.iterator();

        while (dimExprItr.hasNext()) {
            retList.addLast("[");
            retList.addLast(dimExprItr.next());
            retList.addLast("]");
        }

        for (int dimsLeft = node.getDims(); dimsLeft > 0; dimsLeft--) {
            retList.addLast("[]");
        }

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_INITEXPR));
        }

        return retList;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();

        int enclosingID = enclosingInstance.classID();

        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList)
                node.childReturnValueAt(node.CHILD_INDEX_ENCLOSINGINSTANCE);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast("new ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_SUPERTYPE));
        retList.addLast("(");
        retList.addLast(
                _commaList((List) node.childReturnValueAt(node.CHILD_INDEX_SUPERARGS)));
        retList.addLast(") {\n");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_MEMBERS));
        retList.addLast("}\n");

        return retList;
    }

    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", true);
    }

    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", true);
    }

    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitSingleExprNode(node, "+", false);
    }

    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitSingleExprNode(node, "-", false);
    }

    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "++", false);
    }

    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, "--", false);
    }

    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _visitSingleExprNode(node, "~", false);
    }

    public Object visitNotNode(NotNode node, LinkedList args) {
        return _visitSingleExprNode(node, "!", false);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        return TNLManip.arrayToList(new Object[] {"(",
                                                      node.childReturnValueAt(node.CHILD_INDEX_DTYPE), ") ",
                                                      _parenExpr(node.getExpr(), exprStringList)});
    }

    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "*");
    }

    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "/");
    }

    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "%");
    }

    public Object visitPlusNode(PlusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "+");
    }

    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "-");
    }

    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<<");
    }

    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">>>");
    }

    public Object visitRightShiftArithNode(RightShiftArithNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">>");
    }

    public Object visitLTNode(LTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<");
    }

    public Object visitGTNode(GTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">");
    }

    public Object visitLENode(LENode node, LinkedList args) {
        return _visitBinaryOpNode(node, "<=");
    }

    public Object visitGENode(GENode node, LinkedList args) {
        return _visitBinaryOpNode(node, ">=");
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        return TNLManip.arrayToList(new Object[] {
            _parenExpr(node.getExpr(), exprStringList), " instanceof ",
                node.childReturnValueAt(node.CHILD_INDEX_DTYPE)});
    }

    public Object visitEQNode(EQNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "==");
    }

    public Object visitNENode(NENode node, LinkedList args) {
        return _visitBinaryOpNode(node, "!=");
    }

    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "&");
    }

    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "|");
    }

    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "^");
    }

    public Object visitCandNode(CandNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "&&");
    }

    public Object visitCorNode(CorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, "||");
    }

    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        LinkedList e1StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        LinkedList e2StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);
        LinkedList e3StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR3);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);
        e3StringList = _parenExpr(node.getExpr3(), e3StringList);

        return TNLManip.arrayToList(new Object[] {e1StringList, " ? ",
                                                      e2StringList, " : ", e3StringList});
    }

    public Object visitAssignNode(AssignNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_EXPR1));
        retList.addLast(" = ");
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_EXPR2));
        return retList;
    }

    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "*=");
    }

    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "/=");
    }

    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "%=");
    }

    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "+=");
    }

    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "-=");
    }

    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "<<=");
    }

    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, ">>>=");
    }

    public Object visitRightShiftArithAssignNode(RightShiftArithAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, ">>=");
    }

    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "&=");
    }

    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "^=");
    }

    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, "|=");
    }

    protected LinkedList _visitVarInitDeclNode(VarInitDeclNode node) {
        LinkedList retList = new LinkedList();

        retList.addLast(_indent(node));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_DEFTYPE));
        retList.addLast(" " + node.getName().getIdent());

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" = ");
            retList.addLast(node.childReturnValueAt(node.CHILD_INDEX_INITEXPR));
        }

        retList.addLast(";\n");

        return retList;
    }

    protected LinkedList _visitSingleExprNode(SingleExprNode node, String opString,
            boolean post) {
        LinkedList exprStringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR);

        exprStringList = _parenExpr(node.getExpr(), exprStringList);

        if (post) {
            return TNLManip.arrayToList(new Object[] {exprStringList, opString});
        }

        return TNLManip.arrayToList(new Object[] {opString, exprStringList});
    }

    protected LinkedList _visitBinaryOpNode(BinaryOpNode node, String opString) {
        LinkedList e1StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        LinkedList e2StringList =
            (LinkedList) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);

        return TNLManip.arrayToList(new Object[] {e1StringList, " ",
                                                      opString, " ", e2StringList});
    }

    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node, String opString) {
        List e1StringList = (List) node.childReturnValueAt(node.CHILD_INDEX_EXPR1);
        List e2StringList = (List) node.childReturnValueAt(node.CHILD_INDEX_EXPR2);

        return TNLManip.arrayToList(new Object[] {e1StringList , " ",
                                                      opString, " ", e2StringList});
    }

    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // for testing purposes only
        return new LinkedList();
    }

    protected static LinkedList _commaList(List stringList) {
        return _separateList(stringList, ", ");
    }

    protected static LinkedList _separateList(List stringList,
            String separator) {
        Iterator stringListItr = stringList.iterator();
        LinkedList retList = new LinkedList();

        while (stringListItr.hasNext()) {
            retList.addLast(stringListItr.next());
            if (stringListItr.hasNext()) {
                retList.addLast(separator);
            }
        }

        return retList;
    }



    protected List _forInitStringList(List list) {
        int length = list.size();

        if (length <= 0) return TNLManip.addFirst("");

        TreeNode firstNode = (TreeNode) list.get(0);

        if (firstNode.classID() == LOCALVARDECLNODE_ID) {
            // a list of local variables, with the same type and modifier
            LocalVarDeclNode varDeclNode = (LocalVarDeclNode) firstNode;
            LinkedList retList = new LinkedList();

            retList.addLast(Modifier.toString(varDeclNode.getModifiers()));

            retList.addLast(varDeclNode.getDefType().accept(this, null));
            retList.addLast(" ");

            Iterator declNodeItr = list.iterator();

            while (declNodeItr.hasNext()) {
                LocalVarDeclNode declNode = (LocalVarDeclNode) declNodeItr.next();
                retList.addLast(declNode.getName().getIdent());

                TreeNode initExpr = declNode.getInitExpr();
                if (initExpr != AbsentTreeNode.instance) {
                    retList.addLast(" = ");
                    retList.addLast(initExpr.accept(this, null));
                }

                if (declNodeItr.hasNext()) {
                    retList.addLast(", ");
                }
            }

            return retList;

        } else {
            // Re-traverse the subtrees associated with the list
            // expressions. This has the effect of ignoring indentation
            // level settings.
            return _separateList(
                    TNLManip.traverseList(this, null, list), ", ");
        }
    }



    protected static LinkedList _parenExpr(TreeNode expr, LinkedList exprStrList) {
        int classID = expr.classID();

        switch (classID) {
        case INTLITNODE_ID:
        case LONGLITNODE_ID:
        case FLOATLITNODE_ID:
        case DOUBLELITNODE_ID:
        case BOOLLITNODE_ID:
        case CHARLITNODE_ID:
        case STRINGLITNODE_ID:
        case BOOLTYPENODE_ID:
        case ARRAYINITNODE_ID:
        case NULLPNTRNODE_ID:
        case THISNODE_ID:
        case ARRAYACCESSNODE_ID:
        case OBJECTNODE_ID:
        case OBJECTFIELDACCESSNODE_ID:
        case SUPERFIELDACCESSNODE_ID:
        case TYPEFIELDACCESSNODE_ID:
        case THISFIELDACCESSNODE_ID:
        case TYPECLASSACCESSNODE_ID:
        case OUTERTHISACCESSNODE_ID:
        case OUTERSUPERACCESSNODE_ID:
        case METHODCALLNODE_ID:
        case ALLOCATENODE_ID:
        case ALLOCATEANONYMOUSCLASSNODE_ID:
        case ALLOCATEARRAYNODE_ID:
        case POSTINCRNODE_ID:
        case POSTDECRNODE_ID:
        case UNARYPLUSNODE_ID:
        case UNARYMINUSNODE_ID:
        case PREINCRNODE_ID:
        case PREDECRNODE_ID:
        case COMPLEMENTNODE_ID:
        case NOTNODE_ID:
            return exprStrList;

        default:
            return TNLManip.arrayToList(new Object[] {"(", exprStrList, ")"});
        }
    }

    protected static String _stringListToString(List stringList) {
        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List) {
                // only use separators for top level
                sb.append(_stringListToString((List) stringObj));
            } else if (stringObj instanceof String) {
                sb.append((String) stringObj);
            } else {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }

    /** Print information pertaining to a child of a node in
     *  in an abstract syntax tree. The information printed for
     *  each child tree node includes object class information, 
     *  and the return value of the last visit to the node. 
     *
     *  Example call: 
     *  _debugChildReturnValue(classNode, classNode.CHILD_INDEX_MEMBERS)
     * 
     *  @param node The parent of the AST child whose information is
     *  to be printed. 
     *  @index The index of the child to be printed. If the index
     *  references a "hierarchical" tree node (a list), then information 
     *  pertaining to all elements of the list will be printed. 
     */
    protected void _debugChildReturnValue(TreeNode node, int index) {
        Object child = node.getChild(index);
        Object childReturnVal = node.childReturnValueAt(index);
        System.out.println("------- ccg debug: child information --------");
        System.out.println("Child class: " + child.getClass().getName());
        System.out.println("Child return value: ");
        if (childReturnVal instanceof List) {
            System.out.println(_stringListToString((List)childReturnVal));
        }
        if (child instanceof List) {  
            System.out.print("The child is a list. ");
            System.out.println("A description of each list element follows.");
            Iterator elements = ((List)child).iterator();
            while (elements.hasNext()) {
                Object element = elements.next();
                System.out.print("List element class: ");
                System.out.println(element.getClass().getName());
            }
        }
        System.out.println("----------------------------------------------");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Given a tree node for a Java class declaration, return the
     *  name of the declared class. This operation is necessary only
     *  before the AST subtree associated with the class declaration
     *  has been fully traversed.
     *  @param node The tree node associated with the class declaration.
     *  @string A string containing the class name.
     */
    private String _declaredClassNameOf(ClassDeclNode node) {
        NameNode nameNode = (NameNode)(node.getChild(node.CHILD_INDEX_NAME));
        String ident = nameNode.getIdent();
        TreeNode qualifier = nameNode.getQualifier();
        if (qualifier != AbsentTreeNode.instance) {
            // FIXME: Include support for qualifiers.
            System.out.println("Class name qualifiers not supported yet!");
        }
        return ident; 
    }

    /**
     *  Generate C code for a tree node that can be either a statement
     *  or a block (e.g., the then part of an if statement).
     *  The code is added to the given linked list. Code for
     *  a statement is placed on a separate line, and indented 
     *  by one position beyond the indentation
     *  effected by the associated node indentation levels.
     *  @param parent The parent of the statement-or-node block for
     *  which C code is to be generated.
     *  @param index The index of the child for which code is to be generated.
     *  @param retList The linked list to which the generated code is to
     *  be appended.
     */
    void _generateStmtOrBlock(TreeNode parent, int childIndex, 
            LinkedList retList) {
        Object child = parent.getChild(childIndex);
        if ((parent == null) || (child == null)) return;

        if (child instanceof BlockNode) {
            retList.addLast(parent.childReturnValueAt(childIndex));
        }
        else {
            retList.addLast("\n");
            retList.addLast(_indent(1));
            retList.addLast(parent.childReturnValueAt(childIndex));
        }
    }


    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given 
     *  indentation level.
     */  
    private String _indent(int level) {
        StringBuffer indent = new StringBuffer();
        int i;
        for (i = 0; i < level; i++) {
            indent.append("    ");
        }
        return indent.toString();
    }
   
    /** Return a string that generates an indentation string (a sequence
     *  of spaces) based on the indentation level of the given AST node. 
     *  @param node The AST node.
     *  @return The indentation string that corresponds to the 
     *  indentation level of the given AST node.
     */  
    private String _indent(TreeNode node) {
        int level = ((Integer)(node.getProperty(INDENTATION_KEY))).intValue();
        return _indent(level);
    }



}
