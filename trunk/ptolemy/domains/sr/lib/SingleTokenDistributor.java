/* A distributor that processes a single token per iteration.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//import ptolemy.kernel.*;
//import ptolemy.kernel.util.*;
//import ptolemy.data.*;
//import ptolemy.data.expr.*;
//import ptolemy.actor.*;
//import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SingleTokenDistributor
/**
A distributor, which splits an input stream into a set of
output streams. The distributor has an input port and an output port,
the latter of which is a multiport.
The types of the ports are undeclared and will be resolved by the type
resolution mechanism, with the constraint that the output type must be
greater than or equal to the input type. On each call to the fire method, the
actor reads at most one token from the input, and writes one token to an 
output channel.  In the next iteration of this actor, it will produce an
output on the next channel.

@author Paul Whitaker, Mudit Goel, Edward A. Lee
@version $Id$
*/
public class SingleTokenDistributor extends Transformer
    implements SequenceActor {

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport. Create
     *  the actor parameters.
     *
     *  @param container The container.
     *  @param name This is the name of this distributor within the container.
     *  @exception NameDuplicationException If an actor
     *  with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *  by the proposed container.
     */
    public SingleTokenDistributor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from the input port, and write that token
     *  to the current output channel.  If there is no token on the input,
     *  do nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _tentativeOutputPosition = _currentOutputPosition;
        int width = output.getWidth();
        if (input.hasToken(0)) {
            output.send(_tentativeOutputPosition++, input.get(0));
            if (_tentativeOutputPosition >= width) {
                _tentativeOutputPosition = 0;
            }
        }
    }

    /** Begin execution by setting the current output channel to zero.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentOutputPosition = 0;
    }

    /** Update the output position to equal that determined by the most
     *  recent invocation of the fire() method.  The output position is
     *  the channel number of the output port to which the next input
     *  will be sent.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _currentOutputPosition = _tentativeOutputPosition;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The channel number for the next output.
    private int _currentOutputPosition;

    // The new channel number for the next output as determined by fire().
    private int _tentativeOutputPosition;
}
