/* The scheduler for the Giotto domain.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Yellow (cm@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Comparator;

//////////////////////////////////////////////////////////////////////////
//// GiottoScheduler
/**
This class generates schedules for the actors in a CompositeActor
according to the Giotto semantics.
<p>
A schedule is represented by a tree. Consider the following CompositeActor:
<pre>
              +-----------------------+
              |           A           |
              +-----------------------+
              +-----------------------+
              |           B           |
              +-----------------------+
              +---------+   +---------+
              |    C    |   |    C    |
              +---------+   +---------+
</pre>
There are three actors A, B, and C, where C runs twice as often as A and B.
The tree representing the schedule for this CompositeActor looks as follows:
<pre>
+-------+                         +-------+
| | | --------------------------->| | |nil|
+-|-----+                         +-|-----+
  |                                 |
  V                                 V
+-------+  +-------+  +-------+   +-------+
| | | ---->| | | ---->| | |nil|   | | |nil|
+-|-----+  +-|-----+  +-|-----+   +-|-----+
  |          |          |           |
  V          V          V           V
+---+      +---+      +---+       +---+
| A |      | B |      | c |       | c |
+---+      +---+      +---+       +---+

</pre>

@author Haiyang Zheng
@version $Id$
*/
public class GiottoScheduler extends Scheduler {

    /** Construct a Giotto scheduler with no container (director)
     *  in the default workspace.
     */
    public GiottoScheduler() {
        super();
    }

    /** Construct a Giotto scheduler in the given workspace.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public GiottoScheduler(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the frequency of the given actor. If the actor has a
     *  <I>frequency</I> parameter with a valid integer value, return
     *  that value. For actors without a <I>frequency</I> parameter,
     *  their frequency is DEFAULT_GIOTTO_FREQUENCY.
     *  @param actor An actor.
     *  @return The frequency of the actor.
     */
    public static int getFrequency(Actor actor) {
        try {
            Parameter parameter = (Parameter)
                ((NamedObj) actor).getAttribute("frequency");

            if (parameter != null) {
                IntToken intToken = (IntToken) parameter.getToken();

                return intToken.intValue();
            } else
                return DEFAULT_GIOTTO_FREQUENCY;
        } catch (ClassCastException ex) {
            return DEFAULT_GIOTTO_FREQUENCY;
        } catch (IllegalActionException ex) {
            return DEFAULT_GIOTTO_FREQUENCY;
        }
    }

    // FIXME: temporarily I put the method here.
    // it may be put into protected method aera...
    public double getMinTimeStep(double period) {
        return period/_lcm;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The default Giotto frequency. Actors without a <I>frequency</I>
     *  parameter will execute with this frequency.
     */
    public static int DEFAULT_GIOTTO_FREQUENCY = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.
     *  This method should not be called directly; rather the getSchedule()
     *  method will call it when the schedule is invalid. It is not
     *  synchronized on the workspace.
     *  @return A schedule.
     *  @exception NotSchedulableException If the model is not
     *   schedulable.
     */
    protected Schedule _getSchedule() throws NotSchedulableException {
        StaticSchedulingDirector director =
                (StaticSchedulingDirector) getContainer();
        CompositeActor compositeActor =
	        (CompositeActor) (director.getContainer());
        List actorList = compositeActor.deepEntityList();
	int actorCount = actorList.size();

	int[] frequencyArray = new int[actorCount];
	int[] intervalArray = new int[actorCount];
	int[] iterateArray = new int[actorCount];

	ListIterator actorListIterator = actorList.listIterator();

	int i = 0;
	while (actorListIterator.hasNext()) {
	    Actor actor = (Actor) actorListIterator.next();
	    iterateArray[i] = frequencyArray[i] = getFrequency(actor);
	    i++;
	}

	_lcm = lcm(frequencyArray);
	_gcd = gcd(frequencyArray);
        if (_debugging) {
            _debug("LCM of frequencies is " + _lcm);
            _debug("GCD of frequencies is " + _gcd);
        }

	for (i = 0; i < actorCount; i++) {
	  intervalArray[i] = _lcm / frequencyArray[i];
     	  //System.out.println("The " + i + " actor has frequency " + frequencyArray[i] + " ----> " + intervalArray[i]);
	}

	// Compute schedule
	// based on the frequencyArray and the actorList


	Schedule schedule = new Schedule();

	for (  _giottoSchedulerTime = 0; _giottoSchedulerTime < _lcm; ) {
	    Schedule fireAtSameTimeSchedule = new Schedule();
	    actorListIterator = actorList.listIterator();
	    for (i = 0; i < actorCount; i++ ) {
		Actor actor = (Actor) actorListIterator.next();

		if ( ((_giottoSchedulerTime % intervalArray[i]) == 0)
		     &&
		     (iterateArray[i] > 0)
		   )
		{
		    Firing firing = new Firing();
		    firing.setActor(actor);
		    fireAtSameTimeSchedule.add(firing);

		}
	    }

	    _giottoSchedulerTime += _gcd;
	    // there may be several null schedule in schedule...
	    // and the time step is period / _lcm
	    //System.out.println("the size of fireAtSameTimeSchedule is " + fireAtSameTimeSchedule.size());
	    schedule.add(fireAtSameTimeSchedule);
	}
	//System.out.println("the size of schedule is " + schedule.size());
        return schedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // if they are correct, they should go the ptolemy.math package...
    private int gcd(int[] array) {
   		int count = array.length;
		int HighestNumber = array[0];
		int HoldX = 1;
		int X, i, c = 1;

		for( i = 1; i < count; ++i ) {
			if( array[i] == array[0] )
				++c;
			if( array[i] > HighestNumber )
				HighestNumber = array[i] / 2;
		}

		if( c == count )
			return array[0];

		X = 2;
		i = 0;

		while( true ) {

			// Check for Remainder
			if( (array[i] % X) != 0 ) {
				X++;
				i = 0;
			}

			// No remainder, passed
			else
				++i;

			if( i >= count ) {
				HoldX = X;
				i = 0;
				X++;
			}
			if( X >= HighestNumber + 1 )
				break;
		}

		return HoldX;
   }


    private int lcm(int[] array) {

	    int count = array.length;
	    int X = array[0];
	    int i = 0;

	    while( true ) {

		    if( (X % array[i]) == 0 ) {
			    if( i >= count-1 )
				    break;
			    i++;
		    }

		    else {
			    X = X + 1;
			    i = 0;
		    }
	    }

	    return X;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _lcm = 1;
    private int _gcd = 1;
    private int _giottoSchedulerTime = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// GiottoActorComparator
    /* This class implements the Comparator interface for actors
       based on the <I>frequency</I> parameter of the actors.
       The frequency of an actor which does not have a <I>frequency</I>
       parameter is DEFAULT_GIOTTO_FREQUENCY.
       Given two actors A1 and A2, compare(A1, A2) is -1 (A1 < A2) if A1's
       frequency is strictly less than A2's frequency, or compare(A1, A2) is 0
       (A1 == A2) if A1's frequency is equal to A2's frequency, or
       compare(A1, A2) is 1 (A1 > A2) if A1's frequency is strictly greater
       than A2's frequency.
    */

    private class GiottoActorComparator implements Comparator {

	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////

	/** Compare two actors based on their <I>frequency</I> parameter.
	 *  The frequency of an actor that does not have a <I>frequency</I>
         *  parameter is DEFAULT_GIOTTO_FREQUENCY.
	 *
	 *  @param actor1 The first actor to be compared.
	 *  @param actor2 The second actor to be compared.
	 *  @return -1 if the frequency of the first actor is strictly less
	 *   than that of the second actor, 0 if the frequencies are equal,
	 *   1 otherwise.
	 *  @exception ClassCastException If an argument is null or not an
	 *   instance of Actor.
	 */
	public int compare(Object actor1, Object actor2) {
	    if (actor1 != null && actor1 instanceof Actor &&
                actor2 != null && actor2 instanceof Actor) {

		if (getFrequency((Actor)actor1)
		        < getFrequency((Actor)actor2))
		    return -1;
		else if (getFrequency((Actor)actor1)
			== getFrequency((Actor)actor2))
		    return 0;
		else
		    return 1;
	    } else
		throw new ClassCastException();
	}
    }

}
