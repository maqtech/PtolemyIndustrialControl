/*
A class that maintains context information for C code generation.

Copyright (c) 2001-2002 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/* A class that maintains context information for C code generation.

   @author Shuvra S. Bhattacharyya
   @version $Id$
*/


public class Context {

    /** Construct an empty context. */
    public Context() {
        _includeFileSet = new HashSet();
        _stringConstantMap = new HashMap();
        _stringConstantCount = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an include file to the set of include files in the context.
     *  File name delimeters (double quotes or angle brackets), and the .h
     *  suffix, must be included in the argument.
     *  @param fileName the name of the include file.
     */
    public void addIncludeFile(String fileName) {
        if (!_includeFileSet.contains(fileName)) {
            _includeFileSet.add(fileName);
        }
    }

    /** Reset the context to be empty. All information in the current context
     *  is discared.
     */
    public void clear() {
        _includeFileSet.clear();
        _stringConstantMap = new HashMap();
        _stringConstantCount = 0;
    }

    /** Enable importing of referenced include files.
     */
    public void clearDisableImports() {
        _disableImports = false;
    }

    /** Turn off (disable) single class mode translation
     *  (see {@link #getSingleClassMode()}).
     */
    public static void clearSingleClassMode() {
        _singleClassMode = false;
    }

    /** Return true if and only if importing of referenced include files
     *  is presently disabled.
     *  @return true if and only if importing is disabled.
     */
    public boolean getDisableImports() {
        return _disableImports;
    }

    /** Return the C identifer that corresponds to a string constant in this
     *  context.
     *  @param constant the string constant.
     *  @return the C identifier.
     */
    public String getIdentifier(String constant) {
        return (String)(_stringConstantMap.get(constant));
    }

    /** Return an Iterator over the set of include files in the context.
     *  Each element in the Iterator is a String representing an include
     *  file name.
     *  Each such file name includes appropriate file name delimeters
     *  (double quotes or angle brackets), and the .h suffix.
     *  @return the Iterator.
     */
    public Iterator getIncludeFiles() {
        return _includeFileSet.iterator();
    }

    /** Return true if and only if single class mode translation is
     *  presently enabled. In single class mode, inherited methods
     *  and fields are ignored, which can greatly reduce the number of
     *  references to other classes. Single class mode is used primarily
     *  for diagnostic purposes, and for rapid testing of new code.
     *  @return true if and only if single class mode translation is enabled.
     */
    public static boolean getSingleClassMode() {
        return _singleClassMode;
    }

    /** Return an Iterator over the set of string constants in the context.
     *  @return an Iterator over the set of string constants.
     */
    public Iterator getStringConstants() {
        return _stringConstantMap.keySet().iterator();
    }

    /** Add a new string constant to the pool of string constants if the
     *  string does not already exist in the pool. Return the C identifier
     *  for the string constant.
     *  @param the string constant.
     *  @return the C identifier.
     */
    public String newStringConstant(String value) {
        String name;
        if ((name = (String)(_stringConstantMap.get(value))) == null) {
            name = new String ("PCCG__string" + _stringConstantCount++);
            _stringConstantMap.put(value, name);
        }
        return name;
    }

    /** Disable importing of referenced include files.
     */
    public void setDisableImports() {
        _disableImports = true;
    }

    /** Turn on (enable) single class mode translation
     *  (see {@link #getSingleClassMode()}).
     */
    public static void setSingleClassMode() {
        _singleClassMode = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag that indicates whether or not importing of referenced include files
    // is disabled.
    private boolean _disableImports;

    // The set of system and runtime include files that need to be included
    // in the generated code. The include files stored in this set must
    // be delimited with double quotes or angle brackets, and must contain
    // the .h suffix.
    private HashSet _includeFileSet;

    // This variable indicates whether or not single class mode translation
    // is enabled.
    private static boolean _singleClassMode = false;

    // Count of the number of string constants that are currently in the
    // pool of string constants.
    private int _stringConstantCount;

    // The pool of string constants (literals) for the generated code.
    // Keys in this map are the string constant values
    // (the literals to be used),
    // and the values in the map are the C identifiers to use when referencing
    // the strings in the generated code. For each string constant, a static
    // string object is created in the generated code.
    private HashMap _stringConstantMap;

}
