/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.command;

import static com.dell.iotmqttreporter.service.command.CommandConstants.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dell.iotmqttreporter.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Listens on an MQTT topic for incoming command requests.
 * When a command request is received, it passes the request on to a command processor.
 */
@SuppressWarnings("ConstantConditions")  // to suppress unnecessary null pointer exception checks
public class CommandListener implements MqttCallback {

    private static final String TAG = "CommandListener";

    private MqttClient client;
    private SharedPreferences prefs;
    private CommandProcessor processor;

    /**
     * On creattion of the listner (created by and launched by the CommandService), create an instance of the processor to process any new command requests.
     */
    public CommandListener(Context ctx) {
        processor = new CommandProcessor(ctx);
        if (prefs == null) {
            getSharedPreferences(ctx);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Connection to MQTT command listener client lost.");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Receipt of command request complete.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // so long as the message received is for the incoming command, process the payload
        if (prefs != null) {
            if (prefs.getString(INTOPIC_KEY, null).equals(topic)) {
                processor.process(message.getPayload());
            } else
                Log.e(TAG, "Topic of incoming message does not match command message topic");
        } else
            Log.e(TAG, "Preferences not available to get MQTT connection for command message listening");
    }

    /**
     * Start listing for new MQTT command request messages.  Started by the command service.
     */
    public void startListening() {
        MqttConnectOptions options;
        try {
            // TODO - extract client and options work to another class; combine with the CommandResponseSendor and CollectionUpdateSendor that need the same
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

    /**
     * Stop listening for new MQTT command request messages.  Stopped by the command service.
     */
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

    // get the shared preferences holding the MQTT connection information along with the device name
    private void getSharedPreferences(Context ctx) {
        PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }
}
