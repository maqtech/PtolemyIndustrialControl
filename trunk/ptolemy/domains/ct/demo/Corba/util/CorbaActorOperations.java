package ptolemy.domains.ct.demo.Corba.util;


/**
* ptolemy/domains/ct/demo/Corba/util/CorbaActorOperations.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 5:51:19 PM PST
*/


/* A CORBA compatible interface that implements the execution
         * methods of Ptolemy II.
         */
public interface CorbaActorOperations
{

    /* Mirror the fire() method of the Ptolemy
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    void fire () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Return the value (in the form of a string) of
     * a parameter.
     * @exception CorbaIllegalActionException If the
     *  query of parameter is not supported by the actor.
     * @exception CorbaUnknowParamException If the parameter
     *  name is not known by the actor.
     */
    String getParameter (String paramName) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException;

    /* Mirror the initialize() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    void initialize () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Return true if the specified channel of the specified
     * port contains unsent data.
     * @exception CorbaIllegalActionException If the query is
     *   not supported by the actor.
     * @exception CorbaUnknownPortException If the specified
     *   port is not known by the actor.
     * @exception CorbaIndexOutofBoundException If the
     *   channel index is out of the width of the port.
     */
    boolean hasData (String portName, short portIndex) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException;

    /* Return true if there is a parameter of the specified
     * name defined in the actor.
     */
    boolean hasParameter (String paramName);

    /* Return true if there is a port of the specified name
     * and specified property contained by the actor.
     * @param portName The name of the port.
     * @param isInput True if the port is an input port.
     * @param isOutput True if the port is an output port.
     * @param isMultiport True if the port is a multiport.
     */
    boolean hasPort (String portName, boolean isInput, boolean isOutput, boolean isMultiport);

    /* Set the width of the specified port.
     * @param portName The name of the port.
     * @param width The width to be set.
     * @exception CorbaIllegalActionException If the width
     *  to be set is not supported by the port, e.g. the
     *  port is restricted to a single port, but the width
     *  to be set is greater than one.
     * @exception CorbaUnknownPortException If the port is \
     *  not known by the actor.
     */
    void setPortWidth (String portName, short width) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException;

    /* Mirror the postfire() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    boolean postfire () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Mirror the prefire() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    boolean prefire () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Set the value of the specified parameter.
     * @param paramName The parameter name.
     * @param paramValue The value to be set.
     * @exception CorbaIllegalActionException If the set
     *  value opertaion is not supported by the parameter.
     * @exception CorbaUnknownParamException If the
     *  parameter name is not known by the actor.
     * @exception CorbaIllegalValueException If the value
     *  is invalid for this parameter.
     */
    void setParameter (String paramName, String paramValue) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownParamException, ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException;

    /*  Mirror the stopFire() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    void stopFire () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Mirror the terminate() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    void terminate () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;

    /* Transfer the input data to the specified port.
     * @param portName The port name.
     * @param portIndex The channel number within the port.
     * @param tokenValue The string for the value of the
     *        data token.
     * @exception CorbaIllegalActionException If the action is
     *  illegal.
     * @exception CorbaUnknownPortException If the port is unknown.
     * @exception CorbaIndexOutofBoundException If the index
     *  number is out of the width of the port.
     * @exception CorbaIllegalValueException If the value is not
     *  valid, e.g. the string cannot be converted to a value.
     */
    void transferInput (String portName, short portIndex, String tokenValue) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException, ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException;

    /* Transfer the output from an output port.
     * @param portName The port name
     * @param portIndex The channel index within the port.
     * @exception CorbaIllegalActionException If the operation
     *  is illegal, e.g. the port is not an output port.
     * @exception CorbaUnknownPortException If the port name
     *  is unknown.
     * @exception CorbaIndexOutofBoundException If the index
     *  number is out of the width of the port.
     */
    String transferOutput (String portName, short portIndex) throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException, ptolemy.domains.ct.demo.Corba.util.CorbaUnknownPortException, ptolemy.domains.ct.demo.Corba.util.CorbaIndexOutofBoundException;

    /* Mirror the wrapup() method of the Ptolmey
     * executable interface.
     * @exception CorbaIllegalActionException If the
     *   method is an illegal action of the actor.
     */
    void wrapup () throws ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException;
} // interface CorbaActorOperations
