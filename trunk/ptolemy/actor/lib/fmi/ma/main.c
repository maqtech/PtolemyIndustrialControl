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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "fmi.h"
#include "sim_support.h"

#define NUMBER_OF_FMUS 2

double tEnd = 1.0;
double tStart = 0;                       // start time



static fmiComponent initializeFMU(FMU *fmu, fmiCallbackFunctions callbacks, fmiBoolean visible, fmiBoolean loggingOn, int nCategories, char ** categories)
{

    fmiStatus fmiFlag;                       // return code of the fmu functions

    // instantiate the fmu
    callbacks.logger = fmuLogger;
    callbacks.allocateMemory = calloc;
    callbacks.freeMemory = free;
    callbacks.stepFinished = NULL; // fmiDoStep has to be carried out synchronously
    callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.

    fmiReal tolerance = 0;                   // used in setting up the experiment
    fmiBoolean toleranceDefined = fmiFalse;  // true if model description define tolerance
    ValueStatus vs = valueIllegal;

	// handle to the parsed XML file
    ModelDescription* md = fmu->modelDescription;
    // global unique id of the fmu
    const char *guid = getAttributeValue((Element *)md, att_guid);
    // instance name
    const char *instanceName = getAttributeValue((Element *)getCoSimulation(md), att_modelIdentifier);
    // path to the fmu resources as URL, "file://C:\QTronic\sales"
    char *fmuResourceLocation = getTempResourcesLocation();   // TODO: returns crap. got to save the location for every FMU somehow.
    // instance of the fmu
    fmiComponent comp = fmu->instantiate(instanceName, fmiCoSimulation, guid, fmuResourceLocation,
            &callbacks, visible, loggingOn);
    printf("instance name: %s,\n guid: %s,\n ressourceLocation: %s\n", instanceName, guid, fmuResourceLocation);
    free(fmuResourceLocation);

    if (!comp)
    {
    	return NULL;
    }

    Element *defaultExp = getDefaultExperiment(fmu->modelDescription);
    if (defaultExp) tolerance = getAttributeDouble(defaultExp, att_tolerance, &vs);
    if (vs == valueDefined) {
        toleranceDefined = fmiTrue;
    }


    if (nCategories > 0) {
        fmiFlag = fmu->setDebugLogging(comp, fmiTrue, nCategories, (const fmiString*) categories);
        if (fmiFlag > fmiWarning) {
            error("could not initialize model; failed FMI set debug logging");
            return NULL;
        }
    }

    fmiFlag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart, fmiTrue, tEnd);
    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI setup experiment");
        return NULL;
    }
    fmiFlag = fmu->enterInitializationMode(comp);
    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI enter initialization mode");
        return NULL;
    }
    printf("initialization mode entered\n");
    fmiFlag = fmu->exitInitializationMode(comp);
    printf("successfully initialized.\n");

    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI exit initialization mode");
        return NULL;
    }


    return comp;
}


// simulate the given FMU from tStart = 0 to tEnd.
static int simulate(FMU **fmuArray, double tEnd, double h, fmiBoolean loggingOn, char separator,
        int nCategories, char ** categories) {

    double time;
    fmiStatus fmiFlag;                       // return code of the fmu functions
    fmiBoolean visible = fmiFalse;           // no simulator user interface

    fmiCallbackFunctions callbacksOne;          // called by the model during simulation
    fmiCallbackFunctions callbacksTwo;          // called by the model during simulation
    int nSteps = 0;
    FILE* file;

    FMU *fmuOne = fmuArray[0];
    FMU *fmuTwo = fmuArray[1];

    fmiComponent compOne = initializeFMU(fmuArray[0], callbacksOne, visible, loggingOn, nCategories, categories);
    fmiComponent compTwo = initializeFMU(fmuArray[1], callbacksTwo, visible, loggingOn, nCategories, categories);

    if (!compOne)
    {
    	return error("could not instantiate model 1");
    }

    if (!compTwo)
    {
    	return error("could not instantiate model 1");
    }

    // open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }


    // output solution for time t0
    outputRow(fmuTwo, compTwo, tStart, file, separator, TRUE);  // output column names
    outputRow(fmuTwo, compTwo, tStart, file, separator, FALSE); // output values
    printf("zwischenstand\n");

    // enter the simulation loop
    time = tStart;
    fmiInteger tempInt;
    fmiReal tempReal;
    fmiValueReference inputTwo = getValueReference(getScalarVariable(fmuArray[1]->modelDescription, 0));
    fmiValueReference outputOne = getValueReference(getScalarVariable(fmuArray[0]->modelDescription, 0));


    while (time < tEnd) {
    	int i;
    	for (i = 0 ; i < NUMBER_OF_FMUS; i++)
    	{
            fmiFlag = fmuArray[i]->doStep(compOne, time, h, fmiTrue);
            if (fmiFlag > fmiWarning)
            {
            	return error("could not complete simulation of the model");
            }
    	}

    	for (i = 0 ; i < NUMBER_OF_FMUS-1; i++)
    	{
    		fmiFlag = fmuArray[i]->getInteger(compOne, &outputOne, 1, &tempInt);
    		fmiFlag = fmuArray[i]->setInteger(compOne, &outputOne, 1, &tempInt);
    		tempReal = (fmiReal)tempInt;
    		fmiFlag = fmuArray[i+1]->setReal(compTwo, &inputTwo, 1, &tempReal);
    	}

        time += h;
        outputRow(fmuArray[1], compTwo, time, file, separator, FALSE); // output values for this step

        nSteps++;
    }

    // end simulation
    fmuOne->terminate(compOne);
    fmuOne->freeInstance(compOne);
    fmuTwo->terminate(compTwo);
    fmuTwo->freeInstance(compTwo);

    // print simulation summary
    printf("Simulation from %g to %g terminated successful\n", tStart, tEnd);
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %g\n", h);

    fclose(file);

    return 1; // success
}

int main(int argc, char *argv[]) {
#if WINDOWS
    const char* fmuFileName1;
    const char* fmuFileName2;
#else
    char* fmuFileName1;
    char* fmuFileName2;
#endif
    int i;
    
    // parse command line arguments and load the FMU
    // default arguments value
    double h=0.1;
    int loggingOn = 0;
    char csv_separator = ',';
    char **categories = NULL;
    int nCategories = 0;
    
    // Create FMU array and allocate memory
    FMU **fmus;
    fmus = calloc(sizeof(FMU*), NUMBER_OF_FMUS);
    FMU *fmuOne = calloc(sizeof(FMU), 1);
    FMU *fmuTwo = calloc(sizeof(FMU), 1);
    *fmus = fmuOne;
    *(fmus + 1) = fmuTwo;
    
    printf("Parsing arguments!\n");
    parseArguments(argc, argv, &fmuFileName1, &fmuFileName2, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);
    printf("Loading FMU1\n");
    loadFMU(fmuOne, fmuFileName1);
    printf("Loading FMU2\n");
    loadFMU(fmuTwo, fmuFileName2);

  // run the simulation
    printf("FMU Simulator: run '%s' from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c' ",
            fmuFileName1, tEnd, h, loggingOn, csv_separator);
    printf("log categories={ ");
    for (i = 0; i < nCategories; i++) printf("%s ", categories[i]);
    printf("}\n");

    simulate(fmus, tEnd, h, loggingOn, csv_separator, nCategories, categories);
    printf("CSV file '%s' written\n", RESULT_FILE);

    // release FMU
#ifdef _MSC_VER
    FreeLibrary(fmuOne->dllHandle);
    FreeLibrary(fmuTwo->dllHandle);
#else
    dlclose(fmuOne->dllHandle);
    dlclose(fmuTwo->dllHandle);
#endif
    freeModelDescription(fmuOne->modelDescription);
    freeModelDescription(fmuTwo->modelDescription);
    if (categories) free(categories);

    return EXIT_SUCCESS;
}
