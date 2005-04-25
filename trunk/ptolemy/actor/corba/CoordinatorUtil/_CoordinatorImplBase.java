package ptolemy.actor.corba.CoordinatorUtil;


/**
 * ptolemy/actor/corba/CoordinatorUtil/_CoordinatorImplBase.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from Coordinator.idl
 *
 */

/* A CORBA compatible interface for a coordinator.
 */
public abstract class _CoordinatorImplBase
    extends org.omg.CORBA.portable.ObjectImpl
    implements ptolemy.actor.corba.CoordinatorUtil.Coordinator,
               org.omg.CORBA.portable.InvokeHandler {
    // Constructors
    public _CoordinatorImplBase() {
    }

    private static java.util.Hashtable _methods = new java.util.Hashtable();

    static {
        _methods.put("register", new java.lang.Integer(0));
        _methods.put("result", new java.lang.Integer(1));
        _methods.put("unregister", new java.lang.Integer(2));
    }

    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
            org.omg.CORBA.portable.InputStream in,
            org.omg.CORBA.portable.ResponseHandler $rh) {
        org.omg.CORBA.portable.OutputStream out = null;
        java.lang.Integer __method = (java.lang.Integer) _methods.get($method);

        if (__method == null) {
            throw new org.omg.CORBA.BAD_OPERATION(0,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }

        switch (__method.intValue()) {
            /* this method is intended to be called remotely by the client
             * to register with the coordinator.
             */
        case 0: // CoordinatorUtil/Coordinator/register
            {
                try {
                    String clientName = in.read_string();
                    ptolemy.actor.corba.CoordinatorUtil.Client clientRef = ptolemy.actor.corba.CoordinatorUtil.ClientHelper
                        .read(in);
                    this.register(clientName, clientRef);
                    out = $rh.createReply();
                } catch (ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException $ex) {
                    out = $rh.createExceptionReply();
                    ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                        .write(out, $ex);
                }

                break;
            }

            /* this method is intended to be called remotely by the client,
             * so that data can be delived back over the network.
             */
        case 1: // CoordinatorUtil/Coordinator/result
            {
                try {
                    String clientName = in.read_string();
                    org.omg.CORBA.Any data = in.read_any();
                    this.result(clientName, data);
                    out = $rh.createReply();
                } catch (ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException $ex) {
                    out = $rh.createExceptionReply();
                    ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                        .write(out, $ex);
                }

                break;
            }

            /* this method is intended to be called remotely by the client
             * to unregister with this when it leaves.
             */
        case 2: // CoordinatorUtil/Coordinator/unregister
            {
                try {
                    String consumerName = in.read_string();
                    this.unregister(consumerName);
                    out = $rh.createReply();
                } catch (ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException $ex) {
                    out = $rh.createExceptionReply();
                    ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionExceptionHelper
                        .write(out, $ex);
                }

                break;
            }

        default:
            throw new org.omg.CORBA.BAD_OPERATION(0,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }

        return out;
    } // _invoke

    // Type-specific CORBA::Object operations
    private static String[] __ids = {
        "IDL:CoordinatorUtil/Coordinator:1.0"
    };

    public String[] _ids() {
        return (String[]) __ids.clone();
    }
} // class _CoordinatorImplBase
