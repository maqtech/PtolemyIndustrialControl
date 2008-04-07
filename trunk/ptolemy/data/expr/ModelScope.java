/* An interface used by the expression parser for identifier lookup.

 Copyright (c) 2001-2007 The Regents of the University of California.
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
package ptolemy.data.expr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ModelScope

/**
 An abstract class that is useful for implementing expression language
 scopes for Ptolemy models.

 @author Xiaojun Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 @see ptolemy.data.expr.PtParser
 */
public abstract class ModelScope implements ParserScope {
    /** Return a list of object names in scope for variables in the
     * given container.
     * @param container The container of this scope.
     */
    public static Set getAllScopedObjectNames(NamedObj container) {
        Set identifiers = new HashSet();
        identifiers.add("this");
        while (container != null) {
            for (Object attribute : container.attributeList()) {
                identifiers.add(((Attribute) attribute).getName());
            }
            if (container instanceof Entity) {
                for (Object port : ((Entity) container).portList()) {
                    identifiers.add(((Port) port).getName());
                }
            }
            if (container instanceof CompositeEntity) {
                for (Object entity : ((CompositeEntity) container)
                        .entityList()) {
                    identifiers.add(((Entity) entity).getName());
                }

                for (Object relation : ((CompositeEntity) container)
                        .relationList()) {
                    identifiers.add(((Relation) relation).getName());
                }
            }
            container = container.getContainer();
        }

        return identifiers;
    }

    /** Return a list of variable names in scope for variables in the
     * given container.  Exclude the given variable from being
     * considered in scope.
     * @param exclude  The variable to exclude from the scope.
     * @param container The container of this scope.
     */
    public static Set getAllScopedVariableNames(Variable exclude,
            NamedObj container) {
        List variableList = container.attributeList(Variable.class);
        variableList.remove(exclude);

        Set nameSet = new HashSet();

        for (Iterator variables = variableList.iterator(); variables.hasNext();) {
            Variable variable = (Variable) variables.next();
            nameSet.add(variable.getName());
        }

        // Get variables higher in scope.  Moving up the hierarchy
        // terminates when the container is null.
        NamedObj aboveContainer = container.getContainer();

        if (aboveContainer != null) {
            nameSet.addAll(getAllScopedVariableNames(exclude, aboveContainer));
        }

        // Get variables in scope extenders.  Moving down the scope
        // extenders terminates at hierarchy leaves.
        Iterator extenders = container.attributeList(ScopeExtender.class)
                .iterator();

        while (extenders.hasNext()) {
            ScopeExtender extender = (ScopeExtender) extenders.next();

            // It would be nice if ScopeExtender and NamedObj were common in
            // some way to avoid this cast.
            nameSet.addAll(getAllScopedVariableNames(exclude,
                    (NamedObj) extender));
        }

        return nameSet;
    }

    /** Get the NamedObj with the given name in the scope of the given
     *  container.  If the name contains the "::" scoping specifier,
     *  then an attribute more deeply in the hierarchy is searched
     *  for.
     *  @param container The container to search upwards from.
     *  @param name The object name to search for.
     *  @return The NamedObj with the given name or null if the NamedObj
     *  does not exist.
     */
    public static NamedObj getScopedObject(NamedObj container, String name) {
        NamedObj reference = container;
        if (name.equals("this")) {
            return reference;
        }

        String[] parts = name.replaceAll("::", ".").split("\\.");
        NamedObj result = null;
        boolean lookup = true;
        for (String part : parts) {
            result = null;
            while (reference != null) {
                Attribute attribute = reference.getAttribute(part);
                if (attribute != null) {
                    result = attribute;
                } else {
                    if (reference instanceof Entity) {
                        Port port = ((Entity) reference).getPort(part);
                        if (port != null) {
                            result = port;
                        } else if (reference instanceof CompositeEntity) {
                            ComponentEntity entity =
                                ((CompositeEntity) reference).getEntity(part);
                            if (entity != null) {
                                result = entity;
                            } else {
                                ComponentRelation relation = ((CompositeEntity)
                                        reference).getRelation(part);
                                if (relation != null) {
                                    result = relation;
                                }
                            }
                        }
                    }
                }
                if (lookup && result == null) {
                    reference = reference.getContainer();
                } else {
                    break;
                }
            }
            if (result == null) {
                break;
            }
            reference = result;
            lookup = false;
        }

        return result;
    }

    /** Get the variable with the given name in the scope of the given
     *  container.  If the name contains the "::" scoping specifier,
     *  then an attribute more deeply in the hierarchy is searched
     *  for.  The scope of the object includes any container of the
     *  given object, and any variable contained in a scope extending
     *  attribute inside any of those containers.
     *  @param exclude A variable to exclude from the search.
     *  @param container The container to search upwards from.
     *  @param name The variable name to search for.
     *  @return The variable with the given name or null if the variable
     *  does not exist.
     */
    public static Variable getScopedVariable(Variable exclude,
            NamedObj container, String name) {
        String insideName = name.replaceAll("::", ".");

        while (container != null) {
            Variable result = _searchIn(exclude, container, insideName);

            if (result != null) {
                return result;
            } else {
                container = container.getContainer();
            }
        }

        return null;
    }

    // Search in the container for an attribute with the given name.
    // Search recursively in any instance of ScopeExtender in the
    // container.
    private static Variable _searchIn(Variable exclude, NamedObj container,
            String name) {
        Attribute result = container.getAttribute(name);

        if ((result != null) && result instanceof Variable
                && (result != exclude)) {
            return (Variable) result;
        } else {
            Iterator extenders = container.attributeList(ScopeExtender.class)
                    .iterator();

            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender) extenders.next();
                result = extender.getAttribute(name);

                if ((result != null) && result instanceof Variable
                        && (result != exclude)) {
                    return (Variable) result;
                }

                // Should not return null here. The next extender should be
                // searched. (tfeng)
                // return null;
            }
        }

        return null;
    }
}
