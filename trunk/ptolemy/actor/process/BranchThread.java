/* An interface used by receivers that receive data across
composite actor boundaries. 

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.data.*;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// BranchThread
/**
A BranchThread is an interface used by receivers that receive 
data across composite actor boundaries. 

@author John S. Davis II
@version $Id$

*/
public class BranchThread extends PtolemyThread {

    /**
     */
    public BranchThread( Branch branch ) {
        super();
    	_branch = branch;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void continueThread() {
    	_stopRequest = false;
    }
    
    /** 
     */
    public void endThread() {
    	_continue = false;
    }
    
    /** 
     */
    public Branch getBranch() {
    	return _branch;
    }
    
    /**
     */
    public void run() {
        try {
            while( _continue ) {
                _branch.transferTokens();
                while( _stopRequest && _continue ) {
                    synchronized(this) {
                        wait();
                    }
                }
            }
        } catch( InterruptedException e ) {
        }
    } 

    /** 
     */
    public void stopThread() {
    	_stopRequest = true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    private Branch _branch;
    private boolean _continue = true;
    private boolean _stopRequest = false;

}
