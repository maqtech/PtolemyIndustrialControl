/* A clock source.

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
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;

//////////////////////////////////////////////////////////////////////////
//// Clock
/**
This actor produces a periodic signal, a generalized square wave
that sequences through <i>N</i> levels with arbitrary duty cycles
and period.  It has various uses.  Its simplest use in the DE domain
is to generate a sequence of events at regularly spaced
intervals.  In CT, it can be used to generate a square wave.
In both domains, however, it can also generate more intricate
waveforms that cycle through a set of values.
<p>
At the beginning of each time interval of length given by <i>period</i>,
it initiates a sequence of output events with values given by
<i>values</i> and offset into the period given by <i>offsets</i>.
These latter two parameters must both contain row vectors
(1 by <i>N</i> matrices) of the same length (<i>N</i>), or an
exception will be thrown by the fire() method.
The <i>offsets</i> array must be nondecreasing and nonnegative,
or an exception will be thrown when it is set.
Moreover, its largest entry must be smaller than <i>period</i>
or an exception will be thrown by the fire() method.
<p>
The <i>values</i> parameter by default
contains an IntMatrix with value [1, 0] (one row,
two columns, with values 1 and 0).  The default <i>offsets</i>
vector is [0.0, 1.0].  Thus, the default output will be
alternating 1 and 0 with 50% duty cycle.  The default period
is 2.0.
<p>
The actor uses the fireAt() method of the director to request
firing at the beginning of each period plus each of the offsets.
It may in addition fire at any time in response to a trigger
input.  On such firings, it simply repeats the most recent output.
Thus, the trigger, in effect, asks the actor what its current
output value is. Some domains, such as CT, may also fire the actor at
other times, without requiring a trigger input.  Again, the actor
simply repeats the previous output.
Thus, the output can be viewed as samples of the clock waveform,
where the time of each sample is the time of the firing that
produced it.  If the actor fires before the first offset has
been reached, then a zero token of the same type as those in
the <i>values</i> matrix is produced.
<p>
The clock waveform is a square wave (in the sense that transitions
between levels are discrete and signal is piecewise constant),
with <i>N</i> levels, where <i>N</i> is the length of the <i>values</i>
parameter.  Changes between levels occur at times
<i>nP</i> + <i>o<sub>i < /sub></i> where <i>n</i> is any nonnegative integer,
<i>P</i> is the period, and <i>o<sub>i < /sub></i> is an entry
in the <i>offsets</i> vector.
<p>
The type of the output can be any token type that has a corresponding
matrix token type.  The type is inferred from the type of the
<i>values</i> parameter.

@author Edward A. Lee
@version $Id$
*/

public class Clock extends TimedSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Clock(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        period = new Parameter(this, "period", new DoubleToken(2.0));
        period.setTypeEquals(DoubleToken.class);

        double defaultOffsets[][] = {{0.0, 1.0}};
        offsets = new Parameter(this, "offsets",
                new DoubleMatrixToken(defaultOffsets));
        offsets.setTypeEquals(DoubleMatrixToken.class);
        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        int defaultValues[][] = {{1, 0}};
        IntMatrixToken defaultValueToken = new IntMatrixToken(defaultValues);
        values = new Parameter(this, "values", defaultValueToken);
        // Call this so that we don't have to copy its code here...
        attributeChanged(values);

        _dummy = new Variable(this, "_dummy", _getValue(0));
	output.setTypeAtLeast(_dummy);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain a DoubleMatrixToken.
     */
    public Parameter offsets;

    /** The period of the output waveform.
     *  This parameter must contain a DoubleToken.
     */
    public Parameter period;

    /** The values that will be produced at the specified offsets.
     *  This parameter can contain any MatrixToken.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the offsets parameter, check that the array
     *  is nondecreasing and has the right dimension.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the offsets array is not
     *   nondecreasing and nonnegative, or it is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == offsets) {
            // Check nondecreasing property.
            double[][] offsts =
                ((DoubleMatrixToken)offsets.getToken()).doubleMatrix();
            if (offsts.length != 1 || offsts[0].length == 0) {
                throw new IllegalActionException(this,
                        "Value of offsets is not a row vector.");
            }
            double previous = 0.0;
            for (int j = 0; j < offsts[0].length; j++) {
                if (offsts[0][j] < previous) {
                    throw new IllegalActionException(this,
                            "Value of offsets is not nondecreasing " +
                            "and nonnegative.");
                }
                previous = offsts[0][j];
            }
        } else if (attribute == period) {
            double prd = ((DoubleToken)period.getToken()).doubleValue();
            if (prd <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  " +
                        "Period given: " + prd);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Notify the director that a type change has occurred that may
     *  affect the type of the output.
     *  This will cause type resolution to be redone when it is next needed.
     *  It is assumed that type changes in the parameters are implemented
     *  by the director's change request mechanism, so they are implemented
     *  when it is safe to redo type resolution.
     *  If there is no director, then do nothing.
     *  @exception IllegalActionException If the new values array has no
     *   elements in it, or if it is not a MatrixToken.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {
            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateResolvedTypes();
            }
            try {
                _dummy.setToken(_getValue(0));
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                        "Cannot set values to something that is not an array: "
                        + values.getToken());
            }
        } else if (attribute != _dummy) {
            // Notice that type changes to _dummy are allowed...
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        Clock newobj = (Clock)super.clone(ws);
        newobj.offsets = (Parameter)newobj.getAttribute("offsets");
        try {
            newobj.offsets = (Parameter)newobj.getAttribute("offsets");
            newobj.attributeChanged(newobj.offsets);
            newobj.values = (Parameter)newobj.getAttribute("values");
            newobj.attributeChanged(values);
            newobj.period = (Parameter)newobj.getAttribute("period");
            newobj._dummy = (Variable)(newobj.getAttribute("_dummy"));
            newobj._dummy.setToken(newobj._getValue(0));
            newobj.output.setTypeAtLeast(newobj._dummy);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return newobj;
    }

    /** Output the current value of the clock.
     *  @exception IllegalActionException If the values and offsets parameters
     *   do not have the same dimension, or if the value in the offsets
     *   parameter is encountered that is greater than the period, or
     *   if there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Get the current time and period.
        double currentTime = getDirector().getCurrentTime();
        double prd = ((DoubleToken)period.getToken()).doubleValue();

        // In case time has gone backwards since the last call to fire()
        // (something that can occur within an iteration), reinitialize
        // these from the last known good state.
        _tentativeCycleStartTime = _cycleStartTime;
        _tentativeCurrentValue = _currentValue;
        _tentativePhase = _phase;
        // A negative number here indicates that no future firing should
        // be scheduled.
        _tentativeNextFiringTime = -1.0;

        // In case current time has reached or crossed a boundary between
        // periods, update it.  Note that normally it will not
        // have advanced by more than one period
        // (unless, perhaps, the entire domain has been dormant
        // for some time, as might happen for example in a hybrid system).
        while (_tentativeCycleStartTime + prd <= currentTime) {
            _tentativeCycleStartTime += prd;
        }
        double[][] offsts =
            ((DoubleMatrixToken)offsets.getToken()).doubleMatrix();
        MatrixToken val = (MatrixToken)(values.getToken());
        if (offsts[0].length != val.getColumnCount()) {
            throw new IllegalActionException(this,
            "Values and offsets vectors do not have the same length.");
        }
        // Adjust the phase if time has moved beyond the current phase.
        while (currentTime >=
                _tentativeCycleStartTime + offsts[0][_tentativePhase]) {
            // Phase boundary.  Change the current value.
            _tentativeCurrentValue = _getValue(_tentativePhase);
            // Increment to the next phase.
            _tentativePhase++;
            if (_tentativePhase >= offsts[0].length) {
                _tentativePhase = 0;
                // Schedule the first firing in the next period.
                _tentativeCycleStartTime += prd;
            }
            // Schedule the next firing in this period.
            _tentativeNextFiringTime
                = _tentativeCycleStartTime + offsts[0][_tentativePhase];
        }
        output.broadcast(_tentativeCurrentValue);
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the values parameter is not a row vector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _cycleStartTime = 0.0;

        double[][] offsts =
            ((DoubleMatrixToken)offsets.getToken()).doubleMatrix();
        getDirector().fireAt(this, offsts[0][0]);

        _currentValue = _getValue(0).zero();
        _phase = 0;
    }

    /** Update the state of the actor to the correspond to that tentatively
     *  computed in the most recent firing and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException {
        _cycleStartTime = _tentativeCycleStartTime;
        _currentValue = _tentativeCurrentValue;
        _phase = _tentativePhase;

        // A negative number here indicates that no future firing should
        // be scheduled.
        if (_tentativeNextFiringTime >= 0.0) {
            getDirector().fireAt(this, _tentativeNextFiringTime);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Get the specified value, checking the form of the values parameter.
     */
    private Token _getValue(int index) throws IllegalActionException {
        MatrixToken val = (MatrixToken)(values.getToken());
        if (val == null || val.getRowCount() != 1 ||
                val.getColumnCount() <= index) {
            throw new IllegalActionException(this,
                    "Index out of range of the values parameter.");
        }
        return val.getElementAsToken(0, index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Dummy variable which reflects the type of the elements of the
    // values parameter, so that the output type can be related to it.
    private Variable _dummy;

    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.

    // The current value of the clock output.
    private transient Token _currentValue;

    // The most recent cycle start time.
    private transient double _cycleStartTime;

    // The phase of the next output.
    private transient int _phase;

    // Following variables recall data from the fire to the postfire method.
    private transient Token _tentativeCurrentValue;
    private transient double _tentativeCycleStartTime;
    private transient double _tentativeNextFiringTime;
    private transient int _tentativePhase;
}
