/* GR Actor
 Copyright (c) 2000-2001 The Regents of the University of California.
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
@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import javax.media.j3d.*;
import javax.vecmath.*;

//////////////////////////////////////////////////////////////////////////
//// GRActor
/**
A base class for all GR actors. This is an abstract class that is never
used as a standalone actor in a Ptolemy model.

@author C. Fong
@version $Id$
*/
public class GRActor extends TypedAtomicActor {

    /** Create a new GRActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public GRActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }


    public void addChild(Node node) throws IllegalActionException {
        throw new IllegalActionException("GR domain actor" + this +
                " cannot have children");
    }


    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        super.attributeChanged(attribute);
        try {
            // FIXME: need to check every attribute and make changes
            //_createModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fire() throws IllegalActionException {
    }


    public Node getNodeObject() {
        return null;
    }


    public void initialize() throws IllegalActionException {
        super.initialize();
        if (!(getDirector() instanceof GRDirector)) {
            throw new IllegalActionException(
                      "GR Actors can only be used under a GR Director");
        }
        _createModel();
    }

    public void makeSceneGraphConnection() throws IllegalActionException {
    }


    protected void _createModel() throws IllegalActionException {
    }

    protected void _stopRenderer() {
    }

    protected void _startRenderer() {
    }

    protected static GRActor _root;
}
