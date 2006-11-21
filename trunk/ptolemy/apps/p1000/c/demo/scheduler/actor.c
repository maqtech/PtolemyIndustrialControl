/* ACTOR type.

 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

 @author Thomas Huining Feng

 */

#include "actor.h"
#include "scheduler.h"

/**
 * Constant for ACTOR type's method table.
 */
ACTOR_METHOD_TABLE ACTOR_method_table = {
	ACTOR_fire
};

/**
 * Initiate an object of the ACTOR type, and assign a scheduler to it.
 * 
 * @param actor Reference to the ACTOR object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void ACTOR_init(ACTOR* actor, void* actual_ref, SCHEDULER* scheduler) {
	INIT_SUPER_TYPE(ACTOR, GENERAL_TYPE, actor, actual_ref,
		&ACTOR_method_table);
	
	actor->scheduler = scheduler;
	actor->prev = actor->next = NULL;
}

/**
 * Fire the ACTOR.
 * 
 * @param actor Reference to the ACTOR object.
 */
void ACTOR_fire(ACTOR* actor) {
	// Nothing to be done.
}
