package ptolemy.actor.corba.CoordinatorUtil;


/**
* ptolemy/actor/corba/CoordinatorUtil/_ClientStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from Coordinator.idl
* 2003年9月8日 星期一 下午07时32分59秒 PDT
*/


/* A CORBA compatible interface for a consumer.
	 */
public class _ClientStub extends org.omg.CORBA.portable.ObjectImpl implements ptolemy.actor.corba.CoordinatorUtil.Client
{


  /* this method is intended to be called remotely to
  	     * send data to it.
  	     */
  public void push (org.omg.CORBA.Any data) throws ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("push", true);
                $out.write_any (data);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:CoordinatorUtil/CorbaIllegalActionException:1.0"))
                    throw ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                push (data        );
            } finally {
                _releaseReply ($in);
            }
  } // push


  /* this method is intended to be called remotely to start the application
           * for the consumer.
           */
  public void start ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("start", true);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                start (        );
            } finally {
                _releaseReply ($in);
            }
  } // start


  /* this method is intended to be called remotely to stop the application
           * for the consumer.
           */
  public void stop ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("stop", true);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                stop (        );
            } finally {
                _releaseReply ($in);
            }
  } // stop

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:CoordinatorUtil/Client:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init (args, props).string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     String str = org.omg.CORBA.ORB.init (args, props).object_to_string (this);
     s.writeUTF (str);
  }
} // class _ClientStub
