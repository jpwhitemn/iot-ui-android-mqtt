package com.dell.iotmqttreporter.service;

import android.util.Log;

public class CommandProcessor {

    private static final String TAG = "CommandProcessor";

    public void process(byte[] payload) {
        Log.d(TAG, "Processing message:  " + new String(payload));
    }
}
