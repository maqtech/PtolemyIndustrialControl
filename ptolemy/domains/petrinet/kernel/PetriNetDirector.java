/* Petri net director.

 Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (yukewang@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.petrinet.kernel;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.petrinet.kernel.Place;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.Random;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PetriNetDirector
/**
Petri net director.

@author  Yuke Wang and Edward A. Lee
@version $Id$
*/
public class PetriNetDirector extends Director {

    /** Construct a new Petri net director.
     *  @param container The container.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PetriNetDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of PetriNetReceiver.
     *  @return A new PetriNetReceiver.
     */
    public Receiver newReceiver() {
        return new PetriNetReceiver();
    }

    /** Fire calls _chooseTransition to select one of the
     *  ready Transitions to fire. The selection method
     *  here is random. Therefore running the same
     *  test will result in different firing sequence
     *  at different times of the day since the
     *  random seed is the time.
     *  There is also a possibility for infinite loop.
     */


    public void fire() throws IllegalActionException {
        int i = 0;
        Nameable container = getContainer();
        if (container instanceof NamedObj)
            System.out.println("the top container is"
                    + container.getFullName());

        Transition nextTransition = _chooseTransition();
        while (nextTransition != null) {
            i++;
            System.out.println("_"+i+
                    "th firing __"+nextTransition.getFullName());
            nextTransition.fire();
            _setTokens(nextTransition); 
            System.out.println("___________ start to choose next transition");
            nextTransition = _chooseTransition();




        }
    }



 



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


 /** The method first accumulates all the enabled transitions, and
  *  then randomly choose one to fire. 
  */


    private Transition _chooseTransition()  throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors =
                ((CompositeActor)container).deepEntityList().iterator();

            LinkedList readyTransitionList = new LinkedList();
            int i = 0;

            while (actors.hasNext()) {
                Transformer actor = (Transformer) actors.next();
                if (actor instanceof Transition)  {
                    Transition transition = (Transition) actor;
                    if (transition.prefire()) {
                        readyTransitionList.add(transition);
                        i++;
                    }
                    else
                        System.out.println("not ready to fire______");
                }
            }

            if (i>0) {
                System.out.print(i + "  transitions ready in choosing");
                System.out.println(" transitions----------");
                java.util.Random generator = new
                    java.util.Random(System.currentTimeMillis());
                int j = generator.nextInt(i);
                Object chosenTransition = readyTransitionList.get(j);
                if(chosenTransition instanceof Transition)
                    return (Transition) chosenTransition;
            }
            else
                return null;
        }
        return null;
    }

/**  _setTokens has two parts: to modify the marking in the output places and
 *   to modify the marking at the input places. For each output place,
 *   the fire increases the marking by the weight at the arcs.
 *   For each input place, the fire decreases the marking by the weight
 *   at the connected arcs.
 *   Multiple arcs can exist between a place and a transition.
 *   Furthermore, the arcs can be marked as a "Weight" parameter or
 *   not marked. Not-marked arcs are treated as default weight 1.
 *   Loops can exist as well. 
 *
 *  
 *
 **/
  private void _setTokens(Transition transition) throws IllegalActionException {

    System.out.print("start to increase the place marking for outputs");
    System.out.println(" width is " + transition.output.getWidth()  );
    Iterator outRelations = transition.output.linkedRelationList().iterator();
    while(outRelations.hasNext())  {
    
        IORelation weights = (IORelation) outRelations.next();
        if (weights != null) {
           Iterator placePorts = weights.linkedDestinationPortList().iterator();
           while(placePorts.hasNext()) {
               IOPort placePort = (IOPort) placePorts.next();
               Place place = (Place) placePort.getContainer();
               int i = place.getMarking();
               Attribute temporaryAttribute = (Attribute )
                          weights.getAttribute("Weight");
               if (temporaryAttribute == null) {
                   place.increaseMarking(1);
	             System.out.print("default value 1");
                   System.out.print(" source place "+ place.getFullName() +  
                           " original tokens " +i);
                   System.out.println(" new token  " + place.getMarking());
               }
               else if (temporaryAttribute instanceof Variable) {
                   Variable tAttribute = (Variable) temporaryAttribute;
                   Token weightToken = (Token) tAttribute.getToken();
                   if (weightToken instanceof ScalarToken) {
                       ScalarToken wToken = (ScalarToken) weightToken;
                       int j = wToken.intValue();
                       place.increaseMarking(j);
                       System.out.print("source place "+ place.getFullName() +
                               " original tokens " +i);
                       System.out.println("  new token  " + place.getMarking());
                   }
               }
               place.setTemporaryMarking(place.getMarking());
           }
       }
       else
           System.out.println("the arc weight is null");
   }

   System.out.print("start to decrease the place marking for input places"  );
   System.out.println("the input width is" + transition.input.getWidth());

   Iterator inRelations = transition.input.linkedRelationList().iterator();
   while(inRelations.hasNext())  {
       IORelation weights = (IORelation) inRelations.next();
       if (weights != null) {
           Iterator placePorts = weights.linkedSourcePortList().iterator();
           while(placePorts.hasNext()) {
               IOPort placePort = (IOPort) placePorts.next();
               Place place = (Place) placePort.getContainer();
               int i = place.getMarking();

               Attribute temporaryAttribute = (Attribute ) 
                       weights.getAttribute("Weight");
               if (temporaryAttribute == null) {
                   place.decreaseMarking(1);
	             System.out.print("default value 1");
                   System.out.print(" source place "+ place.getFullName()+ 
                           " original tokens " +i);
                   System.out.println("  new token  " + place.getMarking());
               }
               else if (temporaryAttribute instanceof Variable) {
                   Variable tAttribute = (Variable) temporaryAttribute;
                   Token weightToken = (Token) tAttribute.getToken();
                   if (weightToken instanceof ScalarToken) {
                       ScalarToken wToken = (ScalarToken) weightToken;
                       int j = wToken.intValue();
                       place.decreaseMarking(j);
                       System.out.print("source place "+ place.getFullName() +
                               " original tokens " +i);
                       System.out.println("  new token  " + place.getMarking());
                   }
               }
               place.setTemporaryMarking(place.getMarking());
           }
       }
       else
           System.out.println("the arc weight is null");
   }

}


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

   // private we have to set the current state here
   // we also need the initial state, which is the places with markings.

}

