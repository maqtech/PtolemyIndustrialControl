/* An actor that wraps an instance of a Java class.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
//// ClassWrapper
/**
This actor wraps an instance of a Java class specified by the <i>className</i>
parameter. If an input port is added to the actor, the name of the port is
interpreted as a method name of the Java class. When this actor is fired and
a token is received from this input port, the content of the token is treated
as the argument(s) for calling the method. If the method has a return value
and the actor has an output port named <i>methodName</i>Result, the value is
wrapped in a token that is sent to the output port.
<p>
For example, suppose the specified class has a method named foo and the actor
has an input port named foo. If the method foo takes no argument, the token
received from port foo is treated as a trigger for invoking the method, and
its content is ignored. When method foo takes arguments, the input token
should be a record token whose field values are used as values of the
arguments. The field lables of the record token should be "arg1", "arg2", etc.
For example, if method foo takes two double arguments, the record token
"{arg1 = 0.0, arg2 = 1.0}" can be the input.

A special case is when the method foo takes one argument, the token containing
the argument value can be input directly, and does not need to be put into
a record token.
<p>
FIXME: Need to set type constraints appropriately.
       Need (and how) to handle overloaded methods.

@author Xiaojun Liu
@version $Id$
*/

public class ClassWrapper extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>className</i> parameter.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ClassWrapper(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        className = new StringAttribute(this, "className");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the Java class.
     */
    public StringAttribute className;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class, but currently do nothing.
     *  FIXME: Is it reasonable to allow changing the class name during
     *  execution?
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
    }

    /** Read at most one token from each input port. If an input port has
     *  a token, the content of the token is used as argument(s) for invoking
     *  (on the wrapped instance) the method of the same name as the port. If
     *  the method has a return value, the value is wrapped in a token, and is
     *  sent to the output port named <i>methodName</i>Result.
     *  @exception IllegalActionException If the method invocation fails.
     */
    public void fire() throws IllegalActionException {
	Iterator inPorts = inputPortList().iterator();
	while (inPorts.hasNext()) {
	    IOPort inPort = (IOPort)inPorts.next();
	    if (inPort.hasToken(0)) {
		_invoke(inPort, inPort.get(0));
	    }
	}
    }

    /** Get the Class object for the specified class. Gather method invocation
     *  information corresponding to each input port. If there is no method
     *  of the same name as an input port, throw an exception.
     *  @exception IllegalActionException If the specified class cannot be
     *   loaded, or there is no method of the same name as an input port.
     */
    public void preinitialize() throws IllegalActionException {
	try {
	    _class = Class.forName(className.getExpression());
	} catch (ClassNotFoundException ex) {
	    throw new IllegalActionException(this, "Cannot find specified "
		    + "class " + className.getExpression() + "\n"
		    + ex.getMessage());
	}

	_methodTable = new Hashtable();
	Method[] methods = _class.getMethods();
	Iterator inPorts = inputPortList().iterator();
	boolean needInstance = false;
	while (inPorts.hasNext()) {
	    IOPort inPort = (IOPort)inPorts.next();
	    String portName = inPort.getName();
	    Method m = null;
	    for (int i = 0; i < methods.length; ++i) {
		if (methods[i].getName().equals(portName)) {
		    m = methods[i];
		    break;
		}
	    }
	    if (m == null) {
		throw new IllegalActionException(this, "The specified class "
			+ "does not have a method of the same name as input "
			+ "port " + portName);
	    }
	    Object[] methodInfo = new Object[3];
	    methodInfo[0] = m;
	    methodInfo[1] = m.getParameterTypes();
	    IOPort outPort = (IOPort)getPort(portName + "Result");
	    if (outPort != null && outPort.isOutput()) {
		methodInfo[2] = outPort;
	    } else {
		methodInfo[2] = null;
	    }
	    _methodTable.put(inPort, methodInfo);
	    if (!Modifier.isStatic(m.getModifiers())) {
		needInstance = true;
	    }
	}
	_instance = null;
	if (needInstance) {
	    try {
		// FIXME: here only try to use a constuctor with no argument
		Constructor constructor = _class.getConstructor(new Class[0]);
		_instance = constructor.newInstance(new Object[0]);
	    } catch (Exception ex) {
		throw new IllegalActionException(this, "Cannot create an "
			+ "instance of the specified class.\n"
			+ ex.getMessage());
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Invoke on the wrapped instance the method with the same name as the
    // specified port, treating argv as the arguments.
    // NOTE: Should this method return a boolean, false when the invocation
    // fails? The actor may have an error output port, and send a token
    // to this port when invocation fails.
    private void _invoke(IOPort port, Token argv)
            throws IllegalActionException {
	// assert port.isInput()
        Object[] methodInfo = (Object[])_methodTable.get(port);
	// when _methodTable is built, an entry for each input port is
	// guaranteed
	Method m = (Method)methodInfo[0];
	Class[] argTypes = (Class[])methodInfo[1];
	int args = argTypes.length;
	IOPort outPort = (IOPort)methodInfo[2];

	// The following code is mostly copied from data.expr.ASTPtFunctionNode

	Object[] argValues = new Object[args];
	if (args > 0) {
	    RecordToken argRecord = null;
	    if (argv instanceof RecordToken) {
		argRecord = (RecordToken)argv;
	    } else if (args > 1) {
		throw new IllegalActionException(this, "cannot convert "
		        + "input token to method call arguments.");
	    }
	    for (int i = 0; i < args; ++i) {
		Token arg = null;
		if (argRecord != null) {
		    arg = argRecord.get("arg" + (i + 1));
		} else {
		    // this is the case when the method takes one argument
		    // and the input token is not a record token
		    arg = argv;
		}
		if (argTypes[i].isAssignableFrom(arg.getClass())) {
		    argValues[i] = arg;
		} else if (arg instanceof DoubleToken) {
		    argValues[i] =
			    new Double(((DoubleToken)arg).doubleValue());
		} else if (arg instanceof IntToken) {
		    argValues[i] =
                            new Integer(((IntToken)arg).intValue());
		} else if (arg instanceof LongToken) {
		    argValues[i] =
		            new Long(((LongToken)arg).longValue());
		} else if (arg instanceof StringToken) {
		    argValues[i] = ((StringToken)arg).stringValue();
		} else if (arg instanceof BooleanToken) {
		    argValues[i] =
			    new Boolean(((BooleanToken)arg).booleanValue());
		} else if (arg instanceof ComplexToken) {
		    argValues[i] = ((ComplexToken)arg).complexValue();
		} else if (arg instanceof FixToken) {
		    argValues[i] = ((FixToken)arg).fixValue();
		} else {
		    argValues[i] = arg;
		}
	    }
	}
	Object result = null;
	try {
	    result = m.invoke(_instance, argValues);
	} catch (InvocationTargetException ex) {
	    // get the exception produced by the invoked function
	    ex.getTargetException().printStackTrace();
	    throw new IllegalActionException(this,
                    "Error invoking method " + m.getName() + "\n" +
                    ex.getTargetException().getMessage());
	} catch (Exception ex)  {
	    new IllegalActionException(ex.getMessage());
	}

	Token resultToken = null;
	if (result == null) {
	    // the mothod does not return value
	    return;
	} else if (result instanceof Token) {
	    resultToken = (Token)result;
	} else if (result instanceof Double) {
	    resultToken = new DoubleToken(((Double)result).doubleValue());
	} else if (result instanceof Integer) {
	    resultToken = new IntToken(((Integer)result).intValue());
	} else if (result instanceof Long) {
	    resultToken = new LongToken(((Long)result).longValue());
	} else if (result instanceof String) {
	    resultToken = new StringToken((String)result);
	} else if (result instanceof Boolean) {
	    resultToken = new BooleanToken(((Boolean)result).booleanValue());
	} else if (result instanceof Complex) {
	    resultToken = new ComplexToken((Complex)result);
	} else if (result instanceof FixPoint) {
	    resultToken = new FixToken((FixPoint)result);
	} else {
	    throw new IllegalActionException(this, "Result of method call "
	            + port.getName() + " is not a supported type: boolean, "
		    + "complex, fixpoint, double, int, long  and String, "
		    + "or a Token.");
	}
	
	if (outPort != null) {
	    outPort.send(0, resultToken);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Hashtable _methodTable = null;
    private Object _instance = null;
    private Class _class = null;

}
