/* An opaque composite actor that models a processor with interrupt processing.

 Copyright (c) 1998 The Regents of the University of California.
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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEProcessor
/**
This opaque composite actor contains an instance of InterruptibleServer and
DEPoisson. The DEPoisson actor is used to model the arrival of interrupts,
which delays the service time of the InterruptibleServer actor.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEProcessor extends TypedCompositeActor {

    /** Construct a DEProcessor actor with the default parameters.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEProcessor(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, 1.0, 0.5, 0.5);
    }

    /** Construct a DEProcessor actor with the specified parameters.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *  @param minimumServiceTime The minimum service time.
     *  @param interruptServiceTime The interrupt service time.
     *  @param lambda The mean interarrival time of the interrupt.
     *    adder.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEProcessor(TypedCompositeActor container,
            String name,
            double minimumServiceTime,
            double interruptServiceTime,
            double lambda)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create an output port
        output = new TypedIOPort(this, "output", false, true);

        // create input ports
        input = new TypedIOPort(this, "input", true, false);

        // create and attach a local director
        DECQDirector localDir = new DECQDirector(name + " local director");
        this.setDirector(localDir);
        
        // create the actors.
        InterruptibleServer iServer = new InterruptibleServer(this, 
                "InterruptibleServer", 
                minimumServiceTime, 
                interruptServiceTime);
        DEPoisson poisson = new DEPoisson(this, "InterruptPoisson", new Token(), lambda);

        // connect the actors
        this.connect(input, iServer.input);
        this.connect(poisson.output, iServer.interrupt);
        this.connect(iServer.output, output);

        // Set up the parameters.
        _minimumServiceTime = (Parameter)iServer.getAttribute("Minimum Service Time");
        _interruptServiceTime = (Parameter)iServer.getAttribute("Interrupt Service Time");
        _lambda = (Parameter)poisson.getAttribute("lambda");
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the ports.
    public TypedIOPort input;
    public TypedIOPort output;

    private Parameter _minimumServiceTime;
    private Parameter _interruptServiceTime;
    private Parameter _lambda;

}






