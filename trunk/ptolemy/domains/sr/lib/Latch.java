/* An actor that outputs the most recent input received.

 Copyright (c) 1997-2004 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu) Should support multiports
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Latch
/**
This actor implements a latch.  It has one input port and
one output port, both of which are single ports.  A token that is received
on the input port is sent to the output port immediately, and also every time
the actor is fired until a new token is received.  No tokens are output until
the first token is received at the input.

<p>Note that this actor is not really a latch in the classical sense
of the term since it is missing a trigger.  This actor will not
be of much use in the DE domain, see the DE Sampler actor for
an alternative.  This actor is useful in domains with non-strict semantics
like SR and Giotto.

<p>FIXME: This actor should be modified to handle multiports, but under SR
a multiport version of this actor hangs in
$PTII/ptolemy/actor/lib/test/auto/Latch.xml

@see ptolemy.domains.de.lib.Sampler
@author Paul Whitaker, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/

public class Latch extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Latch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token on the input port, consume exactly one token
     *  from the input port, and output this token.  If there is no token
     *  on the input port, output the most recent token received, if any.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _lastInputs = input.get(0);
        }

        if (_lastInputs != null) {
            output.send(0, _lastInputs);
        }
    }

    /** Initialize the buffer variable.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        _lastInputs = null;
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The most recent token received.
    private Token _lastInputs;
}
