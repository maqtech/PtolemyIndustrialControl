/* An actor that outputs a random sequence with a BreitWigner distribution.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.actor.lib.colt;

import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.data.StringToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import cern.jet.random.BreitWigner;
import cern.jet.random.engine.DRand;

//////////////////////////////////////////////////////////////////////////
//// BreitWigner
/**
   Produce a random sequence with a BreitWigner distribution.  On each
   iteration, a new random number is produced.  The output port is of
   type DoubleToken.  The values that are generated are independent
   and identically distributed with the mean and the standard
   deviation given by parameters.  In addition, the seed can be
   specified as a parameter to control the sequence that is generated.

   @author David Bauer and Kostas Oikonomou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/

public class ColtBreitWigner extends ColtRandomSource {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColtBreitWigner(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        output.setTypeEquals(BaseType.DOUBLE);

        coltMean = new Parameter(this, "mean", new DoubleToken(1.0));
        coltMean.setTypeEquals(BaseType.DOUBLE);

        coltGamma = new Parameter(this, "gamma", new DoubleToken(1.0));
        coltGamma.setTypeEquals(BaseType.DOUBLE);

        coltCut = new Parameter(this, "cut", new DoubleToken(1.0));
        coltCut.setTypeEquals(BaseType.DOUBLE);

        randomNumberGeneratorClass = getRandomNumberGeneratorClass(container);

        rng = new BreitWigner(1.0, 1.0, 1.0, randomNumberGenerator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** coltMean.
     *  This parameter contains a DoubleToken, initially with value 1.0.
     */
    public Parameter coltMean;

    /** coltGamma.
     *  This parameter contains a DoubleToken, initially with value 1.0.
     */
    public Parameter coltGamma;

    /** coltCut.
     *  This parameter contains a DoubleToken, initially with value 1.0.
     */
    public Parameter coltCut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a random number with a BreitWigner distribution to the output.
     *  This number is only changed in the prefire() method, so it will
     *  remain constant throughout an iteration.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new DoubleToken(_current));
    }

    /** Calculate the next random number.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {

        double mean = ((DoubleToken) coltMean.getToken()).doubleValue();
        double gamma = ((DoubleToken) coltGamma.getToken()).doubleValue();
        double cut = ((DoubleToken) coltCut.getToken()).doubleValue();

        _current = ((BreitWigner) rng).nextDouble(mean, gamma, cut);

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The random number for the current iteration.
    private double _current;
}
