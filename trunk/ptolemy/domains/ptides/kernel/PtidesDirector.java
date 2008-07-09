/*
@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TimedDirector;
import ptolemy.actor.process.CompositeProcessDirector;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.actor.process.ProcessThread;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.lib.SchedulePlotter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**

 * Top-level director for PTIDES models. The model time is the
 * simulated global physical time.
 * 
 * <p>
 * Top-level actors in a PTIDES domain are platforms. A platform is a
 * composite actor that contains sensors, actuators, computation
 * actors with worst case execution times and model time delay actors.
 * 
 * <p>
 * The PTIDES director simulates the parallel execution of actors on
 * distributed platforms, thus the composite actors representing
 * platforms run in threads. A platform executes actors in model time
 * and maps some actions to real time.  For those mappings, platforms
 * call the fireAt method of this director to schedule refiring at
 * those real times. A mapping to real time is done for
 *
 * <ul>
 * <li> sensors. A sensor is fired at model time = real time. </li>
 * <li> actuators. An actuator is fired at real time = model time. </li>
 * <li> execution of actors with WCET > 0. After starting the
 * execution of an actor with WCET > 0, the platform thread sleeps and
 * continues execution after real time was increased by the WCET or
 * the actor is being preempted. </li>
 * </ul>
 * 
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PtidesDirector extends CompositeProcessDirector implements
        TimedDirector {

    /**
     * Construct a director in the default workspace with an empty
     * string as its name. The director is added to the list of
     * objects in the workspace.  Increment the version number of the
     * workspace.
     * 
     * @exception IllegalActionException If the name contains a
     *                    period, or if the director is not compatible
     *                    with the specified container.
     * @exception NameDuplicationException
     *                    If the container not a CompositeActor and the name
     *                    collides with an entity in the container.
     */
    public PtidesDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _initialize();
    }

    /**
     * Construct a director in the workspace with an empty name. The
     * director is added to the list of objects in the
     * workspace. Increment the version number of the workspace.
     * 
     * @param workspace The workspace of this object.
     * @exception IllegalActionException If the name contains a
     * period, or if the director is not compatible with the specified
     * container.
     * @exception NameDuplicationException If the container not a
     * CompositeActor and the name collides with an entity in the
     * container.
     */
    public PtidesDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);

        _initialize();
    }

    /**
     * Construct a director in the given container with the given
     * name. If the container argument must not be null, or a
     * NullPointerException will be thrown. If the name argument is
     * null, then the name is set to the empty string. Increment the
     * version number of the workspace.
     * 
     * @param container The container.
     * @param name Name of this director.
     * @exception IllegalActionException If the name contains a
     * period, or if the director is not compatible with the specified
     * container.
     * @exception NameDuplicationException If the container not a
     * CompositeActor and the name collides with an entity in the
     * container.
     */
    public PtidesDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _initialize();
    }

    /**
     * If this Parameter is set to true, minimum delays according to
     * Ptides on the platform level are calculated. Otherwise, given
     * minimum delays are used.
     */
    public Parameter calculateMinDelays;

    /**
     * Global clock synchronization error.
     */
    public Parameter clockSyncError;

    /**
     * Global network delay.
     * 
     * FIXME In future developments, network delays could be specified per
     * network and not globally.
     */
    public Parameter networkDelay;

    /**
     * Defines if the Ptides execution strategy should be used. This
     * is only interesting when other distributed event simulations
     * should be tried with this framework.
     */
    public Parameter usePtidesExecutionSemantics;

    /**
     * Time at which the simulation should be stopped.
     */
    public Parameter stopTime;

    /**
     * Add a new schedule listener that will receive events in the
     * _displaySchedule method.
     * 
     * @param plotter
     *                The schedule plotter that will be added as a listener.
     */
    public void addScheduleListener(SchedulePlotter plotter) {
        _scheduleListeners.add(plotter);
    }

    /**
     * Override the base class to update local variables.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == clockSyncError) {
            _clockSyncError = ((DoubleToken) clockSyncError.getToken())
                    .doubleValue();
        } else if (attribute == networkDelay) {
            _networkDelay = ((DoubleToken) networkDelay.getToken())
                    .doubleValue();
        } else if (attribute == usePtidesExecutionSemantics) {
            _usePtidesExecutionSemantics = ((BooleanToken) usePtidesExecutionSemantics
                    .getToken()).booleanValue();
        } else if (attribute == calculateMinDelays) {
            _calculateMinDelays = ((BooleanToken) calculateMinDelays.getToken())
                    .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Return the physical time.
     * 
     * @see #setModelTime(Time)
     */
    public synchronized Time getModelTime() {
        return _currentTime;
    }

    /**
     * Initialize parameters andthe schedule plotters. Calculate minimum delays
     * for ports on platforms according to Ptides.
     * 
     * @see #getModelTime()
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _completionTime = Time.POSITIVE_INFINITY;

        // _nextFirings = new TreeSet<Time>();
        _completionTime = new Time(this, ((DoubleToken) stopTime.getToken())
                .doubleValue());
        if (!_completionTime.equals(Time.POSITIVE_INFINITY)) {
            _nextFirings.add(_completionTime);
        }

        if (_calculateMinDelays) {
            PtidesGraphUtilities utilities = new PtidesGraphUtilities(this
                    .getContainer());
            utilities.calculateMinDelays();

        }

        Hashtable<Actor, List> table = new Hashtable<Actor, List>();
        for (Iterator it = ((CompositeActor) getContainer()).entityList()
                .iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof CompositeActor) {
                CompositeActor actor = (CompositeActor) obj;
                if (actor.getDirector() instanceof PtidesEmbeddedDirector) {
                    PtidesEmbeddedDirector dir = (PtidesEmbeddedDirector) actor
                            .getDirector();
                    dir
                            .setUsePtidesExecutionSemantics(_usePtidesExecutionSemantics);
                    dir.setClockSyncError(_clockSyncError);
                    dir.setNetworkDelay(_networkDelay);
                }
                List<Actor> actors = new ArrayList<Actor>();
                for (Iterator it2 = actor.entityList().iterator(); it2
                        .hasNext();) {
                    Object o = it2.next();
                    if (o instanceof Actor) {
                        actors.add((Actor) o);
                    }
                }
                table.put(actor, actors);
            }
        }
        synchronized (this) {
            if (_scheduleListeners != null) {
                Iterator listeners = _scheduleListeners.iterator();

                while (listeners.hasNext()) {
                    ((ScheduleListener) listeners.next()).initialize(table);
                }
            }
        }

    }

    /**
     * Called by platforms to schedule a future time firing. This director does
     * not remember which platform wants to be fired again.
     * 
     * FIXME If the director remembers which platform asked to be refired,
     * performance improvements can be made.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        if (time.compareTo(getModelTime()) > 0) {
            _nextFirings.add(time);
        }
    }

    /**
     * Return a new PtidesEReceiver.
     */
    public Receiver newReceiver() {
        PtidesReceiver receiver = new PtidesReceiver();
        return receiver;
    }

    /**
     * Notify all waiting threads. The threads decide themselves if they have
     * anything to do.
     */
    public void notifyWaitingThreads() {
        try {
            Set set = (Set) _waitingPlatforms.clone();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Thread thread = (Thread) it.next();
                if (_debugging) {
                    _debug("unblock: " + thread.getName() + " ");
                }
                threadUnblocked(thread, null);
            }
            _waitingPlatforms.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
            // concurrent modification exceptions can occur here
        }
    }

    /**
     * Set physical time for this model.
     * 
     * @see #getModelTime
     */
    public synchronized void setModelTime(Time newTime)
            throws IllegalActionException {
        _currentTime = newTime;
    }

    /**
     * If a platform has nothing to do at the current physical time,
     * it waits for the next physical time which means that the
     * platform thread is blocked. If all threads are about to be
     * blocked, the physical time is increased to the next physical
     * time any platform is interested in being fired again.
     * 
     * @return The new physical time.
     * @throws IllegalActionException
     *                 Thrown if physical time cannot be increased.
     */
    public synchronized Time waitForFuturePhysicalTime()
            throws IllegalActionException {
        if (_debugging) {
            _debug("wait for "
                    + ((ProcessThread) Thread.currentThread()).getActor()
                            .getName() + ", number of active threads: "
                    + _getActiveThreadsCount()
                    + ", number of blocked threads: "
                    + _getBlockedThreadsCount()
                    + ", number of waiting threads: "
                    + _waitingPlatforms.size());
        }
        if ((!_waitingPlatforms.contains(Thread.currentThread())
                        && _getActiveThreadsCount()
                        - _waitingPlatforms.size() == 1)) {
            _increasePhysicalTime();
            return getModelTime();
        }
        if (_stopFireRequested) {
            return getModelTime();
        }
        _waitingPlatforms.add(Thread.currentThread());
        threadBlocked(Thread.currentThread(), null);
        try {
            workspace().wait(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!_stopRequested) {
            _waitingPlatforms.remove(Thread.currentThread());
        }
        return getModelTime();
    }

    /**
     * Clear list containing times platforms are interested in being
     * fired in the future clear list of actors waiting for being
     * re-fired reset physical time.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _waitingPlatforms.clear();
        _nextFirings.clear();
        setModelTime(new Time(this, 0.0));
    }

    /**
     * Forward display events from platforms to the schedule listeners.
     * 
     * @param node platform that forwards the event.
     * @param actor actor inside a platform for which the event was
     * created.  If the actor is null, the event is a platform event,
     * e.g.  input ports read or output ports written.
     * @param time physical time at which the event occurred.
     * @param scheduleEvent type of event.
     */
    protected final void _displaySchedule(Actor node, Actor actor, double time,
            int scheduleEvent) {
        if (_scheduleListeners != null) {
            Iterator listeners = _scheduleListeners.iterator();

            while (listeners.hasNext()) {
                ((ScheduleListener) listeners.next()).event(node, actor, time,
                        scheduleEvent);
            }
        }
    }

    /**
     * Creates a new thread for a platform. A platform is a composite
     * actor at the top level of the model.
     * 
     * @param actor The Composite actor that represents a platform.
     * @param director The process director.
     * @return Return a new process thread.
     */
    protected ProcessThread _newProcessThread(Actor actor,
            ProcessDirector director) {
        return new ProcessThread(actor, director);
    }

    /**
     * Deadlocks can occur due to read or write accesses to the
     * workspace. In case of a deadlock, wake up all threads. They
     * will decide themselves if they have anything to do at current
     * physical time.
     */
    protected synchronized boolean _resolveDeadlock()
            throws IllegalActionException {
        if (_debugging) {
            _debug("resolveDeadlock");
        }
        notifyWaitingThreads();
        return true;
    }

    /**
     * Increase physical time to next time that any of the platforms
     * is interested in doing something.
     * 
     * @throws IllegalActionException
     */
    private synchronized void _increasePhysicalTime()
            throws IllegalActionException {
        if (_nextFirings.size() == 0) {
            return;
        }
        Time time = _nextFirings.first();
        _nextFirings.remove(time);
        if (time.compareTo(_completionTime) > 0) {
            // stopFire();
            stop();
            return;
        }
        _currentTime = time;
        if (_debugging) {
            _debug("physical time " + time + " set by "
                    + Thread.currentThread().getName());
        }
        notifyWaitingThreads();
    }

    /**
     * Initialize parameters of the director.
     * 
     * @throws NameDuplicationException
     *                 Could occur if parameter with same name already exists.
     */
    private void _initialize() throws IllegalActionException,
            NameDuplicationException {
        double value = Double.MAX_VALUE;
        stopTime = new Parameter(this, "stopTime", new DoubleToken(value));
        timeResolution.setVisibility(Settable.FULL);

        try {
            clockSyncError = new Parameter(this, "clockSyncError");
            clockSyncError.setExpression("0.1");
            clockSyncError.setTypeEquals(BaseType.DOUBLE);

            networkDelay = new Parameter(this, "networkDelay");
            networkDelay.setExpression("0.1");
            networkDelay.setTypeEquals(BaseType.DOUBLE);

            usePtidesExecutionSemantics = new Parameter(this,
                    "usePtidesExecutionSemantics");
            usePtidesExecutionSemantics.setExpression("true");
            usePtidesExecutionSemantics.setTypeEquals(BaseType.BOOLEAN);

            calculateMinDelays = new Parameter(this, "calculateMinDelays");
            calculateMinDelays.setExpression("true");
            calculateMinDelays.setTypeEquals(BaseType.BOOLEAN);
        } catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
    }

    /**
     * If true, minimum delays for the ports in the model are calculated.
     */
    private boolean _calculateMinDelays;

    /**
     * Global clock sychronization error.
     */
    private double _clockSyncError;

    /**
     * The completion time. Since the completionTime is a constant, we do not
     * convert it to a time object.
     */
    private Time _completionTime;

    /**
     * List of times that platforms want to be re-fired.
     */
    private TreeSet<Time> _nextFirings = new TreeSet<Time>();

    /**
     * The global network delay.
     */
    private double _networkDelay;

    /**
     * registered schedule listeners, this is used for the schedule plotter
     */
    private Collection<ScheduleListener> _scheduleListeners = new LinkedList<ScheduleListener>();

    /**
     * If true, minimum delays according to Ptides should be used.
     */
    private boolean _usePtidesExecutionSemantics;

    /**
     * List of threads (=platforms) that are idle at current time and want to be
     * re-fired in the future.
     */
    private HashSet<Thread> _waitingPlatforms = new HashSet<Thread>();

}
