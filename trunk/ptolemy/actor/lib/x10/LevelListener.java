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
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.ChoiceAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import x10.Command;

//////////////////////////////////////////////////////////////////////////
//// LevelListener
/** 
 * This actor will output an integer between 0 and 100 whenever a specified
 * command with the specified house and unit code is detected.  If this actor
 * fires and no such command has been issued, then it outputs -1. Only commands
 * that have levels associated with them are supported, namely DIM and BRIGHT.
 * For on-off commands, use CommandSensor.
 * <p>
 * Note also that the CM17A wireless interface is unidirectional, only sending
 * commands to the X10 network, and not receiving from it.  Thus, this
 * actor will not work with it (or more precisely, it will only report
 * commands that are sent out from the same serial port).
 * 
 * @author Colin Cochran and Edward A. Lee
 * @version $Id$
 * @see CommandSensor
 */

public class LevelListener extends Receiver {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LevelListener(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Create output port.    
        level = new TypedIOPort(this, "level", false, true);
        level.setTypeEquals(BaseType.INT);
        
        // Identify the command to detect.
        command = new ChoiceAttribute(this, "command");
        command.addChoice("BRIGHT");
        command.addChoice("DIM");
        command.setExpression("BRIGHT");
        
        // Parameters.        
        houseCode = new StringAttribute(this, "houseCode");
        unitCode = new StringAttribute(this, "unitCode");
        
        houseCode.setExpression("A");
        unitCode.setExpression("1");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The X10 command to listen for.  This is a string with a value
     *  that is one of BRIGHT or DIM. The default is BRIGHT.
     */
    public ChoiceAttribute command;
    
    /** An output with value 0-100, inclusive, is produced on this port
     *  when the specified X10 command is detected for the specified
     *  house and unit codes.
     */
    public TypedIOPort level;
    
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
    
    /** Output an integer between 0 and 100 if the specified command
     *  is sensed with the specified house and unit codes, and output
     *  -1 otherwise.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        // Check whether a command is ready
        if (_commandReady()){
            Command sensedCommand = _getCommand();
            byte function = sensedCommand.getFunctionByte();
            byte functionOfInterest = Command.BRIGHT;
            String commandValue = command.getExpression();
            if (!commandValue.equals("BRIGHT")) {
                functionOfInterest = Command.DIM;
            }
            // String comparison seems easiest here...
            String code = "" + sensedCommand.getHouseCode() + sensedCommand.getUnitCode();
            if((houseCode.getExpression() + unitCode.getExpression()).equals(code)
                    & (function == functionOfInterest)){
                level.send(0, new IntToken(sensedCommand.getLevel()));
            } else {
                level.send(0, new IntToken(-1));
            }
        } 
        
        // Check the command queue for more commands to send.
        if(_commandReady()){
            getDirector().fireAtCurrentTime(LevelListener.this); 
        }
    }   
}