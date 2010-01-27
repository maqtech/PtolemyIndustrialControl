/* A pool of functions used into Pthales Domain

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.domains.pthales.lib;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.domains.pthales.kernel.PthalesDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
////PthalesIOPort

/**
 A PthalesIOPort is an element of ArrayOL in Ptolemy.
 It contains functions needed to use multidimensional arrays.

 @author Remi Barrere
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PthalesIOPort {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the base of this port. 
     *  @return base 
     */
    public static LinkedHashMap<String, Integer[]> getBase(IOPort port) {
        return _parseSpec(port, BASE);
    }

    /** Compute total array size.
     *  @return array size
     */
    public static int getArraySize(IOPort port) {
        int val = 1;
        for (Integer size : getArraySizes(port).values()) {
            val *= size;
        }

        return val;
    }

    /** Computes array sizes (for each dimension)
     *  @return array sizes
     */
    public static LinkedHashMap<String, Integer> getArraySizes(IOPort port) {
        LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, Token> sizesToMap = new LinkedHashMap<String, Token>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        rep = PthalesAtomicActor.getRepetitions((ComponentEntity) actor);

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim)) {
                sizes.put((String) dim, pattern.get(dim)[0]);
                sizesToMap.put((String) dim, new IntToken(pattern.get(dim)[0]));
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            sizes.put((String) dim, pattern.get(dim)[0]
                                    + rep[i] * tiling.get(til)[0] - 1);
                            sizesToMap.put((String) dim, new IntToken(pattern
                                    .get(dim)[0]
                                    + rep[i] * tiling.get(til)[0] - 1));
                        } else {
                            // Not enough reps for tilings, rep = 1
                            sizes.put((String) dim, pattern.get(dim)[0]
                                    + tiling.get(til)[0] - 1);
                            sizesToMap.put((String) dim, new IntToken(pattern
                                    .get(dim)[0]
                                    + tiling.get(til)[0] - 1));
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !((String) til).startsWith("empty")
                        && !dims.contains(til)) {
                    sizes.put((String) til, rep[i] * tiling.get(til)[0]);
                    sizesToMap.put((String) til, new IntToken(rep[i]
                            * tiling.get(til)[0]));
                }
                i++;
            }
        }

        // Size written if not already set
        try {
            OrderedRecordToken array = new OrderedRecordToken(sizesToMap);
            // Write into parameter
            Parameter p = (Parameter) port.getAttribute("size");
            if (p == null) {
                try {
                    // if parameter does not exist, creation
                    p = new Parameter(port, "size");
                } catch (NameDuplicationException e) {
                    e.printStackTrace();
                }
            }

            p.setVisibility(Settable.FULL);
            p.setPersistent(true);
            if (p.getExpression().equals("")) {
                p.setExpression(array.toString());
            }

        } catch (IllegalActionException e) {
            e.printStackTrace();
        }

        return sizes;
    }

    /** Compute number of address needed for each iteration 
     *  @return number of address
     */
    public static int getPatternNbAddress(IOPort port) {
        int val = 1;
        for (int size : getPatternNbAddresses(port)) {
            val *= size;
        }

        return val;
    }

    /** Compute  number of address by dimension needed for each iteration
     *  @return address array
     */
    public static Integer[] getPatternNbAddresses(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim) || rep.length == 0) {
                myList.add(pattern.get(dim)[0]);
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            myList.add(pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1);
                        } else {
                            myList.add(pattern.get(dim)[0] + tiling.get(til)[0]
                                    - 1);
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !dims.contains(til)
                        && !((String) til).startsWith("empty")) {
                    myList.add(rep[i]);
                }
                i++;
            }
        }
        Integer[] result = new Integer[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Computes data size produced for each iteration 
     *  @return data size
     */
    public static int getDataProducedSize(IOPort port) {
        int val = 1;
        for (int size : getDataProducedSizes(port)) {
            val *= size;
        }

        return val;
    }

    /** Computes data sizes (for each dimension) produced for each iteration
     *  @return data sizes
     */
    public static Integer[] getDataProducedSizes(IOPort port) {
        List myList = new ArrayList<String>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = { 1 };

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();
        int i = 0;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim)) {
                myList.add(pattern.get(dim)[0]);
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        if (i < rep.length) {
                            myList.add(pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1);
                        } else {
                            // Data produced does depend of repetition, unlike addresses
                            myList.add(pattern.get(dim)[0]);
                        }

                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !((String) til).startsWith("empty")
                        && !dims.contains(til)) {
                    myList.add(rep[i] * tiling.get(til)[0]);
                }
                i++;
            }
        }
        Integer[] result = new Integer[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Compute pattern for external iteration
     *  @return data sizes
     */
    public static LinkedHashMap<String, Integer[]> getInternalPattern(
            IOPort port) {
        LinkedHashMap<String, Integer[]> internalPattern = new LinkedHashMap<String, Integer[]>();

        Actor actor = (Actor) port.getContainer();
        Integer[] rep = new Integer[0];

        LinkedHashMap<String, Integer[]> pattern = _getPattern(port);
        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        if (actor instanceof AtomicActor) {
            rep = PthalesAtomicActor
                    .getInternalRepetitions((AtomicActor) actor);
        }

        Set dims = pattern.keySet();
        Set tilingSet = tiling.keySet();

        int i = 0;
        Integer[] res;

        for (Object dim : dims.toArray()) {
            if (!tilingSet.contains(dim) || rep.length == 0) {
                internalPattern.put((String) dim, pattern.get(dim));
            } else {
                for (Object til : tilingSet) {
                    if (til.equals(dim)) {
                        res = new Integer[2];
                        res[1] = tiling.get(til)[1];

                        if (i < rep.length) {
                            res[0] = pattern.get(dim)[0] + rep[i]
                                    * tiling.get(til)[0] - 1;
                            internalPattern.put((String) dim, res);
                        } else {
                            res[0] = pattern.get(dim)[0] + tiling.get(til)[0]
                                    - 1;
                            internalPattern.put((String) dim, res);
                        }
                    }
                }
            }
            i++;
        }

        if (rep != null) {
            i = 0;
            for (Object til : tilingSet) {
                if (i < rep.length && !dims.contains(til)
                        && !((String) til).startsWith("empty")) {
                    res = new Integer[2];
                    res[0] = rep[i];
                    res[1] = tiling.get(til)[1];
                    internalPattern.put((String) til, res);
                }
                i++;
            }
        }

        return internalPattern;
    }

    /** Returns tiling of external loops iterations
     * @param port
     * @param nb
     * @return tiling map 
     */
    public static LinkedHashMap<String, Integer[]> getExternalTiling(
            IOPort port, int nb) {
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<String, Integer[]>();

        LinkedHashMap<String, Integer[]> tiling = _getTiling(port);

        Object[] tilingSet = tiling.keySet().toArray();
        for (int i = 0; i < tilingSet.length; i++) {
            if (tilingSet.length - nb <= i) {
                result.put((String) tilingSet[i], tiling.get(tilingSet[i]));
            }
        }
        return result;
    }

    /** Check if data type is a structure.
     * If yes, gives the number of tokens needed to store all the data
     * By default, the return value is 1
     * @return the number of token needed to store the values
     */
    public static int getNbTokenPerData(IOPort port) {
        Parameter p = (Parameter) port.getAttribute("dataType");
        if (p != null) {
            if (p.getExpression().startsWith("Cpl")) {
                return 2;
            }
        }
        return 1;
    }

    /** Returns dimension names, in order of production 
     *  @return dimension names
     */
    public static String[] getDimensions(IOPort port) {
        List myList = new ArrayList<String>();

        Set dims1 = _getPattern(port).keySet();
        Set dims2 = _getTiling(port).keySet();

        for (Object dim : dims1.toArray()) {
            myList.add(dim);
        }
        for (Object dim : dims2.toArray()) {
            if (!myList.contains(dim) && !((String) dim).startsWith("empty")) {
                myList.add(dim);
            }
        }

        String[] result = new String[myList.size()];
        myList.toArray(result);

        return result;
    }

    /** Check if data type is a structure.
     * If yes, gives the number of tokens needed to store all the data
     * By default, the return value is 1
     */
    public static void setDataType(IOPort port) {
        Parameter p = (Parameter) port.getAttribute("dataType");
        if (p != null && port instanceof TypedIOPort) {
            if (p.getExpression().equals("Cplfloat")
                    || p.getExpression().equals("Splfloat")
                    || p.getExpression().equals("float")) {
                ((TypedIOPort) port).setTypeEquals(BaseType.FLOAT);
            }
            if (p.getExpression().equals("Cpldouble")
                    || p.getExpression().equals("Spldouble")
                    || p.getExpression().equals("double")) {
                ((TypedIOPort) port).setTypeEquals(BaseType.DOUBLE);
            }
            if (p.getExpression().equals("Cplint")
                    || p.getExpression().equals("Splint")
                    || p.getExpression().equals("int")) {
                ((TypedIOPort) port).setTypeEquals(BaseType.INT);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Reset the variable part of this type to the specified type.
     *  @exception IllegalActionException If the type is not settable,
     *   or the argument is not a Type.
     */
    public static void initialize(IOPort port) throws IllegalActionException,
            NameDuplicationException {

        if (port.getAttribute("base") == null) {
            new Parameter(port, "base");
        }

        if (port.getAttribute("pattern") == null) {
            new Parameter(port, "pattern");
        }

        if (port.getAttribute("tiling") == null) {
            new Parameter(port, "tiling");
        }

        if (port.getAttribute("dimensionNames") == null) {
            new StringParameter(port, "dimensionNames");
        }
        if (port.getAttribute("size") == null) {
            new Parameter(port, "size");
        }

        if (port.getAttribute("dataType") == null) {
            new StringParameter(port, "dataType");
        }

        if (port.getAttribute("dataTypeSize") == null) {
            new StringParameter(port, "dataTypeSize");
        }
    }

    /** Propagate the header through application relations
     * to update informations
     * @param portIn
     * @param dims
     * @param sizes
     * @param headersize
     * @param arraySizes
     */
    public static void propagateHeader(IOPort portIn, String[] dims,
            int[] sizes, int headersize,
            LinkedHashMap<String, Integer> arraySizes) {
        // Header
        if (portIn.getContainer() instanceof PthalesRemoveHeaderActor) {
            int sum = 1;
            for (int i = 0; i < sizes.length; i++) {
                sum *= sizes[i];
            }
            sum += headersize;

            // Input pattern
            PthalesIOPort._modifyPattern(portIn, "global", sum);

            // Output pattern
            PthalesIOPort._modifyPattern(
                    (IOPort) ((PthalesRemoveHeaderActor) portIn.getContainer())
                            .getPort("out"), dims, sizes);

            // Header found, update of all following Pthales actors
            propagateIterations((IOPort) ((PthalesRemoveHeaderActor) portIn
                    .getContainer()).getPort("out"), arraySizes);
        }

        if (portIn.isOutput()) {
            if (!(portIn instanceof RefinementPort)) {
                for (IOPort port : (List<IOPort>) portIn.connectedPortList()) {
                    propagateHeader(port, dims, sizes, headersize, arraySizes);
                }
            }
            if (portIn instanceof RefinementPort
                    && portIn.getContainer() instanceof Refinement) {
                Refinement ref = ((Refinement) portIn.getContainer());
                State state = ((ModalModel) ref.getContainer()).getController()
                        .currentState();
                if (state.getName().equals(ref.getName())) {
                    for (IOPort port : (List<IOPort>) portIn
                            .connectedPortList()) {
                        propagateHeader(port, dims, sizes, headersize,
                                arraySizes);
                    }

                }
            }
        }
        if (portIn.isInput()) {
            if (portIn.getContainer() instanceof CompositeActor) {
                for (Actor entity : (List<Actor>) ((CompositeActor) portIn
                        .getContainer()).entityList()) {
                    for (IOPort port : (List<IOPort>) entity.inputPortList()) {
                        IOPort port2 = port;
                        if (port2.connectedPortList().contains(portIn)) {
                            int sum = 1;
                            for (int i = 0; i < sizes.length; i++) {
                                sum *= sizes[i];
                            }
                            sum += headersize;

                            // If within Pthales domain, update of the port informations
                            if (((CompositeActor) portIn.getContainer())
                                    .getDirector() instanceof PthalesDirector) {
                                PthalesIOPort._modifyPattern(portIn, "global",
                                        sum);
                            }
                            propagateHeader(port2, dims, sizes, headersize,
                                    arraySizes);
                        }
                    }
                }
            }
        }
    }

    /** Update actor iterations according to pattern and tiling informations 
     * @param portIn
     * @param sizes
     */
    public static void propagateIterations(IOPort portIn,
            LinkedHashMap<String, Integer> sizes) {
        // Iterations
        if (portIn.getContainer() instanceof PthalesCompositeActor) {
            // Iteration computation
            ((PthalesCompositeActor) portIn.getContainer()).computeIterations(
                    portIn, sizes);

            // Once iterations are computed, output port can be computed
            for (IOPort portOut : (List<IOPort>) ((PthalesCompositeActor) portIn
                    .getContainer()).outputPortList()) {
                LinkedHashMap<String, Integer> outputs = PthalesIOPort
                        .getArraySizes(portOut);
                for (IOPort port : (List<IOPort>) portOut.connectedPortList()) {
                    propagateIterations(port, outputs);
                }
            }
        }

        if (portIn.isOutput()) {
            if (!(portIn instanceof RefinementPort)) {
                for (IOPort port : (List<IOPort>) portIn.connectedPortList()) {
                    propagateIterations(port, sizes);
                }
            }
            if (portIn instanceof RefinementPort
                    && portIn.getContainer() instanceof Refinement) {
                Refinement ref = ((Refinement) portIn.getContainer());
                State state = ((ModalModel) ref.getContainer()).getController()
                        .currentState();
                if (state.getName().equals(ref.getName())) {
                    for (IOPort port : (List<IOPort>) portIn
                            .connectedPortList()) {
                        propagateIterations(port, sizes);
                    }

                }
            }
        }
        if (portIn.isInput()) {
            if (portIn.getContainer() instanceof CompositeActor) {
                for (Actor entity : (List<Actor>) ((CompositeActor) portIn
                        .getContainer()).entityList()) {
                    for (IOPort port : (List<IOPort>) entity.inputPortList()) {
                        IOPort port2 = port;
                        if (port2.connectedPortList().contains(portIn)) {
                            propagateIterations(port2, sizes);
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the base parameter. */
    public static String BASE = "base";

    /** The name of the pattern parameter. */
    public static String PATTERN = "pattern";

    /** The name of the tiling parameter. */
    public static String TILING = "tiling";

    public static Integer ONE = new Integer(1);

    /** Initialize the iteration counter.  A derived class must call
     *  this method in its initialize() method or the <i>firingCountLimit</i>
     *  feature will not work.
     *  @exception IllegalActionException If the parent class throws it,
     *   which could occur if, for example, the director will not accept
     *   sequence actors.
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Notify the type listener about type change.
    /** returns the pattern of this port 
     *  @return pattern 
     */
    public static LinkedHashMap<String, Integer[]> _getPattern(IOPort port) {
        return _parseSpec(port, PATTERN);
    }

    public static void _modifyPattern(IOPort port, String dim, int dimSize) {
        Attribute pattern = port.getAttribute(PATTERN);
        if (port.getAttribute(PATTERN) == null) {
            try {
                pattern = new Parameter(port, PATTERN);
            } catch (IllegalActionException e) {
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                e.printStackTrace();
            }
        }

        if (pattern instanceof Parameter) {
            ((Parameter) pattern).setExpression("[" + dim + "={" + dimSize
                    + ",1}]");
        }
    }

    public static void _modifyPattern(IOPort port, String[] dims, int[] dimSizes) {
        Attribute pattern = port.getAttribute(PATTERN);
        if (port.getAttribute(PATTERN) == null) {
            try {
                pattern = new Parameter(port, PATTERN);
            } catch (IllegalActionException e) {
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                e.printStackTrace();
            }
        }

        String s = "[";
        if (pattern instanceof Parameter) {
            for (int i = 0; i < dims.length; i++) {
                s += dims[i] + "={" + dimSizes[i] + ",1}";
                if (i < dims.length - 1) {
                    s += ",";
                }
            }
        }
        s += "]";
        ((Parameter) pattern).setExpression(s);
    }

    /** returns the tiling of this port 
     *  @return tiling 
     */
    public static LinkedHashMap<String, Integer[]> _getTiling(IOPort port) {
        return _parseSpec(port, TILING);
    }

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or null if the parameter does not exist.
     *  @exception IllegalActionException If the parameter cannot be evaluated.
     */
    private static LinkedHashMap<String, Integer[]> _parseSpec(IOPort port,
            String name) {
        LinkedHashMap<String, Integer[]> result = new LinkedHashMap<String, Integer[]>();
        Attribute attribute = port.getAttribute(name);
        if (attribute instanceof Parameter) {
            Token token = null;
            try {
                token = ((Parameter) attribute).getToken();
            } catch (IllegalActionException e) {
                e.printStackTrace();
            }
            if (token != null) {
                if (token instanceof OrderedRecordToken) {
                    Set<String> fieldNames = ((OrderedRecordToken) token)
                            .labelSet();
                    for (String fieldName : fieldNames) {
                        Token value = ((OrderedRecordToken) token)
                                .get(fieldName);
                        Integer[] values = new Integer[2];
                        if (value instanceof IntToken) {
                            values[0] = ((IntToken) value).intValue();
                            values[1] = ONE;
                        } else if (value instanceof ArrayToken) {
                            if (((ArrayToken) value).length() != 2) {
                                // FIXME: Need a better error message here.
                            }
                            // FIXME: Check that tokens are IntToken
                            values[0] = ((IntToken) ((ArrayToken) value)
                                    .getElement(0)).intValue();
                            values[1] = ((IntToken) ((ArrayToken) value)
                                    .getElement(1)).intValue();
                        }
                        result.put(fieldName, values);
                    }
                }
            }
        }
        return result;
    }
}
