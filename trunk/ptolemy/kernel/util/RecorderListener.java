/* A debug listeners that records messages in a string buffer.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// StreamListener
/**
A debug listeners that records messages in a string buffer.

@author  Edward A. Lee
@version $Id$
@see NamedObj

*/
public class RecorderListener implements DebugListener {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a listener.
     */
    public RecorderListener() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a debug message.
     */
    public void message(String message) {
        if (_buffer.length() != 0) {
            _buffer.append("\n");
        }
        _buffer.append(message);
    }

    /** Get messages recorded so far.
     */
    public String getMessages() {
        return _buffer.toString();
    }

    /** Clear the buffer.
     */
    public void reset() {
        _buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private StringBuffer _buffer = new StringBuffer();
}
