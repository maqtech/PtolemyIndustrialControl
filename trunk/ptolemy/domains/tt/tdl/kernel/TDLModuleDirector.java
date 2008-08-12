package ptolemy.domains.tt.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.ModalDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * Director for a TDL module.
 * 
 * Schedule is generated statically in the initialize method after setting
 * output ports and actuators and after each slot in the schedule, the control
 * is handed to the outside director to enable executing other actors outside
 * the TDL module.
 * 
 * @author Patricia Derler
 */
public class TDLModuleDirector extends ModalDirector {

    /**
     * Construct a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     * 
     * @param container
     *            Container of this director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public TDLModuleDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * pick the next action in the schedule and execute it leave the fire method -
     * if mode switch has to be made - before input ports are read and updated -
     * every part of the schedule for the current time was executed (= a slot in
     * the schedule was executed).
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Firing " + getFullName(), " at time " + getModelTime());
        }
        _currentWCET = 0;
        if (!_executeNow()) {
            return;
        }
        
        boolean continueSchedule = true;
        getController().readInputs();

        // if (_debugging)
        _printStatus();

        int i = -1; // used to avoid returning control to the executive director
        // before every input port update
        for (; _currentSchedule.currentPositionInSlot < _schedule.size(); _currentSchedule.currentPositionInSlot++) {
            i++;
            // if (_debugging)
            System.out.print(_currentSchedule.currentPositionInSlot);
            Object obj = _schedule.get(_currentSchedule.currentPositionInSlot);
            if (!_hasGuard((NamedObj) obj) || _guardIsTrue((NamedObj) obj)) {
                if (obj instanceof Actor) {
                    Actor actor = (Actor) obj;
                    if (actor.prefire()) { // TODO prefire should never return
                        // false ?
                        actor.iterate(1);
                        actor.postfire();
                        _currentWCET = getWorstCaseET(actor);
                        if (_currentWCET > 0)
                            break;
                    } else {
                        System.out.println(actor.getName()
                                + " .prefire() = false");
                    }
                } else if (obj instanceof TypedIOPort) {
                    IOPort port = (IOPort) obj;
                    if (_isActuator(port)) {
                        _updateActuator(port);
                    } else if (port.isOutput()) {
                        // skip updating of output ports if the module is
                        // executed the first time
                        // or after a mode switch
                        // only if task is not a fast task
                        if (_currentSchedule.firstSlot
                                && !TDLModuleDirector.isFast(port
                                        .getContainer()))
                            continue;
                        _updateOutputPort(port);
                        _currentWCET = 0.0;
                    } else if (port.isInput()) {
//                        Director executiveDirector = ((Actor) getContainer()
//                                .getContainer()).getDirector();
                        if (i > 0) {
                            fireAt((TDLModule) getContainer(), getModelTime());
                            return;
                        }
                        i--;
                        _updateInputPort(port);
                    }
                } else if (obj instanceof Transition) {
                    // skip mode switches if mode is executed the first time or
                    // immediately after
                    // a mode switch
                    if (_currentSchedule.firstSlot)
                        continue;
                    if (!_chooseTransition((Transition) obj))
                        break;
                } else {
                    throw new IllegalArgumentException(obj
                            + " cannot be executed.");
                }
            }
        }

        // set values for next firing
        if (continueSchedule) {
            _currentSchedule.lastFiredAt = getModelTime().getLongValue();
            _currentSchedule.firstSlot = false;
            long nextTimeStamp = _getNextTimeStamp();
            Time t = new Time(this,
                    (getModelTime().getLongValue() + nextTimeStamp));
            fireAt((TDLModule) getContainer(), t);
            _currentSchedule.nextFireTime = t.getLongValue();
            _currentSchedule.currentPositionInSlot = 0;
            ((TDLModeSchedule) _modeSchedules.get(getController()
                    .currentState())).currentScheduleTime = (((TDLModeSchedule) _modeSchedules
                    .get(getController().currentState())).currentScheduleTime + nextTimeStamp)
                    % ((TDLModeSchedule) _modeSchedules.get(getController()
                            .currentState())).modePeriod;
        }
    }
    
//    public void fire() throws IllegalActionException {
//        _currentWCET = 0;
//        Time time = getModelTime();
//        List actions = (List) _eventQueue.get(time);
//        for (Iterator it = _sensorEventQueues.keySet().iterator(); it.hasNext(); ) {
//            HashMap eventQueue = (HashMap) it.next();
//            actions.add(eventQueue.get(time));
//        }
//        if (actions.size() == 0) // nothing to do
//            return;
//        else {
//            // TODO sort must also consider fast tasks
//            Collections.sort(actions, new TDLAction.TDLActionComparator());
//            for (Iterator it = actions.iterator(); it.hasNext(); ) {
//                TDLAction action = (TDLAction) it.next();
//                if (!_hasGuard((NamedObj) action.object) || _guardIsTrue((NamedObj) action.object)) {
//                    switch (action.actionType) {
//                        case TDLAction.WRITEOUTPUT:
//                            _updateOutputPort((IOPort) action.object);
//                            _currentWCET = 0.0;
//                            break;
//                        case TDLAction.WRITEACTUATOR:
//                            _updateActuator((IOPort) action.object);
//                            break;
//                        case TDLAction.MODESWITCH:
//                            if (_chooseTransition((Transition) action.object)) {
//                                _eventQueue.clear();
//                                _sensorEventQueues.clear();
//                                _computeInitialEvents(((Transition) action.object).g)
//                            }
//                            break;
//                        case TDLAction.READSENSOR:
//                            break;
//                        case TDLAction.READINPUT:
//                            Director executiveDirector = ((Actor) getContainer()
//                                    .getContainer()).getDirector();
//                            if (i > 0) {
//                                fireAt((TDLModule) getContainer(), getModelTime());
//                                return;
//                            }
//                            i--;
//                            _updateInputPort((IOPort) action.object);
//                            break;
//                        case TDLAction.EXECUTETASK:
//                            Actor actor = (Actor) action.object;
//                            if (actor.prefire()) { // TODO prefire should never return
//                                // false ?
//                                actor.iterate(1);
//                                actor.postfire();
//                                _currentWCET = getWorstCaseET(actor);
//                                if (_currentWCET > 0)
//                                    break;
//                            } else {
//                                System.out.println(actor.getName()
//                                        + " .prefire() = false");
//                            }
//                            break;
//                    }
//                }
//            }
//        }
//    }

    /**
     * Get mode period from state parameter "period".
     * 
     * @param obj
     *            The object
     * @return The value of the "period" parameter. If there is no period
     *         parameter or it cannot be converted to a double, then return 1.0.
     */
    public static double getModePeriod(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("period");

            if (parameter != null) {
                DoubleToken intToken = (DoubleToken) parameter.getToken();

                return intToken.doubleValue();
            } else {
                return 1;
            }
        } catch (ClassCastException ex) {
            return 1;
        } catch (IllegalActionException ex) {
            return 1;
        }
    }

    /**
     * Initialize the director, calculate schedule and schedule first firing.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _resetReceivers();
        _graph = new TDLActionsGraph(((TDLModule)getContainer()), getController());
        // this does some initializations in TDL task and transition like computing the LET
        _graph.buildGraph(getController().currentState());
        _buildSchedule();
        _initializeOutputPorts();
        _currentSchedule = (TDLModeSchedule) _modeSchedules.get(getController()
                .currentState());
        if (_currentSchedule == null)
            return;
        fireAt((TDLModule) getContainer(), getModelTime());
        _currentSchedule.firstSlot = true;
    }

    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     * 
     * @param actor
     *            The actor for which the worst case execution time is
     *            requested.
     * @return The worst case execution time.
     */
    public static double getWorstCaseET(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("WCET");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return 0.0;
            }
        } catch (ClassCastException ex) {
            return 0.0;
        } catch (IllegalActionException ex) {
            return 0.0;
        }
    }

    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     * 
     * @return The worst case execution time.
     */
    public double getWCET() {
        return _currentWCET;
    }

    /**
     * Find out if task (=actor) or actuator (=output port) is fast task.
     * 
     * @param obj
     *            The object that could be a fast task or actuator.
     * @return True if it is a fast task.
     */
    public static boolean isFast(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("fast");

            if (parameter != null) {
                BooleanToken intToken = (BooleanToken) parameter.getToken();
                return intToken.booleanValue();
            } else {
                return false;
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
    }

    /**
     * Return a new TDLReceiver.
     * 
     * @return A new TDL receiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new TDLReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /**
     * Check if at the current time there is something to do.
     * 
     * @return True if there is something to do now.
     * @throws IllegalActionException
     *             Thrown if execution was missed, input ports could not be
     *             transferred or by parent class.
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && _executeNow();
    }

    /**
     * Outputs are only transferred when scheduled, therefore do nothing if
     * transfer outputs is called by another actor.
     * 
     * @param port
     *            output port.
     * @return True.
     */
    public boolean transferOutputs(IOPort port) {
        return true;
    }

    /**
     * Get all inputs for a port.
     * 
     * @param port
     *            Input port.
     * @return True if ports transferred inputs.
     * @throws IllegalActionException
     *             Thrown if inputs are about to be transferred for a non opaque
     *             input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }
        boolean wasTransferred = false;
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);
                        if (_debugging) {
                            _debug(getName(), "transferring input from "
                                    + port.getName());
                        }
                        port.sendInside(i, t);
                        wasTransferred = true;
                    }
                } else {
                    // No inside connection to transfer tokens to.
                    // In this case, consume one input token if there is one.
                    if (_debugging) {
                        _debug(getName(), "Dropping single input from "
                                + port.getName());
                    }
                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }
    


    /**
     * Build the schedule for the TDL module by reading all model elements.
     * 
     * @throws IllegalActionException
     */
    private void _buildSchedule() throws IllegalActionException {
        Iterator stateIterator = getController().entityList().iterator();
        _modeSchedules = new HashMap();
        while (stateIterator.hasNext()) {
            State state = (State) stateIterator.next();
            TDLModeScheduler scheduler = new TDLModeScheduler();
            try {
                double modePeriod = getModePeriod(state);
                scheduler.setModePeriod(modePeriod, getTimeResolution());

                // get transitions
                for (Iterator transitionIterator = state
                        .nonpreemptiveTransitionList().iterator(); transitionIterator
                        .hasNext();) {
                    TDLTransition transition = (TDLTransition) transitionIterator
                            .next();
                    scheduler.addModeSwitch(transition);
                }
                Refinement refinement = (Refinement) state.getRefinement()[0];

                // get tasks (regular and fast tasks)
                Iterator taskIterator = refinement.entityList().iterator();
                while (taskIterator.hasNext()) {
                    Actor actor = (Actor) taskIterator.next();

                    // get sensors
                    // List sensors = null;
                    // for (Iterator inputIt = actor.inputPortList().iterator();
                    // inputIt.hasNext(); ) {
                    // IOPort port = (IOPort) inputIt.next();
                    // sensors = port.connectedPortList();
                    // sensors.retainAll(((TDLModule)this.getContainer()).inputPortList());
                    // }

                    // add fast task and connected fast actuators
                    if (TDLModuleDirector.isFast((NamedObj) actor)) {
                        ArrayList fastActuators = new ArrayList();
                        for (Iterator outputIt = actor.outputPortList()
                                .iterator(); outputIt.hasNext();) {
                            IOPort port = (IOPort) outputIt.next();
                            // List l = port.connectedPortList();
                            Receiver[][] channelArray = port
                                    .getRemoteReceivers();
                            for (int i = 0; i < channelArray.length; i++) {
                                Receiver[] receiverArray = channelArray[i];
                                for (int j = 0; j < receiverArray.length; j++) {
                                    TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                                    if (receiver.getContainer() instanceof RefinementPort) {
                                        IOPort refinementPort = receiver
                                                .getContainer();
                                        if (TDLModuleDirector
                                                .isFast(refinementPort)) {
                                            fastActuators.add(refinementPort);
                                        }
                                    }
                                }
                            }
                        }
                        scheduler.addFastTask(/*sensors, */actor,
                                fastActuators);
                    } else {
                        scheduler.addTask(/*sensors, */actor);
                    }
                }

                // get regular actuators
                Iterator portIterator = refinement.outputPortList().iterator();
                while (portIterator.hasNext()) {
                    IOPort port = (IOPort) portIterator.next();
                    if (!isFast(port))
                        scheduler.addActuator(port);
                }

                // compute schedule
                TDLModeSchedule schedule = scheduler.getModeSchedule();
                if (schedule == null)
                    return;
                HashMap modeSchedule = schedule.modeSchedule;
                _modeSchedules.put(state, schedule);
                _printSchedule(modeSchedule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if transition (=mode switch) should be executed.
     * 
     * @param transition
     *            The mode switch.
     * @return True if the mode switch should be done.
     * @throws IllegalActionException
     *             If an error occurs during the transition.
     */
    private boolean _chooseTransition(Transition transition)
            throws IllegalActionException {
        _updateInputs();

        // update sensors
        Refinement ref = (Refinement) getController().currentState()
                .getRefinement()[0];

        if (transition.isEnabled()) {
            if (_debugging)
                System.out.println(transition.getGuardExpression());
            getController().setLastChosenTransition(transition);
            _transferTaskInputs(transition);
            fireAt((TDLModule) getContainer(), getModelTime());
            _currentSchedule.firstSlot = true;
            ((TDLModeSchedule) _modeSchedules
                    .get(transition.destinationState())).firstSlot = true;
            ((TDLModeSchedule) _modeSchedules
                    .get(transition.destinationState())).nextFireTime = getModelTime()
                    .getLongValue();
            _currentSchedule.currentPositionInSlot = 0;
            _currentSchedule.currentScheduleTime = 0;
            return false;
        }
        return true;
    }

//    private void _enqueueEvent(Time timestamp, TDLAction action) {
//        ArrayList l = (ArrayList) _eventQueue.get(timestamp);
//        if (l == null) {
//            l = new ArrayList();
//            _eventQueue.put(timestamp, l);
//        }
//        l.add(action);
//    }
    
//    private void _enqueueSensorEvent(IOPort sensor, Time timestamp, TDLAction action) {      
//        HashMap map = (HashMap) _sensorEventQueues.get(sensor);
//        if (map == null) {
//            map = new HashMap();
//            _sensorEventQueues.put(timestamp, map);
//        }
//        ArrayList l = (ArrayList) map.get(timestamp);
//        if (l == null) {
//            l = new ArrayList();
//            _eventQueue.put(timestamp, l);
//        }
//        l.add(action);
//    }

    /**
     * Check if there is something to do now.
     * 
     * @return True if an action is scheduled to be executed now.
     * @throws IllegalActionException
     *             Thrown if input ports could not be transferred or an
     *             execution was missed.
     */
    private boolean _executeNow() throws IllegalActionException {
        FSMActor controller = getController();
        State state = controller.currentState();

        // read inputs although they are not used to avoid piling up tokens
        for (Iterator it = ((TDLModule) getContainer()).inputPortList()
                .iterator(); it.hasNext();) {
            IOPort port = (IOPort) it.next();
            transferInputs(port);
        }

        _currentSchedule = (TDLModeSchedule) _modeSchedules.get(state);

        long currentTime = getModelTime().getLongValue();

        if (_currentSchedule.firstSlot)
            _currentSchedule.currentScheduleTime = 0;
        if (getModelTime().getLongValue() < _currentSchedule.nextFireTime)
            return false; // already executed
        else if (getModelTime().getLongValue() > _currentSchedule.nextFireTime)
            throw new IllegalActionException("missed execution");

        _schedule = (ArrayList) _currentSchedule.modeSchedule
                .get(_currentSchedule.currentScheduleTime);
        if (_schedule == null || _schedule.size() == 0) {
            return false; // nothing to do here
        }

        if (_currentSchedule.lastFiredAt == currentTime
                && _schedule.size() == _currentSchedule.currentPositionInSlot)
            return false; // already exectued everything that is in this slot

        return true;
    }

    /**
     * Get time for next execution of module.
     * 
     * @return The time for the next execution.
     * @throws IllegalActionException
     *             Thrown if the mode controller could not be retrieved.
     */
    private long _getNextTimeStamp() throws IllegalActionException {
        SortedSet keys = new TreeSet(((TDLModeSchedule) _modeSchedules
                .get(getController().currentState())).modeSchedule.keySet());
        Iterator it = keys.iterator();
        long currentScheduleTime = ((TDLModeSchedule) _modeSchedules
                .get(getController().currentState())).currentScheduleTime;
        long currentModePeriod = ((TDLModeSchedule) _modeSchedules
                .get(getController().currentState())).modePeriod;
        while (it.hasNext()) {
            long d = (Long) it.next();
            if (d == currentScheduleTime) {
                if (it.hasNext())
                    return ((Long) it.next()) - d;
                else
                    return currentModePeriod - d;
            }
        }
        return -1;
    }

    /**
     * Test a guard expression.
     * 
     * @param obj
     *            The object containing a guard expression.
     * @return True if the guard expression evaluates to true.
     * @throws IllegalActionException
     *             Thrown if guard expression could not be read.
     */
    private boolean _guardIsTrue(NamedObj obj) throws IllegalActionException {
        _updateInputs();

        Parameter parameter = (Parameter) obj.getAttribute("guard");
        StringToken token = (StringToken) parameter.getToken();
        ParseTreeEvaluator parseTreeEvaluator = getParseTreeEvaluator();
        FSMActor fsmActor = getController();
        ASTPtRootNode _guardParseTree = null;
        String expr = token.stringValue();

        // Parse the guard expression.
        PtParser parser = new PtParser();
        try {
            _guardParseTree = parser.generateParseTree(expr);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to parse guard expression \"" + expr + "\"");
        }
        Token tok = parseTreeEvaluator.evaluateParseTree(_guardParseTree,
                fsmActor.getPortScope());
        return ((BooleanToken) tok).booleanValue();
    }

    /**
     * Test if an object has a guard expression.
     * 
     * @param obj
     *            Object that might have a guard expression.
     * @return True if the object has a guard parameter.
     */
    private boolean _hasGuard(NamedObj obj) {
        Parameter parameter = (Parameter) obj.getAttribute("guard");
        return (parameter != null);
    }

    /**
     * Initialize a port with an initial token.
     * 
     * @param port
     *            Port to be initialized.
     * @throws IllegalActionException
     *             Thrown if the initial value parameter could not be read.
     */
    private void _initializePort(IOPort port) throws IllegalActionException {
        Parameter initialValueParameter = (Parameter) ((NamedObj) port)
                .getAttribute("initialValue");
        Token token;
        if (initialValueParameter != null)
            token = initialValueParameter.getToken();
        else
            token = new IntToken(0);
        Receiver[][] channelArray = port.getRemoteReceivers();
        for (int i = 0; i < channelArray.length; i++) {
            Receiver[] receiverArray = channelArray[i];
            for (int j = 0; j < receiverArray.length; j++) {
                TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                receiver.init(token);
            }
        }
    }

    /**
     * Get all tasks for a module.
     * 
     * @return A list of all tasks.
     */
    private Collection _getAllTasks() {
        Collection tasks = new ArrayList();
        Iterator it = ((TDLModule) getContainer()).entityList().iterator();
        while (it.hasNext()) {
            Object object = it.next();
            if (object instanceof Refinement) {
                Refinement refinement = (Refinement) object;
                Iterator entIt = refinement.entityList().iterator();
                while (entIt.hasNext()) {
                    Actor task = (Actor) entIt.next();
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * Initialize output ports by reading initial value and initializing the
     * receivers.
     * 
     * @throws IllegalActionException
     *             Thrown if the ports could not be initialized.
     */
    private void _initializeOutputPorts() throws IllegalActionException {

        Iterator it = _getAllTasks().iterator();
        while (it.hasNext()) {
            Actor task = (Actor) it.next();
            if (!isFast((NamedObj) task)) {
                Iterator portIterator = task.outputPortList().iterator();
                while (portIterator.hasNext()) {
                    IOPort port = (IOPort) portIterator.next();
                    _initializePort(port);
                }
            }
        }

        // transfer outputs
        _updateReceivers(((TDLModule) getContainer()).outputPortList());
        getController().readOutputsFromRefinement();

        // transfer inputs
        _updateReceivers(((TDLModule) getContainer()).inputPortList());
        getController().readInputs();

        // init actuators
        Iterator portIterator = getController().outputPortList().iterator();
        while (portIterator.hasNext()) {
            IOPort port = (IOPort) portIterator.next();
            if (!isFast(port)) {
                _initializePort(port);
            }
        }

        // transfer outputs
        _updateReceivers(((TDLModule) getContainer()).outputPortList());
        getController().readOutputsFromRefinement();
    }

    /**
     * Returns true if the port is an actuator. Only RefinementPorts can be
     * actuators.
     * 
     * @param port
     *            Port that might be an actuator.
     * @return True if the port is an actuator.
     */
    private boolean _isActuator(IOPort port) {
        if (port instanceof RefinementPort)
            return true;
        return false;
    }

    /**
     * Update actuator by transferring the outputs.
     * 
     * @param port
     *            Actuator that should be updated.
     * @throws IllegalActionException
     *             Thrown if outputs could not be transferred.
     */
    private void _updateActuator(IOPort port) throws IllegalActionException {
        RefinementPort rport = (RefinementPort) port;
        List l = rport.deepConnectedOutPortList();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) instanceof TypedIOPort)
                super.transferOutputs((TypedIOPort) l.get(i));
        }
    }

    /**
     * Update input port, for TDL that means a sensor value is read.
     * 
     * @param port
     *            Port to be updated.
     */
    private void _updateInputPort(IOPort port) {
        Receiver[][] channelArray = port.getReceivers();
        for (int i = 0; i < channelArray.length; i++) {
            Receiver[] receiverArray = channelArray[i];
            for (int j = 0; j < receiverArray.length; j++) {
                TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                receiver.update();
            }
        }
    }

    /**
     * Read input values and update inputMap the updated inputMap is required
     * when guards are evaluated.
     * 
     * @throws IllegalActionException
     *             Thrown if the controller could not be retrieved or inputs
     *             could not be read.
     */
    private void _updateInputs() throws IllegalActionException {
        Iterator it = ((TDLModule) getContainer()).inputPortList().iterator();
        while (it.hasNext()) {
            IOPort port = (IOPort) it.next();
            Receiver[][] channelArray = port.deepGetReceivers();
            for (int i = 0; i < channelArray.length; i++) {
                Receiver[] receiverArray = channelArray[i];
                for (int j = 0; j < receiverArray.length; j++) {
                    TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                    receiver.update();
                }
            }
        }
        getController().readInputs();
    }

    /**
     * Update output port, for TDL this means an actuator is updated.
     * 
     * @param port
     *            The output port.
     * @throws IllegalActionException
     *             Thrown if output ports from refinement could not be read.
     */
    private void _updateOutputPort(IOPort port) throws IllegalActionException {
        Receiver[][] channelArray = port.getRemoteReceivers();
        for (int i = 0; i < channelArray.length; i++) {
            Receiver[] receiverArray = channelArray[i];

            for (int j = 0; j < receiverArray.length; j++) {
                TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                receiver.update();
            }
        }
        getController().readOutputsFromRefinement();
    }

    /**
     * Only for debugging purposes, prints the schedule.
     * 
     * @param modeSchedule
     *            The mode schedule to be printed.
     */
    private void _printSchedule(HashMap modeSchedule) {
        System.out.println("--- " + getContainer().getName() + " ---");
        SortedSet set = new TreeSet(modeSchedule.keySet());
        Iterator it = set.iterator();
        while (it.hasNext()) {
            long time = (Long) it.next();
            ArrayList list = (ArrayList) modeSchedule.get(time);
            System.out.print(time + ": ");
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j) instanceof TypedCompositeActor) {
                    CompositeActor comp = (CompositeActor) list.get(j);
                    System.out.print(comp.getName());
                } else if (list.get(j) instanceof TypedIOPort) {
                    IOPort port = (IOPort) list.get(j);
                    System.out.print(port.getName());
                } else if (list.get(j) instanceof Transition) {
                    Transition transition = (Transition) list.get(j);
                    System.out
                            .print(transition.guardExpression.getExpression());
                } else
                    System.out.print("unk");
                System.out.print(" ");
            }
            System.out.println();
        }

    }

    /**
     * Only for debugging purposes, prints the current status of the TDL module.
     * 
     * @throws IllegalActionException
     *             Thrown if controller could not be retrieved.
     */
    private void _printStatus() throws IllegalActionException {
        double d = getModelTime().getDoubleValue();
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter
                .format(
                        "%1$5f state %2$10s schedTime %3$2d of %4$2d schedPos %5$2d of %6$2d",
                        new Object[] { Double.valueOf(d),
                                getController().currentState().getName(),
                                _currentSchedule.currentScheduleTime,
                                _currentSchedule.modeSchedule.size(),
                                _currentSchedule.currentPositionInSlot,
                                _schedule.size() });
        System.out.println();
        System.out.print(sb + " ");
    }

    private void _resetReceivers() {
        ListIterator receivers = _receivers.listIterator();
        while (receivers.hasNext()) {
            TDLReceiver receiver = (TDLReceiver) receivers.next();
            if (receiver.getContainer() != null) {
                receiver.reset();
            } else {
                receivers.remove();
            }
        }
    }

    private void _updateReceivers(Collection portList) {
        Iterator it = portList.iterator();
        while (it.hasNext()) {
            IOPort port = (IOPort) it.next();
            // super.transferOutputs(port);
            Receiver[][] channelArray = port.deepGetReceivers();
            for (int i = 0; i < channelArray.length; i++) {
                Receiver[] receiverArray = channelArray[i];
                for (int j = 0; j < receiverArray.length; j++) {
                    TDLReceiver receiver = (TDLReceiver) receiverArray[j];
                    receiver.update();
                }
            }
        }
    }

    /**
     * After a mode switch, tasks that exist in the source and the target state
     * must have the same port values. This method transfers input ports.
     * 
     * @param transition
     *            Mode switch that has been made.
     * @throws IllegalActionException
     *             If refinement or Controller could not be retrieved.
     */
    private void _transferTaskInputs(Transition transition)
            throws IllegalActionException {
        Refinement oldRefinement = (Refinement) getController().currentState()
                .getRefinement()[0];
        Refinement newRefinement = (Refinement) transition.destinationState()
                .getRefinement()[0];

        List oldTasks = oldRefinement.entityList();
        List newTasks = newRefinement.entityList();

        for (int i = 0; i < newTasks.size(); i++) {
            Actor actor = (Actor) newTasks.get(i);
            for (int j = 0; j < oldTasks.size(); j++) {
                Actor oldActor = (Actor) oldTasks.get(j);
                if (actor.getName().equals(oldActor.getName())) {
                    // same actor -> copy input ports from oldActor to newActor
                    // TODO: check if really same actor
                    for (int k = 0; k < oldActor.inputPortList().size(); k++) {
                        IOPort port = (IOPort) oldActor.inputPortList().get(k);
                        IOPort newPort = (IOPort) actor.inputPortList().get(k);
                        Receiver[][] channelArray = port.getReceivers();
                        Receiver[][] newChannelArray = newPort.getReceivers();
                        for (int l = 0; l < channelArray.length; l++) {
                            Receiver[] receiverArray = channelArray[l];
                            Receiver[] newReceiverArray = newChannelArray[l];
                            for (int m = 0; m < receiverArray.length; m++) {
                                TDLReceiver receiver = (TDLReceiver) receiverArray[m];
                                TDLReceiver newReceiver = (TDLReceiver) newReceiverArray[m];
                                receiver.copyTokensTo(newReceiver);
                            }
                        }
                    }
                }
            }
        }
    }
    
 
    /**
     * Current node in the TDL actions graph.
     */
    private TDLActionsGraph _graph; 

    /**
     * Contains a mode schedule for each mode in the TDL module.
     */
    private HashMap _modeSchedules;

    /**
     * All receivers.
     */
    private LinkedList _receivers = new LinkedList();

    /**
     * All tasks that have to be done at current point in time.
     */
    private ArrayList _schedule = null;

    /**
     * Currently active mode schedule.
     */
    private TDLModeSchedule _currentSchedule;

    private double _currentWCET = 0.0;

}
