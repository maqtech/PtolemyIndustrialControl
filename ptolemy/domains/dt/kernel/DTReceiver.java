/* Discrete Time (DT) domain receiver.

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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.Token;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.util.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.lib.Delay;

import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.*;

//////////////////////////////////////////////////////////////////////////
//// DTReceiver
/**
A first-in, first-out (FIFO) queue receiver with variable capacity. Tokens
are put into the receiver with the put() method, and removed from the
receiver with the get() method. The token removed is the oldest one in
the receiver.  Time is incremented by a fixed amount <i>deltaT</i> everytime
the get() method is called. Each receiver has its own value of deltaT.
We calculate deltaT as "period / (rate * repeats)" where:
<UL>
    <LI> period is the execution time of the director per iteration
    <LI> rate   is the rate of the port that holds this receiver
    <LI> repeats is the firing count per iteration of the actor
              that holds this receiver
</UL>
@author C. Fong
@version $Id$
*/
public class DTReceiver extends SDFReceiver implements Receiver {

    /** Construct an empty receiver with no container.
     */
    public DTReceiver() {
        super();
        _init();
    }

    /** Construct an empty receiver with no container and given size.
     *  @param size The size of the buffer for the receiver.
     */
    public DTReceiver(int size) {
        super(size);
        _init();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     */
    public DTReceiver(IOPort container) {
        super(container);
        _init();
    }

    /** Construct an empty receiver with the specified container and size.
     *  @param container The container of the receiver.
     *  @param size  The size of the buffer for the receiver.
     */
    public DTReceiver(IOPort container, int size) {
        super(container,size);
        _init();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calculate the constant time increment for this receiver. This method
     *  should only be invoked after the preinitialize() stage of the director.
     *  Prior to that, certain information about the SDF dataflow graph
     *  topology is not yet available.
     *  @exception IllegalActionException If there is an error in
     *  getting attribute information from the ports.
     */
    public void calculateDeltaT() throws IllegalActionException {
        int repeats;
        double periodValue;
        boolean isCompositeContainer = !((ComponentEntity) _to).isAtomic();

    	if (_from == null)  {
    	    throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
    	} else {

    	    Parameter param = (Parameter)
                _fromPort.getAttribute("tokenProductionRate");
    	    if(param == null) {
                _outrate = 1;
            } else {
                _outrate = ((IntToken)param.getToken()).intValue();
            }

            if ((isCompositeContainer) && (_toPort.isOutput())) {
                _inrate = 1;
            } else {
                param = (Parameter) _toPort.getAttribute("tokenConsumptionRate");
    	        if(param == null) {
                    _inrate = 1;
                } else {
                    _inrate = ((IntToken)param.getToken()).intValue();
                }
            }

            if (_toPort.isOutput()) {

            } else {
            }

            // FIXME: check tunneling topology
            periodValue = _localDirector.getPeriod();
            if (_toPort.isOutput()) {
                repeats = _localDirector.getRepetitions(_from);
                _periodDivider = repeats * _outrate;
                _deltaT = periodValue / _periodDivider;
            } else {
                repeats = _localDirector.getRepetitions(_to);
                _periodDivider = repeats * _inrate;
            	_deltaT = periodValue / _periodDivider;

            }
        }
    }


    /** Determine the source and destination ports that use this
     *  receiver in their communications.  The source and destination
     *  ports are distinct for each receiver.
     *  @param dtDirector The director that directs this receiver
     */
    public void determineEnds(DTDirector dtDirector) {
        _toPort = this.getContainer();
    	_to = (Actor) _toPort.getContainer();
    	_fromPort = null;
        IOPort connectedPort = null;
        List listOfConnectedPorts = null;
        boolean isCompositeContainer = !((ComponentEntity) _to).isAtomic();

    	_localDirector = dtDirector;

    	if (isCompositeContainer && (_toPort.isOutput()) ) {
    	    listOfConnectedPorts = _toPort.insidePortList();
    	} else {
    	    listOfConnectedPorts = _toPort.connectedPortList();
    	}

    	Iterator portListIterator = listOfConnectedPorts.iterator();

    foundReceiver:
    	while (portListIterator.hasNext()) {
    	    connectedPort = (IOPort) portListIterator.next();

    	    if (connectedPort.isOutput() == true) {
                Receiver[][] remoteReceivers = connectedPort.getRemoteReceivers();

                for(int i=0;i<connectedPort.getWidth();i++) {
                    for(int j=0;j<remoteReceivers[i].length;j++) {
                        if (remoteReceivers[i][j] == this) {
                            _from = (Actor) connectedPort.getContainer();
                            _fromPort = connectedPort;
                            if (_fromPort == null) {
                                throw new InternalErrorException(
                                        "internal DT error: Receiver with null source");
                            }
                            break foundReceiver;
                        }
                    }
                }
    	    } else if (connectedPort.getContainer() instanceof TypedCompositeActor) {
    	        // FIXME: should use at isAtomic() insteadof instanceof?
    	        _from = (Actor) connectedPort.getContainer();
    	        _fromPort = connectedPort;
    	        if (_fromPort == null) {
    	            throw new InternalErrorException(
                            "internal DT error: Receiver with null source");
    	        }
    	        break foundReceiver;
    	    } else if (connectedPort.isInput() == true) {
                // This case occurs when the destination port and
                // the queried connected port are both inputs.
                // This case should be ignored.
    	    }
    	}

    	if (_fromPort == null) {
    	    throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
    	}
    }


    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.  Increment the local time by deltaT.
     *  @return The oldest token in the receiver.
     */
    public Token get() {

        Actor actor = (Actor) super.getContainer().getContainer();
        IOPort currentPort = (IOPort) super.getContainer();
        Director director = ((Actor) actor).getDirector();

        // FIXME: need to consider different cases for
        // TypedComposositeActor ports
        try {
            director.setCurrentTime(_localTime);
        } catch (Exception e) {
            // FIXME: process exception
        }

        String sourceName = ((Nameable) _to).getName();
        String destinationName = ((Nameable) _from).getName();

        // FIXME: timing has bugs for DT inside DT
        _localTime = _localTime + _deltaT;
        return super.get();
    }



    /** Return the port that feeds this Receiver
     *  @return The port that feeds this receiver.
     */
    public TypedIOPort getSourcePort() {
        return (TypedIOPort) _fromPort;
    }

    public double getDeltaT() {
        return _deltaT;
    }

    public int getTokenFlowRate() {
        return _periodDivider;
    }


    /** Put a token to the receiver. If the port feeding this
     *  receiver is null, report an internal error.
     *  @param token The token to be put to the receiver.
     *  @exception InternalErrorException If the source port is null.
     */
    public void put(Token token) {
        if (_fromPort == null) {
            throw new InternalErrorException(
                    "internal DT error: Receiver with null source");
        }
        super.put(token);
    }

    /** Return true if get() will succeed in returning a token.
     *  @return A boolean indicating whether there is a token in this
     *  receiver.
     */
    public boolean hasToken() {
        if (overrideHasToken == true) {
            debug.println("disable hasToken");
            return false;
        } else {
            debug.println("enabled hasToken");
            return super.hasToken();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                  package-access methods                   ////

    /**  For debugging purposes. Display pertinent information about
     *   this receiver.
     */
    void displayReceiverInfo() {
        String fromString;
        String toString;

        if (_from == null) {
            fromString="0";
        } else {
            fromString=((Nameable) _from).getName();
        }

        fromString += " (" + ((TypedIOPort)_fromPort).getType() + ")";

        if (_to == null) {
            toString="0";
        } else {
            toString=((Nameable) _to).getName();
        }

        toString += " (" + ((TypedIOPort)_toPort).getType() + ")";

        debug.println(fromString+" "+toString+" "+_deltaT);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the DTReceiver.  Set the cached information regarding
     *  source and destination actors to null.  Set the local time to
     *  zero.  Set deltaT to zero.
     */
    private void _init() {
        _from = null;
        _to   = null;
        _localTime = 0.0;
        _periodDivider = 0;
        _deltaT = 0.0;
        overrideHasToken = false;
        debug = new DTDebug(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package-access variables                 ////

    // override the value of hasToken() given by SDFReceiver
    // This variable is used in mixed-hierarchical DT
    boolean overrideHasToken;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director that directs this receiver; should be a DTDirector
    private DTDirector _localDirector;

    // The dividing factor to the discrete time period
    private int _periodDivider;

    // The amount of time increment for every get() method call
    private double _deltaT;

    // The local cached time
    private double _localTime;

    // The cached value of the destination token consumption rate
    private int _inrate;

    // The cached value of the source token production rate
    private int _outrate;

    // The actor feeding this receiver
    private Actor _from;

    // The actor containing this receiver
    private Actor _to;

    // The port feeding this receiver
    private IOPort _fromPort;

    // The port containing this receiver
    private IOPort _toPort;

    // display for debugging purposes
    private DTDebug debug;
}
