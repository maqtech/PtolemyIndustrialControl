/* Interface for objects that can store tokens.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
@AcceptedRating Red

*/

package ptolemy.actor;

import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// Receiver
/**
Interface for objects that can hold tokens. An implementation of this
interface has two key methods: put and get. Put deposits a token into the
receiver. Get retrieves a token that has been put. The order of
the retrieved tokens depends on specific implementations, and does not
necessarily match the order in which tokens have been put.
<p>
All implementations of this interface must follow these rules, regardless 
of the number of threads that are accessing the receiver:
<ul>
<li> If hasToken() returns true then calling get() should not result in a
NoTokenException being thrown.
<li> If hasRoom() returns true then calling put() should not result in a
NoRoomException being thrown.
</ul>
In general, this means that threaded domains will provide a higher level of 
synchronization for receivers.  
<p>
In addition, objects that implement this interface can only be contained
by an instance of IOPort.

@author Jie Liu, Edward A. Lee, Lukito Muliadi
@version $Id$
*/
public interface Receiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver. Note that the thrown exception
     *  is a runtime exception.
     *  @exception NoTokenException If there is no token.
     */
    public Token get();

    /** Return the container. */
    public IOPort getContainer();

    /** Return true if the receiver has room for putting a token into
     *  (via the put() method).
     *  Returning true in this method should also guarantee that calling
     *  the put() method will not result in an exception.
     *
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public boolean hasRoom() throws IllegalActionException;

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.
     *  Returning true in this method should also guarantee that calling
     *  the get() method will not result in an exception.
     *
     *  @exception IllegalActionException If the Receiver implementation
     *    does not support this query.
     */
    public boolean hasToken() throws IllegalActionException;

    /** Put a token into this receiver. Note that the thrown exception
     *  is a runtime exception, therefore the caller is not required to
     *  catch it.
     *  @exception NoRoomException If the token cannot be put.
     */
    public void put(Token t);

    /** Set the container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort.
     */
    public void setContainer(IOPort port) throws IllegalActionException;
}



