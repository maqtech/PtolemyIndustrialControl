/* In this file we have defined the structure of a DE Event
 * It contains a pointer to its actor, its destination port
 * and a few fields (timestamp, microstep, priority, depth)
 *
 * @author : William Lucas
 */

#ifndef DE_EVENT
#define DE_EVENT

#include "_types.h"
#include "_Actor.h"
#include "_IOPort.h"

struct DEEvent {
	struct Actor* _actor;
	int _depth;
	struct IOPort* _ioPort;
	int _microstep;
	int _priority;
	Time _timestamp;

	void (*free)(struct DEEvent*);

	struct Actor* (*actor)(struct DEEvent*);
	int (*compareTo)(struct DEEvent*, struct DEEvent*);
	int (*depth)(struct DEEvent*);
	bool (*equals)(struct DEEvent*, struct DEEvent*);
	long (*getVirtualBinNumber)(struct DEEvent*, double);
	bool (*hasTheSameTagAndDepthAs)(struct DEEvent*, struct DEEvent*);
	bool (*hasTheSameTagAs)(struct DEEvent*, struct DEEvent*);
	struct IOPort* (*ioPort)(struct DEEvent*);
	int (*microstep)(struct DEEvent*);
	Time (*timeStamp)(struct DEEvent*);
};

struct DEEvent* DEEvent_New();
void DEEvent_Init(struct DEEvent* event);
void DEEvent_New_Free(struct DEEvent* event);

struct Actor* DEEvent_Actor(struct DEEvent* event);
int DEEvent_CompareTo(struct DEEvent* event, struct DEEvent* event2);
int DEEvent_Depth(struct DEEvent* event);
bool DEEvent_Equals(struct DEEvent* event, struct DEEvent* event2);
long DEEvent_GetVirtualBinNumber(struct DEEvent* event, double binWidth);
bool DEEvent_HasTheSameTagAndDepthAs(struct DEEvent* event, struct DEEvent* event2);
bool DEEvent_HasTheSameTagAs(struct DEEvent* event, struct DEEvent* event2);
struct IOPort* DEEvent_IOPort(struct DEEvent* event);
int DEEvent_Microstep(struct DEEvent* event);
Time DEEvent_TimeStamp(struct DEEvent* event);


#endif
