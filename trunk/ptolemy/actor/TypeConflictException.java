/* Exception thrown on detecting type conflicts.

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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;

import ptolemy.kernel.util.KernelException;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TypeConflictException
/**
Thrown on detecting type conflicts.
This class contains all the TypedIOPorts where type conflicts occurred.
There are several kinds of type conflicts: (1) Two TypedIOPorts
with declared types are connected, but the type of the port at the
source end of the connection is not less than or equal to the type of
the port at the destination end. In this case, both ports should be
included in this exception; (2) A type constraint cannot be satisfied
in type resolution. In this case, all the ports whose types are
involved in that constraint should be included in this exception;
(3) After type resolution, the type of a port is resolved to NaT (not
a type), or a type corresponding to an abstract token class or interface.

@author Yuhong Xiong
@version $Id$
*/
public class TypeConflictException extends KernelException {

    /** Construct an Exception with an Enumeration of TypedIOPorts.
     *  The ports are the places where type conflicts
     *  occurred.  The detailed message of this Exception will be
     *  the string "Type conflicts occured on the following ports:",
     *  followed by a list of ports and their resolved types. The
     *  type is represented by the corresponding class name. For
     *  example, the type "Int" is represented by "ptolemy.data.IntToken"
     *  in the message.
     *  Each port takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param ports an Enumeration of TypedIOPorts.
     */
    public TypeConflictException(Enumeration ports) {
	this(ports, "Type conflicts occured on the following ports:");
    }

    /** Construct an Exception with an Enumeration of TypedIOPorts
     *  and a message. The ports are the places where type conflicts
     *  occurred.  The detailed message of this Exception will be
     *  the specified message, following by the list of ports with
     *  type conflicts, and their resolved types.  The type is
     *  represented by the corresponding class name. For example,
     *  the type "Int" is represented by "ptolemy.data.IntToken"
     *  in the message.
     *  Each port takes one line, and each line starts
     *  with 2 white spaces to make the message more readable.
     *  @param ports an Enumeration of TypedIOPorts.
     *  @param detail a message.
     */
    public TypeConflictException(Enumeration ports, String detail) {
	_portList.appendElements(ports);
	_setMessage(detail + "\n" + _getPortsAndTypes());
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Return an Enumeration of TypedIOPorts where type conflicts
     *  occurred. The ports are those specified in the Enumeration
     *  argument of the constructor.
     *  @return An Enumeration.
     */
    public Enumeration getPorts() {
	return _portList.elements();
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // Create a string listing all the ports in _portList and their
    // resolved types. Each port takes one line, and each line starts
    // with 2 white spaces to make the String more readable.
    private String _getPortsAndTypes() {
	String result = "";;
	Enumeration ports = getPorts();
	while(ports.hasMoreElements()) {
	    TypedIOPort port = (TypedIOPort)ports.nextElement();
	    result += "  " + port.getFullName() + ": ";
	    Class type = port.getResolvedType();
	    if (type.equals(Void.TYPE)) {
		result += "NaT\n";
	    } else {
		result += type.getName() + "\n";
	    }
	}

	return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList _portList = new LinkedList();
}

