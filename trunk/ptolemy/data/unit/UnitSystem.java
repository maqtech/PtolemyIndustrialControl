/* A unit system as defined by a set of base and derived units.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

package ptolemy.data.unit;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.type.BaseType;
import ptolemy.math.Complex;

import java.util.Hashtable;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// UnitSystem
/**
A unit system as defined by a set of base and derived units.
<p>
The various measurement units of a unit system are represented by the
parameters of an instance of UnitSystem.
The units belong to a number of categories, such as length and time
in the International System of Units (SI). Each category has a base unit,
for example meter in the length category.
<p>
Several basic unit systems are provided with Ptolemy II. They are specified
using MoML. Customized unit systems can be created following these examples.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.0
*/

public class UnitSystem extends ScopeExtendingAttribute {

    /** Construct a unit system with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public UnitSystem(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Register the specified unit category.
     *  If the category is not already registered, assign a unique index
     *  for the category.
     *  @param category The unit category to be registered.
     */
    public static void addUnitCategory(UnitCategory category) {

	Integer index = (Integer)_indexTable.get(category);
	if (index != null) {
            return;
	} else {
	    index = new Integer(_categories);
	    _indexTable.put(category, index);
            ++_categories;
            _categoryVector.add(category);
	}
    }

    /** Return the name of the base unit of the specified category.
     *  @param categoryIndex The index of the unit category.
     *  @return The name of the base unit of the category.
     */
    public static String getBaseUnitName(int categoryIndex) {
	if (categoryIndex < 0 || categoryIndex >= _categories) {
	    return "unknown";
	} else {
            UnitCategory category =
                    (UnitCategory)_categoryVector.elementAt(categoryIndex);
            if (category != null) {
                return ((BaseUnit)category.getContainer()).getName();
            } else {
                return "unknown";
            }
	}
    }

    /** Return the index assigned to the specified unit category.
     *  @param category The unit category.
     *  @return The index assigned to the category.
     */
    public static int getUnitCategoryIndex(UnitCategory category) {
	Integer index = (Integer)_indexTable.get(category);
	if (index == null) {
	    //FIXME: throw an exception?
	    return -1;
	} else {
	    return index.intValue();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The hash table that maps the name of a unit category to its index.
    private static Hashtable _indexTable = new Hashtable();

    // The number of registered unit categories.
    private static int _categories = 0;

    // The vector that contains all registered categories ordered by index.
    private static Vector _categoryVector = new Vector();
}
