package ptolemy.domains.openmodelica.lib.omc.corba;


/**
* OmcCommunicationHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from omc_communication.idl
* Thursday, October 27, 2005 10:11:20 AM CEST
*/


// As simple as can be omc communication, sending and recieving of strings.
abstract public class OmcCommunicationHelper
{
  private static String  _id = "IDL:OmcCommunication:1.0";

  public static void insert (org.omg.CORBA.Any a, OmcCommunication that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static OmcCommunication extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (OmcCommunicationHelper.id (), "OmcCommunication");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static OmcCommunication read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_OmcCommunicationStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, OmcCommunication value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static OmcCommunication narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof OmcCommunication)
      return (OmcCommunication)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _OmcCommunicationStub stub = new _OmcCommunicationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static OmcCommunication unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof OmcCommunication)
      return (OmcCommunication)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      _OmcCommunicationStub stub = new _OmcCommunicationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
