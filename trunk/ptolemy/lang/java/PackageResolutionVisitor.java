/* PackageResolutionVisitor  

 Copyright (c) 2000 The Regents of the University of California.
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


package ptolemy.lang.java;

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// PackageResolutionVisitor
/**  Resolve packages.
Pass 0: Package resolution, done by PackageResolutionVisitor consists of three
steps:
<ol>
<li> Creation of type environments, done by ResolvePackageVisitor. These
environments are members of class declarations (ClassDecl), which are
created during this step.
<li> Resolution of imports, done by ResolveImportsVisitor.
<li> Resolution of type names, done by ResolveTypesVisitor.
</ol>
Additional classes may be read in during pass 0.

@see ResolvePackageVisitor
@see ResolveImportsVisitor
@see ResolveTypesVisitor

@author Jeff Tsay
@version $Id$
 */
public class PackageResolutionVisitor extends JavaVisitor
    implements JavaStaticSemanticConstants {

    public PackageResolutionVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        PackageDecl thePkgDecl;
        TreeNode pkgDeclNode = node.getPkg();

        if (pkgDeclNode == AbsentTreeNode.instance) {
            thePkgDecl = StaticResolution.UNNAMED_PACKAGE;
        } else {
            NameNode name = (NameNode) StaticResolution.resolveAName(
                    (NameNode) pkgDeclNode,
                    StaticResolution.SYSTEM_PACKAGE.getEnviron(), null, null, CG_PACKAGE);
            thePkgDecl = (PackageDecl) name.getDefinedProperty(DECL_KEY);
        }

        node.setProperty(PACKAGE_KEY, thePkgDecl);

        // build environment for this file
        Environ importOnDemandEnv = new Environ(
                StaticResolution.SYSTEM_PACKAGE.getEnviron());

        Environ pkgEnv = new Environ(importOnDemandEnv);

        pkgEnv.copyDeclList(thePkgDecl.getEnviron());

        Environ environ = new Environ(pkgEnv); // the file level environment
        node.setProperty(ENVIRON_KEY, environ);

        node.accept(new ResolvePackageVisitor(), null);

        node.accept(new ResolveTypesVisitor(), null);

        return null;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("PackageResolution not defined on node type : " +
                node.getClass().getName());
    }
}
