/**
 * Module for HTTP clients.
 * A simple use of this module is to request a web page and print its contents, as
 * illustrated by the following example:
 * <pre>
 *    var httpClient = require('httpClient');
 *    httpClient.get('http://accessors.org', function(message) {
 *        print(message.body);
 *    });
 * </pre>
 * Both http and https are supported.

 * @module httpClient
 * @author Marten Lohstroh and Edward A. Lee
 * @version $Id$
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */

// Java types used.
var HttpClientHelper = Java.type('ptolemy.actor.lib.jjs.modules.httpClient.HttpClientHelper');
var URL = Java.type('java.net.URL'); // FIXME: eventually, have a url module for this

var EventEmitter = require('events').EventEmitter;

/** Issue an HTTP request and provide a callback function for responses.
 *  The callback is a function that is passed an instance of IncomingMessage,
 *  defined here. This function returns an instance of ClientRequest, also defined here.
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
 *  @param options The options or URL.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 *  @return An instance of ClientRequest.
 */
exports.request = function(options, responseCallback) {
  return new ClientRequest(options, responseCallback);
};

// NOTE: Perhaps options should include a localAddress: A string giving a name or 
// IP address for the local network interface to use
// for network connections. This defaults to 'localhost', but on machines with more than one
//  network interface (e.g. WiFi and Ethernet), you may need to specify which one to use.
// Marten, 05/21/2015: I'm not sure if I understand the problem (or solution) here.
// Depending on the IP address, the host's routing table will
// determine which device will be used. The name 'localhost' does not signify a 
// network interface but a hostname.

// NOTE: Node has keepAliveMsecs, but I don't see anything like it in Vert.x
// When using HTTP KeepAlive, this is an integer that specifies
// how often (in milliseconds) to send a TCP KeepAlive packet over sockets being kept alive.
// This defaults 1000 and is only relevant if keepAlive is set to true.

// NOTE: The following options are supported by http.request() in Node.js, but not here,
// or at least not tested (yet):
// hostname: To support url.parse() hostname is preferred over host
// socketPath: Unix Domain Socket (use one of host:port or socketPath)
// auth: Basic authentication i.e. 'user:password' to compute an Authorization header.
// agent: Controls Agent behavior. When an Agent is used request will default to Connection: keep-alive. Possible values:
// undefined (default): use global Agent for this host and port.
// Agent object: explicitly use the passed in Agent.
// false: opts out of connection pooling with an Agent, defaults request to Connection: close.

/** Convenience method to issue an HTTP GET.  This just calls request() and then
 *  calls end() on the object returned by request(). It returns the object returned
 *  by request() (an instance of ClientRequest). See request() for documentation of
 *  the arguments.
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 */
exports.get = function(options, reponseCallback) {
  var request = exports.request(options, reponseCallback);
  request.end();
  return request;
};

// NOTE: The following events are produced by ClientRequest in Node.js
// From: http.ClientRequest 
// Event: 'response'
// Event: 'socket'
// Event: 'connect'
// Event: 'upgrade'
// Event: 'continue'
// From stream.Writeable
// Event: 'finish'
// Event: 'pipe'
// Event: 'unpipe'
// Event: 'error'

/** The object type returned by the request function.
 *  This object type provides the following functions:
 *  <ul>
 *  <li> end(): Call this to end the request. </li>
 *  <li> write(''data'', ''encoding''): Write data (e.g. for a POST request). </li>
 *  </ul>
 *  See the documentation of the request function for an explanation of the arguments.
 *  This is an event emitter that emits the following events:
 *  <ul>
 *  <li> 'error': If an error occurs. The message is passed as an argument. </li>
 *  <li> 'response': A response is received from the server. This event is automatically
 *       handled by calling responseCallback, if responseCallback is not null.</li>
 *  </ul>
 *  @constructor
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 */
function ClientRequest(options, reponseCallback) {
  var self = this;
  
  var defaultOptions = {
    'headers':{},
    'keepAlive':false,
    'method':'GET',
    'trustAll':false,
  };
  var defaultURL = {
    'host':'localhost',
    'path':'/',
    'port':80,
    'protocol':'http',
    'query':''
  };

  var urlSpec;
  if (util.isString(options)) {
    urlSpec = options;
  } else if (util.isString(options.url)) {
    urlSpec = options['url'];
  }
  if (urlSpec) {
    var url = new URL(urlSpec);
    var port = url.getPort();
    if (port < 0) {
        port = url.getDefaultPort();
        if (port < 0) {
            port = 80;
        }
    }
    options = {
        'url':{
            'host':url.getHost(),
            'path':url.getPath(),
            'port':port,
            'protocol':url.getProtocol(),
            'query':url.getQuery()
            }
    };
  } else {
    options.url = util._extend(defaultURL, options.url);
  }
  // Fill in default values.
  options = util._extend(defaultOptions, options);

  // Attach the callback to be invoked when this object issues
  // a 'response' event.  
  if (reponseCallback) {
    self.once('response', reponseCallback);
  }
  
  console.log("Making an HTTP request: " + JSON.stringify(options));

  this.helper = HttpClientHelper.createHttpClient(this, options);
}
util.inherits(ClientRequest, EventEmitter);
exports.ClientRequest = ClientRequest;

/** End a request. */
ClientRequest.prototype.end = function() {
  this.helper.end();
};

// FIXME:
ClientRequest.prototype.write = function(data, encoding) {
   throw("Write not supported yet");
}

/** Internal method used to handle a response. The argument is an
 *  an instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 *  This method uses the data therein to construct an IncomingMessage object
 *  and pass that as an argument to the 'response' event of the ClientRequest.
 *  @param response The response from the server.
 */
ClientRequest.prototype._response = function(response, body) {
    var code = response.statusCode();
    if (code >= 400) {
        // An error occurred. Emit both an error event and a response event.
        this.emit('error', 'Received response code ' + code + ". " + response.statusMessage());
    }
    var message = new IncomingMessage(response, body);
    this.emit('response', message);
}

// NOTE: The following events are produce by IncomingMessage in Node.js
// From stream.Readable
// Event: 'readable'
// Event: 'data'
// Event: 'end'
// Event: 'close'
// Event: 'error'

/** Incoming message object type.  This should not be constructed by the user,
 *  but rather is constructed by the _response function above.
 *  An instance of this object type will be passed to the callback passed to the
 *  request() or get() functions. The instance contains:
 *  <ul>
 *  <li> body: a string with the body of the response. </li>
 *  <li> cookies: an array of strings with cookies returned. </li>
 *  <li> statusCode: an integer indicating the status of the response. </li>
 *  <li> statusMessage: a string with the status message of the response. </li>
 *  </ul>
 *  FIXME: At this time, only UTF-8-encoded string bodies are supported.
 *  @constructor
 *  @param response An instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 */
IncomingMessage = function(response, body) {
    this.body = body;
    this.cookies = response.cookies();
    this.statusCode = response.statusCode();
    this.statusMessage = response.statusMessage();
}
