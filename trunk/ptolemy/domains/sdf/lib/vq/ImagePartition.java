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
//// ImagePartition
/**
This actor partitions a image into smaller submatrices.  Each input image
should have dimensions imageColumns by imageRows, and each output image
will have dimensions partitionColumns by partitionRows.  The output matrices
are row scanned from the input image.

@author Steve Neuendorffer
@version $Id$
*/

public final class ImagePartition extends SDFAtomicActor {
    public ImagePartition(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

	imageColumns = 
            new Parameter(this, "imageColumns", new IntToken("176"));
        imageRows = 
            new Parameter(this, "imageRows", new IntToken("144"));
        partitionColumns = 
            new Parameter(this, "partitionColumns", new IntToken("4"));
        partitionRows = 
            new Parameter(this, "partitionRows", new IntToken("2"));

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTokenConsumptionRate(1);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(IntMatrixToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    /** The width of the input matrices */
    public Parameter imageColumns;

    /** The height of the input matrices */
    public Parameter imageRows;

    /** The width of the input partitions */
    public Parameter partitionColumns;

    /** The height of the input partitions */
    public Parameter partitionRows;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            ImagePartition newobj = (ImagePartition)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.imageRows = 
                (Parameter)newobj.getAttribute("imageRows");
            newobj.imageColumns = 
                (Parameter)newobj.getAttribute("imageColumns");
            newobj.partitionRows = 
                (Parameter)newobj.getAttribute("partitionRows");
            newobj.partitionColumns = 
                (Parameter)newobj.getAttribute("partitionColumns");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Initialize this actor
     * @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _imageColumns = ((IntToken)imageColumns.getToken()).intValue();
        _imageRows = ((IntToken)imageRows.getToken()).intValue();
        _partitionColumns = ((IntToken)partitionColumns.getToken()).intValue();
        _partitionRows = ((IntToken)partitionRows.getToken()).intValue();

        part = new int[_partitionColumns * _partitionRows];
        int partitionCount = _imageColumns * _imageRows
                / _partitionColumns / _partitionRows;
        partitions = new IntMatrixToken[partitionCount];
        output.setTokenProductionRate(partitionCount);
    }

    /**
     * Fire this actor
     * Consume a single IntMatrixToken on the input.  Produce IntMatrixTokens
     * on the output port by partitioning the input image.
     *
     * @exception IllegalActionException If the ports are not connected.
     */
    public void fire() throws IllegalActionException {
        int i, j;
	int x, y;
        int a;
        IntMatrixToken message;

        message = (IntMatrixToken) input.get(0);
        image = message.intArray();

        for(j = 0, a = 0 ; j < _imageRows; j += _partitionRows)
            for(i = 0; i < _imageColumns; i += _partitionColumns, a++) {
                for(y = 0; y < _partitionRows; y++)
                    System.arraycopy(image, (j + y) * _imageColumns + i,
                            part, y * _partitionColumns, _partitionColumns);
                partitions[a] = 
                    new IntMatrixToken(part, _partitionRows, _partitionColumns);
            }

        output.sendArray(0, partitions);
    }

    private IntMatrixToken partitions[];

    private int part[];
    private int image[];
    private int _imageColumns;
    private int _imageRows;
    private int _partitionColumns;
    private int _partitionRows;

}
