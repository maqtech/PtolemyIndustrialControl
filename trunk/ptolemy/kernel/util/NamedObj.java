/* Base class for objects with a name and a container.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
added public String getName(NamedObj parent);
*/

package ptolemy.kernel.util;

import ptolemy.kernel.CompositeEntity;		/* Needed by javadoc */

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/**
This is a base class for objects with a name. A simple name is an arbitrary
string. If no simple name is provided, the name is taken to be an
empty string (not a null reference). The class also has a full name,
which is a concatenation of the container's full name and the simple
name, separating by a period. Obviously, if the simple name contains
a period then there may be some confusion resolving the full name,
so periods are expressly disallowed. If there is no
container, then the full name is a concatenation of the workspace
name with the simple name. The
full name is used for error reporting throughout the package, which
is why it is supported at this low level of the class hierarchy.
<p>
Instances of this class are associated with a workspace, specified
as a constructor argument.  The workspace is immutable.  It cannot
be changed during the lifetime of the object.  It is used for
synchronization of methods that depend on or modify the state of
objects within it.  It is also used for tracking the version of any
topology within the workspace.
Any method in this class or derived classes that changes visible
state of any object within the workspace should call the
incrVersion() method of the workspace.  If no workspace is
specified, then the default workspace is used.
Note that the workspace should not be confused with the container.
The workspace never serves as a container.
<p>
In this base class, the container is null by default, and no method is
provided to change it. Derived classes that support hierarchy provide one
or more methods that set the container.
<p>
By convention, if the container of
a NamedObj is set, then it should be removed from the workspace directory,
if it is present.  The workspace directory is expected
to list only top-level objects in a hierarchy.
The NamedObj can still use the workspace for synchronization.
Any object contained by another uses the workspace of its container
as its own workspace by default.
<p>
Instances of Attribute can be attached by calling their setContainer()
method and passing this object as an argument. These instances will then
be reported by the getAttribute() and getAttributes() methods.
Classes derived from NamedObj may constrain attributes to be a subclass
of Attribute.  To do that, they should override the protected
_addAttribute() method to throw an exception if the
object provided is not of the right class.
<p>
Derived classes should override the _description() method to append
new fields if there is new information that should be included in the
description.

@author Mudit Goel, Edward A. Lee, Neil Smyth
@version $Id$
*/

public class NamedObj implements Nameable, Debuggable,
                                 Serializable, Cloneable {

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj() {
        this((Workspace)null);
    }

    /** Construct an object in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public NamedObj(String name)
            throws IllegalActionException {
        this(_DEFAULT_WORKSPACE, name);
    }

    /** Construct an object in the specified workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NamedObj(Workspace workspace) {
        // NOTE: Can't call the constructor below, which has essentially
        // the same code, without also spuriously throwing
        // IllegalActionException.
        if (workspace == null) {
            workspace = _DEFAULT_WORKSPACE;
        }
        _workspace = workspace;
        // Exception cannot occur, so we ignore. The object does not
        // have a container, and is not already on the workspace list.
        // NOTE: This does not need to be write-synchronized on the workspace
        // because the only side effect is adding to the directory,
        // and methods for adding and reading from the directory are
        // synchronized.
        try {
            workspace.add(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
        try {
            setName("");
        } catch (KernelException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
    }

    /** Construct an object in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this object.
     *  @exception IllegalActionException If the name has a period.
     */
    public NamedObj(Workspace workspace, String name)
            throws IllegalActionException {
        if (workspace == null) {
            workspace = _DEFAULT_WORKSPACE;
        }
        _workspace = workspace;
        // Exception cannot occur, so we ignore. The object does not
        // have a container, and is not already on the workspace list.
        // NOTE: This does not need to be write-synchronized on the workspace
        // because the only side effect is adding to the directory,
        // and methods for adding and reading from the directory are
        // synchronized.
        try {
            workspace.add(this);
        } catch (IllegalActionException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
        try {
            setName(name);
        } catch (NameDuplicationException ex) {
            // This exception should not be thrown.
            throw new InternalErrorException(
                    "Internal error in NamedObj constructor!"
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change listener.  If there is a container, then
     *  delegate to the container.  Otherwise, add the listener
     *  to the list of change listeners in this object. Each listener
     *  will be notified of the execution (or failure) of each
     *  change request that is executed via the requestChange() method.
     *  Note that in this implementation, only the top level of a
     *  hierarchy executes changes, which is why this method delegates
     *  to the container if there is one.
     *  <p>
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to add.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
	NamedObj container = (NamedObj) getContainer();
	if(container != null) {
            container.addChangeListener(listener);
        } else {
            if (_changeListeners == null) {
                _changeListeners = new LinkedList();
                _changeListeners.add(listener);
            } else if (!_changeListeners.contains(listener)) {
                _changeListeners.add(listener);
            }
        }
    }

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public synchronized void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }
        _debugListeners.add(listener);
        _debugging = true;
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {}

    /** Return a list of the attributes contained by this object.
     *  If there are no attributes, return an empty list.
     *  This method is read-synchronized on the workspace.
     *  @return An unmodifiable list of instances of Attribute.
     */
    public List attributeList() {
        try {
            _workspace.getReadAccess();
            if (_attributes == null) {
                _attributes = new NamedList();
            }
            return _attributes.elementList();
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of the attributes contained by this object that
     *  are instances of the specified class.  If there are no such
     *  instances, then return an empty list.
     *  This method is read-synchronized on the workspace.
     *  @param filter The class of attribute of interest.
     *  @return A list of instances of specified class.
     */
    public List attributeList(Class filter) {
        try {
            _workspace.getReadAccess();
            if (_attributes == null) {
                _attributes = new NamedList();
            }
            List result = new LinkedList();
            Iterator attributes = _attributes.elementList().iterator();
            while (attributes.hasNext()) {
                Object attribute = attributes.next();
                if (filter.isInstance(attribute)) {
                    result.add(attribute);
                }
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** React to a change in the type of an attribute.  This method is
     *  called by a contained attribute when its type changes.
     *  In this base class, the method does nothing.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {}

    /** Clone the object into the current workspace by calling the clone()
     *  method that takes a Workspace argument.
     *  This method read-synchronizes on the workspace.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return clone(_workspace);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there). This uses the clone() method of
     *  java.lang.Object, which makes a field-by-field copy.
     *  It then adjusts the workspace reference and clones the
     *  attributes on the attribute list, if there is one. In addition,
     *  if this object has the MoML element name "class", as determined
     *  by className field of the associated MoMLInfo object,
     *  then the new object will not export
     *  its contents when exportMoML() is called, but rather will
     *  declare that it extends this one, and will export only its attributes.
     *  This method read-synchronizes on the workspace.
     *  @param ws The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #getMoMLInfo()
     *  @see #exportMoML(Writer, int, String)
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // NOTE: It is safe to clone an object into a different
        // workspace. It is not safe to move an object to a new
        // workspace, by contrast. The reason this is safe is that
        // after the this method has been run, there
        // are no references in the clone to objects in the old
        // workspace. Moreover, no object in the old workspace can have
        // a reference to the cloned object because we have only just
        // created it and have not returned the reference.
        try {
            _workspace.getReadAccess();
            NamedObj newobj = (NamedObj)super.clone();
            // NOTE: It is not necessary to write-synchronize on the other
            // workspace because this only affects its directory, and methods
            // to access the directory are synchronized.
            newobj._attributes = null;
            newobj._workspace = ws;
            newobj._fullNameVersion = -1;

            Iterator params = attributeList().iterator();
            while (params.hasNext()) {
                Attribute p = (Attribute)params.next();
                Attribute np = (Attribute)p.clone(ws);
                try {
                    np.setContainer(newobj);
                } catch (KernelException ex) {
                    throw new CloneNotSupportedException(
                            "Failed to clone an Attribute of " +
                            getFullName() + ": " + ex.getMessage());
                }
            }
            if (_debugging) {
                if (ws == null) {
                    _debug("Cloned", getFullName(), "into default workspace.");
                } else {
                    _debug("Cloned", getFullName(), "into workspace:",
                            ws.getFullName());
                }
            }
            if (_MoMLInfo != null) {
                newobj._MoMLInfo = new MoMLInfo(newobj);
                newobj._MoMLInfo.elementName = _MoMLInfo.elementName;
                newobj._MoMLInfo.source = _MoMLInfo.source;
                // The new object does not have any other objects deferring
                // their MoML definitions to it, so we have to reset this.
                newobj._MoMLInfo.deferredFrom = null;

                // If the master defers its MoML definition to
                // another object, then so will the clone.
                // So we have to add the clone to the list of objects
                // in the object deferred to.
                if (_MoMLInfo.deferTo != null) {
                    _MoMLInfo.deferTo._MoMLInfo.getDeferredFrom().add(newobj);
                }

                // If the master is a class, then its full name
                // becomes the class name of the clone.
                if (getMoMLInfo().elementName.equals("class")) {
                    newobj._setDeferMoMLDefinitionTo(this);
                    newobj._MoMLInfo.className = getFullName();
                } else {
                    newobj._MoMLInfo.className = _MoMLInfo.className;
                }
            }
            return newobj;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if this object contains the specified object,
     *  directly or indirectly.  That is, return true if the specified
     *  object is contained by an object that this contains, or by an
     *  object contained by an object contained by this, etc.
     *  This method ignores whether the entities report that they are
     *  atomic (see CompositeEntity), and always returns false if the entities
     *  are not in the same workspace.
     *  This method is read-synchronized on the workspace.
     *  @see ptolemy.kernel.CompositeEntity#isAtomic()
     *  @return True if this contains the argument, directly or indirectly.
     */
    public boolean deepContains(NamedObj inside) {
        try {
            _workspace.getReadAccess();
            // Start with the inside and check its containers in sequence.
            if (inside != null) {
                if (_workspace != inside._workspace) return false;
                Nameable container = inside.getContainer();
                while (container != null) {
                    if (container == this) {
                        return true;
                    }
                    container = container.getContainer();
                }
            }
            return false;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a full description of the object. This is accomplished
     *  by calling the description method with an argument for full detail.
     *  This method read-synchronizes on the workspace.
     *  @return A description of the object.
     *  @see #exportMoML(Writer, int, String)
     */
    public String description() {
        return description(COMPLETE);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  This method returns an empty
     *  string (not null) if there is nothing to report.
     *  It read-synchronizes on the workspace.
     *  @param detail The level of detail.
     *  @return A description of the object.
     *  @see #exportMoML(Writer, int, String)
     */
    public String description(int detail) {
        return _description(detail, 0, 0);
    }

    /** Get a MoML description of this object.  This might be an empty string
     *  if there is no MoML description of the object.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @return A MoML description, or null if there is none.
     *  @see #exportMoML(Writer, int, String)
     */
    public final String exportMoML() {
        try {
            StringWriter buffer = new StringWriter();
            exportMoML(buffer, 0);
            return buffer.toString();
        } catch (IOException ex) {
            // This should not occur.
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Get a MoML description of this object with its name replaced by
     *  the specified name.  The description might be an empty string
     *  if there is no MoML description of the object.  This uses the
     *  three-argument version of this method.  It is final to ensure that
     *  derived classes only override that method to change
     *  the MoML description.
     *  @return A MoML description, or null if there is none.
     *  @see #exportMoML(Writer, int, String)
     */
    public final String exportMoML(String name) {
        try {
            StringWriter buffer = new StringWriter();
            exportMoML(buffer, 0, name);
            return buffer.toString();
        } catch (IOException ex) {
            // This should not occur.
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Write a MoML description of this object using the specified
     *  Writer.  If there is no MoML description, then nothing is written.
     *  To write to standard out, do
     *  <pre>
     *      exportMoML(new OutputStreamWriter(System.out))
     *  </pre>
     *  This method uses the three-argument
     *  version of this method.  It is final to ensure that
     *  derived classes only need to override that method to change
     *  the MoML description.
     *  @exception IOException If an I/O error occurs.
     *  @param output The stream to write to.
     *  @see #exportMoML(Writer, int, String)
     */
    public final void exportMoML(Writer output) throws IOException {
        exportMoML(output, 0);
    }

    /** Write a MoML description of this entity with the specified
     *  indentation depth.  This calls the three-argument version of
     *  this method with getName() as the third argument.
     *  This method is final to ensure that
     *  derived classes only override the three-argument method to change
     *  the MoML description.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int, String)
     */
    public final void exportMoML(Writer output, int depth) throws IOException {
        exportMoML(output, depth, getName());
    }

    /** Write a MoML description of this entity with the specified
     *  indentation depth and with the specified name substituting
     *  for the name of this entity.  The element name, class, and
     *  source attributes are determined by the instance of MoMLInfo
     *  returned by getMoMLInfo(). The description has one of two
     *  forms, depending on the element name. If it is "class", then
     *  the exported MoML looks like this:
     *  <pre>
     *      &lt;class name="<i>name</i>" extends="<i>classname</i> source="<i>source</i>"&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/class&gt;
     *  </pre>
     *  If it is anything else (call what it returns
     *  <i>element</i>), then the exported MoML looks like this:
     *  <pre>
     *      &lt;<i>element</i> name="<i>name</i>" class="<i>classname</i>" source="<i>source</i>"&gt;&gt;
     *          <i>body, determined by _exportMoMLContents()</i>
     *      &lt;/<i>element</i>&gt;
     *  </pre>
     *  By default, the element name is "entity."  The default class name
     *  is the Java classname of this instance.  This default is
     *  represented by a null className field in the MoMLInfo object.
     *  The source attribute is by default left off altogether.
     *  <p>
     *  If this object has been cloned from another whose element name
     *  is "class", then the body contains
     *  only the MoML exported by the attributes. I.e., the
     *  _exportMoMLContents() method is not called in this case.
     *  For example, if "foo" was cloned from "bar", and "bar" is a
     *  MoML "class", then the exported MoML looks like:
     *  <pre>
     *      &lt;entity name="foo" class=".bar"&gt;
     *          <i>attributes</i>
     *      &lt;/entity&gt;
     *  </pre>
     *  Here, ".bar" is the full name of another named object.
     *  <p>
     *  If this object has no container and the depth argument is zero,
     *  then this method prepends XML file header information, which is:
     *  <pre>
     *  &lt;?xml version="1.0" standalone="no"?&gt;
     *  &lt;!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
     *      "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;
     *  </pre>
     *  <p>
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  Derived classes should override this method to change the MoML
     *  description of an object.  They should override the protected
     *  method _exportMoMLContents() if they need to only change which
     *  contents are described.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use in the exported MoML.
     *  @exception IOException If an I/O error occurs.
     *  @see #clone(Workspace)
     *  @see #getMoMLInfo()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        String momlElement = getMoMLInfo().elementName;
        String className = getMoMLInfo().className;
        String template = null;
        if (momlElement.equals("class")) {
            template = "\" extends=\"";
        } else {
            template = "\" class=\"";
        }
        if (depth == 0
                && getContainer() == null
                && (momlElement.equals("class")
                        || momlElement.equals("model")
                        || momlElement.equals("entity"))) {
            // No container, and this is a top level moml element.
            // Generate header information.
            output.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE model PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"\n"
                    + "    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">\n");

            // Correct the element name, if appropriate.
            if (momlElement.equals("entity")) {
                // Erroneous element name.  Change it.
                momlElement = "model";
                getMoMLInfo().elementName = momlElement;
            }
        }
        output.write(_getIndentPrefix(depth)
                + "<"
                + momlElement
                + " name=\""
                + name
                + template
                + className
                + "\"");
        if (getMoMLInfo().source != null) {
            output.write(" source=\"" + getMoMLInfo().source + "\">\n");
        } else {
            output.write(">\n");
        }
        if (getMoMLInfo().deferTo == null) {
            _exportMoMLContents(output, depth + 1);
        } else {
            // Describe only the attributes.
            Iterator attributes = attributeList().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute)attributes.next();
                attribute.exportMoML(output, depth + 1);
            }
        }
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }

    /** Get the attribute with the given name. The name may be compound,
     *  with fields separated by periods, in which case the attribute
     *  returned is contained by a (deeply) contained attribute.
     *  This method is read-synchronized on the workspace.
     *  @param name The name of the desired attribute.
     *  @return The requested attribute if it is found, null otherwise.
     */
    public Attribute getAttribute(String name) {
        try {
            _workspace.getReadAccess();
            if (_attributes == null) {
                // No attribute has been added to this NamedObj yet.
                return null;
            } else {
                String[] subnames = _splitName(name);
                if (subnames[1] == null) {
                    return (Attribute) _attributes.get(name);
                } else {
                    Attribute match = (Attribute)_attributes.get(subnames[0]);
                    if (match == null) {
                        return null;
                    } else {
                        return match.getAttribute(subnames[1]);
                    }
                }
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return an enumeration of the attributes attached to this object.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use attributeList() instead.
     *  @return An enumeration of instances of Attribute.
     */
    public Enumeration getAttributes() {
        return Collections.enumeration(attributeList());
    }

    /** Get the container.  Always return null in this base class.
     *  A null returned value should be interpreted as indicating
     *  that there is no container.
     *  @return null.
     */
    public Nameable getContainer() {
	return null;
    }

    /** Return a string of the form ".name1.name2...nameN". Here,
     *  "nameN" is the name of this object,
     *  and the intervening names are the names of the containers
     *  of this other name of this object, if there are containers.
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, results in a runtime exception of class
     *  InvalidStateException.  Note that it is
     *  not possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  This method is read-synchronized on the workspace.
     *  @return The full name of the object.
     */
    public String getFullName() {
        try {
            _workspace.getReadAccess();
            if (_fullNameVersion == _workspace.getVersion()) {
                return _fullNameCache;
            }
            // Cache is not valid. Recalculate full name.
            String fullName = getName();
            // Use a hash set to keep track of what we've seen already.
            Set visited = new HashSet();
            visited.add(this);
            Nameable container = getContainer();

            while (container != null) {
                if (visited.contains(container)) {
                    // Cannot use "this" as a constructor argument to the
                    // exception or we'll get stuck infinitely
                    // calling this method, since this method is used to report
                    // exceptions.  InvalidStateException is a runtime
                    // exception, so it need not be declared.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }
                fullName = container.getName() + "." + fullName;
                visited.add(container);
                container = container.getContainer();
            }
            _fullNameCache = "." + fullName;
            _fullNameVersion = _workspace.getVersion();
            return _fullNameCache;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the data structure defining the MoML description of this class.
     *  This method creates an instance of the data structure if one does
     *  not already exist, so the method never returns null.
     *  @return An instance of MoMLInfo.
     *  @see NamedObj$MoMLInfo
     */
    public MoMLInfo getMoMLInfo() {
        if (_MoMLInfo == null) {
            _MoMLInfo = new MoMLInfo(this);
        }
        return _MoMLInfo;
    }

    /** Get the name. If no name has been given, or null has been given,
     *  then return an empty string, "".
     *  @return The name of the object.
     */
    public String getName() {
        return _name;
    }

    /** Get the name of this object relative to the specified container. 
     *  A recursive structure, where this object is directly or indirectly
     *  contained by itself, may result in a runtime exception of class
     *  InvalidStateException if it is detected.  Note that it is
     *  not possible to construct a recursive structure using this class alone,
     *  since there is no container.
     *  But derived classes might erroneously permit recursive structures,
     *  so this error is caught here.
     *  If the given container is not actually a container of this object,
     *  or is null, then the full name of the object is returned.
     *  This method is read-synchronized on the workspace.
     *  @param parent An object that deeply contains this object.
     *  @return A string of the form "name2...nameN".
     */
    public String getName(NamedObj parent) {
	if(parent == null) {
	    return getFullName();
	}
        try {
            _workspace.getReadAccess();
	    String name = getName();
            // Use a hash set to keep track of what we've seen already.
            Set visited = new HashSet();
            visited.add(this);
            Nameable container = getContainer();

            while (container != null && container != parent) {
                if (visited.contains(container)) {
                    // Cannot use "this" as a constructor argument to the
                    // exception or we'll get stuck infinitely
                    // calling getFullName(), 
		    // since that method is used to report
                    // exceptions.  InvalidStateException is a runtime
                    // exception, so it need not be declared.
                    throw new InvalidStateException(
                            "Container contains itself!");
                }
                name = container.getName() + "." + name;
                visited.add(container);
                container = container.getContainer();
            }
	    if(container == null) {
		return getFullName();
	    }
	    return name;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Remove a change listener. If there is a container, delegate the
     *  request to the container.  If the specified listener is not
     *  on the list of listeners, do nothing.
     *  @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
	NamedObj container = (NamedObj) getContainer();
	if(container != null) {
            container.removeChangeListener(listener);
        } else if (_changeListeners != null) {
            _changeListeners.remove(listener);
        }
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public synchronized void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        _debugListeners.remove(listener);
        if (_debugListeners.size() == 0) {
            _debugging = false;
        }
        return;
    }

    /** Request that given change request be executed.   In this base
     *  class, defer the change request to the container, if there is one.
     *  If there is no container, then execute the request immediately.
     *  In other words, the request will get passed up to the top level
     *  of the hierarchy and then executed.  Subclasses can override
     *  this to queue the change request and execute it at an appropriate time.
     *  Change listeners will be notified of success (or failure) or the
     *  request.
     *  @param change The requested change.
     */
    public void requestChange(ChangeRequest change) {
	NamedObj container = (NamedObj) getContainer();
	if(container == null) {
            // Make sure the list of listeners is not being concurrently
            // modified by making this synchronized.
            synchronized(this) {
                change.setListeners(_changeListeners);
            }
            change.execute();
	} else {
	    container.requestChange(change);
	}
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException Not thrown in this base class.
     *  May be thrown by derived classes if the container already contains
     *  an object with this name.
     */
    public void setName(String name)
            throws IllegalActionException, NameDuplicationException {
        String oldName = getFullName();
        if (name == null) {
            name = new String("");
        }
        int period = name.indexOf(".");
        if (period >= 0) {
            throw new IllegalActionException(this,
                    "Cannot set a name with a period: " + name);
        }
        try {
            _workspace.getWriteAccess();
            _name = name;
        } finally {
            _workspace.doneWriting();
        }
        if (_debugging) {
            _debug("Changed name from", oldName, "to", getFullName());
        }
    }

    /** Return the top level of the containment hierarchy.
     *  @return The top level, or this if this has no container.
     */
    public NamedObj toplevel() {
        NamedObj result = this;
        while (result.getContainer() != null) {
            result = (NamedObj)result.getContainer();
        }
        return result;
    }

    /** Return the class name and the full name of the object,
     *  with syntax "className {fullName}".
     *  @return The class name and the full name. */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    /** Return a name that is guaranteed to not be the name of any
     *  contained attribute.  In derived classes, this should be overridden
     *  so that the returned name is guaranteed to not conflict with
     *  any contained object.
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
    public String uniqueName(String prefix) {
        String candidate = prefix + _uniqueNameIndex++;
        while(getAttribute(candidate) != null) {
            candidate = prefix + _uniqueNameIndex++;
        }
        return candidate;
    }

    /** Get the workspace. This method never returns null, since there
     *  is always a workspace.
     *  @return The workspace responsible for this object.
     */
    public final Workspace workspace() {
        return _workspace;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include everything.
     */
    public static final int COMPLETE = ~0;

    /** Indicate that the description(int) method should include the class name.
     */
    public static final int CLASSNAME = 1;

    /** Indicate that the description(int) method should include the full name.
     *  The full name is surrounded by braces "{name}" in case it has spaces.
     */
    public static final int FULLNAME = 2;

    /** Indicate that the description(int) method should include the links
     *  (if any) that the object has.  This has the form "links {...}"
     *  where the list is a list of descriptions of the linked objects.
     *  This may force some of the contents to be listed.  For example,
     *  a description of an entity will include the ports if this is set,
     *  irrespective of whether the CONTENTS bit is set.
     */
    public static final int LINKS = 4;

    /** Indicate that the description(int) method should include the contained
     *  objects (if any) that the object has.  This has the form
     *  "keyword {{class {name}} {class {name}} ... }" where the keyword
     *  can be ports, entities, relations, or anything else that might
     *  indicate what the object contains.
     */
    public static final int CONTENTS = 8;

    /** Indicate that the description(int) method should include the contained
     *  objects (if any) that the contained objects have.  This has no effect
     *  if CONTENTS is not also specified.  The returned string has the form
     *  "keyword {{class {name} keyword {...}} ... }".
     */
    public static final int DEEP = 16;

    /** Indicate that the description(int) method should include attributes
     *  (if any).
     */
    public static final int ATTRIBUTES = 32;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an attribute.  This method should not be used directly.
     *  Instead, call setContainer() on the attribute.
     *  Derived classes may further constrain the class of the attribute.
     *  To do this, they should override this method to throw an exception
     *  when the argument is not an instance of the expected class.
     *  This method is write-synchronized on the workspace and increments its
     *  version number.
     *  @param p The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    protected void _addAttribute(Attribute p)
            throws NameDuplicationException, IllegalActionException {
        try {
            _workspace.getWriteAccess();
            try {
                if (_attributes == null) {
                    _attributes = new NamedList();
                }
                _attributes.append(p);
            } catch (IllegalActionException ex) {
                // This exception should not be thrown.
                throw new InternalErrorException(
                        "Internal error in NamedObj _addAttribute() method!"
                        + ex.getMessage());
            }
            if (_debugging) {
                _debug("Added attribute", p.getName(), "to", getFullName());
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Send a debug event to all debug listeners that have registered.
     *  @param event The event.
     */
    protected final void _debug(DebugEvent event) {
        if (_debugging) {
            synchronized(this) {
                Iterator listeners = _debugListeners.iterator();
                while (listeners.hasNext()) {
                    ((DebugListener)listeners.next()).event(event);
                }
            }
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    protected final void _debug(String message) {
        if (_debugging) {
            synchronized(this) {
                Iterator listeners = _debugListeners.iterator();
                while (listeners.hasNext()) {
                    ((DebugListener)listeners.next()).message(message);
                }
            }
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the two parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     */
    protected final void _debug(String part1, String part2) {
        if (_debugging) {
            _debug(part1 + " " + part2);
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the three parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     *  @param part3 The third part of the message.
     */
    protected final void _debug(String part1, String part2, String part3) {
        if (_debugging) {
            _debug(part1 + " " + part2 + " " + part3);
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  The message is a concatenation of the four parts, with a space between
     *  them.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param part1 The first part of the message.
     *  @param part2 The second part of the message.
     *  @param part3 The third part of the message.
     *  @param part4 The fourth part of the message.
     */
    protected final void _debug(String part1, String part2,
            String part3, String part4) {
        if (_debugging) {
            _debug(part1 + " " + part2 + " " + part3 + " " + part4);
        }
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in this class (NamedObj).  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result = _getIndentPrefix(indent);
            if (bracket == 1 || bracket == 2) result += "{";
            if((detail & CLASSNAME) != 0) {
                result += getClass().getName();
                if((detail & FULLNAME) != 0) {
                    result += " ";
                }
            }
            if((detail & FULLNAME) != 0) {
                result += "{" + getFullName() + "}";
            }
            if((detail & ATTRIBUTES) != 0) {
                if ((detail & (CLASSNAME | FULLNAME)) != 0) {
                    result += " ";
                }
                result += "attributes {\n";
                // Do not recursively list attributes unless the DEEP
                // bit is set.
                if ((detail & DEEP) == 0) {
                    detail &= ~ATTRIBUTES;
                }
                Iterator params = attributeList().iterator();
                while (params.hasNext()) {
                    Attribute p = (Attribute)params.next();
                    result += p._description(detail, indent+1, 2) + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this base class is the attributes.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     *  @see #exportMoML(Writer, int)
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            attribute.exportMoML(output, depth);
        }
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "    ";
        }
        return result;
    }

    /** Remove the given attribute.
     *  If there is no such attribute, do nothing.
     *  This method is write-synchronized on the workspace and increments its
     *  version. It should only be called by setContainer() in Attribute.
     *  @param param The attribute to be removed.
     */
    protected void _removeAttribute(Attribute param) {
        try {
            _workspace.getWriteAccess();
            _attributes.remove((Nameable)param);
            if (_debugging) {
                _debug("Removed attribute", param.getName(), "from",
                        getFullName());
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Split the specified name at the first period and return the
     *  two parts as a two-element array.  If there is no period, the second
     *  element is null.
     *  @param name The name to split.
     *  @return The name before and after the first period as a two-element
     *   array.
     */
    protected static final String[] _splitName(String name) {
        String[] result = new String[2];
        int period = name.indexOf(".");
        if (period < 0) {
            result[0] = name;
        } else {
            result[0] = name.substring(0, period);
            result[1] = name.substring(period + 1);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A list of change listeners. */
    protected List _changeListeners;

    /** @serial Flag that is true if there are debug listeners. */
    protected boolean _debugging = false;

    /** @serial The list of DebugListeners registered with this object. */
    protected LinkedList _debugListeners = null;

    /** An index that is incremented to expedite the search for a unique
     *  name by the uniqueName() method.
     */
    protected int _uniqueNameIndex = 0;

    /** @serial The workspace for this object.
     * This should be set by the constructor and never changed.
     */
    protected Workspace _workspace;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The Attributes attached to this object. */
    private NamedList _attributes;

    /** @serial Instance of a workspace that can be used if no other
     *  is specified.
     */
    private static Workspace _DEFAULT_WORKSPACE = new Workspace();

    // Cached value of the full name.
    private String _fullNameCache;

    // Version of the workspace when cache last updated.
    private long _fullNameVersion = -1;

    // The MoML information describing this object.
    private MoMLInfo _MoMLInfo;

    /** @serial The name */
    private String _name;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Specify that when generating a MoML description of this named
     *  object, instead of giving a detailed description, refer to the
     *  specified other object.  The name of that other object goes
     *  into the "class" or "extends" attribute of the MoML element
     *  defining this object.  This method is called when this object
     *  is constructed by cloning another object that identifies itself
     *  as a MoML "class".  In addition, calling this method
     *  suppresses description of the contents of the contents
     *  of this named object (_exportMoMLContents() is not called).
     *  Only the attributes are described in the body of the element.
     *  To re-enabled detailed descriptions, call this method with
     *  a null argument.  This method is write synchronized on
     *  the workspace because it modifies the object that is the
     *  argument to refer back to this one.
     *  @param referTo The object to refer to.
     *  @see #exportMoML(Writer, int)
     */
    private void _setDeferMoMLDefinitionTo(NamedObj deferTo) {
        try {
            _workspace.getWriteAccess();
            if (getMoMLInfo().deferTo != null) {
                // NOTE: Referring directly to _MoMLInfo is safe here
                // because getMoMLInfo() above ensures this is non-null.
                if (_MoMLInfo.deferTo.getMoMLInfo().deferredFrom != null) {
                    // Removing a previous reference.
                    _MoMLInfo.deferTo._MoMLInfo.deferredFrom.remove(this);
                }
            }
            _MoMLInfo.deferTo = deferTo;
            if (deferTo != null) {
                deferTo._MoMLInfo.getDeferredFrom().add(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is a data structure for storing MoML information
     *  used to construct the MoML description of an associated NamedObj.
     *  Each NamedObj has at most one instance of this class.  That
     *  instance can be accessed (or created if it does not exist)
     *  by calling getMoMLInfo().
     *  @see NamedObj#getMoMLInfo()
     */
    public class MoMLInfo {

        /** Construct an object with default values for the fields.
         *  @param owner The object that this describes.
         */
        public MoMLInfo(NamedObj owner) {
            className = owner.getClass().getName();
        }

        ///////////////////////////////////////////////////////////////
        ////                     public members                    ////

        /** The MoML class name, which defaults to the Java class name
         *  of the enclosing class.*/
        public String className;

        /** A list of objects that defer their MoML definitions to the
         *  owner of this MoMLInfo object.  Note that this might be null.
         *  To ensure that it is not null, access it using
         *  getDeferredFrom().
         */
        public List deferredFrom;

        /** The object that the owner defers its MoML definition to,
         *  or null if it does not defer its MoML definition.
         */
        public NamedObj deferTo;

        /** The MoML element name. This defaults to "entity".*/
        public String elementName = "entity";

        /** The source attribute. */
        public String source;

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return a list of objects that defer their MoML definitions
         *  owner of this MoMLInfo object.  This might be an empty list,
         *  but the returned value is never null.
         *  @return A list of instances of NamedObj.
         */
        public List getDeferredFrom() {
            if (deferredFrom == null) {
                deferredFrom = new LinkedList();
            }
            return deferredFrom;
        }
    }
}
