/** A class that solves constraints on DimensionTypes.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.graph.InequalityTerm;
import ptolemy.graph.Inequality;	/* Needed for javadoc */ 
import ptolemy.kernel.util.IllegalActionException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DimensionTypeResolver
/**
This class solves constraints on DimensionType objects.  After executing
the resolveTypes method, the dimension type of all the appropriate Typeable 
objects will be fully resolved unless a type conflict occured.

@author Steve Neuendorffer
$Id$

*/

public class DimensionTypeResolver 
{

    /** Check types on all the connections and resolve undeclared types.
     *  This method is not write-synchronized on the workspace, so one
     *  of its calling methods should be (normally Manager.resolveTypes()).
     * 
     *  @return An enumeration of Typeable objects that failed type checking.
     *  @exception InternalErrorException If a constraint is given that
     *  does not fall on DimensionTypes.
     */
    public Enumeration resolveTypes(Enumeration constraints) {
        // FIXME should ensure that all the constraints act on 
        // DimensionTypes.

        LatticeTypeResolver resolver = 
            new LatticeTypeResolver(DimensionTypeLattice.getInstance());
        return resolver.resolveTypes(constraints);
    }
}

