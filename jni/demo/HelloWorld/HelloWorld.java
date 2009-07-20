/* JNI Example for gcc.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2003-2009 The Regents of the University of California.
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
*/
/**
 * JNI Example for gcc.
 * 
 *  @author From <a href="http://www.inonit.com/cygwin/jni/helloWorld/java.html">http://www.inonit.com/cygwin/jni/helloWorld/java.html"</a>.
 *  @version $Id$
 *  @since Ptolemy II 4.x
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */

package jni.demo.HelloWorld;

public class HelloWorld {
    private static native void writeHelloWorldToStdout();

    // private static void writeHelloWorldToStdout() {
    //    System.out.println("Hello World");
    //}
    public static void main(String[] args) {
        System.loadLibrary("HelloWorld");
        writeHelloWorldToStdout();
    }
}
