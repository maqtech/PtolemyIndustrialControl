package ptolemy.actor.corba.CoordinatorUtil;


/**
 * ptolemy/actor/corba/CoordinatorUtil/CorbaIllegalActionExceptionHelper.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from Coordinator.idl
 *
 */
abstract public class CorbaIllegalActionExceptionHelper {
    private static String _id = "IDL:CoordinatorUtil/CorbaIllegalActionException:1.0";

    public static void insert(org.omg.CORBA.Any a,
        ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException extract(
        org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    private static boolean __active = false;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            synchronized (org.omg.CORBA.TypeCode.class) {
                if (__typeCode == null) {
                    if (__active) {
                        return org.omg.CORBA.ORB.init().create_recursive_tc(_id);
                    }

                    __active = true;

                    org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember[1];
                    org.omg.CORBA.TypeCode _tcOf_members0 = null;
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_string_tc(0);
                    _members0[0] = new org.omg.CORBA.StructMember("message",
                            _tcOf_members0, null);
                    __typeCode = org.omg.CORBA.ORB.init().create_exception_tc(ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                            .id(), "CorbaIllegalActionException", _members0);
                    __active = false;
                }
            }
        }

        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException read(
        org.omg.CORBA.portable.InputStream istream) {
        ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException value = new ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException();

        // read and discard the repository ID
        istream.read_string();
        value.message = istream.read_string();
        return value;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,
        ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException value) {
        // write the repository ID
        ostream.write_string(id());
        ostream.write_string(value.message);
    }
}
