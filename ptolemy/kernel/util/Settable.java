/* Interface for attributes that can have their values externally set.

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
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Settable
/**
This is an interface for attributes that can have their values
externally set.  An attribute class that implements this interface has to
be able to have a value set by a string, via the setExpression()
method.  A string representation is returned by the getExpression()
method.  These values are called "expressions" for historical
reasons, although this interface, and all uses of it, regard the
values merely as strings.
<p>
In addition, an attribute class that implements this interface
needs to maintain a list of listeners that are informed whenever
the value of the attribute changes.  It should inform those
listeners whenever setExpression() is called.
<p>
Among other uses, this interface marks attributes whose value
can be set via the value attribute of a MoML property element.
For example, if class XXX implements Settable, then the following
is valid MoML:
<pre>
  &lt;property name="xxx" class="XXX" value="yyy"/&gt;
</pre>

@author Edward A. Lee
@version $Id$
*/

public interface Settable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this settable
     *  object changes. An implementation of this method should ignore
     *  the call if the specified listener is already on the list of
     *  listeners.  In other words, it should not be possible for the
     *  same listener to be notified twice of a value update.
     *  @param listener The listener to add.
     */
    public void addValueListener(ValueListener listener);

    /** Get the value of the attribute that has been set by setExpression(),
     *  or null if there is none.
     *  @return The expression.
     */
    public String getExpression();

    /** Remove a listener from the list of listeners that are
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener);

    /** Set the value of the attribute by giving some expression.
     *  @param expression The value of the attribute.
     *  @exception IllegalActionException If the expression is invalid.
     */
    public void setExpression(String expression) throws IllegalActionException;
}
