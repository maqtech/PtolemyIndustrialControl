package ptolemy.actor.corba.util;

/**
 * ptolemy/actor/corba/util/CorbaIndexOutofBoundException.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from CorbaActor.idl
 * Thursday, January 18, 2001 7:07:58 PM PST
 */
public final class CorbaIndexOutofBoundException extends
        org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    public short index = (short) 0;

    public CorbaIndexOutofBoundException() {
    } // ctor

    public CorbaIndexOutofBoundException(short _index) {
        index = _index;
    } // ctor
} // class CorbaIndexOutofBoundException
