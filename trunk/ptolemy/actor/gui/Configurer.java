/* An attribute that manages configuring its container.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports.
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

// Java imports.
import java.awt.Component;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// Configurer
/**
This is an attribute that manages configuring its container.
It serves as a factory producing widgets for interactively editing
the configuration of the container (these are called "configuration
widgets").

In this base class, the createEditPane() method creates an
instance of PtolemyQuery with one entry for each parameter in
the container of this configurer.  This is the default mechanism
for editing parameters.  Derived classes may override this
method to present radically different interfaces to the user.
For example, a digital filter actor could present a filter
design interface.  A plotter actor could present a panel for
configuring a plot.  A file reader actor could present a file
browser.

A GUI for Ptolemy II should use the convenience method consolidate(),
which obeys the following policy.
To edit the parameters of any instance of NamedObj, it
first checks to see whether that NamedObj contains an instance
of Configurer (using the attributesList(filter: Class) method).
If it contains no configurer, then it creates an
instance of this base class configurer.  It then returns
a panel containing the configuration widgets specified by
each contained configurer, stacked vertically if there is more
than one.  A GUI should typically insert this panel into a
a dialog box and present it to the user.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class Configurer extends Attribute {

    /** Construct a configurer with the specified container and name.
     *  @param container The container.
     *  @param name The name of the configurer.
     *  @exception IllegalActionException If the configurer is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */	
    public Configurer(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Create configuration widgets specified by all the instances
     *  of Configurer in the specified object.  These are consolidated
     *  into a single panel, stacked vertically.
     *  @return A panel containing configuration widgets.
     *  @exception IllegalActionException If a configurer is not of an
     *   acceptable attribute for the container.
     */
    public static JPanel createEditor(NamedObj object)
            throws IllegalActionException {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

        boolean foundOne = false;
        Iterator configurers
                = object.attributeList(Configurer.class).iterator();
        while (configurers.hasNext()) {
            foundOne = true;
            Configurer configurer = (Configurer)configurers.next();
            result.add(configurer.createEditPane());
        }
        if (!foundOne) {
            try {
                Configurer configurer = new Configurer(object,
                        object.uniqueName("configurer"));
                result.add(configurer.createEditPane());
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(ex.toString());
            }
        }
        return result;
    }

    /** Return a new widget for configuring the container.
     *  @return A new widget for configuring the container.
     */
    public Component createEditPane() {
        PtolemyQuery query = new PtolemyQuery();
        // FIXME: The following doesn't work... why?
        query.setTextWidth(20);
        NamedObj container = (NamedObj)getContainer();
        Iterator params
               = container.attributeList(Parameter.class).iterator();
        boolean foundOne = false;
        while (params.hasNext()) {
            foundOne = true;
            Parameter param = (Parameter)params.next();
            // FIXME: Check for ParameterConfigurer.
            query.addLine(param.getName(),
                   param.getName(),
                   param.stringRepresentation());
            query.attachParameter(param, param.getName());
        }
        if (!foundOne) {
            return new JLabel(container.getName() + " has no parameters.");
        }
        // FIXME: should this build in a mechanism for adding
        // parameters?  Perhaps that should be in CompositeActor...
        return query;
    }
}
