/* Handle MoML Parsing Errors.

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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;

import java.io.OutputStream;
import java.io.PrintStream;

//////////////////////////////////////////////////////////////////////////
//// StreamErrorHandler
/**
Basic error handler for the MoMLParser class. This error handler reports
errors to a stream or to standard error, and requests that parsing continue.

@see MoMLParser
@author Edward A. Lee
@version $Id$
*/
public class StreamErrorHandler implements ErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create an error handler that sends messages to the standard error.
     */
    public StreamErrorHandler() {
        _output = System.err;
    }


    /** Create an error handler that sends messages to the specified stream.
     */
    public StreamErrorHandler(OutputStream out) {
        _output = new PrintStream(out);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.  This method does nothing.
     *  @param enable True to enable skipping, false to disable.
     */
    public void enableErrorSkipping(boolean enable) {
    }

    /** Handle an error by printing a description of the error to
     *  the stream specified in the constructor.
     *  @param element The XML element that triggered the error.
     *  @param attributes The attributes of the element as a Map.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return CONTINUE to request skipping this element.
     */
    public int handleError(
            String element,
            NamedObj context,
            Exception exception) {
       String message = "Error encountered in:\n"
               + element
               + "\n"
               + exception.getMessage();
        _output.println(message);
        return CONTINUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private PrintStream _output;
}

