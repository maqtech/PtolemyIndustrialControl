/* A parser for MoML (model markup language)

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.moml;

// Ptolemy imports.
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Configurable;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Documentation;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.gui.MessageHandler;

// Java imports.
import java.awt.Container;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.lang.SecurityException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

// XML imports.
import com.microstar.xml.*;


//////////////////////////////////////////////////////////////////////////
//// MoMLParser
/**
This class constructs Ptolemy II models from specifications
in MoML (model markup language), which is based on XML.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to call its parse() method.
The returned value is top-level composite entity of the model.
<p>
For convenience, there are several forms of the parse method.
Most of these take two arguments, a base, and some specification
of the MoML to parse (a stream or the text itself).  The base is
used to interpret relative URLs that might be present in the MoML.
For example, the base might be the document base of an applet.
An applet might use this class as follows:
<pre>
   MoMLParser parser = new MoMLParser();
   URL docBase = getDocumentBase();
   URL xmlFile = new URL(docBase, modelURL);
   NamedObj toplevel = parser.parse(docBase, xmlFile);
</pre>
If the first argument to parse() is null, then it is assumed that
all URLs in the MoML file are absolute.
<p>
It can be difficult to create an appropriate URL to give as a base,
particularly if what you have is a file or file name
in the directory that you want to use as a base.  The easiest
technique is to use the toURL() method of the File class.
Some of the URL constructors, for reasons we don't understand,
create URLs that do not work.
<p>
The MoML code given to a parse() method may be a fragment,
and does not need to include the "&lt;?xml ... &gt;" element nor
the DOCTYPE specification.  However, if the DOCTYPE specification
is not given, then the DTD will not be read.  The main consequence
of this, given the parser we are using, is that default values
for attributes will not be set.  This could cause errors.
The parser itself is not a validating parser, however, so it
makes very limited use of the DTD.  This may change in the future,
so it is best to give the DOCTYPE element.
<p>
The parse() methods can be used for incremental parsing.  After
creating an initial model using a call to parse(), further MoML
fragments without top-level model or class elements can be evaluated
to modify the model.  You can specify the context in which the
MoML to be interepreted by calling setContext().  However, the
XML parser limits each fragment to one element.  So there always has
to be one top-level element.  If you wish to evaluate a group of
MoML elements in some context, set the context and then place your
MoML elements within a group element, as follows:
<pre>
    &lt;group&gt
        ... sequence of MoML elements ...
    &lt;/group&gt
</pre>
The group element is ignored, and just serves to aggregate the MoML
elements, unless it has a name attribute.  If it has a name attribute,
then the name becomes a prefix (separated by a colon) of all the names
of items immediately in the group element.
<p>
The parse methods throw a variety of exceptions if the parsed
data does not represent a valid MoML file or if the stream
cannot be read for some reason.
<p>
This parser supports the way Ptolemy II handles hierarchical models,
where components are instances cloned from reference models called
"classes." A model (a composite entity) is a "class" in Ptolemy II if
its getMoMLElementName() method returns the string "class".  If a
component is cloned from a class, then when that component exports
MoML, it references the class from which it was cloned
and exports only its attributes.  However, if further changes are
made to the component, it is important that when the component
exports MoML, that those changes are represented in the exported MoML.
This parser ensures that they are by creating an instance of
MoMLAttribute for each change that is made to the clone after cloning.
That attribute exports a MoML description of the change.
This effectively implements an inheritance mechanism, where
a component inherits all the features of the master from which it
is cloned, but then extends the model with its own changes.
<p>
This class works closely with MoMLChangeRequest to implement another
feature of Ptolemy II hierarchy.  In particular, if an entity is cloned
from another that identifies itself as a "class", then any changes that
are made to the class via a MoMLChangeRequest are also made to the clone.
This parser ensures that those changes are <i>not</i> exported when
MoML is exported by the clone, because they will be exported when the
master exports MoML.

@see MoMLChangeRequest
@see MoMLAttribute
@author Edward A. Lee, Steve Neuendorffer, John Reekie
@version $Id$
*/
public class MoMLParser extends HandlerBase {

    /** Construct a parser that creates a new workspace into which to
     *  put the entities created by the parse() method.  
     */
    public MoMLParser() {
        this(null);
    }

    /** Construct a parser that creates entities
     *  in the specified workspace.  If the argument is null,
     *  create a new workspace with an empty name.  Classes will be 
     *  created using the classloader that created this class.
     *  @param workspace The workspace into which to place entities.
     */
    public MoMLParser(Workspace workspace) {
	super();
        if (workspace == null) {
            // NOTE: Workspace has no name, to ensure that full names
            // of enties conform to MoML standard of starting with a
            // leading period.
            workspace = new Workspace();
        }
        _workspace = workspace;
    }

    /** Construct a parser that creates entities in the specified workspace.
     *  If the workspace argument is null, then
     *  create a new workspace with an empty name. Classes will be 
     *  created using the classloader that created this class.
     *  @param workspace The workspace into which to place entities.
     *  @param loader The class loader that will be used to create classes,
     *  or null if the the bootstrap class loader is to be used.
     */
    public MoMLParser(Workspace workspace, ClassLoader loader) {
	this(workspace);
	_classLoader = loader;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle an attribute assignment that is part of an XML element.
     *  This method is called prior to the corresponding startElement()
     *  call, so it simply accumulates attributes in a hashtable for
     *  use by startElement().
     *  @param name The name of the attribute.
     *  @param value The value of the attribute, or null if the attribute
     *   is <code>#IMPLIED</code> and not specified.
     *  @param specified True if the value is specified, false if the
     *   value comes from the default value in the DTD rather than from
     *   the XML file.
     *  @exception XmlException If the name or value is null.
     */
    public void attribute(String name, String value, boolean specified)
            throws XmlException {
        if(name == null) throw new XmlException("Attribute has no name",
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
        // If we have a non-default namespace, then prepend the namespace.
        if (_namespace != DEFAULT_NAMESPACE && name.equals("name")) {
            value = _namespace + ":" + value;
        }
        // NOTE: value may be null if attribute default is #IMPLIED.
        _attributes.put(name, value);
    }

    /** Handle character data.  In this implementation, the
     *  character data is accumulated in a buffer until the
     *  end element.  Character data appears only in doc elements.
     *  &AElig;lfred will call this method once for each chunk of
     *  character data found in the contents of elements.  Note that
     *  the parser may break up a long sequence of characters into
     *  smaller chunks and call this method once for each chunk.
     *  @param chars The character data.
     *  @param offset The starting position in the array.
     *  @param length The number of characters available.
     */
    public void charData(char[] chars, int offset, int length) {
        // If we haven't initialized _currentCharData, then we don't
        // care about character data, so we ignore it.
        if (_currentCharData != null) {
            _currentCharData.append(chars, offset, length);
        }
    }

    /** End the document. The MoMLParser calls this method once, when
     *  it has finished parsing the complete XML document. It is
     *  guaranteed that this will be the last method called in the XML
     *  parsing process. As a consequence, it is guaranteed that all
     *  dependencies between parameters used in the XML description
     *  are resolved.
     *  @exception CancelException If an error occurs parsing one of the
     *   parameter values, and the user clicks on "cancel" to cancel the
     *   parse.
     */
    public void endDocument() throws Exception {
        // Force evaluation of parameters so that any listeners are notified.
        Iterator parameters = _paramsToParse.iterator();
        while(parameters.hasNext()) {
            Variable param = (Variable)parameters.next();
            try {
                param.getToken();
            } catch (IllegalActionException ex) {
                // NOTE: The following may throw a CancelException, which
                // will have the effect of cancelling the entire parse.
                MessageHandler.warning("Evaluating parameter "
                + param.getFullName() + " triggers exception:\n\n"
                + ex.getMessage());
            }
        }
    }

    /** End an element. This method pops the current container from
     *  the stack, if appropriate, and also adds specialized properties
     *  to the container, such as <i>_doc</i>, if appropriate.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
	if ( _skipRendition ) {
	     if (elementName.equals("rendition")) {
		 _skipRendition = false;
	     }
	} else if (elementName.equals("configure")) {
            try {
                ((Configurable)_current).configure(
                        _base, _configureSource, _currentCharData.toString());
            } catch (NoClassDefFoundError e) {
                // If we are running without a display and diva.jar
                // is not in the classpath, then we may get" 
                // "java.lang.NoClassDefFoundError: diva/canvas/Figure"
            }
        } else if (elementName.equals("doc")) {
            if (_currentDocName == null) {
                _currentDocName = "_doc_";
            }
            // If there already is a doc element with the specified name,
            // remove it.
            Attribute preexistingDoc = _current.getAttribute(_currentDocName);
            if (preexistingDoc != null) preexistingDoc.setContainer(null);

            // Create a new doc element only if there is character data.
            if (_currentCharData.length() > 0) {
                Documentation doc 
                        = new Documentation(_current, _currentDocName);
                doc.setValue(_currentCharData.toString());
            }
            _currentDocName = null;
        } else if (elementName.equals("group")) {
            try {
                _namespace = (String)_namespaces.pop();
            } catch (EmptyStackException ex) {
                _namespace = DEFAULT_NAMESPACE;
            }
	} else if (
                elementName.equals("property")
                || elementName.equals("class")
                || elementName.equals("deleteEntity")
                || elementName.equals("deletePort")
                || elementName.equals("deleteProperty")
                || elementName.equals("deleteRelation")
                || elementName.equals("director")
                || elementName.equals("entity")
                || elementName.equals("model")
                || elementName.equals("port")
                || elementName.equals("relation")
                || elementName.equals("rendition")
                || elementName.equals("vertex")) {
            try {
                _current = (NamedObj)_containers.pop();
                _namespace = (String)_namespaces.pop();
            } catch (EmptyStackException ex) {
                // We are back at the top level.
                _current = null;
                _namespace = DEFAULT_NAMESPACE;
            }
        }
    }

    /** Handle the end of an external entity.  This pops the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void endExternalEntity(String systemId) {
        _externalEntities.pop();
    }

    /** Indicate a fatal XML parsing error.
     *  &AElig;lfred will call this method whenever it encounters
     *  a serious error.  This method simply throws an XmlException.
     *  @param message The error message.
     *  @param systemId The URI of the entity that caused the error.
     *  @param line The approximate line number of the error.
     *  @param column The approximate column number of the error.
     *  @exception XmlException If called.
     */
    public void error(String message, String sysid,
            int line, int column) throws XmlException {
        throw new XmlException(message, _currentExternalEntity(), line, column);
    }

    /** Get the top-level entity associated with this parser, or null if none.
     *  @return The top-level associated with this parser.
     */
    public NamedObj getToplevel() {
        return _toplevel;
    }

    /** Parse the MoML file at the given URL, which may be a file
     *  on the local file system, using the specified base
     *  to expand any relative references within the MoML file.
     *  This method uses parse(URL, InputStream).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, URL input)
            throws Exception {
        return parse(base, input.openStream());
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  This method uses parse(URL, Reader).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param input The stream from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, InputStream input)
            throws Exception {
        return parse(base, new InputStreamReader(input));
    }

    /** Parse the given stream, using the specified url as the base
     *  to expand any external references within the MoML file.
     *  The reader is wrapped in a BufferedReader before being used.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param reader The reader from which to read XML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, Reader reader) throws Exception {
        _parser.setHandler(this);
        _base = base;
        Reader buffered = new BufferedReader(reader);
        if (base == null) {
            _parser.parse(null, null, buffered);
        } else {
            _parser.parse(base.toExternalForm(), null, buffered);
        }
        return _toplevel;
    }

    /** Parse the given string, which contains MoML.
     *  If there are external references in the MoML, they are interpreted
     *  relative to the current working directory.
     *  Note that this method attempts to read the user.dir system
     *  property, which is not generally available in applets.  Hence
     *  it is probably not a good idea to use this method in applet code,
     *  since it will probably fail outright.
     *  @param text The string from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     *  @exception SecurityException If the user.dir system property is
     *  not available.
     */
    public NamedObj parse(String text) throws Exception {
        URL base = null;
        // Use the current working directory as a base.
	String cwd = System.getProperty("user.dir");
	if (cwd != null) {
	    base = new URL("file", null, cwd);
	}

	return parse(base, new StringReader(text));
    }

    /** Parse the given string, which contains MoML, using the specified
     *  base to evaluate relative references.
     *  This method uses parse(URL, Reader).
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param text The string from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    public NamedObj parse(URL base, String text) throws Exception {
        return parse(base, new StringReader(text));
    }

    /** Parse the given file, which contains MoML.
     *  If there are external references in the MoML, they are interpreted
     *  relative to the current working directory.
     *  Note that this method attempts to read the user.dir system
     *  property, which is not generally available in applets.  Hence
     *  it is probably not a good idea to use this method in applet code,
     *  since it will probably fail outright.
     *  @param text The file name from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     *  @exception SecurityException If the user.dir system property is
     *  not available.
     */
    public NamedObj parseFile(String filename) throws Exception {
        URL base = null;
        // Use the current working directory as a base.
	String cwd = System.getProperty("user.dir");
	if (cwd != null) {
	    base = new URL("file", null, cwd);
	}
		
        // Java's I/O is so lame that it can't find files in the current
        // working directory...
        FileReader input = new FileReader(new File(new File(cwd), filename));
        return parse(base, input);
    }

    /** Handle a processing instruction.  Processing instructions
     *  are allowed in doc and configure elements, and are passed through
     *  unchanged.  In the case of the doc element, they will be stored
     *  in the Documentation attribute.  In the case of the configure
     *  element, they will be passed to the configure() method
     *  of the parent object.
     *  @param target The name of the processing instruction.
     *  @param data The body of the processing instruction.
     */
    public void processingInstruction (String target, String data) {
        if (_currentCharData != null) {
            _currentCharData.append("<?" + target + " " + data + "?>");
        }
    }

    /** Reset the MoML parser, forgetting about any previously parsed
     *  models.
     */
    public void reset() {
        _attributes = new HashMap();
        _containers = new Stack();
        _namespaces = new Stack();
        _toplevel = null;
        _current = null;
        _namespace = DEFAULT_NAMESPACE;
    }

    /** Resolve an external entity.  If the first argument is the
     *  name of the MoML PUBLIC DTD ("-//UC Berkeley//DTD MoML 1//EN"),
     *  then return a StringReader
     *  that will read the locally cached version of this DTD
     *  (the public variable MoML_DTD_1). Otherwise, return null,
     *  which has the effect of deferring to &AElig;lfred for
     *  resolution of the URI.  Derived classes may return a
     *  a modified URI (a string), an InputStream, or a Reader.
     *  In the latter two cases, the input character stream is
     *  provided.
     *  @param publicId The public identifier, or null if none was supplied.
     *  @param systemId The system identifier.
     *  @return Null, indicating to use the default system identifier.
     */
    public Object resolveEntity(String publicID, String systemID) {
        if (publicID != null &&
                publicID.equals("-//UC Berkeley//DTD MoML 1//EN")) {
            // This is the generic MoML DTD.
            return new StringReader(MoML_DTD_1);
        } else {
            return null;
        }
    }

    /** Set the context for parsing.  This can be used to associate this
     *  parser with a pre-existing model, which can then be modified
     *  via incremental parsing.  This calls reset() and sets the top-level
     *  entity to the top-level of the specified object.
     *  @param context The context for parsing.
     */
    public void setContext(NamedObj context) {
        reset();
        Nameable toplevel = context;
        while (toplevel.getContainer() != null) {
            toplevel = toplevel.getContainer();
        }
        _toplevel = (NamedObj)toplevel;
        _current = context;
    }

    /** Set the top-level entity.  This can be used to associate this
     *  parser with a pre-existing model, which can then be modified
     *  via incremental parsing.  This calls reset().
     *  @param toplevel The top-level to associate with this parser.
     */
    public void setToplevel(NamedObj toplevel) {
        reset();
        _toplevel = toplevel;
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     *  In this implementation, this method does nothing.
     */
    public void startDocument() {
        _paramsToParse.clear();
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        _currentElement = elementName;
        try {
	    if (_skipRendition) {
		return;
	    }
           // NOTE: The elements are alphabetical below...
            // NOTE: I considered using reflection to invoke a set of
            // methods with names that match the element names.  However,
            // since we can't count on the XML parser to enforce the DTD,
            // this seems dangerous.  It could result in being able to write
            // an XML that would call methods of this class that are not
            // intended to be called, simply by putting in an element
            // whose name matches the method name.  So instead, we do
            // a dumb if...then...elseif... chain with string comparisons.
            if (elementName.equals("class")) {
                String className = (String)_attributes.get("extends");
                _checkForNull(className,
                        "Missing \"extends\" attribute for element \"class\"");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"class\"");
                NamedObj newEntity = _createEntity(className, entityName);
                if (_current != null) {
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                } else if (_toplevel == null) {
                    // NOTE: Used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = newEntity.toplevel();
                }
                newEntity.setMoMLElementName("class");
                _current = newEntity;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("configure")) {
                _checkClass(_current, Configurable.class,
                        "Element \"configure\" found inside an element that "
                        + "does not implement Configurable. It is: "
                        + _current);
                _configureSource = (String)_attributes.get("source");
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("deleteEntity")) {
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName,
                        "No name for element \"deleteEntity\"");
                NamedObj deletedEntity = _deleteEntity(entityName);
                if (deletedEntity instanceof Placeable) {
                    // FIXME: remove and revalidate graphical container.
                    // This seems to require an extension to the Placeable
                    // interface.
                }
                // NOTE: This could occur at a top level, although it's
                // not clear what it means to delete a top-level entity.
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                }
                _current = deletedEntity;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("deletePort")) {
                String portName = (String)_attributes.get("name");
                _checkForNull(portName,
                        "No name for element \"deletePort\"");
                NamedObj deletedPort = _deletePort(portName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                }
                _current = deletedPort;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("deleteProperty")) {
                String propName = (String)_attributes.get("name");
                _checkForNull(propName,
                        "No name for element \"deleteProperty\"");
                NamedObj deletedProp = _deleteProperty(propName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                }
                _current = deletedProp;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("deleteRelation")) {
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName,
                        "No name for element \"deleteRelation\"");
                NamedObj deletedRelation = _deleteRelation(relationName);
                if (_current != null) {
                    // Although there is not supposed to be anything inside
                    // this element, we nontheless change the environment so
                    // that if by using a nonvalidating parser elements are
                    // included, then they will be evaluated in the correct
                    // context.
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                }
                _current = deletedRelation;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("director")) {
                // NOTE: We do not check for a previously existing director.
                // There is presumably no harm in just creating a new one.
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"director\"");
                String dirName = (String)_attributes.get("name");
                _checkForNull(dirName, "No name for element \"director\"");
                _checkClass(_current, CompositeActor.class,
                        "Element \"director\" found inside an element that "
                        + "is not a CompositeActor. It is: "
                        + _current);
                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = dirName;
                NamedObj container = _current;
                _containers.push(_current);
                _namespaces.push(_namespace);
                Class newClass = Class.forName(className, true, _classLoader);
                _current = _createInstance(newClass, arguments);
                _namespace = DEFAULT_NAMESPACE;

                // If the container is cloned from something, then
                // add to it a MoML description of the director, so that
                // this new director will be persistent.
                // NOTE: This is no longer needed, since Director is
                // now an attribute, and hence always exported.
                // _recordNewObject(container, _current);

            } else if (elementName.equals("doc")) {
                _currentDocName = (String)_attributes.get("name");
                _currentCharData = new StringBuffer();

            } else if (elementName.equals("entity")) {
                String className = (String)_attributes.get("class");
                String entityName = (String)_attributes.get("name");
                _checkForNull(entityName, "No name for element \"entity\"");
                NamedObj newEntity = _createEntity(className, entityName);
                // NOTE: We tolerate entities at the top level, even
                // though this is not proper MoML.
                if (_current != null) {
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                } else if (_toplevel == null) {
                    // NOTE: We used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = newEntity.toplevel();
                }
                _current = newEntity;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("group")) {
                String groupName = (String)_attributes.get("name");
                if (groupName != null) {
                    // Defining a namespace.
                    _namespaces.push(_namespace);
                    _namespace = groupName;
                } else {
                    _namespaces.push(DEFAULT_NAMESPACE);
                    _namespace = DEFAULT_NAMESPACE;
                }

            } else if (elementName.equals("import")) {
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"import\"");

                // If the base is specified, use that.
                // Otherwise, use this document's base.
                String baseSpec = (String)_attributes.get("base");
                URL base = _base;
                if (baseSpec != null) {
                    base = new URL(base, baseSpec);
                }

                // Read external model definition in a new parser,
                // rather than in the current context.
                MoMLParser newParser =
		    new MoMLParser(_workspace, _classLoader);
                NamedObj reference = _parse(newParser, base, source);

                if (_imports == null) {
                    _imports = new LinkedList();
                }
                // Put the entity at the top of the list so that if there
                // are name duplications in the imports, the most recent
                // import prevails.
                _imports.add(0, reference);

                MoMLAttribute attr = new MoMLAttribute(_current,
                       _current.uniqueName("_import"));
                attr.appendMoMLDescription("<import base=\""
                        + _base.toExternalForm()
                        + "\" source=\""
                        + source
                        + "\"/>");

            } else if (elementName.equals("input")) {
                String source = (String)_attributes.get("source");
                _checkForNull(source, "No source for element \"input\"");

                // If the base is specified, use that.
                // Otherwise, use this document's base.
                String baseSpec = (String)_attributes.get("base");
                URL base = _base;
                if (baseSpec != null) {
                    base = new URL(base, baseSpec);
                }

                // Read external file in the current context, but with
                // a new parser.
                MoMLParser newParser =
		    new MoMLParser(_workspace, _classLoader);
                newParser.setContext(_current);
                newParser._propagating = _propagating;
                _parse(newParser, base, source);

            } else if (elementName.equals("link")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                String relationName = (String)_attributes.get("relation");
                _checkForNull(relationName,
                        "No relation for element \"link\"");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"link\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);

                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                // Get relation
                Relation tmpRelation = context.getRelation(relationName);
                _checkForNull(tmpRelation, "No relation named \"" +
                        relationName + "\" in " + context.getFullName());
                ComponentRelation relation = (ComponentRelation)tmpRelation;

                String insertAtSpec = (String)_attributes.get("insertAt");
                if (insertAtSpec == null) {
                    port.link(relation);
                } else {
                    int insertAt = Integer.parseInt(insertAtSpec);
                    port.insertLink(insertAt, relation);
                }

                // If the container is cloned from something, then
                // add to it a MoML description of the new link, so that
                // this new link will be persistent.
                _recordLink(context, portName, relationName, insertAtSpec);

             } else if (elementName.equals("location")) {
                String value = (String)_attributes.get("value");
                _checkForNull(value, "No value for element \"location\"");
                _checkClass(_current, Locatable.class,
                       "Element \"location\" found inside an element that "
                       + "is not Locatable. It is: "
                       + _current);

                // Parse the specification.
                int comma = value.indexOf(",");
                String xSpec = null;
                String ySpec = null;
                String zSpec = null;
                if (comma < 0) {
                    // Only one dimension given.
                    xSpec = value.trim();
                } else {
                    xSpec = value.substring(0, comma).trim();
                    if (value.length() > comma + 1) {
                        String rest = value.substring(comma + 1);
                        comma = rest.indexOf(",");
                        if (comma < 0) {
                            // No more dimensions given.
                            ySpec = rest.trim();
                        } else {
                            // A third dimension is given.
                            ySpec = rest.substring(0, comma).trim();
                            if (rest.length() > comma + 1) {
                                zSpec = rest.substring(comma + 1).trim();
                            }
                        }
                    }
                }

                double x = Double.parseDouble(xSpec);
                if (ySpec != null) {
                    double y = Double.parseDouble(ySpec);
                    if (zSpec != null) {
                        // Have three dimensions.
                        double z = Double.parseDouble(zSpec);
                        double[] location = {x, y, z};
                        ((Locatable)_current).setLocation(location);
                    } else {
                        // Have two dimensions.
                        double[] location = {x, y};
                        ((Locatable)_current).setLocation(location);
                    }
                } else {
                    // Have one dimension.
                    double[] location = {x};
                    ((Locatable)_current).setLocation(location);
                }

            } else if (elementName.equals("model")) {
                String className = (String)_attributes.get("class");
                String modelName = (String)_attributes.get("name");
                _checkForNull(modelName, "No name for element \"model\"");

                NamedObj newModel;
                if (className != null) {
                    Object[] arguments = new Object[1];
                    arguments[0] = _workspace;
                    Class newClass
                            = Class.forName(className, true, _classLoader);
                    newModel = _createInstance(newClass, arguments);
                    newModel.setName(modelName);
                    newModel.setMoMLElementName("model");
                } else {
                    // Look for previously existing model.
                    newModel = _searchForEntity(modelName, true);
                    if (newModel == null) {
                        throw new XmlException(
                                "No class given for element \"model\".",
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                    }
                }
                if (_current != null) {
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                } else if (_toplevel == null) {
                    // NOTE: We used to set _toplevel to newEntity, but
                    // this isn't quite right because the entity may have a
                    // composite name.
                    _toplevel = newModel.toplevel();
                }
                _current = newModel;
                _namespace = DEFAULT_NAMESPACE;

            } else if (elementName.equals("port")) {
                String className = (String)_attributes.get("class");
                String portName = (String)_attributes.get("name");
                _checkForNull(portName, "No name for element \"port\"");

                _checkClass(_current, Entity.class,
                        "Element \"port\" found inside an element that "
                        + "is not an Entity. It is: "
                        + _current);
                Entity container = (Entity)_current;

                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className, true, _classLoader);
                }
                Port port = container.getPort(portName);
                if (port != null) {
                    if (newClass != null) {
                        // Previously existing port with the specified name.
                        _checkClass(port, newClass,
                                "port named \"" + portName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                } else {
                    // No previously existing port with this name.
                    if (className == null) {
                        // Classname is not given.  Invoke newPort() on the
                        // container.
                        port = container.newPort(portName);
                    } else {
                        // Classname is given.
                        Object[] arguments = new Object[2];
                        arguments[0] = container;
                        arguments[1] = portName;
                        port = (Port)_createInstance(newClass, arguments);
                    }

                    // If the container is cloned from something, then
                    // add to it a MoML description of the port, so that
                    // this new port will be persistent.
                    _recordNewObject(container, port);
                }
                _containers.push(_current);
                _namespaces.push(_namespace);
                _current = port;
                _namespace = DEFAULT_NAMESPACE;

                if (port instanceof IOPort) {
                    String direction = (String)_attributes.get("direction");
                    if (direction != null) {
                        IOPort ioport = (IOPort)port;
                        ioport.setOutput(direction.equals("output")
                                || direction.equals("both"));
                        ioport.setInput(direction.equals("input")
                                || direction.equals("both"));
                    }
                }

            } else if (elementName.equals("property")) {
                String propertyName = (String)_attributes.get("name");
                _checkForNull(propertyName,
                        "No name for element \"property\"");
                String value = (String)_attributes.get("value");

                // First handle special properties that are not translated
                // into Ptolemy II attributes.
                // Note that we have to push something on to the
                // stack so that we can pop it off later.
                // An xml version of the FSM ABP demo tickled this bug
                boolean isIOPort = (_current instanceof IOPort);
                if (propertyName.equals("multiport") && isIOPort) {
                    // The mere presense of a named property "multiport"
                    // makes the enclosing port a multiport.
                    ((IOPort)_current).setMultiport(true);
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = DEFAULT_NAMESPACE;
                } else if (propertyName.equals("output") && isIOPort) {
                    ((IOPort)_current).setOutput(true);
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = DEFAULT_NAMESPACE;
                } else if (propertyName.equals("input") && isIOPort) {
                    ((IOPort)_current).setInput(true);
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    _current =  (Attribute)
                        _current.getAttribute(propertyName);
                    _namespace = DEFAULT_NAMESPACE;
                } else {
                    // Ordinary attribute.
                    NamedObj property = (Attribute)
                            _current.getAttribute(propertyName);
                    String className = (String)_attributes.get("class");
                    Class newClass = null;
                    if (className != null) {
                        newClass = 
			    Class.forName(className, true, _classLoader);
                    }

                    if (property == null) {
                        // No previously existing attribute with this name.
                        if (newClass == null) {
                            newClass = Attribute.class;
                        }

                        // Invoke the constructor.
                        Object[] arguments = new Object[2];
                        arguments[0] = _current;
                        arguments[1] = propertyName;
                        property = _createInstance(newClass, arguments);

                        if (value != null) {
                            if (!(property instanceof Settable)) {
                                throw new XmlException("Property is not an "
                                + "instance of Settable, so can't set value.",
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                            }
                            Settable settable = (Settable)property;
                            settable.setExpression(value);
                            if (property instanceof Variable) {
                                // Add to the list of parameters to evaluate
                                // in endDocument().
                                _paramsToParse.add(property);
                            }
                        }
                    } else {
                        // Previously existing property with this name.
                        if (newClass != null) {
                            // Check that it has the right class.
                            _checkClass(property, newClass,
                            "property named \"" + propertyName
                            + "\" exists and is not an instance of "
                            + className);
                        }
                        // If value is null and the property already
                        // exists, then there is nothing to do.
                        if (value != null) {
                            if (!(property instanceof Settable)) {
                                throw new XmlException("Property is not an "
                                + "instance of Settable, so can't set value.",
                                _currentExternalEntity(),
                                _parser.getLineNumber(),
                                _parser.getColumnNumber());
                            }
                            Settable settable = (Settable)property;
                            settable.setExpression(value);
                            if (property instanceof Variable) {
                                // Add to the list of parameters to evaluate
                                // in endDocument().
                                _paramsToParse.add(property);
                            }
                        }
                    }
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    _current = property;
                    _namespace = DEFAULT_NAMESPACE;
                }

            } else if (elementName.equals("relation")) {
                String className = (String)_attributes.get("class");
                String relationName = (String)_attributes.get("name");
                _checkForNull(relationName, "No name for element \"relation\"");
                _checkClass(_current, CompositeEntity.class,
                        "Element \"relation\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);
                CompositeEntity container = (CompositeEntity)_current;
                Class newClass = null;
                if (className != null) {
                    newClass = Class.forName(className, true, _classLoader);
                }
                Relation relation = container.getRelation(relationName);
                if (relation == null) {
                    // No previous relation with this name.
                    NamedObj newRelation = null;
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    if (newClass == null) {
                        // No classname. Use the newRelation() method.
                        newRelation = container.newRelation(relationName);
                    } else {
                        Object[] arguments = new Object[2];
                        arguments[0] = (CompositeEntity)_current;
                        arguments[1] = relationName;
                        newRelation = _createInstance(newClass, arguments);
                    }
                    _namespace = DEFAULT_NAMESPACE;

                    // If the container is cloned from something, then
                    // add to it a MoML description of the relation, so that
                    // this new relation will be persistent.
                    _recordNewObject(_current, newRelation);
                    _current = newRelation;

                } else {
                    // Previously existing relation with the specified name.
                    if (newClass != null) {
                        _checkClass(relation, newClass,
                                "relation named \"" + relationName
                                + "\" exists and is not an instance of "
                                + className);
                    }
                    _containers.push(_current);
                    _namespaces.push(_namespace);
                    _current = relation;
                    _namespace = DEFAULT_NAMESPACE;
                }

            } else if (elementName.equals("rendition")) {
                String className = (String)_attributes.get("class");
                _checkForNull(className, "No class for element \"rendition\"");

                Object[] arguments = new Object[2];
                arguments[0] = _current;
                arguments[1] = "_icon";

                _containers.push(_current);
                _namespaces.push(_namespace);
		try {
		    Class newClass = Class.forName(className, true,
                            _classLoader);
		    _current = _createInstance(newClass, arguments);
		    _namespace = DEFAULT_NAMESPACE;
		} catch (NoClassDefFoundError e) {
		    // If we are running the nightly tests, then we
		    // have no display, and diva.jar is not in the class
		    // path, so when we process a rendition, we will
		    // get errors like:
		    // "java.lang.NoClassDefFoundError: diva/canvas/Figure"
		    // The fix is to skip the rest of the rendition.
		    _skipRendition = true;
		    _current = (NamedObj)_containers.pop();
		    _namespace = (String)_namespaces.pop();
		}


            } else if (elementName.equals("unlink")) {
                String portName = (String)_attributes.get("port");
                _checkForNull(portName, "No port for element \"link\"");
                String relationName = (String)_attributes.get("relation");
                String indexSpec = (String)_attributes.get("index");
                String insideIndexSpec = (String)_attributes.get("insideIndex");

                _checkClass(_current, CompositeEntity.class,
                        "Element \"unlink\" found inside an element that "
                        + "is not a CompositeEntity. It is: "
                        + _current);

                CompositeEntity context = (CompositeEntity)_current;

                // Parse port
                ComponentPort port = _getPort(portName, context);

                int countArgs = 0;
                if (indexSpec != null) countArgs++;
                if (insideIndexSpec != null) countArgs++;
                if (relationName != null) countArgs++;
                if (countArgs != 1) {
                    throw new XmlException(
                             "Element unlink requires exactly one of "
                             + "an index, an insideIndex, or a relation.",
                             _currentExternalEntity(),
                             _parser.getLineNumber(),
                             _parser.getColumnNumber());
                }

                if (relationName != null) {
                    // Get relation
                    Relation tmpRelation = context.getRelation(relationName);
                    _checkForNull(tmpRelation, "No relation named \"" +
                            relationName + "\" in " + context.getFullName());
                    ComponentRelation relation = (ComponentRelation)tmpRelation;
                    port.unlink(relation);
                } else if (indexSpec != null) {
                    // index is given.
                    int index = Integer.parseInt(indexSpec);
                    port.unlink(index);
                } else {
                    // insideIndex is given.
                    int index = Integer.parseInt(insideIndexSpec);
                    port.unlinkInside(index);
                }
                // If the container is cloned from something, then
                // add to it a MoML description of the unlink, so that
                // this new entity will be persistent.
                _recordUnlink(context, portName, relationName,
                        indexSpec, insideIndexSpec);

            } else if (elementName.equals("vertex")) {
                String vertexName = (String)_attributes.get("name");
                _checkForNull(vertexName, "No name for element \"vertex\"");

                _checkClass(_current, Relation.class,
                        "Element \"vertex\" found inside an element that "
                        + "is not a Relation. It is: "
                        + _current);

                // Create an instance of Vertex and attach it to the Relation.
                Vertex vertex = new Vertex((Relation)_current, vertexName);

                _containers.push(_current);
                _namespaces.push(_namespace);
                _current = vertex;
                _namespace = DEFAULT_NAMESPACE;
            }
        } catch (InvocationTargetException ex) {
            // NOTE: While debugging, we print a stack trace here.
            // This is because XmlException loses it.
            System.err.println("******** original error:");
            ex.printStackTrace();

            // A constructor or method invoked via reflection has
            // triggered an exception.
            String msg = "XML element \"" + elementName
                    + "\" triggers exception:\n  "
                    + ex.getTargetException().toString();
            throw new XmlException(msg,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        } catch (Exception ex) {
            if (ex instanceof XmlException) {

                // NOTE: While debugging, we print a stack trace here.
                // This is because XmlException loses it.
                // System.err.println("******** original error:");
                // ex.printStackTrace();

                throw (XmlException)ex;
            } else {
                String msg = "XML element \"" + elementName
                        + "\" triggers exception:\n  " + ex.toString();

                // NOTE: While debugging, we print a stack trace here.
                // This is because XmlException loses it.
                // FIXME: Printing stack trace temporarily.
                System.err.println("******** original error:");
                ex.printStackTrace();

                throw new XmlException(msg,
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
        }
        _attributes.clear();
    }

    /** Handle the start of an external entity.  This pushes the stack so
     *  that error reporting correctly reports the external entity that
     *  causes the error.
     *  @param systemId The URI for the external entity.
     */
    public void startExternalEntity(String systemId) {
        // NOTE: The Microstar XML parser incorrectly passes the
        // HTML file for the first external entity, rather than
        // XML file.  So error messages typically refer to the wrong file.
        _externalEntities.push(systemId);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The standard MoML DTD, represented as a string.  This is used
     *  to parse MoML data when a compatible PUBLIC DTD is specified.
     */
    public static String MoML_DTD_1 = "<!ELEMENT model (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | property | relation | rendition | unlink)*><!ATTLIST model name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT class (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | property | relation | rendition | unlink)*><!ATTLIST class name CDATA #REQUIRED extends CDATA #IMPLIED><!ELEMENT configure (#PCDATA)><!ATTLIST configure source CDATA #IMPLIED><!ELEMENT deleteEntity EMPTY><!ATTLIST deleteEntity name CDATA #REQUIRED><!ELEMENT deletePort EMPTY><!ATTLIST deletePort name CDATA #REQUIRED><!ELEMENT deleteProperty EMPTY><!ATTLIST deleteProperty name CDATA #REQUIRED><!ELEMENT deleteRelation EMPTY><!ATTLIST deleteRelation name CDATA #REQUIRED><!ELEMENT director (configure | property)*><!ATTLIST director name CDATA \"director\" class CDATA #REQUIRED><!ELEMENT doc (#PCDATA)><!ATTLIST doc name CDATA \"_doc_\"><!ELEMENT entity (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | port | property | relation | rendition | unlink)*><!ATTLIST entity name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT group ANY><!ATTLIST group name CDATA #IMPLIED><!ELEMENT import EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT input EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT link EMPTY><!ATTLIST link insertAt CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #REQUIRED vertex CDATA #IMPLIED><!ELEMENT location EMPTY><!ATTLIST location value CDATA #REQUIRED><!ELEMENT port (configure | doc | property)*><!ATTLIST port class CDATA #IMPLIED name CDATA #REQUIRED><!ELEMENT property (configure | doc | property)*><!ATTLIST property class CDATA #IMPLIED name CDATA #REQUIRED value CDATA #IMPLIED><!ELEMENT relation (property | vertex)*><!ATTLIST relation name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT rendition (configure | location | property)*><!ATTLIST rendition class CDATA #REQUIRED><!ELEMENT unlink EMPTY><!ATTLIST unlink index CDATA #IMPLIED insideIndex CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #REQUIRED><!ELEMENT vertex (location | property)*><!ATTLIST vertex name CDATA #REQUIRED pathTo CDATA #IMPLIED>";

    // NOTE: The master file for the above DTD is at
    // $PTII/ptolemy/moml/MoML_1.dtd.  If modified, it needs to be also
    // updated at ptweb/xml/dtd/MoML_1.dtd.

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the the URI for the current external entity.
     *  @return A string giving the URI of the external entity being read,
     *   or null if none.
     */
    protected String _currentExternalEntity() {
        return (String)_externalEntities.peek();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package freindly variables        ////

    // Indicator that the MoML currently being evaluated is the result
    // of propagating a change from a master to something that was cloned
    // from the master.  This is set by MoMLChangeRequest only.
    boolean _propagating = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // If the first argument is not an instance of the second,
    // throw an exception with the given message.
    private void _checkClass(Object object, Class correctClass, String msg)
            throws XmlException {
        if(!correctClass.isInstance(object)) {
            throw new XmlException(msg,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // If the argument is null, throw an exception with the given message.
    private void _checkForNull(Object object, String message)
            throws XmlException {
        if(object == null) {
            throw new XmlException(message,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
    }

    // Create a new entity from the specified class name, give
    // it the specified entity name, and specify that its container
    // is the current container object.  If the current container
    // already contains an entity with the specified name and class,
    // then return that entity.  If the class name matches
    // a class that has been previously defined (by an absolute
    // or relative name), then that class is cloned.  Otherwise,
    // the class name is interpreted as a Java class name and we
    // attempt to construct the entity.  If _current is not an instance
    // of CompositeEntity, then an XML exception is thrown.
    private NamedObj _createEntity(String className, String entityName)
                 throws Exception {
        if (_current != null && !(_current instanceof CompositeEntity)) {
            throw new XmlException("Cannot create an entity inside "
                   + "of another that is not a CompositeEntity.",
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
        CompositeEntity container = (CompositeEntity)_current;
        ComponentEntity previous = _searchForEntity(entityName, true);
        Class newClass = null;
        ComponentEntity reference = null;
        if (className != null) {
            reference = _searchForEntity(className, false);
            if (reference == null) {
                try {
                    newClass = Class.forName(className, true, _classLoader);
                } catch (Exception ex) {
                    // NOTE: Java sometimes throws ClassNotFoundException
                    // and sometimes NullPointerException when the class
                    // does not exist.  Hence the broad catch here.
                    throw new XmlException("Cannot find the class "
                            + className,
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());                   
                }
	    }
        }
        if (previous != null) {
            if (newClass != null) {
                _checkClass(previous, newClass,
                        "entity named \"" + entityName
                        + "\" exists and is not an instance of "
                        + className);
            }
            return previous;
        }

        // No previous entity.  Class name is required.
        _checkForNull(className, "Cannot create entity without a class name.");

        // Next check to see whether the class extends a named entity.
        if (reference == null) {
            // Not a named entity. Invoke the class loader.
            if (_current != null) {
                _checkClass(_current, CompositeEntity.class,
                       "Cannot create an entity inside an element that "
                       + "is not a CompositeEntity. It is: "
                       + _current);
                Object[] arguments = new Object[2];

                arguments[0] = _current;
                arguments[1] = entityName;
                NamedObj newEntity = _createInstance(newClass, arguments);

                // If the container is cloned from something, then
                // add to it a MoML description of the entity, so that
                // this new entity will be persistent.
                _recordNewObject(container, newEntity);
                return newEntity;
            } else {
                Object[] arguments = new Object[1];
                arguments[0] = _workspace;
                NamedObj result = _createInstance(newClass, arguments);
                result.setName(entityName);
                return result;
            }
        } else {
            // Extending a previously defined entity.  Check to see that
            // it was defined to be a class.
            if (!reference.getMoMLElementName().equals("class")) {
                throw new XmlException("Attempt to extend an entity that "
                + "is not a class: " + reference.getFullName(),
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
            }

            // Clone it.
            ComponentEntity newEntity = (ComponentEntity)reference.clone();

            // Set the name of the clone.
            // NOTE: The container is null, so there will be no
            // name conflict here.  If we were to set the name after
            // setting the container, we could get a spurious name conflict
            // when we set the container.
            newEntity.setName(entityName);

            // Set the container of the clone.
            newEntity.setContainer(container);

            // The master may have an entity name "class" (or "model"?), and
            // that name will be cloned, so we need to change this.
            // It may get changed back if we are inside a "class" element.
            newEntity.setMoMLElementName("entity");

            // If the container is cloned from something, then
            // add to it a MoML description of the entity, so that
            // this new entity will be persistent.
            _recordNewObject(container, newEntity);

            return newEntity;
        }
    }

    // Create an instance of the specified class name by finding a
    // constructor that matches the specified arguments.  The specified
    // class must be NamedObj or derived, or a ClassCastException will
    // be thrown.
    // @param newClass The class.
    // @param arguments The constructor arguments.
    // @exception Exception If no matching constructor is found, or if
    //  invoking the constructor triggers an exception.
    private NamedObj _createInstance(Class newClass, Object[] arguments)
            throws Exception {
        Constructor[] constructors = newClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != arguments.length) continue;
            boolean match = true;
            for (int j = 0; j < parameterTypes.length; j++) {
                if (!(parameterTypes[j].isInstance(arguments[j]))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return (NamedObj)constructor.newInstance(arguments);
            }
        }
        // If we get here, then there is no matching constructor.
        throw new XmlException("Cannot find a suitable constructor for "
                + newClass.getName(),
                _currentExternalEntity(),
                _parser.getLineNumber(),
                _parser.getColumnNumber());
    }

    // Delete the entity after verifying that it is contained (deeply)
    // by the current environment.
    private NamedObj _deleteEntity(String entityName) throws Exception {
        ComponentEntity toDelete = _searchForEntity(entityName, true);
        if (toDelete == null) {
            throw new XmlException("No such entity to delete: " + entityName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        toDelete.setContainer(null);

        // If the container is cloned from something, then
        // add to it a MoML description of the deletion, so that
        // this deletion will be persistent.
        _recordDeletion("Entity", _current, entityName);

        return toDelete;
    }

    // Delete the port after verifying that it is contained (deeply)
    // by the current environment.
    private Port _deletePort(String portName) throws Exception {
        Port toDelete = _searchForPort(portName);
        if (toDelete == null) {
            throw new XmlException("No such port to delete: "
                    + portName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        toDelete.setContainer(null);

        // If the container is cloned from something, then
        // add to it a MoML description of the deletion, so that
        // this deletion will be persistent.
        _recordDeletion("Port", _current, portName);

        return toDelete;
    }

    // Delete the property after verifying that it is contained (deeply)
    // by the current environment.
    private Attribute _deleteProperty(String propName) throws Exception {
        Attribute toDelete = _searchForAttribute(propName);
        if (toDelete == null) {
            throw new XmlException("No such property to delete: "
                    + propName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        toDelete.setContainer(null);

        // If the container is cloned from something, then
        // add to it a MoML description of the deletion, so that
        // this deletion will be persistent.  Note that addition of
        // properties does not need to be recorded, because exportMoML()
        // always describes properties.  However, deletion of properties
        // does need to be recorded.
        _recordDeletion("Property", _current, propName);

        return toDelete;
    }

    // Delete the relation after verifying that it is contained (deeply)
    // by the current environment.
    private Relation _deleteRelation(String relationName) throws Exception {
        ComponentRelation toDelete = _searchForRelation(relationName);
        if (toDelete == null) {
            throw new XmlException("No such relation to delete: "
                    + relationName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        toDelete.setContainer(null);

        // If the container is cloned from something, then
        // add to it a MoML description of the deletion, so that
        // this deletion will be persistent.
        _recordDeletion("Relation", _current, relationName);

        return toDelete;
    }

    // Return the port corresponding to the specified port name in the
    // specified composite entity.  If the port belongs directly to the
    // composite entity, then the argument is a simple name.  If the
    // port belongs to a component entity, then the name is the entity
    // name, a period, and the port name.
    // Throw an exception if there is no such port.
    // The returned value is never null.
    private ComponentPort _getPort(String portspec, CompositeEntity context)
            throws XmlException {
        ComponentPort port = (ComponentPort)context.getPort(portspec);
        _checkForNull(port, "No port named \"" + portspec
                + "\" in " + context.getFullName());
        return (ComponentPort)port;
    }

    /** Use the specified parser to parse the a file or URL,
     *  which contains MoML, using the specified base to find the URL.
     *  If the URL cannot be found relative to this base, then it
     *  is searched for relative to the current working directory
     *  (if this is permitted with the current security restrictions),
     *  and then relative to the classpath.
     *  @param parser The parser to use.
     *  @param base The base URL for relative references, or null if
     *   not known.
     *  @param source The URL from which to read MoML.
     *  @return The top-level composite entity of the Ptolemy II model.
     *  @exception Exception If the parser fails.
     */
    private NamedObj _parse(MoMLParser parser, URL base, String source)
            throws Exception {
        URL xmlFile = new URL(base, source);
        InputStream input = null;
        try {
            input = xmlFile.openStream();
        } catch (IOException ex) {
            // Cannot open the file. Try to open it relative
            // to the current working directory.
            try {
                String cwd = System.getProperty("user.dir");
                if (cwd != null) {
                    base = new URL("file", null, cwd);
                    xmlFile = new URL(base, source);
                    input = xmlFile.openStream();
                }
            } catch (Exception exception) {
                // That failed.  Try opening it relative to the classpath.
                try {
                    xmlFile = _classLoader.getResource(source);
                    if (xmlFile != null) {
                        input = xmlFile.openStream();
                    }
                } catch (Exception anotherException) {}
            }
        }
        if (input == null) {
            throw new XmlException("Cannot open import file: "
                   + source + "\nUsing base: " + base,
                   _currentExternalEntity(),
                   _parser.getLineNumber(),
                   _parser.getColumnNumber());
        }
        return parser.parse(xmlFile, xmlFile.openStream());
    }

    // If an object is deleted from a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordDeletion(
            String type, NamedObj container, String deleted) {
        if (container.getDeferMoMLDefinitionTo() != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                       container.uniqueName("_extension"));
                attr.appendMoMLDescription(
                    "<delete" + type + " name=\"" + deleted + "\"/>");
            } catch (KernelException ex) {
                throw new InternalErrorException(
                    "Unable to record deletion from class!\n" + ex.toString());
            }
        }
    }

    // If a new link is added to a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordLink(
            NamedObj container,
            String port,
            String relation,
            String insertAtSpec) {
        if (container.getDeferMoMLDefinitionTo() != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                       container.uniqueName("_extension"));
                if (insertAtSpec == null) {
                    attr.appendMoMLDescription("<link port=\""
                           + port
                           + "\" relation=\""
                           + relation
                           + "\"/>");
                } else {
                    attr.appendMoMLDescription("<link port=\""
                           + port
                           + "\" relation=\""
                           + relation
                           + "\" insertAt=\""
                           + insertAtSpec
                           + "\"/>");
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(
                    "Unable to record extension to class!\n" + ex.toString());
            }
        }
    }

    // If a new object is added to a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordNewObject(NamedObj container, NamedObj newObj) {
        if (container.getDeferMoMLDefinitionTo() != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                       container.uniqueName("_extension"));
                attr.appendMoMLDescription(newObj.exportMoML());
            } catch (KernelException ex) {
                throw new InternalErrorException(
                    "Unable to record extension to class!\n" + ex.toString());
            }
        }
    }

    // If a link is deleted from a container, and this is not the
    // result of a propagating change from a master, then we need
    // to record the change so that it will be exported in any exported MoML.
    private void _recordUnlink(
            NamedObj container,
            String port,
            String relation,
            String indexSpec,
            String insideIndexSpec) {
        if (container.getDeferMoMLDefinitionTo() != null && !_propagating) {
            try {
                MoMLAttribute attr = new MoMLAttribute(container,
                       container.uniqueName("_extension"));
                if (relation != null) {
                    attr.appendMoMLDescription("<unlink port=\""
                           + port
                           + "\" relation=\""
                           + relation
                           + "\"/>");
                } else if (indexSpec != null) {
                    attr.appendMoMLDescription("<unlink port=\""
                           + port
                           + "\" index=\""
                           + indexSpec
                           + "\"/>");
                } else {
                    attr.appendMoMLDescription("<unlink port=\""
                           + port
                           + "\" insideIndex=\""
                           + insideIndexSpec
                           + "\"/>");
                }
            } catch (KernelException ex) {
                throw new InternalErrorException(
                    "Unable to record extension to class!\n" + ex.toString());
            }
        }
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find an attribute with that name.
    // Return null if it is not found.  The port is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private Attribute _searchForAttribute(String name)
             throws XmlException {
        Attribute result = null;
        // If the name is absolute, strip the prefix.
        String topLevelName = "(no top level)";
        if (_toplevel != null) {
            topLevelName = _toplevel.getFullName();
        }
        if (_toplevel != null && name.startsWith(topLevelName)) {
            int prefix = topLevelName.length();
            if (name.length() > prefix) {
                name = name.substring(1, name.length());
            }
        }
        // Now we are assured that name is relative.
        result = _current.getAttribute(name);
        if (result == null) {
            throw new XmlException("No such property: " + name
                    + " in " + topLevelName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }


    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a component entity with that name.
    // Return null if it is not found.  If the second argument is true,
    // then enforce that the entity found be within the current entity,
    // if there is one.
    private ComponentEntity _searchForEntity(String name,
            boolean enforceContainment) throws XmlException {

        // If the name is absolute, we first have to find a
        // name from the imports that matches.
        if (name.startsWith(".")) {
            // Name is absolute.
            String topLevelName;
            int nextPeriod = name.indexOf(".", 1);
            if (nextPeriod < 1) {
                topLevelName = name.substring(1);
            } else {
                topLevelName = name.substring(1, nextPeriod);
            }
            // First search the current top level, if the name matches.
            if (_toplevel != null && _toplevel instanceof ComponentEntity
                    && topLevelName.equals(_toplevel.getName())) {
                if (nextPeriod < 1) {
                    if (_current != null
                            && _current != _toplevel
                            && enforceContainment == true) {
                        throw new XmlException(
                            "Reference to an existing entity: "
                            + _toplevel.getFullName()
                            + " in an inappropriate context: "
                            + _current.getFullName(),
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                    }
                    return (ComponentEntity)_toplevel;
                } else {
                    if (name.length() > nextPeriod + 1) {
                        ComponentEntity result =
                                ((CompositeEntity)_toplevel).getEntity(
                                name.substring(nextPeriod + 1));
                        if (result != null) {
                            if (enforceContainment == true && _current != null                                      && !_current.deepContains(result)) {
                                throw new XmlException(
                                    "Reference to an existing entity: "
                                    + result.getFullName()
                                    + " in an inappropriate context: "
                                    + _current.getFullName(),
                                    _currentExternalEntity(),
                                    _parser.getLineNumber(),
                                    _parser.getColumnNumber());
                            }
                            return result;
                        }
                    }
                }
            }
            // Next search the imports.
            // NOTE: We assume that if the result is in the inports,
            // then we never want to enforce containment.  Thus, we only
            // search the imports if the second argument is false.
            if (_imports != null && enforceContainment == false) {
                Iterator entries = _imports.iterator();
                while (entries.hasNext()) {
                    Object possibleCandidate = entries.next();
                    if (possibleCandidate instanceof ComponentEntity) {
                        ComponentEntity candidate =
                                (ComponentEntity)possibleCandidate;
                        if (candidate.getName().equals(topLevelName)) {
                            if (nextPeriod < 1) {
                                // Found a match.
                                return candidate;
                            } else {
                                if (candidate instanceof CompositeEntity) {
                                    ComponentEntity result =
                                    ((CompositeEntity)candidate).getEntity(
                                            name.substring(nextPeriod + 1));
                                    if (result != null) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } else {
            // Name is relative.
            if (_current instanceof CompositeEntity) {
                ComponentEntity result =
                       ((CompositeEntity)_current).getEntity(name);
                if (result != null && enforceContainment == true &&
                       !_current.deepContains(result)) {
                    throw new XmlException(
                            "Reference to an existing entity: "
                            + result.getFullName()
                            + " in an inappropriate context: "
                            + _current.getFullName(),
                            _currentExternalEntity(),
                            _parser.getLineNumber(),
                            _parser.getColumnNumber());
                }
                return result;
            }
            if (_current == null) {
                // The name might be a top-level name, but without
                // the leading period.
                return _searchForEntity("." + name, enforceContainment);
            }
            return null;
        }
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a port with that name.
    // Return null if it is not found.  The port is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private Port _searchForPort(String name)
             throws XmlException {
        Port result = null;
        // If the name is absolute, strip the prefix.
        String topLevelName = "(no top level)";
        if (_toplevel != null) {
            topLevelName = _toplevel.getFullName();
        }
        if (_toplevel != null && name.startsWith(topLevelName)) {
            int prefix = topLevelName.length();
            if (name.length() > prefix) {
                name = name.substring(1, name.length());
            }
        }
        // Now we are assured that name is relative.
        if (_current instanceof Entity) {
            result = ((Entity)_current).getPort(name);
        }
        if (result == null) {
            throw new XmlException("No such port: " + name
                    + " in " + topLevelName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }

    // Given a name that is either absolute (with a leading period)
    // or relative to _current, find a relation with that name.
    // Return null if it is not found.  The relation is required to
    // be contained (deeply) by the current environment, or an XmlException
    // will be thrown.
    private ComponentRelation _searchForRelation(String name)
             throws XmlException {
        ComponentRelation result = null;
        // If the name is absolute, strip the prefix.
        String topLevelName = "(no top level)";
        if (_toplevel != null) {
            topLevelName = _toplevel.getFullName();
        }
        if (_toplevel != null && name.startsWith(topLevelName)) {
            int prefix = topLevelName.length();
            if (name.length() > prefix) {
                name = name.substring(1, name.length());
            }
        }
        // Now we are assured that name is relative.
        if (_current instanceof CompositeEntity) {
            result = ((CompositeEntity)_current).getRelation(name);
        }
        if (result == null) {
            throw new XmlException("No such relation: " + name
                    + " in " + topLevelName,
                    _currentExternalEntity(),
                    _parser.getLineNumber(),
                    _parser.getColumnNumber());
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // Attributes associated with an entity.
    private Map _attributes = new HashMap();

    // Base for relative URLs.
    private URL _base;

    // The class loader that will be used to instantiate objects.
    private ClassLoader _classLoader = getClass().getClassLoader();

    // The source attribute specified by the configure element.
    private String _configureSource;

    // The stack of objects that contain the current one.
    private Stack _containers = new Stack();

    // The current object in the hierarchy.
    private NamedObj _current;

    // The current character data for the current element.
    private StringBuffer _currentCharData;

    // The relation for the currently active connection.
    private ComponentRelation _currentConnection;

    // The name of the currently active doc element.
    private String _currentDocName;

    // The latest element seen by startElement.
    private String _currentElement;

    // List of top-level entities imported via import element.
    private List _imports;

    // The default namespace.
    private static String DEFAULT_NAMESPACE = "";

    // The current namespace.
    private String _namespace = DEFAULT_NAMESPACE;

    // The stack of name spaces.
    private Stack _namespaces = new Stack();

    // A list of parameters specified in property tags.
    private List _paramsToParse = new LinkedList();

    // The parser.
    private XmlParser _parser = new XmlParser();

    // True if we are skipping a rendition body.  Rendition bodies
    // are skipped if the rendition class was not found.
    private boolean _skipRendition = false;

    // Top-level entity.
    private NamedObj _toplevel = null;

    // The workspace for this model.
    private Workspace _workspace;

    // The external entities being parsed.
    private Stack _externalEntities = new Stack();
}
