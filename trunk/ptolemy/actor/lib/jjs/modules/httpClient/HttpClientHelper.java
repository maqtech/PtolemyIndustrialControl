/* A JavaScript helper for HttpClient.

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
package ptolemy.actor.lib.jjs.modules.httpClient;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import ptolemy.actor.lib.jjs.modules.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// HttpClientHelper

/**
   A helper class for the HttpClient module in JavaScript.

   @author Marten Lohstroh, Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
 */
public class HttpClientHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a HttpClientHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  The options argument can be a string URL
     *  or a map with the following fields (this helper class assumes
     *  all fields are present, so please be sure they are):
     *  <ul>
     *  <li> headers: An object containing request headers. By default this
     *       is an empty object. Items may have a value that is an array of values,
     *       for headers with more than one value.
     *  <li> keepAlive: A boolean that specified whether to keep sockets around
     *       in a pool to be used by other requests in the future. This defaults to false.
     *  <li> method: A string specifying the HTTP request method.
     *       This defaults to 'GET', but can also be 'PUT', 'POST', 'DELETE', etc.
     *  <li> url: A string that can be parsed as a URL, or an object containing
     *       the following fields:
     *       <ul>
     *       <li> host: A string giving the domain name or IP address of
     *            the server to issue the request to. This defaults to 'localhost'.
     *       <li> path: Request path as a string. This defaults to '/'. This can
     *            include a query string, e.g. '/index.html?page=12', or the query
     *            string can be specified as a separate field (see below).
     *            An exception is thrown if the request path contains illegal characters.
     *       <li> protocol: The protocol. This is a string that defaults to 'http'.
     *       <li> port: Port of remote server. This defaults to 80.
     *       <li> query: A query string to be appended to the path, such as '?page=12'.
     *       </ul>
     *  </ul>
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param options The options.
     *  @return A new HttpClientHelper instance.
     */
    public static HttpClientHelper createHttpClient(
            ScriptObjectMirror currentObj, Map<String, Object> options) {
        return new HttpClientHelper(currentObj, options);
    }

    /** End a request. */
    public void end() {
        if (_request != null) {
            _request.end();
        }
    }

    /** Stop a response. */
    public void stop() {
        if (_response != null) {
            // FIXME: There seems to be no way to stop this stream!!!
            // Even closing the socket doesn't stop the flow from the camera and the invocation of the callback.
            // See FIXME below.
            _response.netSocket().close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor to open an HTTP client.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param address The URL of the WebSocket host with an optional port number
     *   (e.g. 'ws://localhost:8000'). If no port number is given, 80 is used.
     */
    private HttpClientHelper(ScriptObjectMirror currentObj,
            Map<String, Object> options) {
        super(currentObj);
        HttpClient client = _vertx.createHttpClient();

        // NOTE: Vert.x documentation states about HttpClient:
        // "If an instance is instantiated from some other arbitrary Java thread
        // (i.e. when running embedded) then and event loop will be assigned
        // to the instance and used when any of its handlers are called."
        // Hence, the HttpClient we just created will be assigned its own
        // event loop. We need to ensure that callbacks are mutually exclusive
        // with other Java code here.

        Map<String, Object> urlSpec = (Map<String, Object>) options.get("url");

        client.setHost((String) urlSpec.get("host"));
        client.setPort((int) urlSpec.get("port"));
        client.exceptionHandler(new HttpClientExceptionHandler());
        if ((boolean) options.get("keepAlive")) {
            client.setKeepAlive(true);
        }
        // FIXME: Provide a timeout. Use setTimeout() of the client.

        String query = "";
        Object queryObject = urlSpec.get("query");
        if (queryObject != null) {
            String querySpec = queryObject.toString().trim();
            if (!querySpec.equals("") && !querySpec.startsWith("?")) {
                query = "?" + querySpec;
            }
        }

        // NOTE: Documentation of Vertx 2.15 is wrong.
        // The argument is a path with a query, not a URI.
        String uri = urlSpec.get("path") + query;
        
        String protocol = (String) urlSpec.get("protocol");

        // FIXME: How do we set the protocol?
        // It is specified in urlSpec.get("protocol").
        
        // If https, client should use SSL
        if (urlSpec.get("protocol").toString().equalsIgnoreCase("https")) {
            client.setSSL(true);
        }

        Object complete = options.get("outputCompleteResponseOnly");
        if (complete instanceof Boolean && !(Boolean) complete) {
            _outputCompleteResponseOnly = false;
        }

        _request = client.request((String) options.get("method"), uri,
                new HttpClientResponseHandler());

        // Handle the headers.
        Map headers = (Map) options.get("headers");
        if (!headers.isEmpty()) {
            for (Object key : headers.keySet()) {
                Object value = headers.get(key);
                if (value instanceof String) {
                    _request.putHeader((String) key, (String) value);
                } else if (value instanceof Integer) {
                    _request.putHeader((String) key,
                            ((Integer) value).toString());
                } else if (value instanceof Iterable) {
                    _request.putHeader((String) key, (Iterable<String>) value);
                }
            }
        }

        // Handle the body, if present.
        String body = (String) options.get("body");
        if (body != null) {
            _request.write(body);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Boolean indicating whether outputting partial responses is permitted. */
    private boolean _outputCompleteResponseOnly = true;

    /** The request built in the constructor. */
    private HttpClientRequest _request;

    /** The current response, which may be streaming data, or null if there is no active response. */
    private HttpClientResponse _response;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The event handler that is triggered when an error occurs in the HTTP connection.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized (_actor) {
                _currentObj.callMember("emit", "error", throwable.getMessage());
            }
        }
    }

    /** The event handler that is triggered when a response arrives from the server.
     */
    private class HttpClientResponseHandler implements
            Handler<HttpClientResponse> {
        private List<byte[]> _imageParts;
        private String _boundary;
        private boolean _inSegment;

        @Override
        public void handle(HttpClientResponse response) {
            synchronized (_actor) {
                _response = response;
                // The response is not yet complete, but we have some information.
                int status = response.statusCode();
                if (status >= 400) {
                    // An error occurred.
                    _currentObj.callMember("emit", "error",
                            "Request failed with code " + status + ": "
                                    + response.statusMessage());
                    return;
                }
                MultiMap headers = response.headers();

                // If the response is a redirect, handle that here.
                if (status >= 300 && status <= 308 && status != 306) {
                    String newLocation = headers.get("Location");
                    if (newLocation != null) {
                        // FIXME: How to handle the redirect?
                        _currentObj.callMember("emit", "error", "Redirect to "
                                + newLocation
                                + " not yet handled by HttpClientHelper. "
                                + status + ": " + response.statusMessage());
                        return;
                    }
                }

                String contentType = headers.get("Content-Type");
                boolean isText = (contentType == null)
                        || (contentType.startsWith("text"))
                        || (contentType.startsWith("application/json"));
                boolean isMultipart = (contentType != null)
                        && (contentType.startsWith("multipart"));
                if (isMultipart) {
                    int index = contentType.indexOf("=");
                    if (index > 0) {
                        _boundary = "--"
                                + contentType.substring(index + 1).trim();
                    }
                }

                // FIXME: The Content-Type might be something like
                // multipart/x-mixed-replace;boundary=ipcamera, in which case,
                // we really need to be chunking the data rather than using a
                // bodyHandler.

                if (_outputCompleteResponseOnly) {
                    // A bodyHandler is invoked after the response is complete.
                    // Note that large responses could create a memory problem here.
                    // Could look at the Content-Length header.
                    response.bodyHandler(new Handler<Buffer>() {
                        public void handle(Buffer body) {
                            synchronized (_actor) {
                                if (isText) {
                                    _currentObj.callMember("_response",
                                            response, body.toString());
                                } else if (contentType.startsWith("image")) {
                                    InputStream stream = new ByteArrayInputStream(
                                            body.getBytes());
                                    try {
                                        Image image = ImageIO.read(stream);
                                        Token token = new AWTImageToken(image);
                                        _currentObj.callMember("_response",
                                                response, token);
                                    } catch (IOException e) {
                                        // FIXME: What to do here?
                                        _currentObj.callMember("_response",
                                                response, body.getBytes());
                                    }
                                } else {
                                    // FIXME: Need to handle other MIME types.Z
                                    _currentObj.callMember("_response",
                                            response, body.getBytes());
                                }
                                _response = null;
                            }
                        }
                    });
                } else {
                    response.endHandler(new Handler() {
                        public void handle(Object body) {
                            synchronized (_actor) {
                                _response = null;
                            }
                        }
                    });
                    _imageParts = new LinkedList<byte[]>();
                    _inSegment = false;
                    response.dataHandler(new Handler<Buffer>() {
                        public void handle(Buffer body) {
                            // FIXME: There seems to be no way to stop this stream!!!
                            // This function gets invoked even after the model stops!
                            synchronized (_actor) {
                                if (isText) {
                                    _currentObj.callMember("_response",
                                            response, body.toString());
                                } else if (isMultipart) {
                                    byte[] data = body.getBytes();
                                    String dataAsString = body.toString();
                                    int boundaryIndex = dataAsString
                                            .indexOf(_boundary);
                                    if (boundaryIndex >= 0) {
                                        // The data contains a boundary.  If we are in
                                        // a segment, finish it.
                                        if (_inSegment) {
                                            if (boundaryIndex > 1) {
                                                // There is additional data in this segment.
                                                byte[] prefix = new byte[boundaryIndex];
                                                System.arraycopy(data, 0,
                                                        prefix, 0,
                                                        boundaryIndex - 1);
                                                _imageParts.add(prefix);

                                                // Construct one big byte array.
                                                int length = 0;
                                                for (byte[] piece : _imageParts) {
                                                    length += piece.length;
                                                }
                                                byte[] imageBytes = new byte[length];
                                                int position = 0;
                                                for (byte[] piece : _imageParts) {
                                                    System.arraycopy(piece, 0,
                                                            imageBytes,
                                                            position,
                                                            piece.length);
                                                    position += piece.length;
                                                }

                                                // Have a complete image.
                                                InputStream stream = new ByteArrayInputStream(
                                                        imageBytes);
                                                try {
                                                    Image image = ImageIO
                                                            .read(stream);
                                                    if (image != null) {
                                                        Token token = new AWTImageToken(
                                                                image);
                                                        _currentObj
                                                                .callMember(
                                                                        "_response",
                                                                        response,
                                                                        token);
                                                        System.out
                                                                .println("Sent an image.");
                                                    } else {
                                                        System.err
                                                                .println("Input data is apparently not an image.");
                                                    }
                                                } catch (IOException e) {
                                                    // FIXME: What to do here?
                                                    _currentObj.callMember(
                                                            "_response",
                                                            response,
                                                            body.getBytes());
                                                }
                                            }
                                        }
                                        // Since there is a boundary string, we are definitely in the segment.
                                        _inSegment = true;
                                        _imageParts.clear();
                                        // Skip to character 255, the start of a jpeg image.
                                        // (in signed notation, -1).
                                        int start = boundaryIndex
                                                + _boundary.length() + 2;
                                        while (start < data.length
                                                && data[start] != -1) {
                                            start++;
                                        }
                                        if (start < data.length) {
                                            byte[] segment = new byte[data.length
                                                    - start];
                                            System.arraycopy(data, start,
                                                    segment, 0, segment.length);
                                            _imageParts.add(segment);
                                        }
                                    } else {
                                        // data does not contain a boundary.
                                        if (_inSegment) {
                                            _imageParts.add(data);
                                        }
                                    }

                                } else {
                                    // FIXME: Need to handle other MIME types.Z
                                    _currentObj.callMember("_response",
                                            response, body.getBytes());
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}
