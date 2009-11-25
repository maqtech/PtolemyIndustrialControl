/** A lattice node representing SystemC long double type.

 Copyright (c) 2009 The Regents of the University of California.
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

import ptolemy.data.DoubleToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.data.properties.lattice.TypeProperty;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Property

/**
 A lattice node representing SystemC long double type.

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class LongDouble extends LatticeProperty implements TypeProperty {

    /** Construct a node named "LongDouble" in the lattice.
     *  @param lattice The lattice in which the node is to be constructed.   
     */   
    public LongDouble(PropertyLattice lattice) {
        super(lattice, "LongDouble");
    }

    /** Maximum value of a long double in System C.
     *  Note that this may or may not have the value equal
     *  to that of java.lang.Double.MAX_VALUE.
     *  @return The maximum value of a long double in System C.   
     */
    public Token getMaxValue() {
        // FIXME: Is there a java representation for the C long double type?
        return new DoubleToken(1.7976931348623157E308);
    }

    /** Minimum value of a long double in System C.
     *  Note that this may or may not have the value equal
     *  to that of java.lang.Double.MIN_VALUE.
     *  @return The minimum value of a long double in System C.   
     */
    public Token getMinValue() {
        // FIXME: Is there a java representation for the C long double type?
        return new DoubleToken(2.2250738585072016E-308);
    }

    /** Return true if this element has minimum and maximum values.
     *  @return Always return true.
     */
    public boolean hasMinMaxValue() {
        return true;
    }

//    public boolean isInRange(Token token) throws IllegalActionException {
//         // FIXME: Findbugs: Unchecked/unconfirmed cast.
//         // The problem here is that token might not be a ScalarToken.
//         // Is this method used?  Perhaps it can be removed.
//         if ((((ScalarToken) token).doubleValue() < ((ScalarToken) getMinValue())
//                 .doubleValue())
//                 || (((ScalarToken) token).doubleValue() > ((ScalarToken) getMaxValue())
//                         .doubleValue())) {
//
//             return false;
//         } else {
//             return true;
//         }
//     }
}
