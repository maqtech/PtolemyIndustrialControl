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
@AcceptedRating Red
@ProposedRating Red
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// ImageUnPartition
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImageUnpartition extends SDFAtomicActor {
    public ImageUnpartition(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "XPartitionSize", new IntToken("4"));
        new Parameter(this, "YPartitionSize", new IntToken("2"));

        partition = (SDFIOPort) newPort("partition");
        partition.setInput(true);
        partition.setTokenConsumptionRate(3168);
        partition.setTypeEquals(IntMatrixToken.class);

        image = (SDFIOPort) newPort("image");
        image.setOutput(true);
        image.setTokenProductionRate(1);
        image.setTypeEquals(IntMatrixToken.class);
    }

    public SDFIOPort partition;
    public SDFIOPort image;

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            ImageUnpartition newobj = (ImageUnpartition)(super.clone(ws));
            newobj.image = (SDFIOPort)newobj.getPort("image");
            newobj.partition = (SDFIOPort)newobj.getPort("partition");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Initialize this actor
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
	Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        yframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("XPartitionSize");
        xpartsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YPartitionSize");
        ypartsize = ((IntToken)p.getToken()).intValue();

        frame = new int[yframesize * xframesize];
        message = new IntMatrixToken[3168];
    }

    /**
     * Fire this actor
     * Consume a single IntMatrixToken on the input.  Produce IntMatrixTokens
     * on the output port by partitioning the input matrix.
     */
    public void fire() {
        int i, j;
	int x, y;
        int a;

        try {
            partition.getArray(0, message);

            for(j = 0, a = 0; j < yframesize; j += ypartsize)
                for(i = 0; i < xframesize; i += xpartsize, a++) {
                    part = message[a].intArray();
                    for(y = 0; y < ypartsize; y++)
                        System.arraycopy(part, y * xpartsize,
                                frame, (j + y) * xframesize + i, xpartsize);
                }

            IntMatrixToken omessage = new IntMatrixToken(frame, 144, 176);
            image.send(0, omessage);
        }
        catch (IllegalActionException e) {
            // getArray and send should never throw an exception.
            throw new InternalErrorException(e.getMessage());
        }
    }

    IntMatrixToken message[];

    private int partitions[][];
    private int part[];
    private int frame[];
    private int xframesize;
    private int yframesize;
    private int xpartsize;
    private int ypartsize;

}
