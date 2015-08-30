/* Execute a script in JavaScript using Nashorn.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaScript

/**
   An actor whose functionality is given in JavaScript.
   The script defines one or more functions that configure this actor
   with ports and documentation information, initialize the actor,
   perform runtime functions such as reacting to inputs and producing outputs,
   and perform finalization (wrapup) functions. The script may be provided
   as the textual value of the <i>script</i> parameter, or as an input
   on the <i>script</i> port. You can add to the script or modify
   function definitions on each firing.
   <p>
   To use this actor, add input and output ports, parameters (if you like),
   and specify a script. The names of the ports and parameters are currently
   required to be valid JavaScript identifiers and are not permitted to be
   JavaScript keywords, but this constraint will be relaxed in the future.
   </p>
   <p>
   The script may also reference parameters in the scope of this actor
   using usual Ptolemy II $ syntax.  E.g., ${foo} in the script will be
   replaced with the value of foo before the script is evaluated.
   However, this use of parameters will retrieve the value of the parameter
   only when the script is read, so updates to the
   parameter value will not be noticed.
   </p>
   <p>
   Your script can define zero or more of the following functions:</p>
   <ul>
   <li> <b>exports.setup</b>. This function is invoked when the script parameter
   is first set and whenever the script parameter is updated. This function can
   be used to configure this actor with input and output ports.  For example,
   <pre>
     exports.setup = function() {
         actor.input('foo', {'type':'string'});
     }
   </pre>
   will create an input port named "foo" (if one does not already exist), and set
   its type to "string", possibly overriding any previously set data type.
   The methods that are particularly useful to use in setup are input, output,
   author, description, and version.
   <li> <b>exports.initialize</b>. This function is invoked each time this actor
   is initialized. This function should not read inputs or produce outputs.</li>
   <li> <b>exports.fire</b>. This function is invoked each time this actor fires.
   It can read inputs using get() and write outputs using send().
   This actor will consume at most one input token from each input port on
   each firing, if one is available. Any number of calls to get() during the
   firing will return the same consumed value, or will return null if there
   is no available input on that firing.  If you want it to instead return
   a previously read input, then mark the port persistent by giving it a
   <i>defaultValue</i> parameter, or use a PortParameter instead of an ordinary port.</li>
   <li> <b>exports.wrapup</b>. This function is invoked at the end of execution of
   of the model. It can read parameters, but normally should not
   read inputs nor write outputs.</li>
   </ul>
   These functions are fields of the exports object, as usual JavaScript CommonJS modules.
   For example, to define the fire function, specify code like this:
   <pre>
      exports.fire = function () {... function body ...};
   </pre>
   Alternatively, you can do
   <pre>
      var fire = function() {... function body ...};
      exports.fire = fire;
   </pre>
   Your script may also register <b>input handler</b> functions by invoking
   <pre>
      var handle = addInputHandler(function, port);
   </pre>
   <p>
   The specified function will be invoked whenever the port receives a new input.
   Note that the fire() function, if defined, will also be invoked (after the
   specified function) and will see
   the same input. If the specified function is null, then only the fire() function
   will be invoked. The returned handle can be used to call removeInputHandler().
   </p>
   <p>
   Usually, you will need to explicitly set the types
   of the output ports. Alternatively, you can enable backward
   type inference on the enclosing model, and the types of output
   ports will be inferred by how the data are used.</p>
   <p>
   You may also need to set the type of input ports. Usually, forward
   type inference will work, and the type of the input port will be based
   on the source of data. However,
   if the input comes from an output port whose output is undefined,
   such as JSONToToken, then you may want to
   enable backward type inference, and specify here the type of input
   that your script requires.</p>
   <p>
   The context in which your functions run provide the following global functions:</p>
   <ul>
   <li> addInputHandler(function, input): Specify a function to invoke when the input
        with name input (a string) receives a new input value. Note that after that
        function is invoked, the accessor's fire() function will also be invoked,
        if it is defined. If the specified function is null, then only the fire()
        function will be invoked. If the input argument is null or omitted, then
        the specified function will be invoked when any new input arrives to the
        accessor. This function returns a handle that can be used to call removeInputHandler().</li>
   <li> alert(string): pop up a dialog with the specified message.</li>
   <li> clearInterval(int): clear an interval with the specified handle.</li>
   <li> clearTimeout(int): clear a timeout with the specified handle.</li>
   <li> error(string): send a message to error port, or throw an exception if the error port is not connected.</li>
   <li> get(portOrParameter, n): get an input from a port on channel n or a parameter
        (return null if there is no such port or parameter).</li>
   <li> httpRequest(url, method, properties, body, timeout): HTTP request (GET, POST, PUT, etc.)</li>
   <li> localHostAddress(): If not in restricted mode, return the local host IP address as a string. </li>
   <li> print(string): print the specified string to the console (standard out).</li>
   <li> readURL(string): read the specified URL and return its contents as a string (HTTP GET).</li>
   <li> removeInputHandler(handle, input): Remove the callback function with the specified handle
        (returned by addInputHandler()) for the specified input (a string). </li>
   <li> require(string): load and return a CommonJS module by name. See
        <a href="http://wiki.commonjs.org/wiki/Modules">http://wiki.commonjs.org/wiki/Modules</a></li>
   <li> send(value, port, n): send a value to an output port on channel n</li>
   <li> set(value, parameter): set the value of a parameter of this JavaScript actor. </li>
   <li> setInterval(function, int): set the function to execute after specified time and then periodically and return handle.</li>
   <li> setTimeout(function, int): set the function to execute after specified time and return handle.</li>
   </ul>
   <p>
   The last argument of get() and send() (the channel number) is optional.
   If you leave it off, the channel number will be assumed to be zero
   (designating the first channel connected to the port).</p>
   <p>
   Note that get() may be called within a JavaScript callback function. In that case,
   if the callback function is invoked during the firing of this actor, then the get()
   will return immediately. Otherwise, the get() method will request a firing of this
   actor at the current time and block the JavaScript thread until this actor is in
   that firing.  This way, this actor ensures that get() reads a proper input.
   Note that although blocking JavaScript functions is not normally done, this actor
   has its own JavaScript engine, so no other JavaScript anywhere in the model will be
   affected. Those JavaScript threads are not blocked.</p>
   <p>
   The following example script calculates the factorial of the input.</p>
   <pre>
   exports.fire = function() {
       var value = get(input);
       if (value < 0) {
           throw "Input must be greater than or equal to 0.";
       } else {
           var total = 1;
           while (value > 1) {
               total *= value;
               value--;
           }
           send(total, output);
       }
   }
   </pre>
   <p>
   Your script may also store values from one firing to the next, or from
   initialization to firing.  For example,</p>
   <pre>
   var init;
   exports.initialize() = function() {
       init = 0;
   }
   exports.fire = function() {
       init = init + 1;
       send(init, output);
   }
   </pre>
   <p>will send a count of firings to the output named "output".</p>
   <p>
   In addition, the symbols "actor" and "accessor" are defined to be the instance of
   this actor. In JavaScript, you can invoke methods on it.
   For example, the JavaScript</p>
   <pre>
   actor.toplevel().getName();
   </pre>
   <p>will return the name of the top-level composite actor containing
   this actor.</p>
   <p>
   Not all Ptolemy II data types translate naturally to
   JavaScript types. Simple types will "just work."
   This actor converts Ptolemy types int,
   double, string, and boolean to and from equivalent JavaScript
   types when sending and getting to and from ports.
   In addition, arrays at input ports are converted to native
   JavaScript arrays. When sending a JavaScript array to an output
   port, it will be converted into a Ptolemy II array, but keep in
   mind that Ptolemy II arrays have a single element type, namely
   a type that every element can be converted to. So, for example,
   sending the JavaScript array [1, 2, "foo"] to an output port
   will result in the Ptolemy II array {"1", "2", "foo"}, an array of strings.
   If you wish to send a JavaScript array (or any other JavaScript
   object) without modifying it, wrap it in an ObjectToken, as
   in this example:</p>
   <pre>
   var ObjectToken = Java.type('ptolemy.data.ObjectToken');
   exports.fire = function() {
      var token = new ObjectToken([1, 2, 'foo']);
      send(token, output);
   }
   </pre>
   <p>The type of the output port will need to be set to object or general.
   If you send this to another JavaScript actor, that actor can
   retrieve the original JavaScript object as follows:</p>
   <pre>
   var ObjectToken = Java.type('ptolemy.data.ObjectToken');
   exports.fire = function() {
      var token = get(input);
      var array = token.getValue();
      ... operate on array, which is the original [1, 2, 'foo'] ...
   }
   </pre>
   <p>
   Ptolemy II records are also converted into
   JavaScript objects, and JavaScript objects with enumerable
   properties into records.
   When converting a JavaScript object to a record, each enumerable
   property (own or inherited) is included as a field in the resulting
   record, with the value of the field is converted in the same manner.
   To send a JavaScript object without any conversion, wrap it in an
   ObjectToken as with the array example above.
   </p><p>
   These automatic conversions do not cover all cases of interest.
   For example, if you provide inputs to this JavaScript actor that
   cannot be converted, the script will see the
   corresponding Token object.  For example, if you send
   a number of type long to an input of a JavaScript actor,
   the script (inside a fire() function):</p>
   <pre>
      var value = get(input);
      print(value.getClass().toString());
   </pre>
   <p>will print on standard out the string</p>
   <pre>
      "class ptolemy.data.LongToken"
   </pre>
   <p>JavaScript does not have a long data type (as of this writing), so instead the
   get() call returns a JavaScript Object wrapping the Ptolemy II
   LongToken object. You can then invoke methods on that token,
   such as getClass(), as done above.</p>
   <p>
   When sending tokens using send(), you can explicitly prevent any conversions
   from occurring by creating a Ptolemy II token explicitly and sending that.
   For example, the JavaScript nested array [[1, 2], [3, 4]] will be automatically
   converted into a Ptolemy II array of arrays {{1,2}, {3,4}}. If instead you want
   to send a Ptolemy II integer matrix [1,2;3,4], you can do this:</p>
   <pre>
   var IntMatrixToken = Java.type('ptolemy.data.IntMatrixToken');
   exports.fire = function() {
      var token = new IntMatrixToken([[1, 2], [3, 4]]);
      send(token, output);
   }
   </pre>
   <p>
   Scripts can instantiate Java classes and invoke methods on them.
   For example, the following script will build a simple Ptolemy II model
   and execute it each time this JavaScript actor fires.
   <pre>
   var Ramp = Java.type('ptolemy.actor.lib.Ramp');
   var FileWriter = Java.type('ptolemy.actor.lib.FileWriter');
   var SDFDirector = Java.type('ptolemy.domains.sdf.kernel.SDFDirector');
   var TypedCompositeActor = Java.type('ptolemy.actor.TypedCompositeActor');
   var Manager = Java.type('ptolemy.actor.Manager');

   var toplevel = new TypedCompositeActor();
   var ramp = new Ramp(toplevel, "ramp");
   var writer = new FileWriter(toplevel, "writer");

   toplevel.connect(ramp.output, writer.input);

   var director = new SDFDirector(toplevel, "SDFDirector");
   director.getAttribute('iterations').setExpression("10");
   var manager = new Manager();
   toplevel.setManager(manager);

   exports.fire = function() {
      manager.execute();
   }
   </pre>
   <p>You can even send this model out on an output port.
   For example,</p>
   <pre>
   exports.fire = function() {
      send(toplevel, output);
   }
   </pre>
   where "output" is the name of the output port. Note that the manager
   does not get included with the model, so the recipient will need to
   associate a new manager to be able to execute the model.
   <p>
   Subclasses of this actor may put it in "restricted" mode, which
   limits the functionality as follows:</p>
   <ul>
   <li> The "actor" variable (referring to this instance of the actor) provides limited capabilities.</li>
   <li> The localHostAddress() function throws an error.</li>
   <li> The readURL and httpRequest function only support the HTTP protocol (in particular,
   they do not support the "file" protocol, and hence cannot access local files).</li>
   </ul>
   <p>
   FIXME: document console package, Listen to actor, util package.</p>

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class JavaScript extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>error</i> port and the <i>script</i> port parameter.
     *  Initialize <i>script</i> to a block of JavaScript.
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

        // Initialize in alphabetical order.

        // Create an error port.
        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);
        StringAttribute cardinal = new StringAttribute(error, "_cardinal");
        cardinal.setExpression("SOUTH");
        SingletonParameter showName = new SingletonParameter(error, "_showName");
        showName.setExpression("true");

        // Create the script parameter and input port.
        script = new PortParameter(this, "script");
        script.setStringMode(true);
        ParameterPort scriptIn = script.getPort();
        cardinal = new StringAttribute(scriptIn, "_cardinal");
        cardinal.setExpression("SOUTH");
        showName = new SingletonParameter(scriptIn, "_showName");
        showName.setExpression("true");

        // initialize the script to provide an empty template:
        script.setExpression("// Put your JavaScript program here.\n"
                + "// Add ports and parameters.\n"
                + "// Define JavaScript functions initialize(), fire(), and/or wrapup().\n"
                + "// Refer to parameters in scope using dollar-sign{parameterName}.\n"
                + "// In the fire() function, use get(parameterName, channel) to read inputs.\n"
                + "// Send to output ports using send(value, portName, channel).\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output port on which to produce a message when an error occurs
     *  when executing this actor. Note that if nothing is
     *  connected to this port, then an error will cause this
     *  JavaScript actor to throw an exception. Otherwise, a
     *  description of the error will be produced on this port and the
     *  actor will continue executing. Note that any errors that occur
     *  during loading the script or invoking the initialize() or
     *  wrapup() functions always result in an exception, since it
     *  makes no sense to produce an output during those phases of
     *  execution.
     */
    public TypedIOPort error;

    /** The script defining the behavior of this actor. */
    public PortParameter script;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an input handler to be invoked when there is a new token on
     *  any input port.
     *  @param function The function to handle the token.
     *  @return The incremented input handler index.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #removeInputHandler(Integer)
     */
    public int addInputHandler(final Runnable function)
            throws IllegalActionException {
        if (_genericInputHandlers == null) {
            _genericInputHandlers = new LinkedList<Runnable>();
        }
        _genericInputHandlers.add(function);
        _handle++;
        _handleToIndex.put(_handle, _genericInputHandlers.size() - 1);
        return _handle;
    }

    /** React to a change in an attribute, and if the attribute is the
     *  script parameter, and the script parameter possibly contains a
     *  'setup' function, then evaluate that function. This means that as
     *  soon as this script parameter is set, any ports that are created
     *  in setup will appear. Note that the JavaScript engine that evaluates
     *  setup is discarded, and a new one is created when the model executes.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If evaluating the script fails.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == script) {
            try {
                _createEngineAndEvaluateSetup();
            } catch (Exception e) {
                // Turn exceptions into warnings.
                // Otherwise, Vergil may refuse to instantiate the actor
                // and novice users will loose data.
                try {
                    // FIXME: For some reason, this gets invoked twice!
                    MessageHandler.warning("Failed to evaluate script.", e);
                } catch (CancelException e1) {
                    throw new IllegalActionException(this, e, "Cancelled");
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Specify author information to appear in the documentation for this actor.
     *  @param author Author information to appear in documentation.
     */
    public void author(String author) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"author\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(author));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** Clear the timeout or interval with the specified handle, if it
     *  has not already executed.
     *  @param handle The timeout handle.
     *  @see #setTimeout(Runnable, int)
     *  @see #setInterval(Runnable, int)
     */
    public synchronized void clearTimeout(Integer handle) {
        // NOTE: The handle for this timeout remains in the
        // _pendingTimeoutIDs map, but it is more efficient to remove
        // it from that map when the firing occurs.
        _pendingTimeoutFunctions.remove(handle);
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JavaScript newObject = (JavaScript) super.clone(workspace);
        ScriptEngineManager factory = new ScriptEngineManager();
        newObject._engine = factory.getEngineByName("nashorn");
        if (newObject._engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new CloneNotSupportedException(
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }
        newObject._exports = null;
        newObject._inputTokens = new HashMap<IOPort, HashMap<Integer, Token>>();
        newObject._outputTokens = null;
        newObject._pendingTimeoutFunctions = null;
        newObject._pendingTimeoutIDs = null;
        newObject._proxies = null;
        newObject._proxiesByName = null;
        newObject._genericInputHandlers = null;
        newObject._handleToIndex = new HashMap<Integer, Integer>();
        newObject._handleToProxy = new HashMap<Integer, PortOrParameterProxy>();
        return newObject;
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The assume format for documentation is HTML.
     *  @param description A description to appear in documentation.
     */
    public void description(String description) {
        description(description, null);
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The recommended format for documentation is HTML, Markdown, or plain text.
     *  @param description A description to appear in documentation.
     *  @param type The type, which should be one of "text/html" (the default if null
     *   is given), "text/markdown",  or "text/plain".
     */
    public void description(String description, String type) {
        // FIXME: The type is currently ignored.
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"description\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(description));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** If the model is executing and the error port is connected, then send the
     *  message to the error port; otherwise, use the MessageHandler to display the
     *  error. Note that this should not be used for fatal errors, because it returns.
     *  For fatal errors, a script should throw an exception.
     *  In addition, if debugging is turned on, then send the specified message to the
     *  _debug() method, and otherwise send it out to stderr.
     *  @param message The message
     */
    public void error(String message) {
        if (_debugging) {
            _debug(message);
        }
        try {
            // FIXME: Might not be in a phase where anyone will receive this
            // error message. It might be after the last firing, and just before
            // wrapup() sets _executing = false. In this case, the error message
            // will never appear anywhere!
            if (_executing && error.getWidth() > 0) {
                error.send(0, new StringToken(message));
                return;
            }
        } catch (Throwable e) {
            // Sending to the error port fails.
            // Revert to directly reporting.
        }
        MessageHandler.error(getName() + ": " + message);
    }

    /** Produce any pending outputs specified by send() since the last firing,
     *  invoke any timer tasks that match the current time, and invoke the
     *  fire function. Specifically:
     *  <ol>
     *
     *  <li> First, if there is a new token on the script input port,
     *  then evaluate the script specified on that port. Any
     *  previously defined methods such as fire() will be replaced if
     *  the new script has a replacement, and preserved otherwise.  If
     *  the new script has an initialize() method, that method will
     *  not be invoked until the next time this actor is
     *  initialized.</li>
     *
     *  <li> Next, send any outputs that have been queued to be sent
     *  by calling send() from outside any firing of this JavaScript
     *  actor.</li>
     *
     *  <li> Next, read all available inputs, recording their values
     *  for subsequent calls to get().</li>
     *
     *  <li> Next, invoke any pending timer tasks whose timing matches
     *  the current time.</li>
     *
     *  <li> After updating all the inputs, for each input port that
     *  had a new token on any channel and for which there is a
     *  handler function bound to that port via the addInputHandler()
     *  method, invoke that function.  Such a function will be invoked
     *  in the following order: First, invoke the functions for each
     *  PortParameter, in the order in which the PortParameters were
     *  created.  Then invoke the functions for the ordinary input
     *  ports.</li>
     *
     *  <li> Next, if the current script has a fire() function, then
     *  invoke it.</li>
     *  </ol>
     *
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_debugging) {
            // Set a global variable for debugging.
            _engine.put("_debug", true);
        } else {
            _engine.put("_debug", false);
        }

        // If there is an input script, evaluate that script.
        // Note that if this redefines the initialize() method, it will
        // have no effect until the next run unless the script actually invokes it.
        ParameterPort scriptPort = script.getPort();
        if (scriptPort.getWidth() > 0 && scriptPort.hasToken(0)) {
            script.update();
            String scriptValue = ((StringToken) script.getToken())
                    .stringValue();
            try {
                _engine.eval(scriptValue);
                _exports = _engine.eval("exports");
            } catch (Throwable throwable) {
                if (error.getWidth() > 0) {
                    error.send(0, new StringToken(throwable.getMessage()));
                } else {
                    throw new IllegalActionException(this, throwable,
                            "Loading input script triggers an exception.");
                }
            }
            if (_debugging) {
                _debug("Evaluated new script on input port: " + scriptValue);
            }
        }

        // Send out buffered outputs, if there are any.
        // This has to be synchronized in case a callback calls send() at the
        // same time.
        synchronized (this) {
            if (_outputTokens != null) {
                for (IOPort port : _outputTokens.keySet()) {
                    HashMap<Integer, List<Token>> tokens = _outputTokens
                            .get(port);
                    for (Map.Entry<Integer, List<Token>> entry : tokens
                            .entrySet()) {
                        List<Token> queue = entry.getValue();
                        if (queue != null) {
                            for (Token token : queue) {
                                port.send(entry.getKey(), token);
                                if (_debugging) {
                                    _debug("Sent to output port "
                                            + port.getName() + " the value "
                                            + token);
                                }
                            }
                        }
                    }
                }
                _outputTokens.clear();
            }
        }

        // Update port parameters.
        boolean foundNewInput = false;
        for (PortParameter portParameter : attributeList(PortParameter.class)) {
            if (portParameter == script) {
                continue;
            }
            PortOrParameterProxy proxy = _proxies.get(portParameter.getPort());
            // FIXME: This could be null if the port has been deleted, but not the parameter.
            if (portParameter.update()) {
                proxy._hasNewInput(true);
                foundNewInput = true;
                if (_debugging) {
                    _debug("Received new input on " + portParameter.getName()
                            + " with value " + portParameter.getToken());
                }
            } else if (proxy._localInputTokens != null
                    && proxy._localInputTokens.size() > 0) {
                // There is no new input, but there is a locally provided one.
                portParameter
                        .setCurrentValue(proxy._localInputTokens.remove(0));
                proxy._hasNewInput(true);
                foundNewInput = true;
            } else {
                proxy._hasNewInput(false);
            }
        }

        // Read all the available inputs.
        _inputTokens.clear();
        for (IOPort input : inputPortList()) {
            // Skip the scriptIn input.
            if (input == script.getPort()) {
                continue;
            }
            // Skip ParameterPorts, as those are handled by the update() call above.
            if (input instanceof ParameterPort) {
                continue;
            }
            HashMap<Integer, Token> tokens = new HashMap<Integer, Token>();
            boolean hasInput = false;
            PortOrParameterProxy proxy = _proxies.get(input);
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    hasInput = true;
                    tokens.put(i, input.get(i));
                    if (_debugging) {
                        _debug("Received new input on " + input.getName()
                                + " with value " + tokens.get(i));
                    }
                } else {
                    // There is no external input, but there might be one
                    // sent by the token to itself.
                    if (proxy._localInputTokens != null
                            && proxy._localInputTokens.size() > 0) {
                        tokens.put(i, proxy._localInputTokens.remove(0));
                        hasInput = true;
                    }
                }
            }
            // Even if the input width is zero, there might be a token
            // that the actor has sent to itself.
            if (input.getWidth() == 0 && proxy._localInputTokens != null
                    && proxy._localInputTokens.size() > 0) {
                tokens.put(0, proxy._localInputTokens.remove(0));
                hasInput = true;
            }
            _inputTokens.put(input, tokens);
            if (hasInput) {
                proxy._hasNewInput(true);
                foundNewInput = true;
            } else {
                proxy._hasNewInput(false);
            }
        }
        // Invoke any timeout callbacks whose timeout matches current time,
        // followed by specific input handlers, followed by generic input handlers,
        // followed by the fire function.
        // Synchronize to ensure that this function invocation is atomic
        // w.r.t. to any callbacks.
        synchronized (this) {
            // Mark that we are in the fire() method, enabling outputs to be
            // sent immediately.
            _inFire = true;
            try {
                // Handle timeout requests that match the current time.
                if (_pendingTimeoutIDs != null) {
                    // If current time matches pending timeout requests, invoke them.
                    Time currentTime = getDirector().getModelTime();
                    List<Integer> ids = _pendingTimeoutIDs.get(currentTime);
                    if (ids != null) {
                        // Copy the list, because setInterval reschedules a firing
                        // and will cause a concurrent modification exception otherwise.
                        List<Integer> copy = new LinkedList<Integer>(ids);
                        if (ids != null) {
                            for (Integer id : copy) {
                                Runnable function = _pendingTimeoutFunctions
                                        .get(id);
                                if (function != null) {
                                    // Remove the id before firing the function because it may
                                    // reschedule itself using the same id.
                                    // FIXME: No, that's not right. Function may or may not
                                    // cancel a setInterval, and if we remove the id here, we
                                    // have no way to tell whether it cancelled it.
                                    // _pendingTimeoutFunctions.remove(id);
                                    function.run();
                                    if (_debugging) {
                                        _debug("Invoked timeout function.");
                                    }
                                }
                            }
                            _pendingTimeoutIDs.remove(currentTime);
                        }
                    }
                }

                // Invoke input handlers.
                for (IOPort input : inputPortList()) {
                    // Skip the scriptIn input
                    if (input == script.getPort()) {
                        continue;
                    }
                    try {
                        _proxies.get(input).invokeHandlers();
                    } catch (Throwable throwable) {
                        // localFunctions.js has an undefined reference, then catch it here.
                        throw new IllegalActionException(this, throwable,
                                "Failed to invoke a handler on the input \""
                                + input.getName() + "\".");
                    }
                }

                // Invoke generic input handlers.
                if (foundNewInput && _genericInputHandlers != null
                        && _genericInputHandlers.size() > 0) {
                    for (Runnable function : _genericInputHandlers) {
                        if (function != null) {
                            function.run();
                            if (_debugging) {
                                _debug("Invoked generic input handler function.");
                            }
                        }
                    }
                }

                // Invoke the fire function.
                _invokeMethodInContext("fire");
                if (_debugging) {
                    _debug("Invoked fire function.");
                }
            } finally {
                _inFire = false;
            }
        }
    }

    /** If this actor has been initialized, return the JavaScript engine,
     *  otherwise return null.
     *  @return The JavaScript engine for this actor.
     */
    public ScriptEngine getEngine() {
        return _engine;
    }

    /** Get the proxy for a port or parameter with the specified name.
     *  This is an object on which JavaScript can directly invoke methods.
     *  @param name The name of the port or parameter.
     *  @return The proxy for the specified name, or null if there is none.
     */
    public PortOrParameterProxy getPortOrParameterProxy(String name) {
        if (_proxiesByName != null) {
            return _proxiesByName.get(name);
        }
        return null;
    }

    /** Create a new JavaScript engine, load the default functions, and
     *  register the ports so that send() and get() can work.
     *  @exception IllegalActionException If a port name is either a
     *   a JavaScript keyword or not a valid identifier, if loading the
     *   default JavaScript files fails, or if the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Create proxy for ports that don't already have one.
        for (TypedIOPort port : portList()) {
            // Do not convert the script or error ports to a JavaScript variable.
            if (port == script.getPort() || port == error) {
                continue;
            }
            // Do not expose ports that are already exposed.
            if (_proxies.get(port) != null) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(port);
            _proxies.put(port, proxy);
            _proxiesByName.put(port.getName(), proxy);
        }

        // Create proxy for parameters that don't already have one.
        List<Variable> attributes = attributeList(Variable.class);
        for (Variable parameter : attributes) {
            // Do not convert the script parameter to a JavaScript variable.
            if (parameter == script) {
                continue;
            }
            // Do not expose parameters that are already exposed.
            if (_proxies.get(parameter) != null) {
                continue;
            }
            // Do not create a proxy for a PortParameter. There is already
            // a proxy for the port.
            if (parameter instanceof PortParameter) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }

        // Invoke the initialize function.
        // Synchronize to ensure that this is atomic w.r.t. any callbacks.
        // Note that the callbacks might be invoked after a model has terminated
        // execution.
        synchronized (this) {
            // Clear any queued output tokens.
            if (_outputTokens != null) {
                _outputTokens.clear();
            }
            _invokeMethodInContext("initialize");
        }
        _running = true;
    }

    /** Create a new input port if it does not already exist.
     *  This port will have an undeclared type and no description.
     *  @param name The name of the port.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void input(String name) throws IllegalActionException,
            NameDuplicationException {
        input(name, null);
    }

    /** Create a new input port if it does not already exist.
     *  The options argument can specify a "type", a "description",
     *  and/or a "value".
     *  If a type is given, set the type as specified. Otherwise,
     *  leave the type unspecified so that it will be inferred.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  If a value is given, then create a PortParameter instead of
     *  an ordinary port and set its default value.
     *  If a Parameter already exists with the same name, then convert
     *  it to a PortParameter and preserve its value.
     *  @param name The name of the port.
     *  @param options The options, or null to accept the defaults.
     *   To give options, this argument must implement the Map interface.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void input(String name, Map options)
            throws IllegalActionException, NameDuplicationException {
        // FIXME: Should check whether the model is running and use a change
        // request if so.
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create an input.");
        }
        TypedIOPort port = (TypedIOPort) getPort(name);
        PortParameter parameter = null;
        Object token = null;
        Token previousValue = null;
        if (port == null) {
            Attribute previous = getAttribute(name);
            if (previous instanceof Parameter) {
                previousValue = ((Parameter)previous).getToken();
                // Treat an empty string as no value.
                if (previousValue instanceof StringToken
                        && ((StringToken)previousValue).stringValue().trim().equals("")) {
                    previousValue = null;
                }
                previous.setContainer(null);
            }
            if (!(options instanceof Map)) {
                // No options given. Use defaults.
                if (previousValue == null) {
                    // No previous value, so just create an ordinary port.
                    port = (TypedIOPort) newPort(name);
                } else {
                    // There is a previous value. Create a PortParameter to
                    // preserve its value.
                    parameter = new PortParameter(this, name);
                    port = parameter.getPort();
                }
            } else {
                Object value = ((Map) options).get("value");
                if (value == null && previousValue == null) {
                    // No value. Use an ordinary port.
                    port = (TypedIOPort) newPort(name);
                } else {
                    parameter = new PortParameter(this, name);
                    // Convert value to a Ptolemy Token.
                    try {
                        token = ((Invocable) _engine).invokeFunction(
                                "convertToToken", value);
                    } catch (Exception e) {
                        throw new IllegalActionException(this, e,
                                "Cannot convert value to a Ptolemy Token: "
                                        + value);
                    }
                    if (!(token instanceof Token)) {
                        throw new IllegalActionException(this,
                                "Unsupported value: " + value);
                    }
                    port = parameter.getPort();
                }
            }
        } else {
            if (port == script.getPort() || port == error) {
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            }
            if (port instanceof ParameterPort) {
                parameter = ((ParameterPort) port).getParameter();
            }
        }
        if (options instanceof Map) {
            Object type = ((Map) options).get("type");
            if (type instanceof String) {
                // The following will put the parameter in string mode,
                // if appropriate.
                Type ptType = _typeAccessorToPtolemy((String) type, port);
                port.setTypeEquals(ptType);
                _setOptionsForSelect(port, options);
                if (parameter != null) {
                    parameter.setTypeEquals(ptType);
                }
            } else if (type != null) {
                throw new IllegalActionException(this, "Unsupported type: "
                        + type);
            }
            Object description = ((Map) options).get("description");
            if (description != null) {
                _setPortDescription(port, description.toString());
            }
        }
        // Make sure to do this after setting the type to catch
        // string mode and type checks.
        if (parameter != null) {
            // If the parameter already has a value, allow that to prevail.
            if (token != null 
                    && (parameter.getToken() == null
                    || (parameter.isStringMode() && parameter.getExpression().equals("")))) {
                parameter.setToken((Token) token);
            }
            // If there was a previous value, then override the
            // specified value.
            if (previousValue != null) {
                if (parameter.isStringMode() && previousValue instanceof StringToken) {
                    parameter.setExpression(((StringToken)previousValue).stringValue());
                } else {
                    parameter.setExpression(previousValue.toString());
                }
            }
        }
        port.setInput(true);
    }

    /** Return true if the model is executing (between initialize() and
     *  wrapup(), including initialize() but not wrapup()).
     *  This is (more or less) the phase of execution during which
     *  it is OK to send outputs. Note that if an asynchronous callback
     *  calls send() when this returns true, the output may still not
     *  actually reach its destination. It is possible that the last
     *  firing has already occurred, but wrapup() has not yet been called.
     *  @return true if the model is executing.
     */
    public boolean isExecuting() {
        return _executing;
    }

    /** Return true if the specified string is a JavaScript keyword.
     *  @param identifier The identifier name.
     *  @return True if it is a JavaScript keyword.
     */
    public static boolean isJavaScriptKeyword(String identifier) {
        return _KEYWORDS.contains(identifier);
    }

    /** Return true if this actor is restricted.
     *  A restricted instance of this actor limits the capabilities available
     *  to the script it executes so that it can execute untrusted code.
     *  This base class is not restricted, but subclasses may be.
     *  @return True if this actor is restricted.
     */
    public boolean isRestricted() {
        return _restricted;
    }

    /** Return true if the specified string is not a JavaScript keyword
     *  and is a valid JavaScript identifier.
     *  @param identifier The proposed name.
     *  @return True if it is a valid identifier name.
     */
    public static boolean isValidIdentifier(String identifier) {
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
        return true;
    }

    /** Return the local host IP address as a string.
     *  @return A string representation of the local host address.
     *  @exception UnknownHostException If the local host is not known.
     *  @exception SecurityException If this actor is in restricted mode.
     */
    public String localHostAddress() throws UnknownHostException,
            SecurityException {
        if (_restricted) {
            throw new SecurityException(
                    "Actor is restricted. Cannot invoke localHostAddress().");
        }
        return InetAddress.getLocalHost().getHostAddress();
    }

    /** If debugging is turned on, then send the specified message to the
     *  _debug() method, and otherwise send it out to stdout.
     *  @param message The message
     */
    public void log(String message) {
        if (_debugging) {
            _debug(message);
        } else {
            System.out.println(getName() + ": " + message);
        }
    }

    /** Create a new output port if it does not already exist.
     *  Set the type to general.
     *  @param name The name of the port.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void output(String name) throws IllegalActionException,
            NameDuplicationException {
        output(name, null);
    }

    /** Create a new output port if it does not already exist.
     *  The options argument can specify a "type" and/or a "description".
     *  If a type is given, set the type as specified. Otherwise,
     *  set the type to general.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  @param name The name of the port.
     *  @param options The options, or null to accept the defaults.
     *   To give options, this argument must implement the Map interface.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void output(String name, Map options)
            throws IllegalActionException, NameDuplicationException {
        // FIXME: Should check whether the model is running a use a change
        // request if so.
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create an output.");
        }
        TypedIOPort port = (TypedIOPort) getPort(name);
        if (port == null) {
            port = (TypedIOPort) newPort(name);
        } else {
            if (port == script.getPort()) {
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            }
        }
        if (options != null) {
            Object type = ((Map) options).get("type");
            if (type instanceof String) {
                port.setTypeEquals(_typeAccessorToPtolemy((String) type, port));
                _setOptionsForSelect(port, options);
            } else {
                if (type == null) {
                    port.setTypeEquals(BaseType.GENERAL);
                } else {
                    throw new IllegalActionException(this, "Unsupported type: "
                            + type);
                }
            }
            Object description = ((Map) options).get("description");
            if (description != null) {
                _setPortDescription(port, description.toString());
            }
        } else {
            port.setTypeEquals(BaseType.GENERAL);
        }
        port.setOutput(true);
    }

    /** Create a new parameter if it does not already exist.
     *  This parameter will have an undeclared type and no description.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If no name is given, or if the
     *   model is executing.
     *  @exception NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public void parameter(String name) throws IllegalActionException,
            NameDuplicationException {
        parameter(name, null);
    }

    /** Create a new parameter if it does not already exist.
     *  The options argument can specify a "type", a "description",
     *  and/or a "value".
     *  If a type is given, set the type as specified. Otherwise,
     *  leave the type unspecified so that it will be inferred from the value.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  If a value is given, then set the default value of the parameter
     *  if it does not already have a value.
     *  @param name The name of the parameter.
     *  @param options The options, or null to accept the defaults.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public void parameter(String name, Map options)
            throws IllegalActionException, NameDuplicationException {
        /* Don't constrain when this executes.
         * FIXME: Instead, we should use a ChangeRequest if we are executing.
        if (_executing) {
            throw new IllegalActionException(this,
                    "Cannot create a parameter while the model is executing. Stop the model.");
        }
         */
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create a parameter.");
        }
        Attribute parameter = getAttribute(name);
        if (parameter == null) {
            parameter = new Parameter(this, name);
        } else {
            if (parameter == script) {
                // FIXME: Should instead do a name transformation?
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            } else if (!(parameter instanceof Parameter)) {
                throw new NameDuplicationException(this, "Name is taken: "
                        + name);
            }
        }
        if (options != null) {
            // Find the type, if it is specified.
            Type ptType = null;
            Object type = options.get("type");
            if (type instanceof String) {
                ptType = _typeAccessorToPtolemy((String) type, parameter);
            } else if (type != null) {
                throw new IllegalActionException(this, "Unsupported type: "
                        + type);
            }
            if (ptType != null) {
                ((Parameter) parameter).setTypeEquals(ptType);
                _setOptionsForSelect(parameter, options);

            }
            Token previousValue = ((Parameter) parameter).getToken();
            if (previousValue == null
                    || ((ptType == BaseType.STRING)
                            && ((Parameter) parameter).isStringMode()
                            && (((StringToken) previousValue).stringValue().equals("")))) {
                // The parameter does not already have a value. Set the default.
                Object value = options.get("value");
                if (value != null) {
                    // Convert value to a Ptolemy Token.
                    Object token;
                    try {
                        token = ((Invocable) _engine).invokeFunction(
                                "convertToToken", value);
                    } catch (Exception e) {
                        throw new IllegalActionException(this, e,
                                "Cannot convert value to a Ptolemy Token: "
                                        + value);
                    }
                    if (token instanceof Token) {
                        ((Parameter) parameter).setToken((Token) token);
                    } else {
                        throw new IllegalActionException(this,
                                "Unsupported value: " + value);
                    }
                }
            }

            Object description = options.get("description");
            if (description != null) {
                _setPortDescription(parameter, description.toString());
            }
        }
        // Parameters may be immediately referenced in the setup function, so we
        // need to create a proxy for them now.
        if (_proxies.get(parameter) == null) {
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }
    }

    /** If there are any pending self-produced inputs, then request a firing
     *  at the current time.
     *  @exception IllegalActionException If the superclass throws it or the
     *   refiring request fails.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        for (IOPort input : inputPortList()) {
            // Skip the scriptIn input.
            if (input == script.getPort()) {
                continue;
            }
            PortOrParameterProxy proxy = _proxies.get(input);
            if (proxy._localInputTokens != null
                    && proxy._localInputTokens.size() > 0) {
                _fireAtCurrentTime();
                break;
            }
        }
        return super.postfire();
    }

    /** Create a new JavaScript engine, load the default functions,
     *  and evaluate the script parameter.
     *  @exception IllegalActionException If a port name is either a
     *   a JavaScript keyword or not a valid identifier, if loading the
     *   default JavaScript files fails, or if the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        synchronized (this) {
            _pendingTimeoutFunctions = null;
            _pendingTimeoutIDs = null;
            _timeoutCount = 0;

            _createEngineAndEvaluateSetup();

            // Clear any queued output tokens.
            if (_outputTokens != null) {
                _outputTokens.clear();
            }
        }
        _executing = true;
        _running = false;
    }

    /** Utility method to read a string from an input stream.
     *  @param stream The stream.
     *  @return The string.
     * @exception IOException If the stream cannot be read.
     */
    public static String readFromInputStream(InputStream stream)
            throws IOException {
        StringBuffer response = new StringBuffer();
        BufferedReader reader = null;
        String line = "";
        // Avoid Coverity Scan: "Dubious method used (FB.DM_DEFAULT_ENCODING)"
        reader = new BufferedReader(new InputStreamReader(stream,
                java.nio.charset.Charset.defaultCharset()));

        String lineBreak = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            response.append(line);
            if (!line.endsWith(lineBreak)) {
                response.append(lineBreak);
            }
        }
        reader.close();
        return response.toString();
    }

    /** Remove the input handler with the specified handle, if it exists.
     *  @param handle The handler handle.
     *  @see #addInputHandler(Runnable)
     */
    public void removeInputHandler(Integer handle) {
        if (handle != null && _handleToIndex != null) {
            Integer index = _handleToIndex.get(handle);
            if (index != null) {
                int n = index.intValue();
                // If there is a proxy recorded, use that.
                PortOrParameterProxy proxy = _handleToProxy.get(handle);
                if (proxy == null) {
                    // No proxy, so this is a generic input handler.
                    if (_genericInputHandlers != null
                            && n < _genericInputHandlers.size()) {
                        _genericInputHandlers.remove(n);
                    }
                    _handleToIndex.remove(handle);
                } else {
                    // Delegate to the proxy.
                    proxy.removeInputHandler(handle);
                }
            }
        }
    }

    /** Invoke the specified function after the specified amount of time and again
     *  at multiples of that time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     */
    public int setInterval(final Runnable function, final int milliseconds)
            throws IllegalActionException {
        final Integer id = Integer.valueOf(_timeoutCount++);
        // Create a new function that invokes the specified function and then reschedules
        // itself.
        final Runnable reschedulingFunction = new Runnable() {
            @Override
            public void run() {
                _runThenReschedule(function, milliseconds, id);
            }
        };
        _setTimeout(reschedulingFunction, milliseconds, id);
        return id;
    }

    /** Invoke the specified function after the specified amount of time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     */
    public int setTimeout(final Runnable function, int milliseconds)
            throws IllegalActionException {
        final Integer id = Integer.valueOf(_timeoutCount++);
        _setTimeout(function, milliseconds, id);
        return id;
    }

    /** Specify version information to appear in the documentation for this actor.
     *  @param version Version information to appear in documentation.
     */
    public void version(String version) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"version\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(version));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** Execute the wrapup function, if it is defined, and exit the context for this thread.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _executing = false;
        _running = false;
        // Synchronize so that this invocation is atomic w.r.t. any callbacks.
        synchronized (this) {
            // Invoke the wrapup function.
            _invokeMethodInContext("wrapup");
            // If there are unsent output tokens, the issue warning messages.
            // These would result from a call to send() that occurred in an asynchronous
            // callback after the last firing of this actor but before wrapup().
            if (_outputTokens != null) {
                for (IOPort port : _outputTokens.keySet()) {
                    HashMap<Integer, List<Token>> tokens = _outputTokens
                            .get(port);
                    for (Map.Entry<Integer, List<Token>> entry : tokens
                            .entrySet()) {
                        List<Token> queue = entry.getValue();
                        if (queue != null) {
                            for (Token token : queue) {
                                String message = "WARNING: "
                                        + getName()
                                        + ": Unfulfilled send to output port \""
                                        + port.getName() + "\" with the value "
                                        + token;
                                if (_debugging) {
                                    _debug(message);
                                } else {
                                    System.err.println(message);
                                }
                            }
                        }
                    }
                }
                _outputTokens.clear();
            }
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class so that the name of any port added is shown.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
    NameDuplicationException {
        super._addPort(port);
        SingletonParameter showName = new SingletonParameter(port, "_showName");
        showName.setExpression("true");
    }

    /** Check the validity of a name. This implementation throws an exception
     *  if either the name is not a valid JavaScript identifier or it is a
     *  JavaScript keyword.
     *  @param name The name to check.
     *  @exception IllegalActionException If the name is either not a valid
     *   identifier or is a keyword.
     */
    protected void _checkValidity(String name) throws IllegalActionException {
        if (!isValidIdentifier(name)) {
            throw new IllegalActionException(this,
                    "Port name is not a valid JavaScript identifier: " + name);
        }
        if (isJavaScriptKeyword(name)) {
            try {
                MessageHandler.warning("Port name is a JavaScript keyword: "
                        + name);
            } catch (CancelException e) {
                throw new IllegalActionException(this, "Cancelled.");
            }
        }
    }

    /** Return null, because the default type constraints, where output
     *  types are greater than or equal to all input types, make no sense
     *  for this actor. Output types need to be set explicitly or inferred
     *  from backward type inference, and input types need to be set explicitly
     *  or inferred from forward type inference.
     *  @return Null.
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /** Invoke the specified method in the context of the exports object.
     *  If there is no such method in that context, attempt to invoke the
     *  method in the top-level context.
     *  @param methodName The method name.
     *  @exception IllegalActionException If the method does not exist in either
     *   context, or if an error occurs invoking the method.
     */
    protected void _invokeMethodInContext(String methodName)
            throws IllegalActionException {
        try {
            ((Invocable) _engine).invokeMethod(_exports, methodName);
        } catch (NoSuchMethodException e) {
            // Attempt to invoke it in the top-level contenxt.
            try {
                ((Invocable) _engine).invokeFunction(methodName);
            } catch (NoSuchMethodException e1) {
                throw new IllegalActionException(this, e1,
                        "No function defined named " + methodName);
            } catch (ScriptException e1) {
                if (error.getWidth() > 0) {
                    error.send(0, new StringToken(e1.getMessage()));
                } else {
                    throw new IllegalActionException(this, e1,
                            "Failure executing the " + methodName
                                    + " function: " + e1.getMessage());
                }
            }
        } catch (ScriptException e) {
            if (error.getWidth() > 0) {
                error.send(0, new StringToken(e.getMessage()));
            } else {
                throw new IllegalActionException(this, e,
                        "Failure executing the " + methodName + " function: "
                                + e.getMessage());
            }
        }
    }

    /** Set the description of a port or parameter.
     *  @param portOrParameter The port.
     *  @param description The description.
     */
    protected void _setPortDescription(NamedObj portOrParameter,
            String description) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"");
        moml.append(portOrParameter.getName());
        moml.append("\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(description));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Empty argument list for JavaScript function invocation. */
    protected final static Object[] _EMPTY_ARGS = new Object[] {};

    /** JavaScript engine, shared among all instances of this class. */
    protected ScriptEngine _engine;

    /** True while the model is executing (between initialize() and
     *  wrapup(), including the initialize() but not wrapup().
     *  This is the phase of execution during which it is OK to
     *  send outputs.
     */
    protected boolean _executing;

    /** The exports object defined in the script that is evaluated. */
    protected Object _exports;

    /** JavaScript keywords. */
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
    protected static final Set<String> _KEYWORDS = new HashSet<String>(
            Arrays.asList(_JAVASCRIPT_KEYWORDS));

    /** If set to true in the constructor of a base class, then put
     *  this actor in "restricted" mode.  This limits the
     *  functionality as described in the class comment.
     */
    protected boolean _restricted = false;

    /** True while the model is running (past initialize() and before
     *  wrapup()).
     *  This is the phase of execution during which it is OK to
     *  get inputs.
     */
    protected boolean _running = false;

    ///////////////////////////////////////////////////////////////////
    ////                        Private Methods                    ////

    /** Create a script engine, evaluate basic function definitions,
     *  define the 'actor' variable, evaluate the script, and invoke the
     *  setup method if it exists.
     *  @exception IllegalActionException If an error occurs.
     */
    private void _createEngineAndEvaluateSetup() throws IllegalActionException {

        // Reset state so that there isn't spillover from previous scripts.
        _genericInputHandlers = null;
        _handleToIndex = new HashMap<Integer, Integer>();
        _handleToProxy = new HashMap<Integer, PortOrParameterProxy>();

        _handle = 0;

        // Create a script engine.
        // This is private to this class, so each instance of JavaScript
        // has its own completely isolated script engine.
        // If this later creates a performance problem, we may want to
        // provide an option to share a script engine. But then all accesses
        // to the engine will have to be synchronized to ensure that there
        // never is more than one thread trying to evaluate a script.
        // Note that an engine is recreated on each run. This ensures
        // that the JS context does not remember data from one run to the next.
        ScriptEngineManager factory = new ScriptEngineManager();
        // Create a Nashorn script engine
        _engine = factory.getEngineByName("nashorn");
        if (_engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new IllegalActionException(
                    this,
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }
        /* FIXME: The following should intercept errors, but if doesn't!
         * Perhaps Thread.setUncaughtExceptionHandler()? How to get the thread?
         * or Thread.setDefaultUncaughtExceptionHandler().
         * This should be a top-level Ptolemy II thing...
        ScriptContext context = _engine.getContext();
        context.setErrorWriter(new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                // FIXME: Request a firing that can then throw an exception so
                // error port is handled correctly.
                MessageHandler.error(toString());
            }
        });
         */
        String localFunctionsPath = "$CLASSPATH/ptolemy/actor/lib/jjs/localFunctions.js";
        try {
            if (_debugging) {
                _debug("** Instantiated engine. Loading local and basic functions.");
                // Set a global variable for debugging.
                _engine.put("_debug", true);
            } else {
                _engine.put("_debug", false);
            }
            _engine.eval(FileUtilities.openForReading(localFunctionsPath, null, null));
        } catch (Throwable throwable) {
            // eval() can throw a ClassNotFoundException if a Ptolemy class is not found.

            if (throwable instanceof ClassNotFoundException) {
                // FIXME: Temporary code (2015-08-15)
                
                // Attempting to debug why, after 348 tests, these tests
                // can't find the Ptolemy classes:

                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[348] ptolemy/demo/Robot/RandomWalkIntruder.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[350] ptolemy/demo/Robot/RobotChase.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[351] ptolemy/demo/Robot/RobotCollaborativeChase.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[353] ptolemy/demo/Robot/RobotMonitor.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[360] ptolemy/demo/Robot/SmartIntruder6Teams.xml

                String message =  "Parsing "
                    + localFunctionsPath
                    + " resulted in a ClassNotFoundException?"
                    + " Checking ClassLoaders: "
                    + " ClassLoader.getSystemClassLoader(): "
                    + ClassLoader.getSystemClassLoader()
                    + " Thread.currentThread().getContextClassLoader(): "
                    + Thread.currentThread().getContextClassLoader();
                Class clazz = null;
                try {
                    clazz = Class.forName("ptolemy.data.ArrayToken");
                } catch (ClassNotFoundException ex) {
                    throw new IllegalActionException(this, throwable, message
                            + "???? Failed to get ptolemy.data.ArrayToken?"
                            + ex);
                }
                throw new IllegalActionException(this, throwable, message
                        + " Class.forName(\"ptolemy.data.ArrayToken\"): "
                        + clazz);
                // FIXME: End of temporary code.
            } else {
                throw new IllegalActionException(this, throwable,
                        "Failed to load " + localFunctionsPath + ".");
            }
        }
        try {
            _engine.eval(FileUtilities.openForReading(
                    "$CLASSPATH/ptolemy/actor/lib/jjs/basicFunctions.js", null,
                    null));
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to load basicFunctions.js");
        }
        // Define the actor and accessor variables.
        if (!_restricted) {
            _engine.put("accessor", this);
            _engine.put("actor", this);
        } else {
            RestrictedJavaScriptInterface restrictedInterface = new RestrictedJavaScriptInterface(
                    this);
            _engine.put("accessor", restrictedInterface);
            _engine.put("actor", restrictedInterface);
        }

        // Create proxies for the port.
        // Note that additional proxies may need to be created in initialize(),
        // if they are created by a setup() function call.
        _proxies = new HashMap<NamedObj, PortOrParameterProxy>();
        _proxiesByName = new HashMap<String, PortOrParameterProxy>();
        for (TypedIOPort port : portList()) {
            // Do not convert the script or error ports to a JavaScript variable.
            if (port == script.getPort() || port == error) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(port);
            _proxies.put(port, proxy);
            _proxiesByName.put(port.getName(), proxy);
        }

        // Create proxies for parameters.
        // Note that additional parameters may need to be exposed in initialize(),
        // if they are created by a setup() function call.
        List<Variable> attributes = attributeList(Variable.class);
        for (Variable parameter : attributes) {
            // Do not convert the script parameter to a JavaScript variable.
            if (parameter == script) {
                continue;
            }
            // Do not expose parameters that are already exposed.
            if (_proxies.get(parameter) != null) {
                continue;
            }
            // Do not create a proxy for a PortParameter. There is already
            // a proxy for the port.
            if (parameter instanceof PortParameter) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }

        // Evaluate the script.
        String scriptValue = script.getValueAsString();
        try {
            _engine.eval(scriptValue);
            _exports = _engine.eval("exports");
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to evaluate script during initialize.");
        }

        // Invoke the setup function.
        // Synchronize to ensure that this is atomic w.r.t. any callbacks.
        // Note that the callbacks might be invoked after a model has terminated
        // execution.
        synchronized (this) {
            _invokeMethodInContext("setup");
        }
    }

    /** Fire me again at the current model time, one microstep later.
     *  Unlike calling the director's fireAtCurrentTime() method, this
     *  method is not affected by the current real time.
     *  @exception IllegalActionException If the director throws it.
     */
    private void _fireAtCurrentTime() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        // Note that it is not correct to call director.fireAtCurrentTime().
        // At least in DE, that method uses current _real_ time, not
        // model time.
        director.fireAt(this, currentTime);
    }

    /** If the second argument is true, mark the first argument
     *  as requiring its value to be JSON. The mark has the form
     *  of a (non-persistent) singleton parameter named "_JSON".
     *  
     *  @param typeable The object to mark.
     *  @param JSONmode Whether to mark it.
     *  @throws NameDuplicationException Not thrown.
     *  @throws IllegalActionException If adding the mark fails.
     */
    private void _markJSONmode(NamedObj typeable, boolean JSONmode)
            throws NameDuplicationException, IllegalActionException {
        if (JSONmode) {
            // Put in a hint that the string value needs to be JSON.
            SingletonAttribute mark = new SingletonAttribute(typeable, "_JSON");
            mark.setPersistent(false);
            if (typeable instanceof PortParameter) {
                _markJSONmode(((PortParameter)typeable).getPort(), true);
            }
        }
    }

    /** Invoke the specified function, then schedule another call to this
     *  same method after the specified number of milliseconds, using the specified
     *  id for the timeout function.
     *  @param function The function to repeatedly invoke.
     *  @param milliseconds The number of milliseconds in a period.
     *  @param id The id to use for the timeout function.
     */
    private synchronized void _runThenReschedule(final Runnable function,
            final int milliseconds, final Integer id) {
        function.run();
        if (_pendingTimeoutFunctions.get(id) == null) {
            // The callback function itself may cancel the interval.
            return;
        }
        final Runnable reschedulingFunction = new Runnable() {
            @Override
            public void run() {
                _runThenReschedule(function, milliseconds, id);
            }
        };
        try {
            _setTimeout(reschedulingFunction, milliseconds, id);
        } catch (IllegalActionException e) {
            // This should not have happened here. Should have been caught
            // the first time. But just in case...
            throw new InternalErrorException(this, e,
                    "Failed to reschedule function scheduled with setInterval().");
        }
    }

    /** If the options argument inlucdes an "options" field, then use that field
     *  to add choices to the parameter given by the first argument. The value of
     *  options field should be an array of strings.
     *  @param typeable A Parameter or a ParameterPort. Other types are ignored.
     *  @param options The options argument for an input or parameter specification.
     */
    private void _setOptionsForSelect(NamedObj typeable, Map options) {
        if (options != null) {
            Object possibilities = ((Map) options).get("options");
            if (possibilities != null) {
                // Possibilities are specified.
                Parameter parameter = null;
                if (typeable instanceof Parameter) {
                    parameter = (Parameter) typeable;
                } else if (typeable instanceof ParameterPort) {
                    parameter = ((ParameterPort) typeable).getParameter();
                } else {
                    // Can't set possibilities. Ignore this option.
                    return;
                }
                Iterable choices = null;
                if (possibilities instanceof ScriptObjectMirror) {
                    choices = ((ScriptObjectMirror) possibilities).values();
                } else if (possibilities instanceof Object[]) {
                    // Pathetically, Arrays.asList() doesn't work.
                    // It returns a list with one element, the array!
                    // choices = Arrays.asList(possibilities);
                    choices = new LinkedList();
                    for (int i = 0; i < ((Object[])possibilities).length; i++) {
                        ((LinkedList)choices).add(((Object[])possibilities)[i].toString());
                    }
                } else if (possibilities instanceof int[]) {
                    choices = new LinkedList();
                    for (int i = 0; i < ((int[])possibilities).length; i++) {
                        ((LinkedList)choices).add(new Integer(((int[])possibilities)[i]));
                    }
                } else if (possibilities instanceof double[]) {
                    choices = new LinkedList();
                    for (int i = 0; i < ((double[])possibilities).length; i++) {
                        ((LinkedList)choices).add(new Double(((double[])possibilities)[i]));
                    }
                }
                if (choices != null) {
                    for (Object possibility : choices) {
                        if (possibility instanceof String) {
                            if (parameter.isStringMode()) {
                                parameter.addChoice((String) possibility);
                            } else {
                                parameter.addChoice("\"" + (String) possibility
                                        + "\"");
                            }
                        }
                    }
                }
            }
        }
    }

    /** Put the argument in string mode. If the argument is a parameter,
     *  do that directly. Otherwise, if it is ParameterPort, then put its associated
     *  parameter in string mode. Otherwise, do nothing.
     *  @param typeable A Parameter or ParameterPort.
     *  @param JSONmode True to indicate that the string needs to be JSON.
     *  @throws IllegalActionException If the marker attribute cannot be added.
     */
    private void _setStringMode(NamedObj typeable, boolean JSONmode)
            throws NameDuplicationException, IllegalActionException {
        Parameter parameter = null;
        if (typeable instanceof Parameter) {
            parameter = (Parameter)typeable;
        } else if (typeable instanceof ParameterPort) {
            parameter = ((ParameterPort)typeable).getParameter();
            _markJSONmode(parameter, JSONmode);
        } else if (JSONmode) {
            // Argument must be a simple port.
            _markJSONmode(typeable, JSONmode);
        }
        if (parameter != null) {
            /** The following doesn't work. Not persistent.
            parameter.setStringMode(true);
            */
            new SingletonAttribute(parameter, "_stringMode");
            _markJSONmode(parameter, JSONmode);
        }
    }

    /** Invoke the specified function after the specified amount of
     *  time.  Unlike the public method, this method uses the
     *  specified id.  The time will be added to the current time of
     *  the director, and fireAt() request will be made of the
     *  director. If the director cannot fulfill the request, this
     *  method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime
     *  parameter needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @param id The id for the callback function.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     */
    private synchronized void _setTimeout(final Runnable function,
            int milliseconds, Integer id) throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (currentTime == null) {
            throw new IllegalActionException(this,
                    "Model is not running. Please set timeouts and intervals in initialize.");
        }
        final Time callbackTime = currentTime.add(milliseconds * 0.001);

        Time responseTime = getDirector().fireAt(this, callbackTime);
        if (!responseTime.equals(callbackTime)) {
            throw new IllegalActionException(this,
                    "Director is unable to fire this actor at the requested time "
                            + callbackTime
                            + ". It replies that it will fire the actor at "
                            + responseTime + ".");
        }

        // Record the callback function indexed by ID.
        if (_pendingTimeoutFunctions == null) {
            _pendingTimeoutFunctions = new HashMap<Integer, Runnable>();
        }
        _pendingTimeoutFunctions.put(id, function);

        // Record the ID of the timeout indexed by time.
        if (_pendingTimeoutIDs == null) {
            _pendingTimeoutIDs = new HashMap<Time, List<Integer>>();
        }
        List<Integer> ids = _pendingTimeoutIDs.get(callbackTime);
        if (ids == null) {
            ids = new LinkedList<Integer>();
            _pendingTimeoutIDs.put(callbackTime, ids);
        }
        ids.add(id);
    }

    /** Convert an accessor type definition into a Ptolemy type.
     *  If the specified type is "JSON" or "string", then the parameter
     *  is put into string mode.
     *  @param type The type designation.
     *  @param typeable The object to be typed.
     *  @return The Ptolemy II type.
     *  @exception IllegalActionException If the type is not supported.
     */
    private Type _typeAccessorToPtolemy(String type, NamedObj typeable)
            throws IllegalActionException, NameDuplicationException {
        if (type.equals("number")) {
            return (BaseType.DOUBLE);
        } else if (type.equals("JSON")) {
            _setStringMode(typeable, true);
            return (BaseType.STRING);
        } else if (type.equals("int")) {
            return (BaseType.INT);
        } else if (type.equals("string")) {
            _setStringMode(typeable, false);
            return (BaseType.STRING);
        } else if (type.equals("boolean")) {
            return (BaseType.BOOLEAN);
        } else if (type.equals("select")) {
            _setStringMode(typeable, false);
            return (BaseType.STRING);
        } else {
            throw new IllegalActionException(this, "Unsupported type: " + type);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        Private Variables                  ////

    /** A list of generic input handlers, in the order in which they are invoked. */
    private List<Runnable> _genericInputHandlers;

    /** The current handle to assign to an input handler. */
    private int _handle;

    /** A mapping of the handle for an input handler to its index in the list. */
    private Map<Integer, Integer> _handleToIndex;

    /** A mapping of the handle for an input handler to the port or parameter proxy. */
    private Map<Integer, PortOrParameterProxy> _handleToProxy;

    /** True while the actor is firing, false otherwise. */
    private boolean _inFire;

    /** Buffer for all input tokens before calling the fire function
     *  in the script. Calls to get in the script will then retrieve
     *  the value from this buffer instead of actually calling get on
     *  the port.
     */
    private HashMap<IOPort, HashMap<Integer, Token>> _inputTokens = new HashMap<IOPort, HashMap<Integer, Token>>();

    /** Buffer for output tokens that are produced in a call to send
     *  while the actor is not firing. This makes sure that actors can
     *  spontaneously produce outputs.
     */
    private HashMap<IOPort, HashMap<Integer, List<Token>>> _outputTokens;

    /** Map from timeout ID to pending timeout functions. */
    private Map<Integer, Runnable> _pendingTimeoutFunctions;

    /** Map from timeout time to pending timeout IDs. */
    private Map<Time, List<Integer>> _pendingTimeoutIDs;

    /** Map of proxies for ports and parameters. */
    private HashMap<NamedObj, PortOrParameterProxy> _proxies;

    /** Map of proxies for ports and parameters by name */
    private HashMap<String, PortOrParameterProxy> _proxiesByName;

    /** Count to give a unique handle to pending timeouts. */
    private int _timeoutCount = 0;

    ///////////////////////////////////////////////////////////////////
    ////                        Inner Classes                      ////

    /** Proxy for a port or parameter.
     *
     *  This is used to wrap ports and parameters for security
     *  reasons.  If we expose the port or paramter to the JavaScript
     *  environment, then the script can access all aspects of the
     *  model containing this actor. E.g., it can call getContainer()
     *  on the object.
     *
     *  This wrapper provides access to the port or parameter only via
     *  a protected method, which JavaScript cannot access.
     */
    public class PortOrParameterProxy {
        /** Construct a proxy.
         *  @param portOrParameter The object to be proxied.
         *  @exception IllegalActionException If the argument is neither a port nor a parameter.
         */
        protected PortOrParameterProxy(NamedObj portOrParameter)
                throws IllegalActionException {
            if (portOrParameter instanceof Variable) {
                _parameter = (Variable) portOrParameter;
            } else if (portOrParameter instanceof TypedIOPort) {
                _port = (TypedIOPort) portOrParameter;
            } else {
                throw new IllegalActionException(JavaScript.this,
                        portOrParameter,
                        "Cannot create a proxy for something that is neither a port nor a parameter.");
            }
        }

        /** Add an input handler for this port.
         *  @param function The function to invoke.
         *  @return The handler handle.
         *  @exception IllegalActionException If this proxy is not for an input port.
         *  @see #removeInputHandler(Integer)
         */
        public int addInputHandler(final Runnable function)
                throws IllegalActionException {
            if (_inputHandlers == null) {
                _inputHandlers = new LinkedList<Runnable>();
            }
            if (_port == null || !_port.isInput()) {
                if (_parameter instanceof PortParameter) {
                    TypedIOPort port = ((PortParameter) _parameter).getPort();
                    if (port != null) {
                        _inputHandlers.add(function);
                        int index = _inputHandlers.size() - 1;
                        _handle++;
                        _handleToIndex.put(_handle, index);
                        _handleToProxy.put(_handle, this);
                        return _handle;
                    }
                }
                throw new IllegalActionException(JavaScript.this,
                        "Not an input port: "
                                + ((_port == null) ? _parameter.getName()
                                        : _port.getName()));
            }
            _inputHandlers.add(function);
            int index = _inputHandlers.size() - 1;
            _handle++;
            _handleToIndex.put(_handle, index);
            _handleToProxy.put(_handle, this);
            return _handle;
        }

        /** Get the current value of the input port or a parameter.
         *  If it is a ParameterPort, then retrieve the value
         *  from the corresponding parameter instead (the fire() method
         *  will have done update).
         *  @param channelIndex The channel index. This is ignored for parameters.
         *  @return The current value of the input or parameter, or null if there is none.
         *  @exception IllegalActionException If the port is not an input port
         *   or retrieving the value fails.
         */
        public Token get(int channelIndex) throws IllegalActionException {
            if (_parameter != null) {
                Token token = _parameter.getToken();
                if (token == null || token.isNil()) {
                    return null;
                }
                return token;
            }
            if (_port instanceof ParameterPort) {
                Token token = ((ParameterPort) _port).getParameter().getToken();
                if (token == null || token.isNil()) {
                    return null;
                }
                return token;
            }
            synchronized (JavaScript.this) {
                Map<Integer, Token> tokens = _inputTokens.get(_port);
                if (tokens != null) {
                    Token token = tokens.get(channelIndex);
                    if (token == null || token.isNil()) {
                        return null;
                    }
                    return token;
                }
            }
            return null;
        }

        /** Invoke any input handlers that have been added if there
         *  are new inputs.
         *  @see #addInputHandler(Runnable)
         */
        public void invokeHandlers() {
            synchronized(JavaScript.this) {
                if (_inputHandlers != null && _hasNewInput) {
                    for (Runnable function : _inputHandlers) {
                        if (function != null) {
                            // FIXME: Need to invoke the function so that 'this' is the exports object!
                            function.run();
                            if (_debugging) {
                                _debug("Invoked handler function for "
                                        + _port.getName());
                            }
                        }
                    }
                }
            }
        }
        
        /** Return true if the port or parameter value is required to be JSON.
         *  @return True for JSON ports and parameters.
         */
        public boolean isJSON() {
            if (_parameter != null) {
                return (_parameter.getAttribute("_JSON") != null);
            } else {
                return (_port.getAttribute("_JSON") != null);
            }
        }

        /** Remove the input handler with the specified handle, if it exists.
         *  @param handle The handler handle.
         *  @see #addInputHandler(Runnable)
         */
        public void removeInputHandler(Integer handle) {
            if (handle != null && _handleToIndex != null) {
                Integer index = _handleToIndex.get(handle);
                if (index != null) {
                    int n = index.intValue();
                    if (_inputHandlers != null && n < _inputHandlers.size()) {
                        _inputHandlers.remove(n);
                    }
                    _handleToIndex.remove(handle);
                    _handleToProxy.remove(handle);
                }
            }
        }

        /** Expose the send() method of the port.
         *  @param channelIndex The channel index.
         *  @param data The token to send.
         *  @exception IllegalActionException If this is a proxy for a parameter or if sending fails.
         *  @exception NoRoomException If there is no room at the destination.
         */
        public void send(int channelIndex, Token data) throws NoRoomException,
                IllegalActionException {
            if (_port == null) {
                throw new IllegalActionException(JavaScript.this,
                        "Cannot call send on a parameter: "
                                + _parameter.getName() + ". Use set().");
            }
            // If the model is not in a phase of execution where it can send (in initialize()
            // or during execution), then do not send the token. Instead, send messages
            // to the debug channel and stderr. This is likely occurring in a callback that
            // is being invoked after the model has terminated or while the model is in its
            // wrapup phase.
            //
            // NOTE: There is a window between the last firing of this actor and the
            // invocation of wrapup() where _executing == true, but nevertheless, there
            // will be no opportunity to actually send the data token.  In this case,
            // we queue the token and deal with the problem in the wrapup() method.
            //
            // NOTE: This has to be outside the synchronized block! Do not try to
            // get the lock because a Vertx thread might be trying to
            // send while the actor has already obtained the lock in
            // wrapup() and may be calling WebSocketHelper.close(). See
            // https://chess.eecs.berkeley.edu/ptolemy/wiki/Ptolemy/Deadlock
            //
            // FIXME: The above NOTE appears to be wrong.
            // WebSocketHelper is holding a lock on _actor
            // when it calls this in DataHandler.
            if (!_executing) {
                String message = "WARNING: Invoking send() too late (probably in a callback), so "
                        + getName()
                        + " is not able to send the token "
                        + data
                        + " to the output "
                        + _port.getName()
                        + ". Token is discarded.";
                if (_debugging) {
                    _debug(message);
                } else {
                    System.err.println(message);
                }
                return;
            }

            // FIXME: Race condition. wrapup() could be invoked now, setting _executing == false
            // and then acquiring a lock on this and calling WebSocketHelper.close().
            // So we may still have a deadlock risk.
            synchronized (JavaScript.this) {
                if (!_port.isOutput()) {
                    if (!_port.isInput()) {
                        throw new IllegalActionException(JavaScript.this,
                                "Cannot send to a port that is neither an input nor an output.");
                    }
                    // Port is an input.
                    // Record the token value to be provided in get().
                    if (_localInputTokens == null) {
                        _localInputTokens = new LinkedList<Token>();
                    }
                    _localInputTokens.add(data);
                    _fireAtCurrentTime();
                } else if (_inFire) {
                    // Currently firing. Can go ahead and send data.
                    if (_debugging) {
                        _debug("Sending " + data + " to " + _port.getName());
                    }
                    _port.send(channelIndex, data);
                } else {
                    // Not currently firing. Queue the tokens and request a firing.
                    // This should be being called in a callback that holds a
                    // synchronization lock, so synchronizing this isn't really
                    // necessary, but just in case...
                    if (_outputTokens == null) {
                        _outputTokens = new HashMap<IOPort, HashMap<Integer, List<Token>>>();
                    }
                    HashMap<Integer, List<Token>> tokens = _outputTokens
                            .get(_port);
                    if (tokens == null) {
                        tokens = new HashMap<Integer, List<Token>>();
                        _outputTokens.put(_port, tokens);
                    }
                    List<Token> queue = tokens.get(channelIndex);
                    if (queue == null) {
                        queue = new LinkedList<Token>();
                        tokens.put(channelIndex, queue);
                    }
                    queue.add(data);
                    if (_debugging) {
                        _debug("Queueing " + data + " to be sent on "
                                + _port.getName() + " and requesting a firing.");
                    }
                    // Request a firing at the current time.
                    // In this case, we _do_ want to use the director's
                    // fireAtCurrentTime(), and not our _fireAtCurrentTime()
                    // method, because we are not in firing, so if we are
                    // synchronized to real time, we want the firing to
                    // occur later than the current model time, at a time
                    // matching current real time.
                    getDirector().fireAtCurrentTime(JavaScript.this);
                }
            }
        }

        /** Set the current value of the parameter.
         *  @param token The value of the parameter.
         *  @exception IllegalActionException If the set fails or if this is a proxy for a port.
         */
        public void set(Token token) throws IllegalActionException {
            if (_parameter == null) {
                throw new IllegalActionException(JavaScript.this,
                        "Cannot call set on a port " + _port.getName()
                                + ". Use send().");
            }
            _parameter.setToken(token);
        }

        /** Return the name of the proxied port or parameter.
         *  @return The name of the proxied port or parameter
         */
        @Override
        public String toString() {
            if (_port != null) {
                return _port.getName();
            }
            return _parameter.getName();
        }

        /////////////////////////////////////////////////////////////////
        ////           Protected methods

        /** Indicate to this proxy there is a new input from the outside,
         *  so handlers should be invoked.
         *  @param hasNewInput True to indicate that there is a new input.
         */
        protected void _hasNewInput(boolean hasNewInput) {
            _hasNewInput = hasNewInput;
        }

        /////////////////////////////////////////////////////////////////
        ////           Protected variables

        /** Indicator that there is a new input from the outside,
         *  so handlers should be invoked.
         */
        protected boolean _hasNewInput;

        /** A list of input handlers, in the order in which they are invoked. */
        protected List<Runnable> _inputHandlers;

        /** A list of tokens that this JavaScript actor has sent to its own input. */
        protected List<Token> _localInputTokens;

        /** The parameter that is proxied, or null if it's a port. */
        protected Variable _parameter;

        /** The port that is proxied, or null if it's a parameter. */
        protected TypedIOPort _port;
    }
}
