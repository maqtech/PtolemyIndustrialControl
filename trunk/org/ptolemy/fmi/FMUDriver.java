/* Base class to invoke a Functional Mock-up Unit (.fmu) file as
   either co-simulation or model exchange.

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

import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// FMUDriver

/** Base class to invoke a Functional Mock-up Unit (.fmu) file as
 *  either co-simulation or model exchange.
 *
 *  <p>Derived classes should implement the simulate(...) method and
 *  create a static main(String args) method that invokes
 *  _processArgs(args) and them simulate(...).</p>
 *
 *  @author Christopher Brooks
 *  @version $Id: FMUCoSimulation.java 63359 2012-04-16 06:45:49Z cxh $
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public abstract class FMUDriver {

    /** Return a function by name.
     *  @param name The name of the function.
     */
    public Function getFunction(String name) {
        // This is syntactic sugar.
        if (_enableLogging) {
            System.out.println("FMUModelExchange: about to get the " + name
                    + " function.");
        }
        return _nativeLibrary.getFunction(_modelIdentifier + name);
    }


    /** Invoke a function that returns an integer
     *  @param name The name of the function.
     */
    public void invoke(String name, Object [] arguments, String message) {
        Function function = getFunction(name);
        invoke(function, arguments, message);
    }

    /** Invoke a function that returns an integer
     *  @param name The name of the function.
     */
    public void invoke(Function function, Object [] arguments, String message) {
        if (_enableLogging) {
            System.out.println("About to call " + function.getName());
        }
        int fmiFlag = ((Integer) function.invoke(Integer.class,
                        arguments)).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException(message + fmiFlag);
        }
    }

    /** Perform co-simulation or model exchange using the named
     * Functional Mock-up Unit (FMU) file.
     *  
     *  <p>Derived classes should implement this method.</p>
     *
     *  @param fmuFileName The pathname of the co-simulation .fmu file
     *  @param endTime The ending time in seconds.
     *  @param stepSize The step size in seconds.
     *  @param enableLogging True if logging is enabled.
     *  @param csvSeparator The character used for separating fields.
     *  Note that sometimes the decimal point in floats is converted to ','.
     *  @param outputFileName The output file.
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */
    public abstract void simulate(String fmuFileName, double endTime,
            double stepSize, boolean enableLogging, char csvSeparator,
            String outputFileName) throws Exception;

    ///////////////////////////////////////////////////////////////////
    ////                      protected fields                     ////

    /** Process command line arguments for co-simulation or model exchange of
     *  Functional Mock-up Unit (.fmu) files.   
     *          
     *  <p>The command line arguments have the following meaning:</p>
     *  <dl>
     *  <dt>file.fmu</dt>
     *  <dd>The co-simulation or model exchange Functional Mock-up
     *  Unit (FMU) file.  In FMI-1.0, co-simulation fmu files contain
     *  a modelDescription.xml file that has an &lt;Implementation&gt;
     *  element.  Model exchange fmu files do not have this
     *  element.</dd>
     *  <dt>endTime</dt>
     *  <dd>The endTime in seconds, defaults to 1.0.</dd>
     *  <dt>stepTime</dt>
     *  <dd>The time between steps in seconds, defaults to 0.1.</dd>
     *  <dt>enableLogging</dt>
     *  <dd>If "true", then enable logging.  The default is false.</dd>
     *  <dt>outputFile</dt>
     *  <dd>The name of the output file.  The default is results.csv</dd>
     *  </dl>
     *
     *  <p>The format of the arguments is based on the fmusim command from the fmusdk
     *  by QTronic Gmbh.</p>
     *
     *  @param args The arguments: file.fmu [endTime] [stepTime]
     *  [loggingOn] [csvSeparator] [outputFile]
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */
    protected static void _processArgs(String[] args) throws Exception {
        _fmuFileName = args[0];
        if (args.length >= 2) {
            _endTime = Double.valueOf(args[1]);
        }
        if (args.length >= 3) {
            _stepSize = Double.valueOf(args[2]);
        }
        if (args.length >= 4) {
            _enableLogging = Boolean.valueOf(args[3]);
        }
        if (args.length >= 5) {
            if (args[4].equals("c")) {
                _csvSeparator = ',';
            } else if (args[4].equals("s")) {
                _csvSeparator = ';';
            } else {
                _csvSeparator = args[4].charAt(0);
            }
        }
        if (args.length >= 6) {
            _outputFileName = args[5];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** An interface that contains JNA callbacks.
     */
    public interface FMULibrary extends FMILibrary {
        /** The logging function.
         * We need a class that implement the interface because
         * certain methods require interfaces as arguments, yet we
         * need to have method bodies, so we need an actual class.
         */
        public class FMULogger implements FMICallbackLogger {
            /** Log a message.
             *  Note that arguments after the message are currently ignored.   
             *  @param fmiComponent The component that was instantiated.
             *  @param instanceName The name of the instance of the FMU.
             *  @param status The fmiStatus, see
             *  {@link org.ptolemy.fmi.FMILibrary.FMIStatus}
             *  @param category The category, typically "log" or "error".
             *  @param message The message
             */
            public void apply(Pointer fmiComponent, String instanceName,
                    int status, String category, String message/*
                                                                * , Pointer ...
                                                                * parameters
                                                                */) {
                // FIXME: What to do about jni callbacks with varargs?
                // See
                // http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCalbackLogger

                System.out.println("Java FMULogger, status: " + status);
                System.out.println("Java FMULogger, message: " + message/*
                                                                         * .
                                                                         * getString
                                                                         * (0)
                                                                         */);
            }
        }

        /** Allocate memory. */
        public class FMUAllocateMemory implements FMICallbackAllocateMemory {
            // http://markmail.org/message/6ssggt4q6lkq3hen

            /** Allocate memory.
             *  @param numberOfObjects The number of objects to allocate.
             *  @param size The size of the object in bytes.
             *  @return a Pointer to the allocated memory.
             */
            public Pointer apply(NativeSizeT numberOfObjects, NativeSizeT size) {
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
                Memory memory = new Memory(numberOfObjectsValue
                        * size.intValue());
                // FIXME: not sure about alignment.
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);

                // Need to keep a reference so the memory does not get gc'd.
                // See http://osdir.com/ml/java.jna.user/2008-09/msg00065.html
                _pointers.add(pointer);

                // System.out.println("Java fmiAllocateMemory " +
                // numberOfObjects + " " + size
                // + "\n        memory: " + memory + " " + + memory.SIZE + " " +
                // memory.SIZE % 4
                // + "\n alignedMemory: " + alignedMemory + " " +
                // alignedMemory.SIZE + " " + alignedMemory.SIZE %4
                // + "\n       pointer: " + pointer + " " + pointer.SIZE + " " +
                // (pointer.SIZE % 4));
                return pointer;
            }
        }

        /** A callback that frees memory.
         */
        public class FMUFreeMemory implements FMICallbackFreeMemory {
            /** Free memory.
             *  @param object The object to be freed.
             */
            public void apply(Pointer object) {
                _pointers.remove(object);
            }
        }

        /** A callback for when the step is finished. */
        public class FMUStepFinished implements FMIStepFinished {
            /** The step is finished.
             *  @param fmiComponent The FMI component that was instantiate.
             *  @param status The status flag.  See the FMI documentation.
             */
            public void apply(Pointer fmiComponent, int status) {
                System.out.println("Java fmiStepFinished: " + fmiComponent
                        + " " + status);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////
    ////                  protected fields                         ////

    /** The comma separated value separator.
     *  The initial value is ','.
     */
    protected static char _csvSeparator = ',';

    /** True if logging is enabled.
     *  The initial value is false.
     */
    protected static boolean _enableLogging = false;

    /** The end time, in seconds. 
     *  The initial default is 1.0.   
     */
    protected static double _endTime = 1.0;

    /** The name of the .fmu file.
     *  The initial default is the empty string.   
     */
    protected static String _fmuFileName = "";

    /** The modelIdentifier from modelDescription.xml. */
    protected String _modelIdentifier;

    /** The NativeLibrary that contains the functions. */
    protected NativeLibrary _nativeLibrary;

    /** The output file name.
     *  The initial value is "results.csv".
     */
    protected static String _outputFileName = "results.csv";

    /** The step size, in seconds.
     *  The initial default is 0.1 seconds.
     */
    protected static double _stepSize = 0.1;

    ///////////////////////////////////////////////////////////////////
    ////             private fields                                ////

    /** Keep references to memory that has been allocated and
     *  avoid problems with the memory being garbage collected.   
     */
    private static Set<Pointer> _pointers = new HashSet<Pointer>();
}
