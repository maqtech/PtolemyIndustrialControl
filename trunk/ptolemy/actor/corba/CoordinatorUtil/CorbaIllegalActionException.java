package ptolemy.actor.corba.CoordinatorUtil;


/**
* ptolemy/actor/corba/CoordinatorUtil/CorbaIllegalActionException.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from Coordinator.idl
* 2003年9月8日 星期一 下午07时32分59秒 PDT
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
