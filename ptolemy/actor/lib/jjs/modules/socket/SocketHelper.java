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
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.data.LongToken;



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
   unsigned byte, unsigned int, and unsigned short.
   At this time, an image will be encoded using JPG only for transmission.
   This may be generalized sometime in the future.

   @author Edward A. Lee
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
                .setTcpNoDelay((Boolean)options.get("noDelay"));
        
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
                    _error(socketClient, "Failed to connect: " + response.cause().getMessage());
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

            server.listen(result -> {
                _issueResponse(() -> {
                    if (result.succeeded()) {
                        socketServer.callMember("emit", "listening", server.actualPort());
                    } else {
                        _error("Failed to start server listening.");
                    }
                });
            });
        });
    }

    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     */
    public static String[] supportedReceiveTypes() {
        int length = DATA_TYPE.values().length;
        String[] result = new String[length];
        int i = 0;
        for (DATA_TYPE type : DATA_TYPE.values()) {
            result[i++] = type.toString().toLowerCase();
        }
        return result;
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments.
     */
    public static String[] supportedSendTypes() {
        return supportedReceiveTypes();
    }
    
    /** Support data types for send and receive. */
    public static enum DATA_TYPE {
        BYTE,
        DOUBLE,
        FLOAT,
        IMAGE,
        INT,
        LONG,
        NUMBER,
        SHORT,
        STRING,
        UNSIGNEDBYTE,
        UNSIGNEDINT,
        UNSIGNEDSHORT
    };

    ///////////////////////////////////////////////////////////////////
    ////                     public classes                        ////

    /** Input stream backed by a list of byte arrays. */
    public class ByteArrayBackedInputStream extends InputStream {
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
     */
    public class SocketWrapper {

        /** Construct a handler for connections established.
         *  @param eventEmitter The JavaScript object that will emit socket
         *   events.
         *  @param socket The Vertx socket object.
         *  @param sendType The send type.
         *  @param receiveType The receive type.
         */
        public SocketWrapper(
                ScriptObjectMirror eventEmitter,
                Object socket,
                String sendType,
                String receiveType,
                final boolean serializeReceivedArray) {
            _eventEmitter = eventEmitter;
            _socket = (NetSocket)socket;
            
            try {
                _sendType = Enum.valueOf(DATA_TYPE.class, sendType.trim().toUpperCase());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid data type: " + sendType);
            }

            try {
                _receiveType = Enum.valueOf(DATA_TYPE.class, receiveType.trim().toUpperCase());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid data type: " + receiveType);
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
                _socket.endHandler((Void) -> {
                    // End event on the socket triggers a close of the socket.
                    // This gets called when the remote side sends a FIN packet.
                    // FIXME: This isn't right. Need FIN from both ends to close the socket!
                    // _client.close();

                    // In case a send is blocked, unblock it.
                    synchronized(this) {
                        _closed = true;
                        notifyAll();
                    }
                });
                _socket.exceptionHandler(throwable -> {
                    _error(_eventEmitter, throwable.toString());
                });
                _socket.handler(buffer -> {
                    _issueResponse(() -> {
                        if(_receiveType == DATA_TYPE.STRING) {
                            _eventEmitter.callMember("emit", "data", buffer.getString(0, buffer.length()));
                        } else if (_receiveType == DATA_TYPE.IMAGE) {
                            try {
                                byte[] bytes = buffer.getBytes();
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
                                // FIXME: JPG image at least ends with byte -39.
                                // Not sure about others. If any stream comes in that never ends in -39,
                                // then this will fail to produce any more images and will use up memory.
                                if (bytes[bytes.length - 1] == (byte)-39) {
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
                                        _eventEmitter.callMember("emit", "error", "Received corrupted image.");
                                    }
                                }
                            } catch (IOException e) {
                                _error(_eventEmitter, "Failed to read incoming image: " + e.toString());
                            }
                        } else {
                            // Assume a numeric type.
                            int size = _sizeOfReceiveType();
                            int length = buffer.length();
                            int numberOfElements = length / size;
                            if (numberOfElements == 1) {
                                _eventEmitter.callMember("emit", "data", _extractFromBuffer(buffer, 0));
                            } else if (numberOfElements > 1) {
                                if (serializeReceivedArray) {
                                    int position = 0;
                                    for (int i = 0; i < numberOfElements; i++) {
                                        _eventEmitter.callMember("emit", "data", _extractFromBuffer(buffer, position));
                                        position += size;
                                    }
                                } else {
                                    // Not serializing the output, so we output a single array.
                                    Object[] result = new Object[numberOfElements];
                                    int position = 0;
                                    for (int i = 0; i < result.length; i++) {
                                        result[i] = _extractFromBuffer(buffer, position);
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
                                        + buffer.length());
                            }
                        }
                    });
                });
            });
        }
        /** Close the socket.
         */
        public void close() {
            submit(() -> {
                _socket.close();
            });
        }
        /** Send data over the socket.
         *  @param data The data to send.
         */
        public void send(final Object data) {
            // Block if the send buffer is full.
            // Note that this should be called in the director thread, not
            // in the Vert.x thread, so blocking is OK. We need to stall
            // execution of the model to not get ahead of the capability.
            while(_socket.writeQueueFull() && !_closed) {
                synchronized(SocketWrapper.this) {
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
                            _appendToBuffer(element, buffer);
                        }
                    }
                } else {
                    _appendToBuffer(data, buffer);
                }
                _socket.write(buffer);
            });
        }
        /** Append data to be sent to the specified buffer.
         *  @param data The data to send.
         *  @param buffer The buffer.
         */
        private void _appendToBuffer(final Object data, Buffer buffer) {
            if (_sendType.equals(DATA_TYPE.STRING)) {
                // NOTE: Use of toString() method makes this very tolerant, but
                // it won't properly stringify JSON. Is this OK?
                // NOTE: A second argument could take an encoding.
                // Defaults to UTF-8. Is this OK?
                buffer.appendString(data.toString());
            } else if (_sendType.equals(DATA_TYPE.IMAGE)) {
                if (data instanceof ImageToken) {
                    Image image = ((ImageToken)data).asAWTImage();
                    if (image == null) {
                        _error(_eventEmitter, "Empty image received: " + data);
                        return;
                    }
                    if (!(image instanceof BufferedImage)) {
                        _error(_eventEmitter, "Unsupported image token type: " + image.getClass());
                        return;
                    }
                    // FIXME: Image types for sending given by ImageIO.getReaderFormatNames();
                    String imageType = "jpg";
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write((BufferedImage)image, imageType, stream);
                    } catch (IOException e) {
                        _error("Failed to convert image to byte array for sending: " + e.toString());
                    }
                    byte[] imageBytes = stream.toByteArray();
                    buffer.appendBytes(imageBytes);
                } else {
                    _error(_eventEmitter, "Expected image to send, but got "
                            + data.getClass().getName());
                }
            } else {
                _appendNumericToBuffer(buffer, data);
            }
        }
        /** Append a numeric instance of _sendType to a buffer. */
        private void _appendNumericToBuffer(Buffer buffer, Object data) {
            if (data instanceof Number) {
                switch(_sendType) {
                case BYTE:
                    buffer.appendByte(((Number)data).byteValue());
                    break;
                case DOUBLE:
                case NUMBER:
                    buffer.appendDouble(((Number)data).doubleValue());
                    break;
                case FLOAT:
                    buffer.appendFloat(((Number)data).floatValue());
                    break;
                case INT:
                    buffer.appendInt(((Number)data).intValue());
                    break;
                case LONG:
                    buffer.appendLong(((Number)data).longValue());
                    break;
                case SHORT:
                    buffer.appendShort(((Number)data).shortValue());
                    break;
                case UNSIGNEDBYTE:
                    // Number class can't extract an unsigned byte, so we use short.
                    buffer.appendUnsignedByte(((Number)data).shortValue());
                    break;
                case UNSIGNEDINT:
                    // Number class can't extract an unsigned int, so we use long.
                    buffer.appendUnsignedInt(((Number)data).longValue());
                    break;
                case UNSIGNEDSHORT:
                    // Number class can't extract an unsigned short, so we use int.
                    buffer.appendUnsignedShort(((Number)data).intValue());
                    break;
                default:
                    _error(_eventEmitter, "Unsupported type for socket: "
                            + _sendType.toString()); 
                }
            } else if (data instanceof LongToken) {
                // JavaScript has no long data type, and long is not convertible to
                // "number" (which is double), so the Ptolemy host will pass in a
                // LongToken.  Handle this specially.
                buffer.appendLong(((LongToken)data).longValue());
            } else {
                _sendTypeError(_sendType, data);
            }
        }
        /** Extract a numeric instance of the _receiveType from a buffer. */
        private Object _extractFromBuffer(Buffer buffer, int position) {
            try {
                switch(_receiveType) {
                case BYTE:
                    return buffer.getByte(position);
                case DOUBLE:
                case NUMBER:
                    return buffer.getDouble(position);
                case FLOAT:
                    return buffer.getFloat(position);
                case INT:
                    return buffer.getInt(position);
                case LONG:
                    // Note that long is not representable in JavaScript.
                    // Hence, we return a LongToken.
                    long result = buffer.getLong(position);
                    return new LongToken(result);
                case SHORT:
                    return buffer.getShort(position);
                case UNSIGNEDBYTE:
                    return buffer.getUnsignedByte(position);
                case UNSIGNEDINT:
                    return buffer.getUnsignedInt(position);
                case UNSIGNEDSHORT:
                    return buffer.getUnsignedShort(position);
                default:
                    _error(_eventEmitter, "Unsupported type for socket: "
                            + _receiveType.toString());
                    return null;
                }
            } catch (Throwable ex) {
                _receiveTypeError(ex, _receiveType, buffer);
                return null;
            }
        }
        private void _receiveTypeError(Throwable ex, DATA_TYPE type, Buffer buffer) {
            String expectedType = type.toString().toLowerCase();
            // ex.printStackTrace();
            _error(_eventEmitter, "Received data that is not of type "
                    + expectedType
                    + ": "
                    + buffer.toString()
                    + "\nException occurred: "
                    + ex);
        }
        private void _sendTypeError(DATA_TYPE type, Object data) {
            String expectedType = type.toString().toLowerCase();
            _error(_eventEmitter, "Data to send is not a "
                    + expectedType
                    + ". It is: "
                    + data.getClass().getName());
        }
        private int _sizeOfReceiveType() {
            switch(_receiveType) {
            case BYTE:
                return Byte.BYTES;
            case DOUBLE:
            case NUMBER:
                return Double.BYTES;
            case FLOAT:
                return Float.BYTES;
            case INT:
                return Integer.BYTES;
            case LONG:
                return Long.BYTES;
            case SHORT:
                return Short.BYTES;
            case UNSIGNEDBYTE:
                return Byte.BYTES;
            case UNSIGNEDINT:
                return Integer.BYTES;
            case UNSIGNEDSHORT:
                return Short.BYTES;
            default:
                _error(_eventEmitter, "Unsupported type for socket: "
                        + _receiveType.toString());
                return 0;
            }
        }
        private int _bufferCount = 0;
        private boolean _closed = false;
        private NetSocket _socket;
        private ByteArrayBackedInputStream _byteStream;
        private ScriptObjectMirror _eventEmitter;
        private DATA_TYPE _sendType;
        private DATA_TYPE _receiveType;
    }
}