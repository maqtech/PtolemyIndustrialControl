/* A relation supporting clustered graphs.

 Copyright (c) 1997- The Regents of the University of California.
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

@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel;

import ptolemy.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ComponentRelation
/**
This class defines a relation supporting hierarchy (clustered graphs).
Specifically, a method is added for defining a container and for
performing deep traversals of
a graph. Most importantly, however, instances of this class refuse to link
to ports that are not instances of ComponentPort.  Thus, this class
ensures that ComponentPort instances are only connected to other
ComponentPort instances.
<p>
Derived classes may wish to further constrain linked ports to a subclass
of ComponentPort, or to disallow links under other circumstances,
for example if the relation cannot support any more links.
Such derived classes should override the protected method _checkPort()
to throw an exception.
<p>
To link a ComponentPort to a ComponentRelation, use the link() or
liberalLink() method in the ComponentPort class.  To remove a link,
use the unlink() method.
<p>
The container for instances of this class can only be instances of
ComponentEntity.  Derived classes may wish to further constrain the
container to subclasses of ComponentEntity.  To do this, they should
override the setContainer() method.

@author Edward A. Lee
@version $Id$
*/
public class ComponentRelation extends Relation {

    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public ComponentRelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public ComponentRelation(Workspace workspace) {
	super(workspace, "");
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public ComponentRelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container.workspace(), name);
        setContainer(container);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        ComponentRelation newobj = (ComponentRelation)super.clone(ws);
        newobj._container = null;
        return newobj;
    }

    /** Deeply enumerate the ports linked to this relation. Look through
     *  all transparent ports and return only opaque ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of ComponentPorts.
     */
    public Enumeration deepLinkedPorts() {
        try {
            workspace().getReadAccess();
            if (_deeplinkedportsversion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deeplinkedports.elements();
            }
            Enumeration nearports = linkedPorts();
            _deeplinkedports = new LinkedList();

            while( nearports.hasMoreElements() ) {
                ComponentPort port = (ComponentPort)nearports.nextElement();
                if (port._outside(this.getContainer())) {
                    // Port is above me in the hierarchy.
                    if (port.isOpaque()) {
                        // Port is opaque.  Append it to list.
                        _deeplinkedports.insertLast(port);
                    } else {
                        // Port is transparent.  See through it.
                        _deeplinkedports.appendElements(
                                port.deepConnectedPorts());
                    }
                } else {
                    // Port below me in the hierarchy.
                    _deeplinkedports.appendElements(port.deepInsidePorts());
                }
            }
            _deeplinkedportsversion = workspace().getVersion();
            return _deeplinkedports.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Get the container entity.
     *  @return An instance of CompositeEntity.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Specify the container entity, adding the relation to the list
     *  of relations in the container.  If the container already contains
     *  a relation with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this relation, throw an exception.
     *  If the relation is already contained by the container, do nothing.
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the workspace directory, if it is present.
     *  If the argument is null, then unlink the ports from the relation and
     *  remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this relation being garbage collected.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeEntity. This method is write-synchronized
     *  on the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException
     *   If this entity and the container are not in the same workspace.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contents list of the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && workspace() != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        try {
            workspace().getWriteAccess();
            CompositeEntity prevcontainer = (CompositeEntity)getContainer();
            if (prevcontainer == container) return;
            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addRelation(this);
                if (prevcontainer == null) {
                    workspace().remove(this);
                }
            }
            _container = container;
            if (prevcontainer != null) {
                prevcontainer._removeRelation(this);
            }
            if (container == null) {
                unlinkAll();
            }
            workspace().incrVersion();
        } finally {
            workspace().doneWriting();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class ComponentPort).
     *  @param port The port to link to.
     *  @exception IllegalActionException If the port is not a ComponentPort.
     */
    protected void _checkPort (Port port) throws IllegalActionException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
                    "ComponentRelation can only link to a ComponentPort.");
        }
    }

    /////////////////////////////////////////////////////////////////////////
    ////                        private variables                        ////

    // The entity that contains this entity.
    private CompositeEntity _container;

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    // 'transient' means that the variable will not be serialized.
    private transient LinkedList _deeplinkedports;
    private transient long _deeplinkedportsversion = -1;
}

