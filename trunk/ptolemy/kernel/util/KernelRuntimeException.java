/* A base class for runtime exceptions.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

package ptolemy.kernel.util;

import java.io.StringWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// KernelRuntimeException
/**
Base class for runtime exceptions.  This class extends the basic
Java RuntimeException with a constructor that can take a Nameable as
an argument.

This exception supports all the constructor forms of KernelException,
but is implemented as a RuntimeException so that it does not have to
be declared.

<p>The cause argument to the constructor is a Throwable that
caused the exception.  The cause argument is used when code throws
an exception and we want to rethrow the exception but print
the stacktrace where the first exception occurred.  This is called
exception chaining.

<p>JDK1.4 supports exception chaining.  We are implementing a version of
exception chaining here ourselves so that we can use JVMs earlier
than JDK1.4.
See the {@link KernelException KernelException} documentation
for differences between our exception chaining implementation and
the JDK1.4 implementation.

@author Edward A. Lee, Christopher Hylands
@version $Id$ */
public class KernelRuntimeException extends RuntimeException {

    /** Construct an exception with a no specific detail message. */
    public KernelRuntimeException() {
        // Note: this nullary exception is required.  If it is
        // not present, then the subclasses of this class will not
        // compile.
        this(null, null, null, null);
    }

    /** Construct an exception with only a detail message.
     *  @param detail The message.
     */
    public KernelRuntimeException(String detail) {
        this(null, null, null, detail);
    }

    /** Construct an exception with a cause and a detail message.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelRuntimeException(Throwable cause, String detail) {
        this(null, null, cause, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param object The object.
     *  @param detail The message.
     */
    public KernelRuntimeException(Nameable object, String detail) {
        this(object, null, null, detail);
    }

    /** Construct an exception with a detail message that includes
     *  the names of the first two arguments plus the fourth argument
     *  string.  If the cause argument is non-null, then the message
     *  of this exception will include the message of the cause
     *  argument.  The stack trace of the cause argument is used when
     *  we print the stack trace of this exception.  If one or more of
     *  the parameters are null, then the detail message is adjusted
     *  accordingly.
     *
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelRuntimeException(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        _setMessage(KernelException.generateMessage(object1, object2,
                cause, detail));
        _setCause(cause);
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a collection of nameable objects plus the argument string.
     *  @param objects The Collection of Nameable objects
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelRuntimeException(Collection objects,
            Throwable cause, String detail) {
        StringBuffer prefixBuffer = new StringBuffer();
        String name;

        // This method could be a static method in KernelException,
        // but there are no constructors in KernelException or any of
        // KernelException's derived classes that take a Collection
        // as an argument, so we leave this constructor here since
        // there is little point in adding features that are not used.

        // If you make a formatting change here, be sure to update
        // KernelException.generateMessage(Nameable, Nameable,
        //  Throwable, String)

        if (objects.size() == 1) {
            prefixBuffer.append("Object name: ");
        } else if (objects.size() > 1) {
            prefixBuffer.append("Object names: ");
        }
        Iterator objectIterator = objects.iterator();
        while(objectIterator.hasNext()) {
            Object object = objectIterator.next();
            if (object instanceof Nameable) {
                name = KernelException.getFullName((Nameable)object);
            } else {
                name = "<Object of class " +
                    object.getClass().getName() + ">";
            }
            prefixBuffer.append(name + ", ");
        }

        String prefix = prefixBuffer.toString();

        if (prefix.endsWith(", ")) {
            // Remove the trailing ", " which was added processing the
            // last element of the collection.
            prefix = prefix.substring(0, prefix.length()-2);
        }

        _setMessage(KernelException.generateMessage(prefix, cause, detail));
        _setCause(cause);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the cause of this exception.
     *  @return The cause that was passed in as an argument to the
     *  constructor, or null of no cause was specified.
     */
    public Throwable getCause() {
        return _cause;
    }

    /** Get the message of this exception.  The message may have been
     *  adjusted if one of the constructor arguments was null, so the
     *  value returned by this method may not necessarily equal the
     *  value of the detail argument of of the constructor.
     *
     *  @return The error message.
     */
     public String getMessage() {
            return _message;
    }

    /** Print the following to stderr:
     *  this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     */
    public void printStackTrace() {
        // Note that chained exceptions are new JDK1.4.
        // We are implement them ourselves here so that we can
        // use JVMs earlier than JDK1.4.  The JDK1.4 Throwable.getCause()
        // documentation states that it is not necessary to overwrite
        // printStackTrace, but this is only the case when we have a JDK1.4
        // JVM.

        // We could try to factor out the printStackTrace() methods
        // and call package friendly methods in KernelException,
        // but these methods are so short, so why bother.

        printStackTrace(new PrintWriter(System.err));
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     *  @param printStream The PrintStream to write to.
     */
    public void printStackTrace(PrintStream printStream) {
        printStackTrace(new PrintWriter(printStream));
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     *  @param printWriter The PrintWriter to write to.
     */
    public void printStackTrace(PrintWriter printWriter) {
        super.printStackTrace(printWriter);
        if (_cause != null) {
            printWriter.print("Caused by: ");
            _cause.printStackTrace(printWriter);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the cause to the specified throwable.
     *  @param cause The cause of this exception
     */
    protected void _setCause(Throwable cause) {
        _cause = cause;
    }

    /** Sets the error message to the specified string.
     *  If the message argument is null, then the error
     *  message is sent to the empty string.
     *  @param message The message.
     */
    protected void _setMessage(String message) {
        // See KernelException._setMessage() for an explanation
        // about why we need this message instead of calling super().
        if (message == null) {
            _message = "";
        } else {
            _message = message;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The detail message.
    private String _message;

    // The cause of this exception.
    private Throwable _cause;
}
