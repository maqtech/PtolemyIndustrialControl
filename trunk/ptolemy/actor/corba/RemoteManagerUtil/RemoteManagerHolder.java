package ptolemy.actor.corba.RemoteManagerUtil;

/**
* ptolemy/actor/corba/RemoteManagerUtil/RemoteManagerHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from RemoteManager.idl
* Thursday, January 16, 2003 3:50:31 PM PST
*/


/* A CORBA compatible interface that implements the execution
	 * methods of Ptolemy II.
	 */
public final class RemoteManagerHolder implements org.omg.CORBA.portable.Streamable
{
  public ptolemy.actor.corba.RemoteManagerUtil.RemoteManager value = null;

  public RemoteManagerHolder ()
  {
  }

  public RemoteManagerHolder (ptolemy.actor.corba.RemoteManagerUtil.RemoteManager initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ptolemy.actor.corba.RemoteManagerUtil.RemoteManagerHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ptolemy.actor.corba.RemoteManagerUtil.RemoteManagerHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ptolemy.actor.corba.RemoteManagerUtil.RemoteManagerHelper.type ();
  }

}
