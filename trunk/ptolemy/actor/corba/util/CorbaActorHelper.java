package ptolemy.actor.corba.util;


/**
* ptolemy/actor/corba/util/CorbaActorHelper.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 7:07:58 PM PST
*/


/* A CORBA compatible interface that implements the execution
	 * methods of Ptolemy II.
	 */
abstract public class CorbaActorHelper
{
  private static String  _id = "IDL:util/CorbaActor:1.0";

  public static void insert (org.omg.CORBA.Any a, ptolemy.actor.corba.util.CorbaActor that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ptolemy.actor.corba.util.CorbaActor extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (ptolemy.actor.corba.util.CorbaActorHelper.id (), "CorbaActor");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ptolemy.actor.corba.util.CorbaActor read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_CorbaActorStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ptolemy.actor.corba.util.CorbaActor value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static ptolemy.actor.corba.util.CorbaActor narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ptolemy.actor.corba.util.CorbaActor)
      return (ptolemy.actor.corba.util.CorbaActor)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      return new ptolemy.actor.corba.util._CorbaActorStub (delegate);
    }
  }

}
