/* A Wire with one trigger port that accepts read requests.

 Copyright (c) 2006 The Regents of the University of California.
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

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Wire

/**
 A Wire is a stateful actor in DE.  It should have an equal number 
 of input and output channels.  If it recieves input on <i>any</i> of 
 its channels, it should produce the most recent recieved on each
 input channel to the corresponding output channel for <i>all</i>
 channels. We can think its behavior similar to a wire in VHDL, where 
 the value is always the most recently recieved.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (acataldo)
 @Pt.AcceptedRating Red (acataldo)
 @see ptolemy.domains.de.lib.Wire
 */
public class Wire extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Wire(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);

        initialValue = new Parameter(this, "initialValue");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"2\" "
                + "style=\"fill:black\"/>\n" + "<rect x=\"-20\" y=\"-10\" "
                + "width=\"40\" height=\"2\" " + "style=\"fill:black\"/>\n"
                + "<rect x=\"-20\" y=\"0\" " + "width=\"40\" height=\"2\" "
                + "style=\"fill:black\"/>\n" + "<rect x=\"-20\" y=\"10\" "
                + "width=\"40\" height=\"2\" " + "style=\"fill:black\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token in on any channel of the <i>input</i> port,
     *  output the most recent value on all the <i>output</i> port
     *  channels.
     *  @exception IllegalActionException If the base class throws one.
     */
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();", this actor extends another actor.
        int inputWidth = input.getWidth();
        int outputWidth = output.getWidth();
        int commonWidth = Math.min(inputWidth, outputWidth);

        // If the <i>initialValue</i> parameter was not set, or if the
        // width of the input has changed.
        if ((_lastInputs == null) || (_lastInputs.length != inputWidth)) {
            _lastInputs = new Token[inputWidth];
        }

        for (int i = 0; i < commonWidth; i++) {
            if (input.hasToken(i)) {
                _lastInputs[i] = input.get(i);
            }
            output.send(i, _lastInputs[i]);
        }
    }

    /** Return true if there is any token on an input port.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        boolean writeRequest = false;
        int inputWidth = input.getWidth();

        for (int i = 0; i < inputWidth; i++) {
            writeRequest = writeRequest || input.hasToken(i);
        }

        return writeRequest || super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value that is output when no input has yet been received.
     *  The type should be the same as the input port.
     *  @see #typeConstraintList()
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The recorded inputs last seen. */
    protected Token[] _lastInputs;
}
