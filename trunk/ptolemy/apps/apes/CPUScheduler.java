package ptolemy.apps.apes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor; 
import ptolemy.actor.NoRoomException;
import ptolemy.actor.util.Time; 
import ptolemy.apps.apes.TaskExecutionListener.ScheduleEventType;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * The CPU resource implements a fixed priority preemptive scheduling of tasks.
 * Actors that can send events to the input of the CPU resource are triggers and
 * tasks. Input tokens are ResourceTokens. Output events are sent to tasks,
 * output tokens are empty tokens, they are just used to trigger task actors.
 * The CPU resource manages a stack of all tasks and their remaining times
 * according to the task priorities. When the CPU resource is fired, it
 * decreases the remaining time of the currently executing task, puts the tasks
 * that are scheduled to be
 * 
 * @author Patricia Derler
 */
public class CPUScheduler extends ApeActor {

    /**
     * Construct an actor in the default workspace with an empty string as its
     * name. The object is added to the workspace directory. Increment the
     * version number of the workspace.
     * 
     * @throws IllegalActionException
     */
    public CPUScheduler() {
        super();
        _initialize();
    }

    /**
     * Construct an actor in the specified workspace with an empty string as a
     * name. You can then change the name with setName(). If the workspace
     * argument is null, then use the default workspace. The object is added to
     * the workspace directory. Increment the version number of the workspace.
     * 
     * @param workspace
     *            The workspace that will list the entity.
     */
    public CPUScheduler(Workspace workspace) {
        super(workspace);
        _initialize();
    }

    /**
     * Create a new actor in the specified container with the specified name.
     * The name must be unique within the container or an exception is thrown.
     * The container argument must not be null, or a NullPointerException will
     * be thrown.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of this actor within the container.
     * @exception IllegalActionException
     *                If this actor cannot be contained by the proposed
     *                container (see the setContainer() method).
     * @exception NameDuplicationException
     *                If the name coincides with an entity already in the
     *                container.
     */
    public CPUScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initialize();
    }

    public enum TaskState {
        ready_running, suspended, waiting
    }
    
    public enum StatusType {
        E_OK, E_OS_ACCESS, E_OS_CALLEVEL, E_OS_ID, E_OS_LIMIT, E_OS_NOFUNC, E_OS_RESOURCE, E_OS_STATE, E_OS_VALUE}

    // TODO initialize private variables - maps and lists
    public Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }


    
    /**
     * Schedule actors.
     */
    public void fire() throws IllegalActionException {
        System.out.println("CPUScheduler.fire() - Time: " + getDirector().getModelTime()); 
        System.out.println(_tasksInExecution);
        System.out.println(_taskExecutionTimes);
        System.out.println(_tasksThatStartedExecuting);
        Time passedTime = getDirector().getModelTime().subtract(_previousModelTime);
        Actor taskInExecution = null;
        Actor newCurrentlyExecutingTask = null;

        // if time passed, decrease remaining execution time of currently running task
        if (_tasksInExecution.size() > 0 && passedTime.getDoubleValue() > 0.0) {
            taskInExecution = _tasksInExecution.peek(); 
            Time remainingTime = _taskExecutionTimes.get(taskInExecution); 
            if (remainingTime.equals(Time.POSITIVE_INFINITY)) { // task executed but its execution time is not known yet
                _usedExecutionTimes.put(taskInExecution, _usedExecutionTimes.get(taskInExecution).add(passedTime));
            } else { // task executed, decrease its execution time
                remainingTime = remainingTime.subtract(passedTime);
                _taskExecutionTimes.put(taskInExecution, remainingTime);
                
                // take out of the list if remainingTime = 0
                // task can continue execution, execution time is not known and will be sent by access point event 
                if (remainingTime.equals(new Time(getDirector(), 0.0))) {
                    _tasksThatStartedExecuting.remove(taskInExecution); 
                    _taskExecutionTimes.put(taskInExecution, Time.POSITIVE_INFINITY);   
                    _usedExecutionTimes.put(taskInExecution, new Time(getDirector(), 0.0)); 
                    _sendTaskExecutionEvent(taskInExecution, ScheduleEventType.STOP); 
                    _sendTaskExecutionEvent(taskInExecution, ScheduleEventType.START); 
                    newCurrentlyExecutingTask = taskInExecution; 
                }
            }   
        }
        
        

        // schedule tasks according to requests sent via tokens 
        while(input.hasToken(0)) {
            ResourceToken token = (ResourceToken) input.get(0);
            Actor actorToSchedule = token.actorToSchedule;
            Time executionTime = (Time) token.requestedValue; 
            TaskState state = token.state;
            if (state == null)
                state = _taskStates.get(actorToSchedule);
            System.out.println("  -. " + actorToSchedule); 
            Actor actor = scheduleTask(state, actorToSchedule, executionTime);
            if (actor != null)
                newCurrentlyExecutingTask = actor;
        }
        
        
        if (newCurrentlyExecutingTask != null) {
            output.send(newCurrentlyExecutingTask, new BooleanToken(true));
            _tasksThatStartedExecuting.add(taskInExecution);
            _sendTaskExecutionEvent(newCurrentlyExecutingTask, ScheduleEventType.START);
        }
        
        // schedule next firing of this
        if (_tasksInExecution.size() > 0) { 
            getDirector().fireAt(this, getDirector().getModelTime().add(_taskExecutionTimes.get(_tasksInExecution.peek()))); 
        }

        _previousModelTime = getDirector().getModelTime();
        System.out.println(this.getName() + "...end" + _tasksInExecution);
    }
    

    
    
    /**
     * Returns the id of the actor. The id is an int value. The
     * default return value is 0.
     * 
     * @param actor
     *            Given actor.
     * @return Priority of the given actor.
     */
    public int getTaskId(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("ID");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();

                return token.intValue();
            } else {
                return 0;
            }
        } catch (ClassCastException ex) {
            return 0;
        } catch (IllegalActionException ex) {
            return 0;
        }
    }
    
    public int getInternalResource(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("internalResourceId");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();

                return token.intValue();
            } else {
                return -1;
            }
        } catch (ClassCastException ex) {
            return -1;
        } catch (IllegalActionException ex) {
            return -1;
        }
    }
    
    //////////////////////
    /// Task management
    
    

    
    // DeclareTask ??
    
    public StatusType ActivateTask(int taskId) throws NoRoomException, IllegalActionException {
        Actor task = _tasks.get(taskId);
        if (task == null)
            return StatusType.E_OS_ID;
        //else if (Task.state = TaskState.running) return StatusType.E_OS_LIMIT;
        rescheduleTasks(task, TaskState.ready_running);
        return StatusType.E_OK;
    }

    public StatusType TerminateTask() throws NoRoomException, IllegalActionException {
            Actor task = _taskNames.get(Thread.currentThread().getName());  
            rescheduleTasks(task, TaskState.suspended);
            
            return StatusType.E_OK;
        }

    public StatusType ChainTask(int taskId) throws NoRoomException, IllegalActionException {
        Actor task = _tasks.get(taskId);
        Actor currentTask = _taskNames.get(Thread.currentThread().getName());
        rescheduleTasks(currentTask, TaskState.suspended);
        rescheduleTasks(task, TaskState.ready_running); 
        return StatusType.E_OK;
    }

    private Actor scheduleTask(TaskState state, Actor actorToSchedule, Time executionTime) throws IllegalActionException { 
        
        Actor newCurrentlyExecutingTask = null;
         
        
        if (_taskStates.get(actorToSchedule) == TaskState.waiting && executionTime != null) { 
            _taskExecutionTimes.put(actorToSchedule, executionTime);
        } else if (state == TaskState.waiting || state == TaskState.suspended) { // remove from queue if waiting
            _tasksInExecution.pop(); 
            if (state == TaskState.suspended)
                _tasksThatStartedExecuting.remove(actorToSchedule);
            _sendTaskExecutionEvent(actorToSchedule, ScheduleEventType.STOP);    
            if (_tasksInExecution.size() > 0) {
                Actor actor = _tasksInExecution.peek();
                _sendTaskExecutionEvent(actor, ScheduleEventType.START); 
                if (!_tasksThatStartedExecuting.contains(_tasksInExecution.peek()))
                    newCurrentlyExecutingTask = _tasksInExecution.peek();
            }
        } else {
            Actor taskInExecution = null;
            
            // if task needs an internal resource which is occupied now
            // the state should be set to waiting
            if (_internalResources.get(actorToSchedule) != null) {
                // TODO
            }
                   
            if (_tasksInExecution.size() > 0) 
                taskInExecution = _tasksInExecution.peek();   
            if (taskInExecution == null || _taskPriorities.get(actorToSchedule) > _taskPriorities.get(taskInExecution)) { // nothing running or preempting, schedule new task
                if (taskInExecution != null) { // preempting
                    _sendTaskExecutionEvent(taskInExecution, ScheduleEventType.STOP);
                }
                _tasksInExecution.push(actorToSchedule); 
                _sendTaskExecutionEvent(actorToSchedule, ScheduleEventType.START);
                if (!_tasksThatStartedExecuting.contains(actorToSchedule))
                    newCurrentlyExecutingTask = actorToSchedule; 
            } else if (_tasksInExecution.contains(actorToSchedule)) { // already in list but execution time was not known
                executionTime = executionTime.subtract(_usedExecutionTimes.get(actorToSchedule));  
            } else { // new actor to schedule does not preempt currently running task
                for (int i = 0; i < _tasksInExecution.size(); i++) { 
                    Actor actor = _tasksInExecution.get(i);
                    if (_taskPriorities.get(actorToSchedule) < _taskPriorities.get(actor)) {
                        _tasksInExecution.insertElementAt(actorToSchedule, i);
                        break;
                    } 
                } 
                    
            }
            if (executionTime != null) 
                _taskExecutionTimes.put(actorToSchedule, executionTime);
            _usedExecutionTimes.put(actorToSchedule, new Time(getDirector(), 0.0));  
            
        }
        _taskStates.put(actorToSchedule, state);
        return newCurrentlyExecutingTask;
    }

    private void rescheduleTasks(Actor task, TaskState state) throws IllegalActionException {
        Actor newCurrentlyExecutingTask = scheduleTask(state, task, null); 
        
        if (newCurrentlyExecutingTask != null) {
            output.send(newCurrentlyExecutingTask, new BooleanToken(true));
            _tasksThatStartedExecuting.add(newCurrentlyExecutingTask);
            _sendTaskExecutionEvent(newCurrentlyExecutingTask, ScheduleEventType.START);
        } 
        if (_tasksInExecution.size() > 0) { 
            getDirector().fireAt(this, getDirector().getModelTime().add(_taskExecutionTimes.get(_tasksInExecution.peek()))); 
        }
    }

    private HashMap<Actor, List<Integer>> _occupiedResources = new HashMap();
    private HashMap<Actor, Integer> _internalResources = new HashMap();
    private List<Integer> _resources = new ArrayList();
    
    /**
     * release resources if higher priority task is ready
     * @return
     * @throws IllegalActionException 
     * @throws NoRoomException 
     */
    public StatusType Schedule() throws NoRoomException, IllegalActionException {
        Actor task = _taskNames.get(Thread.currentThread().getName()); 
        if (_internalResources.get(task) != null) {
            for (Actor actor : _taskStates.keySet()) {
                if (_tasksInExecution.size() > 0 && 
                        _taskPriorities.get(actor) > _taskPriorities.get(_tasksInExecution.peek()) &&
                        _internalResources.get(actor) == _internalResources.get(_tasksInExecution.peek())) {
                    rescheduleTasks(task, TaskState.suspended); 
                }
            }
        }
        getDirector().fireAt(this, getDirector().getModelTime());
//        if (task with higher priority is scheduled and needs this resource)
//            release resource
        return StatusType.E_OK;
    }
    
    
    //////////////////////
    /// Resource management
   
    
    // DeclareResource
    
    public StatusType GetResource(int resourceId) {
        Actor task = _taskNames.get(Thread.currentThread().getName()); 

        for (Actor actor : _occupiedResources.keySet()) {
            List resources = _occupiedResources.get(actor);
            if (resources.contains(resourceId))
                return StatusType.E_OS_ACCESS; // resource in use
        }
        
        if (!_resources.contains(resourceId)) 
            return StatusType.E_OS_ID;
        else {
            List l = _occupiedResources.get(task);
            if (l == null)
                l = new ArrayList();
            l.add(resourceId);
            _occupiedResources.put(task, l);
        } 
        return StatusType.E_OK;
    }
    
    public StatusType ReleaseResource(int resourceId) {
        Actor task = _taskNames.get(Thread.currentThread().getName()); 
        
        if (!_resources.contains(resourceId)) 
            return StatusType.E_OS_ID;
        else if (_occupiedResources.get(task) == null || 
                !_occupiedResources.get(task).contains(resourceId)) // not occupied
            return StatusType.E_OS_NOFUNC;
        _occupiedResources.get(task).remove(resourceId);
        return StatusType.E_OK;
    }
    
    

    /**
     * Set private variables.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            _previousModelTime = new Time(getDirector(), 0.0);
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

        CompositeActor compositeActor = (CompositeActor) getContainer();
        List entities = compositeActor.entityList();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Object entity = it.next();
            if (entity instanceof Actor) {
                Actor actor = (Actor) entity;
                if (actor instanceof CTask) {
                    _tasks.put(getTaskId(actor), actor);
                    _taskStates.put(actor, TaskState.suspended);
                    _taskExecutionTimes.put(actor, Time.POSITIVE_INFINITY);
                    _usedExecutionTimes.put(actor, getDirector().getModelTime()); // during init, this is 0
                    _taskActivations.put(actor, 0);
                    _taskNames.put(actor.getName(), actor);
                    _taskPriorities.put(actor, CTask.getPriority(actor));
                    if (getInternalResource(actor) != -1)
                        _internalResources.put(actor, getInternalResource(actor));
                }
            }
        }
    }
    /**
     * Register task execution listener.
     * @param taskExecutionListener
     */
    protected void _registerExecutionListener(
            TaskExecutionListener taskExecutionListener) {
        if (_executionListeners == null)
            _executionListeners = new ArrayList<TaskExecutionListener>();
        _executionListeners.add(taskExecutionListener);
    }

    /**
     * Send event to the task execution listeners.
     * @param actor
     * @param time
     * @param eventType
     */
    protected final void _sendTaskExecutionEvent(Actor actor,
            ScheduleEventType eventType) {
        if (_executionListeners != null) {
            Iterator listeners = _executionListeners.iterator();

            while (listeners.hasNext()) {
                ((TaskExecutionListener) listeners.next()).event(actor, getDirector().getModelTime().getDoubleValue(),
                        eventType);
            }
        }
    }

    /**
     * Initialize private variables.
     */
    private void _initialize() {
        _taskExecutionTimes = new HashMap<Actor, Time>();
        _usedExecutionTimes = new HashMap<Actor, Time>();
        _tasksInExecution = new Stack<Actor>();
        _taskStates = new HashMap<Actor, TaskState>();
        _tasks = new HashMap();
        _taskActivations = new HashMap();
        _taskNames = new HashMap();
        _taskPriorities = new HashMap();
        
        
        Parameter sourceActorList= (Parameter) input.getAttribute("sourceActors");
        sourceActorList.setExpression("*");

        Parameter destinationActorList= (Parameter) output.getAttribute("destinationActors");
        destinationActorList.setExpression("*");
         
    }
    
    /** Set of tasks that were started and already executed for some time. Those tasks can be preempted. */
    private Set _tasksThatStartedExecuting = new HashSet();
    
    /** Map of taskIds to tasks. */
    private Map<Integer, Actor> _tasks;
    
    /** Map of taskNames and tasks. */
    private Map<String, Actor> _taskNames;
    
    /** Map of actual Task priorities. */
    private Map<Actor, Integer> _taskPriorities;
    
    /** The number of pending activation requests for a task. */
    private HashMap<Actor, Integer> _taskActivations; 

    /** Tasks in execution and their remaining execution time. */
    private Map<Actor, Time> _taskExecutionTimes;
    
    /** Tasks in execution and their used execution times. This is used when the real execution
     * time is not known yet and the remaining execution time (= infinity) cannot be decreased
     * yet.
     */
    private Map<Actor, Time> _usedExecutionTimes;

    /** Tasks in execution. */
    private Stack<Actor> _tasksInExecution;

    /** Model time at the previous firing. */
    private Time _previousModelTime;

    /** List of all tasks and their current state */
    private Map<Actor, TaskState> _taskStates;

    /** Listeners for the task execution events: start, preempt, resume. */
    private Collection<TaskExecutionListener> _executionListeners;

}
