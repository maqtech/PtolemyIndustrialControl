/* An application for testing the conversion of Ptolemy models into
weighted graphs.

 Copyright (c) 2003 The University of Maryland  
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

package ptolemy.actor.test;

import java.util.Collection;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.GraphReader;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// TestReading
/** An application for testing the conversion of Ptolemy models into
weighted graphs. 
<p>
Usage: <code>java ptolemy.actor.test <em>xmlFileName</em></code>, 
<p>
where <code><em>xmlFileName</code></em> is the name of a MoML file that
contains a Ptolemy II specification. This application converts the
specification into a weighted graph representation, and prints out information
about this weighted graph.

@author Shuvra S. Bhattacharyya 
@version $Id$
*/

public class TestGraphReader {

    // Make the constructor private to prevent instantiation of this class
    private TestGraphReader() {}

    /** Convert a MoML file that contains a Ptolemy II specification into a
     *  weighted graph representation, and display information about
     *  the weighted graph.
     *  @param args The name of the MoML file.
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            throw new RuntimeException("TestReading expects exactly one "
                    + "argument.");
        }

        // The Ptolemy II model returned by the Java parser.
        NamedObj toplevel;

        try {
            MoMLParser parser = new MoMLParser();
            toplevel = parser.parseFile(args[0]);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage()
                    + "Exception raised from the MoML parser\n");
        }

        if (! (toplevel instanceof CompositeActor)) {
            throw new RuntimeException("Top level must be a CompositeActor "
                    + "(in this case, it is '"
                    + ((toplevel == null) ? "null" : 
                    toplevel.getClass().getName())
                    + "')\n");
        }

        GraphReader graphReader = new GraphReader();
        DirectedGraph graph = (DirectedGraph)(graphReader.convert(
                (CompositeActor)toplevel));

        System.out.println("Finished converting graph. A description follows.");
        System.out.println(graph.toString()); 

        // Determine the source nodes
        Collection sourceCollection = graph.sourceNodes();
        System.out.println("Number of source nodes = " + 
                sourceCollection.size()); 
        Iterator sources = sourceCollection.iterator();
        while (sources.hasNext()) {
            int i=0;
            System.out.println("source #" + i + ": " + 
                    ((Node)(sources.next())).getWeight()); 
            System.out.println(); 
        }

        // Determine the sink nodes
        Collection sinkCollection = graph.sinkNodes();
        System.out.println("Number of sink nodes = " + 
                sinkCollection.size()); 
        Iterator sinks = sinkCollection.iterator();
        while (sinks.hasNext()) {
            int i=0;
            System.out.println("sink #" + i + ": " + 
                    ((Node)(sinks.next())).getWeight()); 
            System.out.println(); 
        }
    }
}
