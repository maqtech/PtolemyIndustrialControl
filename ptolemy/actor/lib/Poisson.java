/* A Poisson process source.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.graph.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Poisson
/**
This actor produces a signal that is piecewise constant, with transitions
between levels taken at times given by a Poisson process.
It has various uses.  Its simplest use in the DE domain
is to generate a sequence of events at intervals that are spaced
randomly, according to an exponential distribution.
In CT, it can be used to generate a piecewise constant waveform
with randomly spaced transition times.
In both domains, the output value can cycle through a set of values.
<p>
The mean time between events is given by the <i>meanTime</i> parameter.
An <i>event</i> is defined to be the transition to a new output value.
The default mean time is 1.0.
<p>
The <i>values</i> parameter must contain a row vector
(a 1 by <i>N</i> matrix), or an
exception will be thrown when it is set.
By default it
contains an IntMatrix with value [1, 0] (one row,
two columns, with values 1 and 0). Thus, the default output
value is always 1 or 0.
<p>
In the initialize() method and in each invocation of the fire() method,
the actor uses the fireAt() method of the director to request
the next firing.  The first firing is always at time zero.
It may in addition fire at any time in response to a trigger
input.  On such firings, it simply repeats the most recent output
(or generate a new output if the time is suitable.)
Thus, the trigger, in effect, asks the actor what its current
output value is. Some directors, such as those in CT, may also fire the 
actor at other times, without requiring a trigger input.  Again, the actor
simply repeats the previous output.
Thus, the output can be viewed as samples of the piecewise
constant waveform,
where the time of each sample is the time of the firing that
produced it.
<p>
The type of the output can be any token type that has a corresponding
matrix token type.  The type is inferred from the type of the
<i>values</i> parameter.

@author Edward A. Lee
@version $Id$
*/

public class Poisson extends TimedSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Poisson(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        meanTime = new Parameter(this, "meanTime", new DoubleToken(1.0));
        meanTime.setTypeEquals(BaseType.DOUBLE);

        int defaultValues[][] = {{1, 0}};
        IntMatrixToken defaultValueToken = new IntMatrixToken(defaultValues);
        values = new Parameter(this, "values", defaultValueToken);
        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The mean time between events, where the output value transitions.
     *  This parameter must contain a DoubleToken.
     */
    public Parameter meanTime;

    /** The values that will be produced at the output.
     *  This parameter can contain any MatrixToken.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the meanTime parameter, check that it is
     *  positive.
     *  @exception IllegalActionException If the meanTime value is
     *   not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == meanTime) {
            double mean = ((DoubleToken)meanTime.getToken()).doubleValue();
            if (mean <= 0.0) {
                throw new IllegalActionException(this,
                "meanTime is required to be positive.  meanTime given: "
                + mean);
            }
        } else if (attribute == values) {
            MatrixToken val = (MatrixToken)(values.getToken());
            _length = val.getColumnCount();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If the parameter being changed is <i>values</i>,
     *  notify the director that type resolution may be invalid,
     *  and update the type constraints on the output.
     *  This will cause type resolution to be redone when it is next needed.
     *  It is assumed that type changes in the parameters are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     *  @exception IllegalActionException If the new values array has no
     *   elements in it, or if it is not a MatrixToken, or if the parent
     *   class throws it.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {
            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateResolvedTypes();
            }
        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Poisson newobj = (Poisson)super.clone(ws);
        try {
            newobj.meanTime = (Parameter)newobj.getAttribute("meanTime");
            newobj.values = (Parameter)newobj.getAttribute("values");
            newobj.attributeChanged(values);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return newobj;
    }

    /** Output the current value.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Get the current time and period.
        double currentTime = getDirector().getCurrentTime();

        // Indicator whether we've reached the next event.
        _boundaryCrossed = false;

        _tentativeCurrentOutputIndex = _currentOutputIndex;

        // In case current time has reached or crossed a boundary to the
        // next output, update it.
        if (currentTime >= _nextFiringTime) {
            _tentativeCurrentOutputIndex++;
            if (_tentativeCurrentOutputIndex >= _length) {
                _tentativeCurrentOutputIndex = 0;
            }
            _boundaryCrossed = true;
        }
        output.send(0, _getValue(_tentativeCurrentOutputIndex));
    }

    /** Schedule the first firing at time zero and initialize local variables.
     *  @exception IllegalActionException If the fireAt() method of the
     *   director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _tentativeCurrentOutputIndex = 0;
        double currentTime = getDirector().getCurrentTime();
        _nextFiringTime = currentTime;
        getDirector().fireAt(this, currentTime);
    }

    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException {
        _currentOutputIndex = _tentativeCurrentOutputIndex;
        if (_boundaryCrossed) {
            double meanTimeValue =
                    ((DoubleToken)meanTime.getToken()).doubleValue();
            double exp = -Math.log((1-Math.random()))*meanTimeValue;
            Director dir = getDirector();
            _nextFiringTime = dir.getCurrentTime() + exp;
            dir.fireAt(this, _nextFiringTime);
        }
        return super.postfire();
    }

    /** Return the type constraints of this actor. The constraints are the
     *  ones imposed by the super class, plus the constraint that the type
     *  of the output port must be no less than the element type of the
     *  values parameter.
     *  @return an Enumeration of Inequality.
     */
    public Enumeration typeConstraints() {

	try {
	    // Set up the constraint that the output type must be no less than
	    // the element type of the values parameter. This constraint is
	    // regenerated every time since the element type may change.
            Type elemType = _getValue(0).getType();
	    TypeConstant elemTerm = new TypeConstant(elemType);
	    Inequality ineq = new Inequality(elemTerm, output.getTypeTerm());

	    LinkedList result = new LinkedList();
	    result.insertLast(ineq);
	    result.appendElements(super.typeConstraints());

	    return result.elements();
	} catch (IllegalActionException ex) {
	    throw new InternalErrorException("Clock.typeConstraints(): " +
		"Cannot get element of the values parameter.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Get the specified value, checking the form of the values parameter.
     */
    private Token _getValue(int index) throws IllegalActionException {
        MatrixToken val = (MatrixToken)(values.getToken());
        if (val == null || val.getRowCount() != 1 || index >= _length) {
            throw new IllegalActionException(this,
            "Index out of range of the values parameter.");
        }
        return val.getElementAsToken(0,index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The following are all transient to silence a javadoc bug
    // about the @serialize tag.
    // The transient qualifier should probably be removed if this
    // class is made serializable.

    // The length of the values parameter vector.
    private transient int _length;

    // The index of the current output.
    private transient int _tentativeCurrentOutputIndex;
    private transient int _currentOutputIndex;

    // The next firing time requested of the director.
    private transient double _nextFiringTime;

    // An indicator of whether a boundary is crossed in the fire() method.
    private transient boolean _boundaryCrossed;
}
