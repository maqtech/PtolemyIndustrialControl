package ptolemy.actor.corba.RemoteManagerUtil;


/**
* ptolemy/actor/corba/RemoteManagerUtil/RemoteManagerHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from RemoteManager.idl
* Thursday, January 16, 2003 3:50:31 PM PST
*/


/* A CORBA compatible interface that implements the execution
         * methods of Ptolemy II.
         */
abstract public class RemoteManagerHelper
{
  private static String  _id = "IDL:RemoteManagerUtil/RemoteManager:1.0";

  public static void insert (org.omg.CORBA.Any a, ptolemy.actor.corba.RemoteManagerUtil.RemoteManager that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ptolemy.actor.corba.RemoteManagerUtil.RemoteManager extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (ptolemy.actor.corba.RemoteManagerUtil.RemoteManagerHelper.id (), "RemoteManager");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ptolemy.actor.corba.RemoteManagerUtil.RemoteManager read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_RemoteManagerStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ptolemy.actor.corba.RemoteManagerUtil.RemoteManager value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static ptolemy.actor.corba.RemoteManagerUtil.RemoteManager narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ptolemy.actor.corba.RemoteManagerUtil.RemoteManager)
      return (ptolemy.actor.corba.RemoteManagerUtil.RemoteManager)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ptolemy.actor.corba.RemoteManagerUtil._RemoteManagerStub stub = new ptolemy.actor.corba.RemoteManagerUtil._RemoteManagerStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
