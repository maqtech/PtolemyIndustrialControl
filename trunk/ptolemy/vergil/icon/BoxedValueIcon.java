/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2002 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import ptolemy.kernel.util.*;

import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

//////////////////////////////////////////////////////////////////////////
//// BoxedValueIcon
/**
An icon that displays the value of an attribute of the container in a box
that resizes according to the width of the attribute value.
The attribute is assumed to be an instance of Settable, and its name
is given by the parameter <i>attributeName</i>.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class BoxedValueIcon extends AttributeValueIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public BoxedValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to remember the background figure so that it can center the label
     *  over it in createFigure().
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        String displayString = _displayString();
        double width = 60;
        if (displayString != null) {
            // Measure width of the text.  Unfortunately, this
            // requires generating a label figure that we will not use.
            LabelFigure label = new LabelFigure(displayString,
                    _labelFont, 1.0, SwingConstants.CENTER);
            Rectangle2D stringBounds = label.getBounds();
            // NOTE: Padding of 20.
            width = stringBounds.getWidth() + 20;
        }
        _background = new BasicRectangle(0, 0, width, 30, Color.white, 1);
        return _background;
    }
}
