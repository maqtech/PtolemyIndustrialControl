/* Generated By:JavaScope: Do not edit this line. InternalErrorException.java */
/* Some object or set of objects has a state that in theory is not permitted.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util; import COM.sun.suntest.javascope.database.js$;import COM.sun.suntest.javascope.database.CoverageUnit; 

//////////////////////////////////////////////////////////////////////////
//// InternalErrorException
/**
This exception should be thrown if an unexpected error is encountered
other than one for which InconsistentStateException would be appropriate.
Our design should make it impossible for this exception to ever occur,
so occurrence is a bug. This exception is a RuntimeException
so that it does not have to be declared.

@author Edward A. Lee
@version $Id$
*/
public class InternalErrorException extends RuntimeException { static private int js$t0 = js$.setDatabase("/home/eecs/cxh/jsdatabase");static private String[] js$p={"ptolemy","kernel","util",};static private CoverageUnit js$c=js$.c(js$p,"InternalErrorException","/export/maury/maury2/cxh/tmp/ptII/ptolemy/kernel/util/jsoriginal/InternalErrorException.java",976122374900L,js$n());  static final int[] js$a = js$c.counters; 

    /** Constructs an Exception with a detail message.
     *  @param detail The message.
     */
    public InternalErrorException(String detail) { try{  js$.g(InternalErrorException.js$a,1); 
         js$.g(InternalErrorException.js$a,0);/*$js$*/ _setMessage(detail); }finally{js$.flush(InternalErrorException.js$c);} 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the detail message. */
    public String getMessage() {try  { js$.g(InternalErrorException.js$a,3); 
         js$.g(InternalErrorException.js$a,2);/*$js$*/ return _message;
    } finally{js$.flush(InternalErrorException.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Sets the error message to the specified string.
     *  @param msg The message.
     */
    protected void _setMessage(String msg) {try  { js$.g(InternalErrorException.js$a,5); 
         js$.g(InternalErrorException.js$a,4);/*$js$*/ _message = msg;
    } finally{js$.flush(InternalErrorException.js$c);}} 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The detail message. */
    private String _message ; static private int js$n() {return 6;}  static private int js$t1=js$.flush(InternalErrorException.js$c);private int js$t2=js$.flush(InternalErrorException.js$c); 
