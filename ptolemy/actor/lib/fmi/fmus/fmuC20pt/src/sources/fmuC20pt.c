/* ---------------------------------------------------------------------------*
 * An FMU that copies its input2 to the output.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER fmuC20pt
#define MODEL_GUID "{61b1b767-4015-40e1-a2de-6b7af0ad4238}"

// define model size
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "fmi2FunctionTypes.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define input1_ 0
#define input2_ 1
#define output_ 2

// define state vector as vector of value references
#define STATES { }

// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
    if (comp->loggingOn) {
        comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                "setStartValues()");
    }
    r(output_) = 0;
    r(input1_) = 0;
    r(input2_) = 0;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmi2EventInfo* eventInfo) {
    if (comp->loggingOn) {
        comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                "initialize()");
    }

    // Calculation is not event based, so no event time will be defined
    eventInfo->nextEventTimeDefined   = fmi2False;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        comp->eventInfo.nextEventTimeDefined   = fmi2False;
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {

}

// Called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives.
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr) {
    case output_:
        // Calculate output when output is requested
        r(output_) = r(input2_);
        // Log call to facilitate debugging
        // comp->functions->logger(comp->componentEnvironment, comp->instanceName, fmiOK, "logFmiCall", "input: %F, output: %F", r(input_), r(output_));
        return r(output_);
    default: return 0;
    }
}

// We are adding fmi2GetMaxStepSize as a global, so we need to set up the exports.

// Lines like this appear in fmi2FunctionTypes.h for other fmi2* functions.
// However, if this line is present under gcc-4.9.2, then we get an error.
// typedef fmi2Status fmi2GetMaxStepSizeTYPE                  (fmi2Component, fmi2Real *);

// Lines like thes appear in fmi2Functions.h for other fmi2* functions.
#define fmi2GetMaxStepSize fmi2FullName(fmi2GetMaxStepSize)
FMI2_Export fmi2GetMaxStepSizeTYPE fmi2GetMaxStepSize;

fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
    return fmi2OK;
}
// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

