package com.dell.iotmqttreporter.service;

import android.util.Log;

import com.dell.iotmqttreporter.collection.ReportLevel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CommandProcessor {

    private static final String TAG = "CommandProcessor";
    private static final String CMD_KEY = "cmd";
    private static final String METHOD_KEY = "method";

    private JsonParser parser = new JsonParser();

    public void process(byte[] payload) {
        Log.d(TAG, "Processing message:  " + new String(payload));
        String json = new String(payload);
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        String method = getMethod(jsonObject);
        String cmd = getCmd(jsonObject);
        ReportLevel level = ReportLevel.valueOf(cmd);
        if (level != null && method != null) {
            Log.d(TAG, "Processing " + method + " command:  " + cmd);
            switch (level) {
                case batterylevel:
                    //TODO
                    break;
                case batteryhealth:
                    break;
                case batterytemperature:
                    break;
                case batteryvoltage:
                    break;
                case altitude:
                    break;
                case latitude:
                    break;
                case longitude:
                    break;
                case speed:
                    break;
                case direction:
                    break;
                case lightlevel:
                    break;
            }
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
}
