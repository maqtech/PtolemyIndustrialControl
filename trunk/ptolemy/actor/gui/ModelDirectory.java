/* A directory of open models.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ModelDirectory
/**
A directory of open models. An instance of this class is contained
by a Configuration. Each open model is represented by an instance of
Effigy.  An effigy represents the model data.
It contains a string attribute named "identifier"
with a string value that uniquely identifies the model.
A typical choice (which depend on the configuration)
is the canonical URL for a MoML file that describes the model.
An effigy also contains all open instances of Tableau associated
with the model.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Configuration
@see Effigy
@see Tableau
*/
public class ModelDirectory extends CompositeEntity {

    /** Construct a model directory with the specified container and name.
     *  @param container The configuration that contains this directory.
     *  @param name The name of the directory.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.  This should not be thrown.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelDirectory(Configuration container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the effigy of the model that corresponds to the specified
     *  identifier.
     *  @param identifier The identifier for the model, such as a URL.
     *  @return The effigy for the model, or null if the model is not
     *   in the directory.
     */
    public Effigy getEffigy(String identifier) {
        Iterator entities = entityList(Effigy.class).iterator();
        while (entities.hasNext()) {
            Effigy entity = (Effigy)entities.next();
            StringAttribute id =
                    (StringAttribute)entity.getAttribute("identifier");
            if (id != null) {
                String idString = id.getExpression();
                if (idString.equals(identifier)) return entity;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Remove the specified entity, and if there are no more models
     *  in the directory, then remove this directory from its container.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the entity instead with a null argument.
     *  The entity is assumed to be contained by this composite (otherwise,
     *  nothing happens). This does not alter the entity in any way.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  This class overrides the superclass to check if this composite is
     *  empty, and if so, calls system.exit
     *  @param entity The entity to remove.
     */
    protected void _removeEntity(ComponentEntity entity) {
	super._removeEntity(entity);
	if (entityList(Effigy.class).size() == 0) {
            try {
		setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException("Cannot remove directory!");
            }
        }
    }
}
