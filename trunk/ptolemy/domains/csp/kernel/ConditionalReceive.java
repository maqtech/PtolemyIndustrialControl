/* Class for perfroming Conditional Receives.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.kernel;

import ptolemy.actor.*;
import ptolemy.data.Token;
import ptolemy.kernel.util.Nameable;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// ConditionalReceive
/**
Class for executing a conditional receive in a seperate thread. 
For rendezvous, the receiver is the key synchronization point. 
ConditionalReceive branches are designed to be used once. Upon instantiation, 
they are given the receiver to try to receive from, the parent 
object they are performing the conditional rendezvous for, and the 
identification number of the branch according to the parent.
<p>
A ConditionalReceive branch is created to perfrom a single conditional 
communication. The information it contains in its private memebers is 
immutable and fixed upon creation. This class is designed to be executed 
in a seperate thread. If it succeeds in rendezvousing, it notifies the 
parent actor and terminates. If it cannot rendezvous immediately, it waits 
until it can rendezvous or until it receives notification that another 
branch has succeeded , in which case it notifies the parent actor that 
it failed to rendezvous and terminates.
<p>
There are roughly three parts to the run method, each of which is relevant 
to the different rendezvous scenarios. 
<br>
Case 1: There is a put already waiting at the rendezvous point. In this case 
the branch attempts to register itself, with the parent actor, as the first 
branch ready to rendezvous. If it succeeds it performs the rendezvous, 
notifies the parent that it suceeded and returns. If it is not the first, it 
keeps on trying to register itself until it finally succeeds or another 
branch successfully rendezvoused in which case it fails and terminates. Note 
that a put cannot "go away" so it remians in an innerloop trying to 
rendezvous or failing.
<br>
Case 2: There is a conditional send waiting. In this case it tries to 
register both branches with their parents as the first to try. If it 
succeeds it performs the transfer, notifies the parent and returns. It 
performs the registration in two steps, first reistering this branch and 
then registering the other branch. If it successfully registers this branch, 
but cannot register the other, it unregisters itself as the first branch 
trying, and starts trying to rendezvous from the begining. This is because 
the conditional send could "go away". If it is unable to reister itself as 
the first branch to try, it again starts trying to rendezvous from the 
beginning.
<br>
Case 3: If there is neither a put or a conditional send waiting, it sets a 
flag in the receiver that a conditional receive is trying to rendezvous. It 
then waits until a put is executed on the receiver, or until another branch 
succeeds and this branch fails. If this branch fails, it resets the flag in 
the receiver, notifies the parent actor and returns. Note that it only needs 
to wait on a put as if a conditional send is executed on the receiver, it is 
the branch which is responsible for checking that the rendezvous can proceed.
Thus, in the case where two conditional branches are trying to rendezvous 
at a receiver, it is the responsibilty of the branch arriving second to 
check that the rendezvous can proceed(see case 2).
<p>
FIXME: does this class want/need to have a notion of workspace?
 
@author  Neil Smyth
@version $Id$

*/

public class ConditionalReceive extends ConditionalBranch implements Runnable {
  
  /** Create a conditional receive branch. 
   * @param rec The receiver the branch must try to rendezvous with.
   * @param par The CSPActor that is executing a conditional 
   *  communication construct(CIF or CDO).
   * @param branch The identification number assigned to this branch
   *   upon creation by the CSPActor. 
   */
  public ConditionalReceive(CSPReceiver rec, CSPActor par, int branch) {
    super(rec, par, branch);
  }

  ////////////////////////////////////////////////////////////////////////
  ////                         public methods                         ////
  
  /** The run method has roughly three parts: (1) where there is already 
   * a put waiting, (2) where there is a conditional send waiting, and 
   * (3) where the conditional receive is the first to arrive at the 
   * receiver.
   */
  public void run() {
    try {
      // FIXME: put in check that another get or condRec is not trying 
      //at this receiver
      Token result = null;
      //System.out.println(getParent().getName() + ": Starting conditionalReceive branch: " + getBranchNumber());
      Random rand = new Random();
      Thread.currentThread().sleep((long)(rand.nextDouble()*1000));
      boolean debug = true;
      synchronized(getReceiver()) {
	while (getReceiver().isConditionalReceiveWaiting()) {
	  // last time condRec got there first, condSend hasn't cleared yet
	  System.out.println("CondRec waiting for CondSend to clear state" + getBranchNumber());
	  getReceiver().wait();
	}

	// MAIN LOOP
	while (true) {
	  if (!isAlive()) {
	      getParent().branchFailed(getBranchNumber());
	      return;
	  } else if (getReceiver().isPutWaiting()) {
	    // CASE 1: a put is already waiting at the receiver.
	    // A put cannot dissappear so remain in this part of the 
	    // loop until this branch sucessfully rendezvous or dies.
	    while (true) {
	      if (getParent().amIFirst(getBranchNumber())) {
		// I am the branch that succeeds
		//System.out.println(getParent().getName() + ": conditionalReceive succeeded: " + getBranchNumber());
		result = getReceiver().get();
		getParent().branchSucceeded(getBranchNumber(), result);
		return;
	      } else {
		//System.out.println("not first, but still alive: " + getBranchNumber());
		getReceiver().wait();
		//System.out.println("loop again");
	      }
	      if (!isAlive()) {
		getParent().branchFailed(getBranchNumber());
		return;
	      }
	    }
	  } else if (getReceiver().isConditionalSendWaiting()) {
	  // CASE 2: a conditionalSend is already waiting. As this 
	  // condionalReceive arrived second, it has to check if both 
	  // branches are "first" and if so perform transfer & reset 
	  // state of the receiver. A ConditionalSend can "dissappear" 
	  // so this part of the loop can exit & return to the top of 
	  // main loop.
	    if (getParent().amIFirst(getBranchNumber())) {
	      // receive side ok, need to check that send side also ok
	      if (getReceiver().getOtherParent().amIFirst(getBranchNumber())) {
		//System.out.println(getParent().getName() + ": conditionalReceive succeeded: " + getBranchNumber());
		result = getReceiver().get();
		getReceiver().setConditionalSend(false, null);
		getParent().branchSucceeded(getBranchNumber(), result);
		return;
	      } else {
		getParent().releaseFirst(getBranchNumber());
		getReceiver().notifyAll();
	      }
	    }
	    //System.out.println("not first, but still alive: " + getBranchNumber());
	    getReceiver().wait();
	    //System.out.println("loop again, " + getBranchNumber());
	  } else {
	  // CASE 3: ConditionalReceive tried to rendezvous before a put or a 
	  // ConditionalSend. 
	    //System.out.println(getParent().getName() + ": setting conditionalReceiveWaiting flag: " + getBranchNumber());
	    getReceiver().setConditionalReceive(true, getParent());  
	    getReceiver().wait();
	    //System.out.println("condReceive woke up: " + getBranchNumber());
	    while (true) {
	      if (!isAlive()) {
		// reset state of receiver
		getReceiver().setConditionalReceive(false, null);
		getParent().branchFailed(getBranchNumber());
		return;
	      } else if (getReceiver().isPutWaiting()) {
		if (getParent().amIFirst(getBranchNumber())) {
		  // I am the branch that succeeds
		  getReceiver().setConditionalReceive(false, null);
		  result = getReceiver().get();	  
		  getParent().branchSucceeded(getBranchNumber(), result);
		  return;
		} 
		//System.out.println("not first branch: " + getBranchNumber());
	      }
	      getReceiver().wait();//can't rendrezvous this time, but still alive
	    }
	  } 
	}
      }
    } catch (InterruptedException ex) {
      System.out.println("ConditionalReceive interrupted: " + ex.getMessage());
    }
  }
}
	  
	       
	 

	    
