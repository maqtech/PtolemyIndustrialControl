/* An actor that executes a Python script.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.script;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.NameDuplicationException;

import org.python.core.PyObject;
import org.python.core.PyJavaInstance;
import org.python.core.PyString;
import org.python.core.PyMethod;
import org.python.core.PyClass;
import org.python.util.PythonInterpreter;

import java.util.HashMap;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// PythonScript
/**
An actor of this class executes a Python script. Upon creation, the actor
has no port, and no parameter other than {@link #script script}. As an
example, a simplified version of the {@link ptolemy.actor.lib.Scale Scale}
actor can be implemented by the following script:
<pre>
1.  class Main :
2.    "scale"
3.    def fire(self) :
4.      if not self.input.hasToken(0) :
5.        return
6.      s = self.scale.getToken()
7.      t = self.input.get(0)
8.      self.output.broadcast(s.multiply(t))
</pre>
Line 1 defines a Python class Main. This name is fixed. An instance of this
class is created when the actor is initialized. Line 2 is a description of
the purpose of the script. Lines 3-8 define the fire() method, which is
called by the {@link #fire() fire()} method of this actor. In the method body,
<i>input</i> and <i>output</i> are ports added to the actor, and <i>scale</i>
is a parameter added to the actor. The Main class can provide other methods
in the {@link ptolemy.actor.Executable Executable} interface as needed.
<p>
This class relies on Jython, which is a Java implementation of Python.
Follow the links below for more information about the Python language,
licensing, downloads, etc.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 2.3
@see <a href="http://www.python.org">Python</a>
@see <a href="http://www.jython.org">Jython</a>
*/
public class PythonScript extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>script</i> parameter, and initialize
     *  the script to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @throws NameDuplicationException If the container already
     *   has an actor with this name.
     *  @throws IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public PythonScript(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        script = new StringAttribute(this, "script");
        // initialize the script to provide an empty template:
        //
        // # This is a template.
        // class Main :
        //   "description here"
        //   def fire(self) :
        //     # read input, compute, send output
        //     return
        //
        script.setExpression(
                "# This is a template.\n" +
                "class Main :\n" +
                "  \"description here\"\n" +
                "  def fire(self) :\n" +
                "    # read input, compute, send output\n" +
                "    return\n\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The script that specifies the function of this actor.
     *  The default value provides an empty template.
     */
    public StringAttribute script;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If <i>script</i> is changed, invoke the python interpreter to
     *  evaluate the script.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If there is any error in evaluating
     *   the script.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == script) {
            _evaluateScript();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Invoke the fire() method if defined in the script.
     *  @throws IllegalActionException If there is any error in calling the
     *   fire() method defined by the script.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _invokeMethod("fire", null);
    }

    /** Invoke the initialize() method if defined in the script.
     *  @throws IllegalActionException If there is any error in calling the
     *   initialize() method defined by the script.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _invokeMethod("initialize", null);
    }

    /** Invoke the postfire() method if defined in the script. Return true
     *  when the method return value is not zero, or the method does not
     *  return a value, or the method is not defined in the script.
     *  @return False if postfire() is defined in the script and returns 0,
     *   true otherwise.
     *  @throws IllegalActionException If there is any error in calling the
     *   postfire() method defined by the script.
     */
    public boolean postfire() throws IllegalActionException {
        // boolean result = super.postfire();
        PyObject postfireResult = _invokeMethod("postfire", null);
        if (postfireResult != null) {
            return postfireResult.__nonzero__();
        }
        return true;
    }

    /** Invoke the prefire() method if defined in the script. Return true
     *  when the method return value is not zero, or the method does not
     *  return a value, or the method is not defined in the script.
     *  @return False if prefire() is defined in the script and returns 0,
     *   true otherwise.
     *  @throws IllegalActionException If there is any error in calling the
     *   prefire() method.
     */
    public boolean prefire() throws IllegalActionException {
        // boolean result = super.prefire();
        PyObject prefireResult = _invokeMethod("prefire", null);
        if (prefireResult != null) {
            return prefireResult.__nonzero__();
        }
        return true;
    }

    /** Create an instance of the Main class defined in the script.
     *  Add all parameters and ports of this actor as attributes of
     *  the object, so that they become accessible to the methods
     *  defined in the script.
     *  @throws IllegalActionException If there is any error in creating
     *   an instance of the Main class defined in the script.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _object = _createObject();
    }

    /** Invoke the stop() method if defined in the script. Ignore any error
     *  in calling the method.
     */
    public void stop() {
        try {
            _invokeMethod("stop", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the stopFire() method if defined in the script. Ignore any error
     *  in calling the method.
     */
    public void stopFire() {
        try {
            _invokeMethod("stopFire", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the terminate() method if defined in the script. Ignore any
     *  error in calling the method.
     */
    public void terminate() {
        try {
            _invokeMethod("terminate", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the wrapup() method if defined in the script. Ignore any error
     *  in calling the method.
     *  @throws IllegalActionException If there is any error in calling the
     *   wrapup() method defined in the script.
     */
    public void wrapup() throws IllegalActionException {
        _invokeMethod("stop", null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create an instance of the Main class defined in the script.
     *  Add all parameters and ports of this actor as attributes of
     *  the object, so that they become accessible to the methods
     *  defined in the script. IllegalActionException is thrown if
     *  there is any error in creating an instance of the Main class
     *  defined in the script.
     */
    private PyObject _createObject() throws IllegalActionException {
        // create an instance using the interpreter
        // this could also be accomplished by using the __call__ method
        // of the Main class object
        String name = _mangleName(getName());
        _interpreter.exec(name + " = " + _CLASS_NAME + "()");
        PyObject object = _interpreter.get(name);
        if (object == null) {
            throw new IllegalActionException(this,
                    "Error in creating an instance of the Main class " +
                    "defined in the script.");
        }
        // set up access to this actor
        // first create an attribute "actor" on the object
        // the PyObject class does not allow adding a new attribute to the
        // object
        _interpreter.exec(name + ".actor = 0");
        object.__setattr__("actor", new PyJavaInstance(this));

        // give the object access to attributes and ports of this actor
        Iterator attributes = attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            String mangledName = _mangleName(attribute.getName());
            if (_debugging) {
                _debug("set up reference to attribute \"" +
                        attribute.getName() + "\" as \"" +
                        mangledName + "\"");
            }
            _interpreter.exec(name + "." + mangledName + " = 0");
            object.__setattr__(new PyString(mangledName),
                    new PyJavaInstance(attribute));
        }
        Iterator ports = portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port)ports.next();
            String mangledName = _mangleName(port.getName());
            if (_debugging) {
                _debug("set up reference to port \"" +
                        port.getName() + "\" as \"" +
                        mangledName + "\"");
            }
            _interpreter.exec(name + "." + mangledName + " = 0");
            object.__setattr__(new PyString(mangledName),
                    new PyJavaInstance(port));
        }

        // populate the method map
        for (int i = 0; i < _METHOD_NAMES.length; ++i) {
            String methodName = _METHOD_NAMES[i];
            PyMethod method = null;
            try {
                method = (PyMethod)object.__findattr__(methodName);
            } catch (ClassCastException ex) {
                // the object has an attribute with the methodName but
                // is not a method, ignore
            }
            _methodMap.put(methodName, method);
        }
        return object;
    }

    /*  Evaluate the script by invoking the python interpreter.
     *  IllegalActionException is thrown if the script does not
     *  define a class named Main, or the python interpreter
     *  cannot be initialized.
     */
    private void _evaluateScript() throws IllegalActionException {
        try {
            if (_interpreter == null) {
                _interpreter = new PythonInterpreter();
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot initialize python interpreter:");
        }
        String pythonScript = script.getExpression();
        try {
            _interpreter.exec(pythonScript);
        } catch (Exception ex) {
            throw new IllegalActionException(this,
                    ex, "Error in evaluating script:");
        }
        // get the class defined by the script
        try {
            _class = (PyClass)_interpreter.get(_CLASS_NAME);
        } catch (ClassCastException ex) {
            throw new IllegalActionException(this,
                    "The script does not define a Main class.");
        }
    }

    /*  Invoke the specified method on the instance of the Main class.
     *  Any argument that is not an instance of PyObject is wrapped in
     *  an instance of PyJavaInstance. The result of invoking the method
     *  is returned. IllegalActionException is thrown if there is any
     *  error in calling the method.
     */
    private PyObject _invokeMethod(String methodName, Object[] args)
            throws IllegalActionException {
        PyMethod method = (PyMethod)_methodMap.get(methodName);
        PyObject returnValue = null;
        if (method != null) {
            try {
                if (args == null || args.length == 0) {
                    returnValue = method.__call__();
                } else {
                    PyObject[] convertedArgs = new PyObject[args.length];
                    for (int i = 0; i < args.length; ++i) {
                        if (!(args[i] instanceof PyObject))
                            convertedArgs[i] = new PyJavaInstance(args[i]);
                        else
                            convertedArgs[i] = (PyObject)args[i];
                    }
                    returnValue = _object.__call__(convertedArgs);
                }
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Error in calling the " + methodName + " method:");
            }
        }
        return returnValue;
    }

    /*  Mangle the given name (usually the name of an entity, or a parameter,
     *  or a port). Any character that is not legal in Java identifiers is
     *  changed to the underscore character.
     */
    private String _mangleName(String name) {
        char[] nameChars = name.toCharArray();
        boolean mangled = false;
        for (int i = 0; i < nameChars.length; ++i) {
            if (!Character.isJavaIdentifierPart(nameChars[i])) {
                nameChars[i] = '_';
                mangled = true;
            }
        }
        if (mangled) {
            return new String(nameChars);
        }
        return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The class defined in the script.
    private PyClass _class;

    // The python interpreter.
    private PythonInterpreter _interpreter;

    // Map from method name to PyMethod objects.
    private HashMap _methodMap = new HashMap();

    // The instance of the Main class defined in the script.
    private PyObject _object;

    // The expected name of the class defined in the script.
    private static String _CLASS_NAME = "Main";

    // Invocation of methods named in this list is delegated to the instance
    // of the Main class defined in the script.
    // Listed here are all methods of the Executable interface, except
    // iterate().
    private static final String[] _METHOD_NAMES =
            {"fire", "initialize", "postfire", "prefire", "preinitialize",
             "stop", "stopFire", "terminate", "wrapup"};

}
