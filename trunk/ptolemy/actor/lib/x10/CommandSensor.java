/* This actor senses specified X10 commands.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@ptolemy.eecs.berkeley.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.x10;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.ChoiceAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// CommandSensor
/** 
 * This actor will output a <i>true</i> whenever a specified command with the
 * specified house and unit code is detected.  Only commands that are present
 * or absent are supported by this actor. Use LevelSensor for commands (like
 * BRIGHT and DIM) that have levels associated with them.
 * <p>
 * A typical use of this actor is to have a
 * motion sensor (or "occupancy sensor") with the specified house and unit
 * code. If there is a such a motion sensor, then this actor will output
 * true when it detects command.
 * <p>
 * Note that an X10 motion sensor can be used to turn on and off devices
 * directly if they have the same house and unit codes. The motion sensor
 * will broadcast an on command and after some time an off command for that
 * house and unit code.  This actor, however, only reacts to the on command
 * by producing an output <i>true</i>.
 * 
 * @author Colin Cochran and Edward A. Lee
 * @version $Id$
 */

public class CommandSensor extends Receiver {

	/** Construct an actor with the given container and name.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the actor cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public CommandSensor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException  {
		super(container, name);

		// Create output port.    
		detected = new TypedIOPort(this, "detected", false, true);
        new SingletonAttribute(detected, "_showName");
        // Output true if detected is detected.
        detected.setTypeEquals(BaseType.BOOLEAN);
        
        // Identify the command to detect.
        command = new ChoiceAttribute(this, "command");
        command.addChoice("ON");
        command.addChoice("OFF");
        command.addChoice("ALL_LIGHTS_ON");
        command.addChoice("ALL_LIGHTS_OFF");
        command.addChoice("ALL_UNITS_OFF");
        command.setExpression("ON");
        
        // Parameters.        
		houseCode = new StringAttribute(this, "houseCode");
        unitCode = new StringAttribute(this, "unitCode");
        
		houseCode.setExpression("A");
        unitCode.setExpression("1");
	}
	
	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
    
    /** The X10 command to listen for.  This is a string with a value
     *  that is one of ALL_LIGHTS_OFF, ALL_LIGHTS_ON, ALL_UNITS_OFF,
     *  OFF, or ON.  The default is ON.
     */
    public ChoiceAttribute command;
    
	/** An output with value true is produced on this port when the specified
     *  X10 command is detected for the specified house and unit codes.
	 */
	public TypedIOPort detected;
	
	/** This string is the house code for the command that this
     *  actor listens for. The default value is "A".
	 */
	public StringAttribute houseCode;
    
    /** This string is the unit code for the command that this
     *  actor listens for. The default value is "1".
     */
    public StringAttribute unitCode;
    
	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////
    
    /** Output true if the specified command is sensed with the specified
     *  house and unit codes, and output false otherwise.
	 *  @exception IllegalActionException If the super class throws it.
	 */
	public void fire() throws IllegalActionException {
		super.fire();
        
        // Check whether a command is ready
        if (_commandReady()){
        
            Command command = _getCommand();
       
            byte function = command.getFunctionByte(); 
       
            String code = "" + command.getHouseCode() + command.getUnitCode();
        
            if((houseCode.getExpression() + unitCode.getExpression()).equals(code)
                    & (function == x10.Command.ON)){
                detected.send(0, new BooleanToken(true));
            } else {
                detected.send(0, new BooleanToken(false));
            }
        } 
        
        // Check the command queue for more commands to send.
        if(_commandReady()){
            getDirector().fireAtCurrentTime(CommandSensor.this); 
        }
	}	
}
