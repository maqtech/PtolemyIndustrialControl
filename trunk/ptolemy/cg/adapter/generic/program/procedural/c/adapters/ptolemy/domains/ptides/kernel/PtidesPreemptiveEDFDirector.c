/*** StructDefBlock ***/
//#define LCD_DEBUG

#define LESS -1
#define MORE 1
#define EQUAL 0

#define MAX_EVENTS 100
#define MAX_ACTUATOR_TIMER_VALUES 10

/* structures */
typedef struct {
    uint32 secs;
    uint32 nsecs;
} Time;

typedef struct {
    Time timestamp;
    uint16 microstep;
} Tag;

typedef struct event {
    union {
        int int_Value; 
        double double_Value;
        long long_Value;
        char char_Value;
        unsigned int boolean_Value;
    } Val;
    Tag tag;
    uint32 depth;
    void (*fireMethod)();

    struct event** sinkEvent;
    Time deadline;
    Time offsetTime;

    struct event* nextEvent;
    struct event* prevEvent;
} Event;
/**/

/*** FuncProtoBlock ***/
void addEvent(Event*);
void fireActor(Event*);
void freeEvent(Event*);
unsigned int higherPriority(const Event*);
void initializeEvents(void);
Event* newEvent(void);
void propagateDataToken(Event*);
void queuePriority(Event*);
void removeEvent(Event*);
void safeToProcess(const Event* const, Time*);
void setCurrentModelTag(Event*);
void timeAdd(const Time, const Time, Time*);
int timeCompare(const Time, const Time);
int timeSub(const Time, const Time, Time*);

/* static variables */
Event eventMemory[MAX_EVENTS];
Event* DEADLINE_QUEUE_HEAD = NULL;
Event* DEADLINE_QUEUE_TAIL = NULL;
Event* FREE_EVENT_LIST = NULL;
Time currentModelTime;
int currentMicrostep;

// Global variable to keep track of number of times the timer needs to interrupt before physical
// time has exceeded safe to process time.
unsigned long timerInterruptSecsLeft;
unsigned long actuatorTimerInterruptSecsLeft;

// ID of the current actuator that's causing the timer to run.
int actuatorRunning = -1;
Time lastActuateTime;

int stackedDeadlineIndex = -1;
Time executingDeadlines[MAX_EVENTS];
void (*executingActors[MAX_EVENTS])();

Tag executingModelTag[MAX_EVENTS];
int stackedModelTagIndex = -1;

Time lastTimerInterruptTime;
volatile uint32 _secs = 0;
volatile uint32 _quarterSecs = 0;

// actuator queue
// Head points to the head of the array.
int actuatorArrayHeadPtrs[numActuators];
// Tail points to the last element array.
int actuatorArrayTailPtrs[numActuators];
int actuatorArrayCounts[numActuators];
Time actuatorTimerValues[numActuators][MAX_ACTUATOR_TIMER_VALUES];

// Times.
static Time MAX_TIME = {(uint32)-1, (uint32)-1};
static Time ZERO_TIME = {0, 0};

/**/

/*** FuncBlock ***/
/* event memory manipulations*/

// this method compares events in lexicographical order based on:
// deadline, timestamp, microstep, and depth.
int compareEvents(Event* event1, Event* event2) {
    int compare;
	if (event1 == NULL || event2 == NULL) {
		die("compare NULL events");
	}
    compare = timeCompare(event1->deadline, event2->deadline);
    if (compare != 0) {
        return compare;
    } else {
        compare = timeCompare(event1->tag.timestamp, event2->tag.timestamp);
        if (compare != 0) {
            return compare;
        } else {
            if (event1->tag.microstep < event2->tag.microstep) {
                return -1;
            } else if (event1->tag.microstep > event2->tag.microstep) {
                return 1;
            } else {
                if (event1->depth < event2->depth) {
                    return -1;
                } else if (event1->depth > event2->depth) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}

// Insert an event into the event queue.
void addEvent(Event* newEvent) {
    Event* compareDeadline = DEADLINE_QUEUE_HEAD;
	disableInterrupts();
	if (DEADLINE_QUEUE_HEAD == NULL) {
		DEADLINE_QUEUE_HEAD = newEvent;
		DEADLINE_QUEUE_TAIL = newEvent;
		newEvent->prevEvent = NULL;
		newEvent->nextEvent = NULL;
	} else {
		if (compareEvents(newEvent, DEADLINE_QUEUE_TAIL) >= 0) {
			newEvent->prevEvent = DEADLINE_QUEUE_TAIL;
			DEADLINE_QUEUE_TAIL->nextEvent = newEvent;
			newEvent->nextEvent = NULL;
			DEADLINE_QUEUE_TAIL = newEvent;
		} else {
			while (true) {
				if (compareDeadline == NULL) {
					die("FAIL!!");
				}
				if (compareEvents(newEvent, compareDeadline) <= 0) {
					break;
				} else {
					compareDeadline = compareDeadline->nextEvent;
				}
			}
			newEvent->nextEvent = compareDeadline;
			newEvent->prevEvent = compareDeadline->prevEvent;
			compareDeadline->prevEvent = newEvent;
			if (compareDeadline != DEADLINE_QUEUE_HEAD) {
				newEvent->prevEvent->nextEvent = newEvent;
			} else {
				DEADLINE_QUEUE_HEAD = newEvent;
			}
		}
	}
    enableInterrupts();
}

// Peek the next event pointed to by thisEvent. If thisEvent is NULL,
// return the head of the event queue.
Event* peekNextEvent(Event* thisEvent) {
	if (thisEvent == NULL) {
		return DEADLINE_QUEUE_HEAD;
	} else {
		return thisEvent->nextEvent;
	}
}

int notSameTag(const Event* event1, const Event* event2) {
    if (timeCompare(event1->tag.timestamp, event2->tag.timestamp) == EQUAL
        && event1->tag.microstep == event2->tag.microstep) {
            return false;
    } else {
        return true;
    }
}

int sameDestination(const Event* event1, const Event* event2) {
    // for now, assume if two events are destined to the same actor,
    // then they should be processed together. This is however not
    // true in general. It should be two events destined to the same
    // equivalence class should be processed togetherd.
    return event1->fireMethod == event2->fireMethod;
}

// Remove this event from event queue.
void removeEventFromQueue(Event* event) {
	if (event->prevEvent != NULL) {
		event->prevEvent->nextEvent = event->nextEvent;
	} else {
		// Event is the head.
		DEADLINE_QUEUE_HEAD = event->nextEvent;
	}
	if (event->nextEvent != NULL) {
		event->nextEvent->prevEvent = event->prevEvent;
	} else {
		DEADLINE_QUEUE_TAIL = event->prevEvent;
	}
	event->nextEvent = NULL;
}

// Remove this event from the event queue, as well as all other
// events that share the same timestamp, as well as destination actor.
void removeAndPropagateSameTagEvents(Event* thisEvent) {
	Event* nextEvent = thisEvent->nextEvent;
	Event* lastEvent = thisEvent;
    propagateDataToken(thisEvent);
	removeEventFromQueue(thisEvent);
    // Now find the next event see we should process it at the same time.
    while (true) {
        if (nextEvent == NULL) {
            break;
        } else if (notSameTag(nextEvent, thisEvent)) {
            break;
        } else {
            // If the next event and this event share the same tag,
			// as well as the same destination
            // actor, then propagate this event.
            if (sameDestination(nextEvent, thisEvent)) {
                propagateDataToken(nextEvent);
				removeEventFromQueue(nextEvent);
				lastEvent->nextEvent = nextEvent;
				lastEvent = nextEvent;
            }
			nextEvent = nextEvent->nextEvent; 
        }
    }
	// Make this linked list semi-circular by pointing
	// the prevEvent of thisEvent to the end of the list.
	// This is used later in freeEvent().
	thisEvent->prevEvent = lastEvent;
}

// Allocate a new event from the free list of events.
Event* newEvent(void) {
	Event* result;
	disableInterrupts();
	if (FREE_EVENT_LIST == NULL) {
		die("ran out of memory");
	}
	result = FREE_EVENT_LIST;
	FREE_EVENT_LIST = FREE_EVENT_LIST->nextEvent;
	enableInterrupts();
	return result;
}

// Deallocate this event, as well as all next events linked together using
// the nextEvent construct to the free list of events.
void freeEvent(Event* thisEvent) {
    disableInterrupts();
	// This line of code is confusing. To understand it, refer to the last
	// line of removeAndPropageSameTagEvents() method. There, the prevEvent
	// pointer of thisEvent is set to the end of the list of events removed
	// from the event queue. We simply append this list to the head of
	// FREE_EVENT_LIST.
	thisEvent->prevEvent->nextEvent = FREE_EVENT_LIST;
	FREE_EVENT_LIST = thisEvent;
    enableInterrupts();
}

/* time manipulation */
void timeAdd(const Time time1, const Time time2, Time* timeSum) {
    timeSum->secs = time1.secs + time2.secs;
    timeSum->nsecs = time1.nsecs + time2.nsecs;
    if (timeSum->nsecs >= 1000000000) {
        timeSum->nsecs -= 1000000000;
        timeSum->secs++;
    }
}

//compare two time values
int timeCompare(const Time time1, const Time time2) {
    if (time1.secs < time2.secs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs < time2.nsecs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs == time2.nsecs) {
        return EQUAL;
    }
    return MORE;       
}

/* subtract two time values
 * 
 */
int timeSub(const Time time1, const Time time2, Time* timeSub) {
    if (timeCompare(time1, time2) == -1) {
        return -1;
    }
    timeSub->secs = time1.secs - time2.secs;
    if (time1.nsecs < time2.nsecs) {
        timeSub->secs--;
        timeSub->nsecs = time1.nsecs + 1000000000 - time2.nsecs;
    } else {
        timeSub->nsecs = time1.nsecs - time2.nsecs;
    }
    return 1;
}

/* Event processing */
void processEvents() {
    Event* event = NULL;
    Time processTime;
    Time platformTime;
	// Get the current platform time. This time is later used to
	// perform safe-to-process. This function must be called
	// before interrupts are disabled to ensure DE semantics.
    getRealTime(&platformTime);
    disableInterrupts();
    while (true) {
        // If event is null, then return the highest priority event
        // from the queue. Otherwise, return the next event pointed
        // to by event. Return NULL if there are no more events
        // in the event queue. The event queue is sorted in
        // deadline order.
        event = peekNextEvent(event);
        // If there are no more events in the event queue, break
        // out of the while loop.
		if (!event) {
            break;
        }
        // If this event's priority is higher than the last priority
        // saved in storePriority(), then continue.
        // The priority here is the priority of the event,
        // not the priority of the interrupt.
        if (higherPriority(event)) {
            // check if this event is safe to process.
            safeToProcess(event, &processTime);
            if (timeCompare(platformTime, processTime) >= 0) {
                // Store the priority of the previous interrupt.
                // Stored priority is later used for comparison in
                // higherPriority().
                queuePriority(event);
                // Get all events of the same timestamp, and share
                // the same destination equivalence class.
                // Remove these events from the event queue.
                removeAndPropagateSameTagEvents(event);
                setCurrentModelTag(event);
                // Ready to process the next event, so first enable
                // interrupts.
                enableInterrupts();
                // Execute this event by firing the corresponding
                // actor. During this process more events may
                // be posted onto the queue
                fireActor(event);
                // The executed event can now be freed into the
                // pool of available events
				freeEvent(event);
				// This event has finished execution. The priority of
				// this event can be forgotten. We forget by decrementing
				// the stackedDeadlineIndex.
				stackedDeadlineIndex--;
				// Reset event to null so the next peekNextEvent()
                // looks at the top event.
                event = NULL;
				// Get the current platform time. This time is later used to
				// perform safe-to-process. This function must be called
				// before interrupts are disabled to ensure DE semantics.
			    getRealTime(&platformTime);
                // We are ready to look at the next event in the
                // event queue. Before doing that interrupts need
                // to be disabled
                disableInterrupts();
            } else {
                // This event is not safe to process yet. Set
                // timed interrupt to run this event when platform
                // time has passed for it to be safe to process.
				if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
				// This event is not safe to process, we continue processing
				// by going back to the beginning of the loop.
				// HACK: that doesn't seem to work yet, we'll just process
				// the top event from the queue for now.
				break;
	        }
        } else {
            // This event is of lower priority than the one 
            // currently executing, break out of the while loop.
            break;
        }//end while().
    }
    // restore the last executing stacked model tag.
    if (stackedModelTagIndex >= 0) {
        currentMicrostep = executingModelTag[stackedModelTagIndex].microstep;
        currentModelTime = executingModelTag[stackedModelTagIndex].timestamp;
        stackedModelTagIndex--;
    } else {
        die("cannot restore model tag");
    }
	// End of processEvents(), enable interrupts.
    enableInterrupts();
    // we do not need to disable interrupts for this routine, because it
    // is triggered through a SVC call, which has higher priority than
    // all other external interrupts in the system.
    restoreStack();
    die("should never get here");
}

/*
* Fire the corresponding actor for this event.
*/
void fireActor(Event* thisEvent) {
    if (thisEvent->fireMethod != NULL){
        (thisEvent->fireMethod)();
    } else {
        die("no such method, cannot fire\n");
    }
}

/* Determines whether the event to fire this current actor is of higher priority than
*  whatever even that's currently being executed.
*/                                                                    
unsigned int higherPriority(const Event* const event) {
    int i;
    if (stackedDeadlineIndex < 0) {
        // there are no events on the stack, so it's always true.
        return true;
    } else if (timeCompare(executingDeadlines[stackedDeadlineIndex], event->deadline) == LESS) {
#ifdef LCD_DEBUG
        debugMessageNumber("exDe sec=",
                executingDeadlines[stackedDeadlineIndex].secs);
        debugMessageNumber("exDe nsec=",
                executingDeadlines[stackedDeadlineIndex].nsecs); 
#endif
        return false;
    } else {
        // check for all actors that are currently firing, and make sure we
        // don't fire an actor that's already firing.
        for (i = 0; i <= stackedDeadlineIndex; i++) {
            if (executingActors[i] == event->fireMethod) {
                return false;
            }
        }
        return true;
    }
}

/*
* Add the priority of the current event into queue
* Set the firing flag of the actor, indicate that the actor is currenting being fired.
*/
void queuePriority(Event* event) {
    stackedDeadlineIndex++;
    if (stackedDeadlineIndex == MAX_EVENTS) {
        die("stackedDeadlineIndex exceeds MAX_EVENTS");
    }
    executingDeadlines[stackedDeadlineIndex] = event->deadline;
    executingActors[stackedDeadlineIndex] = event->fireMethod;
}

/*
 * Set the current model time.
 */
void setCurrentModelTag(Event* currentEvent) {
    currentModelTime = currentEvent->tag.timestamp;
    currentMicrostep = currentEvent->tag.microstep;
}

/*
 * Propagate the data token to the downstream input port Event pointer.
 */
void propagateDataToken(Event* currentEvent){
    *(currentEvent->sinkEvent) = currentEvent;
}

/* 
* Determine the physical time at which an event becomes safe to process.
* This time is calculated by subtracting the event's timestamp by an offset.
*/
void safeToProcess(const Event* const thisEvent, Time* safeTimestamp) {
	int out = timeSub(thisEvent->tag.timestamp, thisEvent->offsetTime, safeTimestamp);
	if (out == -1) {
		safeTimestamp->secs = 0;
		safeTimestamp->nsecs = 0;
	}
#ifdef LCD_DEBUG
    //sprintf(str, "STP=%d", safeTimestamp->secs);
    //RIT128x96x4StringDraw(str, 0,40,15);
#endif
}
/**/

/*** initPIBlock ***/
// The platform independent initialization code goes here.
initializeMemory();
initializeEvents();
initializePISystem();
/**/

/*** initPICodeBlock ***/
// The platform independent initialization code goes here.
void initializeEvents(void) {
    // no event initialization is needed here... for now.
}

/* initialize Event memory structures.
 */
void initializeMemory() {
    int i;
    _secs = 0;
    _quarterSecs = 0;
    for(i = 1; i < MAX_EVENTS; i++) {
        // event is "freed and can be returned by newEvent"
        eventMemory[i-1].nextEvent = &eventMemory[i];
    }
	FREE_EVENT_LIST = &eventMemory[0];
}

void initializePISystem() {
    lastTimerInterruptTime = MAX_TIME;
}
/**/

/*** preinitPIBlock() ***/
// This is the platform independent preinitialization code
/**/

/*** mainLoopBlock ***/
void execute() {
    Event* event = NULL;
    Time processTime;
    Time platformTime;
	// Get the current platform time. This time is later used to
	// perform safe-to-process. This function must be called
	// before interrupts are disabled to ensure DE semantics.
    getRealTime(&platformTime);
    disableInterrupts();
    while (true) {
        // If event is null, then return the highest priority event
        // from the queue. Otherwise, return the next event pointed
        // to by event. Return NULL if there are no more events
        // in the event queue. The event queue is sorted in
        // deadline order.
        event = peekNextEvent(event);
        // If there are no more events in the event queue, break
        // out of the while loop.
		if (!event) {
            break;
        }
        // If this event's priority is higher than the last priority
        // saved in storePriority(), then continue.
        // The priority here is the priority of the event,
        // not the priority of the interrupt.
        if (higherPriority(event)) {
            // check if this event is safe to process.
            safeToProcess(event, &processTime);
            if (timeCompare(platformTime, processTime) >= 0) {
                // Store the priority of the previous interrupt.
                // Stored priority is later used for comparison in
                // higherPriority().
                queuePriority(event);
                // Get all events of the same timestamp, and share
                // the same destination equivalence class.
                // Remove these events from the event queue.
                removeAndPropagateSameTagEvents(event);
                setCurrentModelTag(event);
                // Ready to process the next event, so first enable
                // interrupts.
                enableInterrupts();
                // Execute this event by firing the corresponding
                // actor. During this process more events may
                // be posted onto the queue
                fireActor(event);
                // The executed event can now be freed into the
                // pool of available events
				freeEvent(event);
				// This event has finished execution. The priority of
				// this event can be forgotten. We forget by decrementing
				// the stackedDeadlineIndex.
				stackedDeadlineIndex--;
				// Reset event to null so the next peekNextEvent()
                // looks at the top event.
                event = NULL;
				// Get the current platform time. This time is later used to
				// perform safe-to-process. This function must be called
				// before interrupts are disabled to ensure DE semantics.
			    getRealTime(&platformTime);
                // We are ready to look at the next event in the
                // event queue. Before doing that interrupts need
                // to be disabled
                disableInterrupts();
            } else {
                // This event is not safe to process yet. Set
                // timed interrupt to run this event when platform
                // time has passed for it to be safe to process.
				if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
				// This event is not safe to process, we continue processing
				// by going back to the beginning of the loop.
				// HACK: that doesn't seem to work yet, we'll just process
				// the top event from the queue for now.
				break;
	        }
        } else {
            // This event is of lower priority than the one 
            // currently executing, break out of the while loop.
            break;
        }//end while().
    }
	// End of processEvents(), enable interrupts.
    enableInterrupts();
	// Go into an infinite loop to wait for a wakeup signal.
    while (1);
}
/**/
