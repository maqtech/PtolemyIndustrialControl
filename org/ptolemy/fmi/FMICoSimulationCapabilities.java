/* A Function Mock-up Interface Co-Simulation Capabilities object.

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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

///////////////////////////////////////////////////////////////////
//// FMICoSimulationCapbilities

/**
 * An object that represents the the capabilities of a FMI co-simulation
 * slave.
 *
 * <p>A Functional Mock-up Unit file is a .fmu file in zip format that
 * contains a .xml file named "modelDescription.xml".  The xml file
 * may optionally contain a "Implementation" element that will contain
 * either a "CoSimulation_Standalone" element or a "CoSimulation_Tool"
 * element.  Those two elements will contain a "Capabilities" element
 * that has attributes that define the capabilities of the slave.
 * This class has public fields that correspond to the attributes of
 * the "Capabilities" element.  The name of this class is taken from
 * the FMI specification.</p>
 *
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id: FMIModelDescription.java 63521 2012-05-09 23:25:18Z cxh $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMICoSimulationCapabilities {

    /** Create an empty Capability. */
    public FMICoSimulationCapabilities() {
    }

    /** Create a FMICoSimulationCapability from an XML Element.
     *  @param element The XML Element that contains attributes.
     */
    public FMICoSimulationCapabilities(Element element) {
        // We use reflection here so that if Capabilities attributes change,
        // and new fields are added, we don't have to update this method.
        Field fields[] = getClass().getFields();
        for (int i = 0; i < fields.length; i++) {

            // Get the public fields that are attributes with the same name.
            if ((fields[i].getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
                    && element.hasAttribute(fields[i].getName())) {
                try {

                    // The field is a primitive boolean, not a Boolean.
                    if (fields[i].getType().equals(Boolean.TYPE)) {
                        boolean value = Boolean.valueOf(element.getAttribute(fields[i].getName()));
                        fields[i].setBoolean(this, value);
                    } else if (fields[i].getType().equals(Integer.TYPE)) {
                        int value = Integer.valueOf(element.getAttribute(fields[i].getName()));
                        fields[i].setInt(this, value);
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to set the "
                            + fields[i].getName()
                            + " field to "
                            + element.getAttribute(fields[i].getName()) + ".",
                            ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////             public methods                                ////

    /** Return a description of the fields that are true or
     *  non-zero.
     *  @return The true or non-zero fields
     */
    public String toString() {
        // We use reflection here so that if Capabilities attributes change,
        // and new fields are added, we don't have to update this method.
        StringBuffer results = new StringBuffer();
        Field fields[] = getClass().getFields();
        for (int i = 0; i < fields.length; i++) {

            // Get the public fields.
            if ((fields[i].getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
                String valueString = "";
                try {
                    // The field is a primitive boolean, not a Boolean
                    if (fields[i].getType().equals(Boolean.TYPE)) {
                        if (fields[i].getBoolean(this)) {
                            valueString = "true";
                        }
                    } else if (fields[i].getType().equals(Integer.TYPE)) {
                        int value = fields[i].getInt(this);
                        if (value != 0) {
                            valueString = new Integer(value).toString();
                        }
                    }
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get the "
                            + fields[i] + " field", ex);
                }

                // Optionally append a comma.
                if (valueString != "") {
                    if (results.length() > 0) {
                        results.append(", ");
                    }
                    results.append(fields[i].getName() + " = " + valueString);
                }
            }
        }
        // We attempt to return a record in the Ptolemy format.
        return "{" + results.toString() + "}";
    }

    ///////////////////////////////////////////////////////////////////
    ////             public fields                                 ////

    /** True if only one FMU can be instantiated per process.
     *  The default value is false.
     */
    public boolean canBeInstantiatedOnlyOncePerProcess;

    /** True if the slave ignores the allocateMemory()
    /** True if the slave can handle a variable step size.
     *  The default value is false.
     */
    public boolean canHandleVariableCommunicationStepSize;

    /** True if the step size can be zero.
     *  The default value is false.
     */
    public boolean canHandleEvents;

    /** True if slave can interpolate inputs.
     *  The default value is false.
     */
    public boolean canInterpolateInputs;

    /** True if the slave ignores the allocateMemory()
     *  and freeMemory() callback functions and the
     *  slave uses its own memory management.
     *  The default value is false.
     */
    public boolean canNotUseMemoryManagementFunctions;

    /** True if the slave can run the fmiDoStep() call
     *  asynchronously.  The default value is false.
     */
    public boolean canRunAsynchronuously;

    /** True if the slave can discard and repeat a step.
     *  The default value is false.
     */
    public boolean canRejectSteps;

    /** True if the slave can signal events during a communication
     *  step.  If false, then the slave cannot signal events
     *  during the communication step.  The default value is
     *  false.
     */
    public boolean canSignalEvents;

    /** The slave can supply derivatives with a maximum order.n
     *  The default value is 0.
     */
    public int maxOutputDerivativeOrder;
}

