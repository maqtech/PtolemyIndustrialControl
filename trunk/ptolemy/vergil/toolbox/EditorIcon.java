/* An Icon is the graphical representation of an entity.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import diva.canvas.CompositeFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.gui.toolbox.FigureIcon;
import ptolemy.kernel.util.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Writer;

//////////////////////////////////////////////////////////////////////////
//// EditorIcon
/**
An icon is the visual representation of an entity. This class is a factory
for such visual representations. This base class creates an icon as a
a Diva figure (an instanceof of diva.canvas.Figure). It also provides
a facility for generating a Swing icon (i.e. an instance of javax.swing.Icon)
from that figure (the createIcon() method).
<p>
The icon consists of a background figure, created by the
createBackgroundFigure() method, and a decorated version, created
by the createFigure() method.  The decorated version has, in this
base class, a label showing the name of the entity, unless the entity
contains an attribute called "_hideName".  The swing icon created
by createIcon() does not include the decorations, but rather is only
the background figure.  In this base class, the background figure is
a simple white box.
<p>
Derived classes most likely will customize the createBackgroundFigure()
method.  This will affect both the Diva Figure and the Swing
Icon representations.  Derived classes can also create the figure or
the icon in a different way entirely (for example, starting with a
Swing icon and creating the figure using a SwingWrapper) by overriding
both createBackgroundFigure() and createIcon().

@author Steve Neuendorffer, John Reekie
@contributor Edward A. Lee
@version $Id$
*/
public class EditorIcon extends Attribute {

    // NOTE: This class used to have a major design error.  It
    // implemented NotPersistent, which told the MoMLWriter class
    // to not write MoML for it.  But derived classes then are
    // forced to be non-persistent,
    // which means that we cannot have custom icons for objects.
    // An icon is required to be an instance of EditorIcon...

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public EditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This should manufacture a new
     *  figure each time, since figures are inexpensive and contain
     *  their own location.  This base class returns a default background
     *  figure, which is a simple white box.  Subclasses will generally
     *  override this method to create more interesting figures.
     *  This method should never return null.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
	return _createDefaultBackgroundFigure();
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of CompositeFigure with the
     *  figure returned by createBackgroundFigure() as its background.
     *  This method adds a LabelFigure to the CompositeFigure that
     *  contains the name of the container of this icon, unless the
     *  container has an attribute called "_hideName".  If the container
     *  has an attribute called "_centerName", then the name is rendered
     *  in the center of the background figure, rather than above it.
     *  This method should never return null, even if the icon has
     *  not been properly initialized.
     *  @return A new CompositeFigure consisting of the background figure
     *   and a label.
     */
    public Figure createFigure() {
	Figure background = createBackgroundFigure();
	Rectangle2D backBounds = background.getBounds();
        Figure figure = new CompositeFigure(background);
        NamedObj container = (NamedObj)getContainer();
        // Create the label, unless this is a visible attribute,
        // which typically carries no label.
        // NOTE: backward compatibility problem...
        // Old style annotations now have labels...
        if(container.getAttribute("_hideName") == null) {
            String name = container.getName();
            // Do not add a label figure if the name is null.
            if (name != null && !name.equals("")) {
                if(container.getAttribute("_centerName") == null) {
                    LabelFigure label = new LabelFigure(name,
                            _labelFont, 1.0, SwingConstants.SOUTH_WEST);
                    // Shift the label slightly right so it doesn't
                    // collide with ports.
                    label.translateTo(backBounds.getX() + 5, backBounds.getY());
                    ((CompositeFigure)figure).add(label);
                } else {
                    LabelFigure label = new LabelFigure(name,
                            _labelFont, 1.0, SwingConstants.CENTER);
                    label.translateTo(backBounds.getCenterX(),
                            backBounds.getCenterY());
                    ((CompositeFigure)figure).add(label);
                }
            }
        }
	return figure;
    }

    /** Create a new Swing icon.  In this base class, this icon is created
     *  from the background figure returned by createBackgroundFigure().
     *  Note that the background figure does NOT include a label for the name.
     *  This method might be suitable, for example, for creating a small icon
     *  for use in a library.
     *  @return A new Swing Icon.
     */
    public javax.swing.Icon createIcon() {
	// In this class, we cache the rendered icon, since creating icons from
	// figures is expensive.
        if(_iconCache != null) {
	    return _iconCache;
        }
        // No cached object, so rerender the icon.
	Figure figure = createBackgroundFigure();
	_iconCache = new FigureIcon(figure, 20, 15);
	return _iconCache;
    }

    /** Write a MoML description of this object, which in this case is
     *  empty.  Nothing is written. MoML is an XML modeling markup language.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Create a new default background figure, which is a white box.
     *  Subclasses of this class should generally override
     *  the createBackgroundFigure method instead.  This method is provided
     *  so that subclasses are always able to create a default figure even if
     *  an error occurs or the subclass has not been properly initialized.
     *  @return A figure representing a rectangular white box.
     */
    protected static Figure _createDefaultBackgroundFigure() {
	return new BasicRectangle(0, 0, 60, 40, Color.white, 1);
    }

    /** Recreate the figure.  Call this to cause createIcon() to call
     *  createBackgroundFigure() to obtain a new figure.
     */
    protected void _recreateFigure() {
        _iconCache = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    /** The cached Swing icon. */
    protected javax.swing.Icon _iconCache = null;

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
}
