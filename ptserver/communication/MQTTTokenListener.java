/*
 MQTTTokenListener is responsible for processing MQTT messages received,
 converting back to tokens and putting those tokens into appropriate queues.
 
 Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.communication;

import java.util.Date;
import java.util.HashMap;

import ptolemy.data.Token;
import ptolemy.kernel.util.Settable;
import ptserver.data.AttributeChangeToken;
import ptserver.data.CommunicationToken;
import ptserver.data.Tokenizer;

import com.ibm.mqtt.MqttSimpleCallback;

//////////////////////////////////////////////////////////////////////////
////MQTTTokenListener
/**
*  MQTTTokenListener is responsible for processing MQTT messages received,
*  converting back to tokens and putting those tokens into appropriate queues.
* 
* @author Anar Huseynov
* @version $Id$ 
* @since Ptolemy II 8.0
* @Pt.ProposedRating Red (ahuseyno)
* @Pt.AcceptedRating Red (ahuseyno)
*/
public class MQTTTokenListener implements MqttSimpleCallback {

    /**
     * Initialize the instance with a map of source queues and a topic that it listens to.
     * @param remoteSourceMap The map from source entity name to its token queue
     * @param topic The topic that the instance is listening to
     */
    public MQTTTokenListener(HashMap<String, RemoteSourceData> remoteSourceMap,
            HashMap<String, Settable> settableAttributesMap, String topic) {
        this._remoteSourceMap = remoteSourceMap;
        this._settableAttributesMap = settableAttributesMap;
        this._topic = topic;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** 
     * Callback method when the connection with the broker is lost.
     * @see com.ibm.mqtt.MqttSimpleCallback#connectionLost()
     */
    public void connectionLost() throws Exception {
        //TODO: handle connection lost case
        System.out.println("Connection was lost at " + new Date());
    }

    /**
     * Callback method when a message from the topic is received.
     * @param topicName The name of the topic from which the message was received.
     * @param payload The MQTT message.
     * @param qos The Quality of Service at which the message was delivered by the broker.
     * @param retained indicates if this message is retained by the broker.
     * @see com.ibm.mqtt.MqttSimpleCallback#publishArrived(java.lang.String, byte[], int, boolean)
     */
    public void publishArrived(String topicName, byte[] payload, int qos,
            boolean retained) throws Exception {
        if (!_topic.equals(topicName)) {
            return;
        }
        Tokenizer tokenizer = new Tokenizer(payload);
        Token token = null;
        while ((token = tokenizer.getNextToken()) != null) {
            // The listener is only concerned about the following types.
            // FIXME Figure out a better way to handle different token types here
            if (token instanceof CommunicationToken) {
                CommunicationToken communicationToken = (CommunicationToken) token;
                RemoteSourceData data = _remoteSourceMap.get(communicationToken
                        .getTargetActorName());
                data.getTokenQueue().add(communicationToken);
                //Notify remote sources to read from the queue.
                synchronized (data.getRemoteSource()) {
                    data.getRemoteSource().notifyAll();
                }
            } else if (token instanceof AttributeChangeToken) {
                AttributeChangeToken attributeChangeToken = (AttributeChangeToken) token;
                Settable changedObject = _settableAttributesMap.get(attributeChangeToken.
                        getTargetSettableName());
                changedObject.setExpression(attributeChangeToken.getExpression());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * Mapping from source actor name to its queue
     */
    private final HashMap<String, RemoteSourceData> _remoteSourceMap;
    /**
     * Mapping of actor (with settable attributes) name to the settable attributes.
     */
    private final HashMap<String, Settable> _settableAttributesMap;
    /**
     * The topic that the instance is subscribed to
     */
    private final String _topic;

}
