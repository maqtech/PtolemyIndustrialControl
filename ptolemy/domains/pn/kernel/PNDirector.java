/* Starts all the processes and handles deadlocks

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

package pt.domains.pn.kernel;
import pt.kernel.*;
import pt.actors.*;
import pt.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNDirector 
/** 
Handles deadlocks and creates Processes corresponding to all PN stars

@author Mudit Goel
@version $Id$
*/
public class PNDirector extends Director {

    /** Construct an Executive to manage all the processes in the PN Domain
     *  This starts a Thread Group in which the threads, corresponding to stars
     *  in the corresponding Galaxy, are created.
     * @param container is the Universe or Galaxy, this executive is 
     *  responsible for.
     * @param name is the name of this Executive
     */ 
    public PNDirector(CompositeActor container, String name) {
        super(container, name);
        _processGroup = new ThreadGroup("PNStarGroup");
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Creates a thread for each PN star and handles deadlocks and terminates
     * @exception IllegalActionException is thrown by submethods 
     */
    public boolean prefire() { //throws IllegalActionException {        
        if (_debug > 5 ) System.out.println("PNExecutive: execute()");
	//Creating threads for new actors created and starting them
        Enumeration allMyStars = getNewActors();
        while (allMyStars.hasMoreElements()) {
            PNActor star = (PNActor)allMyStars.nextElement();     
            Thread temp = new Thread(_processGroup, star);
            star.setThread(temp);
            temp.start();
        }
	clearNewActors();
	_mutate = false;
        if (_debug > 5) System.out.println(
                "PNExecutive: execute(): after while ");
        return true;
    }

    public void fire() throws IllegalActionException {
        setComplete(_handleDeadlock());
        if (_debug > 5) System.out.println(
                "PNExecutive: execute(): after _handleDeadlock() ");
        return;
    }
    
    public void postfire() {
        //_terminateAll();
        return;
    }

    public ThreadGroup getProcessGroup() {
        return _processGroup;
    }

    /** Checks for deadLock each time a Process stops
     */
    public void processStopped() {
        synchronized(workspace()) {
            _checkForDeadlock(1);
        }
    }
    
    /** Increments the no of queues blocked on read. Also checks for deadlocks 
     */
    public void readBlock() {
        synchronized(workspace()) {
            _readBlockCount++;
            //System.out.println("readblkcount is "+_readBlockCount);
            _checkForDeadlock(0);
            return;
        }
    }
 
    /** Decreases the number of queues blocked on a read
     */
    public void readUnblock() {
        synchronized(workspace()) {
            _readBlockCount--;
            //System.out.println("readunblkcount is "+_readBlockCount);
            return;
        }
    }

    /** The action to start terminating all processes due to a deadlock begins
     *	here.
     */
    public void setTerminate() {
        synchronized(workspace()) {
            _terminate = true;
            if (_debug > 7 )
                System.out.println("PNExecutive: _(): setTerminate()");
            workspace().notifyAll();
        }
    }
    
    public void wrapup() {
        _terminateAll();
        return;
    } 

    /** Increments the capacity, if it can be incremented. Else increments the
     *  number of stars blocked while writing and checks for deadlocks.
     * @param latest input port that blocked the corresponding output star
     *  on a write.
     */
    public void writeBlock() 
	    throws IllegalActionException {
        //The exception should never be thrown!!
        synchronized(workspace()) {
            _writeBlockCount++;
            _checkForDeadlock(0);
            return;
        }
    } 
    

    /** If the stars can be blocked on a write, then unblock it and 
     *  decrement the number of stars blocked on write.
     */
    public void writeUnblock() {
        synchronized(workspace()) {
            _writeBlockCount--;
            return;
        }
    }

    public void setMutate(boolean mutate) {
        synchronized(workspace()) {
            _mutate = mutate;
            workspace().notifyAll();
            return;
        }
    }
    


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    // Checks for deadlock(Both artificial and true deadlocks). 
    // noOfStoppedProcess is 0 if the deadlock is being checked for a 
    // blocked queue. 1 if check is being done when a process stopped.
    private void _checkForDeadlock(int noOfStoppedProcess) {
        synchronized(workspace()) {
            if (_readBlockCount + _writeBlockCount + noOfStoppedProcess >=
                    _processGroup.activeCount()) {
                _deadlock = true;
                if (_debug > 7 )
                    System.out.println("PNExecutive: _checkForDeadlock(): " +
                            "Detected deadlock and stp = "+noOfStoppedProcess);
                workspace().notifyAll();
            }
	return;
        }
    }

    // Finds and returns the port with the smallest write capacity 
    // that is blocked on a write.
    private PNInPort _incrementLowestWriteCapacityPort() {
        //FIXME: Should this be synchronized
        FIFOQueue smallestCapacityQueue = null;
        PNInPort smallestCapacityPort = null;
        int smallestCapacity = -1;
        Enumeration allMyStars = ((CompositeEntity)getContainer()).deepGetEntities();
        while (allMyStars.hasMoreElements()) {
            PNActor star = (PNActor)allMyStars.nextElement();
            Enumeration starPorts = star.getPorts();
            while (starPorts.hasMoreElements()) {
                PNPort port = (PNPort)starPorts.nextElement();
                //Only input ports have a queue
                if (port.isInput()) {
                    //To search only in queues blocked on a write
                    if (((PNInPort)port).isWritePending()) {
                        try {
                            Enumeration outports = port.deepConnectedOutputPorts();
                            while(outports.hasMoreElements()) {
                                PNOutPort outport = (PNOutPort)outports.nextElement();
                                FIFOQueue queue = ((PNInPort)port).getQueue(outport);
                                if (smallestCapacity == -1) {
                                    smallestCapacityQueue = queue;
                                    smallestCapacity = queue.capacity();
                                    smallestCapacityPort = (PNInPort)port;
                                }
                                else if (smallestCapacity > queue.capacity()) {
                                    smallestCapacityQueue = queue;
                                    smallestCapacity = queue.capacity();
                                    smallestCapacityPort = (PNInPort)port;
                                }
                            }
                        } catch (IllegalActionException e) {
                            System.err.println("Exception: " + e.toString());
                            //Should not be thrown as this exception is thrown 
                            //only if port is not an input port, checked above
                        }
                    }
                }
            }
        }            
        //Ensuring that a write is possible, by incrementing capacity to 
        // a positive number.

        try {
            if (smallestCapacityQueue.capacity() <= 0) { 
                smallestCapacityQueue.setCapacity(1);
            } else { 
                smallestCapacityQueue.setCapacity(smallestCapacityQueue.capacity()+1);
            }
            if (smallestCapacityPort.isWritePending()) {
                writeUnblock();
                smallestCapacityPort.setWritePending(false);
                workspace().notifyAll();
            }
        } catch (IllegalActionException e) {
            System.err.println("Exception: " + e.toString());
            //Should not be thrown as this exception is thrown 
            //only if port is not an input port, checked above
        }
        return smallestCapacityPort;
    }

    // Handles deadlock, both real and artificial
    //Returns false only if it detects a mutation.
    //Returns true for termination
    private boolean _handleDeadlock() 
	    throws IllegalActionException {
	// This exception should never be thrown!!
        // Process deadlocks
        if (_debug > 5 )
            System.out.println("PNExecutive: _handleDeadlock()");
        synchronized(workspace()) {
            if (_debug > 5 )
                System.out.println("PNExecutive: _handleDeadlock() synchro");
            while (!_terminate) {
	        if (_debug > 5 )
		    System.out.println("PNExecutive: _handleDeadlock()" +
                            " !_terminate");
                // Wait for a deadlock to occur.
                while (!_mutate && !_terminate && !_deadlock) {
                    try {
                        if (_debug > 7 )
                            System.out.println("PNExecutive: " +
                                    "_handleDeadlock(): about to wait()");
                        workspace().wait();
                    } 
                    catch (InterruptedException e) {
                        System.err.println("Exception: " + e.toString());
                    }
                }
                if (_debug > 5) 
                    System.out.println("Term is " + _terminate +
                            " deadlock is "+_deadlock);
		//Checks for mutation
                //FIXME: IS this the best way to check for mutation?
		// Maybe it should check for workspace version number.
		//FIXME: Should the flags be reset? 
		if (_mutate) { 
		    //_terminate = false;
		    //_deadlock = false;
		    return false;
		}
                if (_terminate) {
                    if (_debug > 6 ) System.out.println(
                            "PNExecutive: _handleDeadlock(): _terminate");
                    return true;
                }
                // check if it's real
                if (_writeBlockCount==0) {
                    _terminate = true;
                    System.out.println("real deadlock. Everyone would be erased");
                }
                else {
                    // it's an artificial deadlock
                    //System.out.println("Artificial deadlock");
                    _deadlock = false;
                    // find the input port with lowest capacity queue 
                    // that is blocked on a write and increment it's capacity
                    PNInPort lowestCap = _incrementLowestWriteCapacityPort();
                }
            }
        if (_debug > 5 ) System.out.println(
                "PNExecutive: _handleDeadlock(): return at bottom");
	return true;
        }
    }
    
    // Terminates all stars and hence the simulation 
    private void _terminateAll() {
        synchronized(workspace()) {
            // Obtaining all stars in the current galaxy 
            Enumeration allStars =
                ((CompositeEntity)getContainer()).deepGetEntities();
            while (allStars.hasMoreElements()) {
                // Obtaining all the ports of each star 
                Enumeration starPorts =
                    ((PNActor)(allStars.nextElement())).getPorts();
                while (starPorts.hasMoreElements()) {
                    PNPort port = (PNPort)starPorts.nextElement();
                    // Terminating the ports and hence the star 
                    if(port.isInput()) port.setTerminate();
                }
            }
        }	
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private boolean _mutate = true;

    // Container is the CompositeEntity the executive is responsible for
    private CompositeEntity _container;

    // Is set when a deadlock occurs
    private boolean _deadlock = false;

    // Level of debugging output
    private int _debug = 0;

    // The threadgroup in which all the stars are created.
    private ThreadGroup _processGroup;
    
    // Number of stars blocking on read.
    private int _readBlockCount = 0;    

    // Is set when the simulation is to be terminated
    private boolean _terminate = false;

    // No of stars blocking on write.
    private int _writeBlockCount = 0;

}


