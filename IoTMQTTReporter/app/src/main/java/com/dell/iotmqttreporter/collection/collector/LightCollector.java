/*******************************************************************************
 * © Copyright 2016, Dell, Inc.  All Rights Reserved.
 ******************************************************************************/
package com.dell.iotmqttreporter.collection.collector;

import static com.dell.iotmqttreporter.service.collection.CollectionConstants.*;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.iotmqttreporter.collection.LastCollected;
import com.dell.iotmqttreporter.collection.ReportKey;

import java.util.HashMap;

/**
 * Created by Jim on 1/10/2016.
 * <p/>
 * Listener to light data changes provided by the Android device.
 * <p/>
 * On a detected change, sends a map containing the light data that changed to the CollectionUpdateSendor (to send data out via MQTT).
 * <p/>
 * This listener is started/registered and unregistered by the CollectionService.
 */
public class LightCollector implements SensorEventListener {

    private static final String TAG = "LightChange";

    // required differences between current and last readings in order to send update
    private final static float CHG_LIGHT_LEVEL = 3;

    private float lastLightLevel;

    private Context ctx;

    public LightCollector(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        HashMap<ReportKey, String> lightUpdateMap = new HashMap<>();
        float lightLevel = event.values[0];
        if (Math.abs(lightLevel - lastLightLevel) > CHG_LIGHT_LEVEL) {
            Log.d(TAG, "Significant light level change detected:  " + lightLevel);
            lightUpdateMap.put(ReportKey.lightlevel, Float.toString(lightLevel));
            lastLightLevel = lightLevel;
            LastCollected.put(ReportKey.lightlevel, lastLightLevel);
            sendMessage(lightUpdateMap);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sendMessage(HashMap<ReportKey, String> lightUpdateMap) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(UPDATE_COLLECTION_ACTION);
        updateIntent.putExtra(INTENT_UPD_KEY, lightUpdateMap);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent);
    }
}
