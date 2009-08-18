/* An actor that specifies the expected behavior in the event of an error in the model's execution.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

 */
package ptolemy.actor.lib;


import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.actor.TypedAtomicActor;

/**
This error actor enables the user to specify how an error is handled in 
C code generated from a Giotto model. In theory it should implement the 
Model Error Handler interface, however I'm not sure if it is correct to 
incorporate error handling in the specification and simulation of a Giotto 
Model since a giotto model only specifies logical execution time.


 @author Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (sssf)
 @Pt.AcceptedRating Red (sssf)
 */
public class Error extends TypedAtomicActor { //should probably also implement the ModelErrorHandler interface//extends Sink {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Error(CompositeEntity container, String name)throws IllegalActionException, NameDuplicationException {
        super(container,name);
        // Parameters
        errorAction = new StringAttribute(this, "ErrorAction");
        errorAction.setExpression("Warn");

        System.out.println("2nd Error Constructor called");
        // Icon is a stop sign named error handler
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-8,-19 8,-19 19,-8 19,8 8,19 "
                + "-8,19 -19,8 -19,-8\" " + "style=\"fill:orange\"/>\n"
                + "<text x=\"-15\" y=\"4\""
                + "style=\"font-size:10; fill:red; font-family:SansSerif\">"
                + "Error Actor</text>\n" + "</svg>\n");
    }


    /** Override the base class to determine which comparison is being
     *  specified.  Read the value of the comparison attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the comparison is not recognized.
     */
    public void attributeChanged(Attribute attribute)
    throws IllegalActionException {
        String errorActionName= "";
        if (attribute == errorAction) {
            errorActionName = errorAction.getExpression().trim();

            if (errorActionName.equals("Warn")) {
                _errorAction = _warn;
            } else if (errorActionName.equals("Reset")) {
                _errorAction = _reset;
            } else if (errorActionName.equals("TimedUtilityFunction")) {
                _errorAction =  _timedutiltiyfunction;
            }  else {
                throw new IllegalActionException(this,
                        "Unrecognized action on error: " + errorActionName);
            }
        }
        else {
            super.attributeChanged(attribute);
        }
    }


public void fire() throws IllegalActionException {
    System.out.print("fire method called"); 
}

public void initialize()
{
    System.out.println("Initialize method called");

}

/** The comparison operator.  This is a string-valued attribute
 *  that defaults to "&gt;".
 */
public StringAttribute errorAction;


///////////////////////////////////////////////////////////////////
////                         private variables                 ////
// An indicator for the comparison to compute.
private int _errorAction;


// Constants used for more efficient execution.
private static final int _warn = 0;

private static final int _reset = 1;

private static final int _timedutiltiyfunction = 2;




}

