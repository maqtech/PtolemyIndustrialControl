/* Test class for moml.IconLoader

 Copyright (c) 2007-2014 The Regents of the University of California.
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


 */
package org.terraswarm.accessor;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.IconLoader;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// AccessorIconLoader

/**
 An icon loader for accessors.
 This icon loader looks for a file whose name matches the name
 of the accessor with the suffix "Icon.xml" in the same location
 that the accessor is stored. It looks first on the local disk
 in the location that mirrors the accessor repository, and then
 looks online if that fails.
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AccessorIconLoader implements IconLoader {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Load an icon for a class in a particular context.
     *  This method defers icon loading until the context object has
     *  been fully constructed.
     *  @param className The name of the class for which the icon is
     *  to be loaded.
     *  @param context The context in which the icon is loaded.
     *  @return False. This method never immediately loads an icon.
     *  @exception Exception If there is a problem adding
     *  the icon.
     */
    @Override
    public boolean loadIconForClass(final String className, final NamedObj context)
            throws Exception {
        // Do this as a change request because the accessorSource attribute
        // of the accessor will not have been set yet when this is called.
        ChangeRequest request = new ChangeRequest(this, "AccessorIconLoader") {
            
            @Override
            protected void _execute() throws Exception {
                // Note that this duplicates code in MoMLParser._loadFileInContext(),
                // but there seems to be no way to prevent that without breaking
                // Kepler.
                
                // First, if the context is an instance JSAccessor, proceed.
                if (context instanceof JSAccessor) {
                    JSAccessor accessor = (JSAccessor)context;
                    String source = accessor.accessorSource.getExpression().trim();
                    int tail = source.lastIndexOf(".js");
                    if (tail < 0) {
                        tail = source.lastIndexOf(".xml");
                    }
                    if (tail > 0) {
                        String iconURLSpec = source.substring(0, tail) + "Icon.xml";
                        // Do not update the repo (second argument is false).
                        try {
                            URL iconURL = JSAccessor._sourceToURL(iconURLSpec, false);
                            InputStream input = iconURL.openStream();
                            MoMLParser newParser = new MoMLParser();
                            // Mark the parser to keep track of objects created.
                            newParser.clearTopObjectsList();
                            newParser.setContext(context);
                            newParser.parse(null, iconURL.toExternalForm(), input);
                            // Have to mark the contents derived objects, so that
                            // the icon is not exported with the MoML export.
                            List<NamedObj> icons = newParser.topObjectsCreated();
                            if (icons != null) {
                                Iterator objects = icons.iterator();

                                while (objects.hasNext()) {
                                    NamedObj newObject = (NamedObj) objects.next();
                                    newObject.setDerivedLevel(1);
                                    _markContentsDerived(newObject, 1);
                                }
                            }
                            return;
                        } catch (Throwable ex) {
                            // Ignore and fall back to default behavior.
                        }
                    }
                }
                String fileName = className.replace('.', '/') + "Icon.xml";
                URL xmlFile = getClass().getClassLoader().getResource(fileName);
                if (xmlFile != null) {
                    InputStream input = xmlFile.openStream();
                    MoMLParser newParser = new MoMLParser();
                    // Mark the parser to keep track of objects created.
                    newParser.clearTopObjectsList();
                    newParser.setContext(context);
                    newParser.parse(null, fileName, input);
                    
                    // Have to mark the contents derived objects, so that
                    // the icon is not exported with the MoML export.
                    List<NamedObj> icons = newParser.topObjectsCreated();
                    if (icons != null) {
                        Iterator objects = icons.iterator();

                        while (objects.hasNext()) {
                            NamedObj newObject = (NamedObj) objects.next();
                            newObject.setDerivedLevel(1);
                            _markContentsDerived(newObject, 1);
                        }
                    }
                }
            }
        };
        context.requestChange(request);
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    // NOTE: The following method is largely duplicated from MoMLParser,
    // but exposing that method is not a good idea.

    /** Mark the contents as being derived objects at a depth
     *  one greater than the depth argument, and then recursively
     *  mark their contents derived.
     *  This makes them not export MoML, and prohibits name and
     *  container changes. Normally, the argument is an Entity,
     *  but this method will accept any NamedObj.
     *  This method also adds all (deeply) contained instances
     *  of Settable to the _paramsToParse list, which ensures
     *  that they will be validated.
     *  @param object The instance that is defined by a class.
     *  @param depth The depth (normally 0).
     */
    private void _markContentsDerived(NamedObj object, int depth) {
        // NOTE: It is necessary to mark objects deeply contained
        // so that we can disable deletion and name changes.
        // While we are at it, we add any
        // deeply contained Settables to the _paramsToParse list.
        Iterator objects = object.lazyContainedObjectsIterator();

        while (objects.hasNext()) {
            NamedObj containedObject = (NamedObj) objects.next();
            containedObject.setDerivedLevel(depth + 1);
            _markContentsDerived(containedObject, depth + 1);
        }
    }
}
