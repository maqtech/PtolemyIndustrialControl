/* A model for a Ptolemy II object as a node in a diva graph.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy;

import diva.graph.modular.NodeModel;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// PtolemyNodeModel

/**
A model for a Ptolemy II object as a node in a diva graph.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public abstract class PtolemyNodeModel implements NodeModel {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove the specified node from the model. */
    public abstract void removeNode(Object eventSource, Object node);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the context for which a change request concerning the given
     *  object should be made.  This is the first container
     *  above the object in the hierarchy that defers its
     *  MoML definition, or the immediate parent if there is none.
     *  @param object The object to change.
     *  @return The context for a change request.
     */
    protected static NamedObj _getChangeRequestParent(NamedObj object) {
        NamedObj container = MoMLChangeRequest.getDeferredToParent(object);
        if (container == null) {
            container = (NamedObj)object.getContainer();
        }
        if (container == null) {
            return object;
        }
        return container;
    }
}
