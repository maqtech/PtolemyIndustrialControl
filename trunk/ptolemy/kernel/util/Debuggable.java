/* Generated By:JavaScope: Do not edit this line. Debuggable.java */
/* Interface for objects that can attach debug listeners.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)
*/

package ptolemy.kernel.util; import COM.sun.suntest.javascope.database.js$;import COM.sun.suntest.javascope.database.CoverageUnit; 

//////////////////////////////////////////////////////////////////////////
//// Debuggable
/**
This is an interface for objects that debug listeners can be attached to.

@author Jie Liu
@version $Id$
@see DebugListener
@see DebugEvent
*/

public interface Debuggable { int js$t9761223775720 = js$.setDatabase("/home/eecs/cxh/jsdatabase");String[] js$p={"ptolemy","kernel","util",};CoverageUnit js$c=js$.c(js$p,"Debuggable","/export/maury/maury2/cxh/tmp/ptII/ptolemy/kernel/util/jsoriginal/Debuggable.java",976122374900L,js$Debuggable.js$n());  static final int[] js$a = js$c.counters; 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a debug listener.
     *  If the listener is already in the list, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener);


    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener); int js$t9761223775801=js$.flush(js$c); 

