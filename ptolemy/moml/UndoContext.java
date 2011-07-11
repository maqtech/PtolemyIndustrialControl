/* Holds information about the current undo context

 Copyright (c) 2000-2010 The Regents of the University of California.
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
package ptolemy.moml;

import java.util.Stack;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// UndoContext

/**
 Holds information about the current undo context. It is used while parsing
 an incremental model change to hold the following information:
 <ul>
 <li> Whether or not undo MoML should be generated for the current context.
 <li> Whether or not undo MoML should be generated for any child nodes.
 <li> The undo MoML to start the undo entry for this context.
 <li> The undo MoML for any child elements, pushed onto a stack.
 <li> The closing undo MoML for this context, if any. This is appended
 after the undo MoML for the child nodes.
 </ul>
 <p>
 At the end of an element, if the context is undoable, then the
 undo MoML is generated by taking the main undo MoML StringBuffer,
 appending the undo MoML for children in the reverse order to the
 order they were parsed, and finally appending any closing undo MoML
 that is present.

 @author     Neil Smyth
 @version    $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class UndoContext {
    ///////////////////////////////////////////////////////////////////
    ////                         Constructors                      ////

    /**
     *  Create a new UndoContext which may or may not need undo MoML
     *  generated.
     *
     *  @param undoableContext Whether or not undo MoML is required for this
     *  context
     */
    public UndoContext(boolean undoableContext) {
        _undoable = undoableContext;
        _undoChildEntries = new Stack();
        _undoMoML = new StringBuffer();
        _closingUndoMoML = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append some MoML to be appended after the undo MoML for child nodes.
     *  Note that these will appear in the reverse order in which this method
     *  is called, which makes sense since generally these are nested calls.
     *  @param undoMoML The MoMl to be appended after any child nodes
     */
    public void appendClosingUndoMoML(String undoMoML) {
        _closingUndoMoML.insert(0, undoMoML);
        return;
    }

    /**
     *  Append some MoML to the current buffer.
     *
     *  @param undoMoML The undo MoML to append to the current buffer.
     */
    public void appendUndoMoML(String undoMoML) {
        _undoMoML.append(undoMoML);
        return;
    }

    /**
     *  Used to handle the "rename" element. Replace the value of the "name"
     *  attribute the given value. This is a bit of a hack as ideally a child
     *  context should not modify the parent context, but with rename that is
     *  exactly what is required.
     *
     *  @param newName the value to give to the value of the name attribute.
     *  @exception IllegalActionException if there is currently no undo MoML at
     *  this level, or if there is no name attribute present.
     */
    public void applyRename(String newName) throws IllegalActionException {
        if (_undoMoML.length() == 0) {
            // this should not happen
            throw new IllegalActionException("Failed to create undo entry:\n"
                    + "Cannot rename an element whose parent "
                    + "undo context does not have any undo MoML. Requested "
                    + "new name: " + newName);
        }

        String undo = _undoMoML.toString();
        String marker = "name=\"";
        int startIndex = undo.indexOf("name=\"");

        if (startIndex == -1) {
            // this should not happen
            throw new IllegalActionException("Failed to create undo entry:\n"
                    + "Cannot rename an element whose parent "
                    + "undo context does not have a name attribute in its "
                    + "undo MoML. Requested new name: " + newName);
        }

        // Move the startIndex to after the marker
        startIndex += marker.length();

        // Now get the end index
        int endIndex = undo.indexOf("\"", startIndex);

        if (endIndex == -1) {
            // Also should not happen
            // this should not happen
            throw new IllegalActionException("Failed to create undo entry:\n"
                    + "Cannot rename an element whose parent "
                    + "undo context does not have a valid name attribute "
                    + "in its undo MoML. Requested new name: " + newName);
        }

        // Finally update the string buffer
        _undoMoML.replace(startIndex, endIndex, newName);
    }

    /**
     *  Generate the undo entry by processing children entries and the
     *  closing undo MoML. First appends the contents of the children
     *  undo entry stack to this elements undo MoML, followed by any
     *  closing MoML. Note that child elements have their undo MoML
     *  appended in reverse order to that in which they were parsed.
     *
     *  @return the generated undo entry.
     */
    public String generateUndoEntry() {
        // First append the undo MoML for the children
        while (!_undoChildEntries.isEmpty()) {
            _undoMoML.append((String) _undoChildEntries.pop());
        }

        // Next append any closing MoML
        _undoMoML.append(_closingUndoMoML.toString());

        // Return the result
        return _undoMoML.toString();
    }

    /**
     *  Get the undo MoML for this element as it currently stands. Note that
     *  this method does not append the MoML for children or any closing MoML
     *  that has been set.
     *
     *  @return The UndoMoML value.
     */
    public String getUndoMoML() {
        return _undoMoML.toString();
    }

    /**
     *  Return whether or not this context has any undo MoML to be processed.
     *
     *  @return true if this context has any undo MoML to be processed.
     */
    public boolean hasUndoMoML() {
        return _undoMoML.length() > 0;
    }

    /**
     *  Return whether or not child nodes need to generate MoML.
     *
     *  @return true if this context has undoable children.
     */
    public boolean hasUndoableChildren() {
        return _childrenUndoable;
    }

    /**
     *  Tells if the current context is undoable or not.
     *
     *  @return true if this context is undoable.
     */
    public boolean isUndoable() {
        return _undoable;
    }

    /** Return the closing element corresponding to the starting element
     *  returned by moveContextStart(), or an empty string if none is
     *  needed.
     *  <p>
     *  For example, if the containee is not already immediately
     *  contained, and the container is an entity, the &lt;/entity&gt;
     *  is appended to the model.
     *  @param context The current context.
     *  @param containee The containee whose immediate context we want.
     *  @return The MoML that closes the MoML returned by moveContextStart(),
     *   or an empty string if none is needed.
     *  @see #moveContextStart(NamedObj, NamedObj)
     */
    public static String moveContextEnd(NamedObj context, NamedObj containee) {
        if (moveContextStart(context, containee).equals("")) {
            return "";
        }

        // If we get to here, then containee and its
        // container cannot be null.
        NamedObj container = containee.getContainer();
        return "</" + container.getElementName() + ">\n";
    }

    /** Return the MoML start element to put us in the
     *  context of the immediate container of the containee,
     *  assuming the current context is as given by the
     *  <i>context</i> argument.  Return an empty string if the
     *  specified context is the immediate container of the
     *  specified containee.
     *  <p>
     *  For example, if the context has full name ".top" and the
     *  containee has full name ".top.a.b.c.d", then MoML to move
     *  down the model such as the following is returned:
     *  &lt;entity name="a.b.c" &gt;
     *  @param context The current context.
     *  @param containee The containee whose immediate context we want.
     *  @return The MoML to put us in the right context from the current
     *   context, or an empty string if we are already in that context
     *   or if either argument is null.
     *  @see #moveContextEnd(NamedObj, NamedObj)
     */
    public static String moveContextStart(NamedObj context, NamedObj containee) {
        if ((context == null) || (containee == null)) {
            return "";
        }

        NamedObj container = containee.getContainer();

        if ((container == null) || (container == context)) {
            return "";
        }

        String entityContext = container.getName(context);
        String elemName = container.getElementName();
        return "<" + elemName + " name=\"" + entityContext + "\" >\n";
    }

    /**
     *  Push the passed in MoML onto the stack of element undo entries. Note
     *  that undo entries are pushed onto a stack so that at the end of the
     *  element they can be aggregated to form the full undo MoML - in the
     *  reverse order to that in which the elements originally appeared.
     *
     * @param  entry  Description of Parameter
     */
    public void pushUndoEntry(String entry) {
        // FIXME: Should we set _childrenUndoable = true here?
        // If we push an undo child entry, then is it always undoable?
        _undoChildEntries.push(entry);
    }

    /**
     *  Set whether or not the child contexts are undoable.
     *
     * @param  isUndoable  the new state
     */
    public void setChildrenUndoable(boolean isUndoable) {
        _childrenUndoable = isUndoable;
    }

    /**
     *  Set whether or not the current context is undoable.
     *
     * @param  isUndoable  the new state
     */
    public void setUndoable(boolean isUndoable) {
        _undoable = isUndoable;
    }

    /** Return a string representation of this object. */
    public String toString() {
        return "UndoContext: " + (isUndoable() ? "are" : "are not")
                + " undoable and "
                + (hasUndoableChildren() ? "has" : "does not have")
                + " undoable children\n" + "undoMoML: " + getUndoMoML() + "\n"
                + "closingUndoMoML: " + _closingUndoMoML.toString() + "\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Flag indicating if child elements should be undoable
    private boolean _childrenUndoable;

    // Whether or not this level is undoable
    private boolean _undoable;

    // Holds the currently generated undoable MoML for this level. Note the
    // MoML is generated in reverse element order for any given context.
    private StringBuffer _undoMoML;

    // Holds the undo MoML that is to be appended after the MoML for
    // any child nodes
    private StringBuffer _closingUndoMoML;

    // Holds the stack of MoML entries, one for each element for
    // which undo MoML was generated
    private Stack _undoChildEntries;
}
