/* A data structure for storing a receiver along with its rcvrTime
and priority.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.odf.kernel;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// RcvrTimeTriple
/**
A RcvrTimeTriple is a data structure for storing a receiver along with its
rcvrTime and priority. RcvrTimeTriples are used by ODFActors to order
incoming events according to time stamps. Each ODFActor has a RcvrTimeTriple
associated with each receiver it owns. In situations where multiple
receivers of an ODFActor have simultaneous events, the priority of the
RcvrTimeTriples are used to determine order.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.odf.kernel.Event
@see ptolemy.domains.odf.kernel.ODFReceiver
@see ptolemy.domains.odf.kernel.ODFActor

*/

public class RcvrTimeTriple extends NamedObj {

    /** Construct a RcvrTimeTriple with a TimeQueueReceiver, a
     *  rcvr time and a priority. The rcvr time must be greater
     *  than or equal to any previous rcvr times associated with
     *  the TimedQueueReceiver.
     * @param rcvr The TimedQueueReceiver associated with this
     *  RcvrTimeTriple
     * @param rcvrTime The time associated with this RcvrTimeTriple
     * @param priority The priority associated with this
     *  RcvrTimeTriple
     */
    public RcvrTimeTriple(TimedQueueReceiver rcvr, double rcvrTime,
            int priority ) {
        super();
        _rcvr = rcvr;
        _priority = priority;
        if( rcvrTime < _rcvrTime && rcvrTime != -1 ) {
            System.out.println(" Rcvr times must be monotonically "
                    + "non-decreasing!\n");
        }
        _rcvrTime = rcvrTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the TimedQueueReceiver of this RcvrTimeTriple.
     * @return TimedQueueReceiver The TimedQueueReceiver of this
     *  RcvrTimeTriple.
     */
    public TimedQueueReceiver getReceiver() {
        return _rcvr;
    }

    /** Return the priority of this RcvrTimeTriple.
     * @return int The priority of this RcvrTimeTriple.
     */
    public int getPriority() {
        return _priority;
    }

    /** Return the time of this RcvrTimeTriple.
     * @return double The time of this RcvrTimeTriple.
     */
    public double getTime() {
        return _rcvrTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private TimedQueueReceiver _rcvr;
    private double _rcvrTime = 0.0;
    private int _priority;
}
