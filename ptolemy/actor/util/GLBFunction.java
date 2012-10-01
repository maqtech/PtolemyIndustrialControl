package ptolemy.actor.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// GLBFunction

/** This class implements a monotonic function that returns the greatest
 *  lower bound (GLB) of its arguments. These arguments are the port type
 *  variables of the destination ports of the TypedIOPort passed to the
 *  constructor. This function is used to define a type constraint asserting
 *  that the type of the output of this port is greater than or equal to the
 *  GLB of its destinations.
 *  <p>
 *  NOTE: It may seem counterintuitive that the constraint is "greater than
 *  or equal to" rather than "less than or equal to". But the latter
 *  constraint is already implied by the connections, since the output port
 *  type is required to be less than or equal to each destination port type.
 *  The combination of these constraints has the effect of setting the type
 *  of the output equal to the GLB of the types of the destination ports.
 *  This resolved type is, in fact, the most general type that satisfies the
 *  constraints of all the downstream ports.
 * @author Edward A. Lee, Marten Lohstroh
 * @version $Id: GLBFunction.java$
 * @since Ptolemy II 9.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class GLBFunction extends MonotonicFunction {

    /** Construct a GLBFunction that finds the greatest lower bound of the
     *  type variable of the destination ports connected to the TypedIOPort
     *  that is given as an argument. If the boolean argument inside is true
     *  then the port is required to be an input port and the arguments for
     *  this <code>GLBFunction</code> will be the inside destination ports.
     *  Otherwise, the port is required to be an output port and the arguments
     *  for this <code>GLBFunction</code> will be the outside destination ports.
     *
     *  @param sourcePort The port connected to the ports of which their type
     *  variables are used to calculate the greatest lower bound.
     *
     */
    public GLBFunction(TypedIOPort sourcePort) {
        _sourcePort = sourcePort;

        _updateArguments();
    }

    ///////////////////////////////////////////////////////////////
    ////                     public methods                    ////

    /** Return the current value of this monotonic function.
     *  @return A Type.
     *  @exception IllegalActionException If thrown while getting the
     *  value of the chached terms.
     */
    public Object getValue()  throws IllegalActionException {
        _updateArguments();

        Set<Type> types = new HashSet<Type>();
        types.addAll(_cachedTypes);
        for (int i = 0; i < _cachedTerms.length; i++) {
            Type type = (Type)_cachedTerms[i].getValue();
            types.add(type);
        }
        // If there are no destination outputs at all, then set
        // the output type to unknown.
        if (types.size() == 0) {
            return BaseType.UNKNOWN;
        }
        // If there is only one destination, the GLB is equal to the
        // type of that port.
        if (types.size() == 1) {
            return types.toArray()[0];
        }

        return TypeLattice.lattice().greatestLowerBound(types);
    }

    /** Return the type variables for this function, which are
     *  the type variables for all the destination ports.
     *  @return An array of InequalityTerms.
     */
    public InequalityTerm[] getVariables() {
        _updateArguments();
        return _cachedTerms;
    }

    /** Return the type constants for this function, which are
     *  the type constant types for all the destination ports.
     *  @return An array of Types.
     */
    public Type[] getConstants() {
        _updateArguments();
        return _cachedTypes.toArray(new Type[0]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Update the arguments used in <code>getValue()</code>, which are the
     *  InequalityTerms and Types of the destination ports. The arguments are
     *  only updated if the workspace version has changed.
     */
    protected void _updateArguments() {
        List<IOPort> destinations = null;
        if (_sourcePort.getContainer().workspace().getVersion()
                == _cachedVariablesWorkspaceVersion) {
            return;
        }
        ArrayList<InequalityTerm> portTypeTermList = new ArrayList<InequalityTerm>();
        _cachedTypes = new HashSet<Type>();
        // Make sure to support ports that both inputs and outputs.
        if (_sourcePort.isOutput()){
            destinations = _sourcePort.sinkPortList();
        }
        if (_sourcePort.isInput()) {
            if (destinations == null) {
                destinations = _sourcePort.insideSinkPortList();
            } else {
                destinations = new LinkedList<IOPort>(destinations);
                destinations.addAll(_sourcePort.insideSinkPortList());
            }
        }

        for (IOPort destination : destinations) {
            InequalityTerm destinationTypeTerm =
                    ((TypedIOPort)destination).getTypeTerm();
            if (destinationTypeTerm.isSettable()) {
                portTypeTermList.add(destinationTypeTerm);
            } else {
                _cachedTypes.add(((TypedIOPort)destination).getType());
            }
        }
        _cachedTerms = portTypeTermList.toArray(new InequalityTerm[0]);
        _cachedVariablesWorkspaceVersion = _sourcePort.getContainer()
                .workspace().getVersion();
    }

/*   @Override
    public String getVerboseString() {
        return _sourcePort.getContainer().getName() + "$" +_sourcePort.getName();
    }
*/ // FIXME

    ///////////////////////////////////////////////////////////////////
    ////                    protected variables                    ////

    /** The constant types found in destination ports. */
    protected Set<Type> _cachedTypes;

    /** The types terms found in destination ports. */
    protected InequalityTerm[] _cachedTerms;

    /** The workspace version number at time of last update of arguments. */
    protected long _cachedVariablesWorkspaceVersion = -1;

    /** The source port. */
    protected TypedIOPort _sourcePort;
}
