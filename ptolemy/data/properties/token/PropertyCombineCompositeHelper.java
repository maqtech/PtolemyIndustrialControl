/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2009 The Regents of the University of California.
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
*/
package ptolemy.data.properties.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.data.properties.PropertyHelper;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * PropertyCombineCompositeHelper class.
 *
 * @author mankit
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PropertyCombineCompositeHelper extends PropertyCombineHelper {

    public PropertyCombineCompositeHelper(PropertyCombineSolver solver,
            Object component) {
        super(solver, component);
    }

    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        List<PropertyHelper> adapters = new ArrayList<PropertyHelper>();

        CompositeEntity component = (CompositeEntity) getComponent();
        Iterator iterator = component.entityList().iterator();

        while (iterator.hasNext()) {
            NamedObj actor = (NamedObj) iterator.next();

            adapters.add(_solver.getHelper(actor));
        }

        return adapters;
    }

}
