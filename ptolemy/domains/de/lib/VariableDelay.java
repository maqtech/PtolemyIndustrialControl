/* An actor that delays the input by the amount spacified through another port.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedIOPort;

//////////////////////////////////////////////////////////////////////////
//// VariableDelay
/**
This actor works exactly as the ptolemy.domains.de.lib.Delay actor,
except that the amount of time delayed is specified by a incoming
token throught the delay port, in stead of a parameter

@see ptolemy.domains.de.lib.Delay
@author Jie Liu
@version $Id$
*/
public class VariableDelay extends DETransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        defaultDelay = new Parameter(this,
                "defaultDelay", new DoubleToken(1.0));
        defaultDelay.setTypeEquals(BaseType.DOUBLE);
        input.delayTo(output);
        delay = new TypedIOPort(this, "delay", true, false);
        delay.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of default delay.  This value is used if no token
     *  has ever received from the delay port. The default is 0.0.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter defaultDelay;

    /** The input port for specifing delay.
     */
    public TypedIOPort delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then check that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == defaultDelay) {
            double newValue =
                ((DoubleToken)(defaultDelay.getToken())).doubleValue();
            if ( newValue < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative delay.");
            } else {
                //System.out.println(getName() + "set delay to " + newValue);
                _delay = newValue;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        VariableDelay newObject = (VariableDelay)super.clone(workspace);
        try {
            newObject.input.delayTo(newObject.output);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Clone failed.");
        }
        return newObject;
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce it to the output.
     *  Read one token from the delay input, if there is any.
     *  Use the new delay for later input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
        } else {
            _currentInput = null;
        }
        if (delay.hasToken(0)) {
            //System.out.println(getName() + "set delay to " + _delay);
            _delay = ((DoubleToken)delay.get(0)).doubleValue();
        }
    }

    /** Produce token that was read in the fire() method, if there
     *  was one.
     *  The output is produced with a time offset equal to the value
     *  of the delay parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (_currentInput != null) {
            output.send(0, _currentInput, _delay);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current inputs.
    private Token _currentInput;

    // delay value;
    private double _delay;
}
