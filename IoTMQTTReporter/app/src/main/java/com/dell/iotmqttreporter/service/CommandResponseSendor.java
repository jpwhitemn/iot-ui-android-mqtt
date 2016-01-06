package com.dell.iotmqttreporter.service;

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
import java.util.Map;

public class CommandResponseSendor extends BroadcastReceiver {

    private static final String RESPBROKER_KEY = "respbroker";
    private static final String RESPCLIENTID_KEY = "respclient";
    private static final String RESPUSER_KEY = "respuser";
    private static final String RESPPASS_KEY = "resppass";
    private static final String RESPTOPIC_KEY = "resptopic";
    private static final int QOS = 0;
    private static final int KEEP_ALIVE = 30;

    private static final String CMD_REQUEST_KEY = "get";

    private static final String TAG = "CommandResponseSendor";
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
                try {
                    ReportKey key = ReportKey.valueOf(intent.getStringExtra(CMD_REQUEST_KEY));
                    Object data = LastCollected.get(key);
                    HashMap<ReportKey, String> response = new HashMap<>();
                    response.put(key, data.toString());
                    response.put(ReportKey.name, prefs.getString("devicename", null));
                    sendMessage(gson.toJson(response));
                } catch (Exception e) {
                    Log.e(TAG, "Unable to response to command request for unknown report key: " + intent.getStringExtra(CMD_REQUEST_KEY));
                    e.printStackTrace();
                }
            }
        }
    }

    private void getClient() {
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

    private void sendMessage(String json) {
        try {
            client.connect(options);
            Log.d(TAG,"Topic name:  " + topic.getName());
            Log.d(TAG, "Publishing outgoing command response message: " + json);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(QOS);
            message.setRetained(false);
            client.publish(topic.getName(), message);
            client.disconnect();
        } catch (MqttException e) {
            Log.e(TAG,"Problem publishing outgoing command response message:  " + json);
            e.printStackTrace();
        }
    }

    private void getSharedPreferences(Context ctx) {
        PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences((ctx));
    }
}
