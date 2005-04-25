package ptolemy.domains.ct.demo.Corba.util;


/**
 * ptolemy/domains/ct/demo/Corba/util/CorbaActorHolder.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from CorbaActor.idl
 * Thursday, January 18, 2001 5:51:19 PM PST
 */

/* A CORBA compatible interface that implements the execution
 * methods of Ptolemy II.
 */
public final class CorbaActorHolder implements org.omg.CORBA.portable.Streamable {
    public ptolemy.domains.ct.demo.Corba.util.CorbaActor value = null;

    public CorbaActorHolder() {
    }

    public CorbaActorHolder(
        ptolemy.domains.ct.demo.Corba.util.CorbaActor initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = ptolemy.domains.ct.demo.Corba.util.CorbaActorHelper.read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        ptolemy.domains.ct.demo.Corba.util.CorbaActorHelper.write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.domains.ct.demo.Corba.util.CorbaActorHelper.type();
    }
}
