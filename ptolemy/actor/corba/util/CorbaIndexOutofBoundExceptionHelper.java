package ptolemy.actor.corba.util;


/**
 * ptolemy/actor/corba/util/CorbaIndexOutofBoundExceptionHelper.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from CorbaActor.idl
 * Thursday, January 18, 2001 7:07:58 PM PST
 */

abstract public class CorbaIndexOutofBoundExceptionHelper
{
    private static String  _id = "IDL:util/CorbaIndexOutofBoundException:1.0";

    public static void insert (org.omg.CORBA.Any a, ptolemy.actor.corba.util.CorbaIndexOutofBoundException that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }

    public static ptolemy.actor.corba.util.CorbaIndexOutofBoundException extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    private static boolean __active = false;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                synchronized (org.omg.CORBA.TypeCode.class)
                    {
                        if (__typeCode == null)
                            {
                                if (__active)
                                    {
                                        return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
                                    }
                                __active = true;
                                org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [1];
                                org.omg.CORBA.TypeCode _tcOf_members0 = null;
                                _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_short);
                                _members0[0] = new org.omg.CORBA.StructMember (
                                        "index",
                                        _tcOf_members0,
                                        null);
                                __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.id (), "CorbaIndexOutofBoundException", _members0);
                                __active = false;
                            }
                    }
            }
        return __typeCode;
    }

    public static String id ()
    {
        return _id;
    }

    public static ptolemy.actor.corba.util.CorbaIndexOutofBoundException read (org.omg.CORBA.portable.InputStream istream)
    {
        ptolemy.actor.corba.util.CorbaIndexOutofBoundException value = new ptolemy.actor.corba.util.CorbaIndexOutofBoundException ();
        // read and discard the repository ID
        istream.read_string ();
        value.index = istream.read_short ();
        return value;
    }

    public static void write (org.omg.CORBA.portable.OutputStream ostream, ptolemy.actor.corba.util.CorbaIndexOutofBoundException value)
    {
        // write the repository ID
        ostream.write_string (id ());
        ostream.write_short (value.index);
    }

}
