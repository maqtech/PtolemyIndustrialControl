/* An interface used by the expression parser for identifier lookup.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import java.util.Iterator;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ModelScope
/**
An abstract class that is useful for implementing expression language
scopes for Ptolemy models.

@author Xiaojun Liu, Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.PtParser
*/

public abstract class ModelScope implements ParserScope {

    /** Get the variable with the given name in the scope of the given
     *  container.  The scope of the object includes any container of the
     *  given object, and any variable contained in a scope extending
     *  attribute inside any of those containers.  If no variable exists
     *  with the given name, then return null.
     *  @param exclude A variable to exclude from the search.
     *  @param container The container to search upwards from.
     *  @param name The variable name to search for.
     */
    public static Variable getScopedVariable(
            Variable exclude, NamedObj container, String name) {
        while (container != null) {
            Variable result = _searchIn(exclude, container, name);
            if (result != null) {
                return result;
            } else {
                container = (NamedObj)container.getContainer();
            }
        }
        return null;
    }

    // Search in the container for an attribute with the given name.
    // Search recursively in any instance of ScopeExtender in the
    // container.
    private static Variable _searchIn(Variable exclude,
            NamedObj container, String name) {
        Attribute result = container.getAttribute(name);
        if (result != null
                && result instanceof Variable
                && result != exclude) {
            return (Variable)result;
        } else {
            Iterator extenders =
                container.attributeList(ScopeExtender.class).iterator();
            while (extenders.hasNext()) {
                ScopeExtender extender = (ScopeExtender)extenders.next();
                result = extender.getAttribute(name);
                if (result != null
                        && result instanceof Variable
                        && result != exclude) {
                    return (Variable)result;
                }
                return null;
            }
        }
        return null;
    }
}

