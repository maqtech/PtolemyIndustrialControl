/* A base interface for all the graph analyzers.

Copyright (c) 2003-2004 The University of Maryland.
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

*/

package ptolemy.graph.analysis.analyzer;

import ptolemy.graph.Graph;

//////////////////////////////////////////////////////////////////////////
//// Analyzer
/**
   A base interface for all the graph analyzers. Graph analyzers are analyzers
   which analyze graphs and compute results, that will be accessed through methods
   that interfaces that extend this one will define.
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (shahrooz)
   @Pt.AcceptedRating Red (ssb)
   @author Shahrooz Shahparnia
   @version $Id$
*/
public interface GraphAnalyzer extends Analyzer {

    /** Return the graph associated with this analyzer.
     *
     *  @return Return the graph associated with this analyzer.
     */
    public Graph graph();
}
