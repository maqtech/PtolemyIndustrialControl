package ptolemy.actor.corba.util;


/**
* ptolemy/actor/corba/util/CorbaIllegalActionException.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 7:07:58 PM PST
*/

public final class CorbaIllegalActionException extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity
{
    public String message = null;

    public CorbaIllegalActionException ()
    {
    } // ctor

    public CorbaIllegalActionException (String _message)
    {
        message = _message;
    } // ctor

} // class CorbaIllegalActionException
