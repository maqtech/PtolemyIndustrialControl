/* AnExecute a script in JavaScript.

 Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.js;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaScript

/**
 Execute a script in JavaScript that can read inputs and parameters,
 perform calculations, and write outputs. The script may be provided
 as the textual value of the <i>script</i> parameter, or as an input
 on the <i>scriptIn</i> port.
 If it is an input, then it can be different on each firing.
 If it is a parameter, then the script is compiled in the initialize()
 method, and any changes made to it are ignored until the next initiatlization
 of the model.
  <p>
 To use this actor, add input and output ports and parameters, and specify
 a script.
 Your script should define one or more of the following functions:
 <ul>
 <li> <b>initialize</b>. This function is invoked each time this actor
      is initialized, or if an initialize function is provided on the
      <i>scriptIn</i> input port, then it will be invoked when this
      actor fires and consumes that input. This function can read
      parameter values using valueOf() (see below), normally there
      will be no inputs, so it should not read inputs.
 </li>
 <li> <b>fire</b>. This function is invoked each time this actor fires.
      It can read parameters, read inputs using get(), and write outputs
      using send(). Note that all inputs from all ports are consumed during 
      a firing, even if the script does not contain a call to get() for 
      a port. Multiple calls to get() during a
      firing will return the same value. 
 </li>
 <li> <b>wrapup</b>. This function is invoked at the end of execution of
      of the model. It can read parameters, but normally should not
      read inputs nor write outputs.
 </li>
 </ul>
 <p>
 Usually, you will need to explicitly specify the types
 of the output ports. By default, the type of an output
 port will be greater than or equal to the  inferred  types of
 all input ports, but often this will not be the type your
 script produces.
 <p>
 The context in which your functions run provide the following methods:
 <ul>
 <li> alert(string): pop up a dialog with the specified message.
 <li> error(string): throw an IllegalActionException with the specified message.
 <li> get(port, n): get an input from a port on channel n (return null if there is no input).
 <li> print(string): print the specified string to the console (standard out).
 <li> readURL(string): read the specified URL and return its contents as a string.
 <li> send(value, port, n): send a value to an output port on channel n
 <li> valueOf(parameter): retrieve the value of a parameter.
 </ul>
 The last argument of get() and send() is optional.
 If you leave it off, the channel number will be assumed to be zero
 (designating the first channel connected to the port).
 <p>
 The following example script calculates the factorial of the input.
 <pre>
function fire() {
  var value = get(input);
  if (value < 0) {
     error("Input must be greater than or equal to 0.");
  } else {
     var total = 1;
     while(value > 1) {
       total *= value;
       value--;
     }
     send(total, output);
  }
}
</pre>
 <p>
 Your script may also store values from one firing to the next, or from
 initialization to firing.  For example,
 <pre>
var init;
function initialize() {
  init = 0;
}
function fire() {
  init = init + 1;
  send(init, output);
}
</pre>
 will send a count of firings to the output named "output".
 <p>
 Notice that you may name ports or parameters of this actor in such
 a way as to shadow objects in the global scope. For example,
 JavaScript provides a JSON object that you can use to operate
 on JSON strings, for example with the function JSON.parse().
 However, if you name a port or parameter "JSON", then any reference
 to JSON in your fire() method, for example, will refer to the port
 or parameter, and not to the global object.  To explicitly refer
 to the global object, use the syntax "this.JSON".
 <p>
 In addition, the symbol "actor" is defined to be the instance of
 this actor. In JavaScript, you can invoke methods on it.
 For example, the JavaScript
 <pre>
    actor.toplevel().getName();
 </pre>
 will return the name of the top-level composite actor containing
 this actor.
 <p>
 Not all Ptolemy II data types translate naturally to
 JavaScript types. This actor converts Ptolemy types int,
 double, string, and boolean to and from equivalent JavaScript
 types. In addition, arrays are converted to native
 JavaScript arrays. Ptolemy II records are converted into
 JavaScript objects, and JavaScript objects with enumerable
 properties into records.
 <p>
 For other Ptolemy II types, the script will see the
 corresponding Token object.  For example, if you send
 a number of type long to an input of a JavaScript actor,
 the script (inside a fire() function):
 <pre>
    var value = get(input);
    print(value.getClass().toString());
 </pre>
 will print on standard out the string
 <pre>
    "class ptolemy.data.LongToken"
 </pre>
 JavaScript does not have a long data type, so instead the
 get() call returns a JavaScript Object wrapping the Ptolemy II
 LongToken object. You can then invoke methods on that token,
 such as getClass(), as done above.
 <p>
 Scripts can instantiate Java classes and invoke methods on them.
 For example, the following script will build a simple Ptolemy II model:
 <pre>
importPackage(Packages.ptolemy.actor);
importClass(Packages.ptolemy.actor.lib.Ramp);
importClass(Packages.ptolemy.actor.lib.FileWriter);
importClass(Packages.ptolemy.domains.sdf.kernel.SDFDirector);

var toplevel = new TypedCompositeActor();
var ramp = new Ramp(toplevel, "ramp");
var writer = new FileWriter(toplevel, "writer");

toplevel.connect(ramp.output, writer.input);

var director = new SDFDirector(toplevel, "SDFDirector");
director.iterations.setExpression("10");
</pre>
You can even send this out on an output port.
For example,
<pre>
send(toplevel, output, 0);
</pre>
where "output" is the name of the output port.
 
 @author Edward A. Lee
 @version $Id: Ramp.java 67679 2013-10-13 03:48:10Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class JavaScript extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JavaScript(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        script = new StringAttribute(this, "script");

        // Set the visibility to expert, as casual users should
        // not see the script.  This is particularly true if one
        // installs an actor that is an instance of this with a
        // particular script in the library.
        // FIXME: Not until we have the editors.
        // script.setVisibility(Settable.EXPERT);
        
        // Create the script input.
        // NOTE: This cannot be a PortParameter because it can't be a string-mode
        // parameter. The parameter syntax uses $var substitution, which is likely
        // to conflict with JavaScript, particularly if jQuery is used.
        scriptIn = new TypedIOPort(this, "scriptIn", true, false);
        scriptIn.setTypeEquals(BaseType.STRING);
        StringAttribute cardinal = new StringAttribute(scriptIn, "_cardinal");
        cardinal.setExpression("SOUTH");

        // initialize the script to provide an empty template:
        script.setExpression("// Put your JavaScript program here.\n"
        		+ "// Add ports and parameters.\n"
        		+ "// Define JavaScript functions initialize(), fire(), and/or wrapup().\n"
        		+ "// Use valueOf(parameterName) to refer to parameters.\n"
        		+ "// In the fire() function, use get(parameterName, channel) to read inputs.\n"
        		+ "// Send to output ports using send(value, portName, channel).\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The script to execute when this actor fires. */
    public StringAttribute script;
    
    /** Alternative way to provide a script to execute when this actor fires.
     *  This input port has type string.
     *  If this is connected and provided an input, then the
     *  script provided as input is executed instead of the one given
     *  by the script parameter.
     */
    public TypedIOPort scriptIn;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the current value of the state of this actor to the output.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        // Update any port parameters that have been added.
        for (PortParameter portParameter : attributeList(PortParameter.class)) {
        	portParameter.update();
        }
                
        // If there is an input at scriptIn, evaluate that script instead.
        try {
        	if (scriptIn.getWidth() > 0 && scriptIn.hasToken(0)) {
        		// A script is provided as input.
        		String scriptValue = ((StringToken)scriptIn.get(0)).stringValue();
        		
        		// Compile the script.
                _compiledScript = _context.compileString(scriptValue, getName(), 1, null);
                
        		// Execute the script (Note that the script defines functions and variables;
                // the functions are not invoked here. Just defined.)
        		_compiledScript.exec(Context.getCurrentContext(), _scope);
        		
        		// Execute the initialize() function, if it exists.
        		Object initializeFunction = _scope.get("initialize", _scope);
        		if (initializeFunction instanceof Function) {
        			((Function)initializeFunction).call(Context.getCurrentContext(), _scope, _global, null);
        		}
        	}
        	// If there is a fire() function, invoke it.
        	Object fireFunction = _scope.get("fire", _scope);
        	if (fireFunction instanceof Function) {
        		// FIXME: Provide a last argument for security.
        	    
        	    // send out buffered outputs
        	    for (IOPort port : _outputTokens.keySet()) {
                    HashMap<Integer, Token> tokens = _outputTokens.get(port);
                    for (Integer i : tokens.keySet()) {
                        Token token = tokens.get(i);
                        port.send(i, token);
                    }
                }
        	    _outputTokens.clear();
        	    
        	    // buffer inputs
        	    _inputTokens.clear();
        	    for (IOPort input : this.inputPortList()) {
        	        HashMap<Integer, Token> tokens = new HashMap<Integer, Token>();
        	        for (int i = 0; i < input.getWidth(); i++) {
        	            tokens.put(i, input.get(i));
        	        }
        	        _inputTokens.put(input, tokens);
        	    }
        	    
        	    _inFire = true;
        		((Function)fireFunction).call(Context.getCurrentContext(), _scope, _global, null);
        		_inFire = false;
        	}
        } catch (WrappedException ex) {
        	Throwable original = ex.getWrappedException();
        	throw new IllegalActionException(this, ex,
        			"Exception during executing script at line "
        			+ ex.lineNumber()
        			+ ".\n"
        			+ original.getMessage());
        }
    }

    /** Register the ports and parameters with the JavaScript engine.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Expose the ports and parameters by name as JavaScript variables.
        for (TypedIOPort port : portList()) {
        	// Do not convert the scriptIn port to a JavaScript variable.
        	if (port == scriptIn) {
        		continue;
        	}
        	Object jsObject = Context.javaToJS(port, _scope);
        	_scope.put(port.getName(), _scope, jsObject);
        }
        for (Parameter parameter : attributeList(Parameter.class)) {
        	Object jsObject = Context.javaToJS(parameter, _scope);
        	_scope.put(parameter.getName(), _scope, jsObject);
        }
                
        // Compile the script to execute at run time.
        String scriptValue = script.getValueAsString();
        _compiledScript = _context.compileString(scriptValue, getName(), 1, null);
        
		// Execute the script (Note that the script defines functions and variables;
        // the functions are not invoked here. Just defined.)
		_compiledScript.exec(Context.getCurrentContext(), _scope);
		
		// Execute the initialize() function, if it exists.
		Object initializeFunction = _scope.get("initialize", _scope);
		if (initializeFunction instanceof Function) {
			((Function)initializeFunction).call(Context.getCurrentContext(), _scope, _global, null);
		}
    }

    /** Return true if the specified string is not a JavaScript keyword and is a valid JavaScript identifier.
     *  @param identifier The proposed name.
     *  @return True if it is a valid identifier name.
     */
    public static boolean isValidIdentifierName(String identifier) {
    	// Pathetically, neither JavaScript nor Rhino provide this method.
        int length = identifier.length();
        if (length == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            return false;
        }
        for (int i = 1; i != length; ++i) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }
        return !_KEYWORDS.contains(identifier);
    }

    /** Set the state to equal the value of the <i>init</i> parameter.
     *  The state is incremented by the value of the <i>step</i>
     *  parameter on each iteration (in the postfire() method).
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Create a context for the currrent thread.
        _context = Context.enter();
        // If you want to include standard Rhino methods, like print, do this:
        // _context.initStandardObjects();
        
        // Create a scope for this actor.
        _scope = new ImporterTopLevel(Context.getCurrentContext());
        // Create also also a global scope so that if the actor shadows any
        // global objects, they can still be referenced using this.objectName.
        _global = new ImporterTopLevel(Context.getCurrentContext());
        
        // Define built-in methods in the context.
        PtolemyJavaScript scriptable = new PtolemyJavaScript();
        scriptable.setParentScope(_scope);
        // Get a reference to the instance method to be made available in JavaScript as a global function.
        Method scriptableInstanceMethod;
		try {
			// Create the alert() method.
			String methodName = "alert";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class});
	        FunctionObject scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);

			// Create the error() method.
	        methodName = "error";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);

	        // Create the get() method.
	        methodName = "get";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName,
					new Class[]{NativeJavaObject.class, Double.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);

			// Create the print() method.
	        methodName = "print";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);

			// Create the readURL() method.
	        methodName = "readURL";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);

	        // Create the send() method.
	        methodName = "send";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName,
					new Class[]{Object.class, NativeJavaObject.class, Double.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);
	        
	        // Create the valueOf() method.
	        methodName = "valueOf";
			scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName,
					new Class[]{NativeJavaObject.class});
	        scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
	        // Make it accessible within the scriptExecutionScope.
	        _scope.put(methodName, _scope, scriptableFunction);
        
            // Create the requestAuth() method.
            methodName = "requestAuth";
                    scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class, String.class, String.class, Boolean.class});
            scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
            // Make it accessible within the scriptExecutionScope.
            _scope.put(methodName, _scope, scriptableFunction);
            
            // Create the requestAuth() method.
            methodName = "requestAccess";
                    scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class, String.class, String.class, String.class, String.class});
            scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
            // Make it accessible within the scriptExecutionScope.
            _scope.put(methodName, _scope, scriptableFunction);
            
            // Create the readProtectedURL() method.
            methodName = "readProtectedURL";
                    scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class, String.class});
            scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
            // Make it accessible within the scriptExecutionScope.
            _scope.put(methodName, _scope, scriptableFunction);
            
            // Create the openBrowser() method.
            methodName = "openBrowser";
                    scriptableInstanceMethod = PtolemyJavaScript.class.getMethod(methodName, new Class[]{String.class});
            scriptableFunction = new PtolemyFunctionObject(methodName, scriptableInstanceMethod, scriptable);
            // Make it accessible within the scriptExecutionScope.
            _scope.put(methodName, _scope, scriptableFunction);

	        // This actor is exposed as an object named "actor".
        	Object jsObject = Context.javaToJS(this, _scope);
        	_scope.put("actor", _scope, jsObject);
		} catch (Exception e) {
			throw new IllegalActionException(this, e, "Failed to create built-in JavaScript methods.");
		}
    }
    
    /** Execute the wrapup function, if it is defined, and exit the context for this thread.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
    	// If there is a wrapup() function, invoke it.
    	Object wrapupFunction = _scope.get("wrapup", _scope);
    	if (wrapupFunction instanceof Function) {
    		// FIXME: Provide a last argument for security.
    		((Function)wrapupFunction).call(Context.getCurrentContext(), _scope, _global, null);
    	}

    	// This is static because the context depends on the current thread.
        // So this exits the context associated with the current thread.
    	Context.exit();
    	super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////
    
    /** Compiled JavaScript. */
    protected Script _compiledScript;
    
    /** Rhino context. */
    protected Context _context;
        
    /** Global scope, unpolluted by anything in this actor. */
    protected Scriptable _global;

    /** Keywords. */
    protected static final String[] _JAVASCRIPT_KEYWORDS = new String[] {
			"abstract", "as", "boolean", "break", "byte", "case", "catch",
			"char", "class", "continue", "const", "debugger", "default",
			"delete", "do", "double", "else", "enum", "export", "extends",
			"false", "final", "finally", "float", "for", "function", "goto",
			"if", "implements", "import", "in", "instanceof", "int",
			"interface", "is", "long", "namespace", "native", "new", "null",
			"package", "private", "protected", "public", "return", "short",
			"static", "super", "switch", "synchronized", "this", "throw",
			"throws", "transient", "true", "try", "typeof", "use", "var",
			"void", "volatile", "while", "with" };
    
    /** Keywords as a Set. */
    protected static final Set<String> _KEYWORDS = new HashSet<String>(Arrays.asList(_JAVASCRIPT_KEYWORDS));
    
    /** Scope in which to evaluate scripts. */
    protected Scriptable _scope;
    
    /** True while the actor is firing, false otherwise. */
    private boolean _inFire;
    
    /** Buffer for all input tokens before calling the fire function 
     *  in the script. Calls to get in the script will then retrieve 
     *  the value from this buffer instead of actually calling get on 
     *  the port.
     */
    private HashMap<IOPort, HashMap<Integer, Token>> _inputTokens = new HashMap();
    
    /** Buffer for output tokens that are produced in a call to send
     *  while the actor is not firing. This makes sure that actors can
     *  spontaneously produce outputs.
     */
    private HashMap<IOPort, HashMap<Integer, Token>> _outputTokens = new HashMap();
    
    ///////////////////////////////////////////////////////////////////
    ////                        Inner Classes                      ////

    /** Container class for built-in methods.
     */
    @SuppressWarnings("serial")
    public class PtolemyJavaScript extends ScriptableObject {

    	/** Alert the user with a message. */
    	public void alert(String message) {
    		MessageHandler.message(message);
    	}

    	/** Throw an IllegalActionException with the specified message. */
    	public void error(String message) throws IllegalActionException {
    		throw new IllegalActionException(JavaScript.this, message);
    	}
    	
    	/** Get buffered inputs for a given input port.
    	 *  @param portWrapper A JavaScript wrapper for a Port.
    	 *  @param channel A channel number, or NaN to use the default (0).
    	 */
    	public Object get(NativeJavaObject portWrapper, Double channel) {    		
    		// In JavaScript, all numbers are doubles. So we have to convert
    		// to an integer.
    		int channelNumber = 0;
    		// Tolerate null or NaN as an argument.
    		// Interestingly, Rhino's implementation of JavaScript, if get(port) is
    		// called with only one argument, does in fact call this method, passing
    		// NaN as the second argument.
    		// This seems unlikely to be robust, so I'm not documenting the "feature"
    		// that you can leave off the channel number.
			if (channel != null && !channel.isNaN()) {
				if (channel.doubleValue() % 1 == 0) {
					// The value is actually an integer.
					channelNumber = channel.intValue();
				} else {
        			throw new InternalErrorException(JavaScript.this, null,
        					"Second argument to send(port, channel) is required to be an integer. Got "
        					+ channel);
				}
			}

    		Object unwrappedPort = portWrapper.unwrap();
    		if (unwrappedPort instanceof TypedIOPort) {
    			TypedIOPort port = (TypedIOPort)unwrappedPort;
        		if (!port.isInput()) {
        			throw new InternalErrorException(JavaScript.this, null,
        					"Cannot get from " + port.getName() + ", which is not an input port.");
        		}
        		try {
            		if (port.getWidth() < 1) {
            			// Port is not connected.
            			return null;
            		}
    				return _wrapToken(_inputTokens.get(port).get(channelNumber));
    			} catch (KernelException e) {
        			throw new InternalErrorException(JavaScript.this, e,
        					"Failed to get input via port " + port.getName() + ".");
    			}
    		} else if (unwrappedPort instanceof PortParameter) {
        		try {
        			PortParameter parameter = (PortParameter)unwrappedPort;
    				return _wrapToken(parameter.getToken());
    			} catch (KernelException e) {
        			throw new InternalErrorException(JavaScript.this, e,
        					"Failed to get value of " + ((PortParameter)unwrappedPort).getName() + ".");
    			}
    		} else {
    			throw new InternalErrorException(JavaScript.this, null,
    					"First argument of get() must be an input port. It is " + unwrappedPort.toString() + ".");
    		}
    	}
    	
    	

		@Override
		public String getClassName() {
			return getClass().getName();
		}

    	/** Print a message to standard out. */
    	public void print(String message) {
    		System.out.println(message);
    	}
    	
    	/** Read the specified URL and return its contents
    	 *  @param url The URL to read.
    	 * @throws IOException 
    	 */
    	public String readURL(String url) throws IOException {
    		URL theURL = new URL(url);
    		InputStream stream = theURL.openStream();
    		// FIXME: Should provide a characterset optional second argument.
    		// This is supported by InputStreamReader.
    		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    		StringBuffer result = new StringBuffer();
    		String line = reader.readLine();
    		while (line != null) {
    			result.append(line);
        		result.append(StringUtilities.LINE_SEPARATOR);
    			line = reader.readLine();
    		}
    		return result.toString();
    	}

    	/** Send outputs via an output port.
    	 *  @param data The data to send via the port.
    	 *  @param portWrapper A JavaScript wrapper for a Port.
    	 *  @param channel A channel number, or NaN to use the default (0).
    	 */
    	public void send(Object data, NativeJavaObject portWrapper, Double channel) {
    		
    		// In JavaScript, all numbers are doubles. So we have convert
    		// to an integer.
    		int channelNumber = 0;
    		// Tolerate null or NaN as an argument.
    		// Interestingly, Rhino's implementation of JavaScript, if get(port) is
    		// called with only one argument, does in fact call this method, passing
    		// NaN as the second argument.
    		// This seems unlikely to be robust, so I'm not documenting the "feature"
    		// that you can leave off the channel number.
			if (channel != null && !channel.isNaN()) {
				if (channel.doubleValue() % 1 == 0) {
					// The value is actually an integer.
					channelNumber = channel.intValue();
				} else {
        			throw new InternalErrorException(JavaScript.this, null,
        					"Second argument to send(port, channel) is required to be an integer. Got "
        					+ channel);
				}
			}

    		Object unwrappedPort = portWrapper.unwrap();
    		if (unwrappedPort instanceof TypedIOPort) {
    			TypedIOPort port = (TypedIOPort)unwrappedPort;
        		if (!port.isOutput()) {
        			throw new InternalErrorException(JavaScript.this, null,
        					"Cannot send via " + port.getName() + ", which is not an output port.");
        		}
        		if (data instanceof NativeJavaObject) {
        			data = ((NativeJavaObject) data).unwrap();
        		}
        		try {
        		    Token token = _createToken(data);
        		    if (_inFire) {
        		        port.send(channelNumber, token);
        		    } else {
        		        HashMap<Integer, Token> tokens = _outputTokens.get(port);
        		        if (tokens == null) {
        		            tokens = new HashMap<Integer, Token>();
        		        }
        		        tokens.put(channelNumber, token);
        		        _outputTokens.put(port, tokens);
        		    }
    			} catch (KernelException e) {
        			throw new InternalErrorException(JavaScript.this, e,
        					"Failed to send output via port " + port.getName() + ".");
    			}
    		} else {
    			throw new InternalErrorException(JavaScript.this, null,
    					"First argument of send() must be an output port. It is " + unwrappedPort.toString() + ".");
    		}
    	}
		
    	/** Get parameter values.
    	 *  @param paramWrapper A JavaScript wrapper for a Variable.
    	 */
    	public Object valueOf(NativeJavaObject paramWrapper) {
    		Object unwrappedParam = paramWrapper.unwrap();
    		if (unwrappedParam instanceof Variable) {
    			Variable parameter = (Variable)unwrappedParam;
        		try {
    				return _wrapToken(parameter.getToken());
    			} catch (KernelException e) {
        			throw new InternalErrorException(JavaScript.this, e,
        					"Failed to get parameter value for " + parameter.getName() + ".");
    			}
    		} else {
    			throw new InternalErrorException(JavaScript.this, null,
    					"First argument of send() must be an output port. It is " + unwrappedParam.toString() + ".");
    		}
    	}
    	
    	/**
    	 * Request an OAuth 2.0 authorization token. This method requires interaction with an Authorization server 
    	 * which is usually implemented as a web service.
    	 * @param providerName References an internally stored end point URL for popular OAuth providers like Google, 
    	 *         Twitter, Facebook, etc. 
    	 * @param clientId This identifies the application that accesses a resource. Usually, client ids are issued 
    	 *         after registering an application via a developer console at a resource provider.
    	 * @param redirectUrl After completion of the authorization request this URL is used to redirect the browser.
    	 * @param openBrowser Indicates, if the method should invoke the system's default browser to enable the user 
    	 *         to login to the server in order to grant access, or if the script handles this on its own.
    	 * @return returns The authorization code, if the authentication at the Authorization Server was successful. 
    	 * @throws IllegalActionException 
    	 */
    	public String requestAuth(String providerName, String clientId, String redirectUrl, Boolean openBrowser) throws IllegalActionException {
    	        OAuthProviderType oauthProvider;
    	        if("google".equals(providerName.toLowerCase()))  {
    	                oauthProvider = OAuthProviderType.GOOGLE;
    	        }
    	        else  {
    	                throw new IllegalActionException("Provider '"+providerName+"' not known. Please consult documentation for built-in provider names.");
    	        }
    	    
                OAuthClientRequest request;
                try {
                        request = OAuthClientRequest
                                .authorizationProvider(oauthProvider)
                                .setClientId(clientId)                  
                                .setRedirectURI(redirectUrl)
                                .setResponseType("code")
                                .setScope("email profile")
                                .buildQueryMessage();
                } catch (OAuthSystemException ex) {
                    throw new IllegalActionException(null, ex, "Could not build OAuth request message.");
                }
                
                if(openBrowser)  {
                        openBrowser(request.getLocationUri());
                }

                return request.getLocationUri();
    	}
    	
    	/**
    	 * Uses an Authorization code to retrieve an Access code.
    	 * @param providerName
    	 * @param clientId
    	 * @param clientSecret
    	 * @param redirectUrl
    	 * @param authCode The authorization code issued by the Authorization server.
    	 * @return The Access code token. This can be used to access the Resource server.
    	 * @throws IllegalActionException
    	 */
    	public String requestAccess(String providerName, String clientId, String clientSecret, String redirectUrl, String authCode) throws IllegalActionException {
                OAuthProviderType oauthProvider;
                if("google".equals(providerName.toLowerCase()))  {
                        oauthProvider = OAuthProviderType.GOOGLE;
                }
                else  {
                        throw new IllegalActionException("Provider '"+providerName+"' not known. Please consult documentation for built-in provider names.");
                }
                OAuthClientRequest request;
                OAuthJSONAccessTokenResponse response;
                try {
                        request = OAuthClientRequest
                                .tokenProvider(oauthProvider)
                                .setGrantType(GrantType.AUTHORIZATION_CODE)
                                .setClientId(clientId)
                                .setClientSecret(clientSecret)
                                .setRedirectURI(redirectUrl)
                                .setCode(authCode)
                                .buildBodyMessage();
                        
                        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
                        response = oAuthClient.accessToken(request); 
                        
                } catch (OAuthSystemException ex) {
                    throw new IllegalActionException(null, ex, "Could not build OAuth request message.");
                } catch (OAuthProblemException ex2) {
                    throw new IllegalActionException(null, ex2, "Could not build OAuth request message.");
                }
                return response.getAccessToken();
        }
    	
    	/**
    	 * Open a URL of web service that is protected by OAuth 2.0.
    	 * @param url The protected URL on a Resource server. Usually this is some kind of RESTful API.
    	 * @param accessToken The code used to prove access authorization to the Resource server.
    	 * @return
    	 * @throws IllegalActionException 
    	 */
        public String readProtectedURL(String url, String accessToken) throws IllegalActionException  { 
            OAuthResourceResponse resourceResponse = null;
            try {
                    OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(url)
                            .setAccessToken(accessToken)
                            .buildQueryMessage();

                    OAuthClient client = new OAuthClient(new URLConnectionClient());
                    resourceResponse = client.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            } catch (OAuthSystemException ex) {
                throw new IllegalActionException(null, ex, "Could not connect to resource server: " + ex.getMessage());
            } catch (OAuthProblemException ex2) {
                throw new IllegalActionException(null, ex2, "Could not connect to resource server: " + ex2.getMessage());
            }
            if(resourceResponse!=null)  {  
                    if(resourceResponse.getResponseCode()==200)  {                   
                            return resourceResponse.getBody();
                    }  else  { 
                            return "Could not access resource: " + 
                                    resourceResponse.getResponseCode() + " " + 
                                    resourceResponse.getBody();
                    }
            }  else  {
                    throw new IllegalActionException("Could not execute resource access request.");
            }
        }
    	
        /**
         * Opens a new tab in the default system browser. 
         * See also {@link http://www.mkyong.com/java/open-browser-in-java-windows-or-linux/}
         * @param url The URL that the browser shall retrieve.
         * @throws IllegalActionException 
         */
        public String openBrowser(String url) throws IllegalActionException  {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();
            
            try {
                    if (os.indexOf( "win" ) >= 0) {                        
                            // this doesn't support showing urls in the form of "page.html#nameLink" 
                            rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);         
                    } else if (os.indexOf( "mac" ) >= 0) {         
                            rt.exec( "open " + url);         
                    } else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {         
                            // Do a best guess on unix until we get a platform independent way
                            // Build a list of browsers to try, in this order.
                            String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape","opera","links","lynx"};         
                            // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                            StringBuffer cmd = new StringBuffer();
                            for (int i=0; i<browsers.length; i++)  {
                                    cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
                            }
                            rt.exec(new String[] { "sh", "-c", cmd.toString() });         
                   } else {
                           throw new IllegalActionException("No browser found.");
                   }
            } catch (IOException e) {
                   throw new IllegalActionException("Could not open browser.");
            }
            return ""; //FIXME Return empty string to suppress 'null' output. Is this the right thing to do?
        }
    	

		/** Create a Ptolemy Token from and object sent by JavaScript. 
		 *  @throws IllegalActionException If constructing a Ptolemy Token fails.
		 */
		private Token _createToken(Object data) throws IllegalActionException {
	    	//**********************************************************************
			// This the key place to support additional output data types.
			if (data instanceof Integer) {
				return new IntToken(((Integer)data).intValue());
			} else if (data instanceof Boolean) {
				return new BooleanToken(((Boolean) data).booleanValue());
			} else if (data instanceof Double) {
				// Since JavaScript represents all numbers as double, we first
				// check to see whether this is actually an integer.
				if (((Double)data).doubleValue() % 1 == 0) {
					// The value is actually an integer.
					return new IntToken(((Double)data).intValue());
				}
				return new DoubleToken(((Double)data).doubleValue());
			} else if (data instanceof String) {
				return new StringToken(data.toString());
			} else if (data instanceof NativeArray) {
				NativeArray array = (NativeArray)data;
				int length = array.size();
				Token[] result = new Token[length];
				for (int i = 0; i < length; i++) {
					result[i] = _createToken(array.get(i));
				}
				return new ArrayToken(result);
			} else if (data instanceof NativeObject) {
				// If the object has a non-empty key set, then construct
				// a record. Otherwise, just return an ObjectToken.
				NativeObject object = (NativeObject)data;
				Set<Object> properties = object.keySet();
				if (properties == null || properties.isEmpty()) {
					return new ObjectToken(data);
				}
				boolean foundOne = false;
				ArrayList<String> keys = new ArrayList<String>();
				ArrayList<Token> values = new ArrayList<Token>();
				for (Object key : properties) {
					if (key instanceof String) {
						Object value = object.get(key);
						if (value != null) {
							foundOne = true;
							keys.add((String)key);
							values.add(_createToken(value));
						}
					}
				}
				if (foundOne) {
					return new RecordToken(
							keys.toArray(new String[keys.size()]),
							values.toArray(new Token[values.size()]));
				}
				return new ObjectToken(data);
			} else if (data instanceof Entity) {
				return new ActorToken((Entity)data);
			} else {
				return new ObjectToken(data);
			}
		}
		
    	//**********************************************************************
		// This the key place to support additional input and parameter data types.
    	// Given a Ptolemy II token, if there is a corresponding native
    	// JavaScript type, then return something that will naturally be
    	// converted to that type.
    	private Object _wrapToken(Token token) {
    		if (token instanceof BooleanToken) {
    			return new Boolean(((BooleanToken)token).booleanValue());
    		} else if (token instanceof DoubleToken) {
    			return new Double(((DoubleToken)token).doubleValue());
    		} else if (token instanceof IntToken) {
    			return new Integer(((IntToken)token).intValue());
    		} else if (token instanceof StringToken) {
    			return ((StringToken)token).stringValue();
    		} else if (token instanceof ActorToken) {
    			Entity entity =  ((ActorToken)token).getEntity();
    			return new NativeJavaObject(_scope, entity, entity.getClass());
    		} else if (token == null) {
    			return null;
    		} else if (token instanceof ArrayToken) {
    			int length = ((ArrayToken)token).length();
    			Object[] result = new Object[length];
    			for (int i = 0; i < length; i++) {
    				Token element = ((ArrayToken)token).getElement(i);
    				result[i] = _wrapToken(element);
    			}
    			// Don't directly construct a NativeArray because
    			// that creates an array with no context or scope.
    			return _context.newArray(_scope, result);
    		} else if (token instanceof RecordToken) {
    			RecordToken record = (RecordToken)token;
    			Scriptable result = _context.newObject(_scope);
    			for (String name : record.labelSet()) {
    				Token element = record.get(name);
    				result.put(name, result, _wrapToken(element));
    			}
    			// Don't directly construct a NativeArray because
    			// that creates an array with no context or scope.
    			return result;
    		}
    		return Context.toObject(token, _scope);
    	}
    }
    
    /** Specialized FunctionObject that provides the parent scope when evaluating the function.
     */
    @SuppressWarnings("serial")
    private static class PtolemyFunctionObject extends FunctionObject {

        private PtolemyFunctionObject(String name, Member methodOrConstructor, Scriptable parentScope) {
          super(name, methodOrConstructor, parentScope);
        }

        @Override
        public Object call(Context context, Scriptable scope, Scriptable thisObj, Object[] args) {
          return super.call(context, scope, getParentScope(), args);
        }
    }
}
