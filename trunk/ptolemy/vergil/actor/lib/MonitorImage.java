/* Monitor image inputs (java.awt.Image) by displaying them in the icon.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.vergil.actor.lib;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.vergil.icon.ImageEditorIcon;

import java.awt.Image;

//////////////////////////////////////////////////////////////////////////
//// MonitorImage
/**
Monitor image inputs (java.awt.Image) by displaying them in the icon.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 0.3
*/

public class MonitorImage extends Sink {

    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorImage(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);

        _icon = new ImageEditorIcon(this, "_icon");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from the input and record its value.
     *  @exception IllegalActionException If the input token does not
     *   contain an image, or if there is no director.
     *  @return True.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken token = (ObjectToken)input.get(0);
            Object value = token.getValue();
            if (!(value instanceof Image)) {
                throw new IllegalActionException(this,
                        "Received a token that does not contain an image.");
            }
            _icon.setImage((Image)value);
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The image icon.
    private ImageEditorIcon _icon;
}
