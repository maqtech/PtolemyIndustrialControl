package ptolemy.actor.corba.CorbaIOUtil;


/**
 * ptolemy/actor/corba/CorbaIOUtil/pullSupplierOperations.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from CorbaIO.idl
 * Wednesday, April 16, 2003 5:05:14 PM PDT
 */


/* A CORBA compatible interface for a pull supplier.
 */
public interface pullSupplierOperations
{

    /* this method is intended to be called remotely by a pull consumer
     * to request data from its supplier.
     */
    org.omg.CORBA.Any pull () throws ptolemy.actor.corba.CorbaIOUtil.CorbaIllegalActionException;
} // interface pullSupplierOperations
