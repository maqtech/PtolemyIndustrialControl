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
import ptolemy.actor.TypedCompositeActor;
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

 *   Petri net director. Basic Petri net model consists of places and
 *   transitions. A transition is enabled if places connected to the
 *   input of the transition all have more tokens than the corresponding
 *   edge weights. An enabled transition can fire. WHen a transition fires,
 *   it reduces the tokens in the places connected to the input of the
 *   transition, and increase the tokens in places connected to the output
 *   of the transition.
 *
 *
 *   The key methods are the testing whether a transition
 *   is ready or not _testReadyTransition, and fire an enabled transition
 *   _fireTransition. The sequence of firing is determinted by the method
 *   fireHierarchicalPetriNet.

 *   It is assumed that the Peri net is a hierarchical petri net. This works
 *   fine for flat Petri net.


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

    /**
     *  For hierarchical structure, the chosen Transition
     *  can be a composite PetriNetActor or  a Transition.
     *
     *  We have to fire a hierarchical petri net even if the
     *  inside component can be flat, since it may connected to things
     *  outside the component.
     */

    public void fire() throws IllegalActionException {
        int i = 0;
        Nameable container = getContainer();
        if (container instanceof TypedCompositeActor) {
            System.out.println("PetriNetDirector,the top container is"
                    + container.getFullName());
            TypedCompositeActor pnContainer =
                                 (TypedCompositeActor) container;

            fireHierarchicalPetriNet(pnContainer);

        }
    }

/** This method fires enabled components step by step.
 *  Each PetriNetActor is fired once, and then it
 *  returns the control to this method and choose
 *  next enabled component.
 *  Since Petri net can go to infinite firing sequence, the count
 *  is used to control the number of firing, for testing purpose.
 *
 *  Other form of firing sequence can be defined and coded as well.
 *  We could randomly fire all the deeply contained transitions.
 *  We could randomly fire the components by hierarchy.
 */

    public void fireHierarchicalPetriNet(TypedCompositeActor container)
                      throws IllegalActionException {

        System.out.println(" _fireHierarchicalPetriNet ___________");

        LinkedList components = _readyComponents(container);
        LinkedList nextComponents = components;
        int i = components.size();
        int k = 0;

        while (i > 0 & k < 5) {



            System.out.print(i + "  transitions ready in choosing");
            System.out.println(" transitions--");

            java.util.Random generator = new
                    java.util.Random(System.currentTimeMillis());
            int j = generator.nextInt(i);
            Nameable chosenTransition = (Nameable) nextComponents.get(j);

            System.out.println();
            System.out.println("start firing " + chosenTransition.getFullName());
            if(chosenTransition instanceof Transition) {
                Transition realTransition = (Transition) chosenTransition;
                _fireTransition(realTransition);
            }
            else if(chosenTransition instanceof PetriNetActor) {
                PetriNetActor realPetriNetActor =
                                (PetriNetActor) chosenTransition;
                k++;
                _fireHierarchicalPetriNetOnce(realPetriNetActor);

            }

            System.out.println(" _finished fireHierarchicalPetriNet ___________");
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();

            nextComponents = _readyComponents(container);
            i = nextComponents.size();

            Iterator pointer = nextComponents.iterator();
            while (pointer.hasNext()) {
                Nameable item = (Nameable) pointer.next();
                System.out.println(i +" ready item is " + item.getFullName());
            }

            k++;

        }

    }

    public boolean  testReadyTransition(Transition transition)
                                    throws IllegalActionException {
        return (_testReadyTransition(transition));
    }



///////////////////////////////////////////////////////////////////
////                         private methods                   ////



/** This is one of the key methods for hierarchical Petri Nets.
 *  It is equivalent to the Prefire method for a transition.
 *
 *  This method works like the graph traverse algorithm, breadth first search
 *  for places connected to the transition, due to the many possible
 *  ports involved and the connection between ports.
 *
 *  multiple arcs are allowed between ports, port-places, and transition-place.
 *  the total weight of the edge from a transition to a place is the
 *  sum of all edges.
 *
 *  It is assumed that the place is connected to transitions or ports
 *  and transitions are connected to places or ports. no action is
 *  performed to verify this.
 */

    private boolean  _testReadyTransition(Transition transition)
                                    throws IllegalActionException {

        boolean readyToFire = true;
        LinkedList placeList =  _findBackwardConnectedPlaces(transition);
        Iterator pointer1 = placeList.iterator();
        int i1 = 0;
        while (pointer1.hasNext()) {
            Place item = (Place) pointer1.next();
            i1++;
            item.setTemporaryMarking(item.getMarking());
        }


        LinkedList newRelationList = new LinkedList();
        newRelationList.addAll(transition.input.linkedRelationList());
        LinkedList temporarySourcePortList = new LinkedList();
        while(newRelationList.size()>0 )  {
            IORelation weights = (IORelation) newRelationList.getFirst();
            if (weights != null) {
                Iterator weightPorts =
                        weights.linkedSourcePortList().iterator();
                while(weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();
                    if (!temporarySourcePortList.contains(weightPort)) {
                        temporarySourcePortList.add(weightPort);
                        Nameable weightPlace = (Nameable) weightPort.getContainer();
                        if (weightPlace instanceof PetriNetActor) {
                              if(weightPort.isOutput())
                                  newRelationList.addAll
                                                  (weightPort.insideRelationList());
                              else if( weightPort.isInput())
                                  newRelationList.addAll
                                                  (weightPort.linkedRelationList());
                        }
                    }
                    else
                        System.out.println("*******found used source port  "
                                                      + weightPort.getFullName());
                }

                int weightNumber = _getWeightNumber(weights);
                LinkedList  updatePlace = _findBackwardConnectedPlaces(weights);
                Iterator pointer = updatePlace.iterator();
                while (pointer.hasNext()) {
                    Place item = (Place) pointer.next();
                    item.decreaseTemporaryMarking(weightNumber);
                    if(item.getTemporaryMarking()<0)
                        return false;
                }
            } else
                System.out.println("the arc weight is null");
            newRelationList.remove(weights);
        }

        return readyToFire;
    }

/** This method finds all the enabled components in a container
 *  and return the list. The firing method will find one component
 *  from this list randomly.
 */

    private LinkedList _readyComponents(TypedCompositeActor container)
                                      throws IllegalActionException {
        Iterator actors = container.entityList().iterator();
        LinkedList readyComponentList = new LinkedList();
        while (actors.hasNext()) {
            Nameable component = (Nameable) actors.next();
            if (component instanceof PetriNetActor)  {
                PetriNetActor pnActor = (PetriNetActor) component;
                if( pnActor.prefire()) {
                    readyComponentList.add(pnActor);
                    System.out.println("found a readyPetriNetActor  "
                                          + pnActor.getFullName());
                }
            }
            else if (component instanceof Transition) {
                Transition componentTransition = (Transition) component;
                if( _testReadyTransition(componentTransition))
                readyComponentList.add(componentTransition);
            }
        }
        return readyComponentList;
    }

/** This is another key method for the Petri Net Domain.
 *  This method updates the tokens in the places connected to the
 *  firing transition. It has two parts: to increase tokens in the forward
 *  connected places, and to decresae tokens in the backward connected
 *  places. The weights to be increased/decreased are determined by
 *  the edges connecting the places to the firing transition.
 *
 */

    private void  _fireTransition(Transition transition)
                                    throws IllegalActionException {


        LinkedList newRelationList = new LinkedList();
        newRelationList.addAll(transition.output.linkedRelationList());
        LinkedList temporaryDestinationPortList = new LinkedList();
        while(newRelationList.size()>0 )  {
            IORelation weights = (IORelation) newRelationList.getFirst();
            if (weights != null) {
                System.out.println("start to increase the weight of relation "
                        +"*********"           + weights.getFullName());
                Iterator weightPorts =
                        weights.linkedDestinationPortList().iterator();
                while(weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();
                    if (!temporaryDestinationPortList.contains(weightPort)) {
                        temporaryDestinationPortList.add(weightPort);
                        Nameable weightPlace =
                                          (Nameable) weightPort.getContainer();
                        if (weightPlace instanceof PetriNetActor) {
                            if(weightPort.isOutput())
                                newRelationList.addAll
                                             (weightPort.linkedRelationList());
                            else if( weightPort.isInput())
                                newRelationList.addAll
                                             (weightPort.insideRelationList());
                        }
                        else if( weightPlace instanceof Place) {
                            Place realPlace = (Place) weightPlace;
                            System.out.print("found a place "
                                + realPlace.getFullName() + "  "
                                + realPlace.getMarking());
                        }
                        else
                            System.out.println("something wrong "
                                          + weightPlace.getFullName());
                    }
                }

                int weightNumber = _getWeightNumber(weights);
                LinkedList  updatePlaceForward =
                                   _findForwardConnectedPlaces(weights);
                Iterator pointerForward = updatePlaceForward.iterator();
                int i = 0;
                while (pointerForward.hasNext()) {
                    Place itemForward = (Place) pointerForward.next();
                    i++;
                    int j = itemForward.getMarking();
                    itemForward.increaseMarking(weightNumber);
                    System.out.println("  the " + i + " item is "
                                + itemForward.getFullName()
                                +"  "+ j
                                + "  "+ itemForward.getMarking());
                }
            } else
                System.out.println("the arc weight is null");
            newRelationList.remove(weights);

        }

        LinkedList backRelationList = new LinkedList();
        backRelationList.addAll(transition.input.linkedRelationList());
        LinkedList temporarySourcePortList = new LinkedList();
        while(backRelationList.size()>0 )  {
            IORelation weights = (IORelation) backRelationList.getFirst();
            if (weights != null) {
                System.out.println("start to decrease the weight of relation "
                        +"*********"           + weights.getFullName());
                Iterator weightPorts =
                        weights.linkedSourcePortList().iterator();
                while(weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();
                    if (!temporarySourcePortList.contains(weightPort)) {
                        temporarySourcePortList.add(weightPort);
                        Nameable weightPlace =
                                            (Nameable) weightPort.getContainer();
                        if (weightPlace instanceof PetriNetActor) {
                            if(weightPort.isOutput())
                                backRelationList.addAll
                                                (weightPort.insideRelationList());
                            else if( weightPort.isInput())
                                backRelationList.addAll
                                                 (weightPort.linkedRelationList());
                        }
                    }
                }
                int weightNumber = _getWeightNumber(weights);
                LinkedList  updatePlace = _findBackwardConnectedPlaces(weights);
                Iterator pointer = updatePlace.iterator();
                int i = 0;
                while (pointer.hasNext()) {
                    Place item = (Place) pointer.next();
                    i++;
                    int j = item.getMarking();
                    item.decreaseMarking(weightNumber);
                    System.out.println("  the " + i + " item is " +
                       item.getFullName() + " old " + j
                       + " new  " + item.getMarking());
                }
            } else
                System.out.println("the arc weight is null");
            backRelationList.remove(weights);
        }
    }

/** This method is for fire a composite Petri Net once. This is needed
 *  for some firing sequence.
 */

    private void _fireHierarchicalPetriNetOnce(TypedCompositeActor container)
                      throws IllegalActionException {

        System.out.println(" _fireHierarchicalPetriNetOnce_");

        LinkedList components = _readyComponents(container);
        int i = components.size();
        if (i > 0) {

            System.out.print(i + "  transitions ready in choosing");
            System.out.println(" transitions--");

            java.util.Random generator = new
                    java.util.Random(System.currentTimeMillis());
            int j = generator.nextInt(i);
            Nameable chosenTransition = (Nameable) components.get(j);

            System.out.println();
            System.out.println("start firing " + chosenTransition.getFullName());
            if(chosenTransition instanceof Transition) {
                Transition realTransition = (Transition) chosenTransition;
                _fireTransition(realTransition);
            }
            else if(chosenTransition instanceof PetriNetActor) {
                PetriNetActor realPetriNetActor =
                                (PetriNetActor) chosenTransition;
                _fireHierarchicalPetriNetOnce(realPetriNetActor);
            }
            System.out.println(" _finished fireHierarchicalPetriNetOnce");
        }

    }


/** This method finds the forward connected places for a given relation.
 *  This is equivalent to find all places reachable for this relation.
 *  This method is needed when we update the tokens in places connected
 *  to a firing transition.
 */

    private LinkedList  _findForwardConnectedPlaces(IORelation weight)
                                   throws IllegalActionException {

        LinkedList newRelationList = new LinkedList();
        newRelationList.add(weight);
        LinkedList temporaryDestinationPortList = new LinkedList();
        LinkedList temporaryPlaceList = new LinkedList();
        while(newRelationList.size()>0 )  {
            IORelation weights = (IORelation) newRelationList.getFirst();
            Iterator weightPorts =
                        weights.linkedDestinationPortList().iterator();
            while(weightPorts.hasNext()) {
                IOPort weightPort = (IOPort) weightPorts.next();
                if (!temporaryDestinationPortList.contains(weightPort)) {
                    temporaryDestinationPortList.add(weightPort);
                    Nameable weightPlace = (Nameable) weightPort.getContainer();
                    if (weightPlace instanceof PetriNetActor) {
                        if(weightPort.isOutput())
                            newRelationList.addAll(weightPort.linkedRelationList());
                        else if( weightPort.isInput())
                            newRelationList.addAll(weightPort.insideRelationList());
                    }
                    else if(weightPlace instanceof Place)
                        temporaryPlaceList.add(weightPlace);
                    else {
                        System.out.println("*******found no place/petrinetactor"
                              + weightPort.getFullName());
                        return null;
                    }
                }

            }
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }


/** For each relation, this method finds all the affected
 *  places in the backward direction. Those places determine
 *  whether a transition is ready to fire or not. If ready,
 *  the firing transition has to update the tokens in all
 *  these places. The algorithm used in this method is the
 *  breadth first search of the graph.
 */

    private LinkedList  _findBackwardConnectedPlaces(IORelation weight)
                                   throws IllegalActionException {

        LinkedList newRelationList = new LinkedList();
        newRelationList.add(weight);
        LinkedList temporarySourcePortList = new LinkedList();
        LinkedList temporaryPlaceList = new LinkedList();
        while(newRelationList.size()>0 )  {
            IORelation weights = (IORelation) newRelationList.getFirst();
            Iterator weightPorts =
                        weights.linkedSourcePortList().iterator();
            while(weightPorts.hasNext()) {
                IOPort weightPort = (IOPort) weightPorts.next();
                if (!temporarySourcePortList.contains(weightPort)) {
                    temporarySourcePortList.add(weightPort);
                    Nameable weightPlace = (Nameable) weightPort.getContainer();
                    if (weightPlace instanceof PetriNetActor) {
                        if(weightPort.isOutput())
                            newRelationList.addAll(weightPort.insideRelationList());
                        else if( weightPort.isInput())
                            newRelationList.addAll(weightPort.linkedRelationList());
                    }
                    else if(weightPlace instanceof Place)
                        temporaryPlaceList.add(weightPlace);
                    else {
                        System.out.println("*******found no place/petrinetactor  "
                                  + weightPort.getFullName());
                        return null;
                    }
                }

            }
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }




/** This method finds all the places that determines whether a
 *  transition is enabled or not. It starts to trace each
 *  input relation of the transition and find each of the place
 *  connected to the relation.
 *  This allows duplicated copies of the same place.It unites all
 *  the connected places to each relation.
 */


    private LinkedList _findBackwardConnectedPlaces(Transition transition)
                                    throws IllegalActionException {

        LinkedList newRelationList = new LinkedList();
        newRelationList.addAll(transition.input.linkedRelationList());
        LinkedList temporaryPlaceList = new LinkedList();
        while(newRelationList.size()>0 )  {
            IORelation weights = (IORelation) newRelationList.getFirst();
            temporaryPlaceList.addAll( _findBackwardConnectedPlaces(weights));
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }


/** THe current hierarchical Petri Net allows mutliple arcs connecting
 *  places, transitions, and ports. Each arc can have an attribute
 *  "weight", or without such attribute. THe default is assumed to
 *  be weight 1. This default weight can be changed into other weight
 *  if necessary.
 */

    private  int  _getWeightNumber(IORelation weights)
                                  throws IllegalActionException {

        Attribute temporaryAttribute = (Attribute)
            weights.getAttribute("Weight");
        if (temporaryAttribute == null)
            return 1;
        else if (temporaryAttribute instanceof Variable) {
            Variable tAttribute = (Variable) temporaryAttribute;
            Token weightToken = (Token) tAttribute.getToken();
            if (weightToken instanceof ScalarToken) {
                ScalarToken wToken = (ScalarToken) weightToken;
                int j = wToken.intValue();
                return j;
           }
           else
               return 0;
        }
        else {
            System.out.println(" something wrong with the edge" );
            return 0;
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}

