package com.dell.iotmqttreporter.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.ReportKey;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;

public class CommandProcessor {

    private static final String TAG = "CommandProcessor";
    private static final String CMD_KEY = "cmd";
    private static final String METHOD_KEY = "method";
    private static final String CMD_REQUEST_KEY = "get";
    private static final String CMD_RESP_ACTION = "com.dell.iot.android.commandresponse";

    private Context ctx;
    private JsonParser parser = new JsonParser();

    public CommandProcessor(Context ctx) {
        this.ctx = ctx;
    }

    public void process(byte[] payload) {
        try {
            Log.d(TAG, "Processing message:  " + new String(payload));
            String json = new String(payload);
            JsonObject jsonObject = parser.parse(json).getAsJsonObject();
            String method = getMethod(jsonObject);
            ReportKey key = ReportKey.valueOf(getCmd(jsonObject));
            Log.d(TAG, "Processing " + method + " command:  " + key.toString());
            sendMessage(key);
        } catch (Exception e) {
            Log.e(TAG, "Error processing command request:  " + new String(payload));
            e.printStackTrace();
        }
    }

    private String getCmd(JsonObject jsonObject) {
        JsonElement element = jsonObject.get(CMD_KEY);
        if (element != null)
            return element.getAsString();
        else
            return null;
    }

    private String getMethod(JsonObject jsonObject) {
        JsonElement element = jsonObject.get(METHOD_KEY);
        if (element != null)
            return element.getAsString();
        else
            return null;
    }

    private void sendMessage(ReportKey requestedKey) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(CMD_RESP_ACTION);
        updateIntent.putExtra(CMD_REQUEST_KEY, requestedKey.toString());
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
