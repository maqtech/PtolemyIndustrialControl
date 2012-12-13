package ptolemy.domains.openmodelica.lib.omc.corba;


/**
* OmcCommunicationHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from omc_communication.idl
* Thursday, October 27, 2005 10:11:20 AM CEST
*/


// As simple as can be omc communication, sending and recieving of strings.
public final class OmcCommunicationHolder implements org.omg.CORBA.portable.Streamable
{
  public OmcCommunication value = null;

  public OmcCommunicationHolder ()
  {
  }

  public OmcCommunicationHolder (OmcCommunication initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = OmcCommunicationHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    OmcCommunicationHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return OmcCommunicationHelper.type ();
  }

}
