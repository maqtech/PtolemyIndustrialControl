#include "_FSMDirector.h"

struct FSMDirector* FSMDirector_New() {
        struct FSMDirector* newDirector = calloc(1, sizeof(struct FSMDirector));
        if (newDirector == NULL) {
                fprintf(stderr, "Allocation error : FSMDirector_New\n");
                exit(-1);
        }
        FSMDirector_Init(newDirector);
        newDirector->free = FSMDirector_New_Free;

        return newDirector;
}
void FSMDirector_Init(struct FSMDirector* director) {
        Director_Init((struct Director*)director);

        director->typeDirector = FSMDIRECTOR;

        director->fire = FSMDirector_Fire;
        director->fireAt = FSMDirector_FireAt;
        director->initialize = FSMDirector_Initialize;
        director->postfire = FSMDirector_Postfire;
        director->prefire = FSMDirector_Prefire;
        director->transferInputs = FSMDirector_TransferInputs;
        director->transferOutputs1 = FSMDirector_TransferOutputs1;
}
void FSMDirector_New_Free(struct FSMDirector* director) {
        Director_New_Free((struct Director*) director);
}



void FSMDirector_Fire(struct FSMDirector* director) {
        director->makeTransitions(director);
}

Time FSMDirector_FireAt(struct FSMDirector* director, struct Actor* actor, Time time, int microstep) {
        struct Actor* container = (struct Actor*) director->container;
        if (container != NULL) {
                struct Director* ExecDirector = container->getExecutiveDirector(container);
                if (director->isEmbedded(director) && ExecDirector != NULL) {
                        Time environmentTime = director->localClock->getEnvironmentTimeForLocalTime(director->localClock, time);
                        Time result = ExecDirector->fireAt(ExecDirector, container, environmentTime, microstep);

                        return director->localClock->getLocalTimeForEnvironmentTime(director->localClock, result);
                }

        }
        director->localClock->setLocalTime(director->localClock, time);
        return time;
}

void FSMDirector_Initialize(struct FSMDirector* director) {
        Director_Initialize((struct Director*) director);

        //director->transferOutputs(director);
        //resetOutputReceivers();
}
bool FSMDirector_Postfire(struct FSMDirector* director) {
        bool result = true;

//        struct FSMActor* controller = director->_controller;
//        result &= controller->postfire();
//        director->_currentLocalReceiverMap = pblMapGet(director->_localReceiverMaps,
//                        controller->currentState(controller), sizeof(struct State*), NULL);

        return result;
}

bool FSMDirector_Prefire(struct FSMDirector* director) {
        return Director_Prefire((struct Director*) director);
}

bool FSMDirector_TransferInputs(struct FSMDirector* director, struct IOPort* port) {
        if (!port->isInput(port) /*|| !port->isOpaque(port)*/) {
                fprintf(stderr, "Attempted to transferInputs on a port is not an opaque input port.");
                exit(-1);
        }
        bool wasTransferred = false;
        PblMap* tokensIn = pblMapNewHashMap();
        for (int i = 0; i < port->getWidth(port); i++) {
                if (i < port->getWidthInside(port)) {
                        if (port->hasToken(port, i)) {
                                Token* t = port->get(port, i);
                                port->sendInside(port, i, t);
                                pblMapAdd(tokensIn, &port, sizeof(struct IOPort*), t, sizeof(Token));
                                wasTransferred = true;
                        }
                } else {
                        if (port->hasToken(port, i)) {
                                port->get(port, i);
                        }
                }
        }
        if (wasTransferred) {
                director->transferModalInputs(tokensIn);
        }
        pblMapFree(tokensIn);
        return wasTransferred;
}
bool FSMDirector_TransferOutputs1(struct FSMDirector* director, struct IOPort* port){
        bool result = false;
        if (!port->isOutput(port) /*|| !port->isOpaque(port)*/) {
                fprintf(stderr, "Attempted to transferOutputs on a port that is not an opaque input port.");
                exit(-1);
        }

        PblMap* tokensOut = pblMapNewHashMap();
        for (int i = 0; i < port->getWidthInside(port); i++) {
                if (port->hasTokenInside(port, i)) {
                        Token* t = port->getInside(port, i);
                        pblMapAdd(tokensOut, &port, sizeof(struct IOPort*), t, sizeof(Token));
                        port->send(port, i, t);
                        result = true;
                }
        }
        if (result) {
                director->transferModalOutputs(tokensOut);
        }
        pblMapFree(tokensOut);
        return result;
}