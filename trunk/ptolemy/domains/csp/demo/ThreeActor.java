/* A  two actor simulation executing a simple get and put rendezvous.

 Copyright (c) 1998 The Regents of the University of California.
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


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;


//////////////////////////////////////////////////////////////////////////
//// ThreeActor Demo
/** 
Source - Transfer - Sink
@author Neil Smyth
@version $Id$
@see classname
@see full-classname
*/
public class ThreeActor {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public ThreeActor() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "Universe");
            Director execdir = new Director("Executive");
            CSPDirector localdir = new CSPDirector("Local Director");
            univ.setExecutiveDirector(execdir);
            univ.setDirector(localdir);

	    CSPSource source = new CSPSource(univ, "Source");
	    CSPTransfer middle = new CSPTransfer(univ, "Transfer");
            CSPSink sink = new CSPSink(univ, "Sink");
            
            IOPort out1 = source.output;
	    IOPort in1 = middle.input;
	    IOPort out2 = middle.output;
            IOPort in2 = sink.input;

            IORelation rel1 = (IORelation)univ.connect(out1, in1, "R1");
            IORelation rel2 = (IORelation)univ.connect(out2, in2, "R2");
            //System.out.println(univ.description(1023));
            System.out.println(univ.getFullName() + " starting!");
            univ.getExecutiveDirector().go(1);
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException(e.getMessage());
        }
    }
}
