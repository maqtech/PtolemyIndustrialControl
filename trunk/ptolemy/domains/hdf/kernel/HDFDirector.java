/* Director for the heterochronous dataflow model of computation.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

////////////////////////////////////////////////////////////////////
//// HDFDirector
/**
The heterochronous dataflow (HDF) domain implements the HDF model
of computation [1]. The HDF model of computation is a generalization
of synchronous dataflow (SDF). In SDF, the set of port rates of an
actor (called the type signature) are constant. In HDF, however,
an actor has a finite number of type signatures which are allowed
to change between iterations of the HDF schedule.
<p>
An HDF actor has an initial type signature when execution begins.
The balance equations can then be solved to find a
periodic schedule, as in SDF. Unlike SDF, an HDF actor is allowed to
change its type signature after an iteration of the schedule.
If a port rate change occurs, a new schedule
corresponding to the new ports rates must then be obtained.
<p>
Since an HDF actor has a finite number of type signatures, it
may be useful to use an FSM to control when type signature changes
may occur. The HDFFSMDirector may be used to compose HDF with
 hierarchical FSMs according to the *charts [1] semantics.
<p>
Since an HDF actor has a finite number of possible type
signatures, the number of possible schedules is also finite.
As a result of this finite state space, deadlock and bounded
channel lengths are decidable in HDF. In principle, all possible
schedules could be computed at compile time. However, the number
of schedules can be exponential in the number of actors, so this
may not be practical.
<p>
This director makes use of an HDF scheduler that computes the
schedules dynamically, and caches them. The size of the cache
can be set by the <i>scheduleCacheSize</i> parameter. The default
value of this parameter is 100.
<p>
<b>References</b>
<p>
<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee,
``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>, '' April 13,
1998.</LI>
</ol>

@see HDFFSMDirector

@author Brian K. Vogel and Rachel Zhou
@version $Id$
*/
public class HDFDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     */
    public HDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace for this object.
     */
    public HDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The HDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** A parameter representing the size of the schedule cache to
     *  use. If the value is less than
     *  or equal to zero, then schedules will never be discarded
     *  from the cache. The default value is 100.
     *  <p>
     *  Note that the number of schedules in an HDF model can be
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     */
    public Parameter scheduleCacheSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the number of firings of current director per top-level
     *  iteration.
     *  @return The number of firings of current director per top-level
     *  iteration.
     */
    public int getDirectorFiringsPerIteration() {
        return _directorFiringsPerIteration;
    }

    /** Return the scheduling sequence as an instance of Schedule.
     *  For efficiency, this method maintains a schedule cache and
     *  will attempt to return a cached version of the schedule.
     *  If the cache does not contain the schedule for the current
     *  hdf graph, then the schedule will be computed by calling
     *  the getSchedule() method of the SDFScheduler.
     *  <p>
     *  The schedule cache uses a least-recently-used replacement
     *  policy. The size of the cache is specified by the
     *  scheduleCacheSize parameter. The default cache size is
     *  100.
     *
     *  @return The Schedule for the current hdf graph.
     *
     *  @exception IllegalActionException If there is a problem getting
     *   the schedule.
     */
    public Schedule getSchedule() throws IllegalActionException{
        Scheduler scheduler = getScheduler();
        Schedule schedule;
        if (isScheduleValid()) {
            // This will return a the current schedule.
            schedule = ((SDFScheduler)scheduler).getSchedule();
        } else {
            // The schedule is no longer valid, so check the schedule
            // cache.
            if (_inputPortList == null) {
                _inputPortList = _getInputPortList();
            }
            if (_outputPortList == null) {
                _outputPortList = _getOutputPortList();
            }
            Iterator inputPorts = _inputPortList.iterator();
            String rates = new String();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort)inputPorts.next();
                int rate =
                    SDFScheduler.getTokenConsumptionRate(inputPort);
                rates = rates + String.valueOf(rate);
            }
            Iterator outputPorts = _outputPortList.iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort)outputPorts.next();
                int rate =
                    SDFScheduler.getTokenProductionRate(outputPort);
                rates = rates + String.valueOf(rate);
            }
            if (_debug_info) {
                System.out.println("Port rates = " + rates);
            }
            String rateKey = rates;
            int cacheSize =
                ((IntToken)(scheduleCacheSize.getToken())).intValue();
            if (cacheSize != _cacheSize) {
                // cache size has changed. reset the cache.
                _scheduleCache = new HashMap();
                _scheduleKeyList = new ArrayList(cacheSize);
                _cacheSize = cacheSize;
                // When using a schedule from the cache, the external
                // Rates also need to be updated. So we also need to
                // cache the external rates.
                _externalRatesCache = new TreeMap();
                _externalRatesKeyList = new ArrayList(cacheSize);
            }
            if (rateKey == _mostRecentRates) {
                schedule = ((SDFScheduler)scheduler).getSchedule();
            } else if (_scheduleCache.containsKey(rateKey)) {
                _mostRecentRates = rateKey;
                // cache hit.
                if (_debug_info) {
                    System.out.println(getName() +
                            " : Cache hit!");
                }
                if (cacheSize > 0) {
                    // Remove the key from its old position in
                    // the list.
                    _scheduleKeyList.remove(rateKey);
                    _externalRatesKeyList.remove(rateKey);
                    // and add the key to head of list.
                    _scheduleKeyList.add(0, rateKey);
                    _externalRatesKeyList.add(0, rateKey);
                }
                schedule = (Schedule)_scheduleCache.get(rateKey);
                Map externalRates = (Map)_externalRatesCache.get(rateKey);
                ((SDFScheduler)scheduler).setContainerRates(externalRates);
                //schedule = ((SDFScheduler)scheduler).getSchedule();
            } else {
                _mostRecentRates = rateKey;
                // cache miss.
                if (_debug_info) {
                    System.out.println(getName() +
                            " : Cache miss!");
                }
                if (cacheSize > 0) {
                    while (_scheduleKeyList.size() >= cacheSize) {
                        // cache is  full.
                        // remove tail of list.
                        Object object = _scheduleKeyList.get(cacheSize - 1);
                        _scheduleKeyList.remove(cacheSize - 1);
                        _externalRatesKeyList.remove(cacheSize - 1);
                        _scheduleCache.remove(object);
                        _externalRatesCache.remove(object);
                    }
                    // Add key to head of list.
                    _scheduleKeyList.add(0, rateKey);
                    _externalRatesKeyList.add(0, rateKey);
                }
                // Add key/schedule to the schedule map.
                schedule = ((SDFScheduler)scheduler).getSchedule();
                Map externalRates =
                    ((SDFScheduler)scheduler).getExternalRates();
                _externalRatesCache.put(rateKey, externalRates);
                _scheduleCache.put(rateKey, schedule);
            }
        }
        return schedule;
    }

    /** Initialize the actors associated with this director.
     *  If this method is called immediatley after preinitialize(),
     *  then it will not compute the schedule because it was done
     *  in preinitialize(). Otherwise it needs to re-compute
     *  the schedule in case this director is nested in a refinement
     *  of FSM and the "reset" in the guard is set to be true.
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler, or if the cache size parameter is not set to
     *  a valid value.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_preinitializeFlag) {
            _preinitializeFlag = false;
            SDFScheduler scheduler = (SDFScheduler)getScheduler();
            getSchedule();
        }
    }

    /** Get the HDF schedule since schedule may change in the postfire.
     *  If this director is at the top level, then update the number of
     *  firings per top-level iteration for each actor from the top level
     *  down to the bottom level.
     *  @exception IllegalActionException If no schedule can be found, or
     *  if the updateFiringsCount method throws it, or if the super class
     *  method throws it.
     */
    public boolean postfire() throws IllegalActionException {
        // Get schedule here, no matter if it is the top level.
        getSchedule();
        CompositeActor container = (CompositeActor)getContainer();
        Director exeDirector = container.getExecutiveDirector();
        //if ( exeDirector != null
        //    || (! (exeDirector instanceof HDFFSMDirector))
        //    || (! (exeDirector instanceof HDFDirector))
        //    || (! (exeDirector instanceof SDFDirector))) {
        //        System.out.println(container.getFullName());
        //        //+ "'s exeDirector is" + exeDirector.getFullName());
        //}
        if (exeDirector == null
            //|| (! (exeDirector instanceof SDFDirector))
            //|| (! (exeDirector instanceof HDFFSMDirector))
            //|| (! (exeDirector instanceof HDFDirector))
            ) {
            _directorFiringsPerIteration = 1;
            updateFiringCount(1, false);
        }
        return super.postfire();
    }

    /** Preinitialize the actors associated with this director.
     *  The super class method will compute the schedule. If this
     *  HDF director is at the top level, then update the number
     *  of firings per top-level iteration for each actor from the
     *  top level down to the bottom level.
     *  @exception IllegalActionException If the super class
     *  preintialize throws it, or if the updateFiringCount method
     *  throws it.
     */
    public void preinitialize() throws IllegalActionException {
        //_scheduleKeyList.clear();
        _mostRecentRates = "";
        super.preinitialize();
        _preinitializeFlag = true;
        CompositeActor container = (CompositeActor)getContainer();
        Director exeDirector = container.getExecutiveDirector();
        if (exeDirector == null) {
            _directorFiringsPerIteration = 1;
            updateFiringCount(1, true);
        }
    }

    /** Set the number of firings per top-level iteration of the
     *  current director.
     *  @param firingsPerIteration Number of firings per top-level
     *  iteration of the current director to be set.
     */
    public void setDirectorFiringsPerIteration(int firingsPerIteration) {
        _directorFiringsPerIteration = firingsPerIteration;
    }

    /** Update the number of firings per top-level iteration of
     *  each actor in the current director.
     *  @param directorFiringCount The number of firings per
     *  top-level iteration of the container that contains this
     *  director.
     *  @param preinitializeFlag Flag indicating whether this
     *  method is called in the preinitialize() method.
     *  @exception IllegalActionException If no schedule can be found,
     *  or if the updateFiringsCount method in HDFFSMDirector throws it.
     */
    public void updateFiringCount
        (int directorFiringCount, boolean preinitializeFlag)
            throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        Scheduler scheduler = ((SDFDirector)this).getScheduler();
        for (Iterator entities = container.deepEntityList().iterator();
                     entities.hasNext();) {
            ComponentEntity entity = (ComponentEntity)entities.next();
            int firingCount =
                ((SDFScheduler)scheduler).getFiringCount(entity);
            if (entity instanceof CompositeActor) {
                Director director = ((CompositeActor)entity).getDirector();
                if (director instanceof HDFFSMDirector) {
                    firingCount = firingCount * directorFiringCount;
                    ((HDFFSMDirector)director)
                        .setFiringsPerScheduleIteration(firingCount);
                    ((HDFFSMDirector)director)
                        .updateFiringCount(firingCount, preinitializeFlag);
                } else if (director instanceof HDFDirector) {
                    firingCount = firingCount * directorFiringCount;
                    ((HDFDirector)director)
                        .setDirectorFiringsPerIteration(firingCount);
                    ((HDFDirector)director)
                        .updateFiringCount(firingCount, preinitializeFlag);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a list of all the input ports contained by the
     *  deeply contained entities of the container of this director.
     *
     *  @return The list of input ports.
     */
    private List _getInputPortList() {
        CompositeActor container =  (CompositeActor)getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator = actors.iterator();
        List inputPortList = new LinkedList();;
        List inputPortRateList = new LinkedList();
        while (actorIterator.hasNext()) {
            Actor containedActor = (Actor)actorIterator.next();
            List temporaryInputPortList =
                containedActor.inputPortList();
            Iterator inputPortIterator =
                temporaryInputPortList.iterator();
            while (inputPortIterator.hasNext()) {
                IOPort inputPort = (IOPort)inputPortIterator.next();
                if (_debug_info) {
                    System.out.println(getName() +
                            "Found input port : " +
                            inputPort.getName());
                }
                inputPortList.add(inputPort);
            }
        }
        return inputPortList;
    }

    /** Return a list of all the output ports contained by the
     *  deeply contained entities of the container of this director.
     *
     *  @return The list of output ports.
     */
    private List _getOutputPortList() {
        CompositeActor container =  (CompositeActor)getContainer();
        List actors = container.deepEntityList();
        Iterator actorIterator2 = actors.iterator();
        List outputPortList = new LinkedList();;
        List outputPortRateList = new LinkedList();
        while (actorIterator2.hasNext()) {
            Actor containedActor = (Actor)actorIterator2.next();
            List temporaryOutputPortList =
                containedActor.outputPortList();
            Iterator outputPortIterator =
                temporaryOutputPortList.iterator();
            while (outputPortIterator.hasNext()) {
                IOPort outputPort = (IOPort)outputPortIterator.next();
                if (_debug_info) {
                    System.out.println(getName() +
                            "Found output port : " +
                            outputPort.getName());
                }
                outputPortList.add(outputPort);
            }
        }
        return outputPortList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object. In this case, we give the HDFDirector a
     *  default scheduler of the class HDFScheduler.
     */
    private void _init() {
        try {
            SDFScheduler scheduler =
                new SDFScheduler(this, uniqueName("Scheduler"));
            setScheduler(scheduler);
        }
        catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }
        try {
            int cacheSize = 100;
            _cacheSize = cacheSize;
            scheduleCacheSize = new Parameter(this,
                "scheduleCacheSize",new IntToken(cacheSize));

            _scheduleCache = new HashMap();
            _scheduleKeyList = new ArrayList(cacheSize);
            _externalRatesCache = new TreeMap();
            _externalRatesKeyList = new ArrayList(cacheSize);
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }


    // The hashmap for the schedule cache.
    private Map _scheduleCache;
    private List _scheduleKeyList;
    private List _inputPortList;
    private List _outputPortList;
    private int _cacheSize = 100;

    private Map _externalRatesCache;
    private List _externalRatesKeyList;

    private String _mostRecentRates;

    // A flag indicating whether the intialize() method is
    // called immediately after the preinitialize() method.
    private boolean _preinitializeFlag;

    // Number of firings per top-level iteration
    // of the current director.
    private int _directorFiringsPerIteration = 1;

    // Set to true to enable debugging.
    //private boolean _debug_info = true;
    private boolean _debug_info = false;
}
