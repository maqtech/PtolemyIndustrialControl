package ptolemy.actor.corba.CorbaIOUtil;


/**
 * ptolemy/actor/corba/CorbaIOUtil/CorbaIllegalActionException.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from CorbaIO.idl
 * Wednesday, April 16, 2003 5:05:14 PM PDT
 */

public final class CorbaIllegalActionException extends org.omg.CORBA.UserException
{
    public String message = null;

    public CorbaIllegalActionException ()
    {
        super(CorbaIllegalActionExceptionHelper.id());
    } // ctor

    public CorbaIllegalActionException (String _message)
    {
        super(CorbaIllegalActionExceptionHelper.id());
        message = _message;
    } // ctor


    public CorbaIllegalActionException (String $reason, String _message)
    {
        super(CorbaIllegalActionExceptionHelper.id() + "  " + $reason);
        message = _message;
    } // ctor

} // class CorbaIllegalActionException
