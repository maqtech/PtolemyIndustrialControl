/* Interface for objects that can store tokens.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bart@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// Receiver
/**
Interface for objects that can hold tokens. An implementation of this
interface has two key methods: put() and get(). The put() method
deposits a token into the receiver. The get() method retrieves
a token that has been put. The order of
the retrieved tokens depends on specific implementations, and does not
necessarily match the order in which tokens have been put.
<p>
All implementations of this interface must follow these rules, regardless
of the number of threads that are accessing the receiver:
<ul>
<li> If hasToken() returns true, then the next call to get() must not
result in a NoTokenException being thrown.
<li> If hasRoom() returns true, then the next call to put() must not
result in a NoRoomException being thrown.
</ul>
In general, this means that multithreaded domains must provide
synchronization for receivers. Note that both NoTokenException
and NoRoomException are runtime exceptions, so they need not be
declared explicitly.
<p>
Objects that implement this interface can only be contained
by an instance of IOPort.

@author Jie Liu, Edward A. Lee, Lukito Muliadi
@version $Id$
@see Token
*/
public interface Receiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a token from this receiver.
     *  @return A token read from the receiver.
     *  @exception NoTokenException If there is no token.
     */
    public Token get() throws NoTokenException;

    /** Get an array of tokens from this receiver. The <i>numberOfTokens</i>
     *  argument specifies the number of tokens to get. In an implementation,
     *  the length of the returned array is allowed to be greater than
     *  <i>numberOfTokens</i>, in which case, only the first
     *  <i>numberOfTokens</i> elements are the ones that have been read
     *  from the receiver. In some domains, this allows the implementation
     *  to be more efficient.
     *  @param numberOfTokens The number of tokens to get in the
     *   returned array.
     *  @return An array of tokens read from the receiver.
     *  @exception NoTokenException If there are not <i>numberOfTokens</i>
     *   tokens.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException;

    /** Return the container of this receiver, or null if there is none.
     *  @return The port containing this receiver.
     */
    public IOPort getContainer();

    /** Return true if the receiver has room to put a token into it
     *  (via the put() method).
     *  Returning true in this method guarantees that the next call to
     *  put() will not result in an exception.
     *  @return True if the next call to put() will not result in a
     *   NoRoomException.
     */
    public boolean hasRoom();

    /** Return true if the receiver has room to put the specified number of
     *  tokens into it (via the put() method).
     *  Returning true in this method guarantees that the next
     *  <i>numberOfTokens</i> calls to put() or a corresponding call
     *  to putArray() will not result in an exception.
     *  @param numberOfTokens The number of tokens to put into this receiver.
     *  @return True if the next <i>numberOfTokens</i> calls to put()
     *   will not result in a NoRoomException.
     */
    public boolean hasRoom(int numberOfTokens);

    /** Return true if the receiver contains a token that can be obtained
     *  by calling the get() method.  In an implementation,
     *  returning true in this method guarantees that the next
     *  call to get() will not result in an exception.
     *  @return True if the next call to get() will not result in a
     *   NoTokenException.
     */
    public boolean hasToken();

    /** Return true if the receiver contains the specified number of tokens.
     *  In an implementation, returning true in this method guarantees
     *  that the next <i>numberOfTokens</i> calls to get(), or a 
     *  corresponding call to getArray(), will not result in an exception.
     *  @param numberOfTokens The number of tokens desired.
     *  @return True if the next <i>numberOfTokens</i> calls to get()
     *   will not result in a NoTokenException.
     */
    public boolean hasToken(int numberOfTokens);

    /** Put the specified token into this receiver.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    public void put(Token token) throws NoRoomException;

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver.  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
             throws NoRoomException;

    /** Set the container.
     *  @param port The container.
     *  @exception IllegalActionException If the container is not of
     *   an appropriate subclass of IOPort for the particular receiver
     *   implementation.
     */
    public void setContainer(IOPort port) throws IllegalActionException;
}
