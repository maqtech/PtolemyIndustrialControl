/* Add a VisibleParameterEditorFactor named _editorFactor to certain Parameters

 Copyright (c) 2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.moml.filter;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;


//////////////////////////////////////////////////////////////////////////
//// AddEditorFactory
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that models from
earlier releases will run in the current release.

<p>This class will filter for actors that have had port name changes, and
for classes with property where the class name has changed

@author Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class AddEditorFactory implements MoMLFilter {

    /**  If the attributeName is "class" and attributeValue names a
     *        class that has had its port names changed between releases,
     *  then substitute in the new port names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.

        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            // Save the name of the for later use if we see a "class"
            _lastNameSeen = attributeValue;
            if (_currentlyProcessingActorThatMayNeedAnEditorFactory) {
                if (attributeValue.equals("_editorFactory")) {
                    // We are processing a Parameter that already has a
                    // _editorFactory
                    _currentlyProcessingActorThatMayNeedAnEditorFactory =
                        false;
                    _currentAttributeHasLocation = false;
                } else if (attributeValue.equals("_location")) {
                    // We only add _editorFactory to parameters that
                    // have locations
                    _currentAttributeHasLocation = true;
                }
            }
        }


        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        // If you place the above command in a file five times, you
        // can get averages with:
        // sh c:/tmp/timeit | awk '{sum += $4; print sum, sum/NR, $0}'

        if (attributeName.equals("class")) {
            if (attributeValue
                .equals("ptolemy.data.expr.Parameter")){
                _currentlyProcessingActorThatMayNeedAnEditorFactory = true;
                if (container != null ) {
                    _currentActorFullName = container.getFullName()
                        + "." + _lastNameSeen;
                } else {
                    _currentActorFullName = "." + _lastNameSeen;
                }
            } else if ( _currentlyProcessingActorThatMayNeedAnEditorFactory
                        && container != null
                        && !container.getFullName()
                        .equals(_currentActorFullName)
                        && !container.getFullName()
                        .startsWith(_currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes, so
                _currentlyProcessingActorThatMayNeedAnEditorFactory =
                    false;
                _currentAttributeHasLocation = false;
            }
        }
        return attributeValue;
    }

    /** Given the elementName, perform any filter operations
     *  that are appropriate for the MOMLParser.endElement() method.
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param elementName The element type name.
     *  @return the filtered element name, or null if
     *  MoMLParser.endElement() should immediately return.
     */
    public String filterEndElement(NamedObj container, String elementName)
            throws Exception {
        if (!_currentlyProcessingActorThatMayNeedAnEditorFactory) {
            return elementName;
        } else         if ( _currentAttributeHasLocation
                     && elementName.equals("property")
                     && container != null
                     && container.getFullName()
                     .equals(_currentActorFullName)) {
            _currentlyProcessingActorThatMayNeedAnEditorFactory = false;
            _currentAttributeHasLocation = false;

            // In theory, we could do something like the lines below
            // but that would mean that the moml package would depend
            // on the vergil.toolbox package.
            //
            // VisibleParameterEditorFactor _editorFactory =
            //        new VisibleParameterEditorFactory(container, "_editorFactory");

            if (_parser == null) {
                _parser = new MoMLParser();
            }
            // setContext calls parser.reset()
            _parser.setContext(container);
            String moml = "<property name=\"_editorFactory\""
                + "class=\"ptolemy.vergil.toolbox."
                + "VisibleParameterEditorFactory\">"
                + "</property>";
            try {
                // Do not call parse(moml) here, since that method
                // will fail if we are in an applet because it tries
                // to read user.dir
                NamedObj icon = _parser.parse(null, moml);
                MoMLParser.setModified(true);
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex, "Failed to parse\n"
                                                 + moml);
            }
        }
        return elementName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if the current attribute has a _location attribute.
    // This variable is used to determine whether we need to add  a
    // _editorFactory.
    private static boolean _currentAttributeHasLocation = false;

    // Set to true if we are currently processing an actor that may
    // need _editorFactory added, set to false when we are done.
    private boolean
        _currentlyProcessingActorThatMayNeedAnEditorFactory = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The parser we use to parse the MoML when we add an _icon.
    private static MoMLParser _parser;
}
