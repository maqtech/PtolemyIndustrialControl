/* Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.

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

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;

import org.ptolemy.fmi.FMILibrary.FMIStatus;
import org.ptolemy.fmi.FMICallbackFunctions.ByValue;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;


///////////////////////////////////////////////////////////////////
//// FMUCoSimulation

/** Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.
 *  
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUCoSimulation {


    public interface FMULibrary extends FMILibrary {
        FMULibrary INSTANCE = (FMULibrary)
            // FIXME: get this from the .fmu
            // Also: update FMILibrary.JNA_LIBRARY_NAME
            Native.loadLibrary("/Users/cxh/src/fmu/jna2/cs/binaries/darwin64/bouncingBall.dylib",
                    FMULibrary.class);

        public class fmiLogger implements FMICallbackLogger {
            // What to do about jni callbacks with varargs?  
            // See http://osdir.com/ml/java.jna.user/2008-08/msg00103.html
            // I'm getting an exception:
            // "Callback argument class [Lcom.sun.jna.Pointer; requires custom type conversion"
            public void apply(Pointer c, Pointer instanceName, int status, Pointer category, Pointer message, String ... parameters) {
                System.out.println("Java fmiLogger, status: " + status);
                System.out.println("Java fmiLogger, message: " + message.getString(0));
            }
        }
        //http://markmail.org/message/6ssggt4q6lkq3hen

        public class fmiAllocateMemory implements FMICallbackAllocateMemory {
            public Pointer apply(NativeSizeT nobj, NativeSizeT size) {
                int numberOfObjects = nobj.intValue();
                if (numberOfObjects <= 0) {
                    // instantiateModel() in fmuTemplate.c
                    // will try to allocate 0 reals, integers, booleans or strings.
                    // However, instantiateModel() later checks to see if
                    // any of the allocated spaces are null and fails with
                    // "out of memory" if they are null.
                    numberOfObjects = 1;
                }
                Memory memory = new Memory(numberOfObjects * size.intValue());
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);

//                 System.out.println("Java fmiAllocateMemory " + nobj + " " + size
//                         + "\n        memory: " + memory + " " + Pointer.nativeValue(memory) + " " + memory.SIZE + " " + memory.SIZE % 4
//                         + "\n alignedMemory: " + alignedMemory + " " + Pointer.nativeValue(alignedMemory) + " " + alignedMemory.SIZE + " " + alignedMemory.SIZE %4
//                         + "\n       pointer: " + pointer + " " + Pointer.nativeValue(pointer) + " " + pointer.SIZE + " " + pointer.SIZE % 4
//                                    );


                return pointer;
            }
        }

        public class fmiFreeMemory implements FMICallbackFreeMemory {
            public void apply(Pointer obj) {
                System.out.println("Java fmiFreeMemory " + obj);
            }
        }
	public class stepFinished implements FMIStepFinished {
            public void apply(Pointer c, int status) {
                System.out.println("Java fmiStepFinished: " + c + " " + status);
            }
	};
    }

    public static void main(String[] args) throws Exception {
        String fmuFileName = args[0];

        FMIModelDescription fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

        NativeLibrary nativeLibrary = NativeLibrary.getInstance(FMUFile.fmuSharedLibrary(
                        fmiModelDescription, fmuFileName));

        String modelName = fmiModelDescription.modelName;
        Function function = nativeLibrary.getFunction(modelName + "_fmiGetVersion");
        
        // The URL of the fmu file.
        String fmuLocation = null;  
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 1000;
        // There is no simulator UI.
        byte visible = 0;
        // Run the simulator without user interaction.
        byte interactive = 0;
        // Callbacks
        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
                new FMULibrary.fmiLogger(),
                new FMULibrary.fmiAllocateMemory(),
                new FMULibrary.fmiFreeMemory(),
                new FMULibrary.stepFinished());
        // Turn off logging.
        byte loggingOn = (byte)0;

        Function instantiateSlave = nativeLibrary.getFunction(modelName + "_fmiInstantiateSlave");
        Pointer fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                new Object [] {
                    modelName,
                    "{8c4e810f-3df3-4a00-8276-176fa3c9f003}",
                    fmuLocation,
                    mimeType,
                    timeout,
                    visible,
                    interactive,
                    callbacks,
                    loggingOn});
        if (fmiComponent.equals(Pointer.NULL)) {
            new RuntimeException("Could not instantiate model.");
        }

        double startTime = 0;
        double endTime = 5.0;
        
        function = nativeLibrary.getFunction(modelName + "_fmiInitializeSlave");
        int fmiFlag = ((Integer)function.invoke(Integer.class,new Object[] {fmiComponent, startTime, (byte)1, endTime})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException("Could not initialize slave: " + fmiFlag);
        }
        
        File outputFile = new File("results.csv"); 
        PrintStream file = null;
        try {
            file = new PrintStream(outputFile);
            char separator = ',';
            // Generate header row
            OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, startTime, file, separator, Boolean.TRUE);  
            // Output the initial values.
            OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, startTime, file, separator, Boolean.FALSE);
            // Loop until the time is greater than the end time.
            double time = startTime;
            double stepSize = 0.1;
            function = nativeLibrary.getFunction(modelName + "_fmiDoStep");
            while (time < endTime) {
                fmiFlag = ((Integer)function.invokeInt(new Object[] {fmiComponent, time, stepSize, (byte)1})).intValue();

                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not simulate.  Time was " + time);
                }
                time += stepSize;
                // Generate a line for this step
                OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, time, file, separator, Boolean.FALSE);
            }
        } finally {            
            if (file != null) {
                file.close();
            }
         }

        function = nativeLibrary.getFunction(modelName + "_fmiTerminateSlave");
        fmiFlag = ((Integer)function.invokeInt(new Object[] {fmiComponent})).intValue();

        function = nativeLibrary.getFunction(modelName + "_fmiFreeSlaveInstance");
        fmiFlag = ((Integer)function.invokeInt(new Object[] {fmiComponent})).intValue();
        System.out.println("Results are in " + outputFile);
  }
}