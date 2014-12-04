/*
@Copyright (c) 2014 The Regents of the University of California.
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
/* -------------------------------------------------------------------------
 * main.c
 * Implements simulation of a single FMU instance
 * that implements the "FMI for Co-Simulation 2.0" interface.
 * Command syntax: see printHelp()
 * Simulates the given FMU from t = 0 .. tEnd with fixed step size h and
 * writes the computed solution to file 'result.csv'.
 * The CSV file (comma-separated values) may e.g. be plotted using
 * OpenOffice Calc or Microsoft Excel.
 * This program demonstrates basic use of an FMU.
 * Real applications may use advanced master algorithms to co-simulate
 * many FMUs, limit the numerical error using error estimation
 * and back-stepping, provide graphical plotting utilities, debug support,
 * and user control of parameter and start values, or perform a clean
 * error handling (e.g. call freeSlaveInstance when a call to the fmu
 * returns with error). All this is missing here.
 *
 * Revision history
 *  07.03.2014 initial version released in FMU SDK 2.0.0
 *
 * Free libraries and tools used to implement this simulator:
 *  - header files from the FMI specification
 *  - libxml2 XML parser, see http://xmlsoft.org
 *  - 7z.exe 4.57 zip and unzip tool, see http://www.7-zip.org
 * Author: Adrian Tirea
 * Copyright QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------*/

/* See $PTII/ptolemy/actor/lib/fmi/ma2/fmusdk-license.htm for the complete FMUSDK License. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdbool.h>
#include "fmi2.h"
#include "sim_support.h"

#define NUMBER_OF_FMUS 3
#define NUMBER_OF_EDGES 1

double tEnd = 1.0;
double tStart = 0;                       // start time



static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible, fmi2Boolean loggingOn, int nCategories, char ** categories)
{

    fmi2Status fmi2Flag;                       // return code of the fmu functions

    // instantiate the fmu
    fmu->callbacks.logger = fmuLogger;
    fmu->callbacks.allocateMemory = calloc;
    fmu->callbacks.freeMemory = free;
    fmu->callbacks.stepFinished = NULL; // fmi2DoStep has to be carried out synchronously
    fmu->callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.

    fmi2Real tolerance = 0;                   // used in setting up the experiment
    fmi2Boolean toleranceDefined = fmi2False;  // true if model description define tolerance
    ValueStatus vs = valueIllegal;

    // handle to the parsed XML file
    ModelDescription* md = fmu->modelDescription;
    // global unique id of the fmu
    const char *guid = getAttributeValue((Element *)md, att_guid);
    // check for ability to get and set state

    fmu->canGetAndSetFMUstate = getAttributeBool((Element*)getCoSimulation(md), att_canGetAndSetFMUstate, &vs);
    fmu->canGetMaxStepSize = getAttributeBool((Element*)getCoSimulation(md), att_canGetMaxStepSize, &vs);
    // instance name
    const char *instanceName = getAttributeValue((Element *)getCoSimulation(md), att_modelIdentifier);
    // path to the fmu resources as URL, "file://C:\QTronic\sales"
    char *fmuResourceLocation = getTempResourcesLocation();   // TODO: returns crap. got to save the location for every FMU somehow.
    // instance of the fmu
    fmi2Component comp = fmu->instantiate(instanceName, fmi2CoSimulation, guid, fmuResourceLocation,
            &fmu->callbacks, visible, loggingOn);
    printf("instance name: %s, \nguid: %s, \nressourceLocation: %s\n", instanceName, guid, fmuResourceLocation);
    free(fmuResourceLocation);

    if (!comp)
        {
            printf("Could not initialize model with guid: %s\n", guid);
            return NULL;
        }

    Element *defaultExp = getDefaultExperiment(fmu->modelDescription);
    if (defaultExp) {
            tolerance = getAttributeDouble(defaultExp, att_tolerance, &vs);
    }
    if (vs == valueDefined) {
        toleranceDefined = fmi2True;
    }


    if (nCategories > 0) {
        fmi2Flag = fmu->setDebugLogging(comp, fmi2True, nCategories, (const fmi2String*) categories);
        if (fmi2Flag > fmi2Warning) {
            error("could not initialize model; failed FMI set debug logging");
            return NULL;
        }
    }

    fmi2Flag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart, fmi2True, tEnd);
    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI setup experiment");
        return NULL;
    }
    fmi2Flag = fmu->enterInitializationMode(comp);
    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI enter initialization mode");
        return NULL;
    }
    printf("initialization mode entered\n");
    fmi2Flag = fmu->exitInitializationMode(comp);
    printf("successfully initialized.\n");

    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI exit initialization mode");
        return NULL;
    }


    return comp;
}

static fmi2Status checkForLegacyFMUs(FMU* fmus, int numberOfFMUs, fmi2Boolean *legacyFMU) {
    int i;

    printf("Rolling back FMUs!\n");

    for (i = 0; i < numberOfFMUs - 1; i++) {
        if (!fmus[i].canGetAndSetFMUstate) {
            printf("Legacy FMU has been detected and it's not at the end of the topological sort.\n");
            return fmi2Error;
        }
    }

    // Check for last FMU in topological sort and set boolean flag to fmi2True, if legacy FMU is present
    if(!fmus[numberOfFMUs-1].canGetAndSetFMUstate) {
        *legacyFMU = fmi2True;
        printf("Legacy FMU detected.\n");
    } else { *legacyFMU = fmi2False; }

    return fmi2OK;
}

static fmi2Status rollbackFMUs(FMU *fmus, int numberOfFMUs) {
        int i;
        fmi2Status fmi2Flag;

        printf("Rolling back FMUs!\n");

        for (i = 0; i < numberOfFMUs; i++) {
            fmi2Flag = fmus[i].setFMUstate(fmus[i].component, fmus[i].lastFMUstate);
            if (fmi2Flag > fmi2Warning) {
                    printf("Rolling back FMU %d failed!\n", i+1);
                    return fmi2Flag;
            }
        }

        return fmi2OK;
}

static fmi2Status setValue(portConnection* connection)
{
    fmi2Status fmi2Flag;
    fmi2Integer tempInt;
    fmi2Real tempReal;
    fmi2Boolean tempBoolean;
    fmi2String tempString;

    // get source value and cast if neccessary
    switch (connection->sourceType) {
        case fmi2_Integer :
            fmi2Flag = connection->sourceFMU->getInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt);
            tempReal = (fmi2Real)tempInt;
            tempBoolean = (tempInt == 0 ? fmi2False : fmi2True);
            break;
        case fmi2_Real :
            fmi2Flag = connection->sourceFMU->getReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal);
            tempInt = (fmi2Integer)round(tempReal);
            tempBoolean = (tempReal == 0.0 ? fmi2False : fmi2True);
            break;
        case fmi2_Boolean :
            fmi2Flag = connection->sourceFMU->getBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean);
            tempInt = (fmi2Integer)tempBoolean;
            tempReal = (fmi2Real)tempBoolean;
            break;
        case fmi2_String :
            fmi2Flag = connection->sourceFMU->getString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString);
            break;
        default :
            return fmi2Error;
    }

    if (fmi2Flag > fmi2Warning) {
        printf("Getting the value of an FMU caused an error.");
        return fmi2Flag;
    }

    if (connection->sourceType != connection->sinkType && (connection->sinkType == fmi2_String || connection ->sourceType == fmi2_String)) {
        printf("A connection of FMUs has incompatible data types. Terminating simulation.\n");
        return fmi2Error;
    }

    // set sink value
    switch (connection->sinkType) {
        case fmi2_Integer :
            fmi2Flag = connection->sinkFMU->setInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt);
            break;
        case fmi2_Real :
            fmi2Flag = connection->sinkFMU->setReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal);
            break;
        case fmi2_Boolean :
            fmi2Flag = connection->sinkFMU->setBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean);
            break;
        case fmi2_String :
            fmi2Flag = connection->sinkFMU->setString(connection->sinkFMU->component, &connection->sinkPort, 1, &tempString);
            break;
        default :
            return fmi2Error;
    }
    return fmi2Flag;
}


// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, double h, fmi2Boolean loggingOn, char separator) {

    // set up experiment
    double time = tStart;
    double stepSize = h;

    // temporary variables
    fmi2Boolean doStepOnLegacy = fmi2True;
    fmi2Boolean legacyFMU = fmi2False;
    fmi2Status fmi2Flag = fmi2OK;   // return code of the fmu functions
    fmi2Status simulationFlag = fmi2OK; // status of the whole simulation
    int i = 0;
    int nSteps = 0;
    int returnValue = 1; // 1 = success
    FILE* file;

    // open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }

    // Check for legacy FMUs
    if (checkForLegacyFMUs(fmus, NUMBER_OF_FMUS, &legacyFMU) > fmi2Warning) {
        goto endSimulation;
    }

    // Set input values
    for (i = 0 ; i < NUMBER_OF_EDGES; i++) {
        setValue(&connections[i]);
    }

    // TODO: Should be done by an FMU
    // output solution for time t0
    outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, TRUE);  // output column names
    outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, FALSE); // output values

    // enter the simulation loop

    // TODO: Find consistent error checking!!
    while (time < tEnd) {
        // Run trough the topologically sorted list of FMUs (Master-Step)

        // Set input values
        for (i = 0 ; i < NUMBER_OF_EDGES; i++) {
            setValue(&connections[i]);
        }

/* TODO: getMaxStepSize() support. Needs change of FMI header files.
 *
        // Save current state of all FMUs. Skip legacy FMU if present.
        for (i = 0 ; i < NUMBER_OF_FMUS - legacyFMU ; i++) {
            if (fmus[i]->canGetMaxStepSize) {
                fmi2Flag = fmus[i].getMaxStepSize(fmus[i].component, &tempMaxStepSize);

                if (fmi2Flag > fmi2Warning) {
                    printf("Saving state of FMU at index %d failed. Terminating simulation.", i);
                    goto endSimulation;
                }
                // Update step size if new step size is smaller
                stepSize = min(stepSize, tempMaxStepSize);
            }
        }
*/
        // Save current state of all FMUs. Skip legacy FMU if present.
        for (i = 0 ; i < NUMBER_OF_FMUS - legacyFMU ; i++) {
            fmi2Flag = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);

            if (fmi2Flag > fmi2Warning) {
                printf("Saving state of FMU at index %d failed. Terminating simulation.", i);
                goto endSimulation;
            }
        }

        // reset simulationFlag
        simulationFlag = fmi2OK;

        // Try doStep() for all FMUs and find acceptable stepSize (skip legacy FMU if present and doStepOnLegacy != fmi2True)
        for (i = 0 ; i < NUMBER_OF_FMUS-(legacyFMU && !doStepOnLegacy); i++) {
            // Try doStep() and check if FMU accepts the step size
            fmi2Flag = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2False);

            // Error checking
            if (fmi2Flag > fmi2Discard) {
                    printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
                    returnValue = 0;
                    // Need to free up memory etc.
                    goto endSimulation;
            }

            // If stepSize of the current attempt is rejected set it to the minimum of all processed FMUs
            if (fmi2Flag == fmi2Discard) {
                fmi2Real lastSuccessfulTime;
                fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
                stepSize = min(stepSize, (lastSuccessfulTime - time));  // setting step size to successful step size of current fmu if smaller
                simulationFlag = fmi2Discard;
            }
        }

        // Rolling back FMUs if step size was rejected. Skip legacy FMU if present.
        if (simulationFlag == fmi2Discard) {
            fmi2Flag = rollbackFMUs(fmus, NUMBER_OF_FMUS - legacyFMU );
            if (fmi2Flag > fmi2Warning) {
                printf("Rolling back of FMUs failed. Terminating simulation.");
                goto endSimulation;
            }
        }

        if (simulationFlag != fmi2Discard) {
            time += stepSize;
        }

        // TODO: Should be done by FMU
        outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, FALSE); // output values for this step

        nSteps++;
    }

 endSimulation:
    // end simulation
    for (i = 0 ; i < NUMBER_OF_FMUS; i++)
        {
            fmus[i].terminate(fmus[i].component);
            if (fmus[i].lastFMUstate != NULL) {
                //FIXME: we are ignoring the return value of this call.
                fmus[i].freeFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
            }

            fmus[i].freeInstance(fmus[i].component);
        }

    // print simulation summary
    if (returnValue == 1) {
        printf("Simulation from %g to %g terminated successful\n", tStart, tEnd);
    } else {
        printf("Simulation from %g to %g terminated early!\n", tStart, tEnd);
    }
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %g\n", h);

    fclose(file);

    return returnValue; // 1=success, 0=not success
}

void setupConnections(FMU* fmus, portConnection* connections) {
    connections[0].sourceFMU = &fmus[0];
    connections[0].sourcePort = getValueReference(getScalarVariable(fmus[0].modelDescription, 0));
    connections[0].sourceType = fmi2_Real;
    connections[0].sinkFMU = &fmus[1];
    connections[0].sinkPort = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));
    connections[0].sinkType = fmi2_Real;

    connections[1].sourceFMU = &fmus[1];
    connections[1].sourcePort = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));
    connections[1].sourceType = fmi2_Real;
    connections[1].sinkFMU = &fmus[2];
    connections[1].sinkPort = getValueReference(getScalarVariable(fmus[2].modelDescription, 0));
    connections[1].sinkType = fmi2_Real;
}

int main(int argc, char *argv[]) {
#if WINDOWS
    const char* fmuFileNames[NUMBER_OF_FMUS];
#else
    char* fmuFileNames[NUMBER_OF_FMUS];
#endif
    int i;

    // parse command line arguments and load the FMU
    // default arguments value
    double h = 0.1;
    int loggingOn = 0;
    char csv_separator = ',';
    char **categories = NULL;
    int nCategories = 0;
    fmi2Boolean visible = fmi2False;           // no simulator user interface

    // Create and allocate arrays for FMUs and port mapping
    FMU *fmus = calloc(NUMBER_OF_FMUS, sizeof(FMU));
    portConnection* connections = calloc(NUMBER_OF_EDGES, sizeof(portConnection));

    printf("Parsing arguments!\n");
    parseArguments(argc, argv, fmuFileNames, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);

    // Load and initialize FMUs
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        printf("Loading FMU %d\n", i+1);
        loadFMU(&fmus[i], fmuFileNames[i]);
        fmus[i].component = initializeFMU(&fmus[i], visible, loggingOn, nCategories, categories);
    }

    // Set up port connections
    setupConnections(fmus, connections);

    // run the simulation
    printf("FMU Simulator: run from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c' \n",
            tEnd, h, loggingOn, csv_separator);

    printf("FMUs list:\n");
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        printf("\t%s\n", fmuFileNames[i]);
    }


    printf("log categories={ ");
    for (i = 0; i < nCategories; i++) {
            printf("%s ", categories[i]);
    }
    printf("}\n");

    simulate(fmus, connections, h, loggingOn, csv_separator); // TODO: Create experiment settings struct

    printf("CSV file '%s' written\n", RESULT_FILE);

    // release FMUs
#ifdef _MSC_VER
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
            FreeLibrary(fmus[i]->dllHandle);
    }
#else
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        dlclose(fmus[i].dllHandle);
    }
#endif

    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        freeModelDescription(fmus[i].modelDescription);
    }

    if (categories) {
            free(categories);
    }

    free(fmus);

    return EXIT_SUCCESS;
}
