/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * (c) 2010 QTronic GmbH
 * See documentation/fmusdk_license.txt
 * ---------------------------------------------------------------------------*/

// Define class name and unique id. Match the values in modelDescription.xml
#define MODEL_IDENTIFIER incME1
#define MODEL_GUID "{8c4e810f-3df3-dead-beef-176fa3c9f000}"

// define model size
#define NUMBER_OF_REALS 0
#define NUMBER_OF_INTEGERS 1
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define counter_ 0

// called by fmiInstantiateModel
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiInitialize
#define setStartValues                  fmiFullName(_setStartValues)
DllExport void setStartValues(ModelInstance *comp) {
    i(counter_) = 1;
}

// called by fmiInitialize() after setting eventInfo to defaults
// Used to set the first time event, if any.
#define initialize                  fmiFullName(_initialize)
DllExport void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
    eventInfo->upcomingTimeEvent   = fmiTrue;
    eventInfo->nextEventTime       = 1 + comp->time;
}

// called by fmiEventUpdate() after setting eventInfo to defaults
// Used to set the next time event, if any.
#define eventUpdate                  fmiFullName(_eventUpdate)
DllExport void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
    i(counter_) += 1;
    if (i(counter_) == 13)
        eventInfo->terminateSimulation = fmiTrue;
    else {
        eventInfo->upcomingTimeEvent   = fmiTrue;
        eventInfo->nextEventTime       = 1 + comp->time;
    }
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

