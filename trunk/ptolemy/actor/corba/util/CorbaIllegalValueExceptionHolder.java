/*
 * File: ../../../ptolemy/actor/corba/util/CorbaIllegalValueExceptionHolder.java
 * From: CorbaActor.idl
 * Date: Wed Jul 28 17:18:28 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public final class CorbaIllegalValueExceptionHolder
    implements org.omg.CORBA.portable.Streamable{
    //	instance variable
    public ptolemy.actor.corba.util.CorbaIllegalValueException value;
    //	constructors
    public CorbaIllegalValueExceptionHolder() {
	this(null);
    }
    public CorbaIllegalValueExceptionHolder(ptolemy.actor.corba.util.CorbaIllegalValueException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.type();
    }
}
