/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.domains.pn.demo;
import pt.domains.pn.kernel.*;
import pt.domains.pn.stars.*;
import pt.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// PNInterleavingExample
/** 
An example to test the PN domain. This example tests the PN INterleaving 
example.
@author Mudit Goel
@version $Id$
*/

class PNInterleavingExample {

    public static void main(String args[]) throws 
	    IllegalStateException, IllegalActionException, 
            NameDuplicationException {
	PNUniverse myUniverse = new PNUniverse();
        myUniverse.setCycles(Integer.parseInt(args[0]));
        PNInterleave _interleave = new PNInterleave(myUniverse, "interleave");
        PNAlternate _alternate = new PNAlternate(myUniverse, "alternate");
        PNRedirect _redirect0 = new PNRedirect(myUniverse, "redirect0");
        _redirect0.setInitState(0);
        PNRedirect _redirect1 = new PNRedirect(myUniverse, "redirect1");
        _redirect1.setInitState(1);

        //FIXME: Find a neat way of specifying the queue length of input port!
        //FIXME: Need a nice way of doing the following.
        //Maybe a nice method that set all star parameters and links all ports
        PNOutPort portout = (PNOutPort)_interleave.getPort("output");
        PNInPort portin = (PNInPort)_alternate.getPort("input");
        IORelation queue = (IORelation)myUniverse.connect(portin, portout, "QX");
        //portin.getQueue().setCapacity(1);

 
        portout = (PNOutPort)_redirect0.getPort("output");
        portin = (PNInPort)_interleave.getPort("input0");
        queue = (IORelation)myUniverse.connect(portin, portout, "QY");
        //portin.getQueue().setCapacity(1);
 
        portout = (PNOutPort)_redirect1.getPort("output");
        portin = (PNInPort)_interleave.getPort("input1");
        queue = (IORelation)myUniverse.connect(portin, portout, "QZ");
        //portin.getQueue().setCapacity(1);
 
        portout = (PNOutPort)_alternate.getPort("output0");
        portin = (PNInPort)_redirect0.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QT1");       
        //portin.getQueue().setCapacity(1);
 
        portout = (PNOutPort)_alternate.getPort("output1");
        portin = (PNInPort)_redirect1.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QT2");
        //portin.getQueue().setCapacity(1);
 
 	myUniverse.execute();
        System.out.println("Bye World\n");
	return;
    }

}
   

