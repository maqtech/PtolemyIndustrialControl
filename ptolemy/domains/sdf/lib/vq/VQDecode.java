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
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.io.*;
import java.net.*;

//////////////////////////////////////////////////////////////////////////
//// VQDecode
/**
This actor decompresses a vector quantized signal.   This operation is simply
a table lookup into the codebook.
FIXME This should be generalized to a Table-lookup actor.

@author Steve Neuendorffer
@version $Id$
*/

public final class VQDecode extends SDFAtomicActor {
    public VQDecode(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTypeEquals(IntToken.class);

        output = (SDFIOPort) newPort("output");
        output.setOutput(true);
        output.setTypeEquals(IntMatrixToken.class);

        codeBook = new Parameter(this, "codeBook",
                new StringToken("ptolemy/domains/sdf" +
                        "/lib/vq/data/usc_hvq_s5.dat"));
        blockCount = new Parameter(this, "blockCount", new IntToken("1"));
        _blockCount = ((IntToken)blockCount.getToken()).intValue();
        output.setTokenProductionRate(_blockCount);
        input.setTokenConsumptionRate(_blockCount);
        blockWidth = 
            new Parameter(this, "blockWidth", new IntToken("4"));
        blockHeight = 
            new Parameter(this, "blockHeight", new IntToken("2"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

     /** A Parameter of type String, giving the location of the codebook data
     *  file relative to the root classpath.
     */
    public Parameter codeBook;

    /** The number of blocks to be decoded during each firing.  
     *  The default value is one, which will always work, but using a higher
     *  number (such as the number of blocks in a frame) will speed things up.
     *  This should contain an integer.
     */
    public Parameter blockCount;

    /** The width, in integer pixels, of the block to decode. */
    public Parameter blockWidth;

    /** The width, in integer pixels, of the block to decode. */
    public Parameter blockHeight;

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
            VQDecode newobj = (VQDecode)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.codeBook = (Parameter)newobj.getAttribute("codeBook");
            newobj.blockCount = (Parameter)newobj.getAttribute("blockCount");
            newobj.blockWidth = (Parameter)newobj.getAttribute("blockWidth");
            newobj.blockHeight = (Parameter)newobj.getAttribute("blockHeight");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Fire this actor.
     * Consume a Vector on the input, and perform Vector Quantization using
     * Hierarchical Table-Lookup Vector Quantization.  Send the computed
     * codeword on the output.
     * @exception IllegalActionException if a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        int j;
        input.getArray(0, _codewords);

        for(j = 0; j < _blockCount; j++) {
            /*           System.arraycopy(_codebook[2][_codewords[j].intValue()], 0,
                    _part, 0,
                    _xpartsize * _ypartsize);
            _blocks[j] = new IntMatrixToken(_part, _ypartsize, _xpartsize);
            */
            _blocks[j] = new IntMatrixToken(_codebook[2][_codewords[j].intValue()], _blockHeight, _blockWidth);
        }

        output.sendArray(0, _blocks);
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        InputStream source = null;

        _blockCount = ((IntToken)blockCount.getToken()).intValue();
        input.setTokenConsumptionRate(_blockCount);
        output.setTokenProductionRate(_blockCount);

        _blockWidth = ((IntToken)blockWidth.getToken()).intValue();
        _blockHeight = ((IntToken)blockHeight.getToken()).intValue();

        _codewords =  new IntToken[_blockCount];
        _blocks = new IntMatrixToken[_blockCount];

        String filename = ((StringToken)codeBook.getToken()).stringValue();
         try {
            if (filename != null) {
                if(_baseurl != null) {
                    try {
                        // showStatus("Reading data");
                        URL dataurl = new URL(_baseurl, filename);
                        System.out.println("dataurl = " + dataurl);
                        source = dataurl.openStream();
                        //showStatus("Done");
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("VQDecode: " +
                                "file not found: " + e);
                    } catch (IOException e) {
                        System.err.println(
                                "VQDecode: error reading"+
                                " input file: " + e);
                    }
                } else {
                    File sourcefile = new File(filename);
                    if(!sourcefile.exists() || !sourcefile.isFile())
                        throw new IllegalActionException("Codebook file " +
                                filename + " does not exist!");
                    if(!sourcefile.canRead())
                        throw new IllegalActionException("Codebook file " +
                                filename + " is unreadable!");
                    source = new FileInputStream(sourcefile);
                }
            }

            int i, j, y, x, size = 1;
            byte temp[];
            for(i = 0; i < 5; i++) {
                size = size * 2;
                temp = new byte[size];
                for(j = 0; j < 256; j++) {
                    _codebook[i][j] = new int[size];
                    if(_fullread(source, temp) != size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codebook[i][j][x] = temp[x] & 255;
                }

		// skip over the lookup tables.

                temp = new byte[65536];
                // read in the lookup table.
                if(_fullread(source, temp) != 65536)
                    throw new IllegalActionException("Error reading " +
                            "codebook file!");
            }
        }
        catch (Exception e) {
            throw new IllegalActionException(e.getMessage());
        }
        finally {
            if(source != null) {
                try {
                    source.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    public void setBaseURL(URL baseurl) {
        _baseurl = baseurl;
    }

    protected int _fullread(InputStream s, byte b[]) throws IOException {
        int len = 0;
        int remaining = b.length;
        int bytesread = 0;
        while(remaining > 0) {
            bytesread = s.read(b, len, remaining);
            if(bytesread == -1) throw new IOException(
                    "HTVQEncode: _fullread: Unexpected EOF");
            remaining -= bytesread;
            len += bytesread;
        }
        return len;
    }

    private int _codebook[][][] = new int[6][256][];
    private IntToken _codewords[];
    private IntMatrixToken _blocks[];

    private int _blockCount;
    private int _blockWidth;
    private int _blockHeight;
    private URL _baseurl;
}
