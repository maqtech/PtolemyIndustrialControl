/* Functional Mock-up Interface (FMI) event information.

   Copyright (c) 2012 The Regents of the University of California.
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

package org.ptolemy.fmi;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * An interface that is used by Java Native Access (JNA) to handle callbacks.
 *
 * <p>This class contains implementations of methods that are registered
 * with the FMI and then called back from by the FMI.  The callback
 * methods allocate and free memory, handle logging and are sometimes
 * called when the step ends.  For each callback we define an inner class
 * that implements the appropriate interface and has one method that
 * provides the body of the callback.</p>
 *
 * <p>For details about how Callbacks work in JNA, see
 * <a href="http://twall.github.com/jna/3.4.0/javadoc/overview-summary.html#callbacks">http://twall.github.com/jna/3.4.0/javadoc/overview-summary.html#callbacks</a>.</p>
 *
 * <p>This file is based on a file that was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br> a tool
 * written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses
 * a few opensource projects.</a>.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface FMULibrary extends FMILibrary {

    /** The logging callback function. */
    public class FMULogger implements FMICallbackLogger {

        /** Instantiate a FMULogger.
         *  @param modelDescription The model description that
         *  contains the names of the variables.  The FMI
         *  specification states that the variable names might not be
         *  stored in the C-functions, which is why we can't just use
         *  the fmiComponent.
         */
        public FMULogger(FMIModelDescription modelDescription) {
            _modelDescription = modelDescription;
        }


        /** Log a message.
         *  @param fmiComponent The component that was instantiated.
         *  @param instanceName The name of the instance of the FMU.
         *  @param status The fmiStatus, see
         *  {@link org.ptolemy.fmi.FMILibrary.FMIStatus}
         *  @param category The category of the message,
         *  defined by the tool that created the fmu.  Typical
         *  values are "log" or "error".
         *  @param message The printf style format string.
         */
        public void apply(Pointer fmiComponent, String instanceName,
                int status, String category, String message) {
            // We place this method in separate file for testing purposes.
            FMULog.log(_modelDescription, fmiComponent, instanceName, status, category, message);
        }

        /** The model description that contains the names of the
         * variables.
         */
        protected FMIModelDescription _modelDescription;
    }

    /** Class for the allocate memory callback function. */
    public class FMUAllocateMemory implements FMICallbackAllocateMemory {

        /** Allocate memory.
         *  @param numberOfObjects The number of objects to allocate.
         *  @param size The size of the object in bytes.
         *  @return a Pointer to the allocated memory.
         */
        public Pointer apply(NativeSizeT numberOfObjects, NativeSizeT size) {
            // For hints, see http://markmail.org/message/6ssggt4q6lkq3hen

            int numberOfObjectsValue = numberOfObjects.intValue();
            if (numberOfObjectsValue <= 0) {
                // instantiateModel() in fmuTemplate.c
                // will try to allocate 0 reals, integers, booleans or
                // strings.
                // However, instantiateModel() later checks to see if
                // any of the allocated spaces are null and fails with
                // "out of memory" if they are null.
                numberOfObjectsValue = 1;
            }
            // FIXME: Perhaps the +4 is needed for the align command to work below?
            int bytes = numberOfObjectsValue * size.intValue() + 4;
            Memory memory = new Memory(bytes);
            // FIXME: not sure about alignment.
            Memory alignedMemory = memory.align(4);
            memory.clear();
            Pointer pointer = alignedMemory.share(0);

            // Need to keep a reference so the memory does not get gc'd.
            // Here, we keep a reference to both the Pointer and the Memory.
            // See http://osdir.com/ml/java.jna.user/2008-09/msg00065.html
            pointers.put(pointer, memory);

            return pointer;
        }

        /** Keep references to memory that has been allocated and
         *  avoid problems with the memory being garbage collected.
         *  FindBugs suggests that this be final.
         */
        public static final Map<Pointer,Memory> pointers = new HashMap<Pointer,Memory>();
    }

    /** A class providing a callback method that frees memory.
     */
    public class FMUFreeMemory implements FMICallbackFreeMemory {
        /** Free memory.
         *  @param object The object to be freed.
         */
        public void apply(Pointer object) {
            FMUAllocateMemory.pointers.remove(object);
        }
    }

    /** A callback for when the step is finished. */
    public class FMUStepFinished implements FMIStepFinished {
        /** The step is finished.
         *  @param fmiComponent The FMI component that was instantiate.
         *  @param status The status flag.  See the FMI documentation.
         */
        public void apply(Pointer fmiComponent, int status) {
            // FIXME: More should be done here.
            System.out.println("Java fmiStepFinished: " + fmiComponent + " "
                    + status);
        }
    };
}
