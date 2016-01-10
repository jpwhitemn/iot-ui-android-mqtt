/*******************************************************************************
 * Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.service.collection;

import static com.dell.iotmqttreporter.service.collection.CollectionConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dell.iotmqttreporter.R;
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
 *
 * Responsible for sending device collected data to the MQTT device service.
 * As a broadcast receiver, it is triggered by intent from each of the collectors/collection listeners
 */
public class CollectionUpdateSendor extends BroadcastReceiver {

    // TODO - abstract out commonalities between CollectionUpdateSendor and CommandResponseSendor

    private static final String TAG = "CollectionUpdateSendor";
    private static final Gson gson = new Gson();

    private MqttClient client;
    private MqttConnectOptions options;
    private MqttTopic topic;

    private SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (prefs == null)
            getSharedPreferences(context);
        if (prefs != null) {
            if (client == null)
                getClient();
            if (client != null) {
                // get the map containing the collected data by the device.
                HashMap updateMap = (HashMap) intent.getSerializableExtra(INTENT_UPD_KEY);
                if (updateMap.size() > 0) {
                    // add the device name to the data to be sent
                    updateMap.put(ReportKey.name, prefs.getString(PREF_DEVICE_NAME, null));
                    sendMessage(gson.toJson(updateMap));
                }
            } else
                Log.e(TAG, "No MQTT client available to send collection updates.");
        } else
            Log.e(TAG, "No preferences available to establish MQTT connection to send collection updates.");
    }

    private void getClient() {
        // TODO - extract client and options work to another class; combine with the CommandResponseSendor and CommandListner that need the same
        try {
            client = new MqttClient(prefs.getString(OUTBROKER_KEY, null), prefs.getString(OUTCLIENTID_KEY, null), new MemoryPersistence());
        } catch (MqttException e) {
            Log.e(TAG, "Problems creating MQTT outbound client.");
            e.printStackTrace();
        }
        options = new MqttConnectOptions();
        options.setUserName(prefs.getString(OUTUSER_KEY, null));
        options.setPassword(prefs.getString(OUTPASS_KEY, null).toCharArray());
        options.setCleanSession(true);
        options.setKeepAliveInterval(KEEP_ALIVE);
        topic = client.getTopic(prefs.getString(OUTTOPIC_KEY, null));
    }

    private void sendMessage(String json) {
        try {
            client.connect(options);
            Log.v(TAG, "Publishing outgoing update message: " + json);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(QOS);
            message.setRetained(false);
            client.publish(topic.getName(), message);
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getSharedPreferences(Context ctx) {
        PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }
}
