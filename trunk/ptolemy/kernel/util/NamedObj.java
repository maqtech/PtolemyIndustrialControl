/* Concrete base class for objects with a name and a container.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package pt.kernel;

import pt.data.*;
import java.io.Serializable;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/** 
Base class for objects with a name.
A simple name is an arbitrary string. If no simple name is
provided, the name is taken to be an empty string (not a null
reference). The class also has a full name, which is a concatenation
of the container's full name and the simple name, separating by a
period. Obviously, if the simple name contains a period then there
may be some confusion resolving the full name, so periods are discouraged.
If there is no container, then the full name is the same as
the simple name. The full name is used for error reporting
throughout the package, which is why it is supported at this low
level of the class hierarchy.

Instances of this class are associated with a workspace, specified
as a constructor argument.  The workspace is immutable.  It cannot
be changed during the lifetime of the object.  It is used for
synchronization of methods that depend on or modify the state of
objects within it.  It is also used for version tracking.
Any method in this class or derived classes that changes the
state of any object within the workspace should call the
incrVersion() method of the workspace.  If no workspace is
specified, then the default workspace is used.  Note however that
since all actions within a default workspace are synchronized,
the use of the default workspace will limit parallelism.

The container for instances of this class is always null, although
derived classes that support hierarchy may report a non-null container.
If they do, they are not explicitly listed in the workspace, although
they will still use it for synchronization.  I.e., the workspace
keeps track only of top-level objects in the containment hierarchy.
Any object contained by another uses the workspace of its container
as its own workspace.

@author Mudit Goel, Edward A. Lee
@version $Id$
*/

public class NamedObj implements Nameable, Serializable {

    /** Construct an object in the default workspace with an empty string
     *  as its name.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public NamedObj() {
        _workspace = _defaultworkspace;
        // Ignore exception that can't occur.
        try {
            _workspace.add(this);
            setName("");
        } catch (IllegalActionException ex) {}
    }

    /** Construct an object in the default workspace with the given name.
     *  Increment the version of the workspace.  If the name argument
     *  is null, then the name is set to the empty string.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public NamedObj(String name) {
        this(_defaultworkspace, name);
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version of the workspace.  If the name argument
     *  is null, then the name is set to the empty string.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     */
    public NamedObj(Workspace workspace, String name) {
        if (workspace == null) {
            workspace = _defaultworkspace;
        }
        _workspace = workspace;
        // Exception cannot occur, so we ignore.
        try {
            workspace.add(this);
        } catch (IllegalActionException ex) {}
        setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Add a Param. this causes the version number of the workspace
     *  to be incremented.
     *  @param p The param to be added
     *  @exception NameDuplicationException thrown if this NamedObj already
     *  has a Param with the same name
     *  @exception IllegalActionionException Thrown if trying to attach 
     *  a Param which is not contained by this NamedObj.
     */
    public void addParam(Param p) throws NameDuplicationException, IllegalActionException {
        if (((NamedObj)p.getContainer()) != this) {
            throw new IllegalActionException("Trying to attach a param to a namedObj that is not its container");
        }
        try {
            if (_params == null) {
                _params = new NamedList();
            }
            _params.append(p);
        } catch (IllegalActionException ex) {   
            // a Param cannot be constructed without a name
        }
        workspace().incrVersion();
    }

    /** Return a description of the object.
     *  @param verbosity The level of verbosity.
     */
    public String description(int verbosity){
        switch (verbosity) {
        case pt.kernel.Nameable.PRETTYPRINT:
        case pt.kernel.Nameable.CONTENTS:
        case pt.kernel.Nameable.LIST_PRETTYPRINT:
            return toString() + "\n";
        case pt.kernel.Nameable.CONNECTIONS:
        case pt.kernel.Nameable.LIST_CONNECTIONS:
            // NamedObjs do not have connections, so we don't return anything.
            return "";
        case pt.kernel.Nameable.LIST_CONTENTS:
        case pt.kernel.Nameable.QUIET:
        default:
            return toString();
        }
    }

    /** Get the container.  Always return null in this base class.
     *  A null return value should be interpreted as indicating 
     *  that there is no container.
     *  @return null.
     */
    public Nameable getContainer() {
	return null;
    }

    /** Return a string of the form "workspace.name1.name2...nameN" where
     *  "nameN" is the name of this object, "workspace" is the name of the
     *  workspace, and the intervening names are the names of the containers
     *  of this othe name of this object, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in an exception.  Note that it is not
     *  possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  This method is synchronized on the workspace.
     *  @return The full name of the object.
     *  @exception InvalidStateException Container contains itself.
     */
    public String getFullName() 
            throws InvalidStateException {
        synchronized (workspace()) {
            String fullname = new String(getName());
            // Use a linked list to keep track of what we've seen already.
            LinkedList visited = new LinkedList();
            visited.insertFirst(this);
            Nameable parent = getContainer();

            while (parent != null) {
                if (visited.firstIndexOf(parent) >= 0) {
                    // Cannot use this pointer or we'll get stuck infinitely
                    // calling this method, since it's used to report
                    // exceptions.
                    throw new InvalidStateException(
                            "Container contains itself.");
                }
                fullname = parent.getName() + "." + fullname;
                visited.insertFirst(parent);
                parent = parent.getContainer();
            }
            return workspace().getName() + "." + fullname;
        }
    }

    /** Get the name. Note that the returned value will always be a 
     *  non-null String object as guaranteed by the constructors and
     *  setName() of this class. It is possible for the string to be 
     *  the empty string; i.e., "".
     *  @return The name of the object. 
     */	
    public String getName() { 
        return _name; 
    }

    /** Get the param attached to this NamedObj with the given name
     *  @param name The name of the desired Param.
     *  @return The requested Param if it is found, null otherwise
     */
    public Param getParam(String name) {
        return (Param) _params.get(name);
    }

    /** Get an Enumeration of the Params attached to this object
     */
    public Enumeration getParams() {
        if  (_params == null) {
            return (new NamedList()).getElements();
        } else {
            return _params.getElements();
        }
    }

    /** Remove the param attached to this NamedObj with the given name
     *  If no Parm with the given name exists, do nothing.
     *  @param name The name of the Param to be removed.
     *  @return The removed Param if it is found, null otherwise
     */
    public Param removeParam(String name) {
        return (Param)_params.remove(name);
    }


    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is synchronized on the workspace.
     *  @param name The new name.
     */
    public void setName(String name) {
        if (name == null) {
            name = new String("");
        }
        synchronized (workspace()) {
            _name = name;
            workspace().incrVersion();
        }
    }

    /** Return a concise description of the object. */ 
    public String toString() {
        try {
            return getClass().getName() + " {" + getFullName()+ "}";
        } catch (InvalidStateException e) {
            return getClass().getName() + " {" + _name + "}";
        }
    }

    /** Get the workspace. This method never returns null, since there
     *  is always a workpace.
     */	
    public Workspace workspace() {
        return _workspace; 
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // Instance of a workspace that can be used if no other is specified.
    protected static Workspace _defaultworkspace = new Workspace();

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The name
    private String _name;

    // The Params attached to this object.
    private NamedList _params;

    // The workspace for this object.
    // This should be set by the constructor and never changed.
    private Workspace _workspace;
}
