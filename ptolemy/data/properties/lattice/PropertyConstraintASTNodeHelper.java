/** A base class representing a property constraint adapter.

 Copyright (c) 2007-2010 The Regents of the University of California.
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


 */
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PropertyConstraintHelper

/**
 A base class representing a property constraint adapter.

 @author Man-Kit Leung, Thomas Mandl, Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintASTNodeHelper extends PropertyConstraintHelper {

    /**
     * Construct the property constraint adapter associated
     * with the given AST node.
     * @param node The given AST node.
     * @exception IllegalActionException Thrown if
     * PropertyConstraintHelper(NamedObj, ASTPtRootNode, boolean)
     * throws it.
     */
    public PropertyConstraintASTNodeHelper(PropertyConstraintSolver solver,
            ASTPtRootNode node) throws IllegalActionException {
        this(solver, node, true);
    }

    /**
     * Construct the property constraint adapter for the given
     * property solver and AST node.
     * @param solver The given component.
     * @param node The given AST node.
     * @param useDefaultConstraints Indicate whether this adapter
     *  uses the default actor constraints.
     * @exception IllegalActionException If the adapter cannot
     *  be initialized.
     */
    public PropertyConstraintASTNodeHelper(PropertyConstraintSolver solver,
            ASTPtRootNode node, boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, node, useDefaultConstraints);
    }

    /** Return the constraints of this component.  The constraints is
     *  a list of inequalities. This base class returns a empty list.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        boolean constraintParent = (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET)
                || (interconnectConstraintType == ConstraintType.SINK_EQUALS_GREATER);

        if (getComponent() instanceof ASTPtLeafNode) {
            ASTPtLeafNode node = (ASTPtLeafNode) getComponent();

            if (node.isConstant() && node.isEvaluated()) {
                //FIXME: Should be handled by use-case specific adapters.
                // We should make a (abstract) method that forces use-case
                // to implement this.
            } else {

                NamedObj namedObj = getNamedObject(getContainerEntity(node),
                        node.getName());

                if (namedObj != null) {
                    // Set up one-direction constraint.
                    if (constraintParent) {
                        setAtLeastByDefault(node, namedObj);
                    } else {
                        setAtLeastByDefault(namedObj, node);
                    }

                } else {
                    // Cannot resolve the property of a label.
                    assert false;
                }
            }
        }

        if (_useDefaultConstraints) {
            ASTPtRootNode node = (ASTPtRootNode) getComponent();
            List<Object> children = new ArrayList<Object>();

            boolean isNone = interconnectConstraintType == ConstraintType.NONE;

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {

                if (!constraintParent && !isNone) {
                    // Set child >= parent.
                    setAtLeastByDefault(node.jjtGetChild(i), node);

                } else {
                    children.add(node.jjtGetChild(i));
                }
            }

            if (constraintParent) {
                _constraintObject(interconnectConstraintType, node, children);
            }
        }

        return _union(_ownConstraints, _subHelperConstraints);
    }

    //    /**
    //     * @param node The given AST node.
    //     * @return The term
    //     * @exception IllegalActionException
    //     */
    //    public InequalityTerm[] getChildrenTerm(ptolemy.data.expr.ASTPtRootNode node) throws IllegalActionException {
    //        InequalityTerm children[] =
    //            new InequalityTerm[node.jjtGetNumChildren()];
    //
    //        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
    //            Object child = node.jjtGetChild(i);
    //
    //            PropertyConstraintASTNodeHelper adapter;
    //
    //            adapter = (PropertyConstraintASTNodeHelper) _solver.getHelper(child);
    //            children[i] = adapter.getPropertyTerm(child);
    //
    //        }
    //        return children;
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return a list of property-able NamedObj contained by
     * the component. All ports and parameters are considered
     * property-able.
     * @return The list of property-able named object.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();
        list.add(getComponent());
        return list;
    }

    /**
     * Return the list of sub-adapters. In this base class,
     * return an empty list.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected List<PropertyHelper> _getSubHelpers() {
        return new ArrayList<PropertyHelper>();
    }

    public static NamedObj getNamedObject(Entity container, String name) {
        // Check the port names.
        TypedIOPort port = (TypedIOPort) container.getPort(name);

        if (port != null) {
            return port;
        }

        Variable result = ModelScope.getScopedVariable(null, container, name);

        if (result != null) {
            return result;
        }
        return null;
    }

    protected ASTPtRootNode _getNode() {
        return (ASTPtRootNode) getComponent();
    }

    public void setAtLeastByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 1);
            _solver.incrementStats("# of AST default constraints", 1);
        }
    }

    public void setSameAsByDefault(Object term1, Object term2) {
        setSameAs(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 2);
            _solver.incrementStats("# of AST default constraints", 2);
        }
    }
}
