/*
 RemoteModel initializes by making needed replacement for sinks and sources.
 
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

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.actor.RemoteSink;
import ptserver.actor.RemoteSource;

import com.ibm.mqtt.IMqttClient;
import com.ibm.mqtt.MqttException;

//////////////////////////////////////////////////////////////////////////
//// RemoteModel
/**
 * RemoteModel initializes by making needed replacement for sinks and sources and 
 * sets up infrastructure for sending and receiving MQTT messages. 
 * 
 * The model can set up the infrastructure for client or server 
 * which differ slightly in actor replacement mechanisms
 * 
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class RemoteModel {

    /**
     * This enumerations specifies the remote model type: client or server.
     *
     */
    public enum RemoteModelType {
        /**
         * Client remote model type
         */
        CLIENT,
        /**
         * Server remote model type
         */
        SERVER;
    }

    /**
     * Create new instance of the remoteModel with the specified parameters.
     * @param mqttClientId the mqtt client id that mqtt broker requires 
     * @param subscriptionTopic the topic name that this model would subscribe to receive tokens from other remote model
     * @param publishingTopic the topic name that this model would publish its tokens to be received by other remote model
     * @param modelType the type of the model which must be either client or server
     */
    public RemoteModel(String mqttClientId, String subscriptionTopic,
            String publishingTopic, RemoteModelType modelType) {
        _tokenPublisher = new TokenPublisher(100, 100);
        _mqttClientId = mqttClientId;
        _subscriptionTopic = subscriptionTopic;
        _publishingTopic = publishingTopic;
        _modelType = modelType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                 ////
    /**
     * The quality of service that would be required from the MQTT broker.  All messages must be send or received only once.
     */
    public static final int QOS_LEVEL = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Sets the mqtt client instance that is connected to the broker
     * @param brokerAddress the network address of the MQTT broker
     * @throws MqttException if there is a problem connecting to the broker
     */
    public void setMqttClient(IMqttClient mqttClient) throws MqttException {
        _mqttClient = mqttClient;
        _tokenPublisher.setMqttClient(_mqttClient);
        _tokenPublisher.setTopic(_publishingTopic);
        _mqttClient.connect(_mqttClientId, true, (short) 10);
    }

    /**
     * Create a new instance of the RemoteSink either by replacing the targetEntity or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy, 
     * otherwise replace all entities connecting to it with one proxy
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see {@link RemoteSink}
     */
    private void createSink(ComponentEntity targetEntity,
            boolean replaceTargetEntity) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        RemoteSink remoteSink = new RemoteSink(getTopLevelActor(),
                targetEntity, replaceTargetEntity);
        remoteSink.setTokenPublisher(_tokenPublisher);
        _remoteSinkMap.put(remoteSink.getTargetEntityName(), remoteSink);
    }

    /**
     * Create a new instance of the RemoteSource either by replacing the targetEntity or by replacing all entities connected to it.
     * @param targetEntity The target entity to be processed
     * @param replaceTargetEntity replaceTargetEntity true to replace the target entity with the proxy, 
     * otherwise replace all entities connecting to it with one proxy
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     * @exception CloneNotSupportedException If port cloning is not supported
     * @see {@link RemoteSource}
     */
    private void createSource(ComponentEntity targetEntity,
            boolean replaceTargetEntity) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        RemoteSource remoteSource = new RemoteSource(getTopLevelActor(),
                targetEntity, replaceTargetEntity);
        RemoteSourceData data = new RemoteSourceData(remoteSource);
        _remoteSourceMap.put(remoteSource.getTargetEntityName(), data);
    }

    /**
     * Return the top level actor after the model was loaded.
     * @return the topLevelActor of the model
     */
    public CompositeActor getTopLevelActor() {
        return _topLevelActor;
    }

    /**
     * Load the model from the specified URL and set up MQTT infrastructure for communicating with another remote model.
     * 
     * @param modelURL the model URL to be loaded
     * @return the top level actor of the model
     * @throws Exception if there is a problem parsing the model, connecting to the mqtt broker or replacing actors.
     */
    public CompositeActor loadModel(URL modelURL) throws Exception {
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

        //TODO: Implement MoMLFilter for remote actor replacement
        //The implementation below fails because relations have old actor names without "_remote" suffix.
        //        MoMLParser.addMoMLFilter(new MoMLFilter() {
        //
        //            @Override
        //            public void filterEndElement(NamedObj container,
        //                    String elementName, StringBuffer currentCharData,
        //                    String xmlFile) throws Exception {
        //                // TODO Auto-generated method stub
        //                System.out.println(container);
        //                if (container.getName().equals("_remote")
        //                        && container instanceof Parameter
        //                        && container.getContainer() instanceof ComponentEntity) {
        //                    Parameter param = (Parameter) container;
        //                    ComponentEntity actor = (ComponentEntity) container
        //                            .getContainer();
        //
        //                    if ("source".equals(param.getExpression())) {
        //                        RemoteSource remoteSource = new RemoteSource(
        //                                (CompositeActor) actor.getContainer(), actor,
        //                                true);
        //                        RemoteSourceData data = new RemoteSourceData(
        //                                remoteSource);
        //                        remoteSourceMap.put(remoteSource.getName(), data);
        //                    } else if ("sink".equals(param.getExpression())) {
        //                        RemoteSink remoteSink = new RemoteSink(
        //                                (CompositeActor) actor.getContainer(), actor,
        //                                true);
        //                        remoteSink.setTokenPublisher(tokenPublisher);
        //                        remoteSinkMap.put(remoteSink.getTargetEntityName(),
        //                                remoteSink);
        //                    }
        //                }
        //            }
        //
        //            @Override
        //            public String filterAttributeValue(NamedObj container,
        //                    String element, String attributeName,
        //                    String attributeValue, String xmlFile) {
        //                System.out.println(container);
        //                System.out.println(element);
        //                System.out.println(attributeName);
        //                System.out.println(attributeValue);
        //
        //                return attributeValue;
        //            }
        //        });
        HashSet<ComponentEntity> unneededActors = new HashSet<ComponentEntity>();
        _topLevelActor = (CompositeActor) parser.parse(null, modelURL);
        for (Object obj : getTopLevelActor().entityList()) {
            ComponentEntity actor = (ComponentEntity) obj;
            Attribute attribute = actor.getAttribute("_remote");
            boolean isSinkOrSource = false;
            if (attribute instanceof Parameter) {
                Parameter parameter = (Parameter) attribute;
                if (parameter.getExpression().equals("source")) {
                    switch (_modelType) {
                    case CLIENT:
                        createSink(actor, false);
                        break;
                    case SERVER:
                        createSource(actor, true);
                        break;
                    }
                    isSinkOrSource = true;
                } else if (parameter.getExpression().equals("sink")) {
                    switch (_modelType) {
                    case CLIENT:
                        createSource(actor, false);
                        break;
                    case SERVER:
                        createSink(actor, true);
                        break;
                    }
                    isSinkOrSource = true;
                }
            }
            if (!isSinkOrSource && _modelType == RemoteModelType.CLIENT) {
                unneededActors.add(actor);
            }
        }
        for (ComponentEntity componentEntity : unneededActors) {
            componentEntity.setContainer(null);
        }

        _mqttClient.registerSimpleHandler(new MQTTTokenListener(
                _remoteSourceMap, _settableAttributesMap, _subscriptionTopic));
        _mqttClient.subscribe(new String[] { _subscriptionTopic },
                new int[] { QOS_LEVEL });
        return _topLevelActor;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The mapping from the original source actor name to its remote source actor and queue. 
     */
    private final HashMap<String, RemoteSourceData> _remoteSourceMap = new HashMap<String, RemoteSourceData>();

    /**
     * The mapping from the original sink actor name to its remote sink. 
     */
    private final HashMap<String, RemoteSink> _remoteSinkMap = new HashMap<String, RemoteSink>();

    /**
     * The mapping from the original settable object name to the remote representation. 
     */
    private final HashMap<String, Settable> _settableAttributesMap = new HashMap<String, Settable>();

    /**
     * The token publisher used to batch tokens sent by the remote sink
     */
    private final TokenPublisher _tokenPublisher;

    /**
     * The top level actor of the loaded model
     */
    private CompositeActor _topLevelActor;

    /**
     * The mqtt client connection
     */
    private IMqttClient _mqttClient;

    /**
     * The topic used to listen for incoming mqtt messages
     */
    private final String _subscriptionTopic;

    /**
     * The topic used to publish outgoing mqtt messages
     */
    private final String _publishingTopic;
    /**
     * The client id required by the mqtt broker
     */
    private final String _mqttClientId;
    /**
     * The type of the remote model
     */
    private final RemoteModelType _modelType;

}
