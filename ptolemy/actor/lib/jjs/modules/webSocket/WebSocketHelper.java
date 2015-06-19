/* AnExecute a script in JavaScript.

@Copyright (c) 2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.lib.jjs.modules.webSocket;

import java.util.LinkedList;
import java.util.List;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;

import ptolemy.actor.lib.jjs.modules.VertxHelperBase;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the webSocket module in JavaScript.
   Instances of this class are helpers for individual sockets.
   See the documentation of that module for instructions.
   This uses Vert.x for the implementation.
   
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @see WebSocketServerHelper
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper extends VertxHelperBase {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Close the web socket.
     */
    public void close() {
	synchronized(_actor) {
	    // Stop reconnect attempts by stating that the number of
	    // tries has already exceeded the maximum. This will also
	    // abort any connection that is established after this close()
	    // method has been called.
	    _numberOfTries = _numberOfRetries + 2;
	    if (_thread != null) {
	        _thread.interrupt();
	    }

	    if (_webSocket != null) {
	        if (_wsIsOpen) {
	            _webSocket.close();
	        }
	        _webSocket = null;
	    }

	    if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
	        try {
	            _currentObj.callMember("emit", "error",
	                    "Unsent messages remain that were queued before the socket opened: "
	                    + _pendingOutputs.toString());
	        } catch (Throwable ex) {
	            // Emitting an error event doesn't work for some reason.
	            // Report the error anyway.
	            _actor.error("Unsent messages remain that were queued before the socket opened: "
                            + _pendingOutputs.toString());
	        }
	    }
	}
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param host IP address or host name of the host.
     *  @param port The port number that the host listens on.
     *  @param numberOfRetries The number of retries.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
	    ScriptObjectMirror currentObj, String host, int port, int numberOfRetries, int timeBetweenRetries) {
	return new WebSocketHelper(currentObj, host, port, numberOfRetries, timeBetweenRetries);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
	    ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        return new WebSocketHelper(currentObj, serverWebSocket);
    }
    
    /** Send text data through the web socket.
     *  @param msg A text message to be sent.
     *  @throws IllegalActionException If establishing the connection to the web socket has
     *   permanently failed.
     */
    public void sendText(String msg) throws IllegalActionException {
	synchronized(_actor) {
	    if (_wsFailed != null) {
	        throw new IllegalActionException(_actor, _wsFailed,
	                "Failed to establish connection after " + _numberOfTries + " tries.");
	    }
	    if (isOpen()) {
		_webSocket.writeTextFrame(msg);
	    } else {
	        // FIXME: Bound the queue. Option to discard data.
		if (_pendingOutputs == null) {
		    _pendingOutputs = new LinkedList();
		}
		_pendingOutputs.add(msg);
	    }
	}
    }

    /** Return whether the web socket is opened successfully.
     *  @return True if the socket is open.
     */
    public boolean isOpen() {
	synchronized(_actor) {
	    if (_webSocket == null) {
		return false;
	    }
	    return _wsIsOpen;
	}
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param host The IP address or host name of the host.
     *  @param port The port number of the host.
     *  @param numberOfRetries The maximum number of retries if a connect attempt fails.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     */
    private WebSocketHelper(
	    ScriptObjectMirror currentObj, String host, int port, int numberOfRetries, int timeBetweenRetries) {
        super(currentObj);

        _host = host;
        _port = port;
        _numberOfRetries = numberOfRetries;
        _timeBetweenRetries = timeBetweenRetries;
        _client = _vertx.createHttpClient();
        _client.setHost(host);
        _client.setPort(port);
        _client.exceptionHandler(new HttpClientExceptionHandler());
        _wsFailed = null;
        
        // Make the connection. This might eventually be a wrapped in a public method.
        _numberOfTries = 1;
        _connectWebsocket(host, port, _client);
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        super(currentObj);
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new WebSocketExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /** Connect to a web socket on the specified host.
     *  @param host The host IP or name.
     *  @param port The port.
     *  @param client The HttpClient object.
     */
    private void _connectWebsocket(String host, int port, HttpClient client) {
        // FIXME: Provide a timeout. Use setTimeout() of the client.
        // FIXME: Why does Vertx require the URI here in addition to setHost() and setPort() above? Seems lame.
        String address = "ws://" + host + ":" + port;
        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                synchronized(_actor) {
                    if (_numberOfTries > _numberOfRetries + 1) {
                        // close() has been called. Abort the connection.
                        websocket.close();
                        return;
                    }
                    _wsIsOpen = true;
                    _webSocket = websocket;

                    _webSocket.dataHandler(new DataHandler());
                    _webSocket.endHandler(new EndHandler());
                    _webSocket.exceptionHandler(new WebSocketExceptionHandler());

                    // Socket.io uses the name "connect" for this event, but WS uses "open",
                    // so we just emit both events.
                    _currentObj.callMember("emit", "connect");
                    _currentObj.callMember("emit", "open");
                    
                    // Send any pending messages.
                    if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                        for (Object message : _pendingOutputs) {
                            if (message instanceof String) {
                                _webSocket.writeTextFrame((String)message);
                            } else {
                                _webSocket.writeBinaryFrame((Buffer)message);
                            }
                        }
                        _pendingOutputs.clear();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The HttpClient object. */
    private HttpClient _client;
    
    /** The host IP or name. */
    private String _host;
    
    /** Maximum number of attempts to reconnect to the web socket if the first try fails. */
    private int _numberOfRetries = 0;
    
    /** Number of attempts so far to connect to the web socket. */
    private int _numberOfTries = 1;
    
    /** Pending outputs received before the socket is opened. */
    private List _pendingOutputs;
    
    /** The host port. */
    private int _port;
    
    /** The time between retries, in milliseconds. */
    private int _timeBetweenRetries;

   /** The thread that reconnects the web socket connection after the timeBetweenRetries interval. */
    private Thread _thread = null;

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;
    
    /** Indicator that web socket connection has failed. No need to keep waiting. */
    private Throwable _wsFailed = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;
   

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /** The event handler that is triggered when a message arrives on the web socket.
     */
    private class DataHandler implements Handler<Buffer> {
        @Override
        public void handle(Buffer buffer) {
            synchronized(_actor) {
        	// This assumes the input is a string encoded in UTF-8.
        	_currentObj.callMember("notifyIncoming", buffer.toString());
            }
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            synchronized(_actor) {
        	_currentObj.callMember("emit", "close");
                _wsIsOpen = false;
                if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                    _currentObj.callMember("emit", "error", "Unsent data remains in the queue.");
                }
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     *  An error here occurs after a socket connection has been established.
     */
    private class WebSocketExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized(_actor) {
        	_currentObj.callMember("emit", "error", throwable.getMessage());
        	_wsIsOpen = false;
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the http client.
     *  An error here may occur, for example, when a socket fails to get established.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            synchronized(_actor) {
                if (_numberOfTries >= _numberOfRetries + 1) {
                    _currentObj.callMember("emit", "error", 
                            "After " + _numberOfTries + " tries: " + arg0.getMessage());
                    _wsIsOpen = false;
                    _wsFailed = arg0;
                } else {
                    _actor.log("Connection failed. Will try again: " + arg0.getMessage());
                    // Retry. Create a new thread to do this.
                    Runnable retry = new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(_timeBetweenRetries);
                            } catch (InterruptedException e) {
                                _currentObj.callMember("emit", "error", 
                                        "After " + _numberOfTries + " tries, retries have been cancelled." );
                                _wsIsOpen = false;
                                _wsFailed = e;
                                return;
                            }
                            synchronized(_actor) {
                                // Prevent any attempt to interrupt this thread, since it is
                                // no longer sleeping.
                                _thread = null;
                                // Check again the status of the number of tries.
                                // This may have been changed if close() was called,
                                // which occurs, for example, when the model stops executing.
                                if (_numberOfTries <= _numberOfRetries) {
                                    _numberOfTries++;
                                    _connectWebsocket(_host, _port, _client);
                                }
                            }
                        }
                    };
                    _thread = new Thread(retry);
                    _thread.start();
                }
            }
        }
    }
}
