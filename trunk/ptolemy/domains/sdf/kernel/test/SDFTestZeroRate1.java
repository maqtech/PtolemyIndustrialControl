/* An SDF test actor.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel.test;

import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.sched.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.*;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SDFTestZeroRate1
/**
A test actor for HDF/SDF. This actor contains parameters that make it
easy to set the rates of the input and output ports. This actor
simply discards whatever it reads in and outputs the contents of
the <i>value</i> parameter.

@author Brian K. Vogel
@version $Id$
*/

public class SDFTestZeroRate1 extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFTestZeroRate1(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
	value = new Parameter(this, "value", new IntToken(1));

	input_rate1 = new Parameter(this, "input_rate1", new IntToken(1));
	input_tokenConsumptionRate = 
            new Parameter(input, "tokenConsumptionRate");
        input_tokenConsumptionRate.setExpression("input_rate1");

	output_rate1 = new Parameter(this, "output_rate1", new IntToken(1));
	output_tokenProductionRate = 
            new Parameter(output, "tokenProductionRate");
        output_tokenProductionRate.setExpression("output_rate1");
	// Set the type constraint.
	output.setTypeAtLeast(value);
	

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by this constant source.
     *  By default, it contains an IntToken with value 1.  If the
     *  type of this token is changed during the execution of a model,
     *  then the director will be asked to redo type resolution.
     */
    public Parameter value;

    public Parameter input_rate1;

    public Parameter output_rate1;

    public Parameter input_tokenConsumptionRate;

    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

     /**
      *  @param attribute The attribute that has changed.
      *  @exception IllegalActionException If the parameters are out of range.
      */
     public void attributeChanged(Attribute attribute)
             throws IllegalActionException {
         SDFDirector dir = (SDFDirector)getDirector();
         
         if (dir != null) {
             dir.invalidateSchedule();
         }
         super.attributeChanged(attribute);
     }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        SDFTestZeroRate1 newObject = (SDFTestZeroRate1)super.clone(workspace);
	// Set the type constraint.
	newObject.output.setTypeAtLeast(newObject.value);
        return newObject;
    }

    /** Discard tokens recieved. Send the token in the value parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	for (int i = 0; i < ((IntToken)input_rate1.getToken()).intValue(); 
             i++) {
            input.get(0);
	}
	for (int i = 0; i < ((IntToken)output_rate1.getToken()).intValue();
             i++) {
            output.send(0, value.getToken());
	}
    }

    /**
     * for debuging only...
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
		// debug sdf schedules:
	SDFDirector dir = (SDFDirector)getDirector();
	SDFScheduler scheduler = (SDFScheduler)dir.getScheduler();
	// For debugging the SDF scheduler...
        //StreamListener sa = new StreamListener();
        //scheduler.addDebugListener(sa);
	//

	// Get the SDF Director's scheduler.
	Scheduler s = dir.getScheduler();
	Iterator allactors = s.getSchedule().actorIterator();
	while (allactors.hasNext()) {
	    Actor actor = (Actor)allactors.next();
	    String schedActName = ((Nameable)actor).getName();
	    System.out.println("Actor in scheduler: " + schedActName);
	}	
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////


}
