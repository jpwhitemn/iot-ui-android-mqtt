package com.dell.iotmqttreporter.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class CommandListener implements MqttCallback {

    private static final String TAG = "CommandListener";

    private static final String INBROKER_KEY = "inbroker";
    private static final String INCLIENTID_KEY = "inclient";
    private static final String INUSER_KEY = "inuser";
    private static final String INPASS_KEY = "inpass";
    private static final String INTOPIC_KEY = "intopic";
    private static final int QOS = 0;
    private static final int KEEP_ALIVE = 30;

    private MqttClient client;
    private MqttConnectOptions options;

    private SharedPreferences prefs;

    private Context ctx;

    private CommandProcessor processor;

    public CommandListener(Context ctx) {
        this.ctx = ctx;
        processor = new CommandProcessor();
        if (prefs == null) {
            getSharedPreferences(ctx);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (prefs.getString(INTOPIC_KEY, null).equals(topic)) {
            processor.process(message.getPayload());
        }
    }

    private void getSharedPreferences(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }

    public void startListening() {
        try {
            client = new MqttClient(prefs.getString(INBROKER_KEY, null), prefs.getString(INCLIENTID_KEY, null), new MemoryPersistence());
            options = new MqttConnectOptions();
            options.setUserName(prefs.getString(INUSER_KEY, null));
            options.setPassword(prefs.getString(INPASS_KEY, null).toCharArray());
            options.setCleanSession(true);
            options.setKeepAliveInterval(KEEP_ALIVE);
            client.connect(options);
            client.setCallback(this);
            client.subscribe(prefs.getString(INTOPIC_KEY, null), QOS);
        } catch (MqttException e) {
            Log.e(TAG, "Problems creating MQTT inbound client.");
            e.printStackTrace();
        }
    }

    public void cleanup() {
        if (client != null) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                Log.e(TAG, "Problems cleaning up MQTT inbound client.");
                e.printStackTrace();
            }
        }
    }

}
