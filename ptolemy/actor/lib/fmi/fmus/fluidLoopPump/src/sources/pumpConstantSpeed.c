/* ---------------------------------------------------------------------------*
 * FMU for a pump with constant speed and linear pressure vs. mass flow rate
 * curve.
 *
 * This FMU has the following properties:
 *  - It has no states.
 *  - It has direct feedthrough.
 *  - Its outputs do not depend on time.
 *
 * This file is based on the template FMU 'helloWorldME2' developed by
 * Christopher Brooks and Edward A. Lee. 
 *
 * Authors: Michael Wetter.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER pumpConstantSpeed

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmiReal so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    fmiReal    *r;
    fmiBoolean mustComputeOutputs;
    const fmiCallbackFunctions* functions;
    fmiString instanceName;
} ModelInstance;

// Number of variables
#define NVARS 8

// Globally unique ID used to make sure the XML file and the DLL match.
// See also guid in modelDescription.xml
// The following was generated at http://guid.us
#define MODEL_GUID "{d23f3307-74ab-48e6-85c3-df507efe7812}"


FMI_Export fmiComponent fmiInstantiate(fmiString instanceName,
        fmiType   fmuType, 
        fmiString fmuGUID, 
        fmiString fmuResourceLocation, 
        const fmiCallbackFunctions* functions, 
        fmiBoolean                  visible,
        fmiBoolean                  loggingOn) {
                                           
    ModelInstance* component;

    // Perform checks.
    if (!functions->logger)
        return NULL;
    if (!functions->allocateMemory || !functions->freeMemory){
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Missing callback function: freeMemory");
        return NULL;
    }
    if (!instanceName || strlen(instanceName)==0) {
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Missing instance name.");
        return NULL;
    }
    if (strcmp(fmuGUID, MODEL_GUID)) {
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", fmuGUID, MODEL_GUID);
        return NULL;
    }
    component = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    // cxh: One key change here was that we allocate memory for the pointer holding
    // the value.
    component->r = functions->allocateMemory(7, sizeof(fmiReal));
    component->functions = functions;
    component->instanceName = instanceName;
    component->mustComputeOutputs = fmiTrue;
    printf("pumpConstantSpeed.c: exit fmiInstantiate.\n");
    return component;
}

void fmiFreeInstance(fmiComponent c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component);
}

FMI_Export fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
        fmiReal communicationStepSize, fmiBoolean newStep) {
    return fmiOK;
}

FMI_Export fmiStatus fmiEnterInitializationMode(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiExitInitializationMode(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    int i;
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;

    // Indices of instance variables can be used to set or get by the master algorithm.
    enum {mIn_flow=1, TIn, pIn, mOut_flow, TOut, pOut, dp0, m0_flow};

    if (nvr > NVARS) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions->logger(component, component->instanceName, fmiError, "error",
        //        "fmiGetReal: Illegal value reference %u.", nvr);
        return fmiError;
    }
    if (nvr > 0) {
      // Check if the output must be computed.
      // This could be made more efficient using an alias as mOut_flow=mIn_flow and TOut=TIn.
      if (component->mustComputeOutputs){
	component->r[mOut_flow] = component->r[mIn_flow];
	component->r[TOut] = component->r[TIn];
	component->r[pOut] = component->r[pIn] + component->r[dp0] * 
	  (1 - component->r[mIn_flow]/component->r[m0_flow]);
	component->mustComputeOutputs = fmiFalse;
      }
      // Assign outputs
      for(i=0; i < nvr; i++){
        value[i] = component->r[vr[i]];
      }
    }
    return fmiOK;
    }

FMI_Export fmiStatus fmiTerminate(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiReset(fmiComponent c) {
    return fmiError;
}


////////////////////////////////////////////////////////////
// Below here are functions that need to be in the FMU for it to
// pass the checker.  However, the all return fmiError or print a
// message so that we can be sure that they are not called accidentally.


FMI_Export fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiInteger value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiBoolean value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetString(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiString value[]) {
    return fmiError;
}

FMI_Export const char* fmiGetTypesPlatform() {
    // FIXME: this leaks, we not allocate memory each time we call it.
    return strdup("default");
}

FMI_Export const char* fmiGetVersion() {
    // FIXME: this leaks, we not allocate memory each time we call it.
    return strdup("2.0");
}

FMI_Export fmiStatus fmiSetDebugLogging(fmiComponent c,
        fmiBoolean      loggingOn, 
        size_t          nCategories, 
        const fmiString categories[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i;
    ModelInstance* component = (ModelInstance *) c;
    if (nvr > NVARS){
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
                          "fmiSetReal: To many real arguments are provided.");
        return fmiError;
    }
    // Set values.
    for (i = 0; i < nvr; i++) {
        component->r[vr[i]] = value[i];
    }
    // Set a flag that indicates that the outputs must be re-computed.
    component->mustComputeOutputs = fmiTrue;
    return fmiOK;
}

FMI_Export fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiInteger value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiBoolean value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetString(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiString value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetupExperiment(fmiComponent c, 
        fmiBoolean   toleranceDefined, 
        fmiReal      tolerance, 
        fmiReal      startTime, 
        fmiBoolean   stopTimeDefined, 
        fmiReal      stopTime) {
    // There is nothing to do here.
    return fmiOK;
}

FMI_Export fmiStatus fmiGetFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetFMUstate(fmiComponent c, fmiFMUstate FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiFreeFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiSerializedFMUstateSize(fmiComponent c, fmiFMUstate FMUstate,
        size_t* size) {
    return fmiError;
}

FMI_Export fmiStatus fmiSerializedFMUstate(fmiComponent c, fmiFMUstate FMUstate,
        fmiByte serializedState[], size_t size) {
    return fmiError;
}

FMI_Export fmiStatus fmiDeSerializedFMUstate(fmiComponent c,
        const fmiByte serializedState[],
        size_t size, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetDirectionalDerivative(fmiComponent c,
        const fmiValueReference vUnknown_ref[], size_t nUnknown,
        const fmiValueReference vKnown_ref[],   size_t nKnown,
        const fmiReal dvKnown[], fmiReal dvUnknown[]) {
    printf("pumpConstantSpeed.c: fmiGetDirectionalDerivative() called, even though the FMU does not provide them.\n");
    // The standard 2.0, RC 1 says on p. 26:
    // If the capability attribute “providesDirectionalDerivative” is true, 
    // fmiGetDirectionalDerivative computes a linear combination of the partial derivatives of h 
    // with respect to the selected input variables
    return fmiOK; // FIXME. Shouldn't this be fmiError?
}

// Start of Model Exchange functions.
// Alphabetical in this section.

FMI_Export fmiStatus fmiGetContinuousStates(fmiComponent c, fmiReal x[],
        size_t nx) {
    printf("pumpConstantSpeed.c: fmiGetContinuousStates() called, even though the FMU has no states.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiGetNominalsOfContinuousStates(fmiComponent c, 
        fmiReal x_nominal[], 
        size_t nx) {
    // Model Exchange
    printf("pumpConstantSpeed.c: fmiGetNominalsOfContinuousStates returning fmiError as it has no states.\n");
    return fmiError;
}

FMI_Export fmiStatus fmiCompletedIntegratorStep(fmiComponent c,
        fmiBoolean   noSetFMUStatePriorToCurrentPoint, 
        fmiBoolean*  enterEventMode, 
        fmiBoolean*   terminateSimulation) {
    // Model Exchange
    return fmiOK;
}

FMI_Export fmiStatus fmiEnterContinuousTimeMode(fmiComponent c) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiEnterEventMode(fmiComponent c) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiGetDerivatives(fmiComponent c, fmiReal derivatives[],
        size_t nx) {
    // Model Exchange
    printf("pumpConstantSpeed.c: fmiGetDerivatives() is called even though the FMU has no states.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiGetEventIndicators(fmiComponent c, 
        fmiReal eventIndicators[], size_t ni) {
    // Model Exchange
    printf("pumpConstantSpeed.c: fmiGetEventIndicators() returning fmiError as it does not trigger events.\n");
    return fmiError;
}

FMI_Export fmiStatus fmiNewDiscreteStates(fmiComponent  c,
        fmiEventInfo* fmiEventInfo) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiSetContinuousStates(fmiComponent c, const fmiReal x[],
        size_t nx) {
    // Model Exchange
    printf("pumpConstantSpeed.c: fmiSetContinuousStates() is called even though the FMU has no states.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiSetTime(fmiComponent c, fmiReal time) {
    // Model Exchange
    return fmiOK;
}



