package ptolemy.actor.corba.RemoteManagerUtil;


/**
* ptolemy/actor/corba/RemoteManagerUtil/_RemoteManagerImplBase.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from RemoteManager.idl
* Thursday, January 16, 2003 3:50:31 PM PST
*/


/* A CORBA compatible interface that implements the execution
	 * methods of Ptolemy II.
	 */
public abstract class _RemoteManagerImplBase extends org.omg.CORBA.portable.ObjectImpl
                implements ptolemy.actor.corba.RemoteManagerUtil.RemoteManager, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors
  public _RemoteManagerImplBase ()
  {
  }

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("execute", new java.lang.Integer (0));
    _methods.put ("initialize", new java.lang.Integer (1));
    _methods.put ("pause", new java.lang.Integer (2));
    _methods.put ("resume", new java.lang.Integer (3));
    _methods.put ("startRun", new java.lang.Integer (4));
    _methods.put ("stop", new java.lang.Integer (5));
    _methods.put ("terminate", new java.lang.Integer (6));
    _methods.put ("changeModel", new java.lang.Integer (7));
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

  /* Mirror the execute() method of Ptolemy II
		 * manager interface.
		 * @exception CorbaIllegalActionException If the
		 *   method is an illegal action of the actor.
		 */
       case 0:  // RemoteManagerUtil/RemoteManager/execute
       {
         try {
           this.execute ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the initialize() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 1:  // RemoteManagerUtil/RemoteManager/initialize
       {
         try {
           this.initialize ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the pause() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 2:  // RemoteManagerUtil/RemoteManager/pause
       {
         try {
           this.pause ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the resume() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 3:  // RemoteManagerUtil/RemoteManager/resume
       {
         try {
           this.resume ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the startRun() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 4:  // RemoteManagerUtil/RemoteManager/startRun
       {
         try {
           this.startRun ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the stop() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 5:  // RemoteManagerUtil/RemoteManager/stop
       {
         try {
           this.stop ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the terminate() method of Ptolemy II
  		 * manager interface.
  		 * @exception CorbaIllegalActionException If the
  		 *   method is an illegal action of the actor.
  		 */
       case 6:  // RemoteManagerUtil/RemoteManager/terminate
       {
         try {
           this.terminate ();
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         }
         break;
       }


  /* Mirror the terminate() method of Ptolemy II
  		 * remoteManager interface.
		 * @exception CorbaIllegalActionException If the
		 *  query of parameter is not supported by the actor.
		 * @exception CorbaUnknowParamException If the parameter
		 *  name is not known by the actor.
		 */
       case 7:  // RemoteManagerUtil/RemoteManager/changeModel
       {
         try {
           String model = in.read_string ();
           this.changeModel (model);
           out = $rh.createReply();
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaIllegalActionExceptionHelper.write (out, $ex);
         } catch (ptolemy.actor.corba.RemoteManagerUtil.CorbaUnknownParamException $ex) {
           out = $rh.createExceptionReply ();
           ptolemy.actor.corba.RemoteManagerUtil.CorbaUnknownParamExceptionHelper.write (out, $ex);
         }
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:RemoteManagerUtil/RemoteManager:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }


} // class _RemoteManagerImplBase
