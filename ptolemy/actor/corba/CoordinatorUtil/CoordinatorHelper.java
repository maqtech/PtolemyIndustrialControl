package ptolemy.actor.corba.CoordinatorUtil;

/**
 * ptolemy/actor/corba/CoordinatorUtil/CoordinatorHelper.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from Coordinator.idl
 *
 */

/* A CORBA compatible interface for a coordinator.
 */
abstract public class CoordinatorHelper {
    private static String _id = "IDL:CoordinatorUtil/Coordinator:1.0";

    public static void insert(org.omg.CORBA.Any a,
            ptolemy.actor.corba.CoordinatorUtil.Coordinator that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static ptolemy.actor.corba.CoordinatorUtil.Coordinator extract(
            org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            __typeCode = org.omg.CORBA.ORB.init().create_interface_tc(
                    ptolemy.actor.corba.CoordinatorUtil.CoordinatorHelper.id(),
                    "Coordinator");
        }

        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static ptolemy.actor.corba.CoordinatorUtil.Coordinator read(
            org.omg.CORBA.portable.InputStream istream) {
        return narrow(istream.read_Object(_CoordinatorStub.class));
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,
            ptolemy.actor.corba.CoordinatorUtil.Coordinator value) {
        ostream.write_Object((org.omg.CORBA.Object) value);
    }

    public static ptolemy.actor.corba.CoordinatorUtil.Coordinator narrow(
            org.omg.CORBA.Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof ptolemy.actor.corba.CoordinatorUtil.Coordinator) {
            return (ptolemy.actor.corba.CoordinatorUtil.Coordinator) obj;
        } else if (!obj._is_a(id())) {
            throw new org.omg.CORBA.BAD_PARAM();
        } else {
            org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)
                    ._get_delegate();
            ptolemy.actor.corba.CoordinatorUtil._CoordinatorStub stub = new ptolemy.actor.corba.CoordinatorUtil._CoordinatorStub();
            stub._set_delegate(delegate);
            return stub;
        }
    }
}
