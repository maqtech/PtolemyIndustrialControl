package ptolemy.actor.corba.CoordinatorUtil;


/**
* ptolemy/actor/corba/CoordinatorUtil/_ClientImplBase.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from Coordinator.idl
* 2003年9月8日 星期一 下午07时32分59秒 PDT
*/


/* A CORBA compatible interface for a consumer.
	 */
public abstract class _ClientImplBase extends org.omg.CORBA.portable.ObjectImpl
                implements ptolemy.actor.corba.CoordinatorUtil.Client, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors
  public _ClientImplBase ()
  {
  }

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("push", new java.lang.Integer (0));
    _methods.put ("start", new java.lang.Integer (1));
    _methods.put ("stop", new java.lang.Integer (2));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {

  /* this method is intended to be called remotely to
  	     * send data to it.
  	     */
       case 0:  // CoordinatorUtil/Client/push
       {
         try {
           org.omg.CORBA.Any data = in.read_any ();
           this.push (data);
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* this method is intended to be called remotely to start the application
           * for the consumer.
           */
       case 1:  // CoordinatorUtil/Client/start
       {
         this.start ();
         out = $rh.createReply();
         break;
       }


  /* this method is intended to be called remotely to stop the application
           * for the consumer.
           */
       case 2:  // CoordinatorUtil/Client/stop
       {
         this.stop ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:CoordinatorUtil/Client:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }


} // class _ClientImplBase
