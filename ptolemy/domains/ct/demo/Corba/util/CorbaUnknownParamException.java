package ptolemy.domains.ct.demo.Corba.util;


/**
* ptolemy/domains/ct/demo/Corba/util/CorbaUnknownParamException.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 5:51:19 PM PST
*/

public final class CorbaUnknownParamException extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity
{
    public String paramName = null;
    public String message = null;

    public CorbaUnknownParamException ()
    {
    } // ctor

    public CorbaUnknownParamException (String _paramName, String _message)
    {
        paramName = _paramName;
        message = _message;
    } // ctor

} // class CorbaUnknownParamException
