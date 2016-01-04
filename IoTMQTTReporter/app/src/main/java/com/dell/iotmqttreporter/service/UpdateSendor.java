package com.dell.iotmqttreporter.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.ReportLevel;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;

public class UpdateSendor extends BroadcastReceiver {

    private static final String OUTBROKER_KEY = "outbroker";
    private static final String OUTCLIENTID_KEY = "outclient";
    private static final String OUTUSER_KEY = "outuser";
    private static final String OUTPASS_KEY = "outpass";
    private static final String OUTTOPIC_KEY = "outtopic";
    private static final int QOS = 0;
    private static final int KEEP_ALIVE = 30;

    private static final String TAG = "UpdateSendor";
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
                HashMap updateMap = (HashMap) intent.getSerializableExtra("updates");
                if (updateMap.size() > 0) {
                    updateMap.put(ReportLevel.name, prefs.getString("devicename", null));
                    sendMessage(gson.toJson(updateMap));
                }
            }
        }
    }

    private void getClient() {
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
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }
}
