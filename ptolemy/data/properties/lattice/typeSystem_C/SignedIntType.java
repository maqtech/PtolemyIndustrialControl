/** A base class representing a property.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.typeSystem_C;

import ptolemy.data.LongToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.data.properties.lattice.TypeProperty;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Property

/**
 A base class representing a property.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class SignedIntType extends LatticeProperty implements
        TypeProperty {

    public SignedIntType(PropertyLattice lattice) {
        super(lattice, "SignedIntType");
    }
    
    // 09/21/09 - Charles Shelton
    // Additional constructor needed for subclasses that inherit from SignedIntType
    // so that they can also set their name member when declared.
    public SignedIntType(PropertyLattice lattice, String name) {
        super(lattice, name);
    }

    public abstract short getNumberBits();

    public boolean isSigned() {
        return true;
    }

    public Token getMaxValue() {
        return new LongToken((long) Math.pow(2, getNumberBits() - 1) - 1);
    }

    public Token getMinValue() {
        return new LongToken((long) -Math.pow(2, getNumberBits() - 1));
    }

    public boolean hasMinMaxValue() {
        return true;
    }

    public boolean isInRange(Token token) throws IllegalActionException {
        if ((((ScalarToken) token).longValue() < ((ScalarToken) getMinValue())
                .longValue())
                || (((ScalarToken) token).longValue() > ((ScalarToken) getMaxValue())
                        .longValue())) {

            return false;
        } else {
            return true;
        }
    }
}
