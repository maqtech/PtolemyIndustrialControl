/* A TopologyListener is an interface implemented by objects that are
   interested in being kept informed about changes in a graph topology.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (johnr@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.kernel.event;


//////////////////////////////////////////////////////////////////////////
//// TopologyListener
/**
A TopologyListener is an interface implemented by objects that are
interested in being kept informed about changes in a graph topology.
The execution mechanism in different classes of models of computation
decide when to perform mutations. They are expected to provide
a mechanism by which a TopologyListener object can be registered,
and to ensure that each registered listener is updated whenever
a mutation is performed.

@author John Reekie
@version $Id$
@see Topology
*/
public interface TopologyListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify that an entity has been added to a composite.
     * The <b>compositeEntity</b> and <b>entity</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void entityAdded(TopologyEvent event);

    /** Notify that an entity has been removed from a composite.
     * The <b>compositeEntity</b> and <b>entity</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void entityRemoved(TopologyEvent event);

    /** Notify that a port has been added to an entity.
     * The <b>entity</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void portAdded(TopologyEvent event);

    /** Notify that a port has been linked to a relation.
     * The <b>relation</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void portLinked(TopologyEvent event);

    /** Notify that a port has been unlinked from a relation.
     * The <b>relation</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void portUnlinked(TopologyEvent event);

    /** Notify that a port has been removed from a entity.
     * The <b>entity</b> and <b>port</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void portRemoved(TopologyEvent event);

    /** Notify that a relation has been added to a composite.
     * The <b>compositeEntity</b> and <b>relation</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void relationAdded(TopologyEvent event);

    /** Notify that a relation has been removed from a composite.
     * The <b>compositeEntity</b> and <b>relation</b> fields of the
     * passed event will be valid.
     *
     * @param event The mutation event
     */
    public void relationRemoved(TopologyEvent event);
}
