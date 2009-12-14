/* A receiver for multidimensional dataflow.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

package ptolemy.domains.pthales.kernel;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.domains.pthales.lib.PthalesGenericActor;
import ptolemy.domains.pthales.lib.PthalesIOPort;
import ptolemy.domains.pthales.lib.PthalesCompositeActor;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

/**
*  
* @author eal
*
*/

public class PthalesReceiver extends SDFReceiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check whether the array is correct or not. FIXME: What does it
     *  mean to be correct?
     *  @param baseSpec The origin.
     *  @param patternSpec Fitting of the array.
     *  @param tilingSpec Paving of the array.
     *  @param dimensions Dimensions contained in the array.
     *  @throws IllegalActionException FIXME: If what?
     */
    public void checkArray(LinkedHashMap<String, Integer[]> baseSpec,
            LinkedHashMap<String, Integer[]> patternSpec,
            LinkedHashMap<String, Integer[]> tilingSpec, List<String> dimensions)
            throws IllegalActionException {

        /* FIXME: Checks for validity of array needed here.
         */
    }

    /** Do nothing.
     *  @exception IllegalActionException If clear() is not supported by
     *   the domain.
     */
    public void clear() {
        // Ignore
    }

    /** Return a list with tokens that are currently in the receiver
     *  available for get() or getArray(). The oldest token (the one
     *  that was put first) should be listed first in any implementation
     *  of this method.
     *  @return A list of instances of Token.
     *  @exception IllegalActionException If the operation is not supported.
     */
    public List<Token> elementList() {
        // FIXME: implement this.
        return new LinkedList();
    }

    /** Get a token from this receiver.
     *  @return A token read from the receiver.
     *  @exception NoTokenException If there is no token.
     */
    public Token get() throws NoTokenException {
        if (_buffer != null) {
            Token result = _buffer[_addressesIn[_posIn++]];
            return result;
        } else {
            throw new NoTokenException("Empty buffer in PthalesReceiver !");
        }
    }

    /** Get an array of tokens from this receiver. The <i>numberOfTokens</i>
     *  argument specifies the number of tokens to get. In an implementation,
     *  the length of the returned array must be equal to
     *  <i>numberOfTokens</i>.
     *  @param numberOfTokens The number of tokens to get in the
     *   returned array.
     *  @return An array of tokens read from the receiver.
     *  @exception NoTokenException If there are not <i>numberOfTokens</i>
     *   tokens.
     */
    public Token[] getArray(int numberOfTokens) throws NoTokenException {
        Token[] result = new Token[numberOfTokens];
        for (int i = 0; i < numberOfTokens; i++)
            result[i] = get();

        return result;
    }

    /** Return true if the buffer can contain one more token
     *  @return true or false.
     */
    public boolean hasRoom() {
        return (_posOut + 1 <= _buffer.length);
    }

    /** Return true if the buffer can contain n more token
     *  @return true or false.
     */
    public boolean hasRoom(int numberOfTokens) {
        return (_posOut + numberOfTokens <= _buffer.length);
    }

    /** Return if the buffer contains 1 more token to be read
     *  @return True.
     */
    public boolean hasToken() {
        return (_posOut >= _posIn + 1);
    }

    /** Return if the buffer contains n more token to be read
     *  @return True.
     */
    public boolean hasToken(int numberOfTokens) {
        return (_posOut >= _posIn + numberOfTokens);
    }

    /** Return true.
     *  @return True.
     */
    public boolean isKnown() {
        return true;
    }

    /** Put the specified token into this receiver.
     *  If the specified token is null, this method
     *  inserts a null into the array.
     *  @param token The token to put into the receiver.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void put(Token token) {
        if (_buffer != null) {
            _buffer[_addressesOut[_posOut++]] = token;
        }
    }

    /** Put a portion of the specified token array into this receiver.
     *  The first <i>numberOfTokens</i> elements of the token array are put
     *  into this receiver.  The ability to specify a longer array than
     *  needed allows certain domains to have more efficient implementations.
     *  @param tokenArray The array containing tokens to put into this
     *   receiver.
     *  @param numberOfTokens The number of elements of the token
     *   array to put into this receiver.
     *  @exception NoRoomException If the token array cannot be put.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putArray(Token[] tokenArray, int numberOfTokens)
            throws NoRoomException, IllegalActionException {
        for (int i = 0; i < numberOfTokens; i++)
            put(tokenArray[i]);
    }

    /** Put a sequence of tokens to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  @param tokens The sequence of token to put.
     *  @param numberOfTokens The number of tokens to put (the array might
     *   be longer).
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putArrayToAll(Token[] tokens, int numberOfTokens,
            Receiver[] receivers) throws NoRoomException,
            IllegalActionException {
        for (Receiver receiver : receivers) {
            for (int i = 0; i < numberOfTokens; i++)
                receiver.put(tokens[i]);
        }
    }

    /** Put a single token to all receivers in the specified array.
     *  Implementers will assume that all such receivers
     *  are of the same class.
     *  If the specified token is null, this method inserts a
     *  null into the arrays.
     *  @param token The token to put, or null to put no token.
     *  @param receivers The receivers.
     *  @exception NoRoomException If there is no room for the token.
     *  @exception IllegalActionException If the token is not acceptable
     *   to one of the ports (e.g., wrong type).
     */
    public void putToAll(Token token, Receiver[] receivers)
            throws NoRoomException, IllegalActionException {
        for (Receiver receiver : receivers) {
            receiver.put(token);
        }
    }

    /** Reset this receiver to its initial state, which is typically
     *  either empty (same as calling clear()) or unknown.
     *  @exception IllegalActionException If reset() is not supported by
     *   the domain.
     */
    public void reset() throws IllegalActionException {
        _posIn = 0;
        _posOut = 0;
    }

    /** Specifies the input array that will read the buffer allocated as output.
     * Here we only check that everything is correct, and computes addresses in output buffer.
     * @param port
     * @param actor
     * @throws IllegalActionException
     */
    public void setInputArray(IOPort port, Actor actor)
            throws IllegalActionException {

        if (_buffer != null)
        {
           fillParameters(actor,port);
           computeAddresses(port, actor);
        }
    }

    /** Specifies the output array that will be read by the receiver
     * It is the output array that determines the available size and dimensions
     * for the receivers.
     * This function allocates a buffer that is used as a memory would be (linear)
     * @param port
     * @param actor
     * @throws IllegalActionException
     */
    public void setOutputArray(IOPort port, Actor actor)
            throws IllegalActionException {

        // Output determines array size needed as input cannot read which has not been written

        // Total size of the array in "memory"
        int finalSize = PthalesIOPort.getArraySize(port);
        if (_buffer == null || _buffer.length < finalSize)
            _buffer = new Token[finalSize*PthalesIOPort.getNbTokenPerData(port)];

        // 
        _sizes = PthalesIOPort.getArraySizes(port);

        //
        String[] objs = PthalesIOPort.getDimensions(port);
        _dimensions = new String[objs.length];
        for (int i = 0; i < objs.length; i++)
            _dimensions[i] = (String) objs[i];

        fillParameters(actor,port);
        //
        computeAddresses(port, actor);
    }
    
    
    public void fillParameters(Actor actor, IOPort port) {
        if (actor instanceof AtomicActor)
            _repetitions = PthalesGenericActor.getRepetitions((AtomicActor)actor);
        if (actor instanceof CompositeActor)
            _repetitions = PthalesCompositeActor.getRepetitions((CompositeActor)actor);

        
        _patternSize = PthalesIOPort.getPatternSize(port);
        _patternSizes = PthalesIOPort.getPatternSizes(port);
        
        _pattern = PthalesIOPort.getPattern(port);
        _tiling = PthalesIOPort.getTiling(port);
        _base = PthalesIOPort.getBase(port);
        
        _addressNumber = PthalesIOPort.getAddressNumber(port);
        _nbTokens = PthalesIOPort.getNbTokenPerData(port);

    }

    ///////////////////////////////////////////////////////////////////
    ////               package friendly variables                  ////

    int _posIn = 0;
    int _posOut = 0;
    
    ///////////////////////////////////////////////////////////////////
    // Variables needed to compute
    int _patternSize;
    Integer[] _patternSizes;
    Integer[] _repetitions;
    
    LinkedHashMap<String, Integer[]> _pattern;
    LinkedHashMap<String, Integer[]> _tiling;
    LinkedHashMap<String, Integer[]> _base;
    
    int _addressNumber;
    int _nbTokens;
    

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    /** Buffer memory. */
    protected Token[] _buffer = null;

    /** addresses for input or output */
    protected int[] _addressesOut = null;

    protected int[] _addressesIn = null;

    // This variable is set by output ports only
    /** array size by dimension */
    protected LinkedHashMap<String, Integer> _sizes = null;

    // This variable is set by output ports only
    /** Dimensions */
    String[] _dimensions = null;

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    void computeAddresses(IOPort port, Actor actor) {
   
        if (port.isInput() && _pattern.isEmpty() ||_base.isEmpty() || _tiling.isEmpty()) {
                _addressesIn = _addressesOut;
                return;
        }


        // Number of token per data
        int nbToken = _nbTokens;

        // FIXME: can reduce number of addresses if the empty tilings are taken in account
        // same number of addresses than buffer size (worst case)
        int jumpPattern[] = new int[_addressNumber];
 
        // Position in buffer 
        int pos = 0;
        
        // Pattern order
        String[] patternOrder = new String[_pattern.size()];
        _pattern.keySet().toArray(patternOrder);
        // tiling order 
        String[] tilingOrder = new String[_tiling.size()];
        _tiling.keySet().toArray(tilingOrder);

        // Pattern size, used for each repetition
        int sizePattern = _patternSize;
        
        // total repetition size
        int sizeRepetition = 1;
        for (Integer size : _repetitions)
        {
            sizeRepetition *= size;
        }
        
        // address indexes
        int dims[] = new int[_patternSizes.length + 1];
        // address indexes
        int reps[] = new int[_repetitions.length + 1];

        // addresses creation
        int jumpDim;
        int jumpRep;
        int previousSize;

        // Address jump for each dimension
        LinkedHashMap<String, Integer> jumpAddr = new LinkedHashMap<String, Integer>();
        for (int nDim = 0; nDim < _dimensions.length; nDim++) {
            previousSize = 1;
            for (int prev = 0; prev < nDim; prev++) {
                if (_sizes.get(_dimensions[prev]) != null)
                	previousSize *= _sizes.get(_dimensions[prev]);
            }
            jumpAddr.put((String) _dimensions[nDim], previousSize);
        }

        // origin construction (order is not important)
        Integer origin = 0;
        for (int nDim = 0; nDim < _dimensions.length; nDim++) {
             origin += _base.get(_dimensions[nDim])[0]
                    * jumpAddr.get(_dimensions[nDim]) * nbToken;
        }

        // Address construction  (order is important)
        for (int rep = 0; rep < sizeRepetition; rep++) {
            jumpRep = 0;
            for (int nRep = 0; nRep < tilingOrder.length; nRep++) {
                if (_tiling.get(tilingOrder[nRep]) != null && !tilingOrder[nRep].startsWith("empty")) {
                    jumpRep += _tiling.get(tilingOrder[nRep])[0]
                            * reps[nRep] * jumpAddr.get(tilingOrder[nRep])
                            * nbToken;
                }
            }

            // Pattern is written/read for each iteration
            for (int dim = 0; dim < sizePattern; dim++) {
                jumpDim = 0;
                for (int nDim = 0; nDim < _pattern.size(); nDim++) {
                    jumpDim += _pattern.get(patternOrder[nDim])[1]
                            * dims[nDim] * jumpAddr.get(patternOrder[nDim])
                            * nbToken;
                }

                for (int numToken = 0; numToken < nbToken; numToken++) {
                    jumpPattern[pos] = origin + jumpDim + jumpRep + numToken;
                    pos++;
                }

                // pattern indexes update
                dims[0]++;
                for (int nDim = 0; nDim < _pattern.size(); nDim++) {
                    if (dims[nDim] == _pattern.get(patternOrder[nDim])[0]) {
                        dims[nDim] = 0;
                        dims[nDim + 1]++;
                    }
                }
            }
            // repetition indexes update
            reps[0]++;
            for (int nRep = 0; nRep < _repetitions.length; nRep++) {
                if (reps[nRep] == _repetitions[nRep]) {
                    reps[nRep] = 0;
                    reps[nRep + 1]++;
                }
            }
        }

        if (port.isInput())
            _addressesIn = jumpPattern;
        else
            _addressesOut = jumpPattern;
    }

}
