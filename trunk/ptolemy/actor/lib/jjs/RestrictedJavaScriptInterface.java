/* Restricted interface to the JavaScript actor.

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
package ptolemy.actor.lib.jjs;

import java.util.Map;

import ptolemy.actor.lib.jjs.JavaScript.PortOrParameterProxy;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


///////////////////////////////////////////////////////////////////
//// RestrictedJavaScriptInterface

/**
   Restricted interface to the {@link JavaScript} actor.
   An instance of this class provides limited access to the JavaScript actor
   specified in the constructor, given access only to methods that an untrusted
   script can safely execute.
   
   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class RestrictedJavaScriptInterface {
    
    /** Construct a restricted interface to the specified JavaScript actor.
     *  @param actor The actor.
     */ 
    public RestrictedJavaScriptInterface(JavaScript actor) {
	_actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an input handler to be invoked when there is a new token on
     *  any input port.
     *  @param function The function to handle the token.
     *  @return The incremented input handler index.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #removeInputHandler(Integer)
     */
    public int addInputHandler(final Runnable function) throws IllegalActionException {
        return _actor.addInputHandler(function);
    }

    /** Specify author information to appear in the documentation for this actor.
     *  @param author Author information to appear in documentation.
     */
    public void author(String author) {
        _actor.author(author);
    }

    /** Clear the interval with the specified handle, if it has not already executed.
     *  @param handle The interval handle.
     *  @see #setInterval(Runnable, int)
     */
    public void clearInterval(Integer handle) {
        _actor.clearTimeout(handle);
    }

    /** Clear the timeout with the specified handle, if it has not already executed.
     *  @param handle The timeout handle.
     *  @see #setTimeout(Runnable, int)
     */
    public void clearTimeout(Integer handle) {
        _actor.clearTimeout(handle);
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The recommended format for documentation is HTML or Markdown.
     *  @param description A description to appear in documentation.
     */
    public void description(String description) {
        _actor.description(description);
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The recommended format for documentation is HTML or Markdown.
     *  @param description A description to appear in documentation.
     *  @param type The type, which should be one of "text/html" (the default if null
     *   is given), "text/markdown",  or "text/plain".
     */
    public void description(String description, String type) {
        _actor.description(description, type);
    }

    /** Delegate to the associated JavaScript actor to report an error.
     *  @param message The message
     *  @throws IllegalActionException If the error cannot be handled.
     */
    public void error(String message) throws IllegalActionException {
	_actor.error(message);
    }
    
    /** Get the proxy for a port or parameter with the specified name.
     *  This is an object on which JavaScript can directly invoke methods.
     *  @param name The name of the port or parameter.
     *  @return The proxy for the specified name, or null if there is none.
     */
    public PortOrParameterProxy getPortOrParameterProxy(String name) {
	return _actor.getPortOrParameterProxy(name);
    }
    
    /** Create a new input port if it does not already exist.
     *  Leave the type unspecified so that it will be inferred.
     *  @param name The name of the port.
     *  @throws IllegalActionException If no name is given.
     *  @throws NameDuplicationException If the name is a reserved word.
     */
    public void input(String name)
	    throws IllegalActionException, NameDuplicationException {
	_actor.input(name, null);
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
     *  @param name The name of the port.
     *  @param options The options, or null to accept the defaults.
     *  @throws IllegalActionException If no name is given.
     *  @throws NameDuplicationException If the name is a reserved word.
     */
    public void input(String name, Map options)
	    throws IllegalActionException, NameDuplicationException {
	_actor.input(name, options);
    }

    /** Return true.
     *  A restricted JavaScript actor limits the capabilities available
     *  to the script it executes so that it can execute untrusted code. 
     *  This class is an interface to a restricted JavaScript actor,
     *  so it returns true.
     *  @return True.
     */
    public boolean isRestricted() {
        return true;
    }

    /** Delegate to the associated JavaScript actor to log a message.
     *  @param message The message
     */
    public void log(String message) {
	_actor.log(message);
    }

    /** Create a new output port if it does not already exist.
     *  Set the type to general.
     *  @param name The name of the port.
     *  @throws IllegalActionException If no name is given.
     *  @throws NameDuplicationException If the name is a reserved word.
     */
    public void output(String name)
	    throws IllegalActionException, NameDuplicationException {
	_actor.output(name, null);
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
     *  @throws IllegalActionException If no name is given.
     *  @throws NameDuplicationException If the name is a reserved word.
     */
    public void output(String name, Map<String,String> options)
	    throws IllegalActionException, NameDuplicationException {
	_actor.output(name, options);
    }

    /** Create a new parameter if it does not already exist.
     *  This parameter will have an undeclared type and no description.
     *  @param name The name of the parameter.
     *  @throws IllegalActionException If no name is given, or if the
     *   model is executing.
     *  @throws NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public void parameter(String name)
	    throws IllegalActionException, NameDuplicationException {
	_actor.parameter(name);
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
     *  @throws IllegalActionException If no name is given.
     *  @throws NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public void parameter(String name, Map options)
	    throws IllegalActionException, NameDuplicationException {
	_actor.parameter(name, options);
    }
    
    /** Remove the input handler with the specified handle, if it exists.
     *  @param handle The handler handle.
     *  @see #addInputHandler(Runnable)
     */
    public void removeInputHandler(Integer handle) {
        _actor.removeInputHandler(handle);
    }

    /** Invoke the specified function after the specified interval and
     *  periodically after that.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param millisecond The number of milliseconds in the future to invoke it
     *   and the period thereafter.
     *  @return A unique ID for this callback
     *  @throws IllegalActionException If the director cannot respect the request.
     */
    public int setInterval(final Runnable function, int millisecond) throws IllegalActionException {
	return _actor.setInterval(function, millisecond);
    }

    /** Invoke the specified function after the specified amount of time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param millisecond The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback
     *  @throws IllegalActionException If the director cannot respect the request.
     */
    public int setTimeout(final Runnable function, int millisecond) throws IllegalActionException {
	return _actor.setTimeout(function, millisecond);
    }
    
    /** Specify version information to appear in the documentation for this actor.
     *  @param version Version information to appear in documentation.
     */
    public void version(String version) {
	_actor.version(version);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        Protected Methods                  ////
    
    /** Return the actor that this wraps. This is protected so that
     *  instances of HelperBase can retrieve it (for the purposes of
     *  synchronization), but not public so that JavaScript cannot gain
     *  access to it.
     *  @return the actor that this wraps.
     */
    protected JavaScript _getActor() {
	return _actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        Private Variables                  ////

    /** The actor to which this is providing an interface. */
    private JavaScript _actor;
}
