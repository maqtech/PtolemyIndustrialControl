/*
 * File: ../../../ptolemy/actor/corba/util/CorbaUnknownPortException.java
 * From: CorbaActor.idl
 * Date: Wed Jul 28 17:18:28 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public final class CorbaUnknownPortException
    extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String portName;
    public String message;
    //	constructors
    public CorbaUnknownPortException() {
	super();
    }
    public CorbaUnknownPortException(String __portName, String __message) {
	super();
	portName = __portName;
	message = __message;
    }
}
