/* PortChannelCode

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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import java.util.Hashtable;


//////////////////////////////////////////////////////////////////////////
//// PortChannelCode
/**
   FIXME: add description!!

@author John S. Davis II
@version $Id$

*/

public class PortChannelCode {
    public PortChannelCode(IOPort port, int channel, int code) {
        _port = port;
        _channel = channel;
        _code = code;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     */
    public int getChannel() {
        return _channel;
    } 
     
    /**
     */
    public int getCode() {
        return _code;
    } 
     
    /**
     */
    public IOPort getPort() {
        return _port;
    } 
     
    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    private IOPort _port;
    private int _channel;
    private int _code;
    
}
