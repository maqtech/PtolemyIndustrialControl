/* An actor that samples the data input when the clock input arrives.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DESampler
/**
This actor samples the data input at the times given by events on the clock
input. The values of the clock input events are ignored. If no data input is
available at the time of sampling, the most recently seen data input is used.
If there has been no data input, then a "zero" token is produced. The exact
meaning of zero depends on the token type. The "zero" token is a parameter,
and can be set to null. If it is null, then no token is produced during
samplings before any input datas.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DESampler extends DEActor {

    /** Construct a DESampler actor.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DESampler(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, null);
        
    }

    /** Construct a DESampler actor with the specified initial token.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DESampler(TypedCompositeActor container,
            String name,
            Token initToken)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // set up parameter
        if (initToken != null) {
            _initToken = new Parameter(this, "InitToken", initToken);
        } else {
            _initToken = null;
        }

        // create an output port
        output = new DEIOPort(this, "output", false, true);
        // create input ports
        input = new DEIOPort(this, "dataInput", true, false);

        clock = new DEIOPort(this, "clockInput", true, false);
        clock.setTypeEquals(Token.class);
        clock.triggers(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the clock input port then produce an event,
     *  otherwise just record the value of the input port.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{

        // Get the receivers.
        DEReceiver clockR = (DEReceiver)(clock.getReceivers())[0][0];
        DEReceiver inputR = (DEReceiver)(input.getReceivers())[0][0];

        // Check if there's an event in the clock input port.
        if (clockR.hasToken()) {
            Token clockToken = null;
            clockToken = clock.get(0);
            // If the input also has token then update _lastToken.

            if (inputR.hasToken()) {
                _lastTokens.clear();
            }

            while (inputR.hasToken()) {
                _lastTokens.insertLast(input.get(0));
            }

            // send the output token via the output port.
            Enumeration enum = _lastTokens.elements();
            while (enum.hasMoreElements()) {
                output.broadcast((Token)enum.nextElement());
            }
        } else if (inputR.hasToken()) {
            // Record the token from the input.
            
            _lastTokens.clear();

            while (inputR.hasToken()) {
                _lastTokens.insertLast(input.get(0));
            }
        } else {
            // if both inputs are empty, then the scheduler is wrong.
            throw new InvalidStateException("DESampler.fire(), "+
                    "bad scheduling");
        }

    }
    
    /** Reset the initial token.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastTokens.clear();
        if (_initToken != null) {
            _lastTokens.insertLast(_initToken.getToken());
        }
    }

    /** Return the type constraints of this actor.
     *  This method is read-synchronized on the workspace.
     *  @return an Enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    /*
    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

            LinkedList result = new LinkedList();
            Inequality constraint = new Inequality(input.getTypeTerm(),output.getTypeTerm());
            result.insertLast(constraint);
            return result.elements();

        } finally {
	    workspace().doneReading();
	}
    }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the last token seen in the input port.
    private LinkedList _lastTokens = new LinkedList();

    // Initial token.
    Parameter _initToken = null;

    // the ports.
    public DEIOPort output;
    public DEIOPort input;
    public DEIOPort clock;
}
