package ptolemy.domains.openmodelica.lib.omc.corba;

/**
 * _OmcCommunicationStub.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.2"
 * from omc_communication.idl
 * Thursday, October 27, 2005 10:11:20 AM CEST
 */

/* A CORBA compatible interface for omc.
 */
public class _OmcCommunicationStub extends org.omg.CORBA.portable.ObjectImpl
        implements OmcCommunication {

    private static final long serialVersionUID = -2199076960265794510L;

    @Override
    public String sendExpression(String expr) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request(
                    "sendExpression", true);
            $out.write_string(expr);
            $in = _invoke($out);
            String $result = $in.read_string();
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return sendExpression(expr);
        } finally {
            _releaseReply($in);
        }
    } // sendExpression

    @Override
    public String sendClass(String model) {
        org.omg.CORBA.portable.InputStream $in = null;
        try {
            org.omg.CORBA.portable.OutputStream $out = _request("sendClass",
                    true);
            $out.write_string(model);
            $in = _invoke($out);
            String $result = $in.read_string();
            return $result;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();
            String _id = $ex.getId();
            throw new org.omg.CORBA.MARSHAL(_id);
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            return sendClass(model);
        } finally {
            _releaseReply($in);
        }
    } // sendClass

    // Type-specific CORBA::Object operations
    private static String[] __ids = { "IDL:OmcCommunication:1.0" };

    @Override
    public String[] _ids() {
        return __ids.clone();
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException {
        String str = s.readUTF();
        String[] args = null;
        java.util.Properties props = null;
        org.omg.CORBA.Object obj = org.omg.CORBA.ORB.init(args, props)
                .string_to_object(str);
        org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)
                ._get_delegate();
        _set_delegate(delegate);
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        String[] args = null;
        java.util.Properties props = null;
        String str = org.omg.CORBA.ORB.init(args, props).object_to_string(this);
        s.writeUTF(str);
    }
} // class _OmcCommunicationStub
