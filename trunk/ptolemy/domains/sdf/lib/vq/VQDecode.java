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
@author Steve Neuendorffer
@version $Id$
*/

public final class VQDecode extends SDFAtomicActor {
    public VQDecode(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        SDFIOPort inputport = (SDFIOPort) newPort("index");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 3168);
        inputport.setDeclaredType(IntToken.class);

        SDFIOPort outputport = (SDFIOPort) newPort("imagepart");
        outputport.setOutput(true);
        setTokenProductionRate(outputport, 3168);
        outputport.setDeclaredType(IntMatrixToken.class);

        Parameter p = new Parameter(this, "Codebook",
                new StringToken("../lib/vq/data/usc_hvq_s5.dat"));
	new Parameter(this, "XFramesize", new IntToken("176"));
        new Parameter(this, "YFramesize", new IntToken("144"));
        new Parameter(this, "XPartitionSize", new IntToken("4"));
        new Parameter(this, "YPartitionSize", new IntToken("2"));

    }


    public void fire() throws IllegalActionException {
        int j;
        int numpartitions =
            _xframesize * _yframesize / _xpartsize / _ypartsize;

        ((SDFIOPort) getPort("index")).getArray(0, _codewords);

        for(j = 0; j < numpartitions; j++) {
            System.arraycopy(_codebook[2][_codewords[j].intValue()], 0,
                    _part, 0,
                    _xpartsize * _ypartsize);
            _partitions[j] = new IntMatrixToken(_part, _ypartsize, _xpartsize);
        }

        ((SDFIOPort) getPort("imagepart")).sendArray(0, _partitions);
    }

    public void initialize() throws IllegalActionException {

        InputStream source = null;

        Parameter p;
	p = (Parameter) getAttribute("XFramesize");
        _xframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YFramesize");
        _yframesize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("XPartitionSize");
        _xpartsize = ((IntToken)p.getToken()).intValue();
        p = (Parameter) getAttribute("YPartitionSize");
        _ypartsize = ((IntToken)p.getToken()).intValue();

        _codewords =
            new IntToken[_yframesize * _xframesize / _ypartsize / _xpartsize];

       _part = new int[_ypartsize * _xpartsize];
       _partitions =
           new IntMatrixToken[_yframesize * _xframesize
                   / _ypartsize / _xpartsize];


        p = (Parameter) getAttribute("Codebook");
        String filename = ((StringToken)p.getToken()).stringValue();
        try {
            if (filename != null) {
                if(_baseurl != null) {
                    try {
                        // showStatus("Reading data");
                        URL dataurl = new URL(_baseurl, filename);
                        System.out.println("dataurl=" + dataurl);
                        source = dataurl.openStream();
                        //showStatus("Done");
                    } catch (MalformedURLException e) {
                        System.err.println(e.toString());
                    } catch (FileNotFoundException e) {
                        System.err.println("RLEncodingApplet: " +
                                "file not found: " +e);
                    } catch (IOException e) {
                        System.err.println(
                                "RLEncodingApplet: error reading"+
                                " input file: " +e);
                    }
                } else {
                    File sourcefile = new File(filename);
                    if(!sourcefile.exists() || !sourcefile.isFile())
                        throw new IllegalActionException("Image file " +
                                filename + " does not exist!");
                    if(!sourcefile.canRead())
                        throw new IllegalActionException("Image file " +
                                filename + " is unreadable!");
                    source = new FileInputStream(sourcefile);
                }
            }

            int i, j, y, x, size = 1;
            byte temp[];
            for(i = 0; i<5; i++) {
                size = size * 2;
                temp = new byte[size];
                for(j = 0; j<256; j++) {
                    _codebook[i][j] = new int[size];
                    if(_fullread(source, temp) != size)
                        throw new IllegalActionException("Error reading " +
                                "codebook file!");
                    for(x = 0; x < size; x++)
                        _codebook[i][j][x] = temp[x];
                }

               // skip over the lookup tables.
                source.skip(65536);

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

    int _fullread(InputStream s, byte b[]) throws IOException {
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

    public void setBaseURL(URL baseurl) {
        _baseurl = baseurl;
    }

    private int _codebook[][][] = new int[6][256][];
    private IntToken _codewords[];
    private IntMatrixToken _partitions[];
    private int _part[];
    private int _xframesize;
    private int _yframesize;
    private int _xpartsize;
    private int _ypartsize;
    private URL _baseurl;

}



