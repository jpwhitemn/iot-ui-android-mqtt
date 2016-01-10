/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.command;

import static com.dell.iotmqttreporter.service.command.CommandConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dell.iotmqttreporter.R;
import com.dell.iotmqttreporter.collection.LastCollected;
import com.dell.iotmqttreporter.collection.ReportKey;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Responsible for sending command responses to caller through MQTT
 * As a BroadcastReceiver, it gets request to send via Intent message
 * The intent could be a request to send a GET or PUT response.
 * On a GET response, it pulls the last updated data for the requested ReportKey along with the command request UUID and device name.
 * On a PUT response, it just sends back the UUID and device name as an indication that the request was satisfied.
 **/
public class CommandResponseSendor extends BroadcastReceiver {

    private static final String TAG = "CommandResponseSendor";
    private static final Gson gson = new Gson();

    private MqttClient client;
    private MqttConnectOptions options;
    private MqttTopic topic;

    private SharedPreferences prefs;

    /**
     * Process each intent for "com.dell.iot.android.commandresponse".  Send command response via MQTT.
     **/
    @Override
    public void onReceive(Context context, Intent intent) {
        // make sure preferences for MQTT properties are loaded (or get the defaults)
        if (prefs == null)
            getSharedPreferences(context);
        if (prefs != null) {
            // get the MQTT client
            if (client == null)
                getClient();
            if (client != null) {
                try {
                    // create a hash map that holds the key/value pairs for the MQTT response
                    // it will be converted to JSON
                    HashMap<ReportKey, String> response = new HashMap<>();
                    // get the UUID from the intent and put it in the hash map response holder
                    String uuid = intent.getStringExtra(UUID_KEY);
                    response.put(ReportKey.uuid, uuid);
                    // get the device name to add to the hash map response holder
                    response.put(ReportKey.name, prefs.getString(PREF_DEVICE_NAME, null));
                    // if a get request, pull the latest data for the ReportKey requested and add it to the hash map response holder
                    if (CMD_GET.equals(intent.getStringExtra(METHOD_KEY))) {
                        ReportKey key = ReportKey.valueOf(intent.getStringExtra(REPPORT_KEY));
                        Object data = LastCollected.get(key);
                        response.put(key, data.toString());
                    }
                    // send the response via MQTT - putting the hash map converted to JSON in the body
                    sendMessage(gson.toJson(response));
                } catch (Exception e) {
                    Log.e(TAG, "Unable to response to command request." + e.getMessage());
                    e.printStackTrace();
                }
            } else
                Log.e(TAG, "MQTT client could not be created.  Unable to send command response.");
        } else
            Log.e(TAG, "No preferences detected.  Unable to send command response.");
    }

    // get the MQTT client to publish a response message into the command response topic
    @SuppressWarnings("ConstantConditions") // to suppress null pointer warnings with regard to prefs
    private void getClient() {
        // TODO - extract client and options work to another class; combine with the CommandListener and CollectionUpdateSendor that need the same
        try {
            client = new MqttClient(prefs.getString(RESPBROKER_KEY, null), prefs.getString(RESPCLIENTID_KEY, null), new MemoryPersistence());
        } catch (MqttException e) {
            Log.e(TAG, "Problems creating MQTT command response client.");
            e.printStackTrace();
        }
        options = new MqttConnectOptions();
        options.setUserName(prefs.getString(RESPUSER_KEY, null));
        options.setPassword(prefs.getString(RESPPASS_KEY, null).toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(KEEP_ALIVE);
        topic = client.getTopic(prefs.getString(RESPTOPIC_KEY, null));
    }

    // send the MQTT message via the MQTT client
    private void sendMessage(String json) {
        try {
            client.connect(options);
            Log.d(TAG, "Topic name:  " + topic.getName());
            Log.d(TAG, "Publishing outgoing command response message: " + json);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(QOS);
            message.setRetained(false);
            client.publish(topic.getName(), message);
            client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, "Problem publishing outgoing command response message:  " + json);
            e.printStackTrace();
        }
    }

    // get the shared preferences holding the MQTT connection information along with the device name
    private void getSharedPreferences(Context ctx) {
        PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }
}
