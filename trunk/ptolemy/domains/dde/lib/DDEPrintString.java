/* 

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

*/

package ptolemy.domains.dde.lib;

import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DDEPrintString
/** 


@author John S. Davis II
@version $Id$
*/
public class DDEPrintString extends DDEActor {

    /** 
     */
    public DDEPrintString(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _input = new DDEIOPort( this, "input", true, false );
        _input.setMultiport(true);
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void fire() throws IllegalActionException {
        StringToken token = null;
        double time = 0.0;

        while( true ) {
	    /*
	    synchronized(this) {
		try {
		    wait();
		} catch(InterruptedException e) {
		    System.err.println("Thread interruption");
		}
	    }
	    */
            // System.out.println("\nStarting DDEPrintString.getNextToken()");
            // token = (StringToken)_input.get(0);
            token = (StringToken)getNextToken();
            // System.out.println("Finished DDEPrintString.getNextToken()");
            // time = getTimeKeeper().getCurrentTime();
            time = getCurrentTime();
	    if( token == null ) {
		System.out.println("Null token in DDEPrintString");
	    }
            // time = getCurrentTime();
            // System.out.println("\t"+ token.stringValue() );
            // System.out.println("\tTime is " + time);
            System.out.println("\t"+token.stringValue()+"\tTime is " + time);
            System.out.print("\n");
        } 
    }
    
    /** 
     */
    public void wrapup() throws IllegalActionException {
        System.out.println("\nIt is finished.\n");
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    public DDEIOPort _input;
    
}




















