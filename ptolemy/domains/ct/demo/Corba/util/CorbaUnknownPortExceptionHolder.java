package ptolemy.domains.ct.demo.Corba.util;

/**
* ptolemy/domains/ct/demo/Corba/util/CorbaUnknownPortExceptionHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 5:51:19 PM PST
*/

public final class CorbaUnknownPortExceptionHolder implements org.omg.CORBA.portable.Streamable
{
  public ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException value = null;

  public CorbaUnknownPortExceptionHolder ()
  {
  }

  public CorbaUnknownPortExceptionHolder (ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortExceptionHelper.type ();
  }

}
