/* A class that writes Ptolemy models as moml

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.moml;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NotPersistent;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.actor.IOPort;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//////////////////////////////////////////////////////////////////////////
//// MoMLWriter
/**
This is a writer that is capable of writing an MoML description of a 
Ptolemy model to another writer.  This is implemented by using a variety of
interfaces, corresponding to various moml constructs.  These interfaces
are also used by the moml parser in order to do the parsing.
<ul>
<li> Settable: Objects that are settable have their expression written out
in their start tag, using the XML attribute "value".
<li> Configurable: Configurable objects have their configuration text
written out between &lt;configure&gt; and &lt;/configure&gt; tags.
<li> NotPersistent: Objects implementing the NotPersistent interface are
ignored completely.  Nothing inside them is written.
</ul>
Base classes (Vertex, Entity, IOPort, CompositeEntity, Documentation, and
MoMLAttribute) are written as appropriate (i.e. pathTo tags; input, output,
and multiport attributes, etc.)
<p>
MoML classes are handled in a rather elegant way.  The writer attempts to 
find an <i>representative instance</i> of the class of an object.  If the
object was cloned, the representative is the original object.  If the 
object was parsed from MoML, then the representative is either the 
MoML class that was specified or a new instance of the class created
using a (workspace) or (container, name) constructor.  By default, only
objects that do not appear in the class, or that have been modified from the
class are written to the MoML stream.
<p>
This writer also supports more verbose options for writing moml.
Calling the setVerbose method with a true argument disables certain
heuristics that are used to minimize the amount of moml written.  For example,
a configurable object with empty configuration text does not normally
contain the configure tags (since they add text, but do not add any 
information to the model).  Similarly, parameters and ports that come from
a base class are not normally written to MoML, since they simply duplicate
information that is already available.  However, when editing MoML by hand,
it is sometimes useful to see these tags to more easily see the syntax
and locations where such tags should be added.  
<p>
Calling the setForcePersistence
method with a true argument works similarly by overriding the NotPersistent
interface.  Even (supposedly) non-persistent objects are written to MoML
when this flag is true.   
<p>
Calling the setLiteral method with a true argument supresses some
translations that are normally done when writing moml.  The most important of
these translations is that instances of the MoMLAttribute class are 
replaced by the MoML that the attribute contains.  This mechanism is
used to store some forms of information about MoML subclasses that would 
not normally be available.  When literal MoML is being written, the 
MoML contained by a MoML attribute is ignored.
<p>
Combinations of these flags allow the entire
structure of an object to be written as MoML, which is often useful for 
debugging or writing tests.   

@author Steve Neuendorffer
@version $Id$
*/
public class MoMLWriter extends Writer {
    /** Create a new writer that wraps the given writer.
     */
    public MoMLWriter(Writer writer) {
        super(writer);
        _writer = writer;
    }

    /** Close this writer.   This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     */
    public void close() throws IOException {
        synchronized(lock) {
            if(_writer != null) {
                _writer.close();
                _writer = null;
            }
        }
    }

    /** Flush the writer.  This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     */
    public void flush() throws IOException {
        synchronized(lock) {
            if(_writer != null)
                _writer.flush();
        }
    }

    /** Return whether or not non-persistent objects are written out.
     *  @return True if non-persistent objects are written to the
     *  moml string.
     */
    public boolean isForcePersistence() {
        return _isForcePersistence;
    }

    /** Return whether or not certain MoML translations (such as replacing a 
     *  MoMLAttribute with its contained MoML) are carried out.
     *  @return True if only literal MoML is written
     */
    public boolean isLiteral() {
        return _isLiteral;
    }

    /** Return whether or not verbose writing is used.
     *  @return True if objects are written verbosly.
     */
    public boolean isVerbose() {
        return _isVerbose;
    }

    /** Set whether or not non-persistent objects should be written out.
     *  This defaults to false.
     *  @param flag True if non-persistent objects should be written to the
     *  moml string.
     */
    public void setForcePersistence(boolean flag) {
        _isForcePersistence = flag;
    }

    /** Set whether or not MoML translations (such as replacing a 
     *  MoMLAttribute with its contained MoML) are carried out.
     *  This defaults to false.
     *  @param flag True if only literal MoML is written.
     */
    public void setLiteral(boolean flag) {
        _isLiteral = flag;
    }

    /** Set whether or not verbose writing should be used.  
     *  This defaults to false.
     *  @param flag True if objects should be written verbosely.
     */
    public void setVerbose(boolean flag) {
        _isVerbose = flag;
    }

    /** Write a portion of an array of characters.
     *  This class simply defers to the writer
     *  that was passed in the constructor.  If a null writer was
     *  given to the constructor, then do nothing.
     *
     *  @param input Array of characters
     *  @param offset Offset from which to start writing characters
     *  @param length Number of characters to write
     *
     *  @exception IOException If an I/O error occurs
     */
    public void write(char input[], int offset, int length)
            throws IOException {
        synchronized(lock) {
            if(_writer != null)
                _writer.write(input, offset, length);
        }
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj model) throws IOException {
        if(model.getContainer() == null)
            writePreamble(model);
        write(model, 0, model.getName());
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj model, String name) throws IOException {
        if(model.getContainer() == null)
            writePreamble(model);
        write(model, 0, name);
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj object, int depth) throws IOException {
        write(object, depth, object.getName());
    }

    /** Write the given model to the writer that was passed in the
     *  constructor of this writer.  The model will be written as
     *  an XML string.
     */
    public void write(NamedObj object, int depth, String name)
            throws IOException {
        try {
            // This isn't technically necessary, but locking the workspace
            // once globally means that the rest of this will operate much
            // faster.
            object.workspace().getReadAccess();
            // System.out.println("Writing = " + object.getFullName());
        synchronized(lock) {
            // Alot of things aren't presistent and are just skipped.
            if(object instanceof NotPersistent && !_isForcePersistence) 
                return;
            // Documentation uses a special tag with no class.
            if(object instanceof Documentation && !_isLiteral) {
                Documentation container = (Documentation)object;
                write(_getIndentPrefix(depth));
                // If the name is the default name, then omit it
                if(name.equals("_doc")) {
                    write("<doc>");
                } else {
                    write("<doc name=\"");
                    write(name);
                    write("\">");
                }
                write(container.getValue());
                write("</doc>\n");
                return;
            }
            // MoMLAttribute writes arbitrary moml.
            // This is a hack, and hopefully we should be able to
            // get rid of it eventually.
            if(object instanceof MoMLAttribute && !_isLiteral) {
                MoMLAttribute container = (MoMLAttribute)object;
                container.writeMoMLDescription(this, depth);
                return;
            }
            NamedObj.MoMLInfo info = object.getMoMLInfo();
            // Write if nothing is being deferred to and there is no
            // class name.
            NamedObj deferredObject = _findDeferredInstance(object);

            // Write the first line.
            write(_getIndentPrefix(depth));
            write("<");
            write(info.elementName);
            write(" name=\"");
            write(name);

            if(info.elementName.equals("class")) {
                write("\" extends=\"");
                write(info.superclass);
            } else {
                write("\" class=\"");
                write(info.className);
            }
            if(info.source != null) {
                write("\" source=\"");
                write(info.source);
            }
            if(object instanceof Settable) {
                Settable settable = (Settable)object;
                String value = settable.getExpression();
                if(value != null && !value.equals("")) {
                    write("\" value=\"");
                    write(StringUtilities.escapeForXML(value));
                }
            }
            write("\">\n");

            if(object instanceof Configurable) {
                Configurable container = (Configurable)object;
                String source = container.getSource();
                String text = container.getText();
                boolean hasSource = source != null && !source.equals("");
                boolean hasText = text != null && !text.equals("");
                if(source == null) source = "";
                if(text == null) text = "";
                
                if(hasSource || _isVerbose) {
                    write(_getIndentPrefix(depth + 1));
                    write("<configure source=\"");
                    write(source);
                    write("\">");
                } else if(hasText) {
                    write(_getIndentPrefix(depth + 1));
                    write("<configure>");
                }
                if(hasText) {
                    write(text);
                }
                if(hasText || hasSource || _isVerbose) {
                    write("</configure>\n");
                }
                
                // Rather awkwardly we have to configure the
                // container, to handle the entity library.
                if(deferredObject != null &&
                        !(object instanceof EntityLibrary)) {
                    try {
                        // first clone it, since we are going to have to 
                        // run configure on this object.
                        try {
                            deferredObject = (NamedObj)deferredObject.clone();
                        } catch (CloneNotSupportedException ex) {
                        }
                        Configurable deferredContainer =
                            (Configurable)deferredObject;
                        deferredContainer.configure(null, source, text);
                    }
                    catch (Exception ex) {
                        System.out.println("Failed to configure because:");
                        ex.printStackTrace();
                    }
                }
            }
            
            if(!(object instanceof EntityLibrary)) {
                writeContents(object, deferredObject, depth + 1);
            }
            write(_getIndentPrefix(depth) + "</"
                    + info.elementName + ">\n");
        }
        } finally {
            //            System.out.println("DONEING = " + object.getFullName());
            object.workspace().doneReading();
        }
    }

    /** Write the contents of the given object as moml on this writer.
     *  Return true if anything was written, or false if nothing was written.
     */
    public boolean writeContents(NamedObj object, NamedObj deferredObject, int depth)
            throws IOException {
        synchronized(lock) {
            boolean wroteAnything = false;
            NamedObj.MoMLInfo info = object.getMoMLInfo();

            _writeAttributeContents(object, deferredObject, depth);

            if(object instanceof ptolemy.kernel.Entity) {
                Entity container = (Entity)object;
                Entity deferredContainer = (Entity)deferredObject;
                wroteAnything |= _writePortContents(container, 
                        deferredContainer, depth);
            } 
            if(object instanceof ptolemy.kernel.CompositeEntity) {
                CompositeEntity container = (CompositeEntity)object;
                CompositeEntity deferredContainer = 
                    (CompositeEntity) deferredObject;
                wroteAnything |= _writeEntityContents(container, 
                        deferredContainer, depth);
                wroteAnything |= _writeRelationContents(container, 
                        deferredContainer, depth);
                wroteAnything |= _writeLinkContents(container, 
                        deferredContainer, depth);
            }
            if(object instanceof ptolemy.actor.IOPort) {
                // Gee, it would be nice if these were regular attributes.
                IOPort container = (IOPort)object;
                if (container.isInput()) {
                    write(_getIndentPrefix(depth));
                    write("<property name=\"input\"/>\n");
                    wroteAnything = true;
                }
                if (container.isOutput()) {
                    write(_getIndentPrefix(depth));
                    write("<property name=\"output\"/>\n");
                    wroteAnything = true;
                }
                if (container.isMultiport()) {
                    write(_getIndentPrefix(depth));
                    write("<property name=\"multiport\"/>\n");
                    wroteAnything = true;
                } 
            }             
            if(object instanceof ptolemy.moml.Vertex) {
                Vertex container = (Vertex)object;
                Vertex linked = container.getLinkedVertex();
                if(linked != null) {
                    write(_getIndentPrefix(depth));
                    write("<pathTo=\"");
                    write(linked.getName());
                    write("\"/>\n");
                    wroteAnything = true;
                }

            }  

            return wroteAnything;
        }
    }

    /** Write the moml header information.  This is usually called
     *  exactly once prior to writing a model to an external file.
     */
    public void writePreamble(NamedObj object) throws IOException {
        synchronized(lock) {
            if(object.getMoMLInfo().elementName.equals("class"))
                _writer.write(_classPreamble);
            else
                _writer.write(_entityPreamble);
        }
    }
 
    /** Return an instance that represents the class that
     *  the given object defers to.
     */
    public NamedObj _findDeferredInstance(NamedObj object) {
        // System.out.println("findDeferred =" + object.getFullName());
        NamedObj deferredObject = null;
        NamedObj.MoMLInfo info = object.getMoMLInfo();
        if(info.deferTo != null) {
            deferredObject = info.deferTo;
            // System.out.println("object = " + object.getFullName());
            //System.out.println("deferredDirectly = " + deferredObject);
            //(new Exception()).printStackTrace(System.out);
        } else if(info.className != null) {
            try {
                // First try to find the local moml class that
                // we extend
                String deferredClass;
                if(info.elementName.equals("class")) {
                    deferredClass = info.superclass;
                } else {
                    deferredClass = info.className;
                }
                /*
                // System.out.println("deferredClass =" + deferredClass);
                  ParserAttribute attribute = (ParserAttribute)
                    object.toplevel().getAttribute("_parser");
                if(attribute != null) {
                    MoMLParser parser = attribute.getParser();
                    deferredObject =
                        parser._searchForClass(deferredClass,
                                info.source);
                                }        
                //        System.out.println("deferredClass = " + deferredClass);
                deferredObject = 
                    _searchForMoMLClass(object, deferredClass, info.source);
                //System.out.println("deferredObject =" + deferredObject);
               if(deferredObject != null) {
                    System.out.println("object =" + object);
                }          
*/
               if(deferredObject == null) {
                    // No moml class..  must have been a java class.
                    // FIXME: This sucks.  We should integrate with 
                    // the classloader mechanism.
                    String objectType;
                    if(object instanceof Attribute) {
                        objectType = "property";
                    } else if(object instanceof Port) {
                        objectType = "port";
                    } else {
                        objectType = "entity";
                    }
                    Class theClass = Class.forName(deferredClass, 
                            true, getClass().getClassLoader());
                    deferredObject = (NamedObj)instanceMap.get(theClass);
                    
                    if(deferredObject == null) {
                        //     System.out.println("reflecting " + theClass);
                        // OK..  try reflecting using a workspace constructor
                        _reflectionArguments[0] = _reflectionWorkspace;
                        Constructor[] constructors = 
                            theClass.getConstructors();
                        for (int i = 0; i < constructors.length; i++) {
                            Constructor constructor = constructors[i];
                            Class[] parameterTypes = 
                                constructor.getParameterTypes();
                            if (parameterTypes.length !=
                                    _reflectionArguments.length)
                                continue;
                            boolean match = true;
                            for (int j = 0; j < parameterTypes.length; j++) {
                                if (!(parameterTypes[j].isInstance(
                                        _reflectionArguments[j]))) {
                                    match = false;
                                    break;
                                }
                            }
                            if (match) {
                                deferredObject = (NamedObj)
                                    constructor.newInstance(
                                            _reflectionArguments);
                                break;
                            }
                        }
                    }
                    
                    //String source = "<" + objectType + " name=\""
                    //    + object.getName() + "\" class=\""
                    //    + deferredClass + "\"/>";
                    //deferredObject = parser.parse(source);
                    //System.out.println("class with workspace = " + 
                    //        deferredClass);
                    if(deferredObject == null) {
                        // Damn, no workspace constructor.  Let's
                        // try a container, name constructor.
                        // It really would be nice if all of 
                        // our actors had workspace constructors,
                        // but version 1.0 only specified the
                        // (container,name) constructor, and 
                        // now we're stuck with it. 
                        String source = "<entity name=\"parsedClone\""
                            + "class=\"ptolemy.kernel.CompositeEntity\">\n"
                            + "<" + objectType + " name=\""
                            + object.getName() + "\" class=\""
                            + deferredClass + "\"/>\n" 
                            + "</entity>";
                        _reflectionParser.reset();
                        CompositeEntity toplevel;
                        try {
                            toplevel = (CompositeEntity)
                                _reflectionParser.parse(source);
                        } catch (Exception ex) {
                            throw new InternalErrorException("Attempt "
                                    + "to create an instance of "
                                    + deferredClass + " failed because "
                                    + "it does not have a Workspace "
                                    + "constructor.  Original error:\n"
                                    + ex.getMessage());
                        }
                        if(object instanceof Attribute) {
                            deferredObject = 
                                toplevel.getAttribute(object.getName());
                        } else if(object instanceof Port) {
                            deferredObject = 
                                toplevel.getPort(object.getName());
                        } else {
                            deferredObject = 
                                toplevel.getEntity(object.getName()); 
                        }
                        //           System.out.println("class without workspace = " + 
                        //        deferredClass);
                    }
                    // Save the reference in the map, so we don't have to
                    // do that crap again.
                    if(deferredObject != null) {
                        instanceMap.put(theClass, deferredObject);
                    }
                        
                }
            }
            catch (Exception ex) {
                System.err.println("Exception occured during parsing:\n");
                ex.printStackTrace();
                deferredObject = null;
            }
        }
        return deferredObject;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        if(level < 0)
            return _getIndentPrefix(0);
        synchronized(_prefixes) {
            if(_prefixes.length <= level) {
                // Expand the cache
                String[] temp = new String[2 * level];
                System.arraycopy(_prefixes, 0, temp, 0, _prefixes.length);
                _prefixes = temp;
            }
            if(_prefixes[level] == null) {
                // update the cache
                _prefixes[level] = _getIndentPrefix(level - 1) + "    ";
            }
            // return the value from the cache
            return _prefixes[level];
        }
    }
    
    private NamedObj _searchForMoMLClass(NamedObj object, 
            String deferredClass, String source) throws Exception {
        ParserAttribute attribute = null;
        NamedObj context = object;
        while(context != null && attribute == null) {
            Attribute tempAttribute = 
                context.getAttribute("_parser");
            if(tempAttribute instanceof ParserAttribute) {
                attribute = (ParserAttribute)tempAttribute;
            }
            context = (NamedObj)context.getContainer();
        }
        // System.out.println("parserAttribute =" + attribute);

        if(attribute != null) {
            MoMLParser parser = attribute.getParser();
            return parser._searchForClass(deferredClass,
                        source);
        }
        return null;
    }        
    

    private boolean _writeAttributeContents(NamedObj object, NamedObj deferredObject, int depth) throws IOException {
        boolean wroteAnything = false;
        // Describe parameterization.
        Iterator attributes = object.attributeList().iterator();
        while(attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            
            if(deferredObject == null) {
                // If we have nothing to refer to, then just write the
                // attribute.
                write(attribute, depth);
                wroteAnything = true;
            } else {
                // Otherwise, check inside the referred object to
                // see if we need to write the attribute.
                Attribute deferAttribute =
                    deferredObject.getAttribute(attribute.getName());
                wroteAnything |=
                    _writeForDeferred(attribute, deferAttribute, depth);
            }
        }
        return wroteAnything;
    }

    private boolean _writeEntityContents(CompositeEntity container, 
            CompositeEntity deferredContainer, int depth) 
            throws IOException {
        boolean wroteAnything = false;
        Iterator entities = container.entityList().iterator();
        while (entities.hasNext()) {
            ComponentEntity entity = 
                (ComponentEntity)entities.next();
            if(deferredContainer == null) {
                // If we have nothing to refer to, 
                // then just write the
                // entity.
                write(entity, depth);
                wroteAnything = true;
            } else {
                // Otherwise, check inside the referred object to
                // see if we need to write the entity.
                String entityName = entity.getName(container);
                //    System.out.println("entityName = " + entityName);
                Entity deferredEntity =
                    deferredContainer.getEntity(entityName);
                // System.out.println("deferEntity= " + 
                //        deferredEntity);
                wroteAnything |=
                    _writeForDeferred(entity, deferredEntity, 
                            depth);
            }
        }
        return wroteAnything;
    }

    private boolean _writeLinkContents(CompositeEntity container, 
            CompositeEntity deferredContainer, int depth) 
            throws IOException {
        boolean wroteAnything = false;
        // Next write the links.
        // To get the ordering right,
        // we read the links from the ports, not from the relations.
        // First, produce the inside links on contained ports.
        Iterator ports = container.portList().iterator();
        while (ports.hasNext()) {
            ComponentPort port = (ComponentPort)ports.next();
            Iterator relations = port.insideRelationList().iterator();
            Iterator deferredRelations = null;
            if(deferredContainer != null) {
                ComponentPort deferredPort = (ComponentPort)
                    deferredContainer.getPort(port.getName());
                if(deferredPort != null) 
                    deferredRelations = 
                        deferredPort.insideRelationList().iterator();
            }
            wroteAnything |= _writeLinks(container, port,
                    relations, deferredRelations, depth);
        }

        // Next, produce the links on ports contained by contained entities.
        Iterator entities = container.entityList().iterator();
        while (entities.hasNext()) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            ComponentEntity deferredEntity = null;
            if(deferredContainer != null) {
                deferredEntity = (ComponentEntity)
                    deferredContainer.getEntity(entity.getName());
            }
            ports = entity.portList().iterator();
            while (ports.hasNext()) {
                ComponentPort port = (ComponentPort)ports.next();
                Iterator relations = port.linkedRelationList().iterator();
                Iterator deferredRelations = null;
               
                if(deferredEntity != null) {
                    ComponentPort deferredPort = (ComponentPort)
                        deferredEntity.getPort(port.getName());
                    if(deferredPort != null) 
                        deferredRelations = 
                            deferredPort.linkedRelationList().iterator();
                }
                wroteAnything |= _writeLinks(container, port,
                        relations, deferredRelations, depth);
            }
        }
        return wroteAnything;
    }
        
    private boolean _writeLinks(CompositeEntity container, ComponentPort port, 
            Iterator relations, Iterator deferredRelations, int depth) 
            throws IOException {
        boolean wroteAnything = false;
        // The following variables are used to determine whether to
        // specify the index of the link explicitly, or to leave
        // it implicit.
        int index = -1;
        boolean useIndex = false;
        while (relations.hasNext()) {
            index++;
            ComponentRelation relation
                = (ComponentRelation)relations.next();
            
            if(deferredRelations != null && deferredRelations.hasNext()) {
                 ComponentRelation deferredRelation
                     = (ComponentRelation)deferredRelations.next();
                 if(deferredRelation.getName().equals(relation.getName())) {
                     useIndex = true;
                     continue;
                 }
            }
            if (relation == null) {
                // Gap in the links.  The next link has to use an
                // explicit index.
                useIndex = true;
                continue;
            }
            // Apply filter.
            if (true) {
                //filter == null
                //    || (filter.contains(relation)
                //    && (filter.contains(port)
                //    ||  filter.contains(port.getContainer())))) {
                
                write(_getIndentPrefix(depth));
                write("<link port=\"");
                write(port.getName(container));
                if (useIndex) {
                    useIndex = false;
                    write("\" insertAt=\"" + index);
                }
                write("\" relation=\"");
                write(relation.getName(container));
                write("\"/>\n");
                wroteAnything = true;
            }
        }
        return wroteAnything;
    }


    private boolean _writePortContents(Entity container,
            Entity deferredContainer, int depth) 
            throws IOException {
        boolean wroteAnything = false;
        Iterator ports = container.portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            if(deferredContainer == null) {
                // If we have nothing to refer to, 
                // then just write the
                // entity.
                write(port, depth);
                wroteAnything = true;
            } else {
                // Otherwise, check inside the referred object to
                // see if we need to write the attribute.
                String portName = port.getName(container);
                //   System.out.println("portName = " + portName);
                Port deferPort = 
                    deferredContainer.getPort(port.getName(container));
                // System.out.println("deferPort = " + 
                //        deferPort);
                wroteAnything |=
                    _writeForDeferred(port, deferPort, depth);
            }
        }
        return wroteAnything;
    }               

    private boolean _writeRelationContents(CompositeEntity container,
            CompositeEntity deferredContainer, int depth) 
            throws IOException {
        boolean wroteAnything = false;
        Iterator relations = container.relationList().iterator();
        while (relations.hasNext()) {
            ComponentRelation relation
                        = (ComponentRelation)relations.next();
            if(deferredContainer == null) {
                // If we have nothing to refer to, 
                // then just write the
                // entity.
                write(relation, depth);
                wroteAnything = true;
            } else {
                // Otherwise, check inside the referred object to
                // see if we need to write the relation.
                String relationName = relation.getName(container);
                ComponentRelation deferredRelation =
                    deferredContainer.getRelation(relationName);
                wroteAnything |=
                    _writeForDeferred(relation, deferredRelation, 
                            depth);
            }
        }
        return wroteAnything;
    }
        
    /** Write a description of the given object, given that it
     *  may represent the given deferredObject.  If the deferredObject is
     *  null, then write a description of the given object.  If the deferred
     *  object is different from the given object, then write the
     *  given object.  Otherwise, do nothing.
     *  @param object The object to be written.
     *  @param deferredObject The object that might represent the object.
     *  @param depth The level of indenting represented by the spaces.
     *  @return true If anything was written.
     */
    protected boolean _writeForDeferred(NamedObj object,
            NamedObj deferredObject, int depth) throws IOException {
        // If there is no deferred object, then write the object.
        if(deferredObject == null || _isVerbose) {
            write(object, depth);
            return true;
        } else {
            StringWriter deferStringWriter = new StringWriter();
            MoMLWriter deferWriter = new MoMLWriter(deferStringWriter);
            deferWriter.setVerbose(true);
            deferWriter.write(deferredObject, depth);
            String deferredString = deferStringWriter.toString();

            StringWriter stringWriter = new StringWriter();
            MoMLWriter writer = new MoMLWriter(stringWriter);
            writer.setVerbose(true);
            writer.write(object, depth);
            String string = stringWriter.toString();

            // If the object is different, then write it.
            if(!string.equals(deferredString)) {
                //  System.out.println("string = " + string);
                //  System.out.println("deferredString = " + deferredString);
                write(object, depth);

                return true;
            }
            return false;
        }
    }
    
    // True to force NotPersistent objects to be written to MoML.
    private boolean _isForcePersistence = false;
    
    // True to force literal MoML to be written.
    private boolean _isLiteral = false;
    
    // An array of indenting spaces, indexed by the number indents.
    // Tese are cached to speed up the indenting code.
    private static String[] _prefixes = {"", "    "};

    // True to force contained objects to be written 
    // out even if they duplicate an object in a superclass.
    private boolean _isVerbose = false;

     // The writer
    private Writer _writer;

    // The preamble string for entities.
    private static final String _entityPreamble = 
    new String("<?xml version=\"1.0\""
            + " standalone=\"no\"?>\n"
            + "<!DOCTYPE entity PUBLIC "
            + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
            + "    \"http://ptolemy.eecs.berkeley.edu"
            + "/xml/dtd/MoML_1.dtd\">\n");

    // The preamble string for classes.
    private static final String _classPreamble = 

    new String("<?xml version=\"1.0\""
            + " standalone=\"no\"?>\n"
            + "<!DOCTYPE class PUBLIC "
            + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
            + "    \"http://ptolemy.eecs.berkeley.edu"
            + "/xml/dtd/MoML_1.dtd\">\n");

    // Making this static is a little nasty..
    private static Map instanceMap = 
    Collections.synchronizedMap(new HashMap());
    
    private Object[] _reflectionArguments = new Object[1];
    private Workspace _reflectionWorkspace = new Workspace();
    private MoMLParser _reflectionParser =
    new MoMLParser(_reflectionWorkspace);
}


