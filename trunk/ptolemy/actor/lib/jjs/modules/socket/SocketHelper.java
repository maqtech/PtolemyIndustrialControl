/* Support for the websocket accessor.

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
package ptolemy.actor.lib.jjs.modules.socket;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;



///////////////////////////////////////////////////////////////////
//// SocketHelper

/**
   A helper class for the socket module in JavaScript.
   You should use {@link #getOrCreateHelper(Object)} to create
   exactly one instance of this helper per actor. Pass the actor
   as an argument.

   A confusing aspect of this design is the socket client will
   have exactly one socket associated with it, whereas a socket
   server can have any number of sockets associated with it.
   In any case, there should be only one instance of this class
   associated with any actor. This ensures that all the socket
   actions and callbacks managed by this instance execute in
   a single verticle.
   
   This class supports transmission of strings, images, and binary
   numerical data of type byte, double, float, int, long, short,
   unsigned byte, unsigned int, and unsigned short, and arrays of
   all the numerical type.
   If you specify the send type to be "image", then a default
   JPG encoding will be used for transmission. Otherwise, the specific
   image type encoding that you specify will be used.
   For received images, any image type supported by the ImageIO
   readers can be accepted, so you just need to specify "image".

   @author Edward A. Lee, Contributor: Hokeun Kim
   @version $Id$
   @see SocketServerHelper
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
 */
public class SocketHelper extends VertxHelperBase {

    /** Constructor for SocketHelper for the specified actor.
     *  @param actor The actor that this will help.
     */
    public SocketHelper(Object actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a client-side socket on behalf of the specified
     *  JavaScript SocketClient object. After this is called,
     *  the specified socketClient will emit the following events:
     *  * open: Emitted when the connection has been established
     *    with the server. This will not be passed any arguments.
     *  * data: Emitted when data is received on the socket.
     *    The received data will be an argument to the event.
     *  * close: Emitted when a socket is closed.
     *    This will not be passed any arguments.
     *  * error: Emitted when an error occurs. This will be passed
     *    an error message.
     * 
     *  @param socketClient The JavaScript SocketClient instance.
     *  @param port The remote port to connect to.
     *  @param host The remote host to connect to.
     *  @param options The options (see the socket.js JavaScript module).
     */
    public void openClientSocket(
            final ScriptObjectMirror socketClient,
            final int port,
            final String host,
            Map<String,Object> options) {
        if ((Boolean)options.get("trustAll")) {
            if (!MessageHandler.yesNoQuestion(
                    "The client is set to trust all certificates ('trustAll' option is true). "
                    + "This means that the client can connect to any server with no "
                    + "verification of the identity of the server. "
                    + "Are you sure?")) {
                return;
            }
        }

        // NOTE: The following assumes all the options are defined.
        // This is handled in the associated JavaScript socket.js module.
        final NetClientOptions clientOptions = new NetClientOptions()
                .setConnectTimeout((Integer)options.get("connectTimeout"))
                .setIdleTimeout((Integer)options.get("idleTimeout"))
                .setReceiveBufferSize((Integer)options.get("receiveBufferSize"))
                .setReconnectAttempts((Integer)options.get("reconnectAttempts"))
                .setReconnectInterval((Integer)options.get("reconnectInterval"))
                .setSendBufferSize((Integer)options.get("sendBufferSize"))
                .setSsl((Boolean)options.get("sslTls"))
                .setTcpKeepAlive((Boolean)options.get("keepAlive"))
                .setTcpNoDelay((Boolean)options.get("noDelay"))
                .setTrustAll((Boolean)options.get("trustAll"));
        
        // NOTE: Find out the (undocumented) default in Vert.x.
        // System.err.println("TcpNoDelay: " + clientOptions.isTcpNoDelay());

        // Create the socket in the associated verticle.
        submit(() -> {
            NetClient client = _vertx.createNetClient(clientOptions);
            // NOTE: In principle, this client can handle multiple connections.
            // But here we use exactly one client per connection. Is this OK?
            client.connect(port, host, response -> {
                if (response.succeeded()) {
                    // Socket has been opened.
                    NetSocket socket = response.result();

                    _issueResponse(() -> {
                        // This should be called in the director thread because it
                        // emits an event that may be handled by the user.
                        socketClient.callMember("_opened", socket, client);
                    });
                } else {
                    Throwable cause = response.cause();
                    String errorMessage = "Failed to connect: " + cause.getMessage();
                    _error(socketClient, errorMessage, cause);
                }
            });
        });
    }

    /** Get or create a helper for the specified actor.
     *  If one has been created before and has not been garbage collected, return
     *  that one. Otherwise, create a new one.
     *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
     */
    public static SocketHelper getOrCreateHelper(Object actor) {
        VertxHelperBase helper = VertxHelperBase.getHelper(actor);
        if (helper instanceof SocketHelper) {
            return (SocketHelper) helper;
        }
        return new SocketHelper(actor);
    }

    /** Create a server that can accept socket connection requests
     *  on behalf of the specified JavaScript SocketServer object.
     *  After this is called, the specified JavaScript
     *  SocketServer object will emit the following events:
     *  <ul>
     *  <li> listening: Emitted when the server is listening.
     *    This will be passed the port number that the server is
     *    listening on (this is useful if the port is specified to be 0).
     *  <li> connection: Emitted when a new connection is established
     *    after a request from (possibly remote) client.
     *    This will be passed an instance of the JavaScript Socket
     *    class that is defined in the socket.js module.
     *    That instance has a send() and close()
     *    function that can be used to send data or to close the socket.
     *    It is also an event emitter that emits 'close', 'data',
     *    and 'error' events.
     *  <li> error: If this server fails to start listening.
     *    An error message will be passed to any event handler.
     *  </ul>
     *  @param socketServer The JavaScript SocketServer instance.
     *  @param options The options (see the socket.js JavaScript module).
     */
    public void openServer(
            final ScriptObjectMirror socketServer,
            final Map<String,Object> options) {

        // Translate clientAuth option to an enum.
        ClientAuth auth = ClientAuth.NONE;
        String authSpec = (String)options.get("clientAuth");
        if (authSpec.toLowerCase().trim().equals("request")) {
            auth = ClientAuth.REQUEST;
        } else if (authSpec.toLowerCase().trim().equals("required")) {
            auth = ClientAuth.REQUIRED;
        }

        // NOTE: The following assumes all the options are defined.
        // This is handled in the associated JavaScript socket.js module.
        final NetServerOptions serverOptions = new NetServerOptions()
                .setClientAuth(auth)
                .setHost((String)options.get("hostInterface"))
                .setIdleTimeout((Integer)options.get("idleTimeout"))
                .setTcpKeepAlive((Boolean)options.get("keepAlive"))
                .setPort((Integer)options.get("port"))
                .setReceiveBufferSize((Integer)options.get("receiveBufferSize"))
                .setSendBufferSize((Integer)options.get("sendBufferSize"))
                .setSsl((Boolean)options.get("sslTls"))
                .setTcpNoDelay((Boolean)options.get("noDelay"));
        
        // If SSL/TLS is enabled, it has to be configured.
        if (serverOptions.isSsl()) {
            String keyStorePath = (String)options.get("keyStorePath");
            File keyStoreFile = FileUtilities.nameToFile(keyStorePath, null);
            if (keyStoreFile == null) {
                _error(socketServer, "Empty keyStoreFile option. Can't find the key store.");
                return;
            }
            String keyStorePassword = (String)options.get("keyStorePassword");
            try {
                serverOptions.setKeyStoreOptions(
                        new JksOptions().
                        setPath(keyStoreFile.getCanonicalPath()).
                        setPassword(keyStorePassword));
            } catch (IOException e) {
                _error(socketServer, "Failed to find key store at " + keyStoreFile);
                return;
            }
        }

        // NOTE: Find out the (undocumented) default in Vert.x.
        // System.err.println("TcpNoDelay: " + serverOptions.isTcpNoDelay());

        // Create the server in the associated verticle.
        submit(() -> {
            final NetServer server = _vertx.createNetServer(serverOptions);

            // Notify the JavaScript SocketServer object of the server.
            socketServer.callMember("_serverCreated", server);

            server.connectHandler(socket -> {
                // Connection is established with a client.
                _issueResponse(() -> {
                    socketServer.callMember("_socketCreated", socket);
                });
            });

            try {
                server.listen(result -> {
                    _issueResponse(() -> {
                        if (result.succeeded()) {
                            socketServer.callMember("emit", "listening", server.actualPort());
                        } else {
                            _error("Failed to start server listening: " + result);
                        }
                    });
                });
            } catch (Throwable ex) {
                _error(socketServer, "Failed to start server listening: " + ex);
            }
        });
    }

    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     *  @return An array of types
     */
    public static String[] supportedReceiveTypes() {
        if (_receiveTypes == null) {
            // Avoid FindBugs LI: Unsynchronized Lazy Initialization (FB.LI_LAZY_INIT_UPDATE_STATIC)
            synchronized (_receiveTypesMutex) {
                if (_receiveTypes == null) {
                    int length = DATA_TYPE.values().length;
                    _receiveTypes = new String[length];
                    int i = 0;
                    for (DATA_TYPE type : DATA_TYPE.values()) {
                        _receiveTypes[i++] = type.toString().toLowerCase();
                    }
                }
            }
        }
        return _receiveTypes;
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments.
     *  @return An array of types
     */
    public static String[] supportedSendTypes() {
        if (_sendTypes == null) {
            // Avoid FindBugs LI: Unsynchronized Lazy Initialization (FB.LI_LAZY_INIT_UPDATE_STATIC)
            synchronized (_sendTypesMutex) {
                if (_sendTypes == null) {
                    int length = DATA_TYPE.values().length;
                    _sendImageTypes = _removeDuplicates(ImageIO.getWriterFormatNames());
                    _sendTypes = new String[length + _sendImageTypes.size()];
                    int i = 0;
                    for (DATA_TYPE type : DATA_TYPE.values()) {
                        _sendTypes[i++] = type.toString().toLowerCase();
                    }
                    for (String imageType : _sendImageTypes) {
                        _sendTypes[i++] = imageType;
                    }
                }
            }
        }
        return _sendTypes;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The array of receive type names. */
    private static String[] _receiveTypes;

    /** A mutex used when creating _receiveTypes. */
    private static Object _receiveTypesMutex = new Object();

    /** The set of informal image type names that can be sent. */
    private static TreeSet<String> _sendImageTypes;
    
    /** The array of send type names. */
    private static String[] _sendTypes;

    /** A mutex used when creating _sendTypes. */
    private static Object _sendTypesMutex = new Object();

    ///////////////////////////////////////////////////////////////////
    ////                     public classes                        ////

    /** Input stream backed by a list of byte arrays. */
    public static class ByteArrayBackedInputStream extends InputStream {
        private LinkedList<byte[]> _list = new LinkedList<byte[]>();
        private int _position, _array;
        public ByteArrayBackedInputStream(byte[] buffer) {
            _list.add(buffer);
            _array = 0;
            _position = 0;
        }
        public void append(byte[] buffer) {
            _list.add(buffer);
            // Reset the stream so the next read starts at the top.
            reset();
        }
        @Override
        public int read() throws IOException {
            byte[] current = _list.get(_array);
            if (_position >= current.length) {
                if (_array < _list.size() - 1) {
                    _array++;
                    _position = 0;
                } else {
                    // No more data.
                    return -1;
                }
            }
            return current[_position++];
        }
        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (_array >= _list.size()) {
                return -1;
            }
            byte[] current = _list.get(_array);
            if (_position >= current.length) {
                if (_array < _list.size() - 1) {
                    _array++;
                    _position = 0;
                } else {
                    // No bytes remaining.
                    return -1;
                }
            }
            int firstCopyLength = Math.min(length, current.length - _position);
            System.arraycopy(current, _position, bytes, offset, firstCopyLength);
            
            _position += firstCopyLength;
            if (_position >= current.length) {
                if (_array < _list.size()) {
                    // Skip to the next array in the list.
                    _array++;
                    _position = 0;
                } else {
                    // Nothing more to read.
                    return firstCopyLength;
                }
            }
            if (firstCopyLength == length) {
                // Read is complete.
                return firstCopyLength;
            }
            int nextCopyLength = read(
                    bytes,
                    offset + firstCopyLength,
                    length - firstCopyLength);
            if (nextCopyLength >= 0) {
                return firstCopyLength + nextCopyLength;
            } else {
                return firstCopyLength;
            }
        }
        @Override
        public void reset() {
            _array = 0;
            _position = 0;
        }
    }

    /** Wrapper for connected TCP sockets.
     *  An instance of this class handles socket events from the
     *  Vert.x NetSocket object and translates them into JavaScript
     *  events emitted by the eventEmitter specified in the constructor.
     *  The events emitted are:
     *  <ul>
     *  <li> close: Emitted when the socket closes. It has no arguments.
     *  <li> error: Emitted when an error occurs. It is passed an error message.
     *  <li> data: Emitted when the socket received data. It is passed the data.
     *  </ul>
     *  This wrapper supports message framing, which ensures that data is emitted
     *  only when a complete message has arrived. To accomplish this, each
     *  message is prepended with a length, in bytes. To minimize overhead for
     *  short messages, then if the length of the message is less than 255 bytes,
     *  the length is encoded in a single byte. Otherwise, it is encoded as a
     *  byte with value 255 followed by an int (four bytes).
     *  
     *  This will only work if both
     *  ends of the socket connection are using this same protocol (e.g.
     *  if both ends are implemented using this same class).
     */
    public class SocketWrapper {

        /** Construct a handler for connections established.
         *  @param eventEmitter The JavaScript object that will emit socket
         *   events.
         *  @param socket The Vertx socket object.
         *  @param sendType The send type.
         *  @param receiveType The receive type.
         *  @param rawBytes If true, send and received raw bytes, with no
         *   message framing. If false, then prepend each sent item with a
         *   length, and for received items, assume received data is prepended
         *   with a length and emit received data only when a complete message
         *   has arrived.
         */
        public SocketWrapper(
                ScriptObjectMirror eventEmitter,
                Object socket,
                String sendType,
                String receiveType,
                boolean rawBytes) {
            _eventEmitter = eventEmitter;
            _rawBytes = rawBytes;
            _socket = (NetSocket)socket;
            
            try {
                _sendType = Enum.valueOf(DATA_TYPE.class, sendType.trim().toUpperCase());
            } catch (Exception ex) {
                // It might be an image type.
                if (_sendImageTypes.contains(sendType)) {
                    _sendImageType = sendType;
                    _sendType = DATA_TYPE.IMAGE;
                } else {
                    throw new IllegalArgumentException("Invalid send data type: " + sendType);
                }
            }

            try {
                _receiveType = Enum.valueOf(DATA_TYPE.class, receiveType.trim().toUpperCase());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid receive data type: " + receiveType);
            }

            // Set up handlers for data, errors, etc.
            // Do this in the verticle.
            submit(() -> {
                _socket.closeHandler((Void) -> {
                    _issueResponse(() -> {
                        _eventEmitter.callMember("emit", "close");
                    });
                });
                _socket.drainHandler((Void) -> {
                    synchronized(SocketWrapper.this) {
                        // This should unblock send(),
                        SocketWrapper.this.notifyAll();
                    }
                });
                _socket.exceptionHandler(throwable -> {
                    _error(_eventEmitter, throwable.toString());
                });
                // Handler for received data.
                _socket.handler(buffer -> {
                    _processBuffer(buffer);
                });
            });
        }
        /** Close the socket.
         */
        public void close() {
            submit(() -> {
                _socket.close();
            });
            synchronized(SocketWrapper.this) {
                _closed = true;
            }
        }
        /** Send data over the socket.
         *  @param data The data to send.
         */
        public void send(final Object data) {
            synchronized(SocketWrapper.this) {
                if (_closed) {
                    _error(_eventEmitter, "Socket is closed. Cannot send data.");
                    return;
                }
                // Block if the send buffer is full.
                // Note that this should be called in the director thread, not
                // in the Vert.x thread, so blocking is OK. We need to stall
                // execution of the model to not get ahead of the capability.
                while(_socket.writeQueueFull()) {
                    try {
                        _actor.log("WARNING: Send buffer is full. Stalling to allow it to drain.");
                        SocketWrapper.this.wait();
                    } catch (InterruptedException e) {
                        _error(_eventEmitter, "Buffer is full, and wait for draining was interrupted");
                    }
                }
            }

            submit(() -> {
                Buffer buffer = Buffer.buffer();
                // Handle the case where data is an array.
                if (data instanceof Object[]) {
                    for (Object element : (Object[]) data) {
                        // JavaScript arrays can have holes, and moreover,
                        // it seems that Nashorn's Java.to() function creates
                        // a bigger array than needed with trailing null elements.
                        if (element != null) {
                            _appendToBuffer(element, _sendType, _sendImageType, buffer);
                        }
                    }
                } else {
                    _appendToBuffer(data, _sendType, _sendImageType, buffer);
                }
                if (!_rawBytes) {
                    // Prepend the buffer with message length information.
                    // Note that unlike the WebSocket standard, we don't need
                    // to break the message into frames. The underlying TCP
                    // implementation will do that.
                    int length = buffer.length();
                    Buffer newBuffer = Buffer.buffer();
                    if (length < 255) {
                        // The cast will extract the low order 8 bits.
                        // Note that the appended byte is interpreted by Java as
                        // being signed, but this does matter, because on the
                        // receiving end we will interpret it as unsigned.
                        newBuffer.appendByte((byte)length);
                    } else {
                        newBuffer.appendByte((byte)0xFF);
                        newBuffer.appendInt(length);
                    }
                    newBuffer.appendBuffer(buffer);
                    buffer = newBuffer;
                }
                _socket.write(buffer);
            });
        }
        /** Extract a length from the head of the buffer. Return the number of
         *  bytes encoding the length (1 or 5) or -1 if there are not enough bytes
         *  in the buffer yet to encode a length. As a side effect, this will set
         *  _expectedLength to the expected length, or to -1 if the length has not
         *  yet been determined.
         *  @param buffer The buffer.
         *  @return The number of bytes encoding the length, or -1 if there aren't
         *   enough bytes in the buffer.
         */
        private int _extractLength(Buffer buffer) {
            _expectedLength = buffer.getByte(0) & 0xFF;
            if (_expectedLength == 0xFF) {
                // May not have an additional four bytes yet.
                if (buffer.length() > 4) {
                    _expectedLength = buffer.getInt(1);
                    return 5;
                } else {
                    _expectedLength = -1;
                    return -1;
                }
            }
            return 1;
        }
        /** Process new buffer data.
         *  @param buffer The buffer, or null to process previously received data.
         */
        private void _processBuffer(Buffer buffer) {
            if (!_rawBytes) {
                // Only issue a response when a complete message has arrived.
                Buffer residual = null;
                if (_partialBuffer == null) {
                    // No prior data has arrived.
                    if (buffer == null || buffer.length() == 0) {
                        // No new data. Nothing to do.
                        return;
                    }
                    int bytesEncodingLength = _extractLength(buffer);
                    if (bytesEncodingLength > 0) {
                        // The length is known.
                        // It is not documented in Vertx, but apparently a sliced buffer
                        // cannot be appended to, despite being a Buffer.
                        _partialBuffer = Buffer.buffer();
                        _partialBuffer.appendBuffer(buffer.slice(bytesEncodingLength, buffer.length()));
                    } else {
                        // The length is not yet known.
                        _partialBuffer = buffer;
                        return;
                    }
                } else {
                    // Previous partial data has arrived.
                    // Check whether the length is known yet.
                    if (_expectedLength <= 0) {
                        // Length is not known yet.
                        if (buffer == null) {
                            // No new data. Nothing to do.
                            return;
                        }
                        Buffer temporaryBuffer = Buffer.buffer();
                        temporaryBuffer.appendBuffer(_partialBuffer);
                        temporaryBuffer.appendBuffer(buffer);
                        if (temporaryBuffer.length() == 0) {
                            // Nothing to do.
                            return;
                        }
                        int bytesEncodingLength = _extractLength(temporaryBuffer);
                        if (bytesEncodingLength > 0) {
                            // The length is now known.
                            // The length encoding must be 5 bytes long, some of which is in _partialBuffer.
                            int additionalLengthInfo = 5 - _partialBuffer.length();
                            buffer = buffer.slice(additionalLengthInfo, buffer.length());
                            // No longer need any prior bytes.
                            _partialBuffer = Buffer.buffer();
                        } else {
                            // The length is not yet known.
                            _partialBuffer.appendBuffer(buffer);
                            return;
                        }
                    }
                    // How many bytes are still needed?
                    int stillNeed = _expectedLength - _partialBuffer.length();
                    if (buffer != null) {
                        // Have new data.
                        int contributing = stillNeed;
                        if (buffer.length() < stillNeed) {
                            contributing = buffer.length();
                        }
                        if (buffer.length() <= stillNeed) {
                            // The entire received buffer contributes to the current message.
                            _partialBuffer.appendBuffer(buffer);
                        } else {
                            // Only part of the received buffer contributes to the current message.
                            // NOTE: This assumes that the second argument to slice() is the index
                            // of the byte AFTER the one in the slice. This is not documented
                            // in Vert.x.
                            _partialBuffer.appendBuffer(buffer.slice(0, contributing));
                            // Put the unused data in the residual buffer.
                            int bufferLength = buffer.length();
                            residual = buffer.slice(contributing, bufferLength);
                        }
                    } else if (_partialBuffer.length() > stillNeed) {
                        // Don't have new data, but the old data contains more
                        // than one message.
                        // Since buffer == null, this is being called recursively to
                        // process additional messages in the buffer.
                        // Note that stillNeed can be negative.
                        residual = _partialBuffer.slice(_expectedLength, _partialBuffer.length());
                        _partialBuffer = _partialBuffer.slice(0, _expectedLength);
                    }
                }
                // At this point, _partialBuffer has all received data contributing to the
                // current message, and residual has any left over data.
                buffer = _partialBuffer;
                if (buffer.length() < _expectedLength) {
                    // Have not yet received all the data. Do not emit data.
                    return;
                }
                // All data for the current message has been received and is in buffer.
                if (residual != null && residual.length() > 0) {
                    // Additional data beyond the current message has also arrived.
                    // Extract the length for the next segment.
                    int bytesEncodingLength = _extractLength(residual);
                    // It is not documented in Vertx, but apparently a sliced buffer
                    // cannot be appended to, despite being a Buffer.
                    // So we need a new buffer.
                    _partialBuffer = Buffer.buffer();
                    if (bytesEncodingLength > 0) {
                        // The length is known.
                        _partialBuffer.appendBuffer(residual.slice(bytesEncodingLength, residual.length()));
                    } else {
                        // The length is not yet known.
                        _partialBuffer.appendBuffer(residual);
                    }
                } else {
                    // Indicate that we have no partial message.
                    _partialBuffer = null;
                }
            }
            // We have a complete message. Issue a response.
            final Buffer finalBuffer = buffer;
            _issueResponse(() -> {
                if(_receiveType == DATA_TYPE.STRING) {
                    _eventEmitter.callMember("emit", "data", finalBuffer.getString(0, finalBuffer.length()));
                } else if (_receiveType == DATA_TYPE.IMAGE) {
                    try {
                        byte[] bytes = finalBuffer.getBytes();
                        if (_byteStream == null) {
                            _byteStream = new ByteArrayBackedInputStream(bytes);
                            _bufferCount = 0;
                        } else {
                            _bufferCount++;
                            _actor.log("WARNING: Cannot parse image from data received. Waiting for more data:"
                                    + _bufferCount);
                            // Append the current buffer to previously received buffer(s).
                            _byteStream.append(bytes);
                        }
                        // If we are not doing image framing, then there is no assurance at this point
                        // that we have a complete image. Thus, we emit a byte array and not an image
                        // token.
                        if (_rawBytes) {
                            _eventEmitter.callMember("emit", "data", bytes);
                        } else {
                            BufferedImage image = ImageIO.read(_byteStream);
                            _byteStream = null;
                            if (image != null && image.getHeight() > 0 && image.getWidth() > 0) {
                                if (_bufferCount > 1) {
                                    _actor.log("Image received over " + _bufferCount
                                            + " buffers. Consider increasing buffer size.");
                                }
                                ImageToken token = new AWTImageToken(image);
                                _eventEmitter.callMember("emit", "data", token);
                            } else {
                                _error(_eventEmitter, "Received corrupted image.");
                            }
                        }
                    } catch (IOException e) {
                        _error(_eventEmitter, "Failed to read incoming image: " + e.toString());
                    }
                } else {
                    // Assume a numeric type.
                    int size = _sizeOfType(_receiveType);
                    int length = finalBuffer.length();
                    int numberOfElements = length / size;
                    if (numberOfElements == 1) {
                        _eventEmitter.callMember("emit", "data", _extractFromBuffer(finalBuffer, _receiveType, 0));
                    } else if (numberOfElements > 1) {
                        if (_rawBytes) {
                            int position = 0;
                            for (int i = 0; i < numberOfElements; i++) {
                                _eventEmitter.callMember("emit", "data", _extractFromBuffer(finalBuffer, _receiveType, position));
                                position += size;
                            }
                        } else {
                            // Using message framing, so we output a single array.
                            Object[] result = new Object[numberOfElements];
                            int position = 0;
                            for (int i = 0; i < result.length; i++) {
                                result[i] = _extractFromBuffer(finalBuffer, _receiveType, position);
                                position += size;
                            }
                            // NOTE: If we return result, then the emitter will not
                            // emit a native JavaScript array. We have to do a song and
                            // dance here which is probably very inefficient (almost
                            // certainly... the array gets copied).
                            try {
                                _eventEmitter.callMember("emit", "data", _actor.toJSArray(result));
                            } catch (Exception e) {
                                _error(_eventEmitter, "Failed to convert to a JavaScript array: "
                                        + e);                    
                                _eventEmitter.callMember("emit", "data", result);
                            }
                        }
                    } else if (numberOfElements <= 0) {
                        _error(_eventEmitter, "Expect to receive type "
                                + _receiveType
                                + ", but received an insufficient number of bytes: "
                                + finalBuffer.length());
                    }
                }
            });
            if (_partialBuffer != null 
                    && _expectedLength > 0 
                    && _partialBuffer.length() >= _expectedLength) {
                // There is at least one more complete message in the buffer.
                // In the following, the null argument indicates that there no
                // new data.
                _processBuffer(null);
            }
        }
        private int _bufferCount = 0;
        private boolean _closed = false;
        private int _expectedLength;
        private Buffer _partialBuffer;
        private boolean _rawBytes;
        private NetSocket _socket;
        private ByteArrayBackedInputStream _byteStream;
        private ScriptObjectMirror _eventEmitter;
        private DATA_TYPE _receiveType;
        private String _sendImageType;
        private DATA_TYPE _sendType;
    }
}