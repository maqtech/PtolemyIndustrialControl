/*
A class that sets an 2-dimensional integer array and prints it out.

Copyright (c) 2001-2002 The University of Maryland
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/




//////////////////////////////////////////////////////////////////////////
//// Terp
/**

A class that sets an 2-dimensional integer array and prints it out.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0

*/

public class Array2DInt{

    public static void main(String args[]) {
        // Initialize array.
        int data[][] = new int[2][];
        data [0] = new int[2];
        data [1] = new int[2];

        // Set the data.
        data[0][0] = 0;
        data[0][1] = 1;
        data[1][0] = 10;
        data[1][1] = 11;

        // Print out the data.
        System.out.println(data[0][0]);
        System.out.println(data[0][1]);
        System.out.println(data[1][0]);
        System.out.println(data[1][1]);
    }
}
