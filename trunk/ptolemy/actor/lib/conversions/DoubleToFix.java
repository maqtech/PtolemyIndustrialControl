/* An actor that convert an input stream containing DoubleToken types
into a stream of FixToken Types.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu) */

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// DoubleToFix
/**
Read a token and converts it to a FixToken with a given precision.

@author Bart Kienhuis 
@version $Id$
*/

public class DoubleToFix extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleToFix(TypedCompositeActor container, String name)
	throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
	output.setTypeEquals(BaseType.FIX);
   
	precision = new Parameter(this, "precision", new StringToken(""));
        precision.setTypeEquals(BaseType.STRING);
   
	mode = new Parameter(this, "mode", new StringToken("SATURATE"));
        mode.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. This parameter contains
     *  a StringToken.
     */
    public Parameter precision;

    public Parameter mode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one token from each input and convert the Token
     *  value in a FixToken with a given precision.  
     *
     * @exception IllegalActionException If there is no director.  
     */

    public void fire() throws IllegalActionException {
	String _precision = ((StringToken) precision.getToken()).toString();
	String _mode = ((StringToken) mode.getToken()).toString();
	// System.out.println(" PRECISION: " + _precision);
	if (input.hasToken(0)) {
    	    DoubleToken in = (DoubleToken)input.get(0);
	    FixToken result = new FixToken(_precision, in.doubleValue() );    
	    result.setRoundingMode( _mode );
            output.send(0, result);
        }
    }

}
