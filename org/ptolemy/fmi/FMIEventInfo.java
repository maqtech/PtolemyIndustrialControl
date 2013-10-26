/* Functional Mock-up Interface (FMI) event information.

   Copyright (c) 2012-2013 The Regents of the University of California.
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
 * Functional Mock-up Interface (FMI) event information.
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
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMIEventInfo extends Structure {

    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     */
    public FMIEventInfo() {
        super();
        // Don't call initFieldOrder with JNA later than jna-3.5.0
        //initFieldOrder();
    }

    /** Instantiate a Java structure that that represents the C
     * structure that contains information about events.
     * @param iterationConverged C type: fmiBoolean
     * @param stateValueReferencesChanged C type: fmiBoolean
     * @param stateValuesChanged C type: fmiBoolean
     * @param terminateSimulation C type: fmiBoolean
     * @param upcomingTimeEvent C type: fmiBoolean
     * @param nextEventTime C type: fmiReal
     */
    public FMIEventInfo(byte iterationConverged,
            byte stateValueReferencesChanged, byte stateValuesChanged,
            byte terminateSimulation, byte upcomingTimeEvent,
            double nextEventTime) {
        super();
        this.iterationConverged = iterationConverged;
        this.stateValueReferencesChanged = stateValueReferencesChanged;
        this.stateValuesChanged = stateValuesChanged;
        this.terminateSimulation = terminateSimulation;
        this.upcomingTimeEvent = upcomingTimeEvent;
        this.nextEventTime = nextEventTime;

        // Don't call initFieldOrder with JNA later than jna-3.5.0
        //initFieldOrder();
    }

    /** Access the structure by reference.
     */
    public static class ByReference extends FMIEventInfo implements
            Structure.ByReference {
    };

    /** Access the structure by value.
     */
    public static class ByValue extends FMIEventInfo implements
            Structure.ByValue {
    };

    // The fields below are in the order in which they are expected to be in the
    // C structure.

    /** C type: fmiBoolean. */
    public byte iterationConverged;

    /** C type: fmiBoolean. */
    public byte stateValueReferencesChanged;

    /** C type: fmiBoolean. */
    public byte stateValuesChanged;

    /** C type: fmiBoolean. */
    public byte terminateSimulation;

    /** C type: fmiBoolean. */
    public byte upcomingTimeEvent;

    /** C type: fmiReal. */
    public double nextEventTime;

    /** Return the field names in the proper order.
     *  <p>This is new in jna-3.5.0.
     *  @return a list of strings that name the fields in order.
     */
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "iterationConverged",
                "stateValueReferencesChanged", "stateValuesChanged",
                "terminateSimulation", "upcomingTimeEvent", "nextEventTime" });
    }

    /** Set the initialization order of the fields so that the order
     * matches the order of the C structure.
     * @deprecated As of jna-3.5.0, use getFieldOrder() instead.
     */
    protected void initFieldOrder() {
        // Note that the name of this method does not have a leading
        // underscore because the name of the protected method in the
        // parent class does not have an underscore.
        setFieldOrder(new String[] { "iterationConverged",
                "stateValueReferencesChanged", "stateValuesChanged",
                "terminateSimulation", "upcomingTimeEvent", "nextEventTime" });
    }
}
