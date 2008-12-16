/**
 A filter for backward compatibility with 7.2.devel or earlier models for width inference.

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.moml.filter;

import ptolemy.actor.IORelation;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// RelationWidthChanges

 
/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.
 This class will filter for relations that have a width equal to zero
 for Ptolemy versions 7.2.devel or older. The width value will be changed
 from 0 to -1, which is the new default for width inference. 

 @author Bert Rodiers
 @version $Id: RelationWidthChanges.java 49346 2008-05-01 18:42:42Z rodiers $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class RelationWidthChanges implements MoMLFilter {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Filter relations widths and change 0 to -1..
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
     */
    public String filterAttributeValue(NamedObj container, String element,
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
        
        if (!_changesNeeded) {
            return attributeValue;
        }
        
        if (_currentlyProcessingRelation) {
            if (_currentlyProcessingWidth) {
                if (attributeName.equals("value")) {
                    if (!_versionDetermined) {
                        NamedObj containerVar = container;
                        NamedObj containerParent = containerVar.getContainer();
                        while (containerParent != null) {
                            containerVar = containerParent;
                            containerParent = containerVar.getContainer();
                        }
                        VersionAttribute version = (VersionAttribute) containerVar.getAttribute("_createdBy");
                        try {
                            _changesNeeded = version!= null && version.isLessThan(new VersionAttribute("7.2.devel"));
                            // FIXME: what to do when version equals null? throw an exception, but then
                            // this model can't be opened anymore. Or not change anything?
                        } catch (IllegalActionException e) {
                            // We don't expect that this fails.                            
                            throw new IllegalStateException(e);
                        }                        
                        _versionDetermined = true;
                    }                    
                    _currentlyProcessingRelation = false;
                    _currentlyProcessingWidth = false;
                    
                    if (_changesNeeded && attributeValue.equals("0")) {
                            MoMLParser.setModified(true);
                            return Integer.toString(IORelation.WIDTH_TO_INFER);
                    }
                }
            }
            else {
                if (attributeValue.equals("width") && element.equals("property")) {
                    _currentlyProcessingWidth = true;
                }
            }
        } else  if (element.equals("relation") && attributeName.equals("class")) {
            _currentlyProcessingRelation = true;
        }        
        return attributeValue;
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  This method is called when an end element in MoML is
     *  encountered. A typical use of this method is to make
     *  some modification to the object (the container) that
     *  was constructed.
     *  <p>
     *  If an implementor makes changes to the specified container,
     *  then it should call MoMLParser.setModified(true) which indicates
     *  that the model was modified so that the user can optionally
     *  save the modified model.
     *
     *  @param container The object defined by the element that this
     *   is the end of.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *  only in the doc and configure elements
     *  @exception Exception If there is a problem modifying the
     *  specified container.
     */
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData) throws Exception {
        if (container instanceof IORelation) {
            _currentlyProcessingRelation = false;
            _currentlyProcessingWidth = false;
        }
    }

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update width of a relation with the following changes:\n");
        results.append("\t0 --> -1");
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
     
    /**A flag that specifies whether we are currently processing a relation*/
    private boolean _currentlyProcessingRelation = false;
    
    /**A flag that specifies whether we are currently processing the width of a relation*/
    private boolean _currentlyProcessingWidth = false;
    
    /**A flag that specifies whether changes might be needed for this model.
     * This is only the case for models older than version 7.2.devel
     */
    private boolean _changesNeeded = true;
    
    /**A flag that specifies whether the version of this model has already been
     * determined.
     */        
    private boolean _versionDetermined = false;       
}
