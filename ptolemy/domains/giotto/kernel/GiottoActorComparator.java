/* The actor comparator for the Giotto domain.

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
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;

import java.util.Comparator;

//////////////////////////////////////////////////////////////////////////
//// GiottoActor
/**
This class implements the Comparator interface for Actors
based on a "frequency" attribute of the actors.
The frequency of an actor which does not have a "frequency" attribute
is _DEFAULT_GIOTTO_FREQUENCY.
<p>
Given two actors A1 and A2,
compare(A1,A2) is -1 (A1<A2) if A1's frequency is strictly less than A2's frequency, or
compare(A1,A2) is 0 (A1==A2) if A1's frequency is equal to A2's frequency, or
compare(A1,A2) is 1 (A1>A2) if A1's frequency is strictly greater than A2's frequency.

@author Christoph Meyer
@version $Id$
*/

public class GiottoActorComparator implements Comparator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public static int getFrequency(Actor actor) {
	try {
	    Parameter parameter = (Parameter)
		((NamedObj) actor).getAttribute("frequency");

	    if (parameter != null) {
		IntToken intToken = (IntToken) parameter.getToken();

		return intToken.intValue();
	    } else
		return _DEFAULT_GIOTTO_FREQUENCY;
	} catch (ClassCastException ex) {
	    return _DEFAULT_GIOTTO_FREQUENCY;
	} catch (IllegalActionException ex) {
	    return _DEFAULT_GIOTTO_FREQUENCY;
	}
    }

    // Caution: equals is inconsistent with compare.

    public int compare(Object object1, Object object2) {
	if (((object1 != null) && Actor.class.isInstance(object1)) &&
	    ((object2 != null) && Actor.class.isInstance(object2))) {
	    Actor actor1 = (Actor) object1;
	    Actor actor2 = (Actor) object2;

	    if (getFrequency(actor1) < getFrequency(actor2))
		return -1;
	    else if (getFrequency(actor1) == getFrequency(actor2))
		return 0;
	    else
		return 1;
	} else
	    throw new ClassCastException();
    }

    // Caution: equals is inconsistent with compare.

    public boolean equals(Object object) {
	return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The static default Giotto frequency.
    protected static int _DEFAULT_GIOTTO_FREQUENCY = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
}
