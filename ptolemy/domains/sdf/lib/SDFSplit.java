/*
@Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red
@AcceptedRating Red
*/
package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

/**
 * This actor deterministically splits its input token stream into two
 * streams.
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class SDFSplit extends SDFAtomicActor {
    public SDFSplit(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try{
            input = (TypedIOPort)newPort("input");
            input.setInput(true);
            setTokenConsumptionRate(input, 2);
            input.setDeclaredType(IntToken.class);

            output1 = (TypedIOPort)newPort("output1");
            output1.setOutput(true);
            setTokenProductionRate(output1, 1);
            output1.setDeclaredType(IntToken.class);

            output2 = (TypedIOPort)newPort("output2");
            output2.setOutput(true);
            setTokenProductionRate(output2, 1);
            output2.setDeclaredType(IntToken.class);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFSplit: constructor error");
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    //// 


    public TypedIOPort input;
    public TypedIOPort output1;
    public TypedIOPort output2;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            SDFSplit newobj = (SDFSplit)(super.clone(ws));
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.output1 = (TypedIOPort)newobj.getPort("output1");
            newobj.output2 = (TypedIOPort)newobj.getPort("output2");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    public void fire() throws IllegalActionException {
        IntToken message;

        message = (IntToken)input.get(0);
        output1.send(0, message);
        message = (IntToken)input.get(0);
        output2.send(0, message);
    }

}






