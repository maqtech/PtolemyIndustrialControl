/* Generated By:JavaScope: Do not edit this line. StreamListener.java */
/* A debug listener that sends messages to a stream or to standard out.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.OutputStream;
import java.io.PrintStream; import COM.sun.suntest.javascope.database.js$;import COM.sun.suntest.javascope.database.CoverageUnit; 

//////////////////////////////////////////////////////////////////////////
//// StreamListener
/**
A debug listener that sends messages to a stream or to the standard output.

@author  Edward A. Lee, Christopher Hylands
@version $Id$
@see NamedObj
@see RecorderListener

*/
public class StreamListener implements DebugListener { static private int js$t0 = js$.setDatabase("/home/eecs/cxh/jsdatabase");static private String[] js$p={"ptolemy","kernel","util",};static private CoverageUnit js$c=js$.c(js$p,"StreamListener","/export/maury/maury2/cxh/tmp/ptII/ptolemy/kernel/util/jsoriginal/StreamListener.java",976122374900L,js$n());  static final int[] js$a = js$c.counters; 

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a debug listener that sends messages to the standard output.
     */
    public StreamListener() { try{  js$.g(StreamListener.js$a,1); 
         js$.g(StreamListener.js$a,0);/*$js$*/ _output = System.out; }finally{js$.flush(StreamListener.js$c);} 
    }


    /** Create a debug listener that sends messages to the specified stream.
     */
    public StreamListener(OutputStream out) { try{  js$.g(StreamListener.js$a,3); 
         js$.g(StreamListener.js$a,2);/*$js$*/ _output = new PrintStream(out); }finally{js$.flush(StreamListener.js$c);} 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Print a string representation of the event to the stream
     *  associated with this listener.
     */
    public void event(DebugEvent event) {try  { js$.g(StreamListener.js$a,5); 
	 js$.g(StreamListener.js$a,4);/*$js$*/ _output.println(event.toString());
    } finally{js$.flush(StreamListener.js$c);}} 

    /** Copy the message argument to the stream associated with
     *  the listener.  Note that a newline is appended to the
     *  end of the message.
     */
    public void message(String message) {try  { js$.g(StreamListener.js$a,7); 
         js$.g(StreamListener.js$a,6);/*$js$*/ _output.println(message);
    } finally{js$.flush(StreamListener.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private PrintStream _output; static private int js$n() {return 8;}  static private int js$t1=js$.flush(StreamListener.js$c);private int js$t2=js$.flush(StreamListener.js$c); 
