/*
A class that keeps track of exceptions and traps.

Copyright (c) 2001-2002 The University of Maryland.
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


@ProposedRating Red (ankush@glue.umd.edu)
@AcceptedRating Red (ankush@glue.umd.edu)

*/

package ptolemy.copernicus.c;

import soot.Trap;
import soot.Unit;
import soot.Body;

import soot.util.Chain;

import java.util.Iterator;
import java.util.LinkedList;

/**
A class that keeps track of Exceptions and Traps.
@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/


public class ExceptionTracker {

    /** A dummy initializer method
     */
    public void ExceptionTracker() {
        //do nothing

    }

        /** checks if unit u is the beginUnit for any Trap in the body.
         *  @param u The Unit to be checked.
         *  @return True if u is a beginUnit.
         */
    public boolean isBeginUnit(Unit u) {
        return(_beginUnitList.contains(u));
    }

        /** checks if unit u is the endUnit for any Trap in the body.
         *  @param u The Unit to be checked.
         *  @return True if u is an endUnit.
         */
    public boolean isEndUnit(Unit u) {
        return(_endUnitList.contains(u));
    }

        /** checks if unit u is the handlerUnit for any Trap in the body.
         *  @param u The Unit to be checked.
         *  @return True if u is a handlerUnit.
         */
    public boolean isHandlerUnit(Unit u) {
        return(_handlerUnitList.contains(u));
    }


    /** gives the index of the first incidence of Unit u in the list of
         *  beginUnits.
         *  @param u The Unit.
         *  @return The index of Unit u in the list.
         */
        public int beginIndexOf(Unit u)    {
       return(_beginUnitList.indexOf(u));
        }

    /** gives the index of the first incidence of Unit u in the list of
         *  endUnits.
         *  @param u The Unit.
         *  @return The index of Unit u in the list.
         */
        public int endIndexOf(Unit u) {
            return(_endUnitList.indexOf(u));
        }

    /** gives the index of the first incidence of Unit u in the list of
         *  handlerUnits.
         *  @param u The Unit.
         *  @return The index of Unit u in the list.
         */
        public int handlerIndexOf(Unit u) {
            return(_handlerUnitList.indexOf(u));
        }


        /**
         * Initializes the class with a given body. This method must be called
         * before calling any of the other methods.
         * @param body The Body for which exceptions are to be tracked.
         */
    public void init(Body body) {
        _trapChain = body.getTraps();
        Iterator i = _trapChain.iterator();
        _epc = 0;

        _beginUnitList   = new LinkedList();
        _endUnitList     = new LinkedList();
        _handlerUnitList = new LinkedList();

                _currently_active_traps = new LinkedList();
                _trapsForEachEpc = new LinkedList();


        while (i.hasNext()) {
            Trap CurrentTrap = (Trap)i.next();

            Unit BeginUnit   = CurrentTrap.getBeginUnit();
            Unit EndUnit     = CurrentTrap.getEndUnit();
            Unit HandlerUnit = CurrentTrap.getHandlerUnit();

            _beginUnitList.add(BeginUnit);
            _endUnitList.add(EndUnit);
            _handlerUnitList.add(HandlerUnit);

        }
    }


        /**
         * @return True if any traps exist in the body.
         */
    public boolean trapsExist() {
        return(_beginUnitList.size() != 0);
    }

        /**
         * @return The number of traps in the body.
         */
    public int numberOfTraps() {
        return(_beginUnitList.size());
    }

        /**
         * @return The current Exceptional PC
         */
    public int getEpc() {
       return _epc;
        }

        /**
         * record that this beginUnit has been encountered and perform the
         * appropriate housekeeping functions.
         * @param u The Unit.
         */
    public void beginUnitEncountered(Unit u) {
           _storeState();

           _epc++;
           Iterator i = _trapChain.iterator();

           //record all traps beginning here as active
           while (i.hasNext()) {
               Trap ThisTrap = (Trap)i.next();
               if (ThisTrap.getBeginUnit() == u)
                   _currently_active_traps.addFirst(ThisTrap);
                   //so that traps are stored in the reverse order in which they
                   //are encountered. Most recent trap first.
           }
    }

        /**
         * record that this endUnit has been encountered and perform the
         * appropriate housekeeping functions.
         * @param u The Unit.
         */
        public void endUnitEncountered(Unit u) {
            _storeState();

            _epc++;
            Iterator i = _trapChain.iterator();

            //record all traps ending here as inactive
            while (i.hasNext()) {
                Trap ThisTrap = (Trap)i.next();
                if (ThisTrap.getEndUnit() == u)
                   _currently_active_traps.remove(ThisTrap);
            }

        }


        /**
         * @param epc The exceptional pc.
         * @return A list of all handlerUnits associated with
         * the given epc.
         */
        public LinkedList getHandlerUnitList(int epc) {
            LinkedList ListOfTraps = (LinkedList)_trapsForEachEpc.get(epc);
            LinkedList ListOfHandlers = new LinkedList();
            Iterator i = ListOfTraps.listIterator();

            while (i.hasNext()) {
                Trap t = (Trap)i.next();
                ListOfHandlers.add(t.getHandlerUnit());
            }

            return ListOfHandlers;
        }

        /**
         * @param epc The exceptional pc
         * @return A list of all Traps for the given epc.
         */
        public LinkedList getTrapsForEpc(int epc) {
            return (LinkedList)_trapsForEachEpc.get(epc);
        }

        /**
         * @return A chain of all the Traps in the body.
         */
        public Chain getTrapChain() {
            return _trapChain;
        }

        /**
         * store the current state of the ExceptionTracker.
         */
        protected void _storeState() {
            _trapsForEachEpc.add(_currently_active_traps.clone());
            //yes, this is a list of lists
        }



    protected int _epc;

    protected Chain _trapChain;

    protected LinkedList _beginUnitList;
    protected LinkedList _endUnitList;
    protected LinkedList _handlerUnitList;

    protected LinkedList _currently_active_traps;

    /**
     * list containing the list of traps for each epc
     * the index in this list is the epc, so the
     * epc is not explicitly stored in this data
     * structure.
     */

    protected LinkedList _trapsForEachEpc;

}


