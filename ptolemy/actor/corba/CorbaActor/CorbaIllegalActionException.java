/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaIllegalActionException.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public final class CorbaIllegalActionException
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String message;
    //	constructors
    public CorbaIllegalActionException() {
	super();
    }
    public CorbaIllegalActionException(String __message) {
	super();
	message = __message;
    }
}
