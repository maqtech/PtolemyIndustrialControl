package ptolemy.actor.corba.CorbaIOUtil;

/**
 * ptolemy/actor/corba/CorbaIOUtil/pullSupplierHolder.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from CorbaIO.idl
 * Wednesday, April 16, 2003 5:05:14 PM PDT
 */


/* A CORBA compatible interface for a pull supplier.
 */
public final class pullSupplierHolder implements org.omg.CORBA.portable.Streamable
{
    public ptolemy.actor.corba.CorbaIOUtil.pullSupplier value = null;

    public pullSupplierHolder ()
    {
    }

    public pullSupplierHolder (ptolemy.actor.corba.CorbaIOUtil.pullSupplier initialValue)
    {
        value = initialValue;
    }

    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = ptolemy.actor.corba.CorbaIOUtil.pullSupplierHelper.read (i);
    }

    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        ptolemy.actor.corba.CorbaIOUtil.pullSupplierHelper.write (o, value);
    }

    public org.omg.CORBA.TypeCode _type ()
    {
        return ptolemy.actor.corba.CorbaIOUtil.pullSupplierHelper.type ();
    }

}
