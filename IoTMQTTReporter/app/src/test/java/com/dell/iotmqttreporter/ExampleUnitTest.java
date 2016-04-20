package com.dell.iotmqttreporter;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private MqttClient client;
    private MqttConnectOptions options;
    private MqttTopic topic;

    public static final String OUTBROKER_KEY = "tcp://m11.cloudmqtt.com:12439";
    public static final String OUTCLIENTID_KEY = "CoreDataPublisher";
    public static final String OUTUSER_KEY = "vyrnnobp";
    public static final String OUTPASS_KEY = "q4u83TLxjCaI";
    public static final int KEEP_ALIVE = 360;
    public static final String OUTTOPIC_KEY= "CoreDataTopic";

    @Test
    public void testMQTTConnection() throws Exception {
        try {
            client = new MqttClient(OUTBROKER_KEY, OUTCLIENTID_KEY, new MemoryPersistence());
        } catch (MqttException e) {
                System.out.println("Problems creating MQTT outbound client.");
        }
        options = new MqttConnectOptions();
        options.setUserName(OUTUSER_KEY);
        options.setPassword(OUTPASS_KEY.toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(KEEP_ALIVE);
        topic = client.getTopic(OUTTOPIC_KEY);
        client.connect(options);
        System.out.println("Publishing outgoing update message: this is a test");
        MqttMessage message = new MqttMessage("this is a test".getBytes());
        message.setQos(1);
        message.setRetained(false);
        client.publish(topic.getName(), message);
        client.disconnect();

    }
}