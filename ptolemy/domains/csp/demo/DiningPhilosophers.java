/* Sieve of Eratosthenes demo.

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
*/


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Implementation of the Dining Philosophers problem.
/**
Five philosophers are seated at a table with a large bowl of food in 
the middle. Between each pair of philosophers is one chopstick, and to 
eat a philosopher must use both chopsticks beside him. Each philosopher 
spends his life in the following cycle: He thinks for a while, gets hungry,
 picks up one of the chopsticks beside him, then the other, eats for a 
while and puts the chopsticks down on the table again. If a philosopher 
tries to grab a chopstick  but it is already being used by another 
philosopher, then the philosopher waits until that chopstick becomes 
available. This implies that no neighbouring philosophers can eat at the 
same time and at most two philosophers can eat at a time.
<p>
The Dining Philosophers problem was first dreamt up by Edsger W. Dijkstra 
in 1965. It is a classic concurrent programming problem that illustrates 
the two basic properties of concurrent programming:
<LI>
<B>Liveness</B>. How can we design the program to avoid deadlock, where 
none of the the philosophers can make progress because each is waiting 
for someone else to do something?
<LI>
<B>Fairness</B>. How can we design the program to avoid starvation, where 
one of the philosoph ers could make progress but does not because others 
always go first?
<p>
This demo uses an algorithm that lets each philosopher randomly chose 
which chopstick to pick up first, and all phiilosophers eat and think at the 
same rates. This algorithm is fair as any time a chopstick is not being used 
and both philosophers try to use it, they both have an equal chance of 
succeeding. However this algorithm does not guarantee the absence of 
deadlock, and if it is let run long enough this will eventually occur. 
The probability that deadlock occurs sooner increases as he thinking 
times are decreased relative to the eating times.
<p>
@author Neil Smyth
@version $Id$
*/
public class DiningPhilosophers {

    /** Create an instance of the Dining Philosophers demo.
     */
    public DiningPhilosophers() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The application code.
     */
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "DiningPhilosophers demo");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector("Local Director");
            univ.setManager(manager);
            univ.setDirector(localdir);

            Parameter thinkingRate = new Parameter(univ, "thinkingRate");
            thinkingRate.setExpression("1.0");
            thinkingRate.evaluate();

            Parameter eatingRate = new Parameter(univ, "eatingRate");
            eatingRate.setExpression("1.0");
            eatingRate.evaluate();

            // Set up the actors and connections
            CSPPhilosopher p1 = new CSPPhilosopher(univ, "Aristotle");
            CSPPhilosopher p2 = new CSPPhilosopher(univ, "Plato");
            CSPPhilosopher p3 = new CSPPhilosopher(univ, "Sartre");
            CSPPhilosopher p4 = new CSPPhilosopher(univ, "DesCatres");
            CSPPhilosopher p5 = new CSPPhilosopher(univ, "Socrates");

            CSPFork f1 = new CSPFork(univ, "Fork1");
            CSPFork f2 = new CSPFork(univ, "Fork2");
            CSPFork f3 = new CSPFork(univ, "Fork3");
            CSPFork f4 = new CSPFork(univ, "Fork4");
            CSPFork f5 = new CSPFork(univ, "Fork5");
            
            // Now connect up the Actors
            IORelation r1 = (IORelation)univ.connect(p1.leftIn, f5.rightOut);
            IORelation r2 = (IORelation)univ.connect(p1.leftOut, f5.rightIn);
            IORelation r3 = (IORelation)univ.connect(p1.rightIn, f1.leftOut);
            IORelation r4 = (IORelation)univ.connect(p1.rightOut, f1.leftIn);
            
            IORelation r5 = (IORelation)univ.connect(p2.leftIn, f1.rightOut);
            IORelation r6 = (IORelation)univ.connect(p2.leftOut, f1.rightIn);
            IORelation r7 = (IORelation)univ.connect(p2.rightIn, f2.leftOut);
            IORelation r8 = (IORelation)univ.connect(p2.rightOut, f2.leftIn);
            
            IORelation r9  = (IORelation)univ.connect(p3.leftIn, f2.rightOut);
            IORelation r10 = (IORelation)univ.connect(p3.leftOut, f2.rightIn);
            IORelation r11 = (IORelation)univ.connect(p3.rightIn, f3.leftOut);
            IORelation r12 = (IORelation)univ.connect(p3.rightOut, f3.leftIn);
            
            IORelation r13 = (IORelation)univ.connect(p4.leftIn, f3.rightOut);
            IORelation r14 = (IORelation)univ.connect(p4.leftOut, f3.rightIn);
            IORelation r15 = (IORelation)univ.connect(p4.rightIn, f4.leftOut);
            IORelation r16 = (IORelation)univ.connect(p4.rightOut, f4.leftIn);
            
            IORelation r17 = (IORelation)univ.connect(p5.leftIn, f4.rightOut);
            IORelation r18 = (IORelation)univ.connect(p5.leftOut, f4.rightIn);
            IORelation r19 = (IORelation)univ.connect(p5.rightIn, f5.leftOut);
            IORelation r20 = (IORelation)univ.connect(p5.rightOut, f5.leftIn);
            
            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}
