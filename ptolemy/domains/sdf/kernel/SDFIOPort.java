/* IOPort for SDF

 Copyright (c) 1997-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.sdf.kernel;

import ptolemy.actor.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;

import java.lang.reflect.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SDFIOPort
/**
This class extends IOPort to allow the transmission of arrays of tokens.
There is no added expressive power in allowing this, and sending an array of
tokens is exactly equivalent to sending each token individually.  Tokens
that are sent using sendArray can be received using get and vice versa.
<p>
This class exists solely as an optimization.  There are two large pieces
of overhead in sending a large number of tokens using the standard IOPort
implementation.  We list them below, and the steps that this class takes to
reduce them.
<ol>
<li>Finding the remote receivers. <br>
getArray() and sendArray() only have to get the receivers once.
<li>Inserting each element into the QueueReceiver.<br>
SDFIOPort creates SDFReceivers, which are based on ArrayFIFOQueue, instead
of FIFOQueue.  ArrayFIFOQueue uses a circular array instead of a linkedlist to
represent the queue.  ArrayFIFOQueue also optimizes insertion and removal of
arrays of objects using the java.lang.System.arraycopy() method.
</ol>

@authors Stephen Neuendorffer
@version $Id$
@see SDFReceiver
@see ArrayFIFOQueue
*/
public final class SDFIOPort extends TypedIOPort {

    /** Construct an SDFIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public SDFIOPort() {
        super();
	_initialize();
    }

    /** Construct an SDFIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SDFIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
	_initialize();
    }

    /** Construct an SDFIOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isinput True if this is to be an input port.
     *  @param isoutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SDFIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
	setInput(isinput);
        setOutput(isoutput);
    }

    public Parameter tokenConsumptionRate;
    public Parameter tokenInitProduction;
    public Parameter tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the port into the specified workspace. This calls the
     *  base class and then creates new parameters.  The new
     *  port will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new SDFIOPort.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        SDFIOPort newobj = (SDFIOPort)(super.clone(ws));
        newobj.tokenConsumptionRate =
            (Parameter)newobj.getAttribute("tokenConsumptionRate");
        newobj.tokenInitProduction =
            (Parameter)newobj.getAttribute("tokenInitProduction");
        newobj.tokenProductionRate =
            (Parameter)newobj.getAttribute("tokenProductionRate");
        return newobj;
    }

    /** Get the number of tokens that are consumed
     *  on every channel of this port.
     *
     *  @return The number of tokens consumed on this port, as specified in
     *  the tokenConsumptionRate Parameter.
     */
    public int getTokenConsumptionRate() throws IllegalActionException {
	return ((IntToken)tokenConsumptionRate.getToken()).intValue();
    }

    /** Get the number of tokens that are produced
     *  on this port during initialization.
     *
     *  @return The number of tokens produced on the port, as specified in
     *  the tokenInitProduction parameter.
     */
    public int getTokenInitProduction() throws IllegalActionException {
	return ((IntToken)tokenInitProduction.getToken()).intValue();
    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during each firing.
     *
     *  @return The number of tokens produced on the port, as specified in
     *  the tokenProductionRate parameter.
     */
    public int getTokenProductionRate() throws IllegalActionException {
	return ((IntToken)tokenProductionRate.getToken()).intValue();
    }

    /**
     * Set whether or not this port is an input.  In addition to the base
     * class operation, set the port rate parameters to reasonable values.
     * If setting the port to be an input, then set the consumption rate to
     * be 1.  If setting the port to not be an input, then set the consumption
     * rate to be 0.
     */
    public void setInput(boolean isInput) {
	super.setInput(isInput);
	try {
	    if(isInput) {
		tokenConsumptionRate.setToken(new IntToken(1));
	    } else {
		tokenConsumptionRate.setToken(new IntToken(0));
	    }
	} catch (Exception e) {
	    // This should never happen
	    throw new InternalErrorException(e.getMessage());
	}
    }

    /**
     * Set whether or not this port is an output.  In addition to the base
     * class operation, set the port rate parameters to reasonable values.
     * If setting the port to be an output, then set the consumption rate to
     * be 1.  If setting the port to not be an output, then set the consumption
     * rate to be 0.
     */
    public void setOutput(boolean isOutput) {
	super.setOutput(isOutput);
	try {
	    if(isOutput) {
		tokenProductionRate.setToken(new IntToken(1));
	    } else {
		tokenProductionRate.setToken(new IntToken(0));
		tokenInitProduction.setToken(new IntToken(0));
	    }
	} catch (Exception e) {
	    // This should never happen.
	    throw new InternalErrorException(e.getMessage());
	}
    }

    /** Set the number of tokens that are consumed
     *  on the appropriate port of this Actor during each firing
     *  by setting the value of the tokenConsumptionRate parameter.
     *
     *  @exception IllegalActionException If the rate is less than zero,
     *  or the port is not an input port.
     */
    public void setTokenConsumptionRate(int rate)
            throws IllegalActionException {
        if(rate < 0) throw new IllegalActionException(
                "Rate must be >= 0");
        if(!isInput()) throw new IllegalActionException(this, "Port " +
                "is not an input port.");
	tokenConsumptionRate.setToken(new IntToken(rate));
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during initialize
     *  by setting the value of the tokenInitProduction parameter.
     *
     *  @exception IllegalActionException If the count is less than zero,
     *  or the port is not an output port.
     */
    public void setTokenInitProduction(int count)
            throws IllegalActionException {
        if(count < 0) throw new IllegalActionException(
                "Count must be >= 0");
        if(!isOutput()) throw new IllegalActionException(this, "Port " +
                "is not an Output Port.");
	tokenInitProduction.setToken(new IntToken(count));
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during each firing
     *  by setting the value of the tokenProductionRate parameter.
     *
     *  @exception IllegalActionException If port is not contained
     *  in this actor, the rate is less than zero, or the port is
     *  not an output port.
     */
    public void setTokenProductionRate(int rate)
            throws IllegalActionException {
        if(rate <= 0) throw new IllegalActionException(
                "Rate must be > 0");
        if(!isOutput()) throw new IllegalActionException(this, "Port " +
                "is not an Output Port.");
	tokenProductionRate.setToken(new IntToken(rate));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Return the convert() Method for the resolved type of this port.
     */
    private Method _getConvertMethod(Class dataType) {
	Method _convertMethod = null;
	try {
	    if (_convertMethod == null) {
	    	Class[] formal = new Class[1];
	    	formal[0] = Token.class;
	    	_convertMethod = dataType.getMethod("convert", formal);
	    }
	    return _convertMethod;

	} catch (NoSuchMethodException e) {
            throw new InternalErrorException("_getConvertMethod: "
                    + "NoSuchMethodException: " + e.getMessage());
        }
    }

    /**
     * Initialize local data members.
     */
    private void _initialize() {
	try {
	    tokenConsumptionRate = new Parameter(this, "tokenConsumptionRate",
                    new IntToken(0));
	    tokenInitProduction = new Parameter(this, "tokenInitProduction",
                    new IntToken(0));
	    tokenProductionRate = new Parameter(this, "tokenProductionRate",
                    new IntToken(0));
	}
	catch (Exception e) {
	    // This should never happen.
	    throw new InternalErrorException(e.getMessage());
	}
    }
}
