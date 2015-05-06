// Module supporting publishing and subscribing on the Vert.x event bus.
// Authors: Patricia Derler and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
// This module is used by the accessors VertxPublish and VertxSubscribe
// at http://terraswarm.org/accessors.  See those accessors for the usage pattern.

////////////////////

var EventBusHelper = Java.type('ptolemy.actor.lib.jjs.modules.eventbus.EventBusHelper');
var events = require('events');

/** Construct an interface to the Vert.x bus. Use this as follows:
 *  <pre>
 *     var eventbus = require('eventbus');
 *     var bus = new eventbus.VertxBus();
 *     bus.subscribe('topic');
 *     bus.on('topic', 
 *      function(msg) {
 *        print(msg);
 *      }
 *     );
 *     bus.publish('topic', {'hello':'world'});
 *  </pre>
 *  This creates an interface to the event bus, subscribes to events
 *  with address 'topic', provides a handler for such events,
 *  and publishes a single event to that same address.
 *  The result should be to print:
 *  <pre>
 *    {'hello':'world'}
 *  </pre>
 *  on the standard output.
 *  <p>
 *  This implementation uses the event emitter pattern common in JavaScript.
 *  Once you have subscribed to an address, you can specify any number of
 *  handlers as follows:
 *  <pre>
 *     bus.on(address, function);
 *  </pre>
 *  To give a handler that reacts only to exactly one event with this address, use
 *  <pre>
 *     bus.once(address, function);
 *  </pre>
 *  To unsubscribe, use
 *  <pre>
 *     bus.removeListener(address, function);
 *  </pre>
 *  where the function is the same function passed to on().
 *  To unsubscribe all listeners to the address, use
 *  <pre>
 *     bus.unsubscribe(address);
 *  </pre>
 *  To unsubscribe to all addresses, use
 *  <pre>
 *     bus.unsubscribe();
 *  </pre>
 *  @constructor
 *  @param options A JSON record containing optional fields 'port' (an int)
 *   and 'host' (a string). These specify the network interface on the local host
 *   to use to connect to the Vert.x event bus cluster. This defaults to
 *   {'host':'localhost', 'port':0}, where a port value of 0 means "find
 *   an open port and use that. If no options parameter is given, then use
 *   the defaults.
 */
function VertxBus(options) {
    this.port = 0; // 0 specifies to find an open port.
    this.host = 'localhost';
    if (options) {
        this.port = options['port'] || 0;
        this.host = options['host'] || 'localhost';
    }
    this.helper = new EventBusHelper(this, this.port, this.host);
};
util.inherits(VertxBus, events.EventEmitter);

/** Notify this object of a received message from the event bus.
 *  This method assumes that the body of the message is a string
 *  in JSON format. It will throw an exception if this is not the case.
 *  @param address The address.
 *  @param body The message body
 */
VertxBus.prototype.notify = function(address, body) {
    try {
        var converted = JSON.parse(body);
        this.emit(address, converted);
    } catch (exception) {
        throw('Failed to parse JSON: ' + body + '\nException: ' + exception);
    }
};

/** Notify this object of a received reply from the event bus
 *  confirming completion of a point-to-point send.
 *  @param handler The callback function to invoke.
 *  @param message The message to send to the callback function.
 */
VertxBus.prototype.notifyReply = function(handler, message) {
    handler.apply(this, [message]);
};

/** Publish the specified data on the specified address.
 *  The data is first converted to a string representation in JSON format.
 *  @param address The address (or topic) of the event bus channel.
 *   This is a string.
 *  @param data The data to publish. This can be any JavaScript object
 *   that has a JSON representation using JSON.stringify().
 *  @see send()
 */
VertxBus.prototype.publish = function(address, data) {
    if (typeof(data) != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.publish(address, data);
};

/** Send the specified data to exactly one receiver at the specified address.
 *  This implements a point-to-point send, vs. the broadcast realized by publish().
 *  The data is first converted to a string representation in JSON format.
 *  According to the Vert.x documentation, the recipient will be chosen in a
 *  loosely round robin fashion.
 *  @param address The address (or topic) of the event bus channel.
 *   This is a string.
 *  @param data The data to publish. This can be a string or any JavaScript object
 *   that has a JSON representation using JSON.stringify().
 *  @param handler A function to invoke with argument address and reply body
 *   when the recipient has received the message, or null to not provide a reply handler.
 *  @see publish()
 */
VertxBus.prototype.send = function(address, data, handler) {
    if (typeof(data) != 'string') {
        data = JSON.stringify(data);
    }
    if (handler == null) {
        this.helper.send(address, data);
    } else {
        this.helper.send(address, data, handler);
    }
};

/** Set the reply to send when events are received in the future via a
 *  point-to-point send.
 *  @param reply The reply to respond with, or null to send no reply.
 *   this should be a string or any object that can be encoded as a
 *   JSON string.
 *  @see send(address, data)
 */
VertxBus.prototype.setReply = function(reply) {
    if (typeof(reply) != 'string') {
        reply = JSON.stringify(reply);
    }
    this.helper.setReply(reply);
};

/** Subscribe to events with the specified address.
 *  To react to those events, use on() or once() as explained above.
 */
VertxBus.prototype.subscribe = function(address) {
    this.helper.subscribe(address);
};

/** Unsubscribe to events with the specified address.
 */
VertxBus.prototype.unsubscribe = function(address) {
    if (address) {
        this.helper.unsubscribe(address);
        this.removeAllListeners(address);
    } else {
        this.helper.unsubscribe(null);
        this.removeAllListeners();
    }
};

exports.VertxBus = VertxBus;