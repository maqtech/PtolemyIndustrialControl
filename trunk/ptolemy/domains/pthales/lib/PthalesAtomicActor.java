package ptolemy.domains.pthales.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.kernel.util.Workspace;

public class PthalesAtomicActor extends TypedAtomicActor {

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesAtomicActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _initialize();
    }

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesAtomicActor() throws IllegalActionException,
            NameDuplicationException {
        super();
        _initialize();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PthalesAtomicActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _initialize();
    }

    /** Convert from an array used in a JNI function to Tokens.
     *  @param realOut The array used in a JNI function.
     *  @return An arry of Tokens.
     */
    public FloatToken[] convertReal(float[] realOut) {
        FloatToken[] tokensOut;

        int nbData = realOut.length;
        tokensOut = new FloatToken[nbData];
        for (int i = 0; i < nbData; i++) {
            tokensOut[i] = new FloatToken(realOut[i]);
        }

        return tokensOut;
    }

    /** Convert from Tokens to array to be used in a JNI function,
     *  @param tokensIn The tokens to be converted.
     *  @return The array to be used in a JNI function.
     */
    public float[] convertToken(Token[] tokensIn) {
        float[] realIn;

        int nbData = tokensIn.length;
        realIn = new float[nbData];
        for (int i = 0; i < nbData; i++) {
            realIn[i] = ((FloatToken) tokensIn[i]).floatValue();
        }

        return realIn;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            TypedIOPort port = new TypedIOPort(this, name, false, false);
            PthalesIOPort.initialize(port);

            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////              static methods implementation              ////

    /** Return a data structure giving the dimension data contained by a
     *  parameter with the specified name in the specified port or actor.
     *  The dimension data is indexed by dimension name and contains two
     *  integers, a value and a stride, in that order.
     *  @param name The name of the parameter
     *  @return The dimension data, or an empty array if the parameter does not exist.
     *  @throws IllegalActionException If the parameter cannot be evaluated.
     */
    protected static Integer[] _parseRepetitions(ComponentEntity actor,
            String name) {
        Integer[] result = new Integer[0];
        Attribute attribute = actor.getAttribute(name);
        if (attribute != null && attribute instanceof Parameter) {
            Token token = null;
            try {
                token = ((Parameter) attribute).getToken();
            } catch (IllegalActionException e) {
                // FIXME: Don't print a stack trace, instead
                // this method should throw an IllegalActionException.
                e.printStackTrace();
            }
            if (token instanceof ArrayToken) {
                int len = ((ArrayToken) token).length();
                result = new Integer[len];
                for (int i = 0; i < len; i++) {
                    result[i] = new Integer(((IntToken) ((ArrayToken) token)
                            .getElement(i)).intValue());
                }
            }
        }

        return result;
    }

    public static Integer[] getRepetitions(ComponentEntity actor) {
        return _parseRepetitions(actor, REPETITIONS);
    }

    ///////////////////////////////////////////////////////////////////
    ////              static methods implementation              ////
    public static Integer[] getInternalRepetitions(ComponentEntity actor) {
        return _parseRepetitions(actor, INTERNAL_REPETITIONS);
    }

    public static int getIteration(ComponentEntity actor) {
        return computeIteration(_parseRepetitions(actor, REPETITIONS),
                _parseRepetitions(actor, INTERNAL_REPETITIONS));
    }

    public static int[] getIterations(ComponentEntity actor) {
        return computeIterations(_parseRepetitions(actor, REPETITIONS),
                _parseRepetitions(actor, INTERNAL_REPETITIONS));
    }

    /** Compute external iterations each time 
     *  an attribute used to calculate it has changed 
     */
    protected static int computeIteration(Integer[] totalRepetitions,
            Integer[] internalRepetitions) {
        // FIXME: prepend an underscore to the name of this protected method.        
        // if no total repetition, no way to calculate
        int iterations = 0;
        if (totalRepetitions != null && totalRepetitions.length > 0) {

            // Internal Loops only used INSIDE the function
            int internal = 1;
            if (internalRepetitions != null) {
                for (Integer iter : internalRepetitions) {
                    internal *= iter;
                }
            }

            // All loops are used to build array
            int iterationCount = 1;
            for (Integer iter : totalRepetitions) {
                iterationCount *= iter;
            }

            // Iteration is only done on external loops
            iterations = iterationCount / internal;
        }
        return iterations;
    }

    /** Compute external iterations each time 
     *  an attribute used to calculate it has changed 
     */
    protected static int[] computeIterations(Integer[] totalRepetitions,
            Integer[] internalRepetitions) {
        // FIXME: prepend an underscore to the name of this protected method.        
        // if no total repetition, no way to calculate
        int internal = 0;
        if (internalRepetitions != null) {
            internal = internalRepetitions.length;
        }

        int[] iterations = new int[totalRepetitions.length - internal];
        if (totalRepetitions != null && totalRepetitions.length > 0) {

            // All loops are used to build array
            for (int i = internal; i < totalRepetitions.length; i++) {
                iterations[i - internal] = totalRepetitions[i];
            }
        }
        return iterations;
    }

    ///////////////////////////////////////////////////////////////////
    ////              static variables              ////

    /** The name of the internal repetitions parameter. */
    protected static String INTERNAL_REPETITIONS = "internalRepetitions";

    protected static String REPETITIONS = "repetitions";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {

        if (getAttribute("_iconDescription") != null) {
            ((SingletonConfigurableAttribute) getAttribute("_iconDescription"))
                    .setExpression("<svg width=\"60\" height=\"40\"><polygon points=\"2.54167,37.2083 13.9198,20.0125 2.54167,2.45833 46.675,2.45833 57.7083,20.0125 47.0198,37.2083\"style=\"fill:#c0c0ff;stroke:#000080;stroke-width:1\"/></svg>");
        }
        if (getAttribute("repetitions") == null) {
            Parameter repetitions = new Parameter(this, "repetitions");
            repetitions.setExpression("{1}");
        }
    }

}
