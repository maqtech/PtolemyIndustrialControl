/* Class representing a type change for one Typeable object.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TypeEvent
/**
   A TypeEvent represents a type change on a Typeable object.  This event is
   generated by the Typeable whose type is changed, and is passed to the type
   change listeners to notify them about the change.

   @author Yuhong Xiong
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (yuhong@eecs.berkeley.edu)
   @Pt.AcceptedRating Green (cxh@eecs.berkeley.edu)
   @see TypeListener
*/

public class TypeEvent implements DebugEvent {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Construct a TypeEvent, with the specified Typeable and the
     *  old and new types.
     *  @param typeable The Typeable whose type is changed.
     *  @param oldType The type of the Typeable before the change.
     *  @param newType The type of the Typeable after the change.
     */
    public TypeEvent(Typeable typeable, Type oldType, Type newType) {
        _typeable = typeable;
        _oldType = oldType;
        _newType = newType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the new type.
     *  @return The type of the Typeable after the change.
     */
    public Type getNewType() {
        return _newType;
    }

    /** Return the old type.
     *  @return The type of the Typeable before the change.
     */
    public Type getOldType() {
        return _oldType;
    }

    /** Return the Typeable whose type is changed.
     *  @return The Typeable whose type is changed.
     */
    public NamedObj getSource() {
        return (NamedObj)_typeable;
    }

    /** Return a string description for this type change. The string is
     *  "The type on <typeable> has changed from <old type> to <new type>",
     *  where <typeable> is the name of the Typeable, and <old type> and
     *  <new type> are the string representation of the types.
     *  @return A string description for this type change.
     */
    public String toString() {
        return "The type on " + getSource().getFullName() + " has changed "
            + "from " + _oldType.toString() + " to " + _newType.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                    ////

    private Typeable _typeable;
    private Type _oldType;
    private Type _newType;
}
