/* A set of static functions to be used by Ptera events.

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.ContainmentExtender;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// EventUtils

/**
 A set of static functions to be used by Ptera events.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class EventUtils {

    public static void closeTableau(final Tableau tableau) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tableau.close();
            }
        });
    }

    /** Find the effigy associated with the top level of the object, and if not
     *  found but the top level has a ContainmentExtender attribute, use that
     *  attribute to find the containment extender of the top level and continue
     *  the search.
     *
     *  @param object The object.
     *  @return The effigy, or null if not found.
     *  @exception IllegalActionException If attributes cannot be retrieved, or
     *   the container that an attribute points to is invalid.
     */
    public static Effigy findToplevelEffigy(NamedObj object)
            throws IllegalActionException {
        NamedObj toplevel;
        do {
            toplevel = object.toplevel();
            Effigy effigy = Configuration.findEffigy(toplevel);
            if (effigy != null) {
                return effigy;
            }
            ContainmentExtender extender = (ContainmentExtender)
                    toplevel.getAttribute("_containmentExtender",
                            ContainmentExtender.class);
            object = toplevel;
            if (extender != null) {
                object = extender.getExtendedContainer();
            }
        } while (toplevel != object);
        return null;
    }

    public static Tableau getTableau(Event event,
            StringParameter referredTableau, TableauParameter defaultTableau)
            throws IllegalActionException {
        TableauParameter parameter = getTableauParameter(event, referredTableau,
                defaultTableau);
        if (parameter == null) {
            throw new IllegalActionException("referredTableau has not been " +
                    "specified in " + event.getName() + ".");
        }
        Tableau tableau = (Tableau) ((ObjectToken) parameter.getToken())
                .getValue();
        return tableau;
    }

    public static TableauParameter getTableauParameter(Event event,
            StringParameter referredTableau, TableauParameter defaultTableau)
            throws IllegalActionException {
        String tableauName = referredTableau.stringValue().trim();
        if (tableauName.equals("")) {
            return defaultTableau;
        } else {
            Variable variable = ModelScope.getScopedVariable(null, event,
                    tableauName);
            if (variable == null || !(variable instanceof TableauParameter)) {
                throw new IllegalActionException(event, "Unable to find " +
                        "variable with name \"" + tableauName + "\", or the " +
                        "variable is not an instanceof TableauParameter.");
            }
            return (TableauParameter) variable;
        }
    }

    public static void setTableau(Event event, StringParameter referredTableau,
            TableauParameter defaultTableau, Tableau tableau)
            throws IllegalActionException {
        ObjectToken token = new ObjectToken(tableau, Tableau.class);
        getTableauParameter(event, referredTableau, defaultTableau).setToken(
                token);
    }
}
