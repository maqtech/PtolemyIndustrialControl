/* An actor that updates fields in a RecordToken.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RecordUpdater
/**
On each firing, read one token from each input port and assemble them
into a RecordToken that contains the union of the original input record
and each of the update ports.  To use this class, instantiate it, and
then add input ports (instances of TypedIOPort).  This actor is polymorphic.
The type constraint is that the output record contains all the labels in
the input record plus the names of added input ports. The type of a field
in the ouput is the same as the type of the added input port, if that field
is updated by an added input port. If a field in the output is not updated
by an input port, its type is the same as the corresponding field in the
input record. For example, if the input record has type
{item: string, value: int}, and this actor has two added input ports with
name/type: value/double and id/int, then the output record will have type
{item: string, vlaue: double, id: int}

@author Michael Shilman
@version $Id$
@see RecordAssembler
*/

public class RecordUpdater extends TypedAtomicActor {

    /** Construct a RecordUpdater with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RecordUpdater(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        input = new TypedIOPort(this, "input", true, false);

	output.setTypeAtLeast(new FunctionTerm(this));

        _addIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. Its type is constrained to be a RecordType. */
    public TypedIOPort output;

    /** The input port. Its type is constrained to be a RecordType. */
    public TypedIOPort input;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        RecordUpdater newObject = (RecordUpdater)super.clone(workspace);
	newObject.output.setTypeAtLeast(new FunctionTerm(newObject));
        return newObject;
    }

    /** Read one token from each input port, assemble them into a
     *  RecordToken that contains the union of the original input record
     *  and each of the update ports.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }

        // Pack a HashMap with all of the record entries from
        // the original record and all of the updating ports.
        HashMap outputMap = new HashMap();
        
        RecordToken record = (RecordToken)input.get(0);
        Set recordLabels = record.labelSet();
        for(Iterator i = recordLabels.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Token value = record.get(name);
            outputMap.put(name,value);
        }
        
        List inputPorts = inputPortList();
	Iterator iter = inputPorts.iterator();
	while(iter.hasNext()) {
	    TypedIOPort inputPort = (TypedIOPort)iter.next();
	    if (inputPort != input) {
                outputMap.put(inputPort.getName(), inputPort.get(0));
	    }
	}

 	// Construct a RecordToken and fill it with the values
        // in the HashMap.
	String[] labels = new String[outputMap.size()];
	Token[] values = new Token[outputMap.size()];

        int j = 0;
	for (Iterator i = outputMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
	    labels[j] = (String)entry.getKey();
	    values[j] = (Token)entry.getValue();
            j++;
	}

	RecordToken result = new RecordToken(labels, values);
        output.send(0, result);
    }

    /** Return true if all input ports have tokens, false if some input
     *  ports do not have a token.
     *  @return True if all input ports have tokens.
     *  @exception IllegalActionException If the hasToken() call to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean prefire() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();
	while (ports.hasNext()) {
	    IOPort port = (IOPort)ports.next();
	    if ( !port.hasToken(0)) {
	        return false;
	    }
        }
	return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addIcon() {
	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"0\" y=\"0\" width=\"10\" " +
                "height=\"60\" style=\"fill:red\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the input port
    // types. The value of the function is a record type that contains
    // all the labels in the input record, plus the names of the added
    // input ports. The type of a field in the function value is the
    // same as the type of an added input port, if the lable of that
    // field is the same as that input port, or, the type of a field
    // is the same as that of the corresponding field in the input
    // record.
    // To ensure that this function is monotonic, the value of the function
    // is bottom if the type of the port with name "input" is bottom. If
    // the type of this port is not bottom (it must be a record), the value
    // of the fundtion is computed as described above.
    private class FunctionTerm implements InequalityTerm {

	// The constructor takes a reference to the RecordUpdater actor
	// so that the clone() method can construct an instance of this
	// class.
	private FunctionTerm(RecordUpdater updater) {
	    _updater = updater;
	}

	///////////////////////////////////////////////////////////////
	////                       public inner methods            ////

	/** Return null.
	 *  @return null.
	 */
	public Object getAssociatedObject() {
	    return null;
	}

	/** Return the function result.
	 *  @return A Type.
	 */
	public Object getValue() {
            TypedIOPort recordInput = (TypedIOPort)_updater.getPort("input");
	    Type inputType = recordInput.getType();
	    if (inputType == BaseType.UNKNOWN) {
	        return BaseType.UNKNOWN;
	    }

	    if ( !(inputType instanceof RecordType)) {
	        throw new InvalidStateException(_updater, "ReocordUpdater: "
		        + "The type of the input port must be record,\n"
			+ "but the connection forces it to be "
			+ inputType);
	    }

            RecordType recordType = (RecordType)inputType;
            Map outputMap = new HashMap();
	    Set recordLabels = recordType.labelSet();
	    Iterator iter = recordLabels.iterator();
	    while (iter.hasNext()) {
	        String label = (String)iter.next();
		Type type = recordType.get(label);
		outputMap.put(label, type);
	    }

	    List inputPorts = _updater.inputPortList();
	    iter = inputPorts.iterator();
	    while (iter.hasNext()) {
	        TypedIOPort port = (TypedIOPort)iter.next();
		if (port != recordInput) {
		    outputMap.put(port.getName(), port.getType());
		}
	    }

 	    // Construct the RecordType
	    Object[] labelsObj = outputMap.keySet().toArray();
	    String[] labels = new String[labelsObj.length];
	    Type[] types = new Type[labelsObj.length];

            for (int i=0; i<labels.length; i++) {
	        labels[i] = (String)labelsObj[i];
		types[i] = (Type)outputMap.get(labels[i]);
	    }

	    return new RecordType(labels, types);
        }

        /** Return all the InequalityTerms for all input ports in an array.
	 *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
	    List inputPorts = inputPortList();
	    Object[] portsObj = inputPorts.toArray();
	    InequalityTerm[] variables = new InequalityTerm[portsObj.length];

	    for (int i=0; i<variables.length; i++) {
	        TypedIOPort port = (TypedIOPort)portsObj[i];
	        variables[i] = port.getTypeTerm();
	    }
	    return variables;
        }

        /** Throw an Exception. This function InequalityTerm cannot be
	 *  initialized.
         *  @exception IllegalActionException Always thrown in this class.
         */
        public void initialize(Object e)
		throws IllegalActionException {
	    throw new IllegalActionException("RecordUpdater$FunctionTerm." +
                    "initialize: Cannot initialize a function term.");
        }

        /** Return false.
         *  @return false.
         */
        public boolean isSettable() {
	    return false;
        }

        /** Return true.
         *  @return True.
         */
        public boolean isValueAcceptable() {
            return true;
        }

        /** Throw an Exception. The value of function InequalityTerm cannot
         *  be set.
         *  @exception IllegalActionException Always thrown in this class.
         */
        public void setValue(Object e) throws IllegalActionException {
	    throw new IllegalActionException(
                    "RecordUpdater$FunctionTerm.setValue: This function "
		    + "InequalityTerm is not settable.");
        }

        /** Override the base class to give a description of this term.
         *  @return A description of this term.
         */
        public String toString() {
            return "(RecordUpdater$FunctionTerm, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

	private RecordUpdater _updater;
    }
}

