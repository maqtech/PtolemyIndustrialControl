/* FlowThru is a test class used to test token production 
AND consumption. 

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.dde.kernel.test;

import ptolemy.domains.dde.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;


//////////////////////////////////////////////////////////////////////////
//// FlowThru
/**
FlowThru is a test class used to test token production AND consumption. 
It has a typed, input and output multiport. The fire() method of this
class simply passes through "real" tokens. Use this class to test 
DDEReceiver and DDEThread. 


@author John S. Davis II
@version $Id$

*/

public class FlowThru extends TypedAtomicActor {

    /**
     */
    public FlowThru(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);

         output = new TypedIOPort(this, "output", false, true);
	 output.setMultiport(true);
	 output.setTypeEquals(Token.class);
         input = new TypedIOPort(this, "input", true, false);
	 input.setMultiport(true);
	 input.setTypeEquals(Token.class);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public void fire() throws IllegalActionException {
	Token token = new Token();
	Receiver[][] rcvrs = input.getReceivers();
	for( int i = 0; i < rcvrs.length; i++ ) {
	    for( int j = 0; j < rcvrs[i].length; j++ ) {
		DDEReceiver rcvr = (DDEReceiver)rcvrs[i][j];
		// System.out.println("DDEPutToken receiver["+i+"]["+j+
		// "]; cnt = "+cnt);
		if( rcvr.hasToken() ) {
		    token = rcvr.get();
		    rcvrs = output.getRemoteReceivers();
		    for( int k = 0; k < rcvrs.length; k++ ) {
			for( int l = 0; l < rcvrs[k].length; l++ ) {
			    rcvr = (DDEReceiver)rcvrs[k][l];
			    rcvr.put(token);
			}
		    }
		}
	    }
	}
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    public TypedIOPort output;
    public TypedIOPort input;
}
