/* The scheduler for the Giotto domain.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// GiottoScheduler
/**


@author Christoph Meyer
@version $Id$
*/

public class GiottoScheduler extends Scheduler {
    /** Construct a Giotto scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "GiottoScheduler".
     */
    public GiottoScheduler() {
        super();
    }

    /** Construct a Giotto scheduler in the given workspace with the name
     *  "GiottoScheduler".
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The static name, FIX ME: not used
    protected static String _DEFAULT_GIOTTO_SCHEDULER_NAME = "GiottoScheduler";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the scheduling sequence.
     *  Overrides _schedule() method in base class.
     *
     *  This method should not be called directly, rather the schedule()
     *  method
     *  will call it when the schedule is invalid. So it is not
     *  synchronized on the workspace.
     *
     * @see ptolemy.kernel.CompositeEntity#deepGetEntities()
     * @return an Enumeration of the scheduling sequence.
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        StaticSchedulingDirector director =
            (StaticSchedulingDirector) getContainer();

        CompositeActor compositeActor =
	    (CompositeActor) (director.getContainer());

        List actorList = compositeActor.deepEntityList();

	/* Sort all actors according to their frequency.
	   Small frequency value means earlier in the list.
	   Keeps order of actors with same frequency value. */
	Collections.sort(actorList, new GiottoActorComparator());

	// Compute schedule

	if (actorList.isEmpty())
	    return null;
	else {
	    // Get first actor's frequency.
	    // Assumption: It's the lowest frequency in actorList.

	    Actor actor = (Actor) actorList.get(0);

	    int frequency = GiottoActorComparator.getFrequency(actor);

	    List scheduleList = _treeSchedule(actorList.listIterator(),
                    GiottoActorComparator._DEFAULT_GIOTTO_FREQUENCY,
                    frequency);

	    return Collections.enumeration(scheduleList);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private List _treeSchedule(ListIterator iterator,
            int lastFrequency, int frequency)
	throws NotSchedulableException {

	List sameFrequencyList = new LinkedList();
	List higherFrequencyList = null;

	while (iterator.hasNext()) {
	    Actor actor = (Actor) (iterator.next());

	    int actorFrequency = GiottoActorComparator.getFrequency(actor);

	    if (actorFrequency == frequency)
		sameFrequencyList.add(actor);
	    else if (actorFrequency > frequency) {
		// Makes sure that current actor will be read again.

		Actor dummy = (Actor) (iterator.previous());

		higherFrequencyList = _treeSchedule(iterator, frequency, actorFrequency);

		// Redundant break because recursive call
		// completely iterates iterator.

		break;
	    } else
		throw new NotSchedulableException("Sorting frequencies failed!");
	}

	List scheduleList = new LinkedList();

	// Assumption: frequency >= lastFrequency

	if ((frequency%lastFrequency) == 0) {
	    int currentFrequency = frequency / lastFrequency;

	    // Length of scheduleList will be even!

	    for (int i = 1; i <= currentFrequency; i++) {
		scheduleList.add(sameFrequencyList);
		scheduleList.add(higherFrequencyList);
	    }
	} else
	    throw new NotSchedulableException("Frequencies not harmonic!");

	return scheduleList;
    }
}
