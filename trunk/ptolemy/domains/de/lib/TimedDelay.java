/* An actor that delays the input by the specified amount.

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

package ptolemy.domains.de.lib;

import java.util.HashMap;

import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TimedDelay
/**
   This actor delays the input by a specified amount of time.
   The time delay is required to be non-negative and has default value 1.0.
   The input and output types are unconstrained, except that the
   output type must be at least that of the input.
   <p>
   The behavior on each firing is to read a token from the input,
   if there is one, and to produce the token on the corresponding output
   channel with the appropriate time delay.  The output is produced
   in the postfire() method, consistent with the notion that persistent
   state is only updated in postfire().  Notice that it produces
   the output immediately, in the same iteration that it reads the
   input, so that even if the actor no longer exists
   after the time delay elapses, the destination actor will still see
   the token. If there is no input token, then no output token is
   produced.
   <p>
   Occasionally, it is useful to set the time
   delay to zero.  This causes the input tokens to be produced on
   the output immediately.  However, since the actor declares that
   there is a delay between the input and the output, the DE director
   will assume there is a delay when determining the precedences of the
   actors.  Moreover, the event is queued to be processed in the next
   microstep, after all events at the current time with the current
   microstep. Thus, it is sometimes useful to think of this zero-valued
   delay as an infinitesimal delay.

   @see ptolemy.actor.util.FunctionDependencyOfAtomicActor
   @see ptolemy.domains.de.lib.VariableDelay
   @see ptolemy.domains.de.lib.Server
   @see ptolemy.domains.sdf.lib.SampleDelay

   @author Edward A. Lee, Lukito Muliadi, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class TimedDelay extends DETransformer {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        delay = new Parameter(this, "delay", new DoubleToken(1.0));
        delay.setTypeEquals(BaseType.DOUBLE);
        
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay.  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then check that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay) {
            if (((DoubleToken)(delay.getToken())).doubleValue() < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative delay: "
                        + ((DoubleToken)(delay.getToken())));
            } else {
                _delay = ((DoubleToken)delay.getToken()).doubleValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the delayTo relation between the input
     *  and the output of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TimedDelay newObject = (TimedDelay)super.clone(workspace);
        return newObject;
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce it to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
        } else {
            _currentInput = null;
        }
        double currentTime = getDirector().getCurrentTime();
        _eventToBeRemoved = null;
        if (_delayedEvents.size() > 0) {
            Double timeDouble = new Double(currentTime);
            _eventToBeRemoved = (DEEvent)_delayedEvents.get(timeDouble);
            if (_eventToBeRemoved != null) {
                output.send(0, _eventToBeRemoved.token());
            }
        }
        if (_delay == 0) {
            output.send(0, _currentInput);
        }
    }

    /** Initialize the states of this actor.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _delay = ((DoubleToken)delay.getToken()).doubleValue();
        _eventToBeRemoved = null;
        _delayedEvents = new HashMap();
    }

    /** Produce token that was read in the fire() method, if there
     *  was one.
     *  The output is produced with a time offset equal to the value
     *  of the delay parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        double currentTime = getDirector().getCurrentTime();
        if (_delayedEvents.size() > 0 && 
            _eventToBeRemoved != null) {
            _delayedEvents.remove(new Double(currentTime));
        }
        if (_currentInput != null && _delay > 0) {
            Double timeDouble = new Double(_delay + currentTime);
            _delayedEvents.put(timeDouble, 
                new DEEvent(this, _currentInput, _delay + currentTime));
            getDirector().fireAt(this, _delay + currentTime);
        }
        return super.postfire();
    }


    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current input.
    // FIXME: this private variable is not necessary.
    private Token _currentInput;
    
    // A new DEFIFOEventQueue may be constructed for this purpose.
    private DEEvent _eventToBeRemoved;
    private HashMap _delayedEvents;
    
    // delay value;
    private double _delay;
}
