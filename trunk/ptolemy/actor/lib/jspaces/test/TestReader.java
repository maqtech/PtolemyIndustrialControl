/* A JavaSpace client that reads stock prices and print out on screen.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces.test;

import ptolemy.data.DoubleToken;
import ptolemy.actor.lib.jspaces.TokenEntry;
import ptolemy.actor.lib.jspaces.IndexEntry;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;


import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

//////////////////////////////////////////////////////////////////////////
//// TestReader
/**
A JavaSpace client that reads stock price from the space and print out
on the screen.

@author Yuhong Xiong
@version $Id$
@since Ptolemy II 1.0
*/

public class TestReader {
    public static void main(String[] args) {
        try {
            TokenEntry yhoo = new TokenEntry();
            yhoo.name = "YHOO";
            JavaSpace space = SpaceFinder.getSpace();

            IndexEntry minimum = new IndexEntry(
                    "YHOO", "minimum", null);
            IndexEntry maximum = new IndexEntry(
                    "YHOO", "maximum", null);
	    while (true) {
                Thread.sleep(1000l);
                IndexEntry min =
                    (IndexEntry)space.read(minimum, null, Long.MAX_VALUE);
                IndexEntry max =
                    (IndexEntry)space.read(maximum, null, Long.MAX_VALUE);
                if (min.getPosition() <= max.getPosition()) {
                    TokenEntry result =
                        (TokenEntry)space.read(yhoo, null, Long.MAX_VALUE);
                    DoubleToken tok = (DoubleToken)result.token;
                    System.out.println(" MIN: " + min.getPosition() +
                            "YHOO " + tok.doubleValue() +
                            " MAX: " + max.getPosition());
                }
	    }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

