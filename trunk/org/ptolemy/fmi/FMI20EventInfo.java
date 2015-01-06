/* Functional Mock-up Interface (FMI) 2.0 event information.

   Copyright (c) 2012-2014 The Regents of the University of California.
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

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * Functional Mock-up Interface (FMI) 2.0 event information.
 *
 * <p>The C language interface to Functional Mock-up Unit (FMU)
 * files includes an structure that represents event information.
 * This class represents that structure.</p>
 *
 * <p>This file is based on a file that was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br> a tool
 * written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses
 * a few opensource projects.</a>.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMI20EventInfo extends Structure {

    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     */
    public FMI20EventInfo() {
        super();
        // Don't call initFieldOrder with JNA later than jna-3.5.0
        //initFieldOrder();
    }

    /** Construct a FMI20EventInfo from data.
     *  @param pointer a pointer to the data.
     *  @param offset the offset, in bytes.
     */
    public FMI20EventInfo(com.sun.jna.Pointer pointer, int offset) {
        super();
        useMemory(pointer, offset);
        read();
    }

    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     * <p>This is for FMI-1.0</p>
     * @param newDiscreteStatesNeeded C type: fmiBoolean
     * @param terminateSimulation C type: fmiBoolean
     * @param nominalsOfContinuousStatesChanged C type: fmiBoolean
     * @param valuesOfContinuousStatesChanged C type: fmiBoolean
     * @param nextEventTimeDefined C type: fmiBoolean
     * @param nextEventTime C type: fmiReal
     */
    public FMI20EventInfo(int newDiscreteStatesNeeded, int terminateSimulation,
            int nominalsOfContinuousStatesChanged,
            int valuesOfContinuousStatesChanged, int nextEventTimeDefined,
            double nextEventTime) {
        super();
        this.newDiscreteStatesNeeded = newDiscreteStatesNeeded;
        this.terminateSimulation = terminateSimulation;
        this.nominalsOfContinuousStatesChanged = nominalsOfContinuousStatesChanged;
        this.valuesOfContinuousStatesChanged = valuesOfContinuousStatesChanged;
        this.nextEventTimeDefined = nextEventTimeDefined;
        this.nextEventTime = nextEventTime;

        // Don't call initFieldOrder with JNA later than jna-3.5.0
        //initFieldOrder();
    }

    /** Access the structure by reference.
     */
    public static class ByReference extends FMI20EventInfo implements
            Structure.ByReference {
        /**  Allocate a new FMI20EventInfo.ByReference struct on the heap.
         */

        public ByReference() {
        }

        /** Create an instance that shares its memory with another
         *  FMU20EventInfo instance public ByReference(FMI20EventInfo.
         *  @param struct The FMI20EventInfo to be shared.
         */
        public ByReference(FMI20EventInfo struct) {
            super(struct.getPointer(), 0);
        }

    };

    /** Access the structure by value.
     */
    public static class ByValue extends FMI20EventInfo implements
            Structure.ByValue {

        /** Create an instance that shares its memory with another
         *  FMU20EventInfo instance public ByReference(FMI20EventInfo.
         *  @param struct The FMI20EventInfo to be shared.
         */
        public ByValue(FMI20EventInfo struct) {
            super(struct.getPointer(), 0);
        }

    };

    // The fields below are in the order in which they are expected to be in the
    // C structure.

    /** C type: fmiBoolean. */
    public int newDiscreteStatesNeeded;

    /** C type: fmiBoolean. */
    public int terminateSimulation;

    /** C type: fmiBoolean. */
    public int nominalsOfContinuousStatesChanged;

    /** C type: fmiBoolean. */
    public int valuesOfContinuousStatesChanged;

    /** C type: fmiBoolean. */
    public int nextEventTimeDefined;

    /** C type: fmiReal. */
    public double nextEventTime;

    /** Return the field names in the proper order.
     *  <p>This is new in jna-3.5.0.
     *  @return a list of strings that name the fields in order.
     */
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "newDiscreteStatesNeeded",
                "terminateSimulation", "nominalsOfContinuousStatesChanged",
                "valuesOfContinuousStatesChanged", "nextEventTimeDefined",
                "nextEventTime" });
    }
}
