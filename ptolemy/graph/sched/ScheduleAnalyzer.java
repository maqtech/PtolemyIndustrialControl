/* An interface for all the scheduling strategies on graphs.

Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.graph.sched;

import ptolemy.graph.analysis.analyzer.GraphAnalyzer;

//////////////////////////////////////////////////////////////////////////
//// ScheduleAnalyzer
/**
   An interface for all the scheduling strategies on graphs.
   <p>
   @see ptolemy.graph.analysis.analyzer.GraphAnalyzer;
   @since Ptolemy II 4.0
   @Pt.ProposedRating red (shahrooz@eng.umd.edu)
   @Pt.AcceptedRating red (ssb@eng.umd.edu)
   @author Shahrooz Shahparnia
*/
public interface ScheduleAnalyzer extends GraphAnalyzer {

    ///////////////////////////////////////////////////////////////////
    ////                         public classes                    ////

    /** Return the schedule computed by the associated analyzer.
     *
     *  @return Return the schedule computed by the associated analyzer.
     */
    public Schedule schedule();
}
