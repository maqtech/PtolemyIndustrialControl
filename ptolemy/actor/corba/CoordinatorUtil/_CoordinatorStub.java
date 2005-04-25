package ptolemy.actor.corba.CoordinatorUtil;


/**
 * ptolemy/actor/corba/CoordinatorUtil/_CoordinatorStub.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from Coordinator.idl
 *
 */

/* A CORBA compatible interface for a coordinator.
 */
public class _CoordinatorStub extends org.omg.CORBA.portable.ObjectImpl
    implements ptolemy.actor.corba.CoordinatorUtil.Coordinator {
    /* this method is intended to be called remotely by the client
     * to register with the coordinator.
     */
    public void register(String clientName,
        ptolemy.actor.corba.CoordinatorUtil.Client clientRef)
        throws ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException {
        org.omg.CORBA.portable.InputStream $in = null;

        try {
            org.omg.CORBA.portable.OutputStream $out = _request("register", true);
            $out.write_string(clientName);
            ptolemy.actor.corba.CoordinatorUtil.ClientHelper.write($out,
                clientRef);
            $in = _invoke($out);
            return;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();

            String _id = $ex.getId();

            if (_id.equals(
                                "IDL:CoordinatorUtil/CorbaIllegalActionException:1.0")) {
                throw ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                            .read($in);
            } else {
                throw new org.omg.CORBA.MARSHAL(_id);
            }
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            register(clientName, clientRef);
        } finally {
            _releaseReply($in);
        }
    } // register

    /* this method is intended to be called remotely by the client,
     * so that data can be delived back over the network.
     */
    public void result(String clientName, org.omg.CORBA.Any data)
        throws ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException {
        org.omg.CORBA.portable.InputStream $in = null;

        try {
            org.omg.CORBA.portable.OutputStream $out = _request("result", true);
            $out.write_string(clientName);
            $out.write_any(data);
            $in = _invoke($out);
            return;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();

            String _id = $ex.getId();

            if (_id.equals(
                                "IDL:CoordinatorUtil/CorbaIllegalActionException:1.0")) {
                throw ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                            .read($in);
            } else {
                throw new org.omg.CORBA.MARSHAL(_id);
            }
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            result(clientName, data);
        } finally {
            _releaseReply($in);
        }
    } // result

    /* this method is intended to be called remotely by the client
     * to unregister with this when it leaves.
     */
    public void unregister(String consumerName)
        throws ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException {
        org.omg.CORBA.portable.InputStream $in = null;

        try {
            org.omg.CORBA.portable.OutputStream $out = _request("unregister",
                    true);
            $out.write_string(consumerName);
            $in = _invoke($out);
            return;
        } catch (org.omg.CORBA.portable.ApplicationException $ex) {
            $in = $ex.getInputStream();

            String _id = $ex.getId();

            if (_id.equals(
                                "IDL:CoordinatorUtil/CorbaIllegalActionException:1.0")) {
                throw ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                            .read($in);
            } else {
                throw new org.omg.CORBA.MARSHAL(_id);
            }
        } catch (org.omg.CORBA.portable.RemarshalException $rm) {
            unregister(consumerName);
        } finally {
            _releaseReply($in);
        }
    } // unregister

    // Type-specific CORBA::Object operations
    private static String[] __ids = {
            "IDL:CoordinatorUtil/Coordinator:1.0"
        };

    public String[] _ids() {
        return (String[]) __ids.clone();
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
} // class _CoordinatorStub
