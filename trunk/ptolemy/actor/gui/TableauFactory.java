/* An object that can create a tableau for a model.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// TableauFactory
/**
A configuration contains an instance of this class, and uses it to create
a tableau for a model represented by an effigy.  This base class assumes
that it contains other tableau factories. Its createTableau() method defers
to each contained factory in order until one is capable of creating a
tableau for the specified effigy.  Subclasses of this class will usually
be inner classes of a Tableau, and will create the Tableau.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Configuration
@see Effigy
@see Tableau
*/
public class TableauFactory extends CompositeEntity {

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TableauFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified effigy. The tableau will
     *  created with a new unique name with the specified effigy as its
     *  container.  If this factory cannot create a tableau
     *  for the given effigy (perhaps because the effigy is not of the
     *  appropriate subclass), then return null.  This base class assumes
     *  that it contains other tableau factories. This method defers
     *  to each contained factory in order until one is capable of creating a
     *  tableau for the specified effigy.  Subclasses of this class will
     *  usually be inner classes of a Tableau, and will create the Tableau.
     *  @param effigy The model effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    public Tableau createTableau(Effigy effigy) throws Exception {
	Tableau tableau = null;
	Iterator factories = entityList(TableauFactory.class).iterator();
	while(factories.hasNext() && tableau == null) {
	    TableauFactory factory = (TableauFactory)factories.next();
	    tableau = factory.createTableau(effigy);
	}
	return tableau;
    }
}
