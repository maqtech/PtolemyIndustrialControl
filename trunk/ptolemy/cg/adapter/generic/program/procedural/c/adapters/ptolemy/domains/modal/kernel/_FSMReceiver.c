#include "_FSMReceiver.h"

// Constructors of the basic receiver
struct FSMReceiver* FSMReceiver_New() {
	struct FSMReceiver* newReceiver = malloc(sizeof(struct FSMReceiver));
	if (newReceiver == NULL) {
		fprintf(stderr, "Allocation error : FSMReceiver_New (_FSMReceiver.c)\n");
		exit(-1);
	}
	FSMReceiver_Init(newReceiver);
	newReceiver->free = FSMReceiver_New_Free;

	return newReceiver;
}

// Initialisation method
void FSMReceiver_Init(struct FSMReceiver* r) {
	Receiver_Init((struct Receiver*)r);
	r->typeReceiver = FSMRECEIVER;

	r->clear = FSMReceiver_Clear;
	r->elementList = FSMReceiver_ElementList;
	r->get = FSMReceiver_Get;
	r->hasRoom = FSMReceiver_HasRoom;
	r->hasRoom1 = FSMReceiver_HasRoom1;
	r->hasToken = FSMReceiver_HasToken;
	r->hasToken1 = FSMReceiver_HasToken1;
	r->put = FSMReceiver_Put;

	r->_token = emptyToken;
}

// Destructors
void FSMReceiver_New_Free(struct FSMReceiver* r) {
	if (r) {
		free(r);
	}
}

// Other methods
void FSMReceiver_Clear(struct FSMReceiver* r) {
	r->_token = emptyToken;
}
PblList* FSMReceiver_ElementList(struct FSMReceiver* r) {
	PblList* list = pblListNewArrayList();
	pblListAdd(list, &(r->_token));
	return list;
}
Token FSMReceiver_Get(struct FSMReceiver* r) {
	if (r->_token.type == -1) {
		fprintf(stderr, "No Token in the FSM Receiver \
				: FSMReceiver_Get (_FSMReceiver.c)\n");
		exit(-1);
	}

	Token* retour = (Token*)(pblListPeek(r->_queue));
	Token nonDynToken = *retour;
	free(retour);
	return nonDynToken;
}
bool FSMReceiver_HasRoom(struct FSMReceiver* r) {
	return true;
}
bool FSMReceiver_HasRoom1(struct FSMReceiver* r, int numberOfTokens) {
	return numberOfTokens == 1;
}
bool FSMReceiver_HasToken(struct FSMReceiver* r) {
	return r->_token.type != -1;
}
bool FSMReceiver_HasToken1(struct FSMReceiver* r, int numberOfTokens) {
	return numberOfTokens == 1 && r->_token.type != -1;
}
void FSMReceiver_Put(struct FSMReceiver* r, Token token) {
	// FIXME : it is not a relevant comparison
	if (token.type == -1) {
		return;
	}
	r->_token = convert(token, ((struct TypedIOPort*)r->container)->_type);
}
