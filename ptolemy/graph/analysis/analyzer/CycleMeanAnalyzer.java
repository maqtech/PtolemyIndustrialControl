/* A common interface for all the cycle mean analyzers.

 Copyright (c) 2002 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (shahrooz@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.graph.analysis.analyzer;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CycleMeanAnalyzer
/**
A common interface for all the cycle mean analyzers.
<p>
@see ptolemy.graph.analysis.CycleMeanAnalysis
@since Ptolemy II 2.0
@author Shahrooz Shahparnia
@version $Id$
*/
public interface CycleMeanAnalyzer extends GraphAnalyzer {

    /** Return the nodes on the cycle that corresponds to the maximum/minimum
     *  cycle mean as an ordered list. If there is more than one cycle with the
     *  same maximal/minimal cycle, one of them is returned randomly,
     *  but the same cycle is returned by different invocations of the method,
     *  unless the graph changes.
     *
     *  @return The nodes on the cycle that corresponds to one of the
     *  maximum/minimum cycle means as an ordered list.
     */
    public List cycle();

    /** Return the maximum cycle mean for a given directed graph.
     *
     *  @return The maximum cycle mean value.
     */
    public double maximumCycleMean();

    /** Return minimum cycle mean for a given directed graph.
     *
     *  @return The minimum cycle mean value.
     */
    public double minimumCycleMean();
}
