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
package ptolemy.actor.lib.jjs.modules.webSocket;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the WebSocket module in JavaScript.
   The Vert.x object from its parent can create an instance of Java WebSocket.
   Each Java WebSocket belongs to one JavaScript WebSocket. 
   
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper extends WebSocketHelperBase {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Close the web socket.
     */
    public void close() {
        if (_webSocket != null) {
            if (_wsIsOpen) {
                _webSocket.close();
            }
            _webSocket = null;
        }
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param address address The URL of the WebSocket host and the port number. 
     *   (e.g. 'ws://localhost:8000'). If no port number is given, then 80 is used.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
	    ScriptObjectMirror currentObj, String address) {
	return new WebSocketHelper(currentObj, address);
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

    /**
     * Send binary data through the internal web socket.
     * 
     * @param msg A binary message to be sent.
     */
    public void sendBinary(byte[] msg) {
        Buffer buffer = new Buffer(msg);
        _webSocket.writeBinaryFrame(buffer);
    }
    
    /**
     * Send text data through the internal web socket.
     * 
     * @param msg A text message to be sent.
     */
    public void sendText(String msg) {
        _webSocket.writeTextFrame(msg);
    }

    /** Return whether the web socket is opened successfully.
     *  @return True if the socket is open.
     */
    public boolean isOpen() {
	if (_webSocket == null) {
	    return false;
	}
	return _wsIsOpen;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param address The URL of the WebSocket host with an optional port number
     *   (e.g. 'ws://localhost:8000'). If no port number is given, 80 is used.
     */
    private WebSocketHelper(
	    ScriptObjectMirror currentObj, String address) {
        _currentObj = currentObj;

        HttpClient client = _vertx.createHttpClient();
        // Parse the address.
        // FIXME: Use utilities for this. Perhaps on the JavaScript side?
        // So far, Java.net.URL raises an exception when protocol name is ws such as ws://localhost:8000.
        // io.netty doesn't work either.
        // Couldn't find a JavaScript side library for parsing URL, 
        // except for using the document object which is not available in node.js.
        if (address.length() > 0 && address.charAt(address.length() - 1) == '/') {
            address = address.substring(0, address.length() - 1);
        }
        int begin = address.indexOf("://");
        if (begin < 0) {
            throw new RuntimeException("Invalid host name in URI: " + address);
        }
        int sep = address.lastIndexOf(':');
        String host = address.substring(begin + 3, sep);
        client.setHost(host);
        
        if (sep > 0) {
            try {
        	client.setPort(Integer.parseInt(address.substring(sep + 1)));
            } catch (NumberFormatException e) {
        	throw new RuntimeException("Invalid port in URI: " + address);
            }
        } else {
            client.setPort(80);
        }
        client.exceptionHandler(new HttpClientExceptionHandler());
        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                _wsIsOpen = true;
                _webSocket = websocket;
                
                _webSocket.dataHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new WebSocketExceptionHandler());
                
                // Socket.io uses the name "connect" for this event, but WS uses "open",
                // so we just emit both events.
                _currentObj.callMember("emit", "connect");
                _currentObj.callMember("emit", "open");
            }
        });
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        _currentObj = currentObj;
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new WebSocketExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /** The event handler that is triggered when a message arrives on the web socket.
     */
    private class DataHandler implements Handler<Buffer> {
        @Override
        public void handle(Buffer buff) {
            byte[] bytes = buff.getBytes();
            Integer[] objBytes = new Integer[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                objBytes[i] = (int)bytes[i];
            }
            
            // Properties of the data.
            // FIXME: What are these properties? Pass string directly?
            Object jsArgs = _currentObj.eval(" var properties = {binary: true}; properties");
            _currentObj.callMember("emit", "message", objBytes, jsArgs);
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            _currentObj.callMember("emit", "close");
            _wsIsOpen = false;
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class WebSocketExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            _currentObj.callMember("emit", "error");
            _wsIsOpen = false;
        }
    }

    /** The event handler that is triggered when an error occurs in the http client.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            Object jsArgs = _currentObj.eval("\'" + arg0.getMessage() + "\'");
            _currentObj.callMember("emit", "close", jsArgs);
            _wsIsOpen = false;
        }
    }
}
