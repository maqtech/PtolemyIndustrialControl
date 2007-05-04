/* Utilities for MoML files

Copyright (c) 2007 The Regents of the University of California.
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


import java.io.StringWriter;
import java.util.Iterator;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// MoMLUtilties

/**
   Utilities for operating on MoML files.

   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class MoMLUtilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check for problems in the moml to be copied.  If there are
     *  missing variables references, search for the variables and
     *  return MoML definitions for any found variables.
     *  @param momlToBeChecked The MoML string to be checked.
     *  @param container The container in which the string is to be checked.
     *  @return MoML to be inserted before the momlToBeChecked
     *  @exception IllegalActionException If there is a problem parsing
     *  the string, or validating a variable.
     */
    public static String checkCopy(String momlToBeChecked,
            NamedObj container) throws IllegalActionException {
        
        _variableBuffer = new StringWriter();
        Workspace workspace = new Workspace("copyWorkspace");
        MoMLParser parser = new MoMLParser(workspace);
        TypedCompositeActor parsedContainer = null;

        // Attempt to parse the moml.  If we fail, check the exception
        // for a missing variable.  If a missing variable is found
        // add it to the moml and reparse.
        boolean doParse = true;
        while (doParse) {
            ErrorHandler handler = MoMLParser.getErrorHandler();
            MoMLParser.setErrorHandler(null);
            try {
                
                // Parse the momlToBeChecked.
                parsedContainer = (TypedCompositeActor) parser.parse(
                        "<entity name=\"auto\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + _variableBuffer.toString() 
                        + momlToBeChecked
                        + "</entity>");
                doParse = false;
            } catch (IllegalActionException ex) {
                try {
                    doParse = _findUndefinedConstantsOrIdentifiers(ex,
                            container, parsedContainer);

                } catch (Exception ex2) {
                    return _variableBuffer.toString();
                }
            } catch (Exception ex3) {
                throw new IllegalActionException(container, ex3,
                        "Failed to parse contents of copy buffer.");
            } finally {
            MoMLParser.setErrorHandler(handler);                
            }
        }

        // Iterate through all the entities, find the attributes
        // that are variables, validate the variables and look for
        // errors.

        // FIXME: what about classes?
        Iterator entities = parsedContainer.allAtomicEntityList().iterator();
        while (entities.hasNext()) {
            Entity entity = (Entity) entities.next();
            Iterator attributes = entity.attributeList().iterator();
            while (attributes.hasNext()) {
                Attribute attribute = (Attribute) attributes.next();
                if (attribute instanceof Variable) {
                    Variable variable = (Variable) attribute;

                    boolean doGetToken = true;
                    while (doGetToken) {
                        doGetToken = false;
                        try {
                            variable.getToken();
                        } catch (IllegalActionException ex) {
                            doGetToken = _findUndefinedConstantsOrIdentifiers(ex,
                                    container, parsedContainer);
                        }
                    };
                }
            }
        }
        return _variableBuffer.toString();
    }

    /** Given an UndefinedConstantOrIdentifierException, find
     *  missing variables.
     */
    private static boolean _findUndefinedConstantsOrIdentifiers(
            IllegalActionException exception,
            NamedObj container, TypedCompositeActor parsedContainer) 
            throws IllegalActionException {

        // True if we should rerun the outer parse or getToken
        boolean doRerun = false;

        // Ok, we have a variable that might have an appropriate
        // undefined constant or identifier.

        // If the current exception is appropriate, or its cause is
        // appropriate, then we look for the missing variable.  If the
        // exception or its cause does not have the node name, then we
        // can't do anything.

        UndefinedConstantOrIdentifierException idException = null;
        if (exception instanceof 
                UndefinedConstantOrIdentifierException) {
            idException = (UndefinedConstantOrIdentifierException) exception;
        } else {
            if (exception.getCause() instanceof
                    UndefinedConstantOrIdentifierException) {
                idException = (UndefinedConstantOrIdentifierException)exception.getCause();
            }
        }

        // System.out.println("MoMLUtilities: idException: " + idException);
        if (idException == null) {
            // The exception or the cause was not an
            // UndefinedConstantOrIdentifierException, so we cannot do
            // anything.
            // System.out.println("MoMLUtilities: returning false");
            return false;
        }

        // We have an exception that has the name of the missing
        // variable.
                                
        // Find the variable in the object we are copying.

        // Get the name of the variable without the .auto.
        String variableName = exception.getNameable1().getFullName().substring(((NamedObj)exception.getNameable1()).toplevel().getName().length()+2);

        Attribute masterAttribute = container.getAttribute(variableName);

        // System.out.println("MoMLUtilities: variableName: " + variableName
        //        + " masterAttr: " + masterAttribute);
        if (masterAttribute instanceof Variable) {
            Variable masterVariable = (Variable)masterAttribute;
            ParserScope parserScope = masterVariable.getParserScope();
            // System.out.println("MoMLUtilities: parserScope: " + parserScope);
            if (parserScope instanceof ModelScope) {
                if (masterVariable != null) {
                    Variable node = masterVariable.getVariable(idException.nodeName());
                    // System.out.println("MoMLUtilities: node: " + node
                    //        + " _previousNode: " + _previousNode);
                    if (node == _previousNode) {
                        // We've already seen this node, so stop
                        // looping through the getToken() loop.
                        // System.out.println("MoMLUtilities: returning false!");
                        return false;
                    }
                    _previousNode = node;

                    try {
                        // System.out.println("MoMLUtilities: about to export"
                        //        + " for " + node);

                        String moml = node.exportMoML();

                        // Insert the new variable so that other
                        // variables may use it.

                        MoMLChangeRequest change = new MoMLChangeRequest(parsedContainer,
                                parsedContainer,
                                moml);

                        if (parsedContainer != null) {
                            // If we are parsing the moml for the first
                            // time, then the parsedContainer might be null.
                            parsedContainer.requestChange(change);
                        }
                        _variableBuffer.append(moml);

                        // Rerun the getToken() call in case there are
                        // other problem variables.
                        doRerun = true;
                    } catch (Throwable ex2) {
                        // Ignore and hope the user pastes into a
                        // location where the variable is defined
                    }
                }
            }
        }
        // System.out.println("MoMLUtilities: returning " + doRerun);
        return doRerun;
    }

    /** The previous node for which we searched.  We keep track of
     *  this to avoid infinite loops.
     */
    private static Variable _previousNode;

    /** The moml of any missing variables we have found thus far.
     */   
    private static StringWriter _variableBuffer;

}
