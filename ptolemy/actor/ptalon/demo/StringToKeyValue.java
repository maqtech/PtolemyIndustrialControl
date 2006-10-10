/* An actor that converts an appropriate string token to key and value tokens.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.ptalon.demo;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// AnythingToDouble

/**
 This actor converts an string token whose string represents a length-two
 array to the key and value tokens in the array.  The key token is the first
 element of the array and the value token is the second token in the array.
 
 <p>
 @author Adam Cataldo
 */
public class StringToKeyValue extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringToKeyValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setTypeEquals(BaseType.STRING);

        keyType = new Parameter(this, "keyType");
        keyType.setToken("\"String Type\"");

        key = new TypedIOPort(this, "key");
        key.setOutput(true);
        key.setTypeAtLeast(keyType);

        valueType = new Parameter(this, "valueType");
        valueType.setToken("1");

        value = new TypedIOPort(this, "value");
        value.setOutput(true);
        value.setTypeAtLeast(valueType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     * 
     */
    public TypedIOPort input;

    /** The key output port.
     * 
     */
    public TypedIOPort key;

    /** A parameter whose type gives the type of the key port.
     * 
     */
    public Parameter keyType;

    /** The value output port.
     * 
     */
    public TypedIOPort value;

    /** A parameter whose type gives the type of the key port.
     * 
     */
    public Parameter valueType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StringToKeyValue newObject = (StringToKeyValue) super.clone(workspace);
        return newObject;
    }

    /** Read exactly one token from the input and output an array
     *  version of the token.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        String inString = ((StringToken) input.get(0)).stringValue();
        StringTokenizer tokenizer = new StringTokenizer(inString, "{,}");

        try {
            if (inString.equals("EOF")) {
                return;
            }
            Token keyToken = (new ArrayToken("{" + tokenizer.nextToken() + "}"))
                    .getElement(0);
            Token valueToken = (new ArrayToken("{" + tokenizer.nextToken()
                    + "}")).getElement(0);

            key.send(0, keyToken);
            value.send(0, valueToken);
        } catch (NoSuchElementException e) {
            throw new IllegalActionException("Bad expression passed as input");
        }
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }

}
